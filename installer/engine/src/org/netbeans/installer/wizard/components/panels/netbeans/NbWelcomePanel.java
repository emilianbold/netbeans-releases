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
package org.netbeans.installer.wizard.components.panels.netbeans;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 *
 * @author Kirill Sorokin
 */
public class NbWelcomePanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public NbWelcomePanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(TEXT_PANE_CONTENT_TYPE_PROPERTY,
                DEFAULT_TEXT_PANE_CONTENT_TYPE);
        setProperty(WELCOME_TEXT_HEADER_PROPERTY,
                DEFAULT_WELCOME_TEXT_HEADER);
        setProperty(WELCOME_TEXT_GROUP_TEMPLATE_PROPERTY,
                DEFAULT_WELCOME_TEXT_GROUP_TEMPLATE);
        setProperty(WELCOME_TEXT_PRODUCT_TEMPLATE_PROPERTY,
                DEFAULT_WELCOME_TEXT_PRODUCT_TEMPLATE);
        setProperty(WELCOME_TEXT_FOOTER_PROPERTY,
                DEFAULT_WELCOME_TEXT_FOOTER);
        setProperty(CUSTOMIZE_BUTTON_TEXT_PROPERTY,
                DEFAULT_CUSTOMIZE_BUTTON_TEXT);
        setProperty(INSTALLATION_SIZE_LABEL_TEXT_PROPERTY,
                DEFAULT_INSTALLATION_SIZE_LABEL_TEXT);
        
        setProperty(CUSTOMIZE_TITLE_PROPERTY,
                DEFAULT_CUSTOMIZE_TITLE);
        
        setProperty(MESSAGE_PROPERTY,
                DEFAULT_MESSAGE);
        setProperty(MESSAGE_INSTALL_PROPERTY,
                DEFAULT_MESSAGE_INSTALL);
        setProperty(MESSAGE_UNINSTALL_PROPERTY,
                DEFAULT_MESSAGE_UNINSTALL);
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
        setProperty(OK_BUTTON_TEXT_PROPERTY,
                DEFAULT_OK_BUTTON_TEXT);
        setProperty(DEFAULT_COMPONENT_DESCRIPTION_PROPERTY, 
                DEFAULT_DEFAULT_COMPONENT_DESCRIPTION);
        
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
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new NbWelcomePanelUi(this);
        }
        
        return wizardUi;
    }
    
    @Override
    public boolean canExecuteForward() {
        return canExecute();
    }
    
    @Override
    public boolean canExecuteBackward() {
        return canExecute();
    }
    
    public boolean isThereAnythingVisibleToInstall() {
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
    
    public boolean isThereAnythingVisibleToUninstall() {
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
    
    // private //////////////////////////////////////////////////////////////////////
    private boolean canExecute() {
        final Registry registry = Registry.getInstance();
        
        return registry.getProducts(Status.NOT_INSTALLED).size() +
                registry.getProducts(Status.TO_BE_INSTALLED).size() > 0;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class NbWelcomePanelUi extends WizardPanelUi {
        protected NbWelcomePanel component;
        
        public NbWelcomePanelUi(NbWelcomePanel component) {
            super(component);
            
            this.component = component;
        }
        
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new NbWelcomePanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class NbWelcomePanelSwingUi extends WizardPanelSwingUi {
        protected NbWelcomePanel component;
        
        private NbiTextPane textPane;
        private NbiButton customizeButton;
        private NbiLabel installationSizeLabel;
        
        private NbCustomizeSelectionDialog customizeDialog;
        
        private List<RegistryNode> registryNodes;
        
        public NbWelcomePanelSwingUi(
                final NbWelcomePanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            registryNodes = new LinkedList<RegistryNode>();
            populateNodesList(
                    registryNodes,
                    Registry.getInstance().getRegistryRoot());
            
            initComponents();
        }

        @Override
        public String getTitle() {
            return null; // the welcome page does not have a title
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initialize() {
            final StringBuilder welcomeText = new StringBuilder();
            welcomeText.append(component.getProperty(WELCOME_TEXT_HEADER_PROPERTY));
            
            for (RegistryNode node: registryNodes) {
                if (node instanceof Product) {
                    if (((Product) node).getStatus() != Status.TO_BE_INSTALLED) {
                        continue;
                    }
                    
                    welcomeText.append(StringUtils.format(
                            component.getProperty(WELCOME_TEXT_PRODUCT_TEMPLATE_PROPERTY),
                            node.getDisplayName()));
                } else if (node instanceof Group) {
                    welcomeText.append(StringUtils.format(
                            component.getProperty(WELCOME_TEXT_GROUP_TEMPLATE_PROPERTY),
                            node.getDisplayName()));
                }
            }
            
            welcomeText.append(component.getProperty(WELCOME_TEXT_FOOTER_PROPERTY));
            
            textPane.setContentType(
                    component.getProperty(TEXT_PANE_CONTENT_TYPE_PROPERTY));
            textPane.setText(welcomeText);
            
            customizeButton.setText(
                    component.getProperty(CUSTOMIZE_BUTTON_TEXT_PROPERTY));
            
            updateInstallationSize();
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // textPane /////////////////////////////////////////////////////////////
            textPane = new NbiTextPane();
            
            // customizeButton //////////////////////////////////////////////////////
            customizeButton = new NbiButton();
            customizeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    customizeButtonPressed();
                }
            });
            
            // installationSizeLabel ////////////////////////////////////////////////
            installationSizeLabel = new NbiLabel();
            
            // this /////////////////////////////////////////////////////////////////
            add(textPane, new GridBagConstraints(
                    0, 0,                             // x, y
                    2, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(customizeButton, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(7, 11, 11, 0),         // padding
                    0, 0));                           // padx, pady - ???
            add(installationSizeLabel, new GridBagConstraints(
                    1, 1,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(7, 11, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
        }
        
        private void updateInstallationSize() {
            long installationSize = 0;
            for (Product product: Registry.getInstance().getProductsToInstall()) {
                installationSize += product.getRequiredDiskSpace();
            }
            
            installationSizeLabel.setText(StringUtils.format(
                    component.getProperty(INSTALLATION_SIZE_LABEL_TEXT_PROPERTY),
                    StringUtils.formatSize(installationSize)));
        }
        
        private void customizeButtonPressed() {
            if (customizeDialog == null) {
                final Runnable callback = new Runnable() {
                    public void run() {
                        initialize();
                    }
                };
                
                customizeDialog = new NbCustomizeSelectionDialog(
                        component,
                        callback,
                        registryNodes);
            }
            
            customizeDialog.setVisible(true);
            customizeDialog.requestFocus();
        }
        
        private void populateNodesList(List<RegistryNode> list, RegistryNode parent) {
            for (RegistryNode node: parent.getChildren()) {
                if (node instanceof Product) {
                    if (!((Product) node).getPlatforms().contains(
                            SystemUtils.getCurrentPlatform())) {
                        continue;
                    }
                }
                
                list.add(node);
                populateNodesList(list, node);
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.title");
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.description"); // NOI18N
    
    public static final String TEXT_PANE_CONTENT_TYPE_PROPERTY =
            "text.pane.content.type"; // NOI18N
    public static final String WELCOME_TEXT_HEADER_PROPERTY =
            "welcome.text.header"; // NOI18N
    public static final String WELCOME_TEXT_GROUP_TEMPLATE_PROPERTY =
            "welcome.text.group.template"; // NOI18N
    public static final String WELCOME_TEXT_PRODUCT_TEMPLATE_PROPERTY =
            "welcome.text.product.template"; // NOI18N
    public static final String WELCOME_TEXT_FOOTER_PROPERTY =
            "welcome.text.footer"; // NOI18N
    public static final String CUSTOMIZE_BUTTON_TEXT_PROPERTY =
            "customize.button.text"; // NOI18N
    public static final String INSTALLATION_SIZE_LABEL_TEXT_PROPERTY =
            "installation.size.label.text"; // NOI18N
    
    public static final String DEFAULT_TEXT_PANE_CONTENT_TYPE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.text.pane.content.type"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_HEADER =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.header"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_GROUP_TEMPLATE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.group.template"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_PRODUCT_TEMPLATE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.product.template"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_FOOTER =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.footer"); // NOI18N
    public static final String DEFAULT_CUSTOMIZE_BUTTON_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.customize.button.text"); // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE_LABEL_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.installation.size.label.text"); // NOI18N
    
    public static final String CUSTOMIZE_TITLE_PROPERTY =
            "customize.title"; // NOI18N
    public static final String MESSAGE_PROPERTY =
            "message"; // NOI18N
    public static final String MESSAGE_INSTALL_PROPERTY =
            "message.install"; // NOI18N
    public static final String MESSAGE_UNINSTALL_PROPERTY =
            "message.uninstall"; // NOI18N
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
    public static final String OK_BUTTON_TEXT_PROPERTY =
            "ok.button.text"; // NOI18N
    public static final String DEFAULT_COMPONENT_DESCRIPTION_PROPERTY = 
            "default.component.description";
    
    public static final String DEFAULT_CUSTOMIZE_TITLE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.customize.title"); // NOI18N
    public static final String DEFAULT_MESSAGE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.message.both"); // NOI18N
    public static final String DEFAULT_MESSAGE_INSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.message.install"); // NOI18N
    public static final String DEFAULT_MESSAGE_UNINSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.message.uninstall"); // NOI18N
    public static final String DEFAULT_COMPONENT_DESCRIPTION_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.component.description.text"); // NOI18N
    public static final String DEFAULT_COMPONENT_DESCRIPTION_CONTENT_TYPE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.component.description.content.type"); // NOI18N
    public static final String DEFAULT_SIZES_LABEL_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.sizes.label.text"); // NOI18N
    public static final String DEFAULT_SIZES_LABEL_TEXT_NO_DOWNLOAD =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.sizes.label.text.no.download"); // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.default.installation.size"); // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.default.download.size"); // NOI18N
    public static final String DEFAULT_OK_BUTTON_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.ok.button.text"); // NOI18N
    public static final String DEFAULT_DEFAULT_COMPONENT_DESCRIPTION = 
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.default.component.description"); // NOI18N
    
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
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.no.changes.both"); // NOI18N
    public static final String DEFAULT_ERROR_NO_CHANGES_INSTALL_ONLY =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.no.changes.install"); // NOI18N
    public static final String DEFAULT_ERROR_NO_CHANGES_UNINSTALL_ONLY =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.no.changes.uninstall"); // NOI18N
    public static final String DEFAULT_ERROR_REQUIREMENT_INSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.requirement.install"); // NOI18N
    public static final String DEFAULT_ERROR_CONFLICT_INSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.conflict.install"); // NOI18N
    public static final String DEFAULT_ERROR_REQUIREMENT_UNINSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.requirement.uninstall"); // NOI18N
}
