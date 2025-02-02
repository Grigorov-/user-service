package com.the.good.club.core.service;

import com.google.protobuf.ByteString;
import com.the.good.club.core.connector.EmailConnector;
import com.the.good.club.dataU.sdk.DataIdentificationGraphHelper;
//import com.the.good.club.core.dataU.sdk.ProxyUClient;
import com.the.good.club.core.spi.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import static com.google.protobuf.ByteString.copyFrom;
import static com.the.good.club.dataU.sdk.ClientUtils.UUIDStringToByteString;

@Service
public class UserPermissionService {
    private static final Logger log = LoggerFactory.getLogger(UserPermissionService.class);

    private static final String SUBJECT = "The good club registration";

    private final String DASHBOARDU_ENDPOINT = "https://dev.datau.eu/#/decode";

    private final EmailConnector emailConnector;
//    private final ProxyUClient proxyUClient;
    private final UserRepository userRepository;

    public UserPermissionService(EmailConnector emailConnector, UserRepository userRepository
                                 //@Lazy ProxyUClient proxyUClient) {
    ) {
        this.emailConnector = emailConnector;
//        this.proxyUClient = proxyUClient;
        this.userRepository = userRepository;
    }

    public void requestCorrelation(String email) {
        String correlationMessage = "";//proxyUClient.createCorrelationMessage();
        String correlationLink = getCorrelationLink(correlationMessage);
        userRepository.save(email, correlationMessage);
        emailConnector.sendSimpleMessage(email, SUBJECT, correlationLink);
    }

    public void requestPermissions(String publicKey, String correlationId) throws NoSuchAlgorithmException, IOException {
        String email = userRepository.getUserEmailByCorrelationId(correlationId);
        String permissionRequestMessage = getPermissionRequestMessage(publicKey);
        emailConnector.sendSimpleMessage(email, SUBJECT, permissionRequestMessage);
    }

    private String getPermissionRequestMessage(String publicKey) throws NoSuchAlgorithmException, IOException {
        ByteString dataRightsSubject = getDataRightsSubject(publicKey);

        // use one of the fields names present in the data identification graph; see them in didgraph.yml from the SDK
        ByteString data = DataIdentificationGraphHelper.getNodeUUID("Personal Information");

        ByteString individualProcess = DataIdentificationGraphHelper.getProcessUUID(DataIdentificationGraphHelper.INDIVIDUAL_PROCESS);

        // optional, send some random value
        ByteString reason = UUIDStringToByteString(UUID.randomUUID().toString());

        ByteString policyHash = getTermsAndConditionsPolicyHash();

        String permissionRequestMessage = "";// proxyUClient.createPermissionRequestMessage(
//                dataRightsSubject, data, individualProcess, reason, policyHash, 1605087413, 1893456000, 0, 1
//        );

        return DASHBOARDU_ENDPOINT + "?message=" + URLEncoder.encode(permissionRequestMessage, StandardCharsets.UTF_8);
    }

    private static ByteString getTermsAndConditionsPolicyHash() throws NoSuchAlgorithmException, IOException {
        // hash for the terms and conditions document
        // -b Displayed when read more button is clicked before the user give consent.
        //TODO - add real terms and conditions
        String fileUrl = "https://generator.lorem-ipsum.info/terms-and-conditions";
        MessageDigest messageDigest = MessageDigest.getInstance("SHA3-256");

        byte[] fileHash;
        try(InputStream inputStream = new URL(fileUrl).openStream()) {
            fileHash = messageDigest.digest(inputStream.readAllBytes());
        }

        return copyFrom(fileHash);
    }

    private static ByteString getDataRightsSubject(String publicKey) {
        return copyFrom(Base64.getDecoder().decode(publicKey));
    }

    private String getCorrelationLink(String correlationMessage) {
        return DASHBOARDU_ENDPOINT + "?message=" + URLEncoder.encode(correlationMessage, StandardCharsets.UTF_8);
    }
}
