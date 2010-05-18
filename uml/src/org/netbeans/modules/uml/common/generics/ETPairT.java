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
