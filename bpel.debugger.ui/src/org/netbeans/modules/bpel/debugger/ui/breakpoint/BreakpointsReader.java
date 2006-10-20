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

package org.netbeans.modules.bpel.debugger.ui.breakpoint;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.bpel.debugger.api.breakpoints.BpelBreakpoint;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;

/**
 *
 * @author Alexander Zgursky
 */
public class BreakpointsReader implements Properties.Reader {


    public String [] getSupportedClassNames() {
        return new String[] {
            BpelBreakpoint.class.getName() 
        };
    }
    
    public Object read(String typeID, Properties properties) {
        BpelBreakpoint b = null;
        
        if (typeID.equals (LineBreakpoint.class.getName())) {
            LineBreakpoint lb = LineBreakpoint.create(
                properties.getString(LineBreakpoint.PROP_URL, null),
                properties.getString(LineBreakpoint.PROP_XPATH, null)
            );
            b = lb;
        }
        assert b != null: "Unknown breakpoint type: \""+typeID+"\"";
        b.setGroupName(
            properties.getString(Breakpoint.PROP_GROUP_NAME, "")
        );
        if (properties.getBoolean (Breakpoint.PROP_ENABLED, true)) {
            b.enable();
        } else {
            b.disable();
        }
        return b;
    }
    
    public void write (Object object, Properties properties) {
        BpelBreakpoint b = (BpelBreakpoint) object;
        properties.setString (
            Breakpoint.PROP_GROUP_NAME, 
            b.getGroupName ()
        );
        properties.setBoolean (Breakpoint.PROP_ENABLED, b.isEnabled ());
        
        if (object instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) object;
            properties.setString (LineBreakpoint.PROP_URL, lb.getURL ());
            properties.setString (LineBreakpoint.PROP_XPATH, lb.getXpath());
        }
    }
}
