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
package org.openide.util;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import javax.swing.*;


/** ActionMap that is composed from all Components up to the ExplorerManager.Provider
*
* @author   Jaroslav Tulach
*/
final class UtilitiesCompositeActionMap extends ActionMap {
    private Component component;

    public UtilitiesCompositeActionMap(Component c) {
        this.component = c;
    }

    public int size() {
        return keys().length;
    }

    public Action get(Object key) {
        Component c = component;

        for (;;) {
            if (c instanceof JComponent) {
                javax.swing.ActionMap m = ((JComponent) c).getActionMap();

                if (m != null) {
                    Action a = m.get(key);

                    if (a != null) {
                        return a;
                    }
                }
            }

            if (c instanceof Lookup.Provider) {
                break;
            }

            c = c.getParent();

            if (c == null) {
                break;
            }
        }

        return null;
    }

    public Object[] allKeys() {
        return keys(true);
    }

    public Object[] keys() {
        return keys(false);
    }

    private Object[] keys(boolean all) {
        java.util.HashSet<Object> keys = new java.util.HashSet<Object>();

        Component c = component;

        for (;;) {
            if (c instanceof JComponent) {
                javax.swing.ActionMap m = ((JComponent) c).getActionMap();

                if (m != null) {
                    java.util.List<Object> l;

                    if (all) {
                        l = java.util.Arrays.asList(m.allKeys());
                    } else {
                        l = java.util.Arrays.asList(m.keys());
                    }

                    keys.addAll(l);
                }
            }

            if (c instanceof Lookup.Provider) {
                break;
            }

            c = c.getParent();

            if (c == null) {
                break;
            }
        }

        return keys.toArray();
    }

    // 
    // Not implemented
    //
    public void remove(Object key) {
    }

    public void setParent(ActionMap map) {
    }

    public void clear() {
    }

    public void put(Object key, Action action) {
    }

    public ActionMap getParent() {
        return null;
    }
}
