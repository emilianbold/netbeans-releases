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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.mapper.cast.AbstractTypeCast;
import org.netbeans.modules.bpel.mapper.model.FromProcessor.FromForm;
import org.netbeans.modules.bpel.mapper.predicates.editor.PathConverter;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.DateValueTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.ForEachConditionsTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
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
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;

/**
 * Looks on the current state of the BPEL Mapper and modifies 
 * the BPEL model correspondingly.  
 * 
 * @author nk160297
 */
public class BpelModelUpdater extends AbstractBpelModelUpdater {
        
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
            updateTimeEventHolder(treePath, (TimeEventHolder)bpelEntity);
        } else if (elementType == If.class || 
                elementType == ElseIf.class || 
                elementType == While.class || 
                elementType == RepeatUntil.class) {
            // If, ElseIf, While, RepeatUntil
            updateConditionHolder(treePath, (ConditionHolder)bpelEntity);
        } else if (elementType == ForEach.class) {
            updateForEach(treePath, (ForEach)bpelEntity);
        }
        //
        return null; // TODO: return some result flag
    }
    
    //==========================================================================

    protected void updateFrom(Graph graph, 
            Set<AbstractTypeCast> typeCastCollector, 
            Set<XPathPseudoComp> pseudoCollector, 
            From from) throws Exception 
    {
        assert from != null;
        FromForm oldFromForm = FromProcessor.calculateFromForm(from);
        //
        GraphInfoCollector graphInfo = new GraphInfoCollector(graph);
        if (graphInfo.onlyOneTransitLink()) {
            // Only one link from the left to the right tree
            // 
            // Take the first link, which is the only.
            Link link = graphInfo.getTransitLinks().get(0); 
            TreeSourcePin sourcePin = (TreeSourcePin)link.getSource();
            TreePath sourceTreePath = sourcePin.getTreePath();
            TreePathInfo tpInfo = collectTreeInfo(
                    sourceTreePath, typeCastCollector, pseudoCollector);
            //
            XPathModel xPathModel = BpelXPathModelFactory.create(from);
            populateFrom(from, xPathModel, tpInfo, 
                    typeCastCollector, pseudoCollector);
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
                            Object dataObj = rootVertex.getDataObject();
                            if (dataObj == XPathStringLiteral.class) {
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
                populateContentHolder(from, graphInfo, 
                        typeCastCollector, pseudoCollector);
                //
                // Remove other attributes because they are not necessary now.
                from.removeEndpointReference();
                from.removeFromChild();
                from.removePart();
                from.removePartnerLink();
                from.removeProperty();
                from.removeVariable();
            }
        }
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
        Set<AbstractTypeCast> typeCastCollector = new HashSet<AbstractTypeCast>();
        Set<XPathPseudoComp> pseudoCollector = new HashSet<XPathPseudoComp>();
        //
        //
        //=====================================================================
        // Populate FROM
        //
        From from = copy.getFrom();
        if (from == null) {
            BpelModel bpelModel = copy.getBpelModel();
            from = bpelModel.getBuilder().createFrom();
            copy.setFrom(from);
        }
        //
        updateFrom(graph, typeCastCollector, pseudoCollector, from);
        //
        registerTypeCasts(copy, typeCastCollector, true);
        registerPseudoComps(copy, pseudoCollector, true);
        //
        //=====================================================================
        // Populate TO
        //
        To to = copy.getTo();
        if (to == null) {
            BpelModel bpelModel = copy.getBpelModel();
            to = bpelModel.getBuilder().createTo();
            copy.setTo(to);
        }
        //
        typeCastCollector = new HashSet<AbstractTypeCast>();
        pseudoCollector = new HashSet<XPathPseudoComp>();
        TreePathInfo tpInfo = collectTreeInfo(
                rightTreePath, typeCastCollector, pseudoCollector);
        //
        XPathModel xPathModel = BpelXPathModelFactory.create(to);
        populateTo(to, xPathModel, tpInfo, typeCastCollector, pseudoCollector);
        registerTypeCasts(copy, typeCastCollector, false);
        registerPseudoComps(copy, pseudoCollector, false);
    }
    
    private void updateAssign(TreePath rightTreePath, Assign assign) throws Exception {
        Graph graph = getMapperModel().graphRequired(rightTreePath);
        //
        Object dataObject = graph.getDataObject();
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
    }
    
    /**
     * 
     * @param rightTreePath
     * @param condHolder It is implied that the parameter is a BpelEntity.
     */
    private void updateConditionHolder(TreePath rightTreePath, 
            ConditionHolder condHolder) {
        //
        // Do common preparations
        //
        Graph graph = getMapperModel().graphRequired(rightTreePath);
        //
        GraphInfoCollector graphInfo = new GraphInfoCollector(graph);
        //
        //=====================================================================
        // Create Condition
        //
        BooleanExpr condition = condHolder.getCondition();
        if (condition == null) {
            BpelModel bpelModel = ((BpelEntity)condHolder).getBpelModel();
            condition = bpelModel.getBuilder().createCondition();
            condHolder.setCondition(condition);
            condition = condHolder.getCondition();
        }
        // 
        Set<AbstractTypeCast> typeCastCollector = new HashSet<AbstractTypeCast>();
        Set<XPathPseudoComp> pseudoCollector = new HashSet<XPathPseudoComp>();
        //
        populateContentHolder(condition, graphInfo, 
                typeCastCollector, pseudoCollector);
        ExtensibleElements destination = (ExtensibleElements)condHolder;
        registerTypeCasts(destination, typeCastCollector, true);
        registerPseudoComps(destination, pseudoCollector, true);
    }
    
    private void updateTimeEventHolder(TreePath rightTreePath, 
            TimeEventHolder timeEventHolder) {
        //
        // Do common preparations
        //
        Graph graph = getMapperModel().graphRequired(rightTreePath);
        //
        GraphInfoCollector graphInfo = new GraphInfoCollector(graph);
        //
        //=====================================================================
        // Populate Condition
        //
        TimeEvent timeEvent = timeEventHolder.getTimeEvent();
        if (timeEvent == null) {
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
        }
        // 
        Set<AbstractTypeCast> typeCastCollector = new HashSet<AbstractTypeCast>();
        Set<XPathPseudoComp> pseudoCollector = new HashSet<XPathPseudoComp>();
        //
        populateContentHolder((ContentElement)timeEvent, graphInfo, 
                typeCastCollector, pseudoCollector);
        registerTypeCasts((ExtensibleElements)timeEventHolder, typeCastCollector, true);
        registerPseudoComps((ExtensibleElements)timeEventHolder, pseudoCollector, true);
    }
    
    private void updateForEach(TreePath rightTreePath, ForEach forEach) {
        //
        // Do common preparations
        //
        Graph graph = getMapperModel().graphRequired(rightTreePath);
        //
        GraphInfoCollector graphInfo = new GraphInfoCollector(graph);
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
            if (startValue == null) {
                startValue = bpelModel.getBuilder().createStartCounterValue();
                forEach.setStartCounterValue(startValue);
                startValue = forEach.getStartCounterValue();
            }
            bpelExpr = startValue;
        } else if (ForEachConditionsTreeModel.FINAL_VALUE.equals(rightTreeDO)) {
            FinalCounterValue finalValue = forEach.getFinalCounterValue();
            if (finalValue == null) {
                finalValue = bpelModel.getBuilder().createFinalCounterValue();
                forEach.setFinalCounterValue(finalValue);
                finalValue = forEach.getFinalCounterValue();
            }
            bpelExpr = finalValue;
        } else if (ForEachConditionsTreeModel.COMPLETION_CONDITION.equals(rightTreeDO)) {
            CompletionCondition completionCond = forEach.getCompletionCondition();
            if (completionCond == null) {
                completionCond = bpelModel.getBuilder().createCompletionCondition();
                forEach.setCompletionCondition(completionCond);
                completionCond = forEach.getCompletionCondition();
            }
            //
            Branches branches = completionCond.getBranches();
            if (branches == null) {
                branches = bpelModel.getBuilder().createBranches();
                completionCond.setBranches(branches);
                branches = completionCond.getBranches();
            }
            bpelExpr = branches;
        } else {
            return;
        }
        //
        // Populate 
        Set<AbstractTypeCast> typeCastCollector = new HashSet<AbstractTypeCast>();
        Set<XPathPseudoComp> pseudoCollector = new HashSet<XPathPseudoComp>();
        //
        populateContentHolder(bpelExpr, graphInfo, typeCastCollector, pseudoCollector);
        registerTypeCasts(forEach, typeCastCollector, true);
        registerPseudoComps(forEach, pseudoCollector, true);
    }
    
    //==========================================================================

    private From populateFrom(From from, XPathModel xPathModel, 
            TreePathInfo tpInfo, Set<AbstractTypeCast> typeCastCollector, 
            Set<XPathPseudoComp> pseudoCollector) {
        FromProcessor.FromForm fromForm = calculateCopyFromForm(tpInfo);
        //
        switch(fromForm) {
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
            populateFromQuery(from, xPathModel, tpInfo, 
                    typeCastCollector, pseudoCollector, sms);
            //
            // from.removeVariable();
            from.removePart();
            // from.removeFromChild();
            from.removePartnerLink();
            from.removeEndpointReference();
            from.removeProperty();
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
            populateFromQuery(from, xPathModel, tpInfo, 
                    typeCastCollector, pseudoCollector, sms);
            //
            // from.removeVariable();
            // from.removePart();
            // from.removeFromChild();
            from.removePartnerLink();
            from.removeEndpointReference();
            from.removeProperty();
            try {
                from.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
            }
            break;
        }
        case PARTNER_LINK: {
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
            try {
                from.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
            }
            break;
        }
        case EXPRESSION: {
            XPathExpression xPathExpr = createVariableXPath(
                    xPathModel, tpInfo, typeCastCollector, pseudoCollector);
            if (xPathExpr != null) {
                try {
                    from.setContent(xPathExpr.getExpressionString());
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
            // from.setContent(null);
            break;
        }
        }
        //
        return from;
    }

    private void populateFromQuery(From from, XPathModel xPathModel, 
            TreePathInfo tpInfo, Set<AbstractTypeCast> typeCastCollector, 
            Set<XPathPseudoComp> pseudoCollector, SchemaModelsStack sms) {
        List<LocationStep> stepList = PathConverter.constructLSteps(xPathModel, 
                tpInfo.schemaCompList, typeCastCollector, pseudoCollector, sms);
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
    
    private To populateTo(To to, XPathModel xPathModel, TreePathInfo tpInfo, 
            Set<AbstractTypeCast> typeCastCollector, 
            Set<XPathPseudoComp> pseudoCollector) {
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
            try {
                to.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
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
            populateToQuery(to, xPathModel, tpInfo, 
                    typeCastCollector, pseudoCollector, sms);
            //
            // to.removeVariable();
            to.removePart();
            // to.removeQuery();
            to.removePartnerLink();
            to.removeProperty();
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
            populateToQuery(to, xPathModel, tpInfo, 
                    typeCastCollector, pseudoCollector, sms);
            //
            // to.removeVariable();
            // to.removePart();
            // to.removeQuery();
            // to.removeFromChild();
            to.removePartnerLink();
            to.removeProperty();
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
            try {
                to.setContent(null);
            } catch (VetoException ex) {
                // Do nothing
            }
            break;
        }
        case EXPRESSION: {
            XPathExpression xPathExpr = createVariableXPath(
                    xPathModel, tpInfo, typeCastCollector, pseudoCollector);
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
            // to.setContent(null);
            break;
        }
        }
        //
        return to;
    }

    private void populateToQuery(To to, XPathModel xPathModel, 
            TreePathInfo tpInfo, Set<AbstractTypeCast> typeCastCollector, 
            Set<XPathPseudoComp> pseudoCollector, SchemaModelsStack sms) {
        List<LocationStep> stepList = PathConverter.constructLSteps(xPathModel, 
                tpInfo.schemaCompList, typeCastCollector, pseudoCollector, sms);
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
            if (tpInfo.part == null && tpInfo.schemaCompList.isEmpty()) {
                return FromProcessor.FromForm.VAR;
            } else if (tpInfo.part != null && tpInfo.schemaCompList.isEmpty()) {
                return FromProcessor.FromForm.VAR_PART;
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
            if (tpInfo.part == null && tpInfo.schemaCompList.isEmpty()) {
                return CopyToProcessor.CopyToForm.VAR;
            } else if (tpInfo.part != null && tpInfo.schemaCompList.isEmpty()) {
                return CopyToProcessor.CopyToForm.VAR_PART;
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
    
    //==========================================================================

}
    