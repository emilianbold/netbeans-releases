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
package org.netbeans.modules.php.project.ui.logicalview;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.php.project.ui.actions.DebugSingleCommand;
import org.netbeans.modules.php.project.ui.actions.DownloadCommand;
import org.netbeans.modules.php.project.ui.actions.RunSingleCommand;
import org.netbeans.modules.php.project.ui.actions.UploadCommand;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.actions.FileSystemAction;
import org.openide.actions.FindAction;
import org.openide.actions.ToolsAction;
import org.openide.actions.PasteAction;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Radek Matous
 */
public class SrcNode extends FilterNode {
    static final Image PACKAGE_BADGE = ImageUtilities.loadImage(
            "org/netbeans/modules/php/project/ui/resources/packageBadge.gif"); // NOI18N

    /**
     * creates source root node based on specified DataFolder.
     * Name is taken from bundle by 'LBL_PhpFiles' key.
     * <br/>
     * TODO : if we support several source roots, remove this constructor
     */
    SrcNode(DataFolder folder, DataFilter filter) {
        this(folder, filter, NbBundle.getMessage(PhpLogicalViewProvider.class, "LBL_PhpFiles"));
    }

    /**
     * creates source root node based on specified DataFolder.
     * Uses specified name.
     */
    SrcNode(DataFolder folder, DataFilter filter, String name) {
        this(new FilterNode(folder.getNodeDelegate(), folder.createNodeChildren(filter)), name);
    }

    private SrcNode(FilterNode node, String name) {
        super(node, new FolderChildren(node));
        disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME | DELEGATE_GET_SHORT_DESCRIPTION | DELEGATE_GET_ACTIONS);
        setDisplayName(name);
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.mergeImages(super.getIcon(type), PACKAGE_BADGE, 7, 7);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.mergeImages(super.getOpenedIcon(type), PACKAGE_BADGE, 7, 7);
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] actions = new Action[]{
            CommonProjectActions.newFileAction(),
            null,
            ProjectSensitiveActions.projectCommandAction(DownloadCommand.ID,
            DownloadCommand.DISPLAY_NAME, null),
            ProjectSensitiveActions.projectCommandAction(UploadCommand.ID,
            UploadCommand.DISPLAY_NAME, null),
            null,
            SystemAction.get(FileSystemAction.class),
            null,
            SystemAction.get(FindAction.class),
            null,
            SystemAction.get(PasteAction.class),
            null,
            SystemAction.get(ToolsAction.class),
            null,
            CommonProjectActions.customizeProjectAction()
        };
        return actions;
    }

    /**
     * Children for node that represents folder (SrcNode or PackageNode)
     */
    private static class FolderChildren extends FilterNode.Children {

        FolderChildren(final Node originalNode) {
            super(originalNode);
        }

        @Override
        protected Node[] createNodes(Node key) {
            return super.createNodes(key);
        }

        @Override
        protected Node copyNode(final Node originalNode) {
            DataObject dobj = originalNode.getLookup().lookup(DataObject.class);
            return (dobj instanceof DataFolder)
                    ? new PackageNode(originalNode)
                    : new ObjectNode(originalNode);
        }
    }

    private static final class PackageNode extends FilterNode {

        public PackageNode(final Node originalNode) {
            super(originalNode, new FolderChildren(originalNode));
        }

        @Override
        public Action[] getActions(boolean context) {
            return getOriginal().getActions(context);
        }
    }

    private static final class ObjectNode extends FilterNode {

        public ObjectNode(final Node originalNode) {
            super(originalNode);
        }

        @Override
        public Action[] getActions(boolean context) {
            List<Action> actions = new ArrayList<Action>();
            actions.addAll(Arrays.asList(getOriginal().getActions(context)));
            Action[] toAdd = new Action[]{
                null,
                ProjectSensitiveActions.projectCommandAction(RunSingleCommand.ID,
                RunSingleCommand.DISPLAY_NAME, null),
                ProjectSensitiveActions.projectCommandAction(DebugSingleCommand.ID,
                DebugSingleCommand.DISPLAY_NAME, null)
            };
            int idx = actions.indexOf(SystemAction.get(PasteAction.class));
            for (int i = 0; i < toAdd.length; i++) {
                if (idx >= 0 && idx + toAdd.length < actions.size()) {
                    //put on the proper place after paste
                    actions.add(idx + i + 1, toAdd[i]);
                } else {
                    //else put at the tail
                    actions.add(toAdd[i]);
                }
            }
            return actions.toArray(new Action[actions.size()]);
        }
    }
}