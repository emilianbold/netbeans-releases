/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.EventDispatchController;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementLifeTimeEventDispatcher;
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
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationValidatorEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ClassifierEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher;
import org.netbeans.modules.uml.ui.support.helpers.ETSmartWaitCursor;

/**
 * @author sumitabhk
 *
 */
public class RoundTripController implements IRoundTripController
{
	private int                                 m_Mode;
    private IRTEventManager                     m_Manager;
    private IClassifierEventDispatcher          m_ClassDispatch;
    private IElementChangeEventDispatcher       m_ElementDispatch;
    private IRelationValidatorEventDispatcher   m_RelationDispatch;
    private IEventDispatchController            m_DispController;
    private IWorkspaceEventDispatcher           m_WorkspaceDispatch;
    private IRoundTripEventDispatcher           m_RTDispatcher;
    private IElementLifeTimeEventDispatcher     m_LifeDispatch;
    private String                              RT_DISPATCH = "RoundTripDispatcher";
	/**
	 * 
	 */
	public RoundTripController() 
	{
        m_Mode = RTMode.RTM_OFF;
        m_Manager = null;
        
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripController#getMode()
	 */
	public int getMode() 
	{
		return m_Mode;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripController#setMode(int)
	 */
	public void setMode(int value) 
	{
        int origMode = m_Mode;
        
// IZ 84855 conover - RT is always off and shouldn't be 
// turned on for any reason
//        if (value != RTMode.RTM_LIVE)
            m_Mode = value;

        switch(m_Mode)
        {
            case RTMode.RTM_SHUT_DOWN:
            {
                    revokeDispatchers();
            }
            break;
            case RTMode.RTM_OFF:
            {
                if( origMode == RTMode.RTM_LIVE ||
                     origMode == RTMode.RTM_BATCH )
                 {
                    revokeDispatchers();
                 }
		 else if ( origMode == RTMode.RTM_SHUT_DOWN )
                 {
                    registerDispatchers();
                 }
            }
            break;

// IZ 84855 conover - RT is always off and shouldn't be 
// turned on for any reason
            case RTMode.RTM_LIVE:
            {
                if( origMode == RTMode.RTM_OFF ||
                     origMode == RTMode.RTM_SHUT_DOWN )
                {
                    registerDispatchers();
                }
            }
            break;

            case RTMode.RTM_BATCH:
            {
                if( origMode == RTMode.RTM_OFF ||
                     origMode == RTMode.RTM_SHUT_DOWN )
                {
                    registerDispatchers();
                }
            }
            break;
        }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripController#getEventDispatchController()
	 */
	public IEventDispatchController getEventDispatchController() {
        
        if(m_DispController == null)
            m_DispController = new EventDispatchController();
        return m_DispController;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripController#setEventDispatchController(org.netbeans.modules.uml.core.eventframework.IEventDispatchController)
	 */
	public void setEventDispatchController(IEventDispatchController value) {
		m_DispController = value;		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripController#initialize(java.lang.Object, int)
	 */
	public void initialize(ICoreProduct prod, int mode) 
	{
        if(prod != null)
        {
            m_Manager = new RTEventManager(this);

            m_Mode = mode;
            
            establishDispatchers( prod );

            // Create the round trip dispatcher and put it on our
            // internal dispatch controller

            m_RTDispatcher = new RoundTripEventDispatcher();

            IEventDispatchController controller = getEventDispatchController();

            if(controller != null)
            {
                controller.addDispatcher(RT_DISPATCH, m_RTDispatcher);
            }
        }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripController#getRoundTripDispatcher()
	 */
	public IRoundTripEventDispatcher getRoundTripDispatcher() {
		return (IRoundTripEventDispatcher)getADispatcher(RT_DISPATCH);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripController#getClassifierDispatcher()
	 */
	public IClassifierEventDispatcher getClassifierDispatcher() {
        return (IClassifierEventDispatcher)getADispatcher(EventDispatchNameKeeper.classifier());
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripController#getElementLifeTimeDispatcher()
	 */
	public IElementLifeTimeEventDispatcher getElementLifeTimeDispatcher() {
        return (IElementLifeTimeEventDispatcher)getADispatcher(EventDispatchNameKeeper.lifeTime());
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripController#getElementChangeDispatcher()
	 */
	public IElementChangeEventDispatcher getElementChangeDispatcher() {
        return (IElementChangeEventDispatcher)getADispatcher(EventDispatchNameKeeper.modifiedName());
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripController#getRelationValidatorDispatcher()
	 */
	public IRelationValidatorEventDispatcher getRelationValidatorDispatcher() 
    {
        return (IRelationValidatorEventDispatcher)getADispatcher(EventDispatchNameKeeper.relation());
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripController#deInitialize()
	 */
	public void deInitialize() 
    {
        revokeDispatchers();
        
        if(m_DispController != null)
            m_DispController.removeDispatcher(RT_DISPATCH);
        
        m_DispController = null;
        m_RTDispatcher = null;
        m_Manager = null;
	}

    /**
     *
     * Creates the dispatchers that will be installed on this 
     * RoundTripController's own EventDispatchController. This
     * allows RequestProcessors to connect to register to these
     * dispatchers, receiving RoundTrip specific payloads.
     *
     * @return HRESULT
     *
     */

    public void createDispatchers()
    {
        IEventDispatchController cont = getEventDispatchController();
        if(cont != null)
        {
            IClassifierEventDispatcher classDispatch = new ClassifierEventDispatcher();
            cont.addDispatcher(EventDispatchNameKeeper.classifier(), classDispatch);

            IElementChangeEventDispatcher elementDispatch = new ElementChangeEventDispatcher();
            cont.addDispatcher(EventDispatchNameKeeper.modifiedName(), elementDispatch);

            IRelationValidatorEventDispatcher relDispatcher = new RelationValidatorEventDispatcher();
            cont.addDispatcher(EventDispatchNameKeeper.relation(), relDispatcher);

            IElementLifeTimeEventDispatcher lifeDispatcher = new ElementLifeTimeEventDispatcher();
            cont.addDispatcher(EventDispatchNameKeeper.lifeTime(), lifeDispatcher);
        }
    }

    /**
     *
     * Connects the sinks the RoundTripController implements with the various
     * dispatchers on the core product. This is where the connection between
     * the product and the RoundTrip mechanism is established.
     *
     * @param prod[in] The product to connect to.
     *
     * @return HRESULT
     *
     */

    public void registerWithProduct(ICoreProduct prod)
    {
        // ----------------------------------------------------------------------
        // Class
        // ----------------------------------------------------------------------

        m_ClassDispatch = (IClassifierEventDispatcher)EventDispatchRetriever.instance().getDispatcher(EventDispatchNameKeeper.classifier());
        if(m_ClassDispatch != null)
        {
            m_ClassDispatch.registerForClassifierFeatureEvents(this);
            m_ClassDispatch.registerForFeatureEvents(this);            
            m_ClassDispatch.registerForStructuralFeatureEvents(this);
            m_ClassDispatch.registerForBehavioralFeatureEvents(this);
            m_ClassDispatch.registerForParameterEvents(this);
            m_ClassDispatch.registerForTypedElementEvents(this);
            m_ClassDispatch.registerForAttributeEvents(this);
            m_ClassDispatch.registerForOperationEvents(this);
            m_ClassDispatch.registerForTransformEvents(this);
            m_ClassDispatch.registerForAssociationEndTransformEvents(this);
            m_ClassDispatch.registerForAffectedElementEvents(this);
            m_ClassDispatch.registerForEventFrameworkEvents(this);
        }

        // ----------------------------------------------------------------------
        // ElementChange
        // ----------------------------------------------------------------------

        m_ElementDispatch = (IElementChangeEventDispatcher)EventDispatchRetriever.instance().getDispatcher(EventDispatchNameKeeper.modifiedName());
        if(m_ElementDispatch != null)
        {            
            m_ElementDispatch.registerForDocumentationModifiedEvents(this);
            m_ElementDispatch.registerForNamespaceModifiedEvents(this);
            m_ElementDispatch.registerForNamedElementEvents(this);
            m_ElementDispatch.registerForRedefinableElementModifiedEvents(this);
            m_ElementDispatch.registerForEventFrameworkEvents(this);
            m_ElementDispatch.registerForPackageEventsSink(this);
        }

        // ----------------------------------------------------------------------
        // RelationValidate
        // ----------------------------------------------------------------------
       
        m_RelationDispatch = (IRelationValidatorEventDispatcher)EventDispatchRetriever.instance().getDispatcher(EventDispatchNameKeeper.relation());
        if(m_RelationDispatch != null)
        {
            m_RelationDispatch.registerForRelationValidatorEvents(this);
            m_RelationDispatch.registerForRelationEvents(this);
            m_RelationDispatch.registerForEventFrameworkEvents(this);
        }

        // ----------------------------------------------------------------------
        // ElementLifeTime
        // ----------------------------------------------------------------------
      
        m_LifeDispatch = (IElementLifeTimeEventDispatcher)EventDispatchRetriever.instance().getDispatcher(EventDispatchNameKeeper.lifeTime());
        if(m_LifeDispatch != null)
        {
            m_LifeDispatch.registerForLifeTimeEvents(this);
            m_LifeDispatch.registerForEventFrameworkEvents(this);
        }


        // ----------------------------------------------------------------------
        // Core
        // ----------------------------------------------------------------------
        // Get the core product's dispatch controll and also register with that one

        IEventDispatchController pCoreController = prod.getEventDispatchController();

        if (pCoreController != null)
        {
            pCoreController.registerForEventFrameworkEvents(this);
        }

        // ----------------------------------------------------------------------
        // Workspace
        // ----------------------------------------------------------------------
        // Register with the workspace dispatcher 
        
        m_WorkspaceDispatch = (IWorkspaceEventDispatcher)EventDispatchRetriever.instance().getDispatcher(EventDispatchNameKeeper.workspaceName());        
        if (m_WorkspaceDispatch != null)
        {
            m_WorkspaceDispatch.registerForWSProjectEvents(this);
        }
    }
    
    /**
    * Creates the round trip dispatcher and creates the dispatch
    * controller that this controller will use to dispatch
    * change requests to the various listeners.
    *
    * @param prod[in] The product to use for dispatch retrieval
    *
    * @return HRESULT
    *
    */

    protected void establishDispatchers(ICoreProduct prod)
    {
        // The RoundTripController needs to register sinks with the dispatchers
        // on the core product, then it will also create and install specific 
        // dispatchers that RequestProcessors can register with. This will 
        // allow the controller to manage events going to processors, gathering
        // any change requests created during pre processing.

        registerWithProduct(prod);
        createDispatchers();
    }
    
    /**
     *
     * Retrieves the current product, registering with all the 
     * appropriate dispatchers necessary for roundtrip
     *
     * @return HRESULT
     *
     */


    protected void registerDispatchers()
    {
        registerWithProduct(ProductRetriever.retrieveProduct());
    }
    
    /**
     *
     * Revokes the various sinks from the various dispatchers on the passed in
     * product
     *
     * @param prod[in] The product to disconnect from.
     *
     * @return HRESULT
     *
     */    
    protected void revokeDispatchers()
    {
        if(m_ClassDispatch  != null)
        {
            m_ClassDispatch.revokeClassifierFeatureSink(this);
            m_ClassDispatch.revokeFeatureSink(this);
            m_ClassDispatch.revokeStructuralFeatureSink(this);
            m_ClassDispatch.revokeBehavioralFeatureSink(this);
            m_ClassDispatch.revokeParameterSink(this);
            m_ClassDispatch.revokeTypedElementSink(this);
            m_ClassDispatch.revokeAttributeSink(this);
            m_ClassDispatch.revokeOperationSink(this);
            m_ClassDispatch.revokeTransformSink(this);
            m_ClassDispatch.revokeAssociationEndTransformSink(this);
            m_ClassDispatch.revokeAffectedElementEvents(this);
            m_ClassDispatch.revokeEventFrameworkSink(this);

            m_ClassDispatch = null;
        }

        if(m_ElementDispatch != null)
        {
            m_ElementDispatch.revokeDocumentationModifiedSink(this);
            m_ElementDispatch.revokeNamespaceModifiedSink(this);
            m_ElementDispatch.revokeNamedElementSink(this);
            m_ElementDispatch.revokeRedefinableElementModifiedEvents(this);
            m_ElementDispatch.revokeEventFrameworkSink(this);
            m_ElementDispatch.revokePackageEventsSink(this);

            m_ElementDispatch = null;
        }

        if(m_RelationDispatch != null)
        {
            m_RelationDispatch.revokeRelationValidatorSink(this);
            m_RelationDispatch.revokeRelationSink(this);
            m_RelationDispatch.revokeEventFrameworkSink(this);

            m_RelationDispatch = null;
        }

        if(m_LifeDispatch != null)
        {
            m_LifeDispatch.revokeLifeTimeSink(this);
            m_LifeDispatch.revokeEventFrameworkSink(this);

            m_LifeDispatch = null;
        }

        if(m_WorkspaceDispatch != null)
        {
            m_WorkspaceDispatch.revokeWSProjectSink(this);
            
            m_WorkspaceDispatch = null;
        }
    }
    
    /**
     *
     * Gets the specifier dispatcher type from the controller
     *
     * @param dispID[in] The dispatcher ID string
     * @param pDisp[out] The dispacther
     *
     * @return HRESULT
     *
     */

    protected IEventDispatcher getADispatcher(String dispID)
    {
        IEventDispatchController cont = getEventDispatchController();
        if(cont != null)
        {
            return cont.retrieveDispatcher(dispID);
        }
        return null;
    }

    //  -------------------------------------------------------------------------
    // IDocumentationModifiedEventsSink
    // -------------------------------------------------------------------------
 
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink#onDocumentationModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDocumentationModified(IElement element, IResultCell cell)
    {
        m_Manager.onRTDocumentationModified(element, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink#onDocumentationPreModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDocumentationPreModified(
        IElement element,
        String doc,
        IResultCell cell)
    {
        m_Manager.onRTDocumentationPreModified(element, doc, cell);
    }


    //  -------------------------------------------------------------------------
    // IElementLifeTimeEventsSink
    // -------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementCreated(IVersionableElement element, IResultCell cell)
    {
        // RoundTrip doesn't handle the element create method
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementDeleted(IVersionableElement element, IResultCell cell)
    {
        m_Manager.onRTElementDelete(element, cell);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementDuplicated(
        IVersionableElement element,
        IResultCell cell)
    {
        // C++ code does nothing

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreCreate(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementPreCreate(String ElementType, IResultCell cell)
    {
        // RoundTrip doesn't handle the element create method
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDelete(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementPreDelete(
        IVersionableElement element,
        IResultCell cell)
    {
        m_Manager.onRTElementPreDelete(element, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementPreDuplicated(
        IVersionableElement element,
        IResultCell cell)
    {
        // C++ code does nothing
    }
    
    //  -------------------------------------------------------------------------
    // INamedElementEventsSink
    // -------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onAliasNameModified(INamedElement element, IResultCell cell)
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement)
     */
    public void onNameCollision(INamedElement element, ETList<INamedElement> collidingElements, IResultCell cell)
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onNameModified(INamedElement element, IResultCell cell)
    {
        ETSmartWaitCursor cursor = new ETSmartWaitCursor();
        try 
        {
        m_Manager.onRTNameModified(element, cell);
    }
        finally 
        {
            cursor.stop();    
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreAliasNameModified(
        INamedElement element,
        String proposedName,
        IResultCell cell)
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String)
     */
    public void onPreNameCollision(INamedElement element, String proposedName, ETList<INamedElement> collidingElements, IResultCell cell)
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreNameModified(
        INamedElement element,
        String proposedName,
        IResultCell cell)
    {
        m_Manager.onRTPreNameModified(element, proposedName, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreVisibilityModified(
        INamedElement element,
        int proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreVisibilityModified(element, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onVisibilityModified(INamedElement element, IResultCell cell)
    {
        m_Manager.onRTVisibilityModified(element, cell);
    }
    
    // -------------------------------------------------------------------------
    // INamespaceModifiedEventsSink
    // -------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink#onElementAddedToNamespace(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementAddedToNamespace(
        INamespace space,
        INamedElement elementAdded,
        IResultCell cell)
    {
        m_Manager.onRTElementAddedToNamespace(space, elementAdded, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink#onPreElementAddedToNamespace(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreElementAddedToNamespace(
        INamespace space,
        INamedElement elementToAdd,
        IResultCell cell)
    {
        m_Manager.onRTPreElementAddedToNamespace(space, elementToAdd, cell);
    }

    // -------------------------------------------------------------------------
    // IRelationValidatorEventsSink
    // -------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationValidatorEventsSink#onPreRelationValidate(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreRelationValidate(IRelationProxy proxy, IResultCell cell)
    {
        m_Manager.onRTPreRelationValidate(proxy, cell);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationValidatorEventsSink#onRelationValidated(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRelationValidated(IRelationProxy payload, IResultCell cell)
    {
        m_Manager.onRTRelationValidate(payload, cell);
    }

    // -------------------------------------------------------------------------
    // RelationEventsSink
    // -------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onPreRelationCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreRelationCreated(IRelationProxy proxy, IResultCell cell)
    {
        m_Manager.onRTPreRelationCreated(proxy, cell);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onPreRelationDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreRelationDeleted(IRelationProxy proxy, IResultCell cell)
    {
        m_Manager.onRTPreRelationDeleted(proxy, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onPreRelationEndAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreRelationEndAdded(IRelationProxy proxy, IResultCell cell)
    {
        m_Manager.onRTPreRelationEndAdded(proxy, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onPreRelationEndModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreRelationEndModified(
        IRelationProxy proxy,
        IResultCell cell)
    {
        m_Manager.onRTPreRelationEndModified(proxy, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onPreRelationEndRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreRelationEndRemoved(IRelationProxy proxy, IResultCell cell)
    {
        m_Manager.onRTPreRelationEndRemoved(proxy, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onRelationCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRelationCreated(IRelationProxy proxy, IResultCell cell)
    {
        m_Manager.onRTRelationCreated(proxy, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onRelationDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRelationDeleted(IRelationProxy proxy, IResultCell cell)
    {
        m_Manager.onRTRelationDeleted(proxy, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onRelationEndAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRelationEndAdded(IRelationProxy proxy, IResultCell cell)
    {
        m_Manager.onRTRelationEndAdded(proxy, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onRelationEndModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRelationEndModified(IRelationProxy payload, IResultCell cell)
    {
        m_Manager.onRTRelationEndModified(payload, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink#onRelationEndRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRelationEndRemoved(IRelationProxy proxy, IResultCell cell)
    {
        m_Manager.onRTRelationEndRemoved(proxy, cell);
    }

    // -------------------------------------------------------------------------
    // IAttributeEventsSink
    // -------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultBodyModified(IAttribute feature, IResultCell cell)
    {
        m_Manager.onRTDefaultBodyModified(feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultLanguageModified(IAttribute feature, IResultCell cell)
    {
        m_Manager.onRTDefaultLanguageModified(feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultModified(IAttribute attr, IResultCell cell)
    {
        m_Manager.onRTDefaultModified(attr, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultPreModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultPreModified(
        IAttribute attr,
        IExpression proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTDefaultPreModified(attr, proposedValue, cell);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDerivedModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDerivedModified(IAttribute feature, IResultCell cell)
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPreDefaultBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDefaultBodyModified(
        IAttribute feature,
        String bodyValue,
        IResultCell cell)
    {
        m_Manager.onRTPreDefaultBodyModified(feature, bodyValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPreDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDefaultLanguageModified(
        IAttribute feature,
        String language,
        IResultCell cell)
    {
        m_Manager.onRTPreDefaultLanguageModified(feature, language, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPreDerivedModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDerivedModified(
        IAttribute feature,
        boolean proposedValue,
        IResultCell cell)
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPrePrimaryKeyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPrePrimaryKeyModified(
        IAttribute feature,
        boolean proposedValue,
        IResultCell cell)
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPrimaryKeyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPrimaryKeyModified(IAttribute feature, IResultCell cell)
    {
        // C++ code does nothing
    }
    
    // -------------------------------------------------------------------------
    // IBehavioralFeatureEventsSink
    // -------------------------------------------------------------------------
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onAbstractModified(
        IBehavioralFeature feature,
        IResultCell cell)
    {
        m_Manager.onRTAbstractModified(feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onConcurrencyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onConcurrencyModified(
        IBehavioralFeature feature,
        IResultCell cell)
    {
        m_Manager.onRTConcurrencyModified(feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onConcurrencyPreModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onConcurrencyPreModified(
        IBehavioralFeature feature,
        int proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTConcurrencyPreModified(feature, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onHandledSignalAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onHandledSignalAdded(
        IBehavioralFeature feature,
        IResultCell cell)
    {
        m_Manager.onRTHandledSignalAdded(feature, cell);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onHandledSignalRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onHandledSignalRemoved(
        IBehavioralFeature feature,
        IResultCell cell)
    {
        m_Manager.onRTHandledSignalRemoved(feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onParameterAdded(
        IBehavioralFeature feature,
        IParameter parm,
        IResultCell cell)
    {
        m_Manager.onRTParameterAdded(feature, parm, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onParameterRemoved(
        IBehavioralFeature feature,
        IParameter parm,
        IResultCell cell)
    {
        m_Manager.onRTParameterRemoved(feature, parm, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreAbstractModified(
        IBehavioralFeature feature,
        boolean proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreAbstractModified(feature, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreHandledSignalAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreHandledSignalAdded(
        IBehavioralFeature feature,
        ISignal proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreHandledSignalAdded(feature, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreHandledSignalRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreHandledSignalRemoved(
        IBehavioralFeature feature,
        ISignal proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreHandledSignalRemoved(feature, proposedValue, cell);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreParameterAdded(
        IBehavioralFeature feature,
        IParameter parm,
        IResultCell cell)
    {
        m_Manager.onRTPreParameterAdded(feature, parm, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreParameterRemoved(
        IBehavioralFeature feature,
        IParameter parm,
        IResultCell cell)
    {
        m_Manager.onRTPreParameterRemoved(feature, parm, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onPreStrictFPModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreStrictFPModified(
        IBehavioralFeature feature,
        boolean proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreStrictFPModified(feature, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeatureEventsSink#onStrictFPModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onStrictFPModified(
        IBehavioralFeature feature,
        IResultCell cell)
    {
        m_Manager.onRTStrictFPModified(feature, cell);
    }
    
    // -------------------------------------------------------------------------
    // IClassifierFeatureEventsSink
    // -------------------------------------------------------------------------    
    
    

    /* (non-Javadoc)
     * @see com.em_Manager.onRTe.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onAbstractModified(IClassifier feature, IResultCell cell)
    {
        m_Manager.onRTAbstractModified(feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeatureAdded(
        IClassifier classifier,
        IFeature feature,
        IResultCell cell)
    {
        m_Manager.onRTFeatureAdded(classifier, feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureDuplicatedToClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeatureDuplicatedToClassifier(
        IClassifier pOldClassifier,
        IFeature pOldFeature,
        IClassifier pNewClassifier,
        IFeature pNewFeature,
        IResultCell cell)
    {
        m_Manager.onRTFeatureDuplicatedToClassifier(pOldClassifier, pOldFeature,pNewClassifier,pNewFeature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureMoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeatureMoved(
        IClassifier classifier,
        IFeature feature,
        IResultCell cell)
    {
        m_Manager.onRTFeatureMoved(classifier, feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeaturePreAdded(
        IClassifier classifier,
        IFeature feature,
        IResultCell cell)
    {
        m_Manager.onRTFeaturePreAdded(classifier, feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreDuplicatedToClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeaturePreDuplicatedToClassifier(
        IClassifier classifier,
        IFeature feature,
        IResultCell cell)
    {
        m_Manager.onRTFeaturePreDuplicatedToClassifier(classifier, feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreMoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeaturePreMoved(
        IClassifier classifier,
        IFeature feature,
        IResultCell cell)
    {
        m_Manager.onRTFeaturePreMoved(classifier, feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeaturePreRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeaturePreRemoved(
        IClassifier classifier,
        IFeature feature,
        IResultCell cell)
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onFeatureRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFeatureRemoved(
        IClassifier classifier,
        IFeature feature,
        IResultCell cell)
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onLeafModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onLeafModified(IClassifier feature, IResultCell cell)
    {
        m_Manager.onRTLeafModified(feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreAbstractModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreAbstractModified(
        IClassifier feature,
        boolean proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreAbstractModified(feature, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreLeafModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreLeafModified(
        IClassifier feature,
        boolean proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreLeafModified(feature, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreTemplateParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreTemplateParameterAdded(
        IClassifier pClassifier,
        IParameterableElement pParam,
        IResultCell cell)
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreTemplateParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreTemplateParameterRemoved(
        IClassifier pClassifier,
        IParameterableElement pParam,
        IResultCell cell)
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onPreTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreTransientModified(
        IClassifier feature,
        boolean proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreTransientModified(feature, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onTemplateParameterAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onTemplateParameterAdded(
        IClassifier pClassifier,
        IParameterableElement pParam,
        IResultCell cell)
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onTemplateParameterRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onTemplateParameterRemoved(
        IClassifier pClassifier,
        IParameterableElement pParam,
        IResultCell cell)
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierFeatureEventsSink#onTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onTransientModified(IClassifier feature, IResultCell cell)
    {
        m_Manager.onRTTransientModified(feature, cell);
    }
    
    // -------------------------------------------------------------------------
    // IFeatureEventsSink
    // -------------------------------------------------------------------------    
    
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink#onNativeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onNativeModified(IFeature feature, IResultCell cell)
    {
        m_Manager.onRTNativeModified(feature, cell);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink#onPreNativeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreNativeModified(
        IFeature feature,
        boolean proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreNativeModified(feature, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink#onPreStaticModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreStaticModified(
        IFeature feature,
        boolean proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreStaticModified(feature, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeatureEventsSink#onStaticModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onStaticModified(IFeature feature, IResultCell cell)
    {
        m_Manager.onRTStaticModified(feature, cell);
    }
    
    // -------------------------------------------------------------------------
    // IOperationEventsSink
    // -------------------------------------------------------------------------
        

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onConditionAdded(
        IOperation oper,
        IConstraint condition,
        boolean isPreCondition,
        IResultCell cell)
    {
        m_Manager.onRTConditionAdded(oper, condition, isPreCondition, cell);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionPreAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onConditionPreAdded(
        IOperation oper,
        IConstraint condition,
        boolean isPreCondition,
        IResultCell cell)
    {
        m_Manager.onRTConditionPreAdded(oper, condition, isPreCondition, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionPreRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onConditionPreRemoved(
        IOperation oper,
        IConstraint condition,
        boolean isPreCondition,
        IResultCell cell)
    {
        m_Manager.onRTConditionPreRemoved(oper, condition, isPreCondition, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onConditionRemoved(
        IOperation oper,
        IConstraint condition,
        boolean isPreCondition,
        IResultCell cell)
    {
        m_Manager.onRTConditionRemoved(oper, condition, isPreCondition, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onOperationPropertyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onOperationPropertyModified(
        IOperation oper,
        int nKind,
        IResultCell cell)
    {
        m_Manager.onRTOperationPropertyModified(oper, nKind, cell);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onPreOperationPropertyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, int, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreOperationPropertyModified(
        IOperation oper,
        int nKind,
        boolean proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreOperationPropertyModified(oper, nKind, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onPreQueryModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreQueryModified(
        IOperation oper,
        boolean proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreQueryModified(oper, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onQueryModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onQueryModified(IOperation oper, IResultCell cell)
    {
        m_Manager.onRTQueryModified(oper, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRaisedExceptionAdded(
        IOperation oper,
        IClassifier pException,
        IResultCell cell)
    {
        m_Manager.onRTRaisedExceptionAdded(oper, pException, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionPreAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRaisedExceptionPreAdded(
        IOperation oper,
        IClassifier pException,
        IResultCell cell)
    {
        m_Manager.onRTRaisedExceptionPreAdded(oper, pException, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionPreRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRaisedExceptionPreRemoved(
        IOperation oper,
        IClassifier pException,
        IResultCell cell)
    {
        m_Manager.onRTRaisedExceptionPreRemoved(oper, pException, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRaisedExceptionRemoved(
        IOperation oper,
        IClassifier pException,
        IResultCell cell)
    {
        m_Manager.onRTRaisedExceptionRemoved(oper, pException, cell);
    }
    
    // -------------------------------------------------------------------------
    // IParameterEventsSink
    // -------------------------------------------------------------------------
    
        

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onDefaultExpBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultExpBodyModified(IParameter feature, IResultCell cell)
    {
        m_Manager.onRTDefaultExpBodyModified(feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onDefaultExpLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultExpLanguageModified(
        IParameter feature,
        IResultCell cell)
    {
        m_Manager.onRTDefaultExpLanguageModified(feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onDefaultExpModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDefaultExpModified(IParameter feature, IResultCell cell)
    {
        m_Manager.onRTDefaultExpModified(feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onDirectionModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onDirectionModified(IParameter feature, IResultCell cell)
    {
        m_Manager.onRTDirectionModified(feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onPreDefaultExpBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDefaultExpBodyModified(
        IParameter feature,
        String bodyValue,
        IResultCell cell)
    {
        m_Manager.onRTPreDefaultExpBodyModified(feature, bodyValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onPreDefaultExpLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDefaultExpLanguageModified(
        IParameter feature,
        String language,
        IResultCell cell)
    {
        m_Manager.onRTPreDefaultExpLanguageModified(feature, language, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onPreDefaultExpModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDefaultExpModified(
        IParameter feature,
        IExpression proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreDefaultExpModified(feature, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterEventsSink#onPreDirectionModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreDirectionModified(
        IParameter feature,
        int proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreDirectionModified(feature, proposedValue, cell);
    }
    
    // -------------------------------------------------------------------------
    // IStructuralFeatureEventsSink
    // -------------------------------------------------------------------------
    
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onChangeabilityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onChangeabilityModified(
        IStructuralFeature feature,
        IResultCell cell)
    {
        m_Manager.onRTChangeabilityModified(feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onPreChangeabilityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreChangeabilityModified(
        IStructuralFeature feature,
        int proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreChangeabilityModified(feature, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onPreTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreTransientModified(
        IStructuralFeature feature,
        boolean proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreTransientModified(feature, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onPreVolatileModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreVolatileModified(
        IStructuralFeature feature,
        boolean proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreVolatileModified(feature, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onTransientModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onTransientModified(
        IStructuralFeature feature,
        IResultCell cell)
    {
        m_Manager.onRTTransientModified(feature, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeatureEventsSink#onVolatileModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onVolatileModified(
        IStructuralFeature feature,
        IResultCell cell)
    {
        m_Manager.onRTVolatileModified(feature, cell);
    }
    
    // -------------------------------------------------------------------------
    // ITypedElementEventsSink
    // -------------------------------------------------------------------------

      

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onLowerModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onLowerModified(
        ITypedElement element,
        IMultiplicity mult,
        IMultiplicityRange range,
        IResultCell cell)
    {
        m_Manager.onRTLowerModified(element, mult, range, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onMultiplicityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onMultiplicityModified(ITypedElement element, IResultCell cell)
    {
        m_Manager.onRTMultiplicityModified(element, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onOrderModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onOrderModified(
        ITypedElement element,
        IMultiplicity mult,
        IResultCell cell)
    {
        m_Manager.onRTOrderModified(element, mult, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreLowerModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreLowerModified(
        ITypedElement element,
        IMultiplicity mult,
        IMultiplicityRange range,
        String proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreLowerModified(element, mult, range, proposedValue, cell);
    }

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
        m_Manager.onRTCollectionTypeModified(element, mult, range, cell);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreMultiplicityModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreMultiplicityModified(
        ITypedElement element,
        IMultiplicity proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreMultiplicityModified(element, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreOrderModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreOrderModified(
        ITypedElement element,
        IMultiplicity mult,
        boolean proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreOrderModified(element, mult, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreRangeAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreRangeAdded(
        ITypedElement element,
        IMultiplicity mult,
        IMultiplicityRange range,
        IResultCell cell)
    {
        m_Manager.onRTPreRangeAdded(element, mult, range, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreRangeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreRangeRemoved(
        ITypedElement element,
        IMultiplicity mult,
        IMultiplicityRange range,
        IResultCell cell)
    {
        m_Manager.onRTPreRangeRemoved(element, mult, range, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreTypeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreTypeModified(
        ITypedElement element,
        IClassifier proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreTypeModified(element, proposedValue, cell);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onPreUpperModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreUpperModified(
        ITypedElement element,
        IMultiplicity mult,
        IMultiplicityRange range,
        String proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreUpperModified(element, mult, range, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onRangeAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRangeAdded(
        ITypedElement element,
        IMultiplicity mult,
        IMultiplicityRange range,
        IResultCell cell)
    {
        m_Manager.onRTRangeAdded(element, mult, range, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onRangeRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRangeRemoved(
        ITypedElement element,
        IMultiplicity mult,
        IMultiplicityRange range,
        IResultCell cell)
    {
        m_Manager.onRTRangeRemoved(element, mult, range, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElementEventsSink#onTypeModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onTypeModified(ITypedElement element, IResultCell cell)
    {
        m_Manager.onRTTypeModified(element, cell);
    }
    
    public void onUpperModified(ITypedElement element, IMultiplicity mult, IMultiplicityRange range, IResultCell cell)
    {
        m_Manager.onRTUpperModified(element, mult, range, cell);
    }
    
    // -------------------------------------------------------------------------
    // IClassifierTransformEventsSink
    // -------------------------------------------------------------------------
    
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink#onPreTransform(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreTransform(
        IClassifier classifier,
        String newForm,
        IResultCell cell)
    {
        m_Manager.onRTPreTransform(classifier, newForm, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink#onTransformed(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onTransformed(IClassifier classifier, IResultCell cell)
    {
        m_Manager.onRTTransformed(classifier, cell);
    }
    
    // -------------------------------------------------------------------------
    // IAssociationEndTransformEventsSink
    // -------------------------------------------------------------------------
    
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndTransformEventsSink#onPreTransform(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreTransform(
        IAssociationEnd pEnd,
        String newForm,
        IResultCell cell)
    {
        m_Manager.onRTPreTransform(pEnd, newForm, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEndTransformEventsSink#onTransformed(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onTransformed(IAssociationEnd pEnd, IResultCell cell)
    {
        m_Manager.onRTTransformed(pEnd, cell);
    }
    
    // -------------------------------------------------------------------------
    // IRedefinableElementModifiedEventsSink
    // -------------------------------------------------------------------------

    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onFinalModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onFinalModified(IRedefinableElement element, IResultCell cell)
    {
        m_Manager.onRTFinalModified(element, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onPreFinalModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreFinalModified(
        IRedefinableElement element,
        boolean proposedValue,
        IResultCell cell)
    {
        m_Manager.onRTPreFinalModified(element, proposedValue, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onPreRedefinedElementAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreRedefinedElementAdded(
        IRedefinableElement redefiningElement,
        IRedefinableElement redefinedElement,
        IResultCell cell)
    {
        m_Manager.onRTPreRedefinedElementAdded(redefiningElement, redefinedElement, cell);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onPreRedefinedElementRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreRedefinedElementRemoved(
        IRedefinableElement redefiningElement,
        IRedefinableElement redefinedElement,
        IResultCell cell)
    {
        m_Manager.onRTPreRedefinedElementRemoved(redefiningElement, redefinedElement, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onPreRedefiningElementAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreRedefiningElementAdded(
        IRedefinableElement redefinedElement,
        IRedefinableElement redefiningElement,
        IResultCell cell)
    {
        m_Manager.onRTPreRedefiningElementAdded(redefiningElement, redefinedElement, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onPreRedefiningElementRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreRedefiningElementRemoved(
        IRedefinableElement redefinedElement,
        IRedefinableElement redefiningElement,
        IResultCell cell)
    {
        m_Manager.onRTPreRedefiningElementRemoved(redefiningElement, redefinedElement, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onRedefinedElementAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRedefinedElementAdded(
        IRedefinableElement redefiningElement,
        IRedefinableElement redefinedElement,
        IResultCell cell)
    {
        m_Manager.onRTRedefinedElementAdded(redefiningElement, redefinedElement, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onRedefinedElementRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRedefinedElementRemoved(
        IRedefinableElement redefiningElement,
        IRedefinableElement redefinedElement,
        IResultCell cell)
    {
        m_Manager.onRTRedefinedElementRemoved(redefiningElement, redefinedElement, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onRedefiningElementAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRedefiningElementAdded(
        IRedefinableElement redefinedElement,
        IRedefinableElement redefiningElement,
        IResultCell cell)
    {
        m_Manager.onRTRedefiningElementAdded(redefiningElement, redefinedElement, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink#onRedefiningElementRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onRedefiningElementRemoved(
        IRedefinableElement redefinedElement,
        IRedefinableElement redefiningElement,
        IResultCell cell)
    {
        m_Manager.onRTRedefiningElementRemoved(redefiningElement, redefinedElement, cell);
    }
    
    // -------------------------------------------------------------------------
    // IAffectedElementEventsSink
    // -------------------------------------------------------------------------
        

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAffectedElementEventsSink#onImpacted(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void onImpacted(IClassifier classifier, ETList<IVersionableElement> impacted, IResultCell cell)
    {
        m_Manager.onRTImpacted(classifier, impacted, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAffectedElementEventsSink#onPreImpacted(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void onPreImpacted(IClassifier classifier, ETList<IVersionableElement> impacted, IResultCell cell)
    {
        m_Manager.onRTPreImpacted(classifier, impacted, cell);
    }
    
    // -------------------------------------------------------------------------
    // IEventFrameworkEventsSink
    // -------------------------------------------------------------------------
    
    

   /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink#onEventContextPopped(org.netbeans.modules.uml.core.eventframework.IEventContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEventContextPopped(IEventContext pContext, IResultCell pCell)
    {
        m_Manager.onRTEventContextPopped(pContext, pCell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink#onEventContextPushed(org.netbeans.modules.uml.core.eventframework.IEventContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onEventContextPushed(IEventContext pContext, IResultCell pCell)
    {
        m_Manager.onRTEventContextPushed(pContext, pCell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink#onEventDispatchCancelled()
     */
    public void onEventDispatchCancelled(ETList<Object> pListeners, Object listenerWhoCancelled, IResultCell pCell)
    {
        m_Manager.onRTEventDispatchCancelled(pListeners, listenerWhoCancelled, pCell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink#onPreEventContextPopped(org.netbeans.modules.uml.core.eventframework.IEventContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreEventContextPopped(
        IEventContext pContext,
        IResultCell pCell)
    {
        m_Manager.onRTPreEventContextPopped(pContext, pCell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink#onPreEventContextPushed(org.netbeans.modules.uml.core.eventframework.IEventContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreEventContextPushed(
        IEventContext pContext,
        IResultCell pCell)
    {
        m_Manager.onRTPreEventContextPushed(pContext, pCell);
    }
    
    // -------------------------------------------------------------------------
    // IWSProjectEventsSink
    // -------------------------------------------------------------------------
    
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectClosed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectClosed(IWSProject project, IResultCell cell)
    {
        m_Manager.onRTWSProjectClosed(project, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectCreated(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectCreated(IWSProject project, IResultCell cell)
    {
        m_Manager.onRTWSProjectCreated(project, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectInserted(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectInserted(IWSProject project, IResultCell cell)
    {
        m_Manager.onRTWSProjectInserted(project, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectOpened(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectOpened(IWSProject project, IResultCell cell)
    {
        m_Manager.onRTWSProjectOpened(project, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreClose(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectPreClose(IWSProject project, IResultCell cell)
    {
        m_Manager.onRTWSProjectPreClose(project, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectPreCreate(
        IWorkspace space,
        String projectName,
        IResultCell cell)
    {
        m_Manager.onRTWSProjectPreCreate(space, projectName, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreInsert(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectPreInsert(
        IWorkspace space,
        String projectName,
        IResultCell cell)
    {
        m_Manager.onRTWSProjectPreInsert(space, projectName, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreOpen(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectPreOpen(
        IWorkspace space,
        String projectName,
        IResultCell cell)
    {
        m_Manager.onRTWSProjectPreOpen(space, projectName, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRemove(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectPreRemove(IWSProject project, IResultCell cell)
    {
        m_Manager.onRTWSProjectPreRemove(project, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRename(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectPreRename(
        IWSProject project,
        String newName,
        IResultCell cell)
    {
        m_Manager.onRTWSProjectPreRename(project, newName, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreSave(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectPreSave(IWSProject project, IResultCell cell)
    {
        m_Manager.onRTWSProjectPreSave(project, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectRemoved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectRemoved(IWSProject project, IResultCell cell)
    {
        m_Manager.onRTWSProjectRemoved(project, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectRenamed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectRenamed(
        IWSProject project,
        String oldName,
        IResultCell cell)
    {
        m_Manager.onRTWSProjectRenamed(project, oldName, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectSaved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectSaved(IWSProject project, IResultCell cell)
    {
        m_Manager.onRTWSProjectSaved(project, cell);
    }
    
    // -------------------------------------------------------------------------
    // IPackageEventsSink
    // -------------------------------------------------------------------------
    
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageEventsSink#onPreSourceDirModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreSourceDirModified(
        IPackage element,
        String proposedSourceDir,
        IResultCell cell)
    {
        m_Manager.onRTPreSourceDirModified(element, proposedSourceDir, cell);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageEventsSink#onSourceDirModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onSourceDirModified(IPackage element, IResultCell cell)
    {
        m_Manager.onRTSourceDirModified(element, cell);
    }

    public void onEnumerationLiteralAdded(
        IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
    {
        // TODO: implement similar to onFeatureAdded
    }

    public void onEnumerationLiteralPreAdded(
        IClassifier classifier, IEnumerationLiteral enumLit, IResultCell cell)
    {
        // TODO: implement similar to onFeaturePreAdded
    }
}
