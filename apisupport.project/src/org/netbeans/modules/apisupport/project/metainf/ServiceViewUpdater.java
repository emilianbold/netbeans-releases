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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.apisupport.project.metainf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * It contains cached Services for suites. It should be better to move it to Suite Project.
 */
final class ServiceViewUpdater {
    private static Map <SuiteProject,List<Service>> suiteToServices = new WeakHashMap<SuiteProject,List<Service>>(); 
    /** Creates a new instance of ServiceViewUpdater */
    
    /** Called if a service was update. It will refresh all openeden ServiceNodeHandlers
     */
    public static void serviceUpdated(Service service,ServiceNodeHandler handler) throws IOException {
        // get suite
        SuiteProject suite = getSuite(handler);
        if (suite != null) {
            SubprojectProvider spp = suite.getLookup().lookup(org.netbeans.spi.project.SubprojectProvider.class);
            for (Iterator it = spp.getSubprojects().iterator(); it.hasNext() ; ) {
                 ServiceNodeHandler handler2 = ((org.netbeans.api.project.Project) it.next()).getLookup().lookup(org.netbeans.modules.apisupport.project.metainf.ServiceNodeHandler.class);
                 if (handler2 != null) {
                    handler2.updateService(service);
                 }
            }
        } else {
            NbModuleProvider.NbModuleType type = (handler.getProject().getLookup().lookup(org.netbeans.modules.apisupport.project.spi.NbModuleProvider.class)).getModuleType();
            if (type == NbModuleProvider.STANDALONE) { 
                // standalone module
                // update only this project
                handler.updateService(service);
            } else if (type == NbModuleProvider.NETBEANS_ORG) {
               // nb org project type
                // update all opened projects
                File nbroot =  ModuleList.findNetBeansOrg(FileUtil.toFile(handler.getProject().getProjectDirectory()));
 
                FileObject nbAllfo = FileUtil.toFileObject(nbroot);
                if (nbAllfo != null) {
                    Project projects[] = OpenProjects.getDefault().getOpenProjects();
                    // iterate through all opened projects
                    for (int i = 0; i < projects.length; i++) {
                        // for all opened projects for nb_all  
                        if (projects[i] instanceof NbModuleProject) {
                            NbModuleProject prj = (NbModuleProject) projects[i];
                            NbModuleProvider.NbModuleType prjType = (handler.getProject().getLookup().lookup(org.netbeans.modules.apisupport.project.spi.NbModuleProvider.class)).getModuleType();
                            if (prjType == NbModuleProvider.NETBEANS_ORG &&
                                FileUtil.isParentOf(nbAllfo,prj.getProjectDirectory())) {
                                 ServiceNodeHandler handler2 = prj.getLookup().lookup(org.netbeans.modules.apisupport.project.metainf.ServiceNodeHandler.class);
                                 if (handler2 != null) {
                                    handler2.updateService(service);
                                 }

                            }
                        }
                    }
                }
            }
        } 
    }
    /** Get SuiteProject of NbModuleProject 
     */
    private static SuiteProject getSuite(ServiceNodeHandler handler) throws IOException {
        SuiteProject retPrj = null;
        Project p = handler.getProject();
        NbModuleProvider.NbModuleType type = p.getLookup().lookup(NbModuleProvider.class).getModuleType();
        if (type == NbModuleProvider.SUITE_COMPONENT) {
            SuiteProvider suiteProv = p.getLookup().lookup(org.netbeans.modules.apisupport.project.SuiteProvider.class);
            assert suiteProv != null : p;
            File suiteDir = suiteProv.getSuiteDirectory();
            if (suiteDir == null || !suiteDir.isDirectory()) {
                throw new IOException("Could not locate suite for " + p); // NOI18N
            }
            Project prj = ProjectManager.getDefault().findProject(FileUtil.toFileObject(suiteDir));
            if (prj instanceof SuiteProject) {
                retPrj = (SuiteProject)prj;
            }
        }
        return retPrj;
    }
    

    /** @return services from platform and modules
     */
    static List<Service> getAllServices(ServiceNodeHandler serviceNodeHandler) throws IOException {
        SuiteProject suite = getSuite(serviceNodeHandler);
        if (suite != null) {
            List <Service> services = suiteToServices.get(suite);
            if (services == null) {
                services = Service.getPlatfromServices(serviceNodeHandler.getProject());
            }
            SubprojectProvider subprojects = suite.getLookup().lookup(org.netbeans.spi.project.SubprojectProvider.class);        
            for (Iterator sIt = subprojects.getSubprojects().iterator() ; sIt.hasNext() ; ) {
                NbModuleProject project = (NbModuleProject)sIt.next();
                services.addAll(Service.getOnlyProjectServices(project));
            }  
            suiteToServices.put(suite,services);
            return services; 
        } else {
            return Service.getPlatfromServices(serviceNodeHandler.getProject());
        }
    }
    
    /** @return subset of services which are in project of handler
     */ 
    static List<Service> filterServices(List services,ServiceNodeHandler handler) throws IOException {
        if (getSuite(handler) != null) {
            List<Service> allServices = getAllServices(handler);
            List <Service> retList = new ArrayList<Service>();
            NbModuleProvider info = handler.getProject().getLookup().lookup(NbModuleProvider.class);
            String cnb = info.getCodeNameBase();
            for (int i = 0 ; i < allServices.size() ; i++) {
               Service service = allServices.get(i);
               if (service.getCodebase().equals(cnb)) {
                   retList.add(service);
               }
            }
            return retList;
        } else {
            return Service.getOnlyProjectServices(handler.getProject());
        }
    }
}
