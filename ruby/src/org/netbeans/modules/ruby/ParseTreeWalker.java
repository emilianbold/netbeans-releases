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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby;

import java.util.List;
import org.jruby.nb.ast.Node;

/**
 * A walker to iterate over a JRuby parse tree
 * 
 * @author Tor Norbye
 */
public class ParseTreeWalker {
    protected final ParseTreeVisitor visitor;

    /**
     * Construct a tree walker which will walk over the AST calling
     * each node until it reaches the startNode (if null, it will walk
     * over the whole tree), and visit until it reaches the endNode.
     * The endpoints can be visited inclusively or exclusively.
     *
     * @param visitor The visitor to be called for each node
     * @param startNode Null, or a node in the tree where visitation will begin
     * @param includeBegin True if the startNode should be visited, otherwise visiting starts after the start node
     * @param endNode Null, or a node in the tree where visition will terminate
     * @param includeEnd True if the endNode should be visited before terminating
     */
    public ParseTreeWalker(ParseTreeVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * Walk the given AST tree from the given root node. 
     * 
     * @param root The node to start recursive traversal.
     * @return True if traversal was aborted, false otherwise.
     */
    public boolean walk(Node root) {
        if (!visitor.visit(root)) {
            List<Node> list = root.childNodes();

            //for (Node child : list) {
            for (int i = 0, n = list.size(); i < n; i++) {
                Node child = list.get(i);
                if (child.isInvisible()) {
                    continue;
                }
                if (walk(child)) {
                    return true;
                }
            }
        }

        if (visitor.unvisit(root)) {
            return true;
        }

        return false;
    }
    
    //    /**
    //     * Walk a -part- of a parse tree 
    //     */
    //    public static class ParseTreeRangeWalker extends ParseTreeWalker {
    //        private final Node startNode;
    //        private final Node endNode;
    //        private final boolean includeEnd;
    //        private final boolean includeBegin;
    //        private boolean visiting;
    //        
    //        /**
    //         * Construct a tree walker which will walk over the AST calling
    //         * each node until it reaches the startNode (if null, it will walk
    //         * over the whole tree), and visit until it reaches the endNode.
    //         * The endpoints can be visited inclusively or exclusively.
    //         *
    //         * @param visitor The visitor to be called for each node
    //         * @param startNode Null, or a node in the tree where visitation will begin
    //         * @param includeBegin True if the startNode should be visited, otherwise visiting starts after the start node
    //         * @param endNode Null, or a node in the tree where visition will terminate
    //         * @param includeEnd True if the endNode should be visited before terminating
    //         */
    //        public ParseTreeRangeWalker(ParseTreeVisitor visitor, Node startNode, boolean includeBegin, Node endNode, boolean includeEnd) {
    //            super(visitor);
    //            this.startNode = startNode;
    //            this.endNode = endNode;
    //            this.includeBegin = includeBegin;
    //            this.includeEnd = includeEnd;
    //            visiting = startNode == null;
    //        }
    //
    //        /**
    //         * Walk the given AST tree from the given root node. 
    //         * 
    //         * @param root The node to start recursive traversal.
    //         * @return True if traversal was aborted, false otherwise.
    //         */
    //        public boolean walk(Node root) {
    //            if (visiting) {
    //                if (root == endNode) {
    //                    if (includeEnd) {
    //                        if (visitor.visit(root)) {
    //                            return true;
    //                        }
    //                    }
    //                    return true;
    //                }
    //                if (visitor.visit(root)) {
    //                    return true;
    //                }
    //            } else {
    //                if (root == startNode) {
    //                    visiting = true;
    //                    if (includeBegin && visitor.visit(root)) {
    //                        return true;
    //                    }
    //                }
    //            }
    //            
    //            List<Node> list = root.childNodes();
    //
    //            //for (Node child : list) {
    //            for (int i = 0, n = list.size(); i < n; i++) {
    //                Node child = list.get(i);
    //                if (walk(child)) {
    //                    return true;
    //                }
    //            }
    //            
    //            if (visiting && visitor.unvisit(root)) {
    //                return true;
    //            }
    //            
    //            return false;
    //        }
    //    }
}

