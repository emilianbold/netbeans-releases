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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author  Marian Petras
 */
public final class SearchScopeRegistry implements ChangeListener {
    
    private static SearchScopeRegistry instance;
    
    private List<ChangeListener> changeListeners;
    private boolean applicable;

    private int projectSearchScopesCount;
    private int applicableSearchScopesCount;
    private Map<SearchScope, Boolean> searchScopes
            = new LinkedHashMap<SearchScope, Boolean>(5);
    
    public static SearchScopeRegistry getInstance() {
        if (instance == null) {
            instance = new SearchScopeRegistry();
        }
        return instance;
    }

    private SearchScopeRegistry() { }
    
    public synchronized boolean hasApplicableSearchScope() {
        return isListening() ? applicable
                             : checkIsApplicable();
    }
    
    synchronized Map<SearchScope, Boolean> getSearchScopes() {
        Map<SearchScope, Boolean> clone
                = new LinkedHashMap<SearchScope, Boolean>(searchScopes);
        if (!isListening()) {
            for (SearchScope searchScope : searchScopes.keySet()) {
                clone.put(searchScope, searchScope.isApplicable());
            }
        }
        return clone;
    }
    
    boolean hasProjectSearchScopes() {
        return projectSearchScopesCount > 0;
    }

    /**
     * Checks whether the given collection of {@code SearchScope}s contains
     * a project-type search scope.
     * 
     * @param  searchScopes  collection of search scopes to be checked
     * @return  {@code true} if the given collection contains at least
     *          one project-type search scope, {@code false} otherwise
     * @see  #isProjectSearchScope(SearchScope)
     */
    static boolean hasProjectSearchScopes(Collection<SearchScope> searchScopes) {
        if (searchScopes.isEmpty()) {
            return false;
        }

        for (SearchScope searchScope : searchScopes) {
            if (isProjectSearchScope(searchScope)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void registerSearchScope(SearchScope searchScope) {
        if (isListening()) {
            searchScopes.put(searchScope, searchScope.isApplicable());
            searchScope.addChangeListener(this);
            if (searchScope.isApplicable()) {
                applicableSearchScopesCount++;
            }
            updateIsApplicableByCount();
        } else {
            searchScopes.put(searchScope, null);
        }
        
        if (isProjectSearchScope(searchScope)) {
            projectSearchScopesCount++;
        }
    }
    
    public synchronized void unregisterSearchScope(SearchScope searchScope) {
        searchScope.removeChangeListener(this);
        Boolean wasApplicable = searchScopes.remove(searchScope);
        if (isListening()) {
            if (wasApplicable == Boolean.TRUE) {
                applicableSearchScopesCount--;
            }
            updateIsApplicableByCount();
        }
        
        if (isProjectSearchScope(searchScope)) {
            projectSearchScopesCount--;
        }
    }
    
    private static boolean isProjectSearchScope(SearchScope searchScope) {
        return searchScope.getClass().getName().startsWith(
                                "org.netbeans.modules.search.project"); //NOI18N
    }
    
    private void setApplicable(boolean applicable) {
        if (applicable == this.applicable) {
            return;
        }
        
        this.applicable = applicable;
        
        if ((changeListeners != null) && !changeListeners.isEmpty()) {
            final ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener l : changeListeners) {
                l.stateChanged(e);
            }
        }
    }
    
    private void updateIsApplicableByCount() {
        setApplicable(applicableSearchScopesCount > 0);
    }
    
    private synchronized boolean checkIsApplicable() {
        for (SearchScope searchScope : searchScopes.keySet()) {
            if (searchScope.isApplicable()) {
                return true;
            }
        }
        return false;
    }
    
    public final synchronized void stateChanged(ChangeEvent e) {
        if (!isListening()) {
            //ignore
        } else {
            assert e.getSource() instanceof SearchScope;
            SearchScope searchScope = (SearchScope) e.getSource();
            Boolean currValue = searchScopes.get(searchScope);
            assert currValue != null;
            boolean newValue = searchScope.isApplicable();
            if (newValue != currValue.booleanValue()) {
                if (newValue) {
                    applicableSearchScopesCount++;
                } else {
                    applicableSearchScopesCount--;
                }
                updateIsApplicableByCount();
            }
        }
    }
    
    private void startListening() {
        assert Thread.currentThread().holdsLock(this);
        applicableSearchScopesCount = 0;
        for (SearchScope searchScope : searchScopes.keySet()) {
            searchScope.addChangeListener(this);
            boolean isApplicable = searchScope.isApplicable();
            searchScopes.put(searchScope, isApplicable);
            if (isApplicable) {
                applicableSearchScopesCount++;
            }
        }
    }
    
    private void stopListening() {
        assert Thread.currentThread().holdsLock(this);
        for (SearchScope searchScope : searchScopes.keySet()) {
            searchScope.removeChangeListener(this);
        }
    }
    
    private boolean isListening() {
        assert Thread.currentThread().holdsLock(this);
        return changeListeners != null;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        if (l == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        
        boolean firstListener = ((changeListeners == null) || changeListeners.isEmpty());
        if (changeListeners == null) {
            changeListeners = new ArrayList<ChangeListener>(1);
        }
        changeListeners.add(l);
        if (firstListener) {
            startListening();
            applicable = checkIsApplicable();
        }
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        if (l == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        
        if (changeListeners.remove(l) && changeListeners.isEmpty()) {
            changeListeners = null;
            stopListening();
        }
    }
    
}
