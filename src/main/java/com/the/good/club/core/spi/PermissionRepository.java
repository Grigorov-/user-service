package com.the.good.club.core.spi;

public interface PermissionRepository {
    void save(String permissionId, String userId);

    String getUserIdByPermissionId(String permissionId);
}
