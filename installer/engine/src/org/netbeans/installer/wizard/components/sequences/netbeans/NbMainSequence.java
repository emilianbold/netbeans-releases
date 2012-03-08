/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.installer.wizard.components.sequences.netbeans;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipOutputStream;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.ExecutionMode;
import org.netbeans.installer.wizard.components.WizardAction;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.WizardSequence;
import org.netbeans.installer.wizard.components.actions.DownloadConfigurationLogicAction;
import org.netbeans.installer.wizard.components.actions.DownloadInstallationDataAction;
import org.netbeans.installer.wizard.components.actions.InstallAction;
import org.netbeans.installer.wizard.components.actions.UninstallAction;
import org.netbeans.installer.wizard.components.actions.netbeans.NbMetricsAction;
import org.netbeans.installer.wizard.components.actions.netbeans.NbShowUninstallationSurveyAction;
import org.netbeans.installer.wizard.components.panels.LicensesPanel;
import org.netbeans.installer.wizard.components.panels.netbeans.NbJUnitLicensePanel;
import org.netbeans.installer.wizard.components.panels.netbeans.NbPostInstallSummaryPanel;
import org.netbeans.installer.wizard.components.panels.netbeans.NbPreInstallSummaryPanel;
import org.netbeans.installer.wizard.components.sequences.ProductWizardSequence;

/**
 *
 * @author Kirill Sorokin
 * @author Dmitry Lipin
 */
public class NbMainSequence extends WizardSequence {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance

    private DownloadConfigurationLogicAction downloadConfigurationLogicAction;
    private LicensesPanel licensesPanel;
    private NbJUnitLicensePanel nbJUnitLicensePanel;
    private NbPreInstallSummaryPanel nbPreInstallSummaryPanel;
    private UninstallAction uninstallAction;
    private DownloadInstallationDataAction downloadInstallationDataAction;
    private InstallAction installAction;
    private NbPostInstallSummaryPanel nbPostInstallSummaryPanel;
    private NbMetricsAction metricsAction;
    private NbShowUninstallationSurveyAction showUninstallationSurveyAction;
    private Map<Product, ProductWizardSequence> productSequences;

    public NbMainSequence() {
        downloadConfigurationLogicAction = new DownloadConfigurationLogicAction();
        licensesPanel = new LicensesPanel();
        nbJUnitLicensePanel = new NbJUnitLicensePanel();
        nbPreInstallSummaryPanel = new NbPreInstallSummaryPanel();
        uninstallAction = new UninstallAction();
        downloadInstallationDataAction = new DownloadInstallationDataAction();
        installAction = new InstallAction();
        nbPostInstallSummaryPanel = new NbPostInstallSummaryPanel();
        metricsAction = new NbMetricsAction();
        showUninstallationSurveyAction = new NbShowUninstallationSurveyAction();
        productSequences = new HashMap<Product, ProductWizardSequence>();

        installAction.setProperty(InstallAction.TITLE_PROPERTY,
                DEFAULT_IA_TITLE);
        installAction.setProperty(InstallAction.DESCRIPTION_PROPERTY,
                DEFAULT_IA_DESCRIPTION);
    }
    
    private static class PupolateCacheAction extends WizardAction {
        private final Product nbBase;
        public PupolateCacheAction(Product p) {
            this.nbBase = p;
        }

        @Override
        public void execute() {
            LogManager.log("running headless NetBeans IDE : ");
            
            File nbInstallLocation = nbBase.getInstallationLocation();
            LogManager.log("    nbLocation = " + nbInstallLocation);
            
            String runIDE = new File(nbInstallLocation, "bin").getPath();
            if (SystemUtils.isWindows()) {
                runIDE += File.separator + "netbeans.exe";
            } else {
                runIDE += File.separator + "netbeans";
            }
            
            File tmpUserDir = new File(SystemUtils.getTempDirectory(), "tmpnb");
            File tmpCacheDir = new File(tmpUserDir, "var" + File.separator + "cache");
            
            String[] commands = new String [] {
                    runIDE,
                    "-J-Dnetbeans.close=true",
                    "--nosplash",
                    "-J-Dorg.netbeans.core.WindowSystem.show=false",
                    "--userdir",
                    tmpUserDir.getPath()};
            LogManager.log("    Run " + Arrays.asList(commands));
            try {
                SystemUtils.executeCommand(nbInstallLocation, commands);
                LogManager.log("    .... success ");
            } catch (IOException ioe) {
                LogManager.log("    .... exception ", ioe);
                return ;
            } finally {
                LogManager.log("    .... done. ");
            }
            
            LogManager.log("preparing caches : ");
            
            LogManager.log("    temporary cache location = " + tmpCacheDir);
            
            // zip OSGi cache
            try {
                LogManager.log("    zipping OSGi caches");
                File zipFile = new File(tmpCacheDir, "populate.zip");
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));            
                FileUtils.zip(new File (tmpCacheDir, "netigso"), zos, tmpCacheDir, new ArrayList <File> ());
                zos.close();
                LogManager.log("    .... success ");
            } catch (IOException ioe) {
                LogManager.log("    .... exception " + ioe.getMessage());
                return ;
            } finally {
                LogManager.log("    .... done. ");
            }
            
            // remove useless files
            try {
                LogManager.log("    remove useless files from cache");
                FileUtils.deleteFile(new File(tmpCacheDir, "netigso"), true);
                FileUtils.deleteFile(new File(tmpCacheDir, "lastModified"), true);
                
                String[] splashFileNames = tmpCacheDir.list(new FilenameFilter() {

                                    @Override
                                    public boolean accept(File dir, String name) {
                                        return name.startsWith("splash"); // NOI18N
                                    }
                                });
                
                if (splashFileNames != null) {
                    for (String name : splashFileNames) {
                        FileUtils.deleteFile(new File(tmpCacheDir, name));
                    }
                }
            } catch (IOException ioe) {
                LogManager.log("    .... exception " + ioe.getMessage());
                return ;
            } finally {
                LogManager.log("    .... done. ");
            }
            
            LogManager.log("copying pupulate caches : ");
            File populateCacheDir = new File(nbInstallLocation, "nb"/*nb clsuter*/ + File.separator + "var" + File.separator + "cache");
            LogManager.log("    pupulate cache location = " + populateCacheDir);
            try {
                FileUtils.copyFile(tmpCacheDir, populateCacheDir, true);
            } catch (IOException ioe) {
                LogManager.log("    .... exception " + ioe.getMessage());
                return ;
            } finally {
                LogManager.log("    .... done. ");
                try {
                    FileUtils.deleteFile(tmpUserDir, true);
                } catch (IOException ioe) {
                    LogManager.log("    .... exception " + ioe.getMessage());
                }
            }
            
            
            // adding files into list of installed files
            LogManager.log("add pupulate caches in installed list: ");
            try {
                for (File f : populateCacheDir.listFiles()) {
                    nbBase.getInstalledFiles().add(f);
                }
            } catch (IOException ioe) {
                LogManager.log("    .... exception " + ioe.getMessage());
            } finally {
                LogManager.log("    .... done. ");
            }
        }
        
    }
    
    @Override
    public void executeForward() {
        final Registry registry = Registry.getInstance();
        final List<Product> toInstall = registry.getProductsToInstall();
        final List<Product> toUninstall = registry.getProductsToUninstall();

        // remove all current children (if there are any), as the components
        // selection has probably changed and we need to rebuild from scratch
        getChildren().clear();

        // the set of wizard components differs greatly depending on the execution
        // mode - if we're installing, we ask for input, run a wizard sequence for
        // each selected component and then download and install; if we're creating
        // a bundle, we only need to download and package things

        if (toInstall.size() > 0) {
            addChild(downloadConfigurationLogicAction);
            addChild(licensesPanel);
            addChild(nbJUnitLicensePanel);

            for (Product product : toInstall) {
                if (!productSequences.containsKey(product)) {
                    productSequences.put(
                            product,
                            new ProductWizardSequence(product));
                }

                addChild(productSequences.get(product));
            }
        }

        addChild(nbPreInstallSummaryPanel);

        if (toUninstall.size() > 0) {
            addChild(uninstallAction);
        }

        if (toInstall.size() > 0) {
            addChild(downloadInstallationDataAction);
            addChild(installAction);
            Product nbBase = toInstall.get(0);
            if ("nb-base".equals(nbBase.getUid())) { // NOI18N
                addChild(new PupolateCacheAction(nbBase));
            }
        }

        addChild(nbPostInstallSummaryPanel);
        if (toInstall.size() > 0) {
            addChild(metricsAction);
        }
        if (toUninstall.size() > 0) {
            addChild(showUninstallationSurveyAction);
        }

        StringBuilder list = new StringBuilder();
        for (Product product : toInstall) {
            list.append(product.getUid() + "," + product.getVersion() + ";");
        }
        System.setProperty(
                LIST_OF_PRODUCTS_TO_INSTALL_PROPERTY,
                list.toString());

        list = new StringBuilder();
        for (Product product : toUninstall) {
            list.append(product.getUid() + "," + product.getVersion() + ";");
        }
        System.setProperty(
                LIST_OF_PRODUCTS_TO_UNINSTALL_PROPERTY,
                list.toString());

        list = new StringBuilder();
        for (Product product : toInstall) {
            for (WizardComponent component : productSequences.get(product).getChildren()) {
                list.append(component.getClass().getName() + ";");
            }
        }
        System.setProperty(
                PRODUCTS_PANEL_FLOW_PROPERTY,
                list.toString());

        super.executeForward();
    }

    @Override
    public boolean canExecuteForward() {
        return ExecutionMode.NORMAL == ExecutionMode.getCurrentExecutionMode();
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_IA_TITLE =
            ResourceUtils.getString(
            NbMainSequence.class,
            "NBMS.IA.title"); // NOI18N
    public static final String DEFAULT_IA_DESCRIPTION =
            ResourceUtils.getString(
            NbMainSequence.class,
            "NBMS.IA.description"); // NOI18N
    
    public static final String LIST_OF_PRODUCTS_TO_INSTALL_PROPERTY =
            "nbi.products.to.install"; // NOI18N
    public static final String LIST_OF_PRODUCTS_TO_UNINSTALL_PROPERTY =
            "nbi.products.to.uninstall"; // NOI18N
    public static final String PRODUCTS_PANEL_FLOW_PROPERTY =
            "nbi.products.panel.flow"; // NOI18N
}
