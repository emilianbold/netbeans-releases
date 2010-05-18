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


