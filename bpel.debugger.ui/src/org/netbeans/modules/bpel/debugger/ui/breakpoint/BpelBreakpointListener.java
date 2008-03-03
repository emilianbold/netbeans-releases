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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.breakpoint;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.modules.bpel.debugger.api.AnnotationType;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;


/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.10.21
 */
public class BpelBreakpointListener extends DebuggerManagerAdapter {
    
    private Map<LineBreakpoint, Object> myBreakpointToAnnotation =
            new HashMap<LineBreakpoint, Object>();
    
    private Map<Object, LineBreakpoint> myAnnotationToBreakpoint =
            new HashMap<Object, LineBreakpoint>();
    
    private final AnnotationListener myAnnotationListener =
            new AnnotationListener();
    
    /**{@inheritDoc}*/
    @Override
    public void breakpointAdded(
            final Breakpoint breakpoint) {
        
        if (breakpoint instanceof LineBreakpoint) {
            final LineBreakpoint lbp = (LineBreakpoint) breakpoint;
            
            lbp.addPropertyChangeListener(Breakpoint.PROP_ENABLED, this);
            addAnnotation(lbp);
        }
    }
    
    @Override
    public Breakpoint[] initBreakpoints() {
        return new Breakpoint[0];
    }
    
    /**{@inheritDoc}*/
    @Override
    public void breakpointRemoved(
            final Breakpoint breakpoint) {
        
        if (breakpoint instanceof LineBreakpoint) {
            final LineBreakpoint lbp = (LineBreakpoint) breakpoint;
            
            lbp.removePropertyChangeListener(Breakpoint.PROP_ENABLED, this);
            removeAnnotation(lbp);
        }
    }
    
    /**{@inheritDoc}*/
    @Override
    public void propertyChange(
            final PropertyChangeEvent event) {
        if (Breakpoint.PROP_ENABLED.equals(event.getPropertyName())) {
            addAnnotation((LineBreakpoint) event.getSource());
        }
    }
    
    /**{@inheritDoc}*/
    @Override
    public String[] getProperties() {
        return new String[] {
            DebuggerManager.PROP_BREAKPOINTS_INIT,
            DebuggerManager.PROP_BREAKPOINTS };
    }
    
    public synchronized List<LineBreakpoint> getBreakpoints() {
        return new ArrayList<LineBreakpoint>(myBreakpointToAnnotation.keySet());
    }
    
    public synchronized LineBreakpoint findBreakpoint(
            final String url, 
            final String xpath, 
            final int lineNumber) {
        
        final String realUrl = url.replace("\\", "/");
        
        final Iterator iterator = 
                myBreakpointToAnnotation.keySet().iterator ();
        while (iterator.hasNext()) {
            final LineBreakpoint lb = (LineBreakpoint) iterator.next();
            
            if (!lb.getURL().equals(realUrl)) {
                continue;
            }
            
            final Object annotation = myBreakpointToAnnotation.get(lb);
            
            final String bpXpath = 
                    EditorContextBridge.getXpath(annotation);
            final int bpLineNumber = 
                    EditorContextBridge.getLineNumber(annotation);
            if (((xpath != null) && xpath.equals(bpXpath)) || 
                    (lineNumber == bpLineNumber)) {
                return lb;
            }
        }
        
        return null;
    }
    
    public Object findAnnotation(
            final LineBreakpoint breakpoint) {
        return myBreakpointToAnnotation.get(breakpoint);
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private synchronized void addAnnotation(
            final LineBreakpoint breakpoint) {
        
        AnnotationType annotationType;
        
        Object annotation = myBreakpointToAnnotation.get(breakpoint);
        if (annotation != null) {
            EditorContextBridge.removeAnnotation(annotation);
        }
        
        if (breakpoint.getXpath() != null) {
            if (breakpoint.isEnabled()) {
                annotationType = AnnotationType.ENABLED_BREAKPOINT;
            } else {
                annotationType = AnnotationType.DISABLED_BREAKPOINT;
            }
        } else {
            annotationType = AnnotationType.BROKEN_BREAKPOINT;
        }
        
        annotation = EditorContextBridge.addAnnotation(
                breakpoint.getURL(),
                breakpoint.getXpath(),
                breakpoint.getLineNumber(),
                annotationType);
        
        if (annotation == null) {
            return;
        }
        
        EditorContextBridge.addAnnotationListener(
                annotation, myAnnotationListener);
        
        myBreakpointToAnnotation.put(breakpoint, annotation);
        myAnnotationToBreakpoint.put(annotation, breakpoint);
    }
    
    private synchronized void removeAnnotation(
            final LineBreakpoint breakpoint) {
        
        final Object annotation = myBreakpointToAnnotation.remove(breakpoint);
        
        if (annotation != null) {
            myAnnotationToBreakpoint.remove(annotation);
            EditorContextBridge.removeAnnotationListener(
                    annotation, 
                    myAnnotationListener);
            EditorContextBridge.removeAnnotation(annotation);
        }
    }

    private void updateBreakpointByAnnotation(
            final Object annotation) {
        
        LineBreakpoint lbp = null;
        synchronized (this) {
            lbp = (LineBreakpoint) myAnnotationToBreakpoint.get(annotation);
        }
        
        if (lbp == null) {
            return;
        }
        
        if (!EditorContextBridge.isValid(annotation)) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(lbp);
            return;
        }
        
        // if the annotation is still attached, then we need to update the 
        // breakpoint
        if (EditorContextBridge.isAttached(annotation)) {
            final String xpath = 
                    EditorContextBridge.getXpath(annotation);
            final int lineNumber = 
                    EditorContextBridge.getLineNumber(annotation);

            final LineBreakpoint existing = 
                    findBreakpoint(lbp.getURL(), xpath, lineNumber);

            if ((existing != null) && !existing.equals(lbp)) {
                DebuggerManager.getDebuggerManager().removeBreakpoint(lbp);
            } else {
                lbp.setXpath(xpath);
                lbp.setLineNumber(lineNumber);

                lbp.touch();
            }
        }
    }
    
    // Inner Classes ///////////////////////////////////////////////////////////
    private class AnnotationListener implements PropertyChangeListener {
        public void propertyChange(
                final PropertyChangeEvent evt) {
            
            updateBreakpointByAnnotation(evt.getSource());
        }
    }
}
