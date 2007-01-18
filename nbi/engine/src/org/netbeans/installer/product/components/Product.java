/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.netbeans.installer.product.components;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.utils.DetailedStatus;
import org.netbeans.installer.product.utils.Status;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.FileUtils;
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
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.helper.ExtendedURI;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.DependencyType;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.progress.ProgressAdapter;
import org.netbeans.installer.utils.progress.ProgressDetailAdapter;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Kirill Sorokin
 */
public final class Product extends RegistryNode {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Version version;
    private List<Platform> supportedPlatforms = new ArrayList<Platform>();
    
    private Status initialStatus;
    private Status currentStatus;
    
    private List<ExtendedURI> configurationLogicUris = new ArrayList<ExtendedURI>();
    private List<ExtendedURI> installationDataUris = new ArrayList<ExtendedURI>();
    
    private long requiredDiskSpace;
    
    private List<Product> requirements = new ArrayList<Product>();
    private List<Product> conflicts = new ArrayList<Product>();
    private List<Dependency> rawDependencies = new ArrayList<Dependency>();
    
    private NbiClassLoader classLoader;
    private ProductConfigurationLogic configurationLogic;
    
    private Throwable installationError;
    private List<Throwable> installationWarnings;
    
    private Throwable uninstallationError;
    private List<Throwable> uninstallationWarnings;
    
    private FilesList filesList;
    
    private InstallationPhase installationPhase = null;
    
    public void downloadConfigurationLogic(Progress progress) throws DownloadException {
        CompositeProgress composite = new CompositeProgress();
        ProgressAdapter   adapter   = new ProgressAdapter(composite, progress);
        Progress          childProgress;
        
        int percentageChunk = Progress.COMPLETE / configurationLogicUris.size();
        
        composite.setTitle("Downloading configuration logic for " + getDisplayName());
        
        FileProxy fileProxy = FileProxy.getInstance();
        for (ExtendedURI uri: configurationLogicUris) {
            childProgress = new Progress();
            new ProgressDetailAdapter(childProgress, composite);
            composite.addChild(childProgress, percentageChunk);
            composite.setDetail("Loading file from " + uri.getRemote());
            
            final File cache = fileProxy.getFile(uri.getRemote(), childProgress);
            uri.setLocal(cache.toURI());
        }
    }
    
    public boolean isConfigurationLogicDownloaded() {
        for (ExtendedURI uri: configurationLogicUris) {
            if (uri.getLocal() == null) {
                return false;
            }
        }
        
        return true;
    }
    
    public void downloadInstallationData(Progress progress) throws DownloadException {
        CompositeProgress composite = new CompositeProgress();
        ProgressAdapter   adapter   = new ProgressAdapter(composite, progress);
        Progress          childProgress;
        
        int percentageChunk = Progress.COMPLETE / installationDataUris.size();
        
        composite.setTitle("Downloading installation data for " + getDisplayName());
        
        FileProxy fileProxy = FileProxy.getInstance();
        for (ExtendedURI uri: installationDataUris) {
            childProgress = new Progress();
            new ProgressDetailAdapter(childProgress, composite);
            composite.addChild(childProgress, percentageChunk);
            composite.setDetail("Loading file from " + uri.getRemote());
            
            final File cache = fileProxy.getFile(uri.getRemote(), childProgress);
            uri.setLocal(cache.toURI());
        }
    }
    
    public boolean isInstallationDataDownloaded() {
        for (ExtendedURI uri: installationDataUris) {
            if (uri.getLocal() == null) {
                return false;
            }
        }
        
        return true;
    }
    
    public void install(final Progress progress) throws InstallationException {
        final CompositeProgress totalProgress = new CompositeProgress();
        final Progress          unjarProgress = new Progress();
        final Progress          logicProgress = new Progress();
        
        totalProgress.addChild(unjarProgress, 80);
        totalProgress.addChild(logicProgress, 20);
        totalProgress.synchronizeTo(progress);
        totalProgress.synchronizeDetails(true);
        
        // initialization phase ////////////////////////////////////////////////
        installationPhase = InstallationPhase.INITIALIZATION;
        
        // load the component's configuration logic (it should be already
        // there, but we need to be sure)
        try {
            loadConfigurationLogic();
        } catch (InitializationException e) {
            throw new InstallationException(
                    "Cannot load configuration logic", e);
        }
        
        // check whether the installation location was set, if it's not we
        // cannot continue
        if (getInstallationLocation() == null) {
            throw new InstallationException(
                    "Installation location property is not set");
        }
        
        // initialize the local cache directory
        final File cache = getCacheDirectory();
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
        filesList = new FilesList();
        
        // check for cancel status
        if (progress.isCanceled()) return;
        
        // extraction phase ////////////////////////////////////////////////////
        installationPhase = InstallationPhase.EXTRACTION;
        
        totalProgress.setTitle("Installing " + getDisplayName());
        
        // extract each of the defined installation data files
        for (ExtendedURI uri: installationDataUris) {
            final URI dataUri = uri.getLocal();
            if (dataUri == null) {
                throw new InstallationException("Installation data is not cached");
            }
            
            final File dataFile = new File(uri.getLocal());
            if (!dataFile.exists()) {
                throw new InstallationException("Installation data is not cached");
            }
            
            try {
                filesList.add(FileUtils.unjar(
                        dataFile,
                        getInstallationLocation(),
                        unjarProgress));
            } catch (IOException e) {
                throw new InstallationException("Cannot extract installation data", e);
            } catch (XMLException e) {
                throw new InstallationException("Cannot extract installation data", e);
            }
        }
        
        // create legal/docs artifacts
        progress.setDetail("Creating legal artifacts");
        try {
            saveLegalArtifacts(configurationLogic);
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
        
        // check for cancel status
        if (progress.isCanceled()) return;
        
        // finalization phase ///////////////////////////////////////////////////////
        installationPhase = InstallationPhase.FINALIZATION;
        
        // register the component in the system install manager
        if (configurationLogic.registerInSystem()) {
            try {
                progress.setDetail("Registering in the system package manager");
                SystemUtils.addComponentToSystemInstallManager(this);
            } catch (NativeException e) {
                addInstallationWarning(e);
            }
        }
        
        // save the installed files list
        progress.setDetail("Saving installed files list");
        try {
            filesList.saveXmlGz(getInstalledFilesList());
        } catch (XMLException e) {
            throw new InstallationException("Cannot save installed files list", e);
        }
        
        installationPhase = InstallationPhase.COMPLETE;
        progress.setPercentage(Progress.COMPLETE);
        setStatus(Status.INSTALLED);
    }
    
    public void rollback(final Progress progress) throws UninstallationException {
        final CompositeProgress totalProgress     = new CompositeProgress();
        final Progress          logicProgress = new Progress();
        final Progress          eraseProgress  = new Progress();
        
        totalProgress.addChild(logicProgress, 20);
        totalProgress.addChild(eraseProgress, 80);
        totalProgress.reverseSynchronizeTo(progress);
        
        // initialization ///////////////////////////////////////////////////////////
        
        // load the component's configuration logic (it should be already
        // there, but we need to be sure)
        try {
            loadConfigurationLogic();
        } catch (InitializationException e) {
            throw new UninstallationException(
                    "Cannot load configuration logic", e);
        }
        
        // rollback /////////////////////////////////////////////////////////////////
        totalProgress.setTitle("Rolling back " + getDisplayName());
        
        switch (installationPhase) {
            case FINALIZATION:
                try {
                    FileUtils.deleteFile(getInstalledFilesList());
                } catch (IOException e) {
                    ErrorManager.notify(ErrorLevel.WARNING, "Cannot delete installed files list", e);
                }
                
                if (configurationLogic.registerInSystem()) {
                    try {
                        SystemUtils.removeComponentFromSystemInstallManager(this);
                    } catch (NativeException e) {
                        ErrorManager.notify(ErrorLevel.WARNING, "Cannot remove component from system registry", e);
                    }
                }
                
            case CUSTOM_LOGIC:
                configurationLogic.uninstall(logicProgress);
                
            case EXTRACTION:
                logicProgress.setPercentage(Progress.COMPLETE);
                
                // remove installation files
                int total   = filesList.getSize();
                int current = 0;
                
                for (FileEntry entry: filesList) {
                    current++;
                    
                    File file = entry.getFile();
                    
                    eraseProgress.setDetail("Deleting " + file);
                    eraseProgress.setPercentage(Progress.COMPLETE * current / total);
                    
                    try {
                        FileUtils.deleteFile(file);
                    } catch (IOException e) {
                        ErrorManager.notify(ErrorLevel.WARNING, "Cannot delete file", e);
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
        final Progress          logicProgress = new Progress();
        final Progress          eraseProgress = new Progress();
        
        totalProgress.addChild(logicProgress, 20);
        totalProgress.addChild(eraseProgress, 80);
        totalProgress.synchronizeTo(progress);
        totalProgress.synchronizeDetails(true);
        
        // initialization phase /////////////////////////////////////////////////////
        
        // load the installed files list
        try {
            filesList = new FilesList().loadXmlGz(getInstalledFilesList());
        } catch (XMLException e) {
            throw new UninstallationException("Cannot get the files list", e);
        }
        
        // custom logic phase ///////////////////////////////////////////////////////
        progress.setTitle("Unconfiguring " + getDisplayName());
        
        // run custom unconfiguration logic
        try {
            loadConfigurationLogic().uninstall(logicProgress);
            
            logicProgress.setPercentage(Progress.COMPLETE);
        } catch (InitializationException e) {
            throw new UninstallationException("initialization failed", e);
        }
        
        // files deletion phase /////////////////////////////////////////////////////
        progress.setTitle("Uninstalling " + getDisplayName());
        
        // remove installation files
        int total   = filesList.getSize();
        int current = 0;
        
        for (FileEntry entry: filesList) {
            current++;
            
            File file = entry.getFile();
            
            eraseProgress.setDetail("Deleting " + file);
            eraseProgress.setPercentage(Progress.COMPLETE * current / total);
            
            try {
                FileUtils.deleteFile(file);
            } catch (IOException e) {
                addUninstallationWarning(new UninstallationException("Cannot delete the file", e));
            }
        }
        
        // remove the component from the native install manager
        if (configurationLogic.registerInSystem()) {
            try {
                SystemUtils.removeComponentFromSystemInstallManager(this);
            } catch (NativeException e) {
                addUninstallationWarning(new UninstallationException("Cannot remove component from the native install manager", e));
            }
        }
        
        // remove the files list
        try {
            FileUtils.deleteFile(getInstalledFilesList());
        } catch (IOException e) {
            addUninstallationWarning(new UninstallationException("Cannot delete installed files list", e));
        }
        
        progress.setPercentage(Progress.COMPLETE);
        setStatus(Status.NOT_INSTALLED);
    }
    
    private void saveLegalArtifacts(ProductConfigurationLogic configurationLogic) throws IOException {
        Text license = configurationLogic.getLicense();
        if (license != null) {
            File file = new File(
                    getInstallationLocation(),
                    "LICENSE-" + uid + license.getContentType().getExtension());
            
            FileUtils.writeFile(file, license.getText());
            filesList.add(file);
        }
        
        Map<String, Text> thirdPartyLicenses = configurationLogic.getThirdPartyLicenses();
        if (thirdPartyLicenses != null) {
            File file = new File(
                    getInstallationLocation(),
                    "THIRDPARTYLICENSES-" + uid + ".txt");
            
            for (String title: thirdPartyLicenses.keySet()) {
                FileUtils.appendFile(file,
                        "%% The following software may be included in this product: " + title + ";\n" +
                        "Use of any of this software is governed by the terms of the license below:\n\n");
                FileUtils.appendFile(file, thirdPartyLicenses.get(title).getText() + "\n\n");
            }
            
            filesList.add(file);
        }
        
        Text releaseNotes = configurationLogic.getReleaseNotes();
        if (releaseNotes != null) {
            File file = new File(
                    getInstallationLocation(),
                    "RELEASENOTES-" + uid + releaseNotes.getContentType().getExtension());
            
            FileUtils.writeFile(file, releaseNotes.getText());
            filesList.add(file);
        }
        
        Text readme = configurationLogic.getReadme();
        if (readme != null) {
            File file = new File(
                    getInstallationLocation(),
                    "README-" + uid + readme.getContentType().getExtension());
            
            FileUtils.writeFile(file, readme.getText());
            filesList.add(file);
        }
    }
    
    public List<WizardComponent> getWizardComponents() {
        try {
            return loadConfigurationLogic().getWizardComponents();
        } catch (InitializationException e) {
            ErrorManager.notify(ErrorLevel.ERROR,
                    "Cannot get component's wizard components", e);
        }
        
        return null;
    }
    
    public ProductConfigurationLogic loadConfigurationLogic() throws InitializationException {
        if (configurationLogic != null) {
            return configurationLogic;
        }
        
        if (!isConfigurationLogicDownloaded()) {
            throw new InitializationException("Configuration logic is not yet downloaded");
        }
        
        try {
            String classname = null;
            for (ExtendedURI uri: configurationLogicUris) {
                JarFile jar = new JarFile(new File(uri.getLocal()));
                classname = jar.getManifest().getMainAttributes().getValue(MANIFEST_LOGIC_CLASS);
                jar.close();
                
                if (classname != null) {
                    break;
                }
            }
            
            classLoader = new NbiClassLoader(configurationLogicUris);
            
            configurationLogic = (ProductConfigurationLogic) classLoader.loadClass(classname).newInstance();
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
    
    public File getCacheDirectory() {
        return new File(Registry.getInstance().getLocalProductCache(), uid + File.separator + version.toString());
    }
    
    private File getInstalledFilesList() {
        return new File(getCacheDirectory(), INSTALLED_FILES_LIST_FILE_NAME);
    }
    
    public Version getVersion() {
        return version;
    }
    
    public List<Platform> getSupportedPlatforms() {
        return supportedPlatforms;
    }
    
    public Status getStatus() {
        return currentStatus;
    }
    
    public void setStatus(final Status status) {
        if (initialStatus == null) {
            initialStatus = status;
        }
        
        currentStatus = status;
    }
    
    public boolean statusChanged() {
        return currentStatus != initialStatus;
    }
    
    public List<ExtendedURI> getConfigurationLogicUris() {
        return configurationLogicUris;
    }
    
    public List<ExtendedURI> getInstallationDataUris() {
        return installationDataUris;
    }
    
    public List<Product> getRequirements() {
        return requirements;
    }
    
    public void addRequirement(final Product component) {
        requirements.add(component);
    }
    
    public Product getRequirementByUid(final String uid) {
        for (Product component: getRequirements()) {
            if (component.getUid().equals(uid)) {
                return component;
            }
        }
        
        return null;
    }
    
    public List<Product> getConflicts() {
        return conflicts;
    }
    
    public void addConflict(final Product component) {
        conflicts.add(component);
    }
    
    public Product getConflictByUid(final String uid) {
        for (Product component: getConflicts()) {
            if (component.getUid().equals(uid)) {
                return component;
            }
        }
        
        return null;
    }
    
    public List<Dependency> getRawDependencies() {
        return rawDependencies;
    }
    
    public long getRequiredDiskSpace() {
        return requiredDiskSpace;
    }
    
    public File getInstallationLocation() {
        String path = SystemUtils.parseString(
                getProperty(INSTALLATION_LOCATION_PROPERTY),
                getClassLoader());
        
        return path == null ? null : new File(path);
    }
    
    public void setInstallationLocation(final File location) {
        setProperty(INSTALLATION_LOCATION_PROPERTY, location.getAbsolutePath());
    }
    
    // legal/documentation stuff ////////////////////////////////////////////////////
    public Text getLicense() throws InitializationException {
        return loadConfigurationLogic().getLicense();
    }
    
    public Map<String, Text> getThirdPartyLicenses() throws InitializationException {
        return loadConfigurationLogic().getThirdPartyLicenses();
    }
    
    public Text getReleaseNotes() throws InitializationException {
        return loadConfigurationLogic().getReleaseNotes();
    }
    
    public Text getReadme() throws InitializationException {
        return loadConfigurationLogic().getReadme();
    }
    
    public Text getInstallationInstructions() throws InitializationException {
        return loadConfigurationLogic().getInstallationInstructions();
    }
    
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    public boolean isVisible() {
        return this.visible;
    }
    
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }
    
    public List<RegistryNode> getVisibleChildren() {
        List<RegistryNode> visibleChildren = new LinkedList<RegistryNode>();
        
        for (RegistryNode child: children) {
            if (child.isVisible()) {
                visibleChildren.add(child);
            }
        }
        
        return visibleChildren;
    }
    
    public boolean requires(final Product component) {
        for (Product requirement: requirements) {
            if (requirement.equals(component) || requirement.requires(component)) {
                return true;
            }
        }
        
        return false;
    }
    
    public String toString() {
        return getDisplayName();
    }
    
    public long getDownloadSize() {
        long downloadSize = 0;
        
        for (ExtendedURI uri: configurationLogicUris) {
            downloadSize += uri.getSize();
        }
        for (ExtendedURI uri: installationDataUris) {
            downloadSize += uri.getSize();
        }
        
        return downloadSize;
    }
    
    public Throwable getInstallationError() {
        return installationError;
    }
    
    public void setInstallationError(Throwable error) {
        installationError = error;
    }
    
    public List<Throwable> getInstallationWarnings() {
        return installationWarnings;
    }
    
    public void addInstallationWarning(Throwable warning) {
        if (installationWarnings == null) {
            installationWarnings = new ArrayList<Throwable>();
        }
        
        installationWarnings.add(warning);
    }
    
    public Throwable getUninstallationError() {
        return uninstallationError;
    }
    
    public void setUninstallationError(Throwable error) {
        uninstallationError = error;
    }
    
    public List<Throwable> getUninstallationWarnings() {
        return uninstallationWarnings;
    }
    
    public void addUninstallationWarning(Throwable warning) {
        if (uninstallationWarnings == null) {
            uninstallationWarnings = new ArrayList<Throwable>();
        }
        
        uninstallationWarnings.add(warning);
    }
    
    public DetailedStatus getDetailedStatus() {
        if (getStatus() == Status.INSTALLED) {
            if (getUninstallationError() != null) {
                return DetailedStatus.FAILED_TO_UNINSTALL;
            }
            if (statusChanged() && (getInstallationWarnings() != null)) {
                return DetailedStatus.INSTALLED_WITH_WARNINGS;
            }
            if (statusChanged()) {
                return DetailedStatus.INSTALLED_SUCCESSFULLY;
            }
        }
        
        if (getStatus() == Status.NOT_INSTALLED) {
            if (getInstallationError() != null) {
                return DetailedStatus.FAILED_TO_INSTALL;
            }
            if (statusChanged() && (getUninstallationWarnings() != null)) {
                return DetailedStatus.UNINSTALLED_WITH_WARNINGS;
            }
            if (statusChanged()) {
                return DetailedStatus.UNINSTALLED_SUCCESSFULLY;
            }
        }
        
        return null;
    }
    
    public FilesList getInstalledFiles() {
        return filesList;
    }
    
    // node <-> dom /////////////////////////////////////////////////////////////////
    protected String getTagName() {
        return "product";
    }
    
    protected Element saveToDom(Element element) throws FinalizationException {
        super.saveToDom(element);
        
        final Document document = element.getOwnerDocument();
        
        element.setAttribute("version", getVersion().toString());
        element.setAttribute("platform", StringUtils.asString(getSupportedPlatforms(), " "));
        element.setAttribute("status", getStatus().toString());
        
        final Element logicNode = document.createElement("configuration-logic");
        for (ExtendedURI uri: configurationLogicUris) {
            final Element node = document.createElement("file");
            node.setAttribute("size", Long.toString(uri.getSize()));
            node.setAttribute("md5", uri.getMd5());
            
            final Element uriNode = document.createElement("default-uri");
            if (uri.getLocal() != null) {
                uriNode.setTextContent(uri.getLocal().toString());
            } else {
                uriNode.setTextContent(uri.getRemote().toString());
            }
            node.appendChild(uriNode);
            
            logicNode.appendChild(node);
        }
        element.appendChild(logicNode);
        
        final Element dataNode = document.createElement("installation-data");
        for (ExtendedURI uri: installationDataUris) {
            final Element node = document.createElement("file");
            node.setAttribute("size", Long.toString(uri.getSize()));
            node.setAttribute("md5", uri.getMd5());
            
            final Element uriNode = document.createElement("default-uri");
            if (uri.getLocal() != null) {
                uriNode.setTextContent(uri.getLocal().toString());
            } else {
                uriNode.setTextContent(uri.getRemote().toString());
            }
            node.appendChild(uriNode);
            
            dataNode.appendChild(node);
        }
        element.appendChild(dataNode);
        
        final Element requirementsNode = document.createElement("requirements");
        
        final Element diskSpaceNode = document.createElement("disk-space");
        diskSpaceNode.setTextContent(Long.toString(getRequiredDiskSpace()));
        requirementsNode.appendChild(diskSpaceNode);
        
        element.appendChild(requirementsNode);
        
        if (getRawDependencies().size() > 0) {
            final Element dependenciesNode = document.createElement("dependencies");
            
            for (Dependency dependency: getRawDependencies()) {
                Element dependencyNode =
                        document.createElement(dependency.getType().toString());
                
                dependencyNode.setAttribute("uid",
                        dependency.getUid());
                dependencyNode.setAttribute("version-lower",
                        dependency.getLower().toString());
                dependencyNode.setAttribute("version-upper",
                        dependency.getUpper().toString());
                
                if (dependency.getDesired() != null) {
                    dependencyNode.setAttribute("version-desired",
                            dependency.getDesired().toString());
                }
                
                dependenciesNode.appendChild(dependencyNode);
            }
            
            element.appendChild(dependenciesNode);
        }
        
        return element;
    }
    
    public Product loadFromDom(Element element) throws InitializationException {
        super.loadFromDom(element);
        
        List<Node> nodes;
        
        try {
            version = new Version(element.getAttribute("version"));
            
            supportedPlatforms = StringUtils.parsePlatforms(element.getAttribute("platform"));
            
            initialStatus = StringUtils.parseStatus(element.getAttribute("status"));
            currentStatus = initialStatus;
            
            nodes = XMLUtils.getChildList(element, "./configuration-logic/file");
            for (Node node: nodes) {
                configurationLogicUris.add(XMLUtils.parseExtendedUri((Element) node));
            }
            
            nodes = XMLUtils.getChildList(element, "./installation-data/file");
            for (Node node: nodes) {
                installationDataUris.add(XMLUtils.parseExtendedUri((Element) node));
            }
            
            requiredDiskSpace = Long.parseLong(XMLUtils.getChildNodeTextContent(element, "./requirements/disk-space"));
            
            nodes = XMLUtils.getChildList(element, "./dependencies/(" + DependencyType.REQUIREMENT + "," + DependencyType.CONFLICT + ")");
            for (Node node: nodes) {
                rawDependencies.add(XMLUtils.parseDependency((Element) node));
            }
        } catch (ParseException e) {
            throw new InitializationException("Could not load components", e);
        }
        
        return this;
    }
    
    ////////////////////////////////////////////////////////////////////////////
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
}
