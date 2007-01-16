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
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.WizardOperator;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Proxy Configuration.
 *
 * @author  mmirilovic@netbeans.org
 */
public class ProxyConfiguration extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    JButtonOperator openProxyButton;
    WizardOperator wizard;
    private String BUNDLE, BUTTON, MENU, TITLE_1;
    
    /** Creates a new instance of ProxyConfiguration */
    public ProxyConfiguration(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of ProxyConfiguration */
    public ProxyConfiguration(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        BUNDLE = "org.netbeans.modules.autoupdate.Bundle";
        MENU = Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Tools") + "|" + Bundle.getStringTrimmed(BUNDLE,"CTL_Update");
        TITLE_1 = Bundle.getStringTrimmed(BUNDLE,"CTL_Wizard");
        BUTTON = Bundle.getStringTrimmed(BUNDLE,"BNT_Proxy");
        
        // open the Update Center wizard
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        
        wizard = new WizardOperator(TITLE_1);
        openProxyButton = new JButtonOperator(wizard, BUTTON);
    }
    
    public void prepare(){
        // do nothing
    }
    
    public ComponentOperator open(){
        // invoke an action
        openProxyButton.pushNoBlock();
        return new OptionsOperator();
    }

    public void shutdown() {
        if(wizard!=null && wizard.isShowing())
            wizard.close();
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new ProxyConfiguration("measureTime"));
    }
    
}
