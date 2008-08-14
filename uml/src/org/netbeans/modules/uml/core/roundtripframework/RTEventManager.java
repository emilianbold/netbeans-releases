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

/**
 * File       : RTEventManager.java
 * Created on : Nov 5, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import java.util.Iterator;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.eventframework.BatchEventContext;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.IBatchEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationValidatorEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationSignatureChangeContext;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog;

/**
 * @author Aztec
 */
public class RTEventManager implements IRTEventManager
{
    private static boolean ESTABLISH_CREATE = true;
    private static boolean ESTABLISH_NORMAL = false;
    private static boolean ESTABLISH_NOOVERRIDE = false;
    private static boolean ESTABLISH_OVERRIDE = true;

   
    private ETList<CompoundRequestListItem> m_CompoundRequests 
        = new ETArrayList<CompoundRequestListItem>();

    /// The RoundTripController this manager is aiding
    private IRoundTripController m_Controller = null;

    /// The dispatch controller on m_Controller. 
    private IEventDispatchController m_DispController = null;

    /// xmi.id, ElementProcPair
    //typedef std::multimap< CComBSTR, PreRequest* > PreElements;
//    typedef std::vector< PreRequest* > PreElements;
//    PreElements                                        m_PreElements;
    private ETList<IPreRequest> m_PreElements =
            new ETArrayList<IPreRequest>();

    /// actual request processor, change requests
    private ETList<ETPairT<IRequestProcessor, ETList<IChangeRequest>>> 
            m_PostElements = 
                new ETArrayList<ETPairT<IRequestProcessor, ETList<IChangeRequest>>>();

    /// xmi.id of element associated with request, requests
    //typedef std::map< CComBSTR, PreRequestPair >       PostElements;
    //typedef std::vector<  PreRequestPair >       PostElements;
    //PostElements                                       m_PostElements;


    /// The language manager. This is just a pointer back to the LanguageManager
    /// on the core product
    private ILanguageManager m_LanguageManager = null;

    ///ProcessorManager manages the RequestProcessors
    IProcessorManager m_ProcManager = null;
    
    
    protected int m_BatchCount;
    protected ETList< IChangeRequest > m_BatchRequests = null;

    private boolean firstAccess = true;
    
    public RTEventManager()
    {
    }
    
    public RTEventManager(IRoundTripController controller)
    {
        m_Controller = controller;
        m_ProcManager = new ProcessorManager(controller);
        if (controller != null)
            m_DispController = controller.getEventDispatchController();
    }
    
    /**
     * For testcases.
     */
    public ETList<IPreRequest> getPreElements()
    {
        return m_PreElements;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreRedefinedElementAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreRedefinedElementAdded(
        IRedefinableElement redefiningElement,
        IRedefinableElement redefinedElement,
        IResultCell cell)
    {
        // C++ code empty
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreRedefinedElementRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreRedefinedElementRemoved(
        IRedefinableElement redefiningElement,
        IRedefinableElement redefinedElement,
        IResultCell cell)
    {
        // C++ code empty
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreRedefiningElementAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreRedefiningElementAdded(
        IRedefinableElement redefinedElement,
        IRedefinableElement redefiningElement,
        IResultCell cell)
    {
        // C++ code empty
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreRedefiningElementRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreRedefiningElementRemoved(
        IRedefinableElement redefinedElement,
        IRedefinableElement redefiningElement,
        IResultCell cell)
    {
        // C++ code empty
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreSourceDirModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreSourceDirModified(
        IPackage element,
        String proposedSourceDir,
        IResultCell cell)
    {
        if (element == null || cell == null) return;
          
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                                                        m_DispController,
                                                        EventDispatchNameKeeper.EDT_ELEMENTMODIFIED_KIND);
        helper.establish(ESTABLISH_CREATE, ESTABLISH_NOOVERRIDE, element, cell);

        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IElementChangeEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IElementChangeEventDispatcher) helper.getDispatcher();
        }
        catch (ClassCastException e)
        {
        }
            
        boolean proceed = false;
        if (dispatcher != null)
        {    
            // Fire the event to any listeners ( request processors most likely ), that need
            // to add more changerequests before continuing..
            proceed = dispatcher.firePreSourceDirModified(
                    element, proposedSourceDir, helper.getPayload());
            
            
            if (proceed)
            {
                recordRequests(element, helper.getPayload(), 
                        RequestDetailKind.RDT_SOURCE_DIR_CHANGED, null);
                INamespace ns = 
                    element instanceof INamespace? (INamespace) element : null;
                propagateNamespaceChange(
                        element, RequestDetailKind.RDT_SOURCE_DIR_CHANGED, 
                        proceed, cell, ns);
            }
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRedefinedElementAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRedefinedElementAdded(
        IRedefinableElement redefiningElement,
        IRedefinableElement redefinedElement,
        IResultCell cell)
    {
        // C++ code empty
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRedefinedElementRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRedefinedElementRemoved(
        IRedefinableElement redefiningElement,
        IRedefinableElement redefinedElement,
        IResultCell cell)
    {
        // C++ code empty
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRedefiningElementAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRedefiningElementAdded(
        IRedefinableElement redefinedElement,
        IRedefinableElement redefiningElement,
        IResultCell cell)
    {
        // C++ code empty
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRedefiningElementRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRedefiningElementRemoved(
        IRedefinableElement redefinedElement,
        IRedefinableElement redefiningElement,
        IResultCell cell)
    {
        // C++ code empty
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTAbstractModified(
        IBehavioralFeature feat,
        IResultCell cell)
    {
        processRequests(feat, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTAbstractModified(
        IClassifier classifier,
        IResultCell cell)
    {
        processRequests(classifier, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTChangeabilityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTChangeabilityModified(
        IStructuralFeature feat,
        IResultCell cell)
    {
        processRequests(feat, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTConcurrencyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTConcurrencyModified(
        IBehavioralFeature feat,
        IResultCell cell)
    {
        processRequests(feat, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTConcurrencyPreModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTConcurrencyPreModified(
        IBehavioralFeature feat,
        int kind,
        IResultCell cell)
    {
        if (feat == null || cell == null) return;
          
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                                                        m_DispController,
                                                        EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, feat, cell);

        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }
            
        boolean proceed = false;
        if (dispatcher != null)
        {    
            proceed = dispatcher.fireConcurrencyPreModified(feat, kind, helper.getPayload());
            
            if(proceed)
                recordRequests(
                    feat,
                    helper.getPayload(),
                    RequestDetailKind.RDT_CONCURRENCY_MODIFIED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed); 
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTConditionAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTConditionAdded(
        IOperation oper,
        IConstraint cons,
        boolean flag,
        IResultCell cell)
    {
        processRequests(cons, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTConditionRTPreAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTConditionPreAdded(
        IOperation oper,
        IConstraint cons,
        boolean flag,
        IResultCell cell)
    {
        if (oper == null || cons == null || cell == null) return;
        
        RTStateTester rt;
        if (new RTStateTester().isAppInRoundTripState(oper))
        {
            RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                                                            m_DispController,
                                                            EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
            helper.establish(ESTABLISH_CREATE, ESTABLISH_OVERRIDE, cons, cell);
            
            // Fire the event to any listeners ( request processors most likely ), that need
            // to add more changerequests before continuing..

            boolean isPreCondition = true; // XXX : How do we set this correctly?
            
            IClassifierEventDispatcher dispatcher = null;
            try
            {
                dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
            }
            catch (Exception e)
            {
            }

            boolean proceed = false;
            if (dispatcher != null)
            {    
                proceed = dispatcher.fireConditionPreAdded(oper, cons, isPreCondition, helper.getPayload());
             
                if(proceed)
                    recordRequests( oper, cons, helper.getPayload(), RequestDetailKind.RDT_CONDITION_ADDED, null);
                
                completeResultCell(cell, helper.getCellOrigValue(), proceed);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTConditionPreRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTConditionPreRemoved(
        IOperation oper,
        IConstraint cons,
        boolean flag,
        IResultCell cell)
    {
        if (oper == null || cons == null || cell == null) return;
        

        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                                                        m_DispController,
                                                        EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, cons, cell);
        
        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..

        boolean isPreCondition = true; // XXX : How do we set this correctly?
        
        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }

        boolean proceed = false;
        if (dispatcher != null)
        {    
            proceed = dispatcher.fireConditionPreRemoved(oper, cons, isPreCondition, helper.getPayload());
         
            if(proceed)
                recordRequests( oper, cons, helper.getPayload(), RequestDetailKind.RDT_CONDITION_REMOVED, null);
            
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTConditionRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTConditionRemoved(
        IOperation oper,
        IConstraint cons,
        boolean flag,
        IResultCell cell)
    {
        processRequests(cons, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTDefaultBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTDefaultBodyModified(IAttribute attr, IResultCell cell)
    {
        processRequests(attr, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTDefaultExpBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTDefaultExpBodyModified(IParameter parm, IResultCell cell)
    {
        processRequests(parm, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTDefaultExpLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTDefaultExpLanguageModified(
        IParameter parm,
        IResultCell cell)
    {
        processRequests(parm, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTDefaultExpModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTDefaultExpModified(IParameter parm, IResultCell cell)
    {
        processRequests(parm, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTDefaultLanguageModified(IAttribute attr, IResultCell cell)
    {
        processRequests(attr, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTDefaultModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTDefaultModified(IAttribute attr, IResultCell cell)
    {
        processRequests(attr, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTDefaultPreModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTDefaultPreModified(
        IAttribute attr,
        IExpression exp,
        IResultCell cell)
    {
        if (attr == null || exp == null || cell == null) return;

        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                                                        m_DispController,
                                                        EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish( ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, attr, cell );

        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }
        
        boolean proceed = false;
        if (dispatcher != null)
        {    
            proceed = dispatcher.fireDefaultPreModified(attr, exp, helper.getPayload());
        
            if(proceed)    
                recordRequests(
                    attr,
                    helper.getPayload(),
                    RequestDetailKind.RDT_ATTRIBUTE_DEFAULT_MODIFIED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);   
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTDirectionModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTDirectionModified(IParameter parm, IResultCell cell)
    {
        processRequests(parm, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTDocumentationModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTDocumentationModified(IElement element, IResultCell cell)
    {
        processRequests(element, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTDocumentationPreModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTDocumentationPreModified(
        IElement element,
        String proposedValue,
        IResultCell cell)
    {
        if(element == null || proposedValue == null || cell == null) return;

        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, m_DispController, EventDispatchNameKeeper.EDT_ELEMENTMODIFIED_KIND);
        helper.establish( ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell );

        if ( helper != null)
        {            
            // Fire the event to any listeners ( request processors most likely ), that need
            // to add more changerequests before continuing..

            boolean proceed = helper.getCellOrigValue();
            IElementChangeEventDispatcher dispatcher = null;
            try
            {
                dispatcher = (IElementChangeEventDispatcher)helper.getDispatcher();
            }
            catch(Exception e){}

            if(dispatcher != null)
            {    
                proceed = dispatcher.fireDocumentationPreModified( element, proposedValue, helper.getPayload());
                if(proceed)
                    recordRequests(element, helper.getPayload(), RequestDetailKind.RDT_DOCUMENTATION_MODIFIED, null);
                
                completeResultCell(cell, helper.getCellOrigValue(), proceed);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTElementAddedToNamespace(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTElementAddedToNamespace(
        INamespace space,
        INamedElement element,
        IResultCell cell)
    {
        if (space == null || element == null) return;
        
        IParameter parm = null;
        
        try
        {
            parm = (IParameter)element;
        }
        catch (Exception e)
        {
        }

        if (parm == null)
        {

            // We do not want to process parameter adds to operations, 'cause we're handling
            // that with a higher level event OnRTPreParameterAdded
            
            processRequests(element, ChangeKind.CT_MODIFY, null);

            INamespace pModNamespace = null;
            
            try
            {
                pModNamespace = (INamespace)element;
            }
            catch (Exception e)
            {
            }

            if (pModNamespace != null)
            {
                propagateNamespaceChange(element, RequestDetailKind.RDT_CHANGED_NAMESPACE, false, cell, pModNamespace);
            }
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTElementDelete(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTElementDelete(
        IVersionableElement element,
        IResultCell cell)
    {
        IElement pElement = null;
        
        try
        {
            pElement = (IElement)element;
        }
        catch (Exception e)
        {
        }

        if (pElement != null)
        {
            processRequests(pElement, ChangeKind.CT_DELETE, null);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTElementPreDelete(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTElementPreDelete(
        IVersionableElement element,
        IResultCell cell)
    {
        if (element == null) return;
        
        IElement elem = null;
        try
        {
            elem = (IElement)element;
        }
        catch (Exception e)
        {
        }

        if (elem != null)
        {

            RTDispatchHelper helper = new RTDispatchHelper( m_ProcManager, 
                                                            m_DispController,
                                                            EventDispatchNameKeeper.EDT_ELEMENT_LIFETIME_KIND);
            helper.establish( ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, elem, cell );

            // Fire the event to any listeners ( request processors most likely ), that need
            // to add more changerequests before continuing..
            IElementLifeTimeEventDispatcher dispatcher = null;
            try
            {
                dispatcher = (IElementLifeTimeEventDispatcher)helper.getDispatcher();
            }
            catch(Exception e){}
            
            boolean proceed = false;
            if(dispatcher != null)
            {    
                proceed = dispatcher.fireElementPreDelete(elem, helper.getPayload());
            
                if(proceed)    
                    recordRequests(elem, helper.getPayload(), RequestDetailKind.RDT_ELEMENT_DELETED, null);
                completeResultCell (cell, helper.getCellOrigValue(), proceed);
            }
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTEventContextPopped(org.netbeans.modules.uml.core.eventframework.IEventContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTEventContextPopped(
        IEventContext pContext,
        IResultCell pCell)
    {
        endBatch(pContext);

        IOperationSignatureChangeContext pSigChange = null;
        
        try
        {
            pSigChange = (IOperationSignatureChangeContext)pContext;
        }
        catch (Exception e)
        {
        }

        if (pSigChange != null)
        {
            // Get the element (operation) from the context

            IOperation pOperation = pSigChange.getOperation();

            if (pOperation != null)
            {
                // See if we aleady have a list item for this one.
                // Because processing and dispatching may cause new 
                // requests to be generated, we have to double buffer 
                // the list so that we don't recursively step on it.

                ETList<CompoundRequestListItem> dispatchList = 
                                new ETArrayList<CompoundRequestListItem>();

                int count = m_CompoundRequests.size();
                CompoundRequestListItem pItem = null;
                
                boolean incr = true;
                for (int i = 0 ; i < count ;)
                {
                    incr = true;
                    pItem = m_CompoundRequests.get(i);
                    if (pItem != null)
                    {
                        if (pItem.isSame(pOperation))
                        {
                            // We decrement first, to see if this one is really
                            // ready to go.

                            if (pItem.decrement() == 0)
                            {
                                // yep. This change request needs to be processed and dispatched.
                                m_CompoundRequests.remove(i);
                                count = m_CompoundRequests.size();
                                incr = false;

                                dispatchList.add(pItem);
                            }
                        }
                    }
                    if (incr)
                    {
                        i++;
                    }
                }

                // Note, the ONLY difference between
                // this routine and ProcessRequests is the list from 
                // which the change request comes from. In ProcessRequests,
                // it comes from m_PostElements. Here, it comes from 
                // m_CompoundRequests.
                count = dispatchList.size();
                IRequestProcessor pProc = null;
                for (int i = 0 ; i < count ; i++)
                {
                    pItem = dispatchList.get(i);
                    if (pItem != null)
                    {
                        IChangeRequest pRequest = pItem.request();

                        pProc = pItem.processor();

                        if (pRequest != null && pProc != null)
                        {
                            // IRequestProcessor::ProcessRequests always expects a list in.

                            ETList<IChangeRequest> inRequests = new ETArrayList<IChangeRequest>();
                            // Make sure that the change request is in a a create state
                            // if modifying an Unnamed operation

                            IElement pBeforeElement = pRequest.getBefore();

                            if (pBeforeElement != null)
                            {
                                if(new NameModifyPreRequest().inCreateState(pBeforeElement))
                                {
                                    pRequest.setState(ChangeKind.CT_CREATE);
                                }
                            }
                            inRequests.add(pRequest);

                            // Like always, the request processor may have added change
                            // requests, which are the validated requests that are the 
                            // final validated ones that are actually to be dispatched.
                            
                            ETList<IChangeRequest> validatedReqs = new ETArrayList<IChangeRequest>();

                            startBatch(null);

                            validatedReqs = pProc.processRequests(inRequests);

                            if(validatedReqs != null && validatedReqs.size() > 0)
                            {
                                dispatchRequests(validatedReqs);
                            }
                            endBatch(null);
                        }
                    }
                }

                // The destruction of the dispatchList should clear the pointers that 
                // we deleted inside the loop.
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTEventContextPushed(org.netbeans.modules.uml.core.eventframework.IEventContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTEventContextPushed(
        IEventContext pContext,
        IResultCell pCell)
    {
        startBatch(pContext);
        // if this is an operation signature change context...
        IOperationSignatureChangeContext sigChange = 
            pContext instanceof IOperationSignatureChangeContext?
                    (IOperationSignatureChangeContext) pContext
                  : null;
        if (sigChange != null)
        {
            // Get the element (operation) from the context
            IOperation op = sigChange.getOperation();
            if (op != null)
            {
                // See if we aleady have a list item for this one.
                ETList<CompoundRequestListItem> dispatchList = 
                                new ETArrayList<CompoundRequestListItem>();

                int count = m_CompoundRequests.size();
                CompoundRequestListItem pItem = null;
                
                boolean foundit = false;
                for (int i = 0 ; i < count ; i++)
                {
                    pItem = m_CompoundRequests.get(i);
                    if (pItem != null)
                    {
                        if (pItem.isSame(op))
                        {
                            // we already have one. Just increment this one.

                            foundit = true;
                            pItem.increment();
    
                            // we cannot exit at this time, using foundit, because 
                            // we might have more than one request processor associated
                            // with this element, meaning we created a compound request
                            // for each one, and so we need to increment them all.
                            // But, we use foundit to say "we don't need to create
                            // another."
                        }
                    }
                }
                
                // If we did not find one, time to create one. First we need to 
                // make sure that the element is roundtripable

                RTStateTester rt;
                if (!foundit && new RTStateTester().isElementRoundTripable(op))
                {
                    // To be complete, we need to get the operations class.
                    IClassifier pClass = op.getFeaturingClassifier();

                    // We are now ready to simply "establish a pre-request".
                    // The only difference is which list the pre-request ends up on.

                    IRoundTripEventPayload pPayload = createRTContextPayload(pCell, op);
                    establishPreRequest(pClass, op, RequestDetailKind.RDT_SIGNATURE_CHANGED, pPayload, null, null);
                }                
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTEventDispatchCancelled()
     */
    public void onRTEventDispatchCancelled ( ETList<Object> pListeners, 
                                                   Object listenerWhoCancelled, 
                                                 IResultCell pCell )
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTFeatureAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTFeatureAdded(
        IClassifier classifier,
        IFeature feat,
        IResultCell cell)
    {
        processRequests( feat, ChangeKind.CT_CREATE, null );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTFeatureDuplicatedToClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTFeatureDuplicatedToClassifier(
        IClassifier pOldClassifier,
        IFeature pOldFeature,
        IClassifier pNewClassifier,
        IFeature pNewFeature,
        IResultCell cell)
    {
        if (pOldFeature == null || pNewFeature == null || pNewClassifier == null) return;
        
        processDuplicateRequests( pNewClassifier, pOldFeature, pNewFeature, ChangeKind.CT_MODIFY);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTFeatureMoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTFeatureMoved(
        IClassifier classifier,
        IFeature feature,
        IResultCell cell)
    {
        if (feature == null) return;
        processRequests(feature, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTFeaturePreAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTFeaturePreAdded(
        IClassifier classifier,
        IFeature element,
        IResultCell cell)
    {
        if (new RTStateTester().isAppInRoundTripState(classifier))
        {    
            RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                    m_DispController,
                    EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
            helper.establish(ESTABLISH_CREATE, ESTABLISH_NOOVERRIDE, element,
                    cell);

            IClassifierEventDispatcher disp = null;
            try
            {
                disp = (IClassifierEventDispatcher) helper.getDispatcher();
            }
            catch (ClassCastException ignored)
            {
            }

            boolean proceed = false;
            if (disp != null)
            {    
                proceed = disp.fireFeaturePreAdded(classifier, element, helper.getPayload());
                if (proceed)
                    recordRequests(classifier, element, helper.getPayload(),
                            RequestDetailKind.RDT_FEATURE_ADDED, null);
                completeResultCell(cell, helper.getCellOrigValue(), proceed);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTFeaturePreDuplicatedToClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTFeaturePreDuplicatedToClassifier(
        IClassifier classifier,
        IFeature feature,
        IResultCell cell)
    {
        if (classifier == null || feature == null) return;
        
        RTDispatchHelper helper =
            new RTDispatchHelper(
                m_ProcManager,
                m_DispController,
                EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, classifier, cell);
        
        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }

        boolean proceed = false;
        if (dispatcher != null)
        {    
            proceed = dispatcher.fireFeatureDuplicatedToClassifier(classifier, feature, null, null, helper.getPayload());
        
            if(proceed)    
                recordRequests(
                    classifier,
                    helper.getPayload(),
                    RequestDetailKind.RDT_FEATURE_DUPLICATED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), true);            
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTFeaturePreMoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTFeaturePreMoved(
        IClassifier classifier,
        IFeature feature,
        IResultCell cell)
    {
        if (classifier == null || feature == null) return;
        
        RTDispatchHelper helper =
            new RTDispatchHelper(
                m_ProcManager,
                m_DispController,
                EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, feature, cell);
        
        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }

        boolean proceed = false;
        if (dispatcher != null)
        {    
            proceed = dispatcher.fireFeaturePreMoved(classifier, feature, helper.getPayload());
        
            if(proceed)    
                recordRequests(
                    feature,
                    helper.getPayload(),
                    RequestDetailKind.RDT_FEATURE_MOVED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), true);     
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTFinalModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTFinalModified(
        IRedefinableElement element,
        IResultCell cell)
    {
        if(element == null || cell == null) return;
        processRequests(element, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTHandledSignalAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTHandledSignalAdded(
        IBehavioralFeature feat,
        IResultCell cell)
    {
        processRequests(feat, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTHandledSignalRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTHandledSignalRemoved(
        IBehavioralFeature feat,
        IResultCell cell)
    {
        processRequests(feat, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTImpacted(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void onRTImpacted(IClassifier classifier, ETList<IVersionableElement> impacted, IResultCell cell)
    {
        if (classifier == null || cell == null) return;

        if (impacted != null)
        {
            // start a batch so that these requests are all in the same batch.
            // We do this because a class name change most likely generated 
            // alot of impacted changes, and multiple other classes are impacted
            // multiple times ( the classic example is changing the name of a class
            // this is the navigable end of an association. Getter, setter, and 
            // attribute are updated on the same class at the other end, and we 
            // don't want the listeners to have to parse the same file 3 times in a row.

            int batchLevel = startBatch(null);

            int count = impacted.size();
            int idx = 0;
            IVersionableElement pItem = null;
            while ( idx < count )
            {
                pItem = impacted.get(idx);
                idx++;
                if (pItem != null)
                {
                    IAttribute pAttr = null;
                    IParameter pParm = null;

                    if (pItem instanceof IAttribute)
                    {
                        pAttr = (IAttribute)pItem;
                    }
                    if (pItem instanceof IParameter)
                    {
                        pParm = (IParameter)pItem;
                    }
                    
                    if (pAttr != null || pParm != null)
                    {
                        IElement elementItem = null;
                                    
                        if (pItem instanceof IElement)
                        {
                            elementItem = (IElement)pItem;
                        }

                        processImpactedRequests (elementItem, classifier, ChangeKind.CT_MODIFY);
                    }
                }
            }
            batchLevel = endBatch (null);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTLeafModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTLeafModified(IClassifier classifier, IResultCell cell)
    {
        processRequests(classifier, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTLowerModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTLowerModified(
        ITypedElement element,
        IMultiplicity mult,
        IMultiplicityRange range,
        IResultCell cell)
    {
        processRequests(element, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTMultiplicityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTMultiplicityModified(
        ITypedElement element,
        IResultCell cell)
    {
        processRequests(element, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTNameModified(INamedElement element, IResultCell cell)
    {
        if (element == null) return;
        
        processRequests(element, ChangeKind.CT_MODIFY, null);

        INamespace pNamespace = null;
        try
        {
            pNamespace = (INamespace)element;
        }
        catch (Exception e)
        {
        }

        if ( pNamespace != null)
            propagateNamespaceChange(element, RequestDetailKind.RDT_NAME_MODIFIED, false, cell, pNamespace);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTNativeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTNativeModified(IFeature feat, IResultCell cell)
    {
        processRequests(feat, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTOperationPropertyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTOperationPropertyModified(
        IOperation oper,
        int nKind,
        IResultCell cell)
    {
        processRequests(oper, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTOrderModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTOrderModified(
        ITypedElement element,
        IMultiplicity mult,
        IResultCell cell)
    {
        processRequests(element, ChangeKind.CT_MODIFY, null);
    }

    public void onRTCollectionTypeModified( ITypedElement element, 
                                            IMultiplicity mult, 
                                            IMultiplicityRange range, 
                                            IResultCell cell )
    {
        RTDispatchHelper helper =
            new RTDispatchHelper(
                m_ProcManager,
                m_DispController,
                EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);
                
        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }
        
        boolean proceed = false;
        if (dispatcher != null)
        {
            recordRequests(range,
                           element,
                           helper.getPayload(),
                           RequestDetailKind.RDT_COLLECTION_TYPE_CHANGED,
                           null);
            completeResultCell(cell, helper.getCellOrigValue(), true);
        }

        
        processRequests(element, ChangeKind.CT_MODIFY, null);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTParameterAdded(
        IBehavioralFeature feat,
        IParameter parm,
        IResultCell cell)
    {
        processRequests(parm, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTParameterRemoved(
        IBehavioralFeature feat,
        IParameter parm,
        IResultCell cell)
    {
        processRequests(parm, ChangeKind.CT_MODIFY, feat);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreAbstractModified(
        IBehavioralFeature feat,
        boolean flag,
        IResultCell cell)
    {
        if (feat == null || cell == null) return;
          
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                                                        m_DispController,
                                                        EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, feat, cell);

        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }
            
        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreAbstractModified(feat, flag, helper.getPayload());
            if(proceed)
                recordRequests(
                    feat,
                    helper.getPayload(),
                    RequestDetailKind.RDT_ABSTRACT_MODIFIED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }
 
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreAbstractModified(
        IClassifier element,
        boolean flag,
        IResultCell cell)
    {
        if (element == null || cell == null) return;
          
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                                                        m_DispController,
                                                        EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);

        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }
        
        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.fireClassifierPreAbstractModified(element, flag, helper.getPayload());
            
            if(proceed)
                recordRequests(
                    element,
                    helper.getPayload(),
                    RequestDetailKind.RDT_ABSTRACT_MODIFIED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }
 

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreChangeabilityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreChangeabilityModified(
        IStructuralFeature feat,
        int kind,
        IResultCell cell)
    {
        if (feat == null || cell == null) return;
          
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                                                        m_DispController,
                                                        EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, feat, cell);

        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }
            
        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreChangeabilityModified(feat, kind, helper.getPayload());
            if(proceed)
                recordRequests(
                    feat,
                    helper.getPayload(),
                    RequestDetailKind.RDT_CHANGEABILITY_MODIFIED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }
 

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreDefaultBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreDefaultBodyModified(
        IAttribute attr,
        String body,
        IResultCell cell)
    {
        if (attr == null || cell == null) return;
          
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                                                        m_DispController,
                                                        EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, attr, cell);

        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }
            
        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreDefaultBodyModified(attr, body, helper.getPayload());
            if(proceed)
                recordRequests(
                    attr,
                    helper.getPayload(),
                    RequestDetailKind.RDT_ATTRIBUTE_DEFAULT_BODY_MODIFIED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreDefaultExpBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreDefaultExpBodyModified(
        IParameter parm,
        String exp,
        IResultCell cell)
    {
        if (parm == null || cell == null) return;
          
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                                                        m_DispController,
                                                        EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, parm, cell);

        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }
            
        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreDefaultExpBodyModified(parm, exp, helper.getPayload());
            if(proceed)
                recordRequests(
                    parm,
                    helper.getPayload(),
                    RequestDetailKind.RDT_PARAMETER_DEFAULT_BODY_MODIFIED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreDefaultExpLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreDefaultExpLanguageModified(
        IParameter parm,
        String exp,
        IResultCell cell)
    {
        if (parm == null || cell == null) return;
          
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                                                        m_DispController,
                                                        EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, parm, cell);

        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }
            
        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreDefaultExpLanguageModified(parm, exp, helper.getPayload());
            if(proceed)
                recordRequests(
            parm,
                    helper.getPayload(),
                    RequestDetailKind.RDT_PARAMETER_DEFAULT_LANGUAGE_MODIFIED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreDefaultExpModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreDefaultExpModified(
        IParameter parm,
        IExpression exp,
        IResultCell cell)
    {
        if (parm == null || cell == null) return;
          
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                                                        m_DispController,
                                                        EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, parm, cell);

        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }
            
        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreDefaultExpModified(parm, exp, helper.getPayload());
            if(proceed)
                recordRequests(
            parm,
                    helper.getPayload(),
                    RequestDetailKind.RDT_PARAMETER_DEFAULT_MODIFIED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreDefaultLanguageModified(
        IAttribute attr,
        String lang,
        IResultCell cell)
    {
        if (attr == null || cell == null) return;
          
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                                                        m_DispController,
                                                        EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, attr, cell);

        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }
            
        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreDefaultLanguageModified(attr, lang, helper.getPayload());
            if(proceed)
                recordRequests(
                    attr,
                    helper.getPayload(),
                    RequestDetailKind.RDT_ATTRIBUTE_DEFAULT_LANGUAGE_MODIFIED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }


    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreDirectionModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreDirectionModified(
        IParameter element,
        int kind,
        IResultCell cell)
    {
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                m_DispController, EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);

        IClassifierEventDispatcher disp = null;
        try
        {
            disp = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (ClassCastException ignored)
        {
        }

        boolean proceed = false;
        if (disp != null)
        {
            proceed = disp.firePreDirectionModified(element, kind, helper.getPayload());
            if (proceed)
                recordRequests(element, helper.getPayload(),
                        RequestDetailKind.RDT_PARAMETER_DIRECTION_MODIFIED, null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreElementAddedToNamespace(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreElementAddedToNamespace(
        INamespace space,
        INamedElement element,
        IResultCell cell)
    {
        if (element == null || cell == null) return;

        IParameter parm = null;
        
        try
        {
            parm = (IParameter)element;
        }
        catch (Exception e)
        {
        }

        if (parm == null)
        {
            // We need to ignore this event when it is occurring due to the fact that a parameter
            // is being added to an operation. This is handled by the higher level routine, OnREPreParameterAdded

            if( new RTStateTester().isElementRoundTripable(space))
            {
                RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                                                                m_DispController,
                                                                EventDispatchNameKeeper.EDT_ELEMENTMODIFIED_KIND);
                if (firstAccess) {
                    // First-access, processor not established with newly create "unowned" element.
                    // So use helper2 establishing on the namespace element to get processor bootstrap
                    RTDispatchHelper helper2 = new RTDispatchHelper(m_ProcManager, m_DispController,
                            EventDispatchNameKeeper.EDT_ELEMENTMODIFIED_KIND);
                    helper2.establish(ESTABLISH_CREATE, ESTABLISH_OVERRIDE, space, cell);
                    firstAccess = false;
                }
                
                helper.establish(ESTABLISH_CREATE, ESTABLISH_OVERRIDE, element, cell);

                // Fire the event to any listeners ( request processors most likely ), that need
                // to add more changerequests before continuing..
                
                IElementChangeEventDispatcher dispatcher = null;
                try
                {
                    dispatcher = (IElementChangeEventDispatcher) helper.getDispatcher();
                }
                catch (Exception e)
                {
                }
                
                if (dispatcher != null)
                {    
                    boolean proceed = dispatcher.firePreElementAddedToNamespace(space, element, helper.getPayload());
                
                    if(proceed)    
                        recordRequests(element, helper.getPayload(), RequestDetailKind.RDT_CHANGED_NAMESPACE, null);
    
                    INamespace pModNamespace = null;
                    
                    try
                    {
                        pModNamespace = (INamespace)element;
                    }
                    catch (Exception e)
                    {
                    }
    
                    if (pModNamespace != null)
                    {
                        propagateNamespaceChange(element, RequestDetailKind.RDT_CHANGED_NAMESPACE, true, cell, pModNamespace);
                    }
    
                    completeResultCell(cell, helper.getCellOrigValue(), proceed);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreEventContextPopped(org.netbeans.modules.uml.core.eventframework.IEventContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreEventContextPopped(
        IEventContext pContext,
        IResultCell pCell)
    {
        // No valid implementation in the C++ code base.
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreEventContextPushed(org.netbeans.modules.uml.core.eventframework.IEventContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreEventContextPushed(
        IEventContext pContext,
        IResultCell pCell)
    {
        // No valid implementation in the C++ code base.
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreFinalModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreFinalModified(
        IRedefinableElement element,
        boolean newVal,
        IResultCell cell)
    {
        RTDispatchHelper helper =
            new RTDispatchHelper(
                m_ProcManager,
                m_DispController,
                EventDispatchNameKeeper.EDT_ELEMENTMODIFIED_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);

        IElementChangeEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IElementChangeEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }

        if (dispatcher != null)
        {    
            boolean proceed = dispatcher.firePreFinalModified(element, newVal, helper.getPayload());

            if (proceed)
                recordRequests(
                    element,
                    helper.getPayload(),
                    RequestDetailKind.RDT_FINAL_MODIFIED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), true);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreHandledSignalAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreHandledSignalAdded(
        IBehavioralFeature feat,
        ISignal sig,
        IResultCell cell)
    {
        RTStateTester rt = new RTStateTester();
        if (rt.isAppInRoundTripState(feat))
        {    
            RTDispatchHelper helper = 
                    new RTDispatchHelper(m_ProcManager, m_DispController,
                            EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
            helper.establish(ESTABLISH_CREATE, ESTABLISH_NOOVERRIDE, sig, cell);
            
            IClassifierEventDispatcher disp = null;
            try
            {
                disp = (IClassifierEventDispatcher) helper.getDispatcher();
            }
            catch (ClassCastException ignored) { }
            
            boolean proceed = false;
            if (disp != null)
            {    
                proceed = disp.firePreHandledSignalAdded(feat, sig, 
                        helper.getPayload());
                if (proceed)
                    recordRequests(feat, sig, helper.getPayload(), 
                            RequestDetailKind.RDT_SIGNAL_ADDED, null);
                completeResultCell(cell, helper.getCellOrigValue(), proceed);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreHandledSignalRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreHandledSignalRemoved(
        IBehavioralFeature feat,
        ISignal sig,
        IResultCell cell)
    {
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                m_DispController, EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, feat, cell);

        IClassifierEventDispatcher disp = null;
        try
        {
            disp = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (ClassCastException ignored)
        {
        }

        boolean proceed = false;
        if (disp != null)
        {
            proceed = disp.firePreHandledSignalRemoved(feat, sig, helper.getPayload());
            if (proceed)
                recordRequests(sig, helper.getPayload(),
                        RequestDetailKind.RDT_SIGNAL_REMOVED, null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreImpacted(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void onRTPreImpacted(IClassifier classifier, ETList<IVersionableElement> impacted, IResultCell cell )
    {
        if (classifier == null || cell == null) return;
        
        // Impacted makes no sense for a "create" modification.
        // In other words, don't create impact requests when
        // a class is being named from "unnamed"
        if (!new NameModifyPreRequest().inCreateState(classifier))
        {
            if (new RTStateTester().isAppInRoundTripState(classifier))
            {
                if (impacted.size() > 0 )
                {
                    RTDispatchHelper helper =
                        new RTDispatchHelper(
                            m_ProcManager,
                            m_DispController,
                            EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
                    helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, classifier, cell);
                    
                    IClassifierEventDispatcher dispatcher = null;
                    try
                    {
                        dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
                    }
                    catch (Exception e)
                    {
                    }

                    boolean proceed = false;
                    if (dispatcher != null)
        {
                        proceed =
                                dispatcher.firePreImpacted(classifier,impacted, helper.getPayload());
    
    
                        // At this point, we have given the user the chance to cancel based on the size
                        // of the impact, and we given the request processor a change to cancel. If 
                        // we are allowed to proceed, we need to generate a pre-request for each impacted
                        // element.
    
                        if (proceed)
                        {
                            IVersionableElement pItem = null;
                            for (int idx = 0, count = impacted.size(); idx < count ; ++idx)
                            {
                                pItem = impacted.get(idx);
                                if (pItem != null)
                                {
                                    IAttribute pAttr = null;
                                    IParameter pParm = null;
    
                                    if (pItem instanceof IAttribute)
                                    {
                                        pAttr = (IAttribute)pItem;
                                    }
                                    if (pItem instanceof IParameter)
                                    {
                                        pParm = (IParameter)pItem;
                                    }
                                    
                                    if (pAttr != null || pParm != null)
                                    {
                                        IElement elementItem = null;
                                        
                                        if (pItem instanceof IElement)
                                        {
                                            elementItem = (IElement)pItem;
                                        }
    
                                        m_ProcManager.establishProcessors(elementItem, true); 
    
                                        // We dont want to do the fire of a pre-event for the element, since
                                        // we fired a pre for the impacted list. But, we still need a payload.
    
                                        IRoundTripEventPayload rtPayload = createRTContextPayload(cell, elementItem);
    
                                        recordImpactedRequests(elementItem, classifier, rtPayload, RequestDetailKind.RDT_TYPE_MODIFIED);
                                    }
                                }
                            }                        
                        }
                        completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

                }
            }
        } 
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreLeafModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreLeafModified(
        IClassifier element,
        boolean flag,
        IResultCell cell)
    {
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                m_DispController, EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);

        IClassifierEventDispatcher disp = null;
        try
        {
            disp = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (ClassCastException ignored)
        {
        }

        boolean proceed = false;
        if (disp != null)
        {
            proceed = disp.firePreLeafModified(element, flag, helper.getPayload());
            if (proceed)
                recordRequests(element, helper.getPayload(),
                        RequestDetailKind.RDT_LEAF_MODIFIED, null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreLowerModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreLowerModified(
        ITypedElement element,
        IMultiplicity mult,
        IMultiplicityRange range,
        String lower,
        IResultCell cell)
    {
        RTDispatchHelper helper =
            new RTDispatchHelper(
                m_ProcManager,
                m_DispController,
                EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);
                
        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }
        
        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreLowerModified(element, mult, range, lower, helper.getPayload());
                
            recordRequests(
                element,
                helper.getPayload(),
                RequestDetailKind.RDT_LOWER_MODIFIED,
                null);
            completeResultCell(cell, helper.getCellOrigValue(), true);
        }


    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreMultiplicityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreMultiplicityModified(
        ITypedElement element,
        IMultiplicity mult,
        IResultCell cell)
    {
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                m_DispController, EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);

        IClassifierEventDispatcher disp = null;
        try
        {
            disp = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (ClassCastException ignored)
        {
        }

        boolean proceed = false;
        if (disp != null)
        {
            proceed = disp.firePreMultiplicityModified(element, mult, helper.getPayload());
            if (proceed)
                recordRequests(element, helper.getPayload(),
                        RequestDetailKind.RDT_MULTIPLICITY_MODIFIED, null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreNameModified(
        INamedElement element,
        String proposedValue,
        IResultCell cell)
    {
        if (element == null || proposedValue == null) return;
                
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                                                    m_DispController,
                                                    EventDispatchNameKeeper.EDT_ELEMENTMODIFIED_KIND);
        helper.establish(ESTABLISH_CREATE, ESTABLISH_OVERRIDE, element, cell);

           
        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IElementChangeEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IElementChangeEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }

        
        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreNameModified(element, proposedValue, helper.getPayload());
    
            if(proceed)
                recordRequests(element, helper.getPayload(), RequestDetailKind.RDT_NAME_MODIFIED, null);
            
            INamespace pNamespace = null;
            try
            {
                pNamespace = (INamespace)element;
            }
            catch (Exception e)
            {
            }
    
            if (pNamespace != null)
            {
                propagateNamespaceChange(element, RequestDetailKind.RDT_NAME_MODIFIED, true, cell, pNamespace);
            }
    
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreNativeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreNativeModified(
        IFeature element,
        boolean flag,
        IResultCell cell)
    {
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                m_DispController, EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);

        IClassifierEventDispatcher disp = null;
        try
        {
            disp = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (ClassCastException ignored)
        {
        }

        boolean proceed = false;
        if (disp != null)
        {
            proceed = disp.firePreNativeModified(element, flag, helper.getPayload());
            if (proceed)
                recordRequests(element, helper.getPayload(),
                        RequestDetailKind.RDT_NATIVE_MODIFIED, null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreOperationPropertyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, int, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreOperationPropertyModified(
        IOperation oper,
        int nKind,
        boolean proposedValue,
        IResultCell cell)
    {
        if (oper == null || cell == null) return;
        
        if (new RTStateTester().isAppInRoundTripState(oper))
        {
            INamedElement pNElement = (oper instanceof INamedElement)?
                                        (INamedElement)oper:null;
            if (pNElement == null)
            {
               m_ProcManager.establishProcessors(oper, ESTABLISH_OVERRIDE);
            }
            else
            {
               m_ProcManager.establishCreateProcessors(pNElement, ESTABLISH_OVERRIDE);
            }


            IClassifierEventDispatcher pDispatcher;
            IRoundTripEventPayload rtPayload;
            boolean orig = false;
            boolean proceed = false;
        
            // Here is why we cannot use the macro. Because this is a preadd, and we desparatly need
            // a context payload with a good document on it, we cannot use element to create the payload,
            // we must use feat instead.
        
            ETPairT<IClassifierEventDispatcher,IRoundTripEventPayload> pair = 
                getClassifierChangeDispatcherAndPayload(cell, oper);
                
            pDispatcher = pair.getParamOne();
            rtPayload = pair.getParamTwo();
            orig = cell.canContinue();
        
            if (pDispatcher != null)
            {            
                // Fire the event to any listeners ( request processors most likely ), that need
                // to add more changerequests before continuing..
        
                proceed = pDispatcher.firePreOperationPropertyModified(oper, nKind, proposedValue, rtPayload);
                 
                if (proceed)
                {
                    recordRequests(oper, rtPayload, RequestDetailKind.RDT_OPERATION_PROPERTY_CHANGED, null);
                }
        
                completeResultCell (cell, orig, proceed);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreOrderModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreOrderModified(
        ITypedElement element,
        IMultiplicity mult,
        boolean flag,
        IResultCell cell)
    {
        if(element == null || mult == null || cell == null) return;
        
        RTDispatchHelper helper =
            new RTDispatchHelper(
                m_ProcManager,
                m_DispController,
                EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);

        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }

        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreOrderModified(element, mult, flag, helper.getPayload());
    
            if (proceed)
                recordRequests(
                    element,
                    helper.getPayload(),
                    RequestDetailKind.RDT_ORDER_MODIFIED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), true);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreParameterAdded(
        IBehavioralFeature feat,
        IParameter parm,
        IResultCell cell)
    {
        if (new RTStateTester().isAppInRoundTripState(feat))
        {
            boolean establishCreate = ESTABLISH_CREATE;
            if (establishCreate)
            {
                if (parm == null)
                    m_ProcManager.establishProcessors(parm, ESTABLISH_OVERRIDE);
                else
                    m_ProcManager.establishCreateProcessors(parm, ESTABLISH_OVERRIDE);
            }
            else
            {
                m_ProcManager.establishCreateProcessors(parm, ESTABLISH_OVERRIDE);
            }
            
            IClassifierEventDispatcher dispatcher = getClassifierChangeDispatcher();
            if (dispatcher != null)
            {
                IRoundTripEventPayload payload = 
                        getClassifierEventPayload(dispatcher, cell, feat);
                
                boolean orig = cell.canContinue();
                
                boolean proceed = 
                    dispatcher.firePreParameterAdded(feat, parm, payload);
                if (proceed)
                    recordRequests(feat, parm, payload, 
                            RequestDetailKind.RDT_PARAMETER_ADDED, null);
                completeResultCell(cell, orig, proceed);
            }
        }
    }
    
    protected IRoundTripEventPayload getClassifierEventPayload(
            IClassifierEventDispatcher dispatcher, IResultCell cell,
            IElement element )
    {
        IRoundTripEventPayload payload = createRTContextPayload(cell, element);
        return payload;
    }
    
    protected IClassifierEventDispatcher getClassifierChangeDispatcher()
    {
        return new EventDispatchRetriever(m_DispController).getDispatcher(
                EventDispatchNameKeeper.classifier());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreParameterRemoved(
        IBehavioralFeature element,
        IParameter parm,
        IResultCell cell)
    {
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                m_DispController, EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);

        IClassifierEventDispatcher disp = null;
        try
        {
            disp = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (ClassCastException ignored)
        {
        }

        boolean proceed = false;
        if (disp != null)
        {
            proceed = disp.firePreParameterRemoved(element, parm, helper.getPayload());
            if (proceed)
                recordRequests(parm, helper.getPayload(),
                        RequestDetailKind.RDT_PARAMETER_REMOVED, null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreQueryModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreQueryModified(
        IOperation element,
        boolean flag,
        IResultCell cell)
    {
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                m_DispController, EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);

        IClassifierEventDispatcher disp = null;
        try
        {
            disp = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (ClassCastException ignored)
        {
        }

        boolean proceed = false;
        if (disp != null)
        {
            proceed = disp.firePreQueryModified(element, flag, helper.getPayload());
            if (proceed)
                recordRequests(element, helper.getPayload(),
                        RequestDetailKind.RDT_QUERY_MODIFIED, null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreRangeAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreRangeAdded(
        ITypedElement element,
        IMultiplicity mult,
        IMultiplicityRange range,
        IResultCell cell)
    {
        if(element == null || mult == null || cell == null || range == null) return;
        
        RTDispatchHelper helper =
            new RTDispatchHelper(
                m_ProcManager,
                m_DispController,
                EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_CREATE, ESTABLISH_NOOVERRIDE, element, cell);

        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }

        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreRangeAdded(element, mult, range, helper.getPayload());
    
            if (proceed)
                recordRequests(
                    element,
                    helper.getPayload(),
                    RequestDetailKind.RDT_RANGE_ADDED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), true);
        }


    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreRangeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreRangeRemoved(
        ITypedElement element,
        IMultiplicity mult,
        IMultiplicityRange range,
        IResultCell cell)
    {
        if(element == null || mult == null || cell == null || range == null) return;
        
        RTDispatchHelper helper =
            new RTDispatchHelper(
                m_ProcManager,
                m_DispController,
                EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_CREATE, ESTABLISH_NOOVERRIDE, element, cell);

        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }

        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreRangeRemoved(element, mult, range, helper.getPayload());
    
            if (proceed)
                recordRequests(
                    element,
                    helper.getPayload(),
                    RequestDetailKind.RDT_RANGE_REMOVED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), true);
        }


    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreRelationCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreRelationCreated(IRelationProxy proxy, IResultCell cell)
    {
        if (proxy == null || cell == null) return;
        
        RTRelationDispatchHelper helper = new RTRelationDispatchHelper(m_ProcManager, m_DispController);
        helper.establish(proxy, cell);
        
        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IRelationValidatorEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IRelationValidatorEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }

        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreRelationCreated(proxy, helper.getPayload());
            
            if(proceed)    
                recordRelationModifyRequests(
                    proxy,
                    helper.getPayload(),
                    RequestDetailKind.RDT_RELATION_CREATED);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }


    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreRelationDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreRelationDeleted(IRelationProxy proxy, IResultCell cell)
    {
        if (proxy == null || cell == null) return;
        
        RTRelationDispatchHelper helper = new RTRelationDispatchHelper(m_ProcManager, m_DispController);
        helper.establish(proxy, cell);
        
        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IRelationValidatorEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IRelationValidatorEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }
        
        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreRelationDeleted(proxy, helper.getPayload());
            
            if(proceed)    
                recordRelationModifyRequests(
                    proxy,
                    helper.getPayload(),
                    RequestDetailKind.RDT_RELATION_DELETED);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreRelationEndAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreRelationEndAdded(IRelationProxy proxy, IResultCell cell)
    {
        if (proxy == null || cell == null) return;
        
        RTRelationDispatchHelper helper = new RTRelationDispatchHelper(m_ProcManager, m_DispController);
        helper.establish(proxy, cell);
        
        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IRelationValidatorEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IRelationValidatorEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }


        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreRelationEndAdded(proxy, helper.getPayload());
            
            if(proceed)    
                recordRelationModifyRequests(
                    proxy,
                    helper.getPayload(),
                    RequestDetailKind.RDT_RELATION_END_ADDED);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreRelationEndModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreRelationEndModified(
        IRelationProxy proxy,
        IResultCell cell)
    {
        if (proxy == null || cell == null) return;
        
        RTRelationDispatchHelper helper = new RTRelationDispatchHelper(m_ProcManager, m_DispController);
        helper.establish(proxy, cell);
        
        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IRelationValidatorEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IRelationValidatorEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }
        
        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreRelationEndModified(proxy, helper.getPayload());
            
            if(proceed)
                recordRelationModifyRequests(
                    proxy,
                    helper.getPayload(),
                    RequestDetailKind.RDT_RELATION_END_MODIFIED);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreRelationEndRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreRelationEndRemoved(
        IRelationProxy proxy,
        IResultCell cell)
    {
        if (proxy == null || cell == null) return;
        
        RTRelationDispatchHelper helper = new RTRelationDispatchHelper(m_ProcManager, m_DispController);
        helper.establish(proxy, cell);
        
        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        
        IRelationValidatorEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IRelationValidatorEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }

        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreRelationEndRemoved(proxy, helper.getPayload());
            
            if(proceed)    
                recordRelationModifyRequests(
                    proxy,
                    helper.getPayload(),
                    RequestDetailKind.RDT_RELATION_END_REMOVED);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreRelationValidate(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreRelationValidate(IRelationProxy proxy, IResultCell cell)
    {
        if (proxy == null || cell == null) return;

        RTRelationDispatchHelper helper = new RTRelationDispatchHelper(m_ProcManager, m_DispController);
        helper.establish( proxy, cell );
            
        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
            
        IRelationValidatorEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IRelationValidatorEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e) 
        {
        }
        
        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreRelationValidate(proxy, helper.getPayload());            
    
            completeResultCell ( cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreStaticModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreStaticModified(
        IFeature element,
        boolean flag,
        IResultCell cell)
    {
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                m_DispController, EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);

        IClassifierEventDispatcher disp = null;
        try
        {
            disp = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (ClassCastException ignored)
        {
        }

        boolean proceed = false;
        if (disp != null)
        {
            proceed = disp.firePreStaticModified(element, flag, helper.getPayload());
            if (proceed)
                recordRequests(element, helper.getPayload(),
                        RequestDetailKind.RDT_STATIC_MODIFIED, null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreStrictFPModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreStrictFPModified(
        IBehavioralFeature feat,
        boolean flag,
        IResultCell cell)
    {
        if (feat == null)
            return;

        RTDispatchHelper helper =
            new RTDispatchHelper(
                m_ProcManager,
                m_DispController,
                EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, feat, cell);

        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }

        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreStrictFPModified(feat, flag, helper.getPayload());
    
            if (proceed)
                recordRequests(
                    feat,
                    helper.getPayload(),
                    RequestDetailKind.RDT_STRICTFP_MODIFIED,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), true);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreTransform(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreTransform(
        IAssociationEnd pEnd,
        String newForm,
        IResultCell cell)
    {
        if (pEnd == null)
            return;

        RTDispatchHelper helper =
            new RTDispatchHelper(
                m_ProcManager,
                m_DispController,
                EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, pEnd, cell);

        IClassifierEventDispatcher dispatcher = null;
        try
        {
            dispatcher = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (Exception e)
        {
        }

        boolean proceed = false;
        if (dispatcher != null)
        {
            proceed = dispatcher.firePreAssociationEndTransform(pEnd, newForm, helper.getPayload());
    
            if (proceed)
                recordRequests(
                    pEnd,
                    helper.getPayload(),
                    RequestDetailKind.RDT_TRANSFORM,
                    null);
            completeResultCell(cell, helper.getCellOrigValue(), true);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreTransform(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreTransform(
        IClassifier classifier,
        String newForm,
        IResultCell cell)
    {
        RTStateTester rt = new RTStateTester();
        boolean preIsRT  = rt.isElementRoundTripable(classifier);
        boolean postIsRT = rt.isElementRoundTripable(newForm);
        boolean doit     = preIsRT || postIsRT;
        
        if (preIsRT && !postIsRT)
        {
            // this could result in code loss if we are in implementation mode.
            if (isAppInDispatchState(classifier))
            {    
                // warn the user and give him a chance to deny
                // AZTEC: TODO: Implement this dialog?
                IPreferenceQuestionDialog diag = null; //new PreferenceQuestionDialog();
                if (diag != null)
                {
                    // AZTEC: TODO: Dialog maneuvers here.
                }
            }
        }
        
        if (doit)
        {
            RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                    m_DispController,
                    EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
            helper.establish(ESTABLISH_NORMAL, ESTABLISH_OVERRIDE, classifier,
                    cell);

            IClassifierEventDispatcher disp = null;
            try
            {
                disp = (IClassifierEventDispatcher) helper.getDispatcher();
            }
            catch (ClassCastException ignored)
            {
            }

            boolean proceed = false;
            if (disp != null)
        {
                proceed = disp.firePreTransform(classifier, newForm, helper.getPayload());
                if (proceed)
                    recordRequests(classifier, helper.getPayload(),
                            RequestDetailKind.RDT_TRANSFORM, null);
                completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreTransientModified(
        IClassifier element,
        boolean flag,
        IResultCell cell)
    {
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                m_DispController, EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);

        IClassifierEventDispatcher disp = null;
        try
        {
            disp = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (ClassCastException ignored)
        {
        }

        boolean proceed = false;
        if (disp != null)
        {
            proceed = disp.fireClassifierPreTransientModified(element, flag, helper.getPayload());
            if (proceed)
                recordRequests(element, helper.getPayload(),
                        RequestDetailKind.RDT_TRANSIENT_MODIFIED, null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreTransientModified(
        IStructuralFeature element,
        boolean flag,
        IResultCell cell)
    {
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                m_DispController, EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);

        IClassifierEventDispatcher disp = null;
        try
        {
            disp = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (ClassCastException ignored)
        {
        }

        boolean proceed = false;
        if (disp != null)
        {
            proceed = disp.firePreTransientModified(element, flag, helper.getPayload());
            if (proceed)
                recordRequests(element, helper.getPayload(),
                        RequestDetailKind.RDT_TRANSIENT_MODIFIED, null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreTypeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreTypeModified(
        ITypedElement element,
        IClassifier classifier,
        IResultCell cell)
    {
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                m_DispController, EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);
        
        IClassifierEventDispatcher disp = null;
        try
        {
            disp = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (ClassCastException ignored) { }

        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        boolean proceed = false;
        if (disp != null)
        {
            proceed = disp.firePreTypeModified(element, classifier, 
                    helper.getPayload());
            if (proceed)
                recordRequests(element, helper.getPayload(), 
                    RequestDetailKind.RDT_TYPE_MODIFIED, null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreUpperModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreUpperModified(
        ITypedElement element,
        IMultiplicity mult,
        IMultiplicityRange range,
        String upper,
        IResultCell cell)
    {
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                m_DispController, EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);
        
        IClassifierEventDispatcher disp = null;
        try
        {
            disp = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (ClassCastException ignored) { }

        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        boolean proceed = false;
        if (disp != null)
           {
            proceed = disp.firePreUpperModified(element, mult, range, upper,
                    helper.getPayload());
            if (proceed)
                recordRequests(element, helper.getPayload(), 
                    RequestDetailKind.RDT_UPPER_MODIFIED, null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreVisibilityModified(
        INamedElement element,
        int proposedValue,
        IResultCell cell)
    {
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                m_DispController, EventDispatchNameKeeper.EDT_ELEMENTMODIFIED_KIND);
        helper.establish(ESTABLISH_CREATE, ESTABLISH_NOOVERRIDE, element, cell);
        
        IElementChangeEventDispatcher disp = null;
        try
        {
            disp = (IElementChangeEventDispatcher) helper.getDispatcher();
        }
        catch (ClassCastException ignored) { }

        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..
        boolean proceed = false;
        if (disp != null)
        {
            proceed = disp.firePreVisibilityModified(element, proposedValue, 
                        helper.getPayload());
            if (proceed)
                recordRequests(element, helper.getPayload(), 
                        RequestDetailKind.RDT_VISIBILITY_MODIFIED, null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTPreVolatileModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTPreVolatileModified(
        IStructuralFeature element,
        boolean flag,
        IResultCell cell)
    {
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                m_DispController, EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
        helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell);

        IClassifierEventDispatcher disp = null;
        try
        {
            disp = (IClassifierEventDispatcher) helper.getDispatcher();
        }
        catch (ClassCastException ignored)
        {
        }

        boolean proceed = false;
        if (disp != null)
        {
            proceed = disp.firePreVolatileModified(element, flag, helper.getPayload());
            if (proceed)
                recordRequests(element, helper.getPayload(),
                        RequestDetailKind.RDT_VOLATILE_MODIFIED, null);
            completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTQueryModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTQueryModified(IOperation oper, IResultCell cell)
    {
        processRequests(oper, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRaisedExceptionAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRaisedExceptionAdded(
        IOperation oper,
        IClassifier pException,
        IResultCell cell)
    {
        processRequests(oper, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRaisedExceptionPreAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRaisedExceptionPreAdded(
        IOperation oper,
        IClassifier pException,
        IResultCell cell)
    {
        if (new RTStateTester().isAppInRoundTripState(oper))
        {
            RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                    m_DispController,
                    EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
            helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, oper,
                    cell);

            IClassifierEventDispatcher disp = null;
            try
            {
                disp = (IClassifierEventDispatcher) helper.getDispatcher();
            }
            catch (ClassCastException ignored)
            {
            }

            boolean proceed = false;
            if (disp != null)
            {
                proceed = disp.fireRaisedExceptionPreAdded(oper, pException, helper.getPayload());
                if (proceed)
                    recordRequests(oper, oper, helper.getPayload(),
                            RequestDetailKind.RDT_EXCEPTION_ADDED, null);
                completeResultCell(cell, helper.getCellOrigValue(), proceed);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRaisedExceptionPreRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRaisedExceptionPreRemoved(
        IOperation oper,
        IClassifier pException,
        IResultCell cell)
    {
        if (new RTStateTester().isAppInRoundTripState(oper))
        {
            RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager,
                    m_DispController,
                    EventDispatchNameKeeper.EDT_CLASSIFIER_KIND);
            helper.establish(ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, oper,
                    cell);

            IClassifierEventDispatcher disp = null;
            try
            {
                disp = (IClassifierEventDispatcher) helper.getDispatcher();
            }
            catch (ClassCastException ignored)
            {
            }

            boolean proceed = false;
            if (disp != null)
        {
                proceed = disp.fireRaisedExceptionPreRemoved(oper, pException, helper.getPayload());
                if (proceed)
                    recordRequests(oper, oper, helper.getPayload(),
                            RequestDetailKind.RDT_EXCEPTION_REMOVED, null);
                completeResultCell(cell, helper.getCellOrigValue(), proceed);
        }

        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRaisedExceptionRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRaisedExceptionRemoved(
        IOperation oper,
        IClassifier pException,
        IResultCell cell)
    {
        processRequests(oper, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRangeAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRangeAdded(
        ITypedElement element,
        IMultiplicity mult,
        IMultiplicityRange range,
        IResultCell cell)
    {
        processRequests(element, ChangeKind.CT_MODIFY);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRangeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRangeRemoved(
        ITypedElement element,
        IMultiplicity mult,
        IMultiplicityRange range,
        IResultCell cell)
    {
        processRequests(element, ChangeKind.CT_MODIFY);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRelationCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRelationCreated(IRelationProxy proxy, IResultCell cell)
    {
        processRelModRequests(proxy, ChangeKind.CT_CREATE);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRelationDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRelationDeleted(IRelationProxy proxy, IResultCell cell)
    {
        processRelModRequests(proxy, ChangeKind.CT_DELETE);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRelationEndAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRelationEndAdded(IRelationProxy proxy, IResultCell cell)
    {
        processRelModRequests(proxy, ChangeKind.CT_MODIFY);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRelationEndModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRelationEndModified(IRelationProxy proxy, IResultCell cell)
    {
        processRelModRequests(proxy, ChangeKind.CT_MODIFY);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRelationEndRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRelationEndRemoved(IRelationProxy proxy, IResultCell cell)
    {
        processRelModRequests(proxy, ChangeKind.CT_MODIFY);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTRelationValidate(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTRelationValidate(IRelationProxy proxy, IResultCell cell)
    {
        // No valid implementation in the C++ code base.
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTStaticModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTStaticModified(IFeature feat, IResultCell cell)
    {
        processRequests(feat, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTStrictFPModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTStrictFPModified(IBehavioralFeature feat, IResultCell cell)
    {
        processRequests(feat, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTTransformed(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTTransformed(IAssociationEnd pEnd, IResultCell cell)
    {
        processRequests(pEnd, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTTransformed(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTTransformed(IClassifier classifier, IResultCell cell)
    {
        processRequests(classifier, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTTransientModified(IClassifier classifier, IResultCell cell)
    {
        processRequests(classifier, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTTransientModified(
        IStructuralFeature feat,
        IResultCell cell)
    {
        processRequests(feat, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTTypeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTTypeModified(ITypedElement element, IResultCell cell)
    {
        processRequests(element, ChangeKind.CT_MODIFY);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTUpperModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTUpperModified(
        ITypedElement element,
        IMultiplicity mult,
        IMultiplicityRange range,
        IResultCell cell)
    {
        processRequests(element, ChangeKind.CT_MODIFY);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTVisibilityModified(INamedElement element, IResultCell cell)
    {
        processRequests(element, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTVolatileModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTVolatileModified(IStructuralFeature feat, IResultCell cell)
    {
        processRequests(feat, ChangeKind.CT_MODIFY, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onSourceDirModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTSourceDirModified(IPackage element, IResultCell cell)
    {
        processRequests(element, ChangeKind.CT_MODIFY, null);
        propagateNamespaceChange(
                element, RequestDetailKind.RDT_SOURCE_DIR_CHANGED, false, cell, 
                element instanceof INamespace? (INamespace) element : null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTWSProjectClosed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTWSProjectClosed(IWSProject project, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTWSProjectCreated(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTWSProjectCreated(IWSProject project, IResultCell cell)
    {
        if (project != null)
        {    
            m_ProcManager.establishProcessorsForProject(project);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTWSProjectInserted(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTWSProjectInserted(IWSProject project, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTWSProjectOpened(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTWSProjectOpened(IWSProject project, IResultCell cell)
    {
        if (project != null)
        {    
            m_ProcManager.establishProcessorsForProject(project);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTWSProjectPreClose(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTWSProjectPreClose(IWSProject project, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTWSProjectPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTWSProjectPreCreate(
        IWorkspace space,
        String projectName,
        IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTWSProjectPreInsert(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTWSProjectPreInsert(
        IWorkspace space,
        String projectName,
        IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTWSProjectPreOpen(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTWSProjectPreOpen(
        IWorkspace space,
        String projName,
        IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTWSProjectPreRemove(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTWSProjectPreRemove(IWSProject project, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTWSProjectPreRename(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTWSProjectPreRename(
        IWSProject project,
        String newName,
        IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTWSProjectPreSave(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTWSProjectPreSave(IWSProject project, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTWSProjectRemoved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTWSProjectRemoved(IWSProject project, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTWSProjectRenamed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTWSProjectRenamed(
        IWSProject project,
        String oldName,
        IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#onRTWSProjectSaved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRTWSProjectSaved(IWSProject project, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#processDuplicateRequests(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, int)
     */
    public void processDuplicateRequests(
        IElement reqElement,
        IElement origElement,
        IElement dupeElement,
        int type)
    {
        if (reqElement == null || origElement == null || dupeElement == null)
            return ;
        
        gatherDuplicateRequests(reqElement, origElement, dupeElement, type);
        processRequests();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#processImpactedRequests(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, int)
     */
    public void processImpactedRequests(
        IElement element,
        IClassifier pModifiedClass,
        int type)
    {
        if(element == null) return;
        processRequests(element, type, null);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#processRelModRequests(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, int)
     */
    public void processRelModRequests(IRelationProxy proxy, int type)
    {
        gatherRelModRequests(proxy, type);
        processRequests();
    }
    
    /**
     * @param proxy[in]
     * @param type[in]
     *
     * @return 
     */
    protected void gatherRelModRequests(IRelationProxy proxy, int type)
    {
        // Go over the list of prerequests. If the prerequest is for the element,
        // get the processor off the prereq. Find the entry on the post elements
        // list that corresponds to the processor. If found, create a change
        // request and add to the list. If not found, create a new entry, create
        // a change request, and add to the list.
        for (int i = 0, count = m_PreElements.size(); i < count; ++i)
        {
            IPreRequest pre = m_PreElements.get(i);
            if (pre == null) continue;
            if (pre.postEvent(proxy))
            {
                // This pre request is for the element. Create a change request
                IRequestProcessor preReqProc = pre.getRequestProcessor(null);
                if (preReqProc != null)
                {
                    ETList<IChangeRequest> changeReqs =
                        getPostElementChangeRequestList(preReqProc);
                    if (changeReqs != null)
                    {
                        // The fact that we still need the connection is now moot. Strive to remove it.
                        IElement connection = proxy.getConnection();
                        if (connection != null)
                        {
                            IChangeRequest newChangeReq = 
                                createRequest(connection, pre, type, 
                                    pre.getDetail());
                            if (newChangeReq != null)
                                changeReqs.add(newChangeReq);
                        }
                    }
                }
                
                // Delete this prerequest from the list
                m_PreElements.remove(i--);
                count--;
            }
        }
    }
    
    protected void gatherDuplicateRequests(IElement reqElement, 
                                           IElement origElement,
                                           IElement dupeElement,
                                           int type)
    {
        // Go over the list of prerequests. If the prerequest is for the element,
        // get the processor off the prereq. Find the entry on the post elements
        // list that corresponds to the processor. If found, create a change
        // request and add to the list. If not found, create a new entry, create
        // a change request, and add to the list.
        for (int i = 0, count = m_PreElements.size(); i < count; ++i)
        {
            IPreRequest p = m_PreElements.get(i);
            if (p == null || !p.postEvent(reqElement)) continue;
            
            // This pre request is for the element. Create a change request
            IRequestProcessor req = p.getRequestProcessor(null);
            if (req != null)
            {
                ETList<IChangeRequest> changeReqs =
                    getPostElementChangeRequestList(req);
                if (changeReqs != null)
                {
                    p.setOrigElement(origElement);
                    p.setDupeElement(dupeElement);
                    
                    IChangeRequest newR = 
                            createRequest(reqElement, p, type, p.getDetail());
                    if (newR != null)
                        changeReqs.add(newR);
                }
            }
            
            // Delete this prerequest from the list
            m_PreElements.remove(i--);
            count--;
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#processRequests(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, int, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature)
     */
    public void processRequests(
        IElement element,
        int type,
        IBehavioralFeature feature)
    {
        if(element == null) return;

        try
        {
           gatherRequests(element, type, feature);
           processRequests();
        }
        finally
        {
           clearClone(element);
        }
    }
    
    public void processRequests(
            IElement element,
            int type)
    {
        processRequests(element, type, null);
    }
    
    /**
     *
     * Gathers all requests associated with the passed in element.
     *
     * @param element[in] The element to match against
     * @param type[in]    The type of the change coming in
     * @param feature[in] In the ParameterRemoved event, this will be the feature
     *                    that the parameter was once owned by. In every other case,
     *                    it will be 0.
     *
     * @return HRESULT
     *
     */
    protected void gatherRequests(IElement element, int changeKind, 
                                  IBehavioralFeature feat)
    {
        // Go over the list of prerequests. If the prerequest is for the element,
        // get the processor off the prereq. Find the entry on the post elements
        // list that corresponds to the processor. If found, create a change
        // request and add to the list. If not found, create a new entry, create
        // a change request, and add to the list.
        for (int i = 0, count = m_PreElements.size(); i < count; ++i)
        {
            IPreRequest pr = m_PreElements.get(i);
            if (pr == null) continue;
            
            if (pr.postEvent(element))
            {
                // This pre request is for the element. Create a change request
                IRequestProcessor preReqProc = pr.getRequestProcessor(null);
                if (preReqProc != null)
                {
                    ETList<IChangeRequest> changeReqs = 
                            getPostElementChangeRequestList(preReqProc);
                    if (changeReqs != null)
                    {
                        IChangeRequest newCR = 
                                createRequest(element, pr, changeKind, 
                                        pr.getDetail());
                        if (newCR != null)
                        {
                            // If this element is taking part in a compound change request,
                            // add the change request to the compound, not to the 
                            // post element. We have to treat parameters special.
                            boolean foundit = false;
                            IElement compoundElement = element;
                            if (element instanceof IParameter)
                            {
                                IBehavioralFeature op = 
                                        ((IParameter) element).getBehavioralFeature();
                                if (op == null)
                                {
                                    // Double check to see if the feature
                                    // was passed in from the remove parameter event.
                                    // In the remove parameter case, get_BehavioralFeature
                                    // will always return a 0 for its owning Operation, as
                                    // it was just removed
                                    op = feat;
                                }
                                
                                if (op != null)
                                    compoundElement = op;
                            }
                            for (int j = 0, ncount = m_CompoundRequests.size();
                                    j < ncount; ++j)
                            {
                                CompoundRequestListItem item = m_CompoundRequests.get(j);
                                if (item == null) continue;
                            
                                if (item.isSame(compoundElement))
                                {
                                    // we already have one. Just increment this one.
                                    foundit = true;
                                    item.add(newCR);
                                
                                    // we cannot exit at this time, using foundit, because 
                                    // we might have more than one request processor associated
                                    // with this element, meaning we created a compound request
                                    // for each one, and so we need to add to them all.
                                    // But, we use foundit to say "we don't need to add to the post element"
                                }
                            }
                        
                            // if the element was not taking part in a compound, simply the new
                            // change request to the post element
                            if (!foundit)
                            {    
                                changeReqs.add(newCR);
                            }
                        }
                    }
                }
                
                // Delete this prerequest from the list
                m_PreElements.remove(i--);
                count--;
            }
        }
    }
    
    /**
     * Creates the actual request object
     *
     * @param element[in] The post-element
     * @param preReq[in] The struct used in order to retrieve the Pre-element
     * @param type[in]
     * @param detail[in]

     * @param req[out] The created request
     *
     * @return HRESULT
     */
    protected IChangeRequest createRequest(IElement element, IPreRequest pre,
                                           int changeKind, int rdkDetail)
    {
        // if the element is a parameter, we want to create a parameter 
        // change request.
        return pre.createChangeRequest(element, changeKind, rdkDetail);
    }
    
    protected ETList<IChangeRequest> getPostElementChangeRequestList(
            IRequestProcessor rp)
    {
        boolean foundit = false;
        ETList<IChangeRequest> changeReqs = null;
        for (int i = m_PostElements.size() - 1; i >= 0; --i)
        {
            ETPairT<IRequestProcessor, ETList<IChangeRequest>> pair =
                    m_PostElements.get(i);
            IRequestProcessor postElementProc = pair.getParamOne();
            
            // The RequestProcessors probably don't implement equals(), so this
            // really is a reference comparison. Is that okay, or is it possible
            // to have two different RequestProcessors that do the same thing?
            if (postElementProc != null && postElementProc.equals(rp))
            {
                foundit = true;
                changeReqs = pair.getParamTwo();
                break;
            }
        }
        
        if (!foundit)
        {
            // need to add a new entry onto the post elements list first
            changeReqs = new ETArrayList<IChangeRequest>();
            m_PostElements.add( 
                    new ETPairT<IRequestProcessor, ETList<IChangeRequest>>(
                            rp, changeReqs ) );
        }

        // Hand back the list so that the caller can add to it.
        return changeReqs;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRTEventManager#processRequests(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, int)
     */
    public void processRequests(IRelationProxy proxy, int type)
    {
        // C++ code also commented out - apparently this method should never be
        // used.
    }
    
    // ***** Protected Methods *****
    
    /**
     *
     * Determines whether or not the passed in relation is associated
     * with artifacts that correspond to well-known RequestProcessors.
     * If it does, an entry is put into our internal map of pre-elements,
     * awaiting a post event that will cause a ProcessRequest on the 
     * associated RequestProcessor
     *
     * @param proxy[in] The relation to check
     * @param eventName[in] Name of the event. This is used to create the appropriate 
     *                      PreRequest object.
     * @param pModifiedNamespace[in] If set, this is the actual namespace being modified
     *
     * @return HRESULT
     *
     */
    
    protected void establishPreRequest(IRelationProxy proxy, 
                                        /*RequestDetailKind*/ int eventName,
                                        IEventPayload payload,
                                        INamespace pModifiedNamespace)
    {
        if (proxy == null) return;
        // XXX : Need to get both (all) elements off the relation and build 
        // prerequests for both (all) of them.
    
        // Note to Warren : RelationProxy == GDKMal
    
        // Ok, we have changed the way we handle AssociationEnd modifies.
        // Because associations are non-direction (from and to don't mean
        // anything), we had to pass the end itself as the connection on the 
        // proxy.
    
        boolean established = false;
    
        if (eventName == RequestDetailKind.RDT_ASSOCIATION_END_MODIFIED ||
               eventName == RequestDetailKind.RDT_ASSOCIATION_END_ADDED    ||
               eventName == RequestDetailKind.RDT_ASSOCIATION_END_REMOVED )
        {
            IElement pConnection = proxy.getConnection();
            if (pConnection != null)
            {
                IAssociationEnd pEnd  = (IAssociationEnd)pConnection;
                if (pEnd != null)
                {
                    establishPreRequest ( pEnd, eventName, payload, proxy, pModifiedNamespace);
                    established = true;
                }
            }
        }
        else if ( eventName == RequestDetailKind.RDT_RELATION_END_MODIFIED ||
                    eventName == RequestDetailKind.RDT_RELATION_END_ADDED    ||
                    eventName == RequestDetailKind.RDT_RELATION_END_REMOVED  ||
                    eventName == RequestDetailKind.RDT_RELATION_CREATED      ||
                    eventName == RequestDetailKind.RDT_RELATION_DELETED )
        {
            IElement pConnection = proxy.getConnection();
            if (pConnection != null)
            {
                establishPreRequest(pConnection, eventName, payload, proxy, pModifiedNamespace);
                established = true;
            }
        }
    
        if (!established)
        {
             IElement pFrom = proxy.getFrom();
             IElement pTo = proxy.getTo();
    
   
             establishPreRequest ( pFrom, eventName, payload, proxy, pModifiedNamespace);
             establishPreRequest ( pTo,   eventName, payload, proxy, pModifiedNamespace);
        } 
    }    


    /**
    * Determines whether or not the passed in element is associated
    * with artifacts that correspond to well-known RequestProcessors.
    * If it does, an entry is put into our internal map of pre-elements,
    * awaiting a post event that will cause a ProcessRequest on the 
    * associated RequestProcessor
    *
    * @param element[in] The element to check
    * @param eventName[in] Name of the event. This is used to create the appropriate 
    *                      PreRequest object.
    *
    * @return HRESULT
    *
    */

    protected void establishPreRequest(IElement element, 
                                      /*RequestDetailKind*/ int eventName,
                                        IEventPayload payload,
                                        IRelationProxy proxy,
                                        INamespace pModifiedNamespace)
    {
        establishPreRequest ( element, element, eventName, payload, proxy, pModifiedNamespace);
    }

 
   /**
    *
    * Determines whether or not the passed in element is associated
    * with artifacts that correspond to well-known RequestProcessors.
    * If it does, an entry is put into our internal map of pre-elements,
    * awaiting a post event that will cause a ProcessRequest on the 
    * associated RequestProcessor
    *
    * @param elementWithArtifact[in] The element to check
    * @param element[in] The element the request is for, which in the case of an add,
    *                    might not be the same as elementWithArtifact
    * @param eventName[in] Name of the event. This is used to create the appropriate 
    *                      PreRequest object.
    *
    * @return HRESULT
    *
    */

    protected void establishPreRequest(IElement elementWithArtifact, 
                                        IElement element, 
                                        /*RequestDetailKind*/ int eventName, 
                                        IEventPayload payload, 
                                        IRelationProxy proxy,
                                        INamespace pModifiedNamespace)
    {
        if (( element != null || proxy != null) && elementWithArtifact != null)
        {
            // We SHOULD I guess, check the element to see if it is roundtripable, and if the 
            // app is in a roundtrip mode, but WE SHOULD HAVE ALREADY DONE THIS because a 
            // process had to have been established first. That is done in the macros.

            ETList< ILanguage > langs = elementWithArtifact.getLanguages();

            if (langs != null)
            {
                int count = langs.size();
                int idx = 0;
                ILanguage pItem = null;
                while (idx < count)
                {
                    pItem = langs.get(idx);
                    idx++;
                    if (pItem != null)
                    {
                        IRequestProcessor proc = m_ProcManager.establishProcessor(pItem);
    
                        if (proc != null && element != null)
                        {
                            // if this is not a signature change
    
                            if (eventName != RequestDetailKind.RDT_SIGNATURE_CHANGED)
                            {
                                buildPreRequest(elementWithArtifact, element, proc, eventName, payload, proxy, pModifiedNamespace); 
                            }
                            else
                            {
                                // in this case, our "pre-request" is a compound change request  list item
                                IElement pClone = cloneElement(element);
    
                                CompoundRequestListItem pCRLItem = new CompoundRequestListItem(element,
                                                                                            pClone,
                                                                                            new OperationSignatureChangeRequest(),
                                                                                            proc,
                                                                                            eventName,
                                                                                            payload);
    
                                m_CompoundRequests.add(pCRLItem);
    
                                // since we are pushing it onto the list now, we increment it now.
                                pCRLItem.increment();
                            }
                        }
                    }
                }
            }
        }
    }
//
////    ----------------------------------------------------------------------------
////    ----------------------------------------------------------------------------
   /**
    *
    * Checks to see if the passed in artifact is associated with a RequestProcessor
    * the round trip framework knows about. If so, an entry is made to the internal
    * map of requests waiting to be processed.
    *
    * @param element[in] The element in question
    * @param fileName[in] The file associated with the element.
    * @param eventName[in] Name of the event. This is used to create the appropriate 
    *                      PreRequest object.
    *
    * @return HRESULT
    *
    */

    protected void buildPreRequest (IElement elementOwner, 
                                   IElement element, 
                                   IRequestProcessor proc,
                                   /*RequestDetailKind*/ int eventName,
                                   IEventPayload payload, 
                                   IRelationProxy proxy,
                                   INamespace pModifiedNamespace)
    {         
        if (proc != null)
        {
            // Clone the element to make sure we are absolutely dealing with a
            // before and after picture.

            IElement clonedElement = cloneElement(element);

            if(clonedElement != null)
            {
                IPreRequest preReq = PreRequestFactory.createPreRequest(eventName, 
                                                            element,
                                                            clonedElement, 
                                                            elementOwner, 
                                                            proc, 
                                                            payload, 
                                                            proxy );

                // If the element is a parameter, we need to clone the operation NOW.

                String elementtype = element.getElementType();

                if ("Parameter".equals(elementtype))
                {
                    IElement pOwner = element.getOwner();
                    if (pOwner == null)
                    {
                        // use the passed in owner as a last resort. This would be needed during a
                        // preparameter added 
                        pOwner = elementOwner;
                    }

                    if (pOwner != null)
                    {
                        IElement clonedOwner = cloneElement(pOwner);
                        preReq.setPreOwnerElement(clonedOwner);
                    }
                }

                preReq.setModifiedNamespace(pModifiedNamespace);
                m_PreElements.add(preReq);
            }
        }
    }
//
   protected void clearClone(IElement element)
   {
      if(element != null)
      {
         if (element instanceof IPackage)
         {
             // never clone the package. Too expensive
         }
         else
         {
            Node node = element.getNode();
            if (node != null)
            {
                FactoryRetriever.instance().clearClonedStatus(node);
            }
         }
      }
   }
   
   /**
    *
    * Clones the passed in element.
    *
    * @param element[in] Element to clone
    * @param cloned[out] The clone of element
    *
    * @return HRESULT
    *
    */

    protected IElement cloneElement(IElement element)
    {
        if(element == null) return null;
        
        IPackage pPackage = element instanceof IPackage? (IPackage) element : null;
        if (pPackage != null)
        {
            // never clone the package. Too expensive
            return pPackage;
        }
        else
        {
            Node node = element.getNode();
            if (node != null)
            {
                return (IElement)FactoryRetriever.instance().clone(node);
            }
        }
        return null;
    }

//
////    ----------------------------------------------------------------------------
////    ----------------------------------------------------------------------------
   /**
    *
    * 
    *
    *
    * @return 
    *
    */

   protected void processRequests()
   {
        // Start a batch so that these elements, which all pertain to the same
        // IElement, are in the same batch.

        int batchLevel = 0; //StartBatch();
        boolean inBatch = false;
        int count = m_PostElements.size();
        // Now loop through the PostElement collection, and process the requests
        for (int i = 0 ; i < count ; ++i)
        {
            if (!inBatch )
            {
                batchLevel = startBatch(null);
                inBatch = true;
            }

            ETPairT<IRequestProcessor, ETList<IChangeRequest>> temp = m_PostElements.get(i);
            
            m_PostElements.remove(i--);
            --count;
            
            IRequestProcessor proc = temp.getParamOne();
            ETList< IChangeRequest > reqs = temp.getParamTwo();

            if (proc != null && reqs != null)
            {
                ETList< IChangeRequest > validatedReqs = proc.processRequests(reqs);

                if( validatedReqs != null)
                {
                    dispatchRequests(validatedReqs);
                }
            }
        }

        // terminate the batch mode if we entered it.

        if (inBatch)
        {
            batchLevel = endBatch(null);
        }
    }

    /**
     *
     * 
     *
     *
     * @return 
     *
     */

    protected int startBatch(IEventContext pContext)
    {
        if (isBatchContext(pContext))
        {
            if (m_BatchCount == 0)
            {
                if (enterBatch(pContext))
                {
                    m_BatchCount++;
                }
            }
            else
            {
                 m_BatchCount++;
            }
       }
       return m_BatchCount;
    }


    /**
     *
     * 
     *
     *
     * @return 
     *
     */

    protected boolean enterBatch(IEventContext pContext)
    {
        IRoundTripEventDispatcher disp = m_Controller.getRoundTripDispatcher();
        if(disp != null)
        {
            IBatchEventContext pBatch = null;
            try
            {
                pBatch = (IBatchEventContext)pContext;
            }
            catch(Exception e){}
            
            if (pBatch == null)
            {
                pBatch = new BatchEventContext();
                pBatch.setName("Processing Requests");
                
            }

            disp.pushEventContext3(pBatch);
        }
        return true;
    }



    /**
     *
     * 
     *
     *
     * @return 
     *
     */

    protected int endBatch(IEventContext pContext)
    {
        if (isBatchContext(pContext))
        {
            if (m_BatchCount > 0)
            {
                m_BatchCount--;

                if (m_BatchCount == 0)
                {
                    exitBatch();
                    m_BatchRequests = null;
                }
            }
        }

        return m_BatchCount;
    }

    /**
     *
     * 
     *
     *
     * @return 
     *
     */

    protected void exitBatch()
    {
        IRoundTripEventDispatcher disp = m_Controller.getRoundTripDispatcher();

        if(disp != null)
        {
            disp.popEventContext();
        }
    }

    /**
     *
     * 
     *
     * @param pContext[in]
     *
     * @return 
     *
     */

    protected boolean isBatchContext(IEventContext pContext)
    {
        boolean retval = false;

        if (pContext == null)
        {
            // A null context means that roundtrip itself is entering batch mode.
            // So a null context is indeed a batch context
            retval = true;
        }    
        else
        {
            IBatchEventContext pBatch = null;
            try
            {
                pBatch = (IBatchEventContext)pContext;
            }
            catch(Exception e){}

            if (pBatch != null)
            {
                retval = true;
    
            }
       }

       return retval;
    }

    /**
     *
     * 
     *
     * @param pRequest[in]
     *
     * @return 
     *
     */

    protected void addBatchRequest(IChangeRequest pRequest)
    {
        // Only add requests to this list if we are actually in batch mode.
        if (m_BatchCount > 0)
        {
            if (pRequest != null)
            {
                if (m_BatchRequests == null)
                {
                    m_BatchRequests = new ETArrayList<IChangeRequest>();
                }
                if (m_BatchRequests != null)
                {
                    m_BatchRequests.add(pRequest);
                }
            }
        }
    }

    /**
     *
     * 
     *
     * @param pRequest[in]
     *
     * @return 
     *
     */

    protected boolean isSimilarOnBatchRequestList(IChangeRequest pRequest)
    {
        boolean retval = false;

        if (pRequest != null)
        {
            IMultipleParameterTypeChangeRequest pMultiple = null;
            try
            {
                pMultiple = (IMultipleParameterTypeChangeRequest)pRequest;
            }
            catch(Exception e){}
            
            if (pMultiple != null)
            {
                if (m_BatchRequests != null)
                {
                    IOperation pOp = pMultiple.getImpactedOperation();
                    String oldTypeName = pMultiple.getOldTypeName();
                    String newTypeName = pMultiple.getNewTypeName();
                    
                    int count = m_BatchRequests.size();
                    int i = 0;
                    IChangeRequest pItem = null;
                    while (i < count && !retval)
                    {
                        pItem = m_BatchRequests.get(i);
                        ++i;
                        
                        if (pItem != null)
                        {
                            IMultipleParameterTypeChangeRequest pMultItem = null;
                            try
                            {
                                pMultItem = (IMultipleParameterTypeChangeRequest)pItem;
                            }
                            catch(Exception e){}

                            if (pMultItem != null)
                            {
                                IOperation pTestOp = pMultItem.getImpactedOperation();
                                String test_oldTypeName = pMultItem.getOldTypeName();
                                String test_newTypeName = pMultItem.getNewTypeName();


                                boolean isSame = false;
                                if (pOp != null && pTestOp != null)
                                {
                                    isSame = pOp.isSame(pTestOp);
                                    if (isSame)
                                    {
                                        isSame = false;
                                        if ( oldTypeName == null && test_oldTypeName == null )
                                        {
                                            isSame = true;
                                        }
                                        else if (oldTypeName.equals(test_oldTypeName))
                                        {
                                            isSame = true;
                                        }

                                        if (isSame)
                                        {
                                            isSame = false;
                                            if (newTypeName == null && test_newTypeName == null)
                                            {
                                                isSame = true;
                                            }
                                            else if ( newTypeName.equals(test_newTypeName))
                                            {
                                                isSame = true;
                                            }

                                            if (isSame)
                                            {
                                                // THIS IS THE SAME REQUEST!!!!
                                                // Not the same instance, mind you, but they look identical.
                                                retval = true;
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
        return retval;   
    }

    /**
     *
     * Dispatches the requests that have been validated / generated
     * by the various RequestProcessors.
     *
     * @param reqs[in] The ChangeRequests that need to be processed
     *
     * @return HRESULT
     *
     */
    
    public void dispatchRequests(ETList<IChangeRequest> reqs)
    {
        if(reqs == null) return;
    
        int count = reqs.size();
        boolean proceed = true;
        IChangeRequest req = null;

        for(int x = 0; x < count && proceed ; x++)
        {
            req = reqs.get(x);    
            proceed = dispatchRequest(req);
        }
    }

    /**
     *
     * Performs the actual dispatching to the listeners of the RoundTrip sinks.
     *
     * @param preLoadName[in] The name of the pre-event
     * @param postLoadName[in] The name of the post-event
     * @param preFunc[in] The function pointer that points at the pre event launch method
     * @param postFunc[in] The function pointer that points at the post event launch method
     * @param req[in] The ChangeRequest to broadcast
     * @param proceed[out] The continue flag returned from the pre event
     *
     * @return HRESULT
     *
     */
    
    protected boolean dispatchRequest(String preLoadName,
                                      String postLoadName,
                                      EventFunctor  preFunc,
                                      EventFunctor  postFunc,
                                      IChangeRequest req)
    {
        if(req == null) return false;
        
        IRoundTripEventDispatcher disp = m_Controller.getRoundTripDispatcher();
        if(disp != null)
        {
            IEventPayload payload = disp.createPayload(preLoadName);    
            preFunc.setParameters(new Object[]{req,payload});
            preFunc.execute(disp);
    
            payload = disp.createPayload(postLoadName);
            postFunc.setParameters(new Object[]{req,payload});
            postFunc.execute(disp);
        }
        return true;
    }

    /**
     *
     * Determines the appropriate RoundTrip event sink to send
     * the passed in ChangeRequest to. Once done, dispatches it.
     *
     * @param req[in] The change request to dispatch
     * @param proceed[out] true if it is ok to continue dispatching, else false
     *
     * @return HRESULT
     *
     */
    
    protected boolean dispatchRequest(IChangeRequest req)
    {
        if (req == null || m_Controller == null) return false;
    
        // first must see if we are in a dispatchable (implementation mode) project
    
        if (isRequestDispatchable(req))
        {
            IRoundTripEventDispatcher disp = m_Controller.getRoundTripDispatcher();
            EventFunctor preFunc = null;
            EventFunctor postFunc = null;
            String dispatcherClassName = "org.netbeans.modules.uml.core.roundtripframework.RoundTripEventDispatcher";
            if (disp != null)
            {
                int /*RTElementKind*/ type = req.getElementType();
    
                int /*RequestDetailKind*/ detail = req.getRequestDetailType();
                if ( detail != RequestDetailKind.RDT_TRANSFORM )
                {
                    switch( type )
                    {
                        case RTElementKind.RCT_CLASS:
                        case RTElementKind.RCT_TEMPLATE_PARAMETER:
                        case RTElementKind.RCT_INTERFACE:
                            preFunc = new EventFunctor(dispatcherClassName,
                                                        "firePreClassChangeRequest");
                            postFunc = new EventFunctor(dispatcherClassName,
                                                        "fireClassChangeRequest");
                            dispatchRequest("PreClassChangeRequest",
                                            "ClassChangeRequest",
                                            preFunc,
                                            postFunc,
                                            req);
                        break;
                        
                        case RTElementKind.RCT_ENUMERATION:
                            preFunc = new EventFunctor(dispatcherClassName,
                                                        "firePreEnumerationChangeRequest");
                            postFunc = new EventFunctor(dispatcherClassName,
                                                        "fireEnumerationChangeRequest");
                            dispatchRequest("PreEnumerationChangeRequest",
                                            "EnumerationChangeRequest",
                                            preFunc,
                                            postFunc,
                                            req);
                            break;
                            
                        case RTElementKind.RCT_ATTRIBUTE:
                        case RTElementKind.RCT_NAVIGABLE_END_ATTRIBUTE :
                            preFunc = new EventFunctor(dispatcherClassName,
                                                        "firePreAttributeChangeRequest");
                            postFunc = new EventFunctor(dispatcherClassName,
                                                        "fireAttributeChangeRequest");
                            dispatchRequest("PreAttributeChangeRequest",
                                            "AttributeChangeRequest",
                                            preFunc,
                                            postFunc,
                                            req);
                        break;
    
                        case RTElementKind.RCT_ENUMERATION_LITERAL :
                            preFunc = new EventFunctor(dispatcherClassName,
                                                        "firePreEnumLiteralChangeRequest");
                            postFunc = new EventFunctor(dispatcherClassName,
                                                        "fireEnumLiteralChangeRequest");
                            dispatchRequest("PreEnumLiteralChangeRequest",
                                            "EnumLiteralChangeRequest",
                                            preFunc,
                                            postFunc,
                                            req);
                        break;
                        
                        case RTElementKind.RCT_OPERATION:
                        case RTElementKind.RCT_PARAMETER:
                            preFunc = new EventFunctor(dispatcherClassName,
                                                        "firePreOperationChangeRequest");
                            postFunc = new EventFunctor(dispatcherClassName,
                                                        "fireOperationChangeRequest");
                            dispatchRequest("PreOperationChangeRequest",
                                            "OperationChangeRequest",
                                            preFunc,
                                            postFunc,
                                            req);

                        break;
    
                        case RTElementKind.RCT_PACKAGE:
                            preFunc = new EventFunctor(dispatcherClassName,
                                                        "firePrePackageChangeRequest");
                            postFunc = new EventFunctor(dispatcherClassName,
                                                        "firePackageChangeRequest");
                            dispatchRequest("PrePackageChangeRequest",
                                            "PackageChangeRequest",
                                            preFunc,
                                            postFunc,
                                            req);

                        break;
          
                        case RTElementKind.RCT_RELATION:
                            preFunc = new EventFunctor(dispatcherClassName,
                                                        "firePreRelationChangeRequest");
                            postFunc = new EventFunctor(dispatcherClassName,
                                                        "fireRelationChangeRequest");
                            dispatchRequest("PreRelationChangeRequest",
                                            "RelationChangeRequest",
                                            preFunc,
                                            postFunc,
                                            req);

                        break;
    
                        default :
                        break;
                    }
                }
                else
                {
                    ITransformChangeRequest pTransform = null;
                    IAssociationEndTransformChangeRequest pEndTransform = null; 
                    try
                    {
                        pTransform = (ITransformChangeRequest)req;
                        pEndTransform = (IAssociationEndTransformChangeRequest)req;
                    }
                    catch(Exception e){}
    
                    int /*RTElementKind*/ type2 = (pTransform != null)?pTransform.getOldElementType():0;
        
                    if (pTransform != null && pEndTransform == null)
                    {
                        if (type2 == RTElementKind.RCT_CLASS ||
                            type2 == RTElementKind.RCT_INTERFACE ||
                            type2 == RTElementKind.RCT_ENUMERATION ||
                            type  == RTElementKind.RCT_CLASS ||
                            type  == RTElementKind.RCT_INTERFACE ||
                            type  == RTElementKind.RCT_ENUMERATION)
                        {
                            preFunc = new EventFunctor(dispatcherClassName,
                                                        "firePreClassChangeRequest");
                            postFunc = new EventFunctor(dispatcherClassName,
                                                        "fireClassChangeRequest");
                            dispatchRequest("PreClassChangeRequest",
                                            "ClassChangeRequest",
                                            preFunc,
                                            postFunc,
                                            req);
                        } else if (type2 == RTElementKind.RCT_ENUMERATION || type  == RTElementKind.RCT_ENUMERATION) {
                            preFunc = new EventFunctor(dispatcherClassName,
                                                        "firePreEnumerationChangeRequest");
                            postFunc = new EventFunctor(dispatcherClassName,
                                                        "fireEnumerationChangeRequest");
                            dispatchRequest("PreEnumerationChangeRequest",
                                            "EnumerationChangeRequest",
                                            preFunc,
                                            postFunc,
                                            req);
                        }
                    }
                    else if (pEndTransform != null)
                    {
                        if (type2 == RTElementKind.RCT_ATTRIBUTE ||
                            type2 == RTElementKind.RCT_NAVIGABLE_END_ATTRIBUTE ||
                            type  == RTElementKind.RCT_ATTRIBUTE ||
                            type  == RTElementKind.RCT_NAVIGABLE_END_ATTRIBUTE)
                        {
                            preFunc = new EventFunctor(dispatcherClassName,
                                                        "firePreAttributeChangeRequest");
                            postFunc = new EventFunctor(dispatcherClassName,
                                                        "fireAttributeChangeRequest");
                            dispatchRequest("PreAttributeChangeRequest",
                                            "AttributeChangeRequest",
                                            preFunc,
                                            postFunc,
                                            req);
    
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     *
     * 
     *
     * @param pProxy[in]
     *
     * @return 
     *
     */

    protected boolean isRelationInDispatchState(IRelationProxy pProxy)
    {
        // If any part of the relation is roundtripable, the relation is roundtripable.
        boolean retval = false;

        if (pProxy != null)
        {
            IElement pConnection = pProxy.getConnection();
            IElement pFrom = pProxy.getFrom();
            IElement pTo = pProxy.getTo();

            if (isAppInDispatchState(pConnection) ||
                isAppInDispatchState(pFrom) ||
                isAppInDispatchState(pTo))
            {
                retval = true;
            }
        }
        return retval;
    }
    
    protected IProject getProject(IElement pElement)
    {
        return pElement != null? new RTStateTester().getProject(pElement) : null;
    }
    
    /**
     *
     * Adds the change requests and processor to the PostElements queue that will be processed once a
     * post event on the given element occurs.
     *
     * @param xmiID[in] XMI ID of the element that was modified
     * @param proc[in] The RequestProcessor that will process these requests during the post event
     * @param reqs[in] The ChangeRequest to put on the queue
     *
     * @return HRESULT
     *
     */
    protected void addToPostElements(IRequestProcessor proc, 
                                     ETList<IChangeRequest> reqs)
    {
        m_PostElements.add( 
                new ETPairT<IRequestProcessor, ETList<IChangeRequest>>(
                        proc, reqs ));
    }

    /**
     *
     * 
     *
     * @param pElement[in]
     *
     * @return 
     *
     */

    protected boolean isAppInDispatchState(IElement pElement)
    {
        boolean retval = false;

        if (pElement != null)
        {
            IProject pProject = getProject(pElement);

            if (pProject != null)
            {
                retval = isProjectInDispatchState(pProject);
            }
        }
        return retval;
    }

    /**
     *
     * 
     *
     * @param pProject[in]
     *
     * @return 
     *
     */

    protected boolean isProjectInDispatchState(IProject pProject)
    {
        boolean retval = false;
        
        if (pProject != null)
        {
            String mode = pProject.getMode();

            if ("Implementation".equals(mode) || "2".equals(mode) || "PSK_IMPLEMENTATION".equals(mode))
            {
                retval = true;
            }
        }
        return retval;
    }
    
//  ----------------------------------------------------------------------------
//  ----------------------------------------------------------------------------
     /**
      *
      * 
      *
      * @param pRequest[in]
      *
      * @return 
      *
      */
    
    protected boolean isRequestDispatchable(IChangeRequest pRequest)
    {
        boolean retval = false;
    
        if (pRequest != null)
        {
            // Because of the delete/create factor, best just to check both. If one is true,
            // we dispatch.
    
            IElement pElement = null;
            IElement pBefore = null;
            IElement pAfter = pRequest.getAfter();

            IAssociationEnd pEnd = null;
            
            try
            {
                pEnd = (IAssociationEnd)pAfter;
            }
            catch(Exception e){}
            
            if (pEnd != null)
            {
                IClassifier pClass = pEnd.getParticipant();
                retval = isAppInDispatchState(pClass);
            }
            else
            {
                retval = isAppInDispatchState(pAfter);
                if (!retval)
                {
                    pBefore = pRequest.getBefore();
    
                    try
                    {
                        pEnd = (IAssociationEnd)pBefore;
                    }
                    catch(Exception e){}
            
                    if (pEnd != null)
                    {
                        IClassifier pClass = pEnd.getParticipant();
                        retval = isAppInDispatchState(pClass);
                    }
                    else
                    {
                        retval = isAppInDispatchState(pBefore);
                    }
                }
            }
    
            // More special cases for association
            if ( !retval )
            {
                IAssociation pAfterAssoc = null;
                IAssociation pBeforeAssoc = null;
                try
                {
                    pAfterAssoc = (IAssociation)pAfter;
                    pBeforeAssoc = (IAssociation)pBefore;
                }
                catch(Exception e){}
                
                if (pAfterAssoc != null)
                {
                    ETList<IAssociationEnd> endlist = pAfterAssoc.getEnds();
                    if ( endlist != null)
                    {
                        for (Iterator<IAssociationEnd> it = endlist.iterator(); it.hasNext() && retval == false;)
                        {
                            IAssociationEnd pItem = it.next();
                            if (pItem != null)
                            {
                                IClassifier pPart = pItem.getParticipant();
                                retval = isAppInDispatchState( pPart );
                            }
                        }
                    }
    
                    if (!retval)
                    {
                        ETList<IAssociationEnd> endlist1 = pBeforeAssoc.getEnds();
                        if ( endlist != null )
                        {
                            for (Iterator<IAssociationEnd> it = endlist.iterator(); it.hasNext() && retval == false;)
                            {
                                IAssociationEnd pItem = it.next();
                                if (pItem != null)
                                {
                                    IClassifier pPart = pItem.getParticipant();
                                    retval = isAppInDispatchState( pPart );
                                }
                            }
                        }
                    }
                }
            }
    
        // if we are still not dispatchable, make sure we are not some other
        // kind of request
    
        if (!retval)
        {
            IRelationProxy pProxy = pRequest.getRelation();
            retval = isRelationInDispatchState ( pProxy );
        }
    
        if ( !retval )
        {
            IDependencyChangeRequest pDepReq = null;
            try
            {
                pDepReq = (IDependencyChangeRequest)pRequest;
            }
            catch(Exception e){}

            if (pDepReq != null)
            {            
                pElement = pDepReq.getIndependentElement();
                retval = isAppInDispatchState(pElement);
            }
            else
            {
                IGeneralizationChangeRequest pGenReq = null;
                try
                {
                    pGenReq = (IGeneralizationChangeRequest)pRequest;
                }
                catch(Exception e){}
        
                if (pGenReq != null)
                {
                    IClassifier pClass = pGenReq.getAfterGeneralizing();
                    retval = isAppInDispatchState(pElement);
    
                    if (!retval)
                    {
                        pClass = pGenReq.getAfterSpecializing();
                        retval = isAppInDispatchState(pElement);
                    }
    
                    if (!retval)
                    {
                        pClass = pGenReq.getBeforeGeneralizing();
                        retval = isAppInDispatchState(pElement);
                    }
    
                    if (!retval)
                    {
                        pClass = pGenReq.getBeforeSpecializing();
                        retval = isAppInDispatchState(pElement);
                       }
                    }
                    else
                    {
                        IImplementationChangeRequest pImpReq = null;
                        try
                        {
                            pImpReq = (IImplementationChangeRequest)pRequest;
                        }
                        catch(Exception e){}
    
                        if (pImpReq != null)
                        {
                            IClassifier pClass = pImpReq.getAfterImplementing();
                            retval = isAppInDispatchState(pElement);
    
                            if (!retval)
                            {
                                pClass = pImpReq.getAfterInterface();
                                retval = isAppInDispatchState(pElement);
                            }
    
                            if (!retval)
                            {
                                pClass = pImpReq.getBeforeImplementing();
                                retval = isAppInDispatchState(pElement);
                            }
    
                            if (!retval)
                            {
                                pClass = pImpReq.getBeforeInterface();
                                retval = isAppInDispatchState(pElement);
                            }
                        }
                        else
                        {
                            IElementDuplicatedChangeRequest pDupeReq = null;
                            try
                            {
                                pDupeReq = (IElementDuplicatedChangeRequest)pRequest;
                            }
                            catch(Exception e){}
    
                            if (pDupeReq != null)
                            {
                                pElement = pDupeReq.getDuplicatedElement();
                                retval = isAppInDispatchState(pElement);
    
                                if (!retval)
                                {
                                    pElement = pDupeReq.getOriginalElement();
                                    retval = isAppInDispatchState(pElement);
                                }
                            }
                        }
                    }
                }
            }

            // Finally, if we ARE dispatchable at this point...
        
            if (retval)
            {
                 // If this is a multiple parameter type change request, look for
                 // and identical one on out BatchRequests list. If we find it, dont 
                 // send it. If we dont find it, send it and add it to this list.
    
                if (isSimilarOnBatchRequestList(pRequest))
                {
                    // DON'T SEND THIS REQUEST. WE HAVE ALREADY.
                    retval = false;
                }
                else
                {
                    // add it to the batch request list so that it does not get
                    // added again.
    
                    addBatchRequest(pRequest);
                }
            }
        }
        return retval;
    }
    
    protected ETList<ILanguage> getLanguages(IElement el)
    {
        return el.getLanguages();
    }
    
    /**
     * This is a convenience function to take some of the drudgery away from the
     * OnRT code above
     *
     * @param cell[in] The result cell
     * @param proceedOrig[in] The original value of the continue flag on the result cell
     * @param proceedFinal[in] The value of the continue flag passed back by the event processor
     *
     */
    protected void completeResultCell(IResultCell cell, 
                                        boolean proceedOrig,
                                        boolean proceedFinal )
    {
        if ( cell != null )
        {
            cell.setContinue(proceedOrig && proceedFinal);
        }
    }

    /**
     *
     * Determines whether or not there are any change requests on the passed in payload. If
     * there are, they are added to our internal list of requests that need to be processed.
     * If not, a request is created and put on the pre-change request list.
     *
     * @param element[in] The element about to be modified
     * @param payload[in] The payload that was just dispatched to a RequestProcessor
     * @param eventName[in] Name of the event. This is used to create the appropriate 
     *                      PreRequest object.
     * @param pModifiedNamespace[in] If set, this is the actual namespace being modified.
     *
     * @return HRESULT
     *
     */

    public void recordRequests (IElement element, 
                                IRoundTripEventPayload payload,
                                /*RequestDetailKind*/int eventName,
                                INamespace pModifiedNamespace )
    {
        recordRequests ( element, element, payload, eventName, pModifiedNamespace );
    }

//     ----------------------------------------------------------------------------
//     ----------------------------------------------------------------------------
    /**
     *
     * Determines whether or not there are any change requests on the passed in payload. If
     * there are, they are added to our internal list of requests that need to be processed.
     * If not, a request is created and put on the pre-change request list.
     *
     * @param elementBeingModified[in] The element being modified
     * @param element[in] The element the event is generated for, which in the case of add,
     *                    might not be the same thing as elementBeingModified
     * @param payload[in] The payload that was just dispatched to a RequestProcessor
     * @param eventName[in] Name of the event. This is used to create the appropriate 
     *                      PreRequest object.
     * @param pModifiedNamespace[in] If set, this is the actual namespace being modified.
     *
     * @return HRESULT
     *
     */

    public void recordRequests(IElement elementBeingModified, 
                                IElement element, 
                                IRoundTripEventPayload payload, 
                                /*RequestDetailKind*/int eventName,
                                INamespace pModifiedNamespace)
    {
        if (elementBeingModified == null || element == null || payload == null) return;
        establishPreRequest( elementBeingModified, element, eventName, payload, null, pModifiedNamespace);
    }

//     ----------------------------------------------------------------------------
//     ----------------------------------------------------------------------------
    /**
     *
     * 
     *
     * @param proxy[in]
     * @param payload[in]
     * @param eventName[in]
     * @param pModifiedNamespace[in] If set, this is the actual namespace being modified.
     *
     * @return 
     *
     */

    public void recordRequests(IRelationProxy proxy,
                                IRoundTripEventPayload payload,
                                /*RequestDetailKind*/int eventName,
                                INamespace pModifiedNamespace)
    {
        if (proxy == null || payload == null) return;
        
        int evtName = morphRequest(proxy, eventName);
        establishPreRequest( proxy, evtName, payload, pModifiedNamespace);
    }
    
    /**
     *
     * This routine looks to see if the relation proxy is specifically for 
     * an association and modifies the detail accordingly.
     *
     * @param proxy[in]
     * @param eventName[in out]
     *
     * @return 
     *
     */

    public int morphRequest(IRelationProxy proxy, int eventName)
    {
        if (proxy == null) return -1;
        int retVal = eventName;
        IElement pConnection = proxy.getConnection();

        if (pConnection != null)
        {
            IAssociationEnd pEnd = null;
            try
            {
                pEnd = (IAssociationEnd)pConnection;
            }
            catch(Exception e){}
            
            if (pEnd != null)
            {
                // This is an assocation end, not a "link". 
                if ( eventName == RequestDetailKind.RDT_RELATION_END_MODIFIED )
                {
                    retVal = RequestDetailKind.RDT_ASSOCIATION_END_MODIFIED;
                }
                else if ( eventName == RequestDetailKind.RDT_RELATION_END_REMOVED )
                {
                    retVal = RequestDetailKind.RDT_ASSOCIATION_END_REMOVED;
                }
                else if ( eventName == RequestDetailKind.RDT_RELATION_END_ADDED )
                {
                    retVal = RequestDetailKind.RDT_ASSOCIATION_END_ADDED;
                }
            }
        }
        return retVal; 
    }

    protected void propagateNamespaceChange(INamedElement pElement,
                                            /*RequestDetailKind*/ int  eventName,
                                            boolean isPreEvent,
                                            IResultCell cell,
                                            INamespace pModifiedNamespace )
    {
        if (pElement == null) return;

        int type = eventName;
        if (eventName == RequestDetailKind.RDT_NAME_MODIFIED)
        {
            // only one name was really modified. The rest are just
            // changes to a namespace identity.

            type = RequestDetailKind.RDT_NAMESPACE_MODIFIED;
        }
        else if (eventName == RequestDetailKind.RDT_CHANGED_NAMESPACE)
        {
            // only one element was really moved. The rest are just
            // changes to a namespace identity.

            type = RequestDetailKind.RDT_NAMESPACE_MOVED;
        }

        IPackage pPackage  = null;

        try
        {
            pPackage = (IPackage)pElement;
        }
        catch (Exception e)
        {
        }

        if (pPackage != null)
        {
            // Get all subelements in the package and generate events for these
            // as well.

            Node pNode = pPackage.getNode();
            if (pNode != null)
            {
                String query = "./UML:Element.ownedElement/*[name()=\"UML:Class\" or name()=\"UML:Package\" or name()=\"UML:Interface\"]";
                ETList<INamespace> namespaceList = new ElementCollector< INamespace >().retrieveElementCollection(pNode, query, INamespace.class);

                if(namespaceList != null)
                {
                    int count = namespaceList.size();
                    int i = 0;
                    INamespace pItem = null;
                    
                    while (i < count) 
                    {
                        pItem = namespaceList.get(i);
                        i++;
                        if (pItem != null)
                        {
                            if (isPreEvent)
                            {
                                onRTPreNamespaceChange(pItem, type, cell, pModifiedNamespace);
                            }
                            else
                            {
                                onRTNamespaceChange (pItem, type, cell, pModifiedNamespace);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * This is a pseudo-event, used to propagate namespaces changes.
     *
     * @param space[in]
     * @param element[in]
     * @param eventName[in]
     * @param cell[in]
     * @param pModifiedNamespace[in]
     *
     * @return 
     *
     */

    protected void onRTPreNamespaceChange(INamedElement element,
                                        /*RequestDetailKind*/ int eventName,
                                        IResultCell cell,
                                        INamespace pModifiedNamespace)
    {
        if (element == null) return;
        
        RTDispatchHelper helper = new RTDispatchHelper(m_ProcManager, 
                                                        m_DispController,
                                                        EventDispatchNameKeeper.EDT_ELEMENTMODIFIED_KIND );
        helper.establish( ESTABLISH_NORMAL, ESTABLISH_NOOVERRIDE, element, cell );

        // SINCE THIS IS NOT A REAL EVENT, WE DON'T FIRE ANYTHING

        // Fire the event to any listeners ( request processors most likely ), that need
        // to add more changerequests before continuing..

        //_VH( helper->FirePreElementAddedToNamespace( space, element, helper.Payload(), &proceed ));
 
        if( helper.getProceed())
        {
 
            recordRequests(element, helper.getPayload(), eventName, pModifiedNamespace);
            propagateNamespaceChange(element, eventName, true, cell, pModifiedNamespace);
        }

        completeResultCell(cell, helper.getCellOrigValue(), helper.getProceed());

    }

//     ----------------------------------------------------------------------------
//     ----------------------------------------------------------------------------
    /**
     *
     * 
     *
     * @param space[in]
     * @param element[in]
     * @param eventName[in]
     * @param cell[in]
     * @param pModifiedNamespace[in]
     *
     * @return 
     *
     */

    protected void onRTNamespaceChange(INamedElement element,
                                        /*RequestDetailKind*/ int eventName,
                                        IResultCell cell,
                                        INamespace pModifiedNamespace)

    {
        if (element == null) return;
       
        processRequests(element, ChangeKind.CT_MODIFY, null);
        propagateNamespaceChange(element, eventName, false, cell, pModifiedNamespace);

    }
    
    /**
     *
     * The difference between a RelationMod prerequest and a normal prerequest is
     * what is cloned.
     *
     * @param proxy[in]
     * @param eventName[in]
     * @param payload[in]
     *
     * @return 
     *
     */

    protected void establishRelModPreRequest(IRelationProxy proxy, 
                                                /*RequestDetailKind*/int eventName,
                                                IEventPayload payload)

    {
        if(proxy == null) return;
        
        IElement pConnection = proxy.getConnection();
        
        if (pConnection != null || eventName == RequestDetailKind.RDT_RELATION_CREATED)
        {
            IClassifier pClassFrom = null;
            IClassifier pClassTo = null;

            if (pConnection != null)
            {
                IGeneralization pGen = null;
                
                try
                {
                    pGen = (IGeneralization)pConnection;
                }
                catch (Exception e)
                {
                }

                if ( pGen != null)
                {
                    pClassFrom = pGen.getSpecific();
                    pClassTo = pGen.getGeneral();
                }
                else 
                {
                    IImplementation pImp = null;
                    try
                    {
                        pImp = (IImplementation)pConnection;
                    }
                    catch (Exception e)
                    {
                    }

                    if (pImp != null)
                    {
                        pClassFrom = pImp.getImplementingClassifier();
                        IClassifier pContract = pImp.getContract();

                        if (pContract != null)
                        {
                            pClassTo = pContract;
                        }
                    }
                }
            }
            else
            {
                String relationType = null;
                
                IElement pElement1 = proxy.getFrom();
                IElement pElement2 = proxy.getTo();
                
                try
                {
                    pClassFrom = (IClassifier)pElement1;
                    pClassTo = (IClassifier)pElement2;
                }
                catch (Exception e)
                {
                }
            }

            // We generate a prerequest for each end of the connection, since we don't really 
            // know, or care, which end is being modified. We don't know which end is meaningful
            // to the request processors. It is THEIR job to filter the requests that are 
            // meaningful.

            if (pClassFrom != null)
            {
                if (pConnection != null)
                {
                    // This will make us get a relation change request
                    establishPreRequest(pClassFrom, pConnection, eventName, payload, proxy, null);
                }
                else
                {
                    // This will make us get a class change request. We want to use relation change
                    // but if the proxy doesn't have a connection, what else can we do?
                    establishPreRequest(pClassFrom, pClassFrom, eventName, payload, proxy, null);
                }
            }

            if (pClassTo != null)
            {
                if (pConnection != null)
                {
                    // This will make us get a relation change request
                    establishPreRequest(pClassTo, pConnection, eventName, payload, proxy, null);
                }
                else
                {
                    // This will make us get a class change request. We want to use relation change
                    // but if the proxy doesn't have a connection, what else can we do?
                    establishPreRequest(pClassTo, pClassTo, eventName, payload, proxy, null);
                }
            }

            IElement pFrom = proxy.getFrom();
            IElement pTo = proxy.getTo();
            boolean isSame = false;
            if (pFrom != null && !pFrom.isSame(pClassFrom))
                establishPreRequest(pFrom, pConnection, eventName, payload, proxy, null);

            if (pTo != null && !pTo.isSame(pClassTo))
                establishPreRequest(pTo, pConnection, eventName, payload, proxy, null);
        }
    }

    protected void recordRelationModifyRequests(IRelationProxy proxy,
                                                IRoundTripEventPayload payload,
                                                /*RequestDetailKind*/int eventName)
    {
        if(proxy == null || payload == null) return;
        
        establishRelModPreRequest( proxy, morphRequest(proxy, eventName), payload);
    }

    /**
     *
     * The IResultCell comes in on the initial event. The Data property of that cell
     * may or may not have valid data on it. Regardless, a new IRoundTripEventPayload object
     * is created, pulling the data property from the IResultCell and placing it into
     * the IRoundTripEventPayload, thereby not losing it. 
     *
     * @param cell[in] The IResultCell to retrieve the data from.
     * @param rtCell[out] The new RoundTripDataCell.
     *
     * @return HRESULT
     *
     */

    protected IRoundTripEventPayload createRTContextPayload(IResultCell cell, 
                                                            IProject pProject,
                                                            IElement pOwner)
    {
        if (cell == null || cell == null) return null;

        IElementContextPayload newCell = new ElementContextPayload();

        Object data = cell.getContextData();

        newCell.setData(data);
        newCell.setProject(pProject);
        newCell.setOwner(pOwner);


        return newCell;
    }

//     ----------------------------------------------------------------------------
//     ----------------------------------------------------------------------------
    /**
     *
     * The IResultCell comes in on the initial event. The Data property of that cell
     * may or may not have valid data on it. Regardless, a new IRoundTripEventPayload object
     * is created, pulling the data property from the IResultCell and placing it into
     * the IRoundTripEventPayload, thereby not losing it. 
     *
     * @param cell[in] The IResultCell to retrieve the data from.
     * @param rtCell[out] The new RoundTripDataCell.
     *
     * @return HRESULT
     *
     */

    IRoundTripEventPayload createRTContextPayload(IResultCell cell, 
                                                    IElement pOwner)
    {
        if (cell == null) return null;

        IProject pProject = null;

        if (pOwner != null)
        {
            pProject = pOwner.getProject();
        }

        return createRTContextPayload(cell, pProject, pOwner);

    }
    
    /**
     *
     * The IResultCell comes in on the initial event. The Data property of that cell
     * may or may not have valid data on it. Regardless, a new IRoundTripEventPayload object
     * is created, pulling the data property from the IResultCell and placing it into
     * the IRoundTripEventPayload, thereby not losing it. 
     *
     * @param cell[in] The IResultCell to retrieve the data from.
     * @param rtCell[out] The new RoundTripDataCell.
     *
     * @return HRESULT
     *
     */

    IRoundTripEventPayload createRTPayload(IResultCell cell)
    {
        if (cell == null) return null;

        IRoundTripEventPayload newCell = new RoundTripEventPayload();
        newCell.setData(cell.getContextData());
        return newCell;
    }

    protected void recordImpactedRequests(IElement element, 
                                            IClassifier pModifiedClass,
                                            IRoundTripEventPayload payload,
                                            /*RequestDetailKind*/int eventName )
    {
        if (element == null || pModifiedClass == null) return;
        ETList< ILanguage > langs = element.getLanguages();

        if ( langs != null )
        {
            ILanguage pItem = null;
            IRequestProcessor proc = null;
            for( int idx = 0, count = langs.size() ; idx < count ; ++idx)
            {
                
                if ( pItem != null);
                {
                    pItem = langs.get(idx);
                    proc = m_ProcManager.establishProcessor(pItem);

                    if (proc != null)
                    {
                        // Nothing to clone

                        IPreRequest preReq  = PreRequestFactory.createImpactedPreRequest (eventName,
                                                                           element, 
                                                                           pModifiedClass,
                                                                           proc, 
                                                                           payload );

                        if (preReq != null)
                        {
                            m_PreElements.add ( preReq );
                        }
                    }    
                }
            }
        } 
    }
    
    /**
     *
     * Retrieves the ElementChangeDispatcher of the internal dispatch controller
     *
     * @param disp[out] The dispatcher
     *
     * @return HRESULT
     *
     */
    
    protected IElementChangeEventDispatcher getElementChangeDispatcher()
    {
        return new EventDispatchRetriever(m_DispController).getDispatcher(EventDispatchNameKeeper.modifiedName());
    }
    
    /**
     * This is a convenience function to take some of the drudgery away from the
     * OnRT code above. NOTE THAT THIS FUNCTION IS CALLED IN THE PREFIRE MACRO!!!
     * 
     * @param dispatcher[out] The classifier event dispatcher
     * @param payload[out] The roundtrip payload
     * @param cell[in] The result cell
     * @param proceedOrig[out] The original value of the continue flag on the result cell
     * @param proceedFinal[out] The value of the continue flag that can be passed to the fire function.
     */
    
    protected ETPairT<IClassifierEventDispatcher,IRoundTripEventPayload> getClassifierChangeDispatcherAndPayload ( IResultCell  cell, 
                                                                                     IElement  elementOfDocument)
    {
        if (cell != null && m_DispController != null)
        {
            IClassifierEventDispatcher dispatcher = null;
            IRoundTripEventPayload payload = null;
            if( m_DispController != null)
            {
                dispatcher = getClassifierChangeDispatcher();
    
                payload = createRTContextPayload(cell, elementOfDocument);
    
            }
             
            ETPairT<IClassifierEventDispatcher,IRoundTripEventPayload> pair 
                = new ETPairT<IClassifierEventDispatcher,IRoundTripEventPayload>(dispatcher, payload);
                
            return pair;                
        }
        return null;
    }

    class CompoundRequestListItem
    {
        private int m_RefCount;
        private ICompoundChangeRequest m_Request = null;
        private IRequestProcessor m_Processor = null;
        
        public CompoundRequestListItem (IElement pElement, 
                                        IElement pClone,
                                        ICompoundChangeRequest request,
                                        IRequestProcessor pProc,
                                        int detail,
                                        IEventPayload pPayload)
        {
            m_Request = request;
            if (m_Request != null)
            {
                m_Request.setAfter(pElement);
                m_Request.setBefore(pClone);
                m_Request.setState(ChangeKind.CT_MODIFY);
                m_Request.setRequestDetailType(detail);

                m_Processor = pProc;

                if (pProc != null)
                {
                    m_Request.setLanguage(pProc.getLanguage());
                }

                if (pPayload != null)
                {
                    m_Request.setPayload(pPayload);
                }
            }                
        }       

        public int getRefCount()
        {
            return m_RefCount;
        }
       
        public int increment()
        {
            m_RefCount++;
            return m_RefCount;
        }
        public int decrement()
        {
            if (m_RefCount > 0)
                m_RefCount--;
            return m_RefCount;
        }

        public boolean isSame (IElement pElement)
        {
            if(pElement == null || m_Request == null) return false;
            return pElement.isSame(m_Request.getAfter());
        }
        
        public void add(IChangeRequest pRequest)
        {
            if (pRequest != null && m_Request != null)
            {
                m_Request.add(pRequest);
            }
        }
        
        public IChangeRequest request()
        {
            return m_Request;
        }
        
        public IElement element()
        {
            return (m_Request != null)?m_Request.getAfter():null;
        }
        
        public IRequestProcessor processor()
        {
            return m_Processor;
        } 
    }
}
