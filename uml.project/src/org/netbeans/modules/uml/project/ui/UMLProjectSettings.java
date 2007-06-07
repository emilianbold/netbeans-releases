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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.project.ui;

import java.io.File;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Storage for some important incrementers and default values
 * @author Mike Frisino
 */
public class UMLProjectSettings {
	
	// TODO - I haven't exactly figured out how this is meant to be used.
	// Craig might know from Jato system option work.
	// I encountered it in the J2SE project code while looking at their
	// wizard. This is providing some of the support for tracking last used
	// project number and stuff like that. But I'm not sure if it is working
	// flawlessly or not.

    private static final String NEW_PROJECT_COUNT = "newProjectCount"; //NOI18N

    private static final String LAST_USED_ARTIFACT_FOLDER = "lastUsedArtifactFolder"; //NOI18N
    
    private static final String LAST_ROSE_FILE_LOCATION = "lastRoseFileLocation"; //NOI18N
    
    public static UMLProjectSettings getDefault () {
        return new UMLProjectSettings();
    }
    
    private static Preferences prefs() {
        return NbPreferences.forModule(UMLProjectSettings.class);
    }
    
    public int getNewProjectCount () {
        return prefs().getInt(NEW_PROJECT_COUNT, 0);
    }

    public String getLastRoseFileLocation() {
        return prefs().get(LAST_ROSE_FILE_LOCATION, "");
    }
    
    public void setNewProjectCount (int count) {
        prefs().putInt(NEW_PROJECT_COUNT, count);
    }
    
    public void setLastRoseFileLocation(String loc) {
       prefs().put(LAST_ROSE_FILE_LOCATION, loc);
    }

    public File getLastUsedArtifactFolder () {
        return new File(prefs().get(LAST_USED_ARTIFACT_FOLDER, System.getProperty("user.home")));
    }

    public void setLastUsedArtifactFolder (File folder) {
        assert folder != null : "Folder can not be null";
        String path = folder.getAbsolutePath();
        prefs().put(LAST_USED_ARTIFACT_FOLDER, path);
    }   

}
