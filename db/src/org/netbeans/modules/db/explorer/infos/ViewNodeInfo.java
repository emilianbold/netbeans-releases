/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.infos;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;
import org.openide.nodes.Node;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;

public class ViewNodeInfo extends DatabaseNodeInfo
{
    static final long serialVersionUID =8370676447530973161L;
    public void initChildren(Vector children) throws DatabaseException {
        try {
            DatabaseMetaData dmd = getSpecification().getMetaData();
            String catalog = (String)get(DatabaseNode.CATALOG);
            String view = (String)get(DatabaseNode.VIEW);

            // Columns
            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getColumns(catalog, dmd, view, null);

            if (drvSpec.rs != null) {
                while (drvSpec.rs.next()) {
                    DatabaseNodeInfo nfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.VIEWCOLUMN, drvSpec.rs);
                    if (nfo != null) children.add(nfo);
                }
                drvSpec.rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void setProperty(String key, Object obj)
    {
        try {
            if (key.equals("remarks")) setRemarks((String)obj); //NOI18N
            put(key, obj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setRemarks(String rem)
    throws DatabaseException
    {
        String viewname = (String)get(DatabaseNode.VIEW);
        Specification spec = (Specification)getSpecification();
        try {
            AbstractCommand cmd = spec.createCommandCommentView(viewname, rem);
            cmd.execute();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void delete()
    throws IOException
    {
        try {
            String code = getCode();
            String table = (String)get(DatabaseNode.TABLE);
            Specification spec = (Specification)getSpecification();
            AbstractCommand cmd = spec.createCommandDropView(getName());
            cmd.execute();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
