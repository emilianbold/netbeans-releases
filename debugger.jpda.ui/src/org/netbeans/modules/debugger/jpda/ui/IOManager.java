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
    
    //private static Hashtable                closedOutputs = new Hashtable ();
    //private static final boolean            sepatated = true;
    //private static final boolean            per_session = false;
    private static Hashtable                debuggerToProcesIO = new Hashtable ();
    private static Hashtable                debuggerToDebuggerIO = new Hashtable ();
    private static DebuggerEngine           currentEngine;

    
    // variables ...............................................................
    
    protected InputOutput                   processIO = null;
    protected InputOutput                   debuggerIO = null;
    private DebuggerEngine                  engine;
    
    private OutputWriter                    processOut;
    private OutputWriter                    debuggerOut;
    private String                          name;
    
    /** output writer Thread */
    private Thread                          inputThread = null;
    private Thread                          outputThread = null;
    private Thread                          errorThread = null;
    private Hashtable                       lines = new Hashtable ();
    private Listener                        listener = new Listener ();
    private static DListener                dListener;

    
    // init ....................................................................
    
    public IOManager (
        DebuggerEngine engine
    ) {
        this.engine = engine;
        InputOutput debuggerIO = IOProvider.getDefault ().getIO 
            ("DebuggerManager Console", true);
            //S ystem.out.println("IO " + debuggerIO.getClass ());
            //debuggerIO.setOutputVisible (true);
            //debuggerIO.setErrVisible (true);
            //debuggerIO.setInputVisible (false);
            debuggerIO.setFocusTaken (false);
            debuggerOut = debuggerIO.getOut ();
        processIO = IOProvider.getDefault ().getIO ("Process Output", true);
            //processIO.setOutputVisible (true);
            //processIO.setErrVisible (true);
            //processIO.setInputVisible (false);
            //processIO.setFocusTaken (true);
            processOut = processIO.getOut ();
            //processIO.select ();
        ((TopComponent) debuggerIO).hide ();
        
        debuggerToDebuggerIO.put (engine, debuggerIO);
        debuggerToProcesIO.put (engine, processIO);
        final DebuggerManager manager = DebuggerManager.getDebuggerManager ();
        if (dListener == null) {
            dListener = new DListener ();
                manager.addDebuggerListener (dListener);
        }
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                // close old tab from previous debugging session on engine start
                // hide current tab and set this engine current

                //S ystem.out.println("IOManager.init - #debuggers " + manager.getDebuggers ().length);
                //S ystem.out.println("IOManager.init - #currentEngine " + currentEngine);
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
    
    /**
    * 
    */
    public void select () {
        processIO.select ();
    }
    
    /**
    * Prints given text to the output.
    */
    public void print (final String text, final int where) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                if ((where & PROCESS_OUT) != 0) processOut.print (text);
                if ((where & DEBUGGER_OUT) != 0) debuggerOut.print (text);
                if ((where & STATUS_OUT) != 0) StatusDisplayer.getDefault ().setStatusText (text);
            }
        });
    }
//
//    /**
//    * Prints given text to the output.
//    */
//    public void println (final String text) {
//        println (text, DEBUGGER_OUT);
//    }
//    
//    /**
//    * Prints given text to the output.
//    */
//    public void println (final String text, final int where) {
//        println (text, where, null);
//    }

    private LinkedList buffer = new LinkedList ();
    private RequestProcessor.Task task;
    
    /**
    * Prints given text to the output.
    */
    public void println (final String text, final int where, final Line line) {
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
                                if ((t.where & PROCESS_OUT) != 0) {
                                    if (t.line != null) {
                                        processOut.println (t.text, listener);
                                        lines.put (t.text, t.line);
                                    } else
                                        processOut.println (t.text);
                                }
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
        
//        SwingUtilities.invokeLater (new Runnable () {
//            public void run () {
//                try {
//                    if ((where & PROCESS_OUT) != 0) {
//                        if (line != null) {
//                            processOut.println (text, listener);
//                            lines.put (text, line);
//                        } else
//                            processOut.println (text);
//                    }
//                    if ((where & DEBUGGER_OUT) != 0) {
//                        if (line != null) {
//                            debuggerOut.println (text, listener);
//                            lines.put (text, line);
//                        } else
//                            debuggerOut.println (text);
//                    }
//                    if ((where & STATUS_OUT) != 0) 
//                        IOProvider.getDefault ().setStatusText (text);
//                } catch (IOException e) {
//                }
//            }
//        });
    }

    /**
    * Shows output and error from this proces in output window.
    */
    public void showOutput (final Process process, int what, final int where) {
        if (process == null) throw new NullPointerException ();
        if ((what | STD_OUT) != 0) {
            (outputThread = new CopyMaker (
                "DebuggerManager output writer thread",
                new InputStreamReader (process.getInputStream ()), 
                ((where & PROCESS_OUT) != 0) ? processOut : debuggerOut,
                false
            )).start ();
        }

        if ((what | ERR_OUT) != 0) {
            (errorThread = new CopyMaker (
                "DebuggerManager error writer thread",
                new InputStreamReader (process.getErrorStream ()), 
                ((where & PROCESS_OUT) != 0) ? processOut : debuggerOut,
                false
            )).start ();
        }
    }

    public void connectInput (final Process process) {
        if (process == null) throw new NullPointerException ();
        if (processIO == null) return;
        processIO.setInputVisible (true);
        processIO.flushReader ();
        (inputThread = new CopyMaker (
            "DebuggerManager input reader thread",
            processIO.getIn (), 
            new OutputStreamWriter (process.getOutputStream ()),
            true
        )).start ();
    }

    /**
     * Stops communication between InputOutput and process.
     */
    public void stop () {
        if (errorThread != null) {
            errorThread.interrupt ();
            errorThread = null;
        }
        if (outputThread != null) {
            outputThread.interrupt ();
            outputThread = null;
        }
        if (inputThread != null) {
            inputThread.interrupt ();
            inputThread = null;
        }
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
    }
    
    
    // helper methods ..........................................................
    
    private static void switchOutput () {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                DebuggerEngine d = DebuggerManager.getDebuggerManager ().getCurrentEngine ();
                //S ystem.out.println("IOManager.switch old d: " + currentEngine);
                //S ystem.out.println("IOManager.switch new d: " + d);
                if ( (currentEngine != null) &&
                     (currentEngine != d) && (d != null)
                ) {
                    // hides current engine
                    //S ystem.out.println("IOManager.switch Tab Hidden " + currentEngine);
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
                    //S ystem.out.println("IOManager.switch Tab Showen " + d);
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
    
    
/*    private static synchronized void addClosedOutput (
        String name, 
        InputOutput io
    ) {
        List list = (List) closedOutputs.get (name);
        if (list == null) {
            list = new LinkedList ();
            closedOutputs.put (name, list);
        }
        list.add (io);
    }*/
    
    
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


    /** 
     * This thread simply reads from given Reader and writes 
     * read chars to given Writer. 
     */
    private static class CopyMaker extends Thread {
        final Writer os;
        final Reader is;
        /** 
         * while set to false at streams that writes to the OutputWindow 
         * it must be true for a stream that reads from the window.
         */
        final boolean autoflush;

        CopyMaker (String name, Reader is, Writer os, boolean b) {
            super (name);
            this.os = os;
            this.is = is;
            autoflush = b;
            setPriority (Thread.MIN_PRIORITY);
        }

        /* Makes copy. */
        public void run() {
            int read;
            char[] buff = new char [256];
            try {                
                while ((read = read(is, buff, 0, 256)) > 0x0) {
                    os.write(buff,0,read);
                    if (autoflush) os.flush();
                }
            } catch (IOException ex) {
            } catch (InterruptedException e) {
            }
        }
        
        private int read (
            Reader is, 
            char[] buff, 
            int start, 
            int count
        ) throws InterruptedException, IOException {
            // XXX (anovak) IBM JDK 1.3.x on OS/2 is broken
            // is.ready()/available() returns false/0 until
            // at least one byte from the stream is read.
            // Then it works as advertised.
            // isao 2001-11-12: ditto for JDK 1.3 on OpenVMS
            
            if (Utilities.getOperatingSystem() != Utilities.OS_OS2
                && Utilities.getOperatingSystem() != Utilities.OS_VMS
                ) {
                while (!is.ready()) sleep(100);
            }
            return is.read(buff, start, count);
        }
    } // end of CopyMaker
    
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
