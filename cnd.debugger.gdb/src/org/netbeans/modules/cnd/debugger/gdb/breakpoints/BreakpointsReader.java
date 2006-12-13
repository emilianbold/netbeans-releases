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

import java.util.Map;
import java.util.WeakHashMap;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.FunctionBreakpoint;

/**
 *
 * @author Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class BreakpointsReader implements Properties.Reader {


    public String [] getSupportedClassNames() {
        return new String[] {
            GdbBreakpoint.class.getName(), 
        };
    }
    
    public Object read (String typeID, Properties properties) {
        GdbBreakpoint b = null;
        // Read both LineBreakpoint and LineBreakpoint$LineBreakpointComparable
        if (typeID.equals(LineBreakpoint.class.getName()) ||
                        typeID.equals(LineBreakpoint.class.getName() + "$LineBreakpointComparable")) { // NOI18N
            LineBreakpoint lb = LineBreakpoint.create(properties.getString(LineBreakpoint.PROP_URL, null),
                        properties.getInt(LineBreakpoint.PROP_LINE_NUMBER, 1));
            lb.setCondition(properties.getString(LineBreakpoint.PROP_CONDITION, "")); // NOI18N
            b = lb;
        }
        if (typeID.equals (FunctionBreakpoint.class.getName()) ||
                        typeID.equals(FunctionBreakpoint.class.getName() + "$FunctionBreakpointComparable")) { // NOI18N
            FunctionBreakpoint fb = FunctionBreakpoint.create(""); // NOI18N
            fb.setFunctionName(properties.getString(FunctionBreakpoint.PROP_FUNCTION_NAME, "")); // NOI18N
            fb.setCondition(properties.getString (FunctionBreakpoint.PROP_CONDITION, "")); // NOI18N
            fb.setBreakpointType(properties.getInt(
                    FunctionBreakpoint.PROP_BREAKPOINT_TYPE, FunctionBreakpoint.TYPE_FUNCTION_ENTRY));
            b = fb;
        }
        
        assert b != null: "Unknown breakpoint type: \"" + typeID + "\""; // NOI18N
        b.setPrintText(properties.getString (GdbBreakpoint.PROP_PRINT_TEXT, "")); // NOI18N
        b.setGroupName(properties.getString (GdbBreakpoint.PROP_GROUP_NAME, "")); // NOI18N
        
        if (properties.getBoolean(GdbBreakpoint.PROP_ENABLED, true)) {
            b.enable();
        } else {
            b.disable();
        }
        return b;
    }
    
    public void write(Object object, Properties properties) {
        GdbBreakpoint b = (GdbBreakpoint) object;
        properties.setString(GdbBreakpoint.PROP_PRINT_TEXT, b.getPrintText());
        properties.setString(GdbBreakpoint.PROP_GROUP_NAME, b.getGroupName());
        properties.setInt(GdbBreakpoint.PROP_SUSPEND, b.getSuspend());
        properties.setBoolean(GdbBreakpoint.PROP_ENABLED, b.isEnabled());
        
        if (object instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) object;
            properties.setString(LineBreakpoint.PROP_URL, lb.getURL());
            properties.setInt(LineBreakpoint.PROP_LINE_NUMBER, lb.getLineNumber());
            properties.setString(LineBreakpoint.PROP_CONDITION, lb.getCondition());
        } else if (object instanceof FunctionBreakpoint) {
            FunctionBreakpoint fb = (FunctionBreakpoint) object;
            properties.setString(FunctionBreakpoint.PROP_FUNCTION_NAME, fb.getFunctionName());
            properties.setString(FunctionBreakpoint.PROP_CONDITION, fb.getCondition());
            properties.setInt(FunctionBreakpoint.PROP_BREAKPOINT_TYPE, fb.getBreakpointType());
        }
    }
}
