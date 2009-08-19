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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 */

package org.netbeans.modules.groovy.grailsproject.ui;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.api.GrailsConstants;
import org.netbeans.modules.groovy.grailsproject.GrailsActionProvider;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.actions.ManagePluginsAction;
import org.netbeans.modules.groovy.grailsproject.actions.GrailsCommandAction;
import org.netbeans.modules.groovy.grailsproject.actions.ResolvePluginsAction;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Adamek
 */
public class GrailsLogicalViewProvider implements LogicalViewProvider {

    private final GrailsProject project;
    GrailsLogicalViewRootNode rootNode = null;

    public GrailsLogicalViewProvider(GrailsProject project) {
        this.project = project;
        }

    public Node createLogicalView() {
        if (rootNode == null){
            rootNode = new GrailsLogicalViewRootNode();
        }

        return rootNode;
    }


    //==========================================================================

    private final class GrailsLogicalViewRootNode extends AbstractNode {

        public GrailsLogicalViewRootNode() {
            super(NodeFactorySupport.createCompositeChildren(
                    project,
                    "Projects/org-netbeans-modules-groovy-grailsproject/Nodes"),
                    Lookups.singleton(project)
                    );

            String prefix = "";

            if(!Utilities.isWindows())
                prefix = File.separator;

            setShortDescription("Grails Project in " + prefix + project.getProjectDirectory().getPath());
        }

        public Image getIcon(int type) {
            return ImageUtilities.loadImage(GrailsConstants.GRAILS_ICON_16x16);
        }

        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        public String getDisplayName() {
            return project.getProjectDirectory().getName();
        }

        @Override
        public Action[] getActions(boolean context) {
            return getAdditionalActions();
        }

        private Action[] getAdditionalActions() {

            List<Action> actions = new ArrayList<Action>();
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(GrailsActionProvider.COMMAND_BUILD,
                    NbBundle.getMessage(GrailsLogicalViewProvider.class, "LBL_BuildAction_Name"), null));
            actions.add(ProjectSensitiveActions.projectCommandAction(GrailsActionProvider.COMMAND_COMPILE,
                    NbBundle.getMessage(GrailsLogicalViewProvider.class, "LBL_Compile_Name"), null));
            actions.add(ProjectSensitiveActions.projectCommandAction(GrailsActionProvider.COMMAND_CLEAN,
                    NbBundle.getMessage(GrailsLogicalViewProvider.class, "LBL_CleanAction_Name"), null));
            actions.add(ProjectSensitiveActions.projectCommandAction(GrailsActionProvider.COMMAND_UPGRADE,
                    NbBundle.getMessage(GrailsLogicalViewProvider.class, "LBL_Upgrade_Name"), null));
            actions.add(null);
            actions.add(SystemAction.get(GrailsCommandAction.class));
            actions.add(ProjectSensitiveActions.projectCommandAction(GrailsActionProvider.COMMAND_GRAILS_SHELL,
                    NbBundle.getMessage(GrailsLogicalViewProvider.class, "LBL_ShellAction_Name"), null));
            actions.add(new ManagePluginsAction(project));
            actions.add(null);
            actions.add(new ResolvePluginsAction(project));
            actions.add(null);
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN,
                    NbBundle.getMessage(GrailsLogicalViewProvider.class, "LBL_RunAction_Name"), null));

            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG,
                    NbBundle.getMessage(GrailsLogicalViewProvider.class, "LBL_DebugAction_Name"), null));

            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST,
                    NbBundle.getMessage(GrailsLogicalViewProvider.class, "LBL_TestAction_Name"), null));
            actions.add(null);
            actions.add(CommonProjectActions.setAsMainProjectAction());
            actions.add(CommonProjectActions.closeProjectAction());
            actions.add(null);
            actions.add(SystemAction.get(FindAction.class));
            actions.add(null);
            actions.add(CommonProjectActions.deleteProjectAction());
            actions.add(null);

            // honor 57874 contact
            actions.addAll(Utilities.actionsForPath("Projects/Actions")); //NOI18N

            actions.add(null);
            actions.add(CommonProjectActions.customizeProjectAction());

            return actions.toArray(new Action[actions.size()]);

        }

    }

    public Node findPath(Node root, Object target) {
        Project project = root.getLookup().lookup(Project.class);
        if (project == null) {
            return null;
        }

        if (target instanceof FileObject) {
            FileObject fo = (FileObject) target;
            Project owner = FileOwnerQuery.getOwner(fo);
            if (!project.equals(owner)) {
                return null; // Don't waste time if project does not own the fo
            }

            for (Node n : root.getChildren().getNodes(true)) {
                Node result = TreeRootNode.findPath(n, target);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

}
