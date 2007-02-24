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


package org.netbeans.modules.uml.ui.controls.projecttree;

import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.FilteredItemManager;
import java.awt.datatransfer.Transferable;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

public interface IProjectTreeEventsSink
{
	/**
	 * A project node is being expanded
	*/
	public void onItemExpanding( IProjectTreeControl pParentControl,
                                IProjectTreeExpandingContext pContext,
                                IResultCell cell );

   /**
	 * A project node is being expanded.  The expanded item will be populated
    * with only the items specified by the filter manager.
	 */
	public void onItemExpandingWithFilter( IProjectTreeControl pParentControl,
                                          IProjectTreeExpandingContext pContext, 
                                          FilteredItemManager manager, 
                                          IResultCell cell );
   
	/**
	 * A project node is about to be edited
	*/
	public void onBeforeEdit( IProjectTreeControl pParentControl, 
	                          IProjectTreeItem pItem, 
	                          IProjectTreeEditVerify pVerify, 
	                          IResultCell cell );

	/**
	 * A project node has been edited
	*/
	public void onAfterEdit( IProjectTreeControl pParentControl,  
	                         IProjectTreeItem pItem, 
	                         IProjectTreeEditVerify pVerify, 
	                         IResultCell cell );

	/**
	 * A project node has been double clicked on
	*/
	public void onDoubleClick( IProjectTreeControl pParentControl, 
	                           IProjectTreeItem    pItem, 
                              boolean             isControl, 
                              boolean             isShift, 
                              boolean             isAlt, 
                              boolean             isMeta, 
	                           IResultCell         cell );

	/**
	 * The tree's selection has changed
	*/
	public void onSelChanged( IProjectTreeControl pParentControl, 
	                          IProjectTreeItem[] pItem, 
	                          IResultCell cell );

	/**
	 * A project node has been right clicked on
	*/
	public void onRightButtonDown( IProjectTreeControl pParentControl, 
	                               IProjectTreeItem pItem, 
	                               IProjectTreeHandled pHandled, 
	                               int nScreenLocX, 
	                               int nScreenLocY, 
	                               IResultCell cell );

	/**
	 * A project node in beginning a drag operation
	*/
	public void onBeginDrag( IProjectTreeControl pParentControl, 
	                         IProjectTreeItem[] pItem, 
	                         IProjectTreeDragVerify pVerify, 
	                         IResultCell cell );

	/**
	 * A dataobject is proposed for dropping
	*/
	public void onMoveDrag( IProjectTreeControl pParentControl, 
	                        Transferable pItem, 
	                        IProjectTreeDragVerify pVerify, 
	                        IResultCell cell );

	/**
	 * A dataobject has been dropped
	*/
	public void onEndDrag( IProjectTreeControl    pParentControl, 
                          Transferable           pItem, 
                          int                    action,
	                       IProjectTreeDragVerify pVerify, 
	                       IResultCell            cell );

}
