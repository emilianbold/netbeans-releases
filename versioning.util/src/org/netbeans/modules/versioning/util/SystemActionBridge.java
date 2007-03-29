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

package org.netbeans.modules.versioning.util;

import org.openide.util.actions.SystemAction;
import org.openide.util.Lookup;
import org.openide.util.ContextAwareAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Converts NetBeans {@link SystemAction} to Swing's {@link Action}.
 *
 * @author Maros Sandor
 */
public class SystemActionBridge extends AbstractAction {

    private Action action;

    public static SystemActionBridge createAction(Action action, String name, Lookup context) {
        if (context != null && action instanceof ContextAwareAction) {
            action = ((ContextAwareAction) action).createContextAwareInstance(context);
        }
        return new SystemActionBridge(action, name);
    }

    public SystemActionBridge(Action action, String name) {
        super(name, null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        this.action = action;
    }

    public void actionPerformed(ActionEvent e) {
        action.actionPerformed(e);
    }

    public boolean isEnabled() {
        return action.isEnabled();
    }
}
