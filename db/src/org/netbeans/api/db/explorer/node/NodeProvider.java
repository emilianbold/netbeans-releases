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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
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
public abstract class NodeProvider implements Lookup.Provider {
    private final ChangeSupport changeSupport;
    private final TreeSet<Node> nodeSet;
    private final Lookup lookup;
    
    /**
     * Constructor
     */
    public NodeProvider(Lookup lookup) {
        this.lookup = lookup;
        changeSupport = new ChangeSupport(this);
        nodeSet = new TreeSet<Node>();
    }
    
    /**
     * Constructor
     */
    public NodeProvider(Lookup lookup, Comparator<Node> comparator) {
        this.lookup = lookup;
        changeSupport = new ChangeSupport(this);
        nodeSet = new TreeSet<Node>(comparator);
    }

    public Lookup getLookup() {
        return lookup;
    }
        
    /**
     * Get the list of nodes in proper sort order.
     * 
     * @return the list of nodes.
     */
    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(nodeSet);
    }
    
    /**
     * Get the list of nodes that contain a lookup that in turn contains 
     * a specified data object.
     * 
     * @param dataObject the data object.
     * 
     * @return the list of nodes that contain a lookup containing the data object
     */
    public Collection<Node> getNodes(Object dataObject) {
        List<Node> results = new ArrayList<Node>();
        
        synchronized (nodeSet) {
            for (Node child : nodeSet) {
                Object obj = child.getLookup().lookup(dataObject.getClass());
                if (obj == dataObject) {
                    results.add(child);
                }
            }
        }
        
        return Collections.unmodifiableCollection(results);
    }

    /**
     * Sets the list of nodes.
     * 
     * @param newList the new list of nodes
     */
    public void setNodes(Collection<Node> newList) {
        synchronized (nodeSet) {
            nodeSet.clear();
            nodeSet.addAll(newList);
        }

        changeSupport.fireChange();
    }
    
    /**
     * Add a Node.
     * 
     * @param node the node to add
     */
    public void addNode(Node node) {

        synchronized (nodeSet) {
            nodeSet.add(node);
        }
        
        changeSupport.fireChange();
    }
    
    /**
     * Remove a node.
     * 
     * @param node the node to remove
     */
    public void removeNode(Node node) {
        synchronized (nodeSet) {
            nodeSet.remove(node);
        }

        changeSupport.fireChange();
    }
    
    /**
     * Remove a list of nodes.
     * 
     * @param remove the list of nodes to remove
     */
    public void removeNodes(List<Node> remove) {
        synchronized (nodeSet) {
            nodeSet.removeAll(remove);
        }

        changeSupport.fireChange();
    }

    /**
     * Remove all nodes.
     */
    public void removeAllNodes() {
        synchronized (nodeSet) {
            nodeSet.clear();
        }

        changeSupport.fireChange();
    }
    
    /**
     * Updates the specified node.  The node is assumed to already
     * be in the list.
     * 
     * @param node the updated node
     */
    public void updateNode(Node node) {
        changeSupport.fireChange();
    }
    
    /**
     * Updates a list of nodes.  The nodes in the list are assumed to
     * be in the collection already.
     * 
     * @param nodes the list of updated nodes
     */
    public void updateNodes(List<Node> nodes) {
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
