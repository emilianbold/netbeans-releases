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
import java.math.BigInteger;
import java.util.Properties;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.jaxrs.JaxRs;
import org.netbeans.modules.websvc.wsstack.jaxrs.JaxRsStackProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Nam Nguyen
 */
public class WebProjectRestSupport extends RestSupport {

    public static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance";   //NOI18N

    public static final String DIRECTORY_DEPLOYMENT_SUPPORTED = "directory.deployment.supported"; // NOI18N


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
            addSwdpLibrary(new String[]{ClassPath.COMPILE, ClassPath.EXECUTE});

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

        if (!hasSwdpLibrary()) {
            addSwdpLibrary(new String[]{
                        ClassPath.COMPILE,
                        ClassPath.EXECUTE
                    });
        }
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
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider == null) {
            return false;
        }

        J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(j2eeModuleProvider.getServerInstanceID());
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

    private boolean hasRestServletAdaptor(WebApp webApp) {
        return getRestServletAdaptor(webApp) != null;
    }

    private Servlet getRestServletAdaptor(WebApp webApp) {
        if (webApp != null) {
            for (Servlet s : webApp.getServlet()) {
                if (REST_SERVLET_ADAPTOR.equals(s.getServletName())) {
                    return s;
                }
            }
        }
        return null;
    }

    public static ServletMapping getRestServletMapping(Project project) throws IOException {
        return getRestServletMapping(getWebApp(project));
    }

    public static ServletMapping getRestServletMapping(WebApp webApp) {
        for (ServletMapping sm : webApp.getServletMapping()) {
            if (REST_SERVLET_ADAPTOR.equals(sm.getServletName())) {
                return sm;
            }
        }
        return null;
    }

    private boolean hasRestServletAdaptor() {
        try {
            return hasRestServletAdaptor(getWebApp());
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return false;
        }
    }

    private FileObject getDeploymentDescriptor() {
        WebModuleProvider wmp = project.getLookup().lookup(WebModuleProvider.class);
        if (wmp != null) {
            return wmp.findWebModule(project.getProjectDirectory()).getDeploymentDescriptor();
        }
        return null;
    }

    private void addResourceConfigToWebApp() throws IOException {
        FileObject ddFO = getDeploymentDescriptor();
        WebApp webApp = getWebApp();
        if (webApp == null) {
            return;
        }
        boolean needsSave = false;
        try {
            Servlet adaptorServlet = getRestServletAdaptor(webApp);
            if (adaptorServlet == null) {
                adaptorServlet = (Servlet) webApp.createBean("Servlet");
                adaptorServlet.setServletName(REST_SERVLET_ADAPTOR);
                adaptorServlet.setServletClass(getServletAdapterClass());
                adaptorServlet.setLoadOnStartup(BigInteger.valueOf(1));
                webApp.addServlet(adaptorServlet);
                needsSave = true;
            }

            ServletMapping sm = getRestServletMapping(webApp);
            if (sm == null) {
                sm = (ServletMapping) webApp.createBean("ServletMapping");
                sm.setServletName(adaptorServlet.getServletName());
                sm.setUrlPattern(REST_SERVLET_ADAPTOR_MAPPING);
                webApp.addServletMapping(sm);
                needsSave = true;
            }
            if (needsSave) {
                webApp.write(ddFO);
            }
        } catch (IOException ioe) {
            throw ioe;
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private void removeResourceConfigFromWebApp() throws IOException {
        FileObject ddFO = getDeploymentDescriptor();
        WebApp webApp = getWebApp();
        if (webApp == null) {
            return;
        }
        boolean needsSave = false;
        Servlet restServlet = getRestServletAdaptor(webApp);
        if (restServlet != null) {
            webApp.removeServlet(restServlet);
            needsSave = true;
        }
        ServletMapping sm = getRestServletMapping(webApp);
        if (sm != null) {
            webApp.removeServletMapping(sm);
            needsSave = true;
        }
        if (needsSave) {
            webApp.write(ddFO);
        }
    }

    private WebApp getWebApp() throws IOException {
        return getWebApp(project);
    }

    public static WebApp getWebApp(Project project) throws IOException {
        FileObject fo = getWebXml(project);
        if (fo != null) {
            return DDProvider.getDefault().getDDRoot(fo);
        }
        return null;
    }

    public FileObject getWebXml() {
        return getWebXml(project);
    }

    public static FileObject getWebXml(Project project) {
        WebModuleImplementation jp = (WebModuleImplementation) project.getLookup().lookup(WebModuleImplementation.class);

        return jp.getDeploymentDescriptor();
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
