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

package org.netbeans.modules.j2me.cdc.project.ui;

import org.openide.util.NbBundle;
import java.io.File;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Misnamed storage of information application to the new cdcproject wizard.
 */
public class CDCFoldersListSettings {

    private static Preferences prefs=NbPreferences.forModule(CDCFoldersListSettings.class);
    
    private static final long serialVersionUID = 2386225041150479082L;
    private static final String NEW_PROJECT_COUNT = "newProjectCount"; //NOI18N
    private static final String NEW_APP_COUNT = "newApplicationCount";  //NOI18N
    private static final String NEW_LIB_COUNT = "newLibraryCount"; //NOI18N
    private static final String LAST_USED_CP_FOLDER = "lastUsedClassPathFolder";    //NOI18N
    private static final String LAST_USED_ARTIFACT_FOLDER = "lastUsedArtifactFolder"; //NOI18N
    

    
    public static String displayName() {
        return NbBundle.getMessage(CDCFoldersListSettings.class, "TXT_CDCProjectFolderList");
    }

    public static int getNewProjectCount () {
        return prefs.getInt (NEW_PROJECT_COUNT,0);
    }

    public static void setNewProjectCount (int count) {
        prefs.putInt(NEW_PROJECT_COUNT, count);
    }
    
    public static int getNewApplicationCount () {
        return prefs.getInt (NEW_APP_COUNT,0);
    }
    
    public static void setNewApplicationCount (int count) {
        prefs.putInt(NEW_APP_COUNT, count);
    }
    
    public static int getNewLibraryCount () {
        return prefs.getInt(NEW_LIB_COUNT,0);
    }
    
    public static void setNewLibraryCount (int count) {
        prefs.putInt(NEW_LIB_COUNT, count);
    }

    public static File getLastUsedClassPathFolder () {
        String lucpr = prefs.get(LAST_USED_CP_FOLDER,System.getProperty("user.home"));       
        return new File (lucpr);
    }

    public static void setLastUsedClassPathFolder (File folder) {
        assert folder != null : "ClassPath root can not be null";
        String path = folder.getAbsolutePath();
        prefs.put(LAST_USED_CP_FOLDER, path);
    }

    public static File getLastUsedArtifactFolder () {
        String folder = prefs.get(LAST_USED_ARTIFACT_FOLDER,System.getProperty("user.home"));        
        return new File (folder);
    }

    public static void setLastUsedArtifactFolder (File folder) {
        assert folder != null : "Folder can not be null";
        String path = folder.getAbsolutePath();
        prefs.put (LAST_USED_ARTIFACT_FOLDER, path);
    }   
}
