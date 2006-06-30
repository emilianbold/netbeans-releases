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

package org.netbeans.modules.diff.builtin.visualizer;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Iterator;

/**
 * Filter out popup menu triggering events.
 *
 * @author Petr Kuzel
 */
public class DEditorPane extends JEditorPane {

    private List popupActions;

    protected void processMouseEvent(MouseEvent e) {
        if (e.isPopupTrigger() == false) {
            super.processMouseEvent(e);
        } else {
            if (popupActions != null) {
                JPopupMenu popup = new JPopupMenu();
                Iterator it = popupActions.iterator();
                int actions = 0;
                while (it.hasNext()) {
                    Action action = (Action) it.next();
                    if (action == null) continue;
                    popup.add(action);
                    actions++;
                }
                if (actions > 0) {
                    // -7 "automaticaly" selects first menu option - well, in most cases
                    popup.show(e.getComponent(), e.getX() - 7, e.getY() - 10);
                }
            }
        }
    }

    public void setPopupActions(List actions) {
        popupActions = actions;
    }
}
