/*
 * IncrementalDeployment.java
 *
 * Created on November 14, 2003, 9:13 AM
 */

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;

import java.io.File;

/** 
 * This interface replaces DeploymentManager.redeploy() for file-based redeploy.
 * perhaps rename to DirectoryDeploy when we re-package the api classes
 * @author  George Finklang
 */
public interface IncrementalDeployment extends DeploymentManagerWrapper {
    
    /* Before this method is called, the on-disk representation of TargetModuleID
     * is updated, and AppChangeDescriptor describes what in the application changed. 
     **/
    public ProgressObject incrementalDeploy(TargetModuleID module, AppChangeDescriptor changes);
    
    // public ProgressObject initialDeploy(DeployableObject app, File dir);
    
}
