/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.api.db.explorer.node;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.db.explorer.action.ActionRegistry;
import org.netbeans.modules.db.explorer.node.NodeDataLookup;
import org.netbeans.modules.db.explorer.node.NodePropertySupport;
import org.netbeans.modules.db.explorer.node.NodeRegistry;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * This is the base class for all database explorer nodes.  It takes care of setting
 * up its Lookup and registering the child factory.
 * 
 * @author Rob Englander
 */
public abstract class BaseNode extends AbstractNode {

    // property name bundle keys
    protected static final String DATABASEURL = "DatabaseURL"; // NOI18N
    protected static final String DATABASEURLDESC = "DatabaseURLDescription"; // NOI18N
    protected static final String DRIVER = "DriverURL"; // NOI18N
    protected static final String DRIVERDESC = "DriverURLDescription"; // NOI18N
    protected static final String SCHEMA = "Schema"; // NOI18N
    protected static final String SCHEMADESC = "SchemaDescription"; // NOI18N
    protected static final String PROP_DEFSCHEMA = "DefaultSchema"; //NOI18N
    protected static final String PROP_DEFSCHEMADESC = "DefaultSchema"; //NOI18N
    protected static final String PROP_DEFCATALOG = "DefaultCatalog"; //NOI18N
    protected static final String PROP_DEFCATALOGDESC = "DefaultCatalog"; //NOI18N
    protected static final String USER = "User"; // NOI18N
    protected static final String USERDESC = "UserDescription"; // NOI18N
    protected static final String REMEMBERPW = "RememberPassword"; // NOI18N
    protected static final String REMEMBERPWDESC = "RememberPasswordDescription"; // NOI18N
    protected static final String CATALOG = "Catalog"; // NOI18N
    protected static final String CATALOGDESC = "CatalogDescription"; // NOI18N
    protected static final String DISPLAYNAME = "DisplayName"; // NOI18N
    protected static final String DISPLAYNAMEDESC = "DisplayNameDescription"; // NOI18N
    protected static final String UNIQUE = "UniqueNoMnemonic"; // NOI18N
    protected static final String UNIQUEDESC = "UniqueDescription"; // NOI18N
    protected static final String NULL = "Null"; // NOI18N
    protected static final String NULLDESC = "NullDescription"; // NOI18N
    protected static final String TYPE = "Type"; // NOI18N
    protected static final String TYPEDESC = "TypeDescription"; // NOI18N
    protected static final String DATATYPE = "Datatype"; // NOI18N
    protected static final String DATATYPEDESC = "DatatypeDescription"; // NOI18N
    protected static final String COLUMNSIZE = "ColumnSize"; // NOI18N
    protected static final String COLUMNSIZEDESC = "ColumnSizeDescription"; // NOI18N
    protected static final String DIGITS = "DecimalDigits"; // NOI18N
    protected static final String DIGITSDESC = "DecimalDigitsDescription"; // NOI18N
    protected static final String POSITION = "Position"; // NOI18N
    protected static final String POSITIONDESC = "PositionDescription"; // NOI18N
    protected static final String FKPOSITION = "PositionInFK"; // NOI18N
    protected static final String FKPOSITIONDESC = "PositionInFKDescription"; // NOI18N
    protected static final String FKREFERRINGSCHEMA = "ReferringFKSchema"; // NOI18N
    protected static final String FKREFERRINGSCHEMADESC = "ReferringFKSchema"; // NOI18N
    protected static final String FKREFERRINGTABLE = "ReferringFKTable"; // NOI18N
    protected static final String FKREFERRINGTABLEDESC = "ReferringFKTable"; // NOI18N
    protected static final String FKREFERRINGCOLUMN = "ReferringFKColumn"; // NOI18N
    protected static final String FKREFERRINGCOLUMNDESC = "ReferringFKColumn"; // NOI18N
    protected static final String FKREFERREDSCHEMA = "ReferredFKSchema"; // NOI18N
    protected static final String FKREFERREDSCHEMADESC = "ReferredFKSchema"; // NOI18N
    protected static final String FKREFERREDTABLE = "ReferredFKTable"; // NOI18N
    protected static final String FKREFERREDTABLEDESC = "ReferredFKTable"; // NOI18N
    protected static final String FKREFERREDCOLUMN = "ReferredFKColumn"; // NOI18N
    protected static final String FKREFERREDCOLUMNDESC = "ReferredFKColumn"; // NOI18N

    private final NodeDataLookup dataLookup;
    private final ActionRegistry actionRegistry;
    private final NodeRegistry nodeRegistry;
    private final ChildNodeFactory childNodeFactory;
    private final NodeProvider nodeProvider;

    private final List<Property> props = new CopyOnWriteArrayList<Property>();
    private final HashMap<String, Object> propMap = new HashMap<String, Object>();

    public boolean isRemoved = false;


    /**
     * Constructor for nodes without children.
     * 
     * @param dataLookup the data lookup for this node
     */
    public BaseNode(NodeDataLookup dataLookup, String layerEntry, NodeProvider provider) {
        this(Children.LEAF, null, dataLookup, layerEntry, provider);
    }

    /**
     * Constructor for nodes with children.
     * 
     * @param childFactory the child factory used to create children of this node
     * @param dataLookup the data lookup for this node
     */
    public BaseNode(ChildNodeFactory childFactory, NodeDataLookup dataLookup, String layerEntry, NodeProvider provider) {
        this(Children.create(childFactory, true), childFactory, dataLookup, layerEntry, provider);
    }

    /**
     * Private constructor used by the public constructors.
     * 
     * @param children the children of this node
     * @param factory the child factory to use
     * @param lookup the associated lookup
     * @param layerEntry the name of the folder in the xml layer
     */
    private BaseNode(Children children, ChildNodeFactory factory, NodeDataLookup lookup, String layerEntry, NodeProvider provider) {
        super(children, lookup);
        dataLookup = lookup;
        childNodeFactory = factory;
        actionRegistry = new ActionRegistry(layerEntry);
        nodeRegistry = NodeRegistry.create(layerEntry, dataLookup);
        nodeProvider = provider;
    }
    
    /**
     * Initialize the node.  This method is called before the creation process
     * completes so that the sub class can perform any initialization it requires.
     */
    protected abstract void initialize();

    public NodeRegistry getNodeRegistry() {
        return nodeRegistry;
    }

    public synchronized void refresh() {
        nodeRegistry.refresh();
        update();
    }

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
        updateProperties();
    }

    protected void remove() {
        remove(false);
    }

    protected void remove(boolean refreshProvider) {
        isRemoved = true;
        nodeProvider.removeNode(this);
        if (refreshProvider) {
            RequestProcessor.getDefault().post(
                new Runnable() {
                    public void run() {
                        Node parent = getParentNode();
                        if (parent instanceof BaseNode) {
                            ((BaseNode)parent).refresh();
                        }
                    }
                }
            );
        }
    }

    /**
     * Get the list of child nodes.
     * 
     * @return the list of child nodes.
     */
    public Collection<? extends Node> getChildNodesSync() {
        if (childNodeFactory != null) {
            childNodeFactory.refreshSync();
        }
        return nodeRegistry.getNodes();
    }

    public Collection<? extends Node> getChildNodes() {
        return nodeRegistry.getNodes();
    }

    /**
     * Updates the node.
     */
    public void update() { 
        updateProperties();

        if (childNodeFactory != null) {
            childNodeFactory.refresh();
        }
    }

    /**
     * Updates the basic node properties.
     */
    protected void updateProperties() {
        setName(getName());
        setDisplayName(getDisplayName());
        String iconBase = getIconBase();
        if (iconBase != null) {
            setIconBaseWithExtension(iconBase);
        }
    }

    protected void clearProperties() {
        for (Property prop : props) {
            getSheet().get(Sheet.PROPERTIES).remove(prop.getName());
        }
        
        props.clear();
        propMap.clear();
    }

    protected void addProperty(Property nps) {
        props.add(nps);
        getSheet().get(Sheet.PROPERTIES).put(nps);
    }

    protected void addProperty(String name, String desc, Class clazz, boolean writeable, Object value) {
        String propName = NbBundle.getMessage (BaseNode.class, name);

        String propDesc;
        if (desc == null) {
            propDesc = propName;
        } else {
            propDesc = NbBundle.getMessage (BaseNode.class, desc);
        }
        PropertySupport ps = new NodePropertySupport(this, name, clazz, propName, propDesc, writeable);
        props.add(ps);
        propMap.put(ps.getName(), value);

        getSheet().get(Sheet.PROPERTIES).put(ps);
    }

    public void setPropertyValue(Property nps, Object val) {
        propMap.put(nps.getName(), val);
    }

    public Object getPropertyValue(String key) {
        return propMap.get(key);
    }

    public synchronized Collection<Property> getProperties() {
        return Collections.unmodifiableCollection(props);
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        setSheet(sheet);
        return sheet;
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
        return actions.toArray (new Action[actions.size ()]);
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
