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
                .email(user.getEmail())
                .status(user.getStatus())
                .publicKey(user.getPublicKey())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt() : Date.from(Instant.now()))
                .build();
    }

    public User toUser(UserEntity userEntity) {
        return User.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .status(userEntity.getStatus())
                .publicKey(userEntity.getPublicKey())
                .createdAt(userEntity.getCreatedAt())
                .build();
    }
}
