package com.the.good.club.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;

import static com.the.good.club.dataU.sdk.ProxyUClientConfiguration.readCertificateChain;
import static com.the.good.club.dataU.sdk.ProxyUClientConfiguration.readPKCS8PrivateKey;

@Service
public class CertificatesService {
  @Value("${proxyU.privateKey}")
  private String privateKeySecretName;

  @Value("${proxyU.certificate}")
  private String certificateSecretName;

  @Value("${proxyU.rootCertificate}")
  private String rootCA;

  private final SecretManagerService secretManagerService;

  public CertificatesService(SecretManagerService secretManagerService) {
    this.secretManagerService = secretManagerService;
  }

  public PrivateKey loadPrivateKey() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
    File privateKeyFile = getCertificateFile(privateKeySecretName, "proxyu", ".key");

    return readPKCS8PrivateKey(privateKeyFile);
  }

  public Certificate[] loadCertificateChain() throws IOException {
    File certFile = getCertificateFile(certificateSecretName, "proxyu", ".pem");

    return readCertificateChain(certFile);
  }

  public File loadRootCA() throws IOException {
    return getCertificateFile(rootCA, "root", ".crt");
  }

  private File getCertificateFile(String fileKey, String tempFileName, String tempFileExtension) throws IOException {
    String rootCAContent = secretManagerService.getSecret(fileKey);

    File rootCAFile = File.createTempFile(tempFileName, tempFileExtension);
    try (FileWriter writer = new FileWriter(rootCAFile)) {
      writer.write(rootCAContent);
    }
    return rootCAFile;
  }
}
