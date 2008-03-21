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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.axis2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.axis2.services.model.ServicesModel;
import org.netbeans.modules.websvc.axis2.services.model.ServicesUtils;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkuchtiak
 */
public class AxisUtils {
    
    private static final String DEFAULT_NAMESPACE="http://ws.apache.org/axis2"; //NOI18N
    private static final String DEFAULT_SCHEMA_NAMESPACE=DEFAULT_NAMESPACE+"/xsd"; //NOI18N
    private static final String BUILD_SERVICES_PATH="build/axis2/resources/services.xml"; //NOI18N
    
    public static void retrieveServicesFromResource(final FileObject targetDir, boolean isWsGroup) throws IOException {
        final String handlerContent =
                (isWsGroup?
                    readResource(AxisUtils.class.getResourceAsStream("/org/netbeans/modules/websvc/axis2/resources/services_2.xml")): //NOI18N
                    readResource(AxisUtils.class.getResourceAsStream("/org/netbeans/modules/websvc/axis2/resources/services_1.xml"))); //NOI18N
        FileSystem fs = targetDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject servicesFo = FileUtil.createData(targetDir, "services.xml");//NOI18N
                FileLock lock = servicesFo.lock();
                BufferedWriter bw = null;
                OutputStream os = null;
                try {
                    os = servicesFo.getOutputStream(lock);
                    bw = new BufferedWriter(new OutputStreamWriter(os));
                    bw.write(handlerContent);
                } finally {
                    if(bw != null)
                        bw.close();
                    if(os != null)
                        os.close();
                    if(lock != null)
                        lock.releaseLock();
                }
            }
        });
    }
    
    public static void retrieveAxis2FromResource(final FileObject targetDir) throws IOException {
        final String axis2Content =
                    readResource(AxisUtils.class.getResourceAsStream("/org/netbeans/modules/websvc/axis2/resources/axis2.xml")); //NOI18N
        FileSystem fs = targetDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject axis2Fo = FileUtil.createData(targetDir, "axis2.xml");//NOI18N
                FileLock lock = axis2Fo.lock();
                BufferedWriter bw = null;
                OutputStream os = null;
                try {
                    os = axis2Fo.getOutputStream(lock);
                    bw = new BufferedWriter(new OutputStreamWriter(os));
                    bw.write(axis2Content);
                } finally {
                    if(bw != null)
                        bw.close();
                    if(os != null)
                        os.close();
                    if(lock != null)
                        lock.releaseLock();
                }
            }
        });
    }
    
    private static String readResource(InputStream is) throws IOException {
        // read the config from resource first
        StringBuffer sb = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }
    
    public static FileObject getServicesFolder(FileObject projectDir, boolean create) {
        FileObject configFolder = projectDir.getFileObject("xml-resources/axis2/META-INF"); //NOI18N
        if (configFolder == null && create) {
            try {
                configFolder = FileUtil.createFolder(projectDir, "xml-resources/axis2/META-INF");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return configFolder;
    }
    
    public static FileObject getNbprojectFolder(FileObject projectDir) {
        return projectDir.getFileObject("nbproject"); //NOI18N
    }
    
    public static ModelSource createModelSource(final FileObject thisFileObj,
            boolean editable) {
        assert thisFileObj != null : "Null file object.";

        final DataObject dobj;
        try {
            dobj = DataObject.find(thisFileObj);
            final EditorCookie editor = dobj.getCookie(EditorCookie.class);
            if (editor != null) {
                Lookup proxyLookup = Lookups.proxy(
                   new Lookup.Provider() { 
                        public Lookup getLookup() {
                            try {
                                return Lookups.fixed(new Object[] {editor.openDocument(), dobj, thisFileObj});
                            } catch (IOException ex) {
                                return Lookups.fixed(new Object[] {dobj, thisFileObj});
                            }
                        }

                    }
                );
                return new ModelSource(proxyLookup, editable);
            }
        } catch (DataObjectNotFoundException ex) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                ex.getMessage(), ex);
        }
        

        return null;
    }
    
    public static String getNamespaceFromClassName(String className) {
        StringTokenizer tokens = new StringTokenizer(className,"."); //NOI18N
        if (tokens.countTokens() <= 1) return DEFAULT_NAMESPACE;
        else {
            List<String> list = new ArrayList<String>();
            int index=0;
            while (tokens.hasMoreTokens()) {
                list.add(tokens.nextToken());
                index++;
            }
            list.remove(index-1);
            StringBuffer buf = new StringBuffer("http://"); //NOI18N
            for (int i = index-2; i>=0; i--) {
                buf.append(list.get(i)+"/"); //NOI18N
            }
            return buf.toString();
        }
    }
    
    public static EditableProperties getEditableProperties(final Project prj,final  String propertiesPath) 
        throws IOException {        
        try {
            return
            ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<EditableProperties>() {
                public EditableProperties run() throws IOException {                                             
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    EditableProperties ep = null;
                    if (propertiesFo!=null) {
                        InputStream is = null; 
                        ep = new EditableProperties();
                        try {
                            is = propertiesFo.getInputStream();
                            ep.load(is);
                        } finally {
                            if (is!=null) is.close();
                        }
                    }
                    return ep;
                }
            });
        } catch (MutexException ex) {
            return null;
        }
    }
    
    public static void storeEditableProperties(final Project prj, final  String propertiesPath, final EditableProperties ep) 
        throws IOException {        
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {                                             
                    FileObject propertiesFo = prj.getProjectDirectory().getFileObject(propertiesPath);
                    if (propertiesFo!=null) {
                        OutputStream os = null;
                        try {
                            os = propertiesFo.getOutputStream();
                            ep.store(os);
                        } finally {
                            if (os!=null) os.close();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException ex) {
        }
    }
    
    public static boolean runTargets(FileObject projectDir, final String[] targets) {
        return runTargets(projectDir, targets, 5000L);
    }
    
    public static boolean runTargets(FileObject projectDir, final String[] targets, final long timeLimit ) {
        final FileObject buildImplFo = projectDir.getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
        try {
            return ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws IOException, InterruptedException {
                    ExecutorTask wsimportTask =
                            ActionUtils.runTarget(buildImplFo,targets,null); //NOI18N
                    return Boolean.valueOf(wsimportTask.waitFinished(timeLimit));
                }
            }).booleanValue();
        } catch (MutexException e) {
            Logger.getLogger(AxisUtils.class.getName()).log(Level.FINE, "AxisUtils.runTargets", e);
            return false;
        }       
    }
    
    public static ServicesModel getServicesModelForProject(Project project) {
        Axis2ModelProvider axis2ModelProvider = project.getLookup().lookup(Axis2ModelProvider.class);
        ServicesModel servicesModel = axis2ModelProvider.getServicesModel();
        if (servicesModel == null) {
            FileObject configFolder = getServicesFolder(project.getProjectDirectory(), false);
            if (configFolder != null) {
                FileObject servicesFo = configFolder.getFileObject("services.xml"); //NOI18N
                servicesModel = ServicesUtils.getServicesModel(servicesFo, true);
                axis2ModelProvider.setServicesModel(servicesModel);
            }
        }
        return servicesModel;
    }
    
    public static FileObject getServicesFileObject(FileObject projectDir) {
        return projectDir.getFileObject(BUILD_SERVICES_PATH);
    }
    
    public static Preferences getPreferences() {
        return NbPreferences.forModule(AxisUtils.class);
    }
    
    public static void updateAxisDeployProperty(Project prj, String axisDeploy) throws IOException {
        EditableProperties ep = AxisUtils.getEditableProperties(prj, AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        boolean needStore = false;
        if (ep != null) {
            String oldAxisDeploy = ep.getProperty("axis2.deploy.war");
            if (oldAxisDeploy == null) oldAxisDeploy = ep.getProperty("axis2.deploy.dir");
            if (axisDeploy != null && !axisDeploy.equals(oldAxisDeploy)) {
                if (axisDeploy.endsWith(".war")) { //NOI18N
                    ep.setProperty("axis2.deploy.war",axisDeploy); //NOI18N
                    ep.remove("axis2.deploy.dir");
                } else {
                    ep.setProperty("axis2.deploy.dir",axisDeploy); //NOI18N
                    ep.remove("axis2.deploy.war"); //NOI18N                       
                }
                needStore = true;
            }
        }
        if (needStore) AxisUtils.storeEditableProperties(prj, AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);         
    }
    
    public static void openInBrowser(String urlString) {
        try {
            final URL url = new URL(urlString);
            if (url!=null) {  
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        boolean connectionOK=false;
                        try {
                            URLConnection connection = url.openConnection();
                            if (connection instanceof HttpURLConnection) {
                                HttpURLConnection httpConnection = (HttpURLConnection)connection;
                                try {
                                    httpConnection.setRequestMethod("GET"); //NOI18N
                                    httpConnection.connect();
                                    int responseCode = httpConnection.getResponseCode();
                                    // for secured web services the response code is 405: we should allow to show the response
                                    if (HttpURLConnection.HTTP_OK == responseCode || HttpURLConnection.HTTP_BAD_METHOD == responseCode)
                                        connectionOK=true;
                                } catch (java.net.ConnectException ex) {
                                    // doing nothing - display warning
                                } finally {
                                    if (httpConnection!=null) 
                                    httpConnection.disconnect();
                                }
                            }

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        if (connectionOK) {
                            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                        } else {
                            DialogDisplayer.getDefault().notify(
                                    new NotifyDescriptor.Message(
                                        NbBundle.getMessage(AxisUtils.class,"MSG_UNABLE_TO_OPEN_TEST_PAGE",url),
                                        NotifyDescriptor.WARNING_MESSAGE));
                        }
                    }
                    
                });
            }
        } catch (MalformedURLException ex) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(AxisUtils.class,
                    "TXT_TesterPageUrl", urlString));   //NOI18N
        }
    }
    
    public static List<URL> getReferencedJars(Project prj) throws IOException {
        List<URL> libraries = new ArrayList<URL>();
        SourceGroup[] sourceGroups = ProjectUtils.getSources(prj).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject srcRoot = null;
        if (sourceGroups.length > 0) srcRoot = sourceGroups[0].getRootFolder();
        if (srcRoot != null) {
            EditableProperties ep = getEditableProperties(prj, AntProjectHelper.PROJECT_PROPERTIES_PATH);
            ClassPath cp = ClassPath.getClassPath(srcRoot, ClassPath.COMPILE);
            List<Entry> entries = cp.entries();
            Set<String> alreadyAdded = new HashSet<String>();
            for (Entry entry:entries) {
                URL archive = FileUtil.getArchiveFile(entry.getURL());
                if (archive != null && isJarReferenced(ep, archive)) {
                    String jarName = getJarName(archive);
                    if (!alreadyAdded.contains(jarName)) {
                        alreadyAdded.add(jarName);
                        libraries.add(archive);
                    }
                }
            }
        }
        return libraries;
    }
    
    private static boolean isJarReferenced(EditableProperties ep, URL jar) {
        String urlString = jar.toExternalForm().replace('\\', '/');
        int index = urlString.lastIndexOf("/");
        if (index >= 0) {
            String jarName = urlString.substring(index+1);
            if (ep.getProperty("file.reference."+jarName) != null) return true;
        }
        return false;
    }
    
    public static String getJarName(URL url) {
        String text = url.getPath().replace('\\', '/');
        int ind = text.lastIndexOf("/");
        return ind >= 0 ? text.substring(ind+1):text;
    }
}
