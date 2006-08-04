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
package org.netbeans.modules.debugger.jpda.actions;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;


/**
 *
 * @author  Jan Jancura
 */
public class SmartSteppingFilterImpl implements SmartSteppingFilter {

    private HashSet<String> filter = new HashSet<String>();
    private ArrayList<String> exact = new ArrayList<String>();
    private ArrayList<String> start = new ArrayList<String>();
    private ArrayList<String> end = new ArrayList<String>();
    private PropertyChangeSupport pcs;
    {pcs = new PropertyChangeSupport (this);}

    {
        addExclusionPatterns (
            (Set) Properties.getDefault ().getProperties ("debugger").
                getProperties ("sources").getProperties ("class_filters").
                getCollection (
                    "enabled", 
                    Collections.EMPTY_SET
                )
        );
    }
    
    
    /**
     * Adds a set of new class exclusion filters. Filter is 
     * {@link java.lang.String} containing full class name. Filter can 
     * begin or end with '*' to define more than one class, for example 
     * "*.Ted", or "examples.texteditor.*".
     *
     * @param patterns a set of class exclusion filters to be added
     */
    public void addExclusionPatterns (Set<String> patterns) {
        Set<String> reallyNew = new HashSet<String>(patterns);
        reallyNew.removeAll (filter);
        if (reallyNew.size () < 1) return;

        filter.addAll (reallyNew);
        refreshFilters (reallyNew);

        pcs.firePropertyChange (PROP_EXCLUSION_PATTERNS, null, reallyNew);
    }

    /**
     * Removes given set of class exclusion filters from filter.
     *
     * @param patterns a set of class exclusion filters to be added
     */
    public void removeExclusionPatterns (Set<String> patterns) {
        filter.removeAll (patterns);
        exact = new ArrayList<String>();
        start = new ArrayList<String>();
        end = new ArrayList<String>();
        refreshFilters (filter);

        pcs.firePropertyChange (PROP_EXCLUSION_PATTERNS, patterns, null);
    }
    
    /**
     * Returns list of all exclusion patterns.
     */
    public String[] getExclusionPatterns () {
        String[] ef = new String [filter.size ()];
        return filter.toArray (ef);
    }

    public boolean stopHere (String className) {
        int i, k = exact.size ();
        for (i = 0; i < k; i++) {
            if (exact.get (i).equals (className)) return false;
        }
        k = start.size ();
        for (i = 0; i < k; i++) {
            if (className.startsWith (start.get (i))) return false;
        }
        k = end.size ();
        for (i = 0; i < k; i++) {
            if (className.endsWith (end.get (i))) return false;
        }
        return true;
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public void removePropertyChangeListener (
        PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (l);
    }

    /**
     * Updates exact, start and end filter lists.
     */
    private void refreshFilters (Set<String> newFilters) {
        Iterator<String> i = newFilters.iterator ();
        while (i.hasNext ()) {
            String p = i.next ();
            if (p.startsWith ("*"))
                end.add (p.substring (1));
            else
            if (p.endsWith ("*"))
                start.add (p.substring (0, p.length () - 1));
            else
                exact.add (p);
        }
    }
}
