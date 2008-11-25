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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Merges multiple contextActions or their stubs.
 *
 * @author Tim Boudreau
 */
final class MergeAction implements ContextAwareAction, PropertyChangeListener {
    private static final String ENABLED = "enabled"; //NOI18N
    private Action[] actions;
    private PropertyChangeSupport supp = new PropertyChangeSupport(this);
    private Map<String, Object> knownValues = new HashMap<String, Object>();
    private Action delegateAction;
    private volatile boolean enabled;
    final boolean allowOnlyOne;

    public MergeAction(Action[] actions, boolean allowOnlyOne) {
        this.actions = actions;
        this.allowOnlyOne = allowOnlyOne;
        assert actions.length > 0;
        for (int i = 0; i < actions.length; i++) {
            Parameters.notNull("Action " + i, actions[i]); //NOI18N
            assert actions[i] instanceof ContextAction || actions[i] instanceof ActionStub;
        }
        //prime our key set common keys
        knownValues.put(NAME, null);
        knownValues.put(ACCELERATOR_KEY, null);
        knownValues.put(LONG_DESCRIPTION, null);
        knownValues.put(SMALL_ICON, null);
        knownValues.put(SHORT_DESCRIPTION, null);
        knownValues.put(LONG_DESCRIPTION, null);
        knownValues.put(SMALL_ICON, null);
        knownValues.put(MNEMONIC_KEY, null);
        knownValues.put("noIconInMenu", null);
        knownValues.put(ENABLED, null);
    }

    public MergeAction(Action[] actions) {
        this (actions, false);
    }

    Action updateDelegateAction() {
        assert attached;
        synchronized (this) {
            return setDelegateAction(findEnabledAction());
        }
    }

    Action setDelegateAction(Action a) {
        assert Thread.holdsLock(this);
        synchronized (this) {
            Action old = this.delegateAction;
            if (old != a) {
                delegateAction = a;
                boolean nowEnabled = getDelegateAction().isEnabled();
                if (nowEnabled != enabled) {
                    enabled = nowEnabled;
                    supp.firePropertyChange(ENABLED, !enabled, enabled);
                }
                if (a != null) {
                    sievePropertyChanges();
                }
            }
        }
        return a;
    }

    Action findEnabledAction() {
        Action result = null;
        int enaCount = 0;
        for (Action a : actions) {
            //We want to, if necessary, briefly addNotify() the action,
            //so run it inside an ActionRunnable.
            ActionRunnable<Boolean> ar = new ActionRunnable<Boolean>() {

                public Boolean run(ContextAction<?> a) {
                    return a.isEnabled();
                }

                public Boolean run(ActionStub<?> a) {
                    return a.isEnabled();
                }
            };
            if (runActive(ar, a)) {
                enaCount++;
                if (!allowOnlyOne) {
                    result = a;
                    break;
                } else if (result == null) {
                    result = a;
                }
            }
        }
        if (allowOnlyOne && enaCount > 1 && result != null) {
            result = null;
        }
        return result;
    }

    Action getDelegateAction() {
        Action result = null;
        synchronized (this) {
            result = delegateAction;
            if (result == null || !result.isEnabled()) {
                if (attached) {
                    result = updateDelegateAction();
                } else {
                    result = findEnabledAction();
                }
            }
        }
        if (result == null) {
            result = actions[0];
        }
        return result;
    }

    private void sievePropertyChanges() {
        Map<String, Object> nue = new HashMap<String, Object>();
//        Action del = getDelegateAction();
        for (String key : knownValues.keySet()) {
            Object expected = knownValues.get(key);
            Object found = getValue(key);//del.getValue(key);
//            Object found = del.getValue(key);
            if (found != expected) {
                nue.put(key, found);
                supp.firePropertyChange(key, expected, found);
            }
        }
    }

    interface ActionRunnable<T> {

        T run(ContextAction<?> a);

        T run(ActionStub<?> a);
    }

    <T> T runActive(ActionRunnable<T> ar, Action a) {
        assert a instanceof ContextAction || a instanceof ActionStub;
//        synchronized (a instanceof ActionStub ? ((ActionStub)a).lock() :
//            ((ContextAction) a).STATE_LOCK) {
            boolean wasActive = a instanceof ContextAction ? ((ContextAction) a).attached : ((ActionStub) a).attached;
            try {
                if (!wasActive) {
                    if (a instanceof ContextAction) {
                        ((ContextAction) a).addNotify();
                        return ar.run((ContextAction) a);
                    } else {
                        ((ActionStub) a).addNotify();
                        return ar.run((ActionStub) a);
                    }
                }
                if (a instanceof ContextAction) {
                    return ar.run((ContextAction) a);
                } else {
                    return ar.run((ActionStub) a);
                }
            } finally {
                if (!wasActive) {
                    if (a instanceof ContextAction) {
                        ((ContextAction) a).removeNotify();
                    } else {
                        ((ActionStub) a).removeNotify();
                    }
                }
            }
//        }
    }
    volatile boolean attached;

    synchronized void addNotify() {
        attached = true;
        for (Action a : actions) {
            a.addPropertyChangeListener(this);
        }
        updateDelegateAction();
    }

    synchronized void removeNotify() {
        attached = false;
        for (Action a : actions) {
            a.removePropertyChangeListener(this);
        }
        setDelegateAction(null);
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        Action[] stubs = new Action[actions.length];
        for (int i = 0; i < stubs.length; i++) {
            stubs[i] = ((ContextAwareAction) actions[i]).createContextAwareInstance(actionContext);
        }
        MergeAction result = new MergeAction(stubs, allowOnlyOne);
        result.knownValues.putAll(knownValues);
        result.pairs.putAll(pairs);
        return result;
    }
    private Map<String, Object> pairs = new HashMap<String, Object>();

    public Object getValue(final String key) {
        Object result = pairs.get(key);
        if (result == null) {
//            synchronized (this) {
                if (isEnabled()) {
                    Action del = getDelegateAction();
                    result = del.getValue(key);
                }
//            }
        }
        if (result == null) {
            ActionRunnable<Object> ar = new ActionRunnable<Object>() {

                public Object run(ContextAction<?> a) {
                    return a.getValue(key);
                }

                public Object run(ActionStub<?> a) {
                    return a.getValue(key);
                }
            };
            for (Action a : actions) {
                result = runActive(ar, a);
                if (result != null) {
                    break;
                }
            }
        }
        this.knownValues.put(key, result);
        return result;
    }
    boolean logged;

    public void putValue(String key, Object value) {
        if (!logged) {
            Logger.getLogger(MergeAction.class.getName()).log(Level.INFO,
                    "putValue (" + key + ',' + value + //NOI18N
                    "called on merged action.  This is probably a mistake."); //NOI18N
        }
        pairs.put(key, value);
    }

    public void setEnabled(boolean b) {
        throw new UnsupportedOperationException("Illegal"); //NOI18N
    }

    public boolean isEnabled() {
        return updateEnabled();
    }

    boolean updateEnabled() {
        enabled = getDelegateAction().isEnabled();
        if (allowOnlyOne && enabled) {
            if (findEnabledAction() == null) {
                enabled = false;
            }
        }
        return enabled;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        supp.addPropertyChangeListener(listener);
        if (supp.getPropertyChangeListeners().length == 1) {
            addNotify();
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        supp.removePropertyChangeListener(listener);
        if (supp.getPropertyChangeListeners().length == 0) {
            removeNotify();
        }
    }

    public void actionPerformed(ActionEvent e) {
        Action a = getDelegateAction();
        if (a == null) {
            throw new IllegalStateException("Not enabled or no delegate: " + //NOI18N
                    this);
        }
        a.actionPerformed(e);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        boolean old = enabled;
        synchronized (this) {
            if (attached) {
                updateDelegateAction();
            }
        }
        boolean nowEnabled = isEnabled();
        if (!ENABLED.equals(evt.getPropertyName())) {
            Object last = knownValues.get(evt.getPropertyName());
            Object mine = getValue(evt.getPropertyName());
            if (mine != last) {
                supp.firePropertyChange(evt.getPropertyName(), last, mine);
            }
            knownValues.put(evt.getPropertyName(), evt.getNewValue());
        }
        if (old != nowEnabled) {
            supp.firePropertyChange(ENABLED, old, nowEnabled);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder (super.toString());
        sb.append ('['); //NOI18N
        for (int i = 0; i < actions.length; i++) {
            sb.append (actions[i]);
            if (i != actions.length - 1) {
                sb.append (','); //NOI18N
            }
        }
        sb.append(']'); //NOI18N
        return sb.toString();
    }
}
