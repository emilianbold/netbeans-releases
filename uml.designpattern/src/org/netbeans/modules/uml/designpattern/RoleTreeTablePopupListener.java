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
 * Created on Jun 9, 2003
 *
 */
package org.netbeans.modules.uml.designpattern;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author sumitabhk
 *
 */
public class RoleTreeTablePopupListener extends MouseAdapter
{

	/**
	 *
	 */
	public RoleTreeTablePopupListener()
	{
		super();
	}

	public void mousePressed(MouseEvent e) {

		if (e.getButton() == MouseEvent.BUTTON3)
		{
			maybeShowPopup(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		//maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		Object source = e.getSource();
		if (source instanceof JRoleTreeTable)
		{
			JRoleTreeTable treeTable = (JRoleTreeTable)source;
			int row = treeTable.rowAtPoint(e.getPoint());
			treeTable.handlePopupDisplay(e);
		}
	}

}



