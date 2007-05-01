/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mashup.db.common;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Properties;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Jonathan Giron
 * @version $Revision$
 */
public class FlatfileDBConnectionFactory {
    
    public static final String DRIVER_NAME = "org.axiondb.jdbc.AxionDriver";
    public static final String DEFAULT_FLATFILE_JDBC_URL_PREFIX = "jdbc:axiondb:";
    
    private static FlatfileDBConnectionFactory instance;
    
    public static final String KEY_IGNORE_LOCK_FILE = "org.axiondb.engine.DiskDatabase.IGNORE_LOCK_FILE";
    
    public static FlatfileDBConnectionFactory getInstance() {
        if (instance == null) {
            instance = new FlatfileDBConnectionFactory();
        }
        return instance;
    }
    
    private FlatfileDBConnectionFactory() {
    }
    
    public String getDriverClassName() {
        return DRIVER_NAME;
    }
    
    public Connection getConnection(String jdbcUrl) throws BaseException {
        return getConnection(jdbcUrl, null, null);
    }
    
    public Connection getConnection(String jdbcUrl, Properties props, ClassLoader cl) throws BaseException {
        if (jdbcUrl == null) {
            throw new BaseException("JDBC URL not set.");
        }
        
        Connection conn = null;
        ClassLoader oldContextLoader = Thread.currentThread().getContextClassLoader();
        
        try {
             /* Not doing as stated in the commented section below. Using system Class Loader.
             Facing problem when connection used by other modules like ETL Engine. */
            
            // WT #67399: if class loader is not specified, use the class loader
            // associated with flatfile db otd module to avoid instantiating Axion classes
            // which are associated with eBAM.
//            if (cl == null) {
//                cl = FlatfileDBConnectionFactory.class.getClassLoader();
//            }
//
//            Thread.currentThread().setContextClassLoader(cl);
            registerDriver();
            
            if (props != null) {
                conn = DriverManager.getConnection(jdbcUrl, props);
            } else {
                conn = DriverManager.getConnection(jdbcUrl);
            }
        } catch (Exception ex) {
            throw new BaseException(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextLoader);
        }
        return conn;
    }
    
    public Connection getConnection(String jdbcUrl, String uid, String passwd, ClassLoader cl) throws BaseException {
        if (jdbcUrl == null) {
            throw new BaseException("JDBC URL not set.");
        }
        
        Connection conn = null;
        ClassLoader oldContextLoader = Thread.currentThread().getContextClassLoader();
        
        try {
            /* Not doing as stated in the commented section below. Using system Class Loader.
             Facing problem when connection used by other modules like ETL Engine. */
            
            // WT #67399: if class loader is not specified, use the class loader
            // associated with flatfile db module to avoid instantiating Axion classes
            // which are associated with eBAM.
//            if (cl == null) {
//                cl = FlatfileDBConnectionFactory.class.getClassLoader();
//            }
            
            //Thread.currentThread().setContextClassLoader(cl);
            Driver drv = registerDriver();
            Properties prop = new Properties();
            prop.setProperty("user", uid);
            prop.setProperty("password", passwd);
            conn = drv.connect(jdbcUrl, prop);
        } catch (Exception ex) {
            throw new BaseException(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextLoader);
        }
        
        return conn;
    }
    
    public Object getIgnoreLockProperty() {
        return System.getProperties().get(KEY_IGNORE_LOCK_FILE);
    }
    
    public void setIgnoreLockProperty(Object newValue) {
        if (null == newValue) {
            System.getProperties().remove(FlatfileDBConnectionFactory.KEY_IGNORE_LOCK_FILE);
        } else {
            System.getProperties().put(FlatfileDBConnectionFactory.KEY_IGNORE_LOCK_FILE, newValue);
        }
    }
    
    /**
     * Registers the JDBC driver specific to this factory.
     *
     * @return Driver Class representing JDBC driver implementation class
     */
    private Driver registerDriver() throws Exception {
        /* Using system class loader to load axion driver so that 
         the connection can be used by other modules like ETL Engine also.*/
        
        Driver drv = (Driver) Thread.currentThread().getContextClassLoader().loadClass(
                getDriverClassName()).newInstance();
        return drv;
    }
}
