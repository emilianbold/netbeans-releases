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
 * Created on Jun 26, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.controls.projecttree;

/**
 *
 * @author Trey Spiva
 */
public class ProjectTreeDragVerifyImpl implements IProjectTreeDragVerify
{
   private IProjectTreeItem m_TargetNode = null;
   private boolean          m_IsCanceled = false;
   private boolean          m_IsHandled  = false;
   private long             m_DropEffect = 0;

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify#getTargetNode()
    */
   public IProjectTreeItem getTargetNode()
   {
      return m_TargetNode;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify#setTargetNode(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
    */
   public void setTargetNode(IProjectTreeItem value)
   {
      m_TargetNode = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify#getCancel()
    */
   public boolean isCancel()
   {
      return m_IsCanceled;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify#setCancel(boolean)
    */
   public void setCancel(boolean value)
   {
      m_IsCanceled = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify#getHandled()
    */
   public boolean getHandled()
   {
      return m_IsHandled;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify#setHandled(boolean)
    */
   public void setHandled(boolean value)
   {
      m_IsHandled = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify#getDropEffect()
    */
   public long getDropEffect()
   {
      return m_DropEffect;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify#setDropEffect(long)
    */
   public void setDropEffect(long value)
   {
      m_DropEffect = value;
   }
}
