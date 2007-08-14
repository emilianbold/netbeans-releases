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

package org.netbeans.modules.websvc.core.jaxws.projects;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.spi.jaxws.client.ProjectJAXWSClientSupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/** Implementation of JAXWSClientSupportImpl for J2SE Project
 *
 * @author mkuchtiak
 */
public class J2SEProjectJAXWSClientSupport extends ProjectJAXWSClientSupport /*implements JAXWSClientSupportImpl*/ {
    private Project project;
    private static final String WSDL_FOLDER = "wsdl"; //NOI18N
    /** proxy host VM property key */
    private static final String KEY_PROXY_HOST = "http.proxyHost"; // NOI18N
    /** proxy port VM property key */
    private static final String KEY_PROXY_PORT = "http.proxyPort"; // NOI18N
    /** non proxy hosts VM property key */
    private static final String KEY_NON_PROXY_HOSTS = "http.nonProxyHosts"; // NOI18N
    /** https proxy host VM property key */
    private static final String KEY_HTTPS_PROXY_HOST = "https.proxyHost"; // NOI18N
    /** https proxy port VM property key */
    private static final String KEY_HTTPS_PROXY_PORT = "https.proxyPort"; // NOI18N
    /** non proxy hosts VM property key */
    private static final String KEY_HTTPS_NON_PROXY_HOSTS = "https.nonProxyHosts"; // NOI18N
    
    private static final String HTTP_PROXY_HOST_OPTION="-Dhttp.proxyHost"; //NOI18N
    private static final String HTTP_PROXY_PORT_OPTION="-Dhttp.proxyPort"; //NOI18N
    private static final String HTTP_NON_PROXY_HOSTS_OPTION="-Dhttp.nonProxyHosts"; //NOI18N
    private static final String HTTPS_PROXY_HOST_OPTION="-Dhttps.proxyHost"; //NOI18N
    private static final String HTTPS_PROXY_PORT_OPTION="-Dhttps.proxyPort"; //NOI18N
    private static final String HTTPS_NON_PROXY_HOSTS_OPTION="-Dhttps.nonProxyHosts"; //NOI18N
    private static final String RUN_JVM_ARGS = "run.jvmargs"; // NOI18N   
    /** Creates a new instance of J2SEProjectJAXWSClientSupport */
    public J2SEProjectJAXWSClientSupport(Project project) {
        super(project);
        this.project=project;
    }

    public FileObject getWsdlFolder(boolean create) throws IOException {
        //EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties ep = WSUtils.getEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assert ep!=null;
        String metaInfStr = ep.getProperty("meta.inf.dir"); //NOI18N
        String wsdlFolderStr = metaInfStr + "/" + WSDL_FOLDER; // NOI18N
        FileObject wsdlFolder = project.getProjectDirectory().getFileObject(wsdlFolderStr);
        if (wsdlFolder == null && create) {
            wsdlFolder = FileUtil.createFolder(project.getProjectDirectory(), wsdlFolderStr);
        }
        return wsdlFolder;
    }

    public String addServiceClient(String clientName, String wsdlUrl, String packageName, boolean isJsr109) {
        
        // call the super.addServiceClient();
        String serviceIdeName = super.addServiceClient(clientName, wsdlUrl, packageName, false);
        
        // add JVM Proxy Options to project's JVM
        if (serviceIdeName!=null) addJVMProxyOptions(clientName);
        
        return serviceIdeName;
    }
    
    protected void addJaxWs20Library() {
        ClassPath classPath = null;
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups!=null && sourceGroups.length>0) {
            FileObject srcRoot = sourceGroups[0].getRootFolder();
            ClassPath compileClassPath = ClassPath.getClassPath(srcRoot,ClassPath.COMPILE);
            ClassPath bootClassPath = ClassPath.getClassPath(srcRoot,ClassPath.BOOT);
            classPath = ClassPathSupport.createProxyClassPath(new ClassPath[]{compileClassPath, bootClassPath});
        }
        FileObject webServiceClass=null;
        if (classPath!=null) {
            webServiceClass = classPath.findResource("javax/jws/WebService.class"); // NOI18N
        }
        if (webServiceClass==null) {
            // add JAX-WS 2.0 if WsImport is not on classpath
            ProjectClassPathExtender pce = (ProjectClassPathExtender)project.getLookup().lookup(ProjectClassPathExtender.class);
            Library jaxwslib = LibraryManager.getDefault().getLibrary("jaxws21"); //NOI18N
            if ((pce!=null) && (jaxwslib != null)) {
                try {
                    pce.addLibrary(jaxwslib);
                } catch (IOException ex) {
                    ErrorManager.getDefault().log(ex.getMessage());
                }
                FileObject wscompileFO=null;
                if (classPath!=null) {
                    wscompileFO = classPath.findResource("com/sun/xml/rpc/tools/ant/Wscompile.class"); // NOI18N
                }
                if (wscompileFO!=null) {
                    NotifyDescriptor desc = new NotifyDescriptor.Message(
                    NbBundle.getMessage(J2SEProjectJAXWSClientSupport.class,"MSG_RemoveJAX-RPC"), // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                }
            }
        }
    }

    private void addJVMProxyOptions(final String clientName) {
        final String proxyHost = System.getProperty(KEY_PROXY_HOST);
        if (proxyHost!=null && proxyHost.length()>0) {
            ProjectManager.mutex().writeAccess ( new Runnable () {
                public void run() {
                    boolean modif=false;
                    String proxyPort = System.getProperty(KEY_PROXY_PORT);
                    if (proxyPort==null || proxyPort.length()==0) proxyPort="8080"; //NOI18N
                    String localHosts ="";
                    localHosts = getDefaultNonProxyHosts();
                    
                    //EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    EditableProperties ep = null;
                    try {
                        ep = WSUtils.getEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                    assert ep!=null;
                    String jvmOptions = ep.getProperty(RUN_JVM_ARGS);
                    if (jvmOptions==null || jvmOptions.length()==0) {
                        jvmOptions = HTTP_PROXY_HOST_OPTION+"="+proxyHost+ //NOI18N
                                " "+HTTP_PROXY_PORT_OPTION+"="+proxyPort+ //NOI18N
                                " "+HTTP_NON_PROXY_HOSTS_OPTION+"="+localHosts+ //NOI18N
                                " "+HTTPS_PROXY_HOST_OPTION+"="+proxyHost+ //NOI18N
                                " "+HTTPS_PROXY_PORT_OPTION+"="+proxyPort+ //NOI18N
                                " "+HTTPS_NON_PROXY_HOSTS_OPTION+"="+localHosts; //NOI18N
                        modif=true;
                    } else {
                        if (jvmOptions.indexOf(HTTP_PROXY_HOST_OPTION)<0) {
                            jvmOptions+=" "+HTTP_PROXY_HOST_OPTION+"="+proxyHost; //NOI18N
                            modif=true;
                        }
                        if (jvmOptions.indexOf(HTTP_PROXY_PORT_OPTION)<0) {
                            jvmOptions+=" "+HTTP_PROXY_PORT_OPTION+"="+proxyPort; //NOI18N
                            modif=true;
                        }
                        if (jvmOptions.indexOf(HTTP_NON_PROXY_HOSTS_OPTION)<0) {
                            jvmOptions+=" "+HTTP_NON_PROXY_HOSTS_OPTION+"="+localHosts; //NOI18N
                            modif=true;
                        }
                        if (jvmOptions.indexOf(HTTPS_PROXY_HOST_OPTION)<0) {
                            jvmOptions+=" "+HTTPS_PROXY_HOST_OPTION+"="+proxyHost; //NOI18N
                            modif=true;
                        }
                        if (jvmOptions.indexOf(HTTPS_PROXY_PORT_OPTION)<0) {
                            jvmOptions+=" "+HTTPS_PROXY_PORT_OPTION+"="+proxyPort; //NOI18N
                            modif=true;
                        }
                        if (jvmOptions.indexOf(HTTPS_NON_PROXY_HOSTS_OPTION)<0) {
                            jvmOptions+=" "+HTTPS_NON_PROXY_HOSTS_OPTION+"="+localHosts; //NOI18N
                            modif=true;
                        }
                    }
                    if (modif) {
                        ep.setProperty(RUN_JVM_ARGS,jvmOptions);                        
                        //updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
                        try {
                            WSUtils.storeEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                            ProjectManager.getDefault().saveProject(project);
                        } catch(IOException ex) {
                            NotifyDescriptor desc = new NotifyDescriptor.Message(
                            NbBundle.getMessage(J2SEProjectJAXWSClientSupport.class,"MSG_ErrorSavingOnWSClientAdd", clientName, ex.getLocalizedMessage()), // NOI18N
                            NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
                        }
                    }
                }
            });

        }
    }
    
     /** Returns the default value for the http.nonProxyHosts system property. <br>
     *  PENDING: should be a user settable property
     * @return sensible default for non-proxy hosts, including 'localhost'
     */
    private String getDefaultNonProxyHosts() {
        String nonProxy = "localhost|127.0.0.1"; // NOI18N
        String localhost = ""; // NOI18N
        try {
            localhost = InetAddress.getLocalHost().getHostName();
            if (!localhost.equals("localhost")) { // NOI18N
                nonProxy = nonProxy + "|" + localhost; // NOI18N
            } else {
                // Avoid this error when hostname == localhost:
                // Error in http.nonProxyHosts system property:  sun.misc.REException: localhost is a duplicate
            }
        } catch (UnknownHostException e) {
            // OK. Sometimes a hostname is assigned by DNS, but a computer
            // is later pulled off the network. It may then produce a bogus
            // name for itself which can't actually be resolved. Normally
            // "localhost" is aliased to 127.0.0.1 anyway.
        }
        try {
            String localhost2 = InetAddress.getLocalHost().getCanonicalHostName();
            if (!localhost2.equals("localhost") && !localhost2.equals(localhost)) { // NOI18N
                nonProxy = nonProxy + "|" + localhost2; // NOI18N
            } else {
                // Avoid this error when hostname == localhost:
                // Error in http.nonProxyHosts system property:  sun.misc.REException: localhost is a duplicate
            }
        } catch (UnknownHostException e) {
            // OK. Sometimes a hostname is assigned by DNS, but a computer
            // is later pulled off the network. It may then produce a bogus
            // name for itself which can't actually be resolved. Normally
            // "localhost" is aliased to 127.0.0.1 anyway.
        }
        return nonProxy;
    }

    
}
