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

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportProvider;
import org.netbeans.modules.websvc.rest.model.api.RestApplicationModel;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.model.spi.RestServicesMetadataModelFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

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
    
    private AntProjectHelper helper;
    private AtomicReference<RestServicesModel> restServicesModel;
    private AtomicReference<RestApplicationModel> restApplicationModel;
    private List<PropertyChangeListener> modelListeners = new ArrayList<PropertyChangeListener>();
    protected final Project project;

    /** Creates a new instance of RestSupport */
    public RestSupport(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Null project");
        }
        this.project = project;
        restServicesModel = new AtomicReference<RestServicesModel>();
        restApplicationModel = new AtomicReference<RestApplicationModel>();
    }
   
    /** 
     * Handles upgrades between different jersey releases.
     * 
     */
    public abstract void upgrade();


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
    
    /**
     * Get persistence.xml file.
     */
    public abstract FileObject getPersistenceXml();

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
                    MiscUtilities.getClassPath(getProject(), ClassPath.BOOT),
                    MiscUtilities.getClassPath(getProject(), ClassPath.COMPILE),
                    MiscUtilities.getClassPath(getProject(), ClassPath.SOURCE),
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
        return MiscUtilities.hasResource(getProject(), REST_SERVLET_ADAPTOR_CLASS.replace('.', '/')+".class") ||
                MiscUtilities.hasResource(getProject(), REST_SERVLET_ADAPTOR_CLASS_2_0.replace('.', '/')+".class");  // NOI18N
    }

    public abstract boolean isRestSupportOn();

    public void setProjectProperty(String name, String value) {
        MiscUtilities.setProjectProperty(getProject(), getAntProjectHelper(), name, value, AntProjectHelper.PROJECT_PROPERTIES_PATH);
    }
    
    public void setPrivateProjectProperty(String name, String value) {
        MiscUtilities.setProjectProperty(getProject(), getAntProjectHelper(), name, value, AntProjectHelper.PRIVATE_PROPERTIES_PATH );
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
                    MiscUtilities.removeProperty(getAntProjectHelper(), propertyNames,
                            AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    MiscUtilities.removeProperty(getAntProjectHelper(), propertyNames,
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
        return MiscUtilities.hasResource(getProject(), "javax/transaction/UserTransaction.class");  // NOI18N
    }
    
    /**
     * Check to see if there is Spring framework support.
     * 
     */
    public boolean hasSpringSupport() {
        return MiscUtilities.hasResource(getProject(), "org/springframework/transaction/annotation/Transactional.class"); // NOI18N
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

    public String getApplicationPath() throws IOException {
        return "webresources"; // default application path
    }
    
    public abstract File getLocalTargetTestRest();
    
    public abstract String getBaseURL() throws IOException;
    
    public abstract void deploy() throws IOException;
    
    public abstract void configure(String... packages) throws IOException;

    protected static class JerseyFilter implements FileFilter {
        private Pattern pattern;

        JerseyFilter(String regexp) {
            pattern = Pattern.compile(regexp);
        }

        public boolean accept(File pathname) {
            return pattern.matcher(pathname.getName()).matches();
        }
    }

    public abstract int getProjectType();
    
    
}

