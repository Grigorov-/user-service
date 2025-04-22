package com.the.good.club.db.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.*;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UserEntity {

    @DocumentId
    private String id;

    @PropertyName("email")
    private String email;

    @PropertyName("status")
    private String status;

    @PropertyName("publicKey")
    private String publicKey;

    @PropertyName("correlationId")
    private String correlationId;

    @PropertyName("permissionIds")
    private String permissionIds;

    @PropertyName("termsAndConditionIds")
    private String termsAndConditionIds;

    @ServerTimestamp
    @PropertyName("createdAt")
    private Date createdAt;

}

