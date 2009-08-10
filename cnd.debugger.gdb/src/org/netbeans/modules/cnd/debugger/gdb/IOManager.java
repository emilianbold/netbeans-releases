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

package org.netbeans.modules.cnd.debugger.gdb;

import org.netbeans.modules.cnd.debugger.common.EditorContextBridge;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;

import org.openide.awt.StatusDisplayer;
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
    
    private final InputOutput               debuggerIO;
    private final OutputWriter              debuggerOut;
    //private String                        name;
    private boolean                         closed = false;
    
    /** output writer Thread */
    private Hashtable<String, Line>           lines = new Hashtable<String, Line>();
    private final Listener                  listener = new Listener();
    
    
    // init ....................................................................
    
    public IOManager(String title) {
        debuggerIO = IOProvider.getDefault().getIO(title, true);
        debuggerIO.select();
        // IZ162493: no need to switch each time into Ouput window
//        io.setFocusTaken(true);
        debuggerOut = debuggerIO.getOut();
    }
    
    
    // public interface ........................................................

    private final LinkedList<Text> buffer = new LinkedList<Text>();
    private RequestProcessor.Task task;
    
    /**
     * Prints given text to the output.
     */
    public void println(final String text, final Line line) {
        if (text == null) {
            throw new NullPointerException();
        }
        synchronized (buffer) {
            buffer.addLast(new Text(text, line));
        }
        if (task == null) {
            task = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    synchronized (buffer) {
                        int i, k = buffer.size();
                        for (i = 0; i < k; i++) {
                            Text t = buffer.removeFirst();
                            try {
                                //if ((t.where & DEBUGGER_OUT) != 0) {
                                    if (t.line != null) {
                                        debuggerOut.println(t.text, listener);
                                        lines.put(t.text, t.line);
                                    } else {
                                        debuggerOut.println(t.text);
                                    }
                                    debuggerOut.flush();
                                    if (closed) {
                                        debuggerOut.close();
                                    }
                                //}
                               // if ((t.where & STATUS_OUT) != 0) 
                                    StatusDisplayer.getDefault().setStatusText(t.text);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }, 500, Thread.MIN_PRIORITY);
        } else {
            task.schedule(500);
        }
    }

    void closeStream() {
        debuggerOut.close();
        closed = true;
    }

    void close() {
        debuggerIO.closeInputOutput();
    }
    
    
    // innerclasses ............................................................
    
    private class Listener implements OutputListener {
        public void outputLineSelected(OutputEvent ev) {
        }
        public void outputLineAction(OutputEvent ev) {
            String t = ev.getLine();
            Line l = lines.get(t);
            if (l == null) {
                return;
            }
            l.show();
        }
        public void outputLineCleared(OutputEvent ev) {
            lines = new Hashtable<String, Line>();
        }
    }
    
    private static class Text {
        private final String text;
        private final Line line;
     //   private int where;
        
        private Text(String text, Line line) {
            this.text = text;
            //this.where = where;
            this.line = line;
        }
    }
    
    public static class Line {
        private final String url;
        private final int lineNumber;
        private final GdbDebugger debugger;
        
        Line(String url, int lineNumber, GdbDebugger debugger) {
            this.url = url;
            this.lineNumber = lineNumber;
            this.debugger = debugger;
        }
        
        void show() {
            EditorContextBridge.getContext().showSource(url, lineNumber, debugger);
        }
    }
}
