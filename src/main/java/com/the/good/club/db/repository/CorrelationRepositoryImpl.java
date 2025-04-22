package com.the.good.club.db.repository;

import com.google.cloud.firestore.Firestore;
import com.the.good.club.core.spi.CorrelationRepository;
import org.springframework.stereotype.Component;

@Component
public class CorrelationRepositoryImpl extends DataUReferenceRepository implements CorrelationRepository {
    private static final String CORRELATIONS_COLLECTION_NAME = "correlations";

    public CorrelationRepositoryImpl(Firestore firestore) {
        super(firestore, CORRELATIONS_COLLECTION_NAME);
    }

    @Override
    public String getUserIdByCorrelationId(String correlationId) {
        return getUserIdByDataUId(correlationId);
    }
}
