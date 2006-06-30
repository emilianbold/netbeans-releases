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

package org.netbeans.bluej.options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Milos Kleint
 */
public class BlueJSettings extends SystemOption {
    public static final String PROP_HOME = "home"; // NOI18N

    private static final long serialVersionUID = -4857548488373437L;

    protected void initialize() {
        super.initialize();
        //TODO try to guess the correct locations..
    }
    
    public String displayName() {
        return NbBundle.getMessage(BlueJSettings.class, "LBL_Settings"); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public static BlueJSettings getDefault() {
        return (BlueJSettings) findObject(BlueJSettings.class, true);
    }
    
    public File getHome() {
        return (File)getProperty(PROP_HOME);
    }
    
    public void setHome(File home) {
        putProperty(PROP_HOME, home, true);
    }    
    
    /**
     * There is a bluej.properties file in the user directory. It countains a row of properties 
     * named bluej.userlibrary.*.location, it's value is the path to the library, * is the number starting from
     * 1. The cycle stops when there is one number missing.
     * the user directory is in various places on each OS. Windows is "bluej" under user.home, on macosx it's "Library/Preferences/org.bluej" under user.home
     * any other platform is ".bluej" under user.home.
     * @return as ant classpath entry.
     */
    public String getUserLibrariesAsClassPath() {
        File userDir = new File(System.getProperty("user.home")); // NOI18N
        File bluejHome = null;
        if (Utilities.isWindows()) {
            bluejHome = new File(userDir, "bluej"); // NOI18N
        } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            bluejHome = new File(userDir, "Library/Preferences/org.bluej"); // NOI18N
        } else {
            bluejHome = new File(userDir, ".bluej"); // NOI18N
        }
        File prop = new File(bluejHome, "bluej.properties"); // NOI18N
        String path = "";
        if (prop.exists()) {
            FileInputStream str = null;
            try {
                str = new FileInputStream(prop);
                Properties properties = new Properties();
                properties.load(str);
                int index = 1;
                while (true) {
                    String propKey = "bluej.userlibrary." + index + ".location"; // NOI18N
                    String value = properties.getProperty(propKey);
                    if (value != null) {
                        path = path + (path.length() == 0 ? "" : ":") + value; // NOI18N
                    } else {
                        //we're done.
                        break;
                    }
                    index = index + 1;
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
                    
        }
        return path;
    }
    
}
