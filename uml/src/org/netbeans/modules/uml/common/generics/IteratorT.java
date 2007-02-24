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
