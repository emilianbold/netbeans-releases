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

package org.netbeans.core.multiview;


import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import java.util.Arrays;
import org.openide.windows.TopComponent;


/** ActionMap that delegates to current action map of provided component and dynamically also the current element.
 * Used in <code>MultiViewTopComopnent</code> lookup.
 *
 * @author Milos Kleint
 */
final class MultiViewActionMap extends ActionMap {
    private ActionMap delegate;
    private ActionMap topComponentMap;
    private TopComponent component;
    
    private boolean preventRecursive = false;
    private Object LOCK = new Object();

    public MultiViewActionMap(TopComponent tc, ActionMap tcMap) {
        topComponentMap = tcMap;
        component = tc;
    }
    
    public void setDelegateMap(ActionMap map) {
        delegate = map;
    }
    
    public int size() {
        return keys ().length;
    }

    public Action get(Object key) {
        // the multiview's action map first.. for stuff like the closewindow and clonewindow from TopComponent.initActionMap
        javax.swing.ActionMap m = topComponentMap;
        if (m != null) {
            Action a = m.get (key);
            if (a != null) {
                return a;
            }
        }
        // delegate then
        m = delegate;
        if (m != null) {
            //this is needed because of Tc's DelegateActionMap which traverses up the component hierarchy.
            // .. results in calling this method again and again and again. -> stackoverflow.
            // this should break the evil cycle.
            synchronized (LOCK) {
                if (preventRecursive) {
                    preventRecursive = false;
                    return null;
                }
                preventRecursive = true;
                Action a = m.get (key);
                preventRecursive = false;
                if (a != null) {
                    return a;
                }
            }
        }
        
        java.awt.Component owner = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        Action found = null;
        while (owner != null && owner != component) {
            if (found == null && (owner instanceof JComponent)) {
                m = ((JComponent)owner).getActionMap ();
                if (m != null) {
                    found = m.get (key);
                }
            }
            owner = owner.getParent ();
        }
        
        return owner == component ? found : null;
    }

    public Object[] allKeys() {
        return keys (true);
    }

    public Object[] keys() {
        return keys (false);
    }


    private Object[] keys(boolean all) {
        java.util.Set keys = new java.util.HashSet();

        if (delegate != null) {
            java.util.List l;
            if (all) {
                l = Arrays.asList(delegate.allKeys());
            } else {
                l = Arrays.asList(delegate.keys());
            }
            keys.addAll(l);
        }
        
        if (topComponentMap != null) {
            java.util.List l;

            if (all) {
                l = Arrays.asList (topComponentMap.allKeys ());
            } else {
                l = Arrays.asList (topComponentMap.keys ());
            }

            keys.addAll (l);
        }

        return keys.toArray();
    }

    // 
    // Not implemented
    //
    public void remove(Object key) {
        topComponentMap.remove(key);
    }        

    public void setParent(ActionMap map) {
        topComponentMap.setParent(map);
    }

    public void clear() {
        topComponentMap.clear();
    }

    public void put(Object key, Action action) {
        topComponentMap.put (key, action);
    }

    public ActionMap getParent() {
        return topComponentMap.getParent();
    }
 
}    
