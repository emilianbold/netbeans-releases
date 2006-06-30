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
package org.netbeans.modules.diff.builtin.visualizer;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.Action;


/**
 * Re-sources action to given source.
 *
 * @author Petr Kuzel
 */
public class SourceTranslatorAction implements Action, PropertyChangeListener {

    final Action scrollAction;
    final Object source;
    final PropertyChangeSupport support;

    public SourceTranslatorAction(Action action, Object source) {
        scrollAction = action;
        this.source = source;
        support = new PropertyChangeSupport(action);
    }

    public Object getValue(String key) {
        return scrollAction.getValue(key);
    }

    public void putValue(String key, Object value) {
        scrollAction.putValue(key, value);
    }

    public void setEnabled(boolean b) {
        scrollAction.setEnabled(b);
    }

    public boolean isEnabled() {
        return scrollAction.isEnabled();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (support.hasListeners(null) == false) {
            scrollAction.addPropertyChangeListener(this);
        }
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
        if (support.hasListeners(null) == false) {
            scrollAction.removePropertyChangeListener(this);
        }
    }

    public void actionPerformed(ActionEvent e) {
        ActionEvent event = new ActionEvent(source, e.getID(), e.getActionCommand(), e.getWhen(), e.getModifiers());
        scrollAction.actionPerformed(event);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        support.firePropertyChange(evt);
    }
}