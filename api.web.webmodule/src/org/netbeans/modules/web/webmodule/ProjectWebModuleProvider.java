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

package org.netbeans.modules.web.webmodule;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;

public class ProjectWebModuleProvider implements WebModuleProvider {
    
    public ProjectWebModuleProvider () {
    }
    
    public org.netbeans.modules.web.api.webmodule.WebModule findWebModule (org.openide.filesystems.FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null) {
            WebModuleProvider provider = (WebModuleProvider) project.getLookup ().lookup (WebModuleProvider.class);
            if (provider != null) {
                return provider.findWebModule (file);
            }
        }
        return null;
    }
}
