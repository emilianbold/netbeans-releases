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
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.MainWindowOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 * Test of Plugin Manager
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class PluginManager extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    protected String BUNDLE, MENU, TITLE;

    /** Creates a new instance of PluginManager */
    public PluginManager(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of PluginManager */
    public PluginManager(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        BUNDLE = "org.netbeans.modules.autoupdate.ui.actions.Bundle";
        MENU = Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Tools") + "|" + Bundle.getStringTrimmed(BUNDLE,"PluginManagerAction_Name");
        TITLE = Bundle.getStringTrimmed(BUNDLE,"PluginManager_Panel_Name");
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Tools / Update Center from the main menu
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        return new WizardOperator(TITLE);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new PluginManager("measureTime"));
    }
    
}
