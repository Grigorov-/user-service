package com.the.good.club.db.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.the.good.club.core.data.User;
import com.the.good.club.core.spi.CorrelationRepository;
import com.the.good.club.core.spi.StoreException;
import com.the.good.club.db.entity.CorrelationEntity;
import com.the.good.club.db.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Component
public class CorrelationRepositoryImpl implements CorrelationRepository {
    public static final String CORRELATIONS = "correlations";

    private final Firestore firestore;

    public CorrelationRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    public void save(String correlationId, String userId) {
        try {
            String encodedCorrelationId = getEncodeToString(correlationId);
            CorrelationEntity correlation = CorrelationEntity.builder()
                    .correlationId(correlationId)
                    .userId(userId)
                    .createdAt(Date.from(Instant.now()))
                    .build();
            ApiFuture<WriteResult> future = firestore.collection(CORRELATIONS)
                    .document(encodedCorrelationId).set(correlation);
            future.get();
        } catch (Exception e) {
            throw new StoreException("Unable to store correlation", e);
        }
    }

    @Override
    public String getUserIdByCorrelationId(String correlationId) {
        try {
            String encodedCorrelationId = getEncodeToString(correlationId);
            DocumentReference docRef = firestore.collection(CORRELATIONS).document(encodedCorrelationId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                CorrelationEntity correlationEntity = document.toObject(CorrelationEntity.class);
                if (correlationEntity != null) {
                    return correlationEntity.getUserId();
                }
            }
            return null;

        } catch (Exception e) {
            throw new StoreException("Unable to retrieve user by correlation id" + correlationId, e);
        }
    }

    private String getEncodeToString(String correlationId) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(correlationId.getBytes(StandardCharsets.UTF_8));
    }
}
