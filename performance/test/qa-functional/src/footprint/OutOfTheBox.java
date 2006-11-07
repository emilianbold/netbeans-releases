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

package footprint;

import org.netbeans.jemmy.operators.ComponentOperator;


/**
 * Measure Out Of The Box memory fooprint
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class OutOfTheBox extends MemoryFootprintTestCase {

    /**
     * Creates a new instance of OutOfTheBox
     * @param testName the name of the test
     */
    public OutOfTheBox(String testName) {
        super(testName);
        prefix = "Out Of The Box Startup |";
    }

    /**
     * Creates a new instance of OutOfTheBox
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OutOfTheBox(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "Out Of The Box Startup |";
    }
    
    public ComponentOperator open(){
        return null;
    }
    
    public void prepare() {
    }
    
    public void close(){
    }
    
    public void shutdown(){
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new OutOfTheBox("measureMemoryFooprint"));
    }
    
}
