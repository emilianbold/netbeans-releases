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

package org.netbeans.modules.applemenu;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * hack for issue #67799, on macosx with single button mouse,
 * make Ctrl-Click work as right click on multiselections
 * @author ttran
 */
public class CtrlClickHack implements AWTEventListener {

    /** Creates a new instance of CtrlClickHack */
    public CtrlClickHack() {
    }

    public void eventDispatched(AWTEvent e) {
        if (! (e instanceof MouseEvent)) {
            return;
        }
        MouseEvent evt = (MouseEvent) e;
        if (evt.getModifiers() != (InputEvent.BUTTON1_MASK | InputEvent.CTRL_MASK)) {
            return;
        }
        try {
            Field f1 = InputEvent.class.getDeclaredField("modifiers");
            Field f2 = MouseEvent.class.getDeclaredField("button");
            Method m = MouseEvent.class.getDeclaredMethod("setNewModifiers", new Class[] {});
            f1.setAccessible(true);
            f1.setInt(evt, InputEvent.BUTTON3_MASK);
            f2.setAccessible(true);
            f2.setInt(evt, MouseEvent.BUTTON3);
            m.setAccessible(true);
            m.invoke(evt, new Object[] {});
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
