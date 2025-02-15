package com.the.good.club.core.dataU.client;

import com.the.good.club.dataU.sdk.ProxyUClientCallbacks;
import com.the.good.club.core.service.UserPermissionService;
import com.the.good.club.dataU.sdk.protocol.DataRetrieveResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClientCallbacksImpl implements ProxyUClientCallbacks {
    private static final Logger logger = LoggerFactory.getLogger(ClientCallbacksImpl.class);

    private final UserPermissionService userPerminssionService;

    public ClientCallbacksImpl(UserPermissionService userPerminssionService) {
        this.userPerminssionService = userPerminssionService;
    }

    @Override
    public void onPublicKeyReceived(String publicKey, String correlationMessage) {
        try {
            userPerminssionService.requestPermissions(publicKey, correlationMessage);
        } catch (Exception e) {
            logger.error("Unable to request permissions", e);
        }
    }

    @Override
    public void onGrantedStatusReceived(boolean granted, String permissionMessage) {

    }

    @Override
    public void onDataRetrieveResponseReceived(DataRetrieveResponse dataRetrieveResponse) {

    }
}
