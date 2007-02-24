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

import java.util.Vector;

public class WizardRoleObject
{
	private String							m_ChosenID = "";
	private String							m_ChosenName = "";
	private IDesignPatternRole				m_Role = null;
	private Vector<WizardRoleObject>		m_Children = new Vector<WizardRoleObject>();

	/**
	 *
	 */
	public WizardRoleObject(String name, IDesignPatternRole pRole)
	{
		super();
		m_ChosenName = name;
		m_Role = pRole;
	}

	public IDesignPatternRole getRole()
	{
		return m_Role;
	}
	public void setRole(IDesignPatternRole newVal)
	{
		m_Role = newVal;
	}
	public String getChosenID()
	{
		return m_ChosenID;
	}
	public void setChosenID(String newVal)
	{
		m_ChosenID = newVal;
	}
	public String getChosenName()
	{
		return m_ChosenName;
	}
	public void setChosenName(String newVal)
	{
		m_ChosenName = newVal;
	}
	public String toString()
	{
		String name = "";
		if (m_Role != null)
		{
			name = m_Role.getName();
		}
		return name;
	}
	public Vector<WizardRoleObject> getChildren()
	{
		return m_Children;
	}
	public int getChildrenCount()
	{
		return m_Children.size();

	}
	public void addChild(WizardRoleObject obj)
	{
		m_Children.add(obj);
	}
	public void removeChild(int pos)
	{
		m_Children.remove(pos);
	}
}
