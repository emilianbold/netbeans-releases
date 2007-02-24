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

import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;

/**
 * Specifies the interface that all fitler dialog must implement.
 *
 * @author Trey Spiva
 */
public interface IFilterDialog
{
   /**
    * Display the filter dialog to the user.
    */
   public void show();

   /**
    * Create a new root level node.  When the value of a root level node changes
    * all of it's childrens values are changed to match the roots new value.
    * The root level node is commonly used to specify a category.  
    * 
    * @param name The name of the root node.
    * @return The new node.
    */
   public IFilterNode createRootNode(String name);
   
   /**
    * Create a new root level node.  When the value of a root level node changes
    * all of it's childrens values are changed to match the roots new value.  
    * The root level node is commonly used to specify a category.  
    * 
    * @param item The name of the root node.
    * @return The new node.
    */
   public IFilterNode createRootNode(IFilterItem item);
   
   /**
    * Adds a new filter item to the specified FilterNode.  The FilterNode that
    * is created to represent the IFilterItem is returned.  When the value of 
    * a root level node changesall of it's childrens values are changed to 
    * match the roots new value.
    * 
    * @param parent The parent of the filter item.  If the parent is 
    *               <code>null</code> then the filter item is not added.
    * @param item The filter item to add to the FilterNode.
    * @return The FilterNode that represents the filter item.
    */
   public IFilterNode addFilterItem(IFilterNode parent, IFilterItem item);
   
   public IProjectTreeModel getProjectTreeModel();
}
