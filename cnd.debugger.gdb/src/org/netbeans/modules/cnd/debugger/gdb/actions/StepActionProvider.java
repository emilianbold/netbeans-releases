/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * StepActionProvider.java
 *
 * Created on July 11, 2006, 2:25 PM
 */
package org.netbeans.modules.cnd.debugger.gdb.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.RequestProcessor;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.modules.cnd.debugger.gdb.CallStackFrame;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.disassembly.Disassembly;

/**
 * Implements non visual part of stepping through code in gdb debugger.
 * It supports standard debugging actions Continue, Step Into, Step Over, 
 * Step Out, and RunToCursor (the last two are not implemented yet). 
 * 
 */
public class StepActionProvider extends GdbDebuggerActionProvider {
    
    private final Set actions  = new HashSet<Object>(Arrays.asList(new Object[] {
            ActionsManager.ACTION_STEP_INTO,
            ActionsManager.ACTION_STEP_OUT,
            ActionsManager.ACTION_STEP_OVER,
            ActionsManager.ACTION_CONTINUE,
            ActionsManager.ACTION_RUN_TO_CURSOR
        }));
    
    /** 
     * Creates a new instance of StepActionProvider
     *
     * @param lookupProvider a context provider
     */
    public StepActionProvider(ContextProvider lookupProvider) {
        super(lookupProvider);
    }
    
    // ActionProviderSupport ...................................................
    
    /**
     * Returns set of actions supported by this ActionsProvider.
     *
     * @return set of actions supported by this ActionsProvider
     */
    public Set getActions() {
        return actions;
    }

    /**
     * Called when the action is called (action button is pressed).
     *
     * @param action an action which has been called
     */
    public void doAction(Object action) {
        runAction(action);
    }
    
    /**
     * Runs the action. This method invokes the appropriate method in GdbDebugger
     * 
     * @param action an action which has been called
     */    
    public void runAction(final Object action) {
        if (getDebugger() != null) {
            synchronized (getDebugger().LOCK) {
                if (action == ActionsManager.ACTION_STEP_INTO) {
                    if (Disassembly.isInDisasm()) {
                        getDebugger().stepI();
                    } else {
                        getDebugger().stepInto();
                    }
                    return;
                }
                if (action == ActionsManager.ACTION_STEP_OUT) {
                    getDebugger().stepOut();
                    return;
                }
                if (action == ActionsManager.ACTION_STEP_OVER) {
                    if (Disassembly.isInDisasm()) {
                        getDebugger().stepOverInstr();
                    } else {
                        getDebugger().stepOver();
                    }
                    return;
                }
                if (action == ActionsManager.ACTION_CONTINUE) {
                    getDebugger().resume();
                    return;
                }
                if (action == ActionsManager.ACTION_RUN_TO_CURSOR) {
                    getDebugger().runToCursor();
                    return;
                }
            }
        }
    }
    
    /**
     * Post the action and let it process asynchronously.
     * The default implementation just delegates to {@link #doAction}
     * in a separate thread and returns immediately.
     *
     * @param action The action to post
     * @param actionPerformedNotifier run this notifier after the action is
     *        done.
     * @since 1.5
     */
    @Override
    public void postAction(final Object action, final Runnable actionPerformedNotifier) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    doAction(action);
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }
    
    protected void checkEnabled(GdbDebugger.State debuggerState) {
        boolean enabled = debuggerState == GdbDebugger.State.STOPPED;
        for (Object action : getActions()) {
            if (action == ActionsManager.ACTION_STEP_OUT) {
                setEnabled(action, enabled && isStepOutValid());
            } else {
                setEnabled(action, enabled);
            }
        }
    }

    private boolean isStepOutValid() {
        List<CallStackFrame> callstack = getDebugger().getCallStack();
        return callstack.size() == 1 ||
                (callstack.size() > 1 && callstack.get(1).isValid());
    }
}
