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

package org.netbeans.modules.db.explorer.nodes;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.*;
import java.text.MessageFormat;


import org.openide.*;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.Node;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

// Node for Table/View/Procedure things.

public class TableNode extends DatabaseNode /*implements InstanceCookie*/ {
    public void setInfo(DatabaseNodeInfo nodeinfo)
    {
        super.setInfo(nodeinfo);
        getCookieSet().add(this);
    }

/*
    public String instanceName() {
        return "org.netbeans.lib.sql.ConnectionSource"; //NOI18N
    }

    public Class instanceClass() throws IOException, ClassNotFoundException {
        return Class.forName("org.netbeans.lib.sql.ConnectionSource", true, org.openide.Topmanager.getDefault ().currentClassLoader ()); //NOI18N
    }

    public Object instanceCreate()
    {
        DatabaseNodeInfo info = getInfo();
        try {
            Method met;
            Class objclass = instanceClass();
            String drv = info.getDriver();
            String db = info.getDatabase();
            String usr = info.getUser();
            String pwd = info.getPassword();
            Object obj =  objclass.newInstance();

            met = objclass.getMethod("setDriver", new Class[] {String.class}); //NOI18N
            if (met != null) met.invoke(obj, new String[] {drv});
            met = objclass.getMethod("setDatabase", new Class[] {String.class}); //NOI18N
            if (met != null) met.invoke(obj, new String[] {db});
            met = objclass.getMethod("setUsername", new Class[] {String.class}); //NOI18N
            if (met != null) met.invoke(obj, new String[] {usr});
            met = objclass.getMethod("setPassword", new Class[] {String.class}); //NOI18N
            if (met != null) met.invoke(obj, new String[] {pwd});

            return obj;

        } catch (Exception ex) {
            ex.printStackTrace ();
            return null;
        }
    }
*/
    
    public void setName(String newname)
    {
        try {
            DatabaseNodeInfo info = getInfo();
            Specification spec = (Specification)info.getSpecification();
            AbstractCommand cmd = spec.createCommandRenameTable(info.getName(), newname);
            cmd.setObjectOwner((String)info.get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
            super.setName(newname);
            info.put(DatabaseNode.TABLE, newname);
        } catch (CommandNotSupportedException ex) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception ex) {
            //			ex.printStackTrace();
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        }
    }
    
    public Transferable clipboardCopy() throws IOException {
        Transferable result;
        final DbMetaDataTransferProvider dbTansferProvider = (DbMetaDataTransferProvider)Lookup.getDefault().lookup(DbMetaDataTransferProvider.class);
        if (dbTansferProvider != null) {
            ExTransferable exTransferable = ExTransferable.create(super.clipboardCopy());
            ConnectionNodeInfo cni = (ConnectionNodeInfo)getInfo().getParent(DatabaseNode.CONNECTION);
            final DatabaseConnection dbconn = ConnectionList.getDefault().getConnection(cni.getDatabaseConnection());
            exTransferable.put(new ExTransferable.Single(dbTansferProvider.getTableDataFlavor()) {
                protected Object getData() {
                    return dbTansferProvider.createTableData(dbconn.getDatabaseConnection(), dbconn.findJDBCDriver(), getInfo().getName());
                }
            });
            result = exTransferable;
        } else {
            result = super.clipboardCopy();
        }
        return result;
    }

    protected void createPasteTypes(Transferable t, List s)
    {
        super.createPasteTypes(t, s);
        DatabaseNodeInfo nfo;
        Node n = NodeTransfer.node(t, NodeTransfer.MOVE);
        if (n != null && n.canDestroy ()) {
            /*
            			nfo = (TableNodeInfo)n.getCookie(TableNodeInfo.class);
            			if (nfo != null) {
            				s.add(new TablePasteType((TableNodeInfo)nfo, n));
            				return;
            			}  
            */
            nfo = (ColumnNodeInfo)n.getCookie(ColumnNodeInfo.class);
            if (nfo != null) {
                s.add(new ColumnPasteType((ColumnNodeInfo)nfo, n));
                return;
            }

        } else {
            /*
            			nfo = (DatabaseNodeInfo)NodeTransfer.copyCookie(t, TableNodeInfo.class);
            			if (nfo != null) {
            				s.add(new TablePasteType((TableNodeInfo)nfo, null));
            				return;
            			}
            */	
            nfo = (DatabaseNodeInfo)NodeTransfer.cookie(t, NodeTransfer.MOVE, ColumnNodeInfo.class);
            if (nfo != null) {
                s.add(new ColumnPasteType((ColumnNodeInfo)nfo, null));
                return;
            }
        }
    }

    /** Paste type for transfering tables.
    */
    private class TablePasteType extends PasteType
    {
        /** transferred info */
        private DatabaseNodeInfo info;

        /** the node to destroy or null */
        private Node node;

        /** Constructs new TablePasteType for the specific type of operation paste.
        */
        public TablePasteType(TableNodeInfo info, Node node)
        {
            this.info = info;
            this.node = node;
        }

        /* @return Human presentable name of this paste type. */
        public String getName()
        {
            ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N
            return bundle.getString("PasteTableName"); //NOI18N
        }

        /** Performs the paste action.
        * @return Transferable which should be inserted into the clipboard after
        *         paste action. It can be null, which means that clipboard content
        *         should stay the same.
        */
        public Transferable paste() throws IOException
        {
            TableNodeInfo info = (TableNodeInfo)getInfo();
            ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle_noi18n"); //NOI18N
            TableListNodeInfo ownerinfo = (TableListNodeInfo)getInfo().getParent(DatabaseNode.TABLELIST);
            if (info != null) {
                TableNodeInfo exinfo = ownerinfo.getChildrenTableInfo(info);
                DatabaseNodeChildren chi = (DatabaseNodeChildren)getChildren();
                String name = info.getName();
                if (exinfo != null) {
                    String namefmt = bundle.getString("PasteTableNameFormat"); //NOI18N
                    name = MessageFormat.format(namefmt, new String[] {name});
                }

                try {

                    // Create in database
                    // PENDING

                    ownerinfo.addTable(name);
                    if (node != null) node.destroy ();

                } catch (Exception e) {
                    throw new IOException(e.getMessage());
                }

            } else
                throw new IOException(bundle.getString("EXC_CannotFindTableOwnerInformation")); //NOI18N
            
            return null;
        }
    }

    /** Paste type for transfering columns.
    */
    private class ColumnPasteType extends PasteType {
        final ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N
        
        /** transferred info */
        private DatabaseNodeInfo info;

        /** the node to destroy or null */
        private Node node;

        /** Constructs new TablePasteType for the specific type of operation paste.
        */
        public ColumnPasteType(ColumnNodeInfo info, Node node)
        {
            this.info = info;
            this.node = node;
        }

        /* @return Human presentable name of this paste type. */
        public String getName() {
            return bundle.getString("PasteColumnName"); //NOI18N
        }

        /** Performs the paste action.
        * @return Transferable which should be inserted into the clipboard after
        *         paste action. It can be null, which means that clipboard content
        *         should stay the same.
        */
        public Transferable paste() throws IOException
        {
            ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N
            TableNodeInfo ownerinfo = (TableNodeInfo)getInfo();
            if (info != null) {
                try {
                    String name = info.getName();
                    ColumnNodeInfo coli = (ColumnNodeInfo)info;
                    TableColumn col = coli.getColumnSpecification();
                    Specification spec = (Specification)ownerinfo.getSpecification();
                    AddColumn cmd = (AddColumn)spec.createCommandAddColumn(ownerinfo.getTable());
                    cmd.getColumns().add(col);
                    cmd.setObjectOwner((String)info.get(DatabaseNodeInfo.SCHEMA));
                    cmd.execute();
                    ownerinfo.addColumn(name);
                    if (node != null) node.destroy();
                } catch (final Exception ex) {
                    ex.printStackTrace();
                    /*
                    					SwingUtilities.invokeLater(new Runnable() {
                    						public void run() {
                    							Topmanager.getDefault().notify(new NotifyDescriptor.Message("Unable to process command, "+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                    						}
                    					});
                    */					
                }
            } else throw new IOException(bundle.getString("EXC_CannotFindColumnOwnerInformation")); //NOI18N
            return null;
        }
    }

    public String getShortDescription() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ND_Table"); //NOI18N
    }
    
}
