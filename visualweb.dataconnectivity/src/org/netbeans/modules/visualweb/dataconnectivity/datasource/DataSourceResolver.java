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
 *//*
 * DataSourceResolver.java
 *
 * Created on September 6, 2006, 10:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.dataconnectivity.datasource;

import java.io.IOException;
import org.netbeans.modules.visualweb.api.j2ee.common.RequestedJdbcResource;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfo;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfoListener;
import org.netbeans.modules.visualweb.dataconnectivity.model.ProjectDataSourceManager;
import org.netbeans.modules.visualweb.project.jsf.services.DesignTimeDataSourceService;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.dataconnectivity.model.ProjectDataSourceManager;
import org.netbeans.modules.visualweb.dataconnectivity.naming.DatabaseSettingsImporter;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourceTracker;
import org.netbeans.modules.visualweb.dataconnectivity.sql.DesignTimeDataSourceHelper;
import org.netbeans.modules.visualweb.dataconnectivity.ui.DataSourceCreationNotSupported;
import org.netbeans.modules.visualweb.dataconnectivity.utils.ImportDataSource;
import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.ModelSet;
import org.netbeans.modules.visualweb.insync.ModelSetsListener;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 *
 * @author John Baker
 */
public class DataSourceResolver implements DataSourceInfoListener, Runnable {
    private static final String DRIVER_CLASS_NET = "org.apache.derby.jdbc.ClientDriver"; // NOI18N    
    private static final String DATASOURCE_PREFIX = "java:comp/env/"; // NOI18N
    private static DataSourceResolver dataSourceResolver;
    private String dataSourceInfo = null;
    protected WaitForModelingListener modelingListener = new WaitForModelingListener();
    private Project project;
    private ProgressHandle handle = null;
    private TopComponent topComponent;
    private RequestProcessor.Task task = null;
    private final RequestProcessor WAIT_FOR_MODELING_RP = new RequestProcessor("DataSourceResolver.WAIT_FOR_MODELING_RP"); //NOI18N    
    private Model[] modelSets = null;
    
    /** Creates a new instance of DataSourceResolver */
    private DataSourceResolver() {
    }

    public static DataSourceResolver getInstance() {
        if (dataSourceResolver == null) {
            dataSourceResolver = new DataSourceResolver();
        }
        return dataSourceResolver;
    }



    public void dataSourceInfoModified(org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfoEvent modelEvent) {
        dataSourceInfo = modelEvent.getDataSourceInfoId();
    }

    public void datasourceInfoAdded(org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfoEvent modelEvent) {
        dataSourceInfo = modelEvent.getDataSourceInfoId();
    }

    public void dataSourceInfoRemoved(org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfoEvent modelEvent) {
        ; // no-op
    }

    // if a project data source does not have a corresponding connetion then return true
    public boolean isDataSourceMissing(Project project, String prjDsName) {
        DesignTimeDataSourceService dataSourceService = Lookup.getDefault().lookup(DesignTimeDataSourceService.class);
        Set<RequestedJdbcResource> problemDatasources = dataSourceService.getBrokenDatasources(project);

        boolean missing = false;
        Iterator it = problemDatasources.iterator();
        while (it.hasNext()) {
            RequestedJdbcResource reqRes = (RequestedJdbcResource) it.next();
            if (("jdbc/" + prjDsName).equals(reqRes.getJndiName())) { //NOI18N
                missing = true;
                break; // data source match made, stop checking
            }
        }

        return missing;
    }
    
    public Set<RequestedJdbcResource> getProjectDataSources(Project project) {
        DesignTimeDataSourceService dataSourceService = Lookup.getDefault().lookup(DesignTimeDataSourceService.class);
        return dataSourceService.getProjectDataSources(project);
    }

    public boolean isDataSourceUnique(Project currentProj, String dsName, String url) {        
        String[] dynamicDataSources = ProjectDataSourceTracker.getDynamicDataSources(currentProj);
                     
        for (String name : dynamicDataSources) {
            if (name.equals((DATASOURCE_PREFIX + "/jdbc/" + dsName))) {  // NOI18N
                if (!getDataSourceUrl(dsName).equals(url)) {
                    return false;
                }
            }
        }
        
        return true;
    }

     private String getDataSourceUrl(String dsName) {
        String url = null;
        DataSourceInfo dsInfo = null;

        List<DataSourceInfo> dataSourcesInfo = DatabaseSettingsImporter.getInstance().getDataSourcesInfo();
        Iterator it = dataSourcesInfo.iterator();
        while (it.hasNext()) {
            dsInfo = (DataSourceInfo) it.next();
            if (dsName.equals(dsInfo.getName())) {
                url = dsInfo.getUrl();
            }
        }

        return url;
    }
     
    public void updateSettings() {
        doCopying();
        registerConnections();
    }
    
    public void update(Project currentProj) {
        updateProject(currentProj);                
    }


    // Find a matching driver registered with the IDE
    public JDBCDriver findMatchingDriver(String driverClass) {
        int i = 0;
        JDBCDriver[] newDrivers;
        newDrivers = JDBCDriverManager.getDefault().getDrivers();       

        for (i = 0; i < newDrivers.length; i++) {
            if (newDrivers[i].getClassName().equals(driverClass)) {
                return newDrivers[i];
            } else if (driverClass.equals("org.gjt.mm.mysql.Driver")) { // NOI18N
                if (newDrivers[i].getClassName().equals("com.mysql.jdbc.Driver"))  { // NOI18N
                    return newDrivers[i];
                }
            }
        }

        return null;
    }

    private boolean updateProject(Project project, DataSourceInfo dsInfo) {
        boolean needAdd = false;
        ProjectDataSourceManager projectDataSourceManager = new ProjectDataSourceManager(project);

        // if project's datasource hasn't been added, then add it
        if (projectDataSourceManager.getDataSourceWithName(dsInfo.getName()) == null) {
            projectDataSourceManager.addDataSource(dsInfo);
            needAdd = true;
        } else {
            needAdd = false;
        }
        return needAdd;
    }

    private void doCopying() {
        try {
            ImportDataSource.prepareCopy();
        } catch (IOException ioe) {
            Logger.getLogger("copy").info("Migrating user settings failed " + ioe); //NOI18N
        }
    }

    private void registerConnections() {
        JDBCDriver[] drvsArray = JDBCDriverManager.getDefault().getDrivers(DRIVER_CLASS_NET);
        if (drvsArray.length > 0) {
            DatabaseSettingsImporter.getInstance().locateAndRegisterDrivers();
            DatabaseSettingsImporter.getInstance().locateAndRegisterConnections(false);
        }
    }

    private void updateProject(Project project) {
        // Update Project's datasources
        try {
            new DesignTimeDataSourceHelper().updateDataSource(project);
            checkConnections(project);
        } catch (NamingException ne) {
            Logger.getLogger("copy").info("Migrating user settings failed " + ne); //NOI18N
        }
    }        
    
    // Check if any database connections needed by the project are missing
    private void checkConnections(Project project) {
        DesignTimeDataSourceService dataSourceService = Lookup.getDefault().lookup(DesignTimeDataSourceService.class);
        Set<RequestedJdbcResource> problemDatasources = dataSourceService.getBrokenDatasources(project);
        if (!problemDatasources.isEmpty()) {
            ImportDataSource.showAlert();
        }
    }
    
    /**
     * Post an information dialog to inform the user that the target application server does not support the automatic creation of data sources
     */
    public synchronized void postUnsupportedDataSourceCreationDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DataSourceCreationNotSupported noDataSourceDialog = new DataSourceCreationNotSupported();
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(DataSourceCreationNotSupported.class, "MSG_DataSourceNotSupported"), NotifyDescriptor.WARNING_MESSAGE); //NOI18N
                DialogDisplayer.getDefault().notify(nd);
                noDataSourceDialog.setVisible(true);
            }
        });
    }

    public void modelProjectForDataSources(Project currentProj) {
        project = currentProj;
        ModelSet.addModelSetsListener(modelingListener);
        topComponent = TopComponent.getRegistry().getActivated();
        topComponent.setCursor(Utilities.createProgressCursor(topComponent));
        String progressBarLabel = org.openide.util.NbBundle.getMessage(DataSourceResolver.class, "LBL_ProgressBar"); //NOI18N
        
        try {
            // model project 
            FacesModelSet modelSet = FacesModelSet.startModeling(project);

            if (modelSet == null) {
                handle = ProgressHandleFactory.createHandle(progressBarLabel);
                handle.start();
                handle.switchToIndeterminate();
            }

            // If modeling has been completed then terminate the progress cursor and update the project
            if (modelSet != null) {
                if (handle != null) {
                    handle.finish();
                }

                ModelSet.removeModelSetsListener(modelingListener);
                ProjectDataSourceTracker.refreshDataSourceReferences(project);
                update(project);
            }
        } finally {
            topComponent.setCursor(null);
        }
    }
    
    /*
     * Schedule update task
     */
    public synchronized void updateTask() {
        if (task == null) {
            task = WAIT_FOR_MODELING_RP.create(this);
        }
        task.schedule(50);
    }

    /*
     * Update data sources in the project
     */
    public synchronized void run() {        
        // make sure the sources are modeled
        for (Model mModel : modelSets) {
            String filenameExt = mModel.getFile().getExt();
            if (filenameExt.equals("java")) {
                FacesModelSet.getFacesModelIfAvailable(mModel.getFile());
            }
        }

        // Refresh data sources in a project
        ProjectDataSourceTracker.refreshDataSourceReferences(project);
        // Update the resource references in the project
        update(project);
        
        // Terminate the progress cursor when done
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (handle != null) {
                    handle.finish();
                }
            }
        });
        
        // clean up
        modelSets = null;
        ModelSet.removeModelSetsListener(modelingListener);
    }
    
    public boolean isDatasourceCreationSupported(Project project) {
        DesignTimeDataSourceService dataSourceService = Lookup.getDefault().lookup(DesignTimeDataSourceService.class);
        return dataSourceService.isDatasourceCreationSupported(project);        
    }

    public class WaitForModelingListener implements ModelSetsListener {        
        
        /*---------- ModelSetsListener------------*/

        public void modelSetAdded(ModelSet modelSet) {
            try {
                // update data sources, if necessary
                modelSets = modelSet.getModels();
                updateTask();                
            } finally {
                task.waitFinished();
                topComponent.setCursor(null);
            }
        }

        public void modelSetRemoved(ModelSet modelSet) {
            // not implemented
        }

        public void modelProjectChanged() {
            // not implemented
        }

        /*---------- end of interface implements ------------*/        
    }            
}
