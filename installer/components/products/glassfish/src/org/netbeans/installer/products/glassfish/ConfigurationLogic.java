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

package org.netbeans.installer.products.glassfish;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.utils.applications.GlassFishUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
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
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.panels.JdkLocationPanel;
import org.netbeans.installer.products.glassfish.wizard.panels.GlassFishPanel;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.applications.JavaUtils.JavaInfo;

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
        
        final String username = getProperty(GlassFishPanel.USERNAME_PROPERTY);
        final String password = getProperty(GlassFishPanel.PASSWORD_PROPERTY);
        final String httpPort = getProperty(GlassFishPanel.HTTP_PORT_PROPERTY);
        final String httpsPort = getProperty(GlassFishPanel.HTTPS_PORT_PROPERTY);
        final String adminPort = getProperty(GlassFishPanel.ADMIN_PORT_PROPERTY);
        
        final File javaHome =
                new File(getProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY));
        JavaInfo info = JavaUtils.getInfo(javaHome);
        LogManager.log("Using the following JDK for GlassFish configuration : ");
        LogManager.log("... path    : "  + javaHome);
        LogManager.log("... version : "  + info.getVersion().toJdkStyle());
        LogManager.log("... vendor  : "  + info.getVendor());
        LogManager.log("... final   : "  + (!info.isNonFinal()));
        
        final FilesList list = getProduct().getInstalledFiles();
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.copy.files")); // NOI18N
            
            if (SystemUtils.isWindows()) {
                list.add(FileUtils.copyFile(
                        new File(directory, ASENV_BAT_TEMPLATE),
                        new File(directory, ASENV_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, ASADMINENV_CONF_TEMPLATE),
                        new File(directory, ASADMINENV_CONF)));
                list.add(FileUtils.copyFile(
                        new File(directory, ASADMIN_BAT_TEMPLATE),
                        new File(directory, ASADMIN_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, ASANT_BAT_TEMPLATE),
                        new File(directory, ASANT_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, APPCLIENT_BAT_TEMPLATE),
                        new File(directory, APPCLIENT_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, JSPC_BAT_TEMPLATE),
                        new File(directory, JSPC_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, PACKAGE_APPCLIENT_BAT_TEMPLATE),
                        new File(directory, PACKAGE_APPCLIENT_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, VERIFIER_BAT_TEMPLATE),
                        new File(directory, VERIFIER_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, ASUPGRADE_BAT_TEMPLATE),
                        new File(directory, ASUPGRADE_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, CAPTURE_SCHEMA_BAT_TEMPLATE),
                        new File(directory, CAPTURE_SCHEMA_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, WSIMPORT_BAT_TEMPLATE),
                        new File(directory, WSIMPORT_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, WSGEN_BAT_TEMPLATE),
                        new File(directory, WSGEN_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, SCHEMAGEN_BAT_TEMPLATE),
                        new File(directory, SCHEMAGEN_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, XJC_BAT_TEMPLATE),
                        new File(directory, XJC_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, ASAPT_BAT_TEMPLATE),
                        new File(directory, ASAPT_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, WSCOMPILE_BAT_TEMPLATE),
                        new File(directory, WSCOMPILE_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, WSDEPLOY_BAT_TEMPLATE),
                        new File(directory, WSDEPLOY_BAT)));
                list.add(FileUtils.copyFile(
                        new File(directory, UPDATETOOL_BAT_TEMPLATE),
                        new File(directory, UPDATETOOL_BAT)));
            } else {
                list.add(FileUtils.copyFile(
                        new File(directory, ASENV_CONF_TEMPLATE),
                        new File(directory, ASENV_CONF)));
                list.add(FileUtils.copyFile(
                        new File(directory, ASADMINENV_CONF_TEMPLATE),
                        new File(directory, ASADMINENV_CONF)));
                list.add(FileUtils.copyFile(
                        new File(directory, UNINSTALL_TEMPLATE),
                        new File(directory, UNINSTALL)));
                list.add(FileUtils.copyFile(
                        new File(directory, ASADMIN_TEMPLATE),
                        new File(directory, ASADMIN)));
                list.add(FileUtils.copyFile(
                        new File(directory, ASANT_TEMPLATE),
                        new File(directory, ASANT)));
                list.add(FileUtils.copyFile(
                        new File(directory, APPCLIENT_TEMPLATE),
                        new File(directory, APPCLIENT)));
                list.add(FileUtils.copyFile(
                        new File(directory, JSPC_TEMPLATE),
                        new File(directory, JSPC)));
                list.add(FileUtils.copyFile(
                        new File(directory, PACKAGE_APPCLIENT_TEMPLATE),
                        new File(directory, PACKAGE_APPCLIENT)));
                list.add(FileUtils.copyFile(
                        new File(directory, VERIFIER_TEMPLATE),
                        new File(directory, VERIFIER)));
                list.add(FileUtils.copyFile(
                        new File(directory, ASUPGRADE_TEMPLATE),
                        new File(directory, ASUPGRADE)));
                list.add(FileUtils.copyFile(
                        new File(directory, CAPTURE_SCHEMA_TEMPLATE),
                        new File(directory, CAPTURE_SCHEMA)));
                list.add(FileUtils.copyFile(
                        new File(directory, WSIMPORT_TEMPLATE),
                        new File(directory, WSIMPORT)));
                list.add(FileUtils.copyFile(
                        new File(directory, WSGEN_TEMPLATE),
                        new File(directory, WSGEN)));
                list.add(FileUtils.copyFile(
                        new File(directory, XJC_TEMPLATE),
                        new File(directory, XJC)));
                list.add(FileUtils.copyFile(
                        new File(directory, SCHEMAGEN_TEMPLATE),
                        new File(directory, SCHEMAGEN)));
                list.add(FileUtils.copyFile(
                        new File(directory, ASAPT_TEMPLATE),
                        new File(directory, ASAPT)));
                list.add(FileUtils.copyFile(
                        new File(directory, WSCOMPILE_TEMPLATE),
                        new File(directory, WSCOMPILE)));
                list.add(FileUtils.copyFile(
                        new File(directory, WSDEPLOY_TEMPLATE),
                        new File(directory, WSDEPLOY)));
                list.add(FileUtils.copyFile(
                        new File(directory, UPDATETOOL_TEMPLATE),
                        new File(directory, UPDATETOOL)));
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.copy.files"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.replace.tokens")); // NOI18N
            
            final Map<String, Object> map = new HashMap<String, Object>();
            
            map.put(CONFIG_HOME_TOKEN, new File(directory, CONFIG_SUBDIR));
            map.put(INSTALL_HOME_TOKEN, directory);
            map.put(WEBSERVICES_LIB_TOKEN, new File(directory, LIB_SUBDIR));
            map.put(JAVA_HOME_TOKEN, javaHome);
            map.put(ANT_HOME_TOKEN, new File(directory, LIB_ANT_SUBDIR));
            map.put(ANT_LIB_TOKEN, new File(directory, LIB_ANT_LIB_SUBDIR));
            map.put(NSS_HOME_TOKEN, new File(directory, LIB_SUBDIR));
            map.put(NSS_BIN_HOME_TOKEN, new File(directory, LIB_ADMINCGI_SUBDIR));
            map.put(IMQ_LIB_TOKEN, new File(directory, IMQ_LIB_SUBDIR));
            map.put(IMQ_BIN_TOKEN, new File(directory, IMQ_BIN_SUBDIR));
            map.put(JHELP_HOME_TOKEN, new File(directory, LIB_SUBDIR));
            map.put(ICU_LIB_TOKEN, new File(directory, LIB_SUBDIR));
            map.put(JATO_LIB_TOKEN, new File(directory, LIB_SUBDIR));
            map.put(WEBCONSOLE_LIB_TOKEN, new File(directory, LIB_SUBDIR));
            map.put(USE_NATIVE_LAUNCHER_TOKEN, USE_NATIVE_LAUNCHER);
            map.put(LAUNCHER_LIB_TOKEN, LAUNCHER_LIB);
            map.put(JDMK_HOME_TOKEN, new File(directory, JMDK_HOME));
            map.put(LOCALE_TOKEN, LOCALE);
            map.put(DEF_DOMAINS_PATH_TOKEN, new File(directory, DOMAINS_SUBDIR));
            map.put(ACC_CONFIG_TOKEN, new File(directory, ACC_CONFIG));
            map.put(DERBY_HOME_TOKEN, new File(directory, DERBY_SUBDIR));
            map.put("localhost", SystemUtils.getHostName());
            map.put("user=admin","user=" + username);
            map.put(HTTP_PORT_TOKEN,httpPort);
            map.put(ADMIN_PORT_TOKEN,adminPort);
            map.put(AS_ADMIN_PORT_TOKEN,adminPort);
            map.put(AS_ADMIN_PROFILE_TOKEN,AS_ADMIN_PROFILE);
            map.put(AS_ADMIN_SECURE_TOKEN,AS_ADMIN_SECURE);
            
            FileUtils.modifyFile(new File(directory, BIN_SUBDIR), map);
            FileUtils.modifyFile(new File(directory, CONFIG_SUBDIR), map);
            
            map.put(UC_INSTALL_HOME_TOKEN,new File(directory, UC_INSTALL_HOME_SUBDIR));
            map.put(UC_EXT_LIB_TOKEN,new File(directory, UC_EXT_LIB));
            map.put(UC_AS_HOME_TOKEN, directory);
            
            if(SystemUtils.isWindows()) {
                map.put(JDIC_LIB_TOKEN,
                        new File(directory, JDIC_LIB_WINDOWS));
                map.put(JDIC_STUB_LIB_TOKEN,
                        new File(directory, JDIC_STUB_LIB_WINDOWS));
            } else if (SystemUtils.isLinux()) {
                map.put(JDIC_LIB_TOKEN,
                        new File(directory, JDIC_LIB_LINUX));
                map.put(JDIC_STUB_LIB_TOKEN,
                        new File(directory, JDIC_STUB_LIB_LINUX));
            } else if(SystemUtils.isMacOS()) {
                map.put(JDIC_LIB_TOKEN,
                        new File(directory, JDIC_LIB_MACOSX));
                map.put(JDIC_STUB_LIB_TOKEN,
                        new File(directory, JDIC_STUB_LIB_MACOSX));
            } else if (SystemUtils.isSolaris()) {
                if(SystemUtils.getCurrentPlatform().
                        isCompatibleWith(Platform.SOLARIS_SPARC)) {
                    map.put(JDIC_LIB_TOKEN,
                            new File(directory, JDIC_LIB_SOLARIS_SPARC));
                } else if(SystemUtils.getCurrentPlatform().
                        isCompatibleWith(Platform.SOLARIS_X86)) {
                    map.put(JDIC_LIB_TOKEN,
                            new File(directory, JDIC_LIB_SOLARIS_X86));
                }
                map.put(JDIC_STUB_LIB_TOKEN,
                        new File(directory, JDIC_STUB_LIB_SOLARIS));
            }
            
            FileUtils.modifyFile(new File(directory, UC_BIN_SUBDIR), map);
            FileUtils.modifyFile(new File(directory, UC_CONFIG_SUBDIR), map);
            
            
            final String javaHomeString = javaHome.getAbsolutePath();
            final String imqVarHomeString = new File(
                    directory,
                    DOMAINS_DOMAIN1_IMQ_SUBDIR).getAbsolutePath();
            final String contents = StringUtils.format(
                    IMQENV_CONF_ADDITION,
                    javaHomeString,
                    imqVarHomeString);
            
            list.add(FileUtils.writeFile(
                    new File(directory, IMQENV_CONF),
                    contents,
                    true));
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.replace.tokens"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        //try {
        //    progress.setDetail(getString("CL.install.irrelevant.files")); // NOI18N
        //
        //    SystemUtils.removeIrrelevantFiles(directory);
        //} catch (IOException e) {
        //    throw new InstallationException(
        //            getString("CL.install.error.irrelevant.files"), // NOI18N
        //            e);
        //}
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.files.permissions")); // NOI18N
            
            SystemUtils.correctFilesPermissions(
                    new File(directory, "bin"));
            SystemUtils.correctFilesPermissions(
                    new File(directory, "imq/bin"));
            SystemUtils.correctFilesPermissions(
                    new File(directory, "updatecenter/bin"));
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
            final InstallationException firstException = new InstallationException(
                    getString("CL.install.error.create.domain"), // NOI18N
                    e);
            
            final File asadminpass = new File(
                    SystemUtils.getUserHomeDirectory(),
                    ".asadminpass");;
                    final File asadmintruststore = new File(
                            SystemUtils.getUserHomeDirectory(),
                            ".asadmintruststore");
                    if (asadminpass.exists() || asadmintruststore.exists()) {
                        LogManager.log("either .asadminpass or .asadmintruststore " +
                                "files exist -- deleting them");
                        
                        getProduct().addInstallationWarning(firstException);
                        
                        try {
                            FileUtils.deleteFile(asadminpass);
                            FileUtils.deleteFile(asadmintruststore);                            
                            FileUtils.deleteFile(
                                    new File(directory,
                                    DOMAINS_SUBDIR + File.separator + DOMAIN_NAME),
                                    true);
                            
                            GlassFishUtils.createDomain(
                                    directory,
                                    DOMAIN_NAME,
                                    username,
                                    password,
                                    httpPort,
                                    httpsPort,
                                    adminPort);
                        } catch (IOException ex) {
                            throw new InstallationException(
                                    getString("CL.install.error.create.domain"), // NOI18N
                                    ex);
                        }
                    } else {
                        throw firstException;
                    }
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.extra.files")); // NOI18N
            
            list.add(new File(directory, DOMAINS_SUBDIR));
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
        try {
            progress.setDetail(getString("CL.uninstall.extra.files")); // NOI18N
            
            if (SystemUtils.isWindows()) {
                FileUtils.deleteFile(new File(directory, ASENV_BAT));
                FileUtils.deleteFile(new File(directory, ASADMIN_BAT));
                FileUtils.deleteFile(new File(directory, ASANT_BAT));
                FileUtils.deleteFile(new File(directory, APPCLIENT_BAT));
                FileUtils.deleteFile(new File(directory, JSPC_BAT));
                FileUtils.deleteFile(new File(directory, PACKAGE_APPCLIENT_BAT));
                FileUtils.deleteFile(new File(directory, VERIFIER_BAT));
                FileUtils.deleteFile(new File(directory, ASUPGRADE_BAT));
                FileUtils.deleteFile(new File(directory, CAPTURE_SCHEMA_BAT));
                FileUtils.deleteFile(new File(directory, WSIMPORT_BAT));
                FileUtils.deleteFile(new File(directory, WSGEN_BAT));
                FileUtils.deleteFile(new File(directory, SCHEMAGEN_BAT));
                FileUtils.deleteFile(new File(directory, XJC_BAT));
                FileUtils.deleteFile(new File(directory, ASAPT_BAT));
                FileUtils.deleteFile(new File(directory, WSCOMPILE_BAT));
                FileUtils.deleteFile(new File(directory, WSDEPLOY_BAT));
            } else {
                FileUtils.deleteFile(new File(directory, ASENV_CONF));
                FileUtils.deleteFile(new File(directory, UNINSTALL));
                FileUtils.deleteFile(new File(directory, ASADMIN));
                FileUtils.deleteFile(new File(directory, ASANT));
                FileUtils.deleteFile(new File(directory, APPCLIENT));
                FileUtils.deleteFile(new File(directory, JSPC));
                FileUtils.deleteFile(new File(directory, PACKAGE_APPCLIENT));
                FileUtils.deleteFile(new File(directory, VERIFIER));
                FileUtils.deleteFile(new File(directory, ASUPGRADE));
                FileUtils.deleteFile(new File(directory, CAPTURE_SCHEMA));
                FileUtils.deleteFile(new File(directory, WSIMPORT));
                FileUtils.deleteFile(new File(directory, WSGEN));
                FileUtils.deleteFile(new File(directory, XJC));
                FileUtils.deleteFile(new File(directory, SCHEMAGEN));
                FileUtils.deleteFile(new File(directory, ASAPT));
                FileUtils.deleteFile(new File(directory, WSCOMPILE));
                FileUtils.deleteFile(new File(directory, WSDEPLOY));
            }
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.extra.files"), // NOI18N
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
            "resource:" + // NOI18N
            "org/netbeans/installer/products/glassfish/wizard.xml"; // NOI18N
    
    public static final String DOMAIN_NAME =
            "domain1"; // NOI18N
    
    public static final String ASENV_BAT_TEMPLATE =
            "lib/install/templates/asenv.bat.template"; // NOI18N
    public static final String ASENV_BAT =
            "config/asenv.bat"; // NOI18N
    
    public static final String ASADMIN_BAT_TEMPLATE =
            "lib/install/templates/asadmin.bat.template"; // NOI18N
    public static final String ASADMIN_BAT =
            "bin/asadmin.bat"; // NOI18N
    
    public static final String ASANT_BAT_TEMPLATE =
            "lib/install/templates/asant.bat.template"; // NOI18N
    public static final String ASANT_BAT =
            "bin/asant.bat"; // NOI18N
    
    public static final String APPCLIENT_BAT_TEMPLATE =
            "lib/install/templates/appclient.bat.template"; // NOI18N
    public static final String APPCLIENT_BAT =
            "bin/appclient.bat"; // NOI18N
    
    public static final String JSPC_BAT_TEMPLATE =
            "lib/install/templates/jspc.bat.template"; // NOI18N
    public static final String JSPC_BAT =
            "bin/jspc.bat"; // NOI18N
    
    public static final String PACKAGE_APPCLIENT_BAT_TEMPLATE =
            "lib/install/templates/package-appclient.bat.template"; // NOI18N
    public static final String PACKAGE_APPCLIENT_BAT =
            "bin/package-appclient.bat"; // NOI18N
    
    public static final String VERIFIER_BAT_TEMPLATE =
            "lib/install/templates/verifier.bat.template"; // NOI18N
    public static final String VERIFIER_BAT =
            "bin/verifier.bat"; // NOI18N
    
    public static final String ASUPGRADE_BAT_TEMPLATE =
            "lib/install/templates/asupgrade.bat.template"; // NOI18N
    public static final String ASUPGRADE_BAT =
            "bin/asupgrade.bat"; // NOI18N
    
    public static final String CAPTURE_SCHEMA_BAT_TEMPLATE =
            "lib/install/templates/capture-schema.bat.template"; // NOI18N
    public static final String CAPTURE_SCHEMA_BAT =
            "bin/capture-schema.bat"; // NOI18N
    
    public static final String WSIMPORT_BAT_TEMPLATE =
            "lib/install/templates/wsimport.bat.template"; // NOI18N
    public static final String WSIMPORT_BAT =
            "bin/wsimport.bat"; // NOI18N
    
    public static final String WSGEN_BAT_TEMPLATE =
            "lib/install/templates/wsgen.bat.template"; // NOI18N
    public static final String WSGEN_BAT =
            "bin/wsgen.bat"; // NOI18N
    
    public static final String SCHEMAGEN_BAT_TEMPLATE =
            "lib/install/templates/schemagen.bat.template"; // NOI18N
    public static final String SCHEMAGEN_BAT =
            "bin/schemagen.bat"; // NOI18N
    
    public static final String XJC_BAT_TEMPLATE =
            "lib/install/templates/xjc.bat.template"; // NOI18N
    public static final String XJC_BAT =
            "bin/xjc.bat"; // NOI18N
    
    public static final String ASAPT_BAT_TEMPLATE =
            "lib/install/templates/asapt.bat.template"; // NOI18N
    public static final String ASAPT_BAT =
            "bin/asapt.bat"; // NOI18N
    
    public static final String WSCOMPILE_BAT_TEMPLATE =
            "lib/install/templates/wscompile.bat.template"; // NOI18N
    public static final String WSCOMPILE_BAT =
            "bin/wscompile.bat"; // NOI18N
    
    public static final String WSDEPLOY_BAT_TEMPLATE =
            "lib/install/templates/wsdeploy.bat.template"; // NOI18N
    public static final String WSDEPLOY_BAT =
            "bin/wsdeploy.bat"; // NOI18N
    
    public static final String UPDATETOOL_BAT_TEMPLATE =
            "updatecenter/lib/install/templates/updatetool.bat.template";//NOI18N
    public static final String UPDATETOOL_BAT =
            "/updatecenter/bin/updatetool.bat";//NOI18N
    
    
    public static final String ASENV_CONF_TEMPLATE =
            "lib/install/templates/asenv.conf.template"; // NOI18N
    public static final String ASENV_CONF =
            "config/asenv.conf"; // NOI18N
    
    public static final String ASADMINENV_CONF_TEMPLATE =
            "lib/install/templates/asadminenv.conf"; // NOI18N
    public static final String ASADMINENV_CONF =
            "config/asadminenv.conf"; // NOI18N
    
    public static final String UNINSTALL_TEMPLATE =
            "lib/install/templates/uninstall.template"; // NOI18N
    public static final String UNINSTALL =
            "bin/uninstall"; // NOI18N
    
    public static final String ASADMIN_TEMPLATE =
            "lib/install/templates/asadmin.template"; // NOI18N
    public static final String ASADMIN =
            "bin/asadmin"; // NOI18N
    
    public static final String ASANT_TEMPLATE =
            "lib/install/templates/asant.template"; // NOI18N
    public static final String ASANT =
            "bin/asant"; // NOI18N
    
    public static final String APPCLIENT_TEMPLATE =
            "lib/install/templates/appclient.template"; // NOI18N
    public static final String APPCLIENT =
            "bin/appclient"; // NOI18N
    
    public static final String JSPC_TEMPLATE =
            "lib/install/templates/jspc.template"; // NOI18N
    public static final String JSPC =
            "bin/jspc"; // NOI18N
    
    public static final String PACKAGE_APPCLIENT_TEMPLATE =
            "lib/install/templates/package-appclient.template"; // NOI18N
    public static final String PACKAGE_APPCLIENT =
            "bin/package-appclient"; // NOI18N
    
    public static final String VERIFIER_TEMPLATE =
            "lib/install/templates/verifier.template"; // NOI18N
    public static final String VERIFIER =
            "bin/verifier"; // NOI18N
    
    public static final String ASUPGRADE_TEMPLATE =
            "lib/install/templates/asupgrade.template"; // NOI18N
    public static final String ASUPGRADE =
            "bin/asupgrade"; // NOI18N
    
    public static final String CAPTURE_SCHEMA_TEMPLATE =
            "lib/install/templates/capture-schema.template"; // NOI18N
    public static final String CAPTURE_SCHEMA =
            "bin/capture-schema"; // NOI18N
    
    public static final String WSIMPORT_TEMPLATE =
            "lib/install/templates/wsimport.template"; // NOI18N
    public static final String WSIMPORT =
            "bin/wsimport"; // NOI18N
    
    public static final String WSGEN_TEMPLATE =
            "lib/install/templates/wsgen.template"; // NOI18N
    public static final String WSGEN =
            "bin/wsgen"; // NOI18N
    
    public static final String XJC_TEMPLATE =
            "lib/install/templates/xjc.template"; // NOI18N
    public static final String XJC =
            "bin/xjc"; // NOI18N
    
    public static final String SCHEMAGEN_TEMPLATE =
            "lib/install/templates/schemagen.template"; // NOI18N
    public static final String SCHEMAGEN =
            "bin/schemagen"; // NOI18N
    
    public static final String ASAPT_TEMPLATE =
            "lib/install/templates/asapt.template"; // NOI18N
    public static final String ASAPT =
            "bin/asapt"; // NOI18N
    
    public static final String WSCOMPILE_TEMPLATE =
            "lib/install/templates/wscompile.template"; // NOI18N
    public static final String WSCOMPILE =
            "bin/wscompile"; // NOI18N
    
    public static final String WSDEPLOY_TEMPLATE =
            "lib/install/templates/wsdeploy.template"; // NOI18N
    public static final String WSDEPLOY =
            "bin/wsdeploy"; // NOI18N
    
    public static final String UPDATETOOL_TEMPLATE =
            "updatecenter/lib/install/templates/updatetool.template";//NOI18N
    public static final String UPDATETOOL =
            "/updatecenter/bin/updatetool";//NOI18N
    
    
    public static final String CONFIG_HOME_TOKEN =
            "%CONFIG_HOME%"; // NOI18N
    public static final String INSTALL_HOME_TOKEN =
            "%INSTALL_HOME%"; // NOI18N
    public static final String WEBSERVICES_LIB_TOKEN =
            "%WEBSERVICES_LIB%"; // NOI18N
    public static final String JAVA_HOME_TOKEN =
            "%JAVA_HOME%"; // NOI18N
    public static final String ANT_HOME_TOKEN =
            "%ANT_HOME%"; // NOI18N
    public static final String ANT_LIB_TOKEN =
            "%ANT_LIB%"; // NOI18N
    public static final String NSS_HOME_TOKEN =
            "%NSS_HOME%"; // NOI18N
    public static final String NSS_BIN_HOME_TOKEN =
            "%NSS_BIN_HOME%"; // NOI18N
    public static final String IMQ_LIB_TOKEN =
            "%IMQ_LIB%"; // NOI18N
    public static final String IMQ_BIN_TOKEN =
            "%IMQ_BIN%"; // NOI18N
    public static final String JHELP_HOME_TOKEN =
            "%JHELP_HOME%"; // NOI18N
    public static final String ICU_LIB_TOKEN =
            "%ICU_LIB%"; // NOI18N
    public static final String JATO_LIB_TOKEN =
            "%JATO_LIB%"; // NOI18N
    public static final String WEBCONSOLE_LIB_TOKEN =
            "%WEBCONSOLE_LIB%"; // NOI18N
    public static final String USE_NATIVE_LAUNCHER_TOKEN =
            "%USE_NATIVE_LAUNCHER%"; // NOI18N
    public static final String LAUNCHER_LIB_TOKEN =
            "%LAUNCHER_LIB%"; // NOI18N
    public static final String JDMK_HOME_TOKEN =
            "%JDMK_HOME%"; // NOI18N
    public static final String LOCALE_TOKEN =
            "%LOCALE%"; // NOI18N
    public static final String DEF_DOMAINS_PATH_TOKEN =
            "%DEF_DOMAINS_PATH%"; // NOI18N
    public static final String ACC_CONFIG_TOKEN =
            "%ACC_CONFIG%"; // NOI18N
    public static final String DERBY_HOME_TOKEN =
            "%DERBY_HOME%"; // NOI18N
    public static final String HTTP_PORT_TOKEN =
            "%HTTP_PORT%"; //NOI18N
    public static final String ADMIN_PORT_TOKEN =
            "%ADMIN_PORT%"; //NOI18N
    public static final String AS_ADMIN_PORT_TOKEN =
            "%AS_ADMIN_PORT%"; //NOI18N
    public static final String AS_ADMIN_PROFILE_TOKEN =
            "%AS_ADMIN_PROFILE%"; //NOI18N
    public static final String AS_ADMIN_SECURE_TOKEN =
            "%AS_ADMIN_SECURE%"; //NOI18N
    
    public static final String UC_INSTALL_HOME_TOKEN =
            "@INSTALL_HOME@"; //NOI18N
    public static final String UC_EXT_LIB_TOKEN =
            "@EXT_LIB@";       //NOI18N
    public static final String UC_AS_HOME_TOKEN =
            "%appserver_home%"; //NOI18N
    public static final String JDIC_LIB_TOKEN =
            "@JDIC_LIB@"; //NOI18N
    public static final String JDIC_STUB_LIB_TOKEN =
            "@JDIC_STUB_LIB@"; //NOI18N
    
    public static final String JDIC_LIB_WINDOWS =
            "updatecenter/lib/jdic/windows/x86";//NOI18N
    public static final String JDIC_LIB_LINUX =
            "updatecenter/lib/jdic/linux/x86";//NOI18N
    public static final String JDIC_LIB_SOLARIS_X86 =
            "updatecenter/lib/jdic/sunos/x86";//NOI18N
    public static final String JDIC_LIB_SOLARIS_SPARC =
            "updatecenter/lib/jdic/sunos/sparc";//NOI18N
    public static final String JDIC_LIB_MACOSX =
            "updatecenter/lib/jdic/mac/ppc";//NOI18N
    
    public static final String JDIC_STUB_LIB_WINDOWS =
            "updatecenter/lib/jdic/windows";//NOI18N
    public static final String JDIC_STUB_LIB_LINUX =
            "updatecenter/lib/jdic/linux";//NOI18N
    public static final String JDIC_STUB_LIB_SOLARIS =
            "updatecenter/lib/jdic/sunos";//NOI18N
    public static final String JDIC_STUB_LIB_MACOSX =
            "updatecenter/lib/jdic/mac";//NOI18N
    
    
    
    public static final String CONFIG_SUBDIR =
            "config"; // NOI18N
    public static final String LIB_SUBDIR =
            "lib"; // NOI18N
    public static final String LIB_ANT_SUBDIR =
            "lib/ant"; // NOI18N
    public static final String LIB_ANT_LIB_SUBDIR =
            "lib/ant/lib"; // NOI18N
    public static final String LIB_ADMINCGI_SUBDIR =
            "lib/admincgi"; // NOI18N
    public static final String IMQ_LIB_SUBDIR =
            "imq/lib"; // NOI18N
    public static final String IMQ_BIN_SUBDIR =
            "imq/bin"; // NOI18N
    public static final String DOMAINS_SUBDIR =
            "domains"; // NOI18N
    public static final String DERBY_SUBDIR =
            "javadb"; // NOI18N
    public static final String UC_INSTALL_HOME_SUBDIR =
            "updatecenter"; //NOI18N
    public static final String UC_BIN_SUBDIR =
            "updatecenter/bin"; //NOI18N
    public static final String UC_CONFIG_SUBDIR =
            "updatecenter/config"; //NOI18N
    
    public static final String UC_EXT_LIB =
            "updatecenter/lib/schema2beans.jar"; //NOI18N
    
    public static final String AS_ADMIN_PROFILE =
            "developer"; //NOI18N
    public static final String AS_ADMIN_SECURE =
            "false"; //NOI18N
    public static final String BIN_SUBDIR =
            "bin"; // NOI18N
    
    public static final String DERBY_LOG =
            "derby.log"; // NOI18N
    
    public static final String USE_NATIVE_LAUNCHER=
            "false"; // NOI18N
    public static final String LAUNCHER_LIB =
            "\\jre\\bin\\client"; // NOI18N
    public static final String JMDK_HOME =
            "lib/SUNWjdmk/5.1"; // NOI18N
    public static final String LOCALE =
            "en_US"; // NOI18N
    public static final String ACC_CONFIG =
            "domains/" + DOMAIN_NAME + "/config/sun-acc.xml"; // NOI18N
    
    public static final String DOMAINS_DOMAIN1_IMQ_SUBDIR =
            "domains/" + DOMAIN_NAME + "/imq"; // NOI18N
    
    public static final String IMQENV_CONF_ADDITION =
            "        set IMQ_DEFAULT_JAVAHOME={0}\n" + // NOI18N
            "        set IMQ_DEFAULT_VARHOME={1}\n"; // NOI18N
    
    public static final String IMQENV_CONF =
            "imq/etc/imqenv.conf"; // NOI18N
    
    public static final String JVM_OPTION_NAME =
            "-Dcom.sun.aas.installRoot"; // NOI18N
    
    public static final String PRODUCT_ID =
            "GLASSFISH"; // NOI18N
}
