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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.tree.TreePath;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperLsm;
import org.netbeans.modules.bpel.mapper.model.CopyToProcessor.CopyToForm;
import org.netbeans.modules.bpel.mapper.model.FromProcessor.FromForm;
import org.netbeans.modules.bpel.mapper.model.customitems.WrapServiceRefHandler;
import org.netbeans.modules.bpel.mapper.tree.models.DateValueTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.ForEachConditionsTreeModel;
import org.netbeans.modules.bpel.mapper.model.customitems.XmlLiteralDataObject;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Branches;
import org.netbeans.modules.bpel.model.api.CompletionCondition;
import org.netbeans.modules.bpel.model.api.ConditionHolder;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.Expression;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.FinalCounterValue;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.FromChild;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.Literal;
import org.netbeans.modules.bpel.model.api.Literal.LiteralForm;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.StartCounterValue;
import org.netbeans.modules.bpel.model.api.TimeEvent;
import org.netbeans.modules.bpel.model.api.TimeEventHolder;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.BpelXPathExtFunctionMetadata;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.soa.mappercore.model.Constant;
import org.netbeans.modules.soa.mappercore.model.Function;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperLsmTree;
import org.netbeans.modules.soa.xpath.mapper.lsm.ExtRegistrationException;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperTreeNode;
import org.netbeans.modules.soa.xpath.mapper.utils.XPathMapperUtils;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xpath.ext.AbstractLocationPath;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaUtils;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCast;
import org.openide.ErrorManager;

/**
 * Looks on the current state of the BPEL Mapper and modifies 
 * the BPEL model correspondingly.  
 * 
 * @author nk160297
 */
public class BpelModelUpdater extends DefaultBpelModelUpdater {
    // for fixing of the RFE
    // http://www.org.netbeans.org/issues/show_bug.cgi?id=168584
    // link from PartnerLink.myRole to EndpointRef
    private PartnerLinkEndpointRefHelper helperPLinkEndpointRef;

    public BpelModelUpdater(MapperTcContext mapperTcContext) {
        super(mapperTcContext);
    }

    /**
     * Implements Callable interface
     * @return
     * @throws java.lang.Exception
     */
    public Object updateOnChanges(TreePath treePath) throws Exception {
        //
        // TODO m
        BpelEntity bpelEntity = getDesignContext().getContextEntity();
        //
        Class<? extends BpelEntity> elementType = bpelEntity.getElementType();
        if (elementType == Assign.class) {
            updateAssign(treePath, (Assign) bpelEntity);
        } else if (elementType == Wait.class || 
                elementType == OnAlarmPick.class || 
                elementType == OnAlarmEvent.class) {
            TimeEvent timeEvent = ((TimeEventHolder)bpelEntity).getTimeEvent();
            updateTimeEventHolder(treePath, (TimeEventHolder)bpelEntity, timeEvent);
        } else if (elementType == If.class || 
                elementType == ElseIf.class || 
                elementType == While.class || 
                elementType == RepeatUntil.class) {
            // If, ElseIf, While, RepeatUntil
            BooleanExpr bpelExp = ((ConditionHolder)bpelEntity).getCondition();
            updateCondition(treePath, (ConditionHolder)bpelEntity, bpelExp);
        } else if (elementType == ForEach.class) {
            updateForEach(treePath, (ForEach)bpelEntity);
        }
        //
        return null; // TODO: return some result flag
    }
    
    //==========================================================================

    protected void updateFrom(Graph graph, MapperLsmTree lsmTree,
            From from, TreePath rightTreePath) throws Exception 
    {
        assert from != null;
        FromForm oldFromForm = FromProcessor.calculateFromForm(from);
        BpelGraphInfoCollector graphInfo = new BpelGraphInfoCollector(graph);
        // XmlLiteral
        if (graphInfo.isXmlLiteral()) {
            FromChild fromChild = from.getFromChild();
            Literal literal = null;
            VertexItem item = graphInfo.getPrimaryRoots().get(0).getItem(0);
            XmlLiteralDataObject dataObject = ((XmlLiteralDataObject) item.getDataObject());

            if (fromChild == null || !(fromChild instanceof Literal)) {
                literal = getDesignContext().getBpelModel().getBuilder().createLiteral();
            } else {
                literal = (Literal) fromChild;
            }
            from.removeEndpointReference();
            from.removePart();
            from.removePartnerLink();
            from.removeProperty();
            from.removeNMProperty();
            from.removeVariable();

            from.setFromChild(literal);

            LiteralForm literalForm = dataObject.getLiteralForm();
            switch (literalForm) {
                case EMPTY:
                    break;
                case TEXT_CONTENT:
                    literal.setContent(item.getText());
                    break;
                case SUBELEMENT:
                    literal.setXmlContent(item.getText());
                    break;
                case CDATA_SUBELEMENT:
                    literal.setCDataContent(item.getText());  // NOI18N
                    break;
            }
            return;
        }

        if  (isDinamicPartnerLink(graphInfo, rightTreePath) &&
            (! isPartnerLinkRole2PartnerLink(graphInfo, rightTreePath, lsmTree))) {
            Link link = graphInfo.getTransitLinks().get(0);
            BpelVertexFactory vertexFactory = BpelVertexFactory.getInstance();
            Function newVertex = null;
            newVertex = vertexFactory.createExtFunction(
                    BpelXPathExtFunctionMetadata.DO_XSL_TRANSFORM_METADATA);
            assert newVertex != null;
            VertexItem item0 = newVertex.getItem(0);

            Object vertexItemDataObject = item0.getDataObject();
            XPathType arg0Type = null;
            if (vertexItemDataObject instanceof ArgumentDescriptor) {
                arg0Type = ((ArgumentDescriptor) vertexItemDataObject).getArgumentType();
            }

            assert arg0Type != null && arg0Type.equals(XPathType.STRING_TYPE);
            Constant uriVertex = vertexFactory.createStringLiteral(
                    WrapServiceRefHandler.XSLTRANSFORM_URN_PREFIX + 
                    WrapServiceRefHandler.WRAP2SERVICEREF_XSL);

            new Link(uriVertex, item0);
            new Link(link.getSource(), newVertex.getItem(1));
            
            XPathModel xPathModel = BpelXPathModelFactory.create(from);
            XPathExpression expr = createXPathRecursive(
                    xPathModel, newVertex, lsmTree);
            
            from.setContent(expr.getExpressionString());
            
            WrapServiceRefHandler.createWrap2ServiceRefFileObject();
            return;
        }
        
        if (graphInfo.onlyOneTransitLink()) {
            // Only one link from the left to the right tree
            // 
            // Take the first link, which is the only.
            Link link = graphInfo.getTransitLinks().get(0); 
            TreePathInfo tpInfo = getLinkSourceTreePathInfo(link, lsmTree);
            //
            
            XPathModel xPathModel = BpelXPathModelFactory.create(from);
            populateFrom(from, xPathModel, tpInfo, rightTreePath);
        } else {
            boolean processed = false;
            // 
            // Process specific cases
            if (oldFromForm != null) {
                switch (oldFromForm) {
                    // Old From has nested literal
                    case LITERAL: 
                        if (graphInfo.getPrimaryRoots().size() == 1 &&
                                graphInfo.getSecondryRoots().size() == 0) {
                            Vertex rootVertex = graphInfo.getPrimaryRoots().get(0);
                            Object dataObj = rootVertex.getItem(0).getDataObject();
                            
                            if (dataObj == XmlLiteralDataObject.class) {
                                // The vertex represents a string constant
                                VertexItem firstVItem = rootVertex.getItem(0);
                                String literalValue = firstVItem.getText();
                                //
                                FromChild literal = from.getFromChild(); // literal
                                if (literal != null && literal instanceof Literal) {
                                    ((Literal)literal).setContent(literalValue);
                                    //
                                    from.removeEndpointReference();
                                    from.removePart();
                                    from.removePartnerLink();
                                    from.removeProperty();
                                    from.removeVariable();
                                    //
                                    processed = true;
                                }
                            }
                        }
                        break;
                }
            }
            //
            //
            if (!processed) {
                // Complex case when an XPath expression form definitly has to be used
                // 
                populateContentHolder(from, graphInfo, lsmTree);
                //
                // Remove other attributes because they are not necessary now.
                from.removeEndpointReference();
                from.removeFromChild();
                from.removePart();
                from.removePartnerLink();
                from.removeProperty();
                from.removeNMProperty();
                from.removeVariable();
            }
        }
    }

    private TreePathInfo getLinkSourceTreePathInfo(Link link, MapperLsmTree mapperLsmTree) {
        if (link == null) return null;

        TreeSourcePin sourcePin = (TreeSourcePin)link.getSource();
        TreePath sourceTreePath = sourcePin.getTreePath();
        TreePathInfo tpInfo = collectTreeInfo(sourceTreePath, mapperLsmTree);
        return tpInfo;
    }

    private boolean isPartnerLinkTreeNode(TreePath treePath) {
        if (treePath == null) return false;

        Object node  = treePath.getLastPathComponent();
        Object dataObject = null;
        if (node instanceof MapperTreeNode) {
            dataObject = ((MapperTreeNode) node).getDataObject();
        }
        return (dataObject instanceof PartnerLink);
    }

    private boolean isDinamicPartnerLink(BpelGraphInfoCollector graphInfo,
        TreePath rightTreePath) {
        if ((graphInfo == null) || (rightTreePath == null)) return false;

        return (graphInfo.onlyOneTransitLink() && isPartnerLinkTreeNode(rightTreePath));
    }

    private boolean isPartnerLinkRole2PartnerLink(BpelGraphInfoCollector graphInfo,
        TreePath rightTreePath, MapperLsmTree mapperLsmTree) {
        if ((graphInfo == null) || (rightTreePath == null) || (mapperLsmTree == null)) {
            return false;
        }

        boolean isRightNodePartnerLink = isPartnerLinkTreeNode(rightTreePath);

        Link link = graphInfo.getTransitLinks().get(0);
        TreePathInfo srcTreePathInfo = getLinkSourceTreePathInfo(link, mapperLsmTree);
        boolean isLeftNodePartnerRole = (srcTreePathInfo.roles != null) &&
            (! srcTreePathInfo.roles.isInvalid());

        return (isRightNodePartnerLink && isLeftNodePartnerRole);
    }

    private void updateCopy(TreePath rightTreePath, Copy copy) throws Exception {
        //
        // Do common preparations
        //
        Graph graph = getMapperModel().graphRequired(rightTreePath);
        //
        //=====================================================================
        //
        // Remove copy if there is not any content in the graph 
        //
        if (graph.isEmpty()) {
            // Remove copy from the BPEL model
            BpelContainer copyOwner = copy.getParent();
            if (copyOwner != null) {
                copyOwner.remove(copy);
            }
            getMapperModel().deleteGraph(rightTreePath); // Remove empty graph !!!
            return; // NOTHING TO DO FURTHER
        }
        //
        //
        //=====================================================================
        // Populate FROM
        //
        // Recreate the From as a whole
        From from = copy.getFrom();
        if (from != null) {
            copy.remove(from);
        }
        BpelModel bpelModel = copy.getBpelModel();
        from = bpelModel.getBuilder().createFrom();
        copy.setFrom(from);


//        if (from == null) {
//            BpelModel bpelModel = copy.getBpelModel();
//            from = bpelModel.getBuilder().createFrom();
//            copy.setFrom(from);
//        }
        //
//        LsmProcessor.deleteAllLsm(from);
        //
        MapperLsmTree lsmTree = new MapperLsmTree(getTcContext(), true); // left tree
        updateFrom(graph, lsmTree, from, rightTreePath);
        //
        getMapperLsmProcessor().registerAll(from, lsmTree, true);
        //
        //=====================================================================
        // Populate TO
        //
        // Recreate the To as a whole
        To to = copy.getTo();
        if (to != null) {
            copy.remove(to);
        }
        bpelModel = copy.getBpelModel();
        to = bpelModel.getBuilder().createTo();
        copy.setTo(to);


//        if (to == null) {
//            bpelModel = copy.getBpelModel();
//            to = bpelModel.getBuilder().createTo();
//            copy.setTo(to);
//        }
//        //
//        LsmProcessor.deleteAllLsm(to);
        //
        lsmTree = new MapperLsmTree(getTcContext(), false); // right tree
        TreePathInfo tpInfo = collectTreeInfo(rightTreePath, lsmTree);
        //
        XPathModel xPathModel = BpelXPathModelFactory.create(to);
        populateTo(to, xPathModel, tpInfo);
        getMapperLsmProcessor().registerAll(to, lsmTree, false);
    }
    
    private void updateAssign(TreePath rightTreePath, Assign assign) throws Exception {
        Graph graph = getMapperModel().graphRequired(rightTreePath);
        //
        Object dataObject = graph.getDataObject();
        //
        // for fixing of http://www.org.netbeans.org/issues/show_bug.cgi?id=168584
        // link from PartnerLink.myRole to EndpointRef
        helperPLinkEndpointRef = new PartnerLinkEndpointRefHelper();
        helperPLinkEndpointRef.setUpdatedAssign(assign);
        helperPLinkEndpointRef.setBpelModelUpdater(this);
        //
        if (dataObject instanceof Copy) {
            // Process a copy
            updateCopy(rightTreePath, (Copy)dataObject);
        } else if (dataObject == null) {
            // absence of data object means that it is necessary to create a new Copy
            BpelModel bpelModel = assign.getBpelModel();
            Copy newCopy = bpelModel.getBuilder().createCopy();
            assign.addAssignChild(newCopy);
            //
            updateCopy(rightTreePath, newCopy);
            //
            graph.setDataObject(newCopy);
        }
        // for fixing of http://www.org.netbeans.org/issues/show_bug.cgi?id=168584
        // link from PartnerLink.myRole to EndpointRef
        helperPLinkEndpointRef.insertNewGeneratedCopy();
    }

    /**
     * Checks if the CopyTo has the expression with the specified
     * target component at the end.
     * @param copy
     * @param copyTo
     * @param targetComp
     * @return
     */
    private boolean checkCopyTargetType(
            Copy copy, To copyTo, SchemaComponent targetComp) {
        //
        assert copy != null && copyTo != null && targetComp != null;
        //
        CopyToForm form = CopyToProcessor.getCopyToForm(copyTo);
        XPathExpression toExpr = null;
        if (form == CopyToForm.EXPRESSION) {
            toExpr = CopyToProcessor.constructExpression(
                    copy, copyTo, null, null);
            if (toExpr instanceof AbstractLocationPath) {
                XPathSchemaContext sCtxt = AbstractLocationPath.
                        class.cast(toExpr).getSchemaContext();
                if (sCtxt != null) {
                    SchemaComponent ctxtComp = XPathSchemaContext.
                            Utilities.getSchemaComp(sCtxt);
                    if (ctxtComp == targetComp) {
                        return true;
                    }
                }
            }
        }
        //
        return false;
    }

    public void updateXsiTypeExpression() throws VetoException {
        BpelEntity bpelEntity = getDesignContext().getContextEntity();
        //
        Class<? extends BpelEntity> elementType = bpelEntity.getElementType();

        if (elementType == Assign.class) {
            Assign assign = (Assign) bpelEntity;
            //
            //delete copy with xsi:type
            GlobalAttribute xsiTypeAttr = SchemaUtils.getXsiTypeAttr();
            //
            for (int i = assign.getAssignChildren().length - 1; i >= 0; i--) {
                if (assign.getAssignChild(i) instanceof Copy) {
                    Copy copy = (Copy) assign.getAssignChild(i);
                    if (copy != null) {
                        To copyTo = copy.getTo();
                        if (checkCopyTargetType(copy, copyTo, xsiTypeAttr)) {
                            assign.removeAssignChild(i);
                        }
                    }
                }
            }
            //
            // add new @xsi:type copies
            //
            BpelMapperLsmProcessor mLsmProcessor = 
                    new BpelMapperLsmProcessor(getTcContext());
            Map<XPathSchemaContext, XPathCast> addXsiTypeCopy =
                    new HashMap<XPathSchemaContext, XPathCast>();
            
            for (int i = assign.getAssignChildren().length - 1; i >= 0; i--) {
                if (!(assign.getAssignChild(i) instanceof Copy)) {
                    continue;
                }
                To to = ((Copy) assign.getAssignChild(i)).getTo();

                List<XPathCast> toCasts = mLsmProcessor.collectsLsmXsiType(to);
                if (toCasts != null) {
                    for (XPathCast xCast : toCasts) {
                        addXsiTypeCopy.put(xCast.getSchemaContext(), xCast);
                    }
                }
            }
            //
            // prepare to @xsi:type copies creation
            ExNamespaceContext ownerNsContext = assign.getNamespaceContext();
            String xsiPrefix = null;
            if (!addXsiTypeCopy.isEmpty()) {
                xsiPrefix = checkXsiPrefixDeclared(ownerNsContext);
            }
            //
            for (XPathSchemaContext pathSContext : addXsiTypeCopy.keySet()) {
                BpelModel bpelModel = assign.getBpelModel();
                Copy newCopy = bpelModel.getBuilder().createCopy();
                assign.addAssignChild(newCopy);
                //
                To to = bpelModel.getBuilder().createTo();
                newCopy.setTo(to);
                //
                assert xsiPrefix != null;
                String text = pathSContext.getExpressionString(ownerNsContext, null) +
                        "/@" + xsiPrefix + ":type";
                to.setContent(text);
                
                MapperLsmTree mapperLsmtree = new MapperLsmTree(getTcContext(), false); // right tree
                
                DirectedList<BpelMapperLsm> list =
                        XPathMapperUtils.extractLsms(
                        BpelPathConverter.singleton().constructObjectLocationList(
                        addXsiTypeCopy.get(pathSContext).getSchemaContext(), true, false),
                        BpelMapperLsm.class);
                
                if (list != null && !list.isEmpty()) {
                    mapperLsmtree.addLsmList(list);
                    try {
                        mLsmProcessor.registerAll(to, mapperLsmtree, false);
                    } catch (ExtRegistrationException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
                
                From from = bpelModel.getBuilder().createFrom();
                Literal literal = bpelModel.getBuilder().createLiteral();
                
                newCopy.setFrom(from);
                from.setFromChild(literal);
                
                GlobalType type = addXsiTypeCopy.get(pathSContext).getType();
                if (type == null || type.getModel() == null) {
                    assign.removeAssignChild(assign.getAssignChildren().length - 1);
                    return;
                    //Exception;
                }
                
                
                String nameSpace = type.getModel().getEffectiveNamespace(type);
                String name = type.getName();
                
                String prefix = assign.getNamespaceContext().getPrefix(nameSpace);
                if (prefix == null) {
                    prefix = nameSpace;
                }
                
                if (prefix == null || prefix.equals("")) {
                    assign.removeAssignChild(assign.getAssignChildren().length - 1);
                    return; 
                }
                
                literal.setContent(prefix + ":" + name);
            }
        }
    }

    /**
     * Creates a new prefix for the "XML Schema Instance" schema
     * @param nsCtxt
     * @return the prefix
     */
    private String checkXsiPrefixDeclared(ExNamespaceContext nsCtxt) {
        String prefix = nsCtxt.getPrefix(
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        //
        if (prefix == null) {
            try {
                prefix = nsCtxt.addNamespace(
                        XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
                assert prefix != null;
            } catch (InvalidNamespaceException ex) {
                assert false : "shouldn't happen!"; // NOI18N
            }
        }
        return prefix;
    }

    /**
     * 
     * @param rightTreePath
     * @param condHolder It is implied that the parameter is a BpelEntity.
     */
    private void updateCondition(TreePath rightTreePath,
            ConditionHolder condHolder, BooleanExpr condition)
            throws ExtRegistrationException {
        //
        // Do common preparations
        //
        Graph graph = getMapperModel().graphRequired(rightTreePath);
        //
        BpelGraphInfoCollector graphInfo = new BpelGraphInfoCollector(graph);
        //
        //=====================================================================
        // Create Condition
        //
        // Recreate condition
        if (condition != null) {
            condHolder.removeCondition();
        }
        BpelModel bpelModel = ((BpelEntity)condHolder).getBpelModel();
        condition = bpelModel.getBuilder().createCondition();
        condHolder.setCondition(condition);
        condition = condHolder.getCondition();


//        if (condition == null) {
//            BpelModel bpelModel = ((BpelEntity)condHolder).getBpelModel();
//            condition = bpelModel.getBuilder().createCondition();
//            condHolder.setCondition(condition);
//            condition = condHolder.getCondition();
//        }
        //
        ExtensibleElements destination = (ExtensibleElements)condition;
//        LsmProcessor.deleteAllLsm(destination);
        //
        MapperLsmTree lsmTree = new MapperLsmTree(getTcContext(), true); // left tree
        //
        populateContentHolder(condition, graphInfo, lsmTree);
        getMapperLsmProcessor().registerAll(destination, lsmTree, true);
    }
    
    private void updateTimeEventHolder(TreePath rightTreePath, 
            TimeEventHolder timeEventHolder, TimeEvent timeEvent)
            throws ExtRegistrationException {
        //
        // Do common preparations
        //
        Graph graph = getMapperModel().graphRequired(rightTreePath);
        //
        BpelGraphInfoCollector graphInfo = new BpelGraphInfoCollector(graph);
        //
        //=====================================================================
        // Populate Condition
        //
        // Recreate time event
        if (timeEvent != null) {
            timeEventHolder.remove(timeEvent);
        }
        BpelModel bpelModel = ((BpelEntity)timeEventHolder).getBpelModel();
        Object rightTreeDO = MapperSwingTreeModel.getDataObject(rightTreePath);
        //
        if (DateValueTreeModel.DEADLINE_CONDITION.equals(rightTreeDO)) {
            timeEvent = bpelModel.getBuilder().createUntil();
        } else if (DateValueTreeModel.DURATION_CONDITION.equals(rightTreeDO)) {
            timeEvent = bpelModel.getBuilder().createFor();
        } else {
            return;
        }
        //
        timeEventHolder.setTimeEvent(timeEvent);
        timeEvent = timeEventHolder.getTimeEvent();
        //
//        if (timeEvent == null) {
//            BpelModel bpelModel = ((BpelEntity)timeEventHolder).getBpelModel();
//            Object rightTreeDO = BpelMapperSwingTreeModel.getDataObject(rightTreePath);
//            //
//            if (DateValueTreeModel.DEADLINE_CONDITION.equals(rightTreeDO)) {
//                timeEvent = bpelModel.getBuilder().createUntil();
//            } else if (DateValueTreeModel.DURATION_CONDITION.equals(rightTreeDO)) {
//                timeEvent = bpelModel.getBuilder().createFor();
//            } else {
//                return;
//            }
//            //
//            timeEventHolder.setTimeEvent(timeEvent);
//            timeEvent = timeEventHolder.getTimeEvent();
//        }
        //
        ExtensibleElements el = (ExtensibleElements)timeEvent;
//        LsmProcessor.deleteAllLsm(el);
        //
        MapperLsmTree lsmTree = new MapperLsmTree(getTcContext(), true); // left tree
        //
        populateContentHolder((ContentElement)timeEvent, graphInfo, lsmTree);
        getMapperLsmProcessor().registerAll(el, lsmTree, true);
    }
    
    private void updateForEach(TreePath rightTreePath, ForEach forEach) 
            throws ExtRegistrationException {
        //
        // Do common preparations
        //
        Graph graph = getMapperModel().graphRequired(rightTreePath);
        //
        BpelGraphInfoCollector graphInfo = new BpelGraphInfoCollector(graph);
        //
        BpelModel bpelModel = forEach.getBpelModel();
        //
        // Create BPEL ForEach structures
        //
        Object rightTreeDO = MapperSwingTreeModel.getDataObject(rightTreePath);
        Expression bpelExpr = null;
        //
        if (ForEachConditionsTreeModel.START_VALUE.equals(rightTreeDO)) {
            StartCounterValue startValue = forEach.getStartCounterValue();
            //
            // Recreate start value
            if (startValue != null) {
                forEach.remove(startValue);
            }
            startValue = bpelModel.getBuilder().createStartCounterValue();
            forEach.setStartCounterValue(startValue);
            startValue = forEach.getStartCounterValue();
            //
//            if (startValue == null) {
//                startValue = bpelModel.getBuilder().createStartCounterValue();
//                forEach.setStartCounterValue(startValue);
//                startValue = forEach.getStartCounterValue();
//            }
            bpelExpr = startValue;
        } else if (ForEachConditionsTreeModel.FINAL_VALUE.equals(rightTreeDO)) {
            FinalCounterValue finalValue = forEach.getFinalCounterValue();
            //
            // Recreate final value
            if (finalValue != null) {
                forEach.remove(finalValue);
            }
            finalValue = bpelModel.getBuilder().createFinalCounterValue();
            forEach.setFinalCounterValue(finalValue);
            finalValue = forEach.getFinalCounterValue();
            //
//            if (finalValue == null) {
//                finalValue = bpelModel.getBuilder().createFinalCounterValue();
//                forEach.setFinalCounterValue(finalValue);
//                finalValue = forEach.getFinalCounterValue();
//            }
            bpelExpr = finalValue;
        } else if (ForEachConditionsTreeModel.COMPLETION_CONDITION.equals(rightTreeDO)) {
            CompletionCondition completionCond = forEach.getCompletionCondition();
            if (completionCond == null) {
                completionCond = bpelModel.getBuilder().createCompletionCondition();
                forEach.setCompletionCondition(completionCond);
                completionCond = forEach.getCompletionCondition();
            }
            //
            //
            // Recreate final value
            Branches branches = completionCond.getBranches();
            if (branches != null) {
                completionCond.remove(branches);
            }
            branches = bpelModel.getBuilder().createBranches();
            completionCond.setBranches(branches);
            branches = completionCond.getBranches();
            //
//            if (branches == null) {
//                branches = bpelModel.getBuilder().createBranches();
//                completionCond.setBranches(branches);
//                branches = completionCond.getBranches();
//            }
            bpelExpr = branches;
        } else {
            return;
        }
        //
        ExtensibleElements extansible = (ExtensibleElements)bpelExpr;
//        LsmProcessor.deleteAllLsm(extansible);
        //
        // Populate
        MapperLsmTree lsmTree = new MapperLsmTree(getTcContext(), true); // left tree
        //
        populateContentHolder(bpelExpr, graphInfo, lsmTree);
        getMapperLsmProcessor().registerAll(extansible, lsmTree, true);
    }
    
    //==========================================================================
    From populateFrom(From from, XPathModel xPathModel, TreePathInfo tpInfo,
        TreePath rightTreePath) {
        FromProcessor.FromForm fromForm = calculateCopyFromForm(tpInfo);
        //
        switch(fromForm) {
            case LITERAL: {
               // System.out.println("LITERAL!!!!!!!!!!!!!!!1");
            }
        case VAR: {
            BpelReference<VariableDeclaration> varRef = 
                    from.createReference(tpInfo.varDecl, VariableDeclaration.class);
            from.setVariable(varRef);

            // from.removeVariable();
            from.removePart();
            from.removeFromChild();
            from.removePartnerLink();
            from.removeEndpointReference();
            from.removeProperty();
            from.removeNMProperty();
            try {
                from.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
            }
            break;
        }
        case VAR_PART: {
            BpelReference<VariableDeclaration> varRef = from.
                    createReference(tpInfo.varDecl, VariableDeclaration.class);
            from.setVariable(varRef);
            //
            WSDLReference<Part> partRef = from.
                    createWSDLReference(tpInfo.part, Part.class);
            from.setPart(partRef);
            //
            // from.removeVariable();
            // from.removePart();
            from.removeFromChild();
            from.removePartnerLink();
            from.removeEndpointReference();
            from.removeProperty();
            from.removeNMProperty();
            try {
                from.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
            }
            break;
        }
        case VAR_PROPERTY: {
            BpelReference<VariableDeclaration> varRef = from
                    .createReference(tpInfo.varDecl, VariableDeclaration.class);

            WSDLReference<CorrelationProperty> propertyRef = from
                    .createWSDLReference(tpInfo.property, CorrelationProperty
                    .class);
            
            from.setVariable(varRef);
            from.setProperty(propertyRef);
            from.removeFromChild();
            from.removePartnerLink();
            from.removeEndpointReference();
            from.removePart();
            from.removeNMProperty();
            
            try {
                from.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
            }
            
            break;
        }
        case VAR_NM_PROPERTY: {
            BpelReference<VariableDeclaration> varRef = from
                    .createReference(tpInfo.varDecl, VariableDeclaration.class);

            String nmProperty = tpInfo.nmProperty;
            
            from.setVariable(varRef);
            from.removeFromChild();
            from.removePartnerLink();
            from.removeEndpointReference();
            from.removePart();
            from.removeProperty();
            
            try {
                from.setNMProperty(nmProperty);
            } catch (VetoException ex) {
            }
            
            try {
                from.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
            }
            
            break;
        }
        case VAR_QUERY: {
            BpelReference<VariableDeclaration> varRef = from.
                    createReference(tpInfo.varDecl, VariableDeclaration.class);
            from.setVariable(varRef);
            //
            SchemaComponent sComp = EditorUtil.getVariableSchemaType(tpInfo.varDecl);
            assert sComp != null;
            SchemaModelsStack sms = new SchemaModelsStack();
            sms.appendSchemaComponent(sComp);
            //
            populateFromQuery(from, xPathModel, tpInfo, sms);
            //
            // from.removeVariable();
            from.removePart();
            // from.removeFromChild();
            from.removePartnerLink();
            from.removeEndpointReference();
            from.removeProperty();
            from.removeNMProperty();
            try {
                from.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
            }
            break;
        }
        case VAR_PART_QUERY: {
            BpelReference<VariableDeclaration> varRef = from.
                    createReference(tpInfo.varDecl, VariableDeclaration.class);
            from.setVariable(varRef);
            //
            WSDLReference<Part> partRef = from.
                    createWSDLReference(tpInfo.part, Part.class);
            from.setPart(partRef);
            //
            SchemaComponent sComp = EditorUtil.getPartType(tpInfo.part);
            assert sComp != null;
            SchemaModelsStack sms = new SchemaModelsStack();
            sms.appendSchemaComponent(sComp);
            //
            populateFromQuery(from, xPathModel, tpInfo, sms);
            //
            // from.removeVariable();
            // from.removePart();
            // from.removeFromChild();
            from.removePartnerLink();
            from.removeEndpointReference();
            from.removeProperty();
            from.removeNMProperty();
            try {
                from.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
            }
            break;
        }
        case PARTNER_LINK: {
            // for fixing of the RFE
            // http://www.org.netbeans.org/issues/show_bug.cgi?id=168584
            // link from PartnerLink.myRole to EndpointRef
            if (rightTreePath != null) {
                Object node  = rightTreePath.getLastPathComponent();
                if (node instanceof MapperTreeNode) {
                    Object dataObj = ((MapperTreeNode) node).getDataObject();
                    // check helperPLinkEndpointRef != null for Logging posibilities
                    if (helperPLinkEndpointRef != null) {
                        From eprFrom = helperPLinkEndpointRef.handlePartnerLinkEndpointRef(from,
                                xPathModel, tpInfo, rightTreePath, dataObj);
                        if (eprFrom != null) {
                            return eprFrom;
                        }
                    }
                }
            }
            //
            BpelReference<PartnerLink> pLinkRef = from.
                    createReference(tpInfo.pLink, PartnerLink.class);
            from.setPartnerLink(pLinkRef);
            //
            if (tpInfo.roles != null) {
                from.setEndpointReference(tpInfo.roles);
            }
            //
            from.removeVariable();
            from.removePart();
            from.removeFromChild();
            // from.removePartnerLink();
            // from.removeEndpointReference();
            from.removeProperty();
            from.removeNMProperty();
            try {
                from.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
            }
            break;
        }
        case EXPRESSION: {
            XPathExpression xPathExpr = createVariableXPath(xPathModel, tpInfo);
            if (xPathExpr != null) {
                try {
                    NamespaceContext nsContext = from.getNamespaceContext();
                    from.setContent(xPathExpr.getExpressionString(nsContext));
                } catch (VetoException ex) {
                    // Do nothing
                }
            }
            //
            from.removeVariable();
            from.removePart();
            from.removeFromChild();
            from.removePartnerLink();
            from.removeEndpointReference();
            from.removeProperty();
            from.removeNMProperty();
            // from.setContent(null);
            break;
        }
        }
        //
        return from;
    }

    private void populateFromQuery(From from, XPathModel xPathModel, 
            TreePathInfo tpInfo, SchemaModelsStack sms) {
        List<LocationStep> stepList = BpelPathConverter.constructLSteps(
                xPathModel, tpInfo.schemaCompList, sms);
        if (stepList != null && !(stepList.isEmpty())) {
            XPathLocationPath locationPath = xPathModel.getFactory().
                    newXPathLocationPath(
                    stepList.toArray(new LocationStep[stepList.size()]));
            //
            if (locationPath != null) {
                xPathModel.fillInStubs(locationPath);
                //
                BpelModel bpelModel = getDesignContext().getBpelModel();
                FromChild fromChild = from.getFromChild();
                try {
                    if (fromChild != null && fromChild instanceof Query) {
                        Query query = (Query)fromChild;
                        query.setContent(locationPath.getExpressionString());
                    } else {
                        Query query = bpelModel.getBuilder().createQuery();
                        query.setContent(locationPath.getExpressionString());
                        from.setFromChild(query);
                    }
                } catch (VetoException ex) {
                    // Do nothing
                }
            }
        }
    }
    
    private To populateTo(To to, XPathModel xPathModel, TreePathInfo tpInfo) {
        CopyToProcessor.CopyToForm toForm = calculateCopyToForm(tpInfo);
        //
        switch(toForm) {
        case VAR: {
            BpelReference<VariableDeclaration> varRef = to.
                    createReference(tpInfo.varDecl, VariableDeclaration.class);
            to.setVariable(varRef);
            //
            // to.removeVariable();
            to.removePart();
            to.removeQuery();
            to.removePartnerLink();
            to.removeProperty();
            to.removeNMProperty();
            try {
                to.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
            }
            break;
        }
        case VAR_PART: {
            BpelReference<VariableDeclaration> varRef = to.
                    createReference(tpInfo.varDecl, VariableDeclaration.class);
            to.setVariable(varRef);
            //
            WSDLReference<Part> partRef = to.
                    createWSDLReference(tpInfo.part, Part.class);
            to.setPart(partRef);
            //
            // to.removeVariable();
            // to.removePart();
            to.removeQuery();
            to.removePartnerLink();
            to.removeProperty();
            to.removeNMProperty();
            try {
                to.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
            }
            break;
        }
        case VAR_PROPERTY: {
            BpelReference<VariableDeclaration> varRef = to.
                    createReference(tpInfo.varDecl, VariableDeclaration.class);
            to.setVariable(varRef);
            
            WSDLReference<CorrelationProperty> propertyRef = to
                    .createWSDLReference(tpInfo.property, CorrelationProperty
                    .class);
            
            to.setProperty(propertyRef);
            to.removeNMProperty();
            to.removePart();
            to.removePartnerLink();
            to.removeQuery();

            try {
                to.setContent(null);
            } catch (VetoException ex) {
                // do nothing
            }
            
            break;
        }
        case VAR_NM_PROPERTY: {
            BpelReference<VariableDeclaration> varRef = to.
                    createReference(tpInfo.varDecl, VariableDeclaration.class);
            to.setVariable(varRef);
            
            String nmProperty = tpInfo.nmProperty;
            
            try {
                to.setNMProperty(tpInfo.nmProperty);
            } catch (VetoException ex) {

            }
            to.removeProperty();
            to.removePart();
            to.removePartnerLink();
            to.removeQuery();

            try {
                to.setContent(null);
            } catch (VetoException ex) {
                // do nothing
            }
            
            break;
        }
        case VAR_QUERY: {
            BpelReference<VariableDeclaration> varRef = to.
                    createReference(tpInfo.varDecl, VariableDeclaration.class);
            to.setVariable(varRef);
            //
            SchemaComponent sComp = EditorUtil.getVariableSchemaType(tpInfo.varDecl);
            assert sComp != null;
            SchemaModelsStack sms = new SchemaModelsStack();
            sms.appendSchemaComponent(sComp);
            //
            populateToQuery(to, xPathModel, tpInfo, sms);
            //
            // to.removeVariable();
            to.removePart();
            // to.removeQuery();
            to.removePartnerLink();
            to.removeProperty();
            to.removeNMProperty();
            try {
                to.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
            }
            break;
        }
        case VAR_PART_QUERY: {
            BpelReference<VariableDeclaration> varRef = to.
                    createReference(tpInfo.varDecl, VariableDeclaration.class);
            to.setVariable(varRef);
            //
            WSDLReference<Part> partRef = to.
                    createWSDLReference(tpInfo.part, Part.class);
            to.setPart(partRef);
            //
            SchemaComponent sComp = EditorUtil.getPartType(tpInfo.part);
            assert sComp != null;
            SchemaModelsStack sms = new SchemaModelsStack();
            sms.appendSchemaComponent(sComp);
            //
            populateToQuery(to, xPathModel, tpInfo, sms);
            //
            // to.removeVariable();
            // to.removePart();
            // to.removeQuery();
            // to.removeFromChild();
            to.removePartnerLink();
            to.removeProperty();
            to.removeNMProperty();
            try {
                to.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
            }
            break;
        }
        case PARTNER_LINK: {
            BpelReference<PartnerLink> pLinkRef = to.
                    createReference(tpInfo.pLink, PartnerLink.class);
            to.setPartnerLink(pLinkRef);
            //
            //
            to.removeVariable();
            to.removePart();
            to.removeQuery();
            // to.removePartnerLink();
            to.removeProperty();
            to.removeNMProperty();
            try {
                to.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
            }
            break;
        }
        case EXPRESSION: {
            XPathExpression xPathExpr = createVariableXPath(xPathModel, tpInfo);
            try {
                to.setContent(xPathExpr.getExpressionString());
            } catch (VetoException ex) {
                // Do nothing
            }
            //
            to.removeVariable();
            to.removePart();
            to.removeQuery();
            to.removePartnerLink();
            to.removeProperty();
            to.removeNMProperty();
            // to.setContent(null);
            break;
        }
        }
        //
        return to;
    }

    private void populateToQuery(To to, XPathModel xPathModel, 
            TreePathInfo tpInfo, SchemaModelsStack sms) {
        List<LocationStep> stepList = BpelPathConverter.constructLSteps(
                xPathModel, tpInfo.schemaCompList, sms);
        if (stepList != null && !(stepList.isEmpty())) {
            XPathLocationPath locationPath = xPathModel.getFactory().
                    newXPathLocationPath(
                    stepList.toArray(new LocationStep[stepList.size()]));
            //
            if (locationPath != null) {
                xPathModel.fillInStubs(locationPath);
                //
                BpelModel bpelModel = getDesignContext().getBpelModel();
                Query query = to.getQuery();
                try {
                    if (query != null) {
                        query.setContent(locationPath.getExpressionString());
                    } else {
                        query = bpelModel.getBuilder().createQuery();
                        query.setContent(locationPath.getExpressionString());
                        to.setQuery(query);
                    }
                } catch (VetoException ex) {
                    // Do nothing
                }
            }
        }
    }
    
    //==========================================================================

    private FromProcessor.FromForm calculateCopyFromForm(TreePathInfo tpInfo) {
        if (tpInfo.varDecl != null) {
            if (tpInfo.part == null 
                    && tpInfo.property == null 
                    && tpInfo.nmProperty == null
                    && tpInfo.schemaCompList.isEmpty()) 
            {
                return FromProcessor.FromForm.VAR;
            } else if (tpInfo.part != null && tpInfo.schemaCompList.isEmpty()) {
                return FromProcessor.FromForm.VAR_PART;
            } else if (tpInfo.property != null
                    && tpInfo.schemaCompList.isEmpty()) 
            {
                return FromProcessor.FromForm.VAR_PROPERTY;
            } else if (tpInfo.nmProperty != null
                    && tpInfo.schemaCompList.isEmpty()) 
            {
                return FromProcessor.FromForm.VAR_NM_PROPERTY;
            } else if (tpInfo.part == null && !tpInfo.schemaCompList.isEmpty()) {
                // return CopyFromProcessor.CopyFromForm.VAR_QUERY;
                return FromProcessor.FromForm.EXPRESSION;
            } else if (tpInfo.part != null && !tpInfo.schemaCompList.isEmpty()) {
                // return CopyFromProcessor.CopyFromForm.VAR_PART_QUERY;
                return FromProcessor.FromForm.EXPRESSION;
            }
        } else if (tpInfo.pLink != null) {
            return FromProcessor.FromForm.PARTNER_LINK;
        }
        return FromProcessor.FromForm.UNKNOWN;
    }
    
    private CopyToProcessor.CopyToForm calculateCopyToForm(TreePathInfo tpInfo) {
        if (tpInfo.varDecl != null) {
            if (tpInfo.part == null 
                    && tpInfo.property == null 
                    && tpInfo.nmProperty == null
                    && tpInfo.schemaCompList.isEmpty()) 
            {
                return CopyToProcessor.CopyToForm.VAR;
            } else if (tpInfo.part != null && tpInfo.schemaCompList.isEmpty()) {
                return CopyToProcessor.CopyToForm.VAR_PART;
            } else if (tpInfo.property != null 
                    && tpInfo.schemaCompList.isEmpty())
            {
                return CopyToProcessor.CopyToForm.VAR_PROPERTY;
            } else if (tpInfo.nmProperty != null 
                    && tpInfo.schemaCompList.isEmpty()) 
            {
                return CopyToProcessor.CopyToForm.VAR_NM_PROPERTY;
            } else if (tpInfo.part == null && !tpInfo.schemaCompList.isEmpty()) {
                // return CopyToProcessor.CopyToForm.VAR_QUERY;
                return CopyToProcessor.CopyToForm.EXPRESSION;
            } else if (tpInfo.part != null && !tpInfo.schemaCompList.isEmpty()) {
                // return CopyFromProcessor.CopyFromForm.VAR_PART_QUERY;
                return CopyToProcessor.CopyToForm.EXPRESSION;
            }
        } else if (tpInfo.pLink != null) {
            return CopyToProcessor.CopyToForm.PARTNER_LINK;
        }
        return CopyToProcessor.CopyToForm.UNKNOWN;
    }
    
}
    
