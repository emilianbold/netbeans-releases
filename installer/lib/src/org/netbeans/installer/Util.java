/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer;

import com.installshield.util.LocalizedStringResolver;
import com.installshield.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.File;
import java.io.FileFilter;
import java.util.StringTokenizer;
import java.util.Vector;

public class Util {
    
    // Operating System
    
    public static boolean isWindowsOS() {
        return System.getProperty("os.name").startsWith("Windows");
    }
    public static boolean isWindowsXP() {
        return System.getProperty("os.name").startsWith("Windows XP");
    }
    public static boolean isWindowsNT() {
        return System.getProperty("os.name").startsWith("Windows NT");
    }
    public static boolean isWindowsME() {
        return System.getProperty("os.name").startsWith("Windows ME");
    }
    public static boolean isWindows2K() {
        return System.getProperty("os.name").startsWith("Windows 2000");
    }
    public static boolean isWindows98() {
        return System.getProperty("os.name").startsWith("Windows 98");
    }
    public static boolean isWindows95() {
        return System.getProperty("os.name").startsWith("Windows 95");
    }
    public static boolean isUnixOS() {
        return isLinuxOS() || isSunOS() || isAixOS() || isHpuxOS() || isIrixOS() || isDigitalOS() || isMacOSX();
    }
    public static boolean isLinuxOS() {
        return System.getProperty("os.name").startsWith("Lin");
    }
    public static boolean isSunOS() {
        return System.getProperty("os.name").startsWith("Sun");
    }
    public static boolean isAixOS() {
        return System.getProperty("os.name").startsWith("AIX");
    }
    public static boolean isHpuxOS() {
        return System.getProperty("os.name").startsWith("HP-UX");
    }
    public static boolean isIrixOS() {
        return System.getProperty("os.name").startsWith("Irix");
    }
    public static boolean isDigitalOS() {
        return System.getProperty("os.name").startsWith("Dig");
    }
    public static boolean isOS2OS() {
        return System.getProperty("os.name").startsWith("OS/2");
    }
    public static boolean isOpenVMSOS() {
        return System.getProperty("os.name").startsWith("Open");
    }
    public static boolean isMacOSX() {
        return System.getProperty("os.name").startsWith("Mac OS X");
    }
    public static boolean isDarwinOS() {
        return System.getProperty("os.name").startsWith("Darwin");
    }
    
    // Product Directories
    
    public static String getTmpDir() {
        return getStringPropertyValue("tmpDir");
    }
    
    public static void setTmpDir(String tmpDir) {
        setStringPropertyValue("tmpDir", tmpDir);
    }
    
    public static String getBackupDir() {
        return getStringPropertyValue("backupDir");
    }
    
    public static void setBackupDir(String backupDir) {
        setStringPropertyValue("backupDir", backupDir);
    }
    
    public static String getNbInstallDir() {
        return getStringPropertyValue("nbInstallDir");
    }
    
    public static void setNbInstallDir(String nbInstallDir) {
        setStringPropertyValue("nbInstallDir", nbInstallDir);
    }
    
    public static Vector getNbInstallList() {
        return (Vector) System.getProperties().get("nbInstallList");
    }
    
    public static void setNbInstallList(Vector nbInstallList) {
        System.getProperties().put("nbInstallList", nbInstallList);
    }
    
    // Installation Attributes
    
    public static boolean isAdmin() {
        return getBooleanPropertyValue("isAdmin");
    }
    
    public static void setAdmin(boolean isAdmin) {
        setBooleanPropertyValue("isAdmin", isAdmin);
    }
    
    public static boolean isSkipAdmin() {
        return getBooleanPropertyValue("skipAdmin");
    }
    
    public static void setSkipAdmin(boolean skipAdmin) {
        setBooleanPropertyValue("skipAdmin", skipAdmin);
    }
    
    public static boolean isContinueInstallation() {
        return getBooleanPropertyValue("continueInstallation");
    }
    
    public static void setContinueInstallation(boolean continueInstallation) {
        setBooleanPropertyValue("continueInstallation", continueInstallation);
    }
    
    public static boolean isAssociateJava() {
        return getBooleanPropertyValue("associateJava");
    }
    
    public static void setAssociateJava(boolean associateJava) {
        setBooleanPropertyValue("associateJava", associateJava);
    }
    
    public static boolean isAssociateNBM() {
        return getBooleanPropertyValue("associateNBM");
    }
    
    public static void setAssociateNBM(boolean associateNBM) {
        setBooleanPropertyValue("associateNBM", associateNBM);
    }
    
    // Installed Product
    
    public static String getProductName() {
        return System.getProperty("productName");
    }
    
    public static void setProductName(String productName) {
        setStringPropertyValue("productName", productName);
    }
    
    ////////////////////////////////////////////////////////////
    public static String getInstalledJdk() {
        return getStringPropertyValue("installedJdk");
    }
    
    public static void setInstalledJdk(String value) {
        setStringPropertyValue("installedJdk", value);
    }
    
    public static boolean isJDKAlreadyInstalled() {
        return getBooleanPropertyValue("jdkAlreadyInstalled");
    }
    
    public static void setJDKAlreadyInstalled(boolean value) {
        setBooleanPropertyValue("jdkAlreadyInstalled", value);
    }
    
    ////////////////////////////////////////////////////////////
    public static String getInstalledJre() {
        return getStringPropertyValue("installedJre");
    }
    
    public static void setInstalledJre(String value) {
        setStringPropertyValue("installedJre", value);
    }
    
    public static boolean isJREAlreadyInstalled() {
        return getBooleanPropertyValue("jreAlreadyInstalled");
    }
    
    public static void setJREAlreadyInstalled(boolean value) {
        setBooleanPropertyValue("jreAlreadyInstalled", value);
    }
    
    ////////////////////////////////////////////////////////////
    public static boolean isBelowRecommendedJDK() {
        return getBooleanPropertyValue("isBelowRecommendedJDK");
    }
    
    public static void setBelowRecommendedJDK(boolean value) {
        setBooleanPropertyValue("isBelowRecommendedJDK", value);
    }
    
    public static String getJVMName() {
        if (isWindowsOS()) {
            return "java.exe";
        } else {
            return "java";
        }
    }
    
    public static String getJ2SEInstallDir() {
        return getStringPropertyValue("j2seInstallDir");
    }
    
    public static void setJ2SEInstallDir(String value) {
        setStringPropertyValue("j2seInstallDir", value);
    }
    
    public static String getJdkHome() {
        return getStringPropertyValue("jdkHome");
    }
    
    public static void setJdkHome(String value) {
        setStringPropertyValue("jdkHome", value);
    }
    
    public static String getCurrentJDKHome() {
        return getStringPropertyValue("currentJDKHome");
    }
    
    public static void setCurrentJDKHome(String value) {
        setStringPropertyValue("currentJDKHome", value);
    }
    
    public static Vector getJdkHomeList() {
        return (Vector) System.getProperties().get("jdkHomeList");
    }
    
    public static void setJdkHomeList(Vector jdkHomeList) {
        System.getProperties().put("jdkHomeList", jdkHomeList);
    }
    
    // JDS
    private static final String PLATFORM_LINUX  = "linuxPlatform";
    private static final String PLATFORM_JDS    = "JDS";
    private static final String PLATFORM_SUSE   = "SuSE";
    private static final String PLATFORM_REDHAT = "RedHat";
    
    public static boolean isSunJDS() {
        return PLATFORM_JDS.equals(getStringPropertyValue(PLATFORM_LINUX));
    }
    
    public static boolean isSuSELinux() {
        return PLATFORM_SUSE.equals(getStringPropertyValue(PLATFORM_LINUX));
    }
    
    public static boolean isRedHatLinux() {
        return PLATFORM_REDHAT.equals(getStringPropertyValue(PLATFORM_LINUX));
    }
    
    public static void setLinuxPlatform() {
        String line = findInFile("/etc/sun-release", "Sun Java Desktop");
        if (line != null) {
            setStringPropertyValue(PLATFORM_LINUX, PLATFORM_JDS);
        }
        else {
            line = findInFile("/etc/SuSE-release", "SuSE");
            if (line != null) {
                setStringPropertyValue(PLATFORM_LINUX, PLATFORM_SUSE);
            }
            else {
                line = findInFile("/etc/redhat-release", "Red Hat");
                if (line != null) {
                    setStringPropertyValue(PLATFORM_LINUX, PLATFORM_REDHAT);
                }
            }
        }
    }
    
    public static String findInFile(String path, String key) {
        File file = new File(path);
        if (file.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(key)) {
                        return line;
                    }
                }
                
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
            finally {
                if (reader != null) {
                    try {reader.close();} catch (Exception ignore) {}
                }
            }
        }
        return null;
    }
    
    public static String getSystemPropertiesFileName(String nbInstallDir) {
        return nbInstallDir + File.separator + "_uninst" + File.separator + "install.properties";
    }
    
    // Utilities
    
    public static void deleteDirectory(File dir) {
        Util.deleteDirectory(dir, null);
    }
    
    public static String getStringPropertyValue(String key) {
        Object obj = System.getProperties().get(key);
        return (obj != null) ? (String) obj : null;
    }
    
    public static String getPreviousStringPropertyValue(String key) {
        Object obj = System.getProperties().get("_previous_"+key);
        return (obj != null) ? (String) obj : null;
    }
    
    public static void setStringPropertyValue(String key, String value) {
        System.getProperties().put(key, value);
    }
    
    /* Return the boolean value from a Boolean object property */
    public static boolean getBooleanPropertyValue(String key) {
        Object obj = System.getProperties().get(key);
        return (obj != null) ? ((Boolean) obj).booleanValue() : false;
    }
    
    public static void setBooleanPropertyValue(String key, boolean value) {
        System.getProperties().put(key, (value)?Boolean.TRUE:Boolean.FALSE);
    }
    
    /*deletes the whole directory with the given filter*/
    public static void deleteDirectory(File dir, FileFilter filter) {
        if(!dir.exists()) {
            return;
        }
        if (!dir.delete()) {
            if (dir.isDirectory()) {
                java.io.File[] list;
                if (filter == null)
                    list = dir.listFiles();
                else
                    list = dir.listFiles(filter);
                for (int i=0; i < list.length ; i++) {
                    deleteDirectory(list[i]);
                }
            }
            dir.delete();
        }
    }
    
    /** returns the size of the specified file in bytes*/
    public static long getFileSize(File filepath) {
        long size = 0;
        if (!filepath.exists()) return size;
        File[] list = filepath.listFiles();
        if ((list == null) || (list.length == 0)) return size;
        for (int i = 0; i<list.length; i++) {
            if (list[i].isDirectory()) {
                size += getFileSize(list[i]);
            }
            else {
                size += list[i].length();
            }
        }
        return size;
    }
    
    /** converts the array to String separated by delimiter */
    public static String arrayToString(Object[] array, String delimiter ) {
        try {
            if (array == null) return null;
            
            StringBuffer buf = new StringBuffer();
            buf.append(array[0]);
            for (int i = 1; i< array.length; i++) {
                buf.append(delimiter);
                buf.append(array[i]);
            }
            return buf.toString();
        } catch (Exception ex) {
            return array.toString();
        }
    }
    
    /** Returns a String holding the stack trace information printed by printStackTrace() */
    public static String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter(500);
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
    
    /** Logs the stack trace information printed by printStackTrace() */
    public static void logStackTrace(Log log, Exception ex) {
        String trace = getStackTrace(ex);
        log.logEvent(log, Log.DBG, trace);
        log.logEvent(log, Log.ERROR, trace);
    }
    
    /** A simple method to copy files. */
    public static void copyFile(File src, File dest) throws Exception {
        try {
            FileInputStream in = new FileInputStream(src);
            FileOutputStream out = new FileOutputStream(dest);
            int c;
            
            while ((c = in.read()) != -1)
                out.write(c);
            
            in.close();
            out.close();
        } catch (FileNotFoundException notFound) {
            throw new Exception("Source or Destination file not found: " + notFound);
        } catch (IOException ioerr) {
            throw new Exception("IO Error copying file " + src.getName());
        }
    }
    
    public static boolean isAboveOrEqualMinimumVersion(String minVersion, String version) {
        if (minVersion == null || minVersion.length() < 3)
            return false;
        if (version == null || version.length() < 3)
            return false;
        Character mv = new Character(minVersion.charAt(0));
        Character v = new Character(version.charAt(0));
        if (v.compareTo(mv)<0)
            return false;
        if (minVersion.charAt(1) != version.charAt(1))
            return false;
        mv = new Character(minVersion.charAt(2));
        v = new Character(version.charAt(2));
        if (v.compareTo(mv)<0)
            return false;
        return true;
    }
    
    /** Check JDK installed by jdkbundle installer. */
    public static boolean checkJdkHome(String jdkHome) {
        File jreDir = new File(jdkHome,File.separator + "jre");
        File jvmJREFile = new File(jreDir, File.separator + "bin" +
                          File.separator + getJVMName());
        File jvmFile = new File(jdkHome, File.separator + "bin" +
                       File.separator + getJVMName());
                                                                                                                                         
        if (!jvmFile.exists() || !jvmJREFile.exists()) {
            return false;
        }
                                                                                                                                         
        RunCommand runCommand = new RunCommand();
        runCommand.execute(jvmFile.getAbsolutePath()+" -version");
        runCommand.waitFor();
                                                                                                                                         
        String line = runCommand.getErrorLine();
                                                                                                                                         
        if (line != null) {
            StringTokenizer st = new StringTokenizer(line.trim());
            String version="";
            while (st.hasMoreTokens()) {
                version=st.nextToken();
            }
            String jdkVersion = LocalizedStringResolver.resolve("org.netbeans.installer.Bundle","JDK.version");
            if (version.equals("\"" + jdkVersion + "\"")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    /** Check public JRE installed by jdkbundle installer.
     * Used only on Windows where public JRE is in different directory. */
    public static boolean checkJreHome(String jreHome) {
        File jvmFile = new File(jreHome, File.separator + "bin" +
                       File.separator + getJVMName());
                                                                                                                                         
        if (!jvmFile.exists()) {
            return false;
        }
                                                                                                                                         
        RunCommand runCommand = new RunCommand();
        runCommand.execute(jvmFile.getAbsolutePath()+" -version");
        runCommand.waitFor();
                                                                                                                                         
        String line = runCommand.getErrorLine();
                                                                                                                                         
        if (line != null) {
            StringTokenizer st = new StringTokenizer(line.trim());
            String version="";
            while (st.hasMoreTokens()) {
                version=st.nextToken();
            }
            String jreVersion = LocalizedStringResolver.resolve("org.netbeans.installer.Bundle","JRE.version");
            if (version.equals("\"" + jreVersion + "\"")){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    /** Delete given file/folder completely. It is called recursively when necessary.
     * @file - name of file/folder to be deleted
     */
    public static void deleteCompletely (File file, Log log) {
        deleteCompletely(file,true, log);
    }
    
    /** Delete given file/folder completely. It is called recursively when necessary.
     * @file - name of file/folder to be deleted
     * @firstCall - should be true
     */
    private static void deleteCompletely (File file, boolean firstCall, Log log) {
        if (file.isDirectory()) {
            //Delete content of folder
            File [] fileArr = file.listFiles();
            for (int i = 0; i < fileArr.length; i++) {
                if (fileArr[i].isDirectory()) {
                    deleteCompletely(fileArr[i],false, log);
                }
                log.logEvent(Util.class, Log.DBG,"Delete file: " + fileArr[i].getPath());
                if (fileArr[i].exists() && !fileArr[i].delete()) {
                    log.logEvent(Util.class, Log.DBG,"Cannot delete file: " + fileArr[i].getPath());
                }
            }
        }
        if (firstCall) {
            log.logEvent(Util.class, Log.DBG,"Delete file: " + file.getPath());
            if (file.exists() && !file.delete()) {
                log.logEvent(Util.class, Log.DBG,"Cannot delete file: " + file.getPath());
            }
        }
    }
                                                                                                                                         
}
