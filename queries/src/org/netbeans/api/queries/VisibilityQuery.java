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

package org.netbeans.api.queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Determine whether files should be hidden in views presented to the user.
 * <p>
 * This query should be considered only as a recommendation. Particular views
 * may decide to display all files and ignore this query.
 * </p>
 * @see org.netbeans.spi.queries.VisibilityQueryImplementation
 * @author Radek Matous
 */
public final class VisibilityQuery {
    private static final VisibilityQuery INSTANCE = new VisibilityQuery();

    private final ResultListener resultListener = new ResultListener();
    private final VqiChangedListener vqiListener = new VqiChangedListener ();

    private final List<ChangeListener> listeners = Collections.synchronizedList(new ArrayList<ChangeListener>());
    private Lookup.Result<VisibilityQueryImplementation> vqiResult = null;
    private Set<VisibilityQueryImplementation> cachedVqiInstances = null;

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

        for (VisibilityQueryImplementation vqi : getVqiInstances()) {
            retVal = vqi.isVisible(file);
            if (!retVal) {
                break;
            }
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
            _listeners = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : _listeners) {
            l.stateChanged(ev);
        }
    }

    private synchronized Set<VisibilityQueryImplementation> getVqiInstances() {
        if (cachedVqiInstances == null) {
            vqiResult = Lookup.getDefault().lookupResult(VisibilityQueryImplementation.class);
            vqiResult.addLookupListener(resultListener);
            setupChangeListeners(cachedVqiInstances, new LinkedHashSet<VisibilityQueryImplementation>(vqiResult.allInstances()));
        }
        return cachedVqiInstances;
    }

    private synchronized void setupChangeListeners(final Set<VisibilityQueryImplementation> oldVqiInstances, final Set<VisibilityQueryImplementation> newVqiInstances) {
        if (oldVqiInstances != null) {
            Set<VisibilityQueryImplementation> removed = new HashSet<VisibilityQueryImplementation>(oldVqiInstances);
            removed.removeAll(newVqiInstances);
            for (VisibilityQueryImplementation vqi : removed) {
                vqi.removeChangeListener(vqiListener);
            }
        }

        Set<VisibilityQueryImplementation> added = new HashSet<VisibilityQueryImplementation>(newVqiInstances);
        if (oldVqiInstances != null) {
            added.removeAll(oldVqiInstances);
        }
        for (VisibilityQueryImplementation vqi : added) {
            vqi.addChangeListener(vqiListener);
        }

        cachedVqiInstances = newVqiInstances;
    }

    private class ResultListener implements LookupListener {
        public void resultChanged(LookupEvent ev) {
            setupChangeListeners(cachedVqiInstances, new LinkedHashSet<VisibilityQueryImplementation>(vqiResult.allInstances()));
            fireChange();
        }
    }

    private class VqiChangedListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            fireChange();
        }
    }

}
