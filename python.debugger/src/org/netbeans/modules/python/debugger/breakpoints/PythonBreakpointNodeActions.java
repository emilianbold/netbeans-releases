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

package org.netbeans.modules.python.debugger.breakpoints;

import javax.swing.Action;
import org.netbeans.modules.python.debugger.Utils;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.ActionPerformer;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Provides actions for nodes representing {@link PythonBreakpoint} in the
 * Breapoint view.
 *
 * @authorJean-Yves Mengant
 */
public final class PythonBreakpointNodeActions implements NodeActionsProviderFilter {
    
    private static final Action GO_TO_SOURCE_ACTION;
    
    static {
        String name = "GoTo Source" ;
        ActionPerformer ap = new ActionPerformer() {
            public boolean isEnabled(Object node) { return true; }
            public void perform(Object[] nodes) {
                PythonBreakpoint bp = (PythonBreakpoint) nodes[0];
                Utils.showLine(Utils.getLine(bp.getFilePath(), bp.getLineNumber() - 1));
            }
        };
        GO_TO_SOURCE_ACTION = Models.createAction(name, ap, Models.MULTISELECTION_TYPE_EXACTLY_ONE);
    }
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof PythonBreakpoint) {
            PythonBreakpoint bp = (PythonBreakpoint) node;
            Utils.showLine(Utils.getLine(bp.getFilePath(), bp.getLineNumber() - 1));
        } else {
            original.performDefaultAction(node);
        }
    }
    
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        Action[] origActions = original.getActions(node);
        if (node instanceof PythonBreakpoint) {
            Action[] actions = new Action[origActions.length + 2];
            actions[0] = GO_TO_SOURCE_ACTION;
            actions[1] = null;
            System.arraycopy(origActions, 0, actions, 2, origActions.length);
            return actions;
        } else {
            return origActions;
        }
    }
    
}
