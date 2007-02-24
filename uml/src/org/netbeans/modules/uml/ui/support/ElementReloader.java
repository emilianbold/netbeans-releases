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



package org.netbeans.modules.uml.ui.support;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;

/**
 * @author sumitabhk
 *
 */
public class ElementReloader
{

	/**
	 *
	 */
	public ElementReloader()
	{
		super();
	}
	
	/**
	 * Reloads an element given only the XMI id.
	 */
	public IElement getElement(String xmiid)
	{
		if (xmiid != null && xmiid.length() > 0)
		{
			IElementLocator locator = new ElementLocator();
			return locator.findElementByID(xmiid);
		}
		return null;
	}

	/**
	 * Reloads an element given the XMI id and the project (toplevel) XMIID.
	 *
	 * @param elementTopLevelXMIID [in] The toplevel id of the model element to reload (ie IProject XMIID)
	 * @param elementXMIID [in] The XMIID of the element to reload
	 * @param pElement [out,retval] The reloaded element, or NULL
	 */
	public IElement getElement(String topLevelID, String xmiid)
	{
		// when the top level XMI ID is blank, IElementLocator calls FindElementByID2() internally
		if (xmiid != null && xmiid.length() > 0)
		{
			IElementLocator locator = new ElementLocator();
			return locator.findElementByID(topLevelID, xmiid);
		}
		return null;
	}
}



