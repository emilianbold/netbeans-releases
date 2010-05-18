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
