package com.securechat.serverhelpers;

import java.io.*;
import java.net.*;

public class MessageHelpers {
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
}
