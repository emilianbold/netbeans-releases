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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportProvider;
import org.netbeans.modules.websvc.api.webservices.WebServicesClientSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesClientSupportProvider;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportFactory;


/** Provider object to locate web services support or web service client support
 *  for this ejb project.
 *
 * @author Peter Williams
 */
public class ProjectWebServicesSupportProvider implements WebServicesSupportProvider{ //, WebServicesClientSupportProvider {
	
	public ProjectWebServicesSupportProvider () {
	}
	
	public WebServicesSupport findWebServicesSupport (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null && project instanceof EjbJarProject) {
            EjbJarProject ejbproject = (EjbJarProject) project;

	    // !PW FileUtil.toFile(file) can return null if the FileObject passed in is abstract,
	    // e.g. from exploring a WAR file for example.
	    if(FileUtil.toFile(ejbproject.getProjectDirectory()).equals(FileUtil.toFile(file))) {
                return ejbproject.getAPIWebServicesSupport();
	    }

            // TODO: ma154696: This is just quick fix for multiple source roots, is it OK?
            FileObject[] sourceRoots = ejbproject.getSourceRoots().getRoots();
            for (int i = 0; i < sourceRoots.length; i++) {
                FileObject src = sourceRoots[i];
                if (src != null && (src.equals (file) || FileUtil.isParentOf (src, file))) {
                    return ejbproject.getAPIWebServicesSupport();
                }
            }
            
            /* FIX-ME: do we need this?
            FileObject web = wp.getWebModule ().getDocumentBase ();
            if (web != null && (web.equals (file) || FileUtil.isParentOf (web, file))) {
                return wp.getAPIWebServicesSupport();
            }
            */
            FileObject build = ejbproject.getEjbModule().getBuildDirectory();
            if (build != null && (build.equals (file) || FileUtil.isParentOf (build, file))) {
                return ejbproject.getAPIWebServicesSupport();
            }
        }
        return null;
	}
        
	/*  FIX-ME: web service client not yet implemented in EJB module
	public WebServicesClientSupport findWebServicesClientSupport (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null && project instanceof EjbJarProject) {
            EjbJarProject ejbproject = (EjbJarProject) project;
			
	    // !PW FileUtil.toFile(file) can return null if the FileObject passed in is abstract,
	    // e.g. from exploring a WAR file for example.
	    if(FileUtil.toFile(ejbproject.getProjectDirectory()).equals(FileUtil.toFile(file))) {
                return ejbproject.getAPIWebServicesClientSupport();
	    }
			
            FileObject src = ejbproject.getSourceDirectory();
            if (src != null && (src.equals (file) || FileUtil.isParentOf (src, file))) {
                return ejbproject.getAPIWebServicesClientSupport();
            }
            FileObject web = wp.getWebModule ().getDocumentBase();
            if (web != null && (web.equals (file) || FileUtil.isParentOf (web, file))) {
                return wp.getAPIWebServicesClientSupport();
            }

            FileObject build = ejbproject.getEjbModule().getBuildDirectory();
            if (build != null && (build.equals (file) || FileUtil.isParentOf (build, file))) {
                return ejbproject.getAPIWebServicesClientSupport();
            }
        }
        return null;
	}
	*/
}
