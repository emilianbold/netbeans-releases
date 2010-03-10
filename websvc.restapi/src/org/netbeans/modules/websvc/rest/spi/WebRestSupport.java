/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.rest.spi;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.model.api.RestApplicationModel;
import org.netbeans.modules.websvc.rest.model.api.RestApplications;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public abstract class WebRestSupport extends RestSupport {

    public static final String PROP_REST_RESOURCES_PATH = "rest.resources.path";//NOI18N
    public static final String PROP_REST_CONFIG_TYPE = "rest.config.type"; //NOI18N
    public static final String CONFIG_TYPE_IDE = "ide"; //NOI18N
    public static final String CONFIG_TYPE_USER= "user"; //NOI18N
    public static final String CONFIG_TYPE_DD= "dd"; //NOI18N
    public static final String REST_CONFIG_TARGET="generate-rest-config"; //NOI18N
    protected static final String JERSEY_SPRING_JAR_PATTERN = "jersey-spring.*\\.jar";//NOI18N
    protected static final String JERSEY_PROP_PACKAGES = "com.sun.jersey.config.property.packages"; //NOI18N
    protected static final String JERSEY_PROP_PACKAGES_DESC = "Multiple packages, separated by semicolon(;), can be specified in param-value"; //NOI18N

    /** Creates a new instance of WebProjectRestSupport */
    public WebRestSupport(Project project) {
        super(project);
    }

    @Override
    public FileObject getPersistenceXml() {
        PersistenceScope ps = PersistenceScope.getPersistenceScope(getProject().getProjectDirectory());
        if (ps != null) {
            return ps.getPersistenceXml();
        }
        return null;
    }
    /** Get deployment descriptor (DD API root bean)
     *
     * @return WebApp bean
     * @throws java.io.IOException
     */
    public WebApp getWebApp() throws IOException {
        FileObject fo = getWebXml();
        if (fo != null) {
            return DDProvider.getDefault().getDDRoot(fo);
        }
        return null;
    }

    protected WebApp findWebApp() throws IOException {
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            FileObject ddFo = wm.getDeploymentDescriptor();
            if (ddFo != null) {
                return DDProvider.getDefault().getDDRoot(ddFo);
            }
        }
        return null;
    }

    public String getApplicationPathFromDD() throws IOException {
        WebApp webApp = findWebApp();
        if (webApp != null) {
            ServletMapping sm = getRestServletMapping(webApp);
            if (sm != null) {
                String urlPattern = null;
                if (sm instanceof ServletMapping25) {
                    String[] urlPatterns = ((ServletMapping25)sm).getUrlPatterns();
                    if (urlPatterns.length > 0) {
                        urlPattern = urlPatterns[0];
                    }
                } else {
                    urlPattern = sm.getUrlPattern();
                }
                if (urlPattern != null) {
                    if (urlPattern.endsWith("*")) { //NOI18N
                        urlPattern = urlPattern.substring(0, urlPattern.length()-1);
                    }
                    if (urlPattern.endsWith("/")) { //NOI18N
                        urlPattern = urlPattern.substring(0, urlPattern.length()-1);
                    }
                    if (urlPattern.startsWith("/")) { //NOI18N
                        urlPattern = urlPattern.substring(1);
                    }
                    return urlPattern;
                }

            }
        }
        return null;
    }

    public FileObject getDeploymentDescriptor() {
        WebModuleProvider wmp = project.getLookup().lookup(WebModuleProvider.class);
        if (wmp != null) {
            return wmp.findWebModule(project.getProjectDirectory()).getDeploymentDescriptor();
        }
        return null;
    }

    public FileObject getWebXml() throws IOException {
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            FileObject ddFo = wm.getDeploymentDescriptor();
            if (ddFo == null) {
                FileObject webInf = wm.getWebInf();
                if (webInf == null) {
                    FileObject docBase = wm.getDocumentBase();
                    if (docBase != null) {
                        webInf = docBase.createFolder("WEB-INF"); //NOI18N
                    }
                }
                if (webInf != null) {
                    ddFo = DDHelper.createWebXml(wm.getJ2eeProfile(), webInf);
                }
            }
            return ddFo;
        }
        return null;
    }

    public ServletMapping getRestServletMapping(WebApp webApp) {
        String servletName = null;
        for (Servlet s : webApp.getServlet()) {
            String servletClass = s.getServletClass();
            if (REST_SERVLET_ADAPTOR_CLASS.equals(servletClass) || REST_SPRING_SERVLET_ADAPTOR_CLASS.equals(servletClass)) {
                servletName = s.getServletName();
                break;
            }
        }
        if (servletName != null) {
            for (ServletMapping sm : webApp.getServletMapping()) {
                if (servletName.equals(sm.getServletName())) {
                    return sm;
                }
            }
        }
        return null;
    }

    protected boolean hasRestServletAdaptor() {
        try {
            return getRestServletAdaptor(getWebApp()) != null;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return false;
        }
    }

    protected Servlet getRestServletAdaptor(WebApp webApp) {
        if (webApp != null) {
            for (Servlet s : webApp.getServlet()) {
                String servletClass = s.getServletClass();
                if ( REST_SERVLET_ADAPTOR_CLASS.equals(servletClass) ||
                    REST_SPRING_SERVLET_ADAPTOR_CLASS.equals(servletClass) ||
                    REST_SERVLET_ADAPTOR_CLASS_OLD.equals(servletClass)) {
                    return s;
                }
            }
        }
        return null;
    }

    protected Servlet getRestServletAdaptorByName(WebApp webApp, String servletName) {
        if (webApp != null) {
            for (Servlet s : webApp.getServlet()) {
                if (servletName.equals(s.getServletName())) {
                    return s;
                }
            }
        }
        return null;
    }
    
    public void addResourceConfigToWebApp(String resourcePath) throws IOException {
        FileObject ddFO = getWebXml();
        WebApp webApp = getWebApp();
        if (webApp == null) {
            return;
        }
        boolean needsSave = false;
        try {
            Servlet adaptorServlet = getRestServletAdaptor(webApp);
            if (adaptorServlet == null) {
                adaptorServlet = (Servlet) webApp.createBean("Servlet"); //NOI18N
                adaptorServlet.setServletName(REST_SERVLET_ADAPTOR);
                boolean isSpring = hasSpringSupport();
                if (isSpring) {
                    adaptorServlet.setServletClass(REST_SPRING_SERVLET_ADAPTOR_CLASS);
                    InitParam initParam = (InitParam) adaptorServlet.createBean("InitParam"); //NOI18N
                    initParam.setParamName(JERSEY_PROP_PACKAGES);
                    initParam.setParamValue("."); //NOI18N
                    initParam.setDescription(JERSEY_PROP_PACKAGES_DESC);
                    adaptorServlet.addInitParam(initParam);
                } else {
                    adaptorServlet.setServletClass(REST_SERVLET_ADAPTOR_CLASS);
                }
                adaptorServlet.setLoadOnStartup(BigInteger.valueOf(1));
                webApp.addServlet(adaptorServlet);
                needsSave = true;
            }

            String resourcesUrl = resourcePath;
            if (!resourcePath.startsWith("/")) { //NOI18N
                resourcesUrl = "/"+resourcePath; //NOI18N
            }
            if (resourcesUrl.endsWith("/")) { //NOI18N
                resourcesUrl = resourcesUrl+"*"; //NOI18N
            } else if (!resourcesUrl.endsWith("*")) { //NOI18N
                resourcesUrl = resourcesUrl+"/*"; //NOI18N
            }

            ServletMapping sm = getRestServletMapping(webApp);
            if (sm == null) {
                sm = (ServletMapping) webApp.createBean("ServletMapping"); //NOI18N
                sm.setServletName(adaptorServlet.getServletName());
                if (sm instanceof ServletMapping25) {
                    ((ServletMapping25)sm).addUrlPattern(resourcesUrl);
                } else {
                    sm.setUrlPattern(resourcesUrl);
                }
                webApp.addServletMapping(sm);
                needsSave = true;
            } else {
                // check old url pattern
                boolean urlPatternChanged = false;
                if (sm instanceof ServletMapping25) {
                    String[] urlPatterns = ((ServletMapping25)sm).getUrlPatterns();
                    if (urlPatterns.length == 0 || !resourcesUrl.equals(urlPatterns[0])) {
                        urlPatternChanged = true;
                    }
                } else {
                    if (!resourcesUrl.equals(sm.getUrlPattern())) {
                        urlPatternChanged = true;
                    }
                }

                if (urlPatternChanged) {
                    if (sm instanceof ServletMapping25) {
                        String[] urlPatterns = ((ServletMapping25)sm).getUrlPatterns();
                        if (urlPatterns.length>0) {
                            ((ServletMapping25)sm).setUrlPattern(0, resourcesUrl);
                        } else {
                            ((ServletMapping25)sm).addUrlPattern(resourcesUrl);
                        }
                    } else {
                        sm.setUrlPattern(resourcesUrl);
                    }
                    needsSave = true;
                }
            }
            if (needsSave) {
                webApp.write(ddFO);
                logResourceCreation(project);
            }
        } catch (IOException ioe) {
            throw ioe;
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    protected void removeResourceConfigFromWebApp() throws IOException {
        FileObject ddFO = getWebXml();
        WebApp webApp = getWebApp();
        if (webApp == null) {
            return;
        }
        boolean needsSave = false;
        Servlet restServlet = getRestServletAdaptorByName(webApp, REST_SERVLET_ADAPTOR);
        if (restServlet != null) {
            webApp.removeServlet(restServlet);
            needsSave = true;
        }

        for (ServletMapping sm : webApp.getServletMapping()) {
            if (REST_SERVLET_ADAPTOR.equals(sm.getServletName())) {
                webApp.removeServletMapping(sm);
                needsSave = true;
                break;
            }
        }

        if (needsSave) {
            webApp.write(ddFO);
        }
    }
    
    /** log rest resource detection
     *
     * @param prj project instance
     */
    protected void logResourceCreation(Project prj) {
    }

    public List<RestApplication> getRestApplications() {
        RestApplicationModel applicationModel = getRestApplicationsModel();
        if (applicationModel != null) {
            try {
                return applicationModel.runReadAction(new MetadataModelAction<RestApplications, List<RestApplication>>() {

                    public List<RestApplication> run(RestApplications metadata) throws IOException {
                        return metadata.getRestApplications();
                    }
                    });
            } catch (IOException ex) {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    protected String setApplicationConfigProperty(boolean annotationConfigAvailable) {
        ApplicationConfigPanel configPanel = new ApplicationConfigPanel(annotationConfigAvailable);
        DialogDescriptor desc = new DialogDescriptor(configPanel,
                NbBundle.getMessage(WebRestSupport.class, "TTL_ApplicationConfigPanel"));
        DialogDisplayer.getDefault().notify(desc);
        if (NotifyDescriptor.OK_OPTION.equals(desc.getValue())) {
            String configType = configPanel.getConfigType();
            setProjectProperty(WebRestSupport.PROP_REST_CONFIG_TYPE, configType);
            if (WebRestSupport.CONFIG_TYPE_IDE.equals(configType)) {
                String applicationPath = configPanel.getApplicationPath();
                if (applicationPath.startsWith("/")) {
                    applicationPath = applicationPath.substring(1);
                }
                setProjectProperty(WebRestSupport.PROP_REST_RESOURCES_PATH, applicationPath);
            } else if (WebRestSupport.CONFIG_TYPE_DD.equals(configType)) {
                return configPanel.getApplicationPath();
            }
        }
        return null;
    }

    protected void addJerseySpringJar() throws IOException {
        FileObject srcRoot = findSourceRoot();
        if (srcRoot != null) {
            ClassPath cp = ClassPath.getClassPath(srcRoot, ClassPath.COMPILE);
            if (cp.findResource("com/sun/jersey/api/spring/Autowire.class") == null) { //NOI18N
                File jerseyRoot = InstalledFileLocator.getDefault().locate(JERSEY_API_LOCATION, null, false);
                if (jerseyRoot != null && jerseyRoot.isDirectory()) {
                    File[] jerseyJars = jerseyRoot.listFiles(new JerseyFilter(JERSEY_SPRING_JAR_PATTERN));
                    if (jerseyJars != null && jerseyJars.length>0) {
                        URL url = FileUtil.getArchiveRoot(jerseyJars[0].toURI().toURL());
                        ProjectClassPathModifier.addRoots(new URL[] {url}, srcRoot, ClassPath.COMPILE);
                    }
                }
            }
        }
    }

}
