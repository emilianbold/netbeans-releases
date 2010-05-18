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



package org.netbeans.modules.uml.core.metamodel.core.foundation;

import java.util.Hashtable;

import org.netbeans.modules.uml.core.support.umlsupport.PreventReEntrance;

/**
 * @author sumitabhk
 *
 */
public class PreventReEntranceByValue extends PreventReEntrance
{
	protected String m_CurrentValue = "";
	protected String m_ProjId = "";
	//Hashtable<String, Hashtable<String, int>> m_Block = null;
	protected static Hashtable m_Block = new Hashtable();
	
	public PreventReEntranceByValue(String value, String projectId, int flag)
	{
		//super(flag);
      
      super();
      this.startBlocking(flag);
		m_CurrentValue = value;
		m_ProjId = projectId;
		
		Object obj = m_Block.get(value);
		if (obj == null)
		{
			Hashtable newTable = new Hashtable();
			newTable.put(m_ProjId, new Integer(1));
			m_Block.put(value, newTable);
		}
		else
		{
			Hashtable newTable = (Hashtable)obj;
			Object obj2 = newTable.get(projectId);
			if (obj2 == null)
			{
				newTable.put(m_ProjId, new Integer(1));
				m_Block.put(value, newTable);
			}
			else
			{
            Integer wrapper = (Integer)obj2;
				int val = wrapper.intValue();
            
            newTable.remove(projectId);
            newTable.put(m_ProjId, new Integer(++val));
			}
		}
	}

	/**
	 *
	 * Checks to see if the current value is in a blocking state
	 *
	 * @return true if blocking, else false
	 *
	 */
	public boolean isBlocking()
	{
		boolean isBlocked = false;
		if (m_Block != null)
		{
			Object obj = m_Block.get(m_CurrentValue);
			if (obj != null)
			{
				Hashtable newTable = (Hashtable)obj;
				Object obj2 = newTable.get(m_ProjId);
				if (obj2 != null)
				{
					int val = ((Integer)obj2).intValue();
					isBlocked = val > 1 ? true : false;
				}
			}
		}
		return isBlocked;
	}
	
	/**
	 *
	 * Removes the value off the reentrance map if the stack
	 * has completely unwound.
	 *
	 */
	public int releaseBlock()
	{
      int retVal = 0;
		if (m_CurrentValue.length() > 0 && m_ProjId.length() > 0)
		{
			Object obj = m_Block.get(m_CurrentValue);
			if (obj != null)
			{
				Hashtable newTable = (Hashtable)obj;
				Object obj2 = newTable.get(m_ProjId);
				if (obj2 != null)
				{
					retVal = ((Integer)obj2).intValue();
					retVal--;
					if (retVal == 0)
					{
						newTable.remove(m_ProjId);
						if (newTable.isEmpty())
						{
							m_Block.remove(m_CurrentValue);
						}
					}
				}
			}
		}
      
      return retVal;
	}
	
}


