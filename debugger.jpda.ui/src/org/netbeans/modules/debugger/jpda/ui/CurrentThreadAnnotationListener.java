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
import org.netbeans.spi.viewmodel.NoInformationException;

import org.openide.util.RequestProcessor;


/**
 * Listens on {@link org.netbeans.api.debugger.DebuggerManager} on
 * {@link JPDADebugger#PROP_CURRENT_THREAD}
 * property and annotates current line and call stack for
 * {@link org.netbeans.api.debugger.jpda.JPDAThread}s in NetBeans editor.
 *
 * @author Jan Jancura
 */
public class CurrentThreadAnnotationListener extends LazyDebuggerManagerListener
implements PropertyChangeListener {

    // annotation for current line
    private transient Object                currentPC;
    private JPDAThread                      currentThread;
    private JPDADebugger                    debugger;



    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_CURRENT_ENGINE};
    }

    /**
     * Listens JPDADebuggerEngineImpl and DebuggerManager.
     */
    public void propertyChange (PropertyChangeEvent e) {
//        if (e.getSource () instanceof JPDAThread) {
//            annotate ();
//        } else
        if (e.getPropertyName () == DebuggerManager.PROP_CURRENT_ENGINE) {
            if (debugger != null)
                debugger.removePropertyChangeListener (this);
            DebuggerEngine currentEngine = DebuggerManager.
                getDebuggerManager ().getCurrentEngine ();
            debugger = null;
            if (currentEngine != null)
                debugger = JPDADebugger.getJPDADebugger (currentEngine);
            if (debugger != null)
                debugger.addPropertyChangeListener (this);
            refreshCurrentThread ();
        } else
        if (e.getPropertyName () == JPDADebugger.PROP_CURRENT_THREAD) {
            refreshCurrentThread ();
        } else
        if ( (e.getPropertyName () == JPDADebugger.PROP_STATE) &&
             (debugger.getState () == debugger.STATE_STOPPED)) {
            annotate ();
        }
    }


    // helper methods ..........................................................

    private void refreshCurrentThread () {
        // get current thread
        JPDAThread t = null;
        JPDAThread newThread = null;
        if (debugger != null) newThread = debugger.getCurrentThread ();
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if ( (currentEngine != null) &&
             (newThread != null) &&
             (newThread instanceof JPDAThread) &&
             (currentEngine.lookupFirst (JPDADebugger.class) != null)
        )
            t = newThread;

        //if (t != currentThread) {
            // move listeners
            currentThread = t;
            annotate ();
        //}
    }

    /**
     * Annotates current thread or removes annotations.
     */
    private void annotate () {
        // 1) no current thread => remove annotations
        if (currentThread == null) {
            removeAnnotations ();
            return;
        }

        // 2) get call stack & Line
        CallStackFrame[] stack = new CallStackFrame [0];
        try {
            stack = currentThread.getCallStack ();
        } catch (NoInformationException ex) {
            return;
        }
        final JPDAThread ct = currentThread;
        final String language = DebuggerManager.getDebuggerManager ().
            getCurrentSession ().getCurrentLanguage ();

        // 3) annotate current line & stack
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                // show current line
                if (currentPC != null)
                    Context.removeAnnotation (currentPC);
                Session currentSession = DebuggerManager.getDebuggerManager ().
                    getCurrentSession();
                if (currentSession != null) {
                    EngineContext ectx = (EngineContext) currentSession.
                        lookupFirst (EngineContext.class);
                    ectx.showSource (ct, language);

                    // annotate current line
                    currentPC = ectx.annotate (ct, language);
                }
            }
        });
        annotateCallStack (stack);
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
                Context.removeAnnotation (currentPC);
                Iterator i = stackAnnotations.values ().iterator ();
                while (i.hasNext ())
                    Context.removeAnnotation (i.next ());
                stackAnnotations.clear ();
            }
        }, 500);
    }

    private void annotateCallStack (
        final CallStackFrame[] stack
    ) {
        if (task != null) {
            // cancel old task
            task.cancel ();
            task = null;
        }
        task = RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager ().
                    getCurrentEngine ();
                String language = DebuggerManager.getDebuggerManager ().
                    getCurrentSession ().getCurrentLanguage ();
                HashMap newAnnotations = new HashMap ();
                int i, k = stack.length;
                for (i = 1; i < k; i++) {

                    // 1) check Line
                    String resourceName = Context.getRelativePath
                        (stack [i], language);
                    int lineNumber = stack [i].getLineNumber (language);
//                    if ( !Context.getContext (currentEngine).canShowLine
//                           (resourceName, lineNumber))
//                        continue;
                    String line = resourceName + lineNumber;

                    // 2) line already annotated?
                    if (newAnnotations.containsKey (line))
                        continue;

                    // 3) line has been annotated?
                    Object da = stackAnnotations.remove (line);
                    if (da == null) {
                        // line has not been annotated -> create annotation
                        EngineContext ectx = (EngineContext) DebuggerManager.
                            getDebuggerManager ().getCurrentSession ().
                            lookupFirst (EngineContext.class);
                        da = ectx.annotate (stack [i], language);
//                        da = Context.annotate (stack [i], language);
                    }

                    // 4) add new line to hashMap
                    if (da != null)
                        newAnnotations.put (line, da);
                } // for

                // delete old anotations
                Iterator iter = stackAnnotations.values ().iterator ();
                while (iter.hasNext ())
                    Context.removeAnnotation (
                        iter.next ()
                    );
                stackAnnotations = newAnnotations;
            }
        }, 500);
    }
}
