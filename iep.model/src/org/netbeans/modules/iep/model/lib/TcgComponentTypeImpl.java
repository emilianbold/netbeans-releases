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

import javax.swing.ImageIcon;
import java.util.List;
import java.util.ArrayList;


import java.io.Serializable;


/**
 * Concrete class implementing TcgComponentType interface which represents a
 * component type. For example, Tag_Font, Tag_Text, etc...
 * TcgComponentTypeImpl should be referenced by Factory class only.
 * Clients/applications access is through the interface.
 *
 * @author Bing Lu
 *
 * @since April 30, 2002
 */
class TcgComponentTypeImpl
        implements TcgComponentType {
    
    private static final long serialVersionUID = -1529726422156252393L;
    
    /**
     * Description of the Field
     */
    private ListMap mChildTypes = new ArrayHashMap();
    
    /**
     * Description of the Field
     */
    private ListMap mCodeTypes = new ArrayHashMap();
    
    /**
     * Description of the Field
     */
    private ListMap mPropertyTypes = new ArrayHashMap();
    
    /**
     * The tree node.
     */
    private transient ListMapTreeNode mTreeNode = null;
    
    /**
     * Description of the Field
     */
    private String mIconName = null;
    
    private ImageIcon mIcon = null;
    
    /**
     * Description of the Field
     */
    private String mName = null;
    
    /**
     * Description of the Field
     */
    private String mPath = null;
    
    /**
     * Description of the Field
     */
    private String mTitle = null;
    
    private String mDescription = null;
    
    /**
     * Description of the Field
     */
    private boolean mAllowsChildren;
    
    /**
     * visible attribute
     */
    private boolean mVisible;
    
    /**
     * component validator
     */
    private transient TcgComponentValidator mValidator;
    
    /**
     * Sort TcgPropertyTypes in alphabetical order, then move the lname property type
     * to the beginning.
     */
    public static final PTComparator PT_COMPARATOR = new PTComparator();
    
    /**
     * Constructor for the TcgComponentType object
     *
     * @param name String name value
     * @param path Description of the Parameter
     * @param title String title value
     * @param iconName String icon file name
     * @param allowsChildren Description of the Parameter
     * @param visible
     * @param codeTypeList Array of TcgCodeType
     * @param propTypeList Array of TcgPropertyType
     * @param childTypeList Description of the Parameter
     */
    TcgComponentTypeImpl(String name, String path, String title, String description,
            String iconName, boolean allowsChildren,
            boolean visible, TcgCodeType[] codeTypeList,
            TcgPropertyType[] propTypeList,
            TcgComponentType[] childTypeList,
            TcgComponentValidator validator) {
        
        mName = name;
        mPath = path;
        mTitle = title;
        mDescription = description;
        mIconName = iconName;
        mAllowsChildren = allowsChildren;
        mVisible = visible;
        
        if (codeTypeList != null) {
            for (int i = 0; i < codeTypeList.length; i++) {
                mCodeTypes.put(codeTypeList[i].getName(), codeTypeList[i]);
            }
        }
        
        if (propTypeList != null) {
            //java.util.Arrays.sort(propTypeList, PT_COMPARATOR);
            for (int j = 0; j < propTypeList.length; j++) {
                mPropertyTypes.put(propTypeList[j].getName(), propTypeList[j]);
            }
        }
        
        if (childTypeList != null) {
            for (int k = 0; k < childTypeList.length; k++) {
                mChildTypes.put(childTypeList[k].getName(), childTypeList[k]);
            }
        }
        
        mTreeNode = new CTTreeNode();
        
        if (mIconName != null && !mIconName.equals("")) {
            mIcon = ImageUtil.getImageIcon(mIconName);
        } else {
            mIconName = TcgModelConstants.UNKNOWN_ICON_NAME;
            mIcon = TcgModelConstants.UNKNOWN_ICON;
        }
        
        mValidator = validator;
    }
    
    /**
     * Gets the allowsChildren attribute of the TcgComponentType object
     *
     * @return The allowsChildren value
     */
    public boolean getAllowsChildren() {
        return mAllowsChildren;
    }
    
    /**
     * Gets the codeType attribute of the TcgComponentType object
     *
     * @param codeTypeName Description of the Parameter
     *
     * @return The codeType value
     */
    public TcgCodeType getCodeType(String codeTypeName) {
        return (TcgCodeType) mCodeTypes.get(codeTypeName);
    }
    
    /**
     * Gets the number of TcgCodeType's contained in this object
     *
     * @return the number of TcgCodeType's contained in this object
     */
    public int getCodeTypeCount() {
        return mCodeTypes.size();
    }
    
    /**
     * Gets the codeTypeList attribute of the TcgComponentType object
     *
     * @return The codeTypeList value
     */
    public java.util.List getCodeTypeList() {
        return mCodeTypes.getValueList();
    }
    
    /**
     * Gets the named TcgComponentType from this object
     *
     * @param componentTypeName the name of the TcgComponentType
     *
     * @return The named TcgComponentType from this object
     */
    public TcgComponentType getComponentType(String componentTypeName) {
        return (TcgComponentType) mChildTypes.get(componentTypeName);
    }
    
    /**
     * Gets the number of TcgComponentType's directly contained in this object
     *
     * @return the number of TcgComponentType's directly contained in this object
     */
    public int getComponentTypeCount() {
        return mChildTypes.size();
    }
    
    /**
     * Gets the list of all TcgComponentType of this object
     *
     * @return the TcgComponentType list of the object
     */
    public List getComponentTypeList() {
        return mChildTypes.getValueList();
    }
    
    /**
     * Gets the iconName attribute of the TcgComponentType object
     *
     * @return The iconName value
     */
    public String getIconName() {
        return mIconName;
    }
    
    public ImageIcon getIcon() {
        return mIcon;
    }
    
    
    /**
     * Gets the name attribute of the TcgComponentType object
     *
     * @return The name value
     */
    public String getName() {
        return mName;
    }
    
    /**
     * Gets the path attribute of the TcgComponentType object
     *
     * @return The path value
     */
    public String getPath() {
        return mPath;
    }
    
    /**
     * Gets the propertyType from this TcgComponentType object given the
     * property type name
     *
     * @param propertyTypeName Description of the Parameter
     *
     * @return The propertyType value
     */
    public TcgPropertyType getPropertyType(String propertyTypeName) {
        return (TcgPropertyType) mPropertyTypes.get(propertyTypeName);
    }
    
    /**
     * @param propertyTypeName Description of the Parameter
     *
     * @return true if this TcgComponentType object has a property type with 
     * the given name
     */
    public boolean hasPropertyType(String propertyTypeName) {
        return mPropertyTypes.containsKey(propertyTypeName);
    }
    

    /**
     * Gets the number of TcgPropertyType's contained in this object
     *
     * @return the number of TcgPropertyType's contained in this object
     */
    public int getPropertyTypeCount() {
        return mPropertyTypes.size();
    }
    
    /**
     * Gets the list of TcgPropertyType associated with this TcgComponentType
     * object
     *
     * @return The propertyTypeList value
     */
    public java.util.List getPropertyTypeList() {
        return mPropertyTypes.getValueList();
    }
    
    /**
     * Gets the title attribute of the TcgComponentType object
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
     * Gets the treeNode attribute of the TcgComponentTypeImpl object
     *
     * @return The treeNode value
     */
    public ListMapTreeNode getTreeNode() {
        return mTreeNode;
    }
    
    /**
     * Gets the visible attribute of the TcgComponentType object
     *
     * @return The visible value
     */
    public boolean isVisible() {
        return mVisible;
    }
    
    public TcgComponentValidator getValidator() {
        return mValidator;
    }
    
    
    /**
     * Duplicate the given TcgComponent instance.
     *
     * @param componentName String component name for the duplicate instance
     * @param original The TcgComponent to clone
     *
     * @return Newle duplicated TcgComponent object
     */
    public TcgComponent duplicate(String componentName, TcgComponent original) {
        
        TcgComponent dupTcgComponent = newShallowComponent(componentName,
                original.getTitle());
        
        // duplicate properties values
        List props = original.getPropertyList();
        
        try {
            for (int i = 0, sz = props.size(); i < sz; i++) {
                TcgProperty p = (TcgProperty) props.get(i);
                
                dupTcgComponent.getProperty(p.getName()).setValue(p.getValue());
            }
        } catch (I18nException dap) {
            dap.printStackTrace();
            //NotifyHelper.reportError(dap.getMessage());
        }
        
        // duplicate markers
        java.util.Map markers = original.getMarkers();
        for (java.util.Iterator it = markers.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            dupTcgComponent.setMarker(key, original.getMarker(key));
        }
        
        // duplicate children
        List children = original.getComponentList();
        
        for (int i = 0, sz = children.size(); i < sz; i++) {
            TcgComponent child = (TcgComponent) children.get(i);
            
            dupTcgComponent.addComponent(child.duplicate(child.getName()));
        }
        
        return dupTcgComponent;
    }
    
    /**
     * Duplicate the TcgComponentType tree rooted at this TcgComponentType
     * instance, and allows change to name, title, path, iconName, visibility, and validator
     *
     * @return DOCUMENT ME!
     */
    public TcgComponentType duplicate(String name, String path, String title, String description, 
            String iconName, boolean allowsChildren, boolean visible,
            List xtraTcgCodeTypeList, List xtraPropTypeList, List xtraChildTypeList,
            TcgComponentValidator validator) {
        
        List ctl = new ArrayList(getCodeTypeList());
        if (xtraTcgCodeTypeList != null) {
            ctl.addAll(xtraTcgCodeTypeList);
        }
        TcgCodeType[] codeTypes = (TcgCodeType[])ctl.toArray(new TcgCodeType[0]);
        
        List ptl = new ArrayList(getPropertyTypeList());
        if (xtraPropTypeList != null) {
            ptl.addAll(xtraPropTypeList);
        }
        TcgPropertyType[] propTypes= (TcgPropertyType[])ptl.toArray(new TcgPropertyType[0]);
        
        List pctl = getComponentTypeList();
        TcgComponentType[] childTypes = null;
        int localCnt = pctl.size();
        int xtraCnt = 0;
        if (xtraChildTypeList != null) {
            xtraCnt = xtraChildTypeList.size();
        }
        childTypes = new TcgComponentType[localCnt + xtraCnt];
        
        for (int i = 0; i < localCnt; i++) {
            TcgComponentType pct = (TcgComponentType)pctl.get(i);
            childTypes[i] = pct.duplicate(pct.getName(),
                    path + "|" + pct.getName(),
                    pct.getTitle(),
                    pct.getDescription(),
                    pct.getIconName(),
                    pct.getAllowsChildren(),
                    pct.isVisible(),
                    null,
                    null,
                    null,
                    pct.getValidator());
        }
        for (int i = 0; i < xtraCnt; i++) {
            childTypes[localCnt + i] = (TcgComponentType)xtraChildTypeList.get(i);
        }
        
        TcgComponentType dup = new TcgComponentTypeImpl(name, path, title, description, 
                iconName, allowsChildren,
                visible, codeTypes, propTypes, childTypes,
                validator);
        
        return dup;
    }
    
    /**
     * Duplicate the TcgComponentType tree rooted at this TcgComponentType
     * instance.
     *
     * @return DOCUMENT ME!
     */
    public TcgComponentType duplicate() {
        return duplicate(mName, mPath, mTitle, mDescription, mIconName, mAllowsChildren, mVisible, null, null, null, mValidator);
    }
    
    /**
     * Creates a TcgComponent instance with the given component name having
     * this TcgComponentType as its attribute
     *
     * @param componentName String component name
     * @param componentTitle Description of the Parameter
     *
     * @return Newly created TcgComponent object
     */
    public TcgComponent newTcgComponent(String componentName,
            String componentTitle) {
        
        TcgComponent aTcgComponent = new TcgComponentImpl(componentName,
                componentTitle, this);
        List childTypes = getComponentTypeList();
        
        for (int i = 0, j = childTypes.size(); i < j; i++) {
            TcgComponentType childType = (TcgComponentType) childTypes.get(i);
            
            aTcgComponent.addComponent(
                    childType.newTcgComponent(childType.getName(),
                    childType.getTitle()));
        }
        
        return aTcgComponent;
    }
    
    /**
     * Creates a TcgComponent instance with the given component name having
     * this TcgComponentType as its attribute. In the case where the
     * TcgComponent is the root of a tree of TcgComponents, only the root is
     * returned. That is the meaning of "shallow"
     *
     * @param componentName String component name
     * @param componentTitle Description of the Parameter
     *
     * @return Newly created TcgComponent object
     */
    TcgComponent newShallowComponent(String componentName,
            String componentTitle) {
        
        return new TcgComponentImpl(componentName, componentTitle, this);
    }
    
    private class CTTreeNode
            extends TcgComponentTypeTreeNode {
        
        /**
         * Constructor for the CTTreeNode object
         */
        public CTTreeNode() {
            super(new ArrayHashMap(), mName, TcgComponentTypeImpl.this, false);
        }
    }
    
}

/**
 * Sort TcgPropertyTypes in alphabetical order, then move the lname property type
 * to the beginning.
 */
class PTComparator implements java.util.Comparator, Serializable {
    private static final long serialVersionUID = -6539447792556252393L;
    
    /**
     * Constructor
     */
    public PTComparator() {
    }
    
    /**
     * @see java.util.Comparator
     */
    public int compare(Object o1, Object o2) {
        if ((o1 instanceof TcgPropertyType) &&
                (o2 instanceof TcgPropertyType)) {
            TcgPropertyType s1 = (TcgPropertyType) o1;
            TcgPropertyType s2 = (TcgPropertyType) o2;
            // FIXME!
            if (s1.getName().equals(
                    TcgModelConstants.NAME_KEY)) {
                return -1;
            } else {
                String c1 = s1.getCategory();
                if (c1 == "") {
                    c1 = "a";
                }
                String c2 = s2.getCategory();
                if (c2 == "") {
                    c2 = "a";
                }
                String fullName1 = c1 + "." + s1.getName();
                String fullName2 = c2 + "." + s2.getName();
                return fullName1.compareTo(fullName2);
            }
        }
        return o1.toString().compareTo(o2.toString());
    }
    
    /**
     * @see java.util.Comparator
     */
    public boolean equals(Object o) {
        return this == o;
    }
    
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }
}
