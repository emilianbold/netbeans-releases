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
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Interface specifies methods needed to access a component from ui and/or from
 * velocity templates. It provides a List view, TableModel view, and a Map
 * view of its properties, and a Tree view of its sub components.
 *
 * @author Bing Lu
 *
 * @since May 1, 2002
 */
public interface TcgComponent extends TcgModelConstants, Serializable {

    /**
     * Gets the TcgComponent from this object
     *
     * @param componentName String name identifies the TcgComponent
     *
     * @return The matched TcgComponent object
     */
    public TcgComponent getComponent(String componentName);
    
    public boolean hasComponent(String componentName);

    public TcgComponent getComponent(int idx);

    /**
     * Gets the number of components directly contained in this object
     *
     * @return the number of components directly contained in this object
     */
    public int getComponentCount();

    /**
     * Gets the index for this component
     *
     * @return the index
     */
    public int getComponentIndex();    
    
    /**
     * Gets the index for a component
     *
     * @return the index
     */
    public int getComponentIndex(TcgComponent comp);
    
    /**
     * Gets the list of all components from this object
     *
     * @return List of TcgComponents
     */
    public List getComponentList();

    /**
     * Gets the list of all components of the specified type from this object
     *
     * @param componentType the PDS TcgComponent Type
     *
     * @return List of TcgComponents
     */
    public List getComponentListByType(TcgComponentType componentType);

    /**
     * Gets the name attribute of the TcgComponent object
     *
     * @return The name value
     */
    public String getName();

    /**
     * Gets the parent component of this object as descendent
     *
     * @return the parent component of this object
     */
    public TcgComponent getParent();

    /**
     * Gets the named property from this TcgComponent object
     *
     * @param propertyName the name of the property
     * @throws I18nException if property not found
     * @return The property value
     */
    public TcgProperty getProperty(String propertyName) throws I18nException;

    public boolean hasProperty(String propertyName);
    
    /**
     * Gets the nubmer of properties contained in this object
     *
     * @return the nubmer of properties contained in this object
     */
    public int getPropertyCount();

    /**
     * Gets the list of all properties of this TcgComponent object
     *
     * @return the list of all properties of the TcgComponent object
     */
    public List getPropertyList();

    /**
     * Gets the TableModel representation of the component's property
     *
     * @return The requested property TableModel
     */
    public ListMapTableModel getPropertyTableModel();

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
    public ListMapTableModelView getPropertyTableModelView(List requestedKeys);

    /**
     * Gets the read-only TableModel representation of the component's property
     *
     * @return The readable property TableModel
     */
    public ListMapTableModelView getReadablePropertyTableModelView();

    /**
     * Gets the root component of the tree, which holds this object
     *
     * @return the root component of the tree, which holds this object
     */
    public TcgComponent getRoot();

    /**
     * Sets the title attribute of the TcgComponent object
     *
     * @param title The new title value
     */
    public void setTitle(String title);

    /**
     * Gets the title attribute of the TcgComponent object
     *
     * @return The title value
     */
    public String getTitle();

    /**
     * Gets the MutableTreeNode associated with this object
     *
     * @return the MutableTreeNode associated with this object
     */
    public ListMapTreeNode getTreeNode();

    /**
     * Gets the type of this TcgComponent object
     *
     * @return The type value
     */
    public TcgComponentType getType();

    /**
     * Validate the TcgComponent using component's validator
     *
     * @return TcgComponentValidationReport
     */
    public TcgComponentValidationReport validate();

    /**
     * Adds TcgComponent component to this object at the end of the list of
     * children
     *
     * @param component The TcgComponent to add
     */
    public void addComponent(TcgComponent component);

    /**
     * Adds the given TcgComponent component to this object at the specified
     * index
     *
     * @param index The index where the component to reside
     * @param component The TcgComponent to add
     */
    public void addComponent(int index, TcgComponent component);

    /**
     * Removes all the TcgComponents from this object
     */
    public void clear();

    /**
     * Duplicate this TcgComponent instance
     *
     * @param componentName Name for the cloned TcgComponent
     *
     * @return The cloned TcgComponent
     */
    public TcgComponent duplicate(String componentName);

    /**
     * Removes the named TcgComponent from this object
     *
     * @param componentName String name identifies the component
     */
    public TcgComponent removeComponent(String componentName);

    /**
     * converts the component to an xml Elment
     *
     * @param doc  Description of the Parameter
     *
     * @return xml element for component
     */
    public Element toXml(Document doc);
    
    public String toXml();
    
    /**
     * Markers are used to annotate the component tree 
     */
    
    public Map getMarkers();

    public void setMarker(String key, Object value);

    public boolean hasMarker(String key);
    
    public Object getMarker(String key);

    public void clearMarker(String key);
    
    public void clearMarkers();

    //=========================================================================
    
    public TcgComponent getNextSibling();

    public TcgComponent getPrevSibling();

    public TcgComponent getFirstChild();

    public TcgComponent getLastChild();
    
    //=========================================================================
    public PropertyChangeSupport getPropertyChangeSupport();
    
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
