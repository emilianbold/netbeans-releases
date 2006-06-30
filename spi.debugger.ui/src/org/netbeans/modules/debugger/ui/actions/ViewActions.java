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


package org.netbeans.modules.debugger.ui.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.AbstractAction;

import org.netbeans.modules.debugger.ui.Utils;
import org.netbeans.modules.debugger.ui.views.View;

import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Opens View TopComponent.
 *
 * @author Jan Jancura, Martin Entlicher
 */
public class ViewActions extends AbstractAction {
    
    private String viewName;

    private ViewActions (String viewName) {
        this.viewName = viewName;
    }

    public Object getValue(String key) {
        if (key == Action.NAME) {
            return NbBundle.getMessage (ViewActions.class, (String) super.getValue(key));
        }
        Object value = super.getValue(key);
        if (key == Action.SMALL_ICON) {
            if (value instanceof String) {
                value = Utils.getIcon ((String) value);
            }
        }
        return value;
    }
    
    public void actionPerformed (ActionEvent evt) {
        openComponent (viewName, true);
    }
    
    static void openComponent (String viewName, boolean activate) {
        TopComponent view = WindowManager.getDefault().findTopComponent(viewName);
        if (view == null) {
            throw new IllegalArgumentException(viewName);
        }
        view.open();
        if (activate) {
            view.requestActive();
        }
    }
    
    
    /**
     * Creates an action that opens Breakpoints TopComponent.
     */
    public static Action createBreakpointsViewAction () {
        ViewActions action = new ViewActions("breakpointsView");
        action.putValue (Action.NAME, "CTL_BreakpointsAction");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint" // NOI18N
        );
        return action;
    }

    /**
     * Creates an action that opens Call Stack TopComponent.
     */
    public static Action createCallStackViewAction () {
        ViewActions action = new ViewActions("callstackView");
        action.putValue (Action.NAME, "CTL_CallStackAction");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/callStackView/NonCurrentFrame" // NOI18N
        );
        return action;
    }

    /**
     * Creates an action that opens Local Variables TopComponent.
     */
    public static Action createLocalsViewAction() {
        ViewActions action = new ViewActions("localsView");
        action.putValue (Action.NAME, "CTL_LocalVariablesAction");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/localsView/LocalVariable" // NOI18N
        );
        return action;
    }

    /**
     * Creates an action that opens Sessions TopComponent.
     */
    public static Action createSessionsViewAction () {
        ViewActions action = new ViewActions("sessionsView");
        action.putValue (Action.NAME, "CTL_SessionsAction");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/sessionsView/Session" // NOI18N
        );
        return action;
    }

    /**
     * Creates an action that opens Threads TopComponent.
     */
    public static Action createThreadsViewAction () {
        ViewActions action = new ViewActions("threadsView");
        action.putValue (Action.NAME, "CTL_ThreadsAction");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/threadsView/ThreadGroup" // NOI18N
        );
        return action;
    }
    
    
    /**
     * Creates an action that opens Watches TopComponent.
     */
    public static Action createWatchesViewAction() {
        ViewActions action = new ViewActions("watchesView");
        action.putValue (Action.NAME, "CTL_WatchesAction");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/watchesView/Watch" // NOI18N
        );
        return action;
    }

}

