/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.websvc.jaxrpc.actions;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.websvc.core.ProjectClientView;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.jaxrpc.ServiceInformation;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.FilterNode;
import org.netbeans.modules.websvc.spi.support.DefaultClientSelectionPanel;

/**
 *
 * @author Peter Williams, Milan Kuchtiak
 */
public class ClientExplorerPanel extends DefaultClientSelectionPanel {

    private Project[] projects;
    private Children rootChildren;
    private Node explorerClientRoot;
    private List<Node> projectNodeList;

    public ClientExplorerPanel(FileObject srcFileObject) {
        super(srcFileObject);
        projects = OpenProjects.getDefault().getOpenProjects();
        rootChildren = new Children.Array();
        explorerClientRoot = new AbstractNode(rootChildren);
        projectNodeList = new ArrayList<Node>();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        getTreeView().expandAll();
    }

    @Override
    protected Node getRootContext() {
        for (int i=0;i<projects.length;i++) {
            Project srcFileProject = FileOwnerQuery.getOwner(getTargetFile());
            if (srcFileProject!=null && JaxWsUtils.isProjectReferenceable(projects[i], srcFileProject)) {
                LogicalViewProvider logicalProvider = (LogicalViewProvider)projects[i].getLookup().lookup(LogicalViewProvider.class);
                if (logicalProvider!=null) {
                    Node rootNode = logicalProvider.createLogicalView();
                    Node[] servicesNodes = ProjectClientView.createClientView(projects[i]);
                    if (servicesNodes!=null && servicesNodes.length>0) {
                        Children children = new Children.Array();
                        for(Node servicesNode:servicesNodes) {
                            Node[] nodes= servicesNode.getChildren().getNodes();
                            if (nodes!=null && nodes.length>0) {
                                //jaxWsServices=true;
                                Node[] filterNodes = new Node[nodes.length];
                                for (int j=0;j<nodes.length;j++) filterNodes[j] = new FilterNode(nodes[j]);
                                children.add(filterNodes);
                            }
                        }
                        if(children.getNodesCount()>0)
                            projectNodeList.add(new ProjectNode(children, rootNode));
                    }
                }
            }

        }
        Node[] projectNodes = new Node[projectNodeList.size()];
        projectNodeList.<Node>toArray(projectNodes);
        rootChildren.add(projectNodes);
        return explorerClientRoot;
    }

    @Override
    protected boolean isClientNode(Node node) {
        Node portNode = node.getParentNode();
        if (portNode != null) {
            Node serviceNode = portNode.getParentNode();
            if (serviceNode != null && serviceNode.getCookie(ServiceInformation.class) != null) {
                return true;
            }
        }
        return false;
    }

    private class ProjectNode extends AbstractNode {
        private Node rootNode;

        ProjectNode(Children children, Node rootNode) {
            super(children);
            this.rootNode=rootNode;
            setName(rootNode.getDisplayName());
        }

        @Override
        public Image getIcon(int type) {
            return rootNode.getIcon(type);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return rootNode.getOpenedIcon(type);
        }

    }

}
