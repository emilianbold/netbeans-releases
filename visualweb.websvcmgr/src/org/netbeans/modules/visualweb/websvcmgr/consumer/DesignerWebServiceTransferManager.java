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
package org.netbeans.modules.visualweb.websvcmgr.consumer;

import com.sun.rave.designtime.BeanCreateInfo;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.Result;
import com.sun.tools.ws.processor.model.java.JavaMethod;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.websvcmgr.WebServiceDescriptor;
import org.netbeans.modules.visualweb.websvcmgr.WebServiceMetaDataTransfer;
import org.netbeans.modules.visualweb.websvcmgr.WebServiceTransferManager;
import org.netbeans.modules.visualweb.websvcmgr.codegen.DataProviderJavaMethod;
import org.netbeans.modules.visualweb.websvcmgr.codegen.DataProviderModelMethod;
import org.netbeans.modules.visualweb.websvcmgr.model.WebServiceData;
import org.netbeans.modules.visualweb.websvcmgr.nodes.WebServiceLibReferenceHelper;
import org.netbeans.modules.visualweb.websvcmgr.util.Util;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author quynguyen
 */
public class DesignerWebServiceTransferManager implements WebServiceTransferManager {
    private static DataFlavor FLAVOR_METHOD_DISPLAY_ITEM;
    private static DataFlavor FLAVOR_PORT_DISPLAY_ITEM;
    
    
    static {
        try {
            FLAVOR_METHOD_DISPLAY_ITEM = new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType + "; class=" + DisplayItem.class.getName(), // NOI18N
                    "Web Services Method Display Item"); // XXX Localize
            
            FLAVOR_PORT_DISPLAY_ITEM = new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType + "; class=" + DisplayItem.class.getName(), // NOI18N
                    "Web Services Port Display Item"); // XXX Localize            
        }catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
    

    
    public DesignerWebServiceTransferManager() {
    }

    public Transferable addDataFlavors(Transferable transferable) {
        try {
            ExTransferable result = ExTransferable.create(transferable);
            
            if (transferable.isDataFlavorSupported(WebServiceMetaDataTransfer.METHOD_FLAVOR) &&
                !transferable.isDataFlavorSupported(FLAVOR_METHOD_DISPLAY_ITEM)) {
                final WebServiceMetaDataTransfer.Method method = 
                        (WebServiceMetaDataTransfer.Method)transferable.getTransferData(WebServiceMetaDataTransfer.METHOD_FLAVOR);
                
                if (method != null) {
                    ExTransferable.Single methodTransferable = new ExTransferable.Single(FLAVOR_METHOD_DISPLAY_ITEM) {
                        protected Object getData() throws IOException, UnsupportedFlavorException {
                            return new MethodBeanCreateInfo(
                                    method.getWebServiceData(),
                                    method.getWebServiceData().getWsdlService().getPortByName(method.getPortName()),
                                    method.getMethod());
                        }  
                    };
                    
                    result.put(methodTransferable);
                    return result;
                }
            }else if (transferable.isDataFlavorSupported(WebServiceMetaDataTransfer.PORT_FLAVOR) &&
                      !transferable.isDataFlavorSupported(FLAVOR_PORT_DISPLAY_ITEM)) {
                final WebServiceMetaDataTransfer.Port port =
                        (WebServiceMetaDataTransfer.Port)transferable.getTransferData(WebServiceMetaDataTransfer.PORT_FLAVOR);
                
                if (port != null) {
                    ExTransferable.Single portTransferable = new ExTransferable.Single(FLAVOR_PORT_DISPLAY_ITEM) {
                        protected Object getData() throws IOException, UnsupportedFlavorException {
                            return new PortBeanCreateInfo(
                                    port.getWebServiceData(),
                                    port.getWebServiceData().getWsdlService().getPortByName(port.getPortName()));
                        }
                    };
                    
                    result.put(portTransferable);
                    return result;
                }
            }
        }catch (Exception ex) {
        }
        
        return transferable;
    }

    private static WebServiceDescriptor getProxyDescriptorForProject(WebServiceData wsData) {
        boolean isJ2EE_15 = JsfProjectUtils.isJavaEE5Project(WebServiceLibReferenceHelper.getActiveProject());
        
        if (isJ2EE_15) {
            return wsData.getJaxWsDescriptor();
        }else {
            return wsData.getJaxRpcDescriptor();
        }        
    }
    
    private static void addJarReferences(boolean isJ2EE_15, Project project, WebServiceDescriptor descriptor) {
        if (requiresLibraryDefinitions(isJ2EE_15, project, descriptor)) {
            addLibraryDefinitions(isJ2EE_15, project, descriptor);
        }
        
        addReferenceArchives(isJ2EE_15, project, descriptor);
    }
    
    private static boolean requiresLibraryDefinitions(boolean isJ2EE_15, Project project, WebServiceDescriptor descriptor) {
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        
        if (wm == null) {
            return true;
        }else if (!isJ2EE_15) {
            ClassPath cp = ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.COMPILE);
            boolean hasJaxRpc = cp.findResource("javax/xml/rpc/Service.class") != null; // NOI18N
            hasJaxRpc = hasJaxRpc && cp.findResource("com/sun/xml/rpc/spi/tools/SOAPConstants.class") != null; // NOI18N
            hasJaxRpc = hasJaxRpc && cp.findResource("com/sun/xml/rpc/client/StubBase.class") != null; // NOI18N

            return !hasJaxRpc;
        }else {
            ClassPath cp = ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.COMPILE);
            boolean hasJaxWs = cp.findResource("javax/xml/ws/Service.class") != null; // NOI18N
            hasJaxWs = cp.findResource("com/sun/xml/ws/spi/ProviderImpl.class") != null; // NO18N
            return !hasJaxWs;
        }
    }
    
    /**
     * Adds the predefined library definitions to the lib lists
     */
    private static void addLibraryDefinitions(boolean isJ2EE_15, Project project, WebServiceDescriptor descriptor) {
        List<Library> compiletimeLibs = new LinkedList<Library>();
        Library webService20LibDef = WebServiceLibReferenceHelper.getWebServiceSupportLibDef(isJ2EE_15);
        
        compiletimeLibs.add(webService20LibDef);
        WebServiceLibReferenceHelper.addLibRefsToProject( project, compiletimeLibs);
    }
    
    /**
     * Adds the generated WS client and designtime jars to the lib lists
     */
    private static void addReferenceArchives(boolean isJ2EE_15, Project project, WebServiceDescriptor descriptor) {
        List<String> jars = new LinkedList<String>();
        String pathPrefix = descriptor.getXmlDescriptorFile().getParent() + File.separator;
        
        for (WebServiceDescriptor.JarEntry jar : descriptor.getJars()) {
            if (jar.getType().equals(DesignerWebServiceExtImpl.VW_DESIGNTIME_JAR) || 
                    jar.getType().equals(WebServiceDescriptor.JarEntry.PROXY_JAR_TYPE)) {
                jars.add(pathPrefix + jar.getName());
            }
            
        }
        
        WebServiceLibReferenceHelper.addArchiveRefsToProject(project, jars);
    }
    
    
    private static class PortBeanCreateInfo implements BeanCreateInfo {
        private final WebServiceData wsData;
        private final WsdlPort port;
        
        public PortBeanCreateInfo(WebServiceData wsData, WsdlPort port) {
            this.wsData = wsData;
            this.port = port;
        }
        
        public String getBeanClassName() {
            // XXX This hack causes the jars to be added to the project if the
            // webservice port/method node is even dragged onto the designer 
            // without dropping
            WebServiceDescriptor wsDescriptor = getProxyDescriptorForProject(wsData);
            String beanClassName = null;
            
            if (wsDescriptor != null) {
                DesignerWebServiceExtData data =
                        (DesignerWebServiceExtData)wsDescriptor.getConsumerData().get(DesignerWebServiceExtImpl.CONSUMER_ID);
                beanClassName = data.getPortToProxyBeanNameMap().get(port.getName());
            }
            
            if (beanClassName != null) {
                addJarReferences( (wsDescriptor.getWsType() == wsDescriptor.JAX_WS_TYPE), WebServiceLibReferenceHelper.getActiveProject(), wsDescriptor);
                return beanClassName;
            }else {
                return "x";
            }
            
            
        }
        
        public Result beanCreatedSetup(DesignBean designBean) {
            return Result.SUCCESS;
        }
        
        public String getDisplayName() {
            return wsData.getName();
        }
        
        public String getDescription() {
            return "";
        }
        
        public Image getLargeIcon() {
            return null;
        }
        
        public Image getSmallIcon() {
            return null;
        }
        
        public String getHelpKey() {
            return null;
        }
        
 
    } // End of MethodBeanCreateInfo.
    
    
    private static class MethodBeanCreateInfo implements BeanCreateInfo {
        private WebServiceData wsData;
        private WsdlPort port;
        private JavaMethod javaMethod;
        
        
        public MethodBeanCreateInfo(WebServiceData wsData, WsdlPort port, JavaMethod javaMethod) {
            this.wsData = wsData;
            this.port = port;
            this.javaMethod = javaMethod;
        }
        
        
        public String getBeanClassName() {
            // XXX This hack causes the jars to be added to the project if the
            // webservice port/method node is even dragged onto the designer
            // without dropping
            WebServiceDescriptor wsDescriptor = getProxyDescriptorForProject(wsData);
            String methodSig = null;
            
            if (wsDescriptor == null) {
                return "x";
            }else if (wsDescriptor.getWsType() == WebServiceDescriptor.JAX_WS_TYPE) {
                methodSig = Util.getMethodSignatureAsString(new DataProviderModelMethod(javaMethod));
            }else {
                java.lang.reflect.Method m = Util.getCorrespondingJaxRpcMethod(javaMethod, port.getName(), wsData);
                methodSig = Util.getMethodSignatureAsString(new DataProviderJavaMethod(m));
            }
            DesignerWebServiceExtData data = 
                    (DesignerWebServiceExtData)wsDescriptor.getConsumerData().get(DesignerWebServiceExtImpl.CONSUMER_ID);
            String beanClassName = data.getPortToDataProviderMap().get(port.getName()).get(methodSig);
            
            if (beanClassName != null) {
                addJarReferences( (wsDescriptor.getWsType() == wsDescriptor.JAX_WS_TYPE), WebServiceLibReferenceHelper.getActiveProject(), wsDescriptor);
                return beanClassName;
            }else {
                // XXX return something that cannot be instantiated by the designer so the drop will be rejected
                return "x";
            }
            
            
        }
        
        public Result beanCreatedSetup(DesignBean designBean) {
            return Result.SUCCESS;
        }

        public String getDisplayName() {
            WebServiceDescriptor wsDescriptor = wsData.getJaxWsDescriptor();
            if (wsDescriptor == null) {
                wsDescriptor = wsData.getJaxRpcDescriptor();
            }
            
            String methodSig = Util.getMethodSignatureAsString(new DataProviderModelMethod(javaMethod));
            DesignerWebServiceExtData data =
                    (DesignerWebServiceExtData)wsDescriptor.getConsumerData().get(DesignerWebServiceExtImpl.CONSUMER_ID);
            return data.getPortToDataProviderMap().get(port.getJavaName()).get(methodSig);
        }
        
        public String getDescription() {
            return "";
        }
        
        public Image getLargeIcon() {
            return null;
        }
        
        public Image getSmallIcon() {
            return null;
        }
        
        public String getHelpKey() {
            return null;
        }
        
    } // End of MethodBeanCreateInfo.
}
