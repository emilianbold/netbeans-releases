/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.cnd.debugger.gdb.GdbCallStackFrame;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.common.ui.VariablesViewButtons;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/*
 * LocalsTreeModel.java
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class LocalsTreeModel implements TreeModel, PropertyChangeListener {
        
    private GdbDebugger debugger;
    private Listener listener;
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();
    private static final Logger log = Logger.getLogger("gdb.logger"); // NOI18N

    private Preferences preferences = NbPreferences.forModule(VariablesViewButtons.class).node(VariablesViewButtons.PREFERENCES_NAME);
    private VariablesPreferenceChangeListener prefListener = new VariablesPreferenceChangeListener();

    private PropertyChangeListener[] varListeners;
        
    public LocalsTreeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, GdbDebugger.class);
        preferences.addPreferenceChangeListener(prefListener);
    }    
    
    public Object getRoot() {
        return ROOT;
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        if (AbstractVariable.PROP_VALUE.equals(ev.getPropertyName())) {
            fireNodeChanged(ev.getSource());
        }
    }
    
    public Object[] getChildren(Object o, int from, int to) throws UnknownTypeException {
        Object[] ch = getChildrenImpl(o);
        for (Object av : ch) {
            if (av instanceof AbstractVariable) {
                ((AbstractVariable)av).addPropertyChangeListener(this);
            }
        }
        return ch;
    }
    
    public Object[] getChildrenImpl(Object o) throws UnknownTypeException {
        if (o.equals(ROOT)) {
            AbstractVariable[] res;
            if (VariablesViewButtons.isShowAutos()) {
                res = getAutos();
            } else {
                res = getLocalVariables();
            }
            updateVarListeners(res);
            return res;
        } else if (o instanceof AbstractVariable) {
            AbstractVariable abstractVariable = (AbstractVariable) o;
            return abstractVariable.getFields();
        } else {
            return new AbstractVariable[0];
        }
    }

    private void updateVarListeners(AbstractVariable[] vars) {
        varListeners = new PropertyChangeListener[vars.length];
        for (int i = 0; i < vars.length; i++) {
            AbstractVariable var = vars[i];
            PropertyChangeListener l = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    fireNodeChanged(evt.getSource());
                }
            };
            varListeners[i] = l; // Hold it so that it does not get lost, till the array is updated.
            var.addPropertyChangeListener(WeakListeners.propertyChange(l, var));
        }
    }
    
    /**
     * Returns number of children for given node.
     *
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  0 if no children are visible
     */
    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node.equals(ROOT)) {
            return Integer.MAX_VALUE;
        } else if (node instanceof AbstractVariable) { // ThisVariable & FieldVariable
            return Integer.MAX_VALUE;
        }
        return 0;
    }
    
    public boolean isLeaf(Object o) throws UnknownTypeException {
        if (o.equals(ROOT)) {
            return false;
        }
        if (o instanceof AbstractVariable) {
            int i = ((AbstractVariable) o).getFieldsCount();
            return i == 0;
        }
        
        if (o.equals("NoInfo")) { // NOI18N
            return true;
        } else if (o.equals("No current thread")) { // NOI18N
            return true;
        } else if (o instanceof AbstractVariable.ErrorField) {
            return true;
        }
        throw new UnknownTypeException(o);
    }
    
    /**
     * Save ModelListener to be able to push updates
     */
    public void addModelListener(ModelListener l) {
        listeners.add(l);
        if (listener == null) {
            listener = new Listener(this, debugger);
        }
    }
    
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
        if (listeners.isEmpty()) {
            listener.destroy();
            listener = null;
        }
    }

    void fireTreeChanged() {
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        for (ModelListener l : ls) {
            l.modelChanged(new ModelEvent.TreeChanged(this));
        }
    }

    private void fireTableValueChanged(Object node, String propertyName) {
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        for (ModelListener l : ls) {
            l.modelChanged(new ModelEvent.TableValueChanged(this, node, propertyName));
        }
    }

    private void fireNodeChanged(Object node) {
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        for (ModelListener l : ls) {
            l.modelChanged(new ModelEvent.NodeChanged(this, node));
        }
    }
    
    
    // private methods .........................................................
    
    private AbstractVariable[] getLocalVariables() {
        GdbCallStackFrame callStackFrame = debugger.getCurrentCallStackFrame();
        if (callStackFrame == null) {
            return new AbstractVariable[0];
        }
        return callStackFrame.getLocalVariables();
    }

    private AbstractVariable[] getAutos() {
        GdbCallStackFrame callStackFrame = debugger.getCurrentCallStackFrame();
        if (callStackFrame == null) {
            return new AbstractVariable[0];
        }
        AbstractVariable[] res = callStackFrame.getAutos();
        if (res != null) {
            return res;
        } else {
            return new AbstractVariable[0];
        }
    }
    
    GdbDebugger getDebugger() {
        return debugger;
    }
    
    
    // innerclasses ............................................................
    
    private static class Listener implements PropertyChangeListener {
        
        private final GdbDebugger debugger;
        private final WeakReference<LocalsTreeModel> model;
        
        public Listener(LocalsTreeModel tm, GdbDebugger debugger) {
            this.debugger = debugger;
            model = new WeakReference<LocalsTreeModel>(tm);
            debugger.addPropertyChangeListener(this);
        }
        
        void destroy() {
            debugger.removePropertyChangeListener(this);
            if (task != null) {
                // cancel old task
                task.cancel();
                task = null;
            }
        }
        
        private LocalsTreeModel getModel() {
            LocalsTreeModel tm = model.get();
            if (tm == null) {
                destroy();
            }
            return tm;
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        public void propertyChange(PropertyChangeEvent e) {
            if ((e.getPropertyName().equals(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME) ||
                    e.getPropertyName().equals(GdbDebugger.PROP_CURRENT_THREAD)) &&
                    debugger.isStopped()) {
                // IF state has been changed to STOPPED or
                // IF current call stack frame has been changed & state is stopped
                log.fine("LTM.propertyChange: Change for " + e.getPropertyName());
                final LocalsTreeModel ltm = getModel();
                if (ltm == null) {
                    return;
                }
                if (task != null) {
                    // cancel old task
                    task.cancel();
                    task = null;
                }
                task = RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        if (debugger.isStopped()) {
                            ltm.fireTreeChanged();
                        }
                    }
                }, 500);
            } else if ((e.getPropertyName().equals(GdbDebugger.PROP_STATE)) &&
                    !debugger.isStopped() &&
                    task != null) {
                // debugger has been resumed
                // =>> cancel task
                task.cancel();
                task = null;
//            } else {
//                final LocalsTreeModel ltm = getModel();
//                if (ltm == null) {
//                    return;
//                }
//                task = RequestProcessor.getDefault().post(new Runnable() {
//                    public void run() {
//                        if (debugger.getState().equals(GdbDebugger.STATE_STOPPED)) {
//                            ltm.fireTreeChanged();
//                        }
//                    }
//                });
            }
        }
    }

    private class VariablesPreferenceChangeListener implements PreferenceChangeListener {
        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt.getKey();
            if (VariablesViewButtons.SHOW_AUTOS.equals(key)) {
                refresh();
            }
        }

        private void refresh() {
            try {
                fireTableValueChanged(ROOT, null);
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                Exceptions.printStackTrace(t);
            }
        }

    }
}
