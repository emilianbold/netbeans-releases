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

import java.util.List;
import java.util.Vector;


/**
 * A concrete class that implements TcgComponentTypeGroup
 *
 * @author Bing Lu
 *
 * @since May 1, 2002
 */
class TcgComponentTypeGroupImpl

    implements TcgComponentTypeGroup {

    private static final long serialVersionUID = -6599746782556252393L;    
    
    private transient CTSTreeNode mTreeNode;
    private transient TcgComponentTypeTableModel mComponentTypeTableModel;
    private ListMap mComponentTypeListMap;
    private ListMap mComponentTypeGroupAndTypeListMap;
    private ListMap mComponentTypeGroupListMap;
    private String mIconName;
    private String mName;
    private String mTitle;
    private String mDescription;

    /**
     * DOCUMENT ME!
     *
     * @param name
     * @param title
     * @param iconName
     *
     * @todo Document this constructor
     */
    public TcgComponentTypeGroupImpl(String name, String title, String description, String iconName) {
        mName = name;
        mTitle = title;
        mDescription = description;
        mIconName = iconName;
        mComponentTypeListMap = new ArrayHashMap();
        mComponentTypeGroupListMap = new ArrayHashMap();
        mComponentTypeGroupAndTypeListMap = new ArrayHashMap();
        mTreeNode = new CTSTreeNode();
        mComponentTypeTableModel = new TcgComponentTypeTableModel();
    }

    /**
     * Gets the named TcgComponentType from this object
     *
     * @param componentTypeName the name of the TcgComponentType
     *
     * @return The named TcgComponentType from this object
     */
    public TcgComponentType getComponentType(String componentTypeName) {
        return (TcgComponentType) mComponentTypeListMap.get(componentTypeName);
    }

    /**
     * Gets the number of TcgComponentTypeGroups directly contained in this object
     *
     * @return the number of TcgComponentTypeGroups directly contained in this object
     */
    public int getComponentTypeCount() {
        return mComponentTypeListMap.size();
    }

    /**
     * Gets the list of all TcgComponentType of this object
     *
     * @return the TcgComponentType list of the object
     */
    public List getComponentTypeList() {
        return mComponentTypeListMap.getValueList();
    }

    /**
     * Gets the TableModel representation of the TcgComponentType's
     *
     * @return The requested TcgComponentType TableModel
     */
    public ListMapTableModel getComponentTypeTableModel() {
        return mComponentTypeTableModel;
    }

    /**
     * Gets the named TcgComponentTypeGroup from this object
     *
     * @param componentTypesName the name of the TcgComponentTypeGroup
     *
     * @return The named TcgComponentTypeGroup from this object
     */
    public TcgComponentTypeGroup getComponentTypeGroup(String componentTypesName) {
        return (TcgComponentTypeGroup) mComponentTypeGroupListMap
            .get(componentTypesName);
    }

    /**
     * Gets the number of TcgComponentTypeGroups directly contained in this object
     *
     * @return the number of TcgComponentTypeGroups directly contained in this object
     */
    public int getComponentTypeGroupCount() {
        return mComponentTypeGroupListMap.size();
    }

    /**
     * Gets the list of all TcgComponentTypeGroup of this object
     *
     * @return the TcgComponentTypeGroup list of the object
     */
    public List getComponentTypeGroupList() {
        return mComponentTypeGroupListMap.getValueList();
    }

    /**
     * Gets the icon attribute of the TcgComponent object
     *
     * @return The icon value
     */
    public String getIconName() {
        return mIconName;
    }

    /**
     * Gets the name attribute of the TcgComponentTypeGroup object
     *
     * @return The name value
     */
    public String getName() {
        return mName;
    }

    /**
     * Gets the parent TcgComponentTypeGroup of this object as descendent
     *
     * @return the parent TcgComponentTypeGroup of this object
     */
    public TcgComponentTypeGroup getParent() {

        ListMapTreeNode p = (ListMapTreeNode) getTreeNode().getParent();
        TcgComponentTypeGroup c = (TcgComponentTypeGroup) p.getUserObject();

        return c;
    }

    /**
     * Gets the root TcgComponentTypeGroup of the tree, which holds this object
     *
     * @return the root TcgComponentTypeGroup of the tree, which holds this object
     */
    public TcgComponentTypeGroup getRoot() {

        ListMapTreeNode r = (ListMapTreeNode) getTreeNode().getRoot();
        TcgComponentTypeGroup c = (TcgComponentTypeGroup) r.getUserObject();

        return c;
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
     * Gets the description attribute of the TcgComponent object
     *
     * @return The description value
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Gets the DefaultMutableTreeNode associated with this object
     *
     * @return the DefaultMutableTreeNode associated with this object
     */
    public ListMapTreeNode getTreeNode() {
        return mTreeNode;
    }

    /**
     * Adds TcgComponentType componentType to this object at the end of the
     * TcgComponentType list
     *
     * @param componentType The TcgComponentType to add
     */
    public void addComponentType(TcgComponentType componentType) {

        getComponentTypeTableModel().addRow(new Object[]{
            componentType.getName(),
            componentType });
    }

    /**
     * Adds TcgComponentType componentType to this object at the specified
     * index of the TcgComponentType list
     *
     * @param index The index where the component to reside
     * @param componentType The TcgComponentType to add
     */
    public void addComponentType(int index, TcgComponentType componentType) {

        getComponentTypeTableModel().insertRow(index,
                                               new Object[]{
                                                   componentType.getName(),
                                                   componentType });
    }

    /**
     * Adds TcgComponentTypeGroup componentTypes to this object at the end of the
     * TcgComponentTypeGroup list
     *
     * @param componentTypes The feature to be added to the TcgComponentTypeGroup
     *        attribute
     */
    public void addComponentTypeGroup(TcgComponentTypeGroup componentTypes) {
        getTreeNode().add(componentTypes.getTreeNode());
        mComponentTypeGroupListMap.put(componentTypes.getName(), componentTypes);
    }

    /**
     * Adds TcgComponentTypeGroup componentTypes to this object at the specified
     * index of the TcgComponentType list
     *
     * @param index The index where the component to reside
     * @param componentTypes The feature to be added to the TcgComponentTypeGroup
     *        attribute
     */
    public void addComponentTypeGroup(int index, TcgComponentTypeGroup componentTypes) {

        getTreeNode().insert(componentTypes.getTreeNode(), index);
        mComponentTypeGroupListMap.put(index, componentTypes.getName(),
                                   componentTypes);
    }

    /**
     * Clears both the TcgComponentTypeGroup list and the TcgComponentType list
     */
    public void clear() {

        mComponentTypeListMap.clear();
        mComponentTypeGroupListMap.clear();
        getTreeNode().removeAllChildren();
    }

    /**
     * Duplicate the TcgComponentType/TcgComponentTypeGroup tree rooted at this
     * TcgComponentTypeGroup instance.
     *
     * @return DOCUMENT ME!
     */
    public TcgComponentTypeGroup duplicate() {

        TcgComponentTypeGroup dup = new TcgComponentTypeGroupImpl(mName, mTitle, mDescription, mIconName);
        List ctl = getComponentTypeList();

        for (int i = 0, j = ctl.size(); i < j; i++) {
            dup.addComponentType(((TcgComponentType) ctl.get(i)).duplicate());
        }

        List ctsl = getComponentTypeGroupList();

        for (int i = 0, k = ctsl.size(); i < k; i++) {
            dup.addComponentTypeGroup(((TcgComponentTypeGroup) ctsl.get(i))
                .duplicate());
        }

        return dup;
    }

    /**
     * Removes the named TcgComponentType from this object
     *
     * @param componentTypeName name identifies the TcgComponentType
     */
    public void removeComponentType(String componentTypeName) {

        int index =
            mComponentTypeListMap.getKeyList().indexOf(componentTypeName);

        if (index >= 0) {
            mComponentTypeTableModel.removeRow(index);
        }
    }

    /**
     * Removes the named TcgComponentTypeGroup from this object
     *
     * @param componentTypesName name identifies the TcgComponentTypeGroup
     */
    public void removeComponentTypeGroup(String componentTypesName) {

        TcgComponentTypeGroup componentTypes =
            (TcgComponentTypeGroup) mComponentTypeGroupListMap.get(componentTypesName);

        if (componentTypes != null) {
            mComponentTypeGroupListMap.remove(componentTypesName);
            getTreeNode().remove(componentTypes.getTreeNode());
        }
    }

    private class CTSTreeNode
        extends TcgComponentTypeTreeNode {

        /**
         * DOCUMENT ME!
         *
         * @todo Document this constructor
         */
        public CTSTreeNode() {
            super(mComponentTypeGroupAndTypeListMap, mName,
                  TcgComponentTypeGroupImpl.this, true);
        }
    }

    private class TcgComponentTypeTableModel
        extends ListMapTableModel {

        /**
         * DOCUMENT ME!
         *
         * @todo Document this constructor
         */
        public TcgComponentTypeTableModel() {
            super(mComponentTypeListMap);
        }

        /*
         *  Override ListMapDataModel's method to prevent objects from being changed
         */

        /**
         * DOCUMENT ME!
         *
         * @param aValue value at
         * @param row value at
         * @param column value at
         *
         * @todo Document: Setter for ValueAt attribute of the
         *       TcgComponentTypeTableModel object
         */
        public void setValueAt(Object aValue, int row, int column) {
        }

        /*
         *  Override ListMapDataModel's method to prevent objects of wrong type from being added
         */

        /**
         * Adds a feature to the Row attribute of the TcgComponentTypeTableModel
         * object
         *
         * @param rowData The feature to be added to the Row attribute
         */
        public void addRow(Vector rowData) {

            if ((rowData != null) && (rowData.size() >= 2)) {
                Object k = rowData.get(0);
                Object v = rowData.get(1);

                if ((k instanceof String)
                        && (v instanceof TcgComponentTypeImpl)) {
                    super.addRow(rowData);

                    TcgComponentType ct = (TcgComponentType) v;

                    getTreeNode().add(ct.getTreeNode());
                }
            }
        }

        /*
         *  Override ListMapDataModel's method to prevent objects of wrong type from being added
         */

        /**
         * Adds a feature to the Row attribute of the TcgComponentTypeTableModel
         * object
         *
         * @param rowData The feature to be added to the Row attribute
         */
        public void addRow(Object[] rowData) {

            if ((rowData != null) && (rowData.length >= 2)) {
                Object k = rowData[0];
                Object v = rowData[1];

                if ((k instanceof String)
                        && (v instanceof TcgComponentTypeImpl)) {
                    super.addRow(rowData);

                    TcgComponentType ct = (TcgComponentType) v;

                    getTreeNode().add(ct.getTreeNode());
                }
            }
        }

        /*
         *  Override ListMapDataModel's method to prevent objects of wrong type from being added
         */

        /**
         * DOCUMENT ME!
         *
         * @param idx
         * @param rowData
         *
         * @todo Document this method
         */
        public void insertRow(int idx, Vector rowData) {

            if ((rowData != null) && (rowData.size() >= 2)) {
                Object k = rowData.get(0);
                Object v = rowData.get(1);

                if ((k instanceof String)
                        && (v instanceof TcgComponentTypeImpl)) {
                    super.insertRow(idx, rowData);

                    TcgComponentType ct = (TcgComponentType) v;

                    getTreeNode().insert(ct.getTreeNode(), idx);
                }
            }
        }

        /*
         *  Override ListMapDataModel's method to prevent objects of wrong type from being added
         */

        /**
         * DOCUMENT ME!
         *
         * @param idx
         * @param rowData
         *
         * @todo Document this method
         */
        public void insertRow(int idx, Object[] rowData) {

            if ((rowData != null) && (rowData.length >= 2)) {
                Object k = rowData[0];
                Object v = rowData[1];

                if ((k instanceof String)
                        && (v instanceof TcgComponentTypeImpl)) {
                    super.insertRow(idx, rowData);

                    TcgComponentType ct = (TcgComponentType) v;

                    getTreeNode().add(ct.getTreeNode());
                }
            }
        }

        /**
         * Override ListMapDataModel's method to remove the TcgComponentType
         * from the tree too.
         *
         * @param row
         */
        public void removeRow(int row) {

            super.removeRow(row);

            TcgComponentType ct =
                (TcgComponentType) getComponentTypeList().get(row);

            if (ct != null) {
                getTreeNode().remove(ct.getTreeNode());
            }
        }
    }
}
