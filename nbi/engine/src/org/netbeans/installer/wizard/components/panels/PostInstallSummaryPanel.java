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
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.product.ProductRegistry;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.utils.InstallationDetailsDialog;
import org.netbeans.installer.wizard.utils.InstallationLogDialog;

/**
 *
 * @author Kirill Sorokin
 */
public class PostInstallSummaryPanel extends TextPanel {
    private NbiTextPane messagePane;
    
    private NbiLabel    successfullyInstalledComponentsLabel;
    private NbiTextPane successfullyInstalledComponentsPane;
    private NbiLabel    componentsInstalledWithWarningsLabel;
    private NbiTextPane componentsInstalledWithWarningsPane;
    private NbiLabel    componentsFailedToInstallLabel;
    private NbiTextPane componentsFailedToInstallPane;
    
    private NbiLabel    successfullyUninstalledComponentsLabel;
    private NbiTextPane successfullyUninstalledComponentsPane;
    private NbiLabel    componentsUninstalledWithWarningsLabel;
    private NbiTextPane componentsUninstalledWithWarningsPane;
    private NbiLabel    componentsFailedToUninstallLabel;
    private NbiTextPane componentsFailedToUninstallPane;
    
    private NbiButton   viewDetailsButton;
    private NbiButton   viewLogButton;
    private NbiButton   sendLogButton;
    
    private NbiPanel    spacer;
    
    private InstallationDetailsDialog detailsDialog;
    private InstallationLogDialog     logDialog;
    
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
        
        setProperty(DIALOG_TITLE_PROPERTY, DEFAULT_DIALOG_TITLE);
    }
    
    public void initialize() {
        getBackButton().setEnabled(false);
        getCancelButton().setEnabled(false);
        
        ProductRegistry registry = ProductRegistry.getInstance();
        
        if (registry.wereErrorsEncountered()) {
            messagePane.setContentType(getProperty(MESSAGE_ERRORS_CONTENT_TYPE_PROPERTY));
            messagePane.setText(getProperty(MESSAGE_ERRORS_TEXT_PROPERTY));
        } else if (registry.wereWarningsEncountered()) {
            messagePane.setContentType(getProperty(MESSAGE_WARNINGS_CONTENT_TYPE_PROPERTY));
            messagePane.setText(getProperty(MESSAGE_WARNINGS_TEXT_PROPERTY));
        } else {
            messagePane.setContentType(getProperty(MESSAGE_SUCCESS_CONTENT_TYPE_PROPERTY));
            messagePane.setText(getProperty(MESSAGE_SUCCESS_TEXT_PROPERTY));
        }
        
        List<ProductComponent> components;
        
        components = registry.getComponentsInstalledSuccessfullyDuringThisSession();
        if (components.size() > 0) {
            successfullyInstalledComponentsLabel.setVisible(true);
            successfullyInstalledComponentsPane.setVisible(true);
            
            successfullyInstalledComponentsLabel.setText(getProperty(SUCCESSFULLY_INSTALLED_COMPONENTS_LABEL_TEXT_PROPERTY));
            successfullyInstalledComponentsPane.setContentType(getProperty(SUCCESSFULLY_INSTALLED_COMPONENTS_CONTENT_TYPE_PROPERTY));
            successfullyInstalledComponentsPane.setText(StringUtils.format(getProperty(SUCCESSFULLY_INSTALLED_COMPONENTS_TEXT_PROPERTY), StringUtils.asString(components, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
        } else {
            successfullyInstalledComponentsLabel.setVisible(false);
            successfullyInstalledComponentsPane.setVisible(false);
        }
        
        components = registry.getComponentsInstalledWithWarningsDuringThisSession();
        if (components.size() > 0) {
            componentsInstalledWithWarningsLabel.setVisible(true);
            componentsInstalledWithWarningsPane.setVisible(true);
            
            componentsInstalledWithWarningsLabel.setText(getProperty(COMPONENTS_INSTALLED_WITH_WARNINGS_LABEL_TEXT_PROPERTY));
            componentsInstalledWithWarningsPane.setContentType(getProperty(COMPONENTS_INSTALLED_WITH_WARNINGS_CONTENT_TYPE_PROPERTY));
            componentsInstalledWithWarningsPane.setText(StringUtils.format(getProperty(COMPONENTS_INSTALLED_WITH_WARNINGS_TEXT_PROPERTY), StringUtils.asString(components, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
        } else {
            componentsInstalledWithWarningsLabel.setVisible(false);
            componentsInstalledWithWarningsPane.setVisible(false);
        }
        
        components = registry.getComponentsFailedToInstallDuringThisSession();
        if (components.size() > 0) {
            componentsFailedToInstallLabel.setVisible(true);
            componentsFailedToInstallPane.setVisible(true);
            
            componentsFailedToInstallLabel.setText(getProperty(COMPONENTS_FAILED_TO_INSTALL_WARNINGS_LABEL_TEXT_PROPERTY));
            componentsFailedToInstallPane.setContentType(getProperty(COMPONENTS_FAILED_TO_INSTALL_CONTENT_TYPE_PROPERTY));
            componentsFailedToInstallPane.setText(StringUtils.format(getProperty(COMPONENTS_FAILED_TO_INSTALL_TEXT_PROPERTY), StringUtils.asString(components, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
        } else {
            componentsFailedToInstallLabel.setVisible(false);
            componentsFailedToInstallPane.setVisible(false);
        }
        
        components = registry.getComponentsUninstalledSuccessfullyDuringThisSession();
        if (components.size() > 0) {
            successfullyUninstalledComponentsLabel.setVisible(true);
            successfullyUninstalledComponentsPane.setVisible(true);
            
            successfullyUninstalledComponentsLabel.setText(getProperty(SUCCESSFULLY_UNINSTALLED_COMPONENTS_LABEL_TEXT_PROPERTY));
            successfullyUninstalledComponentsPane.setContentType(getProperty(SUCCESSFULLY_UNINSTALLED_COMPONENTS_CONTENT_TYPE_PROPERTY));
            successfullyUninstalledComponentsPane.setText(StringUtils.format(getProperty(SUCCESSFULLY_UNINSTALLED_COMPONENTS_TEXT_PROPERTY), StringUtils.asString(components, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
        } else {
            successfullyUninstalledComponentsLabel.setVisible(false);
            successfullyUninstalledComponentsPane.setVisible(false);
        }
        
        components = registry.getComponentsUninstalledWithWarningsDuringThisSession();
        if (components.size() > 0) {
            componentsUninstalledWithWarningsLabel.setVisible(true);
            componentsUninstalledWithWarningsPane.setVisible(true);
            
            componentsUninstalledWithWarningsLabel.setText(getProperty(COMPONENTS_UNINSTALLED_WITH_WARNINGS_LABEL_TEXT_PROPERTY));
            componentsUninstalledWithWarningsPane.setContentType(getProperty(COMPONENTS_UNINSTALLED_WITH_WARNINGS_CONTENT_TYPE_PROPERTY));
            componentsUninstalledWithWarningsPane.setText(StringUtils.format(getProperty(COMPONENTS_UNINSTALLED_WITH_WARNINGS_TEXT_PROPERTY), StringUtils.asString(components, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
        } else {
            componentsUninstalledWithWarningsLabel.setVisible(false);
            componentsUninstalledWithWarningsPane.setVisible(false);
        }
        
        components = registry.getComponentsFailedToUninstallDuringThisSession();
        if (components.size() > 0) {
            componentsFailedToUninstallLabel.setVisible(true);
            componentsFailedToUninstallPane.setVisible(true);
            
            componentsFailedToUninstallLabel.setText(getProperty(COMPONENTS_FAILED_TO_UNINSTALL_WARNINGS_LABEL_TEXT_PROPERTY));
            componentsFailedToUninstallPane.setContentType(getProperty(COMPONENTS_FAILED_TO_UNINSTALL_CONTENT_TYPE_PROPERTY));
            componentsFailedToUninstallPane.setText(StringUtils.format(getProperty(COMPONENTS_FAILED_TO_UNINSTALL_TEXT_PROPERTY), StringUtils.asString(components, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY))));
        } else {
            componentsFailedToUninstallLabel.setVisible(false);
            componentsFailedToUninstallPane.setVisible(false);
        }
        
        final String viewDetailsButtonText = getProperty(VIEW_DETAILS_BUTTON_TEXT_PROPERTY);
        viewDetailsButton.setText(StringUtils.stripMnemonic(viewDetailsButtonText));
        viewDetailsButton.setMnemonic(StringUtils.fetchMnemonic(viewDetailsButtonText));
        
        final String viewLogButtonText = getProperty(VIEW_LOG_BUTTON_TEXT_PROPERTY);
        viewLogButton.setText(StringUtils.stripMnemonic(viewLogButtonText));
        viewLogButton.setMnemonic(StringUtils.fetchMnemonic(viewLogButtonText));
        
        final String sendLogButtonText = getProperty(SEND_LOG_BUTTON_TEXT_PROPERTY);
        sendLogButton.setText(StringUtils.stripMnemonic(sendLogButtonText));
        sendLogButton.setMnemonic(StringUtils.fetchMnemonic(sendLogButtonText));
        
    }
    
    public void initComponents() {
        messagePane = new NbiTextPane();
        
        successfullyInstalledComponentsLabel = new NbiLabel();
        
        successfullyInstalledComponentsPane = new NbiTextPane();
        successfullyInstalledComponentsPane.setOpaque(false);
        successfullyInstalledComponentsPane.setEditable(false);
        successfullyInstalledComponentsPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        componentsInstalledWithWarningsLabel = new NbiLabel();
        
        componentsInstalledWithWarningsPane = new NbiTextPane();
        componentsInstalledWithWarningsPane.setOpaque(false);
        componentsInstalledWithWarningsPane.setEditable(false);
        componentsInstalledWithWarningsPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        componentsFailedToInstallLabel = new NbiLabel();
        
        componentsFailedToInstallPane = new NbiTextPane();
        componentsFailedToInstallPane.setOpaque(false);
        componentsFailedToInstallPane.setEditable(false);
        componentsFailedToInstallPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        successfullyUninstalledComponentsLabel = new NbiLabel();
        
        successfullyUninstalledComponentsPane = new NbiTextPane();
        successfullyUninstalledComponentsPane.setOpaque(false);
        successfullyUninstalledComponentsPane.setEditable(false);
        successfullyUninstalledComponentsPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        componentsUninstalledWithWarningsLabel = new NbiLabel();
        
        componentsUninstalledWithWarningsPane = new NbiTextPane();
        componentsUninstalledWithWarningsPane.setOpaque(false);
        componentsUninstalledWithWarningsPane.setEditable(false);
        componentsUninstalledWithWarningsPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        componentsFailedToUninstallLabel = new NbiLabel();
        
        componentsFailedToUninstallPane = new NbiTextPane();
        componentsFailedToUninstallPane.setOpaque(false);
        componentsFailedToUninstallPane.setEditable(false);
        componentsFailedToUninstallPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        viewDetailsButton = new NbiButton();
        viewDetailsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                viewDetailsButtonClicked();
            }
        });
        
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
    
    private void viewDetailsButtonClicked() {
        if (detailsDialog == null) {
            detailsDialog = new InstallationDetailsDialog(getWizard().getFrame());
        }
        detailsDialog.setVisible(true);
    }
    
    private void viewLogButtonClicked() {
        if (LogManager.getLogFile() != null) {
            if (logDialog == null) {
                logDialog = new InstallationLogDialog(getWizard().getFrame());
            }
            logDialog.setVisible(true);
            logDialog.loadLogFile();
        } else {
            ErrorManager.notify(ErrorLevel.ERROR, "Log file is not available.");
        }
    }
    
    private void sendLogButtonClicked() {
//        try {
//            URL url = new URL("http://glass.russia.sun.com:8080/nbi/SendLogServlet");
//            
//            File logFile = LogManager.getLogFile();
//            String logContents = fileUtils.readFile(logFile);
//            
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            
//            connection.setRequestMethod("POST");
//            connection.setDoOutput(true);
//            connection.setDoInput(true);
//            
//            connection.connect();            
//            
//            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
//            writer.write(URLEncoder.encode("log", "UTF-8") + "=" + URLEncoder.encode(logContents, "UTF-8"));
//            writer.close();
//            
//            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            while (reader.ready()) {
//                System.out.println(reader.readLine());
//            }
//            reader.close();
//        } catch (IOException e) {
//            ErrorManager.notify(ErrorLevel.ERROR, "Cannot send log file.", e);
//        } catch (Throwable e) {
//            ErrorManager.notify(ErrorLevel.ERROR, "Cannot send log file.", e);
//        }
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
    public static final String SUCCESSFULLY_UNINSTALLED_COMPONENTS_LABEL_TEXT_PROPERTY = "successfully.uninstalled.components.label.text";
    public static final String SUCCESSFULLY_UNINSTALLED_COMPONENTS_TEXT_PROPERTY = "successfully.uninstalled.components.text";
    public static final String SUCCESSFULLY_UNINSTALLED_COMPONENTS_CONTENT_TYPE_PROPERTY = "successfully.uninstalled.components.content.type";
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
    
    public static final String DEFAULT_MESSAGE_SUCCESS_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.message.success.text");
    public static final String DEFAULT_MESSAGE_SUCCESS_CONTENT_TYPE = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.message.success.content.type");
    public static final String DEFAULT_MESSAGE_WARNINGS_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.message.warnings.text");
    public static final String DEFAULT_MESSAGE_WARNINGS_CONTENT_TYPE = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.message.warnings.content.type");
    public static final String DEFAULT_MESSAGE_ERRORS_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.message.errors.text");
    public static final String DEFAULT_MESSAGE_ERRORS_CONTENT_TYPE = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.message.errors.content.type");
    public static final String DEFAULT_SUCCESSFULLY_INSTALLED_COMPONENTS_LABEL_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.successfully.installed.components.label.text");
    public static final String DEFAULT_SUCCESSFULLY_INSTALLED_COMPONENTS_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.successfully.installed.components.text");
    public static final String DEFAULT_SUCCESSFULLY_INSTALLED_COMPONENTS_CONTENT_TYPE = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.successfully.installed.components.content.type");
    public static final String DEFAULT_COMPONENTS_INSTALLED_WITH_WARNINGS_LABEL_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.installed.with.warnings.label.text");
    public static final String DEFAULT_COMPONENTS_INSTALLED_WITH_WARNINGS_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.installed.with.warnings.text");
    public static final String DEFAULT_COMPONENTS_INSTALLED_WITH_WARNINGS_CONTENT_TYPE = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.installed.with.warnings.content.type");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_INSTALL_WARNINGS_LABEL_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.failed.to.install.label.text");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_INSTALL_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.failed.to.install.text");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_INSTALL_CONTENT_TYPE = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.failed.to.install.content.type");
    public static final String DEFAULT_SUCCESSFULLY_UNINSTALLED_COMPONENTS_LABEL_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.successfully.uninstalled.components.label.text");
    public static final String DEFAULT_SUCCESSFULLY_UNINSTALLED_COMPONENTS_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.successfully.uninstalled.components.text");
    public static final String DEFAULT_SUCCESSFULLY_UNINSTALLED_COMPONENTS_CONTENT_TYPE = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.successfully.uninstalled.components.content.type");
    public static final String DEFAULT_COMPONENTS_UNINSTALLED_WITH_WARNINGS_LABEL_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.uninstalled.with.warnings.label.text");
    public static final String DEFAULT_COMPONENTS_UNINSTALLED_WITH_WARNINGS_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.uninstalled.with.warnings.text");
    public static final String DEFAULT_COMPONENTS_UNINSTALLED_WITH_WARNINGS_CONTENT_TYPE = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.uninstalled.with.warnings.content.type");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_UNINSTALL_WARNINGS_LABEL_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.failed.to.uninstall.label.text");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_UNINSTALL_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.failed.to.uninstall.text");
    public static final String DEFAULT_COMPONENTS_FAILED_TO_UNINSTALL_CONTENT_TYPE = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.failed.to.uninstall.content.type");
    public static final String DEFAULT_VIEW_DETAILS_BUTTON_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.view.details.button.text");
    public static final String DEFAULT_VIEW_LOG_BUTTON_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.view.log.button.text");
    public static final String DEFAULT_SEND_LOG_BUTTON_TEXT = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.send.log.button.text");
    public static final String DEFAULT_COMPONENTS_LIST_SEPARATOR = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.components.list.separator");
    
    public static final String DEFAULT_DIALOG_TITLE = ResourceUtils.getString(PostInstallSummaryPanel.class, "PostInstallSummaryPanel.default.dialog.title");
}
