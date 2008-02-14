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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.db.mysql;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Storage and retrieval of options for MySQL support.  These options are
 * stored persistently using NbPreferences API.
 * 
 * @author David Van Couvering
 */
public class MySQLOptions {

    private static final Logger LOGGER = Logger.getLogger(MySQLOptions.class.getName());

    private static final MySQLOptions DEFAULT = new MySQLOptions();

    private static final String PROP_MYSQL_LOCATION = "location"; // NOI18N
    private static final String PROP_HOST = "host"; // NO18N
    private static final String PROP_PORT = "port"; // NO18N
    private static final String PROP_ADMINUSER = "adminuser"; // NO18N
    private static final String PROP_ADMINPWD = "adminpwd"; // NO18N
    private static final String PROP_SAVEPWD = "savepwd"; // NO18N
    private static final String PROP_DBDIR = "dbdir"; // NO18N
    private static final String PROP_CONN_REGISTERED = "conn-registered"; // NOI18N
    
    // Currently not modifiable...
    private static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_ADMIN_USER = "root";
    private static final String DEFAULT_ADMIN_PASSWORD = "";
    
    public static MySQLOptions getDefault() {
        return DEFAULT;
    }
    
    private MySQLOptions() {
        setSavePassword(false);
    }

    protected final void putProperty(String key, String value) {
        if (value != null) {
            NbPreferences.forModule(MySQLOptions.class).put(key, value);
        } else {
            NbPreferences.forModule(MySQLOptions.class).remove(key);
        }
    }

    protected final void putProperty(String key, boolean value) {
        NbPreferences.forModule(MySQLOptions.class).putBoolean(key, value);
    }
    
    protected final void clearProperty(String key) {
        NbPreferences.forModule(MySQLOptions.class).remove(key);
    }
    
    protected final String getProperty(String key) {
        return NbPreferences.forModule(MySQLOptions.class).get(key, "");
    }
    
    protected final boolean getBooleanProperty(String key) {
        return NbPreferences.forModule(MySQLOptions.class).getBoolean(key, false);        
    }

    /**
     * Returns the MySQL location or an empty string if the MySQL location
     * is not set. Never returns null.
     */
    /** TODO - implement location semantics 
    public String getLocation() {
        String location = getProperty(PROP_MYSQL_LOCATION);
        if (location == null) {
            location = ""; // NOI18N
        }
        
     */

    /**
     * Returns true if the MySQL location is null. This method is needed
     * since getLocation() will never return a null value.
     */
    /* TODO - implement location semantics   
     public boolean isLocationNull() {
    return getProperty(PROP_MYSQL_LOCATION) == null;
    }
     */
    /**
     * Sets the MySQL location.
     *
     * @param location the MySQL location. A null value is valid and
     *        will be returned by getLocation() as an empty
     *        string (meaning "not set"). An empty string is valid
     *        and has the meaning "set to the default location".
     */
    /* TODO - Implement location semantics
    public void setLocation(String location) {
        if (location !=  null && location.length() > 0) {
            File locationFile = new File(location).getAbsoluteFile();
            if (!locationFile.exists()) {
                String message = NbBundle.getMessage(MySQLOptions.class, 
                        "ERR_DirectoryDoesNotExist", locationFile);
                IllegalArgumentException e = new IllegalArgumentException(message);
                Exceptions.attachLocalizedMessage(e, message);
                throw e;
            }
            if (!Util.isMySQLInstallLocation(locationFile)) {
                String message = NbBundle.getMessage(MySQLOptions.class, 
                        "ERR_InvalidMySQLLocation", locationFile);
                IllegalArgumentException e = new IllegalArgumentException(message);
                Exceptions.attachLocalizedMessage(e, message);
                throw e;
            }
        }

        synchronized (this) {
            stopMySQLServer();
            LOGGER.log(Level.FINE, "Setting location to {0}", location); // NOI18N
            putProperty(PROP_MYSQL_LOCATION, location);
        }
    }

    public synchronized boolean trySetLocation(String location) {
        LOGGER.log(Level.FINE, "trySetLocation: Trying to set location to {0}", location); // NOI18N
        String current = getLocation();
        if (current.length() == 0) {
            setLocation(location);
            LOGGER.fine("trysetLocation: Succeeded"); // NOI18N
            return true;
        }
        File currentFile = new File(current);
        if (!currentFile.exists() || currentFile.isFile()) {
             setLocation(location);
             LOGGER.fine("trysetLocation: correcting"); // NOI18N
             return true;                
        }
        LOGGER.fine("trySetLocation: Another location already set"); // NOI18N
        return false;
    }
     */

    public String getHost() {
        return getProperty(PROP_HOST);
    }

    public void setHost(String host) {
        putProperty(PROP_HOST, host);
    }

    public String getPort() {
        return getProperty(PROP_PORT);
    }

    public void setPort(String port) {
        putProperty(PROP_PORT, port);
    }

    public String getAdminUser() {
        return getProperty(PROP_ADMINUSER);
    }

    public void setAdminUser(String adminUser) {
        putProperty(PROP_ADMINUSER, adminUser);
    }

    public String getAdminPassword() {
        return getProperty(PROP_ADMINPWD);
    }

    public void setAdminPassword(String adminPassword) {
        if ( isSavePassword() ) {
            // 'null' is a valid password, but if we save as null
            // it will actually clear the property.  So convert it to
            // an empty string.
            if ( adminPassword == null ) {
                adminPassword = "";
            }
            putProperty(PROP_ADMINPWD, adminPassword);
        } else {
            LOGGER.log(Level.INFO, "Received request to save MySQL admin " +
                    "password but the savePassword option is not set; ignoring");
        }
    }
    
    public void clearAdminPassword() {
        clearProperty(PROP_ADMINPWD);
    }

    public boolean isSavePassword() {
        return getBooleanProperty(PROP_SAVEPWD);
    }

    public void setSavePassword(boolean savePassword) {
        putProperty(PROP_SAVEPWD, savePassword);
        if ( ! savePassword ) {
            clearAdminPassword();
        }
    }
    
    public void setConnectionRegistered(boolean registered) {
        putProperty(PROP_CONN_REGISTERED, registered);
    }

    public boolean getConnectionRegistered() {
        return getBooleanProperty(PROP_CONN_REGISTERED);
    }


    /* TODO - Implement support for database directory 
    public String getDatabaseDirectory() {
        return getProperty(PROP_PORT);
    }

    public void setDatabaseDirectory(String dbdir) {
        putProperty(PROP_DBDIR, dbdir);
    }
    */
    

    /**
     * Return the JDBC URL based on the information we have
     * 
     * @return a JDBC URL, or null if we don't have enough information
     */
    public static String getURL(String host, String port) {
        // Format is jdbc:mysql://<HOST>:<PORT>
        // No database is specified for an admin connection.
        StringBuffer url = new StringBuffer("jdbc:mysql://"); // NOI18N
        url.append( host == null || host.equals("") ? "localhost" : host); // NO18N
        if ( port != null && (! port.equals("")) ) {
            url.append(":" + port);
        }
        
        return url.toString();
    }
    
    public String getURL() {
        return getURL(getHost(), getPort());
    }

    public static String getDriverClass() {
        return DRIVER_CLASS;
    }
    public static String getDefaultPort() {
        return DEFAULT_PORT;
    }

    public static String getDefaultAdminPassword() {
        return DEFAULT_ADMIN_PASSWORD;
    }

    public static String getDefaultAdminUser() {
        return DEFAULT_ADMIN_USER;
    }

    public static String getDefaultHost() {
        return DEFAULT_HOST;
    }

}
