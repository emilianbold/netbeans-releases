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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
    
    /** Property name constant */
    public static final String          PROP_LINE_NUMBER = "lineNumber"; // NOI18N
    /** Property name constant */
    public static final String          PROP_URL = "url"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_FUNCTION_NAME = "functionName"; // NOI18N
    /** Property name constant */
    public static final String          PROP_CONDITION = "condition"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_BREAKPOINT_TYPE = "breakpointType"; // NOI18N
    /** Property name constant. */
    public static final int             TYPE_FUNCTION_ENTRY = 1;
    /** Property name constant. */
    public static final int             TYPE_FUNCTION_EXIT = 2;
    
    private String                      function = "";  // NOI18N
    private String                      condition = ""; // NOI18N
    private String                      url = "";       // NOI18N
    private String                      path = "";      // NOI18N
    private int                         lineNumber;
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
	b.setID();
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
        firePropertyChange(PROP_FUNCTION_NAME, old, function);
    }
    
    /**
     * Gets file name in URL format.
     *
     * @return file name in URL format or empty string
     */
    public String getURL() {
        return url;
    }
    
    /**
     * Sets file name in URL format.
     *
     * @param file name
     */
    public void setURL(String url) {
        String old;
        synchronized (this) {
            if ((url == this.url) ||
                    ((url != null) && (this.url != null) && url.equals(this.url))) {
                return;
            }
            // The code below is a temporary protection
            // against "invalid" URL values.
            url = url.replace(" ", "%20"); // NOI18N
            if (!url.startsWith("file:/")) { // NOI18N
                if (url.startsWith("/")) { // NOI18N
                    url = "file:" + url; // NOI18N
                } else {
                    url = "file:/" + url; // NOI18N
                }
            }
            
            // Also set the path variable, based on the URL.
            try {
                assert(!(url == null && Boolean.getBoolean("gdb.assertions.enabled"))); // NOI18N
                FileObject fo = URLMapper.findFileObject(new URL(url));
                if (fo != null) {
                    if (Utilities.isWindows()) {
                        path = fo.getPath();
                    } else {
                        path = "/" + fo.getPath();
                    }
                }
            } catch (MalformedURLException mue) {
                assert !Boolean.getBoolean("gdb.assertions.enabled");
                return;
            } catch (Exception ex) {
                assert !Boolean.getBoolean("gdb.assertions.enabled");
            }
            old = this.url;
            this.url = url;
        }
        firePropertyChange(PROP_URL, old, url);
    }
    
    /**
     *  Return a path based on this breakpoints URL. The path is not necessarily the
     *  same as the URL with the "File:/" removed. This is because Windows often substitues
     *  "%20" for spaces. It also puts a "/" before the drive specifier.
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Gets number of line to stop on.
     *
     * @return line number to stop on
     */
    public int getLineNumber() {
        return lineNumber;
    }
    
    /**
     * Sets number of line to stop on.
     *
     * @param ln a line number to stop on
     */
    public void setLineNumber(int ln) {
        int old;
        synchronized (this) {
            if (ln == lineNumber) {
                return;
            }
            old = lineNumber;
            lineNumber = ln;
        }
        firePropertyChange(PROP_LINE_NUMBER, new Integer(old), new Integer(ln));
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
     * Returns condition.
     *
     * @return cond a condition
     */
    public String getCondition() {
        return condition;
    }
    
    /**
     * Sets condition.
     *
     * @param c a new condition
     */
    public void setCondition(String c) {
        String old;
        synchronized (this) {
            if (c == null) {
                c = "";
            }
            c = c.trim();
            if ((c == condition) ||
                    ((c != null) && (condition != null) && condition.equals(c))) {
                return;
            }
            old = condition;
            condition = c;
        }
        firePropertyChange(PROP_CONDITION, old, c);
    }
    
    /**
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString() {
        return "FunctionBreakpoint " + function;
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
