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
package org.openide.nodes;

import java.lang.ref.Reference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/** Holder of nodes for a children object. Communicates
* with children to notify when created/finalized.
*
* @author Jaroslav Tulach
*/
final class ChildrenArray extends NodeAdapter {
    /** children */
    public Children children;

    /** nodes associated */
    private Node[] nodes;

    private Map<Children.Info,Collection<Node>> map;
    /** the reference that points to us */
    private Reference<ChildrenArray> ref;

    private static final Logger LOG_NODES_FOR = Logger.getLogger(
            "org.openide.nodes.ChildrenArray.nodesFor"); // NOI18N

    /** Creates new ChildrenArray */
    public ChildrenArray() {
    }

    public Children getChildren() {
        return children;
    }

    /** When finalized notify the children.
    */
    protected void finalize() {
        children.finalizedChildrenArray(ref);
    }
    
    /** Now points to me */
    final void pointedBy(Reference<ChildrenArray> ref) {
        this.ref = ref;
    }

    /** Getter method to receive a set of computed nodes.
    */
    public Node[] nodes() {
        if (children == null) {
            // not fully initialize
            return null;
        }

        if (nodes == null) {
            nodes = children.justComputeNodes();

            for (int i = 0; i < nodes.length; i++) {
                // keeps a hard reference from the children node to this
                // so we can be GCed only when child nodes are gone
                nodes[i].reassignTo(children, this);
            }

            // if at least one node => be weak
            children.registerChildrenArray(this, nodes.length > 0);
        }

        return nodes;
    }

    /** Clears the array of nodes.
    */
    public void clear() {
        if (nodes != null) {
            nodes = null;

            // register in the childrens to be hold by hard reference
            // because we keep no reference to nodes, we can be
            // hard holded by children
            children.registerChildrenArray(this, false);
        }
    }

    /** Finalizes nodes by calling get on weak hash map,
    * all references stored in the map, that are finalized
    * will be cleared.
    */
    public void finalizeNodes() {
        Map m = map;

        if (m != null) {
            // processes the queue of garbage
            // collected keys
            m.remove(null);
        }
    }

    /** Initilized if has some nodes.
    */
    public boolean isInitialized() {
        return nodes != null;
    }

    private String logInfo(Children.Info info) {
        return info.toString() + '[' + Integer.toHexString(System.identityHashCode(info)) + ']';
    }

    /** Gets the nodes for given info.
    * @param info the info
    * @return the nodes
    */
    public synchronized Collection<Node> nodesFor(Children.Info info) {
        final boolean IS_LOG = LOG_NODES_FOR.isLoggable(Level.FINE);
        if (IS_LOG) {
            LOG_NODES_FOR.fine("nodesFor(" +logInfo(info) + ") on " + Thread.currentThread()); // NOI18N
        }
        if (map == null) {
            map = new WeakHashMap<Children.Info,Collection<Node>>(7);
        }
        Collection<Node> nodes = map.get(info);

        if (IS_LOG) {
            LOG_NODES_FOR.fine("  map size=" + map.size() + ", nodes=" + nodes); // NOI18N
        }

        if (nodes == null) {
            nodes = info.entry.nodes();
            info.length = nodes.size();
            map.put(info, nodes);
            if (IS_LOG) {
                LOG_NODES_FOR.fine("  created nodes=" + nodes); // NOI18N
            }
        }

        if (IS_LOG) {
            LOG_NODES_FOR.fine("  leaving nodesFor(" +logInfo(info) + ") on " + Thread.currentThread()); // NOI18N
        }
        return nodes;
    }

    /** Refreshes the nodes for given info.
    * @param info the info
    * @return the nodes
    */
    public synchronized void useNodes(Children.Info info, Collection<Node> list) {
        if (map == null) {
            map = new WeakHashMap<Children.Info,Collection<Node>>(7);
        }
        
        info.length = list.size();

        map.put(info, list);
    }

    public String toString() {
        return super.toString() + "  " + getChildren(); //NOI18N
    }
}
