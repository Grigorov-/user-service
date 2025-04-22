package com.the.good.club.core.assembler;

import com.the.good.club.core.data.User;
import com.the.good.club.core.data.UserStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class UserAssembler {
    public User toUser(String email, UserStatus status) {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .status(status.name())
                .email(email)
                .createdAt(Date.from(Instant.now()))
                .build();
    }

    public User updateCorrelationData(User user, String publicKey, String permissionId, String correlationId,
                                      UserStatus userStatus) {
        return user.toBuilder()
                .status(userStatus.name())
                .publicKey(publicKey)
                .correlationId(correlationId)
                .permissionIds(permissionId)
                .build();
    }

    public User updateUserData(User user, boolean isGranted) {
        UserStatus status = isGranted ? UserStatus.APPROVED : UserStatus.ACCESS_REVOKED;
        return user.toBuilder().status(status.toString()).build();
    }
}
