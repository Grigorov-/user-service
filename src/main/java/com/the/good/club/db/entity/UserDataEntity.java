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
public class UserDataEntity {
    @DocumentId
    private String dataId;
    private String process;
    private String content;
    private String mimeType;
    @ServerTimestamp
    @PropertyName("lastModifiedAt")
    private Date lastModifiedAt;
}
