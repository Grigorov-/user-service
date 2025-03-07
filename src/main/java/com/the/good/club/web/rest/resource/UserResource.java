package com.the.good.club.web.rest.resource;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserResource {
    private String id;
    private String email;
    private String status;
}
