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

package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/** Used to call "Restore Window" popup menu item, "Window|Restore Window" main menu item,
 * shortcut CTRL+Back Quote (CTRL+`) or restore window by IDE API.
 * @see Action
 * @see org.netbeans.jellytools.TopComponentOperator
 * @author Jiri.Skrivanek@sun.com
 */
public class RestoreWindowAction extends Action {
    
    /** "Window" main menu item. */
    private static final String windowItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                                                                     "Menu/Window");
    
    /** "Window|Restore Window" */
    private static final String windowRestorePath = windowItem+"|"+
                Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                        "CTL_UnmaximizeWindowAction");

    /** "Restore Window" */
    private static final String popupPath = Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                                                    "CTL_UnmaximizeWindowAction");
    
    private static final Shortcut restoreShortcut = new Shortcut(KeyEvent.VK_BACK_QUOTE, KeyEvent.CTRL_MASK);


    /** Creates new instance of RestoreWindowAction. */
    public RestoreWindowAction() {
        super(windowRestorePath, popupPath, restoreShortcut);
    }

    
    /** Performs popup action Restore Window on given component operator 
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param compOperator operator which should be activated and restored
     */
    public void performPopup(ComponentOperator compOperator) {
        if(compOperator instanceof TopComponentOperator) {
            performPopup((TopComponentOperator)compOperator);
        } else {
            throw new UnsupportedOperationException(
                    "RestoreWindowAction can only be called on TopComponentOperator.");
        }
    }
    
    /** Performs popup action Restore Window on given top component operator 
     * which is activated before the action.
     * @param tco top component operator which should be activated and maximized
     */
    public void performPopup(TopComponentOperator tco) {
        tco.pushMenuOnTab(popupPath);
    }
    
    /** Restore active top component by IDE API.*/
    public void performAPI() {
        WindowManagerImpl.getInstance().setMaximizedMode(null);
    }

    /** Throws UnsupportedOperationException because RestoreWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
            "RestoreWindowAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because RestoreWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
            "RestoreWindowAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because RestoreWindowAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
            "RestoreWindowAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because RestoreWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
            "RestoreWindowAction doesn't have popup representation on nodes.");
    }
}