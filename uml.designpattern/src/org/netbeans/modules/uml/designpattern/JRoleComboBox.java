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

/*
 * Created on Jun 12, 2003
 *
 */
package org.netbeans.modules.uml.designpattern;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;



/**
 * @author sumitabhk
 *
 */
public class JRoleComboBox extends JComboBox
{
	private WizardRoleObject m_RoleObject = null;
	private WizardRoles m_Clazz = null;
	/**
	 *
	 */
	public JRoleComboBox()
	{
		super();
		setEditable(true);
		setUI(new NewBasicComboBoxUI());
	}

	public JRoleComboBox(WizardRoleObject obj, WizardRoles clazz)
	{
		super();
		setEditable(true);
		setRequestFocusEnabled(true);
		setFocusable(true);
		setLightWeightPopupEnabled(false);
		setUI(new NewBasicComboBoxUI());
		m_RoleObject = obj;
		m_Clazz = clazz;

		getEditor().getEditorComponent().addFocusListener(new FocusListener() {

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
	//	our extended BasicComboBoxUI class
 	class NewBasicComboBoxUI extends BasicComboBoxUI
	 {
		protected ComboPopup createPopup()
		{
			 BasicComboPopup popup = new BasicComboPopup(comboBox)
			 {
				  protected JScrollPane createScroller() {
							 return new JScrollPane( list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
										ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
				}//end of method createScroller
			 };
			 return popup;
		}//end of method createPopup

	 }

}
