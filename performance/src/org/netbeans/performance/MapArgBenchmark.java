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
