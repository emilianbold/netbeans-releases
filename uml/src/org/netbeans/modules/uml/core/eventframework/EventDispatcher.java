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


package org.netbeans.modules.uml.core.eventframework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import org.dom4j.Document;
import org.dom4j.Node;
import java.util.List;
//import org.apache.xpath.XPathAPI;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.ResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

public class EventDispatcher implements IEventDispatcher
{
	private static HashMap<String,Class> payloadCache = new HashMap<String,Class>();
    private boolean m_preventAllEvents = false;
    private Vector < IEventContext > m_ContextStack = new Vector < IEventContext >();
    private EventManager<IEventFrameworkEventsSink> m_manager = 
        new EventManager<IEventFrameworkEventsSink>();
    private EventFunctor m_CancelFunc = null;
    private EventFunctor m_PrePopFunc = null;
    private EventFunctor m_PostPopFunc = null;
    private EventFunctor m_PrePushFunc = null;
    private EventFunctor m_PostPushFunc = null;

	/**
	 * Registers an event sink to handle frameework events.
	 */
	public void registerForEventFrameworkEvents( IEventFrameworkEventsSink handler )
	{
		try 
		{
		 	m_manager.addListener(handler, null);
		   m_manager.setDispatcher(this);
		}
		catch(Exception e)
		{
		}
	}

	/**
	 * Removes a sink listening for framework events.
	*/
	public void revokeEventFrameworkSink( IEventFrameworkEventsSink handler )
	{
       m_manager.removeListener(handler);
	}

	/**
	 *
	 * Fired right after a context has been pushed on this dispatcher.
	 *
	 * @param pContext[in]  The context pushed
	 * @param payload[in]   The payload
	 *
	 * @return HRESULT
	 *
	 */
	 public void fireEventContextPushed( IEventContext pContext, IEventPayload payLoad )
     {
		
			if( validateEvent( "EventContextPushed",  pContext))
			{
				IResultCell cell = prepareResultCell(payLoad );
				if (m_PostPushFunc == null)
				{
					m_PostPushFunc = new EventFunctor("org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink", "onEventContextPushed");
				}
				Object[] parms = new Object[2];
				parms[0] = pContext;
				parms[1] = cell;
				m_PostPushFunc.setParameters(parms);
				m_manager.notifyListeners(m_PostPushFunc);
			}
     }

	/**
	 *
	 * Fired before an EventContext is pushed onto this Dispatcher.
	 *
	 * @param pContext[in]  The context about to be pushed
	 * @param payload[in]   The EventPayload to include with the event dispatch. Can be 0.
	 * @param proceed[out]  true if the event was fully dispatched, else
	 *                      false if a listener cancelled full dispatch.
	 *
	 * @return HRESULT
	 *
	 */
 	public boolean firePreEventContextPushed( IEventContext pContext, IEventPayload payLoad )
   {
	 	boolean proceed = true;
	     	
   	//proceed = validateEvent("PreEventContextPushed", pContext);

		if(validateEvent("PreEventContextPushed", pContext) == true)
		{
			IResultCell cell = prepareResultCell(payLoad);
			if(m_PrePushFunc == null)
			{
				m_PrePushFunc = new EventFunctor("org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink", "onPreEventContextPushed");
			}
			Object[] parms = new Object[2];
			parms[0] = pContext;
			parms[1] = cell;
			m_PrePushFunc.setParameters(parms);
			m_manager.notifyListenersWithQualifiedProceed(m_PrePushFunc);			
		
			proceed = cell.canContinue();
		}
		
     	return proceed;
   }

	/**
	 *
	 * Fired before an EventContext is popped from this Dispatcher.
	 *
	 * @param pContext[in]  The context about to be popped
	 * @param payload[in]   The EventPayload to include with the event dispatch. Can be 0.
	 * @param proceed[out]  true if the event was fully dispatched, else
	 *                      false if a listener cancelled full dispatch.
	 *
	 * @return HRESULT
	 *
	 */
     public boolean firePreEventContextPopped( IEventContext pContext, IEventPayload payLoad )
     {
     	boolean proceed = true;
		
		if( validateEvent( "PreEventContextPopped", pContext))
		{
			IResultCell cell = prepareResultCell(payLoad);
			if(m_PrePopFunc == null)
			{
				m_PrePopFunc = new EventFunctor("org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink", "onPreEventContextPopped");
			}
			Object[] parms = new Object[2];
			parms[0] = pContext;
			parms[1] = cell;
			m_PrePopFunc.setParameters(parms);
			m_manager.notifyListenersWithQualifiedProceed(m_PrePopFunc);
			proceed = cell.canContinue();
			
/*   TwoParmFunctor< IEventFrameworkEventsSink, IEventContext*, IResultCell* > func( 0, 
										 &IEventFrameworkEventsSink::OnPreEventContextPopped, 
										 pContext,
										 cell );
		   ResultCellFunctor< IEventFrameworkEventsSink > cellFunc( 0, cell, &func );

		   _VH( m_FrameworkSink.NotifyListenersWithQualifiedProceed( cellFunc ));
		   _VH( cell->get_Continue( proceed )); */
		}
        return proceed;
     }

	/**
	 *
	 * Fired right after a context has been popped from this dispatcher.
	 *
	 * @param pContext[in]  The context popped
	 * @param payload[in]   The payload
	 *
	 * @return HRESULT
	 *
	 */
     public void fireEventContextPopped( IEventContext pContext, IEventPayload payLoad )
     {
		
		if( validateEvent( "EventContextPopped", pContext) )		
		{
			IResultCell cell = prepareResultCell(payLoad);
			if (m_PostPopFunc == null)
			{
				m_PostPopFunc = new EventFunctor("org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink", "onEventContextPopped");
			}
			Object[] parms = new Object[2];
			parms[0] = pContext;
			parms[1] = cell;
			m_PostPopFunc.setParameters(parms);
			m_manager.notifyListeners(m_PostPopFunc);

/*		   TwoParmFunctor< IEventFrameworkEventsSink, 
						   IEventContext*, 
						   IResultCell* > func ( 0, 
												 &IEventFrameworkEventsSink::OnEventContextPopped, 
												 pContext, 
												 cell );
		   _VH( m_FrameworkSink.NotifyListeners( func )); */
		}
     }


	/**
	 *
	 * Fired when a dispatch of a particular event is cancelled by a listener.
	 *
	 * @param pListeners[in]            The collection of listeners that were already dispatched to.
	 * @param listenerWhoCancelled[in]  The listener that cancelled the dispatch
	 * @param payload[in]               EventPayload
	 *
	 * @return HRESULT
	 *
	 */
	public void fireEventDispatchCancelled( Object[] pListeners, Object listenerWhoCancelled, IEventPayload payLoad )
   {
		
		ArrayList <Object > var = new ArrayList <Object>();
		
		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		var.add( pListeners );
		var.add( listenerWhoCancelled );

		//prepareVariant( collection, var ));

		if( validateEvent( "EventEventDispatchCancelled", var))
		{
			IResultCell cell = prepareResultCell(payLoad);
			
			if(m_CancelFunc == null)
			{
				m_CancelFunc = new EventFunctor("org.netbeans.modules.uml.core.eventframework.IEventFrameworkEventsSink", "onEventDispatchCancelled");
			}			
			Object[] parms = new Object[3];
			parms[0] = pListeners;
			parms[1] = listenerWhoCancelled;
			parms[2] = cell;
			m_CancelFunc.setParameters(parms);
			m_manager.notifyListeners(m_CancelFunc);
		}
   }

	/**
	 *
	 * Establishes a new EventContext of the given name on the internal
	 * stack of EventContext objects.
	 *
	 * @param context[in] The name of the context to push on the stack
	 # @param pVal[out] The context that was created, else 0.
	 *
	 * @return HRESULT
	 * 
	 */
     public void pushEventContext( String name )
     {
		IEventContext con = createEventContext(name);
		if (con != null)
		{
			pushEventContext3(con);
		}
     }

	/**
	 *
	 * Establishes a new EventContext of the given name on the internal
	 * stack of EventContext objects.
	 *
	 * @param context[in] The name of the context to push on the stack
	 *
	 * @return HRESULT
	 * 
	 */
     public IEventContext pushEventContext2( String context )
     {
     	IEventContext con = createEventContext(context);
     	if (con != null)
     	{
     		pushEventContext3(con);
     	}
        return con;
     }

	/**
	 *
	 * Establishes the passed in EventContext as the current context.
	 *
	 * @param pContext[in] The EventContext to push on the stack
	 *
	 * @return HRESULT
	 *
	 */
     public void pushEventContext3( IEventContext pContext )
     {
     	boolean proceed = true;
     	IEventPayload payload = createPayload( "PreEventContextPushed" );
     	proceed = firePreEventContextPushed(pContext, payload);
     	if (proceed)
     	{
     		m_ContextStack.add(pContext);
     		payload = createPayload("EventContextPushed");
     		fireEventContextPushed(pContext, payload);
     	}
     }

	/**
	 *
	 * Removes the top context on the stack.
	 *
	 * @return S_OK
	 * 
	 */
     public void popEventContext()
     {
        if (m_ContextStack.size() > 0)
        {
        	internalPopEventContext();
        }
     }

	/**
	 *
	 * Pops the current context off our internal stack, firing appropriate events.
	 *
	 * @param context[out] The event context popped
	 *
	 * @return HRESULT
	 *
	 */
	private IEventContext internalPopEventContext() {
		IEventContext retContext = null;
		boolean proceed = true;
		IEventPayload payload = createPayload( "PreEventContextPopped");
		if (m_ContextStack.size() > 0)
		{
			IEventContext curContext = m_ContextStack.lastElement();
			proceed = firePreEventContextPopped(curContext, payload);
			if (proceed)
			{
				m_ContextStack.remove(curContext);
				payload = createPayload("EventContextPopped");
				fireEventContextPopped(curContext, payload);
				retContext = curContext;
			}
		}
		return retContext;
	}

	/**
	 *
	 * Pops the current event context off the stack and returns it.
	 *
	 * @param pContext[out] The context just popped
	 *
	 * @return HRESULT
	 *
	 */
     public IEventContext popEventContext2()
     {
     	IEventContext context = null;
		if( m_ContextStack.size() > 0 )
		{
		   context = internalPopEventContext();
		}
        return context;
     }

	/**
	 *
	 * Retrieves the current context on this dispatcher.
	 *
	 * @param pContext[out] The current EventContext
	 *
	 * @return HRESULT
	 *
	 */
     public IEventContext getCurrentContext()
     {
     	IEventContext context = null;
     	if (m_ContextStack.size() > 0)
     	{
     		context = m_ContextStack.lastElement();
     	}
        return context;
     }

	/**
	 *
	 * Retrieves the name of the current context on this dispatcher.
	 *
	 * @param pContext[out] The current EventContext
	 *
	 * @return HRESULT
	 *
	 */
     public String getCurrentContextName()
     {
		String name = null;
		if (m_ContextStack.size() > 0)
		{
			IEventContext context = m_ContextStack.lastElement();
			name = context.getName();
		}
		return name;
     }

	/**
	 *
	 * Removes an EventContext by name.
	 *
	 * @param name[in] The name of the context to remove
	 *
	 * @return HRESULT
	 *
	 */
     public void removeEventContextByName( String name )
     {
     	int count = m_ContextStack.size();
     	if (name.length()>0 && count > 0)
     	{
     		for(int i=0; i<count; i++)
     		{
     			IEventContext context = m_ContextStack.elementAt(i);
     			String contName = context.getName();
     			if (contName.equals(name))
     			{
     				internalEraseEventContext(context);
     				break;
     			}
     		}
     	}
     }

	/**
	 *
	 * Erases a context off our internal stack, firing appropriate events.
	 *
	 * @param iter[in] The iterator to be used to erase the context from the stack
	 *
	 * @return HRESULT
	 *
	 */
	private void internalEraseEventContext(IEventContext context) {
		boolean proceed = true;
		IEventPayload payload = createPayload( "PreEventContextPopped");
		if (m_ContextStack.size() > 0)
		{
			proceed = firePreEventContextPopped(context, payload);
			if (proceed)
			{
				m_ContextStack.remove(context);
				payload = createPayload("EventContextPopped");
				fireEventContextPopped(context, payload);
			}
		}
	}

	/**
	 *
	 * Removes the context that contains a filter with the passed in ID.
	 *
	 * @param filterID[in] The ID to match against
	 *
	 * @return HRESULT
	 *
	 */
     public void removeEventContextByFilterID( String filterID )
     {
		int count = m_ContextStack.size();
		if (filterID.length()>0 && count > 0)
		{
			for(int i=0; i<count; i++)
			{
				IEventContext context = m_ContextStack.elementAt(i);
				IEventFilter filter = context.getFilter();
				if (filter != null)
				{
					String curID = filter.getFilterID();
					if (curID != null && curID.equals(filterID))
					{
						internalEraseEventContext(context);
						break;
					}
				}
			}
		}
     }

	/**
	 *
	 * Creates the appropriate payload as dictated by the mechanism file.
	 *
	 * @param triggerName[in] Name of the trigger by which the appropriate payload can
	 *                        be determined.
	 * @param payLoad[out] The new payload
	 *
	 * @return HRESULT
	 * 
	 */
     public IEventPayload createPayload( String triggerName )
     {
     	IEventPayload payload = null;
     	try {
			if (!m_preventAllEvents)
			{
				if (payloadCache.containsKey(triggerName)) {
					Class pc = payloadCache.get(triggerName);
					if (pc != null) {
						try {
							return (IEventPayload) pc.newInstance();
						}
						catch (Exception e) {
							Log.stackTrace(e);
						}
					}
					return null;
				}

				boolean resolved = false;
				Document mech = getMechanism();
				if (mech != null)
				{
					String query = "//EMBT:TriggerPoint[@name='";
					query += triggerName;
					query += "']";
					org.dom4j.Node node = XMLManip.selectSingleNode(mech, query);
					if (node != null)
					{
						String value = XMLManip.getAttributeValue(node, "payLoad");
						// It is very valid to NOT have a payLoad attribute
						if (value != null && value.length() > 0)
						{
							try {

								Class c = Class.forName(value);
								payloadCache.put(triggerName, c);
								resolved = true;
								payload = (IEventPayload) c.newInstance();
							} catch(Exception e)
							{}
						}
					}
				}
				if (!resolved && payload == null)
					payloadCache.put(triggerName, null);
			}
     	}catch (Exception e)
     	{
     	}
        return payload;
     }

        public boolean getPreventAllEvents()
        {
            return m_preventAllEvents;
        }

        public void setPreventAllEvents( boolean value )
        {
            m_preventAllEvents = value;
        }

	/**
	 *
	 * Returns how many listeners are associated with this dispatcher
	 *
	 * @param pVal[out] The number of listeners on this dispatcher
	 *
	 * @return HRESULT
	 *
	 */
   public int getNumRegisteredSinks()
   {
     return m_manager.getNumListeners();
   }
	
//	/**
//	 *
//	 * Validates the trigger that is about to trigger the event, and returns a prepared 
//	 * result cell to go with the event, if the trigger validated.
//	 *
//	 * @param triggerName[in] The name of the trigger causing the event
//	 * @param var[in] The VARIANT to pass to the EventContext for event validation purposes
//	 * @param payload[in] The EventPayload to include with the result cell. Can be 0
//	 * @param cell[out] The IResultCell, else 0 if the event did not validate
//	 *
//	 * @return true if the trigger validated, and the resultant event can proceed, else false.
//	 * 
//	 */
//	protected boolean validateAndPrepareResultCell( String triggerName, Object var, IEventPayload payload, IResultCell cell)
//	{
//	   boolean validated = false;
//
//	   cell = null;
//	   validated = validateEvent( triggerName, var ) ? true : false;
//
//	   if( validated )
//	   {
//		  cell = prepareResultCell( payload );
//	   }
//	   return validated;
//	}

	/**
	 * Creates and prepares the IResultCell object that will be included with the event.
	 *
	 * @param payload[in] The payload to include with the event cell. Can be 0
	 * @param cell[out] The ResultCell
	 *
	 * @return HRESULT
	 */
	protected IResultCell prepareResultCell( IEventPayload payload)
	{
		IResultCell cell = new ResultCell(); 

		if( payload != null)
		{
		   // Set the payLoad on the ResultCell
		   cell.setContextData(payload);
		}
	    return cell;
	}

	/**
	 * Validates the trigger, preventing or allowing the resultant event
	 * to occur or not.
	 *
	 * @param triggerName[in] Name of the trigger currently getting pulled
	 * @param payLoad[in] The Variant holding the payload of the event
	 * @param proceed[out] true if the event is allowed, else false
	 *
	 * @return HRESULT
	 */

	protected boolean validateEvent( String triggerName, Object payLoad )
	{
	   boolean valid = true;

	   if( m_preventAllEvents )
	   {
		  valid = false;
	   }
	   else
	   {
		  if( m_ContextStack.size() > 0)
		  {
		  	IEventContext curContext = m_ContextStack.lastElement();
		  	valid = curContext.validateEvent(triggerName, payLoad);
		  }
	   }
	   return valid;
	}

	/**
	 * Retrieves the prog id for the default EventContext implementation.
	 * It does this by retrieving the setting of the eventContextProgID 
	 * xml attribute off the EventFramework element itself.
	 *
	 * @return The progID. If this comes back empty, something is 
	 *         very wrong.
	 */
	private String retrieveDefaultContextProgID()
	{
	   String progID = null;

	   Document mech = getMechanism( );
	   if( mech != null)
	   {
	   	
		  List list = mech.selectNodes("EventMechanism");
		  if (list != null && list.size() > 0)
		  {
		  	Node node = (Node)list.get(0);
		  	progID = XMLManip.getAttributeValue(node, "eventContextProgID");
		  }
	   }
	   return progID;
	}

	/**
	 * Creates the EventContext object that corresponds to the passed
	 * in context state.
	 *
	 * @param context[in] The name of the Context to create
	 * @param pVal[out] The created Context
	 *
	 * @return HRESULT
	 */
	protected IEventContext createEventContext( String sContext)
	{
		IEventContext context = null; 
	   try
	   {
		  org.dom4j.Node node = null;
		  String contextProgID = retrieveContextProgID( sContext, node );

		  if( contextProgID.length() > 0 )
		  {
			context = (IEventContext)Class.forName(contextProgID).newInstance();

			if( node != null)
			{
			   context.setDom4JNode(node);
			}
			else
			{
				context.setName(sContext);
			}
		  }
	   }
	   catch( Exception err )
	   {
	   }
	   return context;
	}

	/**
	 * Retrieves the ProgID of the EventContext object.
	 *
	 * @return The progID
	 */
	private String retrieveContextProgID(String sContext, org.dom4j.Node node) {
		String progID = null;
		try {
			Document mech = getMechanism();
			if (mech != null)
			{
				// First check to see if the specific context has a progID associated with it.
				// If it does, use that. If it doesn't, pull the progID off the EventFramework's
				// eventContextProgID attribute
				String query = "//EMBT:EventContext[@name=\"";
				query += sContext;
				query += "\"]";
			
				node = XMLManip.selectSingleNode(mech, query);
				if (node != null)
				{
					progID = XMLManip.getAttributeValue(node, "progID");
				}
				if (progID == null || progID.length() == 0)
				{
					progID = retrieveDefaultContextProgID();
				}
			}
		} catch (Exception e)
		{}
		return progID;
	}

	/**
	 * Retrieves the document that represents the event
	 * mechanism framework configuration file.
	 *
	 * @param doc[out] The document, else 0
	 *
	 * @return HRESULT
	 */
	protected Document getMechanism()
	{
	   Document doc = null;

	   try
	   {
		  EventMechanism mech = EventMechanism.instance();
		  doc = mech.mechanism();
	   }
	   catch( Exception err )
	   {
	   }
	   return doc;
	}

	protected Object prepareVariant(Vector < Object > col)
	{
		if (col != null && col.size() > 0)
		{
			return col;
		}
		return null;
	}
}
