/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/** Used to call "Close Window" popup menu item, "Window|Close Window" 
 * main menu, "org.openide.actions.CloseViewAction" or Ctrl+F4 shortcut.
 * @see Action
 * @author Jiri.Skrivanek@sun.com
 */
public class CloseViewAction extends Action {
    
    /** Window main menu item. */
    private static final String windowItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", 
                                                                    "Menu/Window");
    /** "Close Window" popup menu item. */
    private static final String popupPath = Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                                                    "LBL_CloseWindowAction");
    /** "Windows|Close Window" main menu item */
    private static final String menuPath = windowItem+"|"+
                            Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                                    "CTL_CloseWindowAction");
    private static final Shortcut shortcut = new Shortcut(KeyEvent.VK_F4, KeyEvent.CTRL_MASK);
    
    /** Create new CloseViewAction instance. */
    public CloseViewAction() {
        super(menuPath, popupPath, "org.openide.actions.CloseViewAction", shortcut);
    }

    /** Performs popup action Close Window on given component operator 
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param compOperator operator which should be activated and closed
     */
    public void performPopup(ComponentOperator compOperator) {
        if(compOperator instanceof TopComponentOperator) {
            performPopup((TopComponentOperator)compOperator);
        } else {
            throw new UnsupportedOperationException(
                    "CloseViewAction can only be called on TopComponentOperator.");
        }
    }

    /** Performs popup action Close Window on given top component operator 
     * which is activated before the action.
     * @param tco operator which should be activated and closed
     */
    public void performPopup(TopComponentOperator tco) {
        tco.pushMenuOnTab(popupPath);
    }
    
    /** Throws UnsupportedOperationException because CloseViewAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloseViewAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because CloseViewAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloseViewAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because CloseViewAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloseViewAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because CloseViewAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloseViewAction doesn't have popup representation on nodes.");
    }
    
}