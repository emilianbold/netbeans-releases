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

package org.netbeans.modules.db.explorer.infos;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.Vector;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

public class ViewNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =8370676447530973161L;

    public void initChildren(Vector children) throws DatabaseException {
        try {
            String view = (String)get(DatabaseNode.VIEW);

            // Columns
            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getColumns(view, "%");

            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                DatabaseNodeInfo nfo;
                while (rs.next()) {
                    nfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.VIEWCOLUMN, drvSpec.getRow());
                    if (nfo != null)
                        children.add(nfo);
                }
                rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void setProperty(String key, Object obj) {
        try {
            if (key.equals("remarks")) setRemarks((String)obj); //NOI18N
            put(key, obj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setRemarks(String rem) throws DatabaseException {
        String viewname = (String)get(DatabaseNode.VIEW);
        Specification spec = (Specification)getSpecification();
        try {
            AbstractCommand cmd = spec.createCommandCommentView(viewname, rem);
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void delete() throws IOException {
        try {
            DDLHelper.deleteView((Specification)getSpecification(), 
                    (String)get(DatabaseNodeInfo.SCHEMA),
                    getName());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
