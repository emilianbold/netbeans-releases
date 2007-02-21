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

package org.netbeans.modules.soa.mapper.basicmapper.methoid;

import org.netbeans.modules.soa.mapper.basicmapper.MapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IField;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicAccumulatingMethoidNode;

/**
 * <p>
 *
 * Title: BasicFieldNode </p> <p>
 *
 * Description: Provides basic functionalities of a methoid field node. This
 * class extends BasicNode to provide all the basic mapper node functionalities.
 * Then it wraps the IMethoidField object passing from the constructor to
 * produce a mapper node of the funcotid field. </p> <p>
 *
 * @author    Un Seng Leong
 * @created   December 26, 2002
 */
public class BasicFieldNode
     extends MapperNode
     implements IFieldNode {

    /**
     * The methoid field of this node.
     */
    private IField mField;
    
    private String mName;
    private String mToolTip;
    private String mType;
    private String mLiteralName;
    private ILiteralUpdater mOriginalLiteralUpdater;
    

    /**
     * Constructor a methoid field node by specified the methoid field.
     *
     * @param field  the methoid field of this node.
     */
    public BasicFieldNode(IField field) {
        mField = field;
        mName = field.getName();
    }

    /**
     * Return the field data in another object repersentation.
     *
     * @return   the field data in another object repersentation.
     */
    public Object getFieldObject() {
        return mField;
    }
    
    public void setFieldObject(Object obj) {
        IField field = (IField)obj;
        Object old = mField;
    	mField = field;
        firePropertyChange(IFieldNode.FIELD_OBJECT_CHANGED, old, obj);
    }
	
    /**
     * Return the name of this field.
     *
     * @return   the name of this field.
     */
    public String getName() {
    	if (mName != null) return mName;
        return mField.getName();
    }

    /**
     * Return the popup tooltip text of this field.
     *
     * @return   the popup tooltip text of this field.
     */
    public String getToolTipText() {
    	if (mToolTip != null) return mToolTip;
        return mField.getToolTipText();
    }

    /**
     * Return the type name of this field.
     *
     * @return   the type name of this field.
     */
    public String getTypeName() {
    	if (mType != null) return mType;
        return mField.getType();
    }

    /**
     * Retrun ture if this field is an input field, false otherwise.
     *
     * @return   true if this field is an input field, false otherwise.
     */
    public boolean isInput() {
        return mField.isInput();
    }

    /**
     * Retrun ture if this field is an output field, false otherwise.
     *
     * @return   true if this field is an output field, false otherwise.
     */
    public boolean isOutput() {
        return mField.isOutput();
    }

    /**
     * Set the name of this field node.
     *
     * @param name  the name of this field node.
     */
    public void setName(String name) {
        String oldName = mName;
        mName = name;
        mLiteralName = null;
        firePropertyChange(IFieldNode.NAME_CHANGED, mName, oldName);
    }
    
    /**
     * Set the popup tooltip text of this field.
     *
     * @param tooltip  the popup tooltip text of this field.
     */
    public void setToolTipText(String tooltip) {
        String oldToolTip = mToolTip;
        mToolTip = tooltip;
        firePropertyChange(IFieldNode.TOOLTIP_CHANGED, mToolTip, oldToolTip);
    }

    /**
     * Set the type name of this field.
     *
     * @param type  the type name of this field.
     */
    public void setTypeName(String type) {
        String oldType = mType;
        mType = type;
        firePropertyChange(IFieldNode.TOOLTIP_CHANGED, mType, oldType);
    }

    /**
     * Return a cloned basic field node of this field node. The IField object
     * will be the same as the this field node.
     *
     * @return   a cloned basic field node of this field node.
     */
    public Object clone() {
        BasicFieldNode newNode = (BasicFieldNode) super.clone();
        newNode.mField = mField;
        newNode.mName = mField.getName();
        newNode.mToolTip = mField.getToolTipText();
        newNode.mType = mField.getType();
        return newNode;
    }

    /**
     * This method calculates the hash code value
     * @return hash code
     */
    public int hashCode() {
        int hashCode = 0;

        if (mField != null) {
            hashCode = hashCode ^ mField.hashCode();
        }
        if (this.mName != null) {
            hashCode = hashCode ^ mName.hashCode();
        }
        if (this.mToolTip != null) {
            hashCode = hashCode ^ mToolTip.hashCode();
        }
        if (this.mType != null) {
            hashCode = hashCode ^ mType.hashCode();
        }
        if (this.getGroupNode() != null) {
            hashCode = hashCode ^ this.getGroupNode().hashCode();
        }

        return hashCode;
    }
    /**
     * This method determines whether two values are equal
     * @param obj obj
     * @return boolean
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BasicFieldNode)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        BasicFieldNode node = (BasicFieldNode) obj;

        if (mField != null) {
            if (!mField.equals(node.getFieldObject())) {
                return false;
            }
        }
        if (mName != null && !mName.equals(node.getName())) {
            return false;
        }
        if (mType != null && !mType.equals(node.getTypeName())) {
            return false;
        }

        if (mToolTip != null && !mToolTip.equals(node.getToolTipText())) {
            return false;
        }
        return true;
    }
    
    /**
     * Add a link that is connected to this node. Link is added only if the link
     * is not already existed.
     *
     * @param link  the link connected to this node.
     */
    public void addLink(IMapperLink link) {
        if (
                isInput() && 
                mField.getLiteralUpdater() != null && 
                hasInPlaceLiteral()) {
            // Remove the in-place literal before we connect a different link expression.
            mField.getLiteralUpdater().literalUnset(this);
            setLiteralName(null);
        }
        super.addLink(link);
    }
    
    private void setStyle(String style) {
        firePropertyChange(IFieldNode.STYLE_CHANGED, style, null);
    }
    
    /**
     * Return the display name of this methoid field node.
     * The display name is the visual name in the methoid field.
     */
    public String getLiteralName() {
        return mLiteralName;
    }
    
    /**
     * Returns whether this field node has an in-place literal.
     * This represents an unlinked field node that is 
     * tied to literal expression. 
     */
    public boolean hasInPlaceLiteral() {
        return mLiteralName != null;
    }
    
    /**
     * Set the display name of this methoid field node.
     */
    public void setLiteralName(String literalName) {
        setLiteralName(literalName, mField.getLiteralUpdater());
    }
    
    /**
     * Set the display name of this methoid field node.
     * Override the innate literal updater with the specified one.
     */
    public void setLiteralName(String literalName, ILiteralUpdater updater) {
        // do not update the mName field, we are only changing the display name
        mLiteralName = literalName;
        if (
                mField.getLiteralUpdater() != null &&
                mField.getLiteralUpdater() != updater) {
            mOriginalLiteralUpdater = mField.getLiteralUpdater();
            mField.setLiteralUpdater(updater);
        }
        if (literalName == null) {
            if (mOriginalLiteralUpdater != null) {
                // if an overriding updater was set, revert it
                mField.setLiteralUpdater(mOriginalLiteralUpdater);
            }
            // unset literal by refreshing original name and reverting style
            setName(getName());
            setStyle(IFieldNode.STYLE_TYPE_NORMAL);
        } else {
            if (updater != null) {
                String literalDisplayName = 
                    updater.getLiteralDisplayText(literalName);
                firePropertyChange(IFieldNode.NAME_CHANGED, literalDisplayName, mName);
            } else {
                firePropertyChange(IFieldNode.NAME_CHANGED, literalName, mName);
            }
            setStyle(IFieldNode.STYLE_TYPE_LITERAL);
            
            // make sure accumulating methoid containers grow properly
            IMethoidNode methoidNode = (IMethoidNode) getGroupNode();
            IMethoid methoid = (IMethoid) methoidNode.getMethoidObject();
            if (methoid.isAccumulative()) {
                ((BasicAccumulatingMethoidNode) methoidNode).checkAddNew();
            }
        }
    }
}
