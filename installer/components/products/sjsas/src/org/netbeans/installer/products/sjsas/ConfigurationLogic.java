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
package org.netbeans.installer.products.sjsas;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.utils.applications.GlassFishUtils;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.panels.JdkLocationPanel;
import org.netbeans.installer.products.sjsas.wizard.panels.ASPanel;
import org.netbeans.installer.utils.FileProxy;

/**
 *
 * @author Kirill Sorokin
 */
public class ConfigurationLogic extends ProductConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private List<WizardComponent> wizardComponents;
    
    // constructor //////////////////////////////////////////////////////////////////
    public ConfigurationLogic() throws InitializationException {
        wizardComponents = Wizard.loadWizardComponents(
                WIZARD_COMPONENTS_URI,
                getClass().getClassLoader());
    }
    
    // configuration logic implementation ///////////////////////////////////////////
    public void install(final Progress progress)
            throws InstallationException {
        final File directory = getProduct().getInstallationLocation();
        
        final String username  = getProperty(ASPanel.USERNAME_PROPERTY);
        final String password  = getProperty(ASPanel.PASSWORD_PROPERTY);
        final String httpPort  = getProperty(ASPanel.HTTP_PORT_PROPERTY);
        final String httpsPort = getProperty(ASPanel.HTTPS_PORT_PROPERTY);
        final String adminPort = getProperty(ASPanel.ADMIN_PORT_PROPERTY);
        
        final File javaHome =
                new File(getProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY));
        
        final FilesList list = getProduct().getInstalledFiles();
        
        /////////////////////////////////////////////////////////////////////////////
        
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.replace.tokens")); // NOI18N
            
            final Map<String, Object> map = new HashMap<String, Object>();
            
            map.put(INSTALL_HOME_TOKEN, directory);
            map.put(INSTALL_HOME_F_TOKEN, directory.getPath().replace(StringUtils.BACK_SLASH, StringUtils.FORWARD_SLASH));
            
            map.put(JAVA_HOME_TOKEN,  javaHome);
            map.put(JAVA_HOME_F_TOKEN, javaHome.getPath().replace(StringUtils.BACK_SLASH, StringUtils.FORWARD_SLASH));
            
            map.put(HOST_NAME_TOKEN,  SystemUtils.getHostName());
            map.put(ADMIN_USERNAME_TOKEN, username);
            map.put(HTTP_PORT_TOKEN,httpPort);
            map.put(ADMIN_PORT_TOKEN,adminPort);
            
            FileUtils.modifyFile(new File(directory, BIN_SUBDIR),map);
            FileUtils.modifyFile(new File(directory, CONFIG_SUBDIR),map);
            FileUtils.modifyFile(new File(directory, DOCS_SUBDIR),map);
            FileUtils.modifyFile(new File(directory, IMQ_SUBDIR), map);
            FileUtils.modifyFile(new File(directory, JBI_SUBDIR),map);
            FileUtils.modifyFile(new File(directory, DERBY_SUBDIR),map);
            FileUtils.modifyFile(new File(directory, SAMPLES_SUBDIR), map);
            FileUtils.modifyFile(new File(directory, BLUEPRINTS_SUBDIR), map);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.replace.tokens"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.irrelevant.files")); // NOI18N
            
            SystemUtils.removeIrrelevantFiles(directory);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.irrelevant.files"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.files.permissions")); // NOI18N
            
            SystemUtils.correctFilesPermissions(directory);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.files.permissions"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.create.domain")); // NOI18N
            
            GlassFishUtils.createDomain(
                    directory,
                    DOMAIN_NAME,
                    username,
                    password,
                    httpPort,
                    httpsPort,
                    adminPort);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.create.domain"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.extra.files")); // NOI18N
            list.add(new File(directory, DERBY_LOG));
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.extra.files"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
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
                                JVM_OPTION_NAME,
                                directory.getAbsolutePath(),
                                true);
                    }
                }
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.ide.integration"),  // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public void uninstall(final Progress progress)
            throws UninstallationException {
        File directory = getProduct().getInstallationLocation();
        
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
                                JVM_OPTION_NAME);
                        
                        if ((value != null) &&
                                (value.equals(directory.getAbsolutePath()))) {
                            NetBeansUtils.removeJvmOption(
                                    nbLocation,
                                    JVM_OPTION_NAME);
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
        try {
            progress.setDetail(getString("CL.uninstall.delete.domain")); // NOI18N
            
            GlassFishUtils.deleteDomain(directory, DOMAIN_NAME);
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.delete.domain"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }
    
    @Override
    public boolean allowModifyMode() {
        return false;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String WIZARD_COMPONENTS_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX + // NOI18N
            "org/netbeans/installer/products/sjsas/wizard.xml"; // NOI18N
    
    public static final String DOMAIN_NAME =
            "domain1"; // NOI18N
    public static final String CONFIG_SUBDIR =
            "config"; // NOI18N
    public static final String LIB_SUBDIR =
            "lib"; // NOI18N
    public static final String IMQ_SUBDIR =
            "imq"; // NOI18N
    public static final String DOMAINS_SUBDIR =
            "domains"; // NOI18N
    public static final String DERBY_SUBDIR =
            "javadb"; // NOI18N
    public static final String UC_INSTALL_HOME_SUBDIR =
            "updatecenter"; //NOI18N
    public static final String BIN_SUBDIR =
            "bin"; // NOI18N
    public static final String DOCS_SUBDIR =
            "docs"; // NOI18N
    public static final String JBI_SUBDIR =
            "jbi"; // NOI18N
    public static final String SAMPLES_SUBDIR =
            "samples"; // NOI18N
    public static final String BLUEPRINTS_SUBDIR =
            "blueprints"; // NOI18N
    
    public static final String INSTALL_HOME_TOKEN =
            "%INSTALL_HOME%"; // NOI18N
    public static final String INSTALL_HOME_F_TOKEN =
            "%INSTALL_HOME_F%"; // NOI18N
    public static final String JAVA_HOME_TOKEN =
            "%JAVA_HOME%"; // NOI18N
    public static final String JAVA_HOME_F_TOKEN =
            "%JAVA_HOME_F%"; // NOI18N
    public static final String HTTP_PORT_TOKEN =
            "%HTTP_PORT%"; //NOI18N
    public static final String ADMIN_PORT_TOKEN =
            "%ADMIN_PORT%"; //NOI18N
    public static final String HOST_NAME_TOKEN =
            "%HOST_NAME%";//N0I18N
    public static final String ADMIN_USERNAME_TOKEN =
            "%ADMIN_USER_NAME%";//N0I18N
    
    public static final String DERBY_LOG =
            "derby.log"; // NOI18N
    
    public static final String JVM_OPTION_NAME =
            "-Dcom.sun.aas.installRoot"; // NOI18N
}
