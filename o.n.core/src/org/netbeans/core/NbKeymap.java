/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Mutex;

/** Implementation of standard key - action mappings.
*
* @author Dafe Simonek
*/
public final class NbKeymap extends Observable implements Keymap, Comparator<KeyStroke> {
    /** Name of this keymap */
    String name;
    /** Parent keymap */
    Keymap parent;
    /** Hashtable holding KeyStroke > Action mappings */
    Map<KeyStroke,Action> bindings;
    /** Default action */
    Action defaultAction;
    /** hash table to map (Action -> ArrayList of KeyStrokes) */
    Map<Action,List<KeyStroke>> actions;
    
    private static List<KeyStroke> context = new ArrayList<KeyStroke>();
    
    public static void resetContext() {
        context.clear();
        StatusDisplayer.getDefault().setStatusText("");
    }

    public static KeyStroke[] getContext() {
        return (KeyStroke[]) context.toArray(new KeyStroke[context.size()]);
    }
    
    public static void shiftContext(KeyStroke stroke) {
        context.add(stroke);

        StringBuilder text = new StringBuilder();
        for (KeyStroke ks: context) {
            text.append(getKeyText(ks)).append(' ');
        }
        StatusDisplayer.getDefault().setStatusText(text.toString());        
    }
    
    private static String getKeyText (KeyStroke keyStroke) {
        if (keyStroke == null) return "";
        String modifText = KeyEvent.getKeyModifiersText 
            (keyStroke.getModifiers ());
        if ("".equals (modifText))       
            return KeyEvent.getKeyText (keyStroke.getKeyCode ());
        return modifText + "+" +                                // NOI18N
            KeyEvent.getKeyText (keyStroke.getKeyCode ()); 
    }
           
    private final Action NO_ACTION = new KeymapAction(null, null);
    
    public Action createMapAction(Keymap k, KeyStroke stroke) {
        return new KeymapAction(k, stroke);
    }

    /** Default constructor
    */
    public NbKeymap() {
        this("Default", null); // NOI18N
    }

    NbKeymap(final String name, final Keymap parent) {
        this.name = name;
        this.parent = parent;
        bindings = new HashMap<KeyStroke,Action>();
    }

    public Action getDefaultAction() {
        if (defaultAction != null) {
            return defaultAction;
        }
        return (parent != null) ? parent.getDefaultAction() : null;
    }

    public void setDefaultAction(Action a) {
        defaultAction = a;
        setChanged();
        notifyObservers();
    }

    public String getName() {
        return name;
    }

    public Action getAction(KeyStroke key) {
        Action a;

        KeyStroke[] ctx = getContext();
        Keymap activ = this;
        for (int i=0; i<ctx.length; i++) {
            if (activ == this) {
                a = bindings.get(ctx[i]);
                if ((a == null) && (parent != null)) {
                    a = parent.getAction(ctx[i]);
                }
            } else {
                a = activ.getAction(ctx[i]);
            }
            
            if (a instanceof KeymapAction) {
                activ = ((KeymapAction)a).keymap;
            } else { // unknown ctx
                int code = key.getKeyCode();
                if (code != KeyEvent.VK_CONTROL &&
                        code != KeyEvent.VK_ALT &&
                        code != KeyEvent.VK_ALT_GRAPH &&
                        code != KeyEvent.VK_SHIFT &&
                        code != KeyEvent.VK_META) resetContext();
                return null;
            }
        }
        
        if (activ == this) {
            a = bindings.get(key);
            if ((a == null) && (parent != null)) {
                a = parent.getAction(key);
            }
            return a;
        } else {
            a = activ.getAction(key);
        }
        
        if (a != null) {
            if (!(a instanceof KeymapAction)) {
                resetContext();
            }
            return a;
        }
            
        // no action, should we reset?
        if (key.isOnKeyRelease() ||
            (key.getKeyChar() != 0 && key.getKeyChar() != KeyEvent.CHAR_UNDEFINED)) {
                return null;
        }
            
        switch (key.getKeyCode()) {
            case KeyEvent.VK_SHIFT:
            case KeyEvent.VK_CONTROL:
            case KeyEvent.VK_ALT:
            case KeyEvent.VK_META:
                return null;
            default:
                resetContext();
                return NO_ACTION;
        }
    }

    public KeyStroke[] getBoundKeyStrokes() {
        int i = 0;
        KeyStroke[] keys = null;
        synchronized (this) {
            keys = new KeyStroke[bindings.size()];
            for (KeyStroke ks: bindings.keySet()) {
                keys[i++] = ks;
            }
        }
        return keys;
    }

    public Action[] getBoundActions() {
        int i = 0;
        Action[] actionsArray = null;
        synchronized (this) {
            actionsArray = new Action[bindings.size()];
            for (Iterator iter = bindings.values().iterator(); iter.hasNext(); ) {
                actionsArray[i++] = (Action) iter.next();
            }
        }
        return actionsArray;
    }

    public KeyStroke[] getKeyStrokesForAction(Action a) {
        Map<Action,List<KeyStroke>> localActions = actions;
        if (localActions == null) {
            localActions = buildReverseMapping ();
        }

        List<KeyStroke> strokes = localActions.get (a);
        if (strokes != null) {
            return strokes.toArray(new KeyStroke[strokes.size ()]);
        } else {
            return new KeyStroke[0];
        }
    }

    private Map<Action,List<KeyStroke>> buildReverseMapping () {
        Map<Action,List<KeyStroke>> localActions = actions = new HashMap<Action,List<KeyStroke>> ();

        synchronized (this) {
            for (Map.Entry<KeyStroke,Action> curEntry: bindings.entrySet()) {
                Action curAction = curEntry.getValue();
                KeyStroke curKey = curEntry.getKey();

                List<KeyStroke> keysForAction = localActions.get (curAction);
                if (keysForAction == null) {
                    keysForAction = Collections.synchronizedList (new ArrayList<KeyStroke> (1));
                    localActions.put (curAction, keysForAction);
                }
                keysForAction.add (curKey);
            }
        }

        return localActions;
    }

    public synchronized boolean isLocallyDefined(KeyStroke key) {
        return bindings.containsKey(key);
    }

    /** Updates action accelerator. */
    private void updateActionAccelerator(final Action a) {
        if(a == null) {
            return;
        }
        
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                KeyStroke[] keystrokes = getKeyStrokesForAction(a);
                Arrays.sort (keystrokes, NbKeymap.this);
                a.putValue(Action.ACCELERATOR_KEY, keystrokes.length > 0 ? keystrokes[0] : null);
            }
        });
    }
    
    public int compare(KeyStroke k1, KeyStroke k2) {
        //#47024 and 32733 - "Find" should not be shown as an accelerator,
        //nor should "Backspace" for Delete.  Solution:  The shorter text wins.
        return KeyEvent.getKeyText(k1.getKeyCode()).length() - 
            KeyEvent.getKeyText(k2.getKeyCode()).length();
    }
    
    
    public void addActionForKeyStroke(KeyStroke key, Action a) {
        // Update reverse binding for old action too (#30455):
        Action old;
        synchronized (this) {
            old = bindings.put(key, a);
            actions = null;
        }
        
        updateActionAccelerator(a);
        updateActionAccelerator(old);
        setChanged();
        notifyObservers();
    }

    void addActionForKeyStrokeMap(Map<KeyStroke,Action> map) {
        Set<Action> actionsSet = new HashSet<Action>();
        synchronized (this) {
            for (Iterator<KeyStroke> it = map.keySet ().iterator (); it.hasNext (); ) {
                KeyStroke key = it.next ();
                Action value = map.get(key);
                // Add both old and new action:
                actionsSet.add(value);
                actionsSet.add(bindings.put(key, value));
            }
            actions = null;
        }
        
        for(Action a: actionsSet) {
            updateActionAccelerator(a);
        }
        
        setChanged();
        notifyObservers();
    }

    public void removeKeyStrokeBinding(KeyStroke key) {
        Action a;
        synchronized (this) {
            a = bindings.remove(key);
            actions = null;
        }
        updateActionAccelerator(a);
        setChanged();
        notifyObservers();
    }

    public void removeBindings() {
        Set<Action> actionsSet;
        synchronized (this) {
            actionsSet = new HashSet<Action>(bindings.values());
            bindings.clear();
            actions = null;
        }
        
        for(Action a: actionsSet) {
            updateActionAccelerator(a);
        }
        
        setChanged();
        notifyObservers();
    }

    public Keymap getResolveParent() {
        return parent;
    }

    public void setResolveParent(Keymap parent) {
        this.parent = parent;
        setChanged();
        notifyObservers();
    }

    /** Returns string representation - can be looong.
    */
    public String toString() {
        return "Keymap[" + name + "]" + bindings; // NOI18N
    }
    
    public static class SubKeymap implements Keymap {
        Object hold;
        Keymap parent;
        Map<KeyStroke, Action> bindings;
        Action defaultAction;

        public SubKeymap(Object hold) {
            this.hold = hold;
            bindings = new HashMap<KeyStroke, Action>();
        }
        
        public String getName() {
            return "name";
        }
        
        public void setResolveParent(Keymap parent) {
            this.parent = parent;
        }

        public Keymap getResolveParent() {
            return parent;
        }

        public void addActionForKeyStroke(KeyStroke key, Action a) {
            bindings.put(key, a);
        }

        public KeyStroke[] getKeyStrokesForAction(Action a) {
            return new KeyStroke[0];
        }

        public void setDefaultAction(Action a) {
                defaultAction = a;
        }

        public Action getAction(KeyStroke key) {
            return bindings.get(key);
        }

        public boolean isLocallyDefined(KeyStroke key) {
            return bindings.containsKey(key);
        }

        public void removeKeyStrokeBinding(KeyStroke keys) {
            bindings.remove(keys);
        }

        public Action[] getBoundActions() {
            synchronized (this) {
                return bindings.values().toArray(new Action[0]);
            }
        }

        public KeyStroke[] getBoundKeyStrokes() {
            synchronized (this) {
                return bindings.keySet().toArray(new KeyStroke[0]);
            }
        }
  
        public Action getDefaultAction() {
            return defaultAction;
        }

        public void removeBindings() {
            bindings.clear();
        }
    
    }
    
    public class KeymapAction extends javax.swing.AbstractAction {
        private Keymap keymap;
        private KeyStroke stroke;
	
        public KeymapAction(Keymap keymap, KeyStroke stroke) {
            this.keymap = keymap;
            this.stroke = stroke;
        }
        
        public Keymap getSubMap() {
            return keymap;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
            if (stroke == null) { // NO_ACTION -> reset
                resetContext();
            } else {
                shiftContext(stroke);
            }	    
        }
    }
}
