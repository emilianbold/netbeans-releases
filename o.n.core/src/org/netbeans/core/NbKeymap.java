/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.event.*;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.openide.util.Mutex;

/** Implementation of standard key - action mappings.
*
* @author Dafe Simonek
*/
public final class NbKeymap extends Observable implements Keymap, Comparator {
    /** Name of this keymap */
    String name;
    /** Parent keymap */
    Keymap parent;
    /** Hashtable holding KeyStroke > Action mappings */
    Map bindings;
    /** Default action */
    Action defaultAction;
    /** hash table to map (Action -> ArrayList of KeyStrokes) */
    Map actions;

    /** Default constructor
    */
    public NbKeymap() {
        this("Default", null); // NOI18N
    }

    NbKeymap(final String name, final Keymap parent) {
        this.name = name;
        this.parent = parent;
        bindings = new HashMap();
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
        Action a = null;
        synchronized (this) {
            a = (Action) bindings.get(key);
        }
        if ((a == null) && (parent != null)) {
            a = parent.getAction(key);
        }
        return a;
    }

    public KeyStroke[] getBoundKeyStrokes() {
        int i = 0;
        KeyStroke[] keys = null;
        synchronized (this) {
            keys = new KeyStroke[bindings.size()];
            for (Iterator iter = bindings.keySet().iterator(); iter.hasNext(); ) {
                keys[i++] = (KeyStroke) iter.next();
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
        Map localActions = actions;
        if (localActions == null) {
            localActions = buildReverseMapping ();
        }

        List strokes = (List)localActions.get (a);
        if (strokes != null) {
            return (KeyStroke[])strokes.toArray(new KeyStroke[strokes.size ()]);
        } else {
            return new KeyStroke[0];
        }
    }

    private Map buildReverseMapping () {
        Map localActions = actions = new HashMap ();

        synchronized (this) {
            for (Iterator it = bindings.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry curEntry = (Map.Entry)it.next();
                Action curAction = (Action) curEntry.getValue();
                KeyStroke curKey = (KeyStroke) curEntry.getKey();

                List keysForAction = (List)localActions.get (curAction);
                if (keysForAction == null) {
                    keysForAction = Collections.synchronizedList (new ArrayList (1));
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
    
    public int compare(Object o1, Object o2) {
        //#47024 and 32733 - "Find" should not be shown as an accelerator,
        //nor should "Backspace" for Delete.  Solution:  The shorter text wins.
        KeyStroke k1 = (KeyStroke) o1;
        KeyStroke k2 = (KeyStroke) o2;
        return KeyEvent.getKeyText(k1.getKeyCode()).length() - 
            KeyEvent.getKeyText(k2.getKeyCode()).length();
    }
    
    
    public void addActionForKeyStroke(KeyStroke key, Action a) {
        // Update reverse binding for old action too (#30455):
        Action old;
        synchronized (this) {
            old = (Action)bindings.put(key, a);
            actions = null;
        }
        
        updateActionAccelerator(a);
        updateActionAccelerator(old);
        setChanged();
        notifyObservers();
    }

    void addActionForKeyStrokeMap(Map map) {
        Set actionsSet = new HashSet();
        synchronized (this) {
            for (Iterator it = map.keySet ().iterator (); it.hasNext (); ) {
                Object key = it.next ();
                Object value = map.get(key);
                // Add both old and new action:
                actionsSet.add(value);
                actionsSet.add(bindings.put(key, value));
            }
            actions = null;
        }
        
        for(Iterator it = actionsSet.iterator(); it.hasNext(); ) {
            updateActionAccelerator((Action)it.next());
        }
        
        setChanged();
        notifyObservers();
    }

    public void removeKeyStrokeBinding(KeyStroke key) {
        Action a;
        synchronized (this) {
            a = (Action)bindings.remove(key);
            actions = null;
        }
        updateActionAccelerator(a);
        setChanged();
        notifyObservers();
    }

    public void removeBindings() {
        Set actionsSet;
        synchronized (this) {
            actionsSet = new HashSet(bindings.values());
            bindings.clear();
            actions = null;
        }
        
        for(Iterator it = actionsSet.iterator(); it.hasNext(); ) {
            updateActionAccelerator((Action)it.next());
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

}
