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
import java.text.MessageFormat;

import org.openide.*;
import org.openide.nodes.Node;

import org.netbeans.lib.ddl.*;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;

public class ColumnNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =-1470704512178901918L;
    
    public boolean canAdd(Map propmap, String propname) {
        if (propname.equals("decdigits")) { //NOI18N
            int type = ((Integer)get("datatype")).intValue(); //NOI18N
            if (type == java.sql.Types.FLOAT || type == java.sql.Types.REAL || type == java.sql.Types.DOUBLE) return true;
            else return false;
        }

        return super.canAdd(propmap, propname);
    }

    public Object getProperty(String key) {
        if (key.equals("isnullable")) { //NOI18N
            boolean eq = ((String)get(key)).toUpperCase().equals("YES"); //NOI18N
            return new Boolean(eq);
        }
        return super.getProperty(key);
    }

    public void delete()
    throws IOException
    {
        try {
            String code = getCode();
            String table = (String)get(DatabaseNode.TABLE);
            Specification spec = (Specification)getSpecification();
            RemoveColumn cmd = (RemoveColumn)spec.createCommandRemoveColumn(table);
            cmd.removeColumn((String)get(code));
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
            // refresh list of columns after column drop
            //getParent().refreshChildren();
        //} catch(DatabaseException exc) {
            //String message = MessageFormat.format(bundle.getString("ERR_UnableToDeleteColumn"), new String[] {exc.getMessage()}); // NOI18N
            //TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception exc) {
            throw new IOException(exc.getMessage());
        }
    }

    public TableColumn getColumnSpecification() throws DatabaseException {
        TableColumn col = null;

        try {
            Specification spec = (Specification)getSpecification();
            CreateTable cmd = (CreateTable)spec.createCommandCreateTable("DUMMY"); //NOI18N
            String code = getCode();

            if (code.equals(DatabaseNode.PRIMARY_KEY)) {
                col = (TableColumn)cmd.createPrimaryKeyColumn(getName());
            } else if (code.equals(DatabaseNode.INDEXED_COLUMN)) {
                col = (TableColumn)cmd.createUniqueColumn(getName());
            } else if (code.equals(DatabaseNode.FOREIGN_KEY)) {
                col = null;
            } else if (code.equals(DatabaseNode.COLUMN)) {
                col = (TableColumn)cmd.createColumn(getName());
            } else {
                String message = MessageFormat.format(bundle.getString("EXC_UnknownCode"), new String[] {code}); // NOI18N
                throw new DatabaseException(message);
            }

            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getColumns((String)get(DatabaseNode.TABLE), (String)get(code));
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                rs.next();
                HashMap rset = new HashMap();
                rset = drvSpec.getRow();
                
                try {
                    //hack because of MSSQL ODBC problems - see DriverSpecification.getRow() for more info - shouln't be thrown
                    col.setColumnType(new Integer((String) rset.get(new Integer(5))).intValue());
                    col.setColumnSize(new Integer((String) rset.get(new Integer(7))).intValue());
                } catch (NumberFormatException exc) {
                    col.setColumnType(0);
                    col.setColumnSize(0);
                }

                col.setNullAllowed(((String) rset.get(new Integer(18))).toUpperCase().equals("YES")); //NOI18N
                col.setDefaultValue((String) rset.get(new Integer(13)));
                rset.clear();

                rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }

        return col;
    }

    // catalog,schema,tablename,name,datatype,typename,
    // columnsize,bufflen,decdigits,radix,nullable,remarks,coldef,
    // reserved1,reserved2,octetlen,ordpos,isnullable

    public void setProperty(String key, Object obj)
    {
        try {
            if (key.equals("remarks")) setRemarks((String)obj); //NOI18N
            else if (key.equals("isnullable")) { //NOI18N
                setNullAllowed(((Boolean)obj).booleanValue());
                obj = (((Boolean)obj).equals(Boolean.TRUE) ? "YES" : "NO"); //NOI18N
            } else if (key.equals("columnsize")) setColumnSize((Integer)obj); //NOI18N
            else if (key.equals("decdigits")) setDecimalDigits((Integer)obj); //NOI18N
            else if (key.equals("coldef")) setDefaultValue((String)obj); //NOI18N
            else if (key.equals("datatype")) setDataType((Integer)obj); //NOI18N
            super.setProperty(key, obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRemarks(String rem)
    throws DatabaseException
    {
        String tablename = (String)get(DatabaseNode.TABLE);
        Specification spec = (Specification)getSpecification();
        try {
            AbstractCommand cmd = spec.createCommandCommentTable(tablename, rem);
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void setColumnSize(Integer size)
    throws DatabaseException
    {
        try {
            Specification spec = (Specification)getSpecification();
            ModifyColumn cmd = (ModifyColumn)spec.createCommandModifyColumn(getTable());
            TableColumn col = getColumnSpecification();
            col.setColumnSize(size.intValue());
            cmd.setColumn(col);
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void setDecimalDigits(Integer size)
    throws DatabaseException
    {
        try {
            Specification spec = (Specification)getSpecification();
            ModifyColumn cmd = (ModifyColumn)spec.createCommandModifyColumn(getTable());
            TableColumn col = getColumnSpecification();
            col.setDecimalSize(size.intValue());
            cmd.setColumn(col);
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void setDefaultValue(String val)
    throws DatabaseException
    {
        try {
            Specification spec = (Specification)getSpecification();
            ModifyColumn cmd = (ModifyColumn)spec.createCommandModifyColumn(getTable());
            TableColumn col = getColumnSpecification();
            col.setDefaultValue(val);
            cmd.setColumn(col);
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void setNullAllowed(boolean flag)
    throws DatabaseException
    {
        try {
            Specification spec = (Specification)getSpecification();
            ModifyColumn cmd = (ModifyColumn)spec.createCommandModifyColumn(getTable());
            TableColumn col = getColumnSpecification();
            col.setNullAllowed(flag);
            cmd.setColumn(col);
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void setDataType(Integer type)
    throws DatabaseException
    {
        try {
            Specification spec = (Specification)getSpecification();
            ModifyColumn cmd = (ModifyColumn)spec.createCommandModifyColumn(getTable());
            TableColumn col = getColumnSpecification();
            col.setColumnType(type.intValue());
            cmd.setColumn(col);
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}
