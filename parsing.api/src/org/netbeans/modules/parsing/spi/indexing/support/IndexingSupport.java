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
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.parsing.impl.indexing.IndexImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexFactoryImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexingSPIAccessor;
import org.netbeans.modules.parsing.impl.indexing.lucene.LuceneIndexFactory;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.util.Parameters;

/**
 * Support for writing indexers. Provides persistent storage
 * for indexers.
 * @author Tomas Zezula
 */
//@NotThreadSafe
public class IndexingSupport {

    private final IndexFactoryImpl spiFactory;
    private final IndexImpl spiIndex;
    private static final Map<String,IndexingSupport> instances = new HashMap<String,IndexingSupport>();

    private IndexingSupport (final Context ctx) throws IOException {
        this.spiFactory = new LuceneIndexFactory();
        this.spiIndex = this.spiFactory.createIndex(ctx);
    }

    /**
     * Returns an {@link IndexingSupport} for given indexing {@link Context}
     * @param context for which the support should be returned
     * @return the context
     * @throws java.io.IOException when underlying storage is corrupted or cannot
     * be created
     */
    public IndexingSupport getInstance (final Context context) throws IOException {
        Parameters.notNull("context", context);
        final String key = createkey(context);
        IndexingSupport support = instances.get(key);
        if (support == null) {
            support = new IndexingSupport(context);
            instances.put(key,support);
        }
        return support;
    }

    /**
     * Creates a new {@link IndexDocument}.
     * @return the decument
     */
    public IndexDocument createDocument () {
        return new IndexDocument(this.spiFactory.createDocument());
    }

    /**
     * Adds a new {@link IndexDocument} into the index
     * @param indexable from which the document was created
     * @param document to be added
     */
    public void addDocument (final Indexable indexable, final IndexDocument document) {
        Parameters.notNull("indexable", indexable);
        Parameters.notNull("document", document.spi);
        spiIndex.addDocument (indexable, document.spi);
    }

    /**
     * Removes all documents for given indexables
     * @param indexable to be removed
     */
    public void removeDocument (final Indexable indexable) {
        Parameters.notNull("indexable", indexable);
        spiIndex.removeDocument (indexable);
    }

    private String createkey (final Context ctx) {
        return ctx.getIndexFolder().getName() + IndexingSPIAccessor.getInstance().getIndexerName (ctx);
    }
   
}
