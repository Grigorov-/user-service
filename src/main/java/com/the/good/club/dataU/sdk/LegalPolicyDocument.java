package com.the.good.club.dataU.sdk;

public class LegalPolicyDocument {
    private String url;
    private String hash;

    public LegalPolicyDocument() {
    }

    public LegalPolicyDocument(String url, String hash) {
        this.url = url;
        this.hash = hash;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
