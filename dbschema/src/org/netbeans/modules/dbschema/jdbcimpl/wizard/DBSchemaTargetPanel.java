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

package org.netbeans.modules.dbschema.jdbcimpl.wizard;

import java.awt.Component;
import java.beans.*;

import javax.swing.event.ChangeListener;

import org.openide.util.HelpCtx;

public class DBSchemaTargetPanel extends DBSchemaPanel {

    private org.openide.WizardDescriptor.Panel panel;
    
    public DBSchemaTargetPanel() {
    }

    public void setPanel(org.openide.WizardDescriptor.Panel panel) {
        this.panel = panel;
    }
    
    public DBSchemaTargetPanel getPanel() {
        return this;
    }

    public Component getComponent() {
        return panel.getComponent();
    }

    public boolean isValid() {
        boolean ret = panel.isValid();
        
        if (ret) {
            org.openide.loaders.TemplateWizard settings = new org.openide.loaders.TemplateWizard();
            String name = settings.getTargetName();
            
            if (name != null)
                if ((name.indexOf("\\") != -1) || (name.indexOf("/") != -1))
                    return false;
        }
        
        return ret;
    }

    public void readSettings(Object settings) {
        panel.readSettings(settings);
    }

    public void storeSettings(Object settings) {
        panel.storeSettings(settings);
    }

    public HelpCtx getHelp() {
        return new HelpCtx("dbschema_ctxhelp_wizard"); //NOI18N
    }

    public synchronized void addChangeListener(ChangeListener listener) {
        panel.addChangeListener(listener);
    }

    public synchronized void removeChangeListener(ChangeListener listener) {
        panel.removeChangeListener(listener);
    }
}
