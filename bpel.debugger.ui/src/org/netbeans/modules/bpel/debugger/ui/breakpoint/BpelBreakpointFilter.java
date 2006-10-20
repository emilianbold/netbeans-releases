/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.bpel.debugger.ui.breakpoint;

import javax.swing.Action;
import org.openide.util.NbBundle;

import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;

import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.10.27
 */
public class BpelBreakpointFilter implements NodeActionsProviderFilter {
    
    /**{@inheritDoc}*/
    public Action[] getActions (NodeActionsProvider original, Object object)
        throws UnknownTypeException
    {
        if ( !(object instanceof LineBreakpoint)) {
            return original.getActions (object);
        }
        Action[] actions = original.getActions (object);
        Action[] results = new Action [actions.length + 2];
        results[0] = GO_TO_SOURCE_ACTION;
        results[1] = null;

        for (int i=0; i < actions.length; i++) {
            results[i+2] = actions[i];
        }
        return results;
    }
    
    /**{@inheritDoc}*/
    public void performDefaultAction (NodeActionsProvider original, Object object)
        throws UnknownTypeException
    {
        if (object instanceof LineBreakpoint) {
            goToSource ((LineBreakpoint) object);
        }
        else {
            original.performDefaultAction (object);
        }
    }

    private void goToSource (LineBreakpoint breakpoint) {
        EditorContextBridge.showSource(
            breakpoint.getURL(),
            breakpoint.getXpath());
    }

    private final Action GO_TO_SOURCE_ACTION = Models.createAction (
        NbBundle.getMessage(
            BpelBreakpointFilter.class,
            "CTL_Breakpoint_Action_Go_to_Source"), // NOI18N
        new Models.ActionPerformer () {
            public boolean isEnabled (Object object) {
                return true;
            }
            public void perform (Object[] objects) {
                goToSource ((LineBreakpoint) objects [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
}
