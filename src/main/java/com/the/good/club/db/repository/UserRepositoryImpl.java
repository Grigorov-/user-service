package com.the.good.club.db.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.the.good.club.core.data.User;
import com.the.good.club.core.spi.CorrelationRepository;
import com.the.good.club.core.spi.StoreException;
import com.the.good.club.core.spi.UserRepository;
import com.the.good.club.db.assembler.UserEntityAssembler;
import com.the.good.club.db.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    public static final String USERS = "users";
    public static final String PUBLIC_KEY = "publicKey";

    private final Firestore firestore;
    private final UserEntityAssembler userEntityAssembler;
    private final CorrelationRepository correlationRepository;
    private final PermissionsRepositoryImpl permissionsRepository;

    public UserRepositoryImpl(Firestore firestore, UserEntityAssembler userEntityAssembler, CorrelationRepository correlationRepository, PermissionsRepositoryImpl permissionsRepository) {
        this.firestore = firestore;
        this.userEntityAssembler = userEntityAssembler;
        this.correlationRepository = correlationRepository;
        this.permissionsRepository = permissionsRepository;
    }

    public void save(User user) {
        try {
            UserEntity userEntity = userEntityAssembler.toEntity(user);
            ApiFuture<WriteResult> future = firestore.collection(USERS).document(user.getId()).set(userEntity);
            future.get();
        } catch (Exception e) {
            throw new StoreException("Unable to store user", e);
        }
    }

    @Override
    public Optional<User> getByCorrelationId(String correlationId) {
        try {
            String userId = correlationRepository.getUserIdByCorrelationId(correlationId);
            return getById(userId);
        } catch (Exception e) {
            throw new StoreException("Unable to retrieve user by correlation id", e);
        }
    }

    @Override
    public Optional<User> getByPermissionId(String permissionId) {
        try {
            String userId = permissionsRepository.getUserIdByPermissionId(permissionId);
            return getById(userId);
        } catch (Exception e) {
            throw new StoreException("Unable to retrieve user by permission id", e);
        }
    }

    @Override
    public Optional<User> getById(String userId) {
        try {
            DocumentReference docRef = firestore.collection("users").document(userId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                UserEntity userEntity = document.toObject(UserEntity.class);
                if (userEntity != null) {
                    return Optional.of(userEntityAssembler.toUser(userEntity));
                }
            }
            return Optional.empty();

        } catch (Exception e) {
            throw new StoreException("Unable to retrieve user", e);
        }
    }

    @Override
    public Optional<User> getByPublicKey(String publicKey) {
        QuerySnapshot snapshot;
        try {
            snapshot = firestore.collection(USERS).whereEqualTo(PUBLIC_KEY, publicKey).get().get();
        } catch (Exception ex) {
            logger.warn("Unable to retrieve user for public key" + publicKey, ex);
            return Optional.empty();
        }

        if (snapshot.isEmpty()) {
            return Optional.empty();
        }

        UserEntity userEntity = snapshot.getDocuments().get(0).toObject(UserEntity.class);
        return Optional.of(userEntityAssembler.toUser(userEntity));
    }

    @Override
    public List<User> getUserByFilters(String status, String company, Date start, Date end) {
        Query query = firestore.collection(USERS);

        if (status != null && !status.isBlank()) {
            query = query.whereEqualTo("status", status);
        }
        if (company != null && !company.isBlank()) {
            query = query.whereEqualTo("company", company);
        }
        query = query
                .whereGreaterThanOrEqualTo("createdAt", start)
                .whereLessThanOrEqualTo("createdAt", end);
        ApiFuture<QuerySnapshot> snapshot = query.get();
        List<User> result = new ArrayList<>();

        try {
            for (DocumentSnapshot doc : snapshot.get().getDocuments()) {
                UserEntity entity = doc.toObject(UserEntity.class);
                result.add(userEntityAssembler.toUser(entity));
            }
        } catch (Exception ex) {
            logger.warn("Unable to retrieve users by filters company:{} status:{} startDate{} endDate{}", company, start, start, end);
            throw new StoreException("Unable to retrieve users by filters", ex);
        }

        return result;
    }
}
