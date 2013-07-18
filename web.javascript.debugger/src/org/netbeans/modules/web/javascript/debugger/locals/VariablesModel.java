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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.netbeans.modules.web.javascript.debugger.ViewModelSupport;
import org.netbeans.modules.web.javascript.debugger.eval.EvaluatorService;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.api.debugger.PropertyDescriptor;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.netbeans.modules.web.webkit.debugging.api.debugger.Scope;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import static org.netbeans.spi.debugger.ui.Constants.*;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.PasteType;

@NbBundle.Messages({
    "VariablesModel_Name=Name",
    "VariablesModel_Desc=Description"
})
@DebuggerServiceRegistration(path="javascript-debuggerengine/LocalsView", types={ TreeModel.class, ExtendedNodeModel.class, TableModel.class })
public class VariablesModel extends ViewModelSupport implements TreeModel, ExtendedNodeModel,
        TableModel, Debugger.Listener, PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(VariablesModel.class.getName());
    public static final String LOCAL = "org/netbeans/modules/debugger/resources/localsView/local_variable_16.png"; // NOI18N
    public static final String GLOBAL = "org/netbeans/modules/web/javascript/debugger/resources/global_variable_16.png"; // NOI18N
    public static final String PROTO = "org/netbeans/modules/web/javascript/debugger/resources/proto_variable_16.png"; // NOI18N
    
    protected final Debugger debugger;
    protected final EvaluatorService evaluator;
    private final Map<String, ScopedRemoteObject> scopeVars = new HashMap<String, ScopedRemoteObject>();
    
    protected final List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();
    private AtomicReference<CallFrame> currentStack = new AtomicReference<CallFrame>();
    private Map<RemoteObject, List<ScopedRemoteObject>> variablesCache = new HashMap<RemoteObject, List<ScopedRemoteObject>>();
    private RequestProcessor RP = new RequestProcessor(VariablesModel.class.getName());

    public VariablesModel(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, Debugger.class);
        evaluator = contextProvider.lookupFirst(null, EvaluatorService.class);
        debugger.addListener(this);
        debugger.addPropertyChangeListener(this);
        // update now:
        if (debugger.isSuspended()) {
            currentStack.set(debugger.getCurrentCallFrame());
        } else {
            currentStack.set(null);
        }
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
        } else if (parent instanceof ScopedRemoteObject) {
            return getProperties((ScopedRemoteObject) parent).toArray();
        } else {
            throw new UnknownTypeException(parent);
        }
    }

    protected CallFrame getCurrentStack() {
        return currentStack.get();
    }

    private List<ScopedRemoteObject> getVariables(CallFrame frame) {
        List<ScopedRemoteObject> vars = new ArrayList<ScopedRemoteObject>();
        for (Scope scope : frame.getScopes()) {
            RemoteObject obj = scope.getScopeObject();
            if (scope.isLocalScope()) {
                vars.addAll(getProperties(obj, ViewScope.LOCAL));
            } else {
                vars.add(getScopeVariable(obj, scope));
            }
        }
        return sortVariables(vars);
    }

    private ScopedRemoteObject getScopeVariable(RemoteObject obj, Scope scope) {
        final String scopeType = scope.getType();
        synchronized (scopeVars) {
            ScopedRemoteObject sro = scopeVars.get(scopeType);
            if (sro == null) {
                sro = new ScopedRemoteObject(obj, scope);
                scopeVars.put(scopeType, sro);
            }
            return sro;
        }
    }

    private List<ScopedRemoteObject> sortVariables(List<ScopedRemoteObject> vars) {
        Collections.sort(vars, new Comparator<ScopedRemoteObject>() {
            @Override
            public int compare(ScopedRemoteObject o1, ScopedRemoteObject o2) {
                int i = o1.getScope().compareTo(o2.getScope());
                if (i != 0) {
                    return i;
                } else {
                    return o1.getObjectName().compareToIgnoreCase(o2.getObjectName());
                }
            }
        });
        return vars;
    }

    private Collection<? extends ScopedRemoteObject> getProperties(ScopedRemoteObject var) {
        return getProperties(var.getRemoteObject(), ViewScope.DEFAULT);
    }

    private Collection<? extends ScopedRemoteObject> getProperties(RemoteObject prop, ViewScope scope) {
        List<ScopedRemoteObject> res = variablesCache.get(prop);
        if (res != null) {
            return res;
        }
        res = new ArrayList<ScopedRemoteObject>();
        variablesCache.put(prop, res);
        if (prop.getType() == RemoteObject.Type.OBJECT) {
            for (PropertyDescriptor desc : prop.getProperties()) {
                if (desc.getValue() == null || desc.getValue().getType() == RemoteObject.Type.FUNCTION) {
                    continue;
                }
                res.add(new ScopedRemoteObject(desc.getValue(), desc.getName(), scope));
            }
        }
        return sortVariables(res);
    }

    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        } else if (node instanceof ScopedRemoteObject) {
            RemoteObject var = ((ScopedRemoteObject) node).getRemoteObject();
            if (var.getType() == RemoteObject.Type.OBJECT) {
                if (var.hasFetchedProperties()) {
                    return var.getProperties().isEmpty();
                } else {
                    updateNodeOnBackground(node, var);
                    return true;
                }
            }
            return true;
        } else {
            throw new UnknownTypeException(node);
        }
    }

    protected void updateNodeOnBackground(final Object node, final RemoteObject var) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                if (getCurrentStack() == null) {
                    return;
                }
                var.getProperties();
                fireChangeEvent(new ModelEvent.NodeChanged(this, node, ModelEvent.NodeChanged.EXPANSION_MASK));
            }
        });
    }

    @Override
    public int getChildrenCount(Object parent) throws UnknownTypeException {
        CallFrame frame = currentStack.get();
        if (frame == null) {
            return 0;
        }
        if (parent == ROOT) {
            return getVariables(frame).size();
        } else if (parent instanceof ScopedRemoteObject) {
            return getProperties((ScopedRemoteObject) parent).size();
        } else {
            throw new UnknownTypeException(parent);
        }
    }

    // NodeModel implementation ................................................
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return Bundle.VariablesModel_Name();
        } else if (node instanceof ScopedRemoteObject) {
            return ((ScopedRemoteObject) node).getObjectName();
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
        if (node instanceof ScopedRemoteObject) {
            ScopedRemoteObject sv = (ScopedRemoteObject) node;
            switch (sv.getScope()) {
                case PROTO:
                    return PROTO;
                case LOCAL:
                case DEFAULT:
                    return LOCAL;
            }
            return GLOBAL;
        } else {
            throw new UnknownTypeException(node);
        }
    }

    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return Bundle.VariablesModel_Desc();
        } else if (node instanceof ScopedRemoteObject) {
            return ((ScopedRemoteObject) node).getObjectName();
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
        } else if (node instanceof ScopedRemoteObject) {
            RemoteObject var = ((ScopedRemoteObject) node).getRemoteObject();
            if (LOCALS_VALUE_COLUMN_ID.equals(columnID)) {
                String value = var.getValueAsString();
                if (value.isEmpty() && var.getType() == RemoteObject.Type.OBJECT) {
                    value = var.getDescription();
                }
                return value;
            } else if (LOCALS_TYPE_COLUMN_ID.equals(columnID)) {
                if (var.getType() == RemoteObject.Type.OBJECT) {
                    String clazz = var.getClassName();
                    if (clazz == null) {
                        return var.getType().getName();
                    } else {
                        return clazz;
                    }
                } else {
                    return var.getType().getName();
                }
            } else if (LOCALS_TO_STRING_COLUMN_ID.equals(columnID)) {
                return var.getValueAsString();
            }
        }
        throw new UnknownTypeException(node);
    }

    @Override
    public boolean isReadOnly(Object node, String columnID)
            throws UnknownTypeException {
        if (LOCALS_VALUE_COLUMN_ID.equals(columnID) && node instanceof ScopedRemoteObject
                || WATCH_VALUE_COLUMN_ID.equals(columnID) && node instanceof ScopedRemoteObject) {
//            RemoteObject var = ((ScopedRemoteObject) node).getRemoteObject();
//            return !var.isMutable();
            return true;
        }
        return true;
    }

    @Override
    public void setValueAt(Object node, String columnID, Object value)
            throws UnknownTypeException {
        if (LOCALS_VALUE_COLUMN_ID.equals(columnID) && node instanceof ScopedRemoteObject) {
            ScopedRemoteObject sro = (ScopedRemoteObject) node;
            evaluator.evaluateExpression(getCurrentStack(), sro.getObjectName() + "=" + value + ";", false);
            refresh();
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
    public void paused(List<CallFrame> callStack, String reason) {
        currentStack.set(debugger.getCurrentCallFrame());
        refresh();
    }

    @Override
    public void resumed() {
        currentStack.set(null);
        variablesCache = new HashMap<RemoteObject, List<ScopedRemoteObject>>();
        synchronized (scopeVars) {
            scopeVars.clear();
        }
        refresh();
    }

    @Override
    public void reset() {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (Debugger.PROP_CURRENT_FRAME.equals(propertyName)) {
            currentStack.set(debugger.getCurrentCallFrame());
            refresh();
        }
    }
    
    
    @NbBundle.Messages({"Scope_Local=Local", "Scope_Global=Global",
                        "Scope_Catch=Catch", "Scope_Closure=Closure",
                        "Scope_With=With" })
    public static class ScopedRemoteObject {

        private RemoteObject var;
        private ViewScope scope;
        private String objectName;

        public ScopedRemoteObject(RemoteObject var, String name) {
            this(var, name, ViewScope.DEFAULT);
        }

        public ScopedRemoteObject(RemoteObject var, Scope sc) {
            this.var = var;
            if (sc.isLocalScope()) {
                this.scope = ViewScope.LOCAL;
                this.objectName = Bundle.Scope_Local();
            } else if (sc.isGlobalScope()) {
                this.scope = ViewScope.GLOBAL;
                this.objectName = Bundle.Scope_Global();
            } else if (sc.isCatchScope()) {
                this.scope = ViewScope.CATCH;
                this.objectName = Bundle.Scope_Catch();
            } else if (sc.isClosureScope()) {
                this.scope = ViewScope.CLOSURE;
                this.objectName = Bundle.Scope_Closure();
            } else if (sc.isWithScope()) {
                this.scope = ViewScope.WITH;
                this.objectName = Bundle.Scope_With();
            } else {
                this.scope = ViewScope.UNKNOWN;
                String type = sc.getType();
                if (type.length() > 1) {
                    type = Character.toUpperCase(type.charAt(0)) + type.substring(1);
                }
                this.objectName = type;
            }
        }

        public ScopedRemoteObject(RemoteObject var, String name, ViewScope scope) {
            this.var = var;
            this.scope = scope;
            this.objectName = name;
        }

        public ViewScope getScope() {
            if ("__proto__".equals(objectName)) {
                return ViewScope.PROTO;
            }
            return scope;
        }

        public RemoteObject getRemoteObject() {
            return var;
        }

        public String getObjectName() {
            return objectName;
        }
    }

    public static enum ViewScope {

        LOCAL,
        CATCH,
        CLOSURE,
        GLOBAL,
        DEFAULT,
        PROTO,
        WITH,
        UNKNOWN,
    }
}
