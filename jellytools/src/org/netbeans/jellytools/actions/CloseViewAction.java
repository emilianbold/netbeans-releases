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

/**
 * Close view action (org.openide.actions.CloseViewAction).
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class CloseViewAction extends Action {
    
    
    /** "Close" popup menu item. */
    private static final String popupPath = Bundle.getStringTrimmed("org.openide.actions.Bundle",
                                                                    "CloseView");
    private static final Shortcut shortcut = new Shortcut(KeyEvent.VK_F4, KeyEvent.CTRL_MASK);
    
    /** Create new CloseViewAction instance. It doesn't have main menu
     * representation. */
    public CloseViewAction() {
        super(null, popupPath, "org.openide.actions.CloseViewAction", shortcut);
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