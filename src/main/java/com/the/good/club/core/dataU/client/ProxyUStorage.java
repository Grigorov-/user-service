package com.the.good.club.core.dataU.client;

import com.google.protobuf.ByteString;
import com.the.good.club.core.spi.UserDataRepository;
import com.the.good.club.dataU.sdk.DataIdentificationGraphNode;
import com.the.good.club.dataU.sdk.ProxyUClientStorage;
import com.the.good.club.dataU.sdk.UserData;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.the.good.club.dataU.sdk.ClientUtils.UUIDStringToByteString;
import static com.the.good.club.dataU.sdk.ClientUtils.byteStringToUUIDString;
import static com.the.good.club.dataU.sdk.DataIdentificationGraphHelper.dataIdentificationGraph;
import static com.the.good.club.dataU.sdk.DataIdentificationGraphHelper.getChildrenUUIDs;
import static com.the.good.club.dataU.sdk.DataIdentificationGraphHelper.getNodeName;
import static java.util.Base64.getEncoder;

@Component
public class ProxyUStorage implements ProxyUClientStorage {
    private static final String APPLICATION_NODE_MIME_TYPE = "application/datau+node";

    private final UserDataRepository userDataRepository;

    public ProxyUStorage(UserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
    }

    @Override
    public void saveOrUpdateBulkUserData(ByteString subject, ByteString dataUUID, ByteString process, ByteString filename, String mime, ByteString dataValue) {
        ProxyUClientStorage.super.saveOrUpdateBulkUserData(subject, dataUUID, process, filename, mime, dataValue);
    }

    @Override
    public void saveOrUpdateUserData(ByteString subjectPublicKey, ByteString dataUUID, ByteString process, String mime, ByteString dataValue) {
        userDataRepository.save(encodePublicKey(subjectPublicKey), byteStringToUUIDString(dataUUID),
                getNodeName(dataUUID), byteStringToUUIDString(process), mime, dataValue.toStringUtf8());
    }

    @Override
    public UserData extractUserData(ByteString userPublicKey, ByteString dataUUID, ByteString process) {
        return userDataRepository.getById(encodePublicKey(userPublicKey), byteStringToUUIDString(dataUUID));
    }

    @Override
    public void deleteData(ByteString userPublicKey, ByteString dataUUID, ByteString process) {
        //TODO how process should be handled here
        deleteData(userPublicKey, byteStringToUUIDString(dataUUID));
    }

    private String encodePublicKey(ByteString publicKey) {
        return getEncoder().encodeToString(publicKey.toByteArray());
    }

    private void deleteData(ByteString userPublicKey, String dataUUIDString) {
        DataIdentificationGraphNode node = dataIdentificationGraph.get(dataUUIDString);
        if (node == null) return;

        if (APPLICATION_NODE_MIME_TYPE.equals(node.getMimeType())) {
            List<ByteString> childrenUUIDs = getChildrenUUIDs(UUIDStringToByteString(dataUUIDString));
            for (ByteString childUUID : childrenUUIDs) {
                String childUUIDString = byteStringToUUIDString(childUUID);
                DataIdentificationGraphNode childNode = dataIdentificationGraph.get(childUUIDString);
                if (childNode != null) {
                    deleteData(userPublicKey, childNode.getKey());
                }
            }
        } else {
            userDataRepository.delete(encodePublicKey(userPublicKey), dataUUIDString);
        }
    }
}
