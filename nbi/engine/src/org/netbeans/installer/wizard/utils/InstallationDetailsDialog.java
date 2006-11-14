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
package org.netbeans.installer.wizard.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.product.ProductComponent.DetailedStatus;
import org.netbeans.installer.product.ProductRegistry;
import org.netbeans.installer.utils.ErrorLevel;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.helper.swing.NbiDialog;
import org.netbeans.installer.utils.helper.swing.NbiScrollPane;
import org.netbeans.installer.utils.helper.swing.treetable.NbiTreeColumnCellRenderer;
import org.netbeans.installer.utils.helper.swing.treetable.NbiTreeTable;
import org.netbeans.installer.utils.helper.swing.treetable.NbiTreeTableModel;
import org.netbeans.installer.wizard.WizardFrame;

public class InstallationDetailsDialog extends NbiDialog {
    private WizardFrame wizardFrame;
    
    private NbiTreeTable   detailsTreeTable;
    private NbiScrollPane detailsScrollPane;
    
    public InstallationDetailsDialog(WizardFrame owner) {
        super(owner);
        
        wizardFrame = owner;
        
        initComponents();
        initialize();
    }
    
    private void initialize() {
        setTitle("Installation details");
        setSize(wizardFrame.getSize());
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        
        detailsTreeTable = new NbiTreeTable(new InstallationDetailsTreeTableModel());
        detailsTreeTable.setShowVerticalLines(false);
        detailsTreeTable.setOpaque(false);
        detailsTreeTable.setTableHeader(null);
        detailsTreeTable.setRowHeight(detailsTreeTable.getRowHeight() + 4);
        detailsTreeTable.setIntercellSpacing(new Dimension(0, 0));
        detailsTreeTable.setTreeColumnCellRenderer(new InstallationDetailsTreeColumnCellRenderer(detailsTreeTable));
        detailsTreeTable.getColumnModel().getColumn(1).setMaxWidth(200);
        detailsTreeTable.getColumnModel().getColumn(1).setMinWidth(200);
        detailsTreeTable.getColumnModel().getColumn(1).setCellRenderer(new InstallationStatusCellRenderer());
        detailsTreeTable.setRowSelectionAllowed(false);
        detailsTreeTable.setColumnSelectionAllowed(false);
        detailsTreeTable.setCellSelectionEnabled(false);
        
        detailsScrollPane = new NbiScrollPane(detailsTreeTable);
        
        add(detailsScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 11, 11), 0, 0));
    }
    
    private static class InstallationDetailsTreeModel implements TreeModel {
        private List<ProductComponent> components = new ArrayList<ProductComponent>();
        private Map<ProductComponent, List<String>> propertiesMap = new HashMap<ProductComponent, List<String>>();
        
        private Object root = new Object();
        
        public InstallationDetailsTreeModel() {
            ProductRegistry registry = ProductRegistry.getInstance();
            
            components.addAll(registry.getComponentsInstalledSuccessfullyDuringThisSession());
            components.addAll(registry.getComponentsInstalledWithWarningsDuringThisSession());
            components.addAll(registry.getComponentsFailedToInstallDuringThisSession());
            
            components.addAll(registry.getComponentsUninstalledSuccessfullyDuringThisSession());
            components.addAll(registry.getComponentsUninstalledWithWarningsDuringThisSession());
            components.addAll(registry.getComponentsFailedToUninstallDuringThisSession());
        }
        
        public Object getRoot() {
            return root;
        }
        
        public Object getChild(Object parent, int index) {
            if (parent.equals(root)) {
                return components.get(index);
            } else {
                if (parent instanceof ProductComponent) {
                    initComponentProperties((ProductComponent) parent);
                    return propertiesMap.get(parent).get(index);
                } else {
                    return null;
                }
            }
        }
        
        public int getChildCount(Object parent) {
            if (parent.equals(root)) {
                return components.size();
            }
            
            if (parent instanceof ProductComponent) {
                initComponentProperties((ProductComponent) parent);
                return propertiesMap.get(parent).size();
            } else {
                return 0;
            }
        }
        
        private void initComponentProperties(ProductComponent component) {
            List<String> properties = propertiesMap.get(component);
            if (properties == null) {
                properties = new ArrayList<String>();
                
                switch (component.getDetailedStatus()) {
                    case INSTALLED_WITH_WARNINGS:
                        for (Throwable warning: component.getInstallationWarnings()) {
                            properties.add("<html><b>Warning:</b> " + warning.getMessage());
                        }
                    case INSTALLED_SUCCESSFULLY:
                        properties.add("Installation location: " + component.getInstallationLocation());
                        break;
                    case FAILED_TO_INSTALL:
                        properties.add("<html><b>Error:</b> " + component.getInstallationError().getMessage());
                        break;
                    case UNINSTALLED_WITH_WARNINGS:
                        for (Throwable warning: component.getUninstallationWarnings()) {
                            properties.add("<html><b>Warning:</b> " + warning.getMessage());
                        }
                    case UNINSTALLED_SUCCESSFULLY:
                        break;
                    case FAILED_TO_UNINSTALL:
                        properties.add("<html><b>Error:</b> " + component.getUninstallationError().getMessage());
                        break;
                    default:
                        break;
                }
                
                propertiesMap.put(component, properties);
            }
        }
        
        public boolean isLeaf(Object node) {
            return !((node.equals(root)) || (node instanceof ProductComponent));
        }
        
        public void valueForPathChanged(TreePath path, Object newValue) {
            // do nothing we are read-only
        }
        
        public int getIndexOfChild(Object parent, Object child) {
            LogManager.log(ErrorLevel.DEBUG,"getIndexOfChild");
            if (parent.equals(root)) {
                return components.indexOf(child);
            } else {
                String string = (String) child;
                if (string.startsWith("Installation Location: ")) {
                    return 0;
                }
                if (string.startsWith("Disk space:")) {
                    return 1;
                }
                return -1;
            }
        }
        
        public void addTreeModelListener(TreeModelListener listener) {
            // do nothing we are read-only
        }
        
        public void removeTreeModelListener(TreeModelListener listener) {
            // do nothing we are read-only
        }
    }
    
    private static class InstallationDetailsTreeTableModel extends NbiTreeTableModel {
        public InstallationDetailsTreeTableModel() {
            super(new InstallationDetailsTreeModel());
        }
        
        public int getTreeColumnIndex() {
            return 0;
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Component";
                case 1:
                    return "Status";
                default:
                    return null;
            }
        }
        
        public Class<?> getColumnClass(int column) {
            return Object.class;
        }
        
        public boolean isCellEditable(int row, int column) {
            return false;
        }
        
        public Object getValueAt(int row, int column) {
            Object node = getTree().getPathForRow(row).getLastPathComponent();
            
            switch (column) {
                case 0:
                    return node;
                case 1:
                    if (node instanceof ProductComponent) {
                        return ((ProductComponent) node).getDetailedStatus();
                    } else {
                        return null;
                    }
                default:
                    return null;
            }
        }
        
        public void setValueAt(Object value, int row, int column) {
            // do nothing, we're read-only
        }
    }
    
    public static class InstallationDetailsTreeColumnCellRenderer extends NbiTreeColumnCellRenderer {
        private static final EmptyBorder EMPTY_BORDER = new EmptyBorder(0, 0, 0, 0);
        private static final EmptyBorder PADDED_BORDER = new EmptyBorder(0, 0, 0, 5);
        
        public InstallationDetailsTreeColumnCellRenderer(final NbiTreeTable treeTable) {
            super(treeTable);
        }
        
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            setOpaque(false);
            setForeground(treeTable.getForeground());
            setBackground(treeTable.getBackground());
            
            if (value instanceof ProductComponent) {
                ProductComponent component = (ProductComponent) value;
                
                setIcon(component.getIcon());
                setText(component.getDisplayName());
                
                setBorder(EMPTY_BORDER);
            } else {
                setIcon(null);                
                setText((value != null) ? value.toString() : "");
                
                setBorder(PADDED_BORDER);
            }
            
            return this;
        }
    }
    
    public static class InstallationStatusCellRenderer extends JLabel implements TableCellRenderer {
        public InstallationStatusCellRenderer() {
            setBorder(new EmptyBorder(0, 5, 0, 5));
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setOpaque(false);
            setBackground(table.getBackground());
            setForeground(table.getForeground());
            setText((value instanceof DetailedStatus) ? value.toString() : "");
            
            return this;
        }
    }
}