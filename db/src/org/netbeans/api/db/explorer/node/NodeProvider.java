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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.api.db.explorer.node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeListener;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;

/**
 * This is the base class for all node providers, which are used to provide
 * lists of Node instances.  This is the mechanism used to dynamically
 * add child nodes to other database explorer nodes.  Instances of NodeProvider
 * are attached to nodes through the xml layer.
 * 
 * @author Rob Englander
 */
public abstract class NodeProvider<N extends Node> implements Lookup.Provider {
    /** change event support */
    private ChangeSupport changeSupport;
    
    /** the nodes supplied by this provider */
    private List<N> nodes = new CopyOnWriteArrayList<N>();
    
    /** the lookup */
    private Lookup lookup;
    
    /**
     * Constructor
     */
    public NodeProvider(Lookup lookup) {
        this.lookup = lookup;
        changeSupport = new ChangeSupport(this);
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    /**
     * Sort a list of nodes.  Subclasses that want to maintain their nodes in
     * a specific order should override this method.  The default implementation
     * doesn't alter the order.  This method should not be called directly.  It
     * is called internally to update the sort order when the collection of nodes is modified
     * in some way.  Calling this method will not change the order of the provided
     * nodes, only the order of the list of nodes passed as a parameter.
     * 
     * @param nodes the list of nodes to sort
     */
    protected void sortNodes(List<N> nodes) {
    }

    /**
     * Convenience method for performing a sort and then updating the list of
     * nodes.
     */
    private void sort(List<N> nodeList) {
        List<N> list = new ArrayList<N>(nodeList);
        sortNodes(list);
        nodes = new CopyOnWriteArrayList<N>(list);
    }
    
    /**
     * Get the list of nodes in proper sort order.
     * 
     * @return the list of nodes.
     */
    public List<N> getNodes() {
        return nodes;
    }
    
    /**
     * Get the list of nodes that contain a lookup that in turn contains 
     * a specified data object.
     * 
     * @param dataObject the data object.
     * 
     * @return the list of nodes that contain a lookup containing the data object
     */
    public List<N> getNodes(Object dataObject) {
        List<N> results = new ArrayList<N>();
        for (N child : nodes) {
            Object obj = child.getLookup().lookup(dataObject.getClass());
            if (obj == dataObject) {
                results.add(child);
            }
        }
        
        return results;
    }

    /**
     * Sets the list of nodes.
     * 
     * @param newList the new list of nodes
     */
    public void setNodes(List<N> newList) {
        sort(newList);
        changeSupport.fireChange();
    }
    
    /**
     * Add a Node.
     * 
     * @param node the node to add
     */
    public void addNode(N node) {
        nodes.add(node);
        sort(nodes);
        changeSupport.fireChange();
    }
    
    /**
     * Remove a node.
     * 
     * @param node the node to remove
     */
    public void removeNode(N node) {
        nodes.remove(node);
        changeSupport.fireChange();
    }
    
    /**
     * Remove a list of nodes.
     * 
     * @param remove the list of nodes to remove
     */
    public void removeNodes(List<N> remove) {
        nodes.removeAll(remove);
        changeSupport.fireChange();
    }

    /**
     * Remove all nodes.
     */
    public void removeAllNodes() {
        nodes.clear();
        changeSupport.fireChange();
    }
    
    /**
     * Updates the specified node.  The node is assumed to already
     * be in the list.
     * 
     * @param node the updated node
     */
    public void updateNode(N node) {
        sort(nodes);
        changeSupport.fireChange();
    }
    
    /**
     * Updates a list of nodes.  The nodes in the list are assumed to
     * be in the collection already.
     * 
     * @param nodes the list of updated nodes
     */
    public void updateNodes(List<N> nodes) {
        sort(nodes);
        changeSupport.fireChange();
    }

    /**
     * Add a change listener.
     * 
     * @param listener the listener to add.
     */
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    /**
     * Remove a change listener.
     * 
     * @param listener the listener to remove.
     */
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }
}
