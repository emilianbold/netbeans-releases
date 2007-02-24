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
 * Created on Feb 25, 2004
 *
 */
package org.netbeans.modules.uml.ui.controls.editcontrol;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.ui.support.drawingproperties.DrawingPropertyResource;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;

import java.awt.event.ActionEvent;

/**
 * @author jingmingm
 *
 */
public class EditControlClassChooser extends JCenterDialog
{
	protected static String m_ClassName = "";
	
	protected JLabel m_selectedClassName = null;
	protected static JLabel m_sampleField = null;
	protected JButton m_cancel = null;
	protected JButton m_ok = null;
	protected static JList m_NameList = null;
	
	protected void init(IStrings strs)
	{
		getContentPane().setLayout(new BorderLayout());
		
      JPanel namePanel = new JPanel();
      namePanel.setLayout(new GridBagLayout());
      
		getContentPane().add(namePanel, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	
		// Add Class name
		m_selectedClassName = new JLabel();
      m_selectedClassName.setText(DrawingPropertyResource.getString("IDS_CHOOSECLASSLABEL"));
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = 0;
      constraints.gridy = 0;
      constraints.insets = new Insets(10, 5, 10, 5);
      constraints.fill = GridBagConstraints.BOTH;
		namePanel.add(m_selectedClassName, constraints);
		
		Object[] names = strs.toArray();
		m_NameList = new JList(names);
      m_NameList.setVisibleRowCount(10);
		m_NameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_NameList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				JList names = (JList)e.getSource();
				m_ClassName = (String)names.getSelectedValue();
			}
		});	
			
		JScrollPane pane = new JScrollPane(m_NameList);
      constraints = new GridBagConstraints();
      constraints.gridx = 0;
      constraints.gridy = 1;
      constraints.fill = GridBagConstraints.BOTH;
      constraints.weightx = 1.0;
      constraints.weighty = 1.0;
      constraints.insets = new Insets(0, 5, 0, 5);
		namePanel.add(pane, constraints);
		
		// Add buttons
		m_ok = new JButton();
		m_ok.setText(DrawingPropertyResource.getString("IDS_OK"));
		buttonPanel.add(m_ok, BorderLayout.WEST);
		m_ok.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					okPressed(e);
				}
			}
		);
		
		m_cancel = new JButton();
		m_cancel.setText(DrawingPropertyResource.getString("IDS_CANCEL"));
		buttonPanel.add(m_cancel, BorderLayout.EAST);
		m_cancel.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					cancelPressed(e);
				}
			}
		);
		setTitle(DrawingPropertyResource.getString("IDS_CHOOSECLASS"));
		setSize(400, 300);
	}
		
	public EditControlClassChooser(IStrings strs)
	{
		init(strs);
	}

	protected void cancelPressed(ActionEvent e)
	{
		m_ClassName = null;
		dispose();
	}
	
	protected void okPressed(ActionEvent e)
	{
		dispose();
	}

   public String selectClass()
   {
      String name = null;
      setModal(true);
      setVisible(true);
      name = m_ClassName;
      return name;
   }
   
}



