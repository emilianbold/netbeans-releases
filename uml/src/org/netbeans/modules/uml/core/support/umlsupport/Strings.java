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
