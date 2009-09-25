/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
            // associated with flatfile db module to avoid instantiating Axion classes
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
