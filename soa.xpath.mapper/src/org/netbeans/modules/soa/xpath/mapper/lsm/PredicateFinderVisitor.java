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
package org.netbeans.modules.soa.xpath.mapper.lsm;

import org.netbeans.modules.soa.xpath.mapper.specstep.SpecialStepManager;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.soa.xpath.mapper.tree.PathConverter;
import org.netbeans.modules.xml.xpath.ext.AbstractLocationPath;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeTest;
import org.netbeans.modules.xml.xpath.ext.StepNodeTypeTest;
import org.netbeans.modules.xml.xpath.ext.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.ext.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SpecialSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.WildcardSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.WrappingSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStep;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitorAdapter;

/**
 * The visitor is intended to look for predicate expressions 
 * inside of an XPath.
 *
 * @author nk160297
 */
public class PredicateFinderVisitor extends XPathVisitorAdapter {

    private MapperSwingTreeModel mMapperTreeModel;
    private PredicateManager mPredManager;
    private SpecialStepManager mSStepManager;
    private XPathVariableReference mXPathVarReference;
    private XPathVariable mXPathVariable;

    public PredicateFinderVisitor(MapperSwingTreeModel treeModel) {
        assert treeModel != null;
        mMapperTreeModel = treeModel;
        //
        ExtensionsManagerHolder emh = mMapperTreeModel.getExtManagerHolder();
        assert emh != null;
        //
        mPredManager = emh.getPredicateManager();
        assert mPredManager != null;
        //
        mSStepManager = emh.getSpecialStepManager();
        assert mSStepManager != null;
    }

    //---------------------------------------------------
    @Override
    public void visit(XPathLocationPath expr) {
        LocationPathProcessor converter = new LocationPathProcessor();
        converter.processLocationPath(expr);
    }

    @Override
    public void visit(XPathExpressionPath expressionPath) {
        XPathExpression rootExpr = expressionPath.getRootExpression();
        rootExpr.accept(this);
        //
        LocationPathProcessor processor = new LocationPathProcessor();
        processor.processLocationPath(expressionPath);
        //
        mXPathVariable = null; // discard the variable
    }

    @Override
    public void visit(XPathVariableReference vReference) {
        mXPathVarReference = vReference;
    }

    public XPathVariable getVariable () {
        if (mXPathVariable == null) {
            if (mXPathVarReference == null) {
                return null;
            }
            //
            mXPathVariable = mXPathVarReference.getVariable();
        }
        return mXPathVariable;
    }
    
    //---------------------------------------------------
    @Override
    public void visit(XPathCoreOperation expr) {
        visitChildren(expr);
    }

    @Override
    public void visit(XPathCoreFunction expr) {
        visitChildren(expr);
    }

    @Override
    public void visit(XPathExtensionFunction expr) {
        visitChildren(expr);
    }

    /**
     * See the description of the method "processLocationPath"
     */
    private class LocationPathProcessor {
        // The list can contain objects of either SchemaComponent
        // or PredicatedScehmaComp type.
        private boolean processingAborted = false;

        /**
         * Resolves each LocationStep to object form and registers new 
         * predicates in the PredicateManager. 
         */
        public void processLocationPath(AbstractLocationPath path) {
            processingAborted = false;

            //speed optimisation, see coments below
            // TODO: The path is a part of a big XPath expression or is
            // an atomic expression itself. The optimization tries evoiding
            // unnecessary resolving of the expression (calculation of XPathSchemaContex).
            // But actually the calculation is going to be done later anyway.
            // So the optimization is suspecious. It is necessary to convince
            // the resolving isn't done second time with the same expression. 
            if (!needProcessing(path))
                return;
            //
            LocationStep[] stepArr = path.getSteps();
            for (int stepIndex = 0; stepIndex < stepArr.length; stepIndex++) {
                if (processingAborted) {
                    break;
                }
                //
                LocationStep step = stepArr[stepIndex];
                XPathSchemaContext xPathContext = step.getSchemaContext();
                if (xPathContext != null) {
                    processStep(xPathContext);
                }
            }
        }
        
        /**
        * AlexeyY: 
        * speed optimisation: check if path contains predicates 
        * before processing this path
        **/
        private boolean needProcessing(AbstractLocationPath path){
            for (LocationStep step : path.getSteps()) {
                XPathPredicateExpression[] predicates = step.getPredicates();
                if (predicates != null && predicates.length > 0) {
                    return true;
                }
                StepNodeTest stepNodeTest = step.getNodeTest();
                if (stepNodeTest instanceof StepNodeTypeTest) {
                    return true;
                }
//                else if (stepNodeTest instanceof StepNodeNameTest) {
//                    // TODO: Process * and @* here
//                }
            }
            return false;
        }

        private void processStep(XPathSchemaContext stepContext) {
            PathConverter pathConverter = mMapperTreeModel.getPathConverter();
            if (stepContext instanceof PredicatedSchemaContext) {
                PredicatedSchemaContext predContext =
                        (PredicatedSchemaContext)stepContext;
                if (predContext.hasUnknownVariable()) {
                    processingAborted = true;
                    return;
                }
                DirectedList<Object> predicatePath = pathConverter.
                        constructObjectLocationList(stepContext, true, true);
                //
                MapperPredicate newPredSComp = 
                        mPredManager.createMapperPredicate(predContext);
                mPredManager.addPredicate(predicatePath, newPredSComp);
                //
                XPathSchemaContext baseContext = predContext.getBaseContext();
                if (baseContext != null) {
                    processStep(baseContext);
                }
            } else if (stepContext instanceof WildcardSchemaContext) {
                //
                // Registure special location step to the Special Step manager
                WildcardSchemaContext wContext = (WildcardSchemaContext)stepContext;
                XPathSpecialStep sStep = wContext.getSpecialStep();
                switch (sStep.getType()) {
                    // case ALL_ELEMENTS:
                    // case ALL_ATTRIBUTES:
                    case NODE:
                        DirectedList<Object> sStepPath = pathConverter.
                                constructObjectLocationList(stepContext, true, true);
                        //
                        mSStepManager.addStep(sStepPath, sStep);
                        break;
                }
            } else if (stepContext instanceof SpecialSchemaContext) {
                //
                // Registure special location step to the Special Step manager
                SpecialSchemaContext wContext = (SpecialSchemaContext)stepContext;
                XPathSpecialStep sStep = wContext.getSpecialStep();
                switch (sStep.getType()) {
                    // case ALL_ELEMENTS:
                    // case ALL_ATTRIBUTES:
                    case COMMENT:
                    case TEXT:
                    case PROCESSING_INSTR:
                        DirectedList<Object> sStepPath = pathConverter.
                                constructObjectLocationList(stepContext, true, true);
                        //
                        mSStepManager.addStep(sStepPath, sStep);
                        break;
                }
            } else if (stepContext instanceof WrappingSchemaContext) {
                processStep(WrappingSchemaContext.class.
                        cast(stepContext).getBaseContext());
            }
        }

    }
}
