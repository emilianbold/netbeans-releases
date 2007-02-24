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


package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADNamedElementListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.DiagramEngineResources;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.swing.SelectableLabel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

public class CustomizeDlg {
    private static final String DLG_TITLE = DiagramEngineResources.getString("IDS_CUSTOMIZE");
    private CustomizeDlgInnerPane innerPane = null ;
    private IDrawEngine m_pEngine = null;
    
    public CustomizeDlg() {
        this(null, DLG_TITLE, false, null);
    }
    
    public CustomizeDlg(IDrawEngine engine) {
        
        this(null, DLG_TITLE, true, engine);
        
    }
    
    public CustomizeDlg(Frame frame, String title, boolean modal, IDrawEngine engine) {
        this.m_pEngine = engine;
        try {
            innerPane = new CustomizeDlgInnerPane(m_pEngine) ;
            
            DialogDescriptor dd = new DialogDescriptor(
                    innerPane, DLG_TITLE, true/*isModel*/, innerPane);
            
            DialogDisplayer dialog = DialogDisplayer.getDefault() ;
            
            dialog.createDialog(dd);
            dialog.notify(dd) ;
            
        } catch (Exception ex) {
            //oops that's a bummer. Should never get here.
        }
    }
    
}


class CustomizeDlgInnerPane extends JPanel implements ActionListener {
    
    private IDrawEngine m_pEngine = null;
    
    private JTree m_Tree = new JTree();
    private DefaultTreeModel m_Model = null;
    private DefaultMutableTreeNode m_RootNode = new DefaultMutableTreeNode();
    
    private List m_NodeList = new ArrayList();
    
    private JPanel pnlNorth = new JPanel();
    private JLabel txtCaption = new JLabel();
    
    
    public CustomizeDlgInnerPane(IDrawEngine engine) {
        this.m_pEngine = engine ;
        
        try {
            initTree() ;
            createUI();
            
        }catch (Exception e) {
            e.printStackTrace() ;
        }
    }
    
    private void createUI() throws Exception {
        GridBagConstraints gridBagConstraints = null;
        
        pnlNorth.setLayout(new GridBagLayout());
        
        String txt = DiagramEngineResources.getString("IDS_CUSTOMIZETEXT");
        txtCaption.setText(txt);
        txtCaption.setToolTipText("");
        txtCaption.setLabelFor(m_Tree);
        
        gridBagConstraints = new GridBagConstraints();
        
        FontMetrics metrics = m_Tree.getFontMetrics(m_Tree.getFont());
        m_Tree.setRowHeight(metrics.getHeight() + 6);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        gridBagConstraints.weightx=1;
        gridBagConstraints.fill=GridBagConstraints.BOTH;
        pnlNorth.add(txtCaption, gridBagConstraints);
        
        JScrollPane scrollPane = new JScrollPane(m_Tree);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        gridBagConstraints.weightx=1;
        gridBagConstraints.weighty=0.1;
        gridBagConstraints.fill=GridBagConstraints.BOTH;
        gridBagConstraints.insets=new Insets(10,10,10,10);
        add(pnlNorth, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=1;
        gridBagConstraints.weightx=1;
        gridBagConstraints.weighty=0.7;
        gridBagConstraints.fill=GridBagConstraints.BOTH;
        gridBagConstraints.insets=new Insets(0,10,10,10);
        add(scrollPane, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=2;
        gridBagConstraints.weightx=1;
        gridBagConstraints.weighty=0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets=new Insets(0,10,10,10);
        
    }
    
    private void initTree() {
        
        boolean proceed = false;
        
        m_Tree.setCellRenderer(new CheckRenderer());
        
        
        m_Tree.setEditable(true);
        m_Tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        m_Tree.setShowsRootHandles(true);
        
        m_Tree.addMouseListener(new NodeSelectionListener(m_Tree));
        
        // Add accelerator
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRows[] = m_Tree.getSelectionRows();
                if (selectedRows == null || selectedRows.length == 0)
                    return;
                
                // get the path of the 1st selected row
                int row = selectedRows[0];
                TreePath path = m_Tree.getPathForRow(row);
                if (path != null) {
                    CompartmentNode node = (CompartmentNode) path.getLastPathComponent();
                    boolean isSelected = !(node.isSelected());
                    node.setSelected(isSelected);
                    
                    if (isSelected)
                        m_Tree.expandPath(path);
                    else
                        m_Tree.collapsePath(path);
                    
                    ((DefaultTreeModel) m_Tree.getModel()).nodeChanged(node);
                    if (row == 0) {
                        m_Tree.revalidate();
                        m_Tree.repaint();
                    }
                }
            }
        };
        
        // use SPACE bar to select/deselect the m_Tree node
        m_Tree.registerKeyboardAction(action, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0), JComponent.WHEN_FOCUSED);
        m_Tree.addFocusListener(new TreeFocusListerner());
        
        
        if (m_pEngine != null) {
            DefaultMutableTreeNode groupNode = null;
            IteratorT < ICompartment > iter = new IteratorT < ICompartment > (m_pEngine.getCompartments());
            
            String compartmentName = "";
            while (iter.hasNext()) {
                ICompartment currObject = iter.next();
                
                if (currObject instanceof INameListCompartment) {
                    IListCompartment nameListCompartment = (IListCompartment)currObject;
                    Iterator < ICompartment > compartmentIterator = nameListCompartment.getCompartments().iterator();
                    
                    String nodeName = "";
                    
                    while (compartmentIterator.hasNext()) {
                        ICompartment foundCompartment = compartmentIterator.next();
                        
                        if (foundCompartment instanceof IADNameCompartment) {
                            IElement modelElem = foundCompartment.getModelElement();
                            if ( modelElem != null) {
                                nodeName = modelElem.toString();
                            } else {
                                compartmentName = foundCompartment.getName();
                                nodeName = (compartmentName != null && compartmentName.length() > 0) ? compartmentName : "???";
                            }
                            m_RootNode = new DefaultMutableTreeNode(new String(nodeName));
                            m_Model = new DefaultTreeModel(m_RootNode);
                            m_Tree.setModel(m_Model);
                            proceed = true;
                            break;
                        }
                    }
                    
                } else if (currObject instanceof IADNamedElementListCompartment && proceed) {
                    IListCompartment listCompartment = (IListCompartment)currObject;
                    
                    boolean enableChild = !listCompartment.getCollapsed();
                    
                    groupNode = new CompartmentNode(listCompartment, false, true, false);
                    
                    m_Model.insertNodeInto(groupNode, m_RootNode, m_RootNode.getChildCount());
                    
                    IteratorT < ICompartment > compartmentIterator = new IteratorT < ICompartment > (listCompartment.getCompartments());
                    
                    while (compartmentIterator.hasNext()) {
                        ICompartment foundCompartment = compartmentIterator.next();
                        CompartmentNode newNode = new CompartmentNode(foundCompartment, false, enableChild, false) ;
                        newNode.setSelected(!foundCompartment.getCollapsed()) ;
                        m_Model.insertNodeInto(newNode, groupNode, groupNode.getChildCount());
                    }
                }
            }
            
            m_Tree.expandPath(new TreePath(m_RootNode));
        }
    }
    
    
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand() ;
        if (!command.equalsIgnoreCase("cancel"))  // NOI18N
            this.updateDrawEngine(m_RootNode);
    }
    
    
    private void getAllNodes(TreeNode node) {
        if (node instanceof CompartmentNode) {
            this.m_NodeList.add(node);
        }
        
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode)e.nextElement();
                getAllNodes(n);
            }
        }
    }
    
    private void updateDrawEngine(TreeNode hItem) {
        
        if (hItem == null) {
            hItem = this.m_RootNode;
        }
        
        m_NodeList = new ArrayList();
        
        getAllNodes(hItem);
        
        Iterator < CompartmentNode > iter = m_NodeList.iterator();
        
        while (iter.hasNext()) {
            ICompartment pCompartment = null;
            
            CompartmentNode pData = (CompartmentNode)iter.next();
            
            if (pData != null) {
                pCompartment = pData.m_pCompartment;
            }
            
            if (pCompartment != null) {
                // never update the name of the class node
                if (pData.m_bIsRoot == false) {
                    INodeDrawEngine pNodeDrawEngine = (m_pEngine instanceof INodeDrawEngine) ? (INodeDrawEngine)m_pEngine : null;
                    
                    if (pNodeDrawEngine != null) {
                        //set Compartment status
                        pCompartment.setCollapsed(!pData.isSelected()) ;
                        // perform collapse
                        pNodeDrawEngine.collapseCompartment(pCompartment, !pData.isSelected());
                    }
                }
            }
        }
        
        m_pEngine.invalidate();
        // Update the invalid regions.
        m_pEngine.getDiagram().refresh(true);
    }
    
    //**************************NodeSelectionListener***************************
    class NodeSelectionListener extends MouseAdapter {
        JTree tree;
        
        NodeSelectionListener(JTree tree) {
            this.tree = tree;
        }
        
        public void mouseClicked(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int row = tree.getRowForLocation(x, y);
            TreePath path = tree.getPathForRow(row);
            
            if (path != null) {
                Object obj = path.getLastPathComponent();
                
                if (obj instanceof CompartmentNode) {
                    CompartmentNode node = (CompartmentNode) obj ;
                    boolean isSelected = !(node.isSelected());
                    node.setSelected(isSelected);
                    
                    if (isSelected) {
                        tree.expandPath(path);
                    }else{
                        tree.collapsePath(path);
                        
                    }
                    
                    ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                    if (row == 0) {
                        tree.revalidate();
                        tree.repaint();
                    }
                }
            }
        }
    }
    
    
    //************************* TreeFocusListener **************************
    class TreeFocusListerner implements FocusListener {
        JTree tree;
        
        public void focusGained(FocusEvent e) {
            Component comp = e.getComponent();
            if (comp instanceof JTree) {
                tree = (JTree) comp;
                TreePath path = tree.getPathForRow(0);
                tree.setSelectionPath(path);
            }
        }
        
        public void focusLost(FocusEvent e) {
            Component comp = e.getComponent();
            if ( comp instanceof JTree) {
                tree = (JTree) comp;
                tree.clearSelection();
            }
        }
    }
    
    
    
}

class CompartmentNode extends DefaultMutableTreeNode {
    ICompartment m_pCompartment;
    boolean m_bIsEditable = true;
    boolean m_bIsRoot = false;
    boolean m_bIsCollapsed = false;
    boolean m_bIsEnabled = true;
    boolean isSelected = true ;
    
    public CompartmentNode(ICompartment pCompartment, boolean bEditable, boolean bEnabled, boolean bIsRoot) {
        m_pCompartment = pCompartment;
        m_bIsEditable = bEditable;
        m_bIsRoot = bIsRoot;
        m_bIsEnabled = bEnabled;
        
        m_bIsCollapsed = (pCompartment != null && pCompartment.getCollapsed());
        
    }
    
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        
        if (children != null) {
            Enumeration nodeEnum = children.elements();
            while (nodeEnum.hasMoreElements()) {
                CompartmentNode node = (CompartmentNode) nodeEnum.nextElement();
                node.setSelected(isSelected);
            }
        } else {
            if (isSelected && (getParent() instanceof CompartmentNode)) {
                CompartmentNode parent = (CompartmentNode)this.getParent() ;
                if (parent != null)
                    parent.isSelected = true ;
            }
        }
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public Enumeration children() {
        if (children == null) {
            return EMPTY_ENUMERATION;
        } else {
            
            return children.elements();
            
            
        }
    }
    
    public String getName() {
        return m_pCompartment.getName();
        
    }
    
}

//****************************CheckRenderer************************************
class CheckRenderer extends JPanel implements TreeCellRenderer {
    protected JCheckBox check;
    protected SelectableLabel strLabel;
    
    public CheckRenderer() {
        setLayout(null);
        add(check = new JCheckBox());
        add(strLabel = new SelectableLabel());
        check.setLocation(50, 0);
        check.setBackground(UIManager.getColor("Tree.textBackground"));
        
        this.setOpaque(false) ;
        
    }
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded, boolean leaf,
            int row, boolean hasFocus) {
        
        CompartmentNode cNode = null;
        
        if(value instanceof CompartmentNode) {
            cNode = (CompartmentNode)value;
        } else {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            JLabel label = new JLabel("  " + node.getUserObject().toString());
            return label;
        }
        
        String stringValue = cNode.getName() ;
        
        setEnabled(tree.isEnabled());
        
        if(cNode != null) {
            check.setSelected(cNode.isSelected());
        } else
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

        
        return this;
    }
    
    public Dimension getPreferredSize() {
        Dimension d_check = check.getPreferredSize();
        Dimension d_label = strLabel.getPreferredSize();
        return new Dimension(
                d_check.width + d_label.width,
                (d_check.height < d_label.height
                ? d_label.height
                : d_check.height));
    }
    
    public void doLayout() {
        Dimension d_check = check.getPreferredSize();
        Dimension d_label = strLabel.getPreferredSize();
        int y_check = 0;
        int y_label = 0;
        if (d_check.height < d_label.height) {
            y_check = (d_label.height - d_check.height) / 2;
        } else {
            y_label = (d_check.height - d_label.height) / 2;
        }
        check.setLocation(0, y_check);
        check.setBounds(0, y_check, d_check.width, d_check.height);
        strLabel.setLocation(d_check.width, y_label);
        strLabel.setBounds(d_check.width, y_label, d_label.width, d_label.height);
    }
    
    public void setBackground(Color color) {
        if (color instanceof ColorUIResource)
            color = null;
        super.setBackground(color);
    }
    
    
    
////******************************class TreeLabel*******************
//    class TreeLabel extends JLabel {
//        boolean isSelected;
//        boolean hasFocus;
//        
//        TreeLabel() {
//        }
//        
//        public void setBackground(Color color) {
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
//        public Dimension getPreferredSize() {
//            Dimension retDimension = super.getPreferredSize();
//            if (retDimension != null) {
//                retDimension =
//                        new Dimension(retDimension.width + 3, retDimension.height);
//            }
//            return retDimension;
//        }
//        
//        void setSelected(boolean isSelected) {
//            this.isSelected = isSelected;
//        }
//        
//        void setFocus(boolean hasFocus) {
//            this.hasFocus = hasFocus;
//        }
//    }
}
