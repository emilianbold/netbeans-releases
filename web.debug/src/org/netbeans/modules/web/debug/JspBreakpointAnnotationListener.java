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
package org.netbeans.modules.web.debug;

import java.beans.*;
import java.util.HashMap;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;

import org.netbeans.modules.web.debug.breakpoints.*;

/**
 * Listens on {@org.netbeans.api.debugger.DebuggerManager} on
 * {@link org.netbeans.api.debugger.DebuggerManager#PROP_BREAKPOINTS} 
 * property and annotates 
 * JSP breakpoints in NetBeans editor.
 *
 * @author Martin Grebac
 */
public class JspBreakpointAnnotationListener extends DebuggerManagerAdapter {
    
    private static HashMap breakpointToAnnotation = new HashMap ();
    
    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_BREAKPOINTS};
    }
    
    /**
     * Listens on breakpoint.
     */
    public void propertyChange (PropertyChangeEvent e) {
        String propertyName = e.getPropertyName ();
        if (propertyName == null) return;
        if ( (!propertyName.equals (JspLineBreakpoint.PROP_CONDITION)) &&
             (!propertyName.equals (JspLineBreakpoint.PROP_URL)) &&
             (!propertyName.equals (JspLineBreakpoint.PROP_LINE_NUMBER)) &&
             (!propertyName.equals (JspLineBreakpoint.PROP_ENABLED))
        ) return;
        JspLineBreakpoint b = (JspLineBreakpoint) e.getSource ();
        annotate (b);
    }

    /**
    * Called when some breakpoint is added.
    *
    * @param b breakpoint
    */
    public void breakpointAdded (Breakpoint b) {
        if (b instanceof JspLineBreakpoint) {
            ((JspLineBreakpoint) b).addPropertyChangeListener (this);
            annotate ((JspLineBreakpoint) b);
        }
    }

    /**
    * Called when some breakpoint is removed.
    *
    * @param breakpoint
    */
    public void breakpointRemoved (Breakpoint b) {
        if (b instanceof JspLineBreakpoint) {
            ((JspLineBreakpoint) b).removePropertyChangeListener (this);
            removeAnnotation ((JspLineBreakpoint) b);
        }
    }

    
    // helper methods ..........................................................
    
    private static void annotate (JspLineBreakpoint b) {
        // remove old annotation
        Object annotation = breakpointToAnnotation.get (b);
        if (annotation != null)
            Context.removeAnnotation (annotation);
        if (b.isHidden ()) return;
        
        // add new one
        annotation = Context.annotate (b);
        breakpointToAnnotation.put (b, annotation);
    }
    
    private static void removeAnnotation(JspLineBreakpoint b) {
        Object annotation = breakpointToAnnotation.remove (b);
        if (annotation != null)
            Context.removeAnnotation (annotation);
    }
}
