package com.securechat.serverhelpers;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageCodec {
    public static MsgEntry readMessageOffset(byte[] payload, int offset){
        int idxCounter = offset;
        int entryLen = ((payload[idxCounter++] & 0xff) << 8) | (payload[idxCounter++] & 0xff); // 2 first bytes are username len
        byte[] entryBytes = Arrays.copyOfRange(payload, idxCounter, entryLen+idxCounter);
        String entry = new String(entryBytes, StandardCharsets.UTF_8);
        return new MsgEntry(entry, idxCounter + entryLen);
    }

    public static MsgBytes readMessageBytesOffset(byte[] payload, int offset){
        int idxCounter = offset;
        int entryLen = ((payload[idxCounter++] & 0xff) << 8) | (payload[idxCounter++] & 0xff);
        byte[] entryBytes = Arrays.copyOfRange(payload, idxCounter, entryLen + idxCounter);
        return new MsgBytes(entryBytes, idxCounter + entryLen);
    }

    public static byte[] createConfirmationPayload(){
        byte[] msg = {0};
        return msg;
    }

    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    public static byte[] longToByteArray(long value) {
        return new byte[] {
                (byte)(value >>> 56),
                (byte)(value >>> 48),
                (byte)(value >>> 40),
                (byte)(value >>> 32),
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    public static long readLongAt(byte[] buf, int offset) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (buf[offset + i] & 0xff);
        }

        return value;
    }

}