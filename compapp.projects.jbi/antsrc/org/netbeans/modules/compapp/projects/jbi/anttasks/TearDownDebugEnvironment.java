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

import java.util.Map;
import javax.management.MBeanServerConnection;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.compapp.jbiserver.GenericConstants;
import org.netbeans.modules.compapp.jbiserver.management.AdministrationService;


/**
 * Ant task to tear down debug environment for CompApp project test case run.
 *
 * @author jqian
 */
public class TearDownDebugEnvironment extends AbstractDebugEnvironmentTask {
    
    public void execute() throws BuildException {
        log("TearDownDebugEnvironment:", Project.MSG_DEBUG);
        //log("Current thread:" + Thread.currentThread(), Project.MSG_DEBUG);
        
        DebuggerManager.getDebuggerManager().finishAllSessions();
        
        // Restore SE's debugEnabled property        
        AdministrationService adminService = getAdminService();            
        MBeanServerConnection connection = adminService.getServerConnection();
                
        Map<String, Boolean> debugEnabledMap = getDebugEnabledMap();
        for (String seName : debugEnabledMap.keySet()) {
            boolean wasDebugEnabled = debugEnabledMap.get(seName); 
            if (!wasDebugEnabled) {
                try {
                    log("Restore debug-enabled property for " + seName, Project.MSG_DEBUG);
                    
                    ConfigureDeployments configurator = new ConfigureDeployments(
                                GenericConstants.SERVICE_ENGINES_FOLDER_NAME, seName, connection);
                    configurator.setProperty(SERVICE_ENGINE_DEBUG_FLAG, Boolean.FALSE);
                } catch (Exception e) {
                    log(e.getMessage(), Project.MSG_WARN);
                }
            }
        }
    }     
}
