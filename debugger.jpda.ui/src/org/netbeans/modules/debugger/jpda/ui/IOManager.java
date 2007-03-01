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

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

public class IOManager {

//    /** DebuggerManager output constant. */
//    public static final int                 DEBUGGER_OUT = 1;
//    /** Process output constant. */
//    public static final int                 PROCESS_OUT = 2;
//    /** Status line output constant. */
//    public static final int                 STATUS_OUT = 4;
//    /** All outputs constant. */
//    public static final int                 ALL_OUT = DEBUGGER_OUT + 
//                                                PROCESS_OUT + STATUS_OUT;
//    /** Standart process output constant. */
//    public static final int                 STD_OUT = 1;
//    /** Error process output constant. */
//    public static final int                 ERR_OUT = 2;

    
    // variables ...............................................................
    
    protected InputOutput                   debuggerIO = null;
    private OutputWriter                    debuggerOut;
    private String                          name;
    private boolean                         closed = false;
    
    /** output writer Thread */
    private Hashtable                       lines = new Hashtable ();
    private Listener                        listener = new Listener ();

    
    // init ....................................................................
    
    public IOManager(String title) {
        debuggerIO = IOProvider.getDefault ().getIO (title, true);
        debuggerIO.setFocusTaken (false);
        debuggerOut = debuggerIO.getOut ();
        debuggerIO.select();
    }
    
    
    // public interface ........................................................

    private LinkedList buffer = new LinkedList ();
    private RequestProcessor.Task task;
    
    /**
     * Prints given text to the output.
     */
    public void println (
        String text, 
        Line line
    ) {
        println(text, line, false);
    }
    
    /**
     * Prints given text to the output.
     */
    public void println (
        String text, 
        Line line,
        boolean important
    ) {
        if (text == null)
            throw new NullPointerException ();
        synchronized (buffer) {
            buffer.addLast (new Text (text, line, important));
        }
        if (task == null)
            task = RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    synchronized (buffer) {
                        int i, k = buffer.size ();
                        for (i = 0; i < k; i++) {
                            Text t = (Text) buffer.removeFirst ();
                            try {
                                //if ((t.where & DEBUGGER_OUT) != 0) {
                                    if (t.line != null) {
                                        debuggerOut.println (t.text, listener, t.important);
                                        if (t.important) {
                                            debuggerIO.select();
                                        }
                                        lines.put (t.text, t.line);
                                    } else
                                        debuggerOut.println (t.text);
                                    debuggerOut.flush ();
                                    if (closed)
                                        debuggerOut.close ();
                                //}
                               // if ((t.where & STATUS_OUT) != 0) 
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

    void closeStream () {
        debuggerOut.close ();
        closed = true;
    }

    void close () {
        debuggerIO.closeInputOutput ();
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
    
    private static class Text {
        private String text;
        private Line line;
        private boolean important;
        
        private Text (String text, Line line, boolean important) {
            this.text = text;
            this.line = line;
            this.important = important;
        }
    }
    
    static class Line {
        private String url;
        private int lineNumber;
        private JPDADebugger debugger;
        
        Line (String url, int lineNumber, JPDADebugger debugger) {
            this.url = url;
            this.lineNumber = lineNumber;
        }
        
        void show () {
            EditorContextBridge.showSource (url, lineNumber, debugger);
        }
    }
}
