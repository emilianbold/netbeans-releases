/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import org.openide.util.Exceptions;

/**
 * Abstract parent class for misc classes that need to listen for property
 * changes on something else and fire its own changes in turn, but which
 * should not listen to that object unless something is listening to it.
 *
 * @author Tim Boudreau
 */
public abstract class ListenerProxy<T> {
    private final List<PropertyChangeListener> l = new LinkedList<PropertyChangeListener>();
    private final T obj;
    private final PropertyChangeListener pcl = new PCL(this);

    protected ListenerProxy(T obj) {
        this.obj = obj;
    }

    protected final T get() {
        return obj;
    }

    final void addNotify() {
        attach(get(), pcl);
    }

    final void removeNotify() {
        detach(get(), pcl);
    }

    protected final Object lock() {
        return l;
    }

    /**
     * Start listening to the underlying object, because a listener has been
     * attached
     * @param obj The object from the constructor
     * @param precreatedListener A listener which will invoke onChange() as needed
     */
    protected abstract void attach(T obj, PropertyChangeListener precreatedListener);

    /**
     * Stop listening to the underlying object, because the last listener has been
     * detached
     * @param obj The object from the constructor
     * @param precreatedListener A listener which will invoke onChange() as needed
     */
    protected abstract void detach(T obj, PropertyChangeListener precreatedListener);

    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        synchronized (l) {
            boolean empty = l.isEmpty();
            l.add(listener);
            if (empty) {
                addNotify();
            }
        }
    }

    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        synchronized (l) {
            l.remove(listener);
            if (l.isEmpty()) {
                removeNotify();
            }
        }
    }

    protected final void fire(String name, Object old, Object nue) {
        PropertyChangeListener[] ls;
        synchronized (l) {
            ls = l.toArray(new PropertyChangeListener[l.size()]);
        }
        if (ls.length > 0) {
            Fire f = new Fire(ls, name, old, nue);
            fireChanges (f);
        }
    }

    protected abstract void onChange(String prop, Object old, Object nue);

    /**
     * Invoke a runnable which will fire property changes.  Subclasses may
     * replan events to the event thread, wrap them in a mutex action, or
     * whatever else is necessary.
     * @param run A runnable which will fire whatever changes are necessary
     */
    protected void fireChanges(Runnable run) {
        run.run();
    }

    private static final class PCL implements PropertyChangeListener {
        private final ListenerProxy<?> proxy;
        PCL (ListenerProxy<?> proxy) {
            this.proxy = proxy;
        }

        @Override
        public final void propertyChange(PropertyChangeEvent evt) {
            proxy.onChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }

    private final class Fire implements Runnable {

        private final String name;
        private final PropertyChangeListener[] ls;
        private final Object old;
        private final Object nue;

        private Fire(PropertyChangeListener[] ls, String name, Object old, Object nue) {
            this.ls = ls;
            this.name = name;
            this.old = old;
            this.nue = nue;
        }

        @Override
        public void run() {
            PropertyChangeEvent pce = new PropertyChangeEvent(ListenerProxy.this, name, old, nue);
            for (PropertyChangeListener p : ls) {
                try {
                    p.propertyChange(pce);
                } catch (RuntimeException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }
}
