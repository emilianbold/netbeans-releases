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
import java.util.HashMap;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author KevinM
 *
 */
public class ProductButtonHandler implements IProductButtonHandler {

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#addMenuItem(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, int, java.lang.String, java.lang.String, java.lang.String, boolean, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuSelectionHandler)
	 */
	public void addMenuItem(
		IProductContextMenu pContextMenu,
		int nMenuButtonID,
		String textString,
		String descriptionString,
		String sButtonSource,
		boolean bDefaultSensitivity,
		IProductContextMenuSelectionHandler pChildHandler) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#addMenuItem(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, int, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addMenuItem(IProductContextMenu pContextMenu, int nMenuButtonID, String textString, String descriptionString, String sButtonSource) {
		// TODO Auto-generated method stub

	}

	public void addMenuItem(
		IProductContextMenu pContextMenu,
		ETList < IProductContextMenuItem > pMenuItems,
		int nMenuButtonID,
		String textString,
		String descriptionString,
		String sButtonSource,
		boolean bDefaultSensitivity,
		IProductContextMenuSelectionHandler pChildHandler) {
		// TODO Auto-generated method stub
	}

	public void addMenuItems(Menu parentMenu, ETList < IProductContextMenuItem > pMenuItems) {
		// TODO Auto-generated method stub
	}

	/// Adds a separator to the menu
	public void addSeparatorMenuItem(IProductContextMenu pContextMenu, ETList < IProductContextMenuItem > pMenuItems) {
		// TODO Auto-generated method stub
	}

	public void addMenuItem(IProductContextMenu pContextMenu, ETList < IProductContextMenuItem > pMenuItems, int nMenuButtonID, String textString, String descriptionString, String sButtonSource) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#addMenuItem(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addMenuItem(IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#addMenuItems(java.awt.Menu)
	 */
	public void addMenuItems(Menu parentMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#addSeparatorMenuItem(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void addSeparatorMenuItem(IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#clearMenuList()
	 */
	public void clearMenuList() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#createOrGetPullright()
	 */
	public IProductContextMenuItem createOrGetPullright(ETList < IProductContextMenuItem > pParentItems, long nPullrightLabel, String sButtonSource) {
		// TODO Auto-generated method stub
		return null;
	}

	/// Useful when creating pullrights.  This one grabs an existing pullright if it exists or
	/// creates a new one if needed.  This is the same as the above, but doesn't do a loadstring.
	public IProductContextMenuItem createOrGetPullright(ETList < IProductContextMenuItem > pParentItems, String sPullrightLabel, String sButtonSource) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#displayAndHandleContextMenu(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public void displayAndHandleContextMenu(IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#getMenuButtonClicked(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem)
	 */
	public int getMenuButtonClicked(IProductContextMenuItem pMenuItem) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#handleButton(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, int)
	 */
	public boolean handleButton(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int menuSelected) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#handleContextMenuSelection(int, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu)
	 */
	public boolean handleContextMenuSelection(int menuItemID, IProductContextMenu pContextMenu) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#handleSelection(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem)
	 */
	public boolean handleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pSelectedItem) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#isValidButton(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem)
	 */
	public boolean isValidButton(IProductContextMenuItem pItem) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#LoadString(java.lang.String)
	 */
	public String loadString(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#menuClosed()
	 */
	public void menuClosed() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.contextmenusupport.IProductButtonHandler#setSensitivityAndCheck(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, int)
	 */
	public void setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind) {
		// TODO Auto-generated method stub

	}

	/// Adds a menu button to our menu list
	protected void addToMenuButtonList(
		IProductContextMenu pContextMenu,
		int menuButtonID,
		String textString,
		String descriptionString,
		String sButtonSource,
		IProductContextMenuSelectionHandler pChildHandler) {
	}

	/// Adds the indicated menu button to the top or bottom of the menu
	protected void addMenuItem(IProductContextMenu pContextMenu, int nKind) {
	}

	/// Adds the indicated menu button to the top or bottom of the menu
	protected void addMenuItem(IProductContextMenu pContextMenu, ETList < IProductContextMenuItem > pMenuItems, int nKind, boolean bDefaultSensitivity) {
	}

	protected void addMenuItem(IProductContextMenu pContextMenu, ETList < IProductContextMenuItem > pMenuItems, int nKind) {
		addMenuItem(pContextMenu, pMenuItems, nKind, true);
	}

	/// Adds a close listener to the context menu
	public void addCloseListener(IProductContextMenu pContextMenu) {
	}

	class ADAMenuButton {
		public ADAMenuButton(String textString, String descriptionString, String sButtonSource, IProductContextMenuSelectionHandler pSelectionHandler) {
			m_DescriptionString = descriptionString;
			m_ButtonSource = sButtonSource;
			if (textString != null) {
				m_TextString = textString;
			}

			m_MenuButtonID = 0;
			m_SelectionHandler = pSelectionHandler;
		}

		public long m_MenuButtonID;
		public String m_TextString;
		public String m_DescriptionString;
		public String m_ButtonSource;
		public IProductContextMenuSelectionHandler m_SelectionHandler;
	}

	class ADMenuButtonHolder {
		public void addButton(int buttonID, ADAMenuButton button) {
			m_buttonCache.put(hash(buttonID), button);
		}

		public ADAMenuButton getButton(int buttonID) {
			return (ADAMenuButton) m_buttonCache.get(hash(buttonID));
		}

		protected String hash(int buttonID) {
			Integer pBtnID = new Integer(buttonID);
			return pBtnID.toString();
		}

		protected HashMap m_buttonCache = new HashMap();
	}

	ADMenuButtonHolder m_MenuButtons = new ADMenuButtonHolder();

	// This is the object that should handle the selection of the button.  Usually the
	// messaged gets piped back this HandleButton on this instance
	// protected ProductContextMenuSelectionHandler m_SelectionHandler;

	// This object gets notified when the context menu is closed (actually FinalRelease)
	// protected ProductContextMenuClosed m_MenuClosedListener;
}
