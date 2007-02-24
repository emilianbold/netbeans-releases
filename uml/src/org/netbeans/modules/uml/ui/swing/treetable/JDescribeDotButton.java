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
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.ui.swing.preferencedialog.PreferenceDialogUI;

/**
 * @author sumitabhk
 *
 */
public class JDescribeDotButton extends JPanel implements ActionListener
{
	private JButton m_Button = null;
	private JTextField m_TextField = null;
	private String m_Text = "";
	private int m_Row = 0;
	private Object m_Object = null;
	private Font m_Font = null;
	private Color m_Color = null;
	
	public JDescribeDotButton(int row, Object obj)
	{
		super();
		m_Row = row;
		m_Object = obj;
		initialize();
	}
	
	public JDescribeDotButton(int row, Object obj, String text)
	{
		super();
		m_Row = row;
		m_Object = obj;
		m_Text = text;
		initialize();
	}

	public JDescribeDotButton(int row, Object obj, String text, Font f, Color c)
	{
		super();
		m_Row = row;
		m_Object = obj;
		m_Text = text;
		if (f != null)
		{
			m_Font = f;
		}
		if (c != null)
		{
			m_Color = c;
		}
		initialize();
	}
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		Object obj = e.getSource();
		if (m_Object != null)
		{
			if (m_Object instanceof PreferenceDialogUI)
			{
				PreferenceDialogUI ui = (PreferenceDialogUI)m_Object;
				IPropertyElement pEle = ui.getElementAtGridRow(m_Row);
				if (pEle != null)
				{
					ui.onCellButtonClicked(m_Row, pEle);
					//ui.refreshFontElement(pEle);
				}
			}
		}
	}

	private void initialize()
	{
		m_TextField = new javax.swing.JTextField(m_Text);
		m_TextField.setEditable(false);
		m_TextField.setBorder(null);
		if (m_Font != null)
		{
			m_TextField.setFont(m_Font);
		}
		else
		{
			m_TextField.setFont(new java.awt.Font("Dialog", 0, 11));
		}
		if (m_Color != null)
		{
			m_TextField.setText("");
			m_TextField.setBackground(m_Color);
		}
		
		m_Button = new javax.swing.JButton("...");
		m_Button.setIcon(null);
		m_Button.setPreferredSize(new java.awt.Dimension(25, 15));
	    m_Button.setBorder(BorderFactory.createEtchedBorder());
		setLayout(new BorderLayout());
		m_Button.addActionListener(this);
		Box pane = Box.createHorizontalBox();
		pane.add(Box.createHorizontalGlue());
		pane.add(m_TextField);
		pane.add(m_Button);
		add(pane, BorderLayout.CENTER);
	}
}


