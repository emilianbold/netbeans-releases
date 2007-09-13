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

package org.netbeans.modules.ruby.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.modules.ruby.debugger.DebuggerAnnotation;
import org.openide.text.Annotation;

/**
 * @author Martin Krauskopf
 */
public final class BreakpointAnnotationListener extends DebuggerManagerAdapter
        implements PropertyChangeListener {
    
    private Map<Breakpoint, Annotation> breakpointToAnnotation
            = new HashMap<Breakpoint, Annotation>();
    
    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_BREAKPOINTS };
    }
    
    @Override
    public void breakpointAdded(final Breakpoint b) {
        if (!(b instanceof RubyBreakpoint)) return;
        addAnnotation(b);
    }
    
    @Override
    public void breakpointRemoved(final Breakpoint b) {
        if (!(b instanceof RubyBreakpoint)) return;
        removeAnnotation(b);
    }
    
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (propName == Breakpoint.PROP_ENABLED || propName == RubyBreakpoint.PROP_UPDATED) {
            removeAnnotation((Breakpoint) evt.getSource());
            addAnnotation((Breakpoint) evt.getSource());
        }
    }
    
    private void addAnnotation(final Breakpoint b) {
        Annotation debugAnnotation = new DebuggerAnnotation(
                b.isEnabled() ? DebuggerAnnotation.BREAKPOINT_ANNOTATION_TYPE : DebuggerAnnotation.DISABLED_BREAKPOINT_ANNOTATION_TYPE,
                ((RubyBreakpoint) b).getLine());
        breakpointToAnnotation.put(b, debugAnnotation);
        b.addPropertyChangeListener(this);
    }
    
    private void removeAnnotation(Breakpoint b) {
        Annotation annotation = breakpointToAnnotation.remove(b);
        if (annotation == null) return;
        annotation.detach();
        b.removePropertyChangeListener(this);
    }
    
}
