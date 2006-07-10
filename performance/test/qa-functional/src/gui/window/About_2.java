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
