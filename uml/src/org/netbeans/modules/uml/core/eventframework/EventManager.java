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

import org.netbeans.modules.uml.common.ETSystem;

import java.util.Vector;
import org.openide.ErrorManager;

/**
 * EventManager is a simple base class used to manage
 * the collection of lister interfaces. The type used to instanciate
 * this class should be the event listener interface, such as IWorkspaceEventsSink.
 */
public class EventManager<Element>
{
	private Vector<Object> m_listeners = new Vector<Object>();
	private IEventDispatcher m_dispatcher = null;
	private IValidationSink m_Validator = null;
	
	public void addListener(Element obj, IValidationSink<Element> sink)
	{
            // conover - duplicate listeners were being registered, and this
            // will prevent that from happening
            if (!m_listeners.contains(obj))
            {
		m_listeners.addElement(obj);
    
                if (sink != null)
                    setValidator(sink);
            }
            
            // log instances when there is an attempt to register
            // a duplicate listener
            else
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, 
                    "Duplicate listener registration was attempted with " 
                    + obj.toString() ); // NOI18N
        }
	
	public void removeListener(Element sink)
	{
		m_listeners.removeElement(sink);
	}
	
	public void setDispatcher(IEventDispatcher disp)
	{
		m_dispatcher = disp;
	}
	
	public boolean anyListeners()
	{
		return m_listeners.size() > 0;
	}
	
	public int getNumListeners()
	{
		return m_listeners.size();
	}
	
	public void notifyListeners(EventFunctor func)
	{
		if (func == null)
			return;
		//
		// Switched the loop to go backwards because in onCoreProductPreQuit
		// the listeners could be removed in the execute and that was causing
		// the array to be messed up.
		// In c++, this function takes a boolean as a parameter, but it is only
		// set to true in WorkspaceEventDispatcherImpl::FireWSProjectPreSave
		// so I am not sure how this isn't causing problems in c++ as well
		//
		//for (int i = 0; i < m_listeners.size(); i++)
		for (int i = m_listeners.size() - 1; i >= 0 ; i--)
		{
			try
			{
				//if (validateSink(obj);
				func.execute(m_listeners.elementAt(i));
			}
			
			catch (Exception e)
			{
				ETSystem.out.println("Error in notifyListeners");
			}
		}
	}
	
	public void notifyListeners(EventFunctor func, Object[] params)
	{
		if (func == null)
			return;
		
		// See above for comment
		//for (int i = 0; i < m_listeners.size(); i++)
		for (int i = m_listeners.size() - 1; i >= 0 ; i--)
		{
			try
			{
				//validateSink(obj);
				func.execute(params, m_listeners.elementAt(i));
			}
			
			catch (Exception e)
			{
				ETSystem.out.println("Error in notifyListeners");
			}
		}
	}
	
	public void notifyListenersWithQualifiedProceed(EventFunctor func)
	{
		// cvc - 6269224
		// loop in reverse because an element might get removed outside of
		// this instance and cause some listeners to be skipped
		// see other notifier methods in this class for exact same comments
		// as this was a problem in the past but this loop wasn't touched
		// for (int i = 0; i < m_listeners.size(); i++)
		for (int i = m_listeners.size()-1; i > -1 ; i--)
		{
			try
			{
				//validateSink(obj);
				func.execute(m_listeners.elementAt(i));
				//check the result and if required call dispatchCancelEvent
				
				if (!func.isResultOK())
				{
					//DispatchCancelledEvent( i, event );
					break;
				}
			}
			
			catch (Exception e)
			{
				ETSystem.out.println(
					"Exception in notifyListenersWithQualifiedProceed"); // NOI18N
			}
		}
	}
	
	/**
	 * @return
	 */
	public IValidationSink getValidator()
	{
		return m_Validator;
	}
	
	/**
	 * @param sink
	 */
	public void setValidator(IValidationSink sink)
	{
		m_Validator = sink;
	}
}
