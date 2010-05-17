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
