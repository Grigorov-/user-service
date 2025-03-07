package com.the.good.club.web.rest.assembler;

import com.the.good.club.core.data.User;
import com.the.good.club.web.rest.resource.UserResource;
import org.springframework.stereotype.Component;

@Component
public class UserResourceAssembler {
    public UserResource toResource(User user) {
        return UserResource.builder()
                .id(user.getId())
                .email(user.getEmail())
                .status(user.getStatus())
                .build();
    }
}
