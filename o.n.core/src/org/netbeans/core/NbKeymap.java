/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.impl;

import java.util.*;
import javax.swing.event.*;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

import com.netbeans.ide.awt.MutableKeymap;

/** Implementation of standard key - action mappings.
*
* @author Dafe Simonek
*/
final class NbKeymap implements MutableKeymap {

  /** Name of this keymap */
  String name;
  /** Parent keymap */
  Keymap parent;
  /** Hashtable holding key - action mappings */
  Hashtable bindings;
  /** Default action */
  Action defaultAction;
  /** Listeners set */
  HashSet listeners;

  /** Default constructor
  */
  NbKeymap() {
    this("Default", null);
  }

  NbKeymap(final String name, final Keymap parent) {
    this.name = name;
    this.parent = parent;
    bindings = new Hashtable();
  }

  public Action getDefaultAction() {
    if (defaultAction != null) {
        return defaultAction;
    }
    return (parent != null) ? parent.getDefaultAction() : null;
  }

  public void setDefaultAction(Action a) {
    defaultAction = a;
    fireChangeEvent(new ChangeEvent(this));
  }

  public String getName() {
    return name;
  }

  public Action getAction(KeyStroke key) {
    Action a = (Action) bindings.get(key);
    if ((a == null) && (parent != null)) {
        a = parent.getAction(key);
    }
    return a;
  }

  public KeyStroke[] getBoundKeyStrokes() {
    KeyStroke[] keys = new KeyStroke[bindings.size()];
    int i = 0;
    for (Enumeration e = bindings.keys() ; e.hasMoreElements() ;) {
      keys[i++] = (KeyStroke) e.nextElement();
    }
    return keys;
  }

  public Action[] getBoundActions() {
    Action[] actions = new Action[bindings.size()];
    int i = 0;
    for (Enumeration e = bindings.elements() ; e.hasMoreElements() ;) {
      actions[i++] = (Action) e.nextElement();
    }
    return actions;
  }

  public KeyStroke[] getKeyStrokesForAction(Action a) {
    // searches for all entries which have value a
    // and add them to the resulting array
    Set result = new HashSet(5);
    Set entries = bindings.entrySet();
    Map.Entry curEntry = null;
    int count = 0;
    for (Iterator it = entries.iterator(); it.hasNext(); ) {
      curEntry = (Map.Entry)it.next();
      if (curEntry.getValue().equals(a)) {
        result.add(curEntry.getKey());
        count++;
      }
    }
    return (KeyStroke[])result.toArray(new KeyStroke[count]);
  }

  public boolean isLocallyDefined(KeyStroke key) {
    return bindings.containsKey(key);
  }

  public void addActionForKeyStroke(KeyStroke key, Action a) {
    bindings.put(key, a);
    fireChangeEvent(new ChangeEvent(this));
  }

  public void removeKeyStrokeBinding(KeyStroke key) {
    bindings.remove(key);
    fireChangeEvent(new ChangeEvent(this));
  }

  public void removeBindings() {
    bindings.clear();
    fireChangeEvent(new ChangeEvent(this));
  }

  public Keymap getResolveParent() {
    return parent;
  }

  public void setResolveParent(Keymap parent) {
    this.parent = parent;
    fireChangeEvent(new ChangeEvent(this));
  }

  /** Returns string representation - can be looong.
  */
  public String toString() {
    return "Keymap[" + name + "]" + bindings;
  }

  /** Adds listener to change of the map.
  * It is informed when some changes are made to the map.
  */
  public synchronized void addChangeListener (final ChangeListener l) {
    // lazy init
    if (listeners == null) listeners = new HashSet(5);
    listeners.add(l);
  }

  /** Removes the listener.
  */
  public synchronized void removeChangeListener (final ChangeListener l) {
    if (listeners == null) return;
    listeners.remove(l);
  }

  /** Fires change event to all listeners.
  * Clears loaderArray before firing a change.
  * @param che change event
  */
  void fireChangeEvent (final ChangeEvent che) {
    if (listeners == null) return;

    HashSet cloned;
    // clone listener list
    synchronized (this) {
      cloned = (HashSet)listeners.clone();
    }
    // fire on cloned list to prevent from modifications when firing
    for (Iterator iter = cloned.iterator(); iter.hasNext(); ) {
      ((ChangeListener)iter.next()).stateChanged(che);
    }
  }

}

/*
* Log
*  2    src-jtulach1.1         3/1/99   David Simonek   icons etc..
*  1    src-jtulach1.0         3/1/99   David Simonek   
* $
*/
