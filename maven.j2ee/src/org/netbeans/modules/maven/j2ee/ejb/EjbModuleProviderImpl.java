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
package org.netbeans.modules.maven.j2ee.ejb;

import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarsInProject;
import org.netbeans.modules.maven.j2ee.ExecutionChecker;
import org.netbeans.modules.maven.j2ee.POHImpl;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  Milos Kleint 
 */

public class EjbModuleProviderImpl extends J2eeModuleProvider implements EjbJarProvider, EjbJarsInProject  {
    
    private EjbJarImpl ejbimpl;
    private Project project;
    private String serverInstanceID;
    private J2eeModule j2eemodule;    
    private EjbJar apiEjbJar;
    private NbMavenProject mavenproject;
    
    /** Creates a new instance of EjbModuleProviderImpl */
    public EjbModuleProviderImpl(Project proj) {
        project = proj;
        ejbimpl = new EjbJarImpl(project, this);
        mavenproject = project.getLookup().lookup(NbMavenProject.class);
    }

    @Override
    public DeployOnSaveSupport getDeployOnSaveSupport() {
        //TODO
        return super.getDeployOnSaveSupport();
    }



    public EjbJarImpl getModuleImpl() {
        return ejbimpl;
    }
    
    /**
     * 
     * @param file 
     * @return 
     */
    public EjbJar findEjbJar(FileObject file) {
        Project proj = FileOwnerQuery.getOwner (file);
        if (proj != null) {
            proj = proj.getLookup().lookup(Project.class);
        }
        if (proj != null && project == proj) {
            if (ejbimpl.isValid()) {
                if (apiEjbJar == null) {
                    apiEjbJar =  EjbJarFactory.createEjbJar(ejbimpl);
                }
                return apiEjbJar;
            }
        }
        return null;
    }

    /**
     * 
     * @return 
     */
    public synchronized J2eeModule getJ2eeModule() {
        if (j2eemodule == null) {
            j2eemodule = J2eeModuleFactory.createJ2eeModule(ejbimpl);
        }
        return j2eemodule; 
    }
    
    /**
     * 
     * @return 
     */
    public ModuleChangeReporter getModuleChangeReporter() {
        return ejbimpl;
    }


    public void setServerInstanceID(String string) {
        String oldone = null;
        if (serverInstanceID != null) {
            oldone = POHImpl.privateGetServerId(serverInstanceID);
        }
        serverInstanceID = string;
        if (oldone != null) {
            fireServerChange(oldone, getServerID());            
        }
    }
    
    @Override
    public String getServerInstanceID() {
        if (serverInstanceID != null && POHImpl.privateGetServerId(serverInstanceID) != null) {
            return serverInstanceID;
        }
        return ExecutionChecker.DEV_NULL;
    }
    
    @Override
    public String getServerID() {
        if (serverInstanceID != null) {
            String tr = POHImpl.privateGetServerId(serverInstanceID);
            if (tr != null) {
                return tr;
            }
        }
        return ExecutionChecker.DEV_NULL;
    }
    
    @Override
    public FileObject[] getSourceRoots() {
//        System.out.println("EjbMP: getsourceroots");

        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        //#116215,121147 just workaround the AIOOBE
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

    public EjbJar[] getEjbJars() {
        if (ejbimpl.isValid()) {
            if (apiEjbJar == null) {
                apiEjbJar =  EjbJarFactory.createEjbJar(ejbimpl);
            }
            return new EjbJar[] {apiEjbJar};
        }
        return new EjbJar[0];
    }
    
}
