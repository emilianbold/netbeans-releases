/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.actions;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventDispatcher;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;

/** Used to call "Clone View" popup menu item, "Window|Clone View" main menu item or
 * "org.openide.actions.CloneViewAction".
 * @see Action
 * @see org.netbeans.jellytools.TopComponentOperator
 * @author Jiri.Skrivanek@sun.com
 */
public class CloneViewAction extends Action {
    

    /** "Clone View" main menu item. */
    private  static final String menuPath = Bundle.getStringTrimmed("org.netbeans.core.Bundle", 
                                                                    "Menu/Window")
                                            + "|"
                                            + Bundle.getStringTrimmed("org.openide.actions.Bundle", 
                                                                      "CloneView");
    /** "Clone View" popup menu item. */
    private static final String popupPath = Bundle.getStringTrimmed("org.openide.actions.Bundle", 
                                                                    "CloneView");
    
    /** Create new CloneViewAction instance. */
    public CloneViewAction() {
        super(menuPath, popupPath, "org.openide.actions.CloneViewAction");
    }
    
    /** Perform popup action on selected tab from given JTabbedPaneOperator.
     * @param tabbedPaneOperator JTabbedPaneOperator instance on which perform
     * the action
     */
    public void performPopup(JTabbedPaneOperator tabbedPaneOperator) {
        int index = tabbedPaneOperator.getSelectedIndex();
        Rectangle rc = tabbedPaneOperator.getBoundsAt(index);
        // make rectagle visible in MDI
        Object[] params = {rc};
        Class[] pClasses = {Rectangle.class};
        new EventDispatcher(tabbedPaneOperator.getSource()).invokeExistingMethod(
                "scrollRectToVisible",
                params,
                pClasses,
                JemmyProperties.getCurrentOutput());
        // open popup
        tabbedPaneOperator.clickForPopup(rc.x, rc.y);
        new JPopupMenuOperator().pushMenu(popupPath, "|");
    }
    
    /** Throws UnsupportedOperationException because CloneViewAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloneViewAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because CloneViewAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloneViewAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because CloneViewAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloneViewAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because CloneViewAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloneViewAction doesn't have popup representation on nodes.");
    }
    
}