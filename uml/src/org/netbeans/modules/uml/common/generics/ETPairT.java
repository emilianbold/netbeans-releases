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

package org.netbeans.modules.uml.common.generics;

public class ETPairT<Type, TypeTwo>
{
	public ETPairT()
	{
		m_paramOne = null;
		m_paramTwo = null;
	}

	public ETPairT(Type paramOne, TypeTwo paramTwo)
	{
		m_paramOne = paramOne;
		m_paramTwo = paramTwo;
	}
	
	public Type getParamOne()
	{
		 return m_paramOne;
	}
	
	public void setParamOne(Type paramOne)
	{
		m_paramOne = paramOne;
	}

	public TypeTwo getParamTwo()
	{
		return m_paramTwo;
	}

	public void setParamTwo(TypeTwo paramTwo)
	{
		m_paramTwo = paramTwo;
	}
	
	public boolean equals(Object other)
	{
		if (other == null) return false;
		if (!(other instanceof ETPairT))
			return false;
		ETPairT<Type,TypeTwo> o = (ETPairT) other;
		return 
			(getParamOne() != null? getParamOne().equals(o.getParamOne()) : 
								  o.getParamOne() == null) &&
			(getParamTwo() != null? getParamTwo().equals(o.getParamTwo()) : 
							  o.getParamTwo() == null);
	}
    
	/**
	 * Returns a <code>String</code> representation of the first 
	 * parameter of this <code>ETPairT</code>.
	 * 
	 * @return The <code>toString()</code> of the first parameter if
	 * 		   it is non-<code>null</code>, else the empty string 
	 * 		   <code>""</code>.
	 */
	public String toString()
	{
		return m_paramOne != null? m_paramOne.toString() : "";
	}
	
	protected Type m_paramOne;
	protected TypeTwo m_paramTwo;
}
