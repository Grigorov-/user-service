package com.the.good.club.core.assembler;

import com.the.good.club.core.data.User;
import com.the.good.club.core.data.UserStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.the.good.club.core.data.UserStatus.PENDING_CORRELATION;

@Component
public class UserAssembler {
    public User toUser(String email, UserStatus status) {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .status(status.name())
                .email(email)
                .build();
    }

    public User updateUser(User user, String publicKey, UserStatus userStatus) {
        return user.toBuilder().status(userStatus.name()).publicKey(publicKey).build();
    }
}
