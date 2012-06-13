/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.*;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.modules.websvc.rest.model.api.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkuchtiak
 */
public abstract class WebRestSupport extends RestSupport {

    
    private static final String GET_REST_RESOURCE_CLASSES = "getRestResourceClasses";//NOI18N
    private static final String GET_CLASSES = "getClasses";                         //NOI18N
    
    public static final String PROP_REST_RESOURCES_PATH = "rest.resources.path";//NOI18N
    public static final String PROP_REST_CONFIG_TYPE = "rest.config.type"; //NOI18N
    public static final String PROP_REST_JERSEY = "rest.jersey.type";      //NOI18N
    
    public static final String CONFIG_TYPE_IDE = "ide"; //NOI18N
    public static final String CONFIG_TYPE_USER= "user"; //NOI18N
    public static final String CONFIG_TYPE_DD= "dd"; //NOI18N
    
    public static final String JERSEY_CONFIG_IDE="ide";         //NOI18N
    public static final String JERSEY_CONFIG_SERVER="server";   //NOI18N
    
    public static final String REST_CONFIG_TARGET="generate-rest-config"; //NOI18N
    protected static final String JERSEY_SPRING_JAR_PATTERN = "jersey-spring.*\\.jar";//NOI18N
    protected static final String JERSEY_PROP_PACKAGES = "com.sun.jersey.config.property.packages"; //NOI18N
    protected static final String JERSEY_PROP_PACKAGES_DESC = "Multiple packages, separated by semicolon(;), can be specified in param-value"; //NOI18N
    
    private volatile PropertyChangeListener restModelListener;
    
    private RequestProcessor REST_APP_MODIFICATION_RP = new RequestProcessor(WebRestSupport.class);


    /** Creates a new instance of WebProjectRestSupport */
    public WebRestSupport(Project project) {
        super(project);
    }

    @Override
    public boolean isRestSupportOn() {
        if (getAntProjectHelper() == null) {
            return false;
        }
        return getProjectProperty(PROP_REST_CONFIG_TYPE) != null;
    }
    
    public void enableRestSupport( RestConfig config ){
        String type =null;
        switch( config){
            case IDE: 
                type= CONFIG_TYPE_IDE;
                break;
            case DD:
                type = CONFIG_TYPE_DD;
                break;
        }
        if ( type!= null ){
            setProjectProperty(PROP_REST_CONFIG_TYPE, type);
        }
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
    
    @Override
    public void upgrade() {
        if (!isRestSupportOn()) {
            return;
        }
        try {
            FileObject ddFO = getDeploymentDescriptor();
            if (ddFO == null) {
                return;
            }

            WebApp webApp = findWebApp();
            if (webApp == null) {
                return;
            }

            Servlet adaptorServlet = getRestServletAdaptorByName(webApp, REST_SERVLET_ADAPTOR);
            if (adaptorServlet != null) {
                // Starting with jersey 0.8, the adaptor class is under 
                // com.sun.jersey package instead of com.sun.we.rest package.
                if (REST_SERVLET_ADAPTOR_CLASS_OLD.equals(adaptorServlet.getServletClass())) {
                    boolean isSpring = hasSpringSupport();
                    if (isSpring) {
                        adaptorServlet.setServletClass(REST_SPRING_SERVLET_ADAPTOR_CLASS);
                        InitParam initParam =
                                (InitParam) adaptorServlet.findBeanByName("InitParam", //NOI18N
                                "ParamName", //NOI18N
                                JERSEY_PROP_PACKAGES);
                        if (initParam == null) {
                            try {
                                initParam = (InitParam) adaptorServlet.createBean("InitParam"); //NOI18N
                                initParam.setParamName(JERSEY_PROP_PACKAGES);
                                initParam.setParamValue("."); //NOI18N
                                initParam.setDescription(JERSEY_PROP_PACKAGES_DESC);
                                adaptorServlet.addInitParam(initParam);
                            } catch (ClassNotFoundException ex) {}
                        }
                    } else {
                        adaptorServlet.setServletClass(REST_SERVLET_ADAPTOR_CLASS);
                    }
                    webApp.write(ddFO);
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
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
    
    public boolean hasServerJerseyLibrary(){
        return getJaxRsStackSupport() != null;
    }
    
    public JaxRsStackSupport getJaxRsStackSupport(){
        return JaxRsStackSupport.getInstance(project);
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
    
    /*
     * Make REST support configuration for current porject state:
     * - Modify Application config class getClasses() method 
     * or 
     * - Update deployment descriptor with Jersey specific options
     */
    @Override
    public void configure(String... packages) throws IOException{
        String configType = getProjectProperty(PROP_REST_CONFIG_TYPE);
        if ( CONFIG_TYPE_DD.equals( configType)){
            configRestPackages(packages);
        }
        else if (CONFIG_TYPE_IDE.equals(configType)) {
            RestApplicationModel restAppModel = getRestApplicationsModel();
            if (restAppModel != null) {
                try {
                    restAppModel.runReadActionWhenReady(
                            new MetadataModelAction<RestApplications, Void>() {

                                @Override
                                public Void run(final RestApplications metadata)
                                        throws IOException 
                                {
                                    List<RestApplication> applications = 
                                            metadata.getRestApplications();
                                    if ( applications!= null && 
                                            !applications.isEmpty())
                                    {
                                        RestApplication application = 
                                                applications.get(0);
                                        String clazz = application.
                                                getApplicationClass();
                                        reconfigApplicationClass(clazz);
                                    }
                                    return null;
                                }
                            });
                } 
                catch (MetadataModelException ex) {
                    Logger.getLogger(WebRestSupport.class.getName()).log(
                            Level.INFO, ex.getLocalizedMessage(), ex);
                } 
            }
        }
    }
    
    public void addResourceConfigToWebApp(String resourcePath) throws IOException {
        FileObject ddFO = getWebXml();
        WebApp webApp = getWebApp();
        if (webApp == null) {
            return;
        }
        if (webApp.getStatus() == webApp.STATE_INVALID_UNPARSABLE) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                        NbBundle.getMessage(WebRestSupport.class, "MSG_InvalidDD", webApp.getError()),
                        NotifyDescriptor.ERROR_MESSAGE));
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
    
    public String getApplicationPath() throws IOException {
        String pathFromDD = getApplicationPathFromDD();
        String applPath = getApplicationPathFromAnnotations(pathFromDD);
        return (applPath == null ? super.getApplicationPath() : applPath);
    }
    
    protected String getApplicationPathFromAnnotations(final String applPathFromDD) {
        List<RestApplication> restApplications = getRestApplications();
        if (applPathFromDD == null) {
            if (restApplications.size() == 0) {
                return null;
            } else {
                return restApplications.get(0).getApplicationPath();
            }
        } else {
            if (restApplications.size() == 0) {
                return applPathFromDD;
            } else {
                for (RestApplication appl: restApplications) {
                    if (applPathFromDD.equals(appl.getApplicationPath())) {
                        return applPathFromDD;
                    }
                }
                return restApplications.get(0).getApplicationPath();
            }
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
                Future<List<RestApplication>> future = applicationModel.
                    runReadActionWhenReady(
                        new MetadataModelAction<RestApplications, List<RestApplication>>() 
                    {
                            public List<RestApplication> run(RestApplications metadata) 
                                throws IOException 
                            {
                                return metadata.getRestApplications();
                            }
                    });
                return future.get();
            } 
            catch (IOException ex) {
                return Collections.emptyList();
            }
            catch (InterruptedException ex) {
                return Collections.emptyList();
            }
            catch (ExecutionException ex) {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    protected RestConfig setApplicationConfigProperty(boolean annotationConfigAvailable) {
        ApplicationConfigPanel configPanel = new ApplicationConfigPanel(
                annotationConfigAvailable, hasServerJerseyLibrary());
        DialogDescriptor desc = new DialogDescriptor(configPanel,
                NbBundle.getMessage(WebRestSupport.class, "TTL_ApplicationConfigPanel"));
        DialogDisplayer.getDefault().notify(desc);
        if (NotifyDescriptor.OK_OPTION.equals(desc.getValue())) {
            String configType = configPanel.getConfigType();
            setProjectProperty(WebRestSupport.PROP_REST_CONFIG_TYPE, configType);
            RestConfig rc = null;
            if (WebRestSupport.CONFIG_TYPE_IDE.equals(configType)) {
                String applicationPath = configPanel.getApplicationPath();
                if (applicationPath.startsWith("/")) {
                    applicationPath = applicationPath.substring(1);
                }
                setProjectProperty(WebRestSupport.PROP_REST_RESOURCES_PATH, applicationPath);
                rc = RestConfig.IDE;
                rc.setResourcePath(applicationPath);
                
            } else if (WebRestSupport.CONFIG_TYPE_DD.equals(configType)) {
                rc = RestConfig.DD;
                rc.setResourcePath(configPanel.getApplicationPath());
            }
            if ( rc!= null ){
                rc.setJerseyLibSelected(configPanel.isJerseyLibSelected());
                rc.setServerJerseyLibSelected(configPanel.isServerJerseyLibSelected()); 
                if ( configPanel.isServerJerseyLibSelected() ){
                    setProjectProperty(PROP_REST_JERSEY, JERSEY_CONFIG_SERVER );
                }
                else if ( configPanel.isJerseyLibSelected()){
                    setProjectProperty(PROP_REST_JERSEY, JERSEY_CONFIG_IDE);
                }
                return rc;
            }
            
        } else {
            setProjectProperty(WebRestSupport.PROP_REST_CONFIG_TYPE, WebRestSupport.CONFIG_TYPE_USER);
            RestConfig rc = RestConfig.USER;
            rc.setJerseyLibSelected(false);
            rc.setServerJerseyLibSelected(false);
            /*if ( configPanel.isServerJerseyLibSelected() ){
                setProjectProperty(PROP_REST_JERSEY, JERSEY_CONFIG_SERVER );
            }
            else if ( configPanel.isJerseyLibSelected()){
                setProjectProperty(PROP_REST_JERSEY, JERSEY_CONFIG_IDE);
            }*/
            return rc;
        }
        return RestConfig.USER;
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

    @Override
    public int getProjectType() {
        return PROJECT_TYPE_WEB;
    }
    
    private String getPackagesList( Iterable<String> packs ) {
        StringBuilder builder = new StringBuilder();
        for (String pack : packs) {
            builder.append( pack);
            builder.append(';');
        }
        String packages ;
        if ( builder.length() >0 ){
            packages  = builder.substring( 0 ,  builder.length() -1 );
        }
        else{
            packages = builder.toString();
        }
        return packages;
    }
    
    private String getPackagesList( String[] packs ) {
        return getPackagesList( Arrays.asList( packs));
    }
    
    private InitParam createJerseyPackagesInitParam( Servlet adaptorServlet,
            String... packs ) throws ClassNotFoundException
    {
        InitParam initParam = (InitParam) adaptorServlet
                .createBean("InitParam"); // NOI18N
        initParam.setParamName(JERSEY_PROP_PACKAGES);
        initParam.setParamValue(getPackagesList(packs));
        initParam.setDescription(JERSEY_PROP_PACKAGES_DESC);
        return initParam;
    }

    public static enum RestConfig {
        IDE,
        USER,
        DD;

        private String resourcePath;
        private boolean jerseyLibSelected;
        private boolean serverJerseyLibSelected;

        public boolean isJerseyLibSelected() {
            return jerseyLibSelected;
        }

        public void setJerseyLibSelected(boolean jerseyLibSelected) {
            this.jerseyLibSelected = jerseyLibSelected;
        }

        public String getResourcePath() {
            return resourcePath;
        }

        public void setResourcePath(String reseourcePath) {
            this.resourcePath = reseourcePath;
        }
        
        public void setServerJerseyLibSelected(boolean isSelected){
            serverJerseyLibSelected = isSelected;
        }
        
        public boolean isServerJerseyLibSelected(){
            return serverJerseyLibSelected;
        }

    }
    
    protected void reconfigApplicationClass( final String appClassFqn ) {
        if ( restModelListener == null ){
            scheduleReconfigAppClass(appClassFqn);
            restModelListener = new PropertyChangeListener() {
                
                @Override
                public void propertyChange( PropertyChangeEvent evt ) {
                    scheduleReconfigAppClass(appClassFqn);
                }
            };
            addModelListener(restModelListener);
        }
    }
    
    private void scheduleReconfigAppClass(final String fqn ){
        Runnable runnable = new Runnable() {
            
            @Override
            public void run() {
                try {
                    doReconfigApplicationClass(fqn);
                }
                catch(IOException e ){
                    Logger.getLogger(WebRestSupport.class.getName()).log(
                            Level.INFO, e.getLocalizedMessage(), e);
                }                
            }
        };
        REST_APP_MODIFICATION_RP.post(runnable);
    }

    protected void doReconfigApplicationClass( String appClassFqn ) throws IOException{
        JavaSource javaSource = getJavaSourceFromClassName(appClassFqn);
        if ( javaSource == null ){
            return;
        }
        javaSource.runModificationTask( new Task<WorkingCopy>() {

            @Override
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                CompilationUnitTree tree = workingCopy.getCompilationUnit();
                for (Tree typeDeclaration : tree.getTypeDecls()){
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDeclaration.getKind())){
                        MethodTree getClasses = null;
                        MethodTree restResources = null;
                        ClassTree classTree = (ClassTree) typeDeclaration;
                        List<? extends Tree> members = classTree.getMembers();
                        for (Tree member : members) {
                            if ( member.getKind().equals(Kind.METHOD)){
                                MethodTree method = (MethodTree)member;
                                String name = method.getName().toString();
                                if ( name.equals(GET_CLASSES)){      
                                    getClasses = method;
                                }
                                else if ( name.equals(GET_REST_RESOURCE_CLASSES)){
                                    restResources = method;
                                }
                            }
                        }
                        TreeMaker maker = workingCopy.getTreeMaker();
                        ClassTree modified = classTree;
                        modified = removeResourcesMethod( restResources,
                                maker, modified);
                        modified = createMethods(getClasses, 
                                maker, modified , restResources== null);

                        workingCopy.rewrite(classTree, modified);
                    }
                }
            }

        }).commit();
    }

    protected FileObject getFileObjectFromClassName(String qualifiedClassName) 
            throws IOException 
    {
        FileObject root = findSourceRoot();
        ClasspathInfo cpInfo = ClasspathInfo.create(root);
        ClassIndex ci = cpInfo.getClassIndex();
        int beginIndex = qualifiedClassName.lastIndexOf('.')+1;
        String simple = qualifiedClassName.substring(beginIndex);
        Set<ElementHandle<TypeElement>> handles = ci.getDeclaredTypes(
                simple, ClassIndex.NameKind.SIMPLE_NAME, 
                Collections.singleton(ClassIndex.SearchScope.SOURCE));
        for (ElementHandle<TypeElement> handle : handles) {
            if (qualifiedClassName.equals(handle.getQualifiedName())) {
                return SourceUtils.getFile(handle, cpInfo);
            }
        }
        return null;
    }
    
    protected JavaSource getJavaSourceFromClassName(String qualifiedClassName)
            throws IOException 
    {
        FileObject fo = getFileObjectFromClassName(qualifiedClassName);
        if (fo != null) {
            return JavaSource.forFileObject(fo);
        } else {
            return null;
        }
    }
    
    private void configRestPackages( String... packs ) throws IOException {
        try {
	    addResourceConfigToWebApp("/webresources/*");
            FileObject ddFO = getWebXml();
            WebApp webApp = getWebApp();
            if (webApp == null) {
                return;
            }
            if (webApp.getStatus() == WebApp.STATE_INVALID_UNPARSABLE) {
                return;
            }
            boolean needsSave = false;
            Servlet adaptorServlet = getRestServletAdaptor(webApp);
	    if ( adaptorServlet == null ){
		return;
	    }
            InitParam[] initParams = adaptorServlet.getInitParam();
            boolean initParamFound = false;
            for (InitParam initParam : initParams) {
                if (initParam.getParamName().equals(JERSEY_PROP_PACKAGES)) {
                    initParamFound = true;
                    String paramValue = initParam.getParamValue();
                    if (paramValue != null) {
                        paramValue = paramValue.trim();
                    }
                    else {
                        paramValue = "";
                    }
                    if (paramValue.length() == 0 || paramValue.equals(".")){ // NOI18N
                        initParam.setParamValue(getPackagesList(packs));
                        needsSave = true;
                    }
                    else {
                        String[] existed = paramValue.split(";");
                        LinkedHashSet<String> set = new LinkedHashSet<String>();
                        set.addAll(Arrays.asList(existed));
                        set.addAll(Arrays.asList(packs));
                        initParam.setParamValue(getPackagesList(set));
                        needsSave = existed.length != set.size();
                    }
                }
            }
            if (!initParamFound) {
                InitParam initParam = createJerseyPackagesInitParam(adaptorServlet,
                        packs);
                adaptorServlet.addInitParam(initParam);
                needsSave = true;
            }
            if (needsSave) {
                webApp.write(ddFO);
                logResourceCreation(project);
            }
	}
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    private ClassTree removeResourcesMethod( MethodTree restResources, 
            TreeMaker maker, ClassTree modified )
    {
        return maker.removeClassMember(modified, restResources);
    }
    
    private ClassTree createMethods( MethodTree getClasses,
            TreeMaker maker,ClassTree modified, boolean addComment) throws IOException
    {
        WildcardTree wildCard = maker.Wildcard(Kind.UNBOUNDED_WILDCARD, 
                null);
        ParameterizedTypeTree wildClass = maker.ParameterizedType(
                maker.QualIdent(Class.class.getCanonicalName()), 
                Collections.singletonList(wildCard));
        ParameterizedTypeTree wildSet = maker.ParameterizedType(
                maker.QualIdent(Set.class.getCanonicalName()), 
                Collections.singletonList(wildClass));
        if ( getClasses == null ){
            ModifiersTree modifiersTree = maker.Modifiers(
                    EnumSet.of(Modifier.PUBLIC));
            MethodTree methodTree = maker.Method(modifiersTree, 
                    GET_CLASSES, wildSet, 
                    Collections.<TypeParameterTree>emptyList(), 
                    Collections.<VariableTree>emptyList(), 
                    Collections.<ExpressionTree>emptyList(), 
                    "{return "+GET_REST_RESOURCE_CLASSES+"();}", null);
            modified = maker.addClassMember(modified, methodTree);
        }
        StringBuilder builder = new StringBuilder();
        collectRestResources(builder);
        ModifiersTree modifiersTree = maker.Modifiers(EnumSet
                .of(Modifier.PRIVATE));
        MethodTree methodTree = maker.Method(modifiersTree,
                GET_REST_RESOURCE_CLASSES, wildSet,
                Collections.<TypeParameterTree> emptyList(),
                Collections.<VariableTree> emptyList(),
                Collections.<ExpressionTree> emptyList(), builder.toString(),
                null);
        if (addComment) {
            Comment comment = Comment.create(Style.JAVADOC, -2, -2, -2,
                    "Do not modify this method. It is "
                            + "automatically generated by "
                            + "NetBeans REST support."); // NOI18N
            maker.addComment(methodTree, comment, true);
        }
        modified = maker.addClassMember(modified, methodTree);
        return modified;
    }

    private void collectRestResources( final StringBuilder builder ) throws IOException {
        builder.append('{');
        builder.append("Set<Class<?>> resources = new java.util.HashSet<Class<?>>();");// NOI18N
        RestServicesModel model = getRestServicesModel();
        try {
            model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>()
            {

                @Override
                public Void run( RestServicesMetadata metadata )
                        throws Exception
                {
                    RestServices services = metadata.getRoot();
                    RestServiceDescription[] descriptions = services.
                        getRestServiceDescription();
                    for (RestServiceDescription description : descriptions){
                        String className = description.getClassName();
                        builder.append("resources.add(");       // NOI18N
                        builder.append( className );
                        builder.append(".class);");             // NOI18N
                    }
                    return null;
                }

            });
        }
        catch (MetadataModelException e) {
            Logger.getLogger(WebRestSupport.class.getName()).log(Level.INFO,
                    e.getLocalizedMessage(), e);
        }
        finally{
            builder.append("return resources;");                // NOI18N
            builder.append('}');
        }
    }
}
