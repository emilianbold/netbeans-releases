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

package org.netbeans.installer.product;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.dependencies.Conflict;
import org.netbeans.installer.product.dependencies.InstallAfter;
import org.netbeans.installer.product.dependencies.Requirement;
import org.netbeans.installer.product.filters.OrFilter;
import org.netbeans.installer.product.filters.ProductFilter;
import org.netbeans.installer.product.filters.GroupFilter;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.product.filters.TrueFilter;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.helper.DetailedStatus;
import org.netbeans.installer.utils.helper.ExtendedUri;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.ExecutionMode;
import org.netbeans.installer.utils.helper.Feature;
import org.netbeans.installer.utils.helper.FinishHandler;
import org.netbeans.installer.utils.helper.NbiProperties;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Kirill Sorokin
 */
public class Registry {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Registry instance;
    
    public static synchronized Registry getInstance() {
        if (instance == null) {
            instance = new Registry();
        }
        
        return instance;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private File localDirectory;
    private File localRegistryFile;
    private File localProductCache;
    
    private String localRegistryStubUri;
    private String bundledRegistryUri;
    private List<String>  remoteRegistryUris;
    private String registrySchemaUri;
    
    private String stateFileSchemaUri;
    private String stateFileStubUri;
    
    private List<Feature> features;
    
    private List<ExtendedUri> includes;
    
    private RegistryNode registryRoot;
    private NbiProperties properties;
    private Platform targetPlatform;
    
    private FinishHandler finishHandler;
    
    // constructors /////////////////////////////////////////////////////////////////
    public Registry() {
        localRegistryStubUri = DEFAULT_LOCAL_PRODUCT_REGISTRY_STUB_URI;
        bundledRegistryUri = DEFAULT_BUNDLED_PRODUCT_REGISTRY_URI;
        remoteRegistryUris = new ArrayList<String>();
        registrySchemaUri = DEFAULT_PRODUCT_REGISTRY_SCHEMA_URI;
        
        stateFileSchemaUri = DEFAULT_STATE_FILE_SCHEMA_URI;
        stateFileStubUri = DEFAULT_STATE_FILE_STUB_URI;
        
        features = new LinkedList<Feature>();
        
        includes = new LinkedList<ExtendedUri>();
        
        registryRoot = new Group();
        registryRoot.setRegistryType(RegistryType.LOCAL);
        
        properties = new NbiProperties();
        targetPlatform = SystemUtils.getCurrentPlatform();
    }
    
    // initialization/finalization //////////////////////////////////////////////////
    public void setLocalDirectory(
            final File localDirectory) {
        this.localDirectory = localDirectory;
        
        localProductCache = new File(
                localDirectory,
                DEFAULT_LOCAL_PRODUCT_CACHE_DIRECTORY_NAME);
        localRegistryFile = new File(
                localDirectory,
                DEFAULT_LOCAL_REGISTRY_FILE_NAME);
    }
    
    public void setFinishHandler(
            final FinishHandler finishHandler) {
        this.finishHandler = finishHandler;
    }
    
    public void setTargetPlatform(
            final Platform targetPlatform) {
        this.targetPlatform = targetPlatform;
    }
    
    public void initializeRegistry(
            final Progress progress) throws InitializationException {
        LogManager.logEntry("initializing product registry");
        
        setRegistryProperties();
        
        final CompositeProgress compositeProgress = new CompositeProgress();
        Progress childProgress;
        
        int percentageChunk = Progress.COMPLETE / (remoteRegistryUris.size() + 2);
        int percentageLeak = Progress.COMPLETE % (remoteRegistryUris.size() + 2);
        
        compositeProgress.synchronizeTo(progress);
        compositeProgress.synchronizeDetails(true);
        
        childProgress = new Progress();
        compositeProgress.addChild(childProgress, percentageChunk + percentageLeak);
        compositeProgress.setTitle(
                ResourceUtils.getString(Registry.class,
                LOADING_LOCAL_REGISTRY_KEY,
                localRegistryFile));
        
        try {
            loadProductRegistry(
                    localRegistryFile.toURI().toString(),
                    childProgress,
                    RegistryType.LOCAL,
                    false);
        } catch (InitializationException e) {
            if (!UiUtils.showYesNoDialog(
                    ResourceUtils.getString(
                    Registry.class, ERROR_LOADING_LOCAL_REGISTRY_TITLE_KEY),
                    ResourceUtils.getString(Registry.class,
                    ERROR_LOADING_LOCAL_REGISTRY_MESSAGE_KEY, localRegistryFile)
                    )) {
                finishHandler.criticalExit();
            } else {
                LogManager.log(ErrorLevel.ERROR, e);
            }
        }
        
        childProgress = new Progress();
        compositeProgress.addChild(childProgress, percentageChunk);
        compositeProgress.setTitle( ResourceUtils.getString(Registry.class,
                LOADING_BUNDLED_REGISTRY_KEY,
                bundledRegistryUri));
        
        try {
            loadProductRegistry(
                    bundledRegistryUri,
                    childProgress,
                    RegistryType.BUNDLED,
                    true);
        } catch (InitializationException e) {
            if (!UiUtils.showYesNoDialog(
                    ResourceUtils.getString(
                    Registry.class, ERROR_LOADING_BUNDLED_REGISTRY_TITLE_KEY),
                    ResourceUtils.getString(Registry.class,
                    ERROR_LOADING_BUNDLED_REGISTRY_MESSAGE_KEY, bundledRegistryUri)
                    )) {
                finishHandler.criticalExit();
            } else {
                LogManager.log(ErrorLevel.ERROR, e);
            }
        }
        
        for (String remoteRegistryURI: remoteRegistryUris) {
            childProgress = new Progress();
            compositeProgress.addChild(childProgress, percentageChunk);
            compositeProgress.setTitle(ResourceUtils.getString(Registry.class,
                    LOADING_REMOTE_REGISTRY_KEY,
                    remoteRegistryURI));
            
            try {
                loadProductRegistry(
                        remoteRegistryURI,
                        childProgress,
                        RegistryType.REMOTE,
                        true);
            } catch (InitializationException e) {
                if (!UiUtils.showYesNoDialog(
                        ResourceUtils.getString(
                        Registry.class, ERROR_LOADING_REMOTE_REGISTRY_TITLE_KEY),
                        ResourceUtils.getString(Registry.class,
                        ERROR_LOADING_REMOTE_REGISTRY_MESSAGE_KEY, remoteRegistryURI)
                        )) {
                    finishHandler.criticalExit();
                } else {
                    LogManager.log(ErrorLevel.ERROR, e);
                }
            }
        }
        
        validateDependencies();
        
        if (System.getProperty(SOURCE_STATE_FILE_PATH_PROPERTY) != null) {
            loadStateFile(
                    new File(System.getProperty(SOURCE_STATE_FILE_PATH_PROPERTY)),
                    new Progress());
        }
        
        applyRegistryFilters();
        changeStatuses();
        
        LogManager.logExit("... product registry initialization complete");
    }
    
    public void finalizeRegistry(
            final Progress progress) throws FinalizationException {
        LogManager.logEntry("finalizing product registry");
        
        progress.setPercentage(Progress.START);
        
        // remove installation data for all the products, if it still exists (should
        // be removed right upon installation); we only remove the files if the
        // local uri is different from the remote one and is not contained in the
        // list of alternate uris -- as we could be useing a locally located remote
        // registry
        LogManager.log("... removing remaining installation data for all the products");
        for (Product product: getProducts()) {
            for (ExtendedUri uri: product.getDataUris()) {
                if ((uri.getLocal() != null) &&
                        !uri.getLocal().equals(uri.getRemote()) &&
                        !uri.getAlternates().contains(uri.getLocal())) {
                    try {
                        FileUtils.deleteFile(new File(uri.getLocal()));
                        uri.setLocal(null);
                    } catch (IOException e) {
                        ErrorManager.notifyWarning(
                                ResourceUtils.getString(Registry.class,
                                ERROR_CANNOT_DELETE_DATA_KEY),
                                e);
                    }
                }
            }
        }
        LogManager.log("... save local registry if necessary");
        // save the local registry if we're executing in normal mode (i.e. not
        // creating a bundle)
        if (ExecutionMode.getCurrentExecutionMode() == ExecutionMode.NORMAL) {
            progress.setTitle(ResourceUtils.getString(Registry.class,
                    SAVE_LOCAL_REGISTRY_TITLE_KEY));
            progress.setDetail(ResourceUtils.getString(Registry.class,
                    SAVE_LOCAL_REGISTRY_DETAIL_KEY,  localRegistryFile));
            LogManager.log("... save registry to file " + localRegistryFile);
            saveProductRegistry(
                    localRegistryFile,
                    new ProductFilter(Status.INSTALLED),
                    false,  // we don't need any includes in the local registry,
                    true,   // but the registry's properties would be nice,
                    false); // and the features list is not required either
        }
        
        // save the state file if it is required (i.e. --record command line option
        // was specified)
        LogManager.log("... save state file if necessary");
        if (System.getProperty(TARGET_STATE_FILE_PATH_PROPERTY) != null) {
            File stateFile =
                    new File(System.getProperty(TARGET_STATE_FILE_PATH_PROPERTY));
            LogManager.log("... save state file to " + stateFile);
            saveStateFile(stateFile, new Progress());
        }
        
        progress.setPercentage(Progress.COMPLETE);
        
        LogManager.logExit("finalizing product registry");
    }
    
    private void setRegistryProperties(
            ) throws InitializationException {
        LogManager.logEntry("initializing product registry properties");
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.logIndent("initializing local product cache directory");
        if (System.getProperty(LOCAL_PRODUCT_CACHE_DIRECTORY_PROPERTY) != null) {
            localProductCache = new File(
                    localDirectory,
                    System.getProperty(LOCAL_PRODUCT_CACHE_DIRECTORY_PROPERTY));
        }
        if (!localProductCache.exists()) {
            if (!localProductCache.mkdirs()) {
                throw new InitializationException("Cannot create local product cache directory: " + localDirectory);
            }
        } else if (localProductCache.isFile()) {
            throw new InitializationException("Local product cache directory exists and is a file: " + localDirectory);
        } else if (!localProductCache.canRead()) {
            throw new InitializationException("Cannot read local product cache directory - not enought permissions");
        } else if (!localProductCache.canWrite()) {
            throw new InitializationException("Cannot write to local product cache directory - not enought permissions");
        }
        LogManager.logUnindent("... " + localProductCache);
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.log("initializing local product registry file");
        if (System.getProperty(LOCAL_PRODUCT_REGISTRY_PROPERTY) != null) {
            localRegistryFile = new File(
                    localDirectory,
                    System.getProperty(LOCAL_PRODUCT_REGISTRY_PROPERTY));
        }
        
        if (!localRegistryFile.exists()) {
            try {
                FileUtils.copyFile(
                        FileProxy.getInstance().getFile(localRegistryStubUri),
                        localRegistryFile);
            } catch (DownloadException e) {
                throw new InitializationException("Cannot create local registry", e);
            } catch (IOException e) {
                throw new InitializationException("Cannot create local registry", e);
            }
        } else if (localRegistryFile.isDirectory()) {
            throw new InitializationException("Local registry is a directory!");
        } else if (!localRegistryFile.canRead()) {
            throw new InitializationException("Cannot read local registry - not enough permissions");
        } else if (!localRegistryFile.canWrite()) {
            throw new InitializationException("Cannot write to local registry - not enough permissions");
        }
        LogManager.log("    ... " + localRegistryFile);
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.log("    initializing local product registry stub uri");
        if (System.getProperty(LOCAL_PRODUCT_REGISTRY_STUB_PROPERTY) != null) {
            localRegistryStubUri =
                    System.getProperty(LOCAL_PRODUCT_REGISTRY_STUB_PROPERTY);
        }
        LogManager.log("    ... " + localRegistryStubUri);
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.log("    initializing bundled product registry uri");
        if (System.getProperty(BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY) != null) {
            bundledRegistryUri =
                    System.getProperty(BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY);
        }
        LogManager.log("    ... " + bundledRegistryUri);
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.log("    initializing product registry schema uri");
        if (System.getProperty(PRODUCT_REGISTRY_SCHEMA_URI_PROPERTY) != null) {
            registrySchemaUri = System.getProperty(PRODUCT_REGISTRY_SCHEMA_URI_PROPERTY);
        }
        LogManager.log("    ... " + registrySchemaUri);
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.log("    initializing remote product registries uris");
        if (System.getProperty(REMOTE_PRODUCT_REGISTRIES_PROPERTY) != null) {
            for (String remoteRegistryURI: System.getProperty(REMOTE_PRODUCT_REGISTRIES_PROPERTY).split("\n")) {
                remoteRegistryUris.add(remoteRegistryURI);
            }
        }
        for (String string: remoteRegistryUris) {
            LogManager.log("    ... " + string);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.log("    initializing state file schema uri");
        if (System.getProperty(STATE_FILE_SCHEMA_URI_PROPERTY) != null) {
            stateFileSchemaUri = System.getProperty(STATE_FILE_SCHEMA_URI_PROPERTY);
        }
        LogManager.log("    ... " + stateFileSchemaUri);
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.log("    initializing default state file uri");
        if (System.getProperty(STATE_FILE_STUB_PROPERTY) != null) {
            stateFileStubUri = System.getProperty(STATE_FILE_STUB_PROPERTY);
        }
        LogManager.log("    ... " + stateFileStubUri);
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.log("    initializing target platform");
        if (System.getProperty(TARGET_PLATFORM_PROPERTY) != null) {
            try {
                targetPlatform = StringUtils.parsePlatform(System.getProperty(TARGET_PLATFORM_PROPERTY));
            } catch (ParseException e) {
                throw new InitializationException("Cannot parse platform", e);
            }
        }
        LogManager.log("    ... " + targetPlatform);
        LogManager.logExit("initializing product registry properties");
    }
    
    private void validateDependencies(
            ) throws InitializationException {
        for (Product product: getProducts()) {
            validateRequirements(product);
            validateConflicts(product);
            validateInstallAfters(product);
        }
    }
    
    private void applyRegistryFilters() {
        // if a target component was specified, hide everything except:
        //   * the target itself
        //   * products, whose requirement(s) the target satisfies
        //   * ancestors of any of the above
        if ((System.getProperty(TARGET_COMPONENT_UID_PROPERTY) != null) &&
                (System.getProperty(TARGET_COMPONENT_VERSION_PROPERTY) != null)) {
            final String  uid = System.getProperty(TARGET_COMPONENT_UID_PROPERTY);
            final Version version = Version.getVersion(
                    System.getProperty(TARGET_COMPONENT_VERSION_PROPERTY));
            
            final Product target = getProduct(uid, version);
            
            if (target != null) {
                final List<Product> dependents = getInavoidableDependents(target);
                
                for (Product product: getProducts()) {
                    if (!target.equals(product) &&
                            !dependents.contains(product) &&
                            !product.isAncestor(target) &&
                            !product.isAncestor(dependents)) {
                        product.setVisible(false);
                    }
                }
            } else {
                if (!UiUtils.showYesNoDialog(
                        ResourceUtils.getString(Registry.class,
                        ERROR_MISSING_TARGET_COMPONENT_TITLE_KEY),
                        ResourceUtils.getString(Registry.class,
                        ERROR_MISSING_TARGET_COMPONENT_MSG_KEY,
                        uid, version ))) {
                    finishHandler.cancel();
                } else {
                    for (Product product: getProducts()) {
                        product.setVisible(false);
                    }
                }
            }
        }
        
        // hide products that do not support the current platform
        for (Product product: getProducts()) {
            boolean compatible = false;
            
            for (Platform productPlatform: product.getPlatforms()) {
                if (targetPlatform.isCompatibleWith(productPlatform)) {
                    compatible = true;
                    break;
                }
            }
            
            if (!compatible) {
                product.setVisible(false);
            }
        }
        
        // hide empty groups
        for (Group group: getGroups()) {
            if (group.isEmpty()) {
                group.setVisible(false);
            }
        }
    }
    
    private void changeStatuses() {
        if (Boolean.getBoolean(SUGGEST_INSTALL_PROPERTY) ||
                Boolean.getBoolean(FORCE_INSTALL_PROPERTY)) {
            for (Product product: getProducts(Status.NOT_INSTALLED)) {
                // we should not change the status of components that are not
                // visible (were filtered out either at build time or runtime), as
                // this may cause unexpected results - these components are not
                // expected to be dealt with
                if (product.isVisible()) {
                    product.setStatus(Status.TO_BE_INSTALLED);
                }
            }
        }
        
        if (Boolean.getBoolean(SUGGEST_UNINSTALL_PROPERTY) ||
                Boolean.getBoolean(FORCE_UNINSTALL_PROPERTY)) {
            for (Product product: getProducts(Status.INSTALLED)) {
                // we should not change the status of components that are not
                // visible (were filtered out either at build time or runtime), as
                // this may cause unexpected results - these components are not
                // expected to be dealt with
                if (product.isVisible()) {
                    product.setStatus(Status.TO_BE_UNINSTALLED);
                }
            }
        }
    }
    
    // validation ///////////////////////////////////////////////////////////////////
    private void validateRequirements(
            final Product product) throws InitializationException {
        validateRequirements(product, new LinkedList<Product>());
    }
    
    private void validateRequirements(
            final Product product,
            final List<Product> prohibitedList) throws InitializationException {
        for (Dependency requirement: product.getDependencies(Requirement.class)) {
            // get the list of products that satisfy the requirement
            final List<Product> requirees = queryProducts(new ProductFilter(
                    requirement.getUid(),
                    requirement.getVersionLower(),
                    requirement.getVersionUpper(),
                    targetPlatform));
            
            // if there are no products that satisfy the requirement, the registry
            // is inconsistent
            if (requirees.size() == 0) {
                String sourceId =
                        product.getUid() + "/" +
                        product.getVersion();
                String requirementId =
                        requirement.getUid() + "/" +
                        requirement.getVersionLower() + " - " +
                        requirement.getVersionUpper() +
                        (requirement.getVersionResolved() != null ?
                            " [" + requirement.getVersionResolved() + "]" : "");
                
                throw new InitializationException(
                        ResourceUtils.getString(Registry.class,
                        ERROR_REQUIREMENT_KEY, sourceId, requirementId));
            }
            
            // iterate over the list of satisfying products, and check whether they
            // define a dependency that is satisfied wither by the current product
            // or by any product in the prohibited list; if it is, we have a cyclic
            // dependency which is faulty - throw an exception
            for (Product requiree: requirees) {
                for (Dependency dependency: requiree.getDependencies()) {
                    if (product.satisfies(dependency)) {
                        throw new InitializationException(
                                ResourceUtils.getString(Registry.class,
                                ERROR_CYCLIC_DEPENDENCY_KEY,
                                product.getUid(), dependency.getUid()));
                        
                    }
                    
                    for (Product prohibited: prohibitedList) {
                        if (prohibited.satisfies(dependency)) {
                            throw new InitializationException(
                                    ResourceUtils.getString(Registry.class,
                                    ERROR_CYCLIC_DEPENDENCY_KEY,
                                    prohibited.getUid(), dependency.getUid()));
                        }
                    }
                }
                
                // if the requiree's dependencies are ok, we need to check whether,
                // the products that satisfy its requirements are ok as well
                final List<Product> newProhibitedList = new LinkedList<Product>();
                newProhibitedList.addAll(prohibitedList);
                newProhibitedList.add(product);
                
                validateRequirements(requiree, newProhibitedList);
            }
        }
    }
    
    private void validateConflicts(
            final Product product) throws InitializationException {
        for (Dependency requirement: product.getDependencies(Requirement.class)) {
            // get the list of products that satisfy the requirement
            final List<Product> requirees = queryProducts(new ProductFilter(
                    requirement.getUid(),
                    requirement.getVersionLower(),
                    requirement.getVersionUpper(),
                    targetPlatform));
            
            for (Dependency conflict: product.getDependencies(Conflict.class)) {
                // get the list of products that satisfy the conflict
                final List<Product> conflictees = queryProducts(new ProductFilter(
                        conflict.getUid(),
                        conflict.getVersionLower(),
                        conflict.getVersionUpper(),
                        targetPlatform));
                
                if (SystemUtils.intersects(requirees, conflictees)) {
                    throw new InitializationException(
                            "A requiree is also a conflictee.");
                }
            }
        }
    }
    
    private void validateInstallAfters(
            final Product product) throws InitializationException {
        validateInstallAfters(product, new LinkedList<Product>());
    }
    
    private void validateInstallAfters(
            final Product product,
            final List<Product> prohibitedList) throws InitializationException {
        for (Dependency installafter: product.getDependencies(InstallAfter.class)) {
            // get the list of products that satisfy the install-after dependency
            final List<Product> dependees = queryProducts(new ProductFilter(
                    installafter.getUid(),
                    targetPlatform));
            
            // iterate over the list of satisfying products, and check whether they
            // define a requirement or install-efter dependency that is satisfied
            // either by the current product or by any product in the prohibited
            // list; if it is, we have a cyclic dependency which is faulty - throw
            // an exception
            for (Product requiree: dependees) {
                for (Dependency dependency: requiree.getDependencies(
                        Requirement.class,
                        InstallAfter.class)) {
                    if (product.satisfies(dependency)) {
                        throw new InitializationException(
                                ResourceUtils.getString(Registry.class,
                                ERROR_CYCLIC_DEPENDENCY_KEY,
                                product.getUid(), dependency.getUid()));
                    }
                    
                    for (Product prohibited: prohibitedList) {
                        if (prohibited.satisfies(dependency)) {
                            throw new InitializationException(
                                    ResourceUtils.getString(Registry.class,
                                    ERROR_CYCLIC_DEPENDENCY_KEY,
                                    prohibited.getUid(), dependency.getUid()));
                        }
                    }
                }
                
                // if the requiree's dependencies are ok, we need to check whether
                // the products that satisfy its requirements are ok as well
                final List<Product> newProhibitedList = new LinkedList<Product>();
                newProhibitedList.addAll(prohibitedList);
                newProhibitedList.add(product);
                
                validateRequirements(requiree, newProhibitedList);
            }
            
        }
    }
    
    /**
     * Returns the list of products for which the given product is the only one,
     * that satisfies the requirement. In other words the returned products define
     * at least one requirement that is directly or indirectly satisfied by this
     * particular product and not by any other products.
     *
     * <p>
     * Product's status is also taken into account, i.e. if the dependent product
     * is installed, a not installed product cannot be considered as satisfying the
     * requirement.
     *
     * @param product Product for which the dependents chain should be constructed.
     * @return The list of products for which the given product is the only one
     *      satisfying their requirements.
     */
    private List<Product> getInavoidableDependents(final Product product) {
        final Set<Product> dependents = new HashSet<Product>();
        
        for (Product candidate: getProducts()) {
            for (Dependency requirement: candidate.getDependencies(Requirement.class)) {
                final List<Product> requirees = getProducts(requirement);
                
                // if the candidate product is installed, then not installed
                // products cannot be counted as satisfying the requirement
                if (candidate.getStatus() == Status.INSTALLED) {
                    for (int i = 0; i < requirees.size(); i++) {
                        if (requirees.get(i).getStatus() != Status.INSTALLED) {
                            requirees.remove(i);
                        }
                    }
                }
                
                // if the requirees size is 0 then we're in trouble, but this method
                // should not be concerned about this stuff -- it's the
                // reponsibility of the requirement validating methods
                
                // if the list of requirees contains only one element and this
                // element equals to the given product -- the candidate should be
                // included in the list of inavoidable dependents; additionally we
                // need to checks for indirect requirements, i.e. run this method
                // recursively on the dependent
                if ((requirees.size() == 1) && requirees.get(0).equals(product)) {
                    dependents.add(candidate);
                    dependents.addAll(getInavoidableDependents(candidate));
                }
            }
        }
        
        return new ArrayList<Product>(dependents);
    }
    
    private void validateInstallations(
            ) throws InitializationException {
        for (Product product: getProducts()) {
            if (product.getStatus() == Status.INSTALLED) {
                final String message = product.getLogic().validateInstallation();
                
                if (message != null) {
                    final List<Product> inavoidableDependents =
                            getInavoidableDependents(product);
                    
                    boolean result = UiUtils.showYesNoDialog(
                            ResourceUtils.getString(Registry.class,
                            ERROR_VALIDATION_TITLE_KEY),
                            ResourceUtils.getString(Registry.class,
                            ERROR_VALIDATION_MSG_KEY,
                            product.getDisplayName(),
                            message ,
                            product.getDisplayName(),
                            StringUtils.asString(inavoidableDependents)));
                    
                    if (result) {
                        product.getParent().removeChild(product);
                        for (Product dependent: inavoidableDependents) {
                            dependent.getParent().removeChild(dependent);
                        }
                    } else {
                        finishHandler.criticalExit();
                    }
                }
            }
        }
    }
    
    // registry <-> dom <-> xml operations //////////////////////////////////////////
    public void loadProductRegistry(
            final File file) throws InitializationException {
        loadProductRegistry(file.toURI().toString());
    }
    
    public void loadProductRegistry(
            final String uri) throws InitializationException {
        loadProductRegistry(uri, new Progress(), RegistryType.REMOTE, false);
    }
    
    public void loadProductRegistry(
            final String uri,
            final Progress progress,
            final RegistryType registryType,
            final boolean loadIncludes) throws InitializationException {
        try {
            final Element registryElement =
                    loadRegistryDocument(uri).getDocumentElement();
            
            // load the includes
            final Element includesElement =
                    XMLUtils.getChild(registryElement, "includes");
            
            if (includesElement != null) {
                includes.addAll(
                        XMLUtils.parseExtendedUrisList(includesElement));
                
                if (loadIncludes) {
                    for (ExtendedUri includeUri: includes) {
                        loadProductRegistry(
                                includeUri.getRemote().toString(),
                                new Progress(),
                                RegistryType.REMOTE,
                                true);
                    }
                }
            }
            
            // load the properties
            final Element propertiesElement =
                    XMLUtils.getChild(registryElement, "properties");
            
            if (propertiesElement != null) {
                final NbiProperties map =
                        XMLUtils.parseNbiProperties(propertiesElement);
                for (Object name: map.keySet()) {
                    if (!properties.containsKey(name)) {
                        properties.put(name, map.get(name));
                    }
                }
            }
            
            // load the features list
            final Element featuresElement =
                    XMLUtils.getChild(registryElement, "features");
            
            if (featuresElement != null) {
                for (Feature feature: XMLUtils.parseFeaturesList(featuresElement)) {
                    boolean shouldAdd = true;
                    
                    int i;
                    for (i = 0; i < features.size(); i++) {
                        if (features.get(i).getId().equals(feature.getId())) {
                            shouldAdd = false;
                            break;
                        }
                        if (features.get(i).getOffset() > feature.getOffset()) {
                            break;
                        }
                    }
                    
                    if (shouldAdd) {
                        features.add(i, feature);
                    }
                }
            }
            
            // load the components
            loadRegistryComponents(registryRoot, registryElement, registryType);
            
            validateInstallations();
            
            progress.setPercentage(Progress.COMPLETE);
        } catch (ParseException e) {
            throw new InitializationException("Cannot load registry", e);
        } catch (XMLException e) {
            throw new InitializationException("Cannot load registry", e);
        }
    }
    
    public void saveProductRegistry(
            final File file,
            final RegistryFilter filter,
            final boolean saveIncludes,
            final boolean saveProperties,
            final boolean saveFeatures) throws FinalizationException {
        try {
            LogManager.logEntry("saving product registry file");
            LogManager.log("... getting registry document");
            Document document = getRegistryDocument(
                    filter, saveIncludes, saveProperties, saveFeatures);
            LogManager.log("... saving registry document to file " + file);
            XMLUtils.saveXMLDocument(document, file);
            LogManager.log("... saving XML file succesfully finished");
        } catch (XMLException e) {
            throw new FinalizationException(ResourceUtils.getString(
                    Registry.class, ERROR_REGISTRY_FINALIZATION), e);
        } finally {
            LogManager.logExit("... saving product registry done");
        }
    }
    
    public Document getEmptyRegistryDocument() throws XMLException {
        return loadRegistryDocument(localRegistryStubUri);
    }
    
    public Document getRegistryDocument(
            final RegistryFilter filter,
            final boolean saveIncludes,
            final boolean saveProperties,
            final boolean saveFeatures) throws XMLException, FinalizationException {
        final Document document = getEmptyRegistryDocument();
        final Element documentElement = document.getDocumentElement();
        
        if ((includes.size() > 0) && saveIncludes) {
            documentElement.appendChild(XMLUtils.saveExtendedUrisList(
                    includes, document.createElement("includes")));
        }
        
        if ((properties.size() > 0) && saveProperties) {
            documentElement.appendChild(XMLUtils.saveNbiProperties(
                    properties, document.createElement("properties")));
        }
        
        if ((features.size() > 0) && saveFeatures) {
            documentElement.appendChild(XMLUtils.saveFeaturesList(
                    features, document.createElement("features")));
        }
        
        final Element componentsElement =
                registryRoot.saveChildrenToDom(document, filter);
        if (componentsElement != null) {
            documentElement.appendChild(componentsElement);
        }
        
        return document;
    }
    
    public Document loadRegistryDocument(
            final String uri) throws XMLException {
        try {
            final File schemaFile =
                    FileProxy.getInstance().getFile(registrySchemaUri);
            final File registryFile =
                    FileProxy.getInstance().getFile(uri, true);
            
            final Schema schema = SchemaFactory.newInstance(
                    XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaFile);
            
            final DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            try {
                factory.setSchema(schema);
            } catch (UnsupportedOperationException e) {
                // if the parser does not support schemas, let it be -- we can do
                // without it anyway -- just log it and proceed
                ErrorManager.notifyDebug(
                        "The current parser - " + factory.getClass() + " - does not support schemas.",
                        e);
            }
            factory.setNamespaceAware(true);
            
            final DocumentBuilder builder = factory.newDocumentBuilder();
            
            return builder.parse(registryFile);
        } catch (DownloadException e) {
            throw new XMLException(ResourceUtils.getString(Registry.class, 
                    ERROR_REGISTRY_DOCUMENT_LOADING), e);
        } catch (ParserConfigurationException e) {
            throw new XMLException(ResourceUtils.getString(Registry.class, 
                    ERROR_REGISTRY_DOCUMENT_LOADING), e);
        } catch (SAXException e) {
            throw new XMLException(ResourceUtils.getString(Registry.class, 
                    ERROR_REGISTRY_DOCUMENT_LOADING), e);
        } catch (IOException e) {
            throw new XMLException(ResourceUtils.getString(Registry.class, 
                    ERROR_REGISTRY_DOCUMENT_LOADING), e);
        }
    }
    
    private void loadRegistryComponents(
            final RegistryNode parentNode,
            final Element parentElement,
            final RegistryType registryType) throws InitializationException {
        final Element element = XMLUtils.getChild(parentElement, "components");
        
        if (element != null) {
            for (Element child: XMLUtils.getChildren(element)) {
                if (child.getNodeName().equals("product")) {
                    final Product product = new Product().loadFromDom(child);
                    
                    // find the existing products which have the same uid/version
                    // and whose platforms itersect with the platforms of the
                    // currently loaded component (i.e. at least one of the platforms
                    // in the existing product is compatible a platform in the
                    // currept product's set and vice versa)
                    final List<Product> existing = getProducts(
                            product.getUid(),
                            product.getVersion(),
                            product.getPlatforms());
                    
                    product.setRegistryType(registryType);
                    
                    if (existing.size() == 0) {
                        parentNode.addChild(product);
                        loadRegistryComponents(product, child, registryType);
                    } else {
                        loadRegistryComponents(existing.get(0), child, registryType);
                    }
                }
                
                if (child.getNodeName().equals("group")) {
                    final Group group = new Group().loadFromDom(child);
                    final Group existing = getGroup(group.getUid());
                    
                    group.setRegistryType(registryType);
                    
                    if (existing == null) {
                        parentNode.addChild(group);
                        loadRegistryComponents(group, child, registryType);
                    } else {
                        loadRegistryComponents(existing, child, registryType);
                    }
                }
            }
        }
    }
    
    // basic queries ////////////////////////////////////////////////////////////////
    public List<RegistryNode> query(final RegistryFilter filter) {
        List<RegistryNode> matches = new LinkedList<RegistryNode>();
        Queue<RegistryNode> queue = new LinkedList<RegistryNode>();
        
        queue.offer(registryRoot);
        while (queue.peek() != null) {
            final RegistryNode node = queue.poll();
            
            if (filter.accept(node)) {
                matches.add(node);
            }
            
            for (RegistryNode child: node.getChildren()) {
                queue.offer(child);
            }
        }
        
        return matches;
    }
    
    public List<Product> queryProducts(final RegistryFilter filter) {
        List<Product> components = new ArrayList<Product>();
        
        for (RegistryNode node: query(filter)) {
            if (node instanceof Product) {
                components.add((Product) node);
            }
        }
        
        return components;
    }
    
    public List<Group> queryGroups(final RegistryFilter filter) {
        List<Group> groups = new ArrayList<Group>();
        
        for (RegistryNode node: query(filter)) {
            if (node instanceof Group) {
                groups.add((Group) node);
            }
        }
        
        return groups;
    }
    
    public List<RegistryNode> getNodes() {
        return query(TrueFilter.INSTANCE);
    }
    
    public List<RegistryNode> getNodes(final RegistryType registryType) {
        final List<RegistryNode> filtered = new LinkedList<RegistryNode>();
        
        for (RegistryNode node: getNodes()) {
            if (node.getRegistryType() == registryType) {
                filtered.add(node);
            }
        }
        
        return filtered;
    }
    
    // products queries /////////////////////////////////////////////////////////////
    public List<Product> getProducts() {
        return queryProducts(TrueFilter.INSTANCE);
    }
    
    public List<Product> getProducts(final Platform platform) {
        return queryProducts(new ProductFilter(platform));
    }
    
    public List<Product> getProducts(final String uid) {
        return queryProducts(new ProductFilter(uid, targetPlatform));
    }
    
    public List<Product> getProducts(final String uid, final Version lower, final Version upper) {
        return queryProducts(new ProductFilter(uid, lower, upper, targetPlatform));
    }
    
    public List<Product> getProducts(final String uid, final Version version, final Platform platform) {
        return queryProducts(new ProductFilter(uid, version, platform));
    }
    
    public List<Product> getProducts(final String uid, final Version version, final List<Platform> platforms) {
        return queryProducts(new ProductFilter(uid, version, platforms));
    }
    
    public List<Product> getProducts(final Dependency dependency) {
        if(dependency instanceof Requirement) {
            if (dependency.getVersionResolved() != null) {
                return queryProducts(new ProductFilter(
                        dependency.getUid(),
                        dependency.getVersionResolved(),
                        dependency.getVersionResolved(),
                        targetPlatform));
            }
        }
        if(dependency instanceof Requirement || dependency instanceof Conflict) {
            return queryProducts(new ProductFilter(
                    dependency.getUid(),
                    dependency.getVersionLower(),
                    dependency.getVersionUpper(),
                    targetPlatform));
        }
        if(dependency instanceof InstallAfter) {
            return queryProducts(new ProductFilter(
                    dependency.getUid(),
                    targetPlatform));
        }
        ErrorManager.notifyCritical("unknown dependency type");
        
        // the only way for us to reach this spot is to get to 'default:' in the
        // switch, but ErrorManager.notifyCritical() will cause a System.exit(),
        // so the below line is present only for successful compilation
        return null;
    }
    
    public List<Product> getProducts(final Status status) {
        return queryProducts(new ProductFilter(status, targetPlatform));
    }
    
    public List<Product> getProducts(final DetailedStatus detailedStatus) {
        return queryProducts(new ProductFilter(detailedStatus, targetPlatform));
    }
    
    public List<Product> getProducts(final Feature feature) {
        return queryProducts(new ProductFilter(feature, targetPlatform));
    }
    
    public Product getProduct(final String uid, final Version version) {
        final List<Product> candidates = getProducts(
                uid,
                version,
                targetPlatform);
        
        return (candidates.size() > 0) ? candidates.get(0) : null;
    }
    
    // groups queries ///////////////////////////////////////////////////////////////
    public List<Group> getGroups() {
        return queryGroups(TrueFilter.INSTANCE);
    }
    
    public Group getGroup(final String uid) {
        List<Group> candidates = queryGroups(new GroupFilter(uid));
        
        return (candidates.size() > 0) ? candidates.get(0) : null;
    }
    
    // installation order related queries ///////////////////////////////////////////
    public List<Product> getProductsToInstall() {
        final List<Product> products = new LinkedList<Product>();
        
        Product product;
        while ((product = getNextComponentToInstall(products)) != null) {
            products.add(product);
        }
        
        return products;
    }
    
    public List<Product> getProductsToUninstall() {
        final List<Product> products = new ArrayList<Product>();
        
        Product product;
        while ((product = getNextComponentToUninstall(products)) != null) {
            products.add(product);
        }
        
        return products;
    }
    
    private Product getNextComponentToInstall(final List<Product> currentList) {
        for (Product product: getProducts()) {
            if ((product.getStatus() == Status.TO_BE_INSTALLED) &&
                    !currentList.contains(product) &&
                    checkDependenciesForInstall(product)) {
                boolean productIsGood = true;
                
                // all products satisfying the requirement and install-after
                // dependencies which are planned for installation should be already
                // present in the list
                for (Dependency dependency: product.getDependencies(
                        Requirement.class,
                        InstallAfter.class)) {
                    for (Product dependee: getProducts(dependency)) {
                        if ((dependee.getStatus() == Status.TO_BE_INSTALLED) &&
                                !currentList.contains(dependee)) {
                            productIsGood = false;
                        }
                    }
                }
                
                if (productIsGood) {
                    return product;
                }
            }
        }
        
        return null;
    }
    
    private Product getNextComponentToUninstall(final List<Product> currentList) {
        for (Product product: getProducts()) {
            if ((product.getStatus() == Status.TO_BE_UNINSTALLED) &&
                    !currentList.contains(product) &&
                    checkDependenciesForUninstall()) {
                boolean productIsGood = true;
                
                for (Product dependent: getProducts()) {
                    if ((dependent.getStatus() != Status.NOT_INSTALLED) &&
                            !currentList.contains(dependent) &&
                            satisfiesRequirement(product, dependent)) {
                        productIsGood = false;
                        break;
                    }
                }
                
                if (productIsGood) {
                    return product;
                }
                
            }
        }
        return null;
    }
    
    // products /////////////////////////////////////////////////////////////////////
    public boolean satisfiesRequirement(final Product candidate, final Product product) {
        for (Dependency requirement: product.getDependencies(Requirement.class)) {
            final List<Product> requirees = getProducts(requirement);
            
            for (Product requiree: requirees) {
                if (candidate.equals(requiree) ||
                        satisfiesRequirement(candidate, requiree)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean checkDependenciesForInstall(final Product product) {
        for (Dependency requirement: product.getDependencies(Requirement.class)) {
            final List<Product> requirees = getProducts(requirement);
            boolean satisfied = false;
            
            for (Product requiree: requirees) {
                if ((requiree.getStatus() == Status.INSTALLED) ||
                        (requiree.getStatus() == Status.TO_BE_INSTALLED)) {
                    satisfied = true;
                    break;
                }
            }
            
            if (!satisfied) return false;
        }
        
        for (Dependency conflict: product.getDependencies(Conflict.class)) {
            final List<Product> conflictees = getProducts(conflict);
            boolean satisfied = true;
            
            for (Product conflictee: conflictees) {
                if ((conflictee.getStatus() == Status.INSTALLED) ||
                        (conflictee.getStatus() == Status.TO_BE_INSTALLED)) {
                    satisfied = false;
                    break;
                }
            }
            
            if (!satisfied) return false;
        }
        
        return true;
    }
    
    public boolean checkDependenciesForUninstall() {
        for (Product product: getProducts()) {
            if ((product.getStatus() == Status.INSTALLED) ||
                    (product.getStatus() == Status.TO_BE_INSTALLED)) {
                for (Dependency requirement: product.getDependencies(Requirement.class)) {
                    final List<Product> requirees = getProducts(requirement);
                    
                    for (Product requiree: requirees) {
                        if (requiree.getStatus() == Status.INSTALLED) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    // properties ///////////////////////////////////////////////////////////////////
    public Properties getProperties() {
        return properties;
    }
    
    public String getProperty(final String name) {
        return properties.getProperty(name);
    }
    
    public void setProperty(final String name, final String value) {
        properties.setProperty(name, value);
    }
    
    // state file methods ///////////////////////////////////////////////////////////
    public void loadStateFile(final File stateFile, final Progress progress) throws InitializationException {
        try {
            LogManager.log("loading state file from " + stateFile.getAbsolutePath());
            
            LogManager.log("parsing xml file...");
            final File schemaFile =
                    FileProxy.getInstance().getFile(stateFileSchemaUri);
            
            final Schema schema = SchemaFactory.
                    newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).
                    newSchema(schemaFile);
            
            final DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            try {
                factory.setSchema(schema);
            } catch (UnsupportedOperationException e) {
                // if the parser does not support schemas, let it be -- we can do
                // without it anyway -- just log it and proceed
                ErrorManager.notifyDebug(
                        "The current parser - " + factory.getClass() + " - does not support schemas.",
                        e);
            }
            factory.setNamespaceAware(true);
            
            final Document document = factory.
                    newDocumentBuilder().
                    parse(stateFile);
            LogManager.log("...complete");
            
            final Element element = document.getDocumentElement();
            
            // get the total number of components in this state file, we need this to
            // be able to properly update the progress
            int productsNumber = XMLUtils.countDescendants(element, "product");
            
            // we should get the percentage per component and we reserce one area for
            // registry-wide properties
            int percentageChunk = Progress.COMPLETE / (productsNumber + 1);
            int percentageLeak = Progress.COMPLETE % (productsNumber + 1);
            
            LogManager.log("    parsing registry properties...");
            
            final Element propertiesElement =
                    XMLUtils.getChild(element, "properties");
            if (propertiesElement != null) {
                progress.setDetail(ResourceUtils.getString(Registry.class,
                        LOADING_REGISTRY_PROPERTIES_KEY));
                properties.putAll(XMLUtils.parseNbiProperties(propertiesElement));
            }
            
            LogManager.log("    ...complete");
            progress.addPercentage(percentageChunk + percentageLeak);
            
            LogManager.log(ErrorLevel.DEBUG, "    parsing components...");
            
            final Element productsElement =
                    XMLUtils.getChild(element, "components");
            if (productsElement != null) {
                for (Element productElement: XMLUtils.getChildren(productsElement)) {
                    final String uid =
                            productElement.getAttribute("uid");
                    final Version version =
                            Version.getVersion(productElement.getAttribute("version"));
                    final List<Platform> platforms =
                            StringUtils.parsePlatforms(productElement.getAttribute("platform"));
                    
                    LogManager.log("        parsing component uid=" + uid + ", version=" + version);
                    progress.setDetail(ResourceUtils.getString(Registry.class,
                            LOADING_COMPONENT_KEY, uid, version));
                    if (platforms.contains(targetPlatform)) {
                        final Product product = getProduct(uid, version);
                        
                        if (product != null) {
                            final Status status = StringUtils.parseStatus(productElement.getAttribute("status"));
                            switch (status) {
                                case NOT_INSTALLED:
                                    continue;
                                case TO_BE_INSTALLED:
                                    if (product.getStatus() != Status.INSTALLED) {
                                        product.setStatus(status);
                                    } else {
                                        continue;
                                    }
                                    break;
                                case INSTALLED:
                                    continue;
                                case TO_BE_UNINSTALLED:
                                    if (product.getStatus() != Status.NOT_INSTALLED) {
                                        product.setStatus(status);
                                    } else {
                                        continue;
                                    }
                                    break;
                            }
                            
                            final Element productPropertiesElement = XMLUtils.getChild(productElement, "properties");
                            if (productPropertiesElement != null) {
                                product.getProperties().putAll(
                                        XMLUtils.parseNbiProperties(productPropertiesElement));
                            }
                        }
                    }
                }
            }
            LogManager.log(ErrorLevel.DEBUG, "    ...complete");
        } catch (DownloadException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Registry.class, ERROR_LOADING_COMPONENTS), e);
        } catch (ParserConfigurationException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Registry.class, ERROR_LOADING_COMPONENTS), e);
        } catch (SAXException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Registry.class, ERROR_LOADING_COMPONENTS), e);
        } catch (IOException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Registry.class, ERROR_LOADING_COMPONENTS), e);
        } catch (ParseException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Registry.class, ERROR_LOADING_COMPONENTS), e);
        }
    }
    
    public void saveStateFile(final File stateFile, final Progress progress) throws FinalizationException {
        try {
            final File schemaFile =
                    FileProxy.getInstance().getFile(stateFileSchemaUri);
            final File stubFile =
                    FileProxy.getInstance().getFile(stateFileStubUri);
            
            final Schema schema = SchemaFactory.
                    newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).
                    newSchema(schemaFile);
            
            final DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            try {
                factory.setSchema(schema);
            } catch (UnsupportedOperationException e) {
                // if the parser does not support schemas, let it be -- we can do
                // without it anyway -- just log it and proceed
                ErrorManager.notifyDebug(
                        "The current parser - " + factory.getClass() + " - does not support schemas.",
                        e);
            }
            factory.setNamespaceAware(true);
            
            final Document document = factory.
                    newDocumentBuilder().
                    parse(stubFile);
            
            final Element documentElement = document.getDocumentElement();
            
            if (properties.size() > 0) {
                Element propertiesNode = document.createElement("properties");
                
                for (Object key: properties.keySet()) {
                    String name = (String) key;
                    
                    Element propertyNode = document.createElement("property");
                    
                    propertyNode.setAttribute("name", name);
                    propertyNode.setTextContent(properties.getProperty(name));
                    
                    propertiesNode.appendChild(propertyNode);
                }
                
                documentElement.appendChild(propertiesNode);
            }
            
            List<Product> products = queryProducts(new OrFilter(
                    new ProductFilter(Status.INSTALLED, targetPlatform),
                    new ProductFilter(Status.NOT_INSTALLED, targetPlatform)));
            if (products.size() > 0) {
                final Element productsNode = document.createElement("components");
                
                for (Product component: products) {
                    final Element productNode = document.createElement("product");
                    
                    productNode.setAttribute(
                            "uid",
                            component.getUid());
                    productNode.setAttribute(
                            "version",
                            component.getVersion().toString());
                    productNode.setAttribute(
                            "platform",
                            StringUtils.asString(component.getPlatforms(), " "));
                    
                    switch (component.getStatus()) {
                        case INSTALLED:
                            productNode.setAttribute(
                                    "status",
                                    Status.TO_BE_INSTALLED.toString());
                            break;
                        case NOT_INSTALLED:
                            productNode.setAttribute(
                                    "status",
                                    Status.TO_BE_UNINSTALLED.toString());
                            break;
                        default:
                            continue;
                    }
                    
                    if (component.getProperties().size() > 0) {
                        Element propertiesNode = document.createElement("properties");
                        
                        for (Object key: component.getProperties().keySet()) {
                            String name = (String) key;
                            
                            Element propertyNode = document.createElement("property");
                            
                            propertyNode.setAttribute("name", name);
                            propertyNode.setTextContent(component.getProperties().getProperty(name));
                            
                            propertiesNode.appendChild(propertyNode);
                        }
                        productNode.appendChild(propertiesNode);
                    }
                    
                    productsNode.appendChild(productNode);
                }
                
                documentElement.appendChild(productsNode);
            }
            FileUtils.mkdirs(stateFile.getParentFile());
            XMLUtils.saveXMLDocument(document, stateFile);
        } catch (DownloadException e) {
            throw new FinalizationException(ResourceUtils.getString(
                    Registry.class, ERROR_REGISTRY_FINALIZATION), e);
        } catch (ParserConfigurationException e) {
            throw new FinalizationException(ResourceUtils.getString(
                    Registry.class, ERROR_REGISTRY_FINALIZATION), e);
        } catch (SAXException e) {
            throw new FinalizationException(ResourceUtils.getString(
                    Registry.class, ERROR_REGISTRY_FINALIZATION), e);
        } catch (IOException e) {
            throw new FinalizationException(ResourceUtils.getString(
                    Registry.class, ERROR_REGISTRY_FINALIZATION), e);
        } catch (XMLException e) {
            throw new FinalizationException(ResourceUtils.getString(
                    Registry.class, ERROR_REGISTRY_FINALIZATION), e);
        }
    }
    
    // miscellanea //////////////////////////////////////////////////////////////////
    public File getLocalProductCache() {
        return localProductCache;
    }
    
    public RegistryNode getRegistryRoot() {
        return registryRoot;
    }
    
    public boolean hasInstalledChildren(final RegistryNode parentNode) {
        for (RegistryNode child: parentNode.getChildren()) {
            if (child instanceof Product) {
                Product component = (Product) child;
                if (component.getStatus() == Status.INSTALLED) {
                    return true;
                }
            }
            
            if (hasInstalledChildren(child)) {
                return true;
            }
        }
        
        return false;
    }
    
    public Platform getTargetPlatform() {
        return targetPlatform;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_LOCAL_PRODUCT_CACHE_DIRECTORY_NAME =
            "product-cache";
    
    public static final String LOCAL_PRODUCT_CACHE_DIRECTORY_PROPERTY =
            "nbi.product.local.cache.directory.name";
    
    public static final String DEFAULT_LOCAL_REGISTRY_FILE_NAME =
            "registry.xml";
    
    public static final String LOCAL_PRODUCT_REGISTRY_PROPERTY =
            "nbi.product.local.registry.file.name";
    
    public static final String DEFAULT_LOCAL_PRODUCT_REGISTRY_STUB_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX +
            "org/netbeans/installer/product/default-registry.xml";
    
    public static final String LOCAL_PRODUCT_REGISTRY_STUB_PROPERTY =
            "nbi.product.local.registry.stub";
    
    public static final String DEFAULT_BUNDLED_PRODUCT_REGISTRY_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX +
            "data/registry.xml";
    
    public static final String BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY =
            "nbi.product.bundled.registry.uri";
    
    public static final String DEFAULT_PRODUCT_REGISTRY_SCHEMA_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX +
            "org/netbeans/installer/product/registry.xsd";
    
    public static final String PRODUCT_REGISTRY_SCHEMA_URI_PROPERTY =
            "nbi.product.registry.schema.uri";
    
    public static final String REMOTE_PRODUCT_REGISTRIES_PROPERTY =
            "nbi.product.remote.registries";
    
    public static final String TARGET_COMPONENT_UID_PROPERTY =
            "nbi.product.target.component.uid";
    
    public static final String TARGET_COMPONENT_VERSION_PROPERTY =
            "nbi.product.target.component.version";
    
    public static final String SOURCE_STATE_FILE_PATH_PROPERTY =
            "nbi.product.source.state.file.path";
    
    public static final String TARGET_STATE_FILE_PATH_PROPERTY =
            "nbi.product.target.state.file.path";
    
    public static final String STATE_FILE_SCHEMA_URI_PROPERTY =
            "nbi.state.file.schema.uri";
    
    public static final String DEFAULT_STATE_FILE_SCHEMA_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX +
            "org/netbeans/installer/product/state-file.xsd";
    
    public static final String DEFAULT_STATE_FILE_STUB_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX +
            "org/netbeans/installer/product/default-state-file.xml";
    
    public static final String STATE_FILE_STUB_PROPERTY =
            "nbi.state.file.stub";
    
    public static final String TARGET_PLATFORM_PROPERTY =
            "nbi.target.platform";
    
    public static final String SUGGEST_INSTALL_PROPERTY =
            "nbi.product.suggest.install";
    
    public static final String SUGGEST_UNINSTALL_PROPERTY =
            "nbi.product.suggest.uninstall";
    
    public static final String FORCE_INSTALL_PROPERTY =
            "nbi.product.force.install";
    
    public static final String FORCE_UNINSTALL_PROPERTY =
            "nbi.product.force.uninstall";
    
    public static final String CREATE_BUNDLE_PATH_PROPERTY =
            "nbi.create.bundle.path";
    
    public static final String LAZY_LOAD_ICONS_PROPERTY =
            "nbi.product.lazy.load.icons";
    
    private static final String LOADING_LOCAL_REGISTRY_KEY =
            "R.loading.local.registry"; //NOI18N
    private static final String ERROR_LOADING_LOCAL_REGISTRY_TITLE_KEY =
            "R.error.loading.local.registry.failed.title";//NOI18N
    private static final String ERROR_LOADING_LOCAL_REGISTRY_MESSAGE_KEY =
            "R.error.loading.local.registry.failed.msg";//NOI18N
    
    private static final String LOADING_BUNDLED_REGISTRY_KEY =
            "R.loading.bundled.registry"; //NOI18N
    private static final String ERROR_LOADING_BUNDLED_REGISTRY_TITLE_KEY =
            "R.error.loading.bundled.registry.failed.title";//NOI18N
    private static final String ERROR_LOADING_BUNDLED_REGISTRY_MESSAGE_KEY =
            "R.error.loading.bundled.registry.failed.msg";//NOI18N
    
    private static final String LOADING_REMOTE_REGISTRY_KEY =
            "R.loading.remote.registry"; //NOI18N
    private static final String ERROR_LOADING_REMOTE_REGISTRY_TITLE_KEY =
            "R.error.loading.remote.registry.failed.title";//NOI18N
    private static final String ERROR_LOADING_REMOTE_REGISTRY_MESSAGE_KEY =
            "R.error.loading.remote.registry.failed.msg"; //NOI18N
    private static final String ERROR_CANNOT_DELETE_DATA_KEY =
            "R.error.cannot.delete.data"; //NOI18N
    private static final String SAVE_LOCAL_REGISTRY_TITLE_KEY =
            "R.save.local.registry.title";//NOI18N
    private static final String SAVE_LOCAL_REGISTRY_DETAIL_KEY =
            "R.save.local.registry.detail"; //NOI18N
    private static final String ERROR_MISSING_TARGET_COMPONENT_TITLE_KEY =
            "R.error.missing.target.component.title"; //NOI18N
    private static final String ERROR_MISSING_TARGET_COMPONENT_MSG_KEY =
            "R.error.missing.target.component.msg"; //NOI18N
    private static final String ERROR_VALIDATION_TITLE_KEY =
            "R.error.validation.title";//NOI18N
    private static final String ERROR_VALIDATION_MSG_KEY =
            "R.error.validation.msg";//NOI18N
    private static final String ERROR_REQUIREMENT_KEY =
            "R.error.matching.requirement";//NOI18N
    private static final String ERROR_CYCLIC_DEPENDENCY_KEY =
            "R.error.cyclic.dependency";//NOI18N
    private static final String LOADING_REGISTRY_PROPERTIES_KEY =
            "R.loading.registry.properties";//NOI18N
    private static final String LOADING_COMPONENT_KEY =
            "R.loading.component";//NOI18N
    private static final String ERROR_LOADING_COMPONENTS =
            "R.error.loading.components";//NOI18N
    private static final String ERROR_REGISTRY_FINALIZATION =
            "R.error.registry.finalization";//NOI18N
    private static final String ERROR_REGISTRY_DOCUMENT_LOADING =
            "R.error.registry.document.loading";//NOI18N
}
