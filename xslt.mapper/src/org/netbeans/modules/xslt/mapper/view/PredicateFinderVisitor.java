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

package org.netbeans.modules.xslt.mapper.view;



import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xpath.LocationStep;
import org.netbeans.modules.xml.xpath.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.XPathLocationPath;
import org.netbeans.modules.xml.xpath.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.XPathVariableReference;
import org.netbeans.modules.xml.xpath.visitor.AbstractXPathVisitor;
import org.netbeans.modules.xslt.mapper.model.PredicatedAxiComponent;
import org.netbeans.modules.xslt.model.XslComponent;

/**
 * The visitor is intended to look for predicate expressions 
 * inside of an XPath.
 *
 * @author nk160297
 */
public class PredicateFinderVisitor extends AbstractXPathVisitor {
    
    private XsltMapper mapper;
    private XPathLocationPath myLocationPath;
    private XslComponent contextXslComp;
    
    public PredicateFinderVisitor(XsltMapper mapper) {
        this.mapper = mapper;
    }
    
    public void setContextXslComponent(XslComponent xslc) {
        contextXslComp = xslc;
    }
    
    //---------------------------------------------------
    
    public void visit(XPathLocationPath expr) {
        myLocationPath = expr;
        //
//        if (!expr.getAbsolute()) {
//            assert false : "Only absolute location pathes are supported now."; // NOI18N
//        }
        //
        LocationPathConverter converter = new LocationPathConverter();
        converter.processLocationPath(expr.getSteps());
    }
    
    public void visit(XPathExpressionPath expressionPath) {
        XPathExpression rootExpr = expressionPath.getRootExpression();
        rootExpr.accept(this);
        //
        LocationPathConverter converter = new LocationPathConverter();
        converter.processLocationPath(expressionPath.getSteps());
        // assert false : "Variables are not supported in the XSLT mapper yet"; // NOI18N
    }
    
    public void visit(XPathVariableReference vReference) {
    }
    
    //---------------------------------------------------
    
    public void visit(XPathCoreOperation expr) {
        visitChildren(expr);
    }
    
    public void visit(XPathCoreFunction expr) {
        visitChildren(expr);
    }
    
    public void visit(XPathExtensionFunction expr) {
        visitChildren(expr);
    }
    
    //---------------------------------------------------
    
    /**
     * See the description of the method "processLocationPath"
     */ 
    private class LocationPathConverter {
        // The list can contain objects of either AXIComponent
        // or PredicatedAxiComponent type.
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
        public void processLocationPath(LocationStep[] stepArr) {
            processingAborted = false;
            //
            for (LocationStep step : stepArr) {
                if (!processingAborted) {
                    // ignore attributes. Process only subelements
                    if (LocationStep.AXIS_CHILD == step.getAxis()) {
                        processStep(step);
                    }
                }
            }
        }
        
        private void processStep(LocationStep step) {
            //
            // Extract the namespace and name of the step
            String name = "";
            String namespace = null;
            //
            String nameTest = step.getNodeTest().toString();
            //
            int pos = nameTest.indexOf(':');
            //
            if (pos != -1){
                String prefix = nameTest.substring(0, pos);
                assert contextXslComp != null && 
                        contextXslComp instanceof AbstractDocumentComponent;
                namespace = ((AbstractDocumentComponent)contextXslComp).
                        lookupNamespaceURI(prefix, true);
                name = nameTest.substring(pos + 1);
            } else {
                namespace = "";
                name = nameTest;
            }
            //
            // Look for the corresponding child AXIOM component
            AXIComponent soughtChildComp = null;
            if (objLocationPath.isEmpty()) {
                // Need look for a root element
                AXIComponent rootComp = mapper.getContext().getSourceType();
                if (isCorresponding(rootComp, name, namespace)) {
                    soughtChildComp = rootComp;
                } else {
                    // Error. The location step has inconsistent type
                    processingAborted = true;
                    return;
                }
            } else {
                Object lastPathItem = objLocationPath.getLast();
                AXIComponent parentAxiComp = null;
                if (lastPathItem instanceof AXIComponent) {
                    parentAxiComp = (AXIComponent)lastPathItem;
                } else if (lastPathItem instanceof PredicatedAxiComponent) {
                    parentAxiComp = ((PredicatedAxiComponent)lastPathItem).getType();
                } else {
                    // Error. The list can contain only objects of the following types:
                    // AXIComponent, PredicatedAxiComponent.
                    processingAborted = true;
                    return;
                }
                //
                assert parentAxiComp != null;
                List<AbstractElement> children = parentAxiComp.getChildElements();
                for (AbstractElement childAxiComp : children) {
                    if (isCorresponding(childAxiComp, name, namespace)) {
                        soughtChildComp = childAxiComp;
                        break;
                    }
                }
            }
            //
            if (soughtChildComp == null) {
                // Error. The location path contains a step with unknown AXIOM type
                // The predicate manager can't continue analysing the path!
                processingAborted = true;
                return;
            }
            //
            XPathPredicateExpression[] predArr = step.getPredicates();
            if (predArr == null || predArr.length == 0) {
                objLocationPath.add(soughtChildComp);
            } else {
                PredicatedAxiComponent newPAxiComp = new PredicatedAxiComponent(
                        soughtChildComp, predArr);
                //
                objLocationPath.add(newPAxiComp);
                //
                LinkedList currentLocationPathList = new LinkedList();
                currentLocationPathList.addAll(objLocationPath);
                //
                PredicateManager pm = mapper.getPredicateManager();
                pm.addPredicate(currentLocationPathList);
            }
        }
        
        /**
         * Check if the specidied AXIOM component has the specified name and namespace.
         */
        private boolean isCorresponding(AXIComponent axiComp,
                String name, String namespace) {
            // Extract the namespace and name of the current Tree Node
            assert axiComp instanceof AXIType;
            String typeName = ((AXIType)axiComp).getName();
            String typeNamespace = AxiomUtils.isUnqualified(axiComp) ?
                "" : axiComp.getTargetNamespace();
            //
            if (typeName.equals(name) && namespace.equals(typeNamespace)){
                return true;
            } else {
                return false;
            }
        }
        
    }
    
}
