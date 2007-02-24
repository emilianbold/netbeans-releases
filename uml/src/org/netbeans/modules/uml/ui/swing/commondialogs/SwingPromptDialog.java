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



package org.netbeans.modules.uml.ui.swing.commondialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.ui.support.commondialogs.IPromptDialog;

/**
 * @author sumitabhk
 *
 */
public class SwingPromptDialog extends JCenterDialog implements IPromptDialog
{
	private boolean m_accept = false;
	private JLabel m_name = null;
	private JTextField m_editName = null;
	protected JButton m_cancel = null;
	protected JButton m_ok = null;

	/**
	 * 
	 */
	public SwingPromptDialog()
	{
		super();
		init();
	}
	
	protected void init()
	{
		getContentPane().setLayout(new BorderLayout());
		Box editPanel = Box.createVerticalBox();
		editPanel.add(Box.createVerticalStrut(10));
		m_name = new JLabel();
		editPanel.add(m_name);
		editPanel.add(Box.createVerticalStrut(10));
		m_editName = new JTextField();
		editPanel.add(m_editName);
		getContentPane().add(editPanel, BorderLayout.CENTER);
	
	
		// Add buttons
		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		m_ok = new JButton();
		m_ok.setText(DefaultCommonDialogResource.getString("IDS_OK"));
		buttonPanel.add(m_ok, BorderLayout.WEST);
		m_ok.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					m_accept = true;
					dispose();
				}
			}
		);
	
		m_cancel = new JButton();
		m_cancel.setText(DefaultCommonDialogResource.getString("IDS_CANCEL"));
		buttonPanel.add(m_cancel, BorderLayout.EAST);
		m_cancel.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					m_accept = false;
					dispose();
				}
			}
		);
		
		setTitle(DefaultCommonDialogResource.getString("IDS_FONT"));
		setSize(300, 123);
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.commondialogs.IPromptDialog#displayEdit(java.lang.String, java.lang.String, boolean, java.lang.StringBuffer, int, java.lang.String)
	 */
	public ETPairT<Boolean, String> displayEdit(String sMessage, String sInitalValue, String sTitle)
	{
		SwingPromptDialog pSwingPromptDialog = new SwingPromptDialog();
		pSwingPromptDialog.setTitle(sTitle);
		pSwingPromptDialog.m_name.setText(sMessage);
		pSwingPromptDialog.m_editName.setText(sInitalValue);
		pSwingPromptDialog.setModal(true);
		pSwingPromptDialog.show();

		ETPairT<Boolean, String> retVal = new ETPairT<Boolean, String>();
		retVal.setParamOne(new Boolean(pSwingPromptDialog.m_accept));
		retVal.setParamTwo(pSwingPromptDialog.m_editName.getText());
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.commondialogs.IPromptDialog#displayEdit2(java.lang.String, int, int, int, boolean, int, int, java.lang.String)
	 */
	public long displayEdit2(String sMessage, int nInitialValue, int nMinValue, int nMaxValue, boolean bUserHitOK, int pResult, int parent, String sTitle)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.commondialogs.IPromptDialog#displayPassword(java.lang.String, boolean, java.lang.StringBuffer, int, java.lang.String)
	 */
	public long displayPassword(String sMessage, boolean bUserHitOK, StringBuffer pResult, int parent, String sTitle)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.commondialogs.ISilentDialog#isRunSilent()
	 */
	public boolean isRunSilent()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.commondialogs.ISilentDialog#setIsRunSilent(boolean)
	 */
	public void setIsRunSilent(boolean value)
	{
		// TODO Auto-generated method stub
		
	}

}



