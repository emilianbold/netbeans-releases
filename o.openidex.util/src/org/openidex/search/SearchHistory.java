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

package org.openidex.search;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shareable search history. Known implementations are explorer search 
 * dialog and editor find&replace dialog. 
 * 
 * Typical use case:
 * Editor registers a listener to listen on lastSelected SearchPattern. If user
 * opens explorer's search dialog and perform search, a search expression is added
 * into SearchHistory and lastSelected SearchPattern is setted. The event is fired,
 * editor can retrieve lastSelected SearchPattern and in accordance with its parameters
 * it can highlight(in yellow) all matched patterns. If editor dialog is open,
 * it contains shareable SearchHistory. Another direction is search in editor, that 
 * adds a SearchPattern in SearchHistory, thus the new item is available also in
 * explorer's search dialog.
 *
 * @since  org.openidex.util/3 3.5, NB 4.1
 * @author  Martin Roskanin
 */
public final class SearchHistory {

    /** Last selected SearchPattern. */
    private SearchPattern lastSelected;
    
    /** Support for listeners */
    private PropertyChangeSupport pcs;

    /** Maximum items allowed in searchPatternsList */
    private static final int MAX_SEARCH_PATTERNS_ITEMS = 50;

    /** Shareable SearchPattern history. It is a List of SearchPatterns */
    private List searchPatternsList = new ArrayList(MAX_SEARCH_PATTERNS_ITEMS);

    /** Singleton instance */
    private static SearchHistory INSTANCE = null;
    
    /** Property name for last selected search pattern */
    public final static String LAST_SELECTED = "last-selected"; //NOI18N
    
    /** Creates a new instance of SearchHistory */
    private SearchHistory() {
    }

    /** @return singleton instance of SearchHistory */
    public synchronized static SearchHistory getDefault(){
        if (INSTANCE == null) {
            INSTANCE = new SearchHistory();
        }
        return INSTANCE;
    }
    
    /** @return last selected SearchPattern */
    public SearchPattern getLastSelected(){
        return lastSelected;
    }
    
    /** Sets last selected SearchPattern 
     *  @param pattern last selected pattern
     */
    public void setLastSelected(SearchPattern pattern){
        SearchPattern oldPattern = this.lastSelected;
        this.lastSelected = pattern;
        if (pcs != null){
            pcs.firePropertyChange(LAST_SELECTED, oldPattern, pattern);
        }
    }
    
    private synchronized PropertyChangeSupport getPropertyChangeSupport(){
        if (pcs == null){
            pcs = new PropertyChangeSupport(this);
        }
        return pcs;
    }

    /** Adds a property change listener.
     * @param pcl the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl){
        getPropertyChangeSupport().addPropertyChangeListener(pcl);
    }
    
    /** Removes a property change listener.
     * @param pcl the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl){
        if (pcs != null){
            pcs.removePropertyChangeListener(pcl);
        }
    }

    /** @return unmodifiable List of SearchPatterns */
    public synchronized List/*<SearchPattern>*/ getSearchPatterns(){
        return Collections.unmodifiableList(searchPatternsList);
    }
    
    /** Adds SearchPattern to SearchHistory 
     *  @param pattern the SearchPattern to add
     */
    public synchronized void add(SearchPattern pattern){
        if (searchPatternsList.size() == MAX_SEARCH_PATTERNS_ITEMS){
            searchPatternsList.remove(MAX_SEARCH_PATTERNS_ITEMS-1);
        }
        searchPatternsList.add(0, pattern);
    }
    
}
