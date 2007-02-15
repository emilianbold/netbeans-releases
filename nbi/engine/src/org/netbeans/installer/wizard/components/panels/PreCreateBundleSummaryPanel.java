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
import org.netbeans.installer.product.Registry;
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
public class PreCreateBundleSummaryPanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public PreCreateBundleSummaryPanel() {
        setProperty(MESSAGE_TEXT_PROPERTY, DEFAULT_MESSAGE_TEXT);
        setProperty(MESSAGE_CONTENT_TYPE_PROPERTY, DEFAULT_MESSAGE_CONTENT_TYPE);
        setProperty(COMPONENTS_TO_BUNDLE_LABEL_TEXT_PROPERTY, DEFAULT_COMPONENTS_TO_BUNDLE_LABEL_TEXT);
        setProperty(COMPONENTS_TO_BUNDLE_TEXT_PROPERTY, DEFAULT_COMPONENTS_TO_BUNDLE_TEXT);
        setProperty(COMPONENTS_TO_BUNDLE_CONTENT_TYPE_PROPERTY, DEFAULT_COMPONENTS_TO_BUNDLE_CONTENT_TYPE);
        setProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY, DEFAULT_COMPONENTS_LIST_SEPARATOR);
        setProperty(DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY, DEFAULT_DOWNLOAD_SIZE_LABEL_TEXT);
        setProperty(REQUIRED_DISK_SPACE_LABEL_TEXT_PROPERTY, DEFAULT_REQUIRED_DISK_SPACE_LABEL_TEXT);
        
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
    }
    
    @Override
    public boolean canExecuteForward() {
        return Registry.getInstance().getProductsToInstall().size() > 0;
    }
    
    @Override
    public boolean canExecuteBackward() {
        return Registry.getInstance().getProductsToInstall().size() > 0;
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new PreCreateBundleSummaryPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class PreCreateBundleSummaryPanelUi extends WizardPanelUi {
        protected PreCreateBundleSummaryPanel        component;
        
        public PreCreateBundleSummaryPanelUi(PreCreateBundleSummaryPanel component) {
            super(component);
            
            this.component = component;
        }
        
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new PreCreateBundleSummaryPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class PreCreateBundleSummaryPanelSwingUi extends WizardPanelSwingUi {
        protected PreCreateBundleSummaryPanel component;
        
        private NbiTextPane messagePane;
        private NbiLabel componentsToBundleLabel;
        private NbiTextPane componentsToBundlePane;
        private NbiLabel downloadSizeLabel;
        private NbiLabel requiredDiskSpaceLabel;
        
        private NbiPanel spacer;
        
        public PreCreateBundleSummaryPanelSwingUi(
                final PreCreateBundleSummaryPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initializeContainer() {
            container.getNextButton().setText("&Install");
        }
        
        @Override
        protected void initialize() {
            final String messageContentType = component.getProperty(MESSAGE_CONTENT_TYPE_PROPERTY);
            messagePane.setContentType(messageContentType);
            
            final String messageText = component.getProperty(MESSAGE_TEXT_PROPERTY);
            messagePane.setText(messageText);
            
            List<Product> componentsToBundle = Registry.getInstance().getProductsToInstall();
            
            componentsToBundleLabel.setVisible(true);
            componentsToBundlePane.setVisible(true);
            downloadSizeLabel.setVisible(true);
            requiredDiskSpaceLabel.setVisible(true);
            
            final String componentsToInstallLabelText = component.getProperty(COMPONENTS_TO_BUNDLE_LABEL_TEXT_PROPERTY);
            componentsToBundleLabel.setText(componentsToInstallLabelText);
            
            final String componentsToInstallContentType = component.getProperty(COMPONENTS_TO_BUNDLE_CONTENT_TYPE_PROPERTY);
            componentsToBundlePane.setContentType(componentsToInstallContentType);
            
            final String componentsToInstallText = StringUtils.format(component.getProperty(COMPONENTS_TO_BUNDLE_TEXT_PROPERTY), StringUtils.asString(componentsToBundle, component.getProperty(COMPONENTS_LIST_SEPARATOR_PROPERTY)));
            componentsToBundlePane.setText(componentsToInstallText);
            
            long downloadSize = 0;
            for (Product component: componentsToBundle) {
                downloadSize += component.getDownloadSize();
            }
            
            long requiredDiskSpace = 0;
            for (Product component: componentsToBundle) {
                requiredDiskSpace += component.getRequiredDiskSpace();
            }
            
            final String downloadSizeLabelText = StringUtils.format(component.getProperty(DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY), StringUtils.formatSize(downloadSize));
            downloadSizeLabel.setText(downloadSizeLabelText);
            
            final String requiredDiskSpaceLabelText = StringUtils.format(component.getProperty(REQUIRED_DISK_SPACE_LABEL_TEXT_PROPERTY), StringUtils.formatSize(requiredDiskSpace));
            requiredDiskSpaceLabel.setText(requiredDiskSpaceLabelText);
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // messagePane //////////////////////////////////////////////////////////
            messagePane = new NbiTextPane();
            
            // componentsToBundlePane ///////////////////////////////////////////////
            componentsToBundlePane = new NbiTextPane();
            
            // componentsToBundleLabel //////////////////////////////////////////////
            componentsToBundleLabel = new NbiLabel();
            componentsToBundleLabel.setLabelFor(componentsToBundlePane);
            
            // downloadSizeLabel ////////////////////////////////////////////////////
            downloadSizeLabel = new NbiLabel();
            downloadSizeLabel.setFocusable(true);
            
            // requiredDiskSpaceLabel ///////////////////////////////////////////////
            requiredDiskSpaceLabel = new NbiLabel();
            requiredDiskSpaceLabel.setFocusable(true);
            
            // spacer ///////////////////////////////////////////////////////////////
            spacer = new NbiPanel();
            
            // this /////////////////////////////////////////////////////////////////
            add(messagePane, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(componentsToBundleLabel, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(15, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(componentsToBundlePane, new GridBagConstraints(
                    0, 3,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(3, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(downloadSizeLabel, new GridBagConstraints(
                    0, 4,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(25, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(requiredDiskSpaceLabel, new GridBagConstraints(
                    0, 5,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(3, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(spacer, new GridBagConstraints(
                    0, 6,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 11, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String MESSAGE_TEXT_PROPERTY =
            "message.text"; // NOI18N
    public static final String MESSAGE_CONTENT_TYPE_PROPERTY =
            "message.content.type"; // NOI18N
    public static final String COMPONENTS_TO_BUNDLE_LABEL_TEXT_PROPERTY =
            "components.to.bundle.label.text"; // NOI18N
    public static final String COMPONENTS_TO_BUNDLE_TEXT_PROPERTY =
            "components.to.bundle.text"; // NOI18N
    public static final String COMPONENTS_TO_BUNDLE_CONTENT_TYPE_PROPERTY =
            "components.to.bundle.content.type"; // NOI18N
    public static final String COMPONENTS_LIST_SEPARATOR_PROPERTY =
            "components.list.separator"; // NOI18N
    public static final String DOWNLOAD_SIZE_LABEL_TEXT_PROPERTY =
            "download.size.label.text"; // NOI18N
    public static final String REQUIRED_DISK_SPACE_LABEL_TEXT_PROPERTY =
            "required.disk.space.label.text"; // NOI18N
    
    public static final String DEFAULT_MESSAGE_TEXT =
            ResourceUtils.getString(PreCreateBundleSummaryPanel.class,
            "PrCBSPmessage.text"); // NOI18N
    public static final String DEFAULT_MESSAGE_CONTENT_TYPE =
            ResourceUtils.getString(PreCreateBundleSummaryPanel.class,
            "PrCBSPmessage.content.type"); // NOI18N
    public static final String DEFAULT_COMPONENTS_TO_BUNDLE_LABEL_TEXT =
            ResourceUtils.getString(PreCreateBundleSummaryPanel.class,
            "PrCBSPcomponents.to.bundle.label.text"); // NOI18N
    public static final String DEFAULT_COMPONENTS_TO_BUNDLE_TEXT =
            ResourceUtils.getString(PreCreateBundleSummaryPanel.class,
            "PrCBSPcomponents.to.bundle.text"); // NOI18N
    public static final String DEFAULT_COMPONENTS_TO_BUNDLE_CONTENT_TYPE =
            ResourceUtils.getString(PreCreateBundleSummaryPanel.class,
            "PrCBSPcomponents.to.bundle.content.type"); // NOI18N
    public static final String DEFAULT_COMPONENTS_LIST_SEPARATOR =
            ResourceUtils.getString(PreCreateBundleSummaryPanel.class,
            "PrCBSPcomponents.list.separator"); // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE_LABEL_TEXT =
            ResourceUtils.getString(PreCreateBundleSummaryPanel.class,
            "PrCBSPdownload.size.label.text"); // NOI18N
    public static final String DEFAULT_REQUIRED_DISK_SPACE_LABEL_TEXT =
            ResourceUtils.getString(PreCreateBundleSummaryPanel.class,
            "PrCBSPrequired.disk.space.label.text"); // NOI18N
    
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(PreCreateBundleSummaryPanel.class,
            "PrCBSPdialog.title"); // NOI18N
}
