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

package org.netbeans.modules.web.jsf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.wizards.JSFConfigurationPanel;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.ViewHandler;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl, Po-Ting Wu, Alexey Butenko
 */
public class JSFFrameworkProvider extends WebFrameworkProvider {
    
    private static final Logger LOGGER = Logger.getLogger(JSFFrameworkProvider.class.getName());

    private static String HANDLER = "com.sun.facelets.FaceletViewHandler";                          //NOI18N
    
    private static String WELCOME_JSF = "welcomeJSF.jsp";   //NOI18N
    private static String WELCOME_XHTML = "index.xhtml"; //NOI18N
    private static String TEMPLATE_XHTML = "template.xhtml"; //NOI18N
    private static String TEMPLATE_XHTML2 = "template-jsf2.xhtml"; //NOI18N
    private static String CSS_FOLDER = "css"; //NOI18N
    private static String CSS_FOLDER2 = "resources/css"; //NOI18N
    private static String DEFAULT_CSS = "default.css"; //NOI18N
    private static String FORWARD_JSF = "forwardToJSF.jsp"; //NOI18N
    private static String RESOURCE_FOLDER = "org/netbeans/modules/web/jsf/resources/"; //NOI18N

    private boolean createWelcome = true;
    
    public void setCreateWelcome(boolean set) {
        createWelcome = set;
    }
    
    private JSFConfigurationPanel panel;
    /** Creates a new instance of JSFFrameworkProvider */
    public JSFFrameworkProvider() {
        super(
                NbBundle.getMessage(JSFFrameworkProvider.class, "JSF_Name"),               // NOI18N
                NbBundle.getMessage(JSFFrameworkProvider.class, "JSF_Description"));       //NOI18N
    }
    
    // not named extend() so as to avoid implementing WebFrameworkProvider.extend()
    // better to move this to JSFConfigurationPanel
    public Set extendImpl(WebModule webModule) {
        Set result = new HashSet();
        Library jsfLibrary = null;      
        Library jstlLibrary = null;
        
        JSFConfigurationPanel.LibraryType libraryType = panel.getLibraryType();
        if (libraryType == JSFConfigurationPanel.LibraryType.NEW) {
            // create new jsf library
            String libraryName = panel.getNewLibraryName();
            File installFolder = panel.getInstallFolder();
            if (installFolder != null && libraryName != null) {
                try {
                    JSFUtils.createJSFUserLibrary(installFolder, libraryName);
                    jsfLibrary = LibraryManager.getDefault().getLibrary(libraryName);
                } catch (IOException exception) {
                    LOGGER.log(Level.WARNING, "Exception during extending an web project", exception); //NOI18N
                }
            }
        } else {
            if (libraryType == JSFConfigurationPanel.LibraryType.USED) {
                //use a selected library
                jsfLibrary = panel.getLibrary();
                // if the selected library is a default one, add also JSTL library
                if (jsfLibrary.getName().equals(JSFUtils.DEFAULT_JSF_1_2_NAME)
                        || jsfLibrary.getName().equals(JSFUtils.DEFAULT_JSF_2_0_NAME)
                        || jsfLibrary.getName().equals(JSFUtils.DEFAULT_JSF_1_1_NAME)) {
                    jstlLibrary = LibraryManager.getDefault().getLibrary(JSFUtils.DEFAULT_JSTL_1_1_NAME);
                }
            }
        }
        
        try {
            FileObject fileObject = webModule.getDocumentBase();
            FileObject[] javaSources = webModule.getJavaSources();
            if (jsfLibrary != null  && javaSources.length > 0) {
                Library[] libraries;
                if (jstlLibrary != null) {
                    libraries = new Library[]{jsfLibrary, jstlLibrary};
                }
                else {
                    libraries = new Library[]{jsfLibrary};
                }
                // This is a way how to add libraries to the project classpath and
                // packed them to the war file by default.
                ProjectClassPathModifier.addLibraries(libraries, javaSources[0], ClassPath.COMPILE);
            }
            
            boolean isMyFaces;
            if (jsfLibrary != null) {
                // find out whether the added library is myfaces jsf implementation
                List<URL> content = jsfLibrary.getContent("classpath"); //NOI18N
                isMyFaces = Util.containsClass(content, JSFUtils.MYFACES_SPECIFIC_CLASS); 
            } else {
                // find out whether the target server has myfaces jsf implementation on the classpath
                ClassPath cp = ClassPath.getClassPath(fileObject, ClassPath.COMPILE);
                isMyFaces = cp.findResource(JSFUtils.MYFACES_SPECIFIC_CLASS.replace('.', '/') + ".class") != null; //NOI18N
            }            

            FileObject webInf = webModule.getWebInf();
            if (webInf == null) {
                webInf = FileUtil.createFolder(webModule.getDocumentBase(), "WEB-INF"); //NOI18N
            }
            assert webInf != null;
            FileSystem fileSystem = webModule.getWebInf().getFileSystem();
            fileSystem.runAtomicAction(new CreateFacesConfig(webModule, isMyFaces));

            FileObject welcomeFile = (panel!=null && panel.isEnableFacelets()) ? webModule.getDocumentBase().getFileObject(WELCOME_XHTML):
                                                                webModule.getDocumentBase().getFileObject(WELCOME_JSF);
            if (welcomeFile != null) {
                result.add(welcomeFile);
            }
        }  catch (IOException exception) {   
           LOGGER.log(Level.WARNING, "Exception during extending an web project", exception); //NOI18N
        }
        createWelcome = true;

        return result;
    }
    
    public static String readResource(InputStream is, String encoding) throws IOException {
        // read the config from resource first
        StringBuffer sbuffer = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
        String line = br.readLine();
        while (line != null) {
            sbuffer.append(line);
            sbuffer.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sbuffer.toString();
    }
    
    public java.io.File[] getConfigurationFiles(org.netbeans.modules.web.api.webmodule.WebModule wm) {
        // The JavaEE 5 introduce web modules without deployment descriptor. In such wm can not be jsf used.
        assert wm != null;
        FileObject dd = wm.getDeploymentDescriptor();
        if (dd != null){
            FileObject[] filesFO = ConfigurationUtils.getFacesConfigFiles(wm);
            File[] files = new File[filesFO.length];
            for (int i = 0; i < filesFO.length; i++)
                files[i] = FileUtil.toFile(filesFO[i]);
            if (files.length > 0)
                return files;
        }
        return null;
    }
    
    @Override
    public WebModuleExtender createWebModuleExtender(WebModule webModule, ExtenderController controller) {
        boolean defaultValue = (webModule == null || !isInWebModule(webModule));
        if (webModule != null) {
            Project project = FileOwnerQuery.getOwner(webModule.getDocumentBase());
            Preferences preferences = ProjectUtils.getPreferences(project, ProjectUtils.class, true);
            panel = new JSFConfigurationPanel(this, controller, !defaultValue, preferences);
        } else {
            panel = new JSFConfigurationPanel(this, controller, !defaultValue);
        }
        if (!defaultValue){
            // get configuration panel with values from the wm
            Servlet servlet = ConfigurationUtils.getFacesServlet(webModule);
            panel.setServletName(servlet.getServletName());
            panel.setURLPattern(ConfigurationUtils.getFacesServletMapping(webModule));
            panel.setValidateXML(JSFConfigUtilities.validateXML(webModule.getDeploymentDescriptor()));
            panel.setVerifyObjects(JSFConfigUtilities.verifyObjects(webModule.getDeploymentDescriptor()));

            //Facelets
            panel.setDebugFacelets(JSFUtils.debugFacelets(webModule.getDeploymentDescriptor()));
            panel.setSkipComments(JSFUtils.skipCommnets(webModule.getDeploymentDescriptor()));
        }
        
        return panel;
    }

    public boolean isInWebModule(org.netbeans.modules.web.api.webmodule.WebModule webModule) {
        // The JavaEE 5 introduce web modules without deployment descriptor. In such wm can not be jsf used.
        FileObject dd = webModule.getDeploymentDescriptor();
        return (dd != null && ConfigurationUtils.getFacesServlet(webModule) != null);
    }

    public String getServletPath(FileObject file){
        String url = null;
        if (file == null) return url;
        
        WebModule wm = WebModule.getWebModule(file);
        if (wm != null){
            url = FileUtil.getRelativePath(wm.getDocumentBase(), file);
            if (url.charAt(0)!='/')
                url = "/" + url;
            String mapping = ConfigurationUtils.getFacesServletMapping(wm);
            if (mapping != null && !"".equals(mapping)){
                if (mapping.endsWith("/*")){
                    mapping = mapping.substring(0, mapping.length()-2);
                    url = mapping + url;
                }
            }
        }
        return url;
    }
    
    public static void createFile(FileObject target, String content, String encoding) throws IOException{
        FileLock lock = target.lock();
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(target.getOutputStream(lock), encoding));
            bw.write(content);
            bw.close();
            
        } finally {
            lock.releaseLock();
        }
    }
    
    private class  CreateFacesConfig implements FileSystem.AtomicAction{
        private static final String FACES_SERVLET_CLASS = "javax.faces.webapp.FacesServlet";  //NOI18N
        private static final String FACES_SERVLET_NAME = "Faces Servlet";                     //NOI18N  
        private static final String FALSE = "false";                                          //NOI18N  
        private static final String INITPARAM_BEAN_NAME = "InitParam";                        //NOI18N  
        private static final String MYFACES_STARTUP_LISTENER_CLASS = "org.apache.myfaces.webapp.StartupServletContextListener";//NOI18N
        private static final String SAVINGMETHOD_PARAM_NAME = "javax.faces.STATE_SAVING_METHOD";//NOI18N
        private static final String TRUE = "true";                                            //NOI18N
        private static final String VALIDATEXML_PARAM_NAME = "com.sun.faces.validateXml";     //NOI18N  
        private static final String VERIFYOBJECTS_PARAM_NAME = "com.sun.faces.verifyObjects"; //NOI18N

//        private static final String FACELETS_SERVLET_MAPPING="*.jsf"; //NOI18N
        WebModule webModule;
        boolean isMyFaces;
        
        public CreateFacesConfig(WebModule webModule, boolean isMyFaces){
            this.webModule = webModule;
            this.isMyFaces = isMyFaces;
        }
        
        public void run() throws IOException {
            // Enter servlet into the deployment descriptor
            FileObject dd = webModule.getDeploymentDescriptor();
            //we need deployment descriptor, create if null
            if(dd==null)
            {
                dd = DDHelper.createWebXml(webModule.getJ2eeProfile(), webModule.getWebInf());
            }
            //faces servlet mapping
            String facesMapping = panel == null ? "faces/*" : panel.getURLPattern();
            
            WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
            if (ddRoot != null){
                try{
                    boolean servletDefined = false;
                    Servlet servlet;
                    Servlet[] servlets = ddRoot.getServlet();
                    for (int i = 0; i < servlets.length; i++) {
                        servlet = servlets[i];
                        if (servlet == null) {
                            continue;
                        }

                        String servletClass = servlet.getServletClass();
                        if (servletClass == null) {
                            continue;
                        }

                        if (FACES_SERVLET_CLASS.equals(servletClass.trim())) {
                            servletDefined = true;
                            break;
                        }
                    }
                    
                    if (!servletDefined) {
                        servlet = (Servlet)ddRoot.createBean("Servlet"); //NOI18N
                        String servletName = (panel == null) ? FACES_SERVLET_NAME : panel.getServletName();
                        servlet.setServletName(servletName); 
                        servlet.setServletClass(FACES_SERVLET_CLASS); 
                        servlet.setLoadOnStartup(new BigInteger("1"));//NOI18N
                        ddRoot.addServlet(servlet);
                        
                        ServletMapping mapping = (ServletMapping)ddRoot.createBean("ServletMapping"); //NOI18N
                        mapping.setServletName(servletName);//NOI18N
                        mapping.setUrlPattern(panel == null? "/faces/*" : panel.getFacesMapping()); //NOI18N
                        ddRoot.addServletMapping(mapping);
                    }
                    
                    boolean faceletsEnabled = panel.isEnableFacelets();

                    if (isMyFaces) {
                        boolean listenerDefined = false;
                        Listener listeners[] = ddRoot.getListener();
                        for (int i = 0; i < listeners.length; i++) {
                            if (MYFACES_STARTUP_LISTENER_CLASS.equals(listeners[i].getListenerClass().trim())) {
                                listenerDefined = true;
                                break;
                            }
                        }
                        if (!listenerDefined) {
                            Listener facesListener = (Listener) ddRoot.createBean("Listener");  //NOI18N
                            facesListener.setListenerClass(MYFACES_STARTUP_LISTENER_CLASS);
                            ddRoot.addListener(facesListener);
                        }
                    }
                    // add welcome file
                    WelcomeFileList welcomeFiles = ddRoot.getSingleWelcomeFileList();
                    List<String> welcomeFileList = new ArrayList<String>();

                    // add the welcome file only if there not any
                    if (!faceletsEnabled && welcomeFiles == null) {
                        if (facesMapping.charAt(0) == '/') {
                            // if the mapping start with '/' (like /faces/*), then the welcome file can be the mapping
                            if (webModule.getDocumentBase().getFileObject(WELCOME_JSF) != null || createWelcome) {
                                welcomeFileList.add(ConfigurationUtils.translateURI(facesMapping, WELCOME_JSF));
                            }
                        } else {
                            // if the mapping doesn't start '/' (like *.jsf), then the welcome file has to be
                            // a helper file, which will foward the request to the right url
                            welcomeFileList.add(FORWARD_JSF);
                            //copy forwardToJSF.jsp
                            if (facesMapping.charAt(0) != '/' && canCreateNewFile(webModule.getDocumentBase(), FORWARD_JSF)) { //NOI18N
                                String content = readResource(Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_FOLDER + FORWARD_JSF), "UTF-8"); //NOI18N
                                content = content.replace("__FORWARD__", ConfigurationUtils.translateURI(facesMapping, FORWARD_JSF));
                                Charset encoding = FileEncodingQuery.getDefaultEncoding();
                                content = content.replaceAll("__ENCODING__", encoding.name());
                                FileObject target = FileUtil.createData(webModule.getDocumentBase(), FORWARD_JSF);//NOI18N
                                createFile(target, content, encoding.name());  //NOI18N
                            }
                        }
                    } else if (faceletsEnabled && welcomeFiles == null) {
                        welcomeFileList.add(ConfigurationUtils.translateURI(facesMapping, WELCOME_XHTML));
                    }
                    if (welcomeFiles == null && !welcomeFileList.isEmpty()) {
                        welcomeFiles = (WelcomeFileList) ddRoot.createBean("WelcomeFileList"); //NOI18N
                        ddRoot.setWelcomeFileList(welcomeFiles);
                        for (String fileName: welcomeFileList) {
                            welcomeFiles.addWelcomeFile(fileName);
                        }
                    }
                    ddRoot.write(dd);                    
                    
                } catch (ClassNotFoundException cnfe){
                    LOGGER.log(Level.WARNING, "Exception in JSFMoveClassPlugin", cnfe); //NOI18N
                }
            }

            boolean isJSF20 = false;
            Library jsfLibrary = null;
            if (panel.getLibraryType() == JSFConfigurationPanel.LibraryType.USED) {
                jsfLibrary = panel.getLibrary();
            } else if (panel.getLibraryType() == JSFConfigurationPanel.LibraryType.NEW) {
                jsfLibrary = LibraryManager.getDefault().getLibrary(panel.getNewLibraryName());
            }

            if (jsfLibrary !=null) {
                List<URL> content = jsfLibrary.getContent("classpath"); //NOI18N
                isJSF20 = Util.containsClass(content, JSFUtils.JSF_2_0__API_SPECIFIC_CLASS);
            } else {
                ClassPath classpath = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.COMPILE);
                isJSF20 = classpath.findResource(JSFUtils.JSF_2_0__API_SPECIFIC_CLASS.replace('.', '/')+".class")!=null; //NOI18N
            }

            // copy faces-config.xml
            File fileConfig = new File(FileUtil.toFile(webModule.getWebInf()), "faces-config.xml"); // NOI18N
            boolean createFacesConfig = false;
            if (!fileConfig.exists()) {
                // Fix Issue#105180, new project wizard lets me select both jsf and visual jsf.
                // The new faces-config.xml template contains no elements;
                // it's better the framework don't replace user's original one if exist.
                String facesConfigTemplate = "faces-config.xml"; //NOI18N
                if (ddRoot != null) {
                    Profile profile = webModule.getJ2eeProfile();
                    if (profile.equals(Profile.JAVA_EE_5) || profile.equals(Profile.JAVA_EE_6_FULL) || profile.equals(Profile.JAVA_EE_6_WEB)) {
                        if (isJSF20)
                            facesConfigTemplate = "faces-config_2_0.xml"; //NOI18N
                        else
                            facesConfigTemplate = "faces-config_1_2.xml"; //NOI18N
                    }
                    if (!profile.equals(Profile.JAVA_EE_6_FULL) && !profile.equals(Profile.JAVA_EE_6_WEB) && !isJSF20) {
                        createFacesConfig = true;
                    }
                }
                if (createFacesConfig) {
                    String content = readResource(Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_FOLDER + facesConfigTemplate), "UTF-8"); //NOI18N
                    FileObject target = FileUtil.createData(webModule.getWebInf(), "faces-config.xml");//NOI18N
                    createFile(target, content, "UTF-8"); //NOI18N
                }
            }

            //If Facelets enabled need to add view-handler
            if (panel.isEnableFacelets()) {
                FileObject files[] = ConfigurationUtils.getFacesConfigFiles(webModule);
                if (files != null && files.length > 0) {
                    JSFConfigModel model = ConfigurationUtils.getConfigModel(files[0], true);
                    FacesConfig jsfConfig = model.getRootComponent();
                    if (jsfConfig != null){
                        Application application = null;
                        boolean newApplication = false;

                        List<Application> applications = jsfConfig.getApplications();
                        if (applications != null && applications.size() > 0){
                            List<ViewHandler> handlers = applications.get(0).getViewHandlers();
                            boolean alreadyDefined = false;
                            if (handlers != null){
                                for (ViewHandler viewHandler : handlers) {
                                    if (HANDLER.equals(viewHandler.getFullyQualifiedClassType().trim())){
                                        alreadyDefined = true;
                                        break;
                                    }
                                }
                            }
                            if (!alreadyDefined){
                                application = applications.get(0);
                            }
                        } else {
                            application = model.getFactory().createApplication();
                            newApplication = true;
                        }
                        if (application != null){
                            model.startTransaction();
                            if (newApplication) {
                                jsfConfig.addApplication(application);
                            }
                            //In JSF2.0 no need to add HANDLER need to change version of faces-config instead
                            if (!isJSF20 && !isMyFaces) {
                                ViewHandler viewHandler = model.getFactory().createViewHandler();
                                viewHandler.setFullyQualifiedClassType(HANDLER);
                                application.addViewHandler(viewHandler);
                            }
                            ClassPath cp = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.COMPILE);
                            if (panel.getLibrary()!=null && panel.getLibrary().getName().indexOf("facelets-icefaces") != -1 //NOI18N
                                    && cp != null && cp.findResource("com/icesoft/faces/facelets/D2DFaceletViewHandler.class") != null){    //NOI18N
                                ViewHandler iceViewHandler = model.getFactory().createViewHandler();
                                iceViewHandler.setFullyQualifiedClassType("com.icesoft.faces.facelets.D2DFaceletViewHandler");  //NOI18N
                                application.addViewHandler(iceViewHandler);
                            }
                            model.endTransaction();
                            model.sync();
                        }
                    }
                }

            }
            final String baseFolder = "org/netbeans/modules/web/jsf/facelets/resources/templates/"; //NOI18N

            if (panel.isEnableFacelets() && panel.isCreateExamples()) {
                InputStream is;
                String content;
                FileObject target;
                Charset encoding = FileEncodingQuery.getDefaultEncoding();

                if (webModule.getDocumentBase().getFileObject(TEMPLATE_XHTML) == null){
                    if (isJSF20) {
                        is= JSFFrameworkProvider.class.getClassLoader()
                            .getResourceAsStream(baseFolder + TEMPLATE_XHTML2);
                    } else {
                        is= JSFFrameworkProvider.class.getClassLoader()
                            .getResourceAsStream(baseFolder + TEMPLATE_XHTML);
                    }
                    content = readResource(is, encoding.name());
                    target = FileUtil.createData(webModule.getDocumentBase(), TEMPLATE_XHTML);
                    createFile(target, content, encoding.name());
                }
                if (webModule.getDocumentBase().getFileObject(WELCOME_XHTML) == null){
                    is = JSFFrameworkProvider.class.getClassLoader()
                    .getResourceAsStream(baseFolder + WELCOME_XHTML);
                    content = readResource(is, encoding.name());
                    target = FileUtil.createData(webModule.getDocumentBase(), WELCOME_XHTML);
                    createFile(target, content, encoding.name());
                }
                String defaultCSSFolder = CSS_FOLDER;
                if (isJSF20) {
                    defaultCSSFolder = CSS_FOLDER2;
                }
                if (webModule.getDocumentBase().getFileObject(defaultCSSFolder+File.separator+DEFAULT_CSS) == null){
                    is = JSFFrameworkProvider.class.getClassLoader().getResourceAsStream(baseFolder + DEFAULT_CSS);  
                    content = readResource(is, encoding.name());
                    target = FileUtil.createData(webModule.getDocumentBase(), defaultCSSFolder + File.separator+DEFAULT_CSS);
                    createFile(target, content, encoding.name());
                }
            }
            //copy Welcome.jsp
            if (!panel.isEnableFacelets() && createWelcome && canCreateNewFile(webModule.getDocumentBase(), WELCOME_JSF)) {
                String content = readResource(Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_FOLDER + WELCOME_JSF), "UTF-8"); //NOI18N
                Charset encoding = FileEncodingQuery.getDefaultEncoding();
                content = content.replaceAll("__ENCODING__", encoding.name());
                FileObject target = FileUtil.createData(webModule.getDocumentBase(), WELCOME_JSF);
                createFile(target, content, encoding.name());  
            }
        }
        
        private boolean canCreateNewFile(FileObject parent, String name){
            File fileToBe = new File(FileUtil.toFile(parent), name);
            boolean create = true;
            if (fileToBe.exists()){
                DialogDescriptor dialog = new DialogDescriptor(
                        NbBundle.getMessage(JSFFrameworkProvider.class, "MSG_OverwriteFile", fileToBe.getAbsolutePath()),
                        NbBundle.getMessage(JSFFrameworkProvider.class, "TTL_OverwriteFile"),
                        true, DialogDescriptor.YES_NO_OPTION, DialogDescriptor.NO_OPTION, null);
                java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
                d.setVisible(true);
                create = (dialog.getValue() == org.openide.DialogDescriptor.NO_OPTION);
            }
            return create;
        }   
    }
}
