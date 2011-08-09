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

import org.netbeans.modules.maven.indexer.spi.ClassUsageQuery;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import org.apache.maven.index.expr.StringSearchExpression;
import org.codehaus.plexus.util.FileUtils;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidArtifactRTException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.index.ArtifactAvailablility;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactContextProducer;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.DefaultArtifactContextProducer;
import org.apache.maven.index.Field;
import org.apache.maven.index.FlatSearchRequest;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.IndexerField;
import org.apache.maven.index.search.grouping.GGrouping;
import org.apache.maven.index.GroupedSearchRequest;
import org.apache.maven.index.GroupedSearchResponse;
import org.apache.maven.index.IndexerFieldVersion;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.NexusIndexer;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.creator.AbstractIndexCreator;
import org.apache.maven.index.SearchEngine;
import org.apache.maven.index.artifact.ArtifactPackagingMapper;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexUtils;
import org.apache.maven.index.creator.JarFileContentsIndexCreator;
import org.apache.maven.index.creator.MavenArchetypeArtifactInfoIndexCreator;
import org.apache.maven.index.creator.MavenPluginArtifactInfoIndexCreator;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdater;
import org.apache.maven.index.updater.ResourceFetcher;
import org.apache.maven.index.updater.WagonHelper;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.settings.Mirror;
import org.apache.maven.wagon.Wagon;
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
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.Base64;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.indexer.spi.ContextLoadedQuery;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.repository.DefaultMirrorSelector;

@ServiceProvider(service=RepositoryIndexerImplementation.class)
public class NexusRepositoryIndexerImpl implements RepositoryIndexerImplementation,
        BaseQueries, ChecksumQueries, ArchetypeQueries, DependencyInfoQueries,
        ClassesQuery, ClassUsageQuery, GenericFindQuery, ContextLoadedQuery {

    private PlexusContainer embedder;
    private ArtifactRepository repository;
    private NexusIndexer indexer;
    private SearchEngine searcher;
    private IndexUpdater remoteIndexUpdater;
    private ArtifactContextProducer contextProducer;
    private boolean inited = false;
    /*Indexer Keys*/
    private static final String NB_DEPENDENCY_GROUP = "nbdg"; //NOI18N
    private static final String NB_DEPENDENCY_ARTIFACT = "nbda"; //NOI18N
    private static final String NB_DEPENDENCY_VERSION = "nbdv"; //NOI18N
    /** Used for indexing all classes in a local repository artifact: bar.Foo&lt;foo.Bar (class bar.Foo is used by class for.Bar)
     *  &lt; may be confusing to read, but will speed up the lucene prefix search: query: bar.Foo&lt;*
     */
    private static final String NB_DEPENDENCY_CLASSES = "nbdc"; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(NexusRepositoryIndexerImpl.class.getName());
    /*custom Index creators*/
    /**
     * any reads, writes from/to index shal be done under mutex access.
     */
    private static final HashMap<String,Mutex> repoMutexMap = new HashMap<String, Mutex>(4);

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
    
    private Lookup lookup;

    private static final int MAX_RESULT_COUNT = 512;

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

                repository = EmbedderFactory.getProjectEmbedder().getLocalRepository();
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

    /** Copied from org.netbeans:nexus-for-netbeans. Workaround for anomalous classifier behavior of nbm-maven-plugin. */
    public static final class CustomArtifactContextProducer extends DefaultArtifactContextProducer {
        @Override public ArtifactContext getArtifactContext(IndexingContext context, File file) {
            ArtifactContext ac = super.getArtifactContext(context, file);
            if (ac != null) {
                ArtifactInfo ai = ac.getArtifactInfo();
                if ("nbm".equals(ai.fextension)) {
                    return new ArtifactContext(ac.getPom(), ac.getArtifact(), ac.getMetadata(), new ArtifactInfo(ai.repository, ai.groupId, ai.artifactId, ai.version, null) {
                        private String uinfo = null;
                        public @Override String getUinfo() {
                            if (uinfo == null) {
                                uinfo = new StringBuilder().append(groupId).append(FS).append(artifactId).append(FS).append(version).append(FS)
                                        .append(NA) // no classifier in this case
                                        .append(FS).append(packaging) // would otherwise omit this
                                        .toString();
                            }
                            return uinfo;
                        }
                    }, ac.getGav());
                }
            }
            return ac;
        }
    }

    //always call from mutex.writeAccess
    private void loadIndexingContext(final RepositoryInfo info) throws IOException {
        LOAD: {
            assert getRepoMutex(info).isWriteAccess();
            initIndexer();

            IndexingContext context = indexer.getIndexingContexts().get(info.getId());
            String indexUpdateUrl = findIndexUpdateUrlConsideringMirrors(info);
            if (context != null) {
                String contexturl = context.getIndexUpdateUrl();
                File contextfile = context.getRepository();
                File repofile = info.getRepositoryPath() != null ? new File(info.getRepositoryPath()) : null;
                //try to figure if context reload is necessary
                if (!Utilities.compareObjects(contexturl, indexUpdateUrl)) {
                    LOGGER.log(Level.FINE, "Remote context changed: {0}, unload/load", info.getId());
                    unloadIndexingContext(info);
                } else if (!Utilities.compareObjects(contextfile, repofile)) {
                    LOGGER.log(Level.FINE, "Local context changed: {0}, unload/load", info.getId());
                    unloadIndexingContext(info);
                } else {
                    LOGGER.log(Level.FINER, "Skipping Context: {0}, already loaded.", info.getId());
                    break LOAD; // XXX does it suffice to just return here, or is code after block needed?
                }
            }
            LOGGER.log(Level.FINE, "Loading Context: {0}", info.getId());
            if (info.isLocal() || info.isRemoteDownloadable()) {
                File loc = new File(getDefaultIndexLocation(), info.getId()); // index folder
                boolean index = false;
                if (!loc.exists() || loc.listFiles().length <= 0) {
                    index = true;
                    LOGGER.log(Level.FINER, "Index Not Available: {0} at: {1}", new Object[] {info.getId(), loc.getAbsolutePath()});
                }

                List<IndexCreator> creators = new ArrayList<IndexCreator>();
                try {
                    // XXX MINDEXER-34: maven-plugin must follow min, so using embedder.lookupList(IndexCreator.class) does not work
                    for (String id : new String[] {MinimalArtifactInfoIndexCreator.ID, MavenPluginArtifactInfoIndexCreator.ID, MavenArchetypeArtifactInfoIndexCreator.ID, JarFileContentsIndexCreator.ID}) {
                        creators.add(embedder.lookup(IndexCreator.class, id));
                    }
                } catch (ComponentLookupException x) {
                    throw new IOException(x);
                }
                if (info.isLocal()) { // #164593
                    creators.add(new NbIndexCreator());
                    creators.add(new NbClassDependenciesIndexCreator());
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
                    LOGGER.log(Level.INFO, "Found a broken index at " + loc.getAbsolutePath(), ex);
                    FileUtils.deleteDirectory(loc);
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NexusRepositoryIndexerImpl.class, "MSG_Reconstruct_Index"));
                    try {
                    indexer.addIndexingContextForced(
                            info.getId(), // context id
                            info.getId(), // repository id
                            info.isLocal() ? new File(info.getRepositoryPath()) : null, // repository folder
                            loc,
                            info.isRemoteDownloadable() ? info.getRepositoryUrl() : null, // repositoryUrl
                            info.isRemoteDownloadable() ? indexUpdateUrl : null,
                            creators);
                    } catch (LockObtainFailedException x) {
                        LOGGER.log(Level.INFO, "#195357: could not clean up from broken index", x);
                    }
                }
                if (index) {
                    indexLoadedRepo(info, true);
                }
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
    }

    private String findIndexUpdateUrlConsideringMirrors(RepositoryInfo info) { // #192064
        String direct = info.getIndexUpdateUrl();
        if (direct == null) {
            return null; // local
        }
        MavenEmbedder embedder2 = EmbedderFactory.getOnlineEmbedder();
        DefaultMirrorSelector selectorNoGroups = new DefaultMirrorSelector();
        DefaultMirrorSelector selectorWithGroups = new DefaultMirrorSelector();
        for (Mirror mirror : embedder2.getSettings().getMirrors()) {
            String mirrorOf = mirror.getMirrorOf();
            if (!mirrorOf.contains("*")/* XXX list might be used just for variant repo names: && !mirrorOf.contains(",")*/) {
                selectorNoGroups.add(mirror.getId(), mirror.getUrl(), mirror.getLayout(), false, mirrorOf, mirror.getMirrorOfLayouts());
            }
            selectorWithGroups.add(mirror.getId(), mirror.getUrl(), mirror.getLayout(), false, mirrorOf, mirror.getMirrorOfLayouts());
        }
        RemoteRepository original = new RemoteRepository(info.getId(), /* XXX do we even support any other layout?*/"default", info.getRepositoryUrl());
        RemoteRepository mirrored = selectorNoGroups.getMirror(original);
        if (mirrored != null) {
            String index = addIndex(mirrored.getUrl());
            LOGGER.log(Level.FINE, "Mirroring {0} to {1}", new Object[] {direct, index});
            return index;
        } else {
            mirrored = selectorWithGroups.getMirror(original);
            if (mirrored != null) {
                // XXX consider displaying warning in GUI; use NbPreferences.root().node("org/netbeans/modules/maven/showQuestions")
                LOGGER.log(Level.WARNING, "Will not mirror {0} to {1}", new Object[] {direct, addIndex(mirrored.getUrl())});
            } else {
                LOGGER.log(Level.FINE, "No mirror for {0}", direct);
            }
            return direct;
        }
    }
    private String addIndex(String baseURL) {
        if (!baseURL.endsWith("/")) {
            baseURL += "/";
        }
        return baseURL + ".index/"; // NOI18N
    }

    /**
     * get the IndexingContext instances for the given RepositoryInfo instances.
     */
    private Collection<IndexingContext> getContexts(RepositoryInfo[] allrepos) {
        List<IndexingContext> toRet = new ArrayList<IndexingContext>();
        for (RepositoryInfo info : allrepos) {
            assert getRepoMutex(info).isWriteAccess();
            IndexingContext context = indexer.getIndexingContexts().get(info.getId());
            if (context != null) {
                toRet.add(context);
            } else {
                if (info.isLocal() || info.isRemoteDownloadable()) {
                    LOGGER.log(Level.WARNING, "The context ''{0}'' is not loaded.", info.getId());
                }
                //else ignore, is not a real nexus repo, is missing any indexing properties..
            }
        }
        return toRet;
    }

    private FlatSearchResponse repeatedFlatSearch(FlatSearchRequest fsr, final Collection<IndexingContext> contexts, boolean shouldThrow) throws IOException {

        int MAX_MAX_CLAUSE = 1<<11;  // conservative maximum for too general queries, like "c:*class*"
        
        Query q = fsr.getQuery();
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
                FlatSearchResponse response;
                try {
                    BooleanQuery.setMaxClauseCount(max);
                    response = searcher.searchFlatPaged(fsr, contexts);
                    LOGGER.log(Level.FINE, "passed on {0} clauses processing {1} with {2} hits", new Object[] {max, q, response.getTotalHitsCount()});
                    return response;
                } catch (BooleanQuery.TooManyClauses exc) {
                    LOGGER.log(Level.FINE, "TooManyClauses on {0} clauses processing {1}", new Object[] {max, q});
                    max *= 2;
                    if (max > MAX_MAX_CLAUSE) {
                        if (shouldThrow) {
                            throw exc;
                        } else {
                            LOGGER.log(Level.WARNING, "Encountered more than {0} clauses processing {1}", new Object[] {MAX_MAX_CLAUSE, q});
                            return null;
                        }
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
    private void unloadIndexingContext(final RepositoryInfo... repos) throws IOException {
        for (RepositoryInfo repo : repos) {
            assert getRepoMutex(repo).isWriteAccess();
            LOGGER.log(Level.FINER, "Unloading Context: {0}", repo.getId());
            IndexingContext ic = indexer.getIndexingContexts().get(repo.getId());
            if (ic != null) {
                indexer.removeIndexingContext(ic, false);
            }
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

    private void indexLoadedRepo(final RepositoryInfo repo, boolean updateLocal) {
        assert getRepoMutex(repo).isWriteAccess();
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
                    // XXX would use WagonHelper.getWagonResourceFetcher if that were not limited to http protocol
                    ResourceFetcher fetcher = new WagonHelper.WagonFetcher(embedder.lookup(Wagon.class, URI.create(repo.getRepositoryUrl()).getScheme()), listener, null, null);
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
                RepositoryIndexerListener listener = new RepositoryIndexerListener(indexingContext);
                try {
                    indexer.scan(indexingContext, listener, updateLocal);
                } finally {
                    listener.close();
                }
            }
        } catch (Cancellation x) {
            LOGGER.log(Level.INFO, "canceled indexing of {0}", repo.getId());
        } catch (IOException x) {
            LOGGER.log(Level.INFO, "could not index " + repo.getId(), x);
            //handle index not found
        } catch (ComponentLookupException x) {
            LOGGER.log(Level.INFO, "could not find protocol handler for " + repo.getRepositoryUrl(), x);
        } catch (RuntimeException x) {
            LOGGER.log(Level.WARNING, "could not index " + repo.getId(), x);
        } finally {
            RepositoryPreferences.getInstance().setLastIndexUpdate(repo.getId(), new Date());
            fireChangeIndex(repo);
        }
    }
    /** Just tracks what is being unpacked after a remote index has been downloaded. */
    private static final class NotifyingIndexCreator implements IndexCreator {
        private RemoteIndexTransferListener listener;
        NotifyingIndexCreator() {}
        private void start(RemoteIndexTransferListener listener) {
            this.listener = listener;
        }
        private void end() {
            listener = null;
        }
        public @Override void updateDocument(ArtifactInfo artifactInfo, Document document) {
            listener.unpackingProgress(artifactInfo.groupId + ':' + artifactInfo.artifactId);
        }
        public @Override Collection<IndexerField> getIndexerFields() {
            return Collections.emptySet();
        }
        public @Override void populateArtifactInfo(ArtifactContext artifactContext) throws IOException {}
        public @Override boolean updateArtifactInfo(Document document, ArtifactInfo artifactInfo) {
            return false;
        }
    }

    @Override
    public void indexRepo(final RepositoryInfo repo) {
        LOGGER.log(Level.FINER, "Indexing Context: {0}", repo);
        try {
            RemoteIndexTransferListener.addToActive(Thread.currentThread());
            getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                public @Override Void run() throws Exception {
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

        try {
            getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                public @Override Void run() throws Exception {
                    loadIndexingContext(repo);
                    Map<String, IndexingContext> indexingContexts = indexer.getIndexingContexts();
                    IndexingContext indexingContext = indexingContexts.get(repo.getId());
                    if (indexingContext == null) {
                        LOGGER.log(Level.WARNING, "Indexing context could not be created: {0}", repo.getId());
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
            getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                public @Override Void run() throws Exception {
                    loadIndexingContext(repo);
                    Map<String, IndexingContext> indexingContexts = indexer.getIndexingContexts();
                    IndexingContext indexingContext = indexingContexts.get(repo.getId());
                    if (indexingContext == null) {
                        LOGGER.log(Level.WARNING, "Indexing context chould not be created: {0}", repo.getId());
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
    public Set<String> getGroups(List<RepositoryInfo> repos) {
        return filterGroupIds("", repos);
    }

    @Override
    public Set<String> filterGroupIds(final String prefix, final List<RepositoryInfo> repos) {
        try {
            final Set<String> groups = new TreeSet<String>();
            final List<RepositoryInfo> slowCheck = new ArrayList<RepositoryInfo>();
            for (final RepositoryInfo repo : repos) {
                getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                    public @Override Void run() throws Exception {
                        loadIndexingContext(repo);
                        if (repo.isLocal() || repo.isRemoteDownloadable()) {
                            IndexingContext context = indexer.getIndexingContexts().get(repo.getId());
                            if (context == null) {
                                return null;
                            }
                            Set<String> all;
                            try {
                                all = context.getAllGroups();
                            } catch (FileNotFoundException x) {
                                LOGGER.log(Level.INFO, "#179624: corrupt index?", x);
                                all = Collections.emptySet();
                            }
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
                        return null;
                    }
                });
            }

            for (final RepositoryInfo slowrepo : slowCheck) {
                getRepoMutex(slowrepo).writeAccess(new Mutex.ExceptionAction<Void>() {
                    public @Override Void run() throws Exception {
                        BooleanQuery bq = new BooleanQuery();
                        bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, prefix)), BooleanClause.Occur.MUST));
                        GroupedSearchRequest gsr = new GroupedSearchRequest(bq, new GGrouping(),
                                new Comparator<String>() {

                                    @Override
                                    public int compare(String o1, String o2) {
                                        return o1.compareTo(o2);
                                    }
                                });
                        try {
                            GroupedSearchResponse response = searcher.searchGrouped(gsr, getContexts(new RepositoryInfo[]{slowrepo}));
                            groups.addAll(response.getResults().keySet());

                        } catch (IOException ioe) {
                        }
                        return null;
                    }
                });
            }
            return groups;
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String>emptySet();
    }

    @Override
    public List<NBVersionInfo> getRecords(final String groupId, final String artifactId, final String version, List<RepositoryInfo> repos) {
        try {
            final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
            for (final RepositoryInfo repo : repos) {
                getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                    public @Override Void run() throws Exception {
                        loadIndexingContext(repo);
                        BooleanQuery bq = new BooleanQuery();
                        String id = groupId + ArtifactInfo.FS + artifactId + ArtifactInfo.FS + version + ArtifactInfo.FS;
                        bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST));
                        FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                        fsr.setCount(MAX_RESULT_COUNT);
                        FlatSearchResponse response = searcher.searchFlatPaged(fsr, getContexts(new RepositoryInfo[] { repo }));
                        infos.addAll(convertToNBVersionInfo(response.getResults()));
                        return null;
                    }
                });
            }
            return infos;
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    @Override
    public Set<String> getArtifacts(final String groupId, final List<RepositoryInfo> repos) {
        try {
            final Set<String> artifacts = new TreeSet<String>();
            for (final RepositoryInfo repo : repos) {
                getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                    public @Override Void run() throws Exception {
                        BooleanQuery bq = new BooleanQuery();
                        loadIndexingContext(repo);
                        String id = groupId + ArtifactInfo.FS;
                        bq.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST));
                        FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                        FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(new RepositoryInfo[] { repo }), false);
                        if (response != null) {
                            for (ArtifactInfo artifactInfo : response.getResults()) {
                                artifacts.add(artifactInfo.artifactId);
                            }
                        }
                        return null;
                    }
                });
            }
            return artifacts;
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String>emptySet();
    }

    @Override
    public List<NBVersionInfo> getVersions(final String groupId, final String artifactId, List<RepositoryInfo> repos) {
        try {
            final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
            for (final RepositoryInfo repo : repos) {
                getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                    public @Override Void run() throws Exception {
                        loadIndexingContext(repo);
                        BooleanQuery bq = new BooleanQuery();
                        String id = groupId + ArtifactInfo.FS + artifactId + ArtifactInfo.FS;
                        bq.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST));
                        FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                        FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(new RepositoryInfo[]{repo}), false);
                        if (response != null) {
                            infos.addAll(convertToNBVersionInfo(response.getResults()));
                        }
                        return null;
                    }
                });
            }
            return infos;
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    @Override
    public List<NBVersionInfo> findVersionsByClass(final String className, List<RepositoryInfo> repos) {
        try {
            final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
            for (final RepositoryInfo repo : repos) {
                getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                    public @Override Void run() throws Exception {
                        loadIndexingContext(repo);
                        String clsname = className.replace(".", "/");
                        FlatSearchRequest fsr = new FlatSearchRequest(setBooleanRewrite(
                                indexer.constructQuery(MAVEN.CLASSNAMES, new StringSearchExpression(clsname.toLowerCase()))),
                                ArtifactInfo.VERSION_COMPARATOR);
                        fsr.setCount(MAX_RESULT_COUNT);
                        FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(new RepositoryInfo[]{repo}), false);
                        if (response != null) {
                            infos.addAll(convertToNBVersionInfo(postProcessClasses(response.getResults(),
                                    clsname)));
                        }
                        return null;
                    }
                });
            }
            return infos;
        } catch (MutexException ex) {
            rethrowTooManyClauses(ex);
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    /**
     * @param s a string, such as a class name
     * @return the CRC-32 of its UTF-8 representation, as big-endian Base-64 without padding (so six chars), with _ for + (safer for Lucene)
     */
    static String crc32base64(String s) {
        crc.reset();
        crc.update(s.getBytes(UTF8));
        long v = crc.getValue();
        byte[] b64 = Base64.encodeBase64(new byte[] {(byte) (v >> 24 & 0xFF), (byte) (v >> 16 & 0xFF), (byte) (v >> 8 & 0xFF), (byte) (v & 0xFF)});
        assert b64.length == 8;
        assert b64[6] == '=';
        assert b64[7] == '=';
        return new String(b64, 0, 6, LATIN1).replace('+', '_');
    }
    private static final CRC32 crc = new CRC32();
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Charset LATIN1 = Charset.forName("ISO-8859-1");

    @Override public List<ClassUsageResult> findClassUsages(final String className, final List<RepositoryInfo> repos) {
        try {
            final List<ClassUsageResult> results = new ArrayList<ClassUsageResult>();
            for (final RepositoryInfo repo : repos) {
                if (repo.isLocal()) {
                    getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                        @Override public Void run() throws Exception {
                            loadIndexingContext(repo);
                            String searchString = crc32base64(className.replace(".", "/"));
                            final Query refClassQuery = indexer.constructQuery(NbClassDependenciesIndexCreator.FLD_NB_DEPENDENCY_CLASS.getOntology(), new StringSearchExpression(searchString));
                            final TopScoreDocCollector collector = TopScoreDocCollector.create(MAX_RESULT_COUNT, true);
                            final Collection<IndexingContext> contexts = getContexts(new RepositoryInfo[]{repo});
                            final IndexingContext context = contexts.iterator().next(); // XXX check for hasNext
                            context.getIndexSearcher().search(refClassQuery, collector);
                            final ScoreDoc[] hits = collector.topDocs().scoreDocs;
                            LOGGER.log(Level.FINER, "for {0} ~ {1} found {2} hits", new Object[] {className, searchString, hits.length});
                            for (ScoreDoc hit : hits) {
                                int docId = hit.doc;
                                Document d = context.getIndexSearcher().doc(docId);
                                String fldValue = d.get(NB_DEPENDENCY_CLASSES);
                                LOGGER.log(Level.FINER, "{0} uses: {1}", new Object[] {className, fldValue});
                                Set<String> refClasses = parseField(searchString, fldValue, d.get(ArtifactInfo.NAMES));
                                if (!refClasses.isEmpty()) {
                                    ArtifactInfo ai = IndexUtils.constructArtifactInfo(d, context);
                                    if (ai != null) {
                                        ai.repository = context.getRepositoryId();
                                        List<NBVersionInfo> version = convertToNBVersionInfo(Collections.singleton(ai));
                                        if (!version.isEmpty()) {
                                            results.add(new ClassUsageResult(version.get(0), refClasses));
                                        }
                                    }
                                }
                            }
                            return null;
                        }
                        private Set<String> parseField(String refereeCRC, String field, String referrersNL) {
                            Set<String> referrers = new TreeSet<String>();
                            int p = 0;
                            for (String referrer : referrersNL.split("\n")) {
                                while (true) {
                                    if (field.charAt(p) == ' ') {
                                        p++;
                                        break;
                                    }
                                    if (field.substring(p, p + 6).equals(refereeCRC)) {
                                        referrers.add(referrer.substring(1).replace('/', '.'));
                                    }
                                    p += 7;
                                }
                            }
                            return referrers;
                        }
                    });
                }
            }
            Collections.sort(results, new Comparator<ClassUsageResult>() {
                @Override public int compare(ClassUsageResult r1, ClassUsageResult r2) {
                    return r1.getArtifact().compareTo(r2.getArtifact());
                }
            });
            return results;
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<ClassUsageResult>emptyList();
    }
    
    @Override
    public List<NBVersionInfo> findDependencyUsage(final String groupId, final String artifactId, final String version, List<RepositoryInfo> repos) {
        try {
            final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
            for (final RepositoryInfo repo : repos) {
                getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                    public @Override Void run() throws Exception {
                        loadIndexingContext(repo);
                        BooleanQuery bq = new BooleanQuery();
                        bq.add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_GROUP, groupId)), BooleanClause.Occur.MUST));
                        bq.add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_ARTIFACT, artifactId)), BooleanClause.Occur.MUST));
                        bq.add(new BooleanClause(new TermQuery(new Term(NB_DEPENDENCY_VERSION, version)), BooleanClause.Occur.MUST));
                        FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                        fsr.setCount(MAX_RESULT_COUNT);
                        FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(new RepositoryInfo[]{repo}), false);
                        if (response != null) {
                            infos.addAll(convertToNBVersionInfo(response.getResults()));
                        }
                        return null;
                    }
                });
            }
            return infos;
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    @Override
    public List<NBVersionInfo> findBySHA1(final String sha1, List<RepositoryInfo> repos) {
        try {
            final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
            for (final RepositoryInfo repo : repos) {
                getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                    public @Override Void run() throws Exception {
                        loadIndexingContext(repo);
                        BooleanQuery bq = new BooleanQuery();
                        bq.add(new BooleanClause((setBooleanRewrite(indexer.constructQuery(MAVEN.SHA1, new StringSearchExpression(sha1)))), BooleanClause.Occur.SHOULD));
                        FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                        fsr.setCount(MAX_RESULT_COUNT);
                        FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(new RepositoryInfo[]{repo}), false);
                        if (response != null) {
                            infos.addAll(convertToNBVersionInfo(response.getResults()));
                        }
                        return null;
                    }
                });
            }
            return infos;
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    @Override
    public List<NBVersionInfo> findArchetypes(List<RepositoryInfo> repos) {
        try {
            final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
            for (final RepositoryInfo repo : repos) {
                getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                    public @Override Void run() throws Exception {
                        loadIndexingContext(repo);
                        BooleanQuery bq = new BooleanQuery();
                        // XXX also consider using NexusArchetypeDataSource
                        bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-archetype")), BooleanClause.Occur.MUST)); //NOI18N
                        FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                        fsr.setCount(MAX_RESULT_COUNT);
                        FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(new RepositoryInfo[]{repo}), false);
                        if (response != null) {
                            List<NBVersionInfo> results = convertToNBVersionInfo(response.getResults());
//                            System.err.println("XXX repo: " + repo.getId() + ":");
//                            for (NBVersionInfo info : results) {
//                                System.err.println("  " + info.getGroupId() + ":" + info.getArtifactId() + ":" + info.getVersion() + ":" + info.getPackaging());
//                            }
                            infos.addAll(results);
                        }
                        return null;
                    }
                });
            }
            return infos;
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    @Override
    public Set<String> filterPluginArtifactIds(final String groupId, final String prefix, List<RepositoryInfo> repos) {
        try {
            final Set<String> artifacts = new TreeSet<String>();
            for (final RepositoryInfo repo : repos) {
                getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                    public @Override Void run() throws Exception {
                        loadIndexingContext(repo);
                        BooleanQuery bq = new BooleanQuery();
                        String id = groupId + ArtifactInfo.FS + prefix;
                        bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-plugin")), BooleanClause.Occur.MUST));
                        bq.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST));
                        FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                        fsr.setCount(MAX_RESULT_COUNT);
                        FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(new RepositoryInfo[]{repo}), false);
                        if (response != null) {
                            for (ArtifactInfo artifactInfo : response.getResults()) {
                                artifacts.add(artifactInfo.artifactId);
                            }
                        }
                        return null;
                    }
                });
            }
            return artifacts;
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String>emptySet();
    }

    @Override
    public Set<String> filterPluginGroupIds(final String prefix, List<RepositoryInfo> repos) {
        try {
            final Set<String> artifacts = new TreeSet<String>();
            for (final RepositoryInfo repo : repos) {
                getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                    public @Override Void run() throws Exception {
                        loadIndexingContext(repo);
                        BooleanQuery bq = new BooleanQuery();
                        bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-plugin")), BooleanClause.Occur.MUST));
                        if (prefix.length() > 0) { //heap out of memory otherwise
                            bq.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.GROUP_ID, prefix))), BooleanClause.Occur.MUST));
                        }
                        FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                        fsr.setCount(MAX_RESULT_COUNT);
                        FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(new RepositoryInfo[]{repo}), false);
                        if (response != null) {
                            for (ArtifactInfo artifactInfo : response.getResults()) {
                                artifacts.add(artifactInfo.groupId);
                            }
                        }
                        return null;
                    }
                });
            }
            return artifacts;
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String>emptySet();
    }

    @Override
    public Set<String> filterArtifactIdForGroupId(final String groupId, final String prefix, List<RepositoryInfo> repos) {
        try {
            final Set<String> artifacts = new TreeSet<String>();
            for (final RepositoryInfo repo : repos) {
                getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                    public @Override Void run() throws Exception {
                        loadIndexingContext(repo);
                        BooleanQuery bq = new BooleanQuery();
                        String id = groupId + ArtifactInfo.FS + prefix;
                        bq.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST));
                        FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                        fsr.setCount(MAX_RESULT_COUNT);
                        FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(new RepositoryInfo[] {repo}), false);
                        if (response != null) {
                            for (ArtifactInfo artifactInfo : response.getResults()) {
                                artifacts.add(artifactInfo.artifactId);
                            }
                        }
                        return null;
                    }
                });
            }
            return artifacts;
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<String>emptySet();
    }

    @Override
    public List<NBVersionInfo> find(final List<QueryField> fields, List<RepositoryInfo> repos) {
        try {
            final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
            for (final RepositoryInfo repo : repos) {
                getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                    public @Override Void run() throws Exception {
                        loadIndexingContext(repo);
                        BooleanQuery bq = new BooleanQuery();
                        for (QueryField field : fields) {
                            BooleanClause.Occur occur = field.getOccur() == QueryField.OCCUR_SHOULD ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST;
                            String fieldName = toNexusField(field.getField());
                            if (fieldName != null) {
                                Query q;
                                if (ArtifactInfo.NAMES.equals(fieldName)) {
                                    String clsname = field.getValue().replace(".", "/"); //NOI18N
                                    q = indexer.constructQuery(MAVEN.CLASSNAMES, new StringSearchExpression(clsname.toLowerCase()));
                                } else if (ArtifactInfo.ARTIFACT_ID.equals(fieldName)) {
                                    q = indexer.constructQuery(MAVEN.ARTIFACT_ID, new StringSearchExpression(field.getValue()));
                                } else {
                                    if (field.getMatch() == QueryField.MATCH_EXACT) {
                                        q = new TermQuery(new Term(fieldName, field.getValue()));
                                    } else {
                                        q = new PrefixQuery(new Term(fieldName, field.getValue()));
                                    }
                                }
                                BooleanClause bc = new BooleanClause(setBooleanRewrite(q), occur);
                                bq.add(bc); //NOI18N
                            } else {
                                //TODO when all fields, we need to create separate
                                //queries for each field.
                            }
                        }
                        FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                        FlatSearchResponse response = repeatedFlatSearch(fsr, getContexts(new RepositoryInfo[] {repo}), false);
                        if (response != null) {
                            infos.addAll(convertToNBVersionInfo(response.getResults()));
                        }
                        return null;
                    }
                });
            }
            return infos;
        } catch (MutexException ex) {
            rethrowTooManyClauses(ex);
            Exceptions.printStackTrace(ex);
        }
        return Collections.<NBVersionInfo>emptyList();
    }

    @Override
    public List<RepositoryInfo> getLoaded(final List<RepositoryInfo> repos) {
        final List<RepositoryInfo> toRet = new ArrayList<RepositoryInfo>(repos.size());
        for (final RepositoryInfo repo : repos) {
            File loc = new File(getDefaultIndexLocation(), repo.getId()); // index folder
            if (loc.exists()) {
                File timestamp = new File(loc, "timestamp"); //NOI18N
                if (timestamp.exists()) {
                    toRet.add(repo);
                }
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
            bVersionInfos.add(nbvi);
        }
        return bVersionInfos;
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
    
    /**
     * Scans classes in (local) JARs for their Java dependencies.
     */
    private static class NbClassDependenciesIndexCreator extends AbstractIndexCreator {

        NbClassDependenciesIndexCreator() {}

        // XXX should rather be Map<ArtifactInfo,...> so we do not rely on interleaving of populateArtifactInfo vs. updateDocument
        /** class/in/this/Jar -> [foreign/Class, other/foreign/Nested$Class] */
        private Map<String,Set<String>> classDeps;
        
        public @Override void populateArtifactInfo(ArtifactContext context) throws IOException {
            classDeps = null;
            ArtifactInfo ai = context.getArtifactInfo();
            if (ai.classifier != null) {
                return;
            }
            if ("pom".equals(ai.packaging) || ai.fextension.endsWith(".lastUpdated")) {
                return;
            }
            File jar = context.getArtifact();
            if (jar == null || !jar.isFile()) {
                LOGGER.log(Level.FINER, "no artifact for {0}", ai); // not a big deal, maybe just *.pom (or *.pom + *.nbm) here
                return;
            }
            if (!ai.packaging.equals("jar") && !isArchiveFile(jar)) {
                LOGGER.log(Level.FINE, "skipping artifact {0} with unrecognized packaging based on {1}", new Object[] {ai, jar});
                return;
            }
            LOGGER.log(Level.FINER, "reading {0}", jar);
            Map<String,byte[]> classfiles = read(jar);
            classDeps = new HashMap<String,Set<String>>();
            Set<String> classes = classfiles.keySet();
            for (Map.Entry<String,byte[]> entry : classfiles.entrySet()) {
                addDependenciesToMap(entry.getKey(), entry.getValue(), classDeps, classes, jar);
            }
        }

        // adapted from FileUtil, since we do not want to have to use FileObject's here
        private static boolean isArchiveFile(File jar) throws IOException {
            InputStream in = new FileInputStream(jar);
            try {
                byte[] buffer = new byte[4];
                return in.read(buffer, 0, 4) == 4 && (Arrays.equals(ZIP_HEADER_1, buffer) || Arrays.equals(ZIP_HEADER_2, buffer));
            } finally {
                in.close();
            }
        }
        private static byte[] ZIP_HEADER_1 = {0x50, 0x4b, 0x03, 0x04};
        private static byte[] ZIP_HEADER_2 = {0x50, 0x4b, 0x05, 0x06};

        @Override public boolean updateArtifactInfo(Document document, ArtifactInfo artifactInfo) {
            return false;
        }
        
        public @Override void updateDocument(ArtifactInfo ai, Document doc) {
            if (classDeps == null || classDeps.isEmpty()) {
                return;
            }
            if (ai.classNames == null) {
                // Might be *.hpi, *.war, etc. - so JarFileContentsIndexCreator ignores it (and our results would anyway be wrong due to WEB-INF/classes/ prefix)
                LOGGER.log(Level.FINE, "no class names in index for {0}; therefore cannot store class usages", ai);
                return;
            }
            StringBuilder b = new StringBuilder();
            String[] classNamesSplit = ai.classNames.split("\n");
            for (String referrerTopLevel : classNamesSplit) {
                Set<String> referees = classDeps.remove(referrerTopLevel./* strip off initial slash */substring(1));
                if (referees != null) {
                    for (String referee : referees) {
                        b.append(crc32base64(referee));
                        b.append(' ');
                    }
                }
                b.append(' ');
            }
            if (!classDeps.isEmpty()) {
                // E.g. findbugs-1.2.0.jar has TigerSubstitutes.class, TigerSubstitutesTest$Foo.class, etc., but no TigerSubstitutesTest.class (?)
                // Or guice-3.0-rc2.jar has e.g. $Transformer.class with no source equivalent.
                LOGGER.log(Level.FINE, "found dependencies for {0} from classes {1} not among {2}", new Object[] {ai, classDeps.keySet(), Arrays.asList(classNamesSplit)});
            }
            LOGGER.log(Level.FINER, "Class dependencies index field: {0}", b);
            // XXX is it possible to _store_ something more compact (binary) using a custom tokenizer?
            // seems like DefaultIndexingContext hardcodes NexusAnalyzer
            doc.add(FLD_NB_DEPENDENCY_CLASS.toField(b.toString()));
        }

        /**
         * @param referrer a referring class, as {@code pkg/Outer$Inner}
         * @param data its bytecode
         * @param depsMap map from referring outer classes (as {@code pkg/Outer}) to referred-to classes (as {@code pkg/Outer$Inner})
         * @param siblings other referring classes in the same artifact (including this one), as {@code pkg/Outer$Inner}
         * @param jar the jar file, for diagnostics
         */
        private static void addDependenciesToMap(String referrer, byte[] data, Map<String,Set<String>> depsMap, Set<String> siblings, File jar) throws IOException{
            ClassLoader jre = ClassLoader.getSystemClassLoader().getParent();
            int shell = referrer.indexOf('$', referrer.lastIndexOf('/') + 1);
            String referrerTopLevel = shell == -1 ? referrer : referrer.substring(0, shell);
            for (String referee : dependencies(data, referrer, jar)) {
                if (siblings.contains(referee)) {
                    continue; // in same JAR, not interesting
                }
                try {
                    jre.loadClass(referee.replace('/', '.')); // XXX ought to cache this result
                    continue; // in JRE, not interesting
                } catch (ClassNotFoundException x) {}
                Set<String> referees = depsMap.get(referrerTopLevel);
                if (referees == null) {
                    referees = new TreeSet<String>();
                    depsMap.put(referrerTopLevel, referees);
                }
                referees.add(referee);
            }
        }

        private static Map<String,byte[]> read(File jar) throws IOException {
            JarFile jf = new JarFile(jar);
            try {
                Map<String,byte[]> classfiles = new TreeMap<String,byte[]>();
                Enumeration<JarEntry> e = jf.entries();
                while (e.hasMoreElements()) {
                    JarEntry entry = e.nextElement();
                    String name = entry.getName();
                    if (!name.endsWith(".class")) {
                        continue;
                    }
                    String clazz = name.substring(0, name.length() - 6);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max((int) entry.getSize(), 0));
                    InputStream is = jf.getInputStream(entry);
                    try {
                        FileUtil.copy(is, baos);
                    } finally {
                        is.close();
                    }
                    classfiles.put(clazz, baos.toByteArray());
                }
                return classfiles;
            } finally {
                jf.close();
            }
        }

        // adapted from org.netbeans.nbbuild.VerifyClassLinkage
        private static Set<String> dependencies(byte[] data, String clazz, File jar) throws IOException {
            Set<String> result = new TreeSet<String>();
            DataInput input = new DataInputStream(new ByteArrayInputStream(data));
            skip(input, 8); // magic, minor_version, major_version
            int size = input.readUnsignedShort() - 1; // constantPoolCount
            String[] utf8Strings = new String[size];
            boolean[] isClassName = new boolean[size];
            boolean[] isDescriptor = new boolean[size];
            for (int i = 0; i < size; i++) {
                byte tag = input.readByte();
                switch (tag) {
                    case 1: // CONSTANT_Utf8
                        utf8Strings[i] = input.readUTF();
                        break;
                    case 7: // CONSTANT_Class
                        int index = input.readUnsignedShort() - 1;
                        if (index >= size) {
                            throw new IOException("@" + i + ": CONSTANT_Class_info.name_index " + index + " too big for size of pool " + size);
                        }
                        //LOGGER.finest("Class reference at " + index);
                        isClassName[index] = true;
                        break;
                    case 3: // CONSTANT_Integer
                    case 4: // CONSTANT_Float
                    case 9: // CONSTANT_Fieldref
                    case 10: // CONSTANT_Methodref
                    case 11: // CONSTANT_InterfaceMethodref
                        skip(input, 4);
                        break;
                    case 12: // CONSTANT_NameAndType
                        skip(input, 2);
                        index = input.readUnsignedShort() - 1;
                        if (index >= size || index < 0) {
                            throw new IOException("@" + i + ": CONSTANT_NameAndType_info.descriptor_index " + index + " too big for size of pool " + size);
                        }
                        isDescriptor[index] = true;
                        break;
                    case 8: // CONSTANT_String
                        skip(input, 2);
                        break;
                    case 5: // CONSTANT_Long
                    case 6: // CONSTANT_Double
                        skip(input, 8);
                        i++; // weirdness in spec
                        break;
                    default:
                        // E.g. com/ibm/icu/icu4j/2.6.1/icu4j-2.6.1.jar!/com/ibm/icu/impl/data/LocaleElements_zh__PINYIN.class is corrupt even acc. to javap.
                        LOGGER.log(Level.FINE, "jar:{4}!/{3}.class: Unrecognized constant pool tag {0} at index {1}; running UTF-8 strings: {2}", new Object[]{tag, i, Arrays.asList(utf8Strings), clazz, jar.toURI()});
                        continue;
                }
            }
            //LOGGER.finest("UTF-8 strings: " + Arrays.asList(utf8Strings));
            for (int i = 0; i < size; i++) {
                String s = utf8Strings[i];
                if (s != null) {
                    if (isClassName[i]) {
                        while (s.charAt(0) == '[') {
                            // array type
                            s = s.substring(1);
                        }
                        if (s.length() == 1) {
                            // primitive
                            continue;
                        }
                        String c;
                        if (s.charAt(s.length() - 1) == ';' && s.charAt(0) == 'L') {
                            // Uncommon but seems sometimes this happens.
                            c = s.substring(1, s.length() - 1);
                        } else {
                            c = s;
                        }
                        result.add(c);
                    } else if (isDescriptor[i]) {
                        int idx = 0;
                        while ((idx = s.indexOf('L', idx)) != -1) {
                            int semi = s.indexOf(';', idx);
                            if (semi == -1) {
                                throw new IOException("Invalid type or descriptor: " + s);
                            }
                            result.add(s.substring(idx + 1, semi));
                            idx = semi;
                        }
                    }
                }
            }
            return result;
        }
        private static void skip(DataInput input, int bytes) throws IOException {
            int skipped = input.skipBytes(bytes);
            if (skipped != bytes) {
                throw new IOException("Truncated class file");
            }
        }

        static final IndexerField FLD_NB_DEPENDENCY_CLASS = new IndexerField(new Field(
            null, "urn:NbClassDependenciesIndexCreator", NB_DEPENDENCY_CLASSES, "From a class's inside an artifact used class-dependencies"),
            IndexerFieldVersion.V3, NB_DEPENDENCY_CLASSES, "From a class's inside an artifact used class-dependencies", Store.YES, Index.ANALYZED);
        @Override public Collection<IndexerField> getIndexerFields() {
            return Arrays.asList(FLD_NB_DEPENDENCY_CLASS);
        }
    }
    
    private static class NbIndexCreator extends AbstractIndexCreator {

        private final List<ArtifactRepository> remoteRepos;
        NbIndexCreator() {
            remoteRepos = new ArrayList<ArtifactRepository>();
            for (RepositoryInfo info : RepositoryPreferences.getInstance().getRepositoryInfos()) {
                if (!info.isLocal()) {
                    remoteRepos.add(new MavenArtifactRepository(info.getId(), info.getRepositoryUrl(), new DefaultRepositoryLayout(), new ArtifactRepositoryPolicy(), new ArtifactRepositoryPolicy()));
                }
            }
        }

        private final Map<ArtifactInfo,List<Dependency>> dependenciesByArtifact = new WeakHashMap<ArtifactInfo,List<Dependency>>();

        public @Override void populateArtifactInfo(ArtifactContext context) throws IOException {
            ArtifactInfo ai = context.getArtifactInfo();
            if (ai.classifier != null) {
                //don't process items with classifier
                return;
            }
            try {
                MavenProject mp = load(ai);
                if (mp != null) {
                    List<Dependency> dependencies = mp.getDependencies();
                    LOGGER.log(Level.FINER, "Successfully loaded project model from repository for {0} with {1} dependencies", new Object[] {ai, dependencies.size()});
                    dependenciesByArtifact.put(ai, dependencies);
                }
            } catch (InvalidArtifactRTException ex) {
                ex.printStackTrace();
            }
        }

        public @Override void updateDocument(ArtifactInfo ai, Document doc) {
            List<Dependency> dependencies = dependenciesByArtifact.get(ai);
            if (dependencies != null) {
                for (Dependency d : dependencies) {
                    doc.add(FLD_NB_DEPENDENCY_GROUP.toField(d.getGroupId()));
                    doc.add(FLD_NB_DEPENDENCY_ARTIFACT.toField(d.getArtifactId()));
                    doc.add(FLD_NB_DEPENDENCY_VERSION.toField(d.getVersion()));
                }
            }
        }
        private static final String NS = "urn:NbIndexCreator"; // NOI18N
        private static IndexerField FLD_NB_DEPENDENCY_GROUP = new IndexerField(new Field(null, NS, NB_DEPENDENCY_GROUP, "Dependency group"),
                IndexerFieldVersion.V3, NB_DEPENDENCY_GROUP, "Dependency group", Store.NO, Index.NOT_ANALYZED);
        private static IndexerField FLD_NB_DEPENDENCY_ARTIFACT = new IndexerField(new Field(null, NS, NB_DEPENDENCY_ARTIFACT, "Dependency artifact"),
                IndexerFieldVersion.V3, NB_DEPENDENCY_ARTIFACT, "Dependency artifact", Store.NO, Index.NOT_ANALYZED);
        private static IndexerField FLD_NB_DEPENDENCY_VERSION = new IndexerField(new Field(null, NS, NB_DEPENDENCY_VERSION, "Dependency version"),
                IndexerFieldVersion.V3, NB_DEPENDENCY_VERSION, "Dependency version", Store.NO, Index.NOT_ANALYZED);
        @Override
        public Collection<IndexerField> getIndexerFields() {
            return Arrays.asList(FLD_NB_DEPENDENCY_GROUP, FLD_NB_DEPENDENCY_ARTIFACT, FLD_NB_DEPENDENCY_VERSION);
        }

        private MavenProject load(ArtifactInfo ai) {
            try {
                MavenEmbedder embedder = EmbedderFactory.getProjectEmbedder();
                Artifact projectArtifact = embedder.createArtifact(
                        ai.groupId,
                        ai.artifactId,
                        ai.version,
                        ai.packaging != null ? ai.packaging : "jar");
                DefaultProjectBuildingRequest dpbr = new DefaultProjectBuildingRequest();
                dpbr.setLocalRepository(embedder.getLocalRepository());
                dpbr.setRemoteRepositories(remoteRepos);
                dpbr.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
                dpbr.setSystemProperties(embedder.getSystemProperties());
                ProjectBuildingResult res = embedder.buildProject(projectArtifact, dpbr);
                if (res.getProject() != null) {
                    return res.getProject();
                } else {
                    LOGGER.log(Level.FINER, "No project model from repository for {0}: {1}", new Object[] {ai, res.getProblems()});
                }
            } catch (ProjectBuildingException ex) {
                LOGGER.log(Level.FINER, "Failed to load project model from repository for {0}: {1}", new Object[] {ai, ex});
            } catch (Exception exception) {
                LOGGER.log(Level.FINER, "Failed to load project model from repository for " + ai, exception);
            }
            return null;
        }

        public @Override boolean updateArtifactInfo(Document doc, ArtifactInfo ai) {
            return false;
        }
    }

}
