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

import java.beans.PropertyChangeListener;
import org.netbeans.api.db.explorer.DatabaseConnection;

/**
 *
 * @author Andrei Badea
 */
public interface SQLExecution {

    public static final String PROP_EXECUTING = "executing"; // NOI18N
    public static final String PROP_DATABASE_CONNECTION = "databaseConnection"; // NOI18N

    public DatabaseConnection getDatabaseConnection();

    public void setDatabaseConnection(DatabaseConnection dbconn);

    /**
     * Executes all SQL statements.
     */
    public void execute();

    /**
     * Executes the selected SQL statements.
     */
    public void executeSelection();

    public boolean isExecuting();

    public boolean isSelection();

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);
}
