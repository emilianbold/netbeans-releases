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
    private String BUNDLE, BUTTON, MENU, TITLE_1, TITLE;
    
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
        MENU = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Tools") + "|" + org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE,"CTL_Update");
        TITLE_1 = org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE,"CTL_Wizard");
        BUTTON = org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE,"BNT_Proxy");
        TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE,"CTL_ProxyDialog_Title");
        
        // open the Update Center wizard
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        wizard = new WizardOperator(TITLE_1);
        openProxyButton = new JButtonOperator(wizard, BUTTON);
    }
    
    public void prepare(){
        // do nothing
    }
    
    public ComponentOperator open(){
        // invoke the action
        openProxyButton.pushNoBlock();
        return new org.netbeans.jellytools.NbDialogOperator(TITLE);
    }

    public void shutdown() {
        if(wizard!=null && wizard.isShowing())
            wizard.close();
    }
}
