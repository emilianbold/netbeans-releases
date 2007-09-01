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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.Lookup;
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
    
    private static SearchScopeRegistry defaultInstance;

    private static final Logger LOG = Logger.getLogger(
            "org.netbeans.modules.search.SearchScopeRegistry");         //NOI18N
    
    private final int id;
    private Collection<Reference<SearchScopeRegistry>> extraInstances;
    private SearchScopeChangeHandler scopeChangeHandler;
    private List<ChangeListener> changeListeners;

    private int projectSearchScopesCount;
    private int applicableSearchScopesCount;
    private Map<SearchScope, Boolean> searchScopes
            = new LinkedHashMap<SearchScope, Boolean>(5);
    
    public static synchronized SearchScopeRegistry getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new SearchScopeRegistry(0);
        }
        return defaultInstance;
    }

    static SearchScopeRegistry getInstance(Lookup lookup, int id) {
        return getDefault().getLookupInstance(lookup, id);
    }

    private SearchScopeRegistry(int id) {
        this.id = id;
    }

    private SearchScopeRegistry getLookupInstance(final Lookup lookup,
                                                  final int id) {
        assert id > 0;
        assert EventQueue.isDispatchThread();
        assert this == defaultInstance;
        if (LOG.isLoggable(FINER)) {
            LOG.finer("getLookupInstance(Lookup, " + id + ')');
        }

        SearchScopeRegistry instance;
        Collection<SearchScope> scopes;
        synchronized (getLock()) {
            instance = new SearchScopeRegistry(id);
            if (extraInstances == null) {
                extraInstances = new ArrayList<Reference<SearchScopeRegistry>>(4);
            }
            extraInstances.add(new WeakReference<SearchScopeRegistry>(instance));

            scopes = cloneSearchScopes(defaultInstance);
        }
        for (SearchScope scope : scopes) {
            instance.registerSearchScope(
                    scope.getContextSensitiveInstance(lookup));
        }
        return instance;
    }

    public void registerSearchScope(final SearchScope searchScope) {
        /* thread: <any> */
        if (LOG.isLoggable(FINER)) {
            log("register search scope " + searchScope);
        }

        final Collection<ChangeListener> listeners;
        final Collection<SearchScopeRegistry> lookupInstances;
        synchronized (getLock()) {
            if (scopeChangeHandler != null) {       //listening
                searchScope.addChangeListener(scopeChangeHandler);
                listeners = checkNewState(searchScope, Boolean.TRUE)
                            ? cloneChangeListeners()
                            : null;
            } else {
                searchScopes.put(searchScope, null);
                listeners = null;
            }
            if (isProjectSearchScope(searchScope)) {
                projectSearchScopesCount++;
            }
            lookupInstances = cloneLookupInstances();
        }
        if (listeners != null) {
            notifyListeners(listeners);
        }
        if (!lookupInstances.isEmpty()) {
            for (SearchScopeRegistry instance : lookupInstances) {
                instance.registerSearchScope(searchScope);
            }
        }
    }

    public void unregisterSearchScope(final SearchScope searchScope) {
        /* thread: <any> */
        if (LOG.isLoggable(FINER)) {
            log("unregister search scope " + searchScope);
        }

        final Collection<ChangeListener> listeners;
        final Collection<SearchScopeRegistry> lookupInstances;
        synchronized (getLock()) {
            if (scopeChangeHandler != null) {       //listening
                searchScope.removeChangeListener(scopeChangeHandler);
                listeners = checkNewState(searchScope, Boolean.FALSE)
                            ? cloneChangeListeners()
                            : null;
            } else {
                searchScopes.remove(searchScope);
                listeners = null;
            }
            if (isProjectSearchScope(searchScope)) {
                projectSearchScopesCount--;
            }
            lookupInstances = cloneLookupInstances();
        }
        if (!lookupInstances.isEmpty()) {
            for (SearchScopeRegistry instance : lookupInstances) {
                instance.unregisterSearchScope(searchScope);
            }
        }
    }
    
    void addChangeListener(final ChangeListener l) {
        /* thread: <any> */
        assert l != null;
        if (LOG.isLoggable(FINER)) {
            log("addChangeListener(" + l + ')');
        }

        synchronized (getLock()) {
            boolean firstListener = (changeListeners == null);
            if (changeListeners == null) {
                changeListeners = new ArrayList<ChangeListener>(1);
            }
            changeListeners.add(l);

            if (firstListener) {
                assert applicableSearchScopesCount == 0;
                applicableSearchScopesCount = 0;
                scopeChangeHandler = new SearchScopeChangeHandler();
                for (Map.Entry<SearchScope,Boolean> entry : searchScopes.entrySet()) {
                    SearchScope scope = entry.getKey();
                    scope.addChangeListener(scopeChangeHandler);
                    boolean applicable = scope.isApplicable();
                    if (applicable) {
                        applicableSearchScopesCount++;
                    }
                    entry.setValue(applicable);     //auto-boxing
                }
                if (LOG.isLoggable(FINER)) {
                    log(" - initial applicable search scopes count: "
                         + applicableSearchScopesCount);
                }
            }
        }
    }

    void removeChangeListener(final ChangeListener l) {
        /* thread: <any> */
        assert l != null;
        if (LOG.isLoggable(FINER)) {
            log("removeChangeListener(" + l + ')');
        }

        synchronized (getLock()) {
            if (changeListeners == null) {
                return;
            }
            boolean lastListener = changeListeners.remove(l) && changeListeners.isEmpty();
            if (lastListener) {
                changeListeners = null;
                applicableSearchScopesCount = 0;
                for (SearchScope scope : searchScopes.keySet()) {
                    scope.removeChangeListener(scopeChangeHandler);
                }
                scopeChangeHandler = null;
            }
        }
    }

    private final class SearchScopeChangeHandler implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            assert e.getSource() instanceof SearchScope;
            searchScopeStateChanged((SearchScope) e.getSource());
        }
    }
    
    private void searchScopeStateChanged(final SearchScope searchScope) {
        /* thread: <any> */
        if (LOG.isLoggable(FINER)) {
            log("searchScopeStateChanged(" + searchScope + ')');
        }

        final Collection<ChangeListener> listeners;
        synchronized (getLock()) {
            listeners = checkNewState(searchScope, null)
                        ? cloneChangeListeners()
                        : null;
        }
        if (listeners != null) {
            notifyListeners(listeners);
        }
    }

    private boolean checkNewState(final SearchScope searchScope,
                                  final Boolean addition) {
        /* thread: <any> */
        assert Thread.holdsLock(getLock());
        if (LOG.isLoggable(FINER)) {
            log("checkNewState(" + searchScope + ')');
        }

        boolean newValue;
        if (addition == null) {                     //SearchScope state changed
            newValue = searchScope.isApplicable();
            Boolean oldValue = searchScopes.put(searchScope, newValue);
            if (oldValue == null) {
                searchScopes.remove(searchScope);
                return false;
            }
            if (newValue == oldValue.booleanValue()) {
                return false;
            }
        } else if (addition.booleanValue()) {       //SearchScope registered
            newValue = searchScope.isApplicable();
            searchScopes.put(searchScope, newValue);
        } else {                                    //SearchScope unregistered
            newValue = false;
            searchScopes.remove(searchScope);
        }

        boolean stateChanged;
        if (newValue) {
            applicableSearchScopesCount++;
            if (LOG.isLoggable(FINER)) {
                log(" - search scope count increased to "
                    + applicableSearchScopesCount);
            }
            stateChanged = (applicableSearchScopesCount == 1);
        } else {
            applicableSearchScopesCount--;
            if (LOG.isLoggable(FINER)) {
                log(" - search scope count decreased to "
                    + applicableSearchScopesCount);
            }
            stateChanged = (applicableSearchScopesCount == 0);
        }
        return stateChanged;
    }

    private void notifyListeners(Collection<ChangeListener> listeners) {
        assert listeners != null;

        final ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(e);
        }
    }
    
    SearchScope getNodeSelectionSearchScope() {
        /*
         * There are several conditions that must be met for implementation
         * of this method to work correctly:
         *   - the Map of search scopes (field "searchScopes") preserves order
         *   - node selection search scope is the first registered search scope
         *   - lookup-sensitive search scopes are registered in the same order
         *     as the default search scopes
         *   - lookup-sensitive variant of SearchScopeNodeSelection
         *     is a (direct or indirect) subclass of SearchScopeNodeSelection
         */
        SearchScope nodeSelectionScope;
        synchronized (getLock()) {
            if (searchScopes.isEmpty()) {
                nodeSelectionScope = null;
            } else {
                nodeSelectionScope = searchScopes.entrySet().iterator().next().getKey();
                assert nodeSelectionScope.getClass().getName().startsWith(
                    "org.netbeans.modules.search.SearchScopeNodeSelection");//NOI18N
            }
        }
        return nodeSelectionScope;
    }

    public boolean hasApplicableSearchScope() {
        /* thread: <any> */
        if (LOG.isLoggable(FINER)) {
            log("hasApplicableSearchScope");
        }

        synchronized (getLock()) {
            if (changeListeners != null) {
                if (LOG.isLoggable(FINER)) {
                    log(" - listening, search scopes count = "
                        + applicableSearchScopesCount);
                }
                return (applicableSearchScopesCount != 0);
            } else {
                if (LOG.isLoggable(FINER)) {
                    log(" - not listening, going to check...");
                }
                return checkIsApplicable();
            }
        }
    }
    
    Map<SearchScope, Boolean> getSearchScopes() {
        assert EventQueue.isDispatchThread();

        final Map<SearchScope, Boolean> result;
        final Collection<SearchScope> scopes;
        synchronized (getLock()) {
            if (changeListeners != null) {
                return new LinkedHashMap<SearchScope, Boolean>(searchScopes);
            }

            scopes = cloneSearchScopes();
        }

        result = new LinkedHashMap<SearchScope, Boolean>(scopes.size() * 2);
        for (SearchScope scope : scopes) {
            result.put(scope, scope.isApplicable());
        }
        return result;
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

    private static boolean isProjectSearchScope(SearchScope searchScope) {
        return searchScope.getClass().getName().startsWith(
                                "org.netbeans.modules.search.project"); //NOI18N
    }
    
    private boolean checkIsApplicable() {
        /* thread: <any> */
        if (LOG.isLoggable(FINER)) {
            log("checkIsApplicable()");
        }

        final Collection<SearchScope> scopes;
        synchronized (getLock()) {
            scopes = cloneSearchScopes();
        }
        for (SearchScope searchScope : scopes) {
            if (searchScope.isApplicable()) {
                if (LOG.isLoggable(FINER)) {
                    log(" - returning true");
                }
                return true;
            }
        }
        if (LOG.isLoggable(FINER)) {
            log(" - returning false");
        }
        return false;
    }

    private Object getLock() {
        return this;
    }

    /**
     * Gets a copy of lookup-sensitive {@code SearchScopeRegistry}
     * instances that are accessible via references held in
     * {@link #extraInstances}.
     * 
     * @return  collection of lookup-sensitive {@code SearchScopeRegistry}
     *          instances, or an empty list of there are no lookup-sensitive
     *          instances accessible
     */
    private Collection<SearchScopeRegistry> cloneLookupInstances() {
        assert Thread.holdsLock(getLock());

        Collection<SearchScopeRegistry> extras;
        if ((extraInstances != null) && !extraInstances.isEmpty()) {
            extras = new ArrayList<SearchScopeRegistry>(extraInstances.size());
            Iterator<Reference<SearchScopeRegistry>> it = extraInstances.iterator();
            while (it.hasNext()) {
                Reference<SearchScopeRegistry> extraInstanceRef = it.next();
                SearchScopeRegistry inst = extraInstanceRef.get();
                if (inst == null) {
                    it.remove();
                    continue;
                }
                extras.add(inst);
            }
            assert extras.size() == extraInstances.size();
        } else {
            extras = null;
        }
        if ((extras == null) || extras.isEmpty()) {
            extras = null;
            extraInstances = null;
        }
        return (extras != null) ? extras
                                : Collections.<SearchScopeRegistry>emptyList();
    }

    /**
     */
    private Collection<SearchScope> cloneSearchScopes() {
        return cloneSearchScopes(this);
    }

    /**
     */
    private Collection<SearchScope> cloneSearchScopes(SearchScopeRegistry fromInstance) {
        assert Thread.holdsLock(getLock());

        return new ArrayList<SearchScope>(fromInstance.searchScopes.keySet());
    }

    /**
     */
    private Collection<ChangeListener> cloneChangeListeners() {
        assert Thread.holdsLock(getLock());

        return (changeListeners != null)
               ? new ArrayList<ChangeListener>(changeListeners)
               : null;
    }

    private void log(String msg) {
        LOG.finer("registry #" + id + ": " + msg);
    }

}
