/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.visualweb.project.jsf.services.DataSourceService.class)
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
