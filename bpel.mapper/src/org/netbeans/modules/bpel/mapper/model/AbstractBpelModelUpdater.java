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

package org.netbeans.modules.bpel.mapper.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.mapper.cast.AbstractTypeCast;
import org.netbeans.modules.bpel.mapper.cast.CastManager;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.predicates.AbstractPredicate;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.VariableDeclarationWrapper;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.netbeans.modules.bpel.model.api.support.Roles;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.soa.mappercore.model.Constant;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.xpath.ext.CoreFunctionType;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeNameTest;
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
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xpath.ext.XPathAxis;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
import org.openide.ErrorManager;

/**
 * 
 * 
 * @author nk160297
 */
public class AbstractBpelModelUpdater {
        
    protected MapperTcContext mMapperTcContext;
   
    public AbstractBpelModelUpdater(MapperTcContext mapperTcContext) {
        assert mapperTcContext != null;
        mMapperTcContext = mapperTcContext;
    }

    public BpelDesignContext getDesignContext() {
        return mMapperTcContext.getDesignContextController().getContext();
    }
    
    public BpelMapperModel getMapperModel() {
        MapperModel mm = mMapperTcContext.getMapper().getModel();
        assert mm instanceof BpelMapperModel;
        return (BpelMapperModel)mm;
    }
    
    //==========================================================================

    public XPathExprList buildXPathExprList(XPathModel xPathModel, 
            GraphInfoCollector graphInfo, 
            Set<AbstractTypeCast> typeCastCollector) {
        //
        ArrayList<XPathExpression> xPathExprList = new ArrayList<XPathExpression>();
        boolean hasRoot = false;
        //
        for (Link link : graphInfo.getTransitLinks()) {
            TreeSourcePin sourcePin = (TreeSourcePin)link.getSource();
            TreePath sourceTreePath = sourcePin.getTreePath();
            TreePathInfo tpInfo = collectTreeInfo(sourceTreePath, typeCastCollector);
            //
            // The XPath for has to be used because there are another 
            // expressions which has to be combined with it. 
            XPathExpression newExpression = createVariableXPath(
                    xPathModel, tpInfo, typeCastCollector);
            if (newExpression != null) {
                xPathModel.fillInStubs(newExpression);
                xPathExprList.add(newExpression);
                hasRoot = true;
            }
        }
        //
        for (Vertex vertex : graphInfo.getPrimaryRoots()) {
            XPathExpression newExpression = createXPathRecursive(
                    xPathModel, vertex, typeCastCollector);
            if (newExpression != null) {
                xPathModel.fillInStubs(newExpression);
                xPathExprList.add(newExpression);
                hasRoot = true;
            }
        }
        //
        for (Vertex vertex : graphInfo.getSecondryRoots()) {
            XPathExpression newExpression = createXPathRecursive(
                    xPathModel, vertex, typeCastCollector);
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
            GraphInfoCollector graphInfo, Set<AbstractTypeCast> typeCastCollector) {
        //
        XPathModel xPathModel = 
                BpelXPathModelFactory.create((BpelEntity)contentHolder);
        //
        XPathExprList exprList = 
                buildXPathExprList(xPathModel, graphInfo, typeCastCollector);
        
        //
        String newExprString = exprList.toString();
        try {
            if (newExprString != null && newExprString.length() != 0) {
                contentHolder.setContent(newExprString);
            } else {
                contentHolder.setContent(null);
            }
        } catch (VetoException ex) {
            // DO nothing here
        }
    }
    
    //==========================================================================

    protected XPathExpression createVariableXPath(XPathModel xPathModel, 
            TreePathInfo tpInfo, Set<AbstractTypeCast> typeCastCollector) {
        //
        if (tpInfo == null || tpInfo.varDecl == null) {
            return null;
        }
        //
        XPathBpelVariable xPathVar = 
                new XPathBpelVariable(tpInfo.varDecl, tpInfo.part);
        //
        ReferenceableSchemaComponent sComp = xPathVar.getType();
        assert sComp != null;
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
            List<LocationStep> stepList = constructLSteps(
                    xPathModel, tpInfo.schemaCompList, typeCastCollector, sms);
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

    protected List<LocationStep> constructLSteps(XPathModel xPathModel, 
            List<Object> sCompList, Set<AbstractTypeCast> typeCastCollector, 
            SchemaModelsStack sms) {
        if (sCompList == null || sCompList.isEmpty()) {
            return null;
        } 
        //
        ArrayList<LocationStep> result = new ArrayList<LocationStep>();
        SchemaComponent sComp = null;
        //
        for (Object stepObj : sCompList) {
            LocationStep newLocationStep = null;
            if (stepObj instanceof SchemaComponent) {
                sComp = (SchemaComponent)stepObj;
                newLocationStep = constructLStep(xPathModel, sComp, null, sms);
            } else if (stepObj instanceof AbstractPredicate) {
                AbstractPredicate pred = (AbstractPredicate)stepObj;
                XPathPredicateExpression[] predArr = pred.getPredicates();
                sComp = pred.getSComponent();
                newLocationStep = constructLStep(xPathModel, sComp, predArr, sms);
            } else if (stepObj instanceof AbstractTypeCast) {
                AbstractTypeCast typeCast = (AbstractTypeCast)stepObj;
                sComp = typeCast.getSComponent();
                newLocationStep = constructLStep(xPathModel, sComp, null, sms);
                //
                if (typeCastCollector != null) {
                    typeCastCollector.add(typeCast);
                }
                //
            } else if (stepObj instanceof LocationStep) {
                //
                // TODO: It would be more correct to do a copy of the stepObj
                // because of it is owned by another XPathModel. 
                newLocationStep = (LocationStep)stepObj;
                if (newLocationStep != null) {
                    XPathSchemaContext sContext = newLocationStep.getSchemaContext();
                    if (sContext != null) {
                        sComp = XPathSchemaContext.Utilities.getSchemaComp(sContext);
                    }
                }
            } else if (stepObj instanceof AbstractTypeCast) {
                sComp = ((AbstractTypeCast)stepObj).getSComponent();
                newLocationStep = constructLStep(xPathModel, sComp, null, sms);
            }
            //
            if (sComp != null) {
                sms.appendSchemaComponent(sComp);
            } else {
                sms.discard();
            }
            //
            if (newLocationStep != null) {
                result.add(newLocationStep);
            }
        }
        //
        return result;
    } 
    
    /**
     * Constructs a LocationStep object by the schema component. 
     * @param xPathModel
     * @param sComp
     * @return
     */
    protected LocationStep constructLStep(XPathModel xPathModel, 
            SchemaComponent sComp, XPathPredicateExpression[] predArr, 
            SchemaModelsStack sms) {
        //
        if (!(sComp instanceof Named)) {
            return null;
        }
        //
        XPathAxis axis = null;
        if (sComp instanceof Attribute) {
            axis = XPathAxis.ATTRIBUTE;
        } else {
            axis = XPathAxis.CHILD;
        }
        //
        StepNodeNameTest nameTest = new StepNodeNameTest(xPathModel, sComp, sms);
        LocationStep newLocationStep = xPathModel.getFactory().
                newLocationStep(axis, nameTest, predArr);
        //
        return newLocationStep;
    }

    //==========================================================================

    /**
     * Analyses the specified treePath and collects variouse information to 
     * intermediate object TreePathInfo.
     * @param treePath
     * @return
     */
    protected TreePathInfo collectTreeInfo(TreePath treePath, 
            Set<AbstractTypeCast> typeCastCollector) {
        //
        List<Object> objectPath = MapperSwingTreeModel.convertTreePath(treePath);
        //
        TreePathInfo sourceInfo = new TreePathInfo();
        //
        // Collect source info according to the tree path
        for (Object item : objectPath) {
            processItem(item, sourceInfo, typeCastCollector);
        }
        //
        return sourceInfo;
    }

    private void processItem(Object item,
            AbstractBpelModelUpdater.TreePathInfo sourceInfo, 
            Set<AbstractTypeCast> typeCastCollector) {
        //
        if (item instanceof SchemaComponent || 
                item instanceof AbstractPredicate || 
                item instanceof LocationStep) {
            sourceInfo.schemaCompList.add(item);
        } else if (item instanceof AbstractTypeCast) {
            AbstractTypeCast typeCast = (AbstractTypeCast)item;
            if (typeCastCollector != null) {
                typeCastCollector.add(typeCast);
            }
            Object castedObj = typeCast.getCastedObject();
            processItem(castedObj, sourceInfo, typeCastCollector);
        } else if (item instanceof AbstractVariableDeclaration) {
            if (item instanceof VariableDeclarationScope) {
                return;
            } else if (item instanceof VariableDeclarationWrapper) {
                sourceInfo.varDecl =
                        ((VariableDeclarationWrapper) item).getDelegate();
            } else if (item instanceof VariableDeclaration) {
                sourceInfo.varDecl =
                        (VariableDeclaration) item;
            }
        } else if (item instanceof Part) {
            sourceInfo.part = (Part) item;
        } else if (item instanceof PartnerLink) {
            sourceInfo.pLink =
                    (PartnerLink) item;
        } else if (item instanceof Roles) {
            sourceInfo.roles =
                    (Roles) item;
        }
    }
    
    //==========================================================================

    protected XPathExpression createXPathRecursive(XPathModel xPathModel, 
            Vertex vertex, Set<AbstractTypeCast> typeCastCollector) {
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
                                xPathModel, sourceVertex, typeCastCollector);
                    } else if (sourcePin instanceof TreeSourcePin) {
                        TreePath sourceTreePath = 
                                ((TreeSourcePin)sourcePin).getTreePath();
                        TreePathInfo tpInfo = 
                                collectTreeInfo(sourceTreePath, typeCastCollector);
                        childExpr = createVariableXPath(
                                xPathModel, tpInfo, typeCastCollector);
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
                BpelModel bpelModel = getDesignContext().getBpelModel();
                Process process = bpelModel.getProcess();
                if (process != null) {
                    ExNamespaceContext nsContext = process.getNamespaceContext();
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

    public void registerTypeCasts(ExtensibleElements destination, 
            Collection<AbstractTypeCast> typeCasts, boolean inLeftTree) {
        if (typeCasts == null || typeCasts.isEmpty()) {
            return;
        }
        //
        CastManager cm = CastManager.getCastManager(getMapperModel(), inLeftTree);
        if (cm != null) {
            cm.registerTypeCasts(destination, typeCasts);
        }
    }

    //==========================================================================
    
    /**
     * Temporary container for collecting information about tree item.
     */
    protected class TreePathInfo {
        public VariableDeclaration varDecl;
        public Part part;
        public ArrayList<Object> schemaCompList = new ArrayList<Object>();
        public PartnerLink pLink;
        public Roles roles;
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
                        sb.append(BpelXPathModelFactory.XPATH_EXPR_DELIMITER);
                    }
                    //
                    sb.append(exprString);
                }
            }
            return sb.toString();
        }
        
    }

}
    