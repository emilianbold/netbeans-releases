/*
 * EjbJarWebServicesSupport.java
 *
 * Created on November 5, 2004, 11:23 AM
 */

package org.netbeans.modules.j2ee.ejbjarproject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.session.SessionGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportImpl;
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
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;
import org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport;
import org.netbeans.modules.websvc.api.webservices.StubDescriptor;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

import java.lang.reflect.Modifier;
import org.openide.src.ClassElement;
import org.openide.src.FieldElement;
import org.openide.src.Identifier;
import org.openide.src.MethodElement;
import org.openide.src.MethodParameter;
import org.openide.src.SourceException;
import org.openide.src.Type;

/**
 *
 * @author  rico
 * Implementation of WebServicesSupportImpl and WebServicesClientSupportImpl
 */
public class EjbJarWebServicesSupport implements WebServicesSupportImpl, WebServicesConstants{
    
    private EjbJarProject project;
    private AntProjectHelper helper;
    private ReferenceHelper referenceHelper;
    
    /** Creates a new instance of EjbJarWebServicesSupport */
    public EjbJarWebServicesSupport(EjbJarProject project, AntProjectHelper helper, ReferenceHelper referenceHelper) {
        this.project = project;
        this.helper = helper;
        this.referenceHelper = referenceHelper;
    }
    
    //implementation of WebServicesSupportImpl
    public String generateImplementationBean(String wsName, FileObject pkg, Project project, String delegateData)throws java.io.IOException {
        SessionGenerator sessionGenerator = new SessionGenerator();
        return sessionGenerator.generateWebServiceImplBean(wsName, pkg, project, delegateData);
    }
    
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
                webservices = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICES); //NOI18N
                data.appendChild(webservices);
            }
            else{
                webservices = (Element)nodes.item(0);
            }
            Element webservice = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE); //NOI18N
            webservices.appendChild(webservice);
            Element webserviceName = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_NAME); //NOI18N
            webservice.appendChild(webserviceName);
            webserviceName.appendChild(doc.createTextNode(serviceName));
            if(fromWSDL) {
                Element fromWSDLElem = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "from-wsdl");
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
    
    public  void addServiceEntriesToDD(String serviceName, String serviceEndpointInterface, String servantClassName) {
        //add service endpoint entry to ejb-jar.xml
        DDProvider provider = DDProvider.getDefault();
        EjbJarImplementation ejbJarImpl = (EjbJarImplementation)project.getLookup().lookup(EjbJarImplementation.class);
        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = null;
        try {
            ejbJar = provider.getDDRoot(ejbJarImpl.getDeploymentDescriptor());
        }
        catch(java.io.IOException e) {
            //FIX-ME: handle this
            throw new RuntimeException(e.getMessage());
        }
        
        EjbJarProvider pwm = (EjbJarProvider) project.getLookup().lookup(EjbJarProvider.class);
        pwm.getConfigSupport().ensureConfigurationReady();
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        Session s = null;
        if(beans == null) {
            beans = ejbJar.newEnterpriseBeans();
            ejbJar.setEnterpriseBeans(beans);
        }
        s = beans.newSession();
        s.setEjbName(serviceName);
        s.setDisplayName(serviceName + "SB");
        s.setEjbClass(servantClassName);
        try {
            s.setServiceEndpoint(serviceEndpointInterface);
        }
        catch(org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException e) {
            //FIX-ME: handle this
            throw new RuntimeException(e.getMessage());
        }
        s.setSessionType("Stateless");
        s.setTransactionType("Container");
        beans.addSession(s);
        try {
            ejbJar.write(ejbJarImpl.getDeploymentDescriptor());
            //Hack to save any defaults put in vendor-specific DD
            //Need a better way to save selectively from server plugins(an api that allows
            //server plugins to save server configuration in selective manner)
            org.openide.LifecycleManager.getDefault().saveAll();
        }
        catch(java.io.IOException e) {
            //FIX-ME: handle this
            throw new RuntimeException(e.getMessage());
        }
        
        //Hack to save any defaults put in vendor-specific DD
        //Need a better way to save selectively from server plugins(an api that allows
        //server plugins to save server configuration in selective manner)
        //FIX-ME: Do we still need this?: org.openide.LifecycleManager.getDefault().saveAll();
    }
    
    public void addServiceImplLinkEntry(ServiceImplBean serviceImplBean, String wsName) {
        serviceImplBean.setEjbLink(wsName);
    }
    
    /**
     * Get the webservices.xml file object
     */
    public FileObject getWebservicesDD() {
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo==null) {
            DialogDisplayer.getDefault().notify(
            new NotifyDescriptor.Message(NbBundle.getMessage(EjbJarProject.class,"MSG_WebInfCorrupted"),
            NotifyDescriptor.ERROR_MESSAGE));
            return null;
        }
        return getMetaInf().getFileObject(WEBSERVICES_DD, "xml");
    }
    
    /**
     *  Returns the directory that contains webservices.xml in the project
     */
    public FileObject getWsDDFolder() {
        return getMetaInf();
    }
    
    /**
     * Returns the name of the directory that contains the webservices.xml in
     * the archive
     */
    public String getArchiveDDFolderName() {
        return "META-INF"; // NOI18N
    }
    
    /**
     * Returns the name of the implementation bean class
     * given the ejb-link name
     */
    public String getImplementationBean(String linkName) {
        EjbJar ejbJar = getEjbJar();
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        Session[] sessionBeans = beans.getSession();
        for(int i = 0; i < sessionBeans.length; i++) {
            Session sessionBean = sessionBeans[i];
            if(sessionBean.getEjbName().equals(linkName)) {
                return sessionBean.getEjbClass();
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
        NodeList nodes = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
        WEB_SERVICES); //NOI18N
        Element webservices = null;
        Element wsNameNode = null;
        if(nodes.getLength() == 1){
            webservices = (Element)nodes.item(0);
            NodeList wsNodes = webservices.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
            WEB_SERVICE); //NOI18N
            for(int j = 0; j < wsNodes.getLength(); j++) {
                Element wsNode = (Element)wsNodes.item(j);
                NodeList wsNameNodes = wsNode.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
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
            } catch(java.io.IOException ex) {
                String mes = NbBundle.getMessage(this.getClass(), "MSG_ErrorSavingOnWSRemove") + serviceName // NOI18N
                + "'\r\n" + ex.getMessage(); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.
                Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);			}
        }
    }
    
    
    public void removeServiceEntry(String linkName) {
        //remove ejb  entry in ejb-jar.xml
        EjbJarImplementation ejbJarImpl = (EjbJarImplementation)project.getLookup().lookup(EjbJarImplementation.class);
        EjbJar ejbJar = getEjbJar();
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        Session[] sessionBeans = beans.getSession();
        for(int i = 0; i < sessionBeans.length; i++) {
            Session sessionBean = sessionBeans[i];
            if(sessionBean.getEjbName().equals(linkName)) {
                beans.removeSession(sessionBean);
                break;
            }
        }
        try {
            ejbJar.write(ejbJarImpl.getDeploymentDescriptor());
        }
        catch(java.io.IOException e) {
            NotifyDescriptor ndd =
            new NotifyDescriptor.Message(NbBundle.getMessage(this.getClass(), "MSG_Unable_WRITE_EJB_DD"), // NOI18N
            NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(ndd);
        }
        
        
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
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
        
        // Implementation from getServiceClients() -- FIXME
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nodes = data.getElementsByTagName(WebServicesConstants.WEB_SERVICES);
        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        if(nodes.getLength() != 0) {
            Element serviceElements = (Element) nodes.item(0);
            NodeList serviceNameList = serviceElements.getElementsByTagNameNS(
            EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesConstants.WEB_SERVICE_NAME);
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
            EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesConstants.WEB_SERVICE_FROM_WSDL);
            if(fromWsdlList.getLength() == 1) {
                result = wsdlServiceStub;
            } else {
                result = seiServiceStub;
            }
        }
        
        return result;
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
        // Set websvc.generated.dir property, if not set.
        if(projectProperties.getProperty(WEBSVC_GENERATED_DIR) == null) {
            projectProperties.setProperty(WEBSVC_GENERATED_DIR, "${build.generated.dir}/wssrc");
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
    
    public void addInfrastructure(String implBeanClass, FileObject pkg){
        try {
            ClassElement clazz = ClassElement.forName(implBeanClass, pkg);
            //remove java.rmi.Remote interface
            Identifier remote = Identifier.create("java.rmi.Remote");
            clazz.removeInterface(remote);
            
            //add javax.ejb.SessionBean interface
            Identifier session = Identifier.create("javax.ejb.SessionBean");
            clazz.addInterface(session);
            
            //add javax.ejb.SessionContext field
            Identifier sessionCtx = Identifier.create("javax.ejb.SessionContext");
            Type sessionCtxType = Type.createClass(sessionCtx);
            FieldElement field = new FieldElement();
            field.setType(sessionCtxType);
            field.setName(Identifier.create("context"));
            clazz.addField(field);
            
            //add setSessionContext(javax.ejb.SessionContext aContext) method
            MethodElement sessionCtxMethod = new MethodElement();
            sessionCtxMethod.setName(Identifier.create("setSessionContext"));
            MethodParameter ctxParam = new MethodParameter("aContext", sessionCtxType, false);
            sessionCtxMethod.setParameters(new MethodParameter[] {ctxParam});
            sessionCtxMethod.setReturn(Type.VOID);
            sessionCtxMethod.setModifiers(Modifier.PUBLIC);
            sessionCtxMethod.setBody("context = aContext;");
            clazz.addMethod(sessionCtxMethod);
            
            //add ejbActivate method
            MethodElement ejbActivateMethod = new MethodElement();
            ejbActivateMethod.setName(Identifier.create("ejbActivate"));
            ejbActivateMethod.setReturn(Type.VOID);
            ejbActivateMethod.setModifiers(Modifier.PUBLIC);
            clazz.addMethod(ejbActivateMethod);
            
            //add ejbPassivate method
            MethodElement ejbPassivateMethod = new MethodElement();
            ejbPassivateMethod.setName(Identifier.create("ejbPassivate"));
            ejbPassivateMethod.setReturn(Type.VOID);
            ejbPassivateMethod.setModifiers(Modifier.PUBLIC);
            clazz.addMethod(ejbPassivateMethod);
            
            //add ejbRemove method
            MethodElement ejbRemoveMethod = new MethodElement();
            ejbRemoveMethod.setName(Identifier.create("ejbRemove"));
            ejbRemoveMethod.setReturn(Type.VOID);
            ejbRemoveMethod.setModifiers(Modifier.PUBLIC);
            clazz.addMethod(ejbRemoveMethod);
            
            //add ejbCreate method
            MethodElement ejbCreateMethod = new MethodElement();
            ejbCreateMethod.setName(Identifier.create("ejbCreate"));
            ejbCreateMethod.setReturn(Type.VOID);
            ejbCreateMethod.setModifiers(Modifier.PUBLIC);
            clazz.addMethod(ejbCreateMethod);
        }
        catch(SourceException e) {
            DialogDisplayer.getDefault().notify(
            new NotifyDescriptor.Message(NbBundle.getMessage(EjbJarWebServicesSupport.class,"MSG_Unable_Add_EJB_Infrastructure"),
            NotifyDescriptor.ERROR_MESSAGE));
        }
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
        return getFileObject(EjbJarProjectProperties.META_INF);
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo==null) {
            DialogDisplayer.getDefault().notify(
            new NotifyDescriptor.Message(NbBundle.getMessage(EjbJarProject.class,"MSG_WebInfCorrupted"),
            NotifyDescriptor.ERROR_MESSAGE));
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
            packageName = FileUtil.getRelativePath(groups [i].getRootFolder(), parent);
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
    
    // Service stub descriptors
    private static final JAXRPCStubDescriptor seiServiceStub = new JAXRPCStubDescriptor(
    StubDescriptor.SEI_SERVICE_STUB,
    NbBundle.getMessage(EjbJarWebServicesSupport.class,"LBL_SEIServiceStub"), // NOI18N
    new String [0]);
    
    private static final JAXRPCStubDescriptor wsdlServiceStub = new JAXRPCStubDescriptor(
    StubDescriptor.WSDL_SERVICE_STUB,
    NbBundle.getMessage(EjbJarWebServicesSupport.class,"LBL_WSDLServiceStub"), // NOI18N
    new String [] { "norpcstructures" }); // NOI18N
    
    /** Stub descriptor for services and clients supported by this project type.
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
