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

//import org.netbeans.modules.uml.ui.products.ad.application.action.IContributionItem;

/**
 *
 * @author Trey Spiva
 */
public interface IMenuItem
{

   /**
    * @return
    */
   public IMenu getParentMenu();

   /**
    * @param text
    */
   public void setText(String text);

   /**
    * @param i
    */
   public void setAccelerator(int i);

   /**
    * @return
    */
   public boolean getEnabled();

   /**
    * @param shouldBeEnabled
    */
   public void setEnabled(boolean shouldBeEnabled);

   /**
    * @return
    */
   public boolean getSelection();

   /**
    * @param bv
    */
   public void setSelection(boolean bv);

   /**
    * @return
    */
   public Object getData();

   /**
    * @param src
    */
//   public void setData(IContributionItem src);
   
   /**
    * 
    */
   public void dispose();

}
