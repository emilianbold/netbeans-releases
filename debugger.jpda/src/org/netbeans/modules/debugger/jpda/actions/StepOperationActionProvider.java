/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.debugger.jpda.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.debugger.ActionsProviderSupport;


/**
 *
 * @author  Martin Entlicher
 */
public class StepOperationActionProvider extends ActionsProviderSupport
                                       implements PropertyChangeListener,
                                                  ActionsManagerListener {

    private JPDADebugger debugger;
    private ActionsManager lastActionsManager;
    
    
    public StepOperationActionProvider (ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener (JPDADebugger.PROP_STATE, this);
    }
    
    private void destroy () {
        debugger.removePropertyChangeListener (JPDADebugger.PROP_STATE, this);
    }
    
    static ActionsManager getCurrentActionsManager () {
        return DebuggerManager.getDebuggerManager ().
            getCurrentEngine () == null ? 
            DebuggerManager.getDebuggerManager ().getActionsManager () :
            DebuggerManager.getDebuggerManager ().getCurrentEngine ().
                getActionsManager ();
    }
    
    private ActionsManager getActionsManager () {
        ActionsManager current = getCurrentActionsManager();
        if (current != lastActionsManager) {
            if (lastActionsManager != null) {
                lastActionsManager.removeActionsManagerListener(
                        ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            }
            current.addActionsManagerListener(
                    ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            lastActionsManager = current;
        }
        return current;
    }

    public void propertyChange (PropertyChangeEvent evt) {
        setEnabled (
            ActionsManager.ACTION_STEP_OPERATION,
            getActionsManager().isEnabled(ActionsManager.ACTION_CONTINUE) &&
            (debugger.getState () == JPDADebugger.STATE_STOPPED)  &&
            (debugger.getCurrentThread () != null)
        );
        if (debugger.getState () == JPDADebugger.STATE_DISCONNECTED) 
            destroy ();
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_STEP_OPERATION);
    }
    
    public void doAction (Object action) {
        doAction(debugger, null);
    }

    static void doAction (JPDADebugger debugger, PropertyChangeListener listener) {
        JPDAStep step = debugger.createJPDAStep(JPDAStep.STEP_OPERATION, JPDAStep.STEP_OVER);
        step.addStep(debugger.getCurrentThread());
        if (listener != null) {
            step.addPropertyChangeListener(listener);
        }
        if (debugger.getSuspend() == JPDADebugger.SUSPEND_EVENT_THREAD) {
            //debugger.getCurrentThread().resume();
            ((JPDADebuggerImpl) debugger).resumeCurrentThread();
        } else {
            ((JPDADebuggerImpl) debugger).resume();
        }
    }

    public void actionPerformed(Object action) {
        // Is never called
    }

    /** Sync up with continue action state. */
    public void actionStateChanged(Object action, boolean enabled) {
        if (ActionsManager.ACTION_CONTINUE == action) {
            setEnabled (
                ActionsManager.ACTION_STEP_OPERATION,
                enabled &&
                (debugger.getState () == JPDADebugger.STATE_STOPPED)  &&
                (debugger.getCurrentThread () != null)
            );
        }
    }
}
