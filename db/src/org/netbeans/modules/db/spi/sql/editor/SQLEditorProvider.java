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

package org.netbeans.modules.db.spi.sql.editor;

import org.netbeans.api.db.explorer.DatabaseConnection;
import org.openide.loaders.DataObject;

/**
 * This interface provides an SQL editor. It is used every time the Database
 * Explorer needs to open an SQL editor, such as from the Execute Command or
 * View Data actions. The implementation should be placed in the default lookup.
 *
 * @author Andrei Badea
 */
public interface SQLEditorProvider {

    /**
     * Opens a new SQL editor for the specified connection and containing the
     * specified SQL statments and possibly executes them.
     *
     * @param dbconn the databaseconnection set as active in the SQL editor. The
     *        statements are also executed against this connection.
     * @param sql the SQL statements to be put in the editor
     * @param execute whether to execute the SQL statements.
     */
    public void openSQLEditor(DatabaseConnection dbconn, String sql, boolean execute);
}
