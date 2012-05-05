/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions.MakeOptionNamedEntity;
import org.netbeans.modules.cnd.makeproject.ui.options.DependencyChecking;
import org.netbeans.modules.cnd.makeproject.ui.options.FixUnresolvedInclude;
import org.netbeans.modules.cnd.makeproject.ui.options.FullFileIndexer;
import org.netbeans.modules.cnd.makeproject.ui.options.RebuildPropsChanged;
import org.netbeans.modules.cnd.makeproject.ui.options.ReuseOutputTab;
import org.netbeans.modules.cnd.makeproject.ui.options.SaveModifiedBeforBuild;
import org.netbeans.modules.cnd.makeproject.ui.options.ShowConfigurationWarning;
import org.netbeans.modules.cnd.makeproject.ui.options.ViewBinaryFiles;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.SharedClassObject;

public class MakeOptions extends SharedClassObject implements PropertyChangeListener {

    private static MakeOptions instance = null;
    // Default make options
    private static final String MAKE_OPTIONS = "makeOptions"; // NOI18N
    private static String defaultMakeOptions = ""; // NOI18N
    // Default Path mode
    private static final String PATH_MODE = "pathMode"; // NOI18N
    //
    private static final String SHOW_PROFILING = "showProfiling"; // NOI18N
    //
    // packaging Defaults
    private static final String DEF_EXE_PERM = "defexeperm"; // NOI18N
    private static final String DEF_FILE_PERM = "deffileperm"; // NOI18N
    private static final String DEF_OWNER = "defowner"; // NOI18N
    private static final String DEF_GROUP = "defgroup"; // NOI18N
    private static final String PREF_APP_LANGUAGE = "prefAppLanguage"; // NOI18N // Prefered language when creating new Application projects
    public static final String USE_BUILD_TRACE = "useBuildTrace"; // NOI18N

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
    public MakeProjectOptions.PathMode getPathMode() {
        MakeProjectOptions.PathMode defaultValue = MakeProjectOptions.PathMode.REL;
        String stringValue = getPreferences().get(PATH_MODE, defaultValue.name());
        // compatibility with previous int-based version, use ordinals
        if (Character.isDigit(stringValue.charAt(0))) {
            int intValue;
            try {
                intValue = Integer.parseInt(stringValue);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
            for (MakeProjectOptions.PathMode pathMode : MakeProjectOptions.PathMode.values()) {
                if (pathMode.ordinal() == intValue) {
                    return pathMode;
                }
            }
            return defaultValue;
        } else {
            return MakeProjectOptions.PathMode.valueOf(stringValue);
        }
    }

    public void setPathMode(MakeProjectOptions.PathMode pathMode) {
        MakeProjectOptions.PathMode oldValue = getPathMode();
        getPreferences().put(PATH_MODE, pathMode.name());
        if (oldValue != pathMode) {
            firePropertyChange(PATH_MODE, oldValue, pathMode);
        }
    }

    // Dependency Checking
    public boolean getDepencyChecking() {
        return getBooleanProperty(findEntity(DependencyChecking.DEPENDENCY_CHECKING));
    }

    public void setDepencyChecking(boolean dependencyChecking) {
        setBooleanProperty(findEntity(DependencyChecking.DEPENDENCY_CHECKING), dependencyChecking);
    }

    // Dependency Checking
    public boolean getRebuildPropChanged() {
        return getBooleanProperty(findEntity(RebuildPropsChanged.REBUILD_PROP_CHANGED));
    }

    public void setRebuildPropChanged(boolean rebuildPropChanged) {
        setBooleanProperty(findEntity(RebuildPropsChanged.REBUILD_PROP_CHANGED), rebuildPropChanged);
    }

    // Display binary files
    public boolean getViewBinaryFiles() {
        return getBooleanProperty(findEntity(ViewBinaryFiles.VIEW_BINARY_FILES));
    }

    public void setViewBinaryFiles(boolean viewBinaryFiles) {
        setBooleanProperty(findEntity(ViewBinaryFiles.VIEW_BINARY_FILES), viewBinaryFiles);
    }

    // Save
    public boolean getSave() {
        return getBooleanProperty(findEntity(SaveModifiedBeforBuild.SAVE));
    }

    public void setSave(boolean save) {
        setBooleanProperty(findEntity(SaveModifiedBeforBuild.SAVE), save);
    }

    // Reuse
    public boolean getReuse() {
        return getBooleanProperty(findEntity(ReuseOutputTab.REUSE));
    }

    public void setReuse(boolean reuse) {
        setBooleanProperty(findEntity(ReuseOutputTab.REUSE), reuse);
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
        return getBooleanProperty(findEntity(ShowConfigurationWarning.SHOW_CONFIGURATION_WARNING));
    }

    public void setShowConfigurationWarning(boolean val) {
        setBooleanProperty(findEntity(ShowConfigurationWarning.SHOW_CONFIGURATION_WARNING), val);
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

    // Is full file indexer available
    public boolean isFullFileIndexer() {
        return getBooleanProperty(findEntity(FullFileIndexer.FULL_FILE_INDEXER));
    }

    public void setFullFileIndexer(boolean value) {
        setBooleanProperty(findEntity(FullFileIndexer.FULL_FILE_INDEXER), value);
    }

    // Fix unresolved include directive by file indexer
    public boolean isFixUnresolvedInclude() {
        return getBooleanProperty(findEntity(FixUnresolvedInclude.FIX_UNRESOLVED_INCLUDE));
    }

    public void setFixUnresolvedInclude(boolean value) {
        setBooleanProperty(findEntity(FixUnresolvedInclude.FIX_UNRESOLVED_INCLUDE), value);
    }

    public boolean getBooleanProperty(MakeOptionNamedEntity entry) {
        if (entry != null) {
            return getPreferences().getBoolean(entry.getName(), entry.isEnabledByDefault());
        }
        return false;
    }

    public void setBooleanProperty(MakeOptionNamedEntity entry, boolean value) {
        if (entry != null) {
            boolean oldValue = getPreferences().getBoolean(entry.getName(), entry.isEnabledByDefault());
            getPreferences().putBoolean(entry.getName(), value);
            if (oldValue != value) {
                firePropertyChange(entry.getName(), oldValue, value);
            }
        }
    }
    
    public MakeOptionNamedEntity findEntity(String name) {
        for(MakeOptionNamedEntity entry : Lookup.getDefault().lookupResult(MakeOptionNamedEntity.class).allInstances()) {
            if (entry.getName().equals(name)) {
                return entry;
            }
        }
        return null;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
    }
}

