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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.explorer.infos;

import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.DropIndex;
import org.netbeans.lib.ddl.impl.Specification;

/**
 * This class factors out interaction with the DDL package.  This allows
 * us to unit test this interaction
 * 
 * @author <a href="mailto:david@vancouvering.com">David Van Couvering</a>
 */
public class DDLHelper {
    public static void deleteTable(Specification spec, 
            String schema, String tablename) throws Exception {
        AbstractCommand cmd = spec.createCommandDropTable(tablename);
        cmd.setObjectOwner(schema);
        cmd.execute();
    }
    
    public static void deleteIndex(Specification spec,
            String schema, String tablename, String indexname)
            throws Exception
    {
        DropIndex cmd = spec.createCommandDropIndex(indexname);
        cmd.setTableName(tablename);
        cmd.setObjectOwner(schema);
        cmd.execute();        
    }
    
    public static void deleteView(Specification spec,
            String schema, String viewname) throws Exception {
        AbstractCommand cmd = spec.createCommandDropView(viewname);
        cmd.setObjectOwner(schema);
        cmd.execute();        
    }
}
