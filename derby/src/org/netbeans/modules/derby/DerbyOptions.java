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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.derby;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.nodes.BeanNode;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Andrei Badea
 */
public class DerbyOptions {

    private static final Logger LOGGER = Logger.getLogger(DerbyOptions.class.getName());

    private static final DerbyOptions INSTANCE = new DerbyOptions();

    /**
     * This system property allows setting a default value for the Derby system home directory.
     * Its value will be returned by the {@link getSystemHome} method if the
     * systemHome property is null. See issue 76908.
     */
    public static final String NETBEANS_DERBY_SYSTEM_HOME = "netbeans.derby.system.home"; // NOI18N

    static final String PROP_DERBY_LOCATION = "location"; // NOI18N
    static final String PROP_DERBY_SYSTEM_HOME = "systemHome"; // NOI18N

    static final String INST_DIR = "db-derby-10.1.1.0"; // NOI18N

    public static final String DRIVER_CLASS_NET = "org.apache.derby.jdbc.ClientDriver"; // NOI18N
    public static final String DRIVER_CLASS_EMBEDDED = "org.apache.derby.jdbc.EmbeddedDriver"; // NOI18N

    private static final String DRIVER_PATH_NET = "lib/derbyclient.jar"; // NOI18N
    private static final String DRIVER_PATH_EMBEDDED = "lib/derby.jar"; // NOI18N

    // XXX these should actually be localized, but we'd have to localize
    // DriverListUtil in the db module first
    public static final String DRIVER_DISP_NAME_NET = "Java DB (Network)"; // NOI18N
    public static final String DRIVER_DISP_NAME_EMBEDDED = "Java DB (Embedded)"; // NOI18N

    private static final String DRIVER_NAME_NET = "apache_derby_net"; // NOI18N
    private static final String DRIVER_NAME_EMBEDDED = "apache_derby_embedded"; // NOI18N

    public static DerbyOptions getDefault() {
        return INSTANCE;
    }

    protected final String putProperty(String key, String value, boolean notify) {
        String retval = NbPreferences.forModule(DerbyOptions.class).get(key, null);
        if (value != null) {
            // LOGGER.log(Level.FINE, "Setting property {0} to {1}", key, value); // NOI18N
            NbPreferences.forModule(DerbyOptions.class).put(key, value);
        } else {
            // LOGGER.log(Level.FINE, "Removing property {0}", key); // NOI18N
            NbPreferences.forModule(DerbyOptions.class).remove(key);
        }
        return retval;
    }

    protected final String getProperty(String key) {
        return NbPreferences.forModule(DerbyOptions.class).get(key, null);
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
        if (location !=  null && location.length() > 0) {
            File locationFile = new File(location).getAbsoluteFile();
            if (!locationFile.exists()) {
                String message = NbBundle.getMessage(DerbyOptions.class, "ERR_DirectoryDoesNotExist", locationFile);
                IllegalArgumentException e = new IllegalArgumentException(message);
                Exceptions.attachLocalizedMessage(e, message);
                throw e;
            }
            if (!Util.isDerbyInstallLocation(locationFile)) {
                String message = NbBundle.getMessage(DerbyOptions.class, "ERR_InvalidDerbyLocation", locationFile);
                IllegalArgumentException e = new IllegalArgumentException(message);
                Exceptions.attachLocalizedMessage(e, message);
                throw e;
            }
        }

        synchronized (this) {
            stopDerbyServer();
            if (location != null && location.length() <= 0) {
                location = getDefaultInstallLocation();
            }
            registerDrivers(location);
            LOGGER.log(Level.FINE, "Setting location to {0}", location); // NOI18N
            putProperty(PROP_DERBY_LOCATION, location, true);
        }
    }

    public synchronized boolean trySetLocation(String location) {
        LOGGER.log(Level.FINE, "trySetLocation: Trying to set location to {0}", location); // NOI18N
        if (getLocation().length() == 0) {
            setLocation(location);
            LOGGER.fine("trysetLocation: Succeeded"); // NOI18N
            return true;
        } else {
            LOGGER.fine("trySetLocation: Another location already set"); // NOI18N
            return false;
        }
    }

    /**
     * Returns the Derby system home or an emtpy string if the system home
     * is not set. Never returns null.
     */
    public String getSystemHome() {
        String systemHome = (String)getProperty(PROP_DERBY_SYSTEM_HOME);
        if (systemHome == null) {
            systemHome = System.getProperty(NETBEANS_DERBY_SYSTEM_HOME);
        }
        if (systemHome == null) {
            systemHome = ""; // NOI18N
        }
        return systemHome;
    }

    public void setSystemHome(String derbySystemHome) {
        if (derbySystemHome != null && derbySystemHome.length() > 0) {
            File derbySystemHomeFile = new File(derbySystemHome).getAbsoluteFile();
            if (!derbySystemHomeFile.exists() || !derbySystemHomeFile.isDirectory()) {
                String message = NbBundle.getMessage(DerbyOptions.class, "ERR_DirectoryDoesNotExist", derbySystemHomeFile);
                IllegalArgumentException e = new IllegalArgumentException(message);
                Exceptions.attachLocalizedMessage(e, message);
                throw e;
            }
            if (!derbySystemHomeFile.canWrite()) {
                String message = NbBundle.getMessage(DerbyOptions.class, "ERR_DirectoryIsNotWritable", derbySystemHomeFile);
                IllegalArgumentException e = new IllegalArgumentException(message);
                Exceptions.attachLocalizedMessage(e, message);
                throw e;
            }
        }

        synchronized (this) {
            stopDerbyServer();
            putProperty(PROP_DERBY_SYSTEM_HOME, derbySystemHome, true);
        }
    }

    static String getDefaultInstallLocation() {
        File location = InstalledFileLocator.getDefault().locate(INST_DIR, null, false);
        if (location == null) {
            return null;
        }
        if (!Util.isDerbyInstallLocation(location)) {
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
            Exceptions.printStackTrace(e);
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
                File file = null;
                if ("file".equals(urls[i].getProtocol())) { // NOI18N
                    // FileObject's do not work nice if the file urls[i] points to doesn't exist
                    try {
                        file = new File(urls[i].toURI());
                    } catch (URISyntaxException e) {
                        LOGGER.log(Level.WARNING, null, e);
                    }
                } else {
                    FileObject fo = URLMapper.findFileObject(urls[j]);
                    if (fo != null) {
                        file = FileUtil.toFile(fo);
                    }
                }
                if (file != null) {
                    String driverFile = file.getAbsolutePath();
                    if (driverFile.startsWith(currentLocation)) {
                        continue;
                    }
                }
                fromCurrentLocation = false;
                break;
            }

            if (fromCurrentLocation) {
                try {
                    JDBCDriverManager.getDefault().removeDriver(driver);
                } catch (DatabaseException e) {
                    LOGGER.log(Level.WARNING, null, e);
                    // better to return if the existing driver could not be registered
                    // otherwise we would register yet another one
                    return;
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
                    LOGGER.log(Level.WARNING, null, e);
                } catch (DatabaseException e) {
                    LOGGER.log(Level.WARNING, null, e);
                }
            }
        }
    }

    private static BeanNode createViewNode() throws java.beans.IntrospectionException {
        return new BeanNode<DerbyOptions>(DerbyOptions.getDefault());
    }
}
