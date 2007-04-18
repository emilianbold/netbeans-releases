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

/**
 * Measure startup time by org.netbeans.core.perftool.StartLog.
 * Number of starts with new userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat.with.new.userdir </code>
 * <br> and number of starts with old userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat </code>
 * Run measurement defined number times, but forget first measured value,
 * it's a attempt to have still the same testing conditions with
 * loaded and cached files.
 *
 * @author mmirilovic@netbeans.org
 */
public class ComplexVisualWebProjectStartup extends org.netbeans.performance.test.utilities.MeasureStartupTimeTestCase {
    
    /** Define testcase
     * @param testName name of the testcase
     */
    public ComplexVisualWebProjectStartup(String testName) {
        super(testName);
    }
    
    /** Testing start of IDE with measurement of the startup time.
     * @throws IOException
     */
    public void testStartIDEWithOpenedFilesNBproject() throws java.io.IOException {
        measureComplexStartupTime("Startup Time with opened Visual Web project");
    }
    

}
