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
package org.netbeans.modules.hibernate.service;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

/**
 * Generates Hibernate specific Nodes that will be included and shown in 
 * Project's logical view. This factory returns an empty value for non-applicable
 * projects.
 * 
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HibernateNodeFactory implements NodeFactory {

    public NodeList<?> createNodes(Project project) {
        ArrayList<FileObject> files = new ArrayList<FileObject>();
        List<FileObject> cLists = ProjectOperations.getDataFiles(project);
        for (FileObject c : cLists) {
            if (c.isFolder() && c.getName().equals("src")) {

                FileObject[] foc = c.getChildren();
                for (FileObject foc2 : foc) {
                    if (foc2.isFolder() && foc2.getName().equals("java")) {
                        FileObject[] foc3 = foc2.getChildren();
                        for (FileObject foc4 : foc3) {
                            if (!foc4.isFolder() && foc4.getNameExt().endsWith("cfg.xml")) {
                                files.add(foc4);
                            }
                        }
                    }
                    if (!foc2.isFolder() && foc2.getNameExt().endsWith("cfg.xml")) {
                        files.add(foc2);
                    }
                }
            }

        }

        ArrayList<Node> nodes = new ArrayList<Node>();
        for (FileObject fo : files) {
            try {
                DataObject dataObject = DataObject.find(fo);
                nodes.add(dataObject.getNodeDelegate());
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
        final Children children = new Children.Array();
        children.add(nodes.toArray(new Node[]{}));
        class HibernateRootNode extends AbstractNode {

            public HibernateRootNode() {
                super(children);
            }

            @Override
            public HelpCtx getHelpCtx() {
                return super.getHelpCtx();
            }

            @Override
            public Image getIcon(int type) {
                return super.getIcon(type);
            }

            @Override
            public String getHtmlDisplayName() {
                return "Hibernate";
            }
        }
        AbstractNode hibernateNode = new HibernateRootNode();
        return NodeFactorySupport.fixedNodeList(new Node[]{hibernateNode});
    }
}
