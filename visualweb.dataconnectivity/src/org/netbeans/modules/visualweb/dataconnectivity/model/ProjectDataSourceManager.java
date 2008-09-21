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
 * ProjectDataSourceManager.java
 *
 * Created on June 6, 2006, 1:33 PM
 *
 */

package org.netbeans.modules.visualweb.dataconnectivity.model;

import org.netbeans.modules.visualweb.api.j2ee.common.RequestedJdbcResource;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProject;
import org.netbeans.modules.visualweb.project.jsf.services.DesignTimeDataSourceService;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.naming.NamingException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;

import org.netbeans.modules.visualweb.dataconnectivity.customizers.SqlCommandCustomizer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 *
 * @author marcow
 */
public class ProjectDataSourceManager  {
    private Project project = null;
    private DesignTimeDataSourceService dataSourceService = null;

    /**
     * Creates a new instance of ProjectDataSourceManager
     */
    public ProjectDataSourceManager(DesignBean designBean) {
        DesignContext designContext = designBean.getDesignContext();
        DesignProject designProject = designContext.getProject();
        File nbprojectFile = null;

        try {
            nbprojectFile = designProject.getResourceFile(new URI("nbproject")); // NOI18N
        } catch (URISyntaxException ex) {
            // Should not happen on a static string
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        if (nbprojectFile != null) {
            project = FileOwnerQuery.getOwner(FileUtil.toFileObject(nbprojectFile));
        }

        dataSourceService =  (DesignTimeDataSourceService)Lookup.getDefault().
                lookup(DesignTimeDataSourceService.class);
    }

    // If I the project is known then just construct a new ProjectDataSourceManager with this project
    public ProjectDataSourceManager(Project project) {
        this.project = project;

        dataSourceService =  (DesignTimeDataSourceService)Lookup.getDefault().
                lookup(DesignTimeDataSourceService.class);
    }

    /**
     * Add the datasource meta data to the project
     */
    public boolean addDataSource(DataSourceInfo dsInfo) {
        if ((project != null) && (dataSourceService != null)) {
            RequestedJdbcResource reqResource = new RequestedJdbcResource("jdbc/" + // NOI18N
                    dsInfo.getName(),
                    dsInfo.getDriverClassName(), dsInfo.getUrl(),  dsInfo.getUsername(),
                    dsInfo.getPassword());

            if (!dataSourceService.updateProjectDataSource(project, reqResource)) {
                return false;
            }
            
            // create a JNDI name for resource reference name
            dataSourceService.updateResourceReference(project, reqResource); 
        }


        // save data source for project
//        DataSourceInfoManager.getInstance().addDataSourceInfo(dsInfo);
//        dsHelper.addFullNameDataSource(dsString, ds.getDataSource() ) ;

        return true;
    }

    /**
     * Return true if a legacy project's RequestedJdbcResources are available
     */
    public boolean isRequestedJdbcResourceAvailable() {
        RequestedJdbcResource jdbcResource = null;
        boolean hasResource = false;

        if (dataSourceService.getProjectDataSources(project).size() > 0) {
            hasResource = true;
        }

        return hasResource;
    }
    
    /**
     * Find the RequestedJdbcResource based on the information in the provided datasource info
     * The Url, driver class, user name and password are matched
     */
    public Set<RequestedJdbcResource> findRequestedJdbcResources(DataSourceInfo dsInfo) {
        Set<RequestedJdbcResource> ret = new HashSet<RequestedJdbcResource>();

        if ((project != null) && (dataSourceService != null)) {
            // First Search the project data sources
            Set<RequestedJdbcResource> projectDataSources = dataSourceService.getProjectDataSources(project);
            Iterator<RequestedJdbcResource> projectDataSourcesIterator = projectDataSources.iterator();

            while (projectDataSourcesIterator.hasNext()) {
                RequestedJdbcResource requestedJdbcResource = projectDataSourcesIterator.next();
                
                if (matchDataSourceInfo(requestedJdbcResource, dsInfo)) {
                    ret.add(requestedJdbcResource);
                }
            }
            // Now search the server data sources
            Set<RequestedJdbcResource> serverDataSources = dataSourceService.getServerDataSources(project);
            Iterator<RequestedJdbcResource> serverDataSourcesIterator = serverDataSources.iterator();
            
            while (serverDataSourcesIterator.hasNext()) {
                RequestedJdbcResource requestedJdbcResource = serverDataSourcesIterator.next();
                
                if (matchDataSourceInfo(requestedJdbcResource, dsInfo)) {
                    ret.add(requestedJdbcResource);
                }
            }
        }
        
        return ret;
    }
    
    public RequestedJdbcResource getDataSourceWithName(String name) {
        String projectDsName = "";
        if ((project != null) && (dataSourceService != null)) {
            // First Search the project data sources
            Set<RequestedJdbcResource> projectDataSources = dataSourceService.getProjectDataSources(project);
            Iterator<RequestedJdbcResource> projectDataSourcesIterator = projectDataSources.iterator();
            
            while (projectDataSourcesIterator.hasNext()) {
                RequestedJdbcResource requestedJdbcResource = projectDataSourcesIterator.next();
                String resourceName = requestedJdbcResource.getResourceName();
                
                 // stripDATASOURCE_PREFIX is a hack for JBoss and other application servers due to differences in JNDI string format
                 // for issue 101812
                if (resourceName.startsWith("jdbc")) { // NOI18N
                    projectDsName = resourceName.replaceFirst("jdbc/",""); // NOI18N
                } else if (resourceName.startsWith("java:/jdbc")) {
                    projectDsName = resourceName.replaceFirst("java:/jdbc/",""); // NOI18N
                }
                
                if (projectDsName.equals(name)) {
                    return requestedJdbcResource;
                }
            }
            
            // Now search the server data sources
            Set<RequestedJdbcResource> serverDataSources = dataSourceService.getServerDataSources(project);
            Iterator<RequestedJdbcResource> serverDataSourcesIterator = serverDataSources.iterator();
            
            while (serverDataSourcesIterator.hasNext()) {
                RequestedJdbcResource requestedJdbcResource = serverDataSourcesIterator.next();
                //String projectDsName = requestedJdbcResource.getResourceName().replaceFirst("jdbc/",""); // NOI18N
                projectDsName = requestedJdbcResource.getResourceName();
                
                 // stripDATASOURCE_PREFIX is a hack for JBoss and other application servers due to differences in JNDI string format
                 // for issue 101812
                if (projectDsName.startsWith("jdbc")) { // NOI18N
                    projectDsName = projectDsName.replaceFirst("jdbc/",""); // NOI18N
                } else if (projectDsName.startsWith("java:/jdbc")) {
                    projectDsName = projectDsName.replaceFirst("java:/jdbc/",""); // NOI18N
                }
                
                if (projectDsName.equals(name)) {
                    return requestedJdbcResource;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Match the datasource based on Url, driver class, user name and password
     */
    public boolean matchDataSourceInfo(RequestedJdbcResource requestedJdbcResource, DataSourceInfo dsInfo) {
        String url = requestedJdbcResource.getUrl();
        String driverClassName = requestedJdbcResource.getDriverClassName();
        String username = requestedJdbcResource.getUsername();
        String password = requestedJdbcResource.getPassword();
        
        String url1 = dsInfo.getUrl();
        String driverClassName1 = dsInfo.getDriverClassName();
        String username1 = dsInfo.getUsername();
        String password1 = dsInfo.getPassword();
                
        // hack-around for 6481339/6494241
        if (matchURL(url, url1, true)  &&
                matchString(username, username1, false) && matchString(password, password1, false)) {
            
        // plug-in returns the wrong driver class name for MySQL            
        //        if (matchString(url, url1, true) && matchString(driverClassName, driverClassName1, false) &&
        //                matchString(username, username1, false) && matchString(password, password1, false)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * The strings match if both or null
     */
    private boolean matchString(String str1, String str2, boolean ignoreCase) {
        if ((str1 != null) && (str2 != null)) {
            if (ignoreCase) {
                return str1.trim().equalsIgnoreCase(str2.trim());
            } else {
                return str1.trim().equals(str2.trim());
            }
        } else if ((str1 == null) && (str2 == null)) {
            return true;
        } else {
            return false;
        }
    }
    
     /**
     * Separate match method for URLs due to url problems when jdbc url doesn't contain a port #
     *         jbaker hack around for 6481339/6494241
     */
    private boolean matchURL(String jdbcResourceUrl, String dsInfoUrl, boolean ignoreCase) {
        if (ignoreCase){
            jdbcResourceUrl = jdbcResourceUrl.toLowerCase();
            dsInfoUrl = dsInfoUrl.toLowerCase();
        }
        if (jdbcResourceUrl.equals(dsInfoUrl)){
            return true;
        }
        
        if (jdbcResourceUrl.contains("derby")) {
            String newJdbcResourceUrl = jdbcResourceUrl.substring(0, jdbcResourceUrl.lastIndexOf(":")) + jdbcResourceUrl.substring(jdbcResourceUrl.lastIndexOf("/"));
            if (newJdbcResourceUrl.equals(dsInfoUrl)){
                return true;
            }
        }
        
        int nextIndex = 0;
        if ((jdbcResourceUrl != null) && (dsInfoUrl != null)){
            char[] jdbcResourceUrlChars = jdbcResourceUrl.toCharArray();
            char[] dsInfoUrlChars = dsInfoUrl.toCharArray();
            for(int i = 0; i < jdbcResourceUrlChars.length - 1; i++){
                if ((jdbcResourceUrlChars[i] != dsInfoUrlChars[i]) && jdbcResourceUrlChars[i] == ':'){
                    nextIndex = 1;
                }else if(jdbcResourceUrlChars[i + nextIndex] != dsInfoUrlChars[i]){
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean removeDataSource(DesignProject designProject, DataSourceInfo dsInfo) {
        // Remove the datasource meta data from the project
        throw new UnsupportedOperationException("Missing support in web/project"); // NOI18N
    }
    
    
    public boolean modifyDataSource(DesignProject designProject, DataSourceInfo dsInfo) {
        // Modify the datasource meta data in the project if it exists in the project
        throw new UnsupportedOperationException("Missing support in web/project"); //NOI18N
    }
    
    
}
