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
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.product.ProductRegistry;
import org.netbeans.installer.product.ProductTreeNode;
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

/**
 *
 * @author Kirill Sorokin
 */
public class ComponentsSelectionPanel extends ErrorMessagePanel {
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
    
    public ComponentsSelectionPanel() {
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
        
        setProperty(DIALOG_TITLE_PROPERTY, DEFAULT_DIALOG_TITLE);
    }
    
    public void initialize() {
        final String messageContentType = getProperty(MESSAGE_CONTENT_TYPE_PROPERTY);
        messagePane.setContentType(messageContentType);
        
        final String messageText = getProperty(MESSAGE_TEXT_PROPERTY);
        messagePane.setText(messageText);
        
        final String descriptionContentType = getProperty(DESCRIPTION_CONTENT_TYPE_PROPERTY);
        descriptionPane.setContentType(descriptionContentType);
        
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
    
    public void evaluateNextButtonClick() {
        String errorMessage = validateInput();
        
        if (errorMessage == null) {
            super.evaluateNextButtonClick();
        } else {
            ErrorManager.notify(ErrorLevel.ERROR, errorMessage);
        }
    }
    
    public String validateInput() {
        List<ProductComponent> componentsToInstall = new ArrayList<ProductComponent>();
        List<ProductComponent> componentsToUninstall = new ArrayList<ProductComponent>();
        
        for (ProductComponent component: ProductRegistry.getInstance().queryComponents(new TrueFilter())) {
            if (component.getStatus() == Status.TO_BE_INSTALLED) {
                componentsToInstall.add(component);
            }
            if (component.getStatus() == Status.TO_BE_UNINSTALLED) {
                componentsToUninstall.add(component);
            }
        }
        
        if ((componentsToInstall.size() == 0) && (componentsToUninstall.size() == 0)) {
            return getProperty(ERROR_NO_CHANGES_PROPERTY);
        }
        
        for (ProductComponent component: componentsToInstall) {
            for (ProductComponent requirement: component.getRequirements()) {
                if ((requirement.getStatus() != Status.TO_BE_INSTALLED) && (requirement.getStatus() != Status.INSTALLED)) {
                    return StringUtils.format(getProperty(ERROR_REQUIREMENT_INSTALL_PROPERTY), component.getDisplayName(), requirement.getDisplayName());
                }
            }
            
            for (ProductComponent conflict: component.getConflicts()) {
                if ((conflict.getStatus() == Status.TO_BE_INSTALLED) || (conflict.getStatus() == Status.INSTALLED)) {
                    return StringUtils.format(getProperty(ERROR_CONFLICT_INSTALL_PROPERTY), component.getDisplayName(), conflict.getDisplayName());
                }
            }
        }
        
        for (ProductComponent component: componentsToUninstall) {
            for (ProductComponent dependent: ProductRegistry.getInstance().queryComponents(new TrueFilter())) {
                if (dependent.requires(component) && ((dependent.getStatus() == Status.INSTALLED) || (dependent.getStatus() == Status.TO_BE_INSTALLED))) {
                    return StringUtils.format(getProperty(ERROR_REQUIREMENT_UNINSTALL_PROPERTY), component.getDisplayName(), dependent.getDisplayName());
                }
            }
        }
        
        return null;
    }
    
    private void updateDescription() {
        int selectedRow = componentsTreeTable.getSelectedRow();
        if (selectedRow == -1) {
            displayNameLabel.setText(parseString(EMPTY_DISPLAY_NAME_LABEL_TEXT) + " ");
            displayNameLabel.setEnabled(false);
            
            descriptionPane.setText(parseString(EMPTY_DESCRIPTION_TEXT) + " ");
            descriptionPane.setEnabled(false);
            
            requirementsLabel.setText(parseString(EMPTY_REQUIREMENTS_LABEL_TEXT) + " ");
            requirementsLabel.setEnabled(false);
            
            conflictsLabel.setText(parseString(EMPTY_CONFLICTS_LABEL_TEXT) + " ");
            conflictsLabel.setEnabled(false);
        } else {
            ProductTreeNode node = (ProductTreeNode) componentsTreeTable.getModel().getValueAt(selectedRow, 0);
            
            displayNameLabel.setText(StringUtils.format(getProperty(DISPLAY_NAME_LABEL_TEXT_PROPERTY), node.getDisplayName()) + " ");
            displayNameLabel.setEnabled(true);
            
            descriptionPane.setText(StringUtils.format(getProperty(DESCRIPTION_TEXT_PROPERTY), node.getDescription()) + " ");
            descriptionPane.setEnabled(true);
            descriptionPane.setCaretPosition(0);
            
            if ((node instanceof ProductComponent) && (((ProductComponent) node).getRequirements().size() > 0)) {
                requirementsLabel.setText(StringUtils.format(getProperty(REQUIREMENTS_LABEL_TEXT_PROPERTY), StringUtils.asString(((ProductComponent) node).getRequirements())) + " ");
                requirementsLabel.setEnabled(true);
            } else {
                requirementsLabel.setText(parseString(EMPTY_REQUIREMENTS_LABEL_TEXT) + " ");
                requirementsLabel.setEnabled(false);
            }
            
            if ((node instanceof ProductComponent) && (((ProductComponent) node).getConflicts().size() > 0)) {
                conflictsLabel.setText(StringUtils.format(getProperty(CONFLICTS_LABEL_TEXT_PROPERTY), StringUtils.asString(((ProductComponent) node).getConflicts())) + " ");
                conflictsLabel.setEnabled(true);
            } else {
                conflictsLabel.setText(parseString(EMPTY_CONFLICTS_LABEL_TEXT) + " ");
                conflictsLabel.setEnabled(false);
            }
        }
    }
    
    private void updateTotalSizes() {
        List<ProductComponent> components = new ArrayList<ProductComponent>();
        
        for (ProductComponent component: ProductRegistry.getInstance().queryComponents(new TrueFilter())) {
            if (component.getStatus() == Status.TO_BE_INSTALLED) {
                components.add(component);
            }
        }
        
        if (components.size() == 0) {
            totalDownloadSizeLabel.setText(StringUtils.format(getProperty(TOTAL_DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY), parseString(DEFAULT_TOTAL_DOWNLOAD_SIZE)) + " ");
            totalDiskSpaceLabel.setText(StringUtils.format(getProperty(TOTAL_DISK_SPACE_LABEL_TEXT_PROPERTY), parseString(DEFAULT_TOTAL_DISK_SPACE)) + " ");
        } else {
            long totalDownloadSize = 0;
            long totalDiskSpace = 0;
            for (ProductComponent component: components) {
                totalDownloadSize += component.getDownloadSize();
                totalDiskSpace += component.getRequiredDiskSpace();
            }
            
            totalDownloadSizeLabel.setText(StringUtils.format(getProperty(TOTAL_DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY), StringUtils.formatSize(totalDownloadSize)) + " ");
            totalDiskSpaceLabel.setText(StringUtils.format(getProperty(TOTAL_DISK_SPACE_LABEL_TEXT_PROPERTY), StringUtils.formatSize(totalDiskSpace)) + " ");
        }
    }
    
    public static class ComponentsTreeModel implements TreeModel {
        private Vector<TreeModelListener> listeners = new Vector<TreeModelListener>();
        
        public Object getRoot() {
            return ProductRegistry.getInstance().getProductTreeRoot();
        }
        
        public Object getChild(Object parent, int index) {
            return ((ProductTreeNode) parent).getVisibleChildren().get(index);
        }
        
        public int getChildCount(Object parent) {
            return ((ProductTreeNode) parent).getVisibleChildren().size();
        }
        
        public boolean isLeaf(Object node) {
            return ((ProductTreeNode) node).getVisibleChildren().size() == 0;
        }
        
        public void valueForPathChanged(TreePath path, Object newValue) {
            // do nothing, we're read-only
        }
        
        public int getIndexOfChild(Object parent, Object child) {
            return ((ProductTreeNode) parent).getVisibleChildren().indexOf(child);
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
    
    public static class ComponentsTreeTableModel extends NbiTreeTableModel {
        public ComponentsTreeTableModel() {
            super(new ComponentsTreeModel());
        }
        
        public int getTreeColumnIndex() {
            return 0;
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public String getColumnName(int column) {
            return "";
        }
        
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0:
                    return ProductTreeNode.class;
                case 1:
                    return Status.class;
                default:
                    return null;
            }
        }
        
        public boolean isCellEditable(int row, int column) {
            switch (column) {
                case 0:
                    return false;
                case 1:
                    return true;
                default:
                    return false;
            }
        }
        
        public Object getValueAt(int row, int column) {
            ProductTreeNode node = (ProductTreeNode) getTree().getPathForRow(row).getLastPathComponent();
            
            switch (column) {
                case 0:
                    return node;
                case 1:
                    if (node instanceof ProductComponent) {
                        return ((ProductComponent) node).getStatus();
                    } else {
                        return null;
                    }
                default:
                    return null;
            }
        }
        
        public void setValueAt(Object value, int row, int column) {
            if (column == 1) {
                ProductTreeNode node = (ProductTreeNode) getTree().getPathForRow(row).getLastPathComponent();
                if (node instanceof ProductComponent) {
                    ((ProductComponent) node).setStatus((Status) value);
                }
                fireTableRowsUpdated(row, row);
            }
        }
    }
    
    public static class ComponentsTreeColumnCellRenderer extends NbiTreeColumnCellRenderer {
        public ComponentsTreeColumnCellRenderer(final NbiTreeTable treeTable) {
            super(treeTable);
        }
        
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            
            ProductTreeNode node = (ProductTreeNode) value;
            
            setIcon(node.getIcon());
            setText(node.getDisplayName());
            
            return this;
        }
    }
    
    public static class ComponentsStatusCellRenderer extends JCheckBox implements TableCellRenderer {
        private static final JLabel EMPTY = new JLabel();
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
            if (selected) {
                setOpaque(true);
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
                EMPTY.setOpaque(true);
                EMPTY.setBackground(table.getSelectionBackground());
            } else {
                setOpaque(false);
                setBackground(table.getBackground());
                setForeground(table.getForeground());
                EMPTY.setOpaque(false);
                EMPTY.setBackground(table.getBackground());
            }
            
            if (value != null) {
                Status status = (Status) value;
                
                if ((status == Status.INSTALLED) || (status == Status.TO_BE_INSTALLED)) {
                    setSelected(true);
                } else {
                    setSelected(false);
                }
                
                setText(status.getDisplayName());
                
                return this;
            } else {
                return EMPTY;
            }
        }
    }
    
    public static class ComponentsStatusCellEditor extends JCheckBox implements TableCellEditor {
        private Status status;
        
        Vector<CellEditorListener> listeners = new Vector<CellEditorListener>();
        
        public ComponentsStatusCellEditor() {
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int column) {
            if (selected) {
                setOpaque(true);
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setOpaque(false);
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            
            if (value != null) {
                status = (Status) value;
                
                if ((status == Status.INSTALLED) || (status == Status.TO_BE_INSTALLED)) {
                    setSelected(true);
                } else {
                    setSelected(false);
                }
                
                setText(status.getDisplayName());
                
                return this;
            } else {
                return null;
            }
        }
        
        public Object getCellEditorValue() {
            if (isSelected()) {
                switch (status) {
                    case NOT_INSTALLED:
                        return Status.TO_BE_INSTALLED;
                    case TO_BE_UNINSTALLED:
                        return Status.INSTALLED;
                    default:
                        return status;
                }
            } else {
                switch (status) {
                    case INSTALLED:
                        return Status.TO_BE_UNINSTALLED;
                    case TO_BE_INSTALLED:
                        return Status.NOT_INSTALLED;
                    default:
                        return status;
                }
            }
        }
        
        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }
        
        public boolean shouldSelectCell(EventObject anEvent) {
            return false;
        }
        
        public boolean stopCellEditing() {
            return true;
        }
        
        public void cancelCellEditing() {
            // do nothing
        }
        
        public void addCellEditorListener(CellEditorListener listener) {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
        
        public void removeCellEditorListener(CellEditorListener listener) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
        
        private void fireEditingCanceled() {
            synchronized (listeners) {
                CellEditorListener[] clone = listeners.toArray(new CellEditorListener[0]);
                ChangeEvent event = new ChangeEvent(this);
                
                for (CellEditorListener listener: clone) {
                    listener.editingCanceled(event);
                }
            }
        }
        
        private void fireEditingStopped() {
            synchronized (listeners) {
                CellEditorListener[] clone = listeners.toArray(new CellEditorListener[0]);
                ChangeEvent event = new ChangeEvent(this);
                
                for (CellEditorListener listener: clone) {
                    listener.editingStopped(event);
                }
            }
        }
    }
    
    public static final String MESSAGE_TEXT_PROPERTY = "message.text";
    public static final String MESSAGE_CONTENT_TYPE_PROPERTY = "message.content.type";
    public static final String DISPLAY_NAME_LABEL_TEXT_PROPERTY = "display.name.label.text";
    public static final String DESCRIPTION_TEXT_PROPERTY = "description.text";
    public static final String DESCRIPTION_CONTENT_TYPE_PROPERTY = "description.content.type";
    public static final String REQUIREMENTS_LABEL_TEXT_PROPERTY = "requirements.label.text";
    public static final String CONFLICTS_LABEL_TEXT_PROPERTY = "conflicts.label.text";
    public static final String TOTAL_DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY = "total.download.size.label.text";
    public static final String TOTAL_DISK_SPACE_LABEL_TEXT_PROPERTY = "total.disk.space.label.text";
    
    public static final String DEFAULT_MESSAGE_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.message.text");
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.message.content.type");
    public static final String DEFAULT_DISPLAY_NAME_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.display.name.label.text");
    public static final String DEFAULT_DESCRIPTION_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.description.text");
    public static final String DEFAULT_DESCRIPTION_CONTENT_TYPE = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.description.content.type");
    public static final String DEFAULT_REQUIREMENTS_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.requirements.label.text");
    public static final String DEFAULT_CONFLICTS_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.conflicts.label.text");
    public static final String DEFAULT_TOTAL_DOWNLOAD_SIZE_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.total.download.size.label.text");
    public static final String DEFAULT_TOTAL_DISK_SPACE_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.total.disk.space.label.text");
    
    public static final String EMPTY_DISPLAY_NAME_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.empty.display.name.label.text");
    public static final String EMPTY_DESCRIPTION_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.empty.description.text");
    public static final String EMPTY_REQUIREMENTS_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.empty.requirements.label.text");
    public static final String EMPTY_CONFLICTS_LABEL_TEXT = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.empty.conflicts.label.text");
    
    public static final String DEFAULT_TOTAL_DOWNLOAD_SIZE = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.total.download.size");
    public static final String DEFAULT_TOTAL_DISK_SPACE = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.total.disk.space");
    
    public static final String ERROR_NO_CHANGES_PROPERTY = "error.no.changes";
    public static final String ERROR_REQUIREMENT_INSTALL_PROPERTY = "error.requirement.install";
    public static final String ERROR_CONFLICT_INSTALL_PROPERTY = "error.conflict.install";
    public static final String ERROR_REQUIREMENT_UNINSTALL_PROPERTY = "error.requirement.uninstall";
    
    public static final String DEFAULT_ERROR_NO_CHANGES = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.error.no.changes");
    public static final String DEFAULT_ERROR_REQUIREMENT_INSTALL = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.error.requirement.install");
    public static final String DEFAULT_ERROR_CONFLICT_INSTALL = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.error.conflict.install");
    public static final String DEFAULT_ERROR_REQUIREMENT_UNINSTALL = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.error.requirement.uninstall");
    
    public static final String DEFAULT_DIALOG_TITLE = ResourceUtils.getString(ComponentsSelectionPanel.class, "ComponentsSelectionPanel.default.dialog.title");
}
