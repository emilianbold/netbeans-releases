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

import java.io.Serializable;
import java.util.List;


/**
 * Interface specifies methods needed to manage a TcgComponentType tree. It
 * provides a List view, TableModel view, and a Map view of its
 * TcgComponentType's, and a Tree view of those TcgComponentTypeGroup nested under
 * it.
 *
 * @author Bing Lu
 *
 * @since May 1, 2002
 */
public interface TcgComponentTypeGroup extends Serializable {

    /**
     * Gets the number of TcgComponentType's directly contained in this object
     *
     * @return the number of TcgComponentType's directly contained in this object
     */
    public int getComponentTypeCount();

    /**
     * Gets the TableModel representation of the TcgComponentType's
     *
     * @return The requested TcgComponentType TableModel
     */
    public ListMapTableModel getComponentTypeTableModel();

    /**
     * Gets the number of TcgComponentTypeses directly contained in this object
     *
     * @return the number of TcgComponentTypeses directly contained in this object
     */
    public int getComponentTypeGroupCount();

    /**
     * Gets the parent TcgComponentTypeGroup of this object as descendent
     *
     * @return the parent TcgComponentTypeGroup of this object
     */
    public TcgComponentTypeGroup getParent();

    /**
     * Gets the root TcgComponentTypeGroup of the tree, which holds this object
     *
     * @return the root TcgComponentTypeGroup of the tree, which holds this object
     */
    public TcgComponentTypeGroup getRoot();

    /**
     * Gets the DefaultMutableTreeNode associated with this object
     *
     * @return the DefaultMutableTreeNode associated with this object
     */
    public ListMapTreeNode getTreeNode();

    /**
     * Duplicate the TcgComponentType/TcgComponentTypeGroup tree rooted at this
     * TcgComponentTypeGroup instance.
     *
     * @return DOCUMENT ME!
     */
    public TcgComponentTypeGroup duplicate();

    /**
     * Gets the named TcgComponentType from this object
     *
     * @param componentTypeName the name of the TcgComponentType
     *
     * @return The named TcgComponentType from this object
     */
    TcgComponentType getComponentType(String componentTypeName);

    /**
     * Gets the list of all TcgComponentType of this object
     *
     * @return the TcgComponentType list of the object
     */
    List getComponentTypeList();

    /**
     * Gets the named TcgComponentTypeGroup from this object
     *
     * @param componentTypesName the name of the TcgComponentTypeGroup
     *
     * @return The named TcgComponentTypeGroup from this object
     */
    TcgComponentTypeGroup getComponentTypeGroup(String componentTypesName);

    /**
     * Gets the list of all TcgComponentTypeGroup of this object
     *
     * @return the TcgComponentTypeGroup list of the object
     */
    List getComponentTypeGroupList();

    /**
     * Gets the icon attribute of the TcgComponent object
     *
     * @return The icon value
     */
    String getIconName();

    /**
     * Gets the name attribute of the TcgComponentTypeGroup object
     *
     * @return The name value
     */
    String getName();

    /**
     * Gets the title attribute of the TcgComponent object
     *
     * @return The title value
     */
    String getTitle();

    /**
     * Gets the description attribute of the TcgComponent object
     *
     * @return The description value
     */
    String getDescription();

    /**
     * Adds TcgComponentType componentType to this object at the end of the
     * TcgComponentType list
     *
     * @param componentType The TcgComponentType to add
     */
    void addComponentType(TcgComponentType componentType);

    /**
     * Adds TcgComponentType componentType to this object at the specified
     * index of the TcgComponentType list
     *
     * @param index The index where the component to reside
     * @param componentType The TcgComponentType to add
     */
    void addComponentType(int index, TcgComponentType componentType);

    /**
     * Adds TcgComponentTypeGroup componentTypes to this object at the end of the
     * TcgComponentTypeGroup list
     *
     * @param componentTypes The feature to be added to the TcgComponentTypeGroup
     *        attribute
     */
    void addComponentTypeGroup(TcgComponentTypeGroup componentTypeGroup);

    /**
     * Adds TcgComponentTypeGroup componentTypes to this object at the specified
     * index of the TcgComponentType list
     *
     * @param index The index where the component to reside
     * @param componentTypes The feature to be added to the TcgComponentTypeGroup
     *        attribute
     */
    void addComponentTypeGroup(int index, TcgComponentTypeGroup componentTypeGroup);

    /**
     * Clears both the TcgComponentTypeGroup list and the TcgComponentType list
     */
    void clear();

    /**
     * Removes the named TcgComponentType from this object
     *
     * @param componentTypeName name identifies the TcgComponentType
     */
    void removeComponentType(String componentTypeName);

    /**
     * Removes the named TcgComponentTypeGroup from this object
     *
     * @param componentTypesName name identifies the TcgComponentTypeGroup
     */
    void removeComponentTypeGroup(String componentTypeGroupName);
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
