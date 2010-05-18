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
 * Created on Jul 15, 2003
 *
 */
package org.netbeans.modules.uml.ui.controls.newdialog;

import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.netbeans.modules.uml.ui.support.wizard.IWizardPage;
import org.netbeans.modules.uml.ui.support.wizard.WizardSheet;

/**
 * @author sumitabhk
 *
 */
public class JDefaultNewDialog extends WizardSheet implements ActionListener
{
	private IWizardPage m_FirstPage = null;
	private IWizardPage m_SecondPage = null;
	private IWizardPage m_ThirdPage = null;
	
	// Variables declaration - do not modify
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	private javax.swing.JButton jButton4;
	private javax.swing.JPanel jButtonsPanel;
	private javax.swing.JPanel jHelpPanel;
	private javax.swing.JTextArea jTextArea1;
	private JLabel jLabel1 = null;
	private javax.swing.JPanel jUIPanel;

	private JTabbedPane m_TabbedPane = null;
	private boolean m_FirstScreen = true;
	private INewDialogUI m_ShownUI = null;
	private GridBagConstraints m_Constraints = null;
	
	private INewDialogTabDetails m_Result = null;
	// End of variables declaration


	public JDefaultNewDialog(int nIDCaption, Frame pParentWnd, int iSelectPage, Icon hbmWatermark, GraphicsConfiguration hpalWatermark, Icon hbmHeader) {
		this("", pParentWnd, iSelectPage, hbmWatermark, hpalWatermark, hbmHeader); //$NON-NLS-1$
	}

	public JDefaultNewDialog(String pszCaption, Frame pParentWnd, int iSelectPage, Icon hbmWatermark, GraphicsConfiguration hpalWatermark, Icon hbmHeader) {
		super(pszCaption, pParentWnd, iSelectPage, hbmWatermark, hpalWatermark, hbmHeader);
		init(hbmWatermark, hpalWatermark, hbmHeader);
	}

	public JDefaultNewDialog(Frame frame, String title, boolean modal) {
		super(frame, title, modal);
	}

	public JDefaultNewDialog() {
		super();
	}

        public void init(Icon hbmWatermark, GraphicsConfiguration hpalWatermark, Icon hbmHeader)
        {
            if (hbmWatermark != null && hbmHeader != null)
            {
                super.init(hbmWatermark, hpalWatermark, hbmHeader);
            }
            
            else
            {
                // just load up the default images
//                super.init(new ImageIcon(JDefaultNewDialog.class.getResource(
//                    NewDialogResources.getString("JDefaultNewDialog.WATERMARK"))), // NOI18N
//                    null,
//                    new ImageIcon(JDefaultNewDialog.class.getResource(
//                    NewDialogResources.getString("JDefaultNewDialog.BANNER")))); // NOI18N
            }
        }

	public void init(Icon hbmWatermark, GraphicsConfiguration hpalWatermark, Icon hbmHeader, INewDialogTabDetails details) 
	{
		this.init(hbmWatermark, hpalWatermark, hbmHeader);

		initWithDefaults(details);
	}

	/**
	 * 
	 */
//	public JDefaultNewDialog()
//	{
//		super();
//		setSize(450, 450);
//		m_TabbedPane = new JTabbedPane();
//		initComponents();
//		showFirstScreen();
//		doLayout();
//		setVisible(true);
//
//		addWindowListener(new WindowAdapter()
//		{
//		   public void windowClosed(WindowEvent e)
//		   {
//			  //System.exit(0);
//		   }
//
//		   public void windowClosing(WindowEvent e)
//		   {
//			  dispose();
//		   }
//
//		});
//      
//      setModal(true);
//	}
//
//	public JDefaultNewDialog(Frame parent)
//	{
//		super(parent, true);
//		setSize(450, 450);
//		m_TabbedPane = new JTabbedPane();
//		initComponents();
//		doLayout();
//
//		addWindowListener(new WindowAdapter()
//		{
//		   public void windowClosed(WindowEvent e)
//		   {
//			  //System.exit(0);
//		   }
//
//		   public void windowClosing(WindowEvent e)
//		   {
//			  dispose();
//		   }
//
//		});
//	}

	/**
	 * @param pValidateProcessor
	 */
	public void init(INewDialogValidateProcessor pValidateProcessor)
	{
		// TODO Auto-generated method stub
		
	}
	public void initWithDefaults(INewDialogTabDetails details)
	{
		if (details != null)
		{
			if (details instanceof INewDialogDiagramDetails)
			{
				//m_FirstPage = new NewDiagramSelectionUI(this, details);
				m_FirstPage = new NewDiagramUI(this, details);
				this.addPage(m_FirstPage, "Select Diagram Kind To Create"); //$NON-NLS-1$
				//this.addPage(m_SecondPage, "Create Diagram");
			}
			else if (details instanceof INewDialogElementDetails)
			{
				//m_FirstPage = new NewElementSelectionUI(this, details);
				m_FirstPage = new NewElementUI(this, details);
				this.addPage(m_FirstPage, "Select Element Kind To Create"); //$NON-NLS-1$
				//this.addPage(m_SecondPage, "Create Element");
			}
			else if (details instanceof INewDialogPackageDetails)
			{
				m_FirstPage = new NewPackageUI(this, (INewDialogPackageDetails)details);
				this.addPage(m_FirstPage, "Create Package"); //$NON-NLS-1$
			}
			else if (details instanceof INewDialogProjectDetails)
			{
				m_FirstPage = new NewProjectUI(this);
				this.addPage(m_FirstPage, "Create Project"); //$NON-NLS-1$
			}
			else if (details instanceof INewDialogWorkspaceDetails)
			{
				m_FirstPage = new NewWorkspaceUI(this);
				m_SecondPage = new NewProjectUI(this);
				this.addPage(m_FirstPage, "Create Workspace"); //$NON-NLS-1$
				this.addPage(m_SecondPage, "Create Project"); //$NON-NLS-1$
			}
		}
		else
		{
			m_FirstPage = new ElementSelectionUI(this);
			this.addPage(m_FirstPage, "Select Type"); //$NON-NLS-1$
		}

		this.setActivePage(0);
	}

	public void setNextPage(IWizardPage page, String name)
	{
		this.addPage(page, name);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0)
	{
		// TODO Auto-generated method stub
		//set the default tab to selected tab at this time.
		// set the results.
		Object source = arg0.getSource();
		if (source.equals(jButton1))
		{
			if (!m_FirstScreen)
			{
				showFirstScreen();
				setupHelpText();
			}
		}
		else if (source.equals(jButton2))
		{
			if (m_ShownUI != null)
			{
				JPanel panel = m_ShownUI.nextButtonClicked();
				if (panel != null)
				{
					if (m_Constraints == null)
					{
						m_Constraints = new GridBagConstraints();
						m_Constraints.fill = GridBagConstraints.BOTH;
						m_Constraints.weightx = 0.7;
						m_Constraints.weighty = 0.7;
					}
					jUIPanel.removeAll();
					jUIPanel.add(panel, m_Constraints);
					if (panel instanceof INewDialogUI)
					{
						m_ShownUI = null;
						m_ShownUI = (INewDialogUI)panel;
					}
					m_FirstScreen = false;
					enableDisableButtons();
					setupHelpText();
					doLayout();
					pack();
					setVisible(true);
					show();
				}
			}
		}
		else if (source.equals(jButton3))
		{
			if (m_ShownUI != null)
			{
				if(m_ShownUI instanceof JPanel)
				{
					JPanel panel = (JPanel)m_ShownUI;
					m_Result = m_ShownUI.finishButtonClicked();
					//if the ui has closed itself after finish button processing, 
					//we should close this dialog.
//					if (!panel.isShowing())
//					{
//						closeDialog(null);
//					}
//					
//					//now dispose the dialog
//					dispose();
					closeDialog(null);
				}
			}
		}
		else if (source.equals(jButton4))
		{
			//cancel button clicked - close dialog
			dispose();
			//System.exit(0);
		}
	}
	
	/**
	 * 
	 */
	private void setupHelpText()
	{
		if (m_FirstScreen)
		{
			jTextArea1.setText("Select item to add"); //$NON-NLS-1$
			jLabel1.setIcon(null);
		}
		else
		{
			jTextArea1.setText(m_ShownUI.getHelpText());
			Icon icon = m_ShownUI.getHelpIcon();
			jLabel1.setIcon(icon);
		}
	}

	/**
	 * 
	 */
	private void showFirstScreen()
	{
		jUIPanel.removeAll();
		m_ShownUI = null;//new ElementSelectionUI();
		if (m_ShownUI instanceof JPanel)
		{
			if (m_Constraints == null)
			{
				m_Constraints = new GridBagConstraints();
				m_Constraints.fill = GridBagConstraints.BOTH;
				m_Constraints.weightx = 0.7;
				m_Constraints.weighty = 0.7;
			}
			jUIPanel.add((JPanel)m_ShownUI, m_Constraints);
		}
		m_FirstScreen = true;
		enableDisableButtons();
		setupHelpText();
		doLayout();
		pack();
		show();
	}
	
	public INewDialogTabDetails getResult()
	{
		return m_Result;
	}

	public void setResult(INewDialogTabDetails result)
	{
		m_Result = result;
	}

	public void enableDisableButtons()
	{
		if (m_FirstScreen)
		{
			jButton1.setEnabled(false); //disable Back button
			jButton2.setEnabled(true);  //enable Next button
			jButton3.setEnabled(false); //disable Finish button
			jButton4.setEnabled(true);  //enable Cancel button
		}
		else
		{
			jButton1.setEnabled(true);
			if (m_ShownUI != null)
			{
				boolean val = m_ShownUI.enableNextButton();
				jButton2.setEnabled(val);
			}
			else
			{
				jButton2.setEnabled(false);
			}
			jButton3.setEnabled(true);
			jButton4.setEnabled(true);
		}
	}

	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		jHelpPanel = new javax.swing.JPanel();
		jTextArea1 = new javax.swing.JTextArea();
		jUIPanel = new javax.swing.JPanel();
		jButtonsPanel = new javax.swing.JPanel();
		jButton1 = new javax.swing.JButton();		
		jButton2 = new javax.swing.JButton();
		jButton3 = new javax.swing.JButton();
		jButton4 = new javax.swing.JButton();
		jLabel1 = new JLabel();

		//none of these buttons should steal focus
		jButton1.setFocusable(false);
		jButton2.setFocusable(false);
		jButton3.setFocusable(false);
		jButton4.setFocusable(false);

		getContentPane().setLayout(new java.awt.GridBagLayout());

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jHelpPanel.setLayout(new java.awt.GridBagLayout());

		jHelpPanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.weighty = 0.5;
		//gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
		jHelpPanel.add(jTextArea1, gridBagConstraints);
		jTextArea1.setWrapStyleWord(true);
		jTextArea1.setLineWrap(true);
		jTextArea1.setAutoscrolls(true);
		jTextArea1.setEnabled(false);
		jTextArea1.setEditable(false);
		jTextArea1.setRows(3);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.ipadx = 10;
		jHelpPanel.add(jLabel1, gridBagConstraints);
		jLabel1.setBackground(jTextArea1.getBackground());
		jLabel1.setOpaque(true);
		jLabel1.setEnabled(true);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
		gridBagConstraints.weightx = 0.2;
		gridBagConstraints.weighty = 0.2;
		getContentPane().add(jHelpPanel, gridBagConstraints);

		jUIPanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
		gridBagConstraints.weightx = 0.7;
		gridBagConstraints.weighty = 0.7;
		getContentPane().add(jUIPanel, gridBagConstraints);

		jButtonsPanel.setLayout(new java.awt.GridBagLayout());

		jButton1.setText(NewDialogResources.getString("JDefaultNewDialog.BACK_BTN")); //$NON-NLS-1$
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
		jButtonsPanel.add(jButton1, gridBagConstraints);
		jButton1.addActionListener(this);

		jButton2.setText(NewDialogResources.getString("JDefaultNewDialog.NEXT_BTN")); //$NON-NLS-1$
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
		jButtonsPanel.add(jButton2, gridBagConstraints);
		jButton2.addActionListener(this);

		jButton3.setText(NewDialogResources.getString("JDefaultNewDialog.FINSH_BTN")); //$NON-NLS-1$
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
		jButtonsPanel.add(jButton3, gridBagConstraints);
		jButton3.addActionListener(this);

		jButton4.setText(NewDialogResources.getString("JDefaultNewDialog.CANCEL_BTN")); //$NON-NLS-1$
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
		jButtonsPanel.add(jButton4, gridBagConstraints);
		jButton4.addActionListener(this);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		getContentPane().add(jButtonsPanel, gridBagConstraints);

		pack();
		enableDisableButtons();
	}

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) {
		setVisible(false);
		dispose();
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.wizard.WizardSheet#canAddNavigationButtons()
    */
   protected boolean canAddNavigationButtons()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.wizard.WizardSheet#getCommitButtonCaption()
    */
   protected String getCommitButtonCaption()
   {
      return NewDialogResources.getString("JDefaultNewDialog.OK_BTN"); //$NON-NLS-1$
   }

}



