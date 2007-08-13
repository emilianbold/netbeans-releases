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

package org.netbeans.modules.tomcat5.ide;

import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.TomcatModule;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;

/*
 * TMIDResolver.java
 *
 * @author  nn136682
 */
public class TMIDResolver extends TargetModuleIDResolver {

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
            Logger.getLogger(TMIDResolver.class.getName()).log(Level.INFO, null, ex);
        }
        
        return (TargetModuleID[]) result.toArray(new TargetModuleID[result.size()]);
    }
}
