/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.gdb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.proxy.GdbProxy;
import org.netbeans.modules.cnd.debugger.gdb.utils.CommandBuffer;

/**
 * Describes a context of debugging
 * it changes every time you do a step
 * @author eu155513
 */
public class GdbContext implements PropertyChangeListener {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Map<String,Request> requests = new HashMap<String,Request>();
    private final Map<String,Object> cache = new HashMap<String,Object>();
    private final Map<String,Object> waitLocks = new HashMap<String,Object>();
    
    private static final int SYNC_UPDATE_TIMEOUT=30000;
    
    public static final String PROP_REGISTERS = "Registers"; // NOI18N
    
    private GdbContext() {
        requests.put(PROP_REGISTERS, new Request() {
            protected void request(CommandBuffer cb, GdbProxy gdb) {
                gdb.data_list_register_values(cb, ""); // NOI18N
                gdb.data_list_changed_registers(cb);
            }
        });
        pcs.addPropertyChangeListener(this); // used to notify sync updates
    }
    
    public void invalidate(boolean fireUpdates) {
        cache.clear();
        
        // fire updates if requested
        if (fireUpdates) {
            for (String propertyName : requests.keySet()) {
                if (hasListeners(propertyName)) {
                    pcs.firePropertyChange(propertyName, 0, 1);
                }
            }
        }
    }
    
    public void update() {
        invalidate(false);
        //request update of all properties that have listeners
        for (Map.Entry<String,Request> entry : requests.entrySet()) {
            if (hasListeners(entry.getKey())) {
                entry.getValue().run(true);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        Object lock = waitLocks.get(evt.getPropertyName());
        if (lock != null) {
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }
    
    public Object getProperty(String name) {
        Object res = cache.get(name);
        // If it is not in cache request it and wait for context change
        if (res == null) {
            boolean child = true;
            Object lock = waitLocks.get(name);
            if (lock == null) {
                child = false;
                lock = "Lock for " + name; // NOI18N
            }
            synchronized (lock) {
                waitLocks.put(name, lock);
                if (child || requests.get(name).run(true)) {
                    try {
                        lock.wait(SYNC_UPDATE_TIMEOUT);
                    } catch (InterruptedException ie) {
                    }
                }
                waitLocks.remove(name);
            }
            return cache.get(name);
        }
        return res;
    }
    
    public void setProperty(String name, Object value) {
        cache.put(name, value);
        pcs.firePropertyChange(name, 0, 1);
    }

    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
        //request property update if needed
        if (!cache.containsKey(propertyName)) {
            requests.get(propertyName).run(true);
        }
    }
    
    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }
    
    private synchronized boolean hasListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName).length > 0;
    }
    
    private static GdbContext instance = null;
    public static synchronized GdbContext getInstance() {
        if (instance == null) {
            instance = new GdbContext();
        }
        return instance;
    }
    
    public static GdbProxy getCurrentGdb() {
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (currentEngine == null) {
            return null;
        }
        GdbDebugger debugger = currentEngine.lookupFirst(null, GdbDebugger.class);
        if (debugger == null) {
            return null;
        }
        return debugger.getGdbProxy();
    }
    
    /////// Request
    private static abstract class Request {
        public boolean run(boolean async) {
            GdbProxy gdb = getCurrentGdb();
            if (gdb != null) {
                CommandBuffer cb = null;
                if (!async) {
                    cb = new CommandBuffer(gdb);
                }
                request(cb, gdb);
                if (cb != null) {
                    cb.waitForCompletion();
                }
                return true;
            }
            return false;
        }
        
        protected abstract void request(CommandBuffer cb, GdbProxy gdb);
    }
}
