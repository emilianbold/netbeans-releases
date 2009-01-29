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

package org.netbeans.modules.maven.graph;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.DependencyTreeFactory;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author mkleint
 */
public class ShowGraphAction extends AbstractAction implements ContextAwareAction {
    public ShowGraphAction() {
        putValue(Action.NAME, org.openide.util.NbBundle.getMessage(ShowGraphAction.class, "ACT_Show_Graph"));
    }
    
    public ShowGraphAction(Project prj) {
        this();
        if (prj != null) {
            putValue("prj", prj); //NOI18N
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        final Project project = (Project) getValue("prj"); //NOI18N
        if (project != null) {
            final InstanceContent ic = new InstanceContent();
            ic.add(project);
            Lookup lkp = new AbstractLookup(ic);
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
                    DependencyGraphScene scene = new DependencyGraphScene(prj.getMavenProject());
                    DependencyNode root = DependencyTreeFactory.createDependencyTree(prj.getMavenProject(), EmbedderFactory.getOnlineEmbedder(), Artifact.SCOPE_TEST);
                    ic.add(prj.getMavenProject());
                    ic.add(root);
                }
            });
            TopComponent tc = new DependencyGraphTopComponent(lkp);
            ProjectInformation info = project.getLookup().lookup(ProjectInformation.class);
            tc.setName("DependencyGraph" + info.getName()); //NOI18N
            tc.setDisplayName(NbBundle.getMessage(DependencyGraphTopComponent.class,
                "TIT_DepGraphTC", info.getDisplayName()));

            WindowManager.getDefault().findMode("editor").dockInto(tc); //NOI18N
            tc.open();
            tc.requestActive();
        }
    }
    
    public Action createContextAwareInstance(Lookup lookup) {
        Project prj = lookup.lookup(Project.class);
        return new ShowGraphAction(prj);
    }
}