 /*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.indexer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipError;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.index.*;
import org.apache.maven.index.artifact.ArtifactPackagingMapper;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.expr.StringSearchExpression;
import org.apache.maven.index.search.grouping.GGrouping;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdater;
import org.apache.maven.index.updater.ResourceFetcher;
import org.apache.maven.index.updater.WagonHelper;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.providers.http.HttpWagon;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.indexer.api.*;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import org.netbeans.modules.maven.indexer.spi.*;
import org.openide.modules.Places;
import org.openide.util.*;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@ServiceProviders({
    @ServiceProvider(service=RepositoryIndexerImplementation.class),
    @ServiceProvider(service=BaseQueries.class),
    @ServiceProvider(service=ChecksumQueries.class),
    @ServiceProvider(service=ArchetypeQueries.class),
    @ServiceProvider(service=DependencyInfoQueries.class),
    @ServiceProvider(service=ClassesQuery.class),
    @ServiceProvider(service=ClassUsageQuery.class),
    @ServiceProvider(service=GenericFindQuery.class),
    @ServiceProvider(service=ContextLoadedQuery.class)
})
public class NexusRepositoryIndexerImpl implements RepositoryIndexerImplementation,
        BaseQueries, ChecksumQueries, ArchetypeQueries, DependencyInfoQueries,
        ClassesQuery, ClassUsageQuery, GenericFindQuery, ContextLoadedQuery {

    
    public static abstract class Accessor {
        
        public abstract void addSkipped(Result<?> result, Collection<RepositoryInfo> infos);
        
        public abstract List<RepositoryInfo> getSkipped(Result<?> result);
        
        public abstract void setStringResults(Result<String> result, Collection<String> newResults);
        
        public abstract void setVersionResults(Result<NBVersionInfo> result, Collection<NBVersionInfo> newResults);
        
        public abstract void addSkipped(Result<?> result, RepositoryInfo info);
        
        public abstract Result<String> createStringResult(Redo<String> redo);
        
        public abstract Result<NBVersionInfo> createVersionResult(Redo<NBVersionInfo> redo);
        
        public abstract Result<NBGroupInfo> createGroupResult(Redo<NBGroupInfo> redo);
        
        public abstract void setGroupResults(Result<NBGroupInfo> result, Collection<NBGroupInfo> newResults);
        
        public abstract Result<RepositoryQueries.ClassUsage> createClassResult(Redo<RepositoryQueries.ClassUsage> redo);
        
        public abstract void setClassResults(Result<RepositoryQueries.ClassUsage> result, Collection<RepositoryQueries.ClassUsage> newResults);
        
        public abstract void addTotalResults(Result<?> result, int moreResults);
        
        public abstract void addReturnedResults(Result<?> result, int moreResults);
    }
    
    private static final Logger LOGGER = Logger.getLogger(NexusRepositoryIndexerImpl.class.getName());
       
    @SuppressWarnings("MS_SHOULD_BE_FINAL")
    public static Accessor ACCESSOR;

    static {
        // invokes static initializer of RepositoryQueries.class
        // that will assign value to the ACCESSOR field above
        Class<?> c = RepositoryQueries.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException x) {
            throw new ExceptionInInitializerError(x);
        }
    }     

    private PlexusContainer embedder;
    private NexusIndexer indexer;
    private SearchEngine searcher;
    private IndexUpdater remoteIndexUpdater;
    private ArtifactContextProducer contextProducer;
    private boolean inited = false;
    /**
     * any reads, writes from/to index shal be done under mutex access.
     */
    private static final HashMap<String,Mutex> repoMutexMap = new HashMap<String, Mutex>(4);

    private static final Set<Mutex> indexingMutexes = new HashSet<Mutex>();
    private static final RequestProcessor RP = new RequestProcessor("indexing", 1);

    private Mutex getRepoMutex(RepositoryInfo repo) {
        return getRepoMutex(repo.getId());
    }
    
    private Mutex getRepoMutex(String repoId) {
        synchronized (repoMutexMap) {
            Mutex m = repoMutexMap.get(repoId);
            if (m == null) {
                m = new Mutex();
                repoMutexMap.put(repoId, m);
            }
            return m;
        }
    }
    
    static final int MAX_RESULT_COUNT = 1024;
    static final int NO_CAP_RESULT_COUNT = AbstractSearchRequest.UNDEFINED;

    public NexusRepositoryIndexerImpl() {
    }

    private void initIndexer () {
        if (!inited) {
            try {
                ContainerConfiguration config = new DefaultContainerConfiguration();
	            //#154755 - start
	            ClassWorld world = new ClassWorld();
	            ClassRealm embedderRealm = world.newRealm("maven.embedder", MavenEmbedder.class.getClassLoader()); //NOI18N
                ClassLoader indexerLoader = NexusRepositoryIndexerImpl.class.getClassLoader();
	            ClassRealm indexerRealm = world.newRealm("maven.indexer", indexerLoader); //NOI18N
	            ClassRealm plexusRealm = world.newRealm("plexus.core", indexerLoader); //NOI18N
	            //need to import META-INF/plexus stuff, otherwise the items in META-INF will not be loaded,
	            // and the Dependency Injection won't work.
	            plexusRealm.importFrom(embedderRealm.getId(), "META-INF/plexus"); //NOI18N
	            plexusRealm.importFrom(embedderRealm.getId(), "META-INF/maven"); //NOI18N
	            plexusRealm.importFrom(indexerRealm.getId(), "META-INF/plexus"); //NOI18N
	            plexusRealm.importFrom(indexerRealm.getId(), "META-INF/maven"); //NOI18N
	            config.setClassWorld(world);
	            //#154755 - end
                embedder = new DefaultPlexusContainer(config);

                ComponentDescriptor<ArtifactContextProducer> desc = new ComponentDescriptor<ArtifactContextProducer>();
                desc.setRoleClass(ArtifactContextProducer.class);
                desc.setImplementationClass(CustomArtifactContextProducer.class);
                ComponentRequirement req = new ComponentRequirement(); // XXX why is this not automatic?
                req.setFieldName("mapper");
                req.setRole(ArtifactPackagingMapper.class.getName());
                desc.addRequirement(req);
                embedder.addComponentDescriptor(desc);

                indexer = embedder.lookup(NexusIndexer.class);
                searcher = embedder.lookup(SearchEngine.class);
                remoteIndexUpdater = embedder.lookup(IndexUpdater.class);
                contextProducer = embedder.lookup(ArtifactContextProducer.class);
                inited = true;
            } catch (Exception x) {
                Exceptions.printStackTrace(x);
            }
        }
    }

    private boolean loadIndexingContext2(final RepositoryInfo info) throws IOException {
        boolean index = false;
        LOAD: {
            assert getRepoMutex(info).isWriteAccess();
            initIndexer();

            IndexingContext context = indexer.getIndexingContexts().get(info.getId());
            String indexUpdateUrl = info.getIndexUpdateUrl();
            if (context != null) {
                String contexturl = context.getIndexUpdateUrl();
                File contextfile = context.getRepository();
                File repofile = info.getRepositoryPath() != null ? new File(info.getRepositoryPath()) : null;
                //try to figure if context reload is necessary
                if (!Utilities.compareObjects(contexturl, indexUpdateUrl)) {
                    LOGGER.log(Level.FINE, "Remote context changed: {0}, unload/load", info.getId());
                    unloadIndexingContext(info.getId());
                } else if (!Utilities.compareObjects(contextfile, repofile)) {
                    LOGGER.log(Level.FINE, "Local context changed: {0}, unload/load", info.getId());
                    unloadIndexingContext(info.getId());
                } else {
                    LOGGER.log(Level.FINER, "Skipping Context: {0}, already loaded.", info.getId());
                    break LOAD; // XXX does it suffice to just return here, or is code after block needed?
                }
            }
            LOGGER.log(Level.FINE, "Loading Context: {0}", info.getId());
                File loc = new File(getDefaultIndexLocation(), info.getId()); // index folder
                try {
                    if (!loc.exists() || !new File(loc, "timestamp").exists() || !IndexReader.indexExists(new SimpleFSDirectory(loc))) {
                        index = true;
                        LOGGER.log(Level.FINER, "Index Not Available: {0} at: {1}", new Object[] {info.getId(), loc.getAbsolutePath()});
                    }
                } catch (IOException ex) {
                    index = true;
                    LOGGER.log(Level.FINER, "Index Not Available: " + info.getId() + " at: " + loc.getAbsolutePath(), ex);
                }

                List<IndexCreator> creators = new ArrayList<IndexCreator>();
                try {
                    creators.addAll(embedder.lookupList(IndexCreator.class));
                } catch (ComponentLookupException x) {
                    throw new IOException(x);
                }
                if (info.isLocal()) { // #164593
                    creators.add(new ArtifactDependencyIndexCreator());
                    creators.add(new ClassDependencyIndexCreator());
                } else {
                    creators.add(new NotifyingIndexCreator());
                }
                try {
                    indexer.addIndexingContextForced(
                            info.getId(), // context id
                            info.getId(), // repository id
                            info.isLocal() ? new File(info.getRepositoryPath()) : null, // repository folder
                            loc,
                            info.isRemoteDownloadable() ? info.getRepositoryUrl() : null, // repositoryUrl
                            info.isRemoteDownloadable() ? indexUpdateUrl : null,
                            creators);
                    LOGGER.log(Level.FINE, "using index creators: {0}", creators);
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, "Found a broken index at " + loc + " with loaded contexts " + indexer.getIndexingContexts().keySet(), ex);
                    break LOAD;
                }
                }

        //figure if a repository was removed from list, remove from context.
        Set<String> currents = new HashSet<String>();
        for (RepositoryInfo info2 : RepositoryPreferences.getInstance().getRepositoryInfos()) {
            currents.add(info2.getId());
        }
        Set<String> toRemove = new HashSet<String>(indexer.getIndexingContexts().keySet());
        toRemove.removeAll(currents);
        if (!toRemove.isEmpty()) {
            for (final String repo : toRemove) {
                try {
                    getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                        public @Override Void run() throws Exception {
                            unloadIndexingContext(repo);
                            return null;
                        }
                    });
                } catch (MutexException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return index;
    }

    private @CheckForNull IteratorSearchResponse repeatedPagedSearch(Query q, final List<IndexingContext> contexts, int count) throws IOException {
        IteratorSearchRequest isr = new IteratorSearchRequest(q, contexts, new NoJavadocSourceFilter());
        if (count > 0) {
            isr.setCount(count);
        }
        
        int MAX_MAX_CLAUSE = 1<<11;  // conservative maximum for too general queries, like "c:*class*"

        if (q instanceof BooleanQuery) {
            BooleanClause[] c = ((BooleanQuery)q).getClauses();
            if (c.length==1) {
                Query q1 = c[0].getQuery();
                if (q1 instanceof PrefixQuery && "u".equals(((PrefixQuery)q1).getPrefix().field())) {
                    // increase for queries like "+u:org.netbeans.modules|*" to succeed
                    MAX_MAX_CLAUSE = 1<<16;
                } else if (q1 instanceof TermQuery && "p".equals(((TermQuery) q1).getTerm().field())) {
                    // +p:nbm also produces several thousand hits
                    MAX_MAX_CLAUSE = 1<<16;
                }
            }
        }

        int oldMax = BooleanQuery.getMaxClauseCount();
        try {
            int max = oldMax;
            while (true) {
                IteratorSearchResponse response;
                try {
                    BooleanQuery.setMaxClauseCount(max);
                    response = searcher.searchIteratorPaged(isr, contexts);
                    LOGGER.log(Level.FINE, "passed on {0} clauses processing {1} with {2} hits", new Object[] {max, q, response.getTotalHitsCount()});
                    return response;
                } catch (BooleanQuery.TooManyClauses exc) {
                    LOGGER.log(Level.FINE, "TooManyClauses on {0} clauses processing {1}", new Object[] {max, q});
                    max *= 2;
                    if (max > MAX_MAX_CLAUSE) {
                        LOGGER.log(Level.WARNING, "Encountered more than {0} clauses processing {1}", new Object[] {MAX_MAX_CLAUSE, q});
                        return null;
                    } else {
                        continue;
                    }
                }
            }
        } finally {
            BooleanQuery.setMaxClauseCount(oldMax);
        }
    }

    //always call from mutex.writeAccess
    private void unloadIndexingContext(final String repo) throws IOException {
        assert getRepoMutex(repo).isWriteAccess();
        LOGGER.log(Level.FINE, "Unloading Context: {0}", repo);
        IndexingContext ic = indexer.getIndexingContexts().get(repo);
        if (ic != null) {
            indexer.removeIndexingContext(ic, false);
        }
    }

    private void indexLoadedRepo(final RepositoryInfo repo, boolean updateLocal) throws IOException {
        Mutex mutex = getRepoMutex(repo);
        assert mutex.isWriteAccess();
        synchronized (indexingMutexes) {
            indexingMutexes.add(mutex);
        }
        try {
            Map<String, IndexingContext> indexingContexts = indexer.getIndexingContexts();
            IndexingContext indexingContext = indexingContexts.get(repo.getId());
            if (indexingContext == null) {
                LOGGER.log(Level.WARNING, "Indexing context could not be found: {0}", repo.getId());
                return;
            }
            if (repo.isRemoteDownloadable()) {
                LOGGER.log(Level.FINE, "Indexing Remote Repository: {0}", repo.getId());
                final RemoteIndexTransferListener listener = new RemoteIndexTransferListener(repo);
                try {
                    String protocol = URI.create(indexingContext.getIndexUpdateUrl()).getScheme();
                    SettingsDecryptionResult settings = embedder.lookup(SettingsDecrypter.class).decrypt(new DefaultSettingsDecryptionRequest(EmbedderFactory.getOnlineEmbedder().getSettings()));
                    AuthenticationInfo wagonAuth = null;
                    for (Server server : settings.getServers()) {
                        if (repo.getId().equals(server.getId())) {
                            wagonAuth = new AuthenticationInfo();
                            wagonAuth.setUserName(server.getUsername());
                            wagonAuth.setPassword(server.getPassword());
                            wagonAuth.setPassphrase(server.getPassphrase());
                            wagonAuth.setPrivateKey(server.getPrivateKey());
                            break;
                        }
                    }
                    ProxyInfo wagonProxy = null;
                    for (Proxy proxy : settings.getProxies()) {
                        if (proxy.isActive()) {
                            wagonProxy = new ProxyInfo();
                            wagonProxy.setHost(proxy.getHost());
                            wagonProxy.setPort(proxy.getPort());
                            wagonProxy.setNonProxyHosts(proxy.getNonProxyHosts());
                            wagonProxy.setUserName(proxy.getUsername());
                            wagonProxy.setPassword(proxy.getPassword());
                            wagonProxy.setType(protocol);
                            break;
                        }
                    }
                    // MINDEXER-42: cannot use WagonHelper.getWagonResourceFetcher
                    Wagon wagon = embedder.lookup(Wagon.class, protocol);
                    if (wagon instanceof HttpWagon) { //#216401
                        HttpWagon httpwagon = (HttpWagon) wagon;
                        //#215343
                        Properties p = new Properties();
                        p.setProperty("User-Agent", "netBeans/" + System.getProperty("netbeans.buildnumber"));
                        httpwagon.setHttpHeaders(p);
                    }
                            
                    ResourceFetcher fetcher = new WagonHelper.WagonFetcher(wagon, listener, wagonAuth, wagonProxy);
                    listener.setFetcher(fetcher);
                    IndexUpdateRequest iur = new IndexUpdateRequest(indexingContext, fetcher);
                    
                    NotifyingIndexCreator nic = null;
                    for (IndexCreator ic : indexingContext.getIndexCreators()) {
                        if (ic instanceof NotifyingIndexCreator) {
                            nic = (NotifyingIndexCreator) ic;
                            break;
                        }
                    }
                    if (nic != null) {
                        nic.start(listener);
                    }
                    try {
                        remoteIndexUpdater.fetchAndUpdateIndex(iur);
                    } finally {
                        if (nic != null) {
                            nic.end();
                        }
                    }
                } finally {
                    listener.close();
                }
            } else {
                LOGGER.log(Level.FINE, "Indexing Local Repository: {0}", repo.getId());
                if (!indexingContext.getRepository().exists()) {
                    //#210743
                    LOGGER.log(Level.FINE, "Local repository at {0} doesn't exist, no scan.", indexingContext.getRepository());
                } else {
                    RepositoryIndexerListener listener = new RepositoryIndexerListener(indexingContext);
                    try {
                        indexer.scan(indexingContext, listener, updateLocal);
                    } finally {
                        listener.close();
                    }
                }
            }
        } catch (Cancellation x) {
            throw new IOException("canceled indexing");
        } catch (ComponentLookupException x) {
            throw new IOException("could not find protocol handler for " + repo.getRepositoryUrl(), x);
        } finally {
            synchronized (indexingMutexes) {
                indexingMutexes.remove(mutex);
            }
            RepositoryPreferences.setLastIndexUpdate(repo.getId(), new Date());
            fireChangeIndex(repo);
            
        }
    }

    //spawn the indexing into a separate thread..
    private void spawnIndexLoadedRepo(final RepositoryInfo repo) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                getRepoMutex(repo).writeAccess(new Mutex.Action<Void>() {
                    public @Override Void run() {
                        try {
                            indexLoadedRepo(repo, true);
                        } catch (IOException ex) {
                            LOGGER.log(Level.INFO, "could not (re-)index " + repo.getId(), ex);
                        }
                        return null;
                    }
                });
            }
        });
    }    

    @Override
    public void indexRepo(final RepositoryInfo repo) {
        LOGGER.log(Level.FINER, "Indexing Context: {0}", repo);
        try {
            RemoteIndexTransferListener.addToActive(Thread.currentThread());
            getRepoMutex(repo).writeAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
                    try {
                        initIndexer();
                        assert indexer != null;
                        boolean noIndexExists = loadIndexingContext2(repo);
                        indexLoadedRepo(repo, !noIndexExists);
                    } catch (IOException x) {
                        LOGGER.log(Level.INFO, "could not (re-)index " + repo.getId(), x);
                    }
                    return null;
                }
            });
        } finally {
            RemoteIndexTransferListener.removeFromActive(Thread.currentThread());
        }

    }

    public void shutdownAll() {
        LOGGER.fine("Shutting Down All Contexts");
        // Do not acquire write access since that can block waiting for a hung download.
        try {
            if (inited) {
                for (IndexingContext ic : indexer.getIndexingContexts().values()) {
                    LOGGER.log(Level.FINER, "Shutting Down: {0}", ic.getId());
                    indexer.removeIndexingContext(ic, false);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void updateIndexWithArtifacts(final RepositoryInfo repo, final Collection<Artifact> artifacts) {
        final ArtifactRepository repository = EmbedderFactory.getProjectEmbedder().getLocalRepository();
        try {
            getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                public @Override Void run() throws Exception {
                    boolean index = loadIndexingContext2(repo);
                    if (index) {
                        try {
                            NexusRepositoryIndexerImpl.this.indexLoadedRepo(repo, true);
                        } catch (IOException ex) {
                            LOGGER.log(Level.INFO, "could not (re-)index " + repo.getId(), ex);
                            return null;
                        }    
                    }
                    Map<String, IndexingContext> indexingContexts = indexer.getIndexingContexts();
                    IndexingContext indexingContext = indexingContexts.get(repo.getId());
                    if (indexingContext == null) {
                        LOGGER.log(Level.WARNING, "Indexing context could not be created: {0}", repo.getId());
                        return null;
                    }
                    
                    if (!indexingContext.getRepository().exists()) {
                        //#210743
                        LOGGER.log(Level.FINE, "Local repository at {0} doesn't exist, no update.", indexingContext.getRepository());  
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
                            //#229296 don't reindex stuff that is already in the index, with exception of snapshots
                            boolean add = artifact.isSnapshot();
                            if (!artifact.isSnapshot()) {
                                BooleanQuery bq = new BooleanQuery();
                                String id = artifact.getGroupId() + ArtifactInfo.FS + artifact.getArtifactId() + ArtifactInfo.FS + artifact.getVersion() + ArtifactInfo.FS + ArtifactInfo.nvl(artifact.getClassifier());
                                bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST));
                                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(indexingContext), MAX_RESULT_COUNT);
                                add = response == null || response.getTotalHitsCount() == 0;
                                if (response != null) {
                                    response.close();
                                }
                            }
                            if (add) {
                                LOGGER.log(Level.FINE, "indexing " + artifact.getId() );
                                ArtifactContext ac = contextProducer.getArtifactContext(indexingContext, art);
    //                            System.out.println("ac gav=" + ac.getGav());
    //                            System.out.println("ac pom=" + ac.getPom());
    //                            System.out.println("ac art=" + ac.getArtifact());
    //                            System.out.println("ac info=" + ac.getArtifactInfo());
    //                                assert indexingContext.getIndexSearcher() != null;
                                try {
                                    indexer.addArtifactToIndex(ac, indexingContext);
                                } catch (ZipError err) {
                                    LOGGER.log(Level.INFO, "#230581 concurrent access to local repository file. Skipping..", err);
                                }
                            } else {
                                LOGGER.log(Level.FINE, "Skipped " + artifact.getId() + " already in index.");
                            }
                        }

                    }
                    return null;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NullPointerException x) {
            LOGGER.log(Level.INFO, "#201057", x);
        }
        fireChangeIndex(repo);
    }
    
    @Override
    public void deleteArtifactFromIndex(final RepositoryInfo repo, final Artifact artifact) {
        final ArtifactRepository repository = EmbedderFactory.getProjectEmbedder().getLocalRepository();
        try {
            getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                public @Override Void run() throws Exception {
                    boolean index = loadIndexingContext2(repo);
                    if (index) {
                        try {
                            indexLoadedRepo(repo, true); //TODO do we care here?
                        } catch (IOException ex) {
                            LOGGER.log(Level.INFO, "could not (re-)index " + repo.getId(), ex);
                            return null;
                        }
                    }
                    Map<String, IndexingContext> indexingContexts = indexer.getIndexingContexts();
                    IndexingContext indexingContext = indexingContexts.get(repo.getId());
                    if (indexingContext == null) {
                        LOGGER.log(Level.WARNING, "Indexing context could not be created: {0}", repo.getId());
                        return null;
                    }
                    if (!indexingContext.getRepository().exists()) {
                        //#210743
                        LOGGER.log(Level.FINE, "Local repository at {0} doesn't exist, no update.", indexingContext.getRepository());  
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
        if (getRepoMutex(repo).isWriteAccess()) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    fireChangeIndex(repo);
                }
            });
            return;
        }
        assert !getRepoMutex(repo).isWriteAccess() && !getRepoMutex(repo).isReadAccess();
        repo.fireChangeIndex();
    }

    private File getDefaultIndexLocation() {
        return Places.getCacheSubdirectory("mavenindex");
    }

    @Override
    public RepositoryQueries.Result<String> getGroups(List<RepositoryInfo> repos) {
        return filterGroupIds("", repos);
    }

    private static boolean isIndexing(Mutex mutex) {
        synchronized (indexingMutexes) {
            return indexingMutexes.contains(mutex);
        }
    }

    private interface RepoAction {
        void run(RepositoryInfo repo, IndexingContext context) throws IOException;
    }
    
    private void iterate(List<RepositoryInfo> repos, final RepoAction action, final RepoAction actionSkip, final boolean skipUnIndexed) {
        if (repos == null) {
            repos = RepositoryPreferences.getInstance().getRepositoryInfos();
        }
        for (final RepositoryInfo repo : repos) {
            Mutex mutex = getRepoMutex(repo);
            if (skipUnIndexed && isIndexing(mutex)) {
                try {
                    actionSkip.run(repo, null);
                } catch (IOException ex) {
                    LOGGER.log(Level.FINER, "could not skip " + repo.getId(), ex);
                }
            } else {
                mutex.writeAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
                    try {
                        boolean index = loadIndexingContext2(repo);
                        if (skipUnIndexed && index) {
                            actionSkip.run(repo, null);
                            spawnIndexLoadedRepo(repo);
                            return null;
                        }
                        IndexingContext context = indexer.getIndexingContexts().get(repo.getId());
                        if (context == null) {
                            if (skipUnIndexed) {
                                actionSkip.run(repo, null);
                            }
                            return null;
                        }
                        action.run(repo, context);
                    } catch (IOException x) {
                        LOGGER.log(Level.INFO, "could not process " + repo.getId(), x);
                    }
                    return null;
                }
            });
        }
    }
    }

    @Override
    public RepositoryQueries.Result<String> filterGroupIds(final String prefix, final List<RepositoryInfo> repos) {
        RepositoryQueries.Result<String> result = ACCESSOR.createStringResult(new Redo<String>() {
            @Override
            public void run(Result<String> result) {
                filterGroupIds(prefix, result, ACCESSOR.getSkipped(result), false);
            }
        });
        return filterGroupIds(prefix, result, repos, true);
    }
    
    private RepositoryQueries.Result<String> filterGroupIds(final String prefix, final RepositoryQueries.Result<String> result, 
                                                            final List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Set<String> groups = new TreeSet<String>(result.getResults());
        final List<RepositoryInfo> slowCheck = new ArrayList<RepositoryInfo>();
        
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                Set<String> all= context.getAllGroups();
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

        }, skipAction, skipUnIndexed);
        
        //the slow check kicking in is nowadays very rare, used to be a workaround for old versions of indexing data..
        iterate(slowCheck, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, prefix)), BooleanClause.Occur.MUST));
                GroupedSearchRequest gsr = new GroupedSearchRequest(bq, new GGrouping(), new Comparator<String>() {
                    @Override public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                });
                GroupedSearchResponse response = searcher.searchGrouped(gsr, Collections.singletonList(context));
                groups.addAll(response.getResults().keySet());
            }
        }, skipAction, skipUnIndexed);
        
        ACCESSOR.setStringResults(result, groups);
        return result;
    }


    

    @Override
    public Result<NBVersionInfo> getRecords(final String groupId, final String artifactId, final String version, List<RepositoryInfo> repos) {
        RepositoryQueries.Result<NBVersionInfo> result = ACCESSOR.createVersionResult(new Redo<NBVersionInfo>() {
            @Override
            public void run(Result<NBVersionInfo> result) {
                getRecords(groupId, artifactId, version, result, ACCESSOR.getSkipped(result), false);
            }
        });
        return getRecords(groupId, artifactId, version, result, repos, true);
    }
    
    private Result<NBVersionInfo> getRecords(final String groupId, final String artifactId, final String version, final Result<NBVersionInfo> result, 
                                             List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                String id = groupId + ArtifactInfo.FS + artifactId + ArtifactInfo.FS + version + ArtifactInfo.FS;
                bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST));
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), MAX_RESULT_COUNT);
                if (response != null) {
                   try {
                        for (ArtifactInfo ai : response.iterator()) {
                            infos.add(convertToNBVersionInfo(ai));
                        }
                    } finally {
                        ACCESSOR.addReturnedResults(result, response.getTotalProcessedArtifactInfoCount());
                        ACCESSOR.addTotalResults(result, response.getTotalHitsCount());
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        Collections.sort(infos);
        ACCESSOR.setVersionResults(result, infos);
        return result;
    }

    @Override
    public RepositoryQueries.Result<String> getArtifacts(final String groupId, final List<RepositoryInfo> repos) {
        RepositoryQueries.Result<String> result = ACCESSOR.createStringResult(new Redo<String>() {
            @Override
            public void run(Result<String> result) {
                getArtifacts(groupId, result, ACCESSOR.getSkipped(result), false);
            }
        });
        return getArtifacts(groupId, result, repos, true);
    }
    
    private  RepositoryQueries.Result<String> getArtifacts(final String groupId, final Result<String> result, final List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Set<String> artifacts = new TreeSet<String>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                String id = groupId + ArtifactInfo.FS;
                bq.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST));
                //mkleint: this is not capped, because only a string is collected (and collapsed), the rest gets CGed fast
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), NO_CAP_RESULT_COUNT);
                if (response != null) {
                    try {
                    for (ArtifactInfo artifactInfo : response.getResults()) {
                        artifacts.add(artifactInfo.artifactId);
                    }
                    } finally {
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        ACCESSOR.setStringResults(result, artifacts);
        return result;
    }

    @Override
    public RepositoryQueries.Result<NBVersionInfo> getVersions(final String groupId, final String artifactId, List<RepositoryInfo> repos) {
        RepositoryQueries.Result<NBVersionInfo> result = ACCESSOR.createVersionResult(new Redo<NBVersionInfo>() {
            @Override
            public void run(Result<NBVersionInfo> result) {
                getVersions(groupId, artifactId, result, ACCESSOR.getSkipped(result), false);
            }
        });
        return getVersions(groupId, artifactId, result, repos, true);
    }
    private RepositoryQueries.Result<NBVersionInfo> getVersions(final String groupId, final String artifactId, final Result<NBVersionInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                String id = groupId + ArtifactInfo.FS + artifactId + ArtifactInfo.FS;
                bq.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST));
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), MAX_RESULT_COUNT);
                if (response != null) {
                   try {
                        for (ArtifactInfo ai : response.iterator()) {
                            infos.add(convertToNBVersionInfo(ai));
                        }
                    } finally {
                        ACCESSOR.addReturnedResults(result, response.getTotalProcessedArtifactInfoCount());
                        ACCESSOR.addTotalResults(result, response.getTotalHitsCount());
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        Collections.sort(infos);
        ACCESSOR.setVersionResults(result, infos);
        return result;
    }

    @Override
    public RepositoryQueries.Result<NBVersionInfo> findVersionsByClass(final String className, List<RepositoryInfo> repos) {
        RepositoryQueries.Result<NBVersionInfo> result = ACCESSOR.createVersionResult(new Redo<NBVersionInfo>() {
            @Override
            public void run(Result<NBVersionInfo> result) {
                findVersionsByClass(className, result, ACCESSOR.getSkipped(result), false);
            }
        });
        return findVersionsByClass(className, result, repos, true);
    }
    
    private RepositoryQueries.Result<NBVersionInfo> findVersionsByClass(final String className, final Result<NBVersionInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                String clsname = className.replace(".", "/");
                Query q = setBooleanRewrite(
                        indexer.constructQuery(MAVEN.CLASSNAMES, new StringSearchExpression(clsname.toLowerCase(Locale.ENGLISH))));       
                IteratorSearchResponse response = repeatedPagedSearch(q, Collections.singletonList(context), MAX_RESULT_COUNT);
                if (response != null) {
                    try {
                        infos.addAll(postProcessClasses(response.getResults(), clsname));
                    } finally {
                        //?? really count in this case?
                        ACCESSOR.addReturnedResults(result, response.getTotalProcessedArtifactInfoCount());
                        ACCESSOR.addTotalResults(result, response.getTotalHitsCount());
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        Collections.sort(infos);
        ACCESSOR.setVersionResults(result, infos);
        return result;
    }

    @Override 
    public RepositoryQueries.Result<RepositoryQueries.ClassUsage> findClassUsages(final String className, @NullAllowed List<RepositoryInfo> repos) {
        RepositoryQueries.Result<RepositoryQueries.ClassUsage> result = ACCESSOR.createClassResult(new Redo<RepositoryQueries.ClassUsage>() {
            @Override
            public void run(Result<RepositoryQueries.ClassUsage> result) {
                findClassUsages(className, result, ACCESSOR.getSkipped(result), false);
            }
        });
        return findClassUsages(className, result, repos, true);
        
    }
    
    private RepositoryQueries.Result<RepositoryQueries.ClassUsage> findClassUsages(final String className, Result<RepositoryQueries.ClassUsage> result, @NullAllowed List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        List<RepositoryInfo> localRepos = new ArrayList<RepositoryInfo>();
        if (repos == null) {
            repos = RepositoryPreferences.getInstance().getRepositoryInfos();
        }
        for (RepositoryInfo repo : repos) {
            if (repo.isLocal()) {
                localRepos.add(repo);
            }
        }
        final List<RepositoryQueries.ClassUsage> results = new ArrayList<RepositoryQueries.ClassUsage>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(localRepos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                ClassDependencyIndexCreator.search(className, indexer, Collections.singletonList(context), results);
            }
        }, skipAction, skipUnIndexed);
        Collections.sort(results, new Comparator<RepositoryQueries.ClassUsage>() {
            @Override public int compare(RepositoryQueries.ClassUsage r1, RepositoryQueries.ClassUsage r2) {
                return r1.getArtifact().compareTo(r2.getArtifact());
            }
        });
        ACCESSOR.setClassResults(result, results);
        return result;
    }
    
    @Override
    public RepositoryQueries.Result<NBVersionInfo> findDependencyUsage(final String groupId, final String artifactId, final String version, @NullAllowed List<RepositoryInfo> repos) {
        RepositoryQueries.Result<NBVersionInfo> result = ACCESSOR.createVersionResult(new Redo<NBVersionInfo>() {
            @Override
            public void run(Result<NBVersionInfo> result) {
                findDependencyUsage(groupId, artifactId, version, result, ACCESSOR.getSkipped(result), false);
            }
        });
        return findDependencyUsage(groupId, artifactId, version, result, repos, true);
    }
    private RepositoryQueries.Result<NBVersionInfo> findDependencyUsage(String groupId, String artifactId, String version, final Result<NBVersionInfo> result, @NullAllowed List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Query q = ArtifactDependencyIndexCreator.query(groupId, artifactId, version);
        final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                IteratorSearchResponse response = repeatedPagedSearch(q, Collections.singletonList(context), MAX_RESULT_COUNT);
                if (response != null) {
                    try {
                        for (ArtifactInfo ai : response.iterator()) {
                            infos.add(convertToNBVersionInfo(ai));
                        }
                    } finally {
                        ACCESSOR.addReturnedResults(result, response.getTotalProcessedArtifactInfoCount());
                        ACCESSOR.addTotalResults(result, response.getTotalHitsCount());
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        ACCESSOR.setVersionResults(result, infos);
        return result;
    }
    
    @Override
    public Result<NBGroupInfo> findDependencyUsageGroups(final String groupId, final String artifactId, final String version, List<RepositoryInfo> repos) {
        RepositoryQueries.Result<NBGroupInfo> result = ACCESSOR.createGroupResult(new Redo<NBGroupInfo>() {
            @Override
            public void run(Result<NBGroupInfo> result) {
                findDependencyUsageGroups(groupId, artifactId, version, result, ACCESSOR.getSkipped(result), false);
            }
        });
        return findDependencyUsageGroups(groupId, artifactId, version, result, repos, true);
    }

    private Result<NBGroupInfo> findDependencyUsageGroups(String groupId, String artifactId, String version, Result<NBGroupInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        //tempmaps
        Map<String, NBGroupInfo> groupMap = new HashMap<String, NBGroupInfo>();
        Map<String, NBArtifactInfo> artifactMap = new HashMap<String, NBArtifactInfo>();
        List<NBGroupInfo> groupInfos = new ArrayList<NBGroupInfo>(result.getResults());
        Result<NBVersionInfo> res = ACCESSOR.createVersionResult(new Redo<NBVersionInfo>() {

            @Override
            public void run(Result<NBVersionInfo> result) {
                //noop will not be called
            }
        });
        findDependencyUsage(groupId, artifactId, version, res, repos, skipUnIndexed);
        convertToNBGroupInfo(res.getResults(),
                groupMap, artifactMap, groupInfos);
        if (res.isPartial()) {
            ACCESSOR.addSkipped(result, ACCESSOR.getSkipped(res));
        }
        ACCESSOR.setGroupResults(result, groupInfos);
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
    
    

    @Override
    public RepositoryQueries.Result<NBVersionInfo> findBySHA1(final String sha1, List<RepositoryInfo> repos) {
        RepositoryQueries.Result<NBVersionInfo> result = ACCESSOR.createVersionResult(new Redo<NBVersionInfo>() {
            @Override
            public void run(Result<NBVersionInfo> result) {
                findBySHA1(sha1, result, ACCESSOR.getSkipped(result), false);
            }
        });
        return findBySHA1(sha1, result, repos, true);
    }
    
    private RepositoryQueries.Result<NBVersionInfo> findBySHA1(final String sha1, final Result<NBVersionInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                bq.add(new BooleanClause((setBooleanRewrite(indexer.constructQuery(MAVEN.SHA1, new StringSearchExpression(sha1)))), BooleanClause.Occur.SHOULD));
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), MAX_RESULT_COUNT);
                if (response != null) {
                    try {
                        for (ArtifactInfo ai : response.iterator()) {
                            infos.add(convertToNBVersionInfo(ai));
                        }
                    } finally {
                        ACCESSOR.addReturnedResults(result, response.getTotalProcessedArtifactInfoCount());
                        ACCESSOR.addTotalResults(result, response.getTotalHitsCount());
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        Collections.sort(infos);
        ACCESSOR.setVersionResults(result, infos);
        return result;
    }

    @Override
    public RepositoryQueries.Result<NBVersionInfo> findArchetypes(List<RepositoryInfo> repos) {
        RepositoryQueries.Result<NBVersionInfo> result = ACCESSOR.createVersionResult(new Redo<NBVersionInfo>() {
            @Override
            public void run(Result<NBVersionInfo> result) {
                findArchetypes( result, ACCESSOR.getSkipped(result), false);
            }
        });
        return findArchetypes( result, repos, true);
    }
    
    private RepositoryQueries.Result<NBVersionInfo> findArchetypes(final Result<NBVersionInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                // XXX also consider using NexusArchetypeDataSource
                bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-archetype")), BooleanClause.Occur.MUST)); //NOI18N
                FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                /* There are >512 archetypes in Central, and we want them all in ChooseArchetypePanel
                fsr.setCount(MAX_RESULT_COUNT);
                */
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), NO_CAP_RESULT_COUNT);
                if (response != null) {
                    try {
                        for (ArtifactInfo ai : response.iterator()) {
                            infos.add(convertToNBVersionInfo(ai));
                        }
                    } finally {
                        ACCESSOR.addReturnedResults(result, response.getTotalProcessedArtifactInfoCount());
                        ACCESSOR.addTotalResults(result, response.getTotalHitsCount());
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        Collections.sort(infos);
        ACCESSOR.setVersionResults(result, infos);
        return result;
    }

    @Override
    public RepositoryQueries.Result<String> filterPluginArtifactIds(final String groupId, final String prefix, List<RepositoryInfo> repos) {
        RepositoryQueries.Result<String> result = ACCESSOR.createStringResult(new Redo<String>() {
            @Override
            public void run(Result<String> result) {
                filterPluginArtifactIds(groupId, prefix, result, ACCESSOR.getSkipped(result), false);
            }
        });
        return filterPluginArtifactIds(groupId, prefix, result, repos, true);
    }
    
    private RepositoryQueries.Result<String> filterPluginArtifactIds(final String groupId, final String prefix, Result<String> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Set<String> artifacts = new TreeSet<String>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                String id = groupId + ArtifactInfo.FS + prefix;
                bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-plugin")), BooleanClause.Occur.MUST));
                bq.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST));
                //mkleint: this is not capped, because only a string is collected (and collapsed), the rest gets CGed fast
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), NO_CAP_RESULT_COUNT);
                if (response != null) {
                    try {
                        for (ArtifactInfo artifactInfo : response.getResults()) {
                            artifacts.add(artifactInfo.artifactId);
                        }
                    } finally {
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        ACCESSOR.setStringResults(result, artifacts);
        return result;
    }

    @Override
    public RepositoryQueries.Result<String> filterPluginGroupIds(final String prefix, List<RepositoryInfo> repos) {
        RepositoryQueries.Result<String> result = ACCESSOR.createStringResult(new Redo<String>() {
            @Override
            public void run(Result<String> result) {
                filterPluginGroupIds( prefix, result, ACCESSOR.getSkipped(result), false);
            }
        });
        return filterPluginGroupIds( prefix, result, repos, true);
    }
    
    private RepositoryQueries.Result<String> filterPluginGroupIds(final String prefix, Result<String> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Set<String> artifacts = new TreeSet<String>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-plugin")), BooleanClause.Occur.MUST));
                if (prefix.length() > 0) { //heap out of memory otherwise
                    bq.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.GROUP_ID, prefix))), BooleanClause.Occur.MUST));
                }
                //mkleint: this is not capped, because only a string is collected (and collapsed), the rest gets CGed fast
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), NO_CAP_RESULT_COUNT);
                if (response != null) {
                    try {
                        for (ArtifactInfo artifactInfo : response.getResults()) {
                            artifacts.add(artifactInfo.groupId);
                        }
                    } finally {
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        ACCESSOR.setStringResults(result, artifacts);
        return result;
    }

    @Override
    public RepositoryQueries.Result<String> filterArtifactIdForGroupId(final String groupId, final String prefix, List<RepositoryInfo> repos) {
        RepositoryQueries.Result<String> result = ACCESSOR.createStringResult(new Redo<String>() {
            @Override
            public void run(Result<String> result) {
                filterArtifactIdForGroupId( groupId, prefix, result, ACCESSOR.getSkipped(result), false);
            }
        });
        return filterArtifactIdForGroupId( groupId, prefix, result, repos, true);
    }
    private RepositoryQueries.Result<String> filterArtifactIdForGroupId(final String groupId, final String prefix, Result<String> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Set<String> artifacts = new TreeSet<String>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                String id = groupId + ArtifactInfo.FS + prefix;
                bq.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST));
                //mkleint: this is not capped, because only a string is collected (and collapsed), the rest gets CGed fast
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), NO_CAP_RESULT_COUNT);
                if (response != null) {
                    try {
                        for (ArtifactInfo artifactInfo : response.getResults()) {
                            artifacts.add(artifactInfo.artifactId);
                        }
                    } finally {
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);       
        ACCESSOR.setStringResults(result, artifacts);
        return result;
    }

    @Override
    public RepositoryQueries.Result<NBVersionInfo> find(final List<QueryField> fields, List<RepositoryInfo> repos) {
        RepositoryQueries.Result<NBVersionInfo> result = ACCESSOR.createVersionResult(new Redo<NBVersionInfo>() {
            @Override
            public void run(Result<NBVersionInfo> result) {
                find( fields, result, ACCESSOR.getSkipped(result), false);
            }
        });
        return find(fields,  result, repos, true);
    }
    private RepositoryQueries.Result<NBVersionInfo> find(final List<QueryField> fields, final Result<NBVersionInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                for (QueryField field : fields) {
                    BooleanClause.Occur occur = field.getOccur() == QueryField.OCCUR_SHOULD ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST;
                    String fieldName = toNexusField(field.getField());
                    String one = field.getValue();
                    while (!one.isEmpty() && (one.startsWith("*") || one.startsWith("?"))) {
                        //#196046
                        one = one.substring(1);
                    }
                    if (one.isEmpty()) {
                        continue;
                    }

                    if (fieldName != null) {
                        Query q;
                        if (ArtifactInfo.NAMES.equals(fieldName)) {
                            try {
                                String clsname = one.replace(".", "/"); //NOI18N
                                q = indexer.constructQuery(MAVEN.CLASSNAMES, new StringSearchExpression(clsname.toLowerCase(Locale.ENGLISH)));
                            } catch (IllegalArgumentException iae) {
                                //#204651 only escape when problems occur
                                String clsname = QueryParser.escape(one.replace(".", "/")); //NOI18N
                                try {
                                    q = indexer.constructQuery(MAVEN.CLASSNAMES, new StringSearchExpression(clsname.toLowerCase(Locale.ENGLISH)));
                                } catch (IllegalArgumentException iae2) {
                                    //#224088
                                    continue;
                                }
                            }
                        } else if (ArtifactInfo.ARTIFACT_ID.equals(fieldName)) {
                            try {
                                q = indexer.constructQuery(MAVEN.ARTIFACT_ID, new StringSearchExpression(one));
                            } catch (IllegalArgumentException iae) {
                                //#204651 only escape when problems occur
                                try {
                                    q = indexer.constructQuery(MAVEN.ARTIFACT_ID, new StringSearchExpression(QueryParser.escape(one)));
                                } catch (IllegalArgumentException iae2) {
                                    //#224088
                                    continue;
                                }
                            }
                        } else {
                            if (field.getMatch() == QueryField.MATCH_EXACT) {
                                q = new TermQuery(new Term(fieldName, one));
                            } else {
                                q = new PrefixQuery(new Term(fieldName, one));
                            }
                        }
                        bq.add(new BooleanClause(setBooleanRewrite(q), occur));
                    } else {
                        //TODO when all fields, we need to create separate
                        //queries for each field.
                    }
                }
                IteratorSearchResponse resp = repeatedPagedSearch(bq, Collections.singletonList(context), MAX_RESULT_COUNT);
                if (resp != null) {
                    try {
                        for (ArtifactInfo ai : resp.iterator()) {
                            infos.add(convertToNBVersionInfo(ai));
                        }
                    } finally {
                        ACCESSOR.addReturnedResults(result, resp.getTotalProcessedArtifactInfoCount());
                        ACCESSOR.addTotalResults(result, resp.getTotalHitsCount());
                        resp.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        Collections.sort(infos);
        ACCESSOR.setVersionResults(result, infos);
        return result;
    }

    @Override
    public List<RepositoryInfo> getLoaded(final List<RepositoryInfo> repos) {
        final List<RepositoryInfo> toRet = new ArrayList<RepositoryInfo>(repos.size());
        for (final RepositoryInfo repo : repos) {
            File loc = new File(getDefaultIndexLocation(), repo.getId()); // index folder
            try {
                if (loc.exists() && new File(loc, "timestamp").exists() && IndexReader.indexExists(new SimpleFSDirectory(loc))) {
                    toRet.add(repo);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.FINER, "Index Not Available: " +repo.getId() + " at: " + loc.getAbsolutePath(), ex);
            }
        }
        return toRet;
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

    private Collection<NBVersionInfo> postProcessClasses(IteratorResultSet artifactInfos, String classname) {
        List<NBVersionInfo> toRet = new ArrayList<NBVersionInfo>();
        int patter = Pattern.DOTALL + Pattern.MULTILINE;
        boolean isPath = classname.contains("/");
        if (isPath) {
            for (ArtifactInfo i : artifactInfos) {
                toRet.add(convertToNBVersionInfo(i));
            }
            return toRet;
        }
        //if I got it right, we need an exact match of class name, which the query doesn't provide? why?
        String pattStr = ".*/" + classname + "$.*";
        Pattern patt = Pattern.compile(pattStr, patter);
        //#217932 for some reason IteratorResultSet implementation decided
        //not to implemenent Iterator.remove().
        //we need to copy to our own list instead of removing from original.
        ArrayList<ArtifactInfo> altArtifactInfos = new ArrayList<ArtifactInfo>();
        Iterator<ArtifactInfo> it = artifactInfos.iterator();        
        while (it.hasNext()) {
            ArtifactInfo ai = it.next();
            Matcher m = patt.matcher(ai.classNames);
            if (m.matches()) {
                altArtifactInfos.add(ai);
            }
        }
        for (ArtifactInfo i : altArtifactInfos) {
            toRet.add(convertToNBVersionInfo(i));
        }
        return toRet;
    }

    static List<NBVersionInfo> convertToNBVersionInfo(Collection<ArtifactInfo> artifactInfos) {
        List<NBVersionInfo> bVersionInfos = new ArrayList<NBVersionInfo>();
        for (ArtifactInfo ai : artifactInfos) {
            NBVersionInfo nbvi = convertToNBVersionInfo(ai);
            if (nbvi != null) {
              bVersionInfos.add(nbvi);
            }
        }
        return bVersionInfos;
    }
    static NBVersionInfo convertToNBVersionInfo(ArtifactInfo ai) {
            if ("javadoc".equals(ai.classifier) || "sources".equals(ai.classifier)) { //NOI18N
                // we don't want javadoc and sources shown anywhere, we use the getJavadocExists(), getSourceExists() methods.
            return null;
            }
            // fextension != packaging - e.g a pom could be packaging "bundle" but from type/extension "jar"
            NBVersionInfo nbvi = new NBVersionInfo(ai.repository, ai.groupId, ai.artifactId,
                    ai.version, ai.fextension, ai.packaging, ai.name, ai.description, ai.classifier);
            /*Javadoc & Sources*/
            nbvi.setJavadocExists(ai.javadocExists == ArtifactAvailablility.PRESENT);
            nbvi.setSourcesExists(ai.sourcesExists == ArtifactAvailablility.PRESENT);
            nbvi.setSignatureExists(ai.signatureExists == ArtifactAvailablility.PRESENT);
//            nbvi.setSha(ai.sha1);
            nbvi.setLastModified(ai.lastModified);
            nbvi.setSize(ai.size);
        nbvi.setLuceneScore(ai.getLuceneScore());
        return nbvi;
        }
    
    private static Query setBooleanRewrite (final Query q) {
        if (q instanceof MultiTermQuery) {
            ((MultiTermQuery)q).setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE);
        } else if (q instanceof BooleanQuery) {
            for (BooleanClause c : ((BooleanQuery)q).getClauses()) {
                setBooleanRewrite(c.getQuery());
            }
        }
        return q;
    }

    private static class SkippedAction implements RepoAction {

        private final Result<?> result;

        private SkippedAction(Result<?> result) {
            this.result = result;
}
        
        @Override
        public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
            //indexing context is always null here..
            ACCESSOR.addSkipped(result, repo);
        }
        
    }

    private static class NoJavadocSourceFilter implements ArtifactInfoFilter {

        @Override
        public boolean accepts(IndexingContext ctx, ArtifactInfo ai) {
            if ("javadoc".equals(ai.classifier) || "sources".equals(ai.classifier)) {
                return false;
}
            return true;
        }
        
    }

}
