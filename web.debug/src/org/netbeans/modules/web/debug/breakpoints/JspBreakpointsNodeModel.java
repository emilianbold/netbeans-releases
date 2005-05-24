/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.debug.breakpoints;


import org.netbeans.api.debugger.jpda.*;
import org.netbeans.modules.web.debug.Context;

import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.NbBundle;


/**
 * @author Martin Grebac
 */
public class JspBreakpointsNodeModel implements NodeModel {

    public static final String LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint";
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o instanceof JspLineBreakpoint) {
            JspLineBreakpoint b = (JspLineBreakpoint) o;
            return NbBundle.getMessage (JspBreakpointsNodeModel.class,
                    "CTL_Jsp_Line_Breakpoint",
                    Context.getFileName (b),
                    "" + b.getLineNumber()
                );
        } 
        throw new UnknownTypeException(o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o instanceof JspLineBreakpoint) {
            return NbBundle.getMessage (
                    JspBreakpointsNodeModel.class,
                    "CTL_Jsp_Line_Breakpoint",
                    Context.getFileName ((JspLineBreakpoint) o),
                    "" + ((JspLineBreakpoint) o).getLineNumber ()
                );
        }
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o instanceof JspLineBreakpoint) {
            return LINE_BREAKPOINT;
        }
        throw new UnknownTypeException (o);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
//        listeners.add (l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
//        listeners.remove (l);
    }
    
//    private void fireTreeChanged () {
//        Vector v = (Vector) listeners.clone ();
//        int i, k = v.size ();
//        for (i = 0; i < k; i++)
//            ((TreeModelListener) v.get (i)).treeChanged ();
//    }
//    
//    private void fireTreeNodeChanged (Object parent) {
//        Vector v = (Vector) listeners.clone ();
//        int i, k = v.size ();
//        for (i = 0; i < k; i++)
//            ((TreeModelListener) v.get (i)).treeNodeChanged (parent);
//    }
    
//    static String getShort (String s) {
//        if (s.indexOf ('*') >= 0) return s;
//        int i = s.lastIndexOf ('.');
//        if (i < 0) return s;
//        return s.substring (i + 1);
//    }
}
