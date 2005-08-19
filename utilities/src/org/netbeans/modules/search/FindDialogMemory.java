/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2005 Sun
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
