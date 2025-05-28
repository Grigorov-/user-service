package com.the.good.club.dataU.sdk;

import com.the.good.club.dataU.sdk.protocol.DataRetrieveResponse;

public interface ProxyUClientCallbacks {

       /**
        * Callback used to save publicKey:correlationMessage mapping for later use on permission request message
        * generation and to link publicKey with a user by the correlationMessage received here and in
        * proxyUClient#createCorrelationMessage
        * @param publicKey of the data subject
        * @param correlationMessage used to link with the data subject
        */
       void onPublicKeyReceived(String publicKey, String correlationMessage);

       /**
        * Callback used to save granted:permissionMessage mapping to later link granted status with a user by the
        * permissionMessage received here and in proxyUClient#createPermissionRequestMessage
        * @param granted the status of the permission request
        * @param permissionMessage message used for requesting data permission
        */
       void onGrantedStatusReceived(boolean granted, String permissionMessage);

       /**
        * Callback used to receive data from another data processor on INDIVIDUAL flow
        * @param dataRetrieveResponse contains the response for the data requested from another data processor
        */
       void onDataRetrieveResponseReceived(DataRetrieveResponse dataRetrieveResponse);
}