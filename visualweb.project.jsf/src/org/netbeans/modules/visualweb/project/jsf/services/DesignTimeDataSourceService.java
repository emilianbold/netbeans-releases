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
 * DesignTimeDataSourceService.java
 *
 * Created on June 2, 2006, 5:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.project.jsf.services;

import org.netbeans.modules.visualweb.api.j2ee.common.RequestedJdbcResource;
import java.util.Set;
import org.netbeans.api.project.Project;

/**
 * This interface is used between the Design Time and the actual project
 * infrastructure on disk.
 *
 * @author marcow
 */
public interface DesignTimeDataSourceService {

    /**
     * This method is used from the design time to store the data source parameters in the
     * project.
     *
     * @return false on any error condition. true otherwise, even when that data source
     * exists already.
     */
    public boolean updateProjectDataSource(Project p, RequestedJdbcResource req);

    public Set<RequestedJdbcResource> getProjectDataSources(Project p);

    public Set<RequestedJdbcResource> getServerDataSources(Project p);
    
    public Set<RequestedJdbcResource> getBrokenDatasources(Project p);
    
    public boolean updateResourceReference(Project project, RequestedJdbcResource req);
    
    public boolean isDatasourceCreationSupported(Project project);
}
