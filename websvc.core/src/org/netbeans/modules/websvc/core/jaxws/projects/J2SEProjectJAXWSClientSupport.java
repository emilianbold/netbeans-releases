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

package org.netbeans.modules.websvc.core.jaxws.projects;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.persistence.api.PersistenceLocation;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.support.SourceGroups;
import org.netbeans.modules.websvc.spi.jaxws.client.ProjectJAXWSClientSupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
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
    private static final String JAXWS_ENDORSED = "jaxws.endorsed.dir"; // NOI18N
    private static final String ENDORSED_OPTION = "-Djava.endorsed.dirs=\"${jaxws.endorsed.dir}\""; //NOI18N
    
    /** Creates a new instance of J2SEProjectJAXWSClientSupport */
    public J2SEProjectJAXWSClientSupport(Project project) {
        super(project);
        this.project=project;
    }

    public FileObject getWsdlFolder(boolean create) throws IOException {
        if (create) {
            FileObject metaInfDir = PersistenceLocation.createLocation(project);
            if (metaInfDir != null) {
                return FileUtil.createFolder(metaInfDir, WSDL_FOLDER);
            }
        } else {
            FileObject metaInfDir = PersistenceLocation.getLocation(project);
            if (metaInfDir != null) {
                return metaInfDir.getFileObject(WSDL_FOLDER);
            }
        }
        return null;
    }

    public String addServiceClient(String clientName, String wsdlUrl, String packageName, boolean isJsr109) {
        
        // call the super.addServiceClient();
        String serviceIdeName = super.addServiceClient(clientName, wsdlUrl, packageName, false);
        
        // add JVM Proxy Options to project's JVM
        if (serviceIdeName!=null) addJVMOptions(clientName);
        
        return serviceIdeName;
    }
    
    protected void addJaxWs20Library() {
        ClassPath classPath = null;
        SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);
        if (sourceGroups!=null && sourceGroups.length>0) {
            FileObject srcRoot = sourceGroups[0].getRootFolder();
            ClassPath compileClassPath = ClassPath.getClassPath(srcRoot,ClassPath.COMPILE);
            ClassPath bootClassPath = ClassPath.getClassPath(srcRoot,ClassPath.BOOT);
            classPath = ClassPathSupport.createProxyClassPath(new ClassPath[]{compileClassPath, bootClassPath});
        }
        FileObject webServiceClass=null;
        if (classPath!=null) {
            webServiceClass = classPath.findResource("javax/xml/ws/WebServiceFeature.class"); // NOI18N
        }
        if (webServiceClass==null) {
            // add Metro library if WebServiceFeature.class
            Library metroLib = LibraryManager.getDefault().getLibrary("metro"); //NOI18N
            if ((sourceGroups.length > 0) && (metroLib != null)) {
                try {
                    ProjectClassPathModifier.addLibraries(
                            new Library[] {metroLib},
                            sourceGroups[0].getRootFolder(),
                            ClassPath.COMPILE);
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
    
    private void addJVMOptions (final String clientName) {
        ProjectManager.mutex().writeAccess ( new Runnable () {
            public void run() {
                EditableProperties ep = null;
                EditableProperties ep1 = null;
                try {
                    ep = WSUtils.getEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    ep1 = WSUtils.getEditableProperties(project, AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    assert ep != null;
                    assert ep1 != null;
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
                
                boolean endorsedModif = modifyEndorsedOption(ep,ep1);
                boolean proxyModif = addJVMProxyOptions(ep);
                
                if (endorsedModif || proxyModif)
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
        });
    }
    
    private boolean addJVMProxyOptions(EditableProperties ep) {
        assert ep != null;
        boolean modif=false;
        final String proxyHost = System.getProperty(KEY_PROXY_HOST);
        if (proxyHost!=null && proxyHost.length()>0) {
            String proxyPort = System.getProperty(KEY_PROXY_PORT);
            if (proxyPort==null || proxyPort.length()==0) proxyPort="8080"; //NOI18N
            String localHosts ="";
            localHosts = getDefaultNonProxyHosts();            
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
            }
        }
        return modif;
    }
    
    // add/remove java.endorsed.dirs JVM Options
    private boolean modifyEndorsedOption(EditableProperties projectProp, EditableProperties privateProp) {
        assert projectProp != null;
        assert privateProp != null;
        String java_version = System.getProperty("java.version"); //NOI18N
        if (java_version == null) return false;
        String endorsed = privateProp.getProperty(JAXWS_ENDORSED);
        boolean modif = false;
        if (isOldJdk16(java_version) && endorsed != null) { //NOI18N
            // create or modify JVM options
            String jvmOptions = projectProp.getProperty(RUN_JVM_ARGS);
            if (jvmOptions == null) {                   
                modif = true;
                jvmOptions = ENDORSED_OPTION;
            } else if (jvmOptions.indexOf("-Djava.endorsed.dirs") == -1) { //NOI18N
                // specify the endorsed property only if not already specified
                jvmOptions = ENDORSED_OPTION + " "+ jvmOptions;
                modif = true;

            }
            if (modif) {
                projectProp.setProperty(RUN_JVM_ARGS,jvmOptions); 
            }
        } else {
            // remove endorsed option from JVM Options  
            String jvmOptions = projectProp.getProperty(RUN_JVM_ARGS);
            if (jvmOptions != null) {
                if (jvmOptions.indexOf(ENDORSED_OPTION) >=0) {
                    // remove JVMOption
                    StringTokenizer options = new StringTokenizer(jvmOptions);
                    StringBuffer newJvmOptions = new StringBuffer();
                    boolean first = true;
                    while (options.hasMoreTokens()) {
                        String token = options.nextToken();
                        if (!ENDORSED_OPTION.equals(token)) {
                            if (!first) {
                                newJvmOptions.append(" "); // options must be separated by space
                            }
                            newJvmOptions.append(token);
                            first = false;
                        }
                    }
                    projectProp.setProperty(RUN_JVM_ARGS,newJvmOptions.toString());
                    modif = true;
                }
            }
        }
        return modif;
    }
    
    /** test if jdk version is 1.6 and older than jdk1.6.0_04 
     * 
     * @param java_version
     * @return
     */
    private boolean isOldJdk16(String java_version) {
        if (java_version.startsWith("1.6.0")) { //NOI18N
            int index = java_version.indexOf("_");
            if (index > 0) {
                String releaseVersion = java_version.substring(index+1);
                StringTokenizer tokens = new StringTokenizer(releaseVersion,"-_. "); //NOI18N
                String updateVersion = tokens.nextToken();
                if (updateVersion != null) {
                    try {
                        Integer rv = Integer.valueOf(updateVersion);
                        if (rv >=4) return false;
                        else return true;
                    } catch (NumberFormatException ex) {
                        // return true for some strange jdk versions
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                // return true for some strange jdk versions
                return false;
            }
        } else {
            return false;
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
