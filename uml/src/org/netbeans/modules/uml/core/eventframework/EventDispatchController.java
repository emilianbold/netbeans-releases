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

import java.util.Enumeration;
import java.util.Hashtable;

public class EventDispatchController extends EventDispatcher implements IEventDispatchController

{
	/// EventDispatcher ID, actual EventDispatcher
	private Hashtable < String, IEventDispatcher >  m_Dispatchers = new Hashtable < String, IEventDispatcher >();
	
	public EventDispatchController()
	{
		super();
	}

	/**
	 *
	 * Adds a new EventDispatcher to this Controller.
	 *
	 * @param id[in] The unique identifier of the dispatcher
	 * @param dispatcher[in] The actual Dispatcher
	 *
	 * @return S_OK unless there is a Dispatcher with the same ID. In that
	 *         case, EDCR_E_DISPATCHER_EXISTS is returned 
	 */
   public long addDispatcher( String id, IEventDispatcher dispatcher )
   {
     	validateID(id);
     	m_Dispatchers.put(id, dispatcher);
      return 0;
   }

	/**
	 *
	 * Adds the EventDispatcher that matches the passed in enumeration.
	 *
	 * @param nKind[in] The dispatcher to match against
	 * @param dispatcher[out] The EventDispatcher
	 *
	 * @return HRESULT
	 */
     public long addDispatcher( /* EventDispatcherKind */ int nKind, IEventDispatcher dispatcher )
     {
     	int eventDispatchType = getDispatchType(nKind);
     	if (eventDispatchType < 0)
     	{
     		eventDispatchType = EventDispatchNameKeeper.EDT_WORKSPACE_KIND;
     	}
     	addDispatcher(EventDispatchNameKeeper.dispatcherName(eventDispatchType), dispatcher);
        return 0;
     }

	/**
	 * Removes the EventDispatcher with the matching ID from this Controller and returns it
	 *
	 * @param id[in] The ID of the dispatcher to remove
	 * @param dispatcher[out] The removed EventDispatcher
	 *
	 * @return HRESULT
	 */
     public IEventDispatcher removeDispatcher( String id )
     {
     	IEventDispatcher disp = null;
     	if (m_Dispatchers != null)
     	{
			disp = m_Dispatchers.get(id);
     		if (disp != null)
     		{
     			m_Dispatchers.remove(id);
     		}
     	}
        return disp;
     }

	/**
	 * Removes the EventDispatcher that matches the passed in enumeration.
	 *
	 * @param nKind[in] The dispatcher to match against
	 * @param dispatcher[out] The EventDispatcher
	 *
	 * @return HRESULT
	 */
     public IEventDispatcher removeDispatcher( /* EventDispatcherKind */ int nKind )
     {
		int eventDispatchType = getDispatchType(nKind);
		if (eventDispatchType < 0)
		{
			eventDispatchType = EventDispatchNameKeeper.EDT_WORKSPACE_KIND;
		}
        return removeDispatcher(EventDispatchNameKeeper.dispatcherName(eventDispatchType));
     }

	/**
	 * Retrieves the EventDispatcher that matches the passed in ID.
	 *
	 * @param id[in] The ID to match against
	 * @param dispatcher[out] The EventDispatcher
	 *
	 * @return HRESULT
	 */
   public IEventDispatcher retrieveDispatcher( String id )
   {
		return m_Dispatchers.get(id);
   }

	/**
	 * Retrieves the EventDispatcher that matches the passed in enumeration.
	 *
	 * @param nKind[in] The dispatcher to match against
	 * @param dispatcher[out] The EventDispatcher
	 *
	 * @return HRESULT
	 */
     public IEventDispatcher retrieveDispatcher( /* EventDispatcherKind */ int nKind )
     {
		int eventDispatchType = getDispatchType(nKind);
		if (eventDispatchType < 0)
		{
			eventDispatchType = EventDispatchNameKeeper.EDT_WORKSPACE_KIND;
		}
		return retrieveDispatcher(EventDispatchNameKeeper.dispatcherName(eventDispatchType));
     }

	/**
	 * Returns the name keeper type.
	 * 
	 * @param nKind [in] The kind of the dispatcher to get
	 * @param nNameKeeperType [out] The namekeeper type
	 * @return true if we were able to convert between the IDL enumeration and the name keeper enumeration
	 */
	protected int getDispatchType(int nKind)
	{
		int nNameKeeperType = -1;
		if (nKind == EventDispatcherKind.EDK_WORKSPACE)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_WORKSPACE_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_DRAWINGAREA)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_DRAWINGAREA_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_PROJECTTREE)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_PROJECTTREE_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_ELEMENTMODIFIED)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_ELEMENTMODIFIED_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_EDITCTRL)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_EDITCTRL_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_PROJECTTREEDIALOGFILTER)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_PROJECTTREEDIALOGFILTER_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_ADDIN)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_ADDIN_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_MESSAGING)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_MESSAGING_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_VBA)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_VBA_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_RELATION)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_RELATION_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_ELEMENT_LIFETIME)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_ELEMENT_LIFETIME_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_CLASSIFIER)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_CLASSIFIER_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_COREPRODUCT)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_COREPRODUCT_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_PREFERENCEMANAGER)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_PREFERENCEMANAGER_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_SCM)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_SCM_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_DYNAMICS)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_DYNAMICS_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_STRUCTURE)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_STRUCTURE_KIND;
		}
		else if (nKind == EventDispatcherKind.EDK_ACTIVITIES)
		{
			nNameKeeperType = EventDispatchNameKeeper.EDT_ACTIVITIES_KIND;
		}

	   return nNameKeeperType;
	}

	/**
	 *
	 * Determines whether or not all the EventDispatchers this controller is 
	 * controlling are blocked from sending events to their listeners.
	 *
	 * @param pVal[out] The value. true if all the dispatchers are blocked.
	 *                  false if at least one dispatcher can send events.
	 *
	 * @return HRESULT
	 */
	public boolean getPreventAllEvents()
	{
		boolean pVal = true;
		Enumeration < IEventDispatcher > enumVal = m_Dispatchers.elements();
		while (enumVal.hasMoreElements())
		{
			boolean blocked = false;
			blocked = enumVal.nextElement().getPreventAllEvents();
			if (blocked == false)
			{
				pVal = false;
				break;
			}
		}
		return pVal;
	}

	/**
	 * Loops through every EventDispatcher on this controller and sets their
	 * internal PreventAllEvents flags to newVal.
	 *
	 * @param newVal[in] The value to set the flag to
	 *
	 * @return HRESULT
	 */
	public void setPreventAllEvents(boolean val)
	{
		Enumeration < IEventDispatcher > enumVal = m_Dispatchers.elements();
		while (enumVal.hasMoreElements())
		{
			enumVal.nextElement().setPreventAllEvents(val);
		}
	}
	
	/**
	 *
	 * Makes sure that an EventDispatcher with a matching ID does not exist.
	 *
	 * @param id[in] The ID to match against
	 *
	 * @return S_OK unless there is a Dispatcher with the same ID. In that
	 *         case, EDCR_E_DISPATCHER_EXISTS is returned 
	 */
	protected void validateID(String id)
	{
		
	}

	/**
	 * Push the context onto our list and all dispatchers owned by the controller.
	 *
	 * @param context[in] The EventContext to push
	 *
	 * @return HRESULT
	 */
	public void pushEventContext ( IEventContext context )
	{
		pushEventContext3( context );
	}

	/**
	 * Push the context onto our list and all dispatchers owned by the controller.
	 *
	 * @param pContext[in]
	 *
	 * @return 
	 */
	public void pushEventContext3(IEventContext pContext )
	{
		super.pushEventContext3(pContext);
		establishContext( pContext );
	}

	/**
	 * Pops the current EventContext present on each dispatcher this controller manages.
	 *
	 * @return HRESULT
	 * @warning This should only be called after previously calling PushEventContext().
	 *          Each PushEventContext() should be paired with a PopEventContext().
	 */
	public void popEventContext()
	{
	   try
	   {
		  Enumeration < IEventDispatcher > enumVal = m_Dispatchers.elements();
		  while (enumVal.hasMoreElements())
		  {
			 enumVal.nextElement().popEventContext();
		  }
		  super.popEventContext();
	   }
	   catch( Exception e)
	   {
	   }
	}

	/**
	 * Pops the current EventContext present on each dispatcher this controller manages.
	 *
	 * @return HRESULT
	 * @warning This should only be called after previously calling PushEventContext().
	 *          Each PushEventContext() should be paired with a PopEventContext().
	 */
	public IEventContext popEventContext2()
	{
		IEventContext pContext = null;
	   try
	   {
		Enumeration < IEventDispatcher > enumVal = m_Dispatchers.elements();
		while (enumVal.hasMoreElements())
		{
		   enumVal.nextElement().popEventContext2();
		}
		pContext = super.popEventContext2();
	   }
	   catch( Exception err )
	   {
	   }
	   return pContext;
	}
	
	/**
	 * Pops an event context by name from each dispatcher this controller maintains.
	 *
	 * @param name[in] The name of the Context to pop
	 *
	 * @return HRESULT
	 */
	public void removeEventContextByName( String name )
	{
	   try
	   {
		  if( name != null && name.length() > 0)
		  {
			Enumeration < IEventDispatcher > enumVal = m_Dispatchers.elements();
			while (enumVal.hasMoreElements())
			{
			   enumVal.nextElement().removeEventContextByName(name);
			}
			super.removeEventContextByName(name);
		  }
	   }
	   catch( Exception err )
	   {
	   }
	}
	
	/**
	 * Pops an event context if it contains a filter with the passed in ID from every
	 * dispatcher this controller manages.
	 *
	 * @param filterID[in] The id to match against
	 *
	 * @return HRESULT
	 */
	public void removeEventContextByFilterID( String filterID )
	{
	   try
	   {
		  if( filterID.length() > 0 )
		  {
			Enumeration < IEventDispatcher > enumVal = m_Dispatchers.elements();
			while (enumVal.hasMoreElements())
			{
			   enumVal.nextElement().removeEventContextByFilterID(filterID);
			}
			super.removeEventContextByFilterID( filterID );
		  }
	   }
	   catch( Exception err )
	   {
	   }
	}

	/**
	 * Returns how many listeners are associated with this dispatcher.
	 *
	 * @param pVal[out] The number of listeners on this dispatcher
	 *
	 * @return HRESULT
	 */
	public int getNumRegisteredSinks()
	{
	   return super.getNumRegisteredSinks();
	}

	/**
	 * Push the context onto all owned dispatchers.
	 *
	 * @param pContext[in]
	 *
	 * @return 
	 */
	protected void establishContext ( IEventContext pContext )
	{
	   try
	   {
		Enumeration < IEventDispatcher > enumVal = m_Dispatchers.elements();
		while (enumVal.hasMoreElements())
		{
		   enumVal.nextElement().pushEventContext3(pContext);
		}
	   }
	   catch( Exception err )
	   {
	   }
	}

	public static final String CLSID = "{A2C15050-7977-4896-B810-E7E1B00B51D3}";

}
