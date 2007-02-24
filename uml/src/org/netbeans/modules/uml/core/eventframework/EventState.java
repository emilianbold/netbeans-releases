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


package org.netbeans.modules.uml.core.eventframework;

/**
 * @author sumitabhk
 *
 */
public class EventState {

	private IEventDispatcher m_Dispatch = null;

	/**
	 *
	 */
	private EventState() {
		super();
	}

	/**
	 * The constructor for EventState. Handles the call to the dispatcher's
	 * PushEventContext() function.
	 */
	public EventState( IEventDispatcher dispatcher, String state )
	{
	   m_Dispatch = dispatcher;
	   if( m_Dispatch != null)
	   {
		  m_Dispatch.pushEventContext(state);
	   }
	}

	/**
	 * Pushes the passed-in context onto the passed-in dispatcher.
	 *
	 * @param dispatcher[in]   The dispatcher to push the context onto
	 * @param context[in]      The context to push
	 *
	 * @return HRESULT
	 */
	public EventState( IEventDispatcher dispatcher, IEventContext context ) 
	{
		m_Dispatch =  dispatcher ;
	   if( m_Dispatch != null)
	   {
		  m_Dispatch.pushEventContext3(context );
	   }
	}

	public EventState( String dispatcherName, String state )
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
	   	m_Dispatch = (IEventDispatcher) ret.getDispatcher(dispatcherName);
		if( m_Dispatch != null)
	   	{
		   m_Dispatch.pushEventContext(state);
	    }
	}
	
	public void existState()
   {
      try
      {
      	m_Dispatch.popEventContext();
      }
      catch (NullPointerException e)
      {
         // I do not want to do anything.  If m_Dispatch
         // is NULL then there is nothing to do.
      }
   }
	

}


