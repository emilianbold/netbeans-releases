/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.websvc.manager;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.websvc.manager.api.*;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.websvc.manager.codegen.Wsdl2Java;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;

import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.xml.retriever.Retriever;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


public final class WebServiceManager {
    
    private static WebServiceManager wsMgr;
    
    public static String WEBSVC_HOME = WebServiceDescriptor.WEBSVC_HOME;
    
    private Map<WebServiceData, WsdlModelListenerImpl> compilationMap = new HashMap<WebServiceData,WsdlModelListenerImpl>();    
    private Set<WebServiceData> compilingServices = Collections.synchronizedSet(new HashSet<WebServiceData>());
            
    private RequestProcessor modeler;
    private RequestProcessor compiler;
    
    private WebServiceManager() {}
    
    public static WebServiceManager getInstance()   {
        if (wsMgr == null) {
            wsMgr = new WebServiceManager();
        }
        return wsMgr;
    }
    
    /**
     * Add webservice to the Web Service List Model.
     * @param wsData The WebServiceData to add
     * @throws java.io.IOException 
     */
    public void addWebService(WebServiceData wsData) throws IOException {
        WsdlModeler wsdlModeler;
        WsdlModelListenerImpl listener;
        
        if (!wsData.isCompiled()) {
            String wsdl = wsData.getURL();
            if (! wsdl.toLowerCase().endsWith("wsdl")) {
                return;
            }
            
            File localWsdlFile;
            File catalogFile;
            
            if (wsData.getCatalog() != null) {
                localWsdlFile = new File(wsdl);
                catalogFile = new File(wsData.getCatalog());
            }else {
                localWsdlFile = copyWsdlResources(wsdl);
                catalogFile = new File(WEBSVC_HOME, getCatalogForWsdl(wsdl));
                wsData.setCatalog(catalogFile.getAbsolutePath());
            }
            
            URL wsdlUrl = localWsdlFile.toURI().toURL();
            listener = new WsdlModelListenerImpl(localWsdlFile, wsData.getOriginalWsdl(), wsData.getPackageName(), wsData.getGroupId(), catalogFile);
            listener.webServiceData = wsData;
            listener.compileClients = false;
            wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlUrl);
            
        }else {
             listener = new WsdlModelListenerImpl(wsData);
             wsdlModeler = WsdlModelerFactory.getDefault().
                getWsdlModeler(new File(wsData.getURL()).toURI().toURL());
        }
        
        listener.setWsdlModeler(wsdlModeler);
        wsdlModeler.setPackageName(wsData.getPackageName());
        wsdlModeler.generateWsdlModel(listener);
    }
    
    public void ensureWebServiceClientReady(WebServiceData wsData) throws IOException {
        if (wsData.getURL() == null || ! new File(wsData.getURL()).isFile()) {
            File localWsdlFile = copyWsdlResources(wsData.getOriginalWsdl());
            File catalogFile = new File(WEBSVC_HOME, getCatalogForWsdl(wsData.getOriginalWsdl()));
            wsData.setCatalog(catalogFile.getAbsolutePath());
            wsData.setURL(localWsdlFile.getAbsolutePath());
        }
        
        URL wsdlLocation = new File(wsData.getURL()).toURL();
        WsdlModeler wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlLocation);
        wsdlModeler.setPackageName(wsData.getPackageName());
        WsdlModel model = wsdlModeler.getAndWaitForWsdlModel();
        
        List<WsdlService> services = model.getServices();
        if (services.isEmpty()) {
            throw new IllegalArgumentException(wsdlLocation + " has no services."); //NOI18N
        }

        for (WsdlService service : services) {
            if (service.getName().equals(wsData.getName())) {
                wsData.setWsdlService(service);

                WebServiceListModel.getInstance().addWebService(wsData);
                WebServiceGroup group = WebServiceListModel.getInstance().getWebServiceGroup(wsData.getGroupId());
                if (group != null) {
                    group.add(wsData.getId());
                }

                compileService(wsData);
                break;
            }
        }
    }

    public void refreshWebService(WebServiceData wsData) throws IOException {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                final WebServiceData inputData = wsData;
                final IOException[] exception = new IOException[1];
                exception[0] = null;
                
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        try {
                            refreshWebService(inputData);
                        }catch (IOException ex) {
                            exception[0] = ex;
                        }
                    }
                });
                
                if (exception[0] != null) throw exception[0];
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }else {
            removeWebService(wsData, false);
            wsData.setURL(null);
            wsData.setCompiled(false);
            wsData.setCatalog(null);
            wsData.setWsdlService(null);
            wsData.setJaxRpcDescriptorPath(null);
            wsData.setJaxRpcDescriptor(null);
            wsData.setJaxWsDescriptor(null);
            wsData.setJaxWsDescriptorPath(null);

            String wsdl = wsData.getOriginalWsdl();

            File localWsdlFile;
            File catalogFile;

            localWsdlFile = copyWsdlResources(wsdl);
            catalogFile = new File(WEBSVC_HOME, getCatalogForWsdl(wsdl));
            wsData.setCatalog(catalogFile.getAbsolutePath());

            URL wsdlUrl = localWsdlFile.toURI().toURL();
            WsdlModelListenerImpl listener = new WsdlModelListenerImpl(localWsdlFile, wsData.getOriginalWsdl(), wsData.getPackageName(), wsData.getGroupId(), catalogFile);
            //listener.refreshing = true;
            listener.webServiceData = wsData;
            WsdlModeler wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlUrl);
            wsdlModeler.setPackageName(wsData.getPackageName());
            listener.setWsdlModeler(wsdlModeler);
            wsdlModeler.generateWsdlModel(listener);
        }
    }
    
    public void resetWebService(WebServiceData wsData) {
        removeWebService(wsData, false);
        // TODO this is strange - WebServiceData objects in the uncompiled state
        // shouldn't rely on this property but should get the original wsdl field
        wsData.setURL(wsData.getOriginalWsdl());
        wsData.setCompiled(false);
        wsData.setCatalog(null);
        wsData.setWsdlService(null);
        wsData.setJaxRpcDescriptorPath(null);
        wsData.setJaxRpcDescriptor(null);
        wsData.setJaxWsDescriptor(null);
        wsData.setJaxWsDescriptorPath(null);        
    }
    
    /**
     * Add webservice to the Web Service List Model.
     * 
     * @param wsdl the wsdl URL
     * @param packageName the package for the webservice java classes
     * @param groupId the id of the group the webservice belongs to
     * 
     * @throws java.io.IOException if the web service could not be added
     */
    public void addWebService(String wsdl, String packageName, String groupId) throws IOException {
        File localWsdlFile = copyWsdlResources(wsdl);
        
        URL wsdlUrl = localWsdlFile.toURI().toURL();
        File catalogFile = new File(WEBSVC_HOME, getCatalogForWsdl(wsdl));
        
        WsdlModelListenerImpl listener = new WsdlModelListenerImpl(localWsdlFile, wsdl, packageName, groupId, catalogFile);
        
        WsdlModeler wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlUrl);
        listener.setWsdlModeler(wsdlModeler);
        wsdlModeler.setPackageName(packageName);
        wsdlModeler.generateWsdlModel(listener);
    }
    
    /**
     * Removes a webservice from the Web Service List Model.
     * Client jars and other data are deleted from the filesystem.
     * 
     * @param wsData the WebService to remove
     */    
    public void removeWebService(WebServiceData wsData) {
        removeWebService(wsData, true);
    }
    
    private void removeWebService(WebServiceData wsData, boolean removeFromModel) {
        if (removeFromModel)
            WebServiceListModel.getInstance().removeWebService(wsData.getId());
        Collection<? extends WebServiceManagerExt> extensions = Lookup.getDefault().lookupAll(WebServiceManagerExt.class);
        WebServiceDescriptor jaxRpcDescriptor = wsData.getJaxRpcDescriptor();
        WebServiceDescriptor jaxWsDescriptor = wsData.getJaxWsDescriptor();
        
        for (WebServiceManagerExt extension : extensions) {            
            if (jaxRpcDescriptor != null)
                extension.wsServiceRemovedExt(jaxRpcDescriptor);
            if (jaxWsDescriptor != null) 
                extension.wsServiceRemovedExt(jaxWsDescriptor);
        }
        
        deleteWsArtifacts(jaxRpcDescriptor);
        deleteWsArtifacts(jaxWsDescriptor);
        
        // remove w/s directory
        if (wsData.getName() != null) {
            new File(WEBSVC_HOME, wsData.getName()).delete();
        }
        
        if (wsData.getURL() == null) {
            return;
        }
        
        boolean deleteWsdl = true;
        WebServiceListModel model = WebServiceListModel.getInstance();
        for (WebServiceData data : model.getWebServiceSet()) {
            if (data != wsData && wsData.getURL().equals(data.getURL())) {
                deleteWsdl = false;
                break;
            }
        }
        
        if (deleteWsdl) {
            // remove the top-level wsdl file
            extendedDelete(new File(wsData.getURL()));

            if (wsData.getCatalog() != null) {
                File catalogFile = new File(wsData.getCatalog());
                if (catalogFile.exists()) {
                    rmDir(catalogFile.getParentFile());
                }
            }
        }
    }
    
    /**
     * Generates the client jars from a web service that was previously
     * added to the model.
     * 
     * @param wsData the data for the web service
     */ 
    public void compileWebService(final WebServiceData wsData) {
        Runnable startCompileTask = new Runnable() {
            public void run() {
                if (WebServiceListModel.getInstance().getWebService(wsData.getId()) == null) {
                    return;
                }
                
                compilingServices.add(wsData);
                Runnable compileTask = new Runnable() {
                    public void run() {
                        WsdlModelListenerImpl compiler = compilationMap.remove(wsData);
        
                        if (wsData.isCompiled() || compiler == null) {
                            return;
                        }
                        compileService(wsData);
                    }
                };

                getCompilationRequestProcessor().post(compileTask);                
            }
        };
        
        getRequestProcessor().post(startCompileTask);
    }
    
    public boolean isCompiling(WebServiceData wsData) {
        return compilingServices.contains(wsData);
    }
    
    public RequestProcessor getRequestProcessor() {
        if (modeler == null) {
            modeler = new RequestProcessor("Services/Web services-modeler"); // NOI18N
        }
        
        return modeler;
    }
    
    public RequestProcessor getCompilationRequestProcessor() {
        if (compiler == null) {
            compiler = new RequestProcessor("Services/WebServices-compiler"); // NOI18N
        }
        
        return compiler;
    }
    
    private void addServiceCompiler(WebServiceData wsData, WsdlModelListenerImpl compiler) {
        compilationMap.put(wsData, compiler);
    }
    
    /**
     * Utility method to remove the web service and delete any generated artifacts
     * @param proxyDescriptor the WebServiceDescriptor representing a set of proxy jars
     */
    private void deleteWsArtifacts(WebServiceDescriptor proxyDescriptor) {
        if (proxyDescriptor == null) {
            return;
        }
        
        File proxyRoot = proxyDescriptor.getXmlDescriptorFile().getParentFile();
        assert proxyRoot != null;
        
        // delete all registered jar files
        for (WebServiceDescriptor.JarEntry jar : proxyDescriptor.getJars()) {
            File jarFile = new File(proxyRoot, jar.getName());
            jarFile.delete();
        }
        
        // delete xml descriptor
        proxyDescriptor.getXmlDescriptorFile().delete();
        
        // remove the empty directory
        proxyRoot.delete();
    }
    
    private static void rmDir(File dir) {
        if (dir == null) return;

        File[] files = dir.listFiles();
        for (int i = 0; files != null && i < files.length; i++) {
            if (files[i].isDirectory()) {
                rmDir(files[i]);
            }
            files[i].delete();
        }
        dir.delete();
    }
    
    private static void extendedDelete(File f) {
        boolean result = f.delete();
        if (!result) f.deleteOnExit();
    }
    
    private static String getCatalogForWsdl(String wsdlUrl) {
        try {
            URL url;
            url = new URL(wsdlUrl);
            
            String urlPath = url.getPath();
            int start;
            if (url.getProtocol().toLowerCase().startsWith("file")) { // NOI18N
                start = urlPath.lastIndexOf(System.getProperty("path.separator")); // NOI18N
                start = (start < 0) ? urlPath.lastIndexOf("/") : start; // NOI18N
            }else {
                start = urlPath.lastIndexOf("/");
            }
            start = (start < 0) ? 0 : start;
            
            return urlPath.substring(start).replace('.', '-') + "-catalog/catalog.xml"; // NOI18N
        }catch (IOException ex) {
            Logger.getLogger(WebServiceManager.class.getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
            return null;
        }
    }
    
    private static File copyWsdlResources(String wsdlUrl) throws IOException {
        File userDirFile = new File(WEBSVC_HOME);
        File catalogFile = new File(userDirFile, getCatalogForWsdl(wsdlUrl));
        File dir = catalogFile.getParentFile();
        
        boolean success = false;
        dir = catalogFile.getParentFile();
        try {
            FileObject dirFO = FileUtil.createFolder(dir);
            URI catalog = catalogFile.toURI();
            URI wsdlUri = new URL(wsdlUrl).toURI();

            Retriever retriever = Retriever.getDefault();
            FileObject wsdlFO = retriever.retrieveResource(dirFO, catalog, wsdlUri);
            
            if (wsdlFO == null) {
                throw new IOException(NbBundle.getMessage(WebServiceManager.class, "WSDL_COPY_ERROR"));
            }
            
            FileObject userdir = FileUtil.createFolder(new File(WEBSVC_HOME));
            File result = FileUtil.toFile(wsdlFO);
            success = true;
            return result;
        }catch (URISyntaxException ex) {
            throw new IOException(ex.getLocalizedMessage());
        }finally {
            if (catalogFile.exists() && !success) {
                rmDir(catalogFile.getParentFile());
            }
        }
    }

    static final class WsdlModelListenerImpl implements WsdlModelListener{
        private boolean newlyAdded;
        //private boolean refreshing;
        private String packageName;
        private String groupId;
        private WsdlModeler modeler = null;
        private File wsdlFile;
        private WebServiceData webServiceData;
        private File catalogFile;
        private String originalWsdl;
        private boolean compileClients = true;
        
        WsdlModelListenerImpl(File wsdlFile, String originalWsdl, String packageName, String groupId, File catalogFile) throws IOException {
            this.packageName = packageName;
            this.groupId = groupId;
            this.newlyAdded = true;
            this.wsdlFile = wsdlFile;
            this.catalogFile = catalogFile;
            this.originalWsdl = originalWsdl;
        }
        
        WsdlModelListenerImpl(WebServiceData wsData){
            if (wsData.getURL() == null || wsData.getCatalog() == null) {
                throw new IllegalArgumentException("Invalid WebServiceData: URL or Catalog");
            }
            this.webServiceData = wsData;
            this.newlyAdded = false;
            this.groupId = wsData.getGroupId();
            this.wsdlFile = new File(wsData.getURL());
            this.catalogFile = new File(wsData.getCatalog());
            this.originalWsdl = wsData.getOriginalWsdl();
        }
        
        protected void setWsdlModeler(WsdlModeler modeler) {
            this.modeler = modeler;
        }
        
        public void modelCreated(final WsdlModel model) {
            Runnable createClients = new Runnable() {
                public void run() {
                    if (model != null) {
                        createWSClient(model);
                    } else {
                        cleanup();
                    }
                }
            };
            
            WebServiceManager.getInstance().getRequestProcessor().post(createClients);
        }
        
        private void cleanup() {
                // clean up wsdl and catalog
                if (webServiceData != null)
                    WebServiceListModel.getInstance().removeWebService(webServiceData.getId());
                
                rmDir(wsdlFile);
                if (catalogFile != null) {
                    rmDir(catalogFile.getParentFile());
                }
                if (modeler != null) {
                    Throwable exc = modeler.getCreationException();
                    String cause = (exc != null) ? exc.getLocalizedMessage() : null;
                    String excString = (exc != null) ? exc.getClass().getName() + " - " + cause : null;
                    
                    String message = NbBundle.getMessage(WebServiceManager.class, "WS_MODELER_ERROR") + "\n\n" + excString; // NOI18N
                    
                    NotifyDescriptor d = new NotifyDescriptor.Message(message);
                    DialogDisplayer.getDefault().notify(d);
                }
        }
        
        private void createWSClient(WsdlModel model) {            
            WebServiceListModel listModel = WebServiceListModel.getInstance();            
            List<WsdlService> services = model.getServices();
            
            if (newlyAdded) {// || refreshing) {
                if (services.size() == 0) {
                    rmDir(wsdlFile);
                    if (catalogFile != null) {
                        rmDir(catalogFile.getParentFile());
                    }
                    
                    String message = NbBundle.getMessage(WebServiceManager.class, "WS_NO_METHODS_ERROR");
                    NotifyDescriptor d = new NotifyDescriptor.Message(message);
                    DialogDisplayer.getDefault().notify(d);
                    return;
                }
                
                for (WsdlService svc: services) {
                    boolean hasSoapPort = false;
                    for (WsdlPort port : svc.getPorts()) {
                        // is a SOAP port iff port.getAddress() is not null
                        hasSoapPort = hasSoapPort || (port.getAddress() != null);
                    }
                    
                    if (!hasSoapPort) {
                        NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(WebServiceManager.class, "WS_NO_SOAP_PORTS_ERROR"));
                        DialogDisplayer.getDefault().notify(d);                        
                        continue;
                    }
                    
                    List<WebServiceData> wsDataList = listModel.getWebServiceSet();
                    for (WebServiceData data : wsDataList) {
                        if (data.getName() == null) continue;
                        if (data.getName().equals(svc.getName()) && data != webServiceData) {
                            String message = NbBundle.getMessage(WebServiceManager.class, "WS_ALREADY_EXISTS_ERROR") + " " + svc.getName();
                            NotifyDescriptor d = new NotifyDescriptor.Message(message);
                            DialogDisplayer.getDefault().notify(d);
                            return;
                        }
                    }
                    
                    WebServiceData wsData;
                    
                    if (webServiceData == null) {
                        wsData = new WebServiceData(svc, wsdlFile.getAbsolutePath(), originalWsdl, groupId);
                               wsData.setCatalog(catalogFile.getAbsolutePath());
                        wsData.setPackageName(packageName);
                        listModel.addWebService(wsData);
                        listModel.getWebServiceGroup(groupId).add(wsData.getId());
                    }else {
                        wsData = webServiceData;
                        wsData.setWsdlService(svc);
                        wsData.setName(svc.getName());
                        wsData.setURL(wsdlFile.getAbsolutePath());
                        wsData.setOriginalWsdl(originalWsdl);
                        wsData.setResolved(true);
                        
                        listModel.addWebService(wsData);
                        listModel.getWebServiceGroup(groupId).add(wsData.getId());

                    }
                    
                    if (compileClients) {
                        final WebServiceData compileData = wsData;
                        Runnable compileTask = new Runnable() {
                            public void run() {
                                compileService(compileData);
                            }
                        };
                        
                        WebServiceManager.getInstance().compilingServices.add(compileData);
                        WebServiceManager.getInstance().getCompilationRequestProcessor().post(compileTask);                        
                    }else {
                        WebServiceManager.getInstance().addServiceCompiler(wsData, this);
                    }
                }
            }else {
                // TODO: This is inneficient, revisit
                for (WsdlService svc: services){
                    if (webServiceData.getName().equals(svc.getName())){
                        webServiceData.setWsdlService(svc);
                        webServiceData.setResolved(true);
                        WebServiceListModel.getInstance().addWebService(webServiceData);
                        WebServiceListModel.getInstance().
                                getWebServiceGroup(webServiceData.getGroupId()).add(webServiceData.getId());
                        break;
                    }
                }
            }
        }
    }
        static synchronized void compileService(WebServiceData wsData) {
            try {
                if (WebServiceListModel.getInstance().getWebService(wsData.getId()) == null || wsData.isCompiled()) {
                    return;
                }
                
                Collection<? extends WebServiceManagerExt> extensions = Lookup.getDefault().lookupAll(WebServiceManagerExt.class);
                WsdlService svc = wsData.getWsdlService();
                
                // compile the WSDL and create the proxy jars
                Wsdl2Java wsdl2Java = new Wsdl2Java(wsData);
                boolean success = wsdl2Java.createProxyJars();
                if (!success) {
                    return;
                }
                URL wsdl = new File(wsData.getURL()).toURI().toURL();
                String packageName = wsData.getPackageName();
                if (wsData.isJaxRpcEnabled()) {
                    WebServiceDescriptor jaxRpcDescriptor = new WebServiceDescriptor(wsData.getName(), packageName, WebServiceDescriptor.JAX_RPC_TYPE, wsdl, new File(WEBSVC_HOME, wsData.getJaxRpcDescriptorPath()), svc);
                    jaxRpcDescriptor.addJar(wsData.getName() + ".jar", WebServiceDescriptor.JarEntry.PROXY_JAR_TYPE);
                    jaxRpcDescriptor.addJar(wsData.getName() + "-src.jar", WebServiceDescriptor.JarEntry.SRC_JAR_TYPE);
                    
                    wsData.setJaxRpcDescriptor(jaxRpcDescriptor);
                }
                if (wsData.isJaxWsEnabled()) {
                    WebServiceDescriptor jaxWsDescriptor = new WebServiceDescriptor(wsData.getName(), packageName, WebServiceDescriptor.JAX_WS_TYPE, wsdl, new File(WEBSVC_HOME, wsData.getJaxWsDescriptorPath()), svc);
                    jaxWsDescriptor.addJar(wsData.getName() + ".jar", WebServiceDescriptor.JarEntry.PROXY_JAR_TYPE);
                    jaxWsDescriptor.addJar(wsData.getName() + "-src.jar", WebServiceDescriptor.JarEntry.SRC_JAR_TYPE);
                    
                    wsData.setJaxWsDescriptor(jaxWsDescriptor);
                }
                
                // create additional classes and jars from registered consumers
                // (e.g. designtime API implementations, client beans, dataproviders, etc.)
                boolean hasJaxWsConsumer = false;
                boolean hasJaxRpcConsumer = false;
                for (WebServiceManagerExt extension : extensions) {
                    if (wsData.getJaxRpcDescriptor() != null) {
                        if (extension.wsServiceAddedExt(wsData.getJaxRpcDescriptor())) {
                            hasJaxRpcConsumer = true;
                        }
                    }
                    if (wsData.getJaxWsDescriptor() != null) {
                        if (extension.wsServiceAddedExt(wsData.getJaxWsDescriptor())) {
                            hasJaxWsConsumer = true;
                        }
                    }
                }
                
                // delete everything if no consumer could be created, otherwise
                // only delete the artifacts of the failed ws type
                if (!hasJaxWsConsumer && !hasJaxRpcConsumer) {
                    WebServiceManager.getInstance().removeWebService(wsData);
                } else if (!hasJaxWsConsumer && wsData.getJaxWsDescriptor() != null) {
                    WebServiceManager.getInstance().deleteWsArtifacts(wsData.getJaxWsDescriptor());
                    wsData.setJaxWsDescriptor(null);
                    wsData.setJaxWsDescriptorPath(null);
                    wsData.setJaxWsEnabled(false);
                } else if (!hasJaxRpcConsumer && wsData.getJaxRpcDescriptor() != null) {
                    WebServiceManager.getInstance().deleteWsArtifacts(wsData.getJaxRpcDescriptor());
                    wsData.setJaxRpcDescriptor(null);
                    wsData.setJaxRpcDescriptorPath(null);
                    wsData.setJaxRpcEnabled(false);
                }
                
                if (hasJaxWsConsumer || hasJaxRpcConsumer) {
                    wsData.setCompiled(true);
                } else {
                    WebServiceManager.getInstance().removeWebService(wsData);
                }
            } catch(IOException ex) {
                Logger.global.log(Level.INFO, ex.getLocalizedMessage(), ex);
            } finally {
                WebServiceManager.getInstance().compilingServices.remove(wsData);
            }
        }
}
