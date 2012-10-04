/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.ejbrefactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Refactoring factory for EJB refactorings. At the moment there is no actual support, 
 * the EJB refactoring plugin only displays a warning message if there is an ejb-jar.xml 
 * file in any of the affected projects.
 *
 * @author Erno Mononen
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class)
public class EjbRefactoringFactory implements RefactoringPluginFactory{
    
    public EjbRefactoringFactory() {
    }
    
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {

        FileObject source = getRefactoringSource(refactoring);
        if (source == null){
            return null;
        }

        List<EjbJar> ejbJars = getEjbJars(source);
        if (ejbJars.isEmpty()){
            return null;
        }
        
        String ejbJarPaths = getEjbJarPaths(ejbJars);
        String msg = null;
        
        if (refactoring instanceof RenameRefactoring){
            msg = "TXT_EjbJarRenameWarning"; //NO18N
        } else if (refactoring instanceof SafeDeleteRefactoring){
            msg = "TXT_EjbJarSafeDeleteWarning"; //NO18N
        } else if (refactoring instanceof MoveRefactoring){
            msg = "TXT_EjbJarMoveClassWarning"; //NO18N
        } else if (refactoring instanceof WhereUsedQuery){
            msg = "TXT_EjbJarWhereUsedWarning";//NO18N
        } else {
            msg = "TXT_EjbJarGeneralWarning";//NO18N
        }
        return new EjbRefactoringPlugin(refactoring, NbBundle.getMessage(EjbRefactoringFactory.class, msg, ejbJarPaths));
    }
    
    /**
     *@return a comma separated string representing the locations of the ejb-jar.xml 
     * files of the given <code>ejbJars</code>. 
     */ 
    private String getEjbJarPaths(List<EjbJar> ejbJars){
        // TODO: it would be probably better to display the project names instead
        StringBuilder ejbJarPaths = new StringBuilder();
        for (Iterator<EjbJar> it = ejbJars.iterator(); it.hasNext();){
            EjbJar ejbJar = it.next();
            String path = FileUtil.getFileDisplayName(ejbJar.getDeploymentDescriptor());
            ejbJarPaths.append(path);
            if (it.hasNext()){
                ejbJarPaths.append(", ");
            }
        }
        return ejbJarPaths.toString();
    }
    
    
    private FileObject getRefactoringSource(AbstractRefactoring refactoring){
        FileObject source = refactoring.getRefactoringSource().lookup(FileObject.class);
        if (source != null){
            return source;
        }
        TreePathHandle tph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (tph != null){
            return tph.getFileObject();
        }
        NonRecursiveFolder folder = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
        if (folder != null){
            return folder.getFolder();
        }
        return null;
    }
    
    /**
     *@return the <code>EjbJar</code>s representing the EJB Modules that are relevant
     * to the given <code>source</code>, i.e. the ones that depend on the project
     * owning the <code>source</code>.
     */
    private List<EjbJar> getEjbJars(FileObject source){
        List<EjbJar> result = new ArrayList<EjbJar>();
        for (EjbJar each : getRelevantEjbModules(source)) {
            FileObject ejbJarFO = each.getDeploymentDescriptor();
            if (ejbJarFO != null){
                result.add(each);
            }
        }
        return result;
    }
    
    /** Finds all ejb projects that depend on a project which is owner of FileObject 'fo' */
    private static Collection<EjbJar> getRelevantEjbModules(FileObject fo) {
        Project affectedProject = FileOwnerQuery.getOwner(fo);
        List<EjbJar> ejbmodules = new ArrayList<EjbJar>();
        List<Project> projects = new ArrayList<Project>();
        
        if (affectedProject != null) {
            // first check if the project which directly contains fo is relevant
            org.netbeans.modules.j2ee.api.ejbjar.EjbJar emod =
                    org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(affectedProject.getProjectDirectory());
            if (emod != null) {
                projects.add(affectedProject);
            } else {
                return Collections.EMPTY_SET;
            }
            for (Project project : OpenProjects.getDefault().getOpenProjects()){
                Object isJ2eeApp = project.getLookup().lookup(J2eeApplicationProvider.class);
                if (isJ2eeApp != null) {
                    J2eeApplicationProvider j2eeApp = (J2eeApplicationProvider) isJ2eeApp;
                    J2eeModuleProvider[] j2eeModules = j2eeApp.getChildModuleProviders();
                    
                    if (j2eeModules != null) {
                        J2eeModuleProvider affectedPrjProvider =
                                (J2eeModuleProvider) affectedProject.getLookup().lookup(J2eeModuleProvider.class);
                        
                        if (affectedPrjProvider != null) {
                            if (Arrays.asList(j2eeModules).contains(affectedPrjProvider)) {
                                for (int k = 0; k < j2eeModules.length; k++) {
                                    FileObject[] sourceRoots = j2eeModules[k].getSourceRoots();
                                    if (sourceRoots != null && sourceRoots.length > 0){
                                        FileObject srcRoot = sourceRoots[0];
                                        Project p = FileOwnerQuery.getOwner(srcRoot);
                                        if ((p != null) && (!projects.contains(p))) {
                                            projects.add(p);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //mkleint: see subprojectprovider for official contract, maybe classpath should be checked instead? see #210465
                //in this case J2eeApplicationprovider might provide the same results though.
                Object obj = project.getLookup().lookup(SubprojectProvider.class);
                if ((obj != null) && (obj instanceof SubprojectProvider)) {
                    Set subprojects = ((SubprojectProvider) obj).getSubprojects();
                    if (subprojects.contains(affectedProject)) {
                        org.netbeans.modules.j2ee.api.ejbjar.EjbJar em = org.netbeans.modules.j2ee.api.ejbjar.EjbJar
                                .getEjbJar(project.getProjectDirectory());
                        if (em != null) {
                            if (!projects.contains(project)) { // include each project only once
                                projects.add(project);
                            }
                        }
                    }
                }
            }
        }
        
        for (int j=0; j < projects.size(); j++) {
            Project prj = (Project)((ArrayList)projects).get(j);
            org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejb =
                    org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(prj.getProjectDirectory());
            if (ejb != null) {
                ejbmodules.add(ejb);
            }
        }
        
        return ejbmodules;
    }
    
}
