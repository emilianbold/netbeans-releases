/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class ServerPropertyFileManager {
    // Constants
    public final static String FILENAME_KEY = "server.file.name";
    public final static String SERVERNAME_KEY = "server.name";
    public final static String CONFIG_DIR_NAME = "CoherenceServers";
    // Local Variables

    private static ResourceBundle bundle = NbBundle.getBundle(ServerPropertyFileManager.class);
    private static HashMap<String, Properties> serverPropertiesMap = new HashMap<String, Properties>();
    private static HashMap<String, Boolean> serverRunning = new HashMap<String, Boolean>();
    private static Logger logger = Logger.getLogger(ServerPropertyFileManager.class.getCanonicalName());

    public static synchronized void saveProperties(Properties serverProp) throws Exception {
        
        if (serverProp == null) {
            throw new Exception("Properties file must be supplied");
        }
        
        Enumeration values = serverProp.elements();
        String value = null;
        while (values.hasMoreElements()) {
            value = values.nextElement().toString();
            logger.log(Level.INFO, "*** APH-I2 : value " +value);
        }

        Enumeration keys = serverProp.keys();
        String key = null;
        while (keys.hasMoreElements()) {
            key = keys.nextElement().toString();
            logger.log(Level.INFO, "*** APH-I2 : key / value " +key + " = "+ serverProp.getProperty(key));
        }

        String filename = serverProp.getProperty(FILENAME_KEY,"");

        logger.log(Level.INFO, "*** APH-I1 : saveProperties() filenameKey " + FILENAME_KEY);
        logger.log(Level.INFO, "*** APH-I1 : saveProperties() filename " + filename);
        logger.log(Level.INFO, "*** APH-I1 : saveProperties() Saving " + serverProp);

        // Create / Get Server Folder
//        FileObject serverFolderFO = FileUtil.createFolder(Repository.getDefault().getDefaultFileSystem().getRoot(), bundle.getString("CONFIG_DirectoryName"));
        FileObject serverFolderFO = FileUtil.createFolder(FileUtil.getConfigRoot(), CONFIG_DIR_NAME);

        if (filename == null || filename.trim().length() == 0) {
            // Build filename
            filename = FileUtil.findFreeFileName(serverFolderFO, "server.properties", ".properties");
            logger.log(Level.INFO, "*** APH-I2 : saveProperties() Server Properties File (FileUtil.findFreeFileName) " + filename);
            filename = "server" + System.currentTimeMillis() + ".properties";
            logger.log(Level.INFO, "*** APH-I2 : saveProperties() Server Properties Filename " + filename);
            serverProp.setProperty(FILENAME_KEY, filename);
        } else {
            logger.log(Level.INFO, "*** APH-I2 : saveProperties() Using existing File " + filename +" Properties "+serverProp);
        }

        // Create / Get File Object
        FileObject serverPropFile = FileUtil.createData(serverFolderFO, filename);
        FileLock lock = serverPropFile.lock();
        OutputStream os = serverPropFile.getOutputStream(lock);
        serverProp.store(os, "Server Details");
        // Save to local Cache
        serverPropertiesMap.put(filename, serverProp);
        serverProp = null;
        try {
            os.flush();
        } catch (Exception e) {
        } finally {
            try {
                os.close();
            } catch (Exception e) {
            }
        }
        os = null;
        lock.releaseLock();
        serverPropFile = null;
        logger.log(Level.INFO, "*** APH-I1 : saveProperties() Unlocked File");
//        AllServersNotifier.changed();
    }

    public static Properties loadProperties(FileObject serverPropFile) throws Exception {
        logger.log(Level.INFO, "*** APH-I1 : loadProperties() Looking for " + serverPropFile.getNameExt());
        Properties serverProp = serverPropertiesMap.get(serverPropFile.getNameExt());

        serverProp = null;

        InputStream is = null;

        if (serverProp == null) {
            serverProp = new Properties();
            try {
                if (!serverPropFile.isLocked() && serverPropFile.canRead()) {
                    FileLock lock = serverPropFile.lock();
                    is = serverPropFile.getInputStream();
                    logger.log(Level.INFO, "*** APH-I1 : Loading for " + serverPropFile.getNameExt());
                    serverProp.load(is);
                    try {
                        is.close();
                    } catch (Exception e) {
                    }
                    lock.releaseLock();
                    serverPropertiesMap.put(serverProp.getProperty(FILENAME_KEY), serverProp);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "*** APH-I3 : Exception " + e.getMessage());
                e.printStackTrace();
                serverProp = null;
            }
        }

        logger.log(Level.INFO, "*** APH-I1 : loadProperties() Found " + serverProp);
        return serverProp;
    }

    public static void deleteProperties(FileObject serverPropFile) throws Exception {
        serverPropertiesMap.remove(serverPropFile.getNameExt());
        serverPropFile.delete();
    }

    public static void deleteProperties(String serverPropFilename) throws Exception {
        // Create / Get Server Folder
//        FileObject jmsServerFolderFO = FileUtil.createFolder(Repository.getDefault().getDefaultFileSystem().getRoot(), bundle.getString("CONFIG_DirectoryName"));
        FileObject serverFolderFO = FileUtil.createFolder(FileUtil.getConfigRoot(), CONFIG_DIR_NAME);
        FileObject serverPropFile = FileUtil.createData(serverFolderFO, serverPropFilename);
        deleteProperties(serverPropFile);
    }

}
