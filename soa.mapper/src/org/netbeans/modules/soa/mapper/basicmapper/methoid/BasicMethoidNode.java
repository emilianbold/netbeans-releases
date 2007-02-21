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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;

import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteItem;
import org.netbeans.modules.soa.mapper.basicmapper.MapperGroupNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IField;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMutableMethoidNode;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
/**
 * <p>
 *
 * Title: </p> BasicMethoidNode <p>
 *
 * Description: </p> BasicMethoidNode provides basic implementation of
 * IMethoidNode.<p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 19, 2002
 * @version   1.0
 */
public class BasicMethoidNode
     extends MapperGroupNode
     implements IMutableMethoidNode {

    private String mName;
    private String mToolTip;
    private Icon mIcon;
    
    /**
     * the methoid object of this methoid node repersents.
     */
    private IMethoid mMethoid;

    
    /**
     * Construct a funcotid mapper node with the specified methoid object.
     *
     * @param methoid  the methoid object of this node repersents.
     */
    public BasicMethoidNode(IMethoid methoid) {
        super();
        mMethoid = methoid;

        // this order add to the list makes up the order in the
        // methoid graphics.
        IFieldNode fieldNode = null;
        List addedFieldNodes = new ArrayList();

        if (mMethoid.getNamespace() != null) {
            fieldNode = new BasicFieldNode(mMethoid.getNamespace());
            addedFieldNodes.add(fieldNode);
            addToLast(fieldNode);
        }

        List fields = mMethoid.getInput();
        int i = 0;

        if ((fields != null) && (fields.size() > 0)) {
            for (; i < fields.size(); i++) {
                fieldNode = new BasicFieldNode((IField) fields.get(i));
                addedFieldNodes.add(fieldNode);
                addToLast(fieldNode);
            }
        }

        fields = mMethoid.getOutput();
        i = 0;

        if ((fields != null) && (fields.size() > 0)) {
            for (; i < fields.size(); i++) {
                fieldNode = new BasicFieldNode((IField) fields.get(i));
                addedFieldNodes.add(fieldNode);
                addToLast(fieldNode);
            }
        }
        this.setX(-1);
        this.setY(-1);
    }

    /**
     * Return the name of this methoid.
     *
     * @return   the name of this methoid.
     */
    public String getMethoidName() {
    	if (mName != null) return mName;
        return mMethoid.getName();
    }

    /**
     * Return the data object of this methoid node. This method returns the
     * IMethoid object that this node repersents.
     *
     * @return   the IMethoid object that this node repersents.
     */
    public Object getMethoidObject() {
        return mMethoid;
    }

    public void setMethoidObject(Object methoidObj) {
       if (!(methoidObj instanceof IMethoid)) {
       	  throw new IllegalArgumentException("Required an IMethoid, found "+methoidObj);
       }
       if (methoidObj == mMethoid) return;
       IMethoid newMethoid = (IMethoid)methoidObj;
       Object data = newMethoid.getData();
       boolean accum = false;
       if (data instanceof IPaletteItem) {
           IPaletteItem item = (IPaletteItem)data;
           accum = String.valueOf(item.getItemAttribute("Accumlative")).equals("true");
       }
       List list = this.getInputFieldNodes();
       List tem = newMethoid.getInput();
       IField lastField = null;
       List toBeRemoved = new LinkedList();
       for (int i = 0, len = list.size(); i < len; i++) {
           IFieldNode fn = (IFieldNode)list.get(i);
           IField f = null;
           if (i < tem.size()) {
               f = (IField)tem.get(i);
               lastField = f;
           } else {
               if (!accum) {
                   toBeRemoved.add(fn);
                   continue;
               }
               f = lastField;
           }
           fn.setFieldObject(f);
           String fname = f.getName();
           String fieldName = fname;
           int sharp = fname.indexOf('#');
           if (sharp > 0) {
               fieldName = fname.substring(0, sharp);
               fieldName += ++i;
               if (sharp + 1 < fname.length()) {
                   fieldName += fname.substring(sharp + 1);
               }
           }
           fn.setName(fieldName);
           fn.setTypeName(f.getType());
       }
       Iterator iter = toBeRemoved.iterator();
       while (iter.hasNext()) {
           removeNode((IMapperNode)iter.next());
       }
       list = this.getOutputFieldNodes();
       tem = newMethoid.getOutput();
       for (int i = 0, len = tem.size(); i < len; i++) {
     	  ((IFieldNode)list.get(i)).setFieldObject(tem.get(i));
       }	
       Object o = mMethoid, n = methoidObj;
       mMethoid = (IMethoid)methoidObj;
       firePropertyChange(IMethoidNode.METHOID_OBJECT_CHANGED, o, n);
    }
    /**
     * Return the icon of this methoid.
     *
     * @return   the icon of this methoid
     */
    public Icon getIcon() {
    	if (mIcon != null) return mIcon;
        return mMethoid.getIcon();
    }

    /**
     * Return the tooltip text of this methoid.
     *
     * @return   the tooltip text of this methoid.
     */
    public String getToolTipText() {
    	if (mToolTip != null) return mToolTip;
        return mMethoid.getToolTipText();
    }

    /**
     * Set the name of this methoid.
     *
     * @param name  the name of this methoid.
     */
    public void setMethoidName(String name) {
       mName = name;
    }

    /**
     * Set the icon of this methoid.
     *
     * @param icon  the icon of this methoid.
     */
    public void setIcon(Icon icon) {
    	mIcon = icon;
    }

    /**
     * Set the tooltip text of this methoid.
     *
     * @param tooltip  the tooltip text of this methoid.
     */
    public void setToolTipText(String tooltip) {
        mToolTip = tooltip;
    }

    /**
     * Find and return the methoid field node that repersents the specified
     * field, or null if the node cannot be found.
     *
     * @param field  the specifed field to be matched.
     * @return       the methoid field node that repersents the specified field.
     */
    public IFieldNode findNode(IField field) {
        for (int i = 0; i < getNodeList().size(); i++) {
            IFieldNode node =
                (IFieldNode) getNodeList().get(i);

            if (node.getFieldObject().equals(field)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Return true if the specified field node is the name space field node of
     * this methoid, false otherwise.
     *
     * @param fieldNode  the field node to be matched.
     * @return           true if the specified field node is the name space
     *      field node of this methoid, false otherwise.
     */
    public boolean isNamespaceField(IFieldNode fieldNode) {
        if (mMethoid.getNamespace() == null) {
            return false;
        }
        return mMethoid.getNamespace().equals(fieldNode.getFieldObject());
    }

    /**
     * Retrun the name space field node of this methoid.
     *
     * @return   the name space field node of this methoid.
     */
    public IFieldNode getNamespaceFieldNode() {
        if (mMethoid.getNamespace() == null) {
            return null;
        }
        for (int i = 0; i < getNodeList().size(); i++) {
            IFieldNode child = (IFieldNode) getNodeList().get(i);
            if (isNamespaceField(child)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Returns the input field nodes of this methoid node.
     *
     * @return   The input field nodes of this methoid node.
     */
    public List getInputFieldNodes() {
        List rslt = new ArrayList();
        for (int i = 0; i < getNodeList().size(); i++) {
            IFieldNode node =
                (IFieldNode) getNodeList().get(i);

            if (node.isInput()) {
                rslt.add(node);
            }
        }
        return rslt;
    }

    /**
     * Returns the output field nodes of this methoid node
     *
     * @return   the output field nodes of this methoid node
     */
    public List getOutputFieldNodes() {
        List rslt = new ArrayList();
        for (int i = 0; i < getNodeList().size(); i++) {
            IFieldNode node =
                (IFieldNode) getNodeList().get(i);

            if (node.isOutput()) {
                rslt.add(node);
            }
        }
        return rslt;
    }

    /**
     * Return a sum of all next nodes of the children.
     *
     * @return   a sum of all next nodes of the children.
     */
    public List getNextNodes() {
        List allNextNodes = new ArrayList();
        for (int i = 0; i < getNodeList().size(); i++) {
            IFieldNode node =
                (IFieldNode) getNodeList().get(i);

            if (node.isOutput()) {
                allNextNodes.addAll(node.getNextNodes());
            }
        }
        return allNextNodes;
    }

    /**
     * Return a sum of all previous nodes of the children.
     *
     * @return   a sum of all previous nodes of the children.
     */
    public List getPreviousNodes() {
        List allPrevNodes = new ArrayList();
        for (int i = 0; i < getNodeList().size(); i++) {
            IFieldNode node =
                (IFieldNode) getNodeList().get(i);

            if (node.isInput()) {
                allPrevNodes.addAll(node.getPreviousNodes());
            }
        }
        return allPrevNodes;
    }

    /**
     * Return a cloned BasicMethoidNode. The IMethoid meta data object will not
     * be cloned. Both orginal and the new cloned are referred to the same
     * IMethoid object.
     *
     * @return   a cloned BasicMethoidNode.
     */
    public Object clone() {
        BasicMethoidNode newNode = (BasicMethoidNode) super.clone();
        newNode.mMethoid = mMethoid;
        newNode.mName = mName;
        newNode.mIcon = mIcon;
        newNode.mToolTip = mToolTip;
        return newNode;
    }
    
    /**
     * Add and return the new input child of this methoid.
     * This only applies to methoid nodes of accumulating methoids.
     *
     * @return   the new input child of this methoid.
     */
    public IFieldNode getNextNewNode() {
        // By default, we are not growable.
        return null;
    }
}
