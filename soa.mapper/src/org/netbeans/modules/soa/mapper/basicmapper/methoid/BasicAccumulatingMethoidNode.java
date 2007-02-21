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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IField;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.IMapperNode;


/**
 * A BasicMethoidNode that has the additional capability of automatically
 * adding one input field each time the last input field is linked to.
 * This allows the user to always be able to link more inputs to the methoid. 
 * 
 * @author Josh Sandusky
 */
public class BasicAccumulatingMethoidNode 
extends BasicMethoidNode {

    /**
     * the listener to listen on the child node add link event.
     */
    private PropertyChangeListener mNodeListListener;
    
    
    /**
     * Constructor for the AccumulatingMethoidNode object with the specified default
     * methoid node, and the mapper.
     *
     * @param defaultNode  the node to be wrapped
     * @param mapper       Description of the Parameter
     */
    public BasicAccumulatingMethoidNode(IMethoid methoid) {
        super(methoid);
        mNodeListListener = new NodeLinkListener();
        int i = 1;
        for (IMapperNode node = getFirstNode(); node != null; node = getNextNode(node)) {
            IFieldNode fn = (IFieldNode)node;
            if (fn.isInput()) {
                node.addPropertyChangeListener(mNodeListListener);
                IField f = (IField)fn.getFieldObject();
                String fname = f.getName();
                String fieldName = fname;
                
                int sharp = fname.indexOf('#');
                if (sharp > 0) {
                    fieldName = fname.substring(0, sharp);
                    fieldName += i;
                    if (sharp + 1 < fname.length()) {
                        fieldName += fname.substring(sharp + 1);
                    }
                }
                fn.setName(fieldName);
                i++;
            }
        }
        addPropertyChangeListener(new ChildNodeListener());
    }
    

    /**
     * Check if a new child is necessary, and adds a new child.
     */
    public void checkAddNew() {
        List inputNodes = getInputFieldNodes();
        IFieldNode node = null;
        for (int i = inputNodes.size() - 1; i >= 0; i--) {
            node = (IFieldNode) inputNodes.get(i);
            if (
                    node.getLinkCount() == 0 &&
                    !node.hasInPlaceLiteral()) {
                return;
            }
        }
        if (node != null) {
            getNextNewNode();
        }
    }

    /**
     * Add and return the new input child of this methoid.
     *
     * @return   the new input child of this methoid.
     */
    public IFieldNode getNextNewNode() {
        List inputNodes = getInputFieldNodes();
        IFieldNode lastNode = (IFieldNode) inputNodes.get(inputNodes.size() - 1);
        IField f = (IField)lastNode.getFieldObject();
        String fname = f.getName();
        String fieldName = fname;
        int sharp = fname.indexOf('#');
        if (sharp > 0) {
            fieldName = fname.substring(0, sharp);
            fieldName += (inputNodes.size() + 1);
            if (sharp + 1 < fname.length()) {
                fieldName += fname.substring(sharp + 1);
            }
        }
        String fieldTypeName = lastNode.getTypeName();
        IField newField = new BasicField(
                fname, 
                lastNode.getTypeName(),
                lastNode.getToolTipText(), 
                f.getData(), 
                true, 
                false,
                f.getLiteralUpdater());
        IFieldNode newFieldNode = new BasicFieldNode(newField);
        newFieldNode.setName(fieldName);
        addNextNode(lastNode, newFieldNode);
        return newFieldNode;
    }

    /**
     * Return a cloned instance of AccumulatingMethoidNode. The mapper is not cloned.
     *
     * @return   a cloned instance of AccumulatingMethoidNode.
     */
    public Object clone() {
        BasicAccumulatingMethoidNode newNode = (BasicAccumulatingMethoidNode) super.clone();
        newNode.mNodeListListener = new NodeLinkListener();
        for (
                IMapperNode node = newNode.getFirstNode(); 
                node != null; 
                node = newNode.getNextNode(node)) {
            if (((IFieldNode) node).isInput()) {
                node.addPropertyChangeListener(newNode.mNodeListListener);
            }
        }
        newNode.addPropertyChangeListener(new ChildNodeListener());
        return newNode;
    }
    
    
    /**
     * Property listener listens on the child node add link event.
     *
     * @author    sleong
     * @created   January 29, 2003
     */
    private class NodeLinkListener implements PropertyChangeListener {
        /**
         * Listens on the child node add link event. Add new node if necessary.
         *
         * @param e  the PropertyChangeEvent.
         */
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(IMapperNode.LINK_ADDED)) {
                checkAddNew();
           }
        }
    }
    

    /**
     * Property listener listens on the new and remove child node of this
     * methoid.
     *
     * @author    sleong
     * @created   January 29, 2003
     */
    private class ChildNodeListener implements PropertyChangeListener {
        /**
         * Add and remove the child property listener, if new or remove child
         * happened, accordingly.
         *
         * @param e  the PropertyChangeEvent
         */
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(NODE_INSERTED)) {
                IMapperNode n = (IMapperNode)e.getNewValue();
                n.addPropertyChangeListener(mNodeListListener);
            } else if (e.getPropertyName().equals(NODE_REMOVED)) {
                IMapperNode n = (IMapperNode) e.getOldValue();
                n.removePropertyChangeListener(mNodeListListener);
                checkAddNew();
            }
        }
    }
}
