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

package gui.menu;

import org.netbeans.performance.test.guitracker.ActionTracker;
        
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test List Of The Recent Opened Windows popup menu on Editor Window down button if 10 files opened
 * @author  juhrik@netbeans.org, mmirilovic@netbeans.org
 */
public class EditorDownButtonPopupMenu extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static EditorWindowOperator editor;
    
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
        editor = new EditorWindowOperator();
    }
    
    public void prepare(){
        // do nothing
    }
    
    public ComponentOperator open(){
        editor.btDown().clickForPopup();
        ComponentOperator popupComponent = new ComponentOperator(editor.btDown().getContainer(ComponentSearcher.getTrueChooser("org.netbeans.core.windows.view.ui.RecentViewListDlg")));
        return popupComponent;
  }
    
    public void shutdown(){
        editor.closeDiscard();
    }
    
}
