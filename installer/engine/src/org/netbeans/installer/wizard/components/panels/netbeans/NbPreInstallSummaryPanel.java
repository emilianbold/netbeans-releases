/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.wizard.components.panels.netbeans;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.Installer;
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
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.swing.NbiCheckBox;
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
        
        private NbiCheckBox gfCheckbox;
        private NbiCheckBox tomcatCheckbox;
        private Product glassfishProduct;
        private Product tomcatProduct;
        
        private NbiLabel runtimesToRemove;
        
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
            boolean nbBasePresent = false;
            
            for (Product product: registry.getProductsToInstall()) {
                installationSize += product.getRequiredDiskSpace();
                downloadSize += product.getDownloadSize();
                
                try {
                    if (product.getLogic().registerInSystem() || product.getUid().equals("jdk")) {
                        nbBasePresent = product.getUid().equals("nb-base") ? true : nbBasePresent;
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
            
            
            File nbLocation = null;
            Product base = null;
            // If there are several packs to be installed but Base is already installed
            // then search it and the corresponding record to text
            if (dependentOnNb.size() > 0 && !nbBasePresent) {
                for(Product product : dependentOnNb) {
                    List <Dependency> bases = product.getDependencyByUid("nb-base");
                    if(!bases.isEmpty()) {
                        // dependency is already resolved at this point
                        base = Registry.getInstance().getProducts(bases.get(0)).get(0);

                        if(base!=null) {
                            nbLocation = base.getInstallationLocation();
                            try {
                                if(base.getLogic().wrapForMacOs() && SystemUtils.isMacOS()) {
                                    final File app = nbLocation.getParentFile().getParentFile().getParentFile();
                                    nbLocation = app;
                                }
                            } catch (InitializationException e){
                                LogManager.log(".. cannot get logic for " + base.getDisplayName() + " (" + base.getVersion() + ")", e);
                            } catch (NullPointerException e){
                                LogManager.log(".. cannot get app directory for " + nbLocation);
                            }
                            if(nbLocation!=null) {
                                text.append(StringUtils.LF);
                                text.append(StringUtils.format(
                                        panel.getProperty(INSTALLATION_FOLDER_NETBEANS_PROPERTY),
                                        base.getDisplayName()));
                                text.append(StringUtils.LF);
                                text.append("    " + nbLocation);
                                text.append(StringUtils.LF);
                            }
                            break;
                        }
                    }
                }
            }
            
            // add top-level components like nb-base, glassfish, tomcat, jdk
            for (Product product: registry.getProductsToInstall()) {
                try {
                    if (product.getLogic().registerInSystem() || product.getUid().equals("jdk")) {
                        String property = panel.getProperty(
                                product.getUid().equals("nb-base") ?
                                    INSTALLATION_FOLDER_NETBEANS_PROPERTY :
                                    INSTALLATION_FOLDER_PROPERTY);
                        text.append(StringUtils.format(property,
                                product.getDisplayName()));
                        text.append(StringUtils.LF);
                        text.append("    " + product.getInstallationLocation());
                        text.append(StringUtils.LF);
                    }
                } catch (InitializationException e) {
                    ErrorManager.notifyError(
                            panel.getProperty(ERROR_LOGIC_ACCESS_PROPERTY),e);
                }
            }
            // if we could not find nb-base location (very rare case) just mention all the packs to be installed
            if(!nbBasePresent && nbLocation == null && dependentOnNb.size() > 0) {
                text.append(StringUtils.LF);
                text.append(StringUtils.format(
                        panel.getProperty(NB_ADDONS_LOCATION_TEXT_PROPERTY),
                        StringUtils.asString(dependentOnNb)));
                text.append(StringUtils.LF);
            }
            // at the end add glassfish components record
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
            //if(gfCheckbox!=null) {
            //    gfCheckbox.doClick();
            //}
            //if(tomcatCheckbox!=null) {
            //    tomcatCheckbox.doClick();
            //}
            super.initialize();
        }
        
        @Override
        protected String validateInput() {
            try {
                if(!Boolean.getBoolean(SystemUtils.NO_SPACE_CHECK_PROPERTY)) {
                    final List<File> roots =
                            SystemUtils.getFileSystemRoots();
                    final List<Product> toInstall =
                            Registry.getInstance().getProductsToInstall();
                    final Map<File, Long> spaceMap =
                            new HashMap<File, Long>();
                    
                    LogManager.log("Available roots : " + StringUtils.asString(roots));
                    
                    File downloadDataDirRoot = FileUtils.getRoot(
                            Installer.getInstance().getLocalDirectory(), roots);
                    long downloadSize = 0;
                    for (Product product: toInstall) {
                        downloadSize+=product.getDownloadSize();
                    }
                    // the critical check point - we download all the data
                    spaceMap.put(downloadDataDirRoot, new Long(downloadSize));
                    long lastDataSize = 0;
                    for (Product product: toInstall) {
                        final File installLocation = product.getInstallationLocation();
                        final File root = FileUtils.getRoot(installLocation, roots);
                        final long productSize = product.getRequiredDiskSpace();
                        
                        LogManager.log("    [" + root + "] <- " + installLocation);
                        
                        if ( root != null ) {
                            Long ddSize =  spaceMap.get(downloadDataDirRoot);
                            // remove space that was freed after the remove of previos product data
                            spaceMap.put(downloadDataDirRoot,
                                    Long.valueOf(ddSize - lastDataSize));
                            
                            // add space required for next product installation
                            Long size = spaceMap.get(root);
                            size = Long.valueOf(
                                    (size != null ? size.longValue() : 0L) +
                                    productSize);
                            spaceMap.put(root, size);
                            lastDataSize = product.getDownloadSize();
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
            int index = 3;
            
            final String runtimesToRemoveText = ADDITIONAL_RUNTIMES_TO_DELETE;
            //final String removeSpecificRuntime = "<html>{0}";
            //final String removeSpecificRuntimeIncluding = "<html>{0} (including {1})";
            
            for(Product product : Registry.getInstance().getProductsToUninstall()) {
                if(product.getUid().equals("nb-base")) {
                    try {
                        File installLocation = product.getInstallationLocation();
                        
                        String gfLocation = NetBeansUtils.getJvmOption(
                                installLocation, GLASSFISH_JVM_OPTION_NAME);
                        String tomcatLocation = NetBeansUtils.getJvmOption(
                                installLocation, TOMCAT_JVM_OPTION_NAME_HOME);
                        if(gfLocation!=null) {
                            for(final Product gfProduct : Registry.getInstance().getProducts("glassfish")) {
                                if(gfProduct.getStatus() == Status.INSTALLED &&
                                        new File(gfLocation).equals(gfProduct.getInstallationLocation()))    {
                                    glassfishProduct = gfProduct;
                                    gfCheckbox = new NbiCheckBox();
                                    //List <Product> alsoRemoving = Registry.getInstance().getInavoidableDependents(gfProduct);
                                    //for(Product toUninstall : Registry.getInstance().getProductsToUninstall()) {
                                    //    alsoRemoving.remove(toUninstall);
                                    //}
                                    //final String text = (alsoRemoving.isEmpty()) ?
                                    //    StringUtils.format(removeSpecificRuntime, gfProduct.getDisplayName()) :
                                    //    StringUtils.format(removeSpecificRuntimeIncluding, gfProduct.getDisplayName(),
                                    //       StringUtils.asString(alsoRemoving));
                                    //gfCheckbox.setText(text);
                                    gfCheckbox.setText(gfProduct.getDisplayName());
                                    gfCheckbox.setBorder(new EmptyBorder(0,0,0,0));
                                    runtimesToRemove = new NbiLabel();
                                    runtimesToRemove.setText(StringUtils.format(runtimesToRemoveText,
                                            product.getLogic().getSystemDisplayName()));
                                    gfCheckbox.addActionListener(new ActionListener() {
                                        public void actionPerformed(ActionEvent e) {
                                            if(gfCheckbox.isSelected()) {
                                                gfProduct.setStatus(Status.TO_BE_UNINSTALLED);
                                            } else {
                                                gfProduct.setStatus(Status.INSTALLED);
                                            }
                                        }
                                    });
                                    
                                    add(runtimesToRemove, new GridBagConstraints(
                                            0, index++,                        // x, y
                                            1, 1,                             // width, height
                                            1.0, 0.0,                         // weight-x, weight-y
                                            GridBagConstraints.PAGE_START,    // anchor
                                            GridBagConstraints.HORIZONTAL,    // fill
                                            new Insets(0, 11, 0, 11),         // padding
                                            0, 0));                           // padx, pady - ???
                                    add(gfCheckbox, new GridBagConstraints(
                                            0, index++,                        // x, y
                                            1, 1,                             // width, height
                                            1.0, 0.0,                         // weight-x, weight-y
                                            GridBagConstraints.PAGE_START,    // anchor
                                            GridBagConstraints.HORIZONTAL,    // fill
                                            new Insets(0, 20, 0, 11),         // padding
                                            0, 0));                           // padx, pady - ???
                                    break;
                                }
                            }
                        }
                        if(tomcatLocation!=null) {
                            for(final Product ttProduct : Registry.getInstance().getProducts("tomcat")) {
                                if(ttProduct.getStatus() == Status.INSTALLED &&
                                        new File(tomcatLocation).equals(ttProduct.getInstallationLocation()))    {
                                    tomcatProduct = ttProduct;
                                    tomcatCheckbox = new NbiCheckBox();
                                    
                                    //List <Product> alsoRemoving = Registry.getInstance().getInavoidableDependents(tomcatProduct);
                                    //for(Product toUninstall : Registry.getInstance().getProductsToUninstall()) {
                                    //alsoRemoving.remove(toUninstall);
                                    //}
                                    //final String text = (alsoRemoving.isEmpty()) ?
                                    //    StringUtils.format(removeSpecificRuntime, tomcatProduct.getDisplayName()) :
                                    //    StringUtils.format(removeSpecificRuntimeIncluding, tomcatProduct.getDisplayName(),
                                    //        StringUtils.asString(alsoRemoving));
                                    //tomcatCheckbox.setText(text);
                                    tomcatCheckbox.setText(tomcatProduct.getDisplayName());
                                    
                                    tomcatCheckbox.setBorder(new EmptyBorder(0,0,0,0));
                                    if(runtimesToRemove==null) {
                                        runtimesToRemove = new NbiLabel();
                                        runtimesToRemove.setText(StringUtils.format(runtimesToRemoveText,
                                                product.getLogic().getSystemDisplayName()));
                                        
                                        add(runtimesToRemove, new GridBagConstraints(
                                                0, index++,                        // x, y
                                                1, 1,                             // width, height
                                                1.0, 0.0,                         // weight-x, weight-y
                                                GridBagConstraints.PAGE_START,    // anchor
                                                GridBagConstraints.HORIZONTAL,    // fill
                                                new Insets(0, 11, 0, 11),         // padding
                                                0, 0));                           // padx, pady - ???
                                    }
                                    
                                    tomcatCheckbox.addActionListener(new ActionListener() {
                                        public void actionPerformed(ActionEvent e) {
                                            if(tomcatCheckbox.isSelected()) {
                                                tomcatProduct.setStatus(Status.TO_BE_UNINSTALLED);
                                            } else {
                                                tomcatProduct.setStatus(Status.INSTALLED);
                                            }
                                        }
                                    });
                                    
                                    add(tomcatCheckbox, new GridBagConstraints(
                                            0, index++,                        // x, y
                                            1, 1,                             // width, height
                                            1.0, 0.0,                         // weight-x, weight-y
                                            GridBagConstraints.PAGE_START,    // anchor
                                            GridBagConstraints.HORIZONTAL,    // fill
                                            new Insets(0, 20, 0, 11),         // padding
                                            0, 0));                           // padx, pady - ???
                                    break;
                                }
                            }
                        }
                        
                    } catch (IOException e) {
                        LogManager.log(e);
                    }  catch (InitializationException e) {
                        LogManager.log(e);
                    }
                }
            }
            
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
        
        @Override
        public void evaluateNextButtonClick() {
            if(glassfishProduct!=null &&
                    glassfishProduct.getStatus()==Status.TO_BE_UNINSTALLED) {
                glassfishProduct.setStatus(Status.INSTALLED);
                List <Product> others = Registry.getInstance().getInavoidableDependents(glassfishProduct);
                for(Product pr : others) {
                    pr.setStatus(Status.TO_BE_UNINSTALLED);
                }
                glassfishProduct.setStatus(Status.TO_BE_UNINSTALLED);
            }
            if(tomcatProduct!=null &&
                    tomcatProduct.getStatus()==Status.TO_BE_UNINSTALLED) {
                tomcatProduct.setStatus(Status.INSTALLED);
                List <Product> others = Registry.getInstance().getInavoidableDependents(tomcatProduct);
                for(Product pr : others) {
                    pr.setStatus(Status.TO_BE_UNINSTALLED);
                }
                tomcatProduct.setStatus(Status.TO_BE_UNINSTALLED);
            }
            super.evaluateNextButtonClick();
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
    public static final String ADDITIONAL_RUNTIMES_TO_DELETE =
            ResourceUtils.getString(NbPreInstallSummaryPanel.class,
            "NPrISP.additional.runtimes.to.delete");//NOI18N
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
    public static final String GLASSFISH_JVM_OPTION_NAME =
            "-Dcom.sun.aas.installRoot"; // NOI18N
    public static final String TOMCAT_JVM_OPTION_NAME_HOME =
            "-Dorg.netbeans.modules.tomcat.autoregister.catalinaHome"; // NOI18N
}
