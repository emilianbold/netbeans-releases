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



package org.netbeans.modules.uml.ui.support.contextmenusupport;

import java.awt.Menu;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;

/**
 * @author KevinM
 *
 */
public interface IProductButtonHandler {
	/// Useful when creating pullrights.  This one grabs an existing pullright if it exists or
	/// creates a new one if needed.
	 public IProductContextMenuItem createOrGetPullright(ETList<IProductContextMenuItem> pParentItems,
										 long nPullrightLabel,
										 String sButtonSource);

	/// Useful when creating pullrights.  This one grabs an existing pullright if it exists or
	/// creates a new one if needed.  This is the same as the above, but doesn't do a loadstring.
	 public IProductContextMenuItem createOrGetPullright(ETList<IProductContextMenuItem> pParentItems,
										 String sPullrightLabel,
										 String sButtonSource);

	/// Set the menu button sensitivity and check state
	 public void setSensitivityAndCheck(IProductContextMenu pContextMenu, 
										   IProductContextMenuItem pMenuItem, 
										   int buttonKind);


	/// Handle a button select.  menuSelected is the id of pMenuItem.
	 public boolean handleButton(IProductContextMenu pContextMenu,
								 IProductContextMenuItem pMenuItem,
								 int menuSelected);

	// Notification that the menu has closed
	 public void menuClosed();

	// Used to clear the menu list
	 public void clearMenuList();

	/// Adds the indicated menu button to the top or bottom of the menu
	 public void addMenuItem(IProductContextMenu pContextMenu, 
								int nMenuButtonID,
								String textString,
								String descriptionString,
								String sButtonSource);

	 public void addMenuItem(IProductContextMenu pContextMenu, 
								int nMenuButtonID,
								String textString,
								String descriptionString,
								String sButtonSource,
								boolean bDefaultSensitivity,
								IProductContextMenuSelectionHandler pChildHandler);
                               
	/// Adds the indicated menu button to the top or bottom of the menu
	 public void addMenuItem(IProductContextMenu pContextMenu,
								ETList<IProductContextMenuItem> pMenuItems,  
								int nMenuButtonID,
								String textString,
								String descriptionString,
								String sButtonSource);
  
	 public void addMenuItem(IProductContextMenu pContextMenu,
								ETList<IProductContextMenuItem> pMenuItems,  
								int nMenuButtonID,
								String textString,
								String descriptionString,
								String sButtonSource,
								boolean bDefaultSensitivity,
								IProductContextMenuSelectionHandler pChildHandler);
                                  
	/// Adds a separator to the menu
	 public void addSeparatorMenuItem(IProductContextMenu pContextMenu);

	/// Adds a separator to the menu
	 public void addSeparatorMenuItem(IProductContextMenu pContextMenu, 
										 ETList<IProductContextMenuItem> pMenuItems);
   
	// Handles the selection of the menu item
	 public boolean handleSelection(IProductContextMenu pContextMenu,
									IProductContextMenuItem pSelectedItem);
   
	/// Converts the description of the menu item to a MenuButtonKind
	 public int getMenuButtonClicked(IProductContextMenuItem pMenuItem);

	/// Displays the context menu and handles the selection
	 public void displayAndHandleContextMenu(IProductContextMenu pContextMenu);

	/// Handles a popup menu selection
	 public boolean handleContextMenuSelection(int menuItemID, 
											   IProductContextMenu pContextMenu);

   
	/// Creates CMenu items based on the ETList<IProductContextMenuItem>
	 public void addMenuItems(Menu parentMenu, 
								 ETList<IProductContextMenuItem> pMenuItems);
   
	/// Users must provide this functionality to load this id from the correct resource
	 String loadString(String id);

	/// Users must provide this functionality to determine whether the button is valid for the menu
	 public boolean isValidButton(IProductContextMenuItem pItem);
}
