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
public class PreCreateBundleSummaryPanel extends DefaultWizardPanel {
    private NbiTextPane   messagePane;
    private NbiLabel      componentsToBundleLabel;
    private NbiTextPane   componentsToBundlePane;
    private NbiLabel      downloadSizeLabel;
    private NbiLabel      requiredDiskSpaceLabel;
    
    private NbiPanel      spacer;
    
    public PreCreateBundleSummaryPanel() {
        setProperty(MESSAGE_TEXT_PROPERTY, DEFAULT_MESSAGE_TEXT);
        setProperty(MESSAGE_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_CONTENT_TYPE);
        setProperty(COMPONENTS_TO_BUNDLE_LABEL_TEXT_PROPERTY, DEFAULT_COMPONENTS_TO_BUNDLE_LABEL_TEXT);
        setProperty(COMPONENTS_TO_BUNDLE_TEXT_PROPERTY, DEFAULT_COMPONENTS_TO_BUNDLE_TEXT);
        setProperty(COMPONENTS_TO_BUNDLE_CONTENT_TYPE_PROPERTY, DEFAULT_COMPONENTS_TO_BUNDLE_CONTENT_TYPE);
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
        
        List<ProductComponent> componentsToBundle = ProductRegistry.getInstance().getComponentsToInstall();
        
        componentsToBundleLabel.setVisible(true);
        componentsToBundlePane.setVisible(true);
        downloadSizeLabel.setVisible(true);
        requiredDiskSpaceLabel.setVisible(true);
        
        final String componentsToInstallLabelText = getProperty(COMPONENTS_TO_BUNDLE_LABEL_TEXT_PROPERTY);
        componentsToBundleLabel.setText(componentsToInstallLabelText);
        
        final String componentsToInstallContentType = getProperty(COMPONENTS_TO_BUNDLE_CONTENT_TYPE_PROPERTY);
        componentsToBundlePane.setContentType(componentsToInstallContentType);
        
        final String componentsToInstallText = stringUtils.formatMessage(getProperty(COMPONENTS_TO_BUNDLE_TEXT_PROPERTY), stringUtils.asString(componentsToBundle, getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY)));
        componentsToBundlePane.setText(componentsToInstallText);
        
        long downloadSize = 0;
        for (ProductComponent component: componentsToBundle) {
            downloadSize += component.getDownloadSize();
        }
        
        long requiredDiskSpace = 0;
        for (ProductComponent component: componentsToBundle) {
            requiredDiskSpace += component.getRequiredDiskSpace();
        }
        
        final String downloadSizeLabelText = stringUtils.formatMessage(getProperty(DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY), stringUtils.formatSize(downloadSize));
        downloadSizeLabel.setText(downloadSizeLabelText);
        
        final String requiredDiskSpaceLabelText = stringUtils.formatMessage(getProperty(REQUIRED_DISK_SPACE_LABEL_TEXT_PROPERTY), stringUtils.formatSize(requiredDiskSpace));
        requiredDiskSpaceLabel.setText(requiredDiskSpaceLabelText);
    }
    
    public void initComponents() {
        messagePane = new NbiTextPane();
        
        componentsToBundleLabel = new NbiLabel();
        
        componentsToBundlePane = new NbiTextPane();
        componentsToBundlePane.setOpaque(false);
        componentsToBundlePane.setEditable(false);
        componentsToBundlePane.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        downloadSizeLabel = new NbiLabel();
        
        requiredDiskSpaceLabel = new NbiLabel();
        
        spacer = new NbiPanel();
        
        add(messagePane, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 11, 0, 11), 0, 0));
        add(componentsToBundleLabel, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 11, 0, 11), 0, 0));
        add(componentsToBundlePane, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
        add(downloadSizeLabel, new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(25, 11, 0, 11), 0, 0));
        add(requiredDiskSpaceLabel, new GridBagConstraints(0, 5, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 11, 0, 11), 0, 0));
        add(spacer, new GridBagConstraints(0, 6, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 11, 11, 11), 0, 0));
        
    }
    
    public boolean canExecuteForward() {
        return ProductRegistry.getInstance().getComponentsToInstall().size() > 0;
    }
    
    public boolean canExecuteBackward() {
        return ProductRegistry.getInstance().getComponentsToInstall().size() > 0;
    }
    
    public static final String MESSAGE_TEXT_PROPERTY = "message.text";
    public static final String MESSAGE_CONTENT_TYPE_PROPERTY = "message.content.type";
    public static final String COMPONENTS_TO_BUNDLE_LABEL_TEXT_PROPERTY = "components.to.bundle.label.text";
    public static final String COMPONENTS_TO_BUNDLE_TEXT_PROPERTY = "components.to.bundle.text";
    public static final String COMPONENTS_TO_BUNDLE_CONTENT_TYPE_PROPERTY = "components.to.bundle.content.type";
    public static final String COMPONENTS_LIST_SEPARATOR_PROPERTY = "components.list.separator";
    public static final String DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY = "download.size.label.text";
    public static final String REQUIRED_DISK_SPACE_LABEL_TEXT_PROPERTY = "required.disk.space.label.text";
    
    public static final String DEFAULT_MESSAGE_TEXT = resourceUtils.getString(PreCreateBundleSummaryPanel.class, "PreCreateBundleSummaryPanel.default.message.text");
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE = resourceUtils.getString(PreCreateBundleSummaryPanel.class, "PreCreateBundleSummaryPanel.default.message.content.type");
    public static final String DEFAULT_COMPONENTS_TO_BUNDLE_LABEL_TEXT = resourceUtils.getString(PreCreateBundleSummaryPanel.class, "PreCreateBundleSummaryPanel.default.components.to.bundle.label.text");
    public static final String DEFAULT_COMPONENTS_TO_BUNDLE_TEXT = resourceUtils.getString(PreCreateBundleSummaryPanel.class, "PreCreateBundleSummaryPanel.default.components.to.bundle.text");
    public static final String DEFAULT_COMPONENTS_TO_BUNDLE_CONTENT_TYPE = resourceUtils.getString(PreCreateBundleSummaryPanel.class, "PreCreateBundleSummaryPanel.default.components.to.bundle.content.type");
    public static final String DEFAULT_COMPONENTS_LIST_SEPARATOR = resourceUtils.getString(PreCreateBundleSummaryPanel.class, "PreCreateBundleSummaryPanel.default.components.list.separator");
    public static final String DEFAULT_DOWNLOAD_SIZE_LABEL_TEXT = resourceUtils.getString(PreCreateBundleSummaryPanel.class, "PreCreateBundleSummaryPanel.default.download.size.label.text");
    public static final String DEFAULT_REQUIRED_DISK_SPACE_LABEL_TEXT = resourceUtils.getString(PreCreateBundleSummaryPanel.class, "PreCreateBundleSummaryPanel.default.required.disk.space.label.text");
    
    public static final String DEFAULT_DIALOG_TITLE = resourceUtils.getString(PreCreateBundleSummaryPanel.class, "PreCreateBundleSummaryPanel.default.dialog.title");
}
