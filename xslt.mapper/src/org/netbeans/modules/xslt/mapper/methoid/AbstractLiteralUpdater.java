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

package org.netbeans.modules.xslt.mapper.methoid;

import java.util.Iterator;
import org.netbeans.modules.soa.mapper.common.IMapperGroupNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater.LiteralSubTypeInfo;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.openide.nodes.Node;


/**
 * Handles creation of literal editors for the BPEL editor as well
 * as what to do when the resulting editor value is applied.
 * Whenever support for a new literal type is added, an editor as well
 * as an updater must be created to handle it. Editors are the UI
 * for the visual mapper editor. They allow users to enter a new
 * literal value or modify an existing one. Updaters are responsible
 * for changing the model expression once the user has finished
 * editing the value.
 * 
 * @author Josh Sandusky
 */
public abstract class AbstractLiteralUpdater implements ILiteralUpdater {

    protected XPathNodeExpressionUpdater mProcessor;
    
    public AbstractLiteralUpdater() {
    }
    

    public boolean hasEditor() {
        return true;
    }
    
// TODO reimplement
//    protected void applyLiteral(IFieldNode fieldNode, 
//                                String newValue, 
//                                XPathLiteralNode literalNode) {
//        updateModelLiteral(
//                fieldNode, 
//                literalNode, 
//                false);
//        fieldNode.setLiteralName(newValue);
//        updateToolTip(fieldNode, newValue);
//        if (mProcessor != null) {
//            mProcessor.updateNodeExpression(fieldNode);
//        }
//    }
    
    public void setXPathProcessor(XPathNodeExpressionUpdater processor) {
        mProcessor = processor;
    }
    
    protected void updateToolTip(IFieldNode fieldNode, String newValue) {
        IMethoidNode methoidNode = (IMethoidNode) fieldNode.getGroupNode();
        IMethoid methoid = (IMethoid) methoidNode.getMethoidObject();
        if (methoid.isLiteral()) {
            fieldNode.setToolTipText(newValue);
        }
    }
    
    public void literalUnset(IFieldNode fieldNode) {
// TODO reimplement
//        updateModelLiteral(
//                fieldNode, 
//                (XPathLiteralNode) fieldNode.getNodeObject(), 
//                true);
        fieldNode.setLiteralName(null);
        if (mProcessor != null) {
            mProcessor.updateNodeExpression(fieldNode);
        }
    }
    
    public String getLiteralDisplayText(String literalText) {
        return literalText;
    }
    
    public LiteralSubTypeInfo getLiteralSubType(String freeTextValue) {
        // default case is no special sub-type information
        return null;
    }

// TODO reimplement    
//    private void updateModelLiteral(IFieldNode fieldNode, 
//                                    XPathLiteralNode literalNode, 
//                                    boolean isRemove) {
//        boolean linksNeedRemoving = false;
//        IMapperGroupNode groupNode = fieldNode.getGroupNode();
//        Node groupNodeObject = (Node) groupNode.getNodeObject();
//        if (groupNodeObject instanceof XPathOperatorNode) {
//            XPathOperatorNode operatorNode = (XPathOperatorNode) groupNodeObject;
//            int fieldIndex = MapperUtil.findFieldIndex(groupNode, fieldNode);
//            if (isRemove) {
//                if (literalNode != null) {
//                    fieldNode.setNodeObject(null);
//                    operatorNode.removeInput(literalNode);
//                }
//            } else {
//                linksNeedRemoving = true;
//                fieldNode.setNodeObject(literalNode);
//                operatorNode.addInput(fieldIndex, literalNode);
//            }
//        } else if (groupNodeObject instanceof XPathLiteralNode) {
//            if (isRemove) {
//                groupNode.setNodeObject(null);
//            } else {
//                groupNode.setNodeObject(literalNode);
//            }
//        }
//        
//        if (linksNeedRemoving) {
//            // Now for each link connected to the field node, we remove the
//            // link's starting node's output.
//            for (Iterator iter=fieldNode.getLinks().iterator(); iter.hasNext();) {
//                IMapperLink link = (IMapperLink) iter.next();
//                IMapperNode startNode = link.getStartNode();
//                Node modelNode = MapperUtil.getMapperNodeObject(startNode);
//                if (modelNode instanceof CanvasNode) {
//                    CanvasNode modelCanvasNode = (CanvasNode) modelNode;
//                    modelCanvasNode.removeOutput(groupNodeObject);
//                }
//            }
//        }
//    }
    
    
    public interface XPathNodeExpressionUpdater {
        public void updateNodeExpression(IFieldNode sourceNode);
    }
}
