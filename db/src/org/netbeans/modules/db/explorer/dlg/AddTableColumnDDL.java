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
package org.netbeans.modules.db.explorer.dlg;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.impl.AddColumn;
import org.netbeans.lib.ddl.impl.CreateIndex;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.DropIndex;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.TableColumn;
import org.netbeans.lib.ddl.util.CommandBuffer;

/**
 * This class factors out the logic of actually adding a column to
 * the database.  It is responsible for interacting with the DDL package.
 * 
 * This refactoring is done to both separate the view from the underlying
 * logic, and also to make it more possible to unit test this logic
 */
public class AddTableColumnDDL {
    Specification       spec;
    DriverSpecification drvSpec;
    String              schema;
    String              tablename;
    String              colname;
    ColumnItem          citem;
    Map                 indexMap = null;
    Map                 uniqueIndexMap = null;
    String              indexName = null;
    boolean             wasException = false;
    

    public AddTableColumnDDL(
            Specification spec, 
            DriverSpecification drvspec,
            String schema,
            String tablename) {
        this.spec       = spec;
        this.drvSpec    = drvspec;
        this.schema     = schema;
        this.tablename  = tablename;
    }
        
    public void setColumn(String colname, ColumnItem citem) {
        this.colname = colname;
        this.citem = citem;
    }
    
    public Map getIndexMap() throws DatabaseException {
        if ( indexMap == null ) {
            buildIndexMaps();
        }
        
        return indexMap;
    }
    
    public Map getUniqueIndexMap() throws DatabaseException {
        if ( uniqueIndexMap == null ) {
            buildIndexMaps();            
        }
        
        return uniqueIndexMap;
    }
    
    private void buildIndexMaps() throws DatabaseException {
        try {
            drvSpec.getIndexInfo(tablename, false, true);
            ResultSet rs = drvSpec.getResultSet();
            HashMap rset = new HashMap();

            indexMap = new HashMap();
            uniqueIndexMap = new HashMap();
            String ixname;
            while (rs.next()) {
                rset = drvSpec.getRow();
                ixname = (String) rset.get(new Integer(6));
                if (ixname != null) {
                    Vector ixcols = (Vector)indexMap.get(ixname);
                    if (ixcols == null) {
                        ixcols = new Vector();
                        indexMap.put(ixname,ixcols);
                        boolean uq = !Boolean.valueOf( 
                                (String)rset.get( new Integer(4) ) ).booleanValue();
                        if(uq)
                            uniqueIndexMap.put( ixname, ColumnItem.UNIQUE );
                    }

                    ixcols.add((String) rset.get(new Integer(9)));
                }
                rset.clear();
            }
            rs.close();
        } catch (SQLException sqle) {
            DatabaseException dbe = new DatabaseException(sqle.getMessage());
            dbe.initCause(sqle);
            throw dbe;
        }
    }
    
    private boolean useIndex() {
        assert citem != null;
        return citem.isIndexed() && !citem.isUnique() && !citem.isPrimaryKey(); 
    }
    
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
    
    public void execute() throws Exception {
        assert citem != null;
        assert colname != null;
        assert (useIndex() && indexName != null) ||
                !useIndex();
        
        CommandBuffer cbuff = new CommandBuffer();

        AddColumn cmd = spec.createCommandAddColumn(tablename);
        cmd.setObjectOwner(schema);
        org.netbeans.lib.ddl.impl.TableColumn col = null;
        if (citem.isPrimaryKey()) {
          col = cmd.createPrimaryKeyColumn(colname);
        } else if (citem.isUnique()) {
          col = cmd.createUniqueColumn(colname);
        } else col = (TableColumn)cmd.createColumn(colname);
        col.setColumnType(Specification.getType(citem.getType().getType()));
        col.setColumnSize(citem.getSize());
        col.setDecimalSize(citem.getScale());
        col.setNullAllowed(citem.allowsNull());
        if (citem.hasDefaultValue()) col.setDefaultValue(citem.getDefaultValue());

        if (citem.hasCheckConstraint()) {
          // add COLUMN constraint (without constraint name)
          col.setCheckCondition(citem.getCheckConstraint());
        }

        cbuff.add(cmd);

        if (useIndex() ) {
          addIndex(cbuff);
        }

        this.wasException = false;

        cbuff.execute();

        if ( cbuff.wasException() ) {
          this.wasException = true;
        }
    }
    
    public boolean wasException() {
        return wasException;
    }
    
    
    private void addIndex(CommandBuffer cbuff) throws Exception {
          buildIndexMaps();

          String isUQ = new String();
          if (indexMap.containsKey(indexName)) {
              if(uniqueIndexMap.containsKey(indexName))
                                isUQ = ColumnItem.UNIQUE;
              DropIndex dropIndexCmd = spec.createCommandDropIndex(indexName);
              dropIndexCmd.setTableName(tablename);
              dropIndexCmd.setObjectOwner(schema);
              cbuff.add(dropIndexCmd);
          }

          CreateIndex xcmd = spec.createCommandCreateIndex(tablename);
          xcmd.setIndexName(indexName);
          xcmd.setIndexType(isUQ);
          xcmd.setObjectOwner(schema);
          Enumeration enu = ((Vector)indexMap.get(indexName)).elements();
          while (enu.hasMoreElements()) {
              xcmd.specifyColumn((String)enu.nextElement());
          }
          xcmd.specifyColumn(citem.getName());
          cbuff.add(xcmd);

    }

}
