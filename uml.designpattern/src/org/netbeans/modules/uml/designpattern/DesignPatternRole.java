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


package org.netbeans.modules.uml.designpattern;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

public class DesignPatternRole implements IDesignPatternRole
{
	private String							m_ID = "";
	private String							m_Name = "";
	private int								m_Multiplicity = -1;
	private String							m_TypeID = "";
	private IElement 						m_Element = null;

	/**
	 *
	 */
	public DesignPatternRole()
	{
		super();
	}

	public IElement getElement()
	{
		return m_Element;
	}
	public void setElement(IElement newVal)
	{
		m_Element = newVal;
	}
	public String getTypeID()
	{
		return m_TypeID;
	}
	public void setTypeID(String newVal)
	{
		m_TypeID = newVal;
	}
	public int getMultiplicity()
	{
		return m_Multiplicity;
	}
	public void setMultiplicity(int newVal)
	{
		m_Multiplicity = newVal;
	}
	public String getName()
	{
		return m_Name;
	}
	public void setName(String newVal)
	{
		m_Name = newVal;
	}
	public String getID()
	{
		return m_ID;
	}
	public void setID(String newVal)
	{
		m_ID = newVal;
	}
	public String toString()
	{
		return getName();
	}

}
