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

package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProviderFactory;
import org.netbeans.modules.cnd.discovery.buildsupport.BuildProjectActionHandler.ExecLogWrapper;
import org.netbeans.modules.cnd.discovery.services.DiscoveryManagerImpl;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider.OutputStreamHandler;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider.class)
public class BuildActionsProviderImpl extends BuildActionsProvider {

    @Override
    public List<BuildAction> getActions(String ioTabName, ProjectActionEvent[] events) {
        //return Collections.<Action>emptyList();
        List<BuildAction> res = new ArrayList<>();
        if (events != null &&events.length >= 2) {
            if (events[events.length-2].getType() == ProjectActionEvent.PredefinedType.CLEAN &&
                events[events.length-1].getType() == ProjectActionEvent.PredefinedType.BUILD &&
                events[events.length-1].getConfiguration() != null &&
                events[events.length-1].getConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_MAKEFILE) {
                res.add(new ConfigureAction(ioTabName, events));
            }
        }
        return res;
    }

    public static final class ConfigureAction extends AbstractAction implements BuildAction,  OutputStreamHandler {
        private final String ioTabName;
        private final ProjectActionEvent[] events;
        private int step = -1;
        private BufferedWriter bw;
        private ExecLogWrapper execLog;

        public ConfigureAction(String ioTabName, ProjectActionEvent[] events) {
            this.ioTabName = ioTabName;
            this.events = events;
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(Action.SMALL_ICON)) {
                return new ImageIcon(BuildActionsProviderImpl.class.getResource("/org/netbeans/modules/cnd/discovery/wizard/resources/configure.png")); // NOI18N
            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
                return NbBundle.getMessage(BuildActionsProviderImpl.class, "OUTPUT_LOG_ACTION_TEXT"); // NOI18N
            } else {
                return super.getValue(key);
            }
        }

        @Override
        public void executionStarted(int pid) {
            setEnabled(false);
            if (step == events.length - 1) {
                try {
                    File file = File.createTempFile("build", ".log"); // NOI18N
                    file.deleteOnExit();
                    if (execLog == null) {
                        execLog = new ExecLogWrapper(null, null);
                    }
                    execLog.setBuildLog(file.getAbsolutePath());
                    bw = Files.newBufferedWriter(file.toPath(), Charset.forName("UTF-8")); //NOI18N
                } catch (IOException ex) {
                    execLog.setBuildLog(null);
                    bw = null;
                    Exceptions.printStackTrace(ex);
                }
            } else if (step == events.length - 2) {
                execLog = null;
            }
        }

        @Override
        public void executionFinished(int rc) {
            if (step == events.length - 1 && rc == 0 && execLog != null && execLog.getBuildLog() != null) {
                setEnabled(true);
            }
        }

        @Override
        public void setStep(int step) {
            this.step = step;
        }


        public void setExecLog(ExecLogWrapper execLog) {
            this.execLog = execLog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            if (step >= 0 && step < events.length) {
                Project project = events[step].getProject();
                if (execLog.getBuildLog() != null) {
                    invokeWizard(project);
                    execLog = null;
                }
            }
        }

        @Override
        public void handleLine(String line) {
            if (bw != null) {
                try {
                    bw.write(line);
                } catch (IOException ex) {
                }
            }
        }

        @Override
        public void flush() {
            if (bw != null) {
                try {
                    bw.flush();
                } catch (IOException ex) {
                }
            }
        }

        @Override
        public void close() {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }

        private void invokeWizard(Project project) {
            DiscoveryProvider provider = null;
            if (execLog.getExecLog() != null) {
                provider = DiscoveryProviderFactory.findProvider(DiscoveryExtension.EXEC_LOG_PROVIDER);
            }
            if (provider == null) {
                provider = DiscoveryProviderFactory.findProvider(DiscoveryExtension.MAKE_LOG_PROVIDER);
            }
            if (provider == null) {
                return;
            }
            if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                getString("OUTPUT_LOG_DILOG_COMTENT_TEXT"), // NOI18N
                getString("OUTPUT_LOG_DILOG_TITLE_TEXT"), // NOI18N
                NotifyDescriptor.YES_NO_OPTION)) != NotifyDescriptor.YES_OPTION){
                return;
            }
            Map<String, Object> artifacts = new HashMap<>();
            artifacts.put(DiscoveryManagerImpl.BUILD_EXEC_KEY, execLog.getExecLog());
            artifacts.put(DiscoveryManagerImpl.BUILD_LOG_KEY, execLog.getBuildLog());
            
            DiscoveryManagerImpl.projectBuilt(project, artifacts, false);
        }
        
        private String getString(String key) {
            return NbBundle.getMessage(BuildActionsProviderImpl.class, key);
        }
    }
}
