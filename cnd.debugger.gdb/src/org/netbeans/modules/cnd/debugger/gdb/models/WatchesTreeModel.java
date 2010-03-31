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
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Watch;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.openide.util.RequestProcessor;

/*
 * WatchesTreeModel.java
 *
 * @author Nik Molchanov (copied from Jan Jancura's Ant Debugger implementation)
 *
 */

public class WatchesTreeModel implements TreeModel, PropertyChangeListener {

    private final GdbDebugger debugger;
    private Listener listener;
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();
    private final Map<Watch, AbstractVariable> watchToVariable = new WeakHashMap<Watch, AbstractVariable>(); 
    private static final Logger log = Logger.getLogger("gdb.logger"); // NOI18N
    
    public WatchesTreeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, GdbDebugger.class);
    }

    public void propertyChange(PropertyChangeEvent ev) {
        if (AbstractVariable.PROP_VALUE.equals(ev.getPropertyName())) {
            fireNodeChanged(ev.getSource());
        }
    }
    
    /** 
     * Returns the root node of the tree or null, if the tree is empty.
     *
     * @return the root node of the tree or null
     */
    public Object getRoot() {
        return ROOT;
    }
    
    /** 
     * Returns children for given parent on given indexes.
     *
     * @param   parent a parent of returned nodes
     * @param   from a start index
     * @param   to a end index
     *
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  children for given parent on given indexes
     */
    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
        if (parent == ROOT) {
            // 1) get Watches
            Watch[] ws = DebuggerManager.getDebuggerManager().getWatches();
            to = Math.min(ws.length, to);
            from = Math.min(ws.length, from);
            Watch[] fws = new Watch[to - from];
            System.arraycopy(ws, from, fws, 0, to - from);
            
            // 2) create GdbWatches for Watches
            int i, k = fws.length;
            AbstractVariable[] gws = new AbstractVariable[k];
            for (i = 0; i < k; i++) {
                AbstractVariable gw = watchToVariable.get(fws[i]);
                if (gw == null) {
                    gw = new GdbWatchVariable(debugger, fws[i]);
                    watchToVariable.put(fws[i], gw);
                    gw.addPropertyChangeListener(this);
                }
                gws[i] = gw;
            }
            
            if (listener == null) {
                listener = new Listener(this, debugger);
            }
            return gws;
        } else if (parent instanceof AbstractVariable) {
            return ((AbstractVariable) parent).getFields();
        }
        throw new UnknownTypeException(parent);
    }
    
    /**
     * Returns true if node is leaf.
     * 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve dchildren for given node type
     * @return  true if node is leaf
     */
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        }
        if (node instanceof Watch) {
            return true;
        }
//        if (node instanceof AbstractVariable) {
//            return ((AbstractVariable) node).getFieldsCount() == 0;
//        }
        if (node instanceof AbstractVariable) {
            return ((AbstractVariable) node).getFieldsCount() == 0;
        }
        throw new UnknownTypeException(node);
    }
    
    /**
     * Returns number of children for given node.
     * 
     * @param   node the parent node
     * @throws  NoInformationException if the set of children can not be 
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     * @since 1.1
     */
    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return Integer.MAX_VALUE;
        } else if (node instanceof AbstractVariable) {
            return Integer.MAX_VALUE;
        }
        throw new UnknownTypeException(node);
    }
    
    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
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
    
    private static class Listener extends DebuggerManagerAdapter implements PropertyChangeListener {
        
        private final WeakReference<WatchesTreeModel> model;
        private final WeakReference<GdbDebugger> debugger;
        
        private Listener(WatchesTreeModel tm, GdbDebugger debugger) {
            model = new WeakReference<WatchesTreeModel>(tm);
            this.debugger = new WeakReference<GdbDebugger>(debugger);
            DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_WATCHES, this);
            debugger.addPropertyChangeListener(this);
            Watch[] ws = DebuggerManager.getDebuggerManager().getWatches();
            int i, k = ws.length;
            for (i = 0; i < k; i++) {
                ws [i].addPropertyChangeListener(this);
            }
        }
        
        private WatchesTreeModel getModel() {
            WatchesTreeModel m = model.get();
            if (m == null) {
                destroy();
            }
            return m;
        }
        
        @Override
        public void watchAdded(Watch watch) {
            WatchesTreeModel m = getModel();
            if (m == null) {
                return;
            }
            watch.addPropertyChangeListener(this);
            m.fireTreeChanged();
        }
        
        @Override
        public void watchRemoved(Watch watch) {
            WatchesTreeModel m = getModel();
            if (m == null) {
                return;
            }
            Object o = m.watchToVariable.remove(watch);
            // fix for the IZ 163112
            if (o != null && o instanceof GdbWatchVariable) {
                ((GdbWatchVariable)o).destroy();
            }
            watch.removePropertyChangeListener(this);
            m.fireTreeChanged();
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            // We already have watchAdded & watchRemoved. Ignore PROP_WATCHES:
            if (DebuggerManager.PROP_WATCHES.equals(propName)) {
                return;
            }
            final WatchesTreeModel m = getModel();
            if (m == null) {
                return;
            }
            if (m.debugger.getState() == GdbDebugger.State.EXITED) {
                destroy();
                return;
            }
            if (m.debugger.getState() == GdbDebugger.State.RUNNING) {
                return;
            }
            
            if (evt.getSource() instanceof Watch) {
                Object node;
                synchronized (m.watchToVariable) {
                    node = m.watchToVariable.get((Watch)evt.getSource());
                }
                if (node != null) {
                    if (node instanceof AbstractVariable && ((AbstractVariable) node).getFieldsCount() > 0) {
                        m.fireTreeChanged();
                    } else {
                        m.fireTableValueChanged(node, null);
                    }
                    return;
                }
            }
            
            if (task == null) {
                GdbDebugger d = debugger.get();
                RequestProcessor rp = (d != null) ? d.getRequestProcessor() : RequestProcessor.getDefault();
                task = rp.create(new Runnable() {
                    public void run() {
                        m.fireTreeChanged();
                    }
                });
            }
            task.schedule(100);
        }
        
        private void destroy() {
            DebuggerManager.getDebuggerManager().removeDebuggerListener(DebuggerManager.PROP_WATCHES, this);
            GdbDebugger d = debugger.get();
            if (d != null) {
                d.removePropertyChangeListener(this);
            }

            Watch[] ws = DebuggerManager.getDebuggerManager().getWatches();
            int i, k = ws.length;
            for (i = 0; i < k; i++) {
                ws [i].removePropertyChangeListener(this);
            }

            if (task != null) {
                // cancel old task
                task.cancel();
                task = null;
            }
        }
    }
}
