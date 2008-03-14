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

package org.netbeans.modules.bpel.mapper.predicates;



import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
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
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitorAdapter;

/**
 * The visitor is intended to look for predicate expressions 
 * inside of an XPath.
 *
 * @author nk160297
 */
public class PredicateFinderVisitor extends XPathVisitorAdapter {
    
    private PredicateManager mPredManager;
    private SpecialStepManager mSStepManager;
    private XPathBpelVariable mXPathVariable;

            
    public PredicateFinderVisitor(PredicateManager predManager, 
            SpecialStepManager sStepManager) {
        mPredManager = predManager;
        mSStepManager = sStepManager;
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
        XPathVariable xPathVar = vReference.getVariable();
        if (xPathVar != null && xPathVar instanceof XPathBpelVariable) {
            mXPathVariable = (XPathBpelVariable)xPathVar;
        }
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
    
    //---------------------------------------------------
    
    /**
     * See the description of the method "processLocationPath"
     */ 
    private class LocationPathProcessor {
        // The list can contain objects of either SchemaComponent
        // or PredicatedScehmaComp type.
        private transient LinkedList objLocationPath = new LinkedList();
        private boolean processingAborted = false;
        
        public List getObjLocationPath() {
            if (processingAborted) {
                return null;
            } else {
                return objLocationPath;
            }
        }
        
        /**
         * Resolves each LocationStep to object form and registers new 
         * predicates in the PredicateManager. 
         */ 
        public void processLocationPath(AbstractLocationPath path) {
            processingAborted = false;
            //
            // Put the variable or part to the locaton path first
            if (mXPathVariable != null) {
                VariableDeclaration varDecl = mXPathVariable.getVarDecl();
                objLocationPath.addFirst(varDecl);
                //
                Part part = mXPathVariable.getPart();
                if (part != null) {
                    objLocationPath.addFirst(part);
                }
            }
            //
            for (LocationStep step : path.getSteps()) {
                if (!processingAborted) {
                    processStep(step);
                }
            }
        }
        
        private void processStep(LocationStep step) {
            //
            // Registure special location step to the Special Step manager
            StepNodeTest stepNodeTest = step.getNodeTest();
            if (mSStepManager != null && stepNodeTest instanceof StepNodeTypeTest) {
                mSStepManager.addStep(objLocationPath, step);
            }
            //
            XPathSchemaContext xPathContext = step.getSchemaContext();
            if (xPathContext == null) {
                processingAborted = true;
                return;
            }
            //
            SchemaComponent sComp = 
                    XPathSchemaContext.Utilities.getSchemaComp(xPathContext);
            if (sComp == null) {
                // Error. The location path contains a step with unknown schema type
                // or multiple possible schema types. 
                // The predicate manager can't continue analysing the path!
                processingAborted = true;
                return;
            }
            //
            XPathPredicateExpression[] predArr = step.getPredicates();
            if (predArr == null || predArr.length == 0) {
                objLocationPath.addFirst(sComp);
            } else {
                AbstractPredicate newPredSComp = new XPathPredicate(step);
                //
                ArrayList currentPath = new ArrayList();
                currentPath.addAll(objLocationPath);
                //
                mPredManager.addPredicate(currentPath, newPredSComp);
                //
                objLocationPath.addFirst(newPredSComp);
            }
        }
        
    }
    
}
