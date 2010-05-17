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



package org.netbeans.modules.uml.ui.controls.newdialog;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;


/**
 * @author sumitabhk
 *
 */
public class NewWorkspaceUI extends NewDialogWizardPage implements INewDialogUI
{
	private String origLocation = null;

	private static final String PG_CAPTION = NewDialogResources.getString("NewDiagramUI.NEWWIZARD_CAPTION");
	private static final String PG_TITLE = NewDialogResources.getString("IDS_CREATEWORKSPACE");
	private static final String PG_SUBTITLE = NewDialogResources.getString("IDS_CREATEWORKSPACEHELP");


	public NewWorkspaceUI(IWizardSheet parent, String caption, String headerTitle, String headerSubTitle) {
		super(parent, caption, headerTitle, headerSubTitle);
		createUI();
	}

	public NewWorkspaceUI(IWizardSheet parent) {
		this(parent, PG_CAPTION, PG_TITLE, PG_SUBTITLE);
	}

	protected void createUI() 
	{
		m_title.setText(PG_TITLE);
		m_subTitle.setText(PG_SUBTITLE);
		super.createUI();
		m_headerImage.setIcon(NewDialogUtilities.getIconForResource("Workspace"));

		java.awt.GridBagConstraints gridBagConstraints;

		jPanel1 = new javax.swing.JPanel();
		jLabel3 = new javax.swing.JLabel();
		jTextFieldName = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jLocationText = new javax.swing.JTextField();
		jButton3 = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jTextFieldWorkspace = new javax.swing.JTextField();
		jPanel3 = new javax.swing.JPanel();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		jLabel5 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		jLabel7 = new javax.swing.JLabel();

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jPanel1.setOpaque(false);
		jLabel3.setFont(new java.awt.Font("Dialog", 0, 12));
		jLabel3.setText(NewDialogResources.determineText(NewDialogResources.getString("IDS_WORKSPACENAME")));
		NewDialogResources.setMnemonic(jLabel3, NewDialogResources.getString("IDS_WORKSPACENAME"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 0.1;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLabel3, gridBagConstraints);

		jTextFieldName.setText("jTextField1");
		jLabel3.setLabelFor(jTextFieldName);
		//jTextFieldName.setPreferredSize(new java.awt.Dimension(120, 20));

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 0.1;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jTextFieldName, gridBagConstraints);

		jLabel2.setFont(new java.awt.Font("Dialog", 0, 12));
		jLabel2.setText(NewDialogResources.determineText(NewDialogResources.getString("IDS_LOCATION")));
		NewDialogResources.setMnemonic(jLabel2, NewDialogResources.getString("IDS_LOCATION"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 0.1;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLabel2, gridBagConstraints);

		jLocationText.setText("jLocationText");
		jLabel2.setLabelFor(jLocationText);
		//jLocationText.setPreferredSize(new java.awt.Dimension(120, 20));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.weightx = 0.1;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLocationText, gridBagConstraints);

		jButton3.setFont(new java.awt.Font("Dialog", 1, 10));
		jButton3.setText("...");
		jButton3.setPreferredSize(new java.awt.Dimension(20, 20));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
		gridBagConstraints.weightx = 0.1;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jButton3, gridBagConstraints);
		
		jButton3.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				performFileChooserAction();
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		m_DetailPanel.setBorder(new TitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 12)));
		m_DetailPanel.add(jPanel1, gridBagConstraints);
	}

	public void onSetActive()
	{
		super.onSetActive();
		getParentSheet().setButtonEnabled(IWizardSheet.PSWIZB_FINISH, true);
		getParentSheet().setButtonEnabled(IWizardSheet.PSWIZB_NEXT, false);
		loadComponents();
	}

	public boolean onDismiss() 
	{
		INewDialogTabDetails details = getResults();
		NewDialogResultProcessor processor = new NewDialogResultProcessor();
		processor.handleResult(details);
		IWizardSheet parent = getParentSheet();
		if (parent != null && parent instanceof JDefaultNewDialog)
		{
			((JDefaultNewDialog)parent).setResult(details);
		}
		return super.onDismiss();
	}

	protected boolean onInitDialog() 
	{
		return super.onInitDialog();
	}

	public void onWizardBack() 
	{
		super.onWizardBack();
	}

	public void onWizardNext() 
	{
		//super.onWizardNext();
	}

	/**
	 * 
	 */
//	public NewWorkspaceUI()
//	{
//		super();
//		initComponents();
//		loadComponents();
//		jTextFieldName.selectAll();
//		jTextFieldName.requestFocus();
//		setPreferredSize(new Dimension(400, 400));      
//		jTextFieldName.addKeyListener(new WorkspaceUIKeyListener());
//		jLocationText.addFocusListener(new WorkspaceUIFocusListener());
//	}
//
//	public NewWorkspaceUI(INewDialogWorkspaceDetails details)
//	{
//		super();
//		initComponents();
//		if (details != null)
//		{
//			String name = details.getName();
//			//set default element name to blank
//			jTextFieldName.setText(name);
//		
//			String loc = details.getLocation();
//			if (loc == null || loc.length() == 0)
//			{
//				//get home location or preference location.
//				jLocationText.setText(NewDialogUtilities.getDefaultWorkspaceLocation());
//			}
//			else
//			{
//				jLocationText.setText(loc);
//			}
//		}
//		else
//		{
//			loadComponents();
//		}
//		jTextFieldWorkspace.setText("Workspace");
//		jTextFieldName.selectAll();
//		jTextFieldName.requestFocus();
//		setPreferredSize(new Dimension(400, 400));      
//		jTextFieldName.addKeyListener(new WorkspaceUIKeyListener());
//		jLocationText.addFocusListener(new WorkspaceUIFocusListener());
//	}

	/**
	 * Loads the combo boxes and initializes the default names
	 */
	private void loadComponents()
	{
		//set default element name to blank
		jTextFieldName.setText(" ");
		
		//get home location or preference location.
		jLocationText.setText(NewDialogUtilities.getDefaultWorkspaceLocation());
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		jPanel1 = new javax.swing.JPanel();
		jLabel3 = new javax.swing.JLabel();
		jTextFieldName = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jLocationText = new javax.swing.JTextField();
		jButton3 = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jTextFieldWorkspace = new javax.swing.JTextField();
		jPanel3 = new javax.swing.JPanel();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		jLabel5 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		jLabel7 = new javax.swing.JLabel();

		setLayout(new java.awt.GridBagLayout());

//		addWindowListener(new java.awt.event.WindowAdapter() {
//			public void windowClosing(java.awt.event.WindowEvent evt) {
//				closeDialog(evt);
//			}
//		});

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jPanel1.setOpaque(false);
		jLabel3.setFont(new java.awt.Font("Dialog", 0, 12));
		jLabel3.setText(NewDialogResources.getString("IDS_WORKSPACENAME"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 0.1;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLabel3, gridBagConstraints);

		jTextFieldName.setText("jTextField1");
		//jTextFieldName.setPreferredSize(new java.awt.Dimension(120, 20));

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 0.1;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jTextFieldName, gridBagConstraints);

		jLabel2.setFont(new java.awt.Font("Dialog", 0, 12));
		jLabel2.setText(NewDialogResources.getString("IDS_LOCATION"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 0.1;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLabel2, gridBagConstraints);

		jLocationText.setText("jLocationText");
		//jLocationText.setPreferredSize(new java.awt.Dimension(120, 20));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.weightx = 0.1;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLocationText, gridBagConstraints);

		jButton3.setFont(new java.awt.Font("Dialog", 1, 10));
		jButton3.setText("...");
		jButton3.setPreferredSize(new java.awt.Dimension(20, 20));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
		gridBagConstraints.weightx = 0.1;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jButton3, gridBagConstraints);
		
		jButton3.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				performFileChooserAction();
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		//gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(jPanel1, gridBagConstraints);

//		jPanel2.setLayout(new java.awt.GridBagLayout());

//		jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
//		jLabel1.setFont(new java.awt.Font("Dialog", 0, 12));
//		jLabel1.setText("Adding");
//		jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
//		jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 0;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//		gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
//		jPanel2.add(jLabel1, gridBagConstraints);
//
//		jTextFieldWorkspace.setEditable(false);
//		jTextFieldWorkspace.setBorder(null);
//		jTextFieldWorkspace.setText("jTextField2");
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 1;
//		gridBagConstraints.gridy = 0;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
//		jPanel2.add(jTextFieldWorkspace, gridBagConstraints);
//
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 0;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
//		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//		add(jPanel2, gridBagConstraints);
//
//		jPanel3.setLayout(new java.awt.GridBagLayout());
//
//		jButton1.setText("Finish");
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 6;
//		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
//		jPanel3.add(jButton1, gridBagConstraints);
//		jButton1.addActionListener(new java.awt.event.ActionListener() {
//			public void actionPerformed(java.awt.event.ActionEvent evt) {
//				jTextField1ActionPerformed(evt);
//			}
//		});
//
//		jButton2.setText("Cancel");
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 1;
//		gridBagConstraints.gridy = 6;
//		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
//		jPanel3.add(jButton2, gridBagConstraints);
//		jButton2.addActionListener(new java.awt.event.ActionListener() {
//			public void actionPerformed(java.awt.event.ActionEvent evt) {
//				closeDialog();
//			}
//		});
//
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 4;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
//		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//		add(jPanel3, gridBagConstraints);

		doLayout();
	}

	private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) 
	{
		try {
			INewDialogTabDetails result = getResults();
			NewDialogResultProcessor processor = new NewDialogResultProcessor();
			processor.handleResult(result);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
    
	/** Closes the dialog */
	private void closeDialog() 
	{
		setVisible(false);
		Container parent = getTopLevelAncestor();
		if (parent != null && parent instanceof JFrame)
		{
			((JFrame)parent).dispose();
		}
	}
    
	// Variables declaration - do not modify
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JTextField jTextFieldName;
	private javax.swing.JTextField jTextFieldWorkspace;
	private javax.swing.JTextField jLocationText;
	// End of variables declaration

	/**
	 * @param selOnTab
	 */
	public void setElementType(String selOnTab)
	{
		jTextFieldWorkspace.setText(selOnTab);
	}

	public INewDialogTabDetails getResults()
	{
		INewDialogWorkspaceDetails details = new NewDialogWorkspaceDetails();
		if (validData())
		{
			String location = jLocationText.getText();
			String name = jTextFieldName.getText();
			
			details.setLocation(location);
			details.setName(name);
		}
		return details;
	}

	/**
	 * @return
	 */
	private boolean validData()
	{
		boolean valid = true;
		
		//make sure that the name and location is entered
		if (jLocationText.getText() == null || 
			jLocationText.getText().length() == 0)
		{
			valid = false;
		}
		
		if (jTextFieldName.getText() == null || 
			jTextFieldName.getText().length() == 0)
		{
			valid = false;
		}
		
		return valid;
	}

	/* 
	 * User should have an option of creating project by clicking next.
	 */
	public JPanel nextButtonClicked()
	{
		NewProjectUI newUI = null;
		try {
			INewDialogTabDetails result = getResults();
			NewDialogResultProcessor processor = new NewDialogResultProcessor();
//			boolean isHandled = processor.handleResult(result);
			processor.handleResult(result);
//			if (isHandled)
//			{
////				newUI = new NewProjectUI();
////				newUI.setElementType("Project");
//			}
//			else
//			{
//			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return newUI;
	}

	/* 
	 * This method should get user inputs and create workspace
	 */
	public INewDialogTabDetails finishButtonClicked()
	{
		INewDialogTabDetails result = null;
		try {
			result = getResults();
//			NewDialogResultProcessor processor = new NewDialogResultProcessor();
//			boolean isHandled = processor.handleResult(result);
//			if (isHandled)
//			{
//				closeDialog();
//			}
//			else
//			{
//				ETSystem.out.println("Error while creating workspace");
//			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogUI#getHelpText()
	 */
	public String getHelpText()
	{
		String text = NewDialogResources.getString("IDS_ADDING") + jTextFieldWorkspace.getText(); 
		return  text;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogUI#getHelpIcon()
	 */
	public Icon getHelpIcon()
	{
		Icon icon = null;
		icon = NewDialogUtilities.getIconForResource(jTextFieldWorkspace.getText());
		return icon;
	}
	
	/*
	 * This method shows a file chooser dialog and then sets the workspace
	 * location as seleted by user 
	 */
	private void performFileChooserAction()
	{
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		if (fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION)
		{
		   File file = fc.getSelectedFile();
		   if (file != null)
		   {
				String fileName = file.getAbsolutePath();
				jLocationText.setText(fileName);
		   }
		}
	}

	public boolean enableNextButton()
	{
		return true;
	}
	
	private class WorkspaceUIFocusListener implements FocusListener
	{
		public void focusGained(FocusEvent arg0) 
		{
			
		}

		public void focusLost(FocusEvent arg0) 
		{
			origLocation = jLocationText.getText();
		}
	}
	
	private class WorkspaceUIKeyListener implements KeyListener
	{
		public WorkspaceUIKeyListener()
		{
			origLocation = jLocationText.getText();
		}

		public void keyTyped(KeyEvent arg0) 
		{
		}

		public void keyPressed(KeyEvent arg0) {
				
		}

		public void keyReleased(KeyEvent arg0) 
		{
			//I want to update the location text
			if (jLocationText != null)
			{
				String str = origLocation + jTextFieldName.getText();
				jLocationText.setText(str);
			}
		}
	}

}



