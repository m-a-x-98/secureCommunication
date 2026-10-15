package com.securechat.serverhelpers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class MessageCodec {
    private static final int MAX_PAYLOAD = 16 * 1024 * 1024;

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
        return str.getBytes(StandardCharsets.UTF_8);
    }

    public static long readLongAt(byte[] buf, int offset) {
        requireRange(buf, offset, 8);
        long value = 0L;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (buf[offset + i] & 0xff);
        }
        return value;
    }
    public static int readInt32At(byte[] buf, int offset) {
        requireRange(buf, offset, 4);
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value = (value << 8) | (buf[offset + i] & 0xff);
        }
        return value;
    }
    public static int readSizeAt(byte[] buf, int nBytes, int offset) {
        requireRange(buf, offset, nBytes);
        long value = 0;                      // long, so an 8-byte size can't overflow
        for (int i = 0; i < nBytes; i++) {
            value = (value << 8) | (buf[offset + i] & 0xff);
        }
        long remaining = (long) buf.length - offset - nBytes;
        if (value < 0 || value > remaining) {
            throw new IllegalArgumentException("Length field exceeds remaining payload");
        }
        return (int) value;
    }

    private static void requireRange(byte[] buf, int offset, int len) {
        if (offset < 0 || (long) offset + len > buf.length) {
            throw new IllegalArgumentException("Field runs past the end of the payload");
        }
    }

    public static byte[] listMessagesToPayload(List<UserMessage> messages) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(buffer);

        out.writeInt(messages.size());                          // [4 count]
        for (UserMessage msg : messages) {
            out.writeInt(msg.message.length);                   // [4 size]
            out.write(msg.message);                             // [ciphertext]
            out.writeLong(msg.padStartOffset);                  // [8 pad start]
            writeName(out, msg.user);                           // [2 size][sender]
            writeName(out, msg.recipient);                      // [2 size][recipient]
            out.writeLong(msg.time.getTime());                  // [8 epoch ms]

            if (buffer.size() > MAX_PAYLOAD) {                  // clamp the response size
                throw new IllegalArgumentException("Response too large");
            }
        }
        return buffer.toByteArray();
    }

    private static void writeName(DataOutputStream out, String name) throws IOException {
        byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 0xFFFF) {                            // clamp to the 2-byte field
            throw new IllegalArgumentException("Name too long");
        }
        out.writeShort(bytes.length);
        out.write(bytes);
    }

    public static LocalDateTime toUtc(Timestamp t) {
        return LocalDateTime.ofInstant(t.toInstant(), ZoneOffset.UTC);
    }
    public static Timestamp fromUtc(LocalDateTime utc) {
        return Timestamp.from(utc.toInstant(ZoneOffset.UTC));
    }
}