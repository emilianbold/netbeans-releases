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



package org.netbeans.modules.uml.ui.swing.pulldownbutton;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

/**
 *
 * @author Trey Spiva
 */
public class PopupMenuInvoker implements IPulldownButtonInvoker
{
   private JPopupMenu m_Menu = null;

   public PopupMenuInvoker(JPopupMenu menu)
   {
      setMenu(menu);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.swing.pulldownbutton.IPulldownButtonInvoker#showPulldown(javax.swing.JComponent)
    */
   public void showPulldown(JComponent owner)
   {
      JPopupMenu menu = getMenu();
      if(menu != null)
      {
         menu.show(owner, 0, owner.getHeight());
      }
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.swing.pulldownbutton.IPulldownButtonInvoker#hidePulldown(javax.swing.JComponent)
    */
   public void hidePulldown(JComponent owner)
   {
      JPopupMenu menu = getMenu();
      if(menu != null)
      {
         menu.setVisible(false);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.addinframework.ui.swing.pulldownbutton.IPulldownButtonInvoker#isPulldownVisible()
    */
   public boolean isPulldownVisible()
   {
      boolean retVal = false;
      
      JPopupMenu menu = getMenu();
      if(menu != null)
      {
         retVal = menu.isVisible();
      }
      
      return retVal;
   }

   /**
    * @return
    */
   public JPopupMenu getMenu()
   {
      return m_Menu;
   }

   /**
    * @param menu
    */
   public void setMenu(JPopupMenu menu)
   {
      m_Menu = menu;
   }
}
