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
 * Software is Sun Micro//S ystems, Inc. Portions Copyright 1997-2006 Sun
 * Micro//S ystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.debug;

import java.beans.*;
import java.util.HashMap;
import java.util.Iterator;

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
    
    private HashMap breakpointToAnnotation = new HashMap ();
    private boolean listen = true;
    
    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_BREAKPOINTS};
    }
    
    /**
     * Listens on breakpoint.
     */
    public void propertyChange (PropertyChangeEvent e) {
        String propertyName = e.getPropertyName ();
        if (propertyName == null) return;
        if (!listen) return;
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

    public JspLineBreakpoint findBreakpoint (String url, int lineNumber) {
        Iterator i = breakpointToAnnotation.keySet ().iterator ();
        while (i.hasNext ()) {
            JspLineBreakpoint lb = (JspLineBreakpoint) i.next ();
            if (!lb.getURL ().equals (url)) continue;
            Object annotation = breakpointToAnnotation.get (lb);
            int ln = Context.getLineNumber (annotation, null);
            if (ln == lineNumber) return lb;
        }
        return null;
    }
    
    // helper methods ..........................................................
    
    private void annotate (JspLineBreakpoint b) {
        // remove old annotation
        Object annotation = breakpointToAnnotation.get (b);
        if (annotation != null)
            Context.removeAnnotation (annotation);
        if (b.isHidden ()) return;
        
        // add new one
        annotation = Context.annotate (b);
        if (annotation == null)
            return;
        
        breakpointToAnnotation.put (b, annotation);
        
        DebuggerEngine de = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        Object timeStamp = null;
        if (de != null)
            timeStamp = de.lookupFirst (null, JPDADebugger.class);
        update (b, timeStamp);        
    }

    public void updateJspLineBreakpoints () {
        Iterator it = breakpointToAnnotation.keySet ().iterator (); 
        while (it.hasNext ()) {
            JspLineBreakpoint lb = (JspLineBreakpoint) it.next ();
            update (lb, null);
        }
    }
    
    private void update (JspLineBreakpoint b, Object timeStamp) {
        Object annotation = breakpointToAnnotation.get (b);
        if (annotation == null) 
            return;
        int ln = Context.getLineNumber (annotation, timeStamp);
        listen = false;
        b.setLineNumber (ln);
        listen = true;
    }
    
    private void removeAnnotation(JspLineBreakpoint b) {
        Object annotation = breakpointToAnnotation.remove (b);
        if (annotation != null)
            Context.removeAnnotation (annotation);
    }
}
