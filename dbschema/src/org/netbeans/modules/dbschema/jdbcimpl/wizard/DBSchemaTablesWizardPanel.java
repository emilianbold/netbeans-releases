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

public final class DBSchemaTablesWizardPanel extends DBSchemaPanel {
    
  /** aggregation, instance of UI component of this wizard panel */
    private DBSchemaTablesPanel panelUI;

    public DBSchemaTablesWizardPanel(DBSchemaWizardData data) {
        this.data = data;
    }

    /** @return AWT component which represents UI of this wizard panel */
    public Component getComponent() {
        return getPanelUI();
    }

    /** @return UI component of this wizard panel. Creates new one if
     * accessed for the first time
     */    
    private DBSchemaTablesPanel getPanelUI () {
        if (panelUI == null) {
            panelUI = new DBSchemaTablesPanel(data, list);
        }
        return panelUI;
    }
    
    public boolean isValid () {
        return getPanelUI().isValid();
    }

}
