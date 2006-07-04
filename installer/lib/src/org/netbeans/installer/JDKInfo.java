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

package org.netbeans.installer;

import java.io.File;
import java.util.StringTokenizer;

import com.installshield.util.Log;

public class JDKInfo {
    public static final int INVALID = 0;
    public static int VALID_JDK = 1;
    public static int VALID_JRE = 2;
    
    private Log log;
    private String home;
    private String version;
    private int type = INVALID;
    
    
    public JDKInfo(Log log) {
        this.log = log;
    }
    
    public JDKInfo(Log log, String home) {
        this(log);
        this.home = home;
    }
    
    public JDKInfo(Log log, String home, String version) {
        this(log, home);
        this.version = version;
    }
    
    public String getHome() {
        return home;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setJDKType() {
        type = VALID_JDK;
    }
    
    public void setJREType() {
        type = VALID_JRE;
    }
    
    public boolean isJDKType() {
        return (type == VALID_JDK);
    }
    
    public boolean isJREType() {
        return (type == VALID_JRE);
    }
    
    public boolean isInvalid() {
        return (type == INVALID);
    }

    /**Checks whether current jvmHome is a valid JDK installation with the required minimum version.
     * If isJREHomeAllowed is true, then it  checks  whether current jvmHome is a valid JRE or JDK
     * installation with the required minimum version.
     * Returns JDKInfo of jvmHome.
     **/
    public static JDKInfo getCurrentJDKInfo(Log log, boolean isJREHomeAllowed) {
        log.logEvent(log, Log.DBG, "Checking Current JDK: " + System.getProperty("java.home"));
        String jdkVersion = System.getProperty("java.version");
        String jdkHome = System.getProperty("java.home");
        File jHome = new File(jdkHome);
        // If the "java.home" property ends with jre then get the parent directory
        if(jHome.getName().equals("jre"))
            jdkHome = jHome.getParentFile().getAbsolutePath();
        
        JDKInfo info = new JDKInfo(log, jdkHome, jdkVersion);
        if (!isJVMVersionValid(jdkVersion)) {
            // default type is invalid
            return info;
        }
        
        info.checkInfo(isJREHomeAllowed);
        return info;
    }
    
    /** Checks whether jdkHome is a valid JDK installation with the required minimum version.
     * Exception: On Mac OS X and asbundle installer we now accept only JDK 1.4.2.
     */
    public static boolean checkJdkHome(Log log, String jdkHome) {
        log.logEvent(log, Log.DBG, "Checking JDK: " + jdkHome);
        JDKInfo info = new JDKInfo(log, jdkHome);
        info.checkInfo(false);
        return info.isJDKType();
    }
    
    /** Checks whether jvmHome is a valid JDK installation with the required minimum version.
     * If isJREHomeAllowed is true, then it  checks  whether jvmHome is a valid JRE or JDK
     * installation with the required minimum version.
     * Returns JDKInfo of jvmHome.
     */
    private int checkInfo(boolean isJREHomeAllowed){
        log.logEvent(log, Log.DBG,"Checking: " + home);
        
        type = INVALID;
        
        try{
            /*
            File jreDir = new File(jvmHome,File.separator+"jre");
            log.logEvent(log, Log.DBG,"Checking: " + jreDir);
            if ((!jreDir.exists()) && (isJREHomeAllowed)) {
                log.logEvent(log, Log.DBG,"Checking as JRE: " + jvmHome);
                jreDir = new File(jvmHome);
                jvmInfo.setJREType(); // say it is a valid jre
            }
            if(jreDir.exists()) {
             */
            
            File jreDir = null;
            
            if (!Util.isMacOSX()) {
                jreDir = new File(home, File.separator+"jre");
                log.logEvent(log, Log.DBG, "Checking: " + jreDir);
                if ((!jreDir.exists()) && (isJREHomeAllowed)) {
                    log.logEvent(log, Log.DBG,"Checking as JRE: " + home);
                    jreDir = new File(home);
                    type = VALID_JRE;
                }
            }
            
            if (Util.isMacOSX() || jreDir.exists()) {
                log.logEvent(log, Log.DBG, "Exists: " + jreDir);
                String jvm = Util.getJVMName();
                File jvmFile = new File(home+File.separator+"bin"+File.separator+jvm);
                if (!jvmFile.exists()) {
                    log.logEvent(log, Log.ERROR, "Cannot find JDK file - " + jvmFile.getAbsolutePath());
                    return INVALID;
                }
                
                RunCommand runCommand = new RunCommand();
                String [] cmdArr = new String[2];
                cmdArr[0] = jvmFile.getAbsolutePath();
                cmdArr[1] = "-version";
                runCommand.execute(cmdArr);
                runCommand.waitFor();
                
                //Look for line starting with "java version"
                String line = "", s = "";
                while (s != null) {
                    s = runCommand.getErrorLine();
                    log.logEvent(log, Log.DBG, "Java version line: '" + s.trim() + "'");
                    if (s.startsWith("java version")) {
                        line = s;
                        break;
                    }
                }
                
                if (line.length() == 0) {
                    log.logEvent(log, Log.ERROR, "Cannot parse java version.");
                    return INVALID;
                }
                StringTokenizer st = new StringTokenizer(line.trim());
                String _version="";
                while (st.hasMoreTokens()) {
                    _version = st.nextToken();
                }
                this.version = _version;
                
                if (isJVMVersionValid(version)) {
                    type = VALID_JDK;
                }
            }
        } catch (NullPointerException npe) {
            log.logEvent(log, Log.DBG, "NPE: " + npe);
            npe.printStackTrace();
        } catch (SecurityException secx) {
            log.logEvent(log, Log.DBG, "SecurityException: " + secx);
            secx.printStackTrace();
        } catch (Exception ex) {
            log.logEvent(log, Log.DBG, ex);
            ex.printStackTrace();
        }
        
        return type;
    }
    
    /** This method determines whether the JVM version is valid or not.
     *  This method must always be updated to determine the correct jdk
     *  based on the product needs.
     *  @boolean - Whether the JVM verison is valid or not
     */
    public static boolean isJVMVersionValid(String version) {
        JDKVersion jdkVersion = new JDKVersion(version);
        return !jdkVersion.isBelowMinimumJDK();
    }
    
}
