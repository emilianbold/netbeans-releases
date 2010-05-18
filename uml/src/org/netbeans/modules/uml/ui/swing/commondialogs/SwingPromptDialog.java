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



