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

package org.netbeans.modules.j2ee.ejbjarproject;

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
 *  for this ejb project.
 *
 * @author Peter Williams
 */
public class ProjectWebServicesSupportProvider implements WebServicesSupportProvider/*, WebServicesClientSupportProvider*/ {

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

            FileObject metaInf = ejbproject.getEjbModule().getMetaInf();
            if(metaInf != null && (metaInf.equals (file) || FileUtil.isParentOf (metaInf, file))) {
                return ejbproject.getAPIWebServicesSupport();
            }

            FileObject build = ejbproject.getEjbModule().getBuildDirectory();
            if (build != null && (build.equals (file) || FileUtil.isParentOf (build, file))) {
                return ejbproject.getAPIWebServicesSupport();
            }
        }
        return null;
    }

    public WebServicesClientSupport findWebServicesClientSupport (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null && project instanceof EjbJarProject) {
            EjbJarProject ejbproject = (EjbJarProject) project;

            // !PW FileUtil.toFile(file) can return null if the FileObject passed in is abstract,
            // e.g. from exploring a WAR file for example.
            if(FileUtil.toFile(ejbproject.getProjectDirectory()).equals(FileUtil.toFile(file))) {
                return ejbproject.getAPIWebServicesClientSupport();
            }

            // TODO: ma154696: This is just quick fix for multiple source roots, is it OK?
            FileObject[] sourceRoots = ejbproject.getSourceRoots().getRoots();
            for (int i = 0; i < sourceRoots.length; i++) {
                FileObject src = sourceRoots[i];
                if (src != null && (src.equals (file) || FileUtil.isParentOf (src, file))) {
                    return ejbproject.getAPIWebServicesClientSupport();
                }
            }

            FileObject metaInf = ejbproject.getEjbModule().getMetaInf();
            if(metaInf != null && (metaInf.equals (file) || FileUtil.isParentOf (metaInf, file))) {
                return ejbproject.getAPIWebServicesClientSupport();
            }

            FileObject build = ejbproject.getEjbModule().getBuildDirectory();
            if (build != null && (build.equals (file) || FileUtil.isParentOf (build, file))) {
                return ejbproject.getAPIWebServicesClientSupport();
            }
        }
        return null;
    }
}
