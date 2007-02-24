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
 * Created on Jun 12, 2003
 *
 */
package org.netbeans.modules.uml.designpattern;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;


/**
 * @author sumitabhk
 *
 */
public class JRoleTextField extends JTextField
{
	private WizardRoleObject m_RoleObject = null;
	private WizardRoles m_Clazz = null;
	/**
	 *
	 */
	public JRoleTextField()
	{
		super();
	}

	public JRoleTextField(WizardRoleObject obj, WizardRoles clazz)
	{
		super();
		m_RoleObject = obj;
		m_Clazz = clazz;

		addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e)
			{
			}

			public void focusLost(FocusEvent e)
			{
				if (m_RoleObject != null)
				{
					Object obj = e.getSource();
					if (obj instanceof JTextField)
					{
						JTextField field = (JTextField)obj;
						m_RoleObject.setChosenName(field.getText());
						// now figure out if what was typed into the text field exists in the
						// project, and if it does, we need to store the id of what was chosen
						// on our object
						String id = m_Clazz.getIDForElementNamed(field.getText(), m_RoleObject);
						if (id != null && id.length() > 0)
						{
							m_RoleObject.setChosenID(id);
						}
					}
				}
			}
		});
	}
}



