/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.j2ee.ear.EarModuleProviderImpl;
import org.netbeans.modules.maven.j2ee.ejb.EjbModuleProviderImpl;
import org.netbeans.modules.maven.api.problem.ProblemReport;
import org.netbeans.modules.maven.api.problem.ProblemReporter;
import org.netbeans.modules.maven.spi.customizer.ModelHandleUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerManager;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.j2ee.web.CopyOnSave;
import org.netbeans.modules.maven.j2ee.web.WebModuleProviderImpl;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class POHImpl extends ProjectOpenedHook {
    private final Project project;
    private J2eeLookupProvider.Provider provider;
    private PropertyChangeListener refreshListener;
    private J2eeModuleProvider lastJ2eeProvider;
    private String contextPath;

    public static final String USG_LOGGER_NAME = "org.netbeans.ui.metrics.maven"; //NOI18N
    public static final Logger USG_LOGGER = Logger.getLogger(USG_LOGGER_NAME);

    public POHImpl(Project prj, J2eeLookupProvider.Provider prov) {
        project = prj;
        provider = prov;
        
    }
    
    public void hackModuleServerChange() {
        ProjectManager.mutex().postReadRequest(new Runnable() {
            public void run() {
                refreshAppServerAssignment();
            }
        });
    }

    public void setContextPath(String path) {
        this.contextPath = path;
    }
    
    protected void projectOpened() {
        refreshAppServerAssignment();
        if (refreshListener == null) {
            //#121148 when the user edits the file we need to reset the server instance
            NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
            refreshListener = new PropertyChangeListener() {
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

    protected synchronized void refreshAppServerAssignment() {
        provider.hackModuleServerChange();

        String[] ids = obtainServerIds(project);
        String instanceFound = ids[0];
        String server = ids[1];

        if (instanceFound != null) {
            WebModuleProviderImpl impl = project.getLookup().lookup(WebModuleProviderImpl.class);
            if (impl != null) {
                impl.setServerInstanceID(instanceFound);
                impl.getConfigSupport().ensureConfigurationReady();
                if (contextPath != null) {
                    impl.getWebModuleImplementation().setContextPath(contextPath);
                }
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
            ProblemReporter report = project.getLookup().lookup(ProblemReporter.class);
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
        J2eeModuleProvider prv = project.getLookup().lookup(J2eeModuleProvider.class);
        if (prv != null) {
            lastJ2eeProvider = prv;
            if (RunUtils.hasApplicationCompileOnSaveEnabled(project)) {
                Deployment.getDefault().enableCompileOnSaveSupport(prv);
            } else {
                Deployment.getDefault().disableCompileOnSaveSupport(prv);
            }
        } else {
            if (lastJ2eeProvider != null) {
                Deployment.getDefault().disableCompileOnSaveSupport(lastJ2eeProvider);
                lastJ2eeProvider = null;
            }
        }
    }

    protected void projectClosed() {
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
                ex.printStackTrace();
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
            String val = props.get(Constants.HINT_DEPLOY_J2EE_SERVER_ID, true);
            ids[1] = props.get(Constants.HINT_DEPLOY_J2EE_SERVER, true);
            if (ids[1] == null) {
                //try checking for old values..
                ids[1] = props.get(Constants.HINT_DEPLOY_J2EE_SERVER_OLD, true);
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
        
        public void actionPerformed(ActionEvent e) {
            String newOne = ServerManager.showAddServerInstanceWizard();
            String serverType = null;
            if (newOne != null) {
                serverType = privateGetServerId(newOne);
            }
            try {
                ModelHandle handle = ModelHandleUtils.createModelHandle(prj);
                //get rid of old settings.
                POMModel model = handle.getPOMModel();
                Profile prof = handle.getNetbeansPublicProfile(false);
                if (prof != null) {
                    Properties props = prof.getProperties();
                    if (props != null) {
                        props.setProperty(Constants.HINT_DEPLOY_J2EE_SERVER_OLD, null);
                    }
                }
                if (newOne != null) {
                    Properties props = model.getProject().getProperties();
                    if (props == null) {
                        props = model.getFactory().createProperties();
                        model.getProject().setProperties(props);
                    }
                    props.setProperty(Constants.HINT_DEPLOY_J2EE_SERVER, serverType);
                    org.netbeans.modules.maven.model.profile.Profile privateProf = handle.getNetbeansPrivateProfile();
                    org.netbeans.modules.maven.model.profile.Properties privs = privateProf.getProperties();
                    if (privs == null) {
                        privs = handle.getProfileModel().getFactory().createProperties();
                        privateProf.setProperties(privs);
                    }
                    privs.setProperty(Constants.HINT_DEPLOY_J2EE_SERVER_ID, newOne);
                    handle.markAsModified(handle.getProfileModel());
                } else {
                    Properties props = model.getProject().getProperties();
                    if (props != null) {
                        props.setProperty(Constants.HINT_DEPLOY_J2EE_SERVER, null);
                    }
                    org.netbeans.modules.maven.model.profile.Profile privprof = handle.getNetbeansPrivateProfile(false);
                    if (privprof != null && privprof.getProperties() != null) {
                        privprof.getProperties().setProperty(Constants.HINT_DEPLOY_J2EE_SERVER_ID, null);
                        handle.markAsModified(handle.getProfileModel());
                    }
                }
                handle.markAsModified(handle.getPOMModel());
                ModelHandleUtils.writeModelHandle(handle, prj);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (XmlPullParserException ex) {
                Exceptions.printStackTrace(ex);
            }
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

}
