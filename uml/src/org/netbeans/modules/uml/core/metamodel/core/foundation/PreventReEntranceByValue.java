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


