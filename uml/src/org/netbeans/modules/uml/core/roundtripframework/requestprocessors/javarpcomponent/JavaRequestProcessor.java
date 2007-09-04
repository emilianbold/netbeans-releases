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

import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.prefs.Preferences;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.IBatchEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IDataType;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationValidatorEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IDerivationClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AttributeTransitionElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.roundtripframework.ChangeKind;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher;
import org.netbeans.modules.uml.core.roundtripframework.RTElementKind;
import org.netbeans.modules.uml.core.roundtripframework.RequestDetailKind;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ErrorDialogIconKind;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.util.NbPreferences;

/**
 */
public class JavaRequestProcessor implements IJavaRequestProcessor
{
    private int m_RPCount;
    
    private IPlugManager m_PlugManager = new PlugManager();
    private ETList<IChangeRequest> m_FinalRequests = new ETArrayList<IChangeRequest>();
    private ILanguage m_Language;
    private IChangeRequest m_ChangeRequest = null;
    
    IJavaChangeHandlerManager m_HandlerManager = new JavaChangeHandlerManager();
    IJavaChangeHandlerUtilities m_Utils = new JavaChangeHandlerUtilities();
    private LinkedBlockingQueue<IChangeRequest> queue = new LinkedBlockingQueue();
    private boolean processing = false;
    
    public JavaRequestProcessor()
    {
        m_HandlerManager.setProcessor(this);
        m_HandlerManager.setPlugManager( m_PlugManager );
        m_HandlerManager.setChangeHandlerUtilities(m_Utils);
    }
    
//    public ETList<IChangeRequest> processRequests(ETList<IChangeRequest> reqs)
//    {
//        ETList<IChangeRequest> requests = null;
//        // If we are blocked, we want to add the input requests onto the
//        // final list of requests to be returned to the RTEventManager to be
//        // forwarded to the listeners (integrations). But we don't want
//        // to process them ourself. This is because WE are responsible the
//        // the changes being generated.  This is and has been the classic
//        // roundtrip/addin problem: Do we block all events in the tool? or
//        // do we just ignore events ourself? Blocking events in wolverine
//        // would be tantamount to neutering the tool, since it is so event
//        // based. So, instead we ignore events at the local level.
//        
//        m_RPCount++;
//        try
//        {
//            if (!m_PlugManager.isPluged())
//            {
//                if ( m_RPCount == 1 )
//                {
//                    // We own it. This is the outermost ProcessRequest call.
//                    int count = reqs.size();
//                    int idx = 0;
//                    
//                    ETList<IChangeRequest> finalInputRequests =
//                            new ETArrayList<IChangeRequest>();
//                    
//                    while ( idx < count )
//                    {
//                        IChangeRequest pItem = reqs.get(idx++);
//                        m_ChangeRequest = pItem;
//                        
//                        if ( pItem != null )
//                        {
//                            IRequestValidator pRequest = new RequestValidator(pItem);
//                            if (!m_Utils.isTemplateClass(pRequest.getRequest()))
//                            {
//                                // Perform all the things that it could be.
//                                if ( preValidationCheck(pRequest))
//                                {
//                                    m_HandlerManager.handleRequest(pRequest);
//                                }
//                                
//                                pRequest.appendRequests(finalInputRequests);
//                            }
//                        }
//                        m_ChangeRequest = null;
//                    }
//                    requests = createOutputRequests(finalInputRequests);
//                }
//                else
//                {
//                    // add the list of input requests onto the final list
//                    // that will be passed back by the innermost call.
//                    appendBlockedRequests(reqs);
//                }
//            }
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();;
//        }
//        
//        m_RPCount--;
//        
//        return requests;
//    }
    
    public ETList<IChangeRequest> processRequests(ETList<IChangeRequest> reqs)
    {
        if (!m_PlugManager.isPluged())
        {
            queue.addAll(reqs);
            
            if (processing)
                return null;
            
            synchronized(this)
            {
                processing = true;
                IChangeRequest request = queue.poll();
                
                while (request != null)
                {
                    if (!m_Utils.isTemplateClass(request))
                    {
                        IRequestValidator pRequest = new RequestValidator(request);
                        if ( preValidationCheck(pRequest))
                        {
                            m_HandlerManager.handleRequest(pRequest);
                        }
                    }
                    request = queue.poll();
                }
                processing = false;
            }
        }

        return null;
    }
        
        
    public void initialize(IRoundTripController controller)
    {
        try
        {
            // Need to register with the roundtrip controller because we are
            // listeners to events
            
            if ( controller != null)
            {
                IElementChangeEventDispatcher pECDispatcher =
                        controller.getElementChangeDispatcher();
                if ( pECDispatcher != null )
                {
                    pECDispatcher.registerForNamedElementEvents(this);
                    pECDispatcher.registerForRedefinableElementModifiedEvents(this);
                    pECDispatcher.registerForNamespaceModifiedEvents(this);
                }
                
                IRelationValidatorEventDispatcher pRELDispatcher =
                        controller.getRelationValidatorDispatcher();
                if ( pRELDispatcher != null )
                {
                    pRELDispatcher.registerForRelationValidatorEvents(this);
                }
                
                IClassifierEventDispatcher pCLDispatcher =
                        controller.getClassifierDispatcher();
                if ( pCLDispatcher != null )
                {
                    pCLDispatcher.registerForFeatureEvents(this);
                    pCLDispatcher.registerForBehavioralFeatureEvents(this);
                    pCLDispatcher.registerForClassifierFeatureEvents(this);
                    pCLDispatcher.registerForAffectedElementEvents(this);
                    pCLDispatcher.registerForTransformEvents(this);
                    // Fix for #5070766
                    pCLDispatcher.registerForAttributeEvents( this );
                }
                
                IElementLifeTimeEventDispatcher pLifeTimeDispatcher =
                        controller.getElementLifeTimeDispatcher();
                if ( pLifeTimeDispatcher != null )
                {
                    pLifeTimeDispatcher.registerForLifeTimeEvents(this);
                }
                
                IRoundTripEventDispatcher pRTDispatcher =
                        controller.getRoundTripDispatcher();
                if ( pRTDispatcher != null )
                {
                    // We only register with the RT dispatcher to get the batches.
                    pRTDispatcher.registerForEventFrameworkEvents(this);
                }
                
                // Register inner processor for dispatch events after actions
                // ( such as FeatureMoved ,....)
                // Fix for #5070768
                DispatchHelper helper = new DispatchHelper();
                helper.registerForClassifierFeatureEvents(new JavaRequestPostProcessor());
                helper.registerForTypedElementEvents(this);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();;
        }
    }
    
    public void deInitialize(IRoundTripController controller)
    {
        try
        {
            if ( controller != null)
            {
                IElementChangeEventDispatcher pECDispatcher =
                        controller.getElementChangeDispatcher();
                if ( pECDispatcher != null )
                {
                    pECDispatcher.revokeNamedElementSink(this);
                    pECDispatcher.revokeRedefinableElementModifiedEvents(this);
                    pECDispatcher.revokeNamespaceModifiedSink(this);
                }
                
                IRelationValidatorEventDispatcher pRELDispatcher =
                        controller.getRelationValidatorDispatcher();
                if ( pRELDispatcher != null )
                {
                    pRELDispatcher.revokeRelationValidatorSink(this);
                }
                
                IClassifierEventDispatcher pCLDispatcher =
                        controller.getClassifierDispatcher();
                if ( pCLDispatcher != null )
                {
                    pCLDispatcher.revokeFeatureSink(this);
                    pCLDispatcher.revokeBehavioralFeatureSink(this);
                    pCLDispatcher.revokeClassifierFeatureSink(this);
                    pCLDispatcher.revokeAffectedElementEvents(this);
                    pCLDispatcher.revokeTransformSink(this);
                }
                
                IElementLifeTimeEventDispatcher pLifeTimeDispatcher =
                        controller.getElementLifeTimeDispatcher();
                if ( pLifeTimeDispatcher != null )
                {
                    pLifeTimeDispatcher.revokeLifeTimeSink(this);
                }
                
                IRoundTripEventDispatcher pRTDispatcher =
                        controller.getRoundTripDispatcher();
                if ( pRTDispatcher != null )
                {
                    pRTDispatcher.revokeEventFrameworkSink(this);
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();;
        }
    }
    
    /**
     *
     * Retrieves the language this processor supports.
     *
     * @return lang The language supported. This will always be "java" for this
     *              processor.
     */
    public String getLanguage()
    {
        return "Java";
    }
    
    /**
     *
     * Retrieves the language this processor supports.
     *
     * @return pLang[out] The actual ILanguage associated with this processor
     */
    public ILanguage getLanguage2()
    {
        try
        {
            if ( m_Language == null )
            {
                String mylang = getLanguage();
                
                ICoreProduct pProduct = ProductRetriever.retrieveProduct();
                
                if ( pProduct != null )
                {
                    ILanguageManager pManager =
                            pProduct.getLanguageManager();
                    
                    if ( pManager != null )
                    {
                        m_Language = pManager.getLanguage(mylang);
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return m_Language;
    }
    
    
    public void onPreNameModified(INamedElement element, String name,
            IResultCell cell)
    {
        try
        {
            boolean isLanguage = checkIfCorrectLanguage(element);
            if (isLanguage)
            {
                if ( checkForInvalidName(element, name) )
                {
                    // DENY
                    cell.setContinue(false);
                }
                else
                {
                    if( element != null)
                    {
                        IStructuralFeature structFeat = element instanceof IStructuralFeature? (IStructuralFeature) element : null;
                        
                        if (structFeat != null)
                        {
                            IClassifier classifier = structFeat.getFeaturingClassifier();
                            
                            if( classifier != null)
                            {
                                if( !ensureUniqueAttribute(classifier, name) )
                                {
                                    deny(cell, RPMessages.getString("IDS_JRT_ATTR_NAME_NOT_UNIQUE"),
                                            RPMessages.getString("IDS_JRT_ATTR_NAME_NOT_UNIQUE_TITLE"));
                                }
                            }
                        }
                    }
                }
            }
            
            if (element instanceof IDataType && ! Util.isTypeCompatibleWithElementSources(name, element, true))
            {
                cell.setContinue(false);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void onNameModified(INamedElement element, IResultCell cell)
    {
        //C++ method is empty
    }
    
    
    public void onPreVisibilityModified(INamedElement element,
            int kind, IResultCell cell)
    {
        boolean denied = false;
        
        if (element != null)
        {
            try
            {
                boolean isLanguage = checkIfCorrectLanguage(element);
                if (isLanguage)
                {
                    // If the element is an attribute of an interface, it cannot be made non-public.
                    
                    if ( kind != IVisibilityKind.VK_PUBLIC )
                    {
                        IAttribute pAttr = element instanceof IAttribute? (IAttribute) element : null;
                        if ( pAttr != null )
                        {
                            IClassifier pClass = m_Utils.getClassOfAttribute(pAttr);
                            if ( pClass != null )
                            {
                                String elementType = pClass.getElementType();
                                if ( elementType != null && elementType.equals("Interface") )
                                {
                                    // DENY
                                    
                                    
                                    deny( cell, RPMessages.getString("IDS_JRT_INTERFACE_ATTRIBUTE_DENY"),
                                            RPMessages.getString("IDS_JRT_INTERFACE_ATTRIBUTE_TITLE"));
                                    denied = true;
                                }
                            }
                        }
                    }
                    
                    if ( !denied && kind == IVisibilityKind.VK_PRIVATE )
                    {
                        // Operations that are redefined cannot be made private
                        IOperation pOp = element instanceof IOperation? (IOperation) element : null;
                        if ( pOp != null )
                        {
                            ETList<IOperation> redefining = m_Utils.collectRedefiningOps(pOp);
                            
                            int count = 0;
                            if ( redefining != null )
                            {
                                count = redefining.size();
                            }
                            if ( count > 0 )
                            {
                                // DENY
                                
                                
                                deny(cell, RPMessages.getString("IDS_JRT_PRIVATE_REDEFINED_OP_DENY"),
                                        RPMessages.getString("IDS_JRT_PRIVATE_REDEFINED_OP_TITLE"));
                                denied = true;
                            }
                        }
                    }
                    
                    // Operations that are redefining something cannot be made "less visible"
                    // in Java. Note, this is different than C++ where you made a virtual method
                    // "final" by making it private.
                    
                    IOperation pOp = element instanceof IOperation? (IOperation) element : null;
                    if ( !denied && pOp != null )
                    {
                        int currentVis = pOp.getVisibility();
                        
                        // if the new visibility is "less" (quick check)
                        boolean isLess = m_Utils.isVisibilityLess(kind, currentVis);
                        
                        if ( isLess )
                        {
                            ETList<IOperation> redefined = m_Utils.collectRedefinedOps(pOp);
                            isLess = false;
                            
                            int count = 0;
                            if ( redefined != null )
                            {
                                count = redefined.size();
                                // should only be one
                                if ( count > 0 )
                                {
                                    IOperation pBaseOp = redefined.get(0);
                                    
                                    if ( pBaseOp != null )
                                    {
                                        int baseVis = pBaseOp.getVisibility();
                                        
                                        // Is the new vis less than the base vis
                                        isLess = m_Utils.isVisibilityLess(kind, baseVis);
                                    }
                                }
                            }
                            if ( isLess )
                            {
                                // DENY
                                
                                deny(cell, RPMessages.getString("IDS_JRT_VISIBILITY_REDEFINING_OP_DENY"),
                                        RPMessages.getString("IDS_JRT_VISIBILITY_REDEFINING_OP_TITLE"));
                                denied = true;
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
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onVisibilityModified(INamedElement element, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreAliasNameModified(INamedElement element,
            String proposedName, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onAliasNameModified(INamedElement element, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreNameCollision(INamedElement element, String proposedName, ETList<INamedElement> els, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onNameCollision(INamedElement element, ETList<INamedElement> els, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreStaticModified(IFeature feat, boolean flag,
            IResultCell cell)
    {
        if (feat != null)
        {
            try
            {
                boolean isLanguage = checkIfCorrectLanguage(feat);
                if (isLanguage)
                {
                    // Attributes of interfaces must remain static
                    if (flag == false)
                    {
                        IAttribute pAttr = feat instanceof IAttribute? (IAttribute) feat : null;
                        if ( pAttr != null )
                        {
                            IClassifier pClass = m_Utils.getClassOfAttribute(pAttr);
                            
                            if ( pClass != null )
                            {
                                IInterface pInterface = pClass instanceof IInterface? (IInterface) pClass : null;
                                if ( pInterface != null )
                                {
                                    // DENY
                                    deny(cell, RPMessages.getString("IDS_JRT_INTERFACE_ATTRIBUTE_DENY"),
                                            RPMessages.getString("IDS_JRT_INTERFACE_ATTRIBUTE_TITLE"));
                                }
                            }
                        }
                    }
                    else
                    {
                        // Operations that are redefined or redefining cannot be made static
                        IOperation pOp = feat instanceof IOperation? (IOperation) feat : null;
                        if ( pOp != null )
                        {
                            ETList<IOperation> redefined = m_Utils.collectRedefinedOps(pOp);
                            ETList<IOperation> redefining = m_Utils.collectRedefiningOps(pOp);
                            
                            int count1 = 0;
                            int count2 = 0;
                            if ( redefined != null )
                            {
                                count1 = redefined.size();
                            }
                            if ( redefining != null )
                            {
                                count2 = redefining.size();
                            }
                            
                            if ( count1 > 0 || count2 > 0 )
                            {
                                // DENY
                                deny(cell, RPMessages.getString("IDS_JRT_STATIC_REDEFINED_OP_DENY"),
                                        RPMessages.getString("IDS_JRT_STATIC_REDEFINED_OP_TITLE"));
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
    }
    
    public void onStaticModified(IFeature feature, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreNativeModified(IFeature feature, boolean proposedValue,
            IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onNativeModified(IFeature feature, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onConcurrencyPreModified(IBehavioralFeature feature,
            int proposedValue, IResultCell cell)
    {
        // C++ method is empty
    }
    
    
    public void onConcurrencyModified(IBehavioralFeature feature,
            IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreHandledSignalAdded(IBehavioralFeature feature,
            ISignal proposedValue, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onHandledSignalAdded(IBehavioralFeature feature,
            IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreHandledSignalRemoved(IBehavioralFeature feature,
            ISignal proposedValue, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onHandledSignalRemoved(IBehavioralFeature feature,
            IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreParameterAdded(IBehavioralFeature feature,
            IParameter parm, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onParameterAdded(IBehavioralFeature feature, IParameter parm,
            IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreParameterRemoved(IBehavioralFeature feature,
            IParameter parm, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onParameterRemoved(IBehavioralFeature feature, IParameter parm,
            IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreAbstractModified(IBehavioralFeature feat,
            boolean flag, IResultCell cell)
    {
        if (feat != null)
        {
            try
            {
                boolean isLanguage = checkIfCorrectLanguage(feat);
                if(isLanguage)
                {
                    // If the feature is part of an interface, disallow
                    if (!flag)
                    {
                        IClassifier pClass = feat.getFeaturingClassifier();
                        if ( pClass != null )
                        {
                            String elementType = pClass.getElementType();
                            
                            if ( elementType != null && elementType.equals("Interface") )
                            {
                                // DENY
                                deny(cell, RPMessages.getString("IDS_JRT_ABSTRACT_OPERATION_DENY"),
                                        RPMessages.getString("IDS_JRT_ABSTRACT_OPERATION_TITLE"));
                            }
                        }
                    }
                    else
                    {
                        // Abstract and final are mutually exclusive.
                        IRedefinableElement pRedef = feat instanceof IRedefinableElement? (IRedefinableElement) feat : null;
                        if ( pRedef != null )
                        {
                            boolean isFinal = pRedef.getIsFinal();
                            if ( isFinal )
                            {
                                // DENY
                                deny(cell, RPMessages.getString("IDS_JRT_FINAL_ABSTRACT_OPERATION_DENY"),
                                        RPMessages.getString("IDS_JRT_FINAL_ABSTRACT_OPERATION_TITLE"));
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
    }
    
    public void onAbstractModified(IBehavioralFeature feature, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreStrictFPModified(IBehavioralFeature feature,
            boolean proposedValue, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onStrictFPModified(IBehavioralFeature feature, IResultCell cell)
    {
        // C++ method is empty
    }
    
    //	----------------------------------------------------------------------------
    //	----------------------------------------------------------------------------
    //	IClassifierFeatureEventsSink
    //	----------------------------------------------------------------------------
    //	----------------------------------------------------------------------------
    
    public void onFeaturePreAdded(IClassifier classifier, IFeature feat,
            IResultCell cell)
    {
        if (classifier != null && feat != null)
        {
            try
            {
                // First check for valid name
                if ( checkForInvalidName(feat) )
                {
                    // DENY
                    cell.setContinue(false);
                }
                else
                {
                    IStructuralFeature structFeat = feat instanceof IStructuralFeature? (IStructuralFeature) feat : null;
                    
                    boolean proceed = true;
                    if (structFeat != null)
                    {
                        String name = structFeat.getName();
                        if(!(feat instanceof AttributeTransitionElement))
                            proceed = ensureUniqueAttribute(classifier, name);
                        
                        if( !proceed )
                        {
                            deny(cell, RPMessages.getString("IDS_JRT_ATTR_NAME_NOT_UNIQUE"),
                                    RPMessages.getString("IDS_JRT_ATTR_NAME_NOT_UNIQUE_TITLE"));
                        }
                    }
                    
                    if( proceed )
                    {
                        // If the classifier is an interface and the feature is a behavioral feature,
                        // make sure it is abstract.
                        
                        String elementType = classifier.getElementType();
                        
                        if ( elementType.equals("Interface") )
                        {
                            IOperation pOp = feat instanceof IOperation? (IOperation) feat : null;
                            if(pOp != null)
                                pOp.setIsAbstract(true);
                            
                            
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public void onFeatureAdded(IClassifier classifier, IFeature feature,
            IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onEnumerationLiteralPreAdded(
            IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
    {
        // TODO
    }
    
    public void onEnumerationLiteralAdded(
            IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
    {
        // C++ method is empty
    }
    
    
    public void onFeaturePreRemoved(IClassifier classifier, IFeature feature,
            IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onFeatureRemoved(IClassifier classifier, IFeature feature,
            IResultCell cell)
    {
        // C++ method is empty
    }
    
    /*
     *
     * @author ads
     * Fix for #5083683
     */
    public void onFeaturePreMoved(IClassifier classifier, IFeature feature,
            IResultCell cell)
    {
        IStructuralFeature structFeat = feature instanceof IStructuralFeature? (IStructuralFeature) feature : null;
        
        boolean proceed = true;
        if (structFeat != null)
        {
            String name = structFeat.getName();
            if(!(feature instanceof AttributeTransitionElement))
                proceed = ensureUniqueAttribute(classifier, name);
            
            if( !proceed )
            {
                deny(cell, RPMessages.getString("IDS_JRT_ATTR_NAME_NOT_UNIQUE"),
                        RPMessages.getString("IDS_JRT_ATTR_NAME_NOT_UNIQUE_TITLE"));
            }
        }
    }
    
    public void onFeatureMoved(IClassifier classifier, IFeature feature,
            IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onFeaturePreDuplicatedToClassifier(IClassifier classifier,
            IFeature feature, IResultCell cell)
    {
        // C++ method is empty
    }
    
    
    public void onFeatureDuplicatedToClassifier(IClassifier pOldClassifier,
            IFeature pOldFeature, IClassifier pNewClassifier,
            IFeature pNewFeature, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreAbstractModified(IClassifier classifier,
            boolean flag, IResultCell cell)
    {
        if (classifier != null)
        {
            try
            {
                boolean isLanguage = checkIfCorrectLanguage(classifier);
                if(isLanguage)
                {
                    // If the classifier is an interface, disallow
                    if (!flag)
                    {
                        String elementType = classifier.getElementType();
                        
                        if ( elementType != null && elementType.equals("Interface") )
                        {
                            // DENY
                            deny(cell, RPMessages.getString("IDS_JRT_ABSTRACT_INTERFACE_DENY"),
                                    RPMessages.getString("IDS_JRT_ABSTRACT_INTERFACE_TITLE"));
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public void onAbstractModified(IClassifier feature, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreLeafModified(IClassifier feature, boolean proposedValue,
            IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onLeafModified(IClassifier feature, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreTransientModified(IClassifier feature,
            boolean proposedValue, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onTransientModified(IClassifier feature, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreTemplateParameterAdded(IClassifier pClassifier,
            IParameterableElement pParam, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onTemplateParameterAdded(IClassifier pClassifier,
            IParameterableElement pParam, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreTemplateParameterRemoved(IClassifier pClassifier,
            IParameterableElement pParam, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onTemplateParameterRemoved(IClassifier pClassifier,
            IParameterableElement pParam, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreRelationValidate(IRelationProxy proxy, IResultCell cell)
    {
        if (proxy != null)
        {
            try
            {
                IElement pFrom = proxy.getFrom();
                
                boolean isLanguage = checkIfCorrectLanguage(pFrom);
                if (isLanguage)
                {
                    boolean valid = true;
                    
                    // This is the code where we would prevent a java class from
                    // extending (generalizing) from more than 1 class.
                    
                    RequestProcessorUtilities utils = new RequestProcessorUtilities();
                    String relationType = utils.getRelationType(proxy);
                    
                    if ( relationType != null && relationType.equals("Generalization") )
                    {
                        // get the "from" type
                        
                        IElement pCandidateFrom = proxy.getFrom();
                        String elementType = null;
                        if (pCandidateFrom != null)
                            elementType = pCandidateFrom.getElementType();
                        
                        // get the "to" type
                        IElement pCandidateTo = proxy.getTo();
                        
                        String toType = null;
                        if (pCandidateTo != null)
                            toType = pCandidateTo.getElementType();
                        
                        // invalidate certain generalizations
                        if ( elementType != null && elementType.equals("Class") )
                        {
                            if ( toType != null && toType.equals("Interface") )
                            {
                                proxy.setConnectionElementType("Implementation");
                            }
                            else
                            {
                                IClassifier pDerivedClass = pCandidateFrom instanceof IClassifier? (IClassifier) pCandidateFrom : null;
                                if ( pDerivedClass != null )
                                {
                                    // Make sure that this class does not generalize off any others
                                    ETList<IGeneralization> pExistingGeneralizationList =
                                            pDerivedClass.getGeneralizations();
                                    
                                    if ( pExistingGeneralizationList != null )
                                    {
                                        int count = pExistingGeneralizationList.size();
                                        int idx = 0;
                                        
                                        while ( idx < count && valid)
                                        {
                                            IGeneralization pExistingGeneralization =
                                                    pExistingGeneralizationList.get(idx);
                                            idx++;
                                            if ( pExistingGeneralization != null )
                                            {
                                                // get the TO element off this generalization and compare it to
                                                // the candidate TO element off the proxy
                                                
                                                IClassifier pExistingTo = pExistingGeneralization.getGeneral();
                                                
                                                if ( pExistingTo != null )
                                                {
                                                    boolean isSame = pExistingTo.isSame(pCandidateTo);
                                                    if (!isSame)
                                                    {
                                                        // This is illegal in Java
                                                        valid = false;
                                                        
                                                        
                                                        sendErrorMessage(RPMessages.getString("IDS_JRT_GENERALIZATION_INVALID"),
                                                                RPMessages.getString("IDS_JRT_INVALID_GENERALIZATION_TITLE"));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else if ( elementType != null && elementType.equals("Interface") )
                        {
                            // make sure that an interface is only deriving from another interface
                            if ( toType != null && !(toType.equals("Interface")) )
                            {
                                // 95999, check for template type
                                if (pCandidateTo instanceof IDerivationClassifier)
                                {
                                    if (!"Interface".equals(((IDerivationClassifier)pCandidateTo).getTemplate().getElementType()))
                                        valid = false;
                                }
                                else
                                {
                                    // This is illegal in Java
                                    valid = false;
                                }
                                
                                if (!valid)
                                    sendErrorMessage(RPMessages.getString("IDS_JRT_GENERALIZATION_INTERFACE_INVALID"),
                                            RPMessages.getString("IDS_JRT_INVALID_GENERALIZATION_TITLE"));
                            }
                        }
                    }
                    else if ( relationType != null && relationType.equals("Implementation") )
                    {
                        // get the "from" type
                        IElement pCandidateFrom = proxy.getFrom();
                        
                        IClassifier pClassifier = pCandidateFrom instanceof IClassifier ? (IClassifier)pCandidateFrom  : null;
                        
                        // Get the "to" type
                        IElement pCandidateTo = proxy.getTo();
                        
                        IInterface pInterface = pCandidateTo instanceof IInterface ? (IInterface)pCandidateTo : null;
                        
                        // Make sure that the interface has not been implemented yet.
                        if (pInterface != null && pClassifier != null &&  checkIfAlreadyImpemented(pInterface, pClassifier))
                        {
                            //C++ content is commented out.
                        }
                    }
                    
                    if (!valid)
                    {
                        // I checked for false here so that we only set when we determine
                        // it to be invalid. This way, we don't inadvertantly set an invalid
                        // back to valid.
                        
                        cell.setContinue(valid);
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    protected boolean checkIfAlreadyImpemented(IInterface pInterface, IClassifier pClassifier)
    {
        //AZTEC: NOTE: Since this method has not been used for any purpose,
        // its not implemented as of now
        return false;
    }
    
    protected boolean isParentInterface(IInterface pInterface, IInterface pWantedInterface)
    {
        //AZTEC: NOTE: this method is being called only from checkIfAlreadyImpemented().
        //It is needed only when checkIfAlreadyImpemented() is implemented.
        return false;
    }
    
    public void onRelationValidated(IRelationProxy payload, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreFinalModified(IRedefinableElement element,
            boolean newVal, IResultCell cell)
    {
        if (element != null)
        {
            try
            {
                // An operation cannot be final if it is currently abstract,
                // nor can it be final if it is already redefined.
                
                if ( newVal )
                {
                    IOperation pOp = element instanceof IOperation? (IOperation) element : null;
                    if ( pOp != null )
                    {
                        boolean isAbstract = pOp.getIsAbstract();
                        if (isAbstract)
                        {
                            // DENY
                            
                            deny(cell, RPMessages.getString("IDS_JRT_FINAL_ABSTRACT_OPERATION_DENY"),
                                    RPMessages.getString("IDS_JRT_FINAL_ABSTRACT_OPERATION_TITLE"));
                        }
                        else
                        {
                            // is this operation already redefined?
                            
                            boolean redefined = false;
                            ETList<IRedefinableElement> opList =
                                    pOp.getRedefiningElements();
                            
                            if ( opList != null )
                            {
                                if ( opList.size() > 0 )
                                {
                                    redefined = true;
                                }
                            }
                            
                            if ( redefined )
                            {
                                // DENY
                                
                                deny( cell, RPMessages.getString("IDS_JRT_FINAL_REDEFINED_OPERATION_DENY"),
                                        RPMessages.getString("IDS_JRT_FINAL_REDEFINED_OPERATION_TITLE"));
                            }
                        }
                    }
                }
                else
                {
                    // Attributes of interfaces must remain final
                    IAttribute pAttr = element instanceof IAttribute? (IAttribute) element : null;
                    if ( pAttr != null )
                    {
                        IClassifier pClass = m_Utils.getClassOfAttribute(pAttr);
                        String elementType = pClass.getElementType();
                        if ( elementType != null && elementType.equals("Interface") )
                        {
                            // DENY
                            deny(cell, RPMessages.getString("IDS_JRT_INTERFACE_ATTRIBUTE_DENY"), RPMessages.getString("IDS_JRT_INTERFACE_ATTRIBUTE_TITLE"));
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public void onFinalModified(IRedefinableElement element, IResultCell cell)
    {
        // C++ method is empty
    }
    
    
    public void onPreRedefinedElementAdded(
            IRedefinableElement redefiningElement,
            IRedefinableElement redefinedElement, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onRedefinedElementAdded(IRedefinableElement redefiningElement,
            IRedefinableElement redefinedElement, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreRedefinedElementRemoved(
            IRedefinableElement redefiningElement,
            IRedefinableElement redefinedElement, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onRedefinedElementRemoved(
            IRedefinableElement redefiningElement,
            IRedefinableElement redefinedElement, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreRedefiningElementAdded(
            IRedefinableElement redefinedElement,
            IRedefinableElement redefiningElement, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onRedefiningElementAdded(IRedefinableElement redefinedElement,
            IRedefinableElement redefiningElement, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreRedefiningElementRemoved(
            IRedefinableElement redefinedElement,
            IRedefinableElement redefiningElement, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onRedefiningElementRemoved(
            IRedefinableElement redefinedElement,
            IRedefinableElement redefiningElement, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreTransform(IClassifier classifier, String newForm, IResultCell cell)
    {
        if (classifier != null)
        {
            try
            {
                // If the classifier is a class that is being transformed to an interface
                // or actor, we have to warn the user that code will be lost.
                
                // If the class/interface is in any generalizations or implementations,
                // it cannot be transform.
                
                ETList<IGeneralization> gens = classifier.getGeneralizations();
                ETList<IImplementation> impls = classifier.getImplementations();
                ETList<IGeneralization> specs = classifier.getSpecializations();
                ETList<IClassifier> classes = m_Utils.getImplementingClassifiers(classifier);
                
                boolean allowed = true;
                if ( gens != null )
                {
                    if ( gens.size() > 0 )
                    {
                        allowed = false;
                    }
                }
                if ( allowed && impls != null )
                {
                    if ( impls.size() > 0 )
                    {
                        allowed = false;
                    }
                }
                if ( allowed && specs != null )
                {
                    if ( specs.size() > 0 )
                    {
                        allowed = false;
                    }
                }
                if ( allowed && classes != null )
                {
                    if ( classes.size() > 0 )
                    {
                        allowed = false;
                    }
                }
                
                if (!allowed)
                {
                    // DENY
                    deny(cell, RPMessages.getString("IDS_JRT_TRANSFORM_DENIED"),
                            RPMessages.getString("IDS_JRT_TRANSFORM_DENIED_TITLE"));
                }
                
                else
                {
                    Preferences prefs = NbPreferences.forModule (DummyCorePreference.class) ;
                    String str = prefs.get ("UML_ShowMe_Dont_Show_Filter_Warning_Dialog", "PSK_ASK") ;
                    
                    if (str != null && str.equals("PSK_NEVER"))
                    {
                        cell.setContinue(false);
                        return;
                    }
                    
                    if (str != null && str.equals("PSK_ALWAYS"))
                        return;
                    
                    // We are allowed to do the transform, but the user might
                    // not want to lose code.
                    
                    boolean checkWithUser = false;
                    
                    if (!m_Utils.isElementUnnamed(classifier))
                    {
                        String oldForm = classifier.getElementType();
                        String newFormTest = newForm;
                        
                        String message = RPMessages.getString("IDS_JRT_TRANSFORM_UNKNOWN_CONFIRM");
                        
                        if (newFormTest != null && oldForm != null)
                        {
                            // Display appropriate message when transforming from IF to A/D/E
                            // and Class to I/A/D/E. Fix for bug 5105839.
                            if ( oldForm.equals("Interface") )
                            {
                                if ( newFormTest.equals("Actor") )
                                {
                                    checkWithUser = false;
                                    //                                    checkWithUser = true;
                                    //                                    message = RPMessages.getString("IDS_JRT_TRANSFORM_IA_CONFIRM");
                                }
                                else if ( newFormTest.equals("DataType") )
                                {
                                    checkWithUser = false;
                                    //                                    checkWithUser = true;
                                    //                                    message = RPMessages.getString("IDS_JRT_TRANSFORM_ID_CONFIRM");
                                }
                                else if ( newFormTest.equals("Enumeration") )
                                {
                                    checkWithUser = true;
                                    message = RPMessages.getString("IDS_JRT_TRANSFORM_IE_CONFIRM");
                                }
                            }
                            else if ( oldForm.equals("Class") )
                            {
                                if ( newFormTest.equals("Interface") )
                                {
                                    checkWithUser = false;
                                    //                                    checkWithUser = true;
                                    //                                    message = RPMessages.getString("IDS_JRT_TRANSFORM_CI_CONFIRM");
                                }
                                else if ( newFormTest.equals("Actor") )
                                {
                                    checkWithUser = false;
                                    //                                    checkWithUser = true;
                                    //                                    message = RPMessages.getString("IDS_JRT_TRANSFORM_CA_CONFIRM");
                                }
                                else if ( newFormTest.equals("DataType") )
                                {
                                    checkWithUser = false;
                                    //                                    checkWithUser = true;
                                    //                                    message = RPMessages.getString("IDS_JRT_TRANSFORM_CD_CONFIRM");
                                }
                                else if ( newFormTest.equals("Enumeration") )
                                {
                                    checkWithUser = true;
                                    message = RPMessages.getString("IDS_JRT_TRANSFORM_CE_CONFIRM");
                                }
                            }
                            else if (oldForm.equals("Enumeration"))
                            {
                                if ( newFormTest.equals("Actor") )
                                {
                                    checkWithUser = false;
                                    //                                    checkWithUser = true;
                                    //                                    message = RPMessages.getString("IDS_JRT_TRANSFORM_EA_CONFIRM");
                                }
                                else if ( newFormTest.equals("DataType") )
                                {
                                    checkWithUser = false;
                                    //                                    checkWithUser = true;
                                    //                                    message = RPMessages.getString("IDS_JRT_TRANSFORM_ED_CONFIRM");
                                }
                                else if ( newFormTest.equals("Interface") )
                                {
                                    checkWithUser = true;
                                    message = RPMessages.getString("IDS_JRT_TRANSFORM_EI_CONFIRM");
                                }
                                else if ( newFormTest.equals("Class") )
                                {
                                    checkWithUser = true;
                                    message = RPMessages.getString("IDS_JRT_TRANSFORM_EC_CONFIRM");
                                }
                            }
                        }
                        
                        String showMePref = prefs.get ("UML_ShowMe_Transform_When_Elements_May_Be_Lost", "PSK_ASK") ;
                        if (showMePref.equals ("PSK_NEVER")) {
                            cell.setContinue(false);
                            checkWithUser = false;
                        } else if (showMePref.equals ("PSK_ALWAYS")) {
                            checkWithUser = false;
                        } 
                        
                        if ( checkWithUser )
                        {
                            IQuestionDialog pDiag = new SwingQuestionDialogImpl();
                            
                            if ( pDiag != null )
                            {
                                
                                String title = RPMessages.getString("IDS_JRT_TRANSFORM_CONFIRM_TITLE");
                                
                                int defaultresult = SimpleQuestionDialogResultKind.SQDRK_RESULT_YES;
                                int result = SimpleQuestionDialogResultKind.SQDRK_RESULT_YES;
                                
                                QuestionResponse qr = pDiag.displaySimpleQuestionDialogWithCheckbox(
                                        SimpleQuestionDialogKind.SQDK_YESNO,
                                        ErrorDialogIconKind.EDIK_ICONQUESTION,
                                        message,
                                        "",
                                        title,
                                        defaultresult,
                                        false);
                                
                                int  dlgAnswer = qr.getResult();
                                if ( dlgAnswer == SimpleQuestionDialogResultKind.SQDRK_RESULT_NO )
                                {
                                    // DENIED BY USER
                                    cell.setContinue(false);
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
    }
    
    
    public void onTransformed(IClassifier classifier, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onElementPreCreate(String ElementType, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onElementCreated(IVersionableElement element, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onElementPreDelete(IVersionableElement element, IResultCell cell)
    {
        try
        {
            if ( element != null )
            {
                boolean checkWithUser = false;
                
                IPackage pPackage = element instanceof IPackage? (IPackage) element : null;
                if ( pPackage != null )
                {
                    if(!pPackage.getOwner().isDeleted())
                        checkWithUser = true;
                }
                
                if ( checkWithUser )
                {
                    IQuestionDialog pDiag = new SwingQuestionDialogImpl();
                    if ( pDiag != null )
                    {
                        
                        String message = RPMessages.getString("IDS_JRT_PACKAGE_DELETE");
                        String title = RPMessages.getString("IDS_JRT_PACKAGE_DELETE_TITLE");
                        
                        int defaultresult = SimpleQuestionDialogResultKind.SQDRK_RESULT_YES;
                        QuestionResponse result = pDiag.displaySimpleQuestionDialogWithCheckbox(
                                SimpleQuestionDialogKind.SQDK_YESNO,
                                ErrorDialogIconKind.EDIK_ICONWARNING,
                                message,
                                "",
                                title,
                                defaultresult,
                                false);
                        
                        if ( result.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_NO )
                        {
                            // DENIED BY USER
                            cell.setContinue(false);
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
    
    public void onElementDeleted(IVersionableElement element, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onElementPreDuplicated(IVersionableElement element,
            IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onElementDuplicated(IVersionableElement element,
            IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreImpacted(IClassifier classifier, ETList<IVersionableElement> impacted, IResultCell cell ) {
        boolean isLanguage = checkIfCorrectLanguage(classifier);
        try {
            if (isLanguage) {
                if ( impacted != null ) {
                    int count = impacted.size();
                    
                    
                    int value = 50; //kris richards - removed LargeImpact pref. Hard coded to 50.
                    
                    if ( count > value ) {
                        IQuestionDialog pDiag = new SwingQuestionDialogImpl();
                        if ( pDiag != null ) {
                            String message = RPMessages.getString("IDS_JRT_LARGE_IMPACT");
                            String title = RPMessages.getString("IDS_JRT_LARGE_IMPACT_TITLE");
                            
                            int defaultresult = SimpleQuestionDialogResultKind.SQDRK_RESULT_YES;
                            QuestionResponse result = pDiag.displaySimpleQuestionDialogWithCheckbox(
                                    SimpleQuestionDialogKind.SQDK_YESNO,
                                    ErrorDialogIconKind.EDIK_ICONQUESTION,
                                    message,
                                    "",
                                    title,
                                    defaultresult,
                                    false);
                            
                            if ( result.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_NO ) {
                                // DENIED BY USER
                                cell.setContinue(false);
                            }
                        }
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void onImpacted(IClassifier classifier, ETList<IVersionableElement> el, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreEventContextPushed(IEventContext pContext,
            IResultCell pCell)
    {
        // C++ method is empty
    }
    
    public void onEventContextPushed(IEventContext pContext, IResultCell pCell)
    {
        if (pContext != null)
        {
            try
            {
                IBatchEventContext pBatch = pContext instanceof IBatchEventContext? (IBatchEventContext) pContext : null;
                if ( pBatch != null )
                {
                    // Is this the batch that roundtrip creates when it is processing requests?
                    // If so, we are not concerned with this batch. We are only interested
                    // in batches started by the tool itself.
                    
                    String batchName = pBatch.getName();
                    if ( batchName != null && batchName.length() > 0 &&
                            batchName.equals("Processing Requests") )
                    {
                        // this is the one we DON'T care about
                    }
                    else
                    {
                        // Tell the handler manager about this one.
                        m_HandlerManager.startBatch();
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public void onPreEventContextPopped(IEventContext pContext,
            IResultCell pCell)
    {
        // C++ method is empty
    }
    
    public void onEventContextPopped(IEventContext pContext, IResultCell pCell)
    {
        if (pContext != null)
        {
            try
            {
                IBatchEventContext pBatch = pContext instanceof IBatchEventContext? (IBatchEventContext) pContext : null;
                if ( pBatch != null )
                {
                    // Is this the batch that roundtrip creates when it is processing requests?
                    // If so, we are not concerned with this batch. We are only interested
                    // in batches started by the tool itself.
                    
                    String batchName = pBatch.getName();
                    if ( batchName != null && batchName.length() > 0 &&
                            batchName.equals("Processing Requests") )
                    {
                        // this is the one we DON'T care about
                    }
                    else
                    {
                        // Tell the handler manager about this one.
                        m_HandlerManager.endBatch();
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public void onEventDispatchCancelled(ETList<Object> objs, Object foo, IResultCell cell)
    {
        // C++ method is empty
    }
    
    public void onPreElementAddedToNamespace(INamespace space,
            INamedElement elementToAdd, IResultCell cell)
    {
        if (space != null && elementToAdd != null)
        {
            if (space instanceof IPackage)
            {
                IPackage pack = (IPackage) space;
                if (! Util.isTypeCompatibleWithPackageSource(pack, elementToAdd, true))
                {
                    cell.setContinue(false);
                    return;
                }
            }
        }
    }
    
    public void onElementAddedToNamespace(INamespace space,
            INamedElement elementAdded, IResultCell cell)
    {
        // C++ method is empty
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultPreModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultPreModified( IAttribute attr, IExpression proposedValue, IResultCell cell )
    {
        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultModified( IAttribute attr, IResultCell cell )
    {
        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPreDefaultBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDefaultBodyModified( IAttribute feature, String bodyValue, IResultCell cell )
    {
        IClassifier classifier = feature.getFeaturingClassifier();
        boolean isLanguage = checkIfCorrectLanguage(feature);
        if ( isLanguage && ( classifier instanceof IInterface ))
        {
            if ( ( bodyValue == null ) || ( bodyValue.length()==0))
            {
                //deny( cell , "" , "" );
                sendErrorMessage( RPMessages.getString("IDS_JRT_ATTRIBUTE_INTERFACE_EMPTY") ,
                        RPMessages.getString("IDS_JRT_ATTRIBUTE_INTERFACE_EMPTY_TITLE") );
                cell.setContinue(false);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultBodyModified( IAttribute feature, IResultCell cell )
    {
        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPreDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDefaultLanguageModified( IAttribute feature, String language, IResultCell cell )
    {
        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultLanguageModified( IAttribute feature, IResultCell cell )
    {
        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPreDerivedModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDerivedModified( IAttribute feature, boolean proposedValue, IResultCell cell )
    {
        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDerivedModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDerivedModified( IAttribute feature, IResultCell cell )
    {
        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPrePrimaryKeyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPrePrimaryKeyModified( IAttribute feature, boolean proposedValue, IResultCell cell )
    {
        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPrimaryKeyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPrimaryKeyModified( IAttribute feature, IResultCell cell )
    {
        
    }
    
    /**
     * Fired whenever the Multiplicity object on a particular element is about to be modified.
     */
    public void onPreMultiplicityModified( ITypedElement element, IMultiplicity proposedValue, IResultCell cell )
    {
    }
    
    /**
     * Fired whenever the Multiplicity object on a particular element was just modified.
     */
    public void onMultiplicityModified( ITypedElement element, IResultCell cell )
    {}
    
    /**
     * Fired whenever the type on a particular element is about to be modified.
     */
    public void onPreTypeModified( ITypedElement element, IClassifier proposedValue, IResultCell cell )
    {
        if (! Util.isTypeCompatibleWithElementSources(proposedValue, element, true))
        {
            cell.setContinue(false);
        }
    }
    
    /**
     * Fired whenever the type flag on a particular element was just modified.
     */
    public void onTypeModified( ITypedElement element, IResultCell cell )
    {
        //all the needed actions has been taken cared of else where.
    }
    
    /**
     * Fired when the lower property on the passed in range is about to be modified.
     */
    public void onPreLowerModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, String proposedValue, IResultCell cell )
    {}
    
    /**
     * Fired when the lower property on the passed in range was modified.
     */
    public void onLowerModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell )
    {}
    
    /**
     * Fired when the upper property on the passed in range is about to be modified.
     */
    public void onPreUpperModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, String proposedValue, IResultCell cell )
    {}
    
    /**
     * Fired when the upper property on the passed in range was modified.
     */
    public void onUpperModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell )
    {}
    
    /**
     * Fired when a new range is about to be added to the passed in multiplicity.
     */
    public void onPreRangeAdded( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell )
    {}
    
    /**
     * Fired when a new range is added to the passed in multiplicity.
     */
    public void onRangeAdded( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell )
    {}
    
    /**
     * Fired when an existing range is about to be removed from the passed in multiplicity.
     */
    public void onPreRangeRemoved( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell )
    {}
    
    /**
     * Fired when an existing range is removed from the passed in multiplicity.
     */
    public void onRangeRemoved( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell )
    {}
    
    /**
     * Fired when the order property is about to be changed on the passed in mulitplicity.
     */
    public void onPreOrderModified( ITypedElement element, IMultiplicity mult, boolean proposedValue, IResultCell cell )
    {}
    
    /**
     * Fired when the order property is changed on the passed in mulitplicity.
     */
    public void onOrderModified( ITypedElement element, IMultiplicity mult, IResultCell cell )
    {}
    
    /**
     * Fired when the collection type property is changed on the passed in
     * range.
     * @param element The type that owned the multilicity element
     * @param mult The multiplicity
     * @param range The multiplicity range that changed
     * @param cell The event result.
     */
    public void onCollectionTypeModified( ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell )
    {
        
    }
    
    /**
     *
     * Determines whether or not the name of the feature about to be added to the
     * passed in classifier is unique. It checks attrs owned by the Classifier and
     * out bound NavigableEnds
     *
     * @param classifier[in]   The classifier to check
     * @param name[in]         The name to check against
     *
     * @return true if the name of the feature is unique, else false
     *
     */
    protected boolean ensureUniqueAttribute(IClassifier classifier, String name)
    {
        boolean isUnique = true;
        
        if (classifier != null)
        {
            ETList<IAttribute> attrs = classifier.getAttributesAndNavEndsByName(name);
            
            if(attrs != null)
            {
                int num = attrs.size();
                
                if( num > 0)
                {
                    isUnique = false;
                }
            }
        }
        return isUnique;
    }
    
    
//    protected void appendBlockedRequests(ETList<IChangeRequest> inputRequests)
//    {
//        if ( inputRequests != null)
//        {
//            int count = inputRequests.size();
//            int idx = 0;
//            
//            // Append (order might be important) the input requests to the
//            // list of final requests.
//            IChangeRequest pItem = null;
//            while (idx < count)
//            {
//                pItem = inputRequests.get(idx);
//                idx++;
//                if (pItem != null)
//                {
//                    m_FinalRequests.add(pItem);
//                }
//            }
//        }
//    }
    
//    protected ETList<IChangeRequest> createOutputRequests(ETList<IChangeRequest> inputRequests)
//    {
//        ETList < IChangeRequest > theBigList = new ETArrayList < IChangeRequest >();
//        
//        if (inputRequests != null)
//        {
//            // We need to start the big list with the input list, because those
//            // are the "first" requests. The m_FinalRequests list are requests
//            // that came in during the processing of inputRequests, so come
//            // after, and right now we are concerned about order.
//            
//            // Note that we don't want to just assign. What if we did the following?
//            //   theBigList = inputRequests;
//            // When we append to theBigList later on, we are really manipulating
//            // the input list, which is not what we want.
//            
//            int count = inputRequests.size();
//            int idx = 0;
//            
//            // Append (order might be important) the inputrequests to the
//            // big list.
//            IChangeRequest pItem = null;
//            while ( idx < count )
//            {
//                pItem = inputRequests.get(idx);
//                idx++;
//                if ( pItem != null )
//                {
//                    if ( postValidationCheck( pItem ) )
//                    {
//                        theBigList.add(pItem);
//                    }
//                }
//            }
//        }
//        
//        if ( m_FinalRequests != null )
//        {
//            int count = m_FinalRequests.size();
//            int idx = 0;
//            
//            // Append (order might be important) the inputrequests to the
//            // big list.
//            IChangeRequest pItem = null;
//            while ( idx < count )
//            {
//                pItem = m_FinalRequests.get(idx);
//                idx++;
//                if ( pItem != null )
//                {
//                    if ( postValidationCheck( pItem ) )
//                    {
//                        theBigList.add(pItem);
//                    }
//                }
//            }
//        }
//        
//        // finally, if the big list is not empty, copy it
//        // to the output list, which is the list of requests that
//        // will be passed to the listeners. We should have been
//        // careful up to this point that we handled correctly
//        // an empty input list and or an empty final list without
//        // having dropped any requests from the big list.
//        
//        m_FinalRequests.clear();
//        
//        return theBigList;
//    }
//    
    protected boolean postValidationCheck(IChangeRequest pRequest)
    {
        // PostValidation is for requests that we want to process but not
        // dispatch. Currently, we don't want to dispatch requests for
        // unnamed classes.
        
        boolean retval = false;
        
        if (pRequest != null)
        {
            retval = true;
            RequestDetails dets = m_Utils.getRequestDetails(pRequest);
            int eType = dets.rtElementKind;
            int cType = dets.changeKind;
            int cDetail = dets.requestDetailKind;
            
            IClassifier pClass = getClassOfRequest(pRequest, false);
            if (pClass != null)
            {
                if (m_Utils.isElementUnnamed(pClass))
                {
                    retval = false;
                }
            }
            
            if (!retval && cDetail == RequestDetailKind.RDT_FEATURE_MOVED )
            {
                // if this is a MOVE of a feature,
                // we have to allow it, because codegen needs
                // to at least delete the feature from the old
                // class.
                
                retval = true;
                
                IClassifier pOldClass = getClassOfRequest(pRequest, true );
                if ( pOldClass != null )
                {
                    if ( m_Utils.isElementUnnamed( pOldClass ) )
                    {
                        // OH!  Both classes were unnamed.
                        retval = false;
                    }
                }
            }
        }
        return retval;
    }
    
    protected IClassifier getClassOfRequest(IChangeRequest pRequest, boolean fromBefore)
    {
        IClassifier pClass = null;
        if (pRequest != null)
        {
            RequestDetails dets = m_Utils.getRequestDetails(pRequest);
            int eType = dets.rtElementKind;
            int cType = dets.changeKind;
            int cDetail = dets.requestDetailKind;
            
            if ( cType == ChangeKind.CT_DELETE )
            {
                fromBefore = true;
            }
            
            if ( eType == RTElementKind.RCT_OPERATION )
            {
                ETPairT<IOperation, IClassifier> opAndClass = m_Utils.getOperationAndClass(pRequest, fromBefore);
                IOperation pOp = opAndClass.getParamOne();
                pClass = opAndClass.getParamTwo();
            }
            else if ( eType == RTElementKind.RCT_PARAMETER )
            {
                IElement pEl = m_Utils.getElement(pRequest, fromBefore);
                IParameter pParm = ( pEl instanceof IParameter)?(IParameter)pEl:null;
                if ( pParm != null )
                {
                    IBehavioralFeature pFeat = pParm.getBehavioralFeature();
                    
                    IOperation pParamsOp = ( pEl instanceof IOperation)?(IOperation)pFeat:null;
                    
                    if ( pParamsOp != null )
                    {
                        pClass = pParamsOp.getFeaturingClassifier();
                    }
                }
            }
            else if ( eType == RTElementKind.RCT_ATTRIBUTE )
            {
                ETPairT<IAttribute, IClassifier> attrAndClass = m_Utils.getAttributeAndClass(pRequest, fromBefore);
                IAttribute pAttr = attrAndClass.getParamOne();
                pClass = attrAndClass.getParamTwo();
            }
            else
            {
                IElement pEl = m_Utils.getElement(pRequest, fromBefore);
                if ( pEl != null )
                {
                    pClass = (pEl instanceof IClassifier)?(IClassifier)pEl:null;
                }
            }
        }
        return pClass;
    }
    
    protected boolean preValidationCheck(IRequestValidator request)
    {
        // PreValidation is where we decide what really needs to be sent to
        // the handlers. We want to NOT send things to the handlers if the
        // actual classifier involved is a data type. In the future, we might
        // filter for some other reason.
        if (request != null)
        {
            try
            {
                if (request.getValid())
                {
                    IClassifier pClass = getClassOfRequest(request.getRequest(), false);
                    // Ok, see if the class is a datatype.
                    if ( pClass != null )
                    {
                        //IDataType pDT = pClass instanceof IDataType? (IDataType) pClass : null;
                        //if ( pDT != null )
                        if(pClass instanceof IDataType)
                        {
                            // Basically we want to process enumeration.  Even though an enumeration
                            // derives from a IDataType.
                            if(!(pClass instanceof IEnumeration))
                            {
                                request.setValid( false );
                            }
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();;
            }
        }
        return request.getValid();
    }
    
    private boolean isConstructorBeingModified(INamedElement pElement, String sProposedName)
    {
        if(pElement instanceof IOperation )
        {
            IOperation operation = (IOperation)pElement;
            IClassifier pClass = getClassOfRequest(m_ChangeRequest, false );
            if (operation != null && pClass != null)
            {
                if( operation.getName().equals(pClass.getName()) &&
                        operation.getIsConstructor() &&
                        !operation.getName().equals(sProposedName) )
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    protected boolean checkForInvalidName(INamedElement pElement)
    {
        boolean retval = false;
        if ( pElement != null )
        {
            String elName = pElement.getName();
            retval = checkForInvalidName(pElement, elName);
        }
        return retval;
    }
    
    protected boolean checkForInvalidName(INamedElement pElement,
            String sProposedName )
    {
        boolean retval = false;
        
        try
        {
            if ( pElement != null )
            {
                if ( pElement instanceof IClassifier )
                {
                    IClassifier pClass = (IClassifier) pElement;
                    // what kind of classifier is it?
                    String elType = pClass.getElementType();
                    // 78383, enforce naming rule on Enumeration
                    if ( elType != null && (elType.equals("Class") ||
                            elType.equals("Interface")) || elType.equals("Enumeration"))
                    {
                        if ( invalidClass( sProposedName ) )
                        {
                            sendErrorMessage( RPMessages.getString("IDS_JRT_CLASS_INVALID_NAME"), RPMessages.getString("IDS_JRT_INVALID_VALUE_TITLE"));
                            retval = true;
                        }
                    }
                }
                // check for invalid name bug fix 5104815
                else if ( pElement instanceof IProject )
                {
                    if ( invalidProject(sProposedName) )
                    {
                        sendErrorMessage( RPMessages.getString("IDS_JRT_PROJECT_INVALID_NAME"),
                                RPMessages.getString("IDS_JRT_INVALID_VALUE_TITLE"));
                        retval = true;
                    }
                }
                else if ( pElement instanceof IPackage )
                {
                    String sPrjojectName = pElement.getProject().getName();
                    if ( invalidPackage(sProposedName) )
                    {
                        //                            sendErrorMessage( RPMessages.getString("IDS_JRT_PACKAGE_INVALID_NAME"),
                        //                            RPMessages.getString("IDS_JRT_INVALID_VALUE_TITLE"));
                        sendErrorMessage( RPMessages.getString("IDS_JRT_PACKAGE_INVALID_NAME",new Object[]{sProposedName,sPrjojectName, }),
                                RPMessages.getString("IDS_JRT_INVALID_VALUE_TITLE"));
                        retval = true;
                    }
                }
                else if (pElement instanceof IAttribute)
                {
                    if ( invalidAttribute(sProposedName) )
                    {
                        sendErrorMessage( RPMessages.getString("IDS_JRT_ATTRIBUTE_INVALID_NAME"),
                                RPMessages.getString("IDS_JRT_INVALID_VALUE_TITLE"));
                        retval = true;
                    }
                }
                else if (pElement instanceof IOperation)
                {
                    //fix for #5077005: Samaresh
                    if(isConstructorBeingModified(pElement, sProposedName))
                    {
                        sendErrorMessage( RPMessages.getString("IDS_JRT_CONSTRUCTOR_INVALID_NAME"),
                                RPMessages.getString("IDS_JRT_INVALID_VALUE_TITLE"));
                        retval = true;
                    }
                    //end
                    
                    else if ( invalidOperation(sProposedName) )
                    {
                        sendErrorMessage( RPMessages.getString("IDS_JRT_OPERATION_INVALID_NAME"),
                                RPMessages.getString("IDS_JRT_INVALID_VALUE_TITLE"));
                        retval = true;
                    }
                }
                else if (pElement instanceof IParameter)
                {
                    if ( invalidParameter(sProposedName) )
                    {
                        sendErrorMessage( RPMessages.getString("IDS_JRT_PARAMETER_INVALID_NAME"),
                                RPMessages.getString("IDS_JRT_INVALID_VALUE_TITLE"));
                        retval = true;
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return retval;
    }
    
    protected boolean invalidClass(String name)
    {
        boolean retval = false;
        retval = invalidIdentifier(name);
        if ( !retval )
        {
            // Class names cannot be primitives
            //		   retval = isPrimitiveType(name);
            // Class names cannot be datatype like "String" #5100528
            retval = isDataType(name);
        }
        return retval;
    }
    
    protected boolean invalidPackage(String name)
    {
        boolean retval = false;
        retval = invalidIdentifier(name);
        if ( !retval )
        {
            // Package names cannot be primitives or datatypes
            retval = isDataType(name);
        }
        return retval;
    }
    protected boolean invalidProject(String name)
    {
        return name.endsWith("~");
    }
    protected boolean invalidAttribute(String name)
    {
        boolean retval = false;
        retval = invalidIdentifier(name);
        if ( !retval )
        {
            // attribute names cannot be primitives or datatypes
            retval = isDataType(name);
        }
        return retval;
    }
    
    protected boolean invalidOperation(String name)
    {
        boolean retval = false;
        retval = invalidIdentifier(name);
        if ( !retval )
        {
            // operation names cannot be primitives or datatypes
            retval = isDataType(name);
        }
        return retval;
    }
    
    
    protected boolean invalidParameter(String name)
    {
        boolean retval = false;
        retval = invalidIdentifier(name);
        if ( !retval )
        {
            // parameter names cannot be primitives or datatypes
            retval = isDataType(name);
        }
        return retval;
    }
    
    protected boolean invalidIdentifier(String name)
    {
        boolean retval = true;
        
        // Before we look for more complex problems, make sure that this
        // name is not a keyword.
        retval = isKeyword( name );
        
        if ( !retval )
        {
            // First character must be alpha, underscore, or dollar
            // Rest of characters must be alphanum, underscore, or dollar
            
            // isJavaIdentifierStart and isJavaIdentifierPart will support unicode in JDK1.5
            if ( name != null && name.length() > 0 )
            {
                if (Locale.getDefault().getDisplayLanguage().equals("English"))
                {
                    retval = true;
                    char firstchar = name.charAt(0);
                    if ( Character.isJavaIdentifierStart(firstchar))
                    {
                        retval = false;
                        for ( int i = 1; i<name.length() && retval == false; i++ )
                        {
                            char namechar = name.charAt(i);
                            if (!Character.isJavaIdentifierPart(namechar))
                                retval = true;
                        }
                    }
                }
                else
                {
                    retval = true;
                    char firstchar = name.charAt(0);
                    if (Character.isLetter(firstchar) || firstchar=='_' || firstchar=='$')
                    {
                        retval = false;
                        for ( int i = 1; i<name.length() && retval == false; i++ )
                        {
                            char namechar = name.charAt(i);
                            if (!(Character.isLetterOrDigit(namechar) || namechar=='_' || namechar=='$'))
                            {
                                retval = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return retval;
    }
    
    /**
     *
     * Is the name a keyword in this language
     *
     * @param name[in] The name
     *
     * @return true if the name is a language keyword.
     *
     */
    protected boolean isKeyword(String name)
    {
        boolean retval = false;
        try
        {
            ILanguage pLang = getLanguage2();
            if ( pLang != null )
            {
                retval = pLang.isKeyword(name);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return retval;
    }
    
    /**
     *
     * Is the name a primitive type in this language
     *
     * @param name[in] The name
     *
     * @return true if the name is a primitive type
     *
     */
    protected boolean isPrimitiveType(String name)
    {
        boolean retval = false;
        try
        {
            ILanguage pLang = getLanguage2();
            if ( pLang != null )
            {
                retval = pLang.isPrimitive(name);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return retval;
    }
    
    /**
     *
     * Is the name a datatype in this language
     *
     * @param name[in] The name
     *
     * @return true if the name is a predefined datatype
     *
     */
    protected boolean isDataType(String name)
    {
        boolean retval = false;
        try
        {
            ILanguage pLang = getLanguage2();
            if ( pLang != null )
            {
                retval = pLang.isDataType(name);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return retval;
    }
    
    protected void sendCriticalMessage(String message, String title)
    {
        m_Utils.sendCriticalMessage(message, title);
    }
    
    protected void sendErrorMessage(String message, String title)
    {
        m_Utils.sendErrorMessage(message, title);
    }
    
    protected void sendWarningMessage(String message, String title)
    {
        m_Utils.sendWarningMessage(message, title);
    }
    
    protected void sendInfoMessage(String message, String title)
    {
        m_Utils.sendInfoMessage(message, title);
    }
    
    protected void sendDebugMessage(String message, String title)
    {
        m_Utils.sendDebugMessage(message, title);
    }
    
    protected void deny(IResultCell cell, String message, String title)
    {
        if (cell != null)
            cell.setContinue(false);
        sendErrorMessage(message, title);
    }
    
    protected boolean checkIfCorrectLanguage(IElement pElement)
    {
        boolean retVal = false;
        
        if(pElement != null)
        {
            try
            {
                String wantedName = getLanguage();
                if (wantedName != null && wantedName.length() > 0)
                {
                    ETList<ILanguage> pLanguages = pElement.getLanguages();
                    if(pLanguages != null)
                    {
                        int max = pLanguages.size();
                        
                        // Save time by not creating the object each time through the loop.
                        ILanguage pCurLanguage = null;
                        for (int index = 0; (index < max) && !retVal; index++)
                        {
                            pCurLanguage = pLanguages.get(index);
                            if(pCurLanguage != null)
                            {
                                String name = pCurLanguage.getName();
                                
                                if(name != null && name.length() > 0 && name.equals(wantedName))
                                {
                                    retVal = true;
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
        return retVal;
    }
    
    
    public boolean isNewNameValid(IElement element, String name)
    {
        if (!checkIfCorrectLanguage(element))
            return true;
        
        if (!checkForInvalidName((INamedElement)element, name))
        {
            IStructuralFeature structFeat = element instanceof IStructuralFeature?
                (IStructuralFeature) element : null;
            if (structFeat != null)
            {
                IClassifier classifier = structFeat.getFeaturingClassifier();
                
                if( classifier != null)
                {
                    if( !ensureUniqueAttribute(classifier, name) )
                    {
                        sendErrorMessage(RPMessages.getString("IDS_JRT_ATTR_NAME_NOT_UNIQUE"),
                                RPMessages.getString("IDS_JRT_ATTR_NAME_NOT_UNIQUE_TITLE"));
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    
    // Fix for #5070768 , #5084945
    public class JavaRequestPostProcessor extends JavaRequestProcessor
    {
        IJavaAttributeChangeHandler handler = null;
        
        public void onFeatureMoved(IClassifier classifier, IFeature feature,
                IResultCell cell)
        {
            IClassifier owner = feature.getFeaturingClassifier();
            String elementType = owner.getElementType();
            
            //		Fix for #5070768
            if ( elementType != null && elementType.equals("Interface")
                    && ( feature instanceof IAttribute))
            {
                IAttribute attr = (IAttribute) feature;
                
                String def = attr.getDefault2();
                if( (def==null) || def.equals(""))
                {
                    if( handler == null )
                    {
                        handler = new JavaAttributeChangeHandler();
                    }
                    handler.setProcessor( this );
                    handler.setDefaultInitialValue( attr );
                }
                feature.setIsFinal( true);
                feature.setIsStatic( true);
                feature.setVisibility(IVisibilityKind.VK_PUBLIC );
                
            }
            //	 Fix for  #5084945
            if ( elementType != null && elementType.equals("Interface")
                    && (feature instanceof IOperation))
            {
                IOperation pOp = (IOperation) feature;
                pOp.setVisibility(IVisibilityKind.VK_PUBLIC );
            }
        }
        protected void deny(IResultCell cell, String message, String title)
        {
            if (cell != null)
                cell.setContinue(false);
        }
        protected void sendCriticalMessage(String message, String title)
        {
        }
        
        protected void sendErrorMessage(String message, String title)
        {
        }
        
        protected void sendWarningMessage(String message, String title)
        {
        }
        
        protected void sendInfoMessage(String message, String title)
        {
        }
        
        protected void sendDebugMessage(String message, String title)
        {
        }
        
    }
    
}
