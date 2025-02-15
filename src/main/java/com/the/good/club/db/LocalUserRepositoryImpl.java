package com.the.good.club.db;

import com.sun.mail.imap.protocol.UIDSet;
import com.the.good.club.core.spi.UserRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LocalUserRepositoryImpl implements UserRepository {
    private final Map<String, String> userMap = new HashMap<>();

    @Override
    public void save(String email, String correlationId) {
        userMap.put(correlationId, email);
    }

    @Override
    public String getUserEmailByCorrelationId(String correlationId) {
        return userMap.get(correlationId);
    }
}
