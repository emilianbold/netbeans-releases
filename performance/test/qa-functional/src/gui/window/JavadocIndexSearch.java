/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.TopComponentOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 * Test of Javadoc Index Search window
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class JavadocIndexSearch extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private String BUNDLE, MENU, TITLE;
    
    /** Creates a new instance of JavadocIndexSearch */
    public JavadocIndexSearch(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of JavadocIndexSearch */
    public JavadocIndexSearch(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        BUNDLE = "org.netbeans.modules.javadoc.search.Bundle";
        MENU = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/View") + "|" + org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE,"CTL_SEARCH_MenuItem");
        TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE,"CTL_SEARCH_WindowTitle");
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        return new TopComponentOperator(TITLE);
    }
    
}
