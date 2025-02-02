package com.the.good.club.core.spi;

public interface UserRepository {
    void save(String email, String correlationId);

    String getUserEmailByCorrelationId(String correlationId);
}
