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

package org.netbeans.modules.uml.codegen.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.netbeans.modules.uml.codegen.dataaccess.TemplateTableModel;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.DomainObject;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.Family;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.TemplateFamilies;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.TemplateFamiliesHandler;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 *
 */
public class DomainTemplatesManagerPanel extends javax.swing.JPanel
    implements ActionListener, TreeExpansionListener, TreeModelListener,
    TableModelListener, DocumentListener, ItemListener, ListSelectionListener
{
    private TemplateFamiliesHandler dataHandler = null;
    private List<String> checkedTree = null;
    private UMLProjectProperties projectProperties = null;
    private boolean dirtyTreeExpand = false;
    private boolean areModifyEventsEnabled = false;
    private boolean isCustomizer = false;
    
    Map<String, Boolean> treeExpandState = new HashMap<String, Boolean>();
    
    private static final Icon DOMAIN_OBJECT_NODE_ICON =
        new ImageIcon(Utilities.loadImage(
        "org/netbeans/modules/uml/resources/NewDiagrams.gif")); // NOI18N
    
    private static final Icon TEMPLATE_FAMILY_NODE_ICON =
        new ImageIcon(Utilities.loadImage(
        "org/netbeans/modules/uml/resources/project.gif")); // NOI18N
    
    // called by project customizer
    public DomainTemplatesManagerPanel(UMLProjectProperties properties)
    {
        initComponents();
        
        if (properties != null)
        {
            isCustomizer = true;
            projectProperties = properties;
            checkedTree = projectProperties.getCodeGenTemplatesArray();
            showButtons(false);
        }

        registerListeners();

        // if (!java.beans.Beans.isDesignTime())
        // {
        populateTemplatesTreeValues(true);
        //     populateModelElementsCombo();
        // }
        
        // gets set to true on the initial expand/collapse setup
        // so need to reset to not expanded
        dirtyTreeExpand = false;
        enableTableModifyingButtons(false);
        areModifyEventsEnabled = true;
    }
    
    // called by globlal options
    public DomainTemplatesManagerPanel()
    {
        this(null);
    }
    
    
    private void registerListeners()
    {
        // tree model listeners
        templatesTree.getModel().addTreeModelListener(this);
        templatesTree.addTreeExpansionListener(this);
        
        if (isCustomizer)
        {
            templatesTree.addMouseListener(
                new NodeSelectionListener(templatesTree));
        }
        
        // table model listeners
        templatesTable.getModel().addTableModelListener(this);
        templatesTable.getSelectionModel().addListSelectionListener(this);
        
        // input field listeners
        modelElementCombo.addItemListener(this);
        stereotypeText.getDocument().addDocumentListener(this);
        descriptionTextArea.getDocument().addDocumentListener(this);
        
        // button listeners
        addButton.addActionListener(this);
        removeButton.addActionListener(this);
        addRowButton.addActionListener(this);
        modifyRowButton.addActionListener(this);
        removeRowButton.addActionListener(this);
    }
    
    
    private void populateTemplatesTreeValues(boolean reset)
    {
        enableTreeModifyingButtons(DISABLE_ADD_REMOVE_BUTTONS);
        enableTemplatePropsFields(false);
        
        dataHandler = TemplateFamiliesHandler.getInstance(reset);
        
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        rootNode.setUserObject(new DomainTreeNode(
            org.openide.util.NbBundle.getMessage(
                DomainTemplatesManagerPanel.class, "LBL_TemplateFamilies")));
        
        if (isCustomizer)
        {
            DomainTreeNodeRendererEditor renderer = 
                new DomainTreeNodeRendererEditor();
            
            templatesTree.setCellRenderer(renderer);
            templatesTree.setEditable(false);
        }
        
        else
        {
            DefaultTreeCellRenderer iconRenderer =
                (DefaultTreeCellRenderer)templatesTree.getCellRenderer();
            
            iconRenderer.setLeafIcon(DOMAIN_OBJECT_NODE_ICON);
            iconRenderer.setClosedIcon(TEMPLATE_FAMILY_NODE_ICON);
            iconRenderer.setOpenIcon(TEMPLATE_FAMILY_NODE_ICON);
        }
        
        Family[] familyList = dataHandler.getTemplateFamilies().getFamily();
        
        int i = 0;
        int famTot = familyList.length;
        
        DefaultMutableTreeNode[] expandedNodes =
            new DefaultMutableTreeNode[famTot];
        
        for (Family family: familyList)
        {
            String familyName = family.getName();
            
//            DefaultMutableTreeNode familyNode =
//                new DefaultMutableTreeNode(familyName, true);

            DefaultMutableTreeNode familyNode = new DefaultMutableTreeNode();
            familyNode.setUserObject(
                new DomainTreeNode(familyName, false, familyName, null));
            
            rootNode.add(familyNode);
            expandedNodes[i] = null;
            
            if (Boolean.valueOf(family.isExpanded()))
                expandedNodes[i] = familyNode;
            
            DomainObject[] domainList = family.getDomainObject();
            
            for (DomainObject domain: domainList)
            {
                DefaultMutableTreeNode domainNode = null;
                String domainName = domain.getName();
                
                domainNode = new DefaultMutableTreeNode();
                domainNode.setUserObject(new DomainTreeNode(
                    domainName, 
                    isCustomizer ? 
                        getCheckedValue(familyName, domainName) : false, 
                    familyName,
                    domainName));
                
                familyNode.add(domainNode);
            }
            
            i++;
        }
        
        templatesTree.setModel(new DefaultTreeModel(rootNode));
        
        for (i = 0; i < famTot; i++)
        {
            if (expandedNodes[i] != null)
            {
                if (expandedNodes[i].getChildCount() > 0)
                {
                    templatesTree.scrollPathToVisible(new TreePath((
                        (DefaultMutableTreeNode)expandedNodes[i]
                        .getFirstChild()).getPath()));
                }
            }
        }
        
        templatesTree.getModel().addTreeModelListener(this);
    }
    
    
    
    private boolean getCheckedValue(String familyName, String domainName)
    {
        return checkedTree.indexOf(familyName + ':' + domainName) != -1;
    }
    
//    private void initSpacebarListener()
//    {
//        ActionListener action = new ActionListener()
//        {
//            public void actionPerformed(ActionEvent e)
//            {
//                int selectedRows[] = templatesTree.getSelectionRows();
//                
//                if (selectedRows == null || selectedRows.length == 0)
//                    return;
//                
//                // get the path of the 1st selected row
//                int row = selectedRows[0];
//                TreePath path = templatesTree.getPathForRow(row);
//                
//                if (path != null)
//                {
//                    DomainTemplatesManagerPanel.CheckBoxTreeNode node = 
//                        (DomainTemplatesManagerPanel.CheckBoxTreeNode) 
//                        path.getLastPathComponent();
//                    
//                    boolean isSelected = !(node.isSelected());
//                    
////                    int state = IFilterItem.FILTER_STATE_OFF;
////                    if (isSelected == true)
////                    {
////                        state = IFilterItem.FILTER_STATE_ON;
////                    }
////                    setItemState(node, state);
//                    
////                    ((DefaultTreeModel) templatesTree.getModel()).nodeChanged(node);
//                    
//                    if (row == 0)
//                    {
//                        templatesTree.revalidate();
//                        templatesTree.repaint();
//                    }
//                }
//            }
//        };
//        
//        // use SPACE bar to select/deselect the m_Tree node
//        templatesTree.registerKeyboardAction(action, 
//            KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0), 
//                JComponent.WHEN_FOCUSED);
//    }
    
    
    
    // called by UMLOptionsPanel
    public void store()
    {
        persistTreeExpandState(false);
        dataHandler.save();
    }
    
    // called by UMLOptionsPanel
    public void load()
    {
        populateTemplatesTreeValues(true);
    }
    
    public void cancel () 
    {
        persistTreeExpandState(false);
        populateTemplatesTreeValues(true);
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">     
    public void actionPerformed(ActionEvent event)
    {
        String cmd = event.getActionCommand();
        
        // listener of the OK button in the Project Customizer
        if (cmd.equals("OK")) // NOI18N
        {
            // Project Customizer UI
            if (projectProperties != null)
            {
                projectProperties.setCodeGenTemplates(checkedTree);
                projectProperties.save();
                persistTreeExpandState(true);
            }
        }
        
        else if (cmd.equals("Cancel")) // NOI18N
        {
            dataHandler.reset();
            persistTreeExpandState(true);
        }
        
        else if (cmd.equals("ADD")) // NOI18N
        {
            persistTreeExpandState(false);
            TemplateFamilies templateFamilies = dataHandler.getTemplateFamilies();
            Family[] familyList = templateFamilies.getFamily();
            TreePath treeSelPath = templatesTree.getSelectionPath();
            int parentRow =  templatesTree.getRowForPath(treeSelPath);
            int index = 0;
            DefaultMutableTreeNode parentNode = null;
            String defaultName = null;
            String baseName = null;
            boolean foundUnique = false;
            
            switch (treeSelPath.getPathCount())
            {
            case 0:
                return;
                
            case 1: // Template Families (tree root)
                parentNode = (DefaultMutableTreeNode)treeSelPath.getPath()[0];
                
                Family family = new Family();
                family.setExpanded(Boolean.TRUE);
                
                defaultName = NbBundle.getMessage(
                    DomainTemplatesManagerPanel.class,
                    "LBL_DefaultFamilyName"); // NOI18N
                
                baseName = defaultName;
                
                for (int i=1; !foundUnique; i++)
                {
                    if (templateFamilies.isUniqueFamilyName(defaultName))
                        foundUnique = true;
                    
                    else
                        defaultName = baseName + i;
                }
                
                index = familyList.length;
                family.setName(defaultName);
                templateFamilies.addFamily(family);
                
                ((DefaultTreeModel)templatesTree.getModel()).insertNodeInto(
                    (new DefaultMutableTreeNode(family.getName(), false)),
                    parentNode, index);
                
                populateTemplatesTreeValues(false);
                selectTreeNode(index + parentRow, false);
                break;
                
            case 2: // Template Family Name
                Family familyParent = templateFamilies.getFamilyByName(
                    treeSelPath.getLastPathComponent().toString());
                
                DomainObject[] domainList = familyParent.getDomainObject();
                parentNode = (DefaultMutableTreeNode)treeSelPath.getPath()[1];
                DomainObject domain = new DomainObject();
                
                defaultName = NbBundle.getMessage(
                    DomainTemplatesManagerPanel.class,
                    "LBL_DefaultDomainName"); // NOI18N
                
                baseName = defaultName;
                
                for (int i=1; !foundUnique; i++)
                {
                    if (familyParent.isUniqueDomainName(defaultName))
                        foundUnique = true;
                    
                    else
                        defaultName = baseName + i;
                }
                
                index = domainList.length;
                domain.setName(defaultName);
                familyParent.addDomainObject(domain);
                
                ((DefaultTreeModel)templatesTree.getModel()).insertNodeInto(
                    (new DefaultMutableTreeNode(domain.getName(), false)),
                    parentNode, index);
                
                populateTemplatesTreeValues(false);
                selectTreeNode(index + parentRow, false);
                break;
                
            case 3: // Template (Domain) Name
                return;
            }
        }
        
        else if (cmd.equals("REMOVE")) // NOI18N
        {
            TemplateFamilies tfams = null;
            persistTreeExpandState(false);
            TreePath treeSelPath = templatesTree.getSelectionPath();
            DefaultMutableTreeNode treeNode = null;
            TreeNode parentNode = null;
            int removedRowIndex = templatesTree.getRowForPath(treeSelPath);
            
            switch (treeSelPath.getPathCount())
            {
            case 0:
            case 1: // Template Families (tree root)
                return;
                
            case 2: // Template Family Name
                treeNode = (DefaultMutableTreeNode)treeSelPath.getPath()[1];
                parentNode = treeNode.getParent();
                tfams = dataHandler.getTemplateFamilies();
                tfams.removeFamily(tfams.getFamilyByName(treeNode.toString()));
                
                ((DefaultTreeModel)templatesTree.getModel())
                    .removeNodeFromParent(treeNode);
                
                populateTemplatesTreeValues(false);
                selectTreeNode(removedRowIndex, true);
                break;
                
            case 3: // Template (Domain) Name
                treeNode = (DefaultMutableTreeNode)treeSelPath.getPath()[2];
                parentNode = treeNode.getParent();
                tfams = dataHandler.getTemplateFamilies();
                Family family = tfams.getFamilyByName(parentNode.toString());

                family.removeDomainObject(
                    family.getDomainByName(treeNode.toString()));
                
                ((DefaultTreeModel)templatesTree.getModel())
                    .removeNodeFromParent(treeNode);
                
                populateTemplatesTreeValues(false);
                selectTreeNode(removedRowIndex, true);
                break;
            }
            
        }
        
        else if (cmd.equals("ADD_ROW")) // NOI18N
        {
            displayTemplatesRowPanel(true);
            templatesTable.setFocusable(true);
        }
        
        else if (cmd.equals("MODIFY_ROW")) // NOI18N
        {
            displayTemplatesRowPanel(false);
            templatesTable.setFocusable(true);
        }
        
        else if (cmd.equals("REMOVE_ROW")) // NOI18N
        {
            ((DefaultTableModel)templatesTable.getModel())
                .removeRow(templatesTable.getSelectedRow());
            
            templatesTable.setFocusable(true);
        }
    }
    
    
    private void selectTreeNode(int affectedRow, boolean rowRemoved)
    {
        if (rowRemoved)
        {
            if (affectedRow >= templatesTree.getRowCount())
                affectedRow = templatesTree.getRowCount() - 1;
            
            else
                affectedRow--;
        }
        
        else
            affectedRow++;
        
        templatesTree.scrollRowToVisible(affectedRow);
        templatesTree.setSelectionRow(affectedRow);
        templatesTree.requestFocusInWindow();
    }
    
    private boolean displayTemplatesRowPanel(boolean isAddMode)
    {
        TemplatesTableRowPanel ttrPanel = null;
        DefaultTableModel model = (DefaultTableModel)templatesTable.getModel();
        int selrow = templatesTable.getSelectedRow();
        
        if (isAddMode)
            ttrPanel = new TemplatesTableRowPanel();
        
        else
        {
            ttrPanel = new TemplatesTableRowPanel(
                (String)model.getValueAt(selrow, 0),
                (String)model.getValueAt(selrow, 1),
                (String)model.getValueAt(selrow, 2),
                (String)model.getValueAt(selrow, 3));
        }
        
        DialogDescriptor dd = new DialogDescriptor(
            
            ttrPanel,NbBundle.getMessage(DomainTemplatesManagerPanel.class,
            "LBL_TemplatesTableRowDialogTitle"), // NOI18N
            true, // modal flag
            NotifyDescriptor.OK_CANCEL_OPTION, // button option type
            NotifyDescriptor.OK_OPTION, // default button
            DialogDescriptor.DEFAULT_ALIGN, // button alignment
            null, // new HelpCtx(), // NOI18N
            ttrPanel); // button action listener
        
        ttrPanel.requestFocus();
        
        if (DialogDisplayer.getDefault().notify(dd)
            == NotifyDescriptor.OK_OPTION)
        {
            // add a new row
            if (isAddMode)
            {
                model.addRow(new String[]
                {
                    ttrPanel.getFilenameFormat(),
                    ttrPanel.getExtension(),
                    ttrPanel.getFolder(),
                    ttrPanel.getTemplateFilename()
                });
            }
            
            // update selected row
            else
            {
                model.setValueAt(ttrPanel.getFilenameFormat(), selrow, 0);
                model.setValueAt(ttrPanel.getExtension(), selrow, 1);
                model.setValueAt(ttrPanel.getFolder(), selrow, 2);
                model.setValueAt(ttrPanel.getTemplateFilename(), selrow, 3);
            }
            
            return true;
        }
        
        return false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        templatesTreePanel = new javax.swing.JPanel();
        treeScroll = new javax.swing.JScrollPane();
        templatesTree = new javax.swing.JTree();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        templatePropsPanel = new javax.swing.JPanel();
        modelElementLabel = new javax.swing.JLabel();
        modelElementCombo = new javax.swing.JComboBox();
        stereotypeLabel = new javax.swing.JLabel();
        stereotypeText = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionScroll = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        templatesTableScroll = new javax.swing.JScrollPane();
        templatesTable = new javax.swing.JTable();
        addRowButton = new javax.swing.JButton();
        modifyRowButton = new javax.swing.JButton();
        removeRowButton = new javax.swing.JButton();

        templatesTreePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "LBL_DomainObjects_PanelTitle"))); // NOI18N

        templatesTree.setAutoscrolls(true);
        templatesTree.setEditable(true);
        templatesTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener()
        {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt)
            {
                templatesTreeValueChanged(evt);
            }
        });
        treeScroll.setViewportView(templatesTree);

        addButton.setText(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "BTN_Add")); // NOI18N
        addButton.setActionCommand("ADD");

        removeButton.setText(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "BTN_Remove")); // NOI18N
        removeButton.setActionCommand("REMOVE");

        org.jdesktop.layout.GroupLayout templatesTreePanelLayout = new org.jdesktop.layout.GroupLayout(templatesTreePanel);
        templatesTreePanel.setLayout(templatesTreePanelLayout);
        templatesTreePanelLayout.setHorizontalGroup(
            templatesTreePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, templatesTreePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(templatesTreePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, treeScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                    .add(templatesTreePanelLayout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addContainerGap())
        );
        templatesTreePanelLayout.setVerticalGroup(
            templatesTreePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, templatesTreePanelLayout.createSequentialGroup()
                .add(treeScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(templatesTreePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addButton)
                    .add(removeButton)))
        );

        templatePropsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "LBL_DomainProperties_PanelTitle"))); // NOI18N

        modelElementLabel.setText(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "LBL_Model_Element")); // NOI18N

        modelElementCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<None Selected>", "Class", "Interface", "Enumeration", "Aborted Final State", "Actor", "Activity Final Node", "Choice Pseudo State", "Combined Fragment", "Comment", "Component", "Composite State", "Data Store", "Datatype", "Decision", "Deep History State", "Deployment Specification", "Deriviation Classifier", "Entry Point State", "Final State", "Flow Final", "Initial Node", "Invocation", "Junction State", "Lifeline", "Node", "Package", "Parameter Usage", "Simple State", "Shallow History State", "Signal", "Submachine State", "Template Class", "Use Case" }));

        stereotypeLabel.setText(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "LBL_Stereotype")); // NOI18N

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "LBL_Description")); // NOI18N

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setRows(2);
        descriptionTextArea.setTabSize(4);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionScroll.setViewportView(descriptionTextArea);

        templatesTableScroll.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "LBL_Templates_TablePanelTitle"))); // NOI18N

        templatesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null},
                {null, null, null, null}
            },
            new String []
            {
                "Filename Format", "Extension", "Folder Path", "Template File"
            }
        )
        {
            Class[] types = new Class []
            {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean []
            {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex)
            {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        templatesTable.setNextFocusableComponent(addRowButton);
        templatesTable.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusGained(java.awt.event.FocusEvent evt)
            {
                templatesTableFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                templatesTableFocusLost(evt);
            }
        });
        templatesTableScroll.setViewportView(templatesTable);

        addRowButton.setText(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "BTN_Add_Row")); // NOI18N
        addRowButton.setActionCommand("ADD_ROW");

        modifyRowButton.setText(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "BTN_Modify_Row")); // NOI18N
        modifyRowButton.setActionCommand("MODIFY_ROW");

        removeRowButton.setText(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "BTN_Remove_Row")); // NOI18N
        removeRowButton.setActionCommand("REMOVE_ROW");

        org.jdesktop.layout.GroupLayout templatePropsPanelLayout = new org.jdesktop.layout.GroupLayout(templatePropsPanel);
        templatePropsPanel.setLayout(templatePropsPanelLayout);
        templatePropsPanelLayout.setHorizontalGroup(
            templatePropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, templatePropsPanelLayout.createSequentialGroup()
                .add(templatePropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(templatesTableScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
                    .add(templatePropsPanelLayout.createSequentialGroup()
                        .add(templatePropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(stereotypeLabel)
                            .add(modelElementLabel)
                            .add(descriptionLabel))
                        .add(templatePropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(templatePropsPanelLayout.createSequentialGroup()
                                .add(79, 79, 79)
                                .add(addRowButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(modifyRowButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(removeRowButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(templatePropsPanelLayout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(templatePropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(stereotypeText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                                    .add(modelElementCombo, 0, 340, Short.MAX_VALUE)
                                    .add(descriptionScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE))))))
                .addContainerGap())
        );
        templatePropsPanelLayout.setVerticalGroup(
            templatePropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(templatePropsPanelLayout.createSequentialGroup()
                .add(templatePropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(modelElementLabel)
                    .add(modelElementCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(7, 7, 7)
                .add(templatePropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(stereotypeLabel)
                    .add(stereotypeText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(templatePropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(descriptionLabel)
                    .add(descriptionScroll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(templatesTableScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(templatePropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addRowButton)
                    .add(modifyRowButton)
                    .add(removeRowButton)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(templatesTreePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(templatePropsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, templatesTreePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, templatePropsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
private void templatesTableFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_templatesTableFocusGained
    enableTableModifyingButtons(true);
}//GEN-LAST:event_templatesTableFocusGained

private void templatesTableFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_templatesTableFocusLost
    if (evt.getOppositeComponent() != addRowButton &&
        evt.getOppositeComponent() != modifyRowButton &&
        evt.getOppositeComponent() != removeRowButton)
    {
        templatesTable.getSelectionModel().clearSelection();
        enableTableModifyingButtons(true);
    }
}//GEN-LAST:event_templatesTableFocusLost

    private void templatesTreeValueChanged(javax.swing.event.TreeSelectionEvent evt)//GEN-FIRST:event_templatesTreeValueChanged
    {//GEN-HEADEREND:event_templatesTreeValueChanged
        areModifyEventsEnabled = false;
        
        // previous selected item was modified, so push values into tree model
        if (evt.getOldLeadSelectionPath() != null &&
            evt.getOldLeadSelectionPath().getPathCount() > 2)
        {
            updateTreeModel(
                evt.getOldLeadSelectionPath().getPath()[1].toString(),
                evt.getOldLeadSelectionPath().getPath()[2].toString());
        }
        
        switch (evt.getPath().getPathCount())
        {
        case 0: // shouldn't happen
            enableTreeModifyingButtons(DISABLE_ADD_REMOVE_BUTTONS);
            enableTableModifyingButtons(false);
            clearTemplateProps();
            clearTemplatesTable();
            enableTemplatesTable(false);
            break;
            
        case 1: // Template Families (tree root)
            enableTreeModifyingButtons(ENABLE_ADD_BUTTON);
            enableTableModifyingButtons(false);
            clearTemplateProps();
            clearTemplatesTable();
            enableTemplatesTable(false);
            break;
            
        case 2: // Template Family Name
            enableTreeModifyingButtons(ENABLE_ADD_REMOVE_BUTTONS);
            clearTemplateProps();
            clearTemplatesTable();
            enableTemplatesTable(false);
            enableTableModifyingButtons(false);
            break;
            
        case 3: // Template (Domain) Name
            String familyName = null;
            String domainName = null;
            
            if (evt.getNewLeadSelectionPath() != null)
            {
                familyName = evt.getNewLeadSelectionPath().getParentPath()
                    .getLastPathComponent().toString();
                
                domainName = evt.getNewLeadSelectionPath()
                    .getLastPathComponent().toString();
            }
            
            else if (templatesTree.getSelectionPath() != null)
            {
                familyName = templatesTree.getSelectionPath()
                    .getParentPath().getLastPathComponent().toString();
                
                domainName = templatesTree.getSelectionPath()
                    .getLastPathComponent().toString();
            }
            
            enableTreeModifyingButtons(ENABLE_REMOVE_BUTTON);
            enableTableModifyingButtons(true);
            enableTemplatesTable(true);
            populateTemplateProps(familyName, domainName);
        }
        
        areModifyEventsEnabled = true;
    }//GEN-LAST:event_templatesTreeValueChanged
    
    
    private void updateTreeModel(String familyName, String domainName)
    {
        DomainObject domainObject = dataHandler.getTemplateFamilies()
            .getFamilyByName(familyName).getDomainByName(domainName);
        
        if (domainObject != null)
        {
            domainObject.setModelElement(
                modelElementCombo.getSelectedItem() == null
                ? null : modelElementCombo.getSelectedItem().toString());
            
            domainObject.setStereotype(stereotypeText.getText());
            domainObject.setDescription(descriptionTextArea.getText());
            
            domainObject.updateTemplates(
                (DefaultTableModel)templatesTable.getModel());
        }
    }
    
    
    private void showButtons(boolean flag)
    {
        // tree buttons
        addButton.setVisible(flag);
        removeButton.setVisible(flag);
        
        // table buttons
        addRowButton.setVisible(flag);
        modifyRowButton.setVisible(flag);
        removeRowButton.setVisible(flag);
    }
    
    private final static int ENABLE_ADD_REMOVE_BUTTONS = 0;
    private final static int ENABLE_ADD_BUTTON = 1;
    private final static int ENABLE_REMOVE_BUTTON = 2;
    private final static int DISABLE_ADD_REMOVE_BUTTONS = 3;
    
    private void enableTreeModifyingButtons(int enableToken)
    {
        switch (enableToken)
        {
            case ENABLE_ADD_REMOVE_BUTTONS:
                addButton.setEnabled(true);
                removeButton.setEnabled(true);
                break;

            case ENABLE_ADD_BUTTON:
                addButton.setEnabled(true);
                removeButton.setEnabled(false);
                break;

            case ENABLE_REMOVE_BUTTON:
                addButton.setEnabled(false);
                removeButton.setEnabled(true);
                break;

            case DISABLE_ADD_REMOVE_BUTTONS:
                addButton.setEnabled(false);
                removeButton.setEnabled(false);
                break;
        }
    }
    
    private void enableTableModifyingButtons(boolean flag)
    {
        boolean isRowSelected = flag;
        if (isRowSelected)
            isRowSelected = templatesTable.getSelectedRowCount() != 0;
        
        addRowButton.setEnabled(flag);
        modifyRowButton.setEnabled(isRowSelected);
        removeRowButton.setEnabled(isRowSelected);
    }
    
    private void clearTemplateProps()
    {
        enableTemplatePropsFields(false);
        modelElementCombo.setSelectedIndex(0);
        stereotypeText.setText("");
        descriptionTextArea.setText("");
    }
    
    private void populateTemplateProps(String familyName, String domainName)
    {
        if (familyName == null || domainName == null)
        {
            enableTemplatePropsFields(false);
            clearTemplateProps();
            clearTemplatesTable();
            return;
        }
        
        DomainObject domainObject = dataHandler.getTemplateFamilies()
            .getFamilyByName(familyName).getDomainByName(domainName);
        
        if (domainObject != null)
        {
            enableTemplatePropsFields(true);
            modelElementCombo.setSelectedItem(domainObject.getModelElement());
            stereotypeText.setText(domainObject.getStereotype());
            descriptionTextArea.setText(domainObject.getDescription());
            populateTemplatesTable(domainObject.getTemplatesTableData());
        }
    }
    
    private void enableTemplatePropsFields(boolean flag)
    {
        // if called from project customizer, always disable these fields
        if (flag && checkedTree != null)
            flag = false;
        
        templatePropsPanel.setEnabled(flag);
        modelElementCombo.setEnabled(flag);
        stereotypeText.setEnabled(flag);
        descriptionTextArea.setEnabled(flag);
        
        templatesTableScroll.setEnabled(flag);
        templatesTable.setEnabled(flag);
        
        enableTemplatesTable(flag);
    }
    
    
    private void populateTemplatesTable(String[][] templateData)
    {
        DefaultTableModel tblMdl;
        
        if (templateData != null)
            tblMdl = new TemplateTableModel(templateData);
        
        else
            tblMdl = new DefaultTableModel();
        
        templatesTable.setModel(tblMdl);
        templatesTable.getModel().addTableModelListener(this);
        templatesTable.getSelectionModel().addListSelectionListener(this);
    }
    
    private void clearTemplatesTable()
    {
        populateTemplatesTable(new String[0][4]);
    }
    
    private void enableTemplatesTable(boolean flag)
    {
        templatesTableScroll.setEnabled(flag);
        templatesTable.setEnabled(flag);
    }
    
    
    public void treeExpanded(TreeExpansionEvent event)
    {
        // if root node, no need to proceed
        if (event.getPath().getPathCount() == 1)
            return;
        
        setNodeExpandedState((((DefaultMutableTreeNode)event.getPath()
            .getLastPathComponent()).toString()), true);
    }
    
    public void treeCollapsed(TreeExpansionEvent event)
    {
        // if root node, no need to proceed
        if (event.getPath().getPathCount() == 1)
            return;
        
        setNodeExpandedState((((DefaultMutableTreeNode)event.getPath()
            .getLastPathComponent()).getUserObject().toString()), false);
    }
    
    
    private void setNodeExpandedState(String familyName, boolean isExpanded)
    {
        dirtyTreeExpand = true;
        treeExpandState.put(familyName, Boolean.valueOf(isExpanded));
        
        dataHandler.getTemplateFamilies()
            .getFamilyByName(familyName).setExpanded(isExpanded);
    }
    
    
    private void persistTreeExpandState(boolean saveData)
    {
        if (!dirtyTreeExpand)
            return;
        
        TemplateFamilies templateFamilies = dataHandler.getTemplateFamilies();
        Set<String> keys = treeExpandState.keySet();
        
        for (String familyName: keys)
        {
            Boolean expandState = treeExpandState.get(familyName);
            
            if (expandState != null)
            {
                Family family = templateFamilies.getFamilyByName(familyName);
                if (family != null)
                    family.setExpanded(expandState.booleanValue());
            }
            
            else
                treeExpandState.remove(familyName);
        }
        
        if (saveData)
            dataHandler.save();
        
        dirtyTreeExpand = false;
    }
    
    
    public void treeNodesChanged(TreeModelEvent event)
    {
        if (projectProperties != null)
            return;
        
        TemplateFamilies templateFamilies = dataHandler.getTemplateFamilies();
        int index = event.getChildIndices()[0];
        
        String newName = ((DefaultMutableTreeNode)event.getChildren()[0])
            .getUserObject().toString();
        
        // Root node rename - TODO: disable rename for root node
        if (event.getPath().length == 0)
        {
            ((DefaultMutableTreeNode)event.getChildren()[0])
                .setUserObject("Template Families");
        }
        
        // Family node rename
        else if (event.getPath().length == 1)
        {
            String oldName = templateFamilies.getFamily()[index].getName();
            
            // if new name is unique and not same as old name, then rename the
            // node in the tree model, as well
            if (!newName.equals(oldName) &&
                templateFamilies.isUniqueFamilyName(newName))
            {
                templateFamilies.getFamily()[index].setName(newName);
            }
            
            // name not unique or not new, so revert to old name
            else
            {
                ((DefaultMutableTreeNode)event.getChildren()[0])
                    .setUserObject(oldName);
                // TODO: reset to edit node mode
                // TODO: display error message that family name must be unique
            }
        }
        
        // Domain node rename
        else if (event.getPath().length == 2)
        {
            String familyName = event.getPath()[1].toString();
            Family parentFamily = templateFamilies.getFamilyByName(familyName);
            String oldName = parentFamily.getDomainObject()[index].getName();
            
            // if new name is unique and not same as old name, then rename the
            // node in the tree model, as well
            if (!newName.equals(oldName) &&
                parentFamily.isUniqueDomainName(newName))
            {
                templateFamilies.getFamilyByName(familyName)
                    .getDomainObject()[index].setName(newName);
            }
            
            // name not unique or not new, so revert to old name
            else
            {
                ((DefaultMutableTreeNode)event.getChildren()[0])
                    .setUserObject(oldName);
                // TODO: reset to edit node mode
                // TODO: display error message that domain name must be unique
            }
        }
    }
    
    public void treeNodesInserted(TreeModelEvent event)
    {
        // System.out.println(event.toString());
    }
    
    public void treeNodesRemoved(TreeModelEvent event)
    {
        // System.out.println(event.toString());
    }
    
    public void treeStructureChanged(TreeModelEvent event)
    {
        // System.out.println(event.toString());
    }
    
    public void tableChanged(TableModelEvent event)
    {
        // System.out.println(event.toString());
    }
    
    public void itemStateChanged(ItemEvent event)
    {
        // System.out.println(event.toString());
    }
    
    public void insertUpdate(DocumentEvent event)
    {
        // System.out.println(event.toString());
    }
    
    public void removeUpdate(DocumentEvent event)
    {
        // System.out.println(event.toString());
    }
    
    public void changedUpdate(DocumentEvent event)
    {
        // System.out.println(event.toString());
    }
    
    // table row selection event
    public void valueChanged(ListSelectionEvent event)
    {
    }
    
    
    
    protected void setItemState(DefaultMutableTreeNode node, boolean checked) 
    {
        DomainTreeNode domainTreeNode = (DomainTreeNode)node.getUserObject();
        domainTreeNode.setChecked(checked);
        int childrenCount = node.getChildCount();
        
        for (int index = 0; index < childrenCount; index++) 
        {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)
                templatesTree.getModel().getChild(node, index);
            
            if (childNode != null) 
                ((DomainTreeNode)childNode.getUserObject()).setChecked(checked);
        }
    }

    
    int rowLastClicked = -1;
    
    class NodeSelectionListener extends MouseAdapter
    {
        JTree tree;
        
        public NodeSelectionListener(JTree tree)
        {
            this.tree = tree;
        }
        
        public void mouseClicked(MouseEvent event)
        {
            int x = event.getX();
            int y = event.getY();
            int row = tree.getRowForLocation(x, y);
            
            if (row == -1)
            {
                row = rowLastClicked;
                return;
            }
            
            if (row != rowLastClicked)
            {
                rowLastClicked = row;
                return;
            }

            rowLastClicked = row;
            TreePath path = tree.getPathForRow(row);
            
            if (path != null)
            {
                DefaultMutableTreeNode node = 
                    (DefaultMutableTreeNode)path.getLastPathComponent();
                
                DomainTreeNode domainTreeNode = 
                    (DomainTreeNode)node.getUserObject();
                
                if (!domainTreeNode.isDomain())
                    return;
                
                boolean isSelected = !(domainTreeNode.isChecked());
                boolean state = isSelected;

                // domain node checked/unchecked
                if (domainTreeNode.isDomain())
                {
                    updateCheckedTree(
                        state, 
                        domainTreeNode.getFamilyName() + ":" + // NOI18N
                        domainTreeNode.getDomainName());
                }

                // family node checked/unchecked, so update all domain 
                // children to have the same state as its parent
//                else if (node.getChildCount() > 0)
//                {
//                    for (int i=0; i < node.getChildCount(); i++)
//                    {
//                        DomainTreeNode childDomainTreeNode = 
//                            (DomainTreeNode)((DefaultMutableTreeNode)node
//                            .getChildAt(i)).getUserObject();
//                        
//                        updateCheckedTree(state, 
//                            childDomainTreeNode.getFamilyName() + ":" + // NOI18N
//                            childDomainTreeNode.getDomainName());
//                    }
//                }

                setItemState(node, state);
                ((DefaultTreeModel)tree.getModel()).nodeChanged(node);

                if (row == 0)
                {
                    tree.revalidate();
                    tree.repaint();
                }
            }
        }
    }
    
    private void updateCheckedTree(boolean checkedState, String value)
    {
        int index = checkedTree.indexOf(value);

        if (index == -1 && checkedState)
            checkedTree.add(value);
        
        else if (index > -1 && !checkedState)
            checkedTree.remove(index);
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addRowButton;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane descriptionScroll;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JComboBox modelElementCombo;
    private javax.swing.JLabel modelElementLabel;
    private javax.swing.JButton modifyRowButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton removeRowButton;
    private javax.swing.JLabel stereotypeLabel;
    private javax.swing.JTextField stereotypeText;
    private javax.swing.JPanel templatePropsPanel;
    private javax.swing.JTable templatesTable;
    private javax.swing.JScrollPane templatesTableScroll;
    private javax.swing.JTree templatesTree;
    private javax.swing.JPanel templatesTreePanel;
    private javax.swing.JScrollPane treeScroll;
    // End of variables declaration//GEN-END:variables
    
}
