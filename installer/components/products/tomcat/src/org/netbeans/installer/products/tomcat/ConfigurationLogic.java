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

package org.netbeans.installer.products.tomcat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;

/**
 *
 * @author Kirill Sorokin
 */
public class ConfigurationLogic extends ProductConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private List<WizardComponent> wizardComponents;
    
    public ConfigurationLogic() throws InitializationException {
        wizardComponents = Wizard.loadWizardComponents(
                WIZARD_COMPONENTS_URI,
                getClass().getClassLoader());
    }
    
    public void install(
            final Progress progress) throws InstallationException {
        final File location = getProduct().getInstallationLocation();
        
        /////////////////////////////////////////////////////////////////////////////
        //try {
        //    progress.setDetail(getString("CL.install.irrelevant.files")); // NOI18N
        //    
        //    SystemUtils.removeIrrelevantFiles(location);
        //} catch (IOException e) {
        //    throw new InstallationException(
        //            getString("CL.install.error.irrelevant.files"), // NOI18N
        //            e);
        //}
        
        /////////////////////////////////////////////////////////////////////////////
//        try {
//            progress.setDetail(getString("CL.install.files.permissions")); // NOI18N
//            
//            SystemUtils.correctFilesPermissions(location);
//        } catch (IOException e) {
//            throw new InstallationException(
//                    getString("CL.install.error.files.permissions"), // NOI18N
//                    e);
//        }
        
        /////////////////////////////////////////////////////////////////////////////
        // Reference: http://wiki.netbeans.org/wiki/view/TomcatAutoRegistration
        try {
            progress.setDetail(getString("CL.install.ide.integration")); // NOI18N
            
            final List<Product> ides = 
                    Registry.getInstance().getProducts("nb-base");
            for (Product ide: ides) {
                if (ide.getStatus() == Status.INSTALLED) {
                    final File nbLocation = ide.getInstallationLocation();
                    
                    if (nbLocation != null) {
                        NetBeansUtils.setJvmOption(
                                nbLocation,
                                JVM_OPTION_AUTOREGISTER_HOME_NAME,
                                location.getAbsolutePath(),
                                true);
                        NetBeansUtils.setJvmOption(
                                nbLocation,
                                JVM_OPTION_AUTOREGISTER_TOKEN_NAME,
                                Long.toString(System.currentTimeMillis()),
                                false);
                        
                        // if the IDE was installed in the same session as the
                        // appserver, we should add its "product id" to the IDE
                        if (ide.hasStatusChanged()) {
                            NetBeansUtils.addPackId(
                                    nbLocation,
                                    PRODUCT_ID);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.ide.integration"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public void uninstall(
            final Progress progress) throws UninstallationException {
        final File location = getProduct().getInstallationLocation();
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.uninstall.ide.integration")); // NOI18N
            
            final List<Product> ides =
                    Registry.getInstance().getProducts("nb-base");
            for (Product ide: ides) {
                if (ide.getStatus() == Status.INSTALLED) {
                    final File nbLocation = ide.getInstallationLocation();
                    
                    if (nbLocation != null) {
                        final String value = NetBeansUtils.getJvmOption(
                                nbLocation,
                                JVM_OPTION_AUTOREGISTER_HOME_NAME);
                        
                        if ((value != null) &&
                                (value.equals(location.getAbsolutePath()))) {
                            NetBeansUtils.removeJvmOption(
                                    nbLocation,
                                    JVM_OPTION_AUTOREGISTER_HOME_NAME);
                            NetBeansUtils.removeJvmOption(
                                    nbLocation,
                                    JVM_OPTION_AUTOREGISTER_TOKEN_NAME);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.ide.integration"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public List<WizardComponent> getWizardComponents(
            ) {
        return wizardComponents;
    }
    
    @Override
    public String getIcon() {
        if (SystemUtils.isWindows()) {
            return "bin/tomcat6.exe";
        } else {
            return null;
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private void integrateWithIDE(
            final Progress progress, 
            final File directory)  throws InstallationException {
        /////////////////////////////////////////////////////////////////////////////
        // Reference: http://wiki.netbeans.org/wiki/view/TomcatAutoRegistration
        try {
            progress.setDetail(getString("CL.install.ide.integration")); // NOI18N
            
            List<Product> ides = Registry.getInstance().getProducts("nb-ide");
            for (Product ide: ides) {
                if (ide.getStatus() == Status.INSTALLED) {
                    File nbLocation = ide.getInstallationLocation();
                    
                    if (nbLocation != null) {
                        NetBeansUtils.setJvmOption(
                                nbLocation,
                                JVM_OPTION_AUTOREGISTER_HOME_NAME,
                                directory.getAbsolutePath(),
                                true);
                        NetBeansUtils.setJvmOption(
                                nbLocation,
                                JVM_OPTION_AUTOREGISTER_TOKEN_NAME,
                                "" + System.currentTimeMillis(),
                                false);
                    }
                }
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.ide.integration"), // NOI18N
                    e);
        }
    }
    @Override
    public boolean allowModifyMode() {
        return false;
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String WIZARD_COMPONENTS_URI =
            "resource:" + // NOI18N
            "org/netbeans/installer/products/tomcat/wizard.xml"; // NOI18N
    
    public static final String JVM_OPTION_AUTOREGISTER_TOKEN_NAME =
            "-Dorg.netbeans.modules.tomcat.autoregister.token"; // NOI18N
    
    public static final String JVM_OPTION_AUTOREGISTER_HOME_NAME =
            "-Dorg.netbeans.modules.tomcat.autoregister.catalinaHome"; // NOI18N
    
    public static final String PRODUCT_ID = 
            "TOMCAT"; // NOI18N
}
