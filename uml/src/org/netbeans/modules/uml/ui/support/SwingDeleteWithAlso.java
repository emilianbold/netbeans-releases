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
 * Created on Jun 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.netbeans.modules.uml.ui.support;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.DiagramEngineResources;
import org.netbeans.modules.uml.ui.swing.commondialogs.DefaultCommonDialogResource;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;

/**
 * @author jingmingm
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SwingDeleteWithAlso extends JCenterDialog implements ItemListener
{
	private boolean m_cancled = true;
	private boolean m_affectModel = true;
	private boolean m_also = false;
	private boolean m_never = false;
	private JLabel m_LabelMessage = null;
	private JCheckBox m_CheckboxData = null;
	private JCheckBox m_checkBoxAlso = null;
	private JCheckBox m_CheckboxNever = null;
	protected JButton m_No = null;
	protected JButton m_Yes = null;
   
   private boolean m_bDeleteAlso = false;
	
	public SwingDeleteWithAlso()
	{
		init( true );
	}
	public SwingDeleteWithAlso( String szDeleteConnectorMessages, String szAlsoQuestion, JDialog pParent )
	{
		init( szDeleteConnectorMessages.equals("PSK_ASK") );
      
      setCheckboxText(m_checkBoxAlso, szAlsoQuestion);

      if( szDeleteConnectorMessages.equals("PSK_ALWAYS") )
      {
         m_checkBoxAlso.setSelected( true );
      }
      else if( szDeleteConnectorMessages.equals("PSK_NEVER") )
      {
         m_checkBoxAlso.setSelected( false );
      }
	}
	
	protected void init( boolean bDisplayExtraCheckboxes )
	{
		getContentPane().setLayout(new BorderLayout());
		
		m_LabelMessage = new JLabel( DiagramEngineResources.getString("ADCoreEngine.DELETE_GRAPH_OBJECTS_MESSAGE") );
		String text = DiagramEngineResources.getString("ADCoreEngine.DELETE_ELEMENTS_QUESTION");
		m_CheckboxData = new JCheckBox();
		setCheckboxText(m_CheckboxData, text);
		m_CheckboxData.setSelected(true);
		m_CheckboxData.addItemListener(this);
		m_checkBoxAlso = new JCheckBox();
		m_checkBoxAlso.setSelected(false);
		m_checkBoxAlso.addItemListener(this);
      m_checkBoxAlso.setVisible( bDisplayExtraCheckboxes );
		m_CheckboxNever = new JCheckBox();
		m_CheckboxNever.setSelected(false);
		String dontShow = DefaultCommonDialogResource.getString("IDS_DONT_SHOW");
		setCheckboxText(m_CheckboxNever, dontShow);
		m_CheckboxNever.addItemListener(this);
      m_CheckboxNever.setVisible( bDisplayExtraCheckboxes );
		Box checkBoxes = Box.createVerticalBox();
		checkBoxes.add(Box.createVerticalStrut(6));
		checkBoxes.add(m_LabelMessage);
		checkBoxes.add(Box.createVerticalStrut(6));
		checkBoxes.add(m_CheckboxData);
		checkBoxes.add(Box.createVerticalStrut(3));
		checkBoxes.add(m_checkBoxAlso);
		checkBoxes.add(Box.createVerticalStrut(3));
		//Box neverBox = Box.createHorizontalBox();
		//neverBox.add(Box.createHorizontalStrut(3));
		//neverBox.add(m_CheckboxNever);
		//checkBoxes.add(neverBox);
		checkBoxes.add(m_CheckboxNever);
		getContentPane().add(checkBoxes, BorderLayout.CENTER);
		
		// Add buttons
		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		String yesStr = DefaultCommonDialogResource.getString("IDS_YES");
		m_Yes = new JButton();
		setButtonText(m_Yes, yesStr);
		buttonPanel.add(m_Yes, BorderLayout.WEST);
		m_Yes.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					m_cancled = false;
					dispose();
				}
			}
		);
		
		String noStr = DefaultCommonDialogResource.getString("IDS_NO");
		m_No = new JButton();
		setButtonText(m_No, noStr);
		buttonPanel.add(m_No, BorderLayout.EAST);
		m_No.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					m_cancled = true;
					dispose();
				}
			}
		);
		setTitle(DiagramEngineResources.getString("ADCoreEngine.DELETE_QUESTIONDIALOGTITLE"));
		setSize(520, 180);
		setModal(true);
	}

   public void itemStateChanged(ItemEvent e)
   {
		Object source = e.getItemSelectable();
		if (source == m_CheckboxData)
		{
			m_affectModel = m_CheckboxData.isSelected();
		}
		else if (source == m_checkBoxAlso)
		{
			m_also = m_checkBoxAlso.isSelected();
		}    
		else if (source == m_CheckboxNever)
		{
			m_never = m_CheckboxNever.isSelected();
		}    
   }

	public boolean getCanceled()
	{
		return m_cancled;
	}
	
	public boolean getDeleteModel()
	{
		return m_affectModel;	
	}
	
	public void setDeleteModel(boolean affectDataModel)
	{
		m_CheckboxData.setSelected(affectDataModel);	
	}
	
	public boolean getAlso()
	{
		return m_also;
	}
	
	public boolean getNever()
	{
		return m_never;
	}
	private void setCheckboxText(JCheckBox box, String text)
	{
		String checkboxText = text;
		String under = "";
		int pos = text.indexOf('&');
		if (pos > -1)
		{
			under = text.substring(pos + 1, pos + 2);
			checkboxText = StringUtilities.replaceAllSubstrings(text, "&", "");
		}
		box.setText( checkboxText );
		if (under.length() > 0)
		{
			box.setMnemonic(under.charAt(0));
		}
	}
	private void setButtonText(JButton button, String text)
	{
		String checkboxText = text;
		String under = "";
		int pos = text.indexOf('&');
		if (pos > -1)
		{
			under = text.substring(pos + 1, pos + 2);
			checkboxText = StringUtilities.replaceAllSubstrings(text, "&", "");
		}
		button.setText( checkboxText );
		if (under.length() > 0)
		{
			button.setMnemonic(under.charAt(0));
		}
	}
	
}
