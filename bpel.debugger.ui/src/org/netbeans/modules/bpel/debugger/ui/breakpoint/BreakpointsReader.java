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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.breakpoint;

import javax.xml.namespace.QName;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.bpel.debugger.api.breakpoints.BpelBreakpoint;
import org.netbeans.modules.bpel.debugger.api.breakpoints.BpelFaultBreakpoint;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;

/**
 *
 * @author Alexander Zgursky
 */
public class BreakpointsReader implements Properties.Reader {


    public String [] getSupportedClassNames() {
        return new String[] {
            LineBreakpoint.class.getName(),
            BpelFaultBreakpoint.class.getName()
        };
    }
    
    public Object read(String typeID, Properties properties) {
        BpelBreakpoint b = null;
        
        if (typeID.equals (LineBreakpoint.class.getName())) {
            b = LineBreakpoint.create(
                properties.getString(LineBreakpoint.PROP_URL, null),
                properties.getString(LineBreakpoint.PROP_XPATH, null),
                properties.getInt(LineBreakpoint.PROP_LINE_NUMBER, -1)
            );
        } else if (typeID.equals(BpelFaultBreakpoint.class.getName())) {
            String strProcQName = properties.getString(BpelFaultBreakpoint.PROP_PROCESS_QNAME, null);
            QName procQName = null;
            if (strProcQName != null && !strProcQName.trim().equals("")) {
                procQName = QName.valueOf(strProcQName);
            }
            
            String strFaultQName = properties.getString(BpelFaultBreakpoint.PROP_FAULT_QNAME, null);
            QName faultQName = null;
            if (strFaultQName != null && !strFaultQName.trim().equals("")) {
                faultQName = QName.valueOf(strFaultQName);
            }
            
            b = BpelFaultBreakpoint.create(procQName, faultQName);
        } else {
            assert false : "Unexpected breakpoint type: " + typeID;
        }
        
        b.setGroupName(
            properties.getString(Breakpoint.PROP_GROUP_NAME, "")
        );
        if (properties.getBoolean(Breakpoint.PROP_ENABLED, true)) {
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
            b.getGroupName()
        );
        properties.setBoolean (Breakpoint.PROP_ENABLED, b.isEnabled());
        
        if (object instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) object;
            properties.setString(LineBreakpoint.PROP_URL, lb.getURL());
            properties.setString(LineBreakpoint.PROP_XPATH, lb.getXpath());
            properties.setInt(LineBreakpoint.PROP_LINE_NUMBER, lb.getLineNumber());
        } else if (object instanceof BpelFaultBreakpoint) {
            BpelFaultBreakpoint fb = (BpelFaultBreakpoint)object;
            if (fb.getProcessQName() != null) {
                properties.setString(BpelFaultBreakpoint.PROP_PROCESS_QNAME, fb.getProcessQName().toString());
            }
            if (fb.getFaultQName() != null) {
                properties.setString(BpelFaultBreakpoint.PROP_FAULT_QNAME, fb.getFaultQName().toString());
            }
        }
    }
}
