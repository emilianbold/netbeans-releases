/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
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

