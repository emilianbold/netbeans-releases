/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.io.IOException;
import org.netbeans.api.project.ProjectManager;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;

/**
 * Startup and shutdown hooks for projectui module.
 * @author Jesse Glick
 */
public class ProjectUiModule extends ModuleInstall {
    
    public void restored() {
        Hacks.keepCurrentProjectNameUpdated();
    }
    
    public void close() {
        OpenProjectList.shutdown();
        // Just in case something was modified outside the usual customizer dialog:
        try {
            ProjectManager.getDefault().saveAllProjects();
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
}
