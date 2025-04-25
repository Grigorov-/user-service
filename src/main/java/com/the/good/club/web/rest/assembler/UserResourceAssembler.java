package com.the.good.club.web.rest.assembler;

import com.the.good.club.core.data.User;
import com.the.good.club.core.data.UserStatus;
import com.the.good.club.web.rest.resource.UserResource;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

import static com.the.good.club.core.service.UserService.DEFAULT_TERMS_AND_CONDITIONS_URL;

@Component
public class UserResourceAssembler {

    public UserResource toResource(User user) {
        return UserResource.builder()
                .id(user.getId())
                .email(user.getEmail())
                .status(user.getStatus())
                .company(user.getCompany())
                .createdAt(getCreatedAtFormatted(user.getCreatedAt()))
                .build();
    }

    public User toUser(UserResource userResource, UserStatus status) {
        return User.builder()
                .id(UUID.randomUUID().toString())
                .company(userResource.getCompany())
                .status(status.name())
                .createdAt(Date.from(Instant.now()))
                .email(userResource.getEmail())
                .termsAndConditionsIds(getTermsAndConditionsUrl(userResource))
                .build();
    }

    private String getTermsAndConditionsUrl(UserResource userResource) {
        return userResource.getTermsAndConditionsUrl() != null
                ? userResource.getTermsAndConditionsUrl()
                : DEFAULT_TERMS_AND_CONDITIONS_URL;
    }

    private String getCreatedAtFormatted(Date date) {
        return DateTimeFormatter.ISO_INSTANT.format(date.toInstant());
    }
}
