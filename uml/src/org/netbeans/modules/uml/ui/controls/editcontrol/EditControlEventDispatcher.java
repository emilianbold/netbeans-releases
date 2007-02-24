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



package org.netbeans.modules.uml.ui.controls.editcontrol;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 * @author sumitabhk
 *
 */
public class EditControlEventDispatcher extends EventDispatcher implements IEditControlEventDispatcher
{
	EventManager<IEditControlEventSink> m_EventManager = null;

	/**
	 *
	 */
	public EditControlEventDispatcher()
	{
		super();
		m_EventManager = new EventManager<IEditControlEventSink>();
	}

	/* 
	 * Registers a listener for the edit control events.
	 */
	public void registerEditCtrlEvents(IEditControlEventSink handler)
	{
		m_EventManager.addListener(handler, null);
	}

	/* 
	 * Removes the listener for edit control events.
	 */
	public void revokeEditCtrlSink(IEditControlEventSink handler)
	{
		m_EventManager.removeListener(handler);
	}

	/**
	 *
	 * Create a new EditEventPayload for an edit control event
	 *
	 * @param rtCell[out] The new IEditEventPayload.
	 *
	 * @return HRESULT
	 *
	 */
	public IEditEventPayload createEventPayload()
	{
		IEditEventPayload payload = new EditEventPayload();
		return payload;
	}

	/* 
	 * nothing to do right now
	 */
	public boolean firePreInvalidData(String ErrorData, IEditEventPayload payload)
	{
		return true;
	}

	/* 
	 * nothing to do right now
	 */
	public void fireInvalidData(String ErrorData, IEditEventPayload payload)
	{
		
	}

	/* 
	 * nothing to do right now
	 */
	public boolean firePreOverstrike(boolean bOverstrike, IEditEventPayload payload)
	{
		return true;
	}

	/* 
	 * nothing to do right now
	 */
	public void fireOverstrike(boolean bOverstrike, IEditEventPayload payload)
	{
		
	}

	/* 
	 * Fired before the edit control gets activated.
	 */
	public boolean firePreActivate(IEditControl pControl, IEditEventPayload payload)
	{
		boolean proceed = true;
		if (validateEvent("PreActivate", pControl))
		{
		   IResultCell cell = prepareResultCell(payload);
		   EventFunctor preActiveFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink", 
												 "onPreActivate");
		   
		   Object[] parms = new Object[2];
		   parms[0] = pControl;
		   parms[1] = cell;
		   preActiveFunc.setParameters(parms);
		   m_EventManager.notifyListenersWithQualifiedProceed(preActiveFunc);
		   proceed = cell.canContinue();
		}
		
		return proceed;
	}

	/* 
	 * Fired after the edit control gets activated.
	 */
	public void fireActivate(IEditControl pControl, IEditEventPayload payload)
	{
		if (validateEvent("Activate", pControl))
		{
		   IResultCell cell = prepareResultCell(payload);
		   EventFunctor activeFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink", 
												 "onActivate");
		   
		   Object[] parms = new Object[2];
		   parms[0] = pControl;
		   parms[1] = cell;
		   activeFunc.setParameters(parms);
		   m_EventManager.notifyListeners(activeFunc);
		}
	}

	/* 
	 * Fired after the edit control gets de-activated.
	 */
	public void fireDeactivate(IEditControl pControl, IEditEventPayload payload)
	{
		if (validateEvent("Deactivate", pControl))
		{
		   IResultCell cell = prepareResultCell(payload);
		   EventFunctor deactiveFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink", 
												 "onDeactivate");
		   
		   Object[] parms = new Object[2];
		   parms[0] = pControl;
		   parms[1] = cell;
		   deactiveFunc.setParameters(parms);
		   m_EventManager.notifyListeners(deactiveFunc);
		}
	}

	/* 
	 * Fired before the edit control gets committed.
	 */
	public boolean firePreCommit(IEditEventPayload payload)
	{
		boolean proceed = true;
		if (validateEvent("PreCommit", null))
		{
		   IResultCell cell = prepareResultCell(payload);
		   EventFunctor preComitFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink", 
												 "onPreCommit");
		   
		   Object[] parms = new Object[1];
		   parms[0] = cell;
		   preComitFunc.setParameters(parms);
		   m_EventManager.notifyListenersWithQualifiedProceed(preComitFunc);
		   proceed = cell.canContinue();
		}
		
		return proceed;
	}

	/* 
	 * Fired after the edit control gets commited.
	 */
	public void firePostCommit(IEditEventPayload payload)
	{
		if (validateEvent("PostCommit", null))
		{
		   IResultCell cell = prepareResultCell(payload);
		   EventFunctor postComitFunc = new EventFunctor("org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventSink", 
												 "onPostCommit");
		   
		   Object[] parms = new Object[1];
		   parms[0] = cell;
		   postComitFunc.setParameters(parms);
		   m_EventManager.notifyListenersWithQualifiedProceed(postComitFunc);
		}
	}

	public int getNumRegisteredSinks()
	{
		return m_EventManager.getNumListeners();
	}

}



