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

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.editcontrol.IEditControl;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.NameCollisionEventSink;

/**
 * @author sumitabhk
 *
 */
public class CoreNameCollisionListener implements ICoreNameCollisionListener
{
	private NameCollisionEventSink m_EventsSink = null;
	private boolean m_bEnabled = false;
	private INameCollisionHandler m_Handler = null;

	/**
	 * 
	 */
	public CoreNameCollisionListener()
	{
		super();
		registerToSinks();
	}

	/**
	 * Returns the enabled flag for the listener
	 * 
	 * @param pVal [out,retval] VARIANT_TRUE if this listener will dispatch to its handler.
	 */
	public boolean getEnabled()
	{
		return m_bEnabled;
	}

	/**
	 * Sets the enabled flag for the listener.
	 * 
	 * @param newVal [in] VARIANT_TRUE if this listener will dispatch to its handler.
	 */
	public void setEnabled(boolean value)
	{
		if (value == false && m_Handler != null)
		{
			m_Handler.listenerDisabled();
		}
		m_bEnabled = value;
	}

	/**
	 * The handler that gets notified of collision events.
	 * 
	 * @param pVal [out,retval] The current handler installed on this listener.
	 */
	public INameCollisionHandler getHandler()
	{
		return m_Handler;
	}

	/**
	 * Sets the handler that gets notified of collision events.
	 * 
	 * @param newVal [in] The current handler installed on this listener.
	 */
	public void setHandler(INameCollisionHandler value)
	{
		m_Handler = value;
	}
	/**
	 * Registers to events
	 */
	private void registerToSinks()
	{
			// Create our event sink to listen to node/edge modifications and other drawing area events
			if (m_EventsSink == null)
			{
				m_EventsSink = new NameCollisionEventSink();
				
				m_EventsSink.setListenerToAdvise(this);
         
				// Register to project tree events
				if (m_EventsSink != null) 
				{
					DispatchHelper helper = new DispatchHelper();
					helper.registerForNamedElementEvents(m_EventsSink);
					helper.registerForInitEvents(m_EventsSink);
					helper.registerEditCtrlEvents(m_EventsSink);
				}
			}
	}	
	/**
	 * Unregisters from events and deletes the sink
	 */
	public void deInitialize()
	{
      DispatchHelper helper = new DispatchHelper();
      helper.revokeNamedElementSink(m_EventsSink);
      helper.revokeInitSink(m_EventsSink);
      helper.revokeEditCtrlSink(m_EventsSink);

      // Delete our sink
      if (m_EventsSink != null)
      {
         m_EventsSink = null;
      }
	}

	/**
	 * Fired whenever the alias name of the passed in element is about to change.
	 * This is where we ask the user to name the element if it is not yet named.
	 */
	public void onPreAliasNameModified(INamedElement element, String proposedName, IResultCell cell )
	{
		if (m_bEnabled && m_Handler != null)
		{
			m_Handler.onPreAliasNameModified(element, proposedName, cell);
		}
	}
	
	/**
	 * Event coming from our sink notifying us of this event.
	 */
	public void onPreNameCollision(INamedElement element, 
																			  String proposedName, 
																			  ETList<INamedElement> collidingElements, 
																			  IResultCell cell)
	{
		if (m_bEnabled && m_Handler != null)
		{
			m_Handler.onPreNameCollision(element, proposedName, collidingElements, cell);
		}
	}
	/**
	 * Event coming from our sink notifying us of this event.
	 */
	public void onNameCollision(INamedElement element, 
																		  ETList<INamedElement> collidingElements, 
																		  IResultCell cell)
	{
			if (m_bEnabled && m_Handler != null)
			{
				m_Handler.onNameCollision(element, collidingElements, cell);
			}
	}
	/**
	 * Event coming from our sink notifying us of this event.
	 */
	public void onCoreProductPreQuit( )
	{
		deInitialize();
	}
	/**
	 * Event coming from our sink notifying us of this event.  When the edit control goes away
	 * we automatically disable the processing of name collisions.
	 */
	public void onDeactivate(IEditControl pControl)
	{
		setEnabled(false);
	}
}



