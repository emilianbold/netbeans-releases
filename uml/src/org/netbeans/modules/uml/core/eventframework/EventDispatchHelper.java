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

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;

public class EventDispatchHelper implements IEventDispatchHelper

{
  protected IEventDispatcher m_dispatcher;

  public EventDispatchHelper()
  {
  }

  public IEventDispatcher getEventDispatcher()
  {
    return m_dispatcher;
  }

  public void setEventDispatcher( IEventDispatcher value )
  {
    m_dispatcher = value;
  }

  /**
   *
   * Creates a payload given the passed in trigger name.
   *
   * @param triggerName[in] Name of the trigger whose payload we are creating
   * @param payload[out] The created payload. Can be 0
   *
   * @return S_OK, else EFR_S_EVENTDISPATCHER_NOT_INITIALIZED if the m_Dispatcher member
   *         has not been properly initialized.
   * 
   */
	protected IEventPayload preparePayload(String triggerName)
	{
		IEventPayload payload = null;
		if (m_dispatcher != null)
		{
			payload = m_dispatcher.createPayload(triggerName);
		}
		return payload;
	}

	/**
	 *
	 * Retrieves a particular dispatcher.
	 *
	 * @param name[in]  The name of the dispatcher to retrieve
	 * @param disp[out] The found dispatcher
	 *
	 * @return HRESULT
	 *
	 */
	protected IEventDispatcher retrieveDispatcher( String name)
	{
		IEventDispatcher disp = null;
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			IEventDispatchController cont = prod.getEventDispatchController();
			if (cont != null)
			{
				disp = cont.retrieveDispatcher(name);
			}
		}
		return disp;
	}

}
