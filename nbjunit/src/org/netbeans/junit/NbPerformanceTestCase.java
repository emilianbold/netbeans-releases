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

import java.util.ArrayList;

/** Default implementation of NbPerformanceTest with added methods to collect
 * measured performance data.
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class NbPerformanceTestCase extends NbTestCase implements NbPerformanceTest {
    
    /** Creates a new instance of NbPerformanceTestCase
     * @param name String test name
     */
    public NbPerformanceTestCase(String name) {
        super(name);
    }
    
    private ArrayList data = new ArrayList();
    
    /** getter for all measured performance data from current test
     * @return PerformanceData[]
     */    
    public NbPerformanceTest.PerformanceData[] getPerformanceData() {
        return (NbPerformanceTest.PerformanceData[])(data.toArray(new NbPerformanceTest.PerformanceData[0]));
    }
    
    /** method for storing and reporting measured performance value,
     * test case name is used as value name and unit is not specified
     * @param value measured perofrmance value
     */    
    public void reportPerformance(long value) {
        reportPerformance(null, value, null);
    }
    
    /** method for storing and reporting measured performance value,
     * test case name is used as value name
     * @param value measured perofrmance value
     * @param unit unit name of measured value
     */    
    public void reportPerformance(long value, String unit) {
        reportPerformance(null, value, unit);
    }

    /** method for storing and reporting measured performance value,
     * unit is not specified
     * @param name measured value name
     * @param value measured perofrmance value
     */    
    public void reportPerformance(String name, long value) {
        reportPerformance(name, value, null);
    }
    
    /** method for storing and reporting measured performance value
     * @param name measured value name
     * @param value measured perofrmance value
     * @param unit unit name of measured value
     */    
    public void reportPerformance(String name, long value, String unit) {
        NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();
        d.name = name==null? getName() : name;
        d.value = value;
        d.unit = unit;
        data.add(d);
    }
    
}
