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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;

/**
 * Notifies about function breakpoint events.
 *
 * @author Jan Jancura and Gordon Prieur
 */
public class FunctionBreakpoint extends GdbBreakpoint {
    
    public static final String          PROP_FUNCTION_NAME = "functionName"; // NOI18N
    public static final String          PROP_BREAKPOINT_TYPE = "breakpointType"; // NOI18N
    public static final int             TYPE_FUNCTION_ENTRY = 1;
    public static final int             TYPE_FUNCTION_EXIT = 2;
    
    private String                      function = "";  // NOI18N
    private int                         type;
    
    private FunctionBreakpoint() {
    }
    
    /**
     * Creates a new breakpoint for given parameters.
     *
     * @param function a function name
     * @param lineNumber a line number
     * @return a new breakpoint for given parameters
     */
    public static FunctionBreakpoint create(String function) {
        FunctionBreakpoint b = new FunctionBreakpointComparable();
        b.setFunctionName(function);
        return b;
    }
    
    /**
     * Gets name of function to stop on.
     *
     * @return name of function to stop on
     */
    public String getFunctionName() {
        return function;
    }
    
    /**
     * Sets name of function to stop on.
     *
     * @param function the function to stop on
     */
    public void setFunctionName(String function) {
        String old;
        synchronized (this) {
            if (function == null) {
                function = ""; // NOI18N
            }
            // Let's try to help user to set "correct" function name
            int i = function.indexOf(' ');
            if (i > 0) {
                // Remove spaces
                function = function.replaceAll(" ", ""); // NOI18N
            }
            i = function.indexOf("(void)"); // NOI18N
            if (i > 0) {
                // Replace "(void)" with "()"
                //function = function.replaceAll("(void)", "()"); // NOI18N
                function = function.substring(0, i+1) + function.substring(i+5);
            }
            if (function.equals(this.function)) {
                return;
            }
            old = function;
            this.function = function;
        }
//        firePropertyChange(PROP_FUNCTION_NAME, old, function);
    }
    
    /**
     * Sets breakpoint type. This will be enter or exit of the function.
     *
     * @param type either TYPE_FUNCTION_ENTRY or TYPE_FUNCTION_EXIT
     */
    public void setBreakpointType(int type) {
        this.type = type;
    }
    
    
    /**
     * Sets breakpoint type. This will be enter or exit of the function.
     *
     * @param type either TYPE_FUNCTION_ENTRY or TYPE_FUNCTION_EXIT
     */
    public int getBreakpointType() {
        return type;
    }
    
    /**
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString() {
        return "FunctionBreakpoint " + function; // NOI18N
    }
    
    private static class FunctionBreakpointComparable extends FunctionBreakpoint implements Comparable {
        
        public FunctionBreakpointComparable() {
        }
        
        public int compareTo(Object o) {
            if (o instanceof FunctionBreakpointComparable) {
                FunctionBreakpoint lbthis = this;
                FunctionBreakpoint lb = (FunctionBreakpoint) o;
                return lbthis.function.compareTo(lb.function);
            } else {
                return -1;
            }
        }
    }
}
