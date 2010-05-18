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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class ElementLifeTimeEventDispatcher extends EventDispatcher implements IElementLifeTimeEventDispatcher{

	private EventManager< IElementLifeTimeEventsSink > m_Sink = null;
	private EventManager< IElementDisposalEventsSink > m_DisposalSink = null;
	private EventManager< IUnknownClassifierEventsSink > m_UnknownClassifierSink = null;

	/**
	 * 
	 */
	public ElementLifeTimeEventDispatcher() 
	{
		super();
		
		//initialize the event manager member variables
		m_Sink = new EventManager<IElementLifeTimeEventsSink>();
		m_DisposalSink = new EventManager<IElementDisposalEventsSink>();
		m_UnknownClassifierSink = new EventManager<IUnknownClassifierEventsSink>();
	}

	/**
	 *
	 * Registers the passed-in event sink with this dispatcher.
	 *
	 * @param sink[in] The actual sink that will receive notifications
	 * @param cookie[in] The unique identifier to be used when removing the listener
	 *
	 * @return HRESULT
	 * 
	 */
	public void registerForLifeTimeEvents(IElementLifeTimeEventsSink handler) {
		m_Sink.addListener(handler, null);
	}

	/**
	 *
	 * Removes a listener from the current list.
	 *
	 * @param cookie[in] The unique cookie used to identify the sink to remove
	 *
	 * @return S_OK
	 * 
	 */
	public void revokeLifeTimeSink(IElementLifeTimeEventsSink handler)
	{
		m_Sink.removeListener(handler);
		
	}

	/**
	 * Registers an event sink to handle element lifetime events.
	 * 
	 * @param handler[in] 
	 * @param cookie[out]
	 * 
	 * @return S_OK
	 */
	public void registerForDisposalEvents(IElementDisposalEventsSink handler) 
	{
		m_DisposalSink.addListener(handler, null);
	}

	/**
	 * Revokes the sink handler.
	 * 
	 * @param long cookie[in] 
	 *
	 * @return S_OK
	 */
	public void revokeDisposalSink(IElementDisposalEventsSink handler) 
	{
		m_DisposalSink.removeListener(handler);
		
	}

	/**
	 * Registers an event sink to handle unknown classifier events.
	 * 
	 * @param handler[in] 
	 * @param cookie[out]
	 * 
	 * @return S_OK
	 */
	public void registerForUnknownClassifierEvents(IUnknownClassifierEventsSink handler) 
	{
		m_UnknownClassifierSink.addListener(handler, null);
	}

	/**
	 * Revokes the sink handler.
	 * 
	 * @param long cookie[in] 
	 *
	 * @return S_OK
	 */
	public void revokeUnknownClassifierSink(IUnknownClassifierEventsSink handler) {
		m_UnknownClassifierSink.removeListener( handler );
		
	}

	/**
	 * Fired whenever an element is about to be created.
	 * 
	 * @param elementType[in] 
	 * @param payload[in]
	 * @param proceed[in]
	 *
	 * @return S_OK
	 */
	public boolean fireElementPreCreate(String elementType, IEventPayload payload) {
		boolean proceed = true;
		
		if (validateEvent("ElementPreCreate", elementType))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor preCreateFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink", "onElementPreCreate");
			
			Object[] parms = new Object[2];
			parms[0] = elementType;
			parms[1] = cell;
			preCreateFunc.setParameters(parms);
			m_Sink.notifyListenersWithQualifiedProceed(preCreateFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	/**
	 * Fired whenever an element is created.
	 * 
	 * @param element[in] 
	 * @param paylaod[in]
	 *
	 * @return S_OK
	 */
	public void fireElementCreated(IVersionableElement element, IEventPayload payload) {
		
		if (validateEvent("ElementCreated", payload))
		{
			IResultCell cell = prepareResultCell(payload);
			
			EventFunctor createFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink", "onElementCreated");
			
			Object[] parms = new Object[2];
			parms[0] = element;
			parms[1] = cell;
			createFunc.setParameters(parms);
			m_Sink.notifyListeners(createFunc);
		}
		
	}

	/**
	 * Fired whenever an element is about to be deleted.
	 * 
	 * @param ver[in] 
	 * @param payload[in]
	 * @param proceed[in]
	 *
	 * @return HRESULT
	 */
	public boolean fireElementPreDelete(IVersionableElement ver, IEventPayload payload) {
		boolean proceed = true;
		
		if (validateEvent("ElementPreDelete", ver))
		{
			IResultCell cell = prepareResultCell(payload);
			
			EventFunctor preDelFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink", "onElementPreDelete");
			
			Object[] parms = new Object[2];
			parms[0] = ver;
			parms[1] = cell;
			preDelFunc.setParameters(parms);
			m_Sink.notifyListenersWithQualifiedProceed(preDelFunc);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/**
	 * Fired whenever an element is deleted.
	 * 
	 * @param element[in] 
	 * @param paylaod[in]
	 *
	 * @return S_OK
	 */
	public void fireElementDeleted(IVersionableElement element, IEventPayload payload) {
		
		if (validateEvent("ElementDeleted", payload))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor delFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink", "onElementDeleted");
			
			Object[] parms = new Object[2];
			parms[0] = element;
			parms[1] = cell;
			delFunc.setParameters(parms);
			m_Sink.notifyListeners(delFunc);
		}
		
	}

	/**
	 * Fired whenever an element is about to be disposed of.
	 * 
	 * @param pElement[in] 
	 * @param payload[in]
	 * @param proceed[in]
	 *
	 * @return S_OK
	 */
	public boolean firePreDisposeElements(ETList<IVersionableElement> pElements, IEventPayload payload) {
		boolean proceed = true;
		
		if (validateEvent("PreDisposeElements", pElements))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preDisposeFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IElementDisposalEventsSink", "onPreDisposeElements");
			
			Object[] parms = new Object[2];
			parms[0] = pElements;
			parms[1] = cell;
			preDisposeFunc.setParameters(parms);
			m_DisposalSink.notifyListenersWithQualifiedProceed(preDisposeFunc);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/**
	 * Fired whenever after an element is created.
	 * 
	 * @param pElements[in] 
	 * @param payload[in]
	 *
	 * @return S_OK
	 */
	public void fireDisposedElements(ETList<IVersionableElement> pElements, IEventPayload payload) {
		
		if (validateEvent("DisposedElements", pElements) )
		{
			IResultCell cell = prepareResultCell(payload);
			
			EventFunctor disposeFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IElementDisposalEventsSink", "onDisposedElements");
			
			Object[] parms = new Object[2];
			parms[0] = pElements;
			parms[1] = cell;
			disposeFunc.setParameters(parms);
			m_DisposalSink.notifyListeners(disposeFunc);
		}
		
	}

	/**
	 * Fired whenever an element is about to be duplicated.
	 * 
	 * @param element[in] 
	 * @param payload[in]
	 * @param proceed[in]
	 *
	 * @return S_OK
	 */
	public boolean fireElementPreDuplicated(IVersionableElement element, IEventPayload payload) {
		boolean proceed = true;
		
		if (validateEvent("ElementPreDuplicated", element))
		{
			IResultCell cell = prepareResultCell(payload);
			
			EventFunctor preDupFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink", "onElementPreDuplicated");
			
			Object[] parms = new Object[2];
			parms[0] = element;
			parms[1] = cell;
			preDupFunc.setParameters(parms);
			m_Sink.notifyListenersWithQualifiedProceed(preDupFunc);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/**
	 * Fired whenever an element is duplicated.
	 * 
	 * @param element[in] 
	 * @param payload[in]
	 *
	 * @return HRESULT
	 */
	public void fireElementDuplicated(IVersionableElement element, IEventPayload payload) {
		
		if (validateEvent("ElementDuplicated", element))
		{
			IResultCell cell = prepareResultCell(payload);
			
			EventFunctor dupFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink", "onElementDuplicated");
			
			Object[] parms = new Object[2];
			parms[0] = element;
			parms[1] = cell;
			dupFunc.setParameters(parms);
			m_Sink.notifyListeners(dupFunc);
		}
		
	}

	/**
	 * Fired when a new classifier is about to be created as specified by the unknown classifier preference.
	 * 
	 * @param element[in] 
	 * @param payload[in]
	 *
	 * @return HRESULT
	 */
	public boolean firePreUnknownCreate(String typeToCreate, IEventPayload payload) {
		boolean proceed = true;
		if (validateEvent("PreUnknownCreate", typeToCreate))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preUnknownFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IUnknownClassifierEventsSink", "onPreUnknownCreate");
			
			Object[] parms = new Object[2];
			parms[0] = typeToCreate;
			parms[1] = cell;
			preUnknownFunc.setParameters(parms);
			m_UnknownClassifierSink.notifyListenersWithQualifiedProceed(preUnknownFunc);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/**
	 * Fired whenever an Unknown is created.
	 * 
	 * @param element[in] 
	 * @param payload[in]
	 *
	 * @return HRESULT
	 */
	public void fireUnknownCreated(INamedElement newType, IEventPayload payload) {
		if (validateEvent("UnknownCreated", newType))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor unknownFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IUnknownClassifierEventsSink", "onUnknownCreated");
			
			Object[] parms = new Object[2];
			parms[0] = newType;
			parms[1] = cell;
			unknownFunc.setParameters(parms);
			m_UnknownClassifierSink.notifyListeners(unknownFunc);
		}
		
	}

	/**
	 * IEventDispatcher override.  Returns the number of registered sinks
	 */
	public int getNumRegisteredSinks()
	{
		int numSink = 0;
	   try
	   {
		  numSink = m_Sink.getNumListeners() + m_DisposalSink.getNumListeners();
	   }
	   catch ( Exception err )
	   {
	   }
	   return numSink;
	}

}


