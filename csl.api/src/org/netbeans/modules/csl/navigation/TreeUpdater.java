/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.csl.navigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.navigation.ElementNode.ElementChildren;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 * Walks the Node multi-tree and updates or refreshes its live Nodes.
 * The class works in levels; after each level, the instance is posted again as 
 * a Task into a RP to process the next level. During processing, nodes to expand
 * are accumulated to member variables. The expansion itself is performed at the end,
 * when all levels are refreshed.
 * 
 * @author sdedic
 */
final class TreeUpdater implements Runnable {
    /**
     * RP which updates the tree
     */
    private static final RequestProcessor UPDATER_RP = new RequestProcessor("Tree Updater"); // NOI18N
    
    private final ClassMemberPanelUI ui;
    
    /**
     * If true, the Runnable is just synchronizing with children key fetch tasks.
     * If false, the runnable should actually update or refresh the tree.
     */
    private boolean     waiting;
    
    private List<Node>  nodesToExpand;
    
    private List<Node>  nodesToExpandRec;
    
    private List<UpdateRecord>  childrenToUpdate;
    
    private List<UpdateRecord>  newChildren;

    public TreeUpdater(ClassMemberPanelUI ui) {
        this.ui = ui;
    }
    
    void execute(ElementNode node, StructureItem description) {
        runUpdate(node, description);
        if (newChildren == null) {
            return;
        }
        this.childrenToUpdate = newChildren;
        UPDATER_RP.post(this);
    }
    
    static class UpdateRecord {
        ElementChildren children;
        Map<StructureItem,ElementNode> oldD2node;
        Set<StructureItem> oldSubs;
        ElementNode parent;
    }
    
    void runUpdate(ElementNode n, StructureItem description) {
        ElementChildren ech = (ElementChildren)n.getChildren();
        Collection<Node> nodes = ech.replaceItem(description);
        if (nodes == null) {
            return;
        }
        UpdateRecord ur = new UpdateRecord();
        ur.children = ech;
        ur.parent = n;
        ur.oldD2node = createNodeMap(nodes);
        ur.oldSubs = new HashSet<StructureItem>(ur.children.getKeys());
        newChildren.add(ur);
    }
    
    private Map<StructureItem, ElementNode> createNodeMap(Collection<Node> nodes) {
        HashMap<StructureItem,ElementNode> oldD2node = new HashMap<StructureItem,ElementNode>(); 
        for (Node node : nodes) {
            oldD2node.put(((ElementNode)node).getModel(), (ElementNode)node);
        }
        return oldD2node;
    }
    
    public void run() {
        if (waiting) {
            // all children being created are now ready, do the update
            UPDATER_RP.post(this);
            waiting = false;
        } else {
            updateChildren();
        }
    }
    
    private void updateChildren() {
        // reset the new children list; the list is added to by EN.updateSelf()
        newChildren = new ArrayList<UpdateRecord>();
        for (UpdateRecord record : childrenToUpdate) {
            ElementChildren children = record.children;
            // we can be now sure that the Children have received a resetKeys() call
            if (!children.wasInitialized()) {
                throw new IllegalStateException("Unitialized children: " + children);
            }
            // these nodes should be already fetched and expanded.
            Node[] nodes = children.getNodes(true);
            Map<StructureItem, ElementNode> newMap = createNodeMap(Arrays.asList(nodes));
            
            boolean alreadyExpanded = false;
            
            for( StructureItem newSub : children.getKeys() ) {
                if (ElementNode.isWaitNode(newSub)) {
                    throw new IllegalStateException("Keys should be already fetched");
                }
                ElementNode node = record.oldD2node.get(newSub);
                if (node != null) { // filtered out
                    if (!record.oldSubs.contains(newSub)) {
                        nodesToExpand.add(node);
                    }
                    // potentially add children to the newChildren list
                    node.updateSelf(newSub, this);
                } else { // a new node
                    if (!alreadyExpanded) {
                        // expand a Node, which got a new child.
                        alreadyExpanded = true;
                        if (record.parent.isExpandedByDefault()) {
                            nodesToExpand.add(record.parent);
                        }
                    }
                    /*
                     * Svata: there's probably not a reason to expand newly created Nodes. If cursor sits inside,
                     * the node should eventually expand
                    Node n = newMap.get(newSub);
                    if (n != null) {
                        nodesToExpandRec.add(n);
                    }
                    */
                }
            }
        }
        
        if (newChildren.isEmpty()) {
            // the END, go expand nodes
            ui.performExpansion(nodesToExpand, nodesToExpandRec);
        } else {
            // swap working areas:
            childrenToUpdate = newChildren;
            newChildren = null; 
            // post ourselves into RP, to synchronize with/after all the potentially pending ECH.getNodes().
            waiting = true;
            ElementNode.runWithChildren(this);
        }
    }
}
