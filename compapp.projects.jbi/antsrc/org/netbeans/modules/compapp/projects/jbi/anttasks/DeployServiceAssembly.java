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

package org.netbeans.modules.compapp.projects.jbi.anttasks;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.netbeans.modules.j2ee.deployment.impl.ServerString;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerTarget;
import org.netbeans.modules.sun.manager.jbi.management.AdministrationService;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIServiceAssemblyDocument;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIServiceAssemblyStatus;
import org.netbeans.modules.sun.manager.jbi.util.ServerInstance;

/**
 * Ant task to deploy/undeploy Service Assembly to/from the target JBI server.
 *
 */
public class DeployServiceAssembly extends Task {
    /**
     * DOCUMENT ME!
     */
    private String serviceAssemblyID;
    
    /**
     * DOCUMENT ME!
     */
    private String serviceAssemblyLocation;
    
    private String undeployServiceAssembly = "false";
    
    private String hostName;
    
    private String port;
    
    private String userName;
    
    private String password;
    
    // REMOVE ME
    private String serverInstanceLocation;
    
    private String netBeansUserDir;
    
    // If not defined, then the first server instance in the setting file
    // will be used.
    // REMOVE ME
    private String j2eeServerInstance;
    
    private boolean FORCE = true; //??
    
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the serviceAssemblyID.
     */
    public String getServiceAssemblyID() {
        return this.serviceAssemblyID;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param serviceAssemblyID
     *            The ServiceAssembly ID to set.
     */
    public void setServiceAssemblyID(String serviceAssemblyID) {
        this.serviceAssemblyID = serviceAssemblyID;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the serviceAssemblyID.
     */
    public String getServiceAssemblyLocation() {
        return this.serviceAssemblyLocation;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param serviceAssemblyLocation
     *            The ServiceAssembly location to set.
     */
    public void setServiceAssemblyLocation(String serviceAssemblyLocation) {
        this.serviceAssemblyLocation = serviceAssemblyLocation;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return Returns the undeployServiceAssembly.
     */
    public String getUndeployServiceAssembly() {
        return this.undeployServiceAssembly;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param undeployServiceAssembly
     *            The undeployServiceAssembly command.
     */
    public void setUndeployServiceAssembly(String undeployServiceAssembly) {
        this.undeployServiceAssembly = undeployServiceAssembly;
    }
    
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    
    public String getHostName() {
        return hostName;
    }
    
    public void setPort(String port) {
        this.port = port;
    }
    
    public String getPort() {
        return port;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setServerInstanceLocation(String serverInstanceLocation) {
        this.serverInstanceLocation = serverInstanceLocation;
    }
    
    public String getServerInstanceLocation() {
        return serverInstanceLocation;
    }
    
    public void setNetBeansUserDir(String netBeansUserDir) {
        this.netBeansUserDir = netBeansUserDir;
    }
    
    public String getNetBeansUserDir() {
        return netBeansUserDir;
    }
    
    public void setJ2eeServerInstance(String j2eeServerInstance) {
        this.j2eeServerInstance = j2eeServerInstance;
    }
    
    public String getJ2eeServerInstance() {
        return j2eeServerInstance;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @throws BuildException
     *             DOCUMENT ME!
     */
    public void execute() throws BuildException {
        if (serviceAssemblyID != null && 
                serviceAssemblyID.equals("${org.netbeans.modules.compapp.projects.jbi.descriptor.uuid.assembly-unit}")) {
            String msg = "Unknown Service Assembly ID: " + serviceAssemblyID + 
                    System.getProperty("line.separator") +
                    "Please re-open your CompApp project using the latest NetBeans to refresh your CompApp project." +
                    System.getProperty("line.separator") +
                    "See http://www.netbeans.org/issues/show_bug.cgi?id=108702 for more info."; 
            throw new BuildException(msg);
        }
                
        String nbUserDir = getNetBeansUserDir();
        String serverInstanceID = getJ2eeServerInstance();
                
        startServer(serverInstanceID);
        
        ServerInstance serverInstance = AdminServiceHelper.getServerInstance(
                nbUserDir, serverInstanceID);
        AdministrationService adminService = null;
        try {
            adminService = new AdministrationService(serverInstance);
        } catch (Exception e) {
            throw new BuildException(e.getMessage());
        }                
        
        hostName = serverInstance.getHostName();
        port = serverInstance.getAdminPort();
        userName = serverInstance.getUserName();
        password = serverInstance.getPassword();
        
        JBIServiceAssemblyStatus assembly = getJBIServiceAssemblyStatus(
                adminService, serviceAssemblyID);
        
        String status = assembly == null ? null : assembly.getStatus();
        // System.out.println("Current assembly status is " + status);
        
        if (JBIComponentStatus.UNKNOWN_STATE.equals(status)) {
            String msg = "Unknown status for Service Assembly "
                    + serviceAssemblyID;
            throw new BuildException(msg);
        }
        
        boolean success;
        if (undeployServiceAssembly.equalsIgnoreCase("true")) { // NOI18N
            if (JBIComponentStatus.STARTED_STATE.equals(status)) {
                success = stopServiceAssembly(adminService)
                && shutdownServiceAssembly(adminService)
                && undeployServiceAssembly(adminService);
            } else if (JBIComponentStatus.STOPPED_STATE.equals(status)) {
                success = shutdownServiceAssembly(adminService)
                && undeployServiceAssembly(adminService);
            } else if (JBIComponentStatus.SHUTDOWN_STATE.equals(status)) {
                success = undeployServiceAssembly(adminService);
            } else {
                success = true;
            }
        } else { // deploy action...
            if (JBIComponentStatus.STARTED_STATE.equals(status)) {
                success = stopServiceAssembly(adminService)
                && shutdownServiceAssembly(adminService)
                && undeployServiceAssembly(adminService)
                && deployServiceAssembly(adminService)
                && startServiceAssembly(adminService);
            } else if (JBIComponentStatus.STOPPED_STATE.equals(status)) {
                success = shutdownServiceAssembly(adminService)
                && undeployServiceAssembly(adminService)
                && deployServiceAssembly(adminService)
                && startServiceAssembly(adminService);
            } else if (JBIComponentStatus.SHUTDOWN_STATE.equals(status)) {
                success = undeployServiceAssembly(adminService)
                && deployServiceAssembly(adminService)
                && startServiceAssembly(adminService);
            } else {
                success = deployServiceAssembly(adminService)
                && startServiceAssembly(adminService);
            }
        }
        
        if (!success) {
            throw new BuildException("Service assembly deployment failed.");
        }
    }
      
    private void startServer(String serverInstanceID) {
        org.netbeans.modules.j2ee.deployment.impl.ServerInstance inst = null;
        
        try {
             inst = ServerRegistry.getInstance().getServerInstance(serverInstanceID);
        } catch (Exception e) {
            // NPE from command line deployment.
            // Fine. Not supporting auto server start if deployed from command line.
            return;
        }
        
        if (inst == null) {
            log("Bad target server ID: " + serverInstanceID);
            return;
        }
        
        ServerString server = new ServerString(inst);
        
        org.netbeans.modules.j2ee.deployment.impl.ServerInstance serverInstance = 
                server.getServerInstance();
        if (server == null || serverInstance == null) {
            log("Make sure a target server is set in project properties.");
        }
        
        // Currently it is not possible to select target to which modules will
        // be deployed. Lets use the first one.
        // (This will start the server if the server is not running.)
        ServerTarget targets[] = serverInstance.getTargets();
    }
    
    /**
     * Retrieves the status of the given Service Assembly deployed on the JBI
     * Container on the Server.
     *
     * @param assemblyName
     *            name of a Service Assembly
     * @return JBI ServiceAssembly Status
     */
    private JBIServiceAssemblyStatus getJBIServiceAssemblyStatus(
            AdministrationService adminService, String assemblyName) {
        
        if (adminService != null) {
            List<JBIServiceAssemblyStatus> assemblyList = 
                    adminService.getServiceAssemblyStatusList();
            for (JBIServiceAssemblyStatus assembly : assemblyList) {
                if (assembly.getServiceAssemblyName().equals(assemblyName)) {
                    return assembly;
                }
            }
        }
        
        return null;
    }
    
    private boolean deployServiceAssembly(AdministrationService adminService) {
        log("[deploy-service-assembly]");
        log("    Deploying a service assembly...");
        log("        host=" + hostName);
        log("        port=" + port);
        log("        file=" + serviceAssemblyLocation);
        
        String result = adminService
                .deployServiceAssembly(serviceAssemblyLocation);
        Object[] value = JBIMBeanTaskResultHandler.getProcessedResult("Deploy", result);
        if (value[0] != null) {
            log((String) value[0]);
        }
        
        return ((Boolean)value[1]).booleanValue();
    }
    
    private boolean startServiceAssembly(AdministrationService adminService) {
        log("[start-service-assembly]");
        log("    Starting a service assembly...");
        log("        host=" + hostName);
        log("        port=" + port);
        log("        name=" + serviceAssemblyID);
        
        String result = adminService.startServiceAssembly(serviceAssemblyID);
        Object[] value = JBIMBeanTaskResultHandler.getProcessedResult("Start", result);
        if (value[0] != null) {
            log((String) value[0]);
        }
        
        return ((Boolean)value[1]).booleanValue();
    }
    
    private boolean stopServiceAssembly(AdministrationService adminService) {
        log("[stop-service-assembly]");
        log("    Stopping a service assembly...");
        log("        host=" + hostName);
        log("        port=" + port);
        log("        name=" + serviceAssemblyID);
        
        String result = adminService.stopServiceAssembly(serviceAssemblyID);
        Object[] value = JBIMBeanTaskResultHandler.getProcessedResult("Stop", result);
        if (value[0] != null) {
            log((String) value[0]);
        }
        
        return ((Boolean)value[1]).booleanValue();
    }
    
    private boolean shutdownServiceAssembly(AdministrationService adminService) {
        log("[shutdown-service-assembly]");
        log("    Shutting down a service assembly...");
        log("        host=" + hostName);
        log("        port=" + port);
        log("        name=" + serviceAssemblyID);
        
        String result = adminService.shutdownServiceAssembly(serviceAssemblyID, FORCE);
        Object[] value = JBIMBeanTaskResultHandler.getProcessedResult("Shutdown", result);
        if (value[0] != null) {
            log((String) value[0]);
        }
        
        return ((Boolean)value[1]).booleanValue();
    }
    
    private boolean undeployServiceAssembly(AdministrationService adminService) {
        log("[undeploy-service-assembly]");
        log("    Undeploying a service assembly...");
        log("        host=" + hostName);
        log("        port=" + port);
        log("        name=" + serviceAssemblyID);
        
        String result = adminService.undeployServiceAssembly(serviceAssemblyID, FORCE);
        Object[] value = JBIMBeanTaskResultHandler.getProcessedResult("Undeploy", result);
        if (value[0] != null) {
            log((String) value[0]);
        }
        
        return ((Boolean)value[1]).booleanValue();
    }
//
//    private boolean postProcessResult(String actionName, String result) {
//        boolean success = (result.indexOf("Exception") == -1)
//                && (result.indexOf("FAILED") == -1);
//
//        if (success) {
//            log("    " + actionName + " service assembly succeeded.");
//        } else {
//
//            if (result.indexOf("<?xml") == -1) {
//                log("    " + actionName + " service assembly failed.");
//                // No XML, exception occurred during invoke()
//                log("    " + result);
//
//            } else {
//
//                if (result.indexOf("SUCCESS") != -1) {
//                    log("    " + actionName + " service assembly failed. (partial success)");
//                } else {
//                    log("    " + actionName + " service assembly failed.");
//                }
//
//                // Extract error info from the XML result
//                try {
//                    result = result.substring(result.indexOf("<?xml"));
//                    Document document = loadXML(result);
//
//                    XPath xpath = XPathFactory.newInstance().newXPath();
//                    NodeList nodes = (NodeList) xpath.evaluate(
//                            "//exception-info/msg-loc-info", document,
//                            XPathConstants.NODESET);
//
//                    if (nodes != null) {
//                        int length = nodes.getLength();
//                        for (int i = 0; i < length; i++) {
//                            Node locInfoNode = nodes.item(i);
//
//                            String locTokenValue = null;
//                            String locMessageValue = null;
//
//                            Node tokenNode = (Node) xpath.evaluate("loc-token",
//                                    locInfoNode, XPathConstants.NODE);
//                            if (tokenNode != null) {
//                                locTokenValue = tokenNode.getTextContent();
//                            }
//
//                            Node messageNode = (Node) xpath.evaluate(
//                                    "loc-message", locInfoNode,
//                                    XPathConstants.NODE);
//                            if (messageNode != null) {
//                                locMessageValue = messageNode.getTextContent();
//                            }
//
//                            if (locTokenValue != null
//                                    || locMessageValue != null) {
//                                log("        MESSAGE: ("
//                                        + locTokenValue + ") "
//                                        + locMessageValue);
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        return success;
//    }
    
    static Document loadXML(String xmlSource) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            // documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory
                    .newDocumentBuilder();
            return documentBuilder.parse(new InputSource(new StringReader(
                    xmlSource)));
        } catch (Exception e) {
            System.out.println("Error parsing XML: " + e);
            return null;
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param args
     *            DOCUMENT ME!
     */
    public static void main(String[] args) {
        System.out.println("here");
        DeployServiceAssembly deploy = new DeployServiceAssembly();
        deploy.setServiceAssemblyID("01000000-C40493EE0B0100-8199A774-01"); // NOI18N
        deploy
                .setServiceAssemblyLocation("C:\\Documents and Settings\\jqian\\CompositeApp10\\dist\\CompositeApp10.zip"); // NOI18N
        deploy.setUserName("admin");
        deploy.setPassword("adminadmin");
        deploy.setHostName("localhost");
        deploy.setPort("4848");
        deploy.setServerInstanceLocation("C:\\Alaska\\Sun\\AppServer");
        deploy.execute();
    }
    
}
