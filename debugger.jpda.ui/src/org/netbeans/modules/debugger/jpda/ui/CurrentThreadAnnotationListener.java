/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Micro//S ystems, Inc. Portions Copyright 1997-2001 Sun
 * Micro//S ystems, Inc. All Rights Reserved.
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
        return (JPDADebugger) currentEngine.lookupFirst 
            (null, JPDADebugger.class);
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
            removeAnnotations ();
            return;
        }
        
        // 2) get call stack & Line
        CallStackFrame[] stack = new CallStackFrame [0];
        try {
            stack = currentThread.getCallStack ();
        } catch (AbsentInformationException ex) {
            removeAnnotations ();
            return;
        }
        final CallStackFrame csf = currentDebugger.getCurrentCallStackFrame ();
        final DebuggerEngine currentEngine = DebuggerManager.
            getDebuggerManager ().getCurrentEngine ();
        final Session currentSession = DebuggerManager.getDebuggerManager ().
            getCurrentSession ();
        final String language = currentSession == null ? 
            null : currentSession.getCurrentLanguage ();
        final SourcePath sourcePath = currentEngine == null ? 
            null : (SourcePath) currentEngine.lookupFirst 
                (null, SourcePath.class);

        // 3) annotate current line & stack
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                // show current line
                if (currentPC != null)
                    EditorContextBridge.removeAnnotation (currentPC);
                if (csf != null && sourcePath != null && currentThread != null) {
                    
                    sourcePath.showSource (csf, language);
                    // annotate current line
                    currentPC = sourcePath.annotate (currentThread, language);
                }
            }
        });
        annotateCallStack (stack, sourcePath);
    }


    // do not need synchronization, called from AWTThread!!!!
    private HashMap               stackAnnotations = new HashMap ();

    // currently waiting / running refresh task
    // there is at most one
    private RequestProcessor.Task task;

    private void removeAnnotations () {
        if (task != null) {
            // cancel old task
            task.cancel ();
            task = null;
        }
        task = RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                if (currentPC != null)
                    EditorContextBridge.removeAnnotation (currentPC);
                currentPC = null;
                Iterator i = stackAnnotations.values ().iterator ();
                while (i.hasNext ())
                    EditorContextBridge.removeAnnotation (i.next ());
                stackAnnotations.clear ();
            }
        }, 500);
    }

    private void annotateCallStack (
        final CallStackFrame[] stack,
        final SourcePath sourcePath
    ) {
        if (task != null) {
            // cancel old task
            task.cancel ();
            task = null;
        }
        task = RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                HashMap newAnnotations = new HashMap ();
                int i, k = stack.length;
                for (i = 1; i < k; i++) {

                    // 1) check Line
                    String language = stack[i].getDefaultStratum();                    
                    String resourceName = EditorContextBridge.getRelativePath
                        (stack [i], language);
                    int lineNumber = stack [i].getLineNumber (language);
                    String line = resourceName + lineNumber;

                    // 2) line already annotated?
                    if (newAnnotations.containsKey (line))
                        continue;

                    // 3) line has been annotated?
                    Object da = stackAnnotations.remove (line);
                    if (da == null) {
                        // line has not been annotated -> create annotation
                        da = sourcePath.annotate (stack [i], language);
                    }

                    // 4) add new line to hashMap
                    if (da != null)
                        newAnnotations.put (line, da);
                } // for

                // delete old anotations
                Iterator iter = stackAnnotations.values ().iterator ();
                while (iter.hasNext ())
                    EditorContextBridge.removeAnnotation (
                        iter.next ()
                    );
                stackAnnotations = newAnnotations;
            }
        }, 500);
    }
}
