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
    
    /** Property name for last selected search pattern
     *  Firing: 
     *  oldValue - old selected pattern
     *  newValue - new selected pattern
     */
    public final static String LAST_SELECTED = "last-selected"; //NOI18N
    
    /** Property name for adding pattern to history
     *  Firing:
     *  oldValue - null
     *  newValue - added pattern
     */
    public final static String ADD_TO_HISTORY = "add-to-history"; //NOI18N
    
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
        if (pattern == null || pattern.getSearchExpression() == null || pattern.getSearchExpression().length() == 0){
            return;
        }
        if (searchPatternsList.size()>0 && pattern.equals(searchPatternsList.get(0))){
            return;
        }
        if (searchPatternsList.size() == MAX_SEARCH_PATTERNS_ITEMS){
            searchPatternsList.remove(MAX_SEARCH_PATTERNS_ITEMS-1);
        }
        searchPatternsList.add(0, pattern);
        if (pcs != null){
            pcs.firePropertyChange(ADD_TO_HISTORY, null, pattern);
        }
    }
    
}
