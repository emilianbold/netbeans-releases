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


