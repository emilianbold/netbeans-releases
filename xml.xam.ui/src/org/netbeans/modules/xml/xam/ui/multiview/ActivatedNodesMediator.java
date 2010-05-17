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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.xam.ui.multiview;

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
 * @author Nathan Fiedler
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
        if ( (explorerManagerProvider == null) || (selected == null) ||
             (explorerManagerProvider.getExplorerManager() == null) )
            return;
        ExplorerManager em = explorerManagerProvider.getExplorerManager();
        if (selected.length > 0) {
            em.setRootContext(getRoot(selected[0]));
        }
        try {            
            em.setSelectedNodes(selected);
        } catch (PropertyVetoException pve) {
            // nothing we can do here
        } catch (IllegalArgumentException iae) {
            // Thread timing can result in our selecting nodes that
            // are not under the root node, so catch and ignore.
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
