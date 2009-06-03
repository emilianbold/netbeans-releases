/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.openide.awt;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/** A delegate action that is usually associated with a specific lookup and
 * listens on certain classes to appear and disappear there.
 */
final class ContextAction<T> extends Object 
implements Action, ContextAwareAction {
    //, Presenter.Menu, Presenter.Popup, Presenter.Toolbar, PropertyChangeListener {
    /** type to check */
    private final Class<T> type;
    /** selection mode */
    final ContextSelection selectMode;
    /** performer to call */
    private final ContextActionPerformer<? super T> performer;
    /** potential enabler to check enabled state on */
    private final ContextActionEnabler<? super T> enabler;

    /** global lookup to work with */
    private final ContextManager global;

    /** support for listeners */
    private PropertyChangeSupport support;

    /** was this action enabled or not*/
    private boolean previousEnabled;

    /** Constructs new action that is bound to given context and
     * listens for changes of <code>ActionMap</code> in order to delegate
     * to right action.
     */
    public ContextAction(
        ContextActionPerformer<? super T> performer, 
        ContextActionEnabler<? super T> enabler,
        ContextSelection selectMode,
        Lookup actionContext, 
        Class<T> type,
        boolean surviveFocusChange
    ) {
        if (performer == null) {
            throw new NullPointerException("Has to provide a key!"); // NOI18N
        }
        this.enabler = enabler;
        this.type = type;
        this.selectMode = selectMode;
        this.performer = performer;
        this.global = ContextManager.findManager(actionContext, surviveFocusChange);
    }

    /*
    public ContextAction(FileObject map) {
        this(
            map.getAttribute("key"), // NOI18N
            Utilities.actionsGlobalContext(), // NOI18N
            (Action)map.getAttribute("fallback"), // NOI18N
            Boolean.TRUE.equals(map.getAttribute("surviveFocusChange")) // NOI18N
        );
    }
     */

    /** Overrides superclass method, adds delegate description. */
    public String toString() {
        return super.toString() + "[type=" + type + ", performer=" + performer + "]"; // NOI18N
    }

    /** Invoked when an action occurs.
     */
    public void actionPerformed(final java.awt.event.ActionEvent e) {
        assert EventQueue.isDispatchThread();

        global.actionPerformed(e, performer, type, selectMode);
    }

    public boolean isEnabled() {
        assert EventQueue.isDispatchThread();
        boolean r = global.isEnabled(type, selectMode, enabler);
        previousEnabled = r;
        return r;
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        boolean first = false;
        if (support== null) {
            support = new PropertyChangeSupport(this);
            first = true;
        }
        support.addPropertyChangeListener(listener);
        if (first) {
            global.registerListener(type, this);
        }
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
        if (!support.hasListeners(null)) {
            global.unregisterListener(type, this);
            support = null;
        }
    }

    public void putValue(String key, Object o) {
    }

    public Object getValue(String key) {
        if ("enabler".equals(key)) { // NOI18N
            // special API to support re-enablement
            assert EventQueue.isDispatchThread();
            updateState();
        }
        return null;
    }

    public void setEnabled(boolean b) {
    }

    void updateState() {
        PropertyChangeSupport s;
        synchronized (this) {
            s = support;
        }
        boolean prev = previousEnabled;
        if (s != null && prev != isEnabled()) {
            s.firePropertyChange("enabled", Boolean.valueOf(prev), Boolean.valueOf(!prev)); // NOI18N
        }
    }

    /** Clones itself with given context.
     */
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextAction<T>(performer, enabler, selectMode, actionContext, type, global.isSurvive());
    }

    public int hashCode() {
        int t = type.hashCode();
        int m = selectMode.hashCode();
        int p = performer.hashCode();
        return (t << 2) + (m << 1) + p;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        
        if (obj instanceof ContextAction) {
            ContextAction c = (ContextAction)obj;
            
            return type.equals(c.type) &&
                selectMode.equals(c.selectMode) &&
                performer.equals(c.performer);
        }
        return false;
    }
}

