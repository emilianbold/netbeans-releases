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

package org.netbeans.modules.maven.jaxws;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoints;
import org.netbeans.modules.websvc.api.jaxws.project.config.EndpointsProvider;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.jaxws.JaxWs;
import org.netbeans.modules.xml.retriever.RetrieveEntry;
import org.netbeans.modules.xml.retriever.Retriever;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class WSUtils {

    public static final String NON_JSR109_DONT_ASK = "dont_ask_for_nonjsr109_config"; //NOI18N
    private final static String SERVLET_CLASS_NAME =
            "com.sun.xml.ws.transport.http.servlet.WSServlet"; //NOI18N
    private final static String SERVLET_LISTENER =
            "com.sun.xml.ws.transport.http.servlet.WSServletContextListener"; //NOI18N
    /** downloads XML resources from source URI to target folder
     * (USAGE : this method can download a wsdl file and all wsdl/XML schemas,
     * that are recursively imported by this wsdl)
     * @param targetFolder A folder inside a NB project (ONLY) to which the retrieved resource will be copied to. All retrieved imported/included resources will be copied relative to this directory.
     * @param source URI of the XML resource that will be retrieved into the project
     * @return FileObject of the retrieved resource in the local file system
     */
    public static FileObject retrieveResource(FileObject targetFolder, URI catalog, URI source)
            throws java.net.UnknownHostException, java.net.URISyntaxException, IOException{
        try {
            Retriever retriever = Retriever.getDefault();
            FileObject result = retriever.retrieveResource(targetFolder, catalog, source);
            if (result==null) {
                Map map = retriever.getRetrievedResourceExceptionMap();
                if (map!=null) {
                    Set keys = map.keySet();
                    Iterator it = keys.iterator();
                    while (it.hasNext()) {
                        RetrieveEntry key = (RetrieveEntry)it.next();
                        Object exc = map.get(key);
                        if (exc instanceof IOException) {
                            throw (IOException)exc;
                        } else if (exc instanceof java.net.URISyntaxException) {
                            throw (java.net.URISyntaxException)exc;
                        } else if (exc instanceof Exception) {
                            IOException ex = new IOException(NbBundle.getMessage(WSUtils.class,"ERR_retrieveResource",key.getCurrentAddress()));
                            ex.initCause((Exception)exc);
                            throw (IOException)(ex);
                        }
                    }
                }
            }
            return result;
        } catch (RuntimeException ex) {
            throw (IOException)(new IOException(ex.getLocalizedMessage()).initCause(ex));
        }
    }
    
    public static void generateSunJaxwsFile(final FileObject targetDir) throws IOException {
        final String sunJaxwsContent =
                readResource(WsdlModel.class.getResourceAsStream("/org/netbeans/modules/websvc/jaxwsmodel/resources/sun-jaxws.xml")); //NOI18N
        FileSystem fs = targetDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject sunJaxwsFo = FileUtil.createData(targetDir, "sun-jaxws.xml");//NOI18N
                FileLock lock = sunJaxwsFo.lock();
                BufferedWriter bw = null;
                OutputStream os = null;
                OutputStreamWriter osw = null;
                try {
                    os = sunJaxwsFo.getOutputStream(lock);
                    osw = new OutputStreamWriter(os);
                    bw = new BufferedWriter(osw);
                    bw.write(sunJaxwsContent);
                } finally {
                    if(bw != null)
                        bw.close();
                    if(os != null)
                        os.close();
                    if(osw != null)
                        osw.close();
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
    
    public static void removeImplClass(Project project, String implClass) {
        Sources sources = project.getLookup().lookup(Sources.class);
        String resource = implClass.replace('.','/')+".java"; //NOI18N
        if (sources!=null) {
            SourceGroup[] srcGroup = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (int i=0;i<srcGroup.length;i++) {
                final FileObject srcRoot = srcGroup[i].getRootFolder();
                final FileObject implClassFo = srcRoot.getFileObject(resource);
                if (implClassFo!=null) {
                    try {
                        FileSystem fs = implClassFo.getFileSystem();
                        fs.runAtomicAction(new AtomicAction() {
                            public void run() {
                                deleteFile(implClassFo);
                            }
                        });
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                    return;
                }
            }
        }
    }
    
    private static void deleteFile(FileObject f) {
        FileLock lock = null;
        try {
            DataObject dObj = DataObject.find(f);
            if (dObj != null) {
                SaveCookie save = dObj.getCookie(SaveCookie.class);
                if (save!=null) save.save();
            }
            lock = f.lock();
            f.delete(lock);
        } catch(java.io.IOException e) {
            NotifyDescriptor ndd =
                    new NotifyDescriptor.Message(NbBundle.getMessage(WSUtils.class, "MSG_Unable_Delete_File", f.getNameExt()),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(ndd);
        } finally {
            if(lock != null) {
                lock.releaseLock();
            }
        }
    }
    
    /** Copy files from source folder to target folder recursively */
    public static void copyFiles(FileObject sourceFolder, FileObject targetFolder) throws IOException {
        FileObject[] children = sourceFolder.getChildren();
        for (int i=0;i<children.length;i++) {
            if (children[i].isFolder()) {
                FileObject folder = targetFolder.createFolder(children[i].getNameExt());
                copyFiles(children[i],folder);
            } else {
                children[i].copy(targetFolder, children[i].getName(), children[i].getExt());
            }
        }
    }
    
    public static FileObject findJaxWsFileObject(Project project) {
        return project.getProjectDirectory().getFileObject("nbproject/jax-ws.xml");
    }
    
    private static final String DEFAULT_PACKAGE_NAME="org.netbeans.ws"; //NOI18N
    
    private static String getPackageNameFromNamespace(String ns) {
        String base = ns;
        int doubleSlashIndex = ns.indexOf("//"); //NOI18N
        if (doubleSlashIndex >=0) {
            base = ns.substring(doubleSlashIndex+2);
        } else {
            int colonIndex = ns.indexOf(":");
            if (colonIndex >=0) base = ns.substring(colonIndex+1);
        }
        StringTokenizer tokens = new StringTokenizer(base,"/"); //NOI18N
        if (tokens.countTokens() > 0) {
            List<String> packageParts = new ArrayList<String>();
            List<String> nsParts = new ArrayList<String>();
            while (tokens.hasMoreTokens()) {
                String part = tokens.nextToken();
                if (part.length() >= 0) {
                    nsParts.add(part);
                }
            }
            if (nsParts.size() > 0) {
                StringTokenizer tokens1 = new StringTokenizer(nsParts.get(0),"."); //NOI18N
                int countTokens = tokens1.countTokens();
                if (countTokens > 0) {
                    List<String> list = new ArrayList<String>();
                    while(tokens1.hasMoreTokens()) {
                        list.add(tokens1.nextToken());
                    }
                    for (int i=countTokens-1; i>=0; i--) {
                        String part = list.get(i);
                        if (i > 0 || !"www".equals(part)) { //NOI18N
                            packageParts.add(list.get(i).toLowerCase());
                        }
                    }
                } else {
                    return DEFAULT_PACKAGE_NAME;
                }
                for (int i=1; i<nsParts.size(); i++) {
                    packageParts.add(nsParts.get(i).toLowerCase());
                }
                StringBuffer buf = new StringBuffer(packageParts.get(0));
                for (int i=1;i<packageParts.size();i++) {
                    buf.append("."+packageParts.get(i));
                }
                return buf.toString();
            }
        }
        return DEFAULT_PACKAGE_NAME;
        
    }

    public static boolean isProjectReferenceable(Project sourceProject, Project targetProject) {
        if (sourceProject == targetProject) {
            return true;
        } else {
            NbMavenProject mavenProject = sourceProject.getLookup().lookup(NbMavenProject.class);
            if (mavenProject != null && NbMavenProject.TYPE_JAR.equals(mavenProject.getPackagingType())) {
                return true;
            }
            return false;
        }
    }


    public static boolean isEJB(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            Object moduleType = j2eeModuleProvider.getJ2eeModule().getModuleType();
            if (J2eeModule.EJB.equals(moduleType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWeb(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            Object moduleType = j2eeModuleProvider.getJ2eeModule().getModuleType();
            if (J2eeModule.WAR.equals(moduleType)) {
                return true;
            }
        }
        return false;
    }

    public static void updateClients(Project prj, JAXWSLightSupport jaxWsSupport) {
        // get old clients
        List<JaxWsService> oldClients = new ArrayList<JaxWsService>();
        Set<String> oldNames = new HashSet<String>();
        for (JaxWsService s : jaxWsSupport.getServices()) {
            if (!s.isServiceProvider()) {
                oldClients.add(s);
                oldNames.add(s.getLocalWsdl());
            }
        }
        FileObject wsdlFolder = jaxWsSupport.getWsdlFolder(false);
        if (wsdlFolder != null) {
            List<JaxWsService> newClients = getJaxWsClients(prj, jaxWsSupport, wsdlFolder);
            Set<String> commonNames = new HashSet<String>();
            for (JaxWsService client : newClients) {
                String localWsdl = client.getLocalWsdl();
                if (oldNames.contains(localWsdl)) {
                    commonNames.add(localWsdl);
                }
            }
            // removing old clients
            for (JaxWsService oldClient : oldClients) {
                if (!commonNames.contains(oldClient.getLocalWsdl())) {
                    jaxWsSupport.removeService(oldClient);
                }
            }
            // add new clients
            for (JaxWsService newClient : newClients) {
                if (!commonNames.contains(newClient.getLocalWsdl())) {
                    newClient.setWsdlUrl(getOriginalWsdlUrl(prj, jaxWsSupport, newClient.getLocalWsdl(), false));
                    jaxWsSupport.addService(newClient);
                }
            }
        } else {
            // removing all clients
            for (JaxWsService client : oldClients) {
                jaxWsSupport.removeService(client);
            }
        }
        
    }

    public static void detectWsdlClients(Project prj, JAXWSLightSupport jaxWsSupport, FileObject wsdlFolder)  {
        List<WsimportPomInfo> candidates = MavenModelUtils.getWsdlFiles(prj);
        if (candidates.size() > 0) {
            for (WsimportPomInfo candidate : candidates) {
                String wsdlPath = candidate.getWsdlPath();
                if (isClient(prj, jaxWsSupport, wsdlPath)) {
                    JaxWsService client = new JaxWsService(wsdlPath, false);
                    if (candidate.getHandlerFile() != null) {
                        client.setHandlerBindingFile(candidate.getHandlerFile());
                    }
                    client.setWsdlUrl(getOriginalWsdlUrl(prj, jaxWsSupport, wsdlPath, false));
                    jaxWsSupport.addService(client);
                }
            }
        } else {
            // look for wsdl in wsdl folder
        }
    }

    private static List<JaxWsService> getJaxWsClients(Project prj, JAXWSLightSupport jaxWsSupport, FileObject wsdlFolder) {
        List<WsimportPomInfo> candidates = MavenModelUtils.getWsdlFiles(prj);
        List<JaxWsService> clients = new ArrayList<JaxWsService>();
        for (WsimportPomInfo candidate : candidates) {
            String wsdlPath = candidate.getWsdlPath();
            if (isClient(prj, jaxWsSupport, wsdlPath)) {
                JaxWsService client = new JaxWsService(wsdlPath, false);
                if (candidate.getHandlerFile() != null) {
                    client.setHandlerBindingFile(candidate.getHandlerFile());
                }
                clients.add(client);
            }
        }
        return clients;
    }

    private static boolean isClient(Project prj, JAXWSLightSupport jaxWsSupport, String localWsdlPath) {
        Preferences prefs = ProjectUtils.getPreferences(prj, MavenWebService.class,true);
        if (prefs != null) {
            FileObject wsdlFo = getLocalWsdl(jaxWsSupport, localWsdlPath);
            if (wsdlFo != null) {
                // if client exists return true
                if (prefs.get(MavenWebService.CLIENT_PREFIX+wsdlFo.getName(), null) != null) {
                    return true;
                // if service doesn't exist return true
                } else if (prefs.get(MavenWebService.SERVICE_PREFIX+wsdlFo.getName(), null) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    private static FileObject getLocalWsdl(JAXWSLightSupport jaxWsSupport, String localWsdlPath) {
        FileObject wsdlFolder = jaxWsSupport.getWsdlFolder(false);
        if (wsdlFolder!=null) {
            return wsdlFolder.getFileObject(localWsdlPath);
        }
        return null;
    }

    public static String getOriginalWsdlUrl(Project prj, JAXWSLightSupport jaxWsSupport, String localWsdl, boolean forService) {
        FileObject wsdlFo = WSUtils.getLocalWsdl(jaxWsSupport, localWsdl);
        if (wsdlFo != null) {
            Preferences prefs = ProjectUtils.getPreferences(prj, MavenWebService.class, true);
            if (prefs != null) {
                // remember original WSDL URL for service
                if (forService) {
                    return prefs.get(MavenWebService.SERVICE_PREFIX+wsdlFo.getName(), null);
                } else {
                    return prefs.get(MavenWebService.CLIENT_PREFIX+wsdlFo.getName(), null);
                }
            }
        }
        return null;
    }
    private static boolean webAppHasListener(WebApp webApp, String listenerClass){
        Listener[] listeners = webApp.getListener();
        for(int i = 0; i < listeners.length; i++){
            Listener listener = listeners[i];
            if(listenerClass.equals(listener.getListenerClass())){
                return true;
            }
        }
        return false;
    }

    // useful methods to work with Deployment Descriptor 
    
    private static WebApp getWebApp(Project prj) {
        try {
            FileObject deploymentDescriptor = getDeploymentDescriptor(prj);
            if(deploymentDescriptor != null) {
                return DDProvider.getDefault().getDDRoot(deploymentDescriptor);
            }
        } catch (java.io.IOException e) {
            Logger.getLogger("global").log(Level.INFO, e.getLocalizedMessage());
        }
        return null;
    }

    private static FileObject getDeploymentDescriptor(Project prj) {
        J2eeModuleProvider provider = prj.getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            File dd = provider.getJ2eeModule().getDeploymentConfigurationFile("WEB-INF/web.xml");
            if (dd != null && dd.exists()) {
                return FileUtil.toFileObject(dd);
            }
        }
        return null;
    }

    // methods that handle sun-jaxws.xml file

    public static void addSunJaxWsEntry(FileObject ddFolder, JaxWsService service)
            throws IOException {
        FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml"); //NOI18N
        if(sunjaxwsFile == null){
            generateSunJaxwsFile(ddFolder);
        }
        sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml"); //NOI18N
        Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunjaxwsFile);
        String serviceName = service.getServiceName();
        Endpoint oldEndpoint = endpoints.findEndpointByName(service.getServiceName());
        if (oldEndpoint == null) {
            addService(endpoints, service);
            FileLock lock = null;
            OutputStream os = null;
            synchronized (sunjaxwsFile) {
                try {
                    lock = sunjaxwsFile.lock();
                    os = sunjaxwsFile.getOutputStream(lock);
                    endpoints.write(os);
                } finally{
                    if (lock != null)
                        lock.releaseLock();

                    if(os != null)
                        os.close();
                }
            }
        }
    }

    private static void addJaxWsEntries(FileObject ddFolder, JAXWSLightSupport jaxWsSupport)
            throws IOException {

        generateSunJaxwsFile(ddFolder);
        FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml"); //NOI18N
        Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunjaxwsFile);
        for (JaxWsService service: jaxWsSupport.getServices()) {
            if (service.isServiceProvider()) {
                addService(endpoints, service);
            }
        }
        FileLock lock = null;
        OutputStream os = null;
        synchronized (sunjaxwsFile) {
            try {
                lock = sunjaxwsFile.lock();
                os = sunjaxwsFile.getOutputStream(lock);
                endpoints.write(os);
            } finally{
                if (lock != null)
                    lock.releaseLock();

                if(os != null)
                    os.close();
            }
        }
    }

    private static void addService(Endpoints endpoints, JaxWsService service) {
        Endpoint endpoint = endpoints.newEndpoint();
        endpoint.setEndpointName(service.getServiceName());
        endpoint.setImplementation(service.getImplementationClass());
        endpoint.setUrlPattern("/" + service.getServiceName());
        endpoints.addEnpoint(endpoint);
    }

    public static void removeSunJaxWsEntry(FileObject ddFolder, JaxWsService service)
            throws IOException {
        FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml"); //NOI18N
        if (sunjaxwsFile != null) {
            Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunjaxwsFile);
            String serviceName = service.getServiceName();
            Endpoint endpoint = endpoints.findEndpointByName(service.getServiceName());
            if (endpoint != null) {
                endpoints.removeEndpoint(endpoint);
                FileLock lock = null;
                OutputStream os = null;
                synchronized (sunjaxwsFile) {
                    try {
                        lock = sunjaxwsFile.lock();
                        os = sunjaxwsFile.getOutputStream(lock);
                        endpoints.write(os);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                }
            }
        }
    }

    private static void removeSunJaxWs(FileObject ddFolder)
            throws IOException {
        FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml"); //NOI18N
        if (sunjaxwsFile != null) {
            sunjaxwsFile.delete();
        }
    }

    public static void replaceSunJaxWsEntries(FileObject ddFolder, String oldServiceName, String newServiceName)
            throws IOException {

        FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml"); //NOI18N
        if (sunjaxwsFile != null) {
            Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunjaxwsFile);
            Endpoint endpoint = endpoints.findEndpointByName(oldServiceName);
            if (endpoint != null) {
                endpoint.setEndpointName(newServiceName);
                endpoint.setUrlPattern("/" + newServiceName);
                FileLock lock = null;
                OutputStream os = null;
                synchronized (sunjaxwsFile) {
                    try {
                        lock = sunjaxwsFile.lock();
                        os = sunjaxwsFile.getOutputStream(lock);
                        endpoints.write(os);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                }
            }
        }
    }

    public static boolean generateNonJsr109Artifacts(Project prj) {
        Preferences prefs = ProjectUtils.getPreferences(prj, MavenWebService.class,true);
        if (prefs == null || prefs.get(NON_JSR109_DONT_ASK , null) == null) {
            ConfirmationPanel panel =
                new ConfirmationPanel(NbBundle.getMessage(WSUtils.class,"MSG_GenerateDDEntries", prj.getProjectDirectory().getName()));
            DialogDescriptor dd = new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(WSUtils.class,"TTL_GenerateDDEntries"),
                    true,
                    DialogDescriptor.YES_NO_OPTION,
                    null,null);
            Object result = DialogDisplayer.getDefault().notify(dd);
            if (panel.notAskAgain()) {
                if (prefs != null) {
                    prefs.put(NON_JSR109_DONT_ASK , "true"); //NOI18N
                }
            }
            return NotifyDescriptor.YES_OPTION.equals(result);
        } else {
            return false;
        }
    }

    private static boolean removeNonJsr109Artifacts(Project prj) {
        Preferences prefs = ProjectUtils.getPreferences(prj, MavenWebService.class,true);
        if (prefs == null || prefs.get(NON_JSR109_DONT_ASK , null) == null) {
            ConfirmationPanel panel =
                new ConfirmationPanel(NbBundle.getMessage(WSUtils.class,"MSG_RemoveDDEntries"));
            DialogDescriptor dd = new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(WSUtils.class,"TTL_RemoveDDEntries"),
                    true,
                    DialogDescriptor.YES_NO_OPTION,
                    null,null);
            Object result = DialogDisplayer.getDefault().notify(dd);
            if (panel.notAskAgain()) {
                if (prefs != null) {
                    prefs.put(NON_JSR109_DONT_ASK , "true"); //NOI18N
                }
            }
            return NotifyDescriptor.YES_OPTION.equals(result);
        } else {
            return false;
        }
    }

    public static boolean isJsr109Supported(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider == null) {
            // set to true by default
            return true;
        } else {
            WSStackUtils stackUtils = new WSStackUtils(project);
            return stackUtils.isJsr109Supported();
        }
    }

    /** Add service entries to deployment descriptor.
     *
     * @param prj
     * @param service
     * @throws java.io.IOException
     */
    public static void addServiceToDD(Project prj, JaxWsService service)
        throws IOException {
        //add servlet entry to web.xml
        WebApp webApp = getWebApp(prj);
        if (webApp != null) {
            try{
                addServlet(webApp, service);
                if (!webAppHasListener(webApp, SERVLET_LISTENER)){
                    webApp.addBean("Listener", new String[]{"ListenerClass"}, //NOI18N
                            new Object[]{SERVLET_LISTENER}, "ListenerClass"); //NOI18N
                }
                // This also saves server specific configuration, if necessary.
                webApp.write(getDeploymentDescriptor(prj));
            } catch (ClassNotFoundException exc) {
                Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage()); //NOI18N
            } catch (NameAlreadyUsedException exc) {
                Logger.getLogger(WSUtils.class.getName()).log(Level.INFO, exc.getLocalizedMessage()); //NOI18N
            }
        }
    }

    private static void addServicesToDD(Project prj, JAXWSLightSupport jaxWsSupport)
        throws IOException {
        WebApp webApp = getWebApp(prj);
        if (webApp != null) {
            try {
                if (!webAppHasListener(webApp, SERVLET_LISTENER)) {
                    webApp.addBean("Listener", new String[]{"ListenerClass"}, //NOI18N
                            new Object[]{SERVLET_LISTENER}, "ListenerClass"); //NOI18N
                }
                for (JaxWsService service : jaxWsSupport.getServices()) {
                    if (service.isServiceProvider()) {
                        addServlet(webApp, service);
                    }
                }
            } catch (NameAlreadyUsedException exc) {
                Logger.getLogger(WSUtils.class.getName()).log(Level.INFO, exc.getLocalizedMessage()); //NOI18N
            } catch (ClassNotFoundException exc) {
                Logger.getLogger(WSUtils.class.getName()).log(Level.INFO, exc.getLocalizedMessage()); //NOI18N
            }
            webApp.write(getDeploymentDescriptor(prj));
        }
    }

    private static void addServlet(WebApp webApp, JaxWsService service) throws ClassNotFoundException, NameAlreadyUsedException {
        String servletName = service.getServiceName();
        Servlet servlet = (Servlet)webApp.addBean("Servlet", new String[]{"ServletName","ServletClass"}, //NOI18N
                new Object[]{servletName, SERVLET_CLASS_NAME}, "ServletName"); //NOI18N
        servlet.setLoadOnStartup(new java.math.BigInteger("1")); //NOI18N
        webApp.addBean("ServletMapping", new String[] {"ServletName", "UrlPattern"}, //NOI18N
                new Object[]{servletName, "/" + servletName}, "ServletName"); //NOI18N
    }

    /**
     * Remove the service entries from deployment descriptor.
     *
     * @param serviceName Name of the web service to be removed
     */
    public static void removeServiceFromDD(Project prj, JaxWsService service)
        throws IOException {
        WebApp webApp = getWebApp(prj);
        if (webApp != null) {
            boolean changed = removeServiceFromDD(webApp, service.getServiceName());

            //determine if there are other web services in the project
            //if none, remove the listener
            boolean hasMoreWebServices = false;
            Servlet[] remainingServlets = webApp.getServlet();
            for(int i = 0; i < remainingServlets.length; i++){
                if(remainingServlets[i].getServletClass().equals(SERVLET_CLASS_NAME)){
                    hasMoreWebServices = true;
                    break;
                }
            }
            if(!hasMoreWebServices){
                Listener[] listeners = webApp.getListener();
                for(int i = 0; i < listeners.length; i++){
                    Listener listener = listeners[i];
                    if(listener.getListenerClass().equals(SERVLET_LISTENER)){
                        webApp.removeListener(listener);
                        changed = true;
                        break;
                    }
                }
            }
            if (changed) {
                webApp.write(getDeploymentDescriptor(prj));
            }
        }
    }

    private static void removeServicesFromDD(Project prj, JAXWSLightSupport jaxWsSupport)
        throws IOException {
        WebApp webApp = getWebApp(prj);
        if (webApp != null) {
            boolean changed = false;
            // remove all services
            for (JaxWsService service : jaxWsSupport.getServices()) {
                changed = removeServiceFromDD(webApp, service.getServiceName());
            }
            // remove servlet listener
            Listener[] listeners = webApp.getListener();
            for(int i = 0; i < listeners.length; i++){
                Listener listener = listeners[i];
                if(listener.getListenerClass().equals(SERVLET_LISTENER)){
                    webApp.removeListener(listener);
                    changed = true;
                    break;
                }
            }
            if (changed) {
                webApp.write(getDeploymentDescriptor(prj));
            }
        }
    }

    /**
     * Remove the web.xml servlets for the non-JSR 109 web service.
     *
     * @param serviceName Name of the web service to be removed
     */
    private static boolean removeServiceFromDD(WebApp webApp, String serviceName) {
        boolean changed = false;
        //first remove the servlet
        Servlet[] servlets = webApp.getServlet();
        for(int i = 0; i < servlets.length; i++){
            Servlet servlet = servlets[i];
            if(servlet.getServletName().equals(serviceName)){
                webApp.removeServlet(servlet);
                changed = true;
                break;
            }
        }
        //remove the servlet mapping
        ServletMapping[] mappings = webApp.getServletMapping();
        for(int i = 0; i < mappings.length; i++){
            ServletMapping mapping = mappings[i];
            if(mapping.getServletName().equals(serviceName)){
                webApp.removeServletMapping(mapping);
                changed = true;
                break;
            }
        }
        return changed;
    }

    /**
     * Remove the web.xml entries for the non-JSR 109 web service.
     *
     * @param serviceName Name of the web service to be removed
     */
    public static void replaceServiceEntriesFromDD(Project prj, String oldServiceName, String newServiceName)
        throws IOException {
        WebApp webApp = getWebApp(prj);
        if (webApp != null) {
            boolean changed = replaceServiceInDD(webApp, oldServiceName, newServiceName);
            if (changed) {
                webApp.write(getDeploymentDescriptor(prj));
            }
        }
    }

    /**
     * Remove the web.xml servlets for the non-JSR 109 web service.
     *
     * @param serviceName Name of the web service to be removed
     */
    private static boolean replaceServiceInDD(WebApp webApp, String oldServiceName, String newServiceName) {
        boolean changed = false;
        //first remove the servlet
        Servlet[] servlets = webApp.getServlet();
        for(int i = 0; i < servlets.length; i++){
            Servlet servlet = servlets[i];
            if(servlet.getServletName().equals(oldServiceName)){
                servlet.setServletName(newServiceName);
                changed = true;
                break;
            }
        }
        //remove the servlet mapping
        ServletMapping[] mappings = webApp.getServletMapping();
        for(int i = 0; i < mappings.length; i++){
            ServletMapping mapping = mappings[i];
            if(mapping.getServletName().equals(oldServiceName)){
                mapping.setServletName(newServiceName);
                mapping.setUrlPattern("/"+newServiceName);
                break;
            }
        }
        return changed;
    }

    public static void checkNonJSR109Entries(Project prj) {
        JAXWSLightSupport jaxWsSupport = JAXWSLightSupport.getJAXWSLightSupport(prj.getProjectDirectory());
        if (jaxWsSupport != null) {
            WSStack<JaxWs> wsStack = new WSStackUtils(prj).getWsStack(JaxWs.class);
            if (wsStack != null) {
                FileObject ddFolder = jaxWsSupport.getDeploymentDescriptorFolder();
                if (wsStack.isFeatureSupported(JaxWs.Feature.JSR109)) {
                    if (ddFolder != null && ddFolder.getFileObject("sun-jaxws.xml") != null) {
                        // remove non JSR109 artifacts
                        if (removeNonJsr109Artifacts(prj)) {
                            try {
                                removeSunJaxWs(ddFolder);
                            } catch (IOException ex) {
                                Logger.getLogger(WSUtils.class.getName()).log(Level.WARNING,
                                        "Cannot remove sun-jaxws.xml file.", ex); //NOI18N
                            }
                            try {
                                removeServicesFromDD(prj, jaxWsSupport);
                            } catch (IOException ex) {
                                Logger.getLogger(WSUtils.class.getName()).log(Level.WARNING,
                                        "Cannot remove services from web.xml.", ex); //NOI18N
                            }
                        }
                    }
                } else {
                    if (ddFolder == null || ddFolder.getFileObject("sun-jaxws.xml") == null) {
                        // generate non JSR109 artifacts
                        if (generateNonJsr109Artifacts(prj)) {
                            if (ddFolder != null) {
                                try {
                                    addJaxWsEntries(ddFolder, jaxWsSupport);
                                } catch (IOException ex) {
                                    Logger.getLogger(WSUtils.class.getName()).log(Level.WARNING,
                                            "Cannot modify sun-jaxws.xml file", ex); //NOI18N
                                }
                                try {
                                    addServicesToDD(prj, jaxWsSupport);
                                } catch (IOException ex) {
                                    Logger.getLogger(WSUtils.class.getName()).log(Level.WARNING,
                                            "Cannot modify web.xml file", ex); //NOI18N
                                }
                            } else {
                                String mes = NbBundle.getMessage(MavenJAXWSSupportImpl.class, "MSG_CannotFindWEB-INF"); // NOI18N
                                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                                DialogDisplayer.getDefault().notify(desc);
                            }
                        }
                    }
                }
            }
        }
    }
    
}