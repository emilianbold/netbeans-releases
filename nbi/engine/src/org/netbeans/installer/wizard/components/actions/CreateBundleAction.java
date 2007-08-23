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

package org.netbeans.installer.wizard.components.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.product.filters.SubTreeFilter;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.EngineResources;
import org.netbeans.installer.utils.helper.ExtendedUri;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 * @author Kirill Sorokin
 */
public class CreateBundleAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            CreateBundleAction.class,
            "CBA.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(
            CreateBundleAction.class,
            "CBA.description"); // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Progress progress;
    
    public CreateBundleAction() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
    }
    
    public void execute() {
        long started = System.currentTimeMillis();
        
        LogManager.log("creating bundle");
        LogManager.log("    initializing registry and required products");
        final Registry registry = Registry.getInstance();
        final RegistryFilter filter =
                new SubTreeFilter(registry.getProductsToInstall());
        final List<Product> products = registry.queryProducts(filter);
        final List<Group> groups = registry.queryGroups(filter);
        
        LogManager.log("        products to install: " + StringUtils.asString(registry.getProductsToInstall()));
        LogManager.log("        selected products: " + StringUtils.asString(products));
        LogManager.log("        selected groups: " + StringUtils.asString(groups));
        
        int percentageChunk = Progress.START;
        int percentageLeak = Progress.COMPLETE;
        
        if (products.size() + groups.size() > 0) {
            percentageChunk =
                    Progress.COMPLETE / (products.size() + groups.size());
            percentageLeak =
                    Progress.COMPLETE % (products.size() + groups.size());
        }
        
        final String targetPath =
                System.getProperty(Registry.CREATE_BUNDLE_PATH_PROPERTY);
        final File targetFile = new File(targetPath);
        
        progress = new Progress();
        
        getWizardUi().setProgress(progress);
        
        JarFile engine = null;
        JarOutputStream output = null;
        
        try {
            LogManager.indent();
            LogManager.log("... creating bundle file at " + targetFile);
            progress.setTitle("Creating a redistributable bundle at " + targetFile);
            progress.setDetail("Adding installer engine...");
            
            engine = new JarFile(Installer.cacheInstallerEngine(new Progress()));
            output = new JarOutputStream(new FileOutputStream(targetFile));
            
            // transfer the engine, skipping existing bundled components
            final Enumeration entries = engine.entries();
            LogManager.log("... adding entries from the engine. Total : " + engine.size());
            while (entries.hasMoreElements()) {
                final JarEntry entry = (JarEntry) entries.nextElement();
                
                // check for cancel status
                if (isCanceled()) return;
                
                final String name = entry.getName();
                
                // skip the (possibly) already cached data
                if (name.startsWith(EngineResources.DATA_DIRECTORY)) {
                    continue;
                }
                
                // skip the signing information if it exists
                if (name.startsWith("META-INF/") &&
                        !name.equals("META-INF/") &&
                        !name.equals("META-INF/MANIFEST.MF")) {
                    continue;
                }
                
                output.putNextEntry(entry);
                StreamUtils.transferData(engine.getInputStream(entry), output);
            }
            
            output.putNextEntry(new JarEntry(
                    EngineResources.DATA_DIRECTORY + "/"));
            
            
            // transfer the engine files list
            output.putNextEntry(new JarEntry(
                    EngineResources.ENGINE_CONTENTS_LIST));
            StreamUtils.transferData(
                    ResourceUtils.getResource(EngineResources.ENGINE_CONTENTS_LIST),
                    output);
            
            // load default engine properties and set all components to be installed by default
            Properties engineProps = new Properties();
            engineProps.load(ResourceUtils.getResource(EngineResources.ENGINE_PROPERTIES));
            engineProps.setProperty(Registry.SUGGEST_INSTALL_PROPERTY,
                    StringUtils.EMPTY_STRING + true);
            output.putNextEntry(new JarEntry(
                    EngineResources.ENGINE_PROPERTIES));
            engineProps.store(output, null);
            
            
            progress.addPercentage(percentageLeak);
            LogManager.log("... adding " + products.size() + " products");
            for (Product product: products) {
                // check for cancel status
                if (isCanceled()) return;
                
                progress.setDetail(
                        "Adding " + product.getDisplayName() + "...");
                LogManager.log("... adding product : " + product.getDisplayName());
                
                final List<Platform> platforms = product.getPlatforms();
                final String entryPrefix =
                        EngineResources.DATA_DIRECTORY + "/" +
                        product.getUid() + "/" +
                        product.getVersion() + "/" +
                        StringUtils.asString(product.getPlatforms(), " ");
                final String uriPrefix =
                        FileProxy.RESOURCE_SCHEME_PREFIX +
                        EngineResources.DATA_DIRECTORY + "/" +
                        product.getUid() + "/" +
                        product.getVersion() + "/" +
                        StringUtils.asString(platforms, "%20");
                
                // create the required directories structure
                output.putNextEntry(new JarEntry(
                        EngineResources.DATA_DIRECTORY + "/" +
                        product.getUid() + "/"));
                output.putNextEntry(new JarEntry(
                        EngineResources.DATA_DIRECTORY + "/" +
                        product.getUid() + "/" +
                        product.getVersion() + "/" +
                        StringUtils.asString(product.getPlatforms(), " ") + "/"));
                output.putNextEntry(new JarEntry(
                        EngineResources.DATA_DIRECTORY + "/" +
                        product.getUid() + "/" +
                        product.getVersion() + "/" +
                        StringUtils.asString(product.getPlatforms(), " ") + "/" +
                        "logic" + "/"));
                output.putNextEntry(new JarEntry(
                        EngineResources.DATA_DIRECTORY + "/" +
                        product.getUid() + "/" +
                        product.getVersion() + "/" +
                        StringUtils.asString(product.getPlatforms(), " ") + "/" +
                        "data" + "/"));
                
                // transfer the icon
                output.putNextEntry(new JarEntry(entryPrefix + "/icon.png"));
                StreamUtils.transferFile(
                        new File(product.getIconUri().getLocal()),
                        output);
                
                // correct the local uri for the icon, so it gets saved correctly in
                // the registry file
                product.getIconUri().setLocal(new URI(uriPrefix + "/icon.png"));
                
                // transfer the configuration logic files
                final List<ExtendedUri> logicUris = product.getLogicUris();
                for (int i = 0; i < logicUris.size(); i++) {
                    final ExtendedUri logicUri = logicUris.get(i);
                    
                    // check for cancel status
                    if (isCanceled()) return;
                    
                    // transfer the file
                    output.putNextEntry(new JarEntry(
                            entryPrefix + "/logic/logic," + (i + 1) + ".jar"));
                    StreamUtils.transferFile(
                            new File(logicUri.getLocal()),
                            output);
                    
                    // delete the downloaded file -- we need to delete it only in 
                    // case it has been really downloaded, i.e. the local URI is
                    // different from the main remote one and the alternates
                    if (!logicUri.getLocal().equals(logicUri.getRemote()) &&
                            !logicUri.getAlternates().contains(logicUri.getLocal())) {
                        FileUtils.deleteFile(new File(logicUri.getLocal()));
                    }
                    
                    // correct the local uri, so it gets saved correctly
                    logicUris.get(i).setLocal(
                            new URI(uriPrefix + "/logic/logic," + (i + 1) + ".jar"));
                }
                
                // transfer the installation data files
                final List<ExtendedUri> dataUris = product.getDataUris();
                for (int i = 0; i < dataUris.size(); i++) {
                    final ExtendedUri dataUri = dataUris.get(i);
                    
                    // check for cancel status
                    if (isCanceled()) return;
                    
                    // transfer the file
                    output.putNextEntry(new JarEntry(
                            entryPrefix + "/data/data," + (i + 1) + ".jar"));
                    StreamUtils.transferFile(
                            new File(dataUris.get(i).getLocal()),
                            output);
                    
                    // delete the downloaded file -- we need to delete it only in 
                    // case it has been really downloaded, i.e. the local URI is
                    // different from the main remote one and the alternates
                    if (!dataUri.getLocal().equals(dataUri.getRemote()) &&
                            !dataUri.getAlternates().contains(dataUri.getLocal())) {
                        FileUtils.deleteFile(new File(dataUri.getLocal()));
                    }
                    
                    // correct the local uri, so it gets saved correctly
                    dataUris.get(i).setLocal(new URI(
                            uriPrefix + "/data/data," + (i + 1) + ".jar"));
                }
                
                // correct the product's status, so it gets saved correctly in the
                // registry file
                product.setStatus(Status.NOT_INSTALLED);
                
                // increment the progress percentage
                progress.addPercentage(percentageChunk);
            }
            
            LogManager.log("... adding " + groups.size() + " groups");
            for (Group group: groups) {
                // check for cancel status
                if (isCanceled()) return;
                
                // we should skip the registry root, as it is a somewhat artificial
                // node and does not have any meaning
                if (group.equals(registry.getRegistryRoot())) {
                    continue;
                }
                
                progress.setDetail(
                        "Adding " + group.getDisplayName() + "...");
                LogManager.log("... adding group : " + group.getDisplayName());
                final String entryPrefix =
                        EngineResources.DATA_DIRECTORY + "/" +
                        group.getUid();
                final String uriPrefix =
                        FileProxy.RESOURCE_SCHEME_PREFIX +
                        EngineResources.DATA_DIRECTORY + "/" +
                        group.getUid();
                
                // create the required directories structure
                output.putNextEntry(new JarEntry(
                        EngineResources.DATA_DIRECTORY + "/" +
                        group.getUid() + "/"));
                
                // transfer the icon
                output.putNextEntry(new JarEntry(entryPrefix + "/icon.png"));
                StreamUtils.transferFile(
                        new File(group.getIconUri().getLocal()),
                        output);
                
                // correct the local uri for the icon, so it gets saved correctly in
                // the registry file
                group.getIconUri().setLocal(new URI(uriPrefix + "/icon.png"));
                
                // increment the progress percentage
                progress.addPercentage(percentageChunk);
            }
            
            // check for cancel status
            if (isCanceled()) return;
            
            // serialize the registry: get the document and save it to the jar file
            output.putNextEntry(new JarEntry(
                    EngineResources.DATA_DIRECTORY + "/registry.xml"));
            XMLUtils.saveXMLDocument(
                    registry.getRegistryDocument(filter, false, true, true), output);
            
            // finally perform some minor cleanup to avoid errors later in the main
            // registry finalization - we set the local uri to be null, to avoid
            // cleanup attempts (they would fail, as the local uris now look like
            // resource:<...>
            for (Product product: products) {
                for (ExtendedUri uri: product.getLogicUris()) {
                    uri.setLocal(null);
                }
                for (ExtendedUri uri: product.getDataUris()) {
                    uri.setLocal(null);
                }
            }
            
            // set the products' status back to installed so that they are
            // correctly mentioned in the state file
            for (Product product: products) {
                product.setStatus(Status.INSTALLED);
            }
        } catch (IOException e) {
            ErrorManager.notifyError("Failed to create the bundle", e);
        } catch (XMLException e) {
            ErrorManager.notifyError("Failed to create the bundle", e);
        } catch (FinalizationException e) {
            ErrorManager.notifyError("Failed to create the bundle", e);
        } catch (URISyntaxException e) {
            ErrorManager.notifyError("Failed to create the bundle", e);
        } finally {
            if (engine != null) {
                try {
                    engine.close();
                } catch (IOException e) {
                    ErrorManager.notifyDebug("Failed to close the stream", e);
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    ErrorManager.notifyDebug("Failed to close the stream", e);
                }
            }
            long seconds = System.currentTimeMillis() - started;
            LogManager.log("... generating bundle finished");
            LogManager.log("[bundle] Time : " + (seconds / 1000) + "." + (seconds % 1000) + " seconds");
            LogManager.unindent();
        }
    }
    
    @Override
    public void cancel() {
        super.cancel();
        
        if (progress != null) {
            progress.setCanceled(true);
        }
    }
}
