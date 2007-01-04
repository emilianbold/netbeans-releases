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
import org.netbeans.jellytools.modules.javacvs.VersioningOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.StringComparator;

/**
 * Test opening Versioning Tab.
 *
 * @author  mmirilovic@netbeans.org
 */
public class VersioningWindow extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    private String MENU;

    /** Creates a new instance of VersioningWindow */
    public VersioningWindow(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of VersioningWindow */
    public VersioningWindow(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare() {
        // do nothing
    }
    
    protected void initialize() {
        MENU = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window") + '|' + 
               Bundle.getStringTrimmed("org.netbeans.modules.versioning.Bundle","Menu/Window/Versioning") + '|' +
               Bundle.getStringTrimmed("org.netbeans.modules.versioning.system.cvss.ui.actions.status.Bundle", "BK0001"); // Window | Versioning | CVS
    }
    
    public ComponentOperator open() {
        // invoke Versioning from the main menu
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        
        // the name of the window varies depends on opened projects - so we need to change the way we are looking for the window by name
        StringComparator oldComparator = MainWindowOperator.getDefault().getComparator();
        MainWindowOperator.getDefault().setComparator(new Operator.DefaultStringComparator(false, false));
        VersioningOperator vo =  new VersioningOperator();
        MainWindowOperator.getDefault().setComparator(oldComparator);
        return vo;
    }
    
    public void close() {
        if(testedComponentOperator!=null && testedComponentOperator.isShowing())
            ((VersioningOperator)testedComponentOperator).close();
    }

    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new VersioningWindow("measureTime"));
    }
    
}
