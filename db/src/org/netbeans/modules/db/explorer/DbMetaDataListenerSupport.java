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

package org.netbeans.modules.db.explorer;

import java.util.Iterator;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.openide.util.Lookup;

/**
 *
 * @author Andrei Badea
 */
public class DbMetaDataListenerSupport {

    private static final Lookup.Result listeners = Lookup.getDefault().lookup(new Lookup.Template(DbMetaDataListener.class));

    private DbMetaDataListenerSupport() {
    }

    public static void fireTablesChanged(DatabaseConnection dbconn) {
        for (Iterator i = listeners.allInstances().iterator(); i.hasNext();) {
            ((DbMetaDataListener)i.next()).tablesChanged(dbconn);
        }
    }

    public static void fireTableChanged(DatabaseConnection dbconn, String tableName) {
        for (Iterator i = listeners.allInstances().iterator(); i.hasNext();) {
            ((DbMetaDataListener)i.next()).tableChanged(dbconn, tableName);
        }
    }
}
