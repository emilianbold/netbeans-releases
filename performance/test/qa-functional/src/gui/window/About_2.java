/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import org.netbeans.performance.test.guitracker.ActionTracker;

import org.netbeans.jellytools.NbDialogOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;

/**
 * Test of About dialog.
 *
 * @author  mmirilovic@netbeans.org
 */
public class About_2 extends About {
    NbDialogOperator about;
    
    /** Creates a new instance of About_2 */
    public About_2(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }
    
    /** Creates a new instance of About_2 */
    public About_2(String testName, String performanceDataName) {
        super(testName, performanceDataName);    
        expectedTime = WINDOW_OPEN;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }
    
    public void prepare(){
        about = (NbDialogOperator)super.open();
    }
    
    public ComponentOperator open(){
        new JTabbedPaneOperator(about).selectPage(DETAIL);
        return new NbDialogOperator("About");
    }
    
}
