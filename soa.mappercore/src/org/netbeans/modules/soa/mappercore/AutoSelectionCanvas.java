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

package org.netbeans.modules.soa.mappercore;

import java.util.List;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionEvent;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionListener;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.utils.Utils;

/**
 *
 * @author AlexanderPermyakov
 */
public class AutoSelectionCanvas implements MapperSelectionListener {
    private Canvas canvas;
    private TreePath currentPath;
    private Link currentLink;
    private Graph currentGraph;
    private Vertex currentVertex;

    public AutoSelectionCanvas(Canvas canvas) {
        this.canvas = canvas;
        canvas.getSelectionModel().addSelectionListener(this);
    }
    public void mapperSelectionChanged(MapperSelectionEvent event) {
        Mapper mapper = canvas.getMapper();

        List<Vertex> vertexes = canvas.getSelectionModel().getSelectedVerteces();
        List<Link> links = canvas.getSelectionModel().getSelectedLinks();
        Graph graph = canvas.getSelectionModel().getSelectedGraph();
        TreePath treePath = canvas.getSelectionModel().getSelectedPath();
        if (treePath == null) return;
        //Change link
        if (links != null && links.size() > 0) {
            Link link = links.get(0);
            mapper.setExpandedGraphState(treePath, true);
            parentsExpand(treePath);

            if (link.getSource() instanceof TreeSourcePin) {
                TreePath leftTreePath = ((TreeSourcePin) link.getSource()).getTreePath();
                leftTreePath = canvas.getLeftTree().getParentVisiblePathForPath(leftTreePath);
                canvas.getLeftTree().setSelectionPath(leftTreePath);
            }
        }

        if (vertexes != null && vertexes.size() > 0) {
            Vertex vertex = vertexes.get(0);
            if (vertex != currentVertex ||
                    (!(Utils.equal(treePath, currentPath) && vertex == currentVertex)))
            {

                currentVertex = vertex;
                if (mapper.getNode(treePath, true).isGraphCollapsed()) {
                    mapper.setExpandedGraphState(treePath, true);
                }
            }
        }

    }

    private void parentsExpand(TreePath treePath) {
        Mapper mapper = canvas.getMapper();
        if (treePath == mapper.getRoot().getTreePath()) return;

        TreePath parrentPath = mapper.getNode(treePath, true).getParent().getTreePath();
        mapper.setExpandedState(parrentPath, true);
        parentsExpand(parrentPath);
    }

}
