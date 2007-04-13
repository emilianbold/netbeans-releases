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

package org.netbeans.modules.tasklist.ui;

import org.netbeans.modules.tasklist.ui.TaskListTopComponent;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;

/**
 * Action which shows TaskList component.
 */
public class TaskListAction extends CallableSystemAction {
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    public void performAction() {
        TopComponent win = TaskListTopComponent.findInstance();
        win.open();
        win.requestActive();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(TaskListAction.class, "CTL_TaskListAction"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx( TaskListAction.class );
    }

    @Override
    protected String iconResource() {
        return TaskListTopComponent.ICON_PATH;
    }
}
