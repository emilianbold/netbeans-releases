/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tools.generator;

import java.util.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;

import org.openide.*;

/**
 * Base class of Generator Wizard panels. <code>updateModel</code>
 * and <code>initView</code> methods need to be implemented. They are called as user goes 
 * over wizard steps and it must (re)store current state.
 *
 * @author  Petr Kuzel
 * @version 
 */
public abstract class SAXGeneratorAbstractPanel extends JPanel implements WizardDescriptor.Panel, Customizer {

    /** Serial Version UID */
    private static final long serialVersionUID =5089896677680825691L;
    
    private Vector listeners = new Vector(); 
    private final ChangeEvent EVENT = new ChangeEvent(this);
    private boolean valid = true;

    /**
     * After a setObject() call contains current model driving wizard.
     */
    protected SAXGeneratorModel model;
    
    /** Creates new SAXGeneratorAbstractPanel */
    public SAXGeneratorAbstractPanel() {
    }

    public java.awt.Component getComponent() {
        return this;
    }
    
    public void readSettings(java.lang.Object p1) {
        updateView();
    }
    
    public org.openide.util.HelpCtx getHelp() {
        return null;
    }
    
    public void addChangeListener(javax.swing.event.ChangeListener l) {
        listeners.add(l);
    }
    
    /**
     * User just leaved the panel, update model
     */
    protected abstract void updateModel();
    
    /**
     * User just entered the panel, init view by model values
     */
    protected abstract void initView();
    
    /**
     * User just reentered the panel.
     */
    protected abstract void updateView();
    
    public void storeSettings(java.lang.Object p1) {
        updateModel();
    }
    
    public boolean isValid() {
        return valid;
    }

    protected final void setValid(boolean valid) {
        
        if (this.valid == valid) return;
        
        this.valid = valid;
        
        synchronized (listeners) {
            Iterator it = listeners.iterator();
            while (it.hasNext()) {
                ChangeListener next = (ChangeListener) it.next();
                next.stateChanged(EVENT);
            }
        }
    }
    
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        listeners.remove(l);
    }
    
    public void setObject(java.lang.Object peer) {
        if ( not(peer instanceof SAXGeneratorModel) ) {
            throw new IllegalArgumentException("SAXGeneratorModel class expected.");  // NOI18N
        }        
        
        model = (SAXGeneratorModel) peer;
        initView();
    }    
        
    public void addPropertyChangeListener(java.beans.PropertyChangeListener p1) {
    }
    
    public void removePropertyChangeListener(java.beans.PropertyChangeListener p1) {
    }

    protected final boolean not (boolean expr) {
        return ! expr;
    }
    
}
