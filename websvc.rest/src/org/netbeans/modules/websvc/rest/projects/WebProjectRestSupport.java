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
package org.netbeans.modules.websvc.rest.projects;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.spi.WebRestSupport;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.api.WSTool;
import org.netbeans.modules.websvc.wsstack.jaxrs.JaxRs;
import org.netbeans.modules.websvc.wsstack.jaxrs.JaxRsStackProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Nam Nguyen
 */
@ProjectServiceProvider(service=RestSupport.class, projectType="org-netbeans-modules-web-project")
public class WebProjectRestSupport extends WebRestSupport {

    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance";   //NOI18N

    public static final String DIRECTORY_DEPLOYMENT_SUPPORTED = "directory.deployment.supported"; // NOI18N

    String[] classPathTypes = new String[] {
                ClassPath.COMPILE
            };

    /** Creates a new instance of WebProjectRestSupport */
    public WebProjectRestSupport(Project project) {
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
            WebApp webApp = getWebApp();

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
        new AntFilesHelper(this).initRestBuildExtension();
    }

    public void ensureRestDevelopmentReady() throws IOException {
        boolean needsRefresh = false;
        if (!isRestSupportOn()) {
            needsRefresh = true;
            setProjectProperty(REST_SUPPORT_ON, "true");
        }

        extendBuildScripts();

        addSwdpLibrary();

        addResourceConfigToWebApp();
        ProjectManager.getDefault().saveProject(getProject());
        if (needsRefresh) {
            refreshRestServicesMetadataModel();
        }
    }

    public void removeRestDevelopmentReadiness() throws IOException {
        removeResourceConfigFromWebApp();
        removeSwdpLibrary(new String[]{
                    ClassPath.COMPILE,
                    ClassPath.EXECUTE
                });
        setProjectProperty(REST_SUPPORT_ON, "false");
        ProjectManager.getDefault().saveProject(getProject());
    }

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
        J2eePlatform platform = getPlatform();
        if (platform == null) {
            return false;
        }
        if(platformHasRestLib(platform)){
            return true;
        }
        boolean hasRestBeansApi = false;
        boolean hasRestBeansImpl = false;
        for (File file : platform.getClasspathEntries()) {
            if (file.getName().equals(REST_API_JAR)) { //NOI18N

                hasRestBeansApi = true;
            }
            if (file.getName().equals(REST_RI_JAR)) { //NOI18N

                hasRestBeansImpl = true;
            }
            if (hasRestBeansApi && hasRestBeansImpl) {
                return true;
            }
        }
        return false;
    }

    public J2eePlatform getPlatform() {
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider == null) {
            return null;
        }
        try {
            return Deployment.getDefault().getServerInstance(j2eeModuleProvider.getServerInstanceID()).getJ2eePlatform();
        } catch (InstanceRemovedException ex) {
            return null;
        }
    }

    private void addSwdpLibrary() throws IOException {

        // check if rest library provided by server corresponds to selected server
        WSStack<JaxRs> wsStack = null;
        J2eeModuleProvider j2eeModuleProvider = null;
        String libName = null;
        J2eePlatform platform = getPlatform();
        if (platform != null) {
            wsStack = JaxRsStackProvider.getJaxRsStack(platform);
            if (wsStack != null) {
                j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
                if (j2eeModuleProvider != null) {
                    libName = getServerRestLibraryName(j2eeModuleProvider);
                    Library oldLibrary = LibraryManager.getDefault().getLibrary(libName);
                    if (oldLibrary != null && !isServerLibrary(libName, j2eeModuleProvider)) {
                        LibraryManager.getDefault().removeLibrary(oldLibrary);
                    }
                }
            }
        }

        // get or create REST library, from selected J2EE server, and add it to project's classpath
        if (!hasSwdpLibrary()) {
            //platform does not have swdp library, so add defaults {restapi, restlib}
            addSwdpLibrary(classPathTypes);
        } else {//add library jars from platform
            if (wsStack != null) { //GF
                if (j2eeModuleProvider != null) {
                    Library swdpLibrary = LibraryManager.getDefault().getLibrary(libName);
                    if (swdpLibrary == null) { //Create one if does not exist
                        WSTool wsTool = wsStack.getWSTool(JaxRs.Tool.JAXRS);
                        if (wsTool != null && wsTool.getLibraries().length > 0) {
                            swdpLibrary = createSwdpLibrary(wsTool.getLibraries(), libName);
                            setServerRestLibraryName(j2eeModuleProvider);
                        }
                    }
                    if (swdpLibrary != null) {
                        addSwdpLibrary(classPathTypes, swdpLibrary);
                    }
                }
            }
        }
    }

    static String getServerRestLibraryName(J2eeModuleProvider j2eeModuleProvider) {
        return "restlib_"+ j2eeModuleProvider.getServerID(); //NOI18N
    }

    static void setServerRestLibraryName(J2eeModuleProvider j2eeModuleProvider) {
        Preferences prefs = NbPreferences.forModule(WebProjectRestSupport.class);
        if (prefs != null) {
            prefs.put("restlib_"+ j2eeModuleProvider.getServerID(), j2eeModuleProvider.getServerInstanceID());
        }
    }

    static boolean isServerLibrary(String libraryName, J2eeModuleProvider j2eeModuleProvider) {
        Preferences prefs = NbPreferences.forModule(WebProjectRestSupport.class);
        if (prefs != null) {
            String oldServerInstanceId = prefs.get(libraryName , null);
            if (oldServerInstanceId != null && oldServerInstanceId.equals(j2eeModuleProvider.getServerInstanceID())) {
                return true;
            }
        }
        return false;
    }

    static J2eePlatform getJ2eePlatform(J2eeModuleProvider j2eeModuleProvider){
        String serverInstanceID = j2eeModuleProvider.getServerInstanceID();
        if(serverInstanceID != null && serverInstanceID.length() > 0) {
            try {
                return Deployment.getDefault().getServerInstance(serverInstanceID).getJ2eePlatform();
            } catch (InstanceRemovedException ex) {
                Logger.getLogger(WebProjectRestSupport.class.getName()).log(Level.INFO, "Failed to find J2eePlatform");
            }
        }
        return null;
    }

    static Library createSwdpLibrary(URL[] libs, final String libraryName) throws IOException {
        // obtain URLs of the jar file
        List <URL> urls = new ArrayList <URL> ();
        for (URL lib:libs) {
            URL url = FileUtil.getArchiveRoot(lib);
            urls.add(url);
        }
        // create new library and register in the Library Manager.
        Map<String, List<URL>> content = Collections.<String, List<URL>>singletonMap ("classpath",urls); //NOI18N
        Library lib = LibraryManager.getDefault().createLibrary("j2se", libraryName, content); //NOI18N
        return lib;
    }

    private FileObject getDeploymentDescriptor() {
        WebModuleProvider wmp = project.getLookup().lookup(WebModuleProvider.class);
        if (wmp != null) {
            return wmp.findWebModule(project.getProjectDirectory()).getDeploymentDescriptor();
        }
        return null;
    }


    @Override
    public FileObject getPersistenceXml() {
        PersistenceScope ps = PersistenceScope.getPersistenceScope(getProject().getProjectDirectory());
        if (ps != null) {
            return ps.getPersistenceXml();
        }
        return null;
    }

    public FileObject getApplicationContextXml() {
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

    public Datasource getDatasource(String jndiName) {
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);

        try {
            return provider.getConfigSupport().findDatasource(jndiName);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    public void setDirectoryDeploymentProperty(Properties p) {
        String instance = getAntProjectHelper().getStandardPropertyEvaluator().getProperty(J2EE_SERVER_INSTANCE);
        if (instance != null) {
            J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
            String sdi = jmp.getServerInstanceID();
            J2eeModule mod = jmp.getJ2eeModule();
            if (sdi != null && mod != null) {
                boolean cFD = Deployment.getDefault().canFileDeploy(instance, mod);
                p.setProperty(DIRECTORY_DEPLOYMENT_SUPPORTED, String.valueOf(cFD)); // NOI18N

            }
        }
    }

   
}
