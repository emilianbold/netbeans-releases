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

import org.dom4j.Document;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;

//import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author sumitabhk
 *
 */
public class EventMechanism {

	private static EventMechanism m_Instance = null;
	private Document m_Mechanism = null;

	/**
	 * 
	 */
	private EventMechanism() {
		super();
	}

	/**
	 * Retrieves the single instance of the mechanism.
	 *
	 * @return The instance, else 0 if something is terribly
	 *         wrong.
	 */
	public static EventMechanism instance() {
		if( m_Instance == null )
		{
		   m_Instance = new EventMechanism();
		   m_Instance.initialize();
		}

		return m_Instance;
	}
	
	/**
	 * Loads the EventFramework.etc file into memory. The contents
	 * of this file are used by every EventDispatcher.
	 */
	private void initialize() {
		String loc = retrieveEventFileLocation();
		if( loc.length() > 0)
		{
		   m_Mechanism = XMLManip.getDOMDocument(loc);
		}
	}

	/**
	 * Retrieves the location of the EventFramework XML file
	 * that describes how to build the appropriate EventContexts.
	 *
	 * @return The location of the EventFramework file
	 */
	private String retrieveEventFileLocation() {
		String loc = null;
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		IConfigManager conMan = null;
		if (prod != null)
		{
			conMan = prod.getConfigManager();
		}
		else
		{
			conMan = new ConfigManager();
		}
		if (conMan != null)
		{
			loc = conMan.getEventFrameworkLocation();
		}
		return loc;
	}

	/**
	 *
	 * Provides direct access to the actual dom document representing
	 * the event framework etc file.
	 *
	 * @param doc[out] The document
	 *
	 * @return HRESULT
	 *
	 */
	public Document mechanism() {
		return m_Mechanism;
	}

	/**
	 * If the EventMechanism instance has been created, this will return that instance.
	 * If it has not been created, null is returned.
	 *
	 * @return The mechanism, else 0.
	 */
	public static EventMechanism exists()
	{
	   return m_Instance;
	}
}


