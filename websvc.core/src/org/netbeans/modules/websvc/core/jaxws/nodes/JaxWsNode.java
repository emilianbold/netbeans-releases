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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.AnnotableElement;
import org.netbeans.jmi.javamodel.Annotation;
import org.netbeans.jmi.javamodel.AttributeValue;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.StringLiteral;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.jaxws.project.GeneratedFilesHelper;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.core.WebServiceReference;
import org.netbeans.modules.websvc.core.WebServiceTransferable;
import org.netbeans.modules.websvc.core.jaxws.actions.AddOperationAction;
import org.netbeans.modules.websvc.core.jaxws.actions.JaxWsRefreshAction;
import org.netbeans.modules.websvc.core.jaxws.actions.WsTesterPageAction;
import org.netbeans.modules.websvc.core.webservices.action.ConfigureHandlerAction;
import org.netbeans.modules.websvc.core.webservices.action.ConfigureHandlerCookie;
import org.netbeans.modules.websvc.core.webservices.ui.panels.MessageHandlerPanel;
import org.netbeans.modules.websvc.core.wseditor.support.EditWSAttributesCookie;
import org.netbeans.modules.websvc.core.wseditor.support.WSEditAttributesAction;
import org.netbeans.modules.websvc.jaxws.api.JaxWsRefreshCookie;
import org.netbeans.modules.websvc.jaxws.api.JaxWsTesterCookie;
import org.netbeans.modules.websvc.jaxws.api.JaxWsWsdlCookie;
import org.netbeans.modules.websvc.api.jaxws.project.config.Handler;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChain;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChains;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChainsProvider;
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
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoints;
import org.netbeans.modules.websvc.api.jaxws.project.config.EndpointsProvider;
import org.netbeans.modules.websvc.core.wseditor.support.EditWSAttributesCookieImpl;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.nodes.Node;

public class JaxWsNode extends AbstractNode implements OpenCookie, JaxWsWsdlCookie, JaxWsTesterCookie, JaxWsRefreshCookie,
        ConfigureHandlerCookie{
    Service service;
    FileObject srcRoot;
    JaxWsModel jaxWsModel;
    JavaClass implBeanClass;
    InstanceContent content;
    Project project;
    
    public JaxWsNode(JaxWsModel jaxWsModel, Service service, FileObject srcRoot) {
        this(jaxWsModel, service, srcRoot, new InstanceContent());
    }
    
    private JaxWsNode(JaxWsModel jaxWsModel, Service service, FileObject srcRoot, InstanceContent content) {
        super(new JaxWsChildren(service,srcRoot),new AbstractLookup(content));
        this.jaxWsModel=jaxWsModel;
        this.service=service;
        this.srcRoot=srcRoot;
        this.content = content;
        setName(service.getName());
        content.add(this);
        content.add(service);
        content.add(srcRoot);
        addImplClassToContent(content);
        project = FileOwnerQuery.getOwner(srcRoot);
    }
    
    public Node.Cookie getCookie(Class type){
        if(type == EditWSAttributesCookie.class){
            return new EditWSAttributesCookieImpl(this, jaxWsModel);
        }
        return super.getCookie(type);
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
    
    /*
    public Image getIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/websvc/core/webservices/ui/resources/XMLServiceDataIcon.gif"); //NOI18N
    }
     */
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    public void open() {
        OpenCookie oc = getOpenCookie();
        if (oc != null) {
            oc.open();
        }
    }
    
    private OpenCookie getOpenCookie() {
        OpenCookie oc = null;
        JavaClass ce = getImplBeanClass();
        if (ce != null) {
            FileObject f = JavaModel.getFileObject(ce.getResource());
            if (f != null) {
                try {
                    DataObject d = DataObject.find(f);
                    oc = (OpenCookie)d.getCookie(OpenCookie.class);
                } catch (DataObjectNotFoundException de) {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, de.toString());
                }
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
        if ((isJsr109Supported(project) && Util.isJavaEE5orHigher(project))) {
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
            contextRoot = configSupport.getWebContextRoot();
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
    
    private String getServiceUri(Object moduleType) throws UnsupportedEncodingException {
        String serviceName=null;
        boolean isProvider = false;
        String name=null;
        JavaClass javaClass = getImplBeanClass();
        if (javaClass!=null) {
            List/*Annotation*/ annotations = javaClass.getAnnotations();
            if (annotations!=null) {
                for (int i=0;i<annotations.size();i++) {
                    Annotation an = (Annotation)annotations.get(i);
                    if ("javax.jws.WebService".equals(an.getType().getName()) ||
                            "javax.xml.ws.WebServiceProvider".equals(an.getType().getName())) { //NOI18N
                        List/*AttributeValue*/ attrs = an.getAttributeValues();
                        for (int j=0;j<attrs.size();j++) {
                            AttributeValue attr = (AttributeValue)attrs.get(j);
                            if ("serviceName".equals(attr.getName())) { //NOI18N
                                serviceName = ((StringLiteral)attr.getValue()).getValue();
                            } else if ("name".equals(attr.getName())) { //NOI18N
                                name = ((StringLiteral)attr.getValue()).getValue();
                            }
                            if (serviceName!=null) {
                                if (J2eeModule.WAR.equals(moduleType)) {
                                    return URLEncoder.encode(serviceName,"UTF-8"); //NOI18N
                                } else if (name!=null) {
                                    return URLEncoder.encode(serviceName,"UTF-8")+"/"+URLEncoder.encode(name,"UTF-8"); //NOI18N
                                }
                            }
                        }
                        if("javax.xml.ws.WebServiceProvider".equals(an.getType().getName())){ //NOI18N
                            isProvider = true;
                        }
                    }
                }
            }
        }
        String qualifiedImplClassName = service.getImplementationClass();
        String implClassName = getNameFromPackageName(qualifiedImplClassName);
        if (serviceName==null) serviceName=implClassName+"Service"; //NOI18N
        if (J2eeModule.WAR.equals(moduleType)) {
            return URLEncoder.encode(serviceName,"UTF-8");
        } else if (J2eeModule.EJB.equals(moduleType)) {
            if (name==null){
                if(isProvider){
                    //per JSR 109, use qualified impl class name for EJB
                    name=qualifiedImplClassName;
                } else{
                    name=implClassName;
                }
            }
            return URLEncoder.encode(serviceName,"UTF-8")+"/"+URLEncoder.encode(name,"UTF-8"); //NOI18N
        } else return URLEncoder.encode(serviceName,"UTF-8");
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
        if (isJsr109Supported(project) && (Util.isJavaEE5orHigher(project))) {
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
    
    private JavaClass getImplBeanClass() {
        String implBean = service.getImplementationClass();
        if(implBean != null) {
            //JavaClass javaClass = (JavaClass)JavaModel.getDefaultExtent().getType().resolve(implBean);
            //return javaClass;
            return JMIUtils.findClass(implBean, srcRoot);
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
        boolean isNew = false;
        implBeanClass = getImplBeanClass();
        ArrayList<String> handlerClasses = new ArrayList<String>();
        FileObject handlerFO = null;
        HandlerChains handlerChains = null;
        //obtain the handler config file, if any from annotation in implbean
        String handlerFileName = null;
        Annotation handlerAnnotation = getAnnotation(implBeanClass, "HandlerChain");
        
        if(handlerAnnotation != null){
            List<AttributeValue> attrs = handlerAnnotation.getAttributeValues();
            for(AttributeValue attr : attrs){
                String attrName = attr.getName();
                if(attrName.equals("file")){
                    StringLiteral fileValue = (StringLiteral)attr.getValue();
                    handlerFileName = fileValue.getValue();
                    break;
                }
            }
            //look for handlerFile in the same directory as the implbean
            FileObject f = JavaModel.getFileObject(implBeanClass.getResource());
            handlerFO = f.getParent().getFileObject(handlerFileName);
            if(handlerFO != null){
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
            } else{  //unable to find the handler file, display a warning
                NotifyDescriptor.Message dialogDesc
                        = new NotifyDescriptor.Message(NbBundle.getMessage(JaxWsNode.class,
                        "MSG_HANDLER_FILE_NOT_FOUND", handlerFileName), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(dialogDesc);
            }
            
        } else{
            isNew = true;
        }
        final MessageHandlerPanel panel = new MessageHandlerPanel(project,
                (String[])handlerClasses.toArray(new String[handlerClasses.size()]), true, service.getName());
        String title = NbBundle.getMessage(JaxWsNode.class,"TTL_MessageHandlerPanel");
        DialogDescriptor dialogDesc = new DialogDescriptor(panel, title);
        dialogDesc.setButtonListener(new HandlerButtonListener( panel,
                handlerChains, handlerFO, implBeanClass, service, isNew));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
        dialog.setVisible(true);
        
    }
    
    public static Annotation getAnnotation(AnnotableElement element, String annotationType) {
        Collection<Annotation> annotations = element.getAnnotations();
        for(Annotation annotation : annotations) {
            if(annotation.getType() == null) {
                continue;
            }
            String name = annotation.getType().getName();
            if (name.indexOf(annotationType) != -1) {
                return annotation;
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
    
    private void addImplClassToContent(final InstanceContent content) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                JavaMetamodel.getManager().waitScanFinished();
                implBeanClass = getImplBeanClass();
                if (implBeanClass != null) {
                    content.add(implBeanClass);
                }
            }
            
        });
    }
    
    void refreshImplClass() {
        if (implBeanClass != null) {
            content.remove(implBeanClass);
        }
        addImplClassToContent(content);
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
