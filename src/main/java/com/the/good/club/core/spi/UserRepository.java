package com.the.good.club.core.spi;

import com.the.good.club.core.data.User;

import java.util.Optional;

public interface UserRepository {
    void save(User user);

    Optional<User> getById(String id);

    Optional<User> getByCorrelationId(String correlationId);

    Optional<User> getByPermissionId(String permissionId);

    Optional<User> getByPublicKey(String publicKey);
}
