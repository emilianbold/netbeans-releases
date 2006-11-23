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


/*
 * SystemInfo.java
 *
 * Created on November 1, 2001, 6:33 PM
 */

package org.netbeans.xtest.pe.xmlbeans;

// for getting hostname
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author  mb115822
 */
public class SystemInfo extends XMLBean {

    /** Creates new SystemInfo */
    public SystemInfo() {
        getCurrentSystemInfo();
    }

    // attributes
    public String   xmlat_host;
    public String   xmlat_osArch;
    public String   xmlat_osName;
    public String   xmlat_osVersion;
    public String   xmlat_javaVendor;
    public String   xmlat_javaVersion;
    public String   xmlat_userLanguage;
    public String   xmlat_userRegion;
    
    /** Holds value of property osArch. */
    private String osArch;
    
    // bussiness methods
    
    public static String getCurrentHost() {
        // if xtest.machine set use it, otherwise try to get it. Used in test4u.
        String host = System.getProperty("xtest.machine");
        if(host == null) {
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException uhe) {
                // we cannot get the hostname
                host = "UnknownHost";
            }
        }
        return host;
    }    
    
    public static String getCurrentOsArch() {
        return System.getProperty("os.arch");
    }
    
    public static String getCurrentOsName() {
        return System.getProperty("os.name");
    }
    
    public static String getCurrentOsVersion() {
        return System.getProperty("os.version");
    }
    
    public static String getCurrentJavaVendor() {
        return System.getProperty("java.vendor");
    }
    
    public static String getCurrentJavaVersion() {
        return System.getProperty("java.version");
    }
    
    public static String getCurrentUserLanguage() {
        return System.getProperty("user.language");
    }
    
    public static String getCurrentUserRegion() {
        return System.getProperty("user.region");
    }
    
    public static String getCurrentPlatform() {
        return "OS: "+getCurrentOsName()+" "+getCurrentOsVersion()+" "+getCurrentOsArch()+", JDK: "+getCurrentJavaVersion()+" "+getCurrentJavaVendor();
    }        
    
    
    public void getCurrentSystemInfo() {      
        xmlat_host = getCurrentHost();
        xmlat_osArch = getCurrentOsArch();
        xmlat_osName = getCurrentOsName();
        xmlat_osVersion = getCurrentOsVersion();
        xmlat_javaVendor = getCurrentJavaVendor();
        xmlat_javaVersion = getCurrentJavaVersion();
        xmlat_userLanguage = getCurrentUserLanguage();
        xmlat_userRegion = getCurrentUserRegion();
    }
    
    
    public void readSystemInfo(SystemInfo si) {
        xmlat_osArch = si.xmlat_osArch;
        xmlat_osName = si.xmlat_osName;
        xmlat_osVersion = si.xmlat_osVersion;
        xmlat_javaVendor = si.xmlat_javaVendor;
        xmlat_javaVersion = si.xmlat_javaVersion;
        xmlat_host = si.xmlat_host;
        xmlat_userLanguage = si.xmlat_userLanguage;
        xmlat_userRegion = si.xmlat_userRegion;
    }

    /** Getter for property host.
     * @return Value of property host.
     */
    public String getHost() {
        return xmlat_host;
    }
    
    /** Setter for property host.
     * @param host New value of property host.
     */
    public void setHost(String host) {
        xmlat_host = host;
    }
    
    /** Getter for property osArch.
     * @return Value of property osArch.
     */
    public String getOsArch() {
        return xmlat_osArch;
    }
    
    /** Setter for property osArch.
     * @param osArch New value of property osArch.
     */
    public void setOsArch(String osArch) {
        xmlat_osArch = osArch;
    }
    
    /** Getter for property osName.
     * @return Value of property osName.
     */
    public String getOsName() {
        return xmlat_osName;
    }
    
    /** Setter for property osName.
     * @param osName New value of property osName.
     */
    public void setOsName(String osName) {
        xmlat_osName = osName;
    }
    
    /** Getter for property osVersion.
     * @return Value of property osVersion.
     */
    public String getOsVersion() {
        return xmlat_osVersion;
    }
    
    /** Setter for property osVersion.
     * @param osVersion New value of property osVersion.
     */
    public void setOsVersion(String osVersion) {
        xmlat_osVersion = osVersion;
    }
    
    /** Getter for property javaVendor.
     * @return Value of property javaVendor.
     */
    public String getJavaVendor() {
        return xmlat_javaVendor;
    }
    
    /** Setter for property javaVendor.
     * @param javaVendor New value of property javaVendor.
     */
    public void setJavaVendor(String javaVendor) {
        xmlat_javaVendor = javaVendor;
    }
    
    /** Getter for property javaVersion.
     * @return Value of property javaVersion.
     */
    public String getJavaVersion() {
        return xmlat_javaVersion;
    }
    
    /** Setter for property javaVersion.
     * @param javaVersion New value of property javaVersion.
     */
    public void setJavaVersion(String javaVersion) {
        xmlat_javaVersion = javaVersion;
    }
    
    /** Getter for property userLanguage.
     * @return Value of property userLanguage.
     */
    public String getUserLanguage() {
        return xmlat_userLanguage;
    }
    
    /** Setter for property userLanguage.
     * @param userLanguage New value of property userLanguage.
     */
    public void setUserLanguage(String userLanguage) {
        xmlat_userLanguage = userLanguage;
    }
    
    /** Getter for property userRegion.
     * @return Value of property userRegion.
     */
    public String getUserRegion() {
        return xmlat_userRegion;
    }
    
    /** Setter for property userRegion.
     * @param userRegion New value of property userRegion.
     */
    public void setUserRegion(String userRegion) {
        xmlat_userRegion = userRegion;
    }
    
}
