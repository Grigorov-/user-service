package com.the.good.club.dataU.sdk;

import com.the.good.club.core.service.CertificatesService;
import io.grpc.*;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import io.netty.handler.ssl.SslContext;

import static java.security.spec.NamedParameterSpec.ED25519;

@Configuration
public class ProxyUClientConfiguration {

    @Value("${proxyU.host}")
    private String target;

    @Value("${proxyU.ip}")
    private String targetIp;

    private final CertificatesService certificatesService;

    public ProxyUClientConfiguration(CertificatesService certificatesService) {
        this.certificatesService = certificatesService;
    }

    @Bean
    ManagedChannel proxyUChannel() throws IOException, NoSuchAlgorithmException, KeyStoreException, InvalidKeySpecException, CertificateException, UnrecoverableKeyException {
        PrivateKey privateKey = certificatesService.loadPrivateKey();
        Certificate[] certificateChain = certificatesService.loadCertificateChain();

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setKeyEntry("key", privateKey, null, certificateChain);

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, null);

        SslContext sslContext = GrpcSslContexts.forClient()
                .trustManager(certificatesService.loadRootCA())
                .keyManager(keyManagerFactory.getKeyManagers()[0])
                .build();

        return NettyChannelBuilder.forTarget(targetIp)
                .overrideAuthority(target)
                .negotiationType(NegotiationType.TLS)
                .sslContext(sslContext)
                .build();
    }

    public static PrivateKey readPKCS8PrivateKey(File file) throws InvalidKeySpecException, IOException, NoSuchAlgorithmException {
        KeyFactory factory = KeyFactory.getInstance(ED25519.getName());

        try (FileReader keyReader = new FileReader(file);
             PemReader pemReader = new PemReader(keyReader)) {

            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(content);
            return factory.generatePrivate(pkcs8EncodedKeySpec);
        }
    }

    public static Certificate[] readCertificateChain(File pemFile) throws IOException {
        List<Certificate> certificateList = new ArrayList<>();

        try (Reader reader = new FileReader(pemFile)) {
            PEMParser pemParser = new PEMParser(reader);

            Object obj;
            JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
            while ((obj = pemParser.readObject()) != null) {
                if (obj instanceof X509CertificateHolder x509CertificateHolder) {
                    certificateList.add(converter.getCertificate(x509CertificateHolder));
                }
            }
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }

        Certificate[] certificateChain = new Certificate[certificateList.size()];
        certificateChain = certificateList.toArray(certificateChain);

        return certificateChain;
    }
}
