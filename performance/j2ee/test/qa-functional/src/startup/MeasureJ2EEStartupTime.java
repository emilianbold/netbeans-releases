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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package startup;

import java.io.IOException;
import org.netbeans.performance.test.utilities.MeasureStartupTimeTestCase;

/**
 * Measures startup time by MeasureStartupTimeTestCase class.
 * Martin.Schovanek@sun.com
 */
public class MeasureJ2EEStartupTime extends MeasureStartupTimeTestCase {
    
    /** Define testcase
     * @param testName name of the testcase
     */
    public MeasureJ2EEStartupTime(java.lang.String testName) {
        super(testName);
    }
    
    @Override
    public void setUp() {
        System.out.println("########  "+getName()+"  ########");
    }

    /** Testing start of IDE with measurement of the startup time.
     * @throws IOException
     */
    public void testStartIDE() throws IOException {
        measureComplexStartupTime("Startup Time");
    }
    
    /** Testing start of IDE with measurement of the startup time.
     * @throws IOException
     */
    public void testStartIDEWithOpenedFiles() throws IOException {
        measureComplexStartupTime("Startup Time with opened J2EE projects");
    }
    
    /** Testing start of IDE with measurement of the startup time.
     * @throws IOException
     */
    public void testStartIDEWithWeb() throws IOException {
        measureComplexStartupTime("Startup Time with opened Web projects");
    }
}
