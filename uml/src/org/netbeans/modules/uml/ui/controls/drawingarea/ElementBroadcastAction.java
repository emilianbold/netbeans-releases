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


package org.netbeans.modules.uml.ui.controls.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class ElementBroadcastAction implements IElementBroadcastAction
{
	int m_Kind = -1;
	ETList<IElement> m_ModelElements = null;

	/**
	 * Returns the kind of action to be performed
	 *
	 * @param pVal [out,retval] The kind of action
	 */
    public int getKind()
    {
    	return m_Kind;
    }

	/**
	 * Sets the kind of action to be performed
	 *
	 * @param newVal [in] The kind of this action
	 */
	 public void setKind(int kind)
    {
    	m_Kind = kind;
    }

	/**
	 * Adds a model element that is to be used with this action
	 *
	 * @param pElement [in] Adds a model element to this actions list
	 */
    public void add(IElement element)
    {
    	if (m_ModelElements == null)
    	{
    		m_ModelElements = new ETArrayList<IElement>();
    	}
    	if (m_ModelElements != null)
    	{
    		m_ModelElements.add(element);
    	}
    }

	/**
	 * Adds model elements that are to be used with this action
	 *
	 * @param pElements [in] Adds a model element(s) to this actions list
	 */
    public void add(ETList<IElement> elements)
    {
		if (m_ModelElements == null)
		{
			m_ModelElements = new ETArrayList<IElement>();
		}
		if (m_ModelElements != null)
		{
			m_ModelElements.addThese(elements);
		}
    }

    public ETList<IElement> getModelElements()
    {
    	return m_ModelElements;
    }
}