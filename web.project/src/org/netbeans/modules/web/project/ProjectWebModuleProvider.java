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

package org.netbeans.modules.web.project;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class ProjectWebModuleProvider implements WebModuleProvider {
    
    public ProjectWebModuleProvider () {
    }
    
    public WebModule findWebModule (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null && project instanceof WebProject) {
            WebProject wp = (WebProject) project;
            FileObject src = wp.getSourceDirectory ();
            if (src != null && (src.equals (file) || FileUtil.isParentOf (src, file))) {
                return wp.getAPIWebModule();
            }

            FileObject web = wp.getWebModule ().getDocumentBase ();
            if (web != null && (web.equals (file) || FileUtil.isParentOf (web, file))) {
                return wp.getAPIWebModule();
            }

            FileObject build = wp.getWebModule().getBuildDirectory();
            if (build != null && (build.equals (file) || FileUtil.isParentOf (build, file))) {
                return wp.getAPIWebModule();
            }

            FileObject prjdir = wp.getProjectDirectory();
            if (prjdir != null && (prjdir.equals (file) || FileUtil.isParentOf(prjdir, file))) {
                return wp.getAPIWebModule();
            }
        }
        return null;
    }
    
}
