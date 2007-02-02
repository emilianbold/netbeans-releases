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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * @author Milos Kleint
 */
public class BlueJSettings {
    public static final String PROP_HOME = "home"; // NOI18N
    
    private static final BlueJSettings INSTANCE = new BlueJSettings();
    
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    protected final Preferences getPreferences() {
        return NbPreferences.forModule(BlueJSettings.class);
    }
    
    protected final String putProperty(String key, String value) {
        String retval = getProperty(key);
        if (value != null) {
            getPreferences().put(key, value);
        } else {
            getPreferences().remove(key);
        }
        support.firePropertyChange(key, retval, value);
        return retval;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    protected final String getProperty(String key) {
        return getPreferences().get(key, null);
    }    
    
    public static BlueJSettings getDefault() {
        return INSTANCE;
    }
    
    public File getHome() {
        String s = getProperty(PROP_HOME);
        return s != null ? new File(s) : null;
    }
    
    public void setHome(File home) {
        putProperty(PROP_HOME, home == null ? null : home.getAbsolutePath());
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
