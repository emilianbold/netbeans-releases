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

package org.netbeans.modules.xml.xpath.ext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.Named;

/**
 * It is intended to: 
 *  -- resolve schema types of steps in a location path.
 *  -- specify a context for relative locatoin paths;
 *  -- specify a context for the root step of the absolute locatoin paths;
 *  -- specify a context for XSL templates or for-each constructs.
 * 
 * Contexts can be organized in chains. This chains can contain repeated 
 * parts in case of recursive XML schemas. 
 * 
 * Context can reference multiple Schema components. It is necessary to support 
 * XPath wildcards "*" or double slash "//". In such case a location step can 
 * have a set of possible schema types.
 * 
 * The context isn't intended to specify a global schema element (type) from 
 * which an absolute location path should be started.
 * 
 * It is also isn't intended to specify a schema type, which should be produced
 * as a result of an XPath expression usage.
 * 
 * The main use-case is the following: 
 * -- The new context is constructed or taken from outside and it is specified 
 * for a new XPath model. 
 * -- The Model is parsed and resolved. The internal model schema resolver 
 * assigns context for all components of the XPath model, for which 
 * it can be specified.
 *
 * @author nk160297
 */
public interface XPathSchemaContext {
    
    /**
     * Refers to the parent context.
     */ 
    XPathSchemaContext getParentContext();
    
    /**
     * Returns objects which hold the references to context schema components.
     */ 
    Set<SchemaCompPair> getSchemaCompPairs();
    
    /**
     * This method returns only those schema component pairs which 
     * are used by next element of the context chain.
     */ 
    Set<SchemaCompPair> getUsedSchemaCompPairs();
    
    /**
     * The context can contain multiple variants of Schema component.
     * But if it isn't the last in the chain then there are next chain item 
     * which can specify which schema components are used. 
     */ 
    void setUsedSchemaComp(Set<SchemaComponent> compSet);

    /**
     * Compare this and parents' chain context 
     * @param obj
     * @return
     */
    boolean equalsChain(XPathSchemaContext obj);
    
    /**
     * This class contans current and parent schema components. 
     * It keeps track from which parent schema component the current 
     * component was taken from. 
     */ 
    public final class SchemaCompPair {
        private SchemaComponent mComp;
        private SchemaComponent mParentComp;
        
        public SchemaCompPair(SchemaComponent comp, SchemaComponent parent) {
            mComp = comp;
            mParentComp = parent;
        }
        
        public SchemaComponent getComp() {
            return mComp;
        }
        
        public SchemaComponent getParetnComp() {
            return mParentComp;
        }
        
        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            //
            SchemaComponent parentComp = getParetnComp();
            if (parentComp != null) {
                SchemaCompPair.appendCompName(sb, parentComp);
                sb.append(">");
            }
            SchemaComponent schemaComp = getComp();
            SchemaCompPair.appendCompName(sb, schemaComp);
            //
            return sb.toString();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof SchemaCompPair) {
                SchemaCompPair other = (SchemaCompPair)obj;
                return (other.mComp == mComp) && (other.mParentComp == mParentComp);
            }
            //
            return false;
        }
        

        /**
         * Helper method for toString
         */ 
        public static void appendCompName(StringBuffer sb, SchemaComponent schemaComp) {
            if (schemaComp instanceof Attribute) {
                sb.append("@");
            }
            if (schemaComp instanceof Named) {
                String name = ((Named)schemaComp).getName();
                sb.append(name);
            } else {
                sb.append("???"); // NOI18N
            }
        }
    
    }
    
    public final class Utilities {
        
        /**
         * Returns a chain of schema components if there is the only one possible 
         * variant of it. Otherwise returns null.
         * @param context
         * @return
         */
        public static List<SchemaComponent> getSchemaCompChain(
                XPathSchemaContext context) {
            ArrayList<SchemaComponent> result = new ArrayList<SchemaComponent>();
            //
            do {
                SchemaComponent sComp = getSchemaComp(context);
                if (sComp == null) {
                    return null;
                } else {
                    result.add(sComp);
                    context = context.getParentContext();
                }
            } while (context != null);
            //
            return result;
        } 

        /**
         * Returns a schema component in case if there is only one possible 
         * variant. Otherwise returns null.
         * @param context
         * @return
         */
        public static SchemaComponent getSchemaComp(XPathSchemaContext context) {
            Set<SchemaCompPair> scPairSet = context.getUsedSchemaCompPairs();
            if (scPairSet != null && scPairSet.size() == 1) {
                SchemaCompPair scPair = scPairSet.iterator().next();
                if (scPair != null) {
                    SchemaComponent sComp = scPair.getComp();
                    return sComp;
                }
            } 
            //
            return null;
        }
        
        public static boolean equalsChain(
                XPathSchemaContext cont1, XPathSchemaContext cont2) {
            if (equals(cont1, cont2)) {
                //
                // Compare parent contexts
                XPathSchemaContext parentCont1 = cont1.getParentContext();
                XPathSchemaContext parentCont2 = cont2.getParentContext();
                if (parentCont1 != null && parentCont2 != null) {
                    boolean result = equalsChain(parentCont1, parentCont2);
                    if (!result) {
                        return false;
                    }
                } else if ((parentCont1 == null && parentCont2 != null) || 
                        (parentCont1 != null && parentCont2 == null)) {
                    return false;
                } 
                //
                return true;
            }
            //
            return false;
        }
                
        public static boolean equals(
                XPathSchemaContext cont1, XPathSchemaContext cont2) {
            assert cont1 != null && cont2 != null;
            //
            // Compare component pairs first
            Set<SchemaCompPair> compPairSet1 = cont1.getSchemaCompPairs();
            Set<SchemaCompPair> compPairSet2 = cont2.getSchemaCompPairs();
            //
            assert compPairSet1 != null && compPairSet2 != null;
            //
            if (compPairSet1.size() != compPairSet2.size()) {
                return false;
            }
            //
            Iterator<SchemaCompPair> scpItr1 = compPairSet1.iterator();
            while (scpItr1.hasNext()) {
                SchemaCompPair scp1 = scpItr1.next();
                if (!compPairSet2.contains(scp1)) {
                    return false;
                }
            }
            //
            return true;
        }
        
        /**
         * Generates a new ralative location path by an absolute expression path
         * and a schema context.
         * 
         * @param absolutePath
         * @param context
         * @return
         */
        public static XPathLocationPath generateRelativePath(
                XPathExpressionPath absolutePath, XPathSchemaContext context) {
            //
            LocationStep[] originalSteps = absolutePath.getSteps();
            if (originalSteps == null || originalSteps.length == 0) {
                return null;
            }
            //
            LocationStep lastStep = originalSteps[originalSteps.length - 1];
            XPathSchemaContext lastStepContext = lastStep.getSchemaContext();
            if (lastStepContext == null) {
                // The last step isn't properly resolved
                // Impossible to calculate relative path.
                return null;
            }
            //
            // Obtain the schema context of the root expression
            XPathExpression rootExpression = absolutePath.getRootExpression();
            XPathSchemaContext rootExprContext = null;
            if (rootExpression instanceof XPathSchemaContextHolder) {
                rootExprContext = ((XPathSchemaContextHolder)rootExpression).
                        getSchemaContext();
            }
            if (rootExprContext == null) {
                return null;
            }
            assert rootExprContext.getParentContext() == null : 
                "the root expression has to have not chained schema context";
            //
            // Looking for the deepest common root
            //
            List<XPathSchemaContext> absPathContextsList = 
                    getInversedContextChain(lastStepContext);
            List<XPathSchemaContext> contextsList = 
                    getInversedContextChain(context);
            //
            Iterator<XPathSchemaContext> absPathContextsItr = 
                    absPathContextsList.iterator();
            Iterator<XPathSchemaContext> contextsItr = contextsList.iterator();
            //
            XPathSchemaContext deepestCommonContext = null;
            int commonContextInd = -1;
            //
            while (absPathContextsItr.hasNext() && contextsItr.hasNext()) {
                XPathSchemaContext context1 = contextsItr.next();
                XPathSchemaContext context2 = absPathContextsItr.next();
                //
                if (!context1.equals(context2)) {
                    break;
                }
                //
                commonContextInd++;
                deepestCommonContext = context1;
            }
            //
            if (deepestCommonContext == null) {
                return null;
            }
            if (deepestCommonContext.equals(rootExprContext)) {
                // in this case the relative path doesn't matter 
                // because of the deepest common context is the same as 
                // the context of the root XPath expression.
                return null;
            }
            //
            // Construct the path
            XPathModelFactory factory = lastStep.getModel().getFactory();
            ArrayList<LocationStep> stepsList = new ArrayList<LocationStep>();
            //
            // Construct "Go To Parent" steps
            // Count number of "Go To Parent" steps which is necessary to add.
            int goToParentCount = contextsList.size() - commonContextInd - 1;
            XPathSchemaContext parentContext = context.getParentContext();
            for (int index = 0; index < goToParentCount; index++) {
                LocationStep goToParent = factory.newLocationStep(
                        XPathAxis.PARENT, 
                        new StepNodeTypeTest(StepNodeTestType.NODETYPE_NODE, null), 
                        null);
                goToParent.setSchemaContext(parentContext);
                stepsList.add(goToParent);
            }
            //
            // Copy a tail of of original location step's chain to the new relative path
            // The originalSteps array doesn't contain the first schema context. 
            // It is taken from the root expression of the absolute path. 
            // So the index is reduced by 1.
            for (int index = commonContextInd; index < originalSteps.length; index++) {
                LocationStep originalStep = originalSteps[index];
                stepsList.add(originalStep);
            }
            //
            // Add SELF step (.) if there is not other steps 
            // and the last step of the absolute path has the same schema 
            // context as that which is used to calculate relative path. 
            // This check can be done at the beginning of the method
            // but it is very rare case.
            if (stepsList.size() == 0) {
                if (lastStepContext.equalsChain(context)) {
                    LocationStep selfStep = factory.newLocationStep(
                            XPathAxis.SELF, 
                            new StepNodeTypeTest(StepNodeTestType.NODETYPE_NODE, null), 
                            null);
                    stepsList.add(selfStep);
                }
            }
            //
            XPathLocationPath result = factory.newXPathLocationPath(
                    stepsList.toArray(new LocationStep[stepsList.size()]));
            //
            return result;
        }
        
        /**
         * Constructs the list of schema contexts based on the context chain. 
         * Thi list provides reverced access to the chain so the last element 
         * in the list is the element, which is specified as a paramenter of 
         * the method.
         *
         * @param context
         * @return
         */
        public static List<XPathSchemaContext> 
                getInversedContextChain(XPathSchemaContext context) {
            //
            LinkedList<XPathSchemaContext> result = 
                    new LinkedList<XPathSchemaContext>();
            while (context != null) {
                result.addFirst(context);
                context = context.getParentContext();
            }
            //
            return result;
        }
        
    }
}

