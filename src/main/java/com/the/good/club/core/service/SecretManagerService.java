package com.the.good.club.core.service;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SecretManagerService {
  @Value("${gcp.projectId}")
  private String projectId;

  public String getSecret(String secretName) throws IOException {
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      SecretVersionName secretVersionName =
          SecretVersionName.of(projectId, secretName, "latest");

      AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
      ByteString secretData = response.getPayload().getData();
      return secretData.toStringUtf8();
    }
  }
}
