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

package org.netbeans.modules.debugger.jpda.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.openide.util.RequestProcessor;

/**
 * A keymap that filters ENTER, ESC and TAB, which have special meaning in dialogs
 *
 * @author Martin Entlicher
 */
public class FilteredKeymap implements Keymap {

    private final javax.swing.KeyStroke enter = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0);
    private final javax.swing.KeyStroke esc = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0);
    private final javax.swing.KeyStroke tab = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, 0);
    private final Keymap keyMap; // The original keymap
    
    /** Creates a new instance of FilteredKeymap */
    public FilteredKeymap(final JTextComponent component) {
        
        class KeymapUpdater implements Runnable {
            public void run() {
                component.setKeymap(new FilteredKeymap(component));
            }
        }
        
        this.keyMap = component.getKeymap();
        component.addPropertyChangeListener("keymap", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (!(evt.getNewValue() instanceof FilteredKeymap)) {
                    // We have to do that lazily, because the property change
                    // is fired *before* the keymap is actually changed!
                    component.removePropertyChangeListener("keymap", this);
                    if (EventQueue.isDispatchThread()) {
                        EventQueue.invokeLater(new KeymapUpdater());
                    } else {
                        RequestProcessor.getDefault().post(new KeymapUpdater(), 100);
                    }
                }
            }
        });
    }
    
    public void addActionForKeyStroke(KeyStroke key, Action a) {
        keyMap.addActionForKeyStroke(key, a);
    }
    public Action getAction(KeyStroke key) {
        if (enter.equals(key) ||
            esc.equals(key) ||
            tab.equals(key)) {

            return null;
        } else {
            return keyMap.getAction(key);
        }
    }
    public Action[] getBoundActions() {
        return keyMap.getBoundActions();
    }
    public KeyStroke[] getBoundKeyStrokes() {
        return keyMap.getBoundKeyStrokes();
    }
    public Action getDefaultAction() {
        return keyMap.getDefaultAction();
    }
    public KeyStroke[] getKeyStrokesForAction(Action a) {
        return keyMap.getKeyStrokesForAction(a);
    }
    public String getName() {
        return keyMap.getName()+"_Filtered"; //NOI18N
    }
    public javax.swing.text.Keymap getResolveParent() {
        return keyMap.getResolveParent();
    }
    public boolean isLocallyDefined(KeyStroke key) {
        if (enter.equals(key) ||
            esc.equals(key) ||
            tab.equals(key)) {
            
            return false;
        } else {
            return keyMap.isLocallyDefined(key);
        }
    }
    public void removeBindings() {
        keyMap.removeBindings();
    }
    public void removeKeyStrokeBinding(KeyStroke keys) {
        keyMap.removeKeyStrokeBinding(keys);
    }
    public void setDefaultAction(Action a) {
        keyMap.setDefaultAction(a);
    }
    public void setResolveParent(javax.swing.text.Keymap parent) {
        keyMap.setResolveParent(parent);
    }
    
}
