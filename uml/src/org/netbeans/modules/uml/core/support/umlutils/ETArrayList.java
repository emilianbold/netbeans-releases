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

package org.netbeans.modules.uml.core.support.umlutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 */
public class ETArrayList<TypeName> extends ArrayList<TypeName> implements ETList<TypeName>
{
	public ETArrayList()
	{		
	}
	public ETArrayList(Collection<TypeName> c)
	{
		super(c);
	}
	public ETArrayList(int initialCapacity)
	{
		super(initialCapacity);
	}
	
	public boolean find(TypeName obj)
	{
        return contains(obj);
	}
	public int getCount()
	{
		return size();
	}
	public TypeName item(int i)
	{
		return get(i);
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlutils.ETListEx#addIfNotInList(null)
    */
   public void addIfNotInList(TypeName element)
   {
      if( !contains( element ))
      {
         add( element );         
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlutils.ETListEx#isInList(null)
    */
   public boolean isInList(TypeName element)
   {
      // TODO Auto-generated method stub
      return contains( element );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlutils.ETListEx#removeItem(null)
    */
   public void removeItem(TypeName element)
   {
      remove( element );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlutils.ETListEx#removeThese()
    */
   public void removeThese( ETList<TypeName> elements )
   {
      removeAll( elements );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlutils.ETListEx#addThese()
    */
   public void addThese( ETList<TypeName> elements )
   {
      addAll( elements );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.support.umlutils.ETListEx#removeDuplicates()
    */
   public void removeDuplicates()
   {
      ETArrayList<TypeName> newList = new ETArrayList<TypeName>();
      
      for (Iterator iter = iterator(); iter.hasNext();)
      {
         TypeName element = (TypeName)iter.next();
         newList.addIfNotInList( element );
      }
      
      if( newList.size() < size() )
      {
         clear();
         addAll( newList );
      }
   }
}
