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

package org.netbeans.modules.j2ee.deployment.impl.ui.wizard;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author Andrei Badea
 */
class ServerChooserPanel implements WizardDescriptor.Panel, ChangeListener {
    private final List listeners = new ArrayList();
    private ServerChooserVisual component;

    public ServerChooserPanel() {
    }

    public Component getComponent() {
        if (component == null) { 
            component = new ServerChooserVisual();
            component.addChangeListener(this);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public void readSettings(Object settings) {
        getVisual().read((AddServerInstanceWizard)settings);
    }

    public void storeSettings(Object settings) {
        getVisual().store((AddServerInstanceWizard)settings);
    }

    public boolean isValid() {
        return getVisual().isValid();
    }

    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public void stateChanged(ChangeEvent event) {
        fireChange(event);
    }

    private void fireChange(ChangeEvent event) {
        ArrayList tempList;

        synchronized (listeners) {
            tempList = new ArrayList(listeners);
        }

        Iterator iter = tempList.iterator();
        while (iter.hasNext())
            ((ChangeListener)iter.next()).stateChanged(event);
    }

    private ServerChooserVisual getVisual() {
        return (ServerChooserVisual)getComponent();
    }
}
