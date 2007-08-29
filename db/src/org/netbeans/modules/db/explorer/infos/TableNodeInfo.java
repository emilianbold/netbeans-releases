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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;

import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

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
            drvSpec.getIndexInfo(table, true, true);
            rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                DatabaseNodeInfo iinfo;
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
            DatabaseException dbe = new DatabaseException(e.getMessage());
            dbe.initCause(e);
            throw dbe;
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
            if (target.get(nextKey) == null)
                target.put(nextKey, source.get(nextKey));
        }
    }
    
    public void setProperty(String key, Object obj) {
        try {
            if (key.equals("remarks"))
                setRemarks((String)obj); //NOI18N
            put(key, obj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setRemarks(String rem) throws DatabaseException {
        String tablename = (String)get(DatabaseNode.TABLE);
        Specification spec = (Specification)getSpecification();
        try {
            AbstractCommand cmd = spec.createCommandCommentTable(tablename, rem);
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            DatabaseException dbe = new DatabaseException(e.getMessage());
            dbe.initCause(e);
            throw dbe;
        }
    }

    public void dropIndex(DatabaseNodeInfo tinfo) throws DatabaseException {
        //???
    }

    public void refreshChildren() throws DatabaseException {
        // force init collection
        getNode().getChildren().getNodes();
        
        // create list of columns (infos)
        Vector charr = new Vector();
        put(DatabaseNodeInfo.CHILDREN, charr);
        initChildren(charr);
        
        // create sub-tree (by infos)
        try {
            Node[] subTreeNodes = new Node[charr.size()+1/*special node Foreign keys*/+/*special node Indexes*/1];

            // current sub-tree
            DatabaseNodeChildren children = (DatabaseNodeChildren) getNode().getChildren();            
            final Node[] childrenNodes = children.getNodes();            
            for (int i = 0; i < childrenNodes.length; i++)
                // is it node Indexes
                if ((childrenNodes[i]).getCookie(IndexListNodeInfo.class) != null) {
                    final int j = i;
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                // refresh indexes
                                ((DatabaseNode) childrenNodes[j]).getInfo().refreshChildren();
                            } catch (Exception ex) {
                                Logger.getLogger("global").log(Level.INFO, null, ex);
                            }
                        }
                    });
                    // add into refreshed sub-tree
                    subTreeNodes[charr.size()] = childrenNodes[i];
                } else
                // is it node Foreign keys or column?
                if ((childrenNodes[i]).getCookie(ForeignKeyListNodeInfo.class) != null) {
                    final int j = i;
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                // refresh foreign keys
                                ((DatabaseNode) childrenNodes[j]).getInfo().refreshChildren();
                            } catch (Exception ex) {
                                Logger.getLogger("global").log(Level.INFO, null, ex);
                            }
                        }
                    });
                    // add into refreshed sub-tree
                    subTreeNodes[charr.size() + 1] = childrenNodes[i];
                }

            // remove current sub-tree
            children.remove(childrenNodes);

            // build refreshed sub-tree
            for (int i=0; i<charr.size(); i++)
                subTreeNodes[i] = children.createNode((DatabaseNodeInfo) charr.elementAt(i));

            // add built sub-tree
            children.add(subTreeNodes);
            
            fireRefresh();
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }
    }

    public void delete() throws IOException {
        try {
            DDLHelper.deleteTable((Specification)getSpecification(), getTable(),
                    (String)get(DatabaseNodeInfo.SCHEMA));
            
            fireRefresh();
        } catch (Exception e) {
            org.openide.DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        }
    }

    /** Returns ColumnNodeInfo specified by info
    * Compares code and name only.
    */
    public ColumnNodeInfo getChildrenColumnInfo(ColumnNodeInfo info) {
        String scode = info.getCode();
        String sname = info.getName();

        try {
            Enumeration enu = getChildren().elements();
            while (enu.hasMoreElements()) {
                ColumnNodeInfo elem = (ColumnNodeInfo)enu.nextElement();
                if (elem.getCode().equals(scode) && elem.getName().equals(sname))
                    return elem;
            }
        } catch (Exception e) {
            //PENDING
        }
        
        return null;
    }

    public void addColumn(String tname) throws DatabaseException {
        try {
            Vector chvec = new Vector(1);
            initChildren(chvec, tname);
            if (chvec.size() == 1) {
                DatabaseNodeInfo nfo = (DatabaseNodeInfo)chvec.elementAt(0);
                DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();
                chld.createSubnode(nfo, true);
            }
            // refresh list of columns
            refreshChildren();
        } catch (Exception e) {
            DatabaseException dbe = new DatabaseException(e.getMessage());
            dbe.initCause(e);
            throw dbe;
        }
    }
}
