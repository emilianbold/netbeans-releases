/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.awt.Component;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  tom
 */
public class TemplatesPanel implements WizardDescriptor.Panel {
    
    private ArrayList listeners;
    private TemplatesPanelGUI panel;
    
    /** Creates a new instance of TemplatesPanel */
    public TemplatesPanel() {
    }
    
    public void readSettings (Object settings) {
        TemplateWizard wd = (TemplateWizard) settings;
        wd.putProperty ("WizardPanel_contentSelectedIndex", new Integer (0)); // NOI18N
        wd.putProperty ("WizardPanel_contentData", new String[] { // NOI18N
                NbBundle.getBundle (TemplatesPanel.class).getString ("LBL_TemplatesPanel_Name"), // NOI18N
                NbBundle.getBundle (TemplatesPanel.class).getString ("LBL_TemplatesPanel_Dots")}); // NOI18N
        ((TemplatesPanelGUI)this.getComponent()).read (wd);
    }
    
    public void storeSettings (Object settings) {
        ((TemplatesPanelGUI)this.getComponent()).store ((TemplateWizard)settings);
    }
    
    public synchronized void addChangeListener(javax.swing.event.ChangeListener l) {
        if (this.listeners == null) {
            this.listeners = new ArrayList ();
        }
        this.listeners.add (l);
    }
    
    public synchronized void removeChangeListener(javax.swing.event.ChangeListener l) {
        if (this.listeners == null) {
            return;
        }
        this.listeners.remove (l);
    }
    
    public boolean isValid() {
        return ((TemplatesPanelGUI)this.getComponent()).valid ();
    }
    
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public synchronized Component getComponent() {
        if (this.panel == null) {
            this.panel = new TemplatesPanelGUI (this);
            this.panel.setName (NbBundle.getBundle (TemplatesPanel.class).getString ("LBL_TemplatesPanel_Name")); // NOI18N
        }
        return this.panel;
    }
    
    void fireChange () {
        Iterator  it = null;
        synchronized (this) {
            if (this.listeners == null) {
                return;
            }
            it = ((ArrayList)this.listeners.clone()).iterator();
        }
        ChangeEvent event = new ChangeEvent (this);
        while (it.hasNext ()) {
            ((ChangeListener)it.next()).stateChanged(event);
        }
    }
    
}
