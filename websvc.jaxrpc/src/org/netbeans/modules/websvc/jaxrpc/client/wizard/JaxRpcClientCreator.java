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

package org.netbeans.modules.websvc.jaxrpc.client.wizard;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.api.client.ClientStubDescriptor;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.core.ClientCreator;
import org.netbeans.modules.websvc.core.ClientWizardProperties;
import org.netbeans.modules.websvc.core.webservices.ui.panels.WebProxySetter;
import org.netbeans.modules.websvc.core.WsdlRetriever;
import org.netbeans.modules.websvc.core.WsdlRetriever;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.j2ee.common.Util;
import org.openide.util.Task;

/**
 *
 * @author radko
 */
public class JaxRpcClientCreator implements ClientCreator {
    
    private static JaxRpcClientCreator instance;
    private Project project;
    private WizardDescriptor wiz;
    
    private static final boolean DEBUG = false;
    private static final int JSE_PROJECT_TYPE = 0;
    private static final int WEB_PROJECT_TYPE = 1;
    private static final int EJB_PROJECT_TYPE = 2;
    private static final int CAR_PROJECT_TYPE = 3;
    
    /**
     * Creates a new instance of WebServiceClientCreator
     */
    public JaxRpcClientCreator(Project project, WizardDescriptor wiz) {
        this.project = project;
        this.wiz = wiz;
    }
     public void createClient() throws IOException {
        
        final boolean isJsr109Supported = isJsr109Supported();
        final boolean isJsr109OldSupported = isJsr109OldSupported();
        final boolean isJWSDPSupported = isJWSDPSupported();
        
        // Use Progress API to display generator messages.
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(JaxRpcClientCreator.class, "MSG_WizCreateClient")); //NOI18N
        
        task = new Task(new Runnable() {
            public void run() {
                try {
//                    String jaxVersion = (String) wiz.getProperty(ClientWizardProperties.JAX_VERSION);
//                    if (jaxVersion.equals(WizardProperties.JAX_WS)) {
                        handle.start();
                        //generate15Client((isJsr109Supported || isJWSDPSupported), handle);
                        generate14Client(handle);
//                    } else {
//                        handle.start(100);
//                        generate14Client(handle);
//                    }
                } catch (IOException exc) {
                    //finish progress bar
                    handle.finish();
                    
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, exc);
                }
            }
        });
        RequestProcessor.getDefault().post(task);
    }
    
    private String getWsdlName(String wsdlUrl) {
        int ind = wsdlUrl.lastIndexOf("/"); //NOI18N
        String wsdlName = ind>=0?wsdlUrl.substring(ind+1):wsdlUrl;
        if (wsdlName.toUpperCase().endsWith("?WSDL")) wsdlName = wsdlName.substring(0,wsdlName.length()-5); //NOI18N
        ind = wsdlName.lastIndexOf(".wsdl"); //NOI18N
        if (ind>0) wsdlName = wsdlName.substring(0,ind);
        // replace special characters with '_'
        return convertAllSpecialChars(wsdlName);
    }
    
    private String convertAllSpecialChars(String resultStr){
        StringBuffer sb = new StringBuffer(resultStr);
        for(int i = 0; i < sb.length(); i++){
            char c = sb.charAt(i);
            if( Character.isLetterOrDigit(c) ||
                    (c == '/') ||
                    (c == '.') ||
                    (c == '_') ||
                    (c == ' ') ||
                    (c == '-')){
                continue;
            }else{
                sb.setCharAt(i, '_');
            }
        }
        return sb.toString();
    }
    
    private void generate14Client(final ProgressHandle handle) throws IOException {
        // Steps:
        // 1. invoke wizard to select which service to add a reference to.
        //    How to interpret node input set --
        //    + empty: wizard forces project selection, then service selection
        //    + client node: determine project and start on service page
        //    + wsdl node: would select project, but not service.  would also
        //      have to verify that WSDL is fully formed.
        
        WebServicesClientSupport clientSupport = null;
        
        // !PW Get client support from project (from first page of wizard)
        if(project != null) {
            clientSupport = WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory());
        }
        
        if(clientSupport == null) {
            // notify no client support
//			String mes = MessageFormat.format (
//				NbBundle.getMessage (WebServiceClientWizardIterator.class, "ERR_WebServiceClientSupportNotFound"),
//				new Object [] {"Servlet Listener"}); //NOI18N
            String mes = NbBundle.getMessage(JaxRpcClientCreator.class, "ERR_NoWebServiceClientSupport"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return;
        }       
        
        final byte [] sourceWsdlDownload = (byte []) wiz.getProperty(ClientWizardProperties.WSDL_DOWNLOAD_FILE);
        final List /*WsdlRetriever.SchemaInfo */ downloadedSchemas = (List) wiz.getProperty(ClientWizardProperties.WSDL_DOWNLOAD_SCHEMAS);
        String wsdlFilePath = (String) wiz.getProperty(ClientWizardProperties.WSDL_FILE_PATH);
        String packageName = (String) wiz.getProperty(ClientWizardProperties.WSDL_PACKAGE_NAME);
        ClientStubDescriptor stubDescriptor = (ClientStubDescriptor) wiz.getProperty(ClientWizardProperties.CLIENT_STUB_TYPE);
        
        String sourceUrl;
        FileObject sourceWsdlFile = null;
        
        if(sourceWsdlDownload == null) {
            // Verify the existence of the source WSDL file and that we can get a file object for it.
            File normalizedWsdlFilePath = FileUtil.normalizeFile(new File(wsdlFilePath));
            sourceUrl = normalizedWsdlFilePath.toString();
            sourceWsdlFile = FileUtil.toFileObject(normalizedWsdlFilePath);
            
            if(sourceWsdlFile == null) {
                String mes = NbBundle.getMessage(JaxRpcClientCreator.class, "ERR_WsdlFileNotFound", normalizedWsdlFilePath); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
                return;
            }
        } else {
            // create a temporary WSDL file
            File wsdlFile = new File(System.getProperty("java.io.tmpdir"), wsdlFilePath);
            if(!wsdlFile.exists()) {
                try {
                    wsdlFile.createNewFile();
                } catch(IOException ex) {
                    String mes = NbBundle.getMessage(JaxRpcClientCreator.class, "ERR_UnableToCreateTempFile", wsdlFile.getPath()); // NOI18N
                    NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                    return;
                }
            }
            
            sourceUrl = (String) wiz.getProperty(ClientWizardProperties.WSDL_DOWNLOAD_URL);
            sourceWsdlFile = FileUtil.toFileObject(FileUtil.normalizeFile(wsdlFile));
            
            if(sourceWsdlFile != null) {
                FileLock wsdlLock = sourceWsdlFile.lock();
                
                try {
                    OutputStream out = sourceWsdlFile.getOutputStream(wsdlLock);
                    try {
                        out.write(sourceWsdlDownload);
                        out.flush();
                    } finally {
                        if(out != null) {
                            out.close();
                        }
                    }
                } finally {
                    wsdlLock.releaseLock();
                }
            } else {
                String mes = NbBundle.getMessage(JaxRpcClientCreator.class, "ERR_UnableToCreateTempFile", wsdlFile.getPath()); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
                return;
            }
            
            // create temporary Schema Files
            if (downloadedSchemas!=null) {
                Iterator it = downloadedSchemas.iterator();
                while (it.hasNext()) {
                    WsdlRetriever.SchemaInfo schemaInfo = (WsdlRetriever.SchemaInfo)it.next();
                    File schemalFile = new File(System.getProperty("java.io.tmpdir"), schemaInfo.getSchemaName());
                    try {
                        schemalFile.createNewFile();
                    } catch(IOException ex) {
                        String mes = NbBundle.getMessage(JaxRpcClientCreator.class, "ERR_UnableToCreateTempFile", schemalFile.getPath()); // NOI18N
                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                        return;
                    }
                    FileObject schemaFo = FileUtil.toFileObject(FileUtil.normalizeFile(schemalFile));
                    if(schemaFo != null) {
                        FileLock lock = schemaFo.lock();
                        
                        try {
                            OutputStream out = schemaFo.getOutputStream(lock);
                            try {
                                out.write(schemaInfo.getSchemaContent());
                                out.flush();
                            } finally {
                                if(out != null) {
                                    out.close();
                                }
                            }
                        } finally {
                            lock.releaseLock();
                        }
                    } else {
                        String mes = NbBundle.getMessage(JaxRpcClientCreator.class, "ERR_UnableToCreateTempFile", schemalFile.getPath()); // NOI18N
                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                        return;
                    }
                } //end while
            } // end if
        } //end else
        
        // 2. add jax-rpc library if wscompile isnt present
        SourceGroup[] sgs = getJavaSourceGroups(project);
        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
        
        FileObject wscompileFO = classPath.findResource("com/sun/xml/rpc/tools/ant/Wscompile.class");
        if (wscompileFO==null) {
            // add jax-rpc16 if webservice is not on classpath
            ProjectClassPathExtender pce = (ProjectClassPathExtender)project.getLookup().lookup(ProjectClassPathExtender.class);
            Library jaxrpclib = LibraryManager.getDefault().getLibrary("jaxrpc16"); //NOI18N
            if ((pce!=null) && (jaxrpclib != null)) {
                pce.addLibrary(jaxrpclib);
            }
        }
        
        // sets JVM Proxy Options
        clientSupport.setProxyJVMOptions(WebProxySetter.getInstance().getProxyHost(),WebProxySetter.getInstance().getProxyPort());
        
        // 3. add the service client to the project.
        // Use Progress API to display generator messages.        
        final ClientBuilder builder = new ClientBuilder(project, clientSupport, sourceWsdlFile, packageName, sourceUrl, stubDescriptor);
        final FileObject sourceWsdlFileTmp = sourceWsdlFile;
        
        org.openide.util.RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    builder.generate(handle);
                    
                    if(sourceWsdlDownload != null) {
                        // we used a temp file, delete it now.
                        try {
                            sourceWsdlFileTmp.delete();
                        } catch(FileAlreadyLockedException ex) {
                            String mes = NbBundle.getMessage(JaxRpcClientCreator.class, "ERR_TempFileLocked", sourceWsdlFileTmp.getNameExt()); // NOI18N
                            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                        } catch(IOException ex) {
                            String mes = NbBundle.getMessage(JaxRpcClientCreator.class, "ERR_TempFileNotDeleted", sourceWsdlFileTmp.getNameExt()); // NOI18N
                            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                        }
                    }
                    
                    handle.progress(NbBundle.getMessage(JaxRpcClientCreator.class, "MSG_WizDone"),99);
                } finally {
                    handle.finish();
                }
            }
        });
    }   
    
    /**
     * Returns Java source groups for all source packages in given project.<br>
     * Doesn't include test packages.
     *
     * @param project Project to search
     * @return Array of SourceGroup. It is empty if any probelm occurs.
     */
    static SourceGroup[] getJavaSourceGroups(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set testGroups = getTestSourceGroups(project, sourceGroups);
        List result = new ArrayList();
        for (int i = 0; i < sourceGroups.length; i++) {
            if (!testGroups.contains(sourceGroups[i])) {
                result.add(sourceGroups[i]);
            }
        }
        return (SourceGroup[]) result.toArray(new SourceGroup[result.size()]);
    }
    
    private static Set/*<SourceGroup>*/ getTestSourceGroups(Project project, SourceGroup[] sourceGroups) {
        Map foldersToSourceGroupsMap = createFoldersToSourceGroupsMap(sourceGroups);
        Set testGroups = new HashSet();
        for (int i = 0; i < sourceGroups.length; i++) {
            testGroups.addAll(getTestTargets(sourceGroups[i], foldersToSourceGroupsMap));
        }
        return testGroups;
    }
    
    private static List/*<SourceGroup>*/ getTestTargets(SourceGroup sourceGroup, Map foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            return new ArrayList();
        }
        List result = new ArrayList();
        List sourceRoots = getFileObjects(rootURLs);
        for (int i = 0; i < sourceRoots.size(); i++) {
            FileObject sourceRoot = (FileObject) sourceRoots.get(i);
            SourceGroup srcGroup = (SourceGroup) foldersToSourceGroupsMap.get(sourceRoot);
            if (srcGroup != null) {
                result.add(srcGroup);
            }
        }
        return result;
    }
    
    private static Map createFoldersToSourceGroupsMap(final SourceGroup[] sourceGroups) {
        Map result;
        if (sourceGroups.length == 0) {
            result = Collections.EMPTY_MAP;
        } else {
            result = new HashMap(2 * sourceGroups.length, .5f);
            for (int i = 0; i < sourceGroups.length; i++) {
                SourceGroup sourceGroup = sourceGroups[i];
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }
        return result;
    }
    
    private static List/*<FileObject>*/ getFileObjects(URL[] urls) {
        List result = new ArrayList();
        for (int i = 0; i < urls.length; i++) {
            FileObject sourceRoot = URLMapper.findFileObject(urls[i]);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else {
                int severity = ErrorManager.INFORMATIONAL;
                if (ErrorManager.getDefault().isNotifiable(severity)) {
                    ErrorManager.getDefault().notify(severity, new IllegalStateException(
                            "No FileObject found for the following URL: " + urls[i])); //NOI18N
                }
            }
        }
        return result;
    }
    
    private J2eePlatform getJ2eePlatform(){
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        if(provider != null){
            String serverInstanceID = provider.getServerInstanceID();
            if(serverInstanceID != null && serverInstanceID.length() > 0) {
                return Deployment.getDefault().getJ2eePlatform(serverInstanceID);
            }
        }
        return null;
    }
    
    private boolean isJWSDPSupported(){
        J2eePlatform j2eePlatform = getJ2eePlatform();
        if(j2eePlatform != null){
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_JWSDP);
        }
        return false;
    }
    
    private boolean isJsr109Supported(){
        J2eePlatform j2eePlatform = getJ2eePlatform();
        if(j2eePlatform != null){
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109);
        }
        return false;
    }
    
    private boolean isJsr109OldSupported(){
        J2eePlatform j2eePlatform = getJ2eePlatform();
        if(j2eePlatform != null){
            return j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE);
        }
        return false;
    }

    /**
      *
      * <b>DON'T USE</b>, for tests only
      */
    Task task;
}
