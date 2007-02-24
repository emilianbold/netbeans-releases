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

/**
 * @author sumitabhk
 */
public class PreventElementReEntrance {

	//hashtable of <BaseElement, int>
	private static Hashtable< Object, Integer > m_Block = new Hashtable< Object, Integer >();
	private Object m_curElement = null;
	private int m_curVal = 0;
	
	public PreventElementReEntrance(Object elem)
	{
		m_curElement = elem;

		Integer obj = m_Block.get(elem);
		if (obj == null)
		{
			m_Block.put(elem, new Integer(1));
			m_curVal = 1;
		}
		else
		{
			int i = obj.intValue();
			i++;
			if (m_curVal == 0)
			{
				m_curVal = i;
			}
			else
			{
				m_curVal++;
			}
			//m_Block.remove(elem);
			//m_Block.put(elem, new Integer(i));
		}
	}

	//checks and tells if the passed element is being blocked.
	public boolean isBlocking()
	{
		Integer obj = m_Block.get(m_curElement);
        return obj != null && m_curVal > 1;
	}
	
	//puts the passed in element for blocking
//    synchronized public static void block(Object elem)
//	{
//		Integer obj = m_Block.get(elem);
//		if (obj == null)
//		{
//			m_Block.put(elem, new Integer(1));
//		}
//		else
//		{
//			int i = obj.intValue();
//			i++;
//			//m_Block.remove(elem);
//			//m_Block.put(elem, new Integer(i));
//		}
//	}
	
	//releases the passed in element from blocking
    public void releaseBlock()
	{
		Integer obj = m_Block.get(m_curElement);
		if (obj != null)
		{
			int i = obj.intValue();
			i--;
			m_curVal--;
			if (m_curVal == 0)
			{
				m_Block.remove(m_curElement);
			}
			else
			{
				//m_Block.put(m_curElement, new Integer(i));
			}
		}
	}
}