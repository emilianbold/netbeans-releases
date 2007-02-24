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


package org.netbeans.modules.uml.designpattern;

import java.util.ArrayList;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public class DesignPatternEventDispatcher extends EventDispatcher implements IDesignPatternEventDispatcher
{
	/** Handles the actual deployment of events to Preference listeners. */
	private EventManager< IDesignPatternEventsSink > m_DesignPatternEventManager = null;

	private EventFunctor m_PreApplyFunc = null;
	private EventFunctor m_PostApplyFunc = null;

	/**
	 *
	 */
	public DesignPatternEventDispatcher()
	{
		super();
		m_DesignPatternEventManager = new EventManager< IDesignPatternEventsSink >();
	}
	/**
	 * Description
	 *
	 * @param pHandler[in]
	 * @param cookie[out]
	 */
	public void registerDesignPatternEvents(IDesignPatternEventsSink pHandler)
	{
		m_DesignPatternEventManager.addListener(pHandler, null);
	}

	/**
	 * Description
	 *
	 * @param pHandler[in]
	 * @param cookie[out]
	 */
	public void revokeDesignPatternSink(IDesignPatternEventsSink pHandler)
	{
		m_DesignPatternEventManager.removeListener(pHandler);
	}
	/**
	 * Fired before a pattern is applied
	 *
	 * @param pDetails[in]	The details that apply to the pattern being applied
	 * @param payload[in]
	 *
	 * @return HRESULT
	 */
	public void firePreApply(IDesignPatternDetails pDetails, IEventPayload payLoad)
	{
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pDetails);

		if (validateEvent("PreApply", collection))
		{
		   IResultCell cell = prepareResultCell(payLoad);
		   if (m_PreApplyFunc == null)
		   {
				m_PreApplyFunc = new EventFunctor("org.netbeans.modules.uml.ui.products.ad.addesigncentergui.designpatternaddin.IDesignPatternEventsSink",
														"onPreApply");
		   }
		   Object[] parms = new Object[2];
		   parms[0] = pDetails;
		   parms[1] = cell;
		   m_PreApplyFunc.setParameters(parms);
		   m_DesignPatternEventManager.notifyListenersWithQualifiedProceed(m_PreApplyFunc);
		}
	}
	/**
	 * Fired after a pattern is applied
	 *
	 * @param pDetails[in]	The details that apply to the pattern was applied
	 * @param payload[in]
	 *
	 * @return HRESULT
	 */
	public void firePostApply(IDesignPatternDetails pDetails, IEventPayload payLoad)
	{
		ArrayList < Object > collection = new ArrayList < Object >();
		collection.add(pDetails);

		if (validateEvent("PostApply", collection))
		{
		   IResultCell cell = prepareResultCell(payLoad);
		   if (m_PostApplyFunc == null)
		   {
				m_PostApplyFunc = new EventFunctor("org.netbeans.modules.uml.ui.products.ad.addesigncentergui.designpatternaddin.IDesignPatternEventsSink",
														"onPostApply");
		   }
		   Object[] parms = new Object[2];
		   parms[0] = pDetails;
		   parms[1] = cell;
		   m_PostApplyFunc.setParameters(parms);
		   m_DesignPatternEventManager.notifyListenersWithQualifiedProceed(m_PostApplyFunc);
		}
	}
}
