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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.InvalidRequestStateException;
import com.sun.jdi.request.StepRequest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
//import org.netbeans.modules.debugger.jpda.JPDAStepImpl.SingleThreadedStepWatch;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.StepRequestWrapper;
import org.netbeans.spi.debugger.ActionsProviderSupport;

import org.openide.util.WeakSet;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
* @author  Marian Petras
*/
abstract class JPDADebuggerActionProvider extends ActionsProviderSupport 
implements PropertyChangeListener {
    
    protected JPDADebuggerImpl debugger;
    
    private static Set<JPDADebuggerActionProvider> providersToDisableOnLazyActions = new WeakSet<JPDADebuggerActionProvider>();
    
    private volatile boolean disabled;
    
    JPDADebuggerActionProvider (JPDADebuggerImpl debugger) {
        this.debugger = debugger;
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        checkEnabled (debugger.getState ());
    }
    
    protected abstract void checkEnabled (int debuggerState);
    
    @Override
    public boolean isEnabled (Object action) {
        if (!disabled) {
            checkEnabled (debugger.getState ());
        }
        return super.isEnabled (action);
    }
    
    JPDADebuggerImpl getDebuggerImpl () {
        return debugger;
    }
    
    protected void removeStepRequests (ThreadReference tr) {
        removeStepRequests(getDebuggerImpl(), tr);
    }

    static void removeStepRequests(JPDADebuggerImpl debugger, ThreadReference tr) {
        //S ystem.out.println ("removeStepRequests");
        try {
            VirtualMachine vm = debugger.getVirtualMachine ();
            if (vm == null) return;
            EventRequestManager erm = VirtualMachineWrapper.eventRequestManager (vm);
            List<StepRequest> l = EventRequestManagerWrapper.stepRequests (erm);
            Iterator<StepRequest> it = l.iterator ();
            while (it.hasNext ()) {
                StepRequest stepRequest = it.next ();
                if (StepRequestWrapper.thread(stepRequest).equals (tr)) {
                    //S ystem.out.println("  remove request " + stepRequest);
                    EventRequestManagerWrapper.deleteEventRequest (erm, stepRequest);
                    //SingleThreadedStepWatch.stepRequestDeleted(stepRequest);
                    debugger.getOperator().unregister(stepRequest);
                    break;
                }
                //S ystem.out.println("  do not remove " + stepRequest + " : " + stepRequest.thread ());
            }
        } catch (VMDisconnectedExceptionWrapper e) {
        } catch (InternalExceptionWrapper e) {
        } catch (IllegalThreadStateException e) {
            e.printStackTrace();
        } catch (InvalidRequestStateException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Mark the provided action provider to be disabled when a lazy action is to be performed.
     */
    protected final void setProviderToDisableOnLazyAction(JPDADebuggerActionProvider provider) {
        synchronized (JPDADebuggerActionProvider.class) {
            providersToDisableOnLazyActions.add(provider);
        }
    }
    
    /**
     * Do the action lazily in a RequestProcessor.
     * @param run The action to perform.
     */
    protected final void doLazyAction(final Runnable run) {
        final Set<JPDADebuggerActionProvider> disabledActions;
        synchronized (JPDADebuggerActionProvider.class) {
            disabledActions = new HashSet<JPDADebuggerActionProvider>(providersToDisableOnLazyActions);
        }
        for (Iterator<JPDADebuggerActionProvider> it = disabledActions.iterator(); it.hasNext(); ) {
            JPDADebuggerActionProvider ap = it.next();
            Set actions = ap.getActions();
            ap.disabled = true;
            for (Iterator ait = actions.iterator(); ait.hasNext(); ) {
                Object action = ait.next();
                ap.setEnabled (action, false);
                //System.out.println(ap+".setEnabled("+action+", "+false+")");
            }
        }
        debugger.getRequestProcessor().post(new Runnable() {
            public void run() {
                try {
                    run.run();
                    for (Iterator<JPDADebuggerActionProvider> it = disabledActions.iterator(); it.hasNext(); ) {
                        JPDADebuggerActionProvider ap = it.next();
                        Set actions = ap.getActions();
                        ap.disabled = false;
                        ap.checkEnabled (debugger.getState ());
                    }
                } catch (com.sun.jdi.VMDisconnectedException e) {
                    // Causes kill action when something is being evaluated
                }
            }
        });
    }
}
