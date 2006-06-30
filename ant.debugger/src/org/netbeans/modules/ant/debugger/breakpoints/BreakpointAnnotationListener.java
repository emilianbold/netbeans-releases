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

package org.netbeans.modules.ant.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.modules.ant.debugger.DebuggerAnnotation;


/**
 * Listens on {@org.netbeans.api.debugger.DebuggerManager} on
 * {@link org.netbeans.api.debugger.DebuggerManager#PROP_BREAKPOINTS}
 * property and annotates JPDA Debugger line breakpoints in NetBeans editor.
 *
 * @author Jan Jancura
 */
public class BreakpointAnnotationListener extends DebuggerManagerAdapter 
implements PropertyChangeListener {
    
    private Map breakpointToAnnotation = new HashMap ();
    
 
    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_BREAKPOINTS};
    }

    /**
    * Called when some breakpoint is added.
    *
    * @param b breakpoint
    */
    public void breakpointAdded (Breakpoint b) {
        if (! (b instanceof AntBreakpoint)) return;
        addAnnotation (b);
    }

    /**
    * Called when some breakpoint is removed.
    *
    * @param breakpoint
    */
    public void breakpointRemoved (Breakpoint b) {
        if (! (b instanceof AntBreakpoint)) return;
        removeAnnotation (b);
    }

    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source 
     *   	and the property that has changed.
     */

    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName () != Breakpoint.PROP_ENABLED) return;
        removeAnnotation ((Breakpoint) evt.getSource ());
        addAnnotation ((Breakpoint) evt.getSource ());
    }
    
    private void addAnnotation (Breakpoint b) {
        breakpointToAnnotation.put (
            b,
            new DebuggerAnnotation (
                b.isEnabled () ? 
                    DebuggerAnnotation.BREAKPOINT_ANNOTATION_TYPE :
                    DebuggerAnnotation.DISABLED_BREAKPOINT_ANNOTATION_TYPE, 
                ((AntBreakpoint) b).getLine ()
            )
        );
        b.addPropertyChangeListener (
            Breakpoint.PROP_ENABLED, 
            this
        );
    }
    
    private void removeAnnotation (Breakpoint b) {
        DebuggerAnnotation annotation = (DebuggerAnnotation) 
            breakpointToAnnotation.remove (b);
        if (annotation == null) return;
        annotation.detach ();
        b.removePropertyChangeListener (
            Breakpoint.PROP_ENABLED, 
            this
        );
    }
}
