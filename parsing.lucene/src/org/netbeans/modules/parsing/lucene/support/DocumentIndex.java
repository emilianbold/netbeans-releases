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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.lucene.support;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Document based index provides a higher level api than {@link Index}
 * It's document oriented. It supports adding, removing and searching of {@link IndexDocument}
 * @since 1.1
 * @author Tomas Zezula
 */
public interface DocumentIndex {
    
    /**
     * Adds a document into the index.
     * The document may not be added persistently until {@link DocumentIndex#store(boolean)} is called
     * @param document to be added
     */
    void addDocument (@NonNull IndexDocument document);        

    /**
     * Removes a document associated with given primary key from the index
     * @param primaryKey the primary key of the document which should be removed
     */
    void removeDocument (@NonNull String primaryKey);

    /**
     * Checks the validity of the index, see {@link Index#isValid(boolean)} for details
     * @return true if index exists and is not broken
     * @throws IOException in case of IO error
     */
    public boolean isValid() throws IOException;
    
    /**
     * Closes the index.
     * @throws IOException in case of IO error
     */
    public void close() throws IOException;

    /**
     * Stores changes done on the index.
     * @param optimize if true Lucene optimizes the index. The optimized index is
     * faster but the optimization takes some time. In general small updates should
     * not be optimized.
     * @throws IOException in case of IO error
     */
    public void store (boolean optimize) throws IOException;

    /**
     * Performs a search on the index.
     * @param fieldName the name of the field to be searched
     * @param value of the field to be searched
     * @param kind of the query, see {@link Queries.QueryKind} for details
     * @param fieldsToLoad names of the field which should be loaded into the document.
     * Loading only needed fields speeds up the search. If null or empty all fields are loaded.
     * @return The collection of found documents
     * @throws IOException in case of IO error
     * @throws InterruptedException  when the search was interrupted
     */
    public @NonNull Collection<? extends IndexDocument> query (
            @NonNull String fieldName,
            @NonNull String value,
            @NonNull Queries.QueryKind kind,
            @NullAllowed String... fieldsToLoad) throws IOException, InterruptedException;
    
    
    /**
     * Performs a search on the index using primary key
     * @param value of the primary key
     * @param kind of the query, see {@link Queries.QueryKind} for details
     * @param fieldsToLoad names of the field which should be loaded into the document.
     * Loading only needed fields speeds up the search. If null or empty all fields are loaded.
     * @return The collection of found documents
     * @throws IOException in case of IO error
     * @throws InterruptedException  when the search was interrupted
     */
    public @NonNull Collection<? extends IndexDocument> findByPrimaryKey (
            @NonNull String primaryKeyValue,
            @NonNull Queries.QueryKind kind,
            @NullAllowed String... fieldsToLoad) throws IOException, InterruptedException;

    /**
     * Marks the primaryKey as dirty. Can be used by client to detect non up to date documents.
     * The dirty keys are cleaned during the save.
     * @param primaryKey of document which should be marked as dirty
     */
    public void markKeyDirty(@NonNull String primaryKey);
    
    /**
     * Cleans the dirty flag for Documents represented by given primary keys.
     * See {@link DocumentIndex#markKeyDirty}
     * @param dirtyKeys the primary keys to be un marked as dirty
     */
    public void removeDirtyKeys(@NonNull Collection<? extends String> dirtyKeys);

    /**
     * Returns the primary keys of dirty documents.
     * See {@link DocumentIndex#markKeyDirty}
     * @return the primary keys of dirty documents, never returns null
     */
    public @NonNull Collection<? extends String> getDirtyKeys();

}
