package com.the.good.club.db.assembler;

import com.the.good.club.core.data.User;
import com.the.good.club.db.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class UserEntityAssembler {
    public UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .company(user.getCompany())
                .email(user.getEmail())
                .status(user.getStatus())
                .publicKey(user.getPublicKey())
                .correlationId(user.getCorrelationId())
                .permissionIds(user.getPermissionIds())
                .termsAndConditionIds(user.getTermsAndConditionsIds())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public User toUser(UserEntity userEntity) {
        return User.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .status(userEntity.getStatus())
                .company(userEntity.getCompany())
                .publicKey(userEntity.getPublicKey())
                .correlationId(userEntity.getCorrelationId())
                .permissionIds(userEntity.getPermissionIds())
                .termsAndConditionsIds(userEntity.getTermsAndConditionIds())
                .createdAt(userEntity.getCreatedAt())
                .build();
    }
}
