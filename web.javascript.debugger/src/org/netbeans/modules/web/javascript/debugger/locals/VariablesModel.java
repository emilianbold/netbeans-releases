/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.web.javascript.debugger.locals;

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.chromium.sdk.*;
import org.netbeans.modules.web.javascript.debugger.Debugger;
import org.netbeans.modules.web.javascript.debugger.DebuggerListener;
import org.netbeans.modules.web.javascript.debugger.DebuggerState;
import org.netbeans.modules.web.javascript.debugger.ViewModelSupport;
import org.netbeans.modules.web.javascript.debugger.watches.WatchesModel;
import org.netbeans.spi.debugger.ContextProvider;
import static org.netbeans.spi.debugger.ui.Constants.*;
import org.netbeans.spi.viewmodel.*;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

@NbBundle.Messages({"VariablesModel_Name=Name",
"VariablesModel_Desc=Description",
"TYPE_ARRAY=Array",
"TYPE_OBJECT=Object",
"TYPE_NUMBER=Number",
"TYPE_STRING=String",
"TYPE_FUNCTION=Function",
"TYPE_BOOLEAN=Boolean",
"TYPE_ERROR=Error",
"TYPE_REGEXP=Regexp",
"TYPE_DATE=Date",
"TYPE_UNDEFINED=Undefined",
"TYPE_NULL=Null"
})
public class VariablesModel extends ViewModelSupport implements TreeModel, ExtendedNodeModel,
		TableModel, DebuggerListener {
	
	public static final String LOCAL = "org/netbeans/modules/debugger/resources/localsView/local_variable_16.png"; // NOI18N
	public static final String GLOBAL = "org/netbeans/modules/web/javascript/debugger/resources/global_variable_16.png"; // NOI18N
	public static final String PROTO = "org/netbeans/modules/web/javascript/debugger/resources/proto_variable_16.png"; // NOI18N

	protected final Debugger debugger;
    
    protected final List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();

    private AtomicReference<CallFrame>  currentStack = new AtomicReference<CallFrame>();

    private static final Logger LOGGER = Logger.getLogger(VariablesModel.class.getName());

	public VariablesModel(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, Debugger.class);
        debugger.addListener(this);
        // update now:
        stateChanged(debugger);
	}

	// TreeModel implementation ................................................

    @Override
	public Object getRoot() {
		return ROOT;
	}

    @Override
	public Object[] getChildren(Object parent, int from, int to)
			throws UnknownTypeException {
        CallFrame frame = currentStack.get();
        if (frame == null) {
            return new Object[0];
        }
		if (parent == ROOT) {
            return getVariables(frame).subList(from, to).toArray();
		} else if (parent instanceof ScopedVariable) {
            return getProperties((ScopedVariable)parent).toArray();
		} else {
			throw new UnknownTypeException(parent);
		}
	}

    protected CallFrame getCurrentStack() {
        return currentStack.get();
    }

    private List<ScopedVariable> getVariables(CallFrame frame) {
        List<ScopedVariable> vars = new ArrayList<ScopedVariable>();
        for (JsScope scope : frame.getVariableScopes()) {
            if (scope.getType() == JsScope.Type.LOCAL && scope.getVariables().isEmpty()) {
                vars.add(WatchesModel.evaluateExpression(frame, "this"));
            }
            for (JsVariable var : scope.getVariables()) {
                if (var.isReadable()) {
                    vars.add(new ScopedVariable(var, scope));
                }
            }
        }
        return sortVariables(vars);
    }
    
    private List<ScopedVariable> sortVariables(List<ScopedVariable> vars) {
        Collections.sort(vars, new Comparator<ScopedVariable>() {
            @Override
            public int compare(ScopedVariable o1, ScopedVariable o2) {
                int i = o1.getScope().compareTo(o2.getScope());
                if (i != 0) {
                    return i;
                } else {
                    return o1.getVariable().getName().compareTo(o2.getVariable().getName());
                }
            }
        });
        return vars;
    }
    
    protected Collection<? extends ScopedVariable> getProperties(ScopedVariable var) {
        JsValue val = var.getVariable().getValue();
        List<ScopedVariable> res = new ArrayList<ScopedVariable>();
        if (JsValue.Type.isObjectType(val.getType())) {
            for (JsVariable v : var.getVariable().getValue().asObject().getProperties()) {
                res.add(new ScopedVariable(v));
            }
            for (JsVariable v : var.getVariable().getValue().asObject().getInternalProperties()) {
                res.add(new ScopedVariable(v, ViewScope.PROTO));
            }
        }
        return sortVariables(res);
    }
    
    @Override
	public boolean isLeaf(Object node) throws UnknownTypeException {
		if (node == ROOT) {
			return false;
		} else if (node instanceof ScopedVariable) {
			JsVariable var = ((ScopedVariable)node).getVariable();
            JsValue val = var.getValue();
            if (val != null && JsValue.Type.isObjectType(val.getType())) {
                JsObject ob = val.asObject();
                if (ob != null) {
                    try {
                    Collection<? extends JsVariable> vars = ob.getProperties();
                    if (vars != null && vars.size() > 0) {
                        return false;
                    }
                    } catch (Throwable t) {
                        LOGGER.log(Level.WARNING, "cannot get value of "+var.getFullyQualifiedName()+". cause: "+t.getMessage());
                        return true;
                    }
                }
            }
            return true;
		} else {
			throw new UnknownTypeException(node);
		}
	}

    @Override
	public int getChildrenCount(Object parent) throws UnknownTypeException {
        CallFrame frame = currentStack.get();
        if (frame == null) {
            return 0;
        }
		if (parent == ROOT) {
            return getVariables(frame).size();
		} else if (parent instanceof ScopedVariable) {
            return getProperties((ScopedVariable)parent).size();
		} else {
			throw new UnknownTypeException(parent);
		}
	}

	// NodeModel implementation ................................................

    @Override
	public String getDisplayName(Object node) throws UnknownTypeException {
		if (node == ROOT) {
			return Bundle.VariablesModel_Name();
		} else if (node instanceof ScopedVariable) {
			return ((ScopedVariable) node).getVariable().getName();
		} else {
			throw new UnknownTypeException(node);
		}
	}

    @Override
	public String getIconBase(Object node) throws UnknownTypeException {
	    throw new UnsupportedOperationException();
	}

    @Override
	public String getIconBaseWithExtension(Object node)
			throws UnknownTypeException {
		assert node != ROOT;
		if (node instanceof ScopedVariable) {
			ScopedVariable sv = (ScopedVariable)node;
            switch (sv.getScope()) {
                case GLOBAL: return GLOBAL;
                case PROTO : return PROTO;
            }
            return LOCAL;
		} else {
			throw new UnknownTypeException(node);
		}
	}

    @Override
	public String getShortDescription(Object node) throws UnknownTypeException {
		if (node == ROOT) {
			return Bundle.VariablesModel_Desc();
		} else if (node instanceof ScopedVariable) {
			JsVariable var = ((ScopedVariable)node).getVariable();
			return var.getFullyQualifiedName();
		} else {
			throw new UnknownTypeException(node);
		}
	}

	// TableModel implementation ...............................................

    @Override
	public Object getValueAt(Object node, String columnID)
			throws UnknownTypeException {
		if (node == ROOT) {
			return "";
		} else if (node instanceof ScopedVariable) {
			JsVariable var = ((ScopedVariable) node).getVariable();
			JsValue value = var.getValue();
			if (LOCALS_VALUE_COLUMN_ID.equals(columnID)) {
			    return value.getValueString();
			} else if (LOCALS_TYPE_COLUMN_ID.equals(columnID)) {
                return getTypeName(value.getType());
			} else if (LOCALS_TO_STRING_COLUMN_ID.equals(columnID)) {
                return value.getValueString();
            }
		}
		throw new UnknownTypeException(node);
	}
    
    private static String getTypeName(JsValue.Type type) {
        switch (type) {
            case TYPE_OBJECT : return Bundle.TYPE_OBJECT();
            case TYPE_NUMBER : return Bundle.TYPE_NUMBER();
            case TYPE_STRING : return Bundle.TYPE_STRING();
            case TYPE_FUNCTION : return Bundle.TYPE_FUNCTION();
            case TYPE_BOOLEAN : return Bundle.TYPE_BOOLEAN();
            case TYPE_ERROR : return Bundle.TYPE_ERROR();
            case TYPE_REGEXP : return Bundle.TYPE_REGEXP();
            case TYPE_DATE : return Bundle.TYPE_DATE();
            case TYPE_ARRAY : return Bundle.TYPE_ARRAY();
            case TYPE_UNDEFINED : return Bundle.TYPE_UNDEFINED();
            case TYPE_NULL : return Bundle.TYPE_NULL();
        }
        return type.name();
    }

    @Override
    public boolean isReadOnly(Object node, String columnID)
            throws UnknownTypeException {
        if (LOCALS_VALUE_COLUMN_ID.equals(columnID) && node instanceof ScopedVariable) {
            JsVariable var = ((ScopedVariable) node).getVariable();
            return !var.isMutable();
        }
        return true;
    }

    @Override
    public void setValueAt(Object node, String columnID, Object value)
            throws UnknownTypeException {
        if (LOCALS_VALUE_COLUMN_ID.equals(columnID) && node instanceof ScopedVariable) {
            JsVariable var = ((ScopedVariable) node).getVariable();
            assert var.isMutable() : var;
            var.setValue(value.toString(), null);
        }
        throw new UnknownTypeException(node);
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
	public Transferable clipboardCopy(Object node) throws IOException,
			UnknownTypeException {
		throw new UnsupportedOperationException("Not supported yet."); // NOI18N
	}

    @Override
	public Transferable clipboardCut(Object node) throws IOException,
			UnknownTypeException {
		throw new UnsupportedOperationException("Not supported yet."); // NOI18N
	}

    @Override
	public PasteType[] getPasteTypes(Object node, Transferable t)
			throws UnknownTypeException {
		return null;
	}

    @Override
	public void setName(Object node, String name) throws UnknownTypeException {
		throw new UnsupportedOperationException("Not supported yet."); // NOI18N
	}

    @Override
    public void stateChanged(Debugger debugger) {
        if (debugger.getState() == DebuggerState.SUSPENDED) {
            currentStack.set(debugger.getCurrentStackTrace().get(0));
        } else {
            currentStack.set(null);
        }
        refresh();
    }

    public static class ScopedVariable {
        private JsVariable var;
        private ViewScope scope;

        public ScopedVariable(JsVariable var) {
            this(var, ViewScope.DEFAULT);
        }
        
        public ScopedVariable(JsVariable var, JsScope sc) {
            this.var = var;
            if (sc.getType().equals(JsScope.Type.LOCAL)) {
                this.scope = ViewScope.LOCAL;
            } else {
                this.scope = ViewScope.GLOBAL;
            }
        }

        public ScopedVariable(JsVariable var, ViewScope scope) {
            this.var = var;
            this.scope = scope;
        }

        public ViewScope getScope() {
            return scope;
        }

        public JsVariable getVariable() {
            return var;
        }
    }
    
    public static enum ViewScope {
        
        LOCAL,
        GLOBAL,
        DEFAULT,
        PROTO,
        
    }
}
