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


package org.netbeans.modules.iep.model.lib;

import java.beans.PropertyChangeSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A concrete class that implements TcgComponent
 *
 * @author Bing Lu
 *
 * @since May 1, 2005
 */
class TcgComponentImpl implements TcgComponent {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(TcgComponentImpl.class.getName());
    
    private static final long serialVersionUID = -3569766482156152493L;
    
    private transient TcgComponentTreeNode mComponentTreeNode;
    private ListMap mComponentListMap;
    private ListMap mPropertyListMap;
    private TcgComponentType mComponentType;
    private PropertyChangeSupport mPropertyChangeSupport = null;
    private transient TcgPropertyTableModel mPropertyTableModel;
    private String mName;
    private String mTitle;

    /**
     * Constructor for the TcgComponent object
     *
     * @param name Description of the Parameter
     * @param title Description of the Parameter
     * @param componentType Description of the Parameter
     */
    TcgComponentImpl(String name, String title,
                     TcgComponentType componentType) {

        mName = name;
        mTitle = title;
        mComponentType = componentType;
        mPropertyListMap = new ArrayHashMap();

        List ptList = componentType.getPropertyTypeList();

        for (int i = 0, j = ptList.size(); i < j; i++) {
            TcgPropertyType pt = (TcgPropertyType) ptList.get(i);
            TcgProperty p = pt.newTcgProperty(this);
            mPropertyListMap.put(p.getName(), p);
        }

        mComponentListMap = new ArrayHashMap();
        mComponentTreeNode = new TcgComponentTreeNode();
        mPropertyTableModel = new TcgPropertyTableModel();
        mPropertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Gets the TcgComponent from this object
     *
     * @param componentName String name identifies the TcgComponent
     *
     * @return The matched TcgComponent object
     */
    public TcgComponent getComponent(String componentName) {
        return (TcgComponent) mComponentListMap.get(componentName);
    }
    
    public boolean hasComponent(String componentName) {
        return mComponentListMap.get(componentName) != null;
    }

    public TcgComponent getComponent(int idx) {
        return (TcgComponent)mComponentListMap.get(idx);
    }

    /**
     * Gets the number of components directly contained in this object
     *
     * @return the number of components directly contained in this object
     */
    public int getComponentCount() {
        return mComponentListMap.size();
    }

    /**
     * Gets the list of all components from this object
     *
     * @return List of TcgComponents
     */
    public List getComponentList() {
        return mComponentListMap.getValueList();
    }

    /**
     * Gets the list of all components of the specified type from this object
     *
     * @return List of TcgComponents
     */
    public List getComponentListByType(TcgComponentType componentType) {
        
        List returnList = new ArrayList();
        List children = this.getComponentList();
                   
        for (int i = 0, size = children.size(); i < size; i++) {
            TcgComponent component = (TcgComponent) children.get(i);
            if (component.getType() == componentType) {
                returnList.add(component);
            }
            returnList.addAll(component.getComponentListByType(componentType));
        }
        return returnList;
    }
    
    /**
     * Gets the name attribute of the TcgComponent object
     *
     * @return The name value
     */
    public String getName() {
        return mName;
    }

    /**
     * Gets the parent component of this object as descendent
     *
     * @return the parent component of this object as descendent
     */
    public TcgComponent getParent() {

        TreeNode n = getTreeNode().getParent();

        if (n instanceof ListMapTreeNode) {
            ListMapTreeNode p = (ListMapTreeNode) n;
            Object o = p.getUserObject();

            if (o instanceof TcgComponent) {
                return (TcgComponent) o;
            }
        }

        return null;
    }

    /**
     * Gets the named property from this TcgComponent object
     *
     * @param propertyName the name of the property
     *
     * @return The property value
     */
    public TcgProperty getProperty(String propertyName) 
        throws I18nException {
            TcgProperty prop = (TcgProperty) mPropertyListMap.get(propertyName);
            if (prop == null) {
                Object[] params = {propertyName, getTitle()};
                throw new I18nException (
                    "TcgComponentImpl.PROPERTY_NOT_FOUND",
                    "org.netbeans.modules.iep.editor.tcg.model.Bundle",
                    params);
            }
            return prop;
    }

    public boolean hasProperty(String propertyName) {
        TcgProperty prop = (TcgProperty) mPropertyListMap.get(propertyName);
        return (prop != null);
    }        
        
    /**
     * Gets the nubmer of properties contained in this object
     *
     * @return the nubmer of properties contained in this object
     */
    public int getPropertyCount() {
        return mPropertyListMap.size();
    }

    /**
     * Gets the list of all properties of this TcgComponent object
     *
     * @return the list of names of properties of the TcgComponent object
     */
    public List getPropertyList() {
        return mPropertyListMap.getValueList();
    }

    /**
     * Gets the TableModel representation of the component's property
     *
     * @return The requested property TableModel
     */
    public ListMapTableModel getPropertyTableModel() {
        return mPropertyTableModel;
    }

    /**
     * Gets the read-only TableModel representation of the component's
     * property. This TableModel contains only properties that are readable
     * and are found in the requestedKeys list. If requestedKeys is null, all
     * readable properties within this TcgComponent are included in the
     * returned TableModel. In other words, null requestedKeys is same as
     * invoking method getReadablePropertyTableModelView.
     *
     * @param requestedKeys List of readable property names to be included in
     *        the result
     *
     * @return The readable property TableModel
     */
    public ListMapTableModelView getPropertyTableModelView(List requestedKeys) {

        if (requestedKeys == null) {
            return getReadablePropertyTableModelView();
        }

        List includes = new ArrayList();
        List keys = mPropertyListMap.getKeyList();

        for (int i = 0, sz = keys.size(); i < sz; i++) {
            Object aKey = keys.get(i);
            TcgProperty p = (TcgProperty) mPropertyListMap.get(aKey);
            TcgPropertyType ptype = p.getType();

            if (ptype == null) {
                continue;
            }

            if (ptype.isReadable() && (requestedKeys.contains(aKey))) {
                includes.add(p.getName());
            }
        }

        return new ListMapTableModelView(mPropertyListMap, includes);
    }

    /**
     * Gets the read-only TableModel representation of the component's property
     *
     * @return The readable property TableModel
     */
    public ListMapTableModelView getReadablePropertyTableModelView() {

        List includes = new ArrayList();
        List keys = mPropertyListMap.getKeyList();

        for (int i = 0, sz = keys.size(); i < sz; i++) {
            Object aKey = keys.get(i);
            TcgProperty p = (TcgProperty) mPropertyListMap.get(aKey);
            TcgPropertyType ptype = p.getType();

            if (ptype == null) {
                continue;
            }

            if (ptype.isReadable()) {
                includes.add(p.getName());
            }
        }

        return new ListMapTableModelView(mPropertyListMap, includes) {
            /**
             * Returns true for columns > 0 and the property on the row is writable.
             *
             * @param rowIndex the row being queried
             * @param columnIndex the column being queried
             *
             * @return false
             */
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if (columnIndex == 0) {
                    return false;
                }
                TcgProperty prop = (TcgProperty)mPropertyListMap.getValueList().get(rowIndex);
                return prop.getType().isWritable();
            }        
        };
    }

    /**
     * Gets the root component of the tree, which holds this object
     *
     * @return the root component of the tree, which holds this object
     */
    public TcgComponent getRoot() {

        TcgComponent p = getParent();

        if (p == null) {
            return this;
        }

        return p.getRoot();
    }

    /**
     * Sets the title attribute of the TcgComponent object
     *
     * @param title The new title value
     */
    public void setTitle(String title) {
        mTitle = title;
    }

    /**
     * Gets the title attribute of the TcgComponent object
     *
     * @return The title value
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Gets the ControlledMutableTreeNode associated with this object
     *
     * @return the ControlledMutableTreeNode associated with this object
     */
    public ListMapTreeNode getTreeNode() {
        return mComponentTreeNode;
    }

    /**
     * Gets the type of this TcgComponent object
     *
     * @return The type value
     */
    public TcgComponentType getType() {
        return mComponentType;
    }

    /**
     * Validate the TcgComponent using component's validator
     *
     * @return TcgComponentValidationReport
     */
    public TcgComponentValidationReport validate() {
        TcgComponentValidator validator = mComponentType.getValidator();
        return validator.validate(this);
    }

    /**
     * Adds TcgComponent component to this object at the end of the list of
     * children
     *
     * @param component The TcgComponent to add
     */
    public void addComponent(TcgComponent component) {

        int cnt = getTreeNode().getChildCount();

        getTreeNode().insert(component.getTreeNode(), cnt);
    }

    /**
     * Adds the given TcgComponent component to this object at the specified
     * index
     *
     * @param index The index where the component to reside
     * @param component The TcgComponent to add
     */
    public void addComponent(int index, TcgComponent component) {
        getTreeNode().insert(component.getTreeNode(), index);
    }

    /**
     * Removes all the TcgComponents from this object
     */
    public void clear() {
        getTreeNode().removeAllChildren();
    }

    /**
     * Duplicate this TcgComponent instance
     *
     * @param componentName Name for the cloned TcgComponent
     *
     * @return The cloned TcgComponent
     */
    public TcgComponent duplicate(String componentName) {
        return mComponentType.duplicate(componentName, this);
    }

    /**
     * Removes the named TcgComponent from this object
     *
     * @param componentName String name identifies the component
     */
    public TcgComponent removeComponent(String componentName) {

        TcgComponent component =
            (TcgComponent) mComponentListMap.get(componentName);

        if (component != null) {
            getTreeNode().remove(component.getTreeNode());
        }
        return component;
    }

    /**
     * Overrides Object's
     *
     * @return DOCUMENT ME!
     */
    public String toString() {

        StringBuffer sb = new StringBuffer();

        sb.append("name=" + getName() + ", " + "title=" + getTitle() + "\n");

        List pList = getPropertyList();

        for (int i = 0, j = pList.size(); i < j; i++) {
            TcgProperty p = (TcgProperty) pList.get(i);

            sb.append("\t" + "name=" + p.getType().getName() + ", " + "value="
                      + p.getValue() + "\n");
        }

        return sb.toString();
    }

    /**
     * converts the component back to xml
     *
     * @param doc Description of the Parameter
     *
     * @return xml element for component
     */
    public Element toXml(Document doc) {

        // Create a roor node
        Element rootNode = doc.createElement("component");

        // Added Name as attribute
        rootNode.setAttribute("name", mName);

        // Aded Title as attribute
        rootNode.setAttribute("title", mTitle);

        // Aded Type as attribute
        rootNode.setAttribute("type", mComponentType.getPath());

        // Lets check out if this node has any property
        List propList = getPropertyList();

        for (int i = 0, j = propList.size(); i < j; i++) {
            TcgProperty pdsproperty = (TcgProperty) propList.get(i);
            TcgPropertyType propertyType = pdsproperty.getType();
            if (!propertyType.isTransient()) {
                Element propertyNode = doc.createElement("property");
    
                // Added Name as attribute
                propertyNode.setAttribute("name", pdsproperty.getName());
    
                // Aded Value as attribute
                propertyNode.setAttribute("value", pdsproperty.getStringValue());
    
                // Now add this elemet to a parent
                rootNode.appendChild(propertyNode);
            }
        }

        /*
         *  Lets check out if this node has any children if it does then recurse
         *  until we hit the bottom
         */
        List compList = getComponentList();

        for (int i = 0, j = compList.size(); i < j; i++) {
            TcgComponent pdscomponent = (TcgComponent) compList.get(i);
            Element childNode = pdscomponent.toXml(doc);

            rootNode.appendChild(childNode);
        }

        return rootNode;
    }

    /**
     * Gets the index for this component
     *
     * @return the index
     */
    public int getComponentIndex() {
        TcgComponent parent = getParent();
        if (parent == null) {
            return -1;
        }
        return parent.getComponentIndex(this);
    }     
    
    /** Gets the list of all components from this object
     *
     * @return List of TcgComponents
     */
    public int getComponentIndex(TcgComponent comp) {
        if (comp == null) {
            return -1;
        }
        return mComponentListMap.getKeyList().indexOf(comp.getName());
    }
    
    /**
     * This class ...
     *
     * @author Bing Lu
     *
     * @since June 17, 2005
     */
    private class TcgComponentTreeNode
        extends ListMapTreeNode {

        /**
         * Constructor for the TcgComponentTreeNode object
         */
        public TcgComponentTreeNode() {
            super(mComponentListMap, mName, TcgComponentImpl.this,
                  mComponentType.getAllowsChildren());
        }

        /*
         *  Override ListMapTreeNode's method to prevent objects of wrong type from being set
         */

        /**
         * Sets the parent attribute of the TcgComponentTreeNode object
         *
         * @param newParent The new parent value
         */
        public void setParent(MutableTreeNode newParent) {

            if (newParent instanceof TcgComponentTreeNode) {
                super.setParent(newParent);
            }
        }

        /*
         *  Override ListMapTreeNode's method to prevent objects of wrong type from being added
         */

        /**
         * Description of the Method
         *
         * @param child Description of the Parameter
         */
        public void add(MutableTreeNode child) {

            if (child instanceof TcgComponentTreeNode) {
                super.add((TcgComponentTreeNode) child);
            }
        }

        /*
         *  Override ListMapTreeNode's method to prevent objects of wrong type from being added
         */

        /**
         * Description of the Method
         *
         * @param child Description of the Parameter
         * @param index Description of the Parameter
         */
        public void insert(MutableTreeNode child, int index) {

            if (child instanceof TcgComponentTreeNode) {
                super.insert(child, index);
            }
        }
    }

    /**
     * This class ...
     *
     * @author Bing Lu
     *
     * @since June 17, 2005
     */
    private class TcgPropertyTableModel
        extends ListMapTableModel {

        /**
         * Constructor for the TcgPropertyTableModel object
         */
        public TcgPropertyTableModel() {
            super(mPropertyListMap);
        }

        /*
         *  Override ListMapDataModel's method to prevent properties from being changed
         */

        /**
         * Sets the valueAt attribute of the TcgPropertyTableModel object
         *
         * @param aValue The new valueAt value
         * @param row The new valueAt value
         * @param column The new valueAt value
         */
        public void setValueAt(Object aValue, int row, int column) {
        }

        /*
         *  Override ListMapDataModel's method to prevent properties from being added
         */

        /**
         * Adds a feature to the Row attribute of the TcgPropertyTableModel object
         *
         * @param rowData The feature to be added to the Row attribute
         */
        public void addRow(Vector rowData) {
        }

        /*
         *  Override ListMapDataModel's method to prevent properties from being added
         */

        /**
         * Adds a feature to the Row attribute of the TcgPropertyTableModel object
         *
         * @param rowData The feature to be added to the Row attribute
         */
        public void addRow(Object[] rowData) {
        }

        /*
         *  Override ListMapDataModel's method to prevent properties from being added
         */

        /**
         * Description of the Method
         *
         * @param idx Description of the Parameter
         * @param rowData Description of the Parameter
         */
        public void insertRow(int idx, Vector rowData) {
        }

        /*
         *  Override ListMapDataModel's method to prevent properties from being added
         */

        /**
         * Description of the Method
         *
         * @param idx Description of the Parameter
         * @param rowData Description of the Parameter
         */
        public void insertRow(int idx, Object[] rowData) {
        }

        /**
         * Returns true for columns > 0 and the property on the row is writable.
         *
         * @param rowIndex the row being queried
         * @param columnIndex the column being queried
         *
         * @return false
         */
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return false;
            }
            TcgProperty prop = (TcgProperty)mPropertyListMap.getValueList().get(rowIndex);
            return prop.getType().isWritable();
        }        
            
    }
        
    // TODO String encoding, boolean omitXMLDeclaration)
    public String toXml() { 
        String ret = null;
        try {
            Document document = DOMUtil.createDocument(true);
            Element xml = toXml(document);
            ret = DOMUtil.toXML(xml, "UTF-8", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
    
    public String asPath()
        throws I18nException {
        StringBuffer ret = new StringBuffer();
        TcgComponent curr = this;
        TcgComponent parent = curr.getParent();
        while (curr != null) {
            TcgProperty propId = curr.getProperty("id");
            if (propId == null) {
                break;
            }
            if (parent != null) {
                ret.insert(0,
                    "/" + propId.getValue() +
                    "[" + parent.getComponentIndex(curr) + "]");
            } else {
                ret.insert(0,
                    "/" + propId.getValue());
                break;
            }
            curr = parent;
            parent = curr.getParent();
        }
        return ret.toString();
    }
        
    /**
     * Markers are used to annotate the component tree 
     */
        
    private transient Map markers = new java.util.HashMap();
    
    public Map getMarkers() {
        return markers;
    }

    public void setMarker(String key, Object value) {
        markers.put(key, value);
    }

    public boolean hasMarker(String key) {
        return markers.containsKey(key);
    }
    
    public Object getMarker(String key) {
        return markers.get(key);
    }

    public void clearMarker(String key) {
        markers.remove(key);
    }
    
    public void clearMarkers() {
        markers.clear();
    }
    
    //=========================================================================
    
    public TcgComponent getNextSibling() {
        TcgComponent ret = null;
        if (getParent() != null) {
            int compIndex = getComponentIndex();
            List siblings = getParent().getComponentList();
            if (compIndex+1 < siblings.size()) {
                ret = (TcgComponent) siblings.get(compIndex+1);
            }
        }
        return ret;
    }

    public TcgComponent getPrevSibling() {
        TcgComponent ret = null;
        if (getParent() != null) {
            int compIndex = getComponentIndex();
            List siblings = getParent().getComponentList();
            if (compIndex-1 >= 0) {
                ret = (TcgComponent) siblings.get(compIndex-1);
            }
        }
        return ret;
    }

    public TcgComponent getFirstChild() {
        TcgComponent ret = null;
        List children = getComponentList();
        if (children.size() > 0) {
            ret = (TcgComponent) children.get(0);
        }
        return ret;
    }

    public TcgComponent getLastChild() {
        TcgComponent ret = null;
        List children = getComponentList();
        if (children.size() > 0) {
            ret = (TcgComponent) children.get(children.size()-1);
        }
        return ret;
    }
    

    public PropertyChangeSupport getPropertyChangeSupport() {
        return mPropertyChangeSupport;
    }
    
}
