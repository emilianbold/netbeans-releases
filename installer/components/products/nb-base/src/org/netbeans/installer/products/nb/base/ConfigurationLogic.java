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
package org.netbeans.installer.products.nb.base;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.Shortcut;
import org.netbeans.installer.utils.helper.ShortcutLocationType;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.progress.Progress;
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
    
    public void install(Progress progress) throws InstallationException {
        final Product product = getProduct();
        final File installLocation = product.getInstallationLocation();
        final FilesList filesList = product.getInstalledFiles();
        final File binSubdir = new File(installLocation, BIN_SUBDIR);
        final File etcSubdir = new File(installLocation, ETC_SUBDIR);
        final File platformCluster = new File(installLocation, PLATFORM_CLUSTER);
        final File nbCluster = new File(installLocation, NB_CLUSTER);
        final File ideCluster = new File(installLocation, IDE_CLUSTER);
        
        /////////////////////////////////////////////////////////////////////////////
        final File jdkHome = new File(
                product.getProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY));
        try {
            progress.setDetail(getString("CL.install.jdk.home")); // NOI18N
            
            NetBeansUtils.setJavaHome(installLocation, jdkHome);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.jdk.home"),  // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.netbeans.clusters")); // NOI18N
            
            NetBeansUtils.addCluster(installLocation, PLATFORM_CLUSTER);
            NetBeansUtils.addCluster(installLocation, NB_CLUSTER);
            NetBeansUtils.addCluster(installLocation, IDE_CLUSTER);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.netbeans.clusters"),  // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.product.id")); // NOI18N
            
            filesList.add(NetBeansUtils.createProductId(installLocation));
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.product.id"),  // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.license.accepted")); // NOI18N
            
            filesList.add(
                    NetBeansUtils.createLicenseAcceptedMarker(installLocation));
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.license.accepted"),  // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.irrelevant.files")); // NOI18N
            
            SystemUtils.removeIrrelevantFiles(binSubdir);
            SystemUtils.removeIrrelevantFiles(etcSubdir);
            SystemUtils.removeIrrelevantFiles(platformCluster);
            SystemUtils.removeIrrelevantFiles(nbCluster);
            SystemUtils.removeIrrelevantFiles(ideCluster);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.irrelevant.files"),  // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.files.permissions")); // NOI18N
            
            SystemUtils.correctFilesPermissions(binSubdir);
            SystemUtils.correctFilesPermissions(etcSubdir);
            SystemUtils.correctFilesPermissions(platformCluster);
            SystemUtils.correctFilesPermissions(nbCluster);
            SystemUtils.correctFilesPermissions(ideCluster);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.files.permissions"),  // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        if (!SystemUtils.isMacOS()) {
            try {
                progress.setDetail(getString("CL.install.desktop")); // NOI18N
                
                SystemUtils.createShortcut(
                        getDesktopShortcut(installLocation),
                        ShortcutLocationType.CURRENT_USER_DESKTOP);
            } catch (NativeException e) {
                throw new InstallationException(
                        getString("CL.install.error.desktop"),  // NOI18N
                        e);
            }
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.start.menu")); // NOI18N
            
            SystemUtils.createShortcut(
                    getStartMenuShortcut(installLocation),
                    ShortcutLocationType.CURRENT_USER_START_MENU);
        } catch (NativeException e) {
            throw new InstallationException(
                    getString("CL.install.error.start.menu"),  // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.netbeans.conf")); // NOI18N
            
            NetBeansUtils.updateNetBeansHome(installLocation);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.netbeans.conf"),  // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.glassfish.integration")); // NOI18N
            
            final List<Product> glassfishes =
                    Registry.getInstance().getProducts("glassfish");
            for (Product glassfish: glassfishes) {
                if (glassfish.getStatus() == Status.INSTALLED) {
                    final File gfLocation = glassfish.getInstallationLocation();
                    
                    if (gfLocation != null) {
                        NetBeansUtils.setJvmOption(
                                installLocation,
                                JVM_OPTION_NAME,
                                gfLocation.getAbsolutePath(),
                                true);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.glassfish.integration"),  // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public void uninstall(Progress progress) throws UninstallationException {
        final Product product = getProduct();
        final File installLocation = product.getInstallationLocation();
        
        NetBeansUtils.warnNetbeansRunning(installLocation);
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.uninstall.start.menu")); // NOI18N
            
            SystemUtils.removeShortcut(
                    getStartMenuShortcut(installLocation),
                    ShortcutLocationType.CURRENT_USER_START_MENU,
                    true);
        } catch (NativeException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.start.menu"),  // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        if (!SystemUtils.isMacOS()) {
            try {
                progress.setDetail(getString("CL.uninstall.desktop")); // NOI18N
                
                SystemUtils.removeShortcut(
                        getDesktopShortcut(installLocation),
                        ShortcutLocationType.CURRENT_USER_DESKTOP,
                        false);
            } catch (NativeException e) {
                throw new UninstallationException(
                        getString("CL.uninstall.error.desktop"),  // NOI18N
                        e);
            }
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
    public boolean wrapForMacOs() {
        return true;
    }
    
    @Override
    public boolean correctForMacOs() {
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
    
    // private //////////////////////////////////////////////////////////////////////
    private Shortcut getDesktopShortcut(final File directory) {
        return getShortcut(
                getString("CL.desktop.shortcut.name"), // NOI18N
                getString("CL.desktop.shortcut.description"), // NOI18N
                getString("CL.desktop.shortcut.path"), // NOI18N
                directory);
    }
    
    private Shortcut getStartMenuShortcut(final File directory) {
        return getShortcut(
                getString("CL.start.menu.shortcut.name"), // NOI18N
                getString("CL.start.menu.shortcut.description"), // NOI18N
                getString("CL.start.menu.shortcut.path"), // NOI18N
                directory);
    }
    
    private Shortcut getShortcut(
            final String name,
            final String description,
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
        
        final Shortcut shortcut = new Shortcut(name, executable);
        
        shortcut.setDescription(description);
        shortcut.setCategories(SHORTCUT_CATEGORIES);
        shortcut.setFileName(SHORTCUT_FILENAME);
        shortcut.setIcon(icon);
        shortcut.setRelativePath(relativePath);
        shortcut.setWorkingDirectory(location);
        shortcut.setModifyExecutablePath(true);
        
        return shortcut;
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
    
    public static final String PLATFORM_UID =
            "nb-platform"; // NOI18N
    
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
        "Application", // NOI18N
        "Programming", // NOI18N
        "Development" // NOI18N
    };
    
    public static final String JVM_OPTION_NAME =
            "-Dcom.sun.aas.installRoot"; // NOI18N
}
