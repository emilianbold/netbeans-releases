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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.jaxrpc.dev.wizard;

import com.sun.source.tree.ClassTree;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.netbeans.modules.websvc.api.support.ServiceCreator;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport;
import org.netbeans.modules.websvc.core.ProjectInfo;
import org.netbeans.modules.websvc.core.WsdlRetriever;
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
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Ajit Bhate
 */
public class JaxRpcServiceCreator implements ServiceCreator, WsdlRetriever.MessageReceiver {
    private static final String WSDL_FILE_PATH = "wsdlFilePath"; //NOI18N
    
    private Project project;
    private ProjectInfo projectInfo;
    private WizardDescriptor wiz;
    private String wsName;
    private String downloadMsg;
    
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
        handle.progress(NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_ADDING_DD_ENTRIES")); //NOI18N
        String portTypeName = null;
        generator.addWebServiceEntry(seiClassName, portTypeName, targetNS);
        
        //open the class in the editor
        FileObject servantFO = pkg.getFileObject(wsName+"Impl", "java");
        if(servantFO!=null) {
            try {
                DataObject dobj = DataObject.find(servantFO);
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                ec.open();
            } catch (DataObjectNotFoundException donfe) {
            }
        }
        
        handle.finish();
    }
    
    private void generateWsFromWsdl(ProgressHandle handle) throws Exception {
        final FileObject pkg = Templates.getTargetFolder(wiz);
        WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(pkg);
        wsName = getUniqueJaxrpcName(wsSupport, Templates.getTargetName(wiz));
        assert wsSupport != null;
        WebServiceGenerator generator = new WebServiceGenerator(wsSupport, wsName, pkg, project);
        
        //coming from wsdl
        FileObject wsDDFolder = wsSupport.getWsDDFolder();
        //get wsdl folder, if none, create it
        FileObject wsdlFolder = wsDDFolder.getFileObject("wsdl"); //NOI18N
        if(wsdlFolder == null) {
            wsdlFolder = wsDDFolder.createFolder("wsdl"); //NOI18N
        }
        
        handle.progress(NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_PARSING_WSDL"), 30); //NOI18N
        File normalizedWsdlFilePath = null;
        String wsdlUrl = (String) wiz.getProperty("wsdl_url");
        String wsdlFilePath = (String)wiz.getProperty(WSDL_FILE_PATH);
        if (wsdlFilePath != null) {
            normalizedWsdlFilePath = FileUtil.normalizeFile(new File(wsdlFilePath));
        } else {
            if (wsdlUrl != null) {
                WsdlRetriever retriever = new WsdlRetriever(this, wsdlUrl);
                retriever.run();
                if (retriever.getState() != WsdlRetriever.STATUS_COMPLETE) {
                    String errorMessage = NbBundle.getMessage(JaxRpcServiceCreator.class, 
                            "ERR_DownloadFailedUnknown");
                    if (downloadMsg != null) {
                        errorMessage = NbBundle.getMessage(JaxRpcServiceCreator.class, 
                                "ERR_DownloadFailed", downloadMsg); // NOI18N
                    }
                    throw new IOException(errorMessage); //NOI18N
                } else {
                    // create a temporary WSDL file
                    File wsdlFile = new File(System.getProperty("java.io.tmpdir"), retriever.getWsdlFileName());
                    if (!wsdlFile.exists()) {
                        try {
                            wsdlFile.createNewFile();
                        } catch (IOException ex) {
                            String mes = NbBundle.getMessage(JaxRpcServiceCreator.class, 
                                    "ERR_UnableToCreateTempFile", wsdlFile.getPath()); // NOI18N
                            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                            return;
                        }
                    }
                    FileObject sourceWsdlFile = FileUtil.toFileObject(FileUtil.normalizeFile(wsdlFile));
                    if (sourceWsdlFile != null) {
                        FileLock wsdlLock = sourceWsdlFile.lock();
                        try {
                            OutputStream out = sourceWsdlFile.getOutputStream(wsdlLock);
                            try {
                                out.write(retriever.getWsdl());
                                out.flush();
                            } finally {
                                if (out != null) {
                                    out.close();
                                }
                            }
                        } finally {
                            wsdlLock.releaseLock();
                        }
                    } else {
                        String mes = NbBundle.getMessage(JaxRpcServiceCreator.class, 
                                "ERR_UnableToCreateTempFile", wsdlFile.getPath()); // NOI18N
                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes, 
                                NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                        return;
                    }
                    // create temporary Schema Files
                    List<WsdlRetriever.SchemaInfo> downloadedSchemas = retriever.getSchemas();
                    if (downloadedSchemas != null && !downloadedSchemas.isEmpty()) {
                        for (WsdlRetriever.SchemaInfo schemaInfo : downloadedSchemas) {
                            File schemalFile = new File(System.getProperty
                                    ("java.io.tmpdir"), schemaInfo.getSchemaName());
                            try {
                                schemalFile.createNewFile();
                            } catch (IOException ex) {
                                String mes = NbBundle.getMessage(JaxRpcServiceCreator.class, 
                                        "ERR_UnableToCreateTempFile", schemalFile.getPath()); // NOI18N
                                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, 
                                        NotifyDescriptor.Message.ERROR_MESSAGE);
                                DialogDisplayer.getDefault().notify(desc);
                                return;
                            }
                            FileObject schemaFo = FileUtil.toFileObject(FileUtil.
                                    normalizeFile(schemalFile));
                            if (schemaFo != null) {
                                FileLock lock = schemaFo.lock();
                                try {
                                    OutputStream out = schemaFo.getOutputStream(lock);
                                    try {
                                        out.write(schemaInfo.getSchemaContent());
                                        out.flush();
                                    } finally {
                                        if (out != null) {
                                            out.close();
                                        }
                                    }
                                } finally {
                                    lock.releaseLock();
                                }
                            } else {
                                String mes = NbBundle.getMessage(JaxRpcServiceCreator.class,
                                        "ERR_UnableToCreateTempFile", schemalFile.getPath()); // NOI18N
                                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, 
                                        NotifyDescriptor.Message.ERROR_MESSAGE);
                                DialogDisplayer.getDefault().notify(desc);
                                return;
                            }
                        } //end for
                    } // end if
                    normalizedWsdlFilePath = FileUtil.normalizeFile(wsdlFile);
                } //end else
            }
        }
            
        final FileObject sourceWsdlFile = FileUtil.toFileObject(normalizedWsdlFilePath);
        if(sourceWsdlFile == null) {
            String mes = NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_CANNOT_GET_FILE_OBJECT", normalizedWsdlFilePath.getAbsolutePath()); //NOI18N
            throw new IOException(mes);
        }
        String changedWsName = null;
        try {
            changedWsName = generator.parseWSDL(sourceWsdlFile.getInputStream());
        } catch (NoWSPortDefinedException exc) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "WSDL does not contain any defined ports"); //NOI18N
            String mes = NbBundle.getMessage(JaxRpcServiceCreator.class, "ERR_WsdlNoPortDefined"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            handle.finish();
            return;
        }
        if (changedWsName==null) changedWsName = wsName;
        handle.progress(NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_CREATING_NEW_WSDL"), 50); //NOI18N
        FileObject wsdlFO = generator.generateWSDL(WebServiceGenerator.WSDL_TEMPLATE, changedWsName, generator.getSoapBinding(),
                generator.getPortTypeName(), wsdlFolder, sourceWsdlFile.getParent(), wsName, new StreamSource(sourceWsdlFile.getInputStream()));
        
        if(wsdlFilePath==null&&wsdlUrl!=null) {
            sourceWsdlFile.delete();
        }
        URI targetNS = null;
        URI typeNS = null;
        try {
            targetNS = generator.getTargetNS();
            typeNS = generator.getDefaultTypeNS(wsName);     //Need to get from user
        } catch(java.net.URISyntaxException e) {
            String mes = NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_INVALID_URL_SYNTAX"); //NOI18N
            throw new Exception(mes);
        }
        
        //Create config file
        handle.progress(NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_CREATING_WSCOMPILE_ARTIFACTS"),60); //NOI18N
        String servantClassName = generator.getServantClassName();
        String seiClassName = generator.getSEIClassName();
        FileObject configFile = null;
        
        File wsdlFile = FileUtil.toFile(wsdlFO);
        URI wsdlURI = wsdlFile.toURI();
        configFile = generator.generateConfigFile(wsdlURI);
        //Add web service entries to the project's property file, project file
        wsSupport.addServiceImpl(wsName, configFile, true, generator.getWscompileFeatures());
        
        //run the wscompile ant target
        handle.progress(NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_RUNNING_WSCOMPILE_TARGET"),70); //NOI18N
        String targetName = wsName + "_wscompile"; //NOI18N
        ExecutorTask task = ActionUtils.runTarget(findBuildXml(), new String[]{targetName}, null);
        task.waitFinished();
        if(task.result() != 0) {
            String mes = NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_WSCOMPILE_UNSUCCESSFUL"); //NOI18N
            wsSupport.removeProjectEntries(wsName);
            try {
                deleteFile(configFile);
                deleteFile(wsdlFO);
            } catch(IOException e) {
                String message = NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_UNABLE_DELETE_FILES"); //NOI18N
                NotifyDescriptor nd =
                        new NotifyDescriptor.Message(message,
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                //let this through
            }
            throw new Exception(mes);
        }
        handle.progress(80);
        final String implClassName = servantClassName.substring(servantClassName.lastIndexOf(".") + 1); //NOI18N

        //commented due to Retouche Bug
        //addHeaderComments(wsName, dobj);
        wsSupport.addInfrastructure(implClassName, pkg);
        
        //Add web service entries to the module's DD
        wsSupport.addServiceEntriesToDD(wsName, seiClassName, servantClassName);
        
        //Add webservice entry in webservices.xml
        handle.progress(NbBundle.getMessage(JaxRpcServiceCreator.class, "MSG_ADDING_DD_ENTRIES"),90); //NOI18N
        String portTypeName = null;
        
        portTypeName = generator.getPortTypeName();
        
        generator.addWebServiceEntry(seiClassName, portTypeName, targetNS);
        
        //open the implementation class in editor
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                // refresh folder due to issue 167543
                pkg.refresh();
                FileObject clz = pkg.getFileObject(implClassName,"java"); //NOI18N
                if (clz != null) {
                    try {
                        DataObject dobj = DataObject.find(clz);
                        EditorCookie ec = dobj.getCookie(EditorCookie.class);
                        ec.open();
                    } catch (Throwable ex) {
                        Logger.getLogger(JaxRpcServiceCreator.class.getName()).log(Level.WARNING,
                                "Cannot open implementation class in editor.", ex); //NOI18N
                    }
                }
            }
        });
        
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
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                handle.progress(70);
                // create new (annotated) method
                StringBuffer buffer = new StringBuffer(NbBundle.getMessage(
                        JaxRpcServiceCreator.class, "MSG_WS_CLASS_COMMENT", wsName) + "\n"); //NOI18N
                buffer.append(NbBundle.getMessage(
                        JaxRpcServiceCreator.class, "MSG_CREATED_COMMENT")
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

    public void setWsdlDownloadMessage(String m) {
        downloadMsg = m;
    }
}
