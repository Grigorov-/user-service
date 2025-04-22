package com.the.good.club.db.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.the.good.club.core.spi.StoreException;
import com.the.good.club.db.entity.DataUReferenceEntity;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

public class DataUReferenceRepository {
    private final Firestore firestore;
    private final String collection;

    public DataUReferenceRepository(Firestore firestore, String collection) {
        this.firestore = firestore;
        this.collection = collection;
    }

    public void save(String dataUId, String userId) {
        try {
            String encodedCorrelationId = getEncodeToString(dataUId);
            DataUReferenceEntity correlation = DataUReferenceEntity.builder()
                    .dataUId(dataUId)
                    .userId(userId)
                    .createdAt(Date.from(Instant.now()))
                    .build();
            ApiFuture<WriteResult> future = firestore.collection(collection)
                    .document(encodedCorrelationId).set(correlation);
            future.get();
        } catch (Exception e) {
            throw new StoreException("Unable to store in " + collection, e);
        }
    }

    public <T> String getUserIdByDataUId(String dataUId) {
        try {
            String dataUIdEncoded = getEncodeToString(dataUId);
            DocumentReference docRef = firestore.collection(collection).document(dataUIdEncoded);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                DataUReferenceEntity dataUReferenceEntity = document.toObject(DataUReferenceEntity.class);
                if (dataUReferenceEntity != null) {
                    return dataUReferenceEntity.getUserId();
                }
            }
            return null;

        } catch (Exception e) {
            throw new StoreException(String.format("Unable to retrieve user by id %s from collection %s",
                    dataUId, collection), e);
        }
    }

    private String getEncodeToString(String correlationId) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(correlationId.getBytes(StandardCharsets.UTF_8));
    }
}
