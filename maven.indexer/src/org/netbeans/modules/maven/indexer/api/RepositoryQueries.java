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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.indexer.spi.ArchetypeQueries;
import org.netbeans.modules.maven.indexer.spi.BaseQueries;
import org.netbeans.modules.maven.indexer.spi.ChecksumQueries;
import org.netbeans.modules.maven.indexer.spi.ClassUsageQuery;
import org.netbeans.modules.maven.indexer.spi.ClassesQuery;
import org.netbeans.modules.maven.indexer.spi.ContextLoadedQuery;
import org.netbeans.modules.maven.indexer.spi.DependencyInfoQueries;
import org.netbeans.modules.maven.indexer.spi.GenericFindQuery;
import org.netbeans.modules.maven.indexer.spi.RepositoryIndexerImplementation;

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
     * @since 2.9
     */
    public final static class Result<T> {
        private boolean skipped = false;
        private List<T> results = new ArrayList<T>();
        
        /**
         * returns true is one or more indexes were skipped, eg because the indexing was taking place.
         * @return 
         */
        public boolean isPartial() {
            return skipped;
        }
        
        /**
         * used internally by the repository indexing/searching engine(s) to mark the result as partially skipped
         */
        public void markAsPartial() {
            skipped = true;
        }
        
        public List<T> getResults() {
            return results;
        }
    }    

    /**
     * 
     * @param repos
     * @return
     * @deprecated use getGroupsResult() method
     */
    @Deprecated
    public static Set<String> getGroups(@NullAllowed List<RepositoryInfo> repos) {
        final Set<String> toRet = new TreeSet<String>();
        toRet.addAll(getGroupsResult(repos).getResults());
        return toRet;
    }    

    /**
     * 
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<String> getGroupsResult(@NullAllowed List<RepositoryInfo> repos) {
        Collection<List<RepositoryInfo>> all = splitReposByType(repos);
        final Result<String> result = new Result<String>();
        Set<String> toRet = new TreeSet<String>();
        for (List<RepositoryInfo> rps : all) {
            RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(rps.get(0));
            if (impl != null) {
                BaseQueries bq = impl.getCapabilityLookup().lookup(BaseQueries.class);
                assert bq != null : "All RepositoryIndexerImplementation need to define BaseQueries:" + impl.getType() + " : " + impl.getClass();
                Result<String> res = bq.getGroups(rps);
                toRet.addAll(res.getResults());
                if (res.isPartial()) {
                    result.markAsPartial();
            }
        }
    }
        result.getResults().addAll(toRet);
        return result;
    }
    
    /**
     * 
     * @param prefix
     * @param repos
     * @return
     * @deprecated use filterGroupIdsResult() method
     */
    @Deprecated
    public static Set<String> filterGroupIds(String prefix, @NullAllowed List<RepositoryInfo> repos) {
        final Set<String> toRet = new TreeSet<String>();
        toRet.addAll(filterGroupIdsResult(prefix, repos).getResults());
        return toRet;
    }
    
    /**
     * 
     * @param prefix
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<String> filterGroupIdsResult(String prefix, @NullAllowed List<RepositoryInfo> repos) {
        Collection<List<RepositoryInfo>> all = splitReposByType(repos);
        final Result<String> result = new Result<String>();
        Set<String> toRet = new TreeSet<String>();
        for (List<RepositoryInfo> rps : all) {
            RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(rps.get(0));
            if (impl != null) {
                BaseQueries bq = impl.getCapabilityLookup().lookup(BaseQueries.class);
                assert bq != null : "All RepositoryIndexerImplementation need to define BaseQueries:" + impl.getType() + " : " + impl.getClass();
                Result<String> res = bq.filterGroupIds(prefix, rps);
                toRet.addAll(res.getResults());
                if (res.isPartial()) {
                    result.markAsPartial();
            }
        }
    }
        result.getResults().addAll(toRet);
        return result;
    }

    /**
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @param repos
     * @return
     * @deprecated use the getRecordsResult() method
     */
    @Deprecated
    public static List<NBVersionInfo> getRecords(String groupId, String artifactId, String version, @NullAllowed List<RepositoryInfo> repos) {
        return getRecordsResult(groupId, artifactId, version, repos).getResults();
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
        Collection<List<RepositoryInfo>> all = splitReposByType(repos);
        List<NBVersionInfo> toRet = new ArrayList<NBVersionInfo>();
        final Result<NBVersionInfo> result = new Result<NBVersionInfo>();
        for (List<RepositoryInfo> rps : all) {
            RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(rps.get(0));
            if (impl != null) {
                BaseQueries bq = impl.getCapabilityLookup().lookup(BaseQueries.class);
                assert bq != null : "All RepositoryIndexerImplementation need to define BaseQueries:" + impl.getType() + " : " + impl.getClass();
                Result<NBVersionInfo> res = bq.getRecords(groupId, artifactId, version, rps);
                toRet.addAll(res.getResults());
                if (res.isPartial()) {
                    result.markAsPartial();
            }
        }
        }
        Collections.sort(toRet);
        result.getResults().addAll(toRet);
        return result;        
    }

    /**
     * 
     * @param groupId
     * @param repos
     * @return
     * @deprecated use the getArtifactsResult() variant
     */
    @Deprecated
    public static Set<String> getArtifacts(String groupId, @NullAllowed List<RepositoryInfo> repos) {
        final Set<String> toRet = new TreeSet<String>();
        toRet.addAll(getArtifactsResult(groupId, repos).getResults());
        return toRet;
    }
    
    /**
     * 
     * @param groupId
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<String> getArtifactsResult(String groupId, @NullAllowed List<RepositoryInfo> repos) {
        Collection<List<RepositoryInfo>> all = splitReposByType(repos);
        final Result<String> result = new Result<String>();
        Set<String> toRet = new TreeSet<String>();
        for (List<RepositoryInfo> rps : all) {
            RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(rps.get(0));
            if (impl != null) {
                BaseQueries bq = impl.getCapabilityLookup().lookup(BaseQueries.class);
                assert bq != null : "All RepositoryIndexerImplementation need to define BaseQueries:" + impl.getType() + " : " + impl.getClass();
                Result<String> res = bq.getArtifacts(groupId, rps);
                toRet.addAll(res.getResults());
                if (res.isPartial()) {
                    result.markAsPartial();
            }
        }
    }
        result.getResults().addAll(toRet);
        return result;
    }

    /**
     * 
     * @param groupId
     * @param artifactId
     * @param repos
     * @return
     * @deprecated use the getVersionsResult() variant
     */
    @Deprecated
    public static List<NBVersionInfo> getVersions(String groupId, String artifactId, @NullAllowed List<RepositoryInfo> repos) {
        return getVersionsResult(groupId, artifactId, repos).getResults();
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
        Collection<List<RepositoryInfo>> all = splitReposByType(repos);
        List<NBVersionInfo> toRet = new ArrayList<NBVersionInfo>();
        final Result<NBVersionInfo> result = new Result<NBVersionInfo>();
        for (List<RepositoryInfo> rps : all) {
            RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(rps.get(0));
            if (impl != null) {
                BaseQueries bq = impl.getCapabilityLookup().lookup(BaseQueries.class);
                assert bq != null : "All RepositoryIndexerImplementation need to define BaseQueries:" + impl.getType() + " : " + impl.getClass();
                Result<NBVersionInfo> res = bq.getVersions(groupId, artifactId, rps);
                toRet.addAll(res.getResults());
                if (res.isPartial()) {
                    result.markAsPartial();
            }
        }
        }
        Collections.sort(toRet);
        result.getResults().addAll(toRet);
        return result;
    }

    /**
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @param repos
     * @return
     * @deprecated use the findDependencyUsageResult() variant
     */
    @Deprecated
    public static List<NBGroupInfo> findDependencyUsage(String groupId, String artifactId, String version, @NullAllowed List<RepositoryInfo> repos) {
        return findDependencyUsageResult(groupId, artifactId, version, repos).getResults();
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
        //tempmaps
        Map<String, NBGroupInfo> groupMap = new HashMap<String, NBGroupInfo>();
        Map<String, NBArtifactInfo> artifactMap = new HashMap<String, NBArtifactInfo>();
        List<NBGroupInfo> groupInfos = new ArrayList<NBGroupInfo>();
        final Result<NBGroupInfo> result = new Result<NBGroupInfo>();
        Collection<List<RepositoryInfo>> all = splitReposByType(repos);
        for (List<RepositoryInfo> rps : all) {
            RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(rps.get(0));
            if (impl != null) {
                DependencyInfoQueries dq = impl.getCapabilityLookup().lookup(DependencyInfoQueries.class);
                if (dq != null) {
                    Result<NBVersionInfo> res = dq.findDependencyUsage(groupId, artifactId, version, rps);
                    convertToNBGroupInfo(res.getResults(),
                            groupMap, artifactMap, groupInfos);
                    if (res.isPartial()) {
                        result.markAsPartial();
                }
            }
        }
    }
        result.getResults().addAll(groupInfos);
        return result;
    }
    
    private static void convertToNBGroupInfo(Collection<NBVersionInfo> artifactInfos, 
                                      Map<String, NBGroupInfo> groupMap, 
                                      Map<String, NBArtifactInfo> artifactMap,
                                      List<NBGroupInfo> groupInfos) {
        for (NBVersionInfo ai : artifactInfos) {
            String groupId = ai.getGroupId();
            String artId = ai.getArtifactId();

            NBGroupInfo ug = groupMap.get(groupId);
            if (ug == null) {
                ug = new NBGroupInfo(groupId);
                groupInfos.add(ug);
                groupMap.put(groupId, ug);
            }
            NBArtifactInfo ua = artifactMap.get(artId);
            if (ua == null) {
                ua = new NBArtifactInfo(artId);
                ug.addArtifactInfo(ua);
                artifactMap.put(artId, ua);
            }
            ua.addVersionInfo(ai);
        }
    }
    
    /**
     * 
     * @param file
     * @param repos
     * @return 
     * @deprecated use the findBySHA1Result() variant
     */
    @Deprecated
    public static List<NBVersionInfo> findBySHA1(File file, @NullAllowed List<RepositoryInfo> repos) {
        return findBySHA1Result(file, repos).getResults();
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
        return new Result<NBVersionInfo>();
        
    }

    private static Result<NBVersionInfo> findBySHA1(String sha1, @NullAllowed List<RepositoryInfo> repos) {
        Collection<List<RepositoryInfo>> all = splitReposByType(repos);
        List<NBVersionInfo> toRet = new ArrayList<NBVersionInfo>();
        final Result<NBVersionInfo> result = new Result<NBVersionInfo>();
        for (List<RepositoryInfo> rps : all) {
            RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(rps.get(0));
            if (impl != null) {
                ChecksumQueries chq = impl.getCapabilityLookup().lookup(ChecksumQueries.class);
                if (chq != null) {
                    Result<NBVersionInfo> res = chq.findBySHA1(sha1, rps);
                    toRet.addAll(res.getResults());
                    if (res.isPartial()) {
                        result.markAsPartial();
                }
            }
        }
        }
        Collections.sort(toRet);
        result.getResults().addAll(toRet);
        return result;
    }
    
    /**
     * @throws BooleanQuery.TooManyClauses This runtime exception can be thrown if given class name is too
     * general and such search can't be executed as it would probably end with
     * OutOfMemoryException. Callers should either assure that no such dangerous
     * queries are constructed or catch BooleanQuery.TooManyClauses and act
     * accordingly, for example by telling user that entered text for
     * search is too general.
     * @deprecated use the findVersionsByClassResult() variant
     */
    @Deprecated
    public static List<NBVersionInfo> findVersionsByClass(final String className, @NullAllowed List<RepositoryInfo> repos) {
        return findVersionsByClassResult(className, repos).getResults();
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
        Collection<List<RepositoryInfo>> all = splitReposByType(repos);
        List<NBVersionInfo> toRet = new ArrayList<NBVersionInfo>();
        final Result<NBVersionInfo> result = new Result<NBVersionInfo>();
        for (List<RepositoryInfo> rps : all) {
            RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(rps.get(0));
            if (impl != null) {
                ClassesQuery chq = impl.getCapabilityLookup().lookup(ClassesQuery.class);
                if (chq != null) {
                    Result<NBVersionInfo> res = chq.findVersionsByClass(className, rps);
                    toRet.addAll(res.getResults());
                    if (res.isPartial()) {
                        result.markAsPartial();
                }
            }
        }
        }
        Collections.sort(toRet);
        result.getResults().addAll(toRet);
        return result;
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
        Collection<List<RepositoryInfo>> all = splitReposByType(query.getRepositories());
        for (Iterator<List<RepositoryInfo>> it = all.iterator(); it.hasNext();) {
            List<RepositoryInfo> rps = it.next();
            for (Iterator<RepositoryInfo> it1 = rps.iterator(); it1.hasNext();) {
                RepositoryInfo repositoryInfo = it1.next();
                RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(repositoryInfo);
                if (impl != null) {
                    ClassesQuery chq = impl.getCapabilityLookup().lookup(ClassesQuery.class);
                    if (chq != null) {
                        List<RepositoryInfo> repositoryInfoL = new ArrayList<RepositoryInfo>(1);
                        repositoryInfoL.add(repositoryInfo);
                        query.addResults(chq.findVersionsByClass(query.getClassName(), repositoryInfoL).getResults(), !it1.hasNext() && !it.hasNext());
                    } else {
                        query.addResults(null, !it1.hasNext() && !it.hasNext());
                    }
                }
                // still someone waiting for results?
                if (query.countObservers() == 0)
                    return;
            }
        }
        if (!query.isFinished())
            query.addResults(null, true);
    }

    /**
     * 
     * @param className
     * @param repos
     * @return
     * @deprecated use the findClassUsagesResult() variant
     * @since 1.17
     */
    @Deprecated
    public static List<ClassUsageQuery.ClassUsageResult> findClassUsages(String className, @NullAllowed List<RepositoryInfo> repos) {    
        return findClassUsagesResult(className, repos).getResults();
    }
    /**
     * Finds all usages of a given class.
     * The implementation may not provide results within the same artifact, or on classes in the JRE.
     * @param className the FQN of a class that might be used as an API
     * @param repos as usual (note that the implementation currently ignores remote repositories)
     * @return a list of usages
     * @since 2.9
     */
    public static Result<ClassUsageQuery.ClassUsageResult> findClassUsagesResult(String className, @NullAllowed List<RepositoryInfo> repos) {
        final Result<ClassUsageQuery.ClassUsageResult> result = new Result<ClassUsageQuery.ClassUsageResult>();
        for (List<RepositoryInfo> rps : splitReposByType(repos)) {
            RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(rps.get(0));
            ClassUsageQuery q = impl.getCapabilityLookup().lookup(ClassUsageQuery.class);
            if (q != null) {
                Result<ClassUsageQuery.ClassUsageResult> r = q.findClassUsages(className, rps);
                result.getResults().addAll(r.getResults());
                if (r.isPartial()) {
                    result.markAsPartial();
            }
        }
        }
        return result;
    }

    /**
     * 
     * @param repos
     * @return 
     * @deprecated  use the findArchetypesResult() variant
     */
    @Deprecated
    public static List<NBVersionInfo> findArchetypes(@NullAllowed List<RepositoryInfo> repos) {
        return findArchetypesResult(repos).getResults();
    }
    
    /**
     * 
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<NBVersionInfo> findArchetypesResult(@NullAllowed List<RepositoryInfo> repos) {
        Collection<List<RepositoryInfo>> all = splitReposByType(repos);
        List<NBVersionInfo> toRet = new ArrayList<NBVersionInfo>();
        final Result<NBVersionInfo> result = new Result<NBVersionInfo>();
        for (List<RepositoryInfo> rps : all) {
            RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(rps.get(0));
            if (impl != null) {
                ArchetypeQueries aq = impl.getCapabilityLookup().lookup(ArchetypeQueries.class);
                if (aq != null) {
                    Result<NBVersionInfo> res = aq.findArchetypes(rps);
                    toRet.addAll(res.getResults());
                    if (res.isPartial()) {
                        result.markAsPartial();
                }
            }
        }
        }
        Collections.sort(toRet);
        result.getResults().addAll(toRet);
        return result;    }
    
    /**
     * 
     * @param groupId
     * @param prefix
     * @param repos
     * @return
     * @deprecated use the filterPluginArtifactIdsResult() variant
     */
    @Deprecated
    public static Set<String> filterPluginArtifactIds(String groupId, String prefix, @NullAllowed List<RepositoryInfo> repos) {
        Set<String> toRet = new TreeSet<String>();
        toRet.addAll(filterPluginArtifactIdsResult(groupId, prefix, repos).getResults());
        return toRet;
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
        Collection<List<RepositoryInfo>> all = splitReposByType(repos);
        Set<String> toRet = new TreeSet<String>();
        final Result<String> result = new Result<String>();
        
        for (List<RepositoryInfo> rps : all) {
            RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(rps.get(0));
            if (impl != null) {
                BaseQueries bq = impl.getCapabilityLookup().lookup(BaseQueries.class);
                assert bq != null : "All RepositoryIndexerImplementation need to define BaseQueries:" + impl.getType() + " : " + impl.getClass();
                Result<String> r = bq.filterPluginArtifactIds(groupId, prefix, rps);
                toRet.addAll(r.getResults());
                if (r.isPartial()) {
                    result.markAsPartial();
            }
        }
    }
        result.getResults().addAll(toRet);
        return result;
    }

    /**
     * 
     * @param prefix
     * @param repos
     * @return
     * @deprecated use the filterPluginGroupIdsResult() variant
     */
    @Deprecated
    public static Set<String> filterPluginGroupIds(String prefix, @NullAllowed List<RepositoryInfo> repos) {
        Set<String> toRet = new TreeSet<String>();
        toRet.addAll(filterPluginGroupIdsResult(prefix, repos).getResults());
        return toRet;
    }
    
    /**
     * 
     * @param prefix
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<String> filterPluginGroupIdsResult(String prefix, @NullAllowed List<RepositoryInfo> repos) {
        Collection<List<RepositoryInfo>> all = splitReposByType(repos);
        Set<String> toRet = new TreeSet<String>();
        final Result<String> result = new Result<String>();
        for (List<RepositoryInfo> rps : all) {
            RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(rps.get(0));
            if (impl != null) {
                BaseQueries bq = impl.getCapabilityLookup().lookup(BaseQueries.class);
                assert bq != null : "All RepositoryIndexerImplementation need to define BaseQueries:" + impl.getType() + " : " + impl.getClass();
                Result<String> r = bq.filterPluginGroupIds(prefix, rps);
                toRet.addAll(r.getResults());
                if (r.isPartial()) {
                    result.markAsPartial();
            }
        }
    }
        result.getResults().addAll(toRet);
        return result;
    }
    
    /**
     * @throws BooleanQuery.TooManyClauses This runtime exception can be thrown if given query is too
     * general and such search can't be executed as it would probably end with
     * OutOfMemoryException. Callers should either assure that no such dangerous
     * queries are constructed or catch BooleanQuery.TooManyClauses and act
     * accordingly, for example by telling user that entered text for
     * search is too general.
     * @deprecated use the findResult() variant
     */
    @Deprecated
    public static List<NBVersionInfo> find(List<QueryField> fields, @NullAllowed List<RepositoryInfo> repos) {
        return findResult(fields, repos).getResults();
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
        Collection<List<RepositoryInfo>> all = splitReposByType(repos);
        List<NBVersionInfo> toRet = new ArrayList<NBVersionInfo>();
        final Result<NBVersionInfo> result = new Result<NBVersionInfo>();
        for (List<RepositoryInfo> rps : all) {
            RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(rps.get(0));
            if (impl != null) {
                GenericFindQuery gfq = impl.getCapabilityLookup().lookup(GenericFindQuery.class);
                if (gfq != null) {
                    Result<NBVersionInfo> res = gfq.find(fields, rps);
                    toRet.addAll(res.getResults());
                    if (res.isPartial()) {
                        result.markAsPartial();
                }
            }
        }
        }
        Collections.sort(toRet);
        result.getResults().addAll(toRet);
        return result;
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
        Collection<List<RepositoryInfo>> all = splitReposByType(query.getRepositories());
        for (Iterator<List<RepositoryInfo>> it = all.iterator(); it.hasNext();) {
            List<RepositoryInfo> rps = it.next();
            for (Iterator<RepositoryInfo> it1 = rps.iterator(); it1.hasNext();) {
                RepositoryInfo repositoryInfo = it1.next();
                RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(repositoryInfo);
                if (impl != null) {
                    GenericFindQuery gfq = impl.getCapabilityLookup().lookup(GenericFindQuery.class);
                    if (gfq != null) {
                        List<RepositoryInfo> repositoryInfoL = new ArrayList<RepositoryInfo>(1);
                        repositoryInfoL.add(repositoryInfo);
                        query.addResults(gfq.find(query.getQueryFields(), repositoryInfoL).getResults(), !it1.hasNext() && !it.hasNext());
                    } else {
                        query.addResults(null, !it1.hasNext() && !it.hasNext());
                    }
                }
                // still someone waiting for results?
                if (query.countObservers() == 0)
                    return;
            }
        }
        if (!query.isFinished())
            query.addResults(null, true);
    }
    
    public static @NonNull List<RepositoryInfo> getLoadedContexts() {
        Collection<List<RepositoryInfo>> all = splitReposByType(RepositoryPreferences.getInstance().getRepositoryInfos());
        List<RepositoryInfo> toRet = new ArrayList<RepositoryInfo>();
        for (List<RepositoryInfo> rps : all) {
            RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(rps.get(0));
            if (impl != null) {
                ContextLoadedQuery clq = impl.getCapabilityLookup().lookup(ContextLoadedQuery.class);
                if (clq != null) {
                    toRet.addAll(clq.getLoaded(rps));
                }
            }
        }
        return toRet;
    }

    /**
     * 
     * @param groupId
     * @param prefix
     * @param repos
     * @return
     * @deprecated use the filterArtifactIdForGroupIdResult() variant
     */
    @Deprecated
    public static Set<String> filterArtifactIdForGroupId(String groupId, String prefix, @NullAllowed List<RepositoryInfo> repos) {
        Set<String> toRet = new TreeSet<String>();
        toRet.addAll(filterArtifactIdForGroupIdResult(groupId, prefix, repos).getResults());
        return toRet;
    }
    
    public static Result<String> filterArtifactIdForGroupIdResult(String groupId, String prefix, @NullAllowed List<RepositoryInfo> repos) {
        Collection<List<RepositoryInfo>> all = splitReposByType(repos);
        Set<String> toRet = new TreeSet<String>();
        final Result<String> result = new Result<String>();
        for (List<RepositoryInfo> rps : all) {
            RepositoryIndexerImplementation impl = RepositoryIndexer.findImplementation(rps.get(0));
            if (impl != null) {
                BaseQueries bq = impl.getCapabilityLookup().lookup(BaseQueries.class);
                assert bq != null : "All RepositoryIndexerImplementation need to define BaseQueries:" + impl.getType() + " : " + impl.getClass();
                Result<String> r = bq.filterArtifactIdForGroupId(groupId, prefix, rps);
                toRet.addAll(r.getResults());
                if (r.isPartial()) {
                    result.markAsPartial();
            }
        }
    }
        result.getResults().addAll(toRet);
        return result;
    }

    private static @NonNull Collection<List<RepositoryInfo>> splitReposByType(@NullAllowed List<RepositoryInfo> repos) {
        if (repos == null) {
            repos = getLoadedContexts();
        }
        Map<String, List<RepositoryInfo>> toRet = new HashMap<String, List<RepositoryInfo>>();
        for (RepositoryInfo info : repos) {
            String type = info.getType();
            List<RepositoryInfo> list = toRet.get(type);
            if (list == null) {
                list = new ArrayList<RepositoryInfo>();
                toRet.put(type, list);
            }
            list.add(info);
        }
        return toRet.values();
    }

}
