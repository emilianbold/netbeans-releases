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

/*
 * NavigatorTreeModel.java
 *
 * Created on December 9, 2005, 3:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.drawingarea.navigator;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author TreySpiva
 */
public class NavigatorTreeModel implements TreeModel
{
   private Object root = new Object();
   private ETList < IElement > items = null; 
   
   /** Creates a new instance of NavigatorTreeModel */
   public NavigatorTreeModel(ETList < IElement > items)
   {
      this.items = new ETArrayList < IElement >();
      this.items.addAll(items);
      Collections.sort(this.items, new ElementNameComparator());
   }

   public boolean isLeaf(Object node)
   {
      boolean retVal = true;
      
      retVal = getChildCount(node) <= 0;
      
      return retVal;
   }

   public int getChildCount(Object parent)
   {
      int retVal = 0;
      if((parent == root) && (items != null))
      {
         retVal = items.size();
      }
      return retVal;
   }

   public void valueForPathChanged(TreePath path, Object newValue)
   {
   }   

   public Object getChild(Object parent, int index)
   {
      Object retVal = null;
      
      if((parent == root) && (items != null))
      {
         if(index < items.size())
         {
            retVal = items.get(index);
         }
      }
      
      return retVal;
   }

   public Object getRoot()
   {
      return root;
   }

   public int getIndexOfChild(Object parent, Object child)
   {
      int retVal = 0;
      
      if((parent == root) && (items != null))
      {
         retVal = items.indexOf(child);
      }
      
      return retVal;
   }
   
   public void removeTreeModelListener(TreeModelListener l)
   {
   }

   public void addTreeModelListener(TreeModelListener l)
   {
   }

}
