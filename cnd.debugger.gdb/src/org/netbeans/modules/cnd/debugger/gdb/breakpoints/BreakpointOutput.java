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

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.regex.Pattern;

import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.cnd.debugger.gdb.DebuggerOutput;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointEvent;
import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointListener;
import org.netbeans.modules.cnd.debugger.gdb.models.BreakpointsNodeModel;
import org.netbeans.modules.cnd.debugger.gdb.IOManager;

/**
 * Listener on all breakpoints and prints text specified in the breakpoint when a it hits.
 *
 * @see GdbBreakpoint#setPrintText(java.lang.String)
 * @author Maros Sandor
 */
public class BreakpointOutput extends LazyActionsManagerListener
                implements DebuggerManagerListener, GdbBreakpointListener, PropertyChangeListener {

    private static final Pattern fileNamePattern = Pattern.compile("\\{fileName\\}"); // NOI18N
    private static final Pattern functionNamePattern = Pattern.compile("\\{functionName\\}"); // NOI18N
    private static final Pattern lineNumberPattern = Pattern.compile("\\{lineNumber\\}"); // NOI18N
    private static final Pattern addressPattern = Pattern.compile("\\{address\\}"); // NOI18N

    private IOManager               ioManager;
    private GdbDebugger             debugger;
    private ContextProvider         contextProvider;
    private final Object            lock = new Object();

    
    public BreakpointOutput(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        this.debugger = contextProvider.lookupFirst(null, GdbDebugger.class);
        debugger.addPropertyChangeListener(GdbDebugger.PROP_STATE, this);
        hookBreakpoints();
        DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_BREAKPOINTS, this);
    }
    
    
    // LazyActionsManagerListener ..............................................
    
    protected void destroy() {
        DebuggerManager.getDebuggerManager().removeDebuggerListener(DebuggerManager.PROP_BREAKPOINTS, this);
        unhookBreakpoints();
        synchronized(lock) {
            ioManager = null;
            debugger = null;
        }
    }

    public String[] getProperties() {
        return new String[] { ActionsManagerListener.PROP_ACTION_PERFORMED };
    }
    
    
    // GdbBreakpointListener ..................................................

    public void breakpointReached(GdbBreakpointEvent event) {
        synchronized (lock) {
            if (event.getDebugger() != debugger) {
                return;
            }
        }
        if (event.getConditionResult() == GdbBreakpointEvent.CONDITION_FALSE) {
            return;
        }
        GdbBreakpoint breakpoint = (GdbBreakpoint) event.getSource();
        if (breakpoint.getSuspend() != GdbBreakpoint.SUSPEND_NONE) {
            getBreakpointsNodeModel().setCurrentBreakpoint(breakpoint);
            debugger.setCurrentBreakpoint(breakpoint);
        }
        synchronized (lock) {
            if (ioManager == null) {
                lookupIOManager();
                if (ioManager == null) {
                    return;
                }
            }
        }
        String printText = breakpoint.getPrintText();
        if (printText == null || printText.length() == 0) {
            return;
        }
        printText = substitute(printText, event);
        synchronized (lock) {
            if (ioManager != null) {
                ioManager.println(printText, null);
            }
        }
    }

    
    // DebuggerManagerListener .................................................

    public void breakpointAdded(Breakpoint breakpoint) {
        hookBreakpoint(breakpoint);
    }

    public void breakpointRemoved(Breakpoint breakpoint) {
        unhookBreakpoint(breakpoint);
    }
    
    public Breakpoint[] initBreakpoints() {return new Breakpoint[0];}
    public void initWatches() {}
    public void watchAdded(Watch watch) {}
    public void watchRemoved(Watch watch) {}
    public void sessionAdded(Session session) {}
    public void sessionRemoved(Session session) {}
    public void engineAdded(DebuggerEngine engine) {}
    public void engineRemoved(DebuggerEngine engine) {}

    
    // PropertyChangeListener ..................................................
    
    public void propertyChange(PropertyChangeEvent evt) {
        synchronized (lock) {
            if (debugger == null || !evt.getPropertyName().equals(GdbDebugger.PROP_STATE) ||
                        debugger.getState() == GdbDebugger.State.STOPPED) {
                return;
            }
        }
        getBreakpointsNodeModel().setCurrentBreakpoint(null);
        debugger.setCurrentBreakpoint(null);
    }

    
    // private methods .........................................................
    
    /**
     * Substitute values into the breakpoint output string.
     *
     * fileName        name of file where breakpoint occurs
     * lineNumber      number of line where breakpoint occurs
     *
     * @param printText
     * @return
     */
    private String substitute(String printText, GdbBreakpointEvent event) {
        Object o = event.getSource();
        if (o instanceof LineBreakpoint) {
            // replace $ by \$
            LineBreakpoint lpb = (LineBreakpoint) o;
            String name = lpb.getPath();
            String lnum = Integer.toString(lpb.getLineNumber());
            printText = fileNamePattern.matcher(printText).replaceAll(name);
            printText = lineNumberPattern.matcher(printText).replaceAll(lnum); // NOI18N
        } else if (o instanceof FunctionBreakpoint) {
            FunctionBreakpoint fbp = (FunctionBreakpoint) o;
            String functionName = fbp.getFunctionName();
            printText = functionNamePattern.matcher(printText).replaceAll(functionName);
        } else if (o instanceof AddressBreakpoint) {
            AddressBreakpoint abp = (AddressBreakpoint) o;
            String address = abp.getAddress();
            printText = addressPattern.matcher(printText).replaceAll(address);
	}
        Throwable thr = event.getConditionException();
        if (thr != null) {
            printText = printText + "\n***\n"+ thr.getLocalizedMessage()+"\n***\n"; // NOI18N
        }
        return printText;
    }

    private void lookupIOManager () {
        List<? extends LazyActionsManagerListener> lamls = contextProvider.lookup(null, LazyActionsManagerListener.class);
        for (LazyActionsManagerListener o : lamls) {
            if (o instanceof DebuggerOutput) {
                ioManager = ((DebuggerOutput) o).getIOManager();
                break;
            }
        }
    }
    
    private void hookBreakpoints() {
        Breakpoint [] bpts = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (int i = 0; i < bpts.length; i++) {
            Breakpoint bpt = bpts [i];
            hookBreakpoint(bpt);
        }
    }

    private void unhookBreakpoints() {
        Breakpoint [] bpts = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (int i = 0; i < bpts.length; i++) {
            Breakpoint bpt = bpts [i];
            unhookBreakpoint(bpt);
        }
    }

    private void hookBreakpoint(Breakpoint breakpoint) {
        if (breakpoint instanceof GdbBreakpoint) {
            GdbBreakpoint gdbBreakpoint = (GdbBreakpoint) breakpoint;
            gdbBreakpoint.addGdbBreakpointListener(this);
        }
    }

    private void unhookBreakpoint(Breakpoint breakpoint) {
        if (breakpoint instanceof GdbBreakpoint) {
            GdbBreakpoint jpdaBreakpoint = (GdbBreakpoint) breakpoint;
            jpdaBreakpoint.removeGdbBreakpointListener(this);
        }
    }
    
    private BreakpointsNodeModel breakpointsNodeModel;
    private BreakpointsNodeModel getBreakpointsNodeModel() {
        if (breakpointsNodeModel == null) {
            List<? extends NodeModel> l = DebuggerManager.getDebuggerManager().lookup("BreakpointsView", NodeModel.class); // NOI18N
            for (NodeModel nm : l) {
                if (nm instanceof BreakpointsNodeModel) {
                    breakpointsNodeModel = (BreakpointsNodeModel) nm;
                    break;
                }
            }
        }
        return breakpointsNodeModel;
    }
}
