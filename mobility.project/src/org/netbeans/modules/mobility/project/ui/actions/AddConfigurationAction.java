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
package org.netbeans.modules.mobility.project.ui.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.ui.customizer.NewConfigurationPanel;
import org.netbeans.modules.mobility.project.ui.customizer.VisualConfigSupport;
import org.netbeans.spi.actions.Single;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public final class AddConfigurationAction extends Single<Node> {

    private AddConfigurationAction() {
        super(Node.class);
        putValue(NAME, NbBundle.getMessage(AddConfigurationAction.class, "LBL_VCS_AddConfiguration")); //NO18N
    }

    public static Action getStaticInstance() {
        return new AddConfigurationAction();
    }

    @Override
    protected boolean isEnabled(Node target) {
        return target.getLookup().lookup(J2MEProject.class) != null;
    }

    @Override
    protected void actionPerformed(Node target) {
        final J2MEProject project = target.getLookup().lookup(J2MEProject.class);
        final J2MEProjectProperties j2meProperties = new J2MEProjectProperties(project, project.getLookup().lookup(AntProjectHelper.class), project.getLookup().lookup(ReferenceHelper.class), project.getConfigurationHelper());
        final ArrayList<ProjectConfiguration> allNames = new ArrayList<ProjectConfiguration>();
        ProjectManager.mutex().postReadRequest(new Runnable() {

            public void run() {
                allNames.addAll(Arrays.asList(j2meProperties.getConfigurations()));
            }
        });
        final ArrayList<String> names = new ArrayList<String>(allNames.size());
        for (ProjectConfiguration cfg : allNames) {
            names.add(cfg.getDisplayName());
        }
        final NewConfigurationPanel ncp = new NewConfigurationPanel(names);
        final DialogDescriptor dd = new DialogDescriptor(ncp, NbBundle.getMessage(AddConfigurationAction.class, "LBL_VCS_AddConfiguration"), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null); //NO18N
        //NOI18N
        ncp.setDialogDescriptor(dd);
        final String newName = NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd)) ? ncp.getName() : null;
        if (newName != null) {
            ProjectManager.mutex().postWriteRequest(new Runnable() {

                public void run() {
                    if (addNewConfig(project, newName, j2meProperties, ncp, allNames)) {
                        // And save the project
                        try {
                            ProjectManager.getDefault().saveProject(project);
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                }
            });
        }
    }

    private boolean addNewConfig(J2MEProject project, final String newName, J2MEProjectProperties j2meProperties, NewConfigurationPanel ncp, List<ProjectConfiguration> allNames) {
        if (newName != null) {
            final ProjectConfiguration cfg = new ProjectConfiguration() {

                public String getDisplayName() {
                    return newName;
                }
            };
            VisualConfigSupport.createFromTemplate(j2meProperties, newName, ncp.getTemplate());
            allNames.add(cfg);
            j2meProperties.setConfigurations(allNames.toArray(new ProjectConfiguration[allNames.size()]));
            // Store the properties
            j2meProperties.store();
            try {
                project.getConfigurationHelper().setActiveConfiguration(cfg);
                return true;
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        return false;
    }
}
