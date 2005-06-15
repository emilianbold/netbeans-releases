/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx;

/**
 * Class used to discover Which JVM is used in order to enable only supported features.
 * @author jfdenise
 */
public class FeaturesDeployer {
    private static Boolean enable;
    
    private static boolean runOnSun() {    
        if(enable == null)  {
            try {
                Class.forName("com.sun.jmx.mbeanserver.JmxMBeanServer");
                enable = Boolean.TRUE;
            }catch(Throwable e) {
                enable = Boolean.FALSE;
            }
        }
                
        return enable.booleanValue();
    }
        
    
    /**
     * Returns if the JVM supports local management features.
     * @return <CODE>boolean</CODE> true if feature is supported
     */
    public static String enableLocal() {
        return runOnSun() ?
            "Actions/Management/org-netbeans-modules-jmx-runtime-EnableLocalManagementAction.instance" :
            null;
    }
    
    /**
     * Returns if the JVM supports local management debug features.
     * @return <CODE>boolean</CODE> true only if JVM supports local management debug features
     */
    public static String debugLocal() {
        return runOnSun() ?
            "Actions/Management/org-netbeans-modules-jmx-runtime-DebugLocalManagementAction.instance" :
            null;
    }
     
    /**
     * Returns if the JVM supports remote management features.
     * @return <CODE>boolean</CODE> true only if JVM supports remote management features.
     */
    public static String enableRemote() {
        return runOnSun() ?
            "Actions/Management/org-netbeans-modules-jmx-runtime-EnableRemoteManagementAction.instance" :
            null;
    }
      
    /**
     * Returns if the JVM supports remote management debug features.
     * @return <CODE>boolean</CODE> true only if JVM supports remote management debug features.
     */
    public static String debugRemote() {
        return runOnSun() ?
            "Actions/Management/org-netbeans-modules-jmx-runtime-DebugRemoteManagementAction.instance" :
            null;
    }
    
     /**
      * Returns if the JVM contains Jconsole (SUN JVM).
      * @return <CODE>boolean</CODE> true only if JVM contains Jconsole (SUN JVM).
      */
     public static String launchJConsole() {
        return runOnSun() ?
            "Actions/Management/org-netbeans-modules-jmx-jconsole-LaunchAction.instance" :
            null;
    }
     
    /**
     * Returns if the JVM supports loading of management configuration files (Sun JVM only).
     * @return <CODE>boolean</CODE> true only if JVM supports loading of management configuration files.
     */
    public static String enableManagementProperties() {
        return runOnSun() ?
            "Management/Templates/management.properties" :
            null;
    }
}
