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

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.ui.Evaluator;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Invokes the expression evaluator GUI
 *
 * @author Martin Entlicher
 */
public class EvaluateAction extends AbstractAction implements PropertyChangeListener,
                                                              Runnable {
    
    private EnableListener listener;
    private transient Reference lastDebuggerRef = new WeakReference(null);

    public EvaluateAction () {
        listener = new EnableListener (this);
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_CURRENT_ENGINE,
                listener);
        putValue (
            Action.NAME, 
            NbBundle.getMessage(EvaluateAction.class, "CTL_Evaluate") // NOI18N
        );
        checkEnabled();
    }
    
    private synchronized boolean canBeEnabled() {
        DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (de == null) return false;
        JPDADebugger debugger = de.lookupFirst(null, JPDADebugger.class);
        JPDADebugger lastDebugger = (JPDADebugger) lastDebuggerRef.get();
        if (lastDebugger != null && debugger != lastDebugger) {
            lastDebugger.removePropertyChangeListener(
                    JPDADebugger.PROP_CURRENT_THREAD,
                    this);
            lastDebuggerRef = new WeakReference(null);
        }
        if (debugger != null) {
            lastDebuggerRef = new WeakReference(debugger);
            debugger.addPropertyChangeListener(
                    JPDADebugger.PROP_CURRENT_THREAD,
                    this);
            return (debugger.getCurrentThread() != null);
        } else {
            return false;
        }
    }
    
    private void checkEnabled() {
        SwingUtilities.invokeLater(this);
    }
    
    public void run() {
        setEnabled(canBeEnabled());
    }
    
    public void actionPerformed (ActionEvent evt) {
        DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (de == null) return ;
        JPDADebugger debugger = de.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) return ;
        Evaluator.open(debugger);
    }
    
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (this) {
                    JPDADebugger lastDebugger = (JPDADebugger) lastDebuggerRef.get();
                    if (lastDebugger != null) {
                        setEnabled(lastDebugger.getCurrentThread() != null);
                    }
                }
            }
        });
    }
    
    protected void finalize() throws Throwable {
        DebuggerManager.getDebuggerManager().removeDebuggerListener(
                DebuggerManager.PROP_CURRENT_ENGINE,
                listener);
    }

        
    private static class EnableListener extends DebuggerManagerAdapter {
        
        private Reference actionRef;
        
        public EnableListener(EvaluateAction action) {
            actionRef = new WeakReference(action);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            EvaluateAction action = (EvaluateAction) actionRef.get();
            if (action != null) {
                action.checkEnabled();
            }
        }
        
    }

}
