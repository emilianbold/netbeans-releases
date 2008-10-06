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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ResolutionNode;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Milos Kleint 
 */
public class GraphDocumentFactory {
    
    /** Creates a new instance of GraphDocumentFactory */
    private GraphDocumentFactory() {
    }
    
    /**
     * creates a graph document for transitive dependencies
     */
    static DependencyGraphScene createDependencyDocument(Project project) {
        DependencyGraphScene scene = new DependencyGraphScene();
        try {
                MavenExecutionRequest req = new DefaultMavenExecutionRequest();
                req.setPomFile(FileUtil.toFile(project.getProjectDirectory().getFileObject("pom.xml")).getAbsolutePath());
                MavenExecutionResult res = EmbedderFactory.getOnlineEmbedder().readProjectWithDependencies(req);
                if (res.hasExceptions()) {
                    for (Object e : res.getExceptions()) {
                        Exceptions.printStackTrace((Exception)e);
                        ((Exception)e).printStackTrace();
                    }
                }
                generate(res.getArtifactResolutionResult(), scene);
        } finally {
            return scene;
        }
    }
    
    private static void generate(ArtifactResolutionResult res, DependencyGraphScene scene) {
        Map<Artifact, ArtifactGraphNode> cache = new HashMap<Artifact, ArtifactGraphNode>();
        Set<ResolutionNode> nodes = res.getArtifactResolutionNodes();
        Artifact root = res.getOriginatingArtifact();
        ResolutionNode nd1 = new ResolutionNode(root, new ArrayList());
        ArtifactGraphNode rootNode = getNode(nd1, cache, scene);
        rootNode.setRoot(true);
        for (ResolutionNode nd : nodes) {
            ArtifactGraphNode gr = getNode(nd, cache, scene);
            if (nd.isChildOfRootNode()) {
                String edge = nd1.getArtifact().getId() + "--" + nd.getArtifact().getId(); //NOI18N
                ArtifactGraphEdge ed = new ArtifactGraphEdge(edge);
                ed.setLevel(0);
                scene.addEdge(ed);
                scene.setEdgeTarget(ed, gr);
                scene.setEdgeSource(ed, rootNode);
            }
//            if (nd.isResolved()) {
                Iterator<ResolutionNode> it = nd.getChildrenIterator();
                while (it.hasNext()) {
                    ResolutionNode child = it.next();
                    ArtifactGraphNode childNode = getNode(child, cache, scene);
                    String edge = nd.getArtifact().getId() + "--" + child.getArtifact().getId();//NOI18N
                    ArtifactGraphEdge ed = new ArtifactGraphEdge(edge);
                    ed.setLevel(nd.getDepth() + 1);
                    scene.addEdge(ed);
                    scene.setEdgeTarget(ed, childNode);
                    scene.setEdgeSource(ed, gr);
                }
//            }
        }
    }
    
        private static ArtifactGraphNode getNode(ResolutionNode art, Map<Artifact, ArtifactGraphNode> cache, DependencyGraphScene scene) {
            ArtifactGraphNode nd = cache.get(art.getArtifact());
            if (nd == null) {
                nd = new ArtifactGraphNode(art);
                cache.put(art.getArtifact(), nd);
                scene.addNode(nd);
            }
            return nd;
        }
    
    
    
}
