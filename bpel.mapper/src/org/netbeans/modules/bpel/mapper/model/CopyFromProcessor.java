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
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.search.EndpointRefFinder;
import org.netbeans.modules.bpel.mapper.tree.search.FinderListBuilder;
import org.netbeans.modules.bpel.mapper.tree.search.PartFinder;
import org.netbeans.modules.bpel.mapper.tree.search.PartnerLinkFinder;
import org.netbeans.modules.bpel.mapper.tree.search.VariableFinder;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemFinder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.FromChild;
import org.netbeans.modules.bpel.model.api.Literal;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.Roles;
import org.netbeans.modules.soa.mappercore.icons.StringIcon2D;
import org.netbeans.modules.soa.mappercore.model.Constant;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.xml.wsdl.model.Part;
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
 */
public class CopyFromProcessor {
    
    public static enum CopyFromForm {
        UNKNOWN, 
        VAR, 
        VAR_PART, 
        VAR_PART_QUERY, 
        VAR_QUERY, 
        PARTNER_LINK, 
        VAR_PROPERTY, 
        EXPRESSION, 
        LITERAL;
    }

    private BpelMapperModelFactory mFactory;
    private Copy mCopy;
    private CopyFromForm mCopyFromForm;
    
    public CopyFromProcessor(BpelMapperModelFactory factory, Copy copy) {
        assert factory != null && copy != null;
        mFactory = factory;
        mCopy = copy;
    }
    
    public synchronized CopyFromForm getCopyFromForm() {
        if (mCopyFromForm == null) {
            calculateCopyFromForm();
        }
        return mCopyFromForm;
    }
    
    public Graph populateGraph(Graph graph, MapperSwingTreeModel leftTreeModel) {
        assert mCopy.getFrom() != null;
        //
        switch (getCopyFromForm()) {
        case EXPRESSION:
            mFactory.populateGraph(graph, leftTreeModel, mCopy, mCopy.getFrom());
            break;
        case LITERAL:
            FromChild literal = mCopy.getFrom().getFromChild(); // literal
            if (literal != null && literal instanceof Literal) {
                String literalText = ((Literal)literal).getContent();
                Vertex newVertex = new Constant(new StringIcon2D("str"));
                VertexItem contentItem = new VertexItem(newVertex, literalText);
                newVertex.addItem(contentItem);
                //
                graph.addVertex(newVertex);
                Link newLink = new Link(newVertex, graph);
                graph.addLink(newLink);
            }
            break;
        case UNKNOWN:
            return null;
        default:
            // there is only one link
            ArrayList<TreeItemFinder> fromNodeFinderList = 
                    constructFindersList(mCopy);
            TreePath sourceTreePath = 
                    leftTreeModel.findFirstNode(fromNodeFinderList);
            if (sourceTreePath != null) {
                TreeSourcePin sourcePin = new TreeSourcePin(sourceTreePath);
                Link newLink = new Link(sourcePin, graph);
                graph.addLink(newLink);
            }
        }
        //
        return graph;
    }
    
    private void calculateCopyFromForm() {
        mCopyFromForm = CopyFromForm.UNKNOWN;
        //
        if (mCopy == null)  {
            return;
        }
        //
        From copyFrom = mCopy.getFrom();
        if (copyFrom == null) {
            return;
        }
        //
        BpelReference<VariableDeclaration> varRef = copyFrom.getVariable();
        if (varRef != null) {
            WSDLReference<Part> partRef = copyFrom.getPart();
            if (partRef != null) {
                FromChild query = copyFrom.getFromChild(); // query
                if (query != null && query instanceof Query) {
                    mCopyFromForm = CopyFromForm.VAR_PART_QUERY;
                    return;
                } else {
                    mCopyFromForm = CopyFromForm.VAR_PART;
                    return;
                }
            } else {
                FromChild query = copyFrom.getFromChild(); // query
                if (query != null && query instanceof Query) {
                    mCopyFromForm = CopyFromForm.VAR_QUERY;
                    return;
                } else {
                    mCopyFromForm = CopyFromForm.VAR;
                    return;
                }
            }
        } else {
            BpelReference<PartnerLink> plRef = copyFrom.getPartnerLink();
            if (plRef != null) {
                mCopyFromForm = CopyFromForm.PARTNER_LINK;
                return;
            }
            FromChild literal = copyFrom.getFromChild(); // literal
            if (literal != null && literal instanceof Literal) {
                mCopyFromForm = CopyFromForm.LITERAL;
                return;
            }
            String expression = copyFrom.getContent(); // Expression
            if (expression != null && expression.length() != 0) {
                mCopyFromForm = CopyFromForm.EXPRESSION;
                return;
            }
        }
        // WSDLReference<CorrelationProperty> cPropRef = copyTo.getProperty();
        //
    }
    
    public ArrayList<TreeItemFinder> constructFindersList(BpelEntity contextEntity) {
        From copyFrom = mCopy.getFrom();
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        //
        CopyFromForm form = getCopyFromForm();
        switch(form) {
        case VAR: {
            BpelReference<VariableDeclaration> varDeclRef = copyFrom.getVariable();
            if (varDeclRef != null) {
                VariableDeclaration varDecl = varDeclRef.get();
                if (varDecl != null) {
                    finderList.add(new VariableFinder(varDecl));
                }
            }
            break;
        }
        case VAR_PART: {
            BpelReference<VariableDeclaration> varDeclRef = copyFrom.getVariable();
            if (varDeclRef != null) {
                VariableDeclaration varDecl = varDeclRef.get();
                if (varDecl != null) {
                    finderList.add(new VariableFinder(varDecl));
                }
            }
            WSDLReference<Part> partRef = copyFrom.getPart();
            if (partRef != null) {
                Part part = partRef.get();
                if (part != null) {
                    finderList.add(new PartFinder(part));
                }
            }
            break;
        }
        case VAR_PART_QUERY: {
            BpelReference<VariableDeclaration> varDeclRef = copyFrom.getVariable();
            if (varDeclRef != null) {
                VariableDeclaration varDecl = varDeclRef.get();
                if (varDecl != null) {
                    finderList.add(new VariableFinder(varDecl));
                }
            }
            WSDLReference<Part> partRef = copyFrom.getPart();
            if (partRef != null) {
                Part part = partRef.get();
                if (part != null) {
                    finderList.add(new PartFinder(part));
                    //
                    FromChild query = copyFrom.getFromChild();
                    if (query != null && query instanceof Query) {
                        LocationPathBuilder builder = new LocationPathBuilder(
                                contextEntity, part, (Query)query);
                        XPathLocationPath lPath = builder.build();
                        if (lPath != null) {
                            finderList.addAll(FinderListBuilder.build(lPath));
                        }
                    }
                }
            }
            break;
        }
        case VAR_QUERY: {
            BpelReference<VariableDeclaration> varDeclRef = copyFrom.getVariable();
            if (varDeclRef != null) {
                VariableDeclaration varDecl = varDeclRef.get();
                if (varDecl != null) {
                    finderList.add(new VariableFinder(varDecl));
                    //
                    FromChild query = copyFrom.getFromChild();
                    if (query != null && query instanceof Query) {
                        LocationPathBuilder builder = new LocationPathBuilder(
                                contextEntity, varDecl, (Query)query);
                        XPathLocationPath lPath = builder.build();
                        if (lPath != null) {
                            finderList.addAll(FinderListBuilder.build(lPath));
                        }
                    }
                }
            }
            break;
        }
        case PARTNER_LINK: {
            BpelReference<PartnerLink> plRef = copyFrom.getPartnerLink();
            if (plRef != null) {
                PartnerLink pLink = plRef.get();
                if (pLink != null) {
                    finderList.add(new PartnerLinkFinder(pLink));
                    //
                    Roles endpointRef = copyFrom.getEndpointReference();
                    finderList.add(new EndpointRefFinder(endpointRef));
                }
            }
            break;
        }
        }
        //
        return finderList;
    }
    
}
