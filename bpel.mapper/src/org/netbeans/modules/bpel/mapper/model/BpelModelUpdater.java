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

import java.util.List;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.CopyFromProcessor.CopyFromForm;
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
import org.netbeans.modules.bpel.model.api.support.XPathModelFactory;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;

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
        //=====================================================================
        // Populate FROM
        //
        From from = copy.getFrom();
        CopyFromForm oldFromForm = null;
        if (from == null) {
            BpelModel bpelModel = copy.getBpelModel();
            from = bpelModel.getBuilder().createFrom();
            copy.setFrom(from);
        } else {
            oldFromForm = CopyFromProcessor.calculateCopyFromForm(from);
        }
        //
        GraphInfoCollector graphInfo = new GraphInfoCollector(graph);
        if (graphInfo.onlyOneTransitLink()) {
            // Only one link from the left to the right tree
            // 
            // Take the first link, which is the only.
            Link link = graphInfo.getTransitLinks().get(0); 
            TreeSourcePin sourcePin = (TreeSourcePin)link.getSource();
            TreePath sourceTreePath = sourcePin.getTreePath();
            TreePathInfo tpInfo = collectTreeInfo(sourceTreePath);
            //
            XPathModel xPathModel = XPathModelFactory.create(from);
            populateFrom(from, xPathModel, tpInfo);
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
                populateContentHolder(from, graphInfo);
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
        TreePathInfo tpInfo = collectTreeInfo(rightTreePath);
        //
        XPathModel xPathModel = XPathModelFactory.create(to);
        populateTo(to, xPathModel, tpInfo);
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
        populateContentHolder(condition, graphInfo);
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
        populateContentHolder((ContentElement)timeEvent, graphInfo);
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
        populateContentHolder(bpelExpr, graphInfo);
    }
    
    //==========================================================================

    private From populateFrom(From from, XPathModel xPathModel, TreePathInfo tpInfo) {
        CopyFromProcessor.CopyFromForm fromForm = calculateCopyFromForm(tpInfo);
        //
        switch(fromForm) {
        case VAR: {
            BpelReference<VariableDeclaration> varRef = from.createReference(tpInfo.varDecl, VariableDeclaration.class);
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
            populateFromQuery(from, xPathModel, tpInfo);
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
            populateFromQuery(from, xPathModel, tpInfo);
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
            XPathExpression xPathExpr = createVariableXPath(xPathModel, tpInfo);
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

    private void populateFromQuery(From from, XPathModel xPathModel, TreePathInfo tpInfo) {
        List<LocationStep> stepList = 
                constructLSteps(xPathModel, tpInfo.schemaCompList);
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
            populateToQuery(to, xPathModel, tpInfo);
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
            populateToQuery(to, xPathModel, tpInfo);
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
            // to.setContent(null);
            break;
        }
        }
        //
        return to;
    }

    private void populateToQuery(To to, XPathModel xPathModel, TreePathInfo tpInfo) {
        List<LocationStep> stepList = 
                constructLSteps(xPathModel, tpInfo.schemaCompList);
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

    private CopyFromProcessor.CopyFromForm calculateCopyFromForm(TreePathInfo tpInfo) {
        if (tpInfo.varDecl != null) {
            if (tpInfo.part == null && tpInfo.schemaCompList.isEmpty()) {
                return CopyFromProcessor.CopyFromForm.VAR;
            } else if (tpInfo.part != null && tpInfo.schemaCompList.isEmpty()) {
                return CopyFromProcessor.CopyFromForm.VAR_PART;
            } else if (tpInfo.part == null && !tpInfo.schemaCompList.isEmpty()) {
                // return CopyFromProcessor.CopyFromForm.VAR_QUERY;
                return CopyFromProcessor.CopyFromForm.EXPRESSION;
            } else if (tpInfo.part != null && !tpInfo.schemaCompList.isEmpty()) {
                // return CopyFromProcessor.CopyFromForm.VAR_PART_QUERY;
                return CopyFromProcessor.CopyFromForm.EXPRESSION;
            }
        } else if (tpInfo.pLink != null) {
            return CopyFromProcessor.CopyFromForm.PARTNER_LINK;
        }
        return CopyFromProcessor.CopyFromForm.UNKNOWN;
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
    