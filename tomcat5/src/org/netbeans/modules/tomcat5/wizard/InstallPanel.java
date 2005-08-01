/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Add Tomcat wizard descriptor panel implementation.
 */
class InstallPanel implements WizardDescriptor.Panel, ChangeListener {
    
    private final List listeners = new ArrayList();
    private WizardDescriptor wizard;
    private InstallPanelVisual component;
    private final int tomcatVersion;

    public InstallPanel(int aTomcatVersion) {
        tomcatVersion = aTomcatVersion;
    }

    public void addChangeListener(javax.swing.event.ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public void storeSettings(Object settings) {
    }

    public void readSettings(Object settings) {
        wizard = (WizardDescriptor)settings;
    }

    public boolean isValid() {
        boolean result = getVisual().isValid();
        wizard.putProperty(AddInstanceIterator.PROP_ERROR_MESSAGE, getVisual().getErrorMessage());
        return result;
    }

    public java.awt.Component getComponent() {
        if (component == null) {
            component = new InstallPanelVisual(tomcatVersion);
            component.addChangeListener(this);
        }

        return component;
    }

    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx("tomcat_addinstall"); // NOI18N
    }

    public void stateChanged(javax.swing.event.ChangeEvent event) {
        fireChange(event);
    }

    public InstallPanelVisual getVisual() {
        return (InstallPanelVisual)getComponent();
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
}
