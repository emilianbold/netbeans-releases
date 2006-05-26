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
package org.netbeans.bluej.export;

import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

public class ExportWizardPanel1 implements WizardDescriptor.Panel {
    
    private String path;
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;

    private FileObject dir;
    private boolean valid = false;
    private WizardDescriptor settings;
    
    ExportWizardPanel1(FileObject fo) {
        dir = fo;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new ExportPanel(dir, this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    public boolean isValid() {
        return valid;
    }
    
    private final Set listeners = new HashSet(1);
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
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object sets) {
        this.settings = (WizardDescriptor)sets;
    }
    public void storeSettings(Object set) {
        WizardDescriptor wiz = (WizardDescriptor)set;
        wiz.putProperty("NewProjectLocation", new File(path.trim()));
    }

    void updateValue(String value) {
        path = value;
        if (path == null || path.trim().length() == 0) {
            settings.putProperty("WizardPanel_errorMessage", "Please specify a folder to export to.");
            valid = false;
        } else {
            File fil = new File(path.trim());
            if (fil.exists() && (fil.isFile() || (fil.isDirectory() && fil.listFiles().length > 0))) {
                settings.putProperty("WizardPanel_errorMessage", "Please select empty or non-existing directory.");
                valid = false;
            } else {
                settings.putProperty("WizardPanel_errorMessage", null);
                valid = true;
            }
        }
        
        fireChangeEvent();
    }
    
}

