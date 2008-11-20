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

import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.db.explorer.action.ActionRegistry;
import org.netbeans.modules.db.explorer.node.NodeDataLookup;
import org.netbeans.modules.db.explorer.node.NodeRegistry;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * This is the base class for all database explorer nodes.  It takes care of setting
 * up its Lookup and registering the child factory.
 * 
 * @author Rob Englander
 */
public abstract class BaseNode extends AbstractNode {

    private final NodeDataLookup dataLookup;
    private final ActionRegistry actionRegistry;
    private final NodeRegistry nodeRegistry;
    private final ChildNodeFactory childNodeFactory;
    
    /**
     * Constructor for nodes without children.
     * 
     * @param dataLookup the data lookup for this node
     */
    public BaseNode(NodeDataLookup dataLookup, String layerEntry) {
        this(Children.LEAF, null, dataLookup, layerEntry);
    }

    /**
     * Constructor for nodes with children.
     * 
     * @param childFactory the child factory used to create children of this node
     * @param dataLookup the data lookup for this node
     */
    public BaseNode(ChildNodeFactory childFactory, NodeDataLookup dataLookup, String layerEntry) {
        this(Children.create(childFactory, true), childFactory, dataLookup, layerEntry);
    }

    /**
     * Private constructor used by the public constructors.
     * 
     * @param children the children of this node
     * @param factory the child factory to use
     * @param lookup the associated lookup
     * @param layerEntry the name of the folder in the xml layer
     */
    private BaseNode(Children children, ChildNodeFactory factory, NodeDataLookup lookup, String layerEntry) {
        super(children, lookup);
        dataLookup = lookup;
        childNodeFactory = factory;
        actionRegistry = new ActionRegistry(layerEntry);
        nodeRegistry = NodeRegistry.create(layerEntry, dataLookup);
    }
    
    /**
     * Initialize the node.  This method is called before the creation process
     * completes so that the sub class can perform any initialization it requires.
     */
    protected abstract void initialize();

    /**
     * Set up the node
     * 
     * @param dataLookup the data lookup
     * @param layerEntry the name of the layer entry folder
     * @param factory the associated child node factory, or null if this node
     * doesn't provide child nodes.
     */
    protected void setup() {
        // put the node registry and this node into the lookup
        dataLookup.add(nodeRegistry);
        dataLookup.add(this);
        
        // listen for changes to the node registry
        nodeRegistry.addChangeListener(
            new ChangeListener() {
                public void stateChanged(ChangeEvent evt) {
                    update();
                }
            }
        );

        initialize();
        update();
    }

    /**
     * Get the list of child nodes.
     * 
     * @return the list of child nodes.
     */
    public Collection<? extends Node> getChildNodes() {
        return nodeRegistry.getNodes();
    }

    /**
     * Updates the node
     */
    public void update() { 
        setName(getName());
        setDisplayName(getDisplayName());

        String iconBase = getIconBase();
        if (iconBase != null) {
            setIconBaseWithExtension(iconBase);
        }
        
        if (childNodeFactory != null) {
            childNodeFactory.refresh();
        }
    }

    /**
     * Gets the actions associated with this node.
     * 
     * @param context true if this is for a context menu, false otherwise
     * @return an array of Actions
     */
    @Override
    public Action[] getActions(boolean context) {
        if (context) {
            return super.getActions(true);
        }
        
        // get the actions from the registry
        Collection<Action> actions = actionRegistry.getActions();
        return (Action[])actions.toArray(new Action[actions.size()]);
    }
    
    /**
     * Get the icon base for the current state of the node.
     * @return the icon base
     */
    public abstract String getIconBase();

    /**
     * Get the name for the current state of the node.
     * @return the name
     */
    @Override
    public abstract String getName();
    
    /**
     * Destroy the node.
     * 
     */
    @Override
    public void destroy() {
    }

    /**
     * Determine if this node can be destroyed.
     * @return true if it can, false otherwise
     */
    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

}
