/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * IncrementalDeployment.java
 *
 */

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.TargetModuleID;
import java.util.Map;
import java.io.*;

/**
 * Interface for any plugin that implments IncrementalDeployment
 *
 * @author George FinKlang
 * @version 0.1
 */

public interface IncrementalDeployment extends DeploymentManagerWrapper {
    
    /* return true if all modules can be redeployed incrementally */
    public boolean canIncrementallyRedeploy(TargetModuleID[] modules);
    
    /**
     * @param moduleIDList Modules being redeployed
     * @param partialArchive A skeleton archive containing just the changed
     * files.  partialArchive is null if the server
     * also implements InplaceDeployment
     * @param deploymentPlan JSR-88 deployment plan
     * @param changelist Map of TargetModuleID -> String[]
     * for the list of changed/new/deleted files in
     * the archive.
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @return
     */    
    public ProgressObject incrementalRedeploy(TargetModuleID[] moduleIDList,
               File partialArchive, InputStream deploymentPlan, Map changelist) 
                   throws UnsupportedOperationException,
                          IllegalStateException;
    }
