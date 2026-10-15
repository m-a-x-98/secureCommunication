package com.securechat.dbconnection;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class SingleConnectionProvider implements ConnectionProvider {
    private final Connection connection;

    public SingleConnectionProvider(String url, String user, String password) throws SQLException {
        this.connection = DriverManager.getConnection(url, user, password);
    }
    
    public Connection getConnection() throws SQLException {
        return (Connection) Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class<?>[]{Connection.class},
            (proxy, method, args) -> {
                if (method.getName().equals("close")) {
                    return null; // no-op — don't close the shared connection
                }
                try {
                    return method.invoke(connection, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();   // the real SQLException, so catch (SQLException e) works
                }
            }
        );
    }
}