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

import com.sun.jmx.mbeanserver.Repository;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.apache.tools.ant.Project;
//import org.netbeans.modules.bpel.debugger.api.AttachingCookie;
import org.netbeans.modules.compapp.jbiserver.GenericConstants;
import org.netbeans.modules.compapp.jbiserver.management.AdministrationService;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.netbeans.modules.compapp.debugger.CompAppSessionProvider;

/**
 * Ant task to set up debug environment for CompApp project test case run.
 *
 * @author jqian
 */
// TODO: increase test case timeout property or simply ignore it
// when debug is enabled.
public class SetUpDebugEnvironment extends Task {
    
    private String j2eeServerInstance;
    
    private String netBeansUserDir;
    
    // Current assumption about the the debug flag and debug port for any SE.
    // Otherwise, the SE needs to provide such info.
    private static final String SERVICE_ENGINE_DEBUG_FLAG = "DebugEnabled"; // NOI18N
    private static final String SERVICE_ENGINE_DEBUG_PORT = "DebugPort"; // NOI18N
    
    /**
     * DOCUMENT ME!
     *
     * @throws BuildException DOCUMENT ME!
     */
    public void execute() throws BuildException {

        //Various debugger engines can discover a host from the given j2ee server instnace
//        String host = "localhost"; // FIXME: do we support remote debugging?
        
        List<String> seNames = getUsedServiceEngineNames();
        
        AdministrationService adminService = getAdminService();
        MBeanServerConnection connection = adminService.getServerConnection();
        
        //We are going to create one debug session to which other modules could
        //provide their debugger engines (i.e. BPEL Debugger Engine).
        //We put all the information that debugger
        //engines might need to debugParams map and suply it to the debug session.
        Map debugParams = new HashMap();
        
        for (String seName : seNames) {
            try {
                ConfigureDeployments configurator = new ConfigureDeployments(
                        GenericConstants.SERVICE_ENGINES_FOLDER_NAME, seName, connection);
                
                // Make sure the SE is in debug mode
                //TODO:probably, this should be refactored so that debugger engines
                //enables the debugging of their SEs themselfs.
                //That would require to make some of JBI Manager APIs public.
                boolean debugEnabled = ((Boolean) configurator.getPropertyValue(SERVICE_ENGINE_DEBUG_FLAG)).booleanValue();
                if (!debugEnabled) {
                    configurator.setProperty(SERVICE_ENGINE_DEBUG_FLAG, Boolean.TRUE);
                }
                
                // Obtain the attach-to port number from the SE
                Integer debugPort = (Integer)configurator.getPropertyValue(SERVICE_ENGINE_DEBUG_PORT);
                
                if (debugPort != null) {
                    //adding Service Engine specific information to debug session parameters
                    //TODO:probably, this should be refactored so that debugger engines
                    //discover the required Service Engine information themselfs
                    //from the given J2EE Server Instance. That would require to
                    //make some of JBI Manager APIs public.
                    Map seParams = new HashMap();
                    seParams.put("port", debugPort); //NOI18N
                    debugParams.put(seName, seParams);
                    // attaches the SE debugger to localhost:<portNumber>
//                    DebuggerManager.getDebuggerManager().startDebugging(
//                            DebuggerInfo.create(AttachingCookie.ID,
//                            new Object[] {AttachingCookie.create(host, debugPort)}));
                    
                    // TMP
//                    try {
//                        Thread.currentThread().sleep(2000);
//                    } catch (InterruptedException ex) {
//                        ex.printStackTrace();
//                    }
                }
            } catch (Exception e) {
                // If the SE doesn't support debugging, simply ignore it.
                e.printStackTrace();
            }
        }
        
        //start debug session only if there are Service Engines which support debugging
        //TODO:it may need to ensure that there is at least one debugger engine
        //that can debug at least one of the SEs used in this CompApp
        if (!debugParams.isEmpty()) {
            try {
                String projectBaseDir = getProject().getProperty("basedir"); //NOI18N
                debugParams.put("projectBaseDir", projectBaseDir); //NOI18N
                String j2eeServerInstance = getProject().
                        getProperty(JbiProjectProperties.J2EE_SERVER_INSTANCE);
                debugParams.put("j2eeServerInstance", j2eeServerInstance); //NOI18N
                
                //that would start a debug session to which other modules
                //could provide debugger engines
                DebuggerManager.getDebuggerManager().startDebugging(
                        DebuggerInfo.create(
                        CompAppSessionProvider.DEBUGGER_INFO_ID,
                        new Object[] {debugParams}));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Gets a list of names of Service Engines to which the Service Units of the
     * current Service Assembly are deployed to.
     */
    private List<String> getUsedServiceEngineNames() {
        
        List<String> ret = getUsedJBIComponentNames();
        
        // Filter out Binding Components        
        AdministrationService adminService = getAdminService();
        List<String> allServiceEngineNames = adminService.getServiceEngineNames();        
        ret.retainAll(allServiceEngineNames);
        
        return ret;
    }
    
    /**
     * Gets a list of names of JBI Components to which the Service Units of the
     * current Service Assembly are deployed to.
     */
    private List<String> getUsedJBIComponentNames() {
        
        List<String> ret = new ArrayList<String>();
        
        try {
            // TODO: should probably parse the jbi.xml inside the build artifact
            Project p = this.getProject();
            String projPath = p.getProperty("basedir") + File.separator;
            String buildDir = projPath + p.getProperty(JbiProjectProperties.BUILD_DIR);
            String jbiFileLoc = buildDir + "/META-INF/jbi.xml"; // NOI18N
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            Document document = builder.parse(new File(jbiFileLoc));
            
            NodeList nodes = (NodeList) xpath.evaluate("//component-name", document,  // NOI18N
                    XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String componentName = node.getTextContent();
                ret.add(componentName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return ret;
    }
    
    private AdministrationService getAdminService() {
        String nbUserDir = getNetBeansUserDir();
        String j2eeServerInstance = getJ2eeServerInstance();
        
        AdministrationService adminService = AdminServiceHelper.getAdminService(
                nbUserDir, j2eeServerInstance);
        
        return adminService;
    }
    
    public String getJ2eeServerInstance() {
        return j2eeServerInstance;
    }
    
    public void setJ2eeServerInstance(String j2eeServerInstance) {
        this.j2eeServerInstance = j2eeServerInstance;
    }
    
    public String getNetBeansUserDir() {
        return netBeansUserDir;
    }
    
    public void setNetBeansUserDir(String netBeansUserDir) {
        this.netBeansUserDir = netBeansUserDir;
    }
}
