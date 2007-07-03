/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.gsf;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * Encodes a reference type,
     * used by {@link ClassIndex#getElements} and {@link ClassIndex#getResources}
     * to restrict the search.
     */
    public enum SearchKind {
        
        /**
         * The returned class has to extend or implement given element
         */
        IMPLEMENTORS,
        
        /**
         * The returned class has to call method on given element
         */
        METHOD_REFERENCES,
        
        /**
         * The returned class has to access a field on given element
         */
        FIELD_REFERENCES,
        
        /**
         * The returned class contains references to the element type
         */
        TYPE_REFERENCES,        
    };
    
    /**
     * Scope used by {@link ClassIndex} to search in
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
        String getValue(String key);
        String[] getValues(String key);
        
        // For index browser (development/debugging aid) only
        Object getIndex(); // GSF Index
        Object getDocument(); // Lucene Document
        Object getIndexReader(); // Lucene IndexReader
        java.io.File getSegment(); // Segment directory
        //String[] getKeys(); // Set of field
        int getDocumentNumber();
    }
    
    // TODO: Find a way to communicate which fields should not be tokenized or indexed...
    
    // Store map of class names, where each entry has a map of fields and values (fields might be "name", "fqn", "case insensitive name", etc.
    // The same fields can be looked up later.
    // TODO: The first key is redundant here (it's repeated as part of the fields; just make this a List<Map> instead!
    public abstract void gsfStore(Set<Map<String,String>> fieldToData, Set<Map<String,String>> noIndexData, Map<String,String> toDelete) throws IOException;
    public abstract void gsfSearch(final String primaryField, final String name, final NameKind kind, 
            final Set<SearchScope> scope, final Set<SearchResult> result) throws IOException;
}
