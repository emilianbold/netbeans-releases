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
