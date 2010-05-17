/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.uml.designpattern;

import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuSelectionHandler;
import org.netbeans.modules.uml.ui.support.contextmenusupport.ProductContextMenuItem;

public class DesignCenterUtilities
{
   /**
    *
    */
   public DesignCenterUtilities()
   {
      super();
   }

   /**
    * Create a menu item on the context menu passed in
    *
    * @param pContextMenu[in]	The context menu to add the one we are about to create to
    * @param name[in]					The name of the new button
    * @param id[in]						The xmi id of the pattern represented by this button
    * @param bSensitive[in]			The state of the new button
    * @param pHandler[in]				The current context menu handler
    * @param pContextMenuOut[out]	The new context menu
    *
    * return HRESULT
    */
   public static IProductContextMenuItem createMenuItemOnMain(IProductContextMenu pContextMenu, String name,
      String id,
      boolean bSensitive,
      IProductContextMenuSelectionHandler pHandler)
   {
      IProductContextMenuItem pContextMenuItemOut = null;
      if (pContextMenu != null && pHandler != null)
      {
         // get the submenus of the passed in menu
         ETList <IProductContextMenuItem> pMenuItems = pContextMenu.getSubMenus();
         if (pMenuItems != null)
         {
            // create the menu item
            pContextMenuItemOut = new ProductContextMenuItem();
            if (pContextMenuItemOut != null)
            {
               pContextMenuItemOut.setMenuString( name );
               pContextMenuItemOut.setButtonSource( id );
               pContextMenuItemOut.setSelectionHandler( pHandler );
               pContextMenuItemOut.setSensitive(bSensitive);
               pMenuItems.add( pContextMenuItemOut );
            }
         }
      }
      return pContextMenuItemOut;
   }
   /**
    * Create a menu item on the context menu item passed in
    *
    * @param pContextMenuItem[in]	The context menu item to add the one we are about to create to
    * @param name[in]					The name of the new button
    * @param id[in]						The xmi id of the pattern represented by this button
    * @param bSensitive[in]			The state of the new button
    * @param pHandler[in]				The current context menu handler
    * @param pContextMenuOut[out]	The new context menu
    *
    * return HRESULT
    */
   public static IProductContextMenuItem createMenuItemOnSub(IProductContextMenuItem pContextMenuItem, String name,
      String id,
      boolean bSensitive,
      IProductContextMenuSelectionHandler pHandler)
   {
      IProductContextMenuItem pContextMenuItemOut = null;
      if (pContextMenuItem != null && pHandler != null)
      {
         // get the submenus of the passed in menu
         ETList <IProductContextMenuItem> pMenuItems = pContextMenuItem.getSubMenus();
         if (pMenuItems != null)
         {
            pContextMenuItemOut = createMenuItem(pMenuItems, name, id, bSensitive, pHandler);
         }
      }
      return pContextMenuItemOut;
   }
   /**
    * Create a menu item and add it to the items passed in
    *
    * @param pContextMenuItems[in]	The items to add the one we are about to create to
    * @param name[in]					The name of the new button
    * @param id[in]						The xmi id of the pattern represented by this button
    * @param bSensitive[in]			The state of the new button
    * @param pHandler[in]				The current context menu handler
    * @param pContextMenuOut[out]	The new context menu
    *
    * return HRESULT
    */
   public static IProductContextMenuItem createMenuItem(ETList <IProductContextMenuItem> pContextMenuItems, String name,
                                                                                                               String id,
                                                                                                               boolean bSensitive,
                                                                                                                  IProductContextMenuSelectionHandler pHandler)
	{
	   	IProductContextMenuItem pContextMenuItemOut = null;
	   	if (pContextMenuItems != null && pHandler != null)
		{
	   		// create the menu item for the namespace
	   		pContextMenuItemOut = new ProductContextMenuItem();
	   		if (pContextMenuItemOut != null)
			{
	   			pContextMenuItemOut.setMenuString( name );
	   			pContextMenuItemOut.setButtonSource( id );
	   			pContextMenuItemOut.setSelectionHandler( pHandler );
	   			pContextMenuItemOut.setSensitive(bSensitive);
	   			pContextMenuItems.add( pContextMenuItemOut );
			}
		}
	   	return pContextMenuItemOut;
	}
   /**
    * Given a particular tree item, go up its tree structure until the node is found that represents
    * the addin.
    *
    *
    * @param pNode[in]				The current node
    * @param pAddInNode[out]		The node that represents the addin
    *
    * @return HRESULT
    *
    */
   public static IProjectTreeItem getAddInNode(IProjectTreeControl pTree, IProjectTreeItem pNode)
   {
      IProjectTreeItem pAddInNode = null;
      if (pTree != null && pNode != null)
      {
        IProjectTreeItem pTemp = pNode;
        String progID = pNode.getSecondaryDescription();
		try
		{
		  	Class clazz = Class.forName(progID);
		  	if (clazz != null)
         	{
            	IProjectTreeItem pParent = pTree.getParent(pNode);
            	pAddInNode = getAddInNode(pTree, pParent);
         	}
         	else
         	{
            	pAddInNode = pTemp;
         	}
		}
		catch (Exception e)
		{}
      }
      return pAddInNode;
   }
   public static boolean selectedItemsInProgIDTree(IProjectTreeControl pControl, ETList<String> pProgIDs)
   {
      boolean bFlag = true;
      if (pControl != null)
      {
	    IProjectTreeItem[] pTreeItems = pControl.getSelected();
	    if (pTreeItems != null)
	    {
		    // if anything is selected
		    int count = pTreeItems.length;
		    for (int x = 0; x < count; x++)
		    {
			    // get the tree item
			    IProjectTreeItem pTreeItem = pTreeItems[x];
			    if (pTreeItem != null)
			    {
				    // get the addin node of the tree item that is being edited
				    IProjectTreeItem pAddInNode = getAddInNode(pControl, pTreeItem);
				    if (pAddInNode != null)
				    {
					    // get the addin node's description
					    String progID2 = pAddInNode.getSecondaryDescription();
					    boolean bInList = true; // TODO pProgIDs.isInList(progID2, false);
					    if (!bInList)
					    {
						    bFlag = false;
						    break;
					    }
				    }
			    }
		    }
	   	}
	  }
	  return bFlag;
	}
}
