package com.securechat.serverhelpers;

import java.io.*;

public class MessageHelpers {
    private static final int MAX_PAYLOAD = 16 * 1024 * 1024;

    public static final void sendMsg(int status, byte[] payload, DataOutputStream out) throws IOException{
        out.writeByte(status);
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }

    public static final int writeError(DataOutputStream out){


        // exception can be passed as argument and be used to send specific error codes to the client 


        try {
            out.writeByte(0x01);
            out.writeInt(0);
            out.flush();
        } catch (Exception e){
            return 1;
        }
        return 0;
    }

    public static final int writeSuccess(DataOutputStream out){
        try {
            out.writeByte(0x00);
            out.writeInt(0);
            out.flush();
        } catch (Exception e){
            return 1;
        }
        return 0;
    }

    public static byte[] readPayload(DataInputStream in) throws IOException {
        int payloadLen = in.readInt();
        if (payloadLen < 0 || payloadLen > MAX_PAYLOAD) return null;
        byte[] payload = new byte[payloadLen];
        in.readFully(payload);
        return payload;
    }
}
