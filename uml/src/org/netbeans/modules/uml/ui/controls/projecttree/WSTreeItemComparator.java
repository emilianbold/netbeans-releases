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



package org.netbeans.modules.uml.ui.controls.projecttree;

import java.util.Comparator;

import org.netbeans.modules.uml.core.workspacemanagement.IWSElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;


/**
 *
 * @author Trey Spiva
 */
public class WSTreeItemComparator implements Comparator < ITreeItem >
{

   private IWSElement m_Element = null;

   public WSTreeItemComparator(IWSElement element)
   {
      m_Element = element;
   }

   public int compare(ITreeItem o1, ITreeItem o2)
   {
      return 0;
   }

   public boolean equals(Object obj)
   {
      boolean retVal = false;
      
      if((obj instanceof ITreeItem) && (m_Element != null))
      {  
         ITreeItem item = (ITreeItem)obj;
         
         IProjectTreeItem myItem = item.getData();
         
         if((myItem != null) && (myItem.isProject() == true))
         {
            IWSElement element = (IWSElement)obj;
            retVal = myItem.getItemText().equals(m_Element.getNameWithAlias());
         }
      }
      
      return retVal;
   }

}
