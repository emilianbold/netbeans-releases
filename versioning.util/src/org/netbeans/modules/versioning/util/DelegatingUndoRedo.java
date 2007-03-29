/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.util;

import org.openide.awt.UndoRedo;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.*;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
import java.util.*;

/**
 * Delegates UndoRedo to the currently active component's UndoRedo.

 * @author Maros Sandor
 */
public class DelegatingUndoRedo implements UndoRedo, ChangeListener {

    private List<ChangeListener> listeners = new ArrayList<ChangeListener>(2);
        
    private UndoRedo delegate = UndoRedo.NONE;

    public void setDiffView(JComponent componentDelegate) {
        if (componentDelegate == null) {
            setDelegate(UndoRedo.NONE);
        } else {
            UndoRedo delegate = (UndoRedo) componentDelegate.getClientProperty(UndoRedo.class);
            if (delegate == null) delegate = UndoRedo.NONE; 
            setDelegate(delegate);
        }
    }

    private void setDelegate(UndoRedo newDelegate) {
        if (newDelegate == delegate) return;
        delegate.removeChangeListener(this);
        delegate = newDelegate;
        stateChanged(new ChangeEvent(this));
        delegate.addChangeListener(this);
    }
        
    public void stateChanged(ChangeEvent e) {
        List<ChangeListener> currentListeners;
        synchronized(this) {
            currentListeners = listeners;
        }
        for (ChangeListener listener : currentListeners) {
            listener.stateChanged(e);
        }
    }

    public boolean canUndo() {
        return delegate.canUndo();
    }

    public boolean canRedo() {
        return delegate.canRedo();
    }

    public void undo() throws CannotUndoException {
        delegate.undo();
    }

    public void redo() throws CannotRedoException {
        delegate.redo();
    }
        
    public synchronized void addChangeListener(ChangeListener l) {
        List<ChangeListener> newListeners = new ArrayList<ChangeListener>(listeners);
        newListeners.add(l);
        listeners = newListeners;
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        List<ChangeListener> newListeners = new ArrayList<ChangeListener>(listeners);
        newListeners.remove(l);
        listeners = newListeners;
    }

    public String getUndoPresentationName() {
        return delegate.getUndoPresentationName();
    }

    public String getRedoPresentationName() {
        return delegate.getRedoPresentationName();
    }
}
