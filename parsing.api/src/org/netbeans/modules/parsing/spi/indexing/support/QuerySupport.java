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

package org.netbeans.modules.parsing.spi.indexing.support;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.impl.RunWhenScanFinishedSupport;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.IndexFactoryImpl;
import org.netbeans.modules.parsing.impl.indexing.Pair;
import org.netbeans.modules.parsing.impl.indexing.PathRecognizerRegistry;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.impl.indexing.TransientUpdateSupport;
import org.netbeans.modules.parsing.impl.indexing.URLCache;
import org.netbeans.modules.parsing.impl.indexing.Util;
import org.netbeans.modules.parsing.impl.indexing.lucene.LayeredDocumentIndex;
import org.netbeans.modules.parsing.impl.indexing.lucene.LuceneIndexFactory;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class QuerySupport {

    /**
     * Gets classpath roots relevant for a file. This method tries to find
     * classpath roots for a given files. It looks at classpaths specified by
     * <code>sourcePathIds</code>, <code>libraryPathIds</code> and
     * <code>binaryLibraryPathIds</code> parameters.
     *
     * <p>The roots collected from <code>binaryLibraryPathIds</code> will be translated
     * by the <code>SourceForBinaryQuery</code> in order to find relevant sources root.
     * The roots collected from <code>libraryPathIds</code> are expected to be
     * libraries in their sources form (ie. no translation).
     *
     * @param f The file to find roots for.
     * @param sourcePathIds The IDs of source classpath to look at.
     * @param libraryPathIds The IDs of library classpath to look at.
     * @param binaryLibraryPathIds The IDs of binary library classpath to look at.
     *
     * @return The collection of roots for a given file. It may be empty, but never <code>null</code>.
     * 
     * @since 1.6
     */
    public static Collection<FileObject> findRoots(
            FileObject f,
            Collection<String> sourcePathIds,
            Collection<String> libraryPathIds,
            Collection<String> binaryLibraryPathIds)
    {
        Collection<FileObject> roots = new HashSet<FileObject>();

        if (sourcePathIds == null) {
            sourcePathIds = PathRecognizerRegistry.getDefault().getSourceIds();
        }

        if (libraryPathIds == null) {
            libraryPathIds = PathRecognizerRegistry.getDefault().getLibraryIds();
        }

        if (binaryLibraryPathIds == null) {
            binaryLibraryPathIds = PathRecognizerRegistry.getDefault().getBinaryLibraryIds();
        }

        collectClasspathRoots(f, sourcePathIds, false, roots);
        collectClasspathRoots(f, libraryPathIds, false, roots);
        collectClasspathRoots(f, binaryLibraryPathIds, true, roots);

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Roots for file " + f //NOI18N
                    + ", sourcePathIds=" + sourcePathIds //NOI18N
                    + ", libraryPathIds=" + libraryPathIds //NOI18N
                    + ", binaryPathIds=" + binaryLibraryPathIds //NOI18N
                    + ": "); //NOI18N
            for(FileObject root : roots) {
                try {
                    LOG.fine("  " + root.getURL()); //NOI18N
                } catch (FileStateInvalidException ex) {
                    //ignore
                }
            }
            LOG.fine("----"); //NOI18N
        }

        return roots != null ? roots : Collections.<FileObject>emptySet();
    }

    /**
     * Gets classpath roots relevant for a project. This method tries to find
     * classpaths with <code>sourcePathIds</code>, <code>libraryPathIds</code> and
     * <code>binaryPathIds</code> supplied by the <code>project</code>.
     *
     * <p>The roots collected from <code>binaryLibraryPathIds</code> will be translated
     * by the <code>SourceForBinaryQuery</code> in order to find relevant sources root.
     * The roots collected from <code>libraryPathIds</code> are expected to be
     * libraries in their sources form (ie. no translation).
     *
     * @param project The project to find the roots for. Can be <code>null</code> in
     *   which case the method searches in all registered classpaths.
     * @param sourcePathIds The IDs of source classpath to look at.
     * @param libraryPathIds The IDs of library classpath to look at.
     * @param binaryLibraryPathIds The IDs of binary library classpath to look at.
     *
     * @return The collection of roots for a given project. It may be empty, but never <code>null</code>.
     * 
     * @since 1.6
     */
    public static Collection<FileObject> findRoots(
            Project project,
            Collection<String> sourcePathIds,
            Collection<String> libraryPathIds,
            Collection<String> binaryLibraryPathIds)
    {
        Set<FileObject> roots = new HashSet<FileObject>();

        if (sourcePathIds == null) {
            sourcePathIds = PathRecognizerRegistry.getDefault().getSourceIds();
        }

        if (libraryPathIds == null) {
            libraryPathIds = PathRecognizerRegistry.getDefault().getLibraryIds();
        }

        if (binaryLibraryPathIds == null) {
            binaryLibraryPathIds = PathRecognizerRegistry.getDefault().getBinaryLibraryIds();
        }

        collectClasspathRoots(null, sourcePathIds, false, roots);
        collectClasspathRoots(null, libraryPathIds, false, roots);
        collectClasspathRoots(null, binaryLibraryPathIds, true, roots);

        if (project != null) {
            Set<FileObject> rootsInProject = new HashSet<FileObject>();
            for(FileObject root : roots) {
                if (FileOwnerQuery.getOwner(root) == project) {
                    rootsInProject.add(root);
                }
            }
            roots = rootsInProject;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Roots for project " + project //NOI18N
                    + ", sourcePathIds=" + sourcePathIds //NOI18N
                    + ", libraryPathIds=" + libraryPathIds //NOI18N
                    + ", binaryPathIds=" + binaryLibraryPathIds //NOI18N
                    + ": "); //NOI18N
            for(FileObject root : roots) {
                try {
                    LOG.fine("  " + root.getURL()); //NOI18N
                } catch (FileStateInvalidException ex) {
                    //ignore
                }
            }
            LOG.fine("----"); //NOI18N
        }

        return roots;
    }

    public static QuerySupport forRoots (final String indexerName, final int indexerVersion, final URL... roots) throws IOException {
        Parameters.notNull("indexerName", indexerName); //NOI18N
        Parameters.notNull("roots", roots); //NOI18N
        return new QuerySupport(indexerName, indexerVersion, roots);
    }

    public static QuerySupport forRoots (final String indexerName, final int indexerVersion, final FileObject... roots) throws IOException {
        Parameters.notNull("indexerName", indexerName); //NOI18N
        Parameters.notNull("roots", roots); //NOI18N
        final List<URL> rootsURL = new ArrayList<URL>(roots.length);
        for (FileObject root : roots) {
            rootsURL.add(root.getURL());
        }
        return new QuerySupport(indexerName, indexerVersion, rootsURL.toArray(new URL[rootsURL.size()]));
    }

    public Collection<? extends IndexResult> query(
            final String fieldName,
            final String fieldValue,
            final Kind kind,
            final String... fieldsToLoad
    ) throws IOException {
        Parameters.notNull("fieldName", fieldName); //NOI18N
        Parameters.notNull("fieldValue", fieldValue); //NOI18N
        Parameters.notNull("kind", kind); //NOI18N
        try {
            return Utilities.runPriorityIO(new Callable<Collection<? extends IndexResult>>() {

                @Override
                public Collection<? extends IndexResult> call() throws Exception {
                    Iterable<? extends Pair<URL, LayeredDocumentIndex>> indices = indexerQuery.getIndices(roots);
                    // check if there are stale indices
                    for (Pair<URL, LayeredDocumentIndex> pair : indices) {
                        final LayeredDocumentIndex index = pair.second;
                        final Collection<? extends String> staleFiles = index.getDirtyKeys();
                        final boolean scanningThread = RunWhenScanFinishedSupport.isScanningThread();
                        LOG.log(
                            Level.FINE,
                            "Index: {0}, staleFiles: {1}, scanning thread: {2}",  //NOI18N
                            new Object[]{
                                index,
                                staleFiles,
                                scanningThread
                            });
                        if (!staleFiles.isEmpty() && !scanningThread) {
                            final URL root = pair.first;
                            LinkedList<URL> list = new LinkedList<URL>();
                            for (String staleFile : staleFiles) {
                                try {
                                    list.add(Util.resolveUrl(root, staleFile, false));
                                } catch (MalformedURLException ex) {
                                    LOG.log(Level.WARNING, null, ex);
                                }
                            }
                            TransientUpdateSupport.setTransientUpdate(true);
                            try {
                                RepositoryUpdater.getDefault().enforcedFileListUpdate(root,list);
                            } finally {
                                TransientUpdateSupport.setTransientUpdate(false);
                            }
                        }
                    }
                    final List<IndexResult> result = new LinkedList<IndexResult>();
                    for (Pair<URL, LayeredDocumentIndex> pair : indices) {
                        final DocumentIndex index = pair.second;
                        final URL root = pair.first;
                        final Collection<? extends org.netbeans.modules.parsing.lucene.support.IndexDocument> pr = index.query(
                                fieldName,
                                fieldValue,
                                translateQueryKind(kind),
                                fieldsToLoad);
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("query(\"" + fieldName + "\", \"" + fieldValue + "\", " + kind + ", " + printFiledToLoad(fieldsToLoad) + ") invoked at " + getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(this)) + "[indexer=" + indexerQuery.getIndexerId() + "]:"); //NOI18N
                            for (org.netbeans.modules.parsing.lucene.support.IndexDocument idi : pr) {
                                LOG.fine(" " + idi); //NOI18N
                            }
                            LOG.fine("----"); //NOI18N
                        }
                        for (org.netbeans.modules.parsing.lucene.support.IndexDocument di : pr) {
                            result.add(new IndexResult(di, root));
                        }
                    }
                    return result;
                }
            });
        } catch (IOException ioe) {
            throw ioe;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Encodes a type of the name kind used by {@link QuerySupport#query}.
     *
     */
    public enum Kind {
        /**
         * The name parameter
         * is an exact simple name of the package or declared type.
         */
        EXACT,
        /**
         * The name parameter
         * is an case sensitive prefix of the package or declared type name.
         */
        PREFIX,
        /**
         * The name parameter is
         * an case insensitive prefix of the declared type name.
         */
        CASE_INSENSITIVE_PREFIX,
        /**
         * The name parameter is
         * an camel case of the declared type name.
         */
        CAMEL_CASE,
        /**
         * The name parameter is
         * an regular expression of the declared type name.
         */
        REGEXP,
        /**
         * The name parameter is
         * an case insensitive regular expression of the declared type name.
         */
        CASE_INSENSITIVE_REGEXP,

        CASE_INSENSITIVE_CAMEL_CASE;
    }

    // ------------------------------------------------------------------------
    // Private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(QuerySupport.class.getName());

    private final IndexerQuery indexerQuery;
    private final List<URL> roots;

    private QuerySupport (final String indexerName, int indexerVersion, final URL... roots) throws IOException {
        this.indexerQuery = IndexerQuery.forIndexer(indexerName, indexerVersion);
        this.roots = new LinkedList<URL>(Arrays.asList(roots));

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(this)) //NOI18N
                    + "[indexer=" + indexerQuery.getIndexerId() + "]:"); //NOI18N
            for(Pair<URL, LayeredDocumentIndex> pair : indexerQuery.getIndices(this.roots)) {
                LOG.fine(" " + pair.first + " -> index: " + pair.second); //NOI18N
            }
            LOG.fine("----"); //NOI18N
        }
    }

    private static void collectClasspathRoots(FileObject file, Collection<String> pathIds, boolean binaryPaths, Collection<FileObject> roots) {
        for(String id : pathIds) {
            Collection<FileObject> classpathRoots = getClasspathRoots(file, id);
            if (binaryPaths) {
                // Filter out roots that do not have source files available
                for(FileObject binRoot : classpathRoots) {
                    URL binRootUrl;
                    try {
                        binRootUrl = binRoot.getURL();
                    } catch (FileStateInvalidException fsie) {
                        continue;
                    }

                    URL[] srcRoots = PathRegistry.getDefault().sourceForBinaryQuery(binRootUrl, null, false);
                    if (srcRoots != null) {
                        LOG.log(Level.FINE, "Translating {0} -> {1}", new Object [] { binRootUrl, srcRoots }); //NOI18N
                        for(URL srcRootUrl : srcRoots) {
                            FileObject srcRoot = URLCache.getInstance().findFileObject(srcRootUrl);
                            if (srcRoot != null) {
                                roots.add(srcRoot);
                            }
                        }
                    } else {
                        LOG.log(Level.FINE, "No sources for {0}, adding bin root", binRootUrl); //NOI18N
                        roots.add(binRoot);
                    }
                }
            } else {
                roots.addAll(classpathRoots);
            }
        }
    }

    private static Collection<FileObject> getClasspathRoots(FileObject file, String classpathId) {
        Collection<FileObject> roots = Collections.<FileObject>emptySet();

        if (file != null) {
            ClassPath classpath = ClassPath.getClassPath(file, classpathId);
            if (classpath != null) {
                roots = Arrays.asList(classpath.getRoots());
            }
        } else {
            roots = new HashSet<FileObject>();
            Set<URL> urls = PathRegistry.getDefault().getRootsMarkedAs(classpathId);
            for(URL url : urls) {
                FileObject f = URLCache.getInstance().findFileObject(url);
                if (f != null) {
                    roots.add(f);
                }
            }
        }

        return roots;
    }

    private static String printFiledToLoad(String... fieldsToLoad) {
        if (fieldsToLoad == null || fieldsToLoad.length == 0) {
            return "<all-fields>"; //NOI18N
        } else {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < fieldsToLoad.length; i++) {
                sb.append("\"").append(fieldsToLoad[i]).append("\""); //NOI18N
                if (i + 1 < fieldsToLoad.length) {
                    sb.append(", "); //NOI18N
                }
            }
            return sb.toString();
        }
    }
    
    private static Queries.QueryKind translateQueryKind(final QuerySupport.Kind kind) {
        switch (kind) {
            case EXACT: return Queries.QueryKind.EXACT;
            case PREFIX: return Queries.QueryKind.PREFIX;                
            case CASE_INSENSITIVE_PREFIX: return Queries.QueryKind.CASE_INSENSITIVE_PREFIX;
            case CAMEL_CASE: return Queries.QueryKind.CAMEL_CASE;
            case CASE_INSENSITIVE_REGEXP: return Queries.QueryKind.CASE_INSENSITIVE_REGEXP;                
            case REGEXP: return Queries.QueryKind.REGEXP;
            case CASE_INSENSITIVE_CAMEL_CASE: return Queries.QueryKind.CASE_INSENSITIVE_CAMEL_CASE;
            default: throw new UnsupportedOperationException (kind.toString());
        }
    } 

    /* test */ static final class IndexerQuery {

        public static synchronized IndexerQuery forIndexer(String indexerName, int indexerVersion) {
            String indexerId = SPIAccessor.getInstance().getIndexerPath(indexerName, indexerVersion);
            IndexerQuery q = queries.get(indexerId);
            if (q == null) {
                q = new IndexerQuery(indexerId);
                queries.put(indexerId, q);
            }
            return q;
        }

        @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS"
        /*,justification="URLs have never host part"*/)
        public Iterable<? extends Pair<URL, LayeredDocumentIndex>> getIndices(List<? extends URL> roots) {
            synchronized (root2index) {
                List<Pair<URL, LayeredDocumentIndex>> indices = new LinkedList<Pair<URL, LayeredDocumentIndex>>();

                for(URL r : roots) {
                    assert PathRegistry.noHostPart(r) : r;
                    Reference<LayeredDocumentIndex> indexRef = root2index.get(r);
                    LayeredDocumentIndex index = indexRef != null ? indexRef.get() : null;
                    if (index == null) {
                        index = findIndex(r);
                        if (index != null) {
                            root2index.put(r, new SoftReference<LayeredDocumentIndex>(index));
                        } else {
                            root2index.remove(r);
                        }
                    }
                    if (index != null) {
                        indices.add(Pair.of(r, index));
                    }
                }

                return indices;
            }
        }

        public String getIndexerId() {
            return indexerId;
        }

        // ------------------------------------------------------------------------
        // Private implementation
        // ------------------------------------------------------------------------

        private static final Map<String, IndexerQuery> queries = new HashMap<String, IndexerQuery>();
        /* test */ static /* final, but tests need to change it */ IndexFactoryImpl indexFactory = LuceneIndexFactory.getDefault();

        private final String indexerId;
        @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS"
        /*,justification="URLs have never host part"*/)
        private final Map<URL, Reference<LayeredDocumentIndex>> root2index = new HashMap<URL, Reference<LayeredDocumentIndex>>();

        private IndexerQuery(String indexerId) {
            this.indexerId = indexerId;
        }

        private LayeredDocumentIndex findIndex(URL root) {
            try {
                FileObject cacheFolder = CacheFolder.getDataFolder(root);
                assert cacheFolder != null;
                FileObject indexFolder = cacheFolder.getFileObject(indexerId);
                if (indexFolder != null) {
                    return indexFactory.getIndex(indexFolder);
                }
            } catch (IOException ioe) {
                LOG.log(Level.INFO, "Can't create index for " + indexerId + " and " + root, ioe); //NOI18N
            }
            return null;
        }        
    } // End of IndexerQuery class
}
