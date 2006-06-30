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
 * Software is Sun Microsystems, Inc. Portions Copyright 2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import org.netbeans.modules.search.types.FullTextType;
import org.openidex.search.SearchHistory;
import org.openidex.search.SearchType;

/**
 * Registry for everything related to the Find dialog.
 * It is <em>not</em> designed to be persistent across invocations
 * of the IDE.
 *
 * @author Marian Petras
 */
public final class FindDialogMemory implements PropertyChangeListener {

    /** singleton instance of this class */
    private static FindDialogMemory singleton;
    
    /**
     * stores information about which search types should be pre-initialized
     * from their history
     */
    private final HashMap searchTypesUsage = new HashMap(4);
    
    /**
     * stores the last used <code>SearchType</code>
     */
    private SearchType lastSearchType = null;
    
    /** Creates a new instance of FindDialogMemory */
    private FindDialogMemory() { }
    
    
    /**
     */
    public static FindDialogMemory getDefault() {
        if (singleton == null) {
            singleton = new FindDialogMemory();
        }
        return singleton;
    }
    
    /**
     */
    void initialize() {
        SearchHistory.getDefault().addPropertyChangeListener(this);
    }
    
    /**
     */
    void uninitialize() {
        SearchHistory.getDefault().removePropertyChangeListener(this);
    }
    
    /**
     * Clears information about used search types.
     */
    public void clearSearchTypesUsed() {
        searchTypesUsage.clear();
    }
    
    /**
     */
    public void setSearchTypeUsed(String searchTypeClsName,
                                         boolean used) {
        searchTypesUsage.put(searchTypeClsName, Boolean.valueOf(used));
    }
    
    /**
     */
    public boolean wasSearchTypeUsed(String searchTypeClsName) {
        return searchTypesUsage.get(searchTypeClsName) == Boolean.TRUE;
    }
    
    /**
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (SearchHistory.ADD_TO_HISTORY.equals(evt.getPropertyName())) {
            searchTypesUsage.put(FullTextType.class.getName(), Boolean.TRUE);
        }
    }
    
    /**
     */
    public void setLastUsedSearchType(SearchType searchType){
        lastSearchType = searchType;
    }
    
    /**
     */    
    public SearchType getLastSearchType(){
        return lastSearchType;
    }

}
