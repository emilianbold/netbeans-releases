/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.ui.support;

import javax.swing.JFileChooser;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.netbeans.modules.project.uiapi.Utilities;
import org.openide.util.Lookup;

import java.io.File;

/**
 * Support for creating project chooser.
 * @author Petr Hrebejk
 */
public class ProjectChooser {
    
    private ProjectChooser() {}


    /**
     * Returns the folder last used for creating a new project.
     * @return File the folder, never returns null. In the case
     * when the projects folder was not set the home folder is returned.
     */
    public static File getProjectsFolder () {
        return Utilities.getProjectChooserFactory().getProjectsFolder();
    }

    /**
     * Sets the folder last used for creating a new project.
     * @param folder The folder to be set as last used. Must not be null
     */
    public static void setProjectsFolder (File folder) {
        assert folder != null && folder.isDirectory(): "Parameter must be a valid folder."; //NOI18N
        Utilities.getProjectChooserFactory().setProjectsFolder(folder);
    }

    /**
     * Creates a project chooser.
     * @return New instance of JFileChooser which is able to select
     *         project directories.
     */
    public static JFileChooser projectChooser() {
        return Utilities.getProjectChooserFactory().createProjectChooser();
    }
        
}
