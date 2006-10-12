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

package org.netbeans.modules.db.api.sql.execute;

import java.sql.SQLException;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.openide.nodes.Node;

/**
 * Cookie for executing SQL files.
 *
 * <p>This interface allows a client to execute the SQL statement(s)
 * contained in the implementing object (currently the
 * DataObject for SQL files). Therefore calling
 * the {@link #execute} method will execute the statement(s) contained
 * in the respective file and display the results.</p>
 *
 * @author Andrei Badea
 */
public interface SQLExecuteCookie extends Node.Cookie {

    // XXX this should not be a cookie, just a plain interface;
    // will be fixed when lookups are added to DataObjects

    /**
     * Call this set the current database connection for this cookie.
     * The database connection will be used by the {@link #execute} method.
     */
    public void setDatabaseConnection(DatabaseConnection dbconn);

    /**
     * Call this to execute the statements in the object implementing the
     * cookie and display them in the result window.
     */
    public void execute();
}
