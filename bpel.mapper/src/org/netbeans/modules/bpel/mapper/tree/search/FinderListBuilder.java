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

package org.netbeans.modules.bpel.mapper.tree.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.bpel.mapper.cast.TypeCast;
import org.netbeans.modules.bpel.mapper.predicates.AbstractPredicate;
import org.netbeans.modules.bpel.mapper.predicates.XPathPredicate;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xpath.ext.AbstractLocationPath;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeTest;
import org.netbeans.modules.xml.xpath.ext.StepNodeTypeTest;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.CastSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.VariableSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public class FinderListBuilder {

    public static void populateFinderList(ArrayList<TreeItemFinder> finderList, 
            VariableSchemaContext varContext) {
        //
        XPathVariable var = varContext.getVariable();
        assert var instanceof XPathBpelVariable;
        XPathBpelVariable bpelVar = (XPathBpelVariable)var;
        AbstractVariableDeclaration varDecl = bpelVar.getVarDecl();
        VariableFinder varFinder = new VariableFinder(varDecl);
        finderList.add(varFinder);
        //
        Part part = bpelVar.getPart();
        if (part != null) {
            PartFinder partFinder = new PartFinder(part);
            finderList.add(partFinder);
        }
    }
    
    public static List<TreeItemFinder> build(XPathSchemaContext schemaContext) {
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        //
        LinkedList<Object> result = new LinkedList<Object>();
        //
        XPathSchemaContext context = schemaContext;
        // 
        while (context != null) { 
            if (context instanceof VariableSchemaContext) {
                populateFinderList(finderList, (VariableSchemaContext)context);
            } else if (context instanceof CastSchemaContext) {
                CastSchemaContext castContext = (CastSchemaContext)context;
                //
                TypeCast typeCast = new TypeCast(castContext.getTypeCast());
                Object castedObj = typeCast.getCastedObject();
                if (castedObj instanceof SchemaComponent) {
                    result.addFirst(typeCast);
                } else if (castedObj instanceof AbstractVariableDeclaration) {
                    finderList.add(new CastedVariableFinder(typeCast));
                } else if (castedObj instanceof Part) {
                    finderList.add(new CastedPartFinder(typeCast));
                }
            } else {
                SchemaCompHolder sCompHolder = XPathSchemaContext.Utilities.
                        getSchemaCompHolder(context);
                if (sCompHolder == null) {
                    return null;
                }
                result.addFirst(sCompHolder.getHeldComponent());
            }
            //
            context = context.getParentContext();
        }
        //
        if (!result.isEmpty()) {
            PathFinder pathFinder = new PathFinder(result);
            finderList.add(pathFinder);
        }
        //
        return finderList;
    }
    
    /**
     * Builds a set of finders for looking a variable with or without a part. 
     * If a type cast parameter is specified then it is intended for looking 
     * the casted variable or casted part. 
     * @param varRef
     * @param typeCast
     * @return
     */
    public static List<TreeItemFinder> build(XPathVariableReference varRef, 
            TypeCast typeCast) {
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        //
        XPathVariable var = varRef.getVariable();
        // Variable could be deleted but the reference no.
        // issue 128684
        if (var == null) {
            Logger.getLogger(FinderListBuilder.class.getName()).log(Level.INFO, 
                    NbBundle.getMessage(FinderListBuilder.class, "LOG_MSG_VAR_NULL", varRef)); //NOI18N
            return finderList;
        }
        assert var instanceof XPathBpelVariable;
        XPathBpelVariable bpelVar = (XPathBpelVariable)var;
        AbstractVariableDeclaration varDecl = bpelVar.getVarDecl();
        Part part = bpelVar.getPart();
        //
        Object castedObj = null;
        if (typeCast != null) {
            castedObj = typeCast.getCastedObject();
        }
        //
        if (castedObj != null && castedObj == varDecl) {
            CastedVariableFinder varCastFinder = new CastedVariableFinder(typeCast);
            finderList.add(varCastFinder);
        } else {
            VariableFinder varFinder = new VariableFinder(varDecl);
            finderList.add(varFinder);
        }
        //
        if (part != null) {
            if (castedObj != null && castedObj == part) {
                CastedPartFinder partCastFinder = new CastedPartFinder(typeCast);
                finderList.add(partCastFinder);
            } else {
                PartFinder partFinder = new PartFinder(part);
                finderList.add(partFinder);
            }
        }
        //
        return finderList;
    }

    public static List<TreeItemFinder> build(AbstractLocationPath locationPath) {
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        //
        if (locationPath instanceof XPathExpressionPath) {
            XPathExpression rootExpr = 
                    ((XPathExpressionPath)locationPath).getRootExpression();
            if (rootExpr instanceof XPathVariableReference) {
                XPathVariableReference varRef = (XPathVariableReference)rootExpr;
                XPathSchemaContext sContext = varRef.getSchemaContext();
                if (sContext instanceof CastSchemaContext) {
                    CastSchemaContext castContext = (CastSchemaContext)sContext;
                    TypeCast typeCast = new TypeCast(castContext.getTypeCast());
                    finderList.addAll(build(varRef, typeCast));
                } else {
                    finderList.addAll(build(varRef, null));
                }
            }
        }
        //
        ArrayList<Object> result = new ArrayList<Object>();
        LocationStep[] stepArr = locationPath.getSteps();
        //
        for (LocationStep step : stepArr) {
            //
            StepNodeTest stepNodeTest = step.getNodeTest();
            if (stepNodeTest instanceof StepNodeTypeTest) {
                result.add(step);
                continue;
            }
            //
            XPathSchemaContext sContext = step.getSchemaContext();
            if (sContext == null) {
                // it didn't manage to resolve a schema context for 
                // the step
                return Collections.EMPTY_LIST;
            }
            //
            XPathPredicateExpression[] predArr = step.getPredicates();
            if (predArr == null || predArr.length == 0) {
                //
                if (sContext instanceof CastSchemaContext) {
                    CastSchemaContext castContext = (CastSchemaContext)sContext;
                    TypeCast typeCast = new TypeCast(castContext.getTypeCast());
                    result.add(typeCast);
                } else {
                    SchemaCompHolder stepSchemaCompHolder = 
                            XPathSchemaContext.Utilities.getSchemaCompHolder(sContext);
                    if (stepSchemaCompHolder == null) {
                        return Collections.EMPTY_LIST;
                    }
                    //
                    result.add(stepSchemaCompHolder.getHeldComponent());
                }
            } else {
                AbstractPredicate pred = new XPathPredicate(step);
                result.add(pred);
            }
        }
        //
        if (!result.isEmpty()) {
            PathFinder pathFinder = new PathFinder(result);
            finderList.add(pathFinder);
        }
        //
        return finderList;
    }
    
}
