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
 * Contributor(s): The Original Software is NetBeans. The Initial
 * Developer of the Original Software is Sun Microsystems, Inc. Portions
 * Copyright 1997-2006 Sun Microsystems, Inc. All Rights Reserved.
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
package org.netbeans.modules.php.project.ui.logicalview;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.project.PhpActionProvider;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * @author ads, Tomas Mysik
 */
public class PhpLogicalViewProvider implements LogicalViewProvider {
    private static final Logger LOGGER = Logger.getLogger(PhpLogicalViewProvider.class.getName());

    final PhpProject project;

    public PhpLogicalViewProvider(PhpProject project) {
        assert project != null;
        this.project = project;
    }

    public Node createLogicalView() {
        return new PhpLogicalViewRootNode(project);
    }

    public Node findPath(Node root, Object target) {
        Project p = root.getLookup().lookup(Project.class);
        if (p == null) {
            return null;
        }
        // Check each child node in turn.
        Node[] children = root.getChildren().getNodes(true);
        for (Node node : children) {
            if (target instanceof DataObject || target instanceof FileObject) {
                DataObject d = node.getLookup().lookup(DataObject.class);
                if (d == null) {
                    continue;
                }
                // Copied from org.netbeans.spi.java.project.support.ui.TreeRootNode.PathFinder.findPath:
                FileObject kidFO = d.getPrimaryFile();
                FileObject targetFO = null;
                if (target instanceof DataObject) {
                    targetFO = ((DataObject) target).getPrimaryFile();
                } else {
                    targetFO = (FileObject) target;
                }
                Project owner = FileOwnerQuery.getOwner(targetFO);
                if (!p.equals(owner)) {
                    return null; // Don't waste time if project does not own the fileobject
                }
                if (kidFO == targetFO) {
                    return node;
                } else if (FileUtil.isParentOf(kidFO, targetFO)) {
                    String relPath = FileUtil.getRelativePath(kidFO, targetFO);

                    // first path without extension (more common case)
                    String[] path = relPath.split("/"); // NOI18N
                    path[path.length - 1] = targetFO.getName();

                    // first try to find the file without extension (more common case)
                    Node found = findNode(node, path);
                    if (found == null) {
                        // file not found, try to search for the name with the extension
                        path[path.length - 1] = targetFO.getNameExt();
                        found = findNode(node, path);
                    }
                    if (found == null) {
                        return null;
                    }
                    if (hasObject(found, target)) {
                        return found;
                    }
                    Node parent = found.getParentNode();
                    Children kids = parent.getChildren();
                    children = kids.getNodes();
                    for (Node child : children) {
                        if (hasObject(child, target)) {
                            return child;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Node findNode(Node start, String[] path) {
        Node found = null;
        try {
            found = NodeOp.findPath(start, path);
        } catch (NodeNotFoundException ex) {
            // ignored
        }
        return found;
    }

    private boolean hasObject(Node node, Object obj) {
        if (obj == null) {
            return false;
        }
        DataObject dataObject = node.getLookup().lookup(DataObject.class);
        if (dataObject == null) {
            return false;
        }
        if (obj instanceof DataObject) {
            if (dataObject.equals(obj)) {
                return true;
            }
            FileObject fileObject = ((DataObject) obj).getPrimaryFile();
            return hasObject(node, fileObject);
        } else if (obj instanceof FileObject) {
            FileObject fileObject = dataObject.getPrimaryFile();
            return obj.equals(fileObject);
        } else {
            return false;
        }
    }
    private static class PhpLogicalViewRootNode extends AbstractNode {

        PhpProject project;

        @Override
        public String getShortDescription() {
            String prjDirDispName = FileUtil.getFileDisplayName(project.getProjectDirectory());
            return NbBundle.getMessage(PhpLogicalViewProvider.class, "HINT_project_root_node", prjDirDispName);
        }

        @Override
        public Action[] getActions(boolean context) {
            PhpActionProvider provider = project.getLookup().lookup(PhpActionProvider.class);
            assert provider != null;
            List<Action> actions = new ArrayList<Action>();
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            actions.add(provider.getAction(ActionProvider.COMMAND_RUN));
            actions.add(provider.getAction(ActionProvider.COMMAND_DEBUG));
            actions.add(null);
            actions.add(CommonProjectActions.setProjectConfigurationAction());
            actions.add(null);
            actions.add(CommonProjectActions.setAsMainProjectAction());
            //actions.add(CommonProjectActions.openSubprojectsAction()); // does not make sense for php now
            actions.add(CommonProjectActions.closeProjectAction());
            actions.add(null);
            actions.add(CommonProjectActions.renameProjectAction());
            actions.add(CommonProjectActions.moveProjectAction());
            actions.add(CommonProjectActions.copyProjectAction());
            actions.add(CommonProjectActions.deleteProjectAction());
            actions.add(null);
            actions.add(SystemAction.get(FindAction.class));
            // honor 57874 contact
            actions.add(null);
            actions.addAll(Utilities.actionsForPath("Projects/Actions")); // NOI18N
            actions.add(null);
            actions.add(CommonProjectActions.customizeProjectAction());
            return actions.toArray(new Action[actions.size()]);
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(PhpLogicalViewProvider.class);
        }

        public PhpLogicalViewRootNode(PhpProject project) {
            super(createChildren(project), Lookups.singleton(project));
            this.project = project;
            setIconBaseWithExtension("org/netbeans/modules/php/project/ui/resources/phpProject.png");
            setName(ProjectUtils.getInformation(this.project).getDisplayName());
        }


        private static Children createChildren(PhpProject project) {
           return NodeFactorySupport.createCompositeChildren(project,
                    "Projects/org-netbeans-modules-php-project/Nodes");//NOI18N
        }
    }
}
