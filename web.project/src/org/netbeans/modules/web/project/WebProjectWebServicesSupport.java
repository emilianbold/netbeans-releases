/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project;

import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.websvc.spi.webservices.WebServicesClientSupportImpl;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportImpl;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.LifecycleManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;
import org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport;
import org.netbeans.modules.websvc.api.webservices.StubDescriptor;
import org.netbeans.spi.project.support.ant.ReferenceHelper;


/**
 *
 * @author  rico
 * Implementation of WebServicesSupportImpl and WebServicesClientSupportImpl
 */
public class WebProjectWebServicesSupport implements WebServicesSupportImpl, WebServicesClientSupportImpl, WebServicesConstants{
    private WebProject project;
    private AntProjectHelper helper;
    private ReferenceHelper referenceHelper;
    
    /** Creates a new instance of WebProjectWebServicesSupport */
    public WebProjectWebServicesSupport(WebProject project, AntProjectHelper helper, ReferenceHelper referenceHelper) {
        this.project = project;
        this.helper = helper;
        this.referenceHelper = referenceHelper;
    }
    
    //implementation of WebServicesSupportImpl
    
    public void addServiceImpl(String serviceName, FileObject configFile, boolean fromWSDL) {
        //Add properties to project.properties file
        EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String packageName = getPackageName(configFile);
        ep.put(serviceName + CONFIG_PROP_SUFFIX, packageName +
        (packageName.equals("") ? "" : "/") + configFile.getNameExt()); //NOI18N
        ep.put(serviceName + MAPPING_PROP_SUFFIX, serviceName + MAPPING_FILE_SUFFIX); //NOI18N
        // Add property for wscompile
        String featurePropertyName = "wscompile.service." + serviceName + ".features"; // NOI18N
        String defaultFeatures = fromWSDL ? wsdlServiceStub.getDefaultFeaturesAsArgument() :
            seiServiceStub.getDefaultFeaturesAsArgument();
        ep.put(featurePropertyName, defaultFeatures);
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
        if(fromWSDL) {
            Element fromWSDLElem = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "from-wsdl");
            webservice.appendChild(fromWSDLElem);
        }
        helper.putPrimaryConfigurationData(data, true);
        // Update wscompile related properties.  boolean return indicates whether
        // any changes were made.
        updateWsCompileProperties(serviceName);
        try {
            ProjectManager.getDefault().saveProject(project);
        }catch(java.io.IOException ioe){
            throw new RuntimeException(ioe.getMessage());
        }
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
    
    public  void addServiceEntriesToDD(String serviceName, String serviceEndpointInterface, String servantClassName) {
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
            if (isProjectOpened()) {
                DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(WebProjectWebServicesSupport.class,"MSG_WebInfCorrupted"),
                NotifyDescriptor.ERROR_MESSAGE));
            }
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
        return "WEB-INF"; // NOI18N
    }
    
    /**
     * Returns the name of the implementation bean class
     * given the servlet-link name
     */
    public String getImplementationBean(String linkName) {
        WebApp webApp = getWebApp();
        org.netbeans.modules.j2ee.dd.api.web.Servlet[] servlets = webApp.getServlet();
        for(int i = 0; i < servlets.length; i++) {
            if(servlets[i].getServletName().equals(linkName)) {
                return servlets[i].getServletClass();
            }
        }
        return null;
    }
    
    public void removeProjectEntries(String serviceName) {
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
        String featureProperty = "wscompile.service." + serviceName + ".features"; // NOI18N
        if(ep.getProperty(featureProperty) != null) {
            ep.remove(featureProperty);
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
                String mes = NbBundle.getMessage(this.getClass(), "MSG_ErrorSavingOnWSRemove") + serviceName // NOI18N
                + "'\r\n" + ex.getMessage();
                NotifyDescriptor desc = new NotifyDescriptor.
                Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);			}
        }
    }
    
    public void removeServiceEntry(String linkName) {
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
            new NotifyDescriptor.Message(NbBundle.getMessage(this.getClass(), "MSG_Unable_WRITE_WS_DD"), // NOI18N
            NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(ndd);
        }

        // Would be nice if we could do this to save the server specific configuration
        // but saveConfiguration is to available in the interface (though it is in
        // ConfigSupportImpl which implements this interface.)
        // We will have to force a saveAll to make sure that any server specific changes
        // have been persisted.  See IZ 55181 and 55414
//        J2eeModuleProvider.ConfigSupport configSupport = project.getWebModule().getConfigSupport();
//        if(configSupport != null) {
//            configSupport.saveConfiguration();
//        }
        LifecycleManager.getDefault().saveAll();
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }
    
    public String generateImplementationBean(String wsName, FileObject pkg, Project project, String delegateData)
    throws java.io.IOException {
        return null;
        //FIX-ME: move impl bean generation here
    }
    
    public void addServiceImplLinkEntry(ServiceImplBean serviceImplBean, String wsName) {
        serviceImplBean.setServletLink(WebServiceServlet_PREFIX + wsName);
    }
    
    public ReferenceHelper getReferenceHelper(){
        return referenceHelper;
    }

    /** !PW This method is exposed in the service support API.  Though it's 
     *  implementation makes more sense here than anywhere else, perhaps this
     *  and the other project.xml/project.properties related methods in this
     *  object should be refactored into another object that this one delegates
     *  to.  That way, this method would be directly available within the web or
     *  ejb module, as it is needed, and remain missing from the API (where it
     *  probably does not belong at this time.
     */
    private static final String [] WSCOMPILE_SEI_SERVICE_FEATURES = {
//        "datahandleronly", // WSDL
        "documentliteral", // SEI ONLY
        "rpcliteral", // SEI ONLY
//        "explicitcontext", // WSDL
//        "infix:<name>", // difficult handle with current API
//        "jaxbenumtype", // WSDL
//        "nodatabinding", // WSDL
        "noencodedtypes",
        "nomultirefs",
//        "norpcstructures", // import only
//        "novalidation", // WSDL
//        "resolveidref", // WSDL
//        "searchschema", // WSDL
        "serializeinterfaces",
        "strict",
        "useonewayoperations", // SEI ONLY
//        "wsi", // WSDL
//        "unwrap", // WSDL
        "donotoverride",
//        "donotunwrap", // WSDL
    };

    private static final List allSeiServiceFeatures = Arrays.asList(WSCOMPILE_SEI_SERVICE_FEATURES);

    private static final String [] WSCOMPILE_KEY_SEI_SERVICE_FEATURES = {
        "documentliteral",
        "rpcliteral",
        "noencodedtypes",
    };
    
    private static final List importantSeiServiceFeatures = Arrays.asList(WSCOMPILE_KEY_SEI_SERVICE_FEATURES);

    private static final String [] WSCOMPILE_WSDL_SERVICE_FEATURES = {
        "datahandleronly", // WSDL
//        "documentliteral", // SEI ONLY
//        "rpcliteral", // SEI ONLY
        "explicitcontext", // WSDL
//        "infix:<name>", // difficult handle with current API
        "jaxbenumtype", // WSDL
        "nodatabinding", // WSDL
        "noencodedtypes",
        "nomultirefs",
        "norpcstructures", // import only
        "novalidation", // WSDL
        "resolveidref", // WSDL
        "searchschema", // WSDL
        "serializeinterfaces",
        "strict",
//        "useonewayoperations", // SEI ONLY
        "wsi", // WSDL
        "unwrap", // WSDL
        "donotoverride",
        "donotunwrap", // WSDL
    };

    private static final List allWsdlServiceFeatures = Arrays.asList(WSCOMPILE_WSDL_SERVICE_FEATURES);

    private static final String [] WSCOMPILE_KEY_WSDL_SERVICE_FEATURES = {
        "norpcstructures",
        "donotunwrap",
        "datahandleronly"
    };
    
    private static final List importantWsdlServiceFeatures = Arrays.asList(WSCOMPILE_KEY_WSDL_SERVICE_FEATURES);
    
    public List/*WsCompileEditorSupport.ServiceSettings*/ getServices() {
        List serviceList = new ArrayList();
        
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nodes = data.getElementsByTagName(WebServicesConstants.WEB_SERVICES);
        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

        if(nodes.getLength() != 0) {
            Element serviceElements = (Element) nodes.item(0);
            NodeList serviceNameList = serviceElements.getElementsByTagNameNS(
                WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesConstants.WEB_SERVICE_NAME);
            for(int i = 0; i < serviceNameList.getLength(); i++ ) {
                Element serviceNameElement = (Element) serviceNameList.item(i);
                NodeList nl = serviceNameElement.getChildNodes();
                if(nl.getLength() == 1) {
                    org.w3c.dom.Node n = nl.item(0);
                    if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                        String serviceName = n.getNodeValue();
                        String currentFeatures = projectProperties.getProperty("wscompile.service." + serviceName + ".features"); // NOI18N
                        StubDescriptor stubType = getServiceStubDescriptor(serviceNameElement.getParentNode());
                        WsCompileEditorSupport.ServiceSettings settings;
                        
                        if(seiServiceStub == stubType) {
                            if(currentFeatures == null) {
                                // default for SEI generation
                                currentFeatures = "documentliteral"; // NOI18N
                            }
                            settings = new WsCompileEditorSupport.ServiceSettings(
                                serviceName, stubType, currentFeatures, allSeiServiceFeatures, importantSeiServiceFeatures);
                        } else {
                            if(currentFeatures == null) {
                                // default for WSDL generation
                                currentFeatures = "norpcstructures,wsi"; // NOI18N
                            }
                            settings = new WsCompileEditorSupport.ServiceSettings(
                                serviceName, stubType, currentFeatures, allWsdlServiceFeatures, importantWsdlServiceFeatures);
                        }
                        serviceList.add(settings);
                    } else {
                        // !PW FIXME node is wrong type?! - log message or trace?
                    }
                } else {
                    // !PW FIXME no name for this service entry - notify user
                }
            }
        }

        return serviceList;
    }

    private StubDescriptor getServiceStubDescriptor(org.w3c.dom.Node parentNode) {
        StubDescriptor result = null;
        
        if(parentNode instanceof Element) {
            Element parentElement = (Element) parentNode;
            NodeList fromWsdlList = parentElement.getElementsByTagNameNS(
                WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesConstants.WEB_SERVICE_FROM_WSDL);
            if(fromWsdlList.getLength() == 1) {
                result = wsdlServiceStub;
            } else {
                result = seiServiceStub;
            }
        }
        
        return result;
    }
    
    private String getPackageName(FileObject file){
        FileObject parent = file.getParent();
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String packageName = null;
        for (int i = 0; i < groups.length && packageName == null; i++) {
            packageName = FileUtil.getRelativePath(groups[i].getRootFolder(), parent);
            if (packageName != null) {
                packageName = groups[i].getName() + "/" + packageName;
            }
        }
        return packageName + "";
    }
    
    // Implementation of WebServiceClientSupportImpl
    public void addServiceClient(String serviceName, String packageName, String sourceUrl, FileObject configFile, StubDescriptor stubDescriptor) {
        // !PW FIXME I have two concerns with this implementation:
        // 1. Since it modifies project.xml, I suspect it should be acquiring
        //    ProjectManager.mutex() for write access.
        // 2. It seems like it ought to be implemented via the AuxiliaryConfiguration
        //    interface.
        boolean needsSave = false;
        boolean modifiedProjectProperties = false;
        
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
            if(nl.getLength() >= 1) {
                org.w3c.dom.Node n = nl.item(0);
                if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                    if(serviceName.equalsIgnoreCase(n.getNodeValue())) {
                        serviceAlreadyAdded = true;
                        
                        // !PW FIXME should force stub type to match value passed in
                        // in case someone is overwriting a current service with a different
                        // stub type.
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
            Element clientElementStubType = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_STUB_TYPE);
            clientElement.appendChild(clientElementStubType);
            clientElementStubType.appendChild(doc.createTextNode(stubDescriptor.getName()));
            Element clientElementSourceUrl = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, CLIENT_SOURCE_URL);
            clientElement.appendChild(clientElementSourceUrl);
            clientElementSourceUrl.appendChild(doc.createTextNode(sourceUrl));
            helper.putPrimaryConfigurationData(data, true);
            needsSave = true;
        }
        
        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // Add property for wscompile features
        {
            String featurePropertyName = "wscompile.client." + serviceName + ".features"; // NOI18N
            String defaultFeatures = "norpcstructures"; // NOI18N
            if(stubDescriptor instanceof JAXRPCStubDescriptor) {
                JAXRPCStubDescriptor stubDesc = (JAXRPCStubDescriptor) stubDescriptor;
                defaultFeatures = stubDesc.getDefaultFeaturesAsArgument();
            } else {
                // !PW FIXME wrong stub type -- log error message.
            }
            String oldFeatures = projectProperties.getProperty(featurePropertyName);
            if(!defaultFeatures.equals(oldFeatures)) {
                projectProperties.put(featurePropertyName, defaultFeatures);
                modifiedProjectProperties = true;
            }
        }
        
        // Add package name property
        {
            String packagePropertyName = "wscompile.client." + serviceName + ".package"; // NOI18N
            String oldPackageName = projectProperties.getProperty(packagePropertyName);
            if(!packageName.equals(oldPackageName)) {
                projectProperties.put(packagePropertyName, packageName);
                modifiedProjectProperties = true;
            }
        }
        
        if(modifiedProjectProperties) {
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
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                NbBundle.getMessage(WebProjectWebServicesSupport.class,"MSG_ErrorSavingOnWSClientAdd", serviceName, ex.getMessage()), // NOI18N
                NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
        }
    }
    
    public void addInfrastructure(String implBeanClass, FileObject pkg) {
        //nothing to do here, there are no infrastructure elements
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject webInfFo = getWebInf();
        if (webInfFo==null) {
            if (isProjectOpened()) {
                DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(WebProjectWebServicesSupport.class,"MSG_WebInfCorrupted"), // NOI18N
                NotifyDescriptor.ERROR_MESSAGE));
            }
            return null;
        }
        return getWebInf().getFileObject(ProjectWebModule.FILE_DD);
    }
    
    public FileObject getWebInf() {
        FileObject documentBase = getDocumentBase();
        if (documentBase != null)
            return documentBase.getFileObject(ProjectWebModule.FOLDER_WEB_INF);
        else
            return null;
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
            globalProperties.setProperty(WSCOMPILE_TOOLS_CLASSPATH, "${java.home}\\..\\lib\\tools.jar"); // NOI18N
            
            try {
                PropertyUtils.putGlobalProperties(globalProperties);
            } catch(IOException ex) {
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                NbBundle.getMessage(WebProjectWebServicesSupport.class,"MSG_ErrorSavingGlobalProperties", serviceName, ex.getMessage()), // NOI18N
                NotifyDescriptor.ERROR_MESSAGE);
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
            projectProperties.setProperty(WEBSVC_GENERATED_DIR, "${build.generated.dir}/wssrc"); // NOI18N
            projectPropertiesChanged = true;
        }
        
        // set tools.jar property if not set
        if(projectProperties.getProperty(WSCOMPILE_TOOLS_CLASSPATH) == null) {
            projectProperties.setProperty(WSCOMPILE_TOOLS_CLASSPATH, "${java.home}\\..\\lib\\tools.jar"); // NOI18N
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

        /** Remove properties from project.properties
         */
        String featureProperty = "wscompile.client." + serviceName + ".features"; // NOI18N
        String packageProperty = "wscompile.client." + serviceName + ".package"; // NOI18N
        EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        if(ep.getProperty(featureProperty) != null) {
            ep.remove(featureProperty);
            needsSave = true;
        }
        
        if(ep.getProperty(packageProperty) != null) {
            ep.remove(packageProperty);
            needsSave = true;
        }
        
        if(needsSave) {
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
        
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
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                NbBundle.getMessage(WebProjectWebServicesSupport.class,"MSG_ErrorSavingOnWSClientRemove", serviceName, ex.getMessage()), // NOI18N
                NotifyDescriptor.ERROR_MESSAGE);
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
            // Create was specified, but no WEB-INF was found, so how do we create it?
            // Expect an NPE if we return null for this case, but log it anyway.
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                NbBundle.getMessage(WebProjectWebServicesSupport.class,"MSG_WebInfNotFoundForWsdlFolder"));
        }
        
        return wsdlFolder;
    }
    
    public List/*StubDescriptor*/ getStubDescriptors() {
        ArrayList stubs = new ArrayList(2);
        String version = project.getWebModule().getJ2eePlatformVersion();
        if(WebModule.J2EE_14_LEVEL.equals(version)) {
            stubs.add(jsr109ClientStub);
        }
        stubs.add(jaxrpcClientStub);
        return stubs;
    }
    
    private boolean isProjectOpened() {
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].equals(project)) 
                return true;
        }
        return false;
    }
    
    /** !PW This method is exposed in the client support API.  Though it's
     *  implementation makes more sense here than anywhere else, perhaps this
     *  and the other project.xml/project.properties related methods in this
     *  object should be refactored into another object that this one delegates
     *  to.  That way, this method would be directly available within the web
     *  web module, as it is needed, and remain missing from the API (where it
     *  probably does not belong at this time.
     */
    private static final String [] WSCOMPILE_CLIENT_FEATURES = {
        "datahandleronly",
        //        "documentliteral", // SEI ONLY
        //        "rpcliteral", // SEI ONLY
        "explicitcontext",
        //        "infix:<name>", // difficult to implement.
        "jaxbenumtype",
        "nodatabinding",
        "noencodedtypes",
        "nomultirefs",
        "norpcstructures",
        "novalidation",
        "resolveidref",
        "searchschema",
        "serializeinterfaces",
        "strict",
        //        "useonewayoperations", // SEI ONLY
        "wsi",
        "unwrap",
        "donotoverride",
        "donotunwrap",
    };
    
    private static final List allClientFeatures = Arrays.asList(WSCOMPILE_CLIENT_FEATURES);
    
    private static final String [] WSCOMPILE_KEY_CLIENT_FEATURES = {
        //        "documentliteral",
        //        "rpcliteral",
        "noencodedtypes",
        "wsi",
    };
    
    private static final List importantClientFeatures = Arrays.asList(WSCOMPILE_KEY_CLIENT_FEATURES);
    
    public List getServiceClients() {
        List serviceNames = new ArrayList();
        
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nodes = data.getElementsByTagName(WebServicesConstants.WEB_SERVICE_CLIENTS);
        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        if(nodes.getLength() != 0) {
            Element clientElements = (Element) nodes.item(0);
            NodeList clientNameList = clientElements.getElementsByTagNameNS(
            WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesConstants.WEB_SERVICE_CLIENT_NAME);
            for(int i = 0; i < clientNameList.getLength(); i++ ) {
                Element clientNameElement = (Element) clientNameList.item(i);
                NodeList nl = clientNameElement.getChildNodes();
                if(nl.getLength() == 1) {
                    org.w3c.dom.Node n = nl.item(0);
                    if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                        String serviceName = n.getNodeValue();
                        String currentFeatures = projectProperties.getProperty("wscompile.client." + serviceName + ".features");
                        if(currentFeatures == null) {
                            currentFeatures = "documentliteral, wsi, norpcstructures";
                        }
                        StubDescriptor stubType = getClientStubDescriptor(clientNameElement.getParentNode());
                        WsCompileEditorSupport.ServiceSettings settings = new WsCompileEditorSupport.ServiceSettings(
                            serviceName, stubType, currentFeatures, allClientFeatures, importantClientFeatures);
                        serviceNames.add(settings);
                    } else {
                        // !PW FIXME node is wrong type?! - log message or trace?
                    }
                } else {
                    // !PW FIXME no name for this service entry - notify user
                }
            }
        }
        
        return serviceNames;
    }
            
    private StubDescriptor getClientStubDescriptor(org.w3c.dom.Node parentNode) {
        StubDescriptor result = null;
        
        if(parentNode instanceof Element) {
            Element parentElement = (Element) parentNode;
            NodeList clientNameList = parentElement.getElementsByTagNameNS(
                WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesConstants.WEB_SERVICE_STUB_TYPE);
            if(clientNameList.getLength() == 1) {
                Element clientStubElement = (Element) clientNameList.item(0);
                NodeList nl = clientStubElement.getChildNodes();
                if(nl.getLength() == 1) {
                    org.w3c.dom.Node n = nl.item(0);
                    if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                        String stubName = n.getNodeValue();
                        if(StubDescriptor.JSR109_CLIENT_STUB.equals(stubName)) {
                            result = jsr109ClientStub;
                        } else if(StubDescriptor.JAXRPC_CLIENT_STUB.equals(stubName)) {
                            result = jaxrpcClientStub;
                        }
                    }
                }
            }
        }
        
        return result;
    }

    public String getWsdlSource(String serviceName) {
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        String wsdlSource = null;
        
        Element clientElement = getWebServiceClientNode(data, serviceName);
        if(clientElement != null) {
            NodeList fromWsdlList = clientElement.getElementsByTagNameNS(
                WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesConstants.CLIENT_SOURCE_URL);
            if(fromWsdlList.getLength() == 1) {
                Element fromWsdlElement = (Element) fromWsdlList.item(0);
                NodeList nl = fromWsdlElement.getChildNodes();
                if(nl.getLength() == 1) {
                    org.w3c.dom.Node n = nl.item(0);
                    if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                        wsdlSource = n.getNodeValue();
                    }
                }
            }
        }
        
        return wsdlSource;
    }
    
    public void setWsdlSource(String serviceName, String wsdlSource) {
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        boolean needsSave = false;

        Element clientElement = getWebServiceClientNode(data, serviceName);
        if(clientElement != null) {
            NodeList fromWsdlList = clientElement.getElementsByTagNameNS(
                WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesConstants.CLIENT_SOURCE_URL);
            if(fromWsdlList.getLength() > 0) {
                Element fromWsdlElement = (Element) fromWsdlList.item(0);
                NodeList nl = fromWsdlElement.getChildNodes();
                if(nl.getLength() > 0) {
                    org.w3c.dom.Node n = nl.item(0);
                    n.setNodeValue(wsdlSource);
                } else {
                    fromWsdlElement.appendChild(doc.createTextNode(wsdlSource));
                }
            } else {
                Element clientElementSourceUrl = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, CLIENT_SOURCE_URL);
                clientElement.appendChild(clientElementSourceUrl);
                clientElementSourceUrl.appendChild(doc.createTextNode(wsdlSource));
            }
            
            needsSave = true;
        }
        
        // !PW Save the project if we were able to make the change.
        if(needsSave) {
            try {
                ProjectManager.getDefault().saveProject(project);
            } catch(IOException ex) {
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                NbBundle.getMessage(WebProjectWebServicesSupport.class,"MSG_ErrorSavingOnWSClientAdd", serviceName, ex.getMessage()), // NOI18N
                NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
        }
    }
    
    private Element getWebServiceClientNode(Element data, String serviceName) {
        Element clientElement = null;
        NodeList nodes = data.getElementsByTagName(WebServicesConstants.WEB_SERVICE_CLIENTS);
        
        if(nodes.getLength() != 0) {
            Element clientElements = (Element) nodes.item(0);
            NodeList clientNameList = clientElements.getElementsByTagNameNS(
                WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesConstants.WEB_SERVICE_CLIENT_NAME);
            for(int i = 0; i < clientNameList.getLength(); i++ ) {
                Element clientNameElement = (Element) clientNameList.item(i);
                NodeList nl = clientNameElement.getChildNodes();
                if(nl.getLength() == 1) {
                    org.w3c.dom.Node n = nl.item(0);
                    if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                        String name = n.getNodeValue();
                        if(serviceName.equals(name)) {
                            org.w3c.dom.Node node = clientNameElement.getParentNode();
                            clientElement = (node instanceof Element) ? (Element) node : null;
                            break;
                        }
                    } else {
                        // !PW FIXME node is wrong type?! - log message or trace?
                    }
                }
            }
        }
        
        return clientElement;
    }
    
    // Service stub descriptors
    private static final JAXRPCStubDescriptor seiServiceStub = new JAXRPCStubDescriptor(
        StubDescriptor.SEI_SERVICE_STUB,
        NbBundle.getMessage(WebProjectWebServicesSupport.class,"LBL_SEIServiceStub"),
        new String [] { "documentliteral" });
    
    private static final JAXRPCStubDescriptor wsdlServiceStub = new JAXRPCStubDescriptor(
        StubDescriptor.WSDL_SERVICE_STUB,
        NbBundle.getMessage(WebProjectWebServicesSupport.class,"LBL_WSDLServiceStub"),
        new String [] { "norpcstructures" });
    
    
    // Client stub descriptors
    private static final JAXRPCStubDescriptor jsr109ClientStub = new JAXRPCStubDescriptor(
        StubDescriptor.JSR109_CLIENT_STUB,
        NbBundle.getMessage(WebProjectWebServicesSupport.class,"LBL_JSR109ClientStub"),
        new String [] { "norpcstructures" });
    
    private static final JAXRPCStubDescriptor jaxrpcClientStub = new JAXRPCStubDescriptor(
        StubDescriptor.JAXRPC_CLIENT_STUB,
        NbBundle.getMessage(WebProjectWebServicesSupport.class,"LBL_JAXRPCStaticClientStub"),
        new String [0]);
    
    /** !PW FIXME add required features, etc. for this stub.
     */
    private static class JAXRPCStubDescriptor extends StubDescriptor {
        
        private String [] defaultFeatures;
        
        public JAXRPCStubDescriptor(String name, String displayName, String [] defaultFeatures) {
            super(name, displayName);
            
            this.defaultFeatures = defaultFeatures;
        }
        
        public String [] getDefaultFeatures() {
            return defaultFeatures;
        }
        
        public String getDefaultFeaturesAsArgument() {
            StringBuffer buf = new StringBuffer(defaultFeatures.length*32);
            for(int i = 0; i < defaultFeatures.length; i++) {
                if(i > 0) {
                    buf.append(",");
                }
                
                buf.append(defaultFeatures[i]);
            }
            return buf.toString();
        }
    }
}
