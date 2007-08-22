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

import java.io.File;
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
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.apache.tools.ant.Project;
import org.netbeans.modules.compapp.jbiserver.GenericConstants;
import org.netbeans.modules.compapp.jbiserver.management.AdministrationService;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.netbeans.modules.compapp.debugger.CompAppSessionProvider;

/**
 * Ant task to set up debug environment for CompApp project test case run.
 *
 * @author jqian
 */
// TODO: increase test case timeout property or simply ignore it
// when debug is enabled.
public class SetUpDebugEnvironment extends AbstractDebugEnvironmentTask {
    
    public void execute() throws BuildException {
        log("SetUpDebugEnvironment:", Project.MSG_DEBUG);
        //log("Current thread:" + Thread.currentThread(), Project.MSG_DEBUG);
        
        Map<String, Boolean> debugEnabledMap = initDebugEnabledMap();
        
        List<String> seNames = getUsedServiceEngineNames();
        
        AdministrationService adminService = getAdminService();
        MBeanServerConnection connection = adminService.getServerConnection();
        
        //We are going to create one debug session to which other modules could
        //provide their debugger engines (i.e. BPEL Debugger Engine).
        //We put all the information that debugger
        //engines might need to debugParams map and suply it to the debug session.
        Map<String, Object> debugParams = new HashMap<String, Object>();
        
        for (String seName : seNames) {
            try {
                ConfigureDeployments configurator = new ConfigureDeployments(
                        GenericConstants.SERVICE_ENGINES_FOLDER_NAME, seName, connection);
                
                // Make sure the SE is in debug mode
                //TODO:probably, this should be refactored so that debugger engines
                //enables the debugging of their SEs themselfs.
                //That would require to make some of JBI Manager APIs public.
                boolean debugEnabled = ((Boolean) configurator.getPropertyValue(SERVICE_ENGINE_DEBUG_FLAG)).booleanValue();  
                
                log("The original debug-enabled property for " + 
                        seName + " is " + debugEnabled, Project.MSG_DEBUG);
                
                debugEnabledMap.put(seName, debugEnabled);
                
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
                    Map<String, Object> seParams = new HashMap<String, Object>();
                    seParams.put("port", debugPort); //NOI18N
                    debugParams.put(seName, seParams);
                }
            } catch (Exception e) {
                // If the SE doesn't support debugging, simply ignore it.
                log(e.getMessage(), Project.MSG_WARN);
            }
        }
        
        //start debug session only if there are Service Engines which support debugging
        //TODO:it may need to ensure that there is at least one debugger engine
        //that can debug at least one of the SEs used in this CompApp
        if (!debugParams.isEmpty()) {
            try {
                String projectBaseDir = getProject().getProperty("basedir"); //NOI18N
                debugParams.put("projectBaseDir", projectBaseDir); //NOI18N
                String serverInstance = getProject().
                        getProperty(JbiProjectProperties.J2EE_SERVER_INSTANCE);
                debugParams.put("j2eeServerInstance", serverInstance); //NOI18N
                
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
}
