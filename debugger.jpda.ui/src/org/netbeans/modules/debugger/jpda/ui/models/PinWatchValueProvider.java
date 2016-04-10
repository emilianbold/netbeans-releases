/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.ui.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.ui.models.WatchesModel.JPDAWatchEvaluating;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.PinWatchUISupport;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path = "netbeans-JPDASession", types = PinWatchUISupport.ValueProvider.class)
public final class PinWatchValueProvider implements PinWatchUISupport.ValueProvider,
                                                    PropertyChangeListener {
    
    private final Map<Watch, ValueListeners> valueListeners = new HashMap<>();
    private final JPDADebuggerImpl debugger;
    private final WatchRefreshModelImpl refrModel = new WatchRefreshModelImpl();
    
    public PinWatchValueProvider(ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }

    @Override
    public String getId() {
        return "org.netbeans.modules.debugger.jpda.PIN_VALUE_PROVIDER";         // NOI18N
    }

    @Override
    public String getValue(Watch watch) {
        ValueListeners vl;
        synchronized (valueListeners) {
            vl = valueListeners.get(watch);
        }
        if (vl == null) {
            return null;
        } else {
            return vl.value;
        }
    }

    @Override
    public void setChangeListener(Watch watch, ValueChangeListener chl) {
        ValueListeners vl = new ValueListeners(chl);
        synchronized (valueListeners) {
            valueListeners.put(watch, vl);
        }
        JPDAWatchEvaluating watchEv = new JPDAWatchEvaluating(refrModel, watch, debugger);
        vl.watchEv = watchEv;
        updateValueFrom(watchEv);
    }

    @Override
    public void unsetChangeListener(Watch watch) {
        synchronized (valueListeners) {
            valueListeners.remove(watch);
        }
    }

    @Override
    public String getEditableValue(Watch watch) {
        ValueListeners vl;
        synchronized (valueListeners) {
            vl = valueListeners.get(watch);
        }
        if (vl == null) {
            return null;
        }
        String valueOnly = vl.valueOnly;
        if (valueOnly == null) {
            return null;
        }
        if (!VariablesTableModel.isReadOnlyVar(vl.watchEv, debugger)) {
            return valueOnly;
        } else {
            return null;
        }
    }

    @Override
    public boolean setValue(final Watch watch, final String value) {
        final ValueListeners vl;
        synchronized (valueListeners) {
            vl = valueListeners.get(watch);
        }
        if (vl == null) {
            return false;
        }
        final String lastValue = vl.value;
        final String lastValueOnly = vl.valueOnly;
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    vl.watchEv.setValue(value);
                    vl.watchEv.setEvaluated(null);
                    //vl.watchEv = new JPDAWatchEvaluating(refrModel, watch, debugger);
                    updateValueFrom(vl.watchEv);
                } catch (InvalidExpressionException ex) {
                    NotifyDescriptor msg = new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(msg);
                    vl.value = lastValue;
                    vl.valueOnly = lastValueOnly;
                    vl.listener.valueChanged(watch);
                }
            }
        });
        vl.value = getEvaluatingText();
        vl.valueOnly = null;
        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (debugger.getCurrentCallStackFrame() != null) {
            List<ValueListeners> vls;
            synchronized (valueListeners) {
                vls = new ArrayList(valueListeners.values());
            }
            for (ValueListeners vl : vls) {
                vl.watchEv.setEvaluated(null);
                vl.value = getEvaluatingText();
                vl.valueOnly = null;
                vl.listener.valueChanged(vl.watchEv.getWatch());
                refrModel.fireTableValueChangedChanged(vl.watchEv, null);
            }
        } else {
            synchronized (valueListeners) {
                for (Map.Entry<Watch, ValueListeners> wvl : valueListeners.entrySet()) {
                    wvl.getValue().value = null;
                    wvl.getValue().valueOnly = null;
                    wvl.getValue().listener.valueChanged(wvl.getKey());
                }
            }
        }
        /*
        String propertyName = evt.getPropertyName();
        if (DebuggerManager.PROP_CURRENT_ENGINE.equals(propertyName)) {
            // Current engine change
            DebuggerEngine engine = (DebuggerEngine) evt.getNewValue();
            JPDADebuggerImpl debugger;
            if (engine == null || (debugger = (JPDADebuggerImpl) engine.lookupFirst(null, JPDADebugger.class)) == null) {
                // No current debugger, null all values:
                Map<Watch, ValueListeners> vls;
                synchronized (valueListeners) {
                    vls = new HashMap(valueListeners);
                }
                for (Map.Entry<Watch, ValueListeners> wvl : vls.entrySet()) {
                    wvl.getValue().value = null;
                    wvl.getValue().listener.valueChanged(wvl.getKey());
                }
            } else {
                currentDebugger = debugger;
                if (!debuggers.contains(debugger)) {
                    debuggers.add(debugger);
                    debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, this);
                    debugger.addPropertyChangeListener(JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
                    synchronized (valueListeners) {
                        for (Map.Entry<Watch, ValueListeners> wvl : valueListeners.entrySet()) {
                            Watch w = wvl.getKey();
                            JPDAWatchEvaluating watchEv = new JPDAWatchEvaluating(refrModel, w, debugger);
                            wvl.getValue().watchEv = watchEv;
                        }
                    }
                }
            }
        } else if (propertyName.equals(JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME)) {
            
        } else if (propertyName.equals(JPDADebugger.PROP_STATE)) {
            if (((Integer) evt.getNewValue()) == JPDADebugger.STATE_DISCONNECTED) {
                JPDADebugger debugger = (JPDADebugger) evt.getSource();
                debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, this);
                debugger.removePropertyChangeListener(JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
                debuggers.remove(debugger);
            }
        }
         */
    }
    
    private static final RequestProcessor RP = new RequestProcessor(PinWatchValueProvider.class);
    
    private void updateValueFrom(final JPDAWatchEvaluating watchEv) {
        final ValueListeners vl;
        final Watch watch = watchEv.getWatch();
        synchronized (valueListeners) {
            vl = valueListeners.get(watch);
        }
        if (vl != null) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    watchEv.getValue();
                    JPDAWatch ew = watchEv.getEvaluatedWatch();
                    if (ew == null) {
                        return ;
                    }
                    String type = ew.getType();
                    int ti = type.lastIndexOf('.');
                    if (ti > 0) {
                        type = type.substring(ti + 1);
                    }
                    String value;
                    if (ew instanceof ObjectVariable) {
                        try {
                            value = ((ObjectVariable) ew).getToStringValue();
                        } catch (InvalidExpressionException ex) {
                            value = ew.getValue();
                        }
                    } else {
                        value = ew.getValue();
                    }
                    vl.value = (type.isEmpty() ? "" : "(" + type + ") ") + value;
                    vl.valueOnly = value;
                    vl.listener.valueChanged(watch);
                }
            });
        }
    }
    
    private final class WatchRefreshModelImpl implements JPDAWatchRefreshModel {

        @Override
        public boolean isLeaf(Object node) throws UnknownTypeException {
            return true;
        }

        @Override
        public void fireTableValueChangedChanged(Object node, String propertyName) {
            JPDAWatchEvaluating watchEv = (JPDAWatchEvaluating) node;
            if (watchEv.isCurrent()) {
                watchEv.setEvaluated(null);
            }
            updateValueFrom(watchEv);
        }

        @Override
        public void fireChildrenChanged(Object node) {
        }
        
    }
    
    private static final class ValueListeners {
        
        volatile String value = null;//PinWatchUISupport.ValueProvider.VALUE_EVALUATING;
        volatile String valueOnly = null;
        
        ValueChangeListener listener;
        JPDAWatchEvaluating watchEv;
        
        ValueListeners(ValueChangeListener listener) {
            this.listener = listener;
        }
    }
    
}
