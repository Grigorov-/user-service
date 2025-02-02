package com.the.good.club.core.dataU.client;

import com.google.protobuf.ByteString;
import com.the.good.club.dataU.sdk.ProxyUClientStorage;
import com.the.good.club.dataU.sdk.UserData;
import org.springframework.stereotype.Component;

@Component
public class ProxyUStorage implements ProxyUClientStorage {
    @Override
    public void saveOrUpdateBulkUserData(ByteString subject, ByteString dataUUID, ByteString process, ByteString filename, String mime, ByteString dataValue) {
        ProxyUClientStorage.super.saveOrUpdateBulkUserData(subject, dataUUID, process, filename, mime, dataValue);
    }

    @Override
    public void saveOrUpdateUserData(ByteString subject, ByteString dataUUID, ByteString process, String mime, ByteString dataValue) {

    }

    @Override
    public UserData extractUserData(ByteString subject, ByteString dataUUID, ByteString process) {
        return null;
    }

    @Override
    public void deleteData(ByteString subject, ByteString dataUUID, ByteString process) {

    }
}
