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
package org.netbeans.installer.products.nb.base;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.filters.OrFilter;
import org.netbeans.installer.product.filters.ProductFilter;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.applications.JavaUtils.JavaInfo;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.shortcut.FileShortcut;
import org.netbeans.installer.utils.system.shortcut.LocationType;
import org.netbeans.installer.utils.system.shortcut.Shortcut;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.panels.JdkLocationPanel;

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

    public void install(final Progress progress) throws InstallationException {
        final Product product = getProduct();
        final File installLocation = product.getInstallationLocation();
        final FilesList filesList = product.getInstalledFiles();
        
        /////////////////////////////////////////////////////////////////////////////
        final File jdkHome = new File(
                product.getProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY));
        try {
            progress.setDetail(getString("CL.install.jdk.home")); // NOI18N
            JavaInfo info = JavaUtils.getInfo(jdkHome);
            LogManager.log("Using the following JDK for NetBeans configuration : ");
            LogManager.log("... path    : "  + jdkHome);
            LogManager.log("... version : "  + info.getVersion().toJdkStyle());
            LogManager.log("... vendor  : "  + info.getVendor());
            LogManager.log("... final   : "  + (!info.isNonFinal()));
            NetBeansUtils.setJavaHome(installLocation, jdkHome);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.jdk.home"), // NOI18N
                    e);
        }

        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.netbeans.clusters")); // NOI18N
            for (String clusterName: CLUSTERS) {
                File lastModified = new File(new File(installLocation, clusterName), 
                                    NetBeansUtils.LAST_MODIFIED_MARKER);
                if(!FileUtils.exists(lastModified)) {
                    filesList.add(lastModified);
                }
                NetBeansUtils.addCluster(installLocation, clusterName);
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.netbeans.clusters"), // NOI18N
                    e);
        }

         // update the update_tracking files information //////////////////////////////
        for (String clusterName: CLUSTERS) {
            try {
                progress.setDetail(getString(
                        "CL.install.netbeans.update.tracking", // NOI18N
                        clusterName));

                NetBeansUtils.updateTrackingFilesInfo(installLocation, clusterName);
            } catch (IOException e) {
                throw new InstallationException(getString(
                        "CL.install.error.netbeans.update.tracking", // NOI18N
                        clusterName),
                        e);
            }
        }

        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.product.id")); // NOI18N

            filesList.add(NetBeansUtils.createProductId(installLocation));
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.product.id"), // NOI18N
                    e);
        }

        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.license.accepted")); // NOI18N

            filesList.add(
                    NetBeansUtils.createLicenseAcceptedMarker(installLocation));
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.license.accepted"), // NOI18N
                    e);
        }

        /////////////////////////////////////////////////////////////////////////////
        //try {
        //    progress.setDetail(getString("CL.install.irrelevant.files")); // NOI18N
        //
        //    SystemUtils.removeIrrelevantFiles(binSubdir);
        //    SystemUtils.removeIrrelevantFiles(etcSubdir);
        //    SystemUtils.removeIrrelevantFiles(platformCluster);
        //    SystemUtils.removeIrrelevantFiles(nbCluster);
        //    SystemUtils.removeIrrelevantFiles(ideCluster);
        //} catch (IOException e) {
        //    throw new InstallationException(
        //            getString("CL.install.error.irrelevant.files"), // NOI18N
        //            e);
        //}

        /////////////////////////////////////////////////////////////////////////////
        //try {
        //    progress.setDetail(getString("CL.install.files.permissions")); // NOI18N
        //
        //    SystemUtils.correctFilesPermissions(binSubdir);
        //    SystemUtils.correctFilesPermissions(etcSubdir);
        //    SystemUtils.correctFilesPermissions(platformCluster);
        //    SystemUtils.correctFilesPermissions(nbCluster);
        //    SystemUtils.correctFilesPermissions(ideCluster);
        //} catch (IOException e) {
        //    throw new InstallationException(
        //            getString("CL.install.error.files.permissions"), // NOI18N
        //            e);
        //}

        /////////////////////////////////////////////////////////////////////////////
        LogManager.logIndent(
                "creating the desktop shortcut for NetBeans IDE"); // NOI18N
        if (!SystemUtils.isMacOS()) {
            try {
                progress.setDetail(getString("CL.install.desktop")); // NOI18N

                if (SystemUtils.isCurrentUserAdmin()) {
                    LogManager.log(
                            "... current user is an administrator " + // NOI18N
                            "-- creating the shortcut for all users"); // NOI18N

                    SystemUtils.createShortcut(
                            getDesktopShortcut(installLocation),
                            LocationType.ALL_USERS_DESKTOP);

                    getProduct().setProperty(
                            DESKTOP_SHORTCUT_LOCATION_PROPERTY,
                            ALL_USERS_PROPERTY_VALUE);
                } else {
                    LogManager.log(
                            "... current user is an ordinary user " + // NOI18N
                            "-- creating the shortcut for the current " + // NOI18N
                            "user only"); // NOI18N

                    SystemUtils.createShortcut(
                            getDesktopShortcut(installLocation),
                            LocationType.CURRENT_USER_DESKTOP);

                    getProduct().setProperty(
                            DESKTOP_SHORTCUT_LOCATION_PROPERTY,
                            CURRENT_USER_PROPERTY_VALUE);
                }
            } catch (NativeException e) {
                LogManager.unindent();

                LogManager.log(
                        getString("CL.install.error.desktop"), // NOI18N
                        e);
            }
        } else {
            LogManager.log(
                    "... skipping this step as we're on Mac OS"); // NOI18N
        }
        LogManager.logUnindent(
                "... done"); // NOI18N

        /////////////////////////////////////////////////////////////////////////////
        LogManager.logIndent(
                "creating the start menu shortcut for NetBeans IDE"); // NOI18N
        try {
            progress.setDetail(getString("CL.install.start.menu")); // NOI18N

            if (SystemUtils.isCurrentUserAdmin()) {
                LogManager.log(
                        "... current user is an administrator " + // NOI18N
                        "-- creating the shortcut for all users"); // NOI18N

                SystemUtils.createShortcut(
                        getStartMenuShortcut(installLocation),
                        LocationType.ALL_USERS_START_MENU);

                getProduct().setProperty(
                        START_MENU_SHORTCUT_LOCATION_PROPERTY,
                        ALL_USERS_PROPERTY_VALUE);
            } else {
                LogManager.log(
                        "... current user is an ordinary user " + // NOI18N
                        "-- creating the shortcut for the current " + // NOI18N
                        "user only"); // NOI18N

                SystemUtils.createShortcut(
                        getStartMenuShortcut(installLocation),
                        LocationType.CURRENT_USER_START_MENU);

                getProduct().setProperty(
                        START_MENU_SHORTCUT_LOCATION_PROPERTY,
                        CURRENT_USER_PROPERTY_VALUE);
            }
        } catch (NativeException e) {
            LogManager.log(
                    getString("CL.install.error.start.menu"), // NOI18N
                    e);
        }
        LogManager.logUnindent(
                "... done"); // NOI18N

        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.netbeans.conf")); // NOI18N

            NetBeansUtils.updateNetBeansHome(installLocation);

        // final long xmx = NetBeansUtils.getJvmMemorySize(
        //         installLocation,
        //         NetBeansUtils.MEMORY_XMX);
        // if (xmx < REQUIRED_XMX_VALUE) {
        //     NetBeansUtils.setJvmMemorySize(
        //            installLocation,
        //             NetBeansUtils.MEMORY_XMX,
        //            REQUIRED_XMX_VALUE);
        // }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.netbeans.conf"), // NOI18N
                    e);
        }

        //get bundled registry to perform further runtime integration
        //http://wiki.netbeans.org/NetBeansInstallerIDEAndRuntimesIntegration
        Registry bundledRegistry = new Registry();
        try {
            final String bundledRegistryUri = System.getProperty(
                    Registry.BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY);

            bundledRegistry.loadProductRegistry(
                    (bundledRegistryUri != null) ? bundledRegistryUri : Registry.DEFAULT_BUNDLED_PRODUCT_REGISTRY_URI);
        } catch (InitializationException e) {
            LogManager.log("Cannot load bundled registry", e);
        }

        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.glassfish.integration")); // NOI18N

            final List<Product> glassfishes =
                    Registry.getInstance().queryProducts(new OrFilter(
                    new ProductFilter("sjsas", Registry.getInstance().getTargetPlatform()),
                    new ProductFilter("glassfish", Registry.getInstance().getTargetPlatform())));

                  Product productToIntegrate = null;
            for (Product glassfish : glassfishes) {
                final Product bundledProduct = bundledRegistry.getProduct(
                        glassfish.getUid(), glassfish.getVersion());
                if (glassfish.getStatus() == Status.INSTALLED && bundledProduct != null) {
                    final File location = glassfish.getInstallationLocation();
                    if (location != null && FileUtils.exists(location) && !FileUtils.isEmpty(location)) {
                        productToIntegrate = glassfish;
                        break;
                    }
                }
            }
            if (productToIntegrate == null) {
                for (Product glassfish : glassfishes) {
                    if (glassfish.getStatus() == Status.INSTALLED) {
                        final File location = glassfish.getInstallationLocation();
                        if (location != null && FileUtils.exists(location) && !FileUtils.isEmpty(location)) {
                            productToIntegrate = glassfish;
                            break;
                        }
                    }
                }
            }
            if (productToIntegrate != null) {
                final File location = productToIntegrate.getInstallationLocation();
                LogManager.log("... integrate " + getSystemDisplayName() + " with " + productToIntegrate.getDisplayName() + " installed at " + location);
                NetBeansUtils.setJvmOption(
                        installLocation,
                        GLASSFISH_JVM_OPTION_NAME,
                        location.getAbsolutePath(),
                        true);
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.glassfish.integration"), // NOI18N
                    e);
        }

        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.glassfish.integration")); // NOI18N


            final List<Product> glassfishes =
                   Registry.getInstance().queryProducts(new OrFilter(
                    new ProductFilter("glassfish-mod-sun", Registry.getInstance().getTargetPlatform()),
                    new ProductFilter("glassfish-mod", Registry.getInstance().getTargetPlatform())));   

            Product productToIntegrate = null;
            for (Product glassfish : glassfishes) {
                final Product bundledProduct = bundledRegistry.getProduct(
                        glassfish.getUid(), glassfish.getVersion());
                if (glassfish.getStatus() == Status.INSTALLED && bundledProduct != null) {
                    final File location = glassfish.getInstallationLocation();
                    if (location != null && FileUtils.exists(location) && !FileUtils.isEmpty(location)) {
                        productToIntegrate = glassfish;
                        break;
                    }
                }
            }
            if (productToIntegrate == null) {
                for (Product glassfish : glassfishes) {
                    if (glassfish.getStatus() == Status.INSTALLED) {
                        final File location = glassfish.getInstallationLocation();
                        if (location != null && FileUtils.exists(location) && !FileUtils.isEmpty(location)) {
                            productToIntegrate = glassfish;
                            break;
                        }
                    }
                }
            }
            if (productToIntegrate != null) {
                final File location = productToIntegrate.getInstallationLocation();
                LogManager.log("... integrate " + getSystemDisplayName() + " with " + productToIntegrate.getDisplayName() + " installed at " + location);
                NetBeansUtils.setJvmOption(
                        installLocation,
                        GLASSFISH_MOD_JVM_OPTION_NAME,
                        location.getAbsolutePath(),
                        true);
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.glassfish.integration"), // NOI18N
                    e);
        } finally {
            progress.setDetail(StringUtils.EMPTY_STRING); // NOI18N
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.tomcat.integration")); // NOI18N

            final List<Product> tomcats =
                    Registry.getInstance().getProducts("tomcat");

            Product productToIntegrate = null;
            for (Product tomcat : tomcats) {
                final Product bundledProduct = bundledRegistry.getProduct(
                        tomcat.getUid(), tomcat.getVersion());
                if (tomcat.getStatus() == Status.INSTALLED && bundledProduct != null) {
                    final File location = tomcat.getInstallationLocation();
                    if (location != null && FileUtils.exists(location) && !FileUtils.isEmpty(location)) {
                        productToIntegrate = tomcat;
                        break;
                    }
                }
            }
            if (productToIntegrate == null) {
                for (Product tomcat : tomcats) {
                    if (tomcat.getStatus() == Status.INSTALLED) {
                        final File location = tomcat.getInstallationLocation();
                        if (location != null && FileUtils.exists(location) && !FileUtils.isEmpty(location)) {
                            productToIntegrate = tomcat;
                            break;
                        }
                    }
                }
            }
            if (productToIntegrate != null) {
                final File location = productToIntegrate.getInstallationLocation();
                LogManager.log("... integrate " + getSystemDisplayName() + " with " + productToIntegrate.getDisplayName() + " installed at " + location);
                NetBeansUtils.setJvmOption(
                        installLocation,
                        TOMCAT_JVM_OPTION_NAME_HOME,
                        location.getAbsolutePath(),
                        true);
                NetBeansUtils.setJvmOption(
                        installLocation,
                        TOMCAT_JVM_OPTION_NAME_TOKEN,
                        "" + System.currentTimeMillis(),
                        true);
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.tomcat.integration"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            final List<Product> jdks = Registry.getInstance().getProducts("jdk");
            for (Product jdk : jdks) {
                // if the IDE was installed in the same session as the jdk, 
                // we should add jdk`s "product id" to the IDE
                if (jdk.getStatus().equals(Status.INSTALLED) && jdk.hasStatusChanged()) {
                    NetBeansUtils.addPackId(installLocation, JDK_PRODUCT_ID);
                    break;
                }
            }
        } catch  (IOException e) {
            LogManager.log("Cannot add jdk`s id to netbeans productid file", e);
        }

        try {
            final File nbCluster = NetBeansUtils.getNbCluster(installLocation);
            filesList.add(new File(nbCluster,"servicetag/registration.xml"));
            filesList.add(new File(nbCluster,"servicetag/servicetag"));
            filesList.add(new File(nbCluster,"servicetag"));
            File coreProp = new File(nbCluster,NetBeansUtils.CORE_PROPERTIES);
            filesList.add(coreProp);
            filesList.add(coreProp.getParentFile());
            filesList.add(coreProp.getParentFile().getParentFile());
            filesList.add(coreProp.getParentFile().getParentFile().getParentFile());
        } catch (IOException e) {
            LogManager.log(e);
        }

	product.setProperty("installation.timestamp", new Long(System.currentTimeMillis()).toString());
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }

    public void uninstall(final Progress progress) throws UninstallationException {
        final Product product = getProduct();
        final File installLocation = product.getInstallationLocation();

        NetBeansUtils.warnNetbeansRunning(installLocation);
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.uninstall.start.menu")); // NOI18N

            final String shortcutLocation =
                    getProduct().getProperty(START_MENU_SHORTCUT_LOCATION_PROPERTY);

            if ((shortcutLocation == null) ||
                    shortcutLocation.equals(CURRENT_USER_PROPERTY_VALUE)) {
                SystemUtils.removeShortcut(
                        getStartMenuShortcut(installLocation),
                        LocationType.CURRENT_USER_START_MENU,
                        true);
            } else {
                SystemUtils.removeShortcut(
                        getStartMenuShortcut(installLocation),
                        LocationType.ALL_USERS_START_MENU,
                        true);
            }
        } catch (NativeException e) {
            LogManager.log(
                    getString("CL.uninstall.error.start.menu"), // NOI18N
                    e);
        }

        /////////////////////////////////////////////////////////////////////////////
        if (!SystemUtils.isMacOS()) {
            try {
                progress.setDetail(getString("CL.uninstall.desktop")); // NOI18N

                final String shortcutLocation = getProduct().getProperty(
                        DESKTOP_SHORTCUT_LOCATION_PROPERTY);

                if ((shortcutLocation == null) ||
                        shortcutLocation.equals(CURRENT_USER_PROPERTY_VALUE)) {
                    SystemUtils.removeShortcut(
                            getDesktopShortcut(installLocation),
                            LocationType.CURRENT_USER_DESKTOP,
                            false);
                } else {
                    SystemUtils.removeShortcut(
                            getDesktopShortcut(installLocation),
                            LocationType.ALL_USERS_DESKTOP,
                            false);
                }
            } catch (NativeException e) {
                LogManager.log(
                        getString("CL.uninstall.error.desktop"), // NOI18N
                        e);
            }
        }
        
        product.setProperty("uninstallation.timestamp",
                new Long(System.currentTimeMillis()).toString());

        if (Boolean.getBoolean("remove.netbeans.userdir")) {
            try {
                progress.setDetail(getString("CL.uninstall.remove.userdir")); // NOI18N
                LogManager.logIndent("Removing NetBeans userdir... ");
                File userDir = NetBeansUtils.getNetBeansUserDirFile(installLocation);
                LogManager.log("... NetBeans userdir location : " + userDir);
                if (FileUtils.exists(userDir) && FileUtils.canWrite(userDir)) {
                    FileUtils.deleteFile(userDir, true);
                }
                LogManager.log("... NetBeans userdir totally removed");
            } catch (IOException e) {
                LogManager.log("Can`t remove NetBeans userdir", e);
            } finally {
                LogManager.unindent();
            }
        }

        /////////////////////////////////////////////////////////////////////////////
        //remove cluster/update files
        try {
            progress.setDetail(getString("CL.uninstall.update.files")); // NOI18N
            for(String cluster : CLUSTERS) {
               File updateDir = new File(installLocation, cluster + File.separator + "update");
               if ( updateDir.exists()) {
                    FileUtils.deleteFile(updateDir, true);
               }
            }
        } catch (IOException e) {
            LogManager.log(
                    getString("CL.uninstall.error.update.files"), // NOI18N
                    e);
        }

        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }

    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }

    @Override
    public String getSystemDisplayName() {
        return getString("CL.system.display.name");
    }

    @Override
    public boolean allowModifyMode() {
        return false;
    }

    @Override
    public boolean wrapForMacOs() {
        return true;
    }

    @Override
    public String getExecutable() {
        if (SystemUtils.isWindows()) {
            return EXECUTABLE_WINDOWS;
        } else {
            return EXECUTABLE_UNIX;
        }
    }

    @Override
    public String getIcon() {
        if (SystemUtils.isWindows()) {
            return ICON_WINDOWS;
        } else if (SystemUtils.isMacOS()) {
            return ICON_MACOSX;
        } else {
            return ICON_UNIX;
        }
    }

    @Override
    public Text getLicense() {
        return null;
    }
    // private //////////////////////////////////////////////////////////////////////
    private Shortcut getDesktopShortcut(final File directory) {
        return getShortcut(
                getStrings("CL.desktop.shortcut.name"), // NOI18N
                getStrings("CL.desktop.shortcut.description"), // NOI18N
                getString("CL.desktop.shortcut.path"), // NOI18N
                directory);
    }

    private Shortcut getStartMenuShortcut(final File directory) {
        if (SystemUtils.isMacOS()) {
            return getShortcut(
                    getStrings("CL.start.menu.shortcut.name.macosx"), // NOI18N
                    getStrings("CL.start.menu.shortcut.description"), // NOI18N
                    getString("CL.start.menu.shortcut.path"), // NOI18N
                    directory);
        } else {
            return getShortcut(
                    getStrings("CL.start.menu.shortcut.name"), // NOI18N
                    getStrings("CL.start.menu.shortcut.description"), // NOI18N
                    getString("CL.start.menu.shortcut.path"), // NOI18N
                    directory);
        }
    }

    private Shortcut getShortcut(
            final Map <Locale, String> names,
            final Map <Locale, String> descriptions,
            final String relativePath,
            final File location) {
        final File icon;
        final File executable;

        if (SystemUtils.isWindows()) {
            icon = new File(location, ICON_WINDOWS);
        } else if (SystemUtils.isMacOS()) {
            icon = new File(location, ICON_MACOSX);
        } else {
            icon = new File(location, ICON_UNIX);
        }

        if (SystemUtils.isWindows()) {
            executable = new File(location, EXECUTABLE_WINDOWS);
        } else {
            executable = new File(location, EXECUTABLE_UNIX);
        }
        final String name = names.get(new Locale(StringUtils.EMPTY_STRING));
        final FileShortcut shortcut = new FileShortcut(name, executable);
        shortcut.setNames(names);
        shortcut.setDescriptions(descriptions);
        shortcut.setCategories(SHORTCUT_CATEGORIES);
        shortcut.setFileName(SHORTCUT_FILENAME);
        shortcut.setIcon(icon);
        shortcut.setRelativePath(relativePath);
        shortcut.setWorkingDirectory(location);
        shortcut.setModifyPath(true);

        return shortcut;
    }
    
    public RemovalMode getRemovalMode() {
        return RemovalMode.LIST;
    }

    @Override
    public Map<String, Object> getAdditionalSystemIntegrationInfo() {
        Map<String, Object> map = super.getAdditionalSystemIntegrationInfo();
        if (SystemUtils.isWindows()) {
            //TODO: get localized readme if it is available and matches current locale
            String readme = new File(getProduct().getInstallationLocation(), "readme.html").getAbsolutePath();
            map.put("DisplayVersion", getString("CL.system.display.version"));
            map.put("Publisher",      getString("CL.system.publisher"));
            map.put("URLInfoAbout",   getString("CL.system.url.about"));
            map.put("URLUpdateInfo",  getString("CL.system.url.update"));
            map.put("HelpLink",       getString("CL.system.url.support"));
            map.put("Readme",         readme);
        }
        return map;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String WIZARD_COMPONENTS_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX + // NOI18N
            "org/netbeans/installer/products/nb/base/wizard.xml"; // NOI18N
    
    public static final String BIN_SUBDIR =
            "bin"; // NOI18N
    public static final String ETC_SUBDIR =
            "etc"; // NOI18N
    
    public static final String PLATFORM_CLUSTER =
            "{platform-cluster}"; // NOI18N
    public static final String NB_CLUSTER  =
            "{nb-cluster}"; // NOI18N
    public static final String IDE_CLUSTER =
            "{ide-cluster}"; // NOI18N
    public static final String WEBSVCCOMMON_CLUSTER =
            "{websvccommon-cluster}"; // NOI18N
//    public static final String EXTRA_CLUSTER =
//            "{extra-cluster}"; // NOI18N
    public static final String [] CLUSTERS = new String [] {
        PLATFORM_CLUSTER,
        NB_CLUSTER,
        IDE_CLUSTER,
        WEBSVCCOMMON_CLUSTER/*,
        EXTRA_CLUSTER*/};
    
    public static final String EXECUTABLE_WINDOWS =
            BIN_SUBDIR + "/netbeans.exe"; // NOI18N
    public static final String EXECUTABLE_UNIX =
            BIN_SUBDIR + "/netbeans"; // NOI18N
    
    public static final String ICON_WINDOWS =
            EXECUTABLE_WINDOWS;
    public static final String ICON_UNIX =
            NB_CLUSTER + "/netbeans.png"; // NOI18N
    public static final String ICON_MACOSX =
            NB_CLUSTER + "/netbeans.icns"; // NOI18N
    
    public static final String SHORTCUT_FILENAME =
            "netbeans-{display-version}.desktop"; // NOI18N
    public static final String[] SHORTCUT_CATEGORIES = new String[] {
        "Application",
        "Development", // NOI18N
        "Java",// NOI18N
        "IDE"// NOI18N
    };
    public static final String JDK_PRODUCT_ID =
            "JDK";//NOI18N
    public static final String GLASSFISH_JVM_OPTION_NAME =
            "-Dcom.sun.aas.installRoot"; // NOI18N
    public static final String GLASSFISH_MOD_JVM_OPTION_NAME =
            "-Dorg.glassfish.v3ee6.installRoot"; //NOI18N
    
    public static final String TOMCAT_JVM_OPTION_NAME_TOKEN =
            "-Dorg.netbeans.modules.tomcat.autoregister.token"; // NOI18N
    
    public static final String TOMCAT_JVM_OPTION_NAME_HOME =
            "-Dorg.netbeans.modules.tomcat.autoregister.catalinaHome"; // NOI18N
    
    public static final long REQUIRED_XMX_VALUE =
            192 * NetBeansUtils.M;
    
    private static final String DESKTOP_SHORTCUT_LOCATION_PROPERTY =
            "desktop.shortcut.location"; // NOI18N
    
    private static final String START_MENU_SHORTCUT_LOCATION_PROPERTY =
            "start.menu.shortcut.location"; // NOI18N
    
    private static final String ALL_USERS_PROPERTY_VALUE =
            "all.users"; // NOI18N
    
    private static final String CURRENT_USER_PROPERTY_VALUE =
            "current.user"; // NOI18N
}
