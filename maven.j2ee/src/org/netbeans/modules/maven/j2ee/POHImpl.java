/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.ear.EarModuleProviderImpl;
import org.netbeans.modules.maven.j2ee.ejb.EjbModuleProviderImpl;
import org.netbeans.modules.maven.api.problem.ProblemReport;
import org.netbeans.modules.maven.api.problem.ProblemReporter;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.ui.BrokenServerLibrarySupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerManager;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.j2ee.web.WebModuleProviderImpl;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class POHImpl {
    private final Project project;
    private J2eeLookupProvider.Provider provider;
    private PropertyChangeListener refreshListener;
    private J2eeModuleProvider lastJ2eeProvider;

    public static final String USG_LOGGER_NAME = "org.netbeans.ui.metrics.maven"; //NOI18N
    public static final Logger USG_LOGGER = Logger.getLogger(USG_LOGGER_NAME);

    public POHImpl(Project prj, J2eeLookupProvider.Provider prov) {
        project = prj;
        provider = prov;
        
    }
    
    public void hackModuleServerChange() {
        ProjectManager.mutex().postReadRequest(new Runnable() {
            @Override
            public void run() {
                refreshAppServerAssignment();
            }
        });
    }
    private void projectOpened() {
        refreshAppServerAssignment();
        if (refreshListener == null) {
            //#121148 when the user edits the file we need to reset the server instance
            NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
            refreshListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                        hackModuleServerChange();
                    }
                }
            };
            watcher.addPropertyChangeListener(refreshListener);
        }

        //USG logging.. log target app server type for the opened project..
        String serverName = obtainServerName(project);
        if (serverName == null) {
            serverName = NbBundle.getMessage(POHImpl.class, "MSG_No_Server");  //NOI18N
        }
        String eeVersion = null;
        NbMavenProject mavProj = project.getLookup().lookup(NbMavenProject.class);
        if (mavProj != null) {
            String pkgType = mavProj.getPackagingType();
            if ("ear".equals(pkgType)) { //NOI18N
                Ear earProj = Ear.getEar(project.getProjectDirectory());
                if (earProj != null) {
                    eeVersion = earProj.getJ2eePlatformVersion();
                }
            } else if ("war".equals(pkgType)) { //NOI18N
                WebModule webM = WebModule.getWebModule(project.getProjectDirectory());
                if (webM != null) {
                    eeVersion = webM.getJ2eePlatformVersion();
                }
            } else if ("ejb".equals(pkgType)) { //NOI18N
                EjbJar ejbProj = EjbJar.getEjbJar(project.getProjectDirectory());
                if (ejbProj != null) {
                    eeVersion = ejbProj.getJ2eePlatformVersion();
                }
            }
        }
        if (eeVersion == null) {
            eeVersion = NbBundle.getMessage(POHImpl.class, "TXT_UnknownEEVersion"); //NOI18N
        }
        
        LogRecord record = new LogRecord(Level.INFO, "USG_PROJECT_OPEN_MAVEN_EE");  //NOI18N
        record.setLoggerName(USG_LOGGER_NAME);
        record.setParameters(new Object[] { serverName, eeVersion });
        USG_LOGGER.log(record);
    }

    private synchronized void refreshAppServerAssignment() {
        provider.hackModuleServerChange();

        String[] ids = obtainServerIds(project);
        String instanceFound = ids[0];
        String server = ids[1];

        ProblemReporter report = project.getLookup().lookup(ProblemReporter.class);

        if (instanceFound != null) {
            WebModuleProviderImpl impl = project.getLookup().lookup(WebModuleProviderImpl.class);
            if (impl != null) {
                impl.setServerInstanceID(instanceFound);
                impl.getConfigSupport().ensureConfigurationReady();
            }
            EjbModuleProviderImpl ejb = project.getLookup().lookup(EjbModuleProviderImpl.class);
            if (ejb != null) {
                ejb.setServerInstanceID(instanceFound);
                ejb.getConfigSupport().ensureConfigurationReady();
            }
            EarModuleProviderImpl ear = project.getLookup().lookup(EarModuleProviderImpl.class);
            if (ear != null) {
                ear.setServerInstanceID(instanceFound);
                ear.getConfigSupport().ensureConfigurationReady();
            }
        } else if (server != null) {
            String tit = Deployment.getDefault().getServerDisplayName(server);
            if (tit == null) {
                tit = server;
            }
            ProblemReport rep = new ProblemReport(ProblemReport.SEVERITY_HIGH, 
                    NbBundle.getMessage(POHImpl.class, "MSG_AppServer", tit),
                    NbBundle.getMessage(POHImpl.class, "HINT_AppServer"),
                    new AddServerAction(project));
            report.addReport(rep);
            
        }
        if (lastJ2eeProvider != null) {
            Deployment.getDefault().disableCompileOnSaveSupport(lastJ2eeProvider);
            lastJ2eeProvider = null;
        }
        J2eeModuleProvider prv = project.getLookup().lookup(J2eeModuleProvider.class);
        if (prv != null) {
            if (!BrokenServerLibrarySupport.getMissingServerLibraries(project).isEmpty()) {
                ProblemReport libProblem =  new ProblemReport(ProblemReport.SEVERITY_HIGH,
                        NbBundle.getMessage(POHImpl.class, "MSG_LibProblem"),
                        NbBundle.getMessage(POHImpl.class, "MSG_LibProblem_Description"),
                        new ServerLibraryAction(project));
                report.addReport(libProblem);
                BrokenServerLibrarySupport.fixOrShowAlert(project, null);
            }
            if (RunUtils.hasApplicationCompileOnSaveEnabled(project)) {
                Deployment.getDefault().enableCompileOnSaveSupport(prv);
                lastJ2eeProvider = prv;
            }
        }
    }

    private void projectClosed() {
        //is null check necessary?
        if (refreshListener != null) {
            NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
            watcher.removePropertyChangeListener(refreshListener);
            refreshListener = null;
        }
        if (lastJ2eeProvider != null) {
            Deployment.getDefault().disableCompileOnSaveSupport(lastJ2eeProvider);
            lastJ2eeProvider = null;
        }
       CopyOnSave copyOnSave = project.getLookup().lookup(CopyOnSave.class);
        if (copyOnSave != null) {
            try {
                copyOnSave.cleanup();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static String[] obtainServerIds (Project project) {
        String[] ids = new String[2];
        SessionContent sc = project.getLookup().lookup(SessionContent.class);
        if (sc.getServerInstanceId() != null) {
            ids[0] = sc.getServerInstanceId();
        }
        if (ids[0] == null) {
            AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
            // XXX should this first look up HINT_DEPLOY_J2EE_SERVER_ID in project (profile, ...) properties? Cf. Wrapper.createComboBoxUpdater.getDefaultValue
            String val = props.get(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID, false);
            ids[1] = props.get(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER, true);
            if (ids[1] == null) {
                //try checking for old values..
                ids[1] = props.get(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_OLD, true);
            }
            if (ids[1] != null) {
                String[] instances = Deployment.getDefault().getInstancesOfServer(ids[1]);
                String inst = null;
                if (instances != null && instances.length > 0) {
                    inst = instances[0];
                    for (int i = 0; i < instances.length; i++) {
                        if (val != null && val.equals(instances[i])) {
                            inst = instances[i];
                            break;
                        }
                    }
                    ids[0] = inst;
                }
            }
        }
        return ids;
    }

    public static String obtainServerName (Project project) {
        String id = obtainServerIds(project)[0];

        if (id != null) {
            ServerInstance si = Deployment.getDefault().getServerInstance(id);
            if (si != null) {
                try {
                    return si.getDisplayName();
                } catch (InstanceRemovedException ex) {
                    Logger.getLogger(Wrapper.class.getName()).log(Level.FINE, "", ex);
                }
            }
        }

        return null;
    }
    
    private static class AddServerAction extends AbstractAction {
        private Project prj;
        private AddServerAction(Project project) {
            prj = project;
            putValue(Action.NAME, NbBundle.getMessage(POHImpl.class, "TXT_Add_Server"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            final String newOne = ServerManager.showAddServerInstanceWizard();
            final String serverType = newOne != null ? privateGetServerId(newOne) : null;
            Utilities.performPOMModelOperations(prj.getProjectDirectory().getFileObject("pom.xml"), Collections.singletonList(new ModelOperation<POMModel>() {
                @Override public void performOperation(POMModel model) {
                    if (newOne != null) {
                        Properties props = model.getProject().getProperties();
                        if (props == null) {
                            props = model.getFactory().createProperties();
                            model.getProject().setProperties(props);
                        }
                        props.setProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER, serverType);
                    } else {
                        Properties props = model.getProject().getProperties();
                        if (props != null) {
                            props.setProperty(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER, null);
                        }
                    }
                }
            }));
            prj.getLookup().lookup(AuxiliaryProperties.class).put(MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER_ID, newOne, false);
        }
    }

    public static String privateGetServerId(String serverInstanceID) {
        ServerInstance si = Deployment.getDefault().getServerInstance(serverInstanceID);
        try {
            return si.getServerID();
        } catch (InstanceRemovedException ex) {
            return null;
        }
    }

    private class ServerLibraryAction extends AbstractAction {

        private Project project;
        public ServerLibraryAction(Project project) {
            putValue(NAME, NbBundle.getMessage(POHImpl.class, "LBL_LibProblem_ActionName"));
            this.project = project;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BrokenServerLibrarySupport.fixServerLibraries(project, new Runnable() {
                @Override
                public void run() {
                    NbMavenProject.fireMavenProjectReload(project);
                }
            });
        }
    }
    
    public static class Hook extends ProjectOpenedHook {

        private POHImpl poh;

        public Hook(POHImpl poh) {
            this.poh = poh;
        }
        
        @Override
        protected void projectOpened() {
            poh.projectOpened();
        }

        @Override
        protected void projectClosed() {
            poh.projectClosed();
        }
        
    }
}
