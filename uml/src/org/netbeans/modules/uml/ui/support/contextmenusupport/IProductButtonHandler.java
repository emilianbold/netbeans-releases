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



package org.netbeans.modules.uml.ui.support.contextmenusupport;

import java.awt.Menu;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

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
