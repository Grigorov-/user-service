package com.the.good.club.core.spi;

import com.the.good.club.core.data.User;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    void save(User user);

    Optional<User> getById(String id);

    Optional<User> getByCorrelationId(String correlationId);

    Optional<User> getByPermissionId(String permissionId);

    Optional<User> getByPublicKey(String publicKey);

    List<User> getUserByFilters(String status, String company, Date start, Date end);
}
