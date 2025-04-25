package com.the.good.club.core.data;

import lombok.*;

import java.util.Date;

@Getter
@Builder(toBuilder = true)
public class User {
    private String id;
    private String email;
    private String company;
    private String publicKey;
    private String correlationId;
    private String permissionIds;
    private String termsAndConditionsIds;
    private String status;
    private Date createdAt;
}
