/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.project.ui;

import java.io.File;
import java.util.Map;

import org.openide.options.SystemOption;
import org.openide.util.NbBundle;

public class FoldersListSettings extends SystemOption {

    static final long serialVersionUID = -4905094097265543014L;
    
    private static final String NEW_PROJECT_COUNT = "newProjectCount"; //NOI18N
    
    private static final String NEW_APP_COUNT = "newApplicationCount";  //NOI18N
    
    private static final String NEW_LIB_COUNT = "newLibraryCount"; //NOI18N

    private static final String LAST_USED_CP_FOLDER = "lastUsedClassPathFolder";    //NOI18N

    private static final String LAST_USED_ARTIFACT_FOLDER = "lastUsedArtifactFolder"; //NOI18N

    private static final String LAST_USED_SOURCE_ROOT_FOLDER = "lastUsedSourceRootFolder";   //NOI18N

    private static final String SHOW_AGAIN_BROKEN_REF_ALERT = "showAgainBrokenRefAlert"; //NOI18N
    
    private static final String LAST_USED_CHOOSER_LOCATIONS = "lastUsedChooserLocations"; //NOI18N

    public static FoldersListSettings getDefault () {
        return (FoldersListSettings) SystemOption.findObject (FoldersListSettings.class, true);
    }
    
    public String displayName() {
        return NbBundle.getMessage(FoldersListSettings.class, "TXT_WebProjectFolderList");
    }

    public int getNewProjectCount () {
        Integer value = (Integer) getProperty (NEW_PROJECT_COUNT);
        return value == null ? 0 : value.intValue();
    }

    public void setNewProjectCount (int count) {
        this.putProperty(NEW_PROJECT_COUNT, new Integer(count),true);
    }
    
    public int getNewApplicationCount () {
        Integer value = (Integer) getProperty (NEW_APP_COUNT);
        return value == null ? 0 : value.intValue();
    }
    
    public void setNewApplicationCount (int count) {
        this.putProperty(NEW_APP_COUNT, new Integer(count),true);
    }
    
    public int getNewLibraryCount () {
        Integer value = (Integer) getProperty (NEW_LIB_COUNT);
        return value == null ? 0 : value.intValue();
    }
    
    public void setNewLibraryCount (int count) {
        this.putProperty(NEW_LIB_COUNT, new Integer(count),true);
    }

    public File getLastUsedClassPathFolder () {
        String lucpr = (String) this.getProperty (LAST_USED_CP_FOLDER);
        if (lucpr == null) {
            lucpr = System.getProperty("user.home");    //NOI18N
        }
        return new File (lucpr);
    }

    public void setLastUsedClassPathFolder (File folder) {
        assert folder != null : "ClassPath root can not be null";
        String path = folder.getAbsolutePath();
        this.putProperty(LAST_USED_CP_FOLDER, path, true);
    }

    public File getLastUsedArtifactFolder () {
        String folder = (String) this.getProperty (LAST_USED_ARTIFACT_FOLDER);
        if (folder == null) {
            folder = System.getProperty("user.home");    //NOI18N
        }
        return new File (folder);
    }

    public void setLastUsedArtifactFolder (File folder) {
        assert folder != null : "Folder can not be null";
        String path = folder.getAbsolutePath();
        this.putProperty (LAST_USED_ARTIFACT_FOLDER, path, true);
    }

    public File getLastUsedSourceRootFolder () {
        String folder = (String) this.getProperty (LAST_USED_SOURCE_ROOT_FOLDER);
        if (folder == null) {
            folder = System.getProperty("user.home");    //NOI18N
        }
        return new File (folder);
    }

    public void setLastUsedSourceRootFolder (File folder) {
        assert folder != null : "Folder can not be null";
        String path = folder.getAbsolutePath();
        this.putProperty (LAST_USED_SOURCE_ROOT_FOLDER, path, true);
    }
    
    public boolean isShowAgainBrokenRefAlert() {
        Boolean b = (Boolean)getProperty(SHOW_AGAIN_BROKEN_REF_ALERT);
        return b == null ? true : b.booleanValue();
    }
    
    public void setShowAgainBrokenRefAlert(boolean again) {
        this.putProperty(SHOW_AGAIN_BROKEN_REF_ALERT, Boolean.valueOf(again), true);
    }

    public Map getLastUsedChooserLocations() {
        return (Map) this.getProperty(LAST_USED_CHOOSER_LOCATIONS);
    }

    public void setLastUsedChooserLocations(Map map) {
        this.putProperty(LAST_USED_CHOOSER_LOCATIONS, map, true);
    }
}
