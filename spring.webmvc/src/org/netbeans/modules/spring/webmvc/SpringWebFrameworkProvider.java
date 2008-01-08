package org.netbeans.modules.spring.webmvc;

/*
 * Copyright 2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import org.netbeans.modules.spring.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.CreateCapability;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.FrameworkConfigurationPanel;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Provides WebFrameworkProvider implementation for Spring Framework
 *
 * @author Craig MacKay
 */
public class SpringWebFrameworkProvider extends WebFrameworkProvider {

    public static final String SPRING_LIB_NAME = "spring-framework-2.5";
    public static final String CONTEXT_LOADER = "org.springframework.web.context.ContextLoaderListener";
    public static final String DISPATCHER_SERVLET = "org.springframework.web.servlet.DispatcherServlet";
    public static final String ENCODING = "UTF-8";

    private SpringConfigPanel panel;

    public SpringWebFrameworkProvider() {
        super(NbBundle.getMessage(SpringWebFrameworkProvider.class, "FrameworkProvider.name"), NbBundle.getMessage(SpringWebFrameworkProvider.class, "FrameworkProvider.description"));
    }

    @Override
    public Set extend(WebModule webModule) {
        EnableFrameworkAction enableFrameworkAction = new EnableFrameworkAction(webModule, getSpringConfigPanel(webModule));
        FileObject webInf = webModule.getWebInf();
        if (webInf != null) {
            try {
                FileSystem fs = webInf.getFileSystem();
                fs.runAtomicAction(enableFrameworkAction);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
                return null;
            }
        }        
        return enableFrameworkAction.getFilesToOpen();
    }

    @Override
    public boolean isInWebModule(WebModule webModule) {
        boolean isInWebModule = false;
        try {
            WebApp webApp = getWebApp(webModule);
            isInWebModule = (webApp.findBeanByName("Servlet", "ServletClass", DISPATCHER_SERVLET) != null) || (webApp.findBeanByName("Listener", "ListenerClass", CONTEXT_LOADER) != null);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        return isInWebModule;
    }

    @Override
    public File[] getConfigurationFiles(WebModule webModule) {
        FileObject webInf = webModule.getWebInf();
        List<File> files = new ArrayList<File>();
        if (webModule.getDeploymentDescriptor() != null) {
            FileObject file = webInf.getFileObject("applicationContext.xml");
            if (file != null) {
                files.add(FileUtil.toFile(file));
            }
            file = webInf.getFileObject("dispatcher-servlet.xml");
            if (file != null) {
                files.add(FileUtil.toFile(file));
            }
        }
        return files.toArray(new java.io.File[0]);
    }

    @Override
    public FrameworkConfigurationPanel getConfigurationPanel(WebModule webModule) {
        return getSpringConfigPanel(webModule);
    }

    protected SpringConfigPanel getSpringConfigPanel(WebModule webModule) {
        if (panel == null) {
            panel = new SpringConfigPanel();
        }
        panel.setWebModule(webModule);
        return panel;
    }

    public WebApp getWebApp(WebModule webModule) throws IOException {
        return DDProvider.getDefault().getDDRoot(webModule.getDeploymentDescriptor());
    }

    public WebApp getWebAppCopy(WebModule webModule) throws IOException {
        return DDProvider.getDefault().getDDRootCopy(webModule.getDeploymentDescriptor());
    }

    private class EnableFrameworkAction implements FileSystem.AtomicAction {

        private Set<FileObject> filesToOpen = new LinkedHashSet<FileObject>();
        private WebModule webModule;
        private SpringConfigPanel frameworkPanel;

        public EnableFrameworkAction(WebModule webModule, SpringConfigPanel frameworkPanel) {
            this.webModule = webModule;
            this.frameworkPanel = frameworkPanel;
        }

        public void run() throws IOException {
            // MODIFY WEB.XML
            FileObject dd = webModule.getDeploymentDescriptor();
            WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
            addContextParam(ddRoot, "contextConfigLocation", "/WEB-INF/applicationContext.xml");
            addListener(ddRoot, CONTEXT_LOADER);
            addServlet(ddRoot, frameworkPanel.getDispatcherName(), DISPATCHER_SERVLET, frameworkPanel.getDispatcherMapping(), "2");
            WelcomeFileList welcomeFiles = ddRoot.getSingleWelcomeFileList();
            if (welcomeFiles == null) {
                try {
                    welcomeFiles = (WelcomeFileList) ddRoot.createBean("WelcomeFileList");
                    ddRoot.setWelcomeFileList(welcomeFiles);
                } catch (ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (welcomeFiles.sizeWelcomeFile() == 0) {
                welcomeFiles.addWelcomeFile("index.jsp");
            }
            ddRoot.write(dd);

            // ADD SPRING LIBRARY
            addLibraryToWebModule(getLibrary(SPRING_LIB_NAME), webModule);
            
            // CREATE WEB-INF/JSP FOLDER
            FileObject webInf = webModule.getWebInf();
            FileObject jsp = webInf.createFolder("jsp");

            // COPY TEMPLATE SPRING RESOURCES (JSP, XML, PROPERTIES)
            copyResource("index.jsp", FileUtil.createData(jsp, "index.jsp"));
            copyResource("taglibs.jsp", FileUtil.createData(jsp, "taglibs.jsp"));
            copyResource("header.jsp", FileUtil.createData(jsp, "header.jsp"));
            copyResource("footer.jsp", FileUtil.createData(jsp, "footer.jsp"));
            copyResource("jdbc.properties", FileUtil.createData(webInf, "jdbc.properties"));
            addFileToOpen(copyResource("applicationContext.xml", FileUtil.createData(webInf, "applicationContext.xml")));
            addFileToOpen(copyResource("dispatcher-servlet.xml", FileUtil.createData(webInf, frameworkPanel.getDispatcherName() + "-servlet.xml")));

            // MODIFY EXISTING INDEX.JSP
            FileObject documentBase = webModule.getDocumentBase();
            FileObject indexJsp = documentBase.getFileObject("index.jsp");
            if (indexJsp == null) {
                indexJsp = FileUtil.createData(documentBase, "index.jsp");
            }
            addFileToOpen(copyResource("redirect.jsp", indexJsp));
        }
        
        public void addFileToOpen(FileObject file) {
            filesToOpen.add(file);
        }

        public Set<FileObject> getFilesToOpen() {
            return filesToOpen;
        }

        protected FileObject copyResource(String resourceName, FileObject target) throws UnsupportedEncodingException, IOException {
            InputStream in = getClass().getResourceAsStream("templates/resources/" + resourceName);
            String lineSeparator = System.getProperty("line.separator");
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, ENCODING));
            try {
                String line = reader.readLine();
                while (line != null) {
                    buffer.append(line);
                    buffer.append(lineSeparator);
                    line = reader.readLine();
                }
            } finally {
                reader.close();
            }
            FileLock lock = target.lock();
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(target.getOutputStream(lock), ENCODING));
                try {
                    writer.write(buffer.toString());
                } finally {
                    writer.close();
                }
            } finally {
                lock.releaseLock();
            }
            return target;
        }

        @SuppressWarnings(value = "deprecation")
        protected void addLibraryToWebModule(Library library, WebModule webModule) throws IOException {
            FileOwnerQuery.getOwner(webModule.getDocumentBase()).getLookup().lookup(org.netbeans.spi.java.project.classpath.ProjectClassPathExtender.class).addLibrary(library);
        }

        protected Listener addListener(WebApp webApp, String classname) throws IOException {
            Listener listener = (Listener) createBean(webApp, "Listener");
            listener.setListenerClass(classname);
            webApp.addListener(listener);
            return listener;
        }

        protected Servlet addServlet(WebApp webApp, String name, String classname, String pattern, String loadOnStartup) throws IOException {
            Servlet servlet = (Servlet) createBean(webApp, "Servlet");
            servlet.setServletName(name);
            servlet.setServletClass(classname);
            if (loadOnStartup != null) {
                servlet.setLoadOnStartup(new BigInteger(loadOnStartup));
            }
            webApp.addServlet(servlet);
            if (pattern != null) {
                addServletMapping(webApp, name, pattern);
            }
            return servlet;
        }
        
        protected ServletMapping addServletMapping(WebApp webApp, String name, String pattern) throws IOException {
            ServletMapping mapping = (ServletMapping) createBean(webApp, "ServletMapping");
            mapping.setServletName(name);
            mapping.setUrlPattern(pattern);
            webApp.addServletMapping(mapping);
            return mapping;
        }

        protected InitParam addContextParam(WebApp webApp, String name, String value) throws IOException {
            InitParam initParam = (InitParam) createBean(webApp, "InitParam");
            initParam.setParamName(name);
            initParam.setParamValue(value);
            webApp.addContextParam(initParam);
            return initParam;
        }

        protected CommonDDBean createBean(CreateCapability creator, String beanName) throws IOException {
            CommonDDBean bean = null;
            try {
                bean = creator.createBean(beanName);
            } catch (ClassNotFoundException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                throw new IOException("Error creating bean with name:" + beanName);
            }
            return bean;
        }

        protected Library getLibrary(String name) {
            return LibraryManager.getDefault().getLibrary(name);
        }

        protected FileSystem getDefaultFileSystem() {
            return Repository.getDefault().getDefaultFileSystem();
        }
    }
}
