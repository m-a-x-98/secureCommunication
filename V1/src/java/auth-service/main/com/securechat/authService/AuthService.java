package com.securechat.authService;

import com.securechat.serverhelpers.Exceptions.*;

// Maybe add such that non of the methods work unless verifyLogin has been called

public interface AuthService {
    public boolean verifyLogin(String username, String password);
}
