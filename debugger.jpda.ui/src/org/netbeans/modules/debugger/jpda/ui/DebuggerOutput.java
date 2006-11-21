/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui;

import com.sun.jdi.AbsentInformationException;
import java.beans.PropertyChangeListener;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;


import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AbstractDICookie;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LaunchingDICookie;
import org.netbeans.api.debugger.jpda.ListeningDICookie;
import org.netbeans.spi.debugger.ContextProvider;

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


    // set of all IOManagers
    private static Set          managers = new HashSet ();
    
    private JPDADebugger        debugger;
    //private DebuggerEngine      engine;
    private SourcePath          engineContext;
    private IOManager           ioManager;
    private ContextProvider     contextProvider;


    public DebuggerOutput (ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        this.debugger = (JPDADebugger) contextProvider.lookupFirst 
            (null, JPDADebugger.class);
        //this.engine = (DebuggerEngine) contextProvider.lookupFirst 
        //    (null, DebuggerEngine.class);
        engineContext = (SourcePath) contextProvider.lookupFirst 
            (null, SourcePath.class);
        
        // close old tabs
        if (DebuggerManager.getDebuggerManager ().getSessions ().length == 1) {
            Iterator i = managers.iterator ();
            while (i.hasNext ())
                ((IOManager) i.next ()).close ();
            managers = new HashSet ();
        }
        
        // open new tab
        String title = (String) contextProvider.lookupFirst (null, String.class);
        if (title == null)
            title = NbBundle.getBundle (IOManager.class).getString 
                ("CTL_DebuggerConsole_Title");
        ioManager = new IOManager (title);
        managers.add (ioManager);
        
        debugger.addPropertyChangeListener (
            JPDADebugger.PROP_STATE,
            this
        );
    }

    protected synchronized void destroy () {
        debugger.removePropertyChangeListener (
            JPDADebugger.PROP_STATE,
            this
        );
        debugger = null;
        //engine = null;
        engineContext = null;
        ioManager = null;
    }

    public String[] getProperties () {
        return new String[] {ActionsManagerListener.PROP_ACTION_PERFORMED};
    }

    public void propertyChange (java.beans.PropertyChangeEvent evt) {
        JPDAThread t;
        int debuggerState;
        IOManager ioManager;
        synchronized (this) {
            if (debugger == null) return ;
            t = debugger.getCurrentThread ();
            debuggerState = debugger.getState();
            ioManager = this.ioManager;
        }
        if (debuggerState == JPDADebugger.STATE_STARTING) {
            AbstractDICookie cookie = (AbstractDICookie) contextProvider.
                lookupFirst (null, AbstractDICookie.class);
            if (cookie instanceof AttachingDICookie) {
                AttachingDICookie c = (AttachingDICookie) cookie;
                if (c.getHostName () != null) {
                    print (
                        "CTL_Attaching_socket",
//                        where,
                        new String[] {
                            c.getHostName (),
                            String.valueOf(c.getPortNumber ())
                        },
                        null
                    );
                } else if (c.getSharedMemoryName() != null) {
                    print (
                        "CTL_Attaching_shmem",
//                        where,
                        new String[] {
                            c.getSharedMemoryName ()
                        },
                        null
                    );
                } else if (c.getArgs().get("pid") != null) {
                    print (
                        "CTL_Attaching_pid",
//                        where,
                        new String[] {
                            c.getArgs().get("pid").toString()
                        },
                        null
                    );
                } else {
                    print (
                        "CTL_Attaching",
                        null,
                        null
                    );
                }
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
                            String.valueOf(c.getPortNumber ())
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
        if (debuggerState == JPDADebugger.STATE_RUNNING) {
            print (
                "CTL_Debugger_running",
//                where,
                new String[] {
                },
                null
            );
        } else
        if (debuggerState == JPDADebugger.STATE_DISCONNECTED) {
            Throwable e = null;
            try {
                synchronized (this) {
                    if (debugger != null) {
                        debugger.waitRunning ();
                    }
                }
            } catch (DebuggerStartException ex) {
                e = ex.getTargetException ();
            }
            if (e == null)
                print ("CTL_Debugger_finished", null, null);
            else {
                String message = e.getMessage ();
                if (e instanceof ConnectException)
                    message = NbBundle.getBundle (DebuggerOutput.class).
                        getString ("CTL_Connection_refused");
                if (e instanceof UnknownHostException)
                    message = NbBundle.getBundle (DebuggerOutput.class).
                        getString ("CTL_Unknown_host");
                if (message != null) {
                    ioManager.println (
                        message,
                        null
                    );
                } else
                    ioManager.println (
                        e.toString (),
                        null
                    );
                //e.printStackTrace ();
            }
            ioManager.closeStream ();
        } else
        if (debuggerState == JPDADebugger.STATE_STOPPED) {
            //DebuggerEngine engine = debugger.getEngine ();
            //S ystem.out.println("State Stopped " + debugger.getLastAction ());
            if (t == null) {
                print ("CTL_Debugger_stopped", null, null);
                return;
            }
            Session session = null;
            Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
            for (int i = 0; i < sessions.length; i++) {
                if (sessions[i].lookupFirst(null, JPDADebugger.class) == debugger) {
                    session = sessions[i];
                    break;
                }
            }
            String language = (session != null) ? session.getCurrentLanguage() : null;
            String threadName = t.getName ();
            String methodName = t.getMethodName ();
            String className = t.getClassName ();
            int lineNumber = t.getLineNumber (language);
            try {
                String sourceName = t.getSourceName (language);
                String relativePath = EditorContextBridge.getRelativePath 
                    (t, language);
                String url = null;
                synchronized (this) {
                    if (relativePath != null && engineContext != null) {
                        url = engineContext.getURL(relativePath, true);
                    }
                }
                IOManager.Line line = null;
                if (lineNumber > 0 && url != null)
                    line = new IOManager.Line (
                        url, 
                        lineNumber,
                        debugger
                    );

                if (lineNumber > 0)
                    print (
                        "CTL_Thread_stopped",
                      //  IOManager.DEBUGGER_OUT + IOManager.STATUS_OUT,
                        new String[] {
                            threadName,
                            sourceName,
                            methodName,
                            String.valueOf(lineNumber)
                        },
                        line
                    );
                else if (sourceName.length() > 0 && methodName.length() > 0)
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
                else
                    print (
                        "CTL_Thread_stopped_no_line_no_source",
                        new String[] { threadName },
                        line
                    );
            } catch (AbsentInformationException ex) {
                if (lineNumber > 0)
                    print (
                        "CTL_Thread_stopped_no_info",
                     //   IOManager.DEBUGGER_OUT + IOManager.STATUS_OUT,
                        new String[] {
                            threadName,
                            className,
                            methodName,
                            lineNumber > 0 ? String.valueOf(lineNumber) : ""
                        },
                        null
                    );
                else
                    print (
                        "CTL_Thread_stopped_no_info_no_line",
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
        if (action == ActionsManager.ACTION_CONTINUE)
            print ("CTL_Continue", null, null);
        else
        if (action == ActionsManager.ACTION_STEP_INTO)
            print ("CTL_Step_Into", null, null);
        else
        if (action == ActionsManager.ACTION_STEP_OUT)
            print ("CTL_Step_Out", null, null);
        else
        if (action == ActionsManager.ACTION_STEP_OVER)
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

        IOManager ioManager;
        synchronized (this) {
            ioManager = this.ioManager;
            if (ioManager == null) return ;
        }
        ioManager.println (
            text,
//            where,
            line
        );
    }
}
