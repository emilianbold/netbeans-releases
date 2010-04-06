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

package org.netbeans.modules.xml.xpath.ext.schema.resolver;

import org.netbeans.modules.xml.xpath.ext.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext.SchemaCompPair;

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
    void setUsedSchemaCompH(Set<SchemaCompHolder> compHolderSet);

    /**
     * Indicates if the context is last in a chain. If it is then it means that
     * there aren't any child context. The getUsedSchemaCompPairs() can return
     * empty set in the case.
     * @return
     */
    boolean isLastInChain();

    void setLastInChain(boolean value);

    /**
     * Compare this and parents' chain context 
     * @param obj
     * @return
     */
    boolean equalsChain(XPathSchemaContext obj);
    
    /**
     * Calculates a text which represents the context. 
     * Text must not contain mentioning the parent context.
     * @return
     */
    String toStringWithoutParent();

    /**
     * Generates correct text with XPath expression.
     *
     * @param nsContext - it is necessary to obtain NS prefixes
     * @param sms - an optional argument. It can be null.
     * It is necessary when schema with empty target namespace is used. 
     * @return
     */
    String getExpressionString(NamespaceContext nsContext, SchemaModelsStack sms);

    /**
     * This class contans current and parent schema components. 
     * It keeps track from which parent schema component the current 
     * component was taken from. 
     */ 
    public final class SchemaCompPair {
        private SchemaCompHolder mCompHolder;
        private SchemaCompHolder mParentCompHolder;
        
        public SchemaCompPair(SchemaCompHolder comp, SchemaCompHolder parent) {
            assert comp != null;
            mCompHolder = comp;
            mParentCompHolder = parent;
        }
        
        public SchemaCompPair(SchemaComponent comp, SchemaCompHolder parent) {
            assert comp != null;
            mCompHolder = SchemaCompHolder.Factory.construct(comp);
            mParentCompHolder = parent;
        }
        
        public SchemaCompHolder getCompHolder() {
            return mCompHolder;
        }
        
        public SchemaCompHolder getParetnCompHolder() {
            return mParentCompHolder;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            //
            SchemaCompHolder parentCompHolder = getParetnCompHolder();
            if (parentCompHolder != null) {
                SchemaCompPair.appendCompName(sb, parentCompHolder);
                sb.append(">");
            }
            SchemaCompHolder schemaCompHolder = getCompHolder();
            SchemaCompPair.appendCompName(sb, schemaCompHolder);
            //
            return sb.toString();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof SchemaCompPair) {
                SchemaCompPair other = (SchemaCompPair)obj;
                return (other.mCompHolder == mCompHolder) && (other.mParentCompHolder == mParentCompHolder);
            }
            //
            return false;
        }
        
        @Override
        public int hashCode() {
            int hash = 5;
            hash = 71 * hash + (this.mCompHolder != null ? this.mCompHolder.hashCode() : 0);
            hash = 71 * hash + (this.mParentCompHolder != null ? this.mParentCompHolder.hashCode() : 0);
            return hash;
        }


        /**
         * Helper method for toString
         */ 
        public static void appendCompName(StringBuilder sb, 
                SchemaCompHolder schemaCompHolder) {
            //
            switch(schemaCompHolder.getComponentType()) {
                case ATTRIBUTE:
                case PSEUDO_ATTRIBUTE:
                    sb.append("@");
                case ELEMENT:
                case PSEUDO_ELEMENT:
                    sb.append(schemaCompHolder.getName());
                    break;
                default:
                    sb.append("???"); // NOI18N
            }
        }
    
    }
    
    public final class Utilities {
        
//        /**
//         * Returns a chain of schema components if there is the only one possible 
//         * variant of it. Otherwise returns null.
//         * @param context
//         * @return
//         */
//        public static List<SchemaComponent> getSchemaCompChain(
//                XPathSchemaContext context) {
//            ArrayList<SchemaComponent> result = new ArrayList<SchemaComponent>();
//            //
//            do {
//                SchemaComponent sComp = getSchemaComp(context);
//                if (sComp == null) {
//                    return null;
//                } else {
//                    result.add(sComp);
//                    context = context.getParentContext();
//                }
//            } while (context != null);
//            //
//            return result;
//        } 

        /**
         * Returns a schema component in case if there is only one possible 
         * variant. Otherwise returns null.
         * @param context
         * @return
         */
        public static SchemaComponent getSchemaComp(XPathSchemaContext context) {
            SchemaCompHolder sCompHolder = getSchemaCompHolder(context, false);
            if (sCompHolder != null)  {
                return sCompHolder.getSchemaComponent();
            }
            //
            return null;
        }
        
        /**
         *
         * @param context
         * @return
         * @deprecated
         */
        public static SchemaCompHolder getSchemaCompHolder(XPathSchemaContext context) {
            return getSchemaCompHolder(context, false);
        }

        /**
         * If the <code>lookForMatryoshkaCore</code> flag is set, then the
         * specified context will be checked. It it is a {@link WrappingSchemaContext}
         * then tne matryoshka's core will be found at first.
         *
         * @see Utilities#getMatryoshkaCore(WrappingSchemaContext)
         * @param context
         * @param lookForMatryoshkaCore
         * @return
         */
        public static SchemaCompHolder getSchemaCompHolder(
                XPathSchemaContext context, boolean lookForMatryoshkaCore) {
            if (context == null)  {
                return null;
            }
            //
            if (lookForMatryoshkaCore && context instanceof WrappingSchemaContext) {
                context = getMatryoshkaCore(WrappingSchemaContext.class.cast(context));
            }
            //
            Set<SchemaCompPair> scPairSet = null;
            if (context.isLastInChain()) {
                scPairSet = context.getSchemaCompPairs();
            } else {
                scPairSet = context.getUsedSchemaCompPairs();
            }           
            //
            if (scPairSet != null && scPairSet.size() > 0) {
                SchemaCompPair scPair = scPairSet.iterator().next();
                if (scPair != null) {
                    SchemaCompHolder sCompHolder = scPair.getCompHolder();
                    return sCompHolder;
                }
            }
            //
            return null;
        }
        
        public static boolean equalsChain(
                XPathSchemaContext cont1, XPathSchemaContext cont2) {
            if (XPathUtils.equal(cont1, cont2)) {
                //
                // Compare parent contexts
                XPathSchemaContext parentCont1 = cont1.getParentContext();
                XPathSchemaContext parentCont2 = cont2.getParentContext();
                //
                if (parentCont1 == parentCont2) {
                    return true;
                }
                if (parentCont1 == null || parentCont2 == null) {
                    return false;
                }
                return parentCont1.equalsChain(parentCont2);
            }
            //
            return false;
        }
                
        public static boolean equalCompPairs(
                XPathSchemaContext cont1, XPathSchemaContext cont2) {
            if (cont1 == cont2) {
                return true;
            }
            if (cont1 == null || cont2 == null) {
                return false;
            }
            //
            // assert cont1 != null && cont2 != null;
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

        /**
         * Calculates the effective namespace of the schema component.
         * 
         * @param sComp
         * @param parentContext is the schema context of the previous 
         * element of a location step.
         * @return
         */
        public static Set<String> getEffectiveNamespaces(
                SchemaComponent sComp, XPathSchemaContext parentContext) {
            //
            SchemaModel ownerModel = sComp.getModel();
            String namespaceUri = ownerModel.getEffectiveNamespace(sComp);
            //
            if (namespaceUri != null && namespaceUri.length() != 0) {
                return Collections.singleton(namespaceUri);
            }
            //
            //------------------------------------------------------------------
            //
            // The target namespace isn't defined
            //
            if (parentContext == null) {
                // parent context is required here!
                return Collections.EMPTY_SET;
            }
            //
            // Result collects possible target namespaces.
            HashSet<String> result = new HashSet<String>();
            //
            // The set of schema components, which require deeper investigations.
            // It is impossible to obtain an effective namespace if a schema 
            // component from the parent context (parentContext) is also located 
            // in the (same or another) schema without a targetNamespace. 
            // So it is required to go to the next schema context (parent of the parent)
            // to resolve the namespace. This set is intended to prevent scanning 
            // unnecessary parent components.
            HashSet<SchemaCompHolder> unresolvedParents = null;
            //
            // The set of models which already has been checked for getting 
            // the effective namespace. It prevents repeated call of the 
            // getEffectiveNamespace method, which is quite expensive. 
            HashSet<SchemaModel> checkedModels = new HashSet<SchemaModel>();
            //
            while (parentContext != null) {
                Set<SchemaCompPair> scPairsSet = parentContext.getUsedSchemaCompPairs();
                HashSet<SchemaCompHolder> unresolvedCH = null;
                if (!(unresolvedParents == null || unresolvedParents.isEmpty())) {
                    // Copy unresolved components which was obtained at previous 
                    // step to separate set. It's necessary because the previous 
                    // containter is going to be used here. 
                    unresolvedCH = new HashSet<SchemaCompHolder>(unresolvedParents);
                }
                for (SchemaCompPair scPair : scPairsSet) {
                    SchemaCompHolder contextSCompHolder = scPair.getCompHolder();
                    if (contextSCompHolder != null) {
                        //
                        if (unresolvedCH != null && 
                                !unresolvedCH.contains(contextSCompHolder)) {
                            // This components' chain has already resolved!
                            // Try take another component
                            continue;
                        }
                        //
                        SchemaModel sModel = contextSCompHolder.
                                getSchemaComponent().getModel();
                        if (sModel == null) {
                            // Invalid Schema in the chain
                            break;
                        }
                        Schema schema = sModel.getSchema();
                        if (schema == null) {
                            // Invalid Schema in the chain
                            break;
                        }
                        //
                        unresolvedParents = new HashSet<SchemaCompHolder>();
                        //
                        if (sModel == ownerModel) {
                            // Skip the same schema model. 
                            // This model doesn't have the target namespace
                            SchemaCompHolder parentCH = scPair.getParetnCompHolder();
                            if (parentCH != null) {
                                unresolvedParents.add(parentCH);
                            }
                            continue;
                        }
                        //
                        String targetNs = schema.getTargetNamespace();
                        if (targetNs == null || targetNs.length() == 0) {
                            // Skip the parent schema without a targetNamespace
                            SchemaCompHolder parentCH = scPair.getParetnCompHolder();
                            if (parentCH != null) {
                                unresolvedParents.add(parentCH);
                            }
                            continue;
                        }
                        //
                        if (checkedModels.contains(sModel)) {
                            // Skip the schema model if it has already checked.
                            SchemaCompHolder parentCH = scPair.getParetnCompHolder();
                            if (parentCH != null) {
                                unresolvedParents.add(parentCH);
                            }
                            continue;
                        }
                        //
                        namespaceUri = sModel.getEffectiveNamespace(sComp);
                        if (namespaceUri != null) {
                            result.add(namespaceUri);
                        }
                        //
                        // Remember the schema model to prevent the repeated call 
                        // of the getEffectiveNamespace method.
                        checkedModels.add(sModel);
                    }
                }
                //
                if (unresolvedParents == null || unresolvedParents.isEmpty()) {
                    // No reason to check the next context layer.
                    break;
                }
                //
                // Move to the parent context
                parentContext = parentContext.getParentContext();
            }
            // 
            return result;
        }
    
        /**
         * Calculates the index of the step for the specified schema context.
         * For example, if there is the following location path:
         *   $Var1/a/b/c/@d then there is the folloing results:
         *   
         *   $Var         -1
         *   a             0
         *   b             1
         *   c             2
         *   d             3
         * 
         * @return
         */
        public static int getSchemaStepIndex(XPathSchemaContext sContext) {
            int counter = -1;
            XPathSchemaContext currContext = sContext;
            while (currContext != null &&
                    !(currContext instanceof VariableSchemaContext)) {
                currContext = currContext.getParentContext();
                counter++;
            }
            //
            return counter;
        }

        /**
         * Sometimes contexts can wrap each other.
         * For example, the {@link PredicatedSchemaContext} implements the
         * interface {@link WrappingSchemaContext} and it means it can wrap
         * another context. So several wrapping contexts can found a matryoshka.
         * The most nested context is a core one.
         * This method looks for the core context recursively.
         *
         * @param ctxt
         * @return
         */
        public static XPathSchemaContext getMatryoshkaCore(WrappingSchemaContext ctxt) {
            XPathSchemaContext base = ctxt.getBaseContext();
            if (base instanceof WrappingSchemaContext) {
                return getMatryoshkaCore(WrappingSchemaContext.class.cast(base));
            } else {
                return base;
            }
        }
    
    }
    
}
