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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;

import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;
import org.openide.windows.TopComponent;
import org.openide.awt.StatusDisplayer;


public class IOManager {

    /** DebuggerManager output constant. */
    public static final int                 DEBUGGER_OUT = 1;
    /** Process output constant. */
    public static final int                 PROCESS_OUT = 2;
    /** Status line output constant. */
    public static final int                 STATUS_OUT = 4;
    /** All outputs constant. */
    public static final int                 ALL_OUT = DEBUGGER_OUT + 
                                                PROCESS_OUT + STATUS_OUT;
    /** Standart process output constant. */
    public static final int                 STD_OUT = 1;
    /** Error process output constant. */
    public static final int                 ERR_OUT = 2;
    
    private static Hashtable                debuggerToProcesIO = new Hashtable ();
    private static Hashtable                debuggerToDebuggerIO = new Hashtable ();
    private static DebuggerEngine           currentEngine;

    
    // variables ...............................................................
    
    protected InputOutput                   debuggerIO = null;
    private DebuggerEngine                  engine;
    
    private OutputWriter                    debuggerOut;
    private String                          name;
    
    /** output writer Thread */
    private Hashtable                       lines = new Hashtable ();
    private Listener                        listener = new Listener ();
    private static DListener                dListener;

    
    // init ....................................................................
    
    public IOManager (
        DebuggerEngine engine
    ) {
        this.engine = engine;
        InputOutput debuggerIO = IOProvider.getDefault ().getIO ( 
            NbBundle.getBundle (IOManager.class).getString 
                ("CTL_DebuggerConsole_Title"), 
            true
        );
        debuggerIO.setFocusTaken (false);
        debuggerOut = debuggerIO.getOut ();
        ((TopComponent) debuggerIO).setVisible(false);
        
        debuggerToDebuggerIO.put (engine, debuggerIO);
        final DebuggerManager manager = DebuggerManager.getDebuggerManager ();
        if (dListener == null) {
            dListener = new DListener ();
                manager.addDebuggerListener (dListener);
        }
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                // close old tab from previous debugging session on engine start
                // hide current tab and set this engine current
                if (currentEngine == null) {
                    currentEngine = IOManager.this.engine;
                    return;
                }

                InputOutput io = (InputOutput) debuggerToProcesIO.get 
                    (currentEngine);
                if (io != null) {
                    if (manager.getSessions ().length < 2) {
                        io.closeInputOutput ();
                        debuggerToProcesIO.remove (currentEngine);
                    } else {
                        io.setOutputVisible (false);
                    }
                }
                io = (InputOutput) debuggerToDebuggerIO.get (currentEngine);
                if (io != null) {
                    if (manager.getSessions ().length < 2) {
                        io.closeInputOutput ();
                        debuggerToDebuggerIO.remove (currentEngine);
                    } else {
                        io.setOutputVisible (false);
                    } 
                }
                currentEngine = IOManager.this.engine;
            }
        });
    }
    
    
    // public interface ........................................................

    private LinkedList buffer = new LinkedList ();
    private RequestProcessor.Task task;
    
    /**
    * Prints given text to the output.
    */
    public void println (final String text, final int where, final Line line) {
        if (text == null)
            throw new NullPointerException ();
        synchronized (buffer) {
            buffer.addLast (new Text (text, where, line));
        }
        if (task == null)
            task = RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    synchronized (buffer) {
                        int i, k = buffer.size ();
                        for (i = 0; i < k; i++) {
                            Text t = (Text) buffer.removeFirst ();
                            try {
                                if ((t.where & DEBUGGER_OUT) != 0) {
                                    if (t.line != null) {
                                        debuggerOut.println (t.text, listener);
                                        lines.put (t.text, t.line);
                                    } else
                                        debuggerOut.println (t.text);
                                }
                                if ((t.where & STATUS_OUT) != 0) 
                                    StatusDisplayer.getDefault ().setStatusText (t.text);
                            } catch (IOException ex) {
                                ex.printStackTrace ();
                            }
                        }
                    }
                }
            }, 500, Thread.MIN_PRIORITY);
        else 
            task.schedule (500);
    }

    /**
     * Stops communication between InputOutput and process.
     */
    public void stop () {
        DebuggerManager manager = DebuggerManager.getDebuggerManager ();
        if (manager.getSessions ().length > 1) {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    //S ystem.out.println("IOManager.stop Tab Closed " + engine);
                    InputOutput io = (InputOutput) debuggerToProcesIO.get 
                        (engine);
                    if (io != null)
                       io.closeInputOutput ();
                    io = (InputOutput) debuggerToDebuggerIO.get (engine);
                    if (io != null)
                        io.closeInputOutput ();
                    debuggerToProcesIO.remove (engine);
                    debuggerToDebuggerIO.remove (engine);
                }
            });
        }
        manager.removeDebuggerListener (dListener);
    }
    
    
    // helper methods ..........................................................
    
    private static void switchOutput () {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                DebuggerEngine d = DebuggerManager.getDebuggerManager ().getCurrentEngine ();
                if ( (currentEngine != null) &&
                     (currentEngine != d) && (d != null)
                ) {
                    // hides current engine
                    InputOutput io = (InputOutput) debuggerToProcesIO.get 
                        (currentEngine);
                    if (io != null)
                        io.setOutputVisible (false);
                    io = (InputOutput) debuggerToDebuggerIO.get (currentEngine);
                    if (io != null)
                        io.setOutputVisible (false);
                    currentEngine = null;
                }
                if (d != null) {
                    InputOutput io = (InputOutput) debuggerToDebuggerIO.get (d);
                    if (io != null)
                        io.setOutputVisible (true);
                    io = (InputOutput) debuggerToProcesIO.get (d);
                    if (io != null)
                        io.setOutputVisible (true);
                    currentEngine = d;
                }
            }
        });
    }
    
    
    // innerclasses ............................................................
    
    private class Listener implements OutputListener {
        public void outputLineSelected (OutputEvent ev) {
        }
        public void outputLineAction (OutputEvent ev) {
            String t = ev.getLine ();
            Line l = (Line) lines.get (t);
            if (l == null) return;
            l.show ();
        }
        public void outputLineCleared (OutputEvent ev) {
            lines = new Hashtable ();
        }
    }
    
    private static class DListener extends DebuggerManagerAdapter {
        public void propertyChange (PropertyChangeEvent e) {
            if ( (e.getPropertyName () == null) ||
                 (e.getPropertyName () != DebuggerManager.PROP_CURRENT_ENGINE)
            ) return;
            switchOutput ();
        }
    }
    
    private static class Text {
        private String text;
        private Line line;
        private int where;
        
        private Text (String text, int where, Line line) {
            this.text = text;
            this.where = where;
            this.line = line;
        }
    }
    
    static class Line {
        Session session;
        String resourceName;
        int lineNumber;
        
        Line (Session session, String resourceName, int lineNumber) {
            this.session = session;
            this.resourceName = resourceName;
            this.lineNumber = lineNumber;
        }
        
        void show () {
            EngineContext ectx = (EngineContext) session.lookupFirst 
                (EngineContext.class);
            Context.showSource (ectx.getURL (resourceName), lineNumber);
        }
    }
}
