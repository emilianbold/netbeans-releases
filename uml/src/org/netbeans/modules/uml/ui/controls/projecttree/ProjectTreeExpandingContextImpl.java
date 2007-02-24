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
 *
 * Created on Jun 12, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.controls.projecttree;

import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;

/**
 *
 * @author Trey Spiva
 */
public class ProjectTreeExpandingContextImpl
   implements IProjectTreeExpandingContext
{
	private ITreeItem m_Item       = null;
	private boolean   m_IsCanceled = false;
	
	public ProjectTreeExpandingContextImpl(ITreeItem item)
	{
      setTreeItem(item);
	}
	
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext#getProjectTreeItem()
    */
   public IProjectTreeItem getProjectTreeItem()
   {
      IProjectTreeItem retVal = null;
      
      if(m_Item != null)
      {
         retVal = m_Item.getData();
      }
      return retVal;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext#setProjectTreeItem(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
    */
   public ITreeItem getTreeItem( )
   {
      return m_Item;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext#setProjectTreeItem(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
    */
   public void setTreeItem(ITreeItem value)
   {
		m_Item = value;
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext#getCancel()
    */
   public boolean isCancel()
   {
      return m_IsCanceled;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext#setCancel(boolean)
    */
   public void setCancel(boolean value)
   {
		m_IsCanceled = value;
   }

}
