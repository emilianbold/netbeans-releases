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
package org.netbeans.performance.bde;

import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;

/** Describes one series */
public final class ArgumentSeries {
    
    private Map argument2Values;
    
    /** Creates new Interval */
    public ArgumentSeries() {
        argument2Values = new HashMap(23);
    }
    
    /** Adds a value to a given argument */
    void add(String arg, List vals) {
        argument2Values.put(arg, vals);
    }
    
    /** @return keys */
    public String[] getKeys() {
        return (String[]) argument2Values.keySet().toArray(new String[argument2Values.size()]);
    }
    
    /** @return an iteration of values for a given key */
    public Iterator getValues(String key) {
        List list = (List) argument2Values.get(key);
        if (list == null) {
            return Collections.EMPTY_LIST.iterator();
        } else {
            return Collections.unmodifiableList(list).iterator();
        }
    }
}
