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


package org.netbeans.modules.bpel.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.bpel.debugger.*;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.breakpoints.BpelBreakpoint;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * Listener for breakpoint events from NB Debugger Framework.
 *
 * @author Sun Microsystems
 */
public class BreakpointsEngineListener
        extends LazyActionsManagerListener 
        implements PropertyChangeListener, DebuggerManagerListener
{
    private BpelDebuggerImpl myDebugger;
    private boolean myIsStarted;
    
    
    /**
     * Creates a new instance of BreakpointsEngineListener.
     *
     * @param lookupProvider debugger engine context
     */
    public BreakpointsEngineListener(ContextProvider lookupProvider) {
        myDebugger = (BpelDebuggerImpl)lookupProvider.lookupFirst 
                (null, BpelDebugger.class);
    
        myDebugger.addPropertyChangeListener(BpelDebugger.PROP_STATE, this);
    }
    
    protected void destroy() {
        myDebugger.removePropertyChangeListener(BpelDebugger.PROP_STATE, this);
        DebuggerManager.getDebuggerManager().removeDebuggerListener(
                DebuggerManager.PROP_BREAKPOINTS, this);
    }

    /** {@inheritDoc} */
    public String[] getProperties() {
        return new String[] {"asd"};    //NOI18N
    }

    /** {@inheritDoc} */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == myDebugger ) {
            if (myDebugger.getState() == BpelDebugger.STATE_RUNNING) {
                if (myIsStarted) {
                    return;
                }
                myIsStarted = true;
                DebuggerManager.getDebuggerManager().addDebuggerListener(
                    DebuggerManager.PROP_BREAKPOINTS, this);
                //TODO:why do we need a PROP_CURRENT_SESSION listener?
                DebuggerManager.getDebuggerManager().addDebuggerListener(
                        DebuggerManager.PROP_CURRENT_SESSION, this);
               
            }
    
            if (myDebugger.getState () == BpelDebugger.STATE_DISCONNECTED) {
                //removeBreakpointImpls();
                myIsStarted = false;
                DebuggerManager.getDebuggerManager().removeDebuggerListener(
                    DebuggerManager.PROP_BREAKPOINTS, this);
                //TODO:why do we need a PROP_CURRENT_SESSION listener?
                DebuggerManager.getDebuggerManager().removeDebuggerListener(
                        DebuggerManager.PROP_CURRENT_SESSION, this);                
            }
        }        
//        else if (evt.getSource() == DebuggerManager.getDebuggerManager()) {
//            Session oldSession = (Session) evt.getOldValue();
//            Session newSession = (Session) evt.getNewValue();
//            
//            if (myDebugger.getSession() == oldSession) {
//                myDebugger.clearOldPosition();
//            } else if (myDebugger.getSession() == newSession) {
//                //Reactivate the current position
//                myDebugger.setCurrentProcessInstance(myDebugger.getCurrentProcessInstance());
//            }
//        }
    }

    /** {@inheritDoc} */
    public Breakpoint[] initBreakpoints() {
        return new Breakpoint[0];
    }

    /** {@inheritDoc} */
    public void breakpointAdded(Breakpoint breakpoint) {
        if (breakpoint instanceof BpelBreakpoint) {
            myDebugger.breakpointAdded((BpelBreakpoint)breakpoint);
        }
    }

    /** {@inheritDoc} */
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (breakpoint instanceof BpelBreakpoint) {
            myDebugger.breakpointRemoved((BpelBreakpoint)breakpoint);
        }
    }

    /** {@inheritDoc} */
    public void initWatches() {
    }

    /** {@inheritDoc} */
    public void watchAdded(Watch watch) {
    }

    /** {@inheritDoc} */
    public void watchRemoved(Watch watch) {
    }

    /** {@inheritDoc} */
    public void sessionAdded(Session session) {
    }

    /** {@inheritDoc} */
    public void sessionRemoved(Session session) {
    }

    /** {@inheritDoc} */
    public void engineAdded(DebuggerEngine debuggerEngine) {
    }

    /** {@inheritDoc} */
    public void engineRemoved(DebuggerEngine debuggerEngine) {
    }
}
