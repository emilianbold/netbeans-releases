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
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.ui.support.NewPackageKind;
import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;
import org.netbeans.modules.uml.ui.support.wizard.WizardInteriorPage;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * @author sumitabhk
 *
 */
public class NewPackageUI extends WizardInteriorPage implements INewDialogUI
{
	private static final String PG_CAPTION = NewDialogResources
		.getString("NewDiagramUI.NEWWIZARD_CAPTION"); // NOI18N
	private static final String PG_TITLE = NewDialogResources
		.getString("IDS_CREATEPACKAGE"); // NOI18N
	private static final String PG_SUBTITLE = NewDialogResources
		.getString("IDS_CREATEPACKAGEHELP"); // NOI18N
	
	private JComboBox jComboBox1;
	private JComboBox jComboBox2;
	private JTextField jNameText;
	private JTextField jDiaNameText;
	private JCheckBox jCheckBox1;
	
	private INewDialogPackageDetails m_Details;
	
	public NewPackageUI(
		IWizardSheet parent, 
		String caption, 
		String headerTitle, 
		String headerSubTitle)
	{
		super(parent, caption, headerTitle, headerSubTitle);
		createUI();
	}
	
	public NewPackageUI(IWizardSheet parent)
	{
		this(parent, PG_CAPTION, PG_TITLE, PG_SUBTITLE);
		m_Details = null;
	}
	
	public NewPackageUI(IWizardSheet parent, INewDialogPackageDetails details)
	{
		this(parent, PG_CAPTION, PG_TITLE, PG_SUBTITLE);
		m_Details = details;
	}
	
	protected void createUI()
	{
		super.createUI();
		java.awt.GridBagConstraints gridBagConstraints;
		JLabel jLabel1;
		JLabel jLabel2;
		JLabel jLabel3;
		JLabel jLabel4;
		JLabel jLabel5;
		JLabel jLabel6;
		JPanel jPanel1;
		JPanel jPanel2;
		JPanel jPanel3;
		JPanel jPanel4;
		JPanel jMainPanel;
		
		jMainPanel = new javax.swing.JPanel();
		jPanel1 = new javax.swing.JPanel();
		jLabel3 = new javax.swing.JLabel();
		jNameText = new javax.swing.JTextField();
		jComboBox1 = new javax.swing.JComboBox();
		jLabel2 = new javax.swing.JLabel();
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jPanel3 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		jCheckBox1 = new javax.swing.JCheckBox();
		jLabel4 = new javax.swing.JLabel();
		jLabel5 = new javax.swing.JLabel();
		jDiaNameText = new javax.swing.JTextField();
		jLabel6 = new javax.swing.JLabel();
		jComboBox2 = new javax.swing.JComboBox();
		
		//setLayout(new java.awt.GridBagLayout());
		jMainPanel.setLayout(new java.awt.GridBagLayout());
		
		jPanel1.setLayout(new java.awt.GridBagLayout());
		
		String packStr = NewDialogResources.getString("IDS_PACKAGE"); // NOI18N
		jPanel1.setBorder(new TitledBorder(null, packStr
			/*, TitledBorder.DEFAULT_JUSTIFICATION, 
			 TitledBorder.DEFAULT_POSITION, new Font("Dialog", 0, 10)*/)); // NOI18N
		
		jPanel1.setOpaque(false);
//      jLabel3.setFont(new java.awt.Font("Dialog", 0, 10));
		jLabel3.setText(NewDialogResources.determineText(
			NewDialogResources.getString("IDS_NAME"))); // NOI18N
		NewDialogResources.setMnemonic(jLabel3, 
			NewDialogResources.getString("IDS_NAME")); // NOI18N
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		//gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 0.1;
		//gridBagConstraints.weighty = 0.2;
		gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
		jPanel1.add(jLabel3, gridBagConstraints);
		
		jNameText.setText("");
		jLabel3.setLabelFor(jNameText);
		//jNameText.setFocusAccelerator(NewDialogResources.getString("IDS_NAME").charAt(0));
		//jNameText.setPreferredSize(new java.awt.Dimension(120, 20));
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
		jPanel1.add(jNameText, gridBagConstraints);
		
		// cvc - CR#6269238
		//  make namespace uneditable (select options only)
		// jComboBox1.setEditable(true);
		jComboBox1.setEditable(false);
		NewDialogResources.setFocusAccelerator(jComboBox1, 
			NewDialogResources.getString("IDS_NAMESPACE")); // NOI18N
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
		jPanel1.add(jComboBox1, gridBagConstraints);
		
//      jLabel2.setFont(new java.awt.Font("Dialog", 0, 10));
		jLabel2.setText(NewDialogResources.determineText(
			NewDialogResources.getString("IDS_NAMESPACE"))); // NOI18N
		NewDialogResources.setMnemonic(jLabel2, 
			NewDialogResources.getString("IDS_NAMESPACE")); // NOI18N
                jLabel2.setLabelFor(jComboBox1);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
		jPanel1.add(jLabel2, gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.5;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		jMainPanel.add(jPanel1, gridBagConstraints);
		
		jPanel4.setLayout(new java.awt.GridBagLayout());
		
		String text = NewDialogResources.getString("IDS_SCOPEDDIAGRAM"); // NOI18N
		jPanel4.setBorder(new TitledBorder(new TitledBorder(null,text
			/*, TitledBorder.DEFAULT_JUSTIFICATION, 
			 TitledBorder.DEFAULT_POSITION, new Font("Dialog", 0, 10)*/))); // NOI18N
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1;
		//gridBagConstraints.weighty = 0.5;
		gridBagConstraints.insets = new java.awt.Insets(0, 10, 15, 10);
		jPanel4.add(jCheckBox1, gridBagConstraints);
		jCheckBox1.setEnabled(true);
		jCheckBox1.setOpaque(false);
		jCheckBox1.setBorder(null);
		//jCheckBox1.setBorderPaintedFlat(true);
		//jCheckBox1.setBackground(Color.WHITE);
		jCheckBox1.setText(NewDialogResources.determineText(
			NewDialogResources.getString("IDS_CREATESCOPED"))); // NOI18N
		NewDialogResources.setMnemonic(jCheckBox1, 
			NewDialogResources.getString("IDS_CREATESCOPED")); // NOI18N
		ActionListener action = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JCheckBox check = ((JCheckBox)e.getSource());
				check.requestFocusInWindow();
				check.setSelected(!check.isSelected());
				performCheckBoxToggleAction();
			}
		};
		//jCheckBox1.registerKeyboardAction(action, 
		//KeyStroke.getKeyStroke("alt C"), JComponent.WHEN_IN_FOCUSED_WINDOW); // NOI18N
		//jCheckBox1.setFont(new java.awt.Font("Dialog", 0, 10));
		
		jCheckBox1.addActionListener(new ActionListener()
		{
			
			public void actionPerformed(ActionEvent e)
			{
				performCheckBoxToggleAction();
			}
		});
		
		//		jLabel4.setFont(new java.awt.Font("Dialog", 0, 12));
		//		jLabel4.setText("Create Scoped Diagram");
		//		gridBagConstraints = new java.awt.GridBagConstraints();
		//		gridBagConstraints.gridx = 1;
		//		gridBagConstraints.gridy = 0;
		//		gridBagConstraints.anchor = GridBagConstraints.WEST;
		//		gridBagConstraints.fill = GridBagConstraints.BOTH;
		//		gridBagConstraints.weightx = 0.9;
		//		jPanel4.add(jLabel4, gridBagConstraints);
		
//      jLabel5.setFont(new java.awt.Font("Dialog", 0, 10));
		jLabel5.setText(NewDialogResources.determineText(
			NewDialogResources.getString("IDS_DIAGRAMNAME"))); // NOI18N
		NewDialogResources.setMnemonic(jLabel5, 
			NewDialogResources.getString("IDS_DIAGRAMNAME")); // NOI18N
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
		jPanel4.add(jLabel5, gridBagConstraints);
		
		jDiaNameText.setText("jDiaNameText"); // NOI18N
		jLabel5.setLabelFor(jDiaNameText);
		//jDiaNameText.setFocusAccelerator(
		//	NewDialogResources.getString("IDS_DIAGRAMNAME").charAt(0));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 10);
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		jPanel4.add(jDiaNameText, gridBagConstraints);
		
//      jLabel6.setFont(new java.awt.Font("Dialog", 0, 10));
		jLabel6.setText(NewDialogResources.determineText(
			NewDialogResources.getString("IDS_DIAGRAMTYPE"))); // NOI18N
		NewDialogResources.setMnemonic(jLabel6, 
			NewDialogResources.getString("IDS_DIAGRAMTYPE")); // NOI18N
                jLabel6.setLabelFor(jComboBox2);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
		jPanel4.add(jLabel6, gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		jPanel4.add(jComboBox2, gridBagConstraints);
		
		// cvc - CR#6269238
		//  make diagram type uneditable (select options only)
		// jComboBox2.setEditable(true);
		jComboBox2.setEditable(false);
		NewDialogResources.setFocusAccelerator(jComboBox2, 
			NewDialogResources.getString("IDS_DIAGRAMTYPE")); // NOI18N
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.5;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		jMainPanel.add(jPanel4, gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		
		performCheckBoxToggleAction();
		
//		m_DetailPanel.setBorder(new TitledBorder(null, "",  // NOI18N
//      javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
//		javax.swing.border.TitledBorder.DEFAULT_POSITION, 
//		new java.awt.Font("Dialog", 0, 12))); // NOI18N
//		//m_DetailPanel.setBackground(Color.WHITE);
//		m_DetailPanel.add(jMainPanel, gridBagConstraints);
		
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new TitledBorder(null, "" // NOI18N
			/*, TitledBorder.DEFAULT_JUSTIFICATION,
			 TitledBorder.DEFAULT_POSITION, new Font("Dialog", 0, 10)*/
			));
		
		contentPane.setLayout(new GridBagLayout());
		contentPane.add(jMainPanel, gridBagConstraints);
		add(contentPane, BorderLayout.CENTER);
	}
	
	protected boolean onInitDialog()
	{
		return super.onInitDialog();
	}
	
	public void onWizardBack()
	{
		//super.onWizardBack();
	}
	
	public void onWizardNext()
	{
		//super.onWizardNext();
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
//		processor.handleResult(details);
		IWizardSheet parent = getParentSheet();
		if (parent != null && parent instanceof JDefaultNewDialog)
		{
			((JDefaultNewDialog)parent).setResult(details);
		}
		return super.onDismiss();
	}
	
	/**
	 *
	 */
	//	public NewPackageUI()
	//	{
	//		super();
	//		initComponents();
	//		loadComponents();
	//		jNameText.selectAll();
	//		jNameText.requestFocus();
	//		setPreferredSize(new Dimension(400, 400));
	//	}
	//
	//	public NewPackageUI(INewDialogPackageDetails details)
	//	{
	//		super();
	//		initComponents();
	//
	//		if (details != null)
	//		{
	//			INamespace space = details.getNamespace();
	//			//load namespaces
	//			if (jComboBox1 != null)
	//			{
	//				NewDialogUtilities.loadNamespace(jComboBox1, space);
	//			}
	//
	//			//load diagram types
	//			if (jComboBox2 != null)
	//			{
	//				NewDialogUtilities.loadDiagramTypes(jComboBox2);
	//				int diaType = details.getScopedDiagramKind();
	//				jComboBox2.setSelectedIndex(diaType);
	//			}
	//
	//			String name = details.getName();
	//			if (name == null || name.length() == 0)
	//			{
	//				//set default package name
	//				jNameText.setText(NewDialogUtilities.getDefaultPackageName());
	//			}
	//			else
	//			{
	//				jNameText.setText(name);
	//			}
	//
	//			String diaName = details.getScopedDiagramName();
	//			if (diaName == null || diaName.length() == 0)
	//			{
	//				//set default diagram name
	//				jDiaNameText.setText(NewDialogUtilities.getDefaultDiagramName());
	//			}
	//			else
	//			{
	//				jDiaNameText.setText(diaName);
	//			}
	//
	//			boolean createDia = details.getCreateScopedDiagram();
	//			if (createDia)
	//			{
	//				jCheckBox1.setSelected(true);
	//			}
	//		}
	//		else
	//		{
	//			loadComponents();
	//		}
	//		jTextFieldPackage.setText("Package");
	//		jNameText.selectAll();
	//		jNameText.requestFocus();
	//		setPreferredSize(new Dimension(400, 400));
	//	}
	
	/**
	 * Loads the combo boxes and initializes the default names
	 */
	private void loadComponents()
	{
		//load namespaces
		if (jComboBox1 != null)
		{
			
			INamespace space = null;
			if(m_Details != null)
			{
				space = m_Details.getNamespace();
			}
			NewDialogUtilities.loadNamespace(jComboBox1, space);
		}
		
		//load diagram types
		if (jComboBox2 != null)
		{
			NewDialogUtilities.loadDiagramTypes(jComboBox2);
			// default to "Class Diagram"
			jComboBox2.setSelectedIndex(1);
		}
		
		//set default package name
		jNameText.setText(NewDialogUtilities.getDefaultPackageName());
		jNameText.selectAll();
		jNameText.requestFocus();
		
		//set default diagram name
		jDiaNameText.setText(NewDialogUtilities.getDefaultDiagramName());
		
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents()
	{
//		java.awt.GridBagConstraints gridBagConstraints;
//
//		jMainPanel = new javax.swing.JPanel();
//		jPanel1 = new javax.swing.JPanel();
//		jLabel3 = new javax.swing.JLabel();
//		jNameText = new javax.swing.JTextField();
//		jComboBox1 = new javax.swing.JComboBox();
//		jLabel2 = new javax.swing.JLabel();
//		jPanel2 = new javax.swing.JPanel();
//		jLabel1 = new javax.swing.JLabel();
//		jTextFieldPackage = new javax.swing.JTextField();
//		jPanel3 = new javax.swing.JPanel();
//		jButton1 = new javax.swing.JButton();
//		jButton2 = new javax.swing.JButton();
//		jPanel4 = new javax.swing.JPanel();
//		jCheckBox1 = new javax.swing.JCheckBox();
//		jLabel4 = new javax.swing.JLabel();
//		jLabel5 = new javax.swing.JLabel();
//		jDiaNameText = new javax.swing.JTextField();
//		jLabel6 = new javax.swing.JLabel();
//		jComboBox2 = new javax.swing.JComboBox();
//
//		setLayout(new java.awt.GridBagLayout());
//		jMainPanel.setLayout(new java.awt.GridBagLayout());
//
////		addWindowListener(new java.awt.event.WindowAdapter() {
////			public void windowClosing(java.awt.event.WindowEvent evt) {
////				closeDialog(evt);
////			}
////		});
//
//		jPanel1.setLayout(new java.awt.GridBagLayout());
//
//		jPanel1.setBorder(new javax.swing.border.TitledBorder(null, "Package", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 12)));
//		jPanel1.setOpaque(false);
//		jLabel3.setFont(new java.awt.Font("Dialog", 0, 12));
//		jLabel3.setText("Name:");
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 2;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//		gridBagConstraints.weightx = 0.1;
//		//gridBagConstraints.weighty = 0.1;
//		jPanel1.add(jLabel3, gridBagConstraints);
//
//		jNameText.setText("jNameText");
//		//jNameText.setPreferredSize(new java.awt.Dimension(120, 20));
//
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 3;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//		gridBagConstraints.weightx = 0.1;
//		//gridBagConstraints.weighty = 0.1;
//		jPanel1.add(jNameText, gridBagConstraints);
//
//		jComboBox1.setEditable(true);
//		//jComboBox1.setPreferredSize(new java.awt.Dimension(120, 25));
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 5;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//		gridBagConstraints.weightx = 0.1;
//		//gridBagConstraints.weighty = 0.1;
//		jPanel1.add(jComboBox1, gridBagConstraints);
//
//		jLabel2.setFont(new java.awt.Font("Dialog", 0, 12));
//		jLabel2.setText("Namespace:");
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 4;
//		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//		gridBagConstraints.weightx = 0.1;
//		//gridBagConstraints.weighty = 0.1;
//		jPanel1.add(jLabel2, gridBagConstraints);
//
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 1;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
//		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//		gridBagConstraints.weightx = 1.0;
//		//gridBagConstraints.weighty = 0.5;
//		jMainPanel.add(jPanel1, gridBagConstraints);
//
////		jPanel2.setLayout(new java.awt.GridBagLayout());
////
////		jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
////		jLabel1.setFont(new java.awt.Font("Dialog", 0, 12));
////		jLabel1.setText("Adding");
////		jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
////		jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
////		gridBagConstraints = new java.awt.GridBagConstraints();
////		gridBagConstraints.gridx = 0;
////		gridBagConstraints.gridy = 0;
////		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
////		gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
////		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
////		jPanel2.add(jLabel1, gridBagConstraints);
////
////		jTextFieldPackage.setEditable(false);
////		jTextFieldPackage.setBorder(null);
////		jTextFieldPackage.setText("jTextField2");
////		gridBagConstraints = new java.awt.GridBagConstraints();
////		gridBagConstraints.gridx = 1;
////		gridBagConstraints.gridy = 0;
////		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
////		gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
////		jPanel2.add(jTextFieldPackage, gridBagConstraints);
////
////		gridBagConstraints = new java.awt.GridBagConstraints();
////		gridBagConstraints.gridx = 0;
////		gridBagConstraints.gridy = 0;
////		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
////		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
////		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
////		add(jPanel2, gridBagConstraints);
////
////		jPanel3.setLayout(new java.awt.GridBagLayout());
////
////		jButton1.setText("Finish");
////		gridBagConstraints = new java.awt.GridBagConstraints();
////		gridBagConstraints.gridx = 0;
////		gridBagConstraints.gridy = 6;
////		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
////		jPanel3.add(jButton1, gridBagConstraints);
////		jButton1.addActionListener(new java.awt.event.ActionListener() {
////			public void actionPerformed(java.awt.event.ActionEvent evt) {
////				jNameTextActionPerformed(evt);
////			}
////		});
////
////		jButton2.setText("Cancel");
////		gridBagConstraints = new java.awt.GridBagConstraints();
////		gridBagConstraints.gridx = 1;
////		gridBagConstraints.gridy = 6;
////		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
////		jPanel3.add(jButton2, gridBagConstraints);
////		jButton2.addActionListener(new java.awt.event.ActionListener() {
////			public void actionPerformed(java.awt.event.ActionEvent evt) {
////				closeDialog();
////			}
////		});
////
////		gridBagConstraints = new java.awt.GridBagConstraints();
////		gridBagConstraints.gridx = 0;
////		gridBagConstraints.gridy = 3;
////		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
////		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
////		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
////		add(jPanel3, gridBagConstraints);
//
//		jPanel4.setLayout(new java.awt.GridBagLayout());
//
//		jPanel4.setBorder(new javax.swing.border.TitledBorder("Scoped Diagram"));
//		//jCheckBox1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 0;
//		//gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
//		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//		gridBagConstraints.weightx = 0.1;
//		//gridBagConstraints.weighty = 0.1;
//		jPanel4.add(jCheckBox1, gridBagConstraints);
//		jCheckBox1.setEnabled(true);
//		jCheckBox1.setOpaque(true);
//		jCheckBox1.setBorder(null);
//		jCheckBox1.setBorderPaintedFlat(true);
//		jCheckBox1.setBackground(Color.WHITE);
//
//		jCheckBox1.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent e)
//			{
//				performCheckBoxToggleAction();
//			}
//		});
//
//		jLabel4.setFont(new java.awt.Font("Dialog", 0, 12));
//		jLabel4.setText("Create Scoped Diagram");
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 1;
//		gridBagConstraints.gridy = 0;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
//		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//		gridBagConstraints.weightx = 0.9;
//		//gridBagConstraints.weighty = 0.1;
//		jPanel4.add(jLabel4, gridBagConstraints);
//
//		jLabel5.setFont(new java.awt.Font("Dialog", 0, 12));
//		jLabel5.setText("Diagram Name:");
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 1;
//		gridBagConstraints.gridwidth = 2;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
//		gridBagConstraints.weightx = 0.1;
//		//gridBagConstraints.weighty = 0.1;
//		jPanel4.add(jLabel5, gridBagConstraints);
//
//		jDiaNameText.setText("jDiaNameText");
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 2;
//		gridBagConstraints.gridwidth = 2;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.weightx = 0.1;
//		//gridBagConstraints.weighty = 0.1;
//		jPanel4.add(jDiaNameText, gridBagConstraints);
//
//		jLabel6.setFont(new java.awt.Font("Dialog", 0, 12));
//		jLabel6.setText("Diagram Type:");
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 3;
//		gridBagConstraints.gridwidth = 2;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
//		gridBagConstraints.weightx = 0.1;
//		//gridBagConstraints.weighty = 0.1;
//		jPanel4.add(jLabel6, gridBagConstraints);
//
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 4;
//		gridBagConstraints.gridwidth = 2;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.weightx = 0.1;
//		//gridBagConstraints.weighty = 0.1;
//		jPanel4.add(jComboBox2, gridBagConstraints);
//		jComboBox2.setEditable(false);
//
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 2;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
//		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//		gridBagConstraints.weightx = 1.0;
//		//gridBagConstraints.weighty = 0.5;
//		jMainPanel.add(jPanel4, gridBagConstraints);
//
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 0;
//		//gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
//		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//		gridBagConstraints.weightx = 1.0;
//		gridBagConstraints.weighty = 1.0;
//		add(jMainPanel, gridBagConstraints);
//
//		performCheckBoxToggleAction();
//
//		doLayout();
	}
	
	private void jNameTextActionPerformed(java.awt.event.ActionEvent evt)
	{
		INewDialogTabDetails result = getResults();
		NewDialogResultProcessor processor = new NewDialogResultProcessor();
		processor.handleResult(result);
	}
	
	/**
	 * @return
	 */
	private INewDialogTabDetails getResults()
	{
		INewDialogPackageDetails details = new NewDialogPackageDetails();
		if (validData())
		{
			// Object obj = jComboBox1.getSelectedItem();
			// if (obj == null)
			// {
			details.setPackageKind(NewPackageKind.NPKGK_PACKAGE);
			// }
			// else
			// {
			//		details.setPackageKind(
			//			NewPackageKind.NPKGK_PACKAGE_FROM_REVERSE_ENG);
			// }
			
			// Get the name
			details.setName(jNameText.getText());
			
			// Get the scoped diagram flag
			details.setCreateScopedDiagram(jCheckBox1.isSelected());
			
			// Get the scoped diagram name
			details.setScopedDiagramName(jDiaNameText.getText());
			
			// Get the namespace
			INamespace pSelectedNamespace = NewDialogUtilities
				.getNamespace((String)jComboBox1.getSelectedItem());
			details.setNamespace(pSelectedNamespace);
			
			// Get the diagram kind
			String diaType = (String)jComboBox2.getSelectedItem();
			if (diaType.equals(NewDialogResources
				.getString("PSK_SEQUENCE_DIAGRAM"))) // NOI18N
			{
				details.setScopedDiagramKind(IDiagramKind.DK_SEQUENCE_DIAGRAM);
			}
			else if (diaType.equals(NewDialogResources
				.getString("PSK_ACTIVITY_DIAGRAM"))) // NOI18N
			{
				details.setScopedDiagramKind(IDiagramKind.DK_ACTIVITY_DIAGRAM);
			}
			else if (diaType.equals(NewDialogResources
				.getString("PSK_CLASS_DIAGRAM"))) // NOI18N
			{
				details.setScopedDiagramKind(IDiagramKind.DK_CLASS_DIAGRAM);
			}
			else if (diaType.equals(NewDialogResources
				.getString("PSK_COLLABORATION_DIAGRAM"))) // NOI18N
			{
				details.setScopedDiagramKind(IDiagramKind.DK_COLLABORATION_DIAGRAM);
			}
			else if (diaType.equals(NewDialogResources
				.getString("PSK_COMPONENT_DIAGRAM"))) // NOI18N
			{
				details.setScopedDiagramKind(IDiagramKind.DK_COMPONENT_DIAGRAM);
			}
			else if (diaType.equals(NewDialogResources
				.getString("PSK_DEPLOYMENT_DIAGRAM"))) // NOI18N
			{
				details.setScopedDiagramKind(IDiagramKind.DK_DEPLOYMENT_DIAGRAM);
			}
			else if (diaType.equals(NewDialogResources
				.getString("PSK_STATE_DIAGRAM"))) // NOI18N
			{
				details.setScopedDiagramKind(IDiagramKind.DK_STATE_DIAGRAM);
			}
			else if (diaType.equals(NewDialogResources
				.getString("PSK_USE_CASE_DIAGRAM"))) // NOI18N
			{
				details.setScopedDiagramKind(IDiagramKind.DK_USECASE_DIAGRAM);
			}
			return details;
		}
		return null;
	}
	
	/**
	 * @return
	 */
	private boolean validData()
	{
		// TODO Auto-generated method stub
//		return true;
		IElementLocator pElementLocator = new ElementLocator();
		ETList<INamedElement> pFoundElements = pElementLocator.findByName(
				NewDialogUtilities.getNamespace((String)jComboBox1.getSelectedItem()), 
												jNameText.getText());

		if (pFoundElements != null)
		{
			int count = pFoundElements.getCount();
			for (int i = 0 ; i < count ; i++)
			{
				INamedElement pFoundElement = pFoundElements.get(i);

				if (pFoundElement != null)
				{
					if (pFoundElement.getElementType().equals("Package"))
					{
						DialogDisplayer.getDefault().notify(
							new NotifyDescriptor.Message(NbBundle.getMessage(
							NewElementUI.class, "IDS_NAMESPACECOLLISION")));
						return false;
					}
				}
			}
		}
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
	//	private javax.swing.JButton jButton1;
	//	private javax.swing.JButton jButton2;
	//	private javax.swing.JCheckBox jCheckBox1;
	//	private javax.swing.JComboBox jComboBox1;
	//	private javax.swing.JComboBox jComboBox2;
	//	private javax.swing.JLabel jLabel1;
	//	private javax.swing.JLabel jLabel2;
	//	private javax.swing.JLabel jLabel3;
	//	private javax.swing.JLabel jLabel4;
	//	private javax.swing.JLabel jLabel5;
	//	private javax.swing.JLabel jLabel6;
	//	private javax.swing.JPanel jPanel1;
	//	private javax.swing.JPanel jPanel2;
	//	private javax.swing.JPanel jPanel3;
	//	private javax.swing.JPanel jPanel4;
	//	private javax.swing.JPanel jMainPanel;
	//	private javax.swing.JTextField jNameText;
	//	private javax.swing.JTextField jTextFieldPackage;
	//	private javax.swing.JTextField jDiaNameText;
	
	/**
	 * @param selOnTab
	 */
	public void setElementType(String selOnTab)
	{
		//jTextFieldPackage.setText(selOnTab);
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
 * This method should create a new package based on user input
 */
	public INewDialogTabDetails finishButtonClicked()
	{
		INewDialogTabDetails result = null;
		try
		{
			result = getResults();
			//			NewDialogResultProcessor processor = new NewDialogResultProcessor();
			//			boolean isHandled = processor.handleResult(result);
			//			if (isHandled)
			//			{
			//				closeDialog();
			//			}
			//			else
			//			{
			//				ETSystem.out.println("Error in create package");
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
		String text = NewDialogResources.getString("IDS_ADDING"); // NOI18N
		// + jTextFieldPackage.getText();
		return  text;
	}
	
/* (non-Javadoc)
 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogUI#getHelpIcon()
 */
	public Icon getHelpIcon()
	{
		Icon icon = null;
		icon = NewDialogUtilities.getIconForResource("Package"); // NOI18N
		return icon;
	}
// End of variables declaration
	
	private void performCheckBoxToggleAction()
	{
		if (jCheckBox1.isSelected())
		{
			jDiaNameText.setEditable(true);
			jDiaNameText.setEnabled(true);
			jDiaNameText.selectAll();
			jDiaNameText.requestFocus();
			// cvc - CR#6269238
			// diagram type combobox doesn't need to be editiable
			// jComboBox2.setEditable(true);
			jComboBox2.setEnabled(true);
			// now default the text in the text box to what the package name is
			String defaultName = jNameText.getText();
			if (defaultName != null && defaultName.length() > 0)
			{
				jDiaNameText.setText(defaultName);
				jDiaNameText.selectAll();
			}
		}
		else
		{
			jDiaNameText.setEditable(false);
			jDiaNameText.setEnabled(false);
			// cvc - CR#6269238
			// diagram type combobox doesn't need to be editiable
			// jComboBox2.setEditable(false);
			jComboBox2.setEnabled(false);
		}
	}
	
	public boolean enableNextButton()
	{
		return false;
	}
	
}
