/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import org.netbeans.jmi.javamodel.Field;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.common.PortComponentRef;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.websvc.api.client.ClientStubDescriptor;
import org.netbeans.modules.websvc.api.client.WebServicesClientConstants;
import org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportImpl;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportImpl;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;
import org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport;
import org.netbeans.modules.websvc.api.webservices.StubDescriptor;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

import java.lang.reflect.Modifier;

/**
 *
 * @author  rico
 * Implementation of WebServicesSupportImpl and WebServicesClientSupportImpl
 */
public class EjbJarWebServicesClientSupport implements WebServicesClientSupportImpl, WebServicesClientConstants {
    
    private EjbJarProject project;
    private AntProjectHelper helper;
    private ReferenceHelper referenceHelper;
    
    /** Creates a new instance of EjbJarWebServicesSupport */
    public EjbJarWebServicesClientSupport(EjbJarProject project, AntProjectHelper helper, ReferenceHelper referenceHelper) {
        this.project = project;
        this.helper = helper;
        this.referenceHelper = referenceHelper;
    }
    
//    /**
//     * Get the webservices.xml file object
//     */
//    public FileObject getWebservicesDD() {
//        FileObject metaInfFo = getMetaInf();
//        if (metaInfFo==null) {
//            return null;
//        }
//        return getMetaInf().getFileObject(WEBSERVICES_DD, "xml");
//    }
    
//    /**
//     *  Returns the directory that contains webservices.xml in the project
//     */
//    public FileObject getWsDDFolder() {
//        return getMetaInf();
//    }
    
//    /**
//     * Returns the name of the directory that contains the webservices.xml in
//     * the archive
//     */
//    public String getArchiveDDFolderName() {
//        return "META-INF"; // NOI18N
//    }
    
 
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }
    
    public ReferenceHelper getReferenceHelper(){
        return referenceHelper;
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
            } catch(java.io.IOException ex) {
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
    

    private EjbJar getEjbJar() {
        try {
            return DDProvider.getDefault().getDDRoot(getDeploymentDescriptor());
        } catch (java.io.IOException e) {
            org.openide.ErrorManager.getDefault().log(e.getLocalizedMessage());
        }
        return null;
    }
    
    public FileObject getMetaInf() {
        EjbJarProvider provider = (EjbJarProvider)project.getLookup().lookup(EjbJarProvider.class);
        return provider.getMetaInf();
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo==null) {
            return null;
        }
        return metaInfFo.getFileObject(EjbJarProvider.FILE_DD);
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
    
    private FileObject getFileObject(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFileObject(prop);
        }
        
        return null;
    }
    
    // Implementation of WebServiceClientSupportImpl
    public void addServiceClient(String serviceName, String packageName, String sourceUrl, FileObject configFile, ClientStubDescriptor stubDescriptor) {
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
            clientElements = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_CLIENTS);
            NodeList srcRoots = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "source-roots"); // NOI18N
            assert srcRoots.getLength() == 1 : "Invalid project.xml."; // NOI18N
            data.insertBefore(clientElements, srcRoots.item(0));
        } else {
            clientElements = (Element) nodes.item(0);
        }
        
        /** Make sure this service is not already registered in project.xml
         */
        boolean serviceAlreadyAdded = false;
        NodeList clientNameList = clientElements.getElementsByTagNameNS(
        EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_CLIENT_NAME);
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
            Element clientElement = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_CLIENT);
            clientElements.appendChild(clientElement);
            Element clientElementName = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_CLIENT_NAME);
            clientElement.appendChild(clientElementName);
            clientElementName.appendChild(doc.createTextNode(serviceName));
            Element clientElementStubType = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_STUB_TYPE);
            clientElement.appendChild(clientElementStubType);
            clientElementStubType.appendChild(doc.createTextNode(stubDescriptor.getName()));
            Element clientElementSourceUrl = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, CLIENT_SOURCE_URL);
            clientElement.appendChild(clientElementSourceUrl);
            clientElementSourceUrl.appendChild(doc.createTextNode(sourceUrl));
            helper.putPrimaryConfigurationData(data, true);
            needsSave = true;
        }
        
        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // Add property for wscompile features
        {
            String featurePropertyName = "wscompile.client." + serviceName + ".features"; // NOI18N
            String defaultFeatures = "wsi, strict"; // NOI18N -- defaults if stub descriptor is bad type (should never happen?)
            if (stubDescriptor instanceof JAXRPCClientStubDescriptor) {
                JAXRPCClientStubDescriptor stubDesc = (JAXRPCClientStubDescriptor) stubDescriptor;
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
                NbBundle.getMessage(EjbJarWebServicesSupport.class,
                "MSG_ErrorSavingOnWSClientAdd", serviceName, ex.getMessage()), // NOI18N
                NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
        }
    }
    
    public void addServiceClientReference(String serviceName, String fqServiceName, String relativeWsdlPath, String relativeMappingPath, String[] portSEIInfo) {

        FileObject ddFO = getDeploymentDescriptor();

        // If we get null for the deployment descriptor, ignore this step.
        if (ddFO != null) {

            try {
                RootInterface rootDD = DDProvider.getDefault().getDDRoot(ddFO);

                ServiceRef serviceRef = (ServiceRef) rootDD.findBeanByName("ServiceRef", "ServiceRefName", serviceName); // NOI18N
                if (serviceRef == null) {
                    serviceRef = (ServiceRef) rootDD.addBean("ServiceRef", // NOI18N
                        new String [] { /* property list */ 
                            "ServiceRefName", // NOI18N
                            "ServiceInterface", // NOI18N
                            "WsdlFile", // NOI18N
                            "JaxrpcMappingFile" // NOI18N
                        },
                        new String [] { /* property values */ 
                            // service name
                            serviceName,
                            // interface package . service name
                            fqServiceName,
                            // web doc base / wsdl folder / wsdl file name
                            relativeWsdlPath,
                            // web doc base / mapping file name
                            relativeMappingPath
                        },
                        "ServiceRefName"); // NOI18N
                } else {
                    serviceRef.setServiceInterface(fqServiceName);
                    serviceRef.setWsdlFile(new URI(relativeWsdlPath));
                    serviceRef.setJaxrpcMappingFile(relativeMappingPath);
                }

                PortComponentRef [] portRefArray = new PortComponentRef [portSEIInfo.length];
                for (int pi = 0; pi < portRefArray.length; pi++) {
                    portRefArray[pi] = (PortComponentRef) serviceRef.createBean("PortComponentRef"); // NOI18N
                    portRefArray[pi].setServiceEndpointInterface(portSEIInfo[pi]); // NOI18N
                }
                serviceRef.setPortComponentRef(portRefArray);
                rootDD.write(ddFO);

            } catch (IOException ex) {
                // Strange thing happen
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } catch (NameAlreadyUsedException ex) {
                // Should never happen because we look for it by name first.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } catch (URISyntaxException ex) {
                // Programmer error - validation of input data should ensure this never happens.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } catch (ClassNotFoundException ex) {
                // Programmer error - mistyped object name.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            
        }
        
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
            NodeList clientNameList = clientElements.getElementsByTagNameNS(
            EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_CLIENT_NAME);
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
                NbBundle.getMessage(EjbJarWebServicesSupport.class,
                "MSG_ErrorSavingOnWSClientRemove", serviceName, ex.getMessage()), // NOI18N
                NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
        }
    }
    
    public FileObject getWsdlFolder(boolean create) throws IOException {
        FileObject wsdlFolder = null;
        FileObject metaInf = getMetaInf();
        
        if(metaInf != null) {
            wsdlFolder = metaInf.getFileObject(WSDL_FOLDER);
            if(wsdlFolder == null && create) {
                wsdlFolder = metaInf.createFolder(WSDL_FOLDER);
            }
        } else if(create) {
            // Create was specified, but no META-INF was found, so how do we create it?
            // Expect an NPE if we return null for this case, but log it anyway.
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
            NbBundle.getMessage(EjbJarWebServicesSupport.class, "MSG_MetaInfNotFoundForWsdlFolder"));
        }
        
        return wsdlFolder;
    }
    
    public List/*StubDescriptor*/ getStubDescriptors() {
        ArrayList stubs = new ArrayList(2);
        String version = project.getEjbModule().getJ2eePlatformVersion();
        if(EjbProjectConstants.J2EE_14_LEVEL.equals(version)) {
            stubs.add(jsr109ClientStub);
        }
        stubs.add(jaxrpcClientStub);
        return stubs;
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
        "datahandleronly", // - portable
        //        "documentliteral", // SEI ONLY
        //        "rpcliteral", // SEI ONLY
        "explicitcontext",
        //        "infix:<name>", // difficult to implement.
        "jaxbenumtype",
        "nodatabinding", //  - portable
        "noencodedtypes",
        "nomultirefs",
        "norpcstructures", //  - portable
        "novalidation", //  - portable
        "resolveidref",
        "searchschema", //  - portable
        "serializeinterfaces",
        "strict", //  - portable
        //        "useonewayoperations", // SEI ONLY
        "wsi", // - portable
        "unwrap",// - portable
        "donotoverride", // - portable
        "donotunwrap", // - portable
    };
    
    private static final List allClientFeatures = Arrays.asList(WSCOMPILE_CLIENT_FEATURES);
    
    private static final String [] WSCOMPILE_KEY_CLIENT_FEATURES = {
        "wsi",
        "strict",
        "unwrap",
        "donotunwrap",
        "donotoverride",
        "datahandleronly",
        "nodatabinding",
        "norpcstructures",
        "novalidation",
        "searchschema",
    };
    
    private static final List importantClientFeatures = Arrays.asList(WSCOMPILE_KEY_CLIENT_FEATURES);
    
    public List getServiceClients() {
        List serviceNames = new ArrayList();
        
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nodes = data.getElementsByTagName(WEB_SERVICE_CLIENTS);
        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        if(nodes.getLength() != 0) {
            Element clientElements = (Element) nodes.item(0);
            NodeList clientNameList = clientElements.getElementsByTagNameNS(
            EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_CLIENT_NAME);
            for(int i = 0; i < clientNameList.getLength(); i++ ) {
                Element clientNameElement = (Element) clientNameList.item(i);
                NodeList nl = clientNameElement.getChildNodes();
                if(nl.getLength() == 1) {
                    org.w3c.dom.Node n = nl.item(0);
                    if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                        String serviceName = n.getNodeValue();
                        String currentFeatures = projectProperties.getProperty("wscompile.client." + serviceName + ".features");
                        if(currentFeatures == null) {
                            // !PW should probably retrieve default features for stub type.
                            // For now, this will work because this is the same value we'd get doing that.
                            //
                            // Defaults if we can't find any feature property for this client
                            // Mostly for upgrading EA1, EA2 projects which did not have
                            // this property, but also useful if the user deletes it from
                            // project.properties.
                            currentFeatures = "wsi, strict";
                        }
                        ClientStubDescriptor stubType = getClientStubDescriptor(clientNameElement.getParentNode());
                        WsCompileClientEditorSupport.ServiceSettings settings = new WsCompileClientEditorSupport.ServiceSettings(
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
    
    private ClientStubDescriptor getClientStubDescriptor(org.w3c.dom.Node parentNode) {
        ClientStubDescriptor result = null;
        
        if(parentNode instanceof Element) {
            Element parentElement = (Element) parentNode;
            NodeList clientNameList = parentElement.getElementsByTagNameNS(
            EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesConstants.WEB_SERVICE_STUB_TYPE);
            if(clientNameList.getLength() == 1) {
                Element clientStubElement = (Element) clientNameList.item(0);
                NodeList nl = clientStubElement.getChildNodes();
                if(nl.getLength() == 1) {
                    org.w3c.dom.Node n = nl.item(0);
                    if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                        String stubName = n.getNodeValue();
                        if (ClientStubDescriptor.JSR109_CLIENT_STUB.equals(stubName)) {
                            result = jsr109ClientStub;
                        } else if (ClientStubDescriptor.JAXRPC_CLIENT_STUB.equals(stubName)) {
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
            EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesConstants.CLIENT_SOURCE_URL);
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
            EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesConstants.CLIENT_SOURCE_URL);
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
                Element clientElementSourceUrl = doc.createElementNS(
                EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, CLIENT_SOURCE_URL);
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
                NbBundle.getMessage(EjbJarWebServicesSupport.class,
                "MSG_ErrorSavingOnWSClientAdd", serviceName, ex.getMessage()), // NOI18N
                NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
        }
    }
    
    private Element getWebServiceClientNode(Element data, String serviceName) {
        Element clientElement = null;
        NodeList nodes = data.getElementsByTagName(WEB_SERVICE_CLIENTS);
        
        if(nodes.getLength() != 0) {
            Element clientElements = (Element) nodes.item(0);
            NodeList clientNameList = clientElements.getElementsByTagNameNS(
            EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_CLIENT_NAME);
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
    
    // Client stub descriptors
    private static final JAXRPCClientStubDescriptor jsr109ClientStub = new JAXRPCClientStubDescriptor(
    ClientStubDescriptor.JSR109_CLIENT_STUB,
    NbBundle.getMessage(EjbJarWebServicesSupport.class,"LBL_JSR109ClientStub"),
    new String [] { "wsi", "strict" });
    
    private static final JAXRPCClientStubDescriptor jaxrpcClientStub = new JAXRPCClientStubDescriptor(
    ClientStubDescriptor.JAXRPC_CLIENT_STUB,
    NbBundle.getMessage(EjbJarWebServicesSupport.class,"LBL_JAXRPCStaticClientStub"),
    new String [] { "wsi", "strict" });
    
    /** Stub descriptor for clients supported by this project type.
     */
    private static class JAXRPCClientStubDescriptor extends ClientStubDescriptor {
        
        private String [] defaultFeatures;
        
        public JAXRPCClientStubDescriptor(String name, String displayName, String [] defaultFeatures) {
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
