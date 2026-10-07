package com.securechat.serverhelpers;

import java.io.IOException;

public class UserDoesntExistException extends Exception{
    public UserDoesntExistException() {}

    public UserDoesntExistException(String usr)
    {
        super("User does not exist: " + usr);
    }
}