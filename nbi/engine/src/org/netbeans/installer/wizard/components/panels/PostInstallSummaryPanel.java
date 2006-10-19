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
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.product.ProductRegistry;

/**
 *
 * @author Kirill Sorokin
 */
public class PostInstallSummaryPanel extends TextPanel {
    private JTextPane   messagePane;
    
    private JLabel      successfullyInstalledComponentsLabel;
    private JTextPane   successfullyInstalledComponentsPane;
    private JLabel      componentsInstalledWithWarningsLabel;
    private JTextPane   componentsInstalledWithWarningsPane;
    private JLabel      componentsFailedToInstallLabel;
    private JTextPane   componentsFailedToInstallPane;
    
    private JLabel      successfullyUninstalledComponentsLabel;
    private JTextPane   successfullyUninstalledComponentsPane;
    private JLabel      componentsUninstalledWithWarningsLabel;
    private JTextPane   componentsUninstalledWithWarningsPane;
    private JLabel      componentsFailedToUninstallLabel;
    private JTextPane   componentsFailedToUninstallPane;
    
    private JButton     viewDetailsButton;
    private JButton     viewLogButton;
    private JButton     sendLogButton;
    
    private JPanel      spacer;
    
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
    }
    
    public void initialize() {
        if (ProductRegistry.getInstance().wereErrorsEncountered()) {
            messagePane.setContentType(getProperty(MESSAGE_ERRORS_CONTENT_TYPE_PROPERTY));
            messagePane.setText(getProperty(MESSAGE_ERRORS_TEXT_PROPERTY));
        } else if (ProductRegistry.getInstance().wereWarningsEncountered()) {
            messagePane.setContentType(getProperty(MESSAGE_WARNINGS_CONTENT_TYPE_PROPERTY));
            messagePane.setText(getProperty(MESSAGE_WARNINGS_TEXT_PROPERTY));
        } else {
            messagePane.setContentType(getProperty(MESSAGE_SUCCESS_CONTENT_TYPE_PROPERTY));
            messagePane.setText(getProperty(MESSAGE_SUCCESS_TEXT_PROPERTY));
        }
        
        List<ProductComponent> components;
        
        components = ProductRegistry.getInstance().getComponentsInstalledSuccessfullyDuringThisSession();
        if (components.size() > 0) {
            successfullyInstalledComponentsLabel.setVisible(true);
            successfullyInstalledComponentsPane.setVisible(true);
            
            successfullyInstalledComponentsLabel.setText(getProperty(SUCCESSFULLY_INSTALLED_COMPONENTS_LABEL_TEXT_PROPERTY));
            successfullyInstalledComponentsPane.setContentType(getProperty(SUCCESSFULLY_INSTALLED_COMPONENTS_CONTENT_TYPE_PROPERTY));
            successfullyInstalledComponentsPane.setText(stringUtils.formatMessage(getProperty(SUCCESSFULLY_INSTALLED_COMPONENTS_TEXT_PROPERTY), stringUtils.asString(components, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
        } else {
            successfullyInstalledComponentsLabel.setVisible(false);
            successfullyInstalledComponentsPane.setVisible(false);
        }
        
        components = ProductRegistry.getInstance().getComponentsInstalledWithWarningsDuringThisSession();
        if (components.size() > 0) {
            componentsInstalledWithWarningsLabel.setVisible(true);
            componentsInstalledWithWarningsPane.setVisible(true);
            
            componentsInstalledWithWarningsLabel.setText(getProperty(COMPONENTS_INSTALLED_WITH_WARNINGS_LABEL_TEXT_PROPERTY));
            componentsInstalledWithWarningsPane.setContentType(getProperty(COMPONENTS_INSTALLED_WITH_WARNINGS_CONTENT_TYPE_PROPERTY));
            componentsInstalledWithWarningsPane.setText(stringUtils.formatMessage(getProperty(COMPONENTS_INSTALLED_WITH_WARNINGS_TEXT_PROPERTY), stringUtils.asString(components, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
        } else {
            componentsInstalledWithWarningsLabel.setVisible(false);
            componentsInstalledWithWarningsPane.setVisible(false);
        }
        
        components = ProductRegistry.getInstance().getComponentsFailedToInstallDuringThisSession();
        if (components.size() > 0) {
            componentsInstalledWithWarningsLabel.setVisible(true);
            componentsInstalledWithWarningsPane.setVisible(true);
            
            componentsInstalledWithWarningsLabel.setText(getProperty(COMPONENTS_FAILED_TO_INSTALL_WARNINGS_LABEL_TEXT_PROPERTY));
            componentsInstalledWithWarningsPane.setContentType(getProperty(COMPONENTS_FAILED_TO_INSTALL_CONTENT_TYPE_PROPERTY));
            componentsInstalledWithWarningsPane.setText(stringUtils.formatMessage(getProperty(COMPONENTS_FAILED_TO_INSTALL_TEXT_PROPERTY), stringUtils.asString(components, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
        } else {
            componentsInstalledWithWarningsLabel.setVisible(false);
            componentsInstalledWithWarningsPane.setVisible(false);
        }
        
        components = ProductRegistry.getInstance().getComponentsUninstalledSuccessfullyDuringThisSession();
        if (components.size() > 0) {
            successfullyUninstalledComponentsLabel.setVisible(true);
            successfullyUninstalledComponentsPane.setVisible(true);
            
            successfullyUninstalledComponentsLabel.setText(getProperty(SUCCESSFULLY_UNINSTALLED_COMPONENTS_LABEL_TEXT_PROPERTY));
            successfullyUninstalledComponentsPane.setContentType(getProperty(SUCCESSFULLY_UNINSTALLED_COMPONENTS_CONTENT_TYPE_PROPERTY));
            successfullyUninstalledComponentsPane.setText(stringUtils.formatMessage(getProperty(SUCCESSFULLY_UNINSTALLED_COMPONENTS_TEXT_PROPERTY), stringUtils.asString(components, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
        } else {
            successfullyUninstalledComponentsLabel.setVisible(false);
            successfullyUninstalledComponentsPane.setVisible(false);
        }
        
        components = ProductRegistry.getInstance().getComponentsUninstalledWithWarningsDuringThisSession();
        if (components.size() > 0) {
            componentsUninstalledWithWarningsLabel.setVisible(true);
            componentsUninstalledWithWarningsPane.setVisible(true);
            
            componentsUninstalledWithWarningsLabel.setText(getProperty(COMPONENTS_UNINSTALLED_WITH_WARNINGS_LABEL_TEXT_PROPERTY));
            componentsUninstalledWithWarningsPane.setContentType(getProperty(COMPONENTS_UNINSTALLED_WITH_WARNINGS_CONTENT_TYPE_PROPERTY));
            componentsUninstalledWithWarningsPane.setText(stringUtils.formatMessage(getProperty(COMPONENTS_UNINSTALLED_WITH_WARNINGS_TEXT_PROPERTY), stringUtils.asString(components, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
        } else {
            componentsUninstalledWithWarningsLabel.setVisible(false);
            componentsUninstalledWithWarningsPane.setVisible(false);
        }
        
        components = ProductRegistry.getInstance().getComponentsFailedToUninstallDuringThisSession();
        if (components.size() > 0) {
            componentsUninstalledWithWarningsLabel.setVisible(true);
            componentsUninstalledWithWarningsPane.setVisible(true);
            
            componentsUninstalledWithWarningsLabel.setText(getProperty(COMPONENTS_FAILED_TO_UNINSTALL_WARNINGS_LABEL_TEXT_PROPERTY));
            componentsUninstalledWithWarningsPane.setContentType(getProperty(COMPONENTS_FAILED_TO_UNINSTALL_CONTENT_TYPE_PROPERTY));
            componentsUninstalledWithWarningsPane.setText(stringUtils.formatMessage(getProperty(COMPONENTS_FAILED_TO_UNINSTALL_TEXT_PROPERTY), stringUtils.asString(components, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
        } else {
            componentsUninstalledWithWarningsLabel.setVisible(false);
            componentsUninstalledWithWarningsPane.setVisible(false);
        }
        
        final String viewDetailsButtonText = getProperty(VIEW_DETAILS_BUTTON_TEXT_PROPERTY);
        viewDetailsButton.setText(stringUtils.stripMnemonic(viewDetailsButtonText));
        viewDetailsButton.setMnemonic(stringUtils.fetchMnemonic(viewDetailsButtonText));
        
        final String viewLogButtonText = getProperty(VIEW_LOG_BUTTON_TEXT_PROPERTY);
        viewLogButton.setText(stringUtils.stripMnemonic(viewLogButtonText));
        viewLogButton.setMnemonic(stringUtils.fetchMnemonic(viewLogButtonText));
        
        final String sendLogButtonText = getProperty(SEND_LOG_BUTTON_TEXT_PROPERTY);
        sendLogButton.setText(stringUtils.stripMnemonic(sendLogButtonText));
        sendLogButton.setMnemonic(stringUtils.fetchMnemonic(sendLogButtonText));
        
    }
    
    public void initComponents() {
        setLayout(new GridBagLayout());
        
        messagePane = new JTextPane();
        messagePane.setOpaque(false);
        messagePane.setEditable(false);
        messagePane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        successfullyInstalledComponentsLabel = new JLabel();
        
        successfullyInstalledComponentsPane = new JTextPane();
        successfullyInstalledComponentsPane.setOpaque(false);
        successfullyInstalledComponentsPane.setEditable(false);
        successfullyInstalledComponentsPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        componentsInstalledWithWarningsLabel = new JLabel();
        
        componentsInstalledWithWarningsPane = new JTextPane();
        componentsInstalledWithWarningsPane.setOpaque(false);
        componentsInstalledWithWarningsPane.setEditable(false);
        componentsInstalledWithWarningsPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        componentsFailedToInstallLabel = new JLabel();
        
        componentsFailedToInstallPane = new JTextPane();
        componentsFailedToInstallPane.setOpaque(false);
        componentsFailedToInstallPane.setEditable(false);
        componentsFailedToInstallPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        successfullyUninstalledComponentsLabel = new JLabel();
        
        successfullyUninstalledComponentsPane = new JTextPane();
        successfullyUninstalledComponentsPane.setOpaque(false);
        successfullyUninstalledComponentsPane.setEditable(false);
        successfullyUninstalledComponentsPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        componentsUninstalledWithWarningsLabel = new JLabel();
        
        componentsUninstalledWithWarningsPane = new JTextPane();
        componentsUninstalledWithWarningsPane.setOpaque(false);
        componentsUninstalledWithWarningsPane.setEditable(false);
        componentsUninstalledWithWarningsPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        componentsFailedToUninstallLabel = new JLabel();
        
        componentsFailedToUninstallPane = new JTextPane();
        componentsFailedToUninstallPane.setOpaque(false);
        componentsFailedToUninstallPane.setEditable(false);
        componentsFailedToUninstallPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        viewDetailsButton = new JButton();
        
        viewLogButton = new JButton();
        
        sendLogButton = new JButton();
        
        spacer = new JPanel();
        spacer.setOpaque(false);
        
        add(messagePane, new GridBagConstraints(0, 0, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 0, 11), 0, 0));
        add(successfullyInstalledComponentsLabel, new GridBagConstraints(0, 1, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 11, 0, 11), 0, 0));
        add(successfullyInstalledComponentsPane, new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
        add(componentsInstalledWithWarningsLabel, new GridBagConstraints(0, 3, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 11, 0, 11), 0, 0));
        add(componentsInstalledWithWarningsPane, new GridBagConstraints(0, 4, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
        add(componentsFailedToInstallLabel, new GridBagConstraints(0, 5, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 11, 0, 11), 0, 0));
        add(componentsFailedToInstallPane, new GridBagConstraints(0, 6, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
        add(successfullyUninstalledComponentsLabel, new GridBagConstraints(0, 7, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 11, 0, 11), 0, 0));
        add(successfullyUninstalledComponentsPane, new GridBagConstraints(0, 8, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
        add(componentsUninstalledWithWarningsLabel, new GridBagConstraints(0, 9, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 11, 0, 11), 0, 0));
        add(componentsUninstalledWithWarningsPane, new GridBagConstraints(0, 10, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
        add(componentsFailedToUninstallLabel, new GridBagConstraints(0, 11, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 11, 0, 11), 0, 0));
        add(componentsFailedToUninstallPane, new GridBagConstraints(0, 12, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
        add(spacer, new GridBagConstraints(0, 13, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 11, 0, 11), 0, 0));
        add(viewDetailsButton, new GridBagConstraints(0, 14, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 11, 11, 0), 0, 0));
        add(viewLogButton, new GridBagConstraints(1, 14, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 6, 11, 0), 0, 0));
        add(sendLogButton, new GridBagConstraints(2, 14, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 6, 11, 11), 0, 0));
    }
    
    public static final String MESSAGE_SUCCESS_TEXT_PROPERTY = "message.success.text";
    public static final String MESSAGE_SUCCESS_CONTENT_TYPE_PROPERTY = "message.success.content.type";
    public static final String MESSAGE_WARNINGS_TEXT_PROPERTY = "message.warnings.text";
    public static final String MESSAGE_WARNINGS_CONTENT_TYPE_PROPERTY = "message.warnings.content.type";
    public static final String MESSAGE_ERRORS_TEXT_PROPERTY = "message.errors.text";
    public static final String MESSAGE_ERRORS_CONTENT_TYPE_PROPERTY = "message.errors.content.type";
    public static final String SUCCESSFULLY_INSTALLED_COMPONENTS_LABEL_TEXT_PROPERTY = "successfully.installed.components.label.text";
    public static final String SUCCESSFULLY_INSTALLED_COMPONENTS_TEXT_PROPERTY = "successfully.installed.components.text";
    public static final String SUCCESSFULLY_INSTALLED_COMPONENTS_CONTENT_TYPE_PROPERTY = "successfully.installed.components.content.type";
    public static final String COMPONENTS_INSTALLED_WITH_WARNINGS_LABEL_TEXT_PROPERTY = "components.installed.with.warnings.label.text";
    public static final String COMPONENTS_INSTALLED_WITH_WARNINGS_TEXT_PROPERTY = "components.installed.with.warnings.text";
    public static final String COMPONENTS_INSTALLED_WITH_WARNINGS_CONTENT_TYPE_PROPERTY = "components.installed.with.warnings.content.type";
    public static final String COMPONENTS_FAILED_TO_INSTALL_WARNINGS_LABEL_TEXT_PROPERTY = "components.failed.to.install.label.text";
    public static final String COMPONENTS_FAILED_TO_INSTALL_TEXT_PROPERTY = "components.failed.to.install.text";
    public static final String COMPONENTS_FAILED_TO_INSTALL_CONTENT_TYPE_PROPERTY = "components.failed.to.install.content.type";
    public static final String SUCCESSFULLY_UNINSTALLED_COMPONENTS_LABEL_TEXT_PROPERTY = "successfully.installed.components.label.text";
    public static final String SUCCESSFULLY_UNINSTALLED_COMPONENTS_TEXT_PROPERTY = "successfully.installed.components.text";
    public static final String SUCCESSFULLY_UNINSTALLED_COMPONENTS_CONTENT_TYPE_PROPERTY = "successfully.installed.components.content.type";
    public static final String COMPONENTS_UNINSTALLED_WITH_WARNINGS_LABEL_TEXT_PROPERTY = "components.uninstalled.with.warnings.label.text";
    public static final String COMPONENTS_UNINSTALLED_WITH_WARNINGS_TEXT_PROPERTY = "components.uninstalled.with.warnings.text";
    public static final String COMPONENTS_UNINSTALLED_WITH_WARNINGS_CONTENT_TYPE_PROPERTY = "components.uninstalled.with.warnings.content.type";
    public static final String COMPONENTS_FAILED_TO_UNINSTALL_WARNINGS_LABEL_TEXT_PROPERTY = "components.failed.to.uninstall.label.text";
    public static final String COMPONENTS_FAILED_TO_UNINSTALL_TEXT_PROPERTY = "components.failed.to.uninstall.text";
    public static final String COMPONENTS_FAILED_TO_UNINSTALL_CONTENT_TYPE_PROPERTY = "components.failed.to.uninstall.content.type";
    public static final String VIEW_DETAILS_BUTTON_TEXT_PROPERTY = "view.details.button.text";
    public static final String VIEW_LOG_BUTTON_TEXT_PROPERTY = "view.log.button.text";
    public static final String SEND_LOG_BUTTON_TEXT_PROPERTY = "send.log.button.text";
    public static final String COMPONENTS_LIST_SEPARATOR_PROPERTY = "components.list.separator";
    
    public static final String DEFAULT_MESSAGE_SUCCESS_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.message.success.text");
    public static final String DEFAULT_MESSAGE_SUCCESS_CONTENT_TYPE = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.message.success.content.type");
    public static final String DEFAULT_MESSAGE_WARNINGS_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.message.warnings.text");
    public static final String DEFAULT_MESSAGE_WARNINGS_CONTENT_TYPE = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.message.warnings.content.type");
    public static final String DEFAULT_MESSAGE_ERRORS_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.message.errors.text");
    public static final String DEFAULT_MESSAGE_ERRORS_CONTENT_TYPE = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.message.errors.content.type");
    public static final String DEFAULT_SUCCESSFULLY_INSTALLED_COMPONENTS_LABEL_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.successfully.installed.components.label.text");
    public static final String DEFAULT_SUCCESSFULLY_INSTALLED_COMPONENTS_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.successfully.installed.components.text");
    public static final String DEFAULT_SUCCESSFULLY_INSTALLED_COMPONENTS_CONTENT_TYPE = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.successfully.installed.components.content.type");
    public static final String DEFAULT_COMPONENTS_INSTALLED_WITH_WARNINGS_LABEL_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.installed.with.warnings.label.text");
    public static final String DEFAULT_COMPONENTS_INSTALLED_WITH_WARNINGS_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.installed.with.warnings.text");
    public static final String DEFAULT_COMPONENTS_INSTALLED_WITH_WARNINGS_CONTENT_TYPE = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.installed.with.warnings.content.type");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_INSTALL_WARNINGS_LABEL_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.failed.to.install.label.text");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_INSTALL_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.failed.to.install.text");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_INSTALL_CONTENT_TYPE = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.failed.to.install.content.type");
    public static final String DEFAULT_SUCCESSFULLY_UNINSTALLED_COMPONENTS_LABEL_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.successfully.uninstalled.components.label.text");
    public static final String DEFAULT_SUCCESSFULLY_UNINSTALLED_COMPONENTS_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.successfully.uninstalled.components.text");
    public static final String DEFAULT_SUCCESSFULLY_UNINSTALLED_COMPONENTS_CONTENT_TYPE = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.successfully.uninstalled.components.content.type");
    public static final String DEFAULT_COMPONENTS_UNINSTALLED_WITH_WARNINGS_LABEL_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.uninstalled.with.warnings.label.text");
    public static final String DEFAULT_COMPONENTS_UNINSTALLED_WITH_WARNINGS_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.uninstalled.with.warnings.text");
    public static final String DEFAULT_COMPONENTS_UNINSTALLED_WITH_WARNINGS_CONTENT_TYPE = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.uninstalled.with.warnings.content.type");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_UNINSTALL_WARNINGS_LABEL_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.failed.to.uninstall.label.text");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_UNINSTALL_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.failed.to.uninstall.text");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_UNINSTALL_CONTENT_TYPE = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.failed.to.uninstall.content.type");
    public static final String DEFAULT_VIEW_DETAILS_BUTTON_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.view.details.button.text");
    public static final String DEFAULT_VIEW_LOG_BUTTON_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.view.log.button.text");
    public static final String DEFAULT_SEND_LOG_BUTTON_TEXT = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.send.log.button.text");
    public static final String DEFAULT_COMPONENTS_LIST_SEPARATOR = resourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.list.separator");
}
