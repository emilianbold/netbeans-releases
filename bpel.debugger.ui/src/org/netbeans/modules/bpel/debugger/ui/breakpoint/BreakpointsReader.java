/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.bpel.debugger.ui.breakpoint;

import javax.xml.namespace.QName;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
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
                properties.getString(LineBreakpoint.PROP_XPATH, null)
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
            b.getGroupName ()
        );
        properties.setBoolean (Breakpoint.PROP_ENABLED, b.isEnabled ());
        
        if (object instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) object;
            properties.setString (LineBreakpoint.PROP_URL, lb.getURL ());
            properties.setString (LineBreakpoint.PROP_XPATH, lb.getXpath());
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
