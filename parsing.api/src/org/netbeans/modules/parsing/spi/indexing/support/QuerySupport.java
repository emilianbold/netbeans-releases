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
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.IndexDocumentImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexFactoryImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexImpl;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.impl.indexing.lucene.LuceneIndexFactory;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class QuerySupport {

    /**
     * Encodes a type of the name kind used by
     * {@link Index#search}, {@link CodeCompletionContext#getNameKind()} etc.
     *
     */
    public enum Kind {
        /**
         * The name parameter
         * is an exact simple name of the package or declared type.
         */
        EXACT_NAME,
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
        CASE_INSENSITIVE_REGEXP;
    }



    private final IndexFactoryImpl spiFactory;
    private final Collection<IndexImpl> indexes;

    private QuerySupport (final String mimeType, final URL... roots) throws IOException {
        this.spiFactory = new LuceneIndexFactory();
        this.indexes = new LinkedList<IndexImpl>();
        final String indexerFolder = findIndexerFolder(mimeType);
        if (indexerFolder != null) {
            for (URL root : roots) {
                final FileObject cacheFolder = CacheFolder.getDataFolder(root);
                assert cacheFolder != null;
                final FileObject indexFolder = cacheFolder.getFileObject(indexerFolder);
                if (indexFolder != null) {
                    final IndexImpl index = this.spiFactory.getIndex(indexFolder);
                    if (index != null) {
                        this.indexes.add(index);
                    }
                }
            }
        }
    }    

    
    public Collection<? extends IndexDocument> query (final String fieldName, final String fieldValue,
            final Kind kind, final String... fieldsToLoad) {
        final List<IndexDocument> result = new LinkedList<IndexDocument>();
        for (IndexImpl index : indexes) {
            final Collection<? extends IndexDocumentImpl> pr = index.query(fieldName, fieldValue, kind, fieldsToLoad);
            for (IndexDocumentImpl di : pr) {
                result.add(new IndexDocument(di));
            }
        }
        return result;
    }

    public static QuerySupport forRoots (final String mimeType, final URL... roots) throws IOException {
        Parameters.notNull("mimeType", mimeType);
        Parameters.notNull("roots", roots);
        return new QuerySupport(mimeType, roots);
    }

    public static QuerySupport forRoots (final String mimeType, final FileObject... roots) throws IOException {
        Parameters.notNull("mimeType", mimeType);
        Parameters.notNull("roots", roots);
        final List<URL> rootsURL = new ArrayList<URL>(roots.length);
        for (FileObject root : roots) {
            rootsURL.add(root.getURL());
        }
        return new QuerySupport(mimeType, rootsURL.toArray(new URL[rootsURL.size()]));
    }

    private static String findIndexerFolder (final String mimeType) {
        final EmbeddingIndexerFactory embeddingFactory = MimeLookup.getLookup(mimeType).lookup(EmbeddingIndexerFactory.class);
        if (embeddingFactory != null) {
            return SPIAccessor.getInstance().getIndexerPath(embeddingFactory.getIndexerName(), embeddingFactory.getIndexVersion());
        }
        final CustomIndexerFactory customFactory = MimeLookup.getLookup(mimeType).lookup(CustomIndexerFactory.class);
        if (customFactory != null) {
            return SPIAccessor.getInstance().getIndexerPath(customFactory.getIndexerName(), embeddingFactory.getIndexVersion());
        }
        return null;
    }

}
