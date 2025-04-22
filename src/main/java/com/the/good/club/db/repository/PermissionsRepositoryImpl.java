package com.the.good.club.db.repository;

import com.google.cloud.firestore.Firestore;
import com.the.good.club.core.spi.CorrelationRepository;
import com.the.good.club.core.spi.PermissionRepository;
import org.springframework.stereotype.Component;

@Component
public class PermissionsRepositoryImpl extends DataUReferenceRepository implements PermissionRepository {
    private static final String PERMISSIONS_COLLECTION_NAME = "permissions";

    public PermissionsRepositoryImpl(Firestore firestore) {
        super(firestore, PERMISSIONS_COLLECTION_NAME);
    }

    @Override
    public String getUserIdByPermissionId(String permissionId) {
        return getUserIdByDataUId(permissionId);
    }
}
