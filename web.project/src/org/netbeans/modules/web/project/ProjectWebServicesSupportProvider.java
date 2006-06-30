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
