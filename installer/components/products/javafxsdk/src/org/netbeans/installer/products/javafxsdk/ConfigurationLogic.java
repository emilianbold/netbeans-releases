/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.installer.products.javafxsdk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaFXUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.helper.NbiThread;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.progress.CompositeProgress;

/**
 *
 * @author ynov
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
        if(progress.isCanceled()) return;
        final Product product = getProduct();
        final File sdkLocation = product.getInstallationLocation();
        final File runtimeLocation = findRuntimeInstallationLocation();
        LogManager.log("JavaFX Runtime installation location is " + runtimeLocation);
        LogManager.log("JavaFX SDK installation location is " + sdkLocation);
        if(SystemUtils.isWindows()) {
            try {                
                ExecutionResults results = null;
                boolean runtimeInstallation = false;
                /*final CompositeProgress overallProgress = new CompositeProgress();
                overallProgress.synchronizeTo(progress);
                overallProgress.synchronizeDetails(true);

                //if sdk is installed, recheck just in case
                if (isJavaFXSDKInstalled(product, sdkLocation)) {
                    LogManager.log("... JavaFX SDK " + getProduct().getVersion() +
                            " is already installed, skipping SDK and Runtime configuration");
                } else {
                    final Progress sdkProgress = new Progress();
                    final Progress runtimeProgress = new Progress();
                    boolean runtimeToBeInstalled = true;
                    if(isJavaFXRuntimeInstalled(product, runtimeLocation)) {
                         LogManager.log("... JavaFX Runtime is already installed, skipping its configuration");
                         runtimeToBeInstalled = false;
                    }                    
                    if(runtimeToBeInstalled) {
                        overallProgress.addChild(sdkProgress, progress.COMPLETE * 15 / 20 );
                        overallProgress.addChild(runtimeProgress, progress.COMPLETE * 5 / 20);
                    } else {
                        overallProgress.addChild(sdkProgress, progress.COMPLETE);
                    }
                    final File sdkInstaller = getSDKWindowsInstaller();
                    results = runSDKInstallerWindows(sdkLocation, sdkInstaller, sdkProgress);                    
                    //if(results.getErrorCode()==0) {
                       // getProduct().setProperty(SDK_INSTALLED_WINDOWS_PROPERTY,
                       //         "" + true);
                    //}
                    //addUninsallationJVM(results, sdkLocation);         
                    if(!progress.isCanceled() && results.getErrorCode() == 0) {
                        if(runtimeToBeInstalled) {
                            runtimeInstallation = true;
                            final File runtimeInstaller = getRuntimeWindowsInstaller();
                            if(runtimeInstaller!=null) {
                                results = runRuntimeInstallerWindows(runtimeLocation, runtimeInstaller, runtimeProgress);
                                //configureJREProductWindows(results);
                            }
                        }
                    }
                } */

                if(results != null && results.getErrorCode()!=0) {
                    throw new InstallationException(
                            ResourceUtils.getString(ConfigurationLogic.class,
                            ((runtimeInstallation) ? ERROR_RUNTIME_INSTALL_SCRIPT_RETURN_NONZERO_KEY
                            : ERROR_SDK_INSTALL_SCRIPT_RETURN_NONZERO_KEY),
                            StringUtils.EMPTY_STRING + results.getErrorCode()));
                }
                if(!isJavaFXRuntimeInstalled(product, runtimeLocation)) {
                    throw new InstallationException(ERROR_RUNTIME_NOT_INSTALLED);
                }
                if(!isJavaFXSDKInstalled(product, sdkLocation)) {
                    throw new InstallationException(ERROR_SDK_NOT_INSTALLED);
                }
            } finally {
                /*try {
                    FileUtils.deleteFile(new File(sdkLocation, JAVAFX_SDK_MSI_FILE_NAME));
                    FileUtils.deleteFile(new File(sdkLocation, JAVAFX_SDK_DATA_FILE_NAME));
                    FileUtils.deleteFile(new File(sdkLocation, JAVAFX_RUNTIME_INSTALLER_DIR), true);
                } catch (IOException e) {
                    LogManager.log("Cannot delete JavaFX installer files ", e);
                }*/
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
            if(runtimeLocation == null || FileUtils.isEmpty(runtimeLocation) || FileUtils.isEmpty(sdkLocation)) {
                    LogManager.log(ErrorLevel.ERROR, getString("CL.install.error.ide.integration.no.javafx.found"));
            } else {
                try {
                    progress.setDetail(getString("CL.install.ide.integration")); // NOI18N

                    final List<Product> ides =
                            Registry.getInstance().getProducts("nb-base");
                    List<Product> productsToIntegrate = new ArrayList<Product>();
                    for (Product ide : ides) {
                        if (ide.getStatus() == Status.INSTALLED) {
                            LogManager.log("... checking if " + getProduct().getDisplayName() + " can be integrated with " + ide.getDisplayName() + " at " + ide.getInstallationLocation());
                            final File location = ide.getInstallationLocation();
                            if (location != null && FileUtils.exists(location) && !FileUtils.isEmpty(location)) {
                                final Product bundledProduct = bundledRegistry.getProduct(ide.getUid(), ide.getVersion());
                                if (bundledProduct != null) {
                                    //one of already installed IDEs is in the bundled registry as well - we need to integrate with it
                                    productsToIntegrate.add(ide);
                                    LogManager.log("... will be integrated since this produce is also bundled");
                                } else {
                                    //check if this IDE is not integrated with any other WL instance - we need integrate with such IDE instance
                                    try {
                                        if(!isJavaFXRegistred(location)) {
                                            LogManager.log("... will be integrated since there it is not yet integrated with any instance or such an instance does not exist");
                                            productsToIntegrate.add(ide);
                                        } else {
                                            LogManager.log("... will not be integrated since it is already integrated with another instance");
                                        }
                                    } catch (IOException e)  {
                                        LogManager.log(e);
                                    }
                                }
                            }
                        }
                    }

                    for (Product productToIntegrate : productsToIntegrate) {
                        final File location = productToIntegrate.getInstallationLocation();
                        LogManager.log("... integrate " + getProduct().getDisplayName() + " with " + productToIntegrate.getDisplayName() + " installed at " + location);

                        if(!registerJavaFX(location, sdkLocation, runtimeLocation)) {
                                continue;
                        }

                        // if the IDE was installed in the same session as the
                        // javafx sdk, we should add its "product id" to the IDE
                        if (productToIntegrate.hasStatusChanged()) {
                            NetBeansUtils.addPackId(
                                    location,
                                    PRODUCT_ID);
                            //TODO!!!, maybe move to registerJavaFX and do for every productToIntegrate
                            //addFiles(productToIntegrate.getInstalledFiles(),new File (location, "nb/config/JavaFX/Instances/javafx_sdk_autoregistered_instance"));
                            //addFiles(productToIntegrate.getInstalledFiles(),new File (location, "nb/config/JavaFX/Instances/.nbattrs"));
                        }
                    }
                } catch  (IOException e) {
                    throw new InstallationException(
                            getString("CL.install.error.ide.integration"), // NOI18N
                            e);
                }
            }
        }
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
       
    }   
    
    private boolean isJavaFXRegistred(File nbLocation) throws IOException {
        return new File (nbLocation, "nb/config/JavaFX/Instances/javafx_sdk_autoregistered_instance").exists();
    }
  
    private boolean registerJavaFX(File nbLocation, File sdkLocation, File reLocation) throws IOException {
        File javaExe = JavaUtils.getExecutable(new File(System.getProperty("java.home")));
        String [] cp = {
            "platform/core/core.jar",
            "platform/lib/boot.jar",
            "platform/lib/org-openide-modules.jar",
            "platform/core/org-openide-filesystems.jar",
            "platform/lib/org-openide-util.jar",
            "platform/lib/org-openide-util-lookup.jar",
            "javafx/modules/org-netbeans-modules-javafx2-platform.jar"
        };
        for(String c : cp) {
            File f = new File(nbLocation, c);
            if(!FileUtils.exists(f)) {
                LogManager.log("... cannot find jar required for JavaFX integration: " + f);
                return false;
            }
        }
        String mainClass = "org.netbeans.modules.javafx2.platform.registration.AutomaticRegistration";
        List <String> commands = new ArrayList <String> ();
        File nbCluster = new File(nbLocation, "nb");
        commands.add(javaExe.getAbsolutePath());
        commands.add("-cp");
        commands.add(StringUtils.asString(cp, File.pathSeparator));
        commands.add(mainClass);        
        commands.add(nbCluster.getAbsolutePath());     
        commands.add(sdkLocation.getAbsolutePath());
        commands.add(reLocation.getAbsolutePath());
        
        return SystemUtils.executeCommand(nbLocation, commands.toArray(new String[]{})).getErrorCode() == 0;
    }
    private  void removeJavaFXIntegration(File nbLocation,  File wlLocation, File domaindir) throws IOException {
        LogManager.log("... ide location is " + nbLocation);
        FileUtils.deleteFile(new File (nbLocation, "nb/config/JavaFX/Instances/javafx_sdk_autoregistered_instance"));
        FileUtils.deleteFile(new File (nbLocation, "nb/config/JavaFX/Instances/.nbattrs"));
    }
    
    
    /**      
     * @param product
     * @param installLocation
     * @return true if windows registry contains info about SDK AND installLocation is not empty
     */
    private boolean isJavaFXSDKInstalled(Product product, File installLocation) {
        return JavaFXUtils.isJavaFXSDKInstalled(product.getPlatforms().get(0), product.getVersion()) &&
               !FileUtils.isEmpty(installLocation);
    }
    
    /**      
     * @param product
     * @param installLocation
     * @return true if windows registry contains info about Runtime AND installLocation is not empty
     */
    private boolean isJavaFXRuntimeInstalled(Product product, File installLocation) {
        return JavaFXUtils.isJavaFXRuntimeInstalled(product.getPlatforms().get(0), product.getVersion()) &&
               !FileUtils.isEmpty(installLocation);
    }

    public void uninstall(final Progress progress)
            throws UninstallationException {    
    }

    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }

    @Override
    public boolean allowModifyMode() {
        return false;
    }


    public boolean requireLegalArtifactSaving() {     
       return false;                                   
    }

    private ExecutionResults runSDKInstallerWindows(File location, File sdkInstaller,
            Progress progress) throws InstallationException {
        progress.setDetail(PROGRESS_DETAIL_RUNNING_SDK_INSTALLER);
        LogManager.log("... installing JavaFX SDK");
        final File logFile = getLog("javafx_sdk_install");
        final String packageOption = "/i \"" + sdkInstaller.getAbsolutePath() +"\" ";
        final String loggingOption = (logFile!=null) ?
            "/log \"" + logFile.getAbsolutePath()  +"\" ":
            EMPTY_STRING;
        final String installLocationOption = "/qn INSTALLDIR=\"" +  location + "\"";        
        String [] commands = new String [] {
            "CMD",
            "/C",
            "msiexec.exe " + packageOption + loggingOption + installLocationOption
            //"msiexec.exe /i D:\\NBI\\FXSDK_bundle\\test msi\\fx2.0.msi /log \"D:\\NBI\\FXSDK_bundle\\test space\\log.log1\" /qn"
        };
   
        ProgressThread progressThread = new ProgressThread(progress,
                new File [] {location},
                getSDKinstallationSize() + getProduct().getDownloadSize());
        try {            
            progressThread.start();
            return SystemUtils.executeCommand(commands);
        } catch (IOException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_INSTALL_SDK_EXCEPTION),e);
        } finally {
            progressThread.finish();
            LogManager.log("... installation of JavaFX SDK completed");
            progress.setPercentage(progress.COMPLETE);
        }
    } 
    
    private ExecutionResults runRuntimeInstallerWindows(File location,
            File runtimeInstaller, Progress progress) throws InstallationException {
        progress.setDetail(PROGRESS_DETAIL_RUNNING_RE_INSTALLER);
        LogManager.log("... installing JavaFX Runtime");
        final File logFile = getLog("javafx_runtime_install");
        final String packageOption = "/i \"" + runtimeInstaller.getAbsolutePath() +"\" /qn ";
        final String loggingOption = (logFile!=null) ?
            "/log \"" + logFile.getAbsolutePath()  +"\"":
            EMPTY_STRING;
        String [] commands = new String [] {
            "CMD",
            "/C",
            "msiexec.exe " + packageOption + loggingOption
            //"msiexec.exe /i D:\\NBI\\FXSDK_bundle\\test msi\\fx2.0.msi /log \"D:\\NBI\\FXSDK_bundle\\test space\\log.log1\" /qn"
        };
   
        List<File> directories = new ArrayList<File>();
        directories.add(location);
        long maxDeltaSize = getRuntimeInstallationSize();

        ProgressThread progressThread = new ProgressThread(progress,
                directories.toArray(new File[directories.size()]),
                maxDeltaSize);
        try {
            progressThread.start();
            return SystemUtils.executeCommand(commands);
        } catch (IOException e) {
            throw new InstallationException(ERROR_INSTALL_RE_EXCEPTION,e);
        } finally {
            progressThread.finish();
            LogManager.log("... installation of JavaFX Runtime completed");
            progress.setPercentage(Progress.COMPLETE);
        }
    }

    /** Find path to public Runtime installer ie. msi file
     * @return null if msi file for given JRE version is not found
     */
    private File getRuntimeWindowsInstaller() {
        File installerFile = new File(getProduct().getInstallationLocation(),
                JAVAFX_RUNTIME_INSTALLER_DIR + File.separator + JAVAFX_RUNTIME_MSI_FILE_NAME );
        if (!installerFile.exists()) {
                LogManager.log("... JavaFX Runtime installer doesn`t exist : " + installerFile);
                return null;
        }
        return installerFile;
    }

    /** Find path to public Runtime installer ie. msi file
     * @return null if msi file for given JRE version is not found
     */
    private File getSDKWindowsInstaller() {
        File installerFile = new File(getProduct().getInstallationLocation(),
                JAVAFX_SDK_MSI_FILE_NAME);
        if (!installerFile.exists()) {
                LogManager.log("... JavaFX SDK installer doesn`t exist : " + installerFile);
                return null;
        }
        return installerFile;
    }

    /** Find path to JavaFX RE
     * @return null if RE is not installed
     */
    private File findRuntimeInstallationLocation() {
        String runtimeLocationPath =  SystemUtils.resolveString(
                getProduct().getProperty(JAVAFX_RUNTIME_INSTALLATION_LOCATION_PROPERTY),
                getProduct().getClassLoader());
        return (runtimeLocationPath != null)? new File(runtimeLocationPath) : null;
    }

    private File getLog(String suffix) {
        File logFile = LogManager.getLogFile();
        File resultLogFile = null;

        if(logFile!=null) {
            String name = logFile.getName();

            if(name.lastIndexOf(".")==-1) {
                name += "_";
                name += suffix;
                name += ".log";
            } else {
                String ext = name.substring(name.lastIndexOf("."));
                name = name.substring(0, name.lastIndexOf("."));
                name += "_";
                name += suffix;
                name += ext;
            }
            resultLogFile = new File(LogManager.getLogFile().getParentFile(),name);
        }
        return resultLogFile;
    }

   class ProgressThread extends NbiThread {
        private File [] directories ;
        private long deltaSize = 0;
        private long initialSize = 0L;
        private Progress progress;
        private final Object LOCK = new Object();
        private boolean loop = false;
        
        public ProgressThread(Progress progress, File [] directories, final long maxDeltaSize) {
            LogManager.log("... new ProgressThread created");
            this.directories = directories;
            for(File directory : directories) {
                if(directory.exists()) {
                    initialSize += FileUtils.getSize(directory);
                }
            }
            this.deltaSize = maxDeltaSize;
            this.progress = progress;
            LogManager.log("... directories : " + StringUtils.asString(directories));
            LogManager.log("...   initial : " + initialSize);
            LogManager.log("...     delta : " + deltaSize);
        }
        public void run() {
            LogManager.log("... progress thread started");
            long sleepTime = 1000L;
            try {
                synchronized (LOCK) {
                    loop = true;
                }
                while (isRunning()) {
                    try {
                        boolean update = false;
                        for(File directory : directories) {
                            if (directory.exists()) {
                                update = true;
                            }
                        }
                        if(update) {
                            updateProgressBar();
                        }
                        Thread.currentThread().sleep(sleepTime);
                    } catch (InterruptedException ex) {
                        LogManager.log(ex);
                        break;
                    } catch (Exception ex) {
                        LogManager.log(ex);
                        break;
                    }
                }
            }  finally {
                synchronized (LOCK) {
                    LOCK.notify();
                }
            }
            progress.setPercentage(Progress.COMPLETE);
            LogManager.log("... progress thread finished");
        }
        public void finish() {
            if(!isRunning()) return;
            synchronized (LOCK) {
                loop = false;
            }
            synchronized (LOCK) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e){
                    LogManager.log(e);
                }
            }
        }
        private boolean isRunning() {
            boolean result;
            synchronized (LOCK) {
                result = loop;
            }
            return result;
        }
        private void updateProgressBar() {
            //LogManager.log("... get directory size");
            long size = 0;
            for(File directory : directories) {
                if(directory.exists()) {
                    size+=FileUtils.getSize(directory);
                }
            }
            //LogManager.log("... size : " + size);
            long d = progress.COMPLETE * (size - initialSize) / deltaSize;
            //LogManager.log(".... real progress : " + d);
            d = progress.getPercentage() + (d  - progress.getPercentage() + 1) / 2;
            //LogManager.log("... bound progress : " + d);
            d = (d<0) ? 0 : (d > progress.COMPLETE ? progress.COMPLETE : d);
            if(((int)d) > progress.getPercentage()) {
                //LogManager.log("..... set progress : " + d);
                progress.setPercentage(d);
            }
        }
    }


    private long getSDKinstallationSize() {
        final long size;
        if(SystemUtils.isWindows()) {
            size = 68000000L ;
        } else {
            // who knows...
            size = 68000000L;
        }
        return size;
    }

    private long getRuntimeInstallationSize() {
        final long size;
        if(SystemUtils.isWindows()) {
            size = 30000000L ;
        } else {
            // who knows...
            size = 30000000L;
        }
        return size;
    }

   private void addFiles(FilesList list, File location) throws IOException {
        LogManager.log("...addFiles");
        if(FileUtils.exists(location)) {
            if(location.isDirectory()) {
                list.add(location);
                File [] files = location.listFiles();
                if(files!=null && files.length>0) {
                    for(File f: files) {
                        addFiles(list, f);
                    }
                }
            } else {
                LogManager.log("...Adding " + location.getAbsolutePath() + " to the list");
                list.add(location);
            }
        }
    }

    @Override
    public boolean registerInSystem() {
        return false;
    }

///////////////////////////////////////////////////////////////////////////////////
//// Constants
    public static final String BACK_SLASH = "\\"; // NOI18N
    public static final String FORWARD_SLASH = "/"; // NOI18N
    public static final String DOUBLE_BACK_SLASH = "\\\\"; // NOI18N
    public static final String QUOTE = "\""; // NOI18N
    public static final String EMPTY_STRING = ""; // NOI18N

    public static final String ENCODING_UTF8 =
            "UTF-8"; // NOI18N

    public static final String WIZARD_COMPONENTS_URI =
            "resource:" + // NOI18N
            "org/netbeans/installer/products/javafxsdk/wizard.xml"; // NOI18N
    public static final String JAVAFX_SDK_MSI_FILE_NAME =
            "fx2.0.msi";   // NOI18N
    public static final String JAVAFX_SDK_DATA_FILE_NAME =
            "Data1.cab";   // NOI18N
    public static final String JAVAFX_RUNTIME_MSI_FILE_NAME =
            "fxruntime.msi";   // NOI18N
    public static final String JAVAFX_RUNTIME_DATA_FILE_NAME =
            "Data1.cab";   // NOI18N
    public static final String JAVAFX_RUNTIME_INSTALLER_DIR =
            "runtime";   // NOI18N

    public static final String PRODUCT_ID =
            "JAVAFXSDK"; // NOI18N

    public static final String JAVAFX_RUNTIME_INSTALLATION_LOCATION_PROPERTY =
            "javafx.runtime.installation.location"; //NOI18N
    public static final String JAVAFX_RUNTIME_INSTALLER_FILE_NAME_PROPERTY =
            "javafx.runtime.installer.file.name"; //NOI18N

    public static final String ERROR_SDK_INSTALL_SCRIPT_RETURN_NONZERO_KEY =
            "CL.error.sdk.installation.return.nonzero";//NOI18
    public static final String ERROR_RUNTIME_INSTALL_SCRIPT_RETURN_NONZERO_KEY =
            "CL.error.runtime.installation.return.nonzero";//NOI18N
    public static final String ERROR_RUNTIME_NOT_INSTALLED =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.error.runtime.not.installed");//NOI18N
    public static final String ERROR_SDK_NOT_INSTALLED =                
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.error.sdk.not.installed");//NOI18N


    public static final String PROGRESS_DETAIL_RUNNING_SDK_INSTALLER =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.progress.detail.install.sdk");
    public static final String PROGRESS_DETAIL_RUNNING_RE_INSTALLER =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.progress.detail.install.runtime");
    public static final String ERROR_INSTALL_RE_EXCEPTION =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.error.install.runtime.exception");
    public static final String ERROR_INSTALL_SDK_EXCEPTION =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.error.install.sdk.exception");
}
