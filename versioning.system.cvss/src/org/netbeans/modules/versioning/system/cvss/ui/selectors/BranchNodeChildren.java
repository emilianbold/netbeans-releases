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

package org.netbeans.modules.versioning.system.cvss.ui.selectors;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.util.lookup.Lookups;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Defines Tags and Branches node structure. Nodes representing
 * tag or branch have <code>String</code> it their lookups.
 *
 * 
 */
final class BranchNodeChildren extends Children.Keys {

    private final Node headNode = new AbstractNode(Children.LEAF, Lookups.singleton("HEAD")); // NOI18N
    private final SymbolicNamesNode branchesNode = SymbolicNamesNode.create(org.openide.util.NbBundle.getMessage(BranchNodeChildren.class, "BK2008"));
    private final SymbolicNamesNode tagsNode = SymbolicNamesNode.create(org.openide.util.NbBundle.getMessage(BranchNodeChildren.class, "BK2009"));

    /**
     * Sets discovered branch names. Until called
     * a wait node is presented.
     */
    public void setBranches(Collection branches) {
        branchesNode.setNames(branches);
    }

    /**
     * Sets discovered tag names. Until called
     * a wait node is presented.
     */
    public void setTags(Collection tags) {
       tagsNode.setNames(tags);
    }

    protected void addNotify() {
        List nodes = new ArrayList(3);
        headNode.setName("HEAD");  // NOI18N
        nodes.add(headNode);
        nodes.add(branchesNode);
        nodes.add(tagsNode);
        setKeys(nodes);
    }

    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }

    protected Node[] createNodes(Object key) {
        return new Node[]{ (Node) key};
    }

    private static final class SymbolicNamesNode extends AbstractNode {

        public static SymbolicNamesNode create(String displayName) {
            Children kids = new SNChildren();
            SymbolicNamesNode node = new SymbolicNamesNode(kids);
            node.setName(displayName);
            return node;
        }

        public void setNames(Collection names) {
            SNChildren kids = (SNChildren) getChildren();
            kids.setNames(names);
        }

        private SymbolicNamesNode(Children children) {
            super(children);
        }

        private static class SNChildren extends Children.Keys {

            private SNChildren() {
                AbstractNode waitNode = new AbstractNode(Children.LEAF);
                waitNode.setDisplayName(org.openide.util.NbBundle.getMessage(BranchNodeChildren.class, "BK2010"));
                setKeys(Collections.singleton(waitNode));
            }

            public void setNames(Collection names) {
                if (names.size() == 0) {
                    AbstractNode waitNode = new AbstractNode(Children.LEAF);
                    waitNode.setDisplayName(org.openide.util.NbBundle.getMessage(BranchNodeChildren.class, "BK2011"));
                    setKeys(Collections.singleton(waitNode));
                } else {
                    setKeys(names);
                }
            }

            protected Node[] createNodes(Object key) {
                if (key instanceof Node) {
                    return new Node[] {(Node) key};
                } else {
                    String name = (String) key;
                    Node node = new AbstractNode(Children.LEAF, Lookups.singleton(name));
                    node.setDisplayName(name);
                    return new Node[] {node};
                }
            }
        }
    }

}
