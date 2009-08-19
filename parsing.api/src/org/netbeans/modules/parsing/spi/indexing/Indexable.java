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

package org.netbeans.modules.parsing.spi.indexing;

import java.io.IOException;
import java.net.URL;
import org.netbeans.modules.parsing.impl.indexing.CancelRequest;
import org.netbeans.modules.parsing.impl.indexing.IndexFactoryImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexableImpl;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;


/**
 * Represens a file to be procesed by an indexer.
 * @author Tomas Zezula
 */
//@NotThreadSafe
public final class Indexable {

    static {
        SPIAccessor.setInstance(new MyAccessor());
    }

    private IndexableImpl delegate;

    Indexable(final IndexableImpl delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    /**
     * Returns a relative path from root to the
     * represented file.
     * @return the relative path from root
     */
    public String getRelativePath () {
        return delegate.getRelativePath();
    }

//    /**
//     * Returns a name of represented file.
//     * @return a name
//     */
//    public String getName () {
//        return this.delegate.getName();
//    }

    /**
     * Returns absolute URL of the represented file
     * @return uri
     */
    public URL getURL () {
        return delegate.getURL();
    }

    /**
     * @return
     * @since 1.13
     */
    public String getMimeType() {
        return delegate.getMimeType();
    }
    
//    /**
//     * Returns a time when the file was last modified
//     * @return A long value representing the time the file was last modified,
//     * measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970),
//     * or 0L if the file does not exist or if an I/O error occurs
//     */
//    public long getLastModified () {
//        return this.delegate.getLastModified();
//    }

//    /**
//     * Returns {@link InputStream} of represented file.
//     * The caller is responsible to correctly close the stream.
//     * @return the {@link InputStream} to read the content
//     * @throws java.io.IOException
//     */
//    public InputStream openInputStream () throws IOException {
//        return this.delegate.openInputStream();
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Indexable other = (Indexable) obj;
        return delegate.equals(other.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    private static class MyAccessor extends SPIAccessor {

        @Override
        public Indexable create(IndexableImpl delegate) {
            return new Indexable(delegate);
        }

        @Override
        public void index(BinaryIndexer indexer, Context context) {
            assert indexer != null;
            assert context != null;
            indexer.index(context);
        }

        @Override
        public void index(CustomIndexer indexer, Iterable<? extends Indexable> files, Context context) {
            assert indexer != null;
            assert files != null;
            assert context != null;
            indexer.index(files, context);
        }

        @Override
        public Context createContext(FileObject indexFolder, URL rootURL, 
                String indexerName, int indexerVersion, IndexFactoryImpl factory,
                boolean followUpJob, boolean checkForEditorModifications,
                boolean sourceForBinaryRoot, CancelRequest cancelRequest) throws IOException {
            return new Context(indexFolder, rootURL, indexerName, indexerVersion, factory, followUpJob, checkForEditorModifications, sourceForBinaryRoot, cancelRequest);
        }

        @Override
        public String getIndexerName(Context ctx) {
            assert ctx != null;
            return ctx.getIndexerName();
        }

        @Override
        public int getIndexerVersion(Context ctx) {
            assert ctx != null;
            return ctx.getIndexerVersion();
        }

        @Override
        public void index(EmbeddingIndexer indexer, Indexable indexable, Result parserResult, Context ctx) {
            assert indexer != null;
            assert indexable != null;
            assert parserResult != null;
            assert ctx != null;
            indexer.index(indexable, parserResult, ctx);
        }

        @Override
        public String getIndexerPath(final String indexerName, final int indexerVersion) {
            assert indexerName != null;
            return Context.getIndexerPath(indexerName, indexerVersion);
        }

        @Override
        public IndexFactoryImpl getIndexFactory(Context ctx) {
            assert ctx != null;
            return ctx.factory;
        }

        @Override
        public void context_attachIndexingSupport(Context context, IndexingSupport support) {
            context.attachIndexingSupport(support);
        }

        @Override
        public IndexingSupport context_getAttachedIndexingSupport(Context context) {
            return context.getAttachedIndexingSupport();
        }

        @Override
        public void setAllFilesJob(final Context context, final boolean allFilesJob) {
            context.setAllFilesJob(allFilesJob);
        }
    }

}
