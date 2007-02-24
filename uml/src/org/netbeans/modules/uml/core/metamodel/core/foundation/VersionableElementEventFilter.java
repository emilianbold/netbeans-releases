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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import java.util.Vector;

import org.netbeans.modules.uml.core.eventframework.EventFilter;

/**
 * @author sumitabhk
 *
 */
public class VersionableElementEventFilter extends EventFilter implements IVersionableElementEventFilter{

	private IVersionableElement m_Element = null;
	private String m_ID = null;

	/**
	 *
	 */
	public VersionableElementEventFilter() {
		super();
	}

	/**
	 *
	 * Retrieves the element this filter is filtering on.
	 *
	 * @param pVal[out] The element this filter is filtering on.
	 *
	 * @return HREUSLT
	 *
	 */
	public IVersionableElement getVersionableElement() {
		return m_Element;
	}

	/**
	 *
	 * Sets the element on this filter. Doing so also sets this filter's ID based on the XMI ID
	 * of the passed in element.
	 *
	 * @param newVal[in] The element to filter on.
	 *
	 * @return HRESULT
	 *
	 */
	public void setVersionableElement(IVersionableElement value) {
		if (value != null)
		{
			String xmiid = value.getXMIID();
			setFilterID(xmiid);
		}
		m_Element = value;
	}

	/**
	 *
	 * Validates the event that is about to be dispatched.
	 *
	 * @param triggerName[in] The name of the trigger.
	 * @param payLoad[in] The payload about to be delivered
	 * @param valid[out] true if the event is ok to be dispatched,
	 *                   else false to prevent the event
	 *
	 * @return HRESULT
	 *
	 */
	public boolean validateEvent(String triggerName, Object payload)
	{
		boolean valid = true;
		if (payload instanceof Vector)
		{
			Vector coll = (Vector)payload;
			int size = (coll).size();
			if (size > 0)
			{
				Object obj = coll.get(0);
				valid = validateEvent(obj);
			}
		}
		else if (payload instanceof IVersionableElement)
		{
			valid = validateEvent(payload);
		}
		
		return valid;
	}

	/**
	 *
	 * Performs the event validation. If the element passed in matches our internal element,
	 * the event is denied.
	 *
	 * @param element[in] The element to match against
	 * @param isValid[out] true if event dispatch is a go, else false to deny.
	 *
	 * @return HRESULT
	 *
	 */
	public boolean validateEvent(Object element)
	{
		boolean valid = true;
		if (element instanceof IVersionableElement)
		{
			IVersionableElement ver = (IVersionableElement)element;
			boolean isSame = false;
			isSame = ver.isSame(m_Element);
			if (isSame)
			{
				valid = false;
			}
		}
		return valid;
	}


	/**
	 *
	 * Retrieves the filter ID
	 *
	 * @param pID[out] The ID of this filter.
	 *
	 * @return HRESULT
	 *
	 */
	public String getFilterID()
	{
		return m_ID;
	}
	
	/**
	 *
	 * Sets the ID for this filter. This ID should be unique.
	 *
	 * @param pID[in] The new id.
	 *
	 * @return HRESULT
	 *
	 */
	public void setFilterID(String id)
	{
		m_ID = id;
	}
	
}



