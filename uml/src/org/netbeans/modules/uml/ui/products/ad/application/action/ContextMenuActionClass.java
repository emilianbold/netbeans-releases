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



package org.netbeans.modules.uml.ui.products.ad.application.action;

import java.awt.event.ActionEvent;

/**
 * @author KevinM
 * JM
 *
 */
public class ContextMenuActionClass extends ContextMenuBaseAction {

	protected IETContextMenuHandler m_menuHandler;
	
	/**
	 *
	 */
	public ContextMenuActionClass(IETContextMenuHandler pMenuHandler, String text, String menuId) {
		super(text, menuId);
		m_menuHandler = pMenuHandler;
	}

	public boolean isEnabled() {
		return m_menuHandler != null ? m_menuHandler.setSensitivityAndCheck(getMenuId(), this) : false;
	}

	public void actionPerformed(ActionEvent e) {
		if (m_menuHandler != null)
			m_menuHandler.onHandleButton(e, getMenuId());
	}

}
