/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Vector;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.nodes.Node;

import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.ExceptionListener;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.nodes.RootNode;

//commented out for 3.6 release, need to solve for next Studio release
//import org.netbeans.modules.db.explorer.PointbasePlus;

import org.netbeans.modules.db.explorer.dlg.ConnectionDialog;
import org.netbeans.modules.db.explorer.dlg.NewConnectionPanel;
import org.netbeans.modules.db.explorer.dlg.SchemaPanel;
import org.netbeans.modules.db.explorer.driver.JDBCDriver;
import org.netbeans.modules.db.explorer.driver.JDBCDriverManager;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.explorer.infos.ConnectionOwnerOperations;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.DriverNodeInfo;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

public class ConnectUsingDriverAction extends DatabaseAction {
    static final long serialVersionUID =8245005834483564671L;

    ConnectionDialog dlg;
    ConnectionNodeInfo cni;
    boolean advancedPanel = false;
    boolean okPressed = false;

    protected boolean enable(Node[] activatedNodes) {
        return (activatedNodes != null && activatedNodes.length == 1);
    }

    public void performAction(Node[] activatedNodes) {
        Node node = activatedNodes[0];
        DriverNodeInfo info = (DriverNodeInfo) node.getCookie(DatabaseNodeInfo.class);
        
        if (nodename == null)
            nodename = DatabaseNode.ROOT;
        final ConnectionOwnerOperations nfo = (ConnectionOwnerOperations) info.getParent(nodename);
        
        Vector drvs = new Vector();
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers();
        for (int i = 0; i < drivers.length; i++)
            if (drivers[i].isAvailable())
                drvs.add(drivers[i]);
        
        final DatabaseConnection cinfo = new DatabaseConnection();
        cinfo.setDriverName(info.getName());
        cinfo.setDriver(info.getURL());

        final NewConnectionPanel basePanel = new NewConnectionPanel(drvs, cinfo);
        final SchemaPanel schemaPanel = new SchemaPanel(cinfo);

        PropertyChangeListener argumentListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals("argumentChanged")) { //NOI18N
                    schemaPanel.setSchemas(new Vector(), ""); //NOI18N
                    schemaPanel.resetProgress();
                    try {
                        Connection conn = cinfo.getConnection();
                        if (conn != null && !conn.isClosed())
                            conn.close();
                    } catch (SQLException exc) {
                        //unable to close the connection
                    }

                }
            }
        };
        basePanel.addPropertyChangeListener(argumentListener);

        final PropertyChangeListener connectionListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals("connected")) { //NOI18N
                    if (setSchema(schemaPanel, cinfo))
                        cinfo.setSchema(schemaPanel.getSchema());
                    else {
                        //switch to schema panel
                        dlg.setSelectedComponent(schemaPanel);
                        return;
                    }

                    //connected by "Get Schemas" button in the schema panel => don't create connection node
                    //and don't close the connect dialog
                    if (advancedPanel && !okPressed)
                        return;

                    try {
                        nfo.addConnection(cinfo);
                        RootNode.getOption().save();
                    } catch (DatabaseException exc) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                        String message = MessageFormat.format(bundle.getString("ERR_UnableToAddConnection"), new String[] {exc.getMessage()}); //NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                        try {
                            cinfo.getConnection().close();
                        } catch (SQLException e) {
                            //unable to close db connection
                        }
                        return;
                    }

                    if(dlg != null) {
                        dlg.close();
//                        removeListeners(cinfo);
                    }
                } else
                    okPressed = false;
            }
        };

        final ExceptionListener excListener = new ExceptionListener() {
            public void exceptionOccurred(Exception exc) {
                if (exc instanceof ClassNotFoundException) {
                    String message = MessageFormat.format(bundle.getString("EXC_ClassNotFound"), new String[] {exc.getMessage()}); //NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                } else {
                    String message = MessageFormat.format(bundle.getString("ERR_UnableToAddConnection"), new String[] {exc.getMessage()}); //NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                }
            }
        };

        cinfo.addPropertyChangeListener(connectionListener);
        cinfo.addExceptionListener(excListener);

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (event.getSource() == DialogDescriptor.OK_OPTION) {
                    okPressed = true;
                    basePanel.setConnectionInfo();
                    try {
                        if (cinfo.getConnection() == null || cinfo.getConnection().isClosed())
                            cinfo.connect();
                        else {
                            cinfo.setSchema(schemaPanel.getSchema());
                            nfo.addConnection(cinfo);
                            RootNode.getOption().save();
                            if(dlg != null)
                                dlg.close();
                        }
                    } catch (SQLException exc) {
                        //isClosed() method failed, try to connect
                        cinfo.connect();
                    } catch (DatabaseException exc) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                        String message = MessageFormat.format(bundle.getString("ERR_UnableToAddConnection"), new String[] {exc.getMessage()}); //NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                        try {
                            cinfo.getConnection().close();
                        } catch (SQLException e) {
                            //unable to close db connection
                        }
                    }
                    return;
                }
            }
        };

        ChangeListener changeTabListener = new ChangeListener() {
            public void stateChanged (ChangeEvent e) {
                if (((JTabbedPane) e.getSource()).getSelectedComponent().equals(schemaPanel)) {
                    advancedPanel = true;
                    basePanel.setConnectionInfo();
                } else
                    advancedPanel = false;
            }
        };

        dlg = new ConnectionDialog(basePanel, schemaPanel, basePanel.getTitle(), actionListener, changeTabListener);
        dlg.setVisible(true);
    }

//    private void removeListeners() {
//        cinfo.removePropertyChangeListener(connectionListener);
//        cinfo.removeExceptionListener(excListener);
//    }

    private boolean setSchema(SchemaPanel schemaPanel, DatabaseConnection dbcon) {
        Vector schemas = new Vector();
        try {
            ResultSet rs = dbcon.getConnection().getMetaData().getSchemas();
            if (rs != null)
                while (rs.next())
                    schemas.add(rs.getString(1).trim());
        } catch (SQLException exc) {
//commented out for 3.6 release, need to solve for next Studio release
            // hack for Pointbase Network Server
//            if (dbcon.getDriver().equals(PointbasePlus.DRIVER))
//                if (exc.getErrorCode() == PointbasePlus.ERR_SERVER_REJECTED) {
                    String message = MessageFormat.format(bundle.getString("ERR_UnableObtainSchemas"), new String[] {exc.getMessage()}); // NOI18N
//                    message = MessageFormat.format(bundle.getString("EXC_PointbaseServerRejected"), new String[] {message, dbcon.getDatabase()}); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
//                    schema will be set to null
//                    return true;
//                }
        }

        return schemaPanel.setSchemas(schemas, dbcon.getUser());
    }
}
