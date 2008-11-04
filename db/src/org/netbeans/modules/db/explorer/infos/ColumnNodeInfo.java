/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.db.explorer.infos;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.text.MessageFormat;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.explorer.DbUtilities;

import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.ModifyColumn;
import org.netbeans.lib.ddl.impl.RemoveColumn;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.TableColumn;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.nodes.ColumnNode;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

public class ColumnNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =-1470704512178901918L;
    private static final Logger LOGGER = Logger.getLogger(ColumnNode.class.getName());
    private static final double JAVADB_MINOR_VERSION_SUPPORTED = 3;  // Java DB minor version that supports column deletion
    private static final String DELETE_ACTION_CLASS = "DeleteAction";  // NOI18N


    @Override
    public Vector getActions() {
        // #149904 [65cat] Cannot remove database table column from action
        Vector actions = super.getActions();
        Vector revisedActions = new Vector();
        Specification spec = (Specification) getSpecification();
        // If Java DB doesn't support column deletion, exclude the Delete action
        for (int i = 0; i < actions.size(); i++) {
            if (spec.getProperties().get("DatabaseProductName").equals("Apache Derby") && !isSupported(spec)) { // NOI18N
                if (actions.get(i) != null) {
                    String simpleClassName = actions.get(i).getClass().getSimpleName();
                    if (!simpleClassName.equals(DELETE_ACTION_CLASS)) {
                        revisedActions.add(actions.get(i));
                    }
                }
            } else {
                revisedActions.add(actions.get(i));
            }
        }
        return revisedActions;
    }
    
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

            notifyChange();
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
    
    public int getColumnPosition() {
        Object ordpos = getProperty("ordpos");
        if (ordpos == null) {
            return 0;
        } else {
            return (Integer)ordpos;
        }
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

    @Override
    public int compareTo(Object o2) {
        // Sort based on column position, not name
        if ( equals(o2) ) {
            return 0;
        }
        
        if ( ! (o2 instanceof ColumnNodeInfo) ) {
            return super.compareTo(o2);
        }

        if (getColumnPosition() == 0) {
            // The database is not telling us ordinal positions, so sort by name
            return super.compareTo(o2);
        }
        
        return this.getColumnPosition() - 
                ((ColumnNodeInfo)o2).getColumnPosition();
    }

    private boolean isSupported(Specification spec) {
        try {
            int majorVersion = spec.getMetaData().getDatabaseMajorVersion();
            if (majorVersion < 10) {
                return false;
            }
            String productVersion = spec.getMetaData().getDatabaseProductVersion();
            int dotLoc = productVersion.indexOf("."); // NOI18N
            if (dotLoc != -1) {  // check if no "dot" in the release - if future Java DB versions do not support dot releases
                int minorVersion = Integer.parseInt(productVersion.substring(dotLoc + 1, dotLoc + 2));
                if (minorVersion < JAVADB_MINOR_VERSION_SUPPORTED) {
                    return false;
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.INFO, "ColumnNodeInfo.isSupported() threw SQLException", ex);
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.INFO, "ColumnNodeInfo.isSupported() threw NumberFormatException retrieving the version", nfe);
        }
        return true;
    }
}
