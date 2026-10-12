package com.securechat.serverhelpers.Exceptions;

public class UserDoesntExistException extends Exception{
    public UserDoesntExistException() {}

    public UserDoesntExistException(String usr)
    {
        super("User does not exist: " + usr);
    }
}