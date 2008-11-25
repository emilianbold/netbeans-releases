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
package org.netbeans.spi.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;

/**Inner stub action class which delegates to the parent action's methods.
* Used both for context aware instances, and for internal state for
* ContextAction instances
* @author Tim Boudreau
*/
class ActionStub<T> implements Action, LookupListener, ContextAwareAction {

    private final Lookup.Result<T> lkpResult;
    private final Map<String, Object> pairs = new ConcurrentHashMap<String, Object>();
    private final PropertyChangeSupport supp = new PropertyChangeSupport(this);
    private final Lookup context;
    protected final ContextAction<T> parent;
    protected boolean enabled;

    ActionStub(Lookup context, ContextAction<T> parent) {
        assert context != null;
        this.context = context;
        this.parent = parent;
        lkpResult = context.lookupResult(parent.type);
        lkpResult.addLookupListener(this);
        if (getClass() == ActionStub.class) {
            //avoid superclass call to Retained.collection() before
            //it has initialized
            enabled = isEnabled();
        }
    }

    public Object getValue(String key) {
        Object result = _getValue(key);
        if (result == null) {
            result = parent.getValue(key);
        }
        return result;
    }

    Object _getValue(String key) {
        if (supp.getPropertyChangeListeners().length == 0) {
            //Make sure any code that updates the name runs - we are not
            //listening to the lookup
            resultChanged(null);
        }
        return pairs.get(key);
    }

    Collection<? extends T> collection() {
        return lkpResult.allInstances();
    }

    public void putValue(String key, Object value) {
        Object old = pairs.put(key, value);
        boolean fire = (old == null) != (value == null);
        if (fire) {
            fire = value != null && !value.equals(old);
            if (fire) {
                supp.firePropertyChange(key, old, value);
            }
        }
    }

    public void setEnabled(boolean b) {
        //Will throw exception
        parent.setEnabled(b);
    }

    public boolean isEnabled() {
        Collection<? extends T> targets = collection();
        assert targets != null;
        assert parent != null;
        if (supp.getPropertyChangeListeners().length == 0) {
            //No addNotify called - we may need to initialize values
            parent.change(targets, this);
        }
        return targets.isEmpty() ? false : parent.checkQuantity(targets) &&
                parent.isEnabled(targets);
    }

    volatile boolean attached;
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        supp.addPropertyChangeListener(listener);
        if (supp.getPropertyChangeListeners().length == 1) {
            attached = true;
            addNotify();
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        supp.removePropertyChangeListener(listener);
        if (supp.getPropertyChangeListeners().length == 0) {
            attached = false;
            removeNotify();
        }
    }

    protected void addNotify() {
        //used in subclasses
    }

    protected void removeNotify() {
        //used in subclasses
    }

    Object lock() {
        return parent.STATE_LOCK;
    }

    public void actionPerformed(ActionEvent e) {
        assert isEnabled() : "Not enabled: " + this + " collection " +
                collection() + " of " + parent.type.getName();
        Collection<? extends T> targets = collection();
        parent.actionPerformed(targets);
    }

    void enabledChanged(final boolean enabled) {
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                firePropertyChange("enabled", !enabled, enabled); //NOI18N
                if (ContextAction.unitTest) {
                    synchronized (parent) {
                        parent.notifyAll();
                    }
                    synchronized (this) {
                        this.notifyAll();
                    }
                }
            }
        });
    }

    void firePropertyChange (final String prop, final Object old, final Object nue) {
        Mutex.EVENT.readAccess (new Runnable() {
            public void run() {
                supp.firePropertyChange(prop, old, nue);
            }
        });
    }

    public void resultChanged(LookupEvent ev) {
        if (ContextAction.unitTest) {
            synchronized (parent) {
                parent.notifyAll();
            }
        }
        synchronized(parent.STATE_LOCK) {
            parent.change (collection(), this == parent.stub ? parent : this);
        }
        boolean old = enabled;
        enabled = isEnabled();
        if (old != enabled) {
            enabledChanged(enabled);
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[name=" + getValue(NAME) + //NOI18N
                "delegating={" + parent + "} context=" + //NOI18N
                context + "]"; //NOI18N
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return parent.createStub(actionContext);
    }
}
