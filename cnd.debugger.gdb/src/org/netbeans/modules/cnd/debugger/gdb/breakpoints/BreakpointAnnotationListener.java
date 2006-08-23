/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Micro//S ystems, Inc. Portions Copyright 1997-2006 Sun
 * Micro//S ystems, Inc. All Rights Reserved.
 */

/*
 * BreakpointAnnotationListener.java
 *
 * @author Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.modules.cnd.debugger.gdb.*;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;




/**
 * Listens on {@org.netbeans.api.debugger.DebuggerManager} on
 * {@link org.netbeans.api.debugger.DebuggerManager#PROP_BREAKPOINTS} 
 * property and annotates GDB Debugger line breakpoints in NetBeans editor.
 * It manages list of line breakpoint annotations for ToggleBreakpointPerformer
 * and BreakpointsUpdated too.
 */
public class BreakpointAnnotationListener extends DebuggerManagerAdapter {
    
    private HashMap breakpointToAnnotation = new HashMap();
    private boolean listen = true;
    
 
    public String[] getProperties() {
        return new String[] {DebuggerManager.PROP_BREAKPOINTS};
    }
    
    /**
     * Listens on breakpoint.
     */
    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        if (propertyName == null) return;
        if (!listen) return;
        if ( (!propertyName.equals(LineBreakpoint.PROP_CONDITION)) &&
             (!propertyName.equals(LineBreakpoint.PROP_URL)) &&
             (!propertyName.equals(LineBreakpoint.PROP_LINE_NUMBER)) &&
             (!propertyName.equals(GdbBreakpoint.PROP_ENABLED))
        ) return;
        LineBreakpoint b = (LineBreakpoint) e.getSource();
        annotate(b);
    }

    /**
    * Called when some breakpoint is added.
    *
    * @param b breakpoint
    */
    public void breakpointAdded(Breakpoint b) {
        if (b instanceof LineBreakpoint) {
            ((LineBreakpoint) b).addPropertyChangeListener(this);
            annotate((LineBreakpoint) b);
        }
    }

    /**
    * Called when some breakpoint is removed.
    *
    * @param breakpoint
    */
    public void breakpointRemoved(Breakpoint b) {
        if (b instanceof LineBreakpoint) {
            ((LineBreakpoint) b).removePropertyChangeListener(this);
            removeAnnotation((LineBreakpoint) b);
        }
    }
    
    public LineBreakpoint findBreakpoint(String url, int lineNumber) {
        Iterator i = breakpointToAnnotation.keySet().iterator();
        while (i.hasNext()) {
            LineBreakpoint lb = (LineBreakpoint) i.next();
            if (!lb.getURL().equals(url)) continue;
            Object annotation = breakpointToAnnotation.get(lb);
            int ln = EditorContextBridge.getContext().getLineNumber(annotation, null);
            if (ln == lineNumber) return lb;
        }
        return null;
    }

    
    // helper methods ..........................................................
    
    private void annotate(LineBreakpoint b) {
        // remove old annotation
        Object annotation = breakpointToAnnotation.get(b);
        if (annotation != null)
            EditorContextBridge.getContext().removeAnnotation(annotation);
        if (b.isHidden()) return;
        
        // add new one
        annotation = EditorContextBridge.annotate(b);
        if (annotation == null) return;
        breakpointToAnnotation.put(b, annotation);
        
        DebuggerEngine dm = DebuggerManager.getDebuggerManager().getCurrentEngine();
        Object timeStamp = null;
        if (dm != null)
            timeStamp = dm.lookupFirst(null, GdbDebugger.class);
        update(b, timeStamp);
    }
    
    public void updateLineBreakpoints() {
        Iterator it = breakpointToAnnotation.keySet().iterator(); 
        while (it.hasNext()) {
            LineBreakpoint lb = (LineBreakpoint) it.next();
            update(lb, null);
        }
    }
    
    private void update(LineBreakpoint b, Object timeStamp) {
        Object annotation = breakpointToAnnotation.get(b);
        int ln = EditorContextBridge.getContext().getLineNumber(annotation, timeStamp);
        listen = false;
        b.setLineNumber (ln);
        listen = true;
    }
    
    private void removeAnnotation(LineBreakpoint b) {
        Object annotation = breakpointToAnnotation.remove(b);
        if (annotation != null)
            EditorContextBridge.getContext().removeAnnotation(annotation);
    }
}
