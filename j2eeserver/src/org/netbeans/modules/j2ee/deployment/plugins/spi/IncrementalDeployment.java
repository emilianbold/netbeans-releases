/*
 * IncrementalDeployment.java
 *
 * Created on November 14, 2003, 9:13 AM
 */

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.model.DeployableObject;

import java.io.File;

/** 
 * This interface replaces DeploymentManager.redeploy() for file-based redeploy.
 * perhaps rename to DirectoryDeploy when we re-package the api classes
 * @author  George Finklang
 */
public interface IncrementalDeployment extends DeploymentManagerWrapper {

    /**
     * First time deployment file distribution
     * @param target target of deployment
     * @param app the app to deploy
     * @param dir the destination directory for the given deploy app
     * @return the object for feedback on progress of deployment
     */ 
    public ProgressObject initialDeploy(Target target, DeployableObject app, File dir);

    /* Before this method is called, the on-disk representation of TargetModuleID
     * is updated, and AppChangeDescriptor describes what in the application changed. 
     **/
    public ProgressObject incrementalDeploy(TargetModuleID module, AppChangeDescriptor changes);
    
}
