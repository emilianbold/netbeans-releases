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

package gui.menu;

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.ComponentOperator;


import java.awt.Component;
import java.awt.Container;
import javax.swing.JPopupMenu;

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
    }
    
    /** Creates a new instance of ValidatePopupMenuOnNodes */
    public ValidatePopupMenuOnNodes(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 300;
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
        measure_mouse_press = true;
        return dataObjectNode.callPopup();
    }
    
        /* it stopped to work after a while, see issue 58790
         *  
    private JPopupMenu callPopup(final org.netbeans.jemmy.operators.ComponentOperator oper, int x, int y, int mouseButton) {
        org.netbeans.jemmy.operators.JPopupMenuOperator popup_op = dataObjectNode.callPopup();
        return (JPopupMenu) popup_op.getSource();
        
        // oper.clickForPopup(x, y, mouseButton);
        
        // oper.clickForPopup -> avoid makeVisible
        // oper.clickMouse(x, y, 1, mouseButton, 0, true);
        try {
//            java.awt.Robot r = new java.awt.Robot();
//            r.mouseMove(oper.getSource().getLocationOnScreen().x+x, oper.getSource().getLocationOnScreen().y+y);
//            r.mousePress(java.awt.event.InputEvent.BUTTON3_MASK);
//            r.mouseRelease(java.awt.event.InputEvent.BUTTON3_MASK);
        }
        catch (Exception ex) {
            throw new Error(ex);
        }
        
        oper.getTimeouts().sleep("JMenuOperator.WaitBeforePopupTimeout");
        return(JPopupMenuOperator.waitJPopupMenu(JPopupMenuOperator.waitJPopupWindow(new org.netbeans.jemmy.ComponentChooser() {
            public boolean checkComponent(Component cmp) {
                Component invoker = ((JPopupMenu)cmp).getInvoker();
                return(invoker == oper.getSource() ||
                (invoker instanceof Container &&
                ((Container)invoker).isAncestorOf(oper.getSource())) ||
                (oper.getSource() instanceof Container &&
                ((Container)oper.getSource()).isAncestorOf(invoker)));
            }
            public String getDescription() {
                return("Popup menu");
                
            }
        }),
        org.netbeans.jemmy.ComponentSearcher.getTrueChooser("Popup menu")));
    }
         */
    
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
