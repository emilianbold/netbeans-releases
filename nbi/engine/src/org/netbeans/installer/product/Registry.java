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
 *
 * $Id$
 */
package org.netbeans.installer.product;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.installer.Installer;
import org.netbeans.installer.Installer.InstallerExecutionMode;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.filters.OrFilter;
import org.netbeans.installer.product.filters.ProductFilter;
import org.netbeans.installer.product.filters.GroupFilter;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.product.filters.TrueFilter;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.helper.DetailedStatus;
import org.netbeans.installer.utils.helper.ExtendedUri;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.DependencyType;
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
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
    private File         localProductCache;
    
    private String       localRegistryStubUri;
    private File         localRegistryFile;
    private String       bundledRegistryUri;
    private List<String> remoteRegistryUris;
    private String       registrySchemaUri;
    
    private String       stateFileSchemaUri;
    private String       stateFileStubUri;
    
    private RegistryNode registryRoot;
    private Properties   properties;
    private Platform     targetPlatform;
    
    // constructors /////////////////////////////////////////////////////////////////
    private Registry() {
        localProductCache = new File(
                Installer.getInstance().getLocalDirectory(),
                DEFAULT_LOCAL_PRODUCT_CACHE_DIRECTORY_NAME);
        
        localRegistryStubUri = DEFAULT_LOCAL_PRODUCT_REGISTRY_STUB_URI;
        localRegistryFile = new File(
                Installer.getInstance().getLocalDirectory(),
                DEFAULT_LOCAL_REGISTRY_FILE_NAME);
        bundledRegistryUri = DEFAULT_BUNDLED_PRODUCT_REGISTRY_URI;
        remoteRegistryUris = new ArrayList<String>();
        registrySchemaUri = DEFAULT_PRODUCT_REGISTRY_SCHEMA_URI;
        
        stateFileSchemaUri = DEFAULT_STATE_FILE_SCHEMA_URI;
        stateFileStubUri = DEFAULT_STATE_FILE_STUB_URI;
        
        registryRoot = new Group();
        properties = new Properties();
        targetPlatform = SystemUtils.getCurrentPlatform();
    }
    
    public Registry(final File file) throws InitializationException {
        this();
        
        setRegistryProperties();
        
        loadProductRegistry(file.toURI().toString(), new Progress());
    }
    
    public Registry(final List<File> files) throws InitializationException {
        this();
        
        setRegistryProperties();
        
        for (File file: files) {
            loadProductRegistry(file.toURI().toString(), new Progress());
        }
    }
    
    public Registry(final Platform platform, final List<File> files) throws InitializationException {
        this();
        
        setRegistryProperties();
        
        this.targetPlatform = platform;
        
        for (File file: files) {
            loadProductRegistry(file.toURI().toString(), new Progress());
        }
    }
    
    // initialization/finalization //////////////////////////////////////////////////
    public void initializeRegistry(final Progress progress) throws InitializationException {
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
        compositeProgress.setTitle("Loading local registry [" + localRegistryFile + "]");
        loadProductRegistry(localRegistryFile.toURI().toString(), childProgress);
        
        childProgress = new Progress();
        compositeProgress.addChild(childProgress, percentageChunk);
        compositeProgress.setTitle("Loading bundled registry [" + bundledRegistryUri + "]");
        loadProductRegistry(bundledRegistryUri, childProgress);
        
        // sleep a little to let the user perceive that something is happening
        SystemUtils.sleep(200);
        
        for (String remoteRegistryURI: remoteRegistryUris) {
            childProgress = new Progress();
            compositeProgress.addChild(childProgress, percentageChunk);
            compositeProgress.setTitle("Loading remote registry [" + remoteRegistryURI + "]");
            loadProductRegistry(remoteRegistryURI, childProgress);
            
            // sleep a little to let the user perceive that something is happening
            SystemUtils.sleep(200);
        }
        
        validateInstallations();
        validateDependencies();
        
        if (System.getProperty(SOURCE_STATE_FILE_PATH_PROPERTY) != null) {
            loadStateFile(
                    new File(System.getProperty(SOURCE_STATE_FILE_PATH_PROPERTY)),
                    new Progress());
        }
        
        applyRegistryFilters();
        
        LogManager.logExit("... product registry initialization complete");
    }
    
    public void finalizeRegistry(final Progress progress) throws FinalizationException {
        LogManager.logEntry("finalizing product registry");
        
        progress.setPercentage(Progress.START);
        
        // remove installation data for all the products, if it still exists (should
        // be removed right upon installation)
        for (Product product: getProducts()) {
            for (ExtendedUri uri: product.getDataUris()) {
                if (uri.getLocal() != null) {
                    try {
                        FileUtils.deleteFile(new File(uri.getLocal()));
                        uri.setLocal(null);
                    } catch (IOException e) {
                        ErrorManager.notifyWarning(
                                "Cannot delete the cached installation data",
                                e);
                    }
                }
            }
        }
        
        // save the local registry if we're executing in normal mode (i.e. not
        // creating a bundle)
        if (Installer.getInstance().getExecutionMode() ==
                InstallerExecutionMode.NORMAL) {
            progress.setTitle("Saving local registry");
            progress.setDetail("Saving to " + localRegistryFile);
            
            saveProductRegistry(
                    localRegistryFile,
                    new ProductFilter(Status.INSTALLED));
        }
        
        // save the state file if it is required (i.e. --record command line option
        // was specified)
        if (System.getProperty(TARGET_STATE_FILE_PATH_PROPERTY) != null) {
            File stateFile =
                    new File(System.getProperty(TARGET_STATE_FILE_PATH_PROPERTY));
            
            saveStateFile(stateFile, new Progress());
        }
        
        progress.setPercentage(Progress.COMPLETE);
        
        LogManager.logExit("finalizing product registry");
    }
    
    private void setRegistryProperties() throws InitializationException {
        LogManager.logEntry("initializing product registry properties");
        
        final File localDirectory = Installer.getInstance().getLocalDirectory();
        
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
    }
    
    private void applyRegistryFilters() {
        // if a target component was specified, hide everything except:
        //   * the target itself
        //   * products, whose requirement(s) the target satisfies
        //   * ancestors of any of the above
        if ((System.getProperty(TARGET_COMPONENT_UID_PROPERTY) != null) &&
                (System.getProperty(TARGET_COMPONENT_VERSION_PROPERTY) != null)) {
            final String uid = System.getProperty(TARGET_COMPONENT_UID_PROPERTY);
            final Version version = Version.getVersion(
                    System.getProperty(TARGET_COMPONENT_VERSION_PROPERTY));
            
            final Product target = getProduct(uid, version);
            
            List<Product> dependents = new ArrayList<Product>();
            for (Product product: getProducts()) {
                if (target.satisfiesRequirement(product)) {
                    dependents.add(product);
                }
            }
            
            for (Product product: getProducts()) {
                if (!target.equals(product) &&
                        !dependents.contains(product) &&
                        !product.isAncestor(target) &&
                        !product.isAncestor(dependents)) {
                    product.setVisible(false);
                }
            }
        }
        
        // hide products that do not support the current platform
        for (Product product: queryProducts(TrueFilter.INSTANCE)) {
            if (!product.getSupportedPlatforms().contains(targetPlatform)) {
                product.setVisible(false);
            }
        }
        
        // hide empty groups
        for (Group group: queryGroups(TrueFilter.INSTANCE)) {
            if (group.isEmpty()) {
                group.setVisible(false);
            }
        }
    }
    
    // validation ///////////////////////////////////////////////////////////////////
    public void validateDependencies() throws InitializationException {
        for (Product product: getProducts()) {
            validateRequirements(product);
            validateConflicts(product);
            validateInstallAfters(product);
        }
    }
    
    private void validateRequirements(Product product) throws InitializationException {
        validateRequirements(product, new LinkedList<Product>());
    }
    
    private void validateRequirements(Product product, List<Product> prohibitedList) throws InitializationException {
        for (Dependency requirement: product.getDependencies(DependencyType.REQUIREMENT)) {
            // get the list of products that satisfy the requirement
            final List<Product> requirees = queryProducts(new ProductFilter(
                    requirement.getUid(),
                    requirement.getVersionLower(),
                    requirement.getVersionUpper(),
                    targetPlatform));
            
            // if there are no products that satisfy the requirement, the registry
            // is inconsistent
            if (requirees.size() == 0) {
                throw new InitializationException("No components " +
                        "matching the requirement.");
            }
            
            // iterate over the list of satisfying products, and check whether they
            // define a dependency that is satisfied wither by the current product
            // or by any product in the prohibited list; if it is, we have a cyclic
            // dependency which is faulty - throw an exception
            for (Product requiree: requirees) {
                for (Dependency dependency: requiree.getDependencies()) {
                    if (product.satisfies(dependency)) {
                        throw new InitializationException(
                                "Cyclic dependency: " + product.getUid() +
                                ", " + dependency.getUid());
                    }
                    
                    for (Product prohibited: prohibitedList) {
                        if (prohibited.satisfies(dependency)) {
                            throw new InitializationException(
                                    "Cyclic dependency: " + prohibited.getUid() +
                                    ", " + dependency.getUid());
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
    
    private void validateConflicts(Product product) throws InitializationException {
        for (Dependency requirement: product.getDependencies(DependencyType.REQUIREMENT)) {
            // get the list of products that satisfy the requirement
            final List<Product> requirees = queryProducts(new ProductFilter(
                    requirement.getUid(),
                    requirement.getVersionLower(),
                    requirement.getVersionUpper(),
                    targetPlatform));
            
            for (Dependency conflict: product.getDependencies(DependencyType.CONFLICT)) {
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
    
    private void validateInstallAfters(Product product) throws InitializationException {
        validateInstallAfters(product, new LinkedList<Product>());
    }
    
    private void validateInstallAfters(Product product, List<Product> prohibitedList) throws InitializationException {
        for (Dependency installafter: product.getDependencies(DependencyType.INSTALL_AFTER)) {
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
                        DependencyType.REQUIREMENT,
                        DependencyType.INSTALL_AFTER)) {
                    if (product.satisfies(dependency)) {
                        throw new InitializationException(
                                "Cyclic dependency: " + product.getUid() +
                                ", " + dependency.getUid());
                    }
                    
                    for (Product prohibited: prohibitedList) {
                        if (prohibited.satisfies(dependency)) {
                            throw new InitializationException(
                                    "Cyclic dependency: " + prohibited.getUid() +
                                    ", " + dependency.getUid());
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
    
    private void validateInstallations() throws InitializationException {
        for (Product product: getProducts()) {
            if (product.getStatus() == Status.INSTALLED) {
                final String message = product.getLogic().validateInstallation();
                
                if (message != null) {
                    boolean result = UiUtils.showYesNoDialog(
                            "Validation Problem",
                            "It seems that the installation of " +
                            product.getDisplayName() + "is corrupted. The " +
                            "validation procedure issued the following " +
                            "warning:\n\n" + message + "\n\n Would you like to " +
                            "mark this product as not installed and continue? If " +
                            "you click No the installer will exit.");
                    
                    if (result) {
                        product.setStatus(Status.NOT_INSTALLED);
                    } else {
                        Installer.getInstance().criticalExit();
                    }
                }
            }
        }
    }
    
    // registry <-> dom <-> xml operations //////////////////////////////////////////
    public void loadProductRegistry(String uri, Progress progress) throws InitializationException {
        try {
            Document document = loadRegistryDocument(uri);
            
            loadRegistryComponents(registryRoot, document.getDocumentElement());
        } catch (XMLException e) {
            throw new InitializationException("Cannot load registry", e);
        }
    }
    
    public void saveProductRegistry(File file, RegistryFilter filter) throws FinalizationException {
        try {
            saveRegistryDocument(getRegistryDocument(filter), file);
        } catch (XMLException e) {
            throw new FinalizationException("Could not finalize registry", e);
        }
    }
    
    public Document getEmptyRegistryDocument() throws XMLException {
        return loadRegistryDocument(localRegistryStubUri);
    }
    
    public Document getRegistryDocument(final RegistryFilter filter) throws XMLException, FinalizationException {
        final Document document        = getEmptyRegistryDocument();
        final Element  documentElement = document.getDocumentElement();
        
        if (properties.size() > 0) {
            Element propertiesElement = document.createElement("properties");
            
            for (Object key: properties.keySet()) {
                String name = (String) key;
                
                Element propertyElement = document.createElement("property");
                
                propertyElement.setAttribute("name", name);
                propertyElement.setTextContent(properties.getProperty(name));
                
                propertiesElement.appendChild(propertyElement);
            }
            
            documentElement.appendChild(propertiesElement);
        }
        
        Element componentsElement =
                registryRoot.saveChildrenToDom(document, filter);
        
        if (componentsElement != null) {
            documentElement.appendChild(componentsElement);
        }
        
        return document;
    }
    
    public Document loadRegistryDocument(String uri) throws XMLException {
        try {
            File schemaFile   = FileProxy.getInstance().getFile(registrySchemaUri);
            File registryFile = FileProxy.getInstance().getFile(uri, true);
            
            Schema schema = SchemaFactory.
                    newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).
                    newSchema(schemaFile);
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setSchema(schema);
            factory.setNamespaceAware(true);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            return builder.parse(registryFile);
        } catch (DownloadException e) {
            throw new XMLException("Could not finalize registry", e);
        } catch (ParserConfigurationException e) {
            throw new XMLException("Could not finalize registry", e);
        } catch (SAXException e) {
            throw new XMLException("Could not finalize registry", e);
        } catch (IOException e) {
            throw new XMLException("Could not finalize registry", e);
        }
    }
    
    public void saveRegistryDocument(Document document, File file) throws XMLException {
        FileOutputStream output = null;
        
        try {
            saveRegistryDocument(document, output = new FileOutputStream(file));
        } catch (IOException e) {
            throw new XMLException("Could not finalize registry", e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    ErrorManager.notify(ErrorLevel.DEBUG, e);
                }
            }
        }
    }
    
    public void saveRegistryDocument(Document document, OutputStream out) throws XMLException {
        try {
            XMLUtils.saveXMLDocument(document, out);
        } catch (XMLException e) {
            throw new XMLException("Could not finalize registry", e);
        }
    }
    
    private void loadRegistryComponents(RegistryNode parentNode, Element parentElement) throws InitializationException {
        Element element = XMLUtils.getChild(parentElement, "components");
        
        if (element != null) {
            for (Element child: XMLUtils.getChildren(element)) {
                if (child.getNodeName().equals("product")) {
                    final Product product = new Product().loadFromDom(child);
                    final List<Product> existing = getProducts(
                            product.getUid(),
                            product.getVersion(),
                            product.getSupportedPlatforms());
                    
                    if (existing.size() == 0) {
                        parentNode.addChild(product);
                        loadRegistryComponents(product, child);
                    } else {
                        loadRegistryComponents(existing.get(0), child);
                    }
                }
                
                if (child.getNodeName().equals("group")) {
                    final Group group = new Group().loadFromDom(child);
                    final Group existing = getGroup(group.getUid());
                    
                    if (existing == null) {
                        parentNode.addChild(group);
                        loadRegistryComponents(group, child);
                    } else {
                        loadRegistryComponents(existing, child);
                    }
                }
            }
        }
    }
    
    // basic queries ////////////////////////////////////////////////////////////////
    public List<RegistryNode> query(RegistryFilter filter) {
        List<RegistryNode>  matches = new ArrayList<RegistryNode>();
        Queue<RegistryNode> queue    = new LinkedList<RegistryNode>();
        
        queue.offer(registryRoot);
        while (queue.peek() != null) {
            RegistryNode node = queue.poll();
            
            if (filter.accept(node)) {
                matches.add(node);
            }
            
            for (RegistryNode child: node.getChildren()) {
                queue.offer(child);
            }
        }
        
        return matches;
    }
    
    public List<Product> queryProducts(RegistryFilter filter) {
        List<Product> components = new ArrayList<Product>();
        
        for (RegistryNode node: query(filter)) {
            if (node instanceof Product) {
                components.add((Product) node);
            }
        }
        
        return components;
    }
    
    public List<Group> queryGroups(RegistryFilter filter) {
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
    
    // products queries /////////////////////////////////////////////////////////////
    public List<Product> getProducts() {
        return queryProducts(TrueFilter.INSTANCE);
    }
    
    public List<Product> getProducts(String uid) {
        return queryProducts(new ProductFilter(uid, targetPlatform));
    }
    
    public List<Product> getProducts(String uid, List<Platform> platforms) {
        return queryProducts(new ProductFilter(uid, platforms));
    }
    
    public List<Product> getProducts(String uid, Version lower, Version upper) {
        return queryProducts(new ProductFilter(uid, lower, upper, targetPlatform));
    }
    
    public List<Product> getProducts(String uid, Version version, List<Platform> platforms) {
        return queryProducts(new ProductFilter(uid, version, platforms));
    }
    
    public List<Product> getProducts(Dependency dependency) {
        switch (dependency.getType()) {
            case REQUIREMENT:
                if (dependency.getVersionResolved() != null) {
                    return queryProducts(new ProductFilter(
                            dependency.getUid(),
                            dependency.getVersionResolved(),
                            dependency.getVersionResolved(),
                            targetPlatform));
                }
            case CONFLICT:
                return queryProducts(new ProductFilter(
                        dependency.getUid(),
                        dependency.getVersionLower(),
                        dependency.getVersionUpper(),
                        targetPlatform));
            case INSTALL_AFTER:
                return queryProducts(new ProductFilter(
                        dependency.getUid(),
                        targetPlatform));
            default:
                ErrorManager.notifyCritical("unknown dependency type");
        }
        
        // the only way for us to reach this spot is to get to 'default:' in the
        // switch, but ErrorManager.notifyCritical() will cause a System.exit(),
        // so the below line is present only for successful compilation
        return null;
    }
    
    public List<Product> getProducts(Status status) {
        return queryProducts(new ProductFilter(status));
    }
    
    public List<Product> getProducts(DetailedStatus detailedStatus) {
        return queryProducts(new ProductFilter(detailedStatus));
    }
    
    public Product getProduct(final String uid, final Version version) {
        List<Product> candidates = queryProducts(new ProductFilter(uid, version, targetPlatform));
        
        return (candidates.size() > 0) ? candidates.get(0) : null;
    }
    
    // groups queries ///////////////////////////////////////////////////////////////
    public Group getGroup(String uid) {
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
    
    public List<Product> getComponentsToUninstall() {
        final List<Product> products = new ArrayList<Product>();
        
        Product product;
        while ((product = getNextComponentToUninstall(products)) != null) {
            products.add(product);
        }
        
        return products;
    }
    
    private Product getNextComponentToInstall(List<Product> currentList) {
        for (Product product: getProducts()) {
            if ((product.getStatus() == Status.TO_BE_INSTALLED) &&
                    !currentList.contains(product) &&
                    product.checkDependenciesForInstall()) {
                boolean productIsGood = true;
                
                // all products satisfying the requirement and install-after
                // dependencies which are planned for installation should be already
                // present in the list
                for (Dependency dependency: product.getDependencies(
                        DependencyType.REQUIREMENT,
                        DependencyType.INSTALL_AFTER)) {
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
    
    private Product getNextComponentToUninstall(List<Product> currentList) {
        for (Product product: getProducts()) {
            if ((product.getStatus() == Status.TO_BE_UNINSTALLED) &&
                    !currentList.contains(product) &&
                    product.checkDependenciesForUninstall()) {
                boolean productIsGood = true;
                
                for (Product dependent: getProducts()) {
                    if ((dependent.getStatus() != Status.NOT_INSTALLED) &&
                            !currentList.contains(dependent) &&
                            product.satisfiesRequirement(dependent)) {
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
    
    // properties ///////////////////////////////////////////////////////////////////
    public Properties getProperties() {
        return properties;
    }
    
    public String getProperty(String name) {
        return properties.getProperty(name);
    }
    
    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }
    
    // state file methods ///////////////////////////////////////////////////////////
    public void loadStateFile(File stateFile, Progress progress) throws InitializationException {
        try {
            LogManager.log(ErrorLevel.DEBUG, "Loading state file from " + stateFile.getAbsolutePath());
            
            File schemaFile =
                    FileProxy.getInstance().getFile(stateFileSchemaUri);
            
            SchemaFactory schemaFactory =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            
            Schema schema = schemaFactory.newSchema(schemaFile);
            
            DocumentBuilderFactory documentBuilderFactory =
                    DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setSchema(schema);
            documentBuilderFactory.setNamespaceAware(true);
            
            DocumentBuilder documentBuilder =
                    documentBuilderFactory.newDocumentBuilder();
            
            LogManager.log(ErrorLevel.DEBUG, "    parsing xml file...");
            Document document = documentBuilder.parse(stateFile);
            LogManager.log(ErrorLevel.DEBUG, "    ...complete");
            
            Node documentElement = document.getDocumentElement();
            
            // get the total number of components in this state file, we need this to
            // be able to properly update the progress
            int componentsNumber = getNumberOfComponents(documentElement);
            
            // we should get the percentage per component and we reserce one area for
            // registry-wide properties
            int percentageChunk = Progress.COMPLETE / (componentsNumber + 1);
            int percentageLeak = Progress.COMPLETE % (componentsNumber + 1);
            
            LogManager.log(ErrorLevel.DEBUG, "    parsing registry properties...");
            List<Node> propertiesNodes = XMLUtils.getChildList(documentElement, "./properties/property");
            
            progress.setDetail("Loading registry properties");
            for (int i = 0; i < propertiesNodes.size(); i++) {
                Node propertyNode = propertiesNodes.get(i);
                
                String name = XMLUtils.getAttribute(propertyNode, "name");
                String value = XMLUtils.getTextContent(propertyNode);
                
                LogManager.log(ErrorLevel.DEBUG, "        parsing property \"" + name + "\"");
                if (getProperty(name) == null) {
                    setProperty(name, value);
                }
            }
            LogManager.log(ErrorLevel.DEBUG, "    ...complete");
            progress.addPercentage(percentageChunk + percentageLeak);
            
            LogManager.log(ErrorLevel.DEBUG, "    parsing components...");
            List<Node> componentsNodes = XMLUtils.getChildList(documentElement, "./components/product");
            for (Node componentNode: componentsNodes) {
                String uid = XMLUtils.getAttribute(componentNode, "uid");
                Version version = Version.getVersion(XMLUtils.getAttribute(componentNode, "version"));
                List<Platform> platforms = StringUtils.parsePlatforms(XMLUtils.getAttribute(componentNode, "platform"));
                
                LogManager.log(ErrorLevel.DEBUG, "        parsing component uid=" + uid + ", version=" + version);
                progress.setDetail("Loading component: uid=" + uid + ", version=" + version);
                if (platforms.contains(targetPlatform)) {
                    Product component = getProduct(uid, version);
                    
                    if (component != null) {
                        Status status = StringUtils.parseStatus(XMLUtils.getAttribute(componentNode, "status"));
                        switch (status) {
                            case NOT_INSTALLED:
                                continue;
                            case TO_BE_INSTALLED:
                                if (component.getStatus() != Status.INSTALLED) {
                                    component.setStatus(status);
                                } else {
                                    continue;
                                }
                                break;
                            case INSTALLED:
                                continue;
                            case TO_BE_UNINSTALLED:
                                if (component.getStatus() != Status.NOT_INSTALLED) {
                                    component.setStatus(status);
                                } else {
                                    continue;
                                }
                                break;
                        }
                        
                        List<Node> componentPropertiesNodes = XMLUtils.getChildList(componentNode, "./properties/property");
                        for (int i = 0; i < componentPropertiesNodes.size(); i++) {
                            Node propertyNode = componentPropertiesNodes.get(i);
                            
                            String name = XMLUtils.getAttribute(propertyNode, "name");
                            String value = XMLUtils.getTextContent(propertyNode);
                            
                            LogManager.log(ErrorLevel.DEBUG, "            parsing property \"" + name + "\"");
                            component.setProperty(name, value);
                        }
                    }
                }
            }
            LogManager.log(ErrorLevel.DEBUG, "    ...complete");
        } catch (DownloadException e) {
            throw new InitializationException(
                    "Could not load components", e);
        } catch (ParserConfigurationException e) {
            throw new InitializationException(
                    "Could not load components", e);
        } catch (SAXException e) {
            throw new InitializationException(
                    "Could not load components", e);
        } catch (IOException e) {
            throw new InitializationException(
                    "Could not load components", e);
        } catch (ParseException e) {
            throw new InitializationException(
                    "Could not load components", e);
        }
    }
    
    public void saveStateFile(File stateFile, Progress progress) throws FinalizationException {
        try {
            File schemaFile =
                    FileProxy.getInstance().getFile(stateFileSchemaUri);
            File stubFile =
                    FileProxy.getInstance().getFile(stateFileStubUri);
            
            SchemaFactory schemaFactory =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            
            Schema schema = schemaFactory.newSchema(schemaFile);
            
            DocumentBuilderFactory documentBuilderFactory =
                    DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setSchema(schema);
            documentBuilderFactory.setNamespaceAware(true);
            
            DocumentBuilder documentBuilder =
                    documentBuilderFactory.newDocumentBuilder();
            
            Document document = documentBuilder.parse(stubFile);
            
            Element documentElement = document.getDocumentElement();
            
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
            
            List<Product> components = queryProducts(new OrFilter(
                    new ProductFilter(Status.INSTALLED),
                    new ProductFilter(Status.NOT_INSTALLED)));
            if (components.size() > 0) {
                Element componentsNode = document.createElement("components");
                for (Product component: components) {
                    Element componentNode = document.createElement("product");
                    
                    componentNode.setAttribute("uid", component.getUid());
                    componentNode.setAttribute("version", component.getVersion().toString());
                    componentNode.setAttribute("platform", StringUtils.asString(component.getSupportedPlatforms(), " "));
                    
                    switch (component.getStatus()) {
                        case INSTALLED:
                            componentNode.setAttribute("status", Status.TO_BE_INSTALLED.toString());
                            break;
                        case NOT_INSTALLED:
                            componentNode.setAttribute("status", Status.TO_BE_UNINSTALLED.toString());
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
                        componentNode.appendChild(propertiesNode);
                    }
                    
                    componentsNode.appendChild(componentNode);
                }
                
                documentElement.appendChild(componentsNode);
            }
            
            XMLUtils.saveXMLDocument(document, stateFile);
        } catch (DownloadException e) {
            throw new FinalizationException("Could not finalize registry", e);
        } catch (ParserConfigurationException e) {
            throw new FinalizationException("Could not finalize registry", e);
        } catch (SAXException e) {
            throw new FinalizationException("Could not finalize registry", e);
        } catch (IOException e) {
            throw new FinalizationException("Could not finalize registry", e);
        } catch (XMLException e) {
            throw new FinalizationException("Could not finalize registry", e);
        }
    }
    
    // miscellanea //////////////////////////////////////////////////////////////////
    public File getLocalProductCache() {
        return localProductCache;
    }
    
    public RegistryNode getRegistryRoot() {
        return registryRoot;
    }
    
    public boolean hasInstalledChildren(RegistryNode parentNode) {
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
    
    private int getNumberOfComponents(Node node) {
        List<Node> list = XMLUtils.getChildList(node,"./components/(product,group)");
        int result = list.size();
        for(int i=0;i<list.size();i++) {
            result += getNumberOfComponents(list.get(i));
        }
        return result;
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
            Installer.DATA_DIRECTORY + "/bundled-registry.xml";
    
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
}
