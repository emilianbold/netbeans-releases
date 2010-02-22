/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.propdos;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * ObservableProperties implementation which can resolve properties within
 * itself ala Ant (i.e. references to other properties can be delimited
 * by <code>${propertyName}</code> and getProperty() will dereference
 * such values).
 *
 * @author Tim Boudreau
 */
public class AntStyleResolvingProperties extends ObservableProperties {
    //This does not need synchronization directly as long as it is accessed
    //under a lock on this - it will always be called from put() which
    //is synchronized
    private final Set<PropertyChangeEvent> pendingEvents = new HashSet<PropertyChangeEvent>();
    private final List<PropertyChangeListener> listeners = new LinkedList<PropertyChangeListener>();
    private volatile boolean loading;
    private final Runnable r = new R();
    private static RequestProcessor rp = new RequestProcessor(
            "Resolving Properties", 1, true); //NOI18N
    private final RequestProcessor.Task task = rp.create(r);
    private final boolean sync;

    public AntStyleResolvingProperties() {
        this(false);
    }

    public AntStyleResolvingProperties(boolean fireChangesSynchronously) {
        this.sync = fireChangesSynchronously;
    }

    @Override
    public String getProperty (String key, String def) {
        String result = getProperty (key);
        return result == null ? def : result;
    }

    @Override
    public String getProperty (String key) {
        return getProperty (key, true);
    }

    public String getProperty (String key, boolean resolve) {
        String result = (String) get(key);
        if (resolve && result != null && result.indexOf ("${") >= 0) { //NOI18N
            result = resolve (key, result);
        }
        return result;
    }

    protected boolean isSavableProperty (String key) {
        return true;
    }

    static final Pattern NESTED_PROP_PATTERN = Pattern.compile("\\$\\{(.*?)\\}"); //NOI18N
    /**
     * Take the given key and value, and convert ${propname} delimited
     * properties to their resolved value
     * @param key The property key being searched for, to avoid recursive
     * invocation
     * @param value The raw value, including delimited strings
     * @return The dereferenced value
     */
    public String resolve (String key, String value) {
        if (value != null && value.equals("${" + key + "}")) { //NOI18N
            return key;
        }
        StringBuilder sb = new StringBuilder(value);
        Matcher m = NESTED_PROP_PATTERN.matcher(sb);
        while (m.find()) {
            String resolveKey = m.group(1);
            if (!resolveKey.equals(key)) {
                String replacement = getProperty(resolveKey);
                if (replacement != null) {
                    int start = m.start();
                    int end = m.end();
                    sb.replace(start, end, replacement);
                    m = NESTED_PROP_PATTERN.matcher(sb);
                }
            }
        }
        return sb.toString().trim();
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

    @Override
    public void store(OutputStream out, String comments) throws IOException {
        if (PropertiesBasedDataObject.LOGGER.isLoggable(Level.FINE)) {
            PropertiesBasedDataObject.LOGGER.log(Level.FINE, "Writing a {0} to disk",  //NOI18N
                    getClass().getName());
        }
        if (PropertiesBasedDataObject.LOGGER.isLoggable(Level.FINEST)) {
            PropertiesBasedDataObject.LOGGER.log(Level.FINEST, "DATA:{0}", this); //NOI18N
        }
        EditableProperties props = new EditableProperties(true);
        for (Map.Entry<Object,Object> e : entrySet()) {
            String key = (String) e.getKey();
            if (isSavableProperty(key)) {
                props.setProperty(key, getProperty(key, false));
            }
        }
        props.store(out);
    }

    private final class R implements Runnable {
        public void run() {
            if (!EventQueue.isDispatchThread()) {
                try {
                    onChangeOccurred();
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

    /**
     * Called on a background thread (or immediately, if true passed to constructor)
     * after a change has occurred.  Can
     * be used to rewrite this data to disk asynchronously.
     * @throws IOException
     */
    void onChangeOccurred() throws IOException {

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

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        synchronized (r) {
            listeners.add(pcl);
        }
    }

    protected void fireEvents(Collection<PropertyChangeEvent> events) {
        synchronized (this) {
            pendingEvents.addAll(events);
        }
        firePendingChangeEvents();
    }

    protected final boolean isLoading() {
        return loading;
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

    @Override
    public synchronized Object put(Object key, Object value) {
        Object result = super.put(key, value);
        if (!equals(value, result)) {
            if (!loading) {
                CoalescablePropertyChangeEvent evt = new CoalescablePropertyChangeEvent(this, key.toString(), result, value);
                pendingEvents.add(evt);
                if (PropertiesBasedDataObject.LOGGER.isLoggable(Level.FINEST)) {
                    PropertiesBasedDataObject.LOGGER.log(Level.FINEST, "Scheduling write of " + this //NOI18N
                            + " in 500ms due to write of property " + key + " to " + value, new Exception()); //NOI18N
                }
                if (!Boolean.getBoolean("JCProjectTest")) { //NOI18N
                    //NOI18N
                    if (!sync) {
                        task.schedule(500);
                    } else {
                        r.run();
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        synchronized (r) {
            listeners.remove(pcl);
        }
    }
    private static final class CoalescablePropertyChangeEvent extends PropertyChangeEvent {

        CoalescablePropertyChangeEvent(AntStyleResolvingProperties source, String name, Object old, Object nue) {
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
