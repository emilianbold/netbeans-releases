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


package org.netbeans.modules.uml.ui.products.ad.application;

//import org.netbeans.modules.uml.ui.products.ad.application.action.PluginAction;

/**
 *
 * @author Trey Spiva
 */
public interface IMenu
{
   public final static int BAR       = 0x0;
   public final static int DROP_DOWN = 0x1;
   public final static int MENU      = 0x2;
   public final static int POP_UP    = 0x4;
   public final static int CASCADE   = 0x8;

   /**
    * @param index
    */
   public void insertSeperatorAt(int index);

   /**
    *
    */
   public void appendSeperator();

   /**
    * 
    */
   public IMenu getParentMenu();

   /**
    * @return
    */
   public int getStyle();

   /**
    * @param m_Action
    * @param index
    * @return
    */
//   public IMenuItem createMenuItem(PluginAction m_Action, int index);
   
//   public IMenuItem createMenuItem(String name, int index);

   /**
    * @param m_Action
    * @return
    */
//   public IMenuItem createMenuItem(PluginAction m_Action);
   
//   public IMenuItem createMenuItem(String name);

   /**
    * @return
    */
   public IMenu createSubMenu();

   /**
    * @return
    */
   public boolean isDisposed();

   /**
    * @return
    */
   public IMenuItem[] getItems();

   /**
    * @return
    */
   public int getItemCount();

   /**
    * @param i
    * @return
    */
   public IMenuItem getMenuItem(int i);

   /**
    * @param text
    */
   public void setText(String text);

}
