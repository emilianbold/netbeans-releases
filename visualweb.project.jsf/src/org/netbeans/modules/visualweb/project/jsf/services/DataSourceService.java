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
 * DataSourceService.java
 * Created during the switch to NB4 projects
 *
 * Created on Feb, 2005
 */

package org.netbeans.modules.visualweb.project.jsf.services ;

import javax.naming.NamingException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.api.j2ee.common.RequestedJdbcResource;
import org.openide.nodes.Node;

// XXX Why is this interface defined here, while it is not used here.
// Move it to the place of use!
/**
 * Provides data source services for project(s).
 * @author  jfbrown
 */
public interface DataSourceService {

    /**
     *  For deployment, to gather the JDBC resources used in the project
     */
    public RequestedJdbcResource[] getProjectDataSourceInfo(Project project) throws NamingException;

    /***
     * Used a Project type to display "Data Source References" in the
     * project navigator.
     */
    public Node getDataSourceReferenceNode( Project project ) ;



}
