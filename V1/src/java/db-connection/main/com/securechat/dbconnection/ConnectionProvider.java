package com.securechat.dbconnection;

import java.sql.SQLException;
import java.sql.Connection;

public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
}