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

import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.event.ChangeListener;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;

public abstract class DBSchemaPanel implements WizardDescriptor.Panel {

    ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle"); //NOI18N

    protected ArrayList list;

    protected DBSchemaWizardData data;

    /** Default preferred width of the panel - should be the same for all panels within one wizard */
    private static final int DEFAULT_WIDTH = 600;
    /** Default preferred height of the panel - should be the same for all panels within one wizard */
    private static final int DEFAULT_HEIGHT = 390;

    public DBSchemaPanel() {
        list = new ArrayList();
    }

    /** @return preferred size of the wizard panel - it should be the same for all panels within one Wizard
    * so that the wizard dialog does not change its size when switching between panels */
    public java.awt.Dimension getPreferredSize () {
        return new java.awt.Dimension (DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public HelpCtx getHelp() {
        return new HelpCtx("dbschema_ctxhelp_wizard"); //NOI18N
    }

    public void readSettings (Object settings) {
    }

    public void storeSettings (Object settings) {
    }
    
    public synchronized void addChangeListener (ChangeListener listener) {
        list.add(listener);
    }

    public synchronized void removeChangeListener (ChangeListener listener) {
        list.remove(listener);
    }
}
