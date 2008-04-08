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
 * Software is Sun Micro//S ystems, Inc. Portions Copyright 1997-2006 Sun
 * Micro//S ystems, Inc. All Rights Reserved.
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
package org.netbeans.modules.debugger.jpda.ui;

import com.sun.jdi.AbsentInformationException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.openide.ErrorManager;

import org.openide.util.RequestProcessor;


/**
 * Listens on {@link org.netbeans.api.debugger.DebuggerManager} on
 * {@link JPDADebugger#PROP_CURRENT_THREAD}
 * property and annotates current line and call stack for
 * {@link org.netbeans.api.debugger.jpda.JPDAThread}s in NetBeans editor.
 *
 * @author Jan Jancura
 */
public class CurrentThreadAnnotationListener extends DebuggerManagerAdapter {

    // annotation for current line
    private transient Object                currentPC;
    private transient Object                currentPCLock = new Object();
    private transient boolean               currentPCSet = false;
    private JPDAThread                      currentThread;
    private JPDADebugger                    currentDebugger;



    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_CURRENT_ENGINE};
    }

    /**
     * Listens JPDADebuggerEngineImpl and DebuggerManager.
     */
    public void propertyChange (PropertyChangeEvent e) {
        if (e.getPropertyName () == DebuggerManager.PROP_CURRENT_ENGINE) {
            updateCurrentDebugger ();
            updateCurrentThread ();
            annotate ();
        } else
        if (e.getPropertyName () == JPDADebugger.PROP_CURRENT_THREAD) {
            updateCurrentThread ();
            annotate ();
        } else
        if (e.getPropertyName () == JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME) {
            updateCurrentThread ();
            annotate ();
        } else
        if (e.getPropertyName () == JPDADebugger.PROP_STATE) {
            annotate ();
        }
    }


    // helper methods ..........................................................

    private void updateCurrentDebugger () {
        JPDADebugger newDebugger = getCurrentDebugger ();
        if (currentDebugger == newDebugger) return;
        if (currentDebugger != null)
            currentDebugger.removePropertyChangeListener (this);
        if (newDebugger != null)
            newDebugger.addPropertyChangeListener (this);
        currentDebugger = newDebugger;
    }
    
    private static JPDADebugger getCurrentDebugger () {
        DebuggerEngine currentEngine = DebuggerManager.
            getDebuggerManager ().getCurrentEngine ();
        if (currentEngine == null) return null;
        return currentEngine.lookupFirst(null, JPDADebugger.class);
    }

    private void updateCurrentThread () {
        // get current thread
        if (currentDebugger != null) 
            currentThread = currentDebugger.getCurrentThread ();
        else
            currentThread = null;
    }

    /**
     * Annotates current thread or removes annotations.
     */
    private void annotate () {
        // 1) no current thread => remove annotations
        if ( (currentThread == null) ||
             (currentDebugger.getState () != JPDADebugger.STATE_STOPPED) ) {
            synchronized (currentPCLock) {
                currentPCSet = false; // The annotation is goint to be removed
            }
            removeAnnotations ();
            return;
        }
        
        // 2) get call stack & Line
        CallStackFrame[] stack;
        try {
            stack = currentThread.getCallStack ();
        } catch (AbsentInformationException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            synchronized (currentPCLock) {
                currentPCSet = false; // The annotation is goint to be removed
            }
            removeAnnotations ();
            return;
        }
        final CallStackFrame csf = currentDebugger.getCurrentCallStackFrame ();
        Session currentSession = null;
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        for (int i = 0; i < sessions.length; i++) {
            if (sessions[i].lookupFirst(null, JPDADebugger.class) == currentDebugger) {
                currentSession = sessions[i];
                break;
            }
        }
        final String language = currentSession == null ? 
            null : currentSession.getCurrentLanguage ();
        DebuggerEngine currentEngine = (currentSession == null) ?
            null : currentSession.getCurrentEngine();
        final SourcePath sourcePath = (currentEngine == null) ? 
            null : currentEngine.lookupFirst(null, SourcePath.class);

        // 3) annotate current line & stack
        synchronized (currentPCLock) {
            currentPCSet = true; // The annotation is goint to be set
        }
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                // show current line
                synchronized (currentPCLock) {
                    if (currentPC != null)
                        EditorContextBridge.getContext().removeAnnotation (currentPC);
                    if (csf != null && sourcePath != null && currentThread != null) {

                        sourcePath.showSource (csf, language);
                        // annotate current line
                        currentPC = sourcePath.annotate (currentThread, language);
                    }
                }
            }
        });
        annotateCallStack (stack, sourcePath);
    }


    // do not need synchronization, called in a 1-way RP
    private HashMap               stackAnnotations = new HashMap ();
    
    private RequestProcessor rp = new RequestProcessor("Debugger Thread Annotation Refresher");

    // currently waiting / running refresh task
    // there is at most one
    private RequestProcessor.Task taskRemove;
    private RequestProcessor.Task taskAnnotate;
    private CallStackFrame[] stackToAnnotate;
    private SourcePath sourcePathToAnnotate;

    private void removeAnnotations () {
        synchronized (rp) {
            if (taskRemove == null) {
                taskRemove = rp.create (new Runnable () {
                    public void run () {
                        synchronized (currentPCLock) {
                            if (currentPCSet) {
                                // Keep the set PC
                                return ;
                            }
                            if (currentPC != null)
                                EditorContextBridge.getContext().removeAnnotation (currentPC);
                            currentPC = null;
                        }
                        Iterator i = stackAnnotations.values ().iterator ();
                        while (i.hasNext ())
                            EditorContextBridge.getContext().removeAnnotation (i.next ());
                        stackAnnotations.clear ();
                    }
                });
            }
        }
        taskRemove.schedule(500);
    }

    private void annotateCallStack (
        CallStackFrame[] stack,
        SourcePath sourcePath
    ) {
        synchronized (rp) {
            if (taskRemove != null) {
                taskRemove.cancel();
            }
            this.stackToAnnotate = stack;
            this.sourcePathToAnnotate = sourcePath;
            if (taskAnnotate == null) {
                taskAnnotate = rp.post (new Runnable () {
                    public void run () {
                        CallStackFrame[] stack;
                        SourcePath sourcePath;
                        synchronized (rp) {
                            if (stackToAnnotate == null) {
                                return ; // Nothing to do
                            }
                            stack = stackToAnnotate;
                            sourcePath = sourcePathToAnnotate;
                            stackToAnnotate = null;
                            sourcePathToAnnotate = null;
                        }
                        HashMap newAnnotations = new HashMap ();
                        int i, k = stack.length;
                        for (i = 1; i < k; i++) {

                            // 1) check Line
                            String language = stack[i].getDefaultStratum();                    
                            String resourceName = EditorContextBridge.getRelativePath
                                (stack[i], language);
                            int lineNumber = stack[i].getLineNumber (language);
                            String line = resourceName + lineNumber;

                            // 2) line already annotated?
                            if (newAnnotations.containsKey (line))
                                continue;

                            // 3) line has been annotated?
                            Object da = stackAnnotations.remove (line);
                            if (da == null) {
                                // line has not been annotated -> create annotation
                                da = sourcePath.annotate (stack[i], language);
                            }

                            // 4) add new line to hashMap
                            if (da != null)
                                newAnnotations.put (line, da);
                        } // for

                        // delete old anotations
                        Iterator iter = stackAnnotations.values ().iterator ();
                        while (iter.hasNext ())
                            EditorContextBridge.getContext().removeAnnotation (
                                iter.next ()
                            );
                        stackAnnotations = newAnnotations;
                    }
                });
            }
        }
        taskAnnotate.schedule(500);
    }
}
