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

package org.netbeans.junit;

import java.util.ArrayList;
import java.util.List;

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

    private List<NbPerformanceTest.PerformanceData> data = new ArrayList<NbPerformanceTest.PerformanceData>();
    
    /** getter for all measured performance data from current test
     * @return PerformanceData[]
     */    
    public NbPerformanceTest.PerformanceData[] getPerformanceData() {
        return data.toArray(new NbPerformanceTest.PerformanceData[0]);
    }
    
    /** method for storing and reporting measured performance value,
     * test case name is used as value name and unit is not specified
     * @param value measured perofrmance value
     */    
    public void reportPerformance(long value) {
        reportPerformance(null, value, null, NbPerformanceTest.PerformanceData.NO_ORDER);
    }
    
    /** method for storing and reporting measured performance value,
     * test case name is used as value name
     * @param value measured perofrmance value
     * @param unit unit name of measured value
     */    
    public void reportPerformance(long value, String unit) {
        reportPerformance(null, value, unit, NbPerformanceTest.PerformanceData.NO_ORDER);
    }

    /** method for storing and reporting measured performance value,
     * unit is not specified
     * @param name measured value name
     * @param value measured perofrmance value
     */    
    public void reportPerformance(String name, long value) {
        reportPerformance(name, value, null, NbPerformanceTest.PerformanceData.NO_ORDER);
    }
    
    /** method for storing and reporting measured performance value
     * @param name measured value name
     * @param value measured perofrmance value
     * @param unit unit name of measured value
     * @param runOrder order in which the data was measured (1st, 2nd, ...)
    */
    public void reportPerformance(String name, long value, String unit, int runOrder) {
        reportPerformance(name, value, unit, runOrder, PerformanceData.NO_THRESHOLD);
    }
    
    /** method for storing and reporting measured performance value
     * @param name measured value name
     * @param value measured perofrmance value
     * @param unit unit name of measured value
     * @param runOrder order in which the data was measured (1st, 2nd, ...)
     * @param threshold - measure threshold
     */    
    public void reportPerformance(String name, long value, String unit, int runOrder, long threshold) {
        NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();
        d.name = name==null? getName() : name;
        d.value = value;
        d.unit = unit;
        d.runOrder = runOrder;
        d.threshold = threshold;
        data.add(d);
    }
    
    
    
}
