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
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerManager;
import org.netbeans.modules.maven.j2ee.web.WebModuleProviderImpl;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class POHImpl extends ProjectOpenedHook {
    private Project project;
    private J2eeLookupProvider.Provider provider;
    private PropertyChangeListener refreshListener;

    public POHImpl(Project prj, J2eeLookupProvider.Provider prov) {
        project = prj;
        provider = prov;
        
    }
    
    public void hackModuleServerChange() {
        provider.hackModuleServerChange();
    }
    
    protected void projectOpened() {
        provider.hackModuleServerChange();
        AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);

        String val = props.get(Constants.HINT_DEPLOY_J2EE_SERVER_ID, true);
        String server = props.get(Constants.HINT_DEPLOY_J2EE_SERVER, true);
        if (server == null) {
            //try checking for old values..
            server = props.get(Constants.HINT_DEPLOY_J2EE_SERVER_OLD, true);
        }
        String instanceFound = null;
        if (server != null) {
            String[] instances = Deployment.getDefault().getInstancesOfServer(server);
            String inst = null;
            if (instances != null && instances.length > 0) {
                inst = instances[0];
                for (int i = 0; i < instances.length; i++) {
                    if (val != null && val.equals(instances[i])) {
                        inst = instances[i];
                        break;
                    }
                }
                instanceFound = inst;
            }
        }

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
        if (refreshListener == null) {
            //#121148 when the user edits the file we need to reset the server instance
            NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
            refreshListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                        projectOpened();
                    }
                }
            };
            watcher.addPropertyChangeListener(refreshListener);
        }
    }

    protected void projectClosed() {
        //is null check necessary?
        if (refreshListener != null) {
            NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
            watcher.removePropertyChangeListener(refreshListener);
            refreshListener = null;
        }
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
