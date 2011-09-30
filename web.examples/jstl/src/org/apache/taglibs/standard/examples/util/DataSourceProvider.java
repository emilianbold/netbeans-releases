package org.apache.taglibs.standard.examples.util;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 *
 * @author mjanicek
 */
public class DataSourceProvider implements DataSource {

    private String driverLocation;
    private String url;
    private Properties credentials;
    
    
    public DataSourceProvider(HttpSession session) {
        this(session.getAttribute("myDbDriver").toString(),
             session.getAttribute("myDbUrl").toString(),
             session.getAttribute("myDbUserName").toString(),
             session.getAttribute("myDbPassword").toString());
    }
    
    public DataSourceProvider(String driverLocation, String url, String username, String password) {
        this.driverLocation = driverLocation;
        this.url = url;
        
        credentials = new Properties();
        credentials.setProperty("user", username);
        credentials.setProperty("password", password);
    }
    
    public Connection getConnection() throws SQLException {
        Connection connection = null;
        try {
            Object driver = Class.forName(driverLocation);
            
            if (driver instanceof Driver) {
                connection = ((Driver) driver).connect(url, credentials);
            } else {
                throw new ClassNotFoundException("Driver not found!");
            }
            
        } catch (ClassNotFoundException ex) {
            connection = DriverManager.getConnection(url, credentials);
        }
        return connection;
    }

    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
