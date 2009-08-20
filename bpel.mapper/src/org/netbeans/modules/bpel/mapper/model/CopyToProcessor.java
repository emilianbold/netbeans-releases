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
import java.util.List;
import org.netbeans.modules.bpel.mapper.tree.search.FinderListBuilder;
import org.netbeans.modules.bpel.mapper.tree.search.PartFinder;
import org.netbeans.modules.bpel.mapper.tree.search.PartnerLinkFinder;
import org.netbeans.modules.bpel.mapper.tree.search.VariableFinder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.xml.xpath.ext.AbstractLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitorAdapter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;

/**
 * Processes the Copy-->To object to find a node in the target tree to 
 * which a link has to be connected.
 * The To BPEL entity can have different forms. 
 * The specific processing is required for different forms.
 * 
 * @author nk160297
 */
public class CopyToProcessor {
    
    public static enum CopyToForm {
        UNKNOWN, 
        VAR, 
        VAR_PART, 
        VAR_PART_QUERY, 
        VAR_QUERY, 
        PARTNER_LINK, 
        VAR_PROPERTY, 
        EXPRESSION;
    }
    
    public static CopyToForm getCopyToForm(To copyTo) {
        if (copyTo != null)  {
            BpelReference<VariableDeclaration> varRef = copyTo.getVariable();
            if (varRef != null) {
                WSDLReference<Part> partRef = copyTo.getPart();
                if (partRef != null) {
                    Query query = copyTo.getQuery(); // query
                    if (query != null) {
                        return CopyToForm.VAR_PART_QUERY;
                    } else {
                        return CopyToForm.VAR_PART;
                    }
                } else {
                    Query query = copyTo.getQuery(); // query
                    if (query != null) {
                        return CopyToForm.VAR_QUERY;
                    } else {
                        return CopyToForm.VAR;
                    }
                }
            } else {
                BpelReference<PartnerLink> plRef = copyTo.getPartnerLink();
                if (plRef != null) {
                    return CopyToForm.PARTNER_LINK;
                }
                String expression = copyTo.getContent(); // Expression
                if (expression != null && expression.length() != 0) {
                    return CopyToForm.EXPRESSION;
                }
            }
            // WSDLReference<CorrelationProperty> cPropRef = copyTo.getProperty();
        }
        //
        return CopyToForm.UNKNOWN;
    }

    public static ArrayList<TreeItemFinder> constructFindersList(
            CopyToForm form, 
            BpelEntity contextEntity, To copyTo, 
            XPathExpression toExpr, 
            List<Cast> castList, 
            List<PseudoComp> pseudoComps, 
            MapperModelFactory modelFactory) {
        //
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        //
        switch(form) {
        case VAR: {
            BpelReference<VariableDeclaration> varDeclRef = copyTo.getVariable();
            if (varDeclRef != null) {
                VariableDeclaration varDecl = varDeclRef.get();
                if (varDecl != null) {
                    finderList.add(new VariableFinder(varDecl));
                }
            }
            break;
        }
        case VAR_PART: {
            BpelReference<VariableDeclaration> varDeclRef = copyTo.getVariable();
            if (varDeclRef != null) {
                VariableDeclaration varDecl = varDeclRef.get();
                if (varDecl != null) {
                    finderList.add(new VariableFinder(varDecl));
                }
            }
            WSDLReference<Part> partRef = copyTo.getPart();
            if (partRef != null) {
                Part part = partRef.get();
                if (part != null) {
                    finderList.add(new PartFinder(part));
                }
            }
            break;
        }
        case VAR_PART_QUERY: {
            BpelReference<VariableDeclaration> varDeclRef = copyTo.getVariable();
            if (varDeclRef != null) {
                VariableDeclaration varDecl = varDeclRef.get();
                if (varDecl != null) {
                    finderList.add(new VariableFinder(varDecl));
                }
            }
            WSDLReference<Part> partRef = copyTo.getPart();
            if (partRef != null) {
                Part part = partRef.get();
                if (part != null) {
                    finderList.add(new PartFinder(part));
                    //
                    Query query = copyTo.getQuery();
                    if (query != null) {
                        LocationPathBuilder builder = new LocationPathBuilder(
                                contextEntity, part, query);
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
            BpelReference<VariableDeclaration> varDeclRef = copyTo.getVariable();
            if (varDeclRef != null) {
                VariableDeclaration varDecl = varDeclRef.get();
                if (varDecl != null) {
                    finderList.add(new VariableFinder(varDecl));
                    //
                    Query query = copyTo.getQuery();
                    if (query != null) {
                        LocationPathBuilder builder = new LocationPathBuilder(
                                contextEntity, varDecl, query);
                        XPathLocationPath lPath = builder.build();
                        if (lPath != null) {
                            finderList.addAll(FinderListBuilder.build(lPath));
                        }
                    }
                }
            }
            break;
        }
        case EXPRESSION: {
            if (toExpr == null) {
                toExpr = constructExpression(contextEntity, copyTo, 
                        castList, pseudoComps, modelFactory);
            }
            //
            if (toExpr != null) {
                if (toExpr instanceof AbstractLocationPath) {
                    finderList.addAll(FinderListBuilder.build(
                            (AbstractLocationPath)toExpr));
                } else if (toExpr instanceof XPathVariableReference) {
                    finderList.addAll(FinderListBuilder.build(
                            (XPathVariableReference)toExpr, null));
                }
            }
            //
            break;
        }
        case PARTNER_LINK: {
            BpelReference<PartnerLink> plRef = copyTo.getPartnerLink();
            if (plRef != null) {
                PartnerLink pLink = plRef.get();
                if (pLink != null) {
                    finderList.add(new PartnerLinkFinder(pLink));
                }
            }
            break;
        }
        case UNKNOWN: 
            return null;
        }
        //
        return finderList;
    }
    
    public static XPathExpression constructExpression(
            BpelEntity contextEntity, To copyTo, 
            List<Cast> castList, List<PseudoComp> pseudoComps, 
            MapperModelFactory modelFactory) {
        //
        String exprLang = copyTo.getExpressionLanguage();
        String exprText = copyTo.getContent();
        boolean isXPathExpr = (exprLang == null || exprLang.length() == 0 ||
                BpelXPathModelFactory.DEFAULT_EXPR_LANGUAGE.equals(exprLang));
        //
        // we can handle only xpath expressions.
        if (isXPathExpr && exprText != null && exprText.length() != 0) {
            try {
                XPathModel newXPathModel = BpelXPathModelFactory.create(
                        contextEntity, castList, pseudoComps);
                //
                // NOT NEED to specify schema context because of an 
                // expression with variable is implied here. 
                //
                XPathExpression expr = newXPathModel.parseExpression(exprText);
                return expr;
            } catch (XPathException ex) {
                // Do nothing
            }
        }
        //
        return null;
    }
    
    private static class ExpressionFinderVisitor extends XPathVisitorAdapter {
        
        private XPathBpelVariable mVar;
        private AbstractLocationPath mLocationPath;
        
        public XPathBpelVariable getVariable() {
            return mVar;
        }
        
        public AbstractLocationPath getPath() { 
            return mLocationPath;
        }
        
        // Only expression pathes can be processed now.
        // TODO: process any kind of expressions
        @Override
        public void visit(XPathExpressionPath expressionPath) {
            mLocationPath = expressionPath;
            //
            XPathExpression rootExpr = expressionPath.getRootExpression();
            rootExpr.accept(this);
        }

        // Root expression can be a variable reference only
        @Override
        public void visit(XPathVariableReference vReference) {
            XPathVariable xPathVar = vReference.getVariable();
            if (xPathVar != null && xPathVar instanceof XPathBpelVariable) {
                mVar = (XPathBpelVariable)xPathVar;
            }
        }
        
    }
    
}
