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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.project.jsf.api;

import org.netbeans.modules.visualweb.project.jsf.JsfProjectTemplateJakarta;
import org.netbeans.modules.visualweb.project.jsf.actions.ImportFileAction;
import org.netbeans.modules.visualweb.project.jsf.libraries.LibraryDefinition;
import org.netbeans.modules.visualweb.project.jsf.libraries.J2SELibraryDefinition;
import org.netbeans.modules.visualweb.project.jsf.libraries.ComponentLibraryDefinition;
import org.netbeans.modules.visualweb.project.jsf.libraries.JsfProjectLibrary;
import org.netbeans.modules.visualweb.api.j2ee.common.RequestedEjbResource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.WeakHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.awt.EventQueue;
import javax.swing.JFileChooser ;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.api.WebProjectLibrariesModifier;
import org.netbeans.modules.web.project.api.WebPropertyEvaluator;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
// XXX wait for NetBeans API
//import org.netbeans.modules.project.ui.ProjectTab;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.ProjectManager;
import org.openide.util.Mutex;


/**
 *
 * @author Po-Ting Wu
 */
public class JsfProjectUtils {
    public final static String PATH_IN_WAR_LIB = "WEB-INF/lib"; //NOI18N
    public final static String SUN_WEB_XML_PATH = "web/WEB-INF/sun-web.xml"; // NOI18N

    private final static String RAVE_AUX_NAMESPACE = "http://www.sun.com/creator/ns";
    private final static String RAVE_AUX_NAME = "creator-data";
    private final static String[] CreatorProperties = {
        JsfProjectConstants.PROP_CURRENT_THEME,
        JsfProjectConstants.PROP_JSF_PAGEBEAN_PACKAGE,
        JsfProjectConstants.PROP_JSF_PROJECT_LIBRARIES_DIR,
        JsfProjectConstants.PROP_START_PAGE
    };

    private static final Map<Project,ArrayList> propertyListeners = new WeakHashMap();

    /**
     * Provides the project template to be used to instantiate the project contents
     * May be overridden by subclasses to produce project variants
     * @return The project template class that will perform the instantiation of project contents
     */
    public static ProjectTemplate getProjectTemplate() {
        return new JsfProjectTemplateJakarta();
    }

    /**
     * Check for Creator project
     * @param project Project to be checked
     */
    public static boolean isJsfProject(Project project) {
        String version = getProjectVersion(project);
        return version != null && version.length() > 0;
    }

    public static boolean isWebProject(Project project) {
        if (project == null) {
            return false;
        }
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        return wm != null;
    }

    private static final Map<FileObject,Boolean> JsfProjectFile = new WeakHashMap();

    /**
     * Check for Creator project file. Note: For DataLoader only when 'Project' is not available.
     * @param fo FileObject to be checked
     */
    public static boolean isJsfProjectFile(FileObject fo) {
        while (fo != null) {
            if (fo.isFolder()) {
                final FileObject projXml = fo.getFileObject("nbproject/project.xml"); // NOI18N
                // Found the project root directory and got the project.xml file
                if (projXml != null) {
                    Boolean ret = (Boolean) JsfProjectFile.get(projXml);
                    if (ret != null) {
                        return ret.booleanValue();
                    }

                    try {
                        Document doc = XMLUtil.parse(new InputSource(FileUtil.toFile(projXml).toURI().toString()), false, true, null,
                            new EntityResolver() {
                                public InputSource resolveEntity(String pubid, String sysid) throws SAXException, IOException {
                                    return new InputSource(new ByteArrayInputStream(new byte[0]));
                                }
                            });
                        NodeList nlist = doc.getElementsByTagNameNS(RAVE_AUX_NAMESPACE, RAVE_AUX_NAME);
                        if (nlist.getLength() > 0) {
                            JsfProjectFile.put(projXml, Boolean.TRUE);
                            return true;
			}
                    } catch (Exception e) {
                        // Try the old Creator format later
                    }
                }

                final FileObject propFile = fo.getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                // Found the project root directory and got the project.properties file
                if (propFile != null) {
                    Boolean ret = (Boolean) JsfProjectFile.get(propFile);
                    if (ret != null) {
                        return ret.booleanValue();
                    }

                    try {
                        EditableProperties prop = new EditableProperties();
                        InputStream is = propFile.getInputStream();
                        
                        prop.load(is);
                        is.close();

                        // Check Creator property
                        boolean isJsf = prop.getProperty("jsf.pagebean.package") != null; // NOI18N
                        JsfProjectFile.put(propFile, Boolean.valueOf(isJsf));
                        if (projXml != null) { // Also put xml file here so that it won't be rescaned every time before coming here
                            JsfProjectFile.put(projXml, Boolean.valueOf(isJsf));
                        }
                        return isJsf;
                    } catch (Exception e) {
                        JsfProjectFile.put(propFile, Boolean.FALSE);
                        if (projXml != null) {
                            JsfProjectFile.put(projXml, Boolean.FALSE);
                        }
                        return false;
                    }
                }
            }

            fo = fo.getParent();
        }

        return false;
    }

    public static String getProjectVersion(Project project) {
        return getProjectProperty(project, JsfProjectConstants.PROP_JSF_PROJECT_VERSION);
    }

    public static void setProjectVersion(Project project, String version) {
        createProjectProperty(project, JsfProjectConstants.PROP_JSF_PROJECT_VERSION, version);

        FileObject projDir = project.getProjectDirectory() ;
        FileObject projXml = projDir.getFileObject("nbproject/project.xml"); // NOI18N
        JsfProjectFile.put(projXml, Boolean.TRUE);
    }

    public static String getProjectProperty(Project project, String propName) {
        if (isWebProject(project)) {
            AuxiliaryConfiguration ac = (AuxiliaryConfiguration)project.getLookup().lookup(AuxiliaryConfiguration.class);
            if (ac == null) {
                return "";
            }
            
            Element auxElement = ac.getConfigurationFragment(RAVE_AUX_NAME, RAVE_AUX_NAMESPACE, true);
            if (auxElement == null) {  // Creator 2 project
                return getCreatorProperty(project, propName);
            }
            String value = auxElement.getAttribute(propName);
            if (value == null || value.equals("")) {  // Creator 2 project
                return getCreatorProperty(project, propName);
            }
            return value;
        } else
            return "";
    }
    
    private static String getCreatorProperty(final Project project, String propName) {
        EditableProperties props;
        WebPropertyEvaluator wpe = (WebPropertyEvaluator) project.getLookup().lookup(WebPropertyEvaluator.class);
        if (wpe != null) {
            PropertyEvaluator pe = wpe.evaluator();
            props = new EditableProperties(pe.getProperties());
        } else {
            // Can't find anything, try to read the project.properties file directly. Shouldn't be here.
            try {
                props = (EditableProperties) ProjectManager.mutex().readAccess(new Mutex.ExceptionAction() {
                    public Object run() throws Exception {
                        EditableProperties ep = new EditableProperties();
                        FileObject propFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        InputStream is = propFile.getInputStream();
            
                        ep.load(is);
                        is.close();

                        return ep;
                    }
                });
            } catch (Exception e) {
                return "";
            }
        }

        // Store Creator properties into the new format
        String ret = "";
        boolean isCreator = false;
        for (int i = 0; i < CreatorProperties.length; i++) {
            String val = props.getProperty(CreatorProperties[i]);
            if (val != null) {
                isCreator = true;

                putProjectProperty(project, CreatorProperties[i], val, "");

                if (propName.equals(CreatorProperties[i])) {
                    ret = val;
                }
            }
        }

        // Store version into the new format
        String version = props.getProperty("creator"); // NOI18N
        if (isCreator && version == null) {
            version = "2.0"; // NOI18N
        }
        if (version != null) {
            setProjectVersion(project, version);
            if (propName.equals(JsfProjectConstants.PROP_JSF_PROJECT_VERSION)) { // NOI18N
                ret = version;
            }
        }

        return ret;
    }
    
    public static void createProjectProperty(Project project, String propName, String value) {
        putProjectProperty(project, propName, value, ""); // NOI18N
    }
    
    public static void putProjectProperty(Project project, String propName, String value) {
        putProjectProperty(project, propName, value, getProjectProperty(project, propName));
    }
    
    private static void putProjectProperty(Project project, String propName, String value, String oldval) {
        if (isWebProject(project)) {
            AuxiliaryConfiguration ac = (AuxiliaryConfiguration)project.getLookup().lookup(AuxiliaryConfiguration.class);
            if (ac == null) {
                return;
            }
            
            Element auxElement = ac.getConfigurationFragment(RAVE_AUX_NAME, RAVE_AUX_NAMESPACE, true);
            if (auxElement == null) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                try {
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document xmlDoc = builder.newDocument();
                    auxElement = xmlDoc.createElementNS(RAVE_AUX_NAMESPACE, RAVE_AUX_NAME);
                } catch (ParserConfigurationException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    return;
                }
            }
            auxElement.setAttribute(propName, value);
            ac.putConfigurationFragment(auxElement, true);
        }
        
        PropertyChangeEvent event = new PropertyChangeEvent(project, propName, oldval, value);
        PropertyChangeListener[] listeners;
        synchronized (propertyListeners) {
            ArrayList projectListeners = (ArrayList) propertyListeners.get(project);
            if (projectListeners == null) {
                return;
            }
            listeners = (PropertyChangeListener[])projectListeners.toArray(new PropertyChangeListener[propertyListeners.size()]);
        }
        for (int i = 0; i < listeners.length; i++) {
            PropertyChangeListener listener = listeners[i];
            if (listener != null) {
                listener.propertyChange(event);
            }
        }
    }
    
    public static void addProjectPropertyListener(Project project, PropertyChangeListener listener) {
        if (isWebProject(project)) {
            synchronized (propertyListeners) {
                ArrayList projectListeners = (ArrayList) propertyListeners.get(project);
                if (projectListeners == null) {
                    projectListeners = new ArrayList();
                    propertyListeners.put(project, projectListeners);
                }
                projectListeners.add(listener);
            }
        }
    }
    
    public static void removeProjectPropertyListener(Project project, PropertyChangeListener listener) {
        if (isWebProject(project)) {
            synchronized (propertyListeners) {
                ArrayList projectListeners = (ArrayList) propertyListeners.get(project);
                if (projectListeners != null) {
                    projectListeners.remove(listener);
                }
            }
        }
    }
    
    /** Check for start page
     * @param webPage JSP file
     */
    public static boolean isStartPage(FileObject webPage) {
        Project project = FileOwnerQuery.getOwner(webPage);
        if (project == null)
            return false;

        FileObject webFolder = getDocumentRoot(project);
        if (webFolder == null)
            return false;

        String startPagePath = getProjectProperty(project, JsfProjectConstants.PROP_START_PAGE);
        if (startPagePath == null || startPagePath.length() == 0)
            return false;

        FileObject actualStartPage = webFolder.getFileObject(startPagePath);
        return (actualStartPage != null && actualStartPage.equals(webPage));
    }
    
    /** Sets the start page for the application
     * @param startPage the path to the JSP or HTML file relative to the document root.
     * @return If successful, returns the path of the new start page relative to the document root, null if unsuccessful.
     */
    public static String setStartPage(FileObject webPage) {
        Project project = FileOwnerQuery.getOwner(webPage);
        if (project == null)
            return null;

        FileObject webFolder = getDocumentRoot(project);
        if (webFolder == null)
            return null;

        String newStartPage = FileUtil.getRelativePath(webFolder, webPage);
        return setStartPage(project, newStartPage);
    }

    /**
     * Sets the start page for the application
     * We need to the ability to specify the actual value for the new start page, since refactoring does not guarantee
     * the order in which refactoring elements are processed.  Since the rename of the file object and the setting of the
     * start page are separate refactoring elements, the setting of the start page can occur prior to the rename of the
     * file object.
     * We use the webPage file object to identify the appro
     * @param project the project to set the start page on
     * @param newStartPage the web folder relative path to the new start page
     * @return If successful, returns the path of the new start page relative to the document root, null if unsuccessful.
     */
    public static String setStartPage(Project project, String newStartPage) {
        if (project == null)
            return null;

        String oldStartPage = getProjectProperty(project, JsfProjectConstants.PROP_START_PAGE);
        putProjectProperty(project, JsfProjectConstants.PROP_START_PAGE, newStartPage);

        // Adjust the path to the startpage based on JSF parameters
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            try {
                FileObject dd = wm.getDeploymentDescriptor();
                WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
                if (ddRoot != null) {
                    String facesMapping = getFacesURLPattern(ddRoot);
                    if (oldStartPage != null) {
                        removeWelcomeFile(ddRoot, facesMapping, oldStartPage);
                    }
                    setWelcomeFile(wm, ddRoot, facesMapping, newStartPage);
                    ddRoot.write(dd);
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }

        return newStartPage;
    }
    
    /**
     * Get the Faces Servlet URL pattern.
     * @param ddRoot the Web Application
     * @return If successful, returns the URL pattern, null if unsuccessful.
     */
    public static String getFacesURLPattern(WebApp ddRoot) {
        Servlet[] servlets = ddRoot.getServlet();
        ServletMapping[] mapping = ddRoot.getServletMapping();
        if (servlets == null || mapping == null) {
            return null;
        }

        for (int i = 0; i < servlets.length; i++) {
            if (servlets[i].getServletClass().equals("javax.faces.webapp.FacesServlet")) { // NOI18N
                String servletName = servlets[i].getServletName();
                for (int j = 0; j < mapping.length; j++) {
                    if (servletName.equals(mapping[j].getServletName())) {
                        return mapping[j].getUrlPattern();
                    }
                }
            }
        }

        return null;
    }

    private static String FORWARD_JSF = "forwardToJSF.jsp"; //NOI18N
    private static String RESOURCE_FOLDER = "org/netbeans/modules/web/jsf/resources/"; //NOI18N

    public static void removeWelcomeFile(WebApp ddRoot, String facesMapping, String pageName) {
        // remove welcome file
        WelcomeFileList welcomeFiles = ddRoot.getSingleWelcomeFileList();
        if (welcomeFiles == null) {
            return;
        }

        String welcomePath;
        if (facesMapping.charAt(0) == '/') {
            // if the mapping start with '/' (like /faces/*), then the welcame file can be the mapping
            welcomeFiles.removeWelcomeFile(ConfigurationUtils.translateURI(facesMapping, pageName));
        } else {
            // if the mapping doesn't strat '/' (like *.jsf), then the welcome file has to be
            // a helper file, which will foward the request to the right url
            welcomeFiles.removeWelcomeFile(FORWARD_JSF);
        }
    }

    public static void setWelcomeFile(WebModule webModule, WebApp ddRoot, String facesMapping, String pageName) {
        // add welcome file
        WelcomeFileList welcomeFiles = ddRoot.getSingleWelcomeFileList();
        if (welcomeFiles == null) {
            try {
                welcomeFiles = (WelcomeFileList) ddRoot.createBean("WelcomeFileList"); //NOI18N
                ddRoot.setWelcomeFileList(welcomeFiles);
            } catch (ClassNotFoundException cnfe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, cnfe);
            }
        }

        String welcomePath;
        if (facesMapping.charAt(0) == '/') {
            // if the mapping start with '/' (like /faces/*), then the welcame file can be the mapping
            welcomePath = ConfigurationUtils.translateURI(facesMapping, pageName);
        } else {
            // if the mapping doesn't strat '/' (like *.jsf), then the welcome file has to be
            // a helper file, which will foward the request to the right url
            welcomePath = FORWARD_JSF;
            //copy forwardToJSF.jsp
            if (facesMapping.charAt(0) != '/') {
                try {
                    String content = readResource(Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_FOLDER + FORWARD_JSF), "UTF-8"); //NOI18N
                    content = content.replace("__FORWARD__", ConfigurationUtils.translateURI(facesMapping, pageName));
                    FileObject target = FileUtil.createData(webModule.getDocumentBase(), FORWARD_JSF);//NOI18N
                    createFile(target, content, "UTF-8");  //NOI18N
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }

        if (welcomeFiles.sizeWelcomeFile() == 0) {
            welcomeFiles.addWelcomeFile(welcomePath);
        } else {
            ArrayList<String> list = new ArrayList(welcomeFiles.sizeWelcomeFile()+1);
            list.add(welcomePath);
            for (String file: welcomeFiles.getWelcomeFile()) {
                if (!welcomePath.equals(file)) {
                    list.add(file);
                }
            }
            welcomeFiles.setWelcomeFile(list.toArray(new String[0]));
        }
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

    public static String setDataSourceReference(Project project, String resourceName) {
        String[] propertyNames = new String[] {
            "ResRefName", // NOI18N
            "Description", // NOI18N
            "ResType", // NOI18N
            "ResAuth" }; // NOI18N
        Object[] propertyValues = new Object[] {
            resourceName,
            "Visual Web generated DataSource Reference", // NOI18N
            "javax.sql.DataSource", // NOI18N
            "Container" }; // NOI18N

        return addWebAppBean(project, "ResourceRef", propertyNames, propertyValues, "ResRefName"); // NOI18N
    }

    public static String setEjbReference(Project project, RequestedEjbResource ejbResource) {
        String[] propertyNames = new String[] {
            "EjbRefName", // NOI18N
            "EjbRefType", // NOI18N
            "Home", // NOI18N
            "Remote" }; // NOI18N
        Object[] propertyValues = new Object[] {
            ejbResource.getEjbRefName(),
            ejbResource.getEjbRefType(),
            ejbResource.getHome(),
            ejbResource.getRemote() };

        return addWebAppBean(project, "EjbRef", propertyNames, propertyValues, "EjbRefName"); // NOI18N
    }

    public static String addWebAppBean(Project project, String beanName, String[] propertyNames, Object[] propertyValues, String keyProperty) {
        if (project == null)
            return null;

        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        String name = (String) propertyValues[0];
        if (wm != null) {
            try {
                FileObject dd = wm.getDeploymentDescriptor();
                WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
                if (ddRoot != null) {
                    if ("ResourceRef".equals(beanName)) {
                        ResourceRef[] rscRefs = ddRoot.getResourceRef();
                        if (rscRefs != null) {
                            for (int i = 0; i < rscRefs.length; i++) {
                                if (name.equals(rscRefs[i].getResRefName())) {
                                    return null;
                                }
                            }
                        }
                    } else if ("EjbRef".equals(beanName)) {
                        EjbRef[] ejbRefs = ddRoot.getEjbRef();
                        if (ejbRefs != null) {
                            for (int i = 0; i < ejbRefs.length; i++) {
                                if (name.equals(ejbRefs[i].getEjbRefName())) {
                                    return null;
                                }
                            }
                        }
                    }
                    ddRoot.addBean(beanName, propertyNames, propertyValues, keyProperty); // NOI18N
                    ddRoot.write(dd);
                }
            } catch (ClassNotFoundException e) {
                // This should really not happen
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (NameAlreadyUsedException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }

        return name;
    }

    /**
     * Convenience method to obtain the project's source encoding
     * @param project the Project object
     * @return string representation of the project's source encoding
     */
    public static String getSourceEncoding(Project project) {
        // TODO
        return "UTF-8";
    }
    
    /**
     * @param project
     * @return
     */
    public static String getDefaultEncoding(Project project) {
        // PROJECTTODO: implement
        return "UTF-8";
    }
    
    /**
     * @param project
     * @return
     */
    public static String getDefaultLocale(Project project) {
        // PROJECTTODO: implement
        return "en";
    }
    
    public static final String J2EE_1_3 = J2eeModule.J2EE_13;
    public static final String J2EE_1_4 = J2eeModule.J2EE_14;
    public static final String JAVA_EE_5 = J2eeModule.JAVA_EE_5;

    private static final Map<Project,Boolean> JavaEE5Project = new WeakHashMap();
    
    /** J2EE platform version - one of the constants {@link #J2EE_13_LEVEL}, {@link #J2EE_14_LEVEL}.
     * @param project
     * @return J2EE platform version
     */
    public static String getJ2eePlatformVersion(Project project) {
        if (project == null) {
            return "";
        }

        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm == null) {
            return "";
        }

        return wm.getJ2eePlatformVersion();
    }
    
    public static boolean isJavaEE5Project(Project project) {
        if (project == null) {
            return false;
        }

        Boolean ret = (Boolean) JavaEE5Project.get(project);
        if (ret == null) {
            ret = new Boolean(J2eeModule.JAVA_EE_5.equals(getJ2eePlatformVersion(project)));
            JavaEE5Project.put(project, ret);
        }

        return ret.booleanValue();
    }

    /**
     * Convenience method to obtain the document root folder.
     * @param project the Project object
     * @return the FileObject of the document root folder
     */
    public static FileObject getDocumentRoot(Project project) {
        if (project == null) {
            return null;
        }

        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm == null) {
            return null;
        }

        return wm.getDocumentBase();
    }
    
    /**
     * Convenience method to obtain the WEB-INF folder.
     * @param project the Project object
     * @return the FileObject of the WEB-INF folder
     */
    public static FileObject getWebInf(Project project) {
        if (project == null) {
            return null;
        }

        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm == null) {
            return null;
        }

        return wm.getWebInf();
    }
    
    /**
     * Convenience method to obtain the source root folder.
     * @param project the Project object
     * @return the FileObject of the source root folder
     */
    public static FileObject getSourceRoot(Project project) {
        if (project == null) {
            return null;
        }

        // Search the ${src.dir} Source Package Folder first, use the first source group if failed.
        Sources src = ProjectUtils.getSources(project);
        SourceGroup[] grp = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i = 0; i < grp.length; i++) {
            if ("${src.dir}".equals(grp[i].getName())) { // NOI18N
                return grp[i].getRootFolder();
            }
        }
        if (grp.length != 0) {
            return grp[0].getRootFolder();
        }

        return null;
    }
    
    /**
     * Returns a directory under the project root where resource files can be added
     * @param project Target project
     * @return FileObject of the resources directory. If it does not exist, it will be created.
     * @throws IOException if the directory cannot be created
     */
    public static FileObject getResourcesDirectory(Project project) throws IOException {
        FileObject docRoot = getDocumentRoot(project);
        if (docRoot == null) {
            return null;
        }

        FileObject resourceRoot = docRoot.getFileObject("resources"); // NOI18N
        if (resourceRoot == null) {
            resourceRoot = FileUtil.createFolder(docRoot, "resources"); // NOI18N
        }
        return resourceRoot;
    }
    
    /**
     * Returns a directory under the project root where library jar files can be added as project-private resources
     * @param project Target project
     * @return FileObject of the library directory. If it does not exist, it will be created.
     * @throws IOException if the directory cannot be created
     */
    public static FileObject getProjectLibraryDirectory(Project project) throws IOException {
        FileObject projRoot = project.getProjectDirectory();
        FileObject libRoot = projRoot.getFileObject(JsfProjectConstants.PATH_LIBRARIES);
        if (libRoot == null) {
            libRoot = FileUtil.createFolder(projRoot, JsfProjectConstants.PATH_LIBRARIES);
        }
        return libRoot;
    }
    
    /**
     * Convenience method to obtain the project's navigation file
     * @param project the Project object
     * @return the FileObject of the navigtion file
     */
    public static FileObject getNavigationFile(Project project) {
        FileObject webInf = getWebInf(project);
        if (webInf == null) {
            return null;
        }

        return webInf.getFileObject("navigation.xml");  // NOI18N
    }
    
    /**
     * Convenience method to obtain the root folder for page beans
     * @param project the Project object
     * @return the FileObject of the page bean root folder
     */
    public static FileObject getPageBeanRoot(Project project) {
        if (project == null)
            return null;
        if (!isWebProject(project))
            return null;
        FileObject srcRoot = getSourceRoot(project);
        if (srcRoot == null)
            return null;
        
        String pageBeanPackage = getProjectProperty(project, JsfProjectConstants.PROP_JSF_PAGEBEAN_PACKAGE);  // NOI18N
        if (pageBeanPackage == null) {
            // Dumb fallback attempt to locate the bean root - sniff the package root for anything familiar
            FileObject[] pkgs = srcRoot.getChildren();
            // Assume Application/Session/Request beans are in the root bean package
            for (int i = 0; i < pkgs.length; i++) {
                if (pkgs[i].isFolder()) {
                    FileObject[] files = pkgs[i].getChildren();
                    for (int j = 0; j < files.length; j++) {
                        if (files[j].getName().startsWith("ApplicationBean")) { // NOI18N
                            putProjectProperty(project, JsfProjectConstants.PROP_JSF_PAGEBEAN_PACKAGE, pkgs[i].getName());
                            return pkgs[i];
                        }
                    }
                }
            }
            // No attribute defined and couldn't find any packages containing managed beans
            return null;
        }
        
        pageBeanPackage = pageBeanPackage.replace('.', '/');
        FileObject pageBeanFolder = srcRoot.getFileObject(pageBeanPackage);
        if (pageBeanFolder != null) {
            return pageBeanFolder;
        }
        
        try {
            return FileUtil.createFolder(srcRoot, pageBeanPackage);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return null;
        }
    }
    
    /** Gets corresponding java file object for specified jsp file object if exists.
     * @return corresponding java file object or <code>null</code> */
    public static FileObject getJavaForJsp(FileObject jspFileObject) {
        if(jspFileObject == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner(jspFileObject);
        if (project == null)
            return null;
        String name = getBasePathForJsp(jspFileObject);
        if (name == null) {
            return null;
        }
        FileObject root = getPageBeanRoot(project);
        if(root == null) {
            return null;
        }
        name += ".java"; // NOI18N
        FileObject javaFile = root.getFileObject(name);
        return javaFile;
    }
    
    // !EAT TODO We will be moving this code back into InSync, but at moment we do not want to have
    // to add a dependency on InSync here, would create a circularity :(
    public static String getBasePathForJava(FileObject javaFile) {
        if(javaFile == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner(javaFile);
        if(project == null) {
            return null;
        }
        FileObject javaRoot = getPageBeanRoot(project);
        if(javaRoot == null) {
            return null;
        }
        String javaRootPath = javaRoot.getPath();
        String basePath = javaFile.getParent().getPath();
        if(basePath.startsWith(javaRootPath)) {
            basePath = basePath.substring(javaRootPath.length());
        } else {
            javaRoot = getSourceRoot(project);
            javaRootPath = javaRoot.getPath();
            if(basePath.startsWith(javaRootPath)) {
                basePath = basePath.substring(javaRootPath.length());
            } else {
                String projectPath = project.getProjectDirectory().getPath();
                if (basePath.startsWith(projectPath))
                    basePath = basePath.substring(projectPath.length());
                else {
                    // !EAT TODO
                    // This case should really be handled, file is outside project tree, what to do ?
                    // We really need to fix this up such that its a function of the project path, but
                    // the "root" of the project element that contains this source file :(
                }
            }
        }
        if(basePath.length() > 0) {
            basePath += "/";
        }
        basePath += javaFile.getName();
        return basePath;
    }
    
    /** Gets corresponding jsp file object for specified java file object if exists.
     * @return corresponding jsp file object or <code>null</code> */
    public static FileObject getJspForJava(FileObject javaFileObject) {
        if(javaFileObject == null || !javaFileObject.getExt().equals("java")) { // NOI18N
            return null;
        }
        Project project = FileOwnerQuery.getOwner(javaFileObject);
        if(project == null) {
            return null;
        }
        String path = getBasePathForJava(javaFileObject);
        if(path == null) {
            return null;
        }
        String jspPath = path + ".jsp"; // NOI18N
        FileObject root = getDocumentRoot(project);
        if(root == null) {
            return null;
        }
        FileObject jspFile = root.getFileObject(jspPath);
        if(jspFile == null) {
            jspPath = path + ".jspf"; // NOI18N
            jspFile = root.getFileObject(jspPath);
        }
        return jspFile;
    }
    
    // !EAT TODO We will be moving this code back into InSync, but at moment we do not want to have
    // to add a dependency on InSync here, would create a circularity :(
    public static String getBasePathForJsp(FileObject jspFile) {
        if (jspFile == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner(jspFile);
        if (project == null) {
            return null;
        }
        FileObject webRoot = getDocumentRoot(project);
        if(webRoot == null) {
            return null;
        }
        String webRootPath = webRoot.getPath();
        String jspPath = jspFile.getParent().getPath();
        if (jspPath.startsWith(webRootPath))
            jspPath = jspPath.substring(webRootPath.length());
        else {
            String projectPath = project.getProjectDirectory().getPath();
            if (jspPath.startsWith(projectPath))
                jspPath = jspPath.substring(projectPath.length());
            else {
                // !EAT TODO
                // This case should really be handled, file is outside project tree, what to do ?
                // We really need to fix this up such that its a function of the project path, but
                // the "root" of the project element that contains this source file :(
            }
        }
        if(jspPath.length() > 0) {
            jspPath += "/";  // NOI18N
        }
        jspPath += jspFile.getName();
        return jspPath;
    }
    
    // folders
    /** Gets corresponding java folder for specified jsp file object if exists.
     * @return corresponding java file object or <code>null</code> */
    public static FileObject getJavaFolderForJsp(FileObject jspFileObject) {
        if(jspFileObject == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner(jspFileObject);
        if (project == null)
            return null;
        String name = getFolderBasePathForJsp(jspFileObject);
        if (name == null) {
            return null;
        }
        FileObject root = getPageBeanRoot(project);
        FileObject javaFolder = root.getFileObject(name);
        if(javaFolder == null) {
            try {
                javaFolder = FileUtil.createFolder(root, name);
            } catch(IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            }
        }
        return javaFolder;
    }
    
    private static String getFolderBasePathForJsp(FileObject jspFile) {
        if (jspFile == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner(jspFile);
        if (project == null) {
            return null;
        }
        FileObject webRoot = getDocumentRoot(project);
        if(webRoot == null) {
            return null;
        }
        String webRootPath = webRoot.getPath();
        String jspPath = jspFile.getParent().getPath();
        if(jspPath.startsWith(webRootPath)) {
            jspPath = jspPath.substring(webRootPath.length());
        } else {
            return null;
//            jspPath = ""; // NOI18N
        }
        return jspPath;
    }
    
    /** Gets corresponding jsp folder for specified java file object if exists.
     * @return corresponding jsp file object or <code>null</code> */
    public static FileObject getJspFolderForJava(FileObject javaFileObject) {
        if(javaFileObject == null || !javaFileObject.getExt().equals("java")) { // NOI18N
            return null;
        }
        Project project = FileOwnerQuery.getOwner(javaFileObject);
        if(project == null) {
            return null;
        }
        String path = getFolderBasePathForJava(javaFileObject);
        if(path == null) {
            return null;
        }
        FileObject root = getDocumentRoot(project);
        FileObject jspFolder = root.getFileObject(path);
        if(jspFolder == null) {
            try {
                jspFolder = FileUtil.createFolder(root, path);
            } catch(IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            }
        }
        return jspFolder;
    }
    
    private static String getFolderBasePathForJava(FileObject javaFile) {
        if(javaFile == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner(javaFile);
        if(project == null) {
            return null;
        }
        FileObject javaRoot = getPageBeanRoot(project);
        if(javaRoot == null) {
            return null;
        }
        String javaRootPath = javaRoot.getPath();
        String basePath = javaFile.getParent().getPath();
        if(basePath.startsWith(javaRootPath)) {
            basePath = basePath.substring(javaRootPath.length());
        } else {
            return null;
        }
        return basePath;
    }
    // folders
    
    public static void importFile(Project project, File file) {
        ImportFileAction.importFile(project, file);
    }
    
    // Other potential convenience methods
    
    /** Add a new resource to the project
     * @param webForm the webform object that will be referencing this resource.
     *        Used to determine the relative path name to the resource
     * @param resourceURL resource to be added
     * @param copy if true, resource will be copied to the project
     * @return a source path for the resource in the project. If copy is true,
     *         this will be a relative path name to the resource file. If copy
     *         is false, addResource will return resourceURL->toString()
     * @throws IOException if an error occurs when accessing the URL or copying
     *         the resource
     */
    public static String addResource(FileObject webForm, URL resourceURL, boolean copy) throws IOException {
        String linkRef = resourceURL.toString();
        String mimeDir = "resources";  // NOI18N
        if ( !copy )  // maybe later show some representation of this remote resource in the PM
            return linkRef;
        
        String fileName = new File(resourceURL.getFile()).getName();
        
        FileObject formFolderFO = webForm.getParent();
        DataObject formFolderDO = DataObject.find(formFolderFO);
        FileObject resFolderFO = formFolderFO;
        if (mimeDir != null) {
            resFolderFO = formFolderFO.getFileObject(mimeDir);
            if (resFolderFO == null)
                resFolderFO = FileUtil.createFolder(formFolderFO, mimeDir);
        }
        
        if (mimeDir != null) {
            linkRef = mimeDir + "/" + fileName;  // NOI18N
        } else {
            linkRef = fileName;
        }
        
        FileObject resourceFO = null;
        resourceFO = resFolderFO.getFileObject(fileName);
        if (resourceFO != null) {
            File targetFile = FileUtil.toFile(resourceFO);
            // 5018183 check if source & target file are the same
            try {
                URL targetURL = targetFile.toURI().toURL();
                if (targetURL.equals(resourceURL)) {
                    return linkRef;
                }
            } catch (MalformedURLException e) {
                throw e;
            }
            
            AddResourceOverwriteDialog d = new AddResourceOverwriteDialog(targetFile);
            d.showDialog();
            File newTarget = d.getFile();
            if (newTarget == null)
                return null;
            if (newTarget.exists())
                return linkRef;
            fileName = newTarget.getName();
            if (mimeDir != null) {
                linkRef = mimeDir + "/" + fileName;  // NOI18N
            } else {
                linkRef = fileName;
            }
        }
        
        InputStream is = null;
        OutputStream os = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        FileLock lock = null;
        try {
            is = resourceURL.openStream();
            // We already checked whether the file exists above
            resourceFO = resFolderFO.createData(fileName);
            lock = resourceFO.lock();
            os = resourceFO.getOutputStream(lock);
            in = new BufferedInputStream(is);
            out = new BufferedOutputStream(os);
            int c;
            while ((c = in.read()) != -1)
                out.write(c);
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (lock != null)
                    lock.releaseLock();
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (Exception e) {
            }
        }
        
        if (resourceFO != null) {
            selectResourceInWindow(resourceFO);
        }
        
        return linkRef;
    }
    
    public static void selectResourceInWindow(final FileObject resourceFO) {
        // Part of #6346374 Window API may be called only from Event dispatching thread.
        if(EventQueue.isDispatchThread()) {
            doSelectResourceInWindow(resourceFO);
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    doSelectResourceInWindow(resourceFO);
                }
            });
        }
    }
    
    private static void doSelectResourceInWindow(FileObject resourceFO) {
        // XXX wait for NetBeans API
        // ProjectTab pt = ProjectTab.findDefault(ProjectTab.ID_LOGICAL);
        // pt.selectNodeAsync(resourceFO);
    }
    
    /**
     * Obtain the portlet support helper object from the project
     * @param project the Project object
     * @return the portlet support object or null if the project is not
     * capable of supporting portlets
     */
    public static JsfPortletSupport getPortletSupport(Project project) {
        FileObject webInf = getWebInf(project);
        if (webInf == null) {
            return null;
        }

        FileObject fo = webInf.getFileObject("portlet.xml");  // NOI18N
        if (fo == null)
            return null;
        else {
            // TODO: This really should be a lookup on web/project.  Currently the module dependencies
            // are incorrect.  The web/project should provide the interface to JsfPortletSupport and hide
            // the implementation.  The web/project should contain the JsfPortletSupport implementation or
            // there should be an API/SPI arrangement set up to elliminate the web/project to project/jsfportlet
            // module dependecy.
            //
            // Current hack:  Because this method is likely to be called numerous
            // times by time-critical modules like "designer", we MUST place the
            // implementation either in the project/jsfprojectapi or in project/jsfportlet
            // and create a dependencey between project/jsfprojectapi and project/jsfportlet.
            // Since the portlet support implementation also needs module portletcontainer interaction,
            // we will put the implementation in project/jsfportlet to elliminate the need for
            // a dependency between project/jsfprojectapi and portletcontainer.
            // In order to elliminate the module dependency between
            // project/jsfprojectapi and project/jsfportlet, we would need to use the
            // layer.xml files and the built-in lookup facility to find the interface.  This would
            // be too time-consuming to do each time this static method was called.  We
            // can't put a static reference to the implementation since the the user
            // can have multiple projects open at once.  The portlet suppport for the first project
            // opened would always be the portlet support given out to all projects.
            // - David Botterill 5/13/2005
            return new JsfPortletSupportImpl(project);
        }
        
    }
    
    public static void updateXml(FileObject prjLoc, String path, String nameSpace, String key, String value) throws IOException {
        try {
            FileObject projXml = prjLoc.getFileObject(path);
            if (projXml == null) {
                return;
            }

            Document doc = XMLUtil.parse(new InputSource(FileUtil.toFile(projXml).toURI().toString()), false, true, null,
                    new EntityResolver() {
                public InputSource resolveEntity(String pubid, String sysid) throws SAXException, IOException {
                    return new InputSource(new ByteArrayInputStream(new byte[0]));
                }
            });
            NodeList nlist;
            if (nameSpace != null) {
                nlist = doc.getElementsByTagNameNS(nameSpace, key);
            } else {
                nlist = doc.getElementsByTagName(key);
            }
            if (nlist != null) {
                for (int i=0; i < nlist.getLength(); i++) {
                    Node n = nlist.item(i);
                    if (n.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    Element e = (Element)n;
                    
                    replaceText(e, value);
                }
                saveXml(doc, prjLoc, path);
            }
        } catch (Exception e) {
            throw new IOException(e.toString());
        }
    }
    
    /**
     * Extract nested text from an element.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent element
     * @return the nested text, or null if none was found
     */
    private static void replaceText(Element parent, String name) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text)l.item(i);
                text.setNodeValue(name);
                return;
            }
        }
    }
    
    /**
     * Save an XML config file to a named path.
     * If the file does not yet exist, it is created.
     */
    private static void saveXml(Document doc, FileObject dir, String path) throws IOException {
        FileObject xml = FileUtil.createData(dir, path);
        FileLock lock = xml.lock();
        try {
            OutputStream os = xml.getOutputStream(lock);
            try {
                XMLUtil.write(doc, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    /**
     * Add an array of library references to a project, qualified for both the design-time classpath or deployed with the application
     * @param project Project to which the library is to be added
     * @param library Library object from the LibraryManager registry
     * @return Returns true if the library reference was successfully added
     * @throws an IOException if there was a problem adding the reference
     */
    public static boolean addLibraryReferences(Project project, Library[] libraries) throws IOException {
        try {
            return ProjectClassPathModifier.addLibraries(libraries, getSourceRoot(project), ClassPath.COMPILE);
        } catch (IOException e) {
            // Should continue here, many exceptions happened in NetBeans codes are not fatal.
        }

        return false;
    }

    /**
     * Add an array of library references to a project, qualified by the type parameter.
     * @param project Project to which the library is to be added
     * @param library Library object from the LibraryManager registry
     * @param type Determines whether the library is to be added to the design-time classpath or deployed
     * with the application
     * @return Returns true if the library reference was successfully added
     * @throws an IOException if there was a problem adding the reference
     */
    public static boolean addLibraryReferences(Project project, Library[] libraries, String type) throws IOException {
        WebProjectLibrariesModifier wplm = (WebProjectLibrariesModifier) project.getLookup().lookup(WebProjectLibrariesModifier.class);
        if (wplm == null) {
            // Something is wrong, shouldn't be here.
            return addLibraryReferences(project, libraries);
        }

        if (type == ClassPath.COMPILE) {
            return wplm.addCompileLibraries(libraries);
        } else if (type == ClassPath.EXECUTE) {
            return wplm.addPackageLibraries(libraries, PATH_IN_WAR_LIB);
        }

        return false;
    }
    
    /**
     * Remove an array of library references from a project, qualified for both the design-time classpath or deployed with the application
     * @param project Project from which the library references are to be removed
     * @param library Array of Library objects from the LibraryManager registry
     * @return Returns true if at least one of the library references were successfully removed
     * @throws an IOException if there was a problem removing the reference
     */
    public static boolean removeLibraryReferences(Project project, Library[] libraries) throws IOException {
        try {
            return ProjectClassPathModifier.removeLibraries(libraries, getSourceRoot(project), ClassPath.COMPILE);
        } catch (IOException e) {
            // Should continue here, many exceptions happened in NetBeans codes are not fatal.
        }
        
        return false;
    }
    
    /**
     * Remove an array of library references from a project, qualified by the type parameter.
     * @param project Project from which the library references are to be removed
     * @param library Array of Library objects from the LibraryManager registry
     * @param type Determines whether the library is to be removed from the design-time classpath or deployed
     * with the application
     * @return Returns true if at least one of the library references were successfully removed
     * @throws an IOException if there was a problem removing the reference
     */
    public static boolean removeLibraryReferences(Project project, Library[] libraries, String type) throws IOException {
        WebProjectLibrariesModifier wplm = (WebProjectLibrariesModifier) project.getLookup().lookup(WebProjectLibrariesModifier.class);
        if (wplm == null) {
            // Something is wrong, shouldn't be here.
            return removeLibraryReferences(project, libraries);
        }

        if (type == ClassPath.COMPILE) {
            return wplm.removeCompileLibraries(libraries);
        } else if (type == ClassPath.EXECUTE) {
            return wplm.removePackageLibraries(libraries, PATH_IN_WAR_LIB);
        }

        return false;
    }
    
    /**
     * Check if a project has a library reference to the named library qualified for both the design-time classpath or deployed with the application
     * @param project Target project
     * @param library Library object
     * @return Returns true if the library is already referenced by the project, false otherwise
     */
    public static boolean hasLibraryReference(Project project, Library library) {
        return hasLibraryReference(project, library, ClassPath.COMPILE);
    }

    /**
     * Check if a project has a library reference to the named library qualified by the type parameter.
     * @param project Target project
     * @param library Library object
     * @param type Determines whether the library is to be referenced from the design-time classpath or deploy
     * time classpath
     * @return Returns true if the library is already referenced by the project, false otherwise
     */
    public static boolean hasLibraryReference(Project project, Library library, String type) {
        List lst = library.getContent("classpath");
        if (lst.isEmpty()) {
            return false;
        }

        URL url = (URL) lst.get(0);
        FileObject obj = URLMapper.findFileObject(url);
        if (obj == null) {
            return false;
        }

        // XXX NetBeans API not finished yet
        type = ClassPath.COMPILE;
        ClassPath cp = ClassPath.getClassPath(getSourceRoot(project), type);

        return cp.contains(obj);
    }
    
    /**
     * Add an root reference to a project qualified for both the design-time classpath or deployed with the application
     * @param project Project to which the root is to be added
     * @param rootFile file object of the root
     * @return Returns true if the root was successfully added
     * @throws an IOException if there was a problem adding the reference
     */
    public static boolean addRootReferences(Project project, URL[] rootFiles) throws IOException {
        try {
            return ProjectClassPathModifier.addRoots(rootFiles, getSourceRoot(project), ClassPath.COMPILE);
        } catch (IOException e) {
            // Should continue here, many exceptions happened in NetBeans codes are not fatal.
        }

        return false;
    }

    /**
     * Add an root reference to a project qualified by the type parameter.
     * @param project Project to which the root is to be added
     * @param rootFile file object of the root
     * @param type Determines whether the root is to be added to the design-time classpath or deployed
     * with the application
     * @return Returns true if the root was successfully added
     * @throws an IOException if there was a problem adding the reference
     */
    public static boolean addRootReferences(Project project, URL[] rootFiles, String type) throws IOException {
        WebProjectLibrariesModifier wplm = (WebProjectLibrariesModifier) project.getLookup().lookup(WebProjectLibrariesModifier.class);
        if (wplm == null) {
            // Something is wrong, shouldn't be here.
            return addRootReferences(project, rootFiles);
        }

        if (type == ClassPath.COMPILE) {
            return wplm.addCompileRoots(rootFiles);
        } else if (type == ClassPath.EXECUTE) {
            return wplm.addPackageRoots(rootFiles, PATH_IN_WAR_LIB);
        }

        return false;
    }

    /**
     * Remove an array of root references from a project qualified for both the design-time classpath or deployed with the application
     * @param project Project from which the root references is to be removed
     * @param rootFile file object of the root
     * @return Returns true if at least one of the roots was successfully removed
     * @throws an IOException if there was a problem removing the references
     */
    public static boolean removeRootReferences(Project project, URL[] rootFiles) throws IOException {
        try {
            return ProjectClassPathModifier.removeRoots(rootFiles, getSourceRoot(project), ClassPath.COMPILE);
        } catch (IOException e) {
            // Should continue here, many exceptions happened in NetBeans codes are not fatal.
        }
        
        return false;
    }
    
    /**
     * Remove an array of root references from a project qualified by the type parameter.
     * @param project Project from which the root references is to be removed
     * @param rootFile file object of the root
     * @param type Determines whether the root is to be removed from the design-time classpath or deploy
     * time classpath
     * @return Returns true if at least one of the roots was successfully removed
     * @throws an IOException if there was a problem removing the references
     */
    public static boolean removeRootReferences(Project project, URL[] rootFiles, String type) throws IOException {
        WebProjectLibrariesModifier wplm = (WebProjectLibrariesModifier) project.getLookup().lookup(WebProjectLibrariesModifier.class);
        if (wplm == null) {
            // Something is wrong, shouldn't be here.
            return removeRootReferences(project, rootFiles);
        }

        if (type == ClassPath.COMPILE) {
            return wplm.removeCompileRoots(rootFiles);
        } else if (type == ClassPath.EXECUTE) {
            return wplm.removePackageRoots(rootFiles, PATH_IN_WAR_LIB);
        }

        return false;
    }
    
    /**
     * Check if a project has an root reference to the named root qualified for both the design-time classpath or deployed with the application
     * @param project Target project
     * @param rootFile file object of the root
     * @return Returns true if the root is already referenced by the project, false otherwise
     */
    public static boolean hasRootReference(Project project, URL rootFile) {
        return hasRootReference(project, rootFile, ClassPath.COMPILE);
    }

    /**
     * Check if a project has an root reference to the named root qualified by the type parameter.
     * @param project Target project
     * @param rootFile file object of the root
     * @param type Determines whether the root is to be referenced from the design-time classpath or deploy
     * time classpath
     * @return Returns true if the root is already referenced by the project, false otherwise
     */
    public static boolean hasRootReference(Project project, URL rootFile, String type) {
        FileObject obj = URLMapper.findFileObject(rootFile);
        if (obj == null) {
            return false;
        }

        // XXX NetBeans API not finished yet
        type = ClassPath.COMPILE;
        ClassPath cp = ClassPath.getClassPath(getSourceRoot(project), type);

        return cp.contains(obj);
    }
    
    public static void addLocalizedRoots(Project project, String[] jarName) throws IOException {
        ArrayList jars = new ArrayList(jarName.length);
        for (int i = 0; i < jarName.length; i++) {
            File f = InstalledFileLocator.getDefault().locate(jarName[i], null, true);
            if (f != null) {
                URL root = FileUtil.getArchiveRoot(FileUtil.toFileObject(f)).getURL();
                if (!hasRootReference(project, root)) {
                    jars.add(root);
                }
            }
        }
        addRootReferences(project, (URL[])jars.toArray(new URL[0]));
    }

    public static void addLocalizedRoots(Project project, String[] jarName, String type) throws IOException {
        ArrayList jars = new ArrayList(jarName.length);
        for (int i = 0; i < jarName.length; i++) {
            File f = InstalledFileLocator.getDefault().locate(jarName[i], null, true);
            if (f != null) {
                URL root = FileUtil.getArchiveRoot(FileUtil.toFileObject(f)).getURL();
                if (!hasRootReference(project, root, type)) {
                    jars.add(root);
                }
            }
        }
        addRootReferences(project, (URL[])jars.toArray(new URL[0]), type);
    }

    public static void updateLocalizedRoots(Project project) {
        try {
            JsfProjectLibrary.updateLocalizedRoots(project);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    public static void addLocalizedTheme(Project project, String themeName) throws IOException {
        URL root = JsfProjectLibrary.getLocalizedThemeRoot(themeName);
        if (root != null) {
            if (!hasRootReference(project, root)) {
                addRootReferences(project, new URL[] { root });
            }
        }
    }
    
    public static void removeLocalizedTheme(Project project, String themeName)  throws IOException {
        URL root = JsfProjectLibrary.getLocalizedThemeRoot(themeName);
        if (root != null) {
            if (hasRootReference(project, root)) {
                removeRootReferences(project, new URL[] { root });
            }
        }
    }
    
    public static Library createJ2SELibrary(
            String name,
            String description,
            String localizingBundle,
            List<URL> classPaths,
            List<URL> sources,
            List<URL> javadocs) throws IOException {
        return J2SELibraryDefinition.create(name, description, localizingBundle, classPaths, sources, javadocs);
    }
    
    public static Library createComponentLibrary(
            String name,
            String description,
            String localizingBundle,
            List<URL> classPaths,
            List<URL> sources,
            List<URL> javadocs,
            List<URL> designtimes) throws IOException {
        return ComponentLibraryDefinition.create(name, description, localizingBundle, classPaths, sources, javadocs, designtimes);
    }
    
    public static void removeLibrary(String name) throws IOException {
        LibraryDefinition.remove(name);
    }
    
    public static boolean isDesigntimeLib(String name) {
        return JsfProjectLibrary.isDesigntimeLib(name);
    }

    /** Reports whether the given name is a valid Java file name.
     * @param name The Java file name to be checked
     * @return true iff the name parameter is a valid Java file name
     * @todo Use the passed in project context to make sure that the
     *   name would not conflict with existing files (e.g. check
     *   the webforms and backing file folders for name conflicts).
     */
    public static boolean isValidJavaFileName(String name) {
        if (name == null) {
            return false;
        }
        int n = name.length();
        if (n == 0) {
            return false;
        }
        
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            return false;
        }
        
        for (int i = 1; i < n; i++) {
            char c = name.charAt(i);
            if (!Character.isJavaIdentifierPart(c)) {
                return false;
            }
        }

        if (!Utilities.isJavaIdentifier(name)) {
            return false;
        }

        return true;
    }
    
    /** Reports whether the given name is a valid Java package name.
     * @param name The Java package name to be checked
     * @return true iff the name parameter is a valid Java package name
     */
    public static boolean isValidJavaPackageName(String pkgName) {
        if (pkgName == null)
            return false;

        String[] pkg = pkgName.split("\\.");
        for (int i = 0; i < pkg.length; i++) {
            if (!Utilities.isJavaIdentifier(pkg[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Derive an identifier suitable for a java package name or context path
     * @param sourceName Original name from which to derive the name
     * @return An identifier suitable for a java package name or context path
     */
    public static String deriveSafeName(String sourceName) {
        StringBuffer dest = new StringBuffer(sourceName.length());
        int sourceLen = sourceName.length();
        if (sourceLen > 0) {
            int pos = 0;
            while (pos < sourceLen) {
                if (Character.isJavaIdentifierStart(sourceName.charAt(pos))) {
                    dest.append(Character.toLowerCase(sourceName.charAt(pos)));
                    pos++;
                    break;
                }
                pos++;
            }

            for (int i = pos; i < sourceLen; i++) {
                if (Character.isJavaIdentifierPart(sourceName.charAt(i)))
                    dest.append(Character.toLowerCase(sourceName.charAt(i)));
            }
        }
        if (dest.length() == 0 || !Utilities.isJavaIdentifier(dest.toString()))
            return "untitled";  // NOI18N
        else
            return dest.toString();
    }

    /**
     *
     * This does a special instantiation of JFileChooser
     * to workaround floppy access bug 5037322.
     * Using privileged code block.
     */
    public static JFileChooser getJFileChooser() {
        return (JFileChooser)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new JFileChooser() ;
            }
        });
    }
    
    public static JFileChooser getJFileChooser(final String currentDirectoryPath) {
        return (JFileChooser)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new JFileChooser(currentDirectoryPath);
            }
        });
    }
    
    public static JFileChooser getJFileChooser(final File currentDirectory) {
        return (JFileChooser)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return new JFileChooser(currentDirectory) ;
            }
        });
    }
}
