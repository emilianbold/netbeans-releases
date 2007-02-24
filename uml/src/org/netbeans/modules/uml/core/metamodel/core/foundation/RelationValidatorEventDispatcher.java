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

package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 * @author sumitabhk
 *
 */
public class RelationValidatorEventDispatcher extends EventDispatcher implements IRelationValidatorEventDispatcher{

	private EventManager<IRelationValidatorEventsSink> m_RelValidatorSink = 
        new EventManager<IRelationValidatorEventsSink>();
	private EventManager<IRelationEventsSink> m_RelSink = 
        new EventManager<IRelationEventsSink>();

	/**
	 * 
	 */
	public RelationValidatorEventDispatcher() 
	{
		super();
		m_RelSink = new EventManager<IRelationEventsSink>();
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
	public void registerForRelationValidatorEvents(IRelationValidatorEventsSink handler) 
	{
		m_RelValidatorSink.addListener( handler, null );
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
	public void registerForRelationEvents(IRelationEventsSink handler) 
	{
		m_RelSink.addListener( handler, null );
		
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
	public void revokeRelationValidatorSink(IRelationValidatorEventsSink handler) 
	{
		m_RelValidatorSink.removeListener( handler );
		
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
	public void revokeRelationSink(IRelationEventsSink handler) 
	{
		m_RelSink.removeListener( handler );
		
	}

	/**
	 *
	 * Dispatches the OnPreRelationValidate event.
	 *
	 * @param proxy[in] The IRelationProxy that is about to be validated
	 * @param payload[in] The EventPayload to include with the event
	 * @param proceed[out] true if the event was fully dispatched, else
	 *                     false if a listener cancelled full dispatch.
	 *
	 * @return HRESULT
	 * 
	 */
	public boolean firePreRelationValidate(IRelationProxy proxy, IEventPayload payload) {
		boolean proceed = true;
		if (validateEvent("PreRelationValidate", proxy))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preRelValidFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationValidatorEventsSink", "onPreRelationValidate");
			
			Object[] parms = new Object[2];
			parms[0] = proxy;
			parms[1] = cell;
			preRelValidFunc.setParameters(parms);
			
			m_RelValidatorSink.notifyListenersWithQualifiedProceed(preRelValidFunc);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/**
	 *
	 * Dispatches the OnRelationValidated event.
	 *
	 * @param proxy[in] The IRelationProxy that is about to be validated
	 * @param payload[in] The EventPayload to include with the event
	 *
	 * @return HRESULT
	 * 
	 */
	public void fireRelationValidated(IRelationProxy proxy, IEventPayload payload) {
		
		if (validateEvent("RelationValidated", proxy))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor relValidFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationValidatorEventsSink", "onRelationValidated");
			
			Object[] parms = new Object[2];
			parms[0] = proxy;
			parms[1] = cell;
			relValidFunc.setParameters(parms);
			
			m_RelValidatorSink.notifyListeners(relValidFunc);
		}
		
	}

	/**
	 *
	 * Dispatches the OnPreRelationModified event.
	 *
	 * @param proxy[in] The IRelationProxy that is about to be modified
	 * @param payload[in] The EventPayload to include with the event
	 * @param proceed[out] true if the event was fully dispatched, else
	 *                     false if a listener cancelled full dispatch
	 *
	 * @return HRESULT
	 * 
	 */
    public boolean firePreRelationEndModified(IRelationProxy proxy, IEventPayload payload) {
        boolean proceed = true;
		
        if (validateEvent("PreRelationEndModified", proxy))
		{
            IResultCell cell = prepareResultCell(payload);
			
			EventFunctor preRelEndModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink", "onPreRelationEndModified");
			
			Object[] parms = new Object[2];
			parms[0] = proxy;
			parms[1] = cell;
			preRelEndModFunc.setParameters(parms);
			
            m_RelSink.notifyListenersWithQualifiedProceed(preRelEndModFunc);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/**
	 *
	 * Dispatches the OnRelationModified event.
	 *
	 * @param proxy[in] The IRelationProxy that is about to be modified
	 * @param payload[in] The EventPayload to include with the event
	 *
	 * @return HRESULT
	 * 
	 */
	public void fireRelationEndModified(IRelationProxy proxy, IEventPayload payload) {
		
		if (validateEvent("RelationEndModified", proxy))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor relEndModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink", "onRelationEndModified");
			
			Object[] parms = new Object[2];
			parms[0] = proxy;
			parms[1] = cell;
			relEndModFunc.setParameters(parms);
			
			m_RelSink.notifyListeners(relEndModFunc);
		}
		
	}

	/**
	 * Fired before a relation meta type is added to.  This includes
	 * Dependency, Generalization, and Associations.
	 *
	 * @param proxy[in]
	 * @param payload[in]
	 * @param proceed[in]
	 *
	 * @result HRESULT
	 */
	public boolean firePreRelationEndAdded(IRelationProxy proxy, IEventPayload payload) {
		boolean proceed = true;
		
		if (validateEvent("PreRelationEndAdded", proxy))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preRelEndAddFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink", "onPreRelationEndAdded");
			
			Object[] parms = new Object[2];
			parms[0] = proxy;
			parms[1] = cell;
			preRelEndAddFunc.setParameters(parms);
			
			m_RelSink.notifyListenersWithQualifiedProceed(preRelEndAddFunc);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/**
	 *
	 * Dispatches the OnRelationEndAdded event.
	 *
	 * @param proxy[in] The IRelationProxy that is about to be modified
	 * @param payload[in] The EventPayload to include with the event
	 *
	 * @return HRESULT
	 * 
	 */
	public void fireRelationEndAdded(IRelationProxy proxy, IEventPayload payload) {
		if (validateEvent("RelationEndAdded", proxy))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor relEndAddFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink", "onRelationEndAdded");
			
			Object[] parms = new Object[2];
			parms[0] = proxy;
			parms[1] = cell;
			relEndAddFunc.setParameters(parms);
			
			m_RelSink.notifyListeners(relEndAddFunc);
		}
		
	}

	/**
	 * Fired before a relation meta type is removed.  This includes
	 * Dependency, Generalization, and Association.
	 *
	 * @param proxy[in]
	 * @param payload[in]
	 * @param proceed[in]
	 *
	 * @result HRESULT
	 */
	public boolean firePreRelationEndRemoved(IRelationProxy proxy, IEventPayload payload) {
		boolean proceed = true;
		if (validateEvent("PreRelationEndRemoved", proxy))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preRelEndRemFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink", "onPreRelationEndRemoved");
			
			Object[] parms = new Object[2];
			parms[0] = proxy;
			parms[1] = cell;
			preRelEndRemFunc.setParameters(parms);
			
			m_RelSink.notifyListenersWithQualifiedProceed(preRelEndRemFunc);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/**
	 * Fired after a relation meta type has been removed.  This includes
	 * Dependency, Generalization and Associations.
	 *
	 * @param proxy[in]
	 * @param payload[in]
	 *
	 * @result HRESULT
	 */
	public void fireRelationEndRemoved(IRelationProxy proxy, IEventPayload payload) {
		if (validateEvent("RelationEndRemoved", proxy))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor relEndRemFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink", "onRelationEndRemoved");
			
			Object[] parms = new Object[2];
			parms[0] = proxy;
			parms[1] = cell;
			relEndRemFunc.setParameters(parms);
			
			m_RelSink.notifyListeners(relEndRemFunc);
		}
		
	}

	/**
	 * Fired before a relation meta type is created.
	 *
	 * @param proxy[in]
	 * @param payload[in]
	 * @param proceed[in]
	 *
	 * @result HRESULT
	 */
	public boolean firePreRelationCreated(IRelationProxy proxy, IEventPayload payload) {
		boolean proceed = true;
		
		if (validateEvent("PreRelationCreated", proxy))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preRelCreateFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink", "onPreRelationCreated");
			
			Object[] parms = new Object[2];
			parms[0] = proxy;
			parms[1] = cell;
			preRelCreateFunc.setParameters(parms);
			
			m_RelSink.notifyListenersWithQualifiedProceed(preRelCreateFunc);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/**
	 * Fired after a relation meta type has been created.
	 *
	 * @param proxy[in]
	 * @param payload[in]
	 *
	 * @result HRESULT
	 */
	public void fireRelationCreated(IRelationProxy proxy, IEventPayload payload) {
		
		if (validateEvent("RelationCreated", proxy))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor relCreateFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink", "onRelationCreated");
			
			Object[] parms = new Object[2];
			parms[0] = proxy;
			parms[1] = cell;
			relCreateFunc.setParameters(parms);
			
			m_RelSink.notifyListeners(relCreateFunc);
		}
		
	}

	/**
	 * Fired before a relation meta type is deleted;
	 *
	 * @param proxy[in]
	 * @param payload[in]
	 * @param proceed[in]
	 *
	 * @result HRESULT
	 */
	public boolean firePreRelationDeleted(IRelationProxy proxy, IEventPayload payload) {
		boolean proceed = true;
		
		if (validateEvent("PreRelationDeleted", proxy))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preRelDelFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink", "onPreRelationDeleted");
			
			Object[] parms = new Object[2];
			parms[0] = proxy;
			parms[1] = cell;
			preRelDelFunc.setParameters(parms);
			
			m_RelSink.notifyListenersWithQualifiedProceed(preRelDelFunc);
			proceed = cell.canContinue();
		}
		return proceed;
	}

	/**
	 * Fired after a relation meta type has been deleted.
	 *
	 * @param proxy[in]
	 * @param payload[in]
	 *
	 * @result HRESULT
	 */
	public void fireRelationDeleted(IRelationProxy proxy, IEventPayload payload) {
		
		if (validateEvent("RelationDeleted", proxy))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor relDelFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationEventsSink", "onRelationDeleted");
			
			Object[] parms = new Object[2];
			parms[0] = proxy;
			parms[1] = cell;
			relDelFunc.setParameters(parms);
			
			m_RelSink.notifyListeners(relDelFunc);
		}
		
	}

	/**
	 * IEventDispatcher override.  Returns the number of registered sinks
	 */
	public int getNumRegisteredSinks()
	{
		return m_RelSink.getNumListeners();
	}

}


