/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.windows;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;


// This is almost copy of org.openide.util.UtilitiesCompositeActionMap.

/** ActionMap that delegates to current action map of provided component.
 * Used in <code>TopComopnent</code> lookup.
 * <p><b>Note: This action map is 'passive', i.e putting new mappings
 * into it makes no effect. Could be changed later.</b>
 *
 * @author Peter Zavadsky
 */
final class DelegateActionMap extends ActionMap {
    private Reference<JComponent> component;
    private ActionMap delegate;

    public DelegateActionMap(JComponent c) {
        setComponent(c);
    }

    public DelegateActionMap(TopComponent c, ActionMap delegate) {
        setComponent(c);
        this.delegate = delegate;
    }

    public int size() {
        return keys().length;
    }

    public Action get(Object key) {
        javax.swing.ActionMap m;

        if (delegate == null) {
            JComponent comp = getComponent();
            if (comp == null) {
                m = null;
            } else {
                m = comp.getActionMap();
            }
        } else {
            m = delegate;
        }

        if (m != null) {
            Action a = m.get(key);

            if (a != null) {
                return a;
            }
        }

        java.awt.Component owner = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        Action found = null;

        while ((owner != null) && (owner != getComponent())) {
            if ((found == null) && (owner instanceof JComponent)) {
                m = ((JComponent) owner).getActionMap();

                if (m != null) {
                    found = m.get(key);
                }
            }

            owner = owner.getParent();
        }

        return (owner == getComponent()) ? found : null;
    }

    public Object[] allKeys() {
        return keys(true);
    }

    public Object[] keys() {
        return keys(false);
    }

    private Object[] keys(boolean all) {
        java.util.Set<Object> keys = new java.util.HashSet<Object>();

        
        javax.swing.ActionMap m;

        if (delegate == null) {
            JComponent comp = getComponent();
            if (comp == null) {
                m = null;
            } else {
                m = comp.getActionMap();
            }
        } else {
            m = delegate;
        }

        if (m != null) {
            java.util.List<Object> l;

            if (all) {
                l = java.util.Arrays.asList(m.allKeys());
            } else {
                l = java.util.Arrays.asList(m.keys());
            }

            keys.addAll(l);
        }

        return keys.toArray();
    }

    // 
    // Not implemented
    //
    public void remove(Object key) {
        if (delegate != null) {
            delegate.remove(key);
        }
    }

    public void setParent(ActionMap map) {
        if (delegate != null) {
            delegate.setParent(map);
        }
    }

    public void clear() {
        if (delegate != null) {
            delegate.clear();
        }
    }

    public void put(Object key, Action action) {
        if (delegate != null) {
            delegate.put(key, action);
        }
    }

    public ActionMap getParent() {
        return (delegate == null) ? null : delegate.getParent();
    }

    public String toString() {
        return super.toString() + " for " + this.getComponent();
    }

    JComponent getComponent() {
        return component.get();
    }

    private void setComponent(JComponent component) {
        this.component = new WeakReference<JComponent>(component);
    }
}
