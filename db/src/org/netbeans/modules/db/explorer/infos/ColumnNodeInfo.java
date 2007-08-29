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
import java.util.HashMap;
import java.util.Map;
import java.text.MessageFormat;
import org.netbeans.modules.db.explorer.DbUtilities;

import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.ModifyColumn;
import org.netbeans.lib.ddl.impl.RemoveColumn;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.TableColumn;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

public class ColumnNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =-1470704512178901918L;
    
    public boolean canAdd(Map propmap, String propname) {
        if (propname.equals("decdigits")) { //NOI18N
            int type = ((Integer) get("datatype")).intValue(); //NOI18N
            return (type == java.sql.Types.FLOAT || type == java.sql.Types.REAL || type == java.sql.Types.DOUBLE);
        }

        return super.canAdd(propmap, propname);
    }

    public Object getProperty(String key) {
        if (key.equals("columnsize") || key.equals("decdigits") || key.equals("ordpos") || key.equals("key_seq")) { //NOI18N
            Object val = get(key);
            if (val instanceof String)
                return Integer.valueOf((String) val);
        }
        if (key.equals("isnullable")) { //NOI18N
            String nullable = (String) get(key);
            boolean eq = (nullable == null) ? false : (nullable).toUpperCase().equals("YES"); //NOI18N
            return eq ? Boolean.TRUE : Boolean.FALSE;
        }
        return super.getProperty(key);
    }

    public void delete() throws IOException {
        try {
            String code = getCode();
            String table = (String) get(DatabaseNode.TABLE);
            Specification spec = (Specification) getSpecification();
            RemoveColumn cmd = (RemoveColumn) spec.createCommandRemoveColumn(table);
            cmd.removeColumn((String) get(code));
            cmd.setObjectOwner((String) get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();

            // refresh list of columns after column drop
            //getParent().refreshChildren();
            fireRefresh();
        } catch (Exception exc) {
            DbUtilities.reportError(bundle().getString("ERR_UnableToDeleteColumn"), exc.getMessage()); // NOI18N
        }
    }

    public TableColumn getColumnSpecification() throws DatabaseException {
        TableColumn col = null;

        try {
            Specification spec = (Specification) getSpecification();
            CreateTable cmd = (CreateTable) spec.createCommandCreateTable("DUMMY"); //NOI18N
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
                String message = MessageFormat.format(bundle().getString("EXC_UnknownCode"), new String[] {code}); // NOI18N
                throw new DatabaseException(message);
            }

            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getColumns((String) get(DatabaseNode.TABLE), (String)get(code));
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                rs.next();
                HashMap rset = drvSpec.getRow();
                
                try {
                    //hack because of MSSQL ODBC problems - see DriverSpecification.getRow() for more info - shouln't be thrown
                    col.setColumnType(Integer.parseInt((String) rset.get(new Integer(5))));
                    col.setColumnSize(Integer.parseInt((String) rset.get(new Integer(7))));
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
            throw new DatabaseException(e);
        }

        return col;
    }

    // catalog,schema,tablename,name,datatype,typename,
    // columnsize,bufflen,decdigits,radix,nullable,remarks,coldef,
    // reserved1,reserved2,octetlen,ordpos,isnullable

    public void setProperty(String key, Object obj) {
        try {
            if (key.equals("remarks")) //NOI18N
                setRemarks((String)obj);
            else if (key.equals("isnullable")) { //NOI18N
                setNullAllowed(((Boolean) obj).booleanValue());
                obj = (((Boolean) obj).equals(Boolean.TRUE) ? "YES" : "NO"); //NOI18N
            } else if (key.equals("columnsize")) //NOI18N
                setColumnSize((Integer) obj);
            else if (key.equals("decdigits")) //NOI18N
                setDecimalDigits((Integer) obj);
            else if (key.equals("coldef")) //NOI18N
                setDefaultValue((String) obj);
            else if (key.equals("datatype")) //NOI18N
                setDataType((Integer) obj);
            
            super.setProperty(key, obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRemarks(String rem) throws DatabaseException {
        String tablename = (String) get(DatabaseNode.TABLE);
        Specification spec = (Specification) getSpecification();
        try {
            AbstractCommand cmd = spec.createCommandCommentTable(tablename, rem);
            cmd.setObjectOwner((String) get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void setColumnSize(Integer size) throws DatabaseException {
        try {
            Specification spec = (Specification) getSpecification();
            ModifyColumn cmd = (ModifyColumn) spec.createCommandModifyColumn(getTable());
            TableColumn col = getColumnSpecification();
            col.setColumnSize(size.intValue());
            cmd.setColumn(col);
            cmd.setObjectOwner((String) get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void setDecimalDigits(Integer size) throws DatabaseException {
        try {
            Specification spec = (Specification) getSpecification();
            ModifyColumn cmd = (ModifyColumn) spec.createCommandModifyColumn(getTable());
            TableColumn col = getColumnSpecification();
            col.setDecimalSize(size.intValue());
            cmd.setColumn(col);
            cmd.setObjectOwner((String) get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void setDefaultValue(String val) throws DatabaseException {
        try {
            Specification spec = (Specification) getSpecification();
            ModifyColumn cmd = (ModifyColumn) spec.createCommandModifyColumn(getTable());
            TableColumn col = getColumnSpecification();
            col.setDefaultValue(val);
            cmd.setColumn(col);
            cmd.setObjectOwner((String) get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void setNullAllowed(boolean flag) throws DatabaseException {
        try {
            Specification spec = (Specification) getSpecification();
            ModifyColumn cmd = (ModifyColumn) spec.createCommandModifyColumn(getTable());
            TableColumn col = getColumnSpecification();
            col.setNullAllowed(flag);
            cmd.setColumn(col);
            cmd.setObjectOwner((String) get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void setDataType(Integer type) throws DatabaseException {
        try {
            Specification spec = (Specification) getSpecification();
            ModifyColumn cmd = (ModifyColumn) spec.createCommandModifyColumn(getTable());
            TableColumn col = getColumnSpecification();
            col.setColumnType(type.intValue());
            cmd.setColumn(col);
            cmd.setObjectOwner((String) get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    /**
     * Using name of column for hashCode computation.
     *
     * @return  computed hashCode based on name of column
     */
    public int hashCode() {
        return getName().hashCode();
    }
    
}
