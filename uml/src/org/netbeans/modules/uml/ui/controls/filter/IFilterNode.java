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

import org.netbeans.modules.uml.ui.swing.projecttree.JFilterDialog;

/**
 *
 * @author Trey Spiva
 */
public interface IFilterNode
{
   /**
    * Removes newChild from its parent and makes it a child of this node by
    * adding it to the end of this node's child array.
    *
    * @param newChild node to add as a child of this node
    * @throws IllegalArgumentException if newChild is null.
    */
   public void add(IFilterNode newChild) throws IllegalArgumentException;

   /**
    * @return
    */
   public String getDispalyName();

   /**
   * Specifies if the checkbox for the filter item is on or off.
   *   
   * @return <b>true</b> if the item is checked, <b>false</b> if the item is
   *         off.
   */
   public boolean isOn();

   /**
    * Retrieves the icon that represent the filter item.
    * 
    * @return The icon to display.
    */
   public Icon getIcon();

   /**
    * Saves the nodes contents to the associated IFilterItem.  The children
    * of the node is also saved.
    * 
    * @param dialog The dialog that set the state.
    */
   public void save(IFilterDialog dialog);

   /**
    * Sets the state of the filter item.
    *   
    * @param value <b>true</b> if the item is checked, <b>false</b> if the 
    *              item is off.
    */
   public void setState(int state);

   /**
    * Retrieve the number of children that the node has.
    * 
    * @return The number of children.
    */
   public int getChildCount();
}
