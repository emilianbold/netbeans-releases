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
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import java.util.Arrays;
import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.ui.support.NewElementKind;
import org.netbeans.modules.uml.ui.support.UserSettings;
import org.netbeans.modules.uml.ui.support.commondialogs.IErrorDialog;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;
import org.netbeans.modules.uml.ui.support.wizard.WizardInteriorPage;
import org.netbeans.modules.uml.ui.support.wizard.WizardSheet;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingErrorDialog;
import org.netbeans.modules.uml.util.StringTokenizer2;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;



/**
 * @author sumitabhk
 * @author Craig Conover, craig.conover@sun.com
 *
 */
public class NewElementUI extends WizardInteriorPage 
	implements INewDialogUI
{
	private static final String PG_CAPTION =
		NewDialogResources.getString("NewDiagramUI.NEWWIZARD_CAPTION"); // NOI18N
	private static final String PG_TITLE =
		NewDialogResources.getString("IDS_NEWELEMENT"); // NOI18N
	private static final String PG_SUBTITLE =
		NewDialogResources.getString("IDS_NEWELEMENTHELP"); // NOI18N
	
	
	// Variables declaration - do not modify
	private JPanel jPanel1;
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JComboBox jComboBox1;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JTextField jTextFieldName;
	private static javax.swing.JTextField jTextFieldElement;
	
	private org.dom4j.Document m_doc = null;
	private JList list = new JList();
//	private ElementList list = new ElementList();
	private JScrollPane scrollpane = new JScrollPane();
	private INewDialogElementDetails m_Details;
	
	
	public NewElementUI(
		IWizardSheet parent,
		String caption,
		String headerTitle,
		String headerSubTitle)
	{
		super(parent, caption, headerTitle, headerSubTitle);
		createUI();
	}
	
	public NewElementUI(IWizardSheet parent)
	{
		this(parent, PG_CAPTION, PG_TITLE, PG_SUBTITLE);
	}
	
	public NewElementUI(IWizardSheet parent, INewDialogTabDetails details)
	{
		super(parent, PG_CAPTION, PG_TITLE, PG_SUBTITLE);
		if (details instanceof INewDialogElementDetails)
		{
			m_Details = (INewDialogElementDetails)details;
		}
		createUI();
	}
	
	protected void createUI()
	{
		super.createUI();
		
		jPanel1 = new JPanel();
		jLabel1 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jTextFieldName = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jComboBox1 = new javax.swing.JComboBox();
		jTextFieldElement = new javax.swing.JTextField();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		final int elementPixelHeight = 17;
		
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
		// set some sort of initial size, but the height will be adjusted
		//  after we have added the Elements to the list. This will make the
		//  scroll bar work properly
		list.setPreferredSize(new Dimension(150,165));
		
		list.setAutoscrolls(true);
		list.setVisibleRowCount(16);
		scrollpane.getViewport().setView(list);
		scrollpane.setVerticalScrollBarPolicy(
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		if (node != null)
		{
			org.dom4j.Element elem = (org.dom4j.Element)node;
			String name = elem.attributeValue("name"); // NOI18N
			
			Vector elements = new Vector();
			List nodeList = m_doc.selectNodes(
				"//PropertyDefinition/aDefinition[@name='" // NOI18N 
				+ "Element" + "']/aDefinition"); // NOI18N
			
			if (list != null)
			{
				int count = nodeList.size();
				for (int i=0; i<count; i++)
				{
					org.dom4j.Element subNode = 
						(org.dom4j.Element)nodeList.get(i);
					
					String subName = 
						subNode.attributeValue("displayName"); // NOI18N
					
					subName = NewDialogResources.getString(subName);
					
					//add workspace node
					elements.add(subName);
				}
			}

			// adjust the height of the JList so that the ScrollablePane
			//  knows how big it is so it can adjust the scrollbars
			list.setPreferredSize(
				new Dimension(150,elements.size() * elementPixelHeight));
			list.setListData(elements);
			
			UserSettings userSettings = new UserSettings();
			
			if (userSettings != null)
			{
				String value = userSettings.getSettingValue(
					"NewDialog", "LastChosenElementType"); // NOI18N
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
		
		list.setBorder(new TitledBorder(null, "", // NOI18N
			javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
			javax.swing.border.TitledBorder.DEFAULT_POSITION, 
			new java.awt.Font("Dialog", 0, 12))); // NOI18N
		//m_DetailPanel.setBackground(Color.WHITE);
		
		GridBagConstraints gridBagConstraints = 
			new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		JLabel elemName = new JLabel();
		elemName.setText(NewDialogResources.determineText(
			NewDialogResources.getString("IDS_ELEMENTTYPE"))); // NOI18N
		NewDialogResources.setMnemonic(elemName, 
			NewDialogResources.getString("IDS_ELEMENTTYPE")); // NOI18N
		elemName.setLabelFor(list);
		jPanel1.add(elemName, gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 25;
//		jPanel1.add(list, gridBagConstraints);
		jPanel1.add(scrollpane, gridBagConstraints);
		
		jLabel3.setText(NewDialogResources.determineText(
			NewDialogResources.getString("IDS_ELEMENTNAME"))); // NOI18N
		NewDialogResources.setMnemonic(jLabel3, 
			NewDialogResources.getString("IDS_ELEMENTNAME")); // NOI18N
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		//gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLabel3, gridBagConstraints);
		
		jTextFieldName.setText("jTextField1"); // NOI18N
		jLabel3.setLabelFor(jTextFieldName);
		//jTextFieldName.setFocusAccelerator(
		//	NewDialogResources.getString("IDS_ELEMENTNAME").charAt(8));
		//jTextFieldName.setPreferredSize(new java.awt.Dimension(120, 20));
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 5);
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jTextFieldName, gridBagConstraints);
		
		jLabel2.setText(NewDialogResources.determineText(
			NewDialogResources.getString("IDS_NAMESPACE"))); // NOI18N
		NewDialogResources.setMnemonic(jLabel2, 
			NewDialogResources.getString("IDS_NAMESPACE")); // NOI18N
                jLabel2.setLabelFor(jComboBox1);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		//gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		//gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
		jPanel1.add(jLabel2, gridBagConstraints);
		
		// cvc - CR#6269238
		//  make namespace uneditable (select options only)
		// jComboBox1.setEditable(true);
		jComboBox1.setEditable(false);
		NewDialogResources.setFocusAccelerator(jComboBox1, 
			NewDialogResources.getString("IDS_NAMESPACE")); // NOI18N
//		ActionListener action = new ActionListener()
//		{
//			public void actionPerformed(ActionEvent e)
//			{
//				((JComponent)e.getSource()).requestFocusInWindow();
//			}
//		};
//		jComboBox1.registerKeyboardAction(action, 
//		KeyStroke.getKeyStroke("alt A"), JComponent.WHEN_IN_FOCUSED_WINDOW);
//		jComboBox1.setPreferredSize(new java.awt.Dimension(120, 25));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jComboBox1, gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new TitledBorder(null, "", // NOI18N
			TitledBorder.DEFAULT_JUSTIFICATION,
			TitledBorder.DEFAULT_POSITION,
			new Font("Dialog", 0, 12)));  // NOI18N
		
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
		INewDialogTabDetails details = getResults();
		if( null == details )
		{
			return false;
		}
		
		NewDialogResultProcessor processor = new NewDialogResultProcessor();
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
				"NewDialog", "LastChosenElementType",  // NOI18N
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
		//super.onWizardBack();
	}
	
	public void onWizardNext()
	{
		//super.onWizardNext();
	}
	
	/**
	 *
	 */
//	public NewElementUI()
//	{
//		super();
//		initComponents();
//		loadComponents();
//		jTextFieldName.selectAll();
//		jTextFieldName.requestFocus();
//		setPreferredSize(new Dimension(400, 400));
//	}
//
//	public NewElementUI(INewDialogElementDetails details)
//	{
//		super();
//		initComponents();
//		if (details != null)
//		{
//			INamespace space = details.getNamespace();
//			String name = details.getName();
//			int kind = details.getElementKind();
//
//			//load namespaces
//			if (jComboBox1 != null)
//			{
//				NewDialogUtilities.loadNamespace(jComboBox1, space);
//			}
//
//			if (name == null || name.length() == 0)
//			{
//				//set default element name
//				jTextFieldName.setText(NewDialogUtilities.getDefaultElementName());
//			}
//			else
//			{
//				jTextFieldName.setText(name);
//			}
//			String sKind = getElementKind(kind);
//			if (sKind == null || sKind.length() == 0)
//			{
//				jTextFieldElement.setText("Class");
//			}
//			else
//			{
//				jTextFieldElement.setText(sKind);
//			}
//		}
//		else
//		{
//			loadComponents();
//			jTextFieldElement.setText("Class");
//		}
//		jTextFieldName.selectAll();
//		jTextFieldName.requestFocus();
//		setPreferredSize(new Dimension(400, 400));
//	}
	
	private String getElementKind(int kind)
	{
		// CR#6263225 cvc - added arrays to NewElementKind to make the
		//  maintenance of adding/changing/removing elements much easier
		// switch/case logic no longer needed
		
		List eleNumList = Arrays.asList(NewElementKind.ELEMENT_NUMBERS);
		int index = eleNumList.indexOf(new Integer(kind));
		
		if (index == -1)
			return NewElementKind.ELEMENT_NAMES[0];
		else
			return NewElementKind.ELEMENT_NAMES[index];
		
//		String retStr = "";
//		switch (kind)
//		{
//			case NewElementKind.NEK_ACTOR :
//				retStr = "Actor";
//				break;
//			case NewElementKind.NEK_ATTRIBUTE :
//				retStr = "Attribute";
//				break;
//			case NewElementKind.NEK_CLASS :
//				retStr = "Class";
//				break;
//			case NewElementKind.NEK_DATATYPE :
//				retStr = "DataType";
//				break;
//			case NewElementKind.NEK_INTERFACE :
//				retStr = "Interface";
//				break;
//			case NewElementKind.NEK_OPERATION :
//				retStr = "Operation";
//				break;
//			case NewElementKind.NEK_USE_CASE :
//				retStr = "UseCase";
//				break;
//			default :
//				retStr = "Class";
//				break;
//		}
//		return retStr;
	}
	
	/**
	 * Loads the combo boxes and initializes the default names
	 */
	private void loadComponents()
	{
		//load namespaces
		if (jComboBox1 != null)
		{
			NewDialogUtilities.loadNamespace(jComboBox1, 
				m_Details.getNamespace());
		}
		
		//set default element name
		jTextFieldName.setText(NewDialogUtilities.getDefaultElementName());
		jTextFieldName.selectAll();
		jTextFieldName.requestFocus();
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
		jTextFieldName = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jComboBox1 = new javax.swing.JComboBox();
		jTextFieldElement = new javax.swing.JTextField();
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
//		jLabel1.setText("Adding Element:");
//		jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
//		jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 0;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
//		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//		add(jLabel1, gridBagConstraints);
		
		jLabel3.setText(NewDialogResources.getString("IDS_ELEMENTNAME")); // NOI18N
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		//gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jLabel3, gridBagConstraints);
		
		jTextFieldName.setText("jTextField1"); // NOI18N
		//jTextFieldName.setPreferredSize(new java.awt.Dimension(120, 20));
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jTextFieldName, gridBagConstraints);
		
		jLabel2.setText(NewDialogResources.getString("IDS_NAMESPACE")); // NOI18N
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		//gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
		jPanel1.add(jLabel2, gridBagConstraints);
		
		// cvc - CR#6269238
		//  make namespace uneditable (select options only)
		// jComboBox1.setEditable(true);
		jComboBox1.setEditable(false);
		//jComboBox1.setPreferredSize(new java.awt.Dimension(120, 25));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		//gridBagConstraints.weighty = 0.1;
		jPanel1.add(jComboBox1, gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		//gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(jPanel1, gridBagConstraints);
		
		
//		jTextFieldElement.setEditable(false);
//		jTextFieldElement.setText("jTextField2");
//		jTextFieldElement.setBorder(null);
//		gridBagConstraints = new java.awt.GridBagConstraints();
//		gridBagConstraints.gridx = 1;
//		gridBagConstraints.gridy = 0;
//		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//		gridBagConstraints.insets = new java.awt.Insets(0, 5, 10, 0);
//		add(jTextFieldElement, gridBagConstraints);
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
		INewDialogTabDetails result = getResults();
		NewDialogResultProcessor processor = new NewDialogResultProcessor();
		processor.handleResult(result);
	}
	
	/**
	 * This method matches the selected element's bundle file value (PSK value)
	 * in the wizard's JList to its static final integer value (NEK value)
	 */
	private INewDialogTabDetails getResults()
	{
		INewDialogElementDetails details = null;
		ETPairT< Boolean, String > pair = validData();
		boolean valid = pair.getParamOne().booleanValue();
		String msg = pair.getParamTwo();
		if (valid && msg == null)
		{
			details = new NewDialogElementDetails();
			
			// Get the kind of Element to create
			String selOnTab = (String)list.getSelectedValue();
			
			// CR#6263225 cvc
			//  added arrays to NewElementKind to make the
			//  maintenance of adding/changing/removing elements much easier
			//	switch/case logic no longer needed
			
			String eleDisplayName =
				StringTokenizer2.replace(selOnTab, " ", ""); // NOI18N
			
			List eleNameList = Arrays.asList(NewElementKind.ELEMENT_NAMES);
			int index = eleNameList.indexOf(eleDisplayName);
			
			if (index == -1)
				// "None" element type
				details.setElementKind(
					NewElementKind.ELEMENT_NUMBERS[0].intValue());
			else
				details.setElementKind(
					NewElementKind.ELEMENT_NUMBERS[index].intValue());
			
//			if (selOnTab.equals(NewDialogResources.getString("PSK_ATTRIBUTE")))
//				details.setElementKind(NewElementKind.NEK_ATTRIBUTE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_ACTOR")))
//				details.setElementKind(NewElementKind.NEK_ACTOR);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_CLASS")))
//				details.setElementKind(NewElementKind.NEK_CLASS);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_DATATYPE")))
//				details.setElementKind(NewElementKind.NEK_DATATYPE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_INTERFACE")))
//				details.setElementKind(NewElementKind.NEK_INTERFACE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_OPERATION")))
//				details.setElementKind(NewElementKind.NEK_OPERATION);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_USE_CASE")))
//				details.setElementKind(NewElementKind.NEK_USE_CASE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_ALIASED_TYPE")))
//				details.setElementKind(NewElementKind.NEK_ALIASED_TYPE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_ARTIFACT")))
//				details.setElementKind(NewElementKind.NEK_ARTIFACT);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_ENUMERATION")))
//				details.setElementKind(NewElementKind.NEK_ENUMERATION);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_NODE")))
//				details.setElementKind(NewElementKind.NEK_NODE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_INVOCATION_NODE")))
//				details.setElementKind(NewElementKind.NEK_INVOCATION_NODE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_INITIAL_NODE")))
//				details.setElementKind(NewElementKind.NEK_INITIAL_NODE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_ACTIVITY_FINAL_NODE")))
//				details.setElementKind(NewElementKind.NEK_ACTIVITY_FINAL_NODE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_ACTIVITY_FLOW_FINAL_NODE")))
//				details.setElementKind(NewElementKind.NEK_ACTIVITY_FLOW_FINAL_NODE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_DECISION_MERGE_NODE")))
//				details.setElementKind(NewElementKind.NEK_DECISION_MERGE_NODE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_ABORTED_FINAL_STATE")))
//				details.setElementKind(NewElementKind.NEK_ABORTED_FINAL_STATE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_COMPOSITE_STATE")))
//				details.setElementKind(NewElementKind.NEK_COMPOSITE_STATE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_COMPONENT")))
//				details.setElementKind(NewElementKind.NEK_COMPONENT);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_DATA_STORE_NODE")))
//				details.setElementKind(NewElementKind.NEK_DATA_STORE_NODE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_DERIVATION_CLASSIFIER")))
//				details.setElementKind(NewElementKind.NEK_DERIVATION_CLASSIFIER);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_ENUMERATION_LITERAL")))
//				details.setElementKind(NewElementKind.NEK_ENUMERATION_LITERAL);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_FINAL_STATE")))
//				details.setElementKind(NewElementKind.NEK_FINAL_STATE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_FORK_STATE")))
//				details.setElementKind(NewElementKind.NEK_FORK_STATE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_INITIAL_STATE")))
//				details.setElementKind(NewElementKind.NEK_INITIAL_STATE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_JOIN_FORK_NODE")))
//				details.setElementKind(NewElementKind.NEK_JOIN_FORK_NODE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_JOIN_STATE")))
//				details.setElementKind(NewElementKind.NEK_JOIN_STATE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_JUNCTION_STATE")))
//				details.setElementKind(NewElementKind.NEK_JUNCTION_STATE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_LIFELINE")))
//				details.setElementKind(NewElementKind.NEK_LIFELINE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_PARAMETER_USAGE_NODE")))
//				details.setElementKind(NewElementKind.NEK_PARAMETER_USAGE_NODE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_SIGNAL_NODE")))
//				details.setElementKind(NewElementKind.NEK_SIGNAL_NODE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_SIMPLE_STATE")))
//				details.setElementKind(NewElementKind.NEK_SIMPLE_STATE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_STATE")))
//				details.setElementKind(NewElementKind.NEK_STATE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_STOP_STATE")))
//				details.setElementKind(NewElementKind.NEK_STOP_STATE);
//			else if (selOnTab.equals(NewDialogResources.getString("PSK_USE_CASE_DETAIL")))
//				details.setElementKind(NewElementKind.NEK_USE_CASE_DETAIL);
			
			//get the name
			details.setName(jTextFieldName.getText());
			
			// Get the namespace
			INamespace pSelectedNamespace =
				NewDialogUtilities.getNamespace(
				(String)jComboBox1.getSelectedItem());
			details.setNamespace(pSelectedNamespace);
		}
		
		else
		{
			if (msg != null && msg.length() > 0)
			{
				IWizardSheet parent = getParentSheet();
				IErrorDialog errorDialog = 
					new SwingErrorDialog((WizardSheet)parent);
				String tempTitle = 
					NewDialogResources.getString("IDS_ERROR_TITLE"); // NOI18N
				errorDialog.display( msg, tempTitle );
			}
		}
		return details;
	}
	
	/**
	 * Returns true if the ok button should be enabled
	 *
	 * @param message [out] A message indicating why the data isn't valid.
	 */
	private ETPairT< Boolean, String > validData()
	{
		boolean bDataIsValid = false;
		String message = null;
		ETPairT< Boolean, String > retVals = null;
		
		String sElementName = jTextFieldName.getText();
		if (sElementName != null && sElementName.length() > 0)
		{
			// Get the namespace
			INamespace pSelectedNamespace = NewDialogUtilities
				.getNamespace((String)jComboBox1.getSelectedItem());
			if ( pSelectedNamespace != null )
			{
				bDataIsValid = true;
				int selectedItem = list.getSelectedIndex();
				if (selectedItem == -1)
				{
					// Nothing selected
					message = NewDialogResources
						.getString("IDS_PLEASESELECTAELEMENT"); // NOI18N
					bDataIsValid = false;
				}
				
				if (bDataIsValid)
				{
					String selOnTab = (String)list.getSelectedValue();

					// CR#6263225 - cvc
					//  naming conventions for elements makes this easy
					//  the display name, "Abc Xyz" should correspond to its
					//  id name "AbcXyz" (removed spaces)
					String sElementTypeToLookFor =
						StringTokenizer2.replace(selOnTab, " ", ""); // NOI18N
					
					if (sElementTypeToLookFor != null && 
						sElementTypeToLookFor.length() > 0)
					{
						if (Util.hasNameCollision(pSelectedNamespace,sElementName, sElementTypeToLookFor, null))
						{	
							bDataIsValid = false;
							DialogDisplayer.getDefault().notify(
								new NotifyDescriptor.Message(NbBundle.getMessage(
								NewElementUI.class, "IDS_NAMESPACECOLLISION")));
						}
					}	
				}
			}
			else
			{
				// Something bad happened.  We added a namespace to the combo
				// and couldn't get it back out!
				message = NewDialogResources.getString(
					"IDS_FAILEDTOGETNAMESPACE"); // NOI18N
			}
		}
		else
		{
			message = NewDialogResources.getString(
				"IDS_PLEASEENTERELEMENTNAME"); // NOI18N
		}
		
		if( null == retVals )
		{
			retVals = new ETPairT<Boolean, String>(
				new Boolean(bDataIsValid), message );
		}
		
		return retVals;
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
	
	

	
	/**
	 * @param selOnTab
	 */
	public static void setElementType(String selOnTab)
	{
		jTextFieldElement.setText(selOnTab);
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
	 * This method will create an element based on user input
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
//				ETSystem.out.println("Error in create Element");
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
		String text = NewDialogResources.getString("IDS_ADDING") // NOI18N
			+ jTextFieldElement.getText();
		return  text;
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogUI#getHelpIcon()
	 */
	public Icon getHelpIcon()
	{
		Icon icon = null;
		icon = NewDialogUtilities.getIconForResource(jTextFieldElement.getText());
		return icon;
	}
	// End of variables declaration
	
	public boolean enableNextButton()
	{
		return false;
	}

	
//	private class ElementList extends JList //implements Scrollable
//	{
//		private int maxUnitIncrement = 1;
//		
//		public Dimension getPreferredScrollableViewportSize()
//		{
//			return list.getPreferredSize();
//		}
//		
//		public int getScrollableBlockIncrement(
//			Rectangle visibleRect,
//			int orientation,
//			int direction)
//		{
//			if (orientation == SwingConstants.HORIZONTAL)
//				return visibleRect.width - maxUnitIncrement;
//			else
//				return visibleRect.height - maxUnitIncrement;
//		}
//		
//		public boolean getScrollableTracksViewportHeight()
//		{
//			return false;
//		}
//		
//		public boolean getScrollableTracksViewportWidth()
//		{
//			return false;
//		}
//		
//		
//		public int getScrollableUnitIncrement(
//			Rectangle visibleRect, int orientation, int direction)
//		{
//			//Get the current position.
//			int currentPosition = 0;
//			if (orientation == SwingConstants.HORIZONTAL)
//			{
//				currentPosition = visibleRect.x;
//			}
//			
//			else
//			{
//				currentPosition = visibleRect.y;
//			}
//			
//			//Return the number of pixels between currentPosition
//			//and the nearest tick mark in the indicated direction.
//			if (direction == 0)
//			{
//				int newPosition = currentPosition -
//					(currentPosition / maxUnitIncrement) * maxUnitIncrement;
//				return (newPosition == 0) ? maxUnitIncrement : newPosition;
//			}
//			
//			else
//			{
//				return ((currentPosition / maxUnitIncrement) + 1)
//				* maxUnitIncrement - currentPosition;
//			}
//		}
//		
//		public void setMaxUnitIncrement(int pixels)
//		{
//			maxUnitIncrement = pixels;
//		}
//	}
	
	
	
	class ElementListCellRenderer extends JLabel
		implements ListCellRenderer
	{
		public Icon getImageIcon(String elemName)
		{
			Icon retIcon = null;
			String displayName = NewDialogResources.getStringKey(elemName);
			String str = "//PropertyDefinition/aDefinition[@name='" + // NOI18N
				"Element" + "']/aDefinition[@displayName='" +  // NOI18N
				displayName + "']"; // NOI18N
			
			org.dom4j.Node node = m_doc.selectSingleNode(str);
			if (node.getNodeType() == org.dom4j.Element.ELEMENT_NODE)
			{
				org.dom4j.Element elem = (org.dom4j.Element)node;
				String fileName = elem.attributeValue("image"); // NOI18N
				File file = new File(fileName);
				retIcon = CommonResourceManager.instance()
							.getIconForFile(fileName);
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
