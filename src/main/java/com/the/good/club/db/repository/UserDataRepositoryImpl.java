package com.the.good.club.db.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.the.good.club.core.data.User;
import com.the.good.club.core.spi.UserDataRepository;
import com.the.good.club.core.spi.UserRepository;
import com.the.good.club.dataU.sdk.UserData;
import com.the.good.club.db.assembler.UserDataAssembler;
import com.the.good.club.db.entity.UserDataEntity;
import com.the.good.club.db.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.the.good.club.db.repository.UserRepositoryImpl.USERS;

@Repository
public class UserDataRepositoryImpl implements UserDataRepository {
    private final Logger logger = LoggerFactory.getLogger(UserDataRepositoryImpl.class);
    public static final String USER_DATA = "userData";

    private final UserRepository userRepository;
    private final Firestore firestore;
    private final UserDataAssembler userDataAssembler;

    public UserDataRepositoryImpl(UserRepository userRepository, Firestore firestore, UserDataAssembler userDataAssembler) {
        this.userRepository = userRepository;
        this.firestore = firestore;
        this.userDataAssembler = userDataAssembler;
    }


    @Override
    public void save(String userPublicKey, String dataId, String process, String mimeType, String content) {
        String userId = getUserIdByPublicKey(userPublicKey);
        if (userId == null) {
            return;
        }

        try {
            UserDataEntity userDataEntity = userDataAssembler.toEntity(dataId, process, content, mimeType);
            getUserDataDocumentReference(dataId, userId)
                    .set(userDataEntity)
                    .get();
        } catch (Exception e) {
            logger.error("Unable to store user data for user {} data {} type {}", userPublicKey, dataId, mimeType, e);
        }
    }

    @Override
    public boolean delete(String userPublicKey, String dataId) {
        String userId = getUserIdByPublicKey(userPublicKey);
        if (userId == null) {
            return false;
        }

        try {
            DocumentSnapshot doc = getUserDataDocumentSnapshot(dataId, userId);

            if (!doc.exists()) {
                logger.error("Data not found for user: {} and dataId {}", userId, dataId);
                return false;
            }

            getUserDataDocumentReference(dataId, userId)
                    .delete();

        } catch (Exception e) {
            logger.error("Unable to delete user data for user {} data id {}", userPublicKey, dataId, e);
            return false;
        }
        return true;
    }
    

    public UserData getById(String userPublicKey, String dataId) {
        String userId = getUserIdByPublicKey(userPublicKey);
        if (userId == null) {
            return null;
        }

        return getUserData(userPublicKey, dataId, userId);

    }

    private UserData getUserData(String userPublicKey, String dataId, String userId) {
        try {
            DocumentSnapshot document = getUserDataDocumentSnapshot(dataId, userId);
            if (document == null || !document.exists()) {
                return null;
            }
            UserDataEntity userDataEntity = document.toObject(UserDataEntity.class);
            return new UserData(userDataEntity.getMimeType(), userDataEntity.getContent());
        } catch (Exception e) {
            logger.error("Unable to delete user data for user {} data id {}", userPublicKey, dataId, e);
            return null;
        }
    }

    private DocumentSnapshot getUserDataDocumentSnapshot(String dataId, String userId) throws InterruptedException, ExecutionException {
        ApiFuture<DocumentSnapshot> documentFuture = getUserDataDocumentReference(dataId, userId)
                .get();
        return documentFuture.get();
    }

    private DocumentReference getUserDataDocumentReference(String dataId, String userId) {
        return firestore.collection(USERS).document(userId)
                .collection(USER_DATA)
                .document(dataId);
    }

    private String getUserIdByPublicKey(String userPublicKey) {
        Optional<User> optionalUser = userRepository.getByPublicKey(userPublicKey);
        if (optionalUser.isEmpty()) {
            logger.error("User not found with public key {}", userPublicKey);
            return null;
        }
        return optionalUser.get().getId();
    }
}
