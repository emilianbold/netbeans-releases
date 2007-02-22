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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.wizard;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
//import org.netbeans.jmi.javamodel.Annotation;
//import org.netbeans.jmi.javamodel.AttributeValue;
//import org.netbeans.jmi.javamodel.ClassDefinition;
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.jmi.javamodel.Method;
//import org.netbeans.jmi.javamodel.Parameter;
//import org.netbeans.jmi.javamodel.PrimitiveType;
//import org.netbeans.jmi.javamodel.PrimitiveTypeKind;
//import org.netbeans.jmi.javamodel.PrimitiveTypeKindEnum;
//import org.netbeans.jmi.javamodel.Type;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
//import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsitconf.util.JMIUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class STSWizardCreator {

    protected static final int JSE_PROJECT_TYPE = 0;
    protected static final int WEB_PROJECT_TYPE = 1;
    protected static final int EJB_PROJECT_TYPE = 2;

    private int projectType;

    private Project project;
    private WizardDescriptor wiz;

    public boolean jwsdpSupported, wsitSupported, jsr109Supported, jsr109oldSupported;

    public STSWizardCreator(Project project, WizardDescriptor wiz) {
        this.project = project;
        this.wiz = wiz;
    }
    
    public STSWizardCreator(Project project) {
        this.project = project;
    }
    
    public void createSTS() {
        final ProgressHandle handle = ProgressHandleFactory.createHandle( 
                NbBundle.getMessage(STSWizardCreator.class, "TXT_WebServiceGeneration")); //NOI18N

        initProjectInfo(project);
        
        Runnable r = new Runnable() {
            public void run() {
                try {
                    handle.start(100);
                    generateWsFromWsdl15(handle);
                } catch (Exception e) {
                    //finish progress bar
                    handle.finish();
                    String message = e.getLocalizedMessage();
                    if(message != null) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    } else {
                        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                    }
                }
            }
        };
        RequestProcessor.getDefault().post(r);
    }
    
    private void initProjectInfo(Project project) {
        JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        if (wss != null) {
            Map properties = wss.getAntProjectHelper().getStandardPropertyEvaluator().getProperties();
            String serverInstance = (String)properties.get("j2ee.server.instance"); //NOI18N
            if (serverInstance != null) {
                J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstance);
                if (j2eePlatform != null) {
                    jwsdpSupported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_JWSDP);
                    wsitSupported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIT);
                    jsr109Supported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109);
                    jsr109oldSupported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE);
                }
            }
        }
        
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        EjbJar em = EjbJar.getEjbJar(project.getProjectDirectory());
        if (em != null)
            projectType = EJB_PROJECT_TYPE;
        else if (wm != null)
            projectType = WEB_PROJECT_TYPE;
        else
            projectType = JSE_PROJECT_TYPE;
    }
    
    private void generateWsFromWsdl15(final ProgressHandle handle) throws Exception {
        String wsdlFilePath = (String) wiz.getProperty(WizardProperties.WSDL_FILE_PATH);
        File normalizedWsdlFilePath = FileUtil.normalizeFile(new File(wsdlFilePath));
        //convert to URI first to take care of spaces
        final URL wsdlURL = normalizedWsdlFilePath.toURI().toURL();
        final WsdlService service = (WsdlService) wiz.getProperty(WizardProperties.WSDL_SERVICE);
        if (service==null) {
//            JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
//            FileObject targetFolder = Templates.getTargetFolder(wiz);
//            String targetName = Templates.getTargetName(wiz);
//            WsdlServiceHandler handler = (WsdlServiceHandler)wiz.getProperty(WizardProperties.WSDL_SERVICE_HANDLER);
//            JaxWsUtils.generateJaxWsArtifacts(project,targetFolder,targetName,wsdlURL,handler.getServiceName(),handler.getPortName());
//            WsdlModeler wsdlModeler = (WsdlModeler) wiz.getProperty(WizardProperties.WSDL_MODELER);
//            if (wsdlModeler!=null && wsdlModeler.getCreationException()!=null) {
//                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
//                            NbBundle.getMessage(WebServiceCreator.class,"TXT_CannotGenerateArtifacts",
//                                                wsdlModeler.getCreationException().getLocalizedMessage()),
//                            NotifyDescriptor.ERROR_MESSAGE)
//                    );
//            }
            handle.finish();
            return;
        } else {
            final WsdlPort port = (WsdlPort) wiz.getProperty(WizardProperties.WSDL_PORT);
            //String portJavaName = port.getJavaName();   
            WsdlModeler wsdlModeler = (WsdlModeler) wiz.getProperty(WizardProperties.WSDL_MODELER);
            // don't set the packageName for modeler (use the default one generated from target Namespace)
            wsdlModeler.generateWsdlModel(new WsdlModelListener() {
                public void modelCreated(WsdlModel model) {
                    WsdlService service1 = model.getServiceByName(service.getName());
                    WsdlPort port1 = service1.getPortByName(port.getName());
                    port1.setSOAPVersion(port.getSOAPVersion());
                    FileObject targetFolder = Templates.getTargetFolder(wiz);
                    String targetName = Templates.getTargetName(wiz);
                    try {
//                        generateJaxWsImplClass(project, targetFolder, targetName, wsdlURL, service1, port1, true, null);
                        generateProviderImplClass(project, targetFolder, targetName, service1, port1, wsdlURL);
                        handle.finish();
                    } catch (Exception ex) {
                        handle.finish();
                        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                    }
                }
            });
        }
    }
    
    public void generateProviderImplClass(Project project, FileObject targetFolder,
            String targetName, WsdlService service, WsdlPort port, URL wsdlURL) throws Exception{
//        initProjectInfo(project);
        
        String serviceID = service.getName();
        
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
//        JavaClass javaClass = null;
//        JavaModel.getJavaRepository().beginTrans(true);
//        
//        try {
//
//            javaClass = JMIUtils.createClass(targetFolder, targetName);
//            //Initially, Provider<Source> will be implemented. The user can then change the Provider type if he/she wishes
//            JMIUtils.addInterface(javaClass, "javax.xml.ws.Provider<Source>"); //NOI18N 
//
//            FileObject fo = javaClass == null ? null : JavaModel.getFileObject(javaClass.getResource());
//            ClassPath classPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//            String serviceImplPath = classPath.getResourceName(fo, '.', false);
//            String portJavaName = port.getJavaName();
//            String artifactsPckg = portJavaName.substring(0, portJavaName.lastIndexOf("."));
//
//            serviceID = jaxWsSupport.addService(targetName, serviceImplPath, wsdlURL.toString(), service.getName(), port.getName(), artifactsPckg, jsr109Supported && isJavaEE5orHigher(project));
//            String wsdlLocation = jaxWsSupport.getWsdlLocation(serviceID);
//            
//            if (projectType == EJB_PROJECT_TYPE) {//EJB project
//                Annotation statelessAnnotation = JMIUtils.createAnnotation(javaClass, "javax.ejb.Stateless", Collections.EMPTY_LIST); //NOI18N
//                javaClass.getAnnotations().add(statelessAnnotation);
//            }
//            
//            //Initially, set mode to PAYLOAD. The user can then change if he/she wishes
//            AttributeValue serviceModeValue = JMIUtils.createAttributeValue(javaClass, "value", "javax.xml.ws.Service.Mode", "PAYLOAD"); //NOI18N
//            ArrayList attrList = new ArrayList();
//            attrList.add(serviceModeValue);
//            Annotation serviceModeAnnotation = JMIUtils.createAnnotation(javaClass, "javax.xml.ws.ServiceMode", attrList); //NOI18N
//            javaClass.getAnnotations().add(serviceModeAnnotation);
//            
//            AttributeValue wsdlLocationValue = JMIUtils.createAttributeValue(javaClass, "wsdlLocation", wsdlLocation );
//            AttributeValue serviceNameAttibuteValue = JMIUtils.createAttributeValue(javaClass, "serviceName", service.getName()); //NOI18N
//            AttributeValue targetNamespaceAttibuteValue = JMIUtils.createAttributeValue(javaClass, "targetNamespace", port.getNamespaceURI()); //NOI18N
//            AttributeValue portNameAttibuteValue = JMIUtils.createAttributeValue(javaClass, "portName", port.getName()); //NOI18N
//            attrList = new ArrayList();
//            attrList.add(wsdlLocationValue);
//            attrList.add(serviceNameAttibuteValue);
//            attrList.add(targetNamespaceAttibuteValue);
//            attrList.add(portNameAttibuteValue);
//            Annotation serviceProviderAnnotation = JMIUtils.createAnnotation(javaClass, "javax.xml.ws.WebServiceProvider", attrList); //NOI18N
//            javaClass.getAnnotations().add(serviceProviderAnnotation);
//            String returnType = "javax.xml.transform.Source";  //NOI18N
//            String operationName = "invoke";   //NOI18N
//            Method op = JMIUtils.createMethod(javaClass, operationName, Modifier.PUBLIC, returnType);
//            Parameter param = JMIUtils.createParameter(javaClass, "source", returnType);  //NOI18N
//            op.getParameters().add(param);
//            op.setBodyText("//TODO implement this method\nreturn null;");  //NOI18N
//            javaClass.getFeatures().add(op);
//        }finally{
//            JavaModel.getJavaRepository().endTrans();
//        }
//        
//        FileObject fo = JavaModel.getFileObject(javaClass.getResource());
//        //open in the editor
//        DataObject dobj = DataObject.find(fo);
//        openFileInEditor(dobj);
    }

    private static void openFileInEditor(DataObject dobj){
        final EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
        RequestProcessor.getDefault().post(new Runnable(){
            public void run(){
                ec.open();
            }
        }, 1000);
    }
    
//    private static String createBody(Type type) {
//        String initVal;
//        
//        if (type instanceof PrimitiveType) {
//            PrimitiveTypeKind primitiveType = ((PrimitiveType) type).getKind();
//            if (PrimitiveTypeKindEnum.BOOLEAN.equals(primitiveType)) {
//                initVal = "false"; // NOI18N
//            } else if (PrimitiveTypeKindEnum.CHAR.equals(primitiveType)) {
//                initVal = "'\\0'"; // NOI18N
//            } else if (PrimitiveTypeKindEnum.VOID.equals(primitiveType)) {
//                return "throw new UnsupportedOperationException(\"Not yet implemented\");"; // NOI18N
//            } else {
//                initVal = "0"; // NOI18N
//            }
//        } else if (type instanceof ClassDefinition) {
//            initVal = "null"; // NOI18N
//        } else {
//            throw new IllegalArgumentException("Type "+type.getClass()); // NOI18N
//        }
//        return "return ".concat(initVal).concat(";"); // NOI18N
//    }

    /**
     * Is J2EE version of a given project JavaEE 5 or higher?
     *
     * @param project J2EE project
     * @return true if J2EE version is JavaEE 5 or higher; otherwise false
     */
    public static boolean isJavaEE5orHigher(Project project) {
        if (project == null) {
            return false;
        }
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            J2eeModule j2eeModule = j2eeModuleProvider.getJ2eeModule();
            if (j2eeModule != null) {
                Object type = j2eeModule.getModuleType();
                double version = Double.parseDouble(j2eeModule.getModuleVersion());
                if (J2eeModule.EJB.equals(type) && (version > 2.1)) {
                    return true;
                };
                if (J2eeModule.WAR.equals(type) && (version > 2.4)) {
                    return true;
                }
                if (J2eeModule.CLIENT.equals(type) && (version > 1.4)) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
