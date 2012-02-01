/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.api.properties;

import org.netbeans.modules.css.lib.api.properties.GroupGrammarElement;
import org.netbeans.modules.css.lib.api.properties.Token;
import org.netbeans.modules.css.lib.api.properties.ResolvedToken;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author mfukala@netbeans.org
 */
public interface Node {

//    public int from();
//
//    public int to();

//    public String type();
    
    public String getName();

    public Collection<Node> children();

    public Node parent();
    
    public void accept(NodeVisitor visitor);

    public CharSequence image();
    
    static abstract class AbstractNode implements Node {
        
        private Node parent;

        void setParent(Node parent) {
            this.parent = parent;
        }
        
        @Override
        public Node parent() {
            return parent;
        }

        @Override
        public void accept(NodeVisitor visitor) {
            visitor.visit(this);
            for(Node child : children()) {
                child.accept(visitor);
            }
            visitor.unvisit(this);
        }

    }
    
    static class ResolvedTokenNode extends AbstractNode {
        
        private ResolvedToken resolvedToken;

        public ResolvedTokenNode(ResolvedToken token) {
            this.resolvedToken = token;
        }
        
        @Override
        public Collection<Node> children() {
            return Collections.emptyList();
        }
        
        public Token getToken() {
            return resolvedToken.token();
        }

        @Override
        public CharSequence image() {
            return resolvedToken.token().image();
        }

        @Override
        public String toString() {
            return resolvedToken.token().toString();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ResolvedTokenNode other = (ResolvedTokenNode) obj;
            if (this.resolvedToken != other.resolvedToken && (this.resolvedToken == null || !this.resolvedToken.equals(other.resolvedToken))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 53 * hash + (this.resolvedToken != null ? this.resolvedToken.hashCode() : 0);
            return hash;
        }

        @Override
        public String getName() {
            return resolvedToken.getGrammarElement().value();
        }
        
    }
    
    static class GroupNode extends AbstractNode {
        
        protected GroupGrammarElement group;
        
        private Map<Node, Node> children = new LinkedHashMap<Node, Node>();

        public GroupNode(GroupGrammarElement group) {
            this.group = group;
        }

        /**
         * @return the instance passed as the node argument if no child node
         * equal to the given node argument exists already, otherwise returns
         * the existing node.
         */
        public <T extends AbstractNode> T addChild(T node) {
            //do not overwrite the existing nodes, use the old ones instead
            if(children.containsKey(node)) {
                return (T)children.get(node); //safe
            }
            children.put(node, node);
            node.setParent(this);
            
            return node;
        }
        
        public String getName() {
            return group.getName();
        }
        
        @Override
        public Collection<Node> children() {
            return children.values();
        }

        @Override
        public String toString() {
            return group.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GroupNode other = (GroupNode) obj;
            if (this.group != other.group && (this.group == null || !this.group.equals(other.group))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + (this.group != null ? this.group.hashCode() : 0);
            return hash;
        }

        @Override
        public CharSequence image() {
            StringBuilder sb = new StringBuilder();
            for(Node child : children()) {
                sb.append(child.image());
            }
            return sb.toString();
        }

        
    }

   
}