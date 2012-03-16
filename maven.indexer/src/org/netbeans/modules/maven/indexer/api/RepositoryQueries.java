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
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    
    
    private static @NonNull BaseQueries findBaseQueries() {
        return RepositoryIndexer.findImplementation().getCapabilityLookup().lookup(BaseQueries.class);
    }

    /**
     * 
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<String> getGroupsResult(@NullAllowed List<RepositoryInfo> repos) {
        return findBaseQueries().getGroups(repos);
    }
    
    /**
     * 
     * @param prefix
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<String> filterGroupIdsResult(String prefix, @NullAllowed List<RepositoryInfo> repos) {
        return findBaseQueries().filterGroupIds(prefix, repos);
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
        return findBaseQueries().getRecords(groupId, artifactId, version, repos);
    }

    
    /**
     * 
     * @param groupId
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<String> getArtifactsResult(String groupId, @NullAllowed List<RepositoryInfo> repos) {
        return findBaseQueries().getArtifacts(groupId, repos);
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
        return findBaseQueries().getVersions(groupId, artifactId, repos);
    }

    
    private static @NonNull DependencyInfoQueries findDIQ() {
        return RepositoryIndexer.findImplementation().getCapabilityLookup().lookup(DependencyInfoQueries.class);
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
        Result<NBVersionInfo> res = findDIQ().findDependencyUsage(groupId, artifactId, version, repos);
        convertToNBGroupInfo(res.getResults(),
                groupMap, artifactMap, groupInfos);
        if (res.isPartial()) {
            result.markAsPartial();
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
    
    private static ChecksumQueries findChecksumQueries() {
        return RepositoryIndexer.findImplementation().getCapabilityLookup().lookup(ChecksumQueries.class);
    }    

    private static Result<NBVersionInfo> findBySHA1(String sha1, @NullAllowed List<RepositoryInfo> repos) {
        ChecksumQueries cq = findChecksumQueries();
        if (cq != null) {
            return findChecksumQueries().findBySHA1(sha1, repos);
        } else {
            //this is here only because of ClassPathProviderImplTest
            return new Result<NBVersionInfo>();
        }
    }
    
    private static @NonNull ClassesQuery findClassesQueries() {
        return RepositoryIndexer.findImplementation().getCapabilityLookup().lookup(ClassesQuery.class);
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
        return findClassesQueries().findVersionsByClass(className, repos);
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
            List<RepositoryInfo> repositoryInfoL = new ArrayList<RepositoryInfo>(1);
            repositoryInfoL.add(repositoryInfo);
            query.addResults(findClassesQueries().findVersionsByClass(query.getClassName(), repositoryInfoL).getResults(), !it1.hasNext());
            // still someone waiting for results?
            if (query.countObservers() == 0)
                return;
        }
        if (!query.isFinished())
            query.addResults(null, true);
    }
    
    private static @NonNull ClassUsageQuery findClassUsageQuery() {
        return RepositoryIndexer.findImplementation().getCapabilityLookup().lookup(ClassUsageQuery.class);        
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
        return findClassUsageQuery().findClassUsages(className, repos);
    }

    
    private static @NonNull ArchetypeQueries findArchetypeQueries() {
        return RepositoryIndexer.findImplementation().getCapabilityLookup().lookup(ArchetypeQueries.class);
    }
    
    /**
     * 
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<NBVersionInfo> findArchetypesResult(@NullAllowed List<RepositoryInfo> repos) {
        return findArchetypeQueries().findArchetypes(repos);
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
        return findBaseQueries().filterPluginArtifactIds(groupId, prefix, repos);
    }

    
    /**
     * 
     * @param prefix
     * @param repos
     * @return 
     * @since 2.9
     */
    public static Result<String> filterPluginGroupIdsResult(String prefix, @NullAllowed List<RepositoryInfo> repos) {
        return findBaseQueries().filterPluginGroupIds(prefix, repos);
    }


    private static @NonNull GenericFindQuery findFindQuery() {
        return RepositoryIndexer.findImplementation().getCapabilityLookup().lookup(GenericFindQuery.class);
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
        return findFindQuery().find(fields, repos);
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
            List<RepositoryInfo> repositoryInfoL = new ArrayList<RepositoryInfo>(1);
            repositoryInfoL.add(repositoryInfo);
            query.addResults(findFindQuery().find(query.getQueryFields(), repositoryInfoL).getResults(), !it1.hasNext());
            // still someone waiting for results?
            if (query.countObservers() == 0)
                return;
        }
        if (!query.isFinished())
            query.addResults(null, true);
    }
    
    public static @NonNull List<RepositoryInfo> getLoadedContexts() {
        List<RepositoryInfo> toRet = new ArrayList<RepositoryInfo>();
        ContextLoadedQuery clq = RepositoryIndexer.findImplementation().getCapabilityLookup().lookup(ContextLoadedQuery.class);
        toRet.addAll(clq.getLoaded(RepositoryPreferences.getInstance().getRepositoryInfos()));
        return toRet;
    }

    
    public static Result<String> filterArtifactIdForGroupIdResult(String groupId, String prefix, @NullAllowed List<RepositoryInfo> repos) {
        return findBaseQueries().filterArtifactIdForGroupId(groupId, prefix, repos);
    }

}
