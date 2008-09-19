/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

//	Author:  Aztec
//	  Date:  Jan 19, 2004
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.support.Debug;
import java.util.HashSet;
import java.util.Iterator;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.metamodel.core.foundation.EventContextManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITransitionElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IParameterDirectionKind;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.CollectionTranslator;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.metamodel.structure.IArtifact;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.roundtripframework.ChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IAssociationEndTransformChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IElementDuplicatedChangeRequest;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IDerivationClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.openide.util.NbPreferences;

/**
 */
public class JavaChangeHandlerUtilities 
    extends RequestProcessorUtilities
    implements IJavaChangeHandlerUtilities
{
    private IElementLocator m_loc = null;
    private IChangeRequest m_pRequest = null;

    public void addRedefiningOperation(
        IRequestValidator request,
        IOperation pOrigOp,
        IOperation pNewOp,
        IClassifier pToClass)
    {
        buildRedefinition(pOrigOp, pNewOp);
        if (pToClass != null)
            pToClass.addOperation(pNewOp);

        // Need a  change handler to actually build the dependency.
        IJavaMethodChangeHandler handler = new JavaMethodChangeHandler();
        handler.addDependency(request, pNewOp, pToClass);
    }

    public ETList< IOperation > appendOperationsToList(
            ETList < IOperation > partList,
            ETList < IOperation > fullList)
    {
        if (partList != null)
        {
            if (fullList == null)
            {
                fullList = new ETArrayList < IOperation > ();
            }
            else
            {
                // need to append

                int count = partList.size();
                int idx = 0;
                while (idx < count)
                {
                    IOperation pOp = partList.get(idx++);
                    if (pOp != null)
                    {
                        fullList.add(pOp);
                    }
                }
            }
        }
        return fullList;
    }

    /**
     *
     * Given the derived class, find all applicable methods in the given base class 
     * and add them to the derived class. This goes "up the tree" from the base class,
     * looking at all generalizations and realizations. 
     *
     * @param pBaseElement[in] The base class or implemented interface
     * @param pDerivedElement[in] The derived or implementing class
     * @param behaviorControl[in] The class that controls the collection of operations.
     * @return 
     */
    public void applyInheritedOperations(
        IRequestValidator request,
        IElement pBaseElement,
        IElement pDerivedElement,
        IOperationCollectionBehavior behaviorControl)
    {
        try
        {
            if (pBaseElement != null && pDerivedElement != null)
            {
                IClassifier cpBaseClass =
                    pBaseElement instanceof IClassifier
                        ? (IClassifier) pBaseElement
                        : null;
                IClassifier cpDerivedClass =
                    pDerivedElement instanceof IClassifier
                        ? (IClassifier) pDerivedElement
                        : null;
                if (cpBaseClass != null && cpDerivedClass != null)
                {
                    ETList < IClassifier > cpClassifiers =
                        new ETArrayList < IClassifier > ();
                    cpClassifiers.add(cpBaseClass);
                    applyInheritedOperations(
                        request,
                        cpClassifiers,
                        cpDerivedClass,
                        behaviorControl);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     * Given the derived class, find all applicable methods in the given base class 
     * and add them to the derived class. This goes "up the tree" from the base class,
     * looking at all generalizations and realizations. 
     *
     * @param pBaseElement[in] The base class or implemented interface
     * @param pDerivedElement[in] The derived or implementing class
     * @param behaviorControl[in] The class that controls the collection of operations.
     */
    public void applyInheritedOperations(IRequestValidator request,
                                         ETList < IClassifier > pBaseClasses,
                                         IClassifier pDerivedClass,
                                         IOperationCollectionBehavior behaviorControl)
    {
        try
        {
            boolean bUseDialog = false;

            ETList < IClassifier > allDerivedClasses = null;
            ETList < IOperation > existingRedefs = null;
            ETList < IOperation > vopList = null;

            // Determine the operations to redefine
            ETList < ETPairT < IClassifier, IOperation >> selectedOps =
                    new ETArrayList < ETPairT < IClassifier, IOperation >> ();
            
            ETList < IClassifier > baseClasses = new ETArrayList < IClassifier >();
            if (pBaseClasses != null)
            {
                
                // Modify the base classes collection to not include any derivation
                // classifiers.
                for(IClassifier curClassifeir : pBaseClasses)
                {
                    baseClasses.add(checkIfDerivation(curClassifeir));
                }
                
                //AZTEC: TODO: need to resolve the following
                //CBusyCtrlProxy busyState( _Module.GetResourceInstance(), IDS_JRT_DETERMINE_OPERATIONS );

                // Get all abstract and virtual functions off of base class and
                // apply them to derived class, based on a preference
                int lCnt = baseClasses.size();
                for (int lIndx = 0; lIndx < lCnt; lIndx++)
                {
                    IClassifier cpClassifier = baseClasses.get(lIndx);
                    
                    if (cpClassifier != null)
                        vopList =
                            collectVirtualOperations(
                                cpClassifier, baseClasses,
                                vopList,
                                behaviorControl);

                }
                if (vopList == null)
                {
                    return;
                }

                // Now, before we go any further, automatically discover and build 
                // redefinitions between existing operations that match signatures.

                for (int lIndx = 0; lIndx < lCnt; lIndx++)
                {
                    IClassifier cpClassifier = baseClasses.get(lIndx);

                    if (cpClassifier != null)
                    {
                        ETPairT < ETList < IClassifier >,
                            ETList < IOperation >> pair = buildExistingRedefinitions2(cpClassifier,
                                                                                      pDerivedClass);
                        if (pair != null)
                        {
                            allDerivedClasses = pair.getParamOne();
                            existingRedefs = pair.getParamTwo();
                        }
                    }
                }

                if (!behaviorControl.getSilent())
                {
                    bUseDialog = true;
                }
                else if (behaviorControl.getSilentSelectAll())
                {
                    int count = vopList.size();
                    //int derClassCount = allDerivedClasses.size();
                    
                    for (int i = 0; i < count; i++)
                    {
                        IOperation pItem = vopList.get(i);
                        if (pItem != null)
                        {
                            if (isOperationAlreadyRedefined(pItem, pDerivedClass) == false)
                            {
                                ETPairT<IClassifier, IOperation> data;
                                data = new ETPairT<IClassifier, IOperation>(pDerivedClass, pItem);
                                selectedOps.add(data);
                            }
                        }
                    }
                }
            }

            if (bUseDialog)
            {
            	   if (vopList.size() > 0)
                {
                    MethodsSelectionDialog msd =
                            new MethodsSelectionDialog(
                            allDerivedClasses,
                            baseClasses, this);

                    selectedOps = msd.display();
                }
            }

            if (selectedOps != null)
            {
                    for (int i = 0, count = selectedOps.size(); i < count; i++)
                    {
                            ETPairT < IClassifier, IOperation > p = selectedOps.get(i);
                            IClassifier pImplClass = p.getParamOne();
                            IOperation pImplOp = p.getParamTwo();

                            if (pImplClass != null && pImplOp != null)
                            {
                                    IOperation pNewOp = copyOperation(pImplOp, pImplClass);
                                    if (pNewOp != null)
                                    {
                                            // Copy operation copies faithfully. That means it set the abstract
                                            // flag. We want to make sure it is NOT set.
                                            pNewOp.setIsAbstract(false);
                                            addRedefiningOperation(request,
                                                    pImplOp,
                                                    pNewOp,
                                                    pImplClass);
                                    }
                            }
                    }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected boolean isOperationAlreadyRedefined(
        IOperation pOp,
        IClassifier pClass)
    {
        boolean retval = false;
        if (pOp != null && pClass != null)
        {
            try
            {
                ETList < IOperation > pRedefiningOperations =
                    pClass.getRedefiningOperations2();

                if (pRedefiningOperations != null)
                {
                    int count = pRedefiningOperations.size();

                    for (int index = 0;(index < count) && !retval; index++)
                    {
                        IOperation pCurRedefElement =
                            pRedefiningOperations.get(index);
                        if (pCurRedefElement != null)
                        {
                            String name = pCurRedefElement.getName();
                            String opName = pOp.getName();

                            retval = pOp.isSignatureSame(pCurRedefElement);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return retval;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#attributePrefix(java.lang.String)
     */
    public String attributePrefix()
    {
        return getPreferenceValue("UML_ATTRIBUTE_PREFIX", "m");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#autoNameNavigableEndPreference()
     */
    public boolean autoNameNavigableEndPreference()
    {
        return getBooleanPreferenceValue("UML_SET_NAVIGABLE_END_ROLE_NAME", true);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#breakReadAccessorFromAttribute(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public void breakReadAccessorFromAttribute(IOperation pOp)
    {
        if (pOp != null)
        {
            String reltype = "Realization";
            ETList < IDependency > deps =
                pOp.getClientDependenciesByType(reltype);
            deleteDependencies(deps);
        }
    }

    public void breakReadAccessorsOfAttribute(IAttribute pAttr)
    {
        if (pAttr != null)
        {
            String reltype = "Realization";
            ETList < IDependency > deps =
                pAttr.getSupplierDependenciesByType(reltype);
            deleteDependencies(deps);
        }
    }

    public void deleteDependencies(ETList < IDependency > deps)
    {
        if (deps != null)
        {
            Iterator < IDependency > iter = deps.iterator();
            if (iter != null)
            {
                while (iter.hasNext())
                {
                    IDependency item = iter.next();
                    if (item != null)
                    {
                        item.delete();
                    }
                }
            }
        }
    }

    public void breakRedefinition(IOperation pBaseOp, IOperation pDerivedOp)
    {
        if (pBaseOp != null && pDerivedOp != null)
        {
            pBaseOp.removeRedefiningElement(pDerivedOp);
            pDerivedOp.removeRedefinedElement(pBaseOp);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#breakRedefinitions()
     */
    public void breakRedefinitions(
        ETList < IOperation > baseOps,
        ETList < IOperation > derivedOps)
    {
        if (baseOps != null && derivedOps != null)
        {
            // We will just assume that the Remove routine works right when 
            // the pair does not actually redefine each other.
            int count1 = baseOps.size();
            int idx1 = 0;
            int count2 = derivedOps.size();
            int idx2 = 0;
            while (idx1 < count1)
            {
                IOperation pBase = baseOps.get(idx1++);
                if (pBase != null)
                {
                    idx2 = 0;
                    while (idx2 < count2)
                    {
                        IOperation pDerived = derivedOps.get(idx2++);
                        if (pDerived != null)
                        {
                            breakRedefinition(pBase, pDerived);
                        }
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#breakRedefinitions(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void breakRedefinitions(
        IClassifier pBaseClass,
        IClassifier pDerivedClass)
    {
        // This routine need to be recursive in BOTH directions:
        // Up from Base and Down from Derived.
        // The Up is now handled in BreakRefinitionsPropagated
        // This is because we MIGHT NOT want to do all at the same time.

        ETList < IOperation > baseOps = pBaseClass.getOperations();
        ETList < IOperation > derivedOps = pDerivedClass.getOperations();

        breakRedefinitions(baseOps, derivedOps);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#breakRedefinitions(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public void breakRedefinitions(IOperation pOperation)
    {
        if (pOperation != null)
        {
            ETList < IOperation > pOps = collectRedefiningOps(pOperation);
            Iterator < IOperation > iter = pOps.iterator();
            if (iter != null)
            {
                while (iter.hasNext())
                {
                    IOperation pItem = iter.next();
                    if (pItem != null)
                    {
                        breakRedefinition(pOperation, pItem);
                    }
                }
            }
            pOps = null;
            iter = null;
            pOps = collectRedefinedOps(pOperation);
            iter = pOps.iterator();
            if (iter != null)
            {
                while (iter.hasNext())
                {
                    IOperation pItem = iter.next();
                    if (pItem != null)
                    {
                        breakRedefinition(pItem, pOperation);
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#breakRedefinitionsPropagated(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void breakRedefinitionsPropagated(
        IClassifier pBaseClass,
        IClassifier pDerivedClass)
    {
        if (pBaseClass != null && pDerivedClass != null)
        {
            // Ok, we are going to get all the redefinitions in the derived class,
            // and then look for those classes in the baseclass's derivation tree.

            ETTripleT < ETList < IOperation >,
                ETList < IOperation >,
                ETList < IOperation >> triple =
                    collectRedefinedOps(pDerivedClass, false);
            if (triple != null)
            {
                ETList < IOperation > opsUp = triple.getParamOne();
                ETList < IOperation > opsDown = triple.getParamTwo();
                ETList < IOperation > opsBoth = triple.getParamThree();

                // Ok, if we have ops up, we have work to do.
                if (opsUp != null)
                {
                    ETList < IClassifier > baseClasses = null;

                    int count = opsUp.size();
                    int idx = 0;
                    if (count > 0)
                        baseClasses = collectBaseClasses(pBaseClass);

                    if (baseClasses != null)
                    {
                        while (idx < count)
                        {
                            IOperation pItem = opsUp.get(idx++);
                            if (pItem != null)
                            {
                                // If this operation is from a class in the collection
                                // of base classes, break the redefinition

                                ETList < IOperation > redefs =
                                    collectRedefinedOps(pItem);

                                if (redefs != null)
                                {
                                    int count2 = redefs.size();
                                    int idx2 = 0;
                                    while (idx2 < count2)
                                    {
                                        IOperation pRedefItem =
                                            redefs.get(idx2++);

                                        if (pRedefItem != null)
                                        {
                                            IClassifier pRedefClass =
                                                pRedefItem
                                                    .getFeaturingClassifier();
                                            if (isMember3(pRedefClass,
                                                baseClasses))
                                            {
                                                // Break this redefinition
                                                breakRedefinition(
                                                    pRedefItem,
                                                    pItem);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#breakWriteAccessorFromAttribute(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public void breakWriteAccessorFromAttribute(IOperation pOp)
    {
        if (pOp != null)
        {
            String reltype = "Realization";
            ETList < IDependency > deps =
                pOp.getSupplierDependenciesByType(reltype);
            deleteDependencies(deps);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#breakWriteAccessorsOfAttribute(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute)
     */
    public void breakWriteAccessorsOfAttribute(IAttribute pAttr)
    {
        if (pAttr != null)
        {
            ETList < IDependency > deps =
                pAttr.getClientDependenciesByType("Realization");
            deleteDependencies(deps);
        }
    }

    public void buildRedefinitions(ETList < IOperation > oppairs)
    {
        try
        {
            if (oppairs != null)
            {
                int count = oppairs.size();
                int idx = 0;

                if ((count % 2) == 0)
                {
                    while (idx < count)
                    {
                        IOperation pBaseOp = oppairs.get(idx++);
                        IOperation pDerivedOp = null;
                        if (idx < count)
                        {
                            pDerivedOp = oppairs.get(idx++);
                        }
                        if (pBaseOp != null && pDerivedOp != null)
                        {
                            buildRedefinition(pBaseOp, pDerivedOp);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Debug.logDebugException("", e, true);
        }
    }

    public void buildRedefinition(IOperation pBaseOp, IOperation pRedefiningOp)
    {
        // Redefinable element has a double backpointer mechanism to 
        // allow us to quickly access operation redefinitions.

        pBaseOp.addRedefiningElement(pRedefiningOp);
        pRedefiningOp.addRedefinedElement(pBaseOp);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#collectAbstractOperations(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IOperationCollectionBehavior)
     */
    public ETList
        < IOperation
        > collectAbstractOperations(
            IClassifier pClass,
            IOperationCollectionBehavior behaviorControl)
    {
        ETList < IOperation > retList = null;
        if (pClass != null && behaviorControl != null)
        {
            try
            {
                boolean doIt = true;

                if (behaviorControl.getInterfacesOnly())
                {
                    doIt = false;
                    String classType = pClass.getElementType();

                    if (classType != null && classType.equals("Interface"))
                        doIt = true;
                }

                if (doIt)
                {
                    ETList < IOperation > classOps = pClass.getOperations();
                    if (classOps != null)
                    {
                        int count = classOps.size();
                        int idx = 0;
                        while (idx < count)
                        {
                            IOperation pItem = classOps.get(idx++);
                            if (pItem != null)
                            {
                                boolean isAbstract = pItem.getIsAbstract();
                                if (isAbstract)
                                {
                                    retList.add(pItem);
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return retList;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#collectBaseClasses(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public ETList < IClassifier > collectBaseClasses(IClassifier pClass)
    {
        ETList < IClassifier > baseClasses = new ETArrayList < IClassifier > ();
        return collectBaseClasses(pClass, baseClasses);
    }

    public ETList
        < IClassifier
        > collectBaseClasses(
            IClassifier pClass,
            ETList < IClassifier > baseClasses)
    {
        try
        {
            if (pClass != null)
            {
                ETList < IClassifier > supers = getGeneralizations(pClass);
                ETList < IClassifier > inters =
                    getImplementedInterfaces(pClass);

                // Now propagate up
                if (supers != null)
                {
                    int count = supers.size();
                    int idx = 0;
                    while (idx < count)
                    {
                        IClassifier pItem = supers.get(idx++);
                        if (pItem != null)
                        {
                            baseClasses.add(pItem);
                            baseClasses =
                                collectBaseClasses(pItem, baseClasses);
                        }
                    }
                }

                if (inters != null)
                {
                    int count = inters.size();
                    int idx = 0;

                    while (idx < count)
                    {
                        IClassifier pItem = inters.get(idx++);
                        if (pItem != null)
                        {
                            baseClasses.add(pItem);
                            baseClasses =
                                collectBaseClasses(pItem, baseClasses);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return baseClasses;
    }

    public ETList
        < IOperation
        > collectRedefinedOps(IClassifier pBaseClass, IClassifier pDerivedClass)
    {
        ETList < IOperation > oppairs = null;
        try
        {
            if (pBaseClass != null && pDerivedClass != null)
            {
                ETList < IOperation > baseOps = pBaseClass.getOperations();
                if (baseOps != null)
                {
                    ETList < IOperation > derivedOps =
                        pDerivedClass.getOperations();
                    if (derivedOps != null)
                    {
                        oppairs = new ETArrayList < IOperation > ();
                        Iterator < IOperation > pBaseIter = baseOps.iterator();
                        if (pBaseIter != null)
                        {
                            while (pBaseIter.hasNext())
                            {
                                IOperation pBaseOp = pBaseIter.next();
                                if (pBaseOp != null)
                                {
                                    Iterator < IOperation > pDerivedIter =
                                        derivedOps.iterator();
                                    if (pDerivedIter != null)
                                    {
                                        while (pDerivedIter.hasNext())
                                        {
                                            IOperation pDerivedOp =
                                                pDerivedIter.next();
                                            boolean isRedefined = false;
                                            if (pDerivedOp != null)
                                                isRedefined =
                                                    isOperationRedefinedBy(
                                                        pBaseOp,
                                                        pDerivedOp);
                                            if (isRedefined)
                                            {
                                                oppairs.add(pBaseOp);
                                                oppairs.add(pDerivedOp);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return oppairs;
    }

    /**
     * Get all operations that are redefined by the given operation.
     * In other words, find the ops in the base classes that are redefined
     * by the method in the derived class. This  redefinition is setup in 
     * ApplyInheritedOperations
     */
    public ETList < IOperation > collectRedefinedOps(IOperation pOp)
    {
        ETList < IOperation > opList = null;
        try
        {
            if (pOp != null)
            {
                ETList < IRedefinableElement > redList =
                    pOp.getRedefinedElements();
                if (redList != null)
                {
                    opList = new ETArrayList < IOperation > ();
                    Iterator < IRedefinableElement > iter = redList.iterator();
                    if (iter != null)
                    {
                        while (iter.hasNext())
                        {
                            IRedefinableElement elem = iter.next();
                            if (elem != null && elem instanceof IOperation)
                            {
                                IOperation op =
                                    elem instanceof IOperation
                                        ? (IOperation) elem
                                        : null;
                                opList.add(op);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return opList;
    }

    public ETTripleT < ETList < IOperation >,
        ETList < IOperation >,
        ETList
            < IOperation
                >> collectRedefinedOps(IClassifier pClass, boolean bExclusive)
    {
        ETList < IOperation > topOps = new ETArrayList < IOperation > ();
        ETList < IOperation > bottomOps = new ETArrayList < IOperation > ();
        ETList < IOperation > middleOps = new ETArrayList < IOperation > ();

        if (pClass != null)
        {
            ETList < IOperation > allOps = pClass.getOperations();
            if (allOps != null)
            {
                Iterator < IOperation > opIter = allOps.iterator();
                if (opIter != null)
                {
                    while (opIter.hasNext())
                    {
                        IOperation pItem = opIter.next();
                        if (pItem != null)
                        {
                            ETList < IOperation > ups =
                                collectRedefinedOps(pItem);
                            ETList < IOperation > downs =
                                collectRedefiningOps(pItem);

                            int count2 = 0;
                            if (ups != null)
                            {
                                count2 = ups.size();
                                if (count2 > 0)
                                {
                                    // this operation is redefining something else
                                    bottomOps.add(pItem);
                                }
                            }

                            int count3 = 0;
                            if (downs != null)
                            {
                                count3 = downs.size();
                                if (count3 > 0)
                                {
                                    // this operation is redefinined by something else
                                    topOps.add(pItem);
                                }
                            }

                            if (count2 > 0 && count3 > 0)
                            {
                                // this operation is both. It is "in the middle"
                                middleOps.add(pItem);
                            }
                        }
                    }

                    // If exclusive, create the outlists using the list subtraction
                    // routines.

                    if (bExclusive)
                    {
                        CollectionTranslator < IOperation,
                            IElement > trans =
                                new CollectionTranslator < IOperation,
                            IElement > ();
                        ETList < IElement > topOpsElements =
                            trans.copyCollection(topOps);
                        ETList < IElement > bottomOpsElements =
                            trans.copyCollection(bottomOps);
                        ETList < IElement > middleOpsElements =
                            trans.copyCollection(middleOps);

                        ETList < IElement > topOpsAfterSub =
                            elementListSubtract(
                                topOpsElements,
                                middleOpsElements);
                        ETList < IElement > bottomOpsAfterSub =
                            elementListSubtract(
                                bottomOpsElements,
                                middleOpsElements);

                        CollectionTranslator < IElement,
                            IOperation > trans1 =
                                new CollectionTranslator < IElement,
                            IOperation > ();
                        topOps = trans1.copyCollection(topOpsAfterSub);
                        bottomOps = trans1.copyCollection(bottomOpsAfterSub);
                    }
                }
            }
        }

        return new ETTripleT < ETList < IOperation >,
            ETList < IOperation >,
            ETList < IOperation >> (topOps, bottomOps, middleOps);
    }

    /**
     *
     * Create an element list containing all of the elements from the first list
     * that are not contained in the second list.
     * Given two sets:
     * Set1 = {A,B,C}
     * Set2 = {B,C,D}
     * SetS1 = Subtract(Set1,Set2) = {A}
     * SetS2 = Subtract(Set2,Set1) = {D}
     *
     * @param list1[in] The first element list
     * @param list2[in] The second element list
     * @param resultList[out] The resultant element list
     */
    public ETList < IElement> elementListSubtract(
            ETList < IElement > list1,
            ETList < IElement > list2)
    {
        ETList < IElement > retList = null;
        if (list1 != null)
        {
            Iterator < IElement > iter = list1.iterator();
            if (iter != null)
            {
                retList = new ETArrayList < IElement > ();
                while (iter.hasNext())
                {
                    IElement elem = iter.next();
                    if (elem != null)
                    {	if(list2 != null)
                      {
                        if (!list2.contains(elem))
                            retList.add(elem);
                      }
                    	else
                    		retList.add(elem);	
                    }
                }
            }
        }
        return retList;
    }

    public ETList<IElement> classesToElements ( ETList<IClassifier> inList)
	{
    	ETList<IElement> outList = null;
    	try
		{
    		if ( inList != null )
    		{
    			Iterator < IClassifier > iter = inList.iterator();
    			if (iter != null)
                {
    				outList = new ETArrayList < IElement >();
    				while ( iter.hasNext() ) 
    				{
    					IClassifier pItem = iter.next();
    					 IElement  pElementItem =  (pItem instanceof IElement) 
						 				? (IElement)pItem : null;
						 if (pElementItem != null)
						 	outList.add(pElementItem);
    				}
                }
    		}
		}
    	catch( Exception e )
		{
    		Log.stackTrace(e);
		}
    	return outList;
	}

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#collectRedefiningOps(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
     */
    public ETList < IOperation > collectRedefiningOps(IOperation pOp)
    {
        ETList < IOperation > opList = null;
        try
        {
            if (pOp != null)
            {
                ETList < IRedefinableElement > redList =
                    pOp.getRedefiningElements();
                if (redList != null)
                {
                    opList = new ETArrayList < IOperation > ();
                    Iterator < IRedefinableElement > iter = redList.iterator();
                    if (iter != null)
                    {
                        while (iter.hasNext())
                        {
                            IRedefinableElement elem = iter.next();
                            if (elem != null && elem instanceof IOperation)
                            {
                                IOperation op =
                                    elem instanceof IOperation
                                        ? (IOperation) elem
                                        : null;
                                opList.add(op);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return opList;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#compareNames(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement)
     */
    public boolean compareNames(INamedElement pItem1, INamedElement pItem2)
    {
        boolean isSame = false;
        if (pItem1 != null
            && pItem2 != null
            && pItem1.getName() != null
            && pItem2.getName() != null
            && pItem1.getName().equals(pItem2.getName()))
            isSame = true;

        return isSame;

    }

    public boolean compareSignatures(IOperation pOp1, IOperation pOp2)
    {
        boolean isSameSignature = false;

        try
        {
            if (pOp1 != null && pOp2 != null)
            {
                // first quick check: Same name?
                boolean sameName = compareNames(pOp1, pOp2);
                if (sameName)
                {
                    ETList < IParameter > parms1 = pOp1.getFormalParameters();
                    ETList < IParameter > parms2 = pOp2.getFormalParameters();

                    // Next quick check. Same number of parameters?
                    int count1 = 0;
                    int count2 = 0;

                    if (parms1 != null)
                        count1 = parms1.size();
                    if (parms2 != null)
                        count2 = parms2.size();

                    if (count1 == count2)
                    {
                        // Ok, check the types of the parameters. Thankfully, this 
                        // is not an n-squared check since the order must be the same

                        int idx = 0;

                        boolean sameParms = true;
                        while (idx < count1 && sameParms)
                        {
                            IParameter pItem1 = parms1.get(idx);
                            IParameter pItem2 = parms2.get(idx++);

                            if (pItem1 != null && pItem2 != null)
                            {
                                sameParms = compareTypes(pItem1, pItem2);
                            }
                            else
                            {
                                // what if only one of the parameters is null? I guess we
                                // should return false here.

                                if (pItem1 == null
                                    && pItem2 != null
                                    || pItem2 == null
                                    && pItem1 != null)
                                {
                                    sameParms = false;
                                }
                            }
                        }

                        if (sameParms)
                        {
                            // The name is the same and the parameter types are the 
                            // same. This means they are the same operation signature.

                            isSameSignature = true;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return isSameSignature;
    }

    /**
     *
     * copies the multiplicity OF the typed element
     *
     * @param pOrig[in]  The original element
     * @param pNew[in]   The new element
     */
    public void copyMultiplicity(ITypedElement pOrig, ITypedElement pNew)
    {
        try
        {
            if (pOrig != null && pNew != null)
            {
                IMultiplicity pMult = pOrig.getMultiplicity();
                if (pMult != null)
                {
                    ETList < IMultiplicityRange > ranges = pMult.getRanges();
                    if (ranges != null)
                    {
                        int count = ranges.size();
                        int idx = 0;

                        IMultiplicity pNewMult = pNew.getMultiplicity();

                        while (idx < count && pNewMult != null)
                        {
                            IMultiplicityRange pItem = ranges.get(idx++);
                            if (pItem != null)
                            {
                                String lower = pItem.getLower();
                                String upper = pItem.getUpper();

                                IMultiplicityRange pNewRange =
                                    pNewMult.createRange();
                                if (pNewRange != null)
                                {
                                    pNewRange.setRange(lower, upper);
                                    pNewMult.addRange(pNewRange);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#copyOperation(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public IOperation copyOperation(IOperation pOrig, IClassifier pOwnerOfNew)
    {
        IOperation pNew = null;
        if (pOrig != null)
        {
            try
            {
                // if the owner is null, use the owner of the original
                IElement pElem = getTrueOwner(pOrig, pOwnerOfNew);
                IClassifier pOwner = null;
                if (pElem != null)
                    pOwner = (IClassifier) pElem;

                if (pOwner != null)
                {
                    IParameter pOrigTypeParm = pOrig.getReturnType();

                    IClassifier pOrigType = null;
                    if (pOrigTypeParm != null)
                    {
                        pOrigType = getType(pOrigTypeParm);
                    }

                    String origName = stringFixer(pOrig.getName());

                    pNew = pOwner.createOperation2(pOrigType, origName);
                    if (pNew != null)
                    {
                        // Make sure to copy the multiplicity of the return parameter
                        IParameter pNewTypeParm = pNew.getReturnType();
                        if (pNewTypeParm != null && pOrigTypeParm != null)
                        {
                            copyMultiplicity(pOrigTypeParm, pNewTypeParm);
                        }
                        // ----------------------------------------------------------------
                        // IOperation
                        // ----------------------------------------------------------------

                        String origAlias = pOrig.getAlias();
                        pNew.setAlias(origAlias);

                        // TODO : Copy PostConditions here
                        // TODO : Copy PreConditions here
                        // Copy RaisedExceptions 

                        ETList < IClassifier > exceptions =
                            pOrig.getRaisedExceptions();
                        if (exceptions != null)
                        {
                            int count = exceptions.size();
                            int idx = 0;
                            while (idx < count)
                            {
                                IClassifier pItem = exceptions.get(idx++);
                                if (pItem != null)
                                {
                                    pNew.addRaisedException(pItem);
                                }
                            }
                        }

                        pNew.setIsQuery(pOrig.getIsQuery());

                        // ----------------------------------------------------------------
                        // IBehavioralFeature
                        // ----------------------------------------------------------------

                        pNew.setConcurrency(pOrig.getConcurrency());
                        pNew.setIsAbstract(pOrig.getIsAbstract());

                        // TODO : Copy raised signals here
                        // TODO : Copy handled signals here

                        ETList < IParameter > origParameters =
                            pOrig.getParameters();

                        if (origParameters != null)
                        {
                            int count = origParameters.size();
                            int idx = 0;
                            while (idx < count)
                            {
                                IParameter pOrigParm =
                                    origParameters.get(idx++);
                                if (pOrigParm != null)
                                {
                                    // Don't copy the return parameter since it 
                                    // was set when we created the operation

                                    int parmkind = pOrigParm.getDirection();
                                    if (parmkind
                                        != IParameterDirectionKind.PDK_RESULT)
                                    {
                                        IParameter pNewParm =
                                            copyParameter(pOrigParm, pNew);

                                        if (pNewParm != null)
                                        {
                                            pNew.addParameter(pNewParm);
                                        }
                                    }
                                }
                            }
                        }

                        // TODO? : Copy methods?
                        // TODO? : Copy Representations?

                        // ----------------------------------------------------------------
                        // IFeature
                        // ----------------------------------------------------------------
                        pNew.setIsStatic(pOrig.getIsStatic());

                        // ----------------------------------------------------------------
                        // IRedefinableElement
                        // ----------------------------------------------------------------

                        pNew.setIsFinal(pOrig.getIsFinal());
                        copyNamedElementData(pOrig, pNew);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return pNew;
    }

    protected void copyNamedElementData(
        INamedElement pOrig,
        INamedElement pCopy)
    {
        if (pOrig != null && pCopy != null)
            pCopy.setVisibility(pOrig.getVisibility());
        copyElementData(pOrig, pCopy);
    }

    protected void copyElementData(IElement pOrig, IElement pCopy)
    {
        if (pOrig != null && pCopy != null)
        {
            try
            {
                // Question: Do we copy tagged values or not? Some tagged values
                // could be considered data, while other could be considered
                // instance info. For instance, a tagged value called "File"
                // would probably not be a good thing to copy.
                // For now, we say we copy tagged values since things like
                // artifacts would own the tagged values we don't want to copy,
                // and we don't copy the artifacts.

                ETList < ITaggedValue > origTags = pOrig.getTaggedValues();

                if (origTags != null)
                {
                    int count = origTags.size();
                    int idx = 0;

                    while (idx < count)
                    {
                        ITaggedValue pTag = origTags.get(idx++);
                        if (pTag != null)
                        {
                            // We copy this as a UML 1.3 tag. See FoundationIFaces.idl
                            String tagName = stringFixer(pTag.getName());
                            String tagValue = stringFixer(pTag.getDataValue());

                            ITaggedValue pNewTag =
                                pCopy.addTaggedValue(tagName, tagValue);

                            // Copy the Named element data
                            if (pNewTag != null)
                            {
                                copyNamedElementData(pTag, pNewTag);
                            }
                        }
                    }
                }

                // Documentation is stored as a tagged value. So do tags first

                String origDoc = stringFixer(pOrig.getDocumentation());
                pCopy.setDocumentation(origDoc);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    protected IElement getTrueOwner(IElement pOrig, IElement pOwnerOfNew)
    {
        IElement pTrueOwner = null;
        if (pOwnerOfNew == null)
        {
            if (pOrig != null)
                pTrueOwner = pOrig.getOwner();
        }
        else
        {
            pTrueOwner = pOwnerOfNew;
        }
        return pTrueOwner;
    }

    public IParameter copyParameter(
        IParameter pOrig,
        IBehavioralFeature pOwnerOfNew)
    {
        IParameter pNew = null;
        if (pOrig != null)
        {
            try
            {
                // if the owner is null, use the owner of the original
                IElement pElem = getTrueOwner(pOrig, pOwnerOfNew);
                IBehavioralFeature pOwner = null;
                if (pElem != null)
                    pOwner = (IBehavioralFeature) pElem;

                if (pOwner != null)
                {
                    // We need the type and name before we can create
                    // it, so we get IParameter stuff and ITypedElement
                    // stuff now, before creation.

                    // -------------------------------------------------------------------
                    // IParameter
                    // -------------------------------------------------------------------

                    String origName = stringFixer(pOrig.getName());

                    int origDirection = pOrig.getDirection();
                    IExpression pOrigDefault = pOrig.getDefault();
                    // TODO : Need a CopyExpression and copy the default HERE

                    // -------------------------------------------------------------------
                    // ITypedElement
                    // -------------------------------------------------------------------

                    IClassifier pOrigType = getType(pOrig);

                    int origOrder = pOrig.getOrdering();

                    // -------------------------------------------------------------------
                    // Create the copy
                    // -------------------------------------------------------------------

                    pNew = pOwner.createParameter2(pOrigType, origName);
                    if (pNew != null)
                    {
                        pNew.setDirection(origDirection);
                        //pNew->put_Default ( pNewDefault ) ); // TODO : cannot do this until we copy the expression

                        copyMultiplicity(pOrig, pNew);
                        pNew.setOrdering(origOrder);

                        // ----------------------------------------------------------------
                        // Now copy the element data onto the new guy
                        // CopyElementData only copies stuff we deem relevant to a copy.
                        // It doesn't copy stuff that pertains only to the original
                        // instance, like "presentation element" and stuff like that. Only
                        // fields that are classified as "data"
                        // ----------------------------------------------------------------

                        copyElementData(pOrig, pNew);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return pNew;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#createChangeRequest(int, int, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public IChangeRequest createChangeRequest(
        Class requestClass,
        int cType,
        int cDetail,
        IElement pBefore,
        IElement pAfter,
        IElement pElementForFiles)
    {
        if (pBefore == null || pAfter == null)
            return null;

        try
        {
            IChangeRequest pReq =
                requestClass == null
                    ? new ChangeRequest()
                    : (IChangeRequest) requestClass.newInstance();

            pReq.setState(cType);
            pReq.setRequestDetailType(cDetail);
            pReq.setLanguage("Java");

            pReq.setBefore(pBefore);
            pReq.setAfter(pAfter);

            return pReq;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void createConstructor(IClassifier pClass)
    {
        createConstructor(pClass, false);
    }

    public void createConstructor(IClassifier pClass, boolean force)
    {
        boolean doIt = force;
        if (!force)
        {
            // need to check the preference first
            doIt = createConstructorPreference();
        }

        if (doIt)
        {
            // Create the constructor function in the class element
            IOperation pOp = pClass.createConstructor();
            if (pOp != null)
            {
                // make absolutely sure that this routine is public
                pOp.setVisibility(IVisibilityKind.VK_PUBLIC);
                pClass.addOperation(pOp);
            }
        }
    }

    public boolean createConstructorPreference()
    {
        return getBooleanPreferenceValue("UML_ADD_CTORS", true);
    }

    public void createDestructor(IClassifier pClass)
    {
        createDestructor(pClass, false);
    }

    /**
     * Create a destructor method on the class. In Java, this is the finalize 
     * method.
     */
 
    public void createDestructor(IClassifier pClass, boolean force)
	{
    	try
		{
    		boolean doIt = force;
    		if ( !force )
    		{
    			// need to check the preference first
    			doIt = createDestructorPreference();
    		}
    		if ( doIt )
    		{
    			// Unlike constructors, where we create one if told because
    			// they can be overloaded, there can only ever be one destructor
    			// ever. 
    			ETList<IOperation> ops = getDestructors(pClass);
    			if ( ops != null )
    			{
    				int count = ops.size();
    				if ( count > 0 )
    				{
    					doIt = false;
    				}
    			}
    		}
    		if ( doIt )
    		{
    			// Create the destructor function in the class element 
    			String opName = "finalize";
    			String retType = "void";
    			int visibility = IVisibilityKind.VK_PROTECTED;
                ETList<IJRPParameter> noParms = null;
    			IOperation  pOp = addOperationToClass (pClass,opName,retType,
    					                            noParms,visibility,true);
    			if ( pOp != null )
    			{
    				pOp.setIsDestructor (true);
    			}
    		}
		}
    	catch( Exception e )
		{
    		Log.stackTrace(e);
		}
	}
    
    public boolean createDestructorPreference ()
    {
       return getBooleanPreferenceValue ( "UML_ADD_DTORS", false );
    }
    
    public ETList<IOperation> getDestructors(IClassifier  pClass)
	{
    	try
		{
           ETList<IOperation> classOps = pClass.getOperations();
            ETList<IOperation> retOps = null;
    		if(classOps != null )
    		{
    			int count = classOps.getCount();
    			int idx = 0;
    			while ( idx < count )
    			{
    				IOperation  pOp = classOps.get(idx++);
    				if ( pOp != null )
    				{
    					boolean isDestructor = false;
                        isDestructor =  pOp.getIsDestructor ();
    					if ( isDestructor == true )
    					{
                            retOps.add(pOp);
    					}
    				}
    			}
    			if ( retOps != null )
    			{
    				return retOps;
    			}
    		}
		}
        catch( Exception e )
        {
            Log.stackTrace(e);
        }
        return null;
	}
    
    public boolean createReadAccessor(
        IAttribute pAttr,
        IClassifier pClass,
        boolean force)
    {
        boolean valid = true;
        if (pAttr != null && pClass != null)
        {
            try
            {

                boolean doIt = force;
                if (!force)
                {
                    // need to check the preference first
                    doIt = createAccessorPreference();
                }

                // Currently we were using this function to validate the request because
                // we didn't want to have to do the same thing twice ( get the type to 
                // see if it is valid, then get it to use it ). Thus, even if the pref
                // is not set, we still want to validate the request

                // Create the accessor function to the owner of
                // the attribute inside the request. The name of the
                // accessor function is the name of the attribute 
                // prepended with the preferred prefix.

                String attrName = stringFixer(pAttr.getName());
                IClassifier retType = getType(pAttr);

                String typeName = null;
                if (retType != null)
                    typeName = stringFixer(retType.getFullyQualifiedName(false));

                // Here is the batch attribute problem all over again.
                // We have to make sure that at least the name and type
                // are set. But wait, a constructor or destructor does not
                // have a type, so how would CreateOperation know to succeed
                // or fail. This is batch attribute all over again!!!!!
                // In this case, that of an accessor, we know that a type
                // is expected, but that means that the attribute must be 
                // built correctly by whoever is adding it (attribute editor, eg).
                if (attrName != null
                    && attrName.trim().length() > 0
                    && typeName != null
                    && typeName.trim().length() > 0)
                {
                    if (doIt)
                    {
                        String opNamePrefix = readAccessorPrefix();
                        String attrNameFix =
                            removePrefixFromAttributeName(attrName);
                        String attrNameCap = capAttributeName(attrNameFix);
                        String opName = attrNameCap;
                        if ( !removePrefixFromAccessor())
                        {
                            if (opNamePrefix != null && attrNameCap != null )
                            {
                                opName = opNamePrefix + attrNameCap;
                            }
                        }

                        IOperation pAccessor = null;
                        ETList < IJRPParameter > noParms =
                            new ETArrayList < IJRPParameter > ();

                        IMultiplicity pMult = pAttr.getMultiplicity();
                        if (pMult != null)
                        {
                            long rangeCount = pMult.getRangeCount();
                            if (rangeCount > 0)
                            {
                                String nullstring = "";
                                JRPParameter retparm =
                                    new JRPParameter(
                                        nullstring,
                                        nullstring,
                                        IParameterDirectionKind.PDK_RESULT);

                                while (rangeCount > 0)
                                {
                                    retparm.addRange("0", "*");
                                    rangeCount--;
                                }
                                noParms.add(retparm);
                            }
                        }

                        pAccessor =
                            addOperationToClass(
                                pClass,
                                opName,
                                retType,
                                noParms,
                                false);
                            
                        // We use a realization relationship to link up the attribute
                        // and the accessor so that we don't have to rely on "attributes"
                        // like we did in GDPro. Relations good, attributes bad.
                        // Our nomenclature is going to be that for a read accessor, the
                        // attribute is the supplier.

                        if (pAccessor != null)
                        {
                            IDependency pDep =  createRealization(pAttr, pAccessor, pClass);
                            pClass.addOperation(pAccessor);
                        }
                    }
                }
                else
                {
                    valid = false;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return valid;
    }

    public boolean createAccessorPreference()
    {
        return getBooleanPreferenceValue("UML_ADD_ACCESSORS", true);
    }

    public IDependency createRealization(
        INamedElement pSupplier,
        INamedElement pClient,
        INamespace pNamespace)
    {
        String reltype = "Realization";

//        ETList<IDependency> deps = pClient.getClientDependenciesByType(reltype);
//        
//        for (IDependency dep: deps)
//        {
//            if (dep.getClient() == pClient)
//                return dep;
//        }
//        
//        
//        deps = pSupplier.getSupplierDependenciesByType(reltype);
//        
//        for (IDependency dep: deps)
//        {
//            if (dep.getSupplier() == pSupplier)
//                return dep;
//        }
        
        IRelationFactory factory = new RelationFactory();

        return factory.createDependency2(
            pClient,
            pSupplier,
            reltype,
            pNamespace);
    }

    public boolean createWriteAccessor(
        IAttribute pAttr,
        IClassifier pClass,
        boolean force)
    {
        boolean valid = true;
        if (pAttr != null && pClass != null)
        {
            IOperation pAccessor = null;
            try
            {
                boolean doIt = force;
                if (!force)
                {
                    // need to check the preference first
                    doIt = createAccessorPreference();
                }

                if (doIt)
                {
                    // Create the accessor function to the owner of
                    // the attribute inside the request. The name of the
                    // accessor function is the name of the attribute 
                    // prepended with the preferred prefix.

                    String attrName = stringFixer(pAttr.getName());
                    String retType = getWriteAccessorReturnType();
                    IClassifier parmType = getType(pAttr);

                    // Here is the batch attribute problem all over again.
                    // We have to make sure that at least the name and type
                    // are set. But wait, a constructor or destructor does not
                    // have a type, so how would CreateOperation know to succeed
                    // or fail. This is batch attribute all over again!!!!!
                    // In this case, that of an accessor, we know that a type
                    // is expected, but that means that the attribute must be 
                    // built correctly by whoever is adding it (attribute editor, eg).

                    if (attrName != null
                        && attrName.length() > 0) // TODO : check against default
                    {
                        // should test for type here, but it is being gotten in
                        // the AddOperation routine. Why check again? batch attribute!!!!
                        // In this case, the attribute type corresponds to the parameter type.

                        
                        String opNamePrefix = writeAccessorPrefix();
                        String attrNameFix =
                            removePrefixFromAttributeName(attrName);
                        String attrNameCap = capAttributeName(attrNameFix);
                        String opName = attrNameCap;
                        if ( !removePrefixFromAccessor())
                        {
                            if (opNamePrefix != null && attrNameCap != null )
                            {
                                opName = opNamePrefix + attrNameCap;
                            }
                        }

                        ETList < IJRPParameter > parms =
                            new ETArrayList < IJRPParameter > ();
                        String parmname = "val";
                        JRPParameter parm =
                            new JRPParameter(
                                parmname,
                                parmType,
                                IParameterDirectionKind.PDK_IN);

                        IMultiplicity pMult = pAttr.getMultiplicity();
                        if (pMult != null)
                        {
                            long rangeCount = pMult.getRangeCount();
                            while (rangeCount > 0)
                            {
                                parm.addRange("0", "*");
                                rangeCount--;
                            }
                        }
                        parms.add(parm);

                        pAccessor =
                            addOperationToClass(
                                pClass,
                                opName,
                                retType,
                                parms,
                                false);
                            
                        // We use a realization relationship to link up the attribute
                        // and the accessor so that we don't have to rely on "attributes"
                        // like we did in GDPro. Relations good, attributes bad.
                        // Our nomenclature is going to be that for a read accessor, the
                        // attribute is the supplier.

                        if (pAccessor != null)
                        {
                            IDependency pDep =
                                createRealization(pAccessor, pAttr, pClass);
                            pClass.addOperation(pAccessor);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return valid;
    }

    protected String getWriteAccessorReturnType()
    {
        return "void";
    }

    /**
     *
     * This is the function that looks for an operation with the same signature.
     * In other words, discovers the redefinition. Notice that this routine does 
     * not assume which is the redefined and which is the redefining. That is 
     * up to the caller of this routine.
     * 
     *
     * @param pOperation[in] The operation that is to be used as the model of the search
     * @param opList[in] The list of operations that is to be searched for an operation identical to the pOperation
     * 
     * @return pIdenticalOperation An operation found in opList that is identical to pOperation
     */
    public IOperation discoverRedefinition(
        IOperation pOperation,
        ETList < IOperation > opList)
    {
        IOperation pIdenticalOperation = null;
        try
        {
            if (pOperation != null)
            {
                boolean opIsStatic = pOperation.getIsStatic();
                boolean opIsFinal = pOperation.getIsFinal();
                int opVisibility = pOperation.getVisibility();

                if (opList != null
                    && !opIsStatic
                    && !opIsFinal
                    && opVisibility != IVisibilityKind.VK_PRIVATE)
                {
                    int count = opList.size();
                    int idx = 0;
                    boolean isSameSig = false;
                    while (idx < count && !isSameSig)
                    {
                        IOperation pItem = opList.get(idx++);

                        boolean isConDes = false;
                        boolean isStatic = false;

                        if (pItem != null)
                        {
                            isConDes = pItem.getIsConstructor();
                            isStatic = pItem.getIsStatic();
                        }

                        if (!isConDes && !isStatic)
                        {
                            // make absolutely sure we are not comparing the same operation
                            if (!isSame(pOperation, pItem))
                            {
                                isSameSig =
                                    compareSignatures(pOperation, pItem);
                            }

                            if (isSameSig)
                            {
                                if (!isRedefinedBy(pOperation, pItem))
                                {
                                    pIdenticalOperation = pItem;
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return pIdenticalOperation;
    }

    /**
     *
     * This routine is used when a generalization or implementation is created.
     * It looks in the two classes for operations that match signature. Because 
     * we want to keep the functions as generic as possible, we DON'T really
     * want to build the redefinition here. But we cannot just return a single
     * operation, since we want to keep the pairs together. So, I really need
     * to create a typedef pair and a vector of those pair. Until then, I just
     * return a list whose length should always be even, where the first operation
     * is the base op, and the second is the redefining op.
     *
     * @param pBaseClass[in]
     * @param pRedefiningClass[in]
     * 
     * @return oppairs[out]
     */
    public ETList
        < IOperation
        > discoverRedefinitions(
            IClassifier pBaseClass,
            IClassifier pRedefiningClass)
    {
        ETList < IOperation > oppairs = new ETArrayList < IOperation > ();
        if (pBaseClass != null && pRedefiningClass != null)
        {
            ETList < IOperation > opsFromRedef =
                pRedefiningClass.getOperations();

            ETList < IOperation > opsFromBase = pBaseClass.getOperations();

            if (opsFromRedef != null && opsFromBase != null)
            {
                int count = opsFromBase.size();
                int idx = 0;
                while (idx < count)
                {
                    IOperation pBaseOp = opsFromBase.get(idx++);
                    if (pBaseOp != null)
                    {
                        IOperation pRedefOp =
                            discoverRedefinition(pBaseOp, opsFromRedef);
                        if (pRedefOp != null)
                        {
                            // add both to the outlist as a pair
                            oppairs.add(pBaseOp);
                            oppairs.add(pRedefOp);
                        }
                    }
                }
            }
        }
        return oppairs;
    }

    protected boolean isRedefinedBy(
        IOperation pOperation,
        IOperation pIdenticalOperation)
    {
        boolean retVal = false;
        if ((pOperation != null) && (pIdenticalOperation != null))
        {
            IClassifier pClassifier = pOperation.getFeaturingClassifier();

            if (pClassifier != null)
            {
                retVal =
                    isOperationAlreadyRedefined(
                        pIdenticalOperation,
                        pClassifier);
            }
        }
        return retVal;
    }

    public String formatOperation(IOperation pOp)
    {
        StringBuffer format = new StringBuffer();

        try
        {
            if (pOp != null)
            {
                String staticStr = "";
                String finalStr = "";
                String abstractStr = "";
                String nativeStr = "";
                String rtMultStr = "";

                boolean isStatic = pOp.getIsStatic();
                boolean isFinal = pOp.getIsFinal();
                boolean isAbstract = pOp.getIsAbstract();
                boolean isNative = pOp.getIsNative();

                if (isStatic)
                {
                    staticStr = "static ";
                }
                if (isAbstract)
                {
                    abstractStr = "abstract ";
                }
                if (isFinal)
                {
                    finalStr = "final ";
                }
                if (isNative)
                {
                    nativeStr = "native ";
                }

                int vis = pOp.getVisibility();
                String visStr = toStr(vis);
                String name = pOp.getName();
                String typeName = pOp.getReturnType2();

                IParameter pParm = pOp.getReturnType();
                if (pParm != null)
                {
                    IMultiplicity pMult = pParm.getMultiplicity();
                    rtMultStr = formatMultiplicity(pMult);
                }

                ETList < IParameter > parms = pOp.getFormalParameters();

                format
                    .append(visStr)
                    .append(" ")
                    .append(staticStr)
                    .append(finalStr)
                    .append(abstractStr)
                    .append(nativeStr);
                // constructors have no type
                if (typeName != null && typeName.length() > 0)
                {
                    format.append(typeName).append(rtMultStr).append(" ");
                }
                format.append(name).append(" (");

                if (parms != null)
                {
                    int count = parms.size();
                    int idx = 0;
                    while (idx < count)
                    {
                        IParameter pItem = parms.get(idx++);
                        if (pItem != null)
                        {
                            if (idx > 1)
                            {
                                format.append(", ");
                            }
                            String parmType = pItem.getTypeName();
                            String parmName = pItem.getName();
                            format.append(" ").append(parmType);
                            format.append(" ").append(parmName);
                            IMultiplicity pMult = pItem.getMultiplicity();
                            String multStr = formatMultiplicity(pMult);

                            format.append(multStr);
                        }
                    }
                }

                format.append(")");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return format.toString();
    }

    public String formatMultiplicity(IMultiplicity pMult)
    {
        StringBuffer multBuffer = new StringBuffer();
        if (pMult != null)
        {
            ETList < IMultiplicityRange > ranges = pMult.getRanges();
            if (ranges != null)
            {
                int count = ranges.size();
                int idx = 0;
                // Java only cares about the number of ranges, not the bounds.
                // The bounds only apply to initial value.

                // In the future, this routine might also be used to format
                // the initial value at the same time.

                while (idx < count)
                {
                    multBuffer.append("[]");
                    idx++;
                }
            }
        }
        return multBuffer.toString();
    }

    protected static String toStr(int vType)
    {
        String val = null;

        switch (vType)
        {
            case IVisibilityKind.VK_PUBLIC :
                val = "public";
                break;
            case IVisibilityKind.VK_PROTECTED :
                val = "protected";
                break;
            case IVisibilityKind.VK_PRIVATE :
                val = "private";
                break;
            case IVisibilityKind.VK_PACKAGE :
                val = "";
                break;
        }
        return val;
    }

    public ETPairT < IAttribute, IClassifier > getAttributeAndClass(IChangeRequest pRequest, boolean fromBefore)
    {
        return getAttributeAndClass(pRequest, null, fromBefore);
    }

    public ETPairT < IAttribute, IClassifier > getAttributeAndClass(
                IChangeRequest pRequest,
                INavigableEnd pNotThisEnd,
                boolean fromBefore)
    {
        IAttribute pAttribute = null;
        IClassifier pClassifier = null;
        try
        {
            IElement pElement = null;

            IElementDuplicatedChangeRequest pDupe = null;
            if (pRequest instanceof IElementDuplicatedChangeRequest)
                pDupe = (IElementDuplicatedChangeRequest) pRequest;

            if (pDupe == null)
            {
                pElement = getElement(pRequest, fromBefore);
            }
            else
            {
                if (fromBefore)
                    pElement = pDupe.getOriginalElement();
                else
                    pElement = pDupe.getDuplicatedElement();
            }
            if (pElement != null)
            {
                pAttribute =
                    pElement instanceof IAttribute
                        ? (IAttribute) pElement
                        : null;
                if (pAttribute != null)
                {
                    INavigableEnd pEnd =
                        pElement instanceof INavigableEnd
                            ? (INavigableEnd) pElement
                            : null;
                    if (pEnd != null)
                    {
                        pClassifier = pEnd.getReferencingClassifier();
                        if (pClassifier == null)
                        {
                            IAssociationEndTransformChangeRequest pTransform =
                                pRequest
                                    instanceof IAssociationEndTransformChangeRequest
                                    ? (IAssociationEndTransformChangeRequest) pRequest
                                    : null;
                            if (pTransform != null)
                            {
                                pClassifier =
                                    pTransform.getOldReferencingClassifier();
                            }
                        }
                    }
                    else
                    {
                        pClassifier = pAttribute.getFeaturingClassifier();
                    }
                }
                else
                {
                    IAssociation pAssoc =
                        pElement instanceof IAssociation
                            ? (IAssociation) pElement
                            : null;
                    if (pAssoc != null)
                    {
                        ETList < IAssociationEnd > ends = pAssoc.getEnds();
                        if (ends != null)
                        {
                            int count = ends.size();
                            int idx = 0;
                            boolean foundIt = false;

                            while (idx < count && !foundIt)
                            {
                                IAssociationEnd pItem = ends.get(idx++);
                                if (pItem != null)
                                {
                                    INavigableEnd pNavEnd =
                                        pItem instanceof INavigableEnd
                                            ? (INavigableEnd) pItem
                                            : null;
                                    if (pNavEnd != null)
                                    {
                                        if (pNotThisEnd != null)
                                        {
                                            boolean sameEnd =
                                                pNotThisEnd.isSame(pNavEnd);
                                            if (!sameEnd)
                                            {
                                                foundIt = true;
                                            }
                                        }
                                        else
                                        {
                                            foundIt = true;
                                        }

                                        if (foundIt)
                                        {
                                            pAttribute = (IAttribute) pNavEnd;
                                            pClassifier =
                                                pNavEnd
                                                    .getReferencingClassifier();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return new ETPairT < IAttribute,
            IClassifier > (pAttribute, pClassifier);
    }

    public IClassifier getClass(IChangeRequest pRequest)
    {
        return getClass(pRequest, false);
    }
    
//Jyothi: Modified this method, and overloaded it to seperate out extraction of IElement
    public IClassifier getClass(IChangeRequest pRequest, boolean fromBefore)
    {
        IClassifier retClass = null;
        try
        {
            IElement pElement = getElement(pRequest, fromBefore);
            setChangeRequest(pRequest);
            retClass = getClass(pElement);             
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return retClass;
    }
         
    public IClassifier getClass(IElement pElement) 
    {
        IClassifier retClass = null;
        if (pElement != null)
            {
                if (pElement instanceof IClassifier)
                {
                    retClass = (IClassifier) pElement;
                }               

                // If we do not have a classifier check if the element is an attibute.
                if (retClass == null)
                {
                    if (pElement instanceof IAttribute)
                    {
                        IAttribute pAttribute = (IAttribute) pElement;
                        ETPairT < IAttribute,IClassifier > operClass = getAttributeAndClass(getChangeRequest(), false);
                        if (operClass != null)
                        {
                            retClass = operClass.getParamTwo();
                        }
                    }
                    else if(pElement instanceof IParameterableElement )
                    {
                       IParameterableElement paramElem = (IParameterableElement)pElement;
                       retClass = paramElem.getTemplate();
                    }
                    else 
                    {
                        ETPairT < IOperation, IClassifier > operClass = getOperationAndClass(getChangeRequest(), false);
                        if (operClass != null)
                        {
                            retClass = operClass.getParamTwo();
                        }
                    }
                    
                }                
            }
        return retClass;
    }

    private IClassifier checkIfDerivation(IClassifier cpClassifier)
    {
        IClassifier retVal = cpClassifier;
        
        if (cpClassifier instanceof IDerivationClassifier)
        {
            IDerivationClassifier classifier = (IDerivationClassifier) cpClassifier;
            IDerivation derivation = classifier.getDerivation();
            if(derivation != null)
            {
                retVal = derivation.getTemplate();
            }
        }

        return retVal;
    }
    
    private IChangeRequest getChangeRequest() {
        return m_pRequest;
    }
    private void setChangeRequest(IChangeRequest pRequest) {
        m_pRequest = pRequest;
    }

    public ETList < IOperation > getConstructors(IClassifier pClass)
    {
        ETList < IOperation > retCons = null;
        if (pClass != null)
        {
            ETList < IOperation > operations = pClass.getOperations();
            if (operations != null)
            {
                Iterator < IOperation > iter = operations.iterator();
                if (iter != null)
                {
                    retCons = new ETArrayList < IOperation > ();
                    while (iter.hasNext())
                    {
                        IOperation oper = iter.next();
                        if (oper.getIsConstructor())
                            retCons.add(oper);
                    }
                }
            }
        }
        return retCons;
    }

    public ETList < IElement > getDependencies(IClassifier pDependentElement)
    {
        ETList < IElement > independents = null;
        try
        {
            if (pDependentElement != null)
            {
                ETList < IDependency > depList =
                    pDependentElement.getClientDependencies();
                if (depList != null)
                {
                    Iterator < IDependency > iter = depList.iterator();
                    if (iter != null)
                    {
                        independents = new ETArrayList < IElement > ();
                        while (iter.hasNext())
                        {
                            IDependency pItem = iter.next();
                            if (pItem != null)
                            {
                                INamedElement pClient = pItem.getSupplier();
                                if (pClient != null)
                                    independents.add(pClient);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return independents;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#getDependents(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public ETList < IElement > getDependents(IClassifier pIndependentElement)
    {
        ETList < IElement > dependents = null;
        try
        {
            if (pIndependentElement != null)
            {
                ETList < IDependency > depList =
                    pIndependentElement.getSupplierDependencies();
                if (depList != null)
                {
                    Iterator < IDependency > iter = depList.iterator();
                    if (iter != null)
                    {
                        dependents = new ETArrayList < IElement > ();
                        while (iter.hasNext())
                        {
                            IDependency pItem = iter.next();
                            if (pItem != null)
                            {
                                INamedElement pClient = pItem.getClient();
                                if (pClient != null)
                                    dependents.add(pClient);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return dependents;
    }

    public IElement getElement(IChangeRequest pRequest, boolean fromBefore)
    {
        IElement pReqElement = null;
        if (fromBefore)
        {
            pReqElement = pRequest.getBefore();
        }
        else
        {
            pReqElement = pRequest.getAfter();
        }
        return pReqElement;
    }

    public ETList < IClassifier > getGeneralizations(IClassifier pSubClass)
    {
        ETList < IClassifier > baseClasses = null;
        try
        {
            if (pSubClass != null)
            {
                ETList < IGeneralization > genRels =
                    pSubClass.getGeneralizations();
                if (genRels != null)
                {
                    Iterator < IGeneralization > iter = genRels.iterator();
                    if (iter != null)
                    {
                        baseClasses = new ETArrayList < IClassifier > ();
                        while (iter.hasNext())
                        {
                            IGeneralization pItem = iter.next();
                            if (pItem != null)
                            {
                                IClassifier pClass = pItem.getGeneral();
                                if (pClass != null)
                                {
                                    baseClasses.add(pClass);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return baseClasses;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#getImplementedInterfaces(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public ETList < IClassifier > getImplementedInterfaces(IClassifier pClass)
    {
        ETList < IClassifier > classList = null;
        try
        {
            if (pClass != null)
            {
                ETList < IImplementation > impList =
                    pClass.getImplementations();
                if (impList != null)
                {
                    Iterator < IImplementation > iter = impList.iterator();
                    if (iter != null)
                    {
                        classList = new ETArrayList < IClassifier > ();
                        while (iter.hasNext())
                        {
                            IImplementation pItem = iter.next();
                            if (pItem != null)
                            {
                                // At this point, we don't know if the passed class is the
                                // implementee, or the implementor. We only want to get the
                                // implementees ( the contracts ).
                                IClassifier pContract = pItem.getContract();
                                if (pContract != null)
                                {
                                    if (!isSame(pClass, pContract))
                                        classList.add(pContract);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return classList;
    }

    public ETList < IClassifier > getImplementingClassifiers(IClassifier pClass,
                                                             ETList < IClassifier > classList)
    {
        try
        {
            if (classList == null)
            {
                classList = new ETArrayList < IClassifier > ();
            }

            if (pClass == null)
                return classList;

            ETList < IImplementation > impList = null;
            ETList < IDependency > deps = pClass.getSupplierDependenciesByType("Implementation");

            if (deps != null)
            {
                int count = deps.size();
                int idx = 0;
                while (idx < count)
                {
                    IDependency pItem = deps.get(idx++);
                    if (pItem != null)
                    {
                        if (pItem instanceof IImplementation)
                        {
                            IImplementation pImpl = (IImplementation)pItem;
                            if (impList == null)
                            {
                                impList = new ETArrayList < IImplementation > ();
                            }
                            impList.add(pImpl);
                        }
                    }
                }
            }

            if (impList != null)
            {
                int count = impList.size();
                int idx = 0;
                while (idx < count)
                {
                    IImplementation pItem = impList.get(idx++);
                    if (pItem != null)
                    {
                        // At this point, we don't know if the passed class is the
                        // implementee, or the implementor. We only want to get the
                        // implementors ( the classes ).

                        IClassifier pImplementor = pItem.getImplementingClassifier();
                        if (pImplementor != null)
                        {
                            if (!isSame(pClass, pImplementor))
                            {
                                classList.add(pImplementor);
                            }
                        }
                    }
                }
            }

            // Ok, now, an interface can be generalized, but we want to get all 
            // implementing classes. That means that we have to navigate generalizations
            // down until we get an interface that is implemented.

            ETList < IClassifier > subInterfaces = getSpecializations(pClass);

            // Now, for each of these interfaces, recursively call this routine.
            if (subInterfaces != null)
            {
                int count = subInterfaces.size();
                int idx = 0;
                while (idx < count)
                {
                    IClassifier pItem = subInterfaces.get(idx++);

                    if (pItem != null)
                    {
                        classList =
                            getImplementingClassifiers(pItem, classList);
                    }
                }
            }
            
            ETList < IDependency > derivations = pClass.getSupplierDependenciesByType("Derivation");

            if (derivations != null)
            {
                int count = derivations.size();
                int idx = 0;
                while (idx < count)
                {
                    IDependency pItem = derivations.get(idx++);
                    if (pItem instanceof IDerivation)
                    {
                        IDerivation derivation = (IDerivation)pItem;
                        if(derivation.getDerivedClassifier() != null)
                        {
                            classList = getImplementingClassifiers(derivation.getDerivedClassifier(), classList);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return classList;
    }

    /**
     *
     * Get classifiers to which the specified class can navigate.
     *
     * @param pClass[in]
     */
    public ETList < IClassifier > getNavigableClasses(IClassifier pClass)
    {
        ETList < IClassifier > independentClasses = null;
        try
        {
            ETList < IAssociationEnd > assocEnds = getParticipatingEnds(pClass);
            if (assocEnds != null)
            {
                Iterator < IAssociationEnd > iter = assocEnds.iterator();
                if (iter != null)
                {
                    independentClasses = new ETArrayList < IClassifier > ();
                    while (iter.hasNext())
                    {
                        IAssociationEnd pItem = iter.next();
                        if (pItem != null)
                        {
                            ETList < IAssociationEnd > otherEnds =
                                pItem.getOtherEnd();
                            if (otherEnds != null)
                            {
                                Iterator < IAssociationEnd > otherIter =
                                    assocEnds.iterator();
                                if (otherIter != null)
                                {
                                    while (otherIter.hasNext())
                                    {
                                        IAssociationEnd pOtherEnd =
                                            otherIter.next();
                                        if (pOtherEnd != null)
                                        {
                                            boolean isNav =
                                                pOtherEnd.getIsNavigable();
                                            if (isNav)
                                            {
                                                IClassifier pEndClass =
                                                    pOtherEnd.getParticipant();
                                                independentClasses.add(
                                                    pEndClass);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return independentClasses;
    }

    /**
     *
     * Get classifiers which can navigate to the specified class.
     *
     * @param pClass[in]
     * 
     * @return dependentClasses
     */
    public ETList < IClassifier > getNavigatingClasses(IClassifier pClass)
    {
        ETList < IClassifier > dependentClasses = null;
        try
        {
            // of course, the real issue here is that,
            // although an association can have many ends,
            // INavigableEnd only return 1 classifier as the
            // referencing classifier. It should return a list.
            // Because of this, we have to do this the hard way.
            ETList < IAssociationEnd > assocEnds = getParticipatingEnds(pClass);
            if (assocEnds != null)
            {
                Iterator < IAssociationEnd > iter = assocEnds.iterator();
                if (iter != null)
                {
                    dependentClasses = new ETArrayList < IClassifier > ();
                    while (iter.hasNext())
                    {
                        IAssociationEnd pItem = iter.next();
                        if (pItem != null)
                        {
                            if (pItem.getIsNavigable())
                            {
                                ETList < IAssociationEnd > otherEnds =
                                    pItem.getOtherEnd();
                                if (otherEnds != null)
                                {
                                    Iterator < IAssociationEnd > otherIter =
                                        assocEnds.iterator();
                                    if (otherIter != null)
                                    {
                                        while (otherIter.hasNext())
                                        {
                                            IAssociationEnd pOtherEnd =
                                                otherIter.next();
                                            if (pOtherEnd != null)
                                            {
                                                IClassifier pEndClass =
                                                    pOtherEnd.getParticipant();
                                                dependentClasses.add(pEndClass);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return dependentClasses;
    }

    public ETPairT < IOperation,
        IClassifier
            > getOperationAndClass(IChangeRequest pRequest, boolean fromBefore)
    {
        IClassifier pClassifier = null;
        IOperation pOperation = null;
        try
        {
            IElement pElement = null;
            IElementDuplicatedChangeRequest pDupe = null;
            if (pRequest != null
                && pRequest instanceof IElementDuplicatedChangeRequest)
                pDupe = (IElementDuplicatedChangeRequest) pRequest;

            if (pDupe == null)
            {
                pElement = getElement(pRequest, fromBefore);
            }
            else
            {
                if (fromBefore)
                    pElement = pDupe.getOriginalElement();
                else
                    pElement = pDupe.getDuplicatedElement();
            }

            if (pElement != null && pElement instanceof IOperation)
            {
                pOperation = (IOperation) pElement;
                if (pOperation != null)
                    pClassifier = pOperation.getFeaturingClassifier();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return new ETPairT < IOperation,
            IClassifier > (pOperation, pClassifier);
    }

    public String getOperationReturnType(IOperation pOperation)
    {
        String retType = null;
        if (pOperation != null)
        {
            IParameter pRetParm = pOperation.getReturnType();
            if (pRetParm != null)
                retType = pRetParm.getTypeName();
        }
        return retType;
    }

    /**
     *
     * Returns the package of the element. If the element is itself a package,
     * the returned package is the same element as the input element.
     *
     * @param pElement[in]
     * 
     * @return pPackage
     */
    public IPackage getPackage(IElement pElement)
    {
        IPackage pPackage = null;
        try
        {
            if (pElement != null)
            {
                pPackage =
                    pElement instanceof IPackage ? (IPackage) pElement : null;
                //Make sure that we are not a project
                if (pPackage != null)
                {
                    if (pPackage instanceof IProject)
                        pPackage = null;
                }
                else
                {
                    IElement pParent = pElement.getOwner();
                    if (pParent != null)
                        pPackage = getPackage(pParent);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return pPackage;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#getParticipatingEnds(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public ETList < IAssociationEnd > getParticipatingEnds(IClassifier pClass)
    {
        ETList < IAssociationEnd > retEnds = null;
        if (pClass != null)
            retEnds = pClass.getAssociationEnds();
        return retEnds;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#getParticipatingNavEnds(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public ETList < INavigableEnd > getParticipatingNavEnds(IClassifier pClass)
    {
        ETList < INavigableEnd > retEnds = new ETArrayList < INavigableEnd > ();
        try
        {
            ETList < IAssociationEnd > assocEnds = getParticipatingEnds(pClass);
            if (assocEnds != null)
            {
                Iterator < IAssociationEnd > iter = assocEnds.iterator();
                if (iter != null)
                {
                    while (iter.hasNext())
                    {
                        IAssociationEnd pItem = iter.next();
                        if (pItem instanceof INavigableEnd)
                        {
                            retEnds.add((INavigableEnd) pItem);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return retEnds;
    }

    /**
     * A convenience function to get the parameter at the specified position in the operation's
     * list of parameters.
     * @param pParamsOp[in] The operation
     * @param position[in] The position of the parameter.
     * 
     * @return IParameter
     */
    public IParameter getPositionParameter(IOperation pParamsOp, int position)
    {
        IParameter retParm = null;
        if (pParamsOp != null)
        {
            ETList < IParameter > parmList = pParamsOp.getParameters();
            if (parmList != null && position < parmList.size())
                retParm = parmList.get(position);
        }
        return retParm;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#getReadAccessorsOfAttribute(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public ETPairT < ETList < IOperation >,
        ETList
            < IDependency
                >> getReadAccessorsOfAttribute(
                    IAttribute pAttr,
                    IClassifier pFromClass)
    {
        ETList < IOperation > pOperations = null;
        ETList < IDependency > pRealizations = null;
        ETList < IOperation > retList = new ETArrayList < IOperation > ();
        try
        {
            // We use a realization relationship to link up the attribute
            // and the accessor so that we don't have to rely on "attributes"
            // like we did in GDPro. Relations good, attributes bad.
            // Our nomenclature is going to be that for a read accessor, the
            // attribute is the supplier.

            String reltype = "Realization";
            ETList < IDependency > deps =
                pAttr.getSupplierDependenciesByType(reltype);

            if (deps != null)
            {
                int count = deps.size();
                int idx = 0;

                ETList < IDependency > accessorDeps = null;
                while (idx < count)
                {
                    IDependency pDep = deps.get(idx++);
                    if (pDep != null)
                    {
                        INamedElement pClient = pDep.getClient();
                        if (pClient != null)
                        {
                            IOperation pOp =
                                pClient instanceof IOperation
                                    ? (IOperation) pClient
                                    : null;
                            if (pOp != null)
                            {
                                boolean doAdd = true;
                                if (pFromClass != null)
                                {
                                    // caller only wants operations from a specific class
                                    doAdd = isSameClass(pOp, pFromClass);
                                }

                                if (doAdd)
                                {
                                    retList.add(pOp);
                                    if (pRealizations != null)
                                    {
                                        if (accessorDeps == null)
                                        {
                                            pRealizations = null;
                                            accessorDeps =
                                                new ETArrayList
                                                    < IDependency
                                                    > ();
                                        }
                                        accessorDeps.add(pDep);
                                    }
                                }
                            }
                        }
                    }
                }

                if (accessorDeps != null && pRealizations != null)
                {
                    pRealizations = accessorDeps;
                }

                if (retList != null)
                {
                    pOperations = retList;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return new ETPairT < ETList < IOperation >,
            ETList < IDependency >> (pOperations, pRealizations);
    }

    public ETList < IAssociationEnd > getReferencingNavEnds(IClassifier pClass)
    {
        ETList < IAssociationEnd > retEnds =
            new ETArrayList < IAssociationEnd > ();
        try
        {
            ETList < IAssociationEnd > assocEnds = getParticipatingEnds(pClass);
            if (assocEnds != null)
            {
                Iterator < IAssociationEnd > iter = assocEnds.iterator();
                if (iter != null)
                {
                    while (iter.hasNext())
                    {
                        IAssociationEnd pItem = iter.next();
                        if (pItem != null)
                        {
                            ETList < IAssociationEnd > otherEnds =
                                pItem.getOtherEnd();
                            if (otherEnds != null)
                            {
                                Iterator < IAssociationEnd > otherIter =
                                    assocEnds.iterator();
                                if (otherIter != null)
                                {
                                    while (otherIter.hasNext())
                                    {
                                        IAssociationEnd pOtherEnd =
                                            otherIter.next();
                                        if (pOtherEnd != null)
                                        {
                                            boolean isNav =
                                                pOtherEnd.getIsNavigable();
                                            if (isNav)
                                            {
                                                retEnds.add(pOtherEnd);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return retEnds;
    }

    public RequestDetails getRequestDetails(IChangeRequest pRequest)
    {
        RequestDetails det = new RequestDetails();
        if (pRequest != null)
        {
            det.rtElementKind = pRequest.getElementType();
            det.changeKind = pRequest.getState();
            det.requestDetailKind = pRequest.getRequestDetailType();
        }
        return det;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#getSpecializations(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public ETList < IClassifier > getSpecializations(IClassifier pBaseClass)
    {
        ETList < IClassifier > subClasses = null;
        try
        {
            if (pBaseClass != null)
            {
                ETList < IGeneralization > genRels =
                    pBaseClass.getSpecializations();
                if (genRels != null)
                {
                    Iterator < IGeneralization > iter = genRels.iterator();
                    if (iter != null)
                    {
                        subClasses = new ETArrayList < IClassifier > ();
                        while (iter.hasNext())
                        {
                            IGeneralization pItem = iter.next();
                            if (pItem != null)
                            {
                                IClassifier pClass = pItem.getSpecific();
                                if (pClass != null)
                                {
                                    subClasses.add(pClass);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return subClasses;
    }

    public IClassifier getType(IElement pAttr)
    {
        IClassifier retClass = null;
        try
        {
            if (pAttr != null)
            {
                if (pAttr instanceof INavigableEnd)
                {
                    INavigableEnd end =
                        pAttr instanceof INavigableEnd
                            ? (INavigableEnd) pAttr
                            : null;
                    if (end != null)
                        retClass = end.getParticipant();
                }
                else if (pAttr instanceof ITypedElement)
                {
                    ITypedElement pTyped =
                        pAttr instanceof ITypedElement
                            ? (ITypedElement) pAttr
                            : null;
                    if (pTyped != null)
                        retClass = pTyped.getType();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return retClass;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#getWriteAccessorsOfAttribute(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public ETPairT < ETList < IOperation >,
        ETList
            < IDependency
                >> getWriteAccessorsOfAttribute(
                    IAttribute pAttr,
                    IClassifier pFromClass)
    {
        ETList < IOperation > pOperations = null;
        ETList < IDependency > pRealizations = null;
        try
        {
            // We use a realization relationship to link up the attribute
            // and the accessor so that we don't have to rely on "attributes"
            // like we did in GDPro. Relations good, attributes bad.
            // Our nomenclature is going to be that for a read accessor, the
            // attribute is the supplier.

            String reltype = "Realization";
            ETList < IDependency > deps =
                pAttr.getClientDependenciesByType(reltype);

            if (deps != null)
            {
                ETList < IOperation > retList =
                    new ETArrayList < IOperation > ();
                int count = deps.size();
                int idx = 0;

                ETList < IDependency > accessorDeps = null;
                while (idx < count)
                {
                    IDependency pDep = deps.get(idx++);
                    if (pDep != null)
                    {
                        INamedElement pSupplier = pDep.getSupplier();
                        if (pSupplier != null)
                        {
                            IOperation pOp =
                                pSupplier instanceof IOperation
                                    ? (IOperation) pSupplier
                                    : null;
                            if (pOp != null)
                            {
                                boolean doAdd = true;
                                if (pFromClass != null)
                                {
                                    // caller only wants operations from a specific class
                                    doAdd = isSameClass(pOp, pFromClass);
                                }

                                if (doAdd)
                                {
                                    retList.add(pOp);
                                    if (pRealizations != null)
                                    {
                                        if (accessorDeps == null)
                                        {
                                            pRealizations = null;
                                            accessorDeps =
                                                new ETArrayList
                                                    < IDependency
                                                    > ();
                                        }
                                        accessorDeps.add(pDep);
                                    }
                                }
                            }
                        }
                    }
                }

                if (accessorDeps != null && pRealizations != null)
                {
                    pRealizations = accessorDeps;
                }

                if (retList != null)
                {
                    pOperations = retList;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return new ETPairT < ETList < IOperation >,
            ETList < IDependency >> (pOperations, pRealizations);
    }

    public boolean isElementUnnamed(INamedElement pElement)
    {
        boolean retVal = false;

        if (pElement != null)
        {
            try
            {
                String elName = pElement.getName();
                IPreferenceAccessor pPref = PreferenceAccessor.instance();
                String defaultName = null;
                if (pPref != null)
                    defaultName = pPref.getDefaultElementName();

                if (elName == null
                    || elName.trim().length() == 0
                    || elName.equals(defaultName))
                {
                    retVal = true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return retVal;
    }

    public boolean isOperationRedefinedBy(IOperation pCandidate,
        ETList < IOperation > opsToCheckAgainst)
    {
        boolean isRedefined = false;
        if (opsToCheckAgainst != null)
        {
            int count = opsToCheckAgainst.size();
            int idx = 0;
            while (idx < count && !isRedefined)
            {
                IOperation pItem = opsToCheckAgainst.get(idx++);
                if (pItem != null)
                {
                    isRedefined = isOperationRedefinedBy(pCandidate, pItem);
                }
            }
        }
        return isRedefined;
    }

    /**
     *
     * Checks to see if the candidate operation is already a redefinition of the other.
     * This means not only to check direct redefinitions, but goes all the way
     * down the redefinition tree.
     *
     * Because an operation can be redefined many times, but will usually redefine
     * only 1, we invert the question: Is pOpToCheckAgainst redefining pCandidate?
     * This should make the search much more efficient. In other words, we assume 
     * that all backpointers are consistent.
     *
     * @param pCandidate[in] The operation that may be redefined
     * @param pOpToCheckAgainst[in] The operation that is to be looked for on the candidates list of redefining operations
     * @return isRedefined[out] True if pCandidate is already redefined by pOpToCheckAgainst
     */
    public boolean isOperationRedefinedBy(
        IOperation pCandidate,
        IOperation pOpToCheckAgainst)
    {
        boolean isRedefined = false;
        try
        {
            ETList < IRedefinableElement > cpRedefinableElements =
                pCandidate.getRedefiningElements();
            if (cpRedefinableElements != null)
            {
                int lCnt = cpRedefinableElements.size();
                for (int lIndx = 0; lIndx < lCnt; lIndx++)
                {
                    IRedefinableElement cpRedefinableElement =
                        cpRedefinableElements.get(lIndx);
                    if (cpRedefinableElement != null)
                    {
                        boolean bIsSame =
                            cpRedefinableElement.isSame(pOpToCheckAgainst);
                        if (bIsSame)
                        {
                            isRedefined = true;
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return isRedefined;
    }

    public boolean isOperationRedefining(
        IOperation pCandidate,
        ETList < IOperation > opsToCheckAgainst)
    {
        return false;
    }

    public boolean isOperationRedefining(
        IOperation pCandidate,
        IOperation pOpToCheckAgainst)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#isSame(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public boolean isSame(IElement pItem1, IElement pItem2)
    {
        boolean isSame = false;
        if (pItem1 != null && pItem2 != null)
        {
            isSame = pItem1.isSame(pItem2);
        }
        return isSame;
    }

    public void moveToClass(IOperation pItem, IClassifier pClass)
    {
        if (pItem != null)
            pItem.moveToClassifier(pClass);
    }

    public void moveToClass2(ETList < IOperation > pOpers, IClassifier pClass)
    {
        if (pOpers != null)
        {
            Iterator < IOperation > iter = pOpers.iterator();
            if (iter != null)
            {
                while (iter.hasNext())
                {
                    IOperation pItem = iter.next();
                    moveToClass(pItem, pClass);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#readAccessorPrefix()
     */
    public String readAccessorPrefix()
    {
        //kris richards - change to NbPreferences
        return NbPreferences.forModule(JavaChangeHandlerUtilities.class).get("UML_READ_ACCESSOR_PREFIX", "get");  // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#removePrefixFromAccessor()
     */
    public boolean removePrefixFromAccessor()
    {
        return getBooleanPreferenceValue("NO_PREFIX_ON_ACCESSORS", false);
    }

    public String removePrefixFromAttributeName(String attrName)
    {
        String attrNameFix = attrName;
        if (attrName != null && !removePrefixFromAccessor())
        {
            String prefix = attributePrefix();
                        
            if (prefix != null && prefix.length() > 0)
            {
                int len = prefix.length();
                //fix for #6175759: See related INF: http://inf.central/inf/integrationReport.jsp?id=39117
                if (attrName.length() > len && attrName.startsWith(prefix) && isReallyAPrefix(attrName, prefix))                                        
                    attrNameFix = attrName.substring(len);
            }
        }
        return attrNameFix;
    }
    
    
    /**
     * A variable name can ne mMainStatus or mainStatus. We should treat 'm' as a prefix only in the former.
     * In former, we should check the next character. If uppercase, we should be treated as a real prefix, else not.
     */
    private boolean isReallyAPrefix(String attrName, String prefix) {
        int index = attrName.indexOf(prefix) + 1;
        String subStr1 = attrName.substring( index, index + 1);        
        String subStr2 = subStr1.toUpperCase();
        
        if(subStr1.equals(subStr2)) {
            return true;
        }        
        
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#setOperationReturnType(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, java.lang.String)
     */
    public void setOperationReturnType(IOperation pOperation, String retType)
    {
        if (pOperation != null)
        {
            IParameter parm = pOperation.getReturnType();
            if (parm != null)
                parm.setTypeName(retType);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpccomponent.IJavaChangeHandlerUtilities#writeAccessorPrefix()
     */
    public String writeAccessorPrefix()
    {
        //kris richards - change to NbPreferences
        return NbPreferences.forModule(JavaChangeHandlerUtilities.class).get("UML_WRITE_ACCESSOR_PREFIX", "set");  // NOI18N
    }

    public boolean isMember(IOperation pItem, ETList < IOperation > pList)
    {
        boolean retval = false;

        if (pItem != null && pList != null)
        {
            int count = pList.size();
            int idx = 0;
            while (idx < count && !retval)
            {
                IOperation pOp = pList.get(idx++);
                retval = isSame(pItem, pOp);
            }
        }
        return retval;
    }

    public ETList < IClassifier > elementsToClasses(ETList < IElement > inList)
    {
        ETList < IClassifier > outList = new ETArrayList < IClassifier > ();
        try
        {
            if (inList != null)
            {
                int count = inList.size();
                int idx = 0;

                while (idx < count)
                {
                    IElement pItem = inList.get(idx++);
                    IClassifier pClassItem =
                        pItem instanceof IClassifier
                            ? (IClassifier) pItem
                            : null;
                    if (pClassItem != null)
                    {
                        outList.add(pClassItem);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return outList;
    }

    public ETList
        < IOperation
        > collectInheritedAbstractOperations(
            IClassifier pClass,
            IOperationCollectionBehavior behaviorControl)
    {
        //C++ is empty
        return null;
    }

    public boolean isMember3(IClassifier pItem, ETList < IClassifier > pList)
    {
        boolean retval = false;

        if (pItem != null && pList != null)
        {
            try
            {
                int count = pList.size();
                int idx = 0;
                while (idx < count && !retval)
                {
                    IClassifier pClass = pList.get(idx++);
                    retval = isSame(pItem, pClass);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return retval;
    }

    /**
     *
     * Return all operations from the given class that are redefinable
     *
     * @param pBaseClass[in] The class to collect methods from
     * @param behaviorControl 
     * 
     * @return opList The list of operations
     */
    public ETList< IOperation > collectVirtualOperations2(
            IClassifier pBaseClass,
			ETList<IClassifier> pBaseClasses,
            ETList < IOperation > opList,
            IOperationCollectionBehavior behaviorControl)
    {
        if (opList == null)
            opList = new ETArrayList < IOperation > ();
        ETList < IOperation > currentList = new ETArrayList < IOperation > ();

        StringBuffer bstrQueryBuf = new StringBuffer();
        bstrQueryBuf =
            bstrQueryBuf
                .append("UML:Element.ownedElement/UML:Operation[ not(@isConstructor='true')")
                .append(" and not(@isDestructor='true')")
                .append(" and not(@isStatic='true')")
				.append(" and not(@isFinal='true')")
                .append(" and not(@visibility='private')");

        if (behaviorControl != null && behaviorControl.getAbstractOnly())
        {
            bstrQueryBuf.append(" and isAbstract='true'");
        }

        bstrQueryBuf.append("]");
        String bstrQuery = bstrQueryBuf.toString();
        ETList < IElement > fullList =
            getLocator().findElementsByQuery(pBaseClass, bstrQuery);

        if (fullList != null)
        {
            int count = fullList.size();
            int idx = 0;
            while (idx < count)
            {
                IElement cpElement = fullList.get(idx++);
                IOperation pOp = null;
                if (cpElement != null)
                    pOp = (IOperation) cpElement;

                if (pOp != null)
                {
                    // Don't add items onto the list that are already redefined by an item
                    // that is already on the list.
					boolean isRedefined = isOperationRedefinedBy(pOp, opList);
					if (!isRedefined)
					{
						currentList.add(pOp);
					}
				}
			}
		}
		// Finally, when we are all done, copy the current list to the output
		// list
		opList = appendOperationsToList(currentList, opList);



		return opList;
	}

	/**
	 *
	 * Return all operations from the given class that are redefinable
	 *
	 * @param pBaseClass[in] The class to collect methods from
	 * @param behaviorControl 
	 * 
	 * @return opList The list of operations
	 */
	public ETList < IOperation > collectVirtualOperations(
                                                            IClassifier pBaseClass,
                                                            ETList<IClassifier> pBaseClasses,
                                                            ETList < IOperation > opList,
                                                            IOperationCollectionBehavior behaviorControl)
	{
            	if (opList == null)
			opList = new ETArrayList < IOperation > ();
		ETList < IOperation > currentList = new ETArrayList < IOperation > ();

		StringBuffer bstrQueryBuf = new StringBuffer();
		bstrQueryBuf =
			bstrQueryBuf
				.append("UML:Element.ownedElement/UML:Operation[ not(@isConstructor='true')")
				.append(" and not(@isDestructor='true')")
				.append(" and not(@isStatic='true')")
				.append(" and not(@isFinal='true')")
				.append(" and not(@visibility='private')");

		if (behaviorControl != null && behaviorControl.getAbstractOnly())
		{
			bstrQueryBuf.append(" and isAbstract='true'");
		}

		bstrQueryBuf.append("]");
		String bstrQuery = bstrQueryBuf.toString();
		ETList < IElement > fullList =
			getLocator().findElementsByQuery(pBaseClass, bstrQuery);
        
        
		if (fullList != null)
		{
			int count = fullList.size();
			int idx = 0;
			while (idx < count)
			{
				IElement cpElement = fullList.get(idx++);
				IOperation pOp = null;
				if (cpElement != null)
					pOp = (IOperation) cpElement;

				if (pOp != null)
				{
					// Don't add items onto the list that are already redefined by an item
					// that is already on the list.

                    boolean isRedefined = isOperationRedefinedBy(pOp, opList);
                    if (!isRedefined)
                    {
                        currentList.add(pOp);
                    }
                }
            }
        }
        // Finally, when we are all done, copy the current list to the output
        // list
        opList = appendOperationsToList(currentList, opList);

        // Now, we have to go up the generalization links and realization link
        // from the base class.
        opList =
            collectOpsFromGeneralizations(pBaseClass, pBaseClasses, opList, behaviorControl);
        opList =
            collectOpsFromImplementations(pBaseClass, pBaseClasses, opList, behaviorControl);

        return opList;
    }

    protected IElementLocator getLocator()
    {
        if (m_loc == null)
            m_loc = new ElementLocator();
        return m_loc;
    }

    public ETList< IOperation > collectOpsFromGeneralizations(
            IClassifier pBaseClass,
			ETList<IClassifier> pBaseClasses,
            ETList < IOperation > opList,
            IOperationCollectionBehavior behaviorControl)
    {
        if (pBaseClass != null)
        {
            ETList < IGeneralization > genRels =
                pBaseClass.getGeneralizations();

            if (genRels != null)
            {
                int count = genRels.size();
                int idx = 0;
                while (idx < count)
                {
                    IGeneralization pRel = genRels.get(idx++);
                    if (pRel != null)
                    {
                        IClassifier pClass = pRel.getGeneral();
                        if (pClass != null)
                        {
                            opList =
                                collectVirtualOperations(
                                    pClass, pBaseClasses,
                                    opList,
                                    behaviorControl);
							// add to list of base classes
							if(pBaseClasses != null 
									&& !pBaseClasses.contains(pClass))
								pBaseClasses.add(pClass);
                        }
                    }
                }
            }
        }
        return opList;
    }

    public ETList< IOperation > collectOpsFromImplementations(
            IClassifier pBaseClass,
			ETList < IClassifier > pBaseClasses,
            ETList < IOperation > opList,
            IOperationCollectionBehavior behaviorControl)
    {
        if (pBaseClass != null)
        {
            ETList < IImplementation > impRels =
                pBaseClass.getImplementations();

            if (impRels != null)
            {
                int count = impRels.size();
                int idx = 0;
                while (idx < count)
                {
                    IImplementation pRel = impRels.get(idx++);
                    if (pRel != null)
                    {
                        IClassifier pInterface = pRel.getContract();
                        if (pInterface != null)
                        {
                            opList =
                                collectVirtualOperations(
                                    pInterface, pBaseClasses,
                                    opList,
                                    behaviorControl);
							// add to list of base classes
							if(pBaseClasses != null 
									&& !pBaseClasses.contains(pInterface))
								pBaseClasses.add(pInterface);
                        }
                    }
                }
            }
        }
        return opList;
    }

    public ETPairT < ETList < IClassifier >, ETList < IOperation >> buildExistingRedefinitions2(
                    IClassifier pBaseClass,
                    IClassifier pDerivedClass)
    {
	return buildExistingRedefinitions2(pBaseClass, pDerivedClass, null);
    }

    public ETPairT < ETList < IClassifier >, ETList < IOperation >> buildExistingRedefinitions2(
                    IClassifier pBaseClass,
                    IClassifier pDerivedClass,
		    HashSet<IClassifier> analyzedSet)
    {
        ETList < IClassifier > ppDerivedClasses = new ETArrayList < IClassifier > ();
        ETList < IOperation > ppExistingRedefs = new ETArrayList < IOperation >();

        ETList < IClassifier > existingClassifiers = new ETArrayList < IClassifier > ();
        if (pBaseClass != null && pDerivedClass != null)
        {
            ppDerivedClasses =
                getAllDerivedClasses(pDerivedClass, ppDerivedClasses, true);
            if (ppDerivedClasses != null)
            {
                int count = ppDerivedClasses.size();
                for (int idx = 0; idx < count; idx++)
                {
                    IClassifier pItem = ppDerivedClasses.get(idx);
                    if (pItem != null)
                    {
			if (analyzedSet != null) 
			{
			    if (analyzedSet.contains(pItem)) 
			    {
				continue;
			    }
			    analyzedSet.add(pItem);
			}

                        ETList < IOperation > newPairs =
                            discoverRedefinitions(pBaseClass, pItem);
                        
                        if(newPairs.size() > 0)
                        {
                            existingClassifiers.add(pItem);
                            // Now, go over the pairs, and build the redefs.
                            buildRedefinitions(newPairs);
                            ppExistingRedefs = appendOperationsToList(newPairs, ppExistingRedefs);
                        }
                    }
                }
            }
        }
//        return new ETPairT < ETList < IClassifier >,
//            ETList < IOperation >> (existingClassifiers, ppExistingRedefs);
        return new ETPairT < ETList < IClassifier >,
            ETList < IOperation >> (ppDerivedClasses, ppExistingRedefs);
    }

    public ETList < IClassifier > getAllDerivedClasses(IClassifier pBaseClass,
                                                       ETList < IClassifier > derivedClasses,
                                                       boolean addBaseToList)
    {
        if (derivedClasses == null)
            derivedClasses = new ETArrayList < IClassifier > ();
        try
        {
            if (pBaseClass != null)
            {
                if (addBaseToList)
                {
                    derivedClasses.add(pBaseClass);
                }
                
                ETList < IClassifier > pSpecializations = getSpecializations(pBaseClass);
                ETList < IClassifier > pImplementations = null;
                pImplementations = getImplementingClassifiers(pBaseClass, pImplementations);

                // We are going to do this in two passes so that the list is built 
                // wide instead of deep.  If we wanted to build deep instead of wide,
                // we would only need to iterate over the lists once. In fact, let me
                // put the following code switch in so that I don't have to rebuild this.

                boolean wideVSdeep = true; // true is wide, false is deep.
                boolean deepVSwide = !wideVSdeep;

                if (wideVSdeep)
                {
                    if (pSpecializations != null)
                    {
                        int count = pSpecializations.size();
                        int idx = 0;
                        while (idx < count)
                        {
                            IClassifier pItem = pSpecializations.get(idx++);
                            if (pItem != null)
                            {
                                derivedClasses.add(pItem);
                            }
                        }
                    }

                    if (pImplementations != null)
                    {
                        int count = pImplementations.size();
                        int idx = 0;
                        while (idx < count)
                        {
                            IClassifier pItem = pImplementations.get(idx++);
                            if (pItem != null)
                            {
                                derivedClasses.add(pItem);
                            }
                        }
                    }
                }

                if (pSpecializations != null)
                {
                    int count = pSpecializations.size();
                    int idx = 0;
                    while (idx < count)
                    {
                        IClassifier pItem = pSpecializations.get(idx++);
                        if (pItem != null)
                        {
                            derivedClasses =
                                getAllDerivedClasses(
                                    pItem,
                                    derivedClasses,
                                    deepVSwide);
                        }
                    }
                }

                if (pImplementations != null)
                {
                    int count = pImplementations.size();
                    int idx = 0;
                    while (idx < count)
                    {
                        IClassifier pItem = pImplementations.get(idx++);
                        if (pItem != null)
                        {
                            derivedClasses =
                                getAllDerivedClasses(
                                    pItem,
                                    derivedClasses,
                                    deepVSwide);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return derivedClasses;
    }

    /**
     *
     * Compare the types of 2 typed elements are return true if they are the same.
     * This compares the identity of the types, which are elements. It does NOT
     * compare the names of the types. So, if the types are different, but actually
     * have the same name, the result will be false, not true.
     *
     * @param pItem1[in] The first element
     * @param pItem2[in] The second element
     * 
     * @return sameType[out] True if the types are the same
     */
    public boolean compareTypes(ITypedElement pItem1, ITypedElement pItem2)
    {
        boolean sameType = false;
        if (pItem1 != null && pItem2 != null)
        {
            IClassifier pType1 = pItem1.getType();
            IClassifier pType2 = pItem2.getType();

            sameType = isSame(pType1, pType2);
        }
        return sameType;
    }

    public String stringFixer(String toFix)
    {
        if (toFix == null || toFix.trim().length() == 0)
            return "";

        return toFix;
    }

    public boolean capAttributeNameInAccessor()
    {
        return getBooleanPreferenceValue("UML_CAP_ON_ACCESSORS", true);
    }

    public String capAttributeName(String attrName)
    {
        String attrNameCap = null;
        if (capAttributeNameInAccessor())
        {
            if (attrName != null && attrName.length() > 0)
            {
                String allChars = attrName.toUpperCase();
                attrNameCap = allChars.substring(0, 1);

                allChars = attrName;
                String therest = allChars.substring(1, attrName.length());
                attrNameCap += therest;
            }
            else
            {
                attrNameCap = attrName;
            }
        }
        else
        {
            attrNameCap = attrName;
        }
        return attrNameCap;
    }

    /**
     * A convenience function to get the parameter of the operation that corresponds
     * to the given parameter. The assumption here is that the operation of the 
     * given parameter is a redefinition of the given operation.
     * @param pOperation[in] The operation
     * @param pParameter[in] The parameter owned by a redefinition of pOperation
     * @param pOpsParam[out] The parameter in pOperation that corresponds to pParameter
     */
    public IParameter getCorrespondingParameter(
        IOperation pOperation,
        IParameter pParameter)
    {
        IParameter retParm = null;
        try
        {
            if (pParameter != null && pOperation != null)
            {
                IBehavioralFeature pFeat = pParameter.getBehavioralFeature();
                if (pFeat != null)
                {
                    IOperation pParamsOp =
                        pFeat instanceof IOperation ? (IOperation) pFeat : null;
                    retParm =
                        getCorrespondingParameter(
                            pOperation,
                            pParamsOp,
                            pParameter);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return retParm;
    }

    /**
     * A convenience function to get the parameter of the operation that corresponds
     * to the given parameter. The assumption here is that the operation of the 
     * given parameter is a redefinition of the given operation.  
     * This overloaded version of the routine is needed to handle deletes and adds,
     * because the operation of the parameter might not be gettable via the parameter.
     * @param pOperation[in] The operation
     * @param pParamsOp[in] The operation that the parameter is owned by. 
     * @param pParameter[in] The parameter owned by a redefinition of pOperation
     * 
     * @return pOpsParam The parameter in pOperation that corresponds to pParameter
     */
    public IParameter getCorrespondingParameter(
        IOperation pOperation,
        IOperation pParamsOp,
        IParameter pParameter)
    {
        IParameter retParm = null;
        int position = getParameterPosition(pParamsOp, pParameter);
        if (position > -1)
        {
            // Get the corresponding parameter in pOperation
            retParm = getPositionParameter(pOperation, position);
        }
        return retParm;
    }

    public int getParameterPosition(
        IOperation pParamsOp,
        IParameter pParameter)
    {
        int position = -1;
        if (pParamsOp != null && pParameter != null)
        {
            ETList < IParameter > parmList = pParamsOp.getParameters();
            if (parmList != null)
            {
                int count = parmList.size();
                int idx = 0;
                while (idx < count && position == -1)
                {
                    IParameter pItem = parmList.get(idx++);
                    if (pParameter.isSame(pItem))
                        position = idx - 1;
                }
            }
        }
        return position;
    }

    public ETList
        < IClassifier
        > add(IClassifier pItem, ETList < IClassifier > pList)
    {
        if (pList == null)
            pList = new ETArrayList < IClassifier > ();
        pList.add(pItem);
        return pList;
    }

    public IOperation addOperationToClass(
        IClassifier pClass,
        String opName,
        IClassifier opType,
        ETList < IJRPParameter > parameters,
        boolean addToClass)
    {
        return addOperationToClass(
            pClass,
            opName,
            opType,
            parameters,
            IVisibilityKind.VK_PUBLIC,
            addToClass);
    }

    public IOperation addOperationToClass(
        IClassifier pClass,
        String opName,
        IClassifier opType,
        ETList < IJRPParameter > parameters,
        int visibility,
        boolean addToClass)
    {
        String retType = null;
        if (opType != null)
            //retType = opType.getFullyQualifiedName(false);
				retType = opType.getName(); //J1408-Creating navigable association between roles creating new package with datatype

        return addOperationToClass(
            pClass,
            opName,
            retType,
            parameters,
            visibility,
            addToClass);
    }

    public IOperation addOperationToClass(
        IClassifier pClass,
        String opName,
        String opType,
        ETList < IJRPParameter > parameters,
        boolean addToClass)
    {
        return addOperationToClass(
            pClass,
            opName,
            opType,
            parameters,
            IVisibilityKind.VK_PUBLIC,
            addToClass);
    }

    public IOperation addOperationToClass(
        IClassifier pClass,
        String opName,
        String opType,
        ETList < IJRPParameter > parameters,
        int visibility,
        boolean addToClass)
    {
        IOperation pOperation = createOperation(pClass, opName, opType);
        if (pOperation != null)
        {
            pOperation.setVisibility(visibility);

            // because of a bug in BaseElement::GetBooleanAttributeValue, I MUST set these
            // flags here

            pOperation.setIsConstructor(false);

            // add the parameters to the operation before we add it to
            // the class. By setting everything before we add to the class,
            // we are trying to eliminate excessive events.

            boolean origBlock = EventBlocker.startBlocking();
            try
            {
                pOperation = addParametersToOperation(pOperation, parameters);
            }
            finally
            {
                EventBlocker.stopBlocking(origBlock);
            }

            if (pClass != null && addToClass)
            {
                pClass.addOperation(pOperation);
            }
        }
        return pOperation;
    }

    protected IOperation createOperation(
        IClassifier pClass,
        String opName,
        String opType)
    {
        TypedFactoryRetriever < IOperation > factory =
            new TypedFactoryRetriever < IOperation > ();
        IOperation pOperation = factory.createType("Operation");
        try
        {
            if (pOperation != null)
            {
                EventContextManager man = new EventContextManager();
                man.establishVersionableElementContext(
                    pClass,
                    pOperation,
                    null);

                ITransitionElement pTransElement =
                    pOperation instanceof ITransitionElement
                        ? (ITransitionElement) pOperation
                        : null;
                if (pTransElement != null)
                {
                    pTransElement.setFutureOwner(pClass);
                }

                pOperation.setName(opName);
                FactoryRetriever fact = FactoryRetriever.instance();
                IParameter pParam =
                    (IParameter) fact.createType("Parameter", null);
                if (pParam != null)
                {
                    ITransitionElement pParamTransElement =
                        pParam instanceof ITransitionElement
                            ? (ITransitionElement) pParam
                            : null;
                    if (pParamTransElement != null)
                    {
                        pParamTransElement.setFutureOwner(pOperation);
                    }

                    if (opType != null && opType.length() > 0)
                    {
                        pParam.setType2(opType);
                    }
                    pOperation.setReturnType(pParam);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return pOperation;
    }

    public IOperation addParametersToOperation(
        IOperation pOperation,
        ETList < IJRPParameter > parameters)
    {
        IOperation retOper = pOperation;
        if (pOperation != null && parameters != null)
        {
            int size = parameters.size();
            for (int idx = 0; idx < size; idx++)
            {
                IJRPParameter parm = parameters.get(idx);
                retOper = addParameterToOperation(retOper, parm);
            }
        }
        return retOper;
    }

    public IOperation addParameterToOperation(
        IOperation pOperation,
        IJRPParameter parm)
    {
        IOperation retOper = null;
        if (parm != null)
        {
            boolean doAdd = true;
            if (parm != null
                && parm.getDirection() == IParameterDirectionKind.PDK_RESULT)
                doAdd = false;
            IParameter pParm = parm.createParameter(pOperation);
            if (doAdd)
            {
                retOper = addParameterToOperation(pOperation, pParm);
            }
            else
            {
				IMultiplicity mul = pParm.getMultiplicity();
				pOperation.getReturnType().setMultiplicity(mul);
				retOper = pOperation;
            }
        }
        return retOper;
    }

    public IOperation addParameterToOperation(
        IOperation pOperation,
        IParameter pParm)
    {
        if (pOperation != null)
            pOperation.addParameter(pParm);
        return pOperation;
    }

    public boolean isSameClass(IOperation pItem1, IClassifier pItem2)
    {
        boolean retVal = false;
        if (pItem1 != null && pItem2 != null)
        {
            IClassifier pClass = pItem1.getFeaturingClassifier();
            retVal = isSame(pClass, pItem2);
        }
        return retVal;
    }

    public boolean isSameClass(IOperation pItem1, IOperation pItem2)
    {
        boolean retVal = false;
        if (pItem1 != null && pItem2 != null)
        {
            IClassifier pClass = pItem1.getFeaturingClassifier();
            retVal = isSame(pItem2, pClass);
        }
        return retVal;
    }

    public boolean isDependent(IElement pDependent, IElement pIndependent)
    {
        return false;
    }

    /**
     * Methods that are empty and unused as of now.
     */
    public String getOldOperationSig(IChangeRequest pRequest)
    {
        return null;
    }

    public String getParameterTypeName(
        IProject pProject,
        IParameter pParameter)
    {
        return null;
    }

    public IElement copyElement(IElement pOrig, IElement pOwnerOfNew)
    {
        return null;
    }

    public IAttribute copyAttribute(IAttribute pOrig, IClassifier pOwnerOfNew)
    {
        return null;
    }

    public void copyMultiplicity(
        IAttribute pOrig,
        IAttribute pNew) // copies the multiplicity OF the typed element
    {
    }

    public void changeAttributeOfAccessors(
        IAttribute pOldAttr,
        IAttribute pNewAttr)
    {
    }

    public void addParameterToOperation(
        IOperation pOperation,
        String parmType,
        String parmName,
        int direction)
    {
    }

    public String getRelationType(IRelationProxy pRelation)
    {
        return super.getRelationType(pRelation);
    }

    /// automatically discover and build redefinitions between existing operations that match signatures.
    public ETTripleT < ETList < IClassifier >,
        ETList < IOperation >,
        ETList
            < IOperation
                >> buildExistingRedefinitions(
                    IClassifier pBaseClass,
                    IClassifier pDerivedClass)
    {
        return null;
    }

    public ETList
        < IOperation
        > getOperationsByName(IClassifier pClass, String opName)
    {
        return null;
    }

    public ETTripleT < ETList < IClassifier >,
        ETList < IOperation >,
        ETList
            < IOperation
                >> collectRedefiningOps(IClassifier pClass, boolean bExclusive)
    {
        return null;
    }

    public ETList <IClassifier> getImplementingClassifiers(IClassifier pClass)
    {
		ETList <IClassifier> classList = new ETArrayList < IClassifier > ();
		try
		{
			if (pClass == null)
				return classList;

			ETList < IImplementation > impList = null;

			// TODO : REmove this code when the Infrastructure IDL is fixed.
			ETList < IDependency > deps =
				pClass.getSupplierDependenciesByType("Implementation");

			if (deps != null)
			{
				int count = deps.size();
				int idx = 0;
				while (idx < count)
				{
					IDependency pItem = deps.get(idx++);
					if (pItem != null)
					{
						IImplementation pImpl =
							pItem instanceof IImplementation
								? (IImplementation) pItem
								: null;
						if (pImpl != null)
						{
							if (impList == null)
							{
								impList =
									new ETArrayList < IImplementation > ();
							}
							impList.add(pImpl);
						}
					}
				}
			}
			/*		   
				 #else
					   impList = pClass.getImplementations();
				 #endif
			*/

			if (impList != null)
			{
				int count = impList.size();
				int idx = 0;
				while (idx < count)
				{
					IImplementation pItem = impList.get(idx++);
					if (pItem != null)
					{
						// At this point, we don't know if the passed class is the
						// implementee, or the implementor. We only want to get the
						// implementors ( the classes ).

						IClassifier pImplementor =
							pItem.getImplementingClassifier();
						if (pImplementor != null)
						{
							if (!isSame(pClass, pImplementor))
							{
								classList.add(pImplementor);
							}
						}
					}
				}
			}

			// Ok, now, an interface can be generalized, but we want to get all 
			// implementing classes. That means that we have to navigate generalizations
			// down until we get an interface that is implemented.

			ETList < IClassifier > subInterfaces = getSpecializations(pClass);

			// Now, for each of these interfaces, recursively call this routine.
			if (subInterfaces != null)
			{
				int count = subInterfaces.size();
				int idx = 0;
				while (idx < count)
				{
					IClassifier pItem = subInterfaces.get(idx++);

					if (pItem != null)
					{
						classList =
							getImplementingClassifiers(pItem, classList);
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return classList;
    }

    public ETList
        < IClassifier
        > addClassifier(IClassifier pItem, ETList < IClassifier > classes)
    {
        return null;
    }

    public ETList
        < IOperation
        > addOperation(IOperation pItem, ETList < IOperation > ops)
    {
        return null;
    }

    public ETList
        < IElement
        > addElement(IElement pItem, ETList < IElement > elems)
    {
        return null;
    }

    public ETList
        < IAssociationEnd
        > addAssociationEnd(
            IAssociationEnd pItem,
            ETList < IAssociationEnd > ends)
    {
        return null;
    }

    public List findAttrsForReadAccessor(IOperation oper, IClassifier pClass) {
        List result = new ArrayList();
        String opName = oper.getName();
        String prefix = readAccessorPrefix();
        if (opName == null || !opName.startsWith(prefix)) {
            return result;
        }
        ETList<IParameter> params = oper.getFormalParameters();
        if (params.size() != 0) {
            return result;
        }
        
        String attrName_1 = opName.substring(prefix.length());
        if (attrName_1.length() == 0) {
            return result;
        }
        String attrName_2 = Character.toLowerCase(attrName_1.charAt(0)) + attrName_1.substring(1);
        
        IParameter retType = oper.getReturnType();
        String retTypeName = retType.getTypeName();
        IMultiplicity mult = retType.getMultiplicity();
        long rangeCount = mult != null ? mult.getRangeCount() : 0;

        IAttribute attr = pClass.getAttributeByName(attrName_1);
        if (attr != null && retTypeName.equals(attr.getTypeName())) {
            IMultiplicity aMult = attr.getMultiplicity();
            long aRangeCount = aMult != null ? aMult.getRangeCount() : 0;
            if (aRangeCount == rangeCount) {
                result.add(attr);
            }
        }
        attr = pClass.getAttributeByName(attrName_2);
        if (attr != null && retTypeName.equals(attr.getTypeName())) {
            IMultiplicity aMult = attr.getMultiplicity();
            long aRangeCount = aMult != null ? aMult.getRangeCount() : 0;
            if (aRangeCount == rangeCount) {
                result.add(attr);
            }
        }
        return result;
    }
    
    public List findAttrsForWriteAccessor(IOperation oper, IClassifier pClass) {
        List result = new ArrayList();
        String opName = oper.getName();
        String prefix = writeAccessorPrefix();
        if (opName == null || !opName.startsWith(prefix)) {
            return result;
        }
        String retType = getWriteAccessorReturnType();
        if (!retType.equals(oper.getReturnType2())) {
            return result;
        }
        ETList<IParameter> params = oper.getFormalParameters();
        if (params.size() != 1) {
            return result;
        }
        
        String attrName_1 = opName.substring(prefix.length());
        if (attrName_1.length() == 0) {
            return result;
        }
        String attrName_2 = Character.toLowerCase(attrName_1.charAt(0)) + attrName_1.substring(1);
        IParameter par = params.get(0);
        String typeName = par.getTypeName();
        IMultiplicity mult = par.getMultiplicity();
        long rangeCount = mult != null ? mult.getRangeCount() : 0;

        IAttribute attr = pClass.getAttributeByName(attrName_1);
        if (attr != null && typeName.equals(attr.getTypeName())) {
            IMultiplicity aMult = attr.getMultiplicity();
            long aRangeCount = aMult != null ? aMult.getRangeCount() : 0;
            if (aRangeCount == rangeCount) {
                result.add(attr);
            }
        }
        attr = pClass.getAttributeByName(attrName_2);
        if (attr != null && typeName.equals(attr.getTypeName())) {
            IMultiplicity aMult = attr.getMultiplicity();
            long aRangeCount = aMult != null ? aMult.getRangeCount() : 0;
            if (aRangeCount == rangeCount) {
                result.add(attr);
            }
        }
        return result;
    }
    
    public boolean isSameClass(IAttribute pItem1, IClassifier pItem2)
    {
        return false;
    }

    public boolean isSameClass(IAttribute pItem1, IAttribute pItem2)
    {
        return false;
    }

    //	retval = lh < rh
    public boolean isVisibilityLess(int lh, int rh)
    {
        return false;
    }

    // MOVE and DUPE fuctions

    public void moveToClass3(IAttribute pItem, IClassifier pClass)
    {
    }

    public void moveToClass4(ETList < IAttribute > list, IClassifier pClass)
    {
    }

    // This routine is used when a generalization or implementation is created.
    // It looks in the two classes for operations that match signature. Because 
    // we want to keep the functions as generic as possible, we DON'T really
    // want to build the redefinition here. But we cannot just return a single
    // operation, since we want to keep the pairs together. So, I really need
    // to create a typedef pair and a vector of those pair. Until then, I just
    // return a list whose length should always be even, where the first operation
    // is the base op, and the second is the redefining op.

    // Here is what the following routines mean.
    // Given two sets:
    // Set1 = {A,B,C}
    // Set2 = {B,C,D}
    // SetU = Union(Set1,Set2) = {A,B,C,D}
    // SetI = Intersect(Set1,Set2) = {B,C}
    // SetS1 = Subtract(Set1,Set2) = {A}
    // SetS2 = Subtract(Set2,Set1) = {D}
    // SetD  = Difference(Set1,Set2) = {A,D} 
    // notice that D(S1,S2) = U(SetS1,SetS2) = Sub(SetU,SetI)

    public ETList
        < IElement
        > elementListUnion(ETList < IElement > list1, ETList < IElement > list2)
    {
        return null;
    }

    public ETList
        < IElement
        > elementListIntersect(
            ETList < IElement > list1,
            ETList < IElement > list2)
    {
        return null;
    }

    public ETList
        < IElement
        > elementListDifference(
            ETList < IElement > list1,
            ETList < IElement > list2)
    {
        return null;
    }

    public ETList
        < IClassifier
        > elementListUnion2(
            ETList < IClassifier > list1,
            ETList < IClassifier > list2)
    {
        return null;
    }

    public ETList
        < IClassifier
        > elementListIntersect2(
            ETList < IClassifier > list1,
            ETList < IClassifier > list2)
    {
        return null;
    }

    public ETList
        < IClassifier
        > elementListSubtract2(
            ETList < IClassifier > list1,
            ETList < IClassifier > list2)
    {
        return null;
    }

    public ETList
        < IClassifier
        > elementListDifference2(
            ETList < IClassifier > list1,
            ETList < IClassifier > list2)
    {
        return null;
    }

    public ETList
        < IOperation
        > elementListUnion3(
            ETList < IOperation > list1,
            ETList < IOperation > list2)
    {
        return null;
    }

    public ETList
        < IOperation
        > elementListIntersect3(
            ETList < IOperation > list1,
            ETList < IOperation > list2)
    {
        return null;
    }

    public ETList
        < IOperation
        > elementListSubtract3(
            ETList < IOperation > list1,
            ETList < IOperation > list2)
    {
        return null;
    }

    public ETList
        < IOperation
        > elementListDifference3(
            ETList < IOperation > list1,
            ETList < IOperation > list2)
    {
        return null;
    }

    public ETList
        < IElement
        > operationsToElements(ETList < IOperation > inList)
    {
        return null;
    }

    public ETList
        < IOperation
        > elementsToOperations(ETList < IElement > inList)
    {
        return null;
    }

    // List membership functions

    public boolean isMember2(
        IOperation pItem,
        ETList < IRedefinableElement > pList)
    {
        return false;
    }

    public String attributePrefix(String prefix)
    {
        return null;
    }

    public String getPreferenceKey()
    {
        return "Default";
    }

    public String getPreferencePath()
    {
        return "RoundTrip|Java";
    }

    public String getLanguage()
    {
        return null;
    }

    public ETPairT < ETList < IElement >,
        ETList < IElement >> getAssociatedArtifacts(IElement pElement)
    {
        return null;
    }

    public String getJavaSourceFile(IArtifact pArtifact, String fileName)
    {
        return null;
    }

    public boolean isTemplateClass(IChangeRequest pRequest)
    {
        return false;
    }

    public ETList < IClassifier > add(IClassifier pItem)
    {
        return null;
    }
    
    //Jyothi: Fix for Bug#6327840
    public boolean doesGetterExist(IAttribute pAttr, IClassifier pClass) {
        boolean exists = false;
        if (pAttr != null && pClass != null) {
            try {
                String attrName = stringFixer(pAttr.getName());
                IClassifier retType = getType(pAttr);
                
                String typeName = null;
                if (retType != null)
                    typeName = stringFixer(retType.getFullyQualifiedName(false));
                
                if (attrName != null && attrName.trim().length() > 0 && typeName != null && typeName.trim().length() > 0) {
                    String opName = readAccessorPrefix();
                    String attrNameFix =  removePrefixFromAttributeName(attrName);
                    String attrNameCap = capAttributeName(attrNameFix);
                    if (opName != null && attrNameCap != null)
                        opName += attrNameCap;
                    
                    //create  a new operation (DO NOT ADD to the class)
                    IOperation myOper = pClass.createOperation2(retType, opName);                    
                    if (this.isOperationAlreadyRedefined(myOper, pClass)) {
                        exists = true;
                        return exists;
                    }
                    
                    ETList <IOperation> operList = pClass.getOperations();
                    if (operList !=  null) {
                        for (int i=0; i< operList.size(); i++) {    
                            IOperation tempOper = operList.get(i);                           
                            if (myOper.isSignatureSame(tempOper)) {
                                exists = true;
                                
                                IDependency pDep = createRealization(pAttr, tempOper, pClass);
                                return exists;
                            } else {
                                //do nothing.. just continue..
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return exists;
    }
    
    //Jyothi: Fix for Bug#6327840
    public boolean doesSetterExist(IAttribute pAttr, IClassifier pClass) {
        boolean exists = false;
        if (pAttr != null && pClass != null) {
            try {
                String attrName = stringFixer(pAttr.getName());
                String retType = getWriteAccessorReturnType();
                IClassifier parmType = getType(pAttr);
                
                if (attrName != null && attrName.length() > 0) // TODO : check against default
                {
                    String opName = writeAccessorPrefix();
                    String attrNameFix =  removePrefixFromAttributeName(attrName);
                    String attrNameCap = capAttributeName(attrNameFix);
                    if (opName != null && attrNameCap != null)
                        opName += attrNameCap;
                    
                    ETList < IJRPParameter > parms =
                            new ETArrayList < IJRPParameter > ();
                    String parmname = "val";
                    JRPParameter parm =
                            new JRPParameter(
                            parmname,
                            parmType,
                            IParameterDirectionKind.PDK_IN);
                    
                    IMultiplicity pMult = pAttr.getMultiplicity();
                    if (pMult != null) {
                        long rangeCount = pMult.getRangeCount();
                        while (rangeCount > 0) {
                            parm.addRange("0", "*");
                            rangeCount--;
                        }
                    }
                    parms.add(parm);

                    IOperation myOper = pClass.createOperation(retType, opName);
                   
                    //set params for the operation.. to do this we need to convert JRPParams to IParams..
                    ETList <IParameter> iParamsList = new ETArrayList<IParameter>();
                    for (int i=0; i<parms.size(); i++) {
                        IJRPParameter jrpParam = parms.get(i);
                        IParameter iParam = jrpParam.createParameter(myOper);
                        myOper.addParameter(iParam);
                    }
                    
                    //Now that the oper is ready, check if the operation is already re-defined
                    if (this.isOperationAlreadyRedefined(myOper, pClass)) {
                        exists = true;
                        return exists;
                    }

                    //Now get the list of all operations of the class..
                    ETList <IOperation> operList = pClass.getOperations();
                    if (operList !=  null) {
                        for (int i=0; i< operList.size(); i++) {
                            IOperation tempOper = operList.get(i);
                            
                            if (myOper.isSignatureSame(tempOper)) {
                                exists = true;
                                IDependency pDep =  createRealization(pAttr, tempOper, pClass);
                                return exists;
                            } else {
                                //do nothing..just continue..
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return exists;
    }
}
