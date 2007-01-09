/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.netbeans.installer.wizard.components.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.ProductRegistry;
import org.netbeans.installer.product.ProductRegistryNode;
import org.netbeans.installer.product.utils.Status;
import org.netbeans.installer.product.filters.TrueFilter;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiScrollPane;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.utils.helper.swing.treetable.NbiTreeColumnCellRenderer;
import org.netbeans.installer.utils.helper.swing.treetable.NbiTreeTable;
import org.netbeans.installer.utils.helper.swing.treetable.NbiTreeTableModel;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 *
 * @author Kirill Sorokin
 */
public class ComponentsSelectionPanel extends ErrorMessagePanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.title");
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.description");
    
    public static final String MESSAGE_TEXT_PROPERTY = "message.text";
    public static final String MESSAGE_CONTENT_TYPE_PROPERTY = "message.content.type";
    public static final String DISPLAY_NAME_LABEL_TEXT_PROPERTY = "display.name.label.text";
    public static final String DESCRIPTION_TEXT_PROPERTY = "description.text";
    public static final String DESCRIPTION_CONTENT_TYPE_PROPERTY = "description.content.type";
    public static final String REQUIREMENTS_LABEL_TEXT_PROPERTY = "requirements.label.text";
    public static final String CONFLICTS_LABEL_TEXT_PROPERTY = "conflicts.label.text";
    public static final String TOTAL_DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY = "total.download.size.label.text";
    public static final String TOTAL_DISK_SPACE_LABEL_TEXT_PROPERTY = "total.disk.space.label.text";
    
    public static final String DEFAULT_MESSAGE_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.message.text");
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.message.content.type");
    public static final String DEFAULT_DISPLAY_NAME_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.display.name.label.text");
    public static final String DEFAULT_DESCRIPTION_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.description.text");
    public static final String DEFAULT_DESCRIPTION_CONTENT_TYPE = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.description.content.type");
    public static final String DEFAULT_REQUIREMENTS_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.requirements.label.text");
    public static final String DEFAULT_CONFLICTS_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.conflicts.label.text");
    public static final String DEFAULT_TOTAL_DOWNLOAD_SIZE_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.total.download.size.label.text");
    public static final String DEFAULT_TOTAL_DISK_SPACE_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.total.disk.space.label.text");
    
    public static final String EMPTY_DISPLAY_NAME_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.empty.display.name.label.text");
    public static final String EMPTY_DESCRIPTION_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.empty.description.text");
    public static final String EMPTY_REQUIREMENTS_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.empty.requirements.label.text");
    public static final String EMPTY_CONFLICTS_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.empty.conflicts.label.text");
    
    public static final String DEFAULT_TOTAL_DOWNLOAD_SIZE = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.total.download.size");
    public static final String DEFAULT_TOTAL_DISK_SPACE = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.total.disk.space");
    
    public static final String ERROR_NO_CHANGES_PROPERTY = "error.no.changes";
    public static final String ERROR_REQUIREMENT_INSTALL_PROPERTY = "error.requirement.install";
    public static final String ERROR_CONFLICT_INSTALL_PROPERTY = "error.conflict.install";
    public static final String ERROR_REQUIREMENT_UNINSTALL_PROPERTY = "error.requirement.uninstall";
    
    public static final String DEFAULT_ERROR_NO_CHANGES  = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.error.no.changes");
    public static final String DEFAULT_ERROR_REQUIREMENT_INSTALL = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.error.requirement.install");
    public static final String DEFAULT_ERROR_CONFLICT_INSTALL = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.error.conflict.install");
    public static final String DEFAULT_ERROR_REQUIREMENT_UNINSTALL = ResourceUtils.getString(ComponentsSelectionPanel.class, "CSP.error.requirement.uninstall");
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ComponentsSelectionPanel() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
        
        setProperty(MESSAGE_TEXT_PROPERTY, DEFAULT_MESSAGE_TEXT);
        setProperty(MESSAGE_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_CONTENT_TYPE);
        setProperty(DISPLAY_NAME_LABEL_TEXT_PROPERTY, DEFAULT_DISPLAY_NAME_LABEL_TEXT);
        setProperty(DESCRIPTION_TEXT_PROPERTY, DEFAULT_DESCRIPTION_TEXT);
        setProperty(DESCRIPTION_CONTENT_TYPE_PROPERTY, DEFAULT_DESCRIPTION_CONTENT_TYPE);
        setProperty(REQUIREMENTS_LABEL_TEXT_PROPERTY, DEFAULT_REQUIREMENTS_LABEL_TEXT);
        setProperty(CONFLICTS_LABEL_TEXT_PROPERTY, DEFAULT_CONFLICTS_LABEL_TEXT);
        setProperty(TOTAL_DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY, DEFAULT_TOTAL_DOWNLOAD_SIZE_LABEL_TEXT);
        setProperty(TOTAL_DISK_SPACE_LABEL_TEXT_PROPERTY, DEFAULT_TOTAL_DISK_SPACE_LABEL_TEXT);
        
        setProperty(ERROR_NO_CHANGES_PROPERTY, DEFAULT_ERROR_NO_CHANGES);
        setProperty(ERROR_REQUIREMENT_INSTALL_PROPERTY, DEFAULT_ERROR_REQUIREMENT_INSTALL);
        setProperty(ERROR_CONFLICT_INSTALL_PROPERTY, DEFAULT_ERROR_CONFLICT_INSTALL);
        setProperty(ERROR_REQUIREMENT_UNINSTALL_PROPERTY, DEFAULT_ERROR_REQUIREMENT_UNINSTALL);
    }
    
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new ComponentsSelectionPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class ComponentsSelectionPanelUi extends ErrorMessagePanelUi {
        private ComponentsSelectionPanel component;
        
        public ComponentsSelectionPanelUi(final ComponentsSelectionPanel component) {
            super(component);
            
            this.component = component;
        }
        
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new ComponentsSelectionPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class ComponentsSelectionPanelSwingUi extends ErrorMessagePanelSwingUi {
        private ComponentsSelectionPanel component;
        
        private NbiTextPane   messagePane;
        private NbiTreeTable  componentsTreeTable;
        private NbiScrollPane treeTableScrollPane;
        
        private NbiLabel      displayNameLabel;
        private NbiTextPane   descriptionPane;
        private NbiScrollPane descriptionScrollPane;
        private NbiLabel      requirementsLabel;
        private NbiLabel      conflictsLabel;
        
        private NbiLabel      totalDownloadSizeLabel;
        private NbiLabel      totalDiskSpaceLabel;
        
        public ComponentsSelectionPanelSwingUi(
                final ComponentsSelectionPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
        protected void initialize() {
            super.initialize();
            
            messagePane.setContentType(
                    component.getProperty(MESSAGE_CONTENT_TYPE_PROPERTY));
            messagePane.setText(
                    component.getProperty(MESSAGE_TEXT_PROPERTY));
            
            descriptionPane.setContentType(
                    component.getProperty(DESCRIPTION_CONTENT_TYPE_PROPERTY));
            
            updateDescription();
            updateTotalSizes();
            
            updateErrorMessage();
        }
        
        public void initComponents() {
            messagePane = new NbiTextPane();
            
            componentsTreeTable = new NbiTreeTable(new ComponentsTreeTableModel());
            componentsTreeTable.setShowVerticalLines(false);
            componentsTreeTable.setShowHorizontalLines(true);
            componentsTreeTable.setOpaque(false);
            componentsTreeTable.setTableHeader(null);
            componentsTreeTable.setRowHeight(componentsTreeTable.getRowHeight() + 4);
            componentsTreeTable.setIntercellSpacing(new Dimension(0, 0));
            componentsTreeTable.setTreeColumnCellRenderer(new ComponentsTreeColumnCellRenderer(componentsTreeTable));
            componentsTreeTable.getColumnModel().getColumn(0).setPreferredWidth(300);
            componentsTreeTable.getColumnModel().getColumn(1).setCellRenderer(new ComponentsStatusCellRenderer());
            componentsTreeTable.getColumnModel().getColumn(1).setCellEditor(new ComponentsStatusCellEditor());
            componentsTreeTable.getModel().addTableModelListener(new TableModelListener() {
                public void tableChanged(TableModelEvent event) {
                    updateTotalSizes();
                    updateErrorMessage();
                }
            });
            componentsTreeTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent event) {
                    updateDescription();
                }
            });
            
            treeTableScrollPane = new NbiScrollPane(componentsTreeTable);
            
            displayNameLabel = new NbiLabel();
            displayNameLabel.setFont(displayNameLabel.getFont().deriveFont(Font.BOLD));
            
            descriptionPane = new NbiTextPane();
            
            descriptionScrollPane = new NbiScrollPane(descriptionPane);
            descriptionScrollPane.setVerticalScrollBarPolicy(NbiScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            descriptionScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
            
            requirementsLabel = new NbiLabel();
            
            conflictsLabel = new NbiLabel();
            
            totalDownloadSizeLabel = new NbiLabel();
            
            totalDiskSpaceLabel = new NbiLabel();
            
            add(messagePane, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 0, 11), 0, 0));
            add(treeTableScrollPane, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(7, 11, 0, 11), 0, 0));
            
            add(displayNameLabel, new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(7, 11, 0, 11), 0, 0));
            add(descriptionScrollPane, new GridBagConstraints(0, 3, 2, 1, 1.0, 0.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
            add(requirementsLabel, new GridBagConstraints(0, 4, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
            add(conflictsLabel, new GridBagConstraints(0, 5, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
            
            add(totalDownloadSizeLabel, new GridBagConstraints(0, 6, 1, 1, 0.5, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(7, 11, 0, 0), 0, 0));
            add(totalDiskSpaceLabel, new GridBagConstraints(1, 6, 1, 1, 0.5, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(7, 6, 0, 11), 0, 0));
        }
        
        public String validateInput() {
            ProductRegistry registry = ProductRegistry.getInstance();
            
            List<Product> toInstall   = new ArrayList<Product>();
            List<Product> toUninstall = new ArrayList<Product>();
            
            for (Product product: registry.getComponents()) {
                if (product.getStatus() == Status.TO_BE_INSTALLED) {
                    toInstall.add(product);
                }
                if (product.getStatus() == Status.TO_BE_UNINSTALLED) {
                    toUninstall.add(product);
                }
            }
            
            if ((toInstall.size() == 0) && (toUninstall.size() == 0)) {
                return component.getProperty(ERROR_NO_CHANGES_PROPERTY);
            }
            
            for (Product product: toInstall) {
                for (Product requirement: product.getRequirements()) {
                    if ((requirement.getStatus() != Status.TO_BE_INSTALLED) && (requirement.getStatus() != Status.INSTALLED)) {
                        return StringUtils.format(component.getProperty(ERROR_REQUIREMENT_INSTALL_PROPERTY), product.getDisplayName(), requirement.getDisplayName());
                    }
                }
                
                for (Product conflict: product.getConflicts()) {
                    if ((conflict.getStatus() == Status.TO_BE_INSTALLED) || (conflict.getStatus() == Status.INSTALLED)) {
                        return StringUtils.format(component.getProperty(ERROR_CONFLICT_INSTALL_PROPERTY), product.getDisplayName(), conflict.getDisplayName());
                    }
                }
            }
            
            for (Product product: toUninstall) {
                for (Product dependent: registry.getComponents()) {
                    if (dependent.requires(product) && 
                            ((dependent.getStatus() == Status.INSTALLED) || 
                            (dependent.getStatus() == Status.TO_BE_INSTALLED))) {
                        return StringUtils.format(
                                component.getProperty(ERROR_REQUIREMENT_UNINSTALL_PROPERTY), 
                                product.getDisplayName(), 
                                dependent.getDisplayName());
                    }
                }
            }
            
            return null;
        }
        
        private void updateDescription() {
            int selectedRow = componentsTreeTable.getSelectedRow();
            if (selectedRow == -1) {
                displayNameLabel.setText(component.parseString(EMPTY_DISPLAY_NAME_LABEL_TEXT) + " ");
                displayNameLabel.setEnabled(false);
                
                descriptionPane.setText(component.parseString(EMPTY_DESCRIPTION_TEXT) + " ");
                descriptionPane.setEnabled(false);
                
                requirementsLabel.setText(component.parseString(EMPTY_REQUIREMENTS_LABEL_TEXT) + " ");
                requirementsLabel.setEnabled(false);
                
                conflictsLabel.setText(component.parseString(EMPTY_CONFLICTS_LABEL_TEXT) + " ");
                conflictsLabel.setEnabled(false);
            } else {
                ProductRegistryNode node = (ProductRegistryNode) componentsTreeTable.getModel().getValueAt(selectedRow, 0);
                
                displayNameLabel.setText(StringUtils.format(component.getProperty(DISPLAY_NAME_LABEL_TEXT_PROPERTY), node.getDisplayName()) + " ");
                displayNameLabel.setEnabled(true);
                
                descriptionPane.setText(StringUtils.format(component.getProperty(DESCRIPTION_TEXT_PROPERTY), node.getDescription()) + " ");
                descriptionPane.setEnabled(true);
                descriptionPane.setCaretPosition(0);
                
                if ((node instanceof Product) && (((Product) node).getRequirements().size() > 0)) {
                    requirementsLabel.setText(StringUtils.format(component.getProperty(REQUIREMENTS_LABEL_TEXT_PROPERTY), StringUtils.asString(((Product) node).getRequirements())) + " ");
                    requirementsLabel.setEnabled(true);
                } else {
                    requirementsLabel.setText(component.parseString(EMPTY_REQUIREMENTS_LABEL_TEXT) + " ");
                    requirementsLabel.setEnabled(false);
                }
                
                if ((node instanceof Product) && (((Product) node).getConflicts().size() > 0)) {
                    conflictsLabel.setText(StringUtils.format(component.getProperty(CONFLICTS_LABEL_TEXT_PROPERTY), StringUtils.asString(((Product) node).getConflicts())) + " ");
                    conflictsLabel.setEnabled(true);
                } else {
                    conflictsLabel.setText(component.parseString(EMPTY_CONFLICTS_LABEL_TEXT) + " ");
                    conflictsLabel.setEnabled(false);
                }
            }
        }
        
        private void updateTotalSizes() {
            List<Product> components = new ArrayList<Product>();
            
            for (Product component: ProductRegistry.getInstance().queryComponents(new TrueFilter())) {
                if (component.getStatus() == Status.TO_BE_INSTALLED) {
                    components.add(component);
                }
            }
            
            if (components.size() == 0) {
                totalDownloadSizeLabel.setText(StringUtils.format(component.getProperty(TOTAL_DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY), component.parseString(DEFAULT_TOTAL_DOWNLOAD_SIZE)) + " ");
                totalDiskSpaceLabel.setText(StringUtils.format(component.getProperty(TOTAL_DISK_SPACE_LABEL_TEXT_PROPERTY), component.parseString(DEFAULT_TOTAL_DISK_SPACE)) + " ");
            } else {
                long totalDownloadSize = 0;
                long totalDiskSpace = 0;
                for (Product component: components) {
                    totalDownloadSize += component.getDownloadSize();
                    totalDiskSpace += component.getRequiredDiskSpace();
                }
                
                totalDownloadSizeLabel.setText(StringUtils.format(component.getProperty(TOTAL_DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY), StringUtils.formatSize(totalDownloadSize)) + " ");
                totalDiskSpaceLabel.setText(StringUtils.format(component.getProperty(TOTAL_DISK_SPACE_LABEL_TEXT_PROPERTY), StringUtils.formatSize(totalDiskSpace)) + " ");
            }
        }
    }
    
    public static class ComponentsTreeModel implements TreeModel {
        private Vector<TreeModelListener> listeners = new Vector<TreeModelListener>();
        
        public Object getRoot() {
            return ProductRegistry.getInstance().getProductTreeRoot();
        }
        
        public Object getChild(Object parent, int index) {
            return ((ProductRegistryNode) parent).getVisibleChildren().get(index);
        }
        
        public int getChildCount(Object parent) {
            return ((ProductRegistryNode) parent).getVisibleChildren().size();
        }
        
        public boolean isLeaf(Object node) {
            return ((ProductRegistryNode) node).getVisibleChildren().size() == 0;
        }
        
        public void valueForPathChanged(TreePath path, Object newValue) {
            // do nothing, we're read-only
        }
        
        public int getIndexOfChild(Object parent, Object child) {
            return ((ProductRegistryNode) parent).getVisibleChildren().indexOf(child);
        }
        
        public void addTreeModelListener(TreeModelListener listener) {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
        
        public void removeTreeModelListener(TreeModelListener listener) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
    }
}
