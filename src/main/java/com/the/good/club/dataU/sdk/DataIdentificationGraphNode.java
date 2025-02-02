package com.the.good.club.dataU.sdk;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class DataIdentificationGraphNode {
    @JsonProperty("key")
    private String key;

    @JsonProperty("mime")
    private String mimeType;

    @JsonProperty("description")
    private String description;

    @JsonProperty("children")
    private List<String> children = Collections.emptyList();

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }
}
