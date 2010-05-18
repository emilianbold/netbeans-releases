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


package org.netbeans.modules.uml.core.coreapplication;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public class CoreProductEventDispatcher extends EventDispatcher
										implements ICoreProductEventDispatcher
{

	private EventManager< ICoreProductInitEventsSink > m_CoreProductSink = null;
	
	public CoreProductEventDispatcher()
	{
		m_CoreProductSink = new EventManager< ICoreProductInitEventsSink >();
	}
	
	/**
	 * Registers a sink for the events specified in the ICoreProductInitEventsSink interface.
	 *
	 * @param handler [in] The actual sink that will receive notification	 
	 * 
	 */
    public void registerForInitEvents(ICoreProductInitEventsSink handler)
    {
		m_CoreProductSink.addListener(handler,null);
    }

	/**
	 * Revokes the handler identified with the passed-in cookie.
	 *
	 * @param  handler [in] 
	 */
	public void revokeInitSink(ICoreProductInitEventsSink handler)
    {
		m_CoreProductSink.removeListener(handler);
    }

	/**
	 * Fired before initialization of the product commences.
	 *
	 * @param prod [in]
	 * @param payload [in]
	 * @return proceed [out] True if the event was fully dispatched, else False if a listener cancelled full dispatch
	 */
	public boolean fireCoreProductPreInit(ICoreProduct prod, IEventPayload payload)
    {
		boolean proceed = true;
					
		if (validateEvent("CoreProductPreInit", prod))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor coreProductPreInit = new EventFunctor("org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink", 
						"onCoreProductPreInit");

         Object[] parms = new Object[2];
			parms[0] = prod;
			parms[1] = cell;
			coreProductPreInit.setParameters(parms);
			m_CoreProductSink.notifyListenersWithQualifiedProceed(coreProductPreInit);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}	
		}
		return proceed;   
    }

   /**
	* Fired after the Core product has been fully initialized.
	*
	* @param prod - ICoreProduct
	* @param payload [in] 
	*/
    public void fireCoreProductInitialized(ICoreProduct prod, IEventPayload payload)
    {    	
		if (validateEvent("CoreProductInitialized", prod))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor coreProductInitialized = new EventFunctor("org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink", 
						"onCoreProductInitialized");

         Object[] parms = new Object[2];
			parms[0] = prod;						
			parms[1] = cell;
			coreProductInitialized.setParameters(parms);
			m_CoreProductSink.notifyListeners(coreProductInitialized);
		}  
    }

   /**
	* Fired before the product quits..
	*
	* @param prod [in]
	* @param payload [in]
	*
	*/
    public void fireCoreProductPreQuit(ICoreProduct prod, IEventPayload payload)
    {
		if (validateEvent("CoreProductPreQuit", prod))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor coreProductPreQuit = new EventFunctor("org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink", 
						"onCoreProductPreQuit");

         Object[] parms = new Object[2];
			parms[0] = prod;						
			parms[1] = cell;
			coreProductPreQuit.setParameters(parms);
			m_CoreProductSink.notifyListeners(coreProductPreQuit);
		}  
    }


	public boolean fireCoreProductPreSaved(ICoreProduct prod, IEventPayload payload)
    {
		boolean proceed = true;
						
		if (validateEvent("CoreProductPreSaved", prod))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor coreProductPreSaved = new EventFunctor("org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink", 
						"onCoreProductPreSaved");

         Object[] parms = new Object[2];
			parms[0] = prod;
			parms[1] = cell;
			coreProductPreSaved.setParameters(parms);
			m_CoreProductSink.notifyListenersWithQualifiedProceed(coreProductPreSaved);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}	
		}
		return proceed;   
    }

 

    public void fireCoreProductSaved(ICoreProduct prod, IEventPayload payload)
    {
		if (validateEvent("CoreProductSaved", prod))
		{
			IResultCell cell = prepareResultCell( payload );
			EventFunctor coreProductSaved = new EventFunctor("org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink", 
						"onCoreProductSaved");

         Object[] parms = new Object[2];
			parms[0] = prod;						
			parms[1] = cell;
			coreProductSaved.setParameters(parms);
			m_CoreProductSink.notifyListeners(coreProductSaved);
		}  
    }

   /**
	* IEventDispatcher override.  Returns the number of registered sinks
	*/
	public int getNumRegisteredSinks()
	{
		return m_CoreProductSink.getNumListeners();
	}
	
}


