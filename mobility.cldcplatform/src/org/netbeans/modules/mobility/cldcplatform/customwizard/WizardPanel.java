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

package org.netbeans.modules.mobility.cldcplatform.customwizard;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author David Kaspar
 */
public class WizardPanel implements WizardDescriptor.FinishablePanel {
    
    final private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
    final private ComponentDescriptor componentDescriptor;
    private WizardDescriptor wizardDescriptor;
    
    public WizardPanel(ComponentDescriptor componentDescriptor) {
        this.componentDescriptor = componentDescriptor;
        componentDescriptor.setWizardPanel(this);
    }
    
    public boolean isFinishPanel() {
        return componentDescriptor.isFinishPanel();
    }
    
    public Component getComponent() {
        return componentDescriptor.getComponent();
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(getComponent().getClass());
    }
    
    public void readSettings(final Object object) {
        wizardDescriptor = (WizardDescriptor) object;
        componentDescriptor.readSettings(wizardDescriptor);
    }
    
    public void storeSettings(final Object object) {
        wizardDescriptor = (WizardDescriptor) object;
        componentDescriptor.storeSettings(wizardDescriptor);
    }
    
    public boolean isValid() {
        return componentDescriptor.isPanelValid();
    }
    
    public void addChangeListener(final ChangeListener changeListener) {
        listeners.add(changeListener);
    }
    
    public void removeChangeListener(final ChangeListener changeListener) {
        listeners.remove(changeListener);
    }
    
    public void fireChanged() {
        final ChangeEvent e = new ChangeEvent(this);
        for ( final ChangeListener l : listeners ) {
            l.stateChanged(e);
        }
    }
    
    public void setErrorMessage(final Class clazz, final String message) {
        if (wizardDescriptor != null)
            wizardDescriptor.putProperty("WizardPanel_errorMessage", message != null ? NbBundle.getMessage(clazz, message) : null); // NOI18N
    }
    
    public Object getProperty(final String property) {
        return wizardDescriptor.getProperty(property);
    }
    
    public void putProperty(final String property, final Object value) {
        wizardDescriptor.putProperty(property, value);
    }
    
    public interface ComponentDescriptor {
        
        public void setWizardPanel(WizardPanel wizardPanel);
        
        public void readSettings(WizardDescriptor wizardDescriptor);
        
        public void storeSettings(WizardDescriptor wizardDescriptor);
        
        public JComponent getComponent();
        
        public boolean isPanelValid();
        
        public boolean isFinishPanel();
        
    }
    
}
