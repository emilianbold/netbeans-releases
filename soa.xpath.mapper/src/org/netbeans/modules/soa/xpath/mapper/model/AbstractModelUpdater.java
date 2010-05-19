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

package org.netbeans.modules.soa.xpath.mapper.model;

import java.util.ArrayList;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.mappercore.model.Constant;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperLsmTree;
import org.netbeans.modules.soa.xpath.mapper.model.updater.GraphInfoCollector;
import org.netbeans.modules.xml.xpath.ext.CoreFunctionType;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathOperationOrFuntion;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.StubExtFunction;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;

/**
 * 
 * 
 * @author Nikita Krjukov
 */
public abstract class AbstractModelUpdater {

    public interface Constructor {
        AbstractModelUpdater createModelUpdater();
    }

    protected MapperStaticContext mStContext;
   
    public AbstractModelUpdater(MapperStaticContext stContext) {
        assert stContext != null;
        mStContext = stContext;
    }

    public XPathExprList buildXPathExprList(XPathModel xPathModel, 
            GraphInfoCollector graphInfo, MapperLsmTree lsmTree) {
        //
        ArrayList<XPathExpression> xPathExprList = new ArrayList<XPathExpression>();
        boolean hasRoot = false;
        //
        for (Link link : graphInfo.getTransitLinks()) {
            TreeSourcePin sourcePin = (TreeSourcePin)link.getSource();
            TreePath sourceTreePath = sourcePin.getTreePath();
            XPathExpression newExpression = createVariableXPath(
                    xPathModel, sourceTreePath, lsmTree);
            if (newExpression != null) {
                xPathModel.fillInStubs(newExpression);
                xPathExprList.add(newExpression);
                hasRoot = true;
            }
        }
        //
        for (Vertex vertex : graphInfo.getPrimaryRoots()) {
            XPathExpression newExpression = createXPathRecursive(
                    xPathModel, vertex, lsmTree);
            if (newExpression != null) {
                xPathModel.fillInStubs(newExpression);
                xPathExprList.add(newExpression);
                hasRoot = true;
            }
        }
        //.
        for (Vertex vertex : graphInfo.getSecondryRoots()) {
            XPathExpression newExpression = createXPathRecursive(
                    xPathModel, vertex, lsmTree);
            if (newExpression != null) {
                xPathModel.fillInStubs(newExpression);
                xPathExprList.add(newExpression);
            }
        }
        //
        XPathExprList result = new XPathExprList(xPathExprList, hasRoot);
        return result;
    }

    //==========================================================================

    protected XPathExpression createXPathRecursive(XPathModel xPathModel, 
            Vertex vertex, MapperLsmTree lsmTree) {
        XPathExpression newExpression = createXPath(xPathModel, vertex);
        //
        if (newExpression instanceof XPathOperationOrFuntion) {
            // Operation or Function
            //
            // Calculate index of the vertex item to which 
            // the last (in order of appearance) incoming links is connected 
            // or which contains a not empty value (it is equal to connection
            // to a constant value)
            int lastConnectedItemIndex = -1;
            for (int index = 0; index < vertex.getItemCount(); index++) {
                VertexItem vItem = vertex.getItem(index);
                if (vItem.isHairline()) {
                    // Skip hirelines
                    continue;
                }
                //
                Object value = vItem.getValue();
                if (value != null) {
                    lastConnectedItemIndex = index;
                    continue;
                }
                //
                Link incomingLink = vItem.getIngoingLink();
                if (incomingLink != null) {
                    lastConnectedItemIndex = index;
                }
            }
            //
            // Process incoming links and inplace values
            for (int index = 0; index < vertex.getItemCount(); index++) {
                VertexItem vItem = vertex.getItem(index);
                if (vItem.isHairline()) {
                    // Skip hirelines
                    continue;
                }
                XPathExpression childExpr = null;
                Link ingoingLink = vItem.getIngoingLink();
                if (ingoingLink == null) {
                    boolean valueProcessed = false;
                    Object value = vItem.getValue();
                    if (value != null) {
                        // Add value as a constant
                        Object dataObject = vItem.getDataObject();
                        assert dataObject instanceof ArgumentDescriptor;
                        //
                        XPathType argType = ((ArgumentDescriptor)dataObject)
                                .getArgumentType();
                        if (argType == XPathType.NUMBER_TYPE) {
                            if (value instanceof Number) {
                                childExpr = xPathModel.getFactory().
                                        newXPathNumericLiteral((Number)value);
                                valueProcessed = true;
                            }
                        } else if (argType == XPathType.STRING_TYPE) {
                            if (value instanceof String && 
                                    ((String)value).length() != 0 ) {
                                childExpr = xPathModel.getFactory().
                                        newXPathStringLiteral((String)value);
                                valueProcessed = true;
                            }
                        } else {
                            assert false : "Unsupported constant's type"; // NOI18N
                        }
                    }
                    //
                    // Consider the value is empty or wrong it it is not processed
                    if (!valueProcessed) {
                        //
                        // Vertex item without connected link
                        if (index >= lastConnectedItemIndex) {
                            // There is not any more items with connected links
                            break;
                        }
                        //
                        // The stub() function will be added to output text.
                        // It is necessary to protect place where the following 
                        // links are connected. Otherwise the links move to the 
                        // first positions after mapper is reloaded. 
                        childExpr = new StubExtFunction(xPathModel);
                    }
                } else {
                    childExpr = createIngoingLinkXPath(
                            ingoingLink, xPathModel, lsmTree, vertex, index);
                }
                //
                if (childExpr != null) {
                    ((XPathOperationOrFuntion)newExpression).addChild(childExpr);
                }
            }
        }
        //
        return newExpression;
    }
    
    /**
     * Creates XPath expression for the specified vertex. 
     * 
     * @param xPathModel
     * @param vertex
     * @return
     */
    protected XPathExpression createXPath(XPathModel xPathModel, Vertex vertex) {
        Object vertexDO = vertex.getDataObject();
        XPathExpression result = null;
        //
        if (vertexDO instanceof CoreOperationType) {
            CoreOperationType operType = (CoreOperationType)vertexDO;
            result = xPathModel.getFactory().newXPathCoreOperation(operType);
        } else if (vertexDO instanceof CoreFunctionType) {
            CoreFunctionType funcType = (CoreFunctionType)vertexDO;
            result = xPathModel.getFactory().newXPathCoreFunction(funcType);
        } else if (vertexDO instanceof ExtFunctionMetadata) {
            ExtFunctionMetadata funcMetadata = (ExtFunctionMetadata)vertexDO;
            QName funcQName = funcMetadata.getName();
            //
            // Checks is there the prefix declaration and create it if necessary
            String nsUri = funcQName.getNamespaceURI();
            mStContext.getMapperSpi().registerNewNsPrefix(nsUri, mStContext);
            //
            result = xPathModel.getFactory().newXPathExtensionFunction(funcQName);
        } else if (vertex instanceof Constant) {
            Constant constant = (Constant)vertex;
            VertexItem firstVItem = constant.getItem(0);
            String textValue = firstVItem.getText();
            if (vertexDO == XPathStringLiteral.class) {
                result = xPathModel.getFactory().newXPathStringLiteral(textValue);
            } else if (vertexDO == XPathNumericLiteral.class) {
                try {
                    Integer literalValue = Integer.valueOf(textValue);
                    result = xPathModel.getFactory().
                            newXPathNumericLiteral(literalValue);
                } catch (NumberFormatException nfEx) {
                    // do nothing
                }
                //
                try {
                    Long literalValue = Long.valueOf(textValue);
                    result = xPathModel.getFactory().
                            newXPathNumericLiteral(literalValue);
                } catch (NumberFormatException nfEx) {
                    // do nothing
                }
                //
                if (result == null) {
                    try {
                        Float literalValue = Float.valueOf(textValue);
                        result = xPathModel.getFactory().
                                newXPathNumericLiteral(literalValue);
                    } catch (NumberFormatException nfEx) {
                        // do nothing
                    }
                }
                //
                if (result == null) {
                    try {
                        Double literalValue = Double.valueOf(textValue);
                        result = xPathModel.getFactory().
                                newXPathNumericLiteral(literalValue);
                    } catch (NumberFormatException nfEx) {
                        // do nothing
                    }
                }
            }
        }
        //
        return result;
    }

    /**
     * Creates a new XPath expression based on an ingoing link of a vertex.
     *
     * @param ingoingLink
     * @param xPathModel
     * @param lsmTree
     * @param vertex to which the link is connected
     * @param index indicates the order number of the link
     * @return
     */
    protected XPathExpression createIngoingLinkXPath(
            Link ingoingLink, XPathModel xPathModel,
            MapperLsmTree lsmTree, Vertex vertex, int index) {
        //
        SourcePin sourcePin = ingoingLink.getSource();
        if (sourcePin instanceof Vertex) {
            Vertex sourceVertex = (Vertex) sourcePin;
            return createXPathRecursive(xPathModel, sourceVertex, lsmTree);
        } else if (sourcePin instanceof TreeSourcePin) {
            TreePath sourceTreePath = ((TreeSourcePin) sourcePin).getTreePath();
            //
            return createVariableXPath(xPathModel, sourceTreePath, lsmTree);
        }
        //
        return null;
    }

    /**
     * Creates a new XPath expression based on a positon in a tree.
     *
     * @param xPathModel
     * @param sourceTreePath the position in the left tree
     * @param lsmTree LSM's holder
     * @return
     */
    protected abstract XPathExpression createVariableXPath(
            XPathModel xPathModel, TreePath sourceTreePath, MapperLsmTree lsmTree);

    //==========================================================================
    
    /**
     * The object is a result of converting a Graph content to the XPath form 
     */
    public class XPathExprList {
        private ArrayList<XPathExpression> mExprList;
        // means that the first list item is connected to the right tree
        private boolean mIsFirstConnected; 
        
        public XPathExprList(ArrayList<XPathExpression> exprList, 
                boolean isFirstConnected) {
            //
            mExprList = exprList;
            mIsFirstConnected = isFirstConnected;
        }
        
        public XPathExpression getConnectedExpression() {
            if (!mIsFirstConnected) {
                return null;
            } else {
                return mExprList.get(0);
            }
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            //
            // The list of expressions can contain a few elements, but not more 
            // then one can be connected to the right tree. Only the first 
            // element, which are placed before the ";" delimiter is considered
            // as connected. All others are detached.
            // 
            boolean isFirst = mIsFirstConnected;
            //
            for (XPathExpression xPathExpr : mExprList) {
                String exprString = xPathExpr.getExpressionString();
                if (exprString != null && exprString.length() != 0) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        sb.append(mStContext.getMapperSpi().getXPathExprDelimiter());
                    }
                    //
                    sb.append(exprString);
                }
            }
            return sb.toString();
        }
        
    }

}
    
