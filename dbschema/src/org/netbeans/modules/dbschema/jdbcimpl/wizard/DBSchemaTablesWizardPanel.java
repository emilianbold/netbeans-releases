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
