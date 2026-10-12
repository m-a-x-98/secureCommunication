package com.securechat.serverhelpers.Exceptions;

public class UserAlreadyExistsException extends Exception{
    public UserAlreadyExistsException() {}

    public UserAlreadyExistsException(String usr)
    {
        super("User already exists: " + usr);
    }
}