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

import org.openide.nodes.Node;

import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.lib.ddl.adaptors.*;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;

public class TableNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =-632875098783935367L;
    
    public void initChildren(Vector children) throws DatabaseException {
        initChildren(children, null);
    }

    private void initChildren(Vector children, String columnname) throws DatabaseException {
        try {
            String table = (String)get(DatabaseNode.TABLE);
            DriverSpecification drvSpec = getDriverSpecification();

            // Primary keys
            Hashtable ihash = new Hashtable();
            drvSpec.getPrimaryKeys(table);
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                DatabaseNodeInfo iinfo;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    iinfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.PRIMARY_KEY, rset);
                    String iname = (String)iinfo.get("name"); //NOI18N
                    ihash.put(iname,iinfo);
                    rset.clear();
                }
                rs.close();
            }

            // Indexes
            Hashtable ixhash = new Hashtable();
            drvSpec.getIndexInfo(table, true, false);
            rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                DatabaseNodeInfo iinfo;
                Object value;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    if (rset.get(new Integer(9)) == null)
                        continue;
                    iinfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEXED_COLUMN, rset);
                    String iname = (String)iinfo.get("name"); //NOI18N
                    ixhash.put(iname,iinfo);
                    rset.clear();
                }
                rs.close();
            }

            /*
            			// Foreign keys
            			Hashtable fhash = new Hashtable(); 	
            			rs = dmd.getImportedKeys(catalog,user,table);
            			while (rs.next()) {
            				DatabaseNodeInfo finfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.FOREIGN_KEY, rs);
            				String iname = (String)finfo.get("name"); //NOI18N
            				fhash.put(iname,finfo);
            			}
            			rs.close();
            */        

            // Columns
            drvSpec.getColumns(table, columnname);
            rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                DatabaseNodeInfo nfo;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    String cname = (String) rset.get(new Integer(4));

                    if (ihash.containsKey(cname)) {
                        nfo = (DatabaseNodeInfo)ihash.get(cname);
                        DatabaseNodeInfo tempInfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.COLUMN, rset);
                        copyProperties(tempInfo, nfo);
                    } else
                        if (ixhash.containsKey(cname)) {
                            nfo = (DatabaseNodeInfo)ixhash.get(cname);
                            DatabaseNodeInfo tempInfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.COLUMN, rset);
                            copyProperties(tempInfo, nfo);
                        }
                    //            else
                    //              if (fhash.containsKey(cname)) {
                    //                nfo = (DatabaseNodeInfo)fhash.get(cname);
                        else
//                                nfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.COLUMN, drvSpec.rsTemp);
                            nfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.COLUMN, rset);

                    children.add(nfo);
                    rset.clear();
                }
                rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    /**
     * Copies all properties from soure to target. Existing properties are not 
     * overwritten
     */
    private void copyProperties(DatabaseNodeInfo source, DatabaseNodeInfo target) {
        Enumeration keys = source.keys();
        while (keys.hasMoreElements()) {
            String nextKey = keys.nextElement().toString();
            
            /*  existing properties are not overwritten*/
            if (target.get(nextKey) == null) {
                target.put(nextKey, source.get(nextKey));
            }
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

    public void dropIndex(DatabaseNodeInfo tinfo)
    throws DatabaseException
    {
        DatabaseNode node = (DatabaseNode)tinfo.getNode();
        DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();
        try {
            String cname = tinfo.getName();
            Specification spec = (Specification)getSpecification();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void refreshChildren() throws DatabaseException
    {
        Vector charr = new Vector();

        // it is unnecessary (it caused the problem with Indexes node) ?????
        put(DatabaseNodeInfo.CHILDREN, charr);

        DatabaseNodeChildren chil = (DatabaseNodeChildren)getNode().getChildren();
        Node[] chilNodes = chil.getNodes();
        int iCountOfChildren = chilNodes.length;
        
        for(int i=0; i < iCountOfChildren; i++)
            if(chilNodes[i] instanceof DatabaseNode)
                // is it node Indexes or column?
                if(((DatabaseNode)chilNodes[i]).getInfo() instanceof IndexListNodeInfo)
                    // refresh indexes
                    ((DatabaseNode)chilNodes[i]).getInfo().refreshChildren();
                else
                // is it node Foreign keys or column?
                if(((DatabaseNode)chilNodes[i]).getInfo() instanceof ForeignKeyListNodeInfo)
                    // refresh foreign keys
                    ((DatabaseNode)chilNodes[i]).getInfo().refreshChildren();
                else
                    // remove column node from list of columns
                    chil.remove(new Node[] {chilNodes[i]});

        // create list of columns
        initChildren(charr);
        Enumeration en = charr.elements();
        while(en.hasMoreElements()) {
            DatabaseNode subnode = chil.createNode((DatabaseNodeInfo)en.nextElement());
            chil.add(new Node[] {subnode});
        }
    }

    public void delete()
    throws IOException
    {
        try {
            Specification spec = (Specification)getSpecification();
            AbstractCommand cmd = spec.createCommandDropTable(getTable());
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /** Returns ColumnNodeInfo specified by info
    * Compares code and name only.
    */
    public ColumnNodeInfo getChildrenColumnInfo(ColumnNodeInfo info)
    {
        String scode = info.getCode();
        String sname = info.getName();

        try {
            Enumeration enu = getChildren().elements();
            while (enu.hasMoreElements()) {
                ColumnNodeInfo elem = (ColumnNodeInfo)enu.nextElement();
                if (elem.getCode().equals(scode) && elem.getName().equals(sname)) {
                    return elem;
                }
            }
        } catch (Exception e) {}
        return null;
    }

    public void addColumn(String tname)
    throws DatabaseException
    {
        try {
            Vector chvec = new Vector(1);

            // !!! TADY JE ASI PROBLEM S REFRESHEM TABULEK PO PRIDANI !!!

            //			ResultSet rs;
            //			DatabaseMetaData dmd = getSpecification().getMetaData();
            //			String catalog = (String)get(DatabaseNode.CATALOG);
            //			String table = (String)get(DatabaseNode.TABLE);

            initChildren(chvec, tname);
            if (chvec.size() == 1) {
                DatabaseNodeInfo nfo = (DatabaseNodeInfo)chvec.elementAt(0);
                DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();
                DatabaseNode dnode = chld.createSubnode(nfo, true);
            }
            // refresh list of columns
            refreshChildren();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}
