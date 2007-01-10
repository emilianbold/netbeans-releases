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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
                Class.forName("com.sun.jmx.mbeanserver.JmxMBeanServer"); // NOI18N
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
    public static String enableMM() {
        return runOnSun() ?
            "Actions/Management/org-netbeans-modules-jmx-runtime-EnableMMAction.instance" : // NOI18N
            null;
    }
    
    /**
     * Returns if the JVM supports local management debug features.
     * @return <CODE>boolean</CODE> true only if JVM supports local management debug features
     */
    public static String debugMM() {
        return runOnSun() ?
            "Actions/Management/org-netbeans-modules-jmx-runtime-DebugMMAction.instance" : // NOI18N
            null;
    }
    
     /**
      * Returns if the JVM contains Jconsole (SUN JVM).
      * @return <CODE>boolean</CODE> true only if JVM contains Jconsole (SUN JVM).
      */
     public static String launchJConsole() {
        return runOnSun() ?
            "Actions/Management/org-netbeans-modules-jmx-jconsole-LaunchAction.instance" : // NOI18N
            null;
    }
     
    /**
     * Returns if the JVM supports loading of management configuration files (Sun JVM only).
     * @return <CODE>boolean</CODE> true only if JVM supports loading of management configuration files.
     */
    public static String enableManagementProperties() {
        return runOnSun() ?
            "Management/Templates/management.properties" : // NOI18N
            null;
    }
}
