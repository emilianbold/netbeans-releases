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

package org.netbeans.modules.j2ee.ejbjarproject;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class ProjectEjbJarProvider implements EjbJarProvider {
    
    public ProjectEjbJarProvider () {
    }
    
    public EjbJar findEjbJar (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null && project instanceof EjbJarProject) {
            EjbJarProject ep = (EjbJarProject) project;
            FileObject src = ep.getSourceDirectory ();
            if (src != null && (src.equals (file) || FileUtil.isParentOf (src, file))) {
                return ep.getAPIEjbJar();
            }

            FileObject build = ep.getEjbModule().getBuildDirectory();
            if (build != null && (build.equals (file) || FileUtil.isParentOf (build, file))) {
                return ep.getAPIEjbJar();
            }

            FileObject prjdir = ep.getProjectDirectory();
            if (prjdir != null && (prjdir.equals (file) || FileUtil.isParentOf(prjdir, file))) {
                return ep.getAPIEjbJar();
            }
        }
        return null;
    }
    
}
