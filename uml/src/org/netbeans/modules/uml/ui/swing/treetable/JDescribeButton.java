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



