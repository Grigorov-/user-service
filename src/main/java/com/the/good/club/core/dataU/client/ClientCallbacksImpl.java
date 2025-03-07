package com.the.good.club.core.dataU.client;

import com.the.good.club.core.service.UserService;
import com.the.good.club.dataU.sdk.ProxyUClientCallbacks;
import com.the.good.club.dataU.sdk.protocol.DataRetrieveResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClientCallbacksImpl implements ProxyUClientCallbacks {
    private static final Logger logger = LoggerFactory.getLogger(ClientCallbacksImpl.class);

    private final UserService userService;

    public ClientCallbacksImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onPublicKeyReceived(String publicKey, String correlationMessage) {
        try {
            userService.requestPermissions(publicKey, correlationMessage);
        } catch (Exception e) {
            logger.error("Unable to request permissions", e);
        }
    }

    @Override
    public void onGrantedStatusReceived(boolean granted, String permissionMessage) {
        logger.info("permissionMessage received: " + permissionMessage);
    }

    @Override
    public void onDataRetrieveResponseReceived(DataRetrieveResponse dataRetrieveResponse) {
        logger.info("dataRetrieveResponse received: " + dataRetrieveResponse);
    }
}
