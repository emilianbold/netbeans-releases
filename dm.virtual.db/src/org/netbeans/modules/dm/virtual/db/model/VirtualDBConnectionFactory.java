/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dm.virtual.db.model;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Properties;
import org.openide.util.NbBundle;

/**
 * @author Ahimanikya Satapathy
 */
public class VirtualDBConnectionFactory {
    
    public static final String PROP_DBTYPE = "dbType";
    public static final String PROP_DRIVERCLASS = "DRIVER";
    public static final String PROP_PASSWORD = "password";
    public static final String PROP_URL = "url";
    public static final String PROP_USERNAME = "username";
    
    public static final String VIRTUAL_DB_URL_PREFIX = "jdbc:axiondb:";
    private static VirtualDBConnectionFactory instance;
    public static final String KEY_IGNORE_LOCK_FILE = "org.axiondb.engine.DiskDatabase.IGNORE_LOCK_FILE";
    
    public static VirtualDBConnectionFactory getInstance() {
        if (instance == null) {
            instance = new VirtualDBConnectionFactory();
        }
        return instance;
    }
    
    private VirtualDBConnectionFactory() {
    }
    
    public String getDriverClassName() {
        return VirtualDBConnectionDefinition.AXION_DRIVER;
    }
    
    public Connection getConnection(String jdbcUrl) throws VirtualDBException {
        return getConnection(jdbcUrl, null);
    }
        
    public Connection getConnection(String jdbcUrl, Properties props) throws VirtualDBException {
        if (jdbcUrl == null) {
            throw new VirtualDBException(NbBundle.getMessage(VirtualDBConnectionFactory.class, "MSG_JDBCUrl_NotSet"));
        }
        
        Connection conn = null;        
        try {
            registerDriver();
            
            if (props != null) {
                conn = DriverManager.getConnection(jdbcUrl, props);
            } else {
                conn = DriverManager.getConnection(jdbcUrl);
            }
        } catch (Exception ex) {
            throw new VirtualDBException(ex);
        }
        return conn;
    }
    
    public Connection getConnection(String jdbcUrl, String uid, String passwd) throws VirtualDBException {
        if (jdbcUrl == null) {
            throw new VirtualDBException(NbBundle.getMessage(VirtualDBConnectionFactory.class, "MSG_JDBCUrl_NotSet"));
        }
        
        Connection conn = null;
        try {
            Driver drv = registerDriver();
            Properties prop = new Properties();
            prop.setProperty("user", uid);
            prop.setProperty("password", passwd);
            conn = drv.connect(jdbcUrl, prop);
        } catch (Exception ex) {
            throw new VirtualDBException(ex);
        }
        
        return conn;
    }
    
    public Object getIgnoreLockProperty() {
        return System.getProperties().get(KEY_IGNORE_LOCK_FILE);
    }
    
    public void setIgnoreLockProperty(Object newValue) {
        if (null == newValue) {
            System.getProperties().remove(VirtualDBConnectionFactory.KEY_IGNORE_LOCK_FILE);
        } else {
            System.getProperties().put(VirtualDBConnectionFactory.KEY_IGNORE_LOCK_FILE, newValue);
        }
    }
    

    private Driver registerDriver() throws Exception {
        /* Using system class loader to load axion driver so that 
         the connection can be used by other modules also.*/
        
        Driver drv = (Driver) Thread.currentThread().getContextClassLoader().loadClass(
                getDriverClassName()).newInstance();
        return drv;
    }
}
