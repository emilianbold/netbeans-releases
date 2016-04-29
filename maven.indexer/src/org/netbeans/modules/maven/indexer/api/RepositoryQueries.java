/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.maven.indexer.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.search.BooleanQuery;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.indexer.ResultImpl;
import org.netbeans.modules.maven.indexer.spi.ArchetypeQueries;
import org.netbeans.modules.maven.indexer.spi.BaseQueries;
import org.netbeans.modules.maven.indexer.spi.ChecksumQueries;
import org.netbeans.modules.maven.indexer.spi.ClassUsageQuery;
import org.netbeans.modules.maven.indexer.spi.ClassesQuery;
import org.netbeans.modules.maven.indexer.spi.ContextLoadedQuery;
import org.netbeans.modules.maven.indexer.spi.DependencyInfoQueries;
import org.netbeans.modules.maven.indexer.spi.GenericFindQuery;
import org.netbeans.modules.maven.indexer.spi.NullResultImpl;
import org.netbeans.modules.maven.indexer.spi.ResultImplementation;
import org.netbeans.modules.project.spi.intern.ProjectIDEServices;
import org.openide.util.Lookup;
import org.netbeans.modules.maven.indexer.spi.RepositoryIndexQueryProvider;

/**
 * Searches Maven repositories in various ways.
 * <p>All methods taking {@code List<RepositoryInfo>} accept null, in which case
 * all <em>loaded</em> indices are searched. If you really want to search all
 * indices - triggering indexing of previously unindexed repositories -
 * then pass {@link RepositoryPreferences#getRepositoryInfos RepositoryPreferences.getInstance().getRepositoryInfos()}.
 * @author mkleint
 */
public final class RepositoryQueries {

    /**
     * query result set
     * @param <T>
     * @since 2.9
     */
    public final static class Result<T> {
        private final ResultImplementation<T> impl;
        
        /**
         * Creates a Result instance.
         * @param impl 
         */
        public Result(ResultImplementation<T> impl) {
            this.impl = impl;
        }
        
        /**
         * Returns true is one or more indexes were skipped, e.g. because the indexing was taking place.
         * @return 
         */
        public synchronized boolean isPartial() {
            return impl.isPartial();
        }
        
        /**
         * Waits for currently unaccessible indexes to finish, not to be called in AWT thread.
         */
        public void waitForSkipped() {
            assert !ProjectIDEServices.isEventDispatchThread();
            impl.waitForSkipped();
        }
        
        /**
         * Returns a list of results
         * @return 
         */
        public synchronized List<T> getResults() {
            return impl.getResults();
        }
        
        /**
         * Total number of hits
         * @return
         * @since 2.20
         */
        public int getTotalResultCount() {
            return impl.getTotalResultCount();
        }

        /**
         * in some cases not entirely accurate number of processed and returned hits, typically should be less or equals to totalResultCount
         * @return 
         * @since 2.20
         */
        public int getReturnedResultCount() {
            return impl.getReturnedResultCount();
        }
    }
        
    private final static class CompositeResult<T> implements ResultImplementation {
        
        private final List<ResultImplementation<T>> results;
        
        public CompositeResult(List<ResultImplementation<T>> results) {
            this.results = results;
        }
    
        @Override
        public boolean isPartial() {
            for (ResultImplementation<T> result : results) {
                if(result.isPartial()) {
                    return true;
            }
        }
            return false;
        }

        @Override
        public void waitForSkipped() {
            for (ResultImplementation<T> result : results) {
                result.waitForSkipped();
            }
        }

        @Override
        public List<T> getResults() {
            List<T> ret = new LinkedList<>();
            for (ResultImplementation<T> result : results) {
                ret.addAll(result.getResults());
            }
            return ret;
        }

        @Override
        public int getTotalResultCount() {
            int ret = 0;
            for (ResultImplementation<T> result : results) {
                ret += result.getTotalResultCount();
            }
            return ret;
        }

        @Override
        public int getReturnedResultCount() {
            int ret = 0;
            for (ResultImplementation<T> result : results) {
                ret += result.getReturnedResultCount();
            }
            return ret;
        }
    }

   /**
     * One usage result.
     */
    public final static class ClassUsage {
        private final NBVersionInfo artifact;
        private final Set<String> classes;
        public ClassUsage(NBVersionInfo artifact, Set<String> classes) {
            this.artifact = artifact;
            this.classes = classes;
        }
        /**
         * @return artifact which refers to the named class
         */
        public NBVersionInfo getArtifact() {
            return artifact;
        }
        /**
         * @return a list of class FQNs within that artifact which do the referring (top-level classes only)
         */
        public Set<String> getClasses() {
            return classes;
        }
        @Override public String toString() {
            return "" + artifact + classes;
        }
    }
    
    private static interface QueryCall<T> {
        ResultImplementation<T> call(RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos);
    }
    
    private static <T> Result<T> getQueryResult(List<RepositoryInfo> repos, QueryCall<T> c) {
        List<ResultImplementation<T>> results = new LinkedList<>();
        if (repos == null) {
            repos = RepositoryPreferences.getInstance().getRepositoryInfos();
        }
        Map<RepositoryIndexQueryProvider, List<RepositoryInfo>> qp2Repo = getQueryProvider2Repo(repos);
        for(Map.Entry<RepositoryIndexQueryProvider, List<RepositoryInfo>> e : qp2Repo.entrySet()) {
            results.add(c.call(e.getKey(), e.getValue()));
        }
        return new Result<>(new CompositeResult<>(results));
    }

    private static Map<RepositoryIndexQueryProvider, List<RepositoryInfo>> getQueryProvider2Repo(List<RepositoryInfo> repos) {
        Collection<? extends RepositoryIndexQueryProvider> idxs = Lookup.getDefault().lookupAll(RepositoryIndexQueryProvider.class);
        Map<RepositoryIndexQueryProvider, List<RepositoryInfo>> qp2Repo = new HashMap<>();
        for (RepositoryInfo repo : repos) {
            for (RepositoryIndexQueryProvider idx : idxs) {
                if(idx.handlesRepository(repo)) {
                    List<RepositoryInfo> mappedRepos = qp2Repo.get(idx);
                    if(mappedRepos == null) {
                        mappedRepos = new LinkedList<>();
                        qp2Repo.put(idx, mappedRepos);
                    }
                    mappedRepos.add(repo);
                    break;
                }
            }
        }
        return qp2Repo;
    }
    
    /**
     * 
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<String> getGroupsResult(@NullAllowed List<RepositoryInfo> repos) {
        return getQueryResult(repos, (RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos) -> {
            BaseQueries baseQueries = queryProvider.getBaseQueries();
            assert baseQueries != null: "RepositoryIndexQueryProvider.getBaseQueries not allowed to return null";
            return baseQueries.getGroups(providerRepos);
        });
    }
    
    /**
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<NBVersionInfo> getRecordsResult(String groupId, String artifactId, String version, @NullAllowed List<RepositoryInfo> repos) {
        return getQueryResult(repos, (RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos) -> {
            BaseQueries baseQueries = queryProvider.getBaseQueries();
            assert baseQueries != null: "RepositoryIndexQueryProvider.getBaseQueries not allowed to return null";
            return baseQueries.getRecords(groupId, artifactId, version, providerRepos);
        });
    }
    
    /**
     * returns list of GAV (groupId:artifactId:version) of artifacts with the given packaging. Not adviseable to run on "jar" packaging or "pom".
     * @param packaging
     * @param repos
     * @return 
     * @since 2.28
     */
    public static Result<String> getGAVsForPackaging(String packaging, @NullAllowed List<RepositoryInfo> repos) {
        return getQueryResult(repos, (RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos) -> {
            BaseQueries baseQueries = queryProvider.getBaseQueries();
            assert baseQueries != null: "RepositoryIndexQueryProvider.getBaseQueries not allowed to return null";
            return baseQueries.getGAVsForPackaging(packaging, providerRepos);
        });
    }    

    /**
     * 
     * @param groupId
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<String> getArtifactsResult(String groupId, @NullAllowed List<RepositoryInfo> repos) {
        return getQueryResult(repos, (RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos) -> {
            BaseQueries baseQueries = queryProvider.getBaseQueries();
            assert baseQueries != null: "RepositoryIndexQueryProvider.getBaseQueries not allowed to return null";
            return baseQueries.getArtifacts(groupId, providerRepos);
        });
    }

    /**
     * 
     * @param groupId
     * @param artifactId
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<NBVersionInfo> getVersionsResult(String groupId, String artifactId, @NullAllowed List<RepositoryInfo> repos) {
        return getQueryResult(repos, (RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos) -> {
            BaseQueries baseQueries = queryProvider.getBaseQueries();
            assert baseQueries != null: "RepositoryIndexQueryProvider.getBaseQueries not allowed to return null";
            return baseQueries.getVersions(groupId, artifactId, providerRepos);
        });
    }

    /**
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<NBGroupInfo> findDependencyUsageResult(String groupId, String artifactId, String version, @NullAllowed List<RepositoryInfo> repos) {
        return getQueryResult(repos, (RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos) -> {
            DependencyInfoQueries dependencyInfoQueries = queryProvider.getDependencyInfoQueries();
            return dependencyInfoQueries != null ? 
                    dependencyInfoQueries.findDependencyUsageGroups(groupId, artifactId, version, providerRepos) :
                    new NullResultImpl<>();
        });        
    }
    
    /**
     * 
     * @param file
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<NBVersionInfo> findBySHA1Result(File file, @NullAllowed List<RepositoryInfo> repos) {
        try {
            String calculateChecksum = RepositoryUtil.calculateSHA1Checksum(file);
            return findBySHA1(calculateChecksum, repos);
        } catch (IOException ex) {
            Logger.getLogger(RepositoryQueries.class.getName()).log(Level.INFO, "Could not determine SHA-1 of " + file, ex);
        }
        return new Result<>(new NullResultImpl<>());
    }
        
    private static Result<NBVersionInfo> findBySHA1(String sha1, @NullAllowed List<RepositoryInfo> repos) {
        return getQueryResult(repos, (RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos) -> {
            ChecksumQueries checksumQueries = queryProvider.getChecksumQueries();
            return checksumQueries != null ? 
                    checksumQueries.findBySHA1(sha1, providerRepos) : 
                    new NullResultImpl<>();
        });
    }
    
    /**
     * 
     * @param className
     * @param repos
     * @return 
     * @throws BooleanQuery.TooManyClauses This runtime exception can be thrown if given class name is too
     * general and such search can't be executed as it would probably end with
     * OutOfMemoryException. Callers should either assure that no such dangerous
     * queries are constructed or catch BooleanQuery.TooManyClauses and act
     * accordingly, for example by telling user that entered text for
     * search is too general.
     * @since 2.9
     */
    public static Result<NBVersionInfo> findVersionsByClassResult(final String className, @NullAllowed List<RepositoryInfo> repos) {
        return getQueryResult(repos, (RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos) -> {
            ClassesQuery classesQuery = queryProvider.getClassesQuery();
            return classesQuery != null ? 
                    classesQuery.findVersionsByClass(className, providerRepos) : 
                    new NullResultImpl<>();
        });
    }

    /**
     * Search in Maven repositories which reads search parameters from
     * the <code>QueryRequest</code> object and adds the results to this
     * observable object incrementally as it searches one by one through
     * the registered repositories.
     * 
     * The query allows the observer of the QueryRequest object
     * to process the results incrementally.
     * 
     * If the requester loses the interest in additional results of this running
     * query, it should remove itself from the list of observers by calling
     * <code>queryRequest.deleteObserver(requester)</code>.
     * 
     * @throws BooleanQuery.TooManyClauses This runtime exception can be thrown if given class name is too
     * general and such search can't be executed as it would probably end with
     * OutOfMemoryException. Callers should either assure that no such dangerous
     * queries are constructed or catch BooleanQuery.TooManyClauses and act
     * accordingly, for example by telling user that entered text for
     * search is too general.
     */
    public static void findVersionsByClass(QueryRequest query) {
        //TODO first process the loaded ones, index and wait for finish of indexing of the unloaded ones..
        for (Iterator<RepositoryInfo> it1 = query.getRepositories().iterator(); it1.hasNext();) {
            RepositoryInfo repositoryInfo = it1.next();
            List<RepositoryInfo> repositoryInfoL = new ArrayList<>(1);
            repositoryInfoL.add(repositoryInfo);
            Result<NBVersionInfo> queryResult = getQueryResult(repositoryInfoL, (RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos) -> {
                ClassesQuery classesQuery = queryProvider.getClassesQuery();
                return classesQuery != null ? 
                        classesQuery.findVersionsByClass(query.getClassName(), providerRepos) : 
                        new NullResultImpl<>();
            });
            query.addResults(queryResult.getResults(), !it1.hasNext());
            // still someone waiting for results?
            if (query.countObservers() == 0)
                return;
        }
        if (!query.isFinished())
            query.addResults(null, true);
    }
    
    /**
     * Finds all usages of a given class.
     * The implementation may not provide results within the same artifact, or on classes in the JRE.
     * @param className the FQN of a class that might be used as an API
     * @param repos as usual (note that the implementation currently ignores remote repositories)
     * @return a list of usages
     * @since 2.9
     */
    public static Result<ClassUsage> findClassUsagesResult(String className, @NullAllowed List<RepositoryInfo> repos) {
        return getQueryResult(repos, (RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos) -> {
            ClassUsageQuery classUsageQuery = queryProvider.getClassUsageQuery();
            return classUsageQuery != null ? 
                    classUsageQuery.findClassUsages(className, repos) :
                    new NullResultImpl<>();
        });
    }

    /**
     * 
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<NBVersionInfo> findArchetypesResult(@NullAllowed List<RepositoryInfo> repos) {
        return getQueryResult(repos, (RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos) -> {
            ArchetypeQueries archetypeQueries = queryProvider.getArchetypeQueries();
            return archetypeQueries != null ? 
                    archetypeQueries.findArchetypes(repos) : 
                    new NullResultImpl<>();
        });
    }
    
    /**
     * 
     * @param groupId
     * @param prefix
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<String> filterPluginArtifactIdsResult(String groupId, String prefix, @NullAllowed List<RepositoryInfo> repos) {
        return getQueryResult(repos, (RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos) -> {
            BaseQueries baseQueries = queryProvider.getBaseQueries();
            assert baseQueries != null: "RepositoryIndexQueryProvider.getBaseQueries not allowed to return null";
            return baseQueries.filterPluginArtifactIds(groupId, prefix, providerRepos);
        });
    }

    /**
     * 
     * @param prefix
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<String> filterPluginGroupIdsResult(String prefix, @NullAllowed List<RepositoryInfo> repos) {
        return getQueryResult(repos, (RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos) -> {
            BaseQueries baseQueries = queryProvider.getBaseQueries();
            assert baseQueries != null: "RepositoryIndexQueryProvider.getBaseQueries not allowed to return null";
            return baseQueries.filterPluginGroupIds(prefix, providerRepos);
        });
    }

    /**
     * @throws BooleanQuery.TooManyClauses This runtime exception can be thrown if given query is too
     * general and such search can't be executed as it would probably end with
     * OutOfMemoryException. Callers should either assure that no such dangerous
     * queries are constructed or catch BooleanQuery.TooManyClauses and act
     * accordingly, for example by telling user that entered text for
     * search is too general.
     * 
     * @param fields
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<NBVersionInfo> findResult(List<QueryField> fields, @NullAllowed List<RepositoryInfo> repos) {
        return getQueryResult(repos, (RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos) -> {
            GenericFindQuery genericFindQuery = queryProvider.getGenericFindQuery();
            return genericFindQuery != null ? 
                    genericFindQuery.find(fields, providerRepos) : 
                    new NullResultImpl<>();
        });
    }

    /**
     * Search in Maven repositories which reads search parameters from
     * the <code>QueryRequest</code> object and adds the results to this
     * observable object incrementally as it searches one by one through
     * the registered repositories.
     * 
     * The query allows the observer of the QueryRequest object
     * to process the results incrementally.
     * 
     * If the requester loses the interest in additional results of this running
     * query, it should remove itself from the list of observers by calling
     * <code>queryRequest.deleteObserver(requester)</code>.
     * 
     * @throws org.apache.lucene.search.BooleanQuery.TooManyClauses This runtime exception can be thrown if given query is too
     * general and such search can't be executed as it would probably end with
     * OutOfMemoryException. Callers should either assure that no such dangerous
     * queries are constructed or catch BooleanQuery.TooManyClauses and act
     * accordingly, for example by telling user that entered text for
     * search is too general.
     */
    public static void find(QueryRequest query) {
        for (Iterator<RepositoryInfo> it1 = query.getRepositories().iterator(); it1.hasNext();) {
            RepositoryInfo repositoryInfo = it1.next();
            List<RepositoryInfo> repositoryInfoL = new ArrayList<>(1);
            repositoryInfoL.add(repositoryInfo);
            Result<NBVersionInfo> qeuryResult = 
                getQueryResult(repositoryInfoL, (RepositoryIndexQueryProvider queryProvider, List<RepositoryInfo> providerRepos) -> {
                    GenericFindQuery genericFindQuery = queryProvider.getGenericFindQuery();
                    return genericFindQuery != null ? 
                            genericFindQuery.find(query.getQueryFields(), providerRepos) : 
                            new NullResultImpl<>();
            });
            query.addResults(qeuryResult.getResults(), !it1.hasNext());
            // still someone waiting for results?
            if (query.countObservers() == 0)
                return;
        }
        if (!query.isFinished())
            query.addResults(null, true);
    }
    
    public static @NonNull List<RepositoryInfo> getLoadedContexts() {
        List<RepositoryInfo> toRet = new ArrayList<>();
        List<RepositoryInfo> repos = RepositoryPreferences.getInstance().getRepositoryInfos();
        Map<RepositoryIndexQueryProvider, List<RepositoryInfo>> i2r = getQueryProvider2Repo(repos);
        for(Entry<RepositoryIndexQueryProvider, List<RepositoryInfo>> e : i2r.entrySet()) {
            ContextLoadedQuery clq = e.getKey().getContextLoadedQuery();
            assert clq != null;
            toRet.addAll(clq.getLoaded(repos));
        }
        return toRet;
    }

}
