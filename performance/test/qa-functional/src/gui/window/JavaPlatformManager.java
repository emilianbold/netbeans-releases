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
import org.netbeans.jellytools.NbDialogOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 * Test of Java Platform Manager
 *
 * @author  mmirilovic@netbeans.org
 */
public class JavaPlatformManager extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private String MENU, TITLE;
    
    /** Creates a new instance of JavaPlatformManager */
    public JavaPlatformManager(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of JavaPlatformManager */
    public JavaPlatformManager(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare() {
        MENU = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Tools") + "|" + org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.java.platform.ui.Bundle","CTL_PlatformManager");
        TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.api.java.platform.Bundle","TXT_PlatformsManager");
        // do nothing
        gui.Utilities.workarroundMainMenuRolledUp();
   }
    
    public ComponentOperator open() {
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        return new NbDialogOperator(TITLE);
    }
    
}
