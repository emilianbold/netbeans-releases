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

import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.models.AbstractVariable;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.openide.util.Exceptions;

/**
 *
 */
public class TruffleVariableImpl implements TruffleVariable {
    
    private static final String TRUFFLE_OBJECT_TYPE = "org.netbeans.modules.debugger.jpda.backend.truffle.TruffleObject";   // NOI18N
    private static final String FIELD_NAME = "name";                            // NOI18N
    private static final String FIELD_TYPE = "type";                            // NOI18N
    private static final String FIELD_LEAF = "leaf";                            // NOI18N
    private static final String FIELD_DISPLAY_VALUE = "displayValue";           // NOI18N
    private static final String METHOD_GET_CHILDREN = "getProperties";          // NOI18N
    private static final String METHOD_GET_CHILDREN_SIG = "()[Lorg/netbeans/modules/debugger/jpda/backend/truffle/TruffleObject;";  // NOI18N
    private static final String FIELD_VALUE_SOURCE = "valueSourcePosition";     // NOI18N
    private static final String FIELD_TYPE_SOURCE = "typeSourcePosition";       // NOI18N
    
    private final ObjectVariable truffleObject;
    private final String name;
    private final String type;
    private final String displayValue;
    private final boolean leaf;
    private SourcePosition valueSource;
    private SourcePosition typeSource;
    
    private TruffleVariableImpl(ObjectVariable truffleObject, String name,
                                String type, String displayValue,
                                boolean leaf) {
        this.truffleObject = truffleObject;
        this.name = name;
        this.type = type;
        this.displayValue = displayValue;
        this.leaf = leaf;
    }
    
    public static TruffleVariableImpl get(Variable var) {
        if (TRUFFLE_OBJECT_TYPE.equals(var.getType())) {
            ObjectVariable truffleObj = (ObjectVariable) var;
            //System.err.println("TruffleVariableImpl.get("+var+") fields on "+truffleObj+", class "+truffleObj.getClassType().getName());
            // The inner value can change in watches.
            Field f = truffleObj.getField(FIELD_NAME);
            if (f == null) {
                return null;
            }
            String name = (String) f.createMirrorObject();
            f = truffleObj.getField(FIELD_TYPE);
            if (f == null) {
                return null;
            }
            String type = (String) f.createMirrorObject();
            f = truffleObj.getField(FIELD_DISPLAY_VALUE);
            if (f == null) {
                return null;
            }
            String dispVal = (String) f.createMirrorObject();
            boolean leaf = isLeaf(truffleObj);
            return new TruffleVariableImpl(truffleObj, name, type, dispVal, leaf);
        } else {
            return null;
        }
    }

    static boolean isLeaf(ObjectVariable truffleObj) {
        Field f = getFieldChecked(FIELD_LEAF, truffleObj);
        Boolean mirrorLeaf = (Boolean) f.createMirrorObject();
        boolean leaf;
        if (mirrorLeaf == null) {
            leaf = false;
        } else {
            leaf = mirrorLeaf;
        }
        return leaf;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public Object getValue() {
        return displayValue; // TODO
    }
    
    @Override
    public synchronized SourcePosition getValueSource() {
        if (valueSource == null) {
            Field f = getFieldChecked(FIELD_VALUE_SOURCE, truffleObject);
            valueSource = TruffleAccess.getSourcePosition(((AbstractVariable) f).getDebugger(), (ObjectVariable) f);
        }
        return valueSource;
    }

    @Override
    public synchronized SourcePosition getTypeSource() {
        if (typeSource == null) {
            Field f = getFieldChecked(FIELD_TYPE_SOURCE, truffleObject);
            typeSource = TruffleAccess.getSourcePosition(((AbstractVariable) f).getDebugger(), (ObjectVariable) f);
        }
        return typeSource;
    }

    @Override
    public boolean isLeaf() {
        return leaf;
    }
    
    @Override
    public Object[] getChildren() {
        return getChildren(truffleObject);
    }

    static Object[] getChildren(ObjectVariable truffleObject) {
        try {
            Variable children = truffleObject.invokeMethod(METHOD_GET_CHILDREN, METHOD_GET_CHILDREN_SIG, new Variable[] {});
            if (children instanceof ObjectVariable) {
                Field[] fields = ((ObjectVariable) children).getFields(0, Integer.MAX_VALUE);
                int n = fields.length;
                Object[] ch = new Object[n];
                for (int i = 0; i < n; i++) {
                    TruffleVariableImpl tv = get(fields[i]);
                    if (tv != null) {
                        ch[i] = tv;
                    } else {
                        ch[i] = fields[i].createMirrorObject();
                    }
                }
                return ch;
            }
        } catch (NoSuchMethodException | InvalidExpressionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new Object[] {};
    }
    
    private static Field getFieldChecked(String fieldName, ObjectVariable truffleObject) {
        Field f = truffleObject.getField(fieldName);
        if (f == null) {
            try {
                throw new IllegalStateException("No "+fieldName+" field in "+truffleObject.getToStringValue());
            } catch (InvalidExpressionException iex) {
                throw new IllegalStateException("No "+fieldName+" field in "+truffleObject);
            }
        } else {
            return f;
        }
    }
}
