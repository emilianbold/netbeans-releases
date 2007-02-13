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

package org.netbeans.spi.project.ui.support;

import java.io.File;
import javax.swing.JFileChooser;
import org.netbeans.modules.project.uiapi.Utilities;

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
