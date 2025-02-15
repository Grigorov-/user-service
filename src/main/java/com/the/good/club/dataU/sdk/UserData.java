package com.the.good.club.dataU.sdk;

import java.io.Serializable;

public class UserData implements Serializable {
    private String mimeType;
    private String value;

    public UserData() {}

    public UserData(String mimeType, String value) {
        this.mimeType = mimeType;
        this.value = value;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
