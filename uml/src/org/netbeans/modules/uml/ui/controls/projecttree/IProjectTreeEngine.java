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
 * Created on Jun 2, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.controls.projecttree;

import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.FilteredItemManager;

/**
 * The project tree engine is used to control the items that are
 * displayed in the project tree.  Other component can also add
 * items to the tree but the engines are to primary source of the
 * project tree items.
 *
 * @author Trey Spiva
 */
public interface IProjectTreeEngine
{
	/**
	 * Initializes the project tree engine.  The model can be
	 * used to add new project elements to the tree.
	 * 
	 * @param model The project tree data.
	 */
	public void initialize(IProjectTreeModel model);
   
   /**
    * Test if it is OK to delete a tree item.
    * 
    * @param item The item to test.
    * @return <b>true</b> if it is OK to delete the tree item, <b>false</b> if 
    *         it is not OK to delete the tree item.
    */
   public boolean canDelete(IProjectTreeItem item);

   /**
	* Test if it is OK to edit a tree item.
	* 
	* @param item The item to test.
	* @return <b>true</b> if it is OK to delete the tree item, <b>false</b> if 
	*         it is not OK to delete the tree item.
	*/
   public boolean canEdit(IProjectTreeItem item);
   
	/**
	 * called when item on the tree is expanding.
	 */
   public void onNodeExpanding(IProjectTreeControl          pParentControl,
									    IProjectTreeExpandingContext pContext,
                               FilteredItemManager          manager);

}
