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



