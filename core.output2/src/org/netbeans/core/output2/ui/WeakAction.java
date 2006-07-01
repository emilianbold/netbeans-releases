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
/*
 * WeakAction.java
 *
 * Created on May 14, 2004, 11:01 PM
 */

package org.netbeans.core.output2.ui;

import org.openide.util.WeakListeners;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Action which delegates to a weakly referenced original.
 *
 * @author  Tim Boudreau
 */
class WeakAction implements Action, PropertyChangeListener {
    private Reference original;
    private Icon icon;
    private List listeners = new ArrayList();
    private String name = null;

    /** Creates a new instance of WeakAction */
    public WeakAction(Action original) {
        wasEnabled = original.isEnabled();
        icon = (Icon) original.getValue (SMALL_ICON);
        name = (String) original.getValue (NAME);
        this.original = new WeakReference (original);
        original.addPropertyChangeListener(WeakListeners.propertyChange(this, original));
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
        Action orig = getOriginal();
        if (orig != null) {
            orig.actionPerformed (actionEvent);
        }
    }
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener pce) {
        listeners.add (pce);
    }
    
    public Object getValue(String str) {
        if (SMALL_ICON.equals(str)) {
            return icon;
        } else {
            Action orig = getOriginal();
            if (orig != null) {
                return orig.getValue(str);
            } else if (NAME.equals(str)) {
                //Avoid NPE if action is disposed but shown in popup
                return name;
            }
        }
        return null;
    }
    
    private boolean wasEnabled = true;
    public boolean isEnabled() {
        Action orig = getOriginal();
        if (orig != null) {
            wasEnabled = orig.isEnabled();
            return wasEnabled;
        }
        return false;
    }
    
    public void putValue(String str, Object obj) {
        if (SMALL_ICON.equals(str)) {
            icon = (Icon) obj;
        } else {
            Action orig = getOriginal();
            if (orig != null) {
                orig.putValue(str, obj);
            }
        }
    }
    
    public synchronized void removePropertyChangeListener(PropertyChangeListener pce) {
        listeners.remove (pce);
    }
    
    public void setEnabled(boolean val) {
        Action orig = getOriginal();
        if (orig != null) {
            orig.setEnabled(val);
        }
    }
    
    private boolean hadOriginal = true;
    private Action getOriginal() {
        Action result = (Action) original.get();
        if (result == null && hadOriginal && wasEnabled) {
            hadOriginal = false;
            firePropertyChange ("enabled", Boolean.TRUE, Boolean.FALSE); //NOI18N
        }
        return result;
    }
    
    private synchronized void firePropertyChange(String nm, Object old, Object nue) {
        PropertyChangeEvent pce = new PropertyChangeEvent (this, nm, old, nue);
        for (Iterator i=listeners.iterator(); i.hasNext();) {
            PropertyChangeListener pcl = (PropertyChangeListener) i.next();
            pcl.propertyChange(pce);
        }
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent pce) {
        firePropertyChange (pce.getPropertyName(), pce.getOldValue(), 
            pce.getNewValue());
        if ("enabled".equals(pce.getPropertyName())) { //NOI18n
            wasEnabled = Boolean.TRUE.equals(pce.getNewValue());
       }
    }
    
}
