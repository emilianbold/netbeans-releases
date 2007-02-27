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
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.identity.profile.api.bridgeapi.SunDDBridge;
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
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.web.AuthConstraint;
import org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint;
import org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
//import org.netbeans.modules.websvc.wsitconf.api.WSITConfigProvider;
//import org.netbeans.modules.websvc.wsitconf.spi.SecurityCheckerRegistry;
import org.openide.filesystems.FileLock;
import org.openide.loaders.DataNode;
import org.openide.windows.TopComponent;

/**
 * Helper class for working with J2EE projects.
 *
 * Created on July 18, 2006, 11:36 PM
 *
 * @author ptliu
 * @author Srividhya Narayanan
 */
public class J2eeProjectHelper {
    
    private static final String SUN_WEB_XML = "sun-web.xml";        //NOI18N
    
    private static final String WEB_CONFIG_FILE_NAME = "WEB-INF/" + SUN_WEB_XML; //NOI18N
    
    private static final String EJB_CONFIG_FILE_NAME = "sun-ejb-jar.xml"; //NOI18N
    
    private static final String CLIENT_CONFIG_FILE_NAME = "sun-application-client.xml"; //NOI18N
    
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
    
    private static SecurityCheckerImpl securityChecker;
    
    static {
        securityChecker = new SecurityCheckerImpl();
        
        // TODO - uncomment this after WSIT is merged into trunk
        //SecurityCheckerRegistry.getDefault().register(securityChecker);
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
   
    /** Creates a new instance of J2eeProjectHelper */
    public J2eeProjectHelper(Node node, JaxWsModel model) {
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
    
    public boolean isServer() {
        // this is a server for 1.4 and 1.5 projects
        if (getJavaSource() != null) return true;
        
        //For 1.5 project client this should be not null.
        if (getClient() != null) return false;
        
        // Shouldn't this return true?
        return false;
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
    
    public File getSunDD() {
        if (sunDD == null) {
            switch (getProjectType()) {
                case WEB:
                    sunDD = getProvider().getDeploymentConfigurationFile(
                            WEB_CONFIG_FILE_NAME);
                    break;
                case EJB:
                    sunDD = getProvider().getDeploymentConfigurationFile(
                            EJB_CONFIG_FILE_NAME);
                    break;
                case CLIENT:
                    sunDD = getProvider().getDeploymentConfigurationFile(
                            CLIENT_CONFIG_FILE_NAME);
                    break;
                default:
                    break;
            }
        }
        
        return sunDD;
    }
    
    private FileObject getSunDDFO() {
        switch (getProjectType()) {
            case WEB:
                FileObject[] fos = getProvider().getConfigurationFiles();
                if (fos.length > 0)
                    return getProvider().getConfigurationFiles()[0];
        }
        
        return null;
    }
    
    public String getEndpointName() {
        if (!isServer())
            return null;
        
        if (endpointName == null) {
            Version version = getVersion();
            
            if (version == Version.VERSION_1_4) {
                String javaClassName = getJavaSourceName();
                
                switch (getProjectType()) {
                    case WEB:
                        for (Servlet s : getWebApp().getServlet()) {
                            if (s.getServletClass().equals(javaClassName)) {
                                endpointName = s.getServletName();
                                break;
                            }
                        }
                        break;
                    case EJB:
                        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = getEjbJar();
                        EnterpriseBeans eb = ejbJar.getEnterpriseBeans();
                        
                        if(eb != null) {
                            for(Ejb ejb : eb.getEjbs()) {
                                // Unlike servlets, the generated ejb name is
                                // the same as the web service name.
                                String ejbName = ejb.getEjbName();
                                if(ejbName != null && ejbName.length() > 0 &&
                                        ejb.getEjbClass().equals(javaClassName)) {
                                    endpointName = ejbName;
                                    break;
                                }
                            }
                        }
                        
                        break;
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
        
        return endpointName;
    }
    
    private void getServiceInfo() {
        String endpointName = getEndpointName();
        Webservices webServices = this.getWebServicesXML();
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
                        return;
                    }
                }
            }
        } catch (Exception excp) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    excp);
        }
        
    }
    
    public String getPortComponentName() {
        if (!isServer()) return null;
        
        if (portComponentName == null) {
            getServiceInfo();
        }
        
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
        
        return serviceDescriptionName;
    }
    
    public List<String> getAllServiceRefNames() {
        if (isServer()) return null;
        
        if (serviceRefNames == null) {
            serviceRefNames = new ArrayList<String>();
            Version version = getVersion();
            
            switch (getProjectType()) {
                case WEB:
                    try {
                        for (ServiceRef s : getWebApp().getServiceRef()) {
                            if (version == Version.VERSION_1_4) {
                                if (s.getServiceRefName().equalsIgnoreCase(getServiceRefName()))
                                    serviceRefNames.add(s.getServiceRefName());
                            } else if (version == Version.VERSION_1_5) {
                                if (isThisTheServiceRef(s))
                                    serviceRefNames.add(s.getServiceRefName());
                            }
                        }
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                    break;
                case CLIENT:
                    try {
                        for (ServiceRef s : getAppClient().getServiceRef()) {
                            if (version == Version.VERSION_1_4) {
                                if (s.getServiceRefName().equalsIgnoreCase(getServiceRefName()))
                                    serviceRefNames.add(s.getServiceRefName());
                            } else if (version == Version.VERSION_1_5) {
                                //if (isThisTheServiceRef(s))
                                serviceRefNames.add(s.getServiceRefName());
                            }
                        }
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                    break;
                case EJB:
                    try {
                        if (version == Version.VERSION_1_5) {
                            org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = getEjbJar();
                            EnterpriseBeans eb = ejbJar.getEnterpriseBeans();
                            
                            if(eb != null) {
                                for(Ejb ejb : eb.getEjbs()) {
                                    for (ServiceRef s : ejb.getServiceRef()) {
                                        if (isThisTheServiceRef(s))
                                            serviceRefNames.add(s.getServiceRefName());
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                    break;
            }
        }
        return serviceRefNames;
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
    
    private FileObject getWsdlFO() {
        if (version == Version.VERSION_1_4) {
            String refName = null;
            
            switch (getProjectType()) {
                case WEB:
                    refName = getServiceRefName();
                    
                    try {
                        for (ServiceRef s : getWebApp().getServiceRef()) {
                            if (s.getServiceRefName().equalsIgnoreCase(refName)) {
                                return getProjectDirectory().getFileObject(
                                        "web/" + s.getWsdlFile().getPath());    //NOI18N
                            }
                        }
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                    break;
                case CLIENT:
                    refName = getServiceRefName();
                    
                    try {
                        for (ServiceRef s : getAppClient().getServiceRef()) {
                            if (s.getServiceRefName().equalsIgnoreCase(refName)) {
                                return getProjectDirectory().getFileObject(
                                        "src/conf/" + s.getWsdlFile().getPath().substring(9));    //NOI18N
                            }
                        }
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                    break;
                case EJB:
                    // Not applicable?
                    break;
            }
        } else if (version == Version.VERSION_1_5) {
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
    
    public boolean isAppServerSun() {
        return RuntimeBridge.isAppServerSun(getProvider());
    }
    
    public boolean providerExists() {
        File sunDD = getSunDD();
        String pcName = getPortComponentName();
        String descName = getServiceDescriptionName();
        List<String> refNames = getAllServiceRefNames();
        
        if (isServer()) {
            if (SunDDBridge.doesEndPointMSBExist(sunDD, descName, pcName) &&
                    !(SunDDBridge.isEndPointMSBAMProvider(sunDD, descName, pcName)))
                return true;
        } else {
            String s = refNames.get(0);
            List<WsdlData> wsdlInfo = getWsdlData();
            if (!wsdlInfo.isEmpty()) {
                WsdlData w = wsdlInfo.get(0);
                String namespace = w.getTargetNameSpace();
                String localPart = w.getPort();
                if (SunDDBridge.doesSvcRefMSBExist(sunDD, s, namespace, localPart) &&
                        !(SunDDBridge.isSvcRefMSBAMProvider(sunDD, s, namespace, localPart)))
                    return true;
            }
        }
        return false;
    }
    
    public boolean isSecurityEnabled() {
        File sunDD = getSunDD();
        String pcName = getPortComponentName();
        String descName = getServiceDescriptionName();
        List<String> refNames = getAllServiceRefNames();
        
        if (isServer()) {
            if (SunDDBridge.doesEndPointMSBExist(sunDD, descName, pcName) &&
                    SunDDBridge.isEndPointMSBAMProvider(sunDD, descName, pcName))
                return true;
        } else {
            String s = refNames.get(0);
            List<WsdlData> wsdlInfo = getWsdlData();
            
            if (!wsdlInfo.isEmpty()) {
                WsdlData w = wsdlInfo.get(0);
                String namespace = w.getTargetNameSpace();
                String localPart = w.getPort();
                
                if (SunDDBridge.doesSvcRefMSBExist(sunDD, s, namespace, localPart) &&
                        SunDDBridge.isSvcRefMSBAMProvider(sunDD, s, namespace, localPart))
                    return true;
            }
        }
        return false;
    }
    
    public boolean isWsitSecurityEnabled() {
        // TODO - uncomment this after WSIT is merged into trunk
        //return WSITConfigProvider.getDefault().isWsitSecurityEnabled(node, model);
        return false;
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
    
    private void enableMessageLevelSecurity(String providerId) {
        File sunDD = getSunDD();
        String pcName = getPortComponentName();
        String descName = getServiceDescriptionName();
        List<String> refNames = getAllServiceRefNames();
        
        if (isServer()) {
//            System.out.println("descname: portcompname: " + descName +
//                    " : " + pcName);
            if (!SunDDBridge.setEndPointMSB(sunDD, descName, pcName, providerId)){
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new Exception("Failed during SunDD changes")); // NOI18N
            }
        } else {
            List<WsdlData> wsdlInfo = getWsdlData();
            int i = 0;
            assert(wsdlInfo.size() >= refNames.size());
            for (String s : refNames) {
                if (wsdlInfo.get(i) != null) {
                    String namespace = wsdlInfo.get(i).getTargetNameSpace();
                    String localPart = wsdlInfo.get(i).getPort();
//                    System.out.println("refName : namespace: localpart: " + s +
//                            " : " + namespace + " : " + localPart);
                    if (!SunDDBridge.setSvceRefMSB(sunDD, s, namespace, localPart)) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                new Exception("Failed during SunDD changes")); // NOI18N
                    }
                }
                i++;
            }
        }
    }
    
    private void disableMessageLevelSecurity() {
        //if (!isSecurityEnabled()) return;
        
        File sunDD = getSunDD();
        String pcName = getPortComponentName();
        String descName = getServiceDescriptionName();
        List<String> refNames = getAllServiceRefNames();
        
        if (isServer()) {
//            System.out.println("Into delete -- descname: portcompname: " + descName +
//                    " : " + pcName);
            if (!SunDDBridge.deleteEndPointMSB(sunDD, descName, pcName)){
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new Exception("Failed during SunDD changes")); // NOI18N
            }
        } else {
            List<WsdlData> wsdlInfo = getWsdlData();
            int i = 0;
            assert(wsdlInfo.size() >= refNames.size());
            for (String s : refNames) {
                if (wsdlInfo.get(i) != null) {
                    String namespace = wsdlInfo.get(i).getTargetNameSpace();
                    String localPart = wsdlInfo.get(i).getPort();
//                    System.out.println("Into delete -- refName : namespace: localpart: " + s +
//                            " : " + namespace + " : " + localPart);
                    if (!SunDDBridge.deleteSvcRefMSB(sunDD, s, namespace, localPart)) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                new Exception("Failed during SunDD changes")); // NOI18N
                    }
                }
                i++;
            }
        }
    }
    
    private void enableLiberty() {
        addAMSecurityConstraint();
        SunDDHelper helper = new SunDDHelper(getSunDDFO());
        helper.addSecurityRoleMapping();
    }
    
    private void disableLiberty() {
        removeAMSecurityConstraint();
        SunDDHelper helper = new SunDDHelper(getSunDDFO());
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
    
    public String getDefaultKeystore() {
        String loc = getProvider().getInstanceProperties().getProperty(
                InstanceProperties.URL_ATTR);
        int startIndx = loc.indexOf('[');
        int endIndx = loc.indexOf(']');
        loc = loc.substring(startIndx+1, endIndx) +
                "/domains/domain1/config/keystore.jks";  //NOI18N
        return normalizeFilePath(loc);
    }
    
    public String getDefaultKeystorePassword() {
        return "adminadmin";        //NOI18N
    }
    
    public String getDefaultKeyAlias() {
        return "s1as";      //NOI18N
    }
    
    private String normalizeFilePath(String path) {
        char o = '\\';
        char n = '/';
        return path.replace(o, n);
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
    
    public boolean closeSunDDEditor() {
        TopComponent.Registry registry = TopComponent.getRegistry();
        Set openedComponents = registry.getOpened();
        Iterator iter = openedComponents.iterator();
        FileObject sunDDFO = getSunDDFO();
        TopComponent sunDDEditor = null;
        
        while (iter.hasNext()) {
            TopComponent component = (TopComponent) iter.next();
            
            // guard against null component and component name
            if (component == null) continue;
            
            String name = component.getName();
            if (name != null && name.equals(SUN_WEB_XML)) {
                Node node  = component.getActivatedNodes()[0];
                DataObject dObj = null;
                
                if (node instanceof DataNode) {
                    dObj = ((DataNode) node).getDataObject();
                } else {
                    try {
                        // This is a hack to get the dataobject associated with
                        // the sun-web config editor.
                        Method method = component.getClass().getMethod("getConfigDataObject"); //NOI18N
                        dObj = (DataObject) method.invoke(component);
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
                
                if (dObj != null && dObj.getPrimaryFile() == sunDDFO) {
                    sunDDEditor = component;
                    break;
                }
            }
        }
        
        if (sunDDEditor != null) {
            return sunDDEditor.close();
        }
        
        return true;
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
    
    private J2eeModuleProvider getProvider() {
        Project project = getProject();
        
        if (project != null)
            return (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        
        return null;
    }
    
    private Project getProject() {
        return FileOwnerQuery.getOwner(getFileObject());
    }
    
    private FileObject getProjectDirectory() {
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
    
     private FileObject getJavaSource() {
        String implBean = getService().getImplementationClass();
        System.out.println("javaSource = " + implBean);
        if(implBean != null) {
            FileObject srcRoot = (FileObject)node.getLookup().lookup(FileObject.class);
            return srcRoot.getFileObject(implBean.replace('.','/')+".java");
        }
        return null;
    }

    private String getJavaSourceName() {
        return getService().getImplementationClass();
    }
    
    private Client getClient() {
        return (Client) node.getLookup().lookup(Client.class);
    }
    
    private Service getService() {
        return (Service) node.getLookup().lookup(Service.class);
    }
    
    private WebModule getWebModule() {
        return WebModule.getWebModule(getProjectDirectory());
    }
    
    private EjbJar getEjbModule() {
        return EjbJar.getEjbJar(getProjectDirectory());
    }
    
    private Car getClientModule() {
        return Car.getCar(getProjectDirectory());
    }
    
    private WebApp getWebApp() {
        //System.out.println(getProvider().getJ2eeModule().getDeploymentDescriptor(
        //        J2eeModule.WEB_XML).dumpBeanNode());
        return (WebApp)getJ2eeModule().getDeploymentDescriptor(
                J2eeModule.WEB_XML);
    }
    
    private AppClient getAppClient() {
        return (AppClient)getJ2eeModule().getDeploymentDescriptor(
                J2eeModule.CLIENT_XML);
    }
    
    private org.netbeans.modules.j2ee.dd.api.ejb.EjbJar getEjbJar() {
        //System.out.println(getProvider().getJ2eeModule().getDeploymentDescriptor(
        //        J2eeModule.EJBJAR_XML).dumpBeanNode());
        return (org.netbeans.modules.j2ee.dd.api.ejb.EjbJar)
        getJ2eeModule().getDeploymentDescriptor(
                J2eeModule.EJBJAR_XML);
    }
    
    private org.netbeans.modules.j2ee.dd.api.webservices.Webservices getWebServicesXML() {
        switch (getProjectType()) {
            case WEB:
                //System.out.println(getProvider().getJ2eeModule().getDeploymentDescriptor(
                //        J2eeModule.WEBSERVICES_XML).dumpBeanNode());
                return (org.netbeans.modules.j2ee.dd.api.webservices.Webservices)
                getJ2eeModule().getDeploymentDescriptor(
                        J2eeModule.WEBSERVICES_XML);
            case EJB:
                //System.out.println("dumping the bean node for webservices xml file ");
                //getProvider().getJ2eeModule().getDeploymentDescriptor(
                //        J2eeModule.EJBSERVICES_XML).dumpXml();
                return (org.netbeans.modules.j2ee.dd.api.webservices.Webservices)
                getJ2eeModule().getDeploymentDescriptor(
                        J2eeModule.EJBSERVICES_XML);
        }
        return null;
    }
    
    private J2eeModule getJ2eeModule() {
        return getProvider().getJ2eeModule();
    }
}

