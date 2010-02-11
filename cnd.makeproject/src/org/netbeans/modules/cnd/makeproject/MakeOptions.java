/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.SharedClassObject;

public class MakeOptions extends SharedClassObject implements PropertyChangeListener {

    // Display binary files
    public static final String VIEW_BINARY_FILES = "viewBinaryFiles"; // NOI18N
    public static final String[] PathModeNames = new String[]{
        getString("TXT_Auto"),
        getString("TXT_AlwaysRelative"),
        getString("TXT_AlwaysAbsolute"),};

    private static MakeOptions instance = null;
    // Default make options
    private static final String MAKE_OPTIONS = "makeOptions"; // NOI18N
    private static String defaultMakeOptions = ""; // NOI18N
    // Default Path mode
    private static final String PATH_MODE = "pathMode"; // NOI18N
    // Dependency checking
    private static final String DEPENDENCY_CHECKING = "dependencyChecking"; // NOI18N
    // Save
    private static final String SAVE = "save";  // NOI18N
    // Reuse
    private static final String REUSE = "reuse";  // NOI18N
    //
    private static final String SHOW_PROFILING = "showProfiling"; // NOI18N
    //
    private static final String SHOW_CONFIGURATION_WARNING = "showConfigurationWarning"; // NOI18N
    // packaging Defaults
    private static final String DEF_EXE_PERM = "defexeperm"; // NOI18N
    private static final String DEF_FILE_PERM = "deffileperm"; // NOI18N
    private static final String DEF_OWNER = "defowner"; // NOI18N
    private static final String DEF_GROUP = "defgroup"; // NOI18N
    private static final String PREF_APP_LANGUAGE = "prefAppLanguage"; // NOI18N // Prefered language when creating new Application projects

    static {
        
    }

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
        if (!oldValue.equals(value)) {
            firePropertyChange(MAKE_OPTIONS, oldValue, value);
        }
    }

    // Path Mode
    public int getPathMode() {
        return getPreferences().getInt(PATH_MODE, MakeProjectOptions.REL);
    }

    public void setPathMode(int pathMode) {
        int oldValue = getPathMode();
        getPreferences().putInt(PATH_MODE, pathMode);
        if (oldValue != pathMode) {
            firePropertyChange(PATH_MODE, Integer.valueOf(oldValue), Integer.valueOf(pathMode));
        }
    }

    // Dependency Checking
    public boolean getDepencyChecking() {
        return getPreferences().getBoolean(DEPENDENCY_CHECKING, true);
    }

    public void setDepencyChecking(boolean dependencyChecking) {
        boolean oldValue = getDepencyChecking();
        getPreferences().putBoolean(DEPENDENCY_CHECKING, dependencyChecking);
        if (oldValue != dependencyChecking) {
            firePropertyChange(DEPENDENCY_CHECKING, Boolean.valueOf(oldValue), Boolean.valueOf(dependencyChecking));
        }
    }

    // Display binary files
    public boolean getViewBinaryFiles() {
        return getPreferences().getBoolean(VIEW_BINARY_FILES, false);
    }

    public void setViewBinaryFiles(boolean viewBinaryFiles) {
        boolean oldValue = getViewBinaryFiles();
        getPreferences().putBoolean(VIEW_BINARY_FILES, viewBinaryFiles);
        if (oldValue != viewBinaryFiles) {
            firePropertyChange(VIEW_BINARY_FILES, Boolean.valueOf(oldValue), Boolean.valueOf(viewBinaryFiles));
        }
    }

    // Save
    public boolean getSave() {
        return getPreferences().getBoolean(SAVE, true);
    }

    public void setSave(boolean save) {
        boolean oldValue = getSave();
        getPreferences().putBoolean(SAVE, save);
        if (oldValue != save) {
            firePropertyChange(SAVE, Boolean.valueOf(oldValue), Boolean.valueOf(save));
        }
    }

    // Reuse
    public boolean getReuse() {
        return getPreferences().getBoolean(REUSE, true);
    }

    public void setReuse(boolean reuse) {
        boolean oldValue = getReuse();
        getPreferences().putBoolean(REUSE, reuse);
        if (oldValue != reuse) {
            firePropertyChange(REUSE, Boolean.valueOf(oldValue), Boolean.valueOf(reuse));
        }
    }

    // Show Profiling
    public boolean getShowProfiling() {
        return getPreferences().getBoolean(SHOW_PROFILING, true);
    }

    public void setShowProfiling(boolean reuse) {
        boolean oldValue = getShowProfiling();
        getPreferences().putBoolean(SHOW_PROFILING, reuse);
        if (oldValue != reuse) {
            firePropertyChange(SHOW_PROFILING, Boolean.valueOf(oldValue), Boolean.valueOf(reuse));
        }
    }

    // Show Configuration warning
    public boolean getShowConfigurationWarning() {
        return getPreferences().getBoolean(SHOW_CONFIGURATION_WARNING, true);
    }

    public void setShowConfigurationWarning(boolean val) {
        boolean oldValue = getShowConfigurationWarning();
        getPreferences().putBoolean(SHOW_CONFIGURATION_WARNING, val);
        if (oldValue != val) {
            firePropertyChange(SHOW_CONFIGURATION_WARNING, Boolean.valueOf(oldValue), Boolean.valueOf(val));
        }
    }

    // Def Exe Perm
    public String getDefExePerm() {
        return getPreferences().get(DEF_EXE_PERM, "755"); // NOI18N
    }

    public void setDefExePerm(String value) {
        String oldValue = getDefExePerm();
        getPreferences().put(DEF_EXE_PERM, value);
        if (!oldValue.equals(value)) {
            firePropertyChange(DEF_EXE_PERM, oldValue, value);
        }
    }

    // Def File Perm
    public String getDefFilePerm() {
        return getPreferences().get(DEF_FILE_PERM, "644"); // NOI18N
    }

    public void setDefFilePerm(String value) {
        String oldValue = getDefFilePerm();
        getPreferences().put(DEF_FILE_PERM, value);
        if (!oldValue.equals(value)) {
            firePropertyChange(DEF_FILE_PERM, oldValue, value);
        }
    }

    // Def Owner Perm
    public String getDefOwner() {
        return getPreferences().get(DEF_OWNER, "root"); // NOI18N
    }

    public void setDefOwner(String value) {
        String oldValue = getDefOwner();
        getPreferences().put(DEF_OWNER, value);
        if (!oldValue.equals(value)) {
            firePropertyChange(DEF_OWNER, oldValue, value);
        }
    }

    // Def Group Perm
    public String getDefGroup() {
        return getPreferences().get(DEF_GROUP, "bin"); // NOI18N
    }

    public void setDefGroup(String value) {
        String oldValue = getDefGroup();
        getPreferences().put(DEF_GROUP, value);
        if (!oldValue.equals(value)) {
            firePropertyChange(DEF_GROUP, oldValue, value);
        }
    }


    // Prefered language when creating new Application projects
    public String getPrefApplicationLanguage() {
        return getPreferences().get(PREF_APP_LANGUAGE, "C++"); // NOI18N
    }

    public void setPrefApplicationLanguage(String value) {
        String oldValue = getPrefApplicationLanguage();
        getPreferences().put(PREF_APP_LANGUAGE, value);
        if (!oldValue.equals(value)) {
            firePropertyChange(PREF_APP_LANGUAGE, oldValue, value);
        }
    }

    public void propertyChange(PropertyChangeEvent pce) {
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(MakeOptions.class, s);
    }
}

