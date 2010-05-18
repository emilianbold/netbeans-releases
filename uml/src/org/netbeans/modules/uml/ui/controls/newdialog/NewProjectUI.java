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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import org.netbeans.modules.uml.core.configstringframework.ConfigStringTranslator;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.netbeans.modules.uml.ui.support.NewProjectKind;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.core.scm.ISCMIntegrator;
import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;
import org.netbeans.modules.uml.ui.support.wizard.WizardInteriorPage;

/**
 * @author sumitabhk
 *
 */
public class NewProjectUI extends WizardInteriorPage implements INewDialogUI
{
	private String origLocation = null;

	private static final String PG_CAPTION = NewDialogResources.getString("NewProjectUI.NEW_PROJECT_CAPTON"); //$NON-NLS-1$
	private static final String PG_TITLE = NewDialogResources.getString("NewProjectUI.NEW_PROJECT_TITLE_DC"); //$NON-NLS-1$
	private static final String PG_SUBTITLE = NewDialogResources.getString("NewProjectUI.NEW_PROJECT_SUBTITLE_DC"); //$NON-NLS-1$


	public NewProjectUI(IWizardSheet parent, String caption, String headerTitle, String headerSubTitle) {
		super(parent, caption, headerTitle, headerSubTitle);
		createUI();
	}

	public NewProjectUI(IWizardSheet parent) {
		this(parent, PG_CAPTION, PG_TITLE, PG_SUBTITLE);
	}

	protected void createUI()
	{
		super.createUI();
		//m_headerImage.setIcon(NewDialogUtilities.getIconForResource("Project")); //$NON-NLS-1$


		java.awt.GridBagConstraints gridBagConstraints;

		jMainPanel = new javax.swing.JPanel();
		jPanel1 = new javax.swing.JPanel();
		jLabel3 = new javax.swing.JLabel();
		jProjectText = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jLocationText = new javax.swing.JTextField();
		jButton3 = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jTextFieldProject = new javax.swing.JTextField();
		jPanel3 = new javax.swing.JPanel();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		jLabel5 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		jComboBox2 = new javax.swing.JComboBox();
		jComboBox1 = new javax.swing.JComboBox();
		jPanel5 = new javax.swing.JPanel();
		jCheckBox2 = new javax.swing.JCheckBox();
		jLabel7 = new javax.swing.JLabel();

		// The only way the new project dialog is accessed from jUML is through the design
		// center and none of the options below apply so commenting out.
      /*
		jMainPanel.setLayout(new java.awt.GridBagLayout());

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jPanel1.setOpaque(false);
		jLabel3.setFont(new java.awt.Font("Dialog", 0, 12)); //$NON-NLS-1$
		jLabel3.setText(NewDialogResources.determineText(NewDialogResources.getString("NewProjectUI.PROJECT_NAME_LBL"))); //$NON-NLS-1$
		NewDialogResources.setMnemonic(jLabel3, NewDialogResources.getString("NewProjectUI.PROJECT_NAME_LBL"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.ipady = 5;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLabel3, gridBagConstraints);

		jProjectText.setText("jProjectText"); //$NON-NLS-1$
		jLabel3.setLabelFor(jProjectText);
		//jProjectText.setPreferredSize(new java.awt.Dimension(120, 20));

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jProjectText, gridBagConstraints);

		jLabel2.setFont(new java.awt.Font("Dialog", 0, 12)); //$NON-NLS-1$
		jLabel2.setText(NewDialogResources.determineText(NewDialogResources.getString("NewProjectUI.PROJECT_LOCATION_LBL"))); //$NON-NLS-1$
		NewDialogResources.setMnemonic(jLabel2, NewDialogResources.getString("NewProjectUI.PROJECT_LOCATION_LBL"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		//gridBagConstraints.ipady = 10;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLabel2, gridBagConstraints);

		jLocationText.setText("jLocationText"); //$NON-NLS-1$
		jLabel2.setLabelFor(jLocationText);
		//jLocationText.setPreferredSize(new java.awt.Dimension(120, 20));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 0.9;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLocationText, gridBagConstraints);

		jButton3.setFont(new java.awt.Font("Dialog", 1, 10)); //$NON-NLS-1$
		jButton3.setText(NewDialogResources.getString("NewProjectUI.NAVIGATION_LBL")); //$NON-NLS-1$
		jButton3.setPreferredSize(new java.awt.Dimension(20, 20));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
		//gridBagConstraints.weightx = 0.1;
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
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.5;
		jMainPanel.add(jPanel1, gridBagConstraints);

		jPanel4.setLayout(new java.awt.GridBagLayout());

		jPanel4.setBorder(new javax.swing.border.TitledBorder(null, NewDialogResources.getString("NewProjectUI.PROJECT_OPTIONS_LBL"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 12))); //$NON-NLS-1$ //$NON-NLS-2$
		jLabel5.setFont(new java.awt.Font("Dialog", 0, 12)); //$NON-NLS-1$
		jLabel5.setText(NewDialogResources.determineText(NewDialogResources.getString("NewProjectUI.MODE_LBL"))); //$NON-NLS-1$
		NewDialogResources.setMnemonic(jLabel5, NewDialogResources.getString("NewProjectUI.MODE_LBL"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.5;
		//gridBagConstraints.weighty = 0.1;
		jPanel4.add(jLabel5, gridBagConstraints);

		jLabel6.setFont(new java.awt.Font("Dialog", 0, 12)); //$NON-NLS-1$
		jLabel6.setText(NewDialogResources.determineText(NewDialogResources.getString("NewProjectUI.DEFUALT_LANGUAGE_LBL"))); //$NON-NLS-1$
		NewDialogResources.setMnemonic(jLabel6, NewDialogResources.getString("NewProjectUI.DEFUALT_LANGUAGE_LBL"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.5;
		//gridBagConstraints.weighty = 0.1;
		jPanel4.add(jLabel6, gridBagConstraints);

		jComboBox2.setEditable(true);
		NewDialogResources.setFocusAccelerator(jComboBox2, NewDialogResources.getString("NewProjectUI.DEFUALT_LANGUAGE_LBL"));
		//jComboBox2.setMinimumSize(new java.awt.Dimension(124, 20));
		//jComboBox2.setPreferredSize(new java.awt.Dimension(124, 20));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		//gridBagConstraints.gridwidth = 2;
		gridBagConstraints.weightx = 0.5;
		//gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
		jPanel4.add(jComboBox2, gridBagConstraints);

		jComboBox1.setEditable(true);
		NewDialogResources.setFocusAccelerator(jComboBox1, NewDialogResources.getString("NewProjectUI.MODE_LBL"));
		//jComboBox1.setMinimumSize(new java.awt.Dimension(124, 20));
		//jComboBox1.setPreferredSize(new java.awt.Dimension(124, 20));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 0.5;
		//gridBagConstraints.weighty = 0.1;
		jPanel4.add(jComboBox1, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.4;
		jMainPanel.add(jPanel4, gridBagConstraints);

		jPanel5.setLayout(new java.awt.GridBagLayout());

		//jCheckBox2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 0.1;
		//gridBagConstraints.weighty = 0.1;
		jPanel5.add(jCheckBox2, gridBagConstraints);
		jCheckBox2.setEnabled(true);
		jCheckBox2.setOpaque(false);
		jCheckBox2.setBorder(null);
		//jCheckBox2.setBorderPaintedFlat(true);
		jCheckBox2.setBackground(Color.WHITE);
      jCheckBox2.setText(NewDialogResources.determineText(NewDialogResources.getString("NewProjectUI.SOURCE_CTRL_LBL"))); //$NON-NLS-1$
		NewDialogResources.setMnemonic(jCheckBox2, NewDialogResources.getString("NewProjectUI.SOURCE_CTRL_LBL"));
		ActionListener action = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JCheckBox check = ((JCheckBox)e.getSource());
				check.requestFocusInWindow();
				check.setSelected(!check.isSelected());
			}
		};
      jCheckBox2.setFont(new java.awt.Font("Dialog", 0, 12)); //$NON-NLS-1$

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 5);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		jMainPanel.add(jPanel5, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		//gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.weighty = 0.5;
		//m_DetailPanel.setBorder(new TitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 12))); //$NON-NLS-1$ //$NON-NLS-2$
		//m_DetailPanel.add(jMainPanel, gridBagConstraints);

      JPanel contentPane = new JPanel();
      contentPane.setBorder(new TitledBorder(null, "",
                                             TitledBorder.DEFAULT_JUSTIFICATION,
                                             TitledBorder.DEFAULT_POSITION,
                                             new Font("Dialog", 0, 12))); //$NON-NLS-1$ //$NON-NLS-2$
      */

		jLabel3.setText(NewDialogResources.determineText(NewDialogResources.getString("NewProjectUI.PROJECT_NAME_LBL"))); //$NON-NLS-1$
		NewDialogResources.setMnemonic(jLabel3, NewDialogResources.getString("NewProjectUI.PROJECT_NAME_LBL"));
		jProjectText.setText("jProjectText"); //$NON-NLS-1$
		jLabel3.setLabelFor(jProjectText);
		jLabel3.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 3, 0)));

		jLabel2.setText(NewDialogResources.determineText(NewDialogResources.getString("NewProjectUI.PROJECT_LOCATION_LBL"))); //$NON-NLS-1$
		NewDialogResources.setMnemonic(jLabel2, NewDialogResources.getString("NewProjectUI.PROJECT_LOCATION_LBL"));
		jLocationText.setText("jLocationText"); //$NON-NLS-1$
		jLabel2.setLabelFor(jLocationText);
		jLabel2.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 3, 0)));

		jButton3.setText(NewDialogResources.getString("NewProjectUI.NAVIGATION_LBL")); //$NON-NLS-1$
		jButton3.setMaximumSize(new java.awt.Dimension(20, 20));
		jButton3.setPreferredSize(new java.awt.Dimension(20, 20));
		jButton3.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				performFileChooserAction();
			}

		});

		pnlContents.setLayout(new GridBagLayout());
		pnlContents.add(jLabel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 205), 32, 0));
		pnlContents.add(jProjectText, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 0), 0, 0));
		pnlContents.add(jLabel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(20, 20, 0, 205), 32, 0));
		pnlContents.add(jLocationText, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 0), 0, 0));
		pnlContents.add(jButton3, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));

		pnlContents.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));

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
//	public NewProjectUI()
//	{
//		super();
//		initComponents();
//		loadComponents();
//		jProjectText.selectAll();
//		jProjectText.requestFocus();
//		setPreferredSize(new Dimension(400, 400));
//		jProjectText.addKeyListener(new ProjectUIKeyListener());
//		jLocationText.addFocusListener(new WorkspaceUIFocusListener());
//	}
//
//	public NewProjectUI(INewDialogProjectDetails details)
//	{
//		super();
//		initComponents();
//		if (details != null)
//		{
//			//load modes
//			if (jComboBox1 != null)
//			{
//				NewDialogUtilities.loadModes(jComboBox1);
//				String mode = details.getMode();
//				if (mode != null)
//				{
//					jComboBox1.setSelectedItem(mode);
//				}
//			}
//
//			//load diagram types
//			if (jComboBox2 != null)
//			{
//				NewDialogUtilities.loadLanguages(jComboBox2);
//				String lang = details.getLanguage();
//				if (lang != null)
//				{
//					jComboBox2.setSelectedItem(lang);
//				}
//			}
//
//			String name = details.getName();
//			if (name == null || name.length() == 0)
//			{
//				//set default project name
//				jProjectText.setText(NewDialogUtilities.getDefaultProjectName());
//			}
//			else
//			{
//				jProjectText.setText(name);
//			}
//
//			String loc = details.getLocation();
//			if (loc == null || loc.length() == 0)
//			{
//				//set default Location text
//				jLocationText.setText(NewDialogUtilities.getWorkspaceLocation());
//			}
//			else
//			{
//				jLocationText.setText(loc);
//			}
//
//			boolean addToSCM = details.getAddToSourceControl();
//			if (addToSCM)
//			{
//				jCheckBox2.setSelected(true);
//			}
//		}
//		else
//		{
//			loadComponents();
//		}
//		jTextFieldProject.setText("Project");
//		jProjectText.selectAll();
//		jProjectText.requestFocus();
//		setPreferredSize(new Dimension(400, 400));
//		jProjectText.addKeyListener(new ProjectUIKeyListener());
//		jLocationText.addFocusListener(new WorkspaceUIFocusListener());
//	}

	/**
	 * Loads the combo boxes and initializes the default names
	 */
	private void loadComponents()
	{
		//load namespaces
		if (jComboBox1 != null)
		{
			NewDialogUtilities.loadModes(jComboBox1);
		}

		//load diagram types
		if (jComboBox2 != null)
		{
			NewDialogUtilities.loadLanguages(jComboBox2);
		}

		//set default project name
		jProjectText.setText(NewDialogUtilities.getDefaultProjectName());
		jProjectText.selectAll();
		jProjectText.requestFocus();

		//set default Location text
		jLocationText.setText(NewDialogUtilities.getWorkspaceLocation());
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		jMainPanel = new javax.swing.JPanel();
		jPanel1 = new javax.swing.JPanel();
		jLabel3 = new javax.swing.JLabel();
		jProjectText = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jLocationText = new javax.swing.JTextField();
		jButton3 = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jTextFieldProject = new javax.swing.JTextField();
		jPanel3 = new javax.swing.JPanel();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		jLabel5 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		jComboBox2 = new javax.swing.JComboBox();
		jComboBox1 = new javax.swing.JComboBox();
		jPanel5 = new javax.swing.JPanel();
		jCheckBox2 = new javax.swing.JCheckBox();
		jLabel7 = new javax.swing.JLabel();

		setLayout(new java.awt.GridBagLayout());
		jMainPanel.setLayout(new java.awt.GridBagLayout());

//		addWindowListener(new java.awt.event.WindowAdapter() {
//			public void windowClosing(java.awt.event.WindowEvent evt) {
//				closeDialog(evt);
//			}
//		});

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jPanel1.setOpaque(false);
		jLabel3.setFont(new java.awt.Font("Dialog", 0, 12)); //$NON-NLS-1$
		jLabel3.setText(NewDialogResources.getString("NewProjectUI.PROJECT_NAME_LBL")); //$NON-NLS-1$
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.ipady = 5;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLabel3, gridBagConstraints);

		jProjectText.setText("jProjectText"); //$NON-NLS-1$
		//jProjectText.setPreferredSize(new java.awt.Dimension(120, 20));

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jProjectText, gridBagConstraints);

		jLabel2.setFont(new java.awt.Font("Dialog", 0, 12)); //$NON-NLS-1$
		jLabel2.setText(NewDialogResources.getString("NewProjectUI.PROJECT_LOCATION_LBL")); //$NON-NLS-1$
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		//gridBagConstraints.ipady = 10;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLabel2, gridBagConstraints);

		jLocationText.setText("jLocationText"); //$NON-NLS-1$
		//jLocationText.setPreferredSize(new java.awt.Dimension(120, 20));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 0.9;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLocationText, gridBagConstraints);

		jButton3.setFont(new java.awt.Font("Dialog", 1, 10)); //$NON-NLS-1$
		jButton3.setText(NewDialogResources.getString("NewProjectUI.NAVIGATION_LBL")); //$NON-NLS-1$
		jButton3.setPreferredSize(new java.awt.Dimension(20, 20));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
		//gridBagConstraints.weightx = 0.1;
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
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.5;
		jMainPanel.add(jPanel1, gridBagConstraints);

		jPanel4.setLayout(new java.awt.GridBagLayout());

		jPanel4.setBorder(new javax.swing.border.TitledBorder(null, NewDialogResources.getString("NewProjectUI.PROJECT_OPTIONS_LBL"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 12))); //$NON-NLS-1$ //$NON-NLS-2$
		jLabel5.setFont(new java.awt.Font("Dialog", 0, 12)); //$NON-NLS-1$
		jLabel5.setText(NewDialogResources.getString("NewProjectUI.MODE_LBL")); //$NON-NLS-1$
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.5;
		//gridBagConstraints.weighty = 0.1;
		jPanel4.add(jLabel5, gridBagConstraints);

		jLabel6.setFont(new java.awt.Font("Dialog", 0, 12)); //$NON-NLS-1$
		jLabel6.setText(NewDialogResources.getString("NewProjectUI.DEFUALT_LANGUAGE_LBL")); //$NON-NLS-1$
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.5;
		//gridBagConstraints.weighty = 0.1;
		jPanel4.add(jLabel6, gridBagConstraints);

		jComboBox2.setEditable(true);
		//jComboBox2.setMinimumSize(new java.awt.Dimension(124, 20));
		//jComboBox2.setPreferredSize(new java.awt.Dimension(124, 20));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		//gridBagConstraints.gridwidth = 2;
		gridBagConstraints.weightx = 0.5;
		//gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
		jPanel4.add(jComboBox2, gridBagConstraints);

		jComboBox1.setEditable(true);
		//jComboBox1.setMinimumSize(new java.awt.Dimension(124, 20));
		//jComboBox1.setPreferredSize(new java.awt.Dimension(124, 20));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 0.5;
		//gridBagConstraints.weighty = 0.1;
		jPanel4.add(jComboBox1, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.4;
		jMainPanel.add(jPanel4, gridBagConstraints);

		jPanel5.setLayout(new java.awt.GridBagLayout());

		//jCheckBox2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 0.1;
		//gridBagConstraints.weighty = 0.1;
		jPanel5.add(jCheckBox2, gridBagConstraints);
		jCheckBox2.setEnabled(true);
		jCheckBox2.setOpaque(true);
		jCheckBox2.setBorder(null);
		jCheckBox2.setBorderPaintedFlat(true);
		jCheckBox2.setBackground(Color.WHITE);
      jCheckBox2.setText(NewDialogResources.getString("NewProjectUI.SOURCE_CTRL_LBL"));
      jCheckBox2.setFont(new java.awt.Font("Dialog", 0, 12)); //$NON-NLS-1$

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 5);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		jMainPanel.add(jPanel5, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		//gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.weighty = 0.5;
		add(jMainPanel, gridBagConstraints);

		doLayout();
	}

	private void jProjectTextActionPerformed(java.awt.event.ActionEvent evt) {
		INewDialogTabDetails result = getResults();
		NewDialogResultProcessor processor = new NewDialogResultProcessor();
		processor.handleResult(result);
	}

	/**
	 * @return
	 */
	private INewDialogTabDetails getResults()
	{
		INewDialogProjectDetails details = new NewDialogProjectDetails();
		if (validData())
		{
			boolean allowFromRE = false;
//			if (m_Defaults != null)
//			{
//			   _VH(m_Defaults->get_AllowFromRESelection(&bAllowFromRE));
//			}

			if (allowFromRE)
			{
				// Get the kind of project to create
				int selectedItem = 0;//GetSelectedListItem((CListCtrl*)GetDlgItem(IDC_LIST));

				if (selectedItem == 0)
				{
				   details.setProjectKind(NewProjectKind.NPK_PROJECT_FROM_REVERSE_ENG);
				}
				else
				{
				   details.setProjectKind(NewProjectKind.NPK_PROJECT);
				}
			}
			else
			{
				details.setProjectKind(NewProjectKind.NPK_PROJECT);
			}

			// Set the add to source control flag if SCM is enabled and button is checked.
			boolean bSCMEnabled = isSCMEnabled();
			details.setAddToSourceControl(bSCMEnabled ? jCheckBox2.isSelected() : false);

			// Get the mode
			String sMode = (String)jComboBox1.getSelectedItem();
			// This is coming from the screen, so could be in any language
			IConfigStringTranslator pTranslator = new ConfigStringTranslator();
			String transValue = pTranslator.translateIntoPSK(null, sMode);
			details.setMode(transValue);

			// Get the language
			details.setLanguage((String)jComboBox2.getSelectedItem());

			// Now save the location and name
			String sLocation = jLocationText.getText();
			String sName = jProjectText.getText();
			details.setName(sName);
			details.setLocation(sLocation);
		}
		return details;
	}

	/**
	 *
	 * Returns whether the source control is enabled.
	 *
	 * @param bEnabled[in,out]
	 *
	 * @return HRESULT
	 *
	 */
	private boolean isSCMEnabled()
	{
		boolean retVal = false;
		IProduct prod = ProductHelper.getProduct();
		if (prod != null)
		{
			ISCMIntegrator scm = prod.getSCMIntegrator();
			if (scm != null)
			{
				retVal = scm.isSCMEnabled();
			}
		}
		return retVal;
	}

	/**
	 * @return
	 */
	private boolean validData()
	{
		// TODO Auto-generated method stub
		return true;
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
	private javax.swing.JCheckBox jCheckBox2;
	private javax.swing.JComboBox jComboBox1;
	private javax.swing.JComboBox jComboBox2;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jMainPanel;
	private javax.swing.JTextField jProjectText;
	private javax.swing.JTextField jTextFieldProject;
	private javax.swing.JTextField jLocationText;

	/**
	 * @param selOnTab
	 */
	public void setElementType(String selOnTab)
	{
		jTextFieldProject.setText(selOnTab);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogUI#nextButtonClicked()
	 */
	public JPanel nextButtonClicked()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * This method should gather user inputs and create project.
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
//				ETSystem.out.println("Error in create project");
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
		// TODO Auto-generated method stub
		return "Adding " + jTextFieldProject.getText(); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogUI#getHelpIcon()
	 */
	public Icon getHelpIcon()
	{
		Icon icon = null;
		icon = NewDialogUtilities.getIconForResource(jTextFieldProject.getText());
		return icon;
	}
	// End of variables declaration

	/*
	 * This method shows a file chooser dialog and then sets the project
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
		return false;
	}

	private class ProjectUIKeyListener implements KeyListener
	{
		public ProjectUIKeyListener()
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
				String str = origLocation + jProjectText.getText();
				jLocationText.setText(str);
			}
		}
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

}



