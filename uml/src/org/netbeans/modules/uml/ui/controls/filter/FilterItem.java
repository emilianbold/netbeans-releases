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

import java.util.ArrayList;

import javax.swing.Icon;

import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;

/**
 * A FilterItem contians the data that represents an item that can be filtered.
 *
 * @author Trey Spiva
 */
public class FilterItem implements IFilterItem
{
   private String      m_ItemName = "";
   private int         m_ItemKind = FILTER_STATE_ON;
   private ArrayList   m_Children = new ArrayList();
   private Icon        m_Icon     = null;

   /**
    * Initalizes the FilterItem obeject.  The state of the item will be 
    * <code>FILTER_STATE_ON</code>.
    * 
    * @param text The name of the filter item.
    */
   public FilterItem(String text)
   {
      this(text, FILTER_STATE_ON);
   }
   
   /**
    * Initalizes the FilterItem obeject.  
    * 
    * @param text The name of the filter item.
    * @param kind The state of the item (FILTER_STATE_ON or FILTER_STATE_OFF).  
    */
   public FilterItem(String text, int kind)
   {
      setName(text);
      setState(kind);
   }
   
   /**
    * Sets the filter state of the item.
    * 
    * @param value Either<code>FILTER_STATE_ON</code> or 
    *              <code>FILTER_STATE_OFF</code>.
    */
   public final void setState(int value)
   {
      setState(value, null);
   }
   
   /**
    * Sets the filter state of the item. 
    * 
    * @param value Either<code>FILTER_STATE_ON</code> or 
    *              <code>FILTER_STATE_OFF</code>.
    * @param dialog The dialog that changed the item.  This can be 
    *               <code>null</code> if the a filter dialog was not used to
    *               change the item.  
    */
   public void setState(int value, IFilterDialog dialog)
   {
      m_ItemKind = value;
   }
   
   /* (non-Javadoc)
   * @see org.netbeans.modules.uml.ui.controls.projecttree.IFilterItem#getState()
   */
  public int getState()
  {
     // TODO Auto-generated method stub
     return m_ItemKind;
  }

   /**
    * Sets the name of the item.
    * @param value The name.
    */
   public void setName(String value)
   {
      m_ItemName = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IFilterItem#getName()
    */
   public String getName()
   {
      return m_ItemName;
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      boolean retVal = false;
      if (obj instanceof String)
      {
         String strObj = (String)obj;
         retVal = strObj.equals(getName());
      }
      else
      {
         retVal = super.equals(obj);
      }
      
      return retVal;
   }
      
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IFilterItem#getIcon()
    */
   public Icon getIcon()
   {
      if(m_Icon == null)
      {
         CommonResourceManager manager = CommonResourceManager.instance();
         setIcon(manager.getIconForElementType(getName()));
      }
      return m_Icon;
   }
   
   /**
    * The icon to use if it can not be retrieved from the CommonResourceManager.
    * 
    * @param icon The icon to use.
    * @see CommonResourceManager
    */
   public void setIcon(Icon icon)
   {
      m_Icon = icon;
   }

}