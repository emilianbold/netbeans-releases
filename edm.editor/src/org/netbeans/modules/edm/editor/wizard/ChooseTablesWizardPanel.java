/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author karthikeyan s
 */
public class ChooseTablesWizardPanel implements WizardDescriptor.FinishablePanel {
    
    private Component component;
    
    public Component getComponent() {
        if (component == null) {
            component = new ChooseTablesVisualPanel(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public boolean isValid() {
        return canAdvance();
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
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }     
    
    public void readSettings(Object settings) {}
    public void storeSettings(Object settings) {
        if(settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
            wd.putProperty("model", 
                    ((ChooseTablesVisualPanel)getComponent()).getTables());
            wd.putProperty("mashupConnection", 
                    ((ChooseTablesVisualPanel)getComponent()).getMashupConnection());            
        }
    }

    public boolean isFinishPanel() {
        return canAdvance();
    }    
    
    private boolean canAdvance() {
        return ((ChooseTablesVisualPanel)getComponent()).canAdvance();        
    }
}