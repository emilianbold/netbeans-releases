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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelSwingUi;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.utils.InstallationDetailsDialog;
import org.netbeans.installer.wizard.utils.InstallationLogDialog;
import static org.netbeans.installer.utils.helper.DetailedStatus.INSTALLED_SUCCESSFULLY;
import static org.netbeans.installer.utils.helper.DetailedStatus.INSTALLED_WITH_WARNINGS;
import static org.netbeans.installer.utils.helper.DetailedStatus.FAILED_TO_INSTALL;
import static org.netbeans.installer.utils.helper.DetailedStatus.UNINSTALLED_SUCCESSFULLY;
import static org.netbeans.installer.utils.helper.DetailedStatus.UNINSTALLED_WITH_WARNINGS;
import static org.netbeans.installer.utils.helper.DetailedStatus.FAILED_TO_UNINSTALL;

/**
 *
 * @author Kirill Sorokin
 */
public class PostInstallSummaryPanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public PostInstallSummaryPanel() {
        setProperty(MESSAGE_SUCCESS_TEXT_PROPERTY, DEFAULT_MESSAGE_SUCCESS_TEXT);
        setProperty(MESSAGE_SUCCESS_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_SUCCESS_CONTENT_TYPE);
        setProperty(MESSAGE_WARNINGS_TEXT_PROPERTY, DEFAULT_MESSAGE_WARNINGS_TEXT);
        setProperty(MESSAGE_WARNINGS_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_WARNINGS_CONTENT_TYPE);
        setProperty(MESSAGE_ERRORS_TEXT_PROPERTY, DEFAULT_MESSAGE_ERRORS_TEXT);
        setProperty(MESSAGE_ERRORS_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_ERRORS_CONTENT_TYPE);
        setProperty(SUCCESSFULLY_INSTALLED_COMPONENTS_LABEL_TEXT_PROPERTY, DEFAULT_SUCCESSFULLY_INSTALLED_COMPONENTS_LABEL_TEXT);
        setProperty(SUCCESSFULLY_INSTALLED_COMPONENTS_TEXT_PROPERTY, DEFAULT_SUCCESSFULLY_INSTALLED_COMPONENTS_TEXT);
        setProperty(SUCCESSFULLY_INSTALLED_COMPONENTS_CONTENT_TYPE_PROPERTY, DEFAULT_SUCCESSFULLY_INSTALLED_COMPONENTS_CONTENT_TYPE);
        setProperty(COMPONENTS_INSTALLED_WITH_WARNINGS_LABEL_TEXT_PROPERTY, DEFAULT_COMPONENTS_INSTALLED_WITH_WARNINGS_LABEL_TEXT);
        setProperty(COMPONENTS_INSTALLED_WITH_WARNINGS_TEXT_PROPERTY, DEFAULT_COMPONENTS_INSTALLED_WITH_WARNINGS_TEXT);
        setProperty(COMPONENTS_INSTALLED_WITH_WARNINGS_CONTENT_TYPE_PROPERTY, DEFAULT_COMPONENTS_INSTALLED_WITH_WARNINGS_CONTENT_TYPE);
        setProperty(COMPONENTS_FAILED_TO_INSTALL_WARNINGS_LABEL_TEXT_PROPERTY, DEFAULT_COMPONENTS_FAILED_TO_INSTALL_WARNINGS_LABEL_TEXT);
        setProperty(COMPONENTS_FAILED_TO_INSTALL_TEXT_PROPERTY, DEFAULT_COMPONENTS_FAILED_TO_INSTALL_TEXT);
        setProperty(COMPONENTS_FAILED_TO_INSTALL_CONTENT_TYPE_PROPERTY, DEFAULT_COMPONENTS_FAILED_TO_INSTALL_CONTENT_TYPE);
        setProperty(SUCCESSFULLY_UNINSTALLED_COMPONENTS_LABEL_TEXT_PROPERTY, DEFAULT_SUCCESSFULLY_UNINSTALLED_COMPONENTS_LABEL_TEXT);
        setProperty(SUCCESSFULLY_UNINSTALLED_COMPONENTS_TEXT_PROPERTY, DEFAULT_SUCCESSFULLY_UNINSTALLED_COMPONENTS_TEXT);
        setProperty(SUCCESSFULLY_UNINSTALLED_COMPONENTS_CONTENT_TYPE_PROPERTY, DEFAULT_SUCCESSFULLY_UNINSTALLED_COMPONENTS_CONTENT_TYPE);
        setProperty(COMPONENTS_UNINSTALLED_WITH_WARNINGS_LABEL_TEXT_PROPERTY, DEFAULT_COMPONENTS_UNINSTALLED_WITH_WARNINGS_LABEL_TEXT);
        setProperty(COMPONENTS_UNINSTALLED_WITH_WARNINGS_TEXT_PROPERTY, DEFAULT_COMPONENTS_UNINSTALLED_WITH_WARNINGS_TEXT);
        setProperty(COMPONENTS_UNINSTALLED_WITH_WARNINGS_CONTENT_TYPE_PROPERTY, DEFAULT_COMPONENTS_UNINSTALLED_WITH_WARNINGS_CONTENT_TYPE);
        setProperty(COMPONENTS_FAILED_TO_UNINSTALL_WARNINGS_LABEL_TEXT_PROPERTY, DEFAULT_COMPONENTS_FAILED_TO_UNINSTALL_WARNINGS_LABEL_TEXT);
        setProperty(COMPONENTS_FAILED_TO_UNINSTALL_TEXT_PROPERTY, DEFAULT_COMPONENTS_FAILED_TO_UNINSTALL_TEXT);
        setProperty(COMPONENTS_FAILED_TO_UNINSTALL_CONTENT_TYPE_PROPERTY, DEFAULT_COMPONENTS_FAILED_TO_UNINSTALL_CONTENT_TYPE);
        setProperty(VIEW_DETAILS_BUTTON_TEXT_PROPERTY, DEFAULT_VIEW_DETAILS_BUTTON_TEXT);
        setProperty(VIEW_LOG_BUTTON_TEXT_PROPERTY, DEFAULT_VIEW_LOG_BUTTON_TEXT);
        setProperty(SEND_LOG_BUTTON_TEXT_PROPERTY, DEFAULT_SEND_LOG_BUTTON_TEXT);
        setProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY, DEFAULT_COMPONENTS_LIST_SEPARATOR);
        
        setProperty(TITLE_PROPERTY, DEFAULT_DIALOG_TITLE);
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new PostInstallSummaryPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class PostInstallSummaryPanelUi extends WizardPanelUi {
        protected PostInstallSummaryPanel        component;
        
        public PostInstallSummaryPanelUi(PostInstallSummaryPanel component) {
            super(component);
            
            this.component = component;
        }
        
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new PostInstallSummaryPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class PostInstallSummaryPanelSwingUi extends WizardPanelSwingUi {
        protected PostInstallSummaryPanel component;
        
        private NbiTextPane messagePane;
        
        private NbiLabel successfullyInstalledComponentsLabel;
        private NbiTextPane successfullyInstalledComponentsPane;
        private NbiLabel componentsInstalledWithWarningsLabel;
        private NbiTextPane componentsInstalledWithWarningsPane;
        private NbiLabel componentsFailedToInstallLabel;
        private NbiTextPane componentsFailedToInstallPane;
        
        private NbiLabel successfullyUninstalledComponentsLabel;
        private NbiTextPane successfullyUninstalledComponentsPane;
        private NbiLabel componentsUninstalledWithWarningsLabel;
        private NbiTextPane componentsUninstalledWithWarningsPane;
        private NbiLabel componentsFailedToUninstallLabel;
        private NbiTextPane componentsFailedToUninstallPane;
        
        private NbiButton viewDetailsButton;
        private NbiButton viewLogButton;
        private NbiButton  sendLogButton;
        
        private NbiPanel spacer;
        
        private InstallationDetailsDialog detailsDialog;
        private InstallationLogDialog     logDialog;
        
        public PostInstallSummaryPanelSwingUi(
                final PostInstallSummaryPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initializeContainer() {
            super.initializeContainer();
            
            // set up the back button
            container.getBackButton().setEnabled(false);
            
            // set up the next (or finish) button
            container.getNextButton().setText(
                    component.getProperty(FINISH_BUTTON_TEXT_PROPERTY));
            
            // set up the cancel button
            container.getCancelButton().setEnabled(false);
        }
        
        @Override
        protected void initialize() {
            final Registry registry = Registry.getInstance();
            
            final boolean errorsEncountered =
                    registry.getProducts(FAILED_TO_INSTALL).size() > 0 &&
                    registry.getProducts(FAILED_TO_UNINSTALL).size() > 0;
            final boolean warningsEncountered =
                    registry.getProducts(INSTALLED_WITH_WARNINGS).size() > 0 &&
                    registry.getProducts(UNINSTALLED_WITH_WARNINGS).size() > 0;
            
            if (errorsEncountered) {
                messagePane.setContentType(component.getProperty(MESSAGE_ERRORS_CONTENT_TYPE_PROPERTY));
                messagePane.setText(component.getProperty(MESSAGE_ERRORS_TEXT_PROPERTY));
            } else if (warningsEncountered) {
                messagePane.setContentType(component.getProperty(MESSAGE_WARNINGS_CONTENT_TYPE_PROPERTY));
                messagePane.setText(component.getProperty(MESSAGE_WARNINGS_TEXT_PROPERTY));
            } else {
                messagePane.setContentType(component.getProperty(MESSAGE_SUCCESS_CONTENT_TYPE_PROPERTY));
                messagePane.setText(component.getProperty(MESSAGE_SUCCESS_TEXT_PROPERTY));
            }
            
            List<Product> products;
            
            products = registry.getProducts(INSTALLED_SUCCESSFULLY);
            if (products.size() > 0) {
                successfullyInstalledComponentsLabel.setVisible(true);
                successfullyInstalledComponentsPane.setVisible(true);
                
                successfullyInstalledComponentsLabel.setText(component.getProperty(SUCCESSFULLY_INSTALLED_COMPONENTS_LABEL_TEXT_PROPERTY));
                successfullyInstalledComponentsPane.setContentType(component.getProperty(SUCCESSFULLY_INSTALLED_COMPONENTS_CONTENT_TYPE_PROPERTY));
                successfullyInstalledComponentsPane.setText(StringUtils.format(component.getProperty(SUCCESSFULLY_INSTALLED_COMPONENTS_TEXT_PROPERTY), StringUtils.asString(products, component.getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
            } else {
                successfullyInstalledComponentsLabel.setVisible(false);
                successfullyInstalledComponentsPane.setVisible(false);
            }
            
            products = registry.getProducts(INSTALLED_WITH_WARNINGS);
            if (products.size() > 0) {
                componentsInstalledWithWarningsLabel.setVisible(true);
                componentsInstalledWithWarningsPane.setVisible(true);
                
                componentsInstalledWithWarningsLabel.setText(component.getProperty(COMPONENTS_INSTALLED_WITH_WARNINGS_LABEL_TEXT_PROPERTY));
                componentsInstalledWithWarningsPane.setContentType(component.getProperty(COMPONENTS_INSTALLED_WITH_WARNINGS_CONTENT_TYPE_PROPERTY));
                componentsInstalledWithWarningsPane.setText(StringUtils.format(component.getProperty(COMPONENTS_INSTALLED_WITH_WARNINGS_TEXT_PROPERTY), StringUtils.asString(products, component.getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
            } else {
                componentsInstalledWithWarningsLabel.setVisible(false);
                componentsInstalledWithWarningsPane.setVisible(false);
            }
            
            products = registry.getProducts(FAILED_TO_INSTALL);
            if (products.size() > 0) {
                componentsFailedToInstallLabel.setVisible(true);
                componentsFailedToInstallPane.setVisible(true);
                
                componentsFailedToInstallLabel.setText(component.getProperty(COMPONENTS_FAILED_TO_INSTALL_WARNINGS_LABEL_TEXT_PROPERTY));
                componentsFailedToInstallPane.setContentType(component.getProperty(COMPONENTS_FAILED_TO_INSTALL_CONTENT_TYPE_PROPERTY));
                componentsFailedToInstallPane.setText(StringUtils.format(component.getProperty(COMPONENTS_FAILED_TO_INSTALL_TEXT_PROPERTY), StringUtils.asString(products, component.getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
            } else {
                componentsFailedToInstallLabel.setVisible(false);
                componentsFailedToInstallPane.setVisible(false);
            }
            
            products = registry.getProducts(UNINSTALLED_SUCCESSFULLY);
            if (products.size() > 0) {
                successfullyUninstalledComponentsLabel.setVisible(true);
                successfullyUninstalledComponentsPane.setVisible(true);
                
                successfullyUninstalledComponentsLabel.setText(component.getProperty(SUCCESSFULLY_UNINSTALLED_COMPONENTS_LABEL_TEXT_PROPERTY));
                successfullyUninstalledComponentsPane.setContentType(component.getProperty(SUCCESSFULLY_UNINSTALLED_COMPONENTS_CONTENT_TYPE_PROPERTY));
                successfullyUninstalledComponentsPane.setText(StringUtils.format(component.getProperty(SUCCESSFULLY_UNINSTALLED_COMPONENTS_TEXT_PROPERTY), StringUtils.asString(products, component.getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
            } else {
                successfullyUninstalledComponentsLabel.setVisible(false);
                successfullyUninstalledComponentsPane.setVisible(false);
            }
            
            products = registry.getProducts(UNINSTALLED_WITH_WARNINGS);
            if (products.size() > 0) {
                componentsUninstalledWithWarningsLabel.setVisible(true);
                componentsUninstalledWithWarningsPane.setVisible(true);
                
                componentsUninstalledWithWarningsLabel.setText(component.getProperty(COMPONENTS_UNINSTALLED_WITH_WARNINGS_LABEL_TEXT_PROPERTY));
                componentsUninstalledWithWarningsPane.setContentType(component.getProperty(COMPONENTS_UNINSTALLED_WITH_WARNINGS_CONTENT_TYPE_PROPERTY));
                componentsUninstalledWithWarningsPane.setText(StringUtils.format(component.getProperty(COMPONENTS_UNINSTALLED_WITH_WARNINGS_TEXT_PROPERTY), StringUtils.asString(products, component.getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
            } else {
                componentsUninstalledWithWarningsLabel.setVisible(false);
                componentsUninstalledWithWarningsPane.setVisible(false);
            }
            
            products = registry.getProducts(FAILED_TO_UNINSTALL);
            if (products.size() > 0) {
                componentsFailedToUninstallLabel.setVisible(true);
                componentsFailedToUninstallPane.setVisible(true);
                
                componentsFailedToUninstallLabel.setText(component.getProperty(COMPONENTS_FAILED_TO_UNINSTALL_WARNINGS_LABEL_TEXT_PROPERTY));
                componentsFailedToUninstallPane.setContentType(component.getProperty(COMPONENTS_FAILED_TO_UNINSTALL_CONTENT_TYPE_PROPERTY));
                componentsFailedToUninstallPane.setText(StringUtils.format(component.getProperty(COMPONENTS_FAILED_TO_UNINSTALL_TEXT_PROPERTY), StringUtils.asString(products, component.getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
            } else {
                componentsFailedToUninstallLabel.setVisible(false);
                componentsFailedToUninstallPane.setVisible(false);
            }
            
            final String viewDetailsButtonText = component.getProperty(VIEW_DETAILS_BUTTON_TEXT_PROPERTY);
            viewDetailsButton.setText(StringUtils.stripMnemonic(viewDetailsButtonText));
            viewDetailsButton.setMnemonic(StringUtils.fetchMnemonic(viewDetailsButtonText));
            
            final String viewLogButtonText = component.getProperty(VIEW_LOG_BUTTON_TEXT_PROPERTY);
            viewLogButton.setText(StringUtils.stripMnemonic(viewLogButtonText));
            viewLogButton.setMnemonic(StringUtils.fetchMnemonic(viewLogButtonText));
            
            final String sendLogButtonText = component.getProperty(SEND_LOG_BUTTON_TEXT_PROPERTY);
            sendLogButton.setText(StringUtils.stripMnemonic(sendLogButtonText));
            sendLogButton.setMnemonic(StringUtils.fetchMnemonic(sendLogButtonText));
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // messagePane //////////////////////////////////////////////////////////
            messagePane = new NbiTextPane();
            
            // successfullyInstalledComponentsPane //////////////////////////////////
            successfullyInstalledComponentsPane = new NbiTextPane();
            
            // successfullyInstalledComponentsLabel /////////////////////////////////
            successfullyInstalledComponentsLabel = new NbiLabel();
            successfullyInstalledComponentsLabel.setLabelFor(
                    successfullyInstalledComponentsPane);
            
            // componentsInstalledWithWarningsPane //////////////////////////////////
            componentsInstalledWithWarningsPane = new NbiTextPane();
            
            // componentsInstalledWithWarningsLabel /////////////////////////////////
            componentsInstalledWithWarningsLabel = new NbiLabel();
            componentsInstalledWithWarningsLabel.setLabelFor(
                    componentsInstalledWithWarningsPane);
            
            // componentsFailedToInstallPane ////////////////////////////////////////
            componentsFailedToInstallPane = new NbiTextPane();
            
            // componentsFailedToInstallLabel ///////////////////////////////////////
            componentsFailedToInstallLabel = new NbiLabel();
            componentsFailedToInstallLabel.setLabelFor(
                    componentsFailedToInstallPane);
            
            // successfullyUninstalledComponentsPane ////////////////////////////////
            successfullyUninstalledComponentsPane = new NbiTextPane();
            
            // successfullyUninstalledComponentsLabel ///////////////////////////////
            successfullyUninstalledComponentsLabel = new NbiLabel();
            successfullyUninstalledComponentsLabel.setLabelFor(
                    successfullyUninstalledComponentsPane);
            
            // componentsUninstalledWithWarningsPane ////////////////////////////////
            componentsUninstalledWithWarningsPane = new NbiTextPane();
            
            // componentsUninstalledWithWarningsLabel ///////////////////////////////
            componentsUninstalledWithWarningsLabel = new NbiLabel();
            componentsUninstalledWithWarningsLabel.setLabelFor(
                    componentsUninstalledWithWarningsPane);
                    
            // componentsFailedToUninstallPane //////////////////////////////////////
            componentsFailedToUninstallPane = new NbiTextPane();
            
            // componentsFailedToUninstallLabel /////////////////////////////////////
            componentsFailedToUninstallLabel = new NbiLabel();
            componentsFailedToUninstallLabel.setLabelFor(
                    componentsFailedToUninstallPane);
            
            // viewDetailsButton ////////////////////////////////////////////////////
            viewDetailsButton = new NbiButton();
            viewDetailsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    viewDetailsButtonClicked();
                }
            });
            
            // viewLogButton ////////////////////////////////////////////////////////
            viewLogButton = new NbiButton();
            viewLogButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    viewLogButtonClicked();
                }
            });
            
            // sendLogButton ////////////////////////////////////////////////////////
            sendLogButton = new NbiButton();
            sendLogButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    sendLogButtonClicked();
                }
            });
            sendLogButton.setEnabled(false);
            
            // spacer ///////////////////////////////////////////////////////////////
            spacer = new NbiPanel();
            
            // this /////////////////////////////////////////////////////////////////
            add(messagePane, new GridBagConstraints(
                    0, 0,                             // x, y
                    3, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(successfullyInstalledComponentsLabel, new GridBagConstraints(
                    0, 1,                             // x, y
                    3, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(15, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(successfullyInstalledComponentsPane, new GridBagConstraints(
                    0, 2,                             // x, y
                    3, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(3, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(componentsInstalledWithWarningsLabel, new GridBagConstraints(
                    0, 3,                             // x, y
                    3, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(15, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(componentsInstalledWithWarningsPane, new GridBagConstraints(
                    0, 4,                             // x, y
                    3, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(3, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(componentsFailedToInstallLabel, new GridBagConstraints(
                    0, 5,                             // x, y
                    3, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(15, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(componentsFailedToInstallPane, new GridBagConstraints(
                    0, 6,                             // x, y
                    3, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(3, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(successfullyUninstalledComponentsLabel, new GridBagConstraints(
                    0, 7,                             // x, y
                    3, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(15, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(successfullyUninstalledComponentsPane, new GridBagConstraints(
                    0, 8,                             // x, y
                    3, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(3, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(componentsUninstalledWithWarningsLabel, new GridBagConstraints(
                    0, 9,                             // x, y
                    3, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(15, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(componentsUninstalledWithWarningsPane, new GridBagConstraints(
                    0, 10,                            // x, y
                    3, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(3, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(componentsFailedToUninstallLabel, new GridBagConstraints(
                    0, 11,                            // x, y
                    3, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(15, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(componentsFailedToUninstallPane, new GridBagConstraints(
                    0, 12,                            // x, y
                    3, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(3, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(spacer, new GridBagConstraints(
                    0, 13,                            // x, y
                    3, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(viewDetailsButton, new GridBagConstraints(
                    0, 14,                            // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(3, 11, 11, 0),         // padding
                    0, 0));                           // padx, pady - ???
            add(viewLogButton, new GridBagConstraints(
                    1, 14,                            // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(3, 6, 11, 0),          // padding
                    0, 0));                           // padx, pady - ???
            add(sendLogButton, new GridBagConstraints(
                    2, 14,                            // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.WEST,          // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(3, 6, 11, 11),         // padding
                    0, 0));                           // padx, pady - ???
        }
        
        private void viewDetailsButtonClicked() {
            if (detailsDialog == null) {
                detailsDialog = new InstallationDetailsDialog();
            }
            detailsDialog.setVisible(true);
        }
        
        private void viewLogButtonClicked() {
            if (LogManager.getLogFile() != null) {
                if (logDialog == null) {
                    logDialog = new InstallationLogDialog();
                }
                logDialog.setVisible(true);
                logDialog.loadLogFile();
            } else {
                ErrorManager.notify(ErrorLevel.ERROR, "Log file is not available.");
            }
        }
        
        private void sendLogButtonClicked() {
            // does nothing
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String MESSAGE_SUCCESS_TEXT_PROPERTY =
            "message.success.text"; // NOI18N
    public static final String MESSAGE_SUCCESS_CONTENT_TYPE_PROPERTY =
            "message.success.content.type"; // NOI18N
    public static final String MESSAGE_WARNINGS_TEXT_PROPERTY =
            "message.warnings.text"; // NOI18N
    public static final String MESSAGE_WARNINGS_CONTENT_TYPE_PROPERTY =
            "message.warnings.content.type"; // NOI18N
    public static final String MESSAGE_ERRORS_TEXT_PROPERTY =
            "message.errors.text"; // NOI18N
    public static final String MESSAGE_ERRORS_CONTENT_TYPE_PROPERTY =
            "message.errors.content.type"; // NOI18N
    public static final String SUCCESSFULLY_INSTALLED_COMPONENTS_LABEL_TEXT_PROPERTY =
            "successfully.installed.components.label.text"; // NOI18N
    public static final String SUCCESSFULLY_INSTALLED_COMPONENTS_TEXT_PROPERTY =
            "successfully.installed.components.text"; // NOI18N
    public static final String SUCCESSFULLY_INSTALLED_COMPONENTS_CONTENT_TYPE_PROPERTY =
            "successfully.installed.components.content.type"; // NOI18N
    public static final String COMPONENTS_INSTALLED_WITH_WARNINGS_LABEL_TEXT_PROPERTY =
            "components.installed.with.warnings.label.text"; // NOI18N
    public static final String COMPONENTS_INSTALLED_WITH_WARNINGS_TEXT_PROPERTY =
            "components.installed.with.warnings.text"; // NOI18N
    public static final String COMPONENTS_INSTALLED_WITH_WARNINGS_CONTENT_TYPE_PROPERTY =
            "components.installed.with.warnings.content.type"; // NOI18N
    public static final String COMPONENTS_FAILED_TO_INSTALL_WARNINGS_LABEL_TEXT_PROPERTY =
            "components.failed.to.install.label.text"; // NOI18N
    public static final String COMPONENTS_FAILED_TO_INSTALL_TEXT_PROPERTY =
            "components.failed.to.install.text"; // NOI18N
    public static final String COMPONENTS_FAILED_TO_INSTALL_CONTENT_TYPE_PROPERTY =
            "components.failed.to.install.content.type"; // NOI18N
    public static final String SUCCESSFULLY_UNINSTALLED_COMPONENTS_LABEL_TEXT_PROPERTY =
            "successfully.uninstalled.components.label.text"; // NOI18N
    public static final String SUCCESSFULLY_UNINSTALLED_COMPONENTS_TEXT_PROPERTY =
            "successfully.uninstalled.components.text"; // NOI18N
    public static final String SUCCESSFULLY_UNINSTALLED_COMPONENTS_CONTENT_TYPE_PROPERTY =
            "successfully.uninstalled.components.content.type"; // NOI18N
    public static final String COMPONENTS_UNINSTALLED_WITH_WARNINGS_LABEL_TEXT_PROPERTY =
            "components.uninstalled.with.warnings.label.text"; // NOI18N
    public static final String COMPONENTS_UNINSTALLED_WITH_WARNINGS_TEXT_PROPERTY =
            "components.uninstalled.with.warnings.text"; // NOI18N
    public static final String COMPONENTS_UNINSTALLED_WITH_WARNINGS_CONTENT_TYPE_PROPERTY =
            "components.uninstalled.with.warnings.content.type"; // NOI18N
    public static final String COMPONENTS_FAILED_TO_UNINSTALL_WARNINGS_LABEL_TEXT_PROPERTY =
            "components.failed.to.uninstall.label.text"; // NOI18N
    public static final String COMPONENTS_FAILED_TO_UNINSTALL_TEXT_PROPERTY =
            "components.failed.to.uninstall.text"; // NOI18N
    public static final String COMPONENTS_FAILED_TO_UNINSTALL_CONTENT_TYPE_PROPERTY =
            "components.failed.to.uninstall.content.type"; // NOI18N
    public static final String VIEW_DETAILS_BUTTON_TEXT_PROPERTY =
            "view.details.button.text"; // NOI18N
    public static final String VIEW_LOG_BUTTON_TEXT_PROPERTY =
            "view.log.button.text"; // NOI18N
    public static final String SEND_LOG_BUTTON_TEXT_PROPERTY =
            "send.log.button.text"; // NOI18N
    public static final String COMPONENTS_LIST_SEPARATOR_PROPERTY =
            "components.list.separator"; // NOI18N
    
    public static final String DEFAULT_MESSAGE_SUCCESS_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.success.text"); // NOI18N
    public static final String DEFAULT_MESSAGE_SUCCESS_CONTENT_TYPE =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.success.content.type"); // NOI18N
    public static final String DEFAULT_MESSAGE_WARNINGS_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.warnings.text"); // NOI18N
    public static final String DEFAULT_MESSAGE_WARNINGS_CONTENT_TYPE =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.warnings.content.type"); // NOI18N
    public static final String DEFAULT_MESSAGE_ERRORS_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.errors.text"); // NOI18N
    public static final String DEFAULT_MESSAGE_ERRORS_CONTENT_TYPE =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.message.errors.content.type"); // NOI18N
    public static final String DEFAULT_SUCCESSFULLY_INSTALLED_COMPONENTS_LABEL_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.successfully.installed.components.label.text"); // NOI18N
    public static final String DEFAULT_SUCCESSFULLY_INSTALLED_COMPONENTS_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.successfully.installed.components.text"); // NOI18N
    public static final String DEFAULT_SUCCESSFULLY_INSTALLED_COMPONENTS_CONTENT_TYPE =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.successfully.installed.components.content.type"); // NOI18N
    public static final String DEFAULT_COMPONENTS_INSTALLED_WITH_WARNINGS_LABEL_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.components.installed.with.warnings.label.text"); // NOI18N
    public static final String DEFAULT_COMPONENTS_INSTALLED_WITH_WARNINGS_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.components.installed.with.warnings.text"); // NOI18N
    public static final String DEFAULT_COMPONENTS_INSTALLED_WITH_WARNINGS_CONTENT_TYPE =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.components.installed.with.warnings.content.type"); // NOI18N
    public static final String DEFAULT_COMPONENTS_FAILED_TO_INSTALL_WARNINGS_LABEL_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.components.failed.to.install.label.text"); // NOI18N
    public static final String DEFAULT_COMPONENTS_FAILED_TO_INSTALL_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.components.failed.to.install.text"); // NOI18N
    public static final String DEFAULT_COMPONENTS_FAILED_TO_INSTALL_CONTENT_TYPE =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.components.failed.to.install.content.type"); // NOI18N
    public static final String DEFAULT_SUCCESSFULLY_UNINSTALLED_COMPONENTS_LABEL_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.successfully.uninstalled.components.label.text"); // NOI18N
    public static final String DEFAULT_SUCCESSFULLY_UNINSTALLED_COMPONENTS_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.successfully.uninstalled.components.text"); // NOI18N
    public static final String DEFAULT_SUCCESSFULLY_UNINSTALLED_COMPONENTS_CONTENT_TYPE =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.successfully.uninstalled.components.content.type"); // NOI18N
    public static final String DEFAULT_COMPONENTS_UNINSTALLED_WITH_WARNINGS_LABEL_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.components.uninstalled.with.warnings.label.text"); // NOI18N
    public static final String DEFAULT_COMPONENTS_UNINSTALLED_WITH_WARNINGS_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.components.uninstalled.with.warnings.text"); // NOI18N
    public static final String DEFAULT_COMPONENTS_UNINSTALLED_WITH_WARNINGS_CONTENT_TYPE =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.components.uninstalled.with.warnings.content.type"); // NOI18N
    public static final String DEFAULT_COMPONENTS_FAILED_TO_UNINSTALL_WARNINGS_LABEL_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.components.failed.to.uninstall.label.text"); // NOI18N
    public static final String DEFAULT_COMPONENTS_FAILED_TO_UNINSTALL_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.components.failed.to.uninstall.text"); // NOI18N
    public static final String DEFAULT_COMPONENTS_FAILED_TO_UNINSTALL_CONTENT_TYPE =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.components.failed.to.uninstall.content.type"); // NOI18N
    public static final String DEFAULT_VIEW_DETAILS_BUTTON_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.view.details.button.text"); // NOI18N
    public static final String DEFAULT_VIEW_LOG_BUTTON_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.view.log.button.text"); // NOI18N
    public static final String DEFAULT_SEND_LOG_BUTTON_TEXT =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.send.log.button.text"); // NOI18N
    public static final String DEFAULT_COMPONENTS_LIST_SEPARATOR =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.components.list.separator"); // NOI18N
    
    public static final String DEFAULT_DIALOG_TITLE =
            ResourceUtils.getString(PostInstallSummaryPanel.class,
            "PoISP.dialog.title"); // NOI18N
}
