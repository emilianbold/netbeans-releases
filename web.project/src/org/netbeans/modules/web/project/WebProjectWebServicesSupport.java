/*
 * WebProjectWebServicesSupport.java
 *
 * Created on November 5, 2004, 2:09 PM
 */

package org.netbeans.modules.web.project;

import org.netbeans.modules.websvc.spi.webservices.WebServicesClientSupportImpl;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportImpl;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.netbeans.api.web.dd.Servlet;
import org.netbeans.api.web.dd.ServletMapping;
import org.netbeans.api.web.dd.WebApp;
import org.netbeans.api.web.dd.DDProvider;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;

/**
 *
 * @author  rico
 * Implementation of WebServicesSupportImpl and WebServicesClientSupportImpl
 */
public class WebProjectWebServicesSupport implements WebServicesSupportImpl, WebServicesClientSupportImpl, WebServicesConstants{
    
   
    private WebProject project;
    private AntProjectHelper helper;
    
    /** Creates a new instance of WebProjectWebServicesSupport */
    public WebProjectWebServicesSupport(WebProject project, AntProjectHelper helper) {
        this.project = project;
        this.helper = helper;
    }
    
    //implementation of WebServicesSupportImpl

    public void addServiceImpl(String serviceName, String serviceEndpointInterface, String servantClassName, FileObject configFile) {
        
        //Add properties to project.properties file
        EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String packageName = getPackageName(configFile);
        ep.put(serviceName + CONFIG_PROP_SUFFIX, packageName +
        (packageName.equals("") ? "" : "/") + configFile.getNameExt()); //NOI18N
        ep.put(serviceName + MAPPING_PROP_SUFFIX, serviceName + MAPPING_FILE_SUFFIX); //NOI18N
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        //Add web-services information in project.xml
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        NodeList nodes = data.getElementsByTagName(WEB_SERVICES); //NOI18N
        Element webservices = null;
        if(nodes.getLength() == 0){
            webservices = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICES); //NOI18N
            data.appendChild(webservices);
        }
        else{
            webservices = (Element)nodes.item(0);
        }
        Element webservice = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE); //NOI18N
        webservices.appendChild(webservice);
        Element webserviceName = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_NAME); //NOI18N
        webservice.appendChild(webserviceName);
        webserviceName.appendChild(doc.createTextNode(serviceName));
        helper.putPrimaryConfigurationData(data, true);
        
        // Update wscompile related properties.  boolean return indicates whether
        // any changes were made.
        updateWsCompileProperties(serviceName);
        
        try {
            ProjectManager.getDefault().saveProject(project);
        }catch(java.io.IOException ioe){
            throw new RuntimeException(ioe.getMessage());
        }
        
        addServiceImplEntry(serviceName, servantClassName);
    }
    
    private WebApp getWebApp() {
        try {
            FileObject deploymentDescriptor = getDeploymentDescriptor();
            if(deploymentDescriptor != null) {
                return DDProvider.getDefault().getDDRoot(deploymentDescriptor);
            }
        } catch (java.io.IOException e) {
            org.openide.ErrorManager.getDefault().log(e.getLocalizedMessage());
        }
        return null;
    }
    
    private void addServiceImplEntry(String serviceName, String servantClassName) {
        //add servlet entry to web.xml
        String servletName = WebServiceServlet_PREFIX + serviceName;
        WebApp webApp = getWebApp();
        if(webApp != null){
            Servlet servlet = null;
            try{
                servlet = (Servlet)webApp.addBean("Servlet", new String[]{"ServletName","ServletClass"},
                new Object[]{servletName,servantClassName}, "ServletName");
                servlet.setLoadOnStartup(new java.math.BigInteger("1"));
                ServletMapping servletMapping = (ServletMapping)
                webApp.addBean("ServletMapping", new String[]{"ServletName","UrlPattern"},
                new Object[]{servletName, "/" + serviceName}, "ServletName");
                webApp.write(getDeploymentDescriptor());
                
                //Hack to save any defaults put in vendor-specific DD
                //Need a better way to save selectively from server plugins(an api that allows
                //server plugins to save server configuration in selective manner)
                org.openide.LifecycleManager.getDefault().saveAll();
            }catch(Exception e){
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
    }
    
    
    /**
     * Get the webservices.xml file object
     * descriptive in interface, e.g., getWebserviceDD
     */
    public FileObject getWebservicesDD() {
        FileObject webInfFo = getWebInf();
        if (webInfFo==null) {
            DialogDisplayer.getDefault().notify(
            new NotifyDescriptor.Message(NbBundle.getMessage(ProjectWebModule.class,"MSG_WebInfCorrupted"),
            NotifyDescriptor.ERROR_MESSAGE));
            return null;
        }
        return getWebInf().getFileObject(WEBSERVICES_DD, "xml");
    }
    
    /**
     *  Returns the directory that contains webservices.xml in the project
     */
    public FileObject getWsDDFolder() {
        return getWebInf();
    }
    
    /**
     * Returns the name of the directory that contains the webservices.xml in
     * the archive
     */
    public String getArchiveDDFolderName() {
        return "WEB-INF";
    }
    
    /**
     * Returns the name of the implementation bean class
     * given the servlet-link name
     */
    public String getImplementationBean(String linkName) {
        WebApp webApp = getWebApp();
        org.netbeans.api.web.dd.Servlet[] servlets = webApp.getServlet();
        for(int i = 0; i < servlets.length; i++) {
            if(servlets[i].getServletName().equals(linkName)) {
                return servlets[i].getServletClass();
            }
        }
        return null;
    }
    
    
    
    public void removeServiceEntry(String serviceName, String linkName) {
        //remove servlet entry in web.xml
        WebApp webApp = getWebApp();
        Servlet[] servlets = webApp.getServlet();
        for(int i = 0; i < servlets.length; i++) {
            Servlet servlet = servlets[i];
            if(servlet.getServletName().equals(linkName)) {
                webApp.removeServlet(servlet);
                break;
            }
        }
        ServletMapping[] mappings = webApp.getServletMapping();
        for(int j = 0; j < mappings.length; j++ ) {
            ServletMapping mapping = mappings[j];
            if(mapping.getServletName().equals(linkName)) {
                webApp.removeServletMapping(mapping);
            }
        }
        try {
            webApp.write(getDeploymentDescriptor());
        }
        catch(java.io.IOException e) {
            NotifyDescriptor ndd =
            new NotifyDescriptor.Message(NbBundle.getMessage(this.getClass(), "MSG_Unable_WRITE_WS_DD"),
            NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(ndd);
        }
        
        boolean needsSave = false;
        
        //Remove entries in the project.properties file
        //FIX-ME:we should move this to websvc
        EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String configProperty = serviceName + CONFIG_PROP_SUFFIX;
        String mappingProperty = serviceName + MAPPING_PROP_SUFFIX;
        if(ep.getProperty(configProperty) != null) {
            ep.remove(configProperty);
            needsSave = true;
        }
        if(ep.getProperty(mappingProperty) != null) {
            ep.remove(mappingProperty);
            needsSave = true;
        }
        
        if(needsSave){
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
        //Remove entry in the project.xml file (we should move this to websvc)
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        NodeList nodes = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,
        WEB_SERVICES); //NOI18N
        Element webservices = null;
        Element wsNameNode = null;
        if(nodes.getLength() == 1){
            webservices = (Element)nodes.item(0);
            NodeList wsNodes = webservices.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,
            WEB_SERVICE); //NOI18N
            for(int j = 0; j < wsNodes.getLength(); j++) {
                Element wsNode = (Element)wsNodes.item(j);
                NodeList wsNameNodes = wsNode.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                WEB_SERVICE_NAME); //NOI18N
                if(wsNameNodes.getLength() == 1) {
                    wsNameNode = (Element)wsNameNodes.item(0);
                    NodeList nl = wsNameNode.getChildNodes();
                    if(nl.getLength() == 1) {
                        Node n = nl.item(0);
                        if(n.getNodeType() == Node.TEXT_NODE) {
                            if(serviceName.equals(n.getNodeValue())) {
                                webservices.removeChild(wsNode);
                                //if there are no more children, remove the web-services node
                                NodeList children = webservices.getChildNodes();
                                if(children.getLength() == 0) {
                                    data.removeChild(webservices);
                                }
                                needsSave = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if(needsSave) {
            helper.putPrimaryConfigurationData(data, true);
            try {
                ProjectManager.getDefault().saveProject(project);
            } catch(IOException ex) {
                String mes = NbBundle.getMessage(this.getClass(), "MSG_ErrorSavingOnWSRemove") + serviceName
                + "'\r\n" + ex.getMessage();
                NotifyDescriptor desc = new NotifyDescriptor.
                Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);			}
        }
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }
    
    public String generateImplementationBean(String wsName, FileObject pkg, Project project)
    throws java.io.IOException {
        return null;
        //FIX-ME: move impl bean generation here
    }
    
    public void addServiceImplLinkEntry(ServiceImplBean serviceImplBean, String wsName) {
        serviceImplBean.setServletLink(WebServiceServlet_PREFIX + wsName);
    }
    
    private String getPackageName(FileObject file){
        FileObject parent = file.getParent();
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String packageName = null;
        for (int i = 0; i < groups.length && packageName == null; i++) {
            packageName = FileUtil.getRelativePath(groups [i].getRootFolder(), parent);
        }
        return packageName + "";
    }
    
    // Implementation of WebServiceClientSupportImpl
   
    
    
    public void addServiceClient(String serviceName, FileObject configFile) {
        // !PW FIXME I have two concerns with this implementation:
        // 1. Since it modifies project.xml, I suspect it should be acquiring
        //    ProjectManager.mutex() for write access.
        // 2. It seems like it ought to be implemented via the AuxiliaryConfiguration
        //    interface.
        boolean needsSave = false;
        
        /** Locate root of web service client node structure in project,xml, creating it
         *  if it's not found.
         */
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        NodeList nodes = data.getElementsByTagName(WEB_SERVICE_CLIENTS);
        Element clientElements = null;
        
        if(nodes.getLength() == 0) {
            // 'needsSave' deliberately left false here because this is a trival change
            // that only should be saved if additional changes are also made below.
            clientElements = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_CLIENTS);
            data.appendChild(clientElements);
        } else {
            clientElements = (Element) nodes.item(0);
        }
        
        /** Make sure this service is not already registered in project.xml
         */
        boolean serviceAlreadyAdded = false;
        NodeList clientNameList = clientElements.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_CLIENT_NAME);
        for(int i = 0; i < clientNameList.getLength(); i++ ) {
            Element clientNameElement = (Element) clientNameList.item(i);
            NodeList nl = clientNameElement.getChildNodes();
            if(nl.getLength() == 1) {
                org.w3c.dom.Node n = nl.item(0);
                if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                    if(serviceName.equalsIgnoreCase(n.getNodeValue())) {
                        serviceAlreadyAdded = true;
                    }
                }
            }
        }
        
        /** Add entry for the client to project.xml and regenerate build-impl.xml.
         */
        if(!serviceAlreadyAdded) {
            Element clientElement = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_CLIENT);
            clientElements.appendChild(clientElement);
            Element clientElementName = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_CLIENT_NAME);
            clientElement.appendChild(clientElementName);
            clientElementName.appendChild(doc.createTextNode(serviceName));
            helper.putPrimaryConfigurationData(data, true);
            needsSave = true;
        }
        
                /* Add JAXRPC libraries to javac.classpath property (needed for client compilation.)
                 */
        // !PW WebProjectProperties offers a higher level interface for complex properties
        // like classpaths, but I'm not sure how to get access to one at this point, other than
        // to create it and it's a pretty heavy weight object to just throw together like that.
        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String javacClasspath = projectProperties.getProperty(WebProjectProperties.JAVAC_CLASSPATH);
        boolean needsJaxRpc = true;
        if(javacClasspath != null) { // Should not be null, but just in case.
            String [] libs = PropertyUtils.tokenizePath(javacClasspath);
            for(int i = 0; i < libs.length; i++) {
                if("${libs.jaxrpc11.classpath}".equals(libs[i])) {
                    needsJaxRpc = false;
                    break;
                }
            }
        }
        
        if(needsJaxRpc) {
            // !PW FIXME confirm that ':' is an acceptable delimiter.  (Both ':' and ';' have been encountered.)
            javacClasspath += ":${libs.jaxrpc11.classpath}";
            projectProperties.put(WebProjectProperties.JAVAC_CLASSPATH, javacClasspath);
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
            needsSave = true;
        }
        
        // Update wscompile related properties.  boolean return indicates whether
        // any changes were made.
        if(updateWsCompileProperties(serviceName)) {
            needsSave = true;
        }
        
        // !PW Lastly, save the project if we actually made any changes to any
        // properties or the build script.
        if(needsSave) {
            try {
                ProjectManager.getDefault().saveProject(project);
            } catch(IOException ex) {
                String mes = "Error saving project when adding web service client '" + serviceName + "'\r\n" + ex.getMessage();
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
        }
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject webInfFo = getWebInf();
        if (webInfFo==null) {
            DialogDisplayer.getDefault().notify(
            new NotifyDescriptor.Message(NbBundle.getMessage(ProjectWebModule.class,"MSG_WebInfCorrupted"),
            NotifyDescriptor.ERROR_MESSAGE));
            return null;
        }
        return getWebInf().getFileObject(ProjectWebModule.FILE_DD);
    }
    
    public FileObject getWebInf() {
        return getDocumentBase().getFileObject(ProjectWebModule.FOLDER_WEB_INF);
    }
    
    public FileObject getDocumentBase() {
        return getFileObject("web.docbase.dir"); // NOI18N
    }
    
    private FileObject getFileObject(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFileObject(prop);
        } else {
            return null;
        }
    }
    
    
    private boolean updateWsCompileProperties(String serviceName) {
        /** Ensure wscompile.classpath and wscompile.tools.classpath are
         *  properly defined.
         *
         *  wscompile.classpath goes in project properties and includes
         *  jaxrpc and qname right now.
         *
         *  wscompile.tools.classpath is for tools.jar which is needed when
         *  running under the Sun JDK to invoke javac.  It is placed in
         *  user.properties so that if we compute it incorrectly (say on a mac)
         *  the user can change it and we will not blow away the change.
         *  Hopefully we can do this better for release.
         */
        boolean globalPropertiesChanged = false;
        
        EditableProperties globalProperties = PropertyUtils.getGlobalProperties();
        if(globalProperties.getProperty(WSCOMPILE_TOOLS_CLASSPATH) == null) {
            globalProperties.setProperty(WSCOMPILE_TOOLS_CLASSPATH, "${java.home}\\..\\lib\\tools.jar");
            
            try {
                PropertyUtils.putGlobalProperties(globalProperties);
            } catch(IOException ex) {
                String mes = "Error saving global properties when adding wscompile.tools.classpath for service '" + serviceName + "'\r\n" + ex.getMessage();
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
            
            globalPropertiesChanged = true;
        }
        
        
        boolean projectPropertiesChanged = false;
        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        { // Block that adjusts wscompile.client.classpath as necessary.
            HashSet wscJars = new HashSet();
            boolean newWscJars = false;
            String wscClientClasspath = projectProperties.getProperty(WSCOMPILE_CLASSPATH);
            if(wscClientClasspath != null) {
                String [] libs = PropertyUtils.tokenizePath(wscClientClasspath);
                for(int i = 0; i < libs.length; i++) {
                    wscJars.add(libs[i]);
                }
            }
            
            for(int i = 0; i < WSCOMPILE_JARS.length; i++) {
                if(!wscJars.contains(WSCOMPILE_JARS[i])) {
                    wscJars.add(WSCOMPILE_JARS[i]);
                    newWscJars = true;
                }
            }
            
            if(newWscJars) {
                StringBuffer newClasspathBuf = new StringBuffer(256);
                for(Iterator iter = wscJars.iterator(); iter.hasNext(); ) {
                    newClasspathBuf.append(iter.next().toString());
                    if(iter.hasNext()) {
                        newClasspathBuf.append(":");
                    }
                }
                projectProperties.put(WSCOMPILE_CLASSPATH, newClasspathBuf.toString());
                projectPropertiesChanged = true;
            }
        }
        
        // Set websvc.generated.dir property, if not set.
        if(projectProperties.getProperty(WEBSVC_GENERATED_DIR) == null) {
            projectProperties.setProperty(WEBSVC_GENERATED_DIR, "${build.generated.dir}/wssrc");
            projectPropertiesChanged = true;
        }
        
        if(projectPropertiesChanged) {
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
        }
        
        return globalPropertiesChanged || projectPropertiesChanged;
    }
    
    public void removeServiceClient(String serviceName) {
        // 2. Remove service from project.xml
        //    Side effect: Regenerate build-impl.xsl
        //    Optional - if last service, remove properties we generated.
        boolean needsSave = false;
        
        /** Locate root of web service client node structure in project,xml
         */
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        NodeList nodes = data.getElementsByTagName(WEB_SERVICE_CLIENTS);
        Element clientElements = null;
        
                /* If there is a root, get all the names of the child services and search
                 * for the one we want to remove.
                 */
        if(nodes.getLength() >= 1) {
            clientElements = (Element) nodes.item(0);
            NodeList clientNameList = clientElements.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_CLIENT_NAME);
            for(int i = 0; i < clientNameList.getLength(); i++ ) {
                Element clientNameElement = (Element) clientNameList.item(i);
                NodeList nl = clientNameElement.getChildNodes();
                if(nl.getLength() == 1) {
                    org.w3c.dom.Node n = nl.item(0);
                    if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                        if(serviceName.equalsIgnoreCase(n.getNodeValue())) {
                            // Found it!  Now remove it.
                            org.w3c.dom.Node serviceNode = clientNameElement.getParentNode();
                            clientElements.removeChild(serviceNode);
                            helper.putPrimaryConfigurationData(data, true);
                            needsSave = true;
                        }
                    }
                }
            }
        }
        
        // !PW Lastly, save the project if we actually made any changes to any
        // properties or the build script.
        if(needsSave) {
            try {
                ProjectManager.getDefault().saveProject(project);
            } catch(IOException ex) {
                String mes = "Error saving project when adding web service client '" + serviceName + "'\r\n" + ex.getMessage();
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
        }
    }
    
    public FileObject getWsdlFolder(boolean create) throws IOException {
        FileObject wsdlFolder = null;
        FileObject webInf = getWebInf();
        
        if(webInf != null) {
            wsdlFolder = webInf.getFileObject(WSDL_FOLDER);
            if(wsdlFolder == null && create) {
                wsdlFolder = webInf.createFolder(WSDL_FOLDER);
            }
        } else if(create) {
            // !PW FIXME create was specified, but no WEB-INF was found, so how
            // do we create it?  (Expect an NPE if we return null for this case.)
        }
        
        return wsdlFolder;
    }
    
    
}
