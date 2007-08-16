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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.manager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.websvc.manager.codegen.Wsdl2Java;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;

import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.xml.retriever.Retriever;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


public final class WebServiceManager {
    
    private static WebServiceManager wsMgr;
    
    public static final String WEBSVC_HOME = System.getProperty("netbeans.user") + File.separator + "websvc"; // NOI18N
    
    private Map<WebServiceData, WsdlModelListenerImpl> compilationMap = new HashMap<WebServiceData,WsdlModelListenerImpl>();    
    private RequestProcessor modeler;
    private RequestProcessor compiler;
    
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
            listener = new WsdlModelListenerImpl(localWsdlFile, wsData.getPackageName(), wsData.getGroupId(), catalogFile);
            listener.webServiceData = wsData;
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
        
        WsdlModelListenerImpl listener = new WsdlModelListenerImpl(localWsdlFile, packageName, groupId, catalogFile);
        
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
        
        // remove the top-level wsdl file
        extendedDelete(new File(wsData.getURL()));
        
        File catalogFile = new File(wsData.getCatalog());
        if (catalogFile.exists()) {
            rmDir(catalogFile.getParentFile());
        }
    }
    
    /**
     * Generates the client jars from a web service that was previously
     * added to the model.
     * 
     * @param wsData the data for the web service
     */ 
    public void compileWebService(WebServiceData wsData) {
        WsdlModelListenerImpl compiler = compilationMap.remove(wsData);
        
        if (wsData.isCompiled() || compiler == null) return;
        
        compiler.compileService(wsData);
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
            File targetWsdlFile = new File(WEBSVC_HOME, wsdlFO.getNameExt());
            if (targetWsdlFile.exists()) {
                throw new IOException(NbBundle.getMessage(WebServiceManager.class, "WSDL_ALREADY_EXISTS_ERROR"));
            }
            
            FileObject userdirWsdl = FileUtil.copyFile(wsdlFO, userdir, wsdlFO.getName());
            File result = FileUtil.toFile(userdirWsdl);
            success = true;
            return result;
        }catch (URISyntaxException ex) {
            throw new IOException(NbBundle.getMessage(WebServiceManager.class, "WSDL_COPY_ERROR"));
        }finally {
            if (catalogFile.exists() && !success) {
                rmDir(catalogFile.getParentFile());
            }
        }
    }
    
    private static final class WsdlModelListenerImpl implements WsdlModelListener{
        private boolean newlyAdded;
        private String packageName;
        private String groupId;
        private WsdlModeler modeler = null;
        private File wsdlFile;
        private URL wsdl;
        private WebServiceData webServiceData;
        private File catalogFile;
        
        WsdlModelListenerImpl(File wsdlFile, String packageName, String groupId, File catalogFile) throws IOException {
            this.packageName = packageName;
            this.groupId = groupId;
            this.newlyAdded = true;
            this.wsdlFile = wsdlFile;
            this.wsdl = wsdlFile.toURI().toURL();
            this.catalogFile = catalogFile;
        }
        
        WsdlModelListenerImpl(WebServiceData wsData){
            this.webServiceData = wsData;
            this.newlyAdded = false;
        }
        
        protected void setWsdlModeler(WsdlModeler modeler) {
            this.modeler = modeler;
        }
        
        public void modelCreated(WsdlModel model) {
            if (model == null) {
                // clean up wsdl and catalog
                rmDir(wsdlFile);
                rmDir(catalogFile.getParentFile());
                
                if (modeler != null) {
                    Throwable exc = modeler.getCreationException();
                    String cause = (exc != null) ? exc.getLocalizedMessage() : null;
                    String excString = (exc != null) ? exc.getClass().getName() + " - " + cause : null;
                    
                    String message = NbBundle.getMessage(WebServiceManager.class, "WS_MODELER_ERROR") + "\n\n" + excString; // NOI18N
                    
                    NotifyDescriptor d = new NotifyDescriptor.Message(message);
                    DialogDisplayer.getDefault().notify(d);
                }
                return;
            }
            
            WebServiceListModel listModel = WebServiceListModel.getInstance();            
            List<WsdlService> services = model.getServices();
            
            if (newlyAdded) {
                for (WsdlService svc: services) {
                    List<WebServiceData> wsDataList = listModel.getWebServiceSet();
                    for (WebServiceData data : wsDataList) {
                        if (!data.isCompiled()) continue;
                        if (data.getName().equals(svc.getName())) {
                            String message = NbBundle.getMessage(WebServiceManager.class, "WS_ALREADY_EXISTS_ERROR") + " " + svc.getName();
                            NotifyDescriptor d = new NotifyDescriptor.Message(message);
                            DialogDisplayer.getDefault().notify(d);
                            return;
                        }
                    }
                    
                    WebServiceData wsData;
                    
                    if (webServiceData == null) {
                        wsData = new WebServiceData(svc, wsdlFile.getAbsolutePath(), packageName, groupId);
                        wsData.setCatalog(catalogFile.getAbsolutePath());
                        
                        listModel.addWebService(wsData);
                        listModel.getWebServiceGroup(groupId).add(wsData.getId());
                        
                        final WebServiceData compileData = wsData;
                        Runnable compileTask = new Runnable() {
                            public void run() {
                                compileService(compileData);
                            }
                        };
                        WebServiceManager.getInstance().getCompilationRequestProcessor().post(compileTask);
                    }else {
                        wsData = webServiceData;
                        wsData.setWsdlService(svc);
                        wsData.setName(svc.getName());
                        wsData.setURL(wsdlFile.getAbsolutePath());
                        
                        listModel.addWebService(wsData);
                        listModel.getWebServiceGroup(groupId).add(wsData.getId());

                        WebServiceManager.getInstance().addServiceCompiler(wsData, this);
                    }
                    

                }
            }else {
                // TODO: This is inneficient, revisit
                for (WsdlService svc: services){
                    if (webServiceData.getName().equals(svc.getName())){
                        webServiceData.setWsdlService(svc);
                        WebServiceListModel.getInstance().addWebService(webServiceData);
                        WebServiceListModel.getInstance().
                                getWebServiceGroup(webServiceData.getGroupId()).add(webServiceData.getId());
                        break;
                    }
                }
            }
        }
        
        protected synchronized void compileService(WebServiceData wsData) {
            if (wsData.isCompiled()) return;
            Collection<? extends WebServiceManagerExt> extensions = Lookup.getDefault().lookupAll(WebServiceManagerExt.class);
            WsdlService svc = wsData.getWsdlService();
            
            // compile the WSDL and create the proxy jars
            Wsdl2Java wsdl2Java = new Wsdl2Java(wsData, catalogFile);
            boolean success = wsdl2Java.createProxyJars();

            if (!success) {
                return;
            }

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
                    hasJaxRpcConsumer = hasJaxRpcConsumer || extension.wsServiceAddedExt(wsData.getJaxRpcDescriptor());
                }
                if (wsData.getJaxWsDescriptor() != null) {
                    hasJaxWsConsumer = hasJaxWsConsumer || extension.wsServiceAddedExt(wsData.getJaxWsDescriptor());
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
            }else {
                WebServiceListModel.getInstance().removeWebService(wsData.getId());
            }
        }        
    }
    
}
