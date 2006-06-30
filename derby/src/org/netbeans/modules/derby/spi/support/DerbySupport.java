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

package org.netbeans.modules.derby.spi.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.RegisterDerby;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Andrei Badea
 */
public final class DerbySupport {
    
    /**
     * The default derby.system.home location relative to the home directory.
     */
    private static final String DEFAULT_SYSTEM_HOME = ".netbeans-derby"; // NOI18N
    
    private DerbySupport() {
    }
    
    /**
     * Sets the location of the Derby installation and registers the Derby drivers.
     * The Derby installation must have a lib subdirectory in which the Derby JAR
     * files are located.
     *
     * @param location the jars locations. This must be an existing directory.
     */
    public static void setLocation(String location) {
        DerbyOptions.getDefault().setLocation(location);
    }
    
    /**
     * Returns the location of the Derby jars. Never returns null,
     * instead it returns an empty string if the location is unknown.
     */
    public static String getLocation() {
        return DerbyOptions.getDefault().getLocation();
    }
    
    /** 
     * Sets the Derby system home, that is, the directory 
     * where the Derby databases are located.
     */
    public static void setSystemHome(String systemHome) {
        DerbyOptions.getDefault().setSystemHome(systemHome);
    }
    
    /** 
     * Returns the Derby system home.
     */
    public static String getSystemHome() {
        return DerbyOptions.getDefault().getSystemHome();
    }
    
    /**
     * Returns the default Derby system home. It is not guaranteed that
     * the directory returned by this method exists.
     */
    public static String getDefaultSystemHome() {
        String userHome = System.getProperty("user.home"); // NOI18N
        return new File(userHome, DEFAULT_SYSTEM_HOME).getAbsolutePath();
    }
    
    /**
     * Creates a sample directory in the Derby system home and registers it
     * in the Database Explorer.
     *
     * <p>This method requires at least the Derby net drivers to be registered. 
     * Otherwise it will throw an IllegalStateException.</p>
     *
     * <p>This method might take a long time to perform. It is advised that
     * clients do not call this method from the event dispatching thread, 
     * where it would block the UI.</p>
     *
     * @throws IllegalStateException if the Derby net driver is not registered.
       @throws DatabaseException if an error occurs while registering
     *         the new database in the Database Explorer.
     */
    public static DatabaseConnection registerSampleDatabase() throws DatabaseException {
        String targetDirectory = getSystemHome();
        if ("".equals(targetDirectory)) { // NOI18N
            throw new IllegalStateException("derby.system.home not set"); // NOI18N
        }
        File targetDirFile = new File(targetDirectory);
        if (!targetDirFile.exists()){
            targetDirFile.mkdirs();
        }
        File source = InstalledFileLocator.getDefault().locate("modules/ext/derbysampledb.zip", null, false);
        FileObject target = FileUtil.toFileObject(targetDirFile);
        FileObject sampleDir = target.getFileObject("sample");
        if (sampleDir == null) {
          extractZip(source, target);
        }
        JDBCDriver drivers[] = JDBCDriverManager.getDefault().getDrivers(RegisterDerby.NET_DRIVER_CLASS_NAME);
        if (drivers.length == 0) {
            throw new IllegalStateException("derby driver not found"); // NOI18N
        }
        DatabaseConnection con = DatabaseConnection.create(drivers[0], "jdbc:derby://localhost:1527/sample", "app", "APP", "app", true); // NOI18N
        ConnectionManager.getDefault().addConnection(con);
        return con;
    }
    
    private static void extractZip(File source, FileObject target) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(source);
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze;


            while ((ze = zis.getNextEntry()) != null) {
                String name = ze.getName();

                if (ze.isDirectory()) {
                    FileUtil.createFolder(target, name);
                    continue;
                }

                // copy the file
                FileObject fd = FileUtil.createData(target, name);
                FileLock lock = fd.lock();

                try {
                    OutputStream os = fd.getOutputStream(lock);

                    try {
                        FileUtil.copy(zis, os);
                    } finally {
                        os.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        } catch (FileNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } finally {
            if (is != null)
                try {
                    is.close();
                }
                catch (IOException e) {
                    // can't do anything
                }
        }
    }
}
