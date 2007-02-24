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
public class EventBlocker {

	private IEventDispatcher m_Dispatch = null;
	private IEventDispatchController m_Controller = null;
	private boolean m_OrigFlag = false;

	/**
	 *
	 */
	private EventBlocker() {
		super();
	}

	/**
	 * EventBlocker constructor. Calls the put_PreventAllEvents( true )
	 * on the passed in IEventDispatcher.
	 */ 
	public static boolean startBlocking( IEventDispatcher dispatcher ) 
	{
		//m_OrigFlag = false;
		//m_Controller = null;
		
		boolean retVal = false;
		
	   if( dispatcher != null)
	   {
			retVal = dispatcher.getPreventAllEvents();
			dispatcher.setPreventAllEvents(true);
	   }
	   
	   return retVal;
	}

	/**
	 * Prevents all event from going out on any and every dispatcher that the
	 * passed in controller manages. If the passed in controller is 0, then
	 * the CoreProduct is retrieved, and the EventDispatchController on that
	 * product is used.
	 *
	 * @param controller[in] The controller to prevent events on. If null, the
	 *                       controller on the CoreProduct is used.
	 */
	public static boolean startBlocking( IEventDispatchController controller ) 
	{
		boolean retVal = false;
		
	   ICoreProduct coreProd = null;

		IEventDispatchController curController = controller;
	   if( curController == null)
	   {
	   		coreProd = ProductRetriever.retrieveProduct();

		  	if( coreProd != null)
		  	{
				curController = coreProd.getEventDispatchController();
		  	}
	   }

	   if( curController != null)
	   {
			retVal = curController.getPreventAllEvents();
			curController.setPreventAllEvents(true);
	   }
	   
	   return retVal;
	}

	public static boolean startBlocking()
	{
		boolean retVal = false;
		
		ICoreProduct product = ProductRetriever.retrieveProduct();
		if(product != null)
		{
			retVal = startBlocking(product.getEventDispatchController());
		}
		
		return retVal;
	}

	public static void stopBlocking(boolean origFlag, IEventDispatcher dispatcher)
	{
		if(dispatcher != null)
		{
			dispatcher.setPreventAllEvents( origFlag );
		}
	}
	
	public static void stopBlocking(boolean origFlag, IEventDispatchController controller)
	{
		if(controller != null)
		{
			controller.setPreventAllEvents( origFlag );
		}			 
	}
	
	public static void stopBlocking(boolean origFlag )
	{
		ICoreProduct product = ProductRetriever.retrieveProduct();
		if(product != null)
		{
			stopBlocking(origFlag, product.getEventDispatchController());
		}
	}
}


