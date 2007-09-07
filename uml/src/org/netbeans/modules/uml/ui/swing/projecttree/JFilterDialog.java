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



package org.netbeans.modules.uml.ui.swing.projecttree;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.tree.TreePath;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.ui.controls.filter.FilterItem;
import org.netbeans.modules.uml.ui.controls.filter.IFilterDialog;
import org.netbeans.modules.uml.ui.controls.filter.IFilterItem;
import org.netbeans.modules.uml.ui.controls.filter.IFilterNode;
import org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventDispatcher;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.swing.SelectableLabel;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;



/**
 * The JFilterDialog allows users to specified the items should be filtered
 * and the items that should not be filtered.  The dialog will present the user
 * with a tree control that has a checkbox by each of it's nodes.  When the
 * checkbox is checked then the item should not be filtered.  When the item is
 * not check the item should be filtered out.
 * <p>
 * The OnProjectTreeFilterDialogInit is sent to all registered
 * IProjectTreeFilterDialogEventsSink objects.  The
 * IProjectTreeFilterDialogEventsSink objects are responsible for filling
 * in the filter items by using the the methods createRootNode, and
 * addFilterItem.
 * <p>
 * JFilterDialog is the swing implementation of the IProjectTreeFilterDialog
 * interface.
 *
 * @author Trey Spiva
 * @see IProjectTreeFilterDialog
 * @see #createRootNode(org.netbeans.modules.uml.ui.controls.projecttree.IFilterItem)
 * @see #createRootNode(java.lang.String)
 * @see #addFilterItem
 */
public class JFilterDialog extends JCenterDialog implements IFilterDialog {
    private JTree                  m_FilterTree = new JTree();
    private JButton                m_OKBtn      = null;
    private JButton                m_CancelBtn  = null;
    private DefaultTreeModel       m_Model      = null;
    private DefaultMutableTreeNode m_Root       = new DefaultMutableTreeNode();
    private DispatchHelper         m_Helper     = new DispatchHelper();
    private IProjectTreeModel      m_ProjectTreeModel = null;
    private JLabel spacer = new JLabel();
    
    /**
     * Initialize the filter dialog.
     * @param model The project tree model that will be filtered.
     */
    public JFilterDialog(IProjectTreeModel model) {
        super();
        initialize();
        pack();
        setProjectTreeModel(model);
    }
    
    /**
     * Initialize the filter dialog.
     * @param parent The owner of the dialog.
     * @param model The project tree model that will be filtered.
     */
    public JFilterDialog(Frame parent, IProjectTreeModel model) {
        super(parent, true);
        initialize();
        pack();  // pack() should be called before center(parent)
        center(parent);
        setProjectTreeModel(model);
    }
    
    /**
     * Initialize the filter dialog.
     * @param parent The owner of the dialog.
     * @param model The project tree model that will be filtered.
     */
    public JFilterDialog(Dialog parent, IProjectTreeModel model) {
        super(parent, true);
        initialize();
        pack();  // pack() should be called before center(parent)
        center(parent);
        setProjectTreeModel(model);
    }
    
    /**
     * Initalizes the dialog.  The controls are created and the renders and
     * editor are setup.
     *
     */
    protected void initialize() {
        setTitle(ProjectTreeResources.getString("JFilterDialog.Filter_Dialog_Title")); //$NON-NLS-1$
        initializeControls();
        
        //setSize(400, 500);
        //setResizable(false);
        setPreferredSize(new java.awt.Dimension(400, 500));
        setResizable(true);
        CheckBoxTreeRenderEditor renderEditor = new CheckBoxTreeRenderEditor();
        m_FilterTree.setCellRenderer(renderEditor);
        m_FilterTree.setShowsRootHandles(true);
        
        m_FilterTree.addMouseListener(new NodeSelectionListener(m_FilterTree));
        
        // Add accelerator
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRows[] = m_FilterTree.getSelectionRows();
                if (selectedRows == null || selectedRows.length == 0)
                    return;
                
                // get the path of the 1st selected row
                int row = selectedRows[0];
                TreePath path = m_FilterTree.getPathForRow(row);
                if (path != null) {
                    FilterNode node = (FilterNode) path.getLastPathComponent();
                    boolean isSelected = !(node.isOn());
                    
                    int state = IFilterItem.FILTER_STATE_OFF;
                    if(isSelected == true) {
                        state = IFilterItem.FILTER_STATE_ON;
                    }
                    setItemState(node, state);
                    
                    ((DefaultTreeModel) m_FilterTree.getModel()).nodeChanged(node);
                    if (row == 0) {
                        m_FilterTree.revalidate();
                        m_FilterTree.repaint();
                    }
                }
            }
        };
        
        // use SPACE bar to select/deselect the m_Tree node
        m_FilterTree.registerKeyboardAction(action, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0), JComponent.WHEN_FOCUSED);
        m_FilterTree.addFocusListener(new TreeFocusListerner());
        m_FilterTree.setEditable(true);
        
        m_FilterTree.setRootVisible(false);
        m_FilterTree.setShowsRootHandles(true);
        
//      m_Model = new DefaultTreeModel(m_Root);
    }
    
    /**
     * Initilalize the control for the filter dialog.
     */
    protected void initializeControls() {
        m_OKBtn = new JButton(new OKAction(this));
        getRootPane().setDefaultButton(m_OKBtn);
        
        m_CancelBtn = new JButton(new CancelAction());
        
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 10, 5, 10));
        
        panel.setLayout(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(m_FilterTree);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, new GridBagConstraints(0, 0, 3, 1, 1.0, 0.9, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0));
        panel.add(spacer, new GridBagConstraints(0, 1, 1 , 1, 0.3, 0.1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 3, 0, 0), 0, 0));
        panel.add(m_OKBtn, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 3), 0, 0));
        panel.add(m_CancelBtn, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 3, 0, 3), 0, 0));
        
        // reset the buttons' size to the larger size. This applies to all locales
        Dimension okSize = m_OKBtn.getPreferredSize();
        Dimension  cancelSize = m_CancelBtn.getPreferredSize();
        if (cancelSize.width > okSize.width) {
            m_OKBtn.setPreferredSize(cancelSize);
        } else {
            m_CancelBtn.setPreferredSize(okSize);
        }
        
        getContentPane().add(panel);
        
    }
    
    /**
     * Creates a top level filter tree node that is not associated to a filtered
     * item.
     *
     * @param name The name of the user to be displayed to the user.    *
     * @return The top level node.
     */
    public IFilterNode createRootNode(String name) {
        FilterItem item = new FilterItem(name);
        return createRootNode(item);
    }
    
    /**
     * Creates a top level filter tree node that represent the filtered item.
     *
     * @param item The filter item to add to the FilterNode.
     * @return The FilterNode that represents the filter item.
     */
    public IFilterNode createRootNode(IFilterItem item) {
        FilterNode retVal = null;
        // We were getting into a situation where this was being called from both the project tree
        // and the design center tree, so we had 4 root nodes
        // There are two root nodes (Model Elements and Diagrams), so before we add anymore root nodes,
        // check the count and don't add if there are already too
        // We will ASSUME that it is the correct two nodes
        if (m_Root.getChildCount() < 2) {
            retVal = new FilterNode(item);
            m_Root.add(retVal);
        }
        return retVal;
    }
    
    /**
     * Adds a new filter item to the specified FilterNode.  The FilterNode that
     * is created to represent the IFilterItem is returned.
     *
     * @param parent The parent of the filter item.  If the parent is
     *               <code>null</code> then the filter item is not added.
     * @param item The filter item to add to the FilterNode.
     * @return The FilterNode that represents the filter item.
     */
    public IFilterNode addFilterItem(IFilterNode parent, IFilterItem item) {
        IFilterNode retVal = null;
        
        if(parent != null) {
            retVal = new FilterNode(item);
            parent.add(retVal);
        }
        return retVal;
    }
    
    /**
     * Shows the dialog to the user.  The OnProjectTreeFilterDialogInit is sent
     * to all registered IProjectTreeFilterDialogEventsSink objects.  The
     * IProjectTreeFilterDialogEventsSink objects are responsible for filling
     * in the filter items by using the the methods createRootNode, and
     * addFilterItem.
     *
     * @see #createRootNode(org.netbeans.modules.uml.ui.controls.projecttree.IFilterItem)
     * @see #createRootNode(java.lang.String)
     * @see #addFilterItem
     */
    public void show() {
        // cvc - CR 6271328
        // this method is called the first time a filter is applied to
        // the Model root node. 2+ times, the show(TreeModel) will be called
        
        TreeModel model = getModel();
        initializeModel(model);
        m_FilterTree.setModel(model);
        
        super.show();
        
        // cvc - CR 6271328
        // clearing the model defeates the purpose of caching it to reuse
        // the settings when the filter dialog is used again	   // clearModel();
    }
    
    // cvc - CR 6271328
    // the filter tree model is being passed in from the action which gets it
    // from the UMLModelRootNode where it is cached to preserve the
    // filter settings
    public void show(DefaultTreeModel model) {
        initializeModel(model);
        m_FilterTree.setModel(model);
        setModel(model);
        
        super.show();
    }
    
    /**
     * Clears the filter dialog.
     */
    protected void clearModel() {
        m_Root.removeAllChildren();
        m_Model = null;
    }
    
    /**
     * Initalizes the model.  The onProjectTreeFilterDialogInit event is sent
     * to all registered IProjectTreeFilterDialogEventsSink.
     *
     * @param model
     * @see IProjectTreeFilterDialogEventsSink#onProjectTreeFilterDialogInit
     */
    protected void initializeModel(TreeModel model) {
        
        IProjectTreeFilterDialogEventDispatcher dispatcher = null;
        dispatcher = m_Helper.getProjectTreeFilterDialogDispatcher();
        
        IEventPayload payLoad = dispatcher.createPayload("ProjectTreeFilterDialogInit"); //$NON-NLS-1$
        dispatcher.fireProjectTreeFilterDialogInit(this, payLoad);
        
    }
    
    /**
     * Retrieves the tree model that contains the filter item data.
     *
     * @return The tree model.
     */
    protected TreeModel getModel() {
        if(m_Model == null) {
            m_Root.removeAllChildren();
            m_Model = new DefaultTreeModel(m_Root);
        }
        
        return m_Model;
    }
    
    protected void setModel(DefaultTreeModel val) {
        m_Model = val;
        m_Root = (DefaultMutableTreeNode)val.getRoot();
        m_Model.setRoot(m_Root);
    }
    
    // cvc - CR 6271328
    // need public access to the tree model to cache in UMLModelRootNode
    public DefaultTreeModel getTreeModel() {
        return (DefaultTreeModel)getModel();
    }
    
    /**
     * Retrieves the root nodes of the tree.
     *
     * @return The root nodes.
     *
     */
    public IFilterNode[] getRootNodes() {
        IFilterNode[] retVal = new FilterNode[m_Root.getChildCount()];
        
        for (int index = 0; index < retVal.length; index++) {
            retVal[index] = (IFilterNode)m_Root.getChildAt(index);
        }
        
        return retVal;
    }
    
    /**
     * Sets the items state.  All of the items children will be set to the
     * same state.
     *
     * @param item The item to process.
     * @param state The state value.
     */
    protected void setItemState(IFilterNode item, int state) {
        item.setState(state);
        
        int childrenCount = item.getChildCount();
        for(int index = 0; index < childrenCount; index++) {
            FilterNode curItem = (FilterNode)m_Model.getChild(item, index);
            
            if(curItem != null) {
                curItem.setState( state );
            }
        }
    }
    
    class CheckBoxTreeRenderEditor extends JPanel
            implements TreeCellRenderer {
        protected JCheckBox check;
        protected SelectableLabel strLabel;
        
        public CheckBoxTreeRenderEditor() {
            setLayout(null);
            add(check = new JCheckBox());
            add(strLabel = new SelectableLabel());
            check.setLocation(50, 0);
            check.setBackground(UIManager.getColor("Tree.textBackground"));
            setOpaque(false);
            
            check.requestFocus();
            
        }
        
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean isSelected, boolean expanded, boolean leaf,
                int row, boolean hasFocus) {
            
            initializeControl(value, tree, isSelected, hasFocus);
            return this;
        }
        
        /**
         * Initilazes the control with the FilterNode details.
         * @param value
         */
        protected void initializeControl(Object value,
                JTree tree,
                boolean isSelected,
                boolean hasFocus) {
            if(value instanceof IFilterNode) {
                IFilterNode cNode = (IFilterNode)value;
                String stringValue = cNode.getDispalyName() ;
                
                setEnabled(tree.isEnabled());
                
                if(cNode != null) {
                    check.setSelected(cNode.isOn());
                }
                
                strLabel.setFont(tree.getFont());
                strLabel.setText(stringValue);
                strLabel.setIcon(cNode.getIcon());
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
                
            } else {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                if(node.getUserObject() != null) {
                    strLabel.setText("  " + node.getUserObject().toString());
                }
            }
            
            
            
        }
        
        public Dimension getPreferredSize() 
        {
            Dimension d_check = check.getPreferredSize();
            Dimension d_label = strLabel.getPreferredSize();
            
            return new Dimension(
                d_check.width + d_label.width, 
                d_check.height < d_label.height 
                    ? d_label.height : d_check.height);
        }

        public void doLayout() 
        {
            Dimension d_check = check.getPreferredSize();
            Dimension d_label = strLabel.getPreferredSize();
            int y_check = 0;
            int y_label = 0;
            
            if (d_check.height > d_label.height) 
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

        public void setBackground(Color color) {
            if (color instanceof ColorUIResource)
                color = null;
            super.setBackground(color);
        }
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
                
                if (obj instanceof FilterNode) {
                    FilterNode node = (FilterNode) obj ;
                    boolean isSelected = !(node.isOn());
                    
                    int state = IFilterItem.FILTER_STATE_OFF;
                    if(isSelected == true) {
                        state = IFilterItem.FILTER_STATE_ON;
                    }
                    
                    setItemState(node, state);
                    
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
    
    
    /**
     * The FilterNode is used to wrap the IFilterItem.  The FilterNode is used
     * to isolate the users changes until they press the OK button.  Once the
     * OK button is pressed the IFilterItem will be updated with the users
     * changes.  If the user did not change the value of the IFilterItem then
     * the item will not be updated.
     *
     * @author Trey Spiva
     * @see IFilterItem
     */
    protected class FilterNode extends DefaultMutableTreeNode implements IFilterNode {
        private int         m_State = IFilterItem.FILTER_STATE_ON;
        private IFilterItem m_Item  = null;
        private Icon        m_Icon  = null;
        
        /**
         * Initializes the filter node.
         * @param item The filter item represented by the node.
         */
        public FilterNode(IFilterItem item) {
            setItem(item);
        }
        
        /**
         * Initializes the filter node.
         * @param item The filter item represented by the node.
         * @param icon The Icon to display.
         */
        public FilterNode(IFilterItem item, Icon icon) {
            setIcon(icon);
            setItem(item);
        }
        
        /**
         * Specifies if the checkbox for the filter item is on or off.
         *
         * @return <b>true</b> if the item is checked, <b>false</b> if the item is
         *         off.
         */
        public boolean isOn() {
            return m_State == IFilterItem.FILTER_STATE_ON;
        }
        
        /**
         * Sets the state of the filter item.
         *
         * @param value <b>true</b> if the item is checked, <b>false</b> if the
         *              item is off.
         * @param dialog The dialog that set the state.
         */
        public void setState(int value) {
            m_State = value;
        }
        
        /**
         * Retrieves the filter item associated with this node.
         *
         * @return The filter item.
         */
        public IFilterItem getItem() {
            return m_Item;
        }
        
        /**
         * Sets the filter item associated with this node.
         *
         * @param item The filter item.
         */
        public void setItem(IFilterItem item) {
            m_Item = item;
            setState(item.getState());
        }
        
        /**
         * Retrieves the name that is to be displayed to the user.
         *
         * @return The display name.
         */
        public String getDispalyName() {
            String retVal = ""; //$NON-NLS-1$
            if(m_Item != null) {
                retVal = m_Item.getName();
            }
            
            return retVal;
        }
        
        /**
         * Retrieves the icon that represent the filter item.
         *
         * @return The icon to display.
         */
        public Icon getIcon() {
            Icon retVal = null;
            
            if(getItem() != null) {
                retVal = getItem().getIcon();
            }
            
            if(retVal == null) {
                retVal = m_Icon;
            }
            
            return retVal;
        }
        
        /**
         * Sets the icon that represent the filter item.
         *
         * @param value The icon to display.
         */
        public void setIcon(Icon value) {
            m_Icon = value;
        }
        
        /**
         * Saves the nodes contents to the associated IFilterItem.  The children
         * of the node is also saved.
         */
        public void save(IFilterDialog dialog) {
            IFilterItem item = getItem();
            if((item != null) && (m_State != item.getState())) {
                item.setState(m_State, dialog);
            }
            
            int childrenCount = getChildCount();
            for(int index = 0; index < childrenCount; index++) {
                Object child = getChildAt(index);
                
                if (child instanceof IFilterNode) {
                    IFilterNode curNode = (IFilterNode)child;
                    if(curNode != null) {
                        curNode.save(dialog);
                    }
                }
            }
        }
        
      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.controls.filter.IFilterNode#add(org.netbeans.modules.uml.ui.controls.filter.IFilterNode)
       */
        public void add(IFilterNode newChild) throws IllegalArgumentException {
            if (newChild instanceof FilterNode) {
                FilterNode node = (FilterNode)newChild;
                super.add(node);
            }
        }
        
    }
    
    /**
     * The action that performs the OK button action.  The users changes will
     * be saved.
     */
    protected class OKAction extends AbstractAction {
        private JFilterDialog m_Dialog = null;
        
        public OKAction(JFilterDialog dialog) {
            super(ProjectTreeResources.getString("JFilterDialog.OK_Btn_Title")); //$NON-NLS-1$
            m_Dialog = dialog;
        }
        
        /**
         * Saves the users changes and closes the window.
         *
         * @param e The event data.
         */
        public void actionPerformed(ActionEvent e) {
            IFilterNode[] roots = getRootNodes();
            
            if(roots != null) {
                for (int index = 0; index < roots.length; index++) {
                    roots[index].save(m_Dialog);
                }
            }
            
            IProjectTreeFilterDialogEventDispatcher dispatcher = null;
            dispatcher = m_Helper.getProjectTreeFilterDialogDispatcher();
            
            IEventPayload payLoad = dispatcher.createPayload("ProjectTreeFilterDialogOKActivated"); //$NON-NLS-1$
            dispatcher.fireProjectTreeFilterDialogOKActivated(m_Dialog, payLoad);
            
            hide();
            dispose();
        }
    }
    
    /**
     * The action that performs the cancel button action.  The users changes will
     * be discarded.
     */
    public class CancelAction extends AbstractAction {
        public CancelAction() {
            super(ProjectTreeResources.getString("JFilterDialog.Cancel_Btn_Title")); //$NON-NLS-1$
        }
        
        /**
         * Saves the users changes and closes the window.
         *
         * @param e The event data.
         */
        public void actionPerformed(ActionEvent e) {
            hide();
            dispose();
        }
    }
    
    /**
     * Retrieves the project tree model that will be filtered.
     * @return The model that will be affectd.
     */
    public IProjectTreeModel getProjectTreeModel() {
        return m_ProjectTreeModel;
    }
    
    /**
     * Sest the project tree model that will be filted.
     * @param model The model that will be affected.
     */
    protected void setProjectTreeModel(IProjectTreeModel model) {
        m_ProjectTreeModel = model;
    }
    
    public static void main(String[] args) {
        final JFilterDialog dialog = new JFilterDialog(null);
        
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
            
            public void windowClosing(WindowEvent e) {
                dialog.dispose();
            }
        });
        
        IFilterNode elementsItem = dialog.createRootNode(ProjectTreeResources.getString("JFilterDialog.Root_Node_Name")); //$NON-NLS-1$
        dialog.addFilterItem(elementsItem, new FilterItem(ProjectTreeResources.getString("JFilterDialog.Class_Node_Name"))); //$NON-NLS-1$
        dialog.addFilterItem(elementsItem, new FilterItem(ProjectTreeResources.getString("JFilterDialog.Interface_Node_Name"))); //$NON-NLS-1$
        dialog.addFilterItem(elementsItem, new FilterItem(ProjectTreeResources.getString("JFilterDialog.SourceFileArtifact_Node_Name"))); //$NON-NLS-1$
        
        dialog.show();
        //System.exit(0);
    }
}
