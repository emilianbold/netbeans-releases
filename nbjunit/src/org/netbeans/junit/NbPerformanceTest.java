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
package org.netbeans.junit;

import junit.framework.Test;

/** Interface extending JUnit test to store measured performance data
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public interface NbPerformanceTest extends NbTest {

    /** Helper class holding one measured performance value */    
    public static class PerformanceData extends Object {
        public static final int NO_ORDER = 0;
        /** performance value name */        
        public String name;
        /** easured performance value */        
        public long value;
        /** performance value unit */        
        public String unit;
        /** run order - for same performance name, which run of the test is it **/
        public int runOrder;
    }
    
    /** getter for all measured performance data from current test
     * @return PerformanceData[]
     */    
    public PerformanceData[] getPerformanceData();
    
}
