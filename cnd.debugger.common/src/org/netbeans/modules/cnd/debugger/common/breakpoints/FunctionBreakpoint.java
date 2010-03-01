/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.debugger.common.breakpoints;

import org.openide.util.NbBundle;

/**
 * Notifies about function breakpoint events.
 *
 * @author Jan Jancura and Gordon Prieur
 */
public class FunctionBreakpoint extends CndBreakpoint {
    
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
        b.setPrintText(NbBundle.getBundle(FunctionBreakpoint.class).getString
                ("CTL_Function_Breakpoint_Print_Text")); // NOI18N
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
        String old = this.function;
        
        synchronized (this) {
            if (function == null) {
                function = ""; // NOI18N
            }
            // Let's try to help user to set "correct" function name
//            int i = function.indexOf(' ');
//            if (i > 0) {
                // Remove spaces
                //function = function.replaceAll(" ", ""); // NOI18N
//            }
            int i = function.indexOf("(void)"); // NOI18N
            if (i > 0) {
                // Replace "(void)" with "()"
                //function = function.replaceAll("(void)", "()"); // NOI18N
                function = function.substring(0, i+1) + function.substring(i+5);
            }
            if (function.equals(this.function)) {
                return;
            }
            this.function = function;
            firePropertyChange(PROP_FUNCTION_NAME, old, function);
        }
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
    @Override
    public String toString() {
        return "FunctionBreakpoint " + function; // NOI18N
    }
    
    private static class FunctionBreakpointComparable extends FunctionBreakpoint implements Comparable {
        public int compareTo(Object o) {
            if (o instanceof FunctionBreakpointComparable) {
                FunctionBreakpoint fbthis = this;
                FunctionBreakpoint fb = (FunctionBreakpoint) o;
                return fbthis.function.compareTo(fb.function);
            } else {
                return -1;
            }
        }
    }
}
