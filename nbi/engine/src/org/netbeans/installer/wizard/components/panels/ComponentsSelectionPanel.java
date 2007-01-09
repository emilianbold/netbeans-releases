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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.ProductRegistry;
import org.netbeans.installer.product.ProductRegistryNode;
import org.netbeans.installer.product.utils.Status;
import org.netbeans.installer.product.filters.TrueFilter;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiScrollPane;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.utils.helper.swing.NbiTree;
import org.netbeans.installer.utils.helper.swing.treetable.NbiTreeTable;
import org.netbeans.installer.wizard.components.panels.OldComponentsSelectionPanel.ComponentsStatusCellEditor;
import org.netbeans.installer.wizard.components.panels.OldComponentsSelectionPanel.ComponentsStatusCellRenderer;
import org.netbeans.installer.wizard.components.panels.OldComponentsSelectionPanel.ComponentsTreeColumnCellRenderer;
import org.netbeans.installer.wizard.components.panels.OldComponentsSelectionPanel.ComponentsTreeTableModel;
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
            ResourceUtils.getString(CLS, "CSP.title");
    public static final String DEFAULT_DESCRIPTION = 
            ResourceUtils.getString(CLS, "CSP.description");
    
    public static final String MESSAGE_TEXT_PROPERTY = 
            "message.text";
    public static final String MESSAGE_CONTENT_TYPE_PROPERTY = 
            "message.content.type";
    public static final String DISPLAY_NAME_LABEL_TEXT_PROPERTY = 
            "display.name.label.text";
    public static final String DESCRIPTION_TEXT_PROPERTY = 
            "description.text";
    public static final String DESCRIPTION_CONTENT_TYPE_PROPERTY = 
            "description.content.type";
    public static final String REQUIREMENTS_LABEL_TEXT_PROPERTY = 
            "requirements.label.text";
    public static final String CONFLICTS_LABEL_TEXT_PROPERTY = 
            "conflicts.label.text";
    public static final String TOTAL_DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY = 
            "total.download.size.label.text";
    public static final String TOTAL_DISK_SPACE_LABEL_TEXT_PROPERTY = 
            "total.disk.space.label.text";
    public static final String SIZES_LABEL_TEXT_PROPERTY = 
            "sizes.label.text"; // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE_PROPERTY = 
            "default.installation.size"; // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE_PROPERTY = 
            "default.download.size"; // NOI18N
    
    public static final String DEFAULT_MESSAGE_TEXT = 
            ResourceUtils.getString(CLS, "CSP.message.text");
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE = 
            ResourceUtils.getString(CLS, "CSP.message.content.type");
    public static final String DEFAULT_DISPLAY_NAME_LABEL_TEXT = 
            ResourceUtils.getString(CLS, "CSP.display.name.label.text");
    public static final String DEFAULT_DESCRIPTION_TEXT = 
            ResourceUtils.getString(CLS, "CSP.description.text");
    public static final String DEFAULT_DESCRIPTION_CONTENT_TYPE = 
            ResourceUtils.getString(CLS, "CSP.description.content.type");
    public static final String DEFAULT_REQUIREMENTS_LABEL_TEXT = 
            ResourceUtils.getString(CLS, "CSP.requirements.label.text");
    public static final String DEFAULT_CONFLICTS_LABEL_TEXT = 
            ResourceUtils.getString(CLS, "CSP.conflicts.label.text");
    public static final String DEFAULT_TOTAL_DOWNLOAD_SIZE_LABEL_TEXT = 
            ResourceUtils.getString(CLS, "CSP.total.download.size.label.text");
    public static final String DEFAULT_TOTAL_DISK_SPACE_LABEL_TEXT = 
            ResourceUtils.getString(CLS, "CSP.total.disk.space.label.text");
    public static final String DEFAULT_SIZES_LABEL_TEXT = 
            ResourceUtils.getString(CLS, "FSP.sizes.label.text"); // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE = 
            ResourceUtils.getString(CLS, "FSP.default.installation.size"); // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE = 
            ResourceUtils.getString(CLS, "FSP.default.download.size"); // NOI18N
    
    public static final String EMPTY_DISPLAY_NAME_LABEL_TEXT = 
            ResourceUtils.getString(CLS, "CSP.empty.display.name.label.text");
    public static final String EMPTY_DESCRIPTION_TEXT = 
            ResourceUtils.getString(CLS, "CSP.empty.description.text");
    public static final String EMPTY_REQUIREMENTS_LABEL_TEXT = 
            ResourceUtils.getString(CLS, "CSP.empty.requirements.label.text");
    public static final String EMPTY_CONFLICTS_LABEL_TEXT = 
            ResourceUtils.getString(CLS, "CSP.empty.conflicts.label.text");
    
    public static final String DEFAULT_TOTAL_DOWNLOAD_SIZE = 
            ResourceUtils.getString(CLS, "CSP.total.download.size");
    public static final String DEFAULT_TOTAL_DISK_SPACE = 
            ResourceUtils.getString(CLS, "CSP.total.disk.space");
    
    public static final String ERROR_NO_CHANGES_PROPERTY = 
            "error.no.changes";
    public static final String ERROR_REQUIREMENT_INSTALL_PROPERTY = 
            "error.requirement.install";
    public static final String ERROR_CONFLICT_INSTALL_PROPERTY = 
            "error.conflict.install";
    public static final String ERROR_REQUIREMENT_UNINSTALL_PROPERTY = 
            "error.requirement.uninstall";
    
    public static final String DEFAULT_ERROR_NO_CHANGES = 
            ResourceUtils.getString(CLS, "CSP.error.no.changes");
    public static final String DEFAULT_ERROR_REQUIREMENT_INSTALL = 
            ResourceUtils.getString(CLS, "CSP.error.requirement.install");
    public static final String DEFAULT_ERROR_CONFLICT_INSTALL = 
            ResourceUtils.getString(CLS, "CSP.error.conflict.install");
    public static final String DEFAULT_ERROR_REQUIREMENT_UNINSTALL = 
            ResourceUtils.getString(CLS, "CSP.error.requirement.uninstall");
    
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
            super.initialize();
            
            messagePane.setContentType(
                    component.getProperty(MESSAGE_CONTENT_TYPE_PROPERTY));
            messagePane.setText(
                    component.getProperty(MESSAGE_TEXT_PROPERTY));
            
            descriptionPane.setContentType(
                    component.getProperty(DESCRIPTION_CONTENT_TYPE_PROPERTY));
            
            updateDescription();
            updateSizes();
            
            updateErrorMessage();
        }
        
        public void initComponents() {
            // messagePane
            messagePane = new NbiTextPane();
            
            // componentsTree
            componentsTree = new NbiTree();
            componentsTree.setModel(new ComponentsTreeModel());
            
            // componentsTree scrollPane
            treeScrollPane = new NbiScrollPane(componentsTree);
            
            // descriptionPane
            descriptionPane = new NbiTextPane();
            
            // descriptionPane scrollPane
            descriptionScrollPane = new NbiScrollPane(descriptionPane);
            descriptionScrollPane.setVerticalScrollBarPolicy(
                    NbiScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            descriptionScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
            
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
                    0.3, 1.0,                         // weight-x, weight-y
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
        }
        
        private void updateSizes() {
            final List<Product> toInstall = ProductRegistry.getInstance().getComponentsToInstall();
            
            String installationSize = component.getProperty(DEFAULT_INSTALLATION_SIZE_PROPERTY);
            String downloadSize = component.getProperty(DEFAULT_DOWNLOAD_SIZE_PROPERTY);
            
            if (toInstall.size() > 0) {
                long installationSizeLong = 0;
                long downloadSizeLong = 0;
                
                for (Product product: toInstall) {
                    installationSizeLong += product.getRequiredDiskSpace();
                    downloadSizeLong += product.getDownloadSize();
                }
                
                installationSize = StringUtils.formatSize(installationSizeLong);
                downloadSize = StringUtils.formatSize(downloadSizeLong);
            }
            
            sizesLabel.setText(StringUtils.format(
                    component.getProperty(SIZES_LABEL_TEXT_PROPERTY)));
        }
    }
    
    public static class ComponentsTreeModel implements TreeModel {
        private Vector<TreeModelListener> listeners = 
                new Vector<TreeModelListener>();
        
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
