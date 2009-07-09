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

package org.netbeans.modules.maven.execute;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.customizer.CustomizerProviderImpl;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author mkleint
 */
public class ReactorChecker implements PrerequisitesChecker {
    
    public ReactorChecker() {
    }

    public boolean checkRunConfig(RunConfig config) {
        boolean showDialog = false;
        if (config.getProject() == null) {
            return true;
        }
        for (String goal : config.getGoals()) {
            if (goal.contains("reactor:")) { //NOI18N
                File dir = config.getExecutionDirectory();
                FileObject fo = FileUtil.toFileObject(dir);
                if (fo == null) {
                    showDialog = true;
                } else {
                    try {
                        Project prj = ProjectManager.getDefault().findProject(fo);
                        if (prj == null) {
                            showDialog = true;
                        } else {
                            NbMavenProject nbprj = prj.getLookup().lookup(NbMavenProject.class);
                            if (nbprj == null) {
                                showDialog = true;
                            } else {
                                if (!NbMavenProject.TYPE_POM.equals(nbprj.getPackagingType())) {
                                    showDialog = true;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }
            }
        }
        if (showDialog) {
            SelectReactorDirectoryPanel pnl = new SelectReactorDirectoryPanel(config.getExecutionDirectory(), config.getProject());
            DialogDescriptor nd = new DialogDescriptor(pnl, NbBundle.getMessage(ReactorChecker.class, "LBL_SELECT_REACTOR_ROOT"));
            Object ret = DialogDisplayer.getDefault().notify(nd);
            if (ret == NotifyDescriptor.OK_OPTION) {
                String path = pnl.getRelativePath();
                File selected = FileUtilities.resolveFilePath(FileUtil.toFile(config.getProject().getProjectDirectory()), path);
                config.setExecutionDirectory(selected);
                // persist in nbactions.xml file
                UserActionGoalProvider usr = config.getProject().getLookup().lookup(UserActionGoalProvider.class);
                NetbeansBuildActionXpp3Reader reader = new NetbeansBuildActionXpp3Reader();
                try {
                    ActionToGoalMapping mapping = reader.read(new StringReader(usr.getRawMappingsAsString()));
                    NetbeansActionMapping m = findAction(mapping.getActions(), config.getActionName());
                    if (m == null) {
                        //add from other locations..
                        m = ActionToGoalUtils.getDefaultMapping(config.getActionName(), config.getProject());
                        if (m == null) {
                            //hmm how come?
                            return true;
                        }
                        mapping.addAction(m);
                    }
                    m.setBasedir(path);
                    CustomizerProviderImpl.writeNbActionsModel(config.getProject(), mapping, M2Configuration.getFileNameExt(M2Configuration.DEFAULT));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (XmlPullParserException ex) {
                    Exceptions.printStackTrace(ex);
                }

            } else {
                return false;
            }
        }
        return true;
    }

    private NetbeansActionMapping findAction(List<NetbeansActionMapping> actions, String actionName) {
        for (NetbeansActionMapping m : actions) {
            if (actionName.equals(m.getActionName())) {
                return m;
            }
        }
        return null;
    }
    
}
