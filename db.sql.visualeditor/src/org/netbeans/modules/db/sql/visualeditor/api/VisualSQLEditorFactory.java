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

package org.netbeans.modules.db.sql.visualeditor.api;

import org.netbeans.api.db.explorer.DatabaseConnection;

/**
 * Factory class for creating VisualSQLEditor instances.
 *
 * @author Jim Davidson
 */
public final class VisualSQLEditorFactory {

    /**
     * Creates and returns a new VisualSQLEditor.
     *
     * @param dbconn the DatabaseConnection 
     * @param statement the initial SQL query to be loaded into the editor
     * @param metadata metadata cache maintained by the client, or null.  If null, the VisualSQLEditor will
     * fetch and manage its own metadata, using the DatabaseConnection
     * @return the new VisualSQLEditor instance
     *
     */
    public static VisualSQLEditor createVisualSQLEditor(DatabaseConnection dbconn, String statement, VisualSQLEditorMetaData metadata)
    {
	return new VisualSQLEditor(dbconn, statement, metadata);
    }

    // Private constructor
    private VisualSQLEditorFactory(){};
}

