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

/**
 * Support for creating project chooser.
 * @author Petr Hrebejk
 */
public class ProjectChooser {
    
    private ProjectChooser() {}
    
    /**
     * Creates a project chooser.
     * @return New instance of JFileChooser which is able to select
     *         project directories.
     */
    public static JFileChooser projectChooser() {
        return Utilities.getProjectChooserFactory().createProjectChooser();
    }
        
}
