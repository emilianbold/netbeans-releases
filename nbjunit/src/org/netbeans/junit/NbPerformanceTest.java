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

import junit.framework.Test;

/** Interface extending JUnit test to store measured performance data
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public interface NbPerformanceTest extends NbTest {

    /** Helper class holding one measured performance value */
    public static class PerformanceData extends Object {
        // no measue order defined
        public static final int NO_ORDER = 0;
        // no threshold defined
        public static final long NO_THRESHOLD = 0;
        /** performance value name */        
        public String name;
        /** easured performance value */        
        public long value;
        /** performance value unit */        
        public String unit;
        /** run order - for same performance name, which run of the test is it **/
        public int runOrder;
        /** threshold for measured data **/
        public long threshold;
    }
    
    /** getter for all measured performance data from current test
     * @return PerformanceData[]
     */    
    public PerformanceData[] getPerformanceData();
    
}
