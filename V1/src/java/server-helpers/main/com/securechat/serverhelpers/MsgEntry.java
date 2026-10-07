package com.securechat.serverhelpers;

public class MsgEntry{
    private final String entry;
    private final int end_pos;
    public MsgEntry(String entry, int end_pos){
        this.entry = entry;
        this.end_pos = end_pos;
    }
    public String getEntry(){
        return entry;
    }
    public int getEndPos(){
        return end_pos;
    }
}