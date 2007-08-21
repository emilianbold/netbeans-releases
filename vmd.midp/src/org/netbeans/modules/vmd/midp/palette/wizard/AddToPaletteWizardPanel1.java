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
 *
 */
package org.netbeans.modules.vmd.midp.palette.wizard;

import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author David Kaspar
 */
public class AddToPaletteWizardPanel1 implements WizardDescriptor.Panel {

    private AddToPaletteVisualPanel1 component;

    public Component getComponent() {
        if (component == null) {
            component = new AddToPaletteVisualPanel1(this);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx (AddToPaletteWizardPanel1.class);
    }

    public boolean isValid() {
        return component.getActiveProject () != null;
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

    public void readSettings(Object settings) {
        getComponent();
        component.reload ((Project) ((WizardDescriptor) settings).getProperty (AddToPaletteWizardAction.PROPERTY_PROJECT));

    }
    public void storeSettings(Object settings) {
        getComponent();
        Project project = component.getActiveProject ();
        ((WizardDescriptor) settings).putProperty (AddToPaletteWizardAction.PROPERTY_PROJECT, project);
    }

}
