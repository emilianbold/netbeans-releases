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
import org.netbeans.installer.wizard.containers.SwingContainer;
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
public class PostCreateBundleSummaryPanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String MESSAGE_SUCCESS_TEXT_PROPERTY = "message.success.text";
    public static final String MESSAGE_SUCCESS_CONTENT_TYPE_PROPERTY = "message.success.content.type";
    public static final String MESSAGE_ERRORS_TEXT_PROPERTY = "message.errors.text";
    public static final String MESSAGE_ERRORS_CONTENT_TYPE_PROPERTY = "message.errors.content.type";
    public static final String SUCCESSFULLY_BUNDLED_COMPONENTS_LABEL_TEXT_PROPERTY = "successfully.bundled.components.label.text";
    public static final String SUCCESSFULLY_BUNDLED_COMPONENTS_TEXT_PROPERTY = "successfully.bundled.components.text";
    public static final String SUCCESSFULLY_BUNDLED_COMPONENTS_CONTENT_TYPE_PROPERTY = "successfully.bundled.components.content.type";
    public static final String COMPONENTS_FAILED_TO_BUNDLE_LABEL_TEXT_PROPERTY = "components.failed.to.bundle.label.text";
    public static final String COMPONENTS_FAILED_TO_BUNDLE_TEXT_PROPERTY = "components.failed.to.bundle.text";
    public static final String COMPONENTS_FAILED_TO_BUNDLE_CONTENT_TYPE_PROPERTY = "components.failed.to.bundle.content.type";
    public static final String VIEW_LOG_BUTTON_TEXT_PROPERTY = "view.log.button.text";
    public static final String SEND_LOG_BUTTON_TEXT_PROPERTY = "send.log.button.text";
    public static final String COMPONENTS_LIST_SEPARATOR_PROPERTY = "components.list.separator";
    
    public static final String DEFAULT_MESSAGE_SUCCESS_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PoCBSP.message.success.text");
    public static final String DEFAULT_MESSAGE_SUCCESS_CONTENT_TYPE = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PoCBSP.message.success.content.type");
    public static final String DEFAULT_MESSAGE_ERRORS_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PoCBSP.message.errors.text");
    public static final String DEFAULT_MESSAGE_ERRORS_CONTENT_TYPE = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PoCBSP.message.errors.content.type");
    public static final String DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_LABEL_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PoCBSP.successfully.bundled.components.label.text");
    public static final String DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PoCBSP.successfully.bundled.components.text");
    public static final String DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_CONTENT_TYPE = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PoCBSP.successfully.bundled.components.content.type");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_LABEL_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PoCBSP.components.failed.to.bundle.label.text");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PoCBSP.components.failed.to.bundle.text");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_CONTENT_TYPE = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PoCBSP.components.failed.to.bundle.content.type");
    public static final String DEFAULT_VIEW_LOG_BUTTON_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PoCBSP.view.log.button.text");
    public static final String DEFAULT_SEND_LOG_BUTTON_TEXT = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PoCBSP.send.log.button.text");
    public static final String DEFAULT_COMPONENTS_LIST_SEPARATOR = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PoCBSP.components.list.separator");
    
    public static final String DEFAULT_DIALOG_TITLE = ResourceUtils.getString(PostCreateBundleSummaryPanel.class, "PoCBSP.dialog.title");
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public PostCreateBundleSummaryPanel() {
        setProperty(MESSAGE_SUCCESS_TEXT_PROPERTY, DEFAULT_MESSAGE_SUCCESS_TEXT);
        setProperty(MESSAGE_SUCCESS_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_SUCCESS_CONTENT_TYPE);
        setProperty(MESSAGE_ERRORS_TEXT_PROPERTY, DEFAULT_MESSAGE_ERRORS_TEXT);
        setProperty(MESSAGE_ERRORS_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_ERRORS_CONTENT_TYPE);
        setProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_LABEL_TEXT_PROPERTY, DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_LABEL_TEXT);
        setProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_TEXT_PROPERTY, DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_TEXT);
        setProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_CONTENT_TYPE_PROPERTY, DEFAULT_SUCCESSFULLY_BUNDLED_COMPONENTS_CONTENT_TYPE);
        setProperty(COMPONENTS_FAILED_TO_BUNDLE_LABEL_TEXT_PROPERTY, DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_LABEL_TEXT);
        setProperty(COMPONENTS_FAILED_TO_BUNDLE_TEXT_PROPERTY, DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_TEXT);
        setProperty(COMPONENTS_FAILED_TO_BUNDLE_CONTENT_TYPE_PROPERTY, DEFAULT_COMPONENTS_FAILED_TO_BUNDLE_CONTENT_TYPE);
        setProperty(VIEW_LOG_BUTTON_TEXT_PROPERTY, DEFAULT_VIEW_LOG_BUTTON_TEXT);
        setProperty(SEND_LOG_BUTTON_TEXT_PROPERTY, DEFAULT_SEND_LOG_BUTTON_TEXT);
        setProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY, DEFAULT_COMPONENTS_LIST_SEPARATOR);
        
        setProperty(TITLE_PROPERTY, DEFAULT_DIALOG_TITLE);
    }
    
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new PostCreateBundleSummaryPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class PostCreateBundleSummaryPanelUi extends WizardPanelUi {
        protected PostCreateBundleSummaryPanel        component;
        
        public PostCreateBundleSummaryPanelUi(PostCreateBundleSummaryPanel component) {
            super(component);
            
            this.component = component;
        }
        
        // swing ui specific ////////////////////////////////////////////////////////
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new PostCreateBundleSummaryPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class PostCreateBundleSummaryPanelSwingUi extends WizardPanelSwingUi {
        protected PostCreateBundleSummaryPanel component;
        
        private NbiTextPane messagePane;
        
        private NbiLabel    successfullyBundledComponentsLabel;
        private NbiTextPane successfullyBundledComponentsPane;
        private NbiLabel    componentsFailedToBundleLabel;
        private NbiTextPane componentsFailedToBundlePane;
        
        private NbiButton   viewLogButton;
        private NbiButton   sendLogButton;
        
        private NbiPanel    spacer;
        
        private InstallationLogDialog     logDialog;
        
        public PostCreateBundleSummaryPanelSwingUi(
                final PostCreateBundleSummaryPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
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
        
        protected void initialize() {
            final Registry registry = Registry.getInstance();
            
            final boolean errorsEncountered = 
                    registry.getProducts(FAILED_TO_INSTALL).size() > 0 && 
                    registry.getProducts(FAILED_TO_UNINSTALL).size() > 0;
            
            if (errorsEncountered) {
                messagePane.setContentType(component.getProperty(MESSAGE_ERRORS_CONTENT_TYPE_PROPERTY));
                messagePane.setText(component.getProperty(MESSAGE_ERRORS_TEXT_PROPERTY));
            } else {
                messagePane.setContentType(component.getProperty(MESSAGE_SUCCESS_CONTENT_TYPE_PROPERTY));
                messagePane.setText(component.getProperty(MESSAGE_SUCCESS_TEXT_PROPERTY));
            }
            
            List<Product> components;
            
            components = registry.getProducts(INSTALLED_SUCCESSFULLY);
            if (components.size() > 0) {
                successfullyBundledComponentsLabel.setVisible(true);
                successfullyBundledComponentsPane.setVisible(true);
                
                successfullyBundledComponentsLabel.setText(component.getProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_LABEL_TEXT_PROPERTY));
                successfullyBundledComponentsPane.setContentType(component.getProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_CONTENT_TYPE_PROPERTY));
                successfullyBundledComponentsPane.setText(StringUtils.format(component.getProperty(SUCCESSFULLY_BUNDLED_COMPONENTS_TEXT_PROPERTY), StringUtils.asString(components, component.getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
            } else {
                successfullyBundledComponentsLabel.setVisible(false);
                successfullyBundledComponentsPane.setVisible(false);
            }
            
            components = registry.getProducts(FAILED_TO_INSTALL);
            if (components.size() > 0) {
                componentsFailedToBundleLabel.setVisible(true);
                componentsFailedToBundlePane.setVisible(true);
                
                componentsFailedToBundleLabel.setText(component.getProperty(COMPONENTS_FAILED_TO_BUNDLE_LABEL_TEXT_PROPERTY));
                componentsFailedToBundlePane.setContentType(component.getProperty(COMPONENTS_FAILED_TO_BUNDLE_CONTENT_TYPE_PROPERTY));
                componentsFailedToBundlePane.setText(StringUtils.format(component.getProperty(COMPONENTS_FAILED_TO_BUNDLE_TEXT_PROPERTY), StringUtils.asString(components, component.getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
            } else {
                componentsFailedToBundleLabel.setVisible(false);
                componentsFailedToBundlePane.setVisible(false);
            }
            
            viewLogButton.setText(component.getProperty(VIEW_LOG_BUTTON_TEXT_PROPERTY));
            sendLogButton.setText(component.getProperty(SEND_LOG_BUTTON_TEXT_PROPERTY));
        }
        
        private void initComponents() {
            messagePane = new NbiTextPane();
            
            successfullyBundledComponentsLabel = new NbiLabel();
            
            successfullyBundledComponentsPane = new NbiTextPane();
            successfullyBundledComponentsPane.setOpaque(false);
            successfullyBundledComponentsPane.setEditable(false);
            successfullyBundledComponentsPane.setBorder(new EmptyBorder(0, 0, 0, 0));
            
            componentsFailedToBundleLabel = new NbiLabel();
            
            componentsFailedToBundlePane = new NbiTextPane();
            componentsFailedToBundlePane.setOpaque(false);
            componentsFailedToBundlePane.setEditable(false);
            componentsFailedToBundlePane.setBorder(new EmptyBorder(0, 0, 0, 0));
            
            viewLogButton = new NbiButton();
            viewLogButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    viewLogButtonClicked();
                }
            });
            
            sendLogButton = new NbiButton();
            sendLogButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    sendLogButtonClicked();
                }
            });
            sendLogButton.setEnabled(false);
            
            spacer = new NbiPanel();
            
            add(messagePane, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 0, 11), 0, 0));
            add(successfullyBundledComponentsLabel, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 11, 0, 11), 0, 0));
            add(successfullyBundledComponentsPane, new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
            add(componentsFailedToBundleLabel, new GridBagConstraints(0, 3, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 11, 0, 11), 0, 0));
            add(componentsFailedToBundlePane, new GridBagConstraints(0, 4, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
            add(spacer, new GridBagConstraints(0, 5, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 11, 0, 11), 0, 0));
            add(viewLogButton, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 11, 11, 0), 0, 0));
            add(sendLogButton, new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 6, 11, 11), 0, 0));
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
}
