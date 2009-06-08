/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.frameworks.facelets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.frameworks.facelets.ui.FaceletsSetupPanel;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.ViewHandler;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class FaceletsFrameworkProvider extends WebFrameworkProvider{
    public enum ExtendType{GUI, NEWFILE};
    
    private static String HANDLER = "com.sun.facelets.FaceletViewHandler";                          //NOI18N
    
    private FaceletsSetupPanel frameworkSetup;
    private Object extendData;
    
    public FaceletsFrameworkProvider(){
        super(NbBundle.getMessage(FaceletsFrameworkProvider.class, "Facelets_Name"),               //NOI18N
                NbBundle.getMessage(FaceletsFrameworkProvider.class, "Facelets_Description"));     //NOI18N
    
        extendData = null;
    }
    
    public void setExtendData(Object data) {
        extendData = data;
    }
       
    public boolean isInWebModule(WebModule webModule) {
        boolean result = false;
        FileObject dd = webModule.getDeploymentDescriptor();
        if (dd != null && ConfigurationUtils.getFacesServlet(webModule) != null) {
            // get all faces configuration files in the web project
            FileObject[] configFO = ConfigurationUtils.getFacesConfigFiles(webModule);
            if (configFO != null){
                for (int i = 0; !result && i < configFO.length; i++) {
                    // obtain for every configuration file the model
                    FacesConfig facesConfig = ConfigurationUtils.getConfigModel(configFO[i], true).getRootComponent();
                    List<Application> applications = null;
                    if (facesConfig != null && ((applications = facesConfig.getApplications()) != null)) {
                        for (Application application : applications) {
                            List<ViewHandler> viewHandlers = application.getViewHandlers();
                            for (ViewHandler viewHandler : viewHandlers) {
                               if (FaceletsUtils.FACELETS_VIEW_HANDLER.equals(viewHandler.getFullyQualifiedClassType().trim())) {
                                    result = true;
                                    break;
                               }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public WebModuleExtender createWebModuleExtender(WebModule webModule, ExtenderController controller) {
        boolean defaultValue = (webModule == null || !isInWebModule(webModule));
        frameworkSetup = new FaceletsSetupPanel(this, controller, !defaultValue);

        if (!defaultValue){
            frameworkSetup.setDebugFacelets(FaceletsUtils.debugFacelets(webModule.getDeploymentDescriptor()));
            frameworkSetup.setSkipComments(FaceletsUtils.skipCommnets(webModule.getDeploymentDescriptor()));
        }
        return frameworkSetup;
    }
    
    public Set extendImpl(WebModule webModule) {
        if (isInWebModule(webModule)) {
            return null;
        }

        boolean extendByNewFile = (extendData == ExtendType.NEWFILE);
        extendData = null;

        Library core = null;
        FaceletsSetupPanel.LibraryType libraryType = frameworkSetup.getLibraryType();
        if (libraryType == FaceletsSetupPanel.LibraryType.USED) {
            core = frameworkSetup.getLibrary();
        } else if (libraryType == FaceletsSetupPanel.LibraryType.NEW) {
            String libraryVersion = frameworkSetup.getNewLibraryVersion();
            File installFolder = frameworkSetup.getInstallFolder();
            if (installFolder != null && libraryVersion != null) {
                try {
                    FaceletsUtils.createFaceletsUserLibrary(installFolder, libraryVersion);
                    FaceletsUtils.createFaceletsJSFUserLibrary(installFolder, libraryVersion);
                    FaceletsUtils.createFaceletsMyFacesUserLibrary(installFolder, libraryVersion);
                    core = FaceletsUtils.getCoreLibrary(libraryVersion);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }

        if (extendByNewFile && (core == null)) {
            for (Library lib : LibraryManager.getDefault().getLibraries()) {
                if (lib.getName().startsWith("facelets-") && !lib.getName().endsWith("el-api")    //NOI18N
                && !lib.getName().endsWith("jsf-ri") && !lib.getName().endsWith("myfaces")){      //NOI18N
                    core = lib;
                    break;
                }
            }
        }

        FileObject documentBase = webModule.getDocumentBase();
        try {
            if (core != null) {
                ClassPath cp = ClassPath.getClassPath(documentBase, ClassPath.COMPILE);
                ArrayList<Library> libraries = new ArrayList<Library>();
                libraries.add(core);

                // is an el expression api on the classpath already?
                if (cp.findResource("javax/el/ELException.class") == null) { // NOI18N
                    Library elapi = LibraryManager.getDefault().getLibrary(core.getName() + "-el-api"); // NOI18N
                    if (elapi != null) {
                        libraries.add(elapi);
                    }
                }

                // is a jsf api on the classpath already?
                if (cp.findResource("javax/faces/FacesException.class") == null) { // NOI18N
                    Library jsfri = LibraryManager.getDefault().getLibrary("jsf12"); // NOI18N
                    if (jsfri != null) {
                        libraries.add(jsfri);
                        Library jstl = LibraryManager.getDefault().getLibrary("jstl11"); // NOI18N
                        if (jstl != null) {
                            libraries.add(jstl);
                        }
                    } else {
                        Library myfaces = LibraryManager.getDefault().getLibrary(core.getName() + "-myfaces"); // NOI18N
                        if (myfaces != null) {
                            libraries.add(myfaces);
                        }
                    }
                }

                FileObject[] javaSources = webModule.getJavaSources();
                if (javaSources.length > 0) {
                    ProjectClassPathModifier.addLibraries(libraries.toArray(new Library[0]), javaSources[0], ClassPath.COMPILE);
                }
            }

            FileSystem fs = webModule.getWebInf().getFileSystem();
            fs.runAtomicAction(new Modifier(webModule, extendByNewFile));
            if (documentBase.getFileObject("template.xhtml") == null || documentBase.getFileObject("template-client.xhtml") == null) { // NOI18N
                Set openedFiles = new HashSet();
                if (documentBase.getFileObject("template.xhtml") == null) { // NOI18N
                    openedFiles.add(documentBase.getFileObject("template.xhtml")); // NOI18N
                }
                if (documentBase.getFileObject("template-client.xhtml") == null) { // NOI18N
                    openedFiles.add(documentBase.getFileObject("template-client.xhtml")); // NOI18N
                }

                return openedFiles;
            }
        } catch (FileNotFoundException exc) {
            ErrorManager.getDefault().notify(exc);
        } catch (IOException exc) {
            ErrorManager.getDefault().notify(exc);
        }

        return null;
    }

    public File[] getConfigurationFiles(WebModule webModule) {
        FileObject dd = webModule.getDeploymentDescriptor();
        if (dd != null){
            FileObject[] filesFO = ConfigurationUtils.getFacesConfigFiles(webModule);
            File[] files = new File[filesFO.length];
            for (int i = 0; i < filesFO.length; i++)
                files[i] = FileUtil.toFile(filesFO[i]);
            if (files.length > 0)
                return files;
        }
        return null;
    }
       
    private class Modifier implements FileSystem.AtomicAction{
        
        private WebModule webModule;
        private boolean extendByNewFile;
        
        public Modifier(WebModule webModule, boolean extendByNewFile){
            this.webModule = webModule;
            this.extendByNewFile = extendByNewFile;
        }
        
        public void run() throws IOException {
            // alter deployment descriptor
            FileObject deploymentDescriptor = webModule.getDeploymentDescriptor();
            WebApp ddRoot = DDProvider.getDefault().getDDRoot(deploymentDescriptor);
            if (ddRoot != null){
                try{
                    InitParam contextParam;
                    
                    if (ConfigurationUtils.getFacesServlet(webModule) == null){
                        // add faces servlet declaration if is not there yet (issue #5)
                        Servlet servlet = (Servlet)ddRoot.createBean("Servlet"); //NOI18N
                        String servletName = "Faces Servlet";
                        servlet.setServletName(servletName); //NOI18N
                        servlet.setServletClass("javax.faces.webapp.FacesServlet"); //NOI18N
                        servlet.setLoadOnStartup(new BigInteger("1"));
                        ddRoot.addServlet(servlet);
                        
                        servlet.setLoadOnStartup(new BigInteger("1"));//NOI18N
                        
                        
                        ServletMapping mapping = (ServletMapping)ddRoot.createBean("ServletMapping"); //NOI18N
                        mapping.setServletName(servletName);//NOI18N
                        mapping.setUrlPattern("*.jsf");//NOI18N
                        
                        ddRoot.addServletMapping(mapping);
                        
                        contextParam = (InitParam)ddRoot.createBean("InitParam"); //NOI18N
                        contextParam.setParamName("com.sun.faces.verifyObjects");  //NOI18N
                        contextParam.setParamValue("true");
                        ddRoot.addContextParam(contextParam);
                        
                        contextParam = (InitParam)ddRoot.createBean("InitParam"); //NOI18N
                        contextParam.setParamName("com.sun.faces.validateXml"); //NOI18N
                        contextParam.setParamValue("true"); //NOI18N
                        ddRoot.addContextParam(contextParam);
                    }
                    
                    if (findInitParamInDD(ddRoot, FaceletsUtils.FACELETS_DEFAULT_SUFFIX)==null){
                        contextParam = (InitParam)ddRoot.createBean("InitParam"); //NOI18N
                        contextParam.setParamName(FaceletsUtils.FACELETS_DEFAULT_SUFFIX); //NOI18N
                        contextParam.setParamValue(".xhtml"); //NOI18N
                        ddRoot.addContextParam(contextParam);
                    }
                    if (findInitParamInDD(ddRoot, FaceletsUtils.FACELETS_DEVELOPMENT)==null){
                        contextParam = (InitParam)ddRoot.createBean("InitParam"); //NOI18N
                        contextParam.setParamName(FaceletsUtils.FACELETS_DEVELOPMENT);
                        contextParam.setParamValue(frameworkSetup.isDebugFacelets()?"true":"false"); //NOI18N
                        ddRoot.addContextParam(contextParam);
                    }
                    if (findInitParamInDD(ddRoot, FaceletsUtils.FACELETS_SKIPCOMMNETS)==null){
                        contextParam = (InitParam)ddRoot.createBean("InitParam"); //NOI18N
                        contextParam.setParamName(FaceletsUtils.FACELETS_SKIPCOMMNETS); //NOI18N
                        contextParam.setParamValue(frameworkSetup.isSkipComments()?"true":"false"); //NOI18N
                        ddRoot.addContextParam(contextParam);
                    }
                    
                    WelcomeFileList welcomeFiles = ddRoot.getSingleWelcomeFileList();
                    if (welcomeFiles == null) {
                        welcomeFiles = (WelcomeFileList) ddRoot.createBean("WelcomeFileList");//NOI18N
                        ddRoot.setWelcomeFileList(welcomeFiles);
                    }
                    if (welcomeFiles.sizeWelcomeFile() == 0) {
                        welcomeFiles.addWelcomeFile("forward.jsp"); //NOI18N
                    }
                    /*contextParam = (InitParam)ddRoot.createBean("InitParam"); //NOI18N
                    contextParam.setParamName("javax.faces.STATE_SAVING_METHOD"); //NOI18N
                    contextParam.setParamValue("client"); //NOI18N
                    ddRoot.addContextParam(contextParam);
                     
                    if (isMyFaces) {
                        Listener facesListener = (Listener) ddRoot.createBean("Listener");
                        facesListener.setListenerClass("org.apache.myfaces.webapp.StartupServletContextListener");
                        ddRoot.addListener(facesListener);
                    }*/
                    ddRoot.write(deploymentDescriptor);
                    
                    
                } catch (ClassNotFoundException cnfe){
                    ErrorManager.getDefault().notify(cnfe);
                }
            }
            
            // configuration file
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
                        ViewHandler viewHandler = model.getFactory().createViewHandler();
                        viewHandler.setFullyQualifiedClassType(HANDLER);
                        application.addViewHandler(viewHandler);                        
                        ClassPath cp = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.COMPILE);
                        if (frameworkSetup.getLibrary().getName().indexOf("facelets-icefaces") != -1
                                && cp != null && cp.findResource("com/icesoft/faces/facelets/D2DFaceletViewHandler.class") != null){
                            ViewHandler iceViewHandler = model.getFactory().createViewHandler();
                            iceViewHandler.setFullyQualifiedClassType("com.icesoft.faces.facelets.D2DFaceletViewHandler");
                            application.addViewHandler(iceViewHandler);
                        }
                        model.endTransaction();
                        model.sync();
                    }
                }
            } else {
                InputStream is = FaceletsFrameworkProvider.class.getClassLoader()
                    .getResourceAsStream("org/netbeans/modules/web/frameworks/facelets/resources/faces-config-12.xml"); //NOI18N
                String content = FaceletsUtils.readResource(is, "UTF-8");
                FileObject target = FileUtil.createData(webModule.getWebInf(), "faces-config.xml");//NOI18N
                FaceletsUtils.createFile(target, content, "UTF-8");
            }
            
            final String baseFolder = "org/netbeans/modules/web/frameworks/facelets/resources/templates/"; //NOI18N
            
            if (!extendByNewFile && frameworkSetup.isCreateExamples()){
                InputStream is;
                String content;
                FileObject target;
                
                if (webModule.getDocumentBase().getFileObject("template.xhtml") == null){ //NOI18N
                    is= FaceletsFrameworkProvider.class.getClassLoader()
                    .getResourceAsStream(baseFolder + "template.xhtml");
                    content = FaceletsUtils.readResource(is, "UTF-8");
                    target = FileUtil.createData(webModule.getDocumentBase(), "template.xhtml");//NOI18N
                    FaceletsUtils.createFile(target, content, "UTF-8");
                }
                if (webModule.getDocumentBase().getFileObject("template-client.xhtml") == null){
                    is = FaceletsFrameworkProvider.class.getClassLoader()
                    .getResourceAsStream(baseFolder + "template-client.xhtml");
                    content = FaceletsUtils.readResource(is, "UTF-8");
                    target = FileUtil.createData(webModule.getDocumentBase(), "template-client.xhtml");//NOI18N
                    FaceletsUtils.createFile(target, content, "UTF-8");
                }
                if (webModule.getDocumentBase().getFileObject("forward.jsp") == null) {
                    is = FaceletsFrameworkProvider.class.getClassLoader().getResourceAsStream(baseFolder + "forward.jsp");
                    content = FaceletsUtils.readResource(is, "UTF-8");
                    target = FileUtil.createData(webModule.getDocumentBase(), "forward.jsp");//NOI18N
                    FaceletsUtils.createFile(target, content, "UTF-8");
                }
                if (webModule.getDocumentBase().getFileObject("css/default.css") == null){
                    is = FaceletsFrameworkProvider.class.getClassLoader()
                    .getResourceAsStream(baseFolder + "default.css");   //NOI18N
                    content = FaceletsUtils.readResource(is, "UTF-8");
                    target = FileUtil.createData(webModule.getDocumentBase().createFolder("css"), "default.css");//NOI18N
                    FaceletsUtils.createFile(target, content, "UTF-8");
                }
            }
        }
        
        private InitParam findInitParamInDD(WebApp webApp, String paramName){
            return (InitParam) webApp
                    .findBeanByName("InitParam", "ParamName", paramName); //NOI18N;
        }
    }
}

