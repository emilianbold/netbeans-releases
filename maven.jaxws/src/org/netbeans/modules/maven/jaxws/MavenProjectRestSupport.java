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
package org.netbeans.modules.maven.jaxws;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.spi.WebRestSupport;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.jaxrs.JaxRs;
import org.netbeans.modules.websvc.wsstack.jaxrs.JaxRsStackProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Nam Nguyen
 */
@ProjectServiceProvider(service=RestSupport.class, projectType="org-netbeans-modules-maven")
public class MavenProjectRestSupport extends WebRestSupport {

    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance";   //NOI18N

    public static final String DIRECTORY_DEPLOYMENT_SUPPORTED = "directory.deployment.supported"; // NOI18N
    private static final String TEST_SERVICES_HTML = "test-services.html"; //NOI18N

    String[] classPathTypes = new String[]{
                ClassPath.COMPILE
            };

    /** Creates a new instance of WebProjectRestSupport */
    public MavenProjectRestSupport(Project project) {
        super(project);
    }

    @Override
    public void upgrade() {
        if (!isRestSupportOn()) {
            return;
        }
        try {
            //Fix issue#141595, 154378
//            addSwdpLibrary();

            FileObject ddFO = getDeploymentDescriptor();
            if (ddFO == null) {
                return;
            }

            WebApp webApp = findWebApp();
            if (webApp == null) {
                return;
            }

            Servlet adaptorServlet = getRestServletAdaptor(webApp);
            if (adaptorServlet != null) {
                // Starting with jersey 0.8, the adaptor class is under 
                // com.sun.jersey package instead of com.sun.we.rest package.
                if (REST_SERVLET_ADAPTOR_CLASS_OLD.equals(adaptorServlet.getServletClass())) {
                    adaptorServlet.setServletClass(this.getServletAdapterClass());
                    webApp.write(ddFO);
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    @Override
    public void extendBuildScripts() throws IOException {
    }

    @Override
    public void ensureRestDevelopmentReady() throws IOException {
        addResourceConfigToWebApp();
        addSwdpLibrary();
    }

    @Override
    public void removeRestDevelopmentReadiness() throws IOException {
        removeResourceConfigFromWebApp();
        removeSwdpLibrary(new String[]{ClassPath.COMPILE} );
    }

    @Override
    public boolean isReady() {
        return isRestSupportOn() && hasSwdpLibrary() && hasRestServletAdaptor();
    }

    private boolean platformHasRestLib(J2eePlatform j2eePlatform) {
        if (j2eePlatform != null) {
            WSStack<JaxRs> wsStack = JaxRsStackProvider.getJaxRsStack(j2eePlatform);
            if (wsStack != null) {
                return wsStack.isFeatureSupported(JaxRs.Feature.JAXRS);
            }
        }
        return false;
    }

    @Override
    public boolean hasSwdpLibrary() {
        SourceGroup[] srcGroups = ProjectUtils.getSources(project).getSourceGroups(
        JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (srcGroups.length > 0) {
            ClassPath classPath = ClassPath.getClassPath(srcGroups[0].getRootFolder(), ClassPath.COMPILE);
            FileObject contextFO = classPath.findResource("javax/ws/rs/core/Context.class"); // NOI18N
            return contextFO != null;
        }
        return false;
    }

    private void addSwdpLibrary() throws IOException {
        if (!hasSwdpLibrary()) { //platform does not have swdp library, so add defaults {restapi, restlib}
            Library swdpLibrary = LibraryManager.getDefault().getLibrary(SWDP_LIBRARY);
            if (swdpLibrary != null) {
                addSwdpLibrary(classPathTypes, swdpLibrary);
            }            
        }
    }
//
//    private boolean hasRestServletAdaptor(WebApp webApp) {
//        return getRestServletAdaptor(webApp) != null;
//    }
//
//    private Servlet getRestServletAdaptor(WebApp webApp) {
//        if (webApp != null) {
//            for (Servlet s : webApp.getServlet()) {
//                if (REST_SERVLET_ADAPTOR.equals(s.getServletName())) {
//                    return s;
//                }
//            }
//        }
//        return null;
//    }
//
//    private ServletMapping getRestServletMapping(WebApp webApp) {
//        for (ServletMapping sm : webApp.getServletMapping()) {
//            if (REST_SERVLET_ADAPTOR.equals(sm.getServletName())) {
//                return sm;
//            }
//        }
//        return null;
//    }
//
//    private boolean hasRestServletAdaptor() {
//        try {
//            return hasRestServletAdaptor(getWebApp());
//        } catch (IOException ioe) {
//            Exceptions.printStackTrace(ioe);
//            return false;
//        }
//    }
//
//    private void addResourceConfigToWebApp() throws IOException {
//        FileObject ddFO = getWebXml();
//        WebApp webApp = getWebApp();
//        if (webApp == null) {
//            return;
//        }
//        boolean needsSave = false;
//        try {
//            Servlet adaptorServlet = getRestServletAdaptor(webApp);
//            if (adaptorServlet == null) {
//                adaptorServlet = (Servlet) webApp.createBean("Servlet");
//                adaptorServlet.setServletName(REST_SERVLET_ADAPTOR);
//                adaptorServlet.setServletClass(getServletAdapterClass());
//                adaptorServlet.setLoadOnStartup(BigInteger.valueOf(1));
//                webApp.addServlet(adaptorServlet);
//                needsSave = true;
//            }
//
//            ServletMapping sm = getRestServletMapping(webApp);
//            if (sm == null) {
//                sm = (ServletMapping) webApp.createBean("ServletMapping");
//                sm.setServletName(adaptorServlet.getServletName());
//                sm.setUrlPattern(REST_SERVLET_ADAPTOR_MAPPING);
//                webApp.addServletMapping(sm);
//                needsSave = true;
//            }
//            if (needsSave) {
//                webApp.write(ddFO);
//            }
//        } catch (IOException ioe) {
//            throw ioe;
//        } catch (ClassNotFoundException ex) {
//            throw new IllegalArgumentException(ex);
//        }
//    }
//
//    private void removeResourceConfigFromWebApp() throws IOException {
//        FileObject ddFO = getWebXml();
//        WebApp webApp = getWebApp();
//        if (webApp == null) {
//            return;
//        }
//        boolean needsSave = false;
//        Servlet restServlet = getRestServletAdaptor(webApp);
//        if (restServlet != null) {
//            webApp.removeServlet(restServlet);
//            needsSave = true;
//        }
//        ServletMapping sm = getRestServletMapping(webApp);
//        if (sm != null) {
//            webApp.removeServletMapping(sm);
//            needsSave = true;
//        }
//        if (needsSave) {
//            webApp.write(ddFO);
//        }
//    }

    private FileObject getApplicationContextXml() {
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        FileObject[] fobjs = provider.getSourceRoots();

        if (fobjs.length > 0) {
            FileObject configRoot = fobjs[0];
            FileObject webInf = configRoot.getFileObject("WEB-INF");        //NOI18N

            if (webInf != null) {
                return webInf.getFileObject("applicationContext", "xml");      //NOI18N
            }
        }

        return null;
    }

    @Override
    public Datasource getDatasource(String jndiName) {
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);

        try {
            return provider.getConfigSupport().findDatasource(jndiName);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    @Override
    public boolean isRestSupportOn() {
        return true;
    }
    
    @Override
    public FileObject generateTestClient(File testdir) throws IOException {
        FileObject resourcesFolder =
                project.getProjectDirectory().getFileObject("src/main/resources"); //NOI18N
        if (resourcesFolder != null) {
            FileObject restFolder = resourcesFolder.getFileObject("rest"); //NOI18N
            if (restFolder == null) {
                restFolder = resourcesFolder.createFolder("rest"); //NOI18N
            }
            return generateMavenTester(FileUtil.toFile(restFolder), getBaseURL());
        }
        return null;
    }

    private FileObject generateMavenTester(File testdir, String baseURL) throws IOException {
        String[] replaceKeys1 = {
            "TTL_TEST_RESBEANS", "MSG_TEST_RESBEANS_INFO"
        };
        String[] replaceKeys2 = {
            "MSG_TEST_RESBEANS_wadlErr", "MSG_TEST_RESBEANS_No_AJAX", "MSG_TEST_RESBEANS_Resource",
            "MSG_TEST_RESBEANS_See", "MSG_TEST_RESBEANS_No_Container", "MSG_TEST_RESBEANS_Content",
            "MSG_TEST_RESBEANS_TabularView", "MSG_TEST_RESBEANS_RawView", "MSG_TEST_RESBEANS_ResponseHeaders",
            "MSG_TEST_RESBEANS_Help", "MSG_TEST_RESBEANS_TestButton", "MSG_TEST_RESBEANS_Loading",
            "MSG_TEST_RESBEANS_Status", "MSG_TEST_RESBEANS_Headers", "MSG_TEST_RESBEANS_HeaderName",
            "MSG_TEST_RESBEANS_HeaderValue", "MSG_TEST_RESBEANS_Insert", "MSG_TEST_RESBEANS_NoContents",
            "MSG_TEST_RESBEANS_AddParamButton", "MSG_TEST_RESBEANS_Monitor", "MSG_TEST_RESBEANS_No_SubResources",
            "MSG_TEST_RESBEANS_SubResources", "MSG_TEST_RESBEANS_ChooseMethod", "MSG_TEST_RESBEANS_ChooseMime",
            "MSG_TEST_RESBEANS_Continue", "MSG_TEST_RESBEANS_AdditionalParams", "MSG_TEST_RESBEANS_INFO",
            "MSG_TEST_RESBEANS_Request", "MSG_TEST_RESBEANS_Sent", "MSG_TEST_RESBEANS_Received",
            "MSG_TEST_RESBEANS_TimeStamp", "MSG_TEST_RESBEANS_Response", "MSG_TEST_RESBEANS_CurrentSelection",
            "MSG_TEST_RESBEANS_DebugWindow", "MSG_TEST_RESBEANS_Wadl", "MSG_TEST_RESBEANS_RequestFailed"

        };
        FileObject testFO = copyFileAndReplaceBaseUrl(testdir, TEST_SERVICES_HTML, replaceKeys1, baseURL);
        copyFile(testdir, TEST_RESBEANS_JS, replaceKeys2, false);
        copyFile(testdir, TEST_RESBEANS_CSS);
        copyFile(testdir, TEST_RESBEANS_CSS2);
        copyFile(testdir, "expand.gif");
        copyFile(testdir, "collapse.gif");
        copyFile(testdir, "item.gif");
        copyFile(testdir, "cc.gif");
        copyFile(testdir, "og.gif");
        copyFile(testdir, "cg.gif");
        copyFile(testdir, "app.gif");

        File testdir2 = new File(testdir, "images");
        testdir2.mkdir();
        copyFile(testdir, "images/background_border_bottom.gif");
        copyFile(testdir, "images/pbsel.png");
        copyFile(testdir, "images/bg_gradient.gif");
        copyFile(testdir, "images/pname.png");
        copyFile(testdir, "images/level1_selected-1lvl.jpg");
        copyFile(testdir, "images/primary-enabled.gif");
        copyFile(testdir, "images/masthead.png");
        copyFile(testdir, "images/primary-roll.gif");
        copyFile(testdir, "images/pbdis.png");
        copyFile(testdir, "images/secondary-enabled.gif");
        copyFile(testdir, "images/pbena.png");
        copyFile(testdir, "images/tbsel.png");
        copyFile(testdir, "images/pbmou.png");
        copyFile(testdir, "images/tbuns.png");
        return testFO;
    }

    /*
     * Copy File, as well as replace tokens, overwrite if specified
     */
    private FileObject copyFileAndReplaceBaseUrl(File testdir, String name, String[] replaceKeys, String baseURL) throws IOException {
        FileObject dir = FileUtil.toFileObject(testdir);
        FileObject fo = dir.getFileObject(name);
        if (fo == null) {
            fo = dir.createData(name);
        }
        FileLock lock = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            lock = fo.lock();
            OutputStream os = fo.getOutputStream(lock);
            writer = new BufferedWriter(new OutputStreamWriter(os));
            InputStream is = RestSupport.class.getResourceAsStream("resources/"+name);
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            String lineSep = "\n";//Unix
            if(File.separatorChar == '\\')//Windows
                lineSep = "\r\n";
            String[] replaceValues = null;
            if(replaceKeys != null) {
                replaceValues = new String[replaceKeys.length];
                for(int i=0;i<replaceKeys.length;i++)
                    replaceValues[i] = NbBundle.getMessage(RestSupport.class, replaceKeys[i]);
            }
            while((line = reader.readLine()) != null) {
                for(int i=0;i<replaceKeys.length;i++) {
                    line = line.replaceAll(replaceKeys[i], replaceValues[i]);
                }
                line = line.replace("${BASE_URL}", baseURL);
                writer.write(line);
                writer.write(lineSep);
            }
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
            if (lock != null) lock.releaseLock();
            if (reader != null) {
                reader.close();
            }
        }
        return fo;
    }

    private String getBaseURL() throws IOException {
        String host = "localhost"; //NOI18N
        String port = "8080"; //NOI18N
        String contextRoot = "";
        WebApp webApp = getWebApp();
        if (webApp != null) {
            String servletNames = "";
            String urlPatterns = "";
            int i=0;
            for (ServletMapping mapping : webApp.getServletMapping()) {
                servletNames+=(i>0 ? ",":"")+mapping.getServletName();
                urlPatterns+= (i>0 ? ",":"")+mapping.getUrlPattern();
                i++;
            }
            http://localhost:8084/mavenprojectWeb3/||ServletAdaptor||resources/*
            return getContextRootURL()+"||"+servletNames+"||"+urlPatterns;
        } else {
            throw new IOException("Cannot read web.xml");
        }
    }


    private String getContextRootURL() {
        String portNumber = "8080"; //NOI18N
        String host = "localhost"; //NOI18N
        String contextRoot = "";
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        Deployment.getDefault().getServerInstance(provider.getServerInstanceID());
        String serverInstanceID = provider.getServerInstanceID();
        if (serverInstanceID == null || WSStackUtils.DEVNULL.equals(serverInstanceID)) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(MavenProjectRestSupport.class, "MSG_MissingServer"), NotifyDescriptor.ERROR_MESSAGE));
        } else {
            // getting port and host name
            ServerInstance serverInstance = Deployment.getDefault().getServerInstance(serverInstanceID);
            try {
                ServerInstance.Descriptor instanceDescriptor = serverInstance.getDescriptor();
                if (instanceDescriptor != null) {
                    int port = instanceDescriptor.getHttpPort();
                    if (port>0) {
                        portNumber = String.valueOf(port);
                    }
                    String hostName = instanceDescriptor.getHostname();
                    if (hostName != null) {
                        host = hostName;
                    }
                }
            } catch (InstanceRemovedException ex) {}
        }
        J2eeModuleProvider.ConfigSupport configSupport = provider.getConfigSupport();
        try {
            contextRoot = configSupport.getWebContextRoot();
        } catch (ConfigurationException e) {
            // TODO the context root value could not be read, let the user know about it
        }
        if (contextRoot.length() > 0 && contextRoot.startsWith("/")) { //NOI18N
            contextRoot = contextRoot.substring(1);
        }
        return "http://"+host+":"+portNumber+"/"+ //NOI18N
                (contextRoot.length()>0 ? contextRoot+"/" : ""); //NOI18N
    }
   
}
