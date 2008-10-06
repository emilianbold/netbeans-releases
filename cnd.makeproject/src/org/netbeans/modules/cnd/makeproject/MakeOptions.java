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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
    
    // packaging Defaults
    static final String DEF_EXE_PERM = "defexeperm"; // NOI18N
    static final String DEF_FILE_PERM = "deffileperm"; // NOI18N
    static final String DEF_OWNER = "defowner"; // NOI18N
    static final String DEF_GROUP = "defgroup"; // NOI18N
    
    static public MakeOptions getInstance() {
        if (instance == null) {
            instance = SharedClassObject.findObject(MakeOptions.class, true);
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
        return getPreferences().getBoolean(DEPENDENCY_CHECKING, true);
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
    
    
    // Def Exe Perm
    public String getDefExePerm() {
        return getPreferences().get(DEF_EXE_PERM, "755"); // NOI18N
    }
    public void setDefExePerm(String value) {
        String oldValue = getDefExePerm();
        getPreferences().put(DEF_EXE_PERM, value);
        if (!oldValue.equals(value))
            firePropertyChange(DEF_EXE_PERM, oldValue, value);
    }
    
    // Def File Perm
    public String getDefFilePerm() {
        return getPreferences().get(DEF_FILE_PERM, "644"); // NOI18N
    }
    public void setDefFilePerm(String value) {
        String oldValue = getDefFilePerm();
        getPreferences().put(DEF_FILE_PERM, value);
        if (!oldValue.equals(value))
            firePropertyChange(DEF_FILE_PERM, oldValue, value);
    }
    
    // Def Owner Perm
    public String getDefOwner() {
        return getPreferences().get(DEF_OWNER, "root"); // NOI18N
    }
    public void setDefOwner(String value) {
        String oldValue = getDefOwner();
        getPreferences().put(DEF_OWNER, value);
        if (!oldValue.equals(value))
            firePropertyChange(DEF_OWNER, oldValue, value);
    }
    
    // Def Group Perm
    public String getDefGroup() {
        return getPreferences().get(DEF_GROUP, "sys"); // NOI18N
    }
    public void setDefGroup(String value) {
        String oldValue = getDefGroup();
        getPreferences().put(DEF_GROUP, value);
        if (!oldValue.equals(value))
            firePropertyChange(DEF_GROUP, oldValue, value);
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(MakeOptions.class, s);
    }

}

