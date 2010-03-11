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
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidArtifactRTException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.InvalidProjectModelException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
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
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.netbeans.modules.maven.indexer.spi.ContextLoadedQuery;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.sonatype.nexus.index.ArtifactAvailablility;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactContextProducer;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchRequest;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.search.grouping.GGrouping;
import org.sonatype.nexus.index.GroupedSearchRequest;
import org.sonatype.nexus.index.GroupedSearchResponse;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.creator.AbstractIndexCreator;
import org.sonatype.nexus.index.creator.JarFileContentsIndexCreator;
import org.sonatype.nexus.index.creator.MinimalArtifactInfoIndexCreator;
import org.sonatype.nexus.index.SearchEngine;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.updater.DefaultIndexUpdater.WagonFetcher;
import org.sonatype.nexus.index.updater.IndexUpdateRequest;
import org.sonatype.nexus.index.updater.IndexUpdater;

/**
 *
 * @author Anuradha G
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.maven.indexer.spi.RepositoryIndexerImplementation.class)
public class NexusRepositoryIndexerImpl implements RepositoryIndexerImplementation,
        BaseQueries, ChecksumQueries, ArchetypeQueries, DependencyInfoQueries,
        ClassesQuery, GenericFindQuery, ContextLoadedQuery {
    private static final String MAVENINDEX_PATH = "mavenindex";

    private ArtifactRepository repository;
    private NexusIndexer indexer;
    private SearchEngine searcher;
    private IndexUpdater remoteIndexUpdater;
    private ArtifactContextProducer contextProducer;
    private WagonManager wagonManager;
    private boolean inited = false;
    /*Indexer Keys*/
    private static final String NB_DEPENDENCY_GROUP = "nbdg"; //NOI18N
    private static final String NB_DEPENDENCY_ARTIFACT = "nbda"; //NOI18N
    private static final String NB_DEPENDENCY_VERSION = "nbdv"; //NOI18N
    /*logger*/
    private static final Logger LOGGER =
            Logger.getLogger("org.netbeans.modules.maven.indexer.RepositoryIndexer");//NOI18N
    /*custom Index creators*/
    /**
     * any reads, writes from/to index shal be done under mutex access.
     */
    static final Mutex MUTEX = new Mutex();
    private Lookup lookup;

    //#158083 more caching to satisfy the classloading gods..
    private List<? extends IndexCreator> CREATORS;
    private List<? extends IndexCreator> getLocalRepoIndexCreators() {
        if (CREATORS == null) {
            CREATORS = Arrays.asList(
                new MinimalArtifactInfoIndexCreator(),
                new JarFileContentsIndexCreator(),
                new NbIndexCreator());
        }
        return CREATORS;
    }

    private static final int MAX_RESULT_COUNT = 512;
    private static final int DEFAULT_MAX_CLAUSE = 1024;
    private static final int MAX_MAX_CLAUSE = 8192;

    //#138102
    public static String createLocalRepositoryPath(FileObject fo) {
        return EmbedderFactory.getProjectEmbedder().getLocalRepository().getBasedir();
    }

    public NexusRepositoryIndexerImpl() {
        lookup = Lookups.singleton(this);
    }

    @Override
    public String getType() {
        return RepositoryPreferences.TYPE_NEXUS;
    }

    @Override
    public Lookup getCapabilityLookup() {
        return lookup;
    }

    private void initIndexer () {
        if (!inited) {
            try {
                PlexusContainer embedder;
                ContainerConfiguration config = new DefaultContainerConfiguration();
	            //#154755 - start
	            ClassWorld world = new ClassWorld();
	            ClassRealm embedderRealm = world.newRealm("maven.embedder", MavenEmbedder.class.getClassLoader()); //NOI18N
	            ClassRealm indexerRealm = world.newRealm("maven.indexer", NexusRepositoryIndexerImpl.class.getClassLoader()); //NOI18N
	            ClassRealm plexusRealm = world.newRealm("plexus.core", NexusRepositoryIndexerImpl.class.getClassLoader()); //NOI18N
	            //need to import META-INF/plexus stuff, otherwise the items in META-INF will not be loaded,
	            // and the Dependency Injection won't work.
	            plexusRealm.importFrom(embedderRealm.getId(), "META-INF/plexus"); //NOI18N
	            plexusRealm.importFrom(embedderRealm.getId(), "META-INF/maven"); //NOI18N
	            plexusRealm.importFrom(indexerRealm.getId(), "META-INF/plexus"); //NOI18N
	            plexusRealm.importFrom(indexerRealm.getId(), "META-INF/maven"); //NOI18N
	            config.setClassWorld(world);
	            //#154755 - end
                embedder = new DefaultPlexusContainer(config);

                repository = EmbedderFactory.getProjectEmbedder().getLocalRepository();
                indexer = (NexusIndexer) embedder.lookup(NexusIndexer.class);
                searcher = (SearchEngine) embedder.lookup(SearchEngine.class);
                remoteIndexUpdater = (IndexUpdater) embedder.lookup(IndexUpdater.class);
                wagonManager = (WagonManager) embedder.lookup( WagonManager.class );
                contextProducer = (ArtifactContextProducer) embedder.lookup(ArtifactContextProducer.class);
                inited = true;
            } catch (DuplicateRealmException ex) {
                Exceptions.printStackTrace(ex);
            } catch (NoSuchRealmException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ComponentLookupException ex) {
                Exceptions.printStackTrace(ex);
            } catch (PlexusContainerException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    //always call from mutex.writeAccess
    private void loadIndexingContext(final RepositoryInfo... repoids) throws IOException {
        assert MUTEX.isWriteAccess();
        initIndexer();

        for (RepositoryInfo info : repoids) {
            IndexingContext context = indexer.getIndexingContexts().get(info.getId());
            if (context != null) {
                String contexturl = context.getIndexUpdateUrl();
                String repourl = info.getIndexUpdateUrl();
                File contextfile = context.getRepository();
                File repofile = info.getRepositoryPath() != null ? new File(info.getRepositoryPath()) : null;
                //try to figure if context reload is necessary
                if ((contexturl == null) != (repourl == null) ||
                    (contexturl != null && !contexturl.equals(repourl))) {
                    LOGGER.fine("Remote context changed:" + info.getId() + ", unload/load");//NOI18N
                    unloadIndexingContext(info);
                } else if ((contextfile == null) != (repofile == null) ||
                           (contextfile != null && !contextfile.equals(repofile))) {
                    LOGGER.fine("Local context changed:" + info.getId() + ", unload/load");//NOI18N
                    unloadIndexingContext(info);
                } else {
                    LOGGER.fine("Skipping Context :" + info.getId() + ", already loaded.");//NOI18N
                    continue;
                }
            }
            LOGGER.fine("Loading Context :" + info.getId());//NOI18N
            if (info.isLocal() || info.isRemoteDownloadable()) {
                File loc = new File(getDefaultIndexLocation(), info.getId()); // index folder
                boolean index = false;
                if (!loc.exists() || loc.listFiles().length <= 0) {
                    index = true;
                    LOGGER.finer("Index Not Available :" + info.getId() + " At :" + loc.getAbsolutePath());//NOI18N
                }

                try {
                    indexer.addIndexingContextForced(
                            info.getId(), // context id
                            info.getId(), // repository id
                            info.isLocal() ? new File(info.getRepositoryPath()) : null, // repository folder
                            loc,
                            info.isRemoteDownloadable() ? info.getRepositoryUrl() : null, // repositoryUrl
                            info.isRemoteDownloadable() ? info.getIndexUpdateUrl() : null, // index update url
                            info.isLocal() ? getLocalRepoIndexCreators() : indexer.FULL_INDEX);
                } catch (IOException ex) {
                    LOGGER.info("Found a broken index at " + loc.getAbsolutePath()); //NOI18N
                    LOGGER.log(Level.FINE, "Caused by ", ex); //NOI18N
                    FileUtils.deleteDirectory(loc);
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NexusRepositoryIndexerImpl.class, "MSG_Reconstruct_Index"));
                    indexer.addIndexingContextForced(
                            info.getId(), // context id
                            info.getId(), // repository id
                            info.isLocal() ? new File(info.getRepositoryPath()) : null, // repository folder
                            loc,
                            info.isRemoteDownloadable() ? info.getRepositoryUrl() : null, // repositoryUrl
                            info.isRemoteDownloadable() ? info.getIndexUpdateUrl() : null, // index update url
                            info.isLocal() ? getLocalRepoIndexCreators() : indexer.FULL_INDEX);
                }
                if (index) {
                    indexLoadedRepo(info, true);
                }
            }
        }

        //figure if a repository was removed from list, remove from context.
        Set<String> currents = new HashSet<String>();
        for (RepositoryInfo info : RepositoryPreferences.getInstance().getRepositoryInfos()) {
            currents.add(info.getId());
        }
        Set<String> toRemove = new HashSet<String>(indexer.getIndexingContexts().keySet());
        toRemove.removeAll(currents);
        if (!toRemove.isEmpty()) {
            unloadIndexingContext(toRemove);
        }
    }

    /**
     * get the IndexingContext instances for the given RepositoryInfo instances.
     */
    private Collection<IndexingContext> getContexts(RepositoryInfo[] allrepos) {
        assert MUTEX.isWriteAccess();
        List<IndexingContext> toRet = new ArrayList<IndexingContext>();
        for (RepositoryInfo info : allrepos) {
            IndexingContext context = indexer.getIndexingContexts().get(info.getId());
            if (context != null) {
                toRet.add(context);
            } else {
                if (info.isLocal() || info.isRemoteDownloadable()) {
                    LOGGER.info("The context '" + info.getId() + "' isn't loaded. Please file under component: maven in NetBeans issue tracking system");
                }
                //else ignore, is not a real nexus repo, is missing any indexing properties..
            }
        }
        return toRet;
    }

    private FlatSearchResponse repeatedFlatSearch(FlatSearchRequest fsr, final Collection<IndexingContext> contexts, boolean shouldThrow) throws IOException {
        FlatSearchResponse response = null;
        try {
            BooleanQuery.TooManyClauses tooManyC = null;
            for (int i = DEFAULT_MAX_CLAUSE; i <= MAX_MAX_CLAUSE; i*=2) {
                try {
                    BooleanQuery.setMaxClauseCount(i);
                    response = searcher.searchFlatPaged(fsr, contexts);
                } catch (BooleanQuery.TooManyClauses exc) {
                    tooManyC = exc;
                    response = null;
                    LOGGER.finest("TooManyClause on " + i + " clauses"); //NOI18N
                }
                if (response != null) {
                    LOGGER.finest("OK, passed on " + i + " clauses"); //NOI18N
                    break;
                }
            }
            if (response == null && shouldThrow) {
                throw tooManyC;
            }
        } finally {
            BooleanQuery.setMaxClauseCount(DEFAULT_MAX_CLAUSE);
        }
        return response;
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


    //always call from mutex.writeAccess
    private void unloadIndexingContext(final Set<String> repos) throws IOException {
        assert MUTEX.isWriteAccess();
        for (String repo : repos) {
            LOGGER.fine("Unloading Context :" + repo);//NOI18N
            IndexingContext ic = indexer.getIndexingContexts().get(repo);
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
                LOGGER.info("Indexing context could not be found :" + repo.getId());//NOI18N
                return;
            }
            if (repo.isRemoteDownloadable()) {
                LOGGER.finer("Indexing Remote Repository :" + repo.getId());//NOI18N
                RemoteIndexTransferListener listener = new RemoteIndexTransferListener(repo);
                try {
                    IndexUpdateRequest iur = new IndexUpdateRequest(indexingContext);
                    iur.setResourceFetcher( new WagonFetcher( wagonManager, listener, null ) );
                    remoteIndexUpdater.fetchAndUpdateIndex(iur);
                } finally {
                    listener.transferCompleted(null);
                }
            } else {
                LOGGER.finer("Indexing Local Repository :" + repo.getId());//NOI18N
                indexer.scan(indexingContext, new RepositoryIndexerListener(indexer, indexingContext), updateLocal);
            }
        } catch (IOException iOException) {
            LOGGER.warning(iOException.getMessage());//NOI18N
            //handle index not found
        } finally {
            RepositoryPreferences.getInstance().setLastIndexUpdate(repo.getId(), new Date());
            fireChangeIndex(repo);
        }
    }

    @Override
    public void indexRepo(final RepositoryInfo repo) {
        LOGGER.finer("Indexing Context :" + repo);//NOI18N
        try {
            RemoteIndexTransferListener.addToActive(Thread.currentThread());
            MUTEX.writeAccess(new Mutex.ExceptionAction<Object>() {

                @Override
                public Object run() throws Exception {
                    initIndexer();
                    //need to delete the index and recreate? the scan(update) parameter doesn't work?
                    IndexingContext cntx = indexer.getIndexingContexts().get(repo.getId());
                    if (cntx != null) {
                        indexer.removeIndexingContext(cntx, true);
                    }
                    loadIndexingContext(repo);
                    indexLoadedRepo(repo, false);
                    return null;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            RemoteIndexTransferListener.removeFromActive(Thread.currentThread());
        }

    }

    public void shutdownAll() {
        LOGGER.finer("Shutting Down All Contexts");//NOI18N
        try {
            MUTEX.writeAccess(new Mutex.ExceptionAction<Object>() {
                @Override
                public Object run() throws Exception {
                    if (inited) {
                        for (IndexingContext ic : indexer.getIndexingContexts().values()) {
                            LOGGER.finer(" Shutting Down:" + ic.getId());//NOI18N
                            indexer.removeIndexingContext(ic, false);
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
    }



//    //to be used from external command line tols like mevenide/netbeans/maven-repo-utils
//    // just comment out, questionalble if ever to be used.
//    public void indexRepo(final String repoId, final File repoDir, final File indexDir) {
//        try {
//            MUTEX.writeAccess(new Mutex.ExceptionAction<Object>() {
//
//                public Object run() throws Exception {
//                    IndexingContext indexingContext = indexer.addIndexingContext( //
//                            repoId, // context id
//                            repoId, // repository id
//                            repoDir, // repository folder
//                            new File(indexDir, repoId), // index folder
//                            null, // repositoryUrl
//                            null, // index update url
//                            NB_INDEX,
//                            false);
//
//
//                    if (indexingContext == null) {
//                        LOGGER.warning("Indexing context chould not be created :" + repoId);//NOI18N
//                        return null;
//                    }
//
//                    indexer.scan(indexingContext, new RepositoryIndexerListener(indexer, indexingContext, true), true);
//                    indexer.removeIndexingContext(indexingContext, false);
//                    return null;
//                }
//            });
//        } catch (MutexException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }

    @Override
    public void updateIndexWithArtifacts(final RepositoryInfo repo, final Collection<Artifact> artifacts) {

        try {
            MUTEX.writeAccess(new Mutex.ExceptionAction<Object>() {

                @Override
                public Object run() throws Exception {

                    loadIndexingContext(repo);
                    Map<String, IndexingContext> indexingContexts = indexer.getIndexingContexts();
                    IndexingContext indexingContext = indexingContexts.get(repo.getId());
                    if (indexingContext == null) {
                        LOGGER.warning("Indexing context could not be created :" + repo.getId());//NOI18N
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
                        File art = new File(absolutePath);
                        if (art.exists()) {
                            ArtifactContext ac = contextProducer.getArtifactContext(indexingContext, art);
//                            System.out.println("ac gav=" + ac.getGav());
//                            System.out.println("ac pom=" + ac.getPom());
//                            System.out.println("ac art=" + ac.getArtifact());
//                            System.out.println("ac info=" + ac.getArtifactInfo());
                            indexer.addArtifactToIndex(ac, indexingContext);
                        }

                    }
                    return null;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        fireChangeIndex(repo);
    }

    @Override
    public void deleteArtifactFromIndex(final RepositoryInfo repo, final Artifact artifact) {
        try {
            MUTEX.writeAccess(new Mutex.ExceptionAction<Object>() {

                @Override
                public Object run() throws Exception {
                    loadIndexingContext(repo);
                    Map<String, IndexingContext> indexingContexts = indexer.getIndexingContexts();
                    IndexingContext indexingContext = indexingContexts.get(repo.getId());
                    if (indexingContext == null) {
                        LOGGER.warning("Indexing context chould not be created :" + repo.getId());//NOI18N
                        return null;
                    }

                    String absolutePath;
                    if (artifact.getFile() != null) {
                        absolutePath = artifact.getFile().getAbsolutePath();
                    } else if (artifact.getVersion() != null) { //#129025 avoid a NPE down the road
                        //well sort of hack, assume the default repo layout in the repository..
                        absolutePath = repo.getRepositoryPath() + File.separator + repository.pathOf(artifact);
                    } else {
                        return null;
                    }
                    String extension = artifact.getArtifactHandler().getExtension();

                    String pomPath = absolutePath.substring(0, absolutePath.length() - extension.length());
                    pomPath += "pom"; //NOI18N
                    File pom = new File(pomPath);
                    if (pom.exists()) {
                        indexer.deleteArtifactFromIndex(contextProducer.getArtifactContext(indexingContext, pom), indexingContext);
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
                @Override
                public void run() {
                    fireChangeIndex(repo);
                }
            });
            return;
        }
        assert !MUTEX.isWriteAccess() && !MUTEX.isReadAccess();
        repo.fireChangeIndex();
    }


    private File getDefaultIndexLocation() {
        String userdir = System.getProperty("netbeans.user"); //NOI18N
        File cacheDir;
        if (userdir != null) {
            cacheDir = new File(new File(new File(userdir, "var"), "cache"), MAVENINDEX_PATH);//NOI18N
        } else {
            File root = FileUtil.toFile(FileUtil.getConfigRoot());
            cacheDir = new File(root, MAVENINDEX_PATH);//NOI18N
        }
        cacheDir.mkdirs();
        return cacheDir;
    }



    @Override
    public Set<String> getGroups(List<RepositoryInfo> repos) {
        return filterGroupIds("", repos);
    }

    @Override
    public Set<String> filterGroupIds(final String prefix, final List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<Set<String>>() {

                @Override
                public Set<String> run() throws Exception {
                    Set<String> groups = new TreeSet<String>();
                    loadIndexingContext(allrepos);

                    List<RepositoryInfo> slowCheck = new ArrayList<RepositoryInfo>();
                    for (RepositoryInfo repo : repos) {
                        if (repo.isLocal() || repo.isRemoteDownloadable()) {
                            IndexingContext context = indexer.getIndexingContexts().get(repo.getId());
                            if (context == null) {
                                continue; //#167884
                            }
                            Set<String> all = context.getAllGroups();
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
                            }
                        }
                    }

                    final RepositoryInfo[] slowrepos = slowCheck.toArray(new RepositoryInfo[slowCheck.size()]);
                    if (slowrepos.length > 0) {
                        BooleanQuery bq = new BooleanQuery();
                        bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, prefix)), BooleanClause.Occur.MUST));
                        GroupedSearchRequest gsr = new GroupedSearchRequest(bq, new GGrouping(),
                                new Comparator<String>() {
                            @Override
                                    public int compare(String o1, String o2) {
                                        return o1.compareTo(o2);
                                    }
                                });
                        GroupedSearchResponse response = searcher.searchGrouped(gsr, getContexts(slowrepos));
                        groups.addAll(response.getResults().keySet());
                    }
                    return groups;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String>emptySet();
    }

    @Override
    public List<NBVersionInfo> getRecords(final String groupId, final String artifactId, final String version, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<NBVersionInfo>>() {

                @Override
                public List<NBVersionInfo> run() throws Exception {
                    List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
                    loadIndexingContext(allrepos);
                    BooleanQuery bq = new BooleanQuery();
                    String id = groupId + ArtifactInfo.FS + artifactId + ArtifactInfo.FS + version + ArtifactInfo.FS;
                    bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST));
                    FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                    fsr.setAiCount(MAX_RESULT_COUNT);
                    FlatSearchResponse response = searcher.searchFlatPaged(fsr, getContexts(allrepos));
                    infos.addAll(convertToNBVersionInfo(response.getResults()));
                    return infos;
                }

            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    @Override
    public Set<String> getArtifacts(final String groupId, final List<RepositoryInfo> repos) {
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<Set<String>>() {
                @Override
                public Set<String> run() throws Exception {
                    return doGetArtifacts(groupId, repos);
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String>emptySet();
    }

    private Set<String> doGetArtifacts(final String groupId, List<RepositoryInfo> repos) throws IOException {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        Set<String> artifacts = new TreeSet<String>();
        BooleanQuery bq = new BooleanQuery();
        loadIndexingContext(allrepos);
        String id = groupId + ArtifactInfo.FS;
        bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST));
        FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
        FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(allrepos), false);
        if (response != null) {
            for (ArtifactInfo artifactInfo : response.getResults()) {
                artifacts.add(artifactInfo.artifactId);
            }
        }
        return artifacts;
    }

    @Override
    public List<NBVersionInfo> getVersions(final String groupId, final String artifactId, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<NBVersionInfo>>() {

                @Override
                public List<NBVersionInfo> run() throws Exception {
                    final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
                    loadIndexingContext(allrepos);
                    BooleanQuery bq = new BooleanQuery();
                    String id = groupId + ArtifactInfo.FS + artifactId + ArtifactInfo.FS;
                    bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST));
                    FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                    FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(allrepos), false);
                    if (response != null) {
                        infos.addAll(convertToNBVersionInfo(response.getResults()));
                    }
                    return infos;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    @Override
    public List<NBVersionInfo> findVersionsByClass(final String className, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<NBVersionInfo>>() {

                @Override
                public List<NBVersionInfo> run() throws Exception {
                    List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
                    loadIndexingContext(allrepos);
                    String clsname = className.replace(".", "/");
                    FlatSearchRequest fsr = new FlatSearchRequest(indexer.constructQuery(ArtifactInfo.NAMES, clsname.toLowerCase()),
                            ArtifactInfo.VERSION_COMPARATOR);
                    fsr.setAiCount(MAX_RESULT_COUNT);
                    FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(allrepos), false);
                    if (response != null) {
                        infos.addAll(convertToNBVersionInfo(postProcessClasses(response.getResults(),
                                clsname)));
                    }
                    return infos;
                }
            });
        } catch (MutexException ex) {
            rethrowTooManyClauses(ex);
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    @Override
    public List<NBVersionInfo> findDependencyUsage(final String groupId, final String artifactId, final String version, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<NBVersionInfo>>() {

                @Override
                public List<NBVersionInfo> run() throws Exception {
                    List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
                    loadIndexingContext(allrepos);
                    BooleanQuery bq = new BooleanQuery();
                    bq.add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_GROUP, groupId)), BooleanClause.Occur.MUST));
                    bq.add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_ARTIFACT, artifactId)), BooleanClause.Occur.MUST));
                    bq.add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_VERSION, version)), BooleanClause.Occur.MUST));
                    FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                    fsr.setAiCount(MAX_RESULT_COUNT);
                    FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(allrepos), false);
                    if (response != null) {
                        infos.addAll(convertToNBVersionInfo(response.getResults()));
                    }
                    return infos;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    @Override
    public List<NBVersionInfo> findByMD5(final String md5, List<RepositoryInfo> repos) {
        //not supported
        return Collections.<NBVersionInfo>emptyList();
    }

    @Override
    public List<NBVersionInfo> findBySHA1(final String sha1, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<NBVersionInfo>>() {

                @Override
                public List<NBVersionInfo> run() throws Exception {
                    List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
                    loadIndexingContext(allrepos);
                    BooleanQuery bq = new BooleanQuery();
                    bq.add(new BooleanClause((indexer.constructQuery(ArtifactInfo.SHA1, sha1)), BooleanClause.Occur.SHOULD));
                    FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                    fsr.setAiCount(MAX_RESULT_COUNT);
                    FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(allrepos), false);
                    if (response != null) {
                        infos.addAll(convertToNBVersionInfo(response.getResults()));
                    }
                    return infos;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    @Override
    public List<NBVersionInfo> findArchetypes(List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<NBVersionInfo>>() {

                @Override
                public List<NBVersionInfo> run() throws Exception {
                    List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
                    loadIndexingContext(allrepos);
                    BooleanQuery bq = new BooleanQuery();
                    bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-archetype")), BooleanClause.Occur.MUST)); //NOI18N
                    FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                    fsr.setAiCount(MAX_RESULT_COUNT);
                    FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(allrepos), false);
                    if (response != null) {
                        infos.addAll(convertToNBVersionInfo(response.getResults()));
                    }
                    return infos;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    @Override
    public Set<String> filterPluginArtifactIds(final String groupId, final String prefix, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<Set<String>>() {

                @Override
                public Set<String> run() throws Exception {
                    Set<String> artifacts = new TreeSet<String>();
                    loadIndexingContext(allrepos);
                    BooleanQuery bq = new BooleanQuery();
                    String id = groupId + ArtifactInfo.FS + prefix;
                    bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-plugin")), BooleanClause.Occur.MUST));
                    bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST));
                    FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                    fsr.setAiCount(MAX_RESULT_COUNT);
                    FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(allrepos), false);
                    if (response != null) {
                        for (ArtifactInfo artifactInfo : response.getResults()) {
                            artifacts.add(artifactInfo.artifactId);
                        }
                    }
                    return artifacts;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String>emptySet();
    }

    @Override
    public Set<String> filterPluginGroupIds(final String prefix, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<Set<String>>() {

                @Override
                public Set<String> run() throws Exception {
                    Set<String> artifacts = new TreeSet<String>();
                    loadIndexingContext(allrepos);
                    BooleanQuery bq = new BooleanQuery();
                    bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-plugin")), BooleanClause.Occur.MUST));
                    if (prefix.length() > 0) { //heap out of memory otherwise
                        bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.GROUP_ID, prefix)), BooleanClause.Occur.MUST));
                    }
                    FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                    fsr.setAiCount(MAX_RESULT_COUNT);
                    FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(allrepos), false);
                    if (response != null) {
                        for (ArtifactInfo artifactInfo : response.getResults()) {
                            artifacts.add(artifactInfo.groupId);
                        }
                    }
                    return artifacts;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String>emptySet();
    }

    @Override
    public Set<String> filterArtifactIdForGroupId(final String groupId, final String prefix, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<Set<String>>() {

                @Override
                public Set<String> run() throws Exception {
                    Set<String> artifacts = new TreeSet<String>();
                    loadIndexingContext(allrepos);
                    BooleanQuery bq = new BooleanQuery();
                    String id = groupId + ArtifactInfo.FS + prefix;
                    bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST));
                    FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                    fsr.setAiCount(MAX_RESULT_COUNT);
                    FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(allrepos), false);
                    if (response != null) {
                        for (ArtifactInfo artifactInfo : response.getResults()) {
                            artifacts.add(artifactInfo.artifactId);
                        }
                    }
                    return artifacts;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String>emptySet();
    }

    @Override
    public List<NBVersionInfo> find(final List<QueryField> fields, List<RepositoryInfo> repos) {
        final RepositoryInfo[] allrepos = repos.toArray(new RepositoryInfo[repos.size()]);
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<NBVersionInfo>>() {

                @Override
                public List<NBVersionInfo> run() throws Exception {
                    List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
                    loadIndexingContext(allrepos);
                    BooleanQuery bq = new BooleanQuery();
                    for (QueryField field : fields) {
                        BooleanClause.Occur occur = field.getOccur() == QueryField.OCCUR_SHOULD ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST;
                        String fieldName = toNexusField(field.getField());
                        if (fieldName != null) {
                            Query q;
                            if (ArtifactInfo.NAMES.equals(fieldName)) {
                                String clsname = field.getValue().replace(".", "/"); //NOI18N
                                q = indexer.constructQuery(ArtifactInfo.NAMES, clsname.toLowerCase());
                            } else if (ArtifactInfo.ARTIFACT_ID.equals(fieldName)) {
                                q = indexer.constructQuery(fieldName, field.getValue());
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
                    FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                    fsr.setAiCount(MAX_RESULT_COUNT);
                    FlatSearchResponse response = repeatedFlatSearch(fsr,getContexts(allrepos), true);
                    infos.addAll(convertToNBVersionInfo(response.getResults()));
                    return infos;
                }
            });
        } catch (MutexException ex) {
            rethrowTooManyClauses(ex);
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    @Override
    public List<RepositoryInfo> getLoaded(final List<RepositoryInfo> repos) {
        try {
            return MUTEX.writeAccess(new Mutex.ExceptionAction<List<RepositoryInfo>>() {

                @Override
                public List<RepositoryInfo> run() throws Exception {
                    if (!inited) {
                        return Collections.emptyList();
                    }
                    List<RepositoryInfo> toRet = new ArrayList<RepositoryInfo>(repos.size());
                    for (RepositoryInfo info : repos) {
                        if (indexer.getIndexingContexts().get(info.getId()) != null) {
                            toRet.add(info);
                        }
                    }
                    return toRet;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.emptyList();
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
        } else if (QueryField.FIELD_PACKAGING.equals(field)) {
            return ArtifactInfo.PACKAGING;
        }
        return field;
    }

    private void rethrowTooManyClauses (MutexException mutEx) {
        Exception cause = mutEx.getException();
        if (cause instanceof BooleanQuery.TooManyClauses) {
            throw (BooleanQuery.TooManyClauses)cause;
        }
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
            if ("javadoc".equals(ai.classifier) || "sources".equals(ai.classifier)) { //NOI18N
                // we don't want javadoc and sources shown anywhere, we use the getJavadocExists(), getSourceExists() methods.
                continue;
            }
            NBVersionInfo nbvi = new NBVersionInfo(ai.repository, ai.groupId, ai.artifactId,
                    ai.version, ai.packaging, ai.packaging, ai.name, ai.description, ai.classifier);
            /*Javadoc & Sources*/
            nbvi.setJavadocExists(ai.javadocExists == ArtifactAvailablility.PRESENT);
            nbvi.setSourcesExists(ai.sourcesExists == ArtifactAvailablility.PRESENT);
            nbvi.setSignatureExists(ai.signatureExists == ArtifactAvailablility.PRESENT);
//            nbvi.setSha(ai.sha1);
            nbvi.setLastModified(ai.lastModified);
            nbvi.setSize(ai.size);
            bVersionInfos.add(nbvi);
        }
        return bVersionInfos;
    }

    private static class NbIndexCreator extends AbstractIndexCreator {
        //TODO make weak referenced to save long term memory footprint??
        private MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();

        public ArtifactRepository repository = online.getLocalRepository();


        @Override
        public void updateDocument(ArtifactInfo context, Document doc) {
            ArtifactInfo ai = context;
            if (ai.classifier != null) {
                //don't process items with classifier
                return;
            }
            try {
                MavenProject mp = load(ai, repository);
                if (mp != null) {
                    @SuppressWarnings("unchecked")
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

        private MavenProject load(ArtifactInfo ai, ArtifactRepository repository) {
            try {
                ArtifactFactory artifactFactory = (ArtifactFactory) online.getPlexusContainer().lookup(ArtifactFactory.class);
                Artifact projectArtifact = artifactFactory.createProjectArtifact(
                        ai.groupId,
                        ai.artifactId,
                        ai.version,
                        null);

                MavenProjectBuilder builder = (MavenProjectBuilder) online.getPlexusContainer().lookup(MavenProjectBuilder.class);
                return builder.buildFromRepository(projectArtifact, new ArrayList(), repository);
            } catch (InvalidProjectModelException ex) {
                //ignore nexus is falling ???
                LOGGER.log(Level.FINE, "Failed to load project model from repository.", ex);
            } catch (ProjectBuildingException ex) {
                LOGGER.log(Level.FINE, "Failed to load project model from repository.", ex);
            } catch (Exception exception) {
                LOGGER.log(Level.FINE, "Failed to load project model from repository.", exception);
            }
            return null;
        }

        @Override
        public void populateArtifactInfo(ArtifactContext context) throws IOException {
        }

        @Override
        public boolean updateArtifactInfo(Document arg0, ArtifactInfo arg1) {
            return false;
        }
    }
}
