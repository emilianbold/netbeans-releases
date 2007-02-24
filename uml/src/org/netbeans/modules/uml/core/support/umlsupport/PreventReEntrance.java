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


package org.netbeans.modules.uml.core.support.umlsupport;

/**
 * Used to avoid unwanted function recursion. General this object is
 * used by sending in the address of a member or static variable of the object
 * or function.
 *
 * @author sumitabhk
 */
public class PreventReEntrance
{
	private int m_Blocking = 0;
	public PreventReEntrance()
	{
		
	}

   /**
    * Ups the count on the passed in integer.
    *
    * @param flag[in] The integer that will be automatically managed
    */
   public int startBlocking(int flag)
   {
      if (flag >= 0)
		{
			flag++;
			m_Blocking = flag;
		}
		else
		{
			m_Blocking = 0;
		}
      
      return flag;
   }
   
	/** If the internal flag is greater than one, we are blocking */
	public boolean isBlocking()
	{
		return m_Blocking > 1 ? true : false;
	}
	
	public int releaseBlock()
	{
		if (m_Blocking > 0)
		{
			m_Blocking--;
		}
      
      return m_Blocking;
	}
}


