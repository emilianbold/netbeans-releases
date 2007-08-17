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

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.FunctionBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.openide.util.Utilities;

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
        if (e.getSource() instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) e.getSource();
            annotate(lb);
            return;
        } 
        if (e.getSource() instanceof FunctionBreakpoint) {
            FunctionBreakpoint fb = (FunctionBreakpoint) e.getSource();
            annotate(fb);
            return;
        }
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
        if (b instanceof FunctionBreakpoint) {
            ((FunctionBreakpoint) b).addPropertyChangeListener(this);
            // annotate((FunctionBreakpoint) b); // No line number yet.
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
        if (b instanceof FunctionBreakpoint) {
            FunctionBreakpoint fb = (FunctionBreakpoint) b;
            fb.removePropertyChangeListener(this);
            removeAnnotation(fb);
            int bpn = fb.getBreakpointNumber();
            if (bpn > 0) {
                GdbDebugger debugger = fb.getDebugger();
                if ((debugger != null) && (debugger.getGdbProxy() != null)){
                    debugger.getGdbProxy().break_delete(bpn);
                }
            }
        }
    }
    
    public GdbBreakpoint findBreakpoint(String url, int lineNumber) {
        Iterator i = breakpointToAnnotation.keySet().iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) o;
                if (!lb.getURL().equals(url)) continue;
                Object annotation = breakpointToAnnotation.get(lb);
                int ln = EditorContextBridge.getContext().getLineNumber(annotation, null);
                if (ln == lineNumber) return lb;
            }
            if (o instanceof FunctionBreakpoint) {
                FunctionBreakpoint fb = (FunctionBreakpoint) o;
                if (Utilities.isWindows()) {
                    boolean found = false;
                    if (fb.getURL().equals(url)) {
                        found = true;
                    } else {
                        // Drive letter is not case sensitive - let's try to ignore case
                        String fb_url = fb.getURL();
                        if ((fb_url.startsWith("file:/")) && (url.startsWith("file:/"))) { // NOI18N
                            if((fb_url.charAt(7) == ':') && (url.charAt(7) == ':')) {
                                String url_lc = url.substring(0, 8);
                                url_lc = url_lc.toLowerCase() + url.substring(8);
                                String fb_url_lc = fb_url.substring(0, 8);
                                fb_url_lc = fb_url_lc.toLowerCase() + fb_url.substring(8);
                                if (fb_url_lc.equals(url_lc)) 
                                    found = true;
                            }
                        }
                    }
                    if (!found) continue;
                } else {
                    if (!fb.getURL().equals(url)) continue;
                }
                Object annotation = breakpointToAnnotation.get(fb);
                int ln = EditorContextBridge.getContext().getLineNumber(annotation, null);
                if (ln == lineNumber) return fb;
            }
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
    
    /**
     * Method updateBreakpoints() is called from BreakpointsUpdater
     * when a new debugging session starts.
     */
    public void updateBreakpoints() {
        Iterator it = breakpointToAnnotation.keySet().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) o;
                update(lb, null);
            }
            // Ignore FunctionBreakpoint ?
            if (o instanceof FunctionBreakpoint) {
                FunctionBreakpoint fb = (FunctionBreakpoint) o;
                update(fb, null);
            }
        }
    }
    
    private void update(LineBreakpoint b, Object timeStamp) {
        Object annotation = breakpointToAnnotation.get(b);
        int ln = EditorContextBridge.getContext().getLineNumber(annotation, timeStamp);
        listen = false;
        b.setLineNumber(ln);
        listen = true;
    }
    
    private void removeAnnotation(LineBreakpoint b) {
        Object annotation = breakpointToAnnotation.remove(b);
        if (annotation != null)
            EditorContextBridge.getContext().removeAnnotation(annotation);
    }
    
    private void annotate(FunctionBreakpoint b) {
        // remove old annotation
        Object annotation = breakpointToAnnotation.get(b);
        if (annotation != null)
            EditorContextBridge.getContext().removeAnnotation(annotation);
        if (b.isHidden()) return;

        // check line number (optimization)
        int lineNumber = b.getLineNumber();
        if (lineNumber < 1) {
            return;
        }
        
        // add new one
        annotation = EditorContextBridge.annotate(b);
        if (annotation == null) return;
        breakpointToAnnotation.put(b, annotation);
        
        // update timestamp
        DebuggerEngine dm = DebuggerManager.getDebuggerManager().getCurrentEngine();
        Object timeStamp = null;
        if (dm != null)
            timeStamp = dm.lookupFirst(null, GdbDebugger.class);
        update(b, timeStamp);
    }
    
    private void update(FunctionBreakpoint b, Object timeStamp) {
        Object annotation = breakpointToAnnotation.get(b);
        int ln = EditorContextBridge.getContext().getLineNumber(annotation, timeStamp);
        listen = false;
        b.setLineNumber(ln);
        listen = true;
    }
    
    private void removeAnnotation(FunctionBreakpoint b) {
        Object annotation = breakpointToAnnotation.remove(b);
        if (annotation == null) {
            annotation = b.annotation;
        }
        if (annotation != null) {
            EditorContextBridge.getContext().removeAnnotation(annotation);
        }
    }
}
