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

package org.netbeans.modules.db.explorer.nodes;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.*;
import java.text.MessageFormat;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;


import org.openide.nodes.NodeTransfer;
import org.openide.nodes.Node;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.openide.util.datatransfer.ExTransferable;

// Node for Table/View/Procedure things.

public class TableNode extends DatabaseNode /*implements InstanceCookie*/ {
    public TableNode(DatabaseNodeInfo info) {
        super(info);
    }
    
    
    @Override
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
    
    
    public Transferable clipboardCopy() throws IOException {
        ExTransferable result = ExTransferable.create(super.clipboardCopy());
        ConnectionNodeInfo cni = (ConnectionNodeInfo)getInfo().getParent(DatabaseNode.CONNECTION);
        final DatabaseConnection dbconn = ConnectionList.getDefault().getConnection(cni.getDatabaseConnection());
        result.put(new ExTransferable.Single(DatabaseMetaDataTransfer.TABLE_FLAVOR) {
            protected Object getData() {
                return DatabaseMetaDataTransferAccessor.DEFAULT.createTableData(dbconn.getDatabaseConnection(), dbconn.findJDBCDriver(), getInfo().getName());
            }
        });
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
}
