package com.securechat.serverhelpers;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.sql.Timestamp;

public class MessageCodec {
    public static MsgEntry readMessageOffset(byte[] payload, int offset, int nBytes){
        int idxCounter = offset + nBytes;
        int entryLen = readSizeAt(payload, nBytes, offset);
        byte[] entryBytes = Arrays.copyOfRange(payload, idxCounter, entryLen+idxCounter);
        String entry = new String(entryBytes, StandardCharsets.UTF_8);
        return new MsgEntry(entry, idxCounter + entryLen);
    }

    public static MsgBytes readMessageBytesOffset(byte[] payload, int offset, int nBytes){
        int idxCounter = offset + nBytes;
        int entryLen = readSizeAt(payload, nBytes, offset);
        byte[] entryBytes = Arrays.copyOfRange(payload, idxCounter, entryLen + idxCounter);
        return new MsgBytes(entryBytes, idxCounter + entryLen);
    }

    public static byte[] createConfirmationPayload(){
        byte[] msg = {0};
        return msg;
    }

    public static byte[] shortToByteArray(int value){
        return new byte[] {
                (byte)(value >>> 8),
                (byte)value};
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

    public static Timestamp readTimestampAt(byte[] payload, int offset){
        return new Timestamp(MessageCodec.readLongAt(payload, offset));
    }

    public static byte[] stringToByteArray(String str){
        return str.getBytes();
    }

    public static long readLongAt(byte[] buf, int offset) {
        long value = 0L;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (buf[offset + i] & 0xff);
        }
        return value;
    }
    public static int readInt32At(byte[] buf, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value = (value << 8) | (buf[offset + i] & 0xff);
        }
        return value;
    }
    public static int readSizeAt(byte[] buf, int nBytes, int offset) {
        int value = 0;
        for (int i = 0; i < nBytes; i++) {
            value = (value << 8) | (buf[offset + i] & 0xff);
        }
        return value;
    }
}