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

package org.netbeans.modules.ruby.railsprojects.ui;

import org.openide.util.NbBundle;

import java.io.File;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Misnamed storage of information application to the new j2seproject wizard.
 */
public class FoldersListSettings {
    private static final FoldersListSettings INSTANCE = new FoldersListSettings();
    private static final String NEW_PROJECT_COUNT = "newProjectCount"; //NOI18N
    private static final String LOGICAL_VIEW = "logicalView"; //NOI18N

    private static final String NEW_APP_COUNT = "newApplicationCount";  //NOI18N

    private static final String LAST_USED_ARTIFACT_FOLDER = "lastUsedArtifactFolder"; //NOI18N
    

    public static FoldersListSettings getDefault () {
        return INSTANCE;
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(FoldersListSettings.class);
    }
    
    public String displayName() {
        return NbBundle.getMessage(FoldersListSettings.class, "TXT_RailsProjectFolderList");
    }

    public boolean getLogicalView() {
        return getPreferences().getBoolean(LOGICAL_VIEW, true);
    }

    public void setLogicalView(boolean logical) {
        getPreferences().putBoolean(LOGICAL_VIEW, logical);
    }
    
    public int getNewProjectCount () {
        return getPreferences().getInt(NEW_PROJECT_COUNT, 0);
    }

    public void setNewProjectCount (int count) {
        getPreferences().putInt(NEW_PROJECT_COUNT, count);
    }
    
    public int getNewApplicationCount () {
        return getPreferences().getInt(NEW_APP_COUNT, 0);
    }
    
    public void setNewApplicationCount (int count) {
        getPreferences().putInt(NEW_APP_COUNT, count);
    }
    
    public File getLastUsedArtifactFolder () {
        return new File (getPreferences().get(LAST_USED_ARTIFACT_FOLDER, System.getProperty("user.home")));
    }

    public void setLastUsedArtifactFolder (File folder) {
        assert folder != null : "Folder can not be null";
        String path = folder.getAbsolutePath();
        getPreferences().put(LAST_USED_ARTIFACT_FOLDER, path);
    }   
}
