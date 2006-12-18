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

package org.netbeans.modules.websvc.core.dev.wizard;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.core.ServiceCreator;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Radko, Milan Kuchtiak
 */
public class JaxWsServiceCreator implements ServiceCreator {
    private ProjectInfo projectInfo;
    private WizardDescriptor wiz;
    private boolean addJaxWsLib;
    private int serviceType;
    private int projectType;
    /**
     * Creates a new instance of WebServiceClientCreator
     */
    public JaxWsServiceCreator(ProjectInfo projectInfo, WizardDescriptor wiz, boolean addJaxWsLib) {
        this.projectInfo = projectInfo;
        this.wiz = wiz;
        this.addJaxWsLib=addJaxWsLib;
    }
        
    public void createService() throws IOException {
        serviceType = ((Integer) wiz.getProperty(WizardProperties.WEB_SERVICE_TYPE)).intValue();
        projectType = projectInfo.getProjectType();
        
        // Use Progress API to display generator messages.
        final ProgressHandle handle = ProgressHandleFactory.createHandle( NbBundle.getMessage(JaxWsServiceCreator.class, "TXT_WebServiceGeneration")); //NOI18N
        handle.start(100);
        
        Runnable r = new Runnable() {
            public void run() {
                try {
                    generateWebService(handle);
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
    
    public void createServiceFromWsdl() throws IOException {
        
        //initProjectInfo(project);
        
        final ProgressHandle handle = ProgressHandleFactory.createHandle( NbBundle.getMessage(JaxWsServiceCreator.class, "TXT_WebServiceGeneration")); //NOI18N
        
        Runnable r = new Runnable() {
            public void run() {
                try {
//                    if (Util.isJavaEE5orHigher(project) ||
//                            (!jsr109Supported && projectType == WEB_PROJECT_TYPE && !jsr109oldSupported)
//                            || (wsitSupported)
//                            || jwsdpSupported) {
                        handle.start();
                        generateWsFromWsdl15(handle);
//                    } else {
//                        handle.start(100);
//                        generateWsFromWsdl14(handle);
//                    }
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
    
    
    //TODO it should be refactored to prevent duplicate code but it is more readable now during development
    private void generateWebService(ProgressHandle handle) throws Exception {
        
        FileObject pkg = Templates.getTargetFolder(wiz);
        String wsName = Templates.getTargetName(wiz);
        
        
        if (serviceType == WizardProperties.FROM_SCRATCH) {
//            if ((projectType == ProjectInfo.JSE_PROJECT_TYPE && Util.isSourceLevel16orHigher(project)) ||
//                    ((Util.isJavaEE5orHigher(project) &&
//                    (projectType == WEB_PROJECT_TYPE || projectType == EJB_PROJECT_TYPE))) ||
//                    (jwsdpSupported)
//                    ) {
                JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(projectInfo.getProject().getProjectDirectory());
                wsName = getUniqueJaxwsName(jaxWsSupport, wsName);
                handle.progress(NbBundle.getMessage(JaxWsServiceCreator.class, "MSG_GEN_WS"), 50); //NOI18N
                //add the JAXWS 2.0 library, if not already added
                if (addJaxWsLib) addJaxws20Library(projectInfo.getProject());
                generateJaxWSImplFromTemplate(pkg, wsName, projectType);
                handle.finish();
                return;
//            } else if (!Util.isJavaEE5orHigher(project) && (projectType == WEB_PROJECT_TYPE ||projectType == EJB_PROJECT_TYPE) || (wsitSupported)) { //NOI18N
//                if ((!jsr109Supported && projectType == WEB_PROJECT_TYPE && !jsr109oldSupported) || (wsitSupported)) {  //if the platform is not jsr 109
//                    
//                    JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
//                    wsName = getUniqueJaxwsName(jaxWsSupport, wsName);
//                    handle.progress(NbBundle.getMessage(WebServiceCreator.class, "MSG_GEN_WS"), 50); //NOI18N
//                    //add the JAXWS 2.0 library, if not already added
//                    addJaxws20Library(project);
//                    generateJaxWSImplFromTemplate(pkg);
//                    
//                    handle.finish();
//                    
//                } else {  //it's not J2ee 1.5 but it is JSR 109
//                    WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(pkg);
//                    assert wsSupport != null;
//                    wsName = getUniqueJaxrpcName(wsSupport, wsName);
//                    WebServiceGenerator generator = new WebServiceGenerator(wsSupport, wsName, pkg, project);
//                    handle.progress(NbBundle.getMessage(WebServiceCreator.class, "MSG_GEN_SEI_AND_IMPL"), 50); //NOI18N
//                    generator.generateWebService();
//                    
//                    URI targetNS = null;
//                    URI typeNS = null;
//                    try {
//                        targetNS = generator.getTargetNS();
//                        typeNS = generator.getDefaultTypeNS(wsName); //Need to get from user
//                    } catch(java.net.URISyntaxException e) {
//                        String mes = NbBundle.getMessage(WebServiceCreator.class, "MSG_INVALID_URL_SYNTAX"); //NOI18N
//                        throw new Exception(mes);
//                    }
//                    //Create config file
//                    handle.progress(NbBundle.getMessage(WebServiceCreator.class, "MSG_CREATING_WSCOMPILE_ARTIFACTS")); //NOI18N
//                    String servantClassName = generator.getServantClassName();
//                    String seiClassName = generator.getSEIClassName();
//                    FileObject configFile = null;
//                    configFile = generator.generateConfigFile(seiClassName, servantClassName, targetNS, typeNS);
//                    handle.progress(70);
//                    
//                    //Add web service entries to the project's property file, project file
//                    wsSupport.addServiceImpl(wsName, configFile, false);
//                    handle.progress(90);
//                    
//                    //Add web service entries to the module's DD
//                    wsSupport.addServiceEntriesToDD(wsName, seiClassName, servantClassName);
//                    
//                    //Add webservice entry in webservices.xml
//                    handle.progress(NbBundle.getMessage(NewWebServiceWizardIterator.class, "MSG_ADDING_DD_ENTRIES")); //NOI18N
//                    String portTypeName = null;
//                    generator.addWebServiceEntry(seiClassName, portTypeName, targetNS);
//                    
//                    handle.finish();
//                    
//                    return;
//                }
//            }
        }
//        if (serviceType == WizardProperties.ENCAPSULATE_SESSION_BEAN) {
//            if ((projectType == JSE_PROJECT_TYPE && Util.isSourceLevel16orHigher(project)) ||
//                    (Util.isJavaEE5orHigher(project) && (projectType == WEB_PROJECT_TYPE
//                    ||projectType == EJB_PROJECT_TYPE)) //NOI18N
//                    ) {
//                
//                JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
//                wsName = getUniqueJaxwsName(jaxWsSupport, wsName);
//                // PENDING : the part below need to be simplified
//                // (the SEI class doesn't need to be generated)
//                WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(pkg);
//                assert wsSupport != null;
//                WebServiceGenerator generator = new WebServiceGenerator(wsSupport, wsName, pkg, project);
//                handle.progress(NbBundle.getMessage(WebServiceCreator.class, "MSG_GEN_SEI_AND_IMPL"), 50); //NOI18N
//                Node[] nodes = (Node[]) wiz.getProperty(WizardProperties.DELEGATE_TO_SESSION_BEAN);
//                generator.generateWebServiceJavaEE5(nodes);
//                
//                handle.progress(70);
//                
//                String servantClassName = generator.getServantClassName();
//                generator.addReferences(servantClassName, nodes);
//                
//                
//                jaxWsSupport.addService(wsName,servantClassName, jsr109Supported);
//                
//                handle.finish();
//                
//                return;
//            } else if (projectType == WEB_PROJECT_TYPE ||projectType == EJB_PROJECT_TYPE) {
//                WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(pkg);
//                assert wsSupport != null;
//                wsName = getUniqueJaxrpcName(wsSupport, wsName);
//                WebServiceGenerator generator = new WebServiceGenerator(wsSupport, wsName, pkg, project);
//                handle.progress(NbBundle.getMessage(WebServiceCreator.class, "MSG_GEN_SEI_AND_IMPL"), 50); //NOI18N
//                Node[] nodes = (Node[]) wiz.getProperty(WizardProperties.DELEGATE_TO_SESSION_BEAN);
//                generator.generateWebService(nodes);
//                
//                URI targetNS = null;
//                URI typeNS = null;
//                try {
//                    targetNS = generator.getTargetNS();
//                    typeNS = generator.getDefaultTypeNS(wsName); //Need to get from user
//                } catch(java.net.URISyntaxException e) {
//                    String mes = NbBundle.getMessage(WebServiceCreator.class, "MSG_INVALID_URL_SYNTAX"); //NOI18N
//                    throw new Exception(mes);
//                }
//                //Create config file
//                handle.progress(NbBundle.getMessage(WebServiceCreator.class, "MSG_CREATING_WSCOMPILE_ARTIFACTS")); //NOI18N
//                String servantClassName = generator.getServantClassName();
//                String seiClassName = generator.getSEIClassName();
//                FileObject configFile = null;
//                configFile = generator.generateConfigFile(seiClassName, servantClassName, targetNS, typeNS);
//                handle.progress(70);
//                
//                //Add web service entries to the project's property file, project file
//                wsSupport.addServiceImpl(wsName, configFile, false);
//                handle.progress(90);
//                
//                //Add web service entries to the module's DD
//                wsSupport.addServiceEntriesToDD(wsName, seiClassName, servantClassName);
//                
//                //Add webservice entry in webservices.xml
//                handle.progress(NbBundle.getMessage(NewWebServiceWizardIterator.class, "MSG_ADDING_DD_ENTRIES")); //NOI18N
//                String portTypeName = null;
//                generator.addWebServiceEntry(seiClassName, portTypeName, targetNS);
//                
//                generator.addReferences(servantClassName, nodes);
//                
//                handle.finish();
//            }
//            
//            return;
//        }
    }
    
    private FileObject generateJaxWSImplFromTemplate(FileObject pkg, String wsName, int projectType) throws Exception {
        DataFolder df = DataFolder.findFolder(pkg);
        FileObject template = Templates.getTemplate(wiz);
        
        if (projectType==ProjectInfo.EJB_PROJECT_TYPE) { //EJB Web Service
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("EjbWebService","java"); //NOI18N
        }        
        DataObject dTemplate = DataObject.find(template);
        DataObject dobj = dTemplate.createFromTemplate(df, wsName);
        FileObject createdFile = dobj.getPrimaryFile();
        
        /* don't rely on annotation listener to notify the service creation */
        JaxWsModel jaxWsModel = projectInfo.getProject().getLookup().lookup(JaxWsModel.class);
        if ( jaxWsModel!= null) {
            ClassPath classPath = ClassPath.getClassPath(createdFile, ClassPath.SOURCE);
            String serviceImplPath = classPath.getResourceName(createdFile, '.', false);
            jaxWsModel.addService(wsName, serviceImplPath);
            jaxWsModel.write();
        }    
        openFileInEditor(dobj);
        return createdFile;
    }
    
    private String getUniqueJaxwsName(JAXWSSupport jaxWsSupport, String origName){
        List<Service> webServices = jaxWsSupport.getServices();
        List<String> serviceNames = new ArrayList<String>(webServices.size());
        for(Service service : webServices){
            serviceNames.add(service.getName());
        }
        return uniqueWSName(origName, serviceNames);
    }
    
    private String uniqueWSName(final String origName, List<String> names ){
        int uniquifier = 0;
        String truename = origName;
        while(names.contains(truename)){
            truename = origName + String.valueOf(++uniquifier);
        }
        return truename;
    }
    
    private void openFileInEditor(DataObject dobj){
        final EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
        RequestProcessor.getDefault().post(new Runnable(){
            public void run(){
                ec.open();
            }
        }, 1000);
    }
    
    private void addJaxws20Library(Project project) throws Exception {
        
        // check if the wsimport class is already present - this means we don't need to add the library
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
        FileObject wsimportFO = classPath.findResource("com/sun/tools/ws/ant/WsImport.class"); // NOI18N
        if (wsimportFO != null) {
            return;
        }
        
        ProjectClassPathExtender pce = (ProjectClassPathExtender)project.getLookup().lookup(ProjectClassPathExtender.class);
        Library jaxws20_ext = LibraryManager.getDefault().getLibrary("jaxws20"); //NOI18N
        if (pce != null && jaxws20_ext != null) {
            try{
                pce.addLibrary(jaxws20_ext);
            } catch(IOException e){
                throw new Exception("Unable to add JAXWS 2.0 Library. " + e.getMessage());
            }
        } else{
            throw new Exception("Unable to add JAXWS 2.0 Library. " +
                    "ProjectClassPathExtender or library not found");
        }
    }
    private void generateWsFromWsdl15(final ProgressHandle handle) throws Exception {
        String wsdlFilePath = (String) wiz.getProperty(WizardProperties.WSDL_FILE_PATH);
        File normalizedWsdlFilePath = FileUtil.normalizeFile(new File(wsdlFilePath));
        //convert to URI first to take care of spaces
        final URL wsdlURL = normalizedWsdlFilePath.toURI().toURL();
        final WsdlService service = (WsdlService) wiz.getProperty(WizardProperties.WSDL_SERVICE);
        if (service==null) {
            JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(projectInfo.getProject().getProjectDirectory());
            FileObject targetFolder = Templates.getTargetFolder(wiz);
            String targetName = Templates.getTargetName(wiz);
            WsdlServiceHandler handler = (WsdlServiceHandler)wiz.getProperty(WizardProperties.WSDL_SERVICE_HANDLER);
            JaxWsUtils.generateJaxWsArtifacts(projectInfo.getProject(),targetFolder,targetName,wsdlURL,handler.getServiceName(),handler.getPortName());
            WsdlModeler wsdlModeler = (WsdlModeler) wiz.getProperty(WizardProperties.WSDL_MODELER);
            if (wsdlModeler!=null && wsdlModeler.getCreationException()!=null) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(JaxWsServiceCreator.class,"TXT_CannotGenerateArtifacts",
                                                wsdlModeler.getCreationException().getLocalizedMessage()),
                            NotifyDescriptor.ERROR_MESSAGE)
                    );
            }
            handle.finish();
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
                        JaxWsUtils.generateJaxWsImplementationClass(projectInfo.getProject(),
                                                                 targetFolder,
                                                                 targetName,
                                                                 wsdlURL,
                                                                 service1, port1);
                        handle.finish();
                    }
                    catch (Exception ex) {
                        handle.finish();
                        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,
                                                         ex);
                    }
                }
            });
        }
    }
}
