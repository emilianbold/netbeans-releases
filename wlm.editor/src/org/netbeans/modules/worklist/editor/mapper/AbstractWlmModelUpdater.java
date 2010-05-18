/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper;

import org.netbeans.modules.worklist.editor.mapper.lsm.MapperLsmProcessor;
import org.netbeans.modules.worklist.editor.mapper.lsm.MapperLsmTree;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.mappercore.model.Constant;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.wlm.model.api.ContentElement;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.VariableDeclaration;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.xpath.WlmXPathModelFactory;
import org.netbeans.modules.wlm.model.xpath.WlmXPathNamespaceContext;
import org.netbeans.modules.wlm.model.xpath.XPathWlmVariable;
import org.netbeans.modules.worklist.editor.mapper.model.GraphInfoCollector;
import org.netbeans.modules.worklist.editor.mapper.model.PathConverter;
import org.netbeans.modules.worklist.editor.mapper.model.WlmMapperModel;
import org.netbeans.modules.worklist.editor.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.xpath.ext.CoreFunctionType;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathOperationOrFuntion;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.StubExtFunction;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStep;
import org.openide.ErrorManager;

/**
 * 
 * 
 * @author nk160297
 */
public class AbstractWlmModelUpdater {
        
    protected MapperTcContext mMapperTcContext;
    protected MapperLsmProcessor mMapperLsmProcessor;
   
    public AbstractWlmModelUpdater(MapperTcContext mapperTcContext) {
        assert mapperTcContext != null;
        mMapperTcContext = mapperTcContext;
    }

    public WlmDesignContext getDesignContext() {
        return mMapperTcContext.getDesignContextController().getContext();
    }
    
    public WlmMapperModel getMapperModel() {
        MapperModel mm = mMapperTcContext.getMapper().getModel();
        assert mm instanceof WlmMapperModel;
        return (WlmMapperModel)mm;
    }

    public MapperLsmProcessor getMapperLsmProcessor() {
        if (mMapperLsmProcessor == null) {
            mMapperLsmProcessor = new MapperLsmProcessor(mMapperTcContext);
        }
        return mMapperLsmProcessor;
    }

    //==========================================================================

    public XPathExprList buildXPathExprList(XPathModel xPathModel, 
            GraphInfoCollector graphInfo, MapperLsmTree lsmTree) {
        //
        ArrayList<XPathExpression> xPathExprList = new ArrayList<XPathExpression>();
        boolean hasRoot = false;
        //
        for (Link link : graphInfo.getTransitLinks()) {
            TreeSourcePin sourcePin = (TreeSourcePin)link.getSource();
            TreePath sourceTreePath = sourcePin.getTreePath();
            TreePathInfo tpInfo = collectTreeInfo(sourceTreePath, lsmTree);
            //
            // The XPath for has to be used because there are another 
            // expressions which has to be combined with it. 
            XPathExpression newExpression = createVariableXPath(
                    xPathModel, tpInfo);
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
    
    protected void populateContentHolder(ContentElement contentHolder, 
            GraphInfoCollector graphInfo, MapperLsmTree lsmTree) {
        //
        XPathModel xPathModel = 
                WlmXPathModelFactory.create((WLMComponent)contentHolder);
        //
        XPathExprList exprList = buildXPathExprList(
                xPathModel, graphInfo, lsmTree);
        
        //
        String newExprString = exprList.toString();
//        try {
            if (newExprString != null && newExprString.length() != 0) {
                contentHolder.setContent(newExprString);
            } else {
                contentHolder.setContent(null);
            }
//        } catch (VetoException ex) {
//            // DO nothing here
//        }
    }
    
    
    //==========================================================================

    protected XPathExpression createVariableXPath(
            XPathModel xPathModel, TreePathInfo tpInfo) {
        //
        if (tpInfo == null || tpInfo.varDecl == null) {
            return null;
        }
        //
        XPathWlmVariable xPathVar =
                new XPathWlmVariable(tpInfo.varDecl, tpInfo.part);
        //
        ReferenceableSchemaComponent sComp = xPathVar.getType();

        assert (sComp != null);

        SchemaModelsStack sms = new SchemaModelsStack();
        sms.appendSchemaComponent(sComp);
        //
        QName varQName = xPathVar.constructXPathName();
        XPathVariableReference xPathVarRef = 
                xPathModel.getFactory().newXPathVariableReference(varQName);
        //
        if (tpInfo.schemaCompList.isEmpty()) {
            return xPathVarRef;
        } else {
            List<LocationStep> stepList = PathConverter.constructLSteps(
                    xPathModel, tpInfo.schemaCompList, sms);
            if (stepList != null && !(stepList.isEmpty())) {
                XPathExpressionPath exprPath = xPathModel.getFactory().
                        newXPathExpressionPath(xPathVarRef, 
                        stepList.toArray(new LocationStep[stepList.size()]));
                return exprPath;
            }
        }
        //
        return null;
    }
    
    //==========================================================================

    /**
     * Analyses the specified treePath and collects variouse information to 
     * intermediate object TreePathInfo.
     * @param treePath
     * @return
     */
    protected TreePathInfo collectTreeInfo(TreePath treePath, MapperLsmTree lsmTree) {
        //
        TreeItem treeItem = MapperSwingTreeModel.getTreeItem(treePath);
        //
        // Populate LSMs tree
        lsmTree.addLsms(treeItem);
        //
        // Collect source info according to the tree path
        TreePathInfo sourceInfo = new TreePathInfo();
        for (Object dataObj : treeItem) { // treeItem is iterable
            processItem(dataObj, sourceInfo);
        }
        //
        return sourceInfo;
    }

    private void processItem(Object item, TreePathInfo sourceInfo) {
        //
        if (item instanceof SchemaComponent || 
                item instanceof XPathSpecialStep) {
            sourceInfo.schemaCompList.addFirst(item);
//        } else if (item instanceof AbstractPredicate) {
//            sourceInfo.schemaCompList.addFirst(item);
//        } else if (item instanceof AbstractTypeCast) {
//            AbstractTypeCast typeCast = (AbstractTypeCast)item;
//            Object castedObj = typeCast.getCastedObject();
//            processItem(castedObj, sourceInfo);
//        } else if (item instanceof AbstractPseudoComp) {
//            AbstractPseudoComp pseudo = (AbstractPseudoComp)item;
//            sourceInfo.schemaCompList.addFirst(pseudo);
        } else if (item instanceof VariableDeclaration) {
            sourceInfo.varDecl = (VariableDeclaration) item;
        } else if (item instanceof Part) {
            sourceInfo.part = (Part) item;
//        } else if (item instanceof CorrelationProperty) {
//            sourceInfo.property = (CorrelationProperty) item;
        }
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
                    //
                    // Vertex item with connected link
                    SourcePin sourcePin = ingoingLink.getSource();
                    //
                    if (sourcePin instanceof Vertex) {
                        Vertex sourceVertex = (Vertex)sourcePin;
                        childExpr = createXPathRecursive(
                                xPathModel, sourceVertex, lsmTree);
                    } else if (sourcePin instanceof TreeSourcePin) {
                        TreePath sourceTreePath = 
                                ((TreeSourcePin)sourcePin).getTreePath();
                        TreePathInfo tpInfo = collectTreeInfo(
                                sourceTreePath, lsmTree);
                        childExpr = createVariableXPath(xPathModel, tpInfo);
                    }
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
            if (nsUri != null && nsUri.length() != 0) {
                WLMModel wlmModel = getDesignContext().getWlmModel();
                TTask task = wlmModel.getTask();
                if (task != null) {
                    ExNamespaceContext nsContext = 
                            new WlmXPathNamespaceContext(task);
                    try {
                        nsContext.addNamespace(nsUri);
                    } catch (InvalidNamespaceException ex) {
                        ErrorManager.getDefault().notify(ex);
                        return null;
                    }
                }
            }
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

    //==========================================================================
    
    /**
     * Temporary container for collecting information about tree item.
     */
    protected class TreePathInfo {
        public VariableDeclaration varDecl;
        public Part part;
        // public CorrelationProperty property;
        // public String nmProperty;
        public LinkedList<Object> schemaCompList = new LinkedList<Object>();
        // public PartnerLink pLink;
        // public Roles roles;
    }
    
    /**
     * The object is a result of converting a Graph content to the XPath form 
     */
    protected class XPathExprList {
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
                        sb.append(WlmXPathModelFactory.XPATH_EXPR_DELIMITER);
                    }
                    //
                    sb.append(exprString);
                }
            }
            return sb.toString();
        }
        
    }

}
    
