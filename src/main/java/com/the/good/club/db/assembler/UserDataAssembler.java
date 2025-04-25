package com.the.good.club.db.assembler;

import com.the.good.club.db.entity.UserDataEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class UserDataAssembler {
    public UserDataEntity toEntity(String id, String field, String process, String content, String mimeType) {
        return UserDataEntity.builder()
                .dataId(id)
                .field(field)
                .process(process)
                .content(content)
                .mimeType(mimeType)
                .lastModifiedAt(Date.from(Instant.now()))
                .build();
    }
}
