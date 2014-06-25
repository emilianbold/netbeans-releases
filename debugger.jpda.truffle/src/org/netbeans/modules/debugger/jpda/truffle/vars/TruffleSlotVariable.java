/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle.vars;

import com.sun.jdi.ClassType;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.truffle.TruffleDebugManager;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
public class TruffleSlotVariable implements TruffleVariable {
    
    private static final String METHOD_GET_SLOT_VALUE = "getSlotValue";         // NOI18N
    private static final String METHOD_GET_SLOT_VALUE_SIG = "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"; // NOI18N
    
    private final JPDADebugger debugger;
    private final ObjectVariable frame;
    private final ObjectVariable slot;
    private final String name;
    private final String type;
    private final int[] valueLoaded = new int[] { 0 }; // 0 - not loaded, 1 - loading, 2 - loaded
    private Object value;
    private TruffleVariableImpl truffleVariable;
    
    public TruffleSlotVariable(JPDADebugger debugger, ObjectVariable frame, ObjectVariable slot,
                               String name, String type) {
        this.debugger = debugger;
        this.frame = frame;
        this.slot = slot;
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }
    
    @Override
    public Object getValue() {
        boolean toLoad = false;
        synchronized (valueLoaded) {
            if (valueLoaded[0] == 2) {
                return value;
            }
            if (valueLoaded[0] == 1) {
                try {
                    valueLoaded.wait();
                } catch (InterruptedException ex) {}
                // will call getValue() again
            } else {
                valueLoaded[0] = 1; // going to load the value...
                toLoad = true;
            }
        }
        if (!toLoad) {
            return getValue();
        }
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        Object valueObj;
        TruffleVariableImpl tv = null;
        try {
            Variable valueVar = debugAccessor.invokeMethod(METHOD_GET_SLOT_VALUE,
                                                           METHOD_GET_SLOT_VALUE_SIG,
                                                           new Variable[] { frame, slot });
            tv = TruffleVariableImpl.get(valueVar);
            if (tv != null) {
                valueObj = tv.getDisplayValue();
            } else {
                valueObj = valueVar.createMirrorObject();
            }
        } catch (NoSuchMethodException | InvalidExpressionException ex) {
            Exceptions.printStackTrace(ex);
            valueObj = null;
        }
        synchronized (valueLoaded) {
            value = valueObj;
            truffleVariable = tv;
            valueLoaded[0] = 2;
            valueLoaded.notifyAll();
        }
        return value;
    }
    
    @Override
    public boolean isLeaf() {
        synchronized (valueLoaded) {
            if (valueLoaded[0] != 2) {
                if ("Object".equals(type)) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        if (truffleVariable == null) {
            return true;
        } else {
            return truffleVariable.isLeaf();
        }
    }
    
    @Override
    public Object[] getChildren() {
        if (truffleVariable != null) {
            return truffleVariable.getChildren();
        } else {
            return new Object[] {};
        }
    }
}
