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

/*
 * Created on Jan 29, 2004
 *
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.*;
import javax.swing.plaf.ColorUIResource;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.swing.SelectableLabel;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;

/**
 * @author Aztec
 *
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

public class MethodsSelectionDialog extends JCenterDialog
{
    private ETList < IClassifier > m_baseClassList = null;
    private ETList < IClassifier > m_derivedClassList = null;
    private ETList < ETPairT<IClassifier,IOperation >> selectedOperationList = null;
    private JavaChangeHandlerUtilities m_utilities = null;

	// Used to store a list of ops chosen so far. Check if an op has been
	 // redefined by an item in this list before displaying an op in a super class\
	 // node.
	 // Ex. 	1. 	Ops in class A = [x,y,z];
	 //		2. 	Ops in class B = [p,q]
	 //		3. 	Ops in class C = [p,q,r]
	 //		4. 	Now, is A extends B and B extends C. The resultant tree should
	 //			be :-
	 //			A
	 //			|_ B
	 //		   		|_p
	 //	       		|_q
	 //         	 _ C
	 //		   		|_r
	 ETList <IOperation> allRedefinedOperations = new ETArrayList<IOperation>();

	JTree tree = null;
	static boolean toggleAbstractButtonFlag = false;
	CheckNode treeNode = null;
	GridBagConstraints gridBagConstraints=new GridBagConstraints();
	JButton okButton=null;
	JButton cancelButton=null;
    JToggleButton selectAllBtn;

    public MethodsSelectionDialog(
    		ETList < IClassifier > allDerivedClasses,
			ETList < IClassifier > allBaseClasses,
			JavaChangeHandlerUtilities utilities)
    {
        super(ProductHelper.getProxyUserInterface().getWindowHandle(), RPMessages
						.getString("IDS_JRT_OVERRIDE_OPERATIONS_TITLE"), true);
        this.m_derivedClassList = allDerivedClasses;
        this.m_baseClassList = allBaseClasses;
        this.m_utilities = utilities;
    }



	public ETList <ETPairT<IClassifier,IOperation >> display()
	{
                String strLabel = "";
		try
		{
			treeNode = createTree(m_derivedClassList, m_baseClassList, false);
			tree = new JTree(treeNode);
			//tree.setRowHeight(tree.getRowHeight() + 3);
			// The height is not set on Linux and Unix
			FontMetrics metrics = tree.getFontMetrics(tree.getFont());
			tree.setRowHeight(metrics.getHeight() + 6);
			tree.setRootVisible(false);
                        
            strLabel = RPMessages.getString("IDS_JRT_OVERRIDE_OPERATIONS_DESCRIPTION");
            tree.getAccessibleContext().setAccessibleName(RPMessages.getString("ACCESS_NAME_TREE"));
            tree.getAccessibleContext().setAccessibleDescription(strLabel);
                        
			getContentPane().setLayout(new GridBagLayout());
            Border emptyBorder = javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2);

            JLabel topLabel = new JLabel();
            topLabel.setText(strLabel);
            topLabel.setLabelFor(tree);
                        
            ImageUtil iUtil = ImageUtil.instance() ;
        
            JPanel topPanelRight = new JPanel();
            selectAllBtn = new JToggleButton(iUtil.getIcon("selectall.gif"));
                        
            // fix a11y issue: CR#6394777. 
            // Need to set the button border for it to be hilighted when it gains the focus
            selectAllBtn.setBorder(emptyBorder);
                        
            JToggleButton unSelectAllBtn = new JToggleButton(iUtil.getIcon("clearall.gif"));
            unSelectAllBtn.setBorder(emptyBorder);
                    
            JToggleButton showAbstractMethodsBtn = new JToggleButton(iUtil.getIcon("abstractonly.gif"));
            showAbstractMethodsBtn.setBorder(emptyBorder);

            topPanelRight.setLayout(new FlowLayout());
            topPanelRight.add(selectAllBtn);
            topPanelRight.add(unSelectAllBtn);
            topPanelRight.add(showAbstractMethodsBtn);

            strLabel = RPMessages.getString("IDS_JRT_MS_SELECTALL_TOOLTIP");
            selectAllBtn.setName("SelectAll");
			selectAllBtn.setToolTipText(strLabel);
            selectAllBtn.getAccessibleContext().setAccessibleName(strLabel);
            selectAllBtn.getAccessibleContext().setAccessibleDescription(strLabel);

            strLabel = RPMessages.getString("IDS_JRT_MS_CLEARALL_TOOLTIP");
			unSelectAllBtn.setName("UnSelectAll");
			unSelectAllBtn.setToolTipText(strLabel);
            unSelectAllBtn.getAccessibleContext().setAccessibleName(strLabel);
            unSelectAllBtn.getAccessibleContext().setAccessibleDescription(strLabel);

            strLabel = RPMessages.getString("IDS_JRT_MS_ABSTRACT_TOOLTIP");
			showAbstractMethodsBtn.setToolTipText(strLabel);
            showAbstractMethodsBtn.getAccessibleContext().setAccessibleName(strLabel);
            showAbstractMethodsBtn.getAccessibleContext().setAccessibleDescription(strLabel);

			selectAllBtn.addActionListener(new SelectAllButtonActionListener(treeNode));
			unSelectAllBtn.addActionListener(new SelectAllButtonActionListener(treeNode));
			showAbstractMethodsBtn.addActionListener(new ToggleAbstractButtonActionListener());
                        
            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new java.awt.GridBagLayout());

            strLabel = RPMessages.getString("IDS_OK");
            gridBagConstraints = new java.awt.GridBagConstraints();
            okButton = new JButton(strLabel);
			okButton.addActionListener(new OkButtonActionListener(treeNode));
            okButton.getAccessibleContext().setAccessibleName(strLabel);
            okButton.getAccessibleContext().setAccessibleDescription(RPMessages.getString("ACCESS_DESC_OK_BTTN"));
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
            bottomPanel.add(okButton, gridBagConstraints);

            strLabel = RPMessages.getString("IDS_CANCEL");
            cancelButton = new JButton(strLabel);
 			cancelButton.addActionListener(new CancelButtonActionListener());
            cancelButton.getAccessibleContext().setAccessibleName(strLabel);
            cancelButton.getAccessibleContext().setAccessibleDescription(RPMessages.getString("ACCESS_DESC_CANCEL_BTTN"));
            bottomPanel.add(cancelButton, new java.awt.GridBagConstraints());

			tree.setCellRenderer(new CheckRenderer());
			//tree.setSize(200, 200);
			tree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addMouseListener(new NodeSelectionListener(tree));

			// Add accelerator
			ActionListener action = new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					int selectedRows[] = tree.getSelectionRows();
					if (selectedRows == null || selectedRows.length == 0)
						return;

					// get the path of the 1st selected row
                                        int row = selectedRows[0];
					TreePath path = tree.getPathForRow(row);
					if (path != null)
					{
						 CheckNode node = (CheckNode) path.getLastPathComponent();
						 boolean isSelected = !(node.isSelected());
						 node.setSelected(isSelected);

						 if (isSelected)
							tree.expandPath(path);
						 else
							tree.collapsePath(path);

						 ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
						 if (row == 0)
						 {
							  tree.revalidate();
							  tree.repaint();
						 }
					}
				}
			};
                        
            // use SPACE bar (instead of Enter) to select/deselect the tree node to be consistent with java 
            // default key for selecting a focused component.
            tree.registerKeyboardAction(action, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0), JComponent.WHEN_FOCUSED);
            tree.addFocusListener(new TreeFocusListerner());

			JScrollPane sp = new JScrollPane(tree);
			sp.setPreferredSize(new java.awt.Dimension(300, 300));
                        
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 0.5;
            gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
            getContentPane().add(topLabel, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 10);
            getContentPane().add(topPanelRight, gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.fill=GridBagConstraints.BOTH;
			gridBagConstraints.gridx=0;
			gridBagConstraints.gridy=1;
			gridBagConstraints.weightx=1;
			gridBagConstraints.weighty=0.7;
            gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
			gridBagConstraints.insets = new Insets(10,10,0,10);
			getContentPane().add(sp,gridBagConstraints);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy=2;
            gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints.insets = new Insets(10,10,10,10);
			getContentPane().add(bottomPanel,gridBagConstraints);

			Dimension buttonSize = getMaxButtonWidth();
			okButton.setMaximumSize(buttonSize);
			okButton.setPreferredSize(buttonSize);
			cancelButton.setPreferredSize(buttonSize);
			cancelButton.setMaximumSize(buttonSize);

			WindowListener wndCloser = new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					return;
				}
			};
			addWindowListener(wndCloser);

			setResizable(true);
			expandAll(tree, true);
			center(ProductHelper.getProxyUserInterface().getWindowHandle());
            pack();
            this.getRootPane().setDefaultButton(okButton);
			setVisible(true);
		}
		catch (Exception e)
		{
                    e.printStackTrace() ;
			Log.stackTrace(e);
		}
		return selectedOperationList;
	}

	public Dimension getMaxButtonWidth()
	{
		Dimension ret = null;
		Dimension d = okButton.getPreferredSize();
		double max  = d.width;

		d = cancelButton.getPreferredSize();
		if(d.width > max){
			 max = d.width;
			 ret = d;
		}

		return ret;

	}

    public void setSelectedOperationList(
    ETList<ETPairT<IClassifier,IOperation>> selectedOperationList)
    {
        this.selectedOperationList = selectedOperationList;
    }

    public ETList<ETPairT<IClassifier,IOperation>> setSelectedOperationList()
    {
        return selectedOperationList;
    }

    // If expand is true, expands all nodes in the tree.
    // Otherwise, collapses all nodes in the tree.
    protected void expandAll(JTree tree, boolean expand)
    {
    	TreeNode root = (TreeNode)tree.getModel().getRoot();
        // Traverse tree from root
        expandAll(tree, new TreePath(root), expand);
    }

    private void expandAll(JTree tree, TreePath parent, boolean expand)
    {
    	//Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        // Expansion or collapse must be done bottom-up
        if (expand)
        {
            tree.expandPath(parent);
        } else
        {
            tree.collapsePath(parent);
        }
    }


    //	*****************************OKActionListener******************************
    class OkButtonActionListener implements ActionListener
    {
        CheckNode root;
        ETList<ETPairT<IClassifier,IOperation>> selectedOperationList
                    = new ETArrayList<ETPairT<IClassifier,IOperation>> ();

        OkButtonActionListener(final CheckNode root)
        {
            this.root = root;
        }

        public void actionPerformed(ActionEvent e)
        {
    		CheckNode rootNode = root;
            Enumeration nodeEnum = rootNode.breadthFirstEnumeration();
            while (nodeEnum.hasMoreElements())
            {
                CheckNode node = (CheckNode) nodeEnum.nextElement();
                if (node.isSelected())
                {
                    TreeNode[] nodes = node.getPath();
                 	for (int j = 0; j < nodes.length; j++)
                	{
                		CheckNode temp = (CheckNode)nodes[j];
                		if (temp.isLeaf())
                		{
                			selectedOperationList
                            .add(new ETPairT
                            < IClassifier,IOperation > (temp.getClassifier(),
                                                        temp.getOperation()));
                		}
                	}
            	}
            }

            MethodsSelectionDialog.this.setSelectedOperationList(
                selectedOperationList);

            dispose();
        }
    }



	//	*****************************SelectAllActionListener******************************
	class SelectAllButtonActionListener implements ActionListener
	{
		CheckNode root = null;
		SelectAllButtonActionListener(CheckNode root)
		{
			this.root = root;
		}

		public void actionPerformed(ActionEvent e)
		{
			boolean selectAll = false;
            if (e.getSource()==selectAllBtn)
				selectAll = true;
			Enumeration nodeEnum = root.breadthFirstEnumeration();
			while (nodeEnum.hasMoreElements())
			{
				CheckNode node = (CheckNode)nodeEnum.nextElement();
				node.setSelected(selectAll);
				TreeNode[] nodes = node.getPath();
				for (int j = 0; j < nodes.length; j++)
				{
					((CheckNode)nodes[j]).setSelected(selectAll);
				}
			}
			MethodsSelectionDialog.this.tree.repaint();
		}
	}

	//*****************************SelectAllActionListener************Inner******************
        class ToggleAbstractButtonActionListener implements ActionListener
	{

		public void actionPerformed(ActionEvent e)
		{
			if(((JToggleButton)e.getSource()).isSelected()) 
				toggleAbstractButtonFlag = true;
			else
				toggleAbstractButtonFlag = false;
                        
			((DefaultTreeModel)MethodsSelectionDialog.this.tree.getModel())
				.nodeStructureChanged(MethodsSelectionDialog.this.treeNode);
			MethodsSelectionDialog.this.tree.treeDidChange();
			MethodsSelectionDialog.this.expandAll(MethodsSelectionDialog.this.tree, true);
		}
	}

	class CancelButtonActionListener implements ActionListener
	   {

		   public void actionPerformed(ActionEvent e)
		   {
			   MethodsSelectionDialog.this.setSelectedOperationList(null);
			   dispose();
		   }
	   }


    //**************************NodeSelectionListener***************************
    class NodeSelectionListener extends MouseAdapter
    {
        JTree tree;

        NodeSelectionListener(JTree tree)
        {
            this.tree = tree;
        }

        public void mouseClicked(MouseEvent e)
        {
            int x = e.getX();
            int y = e.getY();
            int row = tree.getRowForLocation(x, y);
            TreePath path = tree.getPathForRow(row);
            if (path != null)
            {
                CheckNode node = (CheckNode) path.getLastPathComponent();
                boolean isSelected = !(node.isSelected());
                node.setSelected(isSelected);

                if (isSelected)
                	tree.expandPath(path);
                else
                    tree.collapsePath(path);

                ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                if (row == 0)
                {
                    tree.revalidate();
                    tree.repaint();
                }
            }
        }
    }
    
    
    //************************* TreeFocusListener **************************
    class TreeFocusListerner implements FocusListener 
    {
        JTree tree;

        public void focusGained(FocusEvent e) 
        {
            Component comp = e.getComponent();
            if (comp instanceof JTree) 
            {
                tree = (JTree) comp;
                TreePath path = tree.getPathForRow(0);
                tree.setSelectionPath(path);
            }
        }
            
        public void focusLost(FocusEvent e) 
        {
            Component comp = e.getComponent();
            if ( comp instanceof JTree) 
            {
                tree = (JTree) comp;
                tree.clearSelection();
            }
        }
    }
    
    
    public CheckNode createTree(
        ETList < IClassifier > allDerivedClasses,
		ETList < IClassifier > allBaseClasses,
		boolean hideAbstract)
    {
    	int derClassCount = allDerivedClasses.size();
        CheckNode root = null;
        CheckNode node = null;
        CheckNode superNode = new CheckNode();
        CheckNode derClassNode = null;

		ETList <IOperation> derivedClassOpList = null;

		for (int i = 0; i < derClassCount; ++i)
		{
			// Create the node for each of the derived classes
			IClassifier tempDerivedClass = allDerivedClasses.get(i);
			root = new CheckNode(tempDerivedClass.getName());
			derClassNode = root;

			// Get a list of operations that each base class has on offer
			// for the derived class. This excludes the already redefined
			// operations.

			derivedClassOpList = tempDerivedClass.getOperations();

			int baseClassCount = m_baseClassList.size();

			for (int j = 0; j < baseClassCount; ++j)
			{
				IClassifier tempBaseClass = m_baseClassList.get(j);

				root = derClassNode;

				// Get the list of ops on offer from the base class
				ETList<IOperation> opsToRedefine =
					getOperationsToRedefine(tempDerivedClass,
											tempBaseClass);

				// If the size of redefinable ops is 1 or more,
				// create nodes for the base class and the operations
				if(opsToRedefine != null && opsToRedefine.size() > 0)
				{
					// Base class node
					node = new CheckNode(tempBaseClass.getName());
					root.add(node);
					root = node;

					// Add the operations
					int opCount = opsToRedefine.size();
					for(int k = 0 ; k < opCount ; ++k)
					{

						IOperation op = opsToRedefine.get(k);
						if(hideAbstract && op.getIsAbstract())
							continue;
						node = new CheckNode(m_utilities.formatOperation(op));
						node.setOperation(op);
                        node.setClassifier(tempDerivedClass);
						root.add(node);
					}
				}
			}
            if(derClassNode.getChildCount() > 0)
                superNode.add(derClassNode);
		}

		return superNode;
    }


	protected ETList<IOperation> getOperationsToRedefine(IClassifier derivedClass,
															IClassifier baseClass)
	{

		// Get only the redifinable operations
		ETList<IOperation> baseClassRedefinableOps
				= m_utilities.collectVirtualOperations2(baseClass,
														null,
														null,
														null);

		ETList<IOperation> redefinableOps = new ETArrayList<IOperation>();

		int baseOpCount = baseClassRedefinableOps.size();
		IOperation baseOp = null;

		for(int i = 0 ; i < baseOpCount ; ++i)
		{
			baseOp = baseClassRedefinableOps.get(i);

			if(derivedClass.findMatchingOperation(baseOp, false) == null
					&& !redefinableOps.contains(baseOp)
					&& m_utilities.discoverRedefinition(baseOp, allRedefinedOperations) == null)
			{
				redefinableOps.add(baseOp);
				allRedefinedOperations.add(baseOp);
			}
		}

		return redefinableOps;
	}
}

//****************************CheckRenderer************************************
class CheckRenderer extends JPanel implements TreeCellRenderer
{
    protected JCheckBox check;
    protected SelectableLabel strLabel;

    public CheckRenderer()
    {
        setLayout(null);
        add(check = new JCheckBox());
        add(strLabel = new SelectableLabel());
        check.setLocation(50, 0);
        check.setBackground(UIManager.getColor("Tree.textBackground"));
        setOpaque(false);
    }

    public Component getTreeCellRendererComponent(
        JTree tree,
        Object value,
        boolean isSelected,
        boolean expanded,
        boolean leaf,
        int row,
        boolean hasFocus)
    {
        CheckNode cNode = null;
        if(value instanceof CheckNode)
        {
            cNode = (CheckNode)value;
        }

		String stringValue =
            tree.convertValueToText(
                value,
                isSelected,
                expanded,
                leaf,
                row,
                hasFocus);
        setEnabled(tree.isEnabled());
        if(cNode != null)
        {
        	check.setSelected(cNode.isSelected());
        }
        else
        	return this;
        strLabel.setFont(tree.getFont());
        strLabel.setText(stringValue);
        strLabel.setSelected(isSelected);
        strLabel.setFocus(hasFocus);
        
        if (isSelected)
        {
            strLabel.setSelectedBackground(UIManager.getColor("Tree.selectionBackground"));
            strLabel.setForeground(UIManager.getColor("Tree.selectionForeground"));
        }
        else
        {
            strLabel.setForeground(UIManager.getColor("Tree.textForeground"));
        }

		// cvc - CR 6275883
		// use new icons Class and operation (method) icons
        // URL classURL = getClass().getResource("resources/class.gif");
        // URL methodURL = getClass().getResource("resources/method.gif");
//		URL classURL = getClass().getResource(
//			"resources/images/class.png"); // NOI18N
//        URL methodURL = getClass().getResource(
//			"resources/images/operation.gif"); // NOI18N

        ImageUtil iUtil = ImageUtil.instance() ;
        
        
        Icon classIcon = iUtil.getIcon("class.png") ; //new ImageIcon(classURL);
        Icon methodIcon = iUtil.getIcon("operation.gif") ;//new ImageIcon(methodURL);


        if (leaf)
        {
            if(cNode.getOperation() !=  null)
            	strLabel.setIcon(methodIcon);
            else
            	strLabel.setIcon(classIcon);
        }
        else if (expanded)
        {
            strLabel.setIcon(classIcon);
        }
        else
        {
            strLabel.setIcon(classIcon);
        }
        return this;
    }

    public Dimension getPreferredSize()
    {
        Dimension d_check = check.getPreferredSize();
        Dimension d_label = strLabel.getPreferredSize();
        return new Dimension(
            d_check.width + d_label.width,
            (d_check.height < d_label.height
                ? d_label.height
                : d_check.height));
    }

    public void doLayout()
    {
        Dimension d_check = check.getPreferredSize();
        Dimension d_label = strLabel.getPreferredSize();
        int y_check = 0;
        int y_label = 0;
        if (d_check.height < d_label.height)
        {
            y_check = (d_label.height - d_check.height) / 2;
        }
        else
        {
            y_label = (d_check.height - d_label.height) / 2;
        }
        check.setLocation(0, y_check);
        check.setBounds(0, y_check, d_check.width, d_check.height);
        strLabel.setLocation(d_check.width, y_label);
        strLabel.setBounds(d_check.width, y_label, d_label.width, d_label.height);
    }

    public void setBackground(Color color)
    {
        if (color instanceof ColorUIResource)
            color = null;
        super.setBackground(color);
    }

//    //******************************class TreeLabel*******************
//    class TreeLabel extends JLabel
//    {
//        boolean isSelected;
//        boolean hasFocus;
//
//        TreeLabel()
//        {
//        }
//
//        public void setBackground(Color color)
//        {
//            if (color instanceof ColorUIResource)
//                color = null;
//            super.setBackground(color);
//        }
//        
//        public void paint(Graphics g) {
//            int imageOffset = 0;
//            String str;
//            Color foregroundColor;
//            
//            if ((str = getText()) != null) {
//                if (0 < str.length()) {
//                    
//                    Dimension d = getPreferredSize();
//                    
//                    Icon currentI = getIcon();
//                    if (currentI != null) {
//                        imageOffset =
//                                currentI.getIconWidth()
//                                + Math.max(0, getIconTextGap() - 1);
//                    }
//                    
//                    if (isSelected) {
//                        
//                        foregroundColor = UIManager.getColor("Tree.selectionForeground") ;
//                        g.setColor(UIManager.getColor("Tree.selectionBackground"));
//                        g.fillRect(
//                                imageOffset,
//                                0,
//                                d.width - 1 - imageOffset,
//                                d.height);
//                    } else {
//                        foregroundColor = UIManager.getColor("Tree.textForeground") ;
//                    }
//                    
//                    if (hasFocus) {
//                        g.setColor(
//                                UIManager.getColor("Tree.selectionBorderColor"));
//                        g.drawRect(
//                                imageOffset,
//                                0,
//                                d.width - 1 - imageOffset,
//                                d.height - 1);
//                    }
//                    
//                    g.setColor(foregroundColor) ;
//                    g.drawString(this.getText(), imageOffset + 1, this.getHeight() - 3) ;
//                }
//                
//                
//            }
//            
//            
//        }
//
//        public Dimension getPreferredSize()
//        {
//            Dimension retDimension = super.getPreferredSize();
//            if (retDimension != null)
//            {
//                retDimension =
//                    new Dimension(retDimension.width + 3, retDimension.height);
//            }
//            return retDimension;
//        }
//
//        void setSelected(boolean isSelected)
//        {
//            this.isSelected = isSelected;
//        }
//
//        void setFocus(boolean hasFocus)
//        {
//            this.hasFocus = hasFocus;
//        }
//    }
}

//*************************** class CheckNode***********************************
class CheckNode extends DefaultMutableTreeNode
{

    protected boolean isSelected;
    protected IOperation operation;
    protected IClassifier clazz;

    public CheckNode()
    {
        this(null);
    }

    public CheckNode(Object userObject)
    {
        this(userObject, true, false);
    }

    public CheckNode(
        Object userObject,
        boolean allowsChildren,
        boolean isSelected)
    {
        super(userObject, allowsChildren);
        this.isSelected = isSelected;
    }

    public void setSelected(boolean isSelected)
    {
        this.isSelected = isSelected;

        if (children != null)
        {
            Enumeration nodeEnum = children.elements();
            while (nodeEnum.hasMoreElements())
            {
                CheckNode node = (CheckNode) nodeEnum.nextElement();
                node.setSelected(isSelected);
            }
        }
    }

    public boolean isSelected()
    {
        return isSelected;
    }

    /**
     * @return
     */
    public IOperation getOperation()
    {
        return operation;
    }

    /**
     * @param operationListId
     */
    public void setOperation(IOperation operation)
    {
        this.operation = operation;
    }

    /**
      * @return
      */
     public IClassifier getClassifier()
     {
         return clazz;
     }

     /**
      * @param operationListId
      */
     public void setClassifier(IClassifier clazz)
     {
         this.clazz = clazz;
     }

    public int getChildCount()
    {
        if (children == null)
        {
            return 0;
        }
        else
        {
           if(!MethodsSelectionDialog.toggleAbstractButtonFlag)
          		return children.size();
           else
           		return getAbstractNodes(children).size();
        }
    }

    public Enumeration children()
    {
        if (children == null)
        {
            return EMPTY_ENUMERATION;
        }
        else
        {
			if(!MethodsSelectionDialog.toggleAbstractButtonFlag)
				return children.elements();

			else
				return getAbstractNodes(children).elements();
        }
    }

    /**
     * @param children
     * @return
     */
    private Vector getAbstractNodes(Vector children)
    {
        Vector abstractLess = new Vector();

        for(int i=0 ; i < children.size() ; ++i)
        {
           CheckNode node = (CheckNode)children.get(i);
           if(node.getOperation() == null ||
            (node.getOperation() != null &&
                node.getOperation().getIsAbstract()))
            {
                abstractLess.add(node);
            }
        }
        return abstractLess;
    }

}



