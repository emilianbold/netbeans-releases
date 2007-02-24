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

import java.util.Vector;

import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;

public class Strings extends ETArrayList<String> implements IStrings
{

   //private Vector < String > m_Strings = new Vector < String > ();

   public Strings()
   {
   }

   public int getCount()
   {
      return size();
   }

   public boolean add(String str)
   {
   	  boolean retBool = false;
      if (!str.equals(""))
         retBool = super.add(str);

      return retBool;   
   }
   
   public String item(int index)
   {
      String retVal = null;
      
      if(size() >= index)
      {
      	retVal = super.get((int)index);
      }
      return retVal;
   }
   
   public void removeElement(int index)
   {
      if (index >= 0)
      {
         remove(index);
      }
   }
   
   public boolean isInList(String tag, boolean caseSensitive)
   {
      return contains(tag);
   }

   public void addIfNotInList(String tag, boolean caseSensitive)
   {
      if (!contains(tag))
      {
         add(tag);
      }
   }

   public String getListAsDelimitedString(String delimiter)
   {
   	// I am using a Stirng buffer because it is suppose to be
   	// faster when concatenating strings.
      StringBuffer str = new StringBuffer();
      for (int i = 0; i < size(); i++)
      {
         //str += get(i) + delimiter;
         if((str.length() > 0 ) && (delimiter != null) && 
            (delimiter.length() > 0))
         {
         	str.append(delimiter);
         }
         str.append(get(i));
      }
      return str.toString();
   }

   public void clear()
   {
      super.clear();
   }

   public void append(IStrings list)
   {
      if (list != null && list.getCount() > 0)
      {
         for (int i = 0; i < list.getCount(); i++)
         {
            add(list.item(i));
         }
      }
   }

//   public void remove(String sVal, boolean caseSensitive)
//   {
//      if (caseSensitive)
//      {
//         int pos = indexOf(sVal);
//         if (((String) (get(pos))).equals(sVal))
//         {
//            removeElement(sVal);
//         }
//      }
//      else
//      {
//         removeElement(sVal);
//      }
//   }

}