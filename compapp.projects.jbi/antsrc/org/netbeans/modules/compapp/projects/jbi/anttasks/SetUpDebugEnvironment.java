/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.compapp.projects.jbi.anttasks;

import com.sun.esb.management.api.configuration.ConfigurationService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.apache.tools.ant.Project;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.openide.util.Exceptions;
import org.netbeans.modules.compapp.debugger.CompAppSessionProvider;

/**
 * Ant task to set up debug environment for CompApp project test case run.
 *
 * @author jqian
 */
public class SetUpDebugEnvironment extends AbstractDebugEnvironmentTask {
    
    @Override
    public void execute() throws BuildException {
        log("SetUpDebugEnvironment:", Project.MSG_DEBUG);
        
        Map<String, Boolean> debugEnabledMap = initDebugEnabledMap();
        
        Set<String> seNames = getUsedServiceEngineNames(); 
        
        ConfigurationService adminService = getConfigurationService();
        
        //We are going to create one debug session to which other modules could
        //provide their debugger engines (i.e. BPEL Debugger Engine).
        //We put all the information that debugger
        //engines might need to debugParams map and suply it to the debug session.
        Map<String, Object> debugParams = new HashMap<String, Object>();
        
        for (String seName : seNames) {
            try {                
                // Make sure the SE is in debug mode
                //TODO:probably, this should be refactored so that debugger engines
                //enables the debugging of their SEs themselfs.
                //That would require to make some of JBI Manager APIs public.
                
                Properties configProperties = 
                        adminService.getComponentConfiguration(seName, "server");
                String debugFlag = 
                        configProperties.getProperty(SERVICE_ENGINE_DEBUG_FLAG);
                if (debugFlag == null) {
                    continue;
                }

                Boolean debugEnabled = Boolean.parseBoolean(debugFlag);
                log("The original debug-enabled property for " + 
                        seName + " is " + debugEnabled, Project.MSG_DEBUG);

                debugEnabledMap.put(seName, debugEnabled);

                if (!debugEnabled) {
                    Properties properties = new Properties();
                    properties.setProperty(SERVICE_ENGINE_DEBUG_FLAG, "true");                         
                    adminService.setComponentConfiguration(seName, properties, "server");
                }

                // Obtain the attach-to port number from the SE
                String debugPort = 
                    configProperties.getProperty(SERVICE_ENGINE_DEBUG_PORT);

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
     * Gets a set of names of service engines being used in the current compapp.
     */
    private Set<String> getUsedServiceEngineNames() {
        Set<String> ret = new HashSet<String>();
        
        try {
            Project p = this.getProject();
            String propsFilePath = p.getProperty("basedir") + 
                    File.separator + "nbproject" + 
                    File.separator + "project.properties";

            Properties props = new Properties();
            props.load(new FileInputStream(new File(propsFilePath)));
            String components = props.getProperty(JbiProjectProperties.JBI_CONTENT_COMPONENT);
            String[] seNames = components.split("\\s*;\\s*");
            ret.addAll(Arrays.asList(seNames));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return ret;
    }        
    
    /**
     * Gets a list of names of JBI Components to which the Service Units of the
     * current Service Assembly are deployed to.
     *
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
    }*/
}
