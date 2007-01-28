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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ProjectDataSourceServiceImpl.java
 *
 * Created on February 11, 2005, 4:40 PM
 */

package org.netbeans.modules.visualweb.dataconnectivity.project.datasource;

import org.netbeans.modules.visualweb.dataconnectivity.Log;
import org.netbeans.modules.visualweb.project.jsf.services.DataSourceService;
import javax.naming.NamingException;
import org.netbeans.modules.visualweb.api.j2ee.common.RequestedJdbcResource;
import org.netbeans.api.project.Project;

import org.openide.ErrorManager;

import org.openide.nodes.Node;

/**
 * For JSF Project Implementation, new for NB4 support (thresher).
 * Replaces DataSourceCookie and it's implementation.
 *
 * @author jfbrown
 */
public class ProjectDataSourceServiceImpl implements DataSourceService {

    /**
     * Creates a new instance of ProjectDataSourceServiceImpl
     */
    public ProjectDataSourceServiceImpl() {
        Log.err.log( ErrorManager.INFORMATIONAL, "DataSourceService Implementation Ready." ) ; // NOI18N
    }

    public RequestedJdbcResource[] getProjectDataSourceInfo(Project project)
    throws NamingException {
        return ProjectDataSourceTracker.getProjectDataSourceInfo(project);
    }

    public Node getDataSourceReferenceNode( Project project ) {
        return ProjectDataSourceTracker.getNode( project ) ;
    }

    public void addDataSourceListener(Project project, ProjectDataSourceListener listener) {
        ProjectDataSourceTracker.addListener(project, listener);
    }

    public void removeDataSourceListener(Project project, ProjectDataSourceListener listener)
    {
        ProjectDataSourceTracker.removeListener(project, listener);
    }

    public String toString() {
        return "DataSourceService Implementaion" ; // NOI18N
    }

}
