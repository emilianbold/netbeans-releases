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



package org.netbeans.modules.uml.ui.controls.newdialog;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import org.dom4j.Document;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.ui.support.UserSettings;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;


/**
 * @author sumitabhk
 *
 */
public class NewElementSelectionUI extends NewDialogWizardPage
{
	private String origLocation = null;

	private static final String PG_CAPTION = NewDialogResources.getString("NewDiagramUI.NEWWIZARD_CAPTION");
	private static final String PG_TITLE = NewDialogResources.getString("IDS_SELECTELEMENT");
	private static final String PG_SUBTITLE = NewDialogResources.getString("IDS_SELECTELEMENTHELP");
	private Document m_doc = null;
	private JList list = new JList();
	private INewDialogElementDetails m_Details;


	public NewElementSelectionUI(IWizardSheet parent, String caption, String headerTitle, String headerSubTitle) {
		super(parent);//, caption, headerTitle, headerSubTitle);
		createUI();
	}

	public NewElementSelectionUI(IWizardSheet parent) {
		this(parent, PG_CAPTION, PG_TITLE, PG_SUBTITLE);
	}

	public NewElementSelectionUI(IWizardSheet parent, INewDialogTabDetails details) 
	{
		super(parent);
		if (details instanceof INewDialogElementDetails)
		{
			m_Details = (INewDialogElementDetails)details;
		}
		createUI();
	}

	protected void createUI() 
	{
		m_title.setText(PG_TITLE);
		m_subTitle.setText(PG_SUBTITLE);
		super.createUI();

		IConfigManager conMan = ProductRetriever.retrieveProduct().getConfigManager();
		String fileName = conMan.getDefaultConfigLocation();
		fileName += "NewDialogDefinitions.etc";
		m_doc = XMLManip.getDOMDocument(fileName);
		org.dom4j.Node node = m_doc.selectSingleNode("//PropertyDefinitions/PropertyDefinition");
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new ElementListCellRenderer());
		list.setLayout(new GridBagLayout());

		if (node != null)
		{
			org.dom4j.Element elem = (org.dom4j.Element)node;
			String name = elem.attributeValue("name");

			Vector elements = new Vector();
			List nodeList = m_doc.selectNodes("//PropertyDefinition/aDefinition[@name='" + "Element" + "']/aDefinition");
			if (list != null)
			{
				int count = nodeList.size();
				for (int i=0; i<count; i++)
				{
					org.dom4j.Element subNode = (org.dom4j.Element)nodeList.get(i);
					String subName = subNode.attributeValue("name");

					//add workspace node
					elements.add(subName);
				}
			}
			list.setListData(elements);
			UserSettings userSettings = new UserSettings();
			if (userSettings != null)
			{
				String value = userSettings.getSettingValue("NewDialog", "LastChosenElementType");
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

		list.setBorder(new TitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 12)));
		//m_DetailPanel.setBackground(Color.WHITE);

		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 25;
		m_DetailPanel.add(list, gridBagConstraints);
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
		NewElementUI.setElementType((String)list.getSelectedValue());
		UserSettings userSettings = new UserSettings();
		if (userSettings != null)
		{
			// write it back out to the ini file
			userSettings.setSettingValue("NewDialog", "LastChosenElementType", (String)list.getSelectedValue());
		}
		super.onWizardNext();
	}

	class ElementListCellRenderer extends JLabel implements ListCellRenderer 
	{
		public Icon getImageIcon(int index)
		{
			Icon retIcon = null;
			int i = index + 1;
			String str = "//PropertyDefinition/aDefinition[@name='" + "Element" + "']/aDefinition[" + i + "]";
		   org.dom4j.Node node = m_doc.selectSingleNode(str);
		   if (node.getNodeType() == org.dom4j.Element.ELEMENT_NODE)
		   {
			   org.dom4j.Element elem = (org.dom4j.Element)node;
			   String fileName = elem.attributeValue("image");
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
			 setIcon(getImageIcon(index));
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



