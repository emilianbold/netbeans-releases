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
 */
package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.awt.Dialog;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.jaxws.project.GeneratedFilesHelper;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoints;
import org.netbeans.modules.websvc.api.jaxws.project.config.Handler;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChain;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChains;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChainsProvider;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.core.WebServiceReference;
import org.netbeans.modules.websvc.core.WebServiceTransferable;
import org.netbeans.modules.websvc.core.jaxws.actions.AddOperationAction;
import org.netbeans.modules.websvc.core.jaxws.actions.JaxWsRefreshAction;
import org.netbeans.modules.websvc.core.jaxws.actions.WsTesterPageAction;
import org.netbeans.modules.websvc.core.webservices.action.ConfigureHandlerAction;
import org.netbeans.modules.websvc.core.webservices.action.ConfigureHandlerCookie;
import org.netbeans.modules.websvc.core.webservices.ui.panels.MessageHandlerPanel;
import org.netbeans.modules.websvc.core.wseditor.support.WSEditAttributesAction;
import org.netbeans.modules.websvc.jaxws.api.JaxWsRefreshCookie;
import org.netbeans.modules.websvc.jaxws.api.JaxWsTesterCookie;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.cookies.OpenCookie;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint;
import org.netbeans.modules.websvc.api.jaxws.project.config.EndpointsProvider;
import org.netbeans.modules.websvc.core.WsWsdlCookie;
import org.netbeans.modules.websvc.core.wseditor.support.EditWSAttributesCookieImpl;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.nodes.Node;

public class JaxWsNode extends AbstractNode implements WsWsdlCookie, JaxWsTesterCookie, JaxWsRefreshCookie,
        ConfigureHandlerCookie{
    Service service;
    FileObject srcRoot;
    JaxWsModel jaxWsModel;
    private FileObject implBeanClass;
    InstanceContent content;
    Project project;
    
    public JaxWsNode(JaxWsModel jaxWsModel, Service service, FileObject srcRoot, FileObject implBeanClass) {
        this(jaxWsModel, service, srcRoot, implBeanClass, new InstanceContent());
    }
    
    private JaxWsNode(JaxWsModel jaxWsModel, Service service, FileObject srcRoot, FileObject implBeanClass, InstanceContent content) {
        super(new JaxWsChildren(service,srcRoot, implBeanClass),new AbstractLookup(content));
        this.jaxWsModel=jaxWsModel;
        this.service=service;
        this.srcRoot=srcRoot;
        this.content = content;
        this.implBeanClass=implBeanClass;
        if(implBeanClass.getAttribute("jax-ws-service")==null) {
            try {
                implBeanClass.setAttribute("jax-ws-service", java.lang.Boolean.TRUE);
                getDataObject().setValid(false);
            } catch (PropertyVetoException ex) {
            } catch (IOException ex) {
            }
        }
        setName(service.getName());
        content.add(this);
        content.add(service);
        content.add(implBeanClass);
        content.add(new EditWSAttributesCookieImpl(this, jaxWsModel));
        OpenCookie cookie = new OpenCookie() {
            public void open() {
                OpenCookie oc = getOpenCookie();
                if (oc != null) {
                    oc.open();
                }
            }
        };
        content.add(cookie);
        project = FileOwnerQuery.getOwner(srcRoot);
    }
    
    public String getDisplayName() {
        if (service.getWsdlUrl()!=null)
            return NbBundle.getMessage(JaxWsNode.class,"LBL_serviceNodeName",service.getServiceName(),service.getPortName());
        else
            return service.getName();
    }
    
    public String getShortDescription() {
        return getWsdlURL();
    }
    
    private static final java.awt.Image WAITING_BADGE =
            org.openide.util.Utilities.loadImage( "org/netbeans/modules/websvc/core/webservices/ui/resources/waiting.png"); // NOI18N
    private static final java.awt.Image ERROR_BADGE =
            org.openide.util.Utilities.loadImage( "org/netbeans/modules/websvc/core/webservices/ui/resources/error-badge.gif" ); //NOI18N
    private static final java.awt.Image SERVICE_BADGE =
            org.openide.util.Utilities.loadImage( "org/netbeans/modules/websvc/core/webservices/ui/resources/XMLServiceDataIcon.gif" ); //NOI18N
    
    public java.awt.Image getIcon(int type) {
        WsdlModeler wsdlModeler = ((JaxWsChildren)getChildren()).getWsdlModeler();
        if (wsdlModeler==null) return SERVICE_BADGE;
        else if (wsdlModeler.getCreationException()==null) {
            if (((JaxWsChildren)getChildren()).isModelGenerationFinished())
                return SERVICE_BADGE;
            else
                return org.openide.util.Utilities.mergeImages(SERVICE_BADGE, WAITING_BADGE, 15, 8);
        } else {
            Image dirtyNodeImage = org.openide.util.Utilities.mergeImages(SERVICE_BADGE, ERROR_BADGE, 6, 6);
            if (((JaxWsChildren)getChildren()).isModelGenerationFinished())
                return dirtyNodeImage;
            else
                return org.openide.util.Utilities.mergeImages(dirtyNodeImage, WAITING_BADGE, 15, 8);
        }
    }
    
    void changeIcon() {
        fireIconChange();
    }
    
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    private DataObject getDataObject() {
        FileObject f = getImplBean();
        if (f != null) {
            try {
                return  DataObject.find(f);
            } catch (DataObjectNotFoundException de) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, de.toString());
            }
        }
        return null;
    }
    
    private <T extends Node.Cookie> T getCookieFromImplBean(Class<T> type){
        T oc = null;
        FileObject f = getImplBean();
        if (f != null) {
            try {
                DataObject d = DataObject.find(f);
                oc = (T)d.getCookie(type);
            } catch (DataObjectNotFoundException de) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, de.toString());
            }
        }
        return oc;
    }
    
    private OpenCookie getOpenCookie() {
        OpenCookie oc = null;
        FileObject f = getImplBean();
        if (f != null) {
            try {
                DataObject d = DataObject.find(f);
                oc = (OpenCookie)d.getCookie(OpenCookie.class);
            } catch (DataObjectNotFoundException de) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, de.toString());
            }
        }
        return oc;
    }
    
    
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
            SystemAction.get(JaxWsRefreshAction.class),
            null,
            SystemAction.get(AddOperationAction.class),
            null,
            SystemAction.get(WsTesterPageAction.class),
            null,
            SystemAction.get(WSEditAttributesAction.class),
            null,
            SystemAction.get(ConfigureHandlerAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            null,
            SystemAction.get(PropertiesAction.class),
        };
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    // Handle deleting:
    public boolean canDestroy() {
        return true;
    }
    
    
    /**
     * get URL for Web Service WSDL file
     */
    public String getWebServiceURL() {
        J2eeModuleProvider provider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        InstanceProperties instanceProperties = provider.getInstanceProperties();
        if (instanceProperties==null) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(NbBundle.getMessage(JaxWsNode.class,"MSG_MissingServer"),NotifyDescriptor.ERROR_MESSAGE));
            return "";
        }
        // getting port
        String portNumber = instanceProperties.getProperty(InstanceProperties.HTTP_PORT_NUMBER);
        if(portNumber == null || portNumber.equals("")) {
            portNumber = "8080"; //NOI18N
        }
        
        // getting hostName
        String serverUrl = instanceProperties.getProperty(InstanceProperties.URL_ATTR);
        String hostName="localhost"; //NOI18N
        if (serverUrl!=null && serverUrl.indexOf("::")>0) { //NOI18N
            int index1 = serverUrl.indexOf("::"); //NOI18N
            int index2 = serverUrl.lastIndexOf(":"); //NOI18N
            if (index2>index1+2) hostName = serverUrl.substring(index1+2,index2);
        }
        
        String contextRoot = null;
        Object moduleType = provider.getJ2eeModule().getModuleType();
        // need to compute from annotations
        
        String wsURI=null;
        if (isJsr109Supported(project) && Util.isJavaEE5orHigher(project) || JaxWsUtils.isEjbJavaEE5orHigher(project) ) {
            try {
                wsURI = getServiceUri(moduleType);
            } catch (UnsupportedEncodingException ex) {
                // this shouldn't happen'
            }
        } else {
            try {
                wsURI = getNonJsr109Uri(moduleType);
            } catch (UnsupportedEncodingException ex) {
                // this shouldn't happen'
            }
        }
        if(J2eeModule.WAR.equals(moduleType)) {
            J2eeModuleProvider.ConfigSupport configSupport = provider.getConfigSupport();
            try {
                contextRoot = configSupport.getWebContextRoot();
            } catch (ConfigurationException e) {
                // TODO the context root value could not be read, let the user know about it
            }
            if(contextRoot != null && contextRoot.startsWith("/")) { //NOI18N
                contextRoot = contextRoot.substring(1);
            }
        }/* else if(J2eeModule.EJB.equals(moduleType)) {
            contextRoot = "webservice";//NO18N for now, we need to find the real value (see bug...57034 and 52265)
        }*/
        
        return "http://"+hostName+":" + portNumber +"/" + (contextRoot != null && !contextRoot.equals("") ? contextRoot + "/" : "") + wsURI; //NOI18N
    }
    
    private String getNonJsr109Uri(Object moduleType) throws UnsupportedEncodingException {
        JAXWSSupport support = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        if (J2eeModule.WAR.equals(moduleType)) {
            WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
            FileObject webInfFo = webModule.getWebInf();
            if (webInfFo!=null) {
                FileObject sunJaxwsFo = webInfFo.getFileObject("sun-jaxws","xml"); //NOI18N
                if (sunJaxwsFo!=null) {
                    try {
                        Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunJaxwsFo);
                        if (endpoints!=null) {
                            String urlPattern = findUrlPattern(endpoints, service.getImplementationClass());
                            if (urlPattern!=null) return URLEncoder.encode(urlPattern, "UTF-8"); //NOI18N
                        }
                    } catch (IOException ex) {
                        ErrorManager.getDefault().log(ex.getLocalizedMessage());
                    }
                }
            }
        }
        return URLEncoder.encode(getNameFromPackageName(service.getImplementationClass()),"UTF-8"); //NOI18N
    }
    
    private String findUrlPattern(Endpoints endpoints, String implementationClass) {
        Endpoint[] endp = endpoints.getEndpoints();
        for (int i=0;i<endp.length;i++) {
            if (implementationClass.equals(endp[i].getImplementation())) {
                String urlPattern = endp[i].getUrlPattern();
                if (urlPattern!=null) {
                    return urlPattern.startsWith("/")?urlPattern.substring(1):urlPattern; //NOI18N
                }
            }
        }
        return null;
    }
    
    private String getServiceUri(final Object moduleType) throws UnsupportedEncodingException {
        final String[] serviceName=new String[1];
        final String[] name=new String[1];
        final boolean[] isProvider = {false};
        JavaSource javaSource = getImplBeanJavaSource();
        if (javaSource!=null) {
            CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    SourceUtils srcUtils = SourceUtils.newInstance(controller);
                    TypeElement wsElement = controller.getElements().getTypeElement("javax.jws.WebService"); //NOI18N
                    if (srcUtils!=null && wsElement!=null) {
                        boolean foundWsAnnotation = resolveServiceUrl(  moduleType,
                                controller,
                                srcUtils,
                                wsElement,
                                serviceName,
                                name);
                        if (!foundWsAnnotation) {
                            TypeElement wsProviderElement = controller.getElements().getTypeElement("javax.xml.ws.WebServiceProvider"); //NOI18N
                            List<? extends AnnotationMirror> annotations = srcUtils.getTypeElement().getAnnotationMirrors();
                            for (AnnotationMirror anMirror : annotations) {
                                if (controller.getTypes().isSameType(wsProviderElement.asType(), anMirror.getAnnotationType())) {
                                    isProvider[0] = true;
                                }
                            }
                        }
                    }
                }
                
                public void cancel() {}
            };
            try {
                javaSource.runUserActionTask(task, true);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        
        String qualifiedImplClassName = service.getImplementationClass();
        String implClassName = getNameFromPackageName(qualifiedImplClassName);
        if (serviceName[0]==null) {
            serviceName[0]=URLEncoder.encode(implClassName+"Service","UTF-8"); //NOI18N
        }
        if (J2eeModule.WAR.equals(moduleType)) {
            return serviceName[0];
        } else if (J2eeModule.EJB.equals(moduleType)) {
            if (name[0]==null){
                if(isProvider[0]){
                    //per JSR 109, use qualified impl class name for EJB
                    name[0]=qualifiedImplClassName;
                } else{
                    name[0]=implClassName;
                }
                name[0] = URLEncoder.encode(name[0],"UTF-8"); //NOI18N
            }
            return serviceName[0]+"/"+ name[0];
        } else
            return serviceName[0];
    }
    
    private boolean resolveServiceUrl(  Object moduleType,
            CompilationController controller,
            SourceUtils srcUtils,
            TypeElement wsElement,
            String[] serviceName,
            String[] name) throws IOException {
        boolean foundWsAnnotation = false;
        List<? extends AnnotationMirror> annotations = srcUtils.getTypeElement().getAnnotationMirrors();
        for (AnnotationMirror anMirror : annotations) {
            if (controller.getTypes().isSameType(wsElement.asType(), anMirror.getAnnotationType())) {
                foundWsAnnotation=true;
                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                for(ExecutableElement ex:expressions.keySet()) {
                    if (ex.getSimpleName().contentEquals("serviceName")) {
                        serviceName[0] = (String)expressions.get(ex).getValue();
                        if (serviceName[0]!=null) serviceName[0] = URLEncoder.encode(serviceName[0],"UTF-8"); //NOI18N
                    } else if (ex.getSimpleName().contentEquals("name")) {
                        name[0] = (String)expressions.get(ex).getValue();
                        if (name[0]!=null) name[0] = URLEncoder.encode(name[0],"UTF-8");
                    }
                    if (serviceName[0]!=null && name[0]!=null) break;
                }
                break;
            } // end if
        } // end for
        return foundWsAnnotation;
    }
    
    private String getNameFromPackageName(String packageName) {
        int index = packageName.lastIndexOf("."); //NOI18N
        return index>=0?packageName.substring(index+1):packageName;
    }
    
    public String getWsdlURL() {
        String wsdlUrl = getWebServiceURL();
        return wsdlUrl.length()==0?"":wsdlUrl+"?wsdl"; //NOI18N
    }
    /**
     * get URL for Web Service Tester Page
     */
    public String getTesterPageURL() {
        if (isJsr109Supported(project) && (Util.isJavaEE5orHigher(project) || JaxWsUtils.isEjbJavaEE5orHigher(project))) {
            return getWebServiceURL()+"?Tester"; //NOI18N
        } else {
            return getWebServiceURL(); //NOI18N
        }
        
    }
    
    /**
     * refresh service information obtained from wsdl (when wsdl file was changed)
     */
    public void refreshService(boolean downloadWsdl) {
        if(downloadWsdl){
            int result = RefreshWsDialog.open(downloadWsdl, service.getImplementationClass(), service.getWsdlUrl());
            if (RefreshWsDialog.CLOSE==result) return;
            if (RefreshWsDialog.DO_ALL==result)
                ((JaxWsChildren)getChildren()).refreshKeys(true, true);
            else if (RefreshWsDialog.DOWNLOAD_WSDL==result)
                ((JaxWsChildren)getChildren()).refreshKeys(true, false);
            else if (RefreshWsDialog.REGENERATE_IMPL_CLASS==result)
                ((JaxWsChildren)getChildren()).refreshKeys(false, true);
            else
                ((JaxWsChildren)getChildren()).refreshKeys(false, false);
        } else{
            int result = RefreshWsDialog.openWithOKButtonOnly(downloadWsdl, service.getImplementationClass(),
                    service.getWsdlUrl());
            if(RefreshWsDialog.REGENERATE_IMPL_CLASS==result){
                ((JaxWsChildren)getChildren()).refreshKeys(false, true);
            } else{
                ((JaxWsChildren)getChildren()).refreshKeys(false, false);
            }
        }
    }
    
    public void destroy() throws java.io.IOException {
        String serviceName = service.getName();
        NotifyDescriptor.Confirmation notifyDesc =
                new NotifyDescriptor.Confirmation(NbBundle.getMessage(JaxWsNode.class, "MSG_CONFIRM_DELETE", serviceName));
        DialogDisplayer.getDefault().notify(notifyDesc);
        if(notifyDesc.getValue() == NotifyDescriptor.YES_OPTION) {
            JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
            if (wss!=null) {
                FileObject localWsdlFolder = wss.getLocalWsdlFolderForService(serviceName,false);
                if (localWsdlFolder!=null) {
                    // removing local wsdl and xml artifacts
                    FileLock lock=null;
                    FileObject clientArtifactsFolder = localWsdlFolder.getParent();
                    try {
                        lock = clientArtifactsFolder.lock();
                        clientArtifactsFolder.delete(lock);
                    } finally {
                        if (lock!=null) lock.releaseLock();
                    }
                    // removing wsdl and xml artifacts from WEB-INF/wsdl
                    FileObject wsdlFolder = wss.getWsdlFolder(false);
                    if (wsdlFolder!=null) {
                        FileObject serviceWsdlFolder = wsdlFolder.getFileObject(serviceName);
                        if (serviceWsdlFolder!=null) {
                            try {
                                lock = serviceWsdlFolder.lock();
                                serviceWsdlFolder.delete(lock);
                            } finally {
                                if (lock!=null) lock.releaseLock();
                            }
                        }
                    }
                    // cleaning java artifacts
                    Project project = FileOwnerQuery.getOwner(srcRoot);
                    FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
                    try {
                        ExecutorTask wsimportTask =
                                ActionUtils.runTarget(buildImplFo,
                                new String[]{"wsimport-service-clean-"+serviceName},null); //NOI18N
                        wsimportTask.waitFinished();
                    } catch (java.io.IOException ex) {
                        ErrorManager.getDefault().log(ex.getLocalizedMessage());
                    } catch (IllegalArgumentException ex) {
                        ErrorManager.getDefault().log(ex.getLocalizedMessage());
                    }
                }
                
                // removing service from jax-ws.xml
                wss.removeService(serviceName);
                
                // remove non JSR109 entries
                Boolean isJsr109 = jaxWsModel.getJsr109();
                if(isJsr109!=null && !isJsr109.booleanValue()){
                    if(service.getWsdlUrl() != null){ //if coming from wsdl
                        serviceName = service.getServiceName();
                    }
                    wss.removeNonJsr109Entries(serviceName);
                }
                super.destroy();
            }
        }
    }
    
    private FileObject getImplBean() {
        String implBean = service.getImplementationClass();
        if(implBean != null) {
            return srcRoot.getFileObject(implBean.replace('.','/')+".java");
        }
        return null;
    }
    
    private JavaSource getImplBeanJavaSource() {
        FileObject implBean = getImplBean();
        if(implBean != null) {
            return JavaSource.forFileObject(implBean);
        }
        return null;
    }
    
    /**
     * Adds possibility to display custom delete dialog
     */
    
    public Object getValue(String attributeName) {
        Object retValue;
        if (attributeName.equals("customDelete")) { //NOI18N
            retValue = Boolean.TRUE;
        } else {
            retValue = super.getValue(attributeName);
        }
        return retValue;
    }
    
    /**
     * Implementation of the ConfigureHandlerCookie
     */
    public void configureHandler() {
        FileObject implBeanFo = getImplBean();
        List<String> handlerClasses = new ArrayList<String>();
        FileObject handlerFO = null;
        HandlerChains handlerChains = null;
        //obtain the handler config file, if any from annotation in implbean
        final String[] handlerFileName = new String[1];
        final boolean[] isNew = new boolean[]{true};
        JavaSource implBeanJavaSrc = JavaSource.forFileObject(implBeanFo);
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                SourceUtils srcUtils = SourceUtils.newInstance(controller);
                AnnotationMirror handlerAnnotation = getAnnotation(controller, srcUtils, "javax.jws.HandlerChain");
                if (handlerAnnotation!=null) {
                    isNew[0] = false;
                    Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = handlerAnnotation.getElementValues();
                    for(ExecutableElement ex:expressions.keySet()) {
                        if (ex.getSimpleName().contentEquals("file")) {
                            handlerFileName[0] = (String)expressions.get(ex).getValue();
                            break;
                        }
                    }
                }
            }
            public void cancel() {}
        };
        try {
            implBeanJavaSrc.runUserActionTask(task, true);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        if (!isNew[0] && handlerFileName[0]!=null) {
            // look for handlerFile in the same directory as the implbean
            handlerFO = implBeanFo.getParent().getFileObject(handlerFileName[0]);
            if (handlerFO!=null) {
                try{
                    handlerChains =
                            HandlerChainsProvider.getDefault().getHandlerChains(handlerFO);
                }catch(Exception e){
                    ErrorManager.getDefault().notify(e);
                    return; //TODO handle this
                }
                HandlerChain[] handlerChainArray = handlerChains.getHandlerChains();
                //there is always only one, so get the first one
                HandlerChain chain = handlerChainArray[0];
                Handler[] handlers = chain.getHandlers();
                for(int i = 0; i < handlers.length; i++){
                    handlerClasses.add(handlers[i].getHandlerClass());
                }
            } else {  //unable to find the handler file, display a warning
                NotifyDescriptor.Message dialogDesc
                        = new NotifyDescriptor.Message(NbBundle.getMessage(JaxWsNode.class,
                        "MSG_HANDLER_FILE_NOT_FOUND", handlerFileName), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(dialogDesc);
            }
        }
        final MessageHandlerPanel panel = new MessageHandlerPanel(project,
                handlerClasses, true, service.getName());
        String title = NbBundle.getMessage(JaxWsNode.class,"TTL_MessageHandlerPanel");
        DialogDescriptor dialogDesc = new DialogDescriptor(panel, title);
        dialogDesc.setButtonListener(new HandlerButtonListener( panel,
                handlerChains, handlerFO, implBeanFo, service, isNew[0]));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
        dialog.setVisible(true);
    }
    
    static AnnotationMirror getAnnotation(CompilationController controller, SourceUtils srcUtils, String annotationType) {
        TypeElement anElement = controller.getElements().getTypeElement(annotationType);
        if (anElement!=null) {
            List<? extends AnnotationMirror> annotations = srcUtils.getTypeElement().getAnnotationMirrors();
            for(AnnotationMirror annotation : annotations) {
                if (controller.getTypes().isSameType(anElement.asType(), annotation.getAnnotationType())) {
                    return annotation;
                }
            }
        }
        return null;
    }
    
    private boolean isJsr109Supported(Project project) {
        JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        if (wss != null) {
            Map properties = wss.getAntProjectHelper().getStandardPropertyEvaluator().getProperties();
            String serverInstance = (String)properties.get("j2ee.server.instance"); //NOI18N
            if (serverInstance != null) {
                J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstance);
                if (j2eePlatform != null) {
                    return j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109);
                }
            }
        }
        return false;
    }
    
    void refreshImplClass() {
        if (implBeanClass != null) {
            content.remove(implBeanClass);
        }
        implBeanClass = getImplBean();
        content.add(implBeanClass);
    }
    
    public boolean canCopy() {
        return true;
    }
    
    public boolean canCut() {
        return true;
    }
    
    public Transferable clipboardCopy() throws IOException {
        URL url = new URL(getWsdlURL());
        boolean connectionOK=false;
        try {
            URLConnection connection = url.openConnection();
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection)connection;
                try {
                    httpConnection.setRequestMethod("GET"); //NOI18N
                    httpConnection.connect();
                    if (HttpURLConnection.HTTP_OK == httpConnection.getResponseCode())
                        connectionOK=true;
                } catch (java.net.ConnectException ex) {
                    //TODO: throw exception here?
                    url = null;
                } finally {
                    if (httpConnection!=null)
                        httpConnection.disconnect();
                }
                if(!connectionOK){
                    //TODO: throw exception here?
                    url = null;
                }
            }
        } catch (IOException ex) {
            //TODO: throw exception here?
            url = null;
        }
        
        return new WebServiceTransferable(new WebServiceReference(url,
                service.getWsdlUrl() != null ? service.getServiceName() :service.getName(), project.getProjectDirectory().getName()));
    }
}
