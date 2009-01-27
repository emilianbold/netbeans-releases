/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;

/**
 *
 * @author mkleint
 */
class GraphConstructor implements DependencyNodeVisitor {
    private DependencyGraphScene scene;
    private DependencyNode root;
    private Stack<DependencyNode> path;
    private Stack<ArtifactGraphNode> graphPath;
    private Map<String, ArtifactGraphNode> cache;
    private List<ArtifactGraphEdge> edges;

    GraphConstructor(DependencyGraphScene scene) {
        this.scene = scene;
        path = new Stack<DependencyNode>();
        graphPath = new Stack<ArtifactGraphNode>();
        cache = new HashMap<String, ArtifactGraphNode>();
        edges = new ArrayList<ArtifactGraphEdge>();
    }


    public boolean visit(DependencyNode node) {
        if (root == null) {
            root = node;
        }
        ArtifactGraphNode grNode;
        boolean primary = false;
        if (node.getState() == DependencyNode.INCLUDED) {
            grNode = cache.get(node.getArtifact().getDependencyConflictId());
            if (grNode == null) {
                grNode = new ArtifactGraphNode(node);
                cache.put(node.getArtifact().getDependencyConflictId(), grNode);
                scene.addNode(grNode);
            } else {
                grNode.setArtifact(node);
                scene.addNode(grNode);
            }
            primary = true;
        } else {
            grNode = cache.get(node.getArtifact().getDependencyConflictId());
            if (grNode == null) {
                grNode = new ArtifactGraphNode(null);
                Artifact a = node.getState() == DependencyNode.OMITTED_FOR_CONFLICT ? node.getRelatedArtifact() : node.getArtifact();
                cache.put(a.getDependencyConflictId(), grNode);
            }
            grNode.addDuplicateOrConflict(node);
        }

        if (!path.empty()) {
            DependencyNode parent = path.peek();
            ArtifactGraphEdge ed = new ArtifactGraphEdge(parent, node);
            ed.setLevel(path.size() - 0);
            ed.setPrimaryPath(primary);
            edges.add(ed);
        }

        path.push(node);
        graphPath.push(grNode);

        return true;
    }

    public boolean endVisit(DependencyNode node) {
        path.pop();
        graphPath.pop();
        if (root == node) {
            //add all edges now
            for (ArtifactGraphEdge ed : edges) {
                scene.addEdge(ed);
                ArtifactGraphNode grNode = cache.get(ed.getTarget().getArtifact().getDependencyConflictId());
                if (grNode == null) { //FOR conflicting nodes..
                    grNode = cache.get(ed.getTarget().getRelatedArtifact().getDependencyConflictId());
                }
                scene.setEdgeTarget(ed, grNode);
                ArtifactGraphNode parentGrNode = cache.get(ed.getSource().getArtifact().getDependencyConflictId());
                scene.setEdgeSource(ed, parentGrNode);
            }
        }
        return true;
    }
}
