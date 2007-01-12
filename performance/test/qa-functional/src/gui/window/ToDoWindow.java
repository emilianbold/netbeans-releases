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

package gui.window;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.TopComponentOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;


/**
 * Test of TODO Window
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class ToDoWindow extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    private String MENU, TITLE;

    /** Creates a new instance of ToDoWindow */
    public ToDoWindow(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of ToDoWindow */
    public ToDoWindow(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        MENU = Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Window") + "|" + Bundle.getStringTrimmed("org.netbeans.modules.tasklist.docscan.Bundle","BK0001");
        TITLE = Bundle.getStringTrimmed("org.netbeans.modules.tasklist.docscan.Bundle","win-title");
    }    
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Window / To Do from the main menu
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenu(MENU,"|");
        return new TopComponentOperator(TITLE);
    }
    
    public void close() {
        if(testedComponentOperator!=null && testedComponentOperator.isShowing())
            ((TopComponentOperator)testedComponentOperator).close();
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new ToDoWindow("measureTime"));
    }
}
