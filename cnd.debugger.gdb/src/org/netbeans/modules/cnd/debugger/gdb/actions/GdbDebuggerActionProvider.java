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

package org.netbeans.modules.cnd.debugger.gdb.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.WeakSet;

/**
* Representation of a debugging session.
*
* @author  Gordon Prieur (copied from Jan Jancura's and Marian Petras' JPDA implementation)
*/
abstract class GdbDebuggerActionProvider extends ActionsProviderSupport 
                implements PropertyChangeListener {
    
    private final GdbDebugger debugger;
    
    private static Set<GdbDebuggerActionProvider> providersToDisableOnLazyActions 
            = new WeakSet<GdbDebuggerActionProvider>();

    private volatile boolean disabled;

    GdbDebuggerActionProvider(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, GdbDebugger.class);
        debugger.addPropertyChangeListener(this);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String pname = evt.getPropertyName();
        if (pname.equals(GdbDebugger.PROP_STATE) ||
                pname.equals(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME) ||
                pname.equals(GdbDebugger.PROP_CURRENT_THREAD)) {
            checkEnabled(debugger.getState());
        } 
    }
    
    protected abstract void checkEnabled(GdbDebugger.State debuggerState);
    
    @Override
    public boolean isEnabled(Object action) {
        if (!disabled) {
            checkEnabled(debugger.getState());
        }
        return super.isEnabled(action);
    }
    
    GdbDebugger getDebugger() {
        return debugger;
    }

    /**
     * Mark the provided action provider to be disabled when a lazy action is to be performed.
     */
    protected final void setProviderToDisableOnLazyAction(GdbDebuggerActionProvider provider) {
        synchronized (GdbDebuggerActionProvider.class) {
            providersToDisableOnLazyActions.add(provider);
        }
    }
    
    /**
     * Do the action lazily in a RequestProcessor.
     * @param run The action to perform.
     */
    protected final void doLazyAction(final Runnable run) {
        final Set<GdbDebuggerActionProvider> disabledActions;
        synchronized (GdbDebuggerActionProvider.class) {
            disabledActions = new HashSet<GdbDebuggerActionProvider>(providersToDisableOnLazyActions);
        }
        for (Iterator<GdbDebuggerActionProvider> it = disabledActions.iterator(); it.hasNext(); ) {
            GdbDebuggerActionProvider ap = it.next();
            Set actions = ap.getActions();
            ap.disabled = true;
            for (Iterator ait = actions.iterator(); ait.hasNext(); ) {
                Object action = ait.next();
                ap.setEnabled(action, false);
                //System.out.println(ap+".setEnabled("+action+", "+false+")");
            }
        }
        debugger.getRequestProcessor().post(new Runnable() {
            public void run() {
//                try {
                    run.run();
                    for (Iterator<GdbDebuggerActionProvider> it = disabledActions.iterator(); it.hasNext(); ) {
                        GdbDebuggerActionProvider ap = it.next();
                        Set actions = ap.getActions();
                        ap.disabled = false;
                        ap.checkEnabled(debugger.getState ());
                    }
//                } catch (Exception e) {
//                    // Causes kill action when something is being evaluated
//                }
            }
        });
    }
}
