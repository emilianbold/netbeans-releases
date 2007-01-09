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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
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
import org.netbeans.installer.product.filters.DependentsFilter;
import org.netbeans.installer.product.filters.OrFilter;
import org.netbeans.installer.product.filters.ProductChangedStatusFilter;
import org.netbeans.installer.product.filters.ProductFilter;
import org.netbeans.installer.product.filters.ProductDetailedStatusFilter;
import org.netbeans.installer.product.filters.ProductStatusFilter;
import org.netbeans.installer.product.filters.ProductGroupFilter;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.product.filters.RequirementsFilter;
import org.netbeans.installer.product.filters.TrueFilter;
import org.netbeans.installer.product.utils.DetailedStatus;
import org.netbeans.installer.product.utils.Status;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.exceptions.UnresolvedDependencyException;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.helper.Dependency;
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
    // Constants
    public static final String DEFAULT_LOCAL_PRODUCT_CACHE_DIRECTORY_NAME =
            "product-cache";
    
    public static final String LOCAL_PRODUCT_CACHE_DIRECTORY_NAME_PROPERTY =
            "nbi.product.local.cache.directory.name";
    
    public static final String DEFAULT_LOCAL_REGISTRY_FILE_NAME =
            "product-registry.xml";
    
    public static final String LOCAL_PRODUCT_REGISTRY_FILE_NAME_PROPERTY =
            "nbi.product.local.registry.file.name";
    
    public static final String DEFAULT_LOCAL_PRODUCT_REGISTRY_STUB_URI =
            "resource:org/netbeans/installer/product/default-product-registry.xml";
    
    public static final String LOCAL_PRODUCT_REGISTRY_STUB_PROPERTY =
            "nbi.product.local.registry.stub";
    
    public static final String DEFAULT_BUNDLED_PRODUCT_REGISTRY_URI =
            "resource:" + Installer.DATA_DIRECTORY + "bundled-product-registry.xml";
    
    public static final String BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY =
            "nbi.product.bundled.registry.uri";
    
    public static final String DEFAULT_PRODUCT_REGISTRY_SCHEMA_URI =
            "resource:org/netbeans/installer/product/product-registry.xsd";
    
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
            "resource:org/netbeans/installer/product/state-file.xsd";
    
    public static final String DEFAULT_STATE_FILE_STUB_URI =
            "resource:org/netbeans/installer/product/default-state-file.xml";
    
    public static final String STATE_FILE_STUB_PROPERTY =
            "nbi.state.file.stub";
    
    public static final String TARGET_PLATFORM_PROPERTY = 
            "nbi.target.platform";
    
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
    private File localDirectory         = Installer.getInstance().getLocalDirectory();
    private File localProductCache      = new File(localDirectory, DEFAULT_LOCAL_PRODUCT_CACHE_DIRECTORY_NAME).getAbsoluteFile();
    private File localProductRegistry   = new File(localDirectory, DEFAULT_LOCAL_REGISTRY_FILE_NAME).getAbsoluteFile();
    
    private String localRegistryStubURI = DEFAULT_LOCAL_PRODUCT_REGISTRY_STUB_URI;
    private String localRegistryURI     = localProductRegistry.toURI().toString();
    private String bundledRegistryURI   = DEFAULT_BUNDLED_PRODUCT_REGISTRY_URI;
    private String registrySchemaURI    = DEFAULT_PRODUCT_REGISTRY_SCHEMA_URI;
    
    private String stateFileSchemaURI   = DEFAULT_STATE_FILE_SCHEMA_URI;
    private String stateFileStubURI     = DEFAULT_STATE_FILE_STUB_URI;
    
    private List<String> remoteRegistryURIs = new ArrayList<String>();
    
    private RegistryNode productTreeRoot = createTreeRoot();
    
    private Properties properties           = new Properties();
    
    private Platform targetPlatform = SystemUtils.getCurrentPlatform();
    
    private Registry() {
    }
    
    public Registry(File file) throws InitializationException {
        this();
        
        initializeRegistryProperties();
        
        loadProductRegistry(file.toURI().toString(), new Progress());
    }
    
    public Registry(List<File> files) throws InitializationException {
        this();
        
        initializeRegistryProperties();
        
        for (File file: files) {
            loadProductRegistry(file.toURI().toString(), new Progress());
        }
    }
    
    public Registry(Platform platform, List<File> files) throws InitializationException {
        this();
        
        initializeRegistryProperties();
        
        this.targetPlatform = platform;
        
        for (File file: files) {
            loadProductRegistry(file.toURI().toString(), new Progress());
        }
    }
    
    public void initializeRegistry(Progress progress) throws InitializationException {
        LogManager.logEntry("initializing product registry");
        
        initializeRegistryProperties();
        
        CompositeProgress compositeProgress = new CompositeProgress();
        Progress childProgress;
        
        int percentageChunk = Progress.COMPLETE / (remoteRegistryURIs.size() + 2);
        int percentageLeak = Progress.COMPLETE % (remoteRegistryURIs.size() + 2);
        
        compositeProgress.synchronizeTo(progress);
        compositeProgress.synchronizeDetails(true);
        
        childProgress = new Progress();
        compositeProgress.addChild(childProgress, percentageChunk + percentageLeak);
        compositeProgress.setTitle("Loading local registry [" + localRegistryURI + "]");
        localRegistryURI = localProductRegistry.toURI().toString();
        loadProductRegistry(localRegistryURI, childProgress);
        
        // sleep a little to let the user perceive that something is happening
        SystemUtils.sleep(200);
        
        childProgress = new Progress();
        compositeProgress.addChild(childProgress, percentageChunk);
        compositeProgress.setTitle("Loading bundled registry [" + bundledRegistryURI + "]");
        loadProductRegistry(bundledRegistryURI, childProgress);
        
        // sleep a little to let the user perceive that something is happening
        SystemUtils.sleep(200);
        
        for (String remoteRegistryURI: remoteRegistryURIs) {
            childProgress = new Progress();
            compositeProgress.addChild(childProgress, percentageChunk);
            compositeProgress.setTitle("Loading remote registry [" + remoteRegistryURI + "]");
            loadProductRegistry(remoteRegistryURI, childProgress);
            
            // sleep a little to let the user perceive that something is happening
            SystemUtils.sleep(200);
        }
        
        try {
            resolveDependencies();
            checkCircles();//TODO: think about notification message
        } catch (UnresolvedDependencyException e) {
            throw new InitializationException("Cannot resolve dependencies", e);
        }
        
        if (System.getProperty(SOURCE_STATE_FILE_PATH_PROPERTY) != null) {
            loadStateFile(new File(System.getProperty(SOURCE_STATE_FILE_PATH_PROPERTY)), new Progress());
        }
        
        applyRegistryFilters();
        
        LogManager.logExit("... product registry initialization complete");
    }
    
    public void finalizeRegistry(Progress progress) throws FinalizationException {
        LogManager.logEntry("finalizing product registry");
        
        progress.setPercentage(Progress.START);
        
        if (Installer.getInstance().getExecutionMode() ==
                InstallerExecutionMode.NORMAL) {
            progress.setTitle("Saving local registry");
            progress.setDetail("Saving to " + localRegistryURI);
            
            saveProductRegistry(
                    new File(localDirectory, DEFAULT_LOCAL_REGISTRY_FILE_NAME),
                    new ProductStatusFilter(Status.INSTALLED));
        }
        
        if (System.getProperty(TARGET_STATE_FILE_PATH_PROPERTY) != null) {
            File stateFile =
                    new File(System.getProperty(TARGET_STATE_FILE_PATH_PROPERTY));
            
            saveStateFile(stateFile, new Progress());
        }
        
        progress.setPercentage(Progress.COMPLETE);
        
        LogManager.logExit("finalizing product registry");
    }
    
    // initialization ///////////////////////////////////////////////////////////////
    private void initializeRegistryProperties() throws InitializationException {
        LogManager.logEntry("initializing product registry properties");
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.logIndent("initializing local product cache directory");
        if (System.getProperty(LOCAL_PRODUCT_CACHE_DIRECTORY_NAME_PROPERTY) != null) {
            localProductCache = new File(localDirectory,
                    System.getProperty(LOCAL_PRODUCT_CACHE_DIRECTORY_NAME_PROPERTY));
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
        if (System.getProperty(LOCAL_PRODUCT_REGISTRY_FILE_NAME_PROPERTY) != null) {
            localProductRegistry = new File(localDirectory,
                    System.getProperty(LOCAL_PRODUCT_REGISTRY_FILE_NAME_PROPERTY));
            localRegistryURI = localProductRegistry.toURI().toString();
        }
        
        if (!localProductRegistry.exists()) {
            try {
                localProductRegistry = FileProxy.getInstance().getFile(localRegistryStubURI);
            } catch (DownloadException e) {
                throw new InitializationException("Cannot create local registry", e);
            }
        } else if (localProductRegistry.isDirectory()) {
            throw new InitializationException("Local registry is a directory!");
        } else if (!localProductRegistry.canRead()) {
            throw new InitializationException("Cannot read local registry - not enough permissions");
        } else if (!localProductRegistry.canWrite()) {
            throw new InitializationException("Cannot write to local registry - not enough permissions");
        }
        LogManager.log("    ... " + localProductRegistry);
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.log("    initializing local product registry stub uri");
        if (System.getProperty(LOCAL_PRODUCT_REGISTRY_STUB_PROPERTY) != null) {
            localRegistryStubURI =
                    System.getProperty(LOCAL_PRODUCT_REGISTRY_STUB_PROPERTY);
        }
        LogManager.log("    ... " + localRegistryStubURI);
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.log("    initializing bundled product registry uri");
        if (System.getProperty(BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY) != null) {
            bundledRegistryURI =
                    System.getProperty(BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY);
        }
        LogManager.log("    ... " + bundledRegistryURI);
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.log("    initializing product registry schema uri");
        if (System.getProperty(PRODUCT_REGISTRY_SCHEMA_URI_PROPERTY) != null) {
            registrySchemaURI = System.getProperty(PRODUCT_REGISTRY_SCHEMA_URI_PROPERTY);
        }
        LogManager.log("    ... " + registrySchemaURI);
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.log("    initializing remote product registries uris");
        if (System.getProperty(REMOTE_PRODUCT_REGISTRIES_PROPERTY) != null) {
            for (String remoteRegistryURI: System.getProperty(REMOTE_PRODUCT_REGISTRIES_PROPERTY).split("\n")) {
                remoteRegistryURIs.add(remoteRegistryURI);
            }
        }
        for (String string: remoteRegistryURIs) {
            LogManager.log("    ... " + string);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.log("    initializing state file schema uri");
        if (System.getProperty(STATE_FILE_SCHEMA_URI_PROPERTY) != null) {
            stateFileSchemaURI = System.getProperty(STATE_FILE_SCHEMA_URI_PROPERTY);
        }
        LogManager.log("    ... " + stateFileSchemaURI);
        
        /////////////////////////////////////////////////////////////////////////////
        LogManager.log("    initializing default state file uri");
        if (System.getProperty(STATE_FILE_STUB_PROPERTY) != null) {
            stateFileStubURI = System.getProperty(STATE_FILE_STUB_PROPERTY);
        }
        LogManager.log("    ... " + stateFileStubURI);
        
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
        if ((System.getProperty(TARGET_COMPONENT_UID_PROPERTY) != null) &&
                (System.getProperty(TARGET_COMPONENT_VERSION_PROPERTY) != null)) {
            String uid = System.getProperty(TARGET_COMPONENT_UID_PROPERTY);
            Version version = new Version(System.getProperty(TARGET_COMPONENT_VERSION_PROPERTY));
            
            Product target = getProductComponent(uid, version);
            
            List<Product> dependents = new ArrayList<Product>();
            
            for (Product component: queryComponents(TrueFilter.INSTANCE)) {
                if (component.requires(target)) {
                    dependents.add(component);
                }
            }
            
            for (Product component: queryComponents(TrueFilter.INSTANCE)) {
                if ((component == target) || component.requires(target) || component.isAncestor((target)) || component.isAncestor(dependents)) {
                    component.setVisible(true);
                } else {
                    component.setVisible(false);
                }
            }
        }
        
        for (Product component: queryComponents(TrueFilter.INSTANCE)) {
            if (component.getSupportedPlatforms().contains(targetPlatform)) {
                component.setVisible(true);
            } else {
                component.setVisible(false);
            }
        }
        
        for (Group group: queryGroups(TrueFilter.INSTANCE)) {
            if (group.isEmpty()) {
                group.setVisible(false);
            }
        }
    }
    
    private void resolveDependencies() throws UnresolvedDependencyException {
        Queue<RegistryNode> queue = new LinkedList<RegistryNode>();
        
        queue.offer(productTreeRoot);
        while (queue.peek() != null) {
            RegistryNode node = queue.poll();
            
            if (node instanceof Product) {
                Product component = (Product) node;
                
                for (Dependency rawDependency: component.getRawDependencies()) {
                    switch (rawDependency.getType()) {
                        case REQUIREMENT:
                            Product dependee = getProductComponent(rawDependency.getUid(), rawDependency.getLower(), rawDependency.getUpper());
                            
                            if (dependee != null) {
                                component.addRequirement(dependee);
                                
                            } else {
                                throw new UnresolvedDependencyException("Cannot resolve dependency on " + rawDependency.getUid() + " for " + component.getDisplayName());
                            }
                            break;
                        case CONFLICT:
                            List<Product> dependees = getProductComponents(rawDependency.getUid(), rawDependency.getLower(), rawDependency.getUpper());
                            
                            if (dependees.size() > 0) {
                                for (Product dep: dependees) {
                                    component.addConflict(dep);
                                }
                            } else {
                                throw new UnresolvedDependencyException("Cannot resolve dependency on " + rawDependency.getUid() + " for " + component.getDisplayName());
                            }
                            break;
                        default:
                            throw new UnresolvedDependencyException("Unknow dependency type: " + rawDependency.getType());
                    }
                }
            }
            
            for (RegistryNode child: node.getChildren()) {
                queue.offer(child);
            }
        }
    }
    
    // registry <-> dom <-> xml operations //////////////////////////////////////////
    public void loadProductRegistry(String registryUri, Progress progress) throws InitializationException {
        try {
            Document document = loadRegistryDocument(registryUri);
            
            loadRegistryComponents(productTreeRoot, document.getDocumentElement());
        } catch (XMLException e) {
            throw new InitializationException("Cannot load registry", e);
        }
    }
    
    private void loadRegistryComponents(RegistryNode parent, Element parentElement) throws InitializationException {
        Element componentsElement = XMLUtils.getChild(parentElement, "components");
        
        if (componentsElement != null) {
            for (Element child: XMLUtils.getChildren(componentsElement)) {
                if (child.getNodeName().equals("component")) {
                    Product component = new Product().loadFromDom(child);
                    List<Product> existing = getProductComponents(component.getUid(), component.getVersion(), component.getSupportedPlatforms());
                    
                    if (existing == null) {
                        parent.addChild(component);
                        loadRegistryComponents(component, child);
                    } else {
                        loadRegistryComponents(existing.get(0), child);
                    }
                }
                
                if (child.getNodeName().equals("group")) {
                    Group group = new Group().loadFromDom(child);
                    Group existing = getProductGroup(group.getUid());
                    
                    if (existing == null) {
                        parent.addChild(group);
                        loadRegistryComponents(group, child);
                    } else {
                        loadRegistryComponents(existing, child);
                    }
                }
            }
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
        return loadRegistryDocument(localRegistryStubURI);
    }
    
    public Document getRegistryDocument(RegistryFilter filter) throws XMLException, FinalizationException {
        Document document        = getEmptyRegistryDocument();
        Element  documentElement = document.getDocumentElement();
        
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
        
        Element componentsElement = productTreeRoot.saveChildrenToDom(document, filter);
        
        if (componentsElement != null) {
            documentElement.appendChild(componentsElement);
        }
        
        return document;
    }
    
    public Document loadRegistryDocument(String registryUri) throws XMLException {
        try {
            File schemaFile = FileProxy.getInstance().getFile(registrySchemaURI);
            File registryFile = FileProxy.getInstance().getFile(registryUri, true);
            
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
    
    public void saveRegistryDocument(Document document, OutputStream stream) throws XMLException {
        try {
            XMLUtils.saveXMLDocument(document, stream);
        } catch (XMLException e) {
            throw new XMLException("Could not finalize registry", e);
        }
    }
    
    // queries //////////////////////////////////////////////////////////////////////
    public List<RegistryNode> query(RegistryFilter filter) {
        List<RegistryNode>  matches = new ArrayList<RegistryNode>();
        Queue<RegistryNode> queue    = new LinkedList<RegistryNode>();
        
        queue.offer(productTreeRoot);
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
    
    public List<Product> queryComponents(RegistryFilter filter) {
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
    
    public List<Product> getComponents() {
        return queryComponents(TrueFilter.INSTANCE);
    }
    
    public List<Product> getComponentsInstalledDuringThisSession() {
        return queryComponents(new OrFilter(
                new ProductDetailedStatusFilter(DetailedStatus.INSTALLED_SUCCESSFULLY),
                new ProductDetailedStatusFilter(DetailedStatus.INSTALLED_WITH_WARNINGS)));
    }
    
    public List<Product> getComponentsInstalledSuccessfullyDuringThisSession() {
        return queryComponents(new ProductDetailedStatusFilter(DetailedStatus.INSTALLED_SUCCESSFULLY));
    }
    
    public List<Product> getComponentsInstalledWithWarningsDuringThisSession() {
        return queryComponents(new ProductDetailedStatusFilter(DetailedStatus.INSTALLED_WITH_WARNINGS));
    }
    
    public List<Product> getComponentsFailedToInstallDuringThisSession() {
        return queryComponents(new ProductDetailedStatusFilter(DetailedStatus.FAILED_TO_INSTALL));
    }
    
    public List<Product> getComponentsUninstalledDuringThisSession() {
        return queryComponents(new OrFilter(
                new ProductDetailedStatusFilter(DetailedStatus.UNINSTALLED_SUCCESSFULLY),
                new ProductDetailedStatusFilter(DetailedStatus.UNINSTALLED_WITH_WARNINGS)));
    }
    
    public List<Product> getComponentsUninstalledSuccessfullyDuringThisSession() {
        return queryComponents(new ProductDetailedStatusFilter(DetailedStatus.UNINSTALLED_SUCCESSFULLY));
    }
    
    public List<Product> getComponentsUninstalledWithWarningsDuringThisSession() {
        return queryComponents(new ProductDetailedStatusFilter(DetailedStatus.UNINSTALLED_WITH_WARNINGS));
    }
    
    public List<Product> getComponentsFailedToUninstallDuringThisSession() {
        return queryComponents(new ProductDetailedStatusFilter(DetailedStatus.FAILED_TO_UNINSTALL));
    }
    
    public List<Product> getInstalledProducts() {
        return queryComponents(new ProductStatusFilter(Status.INSTALLED));
    }
    
    public boolean wereErrorsEncountered() {
        boolean install = getComponentsFailedToInstallDuringThisSession().size() == 0;
        boolean uninstall = getComponentsFailedToUninstallDuringThisSession().size() == 0;
        
        return !install || !uninstall;
    }
    
    public boolean wereWarningsEncountered() {
        boolean install = getComponentsInstalledWithWarningsDuringThisSession().size() == 0;
        boolean uninstall = getComponentsUninstalledWithWarningsDuringThisSession().size() == 0;
        
        return !install || !uninstall;
    }
    
    public List<Product> getDependingComponents(Product component) {
        return queryComponents(new DependentsFilter(component));
    }
    
    public List<Product> getRequiredComponents(Product component) {
        return queryComponents(new RequirementsFilter(component));
    }
    
    public Product getProductComponent(String uid, Version version) {
        List<Product> candidates = queryComponents(new ProductFilter(uid, version, targetPlatform));
        
        return (candidates.size() > 0) ? candidates.get(0) : null;
    }
    
    public List<Product> getProductComponents(String uid, Version version, List<Platform> platforms) {
        List<Product> candidates = queryComponents(new ProductFilter(uid, version, platforms));
        
        return (candidates.size() > 0) ? candidates : null;
    }
    
    public Product getProductComponent(String uid, Version lower, Version upper) {
        Product newest = null;
        
        for (Product candidate: queryComponents(new ProductFilter(uid, lower, upper, targetPlatform))) {
            if ((newest == null) || newest.getVersion().olderThan(candidate.getVersion())) {
                newest = candidate;
            }
        }
        
        return newest;
    }
    
    public List<Product> getProductComponents(String uid, Version lower, Version upper) {
        return queryComponents(new ProductFilter(uid, lower, upper, targetPlatform));
    }
    
    public Group getProductGroup(String uid) {
        List<Group> candidates = queryGroups(new ProductGroupFilter(uid));
        
        return (candidates.size() > 0) ? candidates.get(0) : null;
    }
    
    public RegistryNode getProductTreeRoot() {
        return productTreeRoot;
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
    
    // installation order related queries ///////////////////////////////////////////
    public List<Product> getComponentsToInstall() {
        List<Product> components = new ArrayList<Product>();
        
        Product component;
        
        while ((component = getNextComponentToInstall(components)) != null) {
            components.add(component);
        }
        
        return components;
    }
    
    private Product getNextComponentToInstall(List<Product> currentList) {
        List<Product> components = queryComponents(TrueFilter.INSTANCE);
        
        for (Product component: components) {
            if ((component.getStatus() == Status.TO_BE_INSTALLED) && !currentList.contains(component) && checkDependenciesForInstall(component)) {
                boolean componentIsGood = true;
                
                for (Product requirement: component.getRequirements()) {
                    if ((requirement.getStatus() != Status.INSTALLED) && !currentList.contains(requirement)) {
                        componentIsGood = false;
                    }
                }
                
                if (componentIsGood) {
                    return component;
                }
            }
        }
        
        return null;
    }
    
    public List<Product> getComponentsToUninstall() {
        List<Product> components = new ArrayList<Product>();
        
        Product component;
        
        while ((component = getNextComponentToUninstall(components)) != null) {
            components.add(component);
        }
        
        return components;
    }
    
    private Product getNextComponentToUninstall(List<Product> currentList) {
        List<Product> components = queryComponents(TrueFilter.INSTANCE);
        
        for (Product component: components) {
            if ((component.getStatus() == Status.TO_BE_UNINSTALLED) && !currentList.contains(component) && checkDependenciesForUninstall(component)) {
                boolean componentIsGood = true;
                
                for (Product dependentComponent: components) {
                    if ((dependentComponent.getStatus() != Status.NOT_INSTALLED) && !currentList.contains(dependentComponent) && dependentComponent.getRequirements().contains(component)) {
                        componentIsGood = false;
                        break;
                    }
                }
                
                if (componentIsGood) {
                    return component;
                }
                
            }
        }
        return null;
    }
    
    // verification /////////////////////////////////////////////////////////////////
    public boolean checkDependenciesForInstall(Product component) {
        for (Product requirement: component.getRequirements()) {
            if ((requirement.getStatus() != Status.INSTALLED) && 
                    (requirement.getStatus() != Status.TO_BE_INSTALLED)) {
                return false;
            }
        }
        
        for (Product conflict: component.getConflicts()) {
            if ((conflict.getStatus() != Status.NOT_INSTALLED) && 
                    (conflict.getStatus() != Status.TO_BE_UNINSTALLED)) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean checkDependenciesForUninstall(Product component) {
        List<Product> components = queryComponents(TrueFilter.INSTANCE);
        
        for (Product dependent: components) {
            if (dependent.requires(component) &&
                    ((dependent.getStatus() == Status.INSTALLED) ||
                    (dependent.getStatus() == Status.TO_BE_INSTALLED))) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean checkDependencies() {
        List<Product> components = queryComponents(TrueFilter.INSTANCE);
        
        for (Product component: components) {
            if (component.getStatus() == Status.TO_BE_INSTALLED) {
                if (!checkDependenciesForInstall(component)) {
                    return false;
                }
            }
            if (component.getStatus() == Status.TO_BE_UNINSTALLED) {
                if (!checkDependenciesForUninstall(component)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private void checkCircles() throws UnresolvedDependencyException {
        final List<Product> list = queryComponents(TrueFilter.INSTANCE);
        for (Product component : list) {
            final Stack<Product> visited = new Stack<Product>();
            final Set<Product> conflictSet = new HashSet<Product>();
            final Set<Product> requirementSet = new HashSet<Product>();
            checkCircles(component, visited, conflictSet, requirementSet);
        }
    }
    
    private void checkCircles(Product component, Stack<Product> visited, Set<Product> conflictSet, Set<Product> requirementSet) throws UnresolvedDependencyException {
        if (visited.contains(component) || conflictSet.contains(component)) {
            throw new UnresolvedDependencyException("circles found");
        }
        visited.push(component);
        requirementSet.add(component);
        if (!Collections.disjoint(requirementSet, component.getConflicts())) {
            throw new UnresolvedDependencyException("circles found");
        }
        conflictSet.addAll(component.getConflicts());
        for (Product comp : component.getRequirements()) {
            checkCircles(comp, visited, conflictSet, requirementSet);
        }
        visited.pop();
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
    
    // various getters //////////////////////////////////////////////////////////////
    public File getLocalProductCache() {
        return localProductCache;
    }
    
    // state file methods ///////////////////////////////////////////////////////////
    public void loadStateFile(File stateFile, Progress progress) throws InitializationException {
        try {
            LogManager.log(ErrorLevel.DEBUG, "Loading state file from " + stateFile.getAbsolutePath());
            
            File schemaFile =
                    FileProxy.getInstance().getFile(stateFileSchemaURI);
            
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
            List<Node> componentsNodes = XMLUtils.getChildList(documentElement, "./components/component");
            for (Node componentNode: componentsNodes) {
                String uid = XMLUtils.getAttribute(componentNode, "uid");
                Version version = new Version(XMLUtils.getAttribute(componentNode, "version"));
                List<Platform> platforms = StringUtils.parsePlatforms(XMLUtils.getAttribute(componentNode, "platform"));
                
                LogManager.log(ErrorLevel.DEBUG, "        parsing component uid=" + uid + ", version=" + version);
                progress.setDetail("Loading component: uid=" + uid + ", version=" + version);
                if (platforms.contains(targetPlatform)) {
                    Product component = getProductComponent(uid, version);
                    
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
                    FileProxy.getInstance().getFile(stateFileSchemaURI);
            File stubFile =
                    FileProxy.getInstance().getFile(stateFileStubURI);
            
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
            
            List<Product> components = queryComponents(new ProductChangedStatusFilter());
            if (components.size() > 0) {
                Element componentsNode = document.createElement("components");
                for (Product component: components) {
                    Element componentNode = document.createElement("component");
                    
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
            
            XMLUtils.saveXMLDocument( document, stateFile);
            
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
    private int getNumberOfComponents(Node node) {
        List<Node> list = XMLUtils.getChildList(node,"./components/(component,group)");
        int result = list.size();
        for(int i=0;i<list.size();i++) {
            result += getNumberOfComponents(list.get(i));
        }
        return result;
    }
    
    private Group createTreeRoot() {
        return new Group();
    }
}
