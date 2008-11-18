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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.websvcmgr.codegen.DataProviderJavaMethod;
import org.netbeans.modules.visualweb.websvcmgr.codegen.DataProviderModelMethod;
import org.netbeans.modules.visualweb.websvcmgr.util.Util;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.util.WebServiceLibReferenceHelper;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WsdlSaasPort;
import org.netbeans.modules.websvc.saas.spi.ConsumerFlavorProvider;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author quynguyen
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.spi.ConsumerFlavorProvider.class)
public class DesignerWebServiceTransferManager implements ConsumerFlavorProvider {
    static DataFlavor FLAVOR_METHOD_DISPLAY_ITEM;
    static DataFlavor FLAVOR_PORT_DISPLAY_ITEM;
    
    
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
            if (transferable.isDataFlavorSupported(ConsumerFlavorProvider.WSDL_METHOD_FLAVOR) &&
                !transferable.isDataFlavorSupported(FLAVOR_METHOD_DISPLAY_ITEM)) 
            {
                Object data = transferable.getTransferData(ConsumerFlavorProvider.WSDL_METHOD_FLAVOR);
                if (data instanceof WsdlSaasMethod) {
                    final WsdlSaasMethod method = (WsdlSaasMethod) data;
                    final JavaMethod javaMethod = (JavaMethod) method.getJavaMethod().getInternalJAXWSJavaMethod();
                    if (javaMethod != null && !Util.hasOutput(javaMethod)) { // NOI18N
                        result.put(new ExTransferable.Single(FLAVOR_PORT_DISPLAY_ITEM) {
                            protected Object getData() throws IOException, UnsupportedFlavorException {
                                return new PortBeanCreateInfo(
                                        (WebServiceData) method.getSaas().getWsdlData(),(WsdlPort) method.getWsdlPort());
                            }
                        });
                    } else {
                        result.put(new ExTransferable.Single(FLAVOR_METHOD_DISPLAY_ITEM) {
                            protected Object getData() throws IOException, UnsupportedFlavorException {
                                return new MethodBeanCreateInfo(
                                    (WebServiceData) method.getSaas().getWsdlData(),(WsdlPort) method.getWsdlPort(),
                                    javaMethod);
                        }  
                        });
                    }
                    return result;
                }
            } else if (transferable.isDataFlavorSupported(ConsumerFlavorProvider.PORT_FLAVOR) &&
                      !transferable.isDataFlavorSupported(FLAVOR_PORT_DISPLAY_ITEM)) {
                final Object data = transferable.getTransferData(ConsumerFlavorProvider.PORT_FLAVOR);
                if (data instanceof WsdlSaasPort) {
                    final WsdlSaasPort port = (WsdlSaasPort) data;
                    result.put(new ExTransferable.Single(FLAVOR_PORT_DISPLAY_ITEM) {
                        protected Object getData() throws IOException, UnsupportedFlavorException {
                            return new PortBeanCreateInfo(
                                    (WebServiceData) port.getParentSaas().getWsdlData(),(WsdlPort) port.getWsdlPort());
                        }
                    });
                    return result;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
        }
        
        return transferable;
    }

    private static WebServiceDescriptor getProxyDescriptorForProject(WebServiceData wsData) {
        boolean isJ2EE_15 = JsfProjectUtils.isJavaEE5Project(Util.getActiveProject());
        
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
        Library webService20LibDef = Util.getWebServiceSupportLibDef(isJ2EE_15);
        
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
    
    
    static class PortBeanCreateInfo implements BeanCreateInfo {
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
                if (data != null) {
                    Map<String, String> proxyMap = data.getPortToProxyBeanNameMap();
                    if (proxyMap != null)
                        beanClassName = proxyMap.get(port.getName());
                }
            }
            
            if (beanClassName != null) {
                addJarReferences( (wsDescriptor.getWsType() == WebServiceDescriptor.JAX_WS_TYPE), Util.getActiveProject(), wsDescriptor);
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
    
    
    static class MethodBeanCreateInfo implements BeanCreateInfo {
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
            
            String beanClassName;
            if (data == null || data.getPortToDataProviderMap().get(port.getName()) == null) {
                beanClassName = null;
            }else {
                beanClassName = data.getPortToDataProviderMap().get(port.getName()).get(methodSig);
            }
            
            if (beanClassName != null) {
                addJarReferences( (wsDescriptor.getWsType() == WebServiceDescriptor.JAX_WS_TYPE), Util.getActiveProject(), wsDescriptor);
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
