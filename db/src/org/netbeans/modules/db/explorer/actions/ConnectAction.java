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

package org.netbeans.modules.db.explorer.actions;

import java.beans.*;
import java.io.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.JTabbedPane;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import org.netbeans.lib.ddl.DBConnection;
import org.netbeans.lib.ddl.impl.*;

import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.dlg.*;

public class ConnectAction extends DatabaseAction {
    static final long serialVersionUID =-6822218300035053411L;

    ConnectionDialog dlg = null;

    protected boolean enable(Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
        else return false;

        DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
        DatabaseNodeInfo nfo = info.getParent(DatabaseNode.CONNECTION);
        if (nfo != null) return (nfo.getConnection() == null);
        return false;
    }

    public void performAction(Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
        else return;

        DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
        final ConnectionNodeInfo nfo = (ConnectionNodeInfo)info.getParent(DatabaseNode.CONNECTION);
        Connection connection = nfo.getConnection();
        if (connection != null) return;

        String drvurl = (String)nfo.get(DatabaseNodeInfo.DRIVER);
        String dburl = (String)nfo.get(DatabaseNodeInfo.DATABASE);
        String user = (String)nfo.getUser();
        String pwd = (String)nfo.getPassword();
        Boolean rpwd = (Boolean)nfo.get(DatabaseNodeInfo.REMEMBER_PWD);
        boolean remember = ((rpwd != null) ? rpwd.booleanValue() : false);
        if (user == null || pwd == null || !remember) {

            final ConnectPanel basePanel = new ConnectPanel(user);
            final SchemaPanel schemaPanel = new SchemaPanel(new Vector(), user);

            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (event.getSource() == DialogDescriptor.OK_OPTION) {
                        dlg.setException(null);
                        String oldUser = nfo.getUser();
                        String oldSchema = nfo.getSchema();
                        nfo.setUser(basePanel.getUser());
                        nfo.setPassword(basePanel.getPassword());
                        if (basePanel.rememberPassword())
                            nfo.put(DatabaseNodeInfo.REMEMBER_PWD, new Boolean(true));
                        try {
                            if(schemaPanel.getSchema()==null)
                                dlg.setSelectedComponent(schemaPanel);
                            if(dlg.isException())
                                return;
                            nfo.setSchema(schemaPanel.getSchema());
                            nfo.connect();
                            if(dlg!=null) dlg.close();
                        } catch (Exception exc) {
                            //exc.printStackTrace();
                            nfo.setUser(oldUser);
                            nfo.setSchema(oldSchema);
                            String message = MessageFormat.format(bundle.getString("ERR_UnableToConnect"), new String[] {exc.getMessage()}); // NOI18N
                            TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                        }
                    }
                  }
                };

            ChangeListener changeTabListener = new ChangeListener() {
                public void stateChanged (ChangeEvent e) {
                    if(((JTabbedPane)e.getSource()).getSelectedComponent().equals(schemaPanel)) {
                        nfo.setUser(basePanel.getUser());
                        nfo.setPassword(basePanel.getPassword());
                        DBConnection con = nfo.getDatabaseConnection();

                        try {
                            Connection connection = con.createJDBCConnection();
                            if (connection != null) {
                                ResultSet rs;
                                Vector items = new Vector();
                                try {
                                    rs = connection.getMetaData().getSchemas();
                                    while (rs.next())
                                        items.add(rs.getString(1).trim());
                                    rs.close();
                                } catch (SQLException exc) {
                                    //hack for databases which don't support schemas
                                }
                                connection.close();
                                schemaPanel.setSchemas(items, nfo.getSchema());
                            }
                        } catch(Exception exc) {
                            String message = MessageFormat.format(bundle.getString("ERR_UnableObtainSchemas"), new String[] {exc.getMessage()}); // NOI18N
                            TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                            dlg.setSelectedComponent(basePanel);
                            dlg.setException(new DatabaseException("Unable to obtain schema. "+exc.getMessage())); // NOI18N
                        }

                    } else
                        if(schemaPanel.getSchema()!=null)
                            nfo.setSchema(schemaPanel.getSchema());
                }
            };

            dlg = new ConnectionDialog(
                                        basePanel, 
                                        schemaPanel, 
                                        basePanel.getTitle(), 
                                        actionListener,
                                        changeTabListener );
            dlg.setVisible(true);

        } else // without dialog

            try { 
                nfo.connect();
            } catch (Exception exc) {
                //exc.printStackTrace();
                String message = MessageFormat.format(bundle.getString("ERR_UnableToConnect"), new String[] {exc.getMessage()}); // NOI18N
                TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            }
    }
}
