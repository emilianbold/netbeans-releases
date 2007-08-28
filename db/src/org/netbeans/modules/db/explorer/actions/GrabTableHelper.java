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
package org.netbeans.modules.db.explorer.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.infos.ColumnNodeInfo;

/**
 * This class refactors out logic from GrabTableAction so that we can unit
 * test it
 * 
 * @author <a href="mailto:david@vancouvering.com">David Van Couvering</a>
 */
public class GrabTableHelper {
    
    public void execute(
            Specification spec,
            String tablename,
            Enumeration nodeChildren,
            File file) throws Exception {
        CreateTable cmd = spec.createCommandCreateTable(tablename);

        while (nodeChildren.hasMoreElements()) {
            Object element = nodeChildren.nextElement();
            if (element instanceof ColumnNodeInfo) {
                cmd.getColumns().add(((ColumnNodeInfo)element).getColumnSpecification());
            }
        }
        
        FileOutputStream fstream = new FileOutputStream(file);
        ObjectOutputStream ostream = new ObjectOutputStream(fstream);
        cmd.setSpecification(null);
        ostream.writeObject(cmd);
        ostream.flush();
        ostream.close();
    }
}
