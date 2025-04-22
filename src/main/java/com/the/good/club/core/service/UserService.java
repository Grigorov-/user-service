package com.the.good.club.core.service;

import com.google.protobuf.ByteString;
import com.the.good.club.core.assembler.UserAssembler;
import com.the.good.club.core.connector.EmailConnector;
import com.the.good.club.core.data.User;
import com.the.good.club.core.spi.CorrelationRepository;
import com.the.good.club.core.spi.PermissionRepository;
import com.the.good.club.core.spi.UserRepository;
import com.the.good.club.dataU.sdk.DataIdentificationGraphHelper;
import com.the.good.club.dataU.sdk.ProxyUClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static com.google.protobuf.ByteString.copyFrom;
import static com.the.good.club.core.data.UserStatus.PENDING_CORRELATION;
import static com.the.good.club.core.data.UserStatus.PENDING_PERMISSION;
import static com.the.good.club.dataU.sdk.ClientUtils.UUIDStringToByteString;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final String SUBJECT = "The good club registration";
    public static final String DEFAULT_TERMS_AND_CONDITIONS_URL =
        "https://storage.googleapis.com/terms-and-conditions-the-good-club-bucket/TERMS%20OF%20USE%20AND%20PRIVACY%20POLICY.html";

    private final String DASHBOARDU_ENDPOINT = "https://dev.datau.eu/#/decode";

    private final EmailConnector emailConnector;
    private final ProxyUClient proxyUClient;
    private final UserRepository userRepository;
    private final UserAssembler userAssembler;
    private final CorrelationRepository correlationRepository;
    private final PermissionRepository permissionRepository;

    public UserService(EmailConnector emailConnector, UserRepository userRepository,
                       @Lazy ProxyUClient proxyUClient, UserAssembler userAssembler, CorrelationRepository correlationRepository, PermissionRepository permissionRepository) {
        this.emailConnector = emailConnector;
        this.proxyUClient = proxyUClient;
        this.userRepository = userRepository;
        this.userAssembler = userAssembler;
        this.correlationRepository = correlationRepository;
        this.permissionRepository = permissionRepository;
    }

    public User requestCorrelationWithUser(User user) {
        String correlationMessage = proxyUClient.createCorrelationMessage();
        String correlationLink = getCorrelationLink(correlationMessage);

        correlationRepository.save(correlationMessage, user.getId());
        userRepository.save(user);
        emailConnector.sendSimpleMessage(user.getEmail(), SUBJECT, correlationLink);
        return user;
    }

    public void requestPermissions(String publicKey, String correlationId) throws NoSuchAlgorithmException, IOException {
        Optional<User> userOptional = userRepository.getByCorrelationId(correlationId);
        if (userOptional.isEmpty()) {
            log.error("User for correlation id {} not found!", correlationId);
            return;
        }
        User user = userOptional.get();

        String permissionId = requestPermission(publicKey, user);
        User pendingPermissionUser = userAssembler.updateCorrelationData(user, publicKey, permissionId, correlationId, PENDING_PERMISSION);
        permissionRepository.save(permissionId, user.getId());
        userRepository.save(pendingPermissionUser);
    }

    public void updatePermissionStatus(String permissionRequest, boolean isGranted) {
        Optional<User> userOptional = userRepository.getByPermissionId(permissionRequest);
        if (userOptional.isEmpty()) {
            log.error("User for permissionRequest id {} not found!", permissionRequest);
            return;
        }

        User user = userAssembler.updateUserData(userOptional.get(), isGranted);
        userRepository.save(user);
    }

    private String requestPermission(String publicKey, User user) throws NoSuchAlgorithmException, IOException {
        String permissionMessage = null;
        try {
            permissionMessage = getPermissionMessage(publicKey, user);
            String permissionLink = getPermissionLink(permissionMessage);
            emailConnector.sendSimpleMessage(user.getEmail(), SUBJECT, permissionLink);
        } catch (Exception ex) {
            log.error("Unable to get permission request", ex);
        }
        return permissionMessage;
    }

    private String getPermissionLink(String message) {
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        return DASHBOARDU_ENDPOINT + "?message=" + encodedMessage;
    }

    private String getPermissionMessage(String publicKey, User user) throws NoSuchAlgorithmException, IOException {
        ByteString dataRightsSubject = getDataRightsSubject(publicKey);

        // use one of the fields names present in the data identification graph; see them in didgraph.yml from the SDK
        ByteString data = DataIdentificationGraphHelper.getNodeUUID("Personal Information");

        ByteString individualProcess = DataIdentificationGraphHelper.getProcessUUID(DataIdentificationGraphHelper.INDIVIDUAL_PROCESS);

        // optional, send some random value
        ByteString reason = UUIDStringToByteString(UUID.randomUUID().toString());

        ByteString policyHash = getTermsAndConditionsPolicyHash(user.getTermsAndConditionsIds());

        return proxyUClient.createPermissionRequestMessage(
                dataRightsSubject, data, individualProcess, reason, policyHash, 1605087413, 1893456000, 0, 1
        );
    }

    private ByteString getTermsAndConditionsPolicyHash(String termsAndConditionsUrl) throws NoSuchAlgorithmException, IOException {
        // hash for the terms and conditions document
        // -b Displayed when read more button is clicked before the user give consent.
        MessageDigest messageDigest = MessageDigest.getInstance("SHA3-256");

        byte[] fileHash;
        try (InputStream inputStream = new URL(termsAndConditionsUrl).openStream()) {
            fileHash = messageDigest.digest(inputStream.readAllBytes());
        }

        return copyFrom(fileHash);
    }


    private static ByteString getDataRightsSubject(String publicKey) {
        return copyFrom(Base64.getDecoder().decode(publicKey));
    }

    private String getCorrelationLink(String correlationMessage) {
        return DASHBOARDU_ENDPOINT + "?message=" + URLEncoder.encode(correlationMessage, StandardCharsets.UTF_8);
    }

    public Optional<User> getById(String id) {
        return userRepository.getById(id);
    }
}
