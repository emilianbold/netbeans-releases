/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.SharedClassObject;

public class MakeOptions extends SharedClassObject implements PropertyChangeListener {
    static private MakeOptions instance = null;
    
    // Default make options
    static final String MAKE_OPTIONS = "makeOptions"; // NOI18N
    static private String defaultMakeOptions = ""; // NOI18N
    
    // Platform
    static final String PLATFORM = "platform"; // NOI18N
    
    // Default Path mode
    public static final int PATH_REL_OR_ABS = 0;
    public static final int PATH_REL = 1;
    public static final int PATH_ABS = 2;
    public static String[] PathModeNames = new String[] {
        getString("TXT_Auto"),
        getString("TXT_AlwaysRelative"),
        getString("TXT_AlwaysAbsolute"),
    };
    static final String PATH_MODE = "pathMode"; // NOI18N
    
    // Dependency checking
    static final String DEPENDENCY_CHECKING = "dependencyChecking"; // NOI18N
    
    // Save
    static final String SAVE = "save";  // NOI18N
    
    // Reuse
    static final String REUSE = "reuse";  // NOI18N
    
    static public MakeOptions getInstance() {
        if (instance == null) {
            instance = (MakeOptions) SharedClassObject.findObject(MakeOptions.class, true);
        }
        return instance;
    }
    
    public static void setDefaultMakeOptions(String makeOptions) {
        defaultMakeOptions = makeOptions;
    }
    
    public static String getDefaultMakeOptions() {
        return defaultMakeOptions;
    }    
    
    public MakeOptions() {
        super();
        addPropertyChangeListener(this);
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(MakeOptions.class);
    }
    
    // Make Options
    public String getMakeOptions() {
        return getPreferences().get(MAKE_OPTIONS, getDefaultMakeOptions());
        }
    public void setMakeOptions(String value) {
        String oldValue = getMakeOptions();
        getPreferences().put(MAKE_OPTIONS, value);
        if (!oldValue.equals(value))
            firePropertyChange(MAKE_OPTIONS, oldValue, value);
    }
    
    // Platform
    public int getPlatform() {
        return getPreferences().getInt(PLATFORM, Platform.getDefaultPlatform());
        }
    public void setPlatform(int value) {
        int oldValue = getPlatform();
        getPreferences().putInt(PLATFORM, value);
        if (oldValue != value)
            firePropertyChange(PLATFORM, "" + oldValue, "" + value); // NOI18N
    }
    
    // Path Mode
    public int getPathMode() {
        return getPreferences().getInt(PATH_MODE, PATH_REL);
    }
    public void setPathMode(int pathMode) {
        int oldValue = getPathMode();
        getPreferences().putInt(PATH_MODE, pathMode);
        if (oldValue != pathMode)
            firePropertyChange(PATH_MODE, new Integer(oldValue), new Integer(pathMode));
    }
    
    // Dependency Checking
    public boolean getDepencyChecking() {
        return getPreferences().getBoolean(DEPENDENCY_CHECKING, false);
    }
    public void setDepencyChecking(boolean dependencyChecking) {
        boolean oldValue = getDepencyChecking();
        getPreferences().putBoolean(DEPENDENCY_CHECKING, dependencyChecking);
        if (oldValue != dependencyChecking)
            firePropertyChange(DEPENDENCY_CHECKING, new Boolean(oldValue), new Boolean(dependencyChecking));
    }
    
    // Save
    public boolean getSave() {
        return getPreferences().getBoolean(SAVE, true);
    }
    public void setSave(boolean save) {
        boolean oldValue = getSave();
        getPreferences().putBoolean(SAVE, save);
        if (oldValue != save)
            firePropertyChange(SAVE, new Boolean(oldValue), new Boolean(save));
    }
    
    // Reuse
    public boolean getReuse() {
        return getPreferences().getBoolean(REUSE, true);
    }
    public void setReuse(boolean reuse) {
        boolean oldValue = getReuse();
        getPreferences().putBoolean(REUSE, reuse);
        if (oldValue != reuse)
            firePropertyChange(REUSE, new Boolean(oldValue), new Boolean(reuse));
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(MakeOptions.class, s);
    }

}

