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

import org.openide.options.SystemOption;
import org.openide.util.NbBundle;

import java.io.File;

/**
 * Storage for some important incrementers and default values
 * @author Mike Frisino
 */
public class UMLProjectSettings extends SystemOption {
	
	// TODO - I haven't exactly figured out how this is meant to be used.
	// Craig might know from Jato system option work.
	// I encountered it in the J2SE project code while looking at their
	// wizard. This is providing some of the support for tracking last used
	// project number and stuff like that. But I'm not sure if it is working
	// flawlessly or not.

    private static final long serialVersionUID = 2386225041150479082L;

    private static final String NEW_PROJECT_COUNT = "newProjectCount"; //NOI18N

    private static final String LAST_USED_ARTIFACT_FOLDER = "lastUsedArtifactFolder"; //NOI18N
    
    private static final String LAST_ROSE_FILE_LOCATION = "lastRoseFileLocation"; //NOI18N
    
    public static UMLProjectSettings getDefault () {
        return (UMLProjectSettings) SystemOption.findObject (UMLProjectSettings.class, true);
    }
    
    public String displayName() {
        return NbBundle.getMessage(UMLProjectSettings.class, "TXT_UMLProjectSettings");
    }

    public int getNewProjectCount () {
        Integer value = (Integer) getProperty (NEW_PROJECT_COUNT);
        return value == null ? 0 : value.intValue();
    }

    public String getLastRoseFileLocation()
    {
       String retVal = "";
       if(getProperty(LAST_ROSE_FILE_LOCATION) != null)
       {
          retVal = (String)getProperty(LAST_ROSE_FILE_LOCATION);
       }
       return retVal;
    }
    
    public void setNewProjectCount (int count) {
        this.putProperty(NEW_PROJECT_COUNT, new Integer(count),true);
    }
    
    public void setLastRoseFileLocation(String loc)
    {
       putProperty(LAST_ROSE_FILE_LOCATION, loc, true);
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

}
