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

import java.util.Iterator;



/*
 * @author KevinM
 *
 */
public class IteratorT < T > extends java.lang.Object implements Iterator {
	
	/*
	 *
	 * @author KevinM
	 *
	 * Inner class is used for performance when someone passes a null list or existing iter.
	 */
	protected class NullIteratorImpl implements Iterator
	{
		public boolean hasNext() {
			return false;
		}
		
		public Object next()
		{
			return null;
		}
		
		public void remove()
		{
			// no op.
		}
	};
	
	public IteratorT(Iterator iter){
		m_iter = iter != null ? iter : new NullIteratorImpl();
	}


	public IteratorT(java.util.List list) {
		if (list != null)
		{
			m_iter = list.iterator();
			if (m_iter == null)
				m_iter = new NullIteratorImpl();	
		}
		else
			m_iter = new NullIteratorImpl();
	}

	public T next() 
	{
		return m_iter.hasNext() ? (T)m_iter.next() : null;
	}

	public void remove() 
	{
		m_iter.remove();
	}

	public boolean hasNext() 
	{
		return m_iter.hasNext();
	}

	public void reset(java.util.List list) 
	{
		m_iter = list != null ? list.iterator() : null;
		if (m_iter == null)
			m_iter = new NullIteratorImpl();
	}

	public void reset(java.util.List list, int moveToIndex) 
	{
		reset(list);
		for (int i = 0; i < moveToIndex && hasNext(); i++)
			this.next();
	}
	
	protected Iterator m_iter = null;
}
