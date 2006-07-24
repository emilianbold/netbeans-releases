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
package org.netbeans.api.visual.model;

import java.util.ArrayList;

/**
 * @author David Kaspar
 */
public class StateModel {

    private final ArrayList<Listener> listeners = new ArrayList<Listener> ();
    private int state;
    private int maxStates;

    public StateModel () {
        this (2);
    }

    public StateModel (int maxStates) {
        assert maxStates > 0;
        this.maxStates = maxStates;
        state = 0;
    }

    private void fireChanged () {
        Listener[] ls;
        synchronized (listeners) {
            ls = this.listeners.toArray (new Listener[this.listeners.size ()]);
        }
        for (Listener listener : ls)
            listener.stateChanged ();
    }

    public boolean getBooleanState () {
        return state > 0;
    }

    public void setBooleanState (boolean state) {
        setState (state ? 1 : 0);
    }

    public void toggleBooleanState () {
        setState (state > 0 ? 0 : 1);
    }

    public int getState () {
        return state;
    }

    public void decrease () {
        if (-- state < 0)
            state = maxStates - 1;
        fireChanged ();
    }

    public void increase () {
        if (++ state >= maxStates)
            state = 0;
        fireChanged ();
    }

    public void setState (int state) {
        this.state = state;
        fireChanged ();
    }

    public int getMaxStates () {
        return maxStates;
    }

    public void addListener (Listener listener) {
        synchronized (listeners) {
            listeners.add (listener);
        }
    }

    public void removeListener (Listener listener) {
        synchronized (listeners) {
            listeners.remove (listener);
        }
    }

    public interface Listener {

        void stateChanged ();

    }

}
