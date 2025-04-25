package com.the.good.club.dataU.sdk;

import com.google.protobuf.ByteString;
import com.the.good.club.dataU.sdk.protocol.*;
import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.the.good.club.dataU.sdk.ClientUtils.*;
import static com.the.good.club.dataU.sdk.DataIdentificationGraphHelper.*;
import static java.util.Base64.getEncoder;


@Service
public class ProxyUClient {
    private static final Logger logger = Logger.getLogger(ProxyUClient.class.getName());

    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String FAILED_STATUS = "FAILED";
    private static final String APPLICATION_NODE_MIME_TYPE = "application/datau+node";

    private final ProxyUIntegrationGrpc.ProxyUIntegrationBlockingStub proxyUIntegrationSyncStub;

    private final ProxyUIntegrationGrpc.ProxyUIntegrationStub proxyUIntegrationAsyncStub;

    private final ProxyUClientStorage proxyUClientStorage;

    private final ProxyUClientCallbacks proxyUClientCallbacks;

    private final BlockingQueue<DataRequest> dataRequests;

    @Value("${proxyU.dataIdGraph:didgraph.yml}")
    private String dataIdentificationGraphFileName;

    @Value("${proxyU.localizedDataIdGraph:en.yml}")
    private String localizedDataIdentificationGraphFileName;

    /**
     * Construct client for accessing ProxyU server using the existing channel
     * @param channel - ssl channel built by proxyUChannel bean factory
     * @param proxyUClientStorage - interface for storing user data
     */
    public ProxyUClient(
            Channel channel,
            ProxyUClientStorage proxyUClientStorage,
            ProxyUClientCallbacks proxyUClientCallbacks
    ) {
        proxyUIntegrationSyncStub = ProxyUIntegrationGrpc.newBlockingStub(channel);
        proxyUIntegrationAsyncStub = ProxyUIntegrationGrpc.newStub(channel);
        this.proxyUClientStorage = proxyUClientStorage;
        this.proxyUClientCallbacks = proxyUClientCallbacks;
        this.dataRequests = new LinkedBlockingQueue<>();
    }

    @PostConstruct
    public void init() {
        loadDataIdentificationGraph(dataIdentificationGraphFileName);
        new Thread(this::processRequests).start();
    }

    /**
     * Async server-streaming.
     * Calls correlation RPC to obtain the correlation message for a data subject.
     * Use this correlation message to receive data subject's public key.
     */
    public String createCorrelationMessage() {
        info("*** Correlation");

        LinkedBlockingQueue<String> correlationQueue = new LinkedBlockingQueue<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        CorrelationRequest correlationRequest = CorrelationRequest.getDefaultInstance();

        StreamObserver<CorrelationResponse> responseObserver = new StreamObserver<>() {

            @Override
            public void onNext(CorrelationResponse correlationResponse) {
                switch (correlationResponse.getResponseCase()) {
                    case CORRELATION_MESSAGE -> {
                        String correlationMessage = correlationResponse.getCorrelationMessage();
                        info("Got Correlation message: {0}", correlationMessage);
                        correlationQueue.offer(correlationMessage);
                        countDownLatch.countDown();
                    }
                    case CORRELATION_RESULT -> {
                        CorrelationResult correlationResult = correlationResponse.getCorrelationResult();
                        String correlationMessage = correlationResult.getCorrelationMessage();
                        String publicKey = getEncoder().encodeToString(correlationResult.getPublicKey().toByteArray());

                        info("Got Public key: {0} for correlation {1}", publicKey, correlationMessage);
                        proxyUClientCallbacks.onPublicKeyReceived(publicKey, correlationMessage);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                warning("Correlation failed: {0}", Status.fromThrowable(throwable));
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                info("Finished correlation!");
            }
        };

        try {
            proxyUIntegrationAsyncStub.correlation(correlationRequest, responseObserver);
        } catch (RuntimeException e) {
            warning("Correlation RPC failed: {0}", e.getMessage());
        }

        try {
            if (countDownLatch.await(1, TimeUnit.MINUTES) && !correlationQueue.isEmpty()) {
                return correlationQueue.poll();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        warning("Correlation message was not received within 1 minutes.");
        return "";
    }


    /**
     * Async server streaming. Calls permission RPC. Used to obtain the permission message which needs to be sent to the
     * data subject in oder to obtain their consent for sharing the group of data referenced by data UUID sent as param;
     * the status of the permission request is available on the onGrantedStatusReceived callback
     * @param publicKey - data subject public key
     * @param data - UUID of the data requested - the parent of a group of data for which the permission is requested
     * @param process - UUID of the process
     * @param reason - UUID of the reason
     * @param policy - SHA3-256 hash bytes of the previously published legal policy
     * @param from - Unix UTC timestamp from when this permission is valid
     * @param until - Unix UTC timestamp until when this permission is valid
     * @param amount - how often this data will be retrieved, 0 sets no limit
     * @param level - the lowest level of data subject identification required
     */
    public String createPermissionRequestMessage(
            ByteString publicKey, ByteString data, ByteString process, ByteString reason, ByteString policy,
            long from, long until, int amount, int level
    ) {
        info("*** Permission Request");

        LinkedBlockingQueue<String> permissionMessageQueue = new LinkedBlockingQueue<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        PermissionRequest permissionRequest = PermissionRequest.newBuilder()
                .setPublicKey(publicKey)
                .setData(data)
                .setProcess(process)
                .setReason(reason)
                .setPolicy(policy)
                .setFrom(from)
                .setUntil(until)
                .setAmount(amount)
                .setLevel(level)
                .build();

        StreamObserver<PermissionResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(PermissionResponse permissionResponse) {
                switch (permissionResponse.getResponseCase()) {
                    case PERMISSION_MESSAGE -> {
                        String permissionMessage = permissionResponse.getPermissionMessage();
                        info("Received permission message for data subject: {0}", permissionMessage);
                        permissionMessageQueue.offer(permissionMessage);
                        countDownLatch.countDown();
                    }
                    case PERMISSION_RESULT -> {
                        String permissionMessage = permissionResponse.getPermissionResult().getPermissionMessage();
                        boolean granted = permissionResponse.getPermissionResult().getGranted();
                        info("Received granted status: {0} for permission message: {1}", granted, permissionMessage);
                        proxyUClientCallbacks.onGrantedStatusReceived(granted, permissionMessage);
                    }
                    case RESPONSE_NOT_SET -> info("Received RESPONSE_NOT_SET");
                }
            }

            @Override
            public void onError(Throwable throwable) {
                warning("Permission request creation failed: {0}", Status.fromThrowable(throwable));
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                info("Permission request completed!");
            }
        };

        try {
            proxyUIntegrationAsyncStub.permission(permissionRequest, responseObserver);
        } catch (RuntimeException e) {
            warning("Permission RPC failed: {0}", e.getMessage());
        }

        try {
            if (countDownLatch.await(1, TimeUnit.MINUTES) && !permissionMessageQueue.isEmpty()) {
                return permissionMessageQueue.poll();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        warning("Permission message was not received within 1 minutes.");
        return "";
    }


    /**
     * Blocking unary call. Calls submitDocument RPC for submitting a terms & conditions document
     * @param legalPolicyDocument - contains URL and hash of the terms & conditions document
     * @return status of the submission: SUCCESS | FAILED
     */
    public String submitDocument(LegalPolicyDocument legalPolicyDocument) {

        SubmitDocumentRequest submitDocumentRequest = SubmitDocumentRequest.newBuilder()
                .setUrl(legalPolicyDocument.getUrl())
                .setHash(ByteString.copyFrom(HexFormat.of().parseHex(legalPolicyDocument.getHash())))
                .build();

        SubmitDocumentResponse submitDocumentResponse;

        try {
            submitDocumentResponse = proxyUIntegrationSyncStub.submitDocument(submitDocumentRequest);
        } catch (StatusRuntimeException e) {
            warning("SubmitDocument RPC failed: {0}", e.getStatus());
            return FAILED_STATUS;
        }

        if (submitDocumentResponse.getOk()) {
            info("Document submitted successfully!");
            return SUCCESS_STATUS;
        } else {
            info("Document submission failed!");
            return FAILED_STATUS;
        }
    }


    /**
     * Bidirectional RPC streaming call. Calls data RPC and handles all possible responses (DataResponse.ResponseCase)
     */
     void processRequests() {
        info("*** Process Data");

        StreamObserver<DataResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(DataResponse dataResponse) {
                StreamObserver<DataRequest> requestObserver = proxyUIntegrationAsyncStub.data(this);

                switch (dataResponse.getResponseCase()) {
                    case RETRIEVE_RESPONSE -> {
                        DataRetrieveResponse dataRetrieveResponse = dataResponse.getRetrieveResponse();

                        if (dataRetrieveResponse.getError() != 0) {
                            info("Data Retrieve failed with {0}", dataRetrieveResponse.getError());
                        }

                        String values = dataRetrieveResponse.getFieldsList()
                                .stream()
                                .map(dataField -> dataField.getValue().toStringUtf8())
                                .collect(Collectors.joining(","));

                        UUID requestedDataUUID = byteStringToUUID(dataRetrieveResponse.getData());
                        info("Retrieved data: {0} {1}", requestedDataUUID, values);
                        proxyUClientCallbacks.onDataRetrieveResponseReceived(dataRetrieveResponse);
                    }
                    case RETRIEVE_REQUEST -> {
                        ByteString subjectPublicKey = ByteString.copyFrom(dataResponse.getRetrieveRequest().getPublicKey().toByteArray());
                        ByteString dataUUID = ByteString.copyFrom(dataResponse.getRetrieveRequest().getData().toByteArray());
                        ByteString process = ByteString.copyFrom(dataResponse.getRetrieveRequest().getProcess().toByteArray());

                        String dataUUIDString = byteStringToUUIDString(dataUUID);
                        DataIdentificationGraphNode graphNode = dataIdentificationGraph.get(dataUUIDString);

                        info("[RetrieveRequest] Extracting user data for node {0}", dataUUIDString);

                        List<DataField> dataFields = new ArrayList<>();

                        // extract user data for node's children if type is application/datau+node
                        if (APPLICATION_NODE_MIME_TYPE.equals(graphNode.getMimeType())) {
                            extractUserDataForNode(dataUUIDString, subjectPublicKey, process, dataFields);
                        } else {
                            UserData userData = proxyUClientStorage.extractUserData(subjectPublicKey, dataUUID, process);
                            info("Extracted user data for node {0}: Mime: {1} value: {2}",
                                    dataUUIDString, userData.getMimeType(), userData.getValue()
                            );
                        }

                        DataRetrieveResponse retrieveResponse = DataRetrieveResponse.newBuilder()
                                .setData(dataUUID)
                                .setError(0)
                                .addAllFields(dataFields)
                                .setProcess(process)
                                .setPublicKey(subjectPublicKey)
                                .build();

                        DataRequest dataRequest = DataRequest.newBuilder()
                                .setRetrieveResponse(retrieveResponse)
                                .build();

                        requestObserver.onNext(dataRequest);
                    }
                    case SUPPLY_REQUEST -> {
                        ByteString publicKey = ByteString.copyFrom(dataResponse.getSupplyRequest().getPublicKey().toByteArray());
                        ByteString dataUUID = ByteString.copyFrom(dataResponse.getSupplyRequest().getData().toByteArray());
                        ByteString process = ByteString.copyFrom(dataResponse.getSupplyRequest().getProcess().toByteArray());

                        if (BULK_PROCESS.equals(getProcessName(process))) {
                            proxyUClientStorage.saveOrUpdateBulkUserData(
                                    publicKey,
                                    dataUUID,
                                    process,
                                    dataResponse.getSupplyRequest().getFilename(),
                                    dataResponse.getSupplyRequest().getMime(),
                                    dataResponse.getSupplyRequest().getValue()
                            );
                        } else {
                            // save user data only for NON application/datau+node type nodes
                            if(!APPLICATION_NODE_MIME_TYPE.equals(dataResponse.getSupplyRequest().getMime())) {
                                info("[SupplyRequest] Received user data: UUID: {0}, Mime: {1}, value: {2}",
                                        byteStringToUUIDString(dataUUID),
                                        dataResponse.getSupplyRequest().getMime(),
                                        dataResponse.getSupplyRequest().getValue()
                                );
                                proxyUClientStorage.saveOrUpdateUserData(
                                        publicKey,
                                        dataUUID,
                                        process,
                                        dataResponse.getSupplyRequest().getMime(),
                                        dataResponse.getSupplyRequest().getValue()
                                );
                            }
                        }
                    }
                    case DELETE_REQUEST -> {
                        ByteString publicKey = ByteString.copyFrom(dataResponse.getDeleteRequest().getPublicKey().toByteArray());
                        ByteString dataUUID = ByteString.copyFrom(dataResponse.getDeleteRequest().getData().toByteArray());
                        ByteString process = ByteString.copyFrom(dataResponse.getDeleteRequest().getProcess().toByteArray());

                        info("Deleting user data {0} for subject {1}!",
                                byteStringToUUID(dataUUID),
                                byteStringToUUID(publicKey)
                        );
                        proxyUClientStorage.deleteData(publicKey, dataUUID, process);

                        DataDeleteResponse deleteResponse = DataDeleteResponse.newBuilder()
                                .setData(dataResponse.getDeleteRequest().getData())
                                .setPublicKey(dataResponse.getDeleteRequest().getPublicKey())
                                .setError(0)
                                .build();

                        DataRequest dataRequest = DataRequest.newBuilder()
                                .setDeleteResponse(deleteResponse)
                                .build();

                        requestObserver.onNext(dataRequest);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                warning("RetrieveData Failed: {0}", Status.fromThrowable(throwable));
            }

            @Override
            public void onCompleted() {
                info("Finished RetrieveData");
            }
        };

        StreamObserver<DataRequest> requestObserver = proxyUIntegrationAsyncStub.data(responseObserver);

        // listen for and send requests
        while (true) {
            try {
                // wait for requests
                DataRequest request = dataRequests.take();
                DataRetrieveRequest dataRetrieveRequest = request.getRetrieveRequest();
                info("Sending Request for data: {0} (UUID)", byteStringToUUID(dataRetrieveRequest.getData()));

                // send request
                requestObserver.onNext(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void info(String msg, Object... params) {
        logger.log(Level.INFO, msg, params);
    }

    void warning(String msg, Object... params) {
        logger.log(Level.WARNING, msg, params);
    }

    public BlockingQueue<DataRequest> getDataRequests() {
        return dataRequests;
    }

    private void extractUserDataForNode(
            String dataUUIDString, ByteString publicKey, ByteString process, List<DataField> dataFields
    ) {
        List<ByteString> childrenUUIDs = getChildrenUUIDs(UUIDStringToByteString(dataUUIDString));

        for (ByteString childUUID : childrenUUIDs) {
            String childUUIDString = byteStringToUUIDString(childUUID);
            DataIdentificationGraphNode dataIdentificationGraphNode = dataIdentificationGraph.get(childUUIDString);

            // recursively extract user data for children with mime type application/datau+node
            if(APPLICATION_NODE_MIME_TYPE.equals(dataIdentificationGraphNode.getMimeType())) {
                extractUserDataForNode(dataIdentificationGraphNode.getKey(), publicKey, process, dataFields);
            } else {
                UserData childData = proxyUClientStorage.extractUserData(publicKey, childUUID, process);
                if (childData == null) {
                    continue;
                }
                info(
                    "Extracted user data: UUID: {0}, Mime: {1}, value: {2}",
                        childUUIDString, childData.getMimeType(), childData.getValue()
                );

                DataField dataField = DataField.newBuilder()
                        .setMime(childData.getMimeType())
                        .setUuid(childUUID)
                        .setValue(ByteString.copyFromUtf8(childData.getValue()))
                        .build();

                dataFields.add(dataField);
            }
        }
    }
}
