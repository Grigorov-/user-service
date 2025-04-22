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
public class DataUReferenceEntity {
    @DocumentId
    private String dataUId;
    private String userId;

    @ServerTimestamp
    @PropertyName("createdAt")
    private Date createdAt;
}
