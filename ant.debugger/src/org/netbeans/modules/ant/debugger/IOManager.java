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

package org.netbeans.modules.ant.debugger;

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import javax.swing.SwingUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.text.Annotatable;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;


public class IOManager {

    // variables ...............................................................
    
    protected InputOutput                   debuggerIO = null;
    private OutputWriter                    debuggerOut;
    private String                          name;
    private boolean                         closed = false;

    
    /** output writer Thread */
    private Hashtable                       lines = new Hashtable ();
    private Listener                        listener = new Listener ();

    
    // init ....................................................................
    
    public IOManager (
        String title
    ) {
        debuggerIO = IOProvider.getDefault ().getIO (title, true);
        debuggerIO.setFocusTaken (false);
        debuggerOut = debuggerIO.getOut ();
    }
    
    
    // public interface ........................................................

    private LinkedList buffer = new LinkedList ();
    private RequestProcessor.Task task;
    
    /**
    * Prints given text to the output.
    */
    public void println (
        final String text, 
        final Object line
    ) {
        if (text == null)
            throw new NullPointerException ();
        synchronized (buffer) {
            buffer.addLast (new Text (text, line));
        }
        if (task == null)
            task = RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    synchronized (buffer) {
                        int i, k = buffer.size ();
                        for (i = 0; i < k; i++) {
                            Text t = (Text) buffer.removeFirst ();
                            try {
                                if (t.line != null) {
                                    debuggerOut.println (t.text, listener);
                                    lines.put (t.text, t.line);
                                } else
                                    debuggerOut.println (t.text);
                                debuggerOut.flush ();
                                if (closed)
                                    debuggerOut.close ();
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
        public void outputLineAction (final OutputEvent ev) {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    String t = ev.getLine ();
                    Object a = lines.get (t);
                    if (a == null) return;
                    Utils.showLine (a);
                }
            });
        }
        public void outputLineCleared (OutputEvent ev) {
            lines = new Hashtable ();
        }
    }
    
    private static class Text {
        private String text;
        private Object line;
        
        private Text (String text, Object line) {
            this.text = text;
            this.line = line;
        }
    }
}
