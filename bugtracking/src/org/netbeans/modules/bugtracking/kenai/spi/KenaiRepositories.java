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

package org.netbeans.modules.bugtracking.kenai.spi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiSupport.BugtrackingType;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
abstract class KenaiRepositories {

    private static KenaiRepositories instance;

    private final Map<String, Object> kenaiLocks = new HashMap<String, Object>(1);
    
    /**
     * Holds already created kenai repositories
     */
    private Map<String, Repository> repositoriesMap = Collections.synchronizedMap(new HashMap<String, Repository>());

    protected KenaiRepositories() { }

    public synchronized static KenaiRepositories getInstance() {
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
     * Returns a {@link Repository} representing the given {@link KenaiProject}
     *
     * @param kp
     * @return
     */
    public Repository getRepository(KenaiProject kp) {
        return getRepository(kp, true);
    }

    /**
     * Returns a {@link Repository} representing the given {@link KenaiProject}.
     *
     * @param kp KenaiProject
     * @param forceCreate determines if a Repository instance should be created if it doesn't already exist
     * @return
     */
    public Repository getRepository(KenaiProject kp, boolean forceCreate) {

        String repositoryKey = kp.getWebLocation().toString();
        BugtrackingManager.LOG.log(Level.FINER, "requesting repository for {0}", repositoryKey);  // NOI18N

        KenaiSupport support = getSupport(kp);
        if(support == null) {
            BugtrackingManager.LOG.log(Level.FINER, "no repository available for {0}", repositoryKey);  // NOI18N
            return null;
        }

        Object lock = getKenaiLock(kp, support);
        synchronized(lock) { // synchronize for a kenai instance and bugtracking type
            Repository repository = repositoriesMap.get(repositoryKey);
            if(repository == null && forceCreate) {
                repository = support.createRepository(kp);
                if(repository != null) {
                    // XXX what if more repos?!
                    repositoriesMap.put(repositoryKey, repository);
                }
            }
            BugtrackingManager.LOG.log(
                    Level.FINER,
                    "returning repository {0}:{1} for {2}", // NOI18N
                    new Object[]{repository != null ? repository.getDisplayName() : "null", repository != null ? repository.getUrl() : "", repositoryKey});  // NOI18N
            return repository;
        }
    }

    private KenaiSupport getSupport(KenaiProject kp) {
        // find the support corresponding with the given project
        KenaiSupport support = null;
        BugtrackingConnector[] connectors = BugtrackingUtil.getBugtrackingConnectors();
        for (BugtrackingConnector c : connectors) {
            support = c.getLookup().lookup(KenaiSupport.class);
            if (support != null && support.getType() == kp.getType()) {
                BugtrackingManager.LOG.log(Level.FINER, "found suport for {0}", kp.getWebLocation().toString()); // NOI18N
                break;
            }
            support = null;
        }
        return support;
    }

    private Object getKenaiLock(KenaiProject kp, KenaiSupport support) {
        synchronized(kenaiLocks) {
            BugtrackingType type = support.getType();
            final String key = kp.getWebLocation().getHost() + ":" + type;  // NOI18N
            BugtrackingManager.LOG.log(Level.FINER, "requesting lock for {0}", key); // NOI18N
            Object lock = kenaiLocks.get(key);
            if(lock == null) {
                lock = new Object();
                kenaiLocks.put(key, lock);
            }
            BugtrackingManager.LOG.log(Level.FINER, "returning lock {0} for {1}", new Object[]{lock, key}); // NOI18N
            return lock;
        }
    }

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

            EnumSet<BugtrackingType> reluctantSupports = EnumSet.noneOf(BugtrackingType.class);
            int count = 0;
            for (KenaiProject p : kenaiProjects) {
                if(!reluctantSupports.contains(p.getType())) {
                    Repository repo = getRepository(p);
                    if (repo != null) {
                        result[count++] = repo;
                    } else {
                        KenaiSupport support = super.getSupport(p);
                        if(support != null) {
                            BugtrackingManager.LOG.log(
                                    Level.WARNING,
                                    "could not get repository for project {0} with {1} bugtracking type ",
                                    new Object[]{p.getWebLocation(), p.getType()});
                            // there is a support available for the projects bugtracking type, yet
                            // we weren't able to create a repository for the project.
                            // lets assume there is something with the bugracker or that the user canceled
                            // the authorisation (see also issue #182946) and skip all other projects with the same
                            // support in this one call.
                            reluctantSupports.add(support.getType());
                        }
                    }
                } else {
                    BugtrackingManager.LOG.log(
                                    Level.WARNING,
                                    "skipping getRepository for project {0} with {1} bugtracking type ",
                                    new Object[]{p.getWebLocation(), p.getType()});
                }
            }
            return stripTrailingNulls(result);
        }

        private KenaiProject[] getDashboardProjects() {
            return KenaiUtil.getDashboardProjects();
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
                kenaiProject = KenaiUtil.getKenaiProjectForRepository((String) attValue);
            } catch (IOException ex) {
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
