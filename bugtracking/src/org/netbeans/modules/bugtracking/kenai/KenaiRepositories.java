/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.kenai;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.KenaiUtil;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.Dashboard;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
public abstract class KenaiRepositories {
    private static KenaiRepositories instance;

    protected KenaiRepositories() { }

    public static KenaiRepositories getInstance() {
        if(instance == null) {
            instance = Lookup.getDefault().lookup(KenaiRepositories.class);
            if (instance == null) {
                instance = new DefaultImpl();
            }
        }
        return instance;
    }

    //--------------------------------------------------------------------------

    /**
     * Returns bugtracking repositories of all Kenai projects currently opened
     * in the Kenai dashboard.
     *
     * @return  array of repositories collected from the projects
     *          (never {@code null})
     */
    public Repository[] getRepositories() {
        return getRepositories(false);
    }

    /**
     * Returns bugtracking repositories of all Kenai projects.
     *
     * @param  allOpenProjects  if {@code false}, search only Kenai projects
     *                          that are currently open in the Kenai dashboard;
     *                          if {@code true}, search also all Kenai projects
     *                          currently opened in the IDE
     * @return  array of repositories collected from the projects
     *          (never {@code null})
     */
    public abstract Repository[] getRepositories(boolean allOpenProjects);

    //--------------------------------------------------------------------------

    /**
     * The default implementation of {@code KenaiRepositories}.
     * This implementation is used if no other implementation is found
     * in the default lookup.
     */
    private static class DefaultImpl extends KenaiRepositories {

        public Repository[] getRepositories(boolean allOpenProjects) {
            KenaiProject[] kenaiProjects = allOpenProjects
                                           ? union(getDashboardProjects(),
                                                   getProjectsViewProjects())
                                           : getDashboardProjects();

            Repository[] result = new Repository[kenaiProjects.length];

            int count = 0;
            for (KenaiProject p : kenaiProjects) {
                Repository repo = KenaiRepositoryUtils.getInstance().getRepository(p);
                if (repo != null) {
                    result[count++] = repo;
                }
            }
            return stripTrailingNulls(result);
        }

        private KenaiProject[] getDashboardProjects() {
            ProjectHandle[] handles = Dashboard.getDefault().getOpenProjects();
            if ((handles == null) || (handles.length == 0)) {
                return new KenaiProject[0];
            }

            int count = 0;
            KenaiProject[] kenaiProjects = new KenaiProject[handles.length];
            for (ProjectHandle handle : handles) {
                KenaiProject project = handle.getKenaiProject();
                if (project != null) {
                    kenaiProjects[count++] = project;
                } else {
                    BugtrackingManager.LOG.warning(
                            "No Kenai project is available for ProjectHandle" //NOI18N
                            + " [" + handle.getId() + ", " + handle.getDisplayName() + "]"); //NOI18N
                }
            }

            return stripTrailingNulls(kenaiProjects);
        }

        private KenaiProject[] getProjectsViewProjects() {
            Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
            if (openProjects.length == 0) {
                return new KenaiProject[0];
            }

            int count = 0;
            KenaiProject[] kenaiProjects = new KenaiProject[openProjects.length];
            for (Project p : openProjects) {
                KenaiProject kenaiProject = getKenaiProject(p);
                if (kenaiProject != null) {
                    kenaiProjects[count++] = kenaiProject;
                }
            }

            return stripTrailingNulls(kenaiProjects);
        }

        private static KenaiProject getKenaiProject(Project p) {
            FileObject rootDir = p.getProjectDirectory();
            Object attValue = rootDir.getAttribute(
                                       "ProvidedExtensions.RemoteLocation");//NOI18N
            if (!(attValue instanceof String)) {
                return null;
            }

            KenaiProject kenaiProject;
            try {
                kenaiProject = KenaiProject.forRepository((String) attValue);
            } catch (KenaiException ex) {
                kenaiProject = null;
                BugtrackingManager.LOG.warning(
                        "No Kenai project is available for bugtracking repository " //NOI18N
                        + " [" + attValue + "]");                           //NOI18N
            }
            return kenaiProject;
        }

        private static KenaiProject[] union(KenaiProject[]... projectArrays) {
            int totalSize = 0;
            KenaiProject[] nonEmpty = null;

            /* Count the sum of all array sizes: */
            for (KenaiProject[] projectArray : projectArrays) {
                if (projectArray.length == 0) {
                    continue;
                }

                totalSize += projectArray.length;
                nonEmpty = projectArray;
            }
            assert (totalSize == 0) == (nonEmpty == null);

            /* Trivial cases: */
            if (totalSize == 0) {
                return new KenaiProject[0];
            }
            if (totalSize == nonEmpty.length) {
                return nonEmpty;        //all other arrays were empty
            }

            int count = 0;
            Collection<String> ids = new ArrayList<String>(totalSize);

            KenaiProject[] union = new KenaiProject[totalSize];
            for (KenaiProject[] projectArray : projectArrays) {
                for (KenaiProject p : projectArray) {
                    if (!ids.contains(p.getName())) {
                        union[count++] = p;
                        ids.add(p.getName());
                    }
                }
            }

            return stripTrailingNulls(union);
        }

        private static <T> T[] stripTrailingNulls(T[] array) {

            /* count trailing nulls -> compute size of the resulting array */
            int resultSize = array.length;
            while ((resultSize > 0) && (array[resultSize - 1] == null)) {
                resultSize--;
            }

            if (resultSize == array.length) {       //no trailing nulls
                return array;
            }

            T[] result = (T[]) java.lang.reflect.Array.newInstance(
                                                array.getClass().getComponentType(),
                                                resultSize);
            if (resultSize != 0) {
                System.arraycopy(array, 0, result, 0, resultSize);
            }
            return result;
        }

    }

}
