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

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JCheckBox;
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
import org.netbeans.installer.product.RegistryType;
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
import org.netbeans.installer.utils.helper.swing.NbiPanel;
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
    // Instance
    public ComponentsSelectionPanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        setProperty(DESCRIPTION_INSTALL_PROPERTY,
                DEFAULT_DESCRIPTION_INSTALL);
        setProperty(DESCRIPTION_UNINSTALL_PROPERTY,
                DEFAULT_DESCRIPTION_UNINSTALL);
        
        setProperty(COMPONENT_DESCRIPTION_TEXT_PROPERTY,
                DEFAULT_COMPONENT_DESCRIPTION_TEXT);
        setProperty(COMPONENT_DESCRIPTION_CONTENT_TYPE_PROPERTY,
                DEFAULT_COMPONENT_DESCRIPTION_CONTENT_TYPE);
        setProperty(SIZES_LABEL_TEXT_PROPERTY,
                DEFAULT_SIZES_LABEL_TEXT);
        setProperty(SIZES_LABEL_TEXT_NO_DOWNLOAD_PROPERTY,
                DEFAULT_SIZES_LABEL_TEXT_NO_DOWNLOAD);
        
        setProperty(DEFAULT_INSTALLATION_SIZE_PROPERTY,
                DEFAULT_INSTALLATION_SIZE);
        setProperty(DEFAULT_DOWNLOAD_SIZE_PROPERTY,
                DEFAULT_DOWNLOAD_SIZE);
        
        setProperty(ERROR_NO_CHANGES_PROPERTY,
                DEFAULT_ERROR_NO_CHANGES);
        setProperty(ERROR_NO_CHANGES_INSTALL_ONLY_PROPERTY,
                DEFAULT_ERROR_NO_CHANGES_INSTALL_ONLY);
        setProperty(ERROR_NO_CHANGES_UNINSTALL_ONLY_PROPERTY,
                DEFAULT_ERROR_NO_CHANGES_UNINSTALL_ONLY);
        setProperty(ERROR_REQUIREMENT_INSTALL_PROPERTY,
                DEFAULT_ERROR_REQUIREMENT_INSTALL);
        setProperty(ERROR_CONFLICT_INSTALL_PROPERTY,
                DEFAULT_ERROR_CONFLICT_INSTALL);
        setProperty(ERROR_REQUIREMENT_UNINSTALL_PROPERTY,
                DEFAULT_ERROR_REQUIREMENT_UNINSTALL);
    }
    
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new ComponentsSelectionPanelUi(this);
        }
        
        return wizardUi;
    }
    
    public boolean canExecuteForward() {
        return canExecute();
    }
    
    public boolean canExecuteBackward() {
        return canExecute();
    }
    
    public void initialize() {
        if (!isThereAnythingVisibleToInstall()) {
            setProperty(
                    DESCRIPTION_PROPERTY,
                    getProperty(DESCRIPTION_INSTALL_PROPERTY));
        }
        if (!isThereAnythingVisibleToUninstall()) {
            setProperty(
                    DESCRIPTION_PROPERTY,
                    getProperty(DESCRIPTION_UNINSTALL_PROPERTY));
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private boolean canExecute() {
        return System.getProperty(Registry.FORCE_UNINSTALL_PROPERTY) == null;
    }
    
    private boolean isThereAnythingVisibleToInstall() {
        final Registry registry = Registry.getInstance();
        
        final List<Product> toInstall = new LinkedList<Product>();
        toInstall.addAll(registry.getProducts(Status.NOT_INSTALLED));
        toInstall.addAll(registry.getProducts(Status.TO_BE_INSTALLED));
        
        for (Product product: toInstall) {
            if (product.isVisible()) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isThereAnythingVisibleToUninstall() {
        final Registry registry = Registry.getInstance();
        
        final List<Product> toUninstall = new LinkedList<Product>();
        toUninstall.addAll(registry.getProducts(Status.INSTALLED));
        toUninstall.addAll(registry.getProducts(Status.TO_BE_UNINSTALLED));
        
        for (Product product: toUninstall) {
            if (product.isVisible()) {
                return true;
            }
        }
        
        return false;
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
        
        private NbiTree       componentsTree;
        private NbiScrollPane treeScrollPane;
        
        private NbiTextPane   descriptionPane;
        private NbiScrollPane descriptionScrollPane;
        
        private NbiLabel      sizesLabel;
        
        public ComponentsSelectionPanelSwingUi(
                final ComponentsSelectionPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
        protected void initialize() {
            descriptionPane.setContentType(
                    component.getProperty(COMPONENT_DESCRIPTION_CONTENT_TYPE_PROPERTY));
            
            if (!component.isThereAnythingVisibleToInstall()) {
                sizesLabel.setVisible(false);
            }
            
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
                if (!component.isThereAnythingVisibleToInstall()) {
                    return component.getProperty(ERROR_NO_CHANGES_UNINSTALL_ONLY_PROPERTY);
                }
                if (!component.isThereAnythingVisibleToUninstall()) {
                    return component.getProperty(ERROR_NO_CHANGES_INSTALL_ONLY_PROPERTY);
                }
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
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // componentsTree
            componentsTree = new NbiTree();
            componentsTree.setModel(
                    new ComponentsTreeModel());
            componentsTree.setCellRenderer(
                    new ComponentsTreeCell());
            componentsTree.setCellEditor(
                    new ComponentsTreeCell());
            componentsTree.setShowsRootHandles(
                    true);
            componentsTree.setRootVisible(
                    false);
            componentsTree.setBorder(
                    new EmptyBorder(5, 5, 5, 5));
            componentsTree.setEditable(
                    true);
            componentsTree.setRowHeight(
                    new JCheckBox().getPreferredSize().height);
            componentsTree.getSelectionModel().addTreeSelectionListener(
                    new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent event) {
                    updateDescription();
                }
            });
            componentsTree.getModel().addTreeModelListener(
                    new TreeModelListener() {
                public void treeNodesChanged(TreeModelEvent event) {
                    handleEvent(event);
                }
                public void treeNodesInserted(TreeModelEvent event) {
                    handleEvent(event);
                }
                public void treeNodesRemoved(TreeModelEvent event) {
                    handleEvent(event);
                }
                public void treeStructureChanged(TreeModelEvent event) {
                    handleEvent(event);
                }
                
                private void handleEvent(TreeModelEvent event) {
                    updateSizes();
                    updateErrorMessage();
                }
            });
            
            // componentsTree scrollPane
            treeScrollPane = new NbiScrollPane(componentsTree);
            
            // descriptionPane
            descriptionPane = new NbiTextPane();
            descriptionPane.setOpaque(false);
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
            
            // add the components
            add(treeScrollPane, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    0.7, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 0),         // padding
                    0, 0));                           // padx, pady - ???
            add(descriptionScrollPane, new GridBagConstraints(
                    1, 0,                             // x, y
                    1, 1,                             // width, height
                    0.3, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 6, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(sizesLabel, new GridBagConstraints(
                    0, 1,                             // x, y
                    2, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(6, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            
            // run through all nodes and expand those that have the expand flag set
            // to true
            for (RegistryNode node: Registry.getInstance().getNodes()) {
                if (node.getExpand()) {
                    componentsTree.expandPath(node.getTreePath());
                }
            }
        }
        
        private void updateDescription() {
            final TreePath path = componentsTree.getSelectionPath();
            
            if (path != null) {
                final RegistryNode node = (RegistryNode) path.getLastPathComponent();
                descriptionPane.setText(node.getDescription());
            } else {
                descriptionPane.clearText();
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
            
            String template;
            if (Registry.getInstance().getNodes(RegistryType.REMOTE).size() == 0) {
                template = component.getProperty(SIZES_LABEL_TEXT_NO_DOWNLOAD_PROPERTY);
            } else {
                template = component.getProperty(SIZES_LABEL_TEXT_PROPERTY);
            }
            
            if (installationSize == 0) {
                sizesLabel.setText(StringUtils.format(
                        template,
                        component.getProperty(DEFAULT_INSTALLATION_SIZE_PROPERTY),
                        component.getProperty(DEFAULT_DOWNLOAD_SIZE_PROPERTY)));
            } else {
                sizesLabel.setText(StringUtils.format(
                        template,
                        StringUtils.formatSize(installationSize),
                        StringUtils.formatSize(downloadSize)));
            }
        }
    }
    
    public static class ComponentsTreeModel implements TreeModel {
        private List<TreeModelListener> listeners =
                new LinkedList<TreeModelListener>();
        
        public Object getRoot() {
            return Registry.getInstance().getRegistryRoot();
        }
        
        public Object getChild(final Object node, final int index) {
            return ((RegistryNode) node).getVisibleChildren().get(index);
        }
        
        public int getChildCount(final Object node) {
            return ((RegistryNode) node).getVisibleChildren().size();
        }
        
        public boolean isLeaf(final Object node) {
            return ((RegistryNode) node).getVisibleChildren().size() == 0;
        }
        
        public void valueForPathChanged(final TreePath path, final Object value) {
            final RegistryNode node = (RegistryNode) path.getLastPathComponent();
            
            if (node instanceof Product) {
                final Product product  = (Product) node;
                final boolean selected = (Boolean) value;
                
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
        
        public int getIndexOfChild(final Object node, final Object child) {
            return ((RegistryNode) node).getVisibleChildren().indexOf(child);
        }
        
        public void addTreeModelListener(final TreeModelListener listener) {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
        
        public void removeTreeModelListener(final TreeModelListener listener) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void fireNodeChanged(final TreePath path) {
            final TreeModelListener[] clone;
            synchronized (listeners) {
                clone = listeners.toArray(new TreeModelListener[0]);
            }
            
            final TreeModelEvent event = new TreeModelEvent(this, path);
            for (TreeModelListener listener: clone) {
                listener.treeNodesChanged(event);
            }
        }
    }
    
    public static class ComponentsTreeCell implements TreeCellRenderer, TreeCellEditor {
        private List<CellEditorListener> listeners =
                new LinkedList<CellEditorListener>();
        
        private NbiPanel    panel;
        private NbiCheckBox checkBox;
        private NbiLabel    label;
        
        private Color foreground;
        private Color background;
        
        private Color selectionForeground;
        private Color selectionBackground;
        
        public ComponentsTreeCell() {
            foreground = UIManager.getColor("Tree.textForeground");
            background = UIManager.getColor("Tree.textBackground");
            
            selectionForeground = UIManager.getColor("Tree.selectionForeground");
            selectionBackground = UIManager.getColor("Tree.selectionBackground");
            
            panel = new NbiPanel();
            panel.setLayout(new GridBagLayout());
            panel.setBackground(selectionBackground);
            
            checkBox = new NbiCheckBox();
            checkBox.setOpaque(false);
            checkBox.setFocusable(false);
            checkBox.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent event) {
                    stopCellEditing();
                }
            });
            
            label = new NbiLabel();
            label.setFocusable(false);
            label.addMouseListener(new MouseAdapter() {
                public void mouseClicked(final MouseEvent event) {
                    stopCellEditing();
                }
                public void mousePressed(final MouseEvent event) {
                    stopCellEditing();
                }
                public void mouseReleased(final MouseEvent event) {
                    stopCellEditing();
                }
            });
            
            panel.add(checkBox, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???);
            panel.add(label, new GridBagConstraints(
                    1, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(0, 0, 0, 0),           // padding
                    0, 0));                           // padx, pady - ???);
        }
        
        public Component getTreeCellRendererComponent(
                final JTree tree,
                final Object value,
                final boolean selected,
                final boolean expanded,
                final boolean leaf,
                final int row,
                final boolean focus) {
            return getComponent(
                    tree,
                    value,
                    selected,
                    expanded,
                    leaf,
                    row,
                    focus);
        }
        
        public Component getTreeCellEditorComponent(
                final JTree tree,
                final Object value,
                final boolean selected,
                final boolean expanded,
                final boolean leaf,
                final int row) {
            return getComponent(
                    tree,
                    value,
                    selected,
                    expanded,
                    leaf,
                    row,
                    true);
        }
        
        public Object getCellEditorValue() {
            return checkBox.isSelected();
        }
        
        public boolean isCellEditable(EventObject event) {
            if (event instanceof MouseEvent) {
                final MouseEvent mouseEvent = (MouseEvent) event;
                
                if (mouseEvent.getID() == MouseEvent.MOUSE_PRESSED) {
                    return true;
                }
            }
            
            if (event instanceof KeyEvent) {
                return true;
            }
            
            return false;
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
        
        public void addCellEditorListener(final CellEditorListener listener) {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
        
        public void removeCellEditorListener(final CellEditorListener listener) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void fireEditingStopped() {
            final CellEditorListener[] clone;
            synchronized (listeners) {
                clone = listeners.toArray(new CellEditorListener[0]);
            }
            
            final ChangeEvent event = new ChangeEvent(this);
            for (CellEditorListener listener: clone) {
                listener.editingStopped(event);
            }
        }
        
        private void fireEditingCanceled() {
            final CellEditorListener[] clone;
            synchronized (listeners) {
                clone = listeners.toArray(new CellEditorListener[0]);
            }
            
            final ChangeEvent event = new ChangeEvent(this);
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
                panel.setOpaque(true);
                
                checkBox.setForeground(selectionForeground);
                label.setForeground(selectionForeground);
            } else {
                panel.setOpaque(false);
                
                checkBox.setForeground(foreground);
                label.setForeground(foreground);
            }
            
            if (value instanceof Product) {
                final Product product = (Product) value;
                
                final String title = 
                        product.getDisplayName() + 
                        " [" + product.getStatus().getDisplayName() + "]";
                
                label.setText(title);
                label.setIcon(product.getIcon());
                label.setToolTipText(title);
                
                checkBox.setVisible(true);
                checkBox.setToolTipText(title);
                
                if ((product.getStatus() == Status.INSTALLED) ||
                        (product.getStatus() == Status.TO_BE_INSTALLED)) {
                    checkBox.setSelected(true);
                } else {
                    checkBox.setSelected(false);
                }
            } else if (value instanceof Group) {
                final Group group = (Group) value;
                
                label.setText(group.getDisplayName());
                label.setIcon(group.getIcon());
                label.setToolTipText(group.getDisplayName());
                
                checkBox.setVisible(false);
            }
            
            return panel;
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DESCRIPTION_INSTALL_PROPERTY =
            "description.install"; // NOI18N
    public static final String DESCRIPTION_UNINSTALL_PROPERTY =
            "description.uninstall"; // NOI18N
    
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.description.both"); // NOI18N
    public static final String DEFAULT_DESCRIPTION_INSTALL =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.description.install"); // NOI18N
    public static final String DEFAULT_DESCRIPTION_UNINSTALL =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.description.uninstall"); // NOI18N
    
    public static final String COMPONENT_DESCRIPTION_TEXT_PROPERTY =
            "component.description.text"; // NOI18N
    public static final String COMPONENT_DESCRIPTION_CONTENT_TYPE_PROPERTY =
            "component.description.content.type"; // NOI18N
    public static final String SIZES_LABEL_TEXT_PROPERTY =
            "sizes.label.text"; // NOI18N
    public static final String SIZES_LABEL_TEXT_NO_DOWNLOAD_PROPERTY =
            "sizes.label.text.no.download"; // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE_PROPERTY =
            "default.installation.size"; // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE_PROPERTY =
            "default.download.size"; // NOI18N
    
    public static final String DEFAULT_COMPONENT_DESCRIPTION_TEXT =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.component.description.text"); // NOI18N
    public static final String DEFAULT_COMPONENT_DESCRIPTION_CONTENT_TYPE =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.component.description.content.type"); // NOI18N
    public static final String DEFAULT_SIZES_LABEL_TEXT =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.sizes.label.text"); // NOI18N
    public static final String DEFAULT_SIZES_LABEL_TEXT_NO_DOWNLOAD =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.sizes.label.text.no.download"); // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.default.installation.size"); // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.default.download.size"); // NOI18N
    
    public static final String ERROR_NO_CHANGES_PROPERTY =
            "error.no.changes.both"; // NOI18N
    public static final String ERROR_NO_CHANGES_INSTALL_ONLY_PROPERTY =
            "error.no.changes.install"; // NOI18N
    public static final String ERROR_NO_CHANGES_UNINSTALL_ONLY_PROPERTY =
            "error.no.changes.uninstall"; // NOI18N
    public static final String ERROR_REQUIREMENT_INSTALL_PROPERTY =
            "error.requirement.install"; // NOI18N
    public static final String ERROR_CONFLICT_INSTALL_PROPERTY =
            "error.conflict.install"; // NOI18N
    public static final String ERROR_REQUIREMENT_UNINSTALL_PROPERTY =
            "error.requirement.uninstall"; // NOI18N
    
    public static final String DEFAULT_ERROR_NO_CHANGES =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.error.no.changes.both"); // NOI18N
    public static final String DEFAULT_ERROR_NO_CHANGES_INSTALL_ONLY =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.error.no.changes.install"); // NOI18N
    public static final String DEFAULT_ERROR_NO_CHANGES_UNINSTALL_ONLY =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.error.no.changes.uninstall"); // NOI18N
    public static final String DEFAULT_ERROR_REQUIREMENT_INSTALL =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.error.requirement.install"); // NOI18N
    public static final String DEFAULT_ERROR_CONFLICT_INSTALL =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.error.conflict.install"); // NOI18N
    public static final String DEFAULT_ERROR_REQUIREMENT_UNINSTALL =
            ResourceUtils.getString(ComponentsSelectionPanel.class,
            "CSP.error.requirement.uninstall"); // NOI18N
}
