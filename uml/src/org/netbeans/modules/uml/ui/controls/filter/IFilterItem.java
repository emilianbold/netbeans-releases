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



package org.netbeans.modules.uml.ui.controls.filter;

import javax.swing.Icon;

/**
 *
 * @author Trey Spiva
 */
public interface IFilterItem
{
   /** The item will be not be filtered out. */
   public static final int FILTER_STATE_ON  = 1;

   /** The item will be filtered out. */
   public static final int FILTER_STATE_OFF = 2;

   /**
    * The name of the filter item.  The name of the filter item will be
    * the name displayed in the filter dialog.
    *
    * @return The name of the item.
    */
   public String getName();

   /**
    * The state of the filter item.  The state will be either 
    * <code>FILTER_STATE_ON</code> or <code>FILTER_STATE_OFF</code>.
    * 
    * @return The state of the item.
    */
   public int getState();
   
   /**
    * Sets the filter state of the item.  
    * 
    * @param value Either<code>FILTER_STATE_ON</code> or 
    *              <code>FILTER_STATE_OFF</code>.
    */
   public void setState(int value);  
   
   /**
    * Sets the filter state of the item.  If the user modified the filter item
    * via the filter dialog the dialog parameter will give access to the tree 
    * settings.
    * 
    * @param value Either<code>FILTER_STATE_ON</code> or 
    *              <code>FILTER_STATE_OFF</code>.
    * @param dialog The dialog that changed the item.  This can be 
    *               <code>null</code> if the a filter dialog was not used to
    *               change the item.  
    */
   public void setState(int value, IFilterDialog dialog);   
   
   /**
    * Retrieve the icon associated with the item.  There no icon has been 
    * associated the icon the name of the item and CommonResources is used to 
    * try to retrieve the icon for the item.   
    * 
    * @return The icon associated with the item or <code>null</code> if an icon
    *         is not associated with the item.
    */
   public Icon getIcon();
}
