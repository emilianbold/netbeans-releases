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

package org.netbeans.installer.product.components;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.dependencies.Conflict;
import org.netbeans.installer.product.dependencies.InstallAfter;
import org.netbeans.installer.product.dependencies.Requirement;
import org.netbeans.installer.utils.helper.DependencyType;
import org.netbeans.installer.utils.helper.DetailedStatus;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.FileEntry;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.NbiClassLoader;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.helper.ApplicationDescriptor;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.helper.ExtendedUri;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.UnixNativeUtils;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Kirill Sorokin
 */
public final class Product extends RegistryNode {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Version version;
    private List<Platform> supportedPlatforms;
    
    private Status initialStatus;
    private Status currentStatus;
    
    private List<ExtendedUri> logicUris;
    private List<ExtendedUri> dataUris;
    
    private List<String> features;
    
    private long requiredDiskSpace;
    
    private List<Dependency> dependencies;
    
    private NbiClassLoader classLoader;
    private ProductConfigurationLogic configurationLogic;
    
    private Throwable installationError;
    private List<Throwable> installationWarnings;
    
    private Throwable uninstallationError;
    private List<Throwable> uninstallationWarnings;
    
    private FilesList installedFiles;
    
    private InstallationPhase installationPhase;
    
    // constructor //////////////////////////////////////////////////////////////////
    public Product() {
        supportedPlatforms = new ArrayList<Platform>();
        logicUris          = new ArrayList<ExtendedUri>();
        dataUris           = new ArrayList<ExtendedUri>();
        dependencies       = new ArrayList<Dependency>();
    }
    
    // essential functionality //////////////////////////////////////////////////////
    public void install(final Progress progress) throws InstallationException {
        final CompositeProgress totalProgress = new CompositeProgress();
        final CompositeProgress unjarProgress = new CompositeProgress();
        final Progress          logicProgress = new Progress();
        
        // initialization phase ////////////////////////////////////////////////
        installationPhase = InstallationPhase.INITIALIZATION;
        
        // load the component's configuration logic (it should be already
        // there, but we need to be sure)
        try {
            getLogic();
        } catch (InitializationException e) {
            throw new InstallationException(
                    "Cannot load configuration logic", e);
        }
        
        totalProgress.addChild(
                unjarProgress,
                Progress.COMPLETE - configurationLogic.getLogicPercentage());
        totalProgress.addChild(
                logicProgress,
                configurationLogic.getLogicPercentage());
        totalProgress.synchronizeTo(progress);
        totalProgress.synchronizeDetails(true);
        
        // check whether the installation location was set, if it's not we
        // cannot continue
        if (getInstallationLocation() == null) {
            throw new InstallationException(
                    "Installation location property is not set");
        }
        
        // initialize the local cache directory
        final File cache = getLocalCache();
        if (!cache.exists()) {
            if (!cache.mkdirs()) {
                throw new InstallationException(
                        "Cannot create the local cache directory for the product");
            }
        } else if (!cache.isDirectory()) {
            throw new InstallationException(
                    "The local cache directory for the product is not a directory");
        }
        
        // initialize the files list
        installedFiles = new FilesList();
        
        // check for cancel status
        if (progress.isCanceled()) return;
        
        // extraction phase /////////////////////////////////////////////////////////
        installationPhase = InstallationPhase.EXTRACTION;
        
        totalProgress.setTitle("Installing " + getDisplayName());
        
        final File contentsDir = new File(getInstallationLocation(), "Contents");
        final File macosDir = new File(contentsDir, "MacOS");
        final File resourcesDir = new File(contentsDir, "Resources");
        final File infoplist = new File(contentsDir, "Info.plist");
        
        // if we're running on macos x and the configuraion logic tells us that the
        // product should be automatically wrapped, we first create the required
        // directories structure and then extract the product
        if (SystemUtils.isMacOS() && configurationLogic.wrapForMacOs()) {
            setProperty(
                    "application.location",
                    getInstallationLocation().getAbsolutePath());
            setInstallationLocation(new File(resourcesDir,
                    getInstallationLocation().getName().replaceAll("\\.app$", "")));
            
            final UnixNativeUtils utils =
                    (UnixNativeUtils) SystemUtils.getNativeUtils();
            
            try {
                installedFiles.add(FileUtils.mkdirs(contentsDir));
                installedFiles.add(FileUtils.mkdirs(resourcesDir));
                installedFiles.add(FileUtils.mkdirs(macosDir));
                
                final String executableName = "executable"; //NOI18N
                
                installedFiles.add(utils.createSymLink(
                        new File(macosDir, executableName),
                        new File(getInstallationLocation(), configurationLogic.getExecutable())));
                
                final String iconName = "icon.icns"; //NOI18N
                
                installedFiles.add(utils.createSymLink(
                        new File(resourcesDir, iconName),
                        new File(getInstallationLocation(), configurationLogic.getIcon())));
                
                installedFiles.add(FileUtils.writeFile(infoplist, StringUtils.format(
                        INFO_PLIST_STUB,
                        getDisplayName(),
                        getVersion().toString(),
                        getVersion().toMinor(),
                        executableName,
                        iconName)));
            } catch (IOException e) {
                throw new InstallationException("Cannot wrap for MacOS", e);
            }
        }
        
        // extract each of the defined installation data files
        unjarProgress.setPercentage(Progress.COMPLETE % dataUris.size());
        unjarProgress.synchronizeDetails(true);
        for (ExtendedUri uri: dataUris) {
            final Progress currentProgress = new Progress();
            unjarProgress.addChild(
                    currentProgress,
                    Progress.COMPLETE / dataUris.size());
            
            // get the uri of the current data file
            final URI dataUri = uri.getLocal();
            if (dataUri == null) {
                throw new InstallationException("Installation data is not cached");
            }
            
            // convert it to a file and do some additional checks
            final File dataFile = new File(uri.getLocal());
            if (!dataFile.exists()) {
                throw new InstallationException("Installation data is not cached");
            }
            
            // exract it and add the files to the installed files list
            try {
                installedFiles.add(FileUtils.unjar(
                        dataFile,
                        getInstallationLocation(),
                        currentProgress));
            } catch (IOException e) {
                if (e.getMessage().equals("Not enough space")) {
                    throw new InstallationException(
                            "Cannot extract installation data -- not " +
                            "enough disk space in the target directory.", 
                            e);
                }
                throw new InstallationException(
                        "Cannot extract installation data", 
                        e);
            } catch (XMLException e) {
                throw new InstallationException("Cannot extract installation data", e);
            }
            
            // finally remove the data file
            try {
                FileUtils.deleteFile(dataFile);
                uri.setLocal(null);
            } catch (IOException e) {
                throw new InstallationException("Cannot clear installation data cache", e);
            }
        }
        
        // create legal/docs artifacts
        progress.setDetail("Creating legal artifacts");
        try {
            saveLegalArtifacts();
        } catch (IOException e) {
            addInstallationWarning(e);
        }
        
        // check for cancel status
        if (progress.isCanceled()) return;
        
        // custom configuration phase ///////////////////////////////////////////////
        installationPhase = InstallationPhase.CUSTOM_LOGIC;
        
        totalProgress.setTitle("Configuring " + getDisplayName());
        
        // run custom configuration logic
        progress.setDetail("Running custom configuration logic");
        configurationLogic.install(logicProgress);
        logicProgress.setPercentage(Progress.COMPLETE);
        progress.setDetail("");
        
        // check for cancel status
        if (progress.isCanceled()) return;
        
        // finalization phase ///////////////////////////////////////////////////////
        installationPhase = InstallationPhase.FINALIZATION;
        
        // register the component in the system install manager
        if (configurationLogic.registerInSystem()) {
            try {
                progress.setDetail("Registering in the system package manager");
                SystemUtils.addComponentToSystemInstallManager(getApplicationDescriptor());
            } catch (NativeException e) {
                LogManager.log("Integration with the system package manager failed");
                LogManager.log(e);
                addInstallationWarning(e);
            }
        }
        
        // save the installed files list
        progress.setDetail("Saving installed files list");
        try {
            installedFiles.saveXmlGz(getInstalledFilesList());
        } catch (XMLException e) {
            throw new InstallationException("Cannot save installed files list", e);
        }
        
        installationPhase = InstallationPhase.COMPLETE;
        progress.setPercentage(Progress.COMPLETE);
        setStatus(Status.INSTALLED);
    }
    
    public void rollback(final Progress progress) throws UninstallationException {
        final CompositeProgress totalProgress = new CompositeProgress();
        final Progress          logicProgress = new Progress();
        final Progress          eraseProgress = new Progress();
        
        // initialization ///////////////////////////////////////////////////////////
        
        // load the component's configuration logic (it should be already
        // there, but we need to be sure)
        try {
            getLogic();
        } catch (InitializationException e) {
            throw new UninstallationException(
                    "Cannot load configuration logic", e);
        }
        
        int logicChunk = (int) (progress.getPercentage() * (
                (float) configurationLogic.getLogicPercentage() /
                (float) Progress.COMPLETE));
        int eraseChunk = (int) (progress.getPercentage() * (1. - (
                (float) configurationLogic.getLogicPercentage() /
                (float) Progress.COMPLETE)));
        
        totalProgress.setPercentage(Progress.COMPLETE - logicChunk - eraseChunk);
        totalProgress.addChild(logicProgress, logicChunk);
        totalProgress.addChild(eraseProgress, eraseChunk);
        totalProgress.synchronizeDetails(true);
        totalProgress.reverseSynchronizeTo(progress);
        
        // rollback /////////////////////////////////////////////////////////////////
        
        // the starting point is chosen depending on the stage at which the
        // installation process was canceled, or failed; note that we intentionally
        // fall through all these cases, as they should be executed exactly in this
        // order and the only unclear point is where to start
        switch (installationPhase) {
            case COMPLETE:
            case FINALIZATION:
                try {
                    FileUtils.deleteFile(getInstalledFilesList());
                } catch (IOException e) {
                    ErrorManager.notifyWarning("Cannot delete installed files list", e);
                }
                
                if (configurationLogic.registerInSystem()) {
                    try {
                        SystemUtils.removeComponentFromSystemInstallManager(getApplicationDescriptor());
                    } catch (NativeException e) {
                        ErrorManager.notifyWarning("Cannot remove component from system registry", e);
                    }
                }
                
            case CUSTOM_LOGIC:
                configurationLogic.uninstall(logicProgress);
                
            case EXTRACTION:
                logicProgress.setPercentage(Progress.COMPLETE);
                
                // remove installation files
                int total   = installedFiles.getSize();
                int current = 0;
                
                for (FileEntry entry: installedFiles) {
                    current++;
                    
                    File file = entry.getFile();
                    
                    eraseProgress.setDetail("Deleting " + file);
                    eraseProgress.setPercentage(Progress.COMPLETE * current / total);
                    
                    try {
                        FileUtils.deleteFile(file);
                    } catch (IOException e) {
                        ErrorManager.notifyWarning("Cannot delete file", e);
                    }
                }
                
            case INITIALIZATION:
                eraseProgress.setPercentage(Progress.COMPLETE);
                // for initialization we don't need to do anything
                
            default:
                // default, nothing should be done here
        }
    }
    
    public void uninstall(final Progress progress) throws UninstallationException {
        final CompositeProgress totalProgress = new CompositeProgress();
        final Progress logicProgress = new Progress();
        final Progress eraseProgress = new Progress();
        
        // initialization phase /////////////////////////////////////////////////////
        
        // load the component's configuration logic (it should be already
        // there, but we need to be sure)
        try {
            getLogic();
        } catch (InitializationException e) {
            throw new UninstallationException(
                    "Cannot load configuration logic", e);
        }
        
        totalProgress.addChild(
                logicProgress,
                configurationLogic.getLogicPercentage());
        totalProgress.addChild(
                eraseProgress,
                Progress.COMPLETE - configurationLogic.getLogicPercentage());
        totalProgress.synchronizeTo(progress);
        totalProgress.synchronizeDetails(true);
        
        // load the installed files list
        try {
            installedFiles = new FilesList().loadXmlGz(getInstalledFilesList());
        } catch (XMLException e) {
            throw new UninstallationException("Cannot get the files list", e);
        }
        
        // custom logic phase ///////////////////////////////////////////////////////
        progress.setTitle("Unconfiguring " + getDisplayName());
        
        // run custom unconfiguration logic
        configurationLogic.uninstall(logicProgress);
        logicProgress.setPercentage(Progress.COMPLETE);
        progress.setDetail("");
        
        // files deletion phase /////////////////////////////////////////////////////
        progress.setTitle("Uninstalling " + getDisplayName());
        
        // remove installation files
        if (configurationLogic.getRemovalMode() == RemovalMode.ALL) {
            try {
                File startPoint = getInstallationLocation();
                if(SystemUtils.isMacOS() && configurationLogic.wrapForMacOs()) {
                    startPoint = startPoint.
                            getParentFile().
                            getParentFile().
                            getParentFile();
                }
                FileUtils.deleteFile(startPoint, true, eraseProgress);
            } catch (IOException e) {
                addUninstallationWarning(new UninstallationException(
                        "Cannot delete the file",
                        e));
            }
        } else {
            try {
                FileUtils.deleteFiles(installedFiles, eraseProgress);
            } catch (IOException e) {
                addUninstallationWarning(new UninstallationException(
                        "Cannot delete the file",
                        e));
            }
        }
        
        // remove the component from the native install manager
        if (configurationLogic.registerInSystem()) {
            try {
                SystemUtils.removeComponentFromSystemInstallManager(getApplicationDescriptor());
            } catch (NativeException e) {
                addUninstallationWarning(new UninstallationException("Cannot remove component from the native install manager", e));
            }
        }
        
        progress.setDetail("");
        // remove the files list
        try {
            FileUtils.deleteFile(getInstalledFilesList());
        } catch (IOException e) {
            addUninstallationWarning(new UninstallationException("Cannot delete installed files list", e));
        }
        
        progress.setPercentage(Progress.COMPLETE);
        setStatus(Status.NOT_INSTALLED);
    }
    
    // configuration logic //////////////////////////////////////////////////////////
    public List<ExtendedUri> getLogicUris() {
        return logicUris;
    }
    
    public void downloadLogic(final Progress progress) throws DownloadException {
        final CompositeProgress overallProgress = new CompositeProgress();
        
        final int percentageChunk = Progress.COMPLETE / logicUris.size();
        final int percentageLeak  = Progress.COMPLETE % logicUris.size();
        
        overallProgress.setPercentage(percentageLeak);
        overallProgress.synchronizeTo(progress);
        overallProgress.synchronizeDetails(true);
        
        for (ExtendedUri uri: logicUris) {
            final Progress currentProgress = new Progress();
            overallProgress.addChild(currentProgress, percentageChunk);
            
            final File cache = FileProxy.getInstance().getFile(
                    uri.getRemote(),
                    currentProgress);
            uri.setLocal(cache.toURI());
        }
    }
    
    public boolean isLogicDownloaded() {
        for (ExtendedUri uri: logicUris) {
            if (uri.getLocal() == null) {
                return false;
            }
        }
        
        return true;
    }
    
    public ProductConfigurationLogic getLogic() throws InitializationException {
        if (configurationLogic != null) {
            return configurationLogic;
        }
        
        if (!isLogicDownloaded()) {
            throw new InitializationException("Configuration logic is not yet downloaded");
        }
        
        try {
            String classname = null;
            for (ExtendedUri uri: logicUris) {
                classname = FileUtils.getJarAttribute(
                        new File(uri.getLocal()),
                        MANIFEST_LOGIC_CLASS);
                
                if (classname != null) {
                    break;
                }
            }
            
            classLoader = new NbiClassLoader(logicUris);
            
            configurationLogic = (ProductConfigurationLogic) classLoader.
                    loadClass(classname).newInstance();
            configurationLogic.setProduct(this);
            
            return configurationLogic;
        } catch (IOException e) {
            throw new InitializationException("Cannot load configuration logic", e);
        } catch (ClassNotFoundException e) {
            throw new InitializationException("Cannot load configuration logic", e);
        } catch (InstantiationException e) {
            throw new InitializationException("Cannot load configuration logic", e);
        } catch (IllegalAccessException e) {
            throw new InitializationException("Cannot load configuration logic", e);
        }
    }
    
    // installation data ////////////////////////////////////////////////////////////
    public List<ExtendedUri> getDataUris() {
        return dataUris;
    }
    
    public void downloadData(final Progress progress) throws DownloadException {
        final CompositeProgress overallProgress = new CompositeProgress();
        
        final int percentageChunk = Progress.COMPLETE / dataUris.size();
        final int percentageLeak  = Progress.COMPLETE % dataUris.size();
        
        overallProgress.setPercentage(percentageLeak);
        overallProgress.synchronizeTo(progress);
        overallProgress.synchronizeDetails(true);
        
        for (ExtendedUri uri: dataUris) {
            final Progress currentProgress = new Progress();
            overallProgress.addChild(currentProgress, percentageChunk);
            
            final File cache = FileProxy.getInstance().getFile(
                    uri.getRemote(),
                    currentProgress);
            uri.setLocal(cache.toURI());
        }
    }
    
    public boolean isDataDownloaded() {
        for (ExtendedUri uri: dataUris) {
            if (uri.getLocal() == null) {
                return false;
            }
        }
        
        return true;
    }
    
    // wizard ///////////////////////////////////////////////////////////////////////
    public List<WizardComponent> getWizardComponents() {
        try {
            return getLogic().getWizardComponents();
        } catch (InitializationException e) {
            ErrorManager.notifyError("Cannot get component's wizard components", e);
        }
        
        return null;
    }
    
    // status ///////////////////////////////////////////////////////////////////////
    public Status getStatus() {
        return currentStatus;
    }
    
    public void setStatus(final Status status) {
        if (initialStatus == null) {
            initialStatus = status;
        }
        
        currentStatus = status;
    }
    
    public boolean hasStatusChanged() {
        return currentStatus != initialStatus;
    }
    
    public DetailedStatus getDetailedStatus() {
        if (getStatus() == Status.INSTALLED) {
            if (getUninstallationError() != null) {
                return DetailedStatus.FAILED_TO_UNINSTALL;
            }
            if (hasStatusChanged() && (getInstallationWarnings() != null)) {
                return DetailedStatus.INSTALLED_WITH_WARNINGS;
            }
            if (hasStatusChanged()) {
                return DetailedStatus.INSTALLED_SUCCESSFULLY;
            }
        }
        
        if (getStatus() == Status.NOT_INSTALLED) {
            if (getInstallationError() != null) {
                return DetailedStatus.FAILED_TO_INSTALL;
            }
            if (hasStatusChanged() && (getUninstallationWarnings() != null)) {
                return DetailedStatus.UNINSTALLED_WITH_WARNINGS;
            }
            if (hasStatusChanged()) {
                return DetailedStatus.UNINSTALLED_SUCCESSFULLY;
            }
        }
        
        return null;
    }
    
    // dependencies /////////////////////////////////////////////////////////////////
    public List<Dependency> getDependencies() {
        return dependencies;
    }
    @Deprecated
    public List<Dependency> getDependencies(final DependencyType ... types) {
        Class [] classes = new Class[types.length];
        for(int i=0;i<types.length;i++) {
           classes[i] = toDependencyClass(types[i]);
        }
        return getDependencies(classes);
    }
    
    @Deprecated
    private Class <? extends Dependency> toDependencyClass(DependencyType type) {
         switch (type) {
                case REQUIREMENT:
                    return Requirement.class;
                case CONFLICT :
                    return Conflict.class; 
                case INSTALL_AFTER:
                    return InstallAfter.class; 
                default :
                    return null;
            }
    }
            
    public List<Dependency> getDependencies(Class ... dependencyClasses) {
        final List<Dependency> filtered = new ArrayList<Dependency>();
        
        for (Dependency dependency: dependencies) {
            for (Class clazz: dependencyClasses) {
                //if (clazz.isInstance(dependency)) {
                if (clazz.isInstance(dependency)) {
                    filtered.add(dependency);
                    break;
                }
            }
        }
        
        return filtered;
    }
    
    public boolean satisfies(final Dependency dependency) {
        return dependency.satisfies(this);
    }
    
    public List<Dependency> getDependencyByUid(String dependentUid) {
        final List<Dependency> filtered = new ArrayList<Dependency>();
        
        for (Dependency dependency: dependencies) {
            if (dependency.getUid().equals(dependentUid)) {
                filtered.add(dependency);
            }
        }
        
        return filtered;
    }
    
    // system requirements //////////////////////////////////////////////////////////
    public long getRequiredDiskSpace() {
        return requiredDiskSpace;
    }
    
    // install-time error/warnings //////////////////////////////////////////////////
    public Throwable getInstallationError() {
        return installationError;
    }
    
    public void setInstallationError(final Throwable error) {
        installationError = error;
    }
    
    public List<Throwable> getInstallationWarnings() {
        return installationWarnings;
    }
    
    public void addInstallationWarning(final Throwable warning) {
        if (installationWarnings == null) {
            installationWarnings = new ArrayList<Throwable>();
        }
        
        installationWarnings.add(warning);
    }
    
    // uninstall-time error/warnings ////////////////////////////////////////////////
    public Throwable getUninstallationError() {
        return uninstallationError;
    }
    
    public void setUninstallationError(final Throwable error) {
        uninstallationError = error;
    }
    
    public List<Throwable> getUninstallationWarnings() {
        return uninstallationWarnings;
    }
    
    public void addUninstallationWarning(final Throwable warning) {
        if (uninstallationWarnings == null) {
            uninstallationWarnings = new ArrayList<Throwable>();
        }
        
        uninstallationWarnings.add(warning);
    }
    
    // node <-> dom /////////////////////////////////////////////////////////////////
    protected String getTagName() {
        return "product";
    }
    
    public Element saveToDom(final Element element) throws FinalizationException {
        super.saveToDom(element);
        
        final Document document = element.getOwnerDocument();
        
        element.setAttribute("version", version.toString());
        element.setAttribute("platforms", StringUtils.asString(supportedPlatforms, " "));
        element.setAttribute("status", currentStatus.toString());
        element.setAttribute("features", StringUtils.asString(features, " "));
        
        element.appendChild(XMLUtils.saveExtendedUrisList(
                logicUris,
                document.createElement("configuration-logic")));
        
        element.appendChild(XMLUtils.saveExtendedUrisList(
                dataUris,
                document.createElement("installation-data")));
        
        final Element systemRequirementsElement =
                document.createElement("system-requirements");
        
        final Element diskSpaceElement = document.createElement("disk-space");
        diskSpaceElement.setTextContent(Long.toString(requiredDiskSpace));
        systemRequirementsElement.appendChild(diskSpaceElement);
        
        element.appendChild(systemRequirementsElement);
        
        if (dependencies.size() > 0) {
            element.appendChild(XMLUtils.saveDependencies(
                    dependencies,
                    document.createElement("dependencies")));
        }
        
        return element;
    }
    
    public Product loadFromDom(final Element element) throws InitializationException {
        
        super.loadFromDom(element);
        
        Element child;
        
        try {
            version =
                    Version.getVersion(element.getAttribute("version"));
            supportedPlatforms =
                    StringUtils.parsePlatforms(element.getAttribute("platforms"));
            
            initialStatus =
                    StringUtils.parseStatus(element.getAttribute("status"));
            currentStatus =
                    initialStatus;
            
            features = StringUtils.asList(element.getAttribute("features"), " ");
            
            logicUris.addAll(XMLUtils.parseExtendedUrisList(XMLUtils.getChild(
                    element,
                    "configuration-logic")));
            
            dataUris.addAll(XMLUtils.parseExtendedUrisList(XMLUtils.getChild(
                    element,
                    "installation-data")));
            
            requiredDiskSpace = Long.parseLong(XMLUtils.getChild(
                    element,
                    "system-requirements/disk-space").getTextContent());
            
            child = XMLUtils.getChild(element, "dependencies");
            if (child != null) {
                dependencies.addAll(XMLUtils.parseDependencies(child));
            }
        } catch (ParseException e) {
            throw new InitializationException("Could not load product", e);
        }
        
        return this;
    }
    
    // essential getters/setters ////////////////////////////////////////////////////
    public Version getVersion() {
        return version;
    }
    
    public List<Platform> getPlatforms() {
        return supportedPlatforms;
    }
    
    public List<String> getFeatures() {
        return features;
    }
    
    public File getInstallationLocation() {
        final String path = SystemUtils.resolveString(
                getProperty(INSTALLATION_LOCATION_PROPERTY),
                getClassLoader());
        
        return path == null ? null : new File(path);
    }
    
    public void setInstallationLocation(final File location) {
        setProperty(INSTALLATION_LOCATION_PROPERTY, location.getAbsolutePath());
    }
    
    public File getLocalCache() {
        return new File(
                Registry.getInstance().getLocalProductCache(),
                uid + File.separator + version);
    }
    
    public FilesList getInstalledFiles() {
        return installedFiles;
    }
    
    public File getInstalledFilesList() {
        return new File(
                getLocalCache(),
                INSTALLED_FILES_LIST_FILE_NAME);
    }
    
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    public long getDownloadSize() {
        long downloadSize = 0;
        
        for (ExtendedUri uri: logicUris) {
            downloadSize += uri.getSize();
        }
        for (ExtendedUri uri: dataUris) {
            downloadSize += uri.getSize();
        }
        
        return downloadSize;
    }
    
    private ApplicationDescriptor getApplicationDescriptor() {
        final String key = "nbi-" + uid + "-" + version;
        final String displayName = configurationLogic.getSystemDisplayName();
        final String icon;
        if (configurationLogic.getIcon() != null) {
            icon = new File(
                    getInstallationLocation(),
                    configurationLogic.getIcon()).getAbsolutePath();
        } else {
            icon = null;
        }
        
        String installLocation = getInstallationLocation().getAbsolutePath();
        if (SystemUtils.isMacOS() && configurationLogic.wrapForMacOs()) {
            final String applicationLocation = getProperty("application.location");
            
            if (applicationLocation != null) {
                installLocation = applicationLocation;
            }
        }
        
        final String[] modifyCommand = new String[] {
            "--target", uid, version.toString()};
        
        final String[] uninstallCommand = new String[] {
            "--target", uid, version.toString(), "--force-uninstall"};
        
        if (configurationLogic.allowModifyMode()) {
            return new ApplicationDescriptor(
                    key,
                    displayName,
                    icon,
                    installLocation,
                    uninstallCommand,
                    modifyCommand);
        } else {
            return new ApplicationDescriptor(
                    key,
                    displayName,
                    icon,
                    installLocation,
                    uninstallCommand,
                    null);
        }
    }
    
    // miscellanea //////////////////////////////////////////////////////////////////
    public boolean isCompatibleWith(final Platform platform) {
        for (Platform compatiblePlatform: supportedPlatforms) {
            if (compatiblePlatform.isCompatibleWith(platform)) {
                return true;
            }
        }
        
        return false;
    }
    
    private void saveLegalArtifacts() throws IOException {
        final Text license = configurationLogic.getLicense();
        if (license != null) {
            final File file = new File(
                    getInstallationLocation(),
                    "LICENSE-" + uid + license.getContentType().getExtension());
            
            FileUtils.writeFile(file, license.getText());
            installedFiles.add(file);
        }
        
        final Map<String, Text> thirdPartyLicenses = configurationLogic.getThirdPartyLicenses();
        if (thirdPartyLicenses != null) {
            final File file = new File(
                    getInstallationLocation(),
                    "THIRDPARTYLICENSES-" + uid + ".txt");
            
            for (String title: thirdPartyLicenses.keySet()) {
                FileUtils.appendFile(file,
                        "%% The following software may be included in this product: " + title + ";\n" +
                        "Use of any of this software is governed by the terms of the license below:\n\n");
                FileUtils.appendFile(file, thirdPartyLicenses.get(title).getText() + "\n\n");
            }
            
            installedFiles.add(file);
        }
        
        final Text thirdPartyLicense = configurationLogic.getThirdPartyLicense();
        if (thirdPartyLicense != null) {
            final File file = new File(
                    getInstallationLocation(),
                    "THIRDPARTYLICENSE-" + uid + thirdPartyLicense.getContentType().getExtension());
            
            FileUtils.writeFile(file, thirdPartyLicense.getText());
            installedFiles.add(file);
        }
        
        final Text releaseNotes = configurationLogic.getReleaseNotes();
        if (releaseNotes != null) {
            final File file = new File(
                    getInstallationLocation(),
                    "RELEASENOTES-" + uid + releaseNotes.getContentType().getExtension());
            
            FileUtils.writeFile(file, releaseNotes.getText());
            installedFiles.add(file);
        }
        
        final Text readme = configurationLogic.getReadme();
        if (readme != null) {
            final File file = new File(
                    getInstallationLocation(),
                    "README-" + uid + readme.getContentType().getExtension());
            
            FileUtils.writeFile(file, readme.getText());
            installedFiles.add(file);
        }
        
        final Text distributionReadme = configurationLogic.getDistributionReadme();
        if (distributionReadme != null) {
            final File file = new File(
                    getInstallationLocation(),
                    "DISTRIBUTION-" + uid + distributionReadme.getContentType().getExtension());
            
            FileUtils.writeFile(file, distributionReadme.getText());
            installedFiles.add(file);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private static enum InstallationPhase {
        INITIALIZATION,
        EXTRACTION,
        CUSTOM_LOGIC,
        FINALIZATION,
        COMPLETE;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String INSTALLATION_LOCATION_PROPERTY =
            "installation.location"; // NOI18N
    
    public static final String INSTALLED_FILES_LIST_FILE_NAME =
            "installed-files.xml.gz"; // NOI18N
    
    public static final String MANIFEST_LOGIC_CLASS =
            "Configuration-Logic-Class"; // NOI18N
    
    public static final String INFO_PLIST_STUB = FileUtils.INFO_PLIST_STUB;
            
}
