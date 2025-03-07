package com.the.good.club.core.data;

import lombok.*;

import java.util.Date;

@Getter
@Builder(toBuilder = true)
public class User {
    private String id;
    private String email;
    private String publicKey;
    private String status;
    private Date createdAt;
}
