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
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.model.BpelMapperLsmProcessor.MapperLsmContainer;
import org.netbeans.modules.bpel.mapper.model.customitems.XmlLiteralDataObject;
import org.netbeans.modules.bpel.mapper.model.customitems.XmlLiteralUtils;
import org.netbeans.modules.bpel.mapper.tree.search.CorrelationPropertyFinder;
import org.netbeans.modules.bpel.mapper.tree.search.EndpointRefFinder;
import org.netbeans.modules.bpel.mapper.tree.search.BpelFinderListBuilder;
import org.netbeans.modules.bpel.mapper.tree.search.NMPropertyFinder;
import org.netbeans.modules.bpel.mapper.tree.search.PartFinder;
import org.netbeans.modules.bpel.mapper.tree.search.PartnerLinkFinder;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.bpel.mapper.tree.search.VariableFinder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.FromChild;
import org.netbeans.modules.bpel.model.api.FromHolder;
import org.netbeans.modules.bpel.model.api.Literal;
import org.netbeans.modules.bpel.model.api.Literal.LiteralForm;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.Roles;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.utils.GraphLayout;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;

/**
 * Takes data from Copy-->From and populates the Graph object.
 * The Graph can has simple or complex content. 
 * In simple case the Graph has only one link from source ot target tree. 
 * In complex case the Grahp has several vertices connected with links.
 * The From BPEL entity can have different forms. 
 * The specific processing is required for different forms.
 * 
 * @author nk160297
 * @author Vitaly Bychkov
 */
public class FromProcessor {
    
    public static enum FromForm {
        UNKNOWN, 
        VAR, 
        VAR_PART, 
        VAR_PART_QUERY, 
        VAR_QUERY, 
        PARTNER_LINK, 
        VAR_PROPERTY, 
        VAR_NM_PROPERTY, 
        EXPRESSION, 
        LITERAL;
    }

    private BpelMapperModelFactory mFactory;
    private FromHolder mContextEntity;
    private FromForm mCopyFromForm;
    
    public FromProcessor(BpelMapperModelFactory factory, FromHolder fromHolder) {
        assert factory != null && fromHolder != null;
        mFactory = factory;
        mContextEntity = fromHolder;
    }
    
    public From getFrom() {
        assert mContextEntity != null;
        return mContextEntity.getFrom();
    }
    
    public synchronized FromForm getFromForm() {
        if (mCopyFromForm == null) {
            calculateFromForm();
        }
        return mCopyFromForm;
    }
    
    public Graph populateGraph(Graph graph, BpelMapperSwingTreeModel leftTreeModel,
            MapperLsmContainer lsmCont) {
        //
        assert getFrom() != null;
        //
        switch (getFromForm()) {
        case EXPRESSION:
            mFactory.populateGraph(graph, leftTreeModel, mContextEntity, 
                    getFrom(), lsmCont);
            break;
        case LITERAL:
            FromChild literal = getFrom().getFromChild(); // literal
            
            if (literal != null && literal instanceof Literal) {
                XmlLiteralUtils.XmlLiteralInfo info = XmlLiteralUtils.calculateLiteralInfo((Literal)literal);
                
                String literalText = info.getTextValue();
                LiteralForm literalForm = info.getLiteralForm();
                XmlLiteralDataObject xmlDataObject = new XmlLiteralDataObject(literal.getNamespaceContext(),
                         info.getTextValue(), literalForm);
                
                Vertex newVertex = BpelVertexFactory.getInstance().createXmlLiteral(xmlDataObject);
                newVertex.getItem(0).setText(literalText);
                //
                graph.addVertex(newVertex);
                Link newLink = new Link(newVertex, graph);
                graph.addLink(newLink);
                //
                GraphLayout.layout(graph);
            }
            break;
        case UNKNOWN:
            return null;
        default:
            // there is only one link
            ArrayList<TreeItemFinder> fromNodeFinderList = 
                    constructFindersList(mContextEntity);
            TreeFinderProcessor fProcessor = new TreeFinderProcessor(leftTreeModel);
            TreePath sourceTreePath = fProcessor.findFirstNode(fromNodeFinderList);
//            TreePath sourceTreePath = 
//                    leftTreeModel.findFirstNode(fromNodeFinderList);
            if (sourceTreePath != null) {
                TreeSourcePin sourcePin = new TreeSourcePin(sourceTreePath);
                Link newLink = new Link(sourcePin, graph);
                graph.addLink(newLink);
            }
        }
        //
        return graph;
    }
    
    private void calculateFromForm() {
        mCopyFromForm = FromForm.UNKNOWN;
        //
        if (mContextEntity == null)  {
            return;
        }
        //
        From from = getFrom();
        if (from == null) {
            return;
        }
        //
        mCopyFromForm = calculateFromForm(from);
    }
    
    public static FromForm calculateFromForm(From copyFrom) {
        //
        if (copyFrom == null) {
            return FromForm.UNKNOWN;
        }
        //
        BpelReference<VariableDeclaration> varRef = copyFrom.getVariable();
        if (varRef != null) {
            WSDLReference<Part> partRef = copyFrom.getPart();
            WSDLReference<CorrelationProperty> property = copyFrom
                    .getProperty();
            String nmProperty = copyFrom.getNMProperty();
            if (partRef != null) {
                FromChild query = copyFrom.getFromChild(); // query
                if (query != null && query instanceof Query) {
                    return FromForm.VAR_PART_QUERY;
                } else {
                    return FromForm.VAR_PART;
                }
            } else if (property != null && property.get() != null) {
                return FromForm.VAR_PROPERTY;
            } else if (nmProperty != null) {
                return FromForm.VAR_NM_PROPERTY;
            } else {
                FromChild query = copyFrom.getFromChild(); // query
                if (query != null && query instanceof Query) {
                    return FromForm.VAR_QUERY;
                } else {
                    return FromForm.VAR;
                }
            }
        } else {
            BpelReference<PartnerLink> plRef = copyFrom.getPartnerLink();
            if (plRef != null) {
                return FromForm.PARTNER_LINK;
            }
            FromChild literal = copyFrom.getFromChild(); // literal
            if (literal != null && literal instanceof Literal) {
                return FromForm.LITERAL;
            }
            String expression = copyFrom.getContent(); // Expression
            if (expression != null && expression.length() != 0) {
                return FromForm.EXPRESSION;
            }
        }
        // WSDLReference<CorrelationProperty> cPropRef = copyTo.getProperty();
        //
        return FromForm.UNKNOWN;
    }

    public static ArrayList<TreeItemFinder> constructFindersList(From from, 
            FromForm form, BpelEntity contextEntity) 
    {
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        if (from == null || form == null) {
            return finderList;
        }
        
        switch(form) {
        case VAR: {
            BpelReference<VariableDeclaration> varDeclRef = from.getVariable();
            if (varDeclRef != null) {
                VariableDeclaration varDecl = varDeclRef.get();
                if (varDecl != null) {
                    finderList.add(new VariableFinder(varDecl));
                }
            }
            break;
        }
        case VAR_PART: {
            BpelReference<VariableDeclaration> varDeclRef = from.getVariable();
            if (varDeclRef != null) {
                VariableDeclaration varDecl = varDeclRef.get();
                if (varDecl != null) {
                    finderList.add(new VariableFinder(varDecl));
                }
            }
            WSDLReference<Part> partRef = from.getPart();
            if (partRef != null) {
                Part part = partRef.get();
                if (part != null) {
                    finderList.add(new PartFinder(part));
                }
            }
            break;
        }
        case VAR_PROPERTY: {
                BpelReference<VariableDeclaration> varDeclRef = from
                        .getVariable();
                WSDLReference<CorrelationProperty> propertyRef = from
                        .getProperty();
                
                if (varDeclRef != null && propertyRef != null) {
                    VariableDeclaration variableDeclaration = varDeclRef.get();
                    if (variableDeclaration != null) {
                        CorrelationProperty property = propertyRef.get(); 
                        if (property != null) {
                            finderList.add(new CorrelationPropertyFinder(
                                    variableDeclaration, property));
                        } 
                    }
                }
            }
            break;
        case VAR_NM_PROPERTY: {
                BpelReference<VariableDeclaration> varDeclRef = from
                        .getVariable();
                String nmProperty = from.getNMProperty();
                
                if (varDeclRef != null && nmProperty != null) {
                    VariableDeclaration variableDeclaration = varDeclRef.get();
                    if (variableDeclaration != null) {
                        finderList.add(new NMPropertyFinder(
                                variableDeclaration, nmProperty));
                    }
                }
            }
            break;
        case VAR_PART_QUERY: {
            BpelReference<VariableDeclaration> varDeclRef = from.getVariable();
            if (varDeclRef != null) {
                VariableDeclaration varDecl = varDeclRef.get();
                if (varDecl != null) {
                    finderList.add(new VariableFinder(varDecl));
                }
            }
            WSDLReference<Part> partRef = from.getPart();
            if (partRef != null) {
                Part part = partRef.get();
                if (part != null) {
                    finderList.add(new PartFinder(part));
                    //
                    FromChild query = from.getFromChild();
                    if (query != null && query instanceof Query) {
                        LocationPathBuilder builder = new LocationPathBuilder(
                                contextEntity, part, (Query)query);
                        XPathLocationPath lPath = builder.build();
                        if (lPath != null) {
                            finderList.addAll(BpelFinderListBuilder.singl().build(lPath));
                        }
                    }
                }
            }
            break;
        }
        case VAR_QUERY: {
            BpelReference<VariableDeclaration> varDeclRef = from.getVariable();
            if (varDeclRef != null) {
                VariableDeclaration varDecl = varDeclRef.get();
                if (varDecl != null) {
                    finderList.add(new VariableFinder(varDecl));
                    //
                    FromChild query = from.getFromChild();
                    if (query != null && query instanceof Query) {
                        LocationPathBuilder builder = new LocationPathBuilder(
                                contextEntity, varDecl, (Query)query);
                        XPathLocationPath lPath = builder.build();
                        if (lPath != null) {
                            finderList.addAll(BpelFinderListBuilder.singl().build(lPath));
                        }
                    }
                }
            }
            break;
        }
        case PARTNER_LINK: {
            BpelReference<PartnerLink> plRef = from.getPartnerLink();
            if (plRef != null) {
                PartnerLink pLink = plRef.get();
                if (pLink != null) {
                    finderList.add(new PartnerLinkFinder(pLink));
                    //
                    Roles endpointRef = from.getEndpointReference();
                    finderList.add(new EndpointRefFinder(endpointRef));
                }
            }
            break;
        }
        }
        //
        return finderList;        
    }
    
    public ArrayList<TreeItemFinder> constructFindersList(BpelEntity contextEntity) {
        From copyFrom = getFrom();
        //
        FromForm form = getFromForm();
        return constructFindersList(copyFrom, form, contextEntity);
    }
    
}
