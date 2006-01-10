/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.derby;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
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
    
    static final String PROP_DERBY_LOCATION = "location"; // NOI18N
    static final String PROP_DERBY_SYSTEM_HOME = "systemHome"; // NOI18N
    
    static final String INST_DIR = "db-derby-10.1.1.0"; // NOI18N
    
    private static final String DRIVER_CLASS_NET = "org.apache.derby.jdbc.ClientDriver"; // NOI18N
    private static final String DRIVER_CLASS_EMBEDDED = "org.apache.derby.jdbc.EmbeddedDriver"; // NOI18N
    
    private static final String DRIVER_PATH_NET = "lib/derbyclient.jar"; // NOI18N
    private static final String DRIVER_PATH_EMBEDDED = "lib/derby.jar"; // NOI18N
    
    // XXX these should actually be localized, but we'd have to localize 
    // DriverListUtil in the db module first
    private static final String DRIVER_DISP_NAME_NET = "Apache Derby (Net)"; // NOI18N
    private static final String DRIVER_DISP_NAME_EMBEDDED = "Apache Derby (Embedded)"; // NOI18N
    
    private static final String DRIVER_NAME_NET = "apache_derby_net"; // NOI18N
    private static final String DRIVER_NAME_EMBEDDED = "apache_derby_embedded"; // NOI18N
    
    public static DerbyOptions getDefault() {
        return (DerbyOptions)SharedClassObject.findObject(DerbyOptions.class, true);
    }
    
    public String displayName() {
        return NbBundle.getMessage(DerbyOptions.class, "LBL_DerbyOptions");
    }
    
    /**
     * Returns the Derby location or an empty string if the Derby location
     * is not set. Never returns null.
     */
    public String getLocation() {
        String location = (String)getProperty(PROP_DERBY_LOCATION);
        if (location == null) {
            location = ""; // NOI18N
        }
        return location;
    }

    /**
     * Returns true if the Derby location is null. This method is needed
     * since getLocation() will never return a null value.
     */
    public boolean isLocationNull() {
        return getProperty(PROP_DERBY_LOCATION) == null;
    }
    
    /**
     * Sets the Derby location.
     * 
     * @param location the Derby location. A null value is valid and
     *        will be returned by getLocation() as an empty 
     *        string (meaning "not set"). An empty string is valid 
     *        and has the meaning "set to the default location".
     */
    public void setLocation(String location) {
        synchronized (getLock()) {
            if (!isReadExternal()) {
                stopDerbyServer();
            }
            if (location != null && location.length() <= 0) {
                location = getDefaultInstallLocation();
            }
            if (!isReadExternal()) {
                registerDrivers(location);
            }
            putProperty(PROP_DERBY_LOCATION, location, true);
        }
    }
    
    /**
     * Returns the Derby system home or an emtpy string if the system home
     * is not set. Never returns null.
     */
    public String getSystemHome() {
        String systemHome = (String)getProperty(PROP_DERBY_SYSTEM_HOME);
        if (systemHome == null) {
            systemHome = ""; // NOI18N
        }
        return systemHome;
    }
    
    public void setSystemHome(String derbySystemHome) {
        synchronized (getLock()) {
            if (!isReadExternal()) {
                stopDerbyServer();
            }
            putProperty(PROP_DERBY_SYSTEM_HOME, derbySystemHome, true);
        }
    }
    
    static String getDefaultInstallLocation() {
        File location = InstalledFileLocator.getDefault().locate(INST_DIR, null, false);
        if (location == null) {
            return null;
        }
        File libDir = new File(location, "lib"); // NOI18N
        if (!libDir.exists()) {
            return null;
        }
        File[] libs = libDir.listFiles();
        if (libs == null || libs.length <= 0) {
            return null;
        }
        return location.getAbsolutePath();
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
    
    private static void registerDrivers(final String newLocation) {
        try {
            // registering the drivers in an atomic action so the Drivers node 
            // is refreshed only once
            Repository.getDefault().getDefaultFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() {
                    registerDriver(DRIVER_NAME_NET, DRIVER_DISP_NAME_NET, DRIVER_CLASS_NET, DRIVER_PATH_NET, newLocation);
                    registerDriver(DRIVER_NAME_EMBEDDED, DRIVER_DISP_NAME_EMBEDDED, DRIVER_CLASS_EMBEDDED, DRIVER_PATH_EMBEDDED, newLocation);
                }
            });
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    private static void registerDriver(String driverName, String driverDisplayName, String driverClass, String driverRelativeFile, String newLocation) {
        // try to remove the driver first if it exists was registered from the current location
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(driverClass);
        for (int i = 0; i < drivers.length; i++) {
            JDBCDriver driver = drivers[i];
            URL[] urls = driver.getURLs();
            String currentLocation = DerbyOptions.getDefault().getLocation();
            if (currentLocation == null) {
                continue;
            }
            
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
        if (newLocation != null && newLocation.length() >= 0) {
            File newDriverFile = new File(newLocation, driverRelativeFile);
            if (newDriverFile.exists()) {
                try {
                    JDBCDriver newDriver = JDBCDriver.create(driverName, driverDisplayName, driverClass, new URL[] { newDriverFile.toURI().toURL() });
                    JDBCDriverManager.getDefault().addDriver(newDriver);
                } catch (MalformedURLException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                } catch (DatabaseException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
    }
}
