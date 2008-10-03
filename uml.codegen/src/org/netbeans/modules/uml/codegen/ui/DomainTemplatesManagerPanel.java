/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.codegen.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.netbeans.modules.uml.codegen.dataaccess.TemplateTableModel;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.DomainObject;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.Family;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.TemplateFamilies;
import org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.TemplateFamiliesHandler;

import org.openide.util.ImageUtilities;
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
    implements ActionListener, TreeModelListener, ListSelectionListener
{
    private TemplateFamiliesHandler dataHandler = null;
    private boolean dirtyTreeExpand = false;
    private Map<String, Boolean> treeExpandState = new HashMap<String, Boolean>();
    
    private static final Icon DOMAIN_OBJECT_NODE_ICON =
        new ImageIcon(ImageUtilities.loadImage(
        "org/netbeans/modules/uml/resources/images/templates.png")); // NOI18N
    
    private static final Icon TEMPLATE_FAMILY_NODE_ICON =
        new ImageIcon(ImageUtilities.loadImage(
        "org/netbeans/modules/uml/resources/images/default-category.gif")); // NOI18N
    
    public DomainTemplatesManagerPanel()
    {
        initComponents();
        registerListeners();
        populateElementTypeChoices();
        populateTemplatesTreeValues(false);
        clearTemplatesTable();
        
        templatesTree.getSelectionModel()
            .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        // gets set to true on the initial expand/collapse setup
        // so need to reset to not expanded
        dirtyTreeExpand = false;
        enableTableModifyingButtons(false);
    }
    
    
    
    private void registerListeners()
    {
        // button listeners
        addButton.addActionListener(this);
        removeButton.addActionListener(this);
        addRowButton.addActionListener(this);
        modifyRowButton.addActionListener(this);
        removeRowButton.addActionListener(this);
    }
    
    
    private void populateElementTypeChoices()
    {
        for (String type: ELEMENT_TYPE_CHOICE_BUNDLE_KEYS)
        {
            modelElementCombo.addItem(NbBundle.getMessage(
                DomainTemplatesManagerPanel.class, type));
        }
    }
    
    private final static String[] ELEMENT_TYPE_CHOICE_BUNDLE_KEYS = new String[]
    {
        "VAL_ElementType_NodeSelected", // NOI18N
        "VAL_ElementType_Class", // NOI18N
        "VAL_ElementType_Interface", // NOI18N
        "VAL_ElementType_Enumeration", // NOI18N
        "VAL_ElementType_Action", // NOI18N
        "VAL_ElementType_Component", // NOI18N
        "VAL_ElementType_Datatype", // NOI18N
        "VAL_ElementType_Invocation", // NOI18N
        "VAL_ElementType_Lifeline", // NOI18N
        "VAL_ElementType_Node", // NOI18N
        "VAL_ElementType_SimpleState", // NOI18N
        "VAL_ElementType_UseCase", // NOI18N
        "VAL_ElementType_AbortedFinalState", // NOI18N
        "VAL_ElementType_ActivityFinalNode", // NOI18N
        "VAL_ElementType_ChoicePseudoState", // NOI18N
        "VAL_ElementType_CombinedFragment", // NOI18N
        "VAL_ElementType_Comment", // NOI18N
        "VAL_ElementType_CompositeState", // NOI18N
        "VAL_ElementType_DataStore", // NOI18N
        "VAL_ElementType_Decision", // NOI18N
        "VAL_ElementType_DeepHistoryState", // NOI18N
        "VAL_ElementType_DeploymentSpecification", // NOI18N
        "VAL_ElementType_DerivationClassifier", // NOI18N
        "VAL_ElementType_EntryPointState", // NOI18N
        "VAL_ElementType_FinalState", // NOI18N
        "VAL_ElementType_FlowFinal", // NOI18N
        "VAL_ElementType_InitialNode", // NOI18N
        "VAL_ElementType_JunctionState", // NOI18N
        "VAL_ElementType_Package", // NOI18N
        "VAL_ElementType_ParameterUsage", // NOI18N
        "VAL_ElementType_ShallowHistoryState", // NOI18N
        "VAL_ElementType_Signal", // NOI18N
        "VAL_ElementType_SubmachineState", // NOI18N
    };
    
    private void populateTemplatesTreeValues(boolean reset)
    {
        enableTreeModifyingButtons(DISABLE_ADD_REMOVE_BUTTONS);
        enableTemplatePropsFields(false);
        dataHandler = TemplateFamiliesHandler.getInstance(reset);
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        
        rootNode.setUserObject(new DomainTreeNode(
            org.openide.util.NbBundle.getMessage(
                DomainTemplatesManagerPanel.class, "LBL_TemplateFamilies")));
        
        DefaultTreeCellRenderer iconRenderer =
            (DefaultTreeCellRenderer)templatesTree.getCellRenderer();

        iconRenderer.setLeafIcon(DOMAIN_OBJECT_NODE_ICON);
        iconRenderer.setClosedIcon(TEMPLATE_FAMILY_NODE_ICON);
        iconRenderer.setOpenIcon(TEMPLATE_FAMILY_NODE_ICON);
        
        Family[] familyList = dataHandler.getTemplateFamilies().getFamily();
        
        int i = 0;
        int famTot = familyList.length;
        
        DefaultMutableTreeNode[] expandedNodes =
            new DefaultMutableTreeNode[famTot];
        
        for (Family family: familyList)
        {
            String familyName = family.getName();
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
                    false, 
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
    
    
    
    // called by UMLOptionsPanel
    public void store()
    {
        persistTreeExpandState();
        dataHandler.save();
    }
    
    // called by UMLOptionsPanel
    public void load()
    {
        populateTemplatesTreeValues(false);
    }
    
    public void cancel () 
    {
        dataHandler.reset();
        // populateTemplatesTreeValues(true);
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">     
    public void actionPerformed(ActionEvent event)
    {
        String cmd = event.getActionCommand();
        
        if (cmd.equals("Cancel")) // NOI18N
        {
            dataHandler.reset();
            persistTreeExpandState();
            dataHandler.save();
        }
        
        else if (cmd.equals("ADD")) // NOI18N
        {
            persistTreeExpandState();
            TemplateFamilies templateFamilies = dataHandler.getTemplateFamilies();
            Family[] familyList = templateFamilies.getFamily();
            TreePath treeSelPath = templatesTree.getSelectionPath();
            int parentRow = templatesTree.getRowForPath(treeSelPath);
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
                    DomainTemplatesManagerPanel.class, "LBL_DefaultDomainName"); // NOI18N
                
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
            persistTreeExpandState();
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
            int rowCount = templatesTable.getRowCount()-1;
            
            templatesTable.getSelectionModel()
                .setSelectionInterval(rowCount, rowCount);
            
            updateOutputParametersTable();
            enableTableModifyingButtons(true);
        }
        
        else if (cmd.equals("MODIFY_ROW")) // NOI18N
        {
            if (templatesTable.getSelectedRow() == -1)
            {
                enableTableModifyingButtons(false);
            }

            else
            {
                displayTemplatesRowPanel(false);
                templatesTable.setFocusable(true);
            }
            
            updateOutputParametersTable();
        }
        
        else if (cmd.equals("REMOVE_ROW")) // NOI18N
        {
            int selRowNum = templatesTable.getSelectedRow();

            if (selRowNum == -1)
            {
                enableTableModifyingButtons(false);
            }

            else
            {
                ((DefaultTableModel)templatesTable.getModel())
                    .removeRow(templatesTable.getSelectedRow());

                templatesTable.setFocusable(true);

                if (selRowNum > 0)
                    selRowNum--;

                if (templatesTable.getRowCount() > 0)
                {
                    templatesTable.getSelectionModel()
                        .setSelectionInterval(selRowNum, selRowNum);

                    enableTableModifyingButtons(true);
                }

                else
                    enableTableModifyingButtons(false);
            }
            
            updateOutputParametersTable();
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        outputParamsLabel = new javax.swing.JLabel();
        domainTemplatesLabel = new javax.swing.JLabel();

        templatesTree.setAutoscrolls(true);
        templatesTree.setEditable(true);
        templatesTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                templatesTreeValueChanged(evt);
            }
        });
        treeScroll.setViewportView(templatesTree);
        templatesTree.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSN_DomainTemplatesTree")); // NOI18N
        templatesTree.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSD_DomainTemplatesTree")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "BTN_Add")); // NOI18N
        addButton.setActionCommand("ADD");
        addButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        addButton.setMaximumSize(new java.awt.Dimension(133, 25));
        addButton.setMinimumSize(new java.awt.Dimension(79, 25));
        addButton.setPreferredSize(new java.awt.Dimension(79, 25));

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "BTN_Remove")); // NOI18N
        removeButton.setActionCommand("REMOVE");
        removeButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        removeButton.setMaximumSize(new java.awt.Dimension(133, 25));

        org.jdesktop.layout.GroupLayout templatesTreePanelLayout = new org.jdesktop.layout.GroupLayout(templatesTreePanel);
        templatesTreePanel.setLayout(templatesTreePanelLayout);
        templatesTreePanelLayout.setHorizontalGroup(
            templatesTreePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(templatesTreePanelLayout.createSequentialGroup()
                .add(templatesTreePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(templatesTreePanelLayout.createSequentialGroup()
                        .add(1, 1, 1)
                        .add(addButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, treeScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE))
                .addContainerGap())
        );
        templatesTreePanelLayout.setVerticalGroup(
            templatesTreePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, templatesTreePanelLayout.createSequentialGroup()
                .add(treeScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(templatesTreePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        addButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSN_AddButton")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSD_AddButton")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSN_RemoveButton")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSD_RemoveButton")); // NOI18N

        modelElementLabel.setLabelFor(modelElementCombo);
        org.openide.awt.Mnemonics.setLocalizedText(modelElementLabel, org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "LBL_Model_Element")); // NOI18N

        modelElementCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                modelElementComboItemStateChanged(evt);
            }
        });

        stereotypeLabel.setLabelFor(stereotypeText);
        org.openide.awt.Mnemonics.setLocalizedText(stereotypeLabel, org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "LBL_Stereotype")); // NOI18N

        stereotypeText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                stereotypeTextKeyReleased(evt);
            }
        });

        descriptionLabel.setLabelFor(descriptionTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "LBL_Description")); // NOI18N

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setRows(2);
        descriptionTextArea.setTabSize(4);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                descriptionTextAreaKeyReleased(evt);
            }
        });
        descriptionScroll.setViewportView(descriptionTextArea);
        descriptionTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSN_Description")); // NOI18N
        descriptionTextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSD_Description")); // NOI18N

        templatesTableScroll.setBorder(null);

        templatesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Filename Format", "Extension", "Folder Path", "Template File"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        templatesTable.setNextFocusableComponent(addRowButton);
        templatesTable.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                templatesTableFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                templatesTableFocusLost(evt);
            }
        });
        templatesTableScroll.setViewportView(templatesTable);
        templatesTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSN_OutputParameters")); // NOI18N
        templatesTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSD_OutputParameters")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addRowButton, org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "BTN_Add_Row")); // NOI18N
        addRowButton.setActionCommand("ADD_ROW");
        addRowButton.setMargin(new java.awt.Insets(2, 10, 2, 10));

        org.openide.awt.Mnemonics.setLocalizedText(modifyRowButton, org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "BTN_Modify_Row")); // NOI18N
        modifyRowButton.setActionCommand("MODIFY_ROW");
        modifyRowButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        modifyRowButton.setMaximumSize(new java.awt.Dimension(133, 25));
        modifyRowButton.setPreferredSize(new java.awt.Dimension(125, 25));

        org.openide.awt.Mnemonics.setLocalizedText(removeRowButton, org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "BTN_Remove_Row")); // NOI18N
        removeRowButton.setActionCommand("REMOVE_ROW");
        removeRowButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        removeRowButton.setMaximumSize(new java.awt.Dimension(133, 25));
        removeRowButton.setPreferredSize(new java.awt.Dimension(125, 25));

        outputParamsLabel.setLabelFor(templatesTable);
        org.openide.awt.Mnemonics.setLocalizedText(outputParamsLabel, org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "LBL_Templates")); // NOI18N

        org.jdesktop.layout.GroupLayout templatePropsPanelLayout = new org.jdesktop.layout.GroupLayout(templatePropsPanel);
        templatePropsPanel.setLayout(templatePropsPanelLayout);
        templatePropsPanelLayout.setHorizontalGroup(
            templatePropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(templatePropsPanelLayout.createSequentialGroup()
                .add(templatePropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, templatesTableScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, templatePropsPanelLayout.createSequentialGroup()
                        .add(templatePropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(stereotypeLabel)
                            .add(modelElementLabel)
                            .add(descriptionLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(templatePropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(stereotypeText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                            .add(modelElementCombo, 0, 476, Short.MAX_VALUE)
                            .add(descriptionScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)))
                    .add(outputParamsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, templatePropsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(addRowButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(modifyRowButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeRowButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)))
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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(outputParamsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(templatesTableScroll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(templatePropsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(removeRowButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(modifyRowButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(addRowButton))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        modelElementCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSN_ElementType")); // NOI18N
        modelElementCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSD_ElementType")); // NOI18N
        stereotypeText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSD_Stereotype")); // NOI18N
        stereotypeText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSD_Stereotype")); // NOI18N
        addRowButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSN_AddTemplateRow")); // NOI18N
        addRowButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSD_AddTemplateRowButton")); // NOI18N
        modifyRowButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSN_EditTemplateRowButton")); // NOI18N
        modifyRowButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSD_EditTemplateRowButton")); // NOI18N
        removeRowButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSN_RemoveTemplateRow")); // NOI18N
        removeRowButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSD_RemoveTemplateRow")); // NOI18N
        outputParamsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSD_OutputParameters")); // NOI18N

        domainTemplatesLabel.setLabelFor(templatesTree);
        org.openide.awt.Mnemonics.setLocalizedText(domainTemplatesLabel, org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "LBL_DomainObjects_PanelTitle")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(templatesTreePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(templatePropsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(domainTemplatesLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(domainTemplatesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, templatePropsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, templatesTreePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSN_TemplatesOptionsPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DomainTemplatesManagerPanel.class, "ACSD_TemplatesOptionPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void modelElementComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_modelElementComboItemStateChanged
    if (evt.getSource() != modelElementCombo || 
        modelElementCombo.getSelectedItem() == null ||
        templatesTree == null || 
        templatesTree.getSelectionPath() == null ||
        templatesTree.getSelectionPath().getPathCount() != 3)
    {
        return;
    }
    
    TemplateFamilies templateFamilies = dataHandler.getTemplateFamilies();
    
    String famName = templatesTree.getSelectionPath().getParentPath()
        .getLastPathComponent().toString();
    
    String domName = templatesTree.getSelectionPath()
        .getLastPathComponent().toString();

    templateFamilies.getFamilyByName(famName).getDomainByName(domName)
        .setModelElement(modelElementCombo.getSelectedItem().toString());
}//GEN-LAST:event_modelElementComboItemStateChanged
    
private void templatesTableFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_templatesTableFocusGained
    enableTableModifyingButtons(true);
}//GEN-LAST:event_templatesTableFocusGained

private void templatesTableFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_templatesTableFocusLost
    if (evt.getOppositeComponent() != addRowButton &&
        evt.getOppositeComponent() != modifyRowButton &&
        evt.getOppositeComponent() != removeRowButton)
    {
        templatesTable.getSelectionModel().clearSelection();
        enableTableModifyingButtons(false);
    }
}//GEN-LAST:event_templatesTableFocusLost

    private void templatesTreeValueChanged(javax.swing.event.TreeSelectionEvent evt)//GEN-FIRST:event_templatesTreeValueChanged
    {//GEN-HEADEREND:event_templatesTreeValueChanged
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
    }//GEN-LAST:event_templatesTreeValueChanged

    private void stereotypeTextKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_stereotypeTextKeyReleased
        if (evt.getSource() != stereotypeText || 
            templatesTree == null || 
            templatesTree.getSelectionPath() == null ||
            templatesTree.getSelectionPath().getPathCount() != 3)
        {
            return;
        }

        TemplateFamilies templateFamilies = dataHandler.getTemplateFamilies();

        String famName = templatesTree.getSelectionPath().getParentPath()
            .getLastPathComponent().toString();

        String domName = templatesTree.getSelectionPath()
            .getLastPathComponent().toString();

        templateFamilies.getFamilyByName(famName).getDomainByName(domName)
            .setStereotype(stereotypeText.getText());
    }//GEN-LAST:event_stereotypeTextKeyReleased

    private void descriptionTextAreaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_descriptionTextAreaKeyReleased
        if (evt.getSource() != descriptionTextArea || 
            templatesTree == null || 
            templatesTree.getSelectionPath() == null ||
            templatesTree.getSelectionPath().getPathCount() != 3)
        {
            return;
        }

        TemplateFamilies templateFamilies = dataHandler.getTemplateFamilies();

        String famName = templatesTree.getSelectionPath().getParentPath()
            .getLastPathComponent().toString();

        String domName = templatesTree.getSelectionPath()
            .getLastPathComponent().toString();

        templateFamilies.getFamilyByName(famName).getDomainByName(domName)
            .setDescription(descriptionTextArea.getText());
    }//GEN-LAST:event_descriptionTextAreaKeyReleased
    
    private void updateOutputParametersTable() 
    {
        if (templatesTree == null || 
            templatesTree.getSelectionPath() == null ||
            templatesTree.getSelectionPath().getPathCount() != 3)
        {
            return;
        }

        TemplateFamilies templateFamilies = dataHandler.getTemplateFamilies();

        String famName = templatesTree.getSelectionPath().getParentPath()
            .getLastPathComponent().toString();

        String domName = templatesTree.getSelectionPath()
            .getLastPathComponent().toString();

        templateFamilies.getFamilyByName(famName).getDomainByName(domName)
            .updateTemplates((DefaultTableModel)templatesTable.getModel());
    }                                  
    
    private void updateTreeModel(String familyName, String domainName)
    {
        TemplateFamilies templateFamilies = dataHandler.getTemplateFamilies();
        if (templateFamilies != null) 
        {
            Family family = templateFamilies.getFamilyByName(familyName);
            if (family != null) 
            {
                DomainObject domainObject = family.getDomainByName(domainName);

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
        }
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
        
        addRowButton.setEnabled(true);
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
        // templatesTable.getSelectionModel().addListSelectionListener(this);
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
    
    
    private void treeExpanded(TreeExpansionEvent event)
    {
        // if root node, no need to proceed
        if (event.getPath().getPathCount() == 1)
            return;
        
        setNodeExpandedState((((DefaultMutableTreeNode)event.getPath()
            .getLastPathComponent()).toString()), true);
    }
    
    private void treeCollapsed(TreeExpansionEvent event)
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
    
    
    private void persistTreeExpandState()
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
        
        dirtyTreeExpand = false;
    }

    
    public void treeNodesInserted(TreeModelEvent event) 
    {
    }

    public void treeNodesRemoved(TreeModelEvent event) 
    {
    }

    public void treeStructureChanged(TreeModelEvent event) 
    {
    }
    
    public void treeNodesChanged(TreeModelEvent event)
    {
        TemplateFamilies templateFamilies = dataHandler.getTemplateFamilies();
        int index = event.getChildIndices()[0];
        
        String newName = ((DefaultMutableTreeNode)event.getChildren()[0])
            .getUserObject().toString();
        
        // Root node rename - TODO: disable rename for root node
        if (event.getPath().length == 0)
        {
            ((DefaultMutableTreeNode)event.getChildren()[0])
                .setUserObject(NbBundle.getMessage(
                DomainTemplatesManagerPanel.class, "LBL_TemplateFamilies")); // NOI18N
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
    
    
    public void valueChanged(ListSelectionEvent event)
    {
        enableTableModifyingButtons(templatesTable.getSelectedRowCount() > 0);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addRowButton;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane descriptionScroll;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JLabel domainTemplatesLabel;
    private javax.swing.JComboBox modelElementCombo;
    private javax.swing.JLabel modelElementLabel;
    private javax.swing.JButton modifyRowButton;
    private javax.swing.JLabel outputParamsLabel;
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
