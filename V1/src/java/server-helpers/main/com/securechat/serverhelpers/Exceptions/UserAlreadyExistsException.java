package com.securechat.serverhelpers;

import java.io.IOException;

public class UserAlreadyExistsException extends Exception{
    public UserAlreadyExistsException() {}

    public UserAlreadyExistsException(String usr)
    {
        super("User already exists: " + usr);
    }
}