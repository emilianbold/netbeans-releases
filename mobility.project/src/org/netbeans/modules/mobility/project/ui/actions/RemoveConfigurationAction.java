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
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.actions.ContextAction;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public final class RemoveConfigurationAction extends ContextAction<Node> {

    private RemoveConfigurationAction() {
        super(Node.class);
        putValue(NAME, NbBundle.getMessage(RemoveConfigurationAction.class, "ACSN_RemovePanel")); //NO18N
    }

    public static Action getStaticInstance() {
        return new RemoveConfigurationAction();
    }

    protected void performAction(final Node[] activatedNodes) {
        StringBuffer buffer = new StringBuffer();
        for (Node node : activatedNodes) {
            ProjectConfiguration conf = node.getLookup().lookup(ProjectConfiguration.class);
            buffer.append('\n').append(conf.getDisplayName());
        }
        final NotifyDescriptor desc = new NotifyDescriptor.Confirmation(NbBundle.getMessage(RemoveConfigurationAction.class, "LBL_VCS_ReallyRemove", buffer), NotifyDescriptor.YES_NO_OPTION); //NO18N
        if (NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(desc))) {
            ProjectManager.mutex().postWriteRequest(new Runnable() {

                public void run() {
                    for (Node node : activatedNodes) {
                        final ProjectConfiguration conf = node.getLookup().lookup(ProjectConfiguration.class);
                        final J2MEProject project = node.getLookup().lookup(J2MEProject.class);
                        removeProperties(project, conf);
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

    private void removeProperties(final J2MEProject project, final ProjectConfiguration conf) {
        final J2MEProjectProperties j2meProperties = new J2MEProjectProperties(project, project.getLookup().lookup(AntProjectHelper.class), project.getLookup().lookup(ReferenceHelper.class), project.getConfigurationHelper());
        final String[] keys = j2meProperties.keySet().toArray(new String[j2meProperties.size()]);
        final String prefix = J2MEProjectProperties.CONFIG_PREFIX + conf.getDisplayName();
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].startsWith(prefix)) {
                j2meProperties.remove(keys[i]);
            }
        }
        final List<ProjectConfiguration> col = Arrays.asList(j2meProperties.getConfigurations());
        final ArrayList<ProjectConfiguration> list = new ArrayList<ProjectConfiguration>();
        list.addAll(col);
        list.remove(conf);
        //Set active in case we are deleting active
        if (project.getConfigurationHelper().getActiveConfiguration().equals(conf)) {
            try {
                project.getConfigurationHelper().setActiveConfiguration(project.getConfigurationHelper().getDefaultConfiguration());
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        j2meProperties.setConfigurations(list.toArray(new ProjectConfiguration[list.size()]));
        // Store the properties
        j2meProperties.store();
    }

    @Override
    public void actionPerformed(Collection<? extends Node> nodes) {
        Node[] n = nodes.toArray(new Node[nodes.size()]);
        performAction(n);
    }
}
