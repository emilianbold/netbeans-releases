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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.mapper.multiview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * A little hack to get the activated nodes to work properly from within the
 * multiview element. Basically the MultiViewTopComponent lookup does not
 * deal with the activatedNodes property, so the activated nodes of the
 * multiview elements must be pushed to the MVTC via a custom Lookup.
 *
 * <p>See IssueZilla for more information on this topic:</p>
 *
 * <pre>
 *   http://www.netbeans.org/issues/show_bug.cgi?id=67257
 * </pre>
 *
     * @author Vitaly Bychkov
     * @author Nathan Fiedler
     * @version 1.0
 */
public class ActivatedNodesMediator 
        implements Lookup.Provider, PropertyChangeListener {
    /** Contents of our Lookup (the activated nodes). */
    private InstanceContent nodesHack;
    /** The lookup for the activated nodes. */
    private Lookup lookup;
    /** The Node that must be excluded from the instance content,
     * may be null. */
    private Node delegate;
    /** Signal that we are processing a property change event. */
    private boolean propertyChanging;
    /** explorer manager which should also receive the events */
    private ExplorerManager.Provider explorerManagerProvider;
    
    /**
     * Creates a new instance of ActivatedNodesMediator.
     *
     * @param  delegate  the Node delegate that must be excluded from Lookup
     *                   (may be null).
     */
    public ActivatedNodesMediator(Node delegate) {
        nodesHack = new InstanceContent();
        lookup = new AbstractLookup(nodesHack);
        this.delegate = delegate;
    }

    public Lookup getLookup() {
        return lookup;
    }

    public void setExplorerManager(ExplorerManager.Provider provider) {
	explorerManagerProvider = provider;
    }
    
    public synchronized void propertyChange(PropertyChangeEvent event) {
        if (propertyChanging) {
            // Avoid an infinite loop whereby changing the lookup contents
            // causes the activated nodes to change, which calls us again.
            return;
        }
        propertyChanging = true;
        try {
            Node[] nodes = (Node[]) event.getNewValue();
            List<Node> list = new ArrayList<Node>();
            for (Node node : nodes) {
                // Can't have same object in two lookups, apparently.
                if (delegate == null || !node.equals(delegate)) {
                    list.add(node);
                }
            }
            nodesHack.set(list, null);
	    updateExplorerManager(list.toArray(new Node[list.size()]));
        } finally {
            propertyChanging = false;
        }
    }

    private void updateExplorerManager(Node[] selected) {
        if (explorerManagerProvider != null) {
            if (selected.length > 0) {
                explorerManagerProvider.getExplorerManager().setRootContext(
                        getRoot(selected[0]));
            }
            try {
                explorerManagerProvider.getExplorerManager().setSelectedNodes(
                        selected);
            } catch (PropertyVetoException pve) {
                // nothing we can do here
            } catch (IllegalArgumentException iae) {
                // Thread timing can result in our selecting nodes that
                // are not under the root node, so catch and ignore.
            }
        }
    }

    private Node getRoot(final Node n) {
        assert n != null;
        Node root = n;
        while (root.getParentNode() != null) {
            root = root.getParentNode();
        }
        return root;
    }
}
