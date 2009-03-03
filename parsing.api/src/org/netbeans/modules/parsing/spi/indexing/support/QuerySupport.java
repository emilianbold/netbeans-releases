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
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.spi.indexing.support;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.IndexDocumentImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexFactoryImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexImpl;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.impl.indexing.lucene.LuceneIndexFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class QuerySupport {

    private static final Logger LOG = Logger.getLogger(QuerySupport.class.getName());
    
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


    private final String indexerIdentification;
    private final IndexFactoryImpl spiFactory;
    private final Map<URL,IndexImpl> indexes;

    private QuerySupport (final String indexerName, int indexerVersion, final URL... roots) throws IOException {
        this.indexerIdentification = indexerName + "/" + indexerVersion; //NOI18N
        this.spiFactory = new LuceneIndexFactory();
        this.indexes = new HashMap<URL, IndexImpl>();
        final String indexerFolder = findIndexerFolder(indexerName, indexerVersion);
        if (indexerFolder != null) {
            for (URL root : roots) {
                final FileObject cacheFolder = CacheFolder.getDataFolder(root);
                assert cacheFolder != null;
                final FileObject indexFolder = cacheFolder.getFileObject(indexerFolder);
                if (indexFolder != null) {
                    final IndexImpl index = this.spiFactory.getIndex(indexFolder);
                    if (index != null) {
                        this.indexes.put(root,index);
                    }
                }
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("QuerySupport for " + indexerIdentification + ":"); //NOI18N
            for(URL root : indexes.keySet()) {
                LOG.fine(" " + root + " -> index: " + indexes.get(root)); //NOI18N
            }
            LOG.fine("----"); //NOI18N
        }
    }

    /**
     * Unit test constructor
     */
    private QuerySupport (final FileObject srcRoot, final String indexerName, final int indexerVersion) throws IOException {
        this.indexerIdentification = indexerName + "/" + indexerVersion; //NOI18N
        this.spiFactory = new LuceneIndexFactory();
        this.indexes = new HashMap<URL, IndexImpl>();
        final FileObject cacheFolder = CacheFolder.getDataFolder(srcRoot.getURL());
        FileObject fo = cacheFolder.getFileObject(SPIAccessor.getInstance().getIndexerPath(indexerName, indexerVersion));
        fo.getClass();
        this.indexes.put (srcRoot.getURL(),this.spiFactory.getIndex(fo));
    }

    public Collection<? extends IndexResult> query(
            final String fieldName,
            final String fieldValue,
            final Kind kind, 
            final String... fieldsToLoad
    ) throws IOException {
        // check if there are stale indicies
        for (Map.Entry<URL, IndexImpl> ie : indexes.entrySet()) {
            final IndexImpl index = ie.getValue();
            final Collection<? extends String> staleFiles = index.getStaleFiles();

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Index: " + index + ", staleFiles: " + staleFiles); //NOI18N
            }
            
            if (staleFiles != null && staleFiles.size() > 0) {
                final URL root = ie.getKey();
                LinkedList<URL> list = new LinkedList<URL>();
                for(String staleFile : staleFiles) {
                    list.add(new URL(root, staleFile));
                }

                IndexingManager.getDefault().refreshIndexAndWait(root, list);
            }
        }

        final List<IndexResult> result = new LinkedList<IndexResult>();
        for (Map.Entry<URL,IndexImpl> ie : indexes.entrySet()) {
            final IndexImpl index = ie.getValue();
            final URL root = ie.getKey();
            final Collection<? extends IndexDocumentImpl> pr = index.query(fieldName, fieldValue, kind, fieldsToLoad);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("query(" + fieldName + ", " + fieldValue + ", " + kind + ", ...) for " + indexerIdentification + ":"); //NOI18N
                for(IndexDocumentImpl idi : pr) {
                    LOG.fine(" " + idi); //NOI18N
                }
                LOG.fine("----"); //NOI18N
            }
            for (IndexDocumentImpl di : pr) {                
                result.add(new IndexResult(di,root));
            }
        }
        return result;
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

    private static String findIndexerFolder (final String indexerName, final int indexerVersion) {
        return SPIAccessor.getInstance().getIndexerPath(indexerName, indexerVersion);
    }

}
