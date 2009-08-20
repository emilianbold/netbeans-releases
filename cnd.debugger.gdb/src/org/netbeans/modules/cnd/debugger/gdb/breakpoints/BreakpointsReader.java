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

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;


import org.netbeans.modules.cnd.debugger.common.breakpoints.FunctionBreakpoint;
import org.netbeans.modules.cnd.debugger.common.breakpoints.AddressBreakpoint;
import org.netbeans.modules.cnd.debugger.common.breakpoints.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.common.breakpoints.CndBreakpoint;
import org.netbeans.api.debugger.Properties;

/**
 *
 * @author Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class BreakpointsReader implements Properties.Reader {

    public String [] getSupportedClassNames() {
        return new String[] {
            CndBreakpoint.class.getName(),
        };
    }
    
    public Object read(String typeID, Properties properties) {
        CndBreakpoint b = null;
        // Read both LineBreakpoint and LineBreakpoint$LineBreakpointComparable
        if (typeID.equals(LineBreakpoint.class.getName()) ||
                        typeID.equals(LineBreakpoint.class.getName() + "$LineBreakpointComparable")) { // NOI18N
            LineBreakpoint lb = LineBreakpoint.create(properties.getString(CndBreakpoint.PROP_URL, null),
                        properties.getInt(CndBreakpoint.PROP_LINE_NUMBER, 1));
            b = lb;
        }
        if (typeID.equals (FunctionBreakpoint.class.getName()) ||
                        typeID.equals(FunctionBreakpoint.class.getName() + "$FunctionBreakpointComparable")) { // NOI18N
            FunctionBreakpoint fb = FunctionBreakpoint.create(properties.getString(FunctionBreakpoint.PROP_FUNCTION_NAME, "")); // NOI18N
            fb.setBreakpointType(properties.getInt(
                    FunctionBreakpoint.PROP_BREAKPOINT_TYPE, FunctionBreakpoint.TYPE_FUNCTION_ENTRY));
            b = fb;
        }
        if (typeID.equals (AddressBreakpoint.class.getName()) ||
                        typeID.equals(AddressBreakpoint.class.getName() + "$AddressBreakpointComparable")) { // NOI18N
            AddressBreakpoint ab = AddressBreakpoint.create(properties.getString(AddressBreakpoint.PROP_ADDRESS_VALUE, "")); // NOI18N
            b = ab;
        }
        
        b.setCondition(properties.getString(CndBreakpoint.PROP_CONDITION, "")); // NOI18N
        b.setSkipCount(properties.getInt(CndBreakpoint.PROP_SKIP_COUNT, 0));
        b.setPrintText(properties.getString(CndBreakpoint.PROP_PRINT_TEXT, "")); // NOI18N
        b.setGroupName(properties.getString(CndBreakpoint.PROP_GROUP_NAME, "")); // NOI18N
        b.setSuspend(properties.getInt(CndBreakpoint.PROP_SUSPEND, CndBreakpoint.SUSPEND_ALL),
                properties.getString(CndBreakpoint.PROP_THREAD_ID, "1")); // NOI18N
        
        if (properties.getBoolean(CndBreakpoint.PROP_ENABLED, true)) {
            b.enable();
        } else {
            b.disable();
        }
        return b;
    }
    
    public void write(Object object, Properties properties) {
        CndBreakpoint b = (CndBreakpoint) object;
        properties.setString(CndBreakpoint.PROP_PRINT_TEXT, b.getPrintText());
        properties.setString(CndBreakpoint.PROP_GROUP_NAME, b.getGroupName());
        properties.setInt(CndBreakpoint.PROP_SUSPEND, b.getSuspend());
        properties.setString(CndBreakpoint.PROP_THREAD_ID, b.getThreadID());
        properties.setBoolean(CndBreakpoint.PROP_ENABLED, b.isEnabled());
        properties.setString(CndBreakpoint.PROP_CONDITION, b.getCondition());
        properties.setInt(CndBreakpoint.PROP_SKIP_COUNT, b.getSkipCount());
        
        if (object instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) object;
            properties.setString(CndBreakpoint.PROP_URL, lb.getURL());
            properties.setInt(CndBreakpoint.PROP_LINE_NUMBER, lb.getLineNumber());
        } else if (object instanceof FunctionBreakpoint) {
            FunctionBreakpoint fb = (FunctionBreakpoint) object;
            properties.setString(FunctionBreakpoint.PROP_FUNCTION_NAME, fb.getFunctionName());
            properties.setInt(FunctionBreakpoint.PROP_BREAKPOINT_TYPE, fb.getBreakpointType());
        } else if (object instanceof AddressBreakpoint) {
            AddressBreakpoint fb = (AddressBreakpoint) object;
            properties.setString(AddressBreakpoint.PROP_ADDRESS_VALUE, fb.getAddress());
        }
    }
}
