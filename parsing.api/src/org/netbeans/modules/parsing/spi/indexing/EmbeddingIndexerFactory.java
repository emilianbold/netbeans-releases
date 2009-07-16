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

import java.util.Collection;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Tomas Zezula
 */
public abstract class EmbeddingIndexerFactory {

    /**
     * Creates  new {@link Indexer}.
     * @param indexing for which the indexer should be created
     * @param snapshot for which the indexer should be created
     * @return an indexer
     */
    public abstract EmbeddingIndexer createIndexer (final Indexable indexable, final Snapshot snapshot);


    /**
     * Called by indexing infrastructure to allow indexer to clean indexes for deleted files.
     * @param deleted the collection of deleted {@link Indexable}s
     * @param contents an indexing context
     * @since 1.18
     */
    public abstract void filesDeleted (Iterable<? extends Indexable> deleted, Context context);
    
    /**
     * Called by indexing infrastructure to notify indexer that a file was modified and so its
     * index may contain stale data.
     *
     * @param dirty the collection of dirty {@link Indexable}s
     * @param context an indexing context
     * @since 1.18
     */
    public abstract void filesDirty (Iterable<? extends Indexable> dirty, Context context);

    /**
     * Return the name of this indexer. This name should be unique because the infrastructure 
     * will use this name to produce a separate data directory for each indexer
     * where it has its own storage.
     *
     * @return The indexer name. This does not need to be localized since it is
     * never shown to the user, but should contain filesystem safe characters.
     */
    public abstract String getIndexerName ();


    /**
     * Return the version stamp of the schema that is currently being stored
     * by this indexer. Along with the index name this string will be used to
     * create a unique data directory for the database.
     *
     * Whenever you incompatibly change what is stored by the indexer,
     * update the version stamp.
     *
     * @return The version stamp of the current index.
     */
    public abstract int getIndexVersion ();

}
