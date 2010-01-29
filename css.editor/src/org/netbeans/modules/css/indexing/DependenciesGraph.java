/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.css.indexing;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import org.openide.filesystems.FileObject;

/**
 * bidirectional dependencies graph
 *
 * The aim of the class is to store a map of dependencies among css file
 * originaly created based on index information which can be later corrected
 * based on the parse results from opened files (if the structural information
 * from the parser result differs from the stored index data (file unsaved))
 *
 * @author marekfukala
 */
public class DependenciesGraph {
    
    private static final String INDENT = "    "; //NOI18N
    
    private Map<FileObject, Node> file2node = new HashMap<FileObject, Node>();
    private Node sourceNode;

    DependenciesGraph(FileObject source) {
        this.sourceNode = new Node(source);
    }

    public Node getNode(FileObject source) {
        Node node = file2node.get(source);
        if(node == null) {
            node = new Node(source);
        }
        return node;
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    /**
     * 
     * @return a collection a files which are either imported or importing the 
     * base source file for this dependencies graph
     */
    public Collection<FileObject> getAllRelatedFiles() {
        Collection<FileObject> files = new HashSet<FileObject>();
        addReferingFiles(files, sourceNode);
        addReferedFiles(files, sourceNode);

        return files;
    }

    private void addReferingFiles(Collection<FileObject> files, Node base) {
        files.add(base.getFile());
        for(Node node : base.refering) {
            addReferingFiles(files, node);
        }
    }

    private void addReferedFiles(Collection<FileObject> files, Node base) {
        files.add(base.getFile());
        for(Node node : base.refered) {
            addReferedFiles(files, node);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        dumpNodes(builder, 0, sourceNode, false); //refering
        dumpNodes(builder, 0, sourceNode, true); //refered
        return builder.toString();
    }

    private void dumpNodes(StringBuilder b, int level, Node node, boolean refered) {
        for(int i = 0; i < level; i++) {
            b.append(INDENT);
        }
        b.append(refered ? "->" : "<-");
        b.append(node);
        b.append('\n');
        for(Node n : refered ? node.getReferedNodes() : node.getReferingNodes()) {
            dumpNodes(b, level + 1, n, refered);
        }

    }



    public class Node {

        private FileObject source;
        private Collection<Node> refering = new LinkedList<Node>();
        private Collection<Node> refered = new LinkedList<Node>();

        private Node(FileObject source) {
            this.source = source;
        }

        public DependenciesGraph getDependencyGraph() {
            return DependenciesGraph.this;
        }

        public FileObject getFile() {
            return source;
        }

        public void addReferedNode(Node node) {
            if(refered.add(node)) {
                node.refering.add(this);
            }
        }

        public void removeReferedNode(Node node) {
            if(refered.remove(node)) {
                node.refering.remove(this);
            }
        }

        public void addReferingNode(Node node) {
            if(refering.add(node)) {
                node.refered.add(this);
            }
        }

        public void removeReferingNode(Node node) {
            if(refering.remove(node)) {
                node.refered.remove(this);
            }
        }

        /**
         *
         * @return unmodifiable collection of nodes which refers to (imports) this node
         */
        public Collection<Node> getReferingNodes() {
            return Collections.unmodifiableCollection(refering);
        }

        /**
         *
         * @return unmodifiable collection of nodes which this node refers to (imports)
         */
        public Collection<Node> getReferedNodes() {
            return Collections.unmodifiableCollection(refered);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Node other = (Node) obj;
            if (this.source != other.source && (this.source == null || !this.source.equals(other.source))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + (this.source != null ? this.source.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "Node[" + source.getPath() + "]";
        }


        
    }
}
