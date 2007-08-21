/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.wizard.components.panels.netbeans;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.RegistryType;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelSwingUi;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 *
 * @author Kirill Sorokin
 */
public class NbPreInstallSummaryPanel extends ErrorMessagePanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public NbPreInstallSummaryPanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(INSTALLATION_FOLDER_PROPERTY,
                DEFAULT_INSTALLATION_FOLDER);
        setProperty(INSTALLATION_FOLDER_NETBEANS_PROPERTY,
                DEFAULT_INSTALLATION_FOLDER_NETBEANS);
        setProperty(UNINSTALL_LIST_LABEL_TEXT_PROPERTY,
                DEFAULT_UNINSTALL_LIST_LABEL_TEXT);
        setProperty(INSTALLATION_SIZE_PROPERTY,
                DEFAULT_INSTALLATION_SIZE);
        setProperty(DOWNLOAD_SIZE_PROPERTY,
                DEFAULT_DOWNLOAD_SIZE);
        setProperty(NB_ADDONS_LOCATION_TEXT_PROPERTY,
                DEFAULT_NB_ADDONS_LOCATION_TEXT);
        setProperty(GF_ADDONS_LOCATION_TEXT_PROPERTY,
                DEFAULT_GF_ADDONS_LOCATION_TEXT);
        
        setProperty(NEXT_BUTTON_TEXT_PROPERTY,
                DEFAULT_NEXT_BUTTON_TEXT);
        
        setProperty(ERROR_NOT_ENOUGH_SPACE_PROPERTY,
                DEFAULT_ERROR_NOT_ENOUGH_SPACE);
        setProperty(ERROR_CANNOT_CHECK_SPACE_PROPERTY,
                DEFAULT_ERROR_CANNOT_CHECK_SPACE);
        setProperty(ERROR_LOGIC_ACCESS_PROPERTY,
                DEFAULT_ERROR_LOGIC_ACCESS);
        setProperty(ERROR_FSROOTS_PROPERTY,
                DEFAULT_ERROR_FSROOTS);
        setProperty(ERROR_NON_EXISTENT_ROOT_PROPERTY,
                DEFAULT_ERROR_NON_EXISTENT_ROOT);
        setProperty(ERROR_CANNOT_WRITE_PROPERTY,
                DEFAULT_ERROR_CANNOT_WRITE);
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new NbPreInstallSummaryPanelUi(this);
        }
        
        return wizardUi;
    }
    
    @Override
    public void initialize() {
        final List<Product> toInstall =
                Registry.getInstance().getProductsToInstall();
        
        if (toInstall.size() > 0) {
            setProperty(NEXT_BUTTON_TEXT_PROPERTY, DEFAULT_NEXT_BUTTON_TEXT);
            setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
        } else {
            setProperty(NEXT_BUTTON_TEXT_PROPERTY, DEFAULT_NEXT_BUTTON_TEXT_UNINSTALL);
            setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION_UNINSTALL);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class NbPreInstallSummaryPanelUi extends ErrorMessagePanelUi {
        protected NbPreInstallSummaryPanel component;
        
        public NbPreInstallSummaryPanelUi(NbPreInstallSummaryPanel component) {
            super(component);
            
            this.component = component;
        }
        
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new NbPreInstallSummaryPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class NbPreInstallSummaryPanelSwingUi extends ErrorMessagePanelSwingUi {
        protected NbPreInstallSummaryPanel component;
        
        private NbiTextPane locationsPane;
        
        private NbiLabel uninstallListLabel;
        private NbiTextPane uninstallListPane;
        
        private NbiLabel installationSizeLabel;
        private NbiLabel installationSizeValue;
        
        private NbiLabel downloadSizeLabel;
        private NbiLabel downloadSizeValue;
        
        private NbiPanel spacer;
        
        public NbPreInstallSummaryPanelSwingUi(
                final NbPreInstallSummaryPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            
            initComponents();
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initializeContainer() {
            super.initializeContainer();
            
            container.getNextButton().setText(
                    panel.getProperty(NEXT_BUTTON_TEXT_PROPERTY));
        }
        
        @Override
        protected void initialize() {
            final Registry registry = Registry.getInstance();
            
            final StringBuilder text = new StringBuilder();
            long installationSize = 0;
            long downloadSize = 0;
            
            final List<Product> dependentOnNb = new LinkedList<Product>();
            final List<Product> dependentOnGf = new LinkedList<Product>();
            for (Product product: registry.getProductsToInstall()) {
                installationSize += product.getRequiredDiskSpace();
                downloadSize += product.getDownloadSize();
                
                try {
                    
                    if (product.getLogic().registerInSystem()) {
                        String property = panel.getProperty(
                                product.getUid().equals("nb-base") ?
                                    INSTALLATION_FOLDER_NETBEANS_PROPERTY :
                                    INSTALLATION_FOLDER_PROPERTY);
                        
                        text.append(StringUtils.format(property,                                
                                product.getDisplayName()));
                        text.append(StringUtils.LF);
                        text.append("    " + product.getInstallationLocation());
                        text.append(StringUtils.LF);
                    } else {
                        if (product.getUid().startsWith("nb-")) {
                            dependentOnNb.add(product);
                        } else {
                            dependentOnGf.add(product);
                        }
                    }
                } catch (InitializationException e) {
                    ErrorManager.notifyError(
                            panel.getProperty(ERROR_LOGIC_ACCESS_PROPERTY),e);
                }
            }
            
            if (dependentOnNb.size() > 0) {
                text.append(StringUtils.LF);
                text.append(StringUtils.format(
                        panel.getProperty(NB_ADDONS_LOCATION_TEXT_PROPERTY),
                        StringUtils.asString(dependentOnNb)));
                text.append(StringUtils.LF);
            }
            if (dependentOnGf.size() > 0) {
                text.append(StringUtils.LF);
                text.append(StringUtils.format(
                        panel.getProperty(GF_ADDONS_LOCATION_TEXT_PROPERTY),
                        StringUtils.asString(dependentOnGf)));
                text.append(StringUtils.LF);
            }
            
            locationsPane.setText(text);
            
            uninstallListLabel.setText(
                    panel.getProperty(UNINSTALL_LIST_LABEL_TEXT_PROPERTY));
            uninstallListPane.setText(
                    StringUtils.asString(registry.getProductsToUninstall()));
            
            installationSizeLabel.setText(
                    panel.getProperty(INSTALLATION_SIZE_PROPERTY));
            installationSizeValue.setText(StringUtils.formatSize(
                    installationSize));
            
            downloadSizeLabel.setText(
                    panel.getProperty(DOWNLOAD_SIZE_PROPERTY));
            downloadSizeValue.setText(StringUtils.formatSize(
                    downloadSize));
            
            if (registry.getProductsToInstall().size() == 0) {
                locationsPane.setVisible(false);
                installationSizeLabel.setVisible(false);
                installationSizeValue.setVisible(false);
            } else {
                locationsPane.setVisible(true);
                installationSizeLabel.setVisible(true);
                installationSizeValue.setVisible(true);
            }
            
            if (registry.getProductsToUninstall().size() == 0) {
                uninstallListLabel.setVisible(false);
                uninstallListPane.setVisible(false);
            } else {
                uninstallListLabel.setVisible(true);
                uninstallListPane.setVisible(true);
            }
            
            downloadSizeLabel.setVisible(false);
            downloadSizeValue.setVisible(false);
            for (RegistryNode remoteNode: registry.getNodes(RegistryType.REMOTE)) {
                if (remoteNode.isVisible()) {
                    downloadSizeLabel.setVisible(true);
                    downloadSizeValue.setVisible(true);
                }
            }
            
            super.initialize();
        }
        
        @Override
        protected String validateInput() {
            try {
                final List<File> roots =
                        SystemUtils.getFileSystemRoots();
                final List<Product> toInstall =
                        Registry.getInstance().getProductsToInstall();
                final Map<File, Long> spaceMap =
                        new HashMap<File, Long>();
                
                LogManager.log("Available roots : " + StringUtils.asString(roots));
                
                for (Product product: toInstall) {
                    final File installLocation = product.getInstallationLocation();
                    final File root = FileUtils.getRoot(installLocation, roots);
                    final long productSize = product.getRequiredDiskSpace();
                    
                    LogManager.log("    [" + root + "] <- " + installLocation);
                    
                    if ( root != null ) {
                        Long size = spaceMap.get(root);
                        size = Long.valueOf(
                                (size != null ? size.longValue() : 0L) +
                                productSize);
                        spaceMap.put(root, size);
                    } else {
                        return StringUtils.format(
                                panel.getProperty(ERROR_NON_EXISTENT_ROOT_PROPERTY),
                                product, installLocation);
                    }
                }
                
                for (File root: spaceMap.keySet()) {
                    try {
                        final long availableSpace =
                                SystemUtils.getFreeSpace(root);
                        final long requiredSpace =
                                spaceMap.get(root) + REQUIRED_SPACE_ADDITION;
                        
                        if (availableSpace < requiredSpace) {
                            return StringUtils.format(
                                    panel.getProperty(ERROR_NOT_ENOUGH_SPACE_PROPERTY),
                                    root,
                                    StringUtils.formatSize(requiredSpace - availableSpace));
                        }
                    } catch (NativeException e) {
                        ErrorManager.notifyError(
                                panel.getProperty(ERROR_CANNOT_CHECK_SPACE_PROPERTY),
                                e);
                    }
                }
                
                final List<Product> toUninstall =
                        Registry.getInstance().getProductsToUninstall();
                for (Product product: toUninstall) {
                    if (!FileUtils.canWrite(product.getInstallationLocation())) {
                        return StringUtils.format(
                                panel.getProperty(ERROR_CANNOT_WRITE_PROPERTY),
                                product,
                                product.getInstallationLocation());
                    }
                }
                
            } catch (IOException e) {
                ErrorManager.notifyError(
                        panel.getProperty(ERROR_FSROOTS_PROPERTY), e);
            }
            
            return null;
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // locationsPane ////////////////////////////////////////////////////////
            locationsPane = new NbiTextPane();
            
            // uninstallListPane ////////////////////////////////////////////////////
            uninstallListPane = new NbiTextPane();
            
            // uninstallListLabel ///////////////////////////////////////////////////
            uninstallListLabel = new NbiLabel();
            uninstallListLabel.setLabelFor(uninstallListPane);
            
            // installationSizeValue ////////////////////////////////////////////////
            installationSizeValue = new NbiLabel();
            installationSizeValue.setFocusable(true);
            
            // installationSizeLabel ////////////////////////////////////////////////
            installationSizeLabel = new NbiLabel();
            installationSizeLabel.setLabelFor(installationSizeValue);
            
            // downloadSizeValue ////////////////////////////////////////////////////
            downloadSizeValue = new NbiLabel();
            downloadSizeValue.setFocusable(true);
            
            // downloadSizeLabel ////////////////////////////////////////////////////
            downloadSizeLabel = new NbiLabel();
            downloadSizeLabel.setLabelFor(downloadSizeValue);
            
            // spacer ///////////////////////////////////////////////////////////////
            spacer = new NbiPanel();
            
            // this /////////////////////////////////////////////////////////////////
            add(locationsPane, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(uninstallListLabel, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(uninstallListPane, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(0, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(installationSizeLabel, new GridBagConstraints(
                    0, 6,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(22, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(installationSizeValue, new GridBagConstraints(
                    0, 7,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 22, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(downloadSizeLabel, new GridBagConstraints(
                    0, 8,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(downloadSizeValue, new GridBagConstraints(
                    0, 9,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 22, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            add(spacer, new GridBagConstraints(
                    0, 25,                            // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(0, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String INSTALLATION_FOLDER_PROPERTY =
            "installation.folder"; // NOI18N
    public static final String INSTALLATION_FOLDER_NETBEANS_PROPERTY =
            "installation.folder.netbeans"; // NOI18N
    public static final String UNINSTALL_LIST_LABEL_TEXT_PROPERTY =
            "uninstall.list.label.text"; // NOI18N
    public static final String INSTALLATION_SIZE_PROPERTY =
            "installation.size"; // NOI18N
    public static final String DOWNLOAD_SIZE_PROPERTY =
            "download.size"; // NOI18N
    public static final String NB_ADDONS_LOCATION_TEXT_PROPERTY =
            "addons.nb.install.location.text"; // NOI18N
    public static final String GF_ADDONS_LOCATION_TEXT_PROPERTY =
            "addons.gf.install.location.text"; // NOI18N
    
    public static final String ERROR_NOT_ENOUGH_SPACE_PROPERTY =
            "error.not.enough.space"; // NOI18N
    public static final String ERROR_CANNOT_CHECK_SPACE_PROPERTY =
            "error.cannot.check.space"; // NOI18N
    public static final String ERROR_LOGIC_ACCESS_PROPERTY =
            "error.logic.access"; // NOI18N
    public static final String ERROR_FSROOTS_PROPERTY =
            "error.fsroots"; // NOI18N
    public static final String ERROR_NON_EXISTENT_ROOT_PROPERTY =
            "error.non.existent.root"; // NOI18N
    public static final String ERROR_CANNOT_WRITE_PROPERTY =
            "error.cannot.write"; // NOI18N
    
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.description"); // NOI18N
    public static final String DEFAULT_DESCRIPTION_UNINSTALL =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.description.uninstall"); // NOI18N
    
    public static final String DEFAULT_INSTALLATION_FOLDER =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.installation.folder"); // NOI18N
    public static final String DEFAULT_INSTALLATION_FOLDER_NETBEANS =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.installation.folder.netbeans"); // NOI18N
    public static final String DEFAULT_UNINSTALL_LIST_LABEL_TEXT =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.uninstall.list.label.text"); // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.installation.size"); // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.download.size"); // NOI18N
    public static final String DEFAULT_GF_ADDONS_LOCATION_TEXT =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.addons.gf.install.location.text"); // NOI18N
    public static final String DEFAULT_NB_ADDONS_LOCATION_TEXT =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.addons.nb.install.location.text"); // NOI18N
    
    public static final String DEFAULT_NEXT_BUTTON_TEXT =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.next.button.text"); // NOI18N
    public static final String DEFAULT_NEXT_BUTTON_TEXT_UNINSTALL =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.next.button.text.uninstall"); // NOI18N
    
    public static final String DEFAULT_ERROR_NOT_ENOUGH_SPACE =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.error.not.enough.space"); // NOI18N
    public static final String DEFAULT_ERROR_CANNOT_CHECK_SPACE =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.error.cannot.check.space");// NOI8N
    public static final String DEFAULT_ERROR_LOGIC_ACCESS =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.error.logic.access");// NOI18N
    public static final String DEFAULT_ERROR_FSROOTS =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.error.fsroots"); // NOI18N
    public static final String DEFAULT_ERROR_NON_EXISTENT_ROOT =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.error.non.existent.root"); // NOI18N
    public static final String DEFAULT_ERROR_CANNOT_WRITE =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.error.cannot.write"); // NOI18N
    
    public static final long REQUIRED_SPACE_ADDITION =
            10L * 1024L * 1024L; // 10MB
}
