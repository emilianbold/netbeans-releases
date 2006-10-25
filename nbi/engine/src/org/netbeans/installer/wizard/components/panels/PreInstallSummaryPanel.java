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
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.product.ProductRegistry;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;

/**
 *
 * @author Kirill Sorokin
 */
public class PreInstallSummaryPanel extends DefaultWizardPanel {
    private NbiTextPane   messagePane;
    private NbiLabel      componentsToInstallLabel;
    private NbiTextPane   componentsToInstallPane;
    private NbiLabel      componentsToUninstallLabel;
    private NbiTextPane   componentsToUninstallPane;
    private NbiLabel      downloadSizeLabel;
    private NbiLabel      requiredDiskSpaceLabel;
    
    private NbiPanel      spacer;
    
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
        
        setProperty(DIALOG_TITLE_PROPERTY, DEFAULT_DIALOG_TITLE);
    }
    
    public void initialize() {
        getNextButton().setText(stringUtils.stripMnemonic("&Install"));
        getNextButton().setMnemonic(stringUtils.fetchMnemonic("&Install"));
        
        final String messageContentType = getProperty(MESSAGE_CONTENT_TYPE_PROPERTY);
        messagePane.setContentType(messageContentType);
        
        final String messageText = getProperty(MESSAGE_TEXT_PROPERTY);
        messagePane.setText(messageText);
        
        List<ProductComponent> componentsToInstall = ProductRegistry.getInstance().getComponentsToInstall();
        List<ProductComponent> componentsToUninstall = ProductRegistry.getInstance().getComponentsToUninstall();
        
        if (componentsToUninstall.size() > 0) {
            componentsToUninstallLabel.setVisible(true);
            componentsToUninstallPane.setVisible(true);
            
            final String componentsToUninstallLabelText = getProperty(COMPONENTS_TO_UNINSTALL_LABEL_TEXT_PROPERTY);
            componentsToUninstallLabel.setText(componentsToUninstallLabelText);
            
            final String componentsToUninstallContentType = getProperty(COMPONENTS_TO_UNINSTALL_CONTENT_TYPE_PROPERTY);
            componentsToUninstallPane.setContentType(componentsToUninstallContentType);
            
            final String componentsToUninstallText = stringUtils.formatMessage(getProperty(COMPONENTS_TO_UNINSTALL_TEXT_PROPERTY), stringUtils.asString(componentsToUninstall, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY)));
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
                    
            final String componentsToInstallLabelText = getProperty(COMPONENTS_TO_INSTALL_LABEL_TEXT_PROPERTY);
            componentsToInstallLabel.setText(componentsToInstallLabelText);
            
            final String componentsToInstallContentType = getProperty(COMPONENTS_TO_INSTALL_CONTENT_TYPE_PROPERTY);
            componentsToInstallPane.setContentType(componentsToInstallContentType);
            
            final String componentsToInstallText = stringUtils.formatMessage(getProperty(COMPONENTS_TO_INSTALL_TEXT_PROPERTY), stringUtils.asString(componentsToInstall, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY)));
            componentsToInstallPane.setText(componentsToInstallText);
            
            long downloadSize = 0;
            for (ProductComponent component: componentsToInstall) {
                downloadSize += component.getDownloadSize();
            }
            
            long requiredDiskSpace = 0;
            for (ProductComponent component: componentsToInstall) {
                requiredDiskSpace += component.getRequiredDiskSpace();
            }
            
            final String downloadSizeLabelText = stringUtils.formatMessage(getProperty(DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY), stringUtils.formatSize(downloadSize));
            downloadSizeLabel.setText(downloadSizeLabelText);
            
            final String requiredDiskSpaceLabelText = stringUtils.formatMessage(getProperty(REQUIRED_DISK_SPACE_LABEL_TEXT_PROPERTY), stringUtils.formatSize(requiredDiskSpace));
            requiredDiskSpaceLabel.setText(requiredDiskSpaceLabelText);
        } else {
            componentsToInstallLabel.setVisible(false);
            componentsToInstallPane.setVisible(false);
            downloadSizeLabel.setVisible(false);
            requiredDiskSpaceLabel.setVisible(false);
        }
    }
    
    public void initComponents() {
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
    
    public static final String DEFAULT_MESSAGE_TEXT = resourceUtils.getString(PreInstallSummaryPanel.class, "PreInstallSummaryPanel.default.message.text");
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE = resourceUtils.getString(PreInstallSummaryPanel.class, "PreInstallSummaryPanel.default.message.content.type");
    public static final String DEFAULT_COMPONENTS_TO_INSTALL_LABEL_TEXT = resourceUtils.getString(PreInstallSummaryPanel.class, "PreInstallSummaryPanel.default.components.to.install.label.text");
    public static final String DEFAULT_COMPONENTS_TO_INSTALL_TEXT = resourceUtils.getString(PreInstallSummaryPanel.class, "PreInstallSummaryPanel.default.components.to.install.text");
    public static final String DEFAULT_COMPONENTS_TO_INSTALL_CONTENT_TYPE = resourceUtils.getString(PreInstallSummaryPanel.class, "PreInstallSummaryPanel.default.components.to.install.content.type");
    public static final String DEFAULT_COMPONENTS_TO_UNINSTALL_LABEL_TEXT = resourceUtils.getString(PreInstallSummaryPanel.class, "PreInstallSummaryPanel.default.components.to.uninstall.label.text");
    public static final String DEFAULT_COMPONENTS_TO_UNINSTALL_TEXT = resourceUtils.getString(PreInstallSummaryPanel.class, "PreInstallSummaryPanel.default.components.to.uninstall.text");
    public static final String DEFAULT_COMPONENTS_TO_UNINSTALL_CONTENT_TYPE = resourceUtils.getString(PreInstallSummaryPanel.class, "PreInstallSummaryPanel.default.components.to.uninstall.content.type");
    public static final String DEFAULT_COMPONENTS_LIST_SEPARATOR = resourceUtils.getString(PreInstallSummaryPanel.class, "PreInstallSummaryPanel.default.components.list.separator");
    public static final String DEFAULT_DOWNLOAD_SIZE_LABEL_TEXT = resourceUtils.getString(PreInstallSummaryPanel.class, "PreInstallSummaryPanel.default.download.size.label.text");
    public static final String DEFAULT_REQUIRED_DISK_SPACE_LABEL_TEXT = resourceUtils.getString(PreInstallSummaryPanel.class, "PreInstallSummaryPanel.default.required.disk.space.label.text");
    
    public static final String DEFAULT_DIALOG_TITLE = resourceUtils.getString(PreInstallSummaryPanel.class, "PreInstallSummaryPanel.default.dialog.title");
}
