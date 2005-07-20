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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportProvider;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportProvider;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportFactory;


/** Provider object to locate web services support or web service client support
 *  for this web project.
 *
 * @author Peter Williams
 */
public class ProjectWebServicesSupportProvider implements WebServicesSupportProvider, WebServicesClientSupportProvider {
	
	public ProjectWebServicesSupportProvider () {
	}
	
	public WebServicesSupport findWebServicesSupport (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null && project instanceof WebProject) {
            WebProject wp = (WebProject) project;

			// !PW FileUtil.toFile(file) can return null if the FileObject passed in is abstract,
			// e.g. from exploring a WAR file for example.
			if(FileUtil.toFile(wp.getProjectDirectory()).equals(FileUtil.toFile(file))) {
                return wp.getAPIWebServicesSupport();
			}

            FileObject src [] = wp.getSourceRoots().getRoots();
            for (int i = 0; i < src.length; i++) {
                if (src[i].equals (file) || FileUtil.isParentOf (src[i], file)) {
                    return wp.getAPIWebServicesSupport();
                }
            }

            FileObject web = wp.getWebModule ().getDocumentBase ();
            if (web != null && (web.equals (file) || FileUtil.isParentOf (web, file))) {
                return wp.getAPIWebServicesSupport();
            }

            FileObject build = wp.getWebModule().getBuildDirectory();
            if (build != null && (build.equals (file) || FileUtil.isParentOf (build, file))) {
                return wp.getAPIWebServicesSupport();
            }
        }
        return null;
	}
	
	public WebServicesClientSupport findWebServicesClientSupport (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null && project instanceof WebProject) {
            WebProject wp = (WebProject) project;
			
			// !PW FileUtil.toFile(file) can return null if the FileObject passed in is abstract,
			// e.g. from exploring a WAR file for example.
			if(FileUtil.toFile(wp.getProjectDirectory()).equals(FileUtil.toFile(file))) {
                return wp.getAPIWebServicesClientSupport();
			}
			
            FileObject src [] = wp.getSourceRoots().getRoots();
            for (int i = 0; i < src.length; i++) {
                if (src[i].equals (file) || FileUtil.isParentOf (src[i], file)) {
                    return wp.getAPIWebServicesClientSupport();
                }
            }

            FileObject web = wp.getWebModule ().getDocumentBase();
            if (web != null && (web.equals (file) || FileUtil.isParentOf (web, file))) {
                return wp.getAPIWebServicesClientSupport();
            }

            FileObject build = wp.getWebModule().getBuildDirectory();
            if (build != null && (build.equals (file) || FileUtil.isParentOf (build, file))) {
                return wp.getAPIWebServicesClientSupport();
            }
        }
        return null;
	}
	
}
