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

import java.net.MalformedURLException;
import java.net.URI;
import org.netbeans.modules.parsing.impl.indexing.IndexingSPIAccessor;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * Represents a context of indexing given root.
 * @author Tomas Zezula
 */
//@NotThreadSafe
public final class Context {

    private final URI rootURI;
    private final FileObject indexFolder;
    private final String indexerName;
    private final int indexerVersion;
    private FileObject root;

    static {
        IndexingSPIAccessor.setInstance(new MyAccessor());
    }

    Context (final FileObject indexFolder,
             final URI rootURI, String indexerName, int indexerVersion) {
        assert indexFolder != null;
        assert rootURI != null;
        assert indexerName != null;
        this.indexFolder = indexFolder;
        this.rootURI = rootURI;
        this.indexerName = indexerName;
        this.indexerVersion = indexerVersion;
    }

    /**
     * Returns the cache folder where the indexer may store language metadata.
     * For each root and indexer there exist a separate cache folder.
     * @return The cahce folder
     */
    public FileObject getIndexFolder () {
        return this.indexFolder;
    }

    /**
     * Return the {@link URI} of the processed root
     * @return the absolute URI
     */
    public URI getRootURI () {
        return this.rootURI;
    }

    /**
     * Return the processed root, may return null
     * when the processed root was deleted.
     * The {@link Context#getRootURI()} can be used in
     * this case.
     * @return the root or null when the root doesn't exist
     */
    public FileObject getRoot () {
        if (root == null) {
            try {
                root = URLMapper.findFileObject(this.rootURI.toURL());
            } catch (MalformedURLException e) {
                //Never thrown
                Exceptions.printStackTrace(e);
                return null;
            }
        }
        return root;
    }



    private static class MyAccessor extends IndexingSPIAccessor {

        @Override
        public void index(EmbeddingIndexer indexer, Result parserResult, Context ctx) {
            indexer.index(parserResult, ctx);
        }

        @Override
        public String getIndexerName(final Context ctx) {
            return ctx.indexerName;
        }

        @Override
        public int getIndexerVersion(final Context ctx) {
            return ctx.indexerVersion;
        }
    }
}
