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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.dom4j.Document;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;
import org.netbeans.modules.uml.ui.support.wizard.WizardInteriorPage;

/**
 * @author sumitabhk
 *
 */
public class ElementSelectionUI extends WizardInteriorPage implements INewDialogUI
{
	private static final String PG_CAPTION = NewDialogResources.getString("NewDiagramUI.NEWWIZARD_CAPTION");
	private static final String PG_TITLE = NewDialogResources.getString("IDS_TYPESELECTION");
	private static final String PG_SUBTITLE = NewDialogResources.getString("IDS_TYPESELECTIONHELP");

	private JList m_TabPane = null;
	private JTree m_Tree = null;
	private JPanel treePanel = null;
	private JPanel listPanel = null;
	private JSplitPane jSplitPane = null;
	private Document m_doc = null;
	
	/**
	 * 
	 */
//	public ElementSelectionUI()
//	{
//		super();
//		initializeControls();
//	}

	public ElementSelectionUI(IWizardSheet parent, String caption, String headerTitle, String headerSubTitle) {
		super(parent, caption, headerTitle, headerSubTitle);
		createUI();
	}

	public ElementSelectionUI(IWizardSheet parent) {
		this(parent, PG_CAPTION, PG_TITLE, PG_SUBTITLE);
	}

	protected void createUI() 
	{
		super.createUI();
		
		JPanel topPane = new JPanel();

		IConfigManager conMan = ProductRetriever.retrieveProduct().getConfigManager();
		String fileName = conMan.getDefaultConfigLocation();
		fileName += "NewDialogDefinitions.etc";
		m_doc = XMLManip.getDOMDocument(fileName);
		org.dom4j.Node node = m_doc.selectSingleNode("//PropertyDefinitions/PropertyDefinition");
		m_TabPane = new JList();
		if (node != null)
		{
			org.dom4j.Element elem = (org.dom4j.Element)node;
			String name = elem.attributeValue("name");

			Vector elements = new Vector();
			List nodeList = m_doc.selectNodes("//PropertyDefinition/aDefinition");
			if (m_TabPane != null)
			{
				int count = nodeList.size();
				for (int i=0; i<count; i++)
				{
					org.dom4j.Element subNode = (org.dom4j.Element)nodeList.get(i);
					String subName = subNode.attributeValue("name");
					String imageName = subNode.attributeValue("image");
					File file = new File(imageName);
			   
					//ETSystem.out.println(fileName + file.exists());
					Icon retIcon = CommonResourceManager.instance().getIconForFile(imageName);
					
					JLabel label = new JLabel(subName);
					label.setIcon(retIcon);

					//add workspace node
					elements.add(label);
				}
			}
			
			m_TabPane.setListData(elements);
			m_TabPane.setSelectedIndex(0);
		}

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		topPane.add(m_TabPane, gridBagConstraints);
		//m_Tree.setSelectionRow(0);
		
		pnlContents.setLayout(new GridBagLayout());
		pnlContents.setBackground(Color.white);
		pnlContents.add(topPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(12, 9, 11, 10), 204, 289));
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
		IWizardSheet parent = getParentSheet();
		if (m_TabPane != null)
		{
			Object val = m_TabPane.getSelectedValue();
			if (val != null && val instanceof String)
			{
				if (parent instanceof JDefaultNewDialog)
				{
					String str = (String)val;
					if (val.equals("Workspace"))
					{
						//add next page as workspace page.
						((JDefaultNewDialog)parent).setNextPage(new NewWorkspaceUI(parent), "Create Workspace");
					}
					else if (val.equals("Project"))
					{
						//add next page as workspace page.
						((JDefaultNewDialog)parent).setNextPage(new NewProjectUI(parent), "Create Project");
					}
					else if (val.equals("Diagram"))
					{
						//add next page as workspace page.
						((JDefaultNewDialog)parent).setNextPage(new NewDiagramSelectionUI(parent), "Select Diagram Type");
						((JDefaultNewDialog)parent).setNextPage(new NewDiagramUI(parent), "Create Diagram");
					}
					else if (val.equals("Element"))
					{
						//add next page as workspace page.
						((JDefaultNewDialog)parent).setNextPage(new NewElementSelectionUI(parent), "Select Element Type");
						((JDefaultNewDialog)parent).setNextPage(new NewElementSelectionUI(parent), "Create Project");
					}
					else if (val.equals("Package"))
					{
						//add next page as workspace page.
						((JDefaultNewDialog)parent).setNextPage(new NewPackageUI(parent), "Create Package");
					}
				}
			}
		}
		super.onWizardNext();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogUI#nextButtonClicked()
	 */
	public JPanel nextButtonClicked()
	{
		// TODO Auto-generated method stub
		//need to show the user right dialog based on what is selected.
		JPanel retPanel = showWindowForUserInput();
		return retPanel;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogUI#finishButtonClicked()
	 */
	public INewDialogTabDetails finishButtonClicked()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	protected void initializeControls()
	{
		m_TabPane = new JList();
		m_TabPane.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_TabPane.setCellRenderer(new ElementListCellRenderer());
		m_TabPane.setPreferredSize(new Dimension(200,200));
		m_TabPane.setLayout(new GridBagLayout());
		
		IConfigManager conMan = ProductRetriever.retrieveProduct().getConfigManager();
		String fileName = conMan.getDefaultConfigLocation();
		fileName += "NewDialogDefinitions.etc";
		m_doc = XMLManip.getDOMDocument(fileName);
		org.dom4j.Node node = m_doc.selectSingleNode("//PropertyDefinitions/PropertyDefinition");
		if (node != null)
		{
			org.dom4j.Element elem = (org.dom4j.Element)node;
			String name = elem.attributeValue("name");
			DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(name);
			NewDialogTreeModel model = new NewDialogTreeModel(treeNode);
			m_Tree = new JTree(model);
			m_Tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			m_Tree.setShowsRootHandles(true);
			m_Tree.addTreeSelectionListener(new ElementTreeSelectionListener());
		}

		jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, m_Tree, m_TabPane);
		setPreferredSize(new Dimension(400, 400));      
		jSplitPane.setDividerLocation(200);
	   
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		add(jSplitPane, gridBagConstraints);
		doLayout();
		m_Tree.setSelectionRow(0);
	}

	private JPanel showWindowForUserInput()
	{
		JPanel retPanel = null;
		
		String selOnTab = getSelectionOnTabPane();
		String selOnTree = getSelectionOnTree();
		
//		if (selOnTree.equals("Workspace"))
//		{
//			NewWorkspaceUI newUI = new NewWorkspaceUI();
//			newUI.setElementType(selOnTab);
//			retPanel = newUI;
//		}
//		else if (selOnTree.equals("Project"))
//		{
//			NewProjectUI newUI = new NewProjectUI();
//			newUI.setElementType(selOnTab);
//			retPanel = newUI;
//		}
//		else if (selOnTree.equals("Package"))
//		{
//			NewPackageUI newUI = new NewPackageUI();
//			newUI.setElementType(selOnTab);
//			retPanel = newUI;
//		}
//		else if (selOnTree.equals("Diagram"))
//		{
//			NewDiagramUI newUI = new NewDiagramUI();
//			newUI.setElementType(selOnTab);
//			retPanel = newUI;
//		}
//		else if (selOnTree.equals("Element"))
//		{
//			NewElementUI newUI = new NewElementUI();
//			newUI.setElementType(selOnTab);
//			retPanel = newUI;
//		}
		
		return retPanel;
	}

	public String getSelectionOnTabPane()
	{
		try
		{
			return (String)m_TabPane.getSelectedValue();
		}
		catch (Exception e)
		{
			//do nothing, as when nothing is selected this will throw.
		}
		return "";
	}
	
	public String getSelectionOnTree()
	{
		String str = null;
		TreePath path = m_Tree.getSelectionPath();
		if (path != null)
		{
			Object obj = m_Tree.getLastSelectedPathComponent();
			if (obj instanceof DefaultMutableTreeNode)
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
				Object objStr = node.getUserObject();
				if (objStr != null)
				{
					str = (String)objStr;
				}
			}
		}
		return str;
	}


	private class ElementTreeSelectionListener implements TreeSelectionListener
	{

		/* (non-Javadoc)
		 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
		 */
		public void valueChanged(TreeSelectionEvent arg0)
		{
			// TODO Auto-generated method stub
			Object obj = arg0.getSource();
			if (obj != null && obj instanceof JTree)
			{
				JTree tree = (JTree)obj;
				TreePath path = tree.getSelectionPath();
				if (path != null)
				{
					int i = m_Tree.getRowForPath(path);
					Object selObj = m_Tree.getLastSelectedPathComponent();
					if (selObj instanceof DefaultMutableTreeNode)
					{
						DefaultMutableTreeNode node = (DefaultMutableTreeNode)selObj;
						Object objStr = node.getUserObject();
						if (objStr != null)
						{
							populateRightHandSide((String)objStr);
						}
					}
					//ETSystem.out.println("Selected row = " + i);
				}
				//path.get
				
			}
		}

		/**
		 * @param string
		 */
		private void populateRightHandSide(String str)
		{
			Vector elements = getValidItemsToDisplay(str);
			if (elements != null)
			{
				m_TabPane.setListData(elements);
				m_TabPane.setSelectedIndex(0);
			}
		}

		/**
		 * @param str
		 * @return
		 */
		private Vector<String> getValidItemsToDisplay(String str)
		{
			Vector<String> retObj = new Vector<String>();
			// TODO Auto-generated method stub
			
			List list = m_doc.selectNodes("//PropertyDefinition/aDefinition[@name='" + str + "']/aDefinition");
			if (list != null)
			{
				int count = list.size();
				for (int i=0; i<count; i++)
				{
					org.dom4j.Element ele = (org.dom4j.Element)list.get(i);
					String name = ele.attributeValue("name");
					if (name != null)
						retObj.add(name);
					else
						retObj.add("");
				}
			}
			
//			if (str.equals("Workspace"))
//			{
//				retObj.add("Workspace");
//			}
//			else if (str.equals("Project"))
//			{
//				retObj.add("Project");
//			}
//			else if (str.equals("Package"))
//			{
//				retObj.add("Package");
//			}
//			else if (str.equals("Diagram"))
//			{
//				retObj.add("Class Diagram");
//				retObj.add("Sequence Diagram");
//				retObj.add("Activity Diagram");
//				retObj.add("Collaboration Diagram");
//				retObj.add("Component Diagram");
//				retObj.add("Deployment Diagram");
//				retObj.add("State Diagram");
//				retObj.add("Use Case Diagram");
//			}
//			else if (str.equals("Element"))
//			{
//				retObj.add("Interface");
//				retObj.add("Class");
//				retObj.add("Actor");
//				retObj.add("Attribute");
//				retObj.add("Operation");
//			}
//			else
//			{
//				retObj.add(" ");
//			}
			return retObj;
		}
	}

	public class NewDialogTreeModel extends DefaultTreeModel 
	{

		/**
		 * 
		 */
		public NewDialogTreeModel(TreeNode node)
		{
			super(node);
			buildChildren();
		
			//collapse the root node
		}

		/**
		 * 
		 */
		private void buildChildren()
		{
			// TODO Auto-generated method stub
			Object obj = this.getRoot();
			if (obj instanceof DefaultMutableTreeNode)
			{
				DefaultMutableTreeNode root = (DefaultMutableTreeNode)obj;
			
				List list = m_doc.selectNodes("//PropertyDefinition/aDefinition");
				if (list != null)
				{
					int count = list.size();
					for (int i=0; i<count; i++)
					{
						org.dom4j.Element node = (org.dom4j.Element)list.get(i);
						String name = node.attributeValue("name");

						//add workspace node
						DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name);
						root.add(newNode);
					}
				}
				//add workspace node
//				DefaultMutableTreeNode wksNode = new DefaultMutableTreeNode("Workspace");
//				DefaultMutableTreeNode prjNode = new DefaultMutableTreeNode("Project");
//				DefaultMutableTreeNode pkgNode = new DefaultMutableTreeNode("Package");
//				DefaultMutableTreeNode diaNode = new DefaultMutableTreeNode("Diagram");
//				DefaultMutableTreeNode eleNode = new DefaultMutableTreeNode("Element");
//
//				root.add(wksNode);
//				root.add(prjNode);
//				root.add(pkgNode);
//				root.add(diaNode);
//				root.add(eleNode);
			}
		}
	}

	class ElementListCellRenderer extends JLabel implements ListCellRenderer 
	{
		public Icon getImageIcon(int index)
		{
			Icon retIcon = null;
			int i = index + 1;
			String str = "//PropertyDefinition/aDefinition[@name='" + getSelectionOnTree() + "']/aDefinition[" + i + "]";
		   org.dom4j.Node node = m_doc.selectSingleNode(str);
		   //ETSystem.out.println("Searching for " + str);
		   if (node.getNodeType() == org.dom4j.Element.ELEMENT_NODE)
		   {
			   org.dom4j.Element elem = (org.dom4j.Element)node;
			   String fileName = elem.attributeValue("image");
			   File file = new File(fileName);
			   
			   //ETSystem.out.println(fileName + file.exists());
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


	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogUI#getHelpText()
	 */
	public String getHelpText()
	{
		return NewDialogResources.getString("IDS_ITEMTOCREATE");
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.newdialog.INewDialogUI#getHelpIcon()
	 */
	public Icon getHelpIcon()
	{
		return null;
	}
	
	public boolean enableNextButton()
	{
		String selOnTab = getSelectionOnTabPane();
		if (selOnTab == null || selOnTab.length() == 0)
		{
			return false;
		}
		return true;
	}
}


