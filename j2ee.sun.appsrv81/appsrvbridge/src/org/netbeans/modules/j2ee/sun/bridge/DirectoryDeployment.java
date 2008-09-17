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

package org.netbeans.modules.j2ee.sun.bridge;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;


import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  Ludo Champenois
 * @author Vince Kraemer
 */
public class DirectoryDeployment extends IncrementalDeployment {
    
    private SunDeploymentManagerInterface dm;    
    
    /** Creates a new instance of DirectoryDeployment */
    public DirectoryDeployment() {
    }
    
    public DirectoryDeployment(DeploymentManager manager) {
        setDeploymentManager( manager);
    }
        
    /** Return a bogus name to satisfy the API. A file may be created by the
     * tool side. That file will be returned to this object in
     * writeDeploymentPlanFiles.  I will use it to find the directory for
     * writing the deployment descriptors and then delete it from that
     * directory.
     *
     * @param type The module type
     * @return a single, unique file name.
     */
    public String[] getDeploymentPlanFileNames(ModuleType type) {
        String[] s;
        if (type==null){
            throw new IllegalArgumentException("invalid null argumment");
        } else if(type.equals(ModuleType.WAR)){
            s = new String[] { "WEB-INF/sun-web.xml" };
        } else if(type.equals(ModuleType.EJB)){
            s = new String[] { "META-INF/sun-ejb-jar.xml", "META-INF/sun-cmp-mappings.xml" };
        } else if(type.equals(ModuleType.EAR)){
            s = new String[] { "META-INF/sun-application.xml" };
        } else if(type.equals(ModuleType.RAR)){
            s = new String[] { "META-INF/sun-connector.xml" };
        } else if(type.equals(ModuleType.CAR)){
            s = new String[] { "META-INF/sun-application-client.xml" };
        }
        
        else{
            s = new String[] { ".timestamp" };
        }
        
        return s;
    }
    
    /** Determine where a module is going to be copied to.
     * If the target is not a local instance, return null.
     *
     * If the bug 4946433 still exists, return null.  This will disable
     * in-place deployment for J2EE apps.
     *
     * @param module The module being deployed
     * @return The name of a directory.
     */
    public File getDirectoryForModule(TargetModuleID module) {
        if (null == dm){
            throw new IllegalStateException("invalid dm value");
        }
        File appDir = AppServerBridge.getDirLocation(module);
        // clean up some unexpected pollution :: https://glassfish.dev.java.net/issues/show_bug.cgi?id=4669
        String path = appDir.getPath();
        String TCONST = "${com.sun.aas.installRoot}";
        if (path.contains(TCONST)) {
            int dex = path.indexOf(TCONST);
            // the installRoot is an absolute... if it shows up somewhere else in a path 
            // correct the path.
            if (dex > 0) {
                path = path.substring(dex);
            }
            path = path.replace(TCONST, dm.getPlatformRoot().getAbsolutePath());
            appDir = new File(path);
        }
        if (null != appDir && appDir.getPath().contains("${")) {
            throw new IllegalStateException(NbBundle.getMessage(DirectoryDeployment.class,
                    "ERR_UndeployAndRedeploy"));
        }
        return appDir;
    }
    
    
    
    /**
     * @param manager
     */
    public void setDeploymentManager(DeploymentManager manager) {
        if (null == manager){
            throw new IllegalArgumentException("invalid null argumment");
        }
        if (manager instanceof SunDeploymentManagerInterface){
            this.dm = (SunDeploymentManagerInterface) manager;
        } else{
            throw new IllegalArgumentException("setDeploymentManager: Invalid manager type, expecting SunDeploymentManager and got "+manager.getClass().getName());
        }
    }
    
    
    /**
     * @param tmid
     * @param aCD
     * @return a progress object representing the incrmental dpeloy action.
     */
    final public ProgressObject  incrementalDeploy( final TargetModuleID tmid, AppChangeDescriptor aCD) {
        ProgressObject retVal = null;
        Thread holder = Thread.currentThread();
        try {
            dm.grabInnerDM(holder,false);
            DirectoryDeploymentFacility ddf = 
                    new DirectoryDeploymentFacility(dm.getHost(),dm.getPort(),
                    dm.getUserName(),dm.getPassword(),dm.isSecure());
            retVal = ddf.incrementalDeploy(tmid);
            if (null != retVal) {
                retVal.addProgressListener(new Releaser(dm,holder));
            }
        } finally {
            if (null == retVal) {
                dm.releaseInnerDM(holder);
            }
        }
        return retVal; //ddf. incrementalDeploy( tmid);
        
    }
    
    /**
     * Get the URI pointing to location of child module inside a application archive.
     * For a root module, service provider does not need to override this method.
     *
     * @param module TargetModuleID of the child module
     * @return its relative path within application archive, returns null by
     * default (for standalone module)
     */
    @Override
    public String getModuleUrl(TargetModuleID module){        
        String retVal = AppServerBridge.getModuleUrl(module);
        if (!retVal.startsWith("/")) {
            retVal = "/"+retVal;
   }
        return retVal;
   }
    
    


    
    /**
     * First time deployment file distribution.
     * Before this method is called the files are copied into the target
     * folder provided by plugin.
     * @param target target of deployment
     * @param app the app to deploy
     * @param configuration server specific data for deployment
     * @param dir the destination directory for the given deploy app
     * @return the object for feedback on progress of deployment
     *     
    public ProgressObject initialDeploy(Target target, 
                DeployableObject deployableObject,
                DeploymentConfiguration deploymentConfiguration, 
                File file) {
        SunDeploymentConfigurationInterface s1dc =(SunDeploymentConfigurationInterface) deploymentConfiguration;
        s1dc.getContextRoot();
        String moduleID= getGoodDirNameFromContextRoot(s1dc.getDeploymentModuleName());
        ProgressObject retVal = null;
        try {
            
            dm.grabInnerDM(false);
            DirectoryDeploymentFacility ddf = new DirectoryDeploymentFacility(dm.getHost(),dm.getPort(),dm.getUserName(),dm.getPassword(),dm.isSecure());
            retVal = ddf.initialDeploy( target,    file , moduleID);
            if (null != retVal) {
                retVal.addProgressListener(new Releaser(dm));
            }
        } finally {
            if (null == retVal) {
                dm.releaseInnerDM();
            }
        }
        return retVal;
    }
*/
    private String computeModuleID(J2eeModule app, File dir) {
        String moduleID = null;
        File foo = app.getDeploymentConfigurationFile("application.xml"); // NOI18N
        if (foo.exists()) {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(foo));
            try {
                if (null != fo) {
                    org.netbeans.modules.j2ee.dd.api.application.Application appdd = org.netbeans.modules.j2ee.dd.api.application.DDProvider.getDefault().getDDRoot(fo);
                    moduleID = appdd.getDisplayName(null);
                }
            } catch (IOException ioe) {
                Logger.getLogger(DirectoryDeployment.class.getName()).log(Level.FINER, null, ioe);
            } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException vnse) {
                Logger.getLogger(DirectoryDeployment.class.getName()).log(Level.FINER, null, vnse);
            }
        }
        if (null == moduleID) {
            foo = app.getDeploymentConfigurationFile("web.xml"); // NOI18N
            if (foo.exists()) {
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(foo));
                try {
                    if (null != fo) {
                        org.netbeans.modules.j2ee.dd.api.web.WebApp webdd = org.netbeans.modules.j2ee.dd.api.web.DDProvider.getDefault().getDDRoot(fo);
                        moduleID = webdd.getDisplayName(null);
                    }
                } catch (IOException ioe) {
                    Logger.getLogger(DirectoryDeployment.class.getName()).log(Level.FINER, null, ioe);
                } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException vnse) {
                    Logger.getLogger(DirectoryDeployment.class.getName()).log(Level.FINER, null, vnse);
                }
            }
        }
        if (null == moduleID) {
            foo = app.getDeploymentConfigurationFile("ejb-jar.xml"); // NOI18N
            if (foo.exists()) {
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(foo));
                try {
                    if (null != fo) {
                        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbdd = org.netbeans.modules.j2ee.dd.api.ejb.DDProvider.getDefault().getDDRoot(fo);
                        moduleID = ejbdd.getDisplayName(null);
                    }
                } catch (IOException ioe) {
                    Logger.getLogger(DirectoryDeployment.class.getName()).log(Level.FINER, null, ioe);
                } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException vnse) {
                    Logger.getLogger(DirectoryDeployment.class.getName()).log(Level.FINER, null, vnse);
                }
            }
        }
        if (null == moduleID) {
            foo = app.getDeploymentConfigurationFile("application-client.xml"); // NOI18N
            if (foo.exists()) {
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(foo));
                try {
                    if (null != fo) {
                        org.netbeans.modules.j2ee.dd.api.client.AppClient acdd = org.netbeans.modules.j2ee.dd.api.client.DDProvider.getDefault().getDDRoot(fo);
                        moduleID = acdd.getDisplayName(null);
                    }
                } catch (IOException ioe) {
                    Logger.getLogger(DirectoryDeployment.class.getName()).log(Level.FINER, null, ioe);
                } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException vnse) {
                    Logger.getLogger(DirectoryDeployment.class.getName()).log(Level.FINER, null, vnse);
                }
            }
        }
        
        // make sure the user hasn't set the desplay name to be "", too.
        //
        if (null == moduleID || moduleID.trim().length() < 1) {            
            FileObject fo = null;
            try {
                fo = app.getContentDirectory();
                moduleID = ProjectUtils.getInformation(FileOwnerQuery.getOwner(fo)).getName();
            } catch (IOException ioe) {
                Logger.getLogger(DirectoryDeployment.class.getName()).log(Level.FINER, null, ioe);
            }
        }
        if (null == moduleID || moduleID.trim().length() < 1) {
            moduleID = simplifyModuleID(dir.getParentFile().getParentFile().getName());
        } else {
            moduleID = simplifyModuleID(moduleID);
        }

        return moduleID;
    }
    
    private String simplifyModuleID(String candidateID){
        String   moduleID = null;
        if (candidateID==null){
            moduleID = "_default_"+this.hashCode() ;
        } else if (candidateID.equals("")){
            moduleID =  "_default_"+this.hashCode();
        }
        if (null == moduleID) {
            moduleID = candidateID.replace(' ','_');
            if (moduleID.startsWith("/")) {
                moduleID = moduleID.substring(1);
            }
            
            // This moduleID will be later used to construct file path,
            // replace the illegal characters in file name
            //  \ / : * ? " < > | with _
            moduleID = moduleID.replace('\\', '_').replace('/', '_');
            moduleID = moduleID.replace(':', '_').replace('*', '_');
            moduleID = moduleID.replace('?', '_').replace('"', '_');
            moduleID = moduleID.replace('<', '_').replace('>', '_');
            moduleID = moduleID.replace('|', '_');
            
            // This moduleID will also be used to construct an ObjectName
            // to register the module, so replace additional special
            // characters , =  used in property parsing with -
            moduleID = moduleID.replace(',', '_').replace('=', '_');

            // parens are illegal in the object name, too. IZ 143389
            moduleID = moduleID.replace('(', '_').replace(')', '_');
        }
        return moduleID;
    }

    /**
     * Return absolute path which the IDE will write the specified app or
     * stand-alone module contents to.
     * @param target target server of the deployment
     * @param app the app or stand-alone module to deploy
     * @param configuration server specific data for deployment
     * @return absolute path root directory for the specified app or
     * null if server can accept the deployment from an arbitrary directory.
     */    
    public java.io.File getDirectoryForNewApplication(Target target, DeployableObject deployableObject, DeploymentConfiguration deploymentConfiguration) {
        return null;
    }
    
    /**
     * Return absolute path the IDE will write the app or stand-alone module content to.
     * Note: to use deployment name, implementation nees to override this.
     *
     * @param deploymentName name to use in deployment
     * @param target target server of the deployment
     * @param configuration server specific data for deployment
     * @return absolute path root directory for the specified app or null if
     * server can accept the deployment from an arbitrary directory.
     */
    public File getDirectoryForNewApplication(String deploymentName, Target target, DeploymentConfiguration configuration){
        //        if (deployableObject.getType().equals(ModuleType.WAR)) {
        //            return null;
        //        }
        //        if (deployableObject.getType().equals(ModuleType.EJB)) {
        //            return null;
        //        }
        return null;
        //return new File(getApplicationsDir().getAbsolutePath()+"/" +deploymentName);        
    }
    
    /**
     * Return absolute path which the IDE will write the specified module contents to.
     * @param appDir the root directory of containing application
     * @param uri the URI of child module within the app
     * @param module the child module object to deploy
     * @param configuration server specific data for deployment
     * @return absolute path root directory for the specified module.
     */   
    public java.io.File getDirectoryForNewModule(File file, String str, 
            DeployableObject deployableObject, DeploymentConfiguration deploymentConfiguration) {
        throw new UnsupportedOperationException();
    }
    
    public ProgressObject initialDeploy(Target target, J2eeModule app, ModuleConfiguration configuration, File dir) {
        ContextRootConfiguration crp = (ContextRootConfiguration) configuration.getLookup().lookup(ContextRootConfiguration.class);
        String moduleID= computeModuleID(app,dir);
        ProgressObject retVal = null;
        Thread holder = Thread.currentThread();
        try {
            
            dm.grabInnerDM(holder, false);
            DirectoryDeploymentFacility ddf = 
                    new DirectoryDeploymentFacility(dm.getHost(),dm.getPort(),
                    dm.getUserName(),dm.getPassword(),dm.isSecure());
            retVal = ddf.initialDeploy( target,    dir , moduleID);
            retVal.addProgressListener(new Releaser(dm,holder));
        } finally {
            if (null == retVal) {
                dm.releaseInnerDM(holder);
            }
        }
        return retVal;
    }
    
    public boolean canFileDeploy(Target target, J2eeModule deployable) {
        throw new UnsupportedOperationException();
    }
    
    public File getDirectoryForNewApplication(Target target, J2eeModule app, 
            ModuleConfiguration configuration) {
        File dest = null;
        if (app.getModuleType() == J2eeModule.EAR) {
            //configuration.
            File tmp = getProjectDir(app); //app.getResourceDirectory();
            if (null == tmp) {
               return dest;
            }
            //tmp = tmp.getParentFile();
            // I am depending on the fact that an app will have a resource dir!!!
            dest = new File(tmp, "dist");
            dest = new File(dest, "gfdeploy");
            if (!dest.exists()) {
                dest.mkdirs();
            }
            if (!dest.isDirectory()) {
                dest = null;
            }
        }
        return dest;
    }
    
    // try to get the Project Directory as a File
    // use a couple different stratgies, since the resource.dir is in a user-
    // editable file -- but it is quicker to access, if it is there....
    //
    private File getProjectDir(J2eeModule app) {
        java.io.File tmp = app.getResourceDirectory();

        if (tmp != null) {
            return tmp.getParentFile();
        }
        try {
            FileObject fo = app.getContentDirectory();
            Project p = FileOwnerQuery.getOwner(fo);
            if (null != p) {
                fo = p.getProjectDirectory();
                return FileUtil.toFile(fo);
            }
        } catch (IOException ex) {
            Logger.getLogger("org.netbeans.modules.j2ee.sun.bridge").log(Level.FINER,    // NOI18N
                    null,ex);
        }
        return null;
    }
    
    @Override
    public File getDirectoryForNewApplication(String deploymentName, Target target, ModuleConfiguration configuration) {
        File retValue;
        
        retValue = super.getDirectoryForNewApplication(deploymentName, target, configuration);
        return retValue;
    }
    
    public File getDirectoryForNewModule(File appDir, String uri, J2eeModule module, ModuleConfiguration configuration) {
        return new File(appDir, transform(removeLeadSlash(uri)));
    }
    
    private String removeLeadSlash(String s) {
        if (null == s) {
            return s;
        }
        if (s.length() < 1) {
            return s;
        }
        if (!s.startsWith("/")) {
            return s;
        }
        return s.substring(1);
    }
    
    public static String transform(String s) {
        int len = s.length();
        if (len > 4) {
            StringBuffer sb = new StringBuffer(s);
            char tmp = sb.charAt(len - 4);
            if (tmp == '.') {
                sb.setCharAt(len-4, '_');
                return sb.toString();
            }
        }
        return s;
    }
    
    @Override
    public void notifyDeployment(TargetModuleID module) {
        super.notifyDeployment(module);
    }
    
    private static class Releaser implements ProgressListener {
        SunDeploymentManagerInterface dm;
        Thread holder;
        Releaser(SunDeploymentManagerInterface dm, Thread holder) {
            this.dm = dm;
            this.holder = holder;
        }
        
        public void handleProgressEvent(ProgressEvent progressEvent) {
            DeploymentStatus dms = progressEvent.getDeploymentStatus();
            if (!dms.isRunning()) {
                dm.releaseInnerDM(holder);
            }
        }
    }
    
}


