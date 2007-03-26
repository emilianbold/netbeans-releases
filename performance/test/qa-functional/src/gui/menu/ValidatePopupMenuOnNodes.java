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

package gui.menu;

import org.netbeans.performance.test.guitracker.ActionTracker;

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Common test case for test of context menu invocation on various nodes in the tree views.
 * @author mmirilovic@netbeans.org
 */
public abstract class ValidatePopupMenuOnNodes extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    protected static Node dataObjectNode;


    /** Creates a new instance of ValidatePopupMenuOnNodes */
    public ValidatePopupMenuOnNodes(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 300;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }
    
    /** Creates a new instance of ValidatePopupMenuOnNodes */
    public ValidatePopupMenuOnNodes(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 300;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }
    
    /**
     * Selects node whose popup menu will be tested.
     */
    public void prepare() {
        dataObjectNode.select();
    }
    
    /**
     * Directly sends mouse events causing popup menu displaying to the selected node.
     * <p>Using Jemmy/Jelly to call popup can cause reselecting of node and more events
     * than is desirable for this case.
     */
    public ComponentOperator open(){
        /* it stopped to work after a while, see issue 58790
        java.awt.Point p = dataObjectNode.tree().getPointToClick(dataObjectNode.getTreePath());
        JPopupMenu menu = callPopup(dataObjectNode.tree(), p.x, p.y, java.awt.event.InputEvent.BUTTON3_MASK);
        return new JPopupMenuOperator(menu);
         */
        
        java.awt.Point point = dataObjectNode.tree().getPointToClick(dataObjectNode.getTreePath());
        int button = dataObjectNode.tree().getPopupMouseButton();
        dataObjectNode.tree().clickMouse(point.x, point.y, 1, button);
        return new JPopupMenuOperator();
    }
    
    /**
     * Closes the popup by sending ESC key event.
     */
    public void close(){
        //testedComponentOperator.pressKey(java.awt.event.KeyEvent.VK_ESCAPE);
        // Above sometimes fails in QUEUE mode waiting to menu become visible.
        // This pushes Escape on underlying JTree which should be always visible
        dataObjectNode.tree().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }
    
}
