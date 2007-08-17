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
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;

/**
 * Notifies about line breakpoint events.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerManager.addBreakpoint(LineBreakpoint.create("src/args.c", 12));
 * </pre>
 * This breakpoint stops in file args.c at line number 12.
 *
 * @author Gordon Prieur (copied from Jan Jancura's JPDA LineBreakpoint)
 */
public class LineBreakpoint extends GdbBreakpoint {

    public static final String          PROP_LINE_NUMBER = "lineNumber"; // NOI18N
    public static final String          PROP_URL = "url"; // NOI18N
    public static final String          PROP_CONDITION = "condition"; // NOI18N
    
    private final String                emptyString = ""; // NOI18N
    private String                      url = emptyString;
    private String                      path = emptyString;
    private int                         lineNumber;
    private String                      condition = emptyString;
    
    /**
     * Creates a new breakpoint for given parameters.
     *
     * @param url a url
     * @param lineNumber a line number
     * @return a new breakpoint for given parameters
     */
    public static LineBreakpoint create(String url, int lineNumber) {
        LineBreakpoint b = new LineBreakpointComparable();
        b.setURL(url);
        b.setLineNumber(lineNumber);
        return b;
    }

    /**
     * Gets name of class to stop on.
     *
     * @return name of class to stop on
     */
    public String getURL() {
        return url;
    }
    
    /**
     * Sets name of class to stop on.
     *
     * @param url the URL of class to stop on
     */
    public void setURL(String url) {
        String old;
        synchronized (this) {
            if ((url == this.url) ||
		     ((url != null) && (this.url != null) && url.equals(this.url))) {
		return;
	    }
            
            // Also set the path variable, based on the URL.
            try {
                assert(!(url == null && Boolean.getBoolean("gdb.assertions.enabled"))); // NOI18N
                FileObject fo = URLMapper.findFileObject(new URL(url));
                if (fo != null) {
		    if (Utilities.isWindows()) {
			path = fo.getPath();
		    } else {
			path = "/" + fo.getPath(); // NOI18N
		    }
		}
            } catch (MalformedURLException mue) {
                assert !Boolean.getBoolean("gdb.assertions.enabled"); // NOI18N
                return;
            } catch (Exception ex) {
                assert !Boolean.getBoolean("gdb.assertions.enabled"); // NOI18N
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
		c = emptyString;
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
        return "LineBreakpoint " + url + " : " + lineNumber; // NOI18N
    }
    
    private static class LineBreakpointComparable extends LineBreakpoint implements Comparable {
        
        public LineBreakpointComparable() {
        }
        
        public int compareTo(Object o) {
            if (o instanceof LineBreakpointComparable) {
                LineBreakpoint lbthis = this;
                LineBreakpoint lb = (LineBreakpoint) o;
                int uc = lbthis.url.compareTo(lb.url);
                if (uc != 0) {
                    return uc;
                } else {
                    return lbthis.lineNumber - lb.lineNumber;
                }
            } else {
                return -1;
            }
        }
    }
}
