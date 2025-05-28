package com.the.good.club.core.service;

import com.the.good.club.dataU.sdk.LegalPolicyDocument;
import com.the.good.club.dataU.sdk.ProxyUClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Scanner;

@Service
public class TermsAndConditionsService {
    private final ProxyUClient proxyUClient;

    public TermsAndConditionsService(ProxyUClient proxyUClient) {
        this.proxyUClient = proxyUClient;
    }

    public String registerTermsAndConditions(String url) throws Exception {
        LegalPolicyDocument legalPolicyDocument = new LegalPolicyDocument();
        legalPolicyDocument.setUrl(url);
        legalPolicyDocument.setHash(getSHA3_256HashFromUrl(url));
        return proxyUClient.submitDocument(legalPolicyDocument);
    }

    public static String getSHA3_256HashFromUrl(String urlStr) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA3-256");

        try (InputStream inputStream = new URL(urlStr).openStream()) {
            byte[] contentBytes = inputStream.readAllBytes();
            byte[] hashBytes = messageDigest.digest(contentBytes);

            return HexFormat.of().formatHex(hashBytes);
        }
    }

    private String getContent(String urlString) throws IOException {
        URL url = new URL(urlString);
        String content;
        try (InputStream inputStream = url.openStream()) {
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A");
            content = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
        }
        return content;
    }
}
