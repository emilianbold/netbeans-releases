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
package org.netbeans.performance;

import java.util.Map;
import java.util.HashMap;


/**
 * Benchmark which arguments are always Maps
 */
public class MapArgBenchmark extends Benchmark {
    
    /** Creates new Benchmark without arguments for given test method
     * @param name the name fo the testing method
     */    
    public MapArgBenchmark(String name) {
        super(name);
    }

    /** Creates new Benchmark for given test method with given set of arguments
     * @param name the name fo the testing method
     * @param args the array of objects describing arguments to testing method
     */    
    public MapArgBenchmark(String name, Object[] args) {
        super(name, args);
    }
    
    /** Creates a Map with default arguments values */
    protected Map createDefaultMap() {
        return new HashMap();
    }
    
    /** Sets argument of mab to this MapArgBenchmark */
    public final void setParent(MapArgBenchmark mab) {
        setArgument(mab.getArgument());
    }
    
    /** @return an int value bound to key */
    protected final int getIntValue(String key) {
        Map param = (Map) getArgument();
        if (param == null) {
            return 0;
        }
        Integer i = (Integer) param.get(key);
        if (i == null) {
            return 0;
        }
        return i.intValue();
    }
}
