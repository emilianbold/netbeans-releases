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

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;

import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;


/**
 *
 * @author marcow
 */
public class ProjectDataSourceManager  {
    private Project project = null;
    private DesignTimeDataSourceService dataSourceService = null;
    private static Project currentProj;

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
                    dsInfo.getDriverClassName(), dsInfo.getUrl(), null, dsInfo.getUsername(),
                    dsInfo.getPassword(), null);

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
        if ((project != null) && (dataSourceService != null)) {
            // First Search the project data sources
            Set<RequestedJdbcResource> projectDataSources = dataSourceService.getProjectDataSources(project);
            Iterator<RequestedJdbcResource> projectDataSourcesIterator = projectDataSources.iterator();
            
            while (projectDataSourcesIterator.hasNext()) {
                RequestedJdbcResource requestedJdbcResource = projectDataSourcesIterator.next();
                String resourceName = requestedJdbcResource.getResourceName();
                String projectDsName = "";
                
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
                String projectDsName = requestedJdbcResource.getResourceName();
                
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
