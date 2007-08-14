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

package org.netbeans.modules.identity.profile.ui.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.netbeans.modules.identity.profile.api.bridgeapi.RuntimeBridge;
import org.netbeans.modules.identity.profile.api.configurator.ConfiguratorException;
import org.netbeans.modules.identity.profile.api.configurator.ServerProperties;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.web.AuthConstraint;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.wsitconf.api.WSITConfigProvider;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityCheckerRegistry;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;

/**
 * Helper class for working with J2EE projects.
 *
 * Created on July 18, 2006, 11:36 PM
 *
 * @author ptliu
 * @author Srividhya Narayanan
 */
public class J2eeProjectHelper {
    
    private static final String SERVICE_REF_NAME_PREFIX = "service/"; //NOI18N
    
    private static final String AM_DEPLOY_XML_PATH = "nbproject/am-deploy.xml"; //NOI18N
    
    private static final String AM_DEPLOY_XSL = "resources/am-deploy.xsl";  //NOI18N
    
    private static final String BUILD_SCRIPT = "/build.xml";       //NOI18N
    
    private static final String BACKUP_EXT = ".bak";        //NOI18N
    
    private static final String IMPORT_AM_DEPLOY_XML = "<import file=\"nbproject/am-deploy.xml\"/>";    //NOI18N
    
    private static final String IMPORT_TAG = "<import"; //NOI18N
    
    private static final String AM_CONSTRAINT_DISPLAY_NAME = "Access Manager Security Constraint";  //NOI18N
    
    private static final String AM_WEB_RESOURCE_NAME = "AUTHENTICATED_RESOURCE";       //NOI18N
    
    private static final String AM_URL_PATTERN = "/*";      //NOI18N
    
    private static final String AM_ROLE_NAME = "AUTHENTICATED_USERS";       //NOI18N
    
    private static final String DEFAULT_ENCODING = "UTF-8"; // NOI18N;
    
    private static final String EJB_WSDL_LOC = "META-INF"; // NOI18N;
    
    private static final String WEB_WSDL_LOC = "WEB-INF"; // NOI18N;
    
    private static final String CLIENT_WSDL_LOC = "META-INF"; // NOI18N;
    
    private static final String SUN_WEB_XML = "sun-web";        //NOI18N
    
    private static final String SUN_WEB_TEMPLATE = "Templates/Identity/SunDD/sun-web.xml";     //NOI18N
    
    private static final String SUN_EJB_JAR_TEMPLATE = "Templates/Identity/SunDD/sun-ejb-jar.xml";     //NOI18N
    
    private static final String SUN_EJB_JAR_XML = "sun-ejb-jar";    //NOI18N
    
    private static final String SUN_APPLICATION_CLIENT_TEMPLATE = "Templates/Identity/SunDD/sun-application-client.xml";    //NOI18N
    
    private static final String SUN_APPLICATION_CLIENT_XML = "sun-application-client";  //NOI18N
    
    private static final String XML_EXT = "xml";        //NOI18N
    
    private static final String WEB_FOLDER = "web";     //NOI18N
    
    private static final String WEB_INF_FOLDER = "WEB-INF";     //NOI18N
    
    private static final String CONF_FOLDER = "conf";       //NOI18N
    
    private static SecurityCheckerImpl securityChecker;
    
    static {
        securityChecker = new SecurityCheckerImpl();
        
        SecurityCheckerRegistry.getDefault().register(securityChecker);
    }
    
    public enum ProjectType {
        WEB, EJB, CLIENT, UNKNOWN
    };
    
    public enum Version {
        VERSION_1_4, VERSION_1_5, UNKNOWN
    };
    
    
    private Node node;
    private JaxWsModel model;
    private ProjectType projectType;
    private Version version;
    private File sunDD;
    private String endpointName;
    private List<String> endpointUri;
    private String portComponentName;
    private String serviceDescriptionName;
    private List<String> serviceRefNames;
    private List<WsdlData> wsdlData;
    private List<String> serviceNames;
    
    public static J2eeProjectHelper newInstance(Node node, JaxWsModel model) {
        J2eeProjectHelper helper = new J2eeProjectHelper(node, model);
        
        //if (helper.getVersion() == Version.VERSION_1_5) {
        //    return new J2ee15ProjectHelper(node, model);
        //}
        
        return helper;
    }
    
    /** Creates a new instance of J2eeProjectHelper */
    protected J2eeProjectHelper(Node node, JaxWsModel model) {
        this.node = node;
        this.model = model;
    }
    
    public boolean isSecurable() {
        if (getProject() == null || getProvider() == null)
            return false;
        
        if (isServer()) {
            if (getPortComponentName() != null &&
                    getServiceDescriptionName() != null)
                return true;
        } else {
            // We don't support 1.4 appclient.
            if (getProjectType() == ProjectType.CLIENT &&
                    getVersion() == Version.VERSION_1_4) {
                return false;
            }
            
            if (!getAllServiceRefNames().isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean noServiceRefExists() {
        if (!isServer()) {
            if (getProjectType() == ProjectType.CLIENT &&
                    getVersion() == Version.VERSION_1_4)
                return false;
            
            if (getAllServiceRefNames().isEmpty())
                return true;
        }
        
        return false;
    }
    
    public boolean isServer() {
        return (getService() != null);
    }
    
    public boolean isAMInstalled() {
        try {
            ServerProperties.getInstance(getServerID());
            return true;
        } catch (Exception ex){
            return false;
        }
    }
    
    public String getServerID() {
        return getProvider().getServerInstanceID();
    }
    
    public void refreshBuildScript() {
        String buildScript = FileUtil.toFile(getProjectDirectory()).getPath() + BUILD_SCRIPT;
        
        //System.out.println("buildScript = " + buildScript);
        
        BufferedReader reader = null;
        BufferedWriter writer = null;
        String line = null;
        boolean added = false;
        
        // First check to see if our import statement has already been added.
        try {
            reader = new BufferedReader(new FileReader(buildScript));
            
            while ((line = reader.readLine()) != null) {
                if (line.indexOf(IMPORT_AM_DEPLOY_XML) != -1) {
                    added = true;
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
        
        // If our import statement has not been added, add it now.
        if (!added) {
            try {
                // Rename the original to build.xml.bak
                File backupBuildScript = new File(buildScript);
                backupBuildScript.renameTo(new File(buildScript + BACKUP_EXT));
                
                reader = new BufferedReader(new FileReader(buildScript + BACKUP_EXT));
                writer = new BufferedWriter(new FileWriter(buildScript));
                added = false;
                int index = 0;
                
                while ((line = reader.readLine()) != null) {
                    if (!added && (index = line.indexOf(IMPORT_TAG)) != -1) {
                        StringBuffer buf = new StringBuffer(line);
                        buf = buf.replace(index, line.length(), IMPORT_AM_DEPLOY_XML);
                        writer.write(buf.toString());
                        writer.newLine();
                        added = true;
                    }
                    
                    writer.write(line);
                    writer.newLine();
                }
            } catch (FileNotFoundException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } finally {
                try {
                    if (writer != null) {
                        writer.flush();
                        writer.close();
                    }
                    
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
        
        
        // Now refresh the am-deploy.xml itself.
        GeneratedFilesHelper genFilesHelper = new GeneratedFilesHelper(getProjectDirectory());
        
        try {
            genFilesHelper.refreshBuildScript(
                    AM_DEPLOY_XML_PATH,
                    J2eeProjectHelper.class.getResource(AM_DEPLOY_XSL),
                    false);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    public ProjectType getProjectType() {
        if (projectType == null) {
            Object moduleType = getProvider().getJ2eeModule().getModuleType();
            
            if(J2eeModule.WAR.equals(moduleType)) {
                projectType = ProjectType.WEB;
            } else if (J2eeModule.EJB.equals(moduleType)) {
                projectType = ProjectType.EJB;
            } else if (J2eeModule.CLIENT.equals(moduleType)) {
                projectType = ProjectType.CLIENT;
            } else {
                projectType = ProjectType.UNKNOWN;
            }
        }
        
        return projectType;
    }
    
    public Version getVersion() {
        if (version == null) {
            String versionString = "";  //NOI18N
            
            switch (getProjectType()) {
            case WEB:
                versionString = getWebModule().getJ2eePlatformVersion();
                break;
            case EJB:
                versionString = getEjbModule().getJ2eePlatformVersion();
                break;
            case CLIENT:
                versionString = getClientModule().getJ2eePlatformVersion();
                break;
            default:
                break;
            }
            
            if (versionString.equals("1.4")) {  //NOI18N
                version = Version.VERSION_1_4;
            } else if (versionString.equals("1.5")) { //NOI18N
                version = Version.VERSION_1_5;
            } else {
                version = Version.UNKNOWN;
            }
        }
        
        return version;
    }
    
    //    protected FileObject getSunDDFO() {
    //        FileObject[] fobjs = getProvider().getConfigurationFiles();
    //
    //        if (fobjs.length > 0) {
    //            return fobjs[0];
    //        }
    //
    //        return null;
    //    }
    
    public String getEndpointName() {
        if (!isServer())
            return null;
        
        if (endpointName == null) {
            Version version = getVersion();
            
            if (version == Version.VERSION_1_4) {
                String javaClassName = getJavaSourceName();
                
                switch (getProjectType()) {
                case WEB:
                    endpointName = getEndpointNameFromWebApp(javaClassName);
                    break;
                case EJB:
                    endpointName = getEndpointNameFromEjbJar(javaClassName);
                default:
                    break;
                }
            } else if (version == Version.VERSION_1_5) {
                Service service = getService();
                if(service != null) {
                    if (service.getWsdlUrl() == null) {
                        endpointName = service.getName();
                    } else {
                        endpointName = service.getServiceName();
                    }
                }
            }
        }
        
        //System.out.println("endpointName = " + endpointName);
        return endpointName;
    }
    
    private String getEndpointNameFromWebApp(final String javaClassName) {
        MetadataModel<WebAppMetadata> model = getJ2eeModule().getMetadataModel(WebAppMetadata.class);
        String name = null;
        
        try {
            name =  model.runReadAction(new MetadataModelAction<WebAppMetadata, String> () {
                public String run(WebAppMetadata metadata) {
                    WebApp webApp = metadata.getRoot();
                    
                    for (Servlet s : webApp.getServlet()) {
                        if (s.getServletClass().equals(javaClassName)) {
                            return s.getServletName();
                        }
                    }
                    
                    return null;
                }
            });
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return name;
    }
    
    private String getEndpointNameFromEjbJar(final String javaClassName) {
        MetadataModel<EjbJarMetadata> model = getJ2eeModule().getMetadataModel(EjbJarMetadata.class);
        String name = null;
        
        try {
            name =  model.runReadAction(new MetadataModelAction<EjbJarMetadata, String> () {
                public String run(EjbJarMetadata metadata) {
                    org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = metadata.getRoot();
                    EnterpriseBeans eb = ejbJar.getEnterpriseBeans();
                    
                    if(eb != null) {
                        for(Ejb ejb : eb.getEjbs()) {
                            // Unlike servlets, the generated ejb name is
                            // the same as the web service name.
                            String ejbName = ejb.getEjbName();
                            if(ejbName != null && ejbName.length() > 0 &&
                                    ejb.getEjbClass().equals(javaClassName)) {
                                return ejbName;
                            }
                        }
                    }
                    
                    return null;
                }
            });
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return name;
    }
    
    private void getServiceInfo() {
        MetadataModel<WebservicesMetadata> webServicesModel = getJ2eeModule().getMetadataModel(WebservicesMetadata.class);
        
        try {
            webServicesModel.runReadAction(new MetadataModelAction<WebservicesMetadata,
                    String> () {
                public String run(WebservicesMetadata metadata) {
                    Webservices webServices = metadata.getRoot();
                    
                    String endpointName = getEndpointName();
                    ProjectType projectType = getProjectType();
                    
                    try {
                        for (WebserviceDescription desc : webServices.getWebserviceDescription()) {
                            for (PortComponent pc : desc.getPortComponent()) {
                                String linkName = "";       //NOI18N
                                
                                if (projectType == ProjectType.WEB) {
                                    linkName = pc.getServiceImplBean().getServletLink();
                                } else if (projectType == ProjectType.EJB) {
                                    linkName = pc.getServiceImplBean().getEjbLink();
                                }
                                
                                if (linkName.equals(endpointName)) {
                                    portComponentName = pc.getPortComponentName();
                                    serviceDescriptionName = desc.getWebserviceDescriptionName();
                                }
                            }
                        }
                    } catch (Exception excp) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                excp);
                    }
                    return portComponentName;
                }
            });
            
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    public String getPortComponentName() {
        if (!isServer()) return null;
        
        if (portComponentName == null) {
            getServiceInfo();
        }
        
        //System.out.println("portComponentName = " + portComponentName);
        return portComponentName;
    }
    
    public String getServiceDescriptionName() {
        if (serviceDescriptionName == null) {
            if (isServer()) {
                getServiceInfo();
            } else {
                serviceDescriptionName = getServiceName();
            }
        }
        
        //System.out.println("serviceDescriptionName = " + serviceDescriptionName);
        return serviceDescriptionName;
    }
    
    public List<String> getAllServiceRefNames() {
        if (isServer()) return null;
        
        if (serviceRefNames == null) {
            switch (getProjectType()) {
            case WEB:
                serviceRefNames = getAllServiceRefNamesFromWebApp();
                break;
            case CLIENT:
                serviceRefNames = getAllServiceRefNamesFromAppClient();
                break;
            case EJB:
                serviceRefNames = getAllServiceRefNamesFromEjbJar();
                break;
            }
        }
        return serviceRefNames;
    }
    
    private List<String> getAllServiceRefNamesFromWebApp() {
        MetadataModel<WebAppMetadata> model = getJ2eeModule().getMetadataModel(WebAppMetadata.class);
        List<String> refNames = null;
        
        try {
            refNames =  model.runReadAction(new MetadataModelAction<WebAppMetadata, List<String>> () {
                public List<String> run(WebAppMetadata metadata) {
                    WebApp webApp = metadata.getRoot();
                    List<String> refNames = new ArrayList<String>();
                    Version version = getVersion();
        
                    try {
                        for (ServiceRef s : webApp.getServiceRef()) {
                            if (version == Version.VERSION_1_4) {
                                if (s.getServiceRefName().equalsIgnoreCase(getServiceRefName()))
                                    refNames.add(s.getServiceRefName());
                            } else if (version == Version.VERSION_1_5) {
                                if (isThisTheServiceRef(s)) {
                                    refNames.add(s.getServiceRefName());
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                    
                    return refNames;
                }
            });
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return refNames;
    }
    
    private List<String> getAllServiceRefNamesFromAppClient() {
        MetadataModel<AppClientMetadata> model = getJ2eeModule().getMetadataModel(AppClientMetadata.class);
        List<String> refNames = null;

        try {
            refNames =  model.runReadAction(new MetadataModelAction<AppClientMetadata, List<String>> () {
                public List<String> run(AppClientMetadata metadata) {
                    AppClient appClient = metadata.getRoot();
                    List<String> refNames = new ArrayList<String>();
                    Version version = getVersion();

                    try {
                        for (ServiceRef s : appClient.getServiceRef()) {
                            if (version == Version.VERSION_1_4) {
                                if (s.getServiceRefName().equalsIgnoreCase(getServiceRefName()))
                                    refNames.add(s.getServiceRefName());
                            } else if (version == Version.VERSION_1_5) {
                                //if (isThisTheServiceRef(s))
                                refNames.add(s.getServiceRefName());
                            }
                        }
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                    
                    return refNames;
                }
            });
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return refNames;
    }
    
    private List<String> getAllServiceRefNamesFromEjbJar() {
        MetadataModel<EjbJarMetadata> model = getJ2eeModule().getMetadataModel(EjbJarMetadata.class);
        List<String> refNames = null;
        
        try {
            refNames = model.runReadAction(new MetadataModelAction<EjbJarMetadata,
                    List<String>> () {
                public List<String> run(EjbJarMetadata metadata) {
                    List<String> refNames = new ArrayList<String>();
                    org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = metadata.getRoot();
                    EnterpriseBeans eb = ejbJar.getEnterpriseBeans();
                    Version version = getVersion();

                    if(eb != null) {
                        for(Ejb ejb : eb.getEjbs()) {
                            try {
                                for (ServiceRef s : ejb.getServiceRef()) {
                                    if (isThisTheServiceRef(s))
                                        refNames.add(s.getServiceRefName());
                                }
                            } catch (Exception ex) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                            }
                        }
                    }
                    
                    return refNames;
                }
            });
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return refNames;
    }
    
    
    private boolean isThisTheServiceRef(ServiceRef s) {
        String wsdlFile = getWsdlFO().getPath();
        
        //System.out.println("wsdlfile from client node " + wsdlFile);
        //System.out.println("wsdlfo name from client node " + getWsdlFO().getName());
        //System.out.println("wsdlfo path from client node " + getWsdlFO().getPath());
        URI wsdlUri = s.getWsdlFile();
        
        //System.out.println("wsdlfile from service-ref " + wsdlUri.toString());
        //System.out.println("wsdlfile from service-ref " + wsdlUri.getPath());
        //System.out.println("wsdlfile from service-ref " + wsdlUri.getSchemeSpecificPart());
        
        String path = wsdlUri.getPath();
        int idx = 0;
  
        if (path.startsWith(EJB_WSDL_LOC)) {
            idx = EJB_WSDL_LOC.length();
        } else if (path.startsWith(WEB_WSDL_LOC)) {
            idx = WEB_WSDL_LOC.length();
        } else if (path.startsWith(CLIENT_WSDL_LOC)) {
            idx = CLIENT_WSDL_LOC.length();
        }
        
        path = path.substring(idx, path.length());
        //System.out.println("extracted path " + path);
        if (wsdlFile != null && wsdlUri != null &&
                wsdlFile.contains(path)) {
            return true;
        }
        
        return false;
    }
    
    public List<WsdlData> getWsdlData() {
        if (isServer()) return null;
        
        if (wsdlData == null) {
            wsdlData = new ArrayList<WsdlData>();
            FileObject wsdlFO = getWsdlFO();
            
            if (wsdlFO != null) {
                try {
                    List<String> svcNames = getAllServiceNames();
                    for (String s : svcNames) {
                        wsdlData.add(WsdlParser.parseWSDLFile(
                                FileUtil.toFile(wsdlFO), s));
                    }
                } catch (IOException ioex) {
                    ErrorManager.getDefault().notify(
                            ErrorManager.INFORMATIONAL, ioex);
                }
            }
        }
        
        return wsdlData;
    }
    
    protected FileObject getWsdlFO() {
        if (getVersion() == Version.VERSION_1_4) {
            
            switch (getProjectType()) {
            case WEB:
                return getWsdlFOFromWebApp(getServiceRefName());
            case CLIENT:
                return getWsdlFOFromAppClient(getServiceRefName());
            case EJB:
                // Not applicable?
                break;
            }
        } else if (getVersion() == Version.VERSION_1_5) {
            Client client = getClient();
            String wsdl = client.getLocalWsdlFile();
            Enumeration e = getProject().getProjectDirectory().getFolders(true);
            while (e.hasMoreElements()) {
                FileObject fo = (FileObject)e.nextElement();
                FileObject wsdlFo = fo.getFileObject(wsdl);
                if (wsdlFo != null)
                    return wsdlFo;
            }
        }
        
        return null;
    }
    
    private FileObject getWsdlFOFromWebApp(final String refName) {
        MetadataModel<WebAppMetadata> model = getJ2eeModule().getMetadataModel(WebAppMetadata.class);
        FileObject fobj = null;
        
        try {
            fobj =  model.runReadAction(new MetadataModelAction<WebAppMetadata, FileObject> () {
                public FileObject run(WebAppMetadata metadata) {
                    WebApp webApp = metadata.getRoot();
                    try {
                        for (ServiceRef s : webApp.getServiceRef()) {
                            if (s.getServiceRefName().equalsIgnoreCase(refName)) {
                                return getProjectDirectory().getFileObject(
                                        "web/" + s.getWsdlFile().getPath());    //NOI18N
                            }
                        }
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                    
                    return null;
                }
            });
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return fobj;
    }
    
    private FileObject getWsdlFOFromAppClient(final String refName) {
        MetadataModel<AppClientMetadata> model = getJ2eeModule().getMetadataModel(AppClientMetadata.class);
        FileObject fobj = null;
        
        try {
            fobj =  model.runReadAction(new MetadataModelAction<AppClientMetadata, FileObject> () {
                public FileObject run(AppClientMetadata metadata) {
                    AppClient appClient = metadata.getRoot();
                    try {
                        for (ServiceRef s : appClient.getServiceRef()) {
                            if (s.getServiceRefName().equalsIgnoreCase(refName)) {
                                return getProjectDirectory().getFileObject(
                                        "src/conf/" + s.getWsdlFile().getPath().substring(9));    //NOI18N
                            }
                        }
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                    
                    return null;
                }
            });
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return fobj;
    }
    
    
    public boolean isAppServerSun() {
        return RuntimeBridge.isAppServerSun(getProvider());
    }
    
    public boolean providerExists() {
        return false;
    }
    
    
    public boolean isSecurityEnabled() {
        SunDDHelper helper = new SunDDHelper(getSunDDFO(), getProjectType());
        
        if (isServer()) {
            return helper.isServiceSecurityEnabled(getServiceDescriptionName(),
                    getPortComponentName());
        } else {
            for (WsdlData wsdlData : getWsdlData()) {
                String namespace = wsdlData.getTargetNameSpace();
                String localPart = wsdlData.getPort();
                
                for (String name : getAllServiceRefNames()) {
                    if (helper.isClientSecurityEnabled(name,
                            namespace, localPart)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    protected void enableMessageLevelSecurity(String providerId) {
        SunDDHelper helper = new SunDDHelper(getSunDDFO(), getProjectType());
        
        if (isServer()) {
            helper.setServiceMessageSecurityBinding(getServiceDescriptionName(),
                    getPortComponentName(), providerId);
        } else {
            for (WsdlData wsdlData : getWsdlData()) {
                String namespace = wsdlData.getTargetNameSpace();
                String localPart = wsdlData.getPort();
                
                for (String name : getAllServiceRefNames()) {
                    helper.setServiceRefMessageSecurityBinding(name,
                            namespace, localPart);
                }
            }
        }
    }
    
    protected void disableMessageLevelSecurity() {
        SunDDHelper helper = new SunDDHelper(getSunDDFO(), getProjectType());
        
        if (isServer()) {
            helper.removeServiceMessageSecurityBinding(getServiceDescriptionName(),
                    getPortComponentName());
        } else {
            for (WsdlData wsdlData : getWsdlData()) {
                String namespace = wsdlData.getTargetNameSpace();
                String localPart = wsdlData.getPort();
                
                for (String name : getAllServiceRefNames()) {
                    helper.removeServiceRefMessageSecurityBinding(name,
                            namespace, localPart);
                }
            }
        }
    }
    
    public FileObject getSunDDFO() {
        FileObject conf = getConfRoot();
        String sunDDName = getSunDDName();
        FileObject fobj = conf.getFileObject(sunDDName, XML_EXT);
        
        if (fobj == null) {
            String template = getSunDDTemplate();
            
            try {
                fobj = createSunDDFromTemplate(template, conf, sunDDName);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return fobj;
    }
    
    private String getSunDDName() {
        switch (getProjectType()) {
        case EJB:
            return SUN_EJB_JAR_XML;
        case WEB:
            return SUN_WEB_XML;
        case CLIENT:
            return SUN_APPLICATION_CLIENT_XML;
        }
        
        return null;
    }
    
    private String getSunDDTemplate() {
        switch (getProjectType()) {
        case EJB:
            return SUN_EJB_JAR_TEMPLATE;
        case WEB:
            return SUN_WEB_TEMPLATE;
        case CLIENT:
            return SUN_APPLICATION_CLIENT_TEMPLATE;
        }
        
        return null;
    }
    
    private FileObject getConfRoot() {
        FileObject[] sourceRoots = getProvider().getSourceRoots();
        
        for (FileObject root : sourceRoots) {
            String name = root.getName();
            
            if (getProjectType() == ProjectType.WEB) {
                if (name.equals(WEB_FOLDER)) {
                    return root.getFileObject(WEB_INF_FOLDER);
                }
            } else {
                if (name.equals(CONF_FOLDER)) {      //NOI18N
                    return root;
                }
            }
        }
        return null;
    }
    
    private FileObject createSunDDFromTemplate(String template,
            FileObject folder, String sunDDName) throws IOException {
        assert template != null;
        assert folder != null;
        
        FileSystem defaultFS = Repository.getDefault().getDefaultFileSystem();
        FileObject templateFO = defaultFS.findResource(template);
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(folder);
        DataObject dataObj = templateDO.createFromTemplate(dataFolder, sunDDName);
        
        return dataObj.getPrimaryFile();
    }
    
    //    public boolean providerExists() {
    //        File sunDD = getSunDD();
    //
    //        if (isServer()) {
    //            String pcName = getPortComponentName();
    //            String descName = getServiceDescriptionName();
    //
    //            if (SunDDBridge.doesEndPointMSBExist(sunDD, descName, pcName) &&
    //                    !(SunDDBridge.isEndPointMSBAMProvider(sunDD, descName, pcName)))
    //                return true;
    //        } else {
    //            List<String> refNames = getAllServiceRefNames();
    //            String s = refNames.get(0);
    //            List<WsdlData> wsdlInfo = getWsdlData();
    //            if (!wsdlInfo.isEmpty()) {
    //                WsdlData w = wsdlInfo.get(0);
    //                String namespace = w.getTargetNameSpace();
    //                String localPart = w.getPort();
    //                if (SunDDBridge.doesSvcRefMSBExist(sunDD, s, namespace, localPart) &&
    //                        !(SunDDBridge.isSvcRefMSBAMProvider(sunDD, s, namespace, localPart)))
    //                    return true;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    public boolean isSecurityEnabled() {
    //        File sunDD = getSunDD();
    //        String pcName = getPortComponentName();
    //        String descName = getServiceDescriptionName();
    //        List<String> refNames = getAllServiceRefNames();
    //
    //        if (isServer()) {
    //            if (SunDDBridge.doesEndPointMSBExist(sunDD, descName, pcName) &&
    //                    SunDDBridge.isEndPointMSBAMProvider(sunDD, descName, pcName))
    //                return true;
    //        } else {
    //            String s = refNames.get(0);
    //            List<WsdlData> wsdlInfo = getWsdlData();
    //
    //            if (!wsdlInfo.isEmpty()) {
    //                WsdlData w = wsdlInfo.get(0);
    //                String namespace = w.getTargetNameSpace();
    //                String localPart = w.getPort();
    //
    //                if (SunDDBridge.doesSvcRefMSBExist(sunDD, s, namespace, localPart) &&
    //                        SunDDBridge.isSvcRefMSBAMProvider(sunDD, s, namespace, localPart))
    //                    return true;
    //            }
    //        }
    //        return false;
    //    }
    
    public boolean isWsitSecurityEnabled() {
        return WSITConfigProvider.getDefault().isWsitSecurityEnabled(node, model);
    }
    
    public void setTransientState(boolean isEnabled) {
        securityChecker.setTransientState(isEnabled);
    }
    
    public void clearTransientState() {
        securityChecker.clearTransientState();
    }
    
    public void enableWSPSecurity(String providerId) {
        enableMessageLevelSecurity(providerId);
    }
    
    public void disableWSPSecurity() {
        disableMessageLevelSecurity();
    }
    
    public void enableWSCSecurity(boolean isLiberty) {
        if (getProjectType().equals(ProjectType.WEB)) {
            if (isLiberty) {
                enableLiberty();
            } else {
                disableLiberty();
            }
        }
        
        enableMessageLevelSecurity(null);
    }
    
    public void disableWSCSecurity() {
        if (getProjectType().equals(ProjectType.WEB)) {
            disableLiberty();
        }
        disableMessageLevelSecurity();
    }
    
    //    protected void enableMessageLevelSecurity(String providerId) {
    //        File sunDD = getSunDD();
    //        String pcName = getPortComponentName();
    //        String descName = getServiceDescriptionName();
    //        List<String> refNames = getAllServiceRefNames();
    //
    //        if (isServer()) {
    //            //            System.out.println("descname: portcompname: " + descName +
    //            //                    " : " + pcName);
    //            if (!SunDDBridge.setEndPointMSB(sunDD, descName, pcName, providerId)){
    //                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
    //                        new Exception("Failed during SunDD changes")); // NOI18N
    //            }
    //        } else {
    //            List<WsdlData> wsdlInfo = getWsdlData();
    //            int i = 0;
    //            assert(wsdlInfo.size() >= refNames.size());
    //            for (String s : refNames) {
    //                if (wsdlInfo.get(i) != null) {
    //                    String namespace = wsdlInfo.get(i).getTargetNameSpace();
    //                    String localPart = wsdlInfo.get(i).getPort();
    //                    //                    System.out.println("refName : namespace: localpart: " + s +
    //                    //                            " : " + namespace + " : " + localPart);
    //                    if (!SunDDBridge.setSvceRefMSB(sunDD, s, namespace, localPart)) {
    //                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
    //                                new Exception("Failed during SunDD changes")); // NOI18N
    //                    }
    //                }
    //                i++;
    //            }
    //        }
    //    }
    
    //    protected void disableMessageLevelSecurity() {
    //        //if (!isSecurityEnabled()) return;
    //
    //        File sunDD = getSunDD();
    //        String pcName = getPortComponentName();
    //        String descName = getServiceDescriptionName();
    //        List<String> refNames = getAllServiceRefNames();
    //
    //        if (isServer()) {
    //            //            System.out.println("Into delete -- descname: portcompname: " + descName +
    //            //                    " : " + pcName);
    //            if (!SunDDBridge.deleteEndPointMSB(sunDD, descName, pcName)){
    //                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
    //                        new Exception("Failed during SunDD changes")); // NOI18N
    //            }
    //        } else {
    //            List<WsdlData> wsdlInfo = getWsdlData();
    //            int i = 0;
    //            assert(wsdlInfo.size() >= refNames.size());
    //            for (String s : refNames) {
    //                if (wsdlInfo.get(i) != null) {
    //                    String namespace = wsdlInfo.get(i).getTargetNameSpace();
    //                    String localPart = wsdlInfo.get(i).getPort();
    //                    //                    System.out.println("Into delete -- refName : namespace: localpart: " + s +
    //                    //                            " : " + namespace + " : " + localPart);
    //                    if (!SunDDBridge.deleteSvcRefMSB(sunDD, s, namespace, localPart)) {
    //                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
    //                                new Exception("Failed during SunDD changes")); // NOI18N
    //                    }
    //                }
    //                i++;
    //            }
    //        }
    //    }
    
    private void enableLiberty() {
        addAMSecurityConstraint();
        SunDDHelper helper = new SunDDHelper(getSunDDFO(), getProjectType());
        helper.addSecurityRoleMapping();
    }
    
    private void disableLiberty() {
        removeAMSecurityConstraint();
        SunDDHelper helper = new SunDDHelper(getSunDDFO(), getProjectType());
        helper.removeSecurityRoleMapping();
    }
    
    private void addAMSecurityConstraint() {
        boolean isModified = false;
        WebApp webApp = getWebApp();
        SecurityRole amRole = getAMSecurityRole(webApp);
        
        try {
            if (amRole == null) {
                amRole = (SecurityRole) webApp.createBean("SecurityRole"); //NOI18N
                amRole.setRoleName(AM_ROLE_NAME);
                webApp.addSecurityRole(amRole);
                isModified = true;
            }
        } catch (ClassNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        SecurityConstraint amConstraint = getAMSecurityConstraint(webApp);
        
        try {
            if (amConstraint == null) {
                amConstraint =
                        (SecurityConstraint) webApp.createBean("SecurityConstraint"); //NOI18n
                amConstraint.setDisplayName(AM_CONSTRAINT_DISPLAY_NAME);
                webApp.addSecurityConstraint(amConstraint);
                
                WebResourceCollection wrc =
                        (WebResourceCollection) amConstraint.createBean("WebResourceCollection");   //NOI18N
                wrc.setWebResourceName(AM_WEB_RESOURCE_NAME);
                wrc.setUrlPattern(new String[] {AM_URL_PATTERN});
                amConstraint.setWebResourceCollection(new WebResourceCollection[] {wrc});
                
                AuthConstraint authConstraint =
                        (AuthConstraint) amConstraint.createBean("AuthConstraint"); //NOI18N
                authConstraint.setRoleName(new String[] {AM_ROLE_NAME});    //NOI18N
                amConstraint.setAuthConstraint(authConstraint);
                isModified = true;
            }
        } catch (ClassNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        if (isModified) saveDD(webApp);
    }
    
    private void removeAMSecurityConstraint() {
        boolean isModified = false;
        WebApp webApp = getWebApp();
        SecurityRole amRole = getAMSecurityRole(webApp);
        
        if (amRole != null) {
            webApp.removeSecurityRole(amRole);
            isModified = true;
        }
        
        SecurityConstraint amConstraint = getAMSecurityConstraint(webApp);
        
        if (amConstraint != null) {
            webApp.removeSecurityConstraint(amConstraint);
            isModified = true;
        }
        
        if (isModified) saveDD(webApp);
    }
    
    private SecurityRole getAMSecurityRole(WebApp webApp) {
        SecurityRole[] roles = webApp.getSecurityRole();
        
        for (SecurityRole role : roles) {
            if (role.getRoleName().equals(AM_ROLE_NAME)) {
                return role;
            }
        }
        
        return null;
    }
    
    private SecurityConstraint getAMSecurityConstraint(WebApp webApp) {
        SecurityConstraint[] constraints = webApp.getSecurityConstraint();
        
        for (SecurityConstraint constraint : constraints) {
            if (constraint.getDefaultDisplayName().equals(AM_CONSTRAINT_DISPLAY_NAME)) {
                return constraint;
            }
        }
        
        return null;
    }
    
    private void saveDD(RootInterface model) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            model.write(out);
            out.close();
            
            FileObject webDD = getWebModule().getDeploymentDescriptor();
            FileLock lock = webDD.lock();
            Writer writer = new OutputStreamWriter(webDD.getOutputStream(lock));
            writer.write(out.toString(DEFAULT_ENCODING));
            writer.flush();
            writer.close();
            lock.releaseLock();
        } catch (IOException e) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
        } catch (IllegalStateException e) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
        }
    }
    
    public String getConfigPath() {
        return FileUtil.toFile(getProjectDirectory()).getPath() + File.separator +
                "src" + File.separator + "conf";    //NOI18N
    }
    
    private String getServiceRefName() {
        return SERVICE_REF_NAME_PREFIX + getServiceName();     //NOI18N
    }
    
    public List<String> getAllServiceNames() {
        if (serviceNames == null) {
            serviceNames = new ArrayList<String>();
            
            FileObject wsdlFO = getWsdlFO();
            
            if (wsdlFO != null) {
                try {
                    serviceNames = WsdlParser.getWsdlSvcNames(
                            FileUtil.toFile(wsdlFO));
                } catch (IOException ioex) {
                    ErrorManager.getDefault().notify(
                            ErrorManager.INFORMATIONAL, ioex);
                }
            }
        }
        
        return serviceNames;
    }
    
    public List<String> getEndpointURI() {
        if (endpointUri == null) {
            endpointUri = new ArrayList<String>();
            
            if (isServer()) {
                endpointUri.add(getServiceDescriptionName());
            } else {
                endpointUri = getAllServiceNames();
            }
        }
        return endpointUri;
    }
    
    private String getServiceName() {
        if (isServer()) {
            //TODO: Need to implement
            return null;
        } else {
            // Currently just using node's name to get the info.
            // Need a proper API for that.
            return node.getName();
        }
    }
    
    protected J2eeModuleProvider getProvider() {
        Project project = getProject();
        
        if (project != null)
            return (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        
        return null;
    }
    
    protected Project getProject() {
        return FileOwnerQuery.getOwner(getFileObject());
    }
    
    protected FileObject getProjectDirectory() {
        return getProject().getProjectDirectory();
    }
    
    private FileObject getFileObject() {
        FileObject source = getJavaSource();
        
        if (source != null) return source;
        
        FileObject fo = (FileObject)node.getLookup().lookup(FileObject.class);
        
        if (fo != null) return fo;
        
        DataObject dobj = (DataObject)node.getLookup().lookup(DataObject.class);
        
        return dobj.getPrimaryFile();
    }
    
    protected FileObject getJavaSource() {
        return (FileObject)node.getLookup().lookup(FileObject.class);
    }
    
    protected String getJavaSourceName() {
        return getService().getImplementationClass();
    }
    
    protected Client getClient() {
        return (Client) node.getLookup().lookup(Client.class);
    }
    
    protected Service getService() {
        return (Service) node.getLookup().lookup(Service.class);
    }
    
    protected WebModule getWebModule() {
        return WebModule.getWebModule(getProjectDirectory());
    }
    
    protected EjbJar getEjbModule() {
        return EjbJar.getEjbJar(getProjectDirectory());
    }
    
    protected Car getClientModule() {
        return Car.getCar(getProjectDirectory());
    }
    
    protected J2eeModule getJ2eeModule() {
        return getProvider().getJ2eeModule();
    }
    
    protected WebApp getWebApp() {
        FileObject conf = getConfRoot();
        FileObject fobj = conf.getFileObject("web", XML_EXT);
 
        try {
            WebApp webApp = DDProvider.getDefault().getDDRoot(fobj);
     
            return webApp;
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
        
        return null;
    }
}

