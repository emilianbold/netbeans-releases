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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 * Test of Internationalization Window
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class InternationalizeDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static EditorOperator editor;
    private String BUNDLE, MENU, TITLE;
    
    /** Creates a new instance of InternationalizeDialog */
    public InternationalizeDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of InternationalizeDialog */
    public InternationalizeDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        BUNDLE = "org.netbeans.modules.i18n.Bundle";
        MENU = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Tools") + "|" + org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE,"LBL_I18nGroupActionName") + "|" + org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE,"CTL_I18nAction");
        TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE,"CTL_I18nDialogTitle");
        // open a java file in the editor
        editor = gui.Utilities.openJavaFile();
    }
    
    public void prepare() {
        // do nothing
   }
    
    public ComponentOperator open() {
        // invoke Tools / Internationalization / Internationalize from the main menu
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(MENU,"|");
        return new NbDialogOperator(TITLE);
    }
 
    public void shutdown(){
        if(editor!=null && editor.isShowing())
            editor.closeDiscard();
    }
    
}
