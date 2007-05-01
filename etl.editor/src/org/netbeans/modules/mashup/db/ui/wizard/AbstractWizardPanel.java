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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mashup.db.ui.wizard;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;

/**
 * Wizard panel to select tables and columns to be included in an Flatfile DB instance.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public abstract class AbstractWizardPanel implements WizardDescriptor.Panel {

    /** Set of ChangeListeners interested in changes to this panel */
    protected Set listeners;

    /** Creates a new instance of AbstractWizardPanel */
    public AbstractWizardPanel() {
        listeners = new HashSet(1);
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#addChangeListener
     */
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    /**
     * Broadcasts change events to all interested listeners.
     */
    public final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }

        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    /**
     * Gets static step label associated with this panel.
     * 
     * @return String representing step label
     */
    public abstract String getStepLabel();

    /**
     * Gets title of this panel.
     * 
     * @return String representing step label
     */
    public abstract String getTitle();

    /**
     * @see org.openide.WizardDescriptor.Panel#isValid
     */
    public boolean isValid() {
        return true;
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#readSettings
     */
    public void readSettings(Object settings) {
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#removeChangeListener
     */
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#storeSettings
     */
    public void storeSettings(Object settings) {
    }
}

