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

package org.netbeans.installer.products.sjsam;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import org.netbeans.installer.utils.applications.GlassFishUtils;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.helper.NbiThread;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;

/**
 *
 * @author Kirill Sorokin
 */
public class ConfigurationLogic extends ProductConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String WIZARD_COMPONENTS_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX + // NOI18N
            "org/netbeans/installer/products/sjsam/wizard.xml"; // NOI18N
    
    private static final String GLASSFISH_UID =
            "glassfish"; // NOI18N
    private static final String APPSERVER_UID =
            "sjsas"; // NOI18N
    
    private static final String AM_INSTALLER =
            "addons/am_installer.jar"; // NOI18N
    private static final String AM_SUBDIR =
            "addons/accessmanager"; // NOI18N
    private static final String AM_CONFIGURATOR =
            "lib/addons/am-configurator.jar"; // NOI18N
    
    private static final String AMSERVER_DIR_INSIDE_AS =
            "domains/domain1/applications/j2ee-modules/amserver"; // NOI18N
    
    private static final String AM_ADDITIONAL_CP =
            "lib/appserv-ext.jar";
    
    private static final String ACCESS_MANAGER_UH =
            "AccessManager"; // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private List<WizardComponent> wizardComponents;
    
    public ConfigurationLogic() throws InitializationException {
        wizardComponents = Wizard.loadWizardComponents(
                WIZARD_COMPONENTS_URI,
                getClass().getClassLoader());
    }
    
    public void install(Progress progress) throws InstallationException {
        final File installLocation = getProduct().getInstallationLocation();
        
        // get the list of suitable glassfish installations
        final List<Dependency> dependencies =
                getProduct().getDependencyByUid(GLASSFISH_UID);
        final List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final File asLocation = sources.get(0).getInstallationLocation();
        
        // resolve the dependency
        dependencies.get(0).setVersionResolved(sources.get(0).getVersion());
        
        final File amInstaller = new File(asLocation, AM_INSTALLER);
        final File javaExecutable;
        try {
            javaExecutable = JavaUtils.getExecutable(
                    GlassFishUtils.getJavaHome(asLocation));
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.java.home"), // NOI18N
                    e);
        }
        progress.setPercentage(Progress.START);
        final int DELTA_PROGRESS = 10;
        // stop the default domain //////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.stop.as")); // NOI18N
            
            GlassFishUtils.stopDefaultDomain(asLocation);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.stop.as"), // NOI18N
                    e);
        }
        
        // run the access manager installer /////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.am.installer")); // NOI18N
            File extCp = new File(asLocation, AM_ADDITIONAL_CP);
            String mainClass = new JarFile(amInstaller).getManifest().
                    getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            
            ExecutionResults results = SystemUtils.executeCommand(
                    installLocation,
                    javaExecutable.getAbsolutePath(),
                    "-cp", // NOI18N
                    extCp.getAbsolutePath() +
                    SystemUtils.getPathSeparator() +
                    amInstaller.getAbsolutePath(),
                    mainClass,
                    asLocation.getAbsolutePath(),
                    "true", // NOI18N
                    "localhost"); // NOI18N
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.am.installer"), // NOI18N
                    e);
        }
        
        // install the jvm option ///////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.add.jvm.option")); // NOI18N
            
            GlassFishUtils.setJvmOption(
                    asLocation,
                    GlassFishUtils.DEFAULT_DOMAIN,
                    "-Dcom.sun.enterprise.server.ss.ASQuickStartup=false"); // NOI18N
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.add.jvm.option"), // NOI18N
                    e);
        } catch (XMLException e) {
            throw new InstallationException(
                    getString("CL.install.error.add.jvm.option"), // NOI18N
                    e);
        }
        progress.addPercentage(DELTA_PROGRESS);
        
        // start the default domain /////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.start.as")); // NOI18N
            
            GlassFishUtils.startDefaultDomain(asLocation);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.start.as"), // NOI18N
                    e);
        }
        
        progress.addPercentage(DELTA_PROGRESS);
        Thread th = new MoveProgressThread(progress, 5L * 60L, DELTA_PROGRESS * 7);
        th.start();
        
        // stop the default domain //////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.stop.as")); // NOI18N
            
            GlassFishUtils.stopDefaultDomain(asLocation);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.stop.as"), // NOI18N
                    e);
        }
        
        if(th.isAlive()) {
            LogManager.log("stopping moving progress thread...");
            th.stop();
        }
        // remove the jvm option ///////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.remove.jvm.option")); // NOI18N
            
            GlassFishUtils.removeJvmOption(
                    asLocation,
                    GlassFishUtils.DEFAULT_DOMAIN,
                    "-Dcom.sun.enterprise.server.ss.ASQuickStartup=false"); // NOI18N
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.remove.jvm.option"), // NOI18N
                    e);
        } catch (XMLException e) {
            throw new InstallationException(
                    getString("CL.install.error.remove.jvm.option"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public void uninstall(Progress progress) throws UninstallationException {
        // get the list of suitable glassfish installations
        final List<Dependency> dependencies =
                getProduct().getDependencyByUid(GLASSFISH_UID);
        final List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final File asLocation = sources.get(0).getInstallationLocation();
        
        final File amSubdir = new File(asLocation, AM_SUBDIR);
        final File amConfigurator = new File(asLocation, AM_CONFIGURATOR);
        
        final File amHomeFile = new File(
                new File(SystemUtils.getUserHomeDirectory(), ACCESS_MANAGER_UH),
                getAMServerLinkName(asLocation));
        
        // stop the default domain //////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.uninstall.stop.as")); // NOI18N
            
            GlassFishUtils.stopDefaultDomain(asLocation);
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.stop.as"), // NOI18N
                    e);
        }
        
        // remove some extra files //////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.uninstall.extra.files")); // NOI18N
            
            FileUtils.deleteFile(amSubdir, true);
            FileUtils.deleteFile(amConfigurator);
            FileUtils.deleteWithEmptyParents(amHomeFile);
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
    public boolean registerInSystem() {
        return false;
    }
    private class MoveProgressThread extends NbiThread {
        private Progress progress;
        public MoveProgressThread(final Progress progress, final long seconds, final int percentageDelta)  {
            super(new Runnable() {
                public void run() {
                    final long MLSEC = 1000L;
                    long startTime = System.currentTimeMillis();
                    long endTime   = startTime + MLSEC * seconds;
                    final int startPercentage = progress.getPercentage();
                    LogManager.log("... starting percentage : " + startPercentage);
                    LogManager.log("... end percentage : " + 
                            (startPercentage + percentageDelta));
                    long timePosition = startTime;
                    
                    while(timePosition < endTime) {
                        try {
                            sleep(MLSEC);
                            timePosition = System.currentTimeMillis();
                            final long current = startPercentage +
                                    ((timePosition - startTime) * percentageDelta) / (seconds * MLSEC);
                            if(current <= 100) {
                                if(progress.getPercentage()!=current) {
                                    LogManager.log("... setting percentage to " + current);
                                    progress.setPercentage(current);
                                }
                            } else {
                                LogManager.log("... 100% has been reached");
                            }
                        } catch (InterruptedException e) {
                            LogManager.log("Interrupted while sleeping", e);
                        }
                    }
                     LogManager.log("... end of progress moving thread");
                }
            });
        }
        
    }
    
    @Override
    public int getLogicPercentage() {
        return 90 ;
    }

    // private //////////////////////////////////////////////////////////////////////
    private String getAMServerLinkName(final File asLocation) {
        final File file = new File(asLocation, AMSERVER_DIR_INSIDE_AS);
        final File root = FileUtils.getRoot(file);
        
        String result = file.
                getPath().
                substring(root.getPath().length()).
                replace(File.separatorChar, '_');
        
        return "AMConfig" + "_" + result + "_";
    }
    @Override
    public RemovalMode getRemovalMode() {
        return RemovalMode.LIST;
    }
}
