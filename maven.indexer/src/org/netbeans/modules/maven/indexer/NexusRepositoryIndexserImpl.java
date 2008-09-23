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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.indexer;

import hidden.org.codehaus.plexus.util.FileUtils;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidArtifactRTException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryUtil;
import org.netbeans.modules.maven.indexer.spi.ArchetypeQueries;
import org.netbeans.modules.maven.indexer.spi.BaseQueries;
import org.netbeans.modules.maven.indexer.spi.ChecksumQueries;
import org.netbeans.modules.maven.indexer.spi.ClassesQuery;
import org.netbeans.modules.maven.indexer.spi.DependencyInfoQueries;
import org.netbeans.modules.maven.indexer.spi.GenericFindQuery;
import org.netbeans.modules.maven.indexer.spi.RepositoryIndexerImplementation;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.sonatype.nexus.index.ArtifactAvailablility;
import org.sonatype.nexus.index.ArtifactContextProducer;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactInfoGroup;
import org.sonatype.nexus.index.GGrouping;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.ArtifactIndexingContext;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.creator.AbstractIndexCreator;
import org.sonatype.nexus.index.creator.IndexCreator;
import org.sonatype.nexus.index.creator.JarFileContentsIndexCreator;
import org.sonatype.nexus.index.creator.MinimalArtifactInfoIndexCreator;
import org.sonatype.nexus.index.updater.IndexUpdater;

/**
 *
 * @author Anuradha G
 */
public class NexusRepositoryIndexserImpl implements RepositoryIndexerImplementation,
        BaseQueries, ChecksumQueries, ArchetypeQueries, DependencyInfoQueries,
        ClassesQuery, GenericFindQuery {

    private ArtifactRepository repository;
    private NexusIndexer indexer;
    private IndexUpdater remoteIndexUpdater;
    private ArtifactContextProducer contextProducer;
    /*Indexer Keys*/
    private static final String NB_DEPENDENCY_GROUP = "nbdg"; //NOI18N
    private static final String NB_DEPENDENCY_ARTIFACT = "nbda"; //NOI18N
    private static final String NB_DEPENDENCY_VERSION = "nbdv"; //NOI18N
    /*logger*/
    private static final Logger LOGGER =
            Logger.getLogger("org.netbeans.modules.maven.indexer.RepositoryIndexer");//NOI18N
    /*custom Index creators*/
    public static final List<? extends IndexCreator> NB_INDEX = Arrays.asList(
            new MinimalArtifactInfoIndexCreator(),
            new JarFileContentsIndexCreator(),
            new NbIndexCreator());
    /**
     * any reads, writes from/to index shal be done under mutex access.
     */
    static final Mutex MUTEX = new Mutex();
    private Lookup lookup;

    //#138102
    public static String createLocalRepositoryPath(FileObject fo) {
        return EmbedderFactory.getProjectEmbedder().getLocalRepository().getBasedir();
    }

    public NexusRepositoryIndexserImpl() {
        //to prevent MaxClauseCount exception (will investigate better way)
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);

        try {
            PlexusContainer embedder;
            ContainerConfiguration config = new DefaultContainerConfiguration();
            embedder = new DefaultPlexusContainer(config);

            repository = EmbedderFactory.getProjectEmbedder().getLocalRepository();
            indexer = (NexusIndexer) embedder.lookup(NexusIndexer.class);
            remoteIndexUpdater = (IndexUpdater) embedder.lookup(IndexUpdater.class);
            contextProducer = (ArtifactContextProducer) embedder.lookup(ArtifactContextProducer.class);
        } catch (ComponentLookupException ex) {
            Exceptions.printStackTrace(ex);
        } catch (PlexusContainerException ex) {
            Exceptions.printStackTrace(ex);
        }
        lookup = Lookups.singleton(this);
    }

    public String getType() {
        return RepositoryPreferences.TYPE_NEXUS;
    }

    public Lookup getCapabilityLookup() {
        return lookup;
    }

    //always call from mutex.writeAccess
    //TODO can the process be made faster by only loading context when missing and
    // only unloading when not wanted?
    private void loadIndexingContext(final RepositoryInfo... repoids) throws IOException {
        assert MUTEX.isWriteAccess();
        for (RepositoryInfo info : repoids) {
            LOGGER.finer("Loading Context :" + info.getId());//NOI18N
            if (info.isLocal() || info.isRemoteDownloadable()) {
                File loc = new File(getDefaultIndexLocation(), info.getId()); // index folder
                try {
                    indexer.addIndexingContextForced(
                            info.getId(), // context id
                            info.getId(), // repository id
                            info.isLocal() ? new File(info.getRepositoryPath()) : null, // repository folder
                            loc,
                            info.isRemoteDownloadable() ? info.getRepositoryUrl() : null, // repositoryUrl
                            info.isRemoteDownloadable() ? info.getIndexUpdateUrl() : null, // index update url
                            NB_INDEX);
                } catch (IOException ex) {
                    LOGGER.info("Found a broken index at " + loc.getAbsolutePath()); //NOI18N
                    LOGGER.log(Level.FINE, "Caused by ", ex); //NOI18N
                    FileUtils.deleteDirectory(loc);
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NexusRepositoryIndexserImpl.class, "MSG_Reconstruct_Index"));
                    indexer.addIndexingContextForced(
                            info.getId(), // context id
                            info.getId(), // repository id
                            info.isLocal() ? new File(info.getRepositoryPath()) : null, // repository folder
                            loc,
                            info.isRemoteDownloadable() ? info.getRepositoryUrl() : null, // repositoryUrl
                            info.isRemoteDownloadable() ? info.getIndexUpdateUrl() : null, // index update url
                            NB_INDEX);
                }
            }
        }
    }


    //TODO mkleint: do we really want to start index whenever it's missing?
    // what about just silently returning empty values and let the scheduled
    // idexing kick in..
    private void checkIndexAvailability(final RepositoryInfo... ids) {
        assert MUTEX.isWriteAccess();

        for (RepositoryInfo id : ids) {
            LOGGER.finer("Checking Context.. :" + id.getId());//NOI18N
            File file = new File(getDefaultIndexLocation(), id.getId());
            if (!file.exists() || file.listFiles().length <= 0) {
                LOGGER.finer("Index Not Available :" + id + " At :" + file.getAbsolutePath());//NOI18N
                indexLoadedRepo(id, true);
            }
            LOGGER.finer("Index Available :" + id + " At :" + file.getAbsolutePath());//NOI18N
        }
    }


    //always call from mutex.writeAccess
    private void unloadIndexingContext(final RepositoryInfo... repos) throws IOException {
        assert MUTEX.isWriteAccess();
        for (RepositoryInfo repo : repos) {
            LOGGER.finer("Unloading Context :" + repo.getId());//NOI18N
            IndexingContext ic = indexer.getIndexingContexts().get(repo.getId());
            if (ic != null) {
                indexer.removeIndexingContext(ic, false);
            }
        }
    }

    private void indexLoadedRepo(final RepositoryInfo repo, boolean updateLocal) {
        assert MUTEX.isWriteAccess();
        try {
            Map<String, IndexingContext> indexingContexts = indexer.getIndexingContexts();
            IndexingContext indexingContext = indexingContexts.get(repo.getId());
            if (indexingContext == null) {
                LOGGER.warning("Indexing context chould not be created :" + repo.getId());//NOI18N
                return;
            }
            if (repo.isRemoteDownloadable()) {
                LOGGER.finer("Indexing Remote Repository :" + repo.getId());//NOI18N
                RemoteIndexTransferListener listener = new RemoteIndexTransferListener(repo);
                try {
                    remoteIndexUpdater.fetchAndUpdateIndex(indexingContext, listener);
                } finally {
                    listener.transferCompleted(null);
                }
            } else {
                LOGGER.finer("Indexing Local Repository :" + repo.getId());//NOI18N
                indexer.scan(indexingContext, new RepositoryIndexerListener(indexer, indexingContext, false), updateLocal);
            }
        } catch (IOException iOException) {
            LOGGER.warning(iOException.getMessage());//NOI18N
            //handle index not found
        } catch (UnsupportedExistingLuceneIndexException e) {
            //mkleint: what does this exception really mean?
            LOGGER.warning(e.getMessage());//NOI18N
        } finally {
            RepositoryPreferences.getInstance().setLastIndexUpdate(repo.getId(), new Date());
            fireChangeIndex(repo);
        }
    }

    public void indexRepo(final RepositoryInfo repo) {
        LOGGER.finer("Indexing Context :" + repo);//NOI18N
        try {
            MUTEX.writeAccess(new Mutex.ExceptionAction<Object>() {

                public Object run() throws Exception {
                    loadIndexingContext(repo);
                    try {
                        indexLoadedRepo(repo, false);
                    } finally {
                        unloadIndexingContext(repo);
                    }
                    return null;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    //to be used from external command line tols like mevenide/netbeans/maven-repo-utils
    public void indexRepo(final String repoId, final File repoDir, final File indexDir) {
        try {
            MUTEX.writeAccess(new Mutex.ExceptionAction<Object>() {

                public Object run() throws Exception {
                    IndexingContext indexingContext = indexer.addIndexingContext( //
                            repoId, // context id
                            repoId, // repository id
                            repoDir, // repository folder
                            new File(indexDir, repoId), // index folder
                            null, // repositoryUrl
                            null, // index update url
                            NB_INDEX,
                            false);


                    if (indexingContext == null) {
                        LOGGER.warning("Indexing context chould not be created :" + repoId);//NOI18N
                        return null;
                    }

                    indexer.scan(indexingContext, new RepositoryIndexerListener(indexer, indexingContext, true), true);
                    indexer.removeIndexingContext(indexingContext, false);
                    return null;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    public void updateIndexWithArtifacts(final RepositoryInfo repo, final Collection<Artifact> artifacts) {

        try {
            MUTEX.writeAccess(new Mutex.ExceptionAction<Object>() {

                public Object run() throws Exception {

                    loadIndexingContext(repo);
                    try {
                        checkIndexAvailability(repo);
                        Map<String, IndexingContext> indexingContexts = indexer.getIndexingContexts();
                        IndexingContext indexingContext = indexingContexts.get(repo.getId());
                        if (indexingContext == null) {
                            LOGGER.warning("Indexing context chould not be created :" + repo.getId());//NOI18N
                            return null;
                        }

                        for (Artifact artifact : artifacts) {
                            String absolutePath;
                            if (artifact.getFile() != null) {
                                absolutePath = artifact.getFile().getAbsolutePath();
                            } else if (artifact.getVersion() != null) { //#129025 avoid a NPE down the road
                                //well sort of hack, assume the default repo layout in the repository..
                                absolutePath = repo.getRepositoryPath() + File.separator + repository.pathOf(artifact);
                            } else {
                                continue;
                            }
                            String extension = artifact.getArtifactHandler().getExtension();

                            String pomPath = absolutePath.substring(0, absolutePath.length() - extension.length());
                            pomPath += "pom"; //NOI18N
                            File pom = new File(pomPath);
                            if (pom.exists()) {
                                indexer.addArtifactToIndex(contextProducer.getArtifactContext(indexingContext, pom), indexingContext);
                            }

                        }
                    } finally {
                        unloadIndexingContext(repo);
                    }
                    return null;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        fireChangeIndex(repo);
    }

    public void deleteArtifactFromIndex(final RepositoryInfo repo, final Artifact artifact) {
        try {
            MUTEX.writeAccess(new Mutex.ExceptionAction<Object>() {

                public Object run() throws Exception {
                    loadIndexingContext(repo);
                    try {
                        checkIndexAvailability(repo);
                        Map<String, IndexingContext> indexingContexts = indexer.getIndexingContexts();
                        IndexingContext indexingContext = indexingContexts.get(repo.getId());
                        if (indexingContext == null) {
                            LOGGER.warning("Indexing context chould not be created :" + repo.getId());//NOI18N
                            return null;
                        }

                        String absolutePath;
                        if (artifact.getFile() != null) {
                            absolutePath = artifact.getFile().getAbsolutePath();
                        } else {
                            //well sort of hack, assume the default repo layout in the repository..
                            absolutePath = repo.getRepositoryPath() + File.separator + repository.pathOf(artifact);
                        }
                        String extension = artifact.getArtifactHandler().getExtension();

                        String pomPath = absolutePath.substring(0, absolutePath.length() - extension.length());
                        pomPath += "pom"; //NOI18N
                        File pom = new File(pomPath);
                        if (pom.exists()) {
                            indexer.deleteArtifactFromIndex(contextProducer.getArtifactContext(indexingContext, pom), indexingContext);
                        }
                    } finally {
                        unloadIndexingContext(repo);
                    }
                    return null;
                }

            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        fireChangeIndex(repo);
    }
    
    private void fireChangeIndex(final RepositoryInfo repo) {
        if (MUTEX.isWriteAccess()) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    fireChangeIndex(repo);
                }
            });
            return;
        }
        assert !MUTEX.isWriteAccess() && !MUTEX.isReadAccess();
        repo.fireChangeIndex();
    }
    

    public File getDefaultIndexLocation() {
        File repo = new File(repository.getBasedir(), ".index/nexus"); //NOI18N
        if (!repo.exists()) {
            repo.mkdirs();
        }
        return repo;
    }

    public Set<String> getGroups(List<RepositoryInfo> repos) {
        return filterGroupIds("", repos);
    }

    public Set<String> filterGroupIds(final String prefix, final List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<Set<String>>() {

                public Set<String> run() throws Exception {
                    Set<String> groups = new TreeSet<String>();
                    loadIndexingContext(allrepos);
                    try {
                        checkIndexAvailability(allrepos);
                    } catch (Throwable t) {
                        unloadIndexingContext(allrepos);
                        return Collections.<String>emptySet();
                    }

                    List<RepositoryInfo> slowCheck = new ArrayList<RepositoryInfo>();
                    for (RepositoryInfo repo : repos) {
                        if (repo.isLocal() || repo.isRemoteDownloadable()) {
                            boolean unload = true;
                            try {
                                IndexingContext context = indexer.getIndexingContexts().get(repo.getId());
                                Set<String> all = indexer.getAllGroups(context);
                                if (all.size() > 0) {
                                    if (prefix.length() == 0) {
                                        groups.addAll(all);
                                    } else {
                                        for (String gr : all) {
                                            if (gr.startsWith(prefix)) {
                                                groups.add(gr);
                                            }
                                        }
                                    }
                                } else {
                                    slowCheck.add(repo);
                                    unload = false;
                                }
                            } finally {
                                if (unload) {
                                    unloadIndexingContext(repo);
                                }
                            }
                        }
                    }

                    final RepositoryInfo[] slowrepos = slowCheck.toArray(new RepositoryInfo[slowCheck.size()]);
                    if (slowrepos.length > 0) {
                        try {
                            BooleanQuery bq = new BooleanQuery();
                            bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, prefix)), BooleanClause.Occur.MUST));

                            Map<String, ArtifactInfoGroup> searchGrouped = indexer.searchGrouped(new GGrouping(),
                                    new Comparator<String>() {

                                        public int compare(String o1, String o2) {
                                            return o1.compareTo(o2);
                                        }
                                    },
                                    bq);
                            groups.addAll(searchGrouped.keySet());
                        } finally {
                            unloadIndexingContext(slowrepos);
                        }
                    }
                    return groups;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String>emptySet();
    }

    public List<NBVersionInfo> getRecords(final String groupId, final String artifactId, final String version, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<NBVersionInfo>>() {

                public List<NBVersionInfo> run() throws Exception {
                    List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
                    loadIndexingContext(allrepos);
                    try {
                        checkIndexAvailability(allrepos);
                        BooleanQuery bq = new BooleanQuery();
                        String id = groupId + AbstractIndexCreator.FS + artifactId + AbstractIndexCreator.FS + version + AbstractIndexCreator.FS;
                        bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST));
                        Collection<ArtifactInfo> searchGrouped = indexer.searchFlat(ArtifactInfo.VERSION_COMPARATOR,
                                bq);
                        infos.addAll(convertToNBVersionInfo(searchGrouped));
                    } finally {
                        unloadIndexingContext(allrepos);
                    }
                    return infos;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    public Set<String> getArtifacts(final String groupId, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<Set<String>>() {

                public Set<String> run() throws Exception {
                    Set<String> artifacts = new TreeSet<String>();
                    BooleanQuery bq = new BooleanQuery();
                    loadIndexingContext(allrepos);
                    try {
                        checkIndexAvailability(allrepos);
                        String id = groupId + AbstractIndexCreator.FS;
                        bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST));
                        Collection<ArtifactInfo> search = indexer.searchFlat(ArtifactInfo.VERSION_COMPARATOR, bq);
                        for (ArtifactInfo artifactInfo : search) {
                            artifacts.add(artifactInfo.artifactId);
                        }
                    } finally {
                        unloadIndexingContext(allrepos);
                    }
                    return artifacts;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String>emptySet();
    }

    public List<NBVersionInfo> getVersions(final String groupId, final String artifactId, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<NBVersionInfo>>() {

                public List<NBVersionInfo> run() throws Exception {
                    final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
                    loadIndexingContext(allrepos);
                    try {
                        checkIndexAvailability(allrepos);
                        BooleanQuery bq = new BooleanQuery();
                        String id = groupId + AbstractIndexCreator.FS + artifactId + AbstractIndexCreator.FS;
                        bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST));
                        Collection<ArtifactInfo> searchResult = indexer.searchFlat(ArtifactInfo.VERSION_COMPARATOR, bq);
                        infos.addAll(convertToNBVersionInfo(searchResult));
                    } finally {
                        unloadIndexingContext(allrepos);
                    }
                    return infos;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    public List<NBVersionInfo> findVersionsByClass(final String className, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<NBVersionInfo>>() {

                public List<NBVersionInfo> run() throws Exception {
                    List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
                    loadIndexingContext(allrepos);
                    String clsname = className.replace(".", "/");
                    try {
                        checkIndexAvailability(allrepos);
                        Collection<ArtifactInfo> searchResult = indexer.searchFlat(ArtifactInfo.VERSION_COMPARATOR,
                                indexer.constructQuery(ArtifactInfo.NAMES, clsname.toLowerCase()));
                        infos.addAll(convertToNBVersionInfo(postProcessClasses(searchResult, clsname)));
                    } finally {
                        unloadIndexingContext(allrepos);
                    }
                    return infos;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    public List<NBVersionInfo> findDependencyUsage(final String groupId, final String artifactId, final String version, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<NBVersionInfo>>() {

                public List<NBVersionInfo> run() throws Exception {
                    List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
                    loadIndexingContext(allrepos);
                    checkIndexAvailability(allrepos);
                    try {
                        BooleanQuery bq = new BooleanQuery();
                        bq.add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_GROUP, groupId)), BooleanClause.Occur.MUST));
                        bq.add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_ARTIFACT, artifactId)), BooleanClause.Occur.MUST));
                        bq.add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_VERSION, version)), BooleanClause.Occur.MUST));
                        Collection<ArtifactInfo> searchResult = indexer.searchFlat(ArtifactInfo.VERSION_COMPARATOR, bq);
                        infos.addAll(convertToNBVersionInfo(searchResult));
                    } finally {
                        unloadIndexingContext(allrepos);
                    }
                    return infos;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    public List<NBVersionInfo> findByMD5(final String md5, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<NBVersionInfo>>() {

                public List<NBVersionInfo> run() throws Exception {
                    List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
                    loadIndexingContext(allrepos);
                    try {
                        checkIndexAvailability(allrepos);
                        BooleanQuery bq = new BooleanQuery();
                        bq.add(new BooleanClause((indexer.constructQuery(ArtifactInfo.MD5, (md5))), BooleanClause.Occur.SHOULD));
                        Collection<ArtifactInfo> search = indexer.searchFlat(ArtifactInfo.VERSION_COMPARATOR, bq);
                        infos.addAll(convertToNBVersionInfo(search));
                    } finally {
                        unloadIndexingContext(allrepos);
                    }
                    return infos;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    public List<NBVersionInfo> findBySHA1(final String sha1, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<NBVersionInfo>>() {

                public List<NBVersionInfo> run() throws Exception {
                    List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
                    loadIndexingContext(allrepos);
                    try {
                        checkIndexAvailability(allrepos);
                        BooleanQuery bq = new BooleanQuery();
                        bq.add(new BooleanClause((indexer.constructQuery(ArtifactInfo.SHA1, sha1)), BooleanClause.Occur.SHOULD));
                        Collection<ArtifactInfo> search = indexer.searchFlat(ArtifactInfo.VERSION_COMPARATOR, bq);
                        infos.addAll(convertToNBVersionInfo(search));
                    } finally {
                        unloadIndexingContext(allrepos);
                    }
                    return infos;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    public List<NBVersionInfo> findArchetypes(List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<NBVersionInfo>>() {

                public List<NBVersionInfo> run() throws Exception {
                    List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
                    loadIndexingContext(allrepos);
                    try {
                        checkIndexAvailability(allrepos);
                        BooleanQuery bq = new BooleanQuery();
                        bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-archetype")), BooleanClause.Occur.MUST)); //NOI18N
                        Collection<ArtifactInfo> search = indexer.searchFlat(ArtifactInfo.VERSION_COMPARATOR, bq);
                        infos.addAll(convertToNBVersionInfo(search));
                    } finally {
                        unloadIndexingContext(allrepos);
                    }
                    return infos;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    public Set<String> filterPluginArtifactIds(final String groupId, final String prefix, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<Set<String>>() {

                public Set<String> run() throws Exception {
                    Set<String> artifacts = new TreeSet<String>();
                    loadIndexingContext(allrepos);
                    try {
                        checkIndexAvailability(allrepos);
                        BooleanQuery bq = new BooleanQuery();
                        String id = groupId + AbstractIndexCreator.FS + prefix;
                        bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-plugin")), BooleanClause.Occur.MUST));
                        bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST));
                        Collection<ArtifactInfo> search = indexer.searchFlat(ArtifactInfo.VERSION_COMPARATOR, bq);
                        for (ArtifactInfo artifactInfo : search) {
                            artifacts.add(artifactInfo.artifactId);
                        }
                    } finally {
                        unloadIndexingContext(allrepos);
                    }
                    return artifacts;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String>emptySet();
    }

    public Set<String> filterPluginGroupIds(final String prefix, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<Set<String>>() {

                public Set<String> run() throws Exception {
                    Set<String> artifacts = new TreeSet<String>();
                    loadIndexingContext(allrepos);
                    try {
                        checkIndexAvailability(allrepos);
                        BooleanQuery bq = new BooleanQuery();
                        bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-plugin")), BooleanClause.Occur.MUST));
                        bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, prefix)), BooleanClause.Occur.MUST));
                        Collection<ArtifactInfo> search = indexer.searchFlat(ArtifactInfo.VERSION_COMPARATOR, bq);
                        for (ArtifactInfo artifactInfo : search) {
                            artifacts.add(artifactInfo.groupId);
                        }
                    } finally {
                        unloadIndexingContext(allrepos);
                    }
                    return artifacts;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String>emptySet();
    }

    public Set<String> filterArtifactIdForGroupId(final String groupId, final String prefix, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<Set<String>>() {

                public Set<String> run() throws Exception {
                    Set<String> artifacts = new TreeSet<String>();
                    loadIndexingContext(allrepos);
                    try {
                        checkIndexAvailability(allrepos);
                        BooleanQuery bq = new BooleanQuery();
                        String id = groupId + AbstractIndexCreator.FS + prefix;
                        bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST));

                        Collection<ArtifactInfo> search = indexer.searchFlat(ArtifactInfo.VERSION_COMPARATOR, bq);
                        for (ArtifactInfo artifactInfo : search) {
                            artifacts.add(artifactInfo.artifactId);
                        }
                    } finally {
                        unloadIndexingContext(allrepos);
                    }
                    return artifacts;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String>emptySet();
    }

    public List<NBVersionInfo> find(final List<QueryField> fields, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<NBVersionInfo>>() {

                public List<NBVersionInfo> run() throws Exception {
                    List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
                    loadIndexingContext(allrepos);
                    try {
                        checkIndexAvailability(allrepos);
                        BooleanQuery bq = new BooleanQuery();
                        for (QueryField field : fields) {
                            BooleanClause.Occur occur = field.getOccur() == QueryField.OCCUR_SHOULD ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST;
                            String fieldName = toNexusField(field.getField());
                            if (fieldName != null) {
                                Query q;
                                if (ArtifactInfo.NAMES.equals(fieldName)) {
                                    String clsname = field.getValue().replace(".", "/"); //NOI18N
                                    q = indexer.constructQuery(ArtifactInfo.NAMES, clsname.toLowerCase());
                                } else {
                                    if (field.getMatch() == QueryField.MATCH_EXACT) {
                                        q = new TermQuery(new Term(fieldName, field.getValue()));
                                    } else {
                                        q = new PrefixQuery(new Term(fieldName, field.getValue()));
                                    }
                                }
                                BooleanClause bc = new BooleanClause(q, occur);
                                bq.add(bc); //NOI18N
                            } else {
                                //TODO when all fields, we need to create separate
                                //queries for each field.
                            }
                        }
                        Collection<ArtifactInfo> search = indexer.searchFlat(ArtifactInfo.VERSION_COMPARATOR, bq);
                        infos.addAll(convertToNBVersionInfo(search));
                    } finally {
                        unloadIndexingContext(allrepos);
                    }
                    return infos;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    private String toNexusField(String field) {
        if (QueryField.FIELD_ARTIFACTID.equals(field)) {
            return ArtifactInfo.ARTIFACT_ID;
        } else if (QueryField.FIELD_GROUPID.equals(field)) {
            return ArtifactInfo.GROUP_ID;
        } else if (QueryField.FIELD_VERSION.equals(field)) {
            return ArtifactInfo.VERSION;
        } else if (QueryField.FIELD_CLASSES.equals(field)) {
            return ArtifactInfo.NAMES;
        } else if (QueryField.FIELD_NAME.equals(field)) {
            return ArtifactInfo.NAME;
        } else if (QueryField.FIELD_DESCRIPTION.equals(field)) {
            return ArtifactInfo.DESCRIPTION;
        }
        return field;
    }



    private Collection<ArtifactInfo> postProcessClasses(Collection<ArtifactInfo> artifactInfos, String classname) {
        int patter = Pattern.DOTALL + Pattern.MULTILINE;
        boolean isPath = classname.contains("/");
        if (isPath) {
            return artifactInfos;
        }
        //if I got it right, we need an exact match of class name, which the query doesn't provide? why?
        String pattStr = ".*/" + classname + "$.*";
        Pattern patt = Pattern.compile(pattStr, patter);
        Iterator<ArtifactInfo> it = artifactInfos.iterator();
        while (it.hasNext()) {
            ArtifactInfo ai = it.next();
            Matcher m = patt.matcher(ai.classNames);
            if (!m.matches()) {
                it.remove();
            }
        }
        return artifactInfos;
    }

    private List<NBVersionInfo> convertToNBVersionInfo(Collection<ArtifactInfo> artifactInfos) {
        List<NBVersionInfo> bVersionInfos = new ArrayList<NBVersionInfo>();
        for (ArtifactInfo ai : artifactInfos) {
            NBVersionInfo nbvi = new NBVersionInfo(ai.repository, ai.groupId, ai.artifactId,
                    ai.version, ai.packaging, ai.packaging, ai.name, ai.description, ai.classifier);
            /*Javadoc & Sources*/
            nbvi.setJavadocExists(ai.javadocExists == ArtifactAvailablility.PRESENT);
            nbvi.setSourcesExists(ai.sourcesExists == ArtifactAvailablility.PRESENT);
            nbvi.setSignatureExists(ai.signatureExists == ArtifactAvailablility.PRESENT);
            bVersionInfos.add(nbvi);
        }
        return bVersionInfos;
    }

    private static class NbIndexCreator extends AbstractIndexCreator {

        public ArtifactRepository repository = EmbedderFactory.getOnlineEmbedder().getLocalRepository();

        public boolean updateArtifactInfo(IndexingContext ctx, Document d, ArtifactInfo artifactInfo) {
            return false;
        }

        public void updateDocument(ArtifactIndexingContext context, Document doc) {
            ArtifactInfo ai = context.getArtifactContext().getArtifactInfo();
            if (ai.classifier != null) {
                //don't process items with classifier
                return;
            }
            try {
                MavenProject mp = RepositoryUtil.readMavenProject(ai.groupId, ai.artifactId, ai.version, repository);
                if (mp != null) {
                    List<Dependency> dependencies = mp.getDependencies();
                    for (Dependency d : dependencies) {
                        doc.add(new Field(NB_DEPENDENCY_GROUP, d.getGroupId(), Field.Store.NO, Field.Index.UN_TOKENIZED));
                        doc.add(new Field(NB_DEPENDENCY_ARTIFACT, d.getArtifactId(), Field.Store.NO, Field.Index.UN_TOKENIZED));
                        doc.add(new Field(NB_DEPENDENCY_VERSION, d.getVersion(), Field.Store.NO, Field.Index.UN_TOKENIZED));
                    }
                }
            } catch (InvalidArtifactRTException ex) {
                ex.printStackTrace();
            }
        }

        public void populateArtifactInfo(ArtifactIndexingContext context) throws IOException {
        }
    }
}
