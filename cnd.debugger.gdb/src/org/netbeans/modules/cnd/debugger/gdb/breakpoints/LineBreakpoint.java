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
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString() {
        return "LineBreakpoint " + getURL() + " : " + getLineNumber(); // NOI18N
    }
    
    private static class LineBreakpointComparable extends LineBreakpoint implements Comparable {
        
        public LineBreakpointComparable() {
        }
        
        public int compareTo(Object o) {
            if (o instanceof LineBreakpointComparable) {
                LineBreakpoint lbthis = this;
                LineBreakpoint lb = (LineBreakpoint) o;
                int uc = lbthis.getURL().compareTo(lb.getURL());
                if (uc != 0) {
                    return uc;
                } else {
                    return lbthis.getLineNumber() - lb.getLineNumber();
                }
            } else {
                return -1;
            }
        }
    }
}
