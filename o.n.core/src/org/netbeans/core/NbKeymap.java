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

/** Implementation of standard key - action mappings.
*
* @author Dafe Simonek
*/
final class NbKeymap implements Keymap {
  /** Name of this keymap */
  String name;
  /** Parent keymap */
  Keymap parent;
  /** Hashtable holding key - action mappings */
  Map bindings;
  /** Default action */
  Action defaultAction;
  /** Listeners set */
  HashSet listeners;
  /** hash table to map (Action -> KeyStroke[]) */
  Map actions;

  /** Default constructor
  */
  NbKeymap() {
    this("Default", null);
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
    fireChangeEvent(new ChangeEvent(this));
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
    Action[] actions = null;
    synchronized (this) {
      actions = new Action[bindings.size()];
      for (Iterator iter = bindings.values().iterator(); iter.hasNext(); ) {
        actions[i++] = (Action) iter.next();
      }
    }
    return actions;
  }

  public KeyStroke[] getKeyStrokesForAction(Action a) {
    Map actions;
    if (this.actions == null) {
      actions = this.actions = new HashMap (bindings.size ());
    } else {
      actions = this.actions;
    }

    

    KeyStroke[] strokes = (KeyStroke[])actions.get (a);
    if (strokes != null) {
      return strokes;
    }
    
    // searches for all entries which have value a
    // and add them to the resulting array
    Set result = new HashSet(5);
    Map.Entry curEntry = null;
    int count = 0;
    synchronized (this) {
      Set entries = bindings.entrySet();
      for (Iterator it = bindings.entrySet().iterator(); it.hasNext(); ) {
        curEntry = (Map.Entry)it.next();
        if (curEntry.getValue().equals(a)) {
          result.add(curEntry.getKey());
          count++;
        }

        actions.put (curEntry.getValue (), new KeyStroke[] { (KeyStroke)curEntry.getKey () });
        
      }
      strokes = (KeyStroke[])result.toArray(new KeyStroke[count]);
    }

    actions.put (a, strokes);
    
    return strokes;
  }

  public synchronized boolean isLocallyDefined(KeyStroke key) {
    return bindings.containsKey(key);
  }

  public void addActionForKeyStroke(KeyStroke key, Action a) {
    synchronized (this) {
      bindings.put(key, a);
      actions = null;
    }
    fireChangeEvent(new ChangeEvent(this));
  }

  public void removeKeyStrokeBinding(KeyStroke key) {
    synchronized (this) {
      bindings.remove(key);
      actions = null;
    }
    fireChangeEvent(new ChangeEvent(this));
  }

  public void removeBindings() {
    synchronized (this) {
      bindings.clear();
      actions = null;
    }
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

  void fireChangeEvent (final ChangeEvent che) {
    NbTopManager.change.firePropertyChange (NbTopManager.PROP_GLOBAL_KEYMAP, null, null);
  }

}

/*
* Log
*  6    src-jtulach1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    src-jtulach1.4         5/5/99   Jaroslav Tulach Speeded up a bit.
*  4    src-jtulach1.3         4/1/99   David Simonek   concurrent writing bug 
*       fixed (hopefully)
*  3    src-jtulach1.2         3/4/99   Jaroslav Tulach ChangeListener in 
*       TopManager
*  2    src-jtulach1.1         3/1/99   David Simonek   icons etc..
*  1    src-jtulach1.0         3/1/99   David Simonek   
* $
*/
