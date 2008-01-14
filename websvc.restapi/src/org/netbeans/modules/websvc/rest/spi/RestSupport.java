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
package org.netbeans.modules.websvc.rest.spi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportProvider;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.model.spi.RestServicesMetadataModelFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.modules.InstalledFileLocator;

/**
 * All development project type supporting REST framework should provide
 * one instance of this in project lookup.
 *
 * @author Nam Nguyen
 */
public abstract class RestSupport {
    public static final String SWDP_LIBRARY = "restlib"; //NOI18N
    public static final String RESTAPI_LIBRARY = "restapi"; //NOI18N
    public static final String PROP_SWDP_CLASSPATH = "libs.swdp.classpath"; //NOI18N
    public static final String PROP_RESTBEANS_TEST_DIR = "restbeans.test.dir";
    public static final String PROP_RESTBEANS_TEST_FILE = "restbeans.test.file";
    public static final String PROP_RESTBEANS_TEST_URL = "restbeans.test.url";
    public static final String PROP_BASE_URL_TOKEN = "base.url.token";
    public static final String BASE_URL_TOKEN = "___BASE_URL___";
    public static final String RESTBEANS_TEST_DIR = "build/generated/rest-test";
    public static final String COMMAND_TEST_RESTBEANS = "test-restbeans";
    public static final String REST_SUPPORT_ON = "rest.support.on";
    public static final String TEST_RESBEANS = "test-resbeans";
    public static final String TEST_RESBEANS_HTML = TEST_RESBEANS + ".html";
    public static final String TEST_RESBEANS_JS = TEST_RESBEANS + ".js";
    public static final String TEST_RESBEANS_CSS = TEST_RESBEANS + ".css";
    public static final String REST_SERVLET_ADAPTOR = "ServletAdaptor";
    public static final String REST_SERVLET_ADAPTOR_CLASS = "com.sun.ws.rest.impl.container.servlet.ServletAdaptor";   
    public static final String REST_SERVLET_ADAPTOR_MAPPING = "/resources/*";
    public static final String PARAM_WEB_RESOURCE_CLASS = "webresourceclass";
    public static final String WEB_RESOURCE_CLASS = "webresources.WebResources";
    public static final String REST_API_JAR = "jsr311-api.jar";
    public static final String REST_RI_JAR = "jersey.jar";
    public static final String IGNORE_PLATFORM_RESTLIB = "restlib.ignore.platform";
    public static final String JSR311_API_LOCATION = "modules/ext/rest/jsr311-api.jar";
    
    private AntProjectHelper helper;
    protected RestServicesModel restServicesModel;
    private List<PropertyChangeListener> modelListeners = new ArrayList<PropertyChangeListener>();
    protected final Project project;

    /** Creates a new instance of RestSupport */
    public RestSupport(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Null project");
        }
        this.project = project;
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
    
    /**
     * Get persistence.xml file.
     */
    public abstract FileObject getPersistenceXml();
    
    public FileObject findSourceRoot() {
        return findSourceRoot(getProject());
    }
    
    public static FileObject findSourceRoot(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups != null && sourceGroups.length > 0) {
            return sourceGroups[0].getRootFolder();
        }
        return null;
    }

    public void addModelListener(PropertyChangeListener listener) {
        modelListeners.add(listener);
        if (restServicesModel != null) {
            restServicesModel.addPropertyChangeListener(listener);
        }
    }

    public void removeModelListener(PropertyChangeListener listener) {
        modelListeners.remove(listener);
        if (restServicesModel != null) {
            restServicesModel.removePropertyChangeListener(listener);
        }
    }
    
    public RestServicesModel getRestServicesModel() {
        FileObject sourceRoot = findSourceRoot();
        if (restServicesModel == null && sourceRoot != null) {
            ClassPathProvider cpProvider = getProject().getLookup().lookup(ClassPathProvider.class);
            MetadataUnit metadataUnit = MetadataUnit.create(
                    cpProvider.findClassPath(sourceRoot, ClassPath.BOOT),
                    extendWithJsr311Api(cpProvider.findClassPath(sourceRoot, ClassPath.COMPILE)),
                    cpProvider.findClassPath(sourceRoot, ClassPath.SOURCE),
                    null);
            restServicesModel = RestServicesMetadataModelFactory.createMetadataModel(metadataUnit, project);
            for (PropertyChangeListener pcl : modelListeners) {
                restServicesModel.addPropertyChangeListener(pcl);
            }
        }
        return restServicesModel;
    }

    protected void refreshRestServicesMetadataModel() {
        if (restServicesModel != null) {
            for (PropertyChangeListener pcl : modelListeners) {
                restServicesModel.removePropertyChangeListener(pcl);
            }
            restServicesModel = null;
        }

        try {
            getRestServicesModel().runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {

                public Void run(RestServicesMetadata metadata) throws IOException {
                    metadata.getRoot().sizeRestServiceDescription();
                    return null;
                }
                });
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
        }
    }
    
    public static ClassPath extendWithJsr311Api(ClassPath classPath) {
        File jsr311JarFile = InstalledFileLocator.getDefault().locate(JSR311_API_LOCATION, null, false);
        return extendClassPath(classPath, jsr311JarFile);
    }
    
    public static ClassPath extendClassPath(ClassPath classPath, File path) {
        if (path == null) {
            return classPath;
        }
        try {
            PathResourceImplementation jsr311Path = getPathResource(path);
            List<PathResourceImplementation> roots = new ArrayList<PathResourceImplementation>();
            roots.add(jsr311Path);
            for (FileObject fo : classPath.getRoots()) {
                roots.add(ClassPathSupport.createResource(fo.getURL()));
            }
            return ClassPathSupport.createClassPath(roots);
        } catch (Exception ex) {
            return classPath;
        }
    }

    private static PathResourceImplementation getPathResource(File path) throws MalformedURLException {
        URL url = path.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        } else {
            url = path.toURI().toURL();
            String surl = url.toExternalForm();
            if (!surl.endsWith("/")) {
                url = new URL(surl + "/");
            }
        }
        return ClassPathSupport.createResource(url);
    }

    public abstract void extendBuildScripts() throws IOException;
    
    private boolean buildExtensionInited = false;
    public void initBuildScripts() throws IOException {
        if (! buildExtensionInited) {
            extendBuildScripts();
            buildExtensionInited = true;
        }
        
    }
    
    /**
     * Generates test client.  Typically RunTestClientAction would need to call 
     * this before invoke the build script target.
     * 
     * @param destDir directory to write test client files in.
     * @return test file object, containing token BASE_URL_TOKEN whether used or not.
     */
    public FileObject generateTestClient(File testdir) throws IOException {        
        initBuildScripts();
        
        if (! testdir.isDirectory()) {
            testdir.mkdirs();
        }
        String[] replaceKeys1 = {
            "TTL_TEST_RESBEANS", "MSG_TEST_RESBEANS_Trail", 
            "MSG_TEST_RESBEANS_TestInput", "MSG_TEST_RESBEANS_TestOutput"
        };
        String[] replaceKeys2 = {
            "MSG_TEST_RESBEANS_wadlErr", "MSG_TEST_RESBEANS_No_AJAX", "MSG_TEST_RESBEANS_Resource",
            "MSG_TEST_RESBEANS_Param", "MSG_TEST_RESBEANS_ResourceInputs", "MSG_TEST_RESBEANS_See",
            "MSG_TEST_RESBEANS_No_Container", "MSG_TEST_RESBEANS_Content", "MSG_TEST_RESBEANS_TabularView",
            "MSG_TEST_RESBEANS_RawView", "MSG_TEST_RESBEANS_ResponseHeaders", "MSG_TEST_RESBEANS_Help",
            "MSG_TEST_RESBEANS_TestButton", "MSG_TEST_RESBEANS_Loading", "MSG_TEST_RESBEANS_ServerError",
            "MSG_TEST_RESBEANS_Status", "MSG_TEST_RESBEANS_Headers", "MSG_TEST_RESBEANS_HeaderName",
            "MSG_TEST_RESBEANS_HeaderValue", "MSG_TEST_RESBEANS_ErrorHint", "MSG_TEST_RESBEANS_Insert",
            "MSG_TEST_RESBEANS_NoContents", "MSG_TEST_RESBEANS_NoHeaders", "MSG_TEST_RESBEANS_NewParamName",
            "MSG_TEST_RESBEANS_NewParamValue", "MSG_TEST_RESBEANS_AddParamButton", "MSG_TEST_RESBEANS_Monitor"
        };
        FileObject testFO = copyFile(testdir, TEST_RESBEANS_HTML, replaceKeys1, true);
        copyFile(testdir, TEST_RESBEANS_JS, replaceKeys2, false);
        copyFile(testdir, TEST_RESBEANS_CSS);
        copyFile(testdir, "expand.gif");
        copyFile(testdir, "collapse.gif");
        copyFile(testdir, "item.gif");
        copyFile(testdir, "cc.gif");
        copyFile(testdir, "og.gif");
        copyFile(testdir, "cg.gif");
        copyFile(testdir, "app.gif");
        return testFO;
    }

    /*
     * Copy File, as well as replace tokens, overwrite if specified
     */
    private FileObject copyFile(File testdir, String name, String[] replaceKeys, boolean overwrite) throws IOException {
        FileObject dir = FileUtil.toFileObject(testdir);
        FileObject fo = dir.getFileObject(name);
        if (fo == null) {
            fo = dir.createData(name);
        } else {
            if(!overwrite)
                return fo;
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
                for(int i=0;i<replaceKeys.length;i++)
                    line = line.replaceAll(replaceKeys[i], replaceValues[i]);
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
    
    /*
     * Copy File only
     */    
    private void copyFile(File testdir, String name) throws IOException {
        String path = "resources/"+name;
        File df = new File(testdir, name);
        if(!df.exists()) {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = RestSupport.class.getResourceAsStream(path);
                os = new FileOutputStream(df);
                int c;
                while ((c = is.read()) != -1) {
                    os.write(c);
                }
            } finally {
                if(os != null) {
                    os.flush();
                    os.close();
                }
                if(is != null)
                    is.close();            
            }
        }
    }
    
    /**
     *  Add SWDP library for given source file on specified class path types.
     * 
     *  @param source source file object for which the libraries is added.
     *  @param classPathTypes types of class path to add ("javac.compile",...)
     */
    public void addSwdpLibrary(String[] classPathTypes) throws IOException {
        Library swdpLibrary = LibraryManager.getDefault().getLibrary(SWDP_LIBRARY);
        if (swdpLibrary == null) {
            return;
        }

        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject sourceRoot = sgs[0].getRootFolder();
        for (String type : classPathTypes) {
            try {
                ProjectClassPathModifier.addLibraries(new Library[] { swdpLibrary }, sourceRoot, type);
                
                Library library = LibraryManager.getDefault().getLibrary(RESTAPI_LIBRARY);
                if (library != null) {
                    ProjectClassPathModifier.removeLibraries(new Library[] {library}, sourceRoot, ClassPath.COMPILE);
                }
            } catch(UnsupportedOperationException ex) {
                Logger.getLogger(getClass().getName()).info(type+" not supported.");
            }
        }
    }
    
    public void removeSwdpLibrary(String[] classPathTypes) throws IOException {
        Library swdpLibrary = LibraryManager.getDefault().getLibrary(SWDP_LIBRARY);
        if (swdpLibrary == null) {
            return;
        }

        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject sourceRoot = sgs[0].getRootFolder();
        for (String type : classPathTypes) {
            try {
                ProjectClassPathModifier.removeLibraries(new Library[] { swdpLibrary }, sourceRoot, type);
            } catch(UnsupportedOperationException ex) {
                Logger.getLogger(getClass().getName()).info(type+" not supported.");
            }
        }
    }
    
    public Project getProject() {
        return project;
    }

    /**
     * Should be overridden by sub-classes
     */
    public boolean hasSwdpLibrary() {
        return ! needsSwdpLibrary(getProject());
    }

    /**
     * A quick check if swdp is already part of classpath.
     */
    private static boolean needsSwdpLibrary(Project restEnableProject) {
        // check if swdp is already part of classpath
        SourceGroup[] sgs = ProjectUtils.getSources(restEnableProject).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sgs.length < 1) {
            return false;
        }
        FileObject sourceRoot = sgs[0].getRootFolder();
        ClassPath classPath = ClassPath.getClassPath(sourceRoot, ClassPath.COMPILE);
        //this package name will change when open source, should just rely on subclass to use file names
        FileObject restClass = classPath.findResource("com/sun/ws/rest/api/UriTemplate.class"); // NOI18N
        if (restClass != null) {
            return false;
        }
        return true;
    }
    
    public boolean isRestSupportOn() {
        if (getAntProjectHelper() == null) {
            return false;
        }
        String v = getProjectProperty(REST_SUPPORT_ON);
        return "true".equalsIgnoreCase(v) || "on".equalsIgnoreCase(v);
    }

    public void setRestSupport(Boolean v) {
        setProjectProperty(REST_SUPPORT_ON, v.toString());
    }

    public void setProjectProperty(String name, String value) {
        if (getAntProjectHelper() == null) {
            return;
        }
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(name, value);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        try {
            ProjectManager.getDefault().saveProject(getProject());
        } catch(IOException ioe) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, ioe.getLocalizedMessage(), ioe);
        }
    }

    public String getProjectProperty(String name) {
        if (getAntProjectHelper() == null) {
            return null;
        }
        return helper.getStandardPropertyEvaluator().getProperty(name);
    }
    
    protected boolean ignorePlatformRestLibrary() {
        String v = getProjectProperty(IGNORE_PLATFORM_RESTLIB);
        Boolean ignore = v != null ? Boolean.valueOf(v) : true;
        if (ignore == Boolean.FALSE) {
            return false;
        }
        return true;
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

    /*public class RestServicesChangeListener implements PropertyChangeListener {
        RestServicesChangeListener() {
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            //System.out.println("updating rest services");
            try {
                final int[] serviceCount = new int[1];
                restServicesMetadataModel.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                    public Void run(RestServicesMetadata metadata) throws IOException {
                        serviceCount[0] = metadata.getRoot().sizeRestServiceDescription();
                        return null;
                    }
                });

                if (serviceCount[0] > 0) {
                    ensureRestDevelopmentReady();
                } else {
                    removeRestDevelopmentReadiness();
                }
            } catch (IOException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
            //System.out.println("done updating rest services");
        }
    }*/
    
}

