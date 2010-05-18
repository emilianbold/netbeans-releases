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


