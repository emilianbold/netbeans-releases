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
package org.openide.awt;

import java.awt.Component;
import java.awt.Point;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;


/** A subclass of JPopupMenu which ensures that the popup menus do
 * not stretch off the edges of the screen.
 * @deprecated - doesn't do anything special anymore. (since org.openide.awt 6.5)
 */
public class JPopupMenuPlus extends JPopupMenu {
//    private static final boolean NO_POPUP_PLACEMENT_HACK = Boolean.getBoolean("netbeans.popup.no_hack"); // NOI18N

    public JPopupMenuPlus() {
    }

//    /*
//     * Override the show() method to ensure that the popup will be
//     * on the screen.
//     */
//    public void show(Component invoker, int x, int y) {
//        if (isVisible()) {
//            return;
//        }
//
////        // HACK[pnejedly]: Notify all the items in the menu we're going to show
////        JInlineMenu.prepareItemsInContainer(this);
//
//        // End of HACK
//        if (NO_POPUP_PLACEMENT_HACK) {
//            super.show(invoker, x, y);
//
//            return;
//        }
//
//        Point p = new Point(x, y);
//        SwingUtilities.convertPointToScreen(p, invoker);
//
//        Point newPt = JPopupMenuUtils.getPopupMenuOrigin(this, p);
//        SwingUtilities.convertPointFromScreen(newPt, invoker);
//        super.show(invoker, newPt.x, newPt.y);
//    }
}
