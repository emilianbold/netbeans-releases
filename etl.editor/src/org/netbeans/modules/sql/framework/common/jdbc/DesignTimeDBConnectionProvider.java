/*
 * DesignTimeDBConnectionProvider.java
 *
 * Created on June 21, 2006, 1:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.sql.framework.common.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.DBConnectionFactory;
import com.sun.sql.framework.jdbc.DBConnectionParameters;
import com.sun.etl.engine.spi.DBConnectionProvider;

/**
 *
 * @author radval
 */
@org.openide.util.lookup.ServiceProvider(service=com.sun.etl.engine.spi.DBConnectionProvider.class)
public class DesignTimeDBConnectionProvider implements DBConnectionProvider {
    
    /** Creates a new instance of DesignTimeDBConnectionProvider */
    public DesignTimeDBConnectionProvider() {
    }

    public Connection getConnection(DBConnectionParameters conDef) throws BaseException {
        String driver = conDef.getDriverClass();
        String username = conDef.getUserName();
        String password = conDef.getPassword();
        String url = conDef.getConnectionURL();
        return DBExplorerUtil.createConnection(driver, url, username, password);
    }

    public Connection getConnection(Properties connProps) throws BaseException {
        String driver = connProps.getProperty(DBConnectionFactory.PROP_DRIVERCLASS);
        String username = connProps.getProperty(DBConnectionFactory.PROP_USERNAME);
        String password = connProps.getProperty(DBConnectionFactory.PROP_PASSWORD);
        String url = connProps.getProperty(DBConnectionFactory.PROP_URL);
        return DBExplorerUtil.createConnection(driver, url, username, password);
    }

    public void closeConnection(Connection con) {
        try {
            if(con != null) {
                con.close();
            }
        } catch (SQLException ex) {
            //ignore
        } finally {
            if(con != null) {
                try {
                    con.close();
                } catch(SQLException e) {
                    //ignore
                }
            }
        }
    }
}