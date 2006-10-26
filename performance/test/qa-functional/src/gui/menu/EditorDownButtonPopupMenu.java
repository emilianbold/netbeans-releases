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

package gui.menu;

import org.netbeans.performance.test.guitracker.ActionTracker;

import org.netbeans.jellytools.EditorWindowOperator;

import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test List Of The Recent Opened Windows popup menu on Editor Window down button if 10 files opened
 * @author  juhrik@netbeans.org, mmirilovic@netbeans.org
 */
public class EditorDownButtonPopupMenu extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    /** Test of popup menu on Editor's 'Down Button' */
    public EditorDownButtonPopupMenu(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }
    
    /** Test of popup menu on Editor's 'Down Button' */
    public EditorDownButtonPopupMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }
    
    
    public void testEditorDownButtonPopupMenu(){
        doMeasurement();
    }
    
    public void initialize(){
        gui.Utilities.open10FilesFromJEdit();
    }
    
    public void prepare(){
        // do nothing
    }
    
    public ComponentOperator open(){
        EditorWindowOperator.btDown().clickForPopup();
        ComponentOperator popupComponent = new ComponentOperator(EditorWindowOperator.btDown().getContainer(ComponentSearcher.getTrueChooser("org.netbeans.core.windows.view.ui.RecentViewListDlg")));
        return popupComponent;
  }
    
    public void shutdown(){
        EditorWindowOperator.closeDiscard();
    }
    
}
