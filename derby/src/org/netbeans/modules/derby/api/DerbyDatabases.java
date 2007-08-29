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

package org.netbeans.modules.derby.api;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.derby.DbURLClassLoader;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.Util;
import org.netbeans.modules.derby.RegisterDerby;
import org.netbeans.modules.derby.spi.support.DerbySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Andrei Badea
 *
 * @since 1.7
 */
public final class DerbyDatabases {

    private DerbyDatabases() {
    }

    /**
     * Checks if the Derby database is registered and the Derby system
     * home is set.
     *
     * @return true if Derby is registered, false otherwise.
     */
    public static boolean isDerbyRegistered() {
        return DerbySupport.getLocation().length() > 0 && DerbySupport.getSystemHome().length() > 0; // NOI18N
    }
    
    /**
     * Returns the Derby system home.
     *
     * @return the Derby system home or null if it is not known.
     */
    public static File getSystemHome() {
        String systemHome = DerbyOptions.getDefault().getSystemHome();
        if (systemHome.length() >= 0) {
            return new File(systemHome);
        }
        return null;
    }

    /**
     * Checks if the given database exists in the Derby system home.
     *
     * @return true if the database exists, false otherwise.
     *
     * @throws NullPointerException if <code>databaseName</code> is null.
     */
    public static boolean databaseExists(String databaseName) {
        if (databaseName == null) {
            throw new NullPointerException("The databaseName parameter cannot be null"); // NOI18N
        }
        // just because it makes sense, not really needed anywhere probably
        if ("".equals(databaseName)) { // NOI18N
            return false;
        }

        String systemHome = DerbySupport.getSystemHome();
        if (systemHome.length() <= 0) { // NOI18N
            return false;
        }
        File databaseFile = new File(systemHome, databaseName);
        return databaseFile.exists();
    }

    /**
     * Returns the first free database name using the specified base name.
     * The method attempts to create a database name by appending numbers to
     * the base name, like in "base1", "base2", etc. and returns the
     * first free name found.
     *
     * @return a database name or null if a free database name could not be found.
     *
     * @throws NullPointerException in the <code>baseDatabaseName</code> parameter
     *         could not be found.
     */
    public static String getFirstFreeDatabaseName(String baseDatabaseName) {
        if (baseDatabaseName == null) {
            throw new NullPointerException("The baseDatabaseName parameter cannot be null"); // NOI18N
        }

        String systemHome = DerbySupport.getSystemHome();
        if (systemHome.length() <= 0) { // NOI18N
            return baseDatabaseName;
        }
        File databaseFile = new File(systemHome, baseDatabaseName);
        if (!databaseFile.exists()) {
            return baseDatabaseName;
        }

        int i = 1;
        while (i <= Integer.MAX_VALUE) {
            String databaseName = baseDatabaseName + String.valueOf(i);
            databaseFile = new File(systemHome, databaseName);
            if (!databaseFile.exists()) {
                return databaseName;
            }
            i++;
        }
        return null;
    }

    /**
     * Returns the code point of the first illegal character in the given database
     * name.
     *
     * @return the code point of the first illegal character or -1 if all characters
     *         are valid.
     *
     * @throws NullPointerException if <code>databaseName</code> is null.
     */
    public static int getFirstIllegalCharacter(String databaseName) {
        if (databaseName == null) {
            throw new NullPointerException("The databaseName parameter cannot be null"); // NOI18N
        }

        for (int i = 0; i < databaseName.length(); i++) {
            char ch = databaseName.charAt(i);
            if (ch == '/') {
                return (int)ch;
            }
            if (ch == File.separatorChar) {
                return (int)ch;
            }
        }

        return -1;
    }

    /**
     * Creates a new empty database in the Derby system and registers
     * it in the Database Explorer. A <code>DatabaseException</code> is thrown
     * if a database with the given name already exists.
     *
     * <p>This method requires at least the Derby network driver to be registered.
     * Otherwise it will throw an IllegalStateException.</p>
     *
     * <p>This method might take a long time to perform. It is advised that
     * clients do not call this method from the event dispatching thread,
     * where it would block the UI.</p>
     *
     * @param  databaseName the name of the database to created; cannot be nul.
     * @param  user the user to set up authentication for. No authentication
     *         will be set up if <code>user</code> is null or an empty string.
     * @param  password the password for authentication.
     *
     * @throws NullPointerException if <code>databaseName</code> is null.
     * @throws IllegalStateException if the Derby network driver is not registered.
     * @throws DatabaseException if an error occurs while creating the database
     *         or registering it in the Database Explorer.
     * @throws IOException if the Derby system home directory does not exist
     *         and it cannot be created.
     */
    public static DatabaseConnection createDatabase(String databaseName, String user, String password) throws DatabaseException, IOException, IllegalStateException {
        if (databaseName == null) {
            throw new NullPointerException("The databaseName parameter cannot be null"); // NOI18N
        }

        ensureSystemHome();
        RegisterDerby.getDefault().ensureStarted();

        Driver driver = loadDerbyNetDriver();
        Properties props = new Properties();
        boolean setupAuthentication = (user != null && user.length() >= 0);

        try {
            String url = "jdbc:derby://localhost:" + RegisterDerby.getDefault().getPort() + "/" + databaseName; // NOI18N
            String urlForCreation = url + ";create=true"; // NOI18N
            Connection connection = driver.connect(urlForCreation, props);


            try {
                if (setupAuthentication) {
                    setupDatabaseAuthentication(connection, user, password);
                }
            } finally {
                connection.close();
            }

            if (setupAuthentication) {
                // we have to reboot the database for the authentication properties
                // to take effect
                try {
                    connection = driver.connect(url + ";shutdown=true", props); // NOI18N
                } catch (SQLException e) {
                    // OK, will always occur
                }
            }
        } catch (SQLException sqle) {
            DatabaseException dbe = new DatabaseException(sqle.getMessage());
            dbe.initCause(sqle);
            throw dbe;
        }

        return registerDatabase(databaseName, user,
                setupAuthentication ? user.toUpperCase() : "APP", // NOI18N
                setupAuthentication ? password : null, setupAuthentication);
    }

    /**
     * Creates the sample database in the Derby system home
     * using the default user and password ("app", resp. "app") and registers
     * it in the Database Explorer. If the sample database already exists
     * it is just registered.
     *
     * <p>This method requires at least the Derby network driver to be registered.
     * Otherwise it will throw an IllegalStateException.</p>
     *
     * <p>This method might take a long time to perform. It is advised that
     * clients do not call this method from the event dispatching thread,
     * where it would block the UI.</p>
     *
     * @throws IllegalStateException if the Derby network driver is not registered.
     * @throws DatabaseException if an error occurs while creating the database
     *         or registering it in the Database Explorer.
     * @throws IOException if the Derby system home directory does not exist
     *         and it cannot be created.
     */
    public static DatabaseConnection createSampleDatabase() throws DatabaseException, IOException, IllegalStateException {
        extractSampleDatabase("sample"); // NOI18N
        return registerDatabase("sample", "app", "APP", "app", true); // NOI18N
    }

    /**
     * Creates the sample database in the Derby system home using the
     * given database name and the default user and password ("app", resp. "app") and registers
     * it in the Database Explorer. A <code>DatabaseException</code> is thrown
     * if a database with the given name already exists.
     *
     * <p>This method requires at least the Derby network driver to be registered.
     * Otherwise it will throw an IllegalStateException.</p>
     *
     * <p>This method might take a long time to perform. It is advised that
     * clients do not call this method from the event dispatching thread,
     * where it would block the UI.</p>
     *
     * @throws NullPointerException if <code>databaseName</code> is null.
     * @throws IllegalStateException if the Derby network driver is not registered.
     * @throws DatabaseException if an error occurs while registering
     *         the new database in the Database Explorer.
     * @throws IOException if the Derby system home directory does not exist
     *         and it cannot be created.
     */
    public static DatabaseConnection createSampleDatabase(String databaseName) throws DatabaseException, IOException {
        if (databaseName == null) {
            throw new NullPointerException("The databaseName parameter cannot be null"); // NOI18N
        }

        extractSampleDatabase(databaseName);
        return registerDatabase(databaseName, "app", "APP", "app", true); // NOI18N
    }

    /**
     * Extracts the sample database under the given name in the Derby system home.
     * Does not overwrite an existing database.
     *
     * <p>Not public because used in tests.</p>
     */
    static void extractSampleDatabase(String databaseName) throws IOException{
        File systemHomeFile = ensureSystemHome();
        File sourceFO = InstalledFileLocator.getDefault().locate("modules/ext/derbysampledb.zip", null, false); // NOI18N
        FileObject systemHomeFO = FileUtil.toFileObject(systemHomeFile);
        FileObject sampleFO = systemHomeFO.getFileObject(databaseName);
        if (sampleFO == null) {
            sampleFO = systemHomeFO.createFolder(databaseName);
            Util.extractZip(sourceFO, sampleFO);
        }
    }

    /**
     * Tries to ensure the Derby system home exists (attempts to create it if necessary).
     */
    private static File ensureSystemHome() throws IOException {
        String systemHome = DerbySupport.getSystemHome();
        boolean noSystemHome = false;
        if (systemHome.length() <= 0) { // NOI18N
            noSystemHome = true;
            systemHome = DerbySupport.getDefaultSystemHome();
        }
        File systemHomeFile = new File(systemHome);
        if (!systemHomeFile.exists()){
            // issue 113747: if mkdirs() fails, it can be caused by another thread having succeeded,
            // since there are a few places where sample databases are created at first startup
            if (!systemHomeFile.mkdirs() && !systemHomeFile.exists()) {
                throw new IOException("Could not create the derby.system.home directory " + systemHomeFile); // NOI18N
            }
        }
        if (noSystemHome) {
            DerbySupport.setSystemHome(systemHome);
        }
        return systemHomeFile;
    }

    /**
     * Registers in the Database Explorer the specified database
     * on the local Derby server.
     */
    private static DatabaseConnection registerDatabase(String databaseName, String user, String schema, String password, boolean rememberPassword) throws DatabaseException {
        JDBCDriver drivers[] = JDBCDriverManager.getDefault().getDrivers(DerbyOptions.DRIVER_CLASS_NET);
        if (drivers.length == 0) {
            throw new IllegalStateException("The " + DerbyOptions.DRIVER_DISP_NAME_NET + " driver was not found"); // NOI18N
        }
        DatabaseConnection dbconn = DatabaseConnection.create(drivers[0], "jdbc:derby://localhost:" + RegisterDerby.getDefault().getPort() + "/" + databaseName, user, schema, password, rememberPassword); // NOI18N
        ConnectionManager.getDefault().addConnection(dbconn);
        return dbconn;
    }

    /**
     * Sets up authentication for the database to which the given connection
     * is connected.
     */
    private static void setupDatabaseAuthentication(Connection conn, String user, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("{call SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(?, ?)}"); // NOI18N
        try {
            stmt.setString(1, "derby.connection.requireAuthentication"); // NOI18N
            stmt.setString(2, "true"); // NOI18N
            stmt.execute();

            stmt.clearParameters();
            stmt.setString(1, "derby.authentication.provider"); // NOI18N
            stmt.setString(2, "BUILTIN"); // NOI18N
            stmt.execute();

            stmt.clearParameters();
            stmt.setString(1, "derby.user." + user); // NOI18N
            stmt.setString(2, password); // NOI18N
            stmt.execute();
        } finally {
            stmt.close();
        }
    }

    /**
     * Loads the Derby network driver.
     */
    private static Driver loadDerbyNetDriver() throws DatabaseException, IllegalStateException {
        Exception exception = null;
        try {
            File derbyClient = Util.getDerbyFile("lib/derbyclient.jar"); // NOI18N
            if (derbyClient == null || !derbyClient.exists()) {
                throw new IllegalStateException("The " + DerbyOptions.DRIVER_DISP_NAME_NET + " driver was not found"); // NOI18N
            }
            URL[] driverURLs = new URL[] { derbyClient.toURI().toURL() }; // NOI18N
            DbURLClassLoader l = new DbURLClassLoader(driverURLs);
            Class c = Class.forName(DerbyOptions.DRIVER_CLASS_NET, true, l);
            return (Driver)c.newInstance();
        } catch (MalformedURLException e) {
            exception = e;
        } catch (IllegalAccessException e) {
            exception = e;
        } catch (ClassNotFoundException e) {
            exception = e;
        } catch (InstantiationException e) {
            exception = e;
        }
        if (exception != null) {
            DatabaseException dbe = new DatabaseException(exception.getMessage());
            dbe.initCause(exception);
            throw dbe;
        }
        // should never get here
        return null;
    }
}
