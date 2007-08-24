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

package org.netbeans.modules.websvc.jaxrpc.dev.wizard;

import com.sun.source.tree.ClassTree;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.transform.stream.StreamSource;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport;
import org.netbeans.modules.websvc.core.ServiceCreator;
import org.netbeans.modules.websvc.core.dev.wizard.NewWebServiceWizardIterator;
import org.netbeans.modules.websvc.core.dev.wizard.ProjectInfo;
import org.netbeans.modules.websvc.core.dev.wizard.WizardProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Ajit Bhate
 */
public class JaxRpcServiceCreator implements ServiceCreator {
    
    private Project project;
    private ProjectInfo projectInfo;
    private WizardDescriptor wiz;
    private String wsName;
    
    /** Creates a new instance of JaxRpcServiceCreator */
    public JaxRpcServiceCreator(Project project, ProjectInfo projectInfo, WizardDescriptor wiz) {
        this.project = project;
        this.projectInfo = projectInfo;
        this.wiz = wiz;
    }
    
    public void createService() throws IOException {
        // Use Progress API to display generator messages.
        final ProgressHandle handle = ProgressHandleFactory.createHandle( NbBundle.getMessage(JaxRpcServiceCreator.class, "TXT_WebServiceGeneration")); //NOI18N
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
        final ProgressHandle handle = ProgressHandleFactory.createHandle( NbBundle.getMessage(JaxRpcServiceCreator.class, "TXT_WebServiceGeneration")); //NOI18N
        
        Runnable r = new Runnable() {
            public void run() {
                try {
                    handle.start(100);
                    generateWsFromWsdl(handle);
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
    
    private void generateWebService(ProgressHandle handle) throws Exception {
        FileObject pkg = Templates.getTargetFolder(wiz);
        WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(pkg);
        assert wsSupport != null;
        
        wsName = getUniqueJaxrpcName(wsSupport, Templates.getTargetName(wiz));
        
        WebServiceGenerator generator = new WebServiceGenerator(wsSupport, wsName, pkg, project);
        handle.progress(NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_GEN_SEI_AND_IMPL"), 50); //NOI18N
        generator.generateWebService();
        
        URI targetNS = null;
        URI typeNS = null;
        try {
            targetNS = generator.getTargetNS();
            typeNS = generator.getDefaultTypeNS(wsName); //Need to get from user
        } catch(java.net.URISyntaxException e) {
            String mes = NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_INVALID_URL_SYNTAX"); //NOI18N
            throw new Exception(mes);
        }
        //Create config file
        handle.progress(NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_CREATING_WSCOMPILE_ARTIFACTS")); //NOI18N
        String servantClassName = generator.getServantClassName();
        String seiClassName = generator.getSEIClassName();
        FileObject configFile = null;
        configFile = generator.generateConfigFile(seiClassName, servantClassName, targetNS, typeNS);
        handle.progress(70);
        
        //Add web service entries to the project's property file, project file
        wsSupport.addServiceImpl(wsName, configFile, false);
        handle.progress(90);
        
        //Add web service entries to the module's DD
        wsSupport.addServiceEntriesToDD(wsName, seiClassName, servantClassName);
        
        //Add webservice entry in webservices.xml
        handle.progress(NbBundle.getMessage(NewWebServiceWizardIterator.class, "MSG_ADDING_DD_ENTRIES")); //NOI18N
        String portTypeName = null;
        generator.addWebServiceEntry(seiClassName, portTypeName, targetNS);
        
        handle.finish();
    }
    
    private void generateWsFromWsdl(ProgressHandle handle) throws Exception {
        FileObject pkg = Templates.getTargetFolder(wiz);
        wsName = Templates.getTargetName(wiz);
        WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(pkg);
        assert wsSupport != null;
        WebServiceGenerator generator = new WebServiceGenerator(wsSupport, wsName, pkg, project);
        
        //coming from wsdl
        FileObject wsDDFolder = wsSupport.getWsDDFolder();
        //get wsdl folder, if none, create it
        FileObject wsdlFolder = wsDDFolder.getFileObject("wsdl"); //NOI18N
        if(wsdlFolder == null) {
            wsdlFolder = wsDDFolder.createFolder("wsdl"); //NOI18N
        }
        
        handle.progress(NbBundle.getMessage(NewWebServiceWizardIterator.class, "MSG_PARSING_WSDL"), 30); //NOI18N
        String wsdlFilePath = (String)wiz.getProperty(WizardProperties.WSDL_FILE_PATH);
        File normalizedWsdlFilePath = FileUtil.normalizeFile(new File(wsdlFilePath));
        final FileObject sourceWsdlFile = FileUtil.toFileObject(normalizedWsdlFilePath);
        if(sourceWsdlFile == null) {
            String mes = NbBundle.getMessage(NewWebServiceWizardIterator.class, "MSG_CANNOT_GET_FILE_OBJECT", normalizedWsdlFilePath.getAbsolutePath()); //NOI18N
            throw new IOException(mes);
        }
        String changedWsName = null;
        try {
            changedWsName = generator.parseWSDL(sourceWsdlFile.getInputStream());
        } catch (NoWSPortDefinedException exc) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "WSDL does not contain any defined ports"); //NOI18N
            String mes = NbBundle.getMessage(NewWebServiceWizardIterator.class, "ERR_WsdlNoPortDefined"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            handle.finish();
            return;
        }
        if (changedWsName==null) changedWsName = wsName;
        handle.progress(NbBundle.getMessage(NewWebServiceWizardIterator.class, "MSG_CREATING_NEW_WSDL"), 50); //NOI18N
        FileObject wsdlFO = generator.generateWSDL(WebServiceGenerator.WSDL_TEMPLATE, changedWsName, generator.getSoapBinding(),
                generator.getPortTypeName(), wsdlFolder, sourceWsdlFile.getParent(), wsName, new StreamSource(sourceWsdlFile.getInputStream()));
        
        URI targetNS = null;
        URI typeNS = null;
        try {
            targetNS = generator.getTargetNS();
            typeNS = generator.getDefaultTypeNS(wsName);     //Need to get from user
        } catch(java.net.URISyntaxException e) {
            String mes = NbBundle.getMessage(NewWebServiceWizardIterator.class, "MSG_INVALID_URL_SYNTAX"); //NOI18N
            throw new Exception(mes);
        }
        
        //Create config file
        handle.progress(NbBundle.getMessage(NewWebServiceWizardIterator.class, "MSG_CREATING_WSCOMPILE_ARTIFACTS"),60); //NOI18N
        String servantClassName = generator.getServantClassName();
        String seiClassName = generator.getSEIClassName();
        FileObject configFile = null;
        
        File wsdlFile = FileUtil.toFile(wsdlFO);
        URI wsdlURI = wsdlFile.toURI();
        configFile = generator.generateConfigFile(wsdlURI);
        //Add web service entries to the project's property file, project file
        wsSupport.addServiceImpl(wsName, configFile, true, generator.getWscompileFeatures());
        
        //run the wscompile ant target
        handle.progress(NbBundle.getMessage(NewWebServiceWizardIterator.class, "MSG_RUNNING_WSCOMPILE_TARGET"),70); //NOI18N
        String targetName = wsName + "_wscompile"; //NOI18N
        ExecutorTask task = ActionUtils.runTarget(findBuildXml(), new String[]{targetName}, null);
        task.waitFinished();
        if(task.result() != 0) {
            String mes = NbBundle.getMessage(NewWebServiceWizardIterator.class, "MSG_WSCOMPILE_UNSUCCESSFUL"); //NOI18N
            wsSupport.removeProjectEntries(wsName);
            try {
                deleteFile(configFile);
                deleteFile(wsdlFO);
            } catch(IOException e) {
                String message = NbBundle.getMessage(NewWebServiceWizardIterator.class, "MSG_UNABLE_DELETE_FILES"); //NOI18N
                NotifyDescriptor nd =
                        new NotifyDescriptor.Message(message,
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                //let this through
            }
            throw new Exception(mes);
        }
        handle.progress(80);
        String implClassName = servantClassName.substring(servantClassName.lastIndexOf(".") + 1); //NOI18N
        FileObject clz = pkg.getFileObject(implClassName,"java"); //NOI18N
        DataObject dobj = DataObject.find(clz);
        //commented due to Retouche Bug
        //addHeaderComments(wsName, dobj);
        wsSupport.addInfrastructure(implClassName, pkg);
        
        //Add web service entries to the module's DD
        wsSupport.addServiceEntriesToDD(wsName, seiClassName, servantClassName);
        
        //Add webservice entry in webservices.xml
        handle.progress(NbBundle.getMessage(NewWebServiceWizardIterator.class, "MSG_ADDING_DD_ENTRIES"),90); //NOI18N
        String portTypeName = null;
        
        portTypeName = generator.getPortTypeName();
        
        generator.addWebServiceEntry(seiClassName, portTypeName, targetNS);
        
        //open the class in the editor
        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        ec.open();
        
        handle.finish();
    }
    
    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
    private void deleteFile(FileObject file)throws IOException {
        FileLock lock = null;
        try {
            lock = file.lock();
            file.delete(lock);
        } finally {
            if(lock != null) {
                lock.releaseLock();
            }
        }
    }
    
    private void addHeaderComments(final String wsName, final DataObject dataObject) {
        final JavaSource targetSource = JavaSource.forFileObject(dataObject.getPrimaryFile());
        final ProgressHandle handle = ProgressHandleFactory.createHandle("Adding operation");
        handle.start(100);
        final CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                ClassTree javaClass = genUtils.getClassTree();
                handle.progress(70);
                // create new (annotated) method
                StringBuffer buffer = new StringBuffer(NbBundle.getMessage(
                        NewWebServiceWizardIterator.class, "MSG_WS_CLASS_COMMENT", wsName) + "\n"); //NOI18N
                buffer.append(NbBundle.getMessage(
                        NewWebServiceWizardIterator.class, "MSG_CREATED_COMMENT")
                        + " " + DateFormat.getDateTimeInstance().format(new Date()) + "\n" ); //NOI18N
                buffer.append("@author " + System.getProperty("user.name") ); //NOI18N
                Comment comment = Comment.create(Style.JAVADOC, 0,0,0,buffer.toString());
                make.addComment(javaClass, comment, true);
                handle.progress(90);
            }
            public void cancel() {}
        };
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    targetSource.runModificationTask(modificationTask).commit();
                    SaveCookie cookie = dataObject.getCookie(SaveCookie.class);
                    if (cookie!=null) cookie.save();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                } finally {
                    handle.finish();
                }
            }
        });
        
    }
    
    private String getUniqueJaxrpcName(WebServicesSupport wsSupport, String origName){
        List<WsCompileEditorSupport.ServiceSettings> webServices = wsSupport.getServices();
        List<String> serviceNames = new ArrayList<String>(webServices.size());
        for(WsCompileEditorSupport.ServiceSettings service: webServices){
            serviceNames.add(service.getServiceName());
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
}
