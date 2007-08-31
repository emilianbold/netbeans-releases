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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Iterator;
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

    private static final Pattern backslashEscapePattern = Pattern.compile("\\\\"); // NOI18N
    private static final Pattern threadNamePattern = Pattern.compile("\\{threadName\\}"); // NOI18N
    private static final Pattern fileNamePattern = Pattern.compile("\\{fileName\\}"); // NOI18N
    private static final Pattern functionNamePattern = Pattern.compile("\\{functionName\\}"); // NOI18N
    private static final Pattern lineNumberPattern = Pattern.compile("\\{lineNumber\\}"); // NOI18N
    private static final Pattern expressionPattern = Pattern.compile("\\{=(.*?)\\}"); // NOI18N

    private IOManager               ioManager;
    private GdbDebugger             debugger;
    private ContextProvider         contextProvider;
    private Object                  lock = new Object();

    
    public BreakpointOutput(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        this.debugger = (GdbDebugger) contextProvider.lookupFirst(null, GdbDebugger.class);
        debugger.addPropertyChangeListener(debugger.PROP_STATE, this);
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
        if (event.getConditionResult() == event.CONDITION_FALSE) {
            return;
        }
        GdbBreakpoint breakpoint = (GdbBreakpoint) event.getSource();
        getBreakpointsNodeModel().setCurrentBreakpoint(breakpoint);
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
            if (debugger == null || evt.getPropertyName() != debugger.PROP_STATE ||
                        debugger.getState() != debugger.STATE_RUNNING) {
                return;
            }
        }
        getBreakpointsNodeModel().setCurrentBreakpoint(null);
            
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
            String name = basename(lpb.getURL());
            String lnum = Integer.toString(lpb.getLineNumber());
            printText = fileNamePattern.matcher(printText).replaceAll(name);
            printText = lineNumberPattern.matcher(printText).replaceAll(lnum); // NOI18N
        } else {
            printText = fileNamePattern.matcher(printText).replaceAll("?"); // NOI18N
            printText = lineNumberPattern.matcher(printText).replaceAll("-1"); // NOI18N
	}
        Throwable thr = event.getConditionException();
        if (thr != null) {
            printText = printText + "\n***\n"+ thr.getLocalizedMessage()+"\n***\n"; // NOI18N
        }
        return printText;
    }
    
    private String basename(String url) {
        int idx = url.lastIndexOf(File.separatorChar);
        String name;
        
        if (idx > 0) {
            name = url.substring(idx + 1);
        } else {
            name = url.substring(5);
        }
        return name;
    }

    private void lookupIOManager () {
        List lamls = contextProvider.lookup 
            (null, LazyActionsManagerListener.class);
        for (Iterator i = lamls.iterator (); i.hasNext ();) {
            Object o = i.next();
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
            List l = DebuggerManager.getDebuggerManager().lookup("BreakpointsView", NodeModel.class); // NOI18N
            Iterator it = l.iterator();
            while (it.hasNext()) {
                NodeModel nm = (NodeModel) it.next();
                if (nm instanceof BreakpointsNodeModel) {
                    breakpointsNodeModel = (BreakpointsNodeModel) nm;
                    break;
                }
            }
        }
        return breakpointsNodeModel;
    }
}
