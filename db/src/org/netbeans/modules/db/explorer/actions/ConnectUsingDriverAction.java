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

import java.util.Vector;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.JTabbedPane;
import java.sql.*;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;

import org.netbeans.lib.ddl.*;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.RootNode;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;
import org.netbeans.modules.db.explorer.dlg.NewConnectionPanel;
import org.netbeans.modules.db.explorer.dlg.ConnectionDialog;
import org.netbeans.modules.db.explorer.dlg.SchemaPanel;

public class ConnectUsingDriverAction extends DatabaseAction {
    static final long serialVersionUID =8245005834483564671L;
    
    ConnectionDialog dlg = null;

    public void performAction(Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
        else return;

        DriverNodeInfo info = (DriverNodeInfo)node.getCookie(DatabaseNodeInfo.class);
        final ConnectionOwnerOperations nfo = (ConnectionOwnerOperations)info.getParent(nodename);
        Vector drvs = RootNode.getOption().getAvailableDrivers();
        DatabaseConnection cinfo = new DatabaseConnection();
        cinfo.setDriverName(info.getName());
        cinfo.setDriver(info.getURL());

        final NewConnectionPanel basePanel = new NewConnectionPanel(drvs, cinfo);
        final SchemaPanel schemaPanel = new SchemaPanel(new Vector(), new String());
        
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (event.getSource() == DialogDescriptor.OK_OPTION) {
                    dlg.setException(null);
                    basePanel.setConnectionInfo();
                    try {
                        ClassLoader syscl = TopManager.getDefault().currentClassLoader();
                        syscl.loadClass(basePanel.getDriver());
                        if(schemaPanel.getSchema()==null)
                            dlg.setSelectedComponent(schemaPanel);
                        if(dlg.isException()) {
                            schemaPanel.setComment(bundle.getString("MSG_SchemaPanelWarning")); // NOI18N
                            return;
                        }
                        basePanel.getConnection().setSchema(schemaPanel.getSchema());
                        nfo.addConnection(basePanel.getConnection());
                        if(dlg!=null) dlg.close();
                    } catch (ClassNotFoundException exc) {
                        String message = MessageFormat.format(bundle.getString("EXC_ClassNotFound"), new String[] {exc.getMessage()}); //NOI18N
                        TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    } catch(Exception exc) {
                        String message = MessageFormat.format(bundle.getString("ERR_UnableToAddConnection"), new String[] {exc.getMessage()}); //NOI18N
                        TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    }
                }
            }
        };
              
        ChangeListener changeTabListener = new ChangeListener() {
            public void stateChanged (ChangeEvent e) {
                String schemaTemp;
                if(schemaPanel.getSchema()!=null)
                    schemaTemp = schemaPanel.getSchema();
                else
                    schemaTemp = basePanel.getUser();

                if(((JTabbedPane)e.getSource()).getSelectedComponent().equals(schemaPanel)) {
                    basePanel.setConnectionInfo();
                    DBConnection con = basePanel.getConnection();

                    try {
                        ClassLoader syscl = TopManager.getDefault().currentClassLoader();
                        syscl.loadClass(basePanel.getDriver());
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
                            if(!schemaPanel.setSchemas(items, schemaTemp))
                                dlg.setException(new DDLException("User name is not in the list of accessible schemas")); // NOI18N
                        }
                    } catch (SQLException exc) {
                        // hack for Pointbase Network Server
                        String message = MessageFormat.format(bundle.getString("ERR_UnableObtainSchemas"), new String[] {exc.getMessage()}); // NOI18N
                        if(con.getDriver().equals(PointbasePlus.DRIVER))
                            if(exc.getErrorCode()==PointbasePlus.ERR_SERVER_REJECTED)
                                message = MessageFormat.format(bundle.getString("EXC_PointbaseServerRejected"), new String[] {message}); // NOI18N
                        TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                        dlg.setSelectedComponent(basePanel);
                        dlg.setException(new DatabaseException("Unable to obtain schema. "+exc.getMessage())); // NOI18N
                    } catch(Exception exc) {
                        String message = MessageFormat.format(bundle.getString("ERR_UnableObtainSchemas"), new String[] {exc.getMessage()}); //NOI18N
                        TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                        dlg.setSelectedComponent(basePanel);
                        dlg.setException(new DatabaseException("Unable to obtain schema. "+exc.getMessage())); // NOI18N
                    }
                    
                } else
                    if(schemaPanel.getSchema()!=null)
                        schemaTemp = schemaPanel.getSchema();
            }
        };
              
        dlg = new ConnectionDialog(
                        basePanel, 
                        schemaPanel, 
                        basePanel.getTitle(), 
                        actionListener,
                        changeTabListener );

        dlg.setVisible(true);

    }
}
