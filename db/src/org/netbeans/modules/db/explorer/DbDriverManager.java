/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import org.netbeans.api.db.explorer.JDBCDriver;

/**
 * Class to load drivers and create connections. It can find drivers and connections from
 * several sources: previously registered drivers, URLs from a JDBCDriver instance or
 * drivers registered to java.sql.DriverManager, exactly this order. DriverManager
 * has the lowest priority since we should always try to use the drivers defined by the
 * user in the DB Explorer, even if the same driver class is on the IDE's classpath.
 * (since the driver on the IDE's classpath could be a wrong/old version).
 *
 * <p>The advantage of this class over DriverManager is that it can work in a multi-class-loader
 * environment. That is, registered drivers can retrieved regardless of the class loader of the
 * caller of the getDriver() method.</p>
 *
 * <p>In the future this class could be used to cache and reuse the class loaders used
 * to load the drivers from URLs. A known disadvantage of not reusing the class loaders
 * is that each driver loaded by them registers itself to DriverManager, resulting in 
 * multiple registrations of the same class (but different instances). See also
 * issue 63957.</p>
 *
 * @author Andrei Badea
 */
public class DbDriverManager {
    
    private static final DbDriverManager DEFAULT = new DbDriverManager();
    
    private Set registeredDrivers;
    
    private DbDriverManager() {
    }
    
    /**
     * Returns the singleton instance.
     */
    public static DbDriverManager getDefault() {
        return DEFAULT;
    }
    
    /**
     * Gets a connection to databaseURL using jdbcDriver as a fallback.
     *
     * @param databaseURL
     * @param props
     * @param jdbcDriver the fallback JDBCDriver; can be null
     */
    public Connection getConnection(String databaseURL, Properties props, JDBCDriver jdbcDriver) throws SQLException {
        // try to find a registered driver or use the supplied jdbcDriver
        // we'll look ourselves in DriverManager, don't look there
        Driver driver = getDriverInternal(databaseURL, jdbcDriver, false);
        if (driver != null) {
            Connection conn = driver.connect(databaseURL, props);
            if (conn == null) {
                throw createDriverNotFoundException();
            }
            return conn;
        }
        
        // try to find a connection using DriverManager 
        try {
            return DriverManager.getConnection(databaseURL, props);
        } catch (SQLException e) {
            // ignore it, we throw our own exceptions
        }
        
        throw createDriverNotFoundException();
    }
    
    /**
     * Register a new driver.
     */
    public synchronized void registerDriver(Driver driver) {
        if (registeredDrivers == null) {
            registeredDrivers = new HashSet();
        }
        registeredDrivers.add(driver);
    }
    
    /**
     * Deregister a previously registered driver.
     */
    public synchronized void deregisterDriver(Driver driver) {
        if (registeredDrivers == null) {
            return;
        }
        registeredDrivers.remove(driver);
    }
    
    /**
     * Gets a driver which accepts databaseURL using jdbcDriver as a fallback.
     * 
     * <p>No checks are made as if the driver loaded from jdbcDriver accepts
     * databaseURL.</p>
     */
    public Driver getDriver(String databaseURL, JDBCDriver jdbcDriver) throws SQLException {
        Driver d = getDriverInternal(databaseURL, jdbcDriver, true);
        if (d == null) {
            throw createDriverNotFoundException();
        }
        return d;
    }
    
    /**
     * Gets a driver, but can skip DriverManager and doesn't throw SQLException if a driver can't be found.
     */
    private Driver getDriverInternal(String databaseURL, JDBCDriver jdbcDriver, boolean lookInDriverManager) throws SQLException {
        // try the registered drivers first
        synchronized (this) {
            if (registeredDrivers != null) {
                for (Iterator i = registeredDrivers.iterator(); i.hasNext();) {
                    Driver d = (Driver)i.next();
                    try {
                        if (d.acceptsURL(databaseURL)) {
                            return d;
                        }
                    } catch (SQLException e) {
                        // ignore it, we don't want to exit prematurely
                    }
                }
            }
        }
        
        // didn't find it, try to load it from jdbcDriver, if any
        if (jdbcDriver != null) {
            ClassLoader l = new DbURLClassLoader(jdbcDriver.getURLs());
            try {
                return (Driver)Class.forName(jdbcDriver.getClassName(), true, l).newInstance();
            } catch (Exception e) {
                SQLException sqlex = createDriverNotFoundException();
                sqlex.initCause(e);
                throw sqlex;
            }
        }
        
        // still nothing, try DriverManager 
        if (lookInDriverManager) {
            try {
                return DriverManager.getDriver(databaseURL);
            } catch (SQLException e) {
                // ignore it, we don't throw exceptions
            }
        }
        
        return null;
    }
    
    private SQLException createDriverNotFoundException() {
        return new SQLException("Unable to find a suitable driver", "08001"); // NOI18N
    }
}
