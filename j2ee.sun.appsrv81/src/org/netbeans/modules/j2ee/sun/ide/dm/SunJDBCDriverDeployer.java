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

package org.netbeans.modules.j2ee.sun.ide.dm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.common.DatasourceHelper;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.netbeans.modules.j2ee.sun.ide.j2ee.DeploymentManagerProperties;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ProgressEventSupport;
import org.netbeans.modules.j2ee.sun.ide.j2ee.Status;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Nitya Doraisamy
 */
public class SunJDBCDriverDeployer implements JDBCDriverDeployer {

    private DeploymentManager dm;
    private SunDeploymentManager sunDm;
    
    public SunJDBCDriverDeployer(DeploymentManager dm) {
        this.dm = dm;
        this.sunDm = (SunDeploymentManager)this.dm;
    }

    public boolean supportsDeployJDBCDrivers(Target target) {
        boolean supported = true;
        if(! this.sunDm.isLocal()){
            supported = false;
        }
        return supported;
    }

    public ProgressObject deployJDBCDrivers(Target target, Set<Datasource> datasources) {
        return new JDBCDriversProgressObject(datasources);
    }

    private class JDBCDriversProgressObject implements ProgressObject, Runnable {
        
        private final ProgressEventSupport eventSupport;
        private final Set<Datasource> datasources;
        
        
        public JDBCDriversProgressObject(Set<Datasource> datasources) {
            eventSupport = new ProgressEventSupport(SunJDBCDriverDeployer.this);
            this.datasources = datasources;
            String msg = NbBundle.getMessage(SunJDBCDriverDeployer.class, "MSG_CheckMissingDrivers");
            eventSupport.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE, CommandType.DISTRIBUTE, msg, StateType.RUNNING));
            RequestProcessor.getDefault().post(this);
        }
    
        public void run() {
            List jdbcDriverURLs = getJdbcDrivers();
            // deploy the driers if needed
            if (jdbcDriverURLs.size() > 0) {
                for(int i=0; i<jdbcDriverURLs.size(); i++){
                    URL jarUrl = (URL)jdbcDriverURLs.get(i);
                    try {
                        File libsDir = getJDBCDriversLocation();
                        File toJar = new File(libsDir, new File(jarUrl.toURI()).getName());
                        try {
                            BufferedInputStream is = new BufferedInputStream(jarUrl.openStream());
                            try {
                                String msg = NbBundle.getMessage(SunJDBCDriverDeployer.class, "MSG_DeployDriver", toJar.getPath());
                                eventSupport.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE, CommandType.DISTRIBUTE, msg, StateType.RUNNING));
                                BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(toJar));
                                try {
                                    FileUtil.copy(is, os);
                                } finally {
                                    os.close();
                                }
                            } finally {
                                is.close();
                            }
                        } catch (IOException e) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                            String msg = NbBundle.getMessage(SunJDBCDriverDeployer.class, "ERR_DeployDriver", toJar.getPath(), libsDir.getPath());
                            eventSupport.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE, CommandType.DISTRIBUTE, msg, StateType.FAILED));
                            return;
                        }
                    } catch (URISyntaxException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        return;
                    }
                } //for
                sunDm.setRestartForDriverDeployment(true);
                String msg = NbBundle.getMessage(SunJDBCDriverDeployer.class, "MSG_DeployDriverComplete");
                eventSupport.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE, CommandType.DISTRIBUTE, msg, StateType.COMPLETED)); // NOI18N
            }
            eventSupport.fireHandleProgressEvent(null, new Status(ActionType.EXECUTE, CommandType.DISTRIBUTE, "", StateType.COMPLETED)); // NOI18N
        }
        
        /** Returns a list of jdbc drivers that need to be deployed. */
        private List getJdbcDrivers() {
            List drivers = new ArrayList();
            Collection driversLocation = Arrays.asList(getJDBCDriversLocation().listFiles());
            for (Datasource datasource : datasources) {
                String className = datasource.getDriverClassName();
                boolean exists = false;
                try {
                    exists = Util.containsClass(driversLocation, className);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                if (!exists) {
                    for (DatabaseConnection databaseConnection : DatasourceHelper.findDatabaseConnections(datasource)) {
                        String driverClass = databaseConnection.getDriverClass();
                        JDBCDriver[] jdbcDrivers = JDBCDriverManager.getDefault().getDrivers(driverClass);
                        for (JDBCDriver jdbcDriver : jdbcDrivers) {
                            drivers.addAll(Arrays.asList(jdbcDriver.getURLs()));
                        }
                    }
                }
            }
            return drivers;
        }
        
        /** Returns location where JDBC drivers are placed
         *  on the server
         */
        private File getJDBCDriversLocation(){
            DeploymentManagerProperties dmProps = new DeploymentManagerProperties(sunDm);
            return dmProps.getDriverLocation();
        }
        
        public DeploymentStatus getDeploymentStatus() {
            return eventSupport.getDeploymentStatus();
        }

        public TargetModuleID[] getResultTargetModuleIDs() {
            return null;
        }

        public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
            return null;
        }

        public boolean isCancelSupported() {
            return false;
        }

        public void cancel() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("Cancel is not supported"); // NOI18N
        }

        public boolean isStopSupported() {
            return false;
        }

        public void stop() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("Stop is not supported"); // NOI18N
        }

        public void addProgressListener(ProgressListener progressListener) {
            eventSupport.addProgressListener(progressListener);
        }

        public void removeProgressListener(ProgressListener progressListener) {
            eventSupport.removeProgressListener(progressListener);
        }        
        
    }
    
}
