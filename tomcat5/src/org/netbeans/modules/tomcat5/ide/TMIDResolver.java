/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.ide;

import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.TomcatModule;
import org.openide.ErrorManager;

import java.util.ArrayList;

/*
 * TMIDResolver.java
 *
 * @author  nn136682
 */
public class TMIDResolver extends org.netbeans.modules.j2ee.deployment.plugins.api.TargetModuleIDResolver {

    TomcatManager tm;
    
    /** Creates a new instance of UndeploySupport */
    public TMIDResolver(DeploymentManager dm) {
        this.tm = (TomcatManager) dm;
    }
    
    public TargetModuleID[] lookupTargetModuleID(java.util.Map queryInfo, Target[] targetList) {
        String contextRoot = (String) queryInfo.get(KEY_CONTEXT_ROOT);
        if (contextRoot == null)
            return EMPTY_TMID_ARRAY;
        // Tomcat ROOT context path bug hack
        if ("".equals(contextRoot)) { // NOI18N
            contextRoot = "/"; // NOI18N
        }
        ArrayList result = new ArrayList();
        try {
            TargetModuleID[] tmidList = tm.getAvailableModules(ModuleType.WAR, targetList);
            for (int i=0; i<tmidList.length; i++) {
                TomcatModule tm = (TomcatModule) tmidList[i];
                if (contextRoot.equals(tm.getPath()))
                    result.add(tm);
            }
        } catch(Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return (TargetModuleID[]) result.toArray(new TargetModuleID[result.size()]);
    }
}
