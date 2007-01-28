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

package org.netbeans.modules.visualweb.extension.openide.awt;

import java.awt.event.MouseEvent;

/** Originally copied from openide/src/org/openide/awt/MouseUtils,
 * to hack isDoubleClick method, there are some rave specific probs. */
public class MouseUtils_RAVE extends Object {
    private static int DOUBLE_CLICK_DELTA = 300;

    /** variable for double click */
    private static int tempx = 0;
    private static int tempy = 0;
    private static long temph = 0;
    private static int tempm = 0;
    // <RAVE>
    private static MouseEvent tempe;
    // </RAVE>


    // <RAVE>
    /** Provide access to the double click interval; this is used
     * by isDoubleClick for example
     */
    public static int getDoubleClickInterval() {
        return DOUBLE_CLICK_DELTA;
    }
    // </RAVE>



    /** Returns true if parametr is a 'doubleclick event'
    * @param e MouseEvent
    * @return true if the event is a doubleclick
    */
    public static boolean isDoubleClick(MouseEvent e) {
        // even number of clicks is considered like doubleclick
        // it works as well as 'normal testing against 2'
        // but on solaris finaly works and on Win32 works better
        //System.out.println ("Click COunt: "+e.getClickCount ()); // NOI18N
 // <RAVE>
 // If you don't do this, then if anyone calls isDoubleClick from
 // say a mouseReleased method, then the immediately following mouseClicked
 // method from a single mouse click will give isDoubleClick=true
        if (e.getID() != MouseEvent.MOUSE_CLICKED) {
            return false;
        }

        if (e.getClickCount() == 0) {
            return false;
        }
 // </RAVE>
        return (e.getClickCount () % 2 == 0) || isDoubleClickImpl(e);
    }

    /** Tests the positions.
    */
    private static boolean isDoubleClickImpl (MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        long h = e.getWhen();
        int m = e.getModifiers();
        //System.out.println ("When:: "+h); // NOI18N
        // same position at short time
        if (tempx == x && tempy == y && h - temph < DOUBLE_CLICK_DELTA &&
            // <RAVE>
            // Without this, calling isDoubleClick() twice on the same
            // mouse event will return true the second time!
            // I considered two fixes:
            // (1) checking that h != temph - e.g. if the buttons were
            // clicked -simultaneously- then it's probably just a second
            // call on the same mouse event. Do we have any users fast
            // enough to click the button so rapidly?
            // (2) checking that this is a different mouse event than
            // the last call. This will not work in the presence of
            // mutable events which are used in a few places in Swing,
            // but apparently not for mouse click events

            //h != temph &&
            e != tempe &&
            // </RAVE>

                m == tempm) {
            // OK forget all
            tempx = 0;
            tempy = 0;
            temph = 0;
            tempm = 0;
            // <RAVE>
            tempe = null;
            // </RAVE>
            return true;
        } else {
            // remember
            tempx = x;
            tempy = y;
            temph = h;
            tempm = m;
            // <RAVE>
            tempe = e;
            // </RAVE>
            return false;
        }
    }

}
