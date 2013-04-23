/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.websvc.rest.spi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportProvider;
import org.netbeans.modules.websvc.rest.ApplicationSubclassGenerator;
import org.netbeans.modules.websvc.rest.MiscPrivateUtilities;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.model.api.RestApplicationModel;
import org.netbeans.modules.websvc.rest.model.api.RestApplications;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.model.spi.RestServicesMetadataModelFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * All development project type supporting REST framework should provide
 * one instance of this in project lookup.
 *
 * @author Nam Nguyen
 */
public abstract class RestSupport {
    public static final String SWDP_LIBRARY = "restlib"; //NOI18N
    public static final String RESTAPI_LIBRARY = "restapi"; //NOI18N
    protected static final String GFV3_RESTLIB = "restlib_gfv3ee6"; // NOI18N
    protected static final String GFV31_RESTLIB = "restlib_gfv31ee6"; // NOI18N
    public static final String PROP_SWDP_CLASSPATH = "libs.swdp.classpath"; //NOI18N
    public static final String PROP_RESTBEANS_TEST_DIR = "restbeans.test.dir"; //NOI18N
    public static final String PROP_RESTBEANS_TEST_FILE = "restbeans.test.file";//NOI18N
    public static final String PROP_RESTBEANS_TEST_URL = "restbeans.test.url";//NOI18N
    public static final String PROP_BASE_URL_TOKEN = "base.url.token";//NOI18N
    public static final String PROP_APPLICATION_PATH = "rest.application.path";//NOI18N
    public static final String BASE_URL_TOKEN = "___BASE_URL___";//NOI18N
    public static final String RESTBEANS_TEST_DIR = "build/generated-sources/rest-test";//NOI18N
    public static final String COMMAND_TEST_RESTBEANS = "test-restbeans";//NOI18N
    public static final String COMMAND_DEPLOY = "run-deploy";//NOI18N
    public static final String TEST_RESBEANS = "test-resbeans";//NOI18N
    public static final String TEST_RESBEANS_HTML = TEST_RESBEANS + ".html";//NOI18N
    public static final String TEST_RESBEANS_JS = TEST_RESBEANS + ".js";
    public static final String TEST_RESBEANS_CSS = TEST_RESBEANS + ".css";//NOI18N
    public static final String TEST_RESBEANS_CSS2 = "css_master-all.css";//NOI18N
    public static final String REST_SERVLET_ADAPTOR = "ServletAdaptor";//NOI18N
    public static final String REST_SERVLET_ADAPTOR_CLASS = "com.sun.jersey.spi.container.servlet.ServletContainer"; //NOI18N
    public static final String REST_SERVLET_ADAPTOR_CLASS_OLD = "com.sun.ws.rest.impl.container.servlet.ServletAdaptor";  //NOI18N 
    public static final String REST_SERVLET_ADAPTOR_CLASS_2_0 = "org.glassfish.jersey.servlet.ServletContainer"; //NOI18N
    public static final String REST_SPRING_SERVLET_ADAPTOR_CLASS = "com.sun.jersey.spi.spring.container.servlet.SpringServlet";    //NOI18N
    public static final String REST_SERVLET_ADAPTOR_MAPPING = "/resources/*";//NOI18N
    public static final String PARAM_WEB_RESOURCE_CLASS = "webresourceclass";//NOI18N
    public static final String WEB_RESOURCE_CLASS = "webresources.WebResources";//NOI18N
    public static final String REST_API_JAR = "jsr311-api.jar";//NOI18N
    public static final String REST_RI_JAR = "jersey";//NOI18N
    public static final String IGNORE_PLATFORM_RESTLIB = "restlib.ignore.platform";//NOI18N
    public static final String JSR311_JAR_PATTERN = "jsr311-api.*\\.jar";//NOI18N
    public static final String JERSEY_API_LOCATION = "modules/ext/rest";//NOI18N
    public static final String JTA_USER_TRANSACTION_CLASS = "javax/transaction/UserTransaction.class";  //NOI18
    public static final String J2EE_SERVER_TYPE = "j2ee.server.type";       //NOI18N
    public static final String TOMCAT_SERVER_TYPE = "tomcat";       //NOI18N
    public static final String GFV3_SERVER_TYPE = "gfv3";          //NOI18N
    public static final String GFV2_SERVER_TYPE = "J2EE";          //NOI18N

    public static final int PROJECT_TYPE_DESKTOP = 0; //NOI18N
    public static final int PROJECT_TYPE_WEB = 1; //NOI18N
    public static final int PROJECT_TYPE_NB_MODULE = 2; //NOI18N
    private static final String POJO_MAPPING_FEATURE =
            "com.sun.jersey.api.json.POJOMappingFeature";                   // NOI18N

    public static final String PROP_REST_RESOURCES_PATH = "rest.resources.path";//NOI18N
    public static final String PROP_REST_CONFIG_TYPE = "rest.config.type"; //NOI18N
    public static final String PROP_REST_JERSEY = "rest.jersey.type";      //NOI18N

    // IDE generates Application subclass
    public static final String CONFIG_TYPE_IDE = "ide"; //NOI18N
    // user does everything manually
    public static final String CONFIG_TYPE_USER= "user"; //NOI18N
    // Jersey servlet is registered in deployment descriptor
    public static final String CONFIG_TYPE_DD= "dd"; //NOI18N

    public static final String JERSEY_CONFIG_IDE="ide";         //NOI18N
    public static final String JERSEY_CONFIG_SERVER="server";   //NOI18N

    public static final String CONTAINER_RESPONSE_FILTER = "com.sun.jersey.spi.container.ContainerResponseFilters";//NOI18N

    public static final String REST_CONFIG_TARGET="generate-rest-config"; //NOI18N
    protected static final String JERSEY_SPRING_JAR_PATTERN = "jersey-spring.*\\.jar";//NOI18N
    protected static final String JERSEY_PROP_PACKAGES = "com.sun.jersey.config.property.packages"; //NOI18N
    protected static final String JERSEY_PROP_PACKAGES_DESC = "Multiple packages, separated by semicolon(;), can be specified in param-value"; //NOI18N

    private volatile PropertyChangeListener restModelListener;

    private RequestProcessor REST_APP_MODIFICATION_RP = new RequestProcessor(RestSupport.class);
    
    private AntProjectHelper helper;
    private AtomicReference<RestServicesModel> restServicesModel;
    private AtomicReference<RestApplicationModel> restApplicationModel;
    private List<PropertyChangeListener> modelListeners = new ArrayList<PropertyChangeListener>();
    private final Project project;

    private ApplicationSubclassGenerator applicationSubclassGenerator;

    /** Creates a new instance of RestSupport */
    public RestSupport(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Null project");
        }
        this.project = project;
        restServicesModel = new AtomicReference<RestServicesModel>();
        restApplicationModel = new AtomicReference<RestApplicationModel>();
        applicationSubclassGenerator = new ApplicationSubclassGenerator(this);
        REST_APP_MODIFICATION_RP.post(new Runnable() {
            @Override
            public void run() {
                String configType = getProjectProperty(PROP_REST_CONFIG_TYPE);
                if (CONFIG_TYPE_IDE.equals(configType)) {
                    initListener();
                }
            }
        });
    }
   
    /**
     * Ensure the project is ready for REST development.
     * Typical implementation would need to invoke addSwdpLibraries
     * REST development with servlet container would need to add servlet adaptor
     * to web.xml.
     */
    public abstract void ensureRestDevelopmentReady() throws IOException;

    /**
     * Cleanup the project from previously added REST development support artifacts.
     * This should not remove any user source code.
     */
    public abstract void removeRestDevelopmentReadiness() throws IOException;

    /**
     * Is the REST development setup ready: SWDP library added, REST adaptors configured, 
     * generic test client created ?
     */
    public abstract boolean isReady();
    
    /** Get Data Source for JNDI name
     *
     * @param jndiName JNDI name
     * @return
     */
    public abstract Datasource getDatasource(String jndiName);
  
    public void addModelListener(PropertyChangeListener listener) {
        modelListeners.add(listener);
        RestServicesModel model = restServicesModel.get();
        if (model != null) {
            model.addPropertyChangeListener(listener);
        }
    }

    public void removeModelListener(PropertyChangeListener listener) {
        modelListeners.remove(listener);
        RestServicesModel model = restServicesModel.get();
        if (model != null) {
            model.removePropertyChangeListener(listener);
        }
    }
    
    public RestServicesModel getRestServicesModel() {
        FileObject sourceRoot = MiscUtilities.findSourceRoot(getProject());
        if (restServicesModel.get() == null && sourceRoot != null) {
            ClassPathProvider cpProvider = getProject().getLookup().lookup(ClassPathProvider.class);
            if (cpProvider != null) {
                ClassPath compileCP = cpProvider.findClassPath(sourceRoot, ClassPath.COMPILE);
                ClassPath bootCP = cpProvider.findClassPath(sourceRoot, ClassPath.BOOT);
                ClassPath sourceCP = cpProvider.findClassPath(sourceRoot, ClassPath.SOURCE);
                if (compileCP != null && bootCP != null) {
                    MetadataUnit metadataUnit = MetadataUnit.create(
                            bootCP,
                            // TODO: do we need to add JAX-RS 2.0 jar to classpath here?
                            extendWithJsr311Api(compileCP),
                            sourceCP,
                            null);
                    RestServicesModel model = RestServicesMetadataModelFactory.
                            createMetadataModel(metadataUnit, project);
                    if (restServicesModel.compareAndSet(null, model)) {
                        for (PropertyChangeListener pcl : modelListeners) {
                            model.addPropertyChangeListener(pcl);
                        }
                    }
                }
            }
        }
        return restServicesModel.get();
    }

    public RestApplicationModel getRestApplicationsModel() {
        FileObject sourceRoot = MiscUtilities.findSourceRoot(getProject());
        if (restApplicationModel.get() == null && sourceRoot != null) {
            MetadataUnit metadataUnit = MetadataUnit.create(
                    MiscPrivateUtilities.getClassPath(getProject(), ClassPath.BOOT),
                    MiscPrivateUtilities.getClassPath(getProject(), ClassPath.COMPILE),
                    MiscPrivateUtilities.getClassPath(getProject(), ClassPath.SOURCE),
                    null
                    );
            RestApplicationModel model =
                    RestServicesMetadataModelFactory.
                    createApplicationMetadataModel(metadataUnit, project);
            restApplicationModel.compareAndSet(null, model);
        }
        return restApplicationModel.get();
    }

    protected void refreshRestServicesMetadataModel() {
        RestServicesModel model = restServicesModel.get();
        if (model != null) {
            for (PropertyChangeListener pcl : modelListeners) {
                model.removePropertyChangeListener(pcl);
            }
            restServicesModel.compareAndSet( model, null);
        }

        try {
            model = getRestServicesModel();
            if (model != null) {
                model.runReadActionWhenReady(new MetadataModelAction<RestServicesMetadata, Void>() {

                    @Override
                    public Void run(RestServicesMetadata metadata) throws IOException {
                        metadata.getRoot().sizeRestServiceDescription();
                        return null;
                    }
                });
            }
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
        }
    }
    
    private static ClassPath extendWithJsr311Api(ClassPath classPath) {
        if (classPath.findResource("javax/ws/rs/core/Application.class") != null) {
            return classPath;
        }
        File jerseyRoot = InstalledFileLocator.getDefault().locate(JERSEY_API_LOCATION, "org.netbeans.modules.websvc.restlib", false);
        if (jerseyRoot != null && jerseyRoot.isDirectory()) {
            File[] jsr311Jars = jerseyRoot.listFiles(new JerseyFilter(JSR311_JAR_PATTERN));
            if (jsr311Jars != null && jsr311Jars.length>0) {
                FileObject fo = FileUtil.toFileObject(jsr311Jars[0]);
                if (fo != null) {
                    fo = FileUtil.getArchiveRoot(fo);
                    if (fo != null) {
                        return ClassPathSupport.createProxyClassPath(classPath,
                                ClassPathSupport.createClassPath(fo));
                    }
                }
            }
        }
        return classPath;
    }
    
    public abstract FileObject generateTestClient(File testdir, String url) 
        throws IOException; 
    
    /**
     * Generates test client.  Typically RunTestClientAction would need to call 
     * this before invoke the build script target.
     * 
     * @param destDir directory to write test client files in.
     * @param url base url of rest service
     * @return test file object, containing token BASE_URL_TOKEN whether used or not.
     */
    public FileObject generateTestClient(File testdir) throws IOException {        
        
        if (! testdir.isDirectory()) {
            FileUtil.createFolder(testdir);
        }
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
            "MSG_TEST_RESBEANS_DebugWindow", "MSG_TEST_RESBEANS_Wadl", "MSG_TEST_RESBEANS_RequestFailed",
            "MSG_TEST_RESBEANS_NoContent"       // NOI18N
            
        };
        FileObject testFO = MiscUtilities.copyFile(testdir, RestSupport.TEST_RESBEANS_HTML, replaceKeys1, true);
        MiscUtilities.copyFile(testdir, RestSupport.TEST_RESBEANS_JS, replaceKeys2, false);
        MiscUtilities.copyFile(testdir, RestSupport.TEST_RESBEANS_CSS);
        MiscUtilities.copyFile(testdir, RestSupport.TEST_RESBEANS_CSS2);
        MiscUtilities.copyFile(testdir, "expand.gif");
        MiscUtilities.copyFile(testdir, "collapse.gif");
        MiscUtilities.copyFile(testdir, "item.gif");
        MiscUtilities.copyFile(testdir, "cc.gif");
        MiscUtilities.copyFile(testdir, "og.gif");
        MiscUtilities.copyFile(testdir, "cg.gif");
        MiscUtilities.copyFile(testdir, "app.gif");

        File testdir2 = new File(testdir, "images");
        testdir2.mkdir();
        MiscUtilities.copyFile(testdir, "images/background_border_bottom.gif");
        MiscUtilities.copyFile(testdir, "images/pbsel.png");
        MiscUtilities.copyFile(testdir, "images/bg_gradient.gif");
        MiscUtilities.copyFile(testdir, "images/pname.png");
        MiscUtilities.copyFile(testdir, "images/level1_deselect.jpg");
        MiscUtilities.copyFile(testdir, "images/level1_selected-1lvl.jpg");
        MiscUtilities.copyFile(testdir, "images/primary-enabled.gif");
        MiscUtilities.copyFile(testdir, "images/masthead.png");
        MiscUtilities.copyFile(testdir, "images/masthead_link_enabled.gif");
        MiscUtilities.copyFile(testdir, "images/masthead_link_roll.gif");
        MiscUtilities.copyFile(testdir, "images/primary-roll.gif");
        MiscUtilities.copyFile(testdir, "images/pbdis.png");
        MiscUtilities.copyFile(testdir, "images/secondary-enabled.gif");
        MiscUtilities.copyFile(testdir, "images/pbena.png");
        MiscUtilities.copyFile(testdir, "images/tbsel.png");
        MiscUtilities.copyFile(testdir, "images/pbmou.png");
        MiscUtilities.copyFile(testdir, "images/tbuns.png");
        return testFO;
    }
    
    public void removeSwdpLibrary(String[] classPathTypes) throws IOException {
        JaxRsStackSupport.getDefault().removeJaxRsLibraries( project );
    }
    
    public Project getProject() {
        return project;
    }

    /**
     * Should be overridden by sub-classes
     */
    public boolean hasSwdpLibrary() {
        return MiscPrivateUtilities.hasResource(getProject(), REST_SERVLET_ADAPTOR_CLASS.replace('.', '/')+".class") ||
                MiscPrivateUtilities.hasResource(getProject(), REST_SERVLET_ADAPTOR_CLASS_2_0.replace('.', '/')+".class");  // NOI18N
    }

    public void setProjectProperty(String name, String value) {
        MiscPrivateUtilities.setProjectProperty(getProject(), getAntProjectHelper(), name, value, AntProjectHelper.PROJECT_PROPERTIES_PATH);
    }
    
    public void setPrivateProjectProperty(String name, String value) {
        MiscPrivateUtilities.setProjectProperty(getProject(), getAntProjectHelper(), name, value, AntProjectHelper.PRIVATE_PROPERTIES_PATH );
    }

    public String getProjectProperty(String name) {
        if (getAntProjectHelper() == null) {
            return null;
        }
        return helper.getStandardPropertyEvaluator().getProperty(name);
    }

    public void removeProjectProperties(final String[] propertyNames) {
        if (getAntProjectHelper() == null) {
            return;
        }   
        try {
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            @Override
            public Object run() throws IOException {
                // and save the project
                try {
                    MiscPrivateUtilities.removeProperty(getAntProjectHelper(), propertyNames,
                            AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    MiscPrivateUtilities.removeProperty(getAntProjectHelper(), propertyNames,
                            AntProjectHelper.PRIVATE_PROPERTIES_PATH );
                    ProjectManager.getDefault().saveProject(getProject());
                } 
                catch(IOException ioe) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, ioe.getLocalizedMessage(), ioe);
                }
                return null;
            }
        });
        }
        catch (MutexException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
        } 
        
    }

    protected boolean ignorePlatformRestLibrary() {
        String v = getProjectProperty(IGNORE_PLATFORM_RESTLIB);
        Boolean ignore = v != null ? Boolean.valueOf(v) : true;
        return !ignore;
    }
    
    public AntProjectHelper getAntProjectHelper() {
        if (helper == null) {
            JAXWSSupportProvider provider = project.getLookup().lookup(JAXWSSupportProvider.class);
            if (provider != null) {
                JAXWSSupport support = provider.findJAXWSSupport(project.getProjectDirectory());
                if (support != null) {
                    helper = support.getAntProjectHelper();
                }
            }
        }
        return helper;
    }

    /**
     * Check to see if there is JTA support.
     */
    public boolean hasJTASupport() {
        return MiscPrivateUtilities.hasResource(getProject(), "javax/transaction/UserTransaction.class");  // NOI18N
    }
    
    /**
     * Check to see if there is Spring framework support.
     * 
     */
    public boolean hasSpringSupport() {
        return MiscPrivateUtilities.hasResource(getProject(), "org/springframework/transaction/annotation/Transactional.class"); // NOI18N
    }
    
    public String getServerType() {
        return getProjectProperty(J2EE_SERVER_TYPE);
    }

    public boolean isServerTomcat() {
        String serverType = getServerType();

        if (serverType != null) {
            return serverType.toLowerCase().contains(TOMCAT_SERVER_TYPE);
        }

        return false;
    }

    public boolean isServerGFV3() {
        if ( getServerType() == null ){
            return false;
        }
        return getServerType().startsWith(GFV3_SERVER_TYPE);
    }

    public boolean isServerGFV2() {
        return GFV2_SERVER_TYPE.equals(getServerType());
    }

    public abstract File getLocalTargetTestRest();
    
    public abstract String getBaseURL() throws IOException;
    
    public abstract void deploy() throws IOException;
    
    protected static class JerseyFilter implements FileFilter {
        private Pattern pattern;

        JerseyFilter(String regexp) {
            pattern = Pattern.compile(regexp);
        }

        public boolean accept(File pathname) {
            return pattern.matcher(pathname.getName()).matches();
        }
    }

    /////////////////// USED TO BE WebRestSupport:

    public boolean isRestSupportOn() {
        if (getAntProjectHelper() == null) {
            return false;
        }
        return getProjectProperty(PROP_REST_CONFIG_TYPE) != null;
    }

    public void enableRestSupport( RestConfig config ){
        String type =null;
        if ( config== null){
            return;
        }
        switch( config){
            case IDE:
                type= CONFIG_TYPE_IDE;
                break;
            case DD:
                type = CONFIG_TYPE_DD;
                JaxRsStackSupport support = getJaxRsStackSupport();
                boolean added = false;
                if ( support != null ){
                    added = support.extendsJerseyProjectClasspath(project);
                }
                if ( !added ){
                    JaxRsStackSupport.getDefault().extendsJerseyProjectClasspath(project);
                }
                break;
            default:
        }
        if ( type!= null ){
            setProjectProperty(PROP_REST_CONFIG_TYPE, type);
        }
    }

    /**
     * Get persistence.xml file.
     */
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

    private WebApp findWebApp() throws IOException {
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

    /**
     * Handles upgrades between different jersey releases.
     *
     */
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
                } else if (REST_SERVLET_ADAPTOR_CLASS.equals(adaptorServlet.getServletClass())) {
                    if (isJersey2()) {
                        // upgrading from Jersey 1.x to 2.x is too complicated to handle it here;
                        // user will likely need to create subclass of Application and use
                        // it instead of web.xml parameters
                        //adaptorServlet.setServletClass(REST_SERVLET_ADAPTOR_CLASS_2_0);
                        //webApp.write(ddFO);
                    }
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public void addInitParam( String paramName, String value ) {
        try {
            FileObject ddFO = getWebXml();
            WebApp webApp = findWebApp();
            if (ddFO == null || webApp == null) {
                return;
            }
            Servlet adaptorServlet = getRestServletAdaptorByName(webApp,
                    REST_SERVLET_ADAPTOR);
            InitParam initParam = (InitParam) adaptorServlet.findBeanByName(
                    "InitParam", // NOI18N
                    "ParamName", // NOI18N
                    paramName);
            if (initParam == null) {
                try {
                    initParam = (InitParam) adaptorServlet
                            .createBean("InitParam"); // NOI18N
                    adaptorServlet.addInitParam(initParam);
                }
                catch (ClassNotFoundException ex) {
                }
            }
            initParam.setParamName(paramName);
            initParam.setParamValue(value);

            webApp.write(ddFO);
        }
        catch (IOException e) {
            Logger.getLogger(RestSupport.class.getName()).log(Level.WARNING,  null , e);
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
            if (REST_SERVLET_ADAPTOR_CLASS.equals(servletClass) || REST_SPRING_SERVLET_ADAPTOR_CLASS.equals(servletClass) ||
                    REST_SERVLET_ADAPTOR_CLASS_2_0.equals(servletClass)) {
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

    /**
     * Returns true if JAX-RS APIs are available for the project. That means if
     * project's profile is EE7 (web profile or full) or EE6 (full).
     */
    public boolean hasJaxRsApi(){
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if ( webModule == null ){
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        boolean isJee6 = Profile.JAVA_EE_6_WEB.equals(profile) ||
                Profile.JAVA_EE_6_FULL.equals(profile);
        boolean isJee7 = Profile.JAVA_EE_7_WEB.equals(profile) ||
                        Profile.JAVA_EE_7_FULL.equals(profile);
        // Fix for BZ#216345: JAVA_EE_6_WEB profile doesn't contain JAX-RS API
        return (isJee6 && supportsTargetProfile(Profile.JAVA_EE_6_FULL)) || isJee7;
    }

    private boolean supportsTargetProfile(Profile profile){
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().
                lookup(J2eeModuleProvider.class);
        String serverInstanceID = provider.getServerInstanceID();
        if ( serverInstanceID == null ){
            return false;
        }
        ServerInstance serverInstance = Deployment.getDefault().
                 getServerInstance(serverInstanceID);
        try {
            Set<Profile> profiles = serverInstance.getJ2eePlatform().getSupportedProfiles();
            return profiles.contains( profile);
        }
        catch( InstanceRemovedException e ){
            return false;
        }
    }

    public boolean isEE7(){
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if ( webModule == null ){
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        return Profile.JAVA_EE_7_WEB.equals(profile) ||
                        Profile.JAVA_EE_7_FULL.equals(profile);
    }

    public Servlet getRestServletAdaptor(WebApp webApp) {
        if (webApp != null) {
            for (Servlet s : webApp.getServlet()) {
                String servletClass = s.getServletClass();
                if ( REST_SERVLET_ADAPTOR_CLASS.equals(servletClass) ||
                    REST_SPRING_SERVLET_ADAPTOR_CLASS.equals(servletClass) ||
                    REST_SERVLET_ADAPTOR_CLASS_2_0.equals(servletClass) ||
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
    public void configure(String... packages) throws IOException {
        String configType = getProjectProperty(PROP_REST_CONFIG_TYPE);
        if ( CONFIG_TYPE_DD.equals( configType)){
            configRestPackages(packages);
        }
        else if (CONFIG_TYPE_IDE.equals(configType)) {
            initListener();
            scheduleReconfigAppClass();
        }
    }

    public void addResourceConfigToWebApp(String resourcePath) throws IOException {
        FileObject ddFO = getWebXml();
        WebApp webApp = getWebApp();
        if (webApp == null) {
            return;
        }
        if (webApp.getStatus() == WebApp.STATE_INVALID_UNPARSABLE ||
                webApp.getStatus() == WebApp.STATE_INVALID_OLD_VERSION)
        {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                        NbBundle.getMessage(RestSupport.class, "MSG_InvalidDD", webApp.getError()),
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
                    if (isJersey2()) {
                        throw new IllegalStateException("this should not be needed!"); //
                        //adaptorServlet.setServletClass(REST_SERVLET_ADAPTOR_CLASS_2_0);
                    } else {
                        adaptorServlet.setServletClass(REST_SERVLET_ADAPTOR_CLASS);
                    }
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
        return (applPath == null ? "webresources" : applPath);
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
                NbBundle.getMessage(RestSupport.class, "TTL_ApplicationConfigPanel"));
        DialogDisplayer.getDefault().notify(desc);
        if (NotifyDescriptor.OK_OPTION.equals(desc.getValue())) {
            String configType = configPanel.getConfigType();
            setProjectProperty(RestSupport.PROP_REST_CONFIG_TYPE, configType);
            RestConfig rc = null;
            if (RestSupport.CONFIG_TYPE_IDE.equals(configType)) {
                String applicationPath = configPanel.getApplicationPath();
                if (applicationPath.startsWith("/")) {
                    applicationPath = applicationPath.substring(1);
                }
                setProjectProperty(RestSupport.PROP_REST_RESOURCES_PATH, applicationPath);
                rc = RestConfig.IDE;
                rc.setResourcePath(applicationPath);

            } else if (RestSupport.CONFIG_TYPE_DD.equals(configType)) {
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
            setProjectProperty(RestSupport.PROP_REST_CONFIG_TYPE, RestSupport.CONFIG_TYPE_USER);
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
        FileObject srcRoot = MiscUtilities.findSourceRoot(getProject());
        if (srcRoot != null) {
            ClassPath cp = ClassPath.getClassPath(srcRoot, ClassPath.COMPILE);
            if (cp ==null ||cp.findResource(
                    "com/sun/jersey/api/spring/Autowire.class") == null)        //NOI18N
            {
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

    public boolean isJersey2() {
        if (MiscPrivateUtilities.hasResource(getProject(), "org.glassfish.jersey.servlet.ServletContainer")) {
            return true;
        }
        JaxRsStackSupport support = getJaxRsStackSupport();
        if (support != null){
            return support.isBundled("org.glassfish.jersey.servlet.ServletContainer");
        }
        return false;
    }

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
        return createInitParam(adaptorServlet, JERSEY_PROP_PACKAGES,
                getPackagesList(packs), JERSEY_PROP_PACKAGES_DESC);
    }

    private InitParam createInitParam( Servlet adaptorServlet, String name,
            String value , String description ) throws ClassNotFoundException
    {
        InitParam initParam = (InitParam) adaptorServlet
                .createBean("InitParam"); // NOI18N
        initParam.setParamName(name);
        initParam.setParamValue(value);
        if ( description != null ){
            initParam.setDescription(description);
        }
        return initParam;
    }

    public static enum RestConfig {
        // Application subclass registration:
        IDE,
        USER,
        // web.xml deployment descriptor registration
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

    private void initListener() {
        if ( restModelListener == null ){
            restModelListener = new PropertyChangeListener() {

                @Override
                public void propertyChange( PropertyChangeEvent evt ) {
                    scheduleReconfigAppClass();
                }
            };
            addModelListener(restModelListener);
        }
    }

    private void scheduleReconfigAppClass(){
        applicationSubclassGenerator.refreshApplicationSubclass();
    }

    private void configRestPackages( String... packs ) throws IOException {
        try {
            addResourceConfigToWebApp("/webresources/*");           // NOI18N
            FileObject ddFO = getWebXml();
            WebApp webApp = getWebApp();
            if (webApp == null) {
                return;
            }
            if (webApp.getStatus() == WebApp.STATE_INVALID_UNPARSABLE ||
                     webApp.getStatus() == WebApp.STATE_INVALID_OLD_VERSION)
            {
                return;
            }
            boolean needsSave = false;
            Servlet adaptorServlet = getRestServletAdaptor(webApp);
            if ( adaptorServlet == null ){
                return;
            }
            InitParam[] initParams = adaptorServlet.getInitParam();
            boolean jerseyParamFound = false;
            boolean jacksonParamFound = false;
            for (InitParam initParam : initParams) {
                if (initParam.getParamName().equals(JERSEY_PROP_PACKAGES)) {
                    jerseyParamFound = true;
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
                else if ( initParam.getParamName().equals( POJO_MAPPING_FEATURE)){
                    jacksonParamFound = true;
                    String paramValue = initParam.getParamValue();
                    if (paramValue != null) {
                        paramValue = paramValue.trim();
                    }
                    if ( !Boolean.TRUE.toString().equals(paramValue)){
                        initParam.setParamValue(Boolean.TRUE.toString());
                        needsSave = true;
                    }
                }
            }
            if (!jerseyParamFound) {
                InitParam initParam = createJerseyPackagesInitParam(adaptorServlet,
                        packs);
                adaptorServlet.addInitParam(initParam);
                needsSave = true;
            }
            if ( !jacksonParamFound ){
                InitParam initParam = createInitParam(adaptorServlet,
                        POJO_MAPPING_FEATURE, Boolean.TRUE.toString(), null);
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


}

