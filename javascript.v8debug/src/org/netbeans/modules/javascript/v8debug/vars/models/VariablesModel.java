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

package org.netbeans.modules.javascript.v8debug.vars.models;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Map;
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.V8Scope;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Object;
import org.netbeans.lib.v8debug.vars.V8Value;
import org.netbeans.modules.javascript.v8debug.ReferencedValues;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.V8DebuggerEngineProvider;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.javascript.v8debug.vars.V8Evaluator;
import org.netbeans.modules.javascript.v8debug.vars.VariableArgument;
import org.netbeans.modules.javascript.v8debug.vars.VariableLocal;
import org.netbeans.modules.javascript2.debug.models.ViewModelSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path=V8DebuggerEngineProvider.ENGINE_NAME+"/LocalsView",
                             types={ TreeModel.class, ExtendedNodeModel.class, TableModel.class })
public class VariablesModel extends ViewModelSupport implements TreeModel,
                                                                ExtendedNodeModel,
                                                                TableModel,
                                                                V8Debugger.Listener {
    
    public static final String LOCAL = "org/netbeans/modules/debugger/resources/localsView/local_variable_16.png"; // NOI18N
    
    private static final Object[] EMPTY_CHILDREN = new Object[]{};
    
    private final V8Debugger dbg;

    public VariablesModel(ContextProvider contextProvider) {
        dbg = contextProvider.lookupFirst(null, V8Debugger.class);
        dbg.addListener(this);
    }

    @Override
    public Object getRoot() {
        return ROOT;
    }

    @Override
    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
        if (parent == ROOT) {
            CallFrame cf = dbg.getCurrentFrame();
            if (cf == null) {
                return EMPTY_CHILDREN;
            }
            V8Frame frame = cf.getFrame();
            Map<String, ReferencedValue> argumentRefs = frame.getArgumentRefs();
            Map<String, ReferencedValue> localRefs = frame.getLocalRefs();
            V8Scope[] scopes = frame.getScopes();
            int n = argumentRefs.size() + localRefs.size() + scopes.length;
            Object[] ch = new Object[n];
            int i = 0;
            ReferencedValues rvals = cf.getRvals();
            for (String name : argumentRefs.keySet()) {
                ReferencedValue rv = argumentRefs.get(name);
                long ref = rv.getReference();
                V8Value v = rvals.getReferencedValue(ref);
                if (v == null) {
                    v = rv.getValue();
                }
                ch[i++] = new VariableArgument(name, ref, v);
            }
            for (String name : localRefs.keySet()) {
                ReferencedValue rv = localRefs.get(name);
                long ref = rv.getReference();
                V8Value v = rvals.getReferencedValue(ref);
                if (v == null) {
                    v = rv.getValue();
                }
                ch[i++] = new VariableLocal(name, ref, v);
            }
            for (V8Scope scope : scopes) {
                ch[i++] = scope;
            }
            return ch;
        } else {
            return EMPTY_CHILDREN;
        }
    }

    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        }
        return true;
    }

    @Override
    public int getChildrenCount(Object node) throws UnknownTypeException {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canRename(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public boolean canCopy(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public boolean canCut(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
        return null;
    }

    @Override
    public Transferable clipboardCut(Object node) throws IOException, UnknownTypeException {
        return null;
    }

    @Override
    public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
        return null;
    }

    @Override
    public void setName(Object node, String name) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (node instanceof VariableArgument) {
            return LOCAL;
        }
        if (node instanceof VariableLocal) {
            return LOCAL;
        }
        if (node instanceof V8Scope) {
            
        }
        throw new UnknownTypeException(node);
    }

    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof VariableLocal) {
            return ((VariableLocal) node).getName();
        }
        if (node instanceof V8Scope) {
            V8Scope scope = (V8Scope) node;
            String text = scope.getText();
            if (text == null) {
                text = scope.getType().toString();
            }
            return text + " Scope";
        }
        throw new UnknownTypeException(node);
    }

    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof VariableLocal) {
            VariableLocal var = (VariableLocal) node;
            return var.getName() + " = " + V8Evaluator.getStringValue(var.getValue());
        }
        return null;
    }

    @Override
    public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
        if (node == ROOT) {
            return "";
        } else if (Constants.LOCALS_VALUE_COLUMN_ID.equals(columnID)) {
            if (node instanceof VariableLocal) {
                VariableLocal var = (VariableLocal) node;
                return V8Evaluator.getStringValue(var.getValue());
            } else if (node instanceof V8Scope) {
                return "";
            }
        } else if (Constants.LOCALS_TYPE_COLUMN_ID.equals(columnID)) {
            if (node instanceof VariableLocal) {
                VariableLocal var = (VariableLocal) node;
                V8Value value = var.getValue();
                V8Value.Type type = value.getType();
                if (type == V8Value.Type.Object) {
                    V8Object obj = (V8Object) value;
                    return obj.getClassName();
                }
                return type.toString();
            } else if (node instanceof V8Scope) {
                return "";
            }
        }
        throw new UnknownTypeException(node);
    }

    @Override
    public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
        return true;
    }

    @Override
    public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void notifySuspended(boolean suspended) {
        refresh();
    }

    @Override
    public void notifyFinished() {
        
    }
    
}
