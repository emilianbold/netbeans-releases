/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.spi.debugger.ContextProvider;

import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Listener on all breakpoints and prints text specified in the breakpoint when a it hits.
 *
 * @see JPDABreakpoint#setPrintText(java.lang.String)
 * @author Maros Sandor
 */
public class BreakpointOutput extends LazyActionsManagerListener
        implements DebuggerManagerListener, JPDABreakpointListener {

    private static final Pattern dollarEscapePattern = Pattern.compile("\\$");
    private static final Pattern backslashEscapePattern = Pattern.compile("\\\\");
    private static final Pattern threadNamePattern = Pattern.compile("\\{threadName\\}");
    private static final Pattern classNamePattern = Pattern.compile("\\{className\\}");
    private static final Pattern methodNamePattern = Pattern.compile("\\{methodName\\}");
    private static final Pattern lineNumberPattern = Pattern.compile("\\{lineNumber\\}");
    private static final Pattern expressionPattern = Pattern.compile("\\{=(.*?)\\}");

    private IOManager               ioManager;
    private JPDADebugger            debugger;
    private ContextProvider         contextProvider;

    
    public BreakpointOutput (ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        this.debugger = (JPDADebugger) contextProvider.lookupFirst (null, JPDADebugger.class);
        hookBreakpoints();
        DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_BREAKPOINTS, this);
    }

    private void hookBreakpoints() {
        Breakpoint [] bpts = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (int i = 0; i < bpts.length; i++) {
            Breakpoint bpt = bpts[i];
            hookBreakpoint(bpt);
        }
    }

    protected void destroy() {
        DebuggerManager.getDebuggerManager().removeDebuggerListener(DebuggerManager.PROP_BREAKPOINTS, this);
        unhookBreakpoints();
        ioManager = null;
        debugger = null;
    }

    private void unhookBreakpoints() {
        Breakpoint [] bpts = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (int i = 0; i < bpts.length; i++) {
            Breakpoint bpt = bpts[i];
            unhookBreakpoint(bpt);
        }
    }

    public void breakpointReached (JPDABreakpointEvent event) {
        if (event.getDebugger () != debugger) return;
        if (event.getConditionResult () == event.CONDITION_FAILED) return;
        if (event.getConditionResult () == event.CONDITION_FALSE) return;
        if (ioManager == null) {
            lookupIOManager ();
            if (ioManager == null) return;
        }
        JPDABreakpoint breakpoint = (JPDABreakpoint) event.getSource();
        String printText = breakpoint.getPrintText();
        if (printText == null || printText.length() == 0) return;
        try {
            printText = substitute(printText, event);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        ioManager.println (printText, null);
    }

    /**
     *   threadName      name of thread where breakpoint ocurres
     *   className       name of class where breakpoint ocurres
     *   methodName      name of method where breakpoint ocurres
     *   lineNumber      number of line where breakpoint ocurres
     *
     * @param printText
     * @return
     */
    private String substitute(String printText, JPDABreakpointEvent event) {
        JPDAThread t = event.getThread ();
        if (t != null) {
            String name = backslashEscapePattern.matcher(t.getName()).replaceAll("\\\\\\\\");
            name = dollarEscapePattern.matcher(name).replaceAll("\\\\\\$");
            printText = threadNamePattern.matcher(printText).replaceAll(name);
        }
        else
            printText = threadNamePattern.matcher(printText).replaceAll ("?");
        
        if (event.getReferenceType () != null) {
            String name = dollarEscapePattern.matcher(event.getReferenceType().name()).replaceAll("\\\\\\$");
            printText = classNamePattern.matcher (printText).replaceAll(name);
        } else
            printText = classNamePattern.matcher (printText).replaceAll ("?");

        String language = DebuggerManager.getDebuggerManager ().
            getCurrentSession ().getCurrentLanguage ();
        
        String methodName = t.getMethodName ();
        if (methodName.equals ("")) methodName = "?";
        printText = methodNamePattern.matcher (printText).replaceAll 
            (methodName);
        int lineNumber = t.getLineNumber (language);
        if (lineNumber < 0)
            printText = lineNumberPattern.matcher (printText).replaceAll 
                ("?");
        else
            printText = lineNumberPattern.matcher (printText).replaceAll 
                (String.valueOf (lineNumber));
             
        for (;;) {
            Matcher m = expressionPattern.matcher(printText);
            if (!m.find()) break;
            String expression = m.group(1);
            String value = "";
            try {
                value = debugger.evaluate(expression).getValue();
                value = backslashEscapePattern.matcher(value).replaceAll("\\\\\\\\");
                value = dollarEscapePattern.matcher(value).replaceAll("\\\\\\$");
            } catch (InvalidExpressionException e) {
                // expression is invalid or cannot be evaluated
                String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                ioManager.println ("Cannot evaluate expression '" + expression + "' : " + msg, null);
            }
            printText = m.replaceFirst(value);
        }
        return printText;
    }

    private void lookupIOManager () {
        List lamls = contextProvider.lookup (null, LazyActionsManagerListener.class);
        for (Iterator i = lamls.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof DebuggerOutput) {
                ioManager = ((DebuggerOutput) o).getIOManager();
                break;
            }
        }
    }

    private void hookBreakpoint(Breakpoint breakpoint) {
        if (breakpoint instanceof JPDABreakpoint) {
            JPDABreakpoint jpdaBreakpoint = (JPDABreakpoint) breakpoint;
            jpdaBreakpoint.addJPDABreakpointListener(this);
        }
    }

    private void unhookBreakpoint(Breakpoint breakpoint) {
        if (breakpoint instanceof JPDABreakpoint) {
            JPDABreakpoint jpdaBreakpoint = (JPDABreakpoint) breakpoint;
            jpdaBreakpoint.removeJPDABreakpointListener(this);
        }
    }

    public void breakpointAdded(Breakpoint breakpoint) {
        hookBreakpoint(breakpoint);
    }

    public void breakpointRemoved(Breakpoint breakpoint) {
        unhookBreakpoint(breakpoint);
    }

    public String[] getProperties() {
        return new String[] { ActionsManagerListener.PROP_ACTION_PERFORMED };
    }

    public void propertyChange (PropertyChangeEvent evt) {}
    public Breakpoint[] initBreakpoints() {return new Breakpoint[0];}
    public void initWatches () {}
    public void watchAdded (Watch watch) {}
    public void watchRemoved (Watch watch) {}
    public void sessionAdded (Session session) {}
    public void sessionRemoved (Session session) {}
    public void engineAdded (DebuggerEngine engine) {}
    public void engineRemoved (DebuggerEngine engine) {}
}
