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

package org.netbeans.modules.visualweb.project.jsf.ui;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Po-Ting Wu
 */
final class PagebeanPackagePanel implements WizardDescriptor.Panel, ChangeListener {

    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private PagebeanPackagePanelGUI gui;

    private Project project;
    private WizardDescriptor wizard;

    PagebeanPackagePanel(Project project) {
        this.project = project;
        this.gui = null;
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new PagebeanPackagePanelGUI(project);
            gui.addChangeListener(this);
        }
        return gui;
    }

    public HelpCtx getHelp() {
        return null;
    }

    public boolean isValid() {
        if (gui == null) {
            return false;
        }

        // Check to make sure that the package name is valid
        String packageName = gui.getPackageName();
        if (!JsfProjectUtils.isValidJavaPackageName(packageName)) {
            wizard.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(PagebeanPackagePanel.class, "MSG_InvalidPackageName", packageName)); // NOI18N
            return false;
        }

        return true;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        List templist;
        synchronized (this) {
            templist = new ArrayList (listeners);
        }
        Iterator it = templist.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }

    public void readSettings(Object settings) {
        wizard = (WizardDescriptor) settings;
                
        if (gui == null) {
            getComponent();
        }
        
        gui.initValues(project);
    }
    
    public void storeSettings(Object settings) { 
        if (WizardDescriptor.PREVIOUS_OPTION.equals(((WizardDescriptor) settings).getValue())) {
            return;
        }

        if (isValid()) {
            ((WizardDescriptor) settings).putProperty(JsfProjectConstants.PROP_JSF_PAGEBEAN_PACKAGE, gui.getPackageName());
        }
    }

    public void stateChanged(ChangeEvent e) {        
        if (wizard != null && isValid()) {
            wizard.putProperty(JsfProjectConstants.PROP_JSF_PAGEBEAN_PACKAGE, gui.getPackageName());
        }

        fireChange();
    }
}
