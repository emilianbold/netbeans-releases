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


package org.netbeans.modules.tbls.model;

import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.netbeans.modules.tbls.model.I18nException;
import org.netbeans.modules.tbls.model.ListMapTableModel;
import org.netbeans.modules.tbls.model.ListMapTableModelView;
import org.netbeans.modules.tbls.model.ListMapTreeNode;
import org.netbeans.modules.tbls.model.TcgComponent;
import org.netbeans.modules.tbls.model.TcgComponentType;

/**
 * Class implements TcgComponent and delegates calls to the underlying
 * TcgComponent
 *
 * @author Bing Lu
 *
 * @since May 1, 2002
 */
public class TcgComponentDelegate implements TcgComponent {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(TcgComponentDelegate.class.getName());

    /**
     * DOCUMENT ME!
     */
    protected TcgComponent mComponent;
    
    protected void initialize(TcgComponent component) {
        mComponent = component;
    }
    
    protected TcgComponentDelegate() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param component pds component
     *
     * @todo Document this constructor
     */
    public TcgComponentDelegate(TcgComponent component) {
        initialize(component);
    }

    /**
     * Gets the TcgComponent from this object
     *
     * @param componentName String name identifies the TcgComponent
     *
     * @return The matched TcgComponent object
     */
    public TcgComponent getComponent(String componentName) {
        return mComponent.getComponent(componentName);
    }

    public boolean hasComponent(String componentName) {
        return mComponent.hasComponent(componentName);
    }

    public TcgComponent getComponent(int idx) {
        return mComponent.getComponent(idx);
    }

    /**
     * Gets the number of components directly contained in this object
     *
     * @return the number of components directly contained in this object
     */
    public int getComponentCount() {
        return mComponent.getComponentCount();
    }

    /**
     * Gets the index for this component
     *
     * @return the index
     */
    public int getComponentIndex() {
        return mComponent.getComponentIndex();
    }
    
    /**
     * Gets the index for a component
     *
     * @return the index
     */
    public int getComponentIndex(TcgComponent comp) {
        return mComponent.getComponentIndex(comp);
    }

    /**
     * Gets the list of all components from this object
     *
     * @return List of TcgComponents
     */
    public List getComponentList() {
        return mComponent.getComponentList();
    }
    
    /**
     * Gets the list of all components of the specified type from this object
     *
     * @param componentType the PDS TcgComponent Type
     * @return List of TcgComponents
     */
    public List getComponentListByType(TcgComponentType componentType) {
        return mComponent.getComponentListByType(componentType);
    }

    /**
     * Gets the name attribute of the TcgComponent object
     *
     * @return The name value
     */
    public String getName() {
        return mComponent.getName();
    }

    /**
     * Gets the parent component of this object as descendent
     *
     * @return the parent component of this object
     */
    public TcgComponent getParent() {
        return mComponent.getParent();
    }

    /**
     * Gets the named property from this TcgComponent object
     *
     * @param propertyName the name of the property
     *
     * @return The property value
     */
    public org.netbeans.modules.tbls.model.TcgProperty getProperty(String propertyName) throws I18nException {
        return mComponent.getProperty(propertyName);
    }

    public boolean hasProperty(String propertyName) {
        return mComponent.hasProperty(propertyName);
    }
    
    /**
     * Gets the nubmer of properties contained in this object
     *
     * @return the nubmer of properties contained in this object
     */
    public int getPropertyCount() {
        return mComponent.getPropertyCount();
    }

    /**
     * Gets the list of all properties of this TcgComponent object
     *
     * @return the list of all properties of the TcgComponent object
     */
    public List getPropertyList() {
        return mComponent.getPropertyList();
    }

    /**
     * Gets the TableModel representation of the component's property
     *
     * @return The requested property TableModel
     */
    public ListMapTableModel getPropertyTableModel() {
        return mComponent.getPropertyTableModel();
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
        return mComponent.getPropertyTableModelView(requestedKeys);
    }

    /**
     * Gets the read-only TableModel representation of the component's property
     *
     * @return The readable property TableModel
     */
    public ListMapTableModelView getReadablePropertyTableModelView() {
        return mComponent.getReadablePropertyTableModelView();
    }

    /**
     * Gets the root component of the tree, which holds this object
     *
     * @return the root component of the tree, which holds this object
     */
    public TcgComponent getRoot() {
        return mComponent.getRoot();
    }

    /**
     * Sets the title attribute of the TcgComponent object
     *
     * @param title The new title value
     */
    public void setTitle(String title) {
        mComponent.setTitle(title);
    }

    /**
     * Gets the title attribute of the TcgComponent object
     *
     * @return The title value
     */
    public String getTitle() {
        return mComponent.getTitle();
    }

    /**
     * Gets the MutableTreeNode associated with this object
     *
     * @return the MutableTreeNode associated with this object
     */
    public ListMapTreeNode getTreeNode() {
        return mComponent.getTreeNode();
    }

    /**
     * Gets the type of this TcgComponent object
     *
     * @return The type value
     */
    public TcgComponentType getType() {
        return mComponent.getType();
    }

    /**
     * Validate the TcgComponent using component's validator
     *
     * @return TcgComponentValidationReport
     */
    public org.netbeans.modules.tbls.model.TcgComponentValidationReport validate() {
        return mComponent.validate();
    }

    /**
     * Adds TcgComponent component to this object at the end of the list of
     * children
     *
     * @param component The TcgComponent to add
     */
    public void addComponent(TcgComponent component) {
        mComponent.addComponent(component);
    }

    /**
     * Adds the given TcgComponent component to this object at the specified
     * index
     *
     * @param index The index where the component to reside
     * @param component The TcgComponent to add
     */
    public void addComponent(int index, TcgComponent component) {
        mComponent.addComponent(index, component);
    }

    /**
     * Removes all the TcgComponents from this object
     */
    public void clear() {
        mComponent.clear();
    }

    /**
     * Duplicate this TcgComponent instance
     *
     * @param componentName Name for the cloned TcgComponent
     *
     * @return The cloned TcgComponent
     */
    public TcgComponent duplicate(String componentName) {
        return mComponent.duplicate(componentName);
    }

    /**
     * Removes the named TcgComponent from this object
     *
     * @param componentName String name identifies the component
     */
    public TcgComponent removeComponent(String componentName) {
        return mComponent.removeComponent(componentName);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @todo Document this method
     */
    public String toString() {
        return mComponent.toString();
    }

    /**
     * converts the component to an xml Elment
     *
     * @param doc  Description of the Parameter
     *
     * @return xml element for component
     */
    public Element toXml(Document doc) {
        return mComponent.toXml(doc);
    }
    
    public String toXml() {
        return mComponent.toXml();
    }
    
    /**
     * Markers are used to annotate the component tree during code generation
     */
    public Map getMarkers() {
        return mComponent.getMarkers();
    }

    public void setMarker(String key, Object value) {
        mComponent.setMarker(key, value);
    }

    public boolean hasMarker(String key) {
        return mComponent.hasMarker(key);
    }
    
    public Object getMarker(String key) {
        return mComponent.getMarker(key);
    }

    public void clearMarker(String key) {
        mComponent.clearMarker(key);
    }
    
    public void clearMarkers() {
        mComponent.clearMarkers();
    }
    
    //=========================================================================
    
    public TcgComponent getNextSibling() {
        return mComponent.getNextSibling();
    }

    public TcgComponent getPrevSibling() {
        return mComponent.getPrevSibling();
    }

    public TcgComponent getFirstChild() {
        return mComponent.getFirstChild();
    }

    public TcgComponent getLastChild() {
        return mComponent.getLastChild();
    }
    
    //=========================================================================
    public PropertyChangeSupport getPropertyChangeSupport() {
        return mComponent.getPropertyChangeSupport();
    }
    
}
