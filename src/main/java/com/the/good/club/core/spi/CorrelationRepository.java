package com.the.good.club.core.spi;

import org.springframework.stereotype.Repository;

public interface CorrelationRepository {
    void save(String correlationId, String userId);

    String getUserIdByCorrelationId(String correlationId);
}
