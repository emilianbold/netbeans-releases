/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.queries;

import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.*;

/**
 * Determine whether files should be hidden in views 
 * presented to the user. This query should be considered 
 * only as a recommendation, there isn't necessary to obey it.   
 * and it is permitted to display all files if the context warrants it.     
 * @see org.netbeans.spi.queries.VisibilityQueryImplementation
 * @author Radek Matous 
 */
public final class VisibilityQuery {
    private static final VisibilityQuery INSTANCE = new VisibilityQuery();
    private static final Lookup.Template TEMPLATE = new Lookup.Template(VisibilityQueryImplementation.class);
    
    private final ResultListener resultListener = new ResultListener();
    private final VqiChangedListener vqiListener = new VqiChangedListener ();
    
    private final List/*<ChangeListener>*/ listeners = Collections.synchronizedList(new ArrayList());    
    private Lookup.Result vqiResult = null;
    private Set/*<VisibilityQueryImplementation>*/ cachedVqiInstances = null;
    
    /**
     * Get default instance of VisibilityQuery.
     * @return instance of VisibilityQuery
     */ 
    public static final VisibilityQuery getDefault() {
        return INSTANCE;
    }

    private VisibilityQuery() {
    }

    /**
     * Check whether a file is recommended to be visible.
     * Default return value is visible unless at least one VisibilityQueryImplementation
     * provider says hidden.
     * @param file a file which should be checked 
     * @return true if it is recommended to show this file 
     */
    public boolean isVisible(FileObject file) {
        boolean retVal = true;

        Set vqiInstances = getVqiInstances();
        
        for (Iterator iterator = vqiInstances.iterator(); retVal &&  iterator.hasNext();) {
            VisibilityQueryImplementation vqi = (VisibilityQueryImplementation) iterator.next();
            retVal = vqi.isVisible(file);            
        }
        
        return retVal;
    }

    /**
     * Add a listener to changes.
     * @param l a listener to add
     */
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
       
    /**
     * Stop listening to changes.
     * @param l a listener to remove
     */
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
        
    private void fireChange() {
        ChangeListener[] _listeners;
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
            _listeners = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (int i = 0; i < _listeners.length; i++) {
            _listeners[i].stateChanged(ev);
        }
    }
    
    private synchronized Set getVqiInstances() {
        if (cachedVqiInstances == null) {
            vqiResult = Lookup.getDefault().lookup(TEMPLATE);
            vqiResult.addLookupListener(resultListener);
            setupChangeListeners(cachedVqiInstances, new LinkedHashSet(vqiResult.allInstances()));
        }
        return cachedVqiInstances;
    }

    private synchronized void setupChangeListeners (final Set oldVqiInstances, final Set newVqiInstances) {
        if (oldVqiInstances != null) {
            Set removed = new HashSet(oldVqiInstances);
            removed.removeAll(newVqiInstances);
            for (Iterator iterator = removed.iterator(); iterator.hasNext();) {
                VisibilityQueryImplementation vqi = (VisibilityQueryImplementation) iterator.next();
                vqi.removeChangeListener(vqiListener);
            }            
        }

        Set added = new HashSet (newVqiInstances);
        if (oldVqiInstances != null) {
            added.removeAll(oldVqiInstances);    
        }
        for (Iterator iterator = added.iterator(); iterator.hasNext();) {
            VisibilityQueryImplementation vqi = (VisibilityQueryImplementation) iterator.next();
            vqi.addChangeListener(vqiListener);
        }
        
        cachedVqiInstances = newVqiInstances; 
    }

    private class ResultListener implements LookupListener {
        public void resultChanged(LookupEvent ev) {
            setupChangeListeners(cachedVqiInstances, new LinkedHashSet(vqiResult.allInstances()));
            fireChange();
        }
    }
    
    private class VqiChangedListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            fireChange();
        }
    }
    
}
