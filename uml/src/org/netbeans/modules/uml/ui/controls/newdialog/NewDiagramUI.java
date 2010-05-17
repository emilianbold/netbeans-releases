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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.JScrollPane;
import javax.swing.event.*;

import org.dom4j.Document;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.UserSettings;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;
import org.netbeans.modules.uml.ui.support.wizard.WizardInteriorPage;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * @author sumitabhk
 * @author cbeckham
 *
 */
public class NewDiagramUI extends WizardInteriorPage implements 
		INewDialogUI, ListSelectionListener
{
	private static final String PG_CAPTION = NewDialogResources
		.getString("NewDiagramUI.NEWWIZARD_CAPTION");  // NOI18N
	private static final String PG_TITLE = NewDialogResources
		.getString("NewDiagramUI.NEWWIZARD_TITLE");  // NOI18N
	private static final String PG_SUBTITLE = NewDialogResources
		.getString("NewDiagramUI.NEWWIZARD_SUBTITL");  // NOI18N
	
	
	public NewDiagramUI(
		IWizardSheet parent, 
		String caption, 
		String headerTitle, 
		String headerSubTitle)
	{
		super(parent);//, caption, headerTitle, headerSubTitle);
		createUI();
	}
	
	public NewDiagramUI(IWizardSheet parent)
	{
		this(parent, PG_CAPTION, PG_TITLE, PG_SUBTITLE);
	}
	
	public NewDiagramUI(IWizardSheet parent, INewDialogTabDetails details)
	{
		super(parent, PG_CAPTION, PG_TITLE, PG_SUBTITLE);
		if (details instanceof INewDialogDiagramDetails)
		{
			m_Details = (INewDialogDiagramDetails)details;
		}
		createUI();
	}
	
	
	 /* The only reason to set the size is because the list will not be
	  * wide enough on larger fontsizes The
	  * algorithm is rather arbitrary but works well in this instance.
	  * Charles Beckham
	  */
	private java.awt.Dimension getListDimensions()
	{
		int fontsize = getFontSize();
		int width  = 150;
		int height = 180;
		int multiplyer = 1;
		
		if (fontsize > 17) multiplyer = 3;
		width  = width  + Math.round(width*(multiplyer*fontsize/100f));
		height = height + Math.round(height*(multiplyer*fontsize/100f));
		
		return new java.awt.Dimension(width, height);
	}
	
	
	private int getFontSize()
	{
		int fontsize;
		java.awt.Font f =
			javax.swing.UIManager.getFont("controlFont"); //NOI18N
		if (f != null)
		{
			fontsize = f.getSize();
		}
		else
		{
			fontsize = 12;
		}
		return fontsize;
	}
	
	/**
	 * Enables us to adjust the panel size to handle larger fonts
	 * CBeckham
	 */
	private Dimension setPanelSize()
	{
		
		int fontsize;
		java.awt.Font f =
			javax.swing.UIManager.getFont("controlFont"); //NOI18N
		if (f != null)
		{
			fontsize = f.getSize();
		}
		else
		{
			fontsize = 12;
		}
		int width  = 600;
		int height = 500;
		int multiplyer = 2;
		
		if (fontsize > 17) multiplyer = 3;
		width  = width  + Math.round(width*(multiplyer*fontsize/100f));
		height = height + Math.round(height*(multiplyer*fontsize/100f));
		
		return new java.awt.Dimension(width, height);
	}
	
	protected void createUI()
	{
		super.createUI();
		//setPreferredSize(setPanelSize());
		
		jPanel1 = new JPanel();
		jLabel1 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jTextField1 = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jComboBox1 = new javax.swing.JComboBox();
		jTextFieldDiagram = new javax.swing.JTextField();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		
		//setLayout(new java.awt.GridBagLayout());
		jPanel1.setLayout(new java.awt.GridBagLayout());
		
		IConfigManager conMan = 
			ProductRetriever.retrieveProduct().getConfigManager();
		String fileName = conMan.getDefaultConfigLocation();
		fileName += "NewDialogDefinitions.etc"; // NOI18N
		m_doc = XMLManip.getDOMDocument(fileName);
		org.dom4j.Node node = m_doc.selectSingleNode(
			"//PropertyDefinitions/PropertyDefinition"); // NOI18N
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new ElementListCellRenderer());
		list.setLayout(new GridBagLayout());
		//changed by Cbeckham to account for large fontsize
		list.setPreferredSize(getListDimensions()); 
		if(getFontSize() > 16)
			scrollPane.getViewport().setView(list);
		// end of CBeckham code add
		
		
		if (node != null)
		{
			org.dom4j.Element elem = (org.dom4j.Element)node;
			String name = elem.attributeValue("name"); // NOI18N
			
			Vector elements = new Vector();
			List nodeList = m_doc.selectNodes(
				"//PropertyDefinition/aDefinition[@name='"  // NOI18N
				+ "Diagram" + "']/aDefinition");  // NOI18N
			if (list != null)
			{
				int diaKind = IDiagramKind.DK_ALL;
				if (m_Details != null)
				{
					diaKind = m_Details.getAvailableDiagramKinds();
				}
				int count = nodeList.size();
				for (int i=0; i<count; i++)
				{
					org.dom4j.Element subNode = (org.dom4j.Element)nodeList.get(i);
					String subName = 
						subNode.attributeValue("displayName"); // NOI18N
					subName = NewDialogResources.getString(subName);
					
					if (diaKind == IDiagramKind.DK_ALL)
					{
						elements.add(subName);
					}
					else
					{
						//only some of diagram kinds are valid
						if (subName.equals(NewDialogResources
							.getString("PSK_CLASS_DIAGRAM"))) // NOI18N
						{
							if ((diaKind & IDiagramKind.DK_CLASS_DIAGRAM) 
								== IDiagramKind.DK_CLASS_DIAGRAM)
							{
								elements.add(subName);
							}
						}
						else if (subName.equals(NewDialogResources
							.getString("PSK_ACTIVITY_DIAGRAM"))) // NOI18N
						{
							if ((diaKind & IDiagramKind.DK_ACTIVITY_DIAGRAM) 
								== IDiagramKind.DK_ACTIVITY_DIAGRAM)
							{
								elements.add(subName);
							}
						}
						else if (subName.equals(NewDialogResources
							.getString("PSK_COLLABORATION_DIAGRAM"))) // NOI18N
						{
							if ((diaKind & IDiagramKind.DK_COLLABORATION_DIAGRAM)
								== IDiagramKind.DK_COLLABORATION_DIAGRAM)
							{
								elements.add(subName);
							}
						}
						else if (subName.equals(NewDialogResources
							.getString("PSK_COMPONENT_DIAGRAM"))) // NOI18N
						{
							if ((diaKind & IDiagramKind.DK_COMPONENT_DIAGRAM) 
								== IDiagramKind.DK_COMPONENT_DIAGRAM)
							{
								elements.add(subName);
							}
						}
						else if (subName.equals(NewDialogResources
							.getString("PSK_DEPLOYMENT_DIAGRAM"))) // NOI18N
						{
							if ((diaKind & IDiagramKind.DK_DEPLOYMENT_DIAGRAM) 
								== IDiagramKind.DK_DEPLOYMENT_DIAGRAM)
							{
								elements.add(subName);
							}
						}
						else if (subName.equals(NewDialogResources
							.getString("PSK_SEQUENCE_DIAGRAM")))  // NOI18N
						{
							if ((diaKind & IDiagramKind.DK_SEQUENCE_DIAGRAM) 
								== IDiagramKind.DK_SEQUENCE_DIAGRAM)
							{
								elements.add(subName);
							}
						}
						else if (subName.equals(NewDialogResources.getString(
							"PSK_STATE_DIAGRAM")))  // NOI18N
						{
							if ((diaKind & IDiagramKind.DK_STATE_DIAGRAM) 
								== IDiagramKind.DK_STATE_DIAGRAM)
							{
								elements.add(subName);
							}
						}
						else if (subName.equals(NewDialogResources.getString(
							"PSK_USE_CASE_DIAGRAM")))  // NOI18N
						{
							if ((diaKind & IDiagramKind.DK_USECASE_DIAGRAM) 
								== IDiagramKind.DK_USECASE_DIAGRAM)
							{
								elements.add(subName);
							}
						}
					}
				}
			}
			
			list.setListData(elements);
			UserSettings userSettings = new UserSettings();
			if (userSettings != null)
			{
				String value = userSettings.getSettingValue(
					"NewDialog", "LastChosenDiagramType");  // NOI18N
				
				if (value != null && value.length() > 0)
				{
					list.setSelectedValue(value, true);
				}
			}
			
			if (list.getSelectedIndex() == -1)
			{
				list.setSelectedIndex(0);
			}
		}
		
		list.setBorder(new TitledBorder(null, "",  // NOI18N
			javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
			javax.swing.border.TitledBorder.DEFAULT_POSITION, 
			new java.awt.Font("Dialog", 0, 12)));  // NOI18N
		//m_DetailPanel.setBackground(Color.WHITE);
		
		JLabel diaNameLabel = new JLabel();
		diaNameLabel.setText(NewDialogResources.determineText(NewDialogResources
			.getString("NewDiagramUI.DIAGRAM_TYPE_LBL")));  // NOI18N
		NewDialogResources.setMnemonic(diaNameLabel, NewDialogResources
			.getString("NewDiagramUI.DIAGRAM_TYPE_LBL")); // NOI18N
		diaNameLabel.setLabelFor(list);
		
		GridBagConstraints gridBagConstraints =
			new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
		//gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		jPanel1.add(diaNameLabel, gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		
		//Add list selection listener. Fix for bug#6283146		
		list.addListSelectionListener(this);
		
		// CBeckham - added this code to add a scroll bar to list when 
		// using very very large fonts
		if(getFontSize() > 16)
			jPanel1.add(scrollPane, gridBagConstraints);
		else
			jPanel1.add(list, gridBagConstraints);
		// end of CBeckham code add
		
		jLabel3.setText(NewDialogResources.determineText(NewDialogResources
			.getString("NewDiagramUI.DIAGRAM_NAME_LBL")));  // NOI18N
		NewDialogResources.setMnemonic(jLabel3, NewDialogResources
			.getString("NewDiagramUI.DIAGRAM_NAME_LBL")); // NOI18N
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
		//gridBagConstraints.weightx = 0.2;
		gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLabel3, gridBagConstraints);
		
		jTextField1.setText("jTextField1");  // NOI18N
		jLabel3.setLabelFor(jTextField1);
		//jTextField1.setPreferredSize(new java.awt.Dimension(120, 20));
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		//gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 5);
		gridBagConstraints.weightx = 0.8;
		gridBagConstraints.weighty = 0.1;
		jPanel1.add(jTextField1, gridBagConstraints);
		
		jLabel2.setText(NewDialogResources.determineText(NewDialogResources
			.getString("NewDiagramUI.NAMESPACE_LBL"))); // NOI18N
		NewDialogResources.setMnemonic(jLabel2, NewDialogResources
			.getString("NewDiagramUI.NAMESPACE_LBL")); // NOI18N
                jLabel2.setLabelFor(jComboBox1);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		//	gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		//gridBagConstraints.weightx = 0.2;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
		jPanel1.add(jLabel2, gridBagConstraints);
		
		// cvc - CR#6269238
		//  make namespace uneditable (select options only)
		// jComboBox1.setEditable(true);
		jComboBox1.setEditable(false);
		NewDialogResources.setFocusAccelerator(jComboBox1, NewDialogResources
			.getString("NewDiagramUI.NAMESPACE_LBL")); // NOI18N
		//jComboBox1.setPreferredSize(new java.awt.Dimension(120, 25));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
		gridBagConstraints.weightx = 0.8;
		gridBagConstraints.weighty = 0.1;
		jPanel1.add(jComboBox1, gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new TitledBorder(null, "",
			TitledBorder.DEFAULT_JUSTIFICATION,
			TitledBorder.DEFAULT_POSITION,
			new Font("Dialog", 0,getFontSize())));  // NOI18N
		
		contentPane.setLayout(new GridBagLayout());
		contentPane.add(jPanel1, gridBagConstraints);
		add(contentPane, BorderLayout.CENTER);
	}
	
	protected boolean onInitDialog()
	{
		return super.onInitDialog();
	}
	
	public boolean onDismiss()
	{
		INewDialogTabDetails details = getResults( true );
		if( null == details )
		{
			return false;
		}
		
//		NewDialogResultProcessor processor = new NewDialogResultProcessor();
//		processor.handleResult(details);
		IWizardSheet parent = getParentSheet();
		if (parent != null && parent instanceof JDefaultNewDialog)
		{
			((JDefaultNewDialog)parent).setResult(details);
		}
		
		UserSettings userSettings = new UserSettings();
		if (userSettings != null)
		{
			// write it back out to the ini file
			userSettings.setSettingValue(
				"NewDialog",  // NOI18N
				"LastChosenDiagramType",  // NOI18N
				(String)list.getSelectedValue());
		}
		return super.onDismiss();
	}
	
	public void onSetActive()
	{
		super.onSetActive();
		getParentSheet().setButtonEnabled(IWizardSheet.PSWIZB_FINISH, true);
		getParentSheet().setButtonEnabled(IWizardSheet.PSWIZB_NEXT, false);
		//m_headerImage.setIcon(getHelpIcon());
		loadComponents();
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
//	public NewDiagramUI()
//	{
//		super();
//		initComponents();
//		loadComponents();
//		jTextField1.selectAll();
//		jTextField1.requestFocus();
//		setPreferredSize(new Dimension(400, 400));
//	}
//
//	public NewDiagramUI(INewDialogDiagramDetails details)
//	{
//		super();
//		initComponents();
//		INamespace space = null;
//		String name = "";
//		int kind = 0;
//		int availableKinds = 0;
//		if (details != null)
//		{
//			space = details.getNamespace();
//			name = details.getName();
//			kind = details.getDiagramKind();
//			availableKinds = details.getAvailableDiagramKinds();
//		}
//		//load namespaces
//		if (jComboBox1 != null)
//		{
//			NewDialogUtilities.loadNamespace(jComboBox1, space);
//		}
//
//		if (name == null || name.length() == 0)
//		{
//			//set default diagram name
//			jTextField1.setText(NewDialogUtilities.getDefaultDiagramName());
//		}
//		else
//		{
//			jTextField1.setText(name);
//		}
//		jTextField1.selectAll();
//		jTextField1.requestFocus();
//		String sKind = getDiagramKind(kind);
//		jTextFieldDiagram.setText(sKind);
//		setPreferredSize(new Dimension(400, 400));
//	}
	
	private String getDiagramKind(int kind)
	{
		String retStr = ""; //$NON-NLS-1$
		switch (kind)
		{
			case IDiagramKind.DK_ACTIVITY_DIAGRAM :
				retStr = NewDialogResources.getString(
					"NewDiagramUI.ACTIVITY_DIAGRAM"); // NOI18N
				break;
				
			case IDiagramKind.DK_CLASS_DIAGRAM :
				retStr = NewDialogResources.getString(
					"NewDiagramUI.CLASS_DIAGRAM"); // NOI18N
				break;
				
			case IDiagramKind.DK_COLLABORATION_DIAGRAM :
				retStr = NewDialogResources.getString(
					"NewDiagramUI.COLLABORATION_DIAGRAM"); // NOI18N
				break;
				
			case IDiagramKind.DK_COMPONENT_DIAGRAM :
				retStr = NewDialogResources.getString(
					"NewDiagramUI.COMPONENT_DIAGRAM"); // NOI18N
				break;
				
			case IDiagramKind.DK_DEPLOYMENT_DIAGRAM :
				retStr = NewDialogResources.getString(
					"NewDiagramUI.DEPLOYMENT_DIAGRAM"); // NOI18N
				break;
				
			case IDiagramKind.DK_SEQUENCE_DIAGRAM :
				retStr = NewDialogResources.getString(
					"NewDiagramUI.SEQUENCE_DIAGRAM"); // NOI18N
				break;
				
			case IDiagramKind.DK_STATE_DIAGRAM :
				retStr = NewDialogResources.getString(
					"NewDiagramUI.STATE_DIAGRAM"); // NOI18N
				break;
				
			case IDiagramKind.DK_USECASE_DIAGRAM :
				retStr = NewDialogResources.getString(
					"NewDiagramUI.USE_CASE_DIAGRAM"); // NOI18N
				break;
				
			default :
				retStr = NewDialogResources.getString(
					"NewDiagramUI.CLASS_DIAGRAM"); // NOI18N
				break;
		}
		return retStr;
	}
	
	/**
	 * Loads the combo boxes and initializes the default names
	 */
	private void loadComponents()
	{
		//load namespaces
		if (jComboBox1 != null)
		{
			//NewDialogUtilities.loadNamespace(jComboBox1, null);
			NewDialogUtilities.loadNamespace(
				jComboBox1, m_Details.getNamespace());
			// Fix for bug#6283146
			for(int i=0;i<jComboBox1.getItemCount();i++)
				 saveNamespaces.add(jComboBox1.getItemAt(i));
			valueChanged(null);			
		}
		
		//set default diagram name
		jTextField1.setText(NewDialogUtilities.getDefaultDiagramName());
		jTextField1.selectAll();
		jTextField1.requestFocus();
		
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents()
	{
		java.awt.GridBagConstraints gridBagConstraints;
		
		jPanel1 = new JPanel();
		jLabel1 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jTextField1 = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jComboBox1 = new javax.swing.JComboBox();
		jTextFieldDiagram = new javax.swing.JTextField();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		
		setLayout(new java.awt.GridBagLayout());
		jPanel1.setLayout(new java.awt.GridBagLayout());
		
//		addWindowListener(new java.awt.event.WindowAdapter() {
//			public void windowClosing(java.awt.event.WindowEvent evt) {
//				closeDialog(evt);
//			}
//		});
		
//		jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
//		jLabel1.setFont(new java.awt.Font("Dialog", 0, 12));
//		jLabel1.setText("Adding Diagram kind:");
//		jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
//		jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 0;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
//		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//		add(jLabel1, gridBagConstraints);
		
		jLabel3.setText(NewDialogResources.getString(
			"NewDiagramUI.DIAGRAM_NAME_LBL")); // NOI18N
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLabel3, gridBagConstraints);
		
		jTextField1.setText("jTextField1"); // NOI18N
		//jTextField1.setPreferredSize(new java.awt.Dimension(120, 20));
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		//gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.1;
		jPanel1.add(jTextField1, gridBagConstraints);
		
		jLabel2.setText(NewDialogResources.getString(
			"NewDiagramUI.NAMESPACE_LBL")); // NOI18N
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
		jPanel1.add(jLabel2, gridBagConstraints);
		
		// cvc - CR#6269238
		// make namespace uneditable (choose options only)
		jComboBox1.setEditable(true);
		//jComboBox1.setPreferredSize(new java.awt.Dimension(120, 25));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.1;
		jPanel1.add(jComboBox1, gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		//gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(jPanel1, gridBagConstraints);
		
		
//		jTextFieldDiagram.setEditable(false);
//		jTextFieldDiagram.setBorder(null);
//		jTextFieldDiagram.setText("jTextField2");
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 1;
//		gridBagConstraints.gridy = 0;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
//		add(jTextFieldDiagram, gridBagConstraints);
//
//		jButton1.setText("Finish");
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 6;
//		gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 5);
//		add(jButton1, gridBagConstraints);
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
//		gridBagConstraints.insets = new java.awt.Insets(20, 5, 0, 0);
//		add(jButton2, gridBagConstraints);
//		jButton2.addActionListener(new java.awt.event.ActionListener() {
//			public void actionPerformed(java.awt.event.ActionEvent evt) {
//				closeDialog();
//			}
//		});
		
		doLayout();
	}
	
	private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt)
	{
		INewDialogTabDetails result = getResults( false );
		NewDialogResultProcessor processor = new NewDialogResultProcessor();
		processor.handleResult(result);
	}
	
	/**
	 * @return
	 */
	private INewDialogTabDetails getResults( boolean bShowErrorMessage )
	{
		INewDialogDiagramDetails details = null;
		
		ETPairT< Boolean, String > pair = dataIsValid();
		if ( pair.getParamOne().booleanValue() )
		{
			details = new NewDialogDiagramDetails();
			
			// Get the diagram kind
			String diaType = (String)list.getSelectedValue();
			if (diaType.equals(NewDialogResources.getString(
				"PSK_SEQUENCE_DIAGRAM"))) // NOI18N
			{
				details.setDiagramKind(IDiagramKind.DK_SEQUENCE_DIAGRAM);
			}
			else if (diaType.equals(NewDialogResources
				.getString("PSK_ACTIVITY_DIAGRAM"))) // NOI18N
			{
				details.setDiagramKind(IDiagramKind.DK_ACTIVITY_DIAGRAM);
			}
			else if (diaType.equals(NewDialogResources
				.getString("PSK_CLASS_DIAGRAM"))) // NOI18N
			{
				details.setDiagramKind(IDiagramKind.DK_CLASS_DIAGRAM);
			}
			else if (diaType.equals(NewDialogResources
				.getString("PSK_COLLABORATION_DIAGRAM"))) // NOI18N
			{
				details.setDiagramKind(IDiagramKind.DK_COLLABORATION_DIAGRAM);
			}
			else if (diaType.equals(NewDialogResources
				.getString("PSK_COMPONENT_DIAGRAM"))) // NOI18N
			{
				details.setDiagramKind(IDiagramKind.DK_COMPONENT_DIAGRAM);
			}
			else if (diaType.equals(NewDialogResources
				.getString("PSK_DEPLOYMENT_DIAGRAM"))) // NOI18N
			{
				details.setDiagramKind(IDiagramKind.DK_DEPLOYMENT_DIAGRAM);
			}
			else if (diaType.equals(NewDialogResources
				.getString("PSK_STATE_DIAGRAM"))) // NOI18N
			{
				details.setDiagramKind(IDiagramKind.DK_STATE_DIAGRAM);
			}
			else if (diaType.equals(NewDialogResources
				.getString("PSK_USE_CASE_DIAGRAM"))) // NOI18N
			{
				details.setDiagramKind(IDiagramKind.DK_USECASE_DIAGRAM);
			}
			
			// Set the name
			details.setName(jTextField1.getText());
			
			// Get the namespace
			INamespace selectedNamespace =
				NewDialogUtilities.getNamespace(
					(String)jComboBox1.getSelectedItem());
			
			details.setNamespace(selectedNamespace);
		}
		else if( bShowErrorMessage )
		{
                   String message = pair.getParamTwo();
                   if (message != null && message.trim().length() > 0 ) 
                   {
                        NotifyDescriptor.Message notifyDesc = 
                                new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(notifyDesc);
                   }
		}
		else
		{
			// Added this code so the behavior does not change for this case
			details = new NewDialogDiagramDetails();
		}
		
		return details;
	}
	
	/**
	 * @return
	 */
	public ETPairT< Boolean, String > dataIsValid()
	{
		boolean bDataIsValid = true;
		String message = null;
		ETPairT< Boolean, String > retVals = null;
		
		String sDiagramName = jTextField1.getText();
		String trimmedName = sDiagramName.trim();
                int trimmedLen = trimmedName.length();

		//boolean bNameHasSpace = !sDiagramName.equals( sDiagramName.trim() );  
                boolean bNameHasSpaces = sDiagramName.length() > trimmedLen;   
                
                //Jyothi: Adding logic to reject diagram names with a dot or a space -- Fix for Bug#6359779
                boolean bNameHasDot = sDiagramName.contains(".");                
		
		if ((trimmedLen > 0) && !bNameHasSpaces && !bNameHasDot)
		{
			String diaType = (String)list.getSelectedValue();
			if ( diaType.length() <= 0 )
			{
				// Nothing selected
				message = NewDialogResources
					.getString("IDS_PLEASESELECTADIAGRAM"); // NOI18N
				bDataIsValid = false;
			}
			else
			{
				// Get the namespace
				INamespace selectedNamespace =
					NewDialogUtilities.getNamespace(
						(String)jComboBox1.getSelectedItem());
				if ( selectedNamespace != null )
				{
					retVals = isValidDiagramForNamespace(
						diaType, selectedNamespace);
				}
				else
				{
					// Something bad happened.  We added a namespace to 
					// the combo and couldn't get it back out!
					message = NewDialogResources
						.getString("IDS_FAILEDTOGETNAMESPACE"); // NOI18N
                                        bDataIsValid = false;
				}
			}
		}
		else  // diagram is not valid: is empty or has dot or has leading/trailing spaces
		{       if (trimmedLen == 0) 
                        {
                            message = NewDialogResources
					.getString("IDS_DIAGRAMNAME_EMPTY"); // NOI18N
                        }
                        else if (bNameHasSpaces)
			{
                            message = NewDialogResources
					.getString("IDS_DIAGRAMNAME_HAS_SPACES"); // NOI18N
			}
                        else if(bNameHasDot) {
                            message = NewDialogResources.getString("IDS_DIAGRAMNAME_HAS_DOT"); // NOI18N
                        }
                        bDataIsValid = false;
		}
		
		if( null == retVals )
		{
                    retVals = new ETPairT<Boolean, String>(new Boolean(bDataIsValid), message);
		}
		
		return retVals;
	}
	
	protected ETPairT<Boolean, String> isValidDiagramForNamespace(
		final String diaType, INamespace namespace)
	{
		ETPairT< Boolean, String > retVals = null;
		
		if( namespace != null )
		{
			if( diaType.equals(NewDialogResources
				.getString("NewDiagramUI.COLLABORATION_DIAGRAM"))) // NOI18N
			{
				retVals = isValidBehaviorDiagramForNamespace(
					IDiagramKind.DK_COLLABORATION_DIAGRAM, namespace);
			}
			else if( diaType.equals(NewDialogResources
				.getString("NewDiagramUI.SEQUENCE_DIAGRAM"))) // NOI18N
			{
				retVals = isValidBehaviorDiagramForNamespace(
					IDiagramKind.DK_SEQUENCE_DIAGRAM, namespace);
			}
		}
		
		return retVals;
	}
	
	protected ETPairT<Boolean, String> isValidBehaviorDiagramForNamespace( 
		final int nTestKind, INamespace namespace )
	{
		// When the namespace is either an operation or interaction,
		// make sure there is one or zero diagram of either collaboration 
		// or sequence type. For other namespaces, an interaction 
		// will be created (somewhere else).
		
		boolean bIsValidForNamespace = true;
		String message = null;
		
		if( namespace != null )
		{
			boolean bUseInteractionMessage = true;
			
			IInteraction interaction = null;
			if ( namespace instanceof IInteraction )
			{
				interaction = (IInteraction)namespace;
			}
			else if (namespace instanceof IOperation)
			{
				bUseInteractionMessage = false;
				
				// Look for an interaction where we will look for a diagram
				ETList<INamedElement> namedElements = 
					namespace.getOwnedElements();
				
				for (Iterator iter = namedElements.iterator(); iter.hasNext();)
				{
					INamedElement namedElement = (INamedElement)iter.next();
					
					if (namedElement instanceof IInteraction)
					{
						interaction = (IInteraction)namedElement;
						break;
					}
				}
			}
			
			// Since there should only be one seq diagram 
			// (or one collaboration diagram)
			// in tree per Interaction, prevent user from 
			// creating multiple diagrams.
			
			INamespace currentNamespace = (IInteraction)interaction;
			if( currentNamespace != null )
			{
				IProxyDiagramManager diagramManager = ProxyDiagramManager.instance();
				if ( diagramManager != null )
				{
					ETList<IProxyDiagram> proxyDiagrams = 
						diagramManager.getDiagramsInNamespace(currentNamespace);
					
					for (Iterator iter = proxyDiagrams.iterator(); iter.hasNext();)
					{
						IProxyDiagram proxyDiagram = (IProxyDiagram)iter.next();
						int nKind = proxyDiagram.getDiagramKind();
						
						if (nTestKind == nKind)
						{
							switch( nTestKind )
							{
								case IDiagramKind.DK_COLLABORATION_DIAGRAM:
									message = NewDialogResources.getString(
										bUseInteractionMessage
										? "IDS_ONE_COD_PER_INTERACTION" // NOI18N
										: "IDS_ONE_COD_PER_OPERATION" ); // NOI18N
									break;
									
								default:
									assert (false);  
									// do we have another behavioral type diagram?
								case IDiagramKind.DK_SEQUENCE_DIAGRAM:
									message = NewDialogResources.getString(
										bUseInteractionMessage
										? "IDS_ONE_SQD_PER_INTERACTION" // NOI18N
										: "IDS_ONE_SQD_PER_OPERATION" ); // NOI18N
									break;
							}
							
							bIsValidForNamespace = false;
						}
					}
				}
			}
		}
		
		return new ETPairT<Boolean, String>(
			new Boolean(bIsValidForNamespace), message);
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
	private JPanel jPanel1;
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JComboBox jComboBox1;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JTextField jTextField1;
	private static javax.swing.JTextField jTextFieldDiagram;
	private java.util.List saveNamespaces=new java.util.ArrayList();
	
	private Document m_doc = null;
	private JList list = new JList();
	private JScrollPane scrollPane = new JScrollPane();
	private INewDialogDiagramDetails m_Details = null;
	
	/**
	 * @param selOnTab
	 */
	public static void setElementType(String selOnTab)
	{
		jTextFieldDiagram.setText(selOnTab);
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
	 * This method will create new diagram based on the user input
	 */
	public INewDialogTabDetails finishButtonClicked()
	{
		INewDialogTabDetails result = null;
		try
		{
			result = getResults( false );
//			NewDialogResultProcessor processor = new NewDialogResultProcessor();
//			boolean isProcessed = processor.handleResult(result);
//			if (isProcessed)
//			{
//				closeDialog();
//			}
//			else
//			{
//				ETSystem.out.println("Error in create diagram");
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
		return "Adding " + jTextFieldDiagram.getText(); // NOI18N
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogUI#getHelpIcon()
	 */
	public Icon getHelpIcon()
	{
		Icon icon = null;
		icon = NewDialogUtilities.getIconForResource(jTextFieldDiagram.getText());
		return icon;
	}
	// End of variables declaration
	
	public boolean enableNextButton()
	{
		return false;
	}
	
				
	//list selection listener callback
	public void valueChanged(ListSelectionEvent e)
	{
		// Fix for bug#6283146
		jComboBox1.removeAllItems();
		String diaType = (String)list.getSelectedValue();
		if (diaType.equals(NewDialogResources.getString(
			"PSK_SEQUENCE_DIAGRAM"))) // NOI18N
		{                        
			if(saveNamespaces.size()>0)
				jComboBox1.addItem(saveNamespaces.get(0));
		}
		else
		{
			for(int i=0;i<saveNamespaces.size();i++)
				jComboBox1.addItem(saveNamespaces.get(i));
		}
	}	
	
	class ElementListCellRenderer extends JLabel implements ListCellRenderer
	{
		public Icon getImageIcon(String diaName)
		{
			Icon retIcon = null;
			String displayName = NewDialogResources.getStringKey(diaName);
			String str = "//PropertyDefinition/aDefinition[@name='" +  // NOI18N
				"Diagram" + "']/aDefinition[@displayName='" + 
				displayName + "']";  // NOI18N
			
			org.dom4j.Node node = m_doc.selectSingleNode(str);
			if (node.getNodeType() == org.dom4j.Element.ELEMENT_NODE)
			{
				org.dom4j.Element elem = (org.dom4j.Element)node;
				String fileName = elem.attributeValue("image");  // NOI18N
				File file = new File(fileName);
				
				retIcon = CommonResourceManager.instance().getIconForFile(fileName);
			}
			
			return retIcon;
		}
		
		public Component getListCellRendererComponent(
			JList list,
			Object value,            // value to display
			int index,               // cell index
			boolean isSelected,      // is the cell selected
			boolean cellHasFocus)    // the list and the cell have the focus
		{
			String s = value.toString();
			setText(s);
			setIcon(getImageIcon(s));
			
			if (isSelected)
			{
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			
			else
			{
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		} 		
	}
}
