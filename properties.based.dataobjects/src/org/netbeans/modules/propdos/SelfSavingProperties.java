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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.propdos;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileSystem;

/**
 * Properties object what has the ability to auto-save on a time delay after
 * any modification.  Calls to put() will fire property changes.  Property
 * changes are fired on the event queue, only *after* the changes have
 * been written to disk.
 *
 * @author Tim Boudreau
 */
abstract class SelfSavingProperties extends ObservableProperties {

    private final Runnable r = new R();
    private static RequestProcessor rp = new RequestProcessor(
            "Self Saving Properties", 1, true); //NOI18N
    private final RequestProcessor.Task task = rp.create(r);
    //A properties subclass that writes itself to disk on a timer after
    //it is modified.  Customizer UI does not have to explicitly save it.
    protected final DataObject dob;
    //This does not need synchronization directly as long as it is accessed
    //under a lock on this - it will always be called from put() which
    //is synchronized
    private final Set<PropertyChangeEvent> pendingEvents = new HashSet<PropertyChangeEvent>();
    private final List<PropertyChangeListener> listeners = new LinkedList<PropertyChangeListener>();

    SelfSavingProperties(DataObject ob) {
        this.dob = ob;
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        Object result = super.put(key, value);
        if (!equals(value, result)) {
            if (!loading) {
                task.schedule(500);
                CoalescablePropertyChangeEvent evt =
                    new CoalescablePropertyChangeEvent(
                    this, key.toString(), result, value);
                pendingEvents.add(evt);
                if (PropertiesBasedDataObject.LOGGER.isLoggable(Level.FINEST)) {
                    PropertiesBasedDataObject.LOGGER.log(Level.FINEST,
                            "Scheduling write of " + dob.getPrimaryFile().getPath() + //NOI18N
                            " in 500ms due to write of property " + key + " to " + //NOI18N
                            value, new Exception());
                }
            }
        }
        return result;
    }
    private volatile boolean loading;

    protected void fireEvents (Collection<PropertyChangeEvent> events) {
        synchronized(this) {
            pendingEvents.addAll(events);
        }
        firePendingChangeEvents();
    }

    private boolean equals(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        } else if ((a == null) != (b == null)) {
            return false;
        } else if (a != null) {
            return a.equals(b);
        } else {
            return b.equals(a);
        }
    }

    protected final boolean isLoading() {
        return loading;
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        loading = true;
        try {
            super.load(inStream);
        } finally {
            loading = false;
        }
    }

    private final class R implements Runnable {

        public void run() {
            if (!EventQueue.isDispatchThread()) {
                try {
                    if (PropertiesBasedDataObject.LOGGER.isLoggable(Level.FINEST)) {
                        PropertiesBasedDataObject.LOGGER.log(Level.FINEST,
                                "Begin write of " + //NOI18N
                                SelfSavingProperties.this.dob.getPrimaryFile().getPath(),
                                new Exception());
                    }
                    write();
                    if (PropertiesBasedDataObject.LOGGER.isLoggable(Level.FINEST)) {
                        PropertiesBasedDataObject.LOGGER.log(Level.FINEST,
                                "Successful write of " + //NOI18N
                                SelfSavingProperties.this.dob.getPrimaryFile().getPath() +
                                " invoking onWriteCompleted() in " +
                                getClass().getName()); //NOI18N
                    }
                    onWriteCompleted();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    EventQueue.invokeLater(this);
                }
            } else {
                firePendingChangeEvents();
            }
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        synchronized (r) {
            listeners.add(pcl);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        synchronized (r) {
            listeners.remove(pcl);
        }
    }

    private void firePendingChangeEvents() {
        Set<PropertyChangeEvent> toFire;
        synchronized (this) {
            toFire = new HashSet<PropertyChangeEvent>(pendingEvents);
            pendingEvents.clear();
        }
        PropertyChangeListener[] ls;
        synchronized (r) {
            ls = listeners.toArray(new PropertyChangeListener[listeners.size()]);
        }
        for (PropertyChangeListener l : ls) {
            for (PropertyChangeEvent e : toFire) {
                try {
                    l.propertyChange(e);
                } catch (RuntimeException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    protected abstract void onWriteCompleted() throws IOException;

    volatile boolean writing;
    private void write() throws IOException {
        //Method may be reentered while another thread is still writing
        if (writing || !dob.isValid()) return;
        final FileObject fo = dob.getPrimaryFile();
        fo.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                writing = true;
                FileLock lock = fo.lock();
                OutputStream out = fo.getOutputStream(lock);
                try {
                    SelfSavingProperties.this.store(out, ""); //NOI18N
                } catch (FileAlreadyLockedException locked) {
                    if (PropertiesBasedDataObject.LOGGER.isLoggable(Level.WARNING)) {
                        PropertiesBasedDataObject.LOGGER.log(Level.WARNING,
                                "Could not write " + dob.getPrimaryFile().getPath() + //NOI18N
                                " - already locked", locked); //NOI18N
                    }
                } finally {
                    writing = false;
                    out.close();
                    lock.releaseLock();
                }
            }
        });
    }

    private static final class CoalescablePropertyChangeEvent extends PropertyChangeEvent {

        CoalescablePropertyChangeEvent(SelfSavingProperties source, String name, Object old, Object nue) {
            super(source, name, old, nue);
            Parameters.notNull("property name", name); //NOI18N
            Parameters.notNull("source", source); //NOI18N
        }

        @Override
        public boolean equals(Object obj) {
            boolean result = obj != null && obj.getClass() == CoalescablePropertyChangeEvent.class;
            if (result) {
                CoalescablePropertyChangeEvent other = (CoalescablePropertyChangeEvent) obj;
                Object otherSource = other.getSource();
                result = otherSource == getSource();
                if (result) {
                    String otherName = other.getPropertyName();
                    result = getPropertyName().equals(otherName);
                }
            }
            return result;
        }

        @Override
        public int hashCode() {
            int srcCode = System.identityHashCode(getSource());
            return srcCode * 41 * getPropertyName().hashCode();
        }
    }
}