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
package org.netbeans.modules.uml.ui.swing.treetable;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
//This has to be uncommnted after resolving cyclic dependency issues
//import org.netbeans.modules.uml.ui.products.ad.addesigncentergui.designpatternaddin.WizardRoles;
import org.netbeans.modules.uml.ui.swing.propertyeditor.PropertyEditor;

/**
 * @author sumitabhk
 *
 */
public class JDescribeButton extends JPanel implements ActionListener
{
	private JButton m_Button = null;
	private JTextField m_Text = null;
	private int m_Row = 0;
	private PropertyEditor m_editor = null;
	//This has to be uncommnted after resolving cyclic dependency issues 
	//private WizardRoles m_RolesClazz = null;

	/**
	 *
	 */
	public JDescribeButton(PropertyEditor editor)
	{
		super();
		m_editor = editor;
//		GridBagLayout gbl = new GridBagLayout();
//		double[] vals = {1.0, 0.0};
//		gbl.columnWeights = vals;
//		GridBagConstraints textConstraints = new GridBagConstraints();
//		GridBagConstraints buttonConstraints = new GridBagConstraints();
//		textConstraints.fill = GridBagConstraints.BOTH;
//		buttonConstraints.gridx = GridBagConstraints.CENTER;
//		buttonConstraints.gridy = GridBagConstraints.CENTER;
//		this.setLayout(gbl);
//		gbl.invalidateLayout(this);
//		this.doLayout();
//
//		m_Text = new JTextField();
//		m_Text.setEditable(false);
//		m_Text.setEnabled(false);
//		gbl.setConstraints(m_Text, textConstraints);
//		m_Button = new JButton("+");
//		m_Button.setPreferredSize(new Dimension(10,10));
//		gbl.setConstraints(m_Button, buttonConstraints);
//		add(m_Text);
//		add(m_Button);
		initialize();
	}

	public JDescribeButton(int row)
	{
		super();
		initialize();
		m_Row = row;
	}

	public JDescribeButton(int row, PropertyEditor editor)
	{
		super();
		initialize();
		m_Row = row;
		m_editor = editor;
	}
	//This has to be uncommnted after resolving cyclic dependency issues 
//	public JDescribeButton(int row, WizardRoles rolesClazz)
//	{
//		super();
//		initialize();
//		//m_RolesClazz = rolesClazz;
//		m_Row = row;
//	}
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (m_editor != null)
		{
			IPropertyElement pEle = m_editor.getElementAtGridRow(m_Row);
			IPropertyDefinition def = pEle.getPropertyDefinition();
			m_editor.onPopupCreate(m_Row, pEle);
		}
		//else if (m_RolesClazz != null)
		//{
		//	m_RolesClazz.onCellButtonClickGrid(m_Row, 0);
		//}
	}

	private void initialize()
	{
//		java.awt.GridBagConstraints gridBagConstraints;
//
//		m_Text = new javax.swing.JTextField();
		m_Button = new javax.swing.JButton("+");
		m_Button.setFocusable(false);
//
//		setLayout(new java.awt.GridBagLayout());
//
//		//setMaximumSize(new java.awt.Dimension(100, 15));
//		m_Text.setEditable(false);
//		//m_Text.setMinimumSize(new java.awt.Dimension(40, 10));
//		m_Text.setEnabled(false);
//		m_Text.setBorder(null);
//
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.weightx = 1.0;
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 0;
//		add(m_Text, gridBagConstraints);
//
//		//m_Button.setText("+");
		m_Button.setIcon(null);
		m_Button.setPreferredSize(new java.awt.Dimension(25, 15));
//		m_Button.setVerticalTextPosition(SwingConstants.TOP);
//		m_Button.setHorizontalTextPosition(SwingConstants.RIGHT);
//		m_Button.setBorderPainted(false);
//		m_Button.setContentAreaFilled(false);
//		java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
//		constraints.gridx = 1;
//		constraints.gridy = 0;
//		constraints.anchor = GridBagConstraints.EAST;
//		add(m_Button, constraints);

	    m_Button.setBorder(BorderFactory.createEtchedBorder());
		setLayout(new BorderLayout());
		m_Button.addActionListener(this);
		Box pane = Box.createHorizontalBox();
		pane.add(Box.createHorizontalGlue());
		pane.add(m_Button);
		add(pane, BorderLayout.CENTER);
	}
}



