/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import java.util.ArrayList;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDependencyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREGeneralization;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREInterface;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRERealization;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IRESuperClass;

/**
 */
public class SourceElementLocator<T>
{
    /**
     * Searches the class hierarchy for the desired element.  The Locator
     * evaluator is used to determine if a element is the desired element.
     * The element that is to be found must implement the == operator.
     *
     * @param evaluator [in] The evaluator to use when searching.
     * @param pLeafContext [in] The leaf of the class hierarchy.
     * @param pClassLoader [in] The class Loader to use.
     * @param traverseGeneralizations [in] true if generalization are to
     *                                     be searched.
     * @param traverseImplementations [in] true if realizations are to
     *                                     be searched.
     * @param pVal [out] The found element.
     */
    public T locate(LocatorEvaluator<T>    evaluator,
            IREClass                leafContext,
            IREClassLoader          classLoader,
            boolean                traverseGeneralizations,
            boolean                traverseImplementations,
            T                       val) {
        if (leafContext == null || classLoader == null)
            return null;
        //Kris - why is "traversImplementations" false??
        
        // First search the leaf.  If we find it then there is no
        // need to search the rest of the tree structure.
        val = evaluator.findElement(leafContext, classLoader, val);
        
        IREGeneralization gens = null;
        IRERealization    real = null;
        if ((val == null || val.equals(null)) && traverseGeneralizations) {
            gens = leafContext.getGeneralizations();
            // Search the super classes of the pLeafContext.  Note
            // that the this is not a recursive routine.  The
            // TraverseSuperClasses will search the generalization tree.
            val = searchSuperClasses(evaluator, leafContext, gens, classLoader,
                    val);
        }
        
        if ( (val == null || val.equals(null)) && traverseImplementations) {
            real = leafContext.getRealizations();
            if (real != null)
                val = searchInterfaces(evaluator, leafContext, real,
                        classLoader, val);
        }
        
        if ( (val == null || val.equals(null)) && traverseGeneralizations)
            val = traverseSuperClasses(evaluator, leafContext, gens,
                    classLoader, traverseGeneralizations,
                    traverseImplementations, val);
        
        if ( (val == null || val.equals(null)) && traverseImplementations)
            val = traverseInterfaces(evaluator, leafContext, real, classLoader,
                    traverseGeneralizations,
                    traverseImplementations, val);
        
        if (val == null || val.equals(null)) {
            val = traverseStaticImports(evaluator, leafContext, classLoader,
                    traverseGeneralizations,
                    traverseImplementations, val);
        }
        return val;
    }
    
    /**
     * Searches the generalizations to deterimine if any of the super
     * classes define the element.
     *
     * <pre>
     * <b>Note:</b> Only the super classes in the IREGeneralization
     *          structure is checked.  Therefore, only the super classes
     *          of the current context is searched.  The
     *          TraverseSuperClasses will search the generalization tree.
     * </pre>
     *
     * @param evaluator [in] The evaluator to use when searching.
     * @param pContextClass [in] The leaf of the class hierarchy.
     * @param pGeneralizations [in] The super classes to search.
     * @param pClassLoader [in] The class Loader to use.
     * @param pVal [out] The found element.
     */
    protected T searchSuperClasses(LocatorEvaluator<T> evaluator,
            IREClass contextClass,
            IREGeneralization gens,
            IREClassLoader classLoader,
            T val) {
        if (gens == null || classLoader == null) return null;
        
        // TODO: Need to figure out a way to be more generic when the super
        //       class is not specified
        int count = gens.getCount();
        if(count > 0) {
            for (int i = 0; i < count; ++i) {
                IRESuperClass sup = gens.item(i);
                if (sup == null) continue;
                
                String typeName = sup.getName();
                if (typeName != null && typeName.length() > 0) {
                    IREClass scd = classLoader.loadClass(typeName, contextClass);
                    if (scd != null) {
                        val = evaluator.findElement(scd, classLoader, val);
                        if (val != null && !val.equals(null))
                            return val;
                    }
                }
            }
        } else {
            IREClass scd = classLoader.loadClass("Object", contextClass);
            if (scd != null) {
                val = evaluator.findElement(scd, classLoader, val);
                if (val != null && !val.equals(null))
                    return val;
            }
        }
        return val;
    }
    
    /**
     * Searches the realizations to deterimine if any of the interfaces
     * define the element.
     *
     * <pre>
     * <b>Note:</b> Only the interfaces in the IRERealization
     *          structure is checked.  Therefore, only the interfaces
     *          of the current context is searched.  The
     *          TraverseInterfaces will search the implementation tree.
     * </pre>
     *
     * @param evaluator [in] The evaluator to use when searching.
     * @param pContextClass [in] The leaf of the class hierarchy.
     * @param pRealizations [in] The interfaces to search.
     * @param pClassLoader [in] The class Loader to use.
     * @param pVal [out] The found element.
     */
    protected T searchInterfaces(LocatorEvaluator<T> evaluator,
            IREClass contextClass,
            IRERealization real,
            IREClassLoader classLoader,
            T val) {
        if (real == null || classLoader == null)
            return null;
        
        for (int i = 0, count = real.getCount(); i < count; ++i) {
            IREInterface intf = real.item(i);
            if (intf == null) continue;
            String typeName = intf.getName();
            if (typeName != null && typeName.length() > 0) {
                IREClass intfd = classLoader.loadClass(typeName, contextClass);
                if (intfd != null) {
                    val = evaluator.findElement(intfd, classLoader, val);
                    if (val != null && !val.equals(null))
                        return val;
                }
            }
        }
        return val;
    }
    
    
    /**
     * Searches all of the generalizations to deterimine if any of the super
     * classes define the element.  TraverseSuperClasses calls Locate on
     * all super classes found in the IREGeneralization collection.
     *
     * @param evaluator [in] The evaluator to use when searching.
     * @param pContextClass [in] The leaf of the class hierarchy.
     * @param pGeneralizations [in] The super classes to search.
     * @param pClassLoader [in] The class Loader to use.
     * @param pVal [out] The found element.
     */
    protected T traverseSuperClasses(LocatorEvaluator<T> evaluator,
            IREClass contextClass,
            IREGeneralization gens,
            IREClassLoader classLoader,
            boolean traverseGens,
            boolean traverseImpls,
            T val) {
        if (contextClass == null || gens == null || classLoader == null)
            return null;
        
        for (int i = 0, count = gens.getCount(); i < count; ++i) {
            IRESuperClass sup = gens.item(i);
            if (sup == null) continue;
            
            String name = sup.getName();
            if (name != null && name.length() > 0) {
                IREClass scd = classLoader.loadClass(name, contextClass);
                if (scd != null) {
                    val = locate(evaluator, scd, classLoader,
                            traverseGens, traverseImpls, val);
                    if (val != null && !val.equals(null))
                        return val;
                }
            }
        }
        return val;
    }
    
    /**
     * Searches all of the interfaces to deterimine if any of the super
     * classes define the element.  TraverseInterfaces calls Locate on
     * all interfaces found in the IRERealization collection.
     *
     * @param evaluator [in] The evaluator to use when searching.
     * @param pContextClass [in] The leaf of the class hierarchy.
     * @param pGeneralizations [in] The super classes to search.
     * @param pClassLoader [in] The class Loader to use.
     * @param pVal [out] The found element.
     */
    protected T traverseInterfaces(LocatorEvaluator<T> evaluator,
            IREClass contextClass,
            IRERealization real,
            IREClassLoader classLoader,
            boolean traverseGens,
            boolean traverseImpls,
            T val) {
        if (contextClass == null || real == null || classLoader == null)
            return null;
        
        for (int i = 0, count = real.getCount(); i < count; ++i) {
            IREInterface sup = real.item(i);
            if (sup == null) continue;
            
            String name = sup.getName();
            if (name != null && name.length() > 0) {
                IREClass scd = classLoader.loadClass(name, contextClass);
                if (scd != null) {
                    val = locate(evaluator, scd, classLoader,
                            traverseGens, traverseImpls, val);
                    if (val != null && !val.equals(null))
                        return val;
                }
            }
        }
        return val;
    }
    
    protected T traverseStaticImports(LocatorEvaluator<T> evaluator,
            IREClass contextClass,
            IREClassLoader classLoader,
            boolean traverseGens,
            boolean traverseImpls,
            T val) {
        
        if (contextClass == null || classLoader == null)
            return null;
        
        ArrayList < IDependencyEvent > dependencies = (ArrayList < IDependencyEvent >) classLoader.getDependencies(contextClass);
        if (dependencies != null)
            for (IDependencyEvent dependency : dependencies) {
                if (dependency == null) continue;
                
                if(dependency.isStaticDependency() == true) {
                    String name = dependency.getSupplier();
                    
                    if (name != null && name.length() > 0) {
                        //because it is a static import, the last section of the name
                        //needs to be removed to get the class
                        
                        name = name.substring(0, name.lastIndexOf("::"));
                        
                        IREClass scd = classLoader.loadClass(name, contextClass);
                        if (scd != null) {
                            val = locate(evaluator, scd, classLoader,
                                    traverseGens, traverseImpls, val);
                            if (val != null && !val.equals(null))
                                return val;
                        }
                    }
                }
            }
            
            return val;
    }
}