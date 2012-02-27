/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
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
package org.netbeans.modules.web.plugins;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


import org.openide.util.Utilities;


/**
 * @author ads
 *
 */
public final class Utils {
    
    private static final String APPDATA_CMD = "cmd /c echo %AppData%"; // NOI18N
    
    private Utils(){
    }
    
    public static String[] getUserPaths(String... paths) {
        String[] result = new String[paths.length];
        String appRoot = getUserHome();

        if (appRoot == null) {
            return null;
        }
        for (int i = 0; i < paths.length; i++) {
            result[i] = appRoot + paths[i];
        }

        return result;
    }
    
    private static String getUserHome() {
        String userHome = System.getProperty("user.home"); // NOI18N

        if (!Utilities.isWindows()) {
            return userHome;
        } 
        else {
            BufferedReader br = null;
            /*try {
                Process process = Runtime.getRuntime().exec(APPDATA_CMD);
                process.waitFor();

                InputStream input = process.getInputStream();
                br = new BufferedReader(new InputStreamReader(input));

                while (br.ready()) {
                    String nextLine = br.readLine();

                    if (nextLine.trim().length() == 0) continue;

                    File f = new File(nextLine.trim());
                    if (f.exists() && f.isDirectory()) {
                        return f.getAbsolutePath();
                    }
                }
            }
            catch (Exception ex) {
                Logger.getLogger(Utils.class.getCanonicalName()).
                    log(Level.INFO, "Unable to run process: " + APPDATA_CMD, ex );      // NOI18N
            }
            finally {
                if (br != null) {
                    try {
                        br.close();
                    }
                    catch (IOException ex) {
                        Logger.getLogger(Utils.class.getCanonicalName()).
                            log(Level.INFO, 
                                    "Unable close process input stream reader " ,       // NOI18N 
                                        ex );      
                    }
                }
            }*/

            //return userHome + File.separator + "Application Data";  // NOI18N
            return System.getenv("AppData");                          // NOI18N
        }
    }

    public static boolean compareChecksum( File zipFile, File checksumFile) {
        if (zipFile == null) {
            return true;
        }
        
        String checkSumName = checksumFile.getName();
        
        if (checksumFile.exists() && checksumFile.isFile()) {
            ZipFile extensionZip = null;
            try {
                extensionZip = new ZipFile(zipFile);
                ZipEntry entry = extensionZip.getEntry(checkSumName);
                
                if (entry != null) {                    
                    BufferedInputStream profileInput  = new BufferedInputStream(
                            new FileInputStream(checksumFile));
                    InputStream xpiInput = extensionZip.getInputStream(entry);
                    
                    try {
                        int red;
                        
                        do {
                            red = profileInput.read();
                            if ( red != xpiInput.read() ){
                                return false;
                            }
                        } 
                        while (red>=0);
                        
                        return true;
                    } 
                    finally {
                        profileInput.close();
                        xpiInput.close();
                    }
                    
                }
            } 
            catch (IOException ex) {
                Logger.getLogger(Utils.class.getCanonicalName()).
                    log(Level.SEVERE, null , ex); 
            } 
            finally {
                if (extensionZip != null) {
                    try {
                        extensionZip.close();
                    }
                    catch (IOException ex) {
                        Logger.getLogger(Utils.class.
                                getCanonicalName()).log(
                                        Level.SEVERE, 
                                            "Error closing zip file", ex);  // NOI18N
                    }
                }
            }
        }
        
        return false;
    }
    
    public static void rmDir(File folder) {
        if (!folder.exists()) {
            return;
        }
        
        File[] children = folder.listFiles();
        if (children != null) {
            for (File child : children) {
                rmDir(child);
            }
        }
        folder.delete();
    }

    public static void extractFiles(File zipFile, File destDir) throws IOException {
        ZipFile zip = new ZipFile(zipFile);
        try {
            destDir.mkdirs();
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String fileName = entry.getName();
                
                if (entry.isDirectory()) {
                    File newFolder = new File(destDir, fileName);
                    newFolder.mkdirs();
                } 
                else {
                    File file = new File(destDir, fileName);
                    file.getParentFile().mkdirs();
                    if (file.exists() && file.isDirectory()) {
                        throw new IOException("Cannot write normal file " +
                        		"to existing directory with the same path");  // NOI18N
                    }
                    
                    BufferedOutputStream output = new BufferedOutputStream(
                            new FileOutputStream(file));
                    InputStream input = zip.getInputStream(entry);
                    
                    try {
                        final byte[] buffer = new byte[4096];
                        int len;
                        while ((len = input.read(buffer)) >= 0) {
                            output.write(buffer, 0, len);
                        }
                    } 
                    finally {
                        output.close();
                        input.close();
                    }
                }
            }
        } 
        finally {
            zip.close();
        }
    }

}
