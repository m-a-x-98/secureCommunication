package com.securechat.serverhelpers;

public class MsgBytes{
    private final byte[] entry;
    private final int end_pos;
    public MsgBytes(byte[] entry, int end_pos){
        this.entry = entry;
        this.end_pos = end_pos;
    }
    public byte[] getEntry(){
        return entry;
    }
    public int getEndPos(){
        return end_pos;
    }
}

