/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui;

import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.jpda.AbstractDICookie;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LaunchingDICookie;
import org.netbeans.api.debugger.jpda.ListeningDICookie;
import org.netbeans.spi.viewmodel.NoInformationException;

import org.openide.util.NbBundle;


/**
 * Listens on
 * {@link org.netbeans.api.debugger.ActionsManagerListener#PROP_ACTION_PERFORMED} and
 * {@link org.netbeans.api.debugger.jpda.JPDADebugger#PROP_STATE}
 * properties and writes some messages to Debugger Console.
 *
 * @author   Jan Jancura
 */
public class DebuggerOutput extends LazyActionsManagerListener implements
PropertyChangeListener {

    //private static final int    where = IOManager.STATUS_OUT + IOManager.STD_OUT;

    private JPDADebugger        debugger;
    private DebuggerEngine      engine;
    private EngineContext       engineContext;
    private IOManager           ioManager;


    public DebuggerOutput (DebuggerEngine engine) {
        this.debugger = (JPDADebugger) engine.lookupFirst
            (JPDADebugger.class);
        this.engine = engine;
        engineContext = (EngineContext) engine.lookupFirst (EngineContext.class);
        
        ioManager = new IOManager ();
        debugger.addPropertyChangeListener (
            JPDADebugger.PROP_STATE,
            this
        );
    }

    protected void destroy () {
        print (
            "CTL_Debugger_finished",
            //IOManager.STATUS_OUT + IOManager.STATUS_OUT,
            null, null
        );
        debugger.removePropertyChangeListener (
            JPDADebugger.PROP_STATE,
            this
        );
        ioManager.stop ();
        debugger = null;
        engine = null;
        engineContext = null;
        ioManager = null;
    }

    public String[] getProperties () {
        return new String[] {ActionsManagerListener.PROP_ACTION_PERFORMED};
    }

    public void propertyChange (java.beans.PropertyChangeEvent evt) {
        JPDAThread t = debugger.getCurrentThread ();
        if (debugger.getState () == JPDADebugger.STATE_STARTING) {
            AbstractDICookie cookie = (AbstractDICookie) engine.
                lookupFirst (AbstractDICookie.class);
            if (cookie instanceof AttachingDICookie) {
                AttachingDICookie c = (AttachingDICookie) cookie;
                if (c.getHostName () != null)
                    print (
                        "CTL_Attaching_socket",
//                        where,
                        new String[] {
                            c.getHostName (),
                            "" + c.getPortNumber ()
                        },
                        null
                    );
                else
                    print (
                        "CTL_Attaching_shmem",
//                        where,
                        new String[] {
                            c.getSharedMemoryName ()
                        },
                        null
                    );
            } else
            if (cookie instanceof ListeningDICookie) {
                ListeningDICookie c = (ListeningDICookie) cookie;
                if (c.getSharedMemoryName () != null)
                    print (
                        "CTL_Listening_shmem",
//                        where,
                        new String[] {
                            c.getSharedMemoryName ()
                        },
                        null
                    );
                else
                    print (
                        "CTL_Listening_socket",
//                        where,
                        new String[] {
                            "" + c.getPortNumber ()
                        },
                        null
                    );
            } else
            if (cookie instanceof LaunchingDICookie) {
                LaunchingDICookie c = (LaunchingDICookie) cookie;
                    print (
                        "CTL_Launching",
//                        where,
                        new String[] {
                            c.getCommandLine ()
                        },
                        null
                    );
            }
        } else
        if (debugger.getState () == JPDADebugger.STATE_RUNNING) {
            print (
                "CTL_Debugger_running",
//                where,
                new String[] {
                },
                null
            );
        } else
        if (debugger.getState () == JPDADebugger.STATE_DISCONNECTED) {
            Throwable e = null;
            try {
                debugger.waitRunning ();
            } catch (DebuggerStartException ex) {
                e = ex.getTargetException ();
            }
            if (e == null)
                print ("CTL_Debugger_finished", null, null);
            else {
                ioManager.println (
                    e.getMessage (),
//                    where,
                    null
                );
                e.printStackTrace ();
            }
        } else
        if (debugger.getState () == JPDADebugger.STATE_STOPPED) {
            //DebuggerEngine engine = debugger.getEngine ();
            //S ystem.out.println("State Stopped " + debugger.getLastAction ());
            if (t == null) {
                print ("CTL_Debugger_stopped", null, null);
                return;
            }
            String language = DebuggerManager.getDebuggerManager ().
                getCurrentSession ().getCurrentLanguage ();
            String threadName = t.getName ();
            String methodName = t.getMethodName ();
            String className = t.getClassName ();
            int lineNumber = t.getLineNumber (language);
            try {
                String sourceName = t.getSourceName (language);
                CallStackFrame f = t.getStackDepth () > 0 ?
                    t.getCallStack () [0] : null;
                String relativePath = f != null ? 
                    Context.getRelativePath (f, language) : null;
                String url = (relativePath != null) ?
                    engineContext.getURL (relativePath) :
                    null;
                IOManager.Line line = null;
                if (lineNumber > 0 && url != null)
                    line = new IOManager.Line (
                        url, 
                        lineNumber 
                    );

                if (lineNumber > 0)
                    print (
                        "CTL_Thread_stopped",
                      //  IOManager.DEBUGGER_OUT + IOManager.STATUS_OUT,
                        new String[] {
                            threadName,
                            sourceName,
                            methodName,
                            "" + lineNumber
                        },
                        line
                    );
                else
                    print (
                        "CTL_Thread_stopped_no_line",
                    //    IOManager.DEBUGGER_OUT + IOManager.STATUS_OUT,
                        new String[] {
                            threadName,
                            sourceName,
                            methodName
                        },
                        line
                    );
            } catch (NoInformationException ex) {
                if (lineNumber > 0)
                    print (
                        "CTL_Thread_stopped_no_source",
                     //   IOManager.DEBUGGER_OUT + IOManager.STATUS_OUT,
                        new String[] {
                            threadName,
                            className,
                            methodName,
                            lineNumber > 0 ? "" + lineNumber : ""
                        },
                        null
                    );
                else
                    print (
                        "CTL_Thread_stopped_no_source_no_line",
                        //IOManager.DEBUGGER_OUT + IOManager.STATUS_OUT,
                        new String[] {
                            threadName,
                            className,
                            methodName
                        },
                        null
                    );
            }
        }
    }

    public void actionPerformed (Object action, boolean success) {
        if (!success) return;
        //print ("CTL_Debugger_running", where, null, null);
        if (action == DebuggerManager.ACTION_CONTINUE)
            print ("CTL_Continue", null, null);
        else
        if (action == DebuggerManager.ACTION_STEP_INTO)
            print ("CTL_Step_Into", null, null);
        else
        if (action == DebuggerManager.ACTION_STEP_OUT)
            print ("CTL_Step_Out", null, null);
        else
        if (action == DebuggerManager.ACTION_STEP_OVER)
            print ("CTL_Step_Over", null, null);
    }

    IOManager getIOManager() {
        return ioManager;
    }

    // helper methods ..........................................................

    private void print (
        String message,
//        int where,
        String[] args,
        IOManager.Line line
    ) {
        String text = (args == null) ?
            NbBundle.getMessage (
                DebuggerOutput.class,
                message
            ) :
            new MessageFormat (NbBundle.getMessage (
                DebuggerOutput.class,
                message
            )).format (args);

        ioManager.println (
            text,
//            where,
            line
        );
    }
}
