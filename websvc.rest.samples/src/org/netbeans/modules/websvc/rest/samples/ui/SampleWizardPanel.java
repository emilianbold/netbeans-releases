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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.rest.samples.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 * @author Peter Liu
 */
public class SampleWizardPanel implements WizardDescriptor.Panel, WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {
    private static final long serialVersionUID = 1L;
    private WizardDescriptor myWizardDescriptor;
    private SampleWizardPanelVisual myComponent;
    
    /** Creates a new instance of templateWizardPanel */
    public SampleWizardPanel() {
    }

    
    public SampleWizardPanelVisual getComponent() {
        if (myComponent == null) {
            myComponent = new SampleWizardPanelVisual(this);
        }
        return myComponent;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(this.getClass());
    }
    
    public boolean isValid() {
        return getComponent().valid( myWizardDescriptor );
    }
    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    public void readSettings(Object settings) {
        myWizardDescriptor = (WizardDescriptor)settings;        
        getComponent().read (myWizardDescriptor);
        
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor)settings;
        getComponent().store(d);
    }

    public boolean isFinishPanel() {
        return true;
    }
    
    public void validate () throws WizardValidationException {
        getComponent().validate (myWizardDescriptor);
    }
}
