package com.the.good.club.dataU.sdk;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;
import java.util.UUID;

public class ClientUtils {

    public static ByteString UUIDStringToByteString(String uuidString) {
        UUID uuid = UUID.fromString(uuidString);
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());

        return ByteString.copyFrom(byteBuffer.array());
    }

    public static UUID byteStringToUUID(ByteString uuidByteString) {
        if (uuidByteString == null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(uuidByteString.toByteArray());
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }

    public static String byteStringToUUIDString(ByteString uuidByteString) {
        return byteStringToUUID(uuidByteString).toString();
    }
}
