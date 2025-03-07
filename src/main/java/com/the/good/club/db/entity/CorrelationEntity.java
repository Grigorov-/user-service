package com.the.good.club.db.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.*;

import java.util.Date;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationEntity {
    @DocumentId
    private String correlationId;
    private String userId;

    @ServerTimestamp
    @PropertyName("createdAt")
    private Date createdAt;
}
