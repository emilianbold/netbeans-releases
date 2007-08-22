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

import java.util.HashMap;
import java.util.Map;
import org.apache.tools.ant.Task;
import org.netbeans.modules.compapp.jbiserver.management.AdministrationService;


/**
 * Abstract Ant task to deal with CompApp debug environment.
 *
 * @author jqian
 */
public class AbstractDebugEnvironmentTask extends Task {    
    
    private String j2eeServerInstance;
    
    private String netBeansUserDir;
    
    // Current assumption about the the debug flag and debug port for any SE.
    // Otherwise, the SE needs to provide such info.
    protected static final String SERVICE_ENGINE_DEBUG_FLAG = "DebugEnabled"; // NOI18N
    protected static final String SERVICE_ENGINE_DEBUG_PORT = "DebugPort"; // NOI18N
                    
    /**
     * A thread local variable for mapping the names of service engines to their 
     * original debug-enabled configurations. Those orignial configurations will 
     * be restored after the debug action is finished.
     */
    private static ThreadLocal<Map<String, Boolean>> DEBUG_ENABLED_MAP_THREADLOCAL = 
            new ThreadLocal<Map<String, Boolean>>();
          
    protected boolean debugAntTask = true;
    
    
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
       
    /**
     * Initializes the map mapping service engine names to the debug-enabled 
     * configurations.
     */
    protected Map<String, Boolean> initDebugEnabledMap() {
        Map<String, Boolean> debugEnabledMap = new HashMap<String, Boolean>();
        DEBUG_ENABLED_MAP_THREADLOCAL.set(debugEnabledMap);
        return debugEnabledMap;
    }
    
    /**
     * Gets the map mapping service engine names to the debug-enabled 
     * configurations.
     */
    protected Map<String, Boolean> getDebugEnabledMap() {
        return DEBUG_ENABLED_MAP_THREADLOCAL.get();
    }
    
    protected AdministrationService getAdminService() {
        String nbUserDir = getNetBeansUserDir();
        String serverInstance = getJ2eeServerInstance();
        
        AdministrationService adminService = AdminServiceHelper.getAdminService(
                nbUserDir, serverInstance);
        
        return adminService;
    }
}
