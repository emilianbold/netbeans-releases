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

package org.netbeans.modules.j2ee.persistence.editor.completion.db;

import java.sql.SQLException;
import org.netbeans.modules.dbschema.TableElement;
import org.netbeans.modules.j2ee.persistence.editor.completion.*;

/**
 *
 * @author abadea
 */
public class DBMetaDataUtils {
    
    private DBMetaDataUtils() {
        assert false;
    }
    
    public static Schema getSchema(DBMetaDataProvider provider, String catalogName, String schemaName) throws SQLException {
        Catalog catalog = provider.getCatalog(catalogName);
        if (catalog != null) {
            return catalog.getSchema(schemaName);
        } 
        return null;
        
    }
    
    public static TableElement getTable(DBMetaDataProvider provider, String catalogName, String schemaName, String tableName) throws SQLException {
        Schema schema = getSchema(provider, catalogName, schemaName);
        if (schema != null) {
            return schema.getTable(tableName);
        }
        return null;
    }
}
