/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

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
    public FilteredKeymap(Keymap keyMap) {
        this.keyMap = keyMap;
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
