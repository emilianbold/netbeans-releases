/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee.ear;

import java.io.File;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation;
import org.netbeans.modules.j2ee.spi.ejbjar.EarProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory;
import org.netbeans.modules.maven.j2ee.ExecutionChecker;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * provider for ear specific functionality
 * @author  Milos Kleint
 */

public class EarModuleProviderImpl extends J2eeApplicationProvider implements EarProvider  {
    
    private EarImpl earimpl;
    private Project project;
    private String serverInstanceID;
    private J2eeModule j2eemodule;
    private NbMavenProject mavenproject;

    
    /** Creates a new instance of MavenEarProvider */
    public EarModuleProviderImpl(Project proj) {
        project = proj;
        mavenproject = project.getLookup().lookup(NbMavenProject.class);
        earimpl = new EarImpl(project, this);
    }
    
    public EarImplementation getEarImplementation() {
        return earimpl;
    }
    
    public void loadPersistedServerId() {
        loadPersistedServerId(true);
    }
    
    private void loadPersistedServerId(boolean ensureReady) {
        String oldId = getServerInstanceID();
        String oldSer = getServerID();
        
        AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
        String val = props.get(Constants.HINT_DEPLOY_J2EE_SERVER_ID, true);
        String server = props.get(Constants.HINT_DEPLOY_J2EE_SERVER, true);
        if (server == null) {
            //try checking for old values..
            server = props.get(Constants.HINT_DEPLOY_J2EE_SERVER_OLD, true);
        }
        String instanceFound = null;
        if (server != null) {
            String[] instances = Deployment.getDefault().getInstancesOfServer(server);
            String inst = null;
            if (instances != null && instances.length > 0) {
                inst = instances[0];
                for (int i = 0; i < instances.length; i++) {
                    if (val != null && val.equals(instances[i])) {
                        inst = instances[i];
                        break;
                    }
                }
                instanceFound = inst;
            }
        }
        serverInstanceID = instanceFound;
        if (oldId != null) {
            fireServerChange(oldSer, getServerID());
        }
        if (ensureReady) {
            getConfigSupport().ensureConfigurationReady();
        }
    }
    
    public Ear findEar(FileObject file) {
        Project proj = FileOwnerQuery.getOwner(file);
        if (proj != null) {
            proj = proj.getLookup().lookup(Project.class);
        }
        if (proj != null && project == proj) {
            if (earimpl != null && earimpl.isValid()) {
                return EjbJarFactory.createEar(earimpl);
            }
        }
        return null;
    }

    /**
     * Returns the provider for the child module specified by given URI.
     * 
     * @param uri the child module URI within the J2EE application.
     * @return J2eeModuleProvider object
     */
    public J2eeModuleProvider getChildModuleProvider(String uri) {
//        System.out.println("!!!give me module with uri=" + uri);
        return null;
    }

    /**
     * Returns list of providers of every child J2EE module of this J2EE app.
     * 
     * @return array of J2eeModuleProvider objects.
     */
    public J2eeModuleProvider[] getChildModuleProviders() {
//        System.out.println("!!!give me child module providers..");
        return new J2eeModuleProvider[0];
    }

    public synchronized J2eeModule getJ2eeModule() {
        if (j2eemodule == null) {
            j2eemodule = J2eeModuleFactory.createJ2eeApplication(earimpl);
        }
        return j2eemodule; 
    }


    public ModuleChangeReporter getModuleChangeReporter() {
        return earimpl;
    }

    /**
     * Returns source deployment configuration file path for the given deployment 
     * configuration file name. 
     * 
     * @param name file name of the deployement configuration file.
     * @return non-null absolute path to the deployment configuration file.
     */
    public File getDeploymentConfigurationFile(String name) {
        if (name == null) {
            return null;
        }
        String path = getConfigSupport().getContentRelativePath(name);
        if (path == null) {
            path = name;
        }
//        System.out.println("EMPI: getDeploymentConfigFile=" + name);
        return earimpl.getDDFile(path);
    }



    /**
     * Finds source deployment configuration file object for the given deployment 
     * configuration file name.  
     * 
     * @param name file name of the deployement configuration file.
     * @return FileObject of the configuration descriptor file; null if the file does not exists.
     */
    public FileObject findDeploymentConfigurationFile(String name) {
        File fil = getDeploymentConfigurationFile(name);
        if (fil != null) {
            return FileUtil.toFileObject(fil);
        }
        return null;
    }

    public void setServerInstanceID(String string) {
       String oldone = null;
        if (serverInstanceID != null) {
            oldone = Deployment.getDefault().getServerID(serverInstanceID);
        }
        serverInstanceID = string;
        if (oldone != null) {
            fireServerChange(oldone, getServerID());            
        }
    }
    
    @Override
    public String getServerInstanceID() {
        if (serverInstanceID != null && Deployment.getDefault().getServerID(serverInstanceID) != null) {
            return serverInstanceID;
        }
        return ExecutionChecker.DEV_NULL;
    }
    
    @Override
    public String getServerID() {
        if (serverInstanceID != null) {
            String tr = Deployment.getDefault().getServerID(serverInstanceID);
            if (tr != null) {
                return tr;
            }
        }
        return ExecutionChecker.DEV_NULL;
    }
    
    
    @Override
    public FileObject[] getSourceRoots() {
        //TODO instead of src/test/java, drop the resources in..
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        //#116215, 121147 just workaround the AIOOBE
        EjbJar[] jars = EjbJar.getEjbJars(project);
        int offset = (jars == null || jars.length == 0) ? 0 : 1;
        FileObject[] roots = new FileObject[groups.length + offset];
        if (jars != null && jars.length > 0) {
            roots[0] = jars[0].getMetaInf();
        }
        for (int i=0; i < groups.length; i++) {
            roots[i + offset] = groups[i].getRootFolder();
        }
        return roots;
    }
    
}
