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

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import java.sql.*;

import org.openide.nodes.Node;

import org.netbeans.lib.ddl.impl.*;
import org.netbeans.lib.ddl.util.PListReader;
import org.netbeans.modules.db.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.nodes.RootNode;

public class ProcedureNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =-5984072379104199563L;

    /*
    	public DatabaseDriver getDatabaseDriver() {
    		return (DatabaseDriver)get(DatabaseNodeInfo.DBDRIVER);
    	}
      
    	public void setDatabaseDriver(DatabaseDriver drv) {
    		put(DatabaseNodeInfo.NAME, drv.getName());
    		put(DatabaseNodeInfo.URL, drv.getURL());
    		put(DatabaseNodeInfo.DBDRIVER, drv);
    	}
    */

    public void initChildren(Vector children) throws DatabaseException {
        try {
            String name = (String)get(DatabaseNode.PROCEDURE);

            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getProcedureColumns(name, "%");
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                DatabaseNodeInfo info;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.PROCEDURE_COLUMN, rset);
                    if (info != null) {
                        Object ibase = null;
                        String itype = "unknown"; //NOI18N
                        
//                        int type = ((Number)info.get("type")).intValue(); //NOI18N
                        
//cannot use previous line because of MSSQL ODBC problems - see DriverSpecification.getRow() for more info
                        int type;       
                        try {
                            type = (new Integer(info.get("type").toString())).intValue(); //NOI18N
                        } catch (NumberFormatException exc) {
                            throw new IllegalArgumentException(exc.getMessage());
                        }
//end of MSSQL hack
                        
                        switch (type) {
                        case DatabaseMetaData.procedureColumnIn:
                            ibase = info.get("iconbase_in"); //NOI18N
                            itype = "in"; //NOI18N
                            break;
                        case DatabaseMetaData.procedureColumnOut:
                            ibase = info.get("iconbase_out"); //NOI18N
                            itype = "out"; //NOI18N
                            break;
                        case DatabaseMetaData.procedureColumnInOut:
                            ibase = info.get("iconbase_inout"); //NOI18N
                            itype = "in/out"; //NOI18N
                            break;
                        case DatabaseMetaData.procedureColumnReturn:
                            ibase = info.get("iconbase_return"); //NOI18N
                            itype = "return"; //NOI18N
                            break;
                        case DatabaseMetaData.procedureColumnResult:
                            ibase = info.get("iconbase_result"); //NOI18N
                            itype = "result"; //NOI18N
                            break;
                        }
                        if (ibase != null)
                            info.put("iconbase", ibase); //NOI18N
                        info.put("type", itype); //NOI18N
                        children.add(info);
                    } else
                        throw new Exception(bundle.getString("EXC_UnableToCreateProcedureColumnNodeInfo"));
                    rset.clear();
                }
                rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    /* delete procedure from list of procedures and drop procedure in the database */	
    public void delete()
    throws IOException
    {
        try {
            Specification spec = (Specification)getSpecification();
            AbstractCommand cmd = spec.createCommandDropProcedure((String)get(DatabaseNode.PROCEDURE));
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
