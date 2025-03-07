package com.the.good.club.core.spi;

import com.the.good.club.dataU.sdk.UserData;
import org.springframework.stereotype.Repository;

public interface UserDataRepository {
    void save(String userPublicKey, String dataId, String process, String mimeType, String content);

    boolean delete(String userPublicKey, String dataId);

    UserData getById(String userPublicKey, String dataId);
}
