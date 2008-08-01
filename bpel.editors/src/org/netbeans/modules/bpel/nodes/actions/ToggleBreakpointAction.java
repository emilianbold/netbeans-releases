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
package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.bpel.nodes.actions.BpelNodeAction;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import java.util.concurrent.Callable;
import javax.swing.KeyStroke;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.nodes.navigator.Util;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Alexander Zgursky
 */
public class ToggleBreakpointAction extends BpelNodeAction {
    private static final long serialVersionUID = 1L;
    public static final String TOGGLE_BREAKPOINT_KEYSTROKE = 
            NbBundle.getMessage(
            ToggleBreakpointAction.class,"ACT_ToggleBreakpointAction");// NOI18N
    
    public ToggleBreakpointAction() {
        super();
        putValue(ToggleBreakpointAction.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(TOGGLE_BREAKPOINT_KEYSTROKE));
    }
    
    protected String getBundleName() {
        return NbBundle.getMessage(getClass(), "CTL_ToggleBreakpointAction"); // NOI18N
    }
    
    public ActionType getType() {
        return ActionType.TOGGLE_BREAKPOINT;
    }
    
    @Override
    public boolean isChangeAction() {
        return false;
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
        DebuggerManager.getDebuggerManager().getActionsManager().doAction(
                ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }
}
