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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.List;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.ProductRegistry;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelSwingUi;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 *
 * @author Kirill Sorokin
 */
public class PreInstallSummaryPanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String MESSAGE_TEXT_PROPERTY = "message.text";
    public static final String MESSAGE_CONTENT_TYPE_PROPERTY = "message.content.type";
    public static final String COMPONENTS_TO_INSTALL_LABEL_TEXT_PROPERTY = "components.to.install.label.text";
    public static final String COMPONENTS_TO_INSTALL_TEXT_PROPERTY = "components.to.install.text";
    public static final String COMPONENTS_TO_INSTALL_CONTENT_TYPE_PROPERTY = "components.to.install.content.type";
    public static final String COMPONENTS_TO_UNINSTALL_LABEL_TEXT_PROPERTY = "components.to.uninstall.label.text";
    public static final String COMPONENTS_TO_UNINSTALL_TEXT_PROPERTY = "components.to.uninstall.text";
    public static final String COMPONENTS_TO_UNINSTALL_CONTENT_TYPE_PROPERTY = "components.to.uninstall.content.type";
    public static final String COMPONENTS_LIST_SEPARATOR_PROPERTY = "components.list.separator";
    public static final String DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY = "download.size.label.text";
    public static final String REQUIRED_DISK_SPACE_LABEL_TEXT_PROPERTY = "required.disk.space.label.text";
    
    public static final String DEFAULT_MESSAGE_TEXT = ResourceUtils.getString(PreInstallSummaryPanel.class, "PrISP.message.text");
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE = ResourceUtils.getString(PreInstallSummaryPanel.class, "PrISP.message.content.type");
    public static final String DEFAULT_COMPONENTS_TO_INSTALL_LABEL_TEXT = ResourceUtils.getString(PreInstallSummaryPanel.class, "PrISP.components.to.install.label.text");
    public static final String DEFAULT_COMPONENTS_TO_INSTALL_TEXT = ResourceUtils.getString(PreInstallSummaryPanel.class, "PrISP.components.to.install.text");
    public static final String DEFAULT_COMPONENTS_TO_INSTALL_CONTENT_TYPE = ResourceUtils.getString(PreInstallSummaryPanel.class, "PrISP.components.to.install.content.type");
    public static final String DEFAULT_COMPONENTS_TO_UNINSTALL_LABEL_TEXT = ResourceUtils.getString(PreInstallSummaryPanel.class, "PrISP.components.to.uninstall.label.text");
    public static final String DEFAULT_COMPONENTS_TO_UNINSTALL_TEXT = ResourceUtils.getString(PreInstallSummaryPanel.class, "PrISP.components.to.uninstall.text");
    public static final String DEFAULT_COMPONENTS_TO_UNINSTALL_CONTENT_TYPE = ResourceUtils.getString(PreInstallSummaryPanel.class, "PrISP.components.to.uninstall.content.type");
    public static final String DEFAULT_COMPONENTS_LIST_SEPARATOR = ResourceUtils.getString(PreInstallSummaryPanel.class, "PrISP.components.list.separator");
    public static final String DEFAULT_DOWNLOAD_SIZE_LABEL_TEXT = ResourceUtils.getString(PreInstallSummaryPanel.class, "PrISP.download.size.label.text");
    public static final String DEFAULT_REQUIRED_DISK_SPACE_LABEL_TEXT = ResourceUtils.getString(PreInstallSummaryPanel.class, "PrISP.required.disk.space.label.text");
    
    public static final String DEFAULT_DIALOG_TITLE = ResourceUtils.getString(PreInstallSummaryPanel.class, "PrISP.dialog.title");
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public PreInstallSummaryPanel() {
        setProperty(MESSAGE_TEXT_PROPERTY, DEFAULT_MESSAGE_TEXT);
        setProperty(MESSAGE_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_CONTENT_TYPE);
        setProperty(COMPONENTS_TO_INSTALL_LABEL_TEXT_PROPERTY, DEFAULT_COMPONENTS_TO_INSTALL_LABEL_TEXT);
        setProperty(COMPONENTS_TO_INSTALL_TEXT_PROPERTY, DEFAULT_COMPONENTS_TO_INSTALL_TEXT);
        setProperty(COMPONENTS_TO_INSTALL_CONTENT_TYPE_PROPERTY, DEFAULT_COMPONENTS_TO_INSTALL_CONTENT_TYPE);
        setProperty(COMPONENTS_TO_UNINSTALL_LABEL_TEXT_PROPERTY, DEFAULT_COMPONENTS_TO_UNINSTALL_LABEL_TEXT);
        setProperty(COMPONENTS_TO_UNINSTALL_TEXT_PROPERTY, DEFAULT_COMPONENTS_TO_UNINSTALL_TEXT);
        setProperty(COMPONENTS_TO_UNINSTALL_CONTENT_TYPE_PROPERTY, DEFAULT_COMPONENTS_TO_UNINSTALL_CONTENT_TYPE);
        setProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY, DEFAULT_COMPONENTS_LIST_SEPARATOR);
        setProperty(DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY, DEFAULT_DOWNLOAD_SIZE_LABEL_TEXT);
        setProperty(REQUIRED_DISK_SPACE_LABEL_TEXT_PROPERTY, DEFAULT_REQUIRED_DISK_SPACE_LABEL_TEXT);
        
        setProperty(TITLE_PROPERTY, DEFAULT_DIALOG_TITLE);
    }
    
    public boolean canExecuteForward() {
        return ProductRegistry.getInstance().getComponentsToInstall().size() +
                ProductRegistry.getInstance().getComponentsToUninstall().size() > 0;
    }
    
    public boolean canExecuteBackward() {
        return ProductRegistry.getInstance().getComponentsToInstall().size() +
                ProductRegistry.getInstance().getComponentsToUninstall().size() > 0;
    }
    
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new PreInstallSummaryPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class PreInstallSummaryPanelUi extends WizardPanelUi {
        protected PreInstallSummaryPanel        component;
        
        public PreInstallSummaryPanelUi(PreInstallSummaryPanel component) {
            super(component);
            
            this.component = component;
        }
        
        // swing ui specific ////////////////////////////////////////////////////////
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new PreInstallSummaryPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class PreInstallSummaryPanelSwingUi extends WizardPanelSwingUi {
        protected PreInstallSummaryPanel component;
        
        private NbiTextPane   messagePane;
        private NbiLabel      componentsToInstallLabel;
        private NbiTextPane   componentsToInstallPane;
        private NbiLabel      componentsToUninstallLabel;
        private NbiTextPane   componentsToUninstallPane;
        private NbiLabel      downloadSizeLabel;
        private NbiLabel      requiredDiskSpaceLabel;
        
        private NbiPanel      spacer;
        
        public PreInstallSummaryPanelSwingUi(
                final PreInstallSummaryPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
        protected void initializeContainer() {
            container.getNextButton().setText("&Install");
        }
        
        protected void initialize() {
            final String messageContentType = component.getProperty(MESSAGE_CONTENT_TYPE_PROPERTY);
            messagePane.setContentType(messageContentType);
            
            final String messageText = component.getProperty(MESSAGE_TEXT_PROPERTY);
            messagePane.setText(messageText);
            
            List<Product> componentsToInstall = ProductRegistry.getInstance().getComponentsToInstall();
            List<Product> componentsToUninstall = ProductRegistry.getInstance().getComponentsToUninstall();
            
            if (componentsToUninstall.size() > 0) {
                componentsToUninstallLabel.setVisible(true);
                componentsToUninstallPane.setVisible(true);
                
                final String componentsToUninstallLabelText = component.getProperty(COMPONENTS_TO_UNINSTALL_LABEL_TEXT_PROPERTY);
                componentsToUninstallLabel.setText(componentsToUninstallLabelText);
                
                final String componentsToUninstallContentType = component.getProperty(COMPONENTS_TO_UNINSTALL_CONTENT_TYPE_PROPERTY);
                componentsToUninstallPane.setContentType(componentsToUninstallContentType);
                
                final String componentsToUninstallText = StringUtils.format(component.getProperty(COMPONENTS_TO_UNINSTALL_TEXT_PROPERTY), StringUtils.asString(componentsToUninstall, component.getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY)));
                componentsToUninstallPane.setText(componentsToUninstallText);
            } else {
                componentsToUninstallLabel.setVisible(false);
                componentsToUninstallPane.setVisible(false);
            }
            
            if (componentsToInstall.size() > 0) {
                componentsToInstallLabel.setVisible(true);
                componentsToInstallPane.setVisible(true);
                downloadSizeLabel.setVisible(true);
                requiredDiskSpaceLabel.setVisible(true);
                
                final String componentsToInstallLabelText = component.getProperty(COMPONENTS_TO_INSTALL_LABEL_TEXT_PROPERTY);
                componentsToInstallLabel.setText(componentsToInstallLabelText);
                
                final String componentsToInstallContentType = component.getProperty(COMPONENTS_TO_INSTALL_CONTENT_TYPE_PROPERTY);
                componentsToInstallPane.setContentType(componentsToInstallContentType);
                
                final String componentsToInstallText = StringUtils.format(component.getProperty(COMPONENTS_TO_INSTALL_TEXT_PROPERTY), StringUtils.asString(componentsToInstall, component.getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY)));
                componentsToInstallPane.setText(componentsToInstallText);
                
                long downloadSize = 0;
                for (Product component: componentsToInstall) {
                    downloadSize += component.getDownloadSize();
                }
                
                long requiredDiskSpace = 0;
                for (Product component: componentsToInstall) {
                    requiredDiskSpace += component.getRequiredDiskSpace();
                }
                
                final String downloadSizeLabelText = StringUtils.format(component.getProperty(DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY), StringUtils.formatSize(downloadSize));
                downloadSizeLabel.setText(downloadSizeLabelText);
                
                final String requiredDiskSpaceLabelText = StringUtils.format(component.getProperty(REQUIRED_DISK_SPACE_LABEL_TEXT_PROPERTY), StringUtils.formatSize(requiredDiskSpace));
                requiredDiskSpaceLabel.setText(requiredDiskSpaceLabelText);
            } else {
                componentsToInstallLabel.setVisible(false);
                componentsToInstallPane.setVisible(false);
                downloadSizeLabel.setVisible(false);
                requiredDiskSpaceLabel.setVisible(false);
            }
        }
        
        private void initComponents() {
            messagePane = new NbiTextPane();
            
            componentsToUninstallLabel = new NbiLabel();
            
            componentsToUninstallPane = new NbiTextPane();
            componentsToUninstallPane.setOpaque(false);
            componentsToUninstallPane.setEditable(false);
            componentsToUninstallPane.setBorder(new EmptyBorder(0, 0, 0, 0));
            
            componentsToInstallLabel = new NbiLabel();
            
            componentsToInstallPane = new NbiTextPane();
            componentsToInstallPane.setOpaque(false);
            componentsToInstallPane.setEditable(false);
            componentsToInstallPane.setBorder(new EmptyBorder(0, 0, 0, 0));
            
            downloadSizeLabel = new NbiLabel();
            
            requiredDiskSpaceLabel = new NbiLabel();
            
            spacer = new NbiPanel();
            
            add(messagePane, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 0, 11), 0, 0));
            add(componentsToUninstallLabel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 11, 0, 11), 0, 0));
            add(componentsToUninstallPane, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
            add(componentsToInstallLabel, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 11, 0, 11), 0, 0));
            add(componentsToInstallPane, new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
            add(downloadSizeLabel, new GridBagConstraints(0, 5, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(25, 11, 0, 11), 0, 0));
            add(requiredDiskSpaceLabel, new GridBagConstraints(0, 6, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
            add(spacer, new GridBagConstraints(0, 7, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 11, 11, 11), 0, 0));
        }
    }
}
