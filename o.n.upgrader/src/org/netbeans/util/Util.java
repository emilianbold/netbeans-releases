/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.util;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

/**
 * Provides utility methods
 *
 * @author Marek Slama
 */

public class Util {
    
    /** Creates a new instance of Utilities */
    private Util() {
    }
    
    /** Tries to set default L&F according to platform. 
     * Uses:
     *   Metal L&F on Linux and Solaris
     *   Windows L&F on Windows
     *   Aqua L&F on Mac OS X
     *   System L&F on other OS
     */
    public static void setDefaultLookAndFeel () {
        String uiClassName;
        if (isWindowsOS()) {
            uiClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"; //NOI18N
        } else if (isMacOSX()) {
            uiClassName = "apple.laf.AquaLookAndFeel"; //NOI18N
        } else if (isLinuxOS() || isSunOS()) {
            uiClassName = "javax.swing.plaf.metal.MetalLookAndFeel"; //NOI18N
        } else {
            uiClassName = UIManager.getSystemLookAndFeelClassName();
        }
        if (uiClassName.equals(UIManager.getLookAndFeel().getClass().getName())) {
            //Desired L&F is already set
            return;
        }
        try {
            UIManager.setLookAndFeel(uiClassName);
        } catch (Exception ex) {
            System.err.println("Cannot set L&F " + uiClassName); //NOI18N
            System.err.println("Exception:" + ex.getMessage()); //NOI18N
            ex.printStackTrace();
        }
    }
    
    private static boolean isWindowsOS() {
        return System.getProperty("os.name").startsWith("Windows"); //NOI18N
    }
    
    private static boolean isLinuxOS() {
        return System.getProperty("os.name").startsWith("Lin"); //NOI18N
    }
    
    private static boolean isSunOS() {
        return System.getProperty("os.name").startsWith("Sun"); //NOI18N
    }
    
    private static boolean isMacOSX() {
        return System.getProperty("os.name").startsWith("Mac OS X"); //NOI18N
    }
    
}

