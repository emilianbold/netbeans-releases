/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.debugger.jpda.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;


/**
 *
 * @author  Jan Jancura
 */
class SmartSteppingFilterImpl implements SmartSteppingFilter {

    private HashSet filter = new HashSet ();
    private ArrayList exact = new ArrayList ();
    private ArrayList start = new ArrayList ();
    private ArrayList end = new ArrayList ();


    /**
     * Adds a set of new class exclusion filters. Filter is 
     * {@link java.lang.String} containing full class name. Filter can 
     * begin or end with '*' to define more than one class, for example 
     * "*.Ted", or "examples.texteditor.*".
     *
     * @param patterns a set of class exclusion filters to be added
     */
    public void addExclusionPatterns (Set patterns) {
        Set reallyNew = new HashSet (patterns);
        reallyNew.removeAll (filter);

        filter.addAll (reallyNew);
        refreshFilters (reallyNew);

//        if (ssManager != null)
//            ssManager.addPatterns (reallyNew);
    }

    /**
     * Removes given set of class exclusion filters from filter.
     *
     * @param patterns a set of class exclusion filters to be added
     */
    public void removeExclusionPatterns (Set patterns) {
        filter.removeAll (patterns);
        exact = new ArrayList ();
        start = new ArrayList ();
        end = new ArrayList ();
        refreshFilters (filter);

//        if (ssManager != null)
//            ssManager.initPatterns ();
    }

    Set getPatterns () {
        return filter;
    }

    /**
     * Updates exact, start and end filter lists.
     */
    private void refreshFilters (Set newFilters) {
        Iterator i = filter.iterator ();
        while (i.hasNext ()) {
            String p = (String) i.next ();
            if (p.startsWith ("*"))
                end.add (p.substring (1));
            else
            if (p.endsWith ("*"))
                start.add (p.substring (0, p.length () - 1));
            else
                exact.add (p);
        }
    }

    public boolean stopHere (String className) {
        int i, k = exact.size ();
        for (i = 0; i < k; i++) {
            if (exact.get (i).equals (className)) return false;
        }
        k = start.size ();
        for (i = 0; i < k; i++) {
            if (className.startsWith ((String) start.get (i))) return false;
        }
        k = end.size ();
        for (i = 0; i < k; i++) {
            if (className.endsWith ((String) end.get (i))) return false;
        }
        return true;
    }
}
