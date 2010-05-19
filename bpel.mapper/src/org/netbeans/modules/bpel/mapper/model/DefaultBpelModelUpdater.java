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
import java.util.LinkedList;
import java.util.List;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperLsm;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperPseudoComp;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperTypeCast;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.predicates.BpelMapperPredicate;
import org.netbeans.modules.bpel.mapper.tree.models.VariableDeclarationWrapper;
import org.netbeans.modules.bpel.mapper.properties.PropertiesConstants;
import org.netbeans.modules.bpel.mapper.properties.PropertiesUtils;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.BpelXPathExtFunctionMetadata;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperLsmTree;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.netbeans.modules.bpel.model.api.support.Roles;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.bpel.model.ext.editor.api.NMProperty;
import org.netbeans.modules.soa.mappercore.model.Function;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.model.AbstractModelUpdater;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathModelFactory;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStep;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 * 
 * 
 * @author Nikita Krjukov
 */
public class DefaultBpelModelUpdater extends AbstractModelUpdater {
        
    protected BpelMapperLsmProcessor mMapperLsmProcessor;
   
    public DefaultBpelModelUpdater(MapperTcContext mapperTcContext) {
        super(mapperTcContext);
    }

    public MapperTcContext getTcContext() {
        return MapperTcContext.class.cast(mStContext);
    }

    public BpelDesignContext getDesignContext() {
        return getTcContext().getDesignContextController().getContext();
    }
    
    public BpelMapperModel getMapperModel() {
        MapperModel mm = mStContext.getMapperModel();
        assert mm instanceof BpelMapperModel;
        return (BpelMapperModel)mm;
    }

    public BpelMapperLsmProcessor getMapperLsmProcessor() {
        if (mMapperLsmProcessor == null) {
            mMapperLsmProcessor = new BpelMapperLsmProcessor(getTcContext());
        }
        return mMapperLsmProcessor;
    }

    //==========================================================================

    @Override
    protected XPathExpression createIngoingLinkXPath(
            Link ingoingLink, XPathModel xPathModel,
            MapperLsmTree lsmTree, Vertex connectedVertex, int index) {
        //
        if (index == 0 && (connectedVertex instanceof Function) &&
                (connectedVertex.getDataObject() ==
                BpelXPathExtFunctionMetadata.GET_VARIABLE_PROPERTY_METADATA)) {
            //
            SourcePin sourcePin = ingoingLink.getSource();
            TreePath sourceTreePath = ((TreeSourcePin) sourcePin).getTreePath();
            TreePathInfo tpInfo = collectTreeInfo(sourceTreePath, lsmTree);
            return xPathModel.getFactory().newXPathStringLiteral(
                    tpInfo.varDecl.getVariableName());
        } else {
            return super.createIngoingLinkXPath(
                    ingoingLink, xPathModel, lsmTree, connectedVertex, index);
        }
    }

    @Override
    protected XPathExpression createVariableXPath(XPathModel xPathModel,
            TreePath sourceTreePath, MapperLsmTree lsmTree) {
        //
        TreePathInfo tpInfo = collectTreeInfo(sourceTreePath, lsmTree);
        return createVariableXPath(xPathModel, tpInfo);
    }

    protected XPathExpression createVariableXPath(
            XPathModel xPathModel, TreePathInfo tpInfo) {
        //
        if (tpInfo == null || tpInfo.varDecl == null) {
            return null;
        }

        if (tpInfo.property != null) {
            XPathModelFactory factory = xPathModel.getFactory();

            QName funcQName = BpelXPathExtFunctionMetadata
                    .GET_VARIABLE_PROPERTY_METADATA.getName();

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

            XPathExtensionFunction function = factory
                    .newXPathExtensionFunction(funcQName);

            XPathExpression variableNameLiteral = factory
                    .newXPathStringLiteral(tpInfo.varDecl.getVariableName());

            String propertyQName;

            propertyQName = PropertiesUtils.getQName(tpInfo.property,
                    getDesignContext());

            XPathExpression propertyNameLiteral = factory.newXPathStringLiteral(
                    propertyQName); // NOI18N

            function.addChild(variableNameLiteral);
            function.addChild(propertyNameLiteral);

            return function;
        }

        if (tpInfo.nmProperty != null) {
            XPathModelFactory factory = xPathModel.getFactory();

            QName funcQName = BpelXPathExtFunctionMetadata
                    .GET_VARIABLE_NM_PROPERTY_METADATA.getName();

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

            XPathExtensionFunction function = factory
                    .newXPathExtensionFunction(funcQName);

            XPathExpression variableNameLiteral = factory
                    .newXPathStringLiteral(tpInfo.varDecl.getVariableName());

            String propertyQName;

            propertyQName = tpInfo.nmProperty;

            XPathExpression propertyNameLiteral = factory.newXPathStringLiteral(
                    propertyQName); // NOI18N

            function.addChild(variableNameLiteral);
            function.addChild(propertyNameLiteral);

            return function;
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
            List<LocationStep> stepList = BpelPathConverter.constructLSteps(
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

    protected void populateContentHolder(ContentElement contentHolder, 
            BpelGraphInfoCollector graphInfo, MapperLsmTree lsmTree) {
        //
        XPathModel xPathModel = 
                BpelXPathModelFactory.create((BpelEntity)contentHolder);
        //
        XPathExprList exprList = buildXPathExprList(
                xPathModel, graphInfo, lsmTree);
        
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
    
    private XPathExprList buildXPathExprList(XPathModel xPathModel,
            BpelGraphInfoCollector graphInfo, MapperLsmTree lsmTree) {
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
        lsmTree.addLsms(treeItem, BpelMapperLsm.class);
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
        } else if (item instanceof BpelMapperPredicate) {
            sourceInfo.schemaCompList.addFirst(item);
        } else if (item instanceof BpelMapperTypeCast) {
            BpelMapperTypeCast typeCast = (BpelMapperTypeCast)item;
            Object castedObj = typeCast.getCastedObject();
            processItem(castedObj, sourceInfo);
        } else if (item instanceof BpelMapperPseudoComp) {
            BpelMapperPseudoComp pseudo = (BpelMapperPseudoComp)item;
            sourceInfo.schemaCompList.addFirst(pseudo);
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
        } else if (item instanceof CorrelationProperty) {
            sourceInfo.property = (CorrelationProperty) item;
        } else if (item instanceof PartnerLink) {
            sourceInfo.pLink =
                    (PartnerLink) item;
        } else if (item instanceof Roles) {
            sourceInfo.roles =
                    (Roles) item;
        } else if (item instanceof FileObject) {
            FileObject fileObject = (FileObject) item;
            if (!fileObject.isFolder()) {
                Object nmPropertyName = fileObject
                        .getAttribute(PropertiesConstants.NM_PROPERTY_ATTR);
                if (nmPropertyName instanceof String) {
                    sourceInfo.nmProperty = (String) nmPropertyName;
                }
            }
        } else if (item instanceof NMProperty) {
            NMProperty nmProperty = (NMProperty) item;
            String nmPropertyName = nmProperty.getNMProperty();

            if (nmPropertyName != null) {
                nmPropertyName = nmPropertyName.trim();
            } else {
                nmPropertyName = ""; // NOI18N
            }
            sourceInfo.nmProperty = nmPropertyName;
        }
    }
    
    //==========================================================================

    /**
     * Temporary container for collecting information about tree item.
     */
    protected class TreePathInfo {
        public VariableDeclaration varDecl;
        public Part part;
        public CorrelationProperty property;
        public String nmProperty;
        public LinkedList<Object> schemaCompList = new LinkedList<Object>();
        public PartnerLink pLink;
        public Roles roles;
    }
    
}
    
