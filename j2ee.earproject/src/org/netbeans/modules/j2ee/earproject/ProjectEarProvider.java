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

package org.netbeans.modules.j2ee.earproject;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.application.Application;
//import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory;
//import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class ProjectEarProvider 
//    implements 
//        EarProvider 
{
    
    public ProjectEarProvider () {
    }
    
    public Application findApplication (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null && project instanceof EarProject) {
            EarProject ep = (EarProject) project;
            FileObject src = ep.getSourceDirectory ();
//            //FileObject web = wp.getWebModule ().getDocumentBase ();
//            if (src.equals (file) || /*web.equals (file) || */ FileUtil.isParentOf (src, file) /* || FileUtil.isParentOf (web, file) */) {
//                return ApplicationFactory.createApplication (wp.getWebModule ());
//            }
//            FileObject build = wp.getWebModule().getBuildDirectory();
//            if (build != null) {
//                if (build.equals (file) || FileUtil.isParentOf (build, file)) {
//                    return ApplicationFactory.createApplication (wp.getWebModule ());
//                }
//            }
        }
        return null;
    }
    
}
