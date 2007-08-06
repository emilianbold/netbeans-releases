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


package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import java.util.Iterator;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.ICoreMessenger;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IParameterDirectionKind;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationSignatureChangeContextManager;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.OperationSignatureChangeContextManager;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.roundtripframework.ChangeKind;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IElementContextPayload;
import org.netbeans.modules.uml.core.roundtripframework.IMultipleParameterTypeChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IOperationSignatureChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IParameterTypeChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRequestProcessor;
import org.netbeans.modules.uml.core.roundtripframework.MultipleParameterTypeChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.RTElementKind;
import org.netbeans.modules.uml.core.roundtripframework.RequestDetailKind;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.ui.support.ErrorDialogIconKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import java.util.HashSet;
import java.util.List;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 */
public class JavaMethodChangeHandler extends JavaChangeHandler implements IJavaMethodChangeHandler
{
    private static boolean diagRetval = true;
    private static boolean doRedef =false;
    private static boolean isRelated = false;
    public JavaMethodChangeHandler()
    {
        super();
    }
    
    public JavaMethodChangeHandler(IJavaChangeHandler copy)
    {
        super(copy);
    }
    
    public void handleRequest(IRequestValidator requestValidator)
    {
        if (requestValidator != null && requestValidator.getValid())
        {
            if (m_Utilities != null)
            {
                RequestDetails details = m_Utilities.getRequestDetails(
                        requestValidator.getRequest());
                if(details != null)
                {
                    int cType = details.changeKind;
                    int cDetail = details.requestDetailKind;
                    int eType = details.rtElementKind;
                    
                    if (eType == RTElementKind.RCT_OPERATION ||
                            eType == RTElementKind.RCT_PARAMETER)
                    {
                        if (eType == RTElementKind.RCT_OPERATION )
                        {
                            added(requestValidator, cType, cDetail);
                            deleted(requestValidator, cType, cDetail);
                            visibilityChange(requestValidator, cType, cDetail);
                            abstractChange(requestValidator, cType, cDetail);
                            nameChange(requestValidator, cType, cDetail);
                            moved(requestValidator, cType, cDetail);
                            signatureChange(requestValidator, cType, cDetail);
                        }
                        else
                        {
                            parameterTypeChange(requestValidator, cType, cDetail);
                            parameterAdded(requestValidator, cType, cDetail);
                            parameterDeleted(requestValidator, cType, cDetail);
                            parameterChange(requestValidator, cType, cDetail);
                            typeChange(requestValidator, cType, cDetail);
                        }
                    }
                    else if(cDetail == RequestDetailKind.RDT_FEATURE_DUPLICATED )
                    {
                        duplicated(requestValidator, cType, cDetail);
                    }
                }
            }
        }
    }
    
    public void transformToEnumeration(IRequestValidator requestValidator,
            IOperation pOperation,
            IClassifier pClass,
            boolean doAbstractChange)
    {
        transformToClass(requestValidator, pOperation, pClass, doAbstractChange);
    }
    
    public void transformToClass(IRequestValidator requestValidator, IOperation pOperation,
            IClassifier pClass, boolean doAbstractChange)
    {
        if (pOperation != null)
        {
            IClassifier pLocalClass = pClass;
            if (pLocalClass == null)
            {
                pLocalClass = pOperation.getFeaturingClassifier();
            }
            
            // Note: We used to make this check by QI pLocalClass
            //       to see if it supported the IInterface, but that
            //       is not sufficient 'cause other types could support
            //       the IInterface interface but should not be treated
            //       as interfaces, such as the IPartFacade.
            String elementType = pLocalClass.getElementType();
            //			if (elementType != null && elementType.equals("Interface"))
            //Fix for the bug # 6176196
            if (elementType != null && elementType.equals("Class"))
            {
                boolean isAbstract = false;
                boolean currentAbstract = pOperation.getIsAbstract();
                if ( currentAbstract != isAbstract )
                {
                    pOperation.setIsAbstract(isAbstract);
                    if (doAbstractChange)
                    {
                        IInterface pInter = pClass instanceof IInterface? (IInterface) pClass : null;
                        abstractChange(requestValidator,pOperation,pInter);
                    }
                }
            }
        }
    }
    
    
    protected void added( IRequestValidator requestValidator, int cType, int cDetail )
    {
        if ( requestValidator != null && requestValidator.getValid() && m_Utilities != null)
        {
            if ( cType == ChangeKind.CT_CREATE )
            {
                IOperation pOperation = null;
                IClassifier pClass = null;
                ETPairT<IOperation, IClassifier> operClass =
                        m_Utilities.getOperationAndClass(requestValidator.getRequest(),false);
                if (operClass != null)
                {
                    pOperation = operClass.getParamOne();
                    pClass = operClass.getParamTwo();
                }
                
                boolean valid = true;
                if (pOperation != null)
                {
                    // If the operation is still untyped at this point, invalidate
                    // the event so that no one receives it.
                    String opName = pOperation.getName();
                    String unnamedStr = PreferenceAccessor.instance().getDefaultElementName();
                    if (opName == null || opName.length() == 0 || opName.equals(unnamedStr))
                    {
                        valid = false;
                    }
                    else
                    {
                        added(requestValidator, pOperation, pClass);
                    }
                    requestValidator.setValid(valid);
                }
            }
        }
    }
    
    public void added(IRequestValidator requestValidator, IOperation pOperation, IClassifier pClass)
    {
        if (pOperation == null || pClass == null)
            return ;
        
        // Make sure to import anything that is needed.
        addDependency( requestValidator, pOperation, pClass );
        
        // What we must do is discover redefinition in both directions.
        // The very first thing we want to do is make sure that the operation
        // does not already exist on the class.
        enforceUniqueness(pOperation, pClass );
        
        //Executes the redefinition up and down
        discoverRedefinitions(pOperation,pClass);
        checkIfConstructor(pOperation, pClass);
        // 78868, check implementing classifier instead of element type, e.g.
        // interface in design pattern has partfacade_interface type
        if (m_Utilities.getImplementingClassifiers(pClass,null).size() > 0)
        {
            boolean noAbstractProcessing = false; // This means "no abstract processing". Just a variable for readability.
            transformToInterface( requestValidator, pOperation, pClass, noAbstractProcessing );
            addToImplementingClasses( requestValidator, pOperation, pClass );
        }
        else
        {
            // if the operation is in a normal class, and is created abstract,
            // we want to do the abstrac change stuff, BUT ONLY if we KNOW
            // is abstract. (we don't want to do the abstract false stuff,
            // because this is not an "abstract modified" event.
            boolean isAbstract = false;
            isAbstract = pOperation.getIsAbstract();
            // if the operation is becoming abstract, we want to do all of the
            // AbstractModified stuff in this case.
            if (isAbstract)
                abstractChange(requestValidator, pOperation, pClass);
        }
        
        // check whether the added method is an accessor method for some attribute
        boolean found = false;
        List attrs = m_Utilities.findAttrsForWriteAccessor(pOperation, pClass);
        for (Iterator iter = attrs.iterator(); iter.hasNext(); )
        {
            IAttribute attr = (IAttribute) iter.next();
            List writeAccessors = null;
            ETPairT<ETList<IOperation>, ETList<IDependency>> writePair = m_Utilities.getWriteAccessorsOfAttribute(attr, pClass);
            if (writePair != null)
            {
                writeAccessors = writePair.getParamOne();
            }
            if (writeAccessors == null || writeAccessors.size() == 0)
            {
                m_Utilities.createRealization(pOperation, attr, pClass);
                found = true;
                break;
            }
        }
        if (!found)
        {
            attrs = m_Utilities.findAttrsForReadAccessor(pOperation, pClass);
            for (Iterator iter = attrs.iterator(); iter.hasNext(); )
            {
                IAttribute attr = (IAttribute) iter.next();
                List readAccessors = null;
                ETPairT<ETList<IOperation>, ETList<IDependency>> readPair = m_Utilities.getReadAccessorsOfAttribute(attr, pClass);
                if (readPair != null)
                {
                    readAccessors = readPair.getParamOne();
                }
                if (readAccessors == null || readAccessors.size() == 0)
                {
                    m_Utilities.createRealization(attr, pOperation, pClass);
                    break;
                }
            }
        }
    }
    
    protected void checkIfConstructor(IOperation pOperation, IClassifier pClassifier)
    {
        IClass pClass = null;
        if (pClassifier != null && pClassifier instanceof IClass)
        {
            pClass = (IClass)pClassifier;
        }
        if (pClass != null && pOperation != null)
        {
            String clsName = pClass.getName();
            String opName = pOperation.getName();
            // 107427
            if (clsName != null && clsName.equals(opName) &&
                    (pOperation.getReturnType() == null ||
                    pOperation.getReturnType().getTypeName().equals("")))
            {
                pOperation.setIsConstructor(true);
            }
        }
    }
    
    protected ETPairT<ETList<IOperation>, Boolean>
            discoverRedefinitionOnClass(IOperation pOperation, IClassifier pClass, ETList<IOperation> pIdenticalOperations)
    {
        boolean keepLooking = true;
        ETList<IOperation> classesOps = null;
        if (pClass != null)
            classesOps = pClass.getOperations();
        IOperation pOpOnClassWithSameSig = m_Utilities.discoverRedefinition(pOperation, classesOps);
        
        // first if we found any, query the user if this is what they wanted to do.
        if (pOpOnClassWithSameSig != null)
        {
            
            
            boolean doRedefinition = queryUserBeforeRedefinition(pOperation);
            doRedef = doRedefinition;
            // Make sure that we stop all further searches in the current direction.
            // Notice that this is independent of the user's answer to the query.
            // Basically, if we found one, we are done.
            keepLooking = false;
            if ( doRedefinition )
            {
                // we found operations on the class that match. This is
                // the list we want to return.
                ETList<IOperation> tempClassesOps = new ETArrayList<IOperation>();
                tempClassesOps.add(pOpOnClassWithSameSig);
                
                pIdenticalOperations = m_Utilities.appendOperationsToList(tempClassesOps, pIdenticalOperations);
            }
        }
        return new ETPairT<ETList<IOperation>, Boolean> (pIdenticalOperations,  new Boolean(keepLooking));
        
    }
    
    /**
     * Discovers the operations on pClass that are the same as pOperation. If not
     * found on pClass, all generalizations and implementations are followed. Because
     * multiple interfaces my be implemented that have the same operation, the result
     * is a list, not a single operation
     *
     * @param pOperation
     * @param pClass
     * @param pIdenticalOperation
     */
    protected ETList<IOperation> discoverRedefinitionUp(IOperation pOperation, IClassifier pClass,
            ETList<IOperation> pIdenticalOperations, boolean byPassClass)
    {
        boolean doRedefinition = true;
        if (!byPassClass)
        {
            ETPairT<ETList<IOperation>, Boolean> opBoolPair =
                    discoverRedefinitionOnClass( pOperation, pClass, pIdenticalOperations );
            
            if (opBoolPair != null)
            {
                pIdenticalOperations = opBoolPair.getParamOne();
                Boolean doRedefine = opBoolPair.getParamTwo();
                if (doRedefine != null)
                    doRedefinition = doRedefine.booleanValue();
            }
        }
        // If there were ops on this class with the same sig (really only 1) we are done.
        // If not we want to follow ALL branches up until we find one on EACH branch
        if ( doRedefinition )
        {
            // Get all classes UP the chain, looking for redefinitions and
            // concantenate the lists.
            ETList<IClassifier> implementedInterfaces = m_Utilities.getImplementedInterfaces(pClass);
            pIdenticalOperations = discoverRedefinitionUp(pOperation, implementedInterfaces, pIdenticalOperations);
            
            ETList<IClassifier> superClasses = m_Utilities.getGeneralizations(pClass);
            pIdenticalOperations = discoverRedefinitionUp(pOperation, superClasses, pIdenticalOperations);
            if(superClasses.getCount()>0 || implementedInterfaces.getCount()>0) isRelated = true;
        }
        return pIdenticalOperations;
    }
    
    protected ETList<IOperation> discoverRedefinitionUp(IOperation pOperation,
            ETList<IClassifier> pClasses,
            ETList<IOperation> pIdenticalOperations)
    {
        if (pClasses != null)
        {
            Iterator<IClassifier> iter = pClasses.iterator();
            boolean doRedefinition = true;
            if (iter != null && doRedefinition)
            {
                ETList<IOperation> opsWithSameSig = new ETArrayList<IOperation>();
                while (iter.hasNext())
                {
                    IClassifier pItem = iter.next();
                    if (pItem != null)
                    {
                        opsWithSameSig = discoverRedefinitionUp(pOperation, pItem, opsWithSameSig, false);
                        // We query evertime we find one. But, the query should be set
                        // as a once only, so no harm. This just makes sure that we
                        // stop at the first opportunity if the user says no.
                        if (opsWithSameSig != null && opsWithSameSig.size() > 0)
                        {
                            doRedefinition = queryUserBeforeRedefinition(pOperation);
                            if (doRedefinition)
                            {
                                // append the found list to the return list.
                                m_Utilities.appendOperationsToList(opsWithSameSig, pIdenticalOperations);
                            }
                        }
                    }
                }
            }
        }
        return pIdenticalOperations;
    }
    
    /**
     *
     * Discovers the operations on pClass that are the same as pOperation. If not
     * found on pClass, all generalizations and implementations are followed. Because
     * multiple interfaces my be implemented that have the same operation, the result
     * is a list, not a single operation
     *
     * @param pOperation
     * @param pClass
     * @param pIdenticalOperation
     */
    protected ETList<IOperation> discoverRedefinitionDown(IOperation pOperation, IClassifier pClass,
            ETList<IOperation> pIdenticalOperations, boolean byPassClass)
    {
        boolean doRedefinition = true;
        if (!byPassClass)
        {
            ETPairT<ETList<IOperation>, Boolean> opBoolPair =
                    discoverRedefinitionOnClass( pOperation, pClass, pIdenticalOperations );
            
            if (opBoolPair != null)
            {
                pIdenticalOperations = opBoolPair.getParamOne();
                Boolean doRedefine = opBoolPair.getParamTwo();
                if (doRedefine != null)
                    doRedefinition = doRedefine.booleanValue();
            }
        }
        // If there were ops on this class with the same sig (really only 1) we are done.
        // If not we want to follow ALL branches up until we find one on EACH branch
        if ( doRedefinition )
        {
            // Get all classes UP the chain, looking for redefinitions and
            // concantenate the lists.
            ETList<IClassifier> implementingClasses = null;
            implementingClasses = m_Utilities.getImplementingClassifiers(pClass, implementingClasses);
            pIdenticalOperations = discoverRedefinitionDown(pOperation, implementingClasses, pIdenticalOperations);
            
            // We have handled all implemented interfaces. Now handle the generalizations.
            ETList<IClassifier> subClasses = m_Utilities.getSpecializations(pClass);
            pIdenticalOperations = discoverRedefinitionDown(pOperation, subClasses, pIdenticalOperations);
            if(subClasses.getCount()>0 || implementingClasses.getCount()>0)isRelated = true;
        }
        return pIdenticalOperations;
    }
    
    protected ETList<IOperation> discoverRedefinitionDown(IOperation pOperation,
            ETList<IClassifier> pClasses,
            ETList<IOperation> pIdenticalOperations)
    {
        if (pClasses != null)
        {
            Iterator<IClassifier> iter = pClasses.iterator();
            boolean doRedefinition = true;
            if (iter != null && doRedefinition)
            {
                ETList<IOperation> opsWithSameSig = new ETArrayList<IOperation>();
                while (iter.hasNext())
                {
                    IClassifier pItem = iter.next();
                    if (pItem != null)
                    {
                        opsWithSameSig = discoverRedefinitionDown(pOperation, pItem, opsWithSameSig, false);
                        // We query evertime we find one. But, the query should be set
                        // as a once only, so no harm. This just makes sure that we
                        // stop at the first opportunity if the user says no.
                        if (opsWithSameSig != null && opsWithSameSig.size() > 0)
                        {
                            doRedefinition = queryUserBeforeRedefinition(pOperation);
                            if (doRedefinition)
                            {
                                // append the found list to the return list.
                                m_Utilities.appendOperationsToList(opsWithSameSig, pIdenticalOperations);
                            }
                        }
                    }
                }
            }
        }
        return pIdenticalOperations;
    }
    
    protected void addToImplementingClasses(IRequestValidator requestValidator,IOperation pOperation,
            IClassifier pInterface )
    {
        ETList<IClassifier> implementingClasses = null;
        
        if (pInterface != null)
            implementingClasses = m_Utilities.getImplementingClassifiers(pInterface,implementingClasses);
        
        // Now, we have to be careful not to add it to a class that already owns
        // a redefinition of this operation.
        ETList<IOperation> redefines =  null;
        if (pOperation != null)
            redefines = m_Utilities.collectRedefiningOps(pOperation);
        
        // We search the existing redefines against the class. We don't
        // get the classes list of operations and see if the operation is
        // redefined by one of them. This is because we make the assumption
        // that the list of existing redefines is small, but the list of
        // operations on a class may be large.
        //
        // What we really need is a way to color elements. Here is why:
        // If we got the list of classes corresponding to the list of
        // redefines we just got, we could color those classes. Then we
        // could go over the implementingClasses list and select only
        // the classes that aren't colored. This reduces the search to order-N.
        // Of course, coloring comes with its own problems ( having to reset,
        // being non-reentrant, etc.) So, we may eventually have to revisit all
        // of the utility routines to use vectors and such instead of com lists.
        ETList<IClassifier> redefiningClasses = null;
        if (redefines != null)
        {
            Iterator<IOperation> iter = redefines.iterator();
            if (iter != null)
            {
                while (iter.hasNext())
                {
                    IOperation pItem = iter.next();
                    IClassifier pItemClass = pItem.getFeaturingClassifier();
                    if (pItemClass != null)
                    {
                        redefiningClasses = m_Utilities.add(pItemClass, redefiningClasses);
                    }
                }
            }
        }
        // We want all the implementing classes that are NOT on the redefining
        // classes list.
        
        //AZTEC: do it now.
        ETList<IElement> implementingElem =
                m_Utilities.classesToElements(implementingClasses);
        ETList<IElement> redefiningElem =
                m_Utilities.classesToElements(redefiningClasses);
        ETList<IElement> nonRedefiningElem =
                m_Utilities.elementListSubtract(implementingElem,redefiningElem);
        ETList<IClassifier> nonRedefiningClasses =
                m_Utilities.elementsToClasses(nonRedefiningElem);
        
        // Now, just add a redefining operation to each class on the final list.
        if (nonRedefiningClasses != null)
        {
            Iterator<IClassifier> iter = nonRedefiningClasses.iterator();
            if (iter != null)
            {
                while (iter.hasNext())
                {
                    IClassifier pItem = iter.next();
                    if (pItem != null)
                    {
                        IOperation pNewOp = m_Utilities.copyOperation(pOperation, pItem);
                        if (pNewOp != null)
                        {
                            // Make sure we set this new one as non-abstract
                            pNewOp.setIsAbstract(false);
                            m_Utilities.addRedefiningOperation(requestValidator, pOperation, pNewOp, pItem);
                        }
                    }
                }
            }
        }
    }
    
    protected boolean queryUserBeforeRedefinition(IOperation pOperation)
    {
        String format = m_Utilities.formatOperation(pOperation);
        return doQuery("REDEFINE",format);
    }
    
    /**
     *
     * Ensures that the operation is unique in the class.
     */
    protected void enforceUniqueness(IOperation pOperation, IClassifier pClass )
    {
        try
        {
            if (pOperation != null && pClass != null )
            {
                boolean done = false;
                while ( !done )
                {
                    ETList<IOperation>  classOps = pClass.getOperations();
                    IOperation  pDiscoveredOp = m_Utilities.discoverRedefinition
                            (pOperation, classOps);
                    if ( pDiscoveredOp == null )
                    {
                        done = true;
                    }
                    else
                    {
                        PreferenceControlledInputDialog  pInput =
                                new PreferenceControlledInputDialog();
                        if ( pInput != null )
                        {
                            String opFormat =
                                    m_Utilities.formatOperation(pOperation);
                            String opName = pOperation.getName();
                            // Make a new unique name for the operation
                            StringBuffer newOpName = new StringBuffer(opName);
                            boolean isUnique = false;
                            int  nextName = 0;
                            if ( classOps != null )
                            {
                                int count = classOps.getCount();
                                int idx = 0;
                                while ( !isUnique )
                                {
                                    isUnique = true;
                                    idx = 0;
                                    nextName++;
                                    newOpName.append(nextName );
                                    while ( idx < count && isUnique )
                                    {
                                        IOperation  pItem = classOps.get(idx++);
                                        if ( pItem != null )
                                        {
                                            String itemName = pItem.getName();
                                            if ( itemName.trim().equals
                                                    (newOpName.toString().trim()))
                                            {
                                                isUnique = false;
                                            }
                                        }
                                    }
                                }
                            }
                            String message = RPMessages.getString
                                    ("IDS_JRT_UNIQUE_OP_MESSAGE", new String[]{opFormat});
                            
                            String prefKey = m_Utilities.getPreferenceKey();
                            String prefPath = m_Utilities.getPreferencePath();
                            String prefName = "UML_SHOW_DUPE_OP_DIALOG";
                            
                            pInput.setEditText( newOpName.toString() );
                            
                            int nDefaultResult
                                    = SimpleQuestionDialogResultKind.SQDRK_RESULT_OK;
                            int nDialogType =
                                    SimpleQuestionDialogKind.SQDK_OK;
                            int nDialogIcon =
                                    ErrorDialogIconKind.EDIK_ICONEXCLAMATION;
                            int nResult = nDefaultResult;
                            String sTitle = RPMessages.getString
                                    ("IDS_JRT_DUPLICATE_OPERATION_TITLE");
                            
                            pInput.displayFromStrings
                                    (prefKey,  prefPath, prefName,
                                    "PSK_NO", "PSK_NO", "PSK_YES",
                                    message, nDefaultResult, nResult,
                                    sTitle , nDialogType, nDialogIcon,
                                    0);
                            
                            final String dialogOpName = pInput.getEditText();
                            // pOperation.setName(dialogOpName);
                            
                            // workaround for #6273664
                            HashSet opNames = new HashSet();
                            for (Iterator iter = classOps.iterator(); iter.hasNext(); )
                            {
                                opNames.add(((IOperation)iter.next()).getName());
                            }
                            
                            // generate temporal name that does not clash with any name of an operator in the classifier
                            StringBuffer tempNameBuf = new StringBuffer(dialogOpName);
                            do
                            {
                                tempNameBuf.append('_');
                            } while (opNames.contains(tempNameBuf.toString()));
                            final String tempName = tempNameBuf.toString();
                            final IOperation op = pOperation;
                            
                            // no additional change request will be generated if the name is set on the "node level"
                            XMLManip.setAttributeValue(pOperation.getNode(), "name", dialogOpName); // NOI18N
                            
                            RequestProcessor.getDefault().post(new Runnable()
                            {
                                public void run()
                                {
                                    // set name of the operation to the temporal name and back
                                    // this will refresh name in the diagram (is there a more strighforward way how to di it ?)
                                    op.setName(tempName);
                                    op.setName(dialogOpName);
                                }
                            });
                            // end of workaround
                        }
                    }
                }
            }
        }
        catch( Exception e )
        {
            Log.stackTrace(e);
        }
    }
    
    /**
     *
     * If the operations class is an interface, make it conform
     *
     * @param pOperation[in]
     * @param pClass[in]
     * @param doAbstractChange[in] if true, we will do AbstractChange processing. Default it TRUE
     */
    public void transformToInterface(IRequestValidator requestValidator, IOperation pOperation,
            IClassifier pClass, boolean doAbstractChange)
    {
        if (pOperation != null)
        {
            IClassifier pLocalClass = pClass;
            if (pLocalClass == null)
                pLocalClass = pOperation.getFeaturingClassifier();
            
            // If the class is an interface, the operation
            // MUST BE nonstatic, nonfinal, and public, and abstract
            //
            // Note: We used to make this check by QI pLocalClass
            //       to see if it supported the IInterface, but that
            //       is not sufficient 'cause other types could support
            //       the IInterface interface but should not be treated
            //       as interfaces, such as the IPartFacade.
            
            String elementType = pLocalClass.getElementType();
            if ( elementType.equals("Interface"))
            {
                boolean isFinal = false;
                boolean isStatic = false;
                boolean isAbstract = true;
                int vis = IVisibilityKind.VK_PUBLIC;
                
                pOperation.setIsFinal(isFinal);
                pOperation.setIsStatic(isStatic);
                pOperation.setVisibility(vis);
                
                boolean currentAbstract = false;
                currentAbstract = pOperation.getIsAbstract();
                if ( currentAbstract != isAbstract )
                {
                    pOperation.setIsAbstract(isAbstract);
                    if (doAbstractChange)
                    {
                        IInterface pInter = pLocalClass instanceof IInterface? (IInterface) pLocalClass : null;
                        abstractChange(requestValidator, pOperation, pInter);
                    }
                }
            }
            
        }
    }
    
    protected void parameterTypeChange(IRequestValidator requestValidator, int cType, int cDetail )
    {
        if (requestValidator != null && requestValidator.getValid())
        {
            if ( cType == ChangeKind.CT_MODIFY && cDetail == RequestDetailKind.RDT_TYPE_MODIFIED);
            {
                IParameterTypeChangeRequest pTypeReq =
                        requestValidator instanceof IParameterTypeChangeRequest?
                            (IParameterTypeChangeRequest)requestValidator.getRequest()
                            : null;
                if (pTypeReq != null)
                {
                    IParameter pParm = pTypeReq.getImpactedParameter();
                    if ( pParm != null )
                    {
                        // Do what ever needs to be done.
                        //AZTEC: comment. this method call does not do anything.
                        // so commenting out.
                        //ParameterTypeChange ( requestValidator, pParm )
                        IBehavioralFeature pFeat = pParm.getBehavioralFeature();
                        if (pFeat != null)
                        {
                            IOperation pOp = pFeat instanceof IOperation? (IOperation) pFeat : null;
                            if (pOp != null)
                            {
                                IChangeRequest pNewRequest = m_Utilities.createChangeRequest(
                                        MultipleParameterTypeChangeRequest.class,
                                        ChangeKind.CT_MODIFY,
                                        RequestDetailKind.RDT_MULTIPLE_PARAMETER_TYPE_MODIFIED,
                                        pOp,
                                        pOp,
                                        pOp);
                                if (pNewRequest != null)
                                {
                                    IMultipleParameterTypeChangeRequest pNewTypeReq = pNewRequest instanceof IMultipleParameterTypeChangeRequest? (IMultipleParameterTypeChangeRequest) pNewRequest : null;
                                    if (pNewTypeReq != null)
                                    {
                                        pNewTypeReq.setOldTypeName(pTypeReq.getOldTypeName());
                                        pNewTypeReq.setNewTypeName(pTypeReq.getNewTypeName());
                                        
                                        // now just add the new request to the passed in request.
                                        
                                        requestValidator.addRequest(pNewRequest);
                                        requestValidator.setValid(false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected void parameterChange(IRequestValidator requestValidator, int cType, int cDetail )
    {
        try
        {
            if (requestValidator != null && requestValidator.getValid()
                    && requestValidator.getRequest() != null)
            {
                IElement pElement = requestValidator.getRequest().getAfter();
                if (pElement != null)
                {
                    IParameter pRequestParm = pElement instanceof IParameter? (IParameter) pElement : null;
                    if (pRequestParm != null)
                    {
                        // Parameter change only applies to non-return parameters
                        int parmDir = pRequestParm.getDirection();
                        if (parmDir != IParameterDirectionKind.PDK_RESULT)
                        {
                            if ( cType == ChangeKind.CT_CREATE && cDetail == RequestDetailKind.RDT_TYPE_MODIFIED)
                                // Treat a type modification from unnamed to named as a create.
                                parameterAdded(requestValidator,pRequestParm);
                            else if ( cType == ChangeKind.CT_MODIFY && cDetail == RequestDetailKind.RDT_TYPE_MODIFIED)
                                parameterChange(requestValidator, pRequestParm);
                            else if ( cType == ChangeKind.CT_MODIFY && cDetail == RequestDetailKind.RDT_NAME_MODIFIED)
                            {
                                // If the parameter is still untyped at this point, invalidate
                                // the event so that no one receives it.
                                String typeName = pRequestParm.getTypeName();
                                if (typeName == null || typeName.trim().length() < 0)
                                {
                                    requestValidator.setValid(false);
                                }
                                else
                                {
                                    parameterNameChange(requestValidator, pRequestParm);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void parameterChange(IParameter pParameter)
    {
        if (getSilent())
        {
            RequestValidator requestValidator = new RequestValidator();
            requestValidator.setValid(true);
            parameterChange(requestValidator, pParameter);
        }
    }
    
    public void parameterChange(IRequestValidator requestValidator, IParameter pParameter)
    {
        try
        {
            if (pParameter != null)
            {
                String newType = pParameter.getTypeName();
                // Get the operation of the parameter
                IBehavioralFeature pFeat = pParameter.getBehavioralFeature();
                if (pFeat != null)
                {
                    IOperation pOperation = pFeat instanceof IOperation? (IOperation) pFeat : null;
                    if (pOperation != null)
                    {
                        // Get any operation that override this one
                        // (going DOWN the hierarchy) and change their name as well.
                        // This means following the redefines all the way down.
                        retypeparmOpsDown(requestValidator, pOperation, pParameter, newType);
                        // Get any operation that this one overrides
                        // (going UP the hierarchy and change that name as well.
                        // This will mean going back DOWN the hierachy for each
                        // change.
                        boolean retypeUp = false;
                        //AZTEC: TODO: need to uncomment the following line once the  IDS_JRT_METHOD_CHANGE_VERIFICATION_TEXT
                        //is available in the properties file.
                        retypeUp = queryBeforeRenameUp(pOperation, newType,
                                RPMessages.getString("IDS_JRT_METHOD_CHANGE_VERIFICATION_TEXT"));
                        
                        if ( retypeUp )
                        {
                            retypeparmOpsUp(requestValidator, pOperation, pParameter, newType);
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    protected void retypeparmOpsDown(IRequestValidator requestValidator, IOperation pOperation,
            IParameter pParameter, String newType)
    {
        if ( pParameter != null )
        {
            // Get any operation that override this one
            // (going DOWN the hierarchy) and change their type as well.
            // This means following the redefines all the way down.
            ETList<IOperation> redefiningOps = m_Utilities.collectRedefiningOps(pOperation);
            if (redefiningOps != null)
            {
                Iterator<IOperation> iter = redefiningOps.iterator();
                if (iter != null)
                {
                    while (iter.hasNext())
                    {
                        IOperation pOp = iter.next();
                        if (pOp != null)
                        {
                            // Before we recurse, see if the change is
                            // actually needed. If the change is not
                            // needed, we assume that we have already
                            // visited here and do not need to go any further.
                            // This is needed because RenameOpsUp call
                            // RenameOpsDown.
                            IParameter pOpsParam = m_Utilities.getCorrespondingParameter(pOp, pParameter);
                            boolean visited = false;
                            if ( pOpsParam != null )
                            {
                                String currentType = pOpsParam.getTypeName();
                                if ( !( currentType.equals(newType)) )
                                {
                                    pOpsParam.setTypeName(newType);
                                    addDependency(requestValidator, pOpsParam, pOp);
                                }
                                else
                                {
                                    visited = true;
                                }
                            }
                            if (!visited)
                                retypeparmOpsDown(requestValidator, pOp, pParameter, newType);
                        }
                    }
                }
            }
        }
    }
    
    protected void retypeparmOpsUp(IRequestValidator requestValidator, IOperation pOperation,
            IParameter pParameter, String newType)
    {
        if ( pParameter != null )
        {
            // Get any operation that override this one
            // (going DOWN the hierarchy) and change their type as well.
            // This means following the redefines all the way dow
            ETList<IOperation> redefinedOps = m_Utilities.collectRedefinedOps(pOperation);
            if (redefinedOps != null)
            {
                Iterator<IOperation> iter = redefinedOps.iterator();
                if (iter != null)
                {
                    while (iter.hasNext())
                    {
                        IOperation pOp = iter.next();
                        if (pOp != null)
                        {
                            // retype the parameter
                            IParameter pOpsParam = m_Utilities.getCorrespondingParameter(pOp,pParameter);
                            if (pOpsParam != null)
                            {
                                pOpsParam.setTypeName(newType);
                                addDependency(requestValidator, pOpsParam, pOp);
                            }
                            // Now, before we go UP from him, we want
                            // to go DOWN from him.
                            
                            retypeparmOpsDown(requestValidator, pOp, pParameter, newType);
                            retypeparmOpsUp(requestValidator, pOp, pParameter, newType);
                        }
                    }
                }
            }
        }
    }
    
    
    public void parameterNameChange(IRequestValidator requestValidator, IParameter pParameter)
    {
        try
        {
            if (pParameter != null)
            {
                String newName = pParameter.getName();
                
                // Get the operation of the parameter
                IBehavioralFeature pFeat = pParameter.getBehavioralFeature();
                
                if (pFeat != null)
                {
                    IOperation pOperation = pFeat instanceof IOperation? (IOperation) pFeat : null;
                    if (pOperation != null)
                    {
                        // Get any operation that override this one
                        // (going DOWN the hierarchy) and change their name as well.
                        // This means following the redefines all the way down.
                        reNameparmOpsDown(requestValidator, pOperation, pParameter, newName);
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     *
     * This operation renames all the parameters of a given position in all the
     * redefined operations the passed in parameter is involved with.
     *
     * @param request[out]     The validator
     * @param pOperation[in]   The owning operation of the parameter
     * @param pParameter[in]   The parameter whose name is changing
     * @param newName[in]      The new name of the parameter
     */
    protected void reNameparmOpsDown(IRequestValidator requestValidator, IOperation pOperation,
            IParameter pParameter, String newName)
    {
        if ( pParameter != null )
        {
            // Get any operation that override this one
            // (going DOWN the hierarchy) and change their type as well.
            // This means following the redefines all the way down.
            ETList<IOperation> redefiningOps = m_Utilities.collectRedefiningOps(pOperation);
            if (redefiningOps != null)
            {
                Iterator<IOperation> iter = redefiningOps.iterator();
                if (iter != null)
                {
                    while (iter.hasNext())
                    {
                        IOperation pOp = iter.next();
                        if (pOp != null)
                        {
                            // Before we recurse, see if the change is
                            // actually needed. If the change is not
                            // needed, we assume that we have already
                            // visited here and do not need to go any further.
                            // This is needed because RenameOpsUp call
                            // RenameOpsDown.
                            IParameter pOpsParam = m_Utilities.getCorrespondingParameter(pOp, pParameter);
                            boolean visited = false;
                            if ( pOpsParam != null )
                            {
                                String currentName = pOpsParam.getName();
                                if ( !( currentName == newName ) )
                                {
                                    pOpsParam.setName(newName);
                                }
                                else
                                {
                                    visited = true;
                                }
                            }
                            if (!visited)
                                reNameparmOpsDown(requestValidator, pOp, pParameter, newName);
                        }
                    }
                }
            }
        }
    }
    
    protected void parameterAdded(IRequestValidator requestValidator, int cType, int cDetail)
    {
        try
        {
            if (requestValidator != null && requestValidator.getValid())
            {
                boolean doIt = true;
                if ( cType == ChangeKind.CT_CREATE && cDetail == RequestDetailKind.RDT_PARAMETER_ADDED)
                {
                    if (requestValidator.getRequest() != null)
                    {
                        IElement pElement = requestValidator.getRequest().getAfter();
                        if ( pElement != null )
                        {
                            IParameter pRequestParm = pElement instanceof IParameter? (IParameter) pElement : null;
                            if ( pRequestParm != null )
                            {
                                // Before we continue, don't do anything if the type is blank
                                String typeName = pRequestParm.getTypeName();
                                if (typeName == null || typeName.length() < 0)
                                {
                                    doIt = false;
                                    requestValidator.setValid( false );
                                }
                                if (doIt)
                                {
                                    parameterAdded(requestValidator, pRequestParm);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void parameterAdded(IRequestValidator requestValidator, IParameter pParameter )
    {
        try
        {
            IBehavioralFeature pFeat = pParameter.getBehavioralFeature();
            if (pFeat != null)
            {
                IOperation pOperation = pFeat instanceof IOperation? (IOperation) pFeat : null;
                if (pOperation != null)
                {
                    addDependency(requestValidator, pParameter, pOperation);
                    // Get any operation that override this one
                    // (going DOWN the hierarchy) and change their name as well.
                    // This means following the redefines all the way down.
                    addparmOpsDown(requestValidator, pOperation, pParameter);
                    // Get any operation that this one overrides
                    // (going UP the hierarchy and change that name as well.
                    // This will mean going back DOWN the hierachy for each
                    // change.
                    
                    boolean addUp = false;
                    String newName = "";
                    //AZTEC: TODO: need to uncomment this one, once the IDS_JRT_METHOD_CHANGE_VERIFICATION_TEXT
                    //is available in the properties file.
                    addUp = queryBeforeRenameUp( pOperation, newName,
                            RPMessages.getString("IDS_JRT_METHOD_CHANGE_VERIFICATION_TEXT"));
                    if ( addUp )
                    {
                        addparmOpsUp(requestValidator, pOperation, pParameter);
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    protected void addparmOpsUp(IRequestValidator requestValidator, IOperation pOperation, IParameter pParameter)
    {
        if ( pParameter != null )
        {
            // Get any operation that override this one
            // (going DOWN the hierarchy) and change their type as well.
            // This means following the redefines all the way dow
            ETList<IOperation> redefinedOps = m_Utilities.collectRedefinedOps(pOperation);
            if (redefinedOps != null)
            {
                Iterator<IOperation> iter = redefinedOps.iterator();
                if (iter != null)
                {
                    while (iter.hasNext())
                    {
                        IOperation pOp = iter.next();
                        if (pOp != null)
                        {
                            // retype the parameter
                            IParameter pOpsParam = m_Utilities.copyParameter(pParameter, pOp);
                            if (pOpsParam != null)
                            {
                                pOp.addParameter(pOpsParam);
                                addDependency(requestValidator, pOpsParam, pOp);
                                // Now, before we go UP from him, we want
                                // to go DOWN from him.
                                addparmOpsDown(requestValidator, pOp, pParameter);
                                // Now we can go up. Java is nice because
                                // we don't have to worry about multiple inheritance.
                                addparmOpsUp(requestValidator, pOp, pParameter);
                            }
                        }
                    }
                }
            }
        }
    }
    
    
    protected void addparmOpsDown(IRequestValidator requestValidator, IOperation pOperation, IParameter pParameter)
    {
        if (pParameter != null)
        {
            // Get any operation that override this one
            // (going DOWN the hierarchy) and change their type as well.
            // This means following the redefines all the way down.
            ETList<IOperation> redefiningOps = m_Utilities.collectRedefiningOps(pOperation);
            if (redefiningOps != null)
            {
                Iterator<IOperation> iter = redefiningOps.iterator();
                if (iter != null)
                {
                    while (iter.hasNext())
                    {
                        IOperation pOp = iter.next();
                        if (pOp != null)
                        {
                            // Before we recurse, see if the change is
                            // actually needed. If the change is not
                            // needed, we assume that we have already
                            // visited here and do not need to go any further.
                            // This is needed because RenameOpsUp call
                            // RenameOpsDown.
                            IParameter pOpsParam = m_Utilities.getCorrespondingParameter(pOp, pParameter);
                            if (pOpsParam == null)
                            {
                                // Ok, there is no corresponding attribute. Copy the attribute
                                // and add it to the redefining operation. Then keep going down.
                                pOpsParam = m_Utilities.copyParameter(pParameter, pOp);
                                if (pOpsParam != null)
                                {
                                    pOp.addParameter(pOpsParam);
                                    addDependency(requestValidator, pOpsParam, pOp);
                                    addparmOpsDown(requestValidator, pOp, pParameter);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected void parameterDeleted( IRequestValidator requestValidator, int cType, int cDetail )
    {
        try
        {
            if (requestValidator != null && requestValidator.getValid())
            {
                if ( cType == ChangeKind.CT_MODIFY && cDetail == RequestDetailKind.RDT_PARAMETER_REMOVED)
                {
                    if (requestValidator.getRequest() != null)
                    {
                        IElement pElement = requestValidator.getRequest().getAfter();
                        if (pElement != null)
                        {
                            IParameter pRequestParm = pElement instanceof IParameter? (IParameter) pElement : null;
                            if (pRequestParm != null)
                            {
                                // We need the operation. Get it from the before element
                                IElement pBefore = requestValidator.getRequest().getBefore();
                                if (pBefore != null)
                                {
                                    IParameter pBeforeParm = pBefore instanceof IParameter? (IParameter) pBefore : null;
                                    if (pBeforeParm != null)
                                    {
                                        // We cannot get the feature from the parameter, since it has
                                        // be removed. Must use the payload.
                                        IEventPayload pPayload = requestValidator.getRequest().getPayload();
                                        if (pPayload != null)
                                        {
                                            IElementContextPayload pContext = pPayload instanceof IElementContextPayload? (IElementContextPayload) pPayload : null;
                                            if (pContext != null)
                                            {
                                                IElement pOwner = pContext.getOwner();
                                                if ( pOwner != null )
                                                {
                                                    IOperation pOperation = pOwner instanceof IOperation? (IOperation) pOwner : null;
                                                    if (pOperation != null)
                                                    {
                                                        parameterDeleted(pOperation, pRequestParm);
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
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void parameterDeleted(IOperation pOperation, IParameter pParameter)
    {
        // In other words, the position of the parameter is all that matters,
        // because there is nothing else to go on. How do we do this, since
        // the operation is already modified?
        // The only thing we can do is to get a redefining or redefined operation
        // and compare the two. The missing parameter is the one that has been
        // removed.  Obviously if there is no redefining or redefined ops,
        // we don't need to do anything anyway.
        
        int paramIDX = -1;
        int oldParmCount = 0;
        
        ETPairT<Integer, Integer> values = determineDeletedParameter(pOperation, pParameter);
        if (values != null)
        {
            if (values.getParamOne() != null)
                paramIDX = values.getParamOne().intValue();
            
            if (values.getParamTwo() != null)
                oldParmCount = values.getParamTwo().intValue();
        }
        
        if ( paramIDX >= 0 )
        {
            // Get any operation that override this one
            // (going DOWN the hierarchy) and change their name as well.
            // This means following the redefines all the way down.
            
            deleteparmOpsDown(pOperation, paramIDX, oldParmCount);
            
            // Get any operation that this one overrides
            // (going UP the hierarchy and change that name as well.
            // This will mean going back DOWN the hierachy for each
            // change.
            
            boolean addUp = false;
            String newType = "";
            //AZTEC: TODO: need to uncomment once IDS_JRT_METHOD_CHANGE_VERIFICATION_TEXT is availbale
            //from the properties file
            addUp = queryBeforeRenameUp( pOperation, newType,
                    RPMessages.getString("IDS_JRT_METHOD_CHANGE_VERIFICATION_TEXT") );
            if ( addUp )
            {
                deleteparmOpsUp(pOperation, paramIDX, oldParmCount);
            }
        }
    }
    
    /**
     *
     * Determine the index of the parameter that was removed from the operation.
     * Passes back the index of the deleted parameter and the number of parameters
     * that were in the operation before it was deleted.
     *
     * @param pOperation[in]
     * @param pParam[in]
     */
    protected ETPairT<Integer, Integer> determineDeletedParameter
            (IOperation pOperation, IParameter pParameter)
    {
        int paramIDX = -1;
        int oldParmCount = 0;
        if (pOperation != null)
        {
            ETList<IParameter> opsParms = pOperation.getParameters();
            int opsParmsCount = 0;
            if (opsParms != null)
                opsParmsCount = opsParms.size();
            ETList<IOperation> redefiningOps = m_Utilities.collectRedefiningOps(pOperation);
            IOperation pItem = null;
            
            if (redefiningOps != null)
            {
                if (redefiningOps.size() > 0)
                    pItem = redefiningOps.get(0);
            }
            
            // If we don't have a good item yet, go to the redefined operations.
            if (pItem == null)
            {
                redefiningOps = null;
                redefiningOps = m_Utilities.collectRedefinedOps(pOperation);
                if (redefiningOps != null)
                {
                    // we only need the first one.
                    if ( redefiningOps.size() > 0 )
                    {
                        pItem = redefiningOps.get(0);
                    }
                }
            }
            
            // If we still don't have one, we don't need to do anything anyway.
            if ( pItem != null )
            {
                ETList<IParameter> itemsParms = pItem.getParameters();
                int itemsParmsCount = 0;
                if (itemsParms != null)
                    itemsParmsCount = itemsParms.size();
                
                oldParmCount = itemsParmsCount;
                
                // Now, go over the two parameter lists, finding the one that is missing.
                int idx1 = 0;
                int idx2 = 0;
                
                while ( idx1 < opsParmsCount &&
                        idx2 < itemsParmsCount &&
                        paramIDX == -1 )
                {
                    IParameter pParm1 = opsParms.get(idx1++);
                    IParameter pParm2 = itemsParms.get(idx2++);
                    
                    String parmName1 = pParm1.getName();
                    String parmName2 = pParm2.getName();
                    
                    if ( parmName1 != null && parmName1.length() > 0 &&
                            parmName2 != null && parmName2.length() > 0 )
                    {
                        if (!(parmName1.equals(parmName2)))
                        {
                            // the names are not the same. The parameter from the
                            // item operation must be the one that was removed.
                            paramIDX = idx2 - 1;
                        }
                    }
                    else
                    {
                        // we can only go on type. This is not absolutely correct,
                        // but it is the only thing we can do right now.
                        String typeName1 = pParm1.getTypeName();
                        String typeName2 = pParm2.getTypeName();
                        
                        if ( typeName1 != null && typeName1.length() > 0 &&
                                typeName1 != null && typeName2.length() > 0  &&
                                (!(typeName1.equals(typeName2))) )
                        {
                            // the types are not the same. The parameter from the
                            // item operation must be the one that was removed.
                            paramIDX = idx2 - 1;
                        }
                    }
                }
                
                if ( paramIDX == -1 )
                {
                    // this would mean that the last one was deleted.
                    paramIDX = itemsParmsCount - 1;
                }
            }
        }
        
        return new ETPairT<Integer,Integer>(new Integer(paramIDX), new Integer(oldParmCount));
    }
    
    protected void deleteparmOpsDown(IOperation pOperation, int paramIDX, int oldParmCount)
    {
        if ( pOperation != null )
        {
            // Get any operation that override this one
            // (going DOWN the hierarchy) and change their type as well.
            // This means following the redefines all the way down.
            ETList<IOperation> redefiningOps = m_Utilities.collectRedefiningOps(pOperation);
            if (redefiningOps != null)
            {
                Iterator<IOperation> iter = redefiningOps.iterator();
                if (iter != null)
                {
                    while (iter.hasNext())
                    {
                        IOperation pOp = iter.next();
                        if (pOp != null)
                        {
                            // Before we recurse, see if the change is
                            // actually needed. If the change is not
                            // needed, we assume that we have already
                            // visited here and do not need to go any further.
                            // This is needed because RenameOpsUp call
                            // RenameOpsDown.
                            ETList<IParameter> pOpsParams = pOp.getParameters();
                            int newParmCount = 0;
                            if ( pOpsParams != null )
                                newParmCount = pOpsParams.size();
                            
                            if ( newParmCount == oldParmCount )
                            {
                                // Ok, this one has not been touched yet.
                                IParameter pOpsParam = m_Utilities.getPositionParameter(pOp, paramIDX);
                                if ( pOpsParam != null )
                                {
                                    pOp.removeParameter(pOpsParam);
                                    pOpsParam.delete();
                                    
                                    deleteparmOpsDown(pOp, paramIDX, oldParmCount);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected void deleteparmOpsUp(IOperation pOperation, int paramIDX, int oldParmCount)
    {
        ETList<IOperation> redefinedOps =  null;
        if (pOperation != null)
            redefinedOps = m_Utilities.collectRedefinedOps(pOperation);
        if ( redefinedOps != null )
        {
            int idx = 0;
            int count = redefinedOps.size();
            
            while ( idx < count )
            {
                IOperation pOp = redefinedOps.get(idx++);
                if (pOp != null)
                {
                    IParameter pOpsParam = m_Utilities.getPositionParameter(pOp, paramIDX);
                    if ( pOpsParam != null )
                    {
                        pOp.removeParameter(pOpsParam);
                        pOpsParam.delete();
                        
                        // Now, before we go UP from him, we want
                        // to go DOWN from him.
                        deleteparmOpsDown(pOp, paramIDX, oldParmCount);
                        
                        // Now we can go up. Java is nice because
                        // we don't have to worry about multiple inheritance.
                        deleteparmOpsUp(pOp, paramIDX, oldParmCount);
                    }
                }
            }
        }
    }
    
    public void typeChange(IOperation pOperation)
    {
        if (getSilent())
        {
            RequestValidator requestValidator = new RequestValidator();
            requestValidator.setValid(true);
            typeChange(requestValidator, pOperation );
        }
    }
    
    protected void typeChange(IRequestValidator requestValidator, int cType, int cDetail)
    {
        try
        {
            if (requestValidator != null && requestValidator.getValid() && requestValidator.getRequest() != null)
            {
                if ( cType == ChangeKind.CT_MODIFY && cDetail == RequestDetailKind.RDT_TYPE_MODIFIED)
                {
                    IElement pElement = requestValidator.getRequest().getAfter();
                    if (pElement != null)
                    {
                        IParameter pRequestParm = pElement instanceof IParameter? (IParameter) pElement : null;
                        if (pRequestParm != null)
                        {
                            // Type change only applies to return parameters
                            int parmDir = pRequestParm.getDirection();
                            if (parmDir == IParameterDirectionKind.PDK_RESULT)
                            {
                                IBehavioralFeature pFeat = pRequestParm.getBehavioralFeature();
                                if (pFeat != null)
                                {
                                    IOperation pRequestOp = pFeat instanceof IOperation? (IOperation) pFeat : null;
                                    if ( pRequestOp != null )
                                    {
                                        typeChange(requestValidator, pRequestOp);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void typeChange(IRequestValidator requestValidator, IOperation pOperation)
    {
        addDependency(requestValidator, pOperation);
        // Get the return type as a string
        String newType = m_Utilities.getOperationReturnType(pOperation);
        // Get any operation that override this one
        // (going DOWN the hierarchy) and change their name as well.
        // This means following the redefines all the way down.
        
        retypeOpsDown(requestValidator, pOperation, newType);
        // Get any operation that this one overrides
        // (going UP the hierarchy and change that name as well.
        // This will mean going back DOWN the hierachy for each
        // change.
        
        boolean retypeUp = false;
        //AZTEC: TODO: uncomment the following once IDS_JRT_METHOD_CHANGE_VERIFICATION_TEXT
        //is available in the properties file.
        retypeUp  = queryBeforeRenameUp( pOperation, newType,
                RPMessages.getString("IDS_JRT_METHOD_CHANGE_VERIFICATION_TEXT"));
        
        if (retypeUp)
        {
            retypeOpsUp(requestValidator, pOperation, newType);
        }
    }
    
    protected void retypeOpsDown(IRequestValidator requestValidator, IOperation pOperation,
            String newType)
    {
        if ( pOperation != null )
        {
            // Get any operation that override this one
            // (going DOWN the hierarchy) and change their type as well.
            // This means following the redefines all the way down.
            ETList<IOperation> redefiningOps = m_Utilities.collectRedefiningOps(pOperation);
            if (redefiningOps != null)
            {
                Iterator<IOperation> iter = redefiningOps.iterator();
                if (iter != null)
                {
                    while (iter.hasNext())
                    {
                        IOperation pOp = iter.next();
                        if (pOp != null)
                        {
                            // Before we recurse, see if the change is
                            // actually needed. If the change is not
                            // needed, we assume that we have already
                            // visited here and do not need to go any further.
                            // This is needed because RenameOpsUp call
                            // RenameOpsDown.
                            if ( pOp != null )
                            {
                                String currentType = m_Utilities.getOperationReturnType(pOp);
                                if (currentType != null && !(currentType.equals(newType)))
                                {
                                    m_Utilities.setOperationReturnType(pOp, newType);
                                    addDependency(requestValidator, pOp);
                                    retypeOpsDown(requestValidator, pOp, newType);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected void retypeOpsUp(IRequestValidator requestValidator, IOperation pOperation, String newType)
    {
        if ( pOperation != null )
        {
            // Get any operation that override this one
            // (going DOWN the hierarchy) and change their type as well.
            // This means following the redefines all the way dow
            ETList<IOperation> redefinedOps = m_Utilities.collectRedefinedOps(pOperation);
            if (redefinedOps != null)
            {
                Iterator<IOperation> iter = redefinedOps.iterator();
                if (iter != null)
                {
                    while (iter.hasNext())
                    {
                        IOperation pOp = iter.next();
                        if (pOp != null)
                        {
                            // retype the operation
                            m_Utilities.setOperationReturnType(pOp, newType);
                            addDependency(requestValidator, pOp);
                            
                            // Now, before we go UP from him, we want
                            // to go DOWN from him.
                            retypeOpsDown(requestValidator, pOp, newType);
                            
                            // Now we can go up. Java is nice because
                            // we don't have to worry about multiple inheritance.
                            retypeOpsUp(requestValidator, pOp, newType);
                        }
                    }
                }
            }
        }
    }
    
    protected void deleted(IRequestValidator requestValidator, int cType, int cDetail)
    {
        try
        {
            if (requestValidator != null && requestValidator.getValid())
            {
                if ( cType == ChangeKind.CT_DELETE && cDetail == RequestDetailKind.RDT_ELEMENT_DELETED)
                {
                    // We need the before so that we can navigate the redefines
                    IElement pElement = m_Utilities.getElement(requestValidator.getRequest(), true);
                    if (pElement != null)
                    {
                        IOperation pOperation = pElement instanceof IOperation? (IOperation) pElement : null;
                        if (pOperation != null)
                            deleted(pOperation);
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void deleted(IOperation pOperation)
    {
        ETList<IOperation> redefiningOps = m_Utilities.collectRedefiningOps(pOperation);
        if (redefiningOps != null)
        {
            boolean deleteDown = false;
            if (redefiningOps.size() > 0)
            {
                // Query the user for what we want to do.
                deleteDown = queryUserBeforeDelete(pOperation);
                if (deleteDown)
                {
                    deleteOpsDown(pOperation);
                }
                else
                {
                    removeFromRedefinitions(pOperation);
                }
            }
        }
    }
    
    protected void deleteOpsDown(IOperation pOperation)
    {
        ETList<IOperation> redefiningOps = m_Utilities.collectRedefiningOps(pOperation);
        if (redefiningOps != null)
        {
            Iterator<IOperation> iter = redefiningOps.iterator();
            if (iter != null)
            {
                while (iter.hasNext())
                {
                    IOperation pItem = iter.next();
                    if (pItem != null)
                    {
                        // This will have the effect of deleting from the bottom up
                        deleteOpsDown(pItem);
                        pItem.delete();
                    }
                }
            }
        }
    }
    
    protected void removeFromRedefinitions(IOperation pOperation)
    {
        ETList<IOperation> redefiningOps = m_Utilities.collectRedefiningOps(pOperation);
        Iterator<IOperation> iter2 = null;
        if (redefiningOps != null)
        {
            iter2 = redefiningOps.iterator();
        }
        
        ETList<IOperation> redefinedOps = m_Utilities.collectRedefinedOps(pOperation);
        if (redefinedOps != null)
        {
            Iterator<IOperation> iter1 = redefinedOps.iterator();
            if (iter1 != null)
            {
                while (iter1.hasNext())
                {
                    IOperation pOuterOp = iter1.next();
                    if (pOuterOp != null)
                    {
                        if (iter2 != null)
                        {
                            while (iter2.hasNext())
                            {
                                IOperation pInnerOp = iter2.next();
                                if (pInnerOp != null)
                                {
                                    pOuterOp.addRedefiningElement(pInnerOp);
                                    pInnerOp.addRedefinedElement(pOuterOp);
                                }
                            }
                        }
                    }
                }
            }
        }
        else
        {
            if (iter2 != null)
            {
                while (iter2.hasNext())
                {
                    IOperation pItem = iter2.next();
                    m_Utilities.breakRedefinition(pOperation, pItem);
                }
            }
        }
    }
    
    
    protected void visibilityChange( IRequestValidator requestValidator, int cType, int cDetail )
    {
        //C++ method is empty.
    }
    
    protected void abstractChange( IRequestValidator requestValidator, int cType, int cDetail )
    {
        if (requestValidator != null && requestValidator.getValid())
        {
            if ( cType == ChangeKind.CT_MODIFY && cDetail == RequestDetailKind.RDT_ABSTRACT_MODIFIED)
            {
                IOperation pOperation = null;
                IClassifier pClass = null;
                ETPairT<IOperation, IClassifier> operClass =
                        m_Utilities.getOperationAndClass(requestValidator.getRequest(),false);
                if (operClass != null)
                {
                    pOperation = operClass.getParamOne();
                    pClass = operClass.getParamTwo();
                }
                abstractChange(requestValidator, pOperation);
            }
        }
    }
    
    public void abstractChange(IRequestValidator requestValidator, IOperation pOperation)
    {
        if (pOperation != null)
        {
            IClassifier pClass = pOperation.getFeaturingClassifier();
            abstractChange(requestValidator, pOperation, pClass);
        }
    }
    
    public void abstractChange(IRequestValidator requestValidator, IOperation pOperation, IClassifier pClass)
    {
        try
        {
            if (pOperation != null)
            {
                boolean isAbstract = false;
                isAbstract = pOperation.getIsAbstract();
                boolean isClassAbstract = isAbstract;
                
                if (!isAbstract)
                {
                    // The operation is becoming non-abstract. We now want
                    // to find if there are anymore abstract methods on the class.
                    // This is not as easy as it sounds. If the class implements
                    // interfaces, and not all interface methods are defined,
                    // the class is abstract. This also is not as easy as it sounds,
                    // because we have to worry about interface generalization,
                    // and interfaces implemented by base classes.
                    IOperationCollectionBehavior behavior = new OperationCollectionBehavior();
                    ETList<IOperation> classOnlyOps =
                            m_Utilities.collectAbstractOperations(pClass, behavior);
                    // The above is a quick check.  If the answer is still no,
                    // we have to do the complex check.
                    boolean hasAbstractOps = false;
                    if (classOnlyOps != null && classOnlyOps.size() > 0)
                        hasAbstractOps = true;
                    
                    if (!hasAbstractOps)
                    {
                        // Gotta do the complex check.
                        ETList<IOperation> allAbstractOps =
                                m_Utilities.collectInheritedAbstractOperations(pClass, behavior);
                        if (allAbstractOps != null && allAbstractOps.size() > 0)
                            hasAbstractOps = true;
                    }
                    if (hasAbstractOps)
                        isClassAbstract = true;
                }
                else
                {
                    // The operation is becoming abstract. Give the user a chance to
                    // add a redefinition to any or all derived classes.
                    ETList<IClassifier> derivedClasses = m_Utilities.getSpecializations(pClass);
                    if (derivedClasses != null)
                    {
                        int count = derivedClasses.size();
                        int idx = 0;
                        boolean addOp = false;
                        //AZTEC: TODO: need to uncomment the following line once the properties
                        //file is available.
                        //addOp = displayYesNoMessage(IDS_JRT_CREATE_OPERATION_FOR_ABSTRACT,
                        //	 						  IDS_JRT_CREATE_OPERATION_FOR_ABSTRACT_TITLE,
                        //							  false );
                        
                        // Copy the operation and add it to the derived class,
                        // making sure to set the new operation as NOT abstract
                        
                        // The first thing we must do is eliminate from the derived list
                        // all classes that have a redefinition of this operation already.
                        ETList<IClassifier> alreadyDefiningClasses = null;
                        ETList<IOperation> redefines = m_Utilities.collectRedefiningOps(pOperation);
                        int alreadyDefinesCount = 0;
                        
                        if (redefines != null)
                        {
                            int count2 = redefines.size();
                            int idx2 = 0;
                            while (idx2 < count2)
                            {
                                IOperation pItem = redefines.get(idx2++);
                                if (pItem != null)
                                {
                                    IClassifier pItemClass = pItem.getFeaturingClassifier();
                                    if (pItemClass != null)
                                    {
                                        if (alreadyDefiningClasses == null)
                                            alreadyDefiningClasses = new ETArrayList<IClassifier>();
                                        else
                                        {
                                            alreadyDefiningClasses.add(pItemClass);
                                            alreadyDefinesCount++;
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Now we can go over the sub classes. If the subclass IS NOT on the
                        // alreadyDefining list, we must process it.
                        while ( idx < count )
                        {
                            IClassifier pItem = derivedClasses.get(idx++);
                            if (pItem != null)
                            {
                                boolean isOnList = false;
                                int idx2 = 0;
                                while ( idx2 < alreadyDefinesCount &&
                                        isOnList == false )
                                {
                                    IClassifier pItemClass = alreadyDefiningClasses.get(idx2++);
                                    boolean isSame = pItem.isSame(pItemClass);
                                    if (isSame)
                                    {
                                        isOnList = true;
                                    }
                                }
                                if ( !isOnList )
                                {
                                    if ( addOp )
                                    {
                                        IOperation pNewOp = m_Utilities.copyOperation(pOperation, pItem);
                                        if (pNewOp != null)
                                        {
                                            pNewOp.setIsAbstract(false);
                                            m_Utilities.addRedefiningOperation(requestValidator, pOperation, pNewOp, pItem);
                                        }
                                    }
                                    else
                                    {
                                        // this class is also becoming abstract
                                        pItem.setIsAbstract(true);
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Finally, set the operations class to the correct abstractness.
                // Unfortunately, we still have the @#*! of the core
                // not checking the current value. To prevent multiple changes,
                // we have to not do a set.
                
                // Also, we might be making a method abstract on an interface
                // (happens during a transform. If this is the case, we want
                // to set the class to abstract but not send a change request.
                // Now that we have the plugger, we can do this easily.
                boolean currentValue = pClass.getIsAbstract();
                if ( isClassAbstract  != currentValue )
                {
                    if (isClassAbstract)
                    {
                        IInterface pInterface = pClass instanceof IInterface? (IInterface) pClass : null;
                        if (pInterface != null)
                        {
                            plug(new RequestPlug());
                            
                        }
                    }
                    pClass.setIsAbstract(isClassAbstract );
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    protected void nameChange(IRequestValidator requestValidator, int cType, int cDetail)
    {
        if (requestValidator != null && requestValidator.getValid())
        {
            if (cType == ChangeKind.CT_MODIFY && (cDetail == RequestDetailKind.RDT_NAME_MODIFIED || cDetail == RequestDetailKind.RDT_SIGNATURE_CHANGED))
            {
                IOperation pOperation = null;
                IClassifier pClass = null;
                ETPairT<IOperation, IClassifier> operClass =
                        m_Utilities.getOperationAndClass(requestValidator.getRequest(),false);
                
                if(operClass != null)
                {
                    pOperation = operClass.getParamOne();
                    if (pOperation != null)
                    {
                        if (pOperation.getIsConstructor())
                        {
                            String oldName = pOperation.getFeaturingClassifier().getName();
                            String newName = pOperation.getName();
                            if ((oldName != null) && !oldName.equals(newName))
                            {
                                // do not rename constructor
                                String msg = RPMessages.getString("IDS_JRT_CONSTRUCTOR_RENAMED_DENY");
                                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                                DialogDisplayer.getDefault().notify(d);
                                requestValidator.setValid(false);
                                pOperation.setName(oldName);
                            }
                        }
                        else if (cDetail == RequestDetailKind.RDT_NAME_MODIFIED)
                        {
                            enforceUniqueness(pOperation, pOperation.getFeaturingClassifier());
                            nameChange(pOperation);
                        }
                    }
                }
            }
        }
    }
    
    public void nameChange(IOperation pOperation)
    {
        String newName = pOperation.getName();
        // Get any operation that override this one
        // (going DOWN the hierarchy) and change their name as well.
        // This means following the redefines all the way down.
        renameOpsDown(pOperation, newName);
        
        // Get any operation that this one overrides
        // (going UP the hierarchy and change that name as well.
        // This will mean going back DOWN the hierachy for each
        // change.
        boolean renameUp = false;
        // AZTEC: TODO: need to uncomment the following line once the IDS_JRT_METHOD_CHANGE_VERIFICATION_TEXT
        // is available in the properties file.
        renameUp = queryBeforeRenameUp( pOperation, newName,
                RPMessages.getString("IDS_JRT_METHOD_CHANGE_VERIFICATION_TEXT"));
        if (renameUp)
            renameOpsUp(pOperation, newName);
    }
    
    protected void renameOpsDown(IOperation pOperation, String newName)
    {
        ETList<IOperation> redefiningOps =  null;
        if (pOperation != null)
            redefiningOps = m_Utilities.collectRedefiningOps(pOperation);
        if ( redefiningOps != null )
        {
            int idx = 0;
            int count = redefiningOps.size();
            
            while ( idx < count )
            {
                IOperation pOp = redefiningOps.get(idx++);
                if (pOp != null)
                {
                    // Before we recurse, see if the change is
                    // actually needed. If the change is not
                    // needed, we assume that we have already
                    // visited here and do not need to go any further.
                    // This is needed because RenameOpsUp call
                    // RenameOpsDown.
                    String currentName = pOp.getName();
                    if (currentName != null && !currentName.equals(newName))
                    {
                        pOp.setName(newName);
                        renameOpsDown(pOp, newName);
                    }
                }
            }
        }
    }
    
    public void renameOpsUp(IOperation pOperation, String newName)
    {
        ETList<IOperation> redefinedOps =  null;
        if (pOperation != null)
            redefinedOps = m_Utilities.collectRedefinedOps(pOperation);
        if ( redefinedOps != null )
        {
            int idx = 0;
            int count = redefinedOps.size();
            
            while ( idx < count )
            {
                IOperation pOp = redefinedOps.get(idx++);
                if (pOp != null)
                {
                    pOp.setName(newName);
                    
                    renameOpsDown(pOp, newName);
                    
                    // Now we can go up. Java is nice because
                    // we don't have to worry about multiple inheritance.
                    renameOpsUp(pOp, newName);
                }
            }
        }
    }
    protected void moved(IRequestValidator requestValidator, int cType, int cDetail)
    {
        if (requestValidator != null && requestValidator.getValid())
        {
            if ( cType == ChangeKind.CT_MODIFY && cDetail == RequestDetailKind.RDT_FEATURE_MOVED)
            {
                IOperation pNewOperation = null;
                IClassifier pNewClass = null;
                
                IOperation pOldOperation = null;
                IClassifier pOldClass = null;
                
                ETPairT<IOperation, IClassifier> operClass =
                        m_Utilities.getOperationAndClass(requestValidator.getRequest(),false);
                if (operClass != null)
                {
                    pNewOperation = operClass.getParamOne();
                    pNewClass = operClass.getParamTwo();
                }
                
                operClass = null;
                operClass = m_Utilities.getOperationAndClass(requestValidator.getRequest(),true);
                if (operClass != null)
                {
                    pOldOperation = operClass.getParamOne();
                    pOldClass = operClass.getParamTwo();
                }
                
                boolean valid = true;
                if (pNewOperation != null && pNewClass != null &&
                        pOldOperation != null && pOldClass != null)
                {
                    moved(requestValidator, pOldOperation, pOldClass, pNewClass);
                }
                requestValidator.setValid(valid);
            }
        }
    }
    
    public void moved(IRequestValidator requestValidator, IOperation pOperation,
            IClassifier pFromClass, IClassifier pToClass)
    {
        // If the new class is unnamed, we want to invalidate the move
        // event and just create a Delete event for the operation from the
        // old class.
        if ( m_Utilities.isElementUnnamed(pToClass))
        {
            IOperation pOldOperation = null;
            IClassifier pTempClass = null;
            
            ETPairT<IOperation, IClassifier> operClass =
                    m_Utilities.getOperationAndClass(requestValidator.getRequest(),false);
            if (operClass != null)
            {
                pOldOperation = operClass.getParamOne();
                pTempClass = operClass.getParamTwo();
            }
            
            if (pOldOperation != null)
            {
                IChangeRequest pNewRequest = m_Utilities.createChangeRequest(
                        null,
                        ChangeKind.CT_DELETE,
                        RequestDetailKind.RDT_ELEMENT_DELETED,
                        pOldOperation,
                        pOldOperation,
                        pFromClass);
                if ( pNewRequest != null )
                {
                    requestValidator.addRequest(pNewRequest);
                    requestValidator.setValid(false);
                }
            }
        }
        else
        {
            // all that needs to be done is add dependencies to the new class
            addDependency(requestValidator, pOperation, pToClass);
        }
    }
    
    protected void duplicated(IRequestValidator requestValidator, int cType, int cDetail)
    {
        if (requestValidator != null && requestValidator.getValid())
        {
            if ( cType == ChangeKind.CT_MODIFY && cDetail == RequestDetailKind.RDT_FEATURE_DUPLICATED)
            {
                IOperation pNewOperation = null;
                IClassifier pNewClass = null;
                
                IOperation pOldOperation = null;
                IClassifier pOldClass = null;
                
                ETPairT<IOperation, IClassifier> operClass =
                        m_Utilities.getOperationAndClass(requestValidator.getRequest(),false);
                if (operClass != null)
                {
                    pNewOperation = operClass.getParamOne();
                    pNewClass = operClass.getParamTwo();
                }
                
                operClass = null;
                operClass = m_Utilities.getOperationAndClass(requestValidator.getRequest(),true);
                if (operClass != null)
                {
                    pOldOperation = operClass.getParamOne();
                    pOldClass = operClass.getParamTwo();
                }
                
                boolean valid = true;
                if (pNewOperation != null && pNewClass != null &&
                        pOldOperation != null && pOldClass != null)
                {
                    duplicated(requestValidator, pOldOperation, pOldClass, pNewOperation, pNewClass);
                }
                requestValidator.setValid(valid);
            }
        }
    }
    
    public void duplicated(IRequestValidator request, IOperation pFromOperation, IClassifier pFromClass,
            IOperation pToOperation, IClassifier pToClass)
    {
        // all that needs to be done is add dependencies to the new class
        addDependency(request, pToOperation, pToClass);
    }
    
    protected void signatureChange  ( IRequestValidator requestValidator, int cType, int cDetail )
    {
        
        try
        {
            if (requestValidator != null && requestValidator.getValid())
            {
                if ( cType == ChangeKind.CT_MODIFY && cDetail == RequestDetailKind.RDT_SIGNATURE_CHANGED)
                {
                    // Lets make sure that we have a signature change request,
                    // and that there are actual changes on it.
                    IOperationSignatureChangeRequest pSigChange =
                            (IOperationSignatureChangeRequest) requestValidator.getRequest();
                    if (pSigChange != null)
                    {
                        int subRequestCount = pSigChange.getCount();
                        if ( subRequestCount > 0 )
                        {
                            // THIS IS THE IMPORTANT DUDE.
                            // The destruction of this guy should cause pops on all the
                            // sig changes on the redefining/redefined ops.
                            ETList<IOperationSignatureChangeContextManager> opSigChanges =
                                    new ETArrayList<IOperationSignatureChangeContextManager>();
                            try
                            {
                                IOperation pRequestOp = null;
                                IClassifier pClass = null;
                                
                                ETPairT<IOperation, IClassifier> operClass =
                                        m_Utilities.getOperationAndClass(requestValidator.getRequest(),false);
                                if (operClass != null)
                                {
                                    pRequestOp = operClass.getParamOne();
                                    pClass = operClass.getParamTwo();
                                }
                                enforceUniqueness(pRequestOp, pClass);
                                boolean renameDown = false;
                                boolean processedRedefines = false;
                                // Ok. We want to start sig changes on all redefined and redefining ops
                                // before we modify those ops.
                                ETList<IOperation> opsDown = m_Utilities.collectRedefiningOps(pRequestOp);
                                
                                if ( opsDown != null )
                                {
                                    int count = opsDown.size();
                                    int idx = 0;
                                    if( count > 0)
                                    {
                                        processedRedefines = true;
                                        renameDown = true;
                                    }
                                    else renameDown = false;
                                    
                                    while ( idx < count )
                                    {
                                        IOperation pItem = opsDown.get(idx++);
                                        if ( pItem != null )
                                        {
                                            IOperationSignatureChangeContextManager pSigChangeMan =
                                                    new OperationSignatureChangeContextManager();
                                            if ( pSigChangeMan != null )
                                            {
                                                pSigChangeMan.startSignatureChange(pItem);
                                                opSigChanges.add(pSigChangeMan);
                                            }
                                        }
                                    }
                                }
                                
                                boolean currentSilent = getSilent();
                                
                                boolean renameUp = false;
                                String dummyName = "";
                                //AZTEC: TODO: Need to uncomment the following once IDS_JRT_METHOD_CHANGE_VERIFICATION_TEXT
                                //is available from the properties file.
                                renameUp = queryBeforeRenameUp( pRequestOp, dummyName,
                                        RPMessages.getString("IDS_JRT_METHOD_CHANGE_VERIFICATION_TEXT"));
                                
                                if (renameUp)
                                {
                                    // Start sig changes on these too.
                                    ETList<IOperation> opsUp = m_Utilities.collectRedefinedOps(pRequestOp);
                                    if ( opsUp != null )
                                    {
                                        int count = opsUp.size();
                                        int idx = 0;
                                        
                                        if( count > 0)
                                            processedRedefines = true;
                                        
                                        while ( idx < count )
                                        {
                                            IOperation pItem = opsUp.get(idx++);
                                            if ( pItem != null )
                                            {
                                                IOperationSignatureChangeContextManager pSigChangeMan =
                                                        new OperationSignatureChangeContextManager();
                                                if ( pSigChangeMan != null )
                                                {
                                                    pSigChangeMan.startSignatureChange(pItem);
                                                    opSigChanges.add(pSigChangeMan);
                                                }
                                            }
                                        }
                                    }
                                    // Now, make sure that we don't ask this question again.
                                    setSilent( true );
                                }
                                if(renameUp || renameDown)
                                {
                                    isRelated = true;
                                }
                                else if(!renameUp)
                                {
                                    if(diagRetval == false)
                                    {
                                        isRelated = true;
                                    }
                                }
                                // Ok. we are now ready to simply go over the list
                                // of sub requests and handle each one as we normally would.
                                
                                if (processedRedefines)
                                {
                                    IRequestProcessor pProc = getProcessor();
                                    if (pProc != null)
                                    {
                                        ETList<IChangeRequest> subRequests = pSigChange.getRequests();
                                        if ( subRequests != null )
                                        {
                                            int numReqs = subRequests.size();
                                            for( int x = 0; x < numReqs; x++ )
                                            {
                                                IChangeRequest req = subRequests.get(x);
                                                if( req != null)
                                                {
                                                    IRequestValidator validator = new RequestValidator(req);
                                                    handleRequest(validator);
                                                }
                                            }
                                            // final requests should always be empty at this point,
                                            // since we are not the outermost call.
                                        }
                                    }
                                }
                                // Now, restore the silent flag.
                                setSilent(currentSilent);
                            }
                            finally
                            {
                                // THE IMPORTANT DUDE SHOULD DESTRUCT HERE.
                                if (opSigChanges != null)
                                {
                                    int count = opSigChanges.size();
                                    for (int i=0; i<count; i++)
                                    {
                                        IOperationSignatureChangeContextManager mgr = opSigChanges.get(i);
                                        mgr.endSignatureChange();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    protected boolean queryUserBeforeDelete(IOperation pOperation)
    {
        String format = m_Utilities.formatOperation(pOperation);
        return doQuery("DELETE", format);
    }
    
    public void addDependency(IRequestValidator requestValidator, IOperation pOperation)
    {
        if (pOperation != null)
        {
            IParameter pRetParm = pOperation.getReturnType();
            addDependency(requestValidator, pRetParm, pOperation);
        }
    }
    public void addDependency(IRequestValidator requestValidator, IOperation pOperation,
            IClassifier pDependentClass)
    {
        if (pOperation != null)
        {
            ETList<IParameter> parms = pOperation.getParameters();
            if (parms != null)
            {
                Iterator<IParameter> iter = parms.iterator();
                if (iter != null)
                {
                    while (iter.hasNext())
                    {
                        IParameter pItem = iter.next();
                        if (pItem != null)
                            addDependency(requestValidator, pItem, pDependentClass);
                    }
                }
            }
        }
    }
    public void addDependency(IRequestValidator requestValidator, IParameter pParameter,
            IClassifier pDependentClass)
    {
        if (pParameter != null)
        {
            IClassifier pParmType = pParameter.getType();
            if (pParmType != null)
            {
                super.addDependency(requestValidator, pDependentClass, pParmType);
            }
        }
    }
    public void addDependency(IRequestValidator requestValidator, IParameter pParameter,
            IOperation pOperationOfDependentClass)
    {
        if (pOperationOfDependentClass != null)
        {
            IClassifier pOpsClass = pOperationOfDependentClass.getFeaturingClassifier();
            if (pOperationOfDependentClass != null)
                addDependency(requestValidator, pParameter, pOpsClass);
        }
    }
    
    public void deleteList(ETList<IOperation> ops, boolean queryOnce)
    {
        if ( queryOnce )
        {
            IHandlerQuery pQuery = findQuery("DELETE");
            if ( pQuery != null )
                pQuery.reset();
        }
        
        if ( ops != null )
        {
            int count = ops.size();
            int idx = 0;
            while ( idx < count )
            {
                IOperation pOp = (IOperation)ops.get(idx++);
                if ( pOp != null )
                {
                    deleted( pOp );
                    
                    // because we are not going to process a request, we have to
                    // call method hander's delete function first, so that we have
                    // the operation before it was deleted.
                    
                    pOp.delete();
                }
            }
        }
    }
    
    protected IHandlerQuery buildQuery(String key)
    {
        IHandlerQuery query = null;
        if ("DELETE".equals(key))
        {
            boolean deleteDeepDefault = false;
            if (inBatch())
            {
                // We want a dialog with an "apply to all items" button
                query = new ConditionalHandlerQuery(
                        key,
                        RPMessages.getString("IDS_JRT_DELETE_REDEFINING_OPERATIONS_WITHNAME"),
                        RPMessages.getString("IDS_JRT_DELETE_REDEFINING_OPERATIONS_TITLE"),
                        RPMessages.getString("IDS_JRT_DELETE_REDEFINING_OPERATIONS_APPLYALL"),
                        deleteDeepDefault,
                        ErrorDialogIconKind.EDIK_ICONQUESTION,
                        getSilent(),
                        false ); // Not a persistent query, since what kind it
                // is is dependent on mode.
            }
            else
            {
                // Normal query dialog
                query = new HandlerQuery(key,
                        RPMessages.getString("IDS_JRT_DELETE_REDEFINING_OPERATIONS"),
                        RPMessages.getString("IDS_JRT_DELETE_REDEFINING_OPERATIONS_TITLE"),
                        deleteDeepDefault,
                        ErrorDialogIconKind.EDIK_ICONQUESTION,
                        getSilent(),
                        false); // not a persistent query, since what kind it is is dependent on mode.
            }
        }
        else if ("REDEFINE".equals(key))
        {
            boolean makeRedefDefault = true;
            
            query = new HandlerQuery( key,
                    RPMessages.getString("IDS_JRT_DISCOVERED_REDEFINITION"),
                    RPMessages.getString("IDS_JRT_DISCOVERED_REDEFINITION_TITLE"),
                    makeRedefDefault,
                    ErrorDialogIconKind.EDIK_ICONQUESTION,
                    getSilent(),
                    true);
        }
        return query;
    }
    
    public boolean queryBeforeRenameUp(IOperation pOperation, String newName,
            String  textID)
    {
        boolean renameUp = false;
        // This function gets the redifined methods in the base class
        ETList<IOperation> redefinedOps =m_Utilities.collectRedefinedOps(pOperation);
        
        //This function gets the redifined methods in the derived class
        ETList<IOperation> redefiningOps =m_Utilities.collectRedefiningOps(pOperation);
        
        
        if(redefinedOps.getCount()>0 || redefiningOps.getCount()>0)
        {
            try
            {
                // before we query, let's see if pOperation is actually redefining
                // anything.
                if ( redefinedOps != null )
                {
                    int count = redefinedOps.size();
                    if ( count > 0 )
                    {
                        renameUp = true;
                        diagRetval = true ;
                    }
                }
            }
            catch( Exception e )
            {
                Log.stackTrace(e);
            }
        }
        else
        {
            renameUp = discoverRedefinitions(pOperation,(IClassifier)pOperation.getOwner());
        }
        return renameUp;
    }
    public boolean discoverRedefinitions(IOperation pOper, IClassifier pCls)
    {
        boolean byPassThisClass = true;
        boolean buildRedef = false;
        
        ETList<IOperation> discoveredOps = new ETArrayList<IOperation>();
        
        discoveredOps = discoverRedefinitionUp( pOper, pCls, discoveredOps, byPassThisClass );
        
        // Build the redefinitions
        if (discoveredOps != null)
        {
            Iterator<IOperation> iter = discoveredOps.iterator();
            if (iter != null)
            {
                while (iter.hasNext())
                {
                    IOperation oper = iter.next();
                    if (oper != null)
                    {
                        m_Utilities.buildRedefinition(oper, pOper);
                        buildRedef = true;
                    }
                }
            }
        }
        
        discoveredOps = new ETArrayList<IOperation>();
        discoveredOps = discoverRedefinitionDown( pOper, pCls, discoveredOps, byPassThisClass);
        // Build the redefinitions
        if (discoveredOps != null)
        {
            Iterator<IOperation> iter = discoveredOps.iterator();
            if (iter != null)
            {
                while (iter.hasNext())
                {
                    IOperation oper = iter.next();
                    if (oper != null)
                    {
                        m_Utilities.buildRedefinition(pOper, oper);
                        buildRedef = true;
                    }
                }
            }
        }
        
        return buildRedef;
    }
    // Returns true if the operation is a redifined else false
    
    public boolean getDoRedef()
    {
        return doRedef;
    }
    
    // Returns true if all the occurances of operation has to be changed else false
    
    public boolean getDiagRetval()
    {
        return diagRetval;
    }
    
    // Returns true the class in which the operation is present has any inheritance or implementations
    
    public boolean getIsRelated()
    {
        return isRelated;
    }
    public void setIsRelated(boolean setVal)
    {
        isRelated = setVal;
    }
    public void setDiagRetVal(boolean setVal)
    {
        diagRetval = setVal;
    }
}
