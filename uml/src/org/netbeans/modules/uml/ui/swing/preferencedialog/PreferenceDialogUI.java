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

package org.netbeans.modules.uml.ui.swing.preferencedialog;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.tree.DefaultMutableTreeNode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceManagerEventsAdapter;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.commondialogs.IErrorDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.drawingproperties.FontChooser;
import org.netbeans.modules.uml.ui.support.drawingproperties.FontColorDialogs.ApplicationColorsAndFonts;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingErrorDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;

/**
 * @author sumitabhk
 *
 */
public class PreferenceDialogUI extends JCenterDialog
{
	// Variables declaration - do not modify
	private javax.swing.JPanel jAdvancedPanel;
	private javax.swing.JPanel jDetailsSplitPanel;
	private JSplitPane jSplitPane;
	private javax.swing.JButton jDefaultsButton;
	private javax.swing.JButton jOKButton;
	private javax.swing.JButton jCancelButton;
	private javax.swing.JPanel jButtonsPanel;
	private javax.swing.JPanel jCategoryPanel;
	private javax.swing.JCheckBox jCheckBox1;
	private javax.swing.JPanel jDetailsPanel;
	private javax.swing.JPanel jHelpPanel;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JTable jTable1;
	private javax.swing.JTextArea jTextArea1;
	private JScrollPane jScrollPaneTextArea;
	private JPreferenceDialogTree jTree1;
	private PreferenceDialogTableModel m_TableModel = null;

	
	private boolean m_Advanced = false;
	private boolean m_isFileReadOnly = false;
	
	private IPreferenceManagerEventsSink m_EventsSink = null;
	// End of variables declaration
    
	/** Creates new form PreferenceDialogUI */
	public PreferenceDialogUI(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
		jTree1.setSelectionRow(0);
		center(parent);
	}
    
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		jCategoryPanel = new javax.swing.JPanel();
		PreferenceDialogTreeModel model = new PreferenceDialogTreeModel(this);
		jTree1 = new JPreferenceDialogTree(model, this);
		jScrollPane1 = new javax.swing.JScrollPane(jTree1);
		jDetailsPanel = new javax.swing.JPanel();
//		jScrollPane2 = new javax.swing.JScrollPane();
//		jTable1 = new javax.swing.JTable();

                
                jAdvancedPanel = new javax.swing.JPanel();
		jCheckBox1 = new javax.swing.JCheckBox();
		jLabel1 = new javax.swing.JLabel();
		jHelpPanel = new javax.swing.JPanel();
		jTextArea1 = new javax.swing.JTextArea(" ");
		jTextArea1.setEditable(false);
		jButtonsPanel = new javax.swing.JPanel();
		jDefaultsButton = new javax.swing.JButton();
		jOKButton = new javax.swing.JButton();
		jCancelButton = new javax.swing.JButton();
		jDetailsSplitPanel = new javax.swing.JPanel();
		jDetailsSplitPanel.setLayout(new java.awt.GridBagLayout());

		setTitle(DefaultPreferenceDialogResource.getString("IDS_PREFERENCES"));

		getContentPane().setLayout(new java.awt.GridBagLayout());

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
			public void windowClosed(java.awt.event.WindowEvent e)
			{
			   //System.exit(0);
			}
		});
		
		jCancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0)
			{
				performCancelAction();
			}
		});

		jOKButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0)
			{
				if (jTable1.isEditing())
				{
					//we were editing this table, need to save the last edit.
					jTable1.getCellEditor().stopCellEditing();
				}
				performSaveAction(true);
			}
		});
                
                
		jDefaultsButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0)
			{                            
				performRestoreDefaultsAction();
			}
		});                

          
                int fontsize;
                java.awt.Font f = 
                    javax.swing.UIManager.getFont ("controlFont"); //NOI18N
                if (f != null) {
                    fontsize = f.getSize();
                } else {
                    fontsize = 12;
                }    
                java.awt.Font  theFont = new Font("Dialog", 0, fontsize);
                
		jCategoryPanel.setLayout(new java.awt.GridBagLayout());

		jCategoryPanel.setBorder(new javax.swing.border.TitledBorder(DefaultPreferenceDialogResource.getString("IDS_CATEGORIES")));
		jCategoryPanel.setFont(theFont);
		jTree1.setShowsRootHandles(true);
		jScrollPane1.setViewportView(jTree1);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		jCategoryPanel.add(jScrollPane1, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
		//gridBagConstraints.weightx = 0.5;
		gridBagConstraints.weighty = 0.5;
//		getContentPane().add(jCategoryPanel, gridBagConstraints);
		jDetailsSplitPanel.add(jCategoryPanel, gridBagConstraints);

		jDetailsPanel.setLayout(new java.awt.GridBagLayout());

		jDetailsPanel.setBorder(new javax.swing.border.TitledBorder(DefaultPreferenceDialogResource.getString("IDS_DETAILS")));
		jDetailsPanel.setFont(theFont);
		
		m_TableModel = new PreferenceDialogTableModel(this);
		jTable1 = new JPreferenceDialogTable(m_TableModel, this);
//		jTable1.setModel(new javax.swing.table.DefaultTableModel(
//			new Object [][] {
//				{null, null, null, null},
//				{null, null, null, null},
//				{null, null, null, null},
//				{null, null, null, null}
//			},
//			new String [] {
//				"Title 1", "Title 2", "Title 3", "Title 4"
//			}
//		));
		//jTable1.setMinimumSize(new java.awt.Dimension(450, 400));
		//jTable1.setPreferredSize(new java.awt.Dimension(300, 400));
		jScrollPane2 = new javax.swing.JScrollPane(jTable1);
		//jScrollPane2.setViewportView(jTable1);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		jDetailsPanel.add(jScrollPane2, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridheight = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.weighty = 0.5;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
//		getContentPane().add(jDetailsPanel, gridBagConstraints);

		jAdvancedPanel.setLayout(new java.awt.GridBagLayout());

		jCheckBox1.setFont(theFont);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
		jAdvancedPanel.add(jCheckBox1, gridBagConstraints);
		jCheckBox1.setEnabled(true);
		
		jCheckBox1.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0)
			{
				Object obj = arg0.getSource();
				if (obj instanceof JCheckBox)
				{
					JCheckBox box = (JCheckBox)obj;
					boolean selected = box.isSelected();
					if (m_Advanced != selected)
					{
						m_Advanced = selected;
						jTree1.reload();
						jTree1.doLayout();
						//refreshUI();
					}
				}
			}
		});

		jLabel1.setFont(theFont);
		jLabel1.setText(DefaultPreferenceDialogResource.determineText(DefaultPreferenceDialogResource.getString("IDS_ADVANCED")));
		DefaultPreferenceDialogResource.setMnemonic(jLabel1, DefaultPreferenceDialogResource.getString("IDS_ADVANCED"));
		DefaultPreferenceDialogResource.setMnemonic(jCheckBox1, DefaultPreferenceDialogResource.getString("IDS_ADVANCED"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
		jAdvancedPanel.add(jLabel1, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
//		getContentPane().add(jAdvancedPanel, gridBagConstraints);
		jDetailsSplitPanel.add(jAdvancedPanel, gridBagConstraints);

		jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jDetailsSplitPanel, jDetailsPanel);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.weighty = 0.5;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		getContentPane().add(jSplitPane, gridBagConstraints);
		jSplitPane.setDividerLocation(200);

		jHelpPanel.setLayout(new java.awt.GridBagLayout());
		jHelpPanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
		//jHelpPanel.setMinimumSize(new java.awt.Dimension(2, 30));
		//jHelpPanel.setPreferredSize(new java.awt.Dimension(2, 30));
		
		jTextArea1.setEditable(false);
		//jTextArea1.setEnabled(false);
		jTextArea1.setRows(2);
		jTextArea1.setOpaque(false);
		jTextArea1.setLineWrap(true);
		jTextArea1.setWrapStyleWord(true);
		jTextArea1.setFont(theFont);
		jTextArea1.setMargin(new java.awt.Insets(2, 2, 2, 2));
		jScrollPaneTextArea = new JScrollPane(jTextArea1);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		jHelpPanel.add(jScrollPaneTextArea, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.weighty = 0.1;
		getContentPane().add(jHelpPanel, gridBagConstraints);

		//jButtonsPanel.setLayout(new java.awt.GridBagLayout());
		jButtonsPanel.setLayout(new BorderLayout());

		jDefaultsButton.setFont(theFont);
		jDefaultsButton.setText(DefaultPreferenceDialogResource.determineText(DefaultPreferenceDialogResource.getString("IDS_RESTOREDEFAULTS")));
		DefaultPreferenceDialogResource.setMnemonic(jDefaultsButton, DefaultPreferenceDialogResource.getString("IDS_RESTOREDEFAULTS"));

		Box pane = Box.createHorizontalBox();
		pane.add(Box.createHorizontalGlue());
		pane.add(jDefaultsButton);
		jButtonsPanel.add(pane, BorderLayout.WEST);

		Dimension buttonSize = new Dimension(75, 25);

		jOKButton.setFont(theFont);
		jOKButton.setText(DefaultPreferenceDialogResource.getString("IDS_OK"));
		//jOKButton.setPreferredSize(buttonSize);
		//jOKButton.setMaximumSize(buttonSize);
		pane = Box.createHorizontalBox();
		pane.add(Box.createHorizontalGlue());
		pane.add(jOKButton);
		pane.add(Box.createHorizontalStrut(1));
		pane.add(Box.createHorizontalGlue());
		pane.add(jCancelButton);
		jButtonsPanel.add(pane, BorderLayout.EAST);

		jCancelButton.setFont(theFont);
		jCancelButton.setText(DefaultPreferenceDialogResource.getString("IDS_CANCEL"));
		//jCancelButton.setPreferredSize(buttonSize);
		//jCancelButton.setMaximumSize(buttonSize);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.fill = gridBagConstraints.BOTH;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 5);
		getContentPane().add(jButtonsPanel, gridBagConstraints);

		pack();
                
                //CBeckham - cahnged to allow for dynamic panel size based on fontsize
		//setSize(new Dimension(600,500));
		setSize(new Dimension(setPanelSize()));
                
		//now I want to register the preference events sink.
		initializePreferenceEventsSink();
		
		initializeButtonStates();
	}
        
	/**
	 * Enables us to adjust the panel size to handle larger fonts
	 * CBeckham
	 */        
         private Dimension setPanelSize() {
            
                int fontsize;
                java.awt.Font f = 
                    javax.swing.UIManager.getFont ("controlFont"); //NOI18N
                if (f != null) {
                    fontsize = f.getSize();
                } else {
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
    
	/**
	 * Enables/disables buttons. If the file is read-only,
	 * we should not allow user to restore defaults.
	 */
	private void initializeButtonStates()
	{
		Object obj = jTree1.getModel().getRoot();
		if (obj != null && obj instanceof DefaultMutableTreeNode)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
			Object userObj = node.getUserObject();
			if (userObj != null && userObj instanceof IPropertyElement)
			{
				IPropertyElement pEle = (IPropertyElement)userObj;
				IPreferenceManager2 prefMan = ProductRetriever.retrieveProduct().getPreferenceManager();
				if (prefMan != null)
				{
					if (prefMan.isEditable(pEle))
					{
						m_isFileReadOnly = false;
					}
					else
					{
						m_isFileReadOnly = true;
						
						//disable the defaults and OK button
						jDefaultsButton.setEnabled(false);
						jOKButton.setEnabled(false);
					}
				}
			}
		}
	}

	/**
	 * 
	 */
	private void initializePreferenceEventsSink()
	{
		if (m_EventsSink == null)
		{
			m_EventsSink = new PreferenceManagerEventsAdapter();
		}
		if (m_EventsSink != null)
		{
			DispatchHelper helper = new DispatchHelper();
			helper.registerForPreferenceManagerEvents(m_EventsSink);
		}
	}

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) {
		setVisible(false);
		dispose();
	}
    
    public void refreshUI()
    {
    	this.doLayout();
    	this.setVisible(true);
    	this.paintAll(this.getGraphics());
    }
    
	/**
	 * Load the preferences into the preference tree by asking the preference manager for
	 * the information.  The preference manager has built its information upon starting the
	 * application from the preferences files.  This control just takes that information and
	 * loads it into the appropriate grids.
	 *
	 * @return HRESULT
	 */
	public Vector<DefaultMutableTreeNode> loadTree()
	{
		Vector<DefaultMutableTreeNode> children = new Vector<DefaultMutableTreeNode>();
		IPreferenceManager2 prefMan = ProductRetriever.retrieveProduct().getPreferenceManager();
		if (prefMan != null)
		{
			IPropertyElement[] elems = prefMan.getPropertyElements();
			if (elems != null)
			{
				// loop through them and add them to the tree
				int count = elems.length;
				for (int i=0; i<count; i++)
				{
					IPropertyElement pEle = elems[i];
					IPropertyDefinition pDef = pEle.getPropertyDefinition();
					if (pDef != null)
					{
						// Now add it to the tree
						String name = pDef.getDisplayName();
						boolean load = true;
						String advancedStr = pDef.getFromAttrMap("advanced");
						if (advancedStr != null && advancedStr.equals("PSK_TRUE") && !m_Advanced)
						{
							load = false;
						}
						
						if (load)
						{
							DefaultMutableTreeNode node = new DefaultMutableTreeNode(pEle);
							children.add(node);
							loadTreeWithSubElements(node, pDef, pEle);
							//node.setExpanded(true);
							
						}
					}
				}
			}
		}
		return children;
	}

	/**
	 * Take the passed-in information and build child nodes in the preference tree.
	 *
	 * @param pNode[in]		The parent grid node
	 * @param pDef[in]		The property definition associated with the passed in property element
	 * @param pEle[in]		The property element in which to process its child elements
	 *
	 * @return HRESULT
	 */
	private void loadTreeWithSubElements(DefaultMutableTreeNode node, 
										IPropertyDefinition pDef, 
										IPropertyElement pEle)
	{
		// The structure of the preference tree is to only display things in the tree that have of a certain
		// level of child nodes, so this is checking to see if the current element should have a node created
		// in the preference tree or not
		if (hasGrandChildren(pEle))
		{
			// loop through its sub "child" elements
			Vector<IPropertyElement> subElems = pEle.getSubElements();
			if (subElems != null)
			{
				int count = subElems.size();
				for (int i=0; i<count; i++)
				{
					IPropertyElement subEle = subElems.elementAt(i);

					// if this element has child elements, then we want to add it to the preference tree
					if (hasChildren(subEle))
					{
						IPropertyDefinition subDef = subEle.getPropertyDefinition();
						if (subDef != null)
						{
							// definitions that have no display name or control type are for information only
							// not for display
							String name = subDef.getDisplayName();
							String controlType = subDef.getControlType();
							if ( (name == null || name.length() == 0) &&
								 (controlType == null || controlType.length() == 0) )
							{
								//do nothing
							}
							else
							{
								boolean load = true;
								String advancedStr = subDef.getFromAttrMap("advanced");
								if (advancedStr != null && advancedStr.equals("PSK_TRUE") && !m_Advanced)
								{
									load = false;
								}
								
								if (load)
								{
									DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(subEle);
									node.add(childNode);
									//childNode.setExpanded(false);
									loadTreeWithSubElements(childNode, subDef, subEle);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Does the passed-in property element have at least one level of children.
	 *
	 * @param pEle[in] The property element in question
	 *
	 * @return BOOL	Whether or not is has at least one level of children
	 */
	private boolean hasChildren(IPropertyElement pEle)
	{
		boolean isParent = false;
		
		// the simple fact that it has children is not good enough
		// in some cases, it could have children, but the children may only be information
		// holders, not actually displayed to the user (Fonts/Colors)
		if (pEle != null)
		{
			Vector<IPropertyElement> subElems = pEle.getSubElements();
			if (subElems != null)
			{
				int count = subElems.size();
				for (int i=0; i<count; i++)
				{
					IPropertyElement subEle = subElems.elementAt(i);
					IPropertyDefinition subDef = subEle.getPropertyDefinition();
					if (subDef != null)
					{
						// check if the child is displayed to the user
						String name = subDef.getDisplayName();
						String controlType = subDef.getControlType();
						
						if ( (name == null || name.length() == 0) &&
							 (controlType == null || controlType.length() == 0) )
						{
							//not a valid child
						}
						else
						{
							isParent = true;
							break;
						}
					}
				}
			}
		}
		
		return isParent;
	}

	/**
	 * Does the passed-in property element have at least two levels of children.
	 *
	 * @param pEle[in]	The property element in question
	 *
	 * @return BOOL	Whether or not is has at least two levels of children
	 */
	private boolean hasGrandChildren(IPropertyElement pEle)
	{
		boolean isGrandParent = false;
		if (pEle != null)
		{
			Vector<IPropertyElement> subEles = pEle.getSubElements();
			if (subEles != null)
			{
				int count = subEles.size();
				for (int i=0; i<count; i++)
				{
					IPropertyElement ele = subEles.elementAt(i);
					Vector<IPropertyElement> subElems2 = ele.getSubElements();
					if (subElems2 != null)
					{
						int count2 = subElems2.size();
						if (count2 > 0)
						{
							isGrandParent = true;
							break;
						}
					}
				}
			}
		}
		return isGrandParent;
	}
    
	public PreferenceDialogUI()
	{
		super();
		initComponents();
		jTree1.setSelectionRow(0);
	}
    
	/**
	 * @param pEle
	 * based on the property element selected in the preference 
	 * tree, load its children into the preference grid
	*/
	public ETList< ETPairT<IPropertyElement, String> > loadTable(IPropertyElement pEle)
	{
		// save anything that was in edit mode before loading another element
		if (jTable1.getCellEditor() != null)
		{
			jTable1.getCellEditor().stopCellEditing();
		}
		ETList< ETPairT<IPropertyElement, String> > hashTable = loadGrid(pEle);
		loadHelp(pEle);
		enableButtons();
		jDetailsPanel.removeAll();
		jTable1 = null;
		jScrollPane2 = null;
		m_TableModel = new PreferenceDialogTableModel(this, hashTable);
		jTable1 = new JPreferenceDialogTable(m_TableModel, this);
		jTable1.setRowHeight(jTable1.getRowHeight() + 1);
		jScrollPane2 = new JScrollPane(jTable1);
		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		jDetailsPanel.add(jScrollPane2, gridBagConstraints);
		
		//now refresh the table so that it paints, cannot use refreshUI it causes a flicker.
		jTable1.updateUI();
		jScrollPane2.doLayout();
		jDetailsPanel.doLayout();
		this.doLayout();
		
		return hashTable;
	}

	/**
	 * 
	 */
	private void enableButtons()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param pEle
	 */
	public void loadHelp(IPropertyElement pEle)
	{
		if (pEle != null)
		{
			IPropertyDefinition pDef = pEle.getPropertyDefinition();
			if (pDef != null)
			{
				// populate the help text
				String help = pDef.getHelpDescription();
				jTextArea1.setText(help);
				jTextArea1.setCaretPosition(0);
			}
		}
	}

	/**
	 * Based on the passed-in property element, load its child nodes into the preference grid.
	 * 
	 * @param pEle[in]	The property element whose children need to be placed in the preference grid
	 *
	 * @return HRESULT
	 */
	private ETList< ETPairT<IPropertyElement, String> > loadGrid(IPropertyElement pEle)
	{
		ETList< ETPairT<IPropertyElement, String> > hashTable = new ETArrayList< ETPairT<IPropertyElement, String> >();
		if (pEle != null)
		{
			Vector<IPropertyElement> subElems = pEle.getSubElements();
			if (subElems != null)
			{
				int count = subElems.size();
				for (int i=0; i<count; i++)
				{
					IPropertyElement subEle = subElems.elementAt(i);

					// if this element has no children, then put it in the preference grid
					// if it has children, then it is in the preference tree, so we don't want to process it here
					if (!hasChildren(subEle))
					{
						IPropertyDefinition pDef = pEle.getPropertyDefinition();
						if (pDef != null)
						{
							IPropertyDefinition subDef = subEle.getPropertyDefinition();
							if (subDef != null)
							{
								// preference definitions have a check on them whether or not they are an advanced
								// preference - will only want to load those marked as advanced if the user has 
								// checked the advanced check box on the dialog
								boolean load = true;
								String advancedStr = subDef.getFromAttrMap("advanced");
								if (advancedStr != null && 
									advancedStr.equals("PSK_TRUE") &&
									!m_Advanced)
								{
									load = false;
								}
								
								if (load)
								{
									String defName = subDef.getName();
									
									// if the definition is of a special type (Fonts/Colors)
									// we cannot just process it normally
									int pos = defName.indexOf("Font");
									if (pos >= 0)
									{
										specialFontProcessing(subDef, subEle, hashTable);
									}
									else
									{
										pos = defName.indexOf("Color");
										if (pos >= 0)
										{
											specialColorProcessing(subDef, subEle, hashTable);
										}
										else
										{
											String name = subDef.getDisplayName();
											if (name != null && name.length() > 0)
											{
												String value = subEle.getTranslatedValue();
												if (value == null)
												{
													value = "";
												}
												hashTable.add(new ETPairT<IPropertyElement, String>(subEle, value));
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return hashTable;
	}

	/**
	 * @param subDef
	 * @param subEle
	 */
	private void specialColorProcessing(IPropertyDefinition subDef, IPropertyElement subEle, ETList< ETPairT<IPropertyElement, String> > hashTable)
	{
		if (subDef != null && subEle != null)
		{
			String name = subDef.getDisplayName();
			hashTable.add(new ETPairT<IPropertyElement, String>(subEle, name));
			//long row;
			//m_Grid->get_Rows(&row);
			//m_Grid->AddItem(name, CComVariant(row));
			//ProcessColorRow(row, pEle);
		}
	}

	/**
	 * @param subDef
	 * @param subEle
	 */
	private void specialFontProcessing(IPropertyDefinition subDef, IPropertyElement subEle, ETList< ETPairT<IPropertyElement, String> > hashTable)
	{
		if (subDef != null && subEle != null)
		{
			String name = subDef.getDisplayName();
			IPropertyElement nameEle = subEle.getSubElement("FaceName", null);
			//long row;
			//m_Grid->get_Rows(&row);
			if (nameEle != null)
			{
				String fontName = nameEle.getTranslatedValue();
				hashTable.add(new ETPairT<IPropertyElement, String>(subEle, fontName));
				//m_Grid->AddItem(name, CComVariant(row));
				//m_Grid->put_Cell(flexcpText, CComVariant(row), CComVariant(1), vtMissing, vtMissing, CComVariant(fontName));
				//SetFontOnGrid(m_Grid, pEle, row);
				//m_Grid->put_Cell(flexcpData, CComVariant(row), CComVariant(0), vtMissing, vtMissing, CComVariant(pEle));
				//long color = RGB(255,255,255);
				//m_Grid->put_Cell(flexcpBackColor, CComVariant(row), CComVariant(1), vtMissing, vtMissing, CComVariant(color));
			}
	   }
	}
	
	private void performSaveAction(boolean bClose)
	{
		try 
		{
			//if the file was not readonly then only we want to save
			if (!m_isFileReadOnly)
			{
				//save the changed preferences and close the dialog
				IPreferenceManager2 prefMan = ProductRetriever.retrieveProduct().getPreferenceManager();
				prefMan.save();
				if (bClose)
				{
					closeDialog(null);
				}
			}
			else
			{
				displaySaveErrorMessage();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void displaySaveErrorMessage()
	{
		String title = loadString("IDS_PROJNAME2");
		String msg = loadString("IDS_SAVEMESSAGE");
		IConfigManager configMgr = ProductHelper.getConfigManager();
		if (configMgr != null)
		{
			String loc = configMgr.getDefaultConfigLocation();
			loc += "PreferenceProperties.etcd";
			String msg2 = StringUtilities.replaceAllSubstrings(msg, "%s", loc);
	
			IErrorDialog pDiag = new SwingErrorDialog(this);
			if (pDiag != null)
			{
				pDiag.display(msg2, MessageIconKindEnum.EDIK_ICONERROR, title);
			}
		}
	}

	private void performRestoreDefaultsAction()
	{            
		IQuestionDialog pDiag = new SwingQuestionDialogImpl(this);         
		if (pDiag != null)
		{
			// ask are they really certain they want to do this
			String title = loadString("IDS_RESTORETITLE");
		 	String msg = loadString("IDS_RESTOREWARNING");
			QuestionResponse result = pDiag.displaySimpleQuestionDialog(SimpleQuestionDialogKind.SQDK_YESNO, MessageIconKindEnum.EDIK_ICONSTOP, msg, SimpleQuestionDialogResultKind.SQDRK_RESULT_YES, null, title);
			if (result.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES)
			{
				refreshUI();
			   // yes they do
				IPreferenceManager2 prefMan = ProductRetriever.retrieveProduct().getPreferenceManager();
				if (prefMan != null)
				{
               try
               {
                  getRootPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
                  prefMan.restore();
                  //save the changed preferences
                  performSaveAction(false);
                  //Now I need to refresh the preference tree and table
                  jTree1.reload();
                  jTree1.setSelectionRow(0);
               }
               finally
               {
                  getRootPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
               }
				}
			}
		}
	}

	private void performCancelAction()
	{
		//if the file was read-only to start with, we do not need to reload
		if (!m_isFileReadOnly)
		{
			//reload the preferences and close the dialog
			IPreferenceManager2 prefMan = ProductRetriever.retrieveProduct().getPreferenceManager();
			prefMan.reloadPreferences();
		}
		
		closeDialog(null);
	}
	/**
	 * Shortcut method to retrieve the data from the grid cell which is in the form of a IPropertyElement
	 *
	 * @param[in] row     The row to retrieve the property element from
	 * @param[out] pEle   The found property element
	 *
	 * @return HRESULT
	 *
	 */
	public IPropertyElement getElementAtGridRow(int row)
	{
		IPropertyElement retEle = null;
		if (row > -1 && jTable1 != null)
		{
			Object obj = m_TableModel.getValueAt(row, 0);
			if (obj instanceof IPropertyElement)
			{
				retEle = (IPropertyElement)obj;
			}
		}
		return retEle;
	}
	public void onCellButtonClicked(int row, IPropertyElement pEle)
	{
		if (pEle != null)
		{
			IPropertyDefinition pDef = pEle.getPropertyDefinition();
			if (pDef != null)
			{
				String pDefName = pDef.getName();
				int pos = pDefName.indexOf("Resources");
				int pos2 = pDefName.indexOf("Font");
				if (pos == 0 && pDefName.length() == 9)
				{
					ApplicationColorsAndFonts pBasicColorsAndFontsDialog = new ApplicationColorsAndFonts((JDialog)this);
					pBasicColorsAndFontsDialog.center(this);
					pBasicColorsAndFontsDialog.show();
					jTable1.editCellAt(row, 1);
				}
				else if (pos2 > -1)
				{
					Font curFont = buildCurrentFont(pEle);
					Font font = FontChooser.selectFont(curFont);
					if (font != null)
					{
						//
						// if the user has just edited a font preference (through the CFontDialog), we want
						// to update the grid with the information from the CFontDialog
						//
						// the information for the font preferences is stored a little differently, so
						// we are at the top level font preference (DefaultFont), this element does not
						// have the information on it, so we get one of its child elements that does
						//
						updateFontOnPropertyElement(pEle, font);
						jTable1.editCellAt(row, 1);
					}
				}
				else
				{
				}
			}
		}
	}
	public Font buildCurrentFont(IPropertyElement pEle)
	{
		Font pFont = null;
		if (pEle != null)
		{
			IPropertyElement subEleName = pEle.getSubElement("FaceName", null);
			String name = subEleName.getValue();
			String height = pEle.getSubElement("Height", null).getValue();
			String strWeight = pEle.getSubElement("Weight", null).getValue();
			int weight = new Integer(strWeight).intValue();
			String italic = pEle.getSubElement("Italic", null).getValue();
			int style = Font.PLAIN;
			if (weight > 400){
				style |= Font.BOLD;
			}
			if (italic.equals("1")){
				style |= Font.ITALIC;
			}
			if (name != null && name.length() > 0 && height != null && height.length() > 0){
  				pFont = new Font(name, style, new Integer(height).intValue());
			}
		}
		return pFont;
	}
	public Color buildCurrentColor(IPropertyElement pEle)
	{
		Color c = null;
		if (pEle != null)
		{
			String val = pEle.getValue();
			if (val != null && val.length() > 0)
			{
				ETList<String> strs = StringUtilities.splitOnDelimiter(val, ", ");
				if (strs != null)
				{
					int count = strs.size();
					if (count == 3)
					{
						// RGB
						Integer i = new Integer(strs.get(0));
						Integer i2 = new Integer(strs.get(1));
						Integer i3 = new Integer(strs.get(2));
						c = new Color(i.intValue(), i2.intValue(), i3.intValue());
					}
				}
			}
		}
		return c;
	}
	public void  updateFontOnPropertyElement(IPropertyElement pEle, Font pFont)
	{
		if (pEle != null && pFont != null)
		{
			String name = pFont.getName();
			IPropertyElement subEleName = pEle.getSubElement("FaceName", null);
			if (subEleName != null){
				subEleName.setValue(name);
				subEleName.setModified(true);
			}
			Integer i = new Integer(pFont.getSize());
			IPropertyElement subEleName2 = pEle.getSubElement("Height", null);
			if (subEleName2 != null){
				subEleName2.setValue(i.toString());
				subEleName2.setModified(true);
			}
			boolean bBold = pFont.isBold();
			IPropertyElement subEleName3 = pEle.getSubElement("Weight", null);
			if (bBold && subEleName3 != null){
				subEleName3.setValue("700");
				subEleName3.setModified(true);
			}
			else if (subEleName3 != null){
				subEleName3.setValue("400");
				subEleName3.setModified(true);
			}
			boolean bItalic = pFont.isItalic();
			IPropertyElement subEleName4 = pEle.getSubElement("Italic", null);
			if (bItalic && subEleName4 != null){
				subEleName4.setValue("1");
				subEleName4.setModified(true);
			}
			else if (subEleName4 != null){
				subEleName4.setValue("0");
				subEleName4.setModified(true);
			}
			pEle.setModified(true);
		}
	}
	public void  updateColorOnPropertyElement(IPropertyElement pEle, Color pColor)
	{
		if (pEle != null && pColor != null)
		{
			int red = pColor.getRed();
			int green = pColor.getGreen();
			int blue = pColor.getBlue();
			Integer redI = new Integer(red);
			Integer greenI = new Integer(green);
			Integer blueI = new Integer(blue);
			String colorString = redI.toString() + ", " + greenI.toString() + ", " + blueI.toString();
			pEle.setValue(colorString);
			pEle.setModified(true);
		}
	}
	public void  refreshFontElement(IPropertyElement pEle)
	{
		if (pEle != null)
		{
			jTable1.updateUI();
			//refreshUI();
		}
	}
	private String loadString(String key)
	{
		return DefaultPreferenceDialogResource.getString(key);
	}
}


