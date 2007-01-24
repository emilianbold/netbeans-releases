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
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.DependencyType;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.swing.NbiCheckBox;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiScrollPane;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.utils.helper.swing.NbiTree;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 *
 * @author Kirill Sorokin
 */
public class ComponentsSelectionPanel extends ErrorMessagePanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final Class CLS = ComponentsSelectionPanel.class;
    
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(CLS, "CSP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(CLS, "CSP.description"); // NOI18N
    
    public static final String MESSAGE_TEXT_PROPERTY =
            "message.text"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_PROPERTY =
            "message.content.type"; // NOI18N
    public static final String DESCRIPTION_TEXT_PROPERTY =
            "description.text"; // NOI18N
    public static final String DESCRIPTION_CONTENT_TYPE_PROPERTY =
            "description.content.type"; // NOI18N
    public static final String SIZES_LABEL_TEXT_PROPERTY =
            "sizes.label.text"; // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE_PROPERTY =
            "default.installation.size"; // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE_PROPERTY =
            "default.download.size"; // NOI18N
    
    public static final String DEFAULT_MESSAGE_TEXT =
            ResourceUtils.getString(CLS, "CSP.message.text"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE =
            ResourceUtils.getString(CLS, "CSP.message.content.type"); // NOI18N
    public static final String DEFAULT_DESCRIPTION_TEXT =
            ResourceUtils.getString(CLS, "CSP.description.text"); // NOI18N
    public static final String DEFAULT_DESCRIPTION_CONTENT_TYPE =
            ResourceUtils.getString(CLS, "CSP.description.content.type"); // NOI18N
    public static final String DEFAULT_SIZES_LABEL_TEXT =
            ResourceUtils.getString(CLS, "CSP.sizes.label.text"); // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE =
            ResourceUtils.getString(CLS, "CSP.default.installation.size"); // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE =
            ResourceUtils.getString(CLS, "CSP.default.download.size"); // NOI18N
    
    public static final String ERROR_NO_CHANGES_PROPERTY =
            "error.no.changes"; // NOI18N
    public static final String ERROR_REQUIREMENT_INSTALL_PROPERTY =
            "error.requirement.install"; // NOI18N
    public static final String ERROR_CONFLICT_INSTALL_PROPERTY =
            "error.conflict.install"; // NOI18N
    public static final String ERROR_REQUIREMENT_UNINSTALL_PROPERTY =
            "error.requirement.uninstall"; // NOI18N
    
    public static final String DEFAULT_ERROR_NO_CHANGES =
            ResourceUtils.getString(CLS, "CSP.error.no.changes"); // NOI18N
    public static final String DEFAULT_ERROR_REQUIREMENT_INSTALL =
            ResourceUtils.getString(CLS, "CSP.error.requirement.install"); // NOI18N
    public static final String DEFAULT_ERROR_CONFLICT_INSTALL =
            ResourceUtils.getString(CLS, "CSP.error.conflict.install"); // NOI18N
    public static final String DEFAULT_ERROR_REQUIREMENT_UNINSTALL =
            ResourceUtils.getString(CLS, "CSP.error.requirement.uninstall"); // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ComponentsSelectionPanel() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
        
        setProperty(MESSAGE_TEXT_PROPERTY, DEFAULT_MESSAGE_TEXT);
        setProperty(MESSAGE_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_CONTENT_TYPE);
        setProperty(DESCRIPTION_TEXT_PROPERTY, DEFAULT_DESCRIPTION_TEXT);
        setProperty(DESCRIPTION_CONTENT_TYPE_PROPERTY, DEFAULT_DESCRIPTION_CONTENT_TYPE);
        setProperty(SIZES_LABEL_TEXT_PROPERTY, DEFAULT_SIZES_LABEL_TEXT);
        
        setProperty(DEFAULT_INSTALLATION_SIZE_PROPERTY, DEFAULT_INSTALLATION_SIZE);
        setProperty(DEFAULT_DOWNLOAD_SIZE_PROPERTY, DEFAULT_DOWNLOAD_SIZE);
        
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
        /////////////////////////////////////////////////////////////////////////////
        // Instance
        private ComponentsSelectionPanel component;
        
        private NbiTextPane   messagePane;
        private NbiTree       componentsTree;
        private NbiScrollPane treeScrollPane;
        
        private NbiTextPane   descriptionPane;
        private NbiScrollPane descriptionScrollPane;
        
        private NbiLabel       sizesLabel;
        
        public ComponentsSelectionPanelSwingUi(
                final ComponentsSelectionPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
        protected void initialize() {
            messagePane.setContentType(
                    component.getProperty(MESSAGE_CONTENT_TYPE_PROPERTY));
            messagePane.setText(
                    component.getProperty(MESSAGE_TEXT_PROPERTY));
            
            descriptionPane.setContentType(
                    component.getProperty(DESCRIPTION_CONTENT_TYPE_PROPERTY));
            
            updateDescription();
            updateSizes();
            
            super.initialize();
        }
        
        protected String validateInput() {
            final Registry registry = Registry.getInstance();
            
            final List<Product> toInstall   =
                    registry.getProducts(Status.TO_BE_INSTALLED);
            final List<Product> toUninstall =
                    registry.getProducts(Status.TO_BE_UNINSTALLED);
            
            if ((toInstall.size() == 0) && (toUninstall.size() == 0)) {
                return component.getProperty(ERROR_NO_CHANGES_PROPERTY);
            }
            
            for (Product product: toInstall) {
                for (Dependency requirement: product.getDependencies(DependencyType.REQUIREMENT)) {
                    List<Product> requirees = registry.getProducts(requirement);
                    
                    boolean satisfied = false;
                    
                    for (Product requiree: requirees) {
                        if ((requiree.getStatus() == Status.TO_BE_INSTALLED) ||
                                (requiree.getStatus() == Status.INSTALLED)) {
                            satisfied = true;
                            break;
                        }
                    }
                    
                    if (!satisfied) {
                        return StringUtils.format(
                                component.getProperty(ERROR_REQUIREMENT_INSTALL_PROPERTY),
                                product.getDisplayName(),
                                requirees.get(0).getDisplayName());
                    }
                }
                
                for (Dependency conflict: product.getDependencies(DependencyType.CONFLICT)) {
                    List<Product> conflictees = registry.getProducts(conflict);
                    
                    boolean satisfied = true;
                    Product unsatisfiedConflict = null;
                    
                    for (Product conflictee: conflictees) {
                        if ((conflictee.getStatus() == Status.TO_BE_INSTALLED) ||
                                (conflictee.getStatus() == Status.INSTALLED)) {
                            satisfied = false;
                            unsatisfiedConflict = conflictee;
                            break;
                        }
                    }
                    
                    if (!satisfied) {
                        return StringUtils.format(
                                component.getProperty(ERROR_CONFLICT_INSTALL_PROPERTY),
                                product.getDisplayName(),
                                unsatisfiedConflict.getDisplayName());
                    }
                }
            }
            
            for (Product product: toUninstall) {
                for (Product dependent: registry.getProducts()) {
                    if ((dependent.getStatus() == Status.TO_BE_UNINSTALLED) ||
                            (dependent.getStatus() == Status.NOT_INSTALLED)) {
                        continue;
                    }
                    
                    for (Dependency requirement: dependent.getDependencies(DependencyType.REQUIREMENT)) {
                        final List<Product> requirees = registry.getProducts(requirement);
                        
                        if (requirees.contains(product)) {
                            boolean satisfied = false;
                            for (Product requiree: requirees) {
                                if (requiree.getStatus() == Status.INSTALLED) {
                                    satisfied = true;
                                    break;
                                }
                            }
                            
                            if (!satisfied) {
                                return StringUtils.format(
                                        component.getProperty(ERROR_REQUIREMENT_UNINSTALL_PROPERTY),
                                        product.getDisplayName(),
                                        dependent.getDisplayName());
                            }
                        }
                    }
                }
            }
            
            return null;
        }
        
        private void initComponents() {
            // messagePane
            messagePane = new NbiTextPane();
            
            // componentsTree
            componentsTree = new NbiTree();
            componentsTree.setModel(
                    new ComponentsTreeModel());
            componentsTree.setCellRenderer(
                    new ComponentsTreeCellRenderer());
            componentsTree.setCellEditor(
                    new ComponentsTreeCellRenderer());
            componentsTree.setShowsRootHandles(
                    true);
            componentsTree.setRootVisible(
                    false);
            componentsTree.setBorder(
                    new EmptyBorder(5, 5, 5, 5));
            componentsTree.setEditable(
                    true);
            componentsTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent event) {
                    updateDescription();
                }
            });
            componentsTree.getModel().addTreeModelListener(new TreeModelListener() {
                public void treeNodesChanged(TreeModelEvent event) {
                    updateSizes();
                    updateErrorMessage();
                }
                public void treeNodesInserted(TreeModelEvent event) {
                    updateSizes();
                    updateErrorMessage();
                }
                public void treeNodesRemoved(TreeModelEvent event) {
                    updateSizes();
                    updateErrorMessage();
                }
                public void treeStructureChanged(TreeModelEvent event) {
                    updateSizes();
                    updateErrorMessage();
                }
            });
            
            // componentsTree scrollPane
            treeScrollPane = new NbiScrollPane(componentsTree);
            
            // descriptionPane
            descriptionPane = new NbiTextPane();
            descriptionPane.setBorder(
                    new EmptyBorder(5, 5, 5, 5));
            
            // descriptionPane scrollPane
            descriptionScrollPane = new NbiScrollPane(descriptionPane);
            descriptionScrollPane.setVerticalScrollBarPolicy(
                    NbiScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            descriptionScrollPane.setBorder(
                    new TitledBorder("Feature Description"));
            
            // sizesLabel
            sizesLabel = new NbiLabel();
            
            add(messagePane, new GridBagConstraints(
                    0, 0,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(treeScrollPane, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    0.6, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(6, 11, 0, 0),          // padding
                    0, 0));                           // padx, pady - ???
            add(descriptionScrollPane, new GridBagConstraints(
                    1, 1,                             // x, y
                    1, 1,                             // width, height
                    0.4, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(6, 6, 0, 11),          // padding
                    0, 0));                           // padx, pady - ???
            add(sizesLabel, new GridBagConstraints(
                    0, 2,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(6, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
        }
        
        private void updateDescription() {
            TreePath path = componentsTree.getSelectionPath();
            
            if (path != null) {
                RegistryNode node = (RegistryNode) path.getLastPathComponent();
                descriptionPane.setText(node.getDescription());
            } else {
                descriptionPane.setText("");
            }
            
            descriptionPane.setCaretPosition(0);
        }
        
        private void updateSizes() {
            long installationSize = 0;
            long downloadSize     = 0;
            
            for (Product product: Registry.getInstance().getProductsToInstall()) {
                installationSize += product.getRequiredDiskSpace();
                downloadSize += product.getDownloadSize();
            }
            
            if (installationSize == 0) {
                sizesLabel.setText(StringUtils.format(
                        component.getProperty(SIZES_LABEL_TEXT_PROPERTY),
                        component.getProperty(DEFAULT_INSTALLATION_SIZE_PROPERTY),
                        component.getProperty(DEFAULT_DOWNLOAD_SIZE_PROPERTY)));
            } else {
                sizesLabel.setText(StringUtils.format(
                        component.getProperty(SIZES_LABEL_TEXT_PROPERTY),
                        StringUtils.formatSize(installationSize),
                        StringUtils.formatSize(downloadSize)));
            }
        }
        
        /////////////////////////////////////////////////////////////////////////////
        // Inner Classes
        public static class ComponentsTreeModel implements TreeModel {
            private Vector<TreeModelListener> listeners =
                    new Vector<TreeModelListener>();
            
            public Object getRoot() {
                return Registry.getInstance().getRegistryRoot();
            }
            
            public Object getChild(Object node, int index) {
                return ((RegistryNode) node).getVisibleChildren().get(index);
            }
            
            public int getChildCount(Object node) {
                return ((RegistryNode) node).getVisibleChildren().size();
            }
            
            public boolean isLeaf(Object node) {
                return ((RegistryNode) node).getVisibleChildren().size() == 0;
            }
            
            public void valueForPathChanged(TreePath path, Object value) {
                RegistryNode node = (RegistryNode) path.getLastPathComponent();
                
                if (node instanceof Product) {
                    Product product = (Product) node;
                    boolean selected = (Boolean) value;
                    
                    if (selected && (product.getStatus() == Status.NOT_INSTALLED)) {
                        product.setStatus(Status.TO_BE_INSTALLED);
                    }
                    if (selected && (product.getStatus() == Status.TO_BE_UNINSTALLED)) {
                        product.setStatus(Status.INSTALLED);
                    }
                    if (!selected && (product.getStatus() == Status.INSTALLED)) {
                        product.setStatus(Status.TO_BE_UNINSTALLED);
                    }
                    if (!selected && (product.getStatus() == Status.TO_BE_INSTALLED)) {
                        product.setStatus(Status.NOT_INSTALLED);
                    }
                    
                    fireNodeChanged(path);
                }
            }
            
            public int getIndexOfChild(Object node, Object child) {
                return ((RegistryNode) node).getVisibleChildren().indexOf(child);
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
            
            private void fireNodeChanged(TreePath path) {
                TreeModelListener[] clone = listeners.toArray(new TreeModelListener[0]);
                TreeModelEvent event = new TreeModelEvent(this, path);
                
                for (TreeModelListener listener: clone) {
                    listener.treeNodesChanged(event);
                }
            }
        }
        
        public static class ComponentsTreeCellRenderer implements TreeCellRenderer, TreeCellEditor {
            private List<CellEditorListener> listeners =
                    new LinkedList<CellEditorListener>();
            
            private NbiCheckBox checkBox = new NbiCheckBox();
            private NbiLabel    label    = new NbiLabel();
            
            public ComponentsTreeCellRenderer() {
                checkBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        fireEditingStopped();
                    }
                });
            }
            
            public Component getTreeCellRendererComponent(
                    final JTree tree,
                    final Object value,
                    final boolean selected,
                    final boolean expanded,
                    final boolean leaf,
                    final int row,
                    final boolean focus) {
                return getComponent(tree, value, selected, expanded, leaf, row, focus);
            }
            
            public Component getTreeCellEditorComponent(
                    final JTree tree,
                    final Object value,
                    final boolean selected,
                    final boolean expanded,
                    final boolean leaf,
                    final int row) {
                return getComponent(tree, value, selected, expanded, leaf, row, false);
            }
            
            public Object getCellEditorValue() {
                if (checkBox.isSelected()) {
                    return true;
                } else {
                    return false;
                }
            }
            
            public boolean isCellEditable(EventObject event) {
                return true;
            }
            
            public boolean shouldSelectCell(EventObject event) {
                return true;
            }
            
            public boolean stopCellEditing() {
                fireEditingStopped();
                return true;
            }
            
            public void cancelCellEditing() {
                fireEditingCanceled();
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
            
            private void fireEditingStopped() {
                CellEditorListener[] clone = listeners.toArray(new CellEditorListener[0]);
                ChangeEvent event = new ChangeEvent(this);
                
                for (CellEditorListener listener: clone) {
                    listener.editingStopped(event);
                }
            }
            
            private void fireEditingCanceled() {
                CellEditorListener[] clone = listeners.toArray(new CellEditorListener[0]);
                ChangeEvent event = new ChangeEvent(this);
                
                for (CellEditorListener listener: clone) {
                    listener.editingCanceled(event);
                }
            }
            
            private JComponent getComponent(
                    final JTree tree,
                    final Object value,
                    final boolean selected,
                    final boolean expanded,
                    final boolean leaf,
                    final int row,
                    final boolean focus) {
                if (selected) {
                    checkBox.setOpaque(true);
                    checkBox.setForeground(UIManager.getColor("Tree.selectionForeground"));
                    checkBox.setBackground(UIManager.getColor("Tree.selectionBackground"));
                    
                    label.setOpaque(true);
                    label.setForeground(UIManager.getColor("Tree.selectionForeground"));
                    label.setBackground(UIManager.getColor("Tree.selectionBackground"));
                } else {
                    checkBox.setOpaque(false);
                    checkBox.setForeground(UIManager.getColor("Tree.textForeground"));
                    checkBox.setBackground(UIManager.getColor("Tree.textBackground"));
                    
                    label.setOpaque(false);
                    label.setForeground(UIManager.getColor("Tree.textForeground"));
                    label.setBackground(UIManager.getColor("Tree.textBackground"));
                }
                
                
                if (value instanceof Product) {
                    Product product = (Product) value;
                    
                    checkBox.setText(product.getDisplayName());
                    checkBox.setToolTipText(product.getDisplayName());
                    
                    if ((product.getStatus() == Status.INSTALLED) ||
                            (product.getStatus() == Status.TO_BE_INSTALLED)) {
                        checkBox.setSelected(true);
                    } else {
                        checkBox.setSelected(false);
                    }
                    
                    return checkBox;
                } else if (value instanceof Group) {
                    Group group = (Group) value;
                    
                    label.setText(group.getDisplayName());
                    label.setToolTipText(group.getDisplayName());
                    
                    return label;
                }
                
                return null;
            }
        }
    }
}
