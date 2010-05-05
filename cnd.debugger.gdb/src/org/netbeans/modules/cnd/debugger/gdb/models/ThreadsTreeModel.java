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
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.RequestProcessor;

/**
 * This tree model provides an array of CallStackFrame objects.
 *
 * @author Gordon Prieur (copied from CallStackTreeModel)
 */
public class ThreadsTreeModel implements TreeModel {
    
    private GdbDebugger     debugger;
    private final Set<ModelListener> listeners = new HashSet<ModelListener>();
    private Listener        listener;
   
    public ThreadsTreeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, GdbDebugger.class);
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
        if (parent.equals(ROOT)) {
	    return debugger.getThreadsList();
        } else {
	    throw new UnknownTypeException(parent);
	}
    }
    
    /**
     * Returns number of children for given node.
     * 
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    public int getChildrenCount(Object parent) throws UnknownTypeException {
        if (parent.equals(ROOT)) {
            return Integer.MAX_VALUE;
        } else {
	    throw new UnknownTypeException(parent);
	}
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object getRoot() {
        return ROOT;
    }
    
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node.equals(ROOT)) {
	    return false;
	}
        if (node instanceof String) {
	    return true;
	}
        throw new UnknownTypeException(node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.add(l);
            if (listener == null) {
                listener = new Listener(this, debugger);
            }
        }
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.remove (l);
            if (listeners.isEmpty()) {
                listener.destroy();
                listener = null;
            }
        }
    }
    
    public void fireTreeChanged () {
        Object[] ls;
        synchronized (listeners) {
            ls = listeners.toArray();
        }
        ModelEvent ev = new ModelEvent.TreeChanged(this);
        for (int i = 0; i < ls.length; i++) {
            ((ModelListener) ls[i]).modelChanged(ev);
        }
    }
    
    
    /**
     * Listens on GdbDebugger on PROP_STATE
     */
    private static class Listener implements PropertyChangeListener {
        
        private final GdbDebugger debugger;
        private final WeakReference<ThreadsTreeModel> model;
        private RequestProcessor.Task task;
        
        public Listener(ThreadsTreeModel tm, GdbDebugger debugger) {
            this.debugger = debugger;
            this.model = new WeakReference<ThreadsTreeModel>(tm);
            debugger.addPropertyChangeListener(this);
        }
        
        private ThreadsTreeModel getModel() {
            ThreadsTreeModel tm = model.get();
            if (tm == null) {
                destroy();
            }
            return tm;
        }
        
        void destroy() {
            debugger.removePropertyChangeListener(this);
            if (task != null) {
                // cancel old task
                task.cancel();
                task = null;
            }
        }
        
        public synchronized void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if ((propertyName.equals(GdbDebugger.PROP_STATE) && 
                    debugger.isStopped()) ||
                    propertyName.equals(GdbDebugger.PROP_CURRENT_THREAD)) {
                synchronized (this) {
                    if (task == null) {
                        task = debugger.getRequestProcessor().create(new Refresher());
                    }
                    task.schedule(200);
                }
            }
        }
        
        private class Refresher extends Object implements Runnable {
            public void run() {
                if (debugger.isStopped()) {
                    ThreadsTreeModel tm = getModel();
                    if (tm != null) {
                        tm.fireTreeChanged();
                    }
                }
            }
        }
    }
}

