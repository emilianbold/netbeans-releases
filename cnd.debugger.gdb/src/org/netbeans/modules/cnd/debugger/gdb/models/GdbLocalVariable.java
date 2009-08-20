/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.cnd.debugger.gdb.GdbCallStackFrame;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbVariable;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariable;

/**
 *
 * @author Egor Ushakov
 */
public class GdbLocalVariable extends AbstractVariable implements LocalVariable, PropertyChangeListener {
    private final String name;
    private final String type;

    public GdbLocalVariable(GdbDebugger debugger, GdbVariable var) {
        super(debugger, var.getValue());
        this.name = var.getName();
        this.type = getDebugger().requestWhatis(name);
        
        debugger.addPropertyChangeListener(GdbDebugger.PROP_VALUE_CHANGED, this);
    }

    public GdbLocalVariable(GdbDebugger debugger, String name) {
        super(debugger, null);
        this.name = name;
        this.type = getDebugger().requestWhatis(name);
        String expr = name;
        if (GdbCallStackFrame.enableMacros && !GdbWatchVariable.disableMacros) {
            expr = GdbWatchVariable.expandMacro(getDebugger(), expr);
        }
        value = getDebugger().requestValue(expr);
        
        debugger.addPropertyChangeListener(GdbDebugger.PROP_VALUE_CHANGED, this);
    }

    public String getType() {
        return type;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        onValueChange(evt);
    }

    public String getName() {
        return name;
    }
}
