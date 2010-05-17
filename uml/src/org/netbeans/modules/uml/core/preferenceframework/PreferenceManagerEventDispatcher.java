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

package org.netbeans.modules.uml.core.preferenceframework;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import java.util.ArrayList;

/**
 * @author sumitabhk
 *
 */
public class PreferenceManagerEventDispatcher extends EventDispatcher implements IPreferenceManagerEventDispatcher
{
	/** Handles the actual deployment of events to Preference listeners. */
	private EventManager< IPreferenceManagerEventsSink > m_PreferenceEventManager = null;

	/**
	 * 
	 */
	public PreferenceManagerEventDispatcher() 
	{
		super();
      
      m_PreferenceEventManager = new EventManager< IPreferenceManagerEventsSink >();
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventDispatcher#registerPreferenceManagerEvents(org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink)
    */
   public void registerPreferenceManagerEvents(IPreferenceManagerEventsSink pHandler)
   {
      m_PreferenceEventManager.addListener(pHandler, null);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventDispatcher#revokePreferenceManagerSink(org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink)
    */
   public void revokePreferenceManagerSink(IPreferenceManagerEventsSink pHandler)
   {
	m_PreferenceEventManager.removeListener(pHandler);
   }

   /**
	* Fired when a preference value changes.
	*
	* @param name[in]		The name of the preference
	* @param pElement[in]	The property element representing the preference
	* @param payload[in]	
	*
	* @return HRESULT
	*/
   public void firePreferenceChange(String name, 
   									IPropertyElement pElement, 
   									IEventPayload payLoad)
   {
	ArrayList < Object > collection = new ArrayList < Object >();
	collection.add(name);
	collection.add(pElement);

	if (validateEvent("PreferenceChange", collection))
	{
	  IResultCell cell = prepareResultCell(payLoad);
	   EventFunctor preferenceChangeFunc = new EventFunctor("org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink", 
													"onPreferenceChange");
	   
	   Object[] parms = new Object[3];
	   parms[0] = name;
	   parms[1] = pElement;
	   parms[2] = cell;
	   preferenceChangeFunc.setParameters(parms);
	   m_PreferenceEventManager.notifyListenersWithQualifiedProceed(preferenceChangeFunc);
	}      
   }

   /**
	* Fired when a preference is added.
	*
	* @param name[in]		The name of the preference
	* @param pElement[in]	The property element representing the preference
	* @param payload[in]	
	*
	* @return HRESULT
	*/
   public void firePreferenceAdd(String name, 
   								 IPropertyElement pElement, 
   								 IEventPayload payLoad)
   {
	// Collect the additional parameters for the EventContext to use
	// during the validation pass of the trigger
	ArrayList < Object > collection = new ArrayList < Object >();
	collection.add(name);
	collection.add(pElement);

	if (validateEvent("PreferenceAdd", collection))
	{
	  IResultCell cell = prepareResultCell(payLoad);
	   EventFunctor preferenceAddFunc = new EventFunctor("org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink", 
													"onPreferenceAdd");
	   
	   Object[] parms = new Object[3];
	   parms[0] = name;
	   parms[1] = pElement;
	   parms[2] = cell;
	   preferenceAddFunc.setParameters(parms);
	   m_PreferenceEventManager.notifyListenersWithQualifiedProceed(preferenceAddFunc);
	}      
   }

   /**
	* Fired when a preference is deleted.
	*
	* @param name[in]		The name of the preference
	* @param pElement[in]	The property element representing the preference
	* @param payload[in]	
	*
	* @return HRESULT
	*/
   public void firePreferenceRemove(String name, 
   									IPropertyElement pElement, 
   									IEventPayload payLoad)
   {
	// Collect the additional parameters for the EventContext to use
	// during the validation pass of the trigger
	ArrayList < Object > collection = new ArrayList < Object >();
	collection.add(name);
	collection.add(pElement);

	if (validateEvent("PreferenceRemove", collection))
	{
	  IResultCell cell = prepareResultCell(payLoad);
	   EventFunctor preferenceRemoveFunc = new EventFunctor("org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink", 
													"onPreferenceRemove");
	   
	   Object[] parms = new Object[3];
	   parms[0] = name;
	   parms[1] = pElement;
	   parms[2] = cell;
	   preferenceRemoveFunc.setParameters(parms);
	   m_PreferenceEventManager.notifyListenersWithQualifiedProceed(preferenceRemoveFunc);
	}      
   }

   /**
	* Fired when any preference value changes.  This method groups all of the changes together in a "batch"
	* event.  The changes to the individual preferences also fired.
	*
	* @param pElements[in]	The property elements representing the preferences that have changed
	* @param payload[in]	
	*
	* @return HRESULT
	*/
   public void firePreferencesChange(IPropertyElement[] pElements, 
   									 IEventPayload payLoad)
   {
	// Collect the additional parameters for the EventContext to use
	// during the validation pass of the trigger
	ArrayList < Object > collection = new ArrayList < Object >();
	collection.add(pElements);

	if (validateEvent("PreferencesChange", collection))
	{
	  IResultCell cell = prepareResultCell(payLoad);
	   EventFunctor preferencesChangeFunc = new EventFunctor("org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink", 
													"onPreferencesChange");
	   
	   Object[] parms = new Object[2];
	   parms[0] = pElements;
	   parms[1] = cell;
	   preferencesChangeFunc.setParameters(parms);
	   m_PreferenceEventManager.notifyListenersWithQualifiedProceed(preferencesChangeFunc);
	}      
   }

	
}


