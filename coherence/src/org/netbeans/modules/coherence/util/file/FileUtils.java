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
package org.netbeans.modules.coherence.util.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ah195842
 */
public class FileUtils {

    /**
     *
     */
    public final static String jpeg = "jpeg";
    /**
     *
     */
    public final static String jpg = "jpg";
    /**
     *
     */
    public final static String gif = "gif";
    /**
     *
     */
    public final static String tiff = "tiff";
    /**
     *
     */
    public final static String tif = "tif";
    /**
     *
     */
    public final static String png = "png";
    /**
     *
     */
    public final static String xml = "xml";
    /**
     *
     */
    public final static String sql = "sql";
    /**
     *
     */
    public final static String backupExt = ".bak";
    private Logger myLogger = Logger.getLogger(getClass().getPackage().getName());
    private static FileUtils instance = null;

    /** Creates a new instance of FileUtils */
    protected FileUtils() {
    }

    /**
     *
     * @return
     */
    public static synchronized FileUtils getInstance() {
        if (instance == null) {
            instance = new FileUtils();
        }
        return instance;
    }

    /**
     * @param dir
     * @param recursive
     * @param directories
     * @param files
     * @return
     *
     * List all Files and Directories, for the specified file list. If Recursive is true then
     * the method recursively calls itself for each directory in the list.
     */
    protected File[] listFiles(File[] dir, boolean recursive, boolean directories, boolean files) {
        List subFilesList = new ArrayList();
        File subFiles[] = null;
        File currFile = null;

        // If the list is null then return
        if (dir == null) {
            return null;
        }

        for (int i = 0; i < dir.length; i++) {
            currFile = dir[i];
            if ((directories && currFile.isDirectory()) || (files && currFile.isFile())) {
                subFilesList.add(currFile);
            }
            if (recursive && currFile.isDirectory()) {
                File[] subDirs = listFiles(currFile.listFiles(), recursive, directories, files);
                if (subDirs != null) {
                    subFilesList.addAll(subFilesList.size(), Arrays.asList(subDirs));
                }
            }
        }
        if (!subFilesList.isEmpty()) {
            subFiles = new File[subFilesList.size()];
            subFiles = (File[]) subFilesList.toArray(subFiles);
        }

        return subFiles;
    }

    /**
     *
     * @param dir
     * @return
     */
    public File[] getSubDirectories(String dir) {
        return getSubDirectories(dir, false);
    }

    /**
     *
     * @param dir
     * @return
     */
    public File[] getSubDirectories(File dir) {
        return getSubDirectories(dir, false);
    }

    /**
     *
     * @param dir
     * @param recursive
     * @return
     */
    public File[] getSubDirectories(File dir, boolean recursive) {
        return getSubDirectories(dir.listFiles(), recursive);
    }

    /**
     *
     * @param dir
     * @param recursive
     * @return
     */
    public File[] getSubDirectories(String dir, boolean recursive) {
        return getSubDirectories(new File(dir), recursive);
    }

    /**
     *
     * @param dir
     * @return
     */
    public File[] getSubDirectories(File[] dir) {
        return getSubDirectories(dir, false);
    }

    /**
     *
     * @param dir
     * @param recursive
     * @return
     */
    public File[] getSubDirectories(File[] dir, boolean recursive) {
        return listFiles(dir, recursive, true, false);
    }

    /**
     *
     * @param dir
     * @return
     */
    public File[] getSubFiles(File dir) {
        return getSubFiles(dir, false);
    }

    /**
     *
     * @param dir
     * @return
     */
    public File[] getSubFiles(String dir) {
        return getSubFiles(dir, false);
    }

    /**
     *
     * @param dir
     * @param recursive
     * @return
     */
    public File[] getSubFiles(File dir, boolean recursive) {
        return getSubFiles(dir.listFiles(), recursive);
    }

    /**
     *
     * @param dir
     * @param recursive
     * @return
     */
    public File[] getSubFiles(String dir, boolean recursive) {
        return getSubFiles(new File(dir), recursive);
    }

    /**
     *
     * @param dir
     * @return
     */
    public File[] getSubFiles(File[] dir) {
        return getSubFiles(dir, false);
    }

    /**
     *
     * @param dir
     * @param recursive
     * @return
     */
    public File[] getSubFiles(File[] dir, boolean recursive) {
        return listFiles(dir, recursive, false, true);
    }

    /**
     *
     * @param dir
     * @param recursive
     * @return
     */
    public File[] listFiles(File dir, boolean recursive) {
        return listFiles(dir.listFiles(), recursive);
    }

    /**
     *
     * @param dir
     * @param recursive
     * @return
     */
    public File[] listFiles(File[] dir, boolean recursive) {
        return listFiles(dir, recursive, true, true);
    }

    /**
     *
     * @param f
     * @return
     */
    public String getExtension(File f) {
        return getExtension(f.getName());
    }

    /**
     *
     * @param s
     * @return
     */
    public String getExtension(String s) {
        String ext = null;
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public String removeExtension(String s) {
        String ext = getExtension(s);
        String name = s;
        if (ext != null && ext.length() > 0) {
            name = s.substring(0, (s.length() - (ext.length() + 1)));
        }
        return name;
    }

    /**
     *
     * @param fullname
     * @return
     */
    public String getFilename(String fullname) {
        String name = fullname;
        String s = convertFileSystemSeparators(fullname);
        char fsSeparator = System.getProperty("file.separator").charAt(0);

        int i = s.lastIndexOf(fsSeparator);
        if (i < 0) {
            i = s.lastIndexOf('/');
        }

        if (i > 0 && i < s.length() - 1) {
            name = s.substring(i + 1);
        }
        return name;
    }

    /**
     *
     * @param fullname
     * @return
     */
    public String getDirectory(String fullname) {
        String filename = getFilename(fullname);
        String dirname = "";

        if (filename != null && filename.length() < fullname.length()) {
            dirname = fullname.substring(0, ((fullname.length() - filename.length()) - 1));
        }

        return dirname;
    }

    /**
     * Removes the specified file or directory, and all subdirectories
     * @param file The file or directory that you wish to delete
     */
    public synchronized void delTree(java.io.File file) {
        boolean success = false;
        if (file.exists()) {
            if (file.isDirectory()) {
                java.io.File[] dir = file.listFiles();
                for (int i = 0; i < dir.length; i++) {
                    delTree(dir[i]);
                }
                //				success = file.delete();
                if (file.exists()) {
                    if (!file.delete()) {
                        System.out.println("Failed to delete " + file.getAbsolutePath());
                    }
                }
            } else {
                //			    success = file.delete();
                if (file.exists()) {
                    if (!file.delete()) {
                        System.out.println("Failed to delete " + file.getAbsolutePath());
                    }
                }
            }
        //		    System.out.println("Deleted : "+file.getAbsolutePath()+" - "+success);
        }
    }

    /**
     *
     * @param filename
     */
    public void delTree(String filename) {
        if (filename != null) {
            File file = new File(filename);
            delTree(file);
        }
    }

    /**
     * @param file
     * @return
     */
    public synchronized boolean backupFile(File file) {
        File backupFile = null;
        String backupFileName = null;
        boolean retVal = false;

        //		System.out.println("Backing up "+file);
        if (file != null) {
            backupFileName = file.getAbsolutePath() + backupExt;
            backupFile = new File(backupFileName);
            try {
                // Delete backup if it already exists
                if (removeBackupFile(file)) {
                    // Rename File
                    retVal = file.renameTo(backupFile);
                }
            } catch (Exception e) {
                System.err.println("Failed to rename file : " + file.getAbsolutePath() + " to " + backupFileName);
                retVal = false;
            }
        }

        return retVal;
    }

    /**
     * @param file
     * @return
     */
    public synchronized boolean backupCopyFile(File file) {
        File backupFile = null;
        String backupFileName = null;
        boolean retVal = false;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        int inOut = 0;

        if (file != null) {
            backupFileName = file.getAbsolutePath() + backupExt;
            backupFile = new File(backupFileName);
            try {
                // Delete backup if it already exists
                if (removeBackupFile(file)) {
                    // Copy File
                    fis = new FileInputStream(file);
                    fos = new FileOutputStream(backupFile);
                    while ((inOut = fis.read()) != -1) {
                        fos.write(inOut);
                    }
                    retVal = true;
                }
            } catch (Exception e) {
                System.err.println("Failed to copy file : " + file.getAbsolutePath() + " to " + backupFileName);
                retVal = false;
            } finally {
                try {
                    fis.close();
                    fos.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }

        return retVal;
    }

    /**
     * @param file
     * @return
     */
    public synchronized String getBackupFileName(File file) {
        String backupFileName = null;
        if (file != null) {
            backupFileName = file.getAbsolutePath() + backupExt;
        }

        return backupFileName;
    }

    /**
     * @param file
     * @return
     */
    public synchronized boolean removeBackupFile(File file) {
        File backupFile = null;
        String backupFileName = null;
        boolean retVal = false;

        if (file != null) {
            backupFileName = file.getAbsolutePath() + backupExt;
            backupFile = new File(backupFileName);
            try {
                // Delete backup if it already exists
                backupFile.delete();
                retVal = true;
            } catch (Exception e) {
                System.err.println("Failed to delete file : " + file.getAbsolutePath());
                retVal = false;
            }
        }

        return retVal;
    }

    /**
     * @param file
     * @return
     */
    public synchronized boolean restoreBackupFile(File file) {
        File backupFile = null;
        String backupFileName = null;
        boolean retVal = false;

        if (file != null) {
            backupFileName = file.getAbsolutePath() + backupExt;
            backupFile = new File(backupFileName);
            try {
                // Delete backup if it already exists
                file.delete();
                // Rename Backup
                backupFile.renameTo(file);
                retVal = true;
            } catch (Exception e) {
                System.err.println("Failed to rename backup file : " + backupFile.getAbsolutePath() + " to " + file.getAbsolutePath());
                retVal = false;
            }
        }

        return retVal;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public String convertFileSystemSeparators(String fileName) {
        // Define Local Variables
        String retVal = null;
        char fsSeparator = System.getProperty("file.separator").charAt(0);

        //		if (fileName != null) retVal = (fileName.replace('\\',fsSeparator)).replace('/',fsSeparator);
        if (fileName != null) {
            retVal = (fileName.replace('\\', '/'));
        }

        return retVal;

    }

    /**
     *
     * @param fileName
     * @return
     */
    public String convertToUNIXFileSystemSeparators(String fileName) {
        // Define Local Variables
        String retVal = null;
        char fsSeparator = '/';

        retVal = (fileName.replace('\\', fsSeparator)).replace('/', fsSeparator);

        return retVal;

    }

    /**
     *
     * @param name
     * @return
     */
    public URL findFileURLInClasspath(String name) {
        URL fileURL = null;
        try {
            if (name != null) {
                fileURL = FileUtils.class.getResource(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileURL;
    }

    /**
     *
     * @param name
     * @return
     */
    public URI findFileURIInClasspath(String name) {
        URI uri = null;
        try {
            URL url = findFileURLInClasspath(name);
            if (url != null) {
                uri = new URI(url.toExternalForm());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }

    /**
     *
     * @param name
     * @return
     */
    public String findFileInClasspath(String name) {
        String fullPath = null;
        URL url = findFileURLInClasspath(name);
        if (url != null) {
            fullPath = url.getFile();
        }
        return fullPath;
    }

    /**
     *
     * @param filename
     * @return
     */
    public String getFileAsString(String filename) {
        File file = null;
        if (filename != null) {
            file = new File(filename);
        }
        return getFileAsString(file);
    }

    public String getFileAsString(InputStream fis) {
        String contentString = "";
        StringWriter sw = null;
        try {
            sw = new StringWriter();
            int c = 0;
            if (fis != null) {
                while ((c = fis.read()) >= 0) {
                    sw.write(c);
                }
            }
            contentString = sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }

        return contentString;
    }

    /**
     *
     * @param filename
     * @param replaceNewline
     * @param nlReplacement
     * @return
     */
    public String getFileAsString(String filename, boolean replaceNewline, String nlReplacement) {
        File file = null;
        if (filename != null) {
            file = new File(filename);
        }

        return getFileAsString(file, replaceNewline, nlReplacement);
    }

    /**
     *
     * @param uri
     * @return
     */
    public String getFileAsString(URI uri) {
        File file = null;
        if (uri != null) {
            file = new File(uri);
        }

        return getFileAsString(file);
    }

    /**
     *
     * @param uri
     * @param replaceNewline
     * @param nlReplacement
     * @return
     */
    public String getFileAsString(URI uri, boolean replaceNewline, String nlReplacement) {
        File file = null;
        if (uri != null) {
            file = new File(uri);
        }

        return getFileAsString(file, replaceNewline, nlReplacement);
    }

    /**
     *
     * @param file
     * @return
     */
    public String getFileAsString(File file) {
        String contentString = "";
        FileInputStream fis = null;
        StringWriter sw = null;

        try {
            if (file != null) {
                fis = new FileInputStream(file);
            }
            sw = new StringWriter();
            int c = 0;
            if (fis != null) {
                while ((c = fis.read()) >= 0) {
                    sw.write(c);
                }
            }
            contentString = sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }

        return contentString;
    }

    /**
     *
     * @param file
     * @param replaceNewline
     * @param nlReplacement
     * @return
     */
    public String getFileAsString(File file, boolean replaceNewline, String nlReplacement) {
        String contentString = "";
        FileInputStream fis = null;
        BufferedReader br = null;
        StringBuffer sb = new StringBuffer();

        try {
            if (file != null) {
                fis = new FileInputStream(file);
            }
            if (fis != null) {
                br = new BufferedReader(new InputStreamReader(fis));
            }
            String line = null;
            String newline = "";
            if (replaceNewline && nlReplacement == null) {
                newline = "\n";
            } else if (replaceNewline) {
                newline = nlReplacement;
            }
            if (br != null) {
                while ((line = br.readLine()) != null) {
                    sb.append(line + newline);
                }
            }
            contentString = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
            try {
                fis.close();
            } catch (Exception e) {
            }
        }

        return contentString;
    }

    public InputStream getFileInputStream(String filename) {
        return getClass().getResourceAsStream(filename);
    }

    public void writeStringToFile(File file, String string) {
        FileOutputStream fos = null;

        try {
            if (file != null) {
                fos = new FileOutputStream(file);
            }
            if (fos != null) {
                fos.write(string.getBytes());
                fos.flush();
            }
        } catch (Exception e) {
            myLogger.log(Level.WARNING, "Failed to write string to file ", e);
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     *
     * @param filename
     * @return
     */
    public Properties loadResourcePropertiesFile(String filename) {
        System.out.println(filename);
        Properties prop = new Properties();
        if (filename != null) {
            try {
                InputStream is = getClass().getResourceAsStream(filename);
                try {
                    prop.load(is);
                } finally {
                    is.close();
                }
            } catch (IOException ex) {
                myLogger.log(Level.FINE, null, ex);
            }
        }
        System.out.println(prop);
        return prop;
    }

    public Properties loadPropertiesFile(InputStream is) {
        Properties prop = new java.util.Properties();
        try {
            if (is != null) {
                prop.load(is);
            }
        } catch (IOException ex) {
            myLogger.log(Level.FINE, null, ex);
        }
        return prop;
    }

    /**
     *
     * @param filename
     * @return
     */
    public Properties loadPropertiesFile(String filename) {
        File file = null;
        if (filename != null) {
            file = new File(filename);
        }

        return loadPropertiesFile(file);
    }

    /**
     *
     * @param uri
     * @return
     */
    public Properties loadPropertiesFile(URI uri) {
        File file = null;
        Properties properties = new Properties();
        if (uri != null) {
            file = new File(uri);
        }

        return loadPropertiesFile(file);
    }

    /**
     *
     * @param file
     * @return
     */
    public Properties loadPropertiesFile(File file) {
        Properties properties = new Properties();
        FileInputStream fis = null;

        try {
            if (file != null) {
                fis = new FileInputStream(file);
            }
            if (fis != null) {
                properties.load(fis);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }

        return properties;
    }

    /**
     *
     * @param filename
     * @return
     */
    public Manifest loadManifestFile(String filename) {
        File file = null;
        if (filename != null) {
            file = new File(filename);
        }

        return loadManifestFile(file);
    }

    /**
     *
     * @param uri
     * @return
     */
    public Manifest loadManifestFile(URI uri) {
        File file = null;
        Properties properties = new Properties();
        if (uri != null) {
            file = new File(uri);
        }

        return loadManifestFile(file);
    }

    /**
     *
     * @param file
     * @return
     */
    public Manifest loadManifestFile(File file) {
        Manifest manifest = null;
        FileInputStream fis = null;

        try {
            if (file != null) {
                fis = new FileInputStream(file);
            }
            if (fis != null) {
                manifest = new Manifest(fis);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }

        return manifest;
    }

    public URI getFileURI(File file) {
        URI uri = null;
        if (file != null) {
            try {
                uri = getFileURI(file.getCanonicalPath());
            } catch (Exception ex) {
                Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return uri;
    }

    public URI getFileURI(String filename) {
        URI uri = null;
        if (filename != null) {
            try {
                uri = new URI(getFileURIAsString(filename));
            } catch (Exception ex) {
                Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return uri;
    }

    public String getFileURIAsString(File file) {
        String uri = null;
        if (file != null) {
            String filePath = null;
            try {
                filePath = file.getCanonicalPath();
                uri = getFileURIAsString(filePath);
//                uri = "file:///" + filePath.replace("\\", "/").replace("file:///", "");
            } catch (Exception ex) {
                Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return uri;
    }

    public String getFileURIAsString(String filename) {
//        System.out.println("FileUtils filename : " + filename);
        String uriAsString = null;
        if (filename != null) {
            uriAsString = "file:///" + filename.replace("\\", "/").replace("file:///", "").replace("file://", "").replace("file:/", "");
        }
//        System.out.println("FileUtils : uriAsString = " + uriAsString);
        return uriAsString;
    }
}
