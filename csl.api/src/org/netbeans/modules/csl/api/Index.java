/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.csl.api;

import java.io.IOException;
import java.util.Set;
import org.netbeans.modules.csl.api.annotations.NonNull;

/**
 * This class represents an index that is provided by the IDE to a language
 * plugin for storage and retrieval. Language plugins should not subclass this class.
 * 
 * @author Tor Norbye
 */
public abstract class Index {
    
    public Index() {
    }

    /**
     * Scope used by {@link #search} to search in
     */
    public enum SearchScope {
        /**
         * Search is done in source path
         */
        SOURCE,
        /**
         * Search is done in compile and boot path
         */
        DEPENDENCIES
    };

    /**
     * Result corresponding to a "document" in the
     * index. Each document contains name,value pairs.
     * Some pairs are unique (a single key and value)
     * whereas others have many values for a single key.
     * Call getValues() on the latter.
     * 
     * @todo There's some asymmetry between gsfStore and
     *   gsfSearch now, in that the store operation
     *   takes a set of maps, whereas the result is a
     *   set of SearchResults (which are map-like). Originally
     *   I returned a set of maps, but the maps aren't 
     *   really maps since they wrap Lucene documents,
     *   which don't support all the map operations.
     *   Possibly I could offer some kind of SearchDocument
     *   interface here to be passed in instead for better
     *   symmetry, as long as it's convenient to use.
     */
    public interface SearchResult {
        @NonNull String getPersistentUrl();
        @NonNull String getValue(@NonNull String key);
        @NonNull String[] getValues(@NonNull String key);
        
        // FOR INDEX BROWSER (development/debugging aid) only
        Object getIndex(); // GSF Index
        Object getDocument(); // Lucene Document
        Object getIndexReader(); // Lucene IndexReader
        java.io.File getSegment(); // Segment directory
        //String[] getKeys(); // Set of field
        int getDocumentNumber();
    }
    
    
    public abstract void search(
            @NonNull final String key, 
            @NonNull final String value, 
            @NonNull final NameKind kind, 
            @NonNull final Set<SearchScope> scope, 
            @NonNull Set<SearchResult> result, 
            @NonNull final Set<String> includeKeys) throws IOException;
}
