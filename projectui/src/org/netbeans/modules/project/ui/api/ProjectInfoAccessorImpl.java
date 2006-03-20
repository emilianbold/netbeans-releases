/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui.api;

import java.net.URL;
import javax.swing.Icon;

import org.netbeans.modules.project.ui.ProjectInfoAccessor;

/**
 * Helper class to create instances of class with package private constructor
 * @author Milan Kubec
 */
final class ProjectInfoAccessorImpl extends ProjectInfoAccessor {
    
    public UnloadedProjectInformation getProjectInfo(String name, Icon icon, URL url) {
        return new UnloadedProjectInformation(name, icon, url);
    }
    
}
