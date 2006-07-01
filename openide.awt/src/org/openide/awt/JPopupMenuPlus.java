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
