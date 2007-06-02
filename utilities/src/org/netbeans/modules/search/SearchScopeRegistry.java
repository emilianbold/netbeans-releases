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

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static java.util.logging.Level.FINER;

/**
 * Registry of {@code SearchScope}s.
 * It holds information about registered {@code SearchScope}s
 * and it informs listeners about changes of their state
 * - see {@link #addChangeListener addChangeListener(...)},
 * {@link #removeChangeListener removeChangeListener(...)}.
 *
 * @author  Marian Petras
 */
public final class SearchScopeRegistry {
    
    private static SearchScopeRegistry instance;

    private final Logger LOG = Logger.getLogger(
            "org.netbeans.modules.search.FindAction_state");            //NOI18N
    
    private SearchScopeChangeHandler scopeChangeHandler;
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

    /*
     * Critical sections (access to...):
     *   - list of registered search scopes~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *       + register/unregister [any -> AWT]
     *       - has project search scope [AWT]
     *   - global state (enabled/disabled)~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *       + change of state of a single search scope [any -> AWT]
     *       - has applicable search scope [AWT]
     *   - list of registered change listeners~~~~~~~~~~~~~~~~~~~~~~~~
     *       + add/remove change listener [AWT]
     *       - notifications of changes of a global state [AWT]
     */
    
    public boolean hasApplicableSearchScope() {
        assert EventQueue.isDispatchThread();

        if (LOG.isLoggable(FINER)) {
            LOG.finer("hasApplicableSearchScope");
            LOG.finer(" - isListening(): " + isListening());
            if (isListening()) {
                LOG.finer(" - applicable: " + applicable);
            }
        }

        return isListening() ? applicable
                             : checkIsApplicable();
    }
    
    Map<SearchScope, Boolean> getSearchScopes() {
        assert EventQueue.isDispatchThread();

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
        assert EventQueue.isDispatchThread();
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

    public final void registerSearchScope(SearchScope searchScope) {
        if (LOG.isLoggable(FINER)) {
            LOG.finer("register search scope " + searchScope);
        }
        registerOrUnregister(searchScope, true);
    }

    public final void unregisterSearchScope(SearchScope searchScope) {
        if (LOG.isLoggable(FINER)) {
            LOG.finer("unregister search scope " + searchScope);
        }
        registerOrUnregister(searchScope, false);
    }

    private void registerOrUnregister(SearchScope searchScope,
                                      boolean register) {
        SearchScopeRegistrator registrator
                = new SearchScopeRegistrator(searchScope, register);
        if (EventQueue.isDispatchThread()) {
            registrator.run();
        } else {
            LOG.finer("- registration/unregistration postponed to AWT");
            EventQueue.invokeLater(registrator);
        }
    }

    private final class SearchScopeRegistrator implements Runnable {
        private final SearchScope searchScope;
        private final boolean register;    //false = unregister
        SearchScopeRegistrator(SearchScope scope, boolean register) {
            this.searchScope = scope;
            this.register = register;
        }
        public void run() {
            assert EventQueue.isDispatchThread();
            if (register) {
                registerSearchScopeAWT(searchScope);
            } else {
                unregisterSearchScopeAWT(searchScope);
            }
            LOG.finer("- registration/unregistration done");
        }
    }

    private void registerSearchScopeAWT(SearchScope searchScope) {
        assert EventQueue.isDispatchThread();

        boolean needsUpdate = false;
        if (isListening()) {
            assert scopeChangeHandler != null;
            boolean applicable = searchScope.isApplicable();
            searchScopes.put(searchScope, applicable);
            searchScope.addChangeListener(scopeChangeHandler);
            if (applicable) {
                applicableSearchScopesCount++;
                needsUpdate = true;
            }
        } else {
            searchScopes.put(searchScope, null);
        }
        
        if (isProjectSearchScope(searchScope)) {
            projectSearchScopesCount++;
        }
        if (needsUpdate) {
            assert isListening();
            updateIsApplicableByCount();
        }
    }
    
    public void unregisterSearchScopeAWT(SearchScope searchScope) {
        assert EventQueue.isDispatchThread();

        boolean needsUpdate = false;
        Boolean wasApplicable = searchScopes.remove(searchScope);
        if (isListening()) {
            assert scopeChangeHandler != null;
            assert wasApplicable != null;
            searchScope.removeChangeListener(scopeChangeHandler);
            if (Boolean.TRUE.equals(wasApplicable)) {
                applicableSearchScopesCount--;
                needsUpdate = true;
            }
        }
        
        if (isProjectSearchScope(searchScope)) {
            projectSearchScopesCount--;
        }
        if (needsUpdate) {
            assert isListening();
            updateIsApplicableByCount();
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
        if (LOG.isLoggable(FINER)) {
            LOG.finer("state changed to " + applicable);
        }
        
        if ((changeListeners != null) && !changeListeners.isEmpty()) {
            final ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener l : changeListeners) {
                l.stateChanged(e);
            }
        }
    }
    
    private void updateIsApplicableByCount() {
        if (LOG.isLoggable(FINER)) {
            LOG.finer("updateIsApplicableByCount()");
            LOG.finer(" - applicableSearchScopesCount = "
                                      + applicableSearchScopesCount);
        }
        setApplicable(applicableSearchScopesCount > 0);
    }
    
    private boolean checkIsApplicable() {
        assert EventQueue.isDispatchThread();
        LOG.finer("checkIsApplicable()");

        for (SearchScope searchScope : searchScopes.keySet()) {
            if (searchScope.isApplicable()) {
                LOG.finer(" - returning true");
                return true;
            }
        }
        LOG.finer(" - returning false");
        return false;
    }

    final class SearchScopeChangeHandler implements ChangeListener, Runnable {
        private final SearchScope searchScope;
        private SearchScopeChangeHandler() {
            this.searchScope = null;
        }
        private SearchScopeChangeHandler(SearchScope searchScope) {
            this.searchScope = searchScope;
        }
        public void stateChanged(ChangeEvent e) {
            assert e.getSource() instanceof SearchScope;
            final SearchScope searchScope = (SearchScope) e.getSource();
            if (EventQueue.isDispatchThread()) {
                searchScopeStateChanged(searchScope);
            } else {
                EventQueue.invokeLater(
                        new SearchScopeChangeHandler(searchScope));
            }
        }
        public void run() {
            assert searchScope != null;
            searchScopeStateChanged(searchScope);
        }
    }
    
    private void searchScopeStateChanged(SearchScope searchScope) {
        assert EventQueue.isDispatchThread();
        if (!isListening()) {
            //ignore
        } else {
            Boolean currValue = searchScopes.get(searchScope);
            assert currValue != null;
            boolean newValue = searchScope.isApplicable();
            if (newValue != currValue.booleanValue()) {
                searchScopes.put(searchScope, newValue);    //auto-boxing
                applicableSearchScopesCount += (newValue ? 1 : -1);
                updateIsApplicableByCount();
            }
        }
    }
    
    private void startListening() {
        assert EventQueue.isDispatchThread();
        assert scopeChangeHandler == null;

        LOG.finer("startListening()");

        applicableSearchScopesCount = 0;
        scopeChangeHandler = new SearchScopeChangeHandler();
        for (SearchScope searchScope : searchScopes.keySet()) {
            searchScope.addChangeListener(scopeChangeHandler);
            boolean isApplicable = searchScope.isApplicable();
            searchScopes.put(searchScope, isApplicable);
            if (isApplicable) {
                applicableSearchScopesCount++;
            }
        }
        applicable = applicableSearchScopesCount > 0;
    }
    
    private void stopListening() {
        assert EventQueue.isDispatchThread();

        LOG.finer("stopListening()");

        for (SearchScope searchScope : searchScopes.keySet()) {
            searchScope.removeChangeListener(scopeChangeHandler);
        }
        scopeChangeHandler = null;
        applicableSearchScopesCount = 0;
    }
    
    private boolean isListening() {
        assert EventQueue.isDispatchThread();
        return changeListeners != null;
    }

    public void addChangeListener(ChangeListener l) {
        assert EventQueue.isDispatchThread();

        if (LOG.isLoggable(FINER)) {
            LOG.finer("addChangeListener(" + l + ')');
        }

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
        }
    }

    public void removeChangeListener(ChangeListener l) {
        assert EventQueue.isDispatchThread();

        if (LOG.isLoggable(FINER)) {
            LOG.finer("removeChangeListener(" + l + ')');
        }

        if (l == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        
        if (changeListeners.remove(l) && changeListeners.isEmpty()) {
            changeListeners = null;
            stopListening();
        }
    }
    
}
