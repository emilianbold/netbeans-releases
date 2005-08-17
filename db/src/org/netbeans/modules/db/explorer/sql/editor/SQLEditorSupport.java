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

package org.netbeans.modules.db.explorer.sql.editor;

import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.spi.sql.editor.SQLEditorProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Andrei Badea
 */
public class SQLEditorSupport {

    public static void openSQLEditor(DatabaseConnection dbconn, String sql, boolean execute) {
        SQLEditorProvider provider = (SQLEditorProvider)Lookup.getDefault().lookup(SQLEditorProvider.class);
        if (provider != null) {
            provider.openSQLEditor(dbconn, sql, execute);
        }
    }
}
