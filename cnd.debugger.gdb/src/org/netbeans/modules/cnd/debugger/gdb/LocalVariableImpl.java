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

package org.netbeans.modules.cnd.debugger.gdb;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;

/*
 * LocalVariableImpl.java
 * Implements LocalVariable for primitive variables.
 *
 * @author Nik Molchanov
 */
public class LocalVariableImpl implements LocalVariable, Field {
    private String name;
    private String previousValueText;
    private String currentValueText;
    private String type;
    private GdbDebugger debugger;
    
    /**
     * Creates a new instance of LocalVariableImpl
     */
    public LocalVariableImpl(String name, String type, String value) {
        this.name = name;
        this.currentValueText = value;
        this.previousValueText = value;
        this.type = type;
        debugger = null;
    }
    
    public LocalVariableImpl(GdbVariable var) {
        name = var.getName();
//        type = var.getType();
        currentValueText = var.getValue();
        previousValueText = var.getValue();
        debugger = null;
    }
    
    public String getName() {
        return name; // Name to show in Locals View
    }
    
    public String getValue() {
        return currentValueText;
    }
    
    /**
     * Sets value of this local represented as text.
     *
     * @param value a new value of this local represented as text
     * @throws InvalidExpressionException if the expression is not correct
     */
    public void setValue(String expression) {
        if (debugger == null) {
	    // Don't set it unless its needed...
	    DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
	    debugger = (GdbDebugger) currentEngine.lookupFirst(null, GdbDebugger.class);
	}
        
        debugger.getGdbProxy().data_evaluate_expression(name + "=" + expression); // NOI18N
    }
    
    public String getType() {
        return type;
    }
    
    /**
     * Returns <code>true</code> for static fields.
     *
     * @return <code>true</code> for static fields
     */
    public boolean isStatic() {
        return false;
    }
}
