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

package org.netbeans.modules.tomcat5.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.tomcat5.TomcatManager.TomcatVersion;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Add Tomcat wizard descriptor panel implementation.
 */
class InstallPanel implements WizardDescriptor.Panel, ChangeListener {

    private final List listeners = new ArrayList();
    private WizardDescriptor wizard;
    private InstallPanelVisual component;
    private final TomcatVersion tomcatVersion;

    public InstallPanel(TomcatVersion tomcatVersion) {
        this.tomcatVersion = tomcatVersion;
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
