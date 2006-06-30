/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 * @author Petr Kuzel
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
