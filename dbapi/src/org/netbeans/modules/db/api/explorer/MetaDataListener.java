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

package org.netbeans.modules.db.api.explorer;

import org.netbeans.api.db.explorer.DatabaseConnection;

/**
 * Interface allowing to listen on changes of the database metadata caused
 * by the Database Explorer, such as adding or dropping a table or a column.
 *
 * @author Andrei Badea
 */
public interface MetaDataListener {

    /**
     * Invoked when the list of tables in the database represented by the
     * <code>dbconn</code> parameter has changed.
     *
     * @param dbconn the database connection whose tables have changed
     */
    void tablesChanged(DatabaseConnection dbconn);

    /**
     * Invoked when the structure of a table in the database represented by the
     * <code>dbconn</code> parameter has changed.
     *
     * @param dbconn the database connection whose table have changed
     * @param tableName the name of the table which changed
     */
    void tableChanged(DatabaseConnection dbconn, String tableName);
}
