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

/**
 * @author sumitabhk
 *
 */
public class EventDispatchRetriever {

	private static EventDispatchRetriever m_Instance = null;
	private IEventDispatchController m_Controller = null;

	public static EventDispatchRetriever instance()
	{
		if (m_Instance == null)
		{
			m_Instance = new EventDispatchRetriever();
		}
		return m_Instance;
	}

	private EventDispatchRetriever() {
		super();
	}

	/**
	 * Simple constructor that can handle the setting of the internal
	 * product used for dispatch retrieval.
	 *
	 * @param prod[in] The product to use for dispatch retrieval. Can
	 *                 be 0. If 0 and GetDispatcher() is called, the
	 *                 current product on the ROT for the current process
	 *                 will be used.
	 */

	public EventDispatchRetriever( ICoreProduct prod )
	{
	   setController( prod );
	}

	/**
	 *
	 * Establishes the dispatch controller on this object
	 *
	 * @param controller[in] The controller to use when retrieving
	 *                       dispatchers.
	 *
	 */
   
	public EventDispatchRetriever( IEventDispatchController controller )
	{
	   m_Controller = controller;
	}

	/**
	 * Retrieves the dispatcher indicated by dispName
	 *
	 * @param dispName[in] The name of the dispatcher to retrieve
	 * @param type[out] The actual dispatcher, else 0 if not found
	 *
	 * @result HRESULT
	 */

	public <T extends IEventDispatcher> T getDispatcher( String dispName)
	{
		IEventDispatcher retObj = null; 
		// If our internal product is not set, pull the product
		// off the ROT
		if (m_Controller == null)
		{
			ICoreProduct prod = ProductRetriever.retrieveProduct();
			if (prod != null)
			{
				m_Controller = prod.getEventDispatchController();
			}
		}
		if (m_Controller != null)
		{
			retObj = m_Controller.retrieveDispatcher(dispName);
		}
		return (T)retObj;
	}

	/**
	 *
	 * Sets a product on this retriever. The retriever will now use
	 * that product when retrieving dispatchers.
	 *
	 * @param prod[in] The product to retrieve dispatchers from.
	 *
	 * @return HRESULT
	 *
	 */
	public void setController( IEventDispatchController cont ) 
	{ m_Controller = cont; }
	
	public void setController( ICoreProduct prod )
	{
	   if( prod != null)
	   {
		  m_Controller = prod.getEventDispatchController();
	   }
	}

	/**
	 *
	 * Retrieves controller that this retriever is pulling dispatchers from. 
	 *
	 * @param cont[out] The controller.
	 *
	 * @return HRESULT
	 *
	 */

	public IEventDispatchController getController() 
	{
	   return m_Controller;
	}

        // hack to know if RequestProcessor has established its processors
        private static boolean firstRequest = false;
        public static boolean isFirstRequest() {
            if (firstRequest) {
                firstRequest = false;
            }
            return firstRequest;
        }
}


