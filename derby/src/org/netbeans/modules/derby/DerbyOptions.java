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

package org.netbeans.modules.derby;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.options.SystemOption;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

/**
 *
 * @author Andrei Badea
 */
public class DerbyOptions extends SystemOption {
    
    private static final long serialVersionUID = 1101894610105398924L;
    
    static final String PROP_DERBY_LOCATION = "derbyLocation"; // NOI18N
    private static final String PROP_LAST_DATABASE_LOCATION = "defaultDbLocation"; // NOI18N
    
    private static final String INST_DIR = "db-derby-10.1.1.0"; // NOI18N
    
    private static final String DRIVER_CLASS_NET = "org.apache.derby.jdbc.ClientDriver"; // NOI18N
    private static final String DRIVER_CLASS_EMBEDDED = "org.apache.derby.jdbc.EmbeddedDriver"; // NOI18N
    
    private static final String DRIVER_PATH_NET = "lib/derbyclient.jar"; // NOI18N
    private static final String DRIVER_PATH_EMBEDDED = "lib/derby.jar"; // NOI18N
    
    // XXX these should actually be localized, but we'd have to localize 
    // DriverListUtil in the db module first
    private static final String DRIVER_NAME_NET = "Apache Derby (Net)"; // NOI18N
    private static final String DRIVER_NAME_EMBEDDED = "Apache Derby (Embedded)"; // NOI18N
    
    public static DerbyOptions getDefault() {
        return (DerbyOptions)SharedClassObject.findObject(DerbyOptions.class, true);
    }
    
    public String displayName() {
        return NbBundle.getMessage(DerbyOptions.class, "LBL_DerbyOptions");
    }
    
    /**
     * Returns the Derby location. Never returns null.
     */
    public String getDerbyLocation() {
        String derbyLocation = (String)getProperty(PROP_DERBY_LOCATION);
        if (derbyLocation == null) {
            derbyLocation = ""; // NOI18N
        }
//        if (derbyLocation == null || derbyLocation.length() <= 0) { // NOI18N
//            derbyLocation = getDefaultInstallLocation();
//            if (derbyLocation == null) {
//                derbyLocation = ""; // NOI18N
//            }
//        }
        return derbyLocation;
    }
    
    /**
     * Sets the Derby location.
     *
     * @param derbyLocation the Derby location. Pass null to set it to the
     *        default location.
     */
    public void setDerbyLocation(String derbyLocation) {
        synchronized (getLock()) {
            stopDerbyServer();
            registerDrivers(derbyLocation);
            putProperty(PROP_DERBY_LOCATION, derbyLocation, true);
        }
    }
    
    public String getLastDatabaseLocation() {
        String lastDatabaseLocation = (String)getProperty(PROP_LAST_DATABASE_LOCATION);
        if (lastDatabaseLocation == null) {
            // upon HIE & docs request the initial value for the 
            // default database directory is ${user.home}/derby
            // XXX maybe we should localize "derby"?
            // lastDatabaseLocation = new File(System.getProperty("user.home"), "derby").getAbsolutePath(); // NOI18N
            lastDatabaseLocation = System.getProperty("user.home"); // NOI18N
        }
        return lastDatabaseLocation;
    }
    
    public void setLastDatabaseLocation(String lastDatabaseLocation) {
        putProperty(PROP_LAST_DATABASE_LOCATION, lastDatabaseLocation, true);
    }
    
    private static String getDefaultInstallLocation() {
        File location = InstalledFileLocator.getDefault().locate(INST_DIR, null, false);
        return (location != null) ? location.getAbsolutePath() :  null; // NOI18N
    }
    
    private static void stopDerbyServer() {
        DatabaseConnection[] dbconn = ConnectionManager.getDefault().getConnections();
        for (int i = 0; i < dbconn.length; i++) {
            if (RegisterDerby.getDefault().acceptsDatabaseURL(dbconn[i].getDatabaseURL())) {
                ConnectionManager.getDefault().disconnect(dbconn[i]);
            }
        }
        RegisterDerby.getDefault().stop();        
    }
    
    private static void registerDrivers(String newLocation) {
        registerDriver(DRIVER_NAME_NET, DRIVER_CLASS_NET, DRIVER_PATH_NET, newLocation);
        registerDriver(DRIVER_NAME_EMBEDDED, DRIVER_CLASS_EMBEDDED, DRIVER_PATH_EMBEDDED, newLocation);
    }
    
    private static void registerDriver(String driverName, String driverClass, String driverRelativeFile, String newLocation) {
        // try to remove the driver first if it exists was registered from the current location
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(driverClass);
        for (int i = 0; i < drivers.length; i++) {
            JDBCDriver driver = drivers[i];
            URL[] urls = driver.getURLs();
            String currentLocation = DerbyOptions.getDefault().getDerbyLocation();
            boolean fromCurrentLocation = true;
            
            for (int j = 0; j < urls.length; j++) {
                FileObject fo = URLMapper.findFileObject(urls[j]);
                if (fo != null) {
                    File file = FileUtil.toFile(fo);
                    if (file != null) {
                        String driverFile = file.getAbsolutePath();
                        if (driverFile.startsWith(currentLocation)) {
                            continue;
                        }
                    }
                }
                fromCurrentLocation = false;
                break;
            }
            
            if (fromCurrentLocation) {
                try {
                    JDBCDriverManager.getDefault().removeDriver(driver);
                } catch (DatabaseException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        
        // register the new driver if it exists at the new location
        File newDriverFile = new File(newLocation, driverRelativeFile);
        if (newDriverFile.exists()) {
            try {
                JDBCDriver newDriver = JDBCDriver.create(driverName, driverClass, new URL[] { newDriverFile.toURL() });
                JDBCDriverManager.getDefault().addDriver(newDriver);
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (DatabaseException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
}
