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

package org.netbeans.modules.websvc.client;

import org.openide.filesystems.FileObject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportProvider;

public class ProjectWebServicesClientSupportProvider implements WebServicesClientSupportProvider {
    
    public ProjectWebServicesClientSupportProvider() {
    }
    
    public WebServicesClientSupport findWebServicesClientSupport(FileObject file) { 
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null) {
            WebServicesClientSupportProvider provider = (WebServicesClientSupportProvider) project.getLookup ().lookup (WebServicesClientSupportProvider.class);
            if (provider != null) {
                return provider.findWebServicesClientSupport (file);
            }
        }
        return null;
	}

}
