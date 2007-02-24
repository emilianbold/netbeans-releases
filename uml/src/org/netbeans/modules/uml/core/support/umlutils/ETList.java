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

import java.util.List;
/**
 *
 */
public interface ETList<TypeName> extends List<TypeName>
{
   public boolean find(TypeName obj);

   /**
    * Returns the size of this list. Intended for compatibility with existing
    * code (new code should use size()).
    *
    * @return The size of the list.
    */
   public int getCount();

   /**
    * Returns the item at the given index. Intended for compatibility with
    * existing code (new code should use get(int)).
    *
    * @param  i The index.
    * @return The item at the given index.
    */
   public TypeName item(int i);
   
   /// Adds if this element is not already in the list
   void addIfNotInList(TypeName element);
   
   /// Is this element in the list?
   boolean isInList(TypeName element);
   
   /// Removes this item from the list.
   void removeItem(TypeName element);
   
   /// Removes these elements from the list.
   void removeThese(ETList < TypeName > elements);
   
   /// Adds these elements into the list if they arent already there.
   void addThese(ETList < TypeName > elements);
   
   /// Removes duplicates from the list.
   void removeDuplicates();
   
   /// Clears the list.
   void clear();
}

