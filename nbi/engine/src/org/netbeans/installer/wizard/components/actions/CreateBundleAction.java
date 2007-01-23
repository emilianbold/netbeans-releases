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
package org.netbeans.installer.wizard.components.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.product.utils.Status;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.ExtendedURI;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;
import org.netbeans.installer.wizard.components.WizardAction.WizardActionUi;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.w3c.dom.Document;

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
        final Registry registry = Registry.getInstance();
        final List<Product> components = registry.getComponentsToInstall();
        final int percentageChunk = Progress.COMPLETE / (components.size() + 1);
        final int percentageLeak = Progress.COMPLETE % (components.size() + 1);
        
        final String targetPath = System.getProperty(Installer.CREATE_BUNDLE_PATH_PROPERTY);
        final File   targetFile = new File(targetPath);
        
        progress = new Progress();
        
        getWizardUi().setProgress(progress);
        
        JarFile         engine = null;
        JarOutputStream output = null;
        try {
            progress.setTitle("Creating a redistributable bundle at " + targetFile);
            progress.setDetail("Adding installer engine...");
            progress.setPercentage(percentageLeak);
            
            engine = new JarFile(Installer.getInstance().getCachedEngine());
            output = new JarOutputStream(new FileOutputStream(targetFile));
            
            // first transfer the engine, skipping existing bundled components
            final Enumeration entries = engine.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = (JarEntry) entries.nextElement();
                
                // check for cancel status
                if (canceled) return;
                
                final String name = entry.getName();
                
                // skip the (possibly) already cached data
                if (name.startsWith(Installer.DATA_DIRECTORY)) {
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
            progress.addPercentage(percentageChunk);
            
            // then transfer configuration logic, installation data and icon for
            // selected components
            output.putNextEntry(new JarEntry(Installer.DATA_DIRECTORY + "/"));
            
            // put engine files list
            output.putNextEntry(new JarEntry(Installer.ENGINE_JAR_CONTENT_LIST));
            StreamUtils.transferData(ResourceUtils.getResource(Installer.ENGINE_JAR_CONTENT_LIST),output);
            
            for (Product component: components) {
                if (canceled) return; // check for cancel status
                
                progress.setDetail("Adding component \"" + component.getDisplayName() + "\"...");
                
                final List<Platform> platforms = component.getSupportedPlatforms();
                final String uriPrefix =
                        FileProxy.RESOURCE_SCHEME_PREFIX +
                        Installer.DATA_DIRECTORY + "/" +
                        component.getUid() + "/" +
                        component.getVersion() + "/" +
                        StringUtils.asString(platforms, "%20");
                
                output.putNextEntry(new JarEntry(Installer.DATA_DIRECTORY + "/" + component.getUid() + "/"));
                output.putNextEntry(new JarEntry(Installer.DATA_DIRECTORY + "/" + component.getUid() + "/" + component.getVersion() + "/" + StringUtils.asString(component.getSupportedPlatforms(), " ") + "/"));
                output.putNextEntry(new JarEntry(Installer.DATA_DIRECTORY + "/" + component.getUid() + "/" + component.getVersion() + "/" + StringUtils.asString(component.getSupportedPlatforms(), " ") + "/configuration-logic/"));
                output.putNextEntry(new JarEntry(Installer.DATA_DIRECTORY + "/" + component.getUid() + "/" + component.getVersion() + "/" + StringUtils.asString(component.getSupportedPlatforms(), " ") + "/installation-data/"));
                
                final File icon = FileProxy.getInstance().getFile(component.getIconUri().getRemote());
                
                output.putNextEntry(new JarEntry(Installer.DATA_DIRECTORY + "/" + component.getUid() + "/" + component.getVersion() + "/" + StringUtils.asString(component.getSupportedPlatforms(), " ") + "/icon.png"));
                StreamUtils.transferFile(icon, output);
                
                component.getIconUri().setLocal(new URI(uriPrefix + "/icon.png"));
                
                
                final List<ExtendedURI> logicUris = component.getConfigurationLogicUris();
                for (int i = 0; i < logicUris.size(); i++) {
                    if (canceled) return; // check for cancel status
                    
                    File logic = FileProxy.getInstance().getFile(logicUris.get(i).getLocal());
                    
                    output.putNextEntry(new JarEntry(Installer.DATA_DIRECTORY + "/" + component.getUid() + "/" + component.getVersion() + "/" + StringUtils.asString(component.getSupportedPlatforms(), " ") + "/configuration-logic/logic-" + (i + 1) + ".jar"));
                    StreamUtils.transferFile(logic, output);
                    
                    logicUris.get(i).setLocal(new URI(uriPrefix + "/configuration-logic/logic-" + (i + 1) + ".jar"));
                }
                
                final List<ExtendedURI> dataUris = component.getInstallationDataUris();
                for (int i = 0; i < dataUris.size(); i++) {
                    if (canceled) return; // check for cancel status
                    
                    File data = FileProxy.getInstance().getFile(dataUris.get(i).getLocal());
                    
                    output.putNextEntry(new JarEntry(Installer.DATA_DIRECTORY + "/" + component.getUid() + "/" + component.getVersion() + "/" + StringUtils.asString(component.getSupportedPlatforms(), " ") + "/installation-data/data-" + (i + 1) + ".jar"));
                    StreamUtils.transferFile(data, output);
                    
                    dataUris.get(i).setLocal(new URI(uriPrefix + "/installation-data/data-" + (i + 1) + ".jar"));
                }
                
                component.setStatus(Status.NOT_INSTALLED);
                
                progress.addPercentage(percentageChunk);
            }
            
            if (canceled) return; // check for cancel status
            
            // then serialize the registry
            final Document document = Registry.getInstance().getRegistryDocument(
                    new UriCorrectingFilter(components));
            
            output.putNextEntry(new JarEntry(
                    Installer.DATA_DIRECTORY + "/bundled-product-registry.xml"));
            Registry.getInstance().saveRegistryDocument(document, output);
        } catch (IOException e) {
            ErrorManager.notifyError("Failed to create the bundle", e);
        } catch (DownloadException e) {
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
                    ErrorManager.notify(ErrorLevel.DEBUG, "Failed to close the stream", e);
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    ErrorManager.notify(ErrorLevel.DEBUG, "Failed to close the stream", e);
                }
            }
        }
    }
    
    public void cancel() {
        super.cancel();
        
        if (progress != null) {
            progress.setCanceled(true);
        }
    }
    
    public static class UriCorrectingFilter implements RegistryFilter {
        private List<Product> components;
        
        public UriCorrectingFilter(List<Product> components) {
            this.components = components;
        }
        
        public boolean accept(final RegistryNode node) {
            if (components.contains(node)) {
                return true;
            }
            
            for (Product component: components) {
                if (node.isAncestor(component)) {
                    node.getIconUri().setLocal(null);
                    
                    if (node instanceof Product) {
                        for (ExtendedURI uri: ((Product) node).getConfigurationLogicUris()) {
                            uri.setLocal(null);
                        }
                        
                        for (ExtendedURI uri: ((Product) node).getInstallationDataUris()) {
                            uri.setLocal(null);
                        }
                    }
                    
                    return true;
                }
            }
            
            return false;
        }
    }
}
